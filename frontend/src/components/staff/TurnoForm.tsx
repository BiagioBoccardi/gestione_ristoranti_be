import { useEffect, useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { staffService } from '@/services/staffService';
import type { StaffMembro, StatoTurno, TurnoItem } from '@/types/staff';

interface Props {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  staff: StaffMembro[];
  turno?: TurnoItem | null;
}

const STATI: StatoTurno[] = ['PIANIFICATO', 'IN_CORSO', 'COMPLETATO'];

const selectClass =
  'w-full h-9 px-3 rounded-lg border border-stone-200 bg-white text-sm text-stone-800 focus:outline-none focus:ring-1 focus:ring-indigo-400';

function toDatetimeLocal(iso: string | null | undefined): string {
  if (!iso) return '';
  return iso.slice(0, 16);
}

function toIso(dt: string): string {
  return dt ? dt + ':00' : '';
}

export default function TurnoForm({ open, onClose, onSaved, staff, turno }: Props) {
  const isEdit = !!turno;

  const [utenteId, setUtenteId] = useState<number>(0);
  const [dataInizio, setDataInizio] = useState('');
  const [dataFine, setDataFine] = useState('');
  const [stato, setStato] = useState<StatoTurno>('PIANIFICATO');
  const [note, setNote] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      setUtenteId(turno?.utenteId ?? staff[0]?.id ?? 0);
      setDataInizio(toDatetimeLocal(turno?.dataInizio));
      setDataFine(toDatetimeLocal(turno?.dataFine));
      setStato(turno?.stato ?? 'PIANIFICATO');
      setNote(turno?.note ?? '');
      setError('');
    }
  }, [open, turno, staff]);

  const handleSave = async () => {
    setError('');
    if (!utenteId) { setError('Seleziona un membro dello staff'); return; }
    if (!dataInizio) { setError('La data di inizio è obbligatoria'); return; }

    setSaving(true);
    try {
      if (isEdit) {
        await staffService.aggiornaTurno(turno!.id, {
          utenteId,
          dataInizio: toIso(dataInizio),
          ...(dataFine ? { dataFine: toIso(dataFine) } : {}),
          stato,
          ...(note.trim() ? { note: note.trim() } : {}),
        });
      } else {
        await staffService.creaTurno({
          utenteId,
          dataInizio: toIso(dataInizio),
          ...(dataFine ? { dataFine: toIso(dataFine) } : {}),
          stato,
          ...(note.trim() ? { note: note.trim() } : {}),
        });
      }
      onSaved();
      onClose();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      setError(err?.response?.data?.message ?? 'Si è verificato un errore');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog.Root open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose(); }}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Modifica turno' : 'Nuovo turno'}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {error && (
            <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
              {error}
            </p>
          )}

          <div className="space-y-1.5">
            <Label htmlFor="tf-utente">Membro staff</Label>
            <select
              id="tf-utente"
              value={utenteId}
              onChange={e => setUtenteId(Number(e.target.value))}
              className={selectClass}
            >
              {staff.map(s => (
                <option key={s.id} value={s.id}>
                  {s.nome} ({s.ruolo})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="tf-inizio">Inizio</Label>
              <input
                id="tf-inizio"
                type="datetime-local"
                value={dataInizio}
                onChange={e => setDataInizio(e.target.value)}
                className={selectClass}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="tf-fine">
                Fine{' '}
                <span className="text-stone-400 font-normal">(opzionale)</span>
              </Label>
              <input
                id="tf-fine"
                type="datetime-local"
                value={dataFine}
                onChange={e => setDataFine(e.target.value)}
                className={selectClass}
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="tf-stato">Stato</Label>
            <select
              id="tf-stato"
              value={stato}
              onChange={e => setStato(e.target.value as StatoTurno)}
              className={selectClass}
            >
              {STATI.map(s => (
                <option key={s} value={s}>{s.replace('_', ' ')}</option>
              ))}
            </select>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="tf-note">Note</Label>
            <textarea
              id="tf-note"
              value={note}
              onChange={e => setNote(e.target.value)}
              rows={2}
              placeholder="Note opzionali..."
              className="w-full px-3 py-2 rounded-lg border border-stone-200 bg-white text-sm text-stone-800 focus:outline-none focus:ring-1 focus:ring-indigo-400 resize-none"
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={saving}>
            Annulla
          </Button>
          <Button
            onClick={handleSave}
            disabled={saving}
            className="bg-indigo-600 hover:bg-indigo-500 text-white"
          >
            {saving ? 'Salvataggio...' : 'Salva'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog.Root>
  );
}
