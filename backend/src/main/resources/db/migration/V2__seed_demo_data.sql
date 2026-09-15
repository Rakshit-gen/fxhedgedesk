-- Demo login: demo@fxhedgedesk.dev / password123
insert into app_user (id, email, password_hash, display_name, created_at) values
    ('11111111-1111-1111-1111-111111111111', 'demo@fxhedgedesk.dev',
     '$2a$10$W6PJJwZamddhrfiA4yhIIuRlPBR2fKYLJuzd/FecxhlMeK9dPTOGe',
     'Demo Treasurer', current_timestamp);

insert into wallet (id, user_id, currency, balance, version) values
    ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111',
     'USD', 500000.00, 0);

insert into currency_pair (code, base_ccy, quote_ccy, starting_rate, annual_volatility, annual_drift) values
    ('EURUSD', 'EUR', 'USD', 1.085000, 0.0800, 0.0000),
    ('GBPUSD', 'GBP', 'USD', 1.265000, 0.0900, 0.0000),
    ('USDJPY', 'USD', 'JPY', 149.500000, 0.1000, 0.0000),
    ('USDINR', 'USD', 'INR', 83.200000, 0.0500, 0.0100),
    ('AUDUSD', 'AUD', 'USD', 0.655000, 0.1100, 0.0000);
