"use client";

import { Box, Paper, Typography, Stack, Chip, LinearProgress, Button } from "@mui/material";
import { pnlColor } from "@/theme/theme";
import type { ExposureResponse } from "@/lib/types";

const STATUS_COLOR: Record<ExposureResponse["status"], "default" | "warning" | "success" | "primary"> = {
  OPEN: "default",
  PARTIALLY_HEDGED: "warning",
  HEDGED: "success",
  SETTLED: "primary",
};

export default function ExposureCard({
  exposure,
  onHedge,
}: {
  exposure: ExposureResponse;
  onHedge: (exposure: ExposureResponse) => void;
}) {
  const progress = exposure.amount > 0 ? (exposure.hedgedAmount / exposure.amount) * 100 : 0;
  const canHedge = exposure.status !== "SETTLED" && exposure.hedgedAmount < exposure.amount;

  return (
    <Paper sx={{ p: 3 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 1.5 }}>
        <Box>
          <Typography variant="subtitle1" fontWeight={700}>
            {exposure.pairCode} &middot; {exposure.direction === "RECEIVABLE" ? "Receivable" : "Payable"}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {exposure.description}
          </Typography>
        </Box>
        <Chip label={exposure.status.replace("_", " ")} color={STATUS_COLOR[exposure.status]} size="small" />
      </Stack>

      <Stack direction="row" justifyContent="space-between" sx={{ mb: 0.5 }}>
        <Typography variant="caption" color="text.secondary">
          {exposure.hedgedAmount.toLocaleString()} / {exposure.amount.toLocaleString()} hedged
        </Typography>
        <Typography variant="caption" color="text.secondary">
          Due day {exposure.dueSimDay}
        </Typography>
      </Stack>
      <LinearProgress variant="determinate" value={Math.min(100, progress)} sx={{ mb: 2 }} />

      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="caption" color="text.secondary">
          Booked at {exposure.bookedRate.toFixed(4)}
          {exposure.status === "SETTLED" && exposure.settlementRate !== null && (
            <>, settled at {exposure.settlementRate.toFixed(4)}</>
          )}
        </Typography>

        {exposure.status === "SETTLED" && exposure.unhedgedVariance !== null ? (
          <Typography variant="body2" fontWeight={700} sx={{ color: pnlColor(exposure.unhedgedVariance) }}>
            {exposure.unhedgedVariance >= 0 ? "+" : ""}
            {exposure.unhedgedVariance.toLocaleString(undefined, { style: "currency", currency: "USD" })} unhedged
          </Typography>
        ) : (
          canHedge && (
            <Button size="small" variant="outlined" onClick={() => onHedge(exposure)}>
              Hedge
            </Button>
          )
        )}
      </Stack>
    </Paper>
  );
}
