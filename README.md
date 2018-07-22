## RESTful API for inter account money transfers

### Running the application

`sbt run`

Will start the application and will bind to `localhost:8080`.

### Libraries used

 `Cats` - Library providing abstractions for functional programming in Scala
 `HTTP4S` - Minimal, idiomatic Scala interface for HTTP services
 `Doobie` - Pure functional JDBC layer for Scala
 `Circe` - Json library for Scala
 `H2` - In memory database


### Endpoints

 `GET api/customers` - return list of customers
 `POST api/customers` - create new customer in the system
 `GET api/customers/$id` - get customer details

 `GET api/customers/$id/accounts` - get customer accounts
 `POST api/customers/$id/accounts` - create a new account for the customer
 `GET api/customers/$id/accounts/$accountId` - get account details

 `GET api/accounts` - list all accounts in the system

 `GET api/transfers` - list all previous transfers
 `GET api/transfers?source=$id` - list all previous transfers originating from account `$id`
 `GET api/transfers?destination=$id` - list all previous transfers to destination account `$id`
 `GET api/transfers?source=$sid&destination$did` - list all transfers between two accounts
 `POST api/transfers` - create transfer between two accounts

### Things to improve

1. Make the `POST api/customers/$id/accounts` take parameters (e.g. currency, account type, etc.)
2. Add more query parameters to the `api/accounts` endpoint (
3. Add currency support
4. Add currency conversion support (e.g. support transfers between accounts with different currencies)
5. Support deposit and withdrawals
