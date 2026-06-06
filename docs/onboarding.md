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

`token-svc` no longer exposes onboarding train or registration-status endpoints.
The product onboarding API belongs in `onboarding-svc`.

`token-svc` still contains transitional onboarding persistence,
`OnboardingEvent`, `OnboardingEngine`, Easy Rules transitions, and related tests.
This remaining code supports registration ids and email-confirmation state while
the identity event feed is deferred. It should be replaced by event publication
instead of redesigned as product onboarding logic.

Current transitional code that remains in `token-svc`:

- `OnboardingProcess` and `OnboardingProcessRepository`.
- `OnboardingEvent` and `OnboardingEventType`.
- `OnboardingEngine` and `DefaultOnboardingEngine`.
- Easy Rules transition classes.
- Calls from registration and email confirmation into the local onboarding
  engine.

This code must be treated as temporary compatibility code. Do not add product
onboarding features, train projection, KYC logic, subscription logic, or
presentation endpoints back into `token-svc`.

The migration is intentionally deferred. When resumed:

- `onboarding-svc` will own onboarding persistence, state transitions, and
  train projection.
- `token-svc` will publish `USER_REGISTERED` and `EMAIL_VERIFIED` identity
  events through a cursor-based feed contract.
- The immediate migration path is an internal, cursor-based identity event
  feed exposed by `token-svc`, not direct REST callbacks.
- `token-svc` will persist identity events in an append-only event log and
  expose them through `GET /internal/identity-events?after=<cursor>&limit=<n>`.
- `onboarding-svc` will poll that feed, persist its source cursor, and process
  each event idempotently.
- The feed is identity-specific. Future KYC, plan, or billing integrations
  should not be folded into `token-svc`.
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

Subscription plan and billing ownership is separate. `onboarding-svc` may own a
temporary plan-selection catalog while the product is simple, but payment
provider callbacks should enter the service that owns subscriptions. If a
future `subscription-svc` exists, `onboarding-svc` should consume subscription
outcomes from that service rather than handling payment provider webhooks
directly.

Camel Quarkus is deferred until KYC, Stripe, or other integrations create
enough routing complexity to justify it.

The detailed migration order and event envelope are documented in
`onboarding-svc/docs/deferred-event-migration.md`.

The immediate feed model should use HTTP resource classes, not the SNS/SQS
envelope. The previously implemented `IdentityEventEnvelope` belongs to the
validated SNS/SQS prototype and should not be treated as the central HTTP feed
contract.

Use these exact names for the feed implementation:

```text
entities/IdentityEventLogEntryEntity.java
repositories/IdentityEventLogEntryRepository.java
model/IdentityEventType.java
webservice/IdentityEventFeedWebService.java
webservice/impl/DefaultIdentityEventFeedWebService.java
webservice/resources/IdentityEventFeedItemResource.java
webservice/resources/IdentityEventFeedPageResource.java
```

Classification:

- `IdentityEventLogEntryEntity` is the append-only persisted event row.
- `IdentityEventLogEntryRepository` is the Spring Data repository for that
  entity.
- `IdentityEventType` is the internal application concept for identity event
  names.
- `IdentityEventFeedWebService` is the HTTP feed contract.
- `DefaultIdentityEventFeedWebService` is the Quarkus REST implementation.
- `IdentityEventFeedItemResource` and `IdentityEventFeedPageResource` are HTTP
  response payloads.

Next implementation steps in `token-svc`:

1. Add append-only `IdentityEventLogEntryEntity` persistence with `sequence`,
   `eventId`, `eventType`, `subject`, `occurredAt`, and optional
   `registrationId`.
2. Add feed response resources `IdentityEventFeedItemResource` and
   `IdentityEventFeedPageResource`.
3. Expose `GET /internal/identity-events?after=<cursor>&limit=<n>`.
4. Write `USER_REGISTERED` and `EMAIL_VERIFIED` to the event log while keeping
   the existing local onboarding engine calls temporarily.
5. After `onboarding-svc` consumes the feed end-to-end, remove direct
   onboarding state mutation from `token-svc`.

The next cleanup cut should therefore be event-feed first, removal second:

1. Implement the local identity event log and cursor feed in `token-svc`.
2. Append `USER_REGISTERED` from the public registration flow.
3. Append `EMAIL_VERIFIED` from the email confirmation flow.
4. Keep the existing local onboarding engine only long enough to preserve
   current registration behavior while `onboarding-svc` catches up.
5. Wire `onboarding-svc` to consume the feed and persist processed events
   idempotently.
6. Remove `OnboardingProcess`, `OnboardingEngine`, Easy Rules, and the
   repository from `token-svc`.

Do not spend more effort polishing the remaining local onboarding engine in
`token-svc`; its only purpose is to bridge the migration.

Event emission must be selective. The identity event log is not an audit log
for every user change. Only explicit user-facing onboarding flows should append
events, initially public registration (`USER_REGISTERED`) and user email
confirmation (`EMAIL_VERIFIED`). Administrative actions such as role changes,
manual activation/deactivation, support edits, migrations, or internal user
updates must not emit onboarding identity events by default.

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
