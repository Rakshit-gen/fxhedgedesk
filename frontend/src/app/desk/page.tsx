"use client";

import { useCallback, useState } from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { Typography, Grid, Box, Stack, Paper, Button, Chip } from "@mui/material";
import AppShell from "@/components/AppShell";
import RequireAuth from "@/components/RequireAuth";
import RateCard from "@/components/RateCard";
import { api } from "@/lib/api";
import { useRateSocket } from "@/lib/useRateSocket";
import { pnlColor } from "@/theme/theme";
import type { ClockResponse, CurrencyPairResponse, PortfolioSummary, RateUpdate } from "@/lib/types";

function money(value: number) {
  return value.toLocaleString(undefined, { style: "currency", currency: "USD", maximumFractionDigits: 0 });
}

function DeskContent() {
  const { data: pairs } = useQuery({
    queryKey: ["market-pairs"],
    queryFn: async () => (await api.get<CurrencyPairResponse[]>("/api/market/pairs")).data,
  });
  const { data: clock } = useQuery({
    queryKey: ["clock"],
    queryFn: async () => (await api.get<ClockResponse>("/api/market/clock")).data,
    refetchInterval: 5000,
  });
  const { data: portfolio } = useQuery({
    queryKey: ["portfolio"],
    queryFn: async () => (await api.get<PortfolioSummary>("/api/portfolio")).data,
    refetchInterval: 5000,
  });

  const [liveRates, setLiveRates] = useState<Record<string, number>>({});
  const pairCodes = pairs?.map((p) => p.code) ?? [];

  const onUpdate = useCallback((update: RateUpdate) => {
    setLiveRates((prev) => ({ ...prev, [update.pairCode]: update.rate }));
  }, []);
  useRateSocket(pairCodes, onUpdate);

  const openMtm = portfolio?.openForwardsMtmUsd ?? 0;

  return (
    <>
      <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" alignItems={{ sm: "center" }} gap={2} sx={{ mb: 4 }}>
        <Box>
          <Typography variant="h4" fontWeight={700} sx={{ mb: 0.5 }}>
            The desk
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Five simulated pairs, ticking live, and a running total of what your hedges are worth.
          </Typography>
        </Box>
        {clock && <Chip label={`Simulated day ${clock.currentSimDay}`} color="primary" variant="outlined" />}
      </Stack>

      <Grid container spacing={2} sx={{ mb: 4 }}>
        {pairs?.map((pair) => (
          <Grid item xs={12} sm={6} md={4} lg={2.4} key={pair.code}>
            <RateCard pairCode={pair.code} liveRate={liveRates[pair.code] ?? pair.currentRate} />
          </Grid>
        ))}
      </Grid>

      {portfolio && (
        <Grid container spacing={2} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="caption" color="text.secondary">Total exposure</Typography>
              <Typography variant="h5" fontWeight={700}>{money(portfolio.totalExposureUsd)}</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="caption" color="text.secondary">Hedge ratio</Typography>
              <Typography variant="h5" fontWeight={700}>{(portfolio.hedgeRatio * 100).toFixed(1)}%</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="caption" color="text.secondary">1-day VaR (95%)</Typography>
              <Typography variant="h5" fontWeight={700} color="warning.main">{money(portfolio.portfolioVar95Usd)}</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="caption" color="text.secondary">Open forwards, mark-to-market</Typography>
              <Typography variant="h5" fontWeight={700} sx={{ color: pnlColor(openMtm) }}>
                {openMtm >= 0 ? "+" : ""}{money(openMtm)}
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      )}

      <Stack direction={{ xs: "column", sm: "row" }} gap={2}>
        <Button component={Link} href="/exposures" variant="contained" size="large">
          Book an exposure
        </Button>
        <Button component={Link} href="/portfolio" variant="outlined" size="large">
          Full risk breakdown
        </Button>
      </Stack>
    </>
  );
}

export default function DeskPage() {
  return (
    <RequireAuth>
      <AppShell>
        <DeskContent />
      </AppShell>
    </RequireAuth>
  );
}
