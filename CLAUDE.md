# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Implementation Tracking

**After completing any task from `src/docs/implementation_plan.md`**, update that file immediately:

1. Flip the checkbox: `- [ ]` → `- [x]`
2. Update the **Progress Summary** table at the bottom — increment `Done`, decrement `Remaining` for the affected section.

Do this as the final step of every implementation session, before reporting the work as done.

---

## Documentation Reference

All design documents live under `src/docs/`. Read the relevant file before implementing anything in its domain.

| File | Description | When to read |
|---|---|---|
| [`src/docs/prd.md`](src/docs/prd.md) | Product Requirements Document — user roles, module-by-module feature specs (Modules 3.1–3.6), acceptance criteria, subscription tiers, monetisation workflow, success KPIs | When clarifying what a feature must do or what a field/flow is for |
| [`src/docs/architecture.md`](src/docs/architecture.md) | Entity Reference — every entity's full field list, column names, constraints, enums, relationships, and multi-tenancy notes; includes cross-schema reference pattern, `PublicCatalogService` code sample, plan quota table | When adding/changing any entity, writing a mapper, or touching cross-schema logic |
| [`src/docs/implementation_plan.md`](src/docs/implementation_plan.md) | Ordered implementation checklist — 12 sections with per-task `[ ]`/`[x]` status, progress summary table | When picking the next task to implement or checking what is already done |
| [`src/docs/explanations.md`](src/docs/explanations.md) | Architecture decision records — explains *why* every major design choice was made (UUID strategy, hydration pattern, schema-per-tenant, soft-delete, JWT claims, async PDF, etc.) | When the reasoning behind a pattern is unclear, or before changing a fundamental design decision |

---

## Commands

```bash
# Build
./mvnw clean install

# Run (requires DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD env vars)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Skip tests during build
./mvnw clean install -DskipTests
```

The dev profile runs on port **8090**. Set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` before running. Flyway is disabled by default (`flyway.enabled: false`); Hibernate manages schema via `ddl-auto: update`.

---

## Project Overview

**KiraDrive** — a multi-tenant SaaS platform for car rental agency management in Morocco. Agencies (tenants) are isolated via PostgreSQL schema-per-tenant. The `X-Tenant-ID` request header drives all tenant routing.

**Stack:** Spring Boot 4.0 · Java 21 · PostgreSQL · Hibernate (`SCHEMA` multi-tenancy strategy)

---

## Module Structure

All code lives under `com.rently.rently`:

| Module | Schema | Contents |
|---|---|---|
| `agency` | PUBLIC | `Agency`, `AgencyRegistration`, `AgencyStatus`, `AgencyRegistrationStatus` |
| `auth` | PUBLIC | `User`, `UserRole`, JWT filter, Spring Security config |
| `billing` | PUBLIC | `SubscriptionPlan`, `Subscription`, `SubscriptionStatus` |
| `catalog` | PUBLIC | `Brand`, `Model`, `Feature`, `CatalogRequest` (+ hydration sub-packages) |
| `fleet` | TENANT | `Vehicle`, `VehicleStatus`, `FuelType`, `Transmission` (+ hydration) |
| `location` | TENANT | `Branch`, `Hub`, `HubType` |
| `reservation` | TENANT | `Customer`, `Reservation`, `Payment`, related enums |
| `shared` | — | `Auditable`, `CRUDService`, mapper interfaces |
| `validation` | — | `@ValidEnum` + `EnumValidator` |

Each sub-package is self-contained: entity, repository, service interface + impl, mapper, controller, request/response DTOs, and optionally a `hydration/` sub-package.

---

## Schema Placement

**PUBLIC schema** — no tenant context required:
`Agency`, `AgencyRegistration`, `User`, `Subscription`, `SubscriptionPlan`, `Brand`, `Model`, `Feature`, `CatalogRequest`

**TENANT schema** — routed via `X-Tenant-ID` header → `CurrentTenantIdentifierResolver` → `MultiTenantConnectionProvider`:
`Vehicle`, `Branch`, `Hub`, `Customer`, `Reservation`, `Payment`

`Agency.slug` is the PostgreSQL schema name for each tenant (URL-safe, immutable after approval).

---

## Multi-Tenancy Infrastructure

- `TenantFilter` — servlet filter; reads `X-Tenant-ID` header, populates `TenantContext` (thread-local)
- `CurrentTenantIdentifierResolver` — reads from `TenantContext`; null for `SUPER_ADMIN` requests
- `MultiTenantConnectionProvider` — switches JDBC connection `search_path` per request
- `TenantSchemaProvisioner` — runs `CREATE SCHEMA IF NOT EXISTS <slug>` + DDL for all tenant tables; called atomically inside `AgencyRegistrationService.approve`

---

## Cross-Schema Reference Pattern

**Tenant-schema entities cannot hold JPA relationships to public-schema entities.** Instead they store plain `String` UUID columns. Application-layer existence checks via `PublicCatalogService` enforce integrity before writes.

| Tenant field | References | JPA mapping |
|---|---|---|
| `Vehicle.modelId` | `public.car_models.id` | Plain `String` column |
| `Vehicle.featureIds` | `public.features.id` | `@ElementCollection Set<String>` |
| `User.agencySlug` | `public.agencies.slug` | Plain `String` column |
| `User.branchId` | tenant `branches.id` | Plain `String` column |

**`PublicCatalogService`** — Spring `@Service` with its own `EntityManager` bound to the public schema (injected via `@Qualifier("publicEntityManagerFactory")`). All cross-schema catalog reads go through this service. Key methods: `getModel`, `getFeatures`, `modelExists`, `allFeaturesExist`, `getBrand`, `brandExists`.

**Soft-delete only for catalog entities** — `Brand`, `Model`, `Feature` have `isActive` boolean and a `POST /{id}/deactivate` endpoint. No hard-delete endpoint exists; this prevents dangling cross-schema UUID references.

---

## Shared Base Types

| Type | Purpose |
|---|---|
| `Auditable` | `@MappedSuperclass` — `String id` (UUID), `Instant createdAt`, `Instant updatedAt` via JPA auditing |
| `CRUDService<CR, PR, R, ID>` | Generic service interface: `getAll / get / create / update / delete` |
| `ResponseMapper<E, R>` | `toResponse(entity)` |
| `CreateMapper<E, CR, CTX>` | `toEntity(request, context)` |
| `PatchMapper<E, PR, CTX>` | `patchEntity(entity, request, context)` — mutates entity in-place |

---

## Hydration Pattern

Used when a mapper needs resolved JPA entities (FK lookups) before building an entity. Each complex domain has:

1. **`XyzHydrationResolver`** — Spring component; calls repositories, throws on constraint violations.
2. **`XyzHydrationContext`** (Lombok `@Builder`) — plain value object holding resolved references.
3. **`XyzHydrator`** — assembles a `Context` from a request by delegating to the resolver.

The mapper receives both the raw request and the hydrated context. Mappers stay pure (no Spring dependencies); existence/uniqueness checks stay in the resolver.

---

## Entity Relationships

```
[PUBLIC]
AgencyRegistration ──(approve)──► Agency ──< Subscription
                                  Agency >── SubscriptionPlan
                                  Agency ──< User
Brand ──< Model
Feature
CatalogRequest (agency proposes; admin approves → catalog entry created)

[TENANT — one schema per approved agency]
Branch ──< Hub ◄── Vehicle.currentHubId
Vehicle ──(modelId)──► public.car_models      (cross-schema, no DB FK)
Vehicle ──(featureIds)──► public.features     (cross-schema @ElementCollection)
Customer ──< Reservation ──── Payment (1:1)
Reservation >── pickupHub, returnHub (both → Hub)
Reservation >── Vehicle
```

**`Vehicle.status`** defaults to `AVAILABLE`. Set to `PENDING_RELOCATION` when a reservation closes with `returnHub ≠ pickupHub`.

**`Reservation.contractStatus`** is derived from two booleans:
- `PENDING` — both `isDigitallySigned` and `isPhysicallyPrinted` are false
- `PARTIAL_EXECUTION` — exactly one is true
- `FULLY_EXECUTED` — both are true

---

## User Roles

| Role | `agencySlug` | Scope |
|---|---|---|
| `SUPER_ADMIN` | null | Platform-level; accesses admin endpoints; skips tenant routing |
| `AGENCY_OWNER` | set | Full agency access; manages staff and subscription |
| `BRANCH_MANAGER` | set | Scoped to one branch (`branchId` stored on User) |
| `AGENT` | set | Scoped to one branch; creates reservations |

JWT carries `userId`, `role`, `agencySlug`, `branchId`. The `TenantFilter` also checks `Agency.status` live — a `BLOCKED` agency is rejected regardless of token validity.

---

## Subscription Plans

`SubscriptionPlan` is a JPA entity (not an enum) managed by the Master Super-Admin. Seed plans:

| Code | Max Branches | Max Hubs | Max Vehicles |
|---|---|---|---|
| `SAFI` | 1 | 1 | 15 |
| `CHAMIL` | null (unlimited) | null | null |

`null` quota fields mean unlimited. Quota enforcement is centralised in `QuotaService` (calls `assertCanAddBranch/Hub/Vehicle` before each create). Returns `HTTP 403` when a limit is exceeded. Plan lookups are cached with a short TTL.

---

## Agency Lifecycle

`AgencyRegistration` and `Agency` are two separate entities (two lifecycles):

- `AgencyRegistration` — public form submission; states: `PENDING → APPROVED | REJECTED`
- `Agency` — operational tenant; only ever created by atomically approving a registration; states: `APPROVED → BLOCKED`

On approval (single `@Transactional`): creates `Agency`, creates owner `User` (role=`AGENCY_OWNER`), provisions tenant schema via `TenantSchemaProvisioner`, marks registration `APPROVED`.

---

## API Conventions

- Base path: `/api/v1/`
- Admin endpoints: `/api/v1/admin/` (`SUPER_ADMIN` only)
- `POST` → `201 CREATED` returning the created resource
- `PATCH` → `204 NO_CONTENT`
- `DELETE` / deactivate → `204 NO_CONTENT`
- Validation: Bean Validation (`@Valid`) on request bodies; `@ValidEnum` for string-to-enum fields
- Domain errors: `EntityNotFoundException` (→ 404) and `EntityExistsException` (→ 409)
- Quota exceeded: `HTTP 403`
- Enums persisted as `STRING` (`@Enumerated(EnumType.STRING)`)

---

## Key Enums

**Fleet:** `VehicleCategory` (`ECONOMY · COMPACT · MIDSIZE · SUV · LUXURY · VAN`), `VehicleStatus` (`AVAILABLE · RENTED · MAINTENANCE · PENDING_RELOCATION`), `FuelType`, `Transmission`

**Location:** `HubType` (`AIRPORT · TRAIN_STATION · MAIN_OFFICE · PRIVATE_LOT`)

**Reservation:** `ReservationStatus` (`ACTIVE · CLOSED · CANCELLED`), `ContractStatus` (`PENDING · PARTIAL_EXECUTION · FULLY_EXECUTED`), `DepositType` (`CASH · CHEQUE · CREDIT_CARD_PREAUTH`), `DepositStatus` (`ACTIVE_HOLD · RELEASED`), `IdType` (`CIN · PASSPORT`)

**Auth/Agency:** `UserRole`, `AgencyStatus` (`APPROVED · BLOCKED`), `AgencyRegistrationStatus` (`PENDING · APPROVED · REJECTED`), `SubscriptionStatus` (`PENDING_PAYMENT · ACTIVE · EXPIRED · SUSPENDED`), `CatalogRequestType` (`BRAND · MODEL · FEATURE`), `CatalogRequestStatus` (`PENDING · APPROVED · REJECTED`)

---

## Async Document Generation

Heavy PDF generation runs off the main HTTP thread via `@Async` + `ThreadPoolTaskExecutor`:
- `ContractPdfService.generateAsync(reservationId)` — triggered on reservation create
- `InvoicePdfService.generateAsync(reservationId)` — triggered on reservation close
- `SubscriptionInvoicePdfService.generateAsync(subscriptionId)` — triggered on `markAsPaid`

Clients poll `GET /api/v1/reservations/{id}/contract/status` → `{ ready: boolean, url: string | null }`.

---

## Implementation Status (as of docs)

| Module | Status |
|---|---|
| Multi-tenancy infrastructure | Not started |
| Auth & Security (JWT) | Not started |
| Agency registration & management | In progress (entity exists, service/controller pending) |
| Subscription & billing | Not started |
| Catalog (Brand/Model/Feature CRUD) | Mostly done; soft-delete + CatalogRequest pending |
| Fleet (Vehicle CRUD) | Mostly done; needs update for cross-schema UUID pattern |
| Location (Branch/Hub) | Not started |
| Reservations (Customer/Reservation) | Not started |
| Payment & Deposit | Not started |
| Signatures & Contract compliance | Not started |
| Document generation (PDF) | Not started |
| Quota enforcement | Not started |
