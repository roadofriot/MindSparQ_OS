# Scope, Modules and Roadmap
## Module map
| Module | MVP | Later |
|---|---|---|
| Authentication/RBAC | Yes | MFA, delegated custom role UI |
| Dashboard | Core KPI cards | Advanced BI |
| Schools/contacts/classes | Yes | CRM pipeline automation |
| Courses/catalogue | Yes | Curriculum authoring |
| Programs/schedules/assignments | Yes | Delivery analytics |
| Participant enrollment | Basic optional records | Guardian portal |
| Agreements/receivables/collections | Yes | E-invoicing/payment gateways |
| Income/expenses/cash/bank | Operational ledger | Double-entry accounting |
| Inventory/suppliers/purchases | Yes | Barcode scanning |
| Distribution/returns | Yes | School inventory audit app |
| Teacher profiles/leave/tasks | Yes | Payroll engine |
| Attendance/geofence | Manual + verified GPS MVP | Background automation only after privacy review |
| Calendar/notifications | Yes | SMS/WhatsApp integrations |
| School visits | Yes | Route planning |
| Reports/export | Core | Forecasting |
| Documents/audit | Yes | Retention automation |
| AI assistant | No | Permission-aware read-only |

## Delivery phases
### Phase 0: Discovery and design (1–2 weeks; estimate only)
Confirm workflow interviews, data inventory, business rules, prototype, risk review, permissions and technical setup.
Deliverables: approved PRD, data dictionary, UI prototype, backlog, ADRs.

### Phase 1: Core foundation (2–4 sprints; estimate only)
Auth, profiles, roles, schools, contacts, classes/sections, courses, programs, schedules, teacher assignment, dashboard shell.
Exit: CRUD, validation, access controls, tests, migrations, demo dataset.

### Phase 2: Finance (2–4 sprints; estimate only)
Agreements, payment schedules, receivables, payments, receipts, income/expense, accounts, transfers and reconciliation reports.
Exit: no double-counted payments/transfers; approval and audit workflows tested.

### Phase 3: Inventory/procurement (2–3 sprints; estimate only)
Catalog, suppliers, purchase requests/orders, goods receiving, stock ledger, distribution, returns, inventory counts.
Exit: stock movements are ledger-driven and traceable.

### Phase 4: Teacher/field operations (2–3 sprints; estimate only)
Leave, tasks, visit reports, attendance, geofence configuration, check-in/out, exceptions and approvals.
Exit: privacy controls, offline/error states and anti-abuse checks tested.

### Phase 5: Notifications, calendar and reporting (2–3 sprints; estimate only)
Calendar, reminder engine, notification preferences, reports, exports and filters.
Exit: deduped notifications, configurable rules and report validation.

### Phase 6: Hardening and launch (2–3 sprints; estimate only)
Security review, accessibility, performance, backup restore, UAT, training, production deploy and monitoring.
Exit: signed UAT, rollback/restore rehearsal, production checklist.

These estimates are illustrative only; scope, team size and integrations determine actual delivery.
