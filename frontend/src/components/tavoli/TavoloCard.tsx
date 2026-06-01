import { Users } from 'lucide-react';
import type { Tavolo, StatoTavolo } from '@/types/ordine';

const STATO_STYLE: Record<StatoTavolo, { bg: string; border: string; text: string; label: string }> = {
  LIBERO:           { bg: 'bg-green-50',  border: 'border-green-200',  text: 'text-green-700',  label: 'Libero' },
  OCCUPATO:         { bg: 'bg-amber-50',  border: 'border-amber-200',  text: 'text-amber-700',  label: 'Occupato' },
  IN_ATTESA_CONTO:  { bg: 'bg-orange-50', border: 'border-orange-300', text: 'text-orange-700', label: 'Conto' },
};

interface TavoloCardProps {
  tavolo: Tavolo;
  selected: boolean;
  onClick: (id: number) => void;
}

export default function TavoloCard({ tavolo, selected, onClick }: TavoloCardProps) {
  const style = STATO_STYLE[tavolo.stato] ?? STATO_STYLE.LIBERO;

  return (
    <button
      onClick={() => onClick(tavolo.id)}
      className={[
        'flex flex-col items-center justify-center gap-2 rounded-xl border-2 p-4 h-28 w-full transition-all cursor-pointer',
        style.bg,
        style.border,
        selected ? 'ring-2 ring-stone-800 ring-offset-1 shadow-md' : 'hover:shadow-sm',
      ].join(' ')}
    >
      <span className="text-2xl font-light text-stone-800">{tavolo.numero}</span>
      <span className={`text-xs font-semibold tracking-widest uppercase ${style.text}`}>
        {style.label}
      </span>
      <span className="flex items-center gap-1 text-xs text-stone-400">
        <Users className="w-3 h-3" />
        {tavolo.capacita}
      </span>
    </button>
  );
}
