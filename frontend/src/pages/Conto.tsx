import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Receipt,
  CreditCard,
  Banknote,
  Building2,
  FileText,
  FileSpreadsheet,
  Users,
  CheckCircle2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import AppSidebar from '@/components/layout/AppSidebar';
import { contoService } from '@/services/contoService';
import type { Conto, MetodoPagamento, SplitBill } from '@/types/conto';

const METODI: { value: MetodoPagamento; label: string; icon: React.ReactNode }[] = [
  { value: 'CONTANTI', label: 'Contanti', icon: <Banknote className="w-4 h-4" /> },
  { value: 'CARTA', label: 'Carta', icon: <CreditCard className="w-4 h-4" /> },
  { value: 'BONIFICO', label: 'Bonifico', icon: <Building2 className="w-4 h-4" /> },
];

export default function ContoPage() {
  const { ordineId } = useParams<{ ordineId: string }>();
  const id = Number(ordineId);

  const [conto, setConto] = useState<Conto | null>(null);
  const [metodo, setMetodo] = useState<MetodoPagamento>('CONTANTI');
  const [split, setSplit] = useState<SplitBill | null>(null);
  const [nPersone, setNPersone] = useState(1);
  const [loadingPaga, setLoadingPaga] = useState(false);
  const [loadingPdf, setLoadingPdf] = useState(false);
  const [loadingExcel, setLoadingExcel] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    contoService.apriConto(id)
      .then(setConto)
      .catch(() => setError('Impossibile caricare il conto.'));
  }, [id]);

  useEffect(() => {
    if (!conto) return;
    contoService.calcolaSplit(conto.id, nPersone)
      .then(setSplit)
      .catch(() => setSplit(null));
  }, [conto, nPersone]);

  async function handlePaga() {
    if (!conto) return;
    setLoadingPaga(true);
    try {
      const aggiornato = await contoService.pagaConto(conto.id, { metodo });
      setConto(aggiornato);
    } catch {
      setError('Errore durante il pagamento.');
    } finally {
      setLoadingPaga(false);
    }
  }

  async function handlePdf() {
    if (!conto) return;
    setLoadingPdf(true);
    try {
      await contoService.downloadPdf(conto.id);
    } finally {
      setLoadingPdf(false);
    }
  }

  async function handleExcel() {
    if (!conto) return;
    setLoadingExcel(true);
    try {
      await contoService.downloadExcel(conto.id);
    } finally {
      setLoadingExcel(false);
    }
  }

  if (error) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <p className="text-stone-500 text-sm">{error}</p>
      </div>
    );
  }

  if (!conto) {
    return (
      <div className="min-h-screen bg-stone-50 flex items-center justify-center">
        <p className="text-stone-400 text-sm tracking-widest uppercase">Caricamento…</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-stone-50">
      {/* Header */}
      <div className="bg-white border-b border-stone-200 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Receipt className="w-5 h-5 text-stone-400" />
          <div>
            <h1 className="text-xl font-light tracking-widest uppercase text-stone-800">
              Conto — Tavolo {conto.numeroTavolo}
            </h1>
            <p className="text-xs text-stone-400 mt-0.5">Ordine #{conto.ordineId}</p>
          </div>
        </div>
        <AppSidebar variant="light" />
      </div>

      <div className="max-w-3xl mx-auto px-6 py-10 space-y-8">

        {/* Stato pagamento */}
        {conto.pagato && (
          <div className="flex items-center gap-3 bg-green-50 border border-green-200 rounded-lg px-5 py-4">
            <CheckCircle2 className="w-5 h-5 text-green-600 shrink-0" />
            <div>
              <p className="text-sm font-medium text-green-800">Conto pagato</p>
              <p className="text-xs text-green-600">
                {METODI.find(m => m.value === conto.metodo)?.label}
                {conto.pagamentoIl && ` · ${new Date(conto.pagamentoIl).toLocaleString('it-IT')}`}
              </p>
            </div>
          </div>
        )}

        {/* Dettaglio items */}
        <div className="bg-white border border-stone-200 rounded-lg overflow-hidden">
          <div className="px-5 py-3 border-b border-stone-100 bg-stone-50">
            <p className="text-xs tracking-widest uppercase text-stone-500 font-medium">Dettaglio ordine</p>
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="text-xs text-stone-400 uppercase tracking-wider border-b border-stone-100">
                <th className="px-5 py-3 text-left font-medium">Piatto</th>
                <th className="px-5 py-3 text-center font-medium">Qtà</th>
                <th className="px-5 py-3 text-right font-medium">Prezzo</th>
                <th className="px-5 py-3 text-right font-medium">Subtotale</th>
              </tr>
            </thead>
            <tbody>
              {conto.items.map(item => (
                <tr key={item.id} className="border-b border-stone-50 last:border-0">
                  <td className="px-5 py-3 text-stone-700">
                    {item.nomePiatto}
                    {item.note && (
                      <span className="block text-xs text-stone-400 mt-0.5">{item.note}</span>
                    )}
                  </td>
                  <td className="px-5 py-3 text-center text-stone-600">{item.quantita}</td>
                  <td className="px-5 py-3 text-right text-stone-600">
                    {item.prezzoUnitario.toFixed(2)} €
                  </td>
                  <td className="px-5 py-3 text-right font-medium text-stone-800">
                    {item.subtotale.toFixed(2)} €
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className="border-t border-stone-200 bg-stone-50">
                <td colSpan={3} className="px-5 py-4 text-sm font-medium text-stone-700 uppercase tracking-wider">
                  Totale
                </td>
                <td className="px-5 py-4 text-right text-lg font-semibold text-stone-900">
                  {conto.totale.toFixed(2)} €
                </td>
              </tr>
            </tfoot>
          </table>
        </div>

        {/* Split bill */}
        <div className="bg-white border border-stone-200 rounded-lg px-5 py-5">
          <div className="flex items-center gap-2 mb-4">
            <Users className="w-4 h-4 text-stone-400" />
            <p className="text-xs tracking-widest uppercase text-stone-500 font-medium">Dividi il conto</p>
          </div>
          <div className="flex items-center gap-4">
            <input
              type="number"
              min={1}
              max={20}
              value={nPersone}
              onChange={e => setNPersone(Math.max(1, parseInt(e.target.value) || 1))}
              className="w-20 border border-stone-200 rounded px-3 py-2 text-sm text-center focus:outline-none focus:ring-1 focus:ring-stone-400"
            />
            <p className="text-sm text-stone-500">persone</p>
            {split && (
              <p className="ml-auto text-sm font-medium text-stone-800">
                <span className="text-stone-400 font-normal mr-1">ciascuno</span>
                {split.quotaPerPersona.toFixed(2)} €
              </p>
            )}
          </div>
        </div>

        {/* Pagamento */}
        {!conto.pagato && (
          <div className="bg-white border border-stone-200 rounded-lg px-5 py-5 space-y-4">
            <p className="text-xs tracking-widest uppercase text-stone-500 font-medium">Metodo di pagamento</p>
            <div className="flex gap-3">
              {METODI.map(m => (
                <button
                  key={m.value}
                  onClick={() => setMetodo(m.value)}
                  className={`flex items-center gap-2 px-4 py-2.5 rounded border text-sm transition-colors ${
                    metodo === m.value
                      ? 'border-stone-800 bg-stone-800 text-stone-50'
                      : 'border-stone-200 text-stone-600 hover:bg-stone-50'
                  }`}
                >
                  {m.icon}
                  {m.label}
                </button>
              ))}
            </div>
            <Button
              onClick={handlePaga}
              disabled={loadingPaga}
              className="w-full bg-stone-800 hover:bg-stone-700 text-stone-50 tracking-widest uppercase text-xs h-11"
            >
              {loadingPaga ? 'Elaborazione…' : 'Chiudi e paga'}
            </Button>
          </div>
        )}

        {/* Export */}
        <div className="flex gap-3">
          <Button
            variant="outline"
            onClick={handlePdf}
            disabled={loadingPdf}
            className="flex-1 border-stone-200 text-stone-600 hover:bg-stone-100 tracking-widest uppercase text-xs gap-2"
          >
            <FileText className="w-3.5 h-3.5" />
            {loadingPdf ? 'Generazione…' : 'Esporta PDF'}
          </Button>
          <Button
            variant="outline"
            onClick={handleExcel}
            disabled={loadingExcel}
            className="flex-1 border-stone-200 text-stone-600 hover:bg-stone-100 tracking-widest uppercase text-xs gap-2"
          >
            <FileSpreadsheet className="w-3.5 h-3.5" />
            {loadingExcel ? 'Generazione…' : 'Esporta Excel'}
          </Button>
        </div>

      </div>
    </div>
  );
}
