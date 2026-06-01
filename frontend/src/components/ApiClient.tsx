import axios, { type InternalAxiosRequestConfig } from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

const MAX_RETRIES = 2;
const RETRYABLE_STATUS = new Set([503, 504]);

interface RetryConfig extends InternalAxiosRequestConfig {
  _retryCount?: number;
}

const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10_000,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    // Auto-unwrap RestResponse<T> envelope { status, message, data, timestamp }
    // Skip for binary responses (PDF/Excel downloads)
    const rt = response.config.responseType;
    if (rt !== 'blob' && rt !== 'arraybuffer') {
      const body = response.data;
      if (body !== null && typeof body === 'object' && 'status' in body && 'data' in body) {
        response.data = body.data;
      }
    }
    return response;
  },
  async (error) => {
    const config = error.config as RetryConfig | undefined;
    const status: number | undefined = error.response?.status;

    // Retry on network errors and transient server errors (503/504)
    const isRetryable = !error.response || RETRYABLE_STATUS.has(status!);
    if (config && isRetryable) {
      config._retryCount = (config._retryCount ?? 0) + 1;
      if (config._retryCount <= MAX_RETRIES) {
        await new Promise((r) => setTimeout(r, 500 * config._retryCount!));
        return apiClient(config);
      }
    }

    // 401 outside of /login → force logout
    if (status === 401 && !window.location.pathname.includes('/login')) {
      localStorage.removeItem('token');
      window.location.href = '/login';
      return Promise.reject(error);
    }

    // 403 → redirect to unauthorized page
    if (status === 403) {
      window.location.href = '/unauthorized';
      return Promise.reject(error);
    }

    // Normalize error message for consistent handling in components
    const serverMessage: string | undefined =
      error.response?.data?.message ?? error.response?.data?.error;
    if (serverMessage) {
      error.message = serverMessage;
    } else if (status && status >= 500) {
      error.message = 'Errore del server. Riprova tra qualche istante.';
    } else if (!error.response) {
      error.message = 'Impossibile contattare il server. Controlla la connessione.';
    }

    return Promise.reject(error);
  }
);

export default apiClient;