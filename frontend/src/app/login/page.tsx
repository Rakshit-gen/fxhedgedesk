"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { Box, Paper, TextField, Typography, Button, Stack, Alert } from "@mui/material";
import { motion } from "framer-motion";
import { useAuth } from "@/lib/auth";
import { extractErrorMessage } from "@/lib/api";

export default function LoginPage() {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(email, password);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", p: 2 }}>
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
        <Paper sx={{ p: 4, width: 400, maxWidth: "90vw" }}>
          <Typography variant="h5" fontWeight={700} sx={{ mb: 0.5 }}>
            Welcome back to the desk
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Sign in to pick up your simulated positions where you left them.
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Stack component="form" onSubmit={handleSubmit} gap={2}>
            <TextField
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              fullWidth
            />
            <TextField
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              fullWidth
            />
            <Button type="submit" variant="contained" size="large" disabled={submitting}>
              {submitting ? "Signing in..." : "Sign in"}
            </Button>
          </Stack>

          <Typography variant="body2" color="text.secondary" sx={{ mt: 3, textAlign: "center" }}>
            New here?{" "}
            <Typography component={Link} href="/register" variant="body2" color="primary.light" sx={{ fontWeight: 600 }}>
              Open a desk
            </Typography>
          </Typography>
        </Paper>
      </motion.div>
    </Box>
  );
}
