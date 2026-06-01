export interface QrCodeInfo {
  tavoloId: number;
  numeroTavolo: number;
  qrToken: string | null;
  menuUrl: string | null;
}

export interface PiattoMenuResponse {
  id: number;
  nome: string;
  descrizione: string | null;
  prezzo: number;
  foto: string | null;
}

export interface CategoriaMenuResponse {
  id: number;
  nome: string;
  descrizione: string | null;
  piatti: PiattoMenuResponse[];
}

export interface MenuQrResponse {
  tavoloId: number;
  numeroTavolo: number;
  categorie: CategoriaMenuResponse[];
}

export interface CarrelloItem {
  piattoId: number;
  nome: string;
  prezzo: number;
  quantita: number;
}
