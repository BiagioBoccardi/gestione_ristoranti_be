import apiClient from '@/components/ApiClient';
import type { QrCodeInfo, MenuQrResponse } from '@/types/qr';
import type { OrdinePayload, Ordine } from '@/types/ordine';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

export const qrService = {
  // --- Endpoint autenticati (ADMIN) ---

  getQrInfo: (tavoloId: number): Promise<QrCodeInfo> =>
    apiClient.get<QrCodeInfo>(`/qr/${tavoloId}/info`).then(r => r.data),

  getQrImageUrl: (tavoloId: number): string =>
    `${BASE_URL}/qr/${tavoloId}`,

  getQrImageBlob: (tavoloId: number): Promise<string> =>
    apiClient.get<Blob>(`/qr/${tavoloId}`, { responseType: 'blob' })
      .then(r => URL.createObjectURL(r.data)),

  rigeneraQr: (tavoloId: number): Promise<QrCodeInfo> =>
    apiClient.post(`/qr/${tavoloId}/rigenera`).then(() =>
      apiClient.get<QrCodeInfo>(`/qr/${tavoloId}/info`).then(r => r.data)
    ),

  // --- Endpoint pubblico (no auth) ---

  getMenuByToken: (token: string): Promise<MenuQrResponse> =>
    fetch(`${BASE_URL}/public/menu/${token}`)
      .then(r => {
        if (!r.ok) throw new Error('QR code non valido');
        return r.json() as Promise<MenuQrResponse>;
      }),

  creaOrdineQr: (token: string, payload: OrdinePayload): Promise<Ordine> =>
    fetch(`${BASE_URL}/public/ordini/qr/${token}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }).then(r => {
      if (!r.ok) throw new Error('Errore durante l\'invio dell\'ordine');
      return r.json() as Promise<Ordine>;
    }),
};
