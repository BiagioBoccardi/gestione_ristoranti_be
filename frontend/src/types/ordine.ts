export type StatoOrdine =
  | 'IN_ATTESA'
  | 'IN_PREPARAZIONE'
  | 'PRONTO'
  | 'CONSEGNATO';

export type StatoTavolo = 'LIBERO' | 'OCCUPATO' | 'IN_ATTESA_CONTO';

export interface Tavolo {
  id: number;
  numero: number;
  capacita: number;
  stato: StatoTavolo;
  posizione?: string;
  qrToken?: string;
}

export interface TavoloPayload {
  stato: StatoTavolo;
}

export interface OrdineItem {
  id: number;
  piatto?: {
    id: number;
    nome: string;
    prezzo: number;
  };
  prezzoUnitario: number;
  quantita: number;
  note?: string;
}

export interface OrdineItemPayload {
  piattoId: number;
  quantita: number;
  note?: string;
}

export interface Ordine {
  id: number;
  tavoloId: number;
  numeroTavolo: number;
  utenteId?: number;
  nomeUtente?: string;
  stato: StatoOrdine;
  items: OrdineItem[];
  totale: number;
  creatoAt: string;
}

export interface OrdinePayload {
  tavoloId: number;
  items: OrdineItemPayload[];
  note?: string;
}

export interface OrdineUpdatePayload {
  items: OrdineItemPayload[];
  note?: string;
}

export interface StatoOrdinePayload {
  stato: StatoOrdine;
}

export interface OrdineStatoEvent {
  ordineId: number;
  tavoloId: number;
  numeroTavolo: number;
  statoVecchio: StatoOrdine | null;
  statoNuovo: StatoOrdine;
  timestamp: string;
}
