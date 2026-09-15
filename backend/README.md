# FX Hedge Desk backend

Spring Boot 3 / Java 21. H2 in-memory by default, Postgres in Docker Compose.

## Running

```bash
mvn spring-boot:run
```

The app boots on port 8081. Flyway runs the schema and seeds a demo account
(`demo@fxhedgedesk.dev` / `password123`) on first startup.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | H2 in-memory | JDBC URL, point at Postgres for anything persistent |
| `DB_USER` / `DB_PASSWORD` | `sa` / empty | Database credentials |
| `JWT_SECRET` | dev placeholder | HMAC signing key, override in any real deployment |
| `JWT_EXPIRY_MINUTES` | 120 | Token lifetime |
| `SIM_TICK_MS` | 4000 | How often the simulated market ticks, in real milliseconds |
| `SIM_DAYS_PER_TICK` | 1 | How many simulated days pass per tick |

## API surface

- `POST /api/auth/register`, `POST /api/auth/login` — issue a JWT
- `GET /api/me` — the authenticated user
- `GET /api/market/pairs` — the five tradable pairs and their live rate
- `GET /api/market/pairs/{code}/history` — recent rate ticks for charting
- `GET /api/market/clock` — the current simulated day
- `POST /api/exposures`, `GET /api/exposures` — book and list FX exposures
- `POST /api/forwards`, `GET /api/forwards` — hedge an exposure with a forward, list open and settled forwards
- `GET /api/portfolio` — hedge ratio, VaR, mark-to-market, and realized P&L, overall and per currency
- `GET /api/wallet`, `GET /api/wallet/ledger` — simulated cash balance and its history

Live rate ticks also stream over STOMP at `/ws`, topic `/topic/rates/{pairCode}`.

## Tests

```bash
mvn test
```

Fourteen tests: domain invariants (`Exposure`, `Wallet`), the FX conversion
math, a seeded-random check that the rate simulation is deterministic, and a
full end-to-end flow through a real H2-backed Spring context, register, book
an exposure, hedge part of it, advance the simulated market, settle, and
verify the forward's P&L and the wallet balance agree with the math.
