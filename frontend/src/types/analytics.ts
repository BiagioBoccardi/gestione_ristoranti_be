export interface KpiResponse {
  revenueTotale: number;
  ordiniCompletati: number;
  valoremedioOrdine: number;
  copertiMediPrenotazione: number;
  foodCostMedioPercentuale: number;
}

export interface RevenuePoint {
  etichetta: string;
  revenue: number;
  ordini: number;
}

export interface TopPiatto {
  piattoId: number;
  nome: string;
  quantitaVenduta: number;
  revenueGenerata: number;
}

export interface MetodoPagamentoStat {
  metodo: 'CONTANTI' | 'CARTA' | 'BONIFICO';
  conteggio: number;
  totale: number;
}

export interface FoodCostItem {
  piattoId: number;
  nomePiatto: string;
  prezzoVendita: number;
  costoPorzione: number;
  foodCostPercentuale: number;
  giudizio: 'OTTIMO' | 'BUONO' | 'ATTENZIONE' | 'CRITICO';
}
