"use client";

import { useEffect } from "react";
import { Box, Typography, Button, Stack } from "@mui/material";

export default function GlobalError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", p: 3 }}>
      <Stack alignItems="center" textAlign="center" gap={2}>
        <Typography variant="h5" fontWeight={700}>
          Something broke on our end
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 380 }}>
          This is a simulation, not your money, so nothing was lost. Try again, and if it keeps
          happening, the backend may not be running.
        </Typography>
        <Button variant="contained" onClick={reset} sx={{ mt: 1 }}>
          Try again
        </Button>
      </Stack>
    </Box>
  );
}
