# UI/UX Blueprint
## Design principles
Clean, professional, responsive, accessible, Apple-inspired but not a copy. Consistent spacing, typography, icons, feedback and forms. Provide light/dark themes and English/Nepali localization.

## Navigation
Dashboard; Schools; Academic (Courses, Programs, Classes, Enrollment); Teachers & HR; Attendance; Calendar; Tasks & Visits; Finance; Inventory & Purchases; Reports; Documents; Notifications; Users & Permissions; Settings.

## Desktop shell
Sidebar + top bar (global search, quick create, notifications, profile) + page content. Sidebar and data visibility are permission-aware.

## Key screens
1. Dashboard: KPI cards, cash/receivables, active programs, attendance exceptions, stock alerts, upcoming tasks.
2. School list: searchable/filterable, status, programs, outstanding, renewal.
3. School detail tabs: overview, contacts, classes, programs, teachers, agreements, payments, inventory, visits, documents, timeline.
4. Teacher detail: profile, assignments, schedule, attendance, leave, tasks, documents.
5. Course catalogue: category, levels, materials.
6. Program detail: school, course, classes, teacher, schedule, dates, financial model, materials, timeline.
7. Finance: receivables aging, collection, accounts, transaction ledger, expenses, transfer, reports.
8. Inventory: catalog, stock by location, movement ledger, purchase, distribution, return and count.
9. Calendar: agenda/day/week/month and filters.
10. Attendance: roster, exceptions, geofence configuration, approval.
11. Reports: filter panel, summary, export and saved views.

## Forms
- Inline validation and accessible errors.
- Save draft where long forms.
- Confirm irreversible/financial actions.
- Show required fields and defaults.
- Use search-select for entities; avoid huge dropdowns.
- Bulk operations only with confirmation and audit.
- Date picker supports AD and BS display; stored dates use canonical representation.

## Mobile
Teacher-focused: today's schedule, check-in, task, visit/class report and notifications. Management: school visit, collection record, assignment and approval. Do not force the full finance desktop experience onto small screens.
