import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

import { getApiErrorMessage } from '../../services/apiClient';
import { authService } from '../../services/authService';
import { tokenStorage } from '../../services/tokenStorage';
import type { LoginRequest, RegisterRequest, User } from '../../types/auth';

type AuthStatus = 'loading' | 'idle';

type AuthContextValue = {
  user: User | null;
  status: AuthStatus;
  login: (request: LoginRequest) => Promise<void>;
  register: (request: RegisterRequest) => Promise<void>;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

type AuthProviderProps = {
  children: ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [status, setStatus] = useState<AuthStatus>(() =>
    tokenStorage.get() ? 'loading' : 'idle',
  );

  const logout = useCallback(() => {
    tokenStorage.clear();
    setUser(null);
    setStatus('idle');
  }, []);

  useEffect(() => {
    let active = true;

    async function loadCurrentUser() {
      if (!tokenStorage.get()) {
        setStatus('idle');
        return;
      }

      try {
        const currentUser = await authService.getCurrentUser();
        if (active) {
          setUser(currentUser);
        }
      } catch (error) {
        if (active) {
          tokenStorage.clear();
          setUser(null);
          console.warn(getApiErrorMessage(error));
        }
      } finally {
        if (active) {
          setStatus('idle');
        }
      }
    }

    loadCurrentUser();

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    window.addEventListener('auth:unauthorized', logout);
    return () => window.removeEventListener('auth:unauthorized', logout);
  }, [logout]);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authService.login(request);
    tokenStorage.set(response.accessToken);
    setUser(response.user);
  }, []);

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await authService.register(request);
    tokenStorage.set(response.accessToken);
    setUser(response.user);
  }, []);

  const value = useMemo(
    () => ({ login, logout, register, status, user }),
    [login, logout, register, status, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
