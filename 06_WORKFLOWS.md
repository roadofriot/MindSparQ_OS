# Workflows
## School onboarding
Prospect → school record → contacts → classes/sections → agreement → program(s) → schedule → instructor assignment → enrollment/materials → activation → monitoring.

## Program launch
Course selection → school/class scope → dates → frequency → schedule → assigned instructor(s) → materials requirements → financial terms → readiness check → activate.

## Agreement and receivable
Create agreement → define pricing/billing model → generate payment schedule/receivable → reminder → follow-up task → receive payment → allocation → receipt → ledger/account update → reconcile.

## Payment recording
Validate permissions → validate account and amount → idempotency check → insert payment and allocations atomically → account transaction → receipt number → notification → audit. Reversal requires authorized adjustment, not deletion.

## Expense
Request (optional) → approval according to threshold → payment → account ledger → attachment → audit. Small petty-cash expenses may have configurable simplified approval.

## Purchase
Request → approval → purchase order → supplier → partial/full goods receipt → stock movement → invoice/payable → payment → reconciliation.

## Distribution
Select school/program/recipient/location → scan or select items → quantity validation → stock movement → acknowledgement → update school distribution summary.

## Stock count
Count by location → variance → review → authorized adjustment movement → report variance.

## Teacher attendance
Scheduled assignment → attendance window → location permission → geofence/accuracy verification → instructor confirmation → event record → exception review. Avoid always-on tracking.

## School visit
Task/visit planned → check-in (optional location evidence) → visit notes/issues/actions → follow-up task → manager review.

## Program renewal
Renewal date alerts → review performance and terms → renewal conversation/task → new agreement/version → schedule/materials update.

## Notifications
Rules evaluate upcoming dates and events → dedupe → permission check → user preference → delivery attempt → retry/log → action link.
