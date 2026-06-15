# Implementation Plan — KiraDrive MVP

**Legend**
- `[x]` Done
- `[ ]` Not started
- `*(update)*` — task modifies already-existing code

---

## 1. Multi-Tenancy Infrastructure

Foundation for all tenant-scoped features. Must be completed before any tenant module is built.

- [ ] Configure a secondary `DataSource` bean for the public schema (used by `PublicCatalogService`)
- [x] Implement `MultiTenantConnectionProvider` — switches the JDBC connection's search path per request
- [x] Implement `CurrentTenantIdentifierResolver` — reads `X-Tenant-ID` from a thread-local context
- [x] Implement `TenantContext` — thread-local holder set by the filter, cleared after the request
- [x] Implement `TenantFilter` — servlet filter that extracts `X-Tenant-ID` header and populates `TenantContext`
- [x] Configure Hibernate multi-tenancy strategy (`SCHEMA`) in `application.yaml`
- [x] Implement `TenantSchemaProvisioner` — utility that executes `CREATE SCHEMA IF NOT EXISTS <slug>` and runs DDL for all tenant tables
- [ ] Wire `TenantSchemaProvisioner` to be called atomically on agency `PENDING → APPROVED` transition

---

## 2. Auth & Security

Required by all protected endpoints. Build after multi-tenancy infrastructure.

- [x] Create `UserRole` enum (`SUPER_ADMIN`, `AGENCY_OWNER`, `BRANCH_MANAGER`, `AGENT`)
- [x] Create `User` entity — `firstName`, `lastName`, `email`, `passwordHash`, `role`, `agencySlug`, `branchId`, `isActive`
- [x] Create `UserRepository` — `findByEmail`
- [x] Create `UserService` + DTOs (`RegisterUserRequest`, `UserResponse`)
- [x] Implement `JwtTokenProvider` — generate and validate signed JWT tokens
- [x] Implement `JwtAuthenticationFilter` — validates token per request, sets `SecurityContext`
- [x] Configure Spring Security — public routes (`/api/v1/auth/**`, `/api/v1/agencies/register`), per-role access rules
- [x] Create `AuthController` — `POST /api/v1/auth/login` returning JWT + role + agencySlug
- [x] Create `UserController` — tenant owner manages their own staff (`POST`, `PATCH`, `DELETE`)
- [x] Implement password hashing with BCrypt

---

## 3. Agency Registration & Management

Two distinct sub-concerns: the public registration application (`AgencyRegistration`) and the approved tenant entity (`Agency`). An `Agency` is only ever created by approving a registration — it never exists in a pending state.

### 3.1 `AgencyRegistration` — application lifecycle

- [x] Create `AgencyRegistrationStatus` enum (`PENDING`, `APPROVED`, `REJECTED`)
- [x] Create `AgencyRegistration` entity — `agencyName`, `rcNumber`, `iceNumber`, `ifNumber`, `patent`, `city`, `address`, `website`, `ownerFirstName`, `ownerlastName`, `ownerEmail`, `ownerPhone`, `status`, `rejectionReason`, `submittedAt`, `reviewedAt`, `reviewedBy`, `resolvedAgencyId`
- [x] Create `AgencyRegistrationRepository` — `findAllByStatus`, `existsByRcNumber`, `existsByIceNumber`, `existsByOwnerEmail`
- [x] Create `SubmitRegistrationRequest` DTO — `agencyName`, `rcNumber`, `iceNumber`, `ifNumber` (opt), `patent` (opt), `city`, `address` (opt), `website` (opt), `ownerFirstName`, `ownerlastName`, `ownerEmail`, `ownerPhone`
- [x] Create `AgencyRegistrationResponse` DTO
- [x] Create `AgencyRegistrationService` — `submit`, `approve`, `reject`, `getAll`, `get`
- [x] Implement `submit` — validates uniqueness of `rcNumber`, `iceNumber`, `ownerEmail`; creates `AgencyRegistration` with `status=PENDING` and `submittedAt`
- [x] Implement `approve` — within a single `@Transactional`: creates `Agency` (status=APPROVED, approvedAt=now, slug generated) + creates `User` (role=AGENCY_OWNER, agencySlug set) + calls `TenantSchemaProvisioner` + sets registration `status=APPROVED`, `reviewedAt`, `resolvedAgencyId`; sends welcome email placeholder
- [x] Implement `reject` — sets registration `status=REJECTED`, `reviewedAt`, `rejectionReason`; sends notification email placeholder
- [x] Create `AgencyRegistrationController`
  - [x] `POST /api/v1/agencies/register` — public, no auth
  - [x] `GET /api/v1/admin/agencies/registrations` — `SUPER_ADMIN` only, filterable by status
  - [x] `GET /api/v1/admin/agencies/registrations/{id}` — `SUPER_ADMIN` only
  - [x] `POST /api/v1/admin/agencies/registrations/{id}/approve` — `SUPER_ADMIN` only
  - [x] `POST /api/v1/admin/agencies/registrations/{id}/reject` — `SUPER_ADMIN` only, body: `{ rejectionReason }`

### 3.2 `Agency` — approved tenant entity

- [x] *(update)* `Agency` entity — remove `submittedAt`, `rejectedAt`, `rejectionReason`; change `status` default to `APPROVED`; mark `approvedAt` NOT NULL; add `plan` (FK → `subscription_plans.id`)
- [x] *(update)* `AgencyStatus` enum — only `APPROVED` and `BLOCKED`
- [x] Create `AgencyRepository` — `findBySlug`, `findByEmail`, `findAllByStatus`
- [x] Create `SlugGenerator` utility — converts agency name to a URL/schema-safe slug (lowercase, dashes, deduplication suffix if collision)
- [x] Create `AgencyResponse` + `AgencyDetailResponse` DTOs
- [x] Create `AgencyService` — `block`, `unblock`, `getAll`, `get`
- [x] Implement `block` — sets `BLOCKED`; tenant middleware returns `403` for any request bearing this agency's slug
- [x] Create `AgencyController`
  - [x] `GET /api/v1/admin/agencies` — `SUPER_ADMIN` only, filterable by status
  - [x] `GET /api/v1/admin/agencies/{id}` — `SUPER_ADMIN` only
  - [x] `POST /api/v1/admin/agencies/{id}/block` — `SUPER_ADMIN` only
  - [x] `POST /api/v1/admin/agencies/{id}/unblock` — `SUPER_ADMIN` only

---

## 4. Subscription & Billing

Handles plan management and the manual payment lifecycle.

- [x] Create `SubscriptionPlan` entity — `code`, `displayName`, `description`, `priceMonthly`, `priceYearly`, `maxBranches`, `maxHubs`, `maxVehicles`, `isActive`
- [x] Create `SubscriptionPlanRepository`
- [x] Create `SubscriptionPlanService` + DTOs
- [x] Seed two default plans on startup: `SAFI` and `CHAMIL`
- [x] Create `SubscriptionPlanController`
  - [x] `GET /api/v1/plans` — public (shown on pricing page)
  - [x] `POST /api/v1/admin/plans` — `SUPER_ADMIN` only
  - [x] `PATCH /api/v1/admin/plans/{id}` — `SUPER_ADMIN` only
  - [x] `POST /api/v1/admin/plans/{id}/deactivate` — `SUPER_ADMIN` only
- [x] Create `SubscriptionStatus` enum (`PENDING_PAYMENT`, `ACTIVE`, `EXPIRED`, `SUSPENDED`)
- [x] Create `Subscription` entity — `agencySlug`, `plan` (FK), `status`, `startDate`, `endDate`, `amountDue`, `paymentMode`, `paidAt`, `invoiceUrl`
- [x] Create `SubscriptionRepository` — `findByAgencySlug`, `findCurrentByAgencySlug`
- [x] Create `SubscriptionService` — `create`, `markAsPaid`, `getForAgency`
- [x] Implement `markAsPaid` — sets `status=ACTIVE`, `paidAt`, triggers async invoice PDF generation
- [x] Configure `@Async` `ThreadPoolTaskExecutor` bean for background document jobs
- [x] Implement async `SubscriptionInvoicePdfService` — generates PDF and writes `invoiceUrl`
- [x] Create `SubscriptionController`
  - [x] `GET /api/v1/admin/subscriptions` — `SUPER_ADMIN` only
  - [x] `POST /api/v1/admin/subscriptions/{id}/mark-paid` — `SUPER_ADMIN` only, body: `paymentMode`
  - [x] `GET /api/v1/settings/subscription` — `AGENCY_OWNER` — current agency subscription + invoice download link

---

## 5. Catalog Module — Updates & Extensions

Moves existing catalog entities to public-schema context, adds soft-delete, and introduces the agency extension request flow.

### 5.1 Updates to existing Brand / Model / Feature

- [x] *(update)* `Brand` entity — add `isActive` field (`boolean`, default `true`); remove `@Table` tenant-routing annotation if present; ensure entity is resolved via public-schema `EntityManager`
- [x] *(update)* `Feature` entity — add `isActive` field
- [x] *(update)* `Model` entity — add `isActive` field
- [x] *(update)* `BrandController` — replace `DELETE /{id}` with `POST /{id}/deactivate`; add `isActive` filter to `GET /`
- [x] *(update)* `FeatureController` — same deactivate pattern
- [x] *(update)* `ModelController` — same deactivate pattern
- [x] *(update)* `BrandServiceImplementation` — remove hard-delete logic; implement deactivate
- [x] *(update)* `FeatureServiceImplementation` — same
- [x] *(update)* `ModelServiceImplementation` — same

### 5.2 `PublicCatalogService`

- [x] Configure a `@Qualifier("publicEntityManagerFactory")` `EntityManagerFactory` bean pointing to the public schema, bypassing `CurrentTenantIdentifierResolver`
- [x] Implement `PublicCatalogService` — `getModel`, `getFeatures`, `modelExists`, `allFeaturesExist`, `getBrand`, `brandExists`

### 5.3 Catalog Extension Requests

- [x] Create `CatalogRequestType` enum (`BRAND`, `MODEL`, `FEATURE`)
- [x] Create `CatalogRequestStatus` enum (`PENDING`, `APPROVED`, `REJECTED`)
- [x] Create `CatalogRequest` entity — all fields per architecture doc (§2.8)
- [x] Create `CatalogRequestRepository` — `findByAgencySlug`, `findAllByStatus`, `findAllByTypeAndStatus`
- [x] Create `SubmitCatalogRequestRequest` DTO + `CatalogRequestResponse` DTO
- [x] Create `CatalogRequestService`
  - [x] `submit` — validates required fields per type (e.g., `proposedCategory` required for MODEL), creates `PENDING` record
  - [x] `approve` — creates the catalog entity (Brand / Model / Feature), sets `resolvedEntityId`, sets `status=APPROVED`
  - [x] `reject` — sets `status=REJECTED`, persists `rejectionReason`
- [x] Create `CatalogRequestController`
  - [x] `POST /api/v1/catalog-requests` — `AGENCY_OWNER` / `BRANCH_MANAGER`
  - [x] `GET /api/v1/catalog-requests` — `AGENCY_OWNER` — own agency's requests
  - [x] `GET /api/v1/admin/catalog-requests` — `SUPER_ADMIN`, filterable by type + status
  - [x] `POST /api/v1/admin/catalog-requests/{id}/approve` — `SUPER_ADMIN`
  - [x] `POST /api/v1/admin/catalog-requests/{id}/reject` — `SUPER_ADMIN`

---

## 6. Fleet Module — Updates to Existing Code

Adapts the existing Vehicle implementation to the public-catalog architecture.

- [x] *(update)* `VehicleStatus` enum — add `PENDING_RELOCATION`
- [x] *(update)* `Vehicle` entity
  - Replace `@ManyToOne Model model` with `String modelId` plain column
  - Replace `@ManyToMany Set<Feature> features` with `@ElementCollection Set<String> featureIds`
  - Add `String currentHubId` plain column (FK-by-app to tenant `hubs.id`)
  - Add `String currentParkingSlot` column
- [x] *(update)* `VehicleHydrationContext` — remove `Model` and `Set<Feature>`; add `Hub` (for `currentHubId` existence check)
- [x] *(update)* `VehicleHydrationResolver` — remove model/feature resolution (now handled by `PublicCatalogService`); add hub existence check
- [x] *(update)* `VehicleHydrator` — align with updated resolver
- [x] *(update)* `VehicleMapper`
  - `toEntity` receives plain IDs, no longer receives resolved Model/Feature objects
  - `toResponse` calls `PublicCatalogService` to resolve `modelId` → `ModelResponse` and `featureIds` → `Set<FeatureResponse>`
- [x] *(update)* `VehicleServiceImplementation` — inject `PublicCatalogService`; add `modelExists` + `allFeaturesExist` checks before create/update; add plan quota check (max vehicles per `SubscriptionPlan.maxVehicles`)
- [x] *(update)* `CreateVehicleRequest` — verify `featureIds` is `Set<String>` (UUID refs), `modelId` is `String` with `@UUID`
- [x] *(update)* `VehicleRepository` — add `countByTenant` for quota check if needed

---

## 7. Location — Branch & Hub

Tenant-scoped physical location management.

- [x] Create `HubType` enum (`AIRPORT`, `TRAIN_STATION`, `MAIN_OFFICE`, `PRIVATE_LOT`)
- [x] Create `Branch` entity — `name`, `city`, `address`, `phone`, `isActive`
- [x] Create `BranchRepository`
- [x] Create `BranchService` + DTOs (`CreateBranchRequest`, `UpdateBranchRequest`, `BranchResponse`)
- [x] Implement plan quota check in `BranchService.create` — rejects if `count(branches) >= plan.maxBranches`
- [x] Create `BranchController`
  - [x] `GET /api/v1/branches`
  - [x] `GET /api/v1/branches/{id}`
  - [x] `POST /api/v1/branches` — `AGENCY_OWNER` / `BRANCH_MANAGER`
  - [x] `PATCH /api/v1/branches/{id}`
  - [x] `POST /api/v1/branches/{id}/deactivate`
- [x] Create `Hub` entity — `name`, `type`, `city`, `address`, `isActive`, `branch` (FK)
- [x] Create `HubRepository`
- [x] Create `HubService` + DTOs
- [x] Implement plan quota check in `HubService.create` — rejects if `count(hubs) >= plan.maxHubs`
- [x] Create `HubController`
  - [x] `GET /api/v1/branches/{branchId}/hubs`
  - [x] `POST /api/v1/branches/{branchId}/hubs` — `AGENCY_OWNER` / `BRANCH_MANAGER`
  - [x] `PATCH /api/v1/hubs/{id}`
  - [x] `POST /api/v1/hubs/{id}/deactivate`

---

## 8. Reservations — Customer & Booking

Core operational flow.

### 8.1 Customer

- [x] Create `IdType` enum (`CIN`, `PASSPORT`)
- [x] Create `Customer` entity — `firstName`, `lastName`, `phone`, `email`, `idType`, `idNumber`, `driverLicenseCode`, `address`
- [x] Add unique constraint `(id_type, id_number)` on `customers` table
- [x] Create `CustomerRepository` — `findByIdTypeAndIdNumber`, `existsByIdTypeAndIdNumber`
- [x] Create `CustomerService` + DTOs
- [x] Create `CustomerController`
  - [x] `GET /api/v1/customers`
  - [x] `GET /api/v1/customers/{id}`
  - [x] `POST /api/v1/customers`
  - [x] `PATCH /api/v1/customers/{id}`

### 8.2 Reservation

- [x] Create `ReservationStatus` enum (`ACTIVE`, `CLOSED`, `CANCELLED`)
- [x] Create `ContractStatus` enum (`PENDING`, `PARTIAL_EXECUTION`, `FULLY_EXECUTED`)
- [x] Create `Reservation` entity — all fields per architecture doc (§2.13)
- [x] Create `ReservationRepository` — `findOverlapping(vehicleId, startDate, endDate)` for concurrency check
- [x] Create `ReservationService`
  - [x] `create` — validates concurrency (no overlapping active reservations for same vehicle), sets `vehicle.status = RENTED`
  - [x] `close` — sets `status=CLOSED`; if `returnHub ≠ pickupHub`, sets `vehicle.status = PENDING_RELOCATION`; else sets `vehicle.status = AVAILABLE`
  - [x] `cancel` — sets `status=CANCELLED`, restores `vehicle.status = AVAILABLE`
  - [x] `updateContractStatus` — derives `ContractStatus` from `isDigitallySigned` + `isPhysicallyPrinted`
- [x] Create `ReservationController`
  - [x] `POST /api/v1/reservations` — `AGENT` / `BRANCH_MANAGER`
  - [x] `GET /api/v1/reservations`
  - [x] `GET /api/v1/reservations/{id}`
  - [x] `POST /api/v1/reservations/{id}/close`
  - [x] `POST /api/v1/reservations/{id}/cancel`

---

## 9. Payment & Deposit

Created alongside each reservation; managed through the deposit lifecycle.

- [x] Create `DepositType` enum (`CASH`, `CHEQUE`, `CREDIT_CARD_PREAUTH`)
- [x] Create `DepositStatus` enum (`ACTIVE_HOLD`, `RELEASED`)
- [x] Create `Payment` entity — all fields per architecture doc (§2.14)
- [x] Add validation: `chequeNumber` required when `depositType=CHEQUE`; `creditCardAuthReference` required when `depositType=CREDIT_CARD_PREAUTH`
- [x] Create `PaymentRepository`
- [x] Create `PaymentService`
  - [x] `createForReservation` — called atomically inside `ReservationService.create`
  - [x] `releaseDeposit` — sets `depositStatus=RELEASED`, records `depositReleasedAt` + `depositReleasedBy`; restricted to `BRANCH_MANAGER` / `AGENCY_OWNER`
- [x] Create `PaymentController`
  - [x] `GET /api/v1/reservations/{reservationId}/payment`
  - [x] `POST /api/v1/reservations/{reservationId}/payment/release-deposit` — `BRANCH_MANAGER` / `AGENCY_OWNER`

---

## 10. Signatures & Contract Compliance

- [x] Create `SignatureController`
  - [x] `POST /api/v1/reservations/{id}/signature` — `AGENT`; body: `{ signatureBase64: string }`; sets `isDigitallySigned=true`, stores Base64 PNG, triggers `updateContractStatus`
  - [x] `POST /api/v1/reservations/{id}/mark-printed` — `AGENT`; sets `isPhysicallyPrinted=true`, triggers `updateContractStatus`
- [x] Validate Base64 payload size server-side (reject if > 500KB after encoding)

---

## 11. Document Generation

All generation runs off the main HTTP thread via `@Async`.

- [x] Implement `PdfGenerationService` using an HTML-to-PDF library (e.g., Flying Saucer / OpenPDF)
- [x] Design rental contract HTML template — A4 layout, fields: agency ICE / IF / Patent / RC, customer identity, vehicle details, rental period, amounts, digital signature in footer
- [x] Implement `ContractPdfService.generateAsync(reservationId)` — renders template, writes file, updates reservation with PDF URL
- [x] Design customer invoice HTML template — A4, itemised totals, agency tax identifiers
- [x] Implement `InvoicePdfService.generateAsync(reservationId)` — triggered on `Reservation.close`
- [x] Implement `SubscriptionInvoicePdfService.generateAsync(subscriptionId)` — triggered on `Subscription.markAsPaid`
- [x] Create `DocumentStatusController`
  - [x] `GET /api/v1/reservations/{id}/contract/status` — returns `{ ready: boolean, url: string | null }`
  - [x] `GET /api/v1/reservations/{id}/invoice/status`
  - [x] `GET /api/v1/settings/subscription/invoice/status`

---

## 12. Plan Quota Enforcement (cross-cutting)

Centralised quota guard called from Branch, Hub, and Vehicle service layers.

- [x] Implement `QuotaService` — resolves current agency's `SubscriptionPlan` (cached via Spring Cache + short TTL), exposes `assertCanAddBranch`, `assertCanAddHub`, `assertCanAddVehicle`
- [x] Configure Spring Cache (`@EnableCaching`) with a short TTL cache for `SubscriptionPlan` lookups
- [x] Wire `QuotaService.assertCanAddBranch` into `BranchService.create`
- [x] Wire `QuotaService.assertCanAddHub` into `HubService.create`
- [x] Wire `QuotaService.assertCanAddVehicle` into `VehicleService.create`
- [x] Return `HTTP 403` with a structured error body when a quota is exceeded

---

## Progress Summary

| Section | Total | Done | Remaining |
| ------- | ----- | ---- | --------- |
| 1. Multi-Tenancy Infrastructure | 8 | 5 | 3 |
| 2. Auth & Security | 10 | 10 | 0 |
| 3. Agency Registration & Management | 25 | 25 | 0 |
| 4. Subscription & Billing | 15 | 15 | 0 |
| 5. Catalog — Updates & Extensions | 17 | 17 | 0 |
| 6. Fleet — Updates to Existing Code | 10 | 10 | 0 |
| 7. Location — Branch & Hub | 16 | 16 | 0 |
| 8. Reservations — Customer & Booking | 15 | 15 | 0 |
| 9. Payment & Deposit | 8 | 8 | 0 |
| 10. Signatures & Contract Compliance | 3 | 3 | 0 |
| 11. Document Generation | 9 | 9 | 0 |
| 12. Plan Quota Enforcement | 6 | 6 | 0 |
| **Total** | **142** | **139** | **3** |
