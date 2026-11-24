-- MindSparQ OS: Atomic Transactional RPCs & Audit Helpers (0003)
-- Enforces idempotency, balance/stock integrity, and immutable audit logging.

-- ============================================================================
-- 1. ATOMIC PAYMENT RECORDING + ALLOCATION + RECEIPT + LEDGER
-- ============================================================================
create or replace function public.record_payment_atomic(
  p_school_id uuid,
  p_account_id uuid,
  p_amount numeric,
  p_payment_method text,
  p_transaction_reference text,
  p_notes text,
  p_idempotency_key text,
  p_allocations jsonb -- array of { "receivable_id": uuid, "amount": numeric }
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_existing_id uuid;
  v_payment_id uuid;
  v_receipt_no text;
  v_alloc jsonb;
  v_alloc_sum numeric := 0;
  v_rec_id uuid;
  v_alloc_amt numeric;
begin
  if not public.auth_has_role(array['super_admin','finance','management']) then
    raise exception 'Unauthorized: insufficient permissions to record payment';
  end if;

  if p_amount is null or p_amount <= 0 then
    raise exception 'Payment amount must be positive';
  end if;

  -- Idempotency check
  if p_idempotency_key is not null then
    select id into v_existing_id from public.payments where idempotency_key = p_idempotency_key;
    if v_existing_id is not null then
      return v_existing_id;
    end if;
  end if;

  v_receipt_no := 'RCP-' || to_char(now(), 'YYYYMMDD') || '-' || substr(gen_random_uuid()::text, 1, 6);

  insert into public.payments (
    receipt_no, school_id, account_id, amount, payment_method,
    transaction_reference, received_by, notes, idempotency_key
  ) values (
    v_receipt_no, p_school_id, p_account_id, p_amount, p_payment_method,
    p_transaction_reference, auth.uid(), p_notes, p_idempotency_key
  ) returning id into v_payment_id;

  -- Allocate to receivables
  if p_allocations is not null then
    for v_alloc in select * from jsonb_array_elements(p_allocations)
    loop
      v_rec_id := (v_alloc->>'receivable_id')::uuid;
      v_alloc_amt := (v_alloc->>'amount')::numeric;
      if v_alloc_amt <= 0 then
        raise exception 'Allocation amount must be positive';
      end if;
      v_alloc_sum := v_alloc_sum + v_alloc_amt;

      insert into public.payment_allocations (payment_id, receivable_id, amount)
      values (v_payment_id, v_rec_id, v_alloc_amt);

      -- Update receivable status based on total allocated
      update public.receivables r
      set status = case
        when (select coalesce(sum(pa.amount), 0) from public.payment_allocations pa where pa.receivable_id = r.id) >= r.amount_due then 'paid'
        else 'partial'
      end
      where r.id = v_rec_id;
    end loop;

    if v_alloc_sum > p_amount then
      raise exception 'Total allocations (%) exceed payment amount (%)', v_alloc_sum, p_amount;
    end if;
  end if;

  -- Account ledger entry
  insert into public.account_transactions (
    account_id, transaction_type, amount, direction,
    reference_type, reference_id, description, created_by, idempotency_key
  ) values (
    p_account_id, 'payment_receipt', p_amount, 'in',
    'payment', v_payment_id, 'Receipt ' || v_receipt_no, auth.uid(),
    case when p_idempotency_key is not null then p_idempotency_key || ':acct' else null end
  );

  -- Audit log
  insert into public.audit_logs (actor_id, action, entity_type, entity_id, new_values)
  values (auth.uid(), 'record_payment', 'payment', v_payment_id,
    jsonb_build_object('receipt_no', v_receipt_no, 'amount', p_amount, 'school_id', p_school_id));

  return v_payment_id;
end;
$$;

-- ============================================================================
-- 2. ATOMIC ACCOUNT TRANSFER (NOT INCOME OR EXPENSE)
-- ============================================================================
create or replace function public.transfer_between_accounts(
  p_from_account_id uuid,
  p_to_account_id uuid,
  p_amount numeric,
  p_reference_no text,
  p_notes text,
  p_idempotency_key text
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_existing_id uuid;
  v_transfer_id uuid;
begin
  if not public.auth_has_role(array['super_admin','finance']) then
    raise exception 'Unauthorized: insufficient permissions for account transfer';
  end if;

  if p_from_account_id = p_to_account_id then
    raise exception 'Source and destination accounts must differ';
  end if;

  if p_amount is null or p_amount <= 0 then
    raise exception 'Transfer amount must be positive';
  end if;

  if p_idempotency_key is not null then
    select id into v_existing_id from public.transfers where idempotency_key = p_idempotency_key;
    if v_existing_id is not null then
      return v_existing_id;
    end if;
  end if;

  insert into public.transfers (
    from_account_id, to_account_id, amount, reference_no, notes, created_by, idempotency_key
  ) values (
    p_from_account_id, p_to_account_id, p_amount, p_reference_no, p_notes, auth.uid(), p_idempotency_key
  ) returning id into v_transfer_id;

  insert into public.account_transactions (
    account_id, transaction_type, amount, direction, reference_type, reference_id, description, created_by
  ) values
    (p_from_account_id, 'transfer_out', p_amount, 'out', 'transfer', v_transfer_id, coalesce(p_notes, 'Account transfer out'), auth.uid()),
    (p_to_account_id, 'transfer_in', p_amount, 'in', 'transfer', v_transfer_id, coalesce(p_notes, 'Account transfer in'), auth.uid());

  insert into public.audit_logs (actor_id, action, entity_type, entity_id, new_values)
  values (auth.uid(), 'transfer_accounts', 'transfer', v_transfer_id,
    jsonb_build_object('from', p_from_account_id, 'to', p_to_account_id, 'amount', p_amount));

  return v_transfer_id;
end;
$$;

-- ============================================================================
-- 3. CURRENT STOCK HELPER VIEW & NEGATIVE-STOCK GUARDED DISTRIBUTION
-- ============================================================================
create or replace function public.get_location_stock(p_product_id uuid, p_location_id uuid)
returns numeric
language sql
stable
security definer
set search_path = public
as $$
  select coalesce(sum(
    case
      when movement_type in ('receipt','return','transfer_in','adjustment') then quantity
      else -quantity
    end
  ), 0)
  from public.stock_movements
  where product_id = p_product_id
    and location_id = p_location_id;
$$;

create or replace function public.distribute_stock_atomic(
  p_source_location_id uuid,
  p_school_id uuid,
  p_program_id uuid,
  p_recipient_name text,
  p_notes text,
  p_idempotency_key text,
  p_lines jsonb -- array of { "product_id": uuid, "quantity": numeric }
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_existing_id uuid;
  v_dist_id uuid;
  v_dist_no text;
  v_line jsonb;
  v_prod_id uuid;
  v_qty numeric;
  v_available numeric;
begin
  if not public.auth_has_role(array['super_admin','inventory','management']) then
    raise exception 'Unauthorized: insufficient permissions to distribute stock';
  end if;

  if p_idempotency_key is not null then
    select id into v_existing_id from public.distributions where idempotency_key = p_idempotency_key;
    if v_existing_id is not null then
      return v_existing_id;
    end if;
  end if;

  v_dist_no := 'DST-' || to_char(now(), 'YYYYMMDD') || '-' || substr(gen_random_uuid()::text, 1, 6);

  insert into public.distributions (
    distribution_no, source_location_id, school_id, program_id,
    recipient_name, distributed_by, notes, idempotency_key
  ) values (
    v_dist_no, p_source_location_id, p_school_id, p_program_id,
    p_recipient_name, auth.uid(), p_notes, p_idempotency_key
  ) returning id into v_dist_id;

  for v_line in select * from jsonb_array_elements(p_lines)
  loop
    v_prod_id := (v_line->>'product_id')::uuid;
    v_qty := (v_line->>'quantity')::numeric;
    if v_qty <= 0 then
      raise exception 'Distribution quantity must be positive';
    end if;

    v_available := public.get_location_stock(v_prod_id, p_source_location_id);
    if v_available < v_qty then
      raise exception 'Insufficient stock for product % (available: %, requested: %)', v_prod_id, v_available, v_qty;
    end if;

    insert into public.distribution_lines (distribution_id, product_id, quantity)
    values (v_dist_id, v_prod_id, v_qty);

    insert into public.stock_movements (
      product_id, location_id, movement_type, quantity,
      reference_type, reference_id, notes, created_by
    ) values (
      v_prod_id, p_source_location_id, 'distribution', v_qty,
      'distribution', v_dist_id, 'Distribution ' || v_dist_no, auth.uid()
    );
  end loop;

  insert into public.audit_logs (actor_id, action, entity_type, entity_id, new_values)
  values (auth.uid(), 'distribute_stock', 'distribution', v_dist_id,
    jsonb_build_object('distribution_no', v_dist_no, 'school_id', p_school_id));

  return v_dist_id;
end;
$$;
