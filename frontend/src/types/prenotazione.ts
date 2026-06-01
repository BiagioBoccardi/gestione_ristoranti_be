export interface Prenotazione {
  id: number;
  tavoloId: number;
  numeroTavolo: number;
  data: string;
  ora: string;
  coperti: number;
  note?: string;
  nomeCliente: string;
  emailCliente: string;
}

export interface PrenotazionePayload {
  tavoloId: number;
  data: string;
  ora: string;
  coperti: number;
  note?: string;
}
