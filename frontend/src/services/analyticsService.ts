import apiClient from '@/components/ApiClient';
import type {
  FoodCostItem,
  KpiResponse,
  MetodoPagamentoStat,
  RevenuePoint,
  TopPiatto,
} from '@/types/analytics';

export const analyticsService = {
  getKpi: (da: string, a: string): Promise<KpiResponse> =>
    apiClient.get<KpiResponse>('/analytics/kpi', { params: { da, a } }).then(r => r.data),

  revenueGiornaliero: (giorni = 7): Promise<RevenuePoint[]> =>
    apiClient.get<RevenuePoint[]>('/analytics/revenue/giornaliero', { params: { giorni } }).then(r => r.data),

  revenueSettimanale: (settimane = 8): Promise<RevenuePoint[]> =>
    apiClient.get<RevenuePoint[]>('/analytics/revenue/settimanale', { params: { settimane } }).then(r => r.data),

  topPiatti: (limit = 10): Promise<TopPiatto[]> =>
    apiClient.get<TopPiatto[]>('/analytics/top-piatti', { params: { limit } }).then(r => r.data),

  metodiPagamento: (da: string, a: string): Promise<MetodoPagamentoStat[]> =>
    apiClient.get<MetodoPagamentoStat[]>('/analytics/metodi-pagamento', { params: { da, a } }).then(r => r.data),

  foodCost: (): Promise<FoodCostItem[]> =>
    apiClient.get<FoodCostItem[]>('/analytics/food-cost').then(r => r.data),
};
