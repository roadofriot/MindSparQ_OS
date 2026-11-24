# Architecture Blueprint
## Pattern
Modular monolith. Modules share a PostgreSQL database but maintain logical boundaries, API/service contracts and migration ownership. Avoid premature microservices.

## Proposed components
- Flutter client(s): responsive web, Android/iOS, desktop where validated.
- Supabase Auth: identity and sessions.
- PostgreSQL: relational data, constraints, indexes, views/functions.
- RLS: data access enforcement; never rely solely on client UI checks.
- Storage: private buckets with scoped access and signed links.
- Edge Functions/server-side service: sensitive workflows, notification jobs, document generation, privileged operations.
- Notification provider: push; optional email/SMS providers.
- Maps/geofence: location provider selected after cost/privacy review.
- GitHub: source, CI, review, release tags.

## Module boundaries
Identity; Schools; Academic (courses/programs/enrollment); HR/Field; Finance; Inventory/Procurement; Documents; Notifications; Analytics; System/Audit.

## Principles
1. Server-side validation for financial and stock actions.
2. Append-only ledgers for payments and stock movements.
3. Use transactions for payment recording, stock receiving, transfer and reversal.
4. Idempotency keys for sensitive submissions.
5. Soft archive only where appropriate; audit records retained.
6. API pagination, filtering, sorting and search.
7. RLS policies tested per role and record scope.
8. No secrets in app bundle or repository.
9. Store timestamps in UTC; display local Nepal time.
10. Use a reliable AD/BS date library and avoid persisting localized date strings as primary dates.

## Deployment environments
Development → Staging → Production. Separate credentials, projects, storage and data. No production personal data in staging.
