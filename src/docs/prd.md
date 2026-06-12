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
Security deposits (la caution) and cash payments are recorded haphazardly on paper logbooks, leading to major reconciliation and shrink issues. Additionally, manually drafting legal contracts reflecting local compliance norms (Patente, ICE, IF, and RC) is highly repetitive and exposes business owners to severe tax and regulatory audit liabilities.

- **Strategic Position Note (Asset Protection vs. Tax Burden):** KiraDrive must be framed entirely as an internal **asset security, parking control, and logistics tracking engine** rather than a strict accounting/tax compliance application. This explicitly accommodates the cash-heavy, informal transaction preference typical of independent Moroccan agencies.

---

## 2. User Roles & System Access Control

| Role                   | Operational Scope                                                                                        | Access Bounds                                                                                                               |
| ---------------------- | -------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| **Master Super-Admin** | Platform owner oversight; manages system health, tenant activation, and global tier adjustments.         | Master system panel; can flag any tenant status to "Paid". Cannot read customer-identifiable data inside any tenant schema. |
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

- As a Tenant Owner, I want the system to instantly generate localized, print-ready PDF rental agreements and commercial invoices including our official regional identification numbers (ICE, Patente, IF, RC), so that we remain fully audit-compliant.
- As a Tenant Owner, I want to download our official platform-to-tenant SaaS subscription invoices within my settings tab as soon as our manual payment transfer is cleared by the platform admin.

**Acceptance Criteria**

- **AC-3.1** — Customer invoices must render in a clean, standard A4 layout using an HTML-to-PDF template component. The PDF header must parse and display all corporate identifiers: ICE, IF, Patente, and RC.
- **AC-3.2** — System-to-Tenant subscription PDFs must compile immediately when the Master Super-Admin flags a subscription period as "Paid". The document must state the payment mode, subscription period, and tier type, exposed via a permanent download link in the Tenant Settings UI.

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
  - **AC-5.4:** When executing physical print actions, the system must render the digital signature vector cleanly inside the PDF footer block adjacent to the required tenant tax variables (**ICE, Patente**).

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
│  Master Admin verifies receipt &    │
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

**Problem:** Platform onboarding is entirely manual today — the Master Super-Admin creates tenant accounts ad-hoc. This creates a bottleneck as the platform scales and leaves prospective agency owners with no structured intake path.

**User Stories**

- As a prospective Agency Owner, I want to submit a self-service registration form with my agency's business details, so that I can request access to the platform without waiting for a direct admin call.
- As a Master Super-Admin, I want to see a dashboard of all pending registration requests and individually approve or reject each one with an optional rejection reason, so that I can vet agencies before granting full system access.
- As a prospective Agency Owner, I want to receive a clear status update (approved or rejected) after my registration is reviewed, so that I know whether to proceed with onboarding or address the stated issues.

**Acceptance Criteria**

- **AC-6.1** — The public-facing registration form must collect: Agency Legal Name, RC Number (Registre de Commerce), ICE Number, Owner Full Name, Business Email, Phone Number, City, and optional Website URL. All fields except Website are mandatory.
- **AC-6.2** — Upon submission, the system must create an `Agency` record with `status = PENDING` and timestamp the `submittedAt` field. The agency must not gain any operational system access while in `PENDING` state.
- **AC-6.3** — The Master Super-Admin's registration queue must display all `PENDING` agencies sorted by `submittedAt` ascending (oldest first). Each row must surface: Agency Name, RC Number, ICE Number, Email, City, and time elapsed since submission.
- **AC-6.4** — Approving an agency must atomically: set `status = APPROVED`, record `approvedAt` timestamp, provision the tenant's isolated database schema, and trigger a welcome email to the registered business email with first-login credentials.
- **AC-6.5** — Rejecting an agency must: set `status = REJECTED`, record `rejectedAt` timestamp, and persist a mandatory `rejectionReason` string. A notification email must be dispatched to the applicant with the stated reason and an invitation to re-apply.
- **AC-6.6** — A rejected agency may re-submit a new registration request. The system must retain the previous rejected record for audit history and create a fresh `PENDING` record upon re-submission.
- **AC-6.7** — An approved agency that later violates platform terms may be set to `BLOCKED` by the Master Super-Admin. A `BLOCKED` agency loses all API access and its tenant staff receive an in-app banner notification.

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

## 6. Success Metrics & KPIs

| Metric                                 | Target                                                                           |
| -------------------------------------- | -------------------------------------------------------------------------------- |
| **Fleet Allocation Collision Rate**    | 0% scheduling conflicts within 30 days of client onboarding                      |
| **Average Vehicle Search Latency**     | Asset location cross-checks reduced to under 2 minutes from contract closing     |
| **Deposit Tracking Error Discrepancy** | 0% un-reconciled deposit errors inside active tenant transaction logs            |
| **Free-to-Paid Pilot Conversion Rate** | >30% transition of early-access trial agencies into long-term annual subscribers |
