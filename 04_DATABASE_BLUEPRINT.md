# Database Blueprint & Data Dictionary
Draft data model; implementation must refine field types, relationships, indexes, constraints, retention and RLS.

## Shared conventions
- UUID primary keys.
- `created_at`, `updated_at` UTC timestamps.
- `created_by` / `updated_by` where appropriate.
- Archive/status rather than hard-delete for business records.
- Money: numeric/decimal, never floating point; currency defaults NPR.
- Quantity: numeric with precision; product units configurable.
- Transaction and stock ledgers append-only with correction/reversal records.
- Foreign keys and uniqueness constraints enforce integrity.

## Core tables
### profiles / roles / permissions
Profile associates auth identity with employee profile. Role grants are configurable; permissions are module/action/scope-based.
### schools
School identity, contacts, status, address, geolocation, visit/contact relations and notes.
### school_classes / sections
School-specific grade/class and section structures.
### teachers
Instructor employment/profile records linked to user profile when account exists.
### course_categories / courses / course_levels
Reusable catalogue, description, age/grade range, materials and status.
### programs
School-specific delivery instance with course, dates, status, scope and agreement link.
### program_classes / program_schedules / program_teacher_assignments
Program scope and delivery schedule, assigned instructors, recurrence and time.
### participants / enrollments
Minimal participant record and program/class enrollment; privacy and retention restrictions.
### agreements / agreement_lines / payment_schedules
Commercial agreement, pricing model (fixed, per participant, monthly, annual, milestone/custom), rate, billing periods, dates and scheduled amounts.
### invoices / invoice_lines
Optional billing documents linked to agreement/payment schedule; invoice amount and balance.
### payments / payment_allocations
Receipt transaction and allocation to one or multiple receivables; partial payments allowed.
### accounts / account_transactions / transfers
Cash/bank/wallet accounts, movements and transfer records. Transfers are not income/expense.
### expense_records / expense_categories
Expense requests, approval, payment and attachment references.
### suppliers / purchase_orders / purchase_order_lines / goods_receipts
Supplier and purchasing lifecycle.
### products / stock_locations / stock_movements
Catalog and ledger-based stock movements. Receipt, distribution, return, adjustment, damage, loss and count variance.
### distributions / distribution_lines
School/teacher/participant distribution with receiver, source location and program context.
### attendance_records / geofences
Attendance event, schedule linkage, check-in/out, location evidence, accuracy, verification and exception state. Do not persist continuous location trails.
### leave_requests / tasks / visits / events
Workflow and operations records.
### notifications / notification_preferences
Delivery state, action link, dedupe key and user preferences.
### documents / audit_logs
Private file metadata, linkage and audit events.

## Integrity rules
- Payment amount must be positive; allocation total cannot exceed payment or open receivable (unless approved credit).
- Agreement currency and payment currency must match or conversion be explicitly modeled.
- Stock cannot become negative except explicitly authorized backorder/negative-stock setting.
- Transfer debit and credit are atomic and balanced.
- Program end date >= start date.
- Attendance linked to schedule and assigned user where possible.
- Unique receipt and document reference numbers within defined scope.
- Critical records cannot be hard-deleted through UI.
