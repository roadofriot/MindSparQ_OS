# Deployment, Operations & Maintenance
## Environments
Development, staging and production must be isolated. Use distinct credentials and storage buckets.

## Release process
1. Issue/PR and code review.
2. Automated lint, unit, integration and security tests.
3. Migration review and staging deploy.
4. UAT and regression.
5. Backup before production migration.
6. Deploy with release tag.
7. Smoke tests and monitoring.
8. Rollback plan if critical failure.

## Backup
- Automated database backup schedule chosen by recovery objectives.
- Storage backup/retention plan.
- Encrypted offsite backup.
- Restore test at regular intervals.
- Document RPO/RTO and recovery steps.
- Keep backup credentials separate.

## Monitoring
- Errors and exceptions.
- API/database latency.
- Failed notification delivery.
- Auth anomalies.
- Storage usage.
- Backup health.
- Financial transaction failure.
- Inventory reconciliation errors.

## Production checklist
- Domain/SSL.
- Secrets management.
- RLS review.
- Admin MFA.
- Backup/restore test.
- Privacy policy and consent language.
- Location policy.
- Data retention policy.
- User training.
- Support contact.
- Incident plan.
- UAT sign-off.
