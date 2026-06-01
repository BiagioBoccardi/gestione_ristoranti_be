export interface Categoria {
  id: number;
  nome: string;
  descrizione?: string;
}

export interface Piatto {
  id: number;
  nome: string;
  descrizione?: string;
  prezzo: number;
  disponibile: boolean;
  immagineUrl?: string;
  categoria: Categoria;
}

export interface PiattoPayload {
  nome: string;
  descrizione?: string;
  prezzo: number;
  disponibile: boolean;
  immagineUrl?: string;
  categoriaId: number;
}

export interface CategoriaPayload {
  nome: string;
  descrizione?: string;
}
