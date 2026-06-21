import os
import httpx
import anthropic
from fastapi import FastAPI, HTTPException
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

app = FastAPI(title="Ristorante AI Assistant")

BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost/api")
ADMIN_EMAIL = os.getenv("ADMIN_EMAIL", "admin@example.com")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")

_claude = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)

TOOLS: list[anthropic.types.ToolParam] = [
    {
        "name": "get_kpi",
        "description": "Ottieni i KPI del ristorante: revenue totale, numero ordini, media coperti per prenotazione.",
        "input_schema": {"type": "object", "properties": {}, "required": []},
    },
    {
        "name": "get_top_piatti",
        "description": "Ottieni la classifica dei piatti più ordinati/venduti.",
        "input_schema": {
            "type": "object",
            "properties": {
                "limit": {
                    "type": "integer",
                    "description": "Numero massimo di piatti da restituire (default 5).",
                }
            },
            "required": [],
        },
    },
    {
        "name": "get_ordini_recenti",
        "description": "Elenca gli ordini più recenti con stato e totale.",
        "input_schema": {
            "type": "object",
            "properties": {
                "stato": {
                    "type": "string",
                    "description": "Filtra per stato: IN_ATTESA, IN_PREPARAZIONE, PRONTO, CONSEGNATO (opzionale).",
                }
            },
            "required": [],
        },
    },
]


async def _get_token() -> str:
    async with httpx.AsyncClient(timeout=10) as http:
        r = await http.post(
            f"{BACKEND_URL}/auth/login",
            json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
        )
        r.raise_for_status()
        data = r.json()
        return data.get("token") or data.get("data", {}).get("token", "")


async def _call_tool(name: str, inputs: dict, token: str) -> str:
    headers = {"Authorization": f"Bearer {token}"}
    async with httpx.AsyncClient(timeout=10) as http:
        if name == "get_kpi":
            r = await http.get(f"{BACKEND_URL}/analytics/kpi", headers=headers)
            return r.text
        if name == "get_top_piatti":
            limit = inputs.get("limit", 5)
            r = await http.get(
                f"{BACKEND_URL}/analytics/top-piatti",
                params={"limit": limit},
                headers=headers,
            )
            return r.text
        if name == "get_ordini_recenti":
            params = {}
            if stato := inputs.get("stato"):
                params["stato"] = stato
            r = await http.get(f"{BACKEND_URL}/ordini", params=params, headers=headers)
            return r.text
    return f"Tool '{name}' non riconosciuto."


class ChatRequest(BaseModel):
    message: str


@app.post("/api/chat")
async def chat(req: ChatRequest):
    try:
        token = await _get_token()
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Impossibile autenticarsi al backend: {e}")

    messages: list[anthropic.types.MessageParam] = [
        {"role": "user", "content": req.message}
    ]

    for _ in range(10):
        response = _claude.messages.create(
            model="claude-sonnet-4-6",
            max_tokens=1024,
            system=(
                "Sei un assistente AI integrato nel sistema gestionale di un ristorante. "
                "Puoi interrogare i dati reali del ristorante tramite i tool disponibili. "
                "Rispondi sempre in italiano, in modo conciso e professionale."
            ),
            tools=TOOLS,
            messages=messages,
        )

        if response.stop_reason == "end_turn":
            text = next(
                (b.text for b in response.content if hasattr(b, "text")), ""
            )
            return {"response": text}

        if response.stop_reason == "tool_use":
            messages.append({"role": "assistant", "content": response.content})
            tool_results: list[anthropic.types.ToolResultBlockParam] = []
            for block in response.content:
                if block.type == "tool_use":
                    result = await _call_tool(block.name, block.input, token)
                    tool_results.append(
                        {
                            "type": "tool_result",
                            "tool_use_id": block.id,
                            "content": result,
                        }
                    )
            messages.append({"role": "user", "content": tool_results})

    raise HTTPException(status_code=500, detail="Loop tool-calling superato.")


app.mount("/", StaticFiles(directory="static", html=True), name="static")
