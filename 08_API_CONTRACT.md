# API / Service Contract Outline
Use server-side services for business-critical mutations. REST endpoints are illustrative; final naming and schema should be standardized.

## Auth/Profile
- GET /me
- PATCH /me
- POST /users (admin)
- PATCH /users/:id/roles (privileged)
## Schools
- GET/POST /schools
- GET/PATCH /schools/:id
- GET/POST /schools/:id/contacts
- GET/POST /schools/:id/classes
- GET/POST /schools/:id/visits
## Academic
- GET/POST /courses
- GET/POST /programs
- GET/PATCH /programs/:id
- POST /programs/:id/assignments
- POST /programs/:id/schedules
- GET/POST /enrollments
## Finance
- POST /agreements
- POST /agreements/:id/payment-schedules
- GET /receivables
- POST /payments (idempotent; allocate atomically)
- GET /payments/:id/receipt
- POST /expenses
- POST /expenses/:id/approve
- POST /accounts/transfers
- GET /accounts/:id/ledger
## Inventory
- GET/POST /products
- GET/POST /suppliers
- POST /purchase-orders
- POST /purchase-orders/:id/receive
- POST /distributions
- POST /stock-counts
- GET /stock-ledger
## Operations
- POST /attendance/check-in
- POST /attendance/check-out
- POST /attendance/:id/override
- POST /tasks
- POST /visits
## Reports
- GET /reports/finance
- GET /reports/receivables-aging
- GET /reports/inventory
- GET /reports/programs
- GET /reports/attendance

## API standards
- JSON and consistent error envelope.
- Pagination (cursor or page), filtering and sorting.
- Idempotency key on payment, receiving and distribution operations.
- Authorization on every server-side operation.
- Validation and database transactions.
- API versioning and documented breaking changes.
- Never return secrets or unnecessary personal/location data.
