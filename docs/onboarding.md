# Onboarding Architecture Notes

This document assumes the scope defined in [token-svc Scope](scope.md): single
realm, shared access-token audiences, and global roles.

`token-svc` owns identity and authentication concerns. Its user status should describe whether an account can authenticate, not whether the user has completed commercial onboarding.

## User Status

`UserStatus` meanings in this service:

- `PENDING`: user was created, but the email confirmation flow is not complete.
- `ACTIVE`: email was confirmed and the account is allowed to log in.
- `INACTIVE`: account is disabled or blocked.

`ACTIVE` does not mean that the user has an active paid plan.

## Service Boundaries

Expected ownership across services:

- `token-svc`: users, credentials, roles, account status, email confirmation, login, JWT issuance.
- `profile-svc`: profile data, business/person metadata, profile completion status.
- `subscription-svc`: plans, checkout, payments, subscriptions, billing status.

Payment and subscription state should not be folded into `UserStatus`. A user can be `ACTIVE` in `token-svc` while having no subscription, a trial subscription, a past-due subscription, or a canceled subscription.

## Onboarding View

If a UI or API needs the complete onboarding state, that state should be composed outside `token-svc`.

Initial approach:

- Use a BFF or orchestrating API to query `token-svc`, `profile-svc`, and `subscription-svc`.
- Return a consolidated onboarding view to the client.

Possible future approach:

- Emit domain events from each service.
- Maintain a read projection in an onboarding or projection service.

Example events:

- `UserCreated`
- `UserActivated`
- `UserDeactivated`
- `EmailConfirmationRequested`
- `ProfileCompleted`
- `SubscriptionActivated`
- `SubscriptionCanceled`

## Deferred Migration to onboarding-svc

`token-svc` currently contains onboarding persistence, `OnboardingEvent`,
`OnboardingEngine`, Easy Rules transitions, train projection logic, and their
tests. This code is transitional, but it is reusable and must be migrated
rather than redesigned from scratch.

The migration is intentionally deferred. When resumed:

- `onboarding-svc` will own onboarding persistence, state transitions, and
  train projection.
- `token-svc` will publish `USER_REGISTERED` and `EMAIL_VERIFIED` events using
  a versioned, transport-neutral contract.
- The immediate migration path is an internal, cursor-based identity event
  feed exposed by `token-svc`, not direct REST callbacks.
- `token-svc` will persist identity events in an append-only event log and
  expose them through `GET /internal/identity-events?after=<cursor>&limit=<n>`.
- `onboarding-svc` will poll that feed, persist its source cursor, and process
  each event idempotently.
- The previously validated SNS/SQS approach remains a future option if fan-out,
  durable queueing, DLQ operations, or multiple independent consumers justify
  the added infrastructure.

Direct REST service-to-service event delivery is not planned because it adds
B2B authentication, retry handling, and availability coupling. The feed-based
approach is different: `onboarding-svc` pulls an ordered event log with cursor
and idempotency.

KYC is expected to be an external provider integration owned by
`onboarding-svc`. Provider callbacks/webhooks should be translated by
`onboarding-svc` into onboarding events. `token-svc` should remain focused on
identity, credentials, email confirmation, and JWT issuance.

Camel Quarkus is deferred until KYC, Stripe, or other integrations create
enough routing complexity to justify it.

The detailed migration order and event envelope are documented in
`onboarding-svc/docs/deferred-event-migration.md`.

## Email Confirmation Event

The current implementation publishes `EmailConfirmationRequested` through CDI async events. The local handler sends the email with Quarkus Mailer, but the event contract is intentionally transport-neutral so it can later be sent through Kafka, RabbitMQ, SQS, or an external email service.

Event envelope:

```json
{
  "eventId": "uuid",
  "eventType": "EmailConfirmationRequested",
  "version": 1,
  "occurredAt": "2026-05-22T21:00:00Z",
  "userId": "user@example.com",
  "email": "user@example.com",
  "confirmationUrl": "http://localhost:9090/token-service/users/confirm-email?token=...",
  "tokenExpiresAt": "2026-05-23T21:00:00Z"
}
```

`token-svc` builds the full `confirmationUrl`; an external email service should not need to know token internals or route construction.

The confirmation page is centralized in `token-svc` at `/users/confirm-email`. After a successful confirmation, the page shows a neutral completion message so callers can continue their own onboarding flow outside this service.

Example consolidated view:

```json
{
  "userId": "user@example.com",
  "accountStatus": "ACTIVE",
  "profileStatus": "COMPLETE",
  "subscriptionStatus": "NONE",
  "onboardingStep": "CHOOSE_PLAN"
}
```

This keeps identity, profile data, and billing logic independently owned while still allowing product flows to present a single onboarding state.
