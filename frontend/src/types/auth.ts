export const Role = {
    ADMIN:     'ROLE_ADMIN',
    CAMERIERE: 'ROLE_CAMERIERE',
    CUOCO:     'ROLE_CUOCO',
    CLIENTE:   'ROLE_CLIENTE',
} as const;

export type RoleType = typeof Role[keyof typeof Role];

export interface User {
    sub: string;
    role: RoleType;
    exp: number;
}

export interface AuthState {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
}
