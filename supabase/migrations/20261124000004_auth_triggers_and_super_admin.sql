-- MindSparQ OS: Auto-Profile Creation, Super Admin Allowlist & Role Assignment RPC (0004)

-- 1. Super Admin Allowlist (seed with the owner's two Super Admin emails)
create table if not exists public.super_admin_allowlist (
  email text primary key,
  note text,
  created_at timestamptz not null default now()
);

alter table public.super_admin_allowlist enable row level security;
create policy super_admin_allowlist_select on public.super_admin_allowlist
  for select using (public.auth_has_role(array['super_admin']));
create policy super_admin_allowlist_write on public.super_admin_allowlist
  for all using (public.auth_has_role(array['super_admin']));

insert into public.super_admin_allowlist (email, note) values
  ('merokhoj@gmail.com', 'Primary Super Admin'),
  ('samay.unik@gmail.com', 'Secondary Super Admin')
on conflict (email) do nothing;

-- 2. Trigger function: auto-create complete profile on auth.users insert (Gmail OAuth / Email)
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_full_name text;
  v_avatar text;
  v_super_role_id uuid;
begin
  v_full_name := coalesce(
    nullif(trim(new.raw_user_meta_data->>'full_name'), ''),
    nullif(trim(new.raw_user_meta_data->>'name'), ''),
    split_part(coalesce(new.email, 'User'), '@', 1)
  );
  v_avatar := new.raw_user_meta_data->>'avatar_url';

  insert into public.profiles (id, full_name, email, avatar_path, status)
  values (new.id, v_full_name, new.email, v_avatar, 'active')
  on conflict (id) do update
    set full_name = excluded.full_name,
        email = coalesce(excluded.email, public.profiles.email),
        avatar_path = coalesce(excluded.avatar_path, public.profiles.avatar_path),
        updated_at = now();

  -- If email is in super_admin_allowlist, automatically assign super_admin role
  if new.email is not null and exists (
    select 1 from public.super_admin_allowlist where lower(email) = lower(new.email)
  ) then
    select id into v_super_role_id from public.roles where code = 'super_admin' limit 1;
    if v_super_role_id is not null then
      insert into public.user_roles (user_id, role_id)
      values (new.id, v_super_role_id)
      on conflict (user_id, role_id) do nothing;
    end if;
  end if;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- 3. Super Admin RPC to assign or revoke roles for any authenticated user
create or replace function public.assign_user_role(
  p_target_user_id uuid,
  p_role_code text,
  p_assign boolean default true
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  v_role_id uuid;
begin
  if not public.auth_has_role(array['super_admin']) then
    raise exception 'Unauthorized: only Super Admin can manage user roles';
  end if;

  select id into v_role_id from public.roles where code = p_role_code limit 1;
  if v_role_id is null then
    raise exception 'Unknown role code: %', p_role_code;
  end if;

  if p_assign then
    insert into public.user_roles (user_id, role_id, assigned_by)
    values (p_target_user_id, v_role_id, auth.uid())
    on conflict (user_id, role_id) do nothing;
  else
    delete from public.user_roles
    where user_id = p_target_user_id and role_id = v_role_id;
  end if;

  insert into public.audit_logs (actor_id, action, entity_type, entity_id, new_values)
  values (
    auth.uid(),
    case when p_assign then 'assign_role' else 'revoke_role' end,
    'user_role',
    p_target_user_id,
    jsonb_build_object('role_code', p_role_code, 'assigned', p_assign)
  );
end;
$$;
