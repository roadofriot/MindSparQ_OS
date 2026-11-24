-- MindSparQ OS: RBAC Seed & Row-Level Security (RLS) Migration (0002)
-- Enforces least-privilege access across all tables.

-- ============================================================================
-- 1. SEED SYSTEM ROLES
-- ============================================================================
insert into public.roles (code, name, description, is_system) values
  ('super_admin', 'Super Admin', 'Full administration across all modules and audit history', true),
  ('admin', 'Admin', 'Delegated configurable administration', true),
  ('management', 'Management / Operations', 'School coordination, visits, tasks, and approved collections', true),
  ('finance', 'Finance Officer', 'Agreements, billing, collections, expenses, and accounts', true),
  ('inventory', 'Inventory Officer', 'Suppliers, purchasing, stock ledger, and distribution', true),
  ('teacher', 'Instructor', 'Own profile, assigned programs, schedules, attendance, and tasks', true),
  ('auditor', 'Read-only Auditor', 'Authorized read-only access to reports and audit logs', true)
on conflict (code) do nothing;

-- ============================================================================
-- 2. HELPER FUNCTIONS (SECURITY DEFINER)
-- ============================================================================
create or replace function public.auth_has_role(p_role_codes text[])
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.user_roles ur
    join public.roles r on r.id = ur.role_id
    where ur.user_id = auth.uid()
      and r.code = any(p_role_codes)
  );
$$;

create or replace function public.auth_has_permission(p_module text, p_action text)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select
    public.auth_has_role(array['super_admin'])
    or exists (
      select 1
      from public.user_roles ur
      join public.role_permissions rp on rp.role_id = ur.role_id
      join public.permissions p on p.id = rp.permission_id
      where ur.user_id = auth.uid()
        and p.module = p_module
        and p.action = p_action
    );
$$;

create or replace function public.auth_assigned_school_ids()
returns setof uuid
language sql
stable
security definer
set search_path = public
as $$
  select distinct p.school_id
  from public.programs p
  join public.program_teacher_assignments pta on pta.program_id = p.id
  join public.teachers t on t.id = pta.teacher_id
  where t.profile_id = auth.uid();
$$;

-- ============================================================================
-- 3. ENABLE RLS ON ALL TABLES
-- ============================================================================
alter table public.profiles enable row level security;
alter table public.roles enable row level security;
alter table public.permissions enable row level security;
alter table public.role_permissions enable row level security;
alter table public.user_roles enable row level security;
alter table public.schools enable row level security;
alter table public.school_contacts enable row level security;
alter table public.school_classes enable row level security;
alter table public.sections enable row level security;
alter table public.teachers enable row level security;
alter table public.course_categories enable row level security;
alter table public.courses enable row level security;
alter table public.course_levels enable row level security;
alter table public.programs enable row level security;
alter table public.program_classes enable row level security;
alter table public.program_teacher_assignments enable row level security;
alter table public.program_schedules enable row level security;
alter table public.participants enable row level security;
alter table public.enrollments enable row level security;
alter table public.accounts enable row level security;
alter table public.account_transactions enable row level security;
alter table public.transfers enable row level security;
alter table public.agreements enable row level security;
alter table public.agreement_lines enable row level security;
alter table public.payment_schedules enable row level security;
alter table public.receivables enable row level security;
alter table public.invoices enable row level security;
alter table public.invoice_lines enable row level security;
alter table public.payments enable row level security;
alter table public.payment_allocations enable row level security;
alter table public.expense_categories enable row level security;
alter table public.expenses enable row level security;
alter table public.suppliers enable row level security;
alter table public.products enable row level security;
alter table public.stock_locations enable row level security;
alter table public.purchase_orders enable row level security;
alter table public.purchase_order_lines enable row level security;
alter table public.goods_receipts enable row level security;
alter table public.distributions enable row level security;
alter table public.distribution_lines enable row level security;
alter table public.stock_movements enable row level security;
alter table public.geofences enable row level security;
alter table public.attendance_records enable row level security;
alter table public.leave_requests enable row level security;
alter table public.tasks enable row level security;
alter table public.visits enable row level security;
alter table public.events enable row level security;
alter table public.notifications enable row level security;
alter table public.notification_preferences enable row level security;
alter table public.documents enable row level security;
alter table public.audit_logs enable row level security;

-- ============================================================================
-- 4. CORE RLS POLICIES
-- ============================================================================
-- Profiles: own profile or admin/super_admin
create policy profiles_select on public.profiles for select
  using (id = auth.uid() or public.auth_has_role(array['super_admin','admin','management']));
create policy profiles_update_self on public.profiles for update
  using (id = auth.uid() or public.auth_has_role(array['super_admin','admin']));

-- RBAC metadata: authenticated read, super_admin write
create policy roles_select on public.roles for select to authenticated using (true);
create policy roles_write on public.roles for all using (public.auth_has_role(array['super_admin']));
create policy permissions_select on public.permissions for select to authenticated using (true);
create policy role_permissions_select on public.role_permissions for select to authenticated using (true);
create policy user_roles_select on public.user_roles for select
  using (user_id = auth.uid() or public.auth_has_role(array['super_admin','admin']));
create policy user_roles_write on public.user_roles for all
  using (public.auth_has_role(array['super_admin']));

-- Schools: operations/admin/finance/inventory/auditor see all; teachers see assigned schools
create policy schools_select on public.schools for select
  using (
    public.auth_has_role(array['super_admin','admin','management','finance','inventory','auditor'])
    or id in (select public.auth_assigned_school_ids())
  );
create policy schools_write on public.schools for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy school_contacts_select on public.school_contacts for select
  using (
    public.auth_has_role(array['super_admin','admin','management','finance','auditor'])
    or school_id in (select public.auth_assigned_school_ids())
  );
create policy school_contacts_write on public.school_contacts for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy school_classes_select on public.school_classes for select to authenticated using (true);
create policy school_classes_write on public.school_classes for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy sections_select on public.sections for select to authenticated using (true);
create policy sections_write on public.sections for all
  using (public.auth_has_role(array['super_admin','admin','management']));

-- Teachers & Academic
create policy teachers_select on public.teachers for select
  using (
    profile_id = auth.uid()
    or public.auth_has_role(array['super_admin','admin','management','auditor'])
  );
create policy teachers_write on public.teachers for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy course_categories_select on public.course_categories for select to authenticated using (true);
create policy course_categories_write on public.course_categories for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy courses_select on public.courses for select to authenticated using (true);
create policy courses_write on public.courses for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy course_levels_select on public.course_levels for select to authenticated using (true);
create policy course_levels_write on public.course_levels for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy programs_select on public.programs for select
  using (
    public.auth_has_role(array['super_admin','admin','management','finance','inventory','auditor'])
    or school_id in (select public.auth_assigned_school_ids())
  );
create policy programs_write on public.programs for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy program_classes_select on public.program_classes for select to authenticated using (true);
create policy program_classes_write on public.program_classes for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy program_teacher_assignments_select on public.program_teacher_assignments for select to authenticated using (true);
create policy program_teacher_assignments_write on public.program_teacher_assignments for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy program_schedules_select on public.program_schedules for select to authenticated using (true);
create policy program_schedules_write on public.program_schedules for all
  using (public.auth_has_role(array['super_admin','admin','management']));

-- Participants: strict child-data privacy
create policy participants_select on public.participants for select
  using (
    public.auth_has_role(array['super_admin','admin','management'])
    or school_id in (select public.auth_assigned_school_ids())
  );
create policy participants_write on public.participants for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy enrollments_select on public.enrollments for select
  using (
    public.auth_has_role(array['super_admin','admin','management'])
    or exists (
      select 1 from public.programs pr
      where pr.id = enrollments.program_id
        and pr.school_id in (select public.auth_assigned_school_ids())
    )
  );
create policy enrollments_write on public.enrollments for all
  using (public.auth_has_role(array['super_admin','admin','management']));

-- Finance: restricted to finance, super_admin, admin, auditor (read-only), management (approved)
create policy accounts_select on public.accounts for select
  using (public.auth_has_role(array['super_admin','admin','finance','auditor']));
create policy accounts_write on public.accounts for all
  using (public.auth_has_role(array['super_admin','finance']));

create policy account_transactions_select on public.account_transactions for select
  using (public.auth_has_role(array['super_admin','admin','finance','auditor']));

create policy transfers_select on public.transfers for select
  using (public.auth_has_role(array['super_admin','admin','finance','auditor']));

create policy agreements_select on public.agreements for select
  using (public.auth_has_role(array['super_admin','admin','finance','management','auditor']));
create policy agreements_write on public.agreements for all
  using (public.auth_has_role(array['super_admin','finance']));

create policy agreement_lines_select on public.agreement_lines for select
  using (public.auth_has_role(array['super_admin','admin','finance','management','auditor']));
create policy agreement_lines_write on public.agreement_lines for all
  using (public.auth_has_role(array['super_admin','finance']));

create policy payment_schedules_select on public.payment_schedules for select
  using (public.auth_has_role(array['super_admin','admin','finance','management','auditor']));
create policy payment_schedules_write on public.payment_schedules for all
  using (public.auth_has_role(array['super_admin','finance']));

create policy receivables_select on public.receivables for select
  using (public.auth_has_role(array['super_admin','admin','finance','management','auditor']));
create policy receivables_write on public.receivables for all
  using (public.auth_has_role(array['super_admin','finance']));

create policy invoices_select on public.invoices for select
  using (public.auth_has_role(array['super_admin','admin','finance','management','auditor']));
create policy invoices_write on public.invoices for all
  using (public.auth_has_role(array['super_admin','finance']));

create policy invoice_lines_select on public.invoice_lines for select
  using (public.auth_has_role(array['super_admin','admin','finance','management','auditor']));
create policy invoice_lines_write on public.invoice_lines for all
  using (public.auth_has_role(array['super_admin','finance']));

create policy payments_select on public.payments for select
  using (public.auth_has_role(array['super_admin','admin','finance','management','auditor']));

create policy payment_allocations_select on public.payment_allocations for select
  using (public.auth_has_role(array['super_admin','admin','finance','management','auditor']));

create policy expense_categories_select on public.expense_categories for select to authenticated using (true);
create policy expense_categories_write on public.expense_categories for all
  using (public.auth_has_role(array['super_admin','finance']));

create policy expenses_select on public.expenses for select
  using (created_by = auth.uid() or public.auth_has_role(array['super_admin','admin','finance','management','auditor']));
create policy expenses_insert on public.expenses for insert
  with check (created_by = auth.uid() or public.auth_has_role(array['super_admin','finance']));
create policy expenses_update on public.expenses for update
  using (public.auth_has_role(array['super_admin','finance','management']));

-- Inventory & Procurement
create policy suppliers_select on public.suppliers for select
  using (public.auth_has_role(array['super_admin','admin','inventory','finance','auditor']));
create policy suppliers_write on public.suppliers for all
  using (public.auth_has_role(array['super_admin','inventory']));

create policy products_select on public.products for select to authenticated using (true);
create policy products_write on public.products for all
  using (public.auth_has_role(array['super_admin','inventory']));

create policy stock_locations_select on public.stock_locations for select to authenticated using (true);
create policy stock_locations_write on public.stock_locations for all
  using (public.auth_has_role(array['super_admin','inventory']));

create policy purchase_orders_select on public.purchase_orders for select
  using (public.auth_has_role(array['super_admin','admin','inventory','finance','auditor']));
create policy purchase_orders_write on public.purchase_orders for all
  using (public.auth_has_role(array['super_admin','inventory','finance']));

create policy purchase_order_lines_select on public.purchase_order_lines for select
  using (public.auth_has_role(array['super_admin','admin','inventory','finance','auditor']));
create policy purchase_order_lines_write on public.purchase_order_lines for all
  using (public.auth_has_role(array['super_admin','inventory','finance']));

create policy goods_receipts_select on public.goods_receipts for select
  using (public.auth_has_role(array['super_admin','admin','inventory','finance','auditor']));

create policy distributions_select on public.distributions for select
  using (public.auth_has_role(array['super_admin','admin','inventory','management','auditor']));

create policy distribution_lines_select on public.distribution_lines for select
  using (public.auth_has_role(array['super_admin','admin','inventory','management','auditor']));

create policy stock_movements_select on public.stock_movements for select
  using (public.auth_has_role(array['super_admin','admin','inventory','finance','auditor']));

-- Field Ops, Attendance, Notifications, Documents & Audit
create policy geofences_select on public.geofences for select to authenticated using (true);
create policy geofences_write on public.geofences for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy attendance_select on public.attendance_records for select
  using (user_id = auth.uid() or public.auth_has_role(array['super_admin','admin','management','auditor']));
create policy attendance_insert on public.attendance_records for insert
  with check (user_id = auth.uid());
create policy attendance_update on public.attendance_records for update
  using (user_id = auth.uid() or public.auth_has_role(array['super_admin','admin','management']));

create policy leave_requests_select on public.leave_requests for select
  using (user_id = auth.uid() or public.auth_has_role(array['super_admin','admin','management']));
create policy leave_requests_insert on public.leave_requests for insert
  with check (user_id = auth.uid());
create policy leave_requests_update on public.leave_requests for update
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy tasks_select on public.tasks for select
  using (assigned_to = auth.uid() or assigned_by = auth.uid() or public.auth_has_role(array['super_admin','admin','management']));
create policy tasks_write on public.tasks for all
  using (assigned_to = auth.uid() or public.auth_has_role(array['super_admin','admin','management']));

create policy visits_select on public.visits for select
  using (visitor_id = auth.uid() or public.auth_has_role(array['super_admin','admin','management','auditor']));
create policy visits_write on public.visits for all
  using (visitor_id = auth.uid() or public.auth_has_role(array['super_admin','admin','management']));

create policy events_select on public.events for select to authenticated using (true);
create policy events_write on public.events for all
  using (public.auth_has_role(array['super_admin','admin','management']));

create policy notifications_select on public.notifications for select using (user_id = auth.uid());
create policy notifications_update on public.notifications for update using (user_id = auth.uid());

create policy notification_prefs_all on public.notification_preferences for all using (user_id = auth.uid());

create policy documents_select on public.documents for select
  using (uploaded_by = auth.uid() or public.auth_has_role(array['super_admin','admin','management','finance','auditor']));
create policy documents_insert on public.documents for insert
  with check (uploaded_by = auth.uid());

create policy audit_logs_select on public.audit_logs for select
  using (public.auth_has_role(array['super_admin','auditor']));
