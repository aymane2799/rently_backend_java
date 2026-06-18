# KiraDrive API Guide

**Base URL:** `http://localhost:8090/api/v1`  
**Format:** JSON (UTF-8), except file upload endpoints (multipart/form-data)  
**Authentication:** Bearer JWT in `Authorization` header  
**Tenant Routing:** `X-Tenant-ID: <agency-slug>` header required for all tenant-scoped endpoints

---

## Table of Contents

1. [Authentication & Headers](#1-authentication--headers)
2. [Error Handling](#2-error-handling)
- [Pagination, Filtering & Options](#pagination-filtering--options)
3. [Enums Reference](#3-enums-reference)
4. [Auth Module](#4-auth-module)
5. [Agency Registration & Management](#5-agency-registration--management)
6. [Agency Branding & Public Profile](#6-agency-branding--public-profile)
7. [Subscription Plans](#7-subscription-plans)
8. [Subscriptions](#8-subscriptions)
9. [Catalog — Brands](#9-catalog--brands)
10. [Catalog — Models](#10-catalog--models)
11. [Catalog — Features](#11-catalog--features)
12. [Catalog Requests](#12-catalog-requests)
13. [Users](#13-users)
14. [Branches](#14-branches)
15. [Hubs](#15-hubs)
16. [Fleet — Vehicles](#16-fleet--vehicles)
17. [Vehicle Images](#17-vehicle-images)
18. [Customers](#18-customers)
19. [Reservations](#19-reservations)
20. [Payments & Deposits](#20-payments--deposits)
21. [Signatures & Contract Compliance](#21-signatures--contract-compliance)
22. [Document Status](#22-document-status)
23. [Booking Requests (Client-Facing)](#23-booking-requests-client-facing)
24. [Client Auth](#24-client-auth)
25. [Dashboard](#25-dashboard)
26. [Calendar](#26-calendar)

---

## 1. Authentication & Headers

### Standard Headers

| Header | Required | Description |
|---|---|---|
| `Authorization` | Yes (authenticated endpoints) | `Bearer <jwt_token>` |
| `X-Tenant-ID` | Yes (tenant-scoped endpoints) | Agency slug, e.g. `casablanca-cars` |
| `Content-Type` | Yes (request bodies) | `application/json` or `multipart/form-data` |

### JWT Claims

The JWT contains:

```json
{
  "userId": "uuid",
  "role": "AGENCY_OWNER",
  "agencySlug": "casablanca-cars",
  "branchId": "uuid-or-null",
  "sub": "user@example.com",
  "iat": 1718000000,
  "exp": 1718086400
}
```

### Role Hierarchy

| Role | Scope | Notes |
|---|---|---|
| `SUPER_ADMIN` | Platform-wide | No `agencySlug`. Accesses `/api/v1/admin/**`. No `X-Tenant-ID` needed. |
| `AGENCY_OWNER` | Full agency | Has `agencySlug`. No `branchId`. |
| `BRANCH_MANAGER` | One branch | Has `agencySlug` + `branchId`. |
| `AGENT` | One branch | Has `agencySlug` + `branchId`. |

### Public Endpoints (no auth required)

- `POST /auth/login`
- `POST /agencies/register`
- `GET /plans`
- `GET /brands`, `GET /brands/{id}`
- `GET /models`, `GET /models/{id}`
- `GET /features`, `GET /features/{id}`
- `GET /public/{slug}` — agency public profile
- `GET /public/{slug}/vehicles` — available vehicles
- `GET /agencies/{slug}/qr-code`
- `POST /public/{slug}/auth/register`
- `POST /public/{slug}/auth/login`
- `POST /public/{slug}/booking-requests`

---

## 2. Error Handling

All errors return a unified **ErrorResponse** body:

```json
{
  "message": "Agency with slug 'acme' not found",
  "status": 404,
  "timestamp": "2025-06-15T12:00:00Z"
}
```

| HTTP Status | When |
|---|---|
| `400 Bad Request` | Validation failure or illegal argument |
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Role not allowed, or quota exceeded |
| `404 Not Found` | Entity does not exist |
| `409 Conflict` | Duplicate entity or illegal state transition |

**Validation errors (400)** include the field that failed:

```json
{
  "message": "email: must be a valid email address",
  "status": 400,
  "timestamp": "2025-06-15T12:00:00Z"
}
```

---

---

## Pagination, Filtering & Options

### Standard Pagination Parameters

All list endpoints accept these query parameters:

| Parameter | Type | Default | Constraint | Description |
|---|---|---|---|---|
| `page` | `integer` | `0` | ≥ 0 | 0-indexed page number |
| `size` | `integer` | `20` | 1–100 | Items per page |
| `sort` | `string` | `createdAt,desc` | — | `fieldName,direction` e.g. `sort=name,asc`. Repeatable: `sort=status,asc&sort=createdAt,desc` |

### Paginated Response Format (`PagedResponse<T>`)

Every list endpoint returns this envelope:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 84,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

| Field | Type | Description |
|---|---|---|
| `content` | `array` | Items for this page |
| `page` | `integer` | Current page (0-indexed) |
| `size` | `integer` | Items per page as requested |
| `totalElements` | `long` | Total matching records across all pages |
| `totalPages` | `integer` | Total number of pages |
| `first` | `boolean` | Whether this is the first page |
| `last` | `boolean` | Whether this is the last page |

### Options Endpoints (Dropdown / Select Inputs)

Separate lightweight endpoints return minimal DTOs without pagination. Always return only active records. Use these for form selects — **not** the paginated list endpoints.

| Endpoint | Required params | Optional params | Returns | Min search |
|---|---|---|---|---|
| `GET /brands/options` | — | — | `List<BrandOptionResponse>` | — |
| `GET /models/options` | — | `brandId` | `List<ModelOptionResponse>` | — |
| `GET /features/options` | — | — | `List<FeatureOptionResponse>` | — |
| `GET /branches/options` | — | — | `List<BranchOptionResponse>` | — |
| `GET /hubs/options` | `branchId` | — | `List<HubOptionResponse>` | — |
| `GET /customers/options` | `search` | — | `List<CustomerOptionResponse>` | **2 chars** |

Option response shapes:

```json
// BrandOptionResponse
{ "id": "uuid", "name": "Renault" }

// ModelOptionResponse
{ "id": "uuid", "name": "Clio", "category": "ECONOMY" }

// FeatureOptionResponse
{ "id": "uuid", "name": "Air Conditioning", "icon": "snowflake" }

// BranchOptionResponse
{ "id": "uuid", "name": "Agence Maarif", "city": "Casablanca" }

// HubOptionResponse
{ "id": "uuid", "name": "CMN Terminal 1", "type": "AIRPORT" }

// CustomerOptionResponse
{ "id": "uuid", "firstName": "Karim", "lastName": "Fassi", "idNumber": "AB123456", "idType": "CIN" }
```

> `GET /customers/options` returns `400 Bad Request` if `search` is absent or fewer than 2 characters.

---

## 3. Enums Reference

### UserRole
`SUPER_ADMIN` · `AGENCY_OWNER` · `BRANCH_MANAGER` · `AGENT`

### AgencyStatus
`APPROVED` · `BLOCKED`

### AgencyRegistrationStatus
`PENDING` · `APPROVED` · `REJECTED`

### SubscriptionStatus
`PENDING_PAYMENT` · `ACTIVE` · `EXPIRED` · `SUSPENDED`

### VehicleCategory
`ECONOMY` · `COMPACT` · `MIDSIZE` · `SUV` · `LUXURY` · `VAN`

### VehicleStatus
`AVAILABLE` · `RENTED` · `MAINTENANCE` · `PENDING_RELOCATION`

### FuelType
`DIESEL` · `GASOLINE` · `ELECTRIC` · `HYBRID`

### Transmission
`MANUAL` · `AUTOMATIC`

### HubType
`AIRPORT` · `TRAIN_STATION` · `MAIN_OFFICE` · `PRIVATE_LOT`

### ReservationStatus
`ACTIVE` · `CLOSED` · `CANCELLED`

### ContractStatus
`PENDING` · `PARTIAL_EXECUTION` · `FULLY_EXECUTED`

### IdType
`CIN` · `PASSPORT`

### DepositType
`CASH` · `CHEQUE` · `CREDIT_CARD_PREAUTH`

### DepositStatus
`ACTIVE_HOLD` · `RELEASED`

### CatalogRequestType
`BRAND` · `MODEL` · `FEATURE`

### CatalogRequestStatus
`PENDING` · `APPROVED` · `REJECTED`

### BookingRequestStatus
`PENDING_CONFIRMATION` · `CONFIRMED` · `REJECTED`

### CalendarEventType
`RESERVATION` · `BOOKING_REQUEST` · `INSURANCE_EXPIRY`

---

## 4. Auth Module

### Login

```
POST /auth/login
```

**Request:**

```json
{
  "email": "owner@acme.ma",
  "password": "secret123"
}
```

**Response `200 OK`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "role": "AGENCY_OWNER",
  "agencySlug": "casablanca-cars",
  "branchId": null
}
```

| Field | Type | Notes |
|---|---|---|
| `token` | `string` | JWT, include as `Bearer <token>` |
| `userId` | `string (UUID)` | |
| `role` | `UserRole` | |
| `agencySlug` | `string \| null` | `null` for `SUPER_ADMIN` |
| `branchId` | `string (UUID) \| null` | Set for `BRANCH_MANAGER` and `AGENT` |

---

## 5. Agency Registration & Management

### Submit Registration (Public)

```
POST /agencies/register
```

**Request:**

```json
{
  "agencyName": "Casablanca Cars",
  "rcNumber": "RC123456",
  "iceNumber": "001234567890123",
  "ifNumber": "12345678",
  "patent": "P-987",
  "city": "Casablanca",
  "address": "123 Bd Mohammed V",
  "website": "https://casablancacars.ma",
  "ownerFirstName": "Youssef",
  "ownerLastName": "Alami",
  "ownerEmail": "youssef@casablancacars.ma",
  "ownerPhone": "+212600000000"
}
```

| Field | Required | Constraints |
|---|---|---|
| `agencyName` | Yes | notblank |
| `rcNumber` | Yes | notblank, unique |
| `iceNumber` | Yes | notblank, unique |
| `ifNumber` | No | |
| `patent` | No | |
| `city` | Yes | notblank |
| `address` | No | |
| `website` | No | |
| `ownerFirstName` | Yes | notblank |
| `ownerLastName` | Yes | notblank |
| `ownerEmail` | Yes | valid email, unique |
| `ownerPhone` | Yes | notblank |

**Response `201 Created`:** → [AgencyRegistrationResponse](#agencyregistrationresponse)

---

### List Registrations (SUPER_ADMIN)

```
GET /admin/agencies/registrations
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `agencyName` · `city` · `submittedAt` · `reviewedAt` |
| `status` | `AgencyRegistrationStatus` | No | `PENDING` · `APPROVED` · `REJECTED` |
| `city` | `string` | No | Exact match |
| `search` | `string` | No | Case-insensitive on agency name or owner email |

**Response `200 OK`:** `PagedResponse<AgencyRegistrationResponse>`

---

### Get Registration (SUPER_ADMIN)

```
GET /admin/agencies/registrations/{id}
```

**Response `200 OK`:** → [AgencyRegistrationResponse](#agencyregistrationresponse)

---

### Approve Registration (SUPER_ADMIN)

```
POST /admin/agencies/registrations/{id}/approve
```

No request body.  
On approval: creates `Agency`, creates `AGENCY_OWNER` user, provisions tenant schema.

**Response `200 OK`:** → [AgencyRegistrationResponse](#agencyregistrationresponse)

---

### Reject Registration (SUPER_ADMIN)

```
POST /admin/agencies/registrations/{id}/reject
```

**Request:**

```json
{
  "rejectionReason": "Incomplete documentation — missing RC number proof."
}
```

**Response `200 OK`:** → [AgencyRegistrationResponse](#agencyregistrationresponse)

---

### List Agencies (SUPER_ADMIN)

```
GET /admin/agencies
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `name` · `city` · `approvedAt` · `createdAt` |
| `status` | `AgencyStatus` | No | `APPROVED` · `BLOCKED` |
| `city` | `string` | No | Exact match |
| `planId` | `string (UUID)` | No | Filter by subscription plan |
| `search` | `string` | No | Case-insensitive on name, owner first/last name, or email |

**Response `200 OK`:** `PagedResponse<AgencyResponse>`

---

### Get Agency (SUPER_ADMIN)

```
GET /admin/agencies/{id}
```

**Response `200 OK`:** → [AgencyResponse](#agencyresponse)

---

### Block Agency (SUPER_ADMIN)

```
POST /admin/agencies/{id}/block
```

No request body.

**Response `200 OK`:** → [AgencyResponse](#agencyresponse)

---

### Unblock Agency (SUPER_ADMIN)

```
POST /admin/agencies/{id}/unblock
```

No request body.

**Response `200 OK`:** → [AgencyResponse](#agencyresponse)

---

### AgencyRegistrationResponse

```json
{
  "id": "uuid",
  "agencyName": "Casablanca Cars",
  "rcNumber": "RC123456",
  "iceNumber": "001234567890123",
  "ifNumber": "12345678",
  "patent": "P-987",
  "city": "Casablanca",
  "address": "123 Bd Mohammed V",
  "website": "https://casablancacars.ma",
  "ownerFirstName": "Youssef",
  "ownerLastName": "Alami",
  "ownerEmail": "youssef@casablancacars.ma",
  "ownerPhone": "+212600000000",
  "status": "PENDING",
  "rejectionReason": null,
  "submittedAt": "2025-06-15T10:00:00Z",
  "reviewedAt": null,
  "reviewedBy": null,
  "resolvedAgencyId": null
}
```

### AgencyResponse

```json
{
  "id": "uuid",
  "name": "Casablanca Cars",
  "slug": "casablanca-cars",
  "rcNumber": "RC123456",
  "iceNumber": "001234567890123",
  "ifNumber": "12345678",
  "patent": "P-987",
  "ownerFirstName": "Youssef",
  "ownerLastName": "Alami",
  "phone": "+212600000000",
  "email": "youssef@casablancacars.ma",
  "city": "Casablanca",
  "address": "123 Bd Mohammed V",
  "website": "https://casablancacars.ma",
  "logo": "https://cdn.example.com/logo.png",
  "coverImage": "https://cdn.example.com/cover.jpg",
  "tagline": "Drive in style",
  "primaryColor": "#1A73E8",
  "secondaryColor": "#FFFFFF",
  "darkPrimaryColor": "#0D47A1",
  "darkSecondaryColor": "#121212",
  "metaTitle": "Casablanca Cars — Premium Rentals",
  "metaDescription": "Rent premium vehicles in Casablanca...",
  "metaKeywords": "car rental, Casablanca, Morocco",
  "ogImageUrl": "https://cdn.example.com/og.jpg",
  "status": "APPROVED",
  "approvedAt": "2025-06-10T08:00:00Z",
  "planId": "uuid"
}
```

---

## 6. Agency Branding & Public Profile

### Get Branding (AGENCY_OWNER)

```
GET /settings/agency/branding
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:**

```json
{
  "tagline": "Drive in style",
  "primaryColor": "#1A73E8",
  "secondaryColor": "#FFFFFF",
  "darkPrimaryColor": "#0D47A1",
  "darkSecondaryColor": "#121212",
  "metaTitle": "Casablanca Cars — Premium Rentals",
  "metaDescription": "Rent premium vehicles in Casablanca...",
  "metaKeywords": "car rental, Casablanca, Morocco",
  "ogImageUrl": "https://cdn.example.com/og.jpg"
}
```

---

### Update Branding (AGENCY_OWNER)

```
PATCH /settings/agency/branding
Headers: X-Tenant-ID: casablanca-cars
```

**Request:** (all fields optional)

```json
{
  "tagline": "Morocco's finest fleet",
  "primaryColor": "#E53935",
  "secondaryColor": "#FFFFFF",
  "darkPrimaryColor": "#B71C1C",
  "darkSecondaryColor": "#121212",
  "metaTitle": "Casablanca Cars",
  "metaDescription": "Rent vehicles in Casablanca",
  "metaKeywords": "car, rental, Morocco",
  "ogImageUrl": "https://cdn.example.com/og-new.jpg"
}
```

| Field | Max Length |
|---|---|
| `tagline` | 200 |
| `primaryColor` | 7 (hex, e.g. `#1A73E8`) |
| `secondaryColor` | 7 |
| `darkPrimaryColor` | 7 |
| `darkSecondaryColor` | 7 |
| `metaTitle` | 70 |
| `metaDescription` | 160 |

**Response `204 No Content`**

---

### Get Public Agency Profile (Public)

```
GET /public/{slug}
```

**Response `200 OK`:**

```json
{
  "id": "uuid",
  "name": "Casablanca Cars",
  "slug": "casablanca-cars",
  "logo": "https://cdn.example.com/logo.png",
  "coverImage": "https://cdn.example.com/cover.jpg",
  "tagline": "Drive in style",
  "primaryColor": "#1A73E8",
  "secondaryColor": "#FFFFFF",
  "darkPrimaryColor": "#0D47A1",
  "darkSecondaryColor": "#121212",
  "metaTitle": "Casablanca Cars",
  "metaDescription": "Rent premium vehicles...",
  "metaKeywords": "car rental, Casablanca",
  "ogImageUrl": "https://cdn.example.com/og.jpg",
  "city": "Casablanca",
  "website": "https://casablancacars.ma",
  "phone": "+212600000000"
}
```

---

### Get Available Vehicles (Public)

```
GET /public/{slug}/vehicles?from=2025-07-01&to=2025-07-07&category=SUV&transmission=AUTOMATIC
```

| Query Param | Required | Type |
|---|---|---|
| `from` | No | `date` (ISO 8601, e.g. `2025-07-01`) |
| `to` | No | `date` |
| `category` | No | `VehicleCategory` |
| `transmission` | No | `Transmission` |

**Response `200 OK`:** `Array<PublicVehicleResponse>`

```json
[
  {
    "id": "uuid",
    "modelName": "Clio",
    "brandName": "Renault",
    "category": "ECONOMY",
    "transmission": "MANUAL",
    "fuelType": "DIESEL",
    "dailyBaseRate": 250.00,
    "seats": 5,
    "year": 2022,
    "color": "White",
    "features": ["Air Conditioning", "Bluetooth"],
    "primaryImageUrl": "https://cdn.example.com/vehicle.jpg"
  }
]
```

---

### Get Agency QR Code (Public)

```
GET /agencies/{slug}/qr-code
```

**Response `200 OK`:** PNG binary image (`Content-Type: image/png`)

---

## 7. Subscription Plans

### List Plans (Public)

```
GET /plans
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `code` · `priceMonthly` · `createdAt` |

Returns only active plans.

**Response `200 OK`:** `PagedResponse<SubscriptionPlanResponse>`

---

### List Plans — Admin (SUPER_ADMIN)

```
GET /admin/plans
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `code` · `priceMonthly` · `createdAt` |
| `active` | `boolean` | No | Filter by active status |

**Response `200 OK`:** `PagedResponse<SubscriptionPlanResponse>`

---

### Create Plan (SUPER_ADMIN)

```
POST /admin/plans
```

**Request:**

```json
{
  "code": "SAFI",
  "displayName": "Safi Plan",
  "description": "Best for small agencies",
  "priceMonthly": 299.00,
  "priceYearly": 2990.00,
  "maxBranches": 1,
  "maxHubs": 1,
  "maxVehicles": 15
}
```

| Field | Required | Notes |
|---|---|---|
| `code` | Yes | Unique, max 50 chars |
| `displayName` | Yes | Max 100 chars |
| `description` | No | |
| `priceMonthly` | Yes | Decimal |
| `priceYearly` | Yes | Decimal |
| `maxBranches` | No | `null` = unlimited |
| `maxHubs` | No | `null` = unlimited |
| `maxVehicles` | No | `null` = unlimited |

**Response `201 Created`:** → [SubscriptionPlanResponse](#subscriptionplanresponse)

---

### Update Plan (SUPER_ADMIN)

```
PATCH /admin/plans/{id}
```

**Request:** (all fields optional)

```json
{
  "displayName": "Safi Plus",
  "priceMonthly": 349.00,
  "maxVehicles": 20
}
```

**Response `204 No Content`**

---

### Deactivate Plan (SUPER_ADMIN)

```
POST /admin/plans/{id}/deactivate
```

No request body. **Response `204 No Content`**

---

### SubscriptionPlanResponse

```json
{
  "id": "uuid",
  "code": "SAFI",
  "displayName": "Safi Plan",
  "description": "Best for small agencies",
  "priceMonthly": 299.00,
  "priceYearly": 2990.00,
  "maxBranches": 1,
  "maxHubs": 1,
  "maxVehicles": 15,
  "active": true
}
```

---

## 8. Subscriptions

### Create Subscription (SUPER_ADMIN)

```
POST /admin/subscriptions
```

**Request:**

```json
{
  "agencySlug": "casablanca-cars",
  "planId": "uuid-of-plan",
  "startDate": "2025-07-01",
  "endDate": "2025-12-31",
  "amountDue": 1500.00
}
```

**Response `201 Created`:** → [SubscriptionResponse](#subscriptionresponse)

---

### List Subscriptions (SUPER_ADMIN)

```
GET /admin/subscriptions
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `startDate` · `endDate` · `amountDue` · `createdAt` |
| `status` | `SubscriptionStatus` | No | `PENDING_PAYMENT` · `ACTIVE` · `EXPIRED` · `SUSPENDED` |
| `agencySlug` | `string` | No | Exact match |
| `planId` | `string (UUID)` | No | Filter by plan |
| `startDateFrom` | `date` | No | ISO 8601 — subscription start on or after this date |
| `startDateTo` | `date` | No | ISO 8601 — subscription start on or before this date |

**Response `200 OK`:** `PagedResponse<SubscriptionResponse>`

---

### Mark Subscription Paid (SUPER_ADMIN)

```
POST /admin/subscriptions/{id}/mark-paid
```

**Request:**

```json
{
  "paymentMode": "BANK_TRANSFER"
}
```

**Response `200 OK`:** → [SubscriptionResponse](#subscriptionresponse)

---

### Get My Subscription (AGENCY_OWNER)

```
GET /settings/subscription
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:** → [SubscriptionResponse](#subscriptionresponse)

---

### SubscriptionResponse

```json
{
  "id": "uuid",
  "agencySlug": "casablanca-cars",
  "plan": {
    "id": "uuid",
    "code": "SAFI",
    "displayName": "Safi Plan",
    "description": "Best for small agencies",
    "priceMonthly": 299.00,
    "priceYearly": 2990.00,
    "maxBranches": 1,
    "maxHubs": 1,
    "maxVehicles": 15,
    "active": true
  },
  "status": "ACTIVE",
  "startDate": "2025-07-01",
  "endDate": "2025-12-31",
  "amountDue": 1500.00,
  "paymentMode": "BANK_TRANSFER",
  "paidAt": "2025-06-30T09:00:00Z",
  "invoiceUrl": "https://storage/invoices/sub-uuid.pdf"
}
```

---

## 9. Catalog — Brands

### List Brands (Public)

```
GET /brands
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `name` · `createdAt` |
| `active` | `boolean` | No | Default `true` |
| `search` | `string` | No | Case-insensitive search on name |

**Response `200 OK`:** `PagedResponse<BrandResponse>`

---

### List Brands — Options (Public)

```
GET /brands/options
```

No pagination. Returns all active brands.

**Response `200 OK`:** `Array<BrandOptionResponse>`

```json
[
  { "id": "uuid", "name": "Renault" },
  { "id": "uuid", "name": "Toyota" }
]
```

---

### Get Brand (Public)

```
GET /brands/{id}
```

**Response `200 OK`:** → [BrandResponse](#brandresponse)

---

### Create Brand (SUPER_ADMIN)

```
POST /admin/brands
```

**Request:**

```json
{
  "name": "Renault"
}
```

| Field | Required | Constraints |
|---|---|---|
| `name` | Yes | notblank, min 3 chars, unique |

**Response `201 Created`:** → [BrandResponse](#brandresponse)

---

### Update Brand (SUPER_ADMIN)

```
PATCH /admin/brands/{id}
```

**Request:**

```json
{
  "name": "Renault Group"
}
```

**Response `204 No Content`**

---

### Deactivate Brand (SUPER_ADMIN)

```
POST /admin/brands/{id}/deactivate
```

No request body. **Response `204 No Content`**  
> Soft delete only — preserves cross-schema UUID references.

---

### BrandResponse

```json
{
  "id": "uuid",
  "name": "Renault",
  "isActive": true
}
```

---

## 10. Catalog — Models

### List Models (Public)

```
GET /models
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `name` · `category` · `createdAt` |
| `active` | `boolean` | No | Default `true` |
| `brandId` | `string (UUID)` | No | Filter by brand |
| `category` | `VehicleCategory` | No | Filter by vehicle category |
| `search` | `string` | No | Case-insensitive search on name |

**Response `200 OK`:** `PagedResponse<ModelResponse>`

---

### List Models — Options (Public)

```
GET /models/options?brandId={uuid}
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `brandId` | `string (UUID)` | No | Restrict to models of a specific brand |

No pagination. Returns all active models (optionally filtered by brand).

**Response `200 OK`:** `Array<ModelOptionResponse>`

```json
[
  { "id": "uuid", "name": "Clio", "category": "ECONOMY" },
  { "id": "uuid", "name": "Megane", "category": "COMPACT" }
]
```

---

### Get Model (Public)

```
GET /models/{id}
```

**Response `200 OK`:** → [ModelResponse](#modelresponse)

---

### Create Model (SUPER_ADMIN)

```
POST /admin/models
```

**Request:**

```json
{
  "name": "Clio",
  "category": "ECONOMY",
  "brandId": "uuid-of-brand"
}
```

| Field | Required | Constraints |
|---|---|---|
| `name` | Yes | notblank |
| `category` | Yes | `VehicleCategory` enum |
| `brandId` | Yes | UUID, must exist and be active |

**Response `201 Created`:** → [ModelResponse](#modelresponse)

---

### Update Model (SUPER_ADMIN)

```
PATCH /admin/models/{id}
```

**Request:** (all fields optional)

```json
{
  "name": "Clio V",
  "category": "COMPACT",
  "brandId": "uuid-of-brand"
}
```

**Response `204 No Content`**

---

### Deactivate Model (SUPER_ADMIN)

```
POST /admin/models/{id}/deactivate
```

No request body. **Response `204 No Content`**

---

### ModelResponse

```json
{
  "id": "uuid",
  "name": "Clio",
  "category": "ECONOMY",
  "brand": {
    "id": "uuid",
    "name": "Renault",
    "isActive": true
  },
  "isActive": true
}
```

---

## 11. Catalog — Features

### List Features (Public)

```
GET /features
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `name` · `createdAt` |
| `active` | `boolean` | No | Default `true` |
| `search` | `string` | No | Case-insensitive search on name |

**Response `200 OK`:** `PagedResponse<FeatureResponse>`

---

### List Features — Options (Public)

```
GET /features/options
```

No pagination. Returns all active features.

**Response `200 OK`:** `Array<FeatureOptionResponse>`

```json
[
  { "id": "uuid", "name": "Air Conditioning", "icon": "snowflake" },
  { "id": "uuid", "name": "Bluetooth", "icon": "bluetooth" }
]
```

---

### Get Feature (Public)

```
GET /features/{id}
```

**Response `200 OK`:** → [FeatureResponse](#featureresponse)

---

### Create Feature (SUPER_ADMIN)

```
POST /admin/features
```

**Request:**

```json
{
  "name": "Air Conditioning",
  "icon": "snowflake",
  "description": "Full climate control system"
}
```

| Field | Required | Constraints |
|---|---|---|
| `name` | Yes | notblank, min 3 chars, unique |
| `icon` | No | Icon identifier (e.g. Font Awesome name) |
| `description` | No | |

**Response `201 Created`:** → [FeatureResponse](#featureresponse)

---

### Update Feature (SUPER_ADMIN)

```
PATCH /admin/features/{id}
```

**Request:** (all fields optional)

```json
{
  "name": "A/C",
  "icon": "wind",
  "description": "Air conditioning"
}
```

**Response `204 No Content`**

---

### Deactivate Feature (SUPER_ADMIN)

```
POST /admin/features/{id}/deactivate
```

No request body. **Response `204 No Content`**

---

### FeatureResponse

```json
{
  "id": "uuid",
  "name": "Air Conditioning",
  "icon": "snowflake",
  "description": "Full climate control system",
  "isActive": true
}
```

---

## 12. Catalog Requests

Agencies can propose new brands, models, or features that don't yet exist in the catalog. A `SUPER_ADMIN` then approves or rejects the request, creating the catalog entry if approved.

### Submit Catalog Request (AGENCY_OWNER, BRANCH_MANAGER)

```
POST /catalog-requests
Headers: X-Tenant-ID: casablanca-cars
```

**Request examples by type:**

**Type: BRAND**

```json
{
  "type": "BRAND",
  "proposedName": "MG Motors",
  "notes": "Increasingly popular in Morocco"
}
```

**Type: MODEL**

```json
{
  "type": "MODEL",
  "proposedName": "ZS EV",
  "proposedBrandId": "uuid-of-mg-brand",
  "proposedBrandName": "MG Motors",
  "proposedCategory": "SUV",
  "notes": "Electric SUV"
}
```

**Type: FEATURE**

```json
{
  "type": "FEATURE",
  "proposedName": "360° Camera",
  "proposedIcon": "camera",
  "proposedDescription": "Full surround-view parking camera",
  "notes": "Needed for our luxury fleet"
}
```

| Field | Required | Notes |
|---|---|---|
| `type` | Yes | `CatalogRequestType` |
| `proposedName` | Yes | notblank |
| `proposedBrandId` | No | UUID; for MODEL type with existing brand |
| `proposedBrandName` | No | For MODEL type with new brand |
| `proposedCategory` | No | `VehicleCategory`; for MODEL type |
| `proposedIcon` | No | For FEATURE type |
| `proposedDescription` | No | For FEATURE type |
| `notes` | No | Additional context |

**Response `201 Created`:** → [CatalogRequestResponse](#catalogrequestresponse)

---

### List My Catalog Requests (AGENCY_OWNER)

```
GET /catalog-requests
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `submittedAt` · `type` · `status` |
| `type` | `CatalogRequestType` | No | `BRAND` · `MODEL` · `FEATURE` |
| `status` | `CatalogRequestStatus` | No | `PENDING` · `APPROVED` · `REJECTED` |

**Response `200 OK`:** `PagedResponse<CatalogRequestResponse>`

---

### List All Catalog Requests (SUPER_ADMIN)

```
GET /admin/catalog-requests
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `submittedAt` · `reviewedAt` · `type` · `status` |
| `type` | `CatalogRequestType` | No | `BRAND` · `MODEL` · `FEATURE` |
| `status` | `CatalogRequestStatus` | No | `PENDING` · `APPROVED` · `REJECTED` |

**Response `200 OK`:** `PagedResponse<CatalogRequestResponse>`

---

### Approve Catalog Request (SUPER_ADMIN)

```
POST /admin/catalog-requests/{id}/approve
```

No request body. Creates the catalog entity (Brand/Model/Feature).

**Response `204 No Content`**

---

### Reject Catalog Request (SUPER_ADMIN)

```
POST /admin/catalog-requests/{id}/reject
```

**Request:**

```json
{
  "rejectionReason": "This brand is already in the catalog under a different name."
}
```

**Response `204 No Content`**

---

### CatalogRequestResponse

```json
{
  "id": "uuid",
  "agencySlug": "casablanca-cars",
  "type": "MODEL",
  "status": "PENDING",
  "proposedName": "ZS EV",
  "proposedBrandId": "uuid",
  "proposedBrandName": "MG Motors",
  "proposedCategory": "SUV",
  "proposedIcon": null,
  "proposedDescription": null,
  "notes": "Electric SUV",
  "rejectionReason": null,
  "submittedAt": "2025-06-15T09:00:00Z",
  "reviewedAt": null,
  "reviewedBy": null,
  "resolvedEntityId": null
}
```

---

## 13. Users

All user management is tenant-scoped and restricted to `AGENCY_OWNER`.

### List Users (AGENCY_OWNER)

```
GET /users
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `firstName` · `lastName` · `role` · `createdAt` |
| `role` | `UserRole` | No | `BRANCH_MANAGER` · `AGENT` |
| `branchId` | `string (UUID)` | No | Filter by branch |
| `active` | `boolean` | No | Filter by active status |
| `search` | `string` | No | Case-insensitive on first name, last name, or email |

**Response `200 OK`:** `PagedResponse<UserResponse>`

---

### Register User (AGENCY_OWNER)

```
POST /users
Headers: X-Tenant-ID: casablanca-cars
```

**Request:**

```json
{
  "firstName": "Sara",
  "lastName": "Benali",
  "email": "sara@casablancacars.ma",
  "password": "securePass123",
  "role": "BRANCH_MANAGER",
  "branchId": "uuid-of-branch"
}
```

| Field | Required | Notes |
|---|---|---|
| `firstName` | Yes | notblank |
| `lastName` | Yes | notblank |
| `email` | Yes | valid email, unique |
| `password` | Yes | notblank |
| `role` | Yes | `BRANCH_MANAGER` or `AGENT` (cannot create `AGENCY_OWNER`) |
| `branchId` | Conditional | Required for `BRANCH_MANAGER` and `AGENT` |

**Response `201 Created`:** → [UserResponse](#userresponse)

---

### Update User (AGENCY_OWNER)

```
PATCH /users/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Request:** (all fields optional)

```json
{
  "firstName": "Sara",
  "lastName": "El Benali",
  "password": "newPass456",
  "branchId": "uuid-of-new-branch",
  "active": true
}
```

**Response `204 No Content`**

---

### Delete User (AGENCY_OWNER)

```
DELETE /users/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Response `204 No Content`**

---

### UserResponse

```json
{
  "id": "uuid",
  "firstName": "Sara",
  "lastName": "Benali",
  "email": "sara@casablancacars.ma",
  "role": "BRANCH_MANAGER",
  "agencySlug": "casablanca-cars",
  "branchId": "uuid-of-branch",
  "is_active": true,
  "createdAt": "2025-06-15T09:00:00Z"
}
```

> Note: The `isActive` field is serialized as `is_active` (snake_case) due to `@JsonProperty("is_active")`.

---

## 14. Branches

Branches are tenant-scoped and subject to quota enforcement.

### List Branches

```
GET /branches
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `name` · `city` · `createdAt` |
| `active` | `boolean` | No | Default `true` |
| `city` | `string` | No | Exact match |
| `search` | `string` | No | Case-insensitive search on name |

**Response `200 OK`:** `PagedResponse<BranchResponse>`

---

### List Branches — Options

```
GET /branches/options
Headers: X-Tenant-ID: casablanca-cars
```

No pagination. Returns all active branches.

**Response `200 OK`:** `Array<BranchOptionResponse>`

```json
[
  { "id": "uuid", "name": "Agence Maarif", "city": "Casablanca" },
  { "id": "uuid", "name": "Agence Guéliz", "city": "Marrakech" }
]
```

---

### Get Branch

```
GET /branches/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:** → [BranchResponse](#branchresponse)

---

### Create Branch (AGENCY_OWNER, BRANCH_MANAGER)

```
POST /branches
Headers: X-Tenant-ID: casablanca-cars
```

**Request:**

```json
{
  "name": "Agence Maarif",
  "city": "Casablanca",
  "address": "45 Rue Bnou Tahir, Maarif",
  "phone": "+212522000001"
}
```

| Field | Required | Constraints |
|---|---|---|
| `name` | Yes | notblank, max 150, unique per agency |
| `city` | Yes | notblank |
| `address` | No | |
| `phone` | No | |

> Returns `403 Forbidden` if branch quota exceeded.

**Response `201 Created`:** → [BranchResponse](#branchresponse)

---

### Update Branch (AGENCY_OWNER, BRANCH_MANAGER)

```
PATCH /branches/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Request:** (all fields optional)

```json
{
  "name": "Agence Maarif Centre",
  "phone": "+212522000002"
}
```

**Response `204 No Content`**

---

### Deactivate Branch (AGENCY_OWNER)

```
POST /branches/{id}/deactivate
Headers: X-Tenant-ID: casablanca-cars
```

No request body. **Response `204 No Content`**

---

### BranchResponse

```json
{
  "id": "uuid",
  "name": "Agence Maarif",
  "city": "Casablanca",
  "address": "45 Rue Bnou Tahir, Maarif",
  "phone": "+212522000001",
  "isActive": true,
  "createdAt": "2025-06-01T08:00:00Z",
  "updatedAt": "2025-06-01T08:00:00Z"
}
```

---

## 15. Hubs

Hubs are pickup/return points inside a branch. Subject to hub quota.

### List Hubs in Branch

```
GET /branches/{branchId}/hubs
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `name` · `type` · `createdAt` |
| `active` | `boolean` | No | Default `true` |
| `type` | `HubType` | No | `AIRPORT` · `TRAIN_STATION` · `MAIN_OFFICE` · `PRIVATE_LOT` |

**Response `200 OK`:** `PagedResponse<HubResponse>`

---

### List Hubs — Options

```
GET /hubs/options?branchId={uuid}
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `branchId` | `string (UUID)` | **Yes** | Branch to list hubs for |

No pagination. Returns all active hubs for the given branch.

**Response `200 OK`:** `Array<HubOptionResponse>`

```json
[
  { "id": "uuid", "name": "CMN Terminal 1", "type": "AIRPORT" },
  { "id": "uuid", "name": "Main Office", "type": "MAIN_OFFICE" }
]
```

---

### Create Hub (AGENCY_OWNER, BRANCH_MANAGER)

```
POST /branches/{branchId}/hubs
Headers: X-Tenant-ID: casablanca-cars
```

**Request:**

```json
{
  "name": "Aéroport CMN Terminal 1",
  "type": "AIRPORT",
  "city": "Casablanca",
  "address": "Route de Nouasseur, Casablanca"
}
```

| Field | Required | Constraints |
|---|---|---|
| `name` | Yes | notblank, max 150 |
| `type` | Yes | `HubType` enum |
| `city` | Yes | notblank |
| `address` | No | |

> Returns `403 Forbidden` if hub quota exceeded.

**Response `201 Created`:** → [HubResponse](#hubresponse)

---

### Update Hub (AGENCY_OWNER, BRANCH_MANAGER)

```
PATCH /hubs/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Request:** (all fields optional)

```json
{
  "name": "CMN Terminal 2",
  "type": "AIRPORT",
  "city": "Casablanca",
  "address": "Terminal 2, Aéroport Mohamed V"
}
```

**Response `204 No Content`**

---

### Deactivate Hub (AGENCY_OWNER, BRANCH_MANAGER)

```
POST /hubs/{id}/deactivate
Headers: X-Tenant-ID: casablanca-cars
```

No request body. **Response `204 No Content`**

---

### HubResponse

```json
{
  "id": "uuid",
  "name": "Aéroport CMN Terminal 1",
  "type": "AIRPORT",
  "city": "Casablanca",
  "address": "Route de Nouasseur",
  "isActive": true,
  "branch": {
    "id": "uuid",
    "name": "Agence Maarif",
    "city": "Casablanca",
    "address": "45 Rue Bnou Tahir",
    "phone": "+212522000001",
    "isActive": true,
    "createdAt": "2025-06-01T08:00:00Z",
    "updatedAt": "2025-06-01T08:00:00Z"
  },
  "createdAt": "2025-06-01T09:00:00Z",
  "updatedAt": "2025-06-01T09:00:00Z"
}
```

---

## 16. Fleet — Vehicles

Vehicle CRUD is tenant-scoped and subject to vehicle quota.

### List Vehicles

```
GET /vehicles
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `licensePlate` · `status` · `mileage` · `dailyBaseRate` · `createdAt` |
| `status` | `VehicleStatus` | No | `AVAILABLE` · `RENTED` · `MAINTENANCE` · `PENDING_RELOCATION` |
| `transmission` | `Transmission` | No | `MANUAL` · `AUTOMATIC` |
| `fuelType` | `FuelType` | No | `DIESEL` · `GASOLINE` · `ELECTRIC` · `HYBRID` |
| `hubId` | `string (UUID)` | No | Filter by current hub |
| `search` | `string` | No | Case-insensitive search on license plate |

**Response `200 OK`:** `PagedResponse<VehicleResponse>`

---

### Get Vehicle

```
GET /vehicles/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:** → [VehicleResponse](#vehicleresponse)

---

### Create Vehicle (tenant users)

```
POST /vehicles
Headers: X-Tenant-ID: casablanca-cars
```

**Request:**

```json
{
  "licensePlate": "12345-A-1",
  "insuranceNumber": "INS-2025-001",
  "insuranceExpiresAt": "2026-06-30",
  "year": 2023,
  "month": 5,
  "color": "Gris Platine",
  "seats": 5,
  "doors": 4,
  "description": "Well maintained, non-smoking",
  "status": "AVAILABLE",
  "transmission": "AUTOMATIC",
  "fuelType": "DIESEL",
  "dailyBaseRate": 350.00,
  "modelId": "uuid-of-clio-model",
  "featureIds": [
    "uuid-of-ac-feature",
    "uuid-of-bluetooth-feature"
  ],
  "currentHubId": "uuid-of-hub",
  "currentParkingSlot": "A-12"
}
```

| Field | Required | Constraints |
|---|---|---|
| `licensePlate` | Yes | notblank, unique per agency |
| `insuranceNumber` | Yes | notblank, unique per agency |
| `insuranceExpiresAt` | Yes | date |
| `year` | No | smallint (e.g. 2023) |
| `month` | No | smallint 1–12 |
| `color` | No | |
| `seats` | No | smallint |
| `doors` | No | smallint |
| `description` | No | |
| `status` | No | `VehicleStatus`; defaults to `AVAILABLE` |
| `transmission` | No | `Transmission` |
| `fuelType` | No | `FuelType` |
| `dailyBaseRate` | No | Decimal |
| `modelId` | Yes | UUID; must exist in public catalog |
| `featureIds` | No | Set of UUIDs; all must exist in public catalog |
| `currentHubId` | No | UUID of a hub in this tenant |
| `currentParkingSlot` | No | max 100 chars |

> Returns `403 Forbidden` if vehicle quota exceeded.

**Response `201 Created`:** → [VehicleResponse](#vehicleresponse)

---

### Update Vehicle (tenant users)

```
PATCH /vehicles/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Request:** (all fields optional)

```json
{
  "color": "Noir",
  "dailyBaseRate": 380.00,
  "status": "MAINTENANCE",
  "mileage": 42000,
  "currentHubId": "uuid-of-new-hub",
  "currentParkingSlot": "B-3"
}
```

**Response `204 No Content`**

---

### Delete Vehicle (tenant users)

```
DELETE /vehicles/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Response `204 No Content`**

---

### VehicleResponse

```json
{
  "id": "uuid",
  "licensePlate": "12345-A-1",
  "insuranceNumber": "INS-2025-001",
  "insuranceExpiresAt": "2026-06-30",
  "year": 2023,
  "month": 5,
  "color": "Gris Platine",
  "mileage": 15000,
  "seats": 5,
  "doors": 4,
  "description": "Well maintained, non-smoking",
  "status": "AVAILABLE",
  "transmission": "AUTOMATIC",
  "fuelType": "DIESEL",
  "dailyBaseRate": 350.00,
  "model": {
    "id": "uuid",
    "name": "Clio",
    "category": "ECONOMY",
    "brand": {
      "id": "uuid",
      "name": "Renault",
      "isActive": true
    },
    "isActive": true
  },
  "features": [
    {
      "id": "uuid",
      "name": "Air Conditioning",
      "icon": "snowflake",
      "description": "Full climate control",
      "isActive": true
    }
  ],
  "currentHubId": "uuid-of-hub",
  "currentParkingSlot": "A-12",
  "images": [
    {
      "id": "uuid",
      "imageUrl": "https://cdn.example.com/car1.jpg",
      "isPrimary": true,
      "displayOrder": 0,
      "altText": "Front view"
    }
  ]
}
```

---

## 17. Vehicle Images

### Upload Image (AGENCY_OWNER, BRANCH_MANAGER)

```
POST /vehicles/{vehicleId}/images
Headers: X-Tenant-ID: casablanca-cars
Content-Type: multipart/form-data
```

**Form fields:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `file` | binary | Yes | Image file (JPEG, PNG, etc.) |
| `altText` | string | No | Alt text for accessibility |

**Response `201 Created`:**

```json
{
  "id": "uuid",
  "imageUrl": "https://cdn.example.com/vehicles/12345.jpg",
  "isPrimary": false,
  "displayOrder": 3,
  "altText": "Side view"
}
```

---

### Delete Image (AGENCY_OWNER, BRANCH_MANAGER)

```
DELETE /vehicles/{vehicleId}/images/{imageId}
Headers: X-Tenant-ID: casablanca-cars
```

**Response `204 No Content`**

---

### Set Primary Image (AGENCY_OWNER, BRANCH_MANAGER)

```
PATCH /vehicles/{vehicleId}/images/{imageId}/set-primary
Headers: X-Tenant-ID: casablanca-cars
```

No request body. **Response `204 No Content`**

---

### Reorder Images (AGENCY_OWNER, BRANCH_MANAGER)

```
PATCH /vehicles/{vehicleId}/images/reorder
Headers: X-Tenant-ID: casablanca-cars
```

**Request:** Array of image IDs in desired order:

```json
["uuid-image-3", "uuid-image-1", "uuid-image-2"]
```

**Response `204 No Content`**

---

## 18. Customers

### List Customers

```
GET /customers
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `firstName` · `lastName` · `createdAt` |
| `idType` | `IdType` | No | `CIN` · `PASSPORT` |
| `search` | `string` | No | Case-insensitive on first name, last name, phone, email, or id number |

**Response `200 OK`:** `PagedResponse<CustomerResponse>`

---

### List Customers — Options (Type-ahead)

```
GET /customers/options?search=ka
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `search` | `string` | **Yes** | Min 2 characters. Searches first name, last name, and id number. |

Returns `400 Bad Request` if `search` is absent or shorter than 2 characters.

**Response `200 OK`:** `Array<CustomerOptionResponse>`

```json
[
  { "id": "uuid", "firstName": "Karim", "lastName": "Fassi", "idNumber": "AB123456", "idType": "CIN" },
  { "id": "uuid", "firstName": "Karima", "lastName": "Alaoui", "idNumber": "CD789012", "idType": "CIN" }
]
```

---

### Get Customer

```
GET /customers/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:** → [CustomerResponse](#customerresponse)

---

### Create Customer (tenant users)

```
POST /customers
Headers: X-Tenant-ID: casablanca-cars
```

**Request:**

```json
{
  "firstName": "Karim",
  "lastName": "Fassi",
  "phone": "+212612345678",
  "email": "karim.fassi@email.com",
  "idType": "CIN",
  "idNumber": "AB123456",
  "driverLicenseCode": "DL-MA-2018-001234",
  "address": "12 Rue des Roses, Rabat"
}
```

| Field | Required | Constraints |
|---|---|---|
| `firstName` | Yes | notblank |
| `lastName` | Yes | notblank |
| `phone` | Yes | notblank |
| `email` | No | |
| `idType` | Yes | `IdType` enum |
| `idNumber` | Yes | notblank; unique per `idType` |
| `driverLicenseCode` | Yes | notblank |
| `address` | No | |

**Response `201 Created`:** → [CustomerResponse](#customerresponse)

---

### Update Customer (tenant users)

```
PATCH /customers/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Request:** (all fields optional)

```json
{
  "phone": "+212612345679",
  "address": "New Address, Casablanca"
}
```

**Response `204 No Content`**

---

### CustomerResponse

```json
{
  "id": "uuid",
  "firstName": "Karim",
  "lastName": "Fassi",
  "phone": "+212612345678",
  "email": "karim.fassi@email.com",
  "idType": "CIN",
  "idNumber": "AB123456",
  "driverLicenseCode": "DL-MA-2018-001234",
  "address": "12 Rue des Roses, Rabat",
  "createdAt": "2025-06-01T10:00:00Z",
  "updatedAt": "2025-06-01T10:00:00Z"
}
```

---

## 19. Reservations

### Create Reservation (AGENT, BRANCH_MANAGER)

```
POST /reservations
Headers: X-Tenant-ID: casablanca-cars
```

**Request:**

```json
{
  "customerId": "uuid-of-customer",
  "vehicleId": "uuid-of-vehicle",
  "pickupHubId": "uuid-of-pickup-hub",
  "returnHubId": "uuid-of-return-hub",
  "startDate": "2025-07-01",
  "endDate": "2025-07-07",
  "totalAmount": 2450.00,
  "payment": {
    "cashAdvanced": 500.00,
    "bankTransferReference": null,
    "bankTransferImageUrl": null,
    "depositType": "CHEQUE",
    "depositAmount": 5000.00,
    "chequeNumber": "CHQ-001234",
    "creditCardAuthReference": null
  }
}
```

| Field | Required | Notes |
|---|---|---|
| `customerId` | Yes | UUID, must exist |
| `vehicleId` | Yes | UUID, must be `AVAILABLE` |
| `pickupHubId` | Yes | UUID, must be active |
| `returnHubId` | Yes | UUID, must be active |
| `startDate` | Yes | ISO date |
| `endDate` | Yes | ISO date, must be after `startDate` |
| `totalAmount` | Yes | Decimal, positive |
| `payment` | Yes | See [CreatePaymentRequest](#createpaymentrequest) |

**CreatePaymentRequest:**

| Field | Required | Notes |
|---|---|---|
| `cashAdvanced` | Yes | Decimal ≥ 0 |
| `bankTransferReference` | No | Reference number |
| `bankTransferImageUrl` | No | URL to transfer proof image |
| `depositType` | Yes | `DepositType` enum |
| `depositAmount` | Yes | Decimal ≥ 0 |
| `chequeNumber` | Conditional | Required if `depositType = CHEQUE` |
| `creditCardAuthReference` | Conditional | Required if `depositType = CREDIT_CARD_PREAUTH` |

> On creation: vehicle status → `RENTED`, async PDF generation triggered.

**Response `201 Created`:** → [ReservationResponse](#reservationresponse)

---

### List Reservations

```
GET /reservations
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `startDate` · `endDate` · `totalAmount` · `status` · `createdAt` |
| `status` | `ReservationStatus` | No | `ACTIVE` · `CLOSED` · `CANCELLED` |
| `contractStatus` | `ContractStatus` | No | `PENDING` · `PARTIAL_EXECUTION` · `FULLY_EXECUTED` |
| `customerId` | `string (UUID)` | No | Filter by customer |
| `vehicleId` | `string (UUID)` | No | Filter by vehicle |
| `startDateFrom` | `date` | No | ISO 8601 — reservation starts on or after this date |
| `startDateTo` | `date` | No | ISO 8601 — reservation starts on or before this date |

**Response `200 OK`:** `PagedResponse<ReservationResponse>`

---

### Get Reservation

```
GET /reservations/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:** → [ReservationResponse](#reservationresponse)

---

### Close Reservation (BRANCH_MANAGER, AGENCY_OWNER)

```
POST /reservations/{id}/close
Headers: X-Tenant-ID: casablanca-cars
```

No request body.

> On close: vehicle status → `AVAILABLE` (or `PENDING_RELOCATION` if returnHub ≠ pickupHub). Async invoice PDF generation triggered.

**Response `204 No Content`**

---

### Cancel Reservation (BRANCH_MANAGER, AGENCY_OWNER)

```
POST /reservations/{id}/cancel
Headers: X-Tenant-ID: casablanca-cars
```

No request body. Vehicle status → `AVAILABLE`.

**Response `204 No Content`**

---

### ReservationResponse

```json
{
  "id": "uuid",
  "customer": {
    "id": "uuid",
    "firstName": "Karim",
    "lastName": "Fassi",
    "phone": "+212612345678",
    "email": "karim.fassi@email.com",
    "idType": "CIN",
    "idNumber": "AB123456",
    "driverLicenseCode": "DL-MA-2018-001234",
    "address": "12 Rue des Roses, Rabat",
    "createdAt": "2025-06-01T10:00:00Z",
    "updatedAt": "2025-06-01T10:00:00Z"
  },
  "vehicle": { "...VehicleResponse..." },
  "pickupHub": { "...HubResponse..." },
  "returnHub": { "...HubResponse..." },
  "startDate": "2025-07-01",
  "endDate": "2025-07-07",
  "status": "ACTIVE",
  "totalAmount": 2450.00,
  "isDigitallySigned": false,
  "isPhysicallyPrinted": false,
  "signatureBase64": null,
  "contractStatus": "PENDING",
  "createdBy": "uuid-of-user",
  "payment": {
    "id": "uuid",
    "reservationId": "uuid",
    "totalContractAmount": 2450.00,
    "cashAdvanced": 500.00,
    "bankTransferReference": null,
    "bankTransferImageUrl": null,
    "depositType": "CHEQUE",
    "depositAmount": 5000.00,
    "depositStatus": "ACTIVE_HOLD",
    "chequeNumber": "CHQ-001234",
    "creditCardAuthReference": null,
    "depositReleasedAt": null,
    "depositReleasedBy": null,
    "createdAt": "2025-07-01T08:00:00Z",
    "updatedAt": "2025-07-01T08:00:00Z"
  },
  "contractUrl": null,
  "invoiceUrl": null,
  "createdAt": "2025-07-01T08:00:00Z",
  "updatedAt": "2025-07-01T08:00:00Z"
}
```

**`contractStatus` derivation:**

| `isDigitallySigned` | `isPhysicallyPrinted` | `contractStatus` |
|---|---|---|
| `false` | `false` | `PENDING` |
| `true` or `false` (one only) | opposite | `PARTIAL_EXECUTION` |
| `true` | `true` | `FULLY_EXECUTED` |

---

## 20. Payments & Deposits

### Get Payment for Reservation

```
GET /reservations/{reservationId}/payment
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:** → [PaymentResponse](#paymentresponse)

---

### Release Deposit (BRANCH_MANAGER, AGENCY_OWNER)

```
POST /reservations/{reservationId}/payment/release-deposit
Headers: X-Tenant-ID: casablanca-cars
```

No request body. Sets `depositStatus → RELEASED`.

**Response `204 No Content`**

---

### PaymentResponse

```json
{
  "id": "uuid",
  "reservationId": "uuid",
  "totalContractAmount": 2450.00,
  "cashAdvanced": 500.00,
  "bankTransferReference": null,
  "bankTransferImageUrl": null,
  "depositType": "CHEQUE",
  "depositAmount": 5000.00,
  "depositStatus": "ACTIVE_HOLD",
  "chequeNumber": "CHQ-001234",
  "creditCardAuthReference": null,
  "depositReleasedAt": null,
  "depositReleasedBy": null,
  "createdAt": "2025-07-01T08:00:00Z",
  "updatedAt": "2025-07-01T08:00:00Z"
}
```

---

## 21. Signatures & Contract Compliance

### Submit Digital Signature (AGENT)

```
POST /reservations/{id}/signature
Headers: X-Tenant-ID: casablanca-cars
```

**Request:**

```json
{
  "signatureBase64": "data:image/png;base64,iVBORw0KGgoAAAANS..."
}
```

Sets `isDigitallySigned = true`. Updates `contractStatus` accordingly.

**Response `204 No Content`**

---

### Mark Contract Printed (AGENT)

```
POST /reservations/{id}/mark-printed
Headers: X-Tenant-ID: casablanca-cars
```

No request body. Sets `isPhysicallyPrinted = true`. Updates `contractStatus` accordingly.

**Response `204 No Content`**

---

## 22. Document Status

PDF documents are generated asynchronously. Poll these endpoints to check readiness.

### Contract Status

```
GET /reservations/{id}/contract/status
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:**

```json
{
  "ready": true,
  "url": "https://storage/contracts/reservation-uuid.pdf"
}
```

| Field | Type | Notes |
|---|---|---|
| `ready` | `boolean` | `true` when PDF is generated |
| `url` | `string \| null` | Download URL; `null` if not yet ready |

---

### Invoice Status

```
GET /reservations/{id}/invoice/status
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:**

```json
{
  "ready": false,
  "url": null
}
```

---

### Subscription Invoice Status (AGENCY_OWNER)

```
GET /settings/subscription/invoice/status
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:** Same shape as above.

---

## 23. Booking Requests (Client-Facing)

Booking requests come from clients via the public portal. Staff can confirm or reject them; confirmed requests are converted to reservations.

### Submit Booking Request (Public — Client JWT)

```
POST /public/{slug}/booking-requests
Content-Type: multipart/form-data
Authorization: Bearer <client_jwt>
```

**Form fields:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `vehicleId` | string | Yes | UUID |
| `pickupHubId` | string | Yes | UUID |
| `returnHubId` | string | Yes | UUID |
| `startDate` | date | Yes | ISO 8601 |
| `endDate` | date | Yes | ISO 8601 |
| `idType` | string | Yes | `CIN` or `PASSPORT` |
| `idNumber` | string | Yes | |
| `driverLicenseCode` | string | Yes | |
| `notes` | string | No | |
| `idDocumentFile` | binary | Yes | Client's ID document image |
| `driverLicenseDocumentFile` | binary | Yes | Driver's license image |

**Response `201 Created`:** → [BookingRequestResponse](#bookingrequestresponse)

---

### List My Booking Requests (Public — Client JWT)

```
GET /public/{slug}/booking-requests
Authorization: Bearer <client_jwt>
```

**Response `200 OK`:** `Array<BookingRequestResponse>`

---

### Get Booking Request (Public — Client JWT)

```
GET /public/{slug}/booking-requests/{id}
Authorization: Bearer <client_jwt>
```

**Response `200 OK`:** → [BookingRequestResponse](#bookingrequestresponse)

---

### List Booking Requests — Staff (AGENT, BRANCH_MANAGER, AGENCY_OWNER)

```
GET /booking-requests
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Type | Required | Description |
|---|---|---|---|
| `page` | `integer` | No | Default `0` |
| `size` | `integer` | No | Default `20`, max `100` |
| `sort` | `string` | No | Sortable: `createdAt` · `startDate` · `status` |
| `status` | `BookingRequestStatus` | No | `PENDING_CONFIRMATION` · `CONFIRMED` · `REJECTED` |
| `vehicleId` | `string (UUID)` | No | Filter by vehicle |

**Response `200 OK`:** `PagedResponse<BookingRequestResponse>`

---

### Get Booking Request — Staff

```
GET /booking-requests/{id}
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:** → [BookingRequestResponse](#bookingrequestresponse)

---

### Confirm Booking Request (AGENT, BRANCH_MANAGER, AGENCY_OWNER)

```
POST /booking-requests/{id}/confirm
Headers: X-Tenant-ID: casablanca-cars
```

No request body. Converts booking request to a `Reservation`.

**Response `200 OK`:** → [BookingRequestResponse](#bookingrequestresponse)

---

### Reject Booking Request (AGENT, BRANCH_MANAGER, AGENCY_OWNER)

```
POST /booking-requests/{id}/reject
Headers: X-Tenant-ID: casablanca-cars
```

**Request:**

```json
{
  "rejectionReason": "Vehicle no longer available for these dates."
}
```

**Response `204 No Content`**

---

### BookingRequestResponse

```json
{
  "id": "uuid",
  "clientId": "uuid",
  "vehicleId": "uuid",
  "vehicleLicensePlate": "12345-A-1",
  "pickupHubId": "uuid",
  "pickupHubName": "Aéroport CMN Terminal 1",
  "returnHubId": "uuid",
  "returnHubName": "Agence Maarif",
  "startDate": "2025-07-10",
  "endDate": "2025-07-14",
  "idType": "CIN",
  "idNumber": "AB123456",
  "driverLicenseCode": "DL-MA-001234",
  "idDocumentUrl": "https://storage/docs/id-uuid.jpg",
  "driverLicenseDocumentUrl": "https://storage/docs/dl-uuid.jpg",
  "status": "PENDING_CONFIRMATION",
  "notes": "Need child seat if possible",
  "rejectionReason": null,
  "confirmedBy": null,
  "confirmedAt": null,
  "rejectedBy": null,
  "rejectedAt": null,
  "convertedReservationId": null,
  "createdAt": "2025-07-05T14:00:00Z",
  "updatedAt": "2025-07-05T14:00:00Z"
}
```

---

## 24. Client Auth

Clients are separate from staff users. Each agency tenant has its own client pool.

### Register Client (Public)

```
POST /public/{slug}/auth/register
```

**Request:**

```json
{
  "firstName": "Amina",
  "lastName": "Cherif",
  "email": "amina.cherif@gmail.com",
  "phone": "+212655000001",
  "password": "mypassword123"
}
```

| Field | Required | Constraints |
|---|---|---|
| `firstName` | Yes | notblank |
| `lastName` | Yes | notblank |
| `email` | Yes | valid email, unique per tenant |
| `phone` | Yes | notblank |
| `password` | Yes | notblank, min 8 chars |

**Response `201 Created`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "clientId": "uuid",
  "email": "amina.cherif@gmail.com",
  "firstName": "Amina",
  "lastName": "Cherif",
  "agencySlug": "casablanca-cars"
}
```

---

### Login Client (Public)

```
POST /public/{slug}/auth/login
```

**Request:**

```json
{
  "email": "amina.cherif@gmail.com",
  "password": "mypassword123"
}
```

**Response `200 OK`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "clientId": "uuid",
  "email": "amina.cherif@gmail.com",
  "firstName": "Amina",
  "lastName": "Cherif",
  "agencySlug": "casablanca-cars"
}
```

> The client token must be included as `Authorization: Bearer <token>` when submitting or viewing booking requests.

---

## 25. Dashboard

### Get Dashboard (AGENCY_OWNER, BRANCH_MANAGER, AGENT)

```
GET /dashboard
Headers: X-Tenant-ID: casablanca-cars
```

**Response `200 OK`:**

```json
{
  "fleetStatus": {
    "available": 12,
    "rented": 5,
    "maintenance": 2,
    "pendingRelocation": 1,
    "total": 20
  },
  "reservationCounts": {
    "currentMonth": 34,
    "currentYear": 198,
    "priorMonth": 28,
    "priorYear": 156
  },
  "revenue": {
    "currentMonthMad": 84500.00,
    "currentYearMad": 490200.00
  },
  "categoryBreakdown": [
    {
      "category": "ECONOMY",
      "fleetCount": 8,
      "closedReservationCount": 92
    },
    {
      "category": "SUV",
      "fleetCount": 5,
      "closedReservationCount": 67
    }
  ],
  "mostRentedCategory": "ECONOMY",
  "utilisationRate": 0.65,
  "depositSummary": {
    "activeHoldCount": 5,
    "activeHoldTotalMad": 25000.00,
    "releasedThisMonthCount": 18
  },
  "generatedAt": "2025-06-15T12:00:00Z"
}
```

| Field | Type | Notes |
|---|---|---|
| `fleetStatus` | object | Counts by vehicle status |
| `reservationCounts` | object | Reservation totals for current/prior periods |
| `revenue` | object | Revenue in MAD |
| `categoryBreakdown` | array | Per-category fleet + reservation counts |
| `mostRentedCategory` | `VehicleCategory \| null` | Top category by closed reservations |
| `utilisationRate` | `number` | 0.0 – 1.0; `rented / total` |
| `depositSummary` | object | Deposit hold stats |
| `generatedAt` | `Instant` | Server timestamp |

---

## 26. Calendar

### Get Calendar Events (AGENCY_OWNER, BRANCH_MANAGER, AGENT)

```
GET /calendar/events?from=2025-07-01&to=2025-07-31
Headers: X-Tenant-ID: casablanca-cars
```

| Query Param | Required | Type |
|---|---|---|
| `from` | Yes | `date` (ISO 8601, e.g. `2025-07-01`) |
| `to` | Yes | `date` (ISO 8601) |

**Response `200 OK`:**

```json
[
  {
    "type": "RESERVATION",
    "id": "uuid-of-reservation",
    "vehicleId": "uuid-of-vehicle",
    "vehiclePlate": "12345-A-1",
    "startDate": "2025-07-01",
    "endDate": "2025-07-07",
    "title": "Karim Fassi — Clio",
    "metadata": {
      "customerName": "Karim Fassi",
      "status": "ACTIVE",
      "pickupHub": "CMN Terminal 1"
    }
  },
  {
    "type": "BOOKING_REQUEST",
    "id": "uuid-of-booking-request",
    "vehicleId": "uuid-of-vehicle",
    "vehiclePlate": "67890-B-2",
    "startDate": "2025-07-10",
    "endDate": "2025-07-14",
    "title": "Booking Request — Clio V",
    "metadata": {
      "status": "PENDING_CONFIRMATION"
    }
  },
  {
    "type": "INSURANCE_EXPIRY",
    "id": "uuid-of-vehicle",
    "vehicleId": "uuid-of-vehicle",
    "vehiclePlate": "11111-C-3",
    "startDate": "2025-07-20",
    "endDate": "2025-07-20",
    "title": "Insurance Expires — BMW X5",
    "metadata": {
      "insuranceNumber": "INS-2023-042"
    }
  }
]
```

| Field | Type | Notes |
|---|---|---|
| `type` | `CalendarEventType` | `RESERVATION`, `BOOKING_REQUEST`, or `INSURANCE_EXPIRY` |
| `id` | `string (UUID)` | ID of the underlying entity |
| `vehicleId` | `string (UUID)` | |
| `vehiclePlate` | `string` | |
| `startDate` | `date` | ISO date |
| `endDate` | `date` | ISO date |
| `title` | `string` | Human-readable label |
| `metadata` | `object` | Additional context; keys vary by event type |

---

## Appendix A — Quota Enforcement

Quotas are enforced by `QuotaService` before creating branches, hubs, or vehicles. When a limit is exceeded, the API returns:

```
HTTP 403 Forbidden

{
  "message": "Vehicle quota exceeded. Your plan allows a maximum of 15 vehicles.",
  "status": 403,
  "timestamp": "2025-06-15T12:00:00Z"
}
```

The active subscription plan's `maxBranches`, `maxHubs`, and `maxVehicles` fields define the limits. `null` means unlimited (CHAMIL plan).

---

## Appendix B — Tenant Context

All agency-scoped endpoints require the `X-Tenant-ID` header. The value must be the agency's `slug` (derived from the agency name during approval, URL-safe, immutable).

**Without `X-Tenant-ID`:** Requests to tenant endpoints without this header will fail or route to the wrong schema.

**Where to find the slug:** It is returned in `AgencyResponse.slug` and in the JWT claim `agencySlug` for all non-SUPER_ADMIN users.

---

## Appendix C — Async PDF Polling Pattern

PDFs are generated off the request thread. The client should poll until `ready: true`:

```
1. POST /reservations        → 201 Created, reservationId = "abc"
2. GET  /reservations/abc/contract/status  → { ready: false, url: null }
3. ... (wait ~2s, retry) ...
4. GET  /reservations/abc/contract/status  → { ready: true, url: "https://..." }
5. Open URL to download PDF
```

Recommended polling interval: 2 seconds, max 30 retries.

---

## Appendix D — File Upload Notes

File upload endpoints (`POST /vehicles/{id}/images`, `POST /public/{slug}/booking-requests`) use `multipart/form-data`. Do **not** send `Content-Type: application/json` for these.

```
Content-Type: multipart/form-data; boundary=----FormBoundary7MA4YWxk
```

File size limits and accepted MIME types are enforced at the server level (Spring Boot default: 1MB per file, 10MB total; may be configured differently in deployment).
