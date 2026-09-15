"use client";

import { useQuery } from "@tanstack/react-query";
import {
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import AppShell from "@/components/AppShell";
import RequireAuth from "@/components/RequireAuth";
import { api } from "@/lib/api";
import { pnlColor } from "@/theme/theme";
import type { LedgerEntryResponse, WalletResponse } from "@/lib/types";

function money(value: number) {
  return value.toLocaleString(undefined, { style: "currency", currency: "USD" });
}

function WalletContent() {
  const { data: wallet } = useQuery({
    queryKey: ["wallet"],
    queryFn: async () => (await api.get<WalletResponse>("/api/wallet")).data,
    refetchInterval: 5000,
  });
  const { data: ledger } = useQuery({
    queryKey: ["ledger"],
    queryFn: async () => (await api.get<LedgerEntryResponse[]>("/api/wallet/ledger")).data,
    refetchInterval: 5000,
  });

  return (
    <>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 0.5 }}>
        Wallet
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Simulated cash, moved only by forward settlements, in dollars, never in the foreign
        currency itself.
      </Typography>

      <Paper sx={{ p: 4, mb: 4 }}>
        <Typography variant="caption" color="text.secondary">Balance</Typography>
        <Typography variant="h3" fontWeight={800}>{wallet ? money(wallet.balance) : "..."}</Typography>
      </Paper>

      <Paper sx={{ p: 3 }}>
        <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
          Ledger
        </Typography>
        {ledger?.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            Nothing&apos;s posted yet. Balances only move when a forward settles.
          </Typography>
        ) : (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>When</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell align="right">Amount</TableCell>
                  <TableCell align="right">Balance after</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {ledger?.map((entry) => (
                  <TableRow key={entry.id}>
                    <TableCell>{new Date(entry.createdAt).toLocaleString()}</TableCell>
                    <TableCell>{entry.description}</TableCell>
                    <TableCell align="right" sx={{ color: pnlColor(entry.amount) }}>
                      {entry.amount >= 0 ? "+" : ""}
                      {money(entry.amount)}
                    </TableCell>
                    <TableCell align="right">{money(entry.balanceAfter)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>
    </>
  );
}

export default function WalletPage() {
  return (
    <RequireAuth>
      <AppShell>
        <WalletContent />
      </AppShell>
    </RequireAuth>
  );
}
