import { useEffect, useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { staffService } from '@/services/staffService';
import type { StaffMembro } from '@/types/staff';

interface Props {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  utente?: StaffMembro | null;
}

const RUOLI = ['ADMIN', 'CAMERIERE', 'CUOCO'];

export default function UtenteForm({ open, onClose, onSaved, utente }: Props) {
  const isEdit = !!utente;

  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [ruolo, setRuolo] = useState('CAMERIERE');
  const [nuovaPassword, setNuovaPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      setNome(utente?.nome ?? '');
      setEmail(utente?.email ?? '');
      setRuolo(utente?.ruolo ?? 'CAMERIERE');
      setNuovaPassword('');
      setError('');
    }
  }, [open, utente]);

  const handleSave = async () => {
    setError('');
    if (!nome.trim() || !email.trim()) {
      setError('Nome ed email sono obbligatori');
      return;
    }

    setSaving(true);
    try {
      if (isEdit) {
        await staffService.aggiornaStaff(utente!.id, {
          nome: nome.trim(),
          email: email.trim(),
          ruolo,
          ...(nuovaPassword.trim() ? { nuovaPassword: nuovaPassword.trim() } : {}),
        });
      } else {
        await staffService.creaUtente({
          nome: nome.trim(),
          email: email.trim(),
          ruolo,
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
          <DialogTitle>{isEdit ? 'Modifica membro staff' : 'Nuovo membro staff'}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {error && (
            <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
              {error}
            </p>
          )}

          <div className="space-y-1.5">
            <Label htmlFor="sf-nome">Nome</Label>
            <Input
              id="sf-nome"
              value={nome}
              onChange={e => setNome(e.target.value)}
              placeholder="Mario Rossi"
            />
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="sf-email">Email</Label>
            <Input
              id="sf-email"
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="mario@restora.it"
            />
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="sf-ruolo">Ruolo</Label>
            <select
              id="sf-ruolo"
              value={ruolo}
              onChange={e => setRuolo(e.target.value)}
              className="w-full h-9 px-3 rounded-lg border border-stone-200 bg-white text-sm text-stone-800 focus:outline-none focus:ring-1 focus:ring-indigo-400"
            >
              {RUOLI.map(r => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>

          {!isEdit ? (
            <p className="text-xs text-stone-500 bg-stone-50 border border-stone-200 rounded-lg px-3 py-2">
              Verrà inviata un'email all'utente con un link per impostare la propria password.
            </p>
          ) : (
            <div className="space-y-1.5">
              <Label htmlFor="sf-nuovaPassword">
                Nuova password{' '}
                <span className="text-stone-400 font-normal">(lascia vuoto per non cambiare)</span>
              </Label>
              <Input
                id="sf-nuovaPassword"
                type="password"
                value={nuovaPassword}
                onChange={e => setNuovaPassword(e.target.value)}
                placeholder="••••••••"
              />
            </div>
          )}
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
