import { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { tavoloService } from '@/services/tavoloService';
import type { Tavolo } from '@/types/ordine';
import type { Prenotazione, PrenotazionePayload } from '@/types/prenotazione';

interface PrenotazioneFormProps {
  prenotazioneEsistente?: Prenotazione;
  datiPrecompilati?: { data?: string; ora?: string };
  onSubmit: (data: PrenotazionePayload) => Promise<void>;
  onAnnulla: () => void;
}

export default function PrenotazioneForm({
  prenotazioneEsistente,
  datiPrecompilati,
  onSubmit,
  onAnnulla,
}: PrenotazioneFormProps) {
  const [tavoli, setTavoli] = useState<Tavolo[]>([]);
  const [loading, setLoading] = useState(false);
  const [errore, setErrore] = useState<string | null>(null);

  const oggi = new Date().toISOString().split('T')[0];

  const [form, setForm] = useState<PrenotazionePayload>({
    tavoloId: prenotazioneEsistente?.tavoloId ?? 0,
    data: prenotazioneEsistente?.data ?? datiPrecompilati?.data ?? '',
    ora: prenotazioneEsistente?.ora?.substring(0, 5) ?? datiPrecompilati?.ora ?? '',
    coperti: prenotazioneEsistente?.coperti ?? 2,
    note: prenotazioneEsistente?.note ?? '',
  });

  useEffect(() => {
    tavoloService.getTavoli().then(setTavoli);
  }, []);

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: name === 'tavoloId' || name === 'coperti' ? Number(value) : value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErrore(null);

    if (!form.tavoloId) return setErrore('Seleziona un tavolo.');
    if (!form.data) return setErrore('Inserisci una data.');
    if (!form.ora) return setErrore('Inserisci un orario.');
    if (form.coperti < 1) return setErrore('Il numero di coperti deve essere almeno 1.');

    setLoading(true);
    try {
      await onSubmit({ ...form, ora: form.ora + ':00' });
    } catch {
      setErrore('Si è verificato un errore. Verifica che il tavolo sia disponibile in quella fascia oraria.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <label className="text-xs font-medium text-stone-600">Tavolo</label>
        <select
          name="tavoloId"
          value={form.tavoloId}
          onChange={handleChange}
          className="border border-stone-200 rounded-lg px-3 py-2 text-sm text-stone-700 bg-white focus:outline-none focus:ring-2 focus:ring-stone-300"
        >
          <option value={0}>Seleziona un tavolo...</option>
          {tavoli.map(t => (
            <option key={t.id} value={t.id}>
              Tavolo {t.numero} — {t.capacita} posti
            </option>
          ))}
        </select>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-stone-600">Data</label>
          <input
            type="date"
            name="data"
            min={oggi}
            value={form.data}
            onChange={handleChange}
            className="border border-stone-200 rounded-lg px-3 py-2 text-sm text-stone-700 focus:outline-none focus:ring-2 focus:ring-stone-300"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-stone-600">Orario</label>
          <input
            type="time"
            name="ora"
            value={form.ora}
            onChange={handleChange}
            className="border border-stone-200 rounded-lg px-3 py-2 text-sm text-stone-700 focus:outline-none focus:ring-2 focus:ring-stone-300"
          />
        </div>
      </div>

      <div className="flex flex-col gap-1">
        <label className="text-xs font-medium text-stone-600">Numero coperti</label>
        <input
          type="number"
          name="coperti"
          min={1}
          max={20}
          value={form.coperti}
          onChange={handleChange}
          className="border border-stone-200 rounded-lg px-3 py-2 text-sm text-stone-700 focus:outline-none focus:ring-2 focus:ring-stone-300"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label className="text-xs font-medium text-stone-600">Note (opzionale)</label>
        <textarea
          name="note"
          rows={2}
          value={form.note}
          onChange={handleChange}
          placeholder="Es. allergie, occasione speciale..."
          className="border border-stone-200 rounded-lg px-3 py-2 text-sm text-stone-700 resize-none focus:outline-none focus:ring-2 focus:ring-stone-300"
        />
      </div>

      {errore && (
        <p className="text-xs text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {errore}
        </p>
      )}

      <div className="flex gap-2 pt-1">
        <Button type="button" variant="ghost" onClick={onAnnulla} className="flex-1">
          Annulla
        </Button>
        <Button type="submit" disabled={loading} className="flex-1">
          {loading ? 'Salvataggio...' : prenotazioneEsistente ? 'Aggiorna' : 'Prenota'}
        </Button>
      </div>
    </form>
  );
}
