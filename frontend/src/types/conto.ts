export type MetodoPagamento = 'CONTANTI' | 'CARTA' | 'BONIFICO';

export interface ContoItem {
  id: number;
  nomePiatto: string;
  quantita: number;
  prezzoUnitario: number;
  subtotale: number;
  note?: string;
}

export interface Conto {
  id: number;
  ordineId: number;
  numeroTavolo: number;
  items: ContoItem[];
  totale: number;
  pagato: boolean;
  metodo: MetodoPagamento;
  pagamentoIl?: string;
}

export interface PagaContoPayload {
  metodo: MetodoPagamento;
}

export interface SplitBill {
  totale: number;
  nPersone: number;
  quotaPerPersona: number;
}
