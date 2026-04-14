CREATE TABLE CASH_CARD (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    card_number VARCHAR(20) NOT NULL UNIQUE,
    balance DECIMAL(10, 2) NOT NULL,
    owner VARCHAR(100) NOT NULL
);


INSERT INTO CASH_CARD (card_number, balance, owner) VALUES ('1111111111', 100.00, 'test');
INSERT INTO CASH_CARD (card_number, balance, owner) VALUES ('2222222222', 200.00, 'test');
INSERT INTO CASH_CARD (card_number, balance, owner) VALUES ('3333333333', 300.00, 'test');
INSERT INTO CASH_CARD (card_number, balance, owner) VALUES ('4444444444', 400.00, 'test');
