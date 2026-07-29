import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import * as authApi from '../api/auth';
import { AUTH_TOKEN_KEY } from '../config/api';
import { UserRole } from '../types';
import { apiRoleToUserRole, roleToApiRole } from '../utils/navigation';

interface AuthState {
  token: string | null;
  role: UserRole | null;
  modules: string[];
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (role: UserRole) => Promise<void>;
  logout: () => void;
  restoreSession: () => Promise<void>;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      role: null,
      modules: [],
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (role: UserRole) => {
        set({ isLoading: true, error: null });

        try {
          const response = await authApi.login({ role: roleToApiRole(role) });

          localStorage.setItem(AUTH_TOKEN_KEY, response.token);

          set({
            token: response.token,
            role: apiRoleToUserRole(response.role),
            modules: response.modules,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } catch (error: unknown) {
          const message =
            (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
            'Login failed. Please try again.';

          set({
            isLoading: false,
            error: message,
            isAuthenticated: false,
            token: null,
            role: null,
            modules: [],
          });

          localStorage.removeItem(AUTH_TOKEN_KEY);
          throw new Error(message);
        }
      },

      logout: () => {
        localStorage.removeItem(AUTH_TOKEN_KEY);
        set({
          token: null,
          role: null,
          modules: [],
          isAuthenticated: false,
          isLoading: false,
          error: null,
        });
      },

      restoreSession: async () => {
        const { token } = get();

        if (!token) {
          set({ isAuthenticated: false });
          return;
        }

        set({ isLoading: true });

        try {
          const response = await authApi.getCurrentUser();

          set({
            role: apiRoleToUserRole(response.role),
            modules: response.modules,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } catch {
          localStorage.removeItem(AUTH_TOKEN_KEY);
          set({
            token: null,
            role: null,
            modules: [],
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });
        }
      },

      clearError: () => set({ error: null }),
    }),
    {
      name: 'arc-auth-storage',
      partialize: (state) => ({
        token: state.token,
        role: state.role,
        modules: state.modules,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        if (state?.token) {
          localStorage.setItem(AUTH_TOKEN_KEY, state.token);
        }
      },
    }
  )
);
