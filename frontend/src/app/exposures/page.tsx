"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Typography, Grid, Stack, Skeleton, Alert } from "@mui/material";
import AppShell from "@/components/AppShell";
import RequireAuth from "@/components/RequireAuth";
import ExposureCard from "@/components/ExposureCard";
import BookExposureForm from "@/components/BookExposureForm";
import HedgeDialog from "@/components/HedgeDialog";
import { api, extractErrorMessage } from "@/lib/api";
import type { ExposureResponse } from "@/lib/types";

function ExposuresContent() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["exposures"],
    queryFn: async () => (await api.get<ExposureResponse[]>("/api/exposures")).data,
  });
  const [hedging, setHedging] = useState<ExposureResponse | null>(null);

  return (
    <>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 0.5 }}>
        Exposures
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Everything your desk is exposed to, hedged or not, ordered by how soon it comes due.
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <BookExposureForm />
        </Grid>
        <Grid item xs={12} md={8}>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{extractErrorMessage(error)}</Alert>}
          <Stack gap={2}>
            {isLoading &&
              Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} variant="rounded" height={140} />)}
            {data?.length === 0 && (
              <Typography variant="body2" color="text.secondary">
                No exposures booked yet. Start with the form on the left.
              </Typography>
            )}
            {data?.map((exposure) => (
              <ExposureCard key={exposure.id} exposure={exposure} onHedge={setHedging} />
            ))}
          </Stack>
        </Grid>
      </Grid>

      <HedgeDialog exposure={hedging} open={!!hedging} onClose={() => setHedging(null)} />
    </>
  );
}

export default function ExposuresPage() {
  return (
    <RequireAuth>
      <AppShell>
        <ExposuresContent />
      </AppShell>
    </RequireAuth>
  );
}
