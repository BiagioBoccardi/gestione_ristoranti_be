import { useCallback, useEffect, useRef, useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import type { DateSelectArg, EventClickArg } from '@fullcalendar/core';
import { CalendarDays, Clock, Users, FileText, Pencil, Trash2, Plus, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import AppSidebar from '@/components/layout/AppSidebar';
import { useToast } from '@/hooks/use-toast';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import PrenotazioneForm from '@/components/prenotazioni/PrenotazioneForm';
import { prenotazioneService } from '@/services/prenotazioneService';
import { useAuth } from '@/context/AuthContext';
import { Role } from '@/types/auth';
import type { Prenotazione, PrenotazionePayload } from '@/types/prenotazione';
import '@/styles/calendar.css';

function buildGoogleCalendarUrl(p: Prenotazione, nomeRistorante = 'Ristorante') {
  const [y, m, d] = p.data.split('-');
  const [h, min] = p.ora.substring(0, 5).split(':');
  const pad = (n: string | number) => String(n).padStart(2, '0');
  const start = `${y}${pad(m)}${pad(d)}T${pad(h)}${pad(min)}00`;
  const endDate = new Date(Number(y), Number(m) - 1, Number(d), Number(h) + 2, Number(min));
  const end = `${endDate.getFullYear()}${pad(endDate.getMonth() + 1)}${pad(endDate.getDate())}T${pad(endDate.getHours())}${pad(endDate.getMinutes())}00`;
  const params = new URLSearchParams({
    action: 'TEMPLATE',
    text: `Prenotazione — ${nomeRistorante}`,
    dates: `${start}/${end}`,
    details: `Tavolo ${p.numeroTavolo} · ${p.coperti} coperti${p.note ? ` · ${p.note}` : ''}`,
  });
  return `https://calendar.google.com/calendar/render?${params.toString()}`;
}

function prenotazioneToEvent(p: Prenotazione) {
  const [h, min] = p.ora.substring(0, 5).split(':');
  const endH = String(Number(h) + 2).padStart(2, '0');
  return {
    id: String(p.id),
    title: `Tavolo ${p.numeroTavolo} · ${p.coperti} cop.`,
    start: `${p.data}T${p.ora.substring(0, 5)}`,
    end: `${p.data}T${endH}:${min}`,
    extendedProps: { prenotazione: p },
    backgroundColor: '#292524',
    borderColor: '#292524',
    textColor: '#fafaf9',
  };
}

export default function PrenotazioniPage() {
  const { user } = useAuth();
  const { toast } = useToast();
  const isStaff = user?.role === Role.ADMIN || user?.role === Role.CAMERIERE;
  const calendarRef = useRef<FullCalendar>(null);

  const [prenotazioni, setPrenotazioni] = useState<Prenotazione[]>([]);
  const [dettaglio, setDettaglio] = useState<Prenotazione | null>(null);
  const [inModifica, setInModifica] = useState<Prenotazione | null>(null);
  const [dataIniziale, setDataIniziale] = useState('');
  const [oraIniziale, setOraIniziale] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [googleUrl, setGoogleUrl] = useState<string | null>(null);

  const carica = useCallback(async () => {
    try {
      const data = isStaff
        ? await prenotazioneService.getPerData(new Date().toISOString().split('T')[0])
        : await prenotazioneService.getMie();
      setPrenotazioni(data);
    } catch {
      console.error('Impossibile caricare le prenotazioni.');
    }
  }, [isStaff]);

  useEffect(() => { carica(); }, [carica]);

  async function handleSubmit(payload: PrenotazionePayload) {
    let saved: Prenotazione;
    if (inModifica) {
      saved = await prenotazioneService.modifica(inModifica.id, payload);
      toast({ title: 'Prenotazione aggiornata', description: 'Le modifiche sono state salvate.' });
    } else {
      saved = await prenotazioneService.crea(payload);
      toast({ title: 'Prenotazione confermata', description: `Tavolo ${saved.numeroTavolo} il ${saved.data} alle ${saved.ora.substring(0, 5)}.` });
    }
    setShowForm(false);
    setInModifica(null);
    setGoogleUrl(buildGoogleCalendarUrl(saved));
    setDettaglio(saved);
    carica();
  }

  async function handleCancella(id: number) {
    if (!confirm('Confermi la cancellazione della prenotazione?')) return;
    await prenotazioneService.cancella(id);
    setDettaglio(null);
    setGoogleUrl(null);
    toast({ title: 'Prenotazione cancellata', description: 'La prenotazione è stata rimossa.' });
    carica();
  }

  function handleModifica(p: Prenotazione) {
    setInModifica(p);
    setDataIniziale(p.data);
    setOraIniziale(p.ora.substring(0, 5));
    setDettaglio(null);
    setShowForm(true);
  }

  function handleDateSelect(arg: DateSelectArg) {
    const data = arg.startStr.split('T')[0];
    const ora = arg.startStr.includes('T') ? arg.startStr.split('T')[1].substring(0, 5) : '20:00';
    setDataIniziale(data);
    setOraIniziale(ora);
    setInModifica(null);
    setShowForm(true);
  }

  function handleEventClick(arg: EventClickArg) {
    const p: Prenotazione = arg.event.extendedProps.prenotazione;
    setDettaglio(p);
    setGoogleUrl(buildGoogleCalendarUrl(p));
  }

  const events = prenotazioni.map(prenotazioneToEvent);

  return (
    <div className="min-h-screen bg-stone-50">
      {/* Header */}
      <div className="bg-white border-b border-stone-200 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <AppSidebar variant="light" compact />
          <div>
            <h1 className="text-xl font-light tracking-widest uppercase text-stone-800">Prenotazioni</h1>
            <p className="text-xs text-stone-400 mt-0.5">
              {isStaff ? 'Clicca su un evento per i dettagli' : 'Clicca su un giorno per prenotare'}
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          <Button
            onClick={() => { setInModifica(null); setDataIniziale(''); setOraIniziale(''); setShowForm(true); }}
            className="gap-2 bg-stone-800 hover:bg-stone-700 text-stone-50 text-xs tracking-widest uppercase"
          >
            <Plus className="w-3.5 h-3.5" />
            Nuova
          </Button>
        </div>
      </div>

      <div className="p-6 max-w-6xl mx-auto">
        <div className="bg-white rounded-xl border border-stone-200 p-5">
          <FullCalendar
            ref={calendarRef}
            plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
            initialView="dayGridMonth"
            locale="it"
            headerToolbar={{
              left: 'prev,next today',
              center: 'title',
              right: 'dayGridMonth,timeGridWeek',
            }}
            buttonText={{ today: 'Oggi', month: 'Mese', week: 'Settimana' }}
            events={events}
            selectable={true}
            selectMirror={true}
            select={handleDateSelect}
            eventClick={handleEventClick}
            height="auto"
            eventDisplay="block"
            dayMaxEvents={3}
            nowIndicator={true}
          />
        </div>
      </div>

      {/* Modale form (nuova / modifica) */}
      <Dialog.Root open={showForm} onOpenChange={(o: boolean) => { if (!o) { setShowForm(false); setInModifica(null); } }}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center justify-between pr-4">
              <span className="tracking-widest uppercase text-sm font-medium text-stone-700">
                {inModifica ? 'Modifica prenotazione' : 'Nuova prenotazione'}
              </span>
              <button onClick={() => { setShowForm(false); setInModifica(null); }} className="text-stone-400 hover:text-stone-600">
                <X className="w-4 h-4" />
              </button>
            </DialogTitle>
          </DialogHeader>
          <PrenotazioneForm
            prenotazioneEsistente={
              inModifica
                ? { ...inModifica, data: dataIniziale || inModifica.data, ora: oraIniziale || inModifica.ora }
                : undefined
            }
            datiPrecompilati={
              !inModifica && dataIniziale
                ? { data: dataIniziale, ora: oraIniziale }
                : undefined
            }
            onSubmit={handleSubmit}
            onAnnulla={() => { setShowForm(false); setInModifica(null); }}
          />
        </DialogContent>
      </Dialog.Root>

      {/* Modale dettaglio */}
      <Dialog.Root open={!!dettaglio} onOpenChange={(o: boolean) => { if (!o) { setDettaglio(null); setGoogleUrl(null); } }}>
        <DialogContent className="max-w-sm">
          {dettaglio && (
            <>
              <DialogHeader>
                <DialogTitle className="flex items-center justify-between pr-4">
                  <span className="tracking-widest uppercase text-sm font-medium text-stone-700">
                    Tavolo {dettaglio.numeroTavolo}
                  </span>
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => handleModifica(dettaglio)}
                      className="p-1.5 rounded-lg text-stone-400 hover:text-stone-700 hover:bg-stone-100 transition-colors"
                      title="Modifica"
                    >
                      <Pencil className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleCancella(dettaglio.id)}
                      className="p-1.5 rounded-lg text-stone-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                      title="Cancella"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => { setDettaglio(null); setGoogleUrl(null); }}
                      className="p-1.5 rounded-lg text-stone-400 hover:text-stone-700 hover:bg-stone-100 transition-colors"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </DialogTitle>
              </DialogHeader>

              <div className="flex flex-col gap-3 mt-1">
                <div className="flex flex-col gap-2.5 text-sm text-stone-600">
                  <div className="flex items-center gap-3">
                    <CalendarDays className="w-4 h-4 text-stone-400 shrink-0" />
                    <span>
                      {new Date(dettaglio.data).toLocaleDateString('it-IT', {
                        weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
                      })}
                    </span>
                  </div>
                  <div className="flex items-center gap-3">
                    <Clock className="w-4 h-4 text-stone-400 shrink-0" />
                    <span>{dettaglio.ora.substring(0, 5)}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <Users className="w-4 h-4 text-stone-400 shrink-0" />
                    <span>{dettaglio.coperti} {dettaglio.coperti === 1 ? 'coperto' : 'coperti'}</span>
                  </div>
                  {dettaglio.note && (
                    <div className="flex items-start gap-3">
                      <FileText className="w-4 h-4 text-stone-400 shrink-0 mt-0.5" />
                      <span className="italic text-stone-500">{dettaglio.note}</span>
                    </div>
                  )}
                  {isStaff && (
                    <div className="pt-2 border-t border-stone-100 text-xs text-stone-400">
                      Cliente: <span className="text-stone-600 font-medium">{dettaglio.nomeCliente}</span>
                      {' · '}{dettaglio.emailCliente}
                    </div>
                  )}
                </div>

                {googleUrl && (
                  <a
                    href={googleUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center justify-center gap-2 w-full text-xs py-2.5 px-3 rounded-lg border border-stone-200 text-stone-600 hover:bg-stone-50 transition-colors font-medium tracking-wide"
                  >
                    <img src="https://www.google.com/favicon.ico" alt="" className="w-3.5 h-3.5" />
                    Aggiungi a Google Calendar
                  </a>
                )}
              </div>
            </>
          )}
        </DialogContent>
      </Dialog.Root>
    </div>
  );
}
