"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Box, Paper, Typography, Stack } from "@mui/material";
import { AreaChart, Area, ResponsiveContainer, YAxis } from "recharts";
import { api } from "@/lib/api";
import type { RateTick } from "@/lib/types";

/**
 * One currency pair's live tile: the rate itself, a small sparkline of
 * recent history, and a brief green/red flash on the number whenever a new
 * tick pushes it up or down, so the "live" part of a live rate is visible,
 * not just implied.
 */
export default function RateCard({ pairCode, liveRate }: { pairCode: string; liveRate: number | undefined }) {
  const { data: history } = useQuery({
    queryKey: ["rate-history", pairCode],
    queryFn: async () => (await api.get<RateTick[]>(`/api/market/pairs/${pairCode}/history`)).data,
    staleTime: 60_000,
  });

  const [flash, setFlash] = useState<"up" | "down" | null>(null);
  const [liveAppends, setLiveAppends] = useState<number[]>([]);
  const previousRate = useRef<number | undefined>(undefined);

  useEffect(() => {
    if (liveRate === undefined) return;
    if (previousRate.current !== undefined && liveRate !== previousRate.current) {
      setFlash(liveRate > previousRate.current ? "up" : "down");
      setLiveAppends((prev) => [...prev.slice(-119), liveRate]);
      const timeout = setTimeout(() => setFlash(null), 600);
      previousRate.current = liveRate;
      return () => clearTimeout(timeout);
    }
    previousRate.current = liveRate;
  }, [liveRate]);

  const sparkline = useMemo(() => {
    const base = (history ?? []).map((tick) => ({ rate: tick.rate }));
    return [...base, ...liveAppends.map((rate) => ({ rate }))];
  }, [history, liveAppends]);

  const displayRate = liveRate ?? sparkline[sparkline.length - 1]?.rate;
  const flashColor = flash === "up" ? "#3ED598" : flash === "down" ? "#F2545B" : undefined;

  return (
    <Paper sx={{ p: 2.5, position: "relative", overflow: "hidden" }}>
      <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
        <Box>
          <Typography variant="caption" color="text.secondary" sx={{ letterSpacing: 1 }}>
            {pairCode.slice(0, 3)}/{pairCode.slice(3)}
          </Typography>
          <Typography
            variant="h5"
            fontWeight={700}
            sx={{ color: flashColor ?? "inherit", transition: "color 0.4s ease" }}
          >
            {displayRate !== undefined ? displayRate.toFixed(4) : "..."}
          </Typography>
        </Box>
      </Stack>

      {sparkline.length > 1 && (
        <Box sx={{ height: 40, mt: 1, mx: -1 }}>
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={sparkline}>
              <YAxis hide domain={["dataMin", "dataMax"]} />
              <Area
                type="monotone"
                dataKey="rate"
                stroke="#3EC6E0"
                strokeWidth={1.5}
                fill="#3EC6E0"
                fillOpacity={0.12}
                isAnimationActive={false}
              />
            </AreaChart>
          </ResponsiveContainer>
        </Box>
      )}
    </Paper>
  );
}
