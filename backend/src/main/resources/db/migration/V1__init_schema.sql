create table app_user (
    id              uuid primary key,
    email           varchar(255) not null unique,
    password_hash   varchar(255) not null,
    display_name    varchar(120) not null,
    created_at      timestamp not null
);

create table wallet (
    id              uuid primary key,
    user_id         uuid not null unique references app_user(id),
    currency        varchar(3) not null default 'USD',
    balance         numeric(18,2) not null,
    version         bigint not null default 0
);

create table ledger_entry (
    id              uuid primary key,
    wallet_id       uuid not null references wallet(id),
    entry_type      varchar(30) not null,
    amount          numeric(18,2) not null,
    balance_after   numeric(18,2) not null,
    description     varchar(255) not null,
    created_at      timestamp not null
);
create index idx_ledger_entry_wallet on ledger_entry(wallet_id);

create table currency_pair (
    code                varchar(6) primary key,
    base_ccy            varchar(3) not null,
    quote_ccy           varchar(3) not null,
    starting_rate       numeric(18,6) not null,
    annual_volatility   numeric(6,4) not null,
    annual_drift        numeric(6,4) not null default 0
);

create table fx_rate_tick (
    id              uuid primary key,
    pair_code       varchar(6) not null references currency_pair(code),
    rate            numeric(18,6) not null,
    sim_day         bigint not null,
    created_at      timestamp not null
);
create index idx_fx_rate_tick_pair on fx_rate_tick(pair_code, sim_day);

create table exposure (
    id                  uuid primary key,
    user_id             uuid not null references app_user(id),
    pair_code           varchar(6) not null references currency_pair(code),
    direction           varchar(12) not null,
    amount              numeric(18,2) not null,
    booked_rate         numeric(18,6) not null,
    due_sim_day         bigint not null,
    description         varchar(255) not null,
    status              varchar(16) not null,
    created_at          timestamp not null,
    settled_at          timestamp,
    settlement_rate     numeric(18,6),
    unhedged_variance   numeric(18,2)
);
create index idx_exposure_user on exposure(user_id);
create index idx_exposure_due_day on exposure(due_sim_day);

create table forward_contract (
    id                      uuid primary key,
    user_id                 uuid not null references app_user(id),
    exposure_id             uuid not null references exposure(id),
    pair_code               varchar(6) not null references currency_pair(code),
    notional                numeric(18,2) not null,
    contracted_rate         numeric(18,6) not null,
    direction               varchar(4) not null,
    trade_sim_day           bigint not null,
    settlement_sim_day      bigint not null,
    status                  varchar(16) not null,
    settlement_rate         numeric(18,6),
    realized_pnl            numeric(18,2),
    created_at              timestamp not null,
    settled_at              timestamp
);
create index idx_forward_user on forward_contract(user_id);
create index idx_forward_exposure on forward_contract(exposure_id);
create index idx_forward_settlement_day on forward_contract(settlement_sim_day);
