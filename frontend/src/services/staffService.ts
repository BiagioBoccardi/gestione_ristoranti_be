import apiClient from '@/components/ApiClient';
import type {
  AggiornaStaffPayload,
  CreaUtentePayload,
  StaffMembro,
  TurnoItem,
  TurnoPayload,
  TurnoUpdatePayload,
} from '@/types/staff';

export const staffService = {
  getStaff: (): Promise<StaffMembro[]> =>
    apiClient.get<StaffMembro[]>('/staff').then(r => r.data),

  getStaffById: (id: number): Promise<StaffMembro> =>
    apiClient.get<StaffMembro>(`/staff/${id}`).then(r => r.data),

  creaUtente: (data: CreaUtentePayload): Promise<{ messaggio: string }> =>
    apiClient.post('/admin/crea-utente', data).then(r => r.data),

  aggiornaStaff: (id: number, data: AggiornaStaffPayload): Promise<StaffMembro> =>
    apiClient.put<StaffMembro>(`/staff/${id}`, data).then(r => r.data),

  eliminaStaff: (id: number): Promise<void> =>
    apiClient.delete(`/staff/${id}`).then(() => undefined),

  getTurni: (): Promise<TurnoItem[]> =>
    apiClient.get<TurnoItem[]>('/staff/turni').then(r => r.data),

  getTurniByUtente: (utenteId: number): Promise<TurnoItem[]> =>
    apiClient.get<TurnoItem[]>(`/staff/${utenteId}/turni`).then(r => r.data),

  creaTurno: (data: TurnoPayload): Promise<TurnoItem> =>
    apiClient.post<TurnoItem>('/staff/turni', data).then(r => r.data),

  aggiornaTurno: (id: number, data: TurnoUpdatePayload): Promise<TurnoItem> =>
    apiClient.put<TurnoItem>(`/staff/turni/${id}`, data).then(r => r.data),

  eliminaTurno: (id: number): Promise<void> =>
    apiClient.delete(`/staff/turni/${id}`).then(() => undefined),
};
