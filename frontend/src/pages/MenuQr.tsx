import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { ShoppingCart, Plus, Minus, Trash2, UtensilsCrossed, CheckCircle } from 'lucide-react';
import { qrService } from '@/services/qrService';
import type { MenuQrResponse, CategoriaMenuResponse, PiattoMenuResponse, CarrelloItem } from '@/types/qr';

type Fase = 'menu' | 'carrello' | 'conferma';

export default function MenuQrPage() {
  const { token } = useParams<{ token: string }>();

  const [menu, setMenu] = useState<MenuQrResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [carrello, setCarrello] = useState<CarrelloItem[]>([]);
  const [fase, setFase] = useState<Fase>('menu');
  const [invio, setInvio] = useState(false);
  const [invioErrore, setInvioErrore] = useState<string | null>(null);
  const [categoriaAttiva, setCategoriaAttiva] = useState<number | null>(null);

  useEffect(() => {
    if (!token) return;
    qrService.getMenuByToken(token)
      .then(data => {
        setMenu(data);
        if (data.categorie.length > 0) setCategoriaAttiva(data.categorie[0].id);
      })
      .catch(() => setError('QR code non valido o scaduto.'))
      .finally(() => setLoading(false));
  }, [token]);

  function aggiungi(piatto: PiattoMenuResponse) {
    setCarrello(prev => {
      const ex = prev.find(i => i.piattoId === piatto.id);
      if (ex) return prev.map(i => i.piattoId === piatto.id ? { ...i, quantita: i.quantita + 1 } : i);
      return [...prev, { piattoId: piatto.id, nome: piatto.nome, prezzo: piatto.prezzo, quantita: 1 }];
    });
  }

  function rimuovi(piattoId: number) {
    setCarrello(prev => {
      const ex = prev.find(i => i.piattoId === piattoId);
      if (!ex) return prev;
      if (ex.quantita === 1) return prev.filter(i => i.piattoId !== piattoId);
      return prev.map(i => i.piattoId === piattoId ? { ...i, quantita: i.quantita - 1 } : i);
    });
  }

  function quantita(piattoId: number) {
    return carrello.find(i => i.piattoId === piattoId)?.quantita ?? 0;
  }

  const totale = carrello.reduce((s, i) => s + i.prezzo * i.quantita, 0);
  const nItems = carrello.reduce((s, i) => s + i.quantita, 0);

  async function inviaOrdine() {
    if (!token || !menu || carrello.length === 0) return;
    setInvio(true);
    setInvioErrore(null);
    try {
      await qrService.creaOrdineQr(token, {
        tavoloId: menu.tavoloId,
        items: carrello.map(i => ({ piattoId: i.piattoId, quantita: i.quantita })),
      });
      setCarrello([]);
      setFase('conferma');
    } catch {
      setInvioErrore('Errore durante l\'invio dell\'ordine. Riprova.');
    } finally {
      setInvio(false);
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-stone-300 border-t-stone-700 rounded-full animate-spin" />
      </div>
    );
  }

  if (error || !menu) {
    return (
      <div className="min-h-screen bg-stone-50 flex flex-col items-center justify-center gap-4 px-6 text-center">
        <UtensilsCrossed className="w-10 h-10 text-stone-300" />
        <p className="text-stone-600">{error ?? 'Menu non disponibile.'}</p>
      </div>
    );
  }

  if (fase === 'conferma') {
    return (
      <div className="min-h-screen bg-stone-50 flex flex-col items-center justify-center gap-5 px-6 text-center">
        <CheckCircle className="w-16 h-16 text-green-500" />
        <div>
          <h2 className="text-xl font-semibold text-stone-800 mb-1">Ordine inviato!</h2>
          <p className="text-sm text-stone-500">Il tuo ordine è stato trasmesso alla cucina.</p>
        </div>
        <button
          onClick={() => setFase('menu')}
          className="mt-2 text-sm text-stone-500 underline underline-offset-2"
        >
          Torna al menu
        </button>
      </div>
    );
  }

  const categoriaSelezionata: CategoriaMenuResponse | undefined =
    menu.categorie.find(c => c.id === categoriaAttiva);

  return (
    <div className="min-h-screen bg-stone-50 flex flex-col max-w-lg mx-auto">

      {/* Header */}
      <header className="sticky top-0 z-10 bg-white border-b border-stone-100 px-4 py-3">
        <p className="text-xs tracking-widest uppercase text-stone-400 font-medium">Tavolo {menu.numeroTavolo}</p>
        <h1 className="text-xl font-semibold text-stone-800 leading-tight">Menu</h1>
      </header>

      {fase === 'menu' && (
        <>
          {/* Tabs categorie */}
          <div className="sticky top-[61px] z-10 bg-white border-b border-stone-100 flex gap-1 overflow-x-auto px-4 py-2 no-scrollbar">
            {menu.categorie.map(cat => (
              <button
                key={cat.id}
                onClick={() => setCategoriaAttiva(cat.id)}
                className={[
                  'shrink-0 px-3 py-1.5 rounded-full text-sm font-medium transition-colors',
                  cat.id === categoriaAttiva
                    ? 'bg-stone-800 text-white'
                    : 'bg-stone-100 text-stone-600 hover:bg-stone-200',
                ].join(' ')}
              >
                {cat.nome}
              </button>
            ))}
          </div>

          {/* Piatti */}
          <div className="flex-1 overflow-y-auto px-4 py-4 pb-28 flex flex-col gap-3">
            {categoriaSelezionata?.piatti.map(piatto => {
              const q = quantita(piatto.id);
              return (
                <div key={piatto.id} className="bg-white rounded-2xl shadow-sm border border-stone-100 p-4 flex gap-4">
                  {piatto.foto && (
                    <img
                      src={piatto.foto}
                      alt={piatto.nome}
                      className="w-20 h-20 rounded-xl object-cover shrink-0"
                    />
                  )}
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-stone-800 text-sm leading-snug">{piatto.nome}</p>
                    {piatto.descrizione && (
                      <p className="text-xs text-stone-400 mt-0.5 line-clamp-2">{piatto.descrizione}</p>
                    )}
                    <div className="flex items-center justify-between mt-3">
                      <span className="text-sm font-semibold text-stone-700">€{piatto.prezzo.toFixed(2)}</span>
                      {q === 0 ? (
                        <button
                          onClick={() => aggiungi(piatto)}
                          className="flex items-center gap-1 bg-stone-800 text-white text-xs px-3 py-1.5 rounded-full hover:bg-stone-700 transition-colors"
                        >
                          <Plus className="w-3 h-3" />
                          Aggiungi
                        </button>
                      ) : (
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => rimuovi(piatto.id)}
                            className="w-7 h-7 flex items-center justify-center rounded-full border border-stone-200 text-stone-600 hover:bg-stone-100 transition-colors"
                          >
                            <Minus className="w-3 h-3" />
                          </button>
                          <span className="text-sm font-semibold text-stone-800 w-4 text-center">{q}</span>
                          <button
                            onClick={() => aggiungi(piatto)}
                            className="w-7 h-7 flex items-center justify-center rounded-full bg-stone-800 text-white hover:bg-stone-700 transition-colors"
                          >
                            <Plus className="w-3 h-3" />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* FAB carrello */}
          {nItems > 0 && (
            <div className="fixed bottom-6 left-1/2 -translate-x-1/2 w-full max-w-lg px-4">
              <button
                onClick={() => setFase('carrello')}
                className="w-full bg-stone-800 text-white rounded-2xl py-4 flex items-center justify-between px-5 shadow-lg hover:bg-stone-700 transition-colors"
              >
                <span className="flex items-center gap-2 text-sm font-medium">
                  <ShoppingCart className="w-4 h-4" />
                  Vedi carrello ({nItems})
                </span>
                <span className="text-sm font-semibold">€{totale.toFixed(2)}</span>
              </button>
            </div>
          )}
        </>
      )}

      {fase === 'carrello' && (
        <div className="flex-1 flex flex-col">
          <div className="flex-1 overflow-y-auto px-4 py-4 flex flex-col gap-3">
            <h2 className="text-base font-semibold text-stone-800 mb-1">Il tuo ordine</h2>
            {carrello.map(item => (
              <div key={item.piattoId} className="bg-white rounded-2xl border border-stone-100 p-4 flex items-center gap-3 shadow-sm">
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-stone-800 truncate">{item.nome}</p>
                  <p className="text-xs text-stone-400 mt-0.5">€{item.prezzo.toFixed(2)} × {item.quantita}</p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <button onClick={() => rimuovi(item.piattoId)} className="w-7 h-7 flex items-center justify-center rounded-full border border-stone-200 text-stone-600 hover:bg-stone-100 transition-colors">
                    <Minus className="w-3 h-3" />
                  </button>
                  <span className="text-sm font-semibold text-stone-800 w-4 text-center">{item.quantita}</span>
                  <button onClick={() => aggiungi({ id: item.piattoId, nome: item.nome, prezzo: item.prezzo, descrizione: null, foto: null })} className="w-7 h-7 flex items-center justify-center rounded-full bg-stone-800 text-white hover:bg-stone-700 transition-colors">
                    <Plus className="w-3 h-3" />
                  </button>
                  <button onClick={() => setCarrello(c => c.filter(i => i.piattoId !== item.piattoId))} className="w-7 h-7 flex items-center justify-center rounded-full text-red-400 hover:bg-red-50 transition-colors ml-1">
                    <Trash2 className="w-3 h-3" />
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="px-4 pb-8 pt-3 border-t border-stone-100 bg-white flex flex-col gap-3">
            <div className="flex items-center justify-between text-stone-800">
              <span className="font-medium">Totale</span>
              <span className="text-lg font-semibold">€{totale.toFixed(2)}</span>
            </div>
            {invioErrore && (
              <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-xl px-3 py-2 text-center">
                {invioErrore}
              </p>
            )}
            <button
              onClick={inviaOrdine}
              disabled={invio}
              className="w-full bg-stone-800 text-white rounded-2xl py-4 text-sm font-semibold hover:bg-stone-700 transition-colors disabled:opacity-60"
            >
              {invio ? 'Invio in corso...' : 'Invia ordine alla cucina'}
            </button>
            <button
              onClick={() => setFase('menu')}
              className="w-full text-stone-500 text-sm py-2 hover:text-stone-800 transition-colors"
            >
              ← Torna al menu
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
