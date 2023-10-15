CREATE TABLE IF NOT EXISTS public.stocks_history
(
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticker            VARCHAR(10)    NOT NULL,
    stock             NUMERIC(16, 8) NOT NULL,
    change_percentage NUMERIC(6, 4)  NOT NULL,
    actual_on         TIMESTAMP      NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS stocks_history_ticker_actual_on_idx ON stocks_history (ticker, actual_on);
