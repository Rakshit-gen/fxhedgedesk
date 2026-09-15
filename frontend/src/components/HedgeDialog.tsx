"use client";

import { FormEvent, useState } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Typography,
  Alert,
  Stack,
} from "@mui/material";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api, extractErrorMessage } from "@/lib/api";
import type { ExposureResponse } from "@/lib/types";

export default function HedgeDialog({
  exposure,
  open,
  onClose,
}: {
  exposure: ExposureResponse | null;
  open: boolean;
  onClose: () => void;
}) {
  const queryClient = useQueryClient();
  const remaining = exposure ? exposure.amount - exposure.hedgedAmount : 0;
  const [notional, setNotional] = useState("");
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: async (value: number) => {
      if (!exposure) return;
      await api.post("/api/forwards", { exposureId: exposure.id, notional: value });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["exposures"] });
      queryClient.invalidateQueries({ queryKey: ["forwards"] });
      queryClient.invalidateQueries({ queryKey: ["portfolio"] });
      setNotional("");
      onClose();
    },
    onError: (err) => setError(extractErrorMessage(err)),
  });

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    const value = Number(notional);
    if (!value || value <= 0) {
      setError("Enter a notional above zero.");
      return;
    }
    if (value > remaining) {
      setError(`Can't hedge more than the ${remaining.toLocaleString()} still unhedged.`);
      return;
    }
    mutation.mutate(value);
  }

  if (!exposure) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Hedge this exposure</DialogTitle>
      <form onSubmit={handleSubmit}>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {exposure.pairCode}, {exposure.direction.toLowerCase()}. {remaining.toLocaleString()} still
            unhedged out of {exposure.amount.toLocaleString()}.
          </Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Stack gap={2}>
            <TextField
              label="Notional to hedge"
              type="number"
              value={notional}
              onChange={(e) => setNotional(e.target.value)}
              inputProps={{ min: 0, max: remaining, step: "0.01" }}
              autoFocus
              fullWidth
            />
            <Button size="small" onClick={() => setNotional(String(remaining))} sx={{ alignSelf: "flex-start" }}>
              Hedge the full remaining amount
            </Button>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={mutation.isPending}>
            {mutation.isPending ? "Booking..." : "Book the forward"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
