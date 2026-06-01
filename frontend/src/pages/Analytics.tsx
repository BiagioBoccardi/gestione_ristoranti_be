import { useEffect, useState } from 'react';
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts';
import {
  TrendingUp, ShoppingBag, Wallet, Users, ChefHat, BarChart3,
} from 'lucide-react';
import { analyticsService } from '@/services/analyticsService';
import type { FoodCostItem, KpiResponse, MetodoPagamentoStat, RevenuePoint, TopPiatto } from '@/types/analytics';
import AppSidebar from '@/components/layout/AppSidebar';

const PIE_COLORS: Record<string, string> = {
  CONTANTI: '#6366f1',
  CARTA:    '#818cf8',
  BONIFICO: '#c7d2fe',
};

const GIUDIZIO_COLORS: Record<string, string> = {
  OTTIMO:     'bg-emerald-950/60 text-emerald-400 ring-1 ring-emerald-800',
  BUONO:      'bg-blue-950/60 text-blue-400 ring-1 ring-blue-800',
  ATTENZIONE: 'bg-amber-950/60 text-amber-400 ring-1 ring-amber-800',
  CRITICO:    'bg-red-950/60 text-red-400 ring-1 ring-red-800',
};

const TOOLTIP_STYLE = {
  border: '1px solid #27272a',
  borderRadius: 10,
  fontSize: 12,
  backgroundColor: '#18181b',
  color: '#e4e4e7',
  boxShadow: '0 8px 24px rgba(0,0,0,.4)',
};

function oggi() { return new Date().toISOString().split('T')[0]; }
function meseFa() {
  const d = new Date();
  d.setDate(d.getDate() - 30);
  return d.toISOString().split('T')[0];
}
function fmt(n: number) {
  return new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' }).format(n);
}

export default function AnalyticsPage() {
  const [kpi,           setKpi]           = useState<KpiResponse | null>(null);
  const [giornaliero,   setGiornaliero]   = useState<RevenuePoint[]>([]);
  const [settimanale,   setSettimanale]   = useState<RevenuePoint[]>([]);
  const [topPiatti,     setTopPiatti]     = useState<TopPiatto[]>([]);
  const [metodi,        setMetodi]        = useState<MetodoPagamentoStat[]>([]);
  const [foodCost,      setFoodCost]      = useState<FoodCostItem[]>([]);
  const [loading,       setLoading]       = useState(true);
  const [periodoGiorni, setPeriodoGiorni] = useState(7);

  useEffect(() => {
    const da = meseFa();
    const a  = oggi();
    setLoading(true);
    Promise.all([
      analyticsService.getKpi(da, a),
      analyticsService.revenueGiornaliero(periodoGiorni),
      analyticsService.revenueSettimanale(8),
      analyticsService.topPiatti(10),
      analyticsService.metodiPagamento(da, a),
      analyticsService.foodCost(),
    ]).then(([k, g, s, tp, m, fc]) => {
      setKpi(k);
      setGiornaliero(g);
      setSettimanale(s);
      setTopPiatti(tp);
      setMetodi(m);
      setFoodCost(fc);
    }).finally(() => setLoading(false));
  }, [periodoGiorni]);

  return (
    <div className="min-h-screen bg-zinc-950">

      {/* Header */}
      <div className="bg-zinc-950 border-b border-zinc-800 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AppSidebar compact />
            <div className="w-9 h-9 rounded-xl bg-indigo-600 flex items-center justify-center shadow-md shadow-indigo-900/50">
              <BarChart3 className="w-4 h-4 text-white" />
            </div>
            <div>
              <h1 className="text-base font-semibold tracking-tight text-zinc-100">Analytics</h1>
              <p className="text-xs text-zinc-500 font-light">Ultimi 30 giorni</p>
            </div>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="flex flex-col items-center gap-3">
            <div className="w-8 h-8 border-2 border-zinc-800 border-t-indigo-500 rounded-full animate-spin" />
            <p className="text-zinc-500 text-xs tracking-widest uppercase">Caricamento</p>
          </div>
        </div>
      ) : (
        <div className="max-w-7xl mx-auto px-6 py-8 space-y-5">

          {/* KPI Cards */}
          <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
            <KpiCard icon={<Wallet className="w-4 h-4" />}    label="Revenue totale"  value={fmt(kpi?.revenueTotale ?? 0)}                              sub="Periodo" />
            <KpiCard icon={<ShoppingBag className="w-4 h-4" />} label="Ordini"         value={String(kpi?.ordiniCompletati ?? 0)}                        sub="Consegnati" />
            <KpiCard icon={<TrendingUp className="w-4 h-4" />}  label="Valore medio"   value={fmt(kpi?.valoremedioOrdine ?? 0)}                          sub="Per ordine" />
            <KpiCard icon={<Users className="w-4 h-4" />}       label="Coperti medi"   value={(kpi?.copertiMediPrenotazione ?? 0).toFixed(1)}             sub="Per prenotazione" />
            <KpiCard icon={<ChefHat className="w-4 h-4" />}     label="Food cost medio" value={`${(kpi?.foodCostMedioPercentuale ?? 0).toFixed(1)}%`}    sub="Sul menu"
              critical={kpi?.foodCostMedioPercentuale != null && kpi.foodCostMedioPercentuale > 40}
            />
          </div>

          {/* Revenue giornaliera */}
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5">
            <div className="flex items-center justify-between mb-5">
              <div>
                <p className="text-sm font-semibold text-zinc-100">Revenue giornaliera</p>
                <p className="text-xs text-zinc-500 font-light mt-0.5">Andamento per giorno</p>
              </div>
              <div className="flex gap-1 bg-zinc-800 p-1 rounded-lg">
                {[7, 14, 30].map(g => (
                  <button
                    key={g}
                    onClick={() => setPeriodoGiorni(g)}
                    className={`px-3 py-1 text-xs rounded-md tracking-widest uppercase font-medium transition-all ${
                      periodoGiorni === g
                        ? 'bg-indigo-600 text-white shadow-sm'
                        : 'text-zinc-500 hover:text-zinc-300'
                    }`}
                  >
                    {g}g
                  </button>
                ))}
              </div>
            </div>
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={giornaliero} margin={{ top: 5, right: 10, left: 10, bottom: 5 }}>
                <defs>
                  <linearGradient id="revGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%"  stopColor="#6366f1" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#27272a" vertical={false} />
                <XAxis dataKey="etichetta" tick={{ fontSize: 11, fill: '#71717a' }} tickLine={false} axisLine={false} />
                <YAxis tick={{ fontSize: 11, fill: '#71717a' }} tickLine={false} axisLine={false} tickFormatter={v => `€${v}`} />
                <Tooltip formatter={(v: unknown) => fmt(Number(v ?? 0))} labelStyle={{ color: '#e4e4e7', fontWeight: 600, fontSize: 12 }} contentStyle={TOOLTIP_STYLE} />
                <Area type="monotone" dataKey="revenue" stroke="#6366f1" strokeWidth={2} fill="url(#revGrad)" dot={false} />
              </AreaChart>
            </ResponsiveContainer>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">

            {/* Revenue settimanale */}
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5">
              <div className="mb-5">
                <p className="text-sm font-semibold text-zinc-100">Revenue settimanale</p>
                <p className="text-xs text-zinc-500 font-light mt-0.5">Ultime 8 settimane</p>
              </div>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={settimanale} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#27272a" vertical={false} />
                  <XAxis dataKey="etichetta" tick={{ fontSize: 10, fill: '#71717a' }} tickLine={false} axisLine={false} />
                  <YAxis tick={{ fontSize: 10, fill: '#71717a' }} tickLine={false} axisLine={false} tickFormatter={v => `€${v}`} />
                  <Tooltip formatter={(v: unknown) => fmt(Number(v ?? 0))} contentStyle={TOOLTIP_STYLE} />
                  <Bar dataKey="revenue" fill="#6366f1" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Metodi di pagamento */}
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5">
              <div className="mb-5">
                <p className="text-sm font-semibold text-zinc-100">Metodi di pagamento</p>
                <p className="text-xs text-zinc-500 font-light mt-0.5">Distribuzione per tipo</p>
              </div>
              {metodi.length === 0 ? (
                <div className="flex items-center justify-center h-48 text-zinc-600 text-sm">Nessun dato disponibile</div>
              ) : (
                <div className="flex items-center gap-6">
                  <ResponsiveContainer width="50%" height={180}>
                    <PieChart>
                      <Pie data={metodi} dataKey="totale" nameKey="metodo" cx="50%" cy="50%" innerRadius={52} outerRadius={76} paddingAngle={3} strokeWidth={0}>
                        {metodi.map(m => (
                          <Cell key={m.metodo} fill={PIE_COLORS[m.metodo] ?? '#4f46e5'} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(v: unknown) => fmt(Number(v ?? 0))} contentStyle={TOOLTIP_STYLE} />
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="space-y-3 flex-1">
                    {metodi.map(m => (
                      <div key={m.metodo} className="flex items-center gap-2.5">
                        <span className="w-2.5 h-2.5 rounded-sm shrink-0" style={{ background: PIE_COLORS[m.metodo] }} />
                        <span className="text-xs text-zinc-500 font-medium uppercase tracking-wide flex-1">{m.metodo}</span>
                        <span className="text-xs font-semibold text-zinc-200">{fmt(m.totale)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Top piatti */}
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-5">
            <div className="mb-5">
              <p className="text-sm font-semibold text-zinc-100">Top 10 piatti venduti</p>
              <p className="text-xs text-zinc-500 font-light mt-0.5">Ordinati per quantità</p>
            </div>
            {topPiatti.length === 0 ? (
              <div className="flex items-center justify-center h-48 text-zinc-600 text-sm">Nessun dato disponibile</div>
            ) : (
              <ResponsiveContainer width="100%" height={Math.max(200, topPiatti.length * 38)}>
                <BarChart layout="vertical" data={topPiatti} margin={{ top: 0, right: 20, left: 10, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#27272a" horizontal={false} />
                  <XAxis type="number" tick={{ fontSize: 11, fill: '#71717a' }} tickLine={false} axisLine={false} />
                  <YAxis type="category" dataKey="nome" width={140} tick={{ fontSize: 11, fill: '#a1a1aa' }} tickLine={false} axisLine={false} />
                  <Tooltip
                    formatter={(v: unknown, name: unknown) => [
                      name === 'quantitaVenduta' ? `${Number(v ?? 0)} pz` : fmt(Number(v ?? 0)),
                      name === 'quantitaVenduta' ? 'Quantità' : 'Revenue',
                    ]}
                    contentStyle={TOOLTIP_STYLE}
                  />
                  <Legend formatter={n => n === 'quantitaVenduta' ? 'Quantità' : 'Revenue'} wrapperStyle={{ fontSize: 11, color: '#71717a' }} />
                  <Bar dataKey="quantitaVenduta" fill="#6366f1" radius={[0, 5, 5, 0]} barSize={13} />
                  <Bar dataKey="revenueGenerata"  fill="#818cf8" radius={[0, 5, 5, 0]} barSize={13} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>

          {/* Food Cost */}
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl overflow-hidden">
            <div className="px-5 py-4 border-b border-zinc-800 flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-zinc-800 flex items-center justify-center border border-zinc-700">
                <ChefHat className="w-4 h-4 text-zinc-400" />
              </div>
              <div>
                <p className="text-sm font-semibold text-zinc-100">Food cost per piatto</p>
                <p className="text-xs text-zinc-500 font-light">Basato sulle ricette inserite</p>
              </div>
            </div>
            {foodCost.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-32 gap-2 text-zinc-600">
                <ChefHat className="w-6 h-6 opacity-40" />
                <p className="text-sm">Nessuna ricetta inserita — aggiungi ingredienti dal pannello admin</p>
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-zinc-950/50 border-b border-zinc-800">
                    <th className="px-5 py-3 text-left text-xs text-zinc-500 uppercase tracking-wider font-semibold">Piatto</th>
                    <th className="px-5 py-3 text-right text-xs text-zinc-500 uppercase tracking-wider font-semibold">Costo porzione</th>
                    <th className="px-5 py-3 text-right text-xs text-zinc-500 uppercase tracking-wider font-semibold">Prezzo vendita</th>
                    <th className="px-5 py-3 text-right text-xs text-zinc-500 uppercase tracking-wider font-semibold">Food cost %</th>
                    <th className="px-5 py-3 text-center text-xs text-zinc-500 uppercase tracking-wider font-semibold">Giudizio</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-800/50">
                  {foodCost.map(fc => (
                    <tr key={fc.piattoId} className="hover:bg-zinc-800/30 transition-colors">
                      <td className="px-5 py-3.5 text-zinc-200 font-medium text-sm">{fc.nomePiatto}</td>
                      <td className="px-5 py-3.5 text-right text-zinc-500 text-sm tabular-nums">{fmt(fc.costoPorzione)}</td>
                      <td className="px-5 py-3.5 text-right text-zinc-500 text-sm tabular-nums">{fmt(fc.prezzoVendita)}</td>
                      <td className="px-5 py-3.5 text-right font-semibold text-zinc-100 text-sm tabular-nums">{fc.foodCostPercentuale.toFixed(1)}%</td>
                      <td className="px-5 py-3.5 text-center">
                        <span className={`inline-flex px-2.5 py-1 rounded-lg text-xs font-semibold tracking-wide ${GIUDIZIO_COLORS[fc.giudizio]}`}>
                          {fc.giudizio}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

        </div>
      )}
    </div>
  );
}

function KpiCard({
  icon, label, value, sub, critical = false,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  sub?: string;
  critical?: boolean;
}) {
  return (
    <div className={`bg-zinc-900 border rounded-xl px-4 py-4 flex flex-col gap-3 hover:border-zinc-700 transition-colors ${
      critical ? 'border-red-900' : 'border-zinc-800'
    }`}>
      <div className={`flex items-center gap-2 ${critical ? 'text-red-400' : 'text-zinc-400'}`}>
        <div className={`w-7 h-7 rounded-lg flex items-center justify-center ${critical ? 'bg-red-950/60' : 'bg-zinc-800'}`}>
          {icon}
        </div>
        <span className="text-xs tracking-widest uppercase font-semibold leading-tight">{label}</span>
      </div>
      <div>
        <p className={`text-2xl font-light tracking-tight ${critical ? 'text-red-400' : 'text-zinc-100'}`}>{value}</p>
        {sub && <p className="text-xs text-zinc-600 font-light mt-0.5">{sub}</p>}
      </div>
    </div>
  );
}
