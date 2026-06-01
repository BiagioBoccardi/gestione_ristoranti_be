import apiClient from '@/components/ApiClient';
import type { Tavolo, TavoloPayload, Ordine } from '@/types/ordine';

export const tavoloService = {
  getTavoli: (): Promise<Tavolo[]> =>
    apiClient.get<Tavolo[]>('/tavoli').then(r => r.data),

  getTavolo: (id: number): Promise<Tavolo> =>
    apiClient.get<Tavolo>(`/tavoli/${id}`).then(r => r.data),

  updateTavolo: (id: number, data: TavoloPayload): Promise<Tavolo> =>
    apiClient.put<Tavolo>(`/tavoli/${id}`, data).then(r => r.data),

  getOrdiniByTavolo: (tavoloId: number): Promise<Ordine[]> =>
    apiClient.get<Ordine[]>(`/ordini/tavolo/${tavoloId}`).then(r => r.data),
};
