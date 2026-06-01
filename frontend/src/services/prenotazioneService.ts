import apiClient from '@/components/ApiClient';
import type { Prenotazione, PrenotazionePayload } from '@/types/prenotazione';

export const prenotazioneService = {
  crea: (data: PrenotazionePayload): Promise<Prenotazione> =>
    apiClient.post<Prenotazione>('/prenotazioni', data).then(r => r.data),

  getMie: (): Promise<Prenotazione[]> =>
    apiClient.get<Prenotazione[]>('/prenotazioni/mie').then(r => r.data),

  getPerData: (data: string): Promise<Prenotazione[]> =>
    apiClient.get<Prenotazione[]>('/prenotazioni', { params: { data } }).then(r => r.data),

  modifica: (id: number, data: PrenotazionePayload): Promise<Prenotazione> =>
    apiClient.put<Prenotazione>(`/prenotazioni/${id}`, data).then(r => r.data),

  cancella: (id: number): Promise<void> =>
    apiClient.delete(`/prenotazioni/${id}`).then(() => undefined),
};
