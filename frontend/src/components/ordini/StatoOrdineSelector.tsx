import type { StatoOrdine } from '@/types/ordine';

const PROSSIMO_STATO: Partial<Record<StatoOrdine, StatoOrdine>> = {
  IN_ATTESA:       'IN_PREPARAZIONE',
  IN_PREPARAZIONE: 'PRONTO',
  PRONTO:          'CONSEGNATO',
};

const LABEL_PROSSIMO: Partial<Record<StatoOrdine, string>> = {
  IN_ATTESA:       'Inizia preparazione',
  IN_PREPARAZIONE: 'Segna pronto',
  PRONTO:          'Consegna',
};

interface StatoOrdineSelectorProps {
  stato: StatoOrdine;
  onChange: (stato: StatoOrdine) => void;
  loading?: boolean;
}

export default function StatoOrdineSelector({ stato, onChange, loading }: StatoOrdineSelectorProps) {
  const prossimo = PROSSIMO_STATO[stato];
  if (!prossimo) return null;

  return (
    <button
      disabled={loading}
      onClick={() => onChange(prossimo)}
      className="text-xs border border-stone-300 rounded-lg px-2 py-1 bg-white text-stone-700 hover:bg-stone-50 disabled:opacity-50 cursor-pointer transition-colors"
    >
      {loading ? '…' : LABEL_PROSSIMO[stato]}
    </button>
  );
}
