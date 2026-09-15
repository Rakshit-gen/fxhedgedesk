# FX Hedge Desk

A simulated corporate treasury desk. No real money, no real market feed,
just the actual mechanics of managing foreign-currency exposure the way a
treasury team does: book an exposure, decide how much of it to hedge with a
forward contract, watch the simulated market move, and see what hedging
actually bought you once the contract settles.

Most people never see this side of finance because it lives behind
institutional trading terminals. This rebuilds the core loop, exposure to
hedge to settlement to realized outcome, so the tradeoffs are visible instead
of abstract.

## What it does

- Books FX exposures (an invoice or payment due in a foreign currency, on a
  simulated future day) across five currency pairs
- Simulates a live FX market with a random-walk rate engine per pair,
  pushed to the browser over WebSocket as it ticks
- Hedges exposures with forward contracts, locking today's simulated rate
  for a future simulated settlement day, full or partial notional
- Runs a simulated clock that advances faster than real time, so a 90-day
  hedge settles in minutes instead of months
- Marks open forwards to market against the live rate so unrealized P&L is
  visible before settlement
- On settlement, realizes the hedge P&L and shows the counterfactual: what
  the same exposure would have cost left unhedged, so hedging is judged on
  an actual outcome instead of a gut feeling
- Reports portfolio-level hedge ratio and a simple parametric VaR per
  currency, the same numbers a real treasury desk watches

## Stack

- Backend: Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA,
  Flyway, WebSocket/STOMP
- Frontend: Next.js 15, TypeScript, MUI v5, TanStack Query, framer-motion,
  recharts
- Storage: H2 in-memory by default, Postgres in Docker Compose

## Running it

```bash
# backend
cd backend && ./mvnw spring-boot:run   # or: mvn spring-boot:run

# frontend
cd frontend && npm install && npm run dev
```

Demo login: `demo@fxhedgedesk.dev` / `password123`, seeded with a
$500,000 simulated treasury balance.

Full setup, environment variables, and the API surface are documented in
[`backend/README.md`](backend/README.md).
