import { useEffect, useRef, useState } from 'react';
import { ImageIcon, X } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { menuService } from '@/services/menuService';
import type { Categoria, Piatto, PiattoPayload } from '@/types/menu';

interface PiattoFormProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: PiattoPayload) => Promise<void>;
  piatto?: Piatto;
  categorie: Categoria[];
}

export default function PiattoForm({ open, onClose, onSave, piatto, categorie }: PiattoFormProps) {
  const [nome, setNome] = useState('');
  const [descrizione, setDescrizione] = useState('');
  const [prezzo, setPrezzo] = useState('');
  const [disponibile, setDisponibile] = useState(true);
  const [categoriaId, setCategoriaId] = useState<number | ''>('');
  const [immagineUrl, setImmagineUrl] = useState('');
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (open) {
      setNome(piatto?.nome ?? '');
      setDescrizione(piatto?.descrizione ?? '');
      setPrezzo(piatto ? String(piatto.prezzo) : '');
      setDisponibile(piatto?.disponibile ?? true);
      setCategoriaId(piatto?.categoria.id ?? (categorie[0]?.id ?? ''));
      setImmagineUrl(piatto?.immagineUrl ?? '');
      setError(null);
    }
  }, [open, piatto, categorie]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const url = await menuService.uploadImmagine(file);
      setImmagineUrl(url);
    } catch {
      setError('Errore upload immagine. Riprova.');
    } finally {
      setUploading(false);
      if (fileRef.current) fileRef.current.value = '';
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nome.trim() || !prezzo || categoriaId === '') return;
    setLoading(true);
    setError(null);
    try {
      await onSave({
        nome: nome.trim(),
        descrizione: descrizione.trim() || undefined,
        prezzo: parseFloat(prezzo),
        disponibile,
        immagineUrl: immagineUrl || undefined,
        categoriaId: Number(categoriaId),
      });
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
          <DialogTitle>{piatto ? 'Modifica piatto' : 'Nuovo piatto'}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">

          {/* Nome */}
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="piatto-nome" className="text-xs tracking-widest uppercase text-stone-500 font-medium">
              Nome *
            </Label>
            <Input
              id="piatto-nome"
              value={nome}
              onChange={e => setNome(e.target.value)}
              placeholder="es. Tagliatelle al ragù"
              required
              disabled={loading}
            />
          </div>

          {/* Descrizione */}
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="piatto-desc" className="text-xs tracking-widest uppercase text-stone-500 font-medium">
              Descrizione
            </Label>
            <Textarea
              id="piatto-desc"
              value={descrizione}
              onChange={e => setDescrizione(e.target.value)}
              placeholder="Ingredienti, preparazione…"
              disabled={loading}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* Prezzo */}
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="piatto-prezzo" className="text-xs tracking-widest uppercase text-stone-500 font-medium">
                Prezzo (€) *
              </Label>
              <Input
                id="piatto-prezzo"
                type="number"
                min="0"
                step="0.01"
                value={prezzo}
                onChange={e => setPrezzo(e.target.value)}
                placeholder="0.00"
                required
                disabled={loading}
              />
            </div>

            {/* Categoria */}
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="piatto-cat" className="text-xs tracking-widest uppercase text-stone-500 font-medium">
                Categoria *
              </Label>
              <select
                id="piatto-cat"
                value={categoriaId}
                onChange={e => setCategoriaId(Number(e.target.value))}
                required
                disabled={loading}
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs focus-visible:outline-none focus-visible:border-ring disabled:cursor-not-allowed disabled:opacity-50"
              >
                {categorie.map(c => (
                  <option key={c.id} value={c.id}>{c.nome}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Disponibile */}
          <label className="flex items-center gap-2.5 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={disponibile}
              onChange={e => setDisponibile(e.target.checked)}
              disabled={loading}
              className="w-4 h-4 rounded border-stone-300 accent-stone-800"
            />
            <span className="text-sm text-stone-700">Disponibile</span>
          </label>

          {/* Upload immagine */}
          <div className="flex flex-col gap-1.5">
            <Label className="text-xs tracking-widest uppercase text-stone-500 font-medium">
              Immagine
            </Label>
            <input
              ref={fileRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleFileChange}
              disabled={loading || uploading}
            />
            <div className="flex items-center gap-3">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => fileRef.current?.click()}
                disabled={loading || uploading}
                className="shrink-0"
              >
                <ImageIcon className="w-4 h-4 mr-1" />
                {uploading ? 'Caricamento…' : 'Scegli immagine'}
              </Button>
              {immagineUrl && (
                <div className="flex items-center gap-2 flex-1 min-w-0">
                  <img
                    src={immagineUrl}
                    alt="Anteprima"
                    className="w-10 h-10 object-cover rounded-md border border-stone-200 shrink-0"
                  />
                  <button
                    type="button"
                    onClick={() => setImmagineUrl('')}
                    className="text-stone-400 hover:text-stone-600"
                    aria-label="Rimuovi immagine"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              )}
            </div>
          </div>

          {error && <p className="text-sm text-red-600">{error}</p>}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
              Annulla
            </Button>
            <Button
              type="submit"
              disabled={loading || uploading || !nome.trim() || !prezzo || categoriaId === ''}
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
