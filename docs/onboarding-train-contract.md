# Onboarding Train Contract

The onboarding frontend presents a product-level train with three visible steps.
Each visible step can group one or more internal onboarding events and states.

## Visible Train Steps

```text
REGISTRATION
IDENTITY_CHECK
PLAN_SELECTION
```

Labels used by the backend response:

- `REGISTRATION`: Registro
- `IDENTITY_CHECK`: Comprueba tu identidad
- `PLAN_SELECTION`: Elige tu plan

## Internal States

The backend keeps a more granular state chain:

```text
REGISTERED
EMAIL_VERIFIED
KYC_APPROVED
PLAN_SELECTED
PROFILE_COMPLETED
READY_TO_PUBLISH
```

The frontend should not assume a one-to-one relationship between internal states
and train steps.

## State To Train Mapping

| Internal state | Current train step | Notes |
| --- | --- | --- |
| `REGISTERED` | `REGISTRATION` | User was created; email confirmation is still part of registration. |
| `EMAIL_VERIFIED` | `IDENTITY_CHECK` | Registration is complete; KYC can start. |
| `KYC_APPROVED` | `PLAN_SELECTION` | Identity check is complete; user can choose a plan. |
| `PLAN_SELECTED` | `PLAN_SELECTION` | Plan step can still include checkout/payment/subscription activation. |
| `PROFILE_COMPLETED` | `PLAN_SELECTION` | Current frontend train still has three steps; profile is not shown as its own train step. |
| `READY_TO_PUBLISH` | none | All train steps are complete. |

## Endpoint

Authenticated user:

```http
GET /api/onboarding/me/train
```

Admin/debug lookup:

```http
GET /api/onboarding/{username}/train
```

If no onboarding process exists for the user, the backend returns `404`.

## Response Example

```json
{
  "username": "user@example.com",
  "currentState": "EMAIL_VERIFIED",
  "currentStep": "IDENTITY_CHECK",
  "steps": [
    {
      "key": "REGISTRATION",
      "label": "Registro",
      "status": "COMPLETED"
    },
    {
      "key": "IDENTITY_CHECK",
      "label": "Comprueba tu identidad",
      "status": "CURRENT"
    },
    {
      "key": "PLAN_SELECTION",
      "label": "Elige tu plan",
      "status": "PENDING"
    }
  ]
}
```

## Step Status

```text
COMPLETED
CURRENT
PENDING
```

For `READY_TO_PUBLISH`, every visible train step is returned as `COMPLETED`.

## Current Scope

The current backend implementation only connects:

- user creation -> `USER_REGISTERED` -> `REGISTERED`
- email confirmation -> `EMAIL_VERIFIED` -> `EMAIL_VERIFIED`

KYC, subscription, payment, and profile events are intentionally left for later
iterations.
