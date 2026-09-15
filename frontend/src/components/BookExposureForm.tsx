"use client";

import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Paper,
  Typography,
  Stack,
  TextField,
  MenuItem,
  ToggleButton,
  ToggleButtonGroup,
  Button,
  Alert,
} from "@mui/material";
import { api, extractErrorMessage } from "@/lib/api";
import type { CurrencyPairResponse, ExposureDirection } from "@/lib/types";

export default function BookExposureForm() {
  const queryClient = useQueryClient();
  const { data: pairs } = useQuery({
    queryKey: ["market-pairs"],
    queryFn: async () => (await api.get<CurrencyPairResponse[]>("/api/market/pairs")).data,
  });

  const [pairCode, setPairCode] = useState("EURUSD");
  const [direction, setDirection] = useState<ExposureDirection>("RECEIVABLE");
  const [amount, setAmount] = useState("");
  const [daysUntilDue, setDaysUntilDue] = useState("30");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: async () =>
      api.post("/api/exposures", {
        pairCode,
        direction,
        amount: Number(amount),
        daysUntilDue: Number(daysUntilDue),
        description,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["exposures"] });
      queryClient.invalidateQueries({ queryKey: ["portfolio"] });
      setAmount("");
      setDescription("");
    },
    onError: (err) => setError(extractErrorMessage(err)),
  });

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    if (!amount || Number(amount) <= 0) {
      setError("Enter an amount above zero.");
      return;
    }
    if (!description.trim()) {
      setError("Give it a short description, it's what you'll recognize it by later.");
      return;
    }
    mutation.mutate();
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" fontWeight={700} sx={{ mb: 0.5 }}>
        Book an exposure
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        An invoice or bill in a foreign currency, due on a simulated future day.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Stack component="form" onSubmit={handleSubmit} gap={2}>
        <TextField select label="Currency pair" value={pairCode} onChange={(e) => setPairCode(e.target.value)} fullWidth>
          {pairs?.map((pair) => (
            <MenuItem key={pair.code} value={pair.code}>
              {pair.code} &middot; {pair.currentRate.toFixed(4)}
            </MenuItem>
          ))}
        </TextField>

        <ToggleButtonGroup
          value={direction}
          exclusive
          onChange={(_, value) => value && setDirection(value)}
          fullWidth
        >
          <ToggleButton value="RECEIVABLE">Receivable (money coming in)</ToggleButton>
          <ToggleButton value="PAYABLE">Payable (money going out)</ToggleButton>
        </ToggleButtonGroup>

        <TextField
          label="Amount (foreign currency)"
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          fullWidth
        />

        <TextField
          label="Due in (simulated days)"
          type="number"
          value={daysUntilDue}
          onChange={(e) => setDaysUntilDue(e.target.value)}
          fullWidth
        />

        <TextField
          label="Description"
          placeholder="e.g. Q2 invoice to a Berlin customer"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          fullWidth
        />

        <Button type="submit" variant="contained" size="large" disabled={mutation.isPending}>
          {mutation.isPending ? "Booking..." : "Book exposure"}
        </Button>
      </Stack>
    </Paper>
  );
}
