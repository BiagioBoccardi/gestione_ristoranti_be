import { Trash2, Pencil, Calendar, Clock, Users, FileText } from 'lucide-react';
import { Button } from '@/components/ui/button';
import type { Prenotazione } from '@/types/prenotazione';

interface PrenotazioneCardProps {
  prenotazione: Prenotazione;
  onModifica: (p: Prenotazione) => void;
  onCancella: (id: number) => void;
  showCliente?: boolean;
}

export default function PrenotazioneCard({
  prenotazione,
  onModifica,
  onCancella,
  showCliente = false,
}: PrenotazioneCardProps) {
  const dataFormatted = new Date(prenotazione.data).toLocaleDateString('it-IT', {
    weekday: 'short', day: '2-digit', month: 'long', year: 'numeric',
  });

  return (
    <div className="bg-white rounded-xl border border-stone-200 p-4 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <span className="text-sm font-semibold text-stone-700">
          Tavolo {prenotazione.numeroTavolo}
        </span>
        {showCliente && (
          <span className="text-xs text-stone-500">{prenotazione.nomeCliente}</span>
        )}
      </div>

      <div className="flex flex-col gap-1.5 text-sm text-stone-600">
        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-stone-400" />
          <span>{dataFormatted}</span>
        </div>
        <div className="flex items-center gap-2">
          <Clock className="w-4 h-4 text-stone-400" />
          <span>{prenotazione.ora.substring(0, 5)}</span>
        </div>
        <div className="flex items-center gap-2">
          <Users className="w-4 h-4 text-stone-400" />
          <span>{prenotazione.coperti} {prenotazione.coperti === 1 ? 'coperto' : 'coperti'}</span>
        </div>
        {prenotazione.note && (
          <div className="flex items-start gap-2">
            <FileText className="w-4 h-4 text-stone-400 mt-0.5" />
            <span className="text-stone-500 italic">{prenotazione.note}</span>
          </div>
        )}
      </div>

      <div className="flex gap-2 border-t border-stone-100 pt-3">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onModifica(prenotazione)}
          className="flex-1 text-stone-600 hover:text-stone-900 gap-1.5 text-xs"
        >
          <Pencil className="w-3 h-3" />
          Modifica
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onCancella(prenotazione.id)}
          className="flex-1 text-red-500 hover:text-red-700 hover:bg-red-50 gap-1.5 text-xs"
        >
          <Trash2 className="w-3 h-3" />
          Cancella
        </Button>
      </div>
    </div>
  );
}
