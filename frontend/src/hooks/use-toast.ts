import { useToast as useToastContext } from '@/context/ToastContext';

interface ToastOptions {
  title: string;
  description?: string;
}

export function useToast() {
  const { success } = useToastContext();
  return {
    toast: ({ title, description }: ToastOptions) =>
      success(description ? `${title}: ${description}` : title),
  };
}
