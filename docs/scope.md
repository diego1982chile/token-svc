# token-svc Scope

`token-svc` is an identity service built with Quarkus, JWT, Vaadin, Qute, and
Quarkus Mailer.

## Decision

This project currently uses a single realm and a shared access-token role model.

It is not intended to be a full multi-client or multi-realm identity provider.

`JWT_ISSUER` is the configured realm identity for issued tokens.
`JWT_ACCESS_AUDIENCES` contains the services allowed to accept access tokens.
`JWT_AUDIENCE` remains the audience for email-confirmation tokens, which are
also distinguished by `typ=email-confirmation`.

Email confirmation is centralized in this service. The confirmation link points to this service's Vaadin confirmation page, and the completed flow shows a neutral success message so the calling application or service can continue its own onboarding flow.

## Current Responsibilities

- Store users and credentials.
- Store global access-token roles.
- Authenticate users.
- Issue JWTs.
- Manage the Vaadin administration UI.
- Send email confirmation links.
- Confirm user email addresses.

## Internal Roles

Roles in this project are internal to `token-svc`.

- `ADMIN`: can access the `token-svc` administration UI and protected administration APIs.
- `USER`: basic authenticated user role.

These roles are shared access-token roles such as `USER` and `ADMIN`. They are
not yet scoped per client application.

## Non-Goals

- No multiple client applications.
- No multiple realms or tenants.
- No per-application roles.
- No per-application user activation state.
- No OAuth/OIDC feature completeness.
- No SNS/SQS onboarding event transport yet.
- No transactional outbox for onboarding events yet.

If this project evolves into a reusable identity provider for multiple
applications, the model should introduce client applications, scoped roles, and
per-client user access state instead of extending the current global role and
global user status model.
