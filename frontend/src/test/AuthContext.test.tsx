/// <reference types="vitest/globals" />
import { render, screen, act } from '@testing-library/react';
import { AuthProvider, useAuth } from '@/context/AuthContext';

// JWT valido fittizio con exp nel futuro (payload: { sub: 'test@test.com', role: 'ADMIN', exp: 9999999999 })
const VALID_TOKEN =
  'eyJhbGciOiJIUzI1NiJ9.' +
  btoa(JSON.stringify({ sub: 'test@test.com', role: 'ADMIN', exp: 9999999999 })) +
  '.signature';

// JWT scaduto (exp nel passato)
const EXPIRED_TOKEN =
  'eyJhbGciOiJIUzI1NiJ9.' +
  btoa(JSON.stringify({ sub: 'test@test.com', role: 'ADMIN', exp: 1 })) +
  '.signature';

function TestConsumer() {
  const { isAuthenticated, user, token, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="auth">{isAuthenticated ? 'autenticato' : 'anonimo'}</span>
      <span data-testid="role">{user?.role ?? 'nessuno'}</span>
      <span data-testid="token">{token ?? 'nessuno'}</span>
      <button onClick={() => login(VALID_TOKEN)}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
}

beforeEach(() => {
  localStorage.clear();
});

describe('AuthContext', () => {
  it('inizia come non autenticato senza token in localStorage', () => {
    render(<AuthProvider><TestConsumer /></AuthProvider>);
    expect(screen.getByTestId('auth').textContent).toBe('anonimo');
  });

  it('login salva token e aggiorna stato autenticazione', async () => {
    render(<AuthProvider><TestConsumer /></AuthProvider>);

    await act(async () => {
      screen.getByText('Login').click();
    });

    expect(screen.getByTestId('auth').textContent).toBe('autenticato');
    expect(screen.getByTestId('role').textContent).toBe('ADMIN');
    expect(localStorage.getItem('token')).toBe(VALID_TOKEN);
  });

  it('logout cancella token e resetta lo stato', async () => {
    render(<AuthProvider><TestConsumer /></AuthProvider>);

    await act(async () => { screen.getByText('Login').click(); });
    await act(async () => { screen.getByText('Logout').click(); });

    expect(screen.getByTestId('auth').textContent).toBe('anonimo');
    expect(screen.getByTestId('token').textContent).toBe('nessuno');
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('token scaduto in localStorage provoca logout automatico al mount', async () => {
    localStorage.setItem('token', EXPIRED_TOKEN);

    await act(async () => {
      render(<AuthProvider><TestConsumer /></AuthProvider>);
    });

    expect(screen.getByTestId('auth').textContent).toBe('anonimo');
  });
});
