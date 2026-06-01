import apiClient from '@/components/ApiClient';
import type { Conto, PagaContoPayload, SplitBill } from '@/types/conto';

export const contoService = {
  apriConto: (ordineId: number): Promise<Conto> =>
    apiClient.post<Conto>(`/conti/${ordineId}`).then(r => r.data),

  getConto: (ordineId: number): Promise<Conto> =>
    apiClient.get<Conto>(`/conti/${ordineId}`).then(r => r.data),

  pagaConto: (contoId: number, payload: PagaContoPayload): Promise<Conto> =>
    apiClient.put<Conto>(`/conti/${contoId}/paga`, payload).then(r => r.data),

  calcolaSplit: (contoId: number, persone: number): Promise<SplitBill> =>
    apiClient.get<SplitBill>(`/conti/${contoId}/split`, { params: { persone } }).then(r => r.data),

  downloadPdf: async (contoId: number): Promise<void> => {
    const res = await apiClient.get(`/conti/${contoId}/export/pdf`, { responseType: 'blob' });
    const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
    const a = document.createElement('a');
    a.href = url;
    a.download = `conto-${contoId}.pdf`;
    a.click();
    URL.revokeObjectURL(url);
  },

  downloadExcel: async (contoId: number): Promise<void> => {
    const res = await apiClient.get(`/conti/${contoId}/export/excel`, { responseType: 'blob' });
    const url = URL.createObjectURL(new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    }));
    const a = document.createElement('a');
    a.href = url;
    a.download = `conto-${contoId}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
  },
};
