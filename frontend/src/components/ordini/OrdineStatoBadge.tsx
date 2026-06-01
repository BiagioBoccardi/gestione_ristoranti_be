import { Badge } from '@/components/ui/badge';
import type { StatoOrdine } from '@/types/ordine';

const STATO_CONFIG: Record<StatoOrdine, { label: string; variant: 'default' | 'warning' | 'success' | 'secondary' | 'destructive' }> = {
  IN_ATTESA:        { label: 'In attesa',       variant: 'default' },
  IN_PREPARAZIONE:  { label: 'In preparazione', variant: 'warning' },
  PRONTO:           { label: 'Pronto',          variant: 'success' },
  CONSEGNATO:       { label: 'Consegnato',      variant: 'secondary' },
};

interface OrdineStatoBadgeProps {
  stato: StatoOrdine;
}

export default function OrdineStatoBadge({ stato }: OrdineStatoBadgeProps) {
  const { label, variant } = STATO_CONFIG[stato] ?? STATO_CONFIG.IN_ATTESA;
  return <Badge variant={variant}>{label}</Badge>;
}
