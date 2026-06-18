# Implementation Plan — KiraDrive MVP

**Legend**
- `[x]` Done
- `[ ]` Not started
- `*(update)*` — task modifies already-existing code

---

## 1. Multi-Tenancy Infrastructure

Foundation for all tenant-scoped features. Must be completed before any tenant module is built.

- [x] Configure a secondary `DataSource` bean for the public schema (used by `PublicCatalogService`)
- [x] Implement `MultiTenantConnectionProvider` — switches the JDBC connection's search path per request
- [x] Implement `CurrentTenantIdentifierResolver` — reads `X-Tenant-ID` from a thread-local context
- [x] Implement `TenantContext` — thread-local holder set by the filter, cleared after the request
- [x] Implement `TenantFilter` — servlet filter that extracts `X-Tenant-ID` header and populates `TenantContext`
- [x] Configure Hibernate multi-tenancy strategy (`SCHEMA`) in `application.yaml`
- [x] Implement `TenantSchemaProvisioner` — utility that executes `CREATE SCHEMA IF NOT EXISTS <slug>` and runs DDL for all tenant tables
- [x] Wire `TenantSchemaProvisioner` to be called atomically on agency `PENDING → APPROVED` transition

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

## 13. Vehicle Image Gallery

Adds a photo gallery to each vehicle with primary-image designation and display ordering.

- [x] Create `VehicleImage` entity — `vehicleId`, `imageUrl`, `isPrimary`, `displayOrder`, `altText`
- [x] Create `VehicleImageRepository` — `findByVehicleIdOrderByDisplayOrder`, `findPrimaryByVehicleId`
- [x] Create `VehicleImageService`
  - [x] `upload` — saves compressed image URL, default `isPrimary=false`
  - [x] `setPrimary` — atomically clears existing primary flag then sets new one
  - [x] `delete` — removes image; if deleted image was primary, promotes the lowest-order remaining image
  - [x] `reorder` — accepts ordered list of image IDs, reassigns `displayOrder` values
- [x] Create `VehicleImageResponse` DTO — `id`, `imageUrl`, `isPrimary`, `displayOrder`, `altText`
- [x] *(update)* `VehicleResponse` — add `List<VehicleImageResponse> images`
- [x] Create `VehicleImageController`
  - [x] `POST /api/v1/vehicles/{vehicleId}/images` — `AGENCY_OWNER` / `BRANCH_MANAGER`; body: multipart image upload
  - [x] `DELETE /api/v1/vehicles/{vehicleId}/images/{imageId}` — `AGENCY_OWNER` / `BRANCH_MANAGER`
  - [x] `PATCH /api/v1/vehicles/{vehicleId}/images/{imageId}/set-primary` — `AGENCY_OWNER` / `BRANCH_MANAGER`
  - [x] `PATCH /api/v1/vehicles/{vehicleId}/images/reorder` — `AGENCY_OWNER` / `BRANCH_MANAGER`; body: ordered list of image IDs

---

## 14. Agency Branding & Public Landing Page

Adds branding configuration to `Agency` and exposes the public-facing landing page endpoints.

- [x] *(update)* `Agency` entity — add `tagline`, `primaryColor`, `secondaryColor`, `darkPrimaryColor`, `darkSecondaryColor`, `metaTitle`, `metaDescription`, `metaKeywords`, `ogImageUrl`
- [x] Create `UpdateAgencyBrandingRequest` DTO — all nine branding fields, all optional
- [x] Create `AgencyBrandingController`
  - [x] `GET /api/v1/settings/agency/branding` — `AGENCY_OWNER`; returns current branding fields
  - [x] `PATCH /api/v1/settings/agency/branding` — `AGENCY_OWNER`; updates branding fields → `204 NO_CONTENT`
- [x] Add ZXing (`com.google.zxing`) dependency for QR code generation
- [x] Implement `QrCodeService.generatePng(url, sizePixels)` — generates QR code as `byte[]`
- [x] Create `PublicQrCodeController`
  - [x] `GET /api/v1/agencies/{slug}/qr-code` — any authenticated; returns `image/png` of the agency's landing page URL
- [x] Create `PublicAgencyController` (no auth — `permitAll`)
  - [x] `GET /api/v1/public/{slug}` — returns `AgencyPublicProfileResponse` (name, logo, tagline, branding colours, SEO metadata)
  - [x] `GET /api/v1/public/{slug}/vehicles` — returns available vehicles with optional query params: `from`, `to` (date range), `category`, `transmission`; enforces availability check when date params provided
- [x] Create `AgencyPublicProfileResponse` DTO — all public-facing agency fields including branding and SEO
- [x] Create `PublicVehicleResponse` DTO — model name, category, transmission, daily rate, seats, features, primary image URL

---

## 15. Client Accounts & Booking Requests

Enables client self-registration on the agency landing page and the two-step booking request lifecycle.

### 15.1 Client Accounts

- [x] Create `ClientAccount` entity — `firstName`, `lastName`, `email`, `phone`, `passwordHash`, `isActive`, `emailVerifiedAt`
- [x] Create `ClientAccountRepository` — `findByEmail`, `existsByEmail`
- [x] Create `ClientAccountService` — `register`, `login`
- [x] Implement `ClientJwtTokenProvider` — issues a separate JWT for client accounts; payload: `clientId`, `agencySlug`
- [x] Create `ClientAuthController` (public — `permitAll`)
  - [x] `POST /api/v1/public/{slug}/auth/register` — validates uniqueness of email within tenant; returns client JWT
  - [x] `POST /api/v1/public/{slug}/auth/login` — returns client JWT

### 15.2 Booking Requests

- [x] Create `BookingRequestStatus` enum (`PENDING_CONFIRMATION`, `CONFIRMED`, `REJECTED`)
- [x] Create `BookingRequest` entity — all fields per architecture doc (§2.17)
- [x] Create `BookingRequestRepository`
  - [x] `findOverlapping(vehicleId, startDate, endDate)` — checks for active Reservations AND PENDING_CONFIRMATION BookingRequests in the date window
  - [x] `findByClientAccountId(clientId)`
  - [x] `findAllByStatus(status)` — for agency staff queue
- [x] Create `SubmitBookingRequestRequest` DTO — multipart form: `vehicleId`, `pickupHubId`, `returnHubId`, `startDate`, `endDate`, `idType` (`@ValidEnum`), `idNumber`, `driverLicenseCode`, `idDocumentFile` (multipart), `driverLicenseDocumentFile` (multipart), `notes` (optional); validate all identity fields present (`422` otherwise)
- [x] Create `BookingRequestResponse` DTO — includes identity field values + document URLs (for agency staff review)
- [x] Create `BookingRequestService`
  - [x] `submit(clientId, request, idDocumentFile, driverLicenseDocumentFile)` — stores uploaded files, runs availability check (overlapping Reservations + BookingRequests); throws `409` if unavailable; creates `PENDING_CONFIRMATION` record with identity fields + document URLs
  - [x] `confirm(requestId, agentUserId)` — `@Transactional`: upserts `Customer` record using `(idType, idNumber)` as lookup key (creates if absent, using firstName/lastName/phone/email from `ClientAccount` + identity fields from `BookingRequest`); creates `Reservation` (status=`ACTIVE`) linked to the `Customer`; creates `Payment` (amounts to be completed by agent); sets request `CONFIRMED`; populates `convertedReservationId`
  - [x] `reject(requestId, agentUserId, rejectionReason)` — sets `REJECTED`, records `rejectedAt`/`rejectedBy`, dispatches rejection email to `clientAccount.email`
- [x] Create `PublicBookingRequestController` (client JWT auth)
  - [x] `POST /api/v1/public/{slug}/booking-requests` — submit new booking request
  - [x] `GET /api/v1/public/{slug}/booking-requests` — list own booking requests (client sees own only)
  - [x] `GET /api/v1/public/{slug}/booking-requests/{id}` — get single booking request status
- [x] Create `BookingRequestController` (agency staff auth)
  - [x] `GET /api/v1/booking-requests` — `AGENT` / `BRANCH_MANAGER` / `AGENCY_OWNER`; list all requests, filterable by status
  - [x] `GET /api/v1/booking-requests/{id}` — detail view
  - [x] `POST /api/v1/booking-requests/{id}/confirm` — `AGENT` / `BRANCH_MANAGER` / `AGENCY_OWNER`
  - [x] `POST /api/v1/booking-requests/{id}/reject` — `AGENT` / `BRANCH_MANAGER` / `AGENCY_OWNER`; body: `{ rejectionReason }`

---

## 16. Agency Operations Dashboard

All metrics computed at query time — no new persisted entity.

- [x] Create `DashboardResponse` DTO — fleet status counts, reservation counts (month/year + prior year comparisons), revenue totals, category breakdown, utilisation rate, deposit summary, `generatedAt`
- [x] Create `DashboardService`
  - [x] `getFleetStatusCounts(branchId?)` — count vehicles per `VehicleStatus`; total fleet count
  - [x] `getReservationCounts(branchId?)` — count reservations for current month, current year, prior month, prior year
  - [x] `getRevenueTotals(branchId?)` — sum `Payment.totalContractAmount` for `CLOSED` reservations in current month and current year
  - [x] `getVehicleCategoryBreakdown()` — count per `VehicleCategory` in fleet; identify most-rented category by closed reservation count in current year
  - [x] `getDepositSummary(branchId?)` — count + MAD total of `ACTIVE_HOLD` deposits; count of `RELEASED` deposits in current period
  - [x] `getUtilisationRate(branchId?)` — `(total rented vehicle-days in current month) ÷ (total fleet × days in month) × 100`
- [x] Create `DashboardController`
  - [x] `GET /api/v1/dashboard` — `AGENCY_OWNER` / `BRANCH_MANAGER` / `AGENT`; passes `branchId` from JWT for non-owner roles

---

## 17. Calendar & Gantt Timeline View

Unified event API consumed by both the date-grid calendar and the per-vehicle Gantt timeline on the frontend.

- [x] Create `CalendarEventType` enum (`RESERVATION`, `BOOKING_REQUEST`, `INSURANCE_EXPIRY`)
- [x] Create `CalendarEventResponse` DTO — `type`, `id`, `vehicleId`, `vehiclePlate`, `startDate`, `endDate`, `title`, `metadata` (map for type-specific fields: customerName, status, daysRemaining, etc.)
- [x] Create `CalendarService`
  - [x] `getEvents(from, to, branchId?)` — queries `Reservation` (ACTIVE/CLOSED), `BookingRequest` (PENDING_CONFIRMATION), and `Vehicle.insuranceExpiresAt`; maps each to a typed `CalendarEventResponse`
  - [x] Validates that `from ≤ to` and range ≤ 366 days; throws `400` otherwise
- [x] Create `CalendarController`
  - [x] `GET /api/v1/calendar/events?from={date}&to={date}` — `AGENCY_OWNER` / `BRANCH_MANAGER` / `AGENT`; branch-scoped for non-owner roles; returns `List<CalendarEventResponse>`

---

## 18. Pagination, Filtering & Sorting

Adds paginated list endpoints with per-entity filters and sort controls to every `getAll` API. Introduces a shared `PagedResponse<T>` wrapper and per-entity `Specification` classes. Also adds lightweight `/options` endpoints for select/dropdown inputs that return minimal DTOs without pagination.

### 18.1 Shared Infrastructure

- [x] Create `PagedResponse<T>` generic record in `shared/` — fields: `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`
- [x] Create `PagedResponseMapper` utility — converts Spring `Page<E>` to `PagedResponse<R>` by applying a mapping function to each element

### 18.2 Public Catalog — Brands

- [x] Add `JpaSpecificationExecutor<Brand>` to `BrandRepository`
- [x] Create `BrandSpecification` — predicates: `isActive`, `search` (name `ILIKE`)
- [x] *(update)* `BrandController.getAll` — accepts `page`, `size`, `sort`, `active`, `search`; returns `PagedResponse<BrandResponse>`
- [x] Add `GET /brands/options` — no pagination; returns `List<BrandOptionResponse>` (`id`, `name`)

### 18.3 Public Catalog — Models

- [x] Add `JpaSpecificationExecutor<Model>` to `ModelRepository`
- [x] Create `ModelSpecification` — predicates: `isActive`, `brandId`, `category`, `search`
- [x] *(update)* `ModelController.getAll` — accepts `page`, `size`, `sort`, `active`, `brandId`, `category`, `search`; returns `PagedResponse<ModelResponse>`
- [x] Add `GET /models/options` — accepts optional `brandId`; returns `List<ModelOptionResponse>` (`id`, `name`, `category`)

### 18.4 Public Catalog — Features

- [x] Add `JpaSpecificationExecutor<Feature>` to `FeatureRepository`
- [x] Create `FeatureSpecification` — predicates: `isActive`, `search`
- [x] *(update)* `FeatureController.getAll` — accepts `page`, `size`, `sort`, `active`, `search`; returns `PagedResponse<FeatureResponse>`
- [x] Add `GET /features/options` — no pagination; returns `List<FeatureOptionResponse>` (`id`, `name`, `icon`)

### 18.5 Admin — Agencies

- [x] Add `JpaSpecificationExecutor<Agency>` to `AgencyRepository`
- [x] Create `AgencySpecification` — predicates: `status`, `city`, `planId`, `search` (name, owner name, email)
- [x] *(update)* `AdminAgencyController.getAll` — accepts `page`, `size`, `sort`, `status`, `city`, `planId`, `search`; returns `PagedResponse<AgencyResponse>`

### 18.6 Admin — Agency Registrations

- [x] Add `JpaSpecificationExecutor<AgencyRegistration>` to `AgencyRegistrationRepository`
- [x] Create `AgencyRegistrationSpecification` — predicates: `status`, `city`, `search` (agency name, owner email)
- [x] *(update)* `AdminRegistrationController.getAll` — accepts `page`, `size`, `sort`, `status`, `city`, `search`; returns `PagedResponse<AgencyRegistrationResponse>`

### 18.7 Admin — Subscriptions

- [x] Add `JpaSpecificationExecutor<Subscription>` to `SubscriptionRepository`
- [x] Create `SubscriptionSpecification` — predicates: `status`, `agencySlug`, `planId`, `startDateFrom`, `startDateTo`
- [x] *(update)* `AdminSubscriptionController.getAll` — accepts `page`, `size`, `sort`, `status`, `agencySlug`, `planId`, `startDateFrom`, `startDateTo`; returns `PagedResponse<SubscriptionResponse>`

### 18.8 Admin — Catalog Requests

- [x] Add `JpaSpecificationExecutor<CatalogRequest>` to `CatalogRequestRepository`
- [x] Create `CatalogRequestSpecification` — predicates: `type`, `status`, `agencySlug`
- [x] *(update)* `AdminCatalogRequestController.getAll` — accepts `page`, `size`, `sort`, `type`, `status`; returns `PagedResponse<CatalogRequestResponse>`
- [x] *(update)* `CatalogRequestController.getForAgency` — accepts `page`, `size`, `sort`, `type`, `status`; returns `PagedResponse<CatalogRequestResponse>`

### 18.9 Auth — Users

- [x] Add `JpaSpecificationExecutor<User>` to `UserRepository`
- [x] Create `UserSpecification` — predicates: `role`, `branchId`, `active`, `search` (first/last name, email)
- [x] *(update)* `UserController.getAll` — accepts `page`, `size`, `sort`, `role`, `branchId`, `active`, `search`; returns `PagedResponse<UserResponse>`

### 18.10 Location — Branches

- [x] Add `JpaSpecificationExecutor<Branch>` to `BranchRepository`
- [x] Create `BranchSpecification` — predicates: `active`, `city`, `search` (name)
- [x] *(update)* `BranchController.getAll` — accepts `page`, `size`, `sort`, `active`, `city`, `search`; returns `PagedResponse<BranchResponse>`
- [x] Add `GET /branches/options` — no pagination; returns `List<BranchOptionResponse>` (`id`, `name`, `city`)

### 18.11 Location — Hubs

- [x] Add `JpaSpecificationExecutor<Hub>` to `HubRepository`
- [x] Create `HubSpecification` — predicates: `active`, `type`
- [x] *(update)* `HubController.getAllByBranch` — accepts `page`, `size`, `sort`, `active`, `type`; returns `PagedResponse<HubResponse>`
- [x] Add `GET /hubs/options` — requires `branchId`; returns `List<HubOptionResponse>` (`id`, `name`, `type`)

### 18.12 Fleet — Vehicles

- [x] Add `JpaSpecificationExecutor<Vehicle>` to `VehicleRepository`
- [x] Create `VehicleSpecification` — predicates: `status`, `transmission`, `fuelType`, `currentHubId`, `search` (license plate)
- [x] *(update)* `VehicleController.getAll` — accepts `page`, `size`, `sort`, `status`, `transmission`, `fuelType`, `hubId`, `search`; returns `PagedResponse<VehicleResponse>`

### 18.13 Reservations — Customers

- [x] Add `JpaSpecificationExecutor<Customer>` to `CustomerRepository`
- [x] Create `CustomerSpecification` — predicates: `idType`, `search` (first/last name, phone, email, id number)
- [x] *(update)* `CustomerController.getAll` — accepts `page`, `size`, `sort`, `idType`, `search`; returns `PagedResponse<CustomerResponse>`
- [x] Add `GET /customers/options` — requires `search` ≥ 2 chars; returns `List<CustomerOptionResponse>` (`id`, `firstName`, `lastName`, `idNumber`, `idType`)

### 18.14 Reservations

- [x] Add `JpaSpecificationExecutor<Reservation>` to `ReservationRepository`
- [x] Create `ReservationSpecification` — predicates: `status`, `contractStatus`, `customerId`, `vehicleId`, `startDateFrom`, `startDateTo`, `createdBy`
- [x] *(update)* `ReservationController.getAll` — accepts `page`, `size`, `sort`, `status`, `contractStatus`, `customerId`, `vehicleId`, `startDateFrom`, `startDateTo`; returns `PagedResponse<ReservationResponse>`

### 18.15 Booking Requests

- [x] Add `JpaSpecificationExecutor<BookingRequest>` to `BookingRequestRepository`
- [x] Create `BookingRequestSpecification` — predicates: `status`, `vehicleId`
- [x] *(update)* `BookingRequestController.getAll` — accepts `page`, `size`, `sort`, `status`, `vehicleId`; returns `PagedResponse<BookingRequestResponse>`

---

## Progress Summary

| Section | Total | Done | Remaining |
| ------- | ----- | ---- | --------- |
| 1. Multi-Tenancy Infrastructure | 8 | 8 | 0 |
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
| 13. Vehicle Image Gallery | 8 | 8 | 0 |
| 14. Agency Branding & Public Landing Page | 10 | 10 | 0 |
| 15. Client Accounts & Booking Requests | 19 | 19 | 0 |
| 16. Agency Operations Dashboard | 9 | 9 | 0 |
| 17. Calendar & Gantt Timeline View | 5 | 5 | 0 |
| 18. Pagination, Filtering & Sorting | 51 | 51 | 0 |
| **Total** | **244** | **244** | **0** |
