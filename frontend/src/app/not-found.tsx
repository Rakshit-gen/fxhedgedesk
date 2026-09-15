"use client";

import Link from "next/link";
import { Box, Typography, Button, Stack } from "@mui/material";

export default function NotFound() {
  return (
    <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", p: 3 }}>
      <Stack alignItems="center" textAlign="center" gap={2}>
        <Typography variant="h2" fontWeight={800} color="primary.light">
          404
        </Typography>
        <Typography variant="h6">This position doesn&apos;t exist</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 380 }}>
          Whatever you were looking for was never booked, or the link is off. Head back to the
          desk to see what&apos;s actually live.
        </Typography>
        <Button component={Link} href="/desk" variant="contained" sx={{ mt: 1 }}>
          Back to the desk
        </Button>
      </Stack>
    </Box>
  );
}
