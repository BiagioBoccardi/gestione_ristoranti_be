import apiClient from '@/components/ApiClient';
import type { Ordine, OrdinePayload, OrdineUpdatePayload, StatoOrdine } from '@/types/ordine';

export const ordineService = {
  getOrdini: (params?: { stato?: StatoOrdine; tavoloId?: number }): Promise<Ordine[]> =>
    apiClient.get<Ordine[]>('/ordini', { params }).then(r => r.data),

  getOrdine: (id: number): Promise<Ordine> =>
    apiClient.get<Ordine>(`/ordini/${id}`).then(r => r.data),

  createOrdine: (data: OrdinePayload): Promise<Ordine> =>
    apiClient.post<Ordine>('/ordini', data).then(r => r.data),

  updateOrdine: (id: number, data: OrdineUpdatePayload): Promise<Ordine> =>
    apiClient.patch<Ordine>(`/ordini/${id}`, data).then(r => r.data),

  updateStatoOrdine: (id: number, stato: StatoOrdine): Promise<Ordine> =>
    apiClient.patch<Ordine>(`/ordini/${id}/stato`, { nuovoStato: stato }).then(r => r.data),
};
