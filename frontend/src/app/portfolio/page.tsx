"use client";

import { useQuery } from "@tanstack/react-query";
import {
  Typography,
  Grid,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  LinearProgress,
  Box,
} from "@mui/material";
import AppShell from "@/components/AppShell";
import RequireAuth from "@/components/RequireAuth";
import { api } from "@/lib/api";
import { pnlColor } from "@/theme/theme";
import type { PortfolioSummary } from "@/lib/types";

function money(value: number) {
  return value.toLocaleString(undefined, { style: "currency", currency: "USD", maximumFractionDigits: 0 });
}

function PortfolioContent() {
  const { data } = useQuery({
    queryKey: ["portfolio"],
    queryFn: async () => (await api.get<PortfolioSummary>("/api/portfolio")).data,
    refetchInterval: 5000,
  });

  return (
    <>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 0.5 }}>
        Portfolio risk
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Hedge ratio and value-at-risk, per currency and overall. The VaR here is a standard
        parametric estimate, not a full historical simulation.
      </Typography>

      {data && (
        <>
          <Grid container spacing={2} sx={{ mb: 4 }}>
            <Grid item xs={12} sm={6} md={3}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="caption" color="text.secondary">Total exposure</Typography>
                <Typography variant="h5" fontWeight={700}>{money(data.totalExposureUsd)}</Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="caption" color="text.secondary">Hedged</Typography>
                <Typography variant="h5" fontWeight={700}>{money(data.totalHedgedUsd)}</Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="caption" color="text.secondary">Realized P&amp;L, all time</Typography>
                <Typography variant="h5" fontWeight={700} sx={{ color: pnlColor(data.realizedPnlUsd) }}>
                  {data.realizedPnlUsd >= 0 ? "+" : ""}{money(data.realizedPnlUsd)}
                </Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="caption" color="text.secondary">1-day VaR (95%)</Typography>
                <Typography variant="h5" fontWeight={700} color="warning.main">{money(data.portfolioVar95Usd)}</Typography>
              </Paper>
            </Grid>
          </Grid>

          <Paper sx={{ p: 3 }}>
            <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
              By currency pair
            </Typography>
            {data.byCurrency.length === 0 ? (
              <Typography variant="body2" color="text.secondary">
                No open exposure right now.
              </Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Pair</TableCell>
                      <TableCell align="right">Total</TableCell>
                      <TableCell align="right">Hedged</TableCell>
                      <TableCell>Hedge ratio</TableCell>
                      <TableCell align="right">1-day VaR</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {data.byCurrency.map((row) => (
                      <TableRow key={row.pairCode}>
                        <TableCell>{row.pairCode}</TableCell>
                        <TableCell align="right">{money(row.totalUsd)}</TableCell>
                        <TableCell align="right">{money(row.hedgedUsd)}</TableCell>
                        <TableCell sx={{ width: 160 }}>
                          <Stack direction="row" alignItems="center" gap={1}>
                            <Box sx={{ flex: 1 }}>
                              <LinearProgress variant="determinate" value={Math.min(100, row.hedgeRatio * 100)} />
                            </Box>
                            <Typography variant="caption">{(row.hedgeRatio * 100).toFixed(0)}%</Typography>
                          </Stack>
                        </TableCell>
                        <TableCell align="right">{money(row.var95Usd)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>
        </>
      )}
    </>
  );
}

export default function PortfolioPage() {
  return (
    <RequireAuth>
      <AppShell>
        <PortfolioContent />
      </AppShell>
    </RequireAuth>
  );
}
