# Master Product Requirements Document (PRD)
## Product
**MindSparQ OS — Education Business Management & Operations Platform**

## 1. Product vision
Create one secure, responsive platform that manages MindSparQ's schools, instructors, courses, programs, schedules, enrollment, agreements, payment collections, income/expenses, accounts, inventory, procurement, field visits, attendance, tasks, notifications and reporting.

## 2. Outcomes
- Centralized and trustworthy operational records.
- Visibility into school/program/teacher assignments.
- Traceable collections, receivables, income, expenses and account movements.
- Stock accountability for books, abacus devices and learning kits.
- Privacy-aware attendance and field operations.
- Actionable reporting and reminders.
- Accessible English/Nepali interface with AD/BS dates and NPR.

## 3. Personas
- Super Admin / Owner: full control, configuration, audit, business oversight.
- Admin: permissions delegated by Super Admin.
- Management / Operations: school visits, assignments, tasks and permitted collection workflows.
- Finance Officer: agreements, invoices, collections, expense approvals, cash/bank and reporting.
- Inventory Officer: suppliers, purchasing, receipt, stock movement and distribution.
- Instructor: own profile, assigned schools/programs, schedule, attendance, tasks and reporting.
- Read-only Auditor (optional): authorized view of reports and audit history.

## 4. Functional domains
### 4.1 Identity and access
Account lifecycle, role/permission assignment, sessions, optional MFA, password reset, audit trail.
### 4.2 School CRM
Prospects and active schools, contacts, location, classes/sections, documents, visit history, contracts and status.
### 4.3 Course catalogue
Configurable categories, course metadata, levels, requirements, materials, curriculum links and lifecycle.
### 4.4 Program delivery
School-specific offerings, program dates/status, class/section scope, schedules, teacher assignments, delivery milestones and renewals.
### 4.5 Participants
Optional student/participant records and enrollment by class, section and program. Minimize child data, limit access and define retention.
### 4.6 Finance
Agreements, billing rules, payment schedules, invoices, partial payments, receipts, receivables, income/expense categories, account ledger, transfers and reporting.
### 4.7 Inventory and procurement
Product catalog, suppliers, purchase requests/orders, receiving, locations, stock ledger, distribution, returns, damage/loss and inventory counts.
### 4.8 HR and teacher operations
Profiles, qualifications, employment details, assignments, leave, attendance, class/visit reports, payment records (payroll expansion later).
### 4.9 Field and schedule operations
School visits, GPS attendance verification, tasks, calendar, reminders and incident reporting.
### 4.10 Analytics
Operational KPIs, financial reports, school/course/program/teacher performance and export.
### 4.11 Document management
Secure attachments, categories, related entities, expiry dates, preview/download permissions.
### 4.12 AI assistant (future)
Permission-aware, read-only natural-language reporting. Any action-changing workflow requires explicit approval and audit logging.

## 5. Non-functional requirements
- Security: least privilege, RLS, MFA for privileged roles, secure secrets, encrypted transport and backups.
- Privacy: transparent location permissions, limited retention, purpose limitation and access controls.
- Reliability: monitored service, tested backup restoration and clear incident procedures.
- Performance: pagination, indexes, efficient query patterns, avoid N+1.
- Accessibility: keyboard navigation, readable contrast, screen-reader semantics.
- Localization: English/Nepali UI, AD/BS date display, NPR formatting.
- Maintainability: modular code, migrations, automated tests, documented decisions.
- Portability: data export and migration strategy.
- Auditability: immutable-style audit events; corrections via adjustment/reversal instead of destructive edits.

## 6. Success measures
Track baseline then targets: active school records completeness, schedule coverage, collection follow-up coverage, overdue balance aging, inventory reconciliation variance, attendance exception resolution, reporting latency, error rate and user adoption. Set numerical targets after baseline measurement.

## 7. Out of scope for first release
Full double-entry accounting and tax filing, payroll compliance engine, parent/student portal, online payments, multi-tenant SaaS, automated background location tracking, AI write-actions and advanced LMS functionality.

## 8. Constraints and assumptions
- Initially one company organization, multiple schools and locations.
- Flexible contracts and payment rules.
- User permissions may vary per module/action.
- GPS is evidence with limitations, not absolute proof of presence.
- Confirm business-specific rates, workflows, date conventions and legal requirements before implementation.
