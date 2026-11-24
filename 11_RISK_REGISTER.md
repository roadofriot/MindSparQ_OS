# Risk Register
| Risk | Severity | Mitigation |
|---|---|---|
| Incorrect payment/ledger calculation | Critical | Atomic transactions, tests, reconciliation, adjustment-only correction |
| RLS or permission leak | Critical | RLS on all tables, role tests, independent review |
| GPS misuse or inaccurate evidence | High | Purpose limitation, accuracy, consent, exception path, no continuous tracking |
| Inventory discrepancies | High | Ledger, count, approval, reconciliation |
| Duplicate payments/receipts | High | Idempotency, unique references, transactional processing |
| Backup failure | Critical | Monitoring and restore drills |
| Scope creep | High | Phase roadmap, change control, MVP prioritization |
| Poor data migration | High | Data mapping, validation, dry run and reconciliation |
| Notification overload | Medium | Preferences, dedupe and configurable thresholds |
| Child/student data exposure | Critical | Minimize collection, restricted roles, retention and security review |
| Date/BS conversion error | Medium | Tested library, timezone handling and validation |
| Vendor lock-in | Medium | Export plan, documentation and portability review |
| Weak adoption | High | Training, pilot, feedback and usability testing |
