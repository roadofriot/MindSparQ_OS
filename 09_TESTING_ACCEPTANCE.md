# Testing Plan & Acceptance Criteria
## Test layers
- Unit: business rules, pricing, date recurrence, permission evaluation, ledger calculations.
- Integration: database transactions, RLS, auth, storage and notification job.
- UI: form validation, navigation, error states and accessibility.
- End-to-end: school onboarding, program activation, collection, purchasing, distribution and attendance.
- Security: role matrix, RLS bypass attempts, file access, rate limiting and privilege escalation.
- Performance: dashboard/report queries, pagination, concurrent requests.
- Recovery: backup restore and migration rollback strategy.
- UAT: real workflows with anonymized test data.

## Acceptance examples
### School
Given authorized user, when a school is created with required fields, then it is searchable, its history is auditable and unauthorized roles cannot access restricted fields.

### Program
Program cannot be activated without valid school/course dates and required assignments. Conflicting schedule is flagged.

### Agreement
Pricing model and terms are explicit. Schedule totals validate against agreement. Updates preserve version history.

### Payment
Payment is recorded exactly once under retry. Allocations cannot exceed permitted open balance. Receipt is generated. Account ledger updates atomically. Reversal preserves history.

### Transfer
Transfer atomically debits and credits two accounts, does not appear as income or expense, and has an audit trail.

### Inventory
Goods receipt increases stock only once. Distribution reduces stock and links to recipient/school/program. Stock cannot be silently negative. Return/damage/adjustment is ledgered.

### Attendance
Only assigned/authorized user can submit. Location and accuracy checks follow policy. Poor GPS/offline/denied permission states provide manual exception path. No continuous tracking by default.

### Notifications
Notifications are deduplicated, actionable, configurable and logged; retries do not create duplicate business actions.

### RBAC
Teacher cannot access finance, other teachers' private data or unassigned schools. Admin access is permission-limited. Super Admin actions are logged.

## UAT sign-off
Record tester, scenario, expected/actual result, evidence, defect severity, resolution and sign-off.
