CREATE TABLE customers(
    id UUID PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
);

CREATE TABLE accounts(
    id UUID PRIMARY KEY,
    customerId UUID NOT NULL,

    CONSTRAINT FK_AccountCustomer FOREIGN KEY(customerId) REFERENCES customers(id)
);