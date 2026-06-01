import { memo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Pencil, Receipt } from 'lucide-react';
import { Button } from '@/components/ui/button';
import OrdineStatoBadge from './OrdineStatoBadge';
import StatoOrdineSelector from './StatoOrdineSelector';
import type { Ordine, StatoOrdine } from '@/types/ordine';

interface OrdineCardProps {
  ordine: Ordine;
  onEdit: (ordine: Ordine) => void;
  onCambiaStato: (id: number, stato: StatoOrdine) => Promise<void>;
  canEdit: boolean;
}

function formatOra(isoString: string) {
  return new Date(isoString).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
}

function OrdineCard({ ordine, onEdit, onCambiaStato, canEdit }: OrdineCardProps) {
  const navigate = useNavigate();
  const [loadingStato, setLoadingStato] = useState(false);

  async function handleStatoChange(stato: StatoOrdine) {
    setLoadingStato(true);
    try {
      await onCambiaStato(ordine.id, stato);
    } finally {
      setLoadingStato(false);
    }
  }

  return (
    <div className="bg-white rounded-xl border border-stone-200 p-4 flex flex-col gap-3">
      {/* Header */}
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-stone-500">Ordine #{ordine.id}</span>
          <OrdineStatoBadge stato={ordine.stato} />
        </div>
        <span className="text-xs text-stone-400">{formatOra(ordine.creatoAt)}</span>
      </div>

      {/* Items */}
      <ul className="flex flex-col gap-1">
        {ordine.items.map(item => (
          <li key={item.id} className="flex justify-between text-sm text-stone-700">
            <span>
              <span className="text-stone-400 mr-1">{item.quantita}×</span>
              {item.piatto?.nome ?? '—'}
              {item.note && <span className="text-xs text-stone-400 ml-1">({item.note})</span>}
            </span>
            <span className="text-stone-500">
              {((item.piatto?.prezzo ?? 0) * item.quantita).toFixed(2)} €
            </span>
          </li>
        ))}
      </ul>

      {/* Totale */}
      <div className="flex justify-between items-center border-t border-stone-100 pt-2">
        <span className="text-xs font-medium tracking-widest uppercase text-stone-400">Totale</span>
        <span className="text-sm font-medium text-stone-800">{ordine.totale.toFixed(2)} €</span>
      </div>

      {/* Azioni */}
      <div className="flex items-center justify-between gap-2">
        <StatoOrdineSelector
          stato={ordine.stato}
          onChange={handleStatoChange}
          loading={loadingStato}
        />
        <div className="flex gap-1">
          {ordine.stato === 'CONSEGNATO' && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate(`/conto/${ordine.id}`)}
              className="text-stone-500 hover:text-stone-800 gap-1.5 text-xs h-7 px-2"
            >
              <Receipt className="w-3 h-3" />
              Conto
            </Button>
          )}
          {canEdit && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onEdit(ordine)}
              className="text-stone-500 hover:text-stone-800 gap-1.5 text-xs h-7 px-2"
            >
              <Pencil className="w-3 h-3" />
              Modifica
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}

export default memo(OrdineCard);
