import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { jwtDecode } from 'jwt-decode';
import type { User, AuthState } from '../types/auth';

interface AuthContextType extends AuthState {
    login: (token: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [authState, setAuthState] = useState<AuthState>({
        user: null,
        token: localStorage.getItem('token'),
        isAuthenticated: !!localStorage.getItem('token'),
    });

    // Effetto per decodificare il token al caricamento se presente
    useEffect(() => {
        if (authState.token) {
            try {
                const decoded: User = jwtDecode(authState.token);
                // Controllo scadenza token (opzionale ma consigliato)
                if (decoded.exp * 1000 < Date.now()) {
                    logout();
                } else {
                    setAuthState(prev => ({ ...prev, user: decoded, isAuthenticated: true }));
                }
            } catch (error) {
                logout();
            }
        }
    }, [authState.token]);

    const login = (token: string) => {
        localStorage.setItem('token', token);
        const decoded: User = jwtDecode(token);
        setAuthState({
            token,
            user: decoded,
            isAuthenticated: true,
        });
    };

    const logout = () => {
        localStorage.clear();
        sessionStorage.clear();
        setAuthState({
            token: null,
            user: null,
            isAuthenticated: false,
        });
    };

    return (
        <AuthContext.Provider value={{ ...authState, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

// Hook personalizzato per usare il contesto facilmente
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth deve essere usato all\'interno di un AuthProvider');
    }
    return context;
};