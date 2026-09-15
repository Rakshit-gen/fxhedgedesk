"use client";

import { ReactNode, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Box, CircularProgress } from "@mui/material";
import { useAuth } from "@/lib/auth";

export default function RequireAuth({ children }: { children: ReactNode }) {
  const { me, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !me) {
      router.replace("/login");
    }
  }, [isLoading, me, router]);

  if (isLoading || !me) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 10 }}>
        <CircularProgress />
      </Box>
    );
  }

  return <>{children}</>;
}
