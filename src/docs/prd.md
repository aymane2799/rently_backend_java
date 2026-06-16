# Product Requirements Document — KiraDrive (MVP)

**Author:** Senior Product Manager
**Architecture:** Multi-Tenant SaaS (PostgreSQL Schema-per-Tenant)
**Tech Stack:** Java Spring Boot + React.js (Vite / Tailwind CSS / TypeScript)

---

## 1. Problem Statement

Independent and mid-sized car rental agencies in Morocco experience significant operational bottlenecks once their fleets expand past 15 vehicles or distribute across multiple regional offices. Traditional generic ERPs fail to account for the unique infrastructural and transactional nuances of the Moroccan market.

**The Multi-Branch Blindspot**
Corporate owners operating simultaneous hubs (e.g., Casablanca Mohamed V Airport and downtown Marrakech) lack a single source of truth for fleet distribution. Vehicles frequently shift dynamically due to asymmetric customer drop-offs, culminating in booking collisions where cars are digitally rented in one location while physically stranded in another.

**The Location & Parking Puzzle**
Vehicles are rarely stationed directly outside the rental desk. They occupy spaces across public airport infrastructure, transit terminals (e.g., Casa-Voyageurs, Rabat-Agdal), or separate compound lots. Without mapping the physical layout down to explicit parking slots, counter staff lose hours daily cross-calling to locate vehicle keys and coordinates.

**Document and Payment Leakage**
Security deposits (la caution) and cash payments are recorded haphazardly on paper logbooks, leading to major reconciliation and shrink issues. Additionally, manually drafting legal contracts reflecting local compliance norms (patent, ICE, IF, and RC) is highly repetitive and exposes business owners to severe tax and regulatory audit liabilities.

- **Strategic Position Note (Asset Protection vs. Tax Burden):** KiraDrive must be framed entirely as an internal **asset security, parking control, and logistics tracking engine** rather than a strict accounting/tax compliance application. This explicitly accommodates the cash-heavy, informal transaction preference typical of independent Moroccan agencies.

---

## 2. User Roles & System Access Control

| Role                   | Operational Scope                                                                                        | Access Bounds                                                                                                               |
| ---------------------- | -------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| **Super-Admin** | Platform owner oversight; manages system health, tenant activation, and global tier adjustments.         | Master system panel; can flag any tenant status to "Paid". Cannot read customer-identifiable data inside any tenant schema. |
| **Tenant Owner**       | Agency business owner. Full control over corporate configurations, entire regional fleets, and staffing. | Unrestricted schema access, multi-branch revenue data, user configuration matrices.                                         |
| **Branch Manager**     | Regional operations supervisor. Monitors assets, processes exceptions, tracks local staff activity.      | Restricted to the assigned physical branch data pool and regional parking hubs.                                             |
| **Desk Counter Agent** | Frontline operator. Executes bookings, check-ins, and parking slot logging.                              | Single-branch operational dashboard; access to reservation wizard and asset directory. Denied access to system settings.    |

---

## 3. MVP Functional Feature Specifications

### Module 3.1 — Location & Parking Spot Matrix

**Problem:** Frontline staff waste time searching for vehicles across large airport or municipal train parking zones because exact locations are untracked.

**User Stories**

- As a Tenant Owner / Branch Manager, I want to configure specific operational Hubs (e.g., Airport Terminal 1, Train Station Lot) and assign named parking slots, so that my field staff can update asset statuses with exact geographic anchors.
- As a Desk Counter Agent, I want to look up the precise parking coordinate of an available vehicle instantly from my active fleet dashboard, so that I can direct the arriving client to their car without delay.

**Acceptance Criteria**

- **AC-1.1** — The system must validate mandatory schema fields upon Hub creation: Name, Hub Type (Airport, Train Station, Main Office, Private Lot), and City.
- **AC-1.2** — Upon change of any vehicle state to `Available` or `Returned`, the interface must require selection of an active Hub and input of a text-based Parking Spot coordinate (e.g., "Row G, Spot 14").
- **AC-1.3** — If a tenant is provisioned under Plan Safi, the application must programmatically hide multi-branch dropdowns and restrict asset allocations to a maximum of 1 branch and 1 operational hub.

---

### Module 3.2 — Dynamic Reservation Wizard

**Problem:** Inefficient registration processes create booking errors and data gaps that violate local data storage compliance regulations.

**User Stories**

- As a Desk Counter Agent, I want to progress through a structured checkout wizard to log client identity documents, choose multi-hub pickup/return criteria, and block the vehicle, so that I can prevent scheduling overlap.

**Acceptance Criteria**

- **AC-2.1** — The checkout workflow must enforce complete collection of mandatory local identity fields: Moroccan National Identity Card (CIN) or Passport Number, Full Legal Name, Phone Contact, and Driver's License (Permis de Conduire) alphanumeric code.
- **AC-2.2** — The reservation framework must support distinct Pick-up Hub and Return Hub inputs. If the Return Hub differs from the Origin Hub, the vehicle asset state must flag as `Pending Relocation` upon checkout closing.
- **AC-2.3** — Real-time concurrency checks must execute against the database when a vehicle is assigned. The API must return a hard validation error if the proposed date window intersects with any active booking for that specific VIN (Vehicle Identification Number).

---

### Module 3.3 — Two-Tiered Document Generator

**Problem:** Manual contract compilation causes clerical errors and risks financial and legal non-compliance under regional business standards.

**User Stories**

- As a Tenant Owner, I want the system to instantly generate localized, print-ready PDF rental agreements and commercial invoices including our official regional identification numbers (ICE, patent, IF, RC), so that we remain fully audit-compliant.
- As a Tenant Owner, I want to download our official platform-to-tenant SaaS subscription invoices within my settings tab as soon as our manual payment transfer is cleared by the platform admin.

**Acceptance Criteria**

- **AC-3.1** — Customer invoices must render in a clean, standard A4 layout using an HTML-to-PDF template component. The PDF header must parse and display all corporate identifiers: ICE, IF, patent, and RC.
- **AC-3.2** — System-to-Tenant subscription PDFs must compile immediately when the Super-Admin flags a subscription period as "Paid". The document must state the payment mode, subscription period, and tier type, exposed via a permanent download link in the Tenant Settings UI.

---

### Module 3.4 — Manual Payment & Deposit Tracker

**Problem:** Cash advances and uncashed security check deposits are tracked loosely, leading to discrepancies at terminal shift handovers.

**User Stories**

- As an Agency Owner, I want an internal ledger to track the exact payment breakdown and hold status of security deposits (la caution), so that I can audit cash drawer balances and safeguard physical collateral.

**Acceptance Criteria**

- **AC-4.1** — Every booking entry must require a full payment breakdown: Total Contract Amount, Cash Advanced, Bank Transfer Reference (with image attachment placeholder for transfer confirmation screenshots), and Deposit Configuration.
- **AC-4.2** — Deposits must be classified as: **Cash** (held in register), **Cheque** (requiring check serial number), or **Credit Card Pre-Authorization Reference**. The deposit state must remain explicitly marked as `Active Hold` until a manager performs a verification release.

### Module 3.5: Hybrid Signature & Verification Matrix

- **Problem Statement:** Moroccan agencies navigate a split operational environment: they need fast in-app validation on screens to anchor secure digital records, but local security checks and standard legal frameworks require structural, paper physical copies in the glovebox.
- **User Stories:**
  - _As a Desk Counter Agent_, I want to collect a client's digital touch-signature on my device screen while also tracking the status of their physical paper printout, so that both compliance requirements are handled in the same dashboard workflow.
- **Acceptance Criteria:**
  - **AC-5.1:** The checkout screen must present an interactive touch/stylus HTML5 signature canvas. Upon saving, vector coordinate coordinates must be compressed into a Base64 PNG string asset stored within that specific tenant's isolated database space.
  - **AC-5.2:** The reservation row schema must store and maintain two explicit compliance boolean parameters: `is_digitally_signed` and `is_physically_printed`.
  - **AC-5.3:** System generation of the final contract layout must feature a mandatory lifecycle evaluation state indicator:
    - `PENDING`: Both signature parameters evaluate as false.
    - `PARTIAL_EXECUTION`: Only one tracking parameter evaluates as true.
    - `FULLY_EXECUTED`: Both digital signing and physical contract printing states are validated true.
  - **AC-5.4:** When executing physical print actions, the system must render the digital signature vector cleanly inside the PDF footer block adjacent to the required tenant tax variables (**ICE, patent**).

---

## 4. Technical & Architecture Specifications

### 4.1 Multi-Tenant Routing & Compliance

**Schema Isolation**
The system must intercept every HTTP transaction, parse the mandatory `X-Tenant-ID` header, and leverage Hibernate's `MultiTenantConnectionProvider` and `CurrentTenantIdentifierResolver` to establish isolated context routing. No database queries may bleed across schemas.

**Moroccan Law 09-08 (CNDP)**
Platform terms of service must explicitly define data ownership as resting 100% with the Tenant. Customer PII data must reside only within the encrypted tenant schema boundary to simplify future CNDP regulatory declarations.

### 4.2 Network Resilience & Async Processing

**Low-Bandwidth Optimization**
Frontend checkouts occurring in subterranean parking zones or low-connectivity areas must remain operational. The React frontend must perform image compression via the HTML5 Canvas API, scaling multi-megabyte photo uploads (~6MB) down to an optimized ceiling of ~300KB prior to sending multipart POST requests.

**Asynchronous Processing**
Heavy document generation or report assembly must never run on the main HTTP servlet thread. Tasks must be delegated to background processing queues via Spring Boot's `@Async` `ThreadPoolTaskExecutor`. The frontend will verify processing status via a status polling endpoint.

---

## 5. Monetization & Subscription Workflow

Due to credit card billing friction common among small businesses in Morocco, automated recurring billing is omitted from the MVP in favor of a strictly audited manual payment lifecycle.

```
┌─────────────────────────────────────┐
│  System displays RIB payment        │
│  details & warning banner in UI     │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│  Tenant sends manual transfer /     │
│  Cash Plus receipt via WhatsApp     │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│  Super Admin verifies receipt &    │
│  switches Tenant status to PAID     │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│  Banner drops; System generates     │
│  downloadable Platform Invoice      │
└─────────────────────────────────────┘
```

### Tier Quota Structure

| Plan                           | Price                          | Constraints                                                          |
| ------------------------------ | ------------------------------ | -------------------------------------------------------------------- |
| **Plan Safi** (Single-Branch)  | 350 MAD/month · 3,500 MAD/year | 1 branch, 1 location hub, max 15 vehicles                            |
| **Plan Chamil** (Multi-Branch) | 950 MAD/month · 9,000 MAD/year | Unlimited branches, unlimited hub profiles, unrestricted fleet count |

---

### Module 3.6 — Agency Self-Registration & Admin Approval

**Problem:** Platform onboarding is entirely manual today — the Super-Admin creates tenant accounts ad-hoc. This creates a bottleneck as the platform scales and leaves prospective agency owners with no structured intake path.

**User Stories**

- As a prospective Agency Owner, I want to submit a self-service registration form with my agency's business details, so that I can request access to the platform without waiting for a direct admin call.
- As a Super-Admin, I want to see a dashboard of all pending registration requests and individually approve or reject each one with an optional rejection reason, so that I can vet agencies before granting full system access.
- As a prospective Agency Owner, I want to receive a clear status update (approved or rejected) after my registration is reviewed, so that I know whether to proceed with onboarding or address the stated issues.

**Acceptance Criteria**

- **AC-6.1** — The public-facing registration form must collect: Agency Legal Name, RC Number (Registre de Commerce), ICE Number, Owner First Name, Owner Last Name, Business Email, Phone Number, City, and optional Website URL. All fields except Website are mandatory.
- **AC-6.2** — Upon submission, the system must create an `Agency` record with `status = PENDING` and timestamp the `submittedAt` field. The agency must not gain any operational system access while in `PENDING` state.
- **AC-6.3** — The Super-Admin's registration queue must display all `PENDING` agencies sorted by `submittedAt` ascending (oldest first). Each row must surface: Agency Name, RC Number, ICE Number, Email, City, and time elapsed since submission.
- **AC-6.4** — Approving an agency must atomically: set `status = APPROVED`, record `approvedAt` timestamp, provision the tenant's isolated database schema, and trigger a welcome email to the registered business email with first-login credentials.
- **AC-6.5** — Rejecting an agency must: set `status = REJECTED`, record `rejectedAt` timestamp, and persist a mandatory `rejectionReason` string. A notification email must be dispatched to the applicant with the stated reason and an invitation to re-apply.
- **AC-6.6** — A rejected agency may re-submit a new registration request. The system must retain the previous rejected record for audit history and create a fresh `PENDING` record upon re-submission.
- **AC-6.7** — An approved agency that later violates platform terms may be set to `BLOCKED` by the Super-Admin. A `BLOCKED` agency loses all API access and its tenant staff receive an in-app banner notification.

**Registration State Machine**

```
[Public Form Submitted]
         │
         ▼
    ┌─────────┐
    │ PENDING │ ◄─── Re-submission creates a new record
    └────┬────┘
         │
    ┌────┴────┐
    │  Admin  │
    │ Reviews │
    └────┬────┘
    ┌────┴────────────┐
    │                 │
    ▼                 ▼
┌──────────┐    ┌──────────┐
│ APPROVED │    │ REJECTED │ (reason stored)
└────┬─────┘    └──────────┘
     │
     ▼
┌─────────┐
│ BLOCKED │ (admin-triggered, access revoked)
└─────────┘
```

---

### Module 3.7 — Agency Operations Dashboard

**Problem:** Agency owners and managers lack a real-time consolidated view of fleet performance and business health. Decisions about vehicle procurement, pricing, and staffing are currently made from memory or manual counting rather than data.

**User Stories**

- As an Agency Owner / Branch Manager, I want a real-time fleet status breakdown, so that I can instantly assess operational state without calling staff.
- As an Agency Owner, I want to see monthly and yearly reservation counts and revenue totals, so that I can track business performance over time.
- As an Agency Owner, I want to see vehicle category distribution and the most-rented category, so that I can make informed fleet procurement decisions.
- As an Agency Owner / Branch Manager, I want to see a deposit summary (active holds vs released, total value held), so that I can monitor collateral exposure at shift handover.

**Acceptance Criteria**

- **AC-7.1** — The dashboard must display fleet status counts: total vehicles, available, rented, under maintenance, and pending relocation — scoped to the authenticated user's branch for Branch Managers and Agents, or the full agency for Owners.
- **AC-7.2** — The dashboard must display reservation counts (total bookings created) for the current calendar month and the current calendar year, each with a comparison figure for the same period in the prior year.
- **AC-7.3** — The dashboard must display revenue totals (sum of `Payment.totalContractAmount` across `CLOSED` reservations) for the current month and the current year.
- **AC-7.4** — The dashboard must display vehicle category distribution: count per `VehicleCategory` type and the category with the highest number of completed rentals in the current year.
- **AC-7.5** — The dashboard must display a deposit summary: count and total MAD value of deposits with status `ACTIVE_HOLD`, and count of `RELEASED` deposits for the current period.
- **AC-7.6** — The vehicle utilisation rate must be computed as `(total rented vehicle-days ÷ total possible vehicle-days in period) × 100`. The period displayed is the current calendar month.
- **AC-7.7** — All dashboard metrics must be served from a single `GET /api/v1/dashboard` endpoint. The response must include a `generatedAt` timestamp. No new persisted entity is required — all figures are computed at query time.

---

### Module 3.8 — Public Agency Landing Page & Client Portal

**Problem:** Agencies have no digital storefront. Prospective clients must phone or visit in person to check vehicle availability. Agency owners cannot share their fleet online or accept pre-qualified reservation inquiries outside of business hours.

**User Stories**

- As an Agency Owner, I want a public-facing landing page auto-generated for my agency at a stable URL, so that I can share it with clients via link or QR code without building a website.
- As an Agency Owner, I want to customise my landing page with brand colours (light and dark mode variants), a tagline, and SEO metadata, so that the page reflects my business identity.
- As a Client, I want to browse available vehicles on an agency's public page without logging in, so that I can evaluate the fleet before committing.
- As a Client, I want to create a lightweight account (email + phone + password) on the agency's page, so that I can submit reservation requests and track their status.
- As a Client, I want to request a reservation for a specific available vehicle and date range, and to receive confirmation that the vehicle is actually free for those dates before my request is submitted.
- As an Agency Agent / Owner, I want to review incoming client booking requests and confirm or reject each one with an optional reason, so that I control which bookings are formally accepted into the system.
- As a Client, I want to receive an email when my booking request is rejected (with the stated reason), so that I can adjust my request and re-submit.

**Acceptance Criteria**

- **AC-8.1** — The agency landing page must be accessible at `/public/{agency-slug}` without authentication. It must display the agency name, logo, tagline, and apply the agency's brand colours (primary and secondary, with dark mode variants).
- **AC-8.2** — The landing page must list all vehicles with `status = AVAILABLE`, showing the model name, category, daily rate, primary image, and up to five key features.
- **AC-8.3** — The landing page must support real-time vehicle filtering by desired date range, vehicle category, and transmission type. Only vehicles that have no overlapping active reservation or pending `BookingRequest` for the selected dates may appear as available.
- **AC-8.4** — A client must be able to register with email, phone number, and password only. Identity documents are NOT required at account registration.
- **AC-8.5** — When submitting a `BookingRequest`, the client must provide: identity document type (`CIN` or `PASSPORT`), the corresponding ID number, driver's licence code, an uploaded scan or PDF of the identity document, and an uploaded scan or PDF of the driver's licence. All five fields are mandatory. The API must reject the submission with `422 UNPROCESSABLE_ENTITY` if any is absent. Uploaded files are stored server-side and the resulting URLs persisted on the `BookingRequest`.
- **AC-8.6** — A client may submit a `BookingRequest` only if no active `Reservation` or `PENDING_CONFIRMATION` `BookingRequest` overlaps the requested date range for that vehicle. The API must enforce this check and return a `409 CONFLICT` if the vehicle is unavailable.
- **AC-8.7** — On agency confirmation of a `BookingRequest`, the system must atomically: derive and create a `Customer` record from the identity fields already stored on the `BookingRequest` (firstName + lastName + phone + email from the linked `ClientAccount`; idType + idNumber + driverLicenseCode from the `BookingRequest`) if no matching `Customer` already exists for that `(idType, idNumber)` pair, create a full `Reservation` record (status `ACTIVE`) linked to that `Customer`, create a linked `Payment` record (amounts to be completed by the agent), set `BookingRequest.status = CONFIRMED`, and populate `convertedReservationId`. No manual identity data entry is required from the agent.
- **AC-8.8** — On agency rejection of a `BookingRequest`, the system must set `BookingRequest.status = REJECTED`, persist the `rejectionReason`, record `rejectedAt` and `rejectedBy`, and dispatch a rejection email to the client's registered email address containing the stated reason.
- **AC-8.9** — The system must generate a QR code PNG image for each agency's landing page URL on demand via `GET /api/v1/agencies/{slug}/qr-code`, returning the image as `image/png`. No QR image is persisted — it is generated in-process per request.
- **AC-8.10** — The `Agency` entity must store the following landing page and branding fields: `primaryColor`, `secondaryColor`, `darkPrimaryColor`, `darkSecondaryColor` (hex strings), `tagline` (max 200 chars), `metaTitle` (max 70 chars), `metaDescription` (max 160 chars), `metaKeywords` (TEXT, comma-separated), and `ogImageUrl` (Open Graph image for social sharing previews).

**Booking Request State Machine**

```
[Client submits request — availability pre-validated]
                  │
                  ▼
     ┌────────────────────────┐
     │   PENDING_CONFIRMATION │
     └────────────┬───────────┘
                  │
         ┌────────┴────────┐
         │                 │
         ▼                 ▼
   ┌──────────┐      ┌──────────┐
   │ CONFIRMED│      │ REJECTED │ (reason stored; rejection email sent)
   └──────────┘      └──────────┘
   Reservation + Payment
   created atomically
```

---

### Module 3.9 — Calendar & Gantt Timeline View

**Problem:** Agency staff have no visual timeline to track when vehicles are booked, returning, or approaching critical dates like insurance renewals. Scheduling gaps and missed compliance deadlines create both operational and legal risk.

**User Stories**

- As an Agency Owner / Branch Manager, I want a date-grid calendar view showing all reservations and insurance expiry events, so that I can spot upcoming deadlines and scheduling conflicts at a glance.
- As an Agency Owner / Branch Manager, I want a per-vehicle Gantt timeline showing each vehicle's occupied and free windows across a date range, so that I can visually assess fleet availability when a client enquires.

**Acceptance Criteria**

- **AC-9.1** — The calendar must support month, week, and day grid views. Each reservation event must show: customer full name, vehicle licence plate, and the start/end date. Insurance expiry events must show: vehicle licence plate and expiry date, highlighted in a warning colour when within 30 days.
- **AC-9.2** — The Gantt timeline must display one row per vehicle. Reservation blocks span `startDate` to `endDate` and must use distinct colours for `ACTIVE` reservations vs `PENDING_CONFIRMATION` booking requests. Hovering a block must show a tooltip with: customer name (or "Pending client request"), hub names, and total amount.
- **AC-9.3** — A backend endpoint `GET /api/v1/calendar/events?from={date}&to={date}` must return a typed event list with the following event shapes:
  - `RESERVATION` — `{ type, reservationId, vehicleId, vehiclePlate, customerName, startDate, endDate, status }`
  - `BOOKING_REQUEST` — `{ type, bookingRequestId, vehicleId, vehiclePlate, clientName, startDate, endDate }`
  - `INSURANCE_EXPIRY` — `{ type, vehicleId, vehiclePlate, expiresAt, daysRemaining }`
- **AC-9.4** — Responses must be scoped to the authenticated user's branch for `BRANCH_MANAGER` and `AGENT` roles, and to the full agency for `AGENCY_OWNER`.
- **AC-9.5** — The `from`/`to` query parameters are mandatory; the API must return `400 BAD REQUEST` if either is absent or if `from > to`. Maximum range is 366 days.

---

## 6. Success Metrics & KPIs

| Metric                                 | Target                                                                           |
| -------------------------------------- | -------------------------------------------------------------------------------- |
| **Fleet Allocation Collision Rate**    | 0% scheduling conflicts within 30 days of client onboarding                      |
| **Average Vehicle Search Latency**     | Asset location cross-checks reduced to under 2 minutes from contract closing     |
| **Deposit Tracking Error Discrepancy** | 0% un-reconciled deposit errors inside active tenant transaction logs            |
| **Free-to-Paid Pilot Conversion Rate** | >30% transition of early-access trial agencies into long-term annual subscribers |
