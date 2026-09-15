"use client";

import { FormEvent, Suspense, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Box, Paper, TextField, Typography, Button, Stack, Alert } from "@mui/material";
import { motion } from "framer-motion";
import { useAuth } from "@/lib/auth";
import { extractErrorMessage } from "@/lib/api";

function RegisterForm() {
  const { register } = useAuth();
  const searchParams = useSearchParams();

  const [displayName, setDisplayName] = useState(searchParams.get("name") ?? "");
  const [email, setEmail] = useState(searchParams.get("email") ?? "");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(displayName, email, password);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", p: 2 }}>
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
        <Paper sx={{ p: 4, width: 420, maxWidth: "90vw" }}>
          <Typography variant="h5" fontWeight={700} sx={{ mb: 0.5 }}>
            Open your desk
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            You&apos;ll start with $500,000 in simulated treasury cash. Nothing here is real money.
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Stack component="form" onSubmit={handleSubmit} gap={2}>
            <TextField
              label="Full name"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              required
              fullWidth
            />
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
              helperText="At least 8 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              fullWidth
            />

            <Button type="submit" variant="contained" size="large" disabled={submitting}>
              {submitting ? "Opening desk..." : "Open desk"}
            </Button>
          </Stack>

          <Typography variant="body2" color="text.secondary" sx={{ mt: 3, textAlign: "center" }}>
            Already have a desk?{" "}
            <Typography component={Link} href="/login" variant="body2" color="primary.light" sx={{ fontWeight: 600 }}>
              Sign in
            </Typography>
          </Typography>
        </Paper>
      </motion.div>
    </Box>
  );
}

export default function RegisterPage() {
  return (
    <Suspense fallback={null}>
      <RegisterForm />
    </Suspense>
  );
}
