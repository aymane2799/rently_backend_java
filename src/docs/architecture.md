# Architecture — KiraDrive Entity Reference

**Stack:** Spring Boot 4.0 · Java 21 · PostgreSQL · Hibernate (schema-per-tenant multi-tenancy)

---

## 1. Shared Base — `Auditable`

All entities extend this `@MappedSuperclass`. Never mapped to its own table.

| Field       | Type      | Column       | Notes                             |
| ----------- | --------- | ------------ | --------------------------------- |
| `id`        | `String`  | `id` (PK)    | UUID, auto-generated              |
| `createdAt` | `Instant` | `created_at` | Set once on insert, never updated |
| `updatedAt` | `Instant` | `updated_at` | Auto-updated on every save        |

---

## 2. Entities

### Schema placement legend
- **[PUBLIC]** — lives in the shared PostgreSQL schema; accessible without a tenant context.
- **[TENANT]** — lives inside the per-agency isolated schema; routed via `X-Tenant-ID`.

---

### 2.1 `Agency` [PUBLIC]

**Table:** `agencies` | **Module:** `agency`

A tenant organisation (car rental agency). Created atomically when an `AgencyRegistration` (§2.15) is approved; never exists in a pending state. `slug` is derived from the agency name and doubles as the PostgreSQL schema name for tenant routing.

| Field            | Type           | Column             | Constraints / Default                    |
|------------------| -------------- |--------------------| ---------------------------------------- |
| `name`           | `String`       | `name`             | NOT NULL, UNIQUE                         |
| `slug`           | `String`       | `slug`             | NOT NULL, UNIQUE — tenant schema key     |
| `rcNumber`       | `String`       | `rc_number`        | NOT NULL, UNIQUE                         |
| `iceNumber`      | `String`       | `ice_number`       | NOT NULL, UNIQUE                         |
| `ifNumber`       | `String`       | `if_number`        | nullable, UNIQUE                         |
| `patente`        | `String`       | `patente`          | nullable, UNIQUE                         |
| `ownerFirstName` | `String`       | `owner_first_name` | NOT NULL                                 |
| `ownerLastName`  | `String`       | `owner_last_name`  | NOT NULL                                 |
| `phone`          | `String`       | `phone`            | NOT NULL                                 |
| `email`          | `String`       | `email`            | NOT NULL, UNIQUE                         |
| `city`           | `String`       | `city`             | NOT NULL                                 |
| `address`        | `String`       | `address`          | nullable                                 |
| `website`        | `String`       | `website`          | nullable                                 |
| `logo`           | `String`       | `logo`             | nullable — file path / URL               |
| `coverImage`     | `String`       | `cover_image`      | nullable — file path / URL               |
| `status`         | `AgencyStatus` | `status`           | NOT NULL, STRING enum, default `APPROVED` |
| `approvedAt`     | `Instant`      | `approved_at`      | NOT NULL — set when `AgencyRegistration` is approved |
| `plan`           | `SubscriptionPlan` | `plan_id`          | nullable, FK → `subscription_plans.id` — assigned on approval |

**State machine**

```
APPROVED ──► BLOCKED
```

> Pre-approval lifecycle (`PENDING → APPROVED | REJECTED`) lives on `AgencyRegistration` (§2.15).

**Relationships**

| Type        | Target             | Notes                                               |
| ----------- | ------------------ | --------------------------------------------------- |
| One-to-Many | `Subscription`     | All subscription periods for this agency            |
| One-to-Many | `User`             | Platform users belonging to this agency (cross-schema ref via `agencySlug`) |
| Many-to-One | `SubscriptionPlan` | Current active plan; FK `plan_id` (LAZY)            |

---

### 2.2 `User` [PUBLIC]

**Table:** `users` | **Module:** `auth`

A platform user account. Super-Admins have no agency affiliation; all other roles belong to exactly one agency. Branch Managers and Desk Counter Agents are additionally scoped to a branch (recorded as a plain `branchId` string — cross-schema FK to the tenant's `branches` table).

| Field          | Type       | Column          | Constraints / Default                                                |
|----------------| ---------- |-----------------|----------------------------------------------------------------------|
| `firstName`    | `String`   | `first_name`    | NOT NULL                                                             |
| `lastName`     | `String`   | `last_name`     | NOT NULL                                                             |
| `email`        | `String`   | `email`         | NOT NULL, UNIQUE                                                     |
| `passwordHash` | `String`   | `password_hash` | NOT NULL                                                             |
| `role`         | `UserRole` | `role`          | NOT NULL, STRING enum                                                |
| `agencySlug`   | `String`   | `agency_slug`   | nullable — null for SUPER_ADMIN; FK-by-convention to `agencies.slug` |
| `branchId`     | `String`   | `branch_id`     | nullable — UUID of branch within tenant schema                       |
| `isActive`     | `boolean`  | `is_active`     | NOT NULL, default `true`                                             |

**Relationships**

| Type        | Target  | Notes                                         |
| ----------- | ------- | --------------------------------------------- |
| Many-to-One | `Agency`| Via `agencySlug` (convention, not hard FK)    |

---

### 2.3 `Subscription` [PUBLIC]

**Table:** `subscriptions` | **Module:** `billing`

Tracks each subscription period for an agency. A new record is created for every renewal cycle. The Super-Admin manually flips `status` to `PAID` after verifying the bank transfer or Cash Plus receipt, which triggers invoice generation.

| Field         | Type               | Column         | Constraints / Default                   |
| ------------- | ------------------ | -------------- | --------------------------------------- |
| `agencySlug`  | `String`           | `agency_slug`  | NOT NULL — FK-by-convention to `agencies.slug` |
| `plan`        | `SubscriptionPlan` | `plan_id`      | NOT NULL, FK → `subscription_plans.id`  |
| `status`      | `SubscriptionStatus` | `status`     | NOT NULL, STRING enum, default `PENDING_PAYMENT` |
| `startDate`   | `LocalDate`        | `start_date`   | NOT NULL                                |
| `endDate`     | `LocalDate`        | `end_date`     | NOT NULL                                |
| `amountDue`   | `BigDecimal`       | `amount_due`   | NOT NULL, precision(10,2)               |
| `paymentMode` | `String`           | `payment_mode` | nullable — e.g. "Bank Transfer RIB", "Cash Plus" |
| `paidAt`      | `Instant`          | `paid_at`      | nullable — set when admin marks PAID    |
| `invoiceUrl`  | `String`           | `invoice_url`  | nullable — permanent PDF download link  |

**Relationships**

| Type        | Target             | Notes                                  |
| ----------- | ------------------ | -------------------------------------- |
| Many-to-One | `Agency`           | Via `agencySlug`                       |
| Many-to-One | `SubscriptionPlan` | The plan this period was purchased on  |

---

### 2.4 `SubscriptionPlan` [PUBLIC]

**Table:** `subscription_plans` | **Module:** `billing`

A configurable plan record managed exclusively by the Super-Admin. Replacing the former `SubscriptionPlan` enum allows admins to create new tiers, adjust pricing or quotas, and activate/deactivate plans at runtime without a code deployment. Agencies and subscriptions hold a FK to this entity; quota enforcement reads from it at runtime (cached).

| Field          | Type         | Column           | Constraints / Default         |
| -------------- | ------------ | ---------------- | ----------------------------- |
| `code`         | `String`     | `code`           | NOT NULL, UNIQUE, len≤50 — machine key (e.g., `SAFI`, `CHAMIL`) |
| `displayName`  | `String`     | `display_name`   | NOT NULL, len≤100             |
| `description`  | `String`     | `description`    | nullable, TEXT                |
| `priceMonthly` | `BigDecimal` | `price_monthly`  | NOT NULL, precision(10,2)     |
| `priceYearly`  | `BigDecimal` | `price_yearly`   | NOT NULL, precision(10,2)     |
| `maxBranches`  | `Integer`    | `max_branches`   | NOT NULL — `null` means unlimited |
| `maxHubs`      | `Integer`    | `max_hubs`       | NOT NULL — `null` means unlimited |
| `maxVehicles`  | `Integer`    | `max_vehicles`   | NOT NULL — `null` means unlimited |
| `isActive`     | `boolean`    | `is_active`      | NOT NULL, default `true`      |

> Quota fields use `Integer` (nullable) rather than a sentinel value like `-1` so that `null` unambiguously means "unlimited" in application logic.

**Relationships**

| Type        | Target         | Notes                                  |
| ----------- | -------------- | -------------------------------------- |
| One-to-Many | `Agency`       | Agencies currently on this plan        |
| One-to-Many | `Subscription` | Historical subscription periods on this plan |

---

### 2.5 `Brand` [PUBLIC]

**Table:** `car_brands` | **Module:** `catalog`

Represents an automobile manufacturer (e.g., Toyota, Renault). Shared across all tenants. Agencies may request new brands via `CatalogRequest` (§2.8).

| Field  | Type     | Column | Constraints               |
| ------ | -------- | ------ | ------------------------- |
| `name` | `String` | `name` | NOT NULL, UNIQUE, len≤100 |

**Relationships**

| Type        | Target           | FK               | Notes                                         |
| ----------- | ---------------- | ---------------- | --------------------------------------------- |
| One-to-Many | `Model`          | `model.brand_id` | —                                             |
| One-to-Many | `CatalogRequest` | —                | Requests referencing this brand as parent     |

---

### 2.6 `Model` [PUBLIC]

**Table:** `car_models` | **Module:** `catalog`

A specific car model belonging to a brand (e.g., Toyota Corolla — Sedan). Shared across all tenants. Agencies may request new models via `CatalogRequest` (§2.8).

| Field      | Type              | Column     | Constraints                    |
| ---------- | ----------------- | ---------- | ------------------------------ |
| `name`     | `String`          | `name`     | NOT NULL, len≤100              |
| `category` | `VehicleCategory` | `category` | NOT NULL, STRING enum, len≤50  |
| `brand`    | `Brand`           | `brand_id` | NOT NULL, FK → `car_brands.id` |

**Unique constraint:** `(brand_id, name)`

**Named entity graphs**
- `Model.brand` — eagerly loads the `brand` association

**Relationships**

| Type        | Target    | FK                 | Notes   |
| ----------- | --------- | ------------------ | ------- |
| Many-to-One | `Brand`   | `brand_id`         | LAZY    |
| One-to-Many | `Vehicle` | `vehicle.model_id` | cross-schema ref (no DB FK) |

---

### 2.7 `Feature` [PUBLIC]

**Table:** `features` | **Module:** `catalog`

A vehicle amenity tag (e.g., "GPS", "Heated Seats", "Bluetooth"). Shared across all tenants. Agencies may request new features via `CatalogRequest` (§2.8).

| Field         | Type     | Column        | Constraints               |
| ------------- | -------- | ------------- | ------------------------- |
| `name`        | `String` | `name`        | NOT NULL, UNIQUE, len≤100 |
| `icon`        | `String` | `icon`        | nullable, len≤100         |
| `description` | `String` | `description` | nullable, TEXT            |

**Relationships**

| Type         | Target    | Join table         | Notes                                         |
| ------------ | --------- | ------------------ | --------------------------------------------- |
| Many-to-Many | `Vehicle` | `vehicle_features` | Join table lives in tenant schema; no DB FK enforced across schemas |

---

### 2.8 `CatalogRequest` [PUBLIC]

**Table:** `catalog_requests` | **Module:** `catalog`

An agency-initiated request to add a new Brand, Model, or Feature to the shared catalog. Reviewed and actioned by the Super-Admin. On approval the item is created in the shared catalog and becomes immediately available to all tenants.

| Field                 | Type                   | Column                  | Constraints / Default                                |
| --------------------- | ---------------------- | ----------------------- | ---------------------------------------------------- |
| `agencySlug`          | `String`               | `agency_slug`           | NOT NULL — submitting agency                         |
| `type`                | `CatalogRequestType`   | `type`                  | NOT NULL, STRING enum                                |
| `status`              | `CatalogRequestStatus` | `status`                | NOT NULL, STRING enum, default `PENDING`             |
| `proposedName`        | `String`               | `proposed_name`         | NOT NULL, len≤100                                    |
| `proposedBrandId`     | `String`               | `proposed_brand_id`     | nullable — for MODEL requests: FK → `car_brands.id` if the parent brand already exists |
| `proposedBrandName`   | `String`               | `proposed_brand_name`   | nullable — for MODEL requests: new brand name when brand also needs creating |
| `proposedCategory`    | `VehicleCategory`      | `proposed_category`     | nullable — for MODEL requests                        |
| `proposedIcon`        | `String`               | `proposed_icon`         | nullable — for FEATURE requests, len≤100             |
| `proposedDescription` | `String`               | `proposed_description`  | nullable, TEXT — for FEATURE requests                |
| `notes`               | `String`               | `notes`                 | nullable, TEXT — agency's justification              |
| `rejectionReason`     | `String`               | `rejection_reason`      | nullable, TEXT — admin's reason when rejected        |
| `submittedAt`         | `Instant`              | `submitted_at`          | NOT NULL                                             |
| `reviewedAt`          | `Instant`              | `reviewed_at`           | nullable — set on approve or reject                  |
| `reviewedBy`          | `String`               | `reviewed_by`           | nullable — UUID of the admin `User`                  |
| `resolvedEntityId`    | `String`               | `resolved_entity_id`    | nullable — UUID of the created catalog entity on approval |

**State machine**

```
PENDING ──► APPROVED  (resolvedEntityId populated; catalog entry created)
   │
   └──► REJECTED  (rejectionReason required)
```

**Relationships**

| Type        | Target  | Notes                                        |
| ----------- | ------- | -------------------------------------------- |
| Many-to-One | `Brand` | Via `proposedBrandId` (nullable, LAZY)       |

---

### 2.9 `Vehicle` [TENANT]

**Table:** `vehicles` | **Module:** `fleet`

A physical car asset belonging to a tenant's fleet.

| Field                | Type            | Column                 | Constraints / Default                      |
| -------------------- | --------------- | ---------------------- | ------------------------------------------ |
| `licensePlate`       | `String`        | `license_plate`        | NOT NULL, UNIQUE, len≤20                   |
| `insuranceNumber`    | `String`        | `insurance_number`     | NOT NULL, UNIQUE, len≤20                   |
| `insuranceExpiresAt` | `LocalDate`     | `insurance_expires_at` | NOT NULL                                   |
| `year`               | `Short`         | `year`                 | nullable, SMALLINT                         |
| `month`              | `Short`         | `month`                | nullable, SMALLINT                         |
| `color`              | `String`        | `color`                | nullable, len≤50                           |
| `mileage`            | `int`           | `mileage`              | NOT NULL, default `0`                      |
| `seats`              | `Short`         | `seats`                | nullable, SMALLINT                         |
| `doors`              | `Short`         | `doors`                | nullable, SMALLINT                         |
| `description`        | `String`        | `description`          | nullable, TEXT                             |
| `status`             | `VehicleStatus` | `status`               | NOT NULL, STRING enum, default `AVAILABLE` |
| `transmission`       | `Transmission`  | `transmission`         | nullable, STRING enum, len≤20              |
| `fuelType`           | `FuelType`      | `fuel_type`            | nullable, STRING enum, len≤20              |
| `dailyBaseRate`      | `BigDecimal`    | `daily_base_rate`      | nullable, precision(10,2)                  |
| `model`              | `Model`         | `model_id`             | NOT NULL, FK → `car_models.id`             |
| `features`           | `Set<Feature>`  | `vehicle_features`     | ManyToMany join table                      |
| `currentHub`         | `Hub`           | `current_hub_id`       | nullable, FK → `hubs.id` — updated on every status change to `AVAILABLE` or `RETURNED` |
| `currentParkingSlot` | `String`        | `current_parking_slot` | nullable, len≤100 — free-text coordinate (e.g., "Row G, Spot 14") |

**Named entity graphs**
- `Vehicle.details` — eagerly loads `model` (with `brand`) + `features`

**Join table: `vehicle_features`** _(lives in tenant schema)_

| Column       | References                              |
| ------------ | --------------------------------------- |
| `vehicle_id` | `vehicles.id` (tenant schema)           |
| `feature_id` | `public.features.id` (public schema)    |

> No database-level FK is enforced on `feature_id` because PostgreSQL does not support cross-schema FK constraints with Hibernate multi-tenancy. Referential integrity is enforced at the application layer. Same applies to `model_id`.

**Relationships**

| Type         | Target    | FK / Join table    | Notes                         |
| ------------ | --------- | ------------------ | ----------------------------- |
| Many-to-One  | `Model`   | `model_id`         | LAZY — cross-schema, no DB FK |
| Many-to-One  | `Hub`     | `current_hub_id`   | LAZY                          |
| Many-to-Many | `Feature` | `vehicle_features` | LAZY — cross-schema, no DB FK |

---

### 2.10 `Branch` [TENANT]

**Table:** `branches` | **Module:** `location`

A physical office or regional operation centre of the agency. Plan Safi tenants are capped at 1 branch; Plan Chamil tenants are unlimited.

| Field     | Type     | Column    | Constraints               |
| --------- | -------- | --------- | ------------------------- |
| `name`    | `String` | `name`    | NOT NULL, UNIQUE, len≤150 |
| `city`    | `String` | `city`    | NOT NULL                  |
| `address` | `String` | `address` | nullable                  |
| `phone`   | `String` | `phone`   | nullable                  |
| `isActive`| `boolean`| `is_active` | NOT NULL, default `true` |

**Relationships**

| Type        | Target | FK               | Notes  |
| ----------- | ------ | ---------------- | ------ |
| One-to-Many | `Hub`  | `hub.branch_id`  | LAZY   |

---

### 2.11 `Hub` [TENANT]

**Table:** `hubs` | **Module:** `location`

An operational location hub attached to a branch (e.g., "Airport Terminal 1", "Train Station Lot"). Vehicles are parked at hubs. Plan Safi tenants are capped at 1 hub.

| Field      | Type      | Column      | Constraints                    |
| ---------- | --------- | ----------- | ------------------------------ |
| `name`     | `String`  | `name`      | NOT NULL, len≤150              |
| `type`     | `HubType` | `type`      | NOT NULL, STRING enum          |
| `city`     | `String`  | `city`      | NOT NULL                       |
| `address`  | `String`  | `address`   | nullable                       |
| `isActive` | `boolean` | `is_active` | NOT NULL, default `true`       |
| `branch`   | `Branch`  | `branch_id` | NOT NULL, FK → `branches.id`   |

**Relationships**

| Type        | Target    | FK                   | Notes  |
| ----------- | --------- | -------------------- | ------ |
| Many-to-One | `Branch`  | `branch_id`          | LAZY   |
| One-to-Many | `Vehicle` | `vehicle.current_hub_id` | — |

---

### 2.12 `Customer` [TENANT]

**Table:** `customers` | **Module:** `reservation`

A rental client. Identity is validated at checkout (AC-2.1). A single `idType` + `idNumber` pair stores the identity document — one source of truth, no nullable-pair ambiguity.

| Field               | Type     | Column                | Constraints                              |
|---------------------| -------- |-----------------------| ---------------------------------------- |
| `firstName`         | `String` | `first_name`          | NOT NULL                                 |
| `lastName`          | `String` | `last_name`           | NOT NULL                                 |
| `phone`             | `String` | `phone`               | NOT NULL                                 |
| `email`             | `String` | `email`               | nullable                                 |
| `idType`            | `IdType` | `id_type`             | NOT NULL, STRING enum                    |
| `idNumber`          | `String` | `id_number`           | NOT NULL                                 |
| `driverLicenseCode` | `String` | `driver_license_code` | NOT NULL — Permis de Conduire            |
| `address`           | `String` | `address`             | nullable                                 |

**Unique constraint:** `(id_type, id_number)` — same number can exist across different document types

**Relationships**

| Type        | Target        | FK                       | Notes |
| ----------- | ------------- | ------------------------ | ----- |
| One-to-Many | `Reservation` | `reservation.customer_id` | —    |

---

### 2.13 `Reservation` [TENANT]

**Table:** `reservations` | **Module:** `reservation`

A booking that links a customer, a vehicle, pickup/return hubs, and the full compliance lifecycle (payment, deposit, signature, contract status).

| Field                | Type               | Column                | Constraints / Default                                     |
| -------------------- | ------------------ | --------------------- |-----------------------------------------------------------|
| `customer`           | `Customer`         | `customer_id`         | NOT NULL, FK → `customers.id`                             |
| `vehicle`            | `Vehicle`          | `vehicle_id`          | NOT NULL, FK → `vehicles.id`                              |
| `pickupHub`          | `Hub`              | `pickup_hub_id`       | NOT NULL, FK → `hubs.id`                                  |
| `returnHub`          | `Hub`              | `return_hub_id`       | NOT NULL, FK → `hubs.id` — same as pickup if no cross-hub |
| `startDate`          | `LocalDateTime`    | `start_date`          | NOT NULL                                                  |
| `endDate`            | `LocalDateTime`    | `end_date`            | NOT NULL                                                  |
| `status`             | `ReservationStatus`| `status`              | NOT NULL, STRING enum, default `ACTIVE`                   |
| `totalAmount`        | `BigDecimal`       | `total_amount`        | NOT NULL, precision(10,2)                                 |
| `isDigitallySigned`  | `boolean`          | `is_digitally_signed` | NOT NULL, default `false`                                 |
| `isPhysicallyPrinted`| `boolean`          | `is_physically_printed` | NOT NULL, default `false`                                 |
| `signatureBase64`    | `String`           | `signature_base64`    | nullable, TEXT — compressed Base64 PNG vector             |
| `contractStatus`     | `ContractStatus`   | `contract_status`     | NOT NULL, STRING enum, default `PENDING`                  |
| `createdBy`          | `String`           | `created_by`          | NOT NULL — UUID of the `User` who created                 |

**Derived rule:** if `returnHub ≠ pickupHub` upon close, set `vehicle.status = PENDING_RELOCATION`.

**Relationships**

| Type        | Target    | FK                    | Notes  |
| ----------- | --------- | --------------------- | ------ |
| Many-to-One | `Customer`| `customer_id`         | LAZY   |
| Many-to-One | `Vehicle` | `vehicle_id`          | LAZY   |
| Many-to-One | `Hub`     | `pickup_hub_id`       | LAZY   |
| Many-to-One | `Hub`     | `return_hub_id`       | LAZY   |
| One-to-One  | `Payment` | `payment.reservation_id` | —   |

---

### 2.14 `Payment` [TENANT]

**Table:** `payments` | **Module:** `reservation`

Full payment and deposit breakdown for a single reservation (AC-4.1, AC-4.2). One-to-one with `Reservation`.

| Field                    | Type           | Column                      | Constraints / Default                                      |
| ------------------------ | -------------- | --------------------------- |------------------------------------------------------------|
| `reservation`            | `Reservation`  | `reservation_id`            | NOT NULL, UNIQUE, FK → `reservations.id`                   |
| `totalContractAmount`    | `BigDecimal`   | `total_contract_amount`     | NOT NULL, precision(10,2)                                  |
| `cashAdvanced`           | `BigDecimal`   | `cash_advanced`             | NOT NULL, default `0`, precision(10,2)                     |
| `bankTransferReference`  | `String`       | `bank_transfer_reference`   | nullable                                                   |
| `bankTransferImageUrl`   | `String`       | `bank_transfer_image_url`   | nullable — compressed upload URL                           |
| `depositType`            | `DepositType`  | `deposit_type`              | NOT NULL, STRING enum                                      |
| `depositAmount`          | `BigDecimal`   | `deposit_amount`            | NOT NULL, precision(10,2)                                  |
| `depositStatus`          | `DepositStatus`| `deposit_status`            | NOT NULL, STRING enum, default `ACTIVE_HOLD`               |
| `chequeNumber`           | `String`       | `cheque_number`             | nullable — required if `depositType = CHEQUE`              |
| `creditCardAuthReference`| `String`       | `credit_card_auth_reference`| nullable — required if `depositType = CREDIT_CARD_PREAUTH` |
| `depositReleasedAt`      | `Instant`      | `deposit_released_at`       | nullable — set when manager releases hold                  |
| `depositReleasedBy`      | `String`       | `deposit_released_by`       | nullable — UUID of releasing `User`                        |

**Relationships**

| Type        | Target        | FK               | Notes      |
| ----------- | ------------- | ---------------- | ---------- |
| One-to-One  | `Reservation` | `reservation_id` | Owner side |

---

### 2.15 `AgencyRegistration` [PUBLIC]

**Table:** `agency_registrations` | **Module:** `agency`

A pending application to onboard a new agency, submitted publicly without authentication. Holds all agency and owner information needed to bootstrap both the `Agency` and the `AGENCY_OWNER` `User` on approval. On admin approval these two records are created atomically, the registration is marked `APPROVED`, and `resolvedAgencyId` is populated — making the registration a permanent audit trail of the original application.

| Field              | Type                       | Column               | Constraints / Default                                          |
|--------------------| -------------------------- |----------------------| -------------------------------------------------------------- |
| `agencyName`       | `String`                   | `agency_name`        | NOT NULL                                                       |
| `rcNumber`         | `String`                   | `rc_number`          | NOT NULL, UNIQUE                                               |
| `iceNumber`        | `String`                   | `ice_number`         | NOT NULL, UNIQUE                                               |
| `ifNumber`         | `String`                   | `if_number`          | nullable                                                       |
| `patente`          | `String`                   | `patente`            | nullable                                                       |
| `city`             | `String`                   | `city`               | NOT NULL                                                       |
| `address`          | `String`                   | `address`            | nullable                                                       |
| `website`          | `String`                   | `website`            | nullable                                                       |
| `ownerFirstName`   | `String`                   | `owner_first_name`   | NOT NULL                                                       |
| `ownerLastName`    | `String`                   | `owner_last_name`    | NOT NULL                                                       |
| `ownerEmail`       | `String`                   | `owner_email`        | NOT NULL, UNIQUE                                               |
| `ownerPhone`       | `String`                   | `owner_phone`        | NOT NULL                                                       |
| `status`           | `AgencyRegistrationStatus` | `status`             | NOT NULL, STRING enum, default `PENDING`                       |
| `rejectionReason`  | `String`                   | `rejection_reason`   | nullable, TEXT                                                 |
| `submittedAt`      | `Instant`                  | `submitted_at`       | NOT NULL — set on submission                                   |
| `reviewedAt`       | `Instant`                  | `reviewed_at`        | nullable — set on approve or reject                            |
| `reviewedBy`       | `String`                   | `reviewed_by`        | nullable — UUID of the admin `User`                            |
| `resolvedAgencyId` | `String`                   | `resolved_agency_id` | nullable — UUID of the created `Agency` on approval            |

**State machine**

```
PENDING ──► APPROVED  (Agency + User created atomically; resolvedAgencyId populated)
   │
   └──► REJECTED  (rejectionReason required; re-submission opens a new PENDING record)
```

**Relationships**

None — `AgencyRegistration` is a self-contained application record with no JPA associations. `resolvedAgencyId` is a plain UUID string reference to the created `Agency`.

---

## 3. Enums

### Catalog & Fleet

**`VehicleCategory`**
Values: `ECONOMY` · `COMPACT` · `MIDSIZE` · `SUV` · `LUXURY` · `VAN`
Used by: `Model.category`

**`CatalogRequestType`**
Values: `BRAND` · `MODEL` · `FEATURE`
Used by: `CatalogRequest.type`

**`CatalogRequestStatus`**
Values: `PENDING` · `APPROVED` · `REJECTED`
Used by: `CatalogRequest.status`

**`VehicleStatus`**
Values: `AVAILABLE` · `RENTED` · `MAINTENANCE` · `PENDING_RELOCATION`
Used by: `Vehicle.status`
> `PENDING_RELOCATION` is set automatically when a reservation closes with a return hub different from the pickup hub (AC-2.2).

**`FuelType`**
Values: `DIESEL` · `GASOLINE` · `ELECTRIC` · `HYBRID`
Used by: `Vehicle.fuelType`

**`Transmission`**
Values: `MANUAL` · `AUTOMATIC`
Used by: `Vehicle.transmission`

---

### Location

**`HubType`**
Values: `AIRPORT` · `TRAIN_STATION` · `MAIN_OFFICE` · `PRIVATE_LOT`
Used by: `Hub.type`

---

### Reservation & Compliance

**`ReservationStatus`**
Values: `ACTIVE` · `CLOSED` · `CANCELLED`
Used by: `Reservation.status`

**`ContractStatus`**
Values: `PENDING` · `PARTIAL_EXECUTION` · `FULLY_EXECUTED`
Used by: `Reservation.contractStatus`
> Derived from `isDigitallySigned` + `isPhysicallyPrinted`:
> - `PENDING` — both false
> - `PARTIAL_EXECUTION` — exactly one is true
> - `FULLY_EXECUTED` — both true

**`DepositType`**
Values: `CASH` · `CHEQUE` · `CREDIT_CARD_PREAUTH`
Used by: `Payment.depositType`

**`DepositStatus`**
Values: `ACTIVE_HOLD` · `RELEASED`
Used by: `Payment.depositStatus`

---

### Auth & Agency

**`AgencyStatus`**
Values: `APPROVED` · `BLOCKED`
Used by: `Agency.status`

**`AgencyRegistrationStatus`**
Values: `PENDING` · `APPROVED` · `REJECTED`
Used by: `AgencyRegistration.status`

**`UserRole`**
Values: `SUPER_ADMIN` · `AGENCY_OWNER` · `BRANCH_MANAGER` · `AGENT`
Used by: `User.role`

**`SubscriptionStatus`**
Values: `PENDING_PAYMENT` · `ACTIVE` · `EXPIRED` · `SUSPENDED`
Used by: `Subscription.status`

> `SubscriptionPlan` is now a **managed entity** (§2.4), not an enum. Plan codes (`SAFI`, `CHAMIL`, etc.) are stored in the `subscription_plans` table and configurable by the Super-Admin.

---

### Customer

**`IdType`**
Values: `CIN` · `PASSPORT`
Used by: `Customer.idType`
> Combined with `idNumber` as a unique pair — eliminates the two-column nullable pattern.

---

## 4. Entity Relationship Overview

```
[PUBLIC SCHEMA]
─────────────────────────────────────────────────────────────────────

  AgencyRegistration ──► Agency (on approval, atomic)
                          │
  Agency ──< Subscription │      SubscriptionPlan ──< Agency
    │                     │                       ──< Subscription
    └── User (agencySlug ref)

  Brand ──< Model               (shared catalog)
  Feature                       (shared catalog)

  CatalogRequest ◄── Agency     (agency proposes; admin approves → catalog entry created)


[TENANT SCHEMA — one per approved agency]
─────────────────────────────────────────────────────────────────────

  Branch ──< Hub ◄── Vehicle.currentHub

  Vehicle >──(model_id)──► public.Model     (cross-schema, no DB FK)
  Vehicle >──< vehicle_features ──► public.Feature  (cross-schema join table)

  Customer ──< Reservation ──── Payment (1:1)
                  │   │
           pickupHub  returnHub (both → Hub)
                  │
               Vehicle


Full entity boxes:

[PUBLIC]

┌──────────┐    ┌──────────────────────┐
│  Brand   │1──N│        Model         │
│──────────│    │──────────────────────│
│ name     │    │ name / category      │
└────┬─────┘    │ brand_id (FK)        │
     │          └──────────────────────┘
     │  proposed_brand_id (nullable)
     │
┌────▼──────────────────────────────────┐
│             CatalogRequest            │
│───────────────────────────────────────│
│ agencySlug / type (BRAND|MODEL|FEATURE│
│ status (PENDING|APPROVED|REJECTED)    │
│ proposedName                          │
│ proposedBrandId / proposedBrandName   │
│ proposedCategory / proposedIcon       │
│ proposedDescription / notes           │
│ rejectionReason                       │
│ submittedAt / reviewedAt / reviewedBy │
│ resolvedEntityId                      │
└───────────────────────────────────────┘

┌─────────────────────┐
│       Feature       │
│─────────────────────│
│ name / icon / desc  │
└─────────────────────┘

┌──────────────────────────────────────┐
│          AgencyRegistration          │
│──────────────────────────────────────│
│ agencyName / rcNumber / iceNumber    │
│ ifNumber / patente / city / address  │
│ website                              │
│ ownerFirstName / ownerLastName /     │
│  ownerEmail                          │
│ ownerPhone                           │
│ status (AgencyRegistrationStatus)    │
│ rejectionReason                      │
│ submittedAt / reviewedAt / reviewedBy│
│ resolvedAgencyId (ref → Agency.id)   │
└──────────────────────────────────────┘
         │ on APPROVED: creates atomically
         ▼
┌────────────────────────────┐    ┌──────────────────────────────────┐
│           Agency           │N──1│         SubscriptionPlan         │
│────────────────────────────│    │──────────────────────────────────│
│ name / slug                │    │ code (e.g. SAFI, CHAMIL)         │
│ rcNumber / iceNumber       │    │ displayName / description        │
│ ifNumber / patente         │    │ priceMonthly / priceYearly       │
│ phone                      │    │ maxBranches / maxHubs            │
│ email / city               │    │ maxVehicles / isActive           │
│ status (AgencyStatus)      │    └──────────────┬───────────────────┘
│ approvedAt                 │                   │ 1
│ plan_id (FK) ──────────────┘            ┌─────┘
                                          │ N
└────────────────────────────┐   ┌────────▼─────────────────────────┐
                             │   │          Subscription            │
                             │   │──────────────────────────────────│
                             │   │ agencySlug / plan_id (FK)        │
                             │   │ status (SubscriptionStatus)      │
                             │   │ startDate / endDate              │
                             │   │ amountDue / paymentMode          │
                             │   │ paidAt / invoiceUrl              │
                             │   └──────────────────────────────────┘
                             │
              ┌──────────────▼───────────────────┐
              │               User               │
              │──────────────────────────────────│
              │ firstName / lastName / email /   │
              │ passwordHash                     │
              │ role (UserRole)                  │
              │ agencySlug (ref, nullable)       │
              │ branchId (UUID ref, nullable)    │
              │ isActive                         │
              └──────────────────────────────────┘

[TENANT]

┌──────────┐    ┌─────────────────┐
│  Branch  │1──N│       Hub       │
│──────────│    │─────────────────│
│ name     │    │ name / city     │
│ city     │    │ type (HubType)  │
│ isActive │    │ branch_id (FK)  │
└──────────┘    │ isActive        │
                └────────┬────────┘
                         │ currentHub_id / pickupHub / returnHub
        ┌────────────────┼─────────────────────────────┐
        │                │                             │
┌───────▼──────────────────────────────┐               │
│               Vehicle                │               │
│──────────────────────────────────────│               │
│ licensePlate / insuranceNumber       │               │
│ insuranceExpiresAt                   │               │
│ year / month / color / mileage       │               │
│ seats / doors / dailyBaseRate        │               │
│ fuelType / transmission              │               │
│ status (VehicleStatus)               │               │
│ model_id ──────────────────────────► public.Model    │
│ currentHub_id (FK)                   │               │
│ currentParkingSlot                   │               │
└────────────────┬─────────────────────┘               │
                 │ ManyToMany (cross-schema join table) │
        ┌────────▼────────┐                            │
        │ vehicle_features│                            │
        └────────┬────────┘                            │
                 │                                     │
                 ▼                                     │
         public.Feature                                │
                                                       │
┌──────────────────┐    ┌──────────────────────────────▼──────────┐
│     Customer     │1──N│              Reservation                │
│──────────────────│    │─────────────────────────────────────────│
│ firstName/lastName│   │ customer_id / vehicle_id                │
│ phone / email    │    │ pickupHub_id / returnHub_id             │
│ idType (IdType)  │    │ startDate / endDate                     │
│ idNumber         │    │ status (ReservationStatus)              │
│ driverLicenseCode│    │ totalAmount                             │
│ address          │    │ isDigitallySigned / isPhysicallyPrinted │
└──────────────────┘    │ signatureBase64                         │
                        │ contractStatus (ContractStatus)         │
                        │ createdBy (User UUID)                   │
                        └───────────────┬─────────────────────────┘
                                        │ 1:1
                        ┌───────────────▼─────────────────────────┐
                        │                Payment                  │
                        │─────────────────────────────────────────│
                        │ totalContractAmount / cashAdvanced      │
                        │ bankTransferReference / imageUrl        │
                        │ depositType / depositAmount             │
                        │ depositStatus (DepositStatus)           │
                        │ chequeNumber / creditCardAuthRef        │
                        │ depositReleasedAt / depositReleasedBy   │
                        └─────────────────────────────────────────┘
```

---

## 5. Multi-Tenancy Notes

- `Agency.slug` is used as the PostgreSQL schema name for tenant isolation.
- **Public schema entities** — no tenant context required: `Agency`, `User`, `Subscription`, `SubscriptionPlan`, `Brand`, `Model`, `Feature`, `CatalogRequest`.
- **Tenant schema entities** — routed via `X-Tenant-ID` header → `CurrentTenantIdentifierResolver` → `MultiTenantConnectionProvider`: `Vehicle`, `Branch`, `Hub`, `Customer`, `Reservation`, `Payment`.
- Schema provisioning is triggered atomically when an agency transitions `PENDING → APPROVED`.
- **Cross-schema references** are stored as plain UUID strings (not JPA `@ManyToOne`) to avoid cross-schema FK constraint violations. This applies to:
  - `User.agencySlug` / `User.branchId`
  - `Vehicle.model_id` → `public.car_models.id`
  - `vehicle_features.feature_id` → `public.features.id`
- **Catalog integrity** for the cross-schema references is enforced at the application layer (service-level existence checks before write), not at the database layer.

## 6. Cross-Schema Access Pattern

Brand, Model, and Feature live in the public schema. Tenant-schema entities (`Vehicle`, `Reservation`) reference them by UUID string only — no JPA `@ManyToOne` or `@ManyToMany` crosses the schema boundary, because Hibernate's multi-tenancy routing cannot resolve public-schema entities through the tenant `EntityManager`.

### 6.1 `PublicCatalogService`

A dedicated Spring service with its own `EntityManager` permanently bound to the public schema — bypasses `CurrentTenantIdentifierResolver` entirely. All cross-schema catalog reads go through this service; nothing else accesses it directly.

```java
@Service
public class PublicCatalogService {

    private final EntityManager publicEm; // injected with public-schema EMF qualifier

    public Model getModel(String modelId) {
        Model model = publicEm.find(Model.class, modelId);
        if (model == null) throw new EntityNotFoundException("Model not found: " + modelId);
        return model;
    }

    public List<Feature> getFeatures(Set<String> featureIds) {
        if (featureIds.isEmpty()) return List.of();
        return publicEm
            .createQuery("SELECT f FROM Feature f WHERE f.id IN :ids", Feature.class)
            .setParameter("ids", featureIds)
            .getResultList();
    }

    public boolean modelExists(String modelId) {
        return publicEm.find(Model.class, modelId) != null;
    }

    public boolean allFeaturesExist(Set<String> featureIds) {
        if (featureIds.isEmpty()) return true;
        Long count = publicEm
            .createQuery("SELECT COUNT(f) FROM Feature f WHERE f.id IN :ids", Long.class)
            .setParameter("ids", featureIds)
            .getSingleResult();
        return count == featureIds.size();
    }
}
```

### 6.2 JPA mapping on the tenant side

`model_id` is a plain `String` column. `featureIds` uses `@ElementCollection` so JPA manages the `vehicle_features` join table as a collection of scalar values — no entity relationship, no cross-schema navigation.

```java
@Entity
@Table(name = "vehicles")
public class Vehicle extends Auditable {

    @Column(name = "model_id", nullable = false)
    private String modelId;  // UUID ref → public.car_models.id

    @ElementCollection
    @CollectionTable(
        name = "vehicle_features",
        joinColumns = @JoinColumn(name = "vehicle_id")
    )
    @Column(name = "feature_id")
    private Set<String> featureIds = new HashSet<>();  // UUID refs → public.features.id

    // ... other fields
}
```

### 6.3 Service layer usage

```java
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepo;
    private final PublicCatalogService catalog;

    public VehicleResponse getVehicle(String id) {
        Vehicle vehicle = vehicleRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + id));

        Model model     = catalog.getModel(vehicle.getModelId());
        List<Feature> features = catalog.getFeatures(vehicle.getFeatureIds());

        return VehicleMapper.toResponse(vehicle, model, features);
    }

    public Vehicle createVehicle(CreateVehicleRequest req) {
        // existence checks before write — enforces integrity app-side
        if (!catalog.modelExists(req.getModelId()))
            throw new EntityNotFoundException("Model not found: " + req.getModelId());
        if (!catalog.allFeaturesExist(req.getFeatureIds()))
            throw new EntityNotFoundException("One or more features not found");

        return vehicleRepo.save(VehicleMapper.toEntity(req));
    }
}
```

### 6.4 Soft-delete guarantee

Because there is no DB FK, a deleted catalog item would leave `model_id` / `featureIds` dangling. This is prevented by a hard rule: **Brand, Model, and Feature are never hard-deleted.** The admin can only set `isActive = false`. The `PublicCatalogService` never exposes a delete endpoint; the admin API only exposes a deactivate endpoint. Existing Vehicle references remain valid forever.

### 6.5 Summary of cross-schema references

| Tenant field               | References             | JPA mapping          | Integrity mechanism            |
| -------------------------- | ---------------------- | -------------------- | ------------------------------ |
| `Vehicle.modelId`          | `public.car_models.id` | Plain `String` column | Existence check on write; soft-delete only |
| `Vehicle.featureIds`       | `public.features.id`   | `@ElementCollection`  | Existence check on write; soft-delete only |
| `User.agencySlug`          | `public.agencies.slug` | Plain `String` column | Set once on user creation; slug is immutable after approval |
| `User.branchId`            | tenant `branches.id`   | Plain `String` column | Existence check on write |

---

## 7. Plan Quota Enforcement

Quota limits are read from the `SubscriptionPlan` entity (§2.4) at runtime via the agency's `plan_id` FK. The table below reflects the two seed plans; values are DB-configurable by the Super-Admin without redeployment.

| Constraint      | SAFI (seed) | CHAMIL (seed) | Source field        |
| --------------- | ----------- | ------------- | ------------------- |
| Max branches    | 1           | unlimited     | `maxBranches`       |
| Max hubs        | 1           | unlimited     | `maxHubs`           |
| Max vehicles    | 15          | unlimited     | `maxVehicles`       |
| Multi-branch UI | Hidden      | Visible       | `maxBranches == 1`  |

> `null` in `maxBranches`, `maxHubs`, or `maxVehicles` means unlimited. Quota checks run server-side before every create on `Branch`, `Hub`, or `Vehicle`. Plans should be cached (e.g., Spring Cache + short TTL) to avoid a DB round-trip on every quota check.

---

## 8. Spring Security & JWT — Authentication Architecture

### 8.1 Login Flow (token issuance)

```
Client                       AuthController              UserRepository        JwtTokenProvider
  │                               │                           │                      │
  │  POST /api/v1/auth/login      │                           │                      │
  │  { email, password }          │                           │                      │
  │──────────────────────────────►│                           │                      │
  │                               │  findByEmail(email)       │                      │
  │                               │──────────────────────────►│                      │
  │                               │  User | empty             │                      │
  │                               │◄──────────────────────────│                      │
  │                               │                           │                      │
  │                               │  [guard: user not found]──► 401 UNAUTHORIZED     │
  │                               │  [guard: user inactive] ──► 401 UNAUTHORIZED     │
  │                               │  [guard: wrong password]──► 401 UNAUTHORIZED     │
  │                               │                           │                      │
  │                               │  generateToken(user)                             │
  │                               │─────────────────────────────────────────────────►│
  │                               │  signed JWT                                      │
  │                               │◄─────────────────────────────────────────────────│
  │                               │                                                  │
  │  200 OK                       │                                                  │
  │  { token, userId, role,       │                                                  │
  │    agencySlug, branchId }     │                                                  │
  │◄──────────────────────────────│                                                  │
```

**JWT payload claims:**

| Claim        | Source                   | Notes                                   |
| ------------ | ------------------------ | --------------------------------------- |
| `sub`        | `user.getId()`           | UUID of the `User` record               |
| `role`       | `user.getRole().name()`  | e.g. `AGENCY_OWNER`                     |
| `agencySlug` | `user.getAgencySlug()`   | null for `SUPER_ADMIN`           |
| `branchId`   | `user.getBranchId()`     | null unless `BRANCH_MANAGER` or `AGENT` |
| `iat`        | issue timestamp          | —                                       |
| `exp`        | `iat + jwt.expiration`   | configured via `jwt.expiration` (ms)    |

Signed with **HMAC-SHA** using the key derived from `jwt.secret` (application.yaml).

---

### 8.2 Authenticated Request Lifecycle

Every non-login request passes through the following filter chain before reaching a controller:

```
Incoming HTTP Request
        │
        ▼
┌───────────────────────────────────────────────┐
│         Spring Security Filter Chain          │
│                                               │
│  ┌────────────────────────────────────────┐   │
│  │  1. TenantFilter  (before Security)    │   │  ← reads X-Tenant-ID header
│  │     TenantContext.set(tenantId)        │   │    stores slug in thread-local
│  └────────────────────────────────────────┘   │
│                    │                          │
│                    ▼                          │
│  ┌────────────────────────────────────────┐   │
│  │  2. JwtAuthenticationFilter            │   │  ← OncePerRequestFilter
│  │     extends OncePerRequestFilter       │   │
│  │                                        │   │
│  │  a) Read Authorization header          │   │
│  │     missing / not "Bearer " ──────────────► pass-through (anonymous)
│  │                                        │   │
│  │  b) JwtTokenProvider.isTokenValid()    │   │
│  │     invalid / expired ────────────────────► pass-through (anonymous)
│  │                                        │   │
│  │  c) extractClaims(token) → subject     │   │
│  │     (userId UUID)                      │   │
│  │                                        │   │
│  │  d) UserRepository.findById(userId)    │   │
│  │     not found ─────────────────────────────► pass-through (anonymous)
│  │     user.isActive() == false ──────────────► pass-through (anonymous)
│  │                                        │   │
│  │  e) Build UsernamePasswordAuthToken    │   │
│  │     principal = User entity            │   │
│  │     authority = "ROLE_" + role         │   │
│  │     SecurityContextHolder.set(auth)    │   │
│  └────────────────────────────────────────┘   │
│                    │                          │
│                    ▼                          │
│  ┌────────────────────────────────────────┐   │
│  │  3. UsernamePasswordAuthenticationFilter│  │  ← skipped (stateless; JWT
│  │     (standard Spring, bypassed here)   │   │     filter runs before this)
│  └────────────────────────────────────────┘   │
│                    │                          │
│                    ▼                          │
│  ┌────────────────────────────────────────┐   │
│  │  4. Authorization (SecurityConfig)     │   │
│  │                                        │   │
│  │  /api/v1/auth/**           → permitAll │   │
│  │  POST /api/v1/agencies/register→permitAll  │
│  │  /api/v1/admin/**          →  SUPER_ADMIN  │
│  │  anyRequest                → authenticated │
│  └────────────────────────────────────────┘   │
└───────────────────────────────────────────────┘
        │
        ▼
  Controller method
  (principal available via SecurityContextHolder
   or injected as method parameter)
        │
        ▼
  Response returned; TenantFilter clears TenantContext
```

---

### 8.3 Filter Registration Order

| Order | Filter | Registered via |
| ----- | ------ | -------------- |
| 1 | `TenantFilter` | `@Component` + `OncePerRequestFilter` (auto-registered by Spring Boot) |
| 2 | `JwtAuthenticationFilter` | `addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` in `SecurityConfig` |
| 3 | `UsernamePasswordAuthenticationFilter` | Spring Security default (effectively unused — stateless) |

> Session creation policy is `STATELESS` — Spring Security never creates an `HttpSession`. Every request is fully authenticated from the token alone.

---

### 8.4 Endpoint Access Matrix

| Endpoint pattern | Auth required | Role constraint | Notes |
| ---------------- | ------------- | --------------- | ----- |
| `POST /api/v1/auth/login` | No | — | Token issuance |
| `POST /api/v1/agencies/register` | No | — | Public agency registration form |
| `/api/v1/admin/**` | Yes | `SUPER_ADMIN` | `hasRole()` enforced by `SecurityConfig` |
| All other `/api/v1/**` | Yes | Any authenticated user | Fine-grained roles via `@PreAuthorize` / `@EnableMethodSecurity` |

---

### 8.5 Key Components

| Component | Location | Responsibility |
| --------- | -------- | -------------- |
| `JwtTokenProvider` | `auth/jwt/` | Sign/verify tokens, extract claims |
| `JwtAuthenticationFilter` | `auth/jwt/` | Per-request token validation; populates `SecurityContext` |
| `SecurityConfig` | `config/` | Declares filter chain, URL rules, password encoder bean |
| `AuthController` | `auth/` | `POST /api/v1/auth/login` — credential check → token |
| `TenantFilter` | `config/` (multi-tenancy) | Reads `X-Tenant-ID` header into `TenantContext` thread-local |
| `BCryptPasswordEncoder` | bean in `SecurityConfig` | Password hashing on registration; matching on login |
