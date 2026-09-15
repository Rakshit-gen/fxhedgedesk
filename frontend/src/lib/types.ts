export type ExposureDirection = "RECEIVABLE" | "PAYABLE";
export type ForwardDirectionValue = "BUY" | "SELL";

export interface MeResponse {
  id: string;
  email: string;
  displayName: string;
}

export interface CurrencyPairResponse {
  code: string;
  baseCcy: string;
  quoteCcy: string;
  currentRate: number;
}

export interface RateTick {
  id: string;
  pairCode: string;
  rate: number;
  simDay: number;
  createdAt: string;
}

/** The shape of a live WebSocket push, lighter than the persisted RateTick. */
export interface RateUpdate {
  pairCode: string;
  rate: number;
  simDay: number;
}

export interface ClockResponse {
  currentSimDay: number;
}

export interface ExposureResponse {
  id: string;
  pairCode: string;
  direction: ExposureDirection;
  amount: number;
  hedgedAmount: number;
  bookedRate: number;
  dueSimDay: number;
  description: string;
  status: "OPEN" | "PARTIALLY_HEDGED" | "HEDGED" | "SETTLED";
  settlementRate: number | null;
  unhedgedVariance: number | null;
}

export interface ForwardResponse {
  id: string;
  exposureId: string;
  pairCode: string;
  notional: number;
  contractedRate: number;
  direction: ForwardDirectionValue;
  tradeSimDay: number;
  settlementSimDay: number;
  status: "OPEN" | "SETTLED" | "CANCELLED";
  settlementRate: number | null;
  realizedPnl: number | null;
  unrealizedPnl: number;
}

export interface CurrencyBreakdown {
  pairCode: string;
  totalUsd: number;
  hedgedUsd: number;
  unhedgedUsd: number;
  hedgeRatio: number;
  var95Usd: number;
}

export interface PortfolioSummary {
  totalExposureUsd: number;
  totalHedgedUsd: number;
  hedgeRatio: number;
  portfolioVar95Usd: number;
  openForwardsMtmUsd: number;
  realizedPnlUsd: number;
  byCurrency: CurrencyBreakdown[];
}

export interface WalletResponse {
  balance: number;
  currency: string;
}

export interface LedgerEntryResponse {
  id: string;
  entryType: string;
  amount: number;
  balanceAfter: number;
  description: string;
  createdAt: string;
}
