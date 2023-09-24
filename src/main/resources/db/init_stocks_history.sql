CREATE TABLE IF NOT EXISTS public.stocks_history
(
    ticker            VARCHAR(10) NOT NULL UNIQUE,
    stock             BIGINT      NOT NULL,
    change_percentage INT         NOT NULL,
    actual_on         TIMESTAMP   NOT NULL
);
ALTER TABLE stocks_history ADD CONSTRAINT stocks_history_pk PRIMARY KEY (ticker, actual_on);