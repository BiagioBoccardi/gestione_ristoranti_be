import apiClient from '@/components/ApiClient';

interface LoginResponse {
  token: string;
}

export const authService = {
  login: async (email: string, password: string): Promise<LoginResponse> => {
    const { data } = await apiClient.post<LoginResponse>('/auth/login', { email, password });
    return data;
  },

  register: async (nome: string, email: string, password: string): Promise<void> => {
    await apiClient.post('/auth/register', { nome, email, password });
  },

  forgotPassword: async (email: string): Promise<void> => {
    await apiClient.post('/auth/forgot-password', { email });
  },

  resetPassword: async (token: string, nuovaPassword: string): Promise<void> => {
    await apiClient.post('/auth/reset-password', { token, nuovaPassword });
  },
};