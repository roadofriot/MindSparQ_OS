-- MindSparQ OS initial schema draft (review and harden before production)
-- Supabase/PostgreSQL. This draft is deliberately foundational, not exhaustive.
create extension if not exists pgcrypto;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete restrict,
  full_name text not null,
  phone text,
  email text,
  avatar_path text,
  preferred_language text not null default 'en',
  status text not null default 'active' check (status in ('active','inactive','suspended')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.schools (
  id uuid primary key default gen_random_uuid(),
  school_code text unique,
  name text not null,
  school_type text,
  status text not null default 'prospect' check (status in ('prospect','negotiation','contract_pending','active','inactive','suspended','expired')),
  address text,
  province text,
  district text,
  municipality text,
  ward text,
  latitude numeric(10,7),
  longitude numeric(10,7),
  location_accuracy_m numeric,
  phone text,
  email text,
  website text,
  principal_name text,
  coordinator_name text,
  notes text,
  created_by uuid references public.profiles(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.school_contacts (
  id uuid primary key default gen_random_uuid(),
  school_id uuid not null references public.schools(id) on delete restrict,
  name text not null,
  designation text,
  phone text,
  email text,
  is_primary boolean not null default false,
  notes text,
  created_at timestamptz not null default now()
);

create table if not exists public.school_classes (
  id uuid primary key default gen_random_uuid(),
  school_id uuid not null references public.schools(id) on delete restrict,
  name text not null,
  sort_order int,
  created_at timestamptz not null default now(),
  unique(school_id, name)
);

create table if not exists public.sections (
  id uuid primary key default gen_random_uuid(),
  class_id uuid not null references public.school_classes(id) on delete restrict,
  name text not null,
  created_at timestamptz not null default now(),
  unique(class_id, name)
);

create table if not exists public.teachers (
  id uuid primary key default gen_random_uuid(),
  profile_id uuid references public.profiles(id) on delete restrict,
  teacher_code text unique,
  full_name text not null,
  phone text,
  email text,
  address text,
  joining_date date,
  employment_type text,
  designation text,
  qualifications text,
  experience_years numeric(4,1),
  status text not null default 'active' check (status in ('active','inactive','leave','suspended','resigned')),
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.course_categories (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  description text,
  active boolean not null default true
);

create table if not exists public.courses (
  id uuid primary key default gen_random_uuid(),
  category_id uuid references public.course_categories(id) on delete restrict,
  course_code text unique,
  name text not null,
  description text,
  age_min int,
  age_max int,
  status text not null default 'active' check (status in ('active','inactive')),
  created_at timestamptz not null default now()
);

create table if not exists public.programs (
  id uuid primary key default gen_random_uuid(),
  school_id uuid not null references public.schools(id) on delete restrict,
  course_id uuid not null references public.courses(id) on delete restrict,
  name text not null,
  program_type text not null default 'custom',
  start_date date not null,
  end_date date,
  status text not null default 'planned' check (status in ('planned','pending','active','paused','completed','cancelled','expired')),
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  check (end_date is null or end_date >= start_date)
);

create table if not exists public.program_classes (
  program_id uuid not null references public.programs(id) on delete restrict,
  section_id uuid not null references public.sections(id) on delete restrict,
  primary key(program_id, section_id)
);

create table if not exists public.program_teacher_assignments (
  id uuid primary key default gen_random_uuid(),
  program_id uuid not null references public.programs(id) on delete restrict,
  teacher_id uuid not null references public.teachers(id) on delete restrict,
  assignment_role text not null default 'instructor',
  start_date date,
  end_date date,
  created_at timestamptz not null default now()
);

create table if not exists public.program_schedules (
  id uuid primary key default gen_random_uuid(),
  program_id uuid not null references public.programs(id) on delete restrict,
  weekday int check (weekday between 0 and 6),
  start_time time not null,
  end_time time not null,
  recurrence text not null default 'weekly',
  effective_from date,
  effective_until date,
  timezone text not null default 'Asia/Kathmandu',
  notes text,
  check (end_time > start_time)
);

create table if not exists public.suppliers (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  contact_person text,
  phone text,
  email text,
  address text,
  pan_vat text,
  notes text,
  active boolean not null default true
);

create table if not exists public.products (
  id uuid primary key default gen_random_uuid(),
  sku text unique,
  name text not null,
  category text,
  unit text not null default 'piece',
  description text,
  minimum_stock numeric(14,3) not null default 0,
  active boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists public.stock_locations (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  location_type text not null default 'warehouse',
  address text,
  active boolean not null default true
);

create table if not exists public.stock_movements (
  id uuid primary key default gen_random_uuid(),
  product_id uuid not null references public.products(id) on delete restrict,
  location_id uuid not null references public.stock_locations(id) on delete restrict,
  movement_type text not null check (movement_type in ('receipt','distribution','return','transfer_in','transfer_out','damage','loss','adjustment','count_variance')),
  quantity numeric(14,3) not null check (quantity > 0),
  unit_cost numeric(14,4),
  reference_type text,
  reference_id uuid,
  movement_date timestamptz not null default now(),
  notes text,
  created_by uuid references public.profiles(id),
  idempotency_key text unique
);

create table if not exists public.accounts (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  account_type text not null check (account_type in ('cash','bank','wallet','other')),
  currency text not null default 'NPR',
  opening_balance numeric(14,2) not null default 0,
  active boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists public.account_transactions (
  id uuid primary key default gen_random_uuid(),
  account_id uuid not null references public.accounts(id) on delete restrict,
  transaction_type text not null,
  amount numeric(14,2) not null check (amount > 0),
  direction text not null check (direction in ('in','out')),
  reference_type text,
  reference_id uuid,
  transaction_date timestamptz not null default now(),
  description text,
  created_by uuid references public.profiles(id),
  idempotency_key text unique
);

create table if not exists public.agreements (
  id uuid primary key default gen_random_uuid(),
  school_id uuid not null references public.schools(id) on delete restrict,
  agreement_code text unique,
  start_date date not null,
  end_date date,
  pricing_model text not null check (pricing_model in ('fixed','per_student','monthly','annual','milestone','custom')),
  agreed_amount numeric(14,2),
  currency text not null default 'NPR',
  terms text,
  status text not null default 'draft' check (status in ('draft','active','completed','expired','cancelled')),
  document_path text,
  created_at timestamptz not null default now(),
  check (end_date is null or end_date >= start_date)
);

create table if not exists public.receivables (
  id uuid primary key default gen_random_uuid(),
  agreement_id uuid references public.agreements(id) on delete restrict,
  school_id uuid not null references public.schools(id) on delete restrict,
  description text,
  due_date date,
  amount_due numeric(14,2) not null check (amount_due >= 0),
  currency text not null default 'NPR',
  status text not null default 'pending' check (status in ('pending','partial','paid','overdue','disputed','cancelled')),
  created_at timestamptz not null default now()
);

create table if not exists public.payments (
  id uuid primary key default gen_random_uuid(),
  receipt_no text unique,
  school_id uuid references public.schools(id) on delete restrict,
  account_id uuid not null references public.accounts(id) on delete restrict,
  amount numeric(14,2) not null check (amount > 0),
  currency text not null default 'NPR',
  payment_method text not null,
  transaction_reference text,
  payment_date timestamptz not null default now(),
  received_by uuid references public.profiles(id),
  notes text,
  idempotency_key text unique
);

create table if not exists public.payment_allocations (
  id uuid primary key default gen_random_uuid(),
  payment_id uuid not null references public.payments(id) on delete restrict,
  receivable_id uuid not null references public.receivables(id) on delete restrict,
  amount numeric(14,2) not null check (amount > 0),
  created_at timestamptz not null default now(),
  unique(payment_id, receivable_id)
);

create table if not exists public.expense_categories (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  active boolean not null default true
);

create table if not exists public.expenses (
  id uuid primary key default gen_random_uuid(),
  category_id uuid references public.expense_categories(id) on delete restrict,
  school_id uuid references public.schools(id) on delete restrict,
  program_id uuid references public.programs(id) on delete restrict,
  account_id uuid references public.accounts(id) on delete restrict,
  amount numeric(14,2) not null check (amount > 0),
  currency text not null default 'NPR',
  expense_date date not null,
  description text not null,
  vendor text,
  status text not null default 'pending' check (status in ('pending','approved','paid','rejected','cancelled')),
  attachment_path text,
  created_by uuid references public.profiles(id),
  approved_by uuid references public.profiles(id),
  created_at timestamptz not null default now()
);

create table if not exists public.tasks (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text,
  assigned_to uuid references public.profiles(id) on delete restrict,
  assigned_by uuid references public.profiles(id) on delete restrict,
  school_id uuid references public.schools(id) on delete restrict,
  due_at timestamptz,
  priority text not null default 'normal' check (priority in ('low','normal','high','urgent')),
  status text not null default 'pending' check (status in ('pending','in_progress','completed','overdue','cancelled')),
  created_at timestamptz not null default now()
);

create table if not exists public.attendance_records (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete restrict,
  teacher_id uuid references public.teachers(id) on delete restrict,
  school_id uuid references public.schools(id) on delete restrict,
  program_id uuid references public.programs(id) on delete restrict,
  attendance_type text not null check (attendance_type in ('office','school','visit','other')),
  check_in timestamptz,
  check_out timestamptz,
  latitude numeric(10,7),
  longitude numeric(10,7),
  accuracy_m numeric,
  verification_status text not null default 'pending',
  status text not null default 'present',
  notes text,
  created_at timestamptz not null default now()
);

create table if not exists public.notifications (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete restrict,
  title text not null,
  body text not null,
  category text,
  reference_type text,
  reference_id uuid,
  dedupe_key text unique,
  read_at timestamptz,
  created_at timestamptz not null default now()
);

create table if not exists public.audit_logs (
  id uuid primary key default gen_random_uuid(),
  actor_id uuid references public.profiles(id) on delete restrict,
  action text not null,
  entity_type text not null,
  entity_id uuid,
  old_values jsonb,
  new_values jsonb,
  created_at timestamptz not null default now()
);

-- Indexes to support common search/filter operations
create index if not exists idx_schools_status on public.schools(status);
create index if not exists idx_programs_school on public.programs(school_id);
create index if not exists idx_programs_dates on public.programs(start_date,end_date);
create index if not exists idx_teacher_status on public.teachers(status);
create index if not exists idx_receivables_school_due on public.receivables(school_id,due_date);
create index if not exists idx_payments_school_date on public.payments(school_id,payment_date);
create index if not exists idx_stock_movements_product_date on public.stock_movements(product_id,movement_date);
create index if not exists idx_account_transactions_account_date on public.account_transactions(account_id,transaction_date);
create index if not exists idx_attendance_user_checkin on public.attendance_records(user_id,check_in);
create index if not exists idx_tasks_assignee_status on public.tasks(assigned_to,status);

-- RLS is intentionally not enabled in this draft until policies are designed and
-- tested comprehensively. Never deploy with sensitive tables exposed publicly.
-- Enable RLS on all tables and create reviewed policies before production.
