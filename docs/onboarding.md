# Onboarding Architecture Notes

This document assumes the scope defined in [token-svc Scope](scope.md): single realm, single audience, single client, and internal roles only.

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
