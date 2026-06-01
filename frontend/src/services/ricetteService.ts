import apiClient from '@/components/ApiClient';

export interface Ingrediente {
  id: number;
  nome: string;
  unitaMisura: string;
  costoPerUnita: number;
}

export interface RicettaVoce {
  id: number;
  ingredienteId: number;
  nomeIngrediente: string;
  unitaMisura: string;
  quantita: number;
  percentualeScarto: number;
  costoVoce: number;
}

export interface Ricetta {
  piattoId: number;
  nomePiatto: string;
  prezzoVendita: number;
  voci: RicettaVoce[];
  costoTotale: number;
  foodCostPercentuale: number;
}

export const ricetteService = {
  getIngredienti: () =>
    apiClient.get<Ingrediente[]>('/ingredienti').then(r => r.data),

  creaIngrediente: (body: { nome: string; unitaMisura: string; costoPerUnita: number }) =>
    apiClient.post<Ingrediente>('/ingredienti', body).then(r => r.data),

  aggiornaIngrediente: (id: number, body: { nome: string; unitaMisura: string; costoPerUnita: number }) =>
    apiClient.put<Ingrediente>(`/ingredienti/${id}`, body).then(r => r.data),

  eliminaIngrediente: (id: number) =>
    apiClient.delete(`/ingredienti/${id}`),

  getRicetta: (piattoId: number) =>
    apiClient.get<Ricetta>(`/ricette/${piattoId}`).then(r => r.data),

  aggiungiVoce: (piattoId: number, body: { ingredienteId: number; quantita: number; percentualeScarto: number }) =>
    apiClient.post<Ricetta>(`/ricette/${piattoId}/voci`, body).then(r => r.data),

  eliminaVoce: (piattoId: number, voceId: number) =>
    apiClient.delete(`/ricette/${piattoId}/voci/${voceId}`),
};
