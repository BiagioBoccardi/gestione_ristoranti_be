import apiClient from '@/components/ApiClient';
import type { Categoria, CategoriaPayload, Piatto, PiattoPayload } from '@/types/menu';

export const menuService = {
  getCategorie: (): Promise<Categoria[]> =>
    apiClient.get<Categoria[]>('/menu/categorie').then(r => r.data),

  createCategoria: (data: CategoriaPayload): Promise<Categoria> =>
    apiClient.post<Categoria>('/menu/categorie', data).then(r => r.data),

  updateCategoria: (id: number, data: CategoriaPayload): Promise<Categoria> =>
    apiClient.put<Categoria>(`/menu/categorie/${id}`, data).then(r => r.data),

  deleteCategoria: (id: number): Promise<void> =>
    apiClient.delete(`/menu/categorie/${id}`).then(() => undefined),

  getPiatti: (categoriaId?: number): Promise<Piatto[]> =>
    apiClient
      .get<Piatto[]>('/menu/piatti', { params: categoriaId ? { categoriaId } : {} })
      .then(r => r.data),

  createPiatto: (data: PiattoPayload): Promise<Piatto> =>
    apiClient.post<Piatto>('/menu/piatti', data).then(r => r.data),

  updatePiatto: (id: number, data: PiattoPayload): Promise<Piatto> =>
    apiClient.put<Piatto>(`/menu/piatti/${id}`, data).then(r => r.data),

  deletePiatto: (id: number): Promise<void> =>
    apiClient.delete(`/menu/piatti/${id}`).then(() => undefined),

  uploadImmagine: (file: File): Promise<string> =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = () => reject(new Error('Lettura file fallita'));
      reader.readAsDataURL(file);
    }),
};
