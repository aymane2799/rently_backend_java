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

- [ ] Create `AgencyRegistrationStatus` enum (`PENDING`, `APPROVED`, `REJECTED`)
- [ ] Create `AgencyRegistration` entity — `agencyName`, `rcNumber`, `iceNumber`, `ifNumber`, `patente`, `city`, `address`, `website`, `ownerFirstName`, `ownerlastName`, `ownerEmail`, `ownerPhone`, `status`, `rejectionReason`, `submittedAt`, `reviewedAt`, `reviewedBy`, `resolvedAgencyId`
- [ ] Create `AgencyRegistrationRepository` — `findAllByStatus`, `existsByRcNumber`, `existsByIceNumber`, `existsByOwnerEmail`
- [ ] Create `SubmitRegistrationRequest` DTO — `agencyName`, `rcNumber`, `iceNumber`, `ifNumber` (opt), `patente` (opt), `city`, `address` (opt), `website` (opt), `ownerFirstName`, `ownerlastName`, `ownerEmail`, `ownerPhone`
- [ ] Create `AgencyRegistrationResponse` DTO
- [ ] Create `AgencyRegistrationService` — `submit`, `approve`, `reject`, `getAll`, `get`
- [ ] Implement `submit` — validates uniqueness of `rcNumber`, `iceNumber`, `ownerEmail`; creates `AgencyRegistration` with `status=PENDING` and `submittedAt`
- [ ] Implement `approve` — within a single `@Transactional`: creates `Agency` (status=APPROVED, approvedAt=now, slug generated) + creates `User` (role=AGENCY_OWNER, agencySlug set) + calls `TenantSchemaProvisioner` + sets registration `status=APPROVED`, `reviewedAt`, `resolvedAgencyId`; sends welcome email placeholder
- [ ] Implement `reject` — sets registration `status=REJECTED`, `reviewedAt`, `rejectionReason`; sends notification email placeholder
- [ ] Create `AgencyRegistrationController`
  - [ ] `POST /api/v1/agencies/register` — public, no auth
  - [ ] `GET /api/v1/admin/agencies/registrations` — `SUPER_ADMIN` only, filterable by status
  - [ ] `GET /api/v1/admin/agencies/registrations/{id}` — `SUPER_ADMIN` only
  - [ ] `POST /api/v1/admin/agencies/registrations/{id}/approve` — `SUPER_ADMIN` only
  - [ ] `POST /api/v1/admin/agencies/registrations/{id}/reject` — `SUPER_ADMIN` only, body: `{ rejectionReason }`

### 3.2 `Agency` — approved tenant entity

- [ ] *(update)* `Agency` entity — remove `submittedAt`, `rejectedAt`, `rejectionReason`; change `status` default to `APPROVED`; mark `approvedAt` NOT NULL; add `plan` (FK → `subscription_plans.id`)
- [ ] *(update)* `AgencyStatus` enum — only `APPROVED` and `BLOCKED`
- [ ] Create `AgencyRepository` — `findBySlug`, `findByEmail`, `findAllByStatus`
- [ ] Create `SlugGenerator` utility — converts agency name to a URL/schema-safe slug (lowercase, dashes, deduplication suffix if collision)
- [ ] Create `AgencyResponse` + `AgencyDetailResponse` DTOs
- [ ] Create `AgencyService` — `block`, `unblock`, `getAll`, `get`
- [ ] Implement `block` — sets `BLOCKED`; tenant middleware returns `403` for any request bearing this agency's slug
- [ ] Create `AgencyController`
  - [ ] `GET /api/v1/admin/agencies` — `SUPER_ADMIN` only, filterable by status
  - [ ] `GET /api/v1/admin/agencies/{id}` — `SUPER_ADMIN` only
  - [ ] `POST /api/v1/admin/agencies/{id}/block` — `SUPER_ADMIN` only
  - [ ] `POST /api/v1/admin/agencies/{id}/unblock` — `SUPER_ADMIN` only

---

## 4. Subscription & Billing

Handles plan management and the manual payment lifecycle.

- [ ] Create `SubscriptionPlan` entity — `code`, `displayName`, `description`, `priceMonthly`, `priceYearly`, `maxBranches`, `maxHubs`, `maxVehicles`, `isActive`
- [ ] Create `SubscriptionPlanRepository`
- [ ] Create `SubscriptionPlanService` + DTOs
- [ ] Seed two default plans on startup: `SAFI` and `CHAMIL`
- [ ] Create `SubscriptionPlanController`
  - [ ] `GET /api/v1/plans` — public (shown on pricing page)
  - [ ] `POST /api/v1/admin/plans` — `SUPER_ADMIN` only
  - [ ] `PATCH /api/v1/admin/plans/{id}` — `SUPER_ADMIN` only
  - [ ] `POST /api/v1/admin/plans/{id}/deactivate` — `SUPER_ADMIN` only
- [ ] Create `SubscriptionStatus` enum (`PENDING_PAYMENT`, `ACTIVE`, `EXPIRED`, `SUSPENDED`)
- [ ] Create `Subscription` entity — `agencySlug`, `plan` (FK), `status`, `startDate`, `endDate`, `amountDue`, `paymentMode`, `paidAt`, `invoiceUrl`
- [ ] Create `SubscriptionRepository` — `findByAgencySlug`, `findCurrentByAgencySlug`
- [ ] Create `SubscriptionService` — `create`, `markAsPaid`, `getForAgency`
- [ ] Implement `markAsPaid` — sets `status=ACTIVE`, `paidAt`, triggers async invoice PDF generation
- [ ] Configure `@Async` `ThreadPoolTaskExecutor` bean for background document jobs
- [ ] Implement async `SubscriptionInvoicePdfService` — generates PDF and writes `invoiceUrl`
- [ ] Create `SubscriptionController`
  - [ ] `GET /api/v1/admin/subscriptions` — `SUPER_ADMIN` only
  - [ ] `POST /api/v1/admin/subscriptions/{id}/mark-paid` — `SUPER_ADMIN` only, body: `paymentMode`
  - [ ] `GET /api/v1/settings/subscription` — `AGENCY_OWNER` — current agency subscription + invoice download link

---

## 5. Catalog Module — Updates & Extensions

Moves existing catalog entities to public-schema context, adds soft-delete, and introduces the agency extension request flow.

### 5.1 Updates to existing Brand / Model / Feature

- [ ] *(update)* `Brand` entity — add `isActive` field (`boolean`, default `true`); remove `@Table` tenant-routing annotation if present; ensure entity is resolved via public-schema `EntityManager`
- [ ] *(update)* `Feature` entity — add `isActive` field
- [ ] *(update)* `Model` entity — add `isActive` field
- [ ] *(update)* `BrandController` — replace `DELETE /{id}` with `POST /{id}/deactivate`; add `isActive` filter to `GET /`
- [ ] *(update)* `FeatureController` — same deactivate pattern
- [ ] *(update)* `ModelController` — same deactivate pattern
- [ ] *(update)* `BrandServiceImplementation` — remove hard-delete logic; implement deactivate
- [ ] *(update)* `FeatureServiceImplementation` — same
- [ ] *(update)* `ModelServiceImplementation` — same

### 5.2 `PublicCatalogService`

- [ ] Configure a `@Qualifier("publicEntityManagerFactory")` `EntityManagerFactory` bean pointing to the public schema, bypassing `CurrentTenantIdentifierResolver`
- [ ] Implement `PublicCatalogService` — `getModel`, `getFeatures`, `modelExists`, `allFeaturesExist`, `getBrand`, `brandExists`

### 5.3 Catalog Extension Requests

- [ ] Create `CatalogRequestType` enum (`BRAND`, `MODEL`, `FEATURE`)
- [ ] Create `CatalogRequestStatus` enum (`PENDING`, `APPROVED`, `REJECTED`)
- [ ] Create `CatalogRequest` entity — all fields per architecture doc (§2.8)
- [ ] Create `CatalogRequestRepository` — `findByAgencySlug`, `findAllByStatus`, `findAllByTypeAndStatus`
- [ ] Create `SubmitCatalogRequestRequest` DTO + `CatalogRequestResponse` DTO
- [ ] Create `CatalogRequestService`
  - [ ] `submit` — validates required fields per type (e.g., `proposedCategory` required for MODEL), creates `PENDING` record
  - [ ] `approve` — creates the catalog entity (Brand / Model / Feature), sets `resolvedEntityId`, sets `status=APPROVED`
  - [ ] `reject` — sets `status=REJECTED`, persists `rejectionReason`
- [ ] Create `CatalogRequestController`
  - [ ] `POST /api/v1/catalog-requests` — `AGENCY_OWNER` / `BRANCH_MANAGER`
  - [ ] `GET /api/v1/catalog-requests` — `AGENCY_OWNER` — own agency's requests
  - [ ] `GET /api/v1/admin/catalog-requests` — `SUPER_ADMIN`, filterable by type + status
  - [ ] `POST /api/v1/admin/catalog-requests/{id}/approve` — `SUPER_ADMIN`
  - [ ] `POST /api/v1/admin/catalog-requests/{id}/reject` — `SUPER_ADMIN`

---

## 6. Fleet Module — Updates to Existing Code

Adapts the existing Vehicle implementation to the public-catalog architecture.

- [ ] *(update)* `VehicleStatus` enum — add `PENDING_RELOCATION`
- [ ] *(update)* `Vehicle` entity
  - Replace `@ManyToOne Model model` with `String modelId` plain column
  - Replace `@ManyToMany Set<Feature> features` with `@ElementCollection Set<String> featureIds`
  - Add `String currentHubId` plain column (FK-by-app to tenant `hubs.id`)
  - Add `String currentParkingSlot` column
- [ ] *(update)* `VehicleHydrationContext` — remove `Model` and `Set<Feature>`; add `Hub` (for `currentHubId` existence check)
- [ ] *(update)* `VehicleHydrationResolver` — remove model/feature resolution (now handled by `PublicCatalogService`); add hub existence check
- [ ] *(update)* `VehicleHydrator` — align with updated resolver
- [ ] *(update)* `VehicleMapper`
  - `toEntity` receives plain IDs, no longer receives resolved Model/Feature objects
  - `toResponse` calls `PublicCatalogService` to resolve `modelId` → `ModelResponse` and `featureIds` → `Set<FeatureResponse>`
- [ ] *(update)* `VehicleServiceImplementation` — inject `PublicCatalogService`; add `modelExists` + `allFeaturesExist` checks before create/update; add plan quota check (max vehicles per `SubscriptionPlan.maxVehicles`)
- [ ] *(update)* `CreateVehicleRequest` — verify `featureIds` is `Set<String>` (UUID refs), `modelId` is `String` with `@UUID`
- [ ] *(update)* `VehicleRepository` — add `countByTenant` for quota check if needed

---

## 7. Location — Branch & Hub

Tenant-scoped physical location management.

- [ ] Create `HubType` enum (`AIRPORT`, `TRAIN_STATION`, `MAIN_OFFICE`, `PRIVATE_LOT`)
- [ ] Create `Branch` entity — `name`, `city`, `address`, `phone`, `isActive`
- [ ] Create `BranchRepository`
- [ ] Create `BranchService` + DTOs (`CreateBranchRequest`, `UpdateBranchRequest`, `BranchResponse`)
- [ ] Implement plan quota check in `BranchService.create` — rejects if `count(branches) >= plan.maxBranches`
- [ ] Create `BranchController`
  - [ ] `GET /api/v1/branches`
  - [ ] `GET /api/v1/branches/{id}`
  - [ ] `POST /api/v1/branches` — `AGENCY_OWNER` / `BRANCH_MANAGER`
  - [ ] `PATCH /api/v1/branches/{id}`
  - [ ] `POST /api/v1/branches/{id}/deactivate`
- [ ] Create `Hub` entity — `name`, `type`, `city`, `address`, `isActive`, `branch` (FK)
- [ ] Create `HubRepository`
- [ ] Create `HubService` + DTOs
- [ ] Implement plan quota check in `HubService.create` — rejects if `count(hubs) >= plan.maxHubs`
- [ ] Create `HubController`
  - [ ] `GET /api/v1/branches/{branchId}/hubs`
  - [ ] `POST /api/v1/branches/{branchId}/hubs` — `AGENCY_OWNER` / `BRANCH_MANAGER`
  - [ ] `PATCH /api/v1/hubs/{id}`
  - [ ] `POST /api/v1/hubs/{id}/deactivate`

---

## 8. Reservations — Customer & Booking

Core operational flow.

### 8.1 Customer

- [ ] Create `IdType` enum (`CIN`, `PASSPORT`)
- [ ] Create `Customer` entity — `firstName`, `lastName`, `phone`, `email`, `idType`, `idNumber`, `driverLicenseCode`, `address`
- [ ] Add unique constraint `(id_type, id_number)` on `customers` table
- [ ] Create `CustomerRepository` — `findByIdTypeAndIdNumber`, `existsByIdTypeAndIdNumber`
- [ ] Create `CustomerService` + DTOs
- [ ] Create `CustomerController`
  - [ ] `GET /api/v1/customers`
  - [ ] `GET /api/v1/customers/{id}`
  - [ ] `POST /api/v1/customers`
  - [ ] `PATCH /api/v1/customers/{id}`

### 8.2 Reservation

- [ ] Create `ReservationStatus` enum (`ACTIVE`, `CLOSED`, `CANCELLED`)
- [ ] Create `ContractStatus` enum (`PENDING`, `PARTIAL_EXECUTION`, `FULLY_EXECUTED`)
- [ ] Create `Reservation` entity — all fields per architecture doc (§2.13)
- [ ] Create `ReservationRepository` — `findOverlapping(vehicleId, startDate, endDate)` for concurrency check
- [ ] Create `ReservationService`
  - [ ] `create` — validates concurrency (no overlapping active reservations for same vehicle), sets `vehicle.status = RENTED`
  - [ ] `close` — sets `status=CLOSED`; if `returnHub ≠ pickupHub`, sets `vehicle.status = PENDING_RELOCATION`; else sets `vehicle.status = AVAILABLE`
  - [ ] `cancel` — sets `status=CANCELLED`, restores `vehicle.status = AVAILABLE`
  - [ ] `updateContractStatus` — derives `ContractStatus` from `isDigitallySigned` + `isPhysicallyPrinted`
- [ ] Create `ReservationController`
  - [ ] `POST /api/v1/reservations` — `AGENT` / `BRANCH_MANAGER`
  - [ ] `GET /api/v1/reservations`
  - [ ] `GET /api/v1/reservations/{id}`
  - [ ] `POST /api/v1/reservations/{id}/close`
  - [ ] `POST /api/v1/reservations/{id}/cancel`

---

## 9. Payment & Deposit

Created alongside each reservation; managed through the deposit lifecycle.

- [ ] Create `DepositType` enum (`CASH`, `CHEQUE`, `CREDIT_CARD_PREAUTH`)
- [ ] Create `DepositStatus` enum (`ACTIVE_HOLD`, `RELEASED`)
- [ ] Create `Payment` entity — all fields per architecture doc (§2.14)
- [ ] Add validation: `chequeNumber` required when `depositType=CHEQUE`; `creditCardAuthReference` required when `depositType=CREDIT_CARD_PREAUTH`
- [ ] Create `PaymentRepository`
- [ ] Create `PaymentService`
  - [ ] `createForReservation` — called atomically inside `ReservationService.create`
  - [ ] `releaseDeposit` — sets `depositStatus=RELEASED`, records `depositReleasedAt` + `depositReleasedBy`; restricted to `BRANCH_MANAGER` / `AGENCY_OWNER`
- [ ] Create `PaymentController`
  - [ ] `GET /api/v1/reservations/{reservationId}/payment`
  - [ ] `POST /api/v1/reservations/{reservationId}/payment/release-deposit` — `BRANCH_MANAGER` / `AGENCY_OWNER`

---

## 10. Signatures & Contract Compliance

- [ ] Create `SignatureController`
  - [ ] `POST /api/v1/reservations/{id}/signature` — `AGENT`; body: `{ signatureBase64: string }`; sets `isDigitallySigned=true`, stores Base64 PNG, triggers `updateContractStatus`
  - [ ] `POST /api/v1/reservations/{id}/mark-printed` — `AGENT`; sets `isPhysicallyPrinted=true`, triggers `updateContractStatus`
- [ ] Validate Base64 payload size server-side (reject if > 500KB after encoding)

---

## 11. Document Generation

All generation runs off the main HTTP thread via `@Async`.

- [ ] Implement `PdfGenerationService` using an HTML-to-PDF library (e.g., Flying Saucer / OpenPDF)
- [ ] Design rental contract HTML template — A4 layout, fields: agency ICE / IF / Patente / RC, customer identity, vehicle details, rental period, amounts, digital signature in footer
- [ ] Implement `ContractPdfService.generateAsync(reservationId)` — renders template, writes file, updates reservation with PDF URL
- [ ] Design customer invoice HTML template — A4, itemised totals, agency tax identifiers
- [ ] Implement `InvoicePdfService.generateAsync(reservationId)` — triggered on `Reservation.close`
- [ ] Implement `SubscriptionInvoicePdfService.generateAsync(subscriptionId)` — triggered on `Subscription.markAsPaid`
- [ ] Create `DocumentStatusController`
  - [ ] `GET /api/v1/reservations/{id}/contract/status` — returns `{ ready: boolean, url: string | null }`
  - [ ] `GET /api/v1/reservations/{id}/invoice/status`
  - [ ] `GET /api/v1/settings/subscription/invoice/status`

---

## 12. Plan Quota Enforcement (cross-cutting)

Centralised quota guard called from Branch, Hub, and Vehicle service layers.

- [ ] Implement `QuotaService` — resolves current agency's `SubscriptionPlan` (cached via Spring Cache + short TTL), exposes `assertCanAddBranch`, `assertCanAddHub`, `assertCanAddVehicle`
- [ ] Configure Spring Cache (`@EnableCaching`) with a short TTL cache for `SubscriptionPlan` lookups
- [ ] Wire `QuotaService.assertCanAddBranch` into `BranchService.create`
- [ ] Wire `QuotaService.assertCanAddHub` into `HubService.create`
- [ ] Wire `QuotaService.assertCanAddVehicle` into `VehicleService.create`
- [ ] Return `HTTP 403` with a structured error body when a quota is exceeded

---

## Progress Summary

| Section | Total | Done | Remaining |
| ------- | ----- | ---- | --------- |
| 1. Multi-Tenancy Infrastructure | 8 | 5 | 3 |
| 2. Auth & Security | 10 | 10 | 0 |
| 3. Agency Registration & Management | 25 | 2 | 23 |
| 4. Subscription & Billing | 15 | 0 | 15 |
| 5. Catalog — Updates & Extensions | 17 | 9 | 8 |
| 6. Fleet — Updates to Existing Code | 10 | 8 | 2 |
| 7. Location — Branch & Hub | 16 | 0 | 16 |
| 8. Reservations — Customer & Booking | 15 | 0 | 15 |
| 9. Payment & Deposit | 8 | 0 | 8 |
| 10. Signatures & Contract Compliance | 3 | 0 | 3 |
| 11. Document Generation | 9 | 0 | 9 |
| 12. Plan Quota Enforcement | 6 | 0 | 6 |
| **Total** | **142** | **34** | **108** |
