"use client";

import { Box, Paper, Typography, Stack, Chip } from "@mui/material";
import { pnlColor } from "@/theme/theme";
import type { ForwardResponse } from "@/lib/types";

export default function ForwardCard({ forward }: { forward: ForwardResponse }) {
  const pnl = forward.status === "OPEN" ? forward.unrealizedPnl : (forward.realizedPnl ?? 0);
  const label = forward.status === "OPEN" ? "unrealized" : "realized";

  return (
    <Paper sx={{ p: 3 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 1.5 }}>
        <Box>
          <Typography variant="subtitle1" fontWeight={700}>
            {forward.pairCode} &middot; {forward.direction}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {forward.notional.toLocaleString()} notional at {forward.contractedRate.toFixed(4)}
          </Typography>
        </Box>
        <Chip
          label={forward.status}
          color={forward.status === "OPEN" ? "warning" : "success"}
          size="small"
        />
      </Stack>

      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="caption" color="text.secondary">
          {forward.status === "OPEN"
            ? `Settles on simulated day ${forward.settlementSimDay}`
            : `Settled at ${forward.settlementRate?.toFixed(4)}`}
        </Typography>
        <Typography variant="body1" fontWeight={700} sx={{ color: pnlColor(pnl) }}>
          {pnl >= 0 ? "+" : ""}
          {pnl.toLocaleString(undefined, { style: "currency", currency: "USD" })}{" "}
          <Typography component="span" variant="caption" color="text.secondary">
            {label}
          </Typography>
        </Typography>
      </Stack>
    </Paper>
  );
}
