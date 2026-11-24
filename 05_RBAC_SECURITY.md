# RBAC, Security & Privacy
## Roles (initial)
- Super Admin: full administration and all modules.
- Admin: delegated configurable permissions.
- Management: operations, school coordination, tasks, visits and approved financial actions.
- Finance: agreements, billing, collections, expense, accounts and finance reports.
- Inventory: purchasing, receiving, stock and distribution.
- Teacher: own profile, assignments, schedule, attendance, tasks and permitted reports.
- Auditor/read-only: explicitly assigned records and reports.

Role labels are configurable; use permission bundles rather than fixed hard-coded role assumptions.

## Permission dimensions
Module × action (view/create/update/approve/export/archive) × scope (all/assigned/own/selected schools).
Use server-side authorization and RLS for sensitive tables.

## Security checklist
- MFA for Super Admin/Finance/Admin recommended.
- Secure password policies and recovery.
- RLS on all sensitive tables and storage.
- Review service-role key use; never expose it in client apps.
- Rate-limit login and sensitive endpoints.
- Validate uploads, MIME types, size, filenames and malware scanning where available.
- Private storage; short-lived signed links.
- Audit privilege changes, payments, adjustments, stock, attendance overrides.
- Enforce approval limits and separation of duties where practical.
- Encrypt transport and backups.
- Test tenant/company boundary even if initially single-company.
- Define retention and deletion policy for student, staff, location and documents.
- Incident response and access revocation process.

## GPS/location policy
- Location tracking limited to explicit attendance/visit context and configured work windows.
- Explain why location is collected and request consent/permission.
- Prefer geofence check-in over continuous tracking.
- Capture accuracy, timestamp and verification outcome.
- Manual exceptions with approval and audit.
- Never assume GPS proves class delivery.
- Retain raw coordinates only as long as operationally necessary.
- Provide controls for denied permission, poor accuracy and offline state.
