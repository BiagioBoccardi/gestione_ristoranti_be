import apiClient from '@/components/ApiClient';

export interface UtenteAdmin {
  id: number;
  nome: string;
  email: string;
  ruolo: string;
}

export const adminService = {
  getUtenti: (): Promise<UtenteAdmin[]> =>
    apiClient.get<UtenteAdmin[]>('/admin/utenti').then(r => r.data),

  aggiornaRuolo: (id: number, ruolo: string): Promise<UtenteAdmin> =>
    apiClient.put<UtenteAdmin>(`/admin/utenti/${id}/ruolo`, { ruolo }).then(r => r.data),

  eliminaUtente: (id: number): Promise<void> =>
    apiClient.delete(`/admin/utenti/${id}`).then(() => undefined),
};
