"use client";

import Link from "next/link";
import { Box, Typography, Button, Stack, Grid, Paper } from "@mui/material";
import { motion } from "framer-motion";
import ShowChartIcon from "@mui/icons-material/ShowChart";
import BalanceIcon from "@mui/icons-material/Balance";
import QueryStatsIcon from "@mui/icons-material/QueryStats";
import BoltIcon from "@mui/icons-material/Bolt";
import HeroBackground from "@/components/HeroBackground";
import QuickStartCard from "@/components/QuickStartCard";

const NAV_LINKS = [
  { label: "The desk", href: "/desk" },
  { label: "How hedging works", href: "/#hedging" },
  { label: "The simulation clock", href: "/#clock" },
];

const PILLARS = [
  {
    icon: <BalanceIcon color="primary" fontSize="large" />,
    title: "A forward contract you can actually explain",
    body: "Lock today's simulated rate for a future settlement day, on all or part of an exposure. No delivery mechanics to model, it cash-settles on the difference, same economics as the real thing.",
  },
  {
    icon: <QueryStatsIcon color="secondary" fontSize="large" />,
    title: "See what hedging actually bought you",
    body: "At settlement, the desk shows you the counterfactual: what the unhedged remainder would have cost against what you budgeted. Hedging gets judged on a real outcome, not a hunch.",
  },
  {
    icon: <BoltIcon color="warning" fontSize="large" />,
    title: "Hedge ratio and VaR, watched live",
    body: "The same numbers a real treasury desk tracks, how much of your book is covered and what a bad day could cost, recalculated the instant the market moves.",
  },
];

const HEDGING_STEPS = [
  { step: "1", title: "Book the exposure", body: "An invoice or bill in a foreign currency, due on a simulated future day. Today's rate gets locked in as the budget to measure against." },
  { step: "2", title: "Hedge some, all, or none", body: "Book a forward for part of it, all of it, or leave it riding the market. Nothing forces a decision either way." },
  { step: "3", title: "Watch it settle", body: "When the due day arrives, the forward cash-settles against the spot rate, and the unhedged remainder shows you what riding the market actually cost." },
];

export default function HomePage() {
  return (
    <Box sx={{ p: { xs: 1.5, sm: 2, md: 3 } }}>
      <Box
        sx={{
          position: "relative",
          borderRadius: { xs: 4, sm: 6 },
          overflow: "hidden",
          minHeight: {
            xs: "calc(100vh - 24px)",
            sm: "calc(100vh - 32px)",
            md: "calc(100vh - 48px)",
          },
          bgcolor: "#06090E",
        }}
      >
        <HeroBackground />
        <Box
          sx={{
            position: "absolute",
            inset: 0,
            background: "linear-gradient(180deg, rgba(6,9,14,0.15) 0%, rgba(6,9,14,0.55) 70%, rgba(6,9,14,0.92) 100%)",
          }}
        />

        <Box
          sx={{
            position: "relative",
            zIndex: 1,
            display: "flex",
            flexDirection: "column",
            minHeight: {
              xs: "calc(100vh - 24px)",
              sm: "calc(100vh - 32px)",
              md: "calc(100vh - 48px)",
            },
            p: { xs: 2.5, sm: 3.5, md: 5 },
            gap: 3,
          }}
        >
          {/* Glass pill navbar */}
          <Stack
            direction="row"
            alignItems="center"
            gap={{ xs: 1.5, sm: 3 }}
            sx={{
              bgcolor: "rgba(255,255,255,0.06)",
              backdropFilter: "blur(20px)",
              border: "1px solid rgba(255,255,255,0.08)",
              borderRadius: 6,
              pl: 2,
              pr: 1,
              py: 1,
              width: { xs: "100%", sm: "fit-content" },
            }}
          >
            <Stack direction="row" alignItems="center" gap={1} component={Link} href="/" sx={{ color: "inherit" }}>
              <ShowChartIcon color="primary" />
              <Typography variant="subtitle1" fontWeight={800}>
                FX Hedge Desk
              </Typography>
            </Stack>

            <Stack direction="row" gap={3} sx={{ display: { xs: "none", sm: "flex" } }}>
              {NAV_LINKS.map((link) => (
                <Typography
                  key={link.href}
                  component={Link}
                  href={link.href}
                  variant="body2"
                  sx={{ color: "rgba(255,255,255,0.75)", "&:hover": { color: "white" } }}
                >
                  {link.label}
                </Typography>
              ))}
            </Stack>

            <Button component={Link} href="/register" variant="contained" size="small" sx={{ ml: "auto", borderRadius: 4 }}>
              Open a desk
            </Button>
          </Stack>

          {/* Headline + quick-start card. The headline is a flex child that grows
              to fill whatever the card doesn't use, so the card always sits flush
              against the hero's right edge with no leftover-space math needed. */}
          <Stack
            direction={{ xs: "column", lg: "row" }}
            alignItems={{ lg: "center" }}
            gap={4}
            sx={{ width: "100%", my: "auto" }}
          >
            <Box
              component={motion.div}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              sx={{ flex: { lg: "1 1 auto" }, minWidth: 0 }}
            >
              <Typography
                sx={{
                  color: "white",
                  fontSize: { xs: "2.1rem", sm: "2.6rem", md: "3.2rem" },
                  fontWeight: 600,
                  lineHeight: 1.15,
                  maxWidth: 680,
                  textShadow: "0 8px 30px rgba(0,0,0,0.4)",
                }}
              >
                Hedge like it&apos;s real,
                <br />
                because every dollar is{" "}
                <Box component="span" sx={{ fontFamily: '"Georgia", serif', fontStyle: "italic", fontWeight: 400 }}>
                  simulated
                </Box>
                .
              </Typography>
              <Typography sx={{ color: "rgba(255,255,255,0.7)", mt: 2, maxWidth: 520 }}>
                Book a foreign-currency exposure, hedge it with a forward contract against a live
                simulated market, and watch the simulated clock carry it to settlement. See exactly
                what hedging bought you, in numbers, not gut feeling.
              </Typography>
            </Box>

            <Box
              component={motion.div}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.15 }}
              sx={{ flexShrink: { lg: 0 }, width: { xs: "100%", lg: "auto" } }}
            >
              <QuickStartCard />
            </Box>
          </Stack>
        </Box>
      </Box>

      <Grid container spacing={3} sx={{ py: 8, maxWidth: 1100, mx: "auto" }}>
        {PILLARS.map((pillar, index) => (
          <Grid item xs={12} md={4} key={pillar.title}>
            <motion.div
              initial={{ opacity: 0, y: 24 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              style={{ height: "100%" }}
            >
              <Paper sx={{ p: 3, height: "100%" }}>
                <Box sx={{ mb: 2 }}>{pillar.icon}</Box>
                <Typography variant="h6" sx={{ mb: 1 }}>
                  {pillar.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {pillar.body}
                </Typography>
              </Paper>
            </motion.div>
          </Grid>
        ))}
      </Grid>

      <Box id="hedging" sx={{ scrollMarginTop: 24, maxWidth: 1100, mx: "auto", py: 8, px: { xs: 1, sm: 0 } }}>
        <Typography variant="h4" fontWeight={700} sx={{ mb: 1 }}>
          How hedging works
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4, maxWidth: 640 }}>
          Three steps, the same three a real treasury desk goes through, just compressed into
          minutes instead of a fiscal quarter.
        </Typography>

        <Grid container spacing={2}>
          {HEDGING_STEPS.map((item) => (
            <Grid item xs={12} md={4} key={item.step}>
              <Paper sx={{ p: 3, height: "100%" }}>
                <Typography variant="h4" fontWeight={800} color="primary.light" sx={{ mb: 1 }}>
                  {item.step}
                </Typography>
                <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1 }}>
                  {item.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {item.body}
                </Typography>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Box>

      <Box id="clock" sx={{ scrollMarginTop: 24, maxWidth: 1100, mx: "auto", py: 8, px: { xs: 1, sm: 0 } }}>
        <Typography variant="h4" fontWeight={700} sx={{ mb: 1 }}>
          The simulation clock
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4, maxWidth: 640 }}>
          A real hedge sits for weeks or months before it settles. This one doesn&apos;t make you
          wait, a background clock advances the simulated market and carries every open exposure
          toward its due day automatically.
        </Typography>

        <Grid container spacing={2}>
          <Grid item xs={12} sm={4}>
            <Paper sx={{ p: 3, height: "100%" }}>
              <Typography variant="h5" fontWeight={800} color="secondary.main">
                A random walk
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                Every pair moves on its own geometric Brownian motion, the same model used to
                sanity-check option pricing, just run forward instead of solved backward.
              </Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Paper sx={{ p: 3, height: "100%" }}>
              <Typography variant="h5" fontWeight={800} color="secondary.main">
                Ticks, not days
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                Each tick advances the simulated calendar and every rate with it, so a 90-day
                hedge plays out in minutes.
              </Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Paper sx={{ p: 3, height: "100%" }}>
              <Typography variant="h5" fontWeight={800} color="secondary.main">
                Settles itself
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                When an exposure&apos;s due day arrives, the clock settles it and any forward
                hedging it, no manual step required.
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      </Box>
    </Box>
  );
}
