"use client";

import { createContext, useCallback, useContext, useEffect, useState, ReactNode } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { api } from "./api";
import type { MeResponse } from "./types";

interface AuthContextValue {
  me: MeResponse | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (displayName: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [me, setMe] = useState<MeResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();
  const queryClient = useQueryClient();

  const loadMe = useCallback(async () => {
    const token = typeof window !== "undefined" ? window.localStorage.getItem("fxhedgedesk_token") : null;
    if (!token) {
      setMe(null);
      setIsLoading(false);
      return;
    }
    try {
      const response = await api.get<MeResponse>("/api/me");
      setMe(response.data);
    } catch {
      window.localStorage.removeItem("fxhedgedesk_token");
      setMe(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- fetch-on-mount, not derived state
    loadMe();
  }, [loadMe]);

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await api.post<{ token: string }>("/api/auth/login", { email, password });
      window.localStorage.setItem("fxhedgedesk_token", response.data.token);
      await loadMe();
      router.push("/desk");
    },
    [loadMe, router]
  );

  const register = useCallback(
    async (displayName: string, email: string, password: string) => {
      const response = await api.post<{ token: string }>("/api/auth/register", {
        displayName,
        email,
        password,
      });
      window.localStorage.setItem("fxhedgedesk_token", response.data.token);
      await loadMe();
      router.push("/desk");
    },
    [loadMe, router]
  );

  const logout = useCallback(() => {
    window.localStorage.removeItem("fxhedgedesk_token");
    setMe(null);
    queryClient.clear();
    router.push("/login");
  }, [queryClient, router]);

  return (
    <AuthContext.Provider value={{ me, isLoading, login, register, logout, refresh: loadMe }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
}
