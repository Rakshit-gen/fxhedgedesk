"use client";

import { useQuery } from "@tanstack/react-query";
import { Typography, Stack, Skeleton, Alert } from "@mui/material";
import AppShell from "@/components/AppShell";
import RequireAuth from "@/components/RequireAuth";
import ForwardCard from "@/components/ForwardCard";
import { api, extractErrorMessage } from "@/lib/api";
import type { ForwardResponse } from "@/lib/types";

function ForwardsContent() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["forwards"],
    queryFn: async () => (await api.get<ForwardResponse[]>("/api/forwards")).data,
    refetchInterval: 10_000,
  });

  return (
    <>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 0.5 }}>
        Forwards
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Every hedge you&apos;ve booked, open ones marked to today&apos;s rate, settled ones showing
        what they actually paid out.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{extractErrorMessage(error)}</Alert>}

      <Stack gap={2}>
        {isLoading && Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} variant="rounded" height={110} />)}
        {data?.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            No forwards booked yet. Hedge an exposure to open one.
          </Typography>
        )}
        {data?.map((forward) => (
          <ForwardCard key={forward.id} forward={forward} />
        ))}
      </Stack>
    </>
  );
}

export default function ForwardsPage() {
  return (
    <RequireAuth>
      <AppShell>
        <ForwardsContent />
      </AppShell>
    </RequireAuth>
  );
}
