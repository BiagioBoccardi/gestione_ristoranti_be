/// <reference types="vitest/globals" />
import { menuService } from '@/services/menuService';
import apiClient from '@/components/ApiClient';

vi.mock('@/components/ApiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

const mockApiClient = apiClient as {
  get: ReturnType<typeof vi.fn>;
  post: ReturnType<typeof vi.fn>;
  put: ReturnType<typeof vi.fn>;
  delete: ReturnType<typeof vi.fn>;
};

const piatto = { id: 1, nome: 'Pizza', prezzo: 10, disponibile: true };
const categoria = { id: 1, nome: 'Antipasti' };

describe('menuService', () => {
  beforeEach(() => vi.clearAllMocks());

  it('getPiatti restituisce lista piatti', async () => {
    mockApiClient.get.mockResolvedValue({ data: [piatto] });

    const result = await menuService.getPiatti();

    expect(mockApiClient.get).toHaveBeenCalledWith('/menu/piatti', { params: {} });
    expect(result).toEqual([piatto]);
  });

  it('getPiatti con categoriaId passa il parametro corretto', async () => {
    mockApiClient.get.mockResolvedValue({ data: [piatto] });

    await menuService.getPiatti(5);

    expect(mockApiClient.get).toHaveBeenCalledWith('/menu/piatti', { params: { categoriaId: 5 } });
  });

  it('getCategorie restituisce lista categorie', async () => {
    mockApiClient.get.mockResolvedValue({ data: [categoria] });

    const result = await menuService.getCategorie();

    expect(mockApiClient.get).toHaveBeenCalledWith('/menu/categorie');
    expect(result).toEqual([categoria]);
  });

  it('createPiatto chiama POST con il payload corretto', async () => {
    const payload = { nome: 'Pasta', prezzo: 8, categoriaId: 1, disponibile: true };
    mockApiClient.post.mockResolvedValue({ data: { id: 2, ...payload } });

    const result = await menuService.createPiatto(payload as any);

    expect(mockApiClient.post).toHaveBeenCalledWith('/menu/piatti', payload);
    expect(result.nome).toBe('Pasta');
  });

  it('deletePiatto chiama DELETE sull\'endpoint corretto', async () => {
    mockApiClient.delete.mockResolvedValue({});

    await menuService.deletePiatto(3);

    expect(mockApiClient.delete).toHaveBeenCalledWith('/menu/piatti/3');
  });

  it('updateCategoria chiama PUT con id e payload', async () => {
    const payload = { nome: 'Primi' };
    mockApiClient.put.mockResolvedValue({ data: { id: 1, ...payload } });

    await menuService.updateCategoria(1, payload as any);

    expect(mockApiClient.put).toHaveBeenCalledWith('/menu/categorie/1', payload);
  });
});
