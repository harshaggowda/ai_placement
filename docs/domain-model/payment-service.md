# Payment Service — Domain Model & Database Design

Database: **`careeros_payment`**. Base package (future): `com.careeros.payment`. Conventions per
[`00-overview-erd.md`](00-overview-erd.md). Stripe is the processor; **Stripe object ids are stored
and `unique` for idempotency**. Money is `NUMERIC(19,4)` + ISO-4217 `currency char(3)`.

## Entities (7)

| Table | Base | Soft-del | Key columns | Unique | Indexes | FKs |
|---|---|---|---|---|---|---|
| `plans` | SoftDeletable | ✓ | code(varchar40,NN), name(varchar120,NN), description, price_amount(num19,4,NN), currency(char3,NN), `billing_interval`(enum,NN), stripe_price_id(varchar120), `status`(enum,NN), trial_days(int), features(jsonb) | `uk_plans_code`, `uk_plans_name`, `uk_plans_stripe_price`(stripe_price_id) | status | — |
| `subscriptions` | SoftDeletable | ✓ | `user_id`(NN), `plan_id`(NN), `status`(enum,NN), stripe_subscription_id(varchar120), stripe_customer_id(varchar120), current_period_start(ts), current_period_end(ts), cancel_at_period_end(bool,NN), canceled_at(ts), trial_end(ts) | `uk_subscriptions_stripe`(stripe_subscription_id) | user_id, status, plan_id | →plans |
| `invoices` | Auditable | — | `subscription_id`(null), `user_id`(NN), `status`(enum,NN), stripe_invoice_id(varchar120), amount_due(num19,4), amount_paid(num19,4), currency(char3), `coupon_id`(null), issued_at, due_at, paid_at | `uk_invoices_stripe`(stripe_invoice_id) | subscription_id, user_id, status | →subscriptions, →coupons |
| `payments` | Auditable | — | `invoice_id`(NN), `user_id`(NN), `status`(enum,NN), stripe_payment_intent_id(varchar120), amount(num19,4,NN), currency(char3,NN), `method`(enum), failure_reason(varchar255), processed_at | `uk_payments_stripe_pi`(stripe_payment_intent_id) | invoice_id, status | →invoices |
| `coupons` | SoftDeletable | ✓ | code(varchar60,NN), `type`(enum,NN), percent_off(num5,2,null), amount_off(num19,4,null), currency(char3,null), stripe_coupon_id(varchar120), max_redemptions(int,null), redeemed_count(int,NN), `status`(enum,NN), valid_from(ts), valid_until(ts) | `uk_coupons_code`(code) | status | — |
| `usage_records` | Auditable | — | `subscription_id`(NN), `user_id`(NN), metric(varchar80,NN), quantity(bigint,NN), recorded_at(ts,NN), period_start(date), period_end(date) | — | subscription_id, metric, recorded_at | →subscriptions |
| `webhook_events` | Auditable | — | `provider`(enum,NN), event_type(varchar120,NN), stripe_event_id(varchar120,NN), `status`(enum,NN), payload(jsonb,NN), received_at(ts,NN), processed_at(ts), error(text) | `uk_webhook_stripe_event`(stripe_event_id) | event_type, status | — |

**Enums:** `billing_interval`(MONTH,YEAR), plan/coupon `status`(ACTIVE,RETIRED/EXPIRED,DISABLED),
subscription `status`(TRIALING,ACTIVE,PAST_DUE,CANCELED,INCOMPLETE,EXPIRED), invoice `status`(DRAFT,
OPEN,PAID,VOID,UNCOLLECTIBLE), payment `status`(PENDING,SUCCEEDED,FAILED,REFUNDED), payment `method`
(CARD,BANK_TRANSFER,WALLET), coupon `type`(PERCENT,AMOUNT), webhook `provider`(STRIPE), webhook
`status`(RECEIVED,PROCESSED,FAILED,IGNORED).

## Relationships & JPA

```
plans 1───* subscriptions 1───* invoices 1───* payments
invoices *───0..1 coupons          subscriptions 1───* usage_records
webhook_events  (standalone, idempotent ingest ledger)
```
- `subscriptions→plans`, `invoices→subscriptions`(null for one-off), `payments→invoices`,
  `usage_records→subscriptions`, `invoices→coupons`(null) — all `@ManyToOne(LAZY)`.
- `webhook_events` has no relationships: it is the raw, idempotent inbound ledger consulted before
  any state change.

## DTOs

- Full `Create/Update/Response/Summary/Search`: **Plan**, **Coupon** (admin-managed catalog).
- `Response/Summary/Search`: **Subscription** (created via Stripe checkout, not a raw POST body),
  **Invoice**, **Payment**, **UsageRecord** (system/Stripe-driven, read-mostly).
- **`WebhookEvent` has no outward DTO** — internal idempotency/audit ledger, never client-facing
  (same rationale as auth token entities).

## Repositories (7) — contracts only

`PlanRepository` (`findByCode`, `findAllByStatus`, `findByStripePriceId`) · `SubscriptionRepository`
(`findAllByUserId`, `findByUserIdAndStatus`, `findByStripeSubscriptionId`) · `InvoiceRepository`
(`findAllByUserId`, `findByStripeInvoiceId`, `findAllBySubscriptionId`) · `PaymentRepository`
(`findAllByInvoiceId`, `findByStripePaymentIntentId`) · `CouponRepository` (`findByCode`,
`findAllByStatus`) · `UsageRecordRepository` (`findAllBySubscriptionIdAndMetric`,
`sumQuantityBySubscriptionIdAndMetricAndRecordedAtBetween`) · `WebhookEventRepository`
(`findByStripeEventId`, `existsByStripeEventId`). All `JpaRepository<E, UUID>`.

## API contracts (controllers not implemented)

- **Plans** `/api/payments/plans`: `GET`, `GET /{id}` (public); `POST/PUT/PATCH/DELETE` (admin).
- **Subscriptions** `/api/payments/subscriptions`: `GET` (self), `GET /{id}`,
  `POST /checkout` (creates Stripe Checkout session), `PATCH /{id}/cancel`, `POST /search`.
- **Invoices** `/api/payments/invoices`: `GET`, `GET /{id}` (read-only).
- **Payments** `/api/payments/payments`: `GET`, `GET /{id}` (read-only).
- **Coupons** `/api/payments/coupons`: admin CRUD + `GET /validate?code=` (public validation).
- **Usage** `/api/payments/usage-records`: `GET` (self, metered), `POST` (internal metering).
- **Webhook** `POST /api/payments/webhook` — Stripe ingestion endpoint (signature-verified;
  idempotent via `uk_webhook_stripe_event`). Not a CRUD resource.

## Key justifications

- **Stripe id uniqueness = idempotency** — `stripe_subscription_id`, `stripe_invoice_id`,
  `stripe_payment_intent_id`, and especially `webhook_events.stripe_event_id` are `unique`. Stripe
  delivers webhooks at-least-once; the unique event id makes processing exactly-once.
- **`webhook_events` as a durable ledger** — every inbound event is persisted raw (`payload jsonb`)
  with a processing `status` before any domain mutation, giving replayability and an audit trail.
- **Plan/Subscription separation** — `plans` is the priced catalog (mirrors Stripe Prices);
  `subscriptions` is the per-user instance. Price changes create new plans; existing subscriptions
  keep their `plan_id` (history preserved).
- **Money discipline** — `NUMERIC(19,4)` + explicit `currency`; never floating point.
- **`usage_records` for metered billing** — append-only quantities per `metric` per period, summed
  for usage-based charges (e.g. AI tokens), decoupled from the priced plan.
- **Invoice/Payment split** — one invoice may have multiple payment attempts (retries, partial,
  refunds); modeling them separately keeps the financial history accurate (3NF).
