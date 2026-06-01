export interface StaffMembro {
  id: number;
  nome: string;
  email: string;
  ruolo: string;
  nrTurni: number;
}

export type StatoTurno = 'PIANIFICATO' | 'IN_CORSO' | 'COMPLETATO';

export interface TurnoItem {
  id: number;
  utenteId: number;
  utenteNome: string;
  dataInizio: string;
  dataFine: string | null;
  stato: StatoTurno;
  note: string | null;
}

export interface CreaUtentePayload {
  nome: string;
  email: string;
  password: string;
  ruolo: string;
}

export interface AggiornaStaffPayload {
  nome?: string;
  email?: string;
  ruolo?: string;
  nuovaPassword?: string;
}

export interface TurnoPayload {
  utenteId: number;
  dataInizio: string;
  dataFine?: string;
  stato?: StatoTurno;
  note?: string;
}

export interface TurnoUpdatePayload {
  utenteId?: number;
  dataInizio?: string;
  dataFine?: string;
  stato?: StatoTurno;
  note?: string;
}
