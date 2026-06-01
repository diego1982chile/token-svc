# token-svc Scope

`token-svc` is a proof of concept for a small identity service built with Quarkus, JWT, Vaadin, Qute, and Quarkus Mailer.

## Decision

This project is scoped as a single-realm, single-audience, single-client identity proof of concept.

It is not intended to be a full multi-client or multi-realm identity provider.

`JWT_ISSUER` is the configured realm identity for issued tokens. `JWT_AUDIENCE` is the only supported audience; login and email-confirmation tokens use the same audience and are distinguished by purpose-specific claims such as `typ=email-confirmation`.

Email confirmation is centralized in this service. The confirmation link points to this service's Vaadin confirmation page, and the completed flow shows a neutral success message so the calling application or service can continue its own onboarding flow.

## Current Responsibilities

- Store users and credentials.
- Store internal roles.
- Authenticate users.
- Issue JWTs.
- Manage the Vaadin administration UI.
- Send email confirmation links.
- Confirm user email addresses.

## Internal Roles

Roles in this project are internal to `token-svc`.

- `ADMIN`: can access the `token-svc` administration UI and protected administration APIs.
- `USER`: basic authenticated user role.

These roles do not represent permissions for external business applications or other microservices.

## Non-Goals

- No multiple client applications.
- No multiple realms or tenants.
- No multiple JWT audiences.
- No per-application roles.
- No per-application user activation state.
- No OAuth/OIDC feature completeness.

If this project evolves into a reusable identity provider for multiple applications, the model should be redesigned before adding more features. That redesign should introduce client applications, scoped roles, and per-client user access state instead of extending the current global role and global user status model.
