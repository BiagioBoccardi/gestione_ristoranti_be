import { Pencil, Trash2, UtensilsCrossed } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import type { Piatto } from '@/types/menu';

interface PiattoCardProps {
  piatto: Piatto;
  onEdit: (piatto: Piatto) => void;
  onDelete: (id: number) => void;
  canEdit: boolean;
}

export default function PiattoCard({ piatto, onEdit, onDelete, canEdit }: PiattoCardProps) {
  return (
    <div className="group/piatto bg-white rounded-xl border border-stone-200 shadow-xs overflow-hidden flex flex-col">
      {/* Immagine */}
      <div className="relative h-40 bg-stone-100 flex items-center justify-center shrink-0">
        {piatto.immagineUrl ? (
          <img
            src={piatto.immagineUrl}
            alt={piatto.nome}
            className="w-full h-full object-cover"
          />
        ) : (
          <UtensilsCrossed className="w-10 h-10 text-stone-300" />
        )}
        <div className="absolute top-2 right-2">
          <Badge variant={piatto.disponibile ? 'success' : 'destructive'}>
            {piatto.disponibile ? 'Disponibile' : 'Non disponibile'}
          </Badge>
        </div>
      </div>

      {/* Contenuto */}
      <div className="flex flex-col gap-2 p-4 flex-1">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-medium text-stone-800 leading-tight">{piatto.nome}</h3>
          <span className="text-base font-semibold text-stone-800 shrink-0">
            €{piatto.prezzo.toFixed(2)}
          </span>
        </div>

        {piatto.descrizione && (
          <p className="text-sm text-stone-500 line-clamp-2 leading-relaxed">
            {piatto.descrizione}
          </p>
        )}

        <div className="mt-auto pt-2 flex items-center justify-between">
          <Badge variant="secondary">{piatto.categoria.nome}</Badge>
          {canEdit && (
            <div className="flex items-center gap-1.5 opacity-0 group-hover/piatto:opacity-100 transition-opacity">
              <Button
                variant="ghost"
                size="icon-sm"
                onClick={() => onEdit(piatto)}
                aria-label="Modifica piatto"
              >
                <Pencil className="w-3.5 h-3.5" />
              </Button>
              <Button
                variant="ghost"
                size="icon-sm"
                onClick={() => onDelete(piatto.id)}
                aria-label="Elimina piatto"
                className="text-red-500 hover:text-red-700 hover:bg-red-50"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
