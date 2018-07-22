CREATE TABLE customers(
    id UUID PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    registeredOn TIMESTAMP NOT NULL,
);

CREATE TABLE accounts(
  id UUID PRIMARY KEY,
  customerId UUID NOT NULL,
  balance NUMERIC NOT NULL,
  createdOn TIMESTAMP NOT NULL,

  CONSTRAINT FK_AccountCustomer FOREIGN KEY(customerId) REFERENCES customers(id)
);

CREATE TABLE transfers(
  id UUID PRIMARY KEY,
  source UUID NOT NULL,
  destination UUID NOT NULL,
  amount NUMERIC NOT NULL,
  description NVARCHAR(200),
  date TIMESTAMP NOT NULL,

  CONSTRAINT FK_TRANSFER_SOURCE_ACCOUNT FOREIGN KEY(source) REFERENCES accounts(id),
  CONSTRAINT FK_TRANSFER_DESTINATION_ACCOUNT FOREIGN KEY(source) REFERENCES accounts(id)
);