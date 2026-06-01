import { useEffect, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import type { Categoria, CategoriaPayload } from '@/types/menu';

interface CategoriaFormProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: CategoriaPayload) => Promise<void>;
  categoria?: Categoria;
}

export default function CategoriaForm({ open, onClose, onSave, categoria }: CategoriaFormProps) {
  const [nome, setNome] = useState('');
  const [descrizione, setDescrizione] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      setNome(categoria?.nome ?? '');
      setDescrizione(categoria?.descrizione ?? '');
      setError(null);
    }
  }, [open, categoria]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nome.trim()) return;
    setLoading(true);
    setError(null);
    try {
      await onSave({ nome: nome.trim(), descrizione: descrizione.trim() || undefined });
      onClose();
    } catch {
      setError('Errore nel salvataggio. Riprova.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog.Root open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{categoria ? 'Modifica categoria' : 'Nuova categoria'}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="cat-nome" className="text-xs tracking-widest uppercase text-stone-500 font-medium">
              Nome *
            </Label>
            <Input
              id="cat-nome"
              value={nome}
              onChange={e => setNome(e.target.value)}
              placeholder="es. Antipasti"
              required
              disabled={loading}
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="cat-desc" className="text-xs tracking-widest uppercase text-stone-500 font-medium">
              Descrizione
            </Label>
            <Textarea
              id="cat-desc"
              value={descrizione}
              onChange={e => setDescrizione(e.target.value)}
              placeholder="Descrizione opzionale"
              disabled={loading}
            />
          </div>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
              Annulla
            </Button>
            <Button
              type="submit"
              disabled={loading || !nome.trim()}
              className="bg-stone-800 hover:bg-stone-700 text-stone-50"
            >
              {loading ? 'Salvataggio…' : 'Salva'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog.Root>
  );
}
