# Rently Backend — Architecture & Implementation Decisions

This document explains the reasoning behind every major architectural and implementation decision in this project. For each decision: what we chose, why we chose it, what alternatives existed, and what trade-offs we accepted.

---

## Table of Contents

1. [Project Structure & Module Separation](#1-project-structure--module-separation)
2. [Entity Identity — UUIDs](#2-entity-identity--uuids)
3. [The `Auditable` Base Class](#3-the-auditable-base-class)
4. [Generic `CRUDService` Interface](#4-generic-crudservice-interface)
5. [The Three-Interface Mapper System](#5-the-three-interface-mapper-system)
6. [The Hydration Pattern](#6-the-hydration-pattern)
7. [Custom `@ValidEnum` Annotation](#7-custom-validatenum-annotation)
8. [JPA Fetch Strategy — Lazy Loading + EntityGraph](#8-jpa-fetch-strategy--lazy-loading--entitygraph)
9. [Enum Persistence Strategy — `EnumType.STRING`](#9-enum-persistence-strategy--enumtypestring)
10. [Composite Unique Constraint on Model](#10-composite-unique-constraint-on-model)
11. [ManyToMany — Vehicle Features](#11-manytomany--vehicle-features)
12. [Type Choices for Vehicle Fields](#12-type-choices-for-vehicle-fields)
13. [Error Handling Strategy](#13-error-handling-strategy)
14. [Database Schema Management — Flyway vs Hibernate DDL](#14-database-schema-management--flyway-vs-hibernate-ddl)
15. [Profiles & Configuration Strategy](#15-profiles--configuration-strategy)
16. [Lombok Usage](#16-lombok-usage)
17. [Catalog vs Fleet Module Split](#17-catalog-vs-fleet-module-split)
18. [Agency Module — Entity-First Approach](#18-agency-module--entity-first-approach)
19. [What Is Not Here Yet (and Why)](#19-what-is-not-here-yet-and-why)
20. [Multi-Tenancy Strategy — Schema-Per-Tenant](#20-multi-tenancy-strategy--schema-per-tenant)
21. [Cross-Schema Reference Pattern — Plain UUID Strings](#21-cross-schema-reference-pattern--plain-uuid-strings)
22. [PublicCatalogService — Dedicated Public-Schema EntityManager](#22-publiccatalogservice--dedicated-public-schema-entitymanager)
23. [Soft-Delete for Catalog Entities](#23-soft-delete-for-catalog-entities)
24. [AgencyRegistration vs Agency — Two Entities for Two Lifecycles](#24-agencyregistration-vs-agency--two-entities-for-two-lifecycles)
25. [SubscriptionPlan as a Managed Entity (not an Enum)](#25-subscriptionplan-as-a-managed-entity-not-an-enum)
26. [Manual Payment Flow & Subscription Lifecycle](#26-manual-payment-flow--subscription-lifecycle)
27. [Quota Enforcement — QuotaService with Caching](#27-quota-enforcement--quotaservice-with-caching)
28. [JWT Authentication Strategy](#28-jwt-authentication-strategy)
29. [Four-Role User Hierarchy](#29-four-role-user-hierarchy)
30. [ContractStatus Derived from Two Booleans](#30-contractstatus-derived-from-two-booleans)
31. [Async Document Generation](#31-async-document-generation)
32. [@ElementCollection for Vehicle Feature IDs](#32-elementcollection-for-vehicle-feature-ids)

---

## 1. Project Structure & Module Separation

**What we chose:** Two top-level domain modules — `catalog` and `fleet` — each self-contained with sub-packages per entity.

```
com.rently.rently/
├── catalog/
│   ├── brands/        (entity, repo, service, mapper, controller, request, response)
│   ├── features/
│   └── models/
│       └── hydration/ (Resolver, Context, Hydrator)
├── fleet/
│   └── vehicles/
│       └── hydration/
├── agency/
├── shared/
└── validation/
```

**Why this structure:**

The separation between `catalog` and `fleet` is driven by the *nature of the data*:

- **Catalog** contains reference/master data — things that don't change often (brands, models, feature definitions). These are lookup tables that other domains reference.
- **Fleet** contains operational data — things that change frequently and carry business state (vehicles being rented, maintained, etc.).

This split makes it easy to reason about what changes when. If you add a new domain, you know exactly where it goes. It also mirrors how a real business team is organized: a catalog team vs an operations team.

**Alternatives considered:**

- **Flat package by layer** (`controllers/`, `services/`, `repositories/`) — common in tutorials, terrible at scale. When you need to understand all of `Brand`, you'd jump across four packages. It forces you to think in technical layers instead of in business concepts.
- **Full hexagonal / ports-and-adapters** — clean separation between domain and infrastructure, but significantly more boilerplate for a team of this size. Would require separate `domain`, `application`, `infrastructure` packages per module. Overkill at this stage.
- **Multi-module Maven project** — each domain as a Maven module with its own `pom.xml`. Gives strong compilation boundaries but adds build complexity. Reserved for when the project grows large enough to warrant independent deployment.

**Trade-off accepted:** We get conceptual clarity at the cost of slightly deeper package paths.

---

## 2. Entity Identity — UUIDs

**What we chose:** All entities extend `Auditable`, which declares `String id` generated via `GenerationType.UUID`.

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private String id;
```

**Why UUIDs:**

- **No sequential leakage.** Auto-increment integers (`1, 2, 3...`) tell an attacker how many resources exist and make enumeration trivial. UUIDs don't.
- **Globally unique.** UUIDs can be generated anywhere — by the client, in a distributed service, in a migration script — without coordinating with the database. This matters when you eventually integrate events or multiple services.
- **Decoupling from the DB.** The ID doesn't require a round-trip to the DB to know what it will be.

**Why `String` instead of `java.util.UUID`:**

Using `String` avoids any serialization ambiguity. Jackson serializes `UUID` and `String` the same way in JSON, but some JDBC drivers handle the `UUID` type differently across databases. `String` is unambiguous everywhere. The cost is that the DB column is `varchar(36)` instead of a native UUID type — a minor storage overhead accepted for portability.

**Alternatives considered:**

- **Auto-increment Long** — simpler, faster index lookups, smaller storage. Rejected because of sequential leakage in a public-facing API.
- **ULID / KSUID** — sortable unique IDs, better for time-ordered queries. More complex, no out-of-box JPA support. Could be a future upgrade.
- **UUID type in PostgreSQL** — the DB can store it as 16 bytes vs 36. Requires custom type mapping. Acceptable future optimization once the schema stabilizes.

---

## 3. The `Auditable` Base Class

**What we chose:** A `@MappedSuperclass` that every entity inherits, providing `id`, `createdAt`, and `updatedAt` automatically.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
```

**Why inheritance here (not composition):**

JPA auditing requires these fields to be on the entity itself or a mapped superclass. You cannot achieve this through composition (`private AuditFields audit`) without custom SQL queries or second-level tricks. `@MappedSuperclass` is the idiomatic JPA solution.

**Why `Instant` and not `LocalDateTime`:**

`Instant` is timezone-agnostic — it represents a point in time in UTC. `LocalDateTime` has no timezone information baked in, which means: if your server changes timezone settings, timestamps look wrong. `Instant` is always correct regardless of server configuration.

**Why Spring's `@EnableJpaAuditing` is on the main class:**

The annotation must be present in the Spring context at startup to activate the `AuditingEntityListener`. Placing it on `RentlyApplication` ensures it's always active regardless of which beans are loaded.

**Alternatives considered:**

- **Trigger-based auditing in PostgreSQL** — `created_at DEFAULT NOW()` and a `BEFORE UPDATE` trigger. Works without any Spring code, but creates invisible side effects that aren't visible to the developer reading the entity class. Also breaks if you bypass JPA (e.g., bulk updates via native SQL).
- **Storing `ZonedDateTime`** — carries timezone but is more verbose and creates inconsistencies if different timezones are recorded. `Instant` with UTC is the industry standard.

---

## 4. Generic `CRUDService` Interface

**What we chose:** A single generic interface that all services implement.

```java
public interface CRUDService<CR, PR, R, ID> {
    List<R> getAll();
    R get(ID id);
    R create(CR request);
    void update(ID id, PR request);
    void delete(ID id);
}
```

**Why a generic interface:**

Every domain (`Brand`, `Feature`, `Model`, `Vehicle`) has the same lifecycle. Codifying that lifecycle as a generic interface serves several purposes:

1. **Consistency guarantee.** You cannot implement a service for a new domain and forget one of the operations — the compiler forces you to implement them all.
2. **Discoverability.** A new developer reads `CRUDService` and immediately knows the contract every service follows. No need to examine each service individually.
3. **Future tooling.** Generic interfaces allow writing generic tests, generic admin UIs, or generic audit tools that work across all domains.

**Why four type parameters and not just two:**

- `CR` (CreateRequest) and `PR` (PatchRequest) are separate because create and update often need different fields. A create request might require `brandId`, while a patch request treats it as optional. A single `Request` type would either force all fields as optional (validation headache) or require the same fields for both operations (too rigid).
- `R` (Response) is separate from the request types to enforce asymmetry: what goes in doesn't have to look like what comes out.
- `ID` is a parameter (rather than hardcoding `String`) for flexibility — theoretically you could have a composite key domain.

**Why `void update(...)` and not `R update(...)`:**

The HTTP convention we follow is `PATCH → 204 No Content`. The controller doesn't return the updated resource. Returning the resource from the service and then discarding it would be wasteful — it costs a mapping operation for no benefit. If a client wants the updated state, they issue a `GET` afterward (which is also the REST-purist approach: `PATCH` modifies, you `GET` to see the result).

**Alternatives considered:**

- **No shared interface, duplicate contracts per service** — simpler initially, diverges quickly. You'd end up with `BrandService.findAll()` and `VehicleService.list()` and `FeatureService.getFeatures()` with no consistency.
- **Spring Data's `CrudRepository` as the service-layer contract** — repositories are an infrastructure concern. Exposing repository-level contracts at the service layer bypasses all business logic and creates tight coupling to persistence.

---

## 5. The Three-Interface Mapper System

**What we chose:** Three focused mapper interfaces instead of one general-purpose mapper.

```java
// Maps entity → response DTO
interface ResponseMapper<E, R> {
    R toResponse(E entity);
}

// Maps create request → new entity (needs hydrated context for FK lookups)
interface CreateMapper<E, CR, CTX> {
    E toEntity(CR request, CTX ctx);
}

// Applies patch request to existing entity (mutates in-place)
interface PatchMapper<E, PR, CTX> {
    void patchEntity(E entity, PR request, CTX ctx);
}
```

**Why three interfaces and not one:**

Each operation has a fundamentally different signature and different requirements:

- `toResponse` never needs a context — the entity already has everything.
- `toEntity` needs a context because creating an entity often requires resolved FK references (e.g., you can't set `model.setBrand(brand)` without first fetching `brand`).
- `patchEntity` needs both an existing entity (to selectively update) and a context (for FK lookups). It mutates rather than creates, which allows updating only the changed fields.

If you put all three in one interface, every mapper would be forced to have methods it doesn't use in certain contexts, or you'd use overloading with ambiguous signatures.

**Why `PatchMapper` mutates instead of returning a new entity:**

The service already has a reference to the managed JPA entity. Hibernate tracks changes to managed entities — if you mutate the entity, Hibernate will generate an `UPDATE` statement automatically when the transaction commits. If `patchEntity` returned a new instance, you'd have to call `repository.save(newInstance)` manually, and the old instance would still be tracked (potential for conflicts). Mutating in-place is the natural JPA idiom.

**Why there's a `CTX` (Context) type parameter:**

Not every domain needs a context. For simple domains like `Brand` (no foreign keys), the mapper is called with `Object` context (and the context is `null` or `new Object()`). For complex domains like `Vehicle` (needs both a `Model` and a `Set<Feature>`), the context carries all resolved dependencies. Making it a type parameter rather than a fixed type means simple mappers aren't burdened with dependencies they don't use.

**There was a legacy `Mapper<E, R, CR, UR>` interface:**

The CLAUDE.md notes this is "being replaced." It combined all four operations in one interface, which caused the problems described above. The migration to three focused interfaces is in progress.

**Alternatives considered:**

- **MapStruct** — annotation-processor-based mapper that generates implementation code at compile time. Zero runtime overhead, handles most cases automatically, great for simple mappings. Rejected because it makes the hydration pattern awkward: MapStruct doesn't natively understand the concept of "resolve this ID to an entity before mapping," requiring workarounds (`@ObjectFactory`, qualifiers). The explicit three-interface approach is more readable for complex cases.
- **ModelMapper** — reflection-based, maps by field name convention. Fast to write but runtime errors (field name changes break mappings silently). Also hidden magic, difficult to debug.
- **Manual mapping in the service** — simplest, no abstraction. Rejected because it mixes two concerns (business logic and DTO-to-entity translation) in the same class.

---

## 6. The Hydration Pattern

**What we chose:** For any mapper that needs to resolve foreign-key references, we use a three-component pattern:

```
HydrationResolver → HydrationContext → Hydrator
```

For example, `ModelHydrationResolver` fetches the `Brand` entity by ID and throws if not found. `ModelHydrationContext` holds the resolved `Brand`. `ModelHydrator` orchestrates by calling the resolver and building the context. The mapper then receives `(request, context)`.

**Why this pattern exists:**

The root problem: mappers need JPA entities to set relationships, but fetching entities requires Spring repositories (which are Spring beans). If mappers have Spring dependencies, they become harder to test (you need a full Spring context or complex mocking). The hydration pattern separates the concern:

- **Resolver** = Spring bean, knows about repositories, does the fetching and validation.
- **Context** = plain value object (Lombok `@Builder`), no Spring, no framework. Holds the results.
- **Mapper** = plain class, no Spring, receives the context. Pure transformation logic, easily unit-tested.

**Why the resolver throws on missing references:**

If you call `modelService.create(request)` with a `brandId` that doesn't exist, the operation must fail with a meaningful error *before* any mapping happens. The resolver is the right place: it's responsible for validating that all referenced entities exist. Throwing a `EntityNotFoundException` here means the error is caught before we even start building the entity.

**Why Context uses Lombok `@Builder`:**

The context is immutable once constructed — you build it and pass it around. `@Builder` gives you a clean, readable construction pattern without needing a constructor with many parameters. Since `@Builder` is a Lombok annotation, there's no runtime overhead.

**Complexity scales with the domain:**

- **Brand, Feature** — no hydration needed (no foreign keys). Mappers use `Object` as context type and ignore it.
- **Model** — one foreign key (`brand`). `ModelHydrationContext` holds one `Brand`.
- **Vehicle** — two relationships (`model`, `Set<Feature>`). `VehicleHydrationContext` holds both.

**Alternatives considered:**

- **Resolving references inside the service** — the service fetches entities and sets them directly, skipping mappers. This works for simple cases but turns the service into a god object that handles both business logic and data transformation.
- **Resolving references inside the mapper (with Spring injected)** — simpler to write, but mappers become Spring-aware. Unit testing requires mocking repositories. Violates single-responsibility.
- **JPA handling it transparently via ID only** — set `model.setModelId(id)` and let JPA resolve the reference lazily. This can cause `LazyInitializationException` outside a transaction, and you lose the ability to validate that the reference exists before committing.
- **Using a DTO-to-entity framework like Orika** — adds an external dependency and still doesn't solve the FK resolution problem natively.

---

## 7. Custom `@ValidEnum` Annotation

**What we chose:** A custom Bean Validation constraint that validates whether a string value is a valid member of a given enum.

```java
@ValidEnum(enumClass = VehicleStatus.class, ignoreCase = true)
private String status;
```

**Why a custom annotation instead of letting Jackson fail:**

By default, Jackson throws a `HttpMessageNotReadableException` when it encounters an unknown enum value. That exception produces a generic 400 error with no useful message to the client. A custom validator integrates with Bean Validation's `ConstraintViolation` system, which produces structured error messages listing accepted values.

**Why `ignoreCase`:**

API clients (especially frontend developers) commonly send `"available"` or `"AVAILABLE"` interchangeably. Being strict about case creates unnecessary friction. The `ignoreCase` flag is optional — you can enforce exact casing if needed.

**Why validate on the DTO as a `String` rather than deserializing directly to the enum:**

If the DTO field were typed as `VehicleStatus`, Jackson would reject unknown values before validation even runs — the request body would fail to deserialize, and you'd lose the ability to return a structured validation error. Using `String` and validating separately gives you control over the error response.

**Alternatives considered:**

- **`@JsonProperty` with `@JsonCreator`** — custom deserialization logic in the enum itself. More verbose, couples the enum to Jackson.
- **`DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL`** — silently null-ifies unknown values. Wrong: it turns a client error into a hidden null that causes a `NullPointerException` later.
- **No validation, fail at database level** — the DB constraint rejects unknown values, but the error message is a low-level JDBC exception. Never acceptable for a public API.

---

## 8. JPA Fetch Strategy — Lazy Loading + EntityGraph

**What we chose:** All `@ManyToOne` and `@ManyToMany` relationships are `FetchType.LAZY`. For endpoints that need related data, we override with a named `@NamedEntityGraph`.

```java
@NamedEntityGraph(
    name = "Vehicle.details",
    attributeNodes = {
        @NamedAttributeNode(value = "model", subgraph = "model-details"),
        @NamedAttributeNode("features")
    },
    subgraphs = @NamedSubgraph(name = "model-details", attributeNodes = {
        @NamedAttributeNode("brand")
    })
)
```

The repository then uses this graph on `findAll()` and `findById()`:

```java
@EntityGraph("Vehicle.details")
List<Vehicle> findAll();
```

**Why lazy loading as the default:**

Eager loading means every time you load an entity, all related entities are loaded too — even if you don't need them. For a `Vehicle`, eager-loading `Model` eager-loads `Brand`. For a list of 100 vehicles, you'd execute 1 (vehicles) + 100 (models) + 100 (brands) = 201 queries (N+1 problem). Lazy loading defers that to when the relationship is actually accessed.

**Why EntityGraph instead of just writing `@Query` with a `JOIN FETCH`:**

Named entity graphs are declared once on the entity and reusable across multiple repository methods. A `JOIN FETCH` in `@Query` must be duplicated for every query method that needs it. Entity graphs are also composable — you could define a "minimal" graph and a "full details" graph and apply them contextually.

**Why include `brand` as a subgraph on the Vehicle's model:**

The response DTO for `Vehicle` includes brand information. Without the subgraph, accessing `vehicle.getModel().getBrand()` would trigger a lazy load outside the original transaction, causing a `LazyInitializationException`. The subgraph fetches it in the same query.

**Alternatives considered:**

- **All relationships EAGER** — simplest to write but devastating at scale. Rejected without hesitation for list endpoints.
- **`FetchType.EAGER` only on ManyToOne** — ManyToOne eager loading is less dangerous than ManyToMany (one extra join vs N extra queries), but still loads data that may not be needed. Consistency with LAZY everywhere is cleaner.
- **Hibernate `@Fetch(FetchMode.JOIN)` or `@BatchSize`** — `@BatchSize` reduces N+1 to N/batchSize+1 queries (e.g., batch 10 → 11 queries for 100 items). A reasonable alternative if entity graphs feel complex. EntityGraph was chosen for explicitness.

---

## 9. Enum Persistence Strategy — `EnumType.STRING`

**What we chose:** All enums are persisted as strings.

```java
@Enumerated(EnumType.STRING)
private VehicleStatus status;
```

**Why STRING and not ORDINAL:**

`EnumType.ORDINAL` stores the integer position (0, 1, 2...) of the enum constant. If you ever reorder enum constants or insert one in the middle, all existing database rows become wrong — silently. `EnumType.STRING` stores the name (`"AVAILABLE"`, `"RENTED"`), which is stable across enum evolution.

Concrete scenario: suppose `VehicleStatus` currently has `{AVAILABLE=0, RENTED=1, MAINTENANCE=2}`. If you add `RESERVED` between `AVAILABLE` and `RENTED`, ORDINAL makes all existing `RENTED` rows look like `RESERVED`. STRING is immune to this.

**Why not a converter to a database-native type (e.g., PostgreSQL ENUM):**

PostgreSQL enums require DDL changes (`ALTER TYPE`) to add values — a migration operation that blocks writes briefly. Java enums with STRING mapping can add values with no DDL change. Given that we're in active development, avoiding DDL churn is valuable.

**Alternatives considered:**

- **`EnumType.ORDINAL`** — rejected for the fragility described above.
- **PostgreSQL native ENUM type** — stricter at the DB level (impossible to store invalid values), but brittle to evolve. Rejected during development; could be worth revisiting for production once enums stabilize.
- **Integer code column with a lookup table** — fully normalized, zero enum fragility, queryable with joins. Overkill for this domain size. Adds joins to every query involving status.

---

## 10. Composite Unique Constraint on Model

**What we chose:** `Model.name` is not globally unique. Instead, the combination of `(brand_id, name)` is unique.

```java
@Table(
    name = "models",
    uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "name"})
)
```

**Why:**

A model name like "Camry" can exist under Toyota but not under Honda — yet "Accord" could exist under Honda. Enforcing global uniqueness on `name` alone would prevent `Toyota Camry` and `Honda Accord` from coexisting if they happened to share a name (unlikely, but architecturally wrong regardless). The constraint must be on the business key: brand + model name.

**How it's enforced in the service:**

The service checks for conflicts before saving:

```java
if (repository.existsByBrandAndName(brand, request.getName())) {
    throw new EntityExistsException(...);
}
```

The DB constraint is a safety net; the service-level check gives a better error message.

**Alternatives considered:**

- **Global uniqueness on name** — wrong. It would prevent valid data like "Sport" models from different brands.
- **No uniqueness constraint** — allows duplicate `(brand, model)` pairs. Wrong: you'd end up with two "Toyota Camry" rows.
- **Enforcing only at the service level, no DB constraint** — the DB constraint is the last line of defense against concurrent inserts that race past the service-level check. Both layers are necessary.

---

## 11. ManyToMany — Vehicle Features

**What we chose:** A `@ManyToMany` relationship between `Vehicle` and `Feature` via a join table `vehicle_features`.

```java
@ManyToMany
@JoinTable(
    name = "vehicle_features",
    joinColumns = @JoinColumn(name = "vehicle_id"),
    inverseJoinColumns = @JoinColumn(name = "feature_id")
)
private Set<Feature> features = new HashSet<>();
```

**Why ManyToMany (not a custom join entity):**

Features are catalog items — they don't carry any extra data on the relationship itself (there's no "quantity of this feature" or "added date"). A pure join table with just two foreign keys is the right model. If we needed to store additional data on the relationship (e.g., "feature was added by which agency"), we'd promote the join table to a full entity.

**Why `Set` and not `List`:**

- A vehicle either has a feature or it doesn't — duplicates are meaningless.
- `Set` expresses this semantic directly.
- Hibernate's ManyToMany with `List` has a known behavior issue: when you remove one element from a `List`, Hibernate sometimes deletes all rows from the join table and reinserts the remaining ones. With `Set` and `HashSet`, Hibernate deletes only the specific row. Using `Set` is safer and more efficient for join tables.

**Why `HashSet` as the default and not `LinkedHashSet`:**

Insertion order for features doesn't matter. `HashSet` is slightly more efficient. If you ever need ordered features (e.g., "primary features first"), you'd switch to a `LinkedHashSet` or add an ordering column to the join table.

**Why the relationship is unidirectional (on `Vehicle` only):**

`Feature` doesn't need to know about vehicles. The query pattern is always "give me this vehicle's features" — never "give me all vehicles that have this feature" (at least not yet). Bidirectional mapping adds complexity (owner side, mapped-by side, cascade rules) for a query that can be served by a `JOIN` when needed.

---

## 12. Type Choices for Vehicle Fields

Each field type was chosen deliberately:

| Field | Type | Why |
|-------|------|-----|
| `dailyBaseRate` | `BigDecimal` | Floating-point (`double`, `float`) cannot represent all decimal fractions exactly (0.1 + 0.2 ≠ 0.3 in floating-point). Financial values require exact decimal arithmetic. `BigDecimal` is the Java standard for money. |
| `insuranceExpiresAt` | `LocalDate` | Insurance expires on a specific calendar date, not at a specific instant. Time-of-day and timezone are irrelevant. `LocalDate` is semantically correct. `Instant` would force a time component that has no business meaning. |
| `year` | `Short` | Vehicle years are 4-digit numbers (1900–2099). `Short` (max 32,767) fits the domain. `int` would work but wastes 2 bytes per row per vehicle. `Short` communicates the field's intended range. |
| `month` | `Short` | Months are 1–12. Same reasoning as `year`. |
| `mileage` | `int` | Mileage is always a non-negative whole number. `int` supports up to ~2 billion km, more than enough. `Long` would be overkill. |
| `seats`, `doors` | `Short` (nullable) | Single-digit numbers; nullable because these may not be known for every vehicle at creation. |
| `description` | `@Column(columnDefinition = "TEXT")` | Unbounded length. VARCHAR with a fixed limit (e.g., 500) would be arbitrary and frustrating. `TEXT` in PostgreSQL has no length limit and stores identically to `VARCHAR` for short values. |
| `licensePlate`, `insuranceNumber` | `VARCHAR(20)` + unique | Fixed short identifiers that must be unique per vehicle. 20 characters is standard for license plates across most regions. |

---

## 13. Error Handling Strategy

**What we chose:** Use Jakarta Persistence standard exceptions — `EntityNotFoundException` and `EntityExistsException` — without a custom exception hierarchy.

**Why Jakarta's built-in exceptions:**

- Zero added classes.
- Semantically accurate: `EntityNotFoundException` means "you asked for something that doesn't exist," which maps naturally to 404. `EntityExistsException` means "you tried to create something that already exists," which maps to 409.
- Standard Java/Jakarta, understandable to any Java developer without reading custom code.

**What's missing (acknowledged):**

There is no `@RestControllerAdvice` global exception handler yet. Without one, Spring's default behavior kicks in — which returns a 500 with a stack trace or a generic JSON error depending on the Spring Boot version. This needs to be added before the API goes to production.

A proper handler would look like:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(EntityExistsException ex) {
        return ResponseEntity.status(409).body(new ErrorResponse(ex.getMessage()));
    }
}
```

**Alternatives considered:**

- **Custom exception hierarchy** (`VehicleNotFoundException extends NotFoundException`) — gives fine-grained control and clean catch clauses, but is significant boilerplate for minimal benefit at this stage. Worth doing if error responses need domain-specific metadata.
- **Result/Either types** — functional approach, no exceptions. Works well in Kotlin or Scala; idiomatic Java devs find it unfamiliar. Rejected for now.
- **Problem Details (RFC 7807)** — Spring 6 has built-in `ProblemDetail` support. A future improvement that makes error responses machine-readable. Not implemented yet.

---

## 14. Database Schema Management — Flyway vs Hibernate DDL

**What we chose:** Flyway is configured but disabled (`flyway.enabled: false`). Hibernate manages the schema via `ddl-auto: update`.

**Why `ddl-auto: update` in development:**

During active development, the schema changes frequently. Writing a Flyway migration for every field addition would create enormous overhead — you'd have 50 migration files before the schema stabilizes. `update` mode lets Hibernate add columns automatically, which is perfect for early-stage development.

**Why Flyway is configured at all:**

The intention is to switch to Flyway-managed migrations before production. The `flyway` configuration block in `application.yaml` means this switch is a single property change (`flyway.enabled: true`) plus populating the `db/migration/` directory. The infrastructure is ready; the migrations just haven't been written yet.

**Why `ddl-auto: update` is not acceptable in production:**

- It never drops columns or tables (only adds). If you rename a column, you end up with both the old and new column.
- It doesn't give you a migration history — you can't know exactly what changed between deployments.
- It's unpredictable under concurrent deployments.
- Production schema changes need to be reviewed, staged, and tested as explicit migrations.

**The intended production path:**

1. Stabilize the schema.
2. Write a Flyway baseline migration (V1) capturing the current state.
3. Set `flyway.enabled: true` and `ddl-auto: validate`.
4. All future schema changes go through numbered Flyway migrations.

**Alternatives considered:**

- **Liquibase** — XML/YAML-based migrations, more verbose than Flyway's plain SQL. Similar capabilities; Flyway was chosen because it uses plain SQL, which every developer can read without learning Liquibase syntax.
- **Purely manual DDL** — apply schema changes manually in the DB console. No history, no repeatability, nightmare for onboarding. Rejected.

---

## 15. Profiles & Configuration Strategy

**What we chose:** Three profiles — base `application.yaml`, `application-dev.yaml` (port 8090), `application-prod.yaml` (port 8080).

**Why separate dev and prod:**

- Dev logs SQL (`show-sql: true`) and generates schema DDL scripts to `target/`. These are useful for debugging but waste CPU in production.
- Dev uses a different port (8090) so it doesn't conflict with other services running on 8080 locally.
- Prod will eventually have different logging levels, no DDL changes, and stricter settings.

**Why environment variables for credentials:**

Database credentials (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`) are never hardcoded. They come from environment variables:

```yaml
datasource:
  url: ${DATABASE_URL}
  username: ${DATABASE_USERNAME}
  password: ${DATABASE_PASSWORD}
```

This follows the [12-factor app methodology](https://12factor.net/config) — config that varies between environments belongs in the environment, not in code. Prevents credentials from appearing in git history.

**The schema generation script (`generate-ddl: true`):**

In dev, Hibernate writes the generated schema to `target/generated-schema.sql`. This is useful for reviewing what Hibernate will create without connecting to a DB — and serves as input for writing the Flyway baseline migration when the time comes.

---

## 16. Lombok Usage

**What we chose:** Lombok annotations across all entities, DTOs, and context objects.

Common usage:
- `@Data` on DTOs — generates getters, setters, equals, hashCode, toString.
- `@Builder` on context objects — clean construction pattern.
- `@NoArgsConstructor` + `@AllArgsConstructor` on entities — JPA requires a no-args constructor; builder pattern requires all-args.
- `@Builder.Default` for default field values in builders (`status = VehicleStatus.AVAILABLE`, `mileage = 0`).

**Why Lombok:**

Without Lombok, every entity/DTO has 30–50 lines of boilerplate (getters, setters, constructors, equals/hashCode). This boilerplate carries no information but makes files twice as long. Lombok eliminates it, leaving only the meaningful code.

**Why `@Builder.Default` for entity fields:**

If a `@Builder` is used and a field has a default value (`new HashSet<>()`), Lombok's generated builder will ignore the field's initializer and set it to `null` unless `@Builder.Default` is specified. This is a Lombok gotcha that's easy to miss — forgetting it causes `NullPointerException`s when accessing `vehicle.getFeatures()` on a newly built vehicle.

**Alternatives considered:**

- **Records** (Java 16+) — immutable by design, no setters, not compatible with JPA (which requires mutable entities and a no-args constructor). Usable for DTOs, but we've standardized on Lombok for consistency.
- **Manual boilerplate** — verbose, no benefit. Rejected.
- **IDE generation** — generates the same boilerplate, but it lives in the file and drifts as fields change. Lombok stays in sync automatically.

---

## 17. Catalog vs Fleet Module Split

**What we chose:** Reference data (brands, models, features) lives in `catalog`. Operational data (vehicles) lives in `fleet`.

**The core distinction:**

- **Catalog** data changes infrequently and is shared — it's the "dictionary" that other domains reference.
- **Fleet** data changes constantly and carries business state — vehicles are rented, returned, maintained.

This has real consequences:

- Catalog entities rarely change. Fleet entities change on every rental transaction.
- Catalog could be cached more aggressively. Fleet must always be fresh.
- Future microservices split: catalog could become a shared read-only service; fleet would be the operational service.

**Vehicle references `catalog.models.Model`:**

The `Vehicle` entity has a `@ManyToOne` to `catalog.models.Model`. This is a deliberate cross-module dependency — fleet depends on catalog, not the other way around. The dependency flows in one direction: `fleet → catalog`. Catalog has no knowledge of fleet.

**Alternatives considered:**

- **Everything in one flat domain package** — acceptable at small scale, breaks down when the codebase grows and you need to reason about what's stable vs volatile.
- **Full microservices from day one** — premature. Network overhead, distributed transactions, deployment complexity — none of these are justified yet. The current structure *allows* a microservices split later without requiring it now.

---

## 18. Agency Module — Entity-First Approach

**What we chose:** The `Agency` entity and `AgencyStatus` enum are defined, but no service, mapper, or controller exists yet.

**Why build the entity before the service:**

The schema and data model tend to be more stable than the business logic. Defining the entity first:
- Allows the DB table to be created (via `ddl-auto: update`) and inspected.
- Captures early domain decisions (what fields does an agency have? what statuses?).
- Lets other team members or parallel workstreams see the data contract.

**Agency-specific design decisions:**

- `slug` — a URL-safe identifier for the agency (e.g., `"alami-cars"`). Allows stable URLs that don't expose the internal UUID.
- `rcNumber`, `iceNumber` — Moroccan business registration identifiers (Registre de Commerce, ICE). Both unique per agency.
- `status` defaults to `PENDING` — agencies must be approved before they can list vehicles. This is a business workflow decision: agencies are not trusted by default.
- `approvedAt`, `rejectedAt` — timestamps for state transitions. Useful for audit trails and reporting ("how long does approval take on average?").

**What comes next for Agency:**

The full CRUD service, hydration (none needed — no FK references), mapper, and controller. The status workflow (approve/reject operations) will likely add non-CRUD endpoints.

---

## 19. What Is Not Here Yet (and Why)

These are acknowledged gaps, not oversights. They're left out intentionally during early development to avoid premature investment.

### Global Exception Handler
Not yet implemented. Spring Boot's default error handling (`/error` endpoint) returns something, but the format is not controlled. Adding `@RestControllerAdvice` is the next infrastructure task — it's small and should be done before the first integration test.

### `@Transactional`
Service methods don't declare `@Transactional`. Spring Boot's auto-configuration sets the default transaction propagation at the repository layer (repositories are `@Transactional` by default). For single-operation service methods, this is fine. For multi-step operations (e.g., "create a vehicle and log an event"), missing `@Transactional` on the service method could leave partial state if step 2 fails. This needs attention as business logic grows.

### Security (Spring Security / JWT)
No authentication or authorization yet. The API is currently open. Security is a major workstream that hasn't started — it will involve JWT validation, role-based access control (agency admin vs platform admin vs anonymous), and request filtering.

### API Documentation (OpenAPI / Swagger)
No `springdoc-openapi` dependency. Adding it with `@Operation` annotations on controllers would auto-generate interactive docs at `/swagger-ui.html`. Low effort, high value for frontend integration.

### Logging
No `SLF4J` logger instances in service classes. SQL is logged in dev via Hibernate, but application-level events (entity created, update attempted, etc.) are not. Should be added to service implementations before production.

### Caching
No `@Cacheable` on any service methods. Catalog data (brands, models, features) is read-heavy and write-rare — a prime candidate for in-memory caching with `@EnableCaching` + `Caffeine`. Not implemented yet because premature optimization; profile first.

### Pagination
`getAll()` returns `List<R>` — unbounded. For `Vehicle.findAll()` with potentially thousands of rows, this will become a problem. The `CRUDService` interface would need a `getAll(Pageable pageable)` overload, and the repository would need `Page<Vehicle> findAll(Pageable pageable, EntityGraph)`. This is a planned upgrade.

---

## 20. Multi-Tenancy Strategy — Schema-Per-Tenant

**What we chose:** PostgreSQL schema-per-tenant isolation via Hibernate's `SCHEMA` multi-tenancy strategy. Each approved agency gets a dedicated PostgreSQL schema named after its `slug` (e.g., `alami-cars`). Tenant routing is driven by the `X-Tenant-ID` HTTP header, which populates a thread-local `TenantContext` via a servlet filter. Hibernate's `MultiTenantConnectionProvider` switches the JDBC connection's `search_path` for each request.

**Why schema-per-tenant and not row-level isolation:**

The main alternative — a single shared schema with a `tenant_id` discriminator column on every table — is simpler to implement initially but has serious problems at scale:

- **Data leakage risk is higher.** A missing `WHERE tenant_id = ?` clause returns every tenant's data. With schema isolation, the `search_path` physically prevents a connection from seeing another tenant's tables.
- **Index efficiency degrades.** Indexes on a shared table span all tenants' data. A per-tenant schema has indexes sized to one tenant's data, which is faster for all queries.
- **Backup and restore granularity.** With schema isolation you can snapshot or restore a single tenant without touching others. With row-level, you can't restore one tenant without restoring all.
- **Regulatory compliance.** Some clients will eventually need a contractual guarantee that their data is physically separated. Schema isolation satisfies this; row-level cannot.

**Why schema-per-tenant and not database-per-tenant:**

A dedicated PostgreSQL database per tenant gives even stronger isolation but introduces operational overhead that isn't justified yet: separate connection pools per tenant, separate `pg_hba.conf` entries, separate maintenance windows, and no easy way to run cross-tenant admin queries. Schema-per-tenant gives 95% of the isolation at a fraction of the cost.

**Why `Agency.slug` doubles as the schema name:**

The slug is already URL-safe (lowercase letters, dashes, no special characters), stable after approval, and unique. Using it as the schema name means you never need a separate lookup table to go from "tenant header value" to "schema name" — they are the same thing.

**Schema provisioning trigger:**

The `TenantSchemaProvisioner` runs atomically inside the `AgencyRegistrationService.approve` transaction. If schema creation fails, the entire approval transaction rolls back — there is never a state where an `Agency` row exists but its schema does not.

**Alternatives considered:**

- **Row-level security (RLS) in PostgreSQL** — PostgreSQL policies that automatically filter by tenant. More transparent than application-level checks but requires careful policy management and doesn't work through JPA's entity cache without custom work.
- **Separate database per tenant** — strongest isolation, rejected for operational complexity at this stage.
- **Spring's `AbstractRoutingDataSource`** — routes to a different `DataSource` per tenant. Requires a connection pool per tenant (hundreds of pools at scale). Schema-per-tenant with a single pool is more efficient.

**Trade-off accepted:** Schema-per-tenant requires every DDL change to be applied to all schemas, not just one. Flyway migrations will need to run once per tenant schema. This is manageable with a loop over all slugs, but it's more work than a single migration on a flat schema.

---

## 21. Cross-Schema Reference Pattern — Plain UUID Strings

**What we chose:** Wherever a tenant-schema entity needs to reference a public-schema entity, the reference is stored as a plain `String` UUID column — not a JPA `@ManyToOne` relationship. For example, `Vehicle` stores `String modelId` and `Set<String> featureIds` instead of `@ManyToOne Model model` and `@ManyToMany Set<Feature> features`.

**Why JPA relationships cannot cross schema boundaries:**

Hibernate's multi-tenancy routing works by setting the JDBC connection's `search_path` to the tenant schema at the start of each request. When a tenant-schema entity navigates a JPA relationship, Hibernate issues a query — and that query runs against the tenant schema's `search_path`. Public-schema entities (`Brand`, `Model`, `Feature`) are not in the tenant schema, so JPA cannot find them through the tenant `EntityManager`.

You could configure the tenant `EntityManager` to include the public schema in `search_path`, but this breaks isolation: any query issued through the tenant `EntityManager` could now touch both schemas, defeating the purpose of schema separation.

**Why PostgreSQL cannot enforce FK constraints across schemas:**

PostgreSQL does support cross-schema FK constraints within the same database, but Hibernate's DDL generation does not produce them in a multi-tenancy setup (because the schema name for the tenant table isn't known at compile time). The constraint would have to be added manually per-tenant-schema per Flyway migration. Application-layer enforcement is more maintainable.

**How referential integrity is maintained:**

Before any write that creates or updates a `Vehicle`, the service calls `PublicCatalogService.modelExists(modelId)` and `PublicCatalogService.allFeaturesExist(featureIds)`. If any reference is invalid, the service throws `EntityNotFoundException` before touching the database. This is the "application-layer FK" pattern.

**Why this is safe in conjunction with soft-delete:**

Catalog entities are never hard-deleted (see §23). A `modelId` stored on a `Vehicle` today will continue to point to a valid (if possibly inactive) `Model` row forever. There is no deletion cascade that could leave a dangling reference.

**Trade-off accepted:** JPA navigation is lost. You cannot call `vehicle.getModel()` and get a `Model` object. All cross-schema reads go through `PublicCatalogService` explicitly. This is verbose but intentional — every cross-schema read is visible in the code, not hidden behind lazy-load proxies.

---

## 22. PublicCatalogService — Dedicated Public-Schema EntityManager

**What we chose:** A Spring `@Service` that holds an `EntityManager` permanently bound to the public PostgreSQL schema, injected via a `@Qualifier("publicEntityManagerFactory")` `EntityManagerFactory` bean. All application code that needs to read `Brand`, `Model`, or `Feature` from within a tenant request context must go through this service.

**Why a dedicated EntityManager:**

In a multi-tenant request, the default `EntityManager` routes to the tenant schema. If a tenant service injects the default `EntityManager` and calls `em.find(Model.class, id)`, Hibernate looks in the tenant schema — where `models` doesn't exist. The dedicated `publicEntityManagerFactory` bypasses `CurrentTenantIdentifierResolver` entirely, pointing permanently at the public schema regardless of which tenant is active.

**Why a service wrapper and not a repository:**

Spring Data repositories rely on the default `EntityManagerFactory`. Creating a `ModelRepository` that uses the public factory would require a non-trivial configuration (`@EnableJpaRepositories(entityManagerFactoryRef = "publicEntityManagerFactory")`), and all catalog repositories would need this configuration. Wrapping the public `EntityManager` in a single `PublicCatalogService` is simpler — the configuration is in one place, and callers don't need to know about `EntityManagerFactory` variants.

**What PublicCatalogService exposes:**

- `getModel(String modelId)` — fetch a single model, throw if not found.
- `getFeatures(Set<String> featureIds)` — batch fetch features by a set of IDs.
- `modelExists(String modelId)` — boolean existence check (for pre-write validation).
- `allFeaturesExist(Set<String> featureIds)` — count-based existence check for a set.
- `getBrand(String brandId)` / `brandExists(String brandId)` — same pattern for brands.

**Why `getFeatures` uses a batch query instead of N individual lookups:**

A vehicle can have many features. If the response mapper called `getModel` once per feature, a vehicle with 10 features would issue 11 queries (1 model + 10 features). The batch `WHERE f.id IN :ids` query is a single round-trip regardless of how many features the vehicle has.

**Alternatives considered:**

- **Static utility methods on the repository** — doesn't integrate with Spring's `EntityManager` lifecycle or transaction management.
- **Duplicate the catalog data into the tenant schema** — eventual consistency problem; any update to a shared feature would need to propagate to all tenant schemas. Rejected.
- **Bypass JPA entirely with JDBC and `public.` schema prefix** — works but loses type safety and ORM benefits for the few queries involved.

---

## 23. Soft-Delete for Catalog Entities

**What we chose:** `Brand`, `Model`, and `Feature` entities each have an `isActive` boolean field (default `true`). There is no delete endpoint for any of them — only a `POST /{id}/deactivate` endpoint that sets `isActive = false`. The underlying rows are never removed from the database.

**Why no hard delete:**

The cross-schema reference pattern (§21) stores catalog IDs as plain string columns in tenant entities. If a `Model` row is hard-deleted, every `Vehicle` row that references its ID becomes silently corrupted — the `modelId` column contains a UUID that no longer exists, but there is no DB FK to catch it. The application would return broken responses or `EntityNotFoundException` errors for legitimate vehicles.

Soft-delete converts this from a data-corruption risk to a read-time business rule: inactive catalog items are excluded from selection UIs but remain reachable for historical data display.

**How it affects queries:**

- **Selection/search endpoints** filter by `isActive = true` — agencies only see active catalog items when creating vehicles.
- **Resolution in PublicCatalogService** — `getModel` and `getFeatures` do not filter by `isActive`. This is intentional: a vehicle that was created when the model was active should still resolve correctly after the model is deactivated. The vehicle doesn't become invalid just because the catalog item is retired.
- **Deactivate vs delete** — `POST /brands/{id}/deactivate` is the admin action. No `DELETE /brands/{id}` endpoint exists.

**Why `isActive` and not a `deletedAt` timestamp:**

`isActive` is simpler to query (`WHERE is_active = true`) and matches the business concept — an item is either available or retired, with no intermediate states. A `deletedAt` timestamp would be equivalent but adds noise: the field is either `null` or has a value, and you'd always write `WHERE deleted_at IS NULL` instead of `WHERE is_active = true`.

**Alternatives considered:**

- **Hard delete with cascade updates** — on Brand delete, null out all `Model.brand_id` references. Makes models orphaned (brand-less), breaks the model's identity, and creates inconsistent display data.
- **Tombstone table** — on delete, move the row to a `deleted_brands` archive table. Adds complexity for minimal benefit; the same queries against the live table would need to check both tables.
- **Restrict delete if referenced** — prevent deleting a Brand if any Model references it. This is the safest hard-delete approach, but it still leaves the cross-schema vehicle references dangling if a Model is deleted. Soft-delete is cleaner.

---

## 24. AgencyRegistration vs Agency — Two Entities for Two Lifecycles

**What we chose:** Two separate entities: `AgencyRegistration` captures the public application form and its review lifecycle; `Agency` represents an approved, operational tenant. An `Agency` row is only ever created by approving a registration — it never exists in a pending state.

**Why not a single `Agency` entity with a status field:**

The tempting approach is one `Agency` table with `status = PENDING | APPROVED | REJECTED | BLOCKED`. The problem is that the two states represent fundamentally different things:

- A `PENDING` agency is an application — it has no tenant schema, no users, no vehicles, no operational significance. It's a form submission.
- An `APPROVED` agency is a live tenant — it has a schema provisioned, an owner `User` created, a subscription assigned, and real data.

If both states share one table, every query and business rule touching `Agency` must guard against accidentally operating on a `PENDING` row. Every service that does `agencyRepository.findBySlug(slug)` must check that the result is `APPROVED`. Every FK reference to `Agency` could accidentally point to a pending application.

Separating them makes the invariant compile-time enforced: an `Agency` object in the system is always approved. There is no `if (agency.getStatus() == APPROVED)` guard needed anywhere in the codebase.

**The atomic approval transaction:**

When an admin approves a registration, a single `@Transactional` method must:
1. Create the `Agency` row (status = APPROVED, approvedAt = now, slug generated).
2. Create the owner `User` row (role = AGENCY_OWNER, agencySlug set).
3. Call `TenantSchemaProvisioner` to create the PostgreSQL schema.
4. Mark the `AgencyRegistration` as APPROVED, set `reviewedAt`, `resolvedAgencyId`.

If any step fails, the transaction rolls back and none of the above persists. This atomicity guarantee means there is never a state where an `Agency` exists without an owner `User`, or a schema exists without an `Agency` row.

**`AgencyRegistration` as a permanent audit trail:**

After approval, the `AgencyRegistration` row remains with `status = APPROVED` and `resolvedAgencyId` pointing to the created `Agency`. This gives a full audit trail: when was the application submitted, who reviewed it, when was it approved, which agency was created. This history cannot be derived from `Agency` alone.

**Alternatives considered:**

- **Single entity with status** — rejected as described above.
- **Soft-delete the registration after approval** — loses the audit trail.
- **Event sourcing** — application submitted as an event, approval as another event. More accurate but significantly more infrastructure for no added benefit at this stage.

---

## 25. SubscriptionPlan as a Managed Entity (not an Enum)

**What we chose:** `SubscriptionPlan` is a JPA entity (`@Entity`) backed by the `subscription_plans` table, managed by the Super-Admin through a CRUD API. It is seeded at startup with two default plans (`SAFI`, `CHAMIL`) but can be extended or modified at runtime without code changes.

**Why an entity and not a Java enum:**

The original design had `SubscriptionPlan` as an enum with hardcoded quota values. The problem is that business decisions about pricing and plan limits should not require a code deployment to change. If the product team wants to add a third tier, adjust the vehicle limit of `SAFI`, or change the monthly price of `CHAMIL`, they should be able to do that in a database record — not in a pull request.

Making it an entity also enables:
- **Admin UI** — a full CRUD API for plan management, accessible to the Super-Admin.
- **Soft-deactivation** — old plans can be set `isActive = false` without losing historical subscription data that references them.
- **Audit trail** — `createdAt` / `updatedAt` from `Auditable` show when plan parameters changed.

**Why `null` for unlimited quotas and not a sentinel like `-1`:**

`null` in Java and SQL unambiguously means "no value" / "unbounded". Using `-1` requires every quota check to contain `if (plan.getMaxBranches() == -1 || count < plan.getMaxBranches())` — a magic number that every developer needs to know about. With `null` the check reads `if (plan.getMaxBranches() == null || count < plan.getMaxBranches())` — semantically obvious.

**The `code` field as a stable machine key:**

`code` (e.g., `SAFI`, `CHAMIL`) is the stable identifier used in business logic. `displayName` can be changed freely (localised, rebranded) without affecting application logic. This separation of machine key from display label is the standard design for reference data.

**Alternatives considered:**

- **Java enum with hardcoded values** — rejected because it couples pricing decisions to deployments.
- **Properties file configuration** — limits are in `application.yaml`, not the DB. Works for single-tenant apps but doesn't support per-agency plan assignment or admin-driven changes.
- **Feature flags service** — overkill for this use case; a simple DB table is sufficient.

---

## 26. Manual Payment Flow & Subscription Lifecycle

**What we chose:** No payment gateway integration. Agencies pay by bank transfer or Cash Plus (a Moroccan cash payment service). The Super-Admin manually verifies the payment receipt and calls `POST /admin/subscriptions/{id}/mark-paid`. This triggers an async invoice PDF generation job.

**Why manual payment and not Stripe/PayPal:**

The target market — small and medium car rental agencies in Morocco — predominantly operates with bank transfers and cash payment services rather than card-on-file. Integrating a card gateway would add friction for a payment method most agencies don't use, plus PCI-DSS scope. The manual verification flow matches the actual business process.

**The `SubscriptionStatus` state machine:**

```
PENDING_PAYMENT  ──► ACTIVE   (admin marks paid)
ACTIVE           ──► EXPIRED  (end date passes — via scheduled job, future)
ACTIVE           ──► SUSPENDED (admin action — future)
```

`PENDING_PAYMENT` is the default on subscription creation. The subscription record is created at approval time so that the admin can immediately see which agencies are awaiting payment confirmation.

**Why `paymentMode` is a free-text string:**

Payment mode options are descriptive ("Bank Transfer — CIH Bank", "Cash Plus — Receipt #1234"). Enforcing an enum would require code changes every time a new payment variant is accepted. A free-text field lets the admin record exactly how the payment was received without schema changes.

**Invoice PDF generation on markAsPaid:**

The invoice is generated asynchronously after the payment is confirmed (see §31). The admin confirms payment → subscription status becomes `ACTIVE` → async job generates PDF → `invoiceUrl` is written back to the subscription row. The agency owner can download it from their settings page by polling `GET /settings/subscription`.

**Alternatives considered:**

- **Stripe / Paddle** — full payment gateway. Adds PCI-DSS scope and integration complexity for a market that mostly doesn't use card payments.
- **Pre-generate invoices** — generate the PDF at subscription creation time (before payment). Wrong: an invoice should reflect a confirmed transaction, not a pending one.
- **Synchronous PDF generation** — block the `mark-paid` HTTP response until the PDF is ready. Rejected because PDF generation can take seconds; it should not hold an HTTP connection open.

---

## 27. Quota Enforcement — QuotaService with Caching

**What we chose:** A centralised `QuotaService` that reads the agency's current `SubscriptionPlan` from the database, checks quota limits, and throws an exception (resulting in HTTP 403) if a limit would be exceeded. Quota lookups are cached via Spring Cache with a short TTL to avoid a DB round-trip on every `Branch`, `Hub`, or `Vehicle` creation.

**Why centralised and not inline per service:**

Without centralisation, `BranchService`, `HubService`, and `VehicleService` would each need to: resolve the current agency's subscription, load the plan, check the relevant limit. That's three copies of the same lookup + check logic. If the quota enforcement rule changes (e.g., a new way to fetch the active subscription), it must be updated in three places.

`QuotaService` exposes three methods — `assertCanAddBranch()`, `assertCanAddHub()`, `assertCanAddVehicle()` — and each service calls the relevant one before creating the entity.

**Why HTTP 403 for quota exceeded and not 422 or 400:**

- `400 Bad Request` signals a malformed request. The request itself is valid — it's the caller's subscription plan that prevents the action.
- `422 Unprocessable Entity` signals a semantic validation error. Closer, but semantically it's about business rules on the request data.
- `403 Forbidden` signals that the caller does not have permission to perform this action. A plan limit is fundamentally a permission constraint — the agency is not *allowed* to exceed its plan. `403` is the most semantically accurate status code.

**Why cache with a short TTL:**

The active plan for an agency changes infrequently (once per subscription renewal cycle). Fetching it from the DB on every vehicle create would be wasteful. A short TTL cache (e.g., 5 minutes) means that if a plan is upgraded mid-session, the change takes effect within minutes — acceptable for a manually-managed billing system. The cache is keyed by `agencySlug`.

**Alternatives considered:**

- **Check in each individual service** — avoids a new class but duplicates the resolution logic. Rejected.
- **DB trigger or constraint** — a `CHECK` constraint that counts rows. Not possible for cross-table count constraints in standard PostgreSQL without triggers. Triggers are invisible to the developer reading service code.
- **No caching, always hit the DB** — simpler, correct, but adds a DB round-trip to every resource-creation request. Worth reconsidering only if plan changes need instant effect.

---

## 28. JWT Authentication Strategy

**What we chose:** Stateless JWT (JSON Web Token) authentication. On login, the server issues a signed JWT containing the user's ID, role, and `agencySlug`. The client includes this token in the `Authorization: Bearer <token>` header of every request. A `JwtAuthenticationFilter` validates the token and populates the Spring `SecurityContext` before the request reaches a controller.

**Why stateless JWT and not server-side sessions:**

- **Multi-tenancy routing.** Each request already carries an `X-Tenant-ID` header to route to the correct tenant schema. If session state were stored server-side, the session store would itself be a shared resource that doesn't fit neatly into the per-tenant schema model. JWT avoids a session store entirely.
- **Horizontal scaling.** Stateless tokens work across multiple application instances without shared session state. Every instance can validate a JWT independently using the signing key.
- **Self-contained claims.** The JWT carries `role` and `agencySlug` directly. The `JwtAuthenticationFilter` doesn't need a DB lookup to know what the user is allowed to do — the role is in the token. This avoids a DB round-trip on every authenticated request.

**Why the JWT carries `agencySlug` and not just `userId`:**

The `agencySlug` is needed by every tenant-scoped request to set the `TenantContext`. If only `userId` were in the token, the filter would need to load the `User` from the database on every request to get the `agencySlug` — defeating one of the main benefits of stateless JWT. Embedding `agencySlug` in the token means the filter can populate both `SecurityContext` and `TenantContext` from the token alone.

**What goes in the token vs what is always fetched:**

The JWT carries stable, infrequently-changing claims: `userId`, `role`, `agencySlug`, `branchId`. It does not carry per-request state like current subscription status — those are always fetched fresh. If an agency is blocked, the `TenantFilter` checks the live `Agency` row; the JWT alone cannot grant access to a blocked tenant.

**Token expiry and refresh:**

Not yet implemented, but the design anticipates short-lived access tokens with a refresh token mechanism. Short access token TTL limits the window during which a stolen token is valid. The implementation will add a `POST /api/v1/auth/refresh` endpoint.

**Alternatives considered:**

- **Spring Session with Redis** — shared session store, works with any session-based approach. Adds a Redis dependency and complicates the multi-tenancy setup. Rejected.
- **Opaque tokens** — random string that maps to a session record in the DB. Every request requires a DB lookup to validate the token and retrieve claims. Slower, and reintroduces session state.
- **OAuth2 / OpenID Connect** — delegates authentication to an identity provider (Keycloak, Auth0). Correct for large-scale platforms; adds infrastructure complexity that isn't justified for a self-hosted SaaS with a fixed user base.

---

## 29. Four-Role User Hierarchy

**What we chose:** Four roles — `SUPER_ADMIN`, `AGENCY_OWNER`, `BRANCH_MANAGER`, `AGENT` — stored as a `UserRole` enum on the `User` entity.

**Why these four roles:**

The roles map directly to the organisational structure of a car rental agency business:

| Role | Scope | Capabilities |
|------|-------|-------------|
| `SUPER_ADMIN` | Platform | Manages agencies, plans, registrations; no tenant affiliation |
| `AGENCY_OWNER` | Agency | Manages own staff, settings, subscription; sees all branches |
| `BRANCH_MANAGER` | Branch | Manages vehicles and reservations within one branch |
| `AGENT` | Branch | Creates reservations, processes payments; read-only on vehicles |

**Why `SUPER_ADMIN` has a null `agencySlug`:**

The super admin is a platform-level user, not a tenant user. There is no agency to associate them with. `null` is the correct representation — the null check in the `TenantFilter` explicitly skips tenant routing for requests authenticated as `SUPER_ADMIN`, allowing them to access the public-schema admin endpoints directly.

**Why `BRANCH_MANAGER` and `AGENT` store a `branchId`:**

Operations staff are scoped to a single branch. If a branch manager creates a reservation, it must be associated with their branch. Storing `branchId` in the JWT (via the User record) means the server always knows which branch the request originates from without the client having to send it — and the client cannot forge a different branch scope.

**Why not a permission-based system instead of role-based:**

A permission-based system (each user has a set of granular permissions) would be more flexible but is significant overhead to implement correctly. The four roles are stable — this is not a system where users frequently get custom permission sets. Role-based access control (RBAC) is simpler and easier to reason about: "what can a BRANCH_MANAGER do?" is a one-line answer.

---

## 30. ContractStatus Derived from Two Booleans

**What we chose:** A `Reservation` stores two separate boolean flags — `isDigitallySigned` and `isPhysicallyPrinted` — and derives a `ContractStatus` enum from their combination:

| `isDigitallySigned` | `isPhysicallyPrinted` | `ContractStatus` |
|---|---|---|
| false | false | `PENDING` |
| true | false | `PARTIAL_EXECUTION` |
| false | true | `PARTIAL_EXECUTION` |
| true | true | `FULLY_EXECUTED` |

**Why store `ContractStatus` at all if it's derived:**

`ContractStatus` is stored as a column (not computed at read time) for two reasons:

1. **Queryability.** The admin dashboard needs to filter reservations by contract compliance state (`WHERE contract_status = 'FULLY_EXECUTED'`). Computing it on the fly would require a CASE expression in every query.
2. **Change detection.** The `updateContractStatus` service method is called whenever either boolean changes, derives the new status, and persists it. This keeps the derived value always in sync without a computed column (which PostgreSQL supports but JPA doesn't map cleanly).

**Why two separate booleans rather than just the enum:**

The two actions — digital signing and physical printing — are triggered by different actors (the customer signs digitally, the agent marks the physical print). They happen through separate API endpoints (`POST /{id}/signature` and `POST /{id}/mark-printed`). If only the enum were stored, each endpoint would need to read the current status, derive what the new status should be based on which endpoint was called, and update it. The booleans make this logic trivial: each endpoint flips one boolean, then `updateContractStatus` re-derives the enum.

**Why `PARTIAL_EXECUTION` for "exactly one of the two":**

Moroccan rental regulations require both a digital signature record and a printed physical contract. "One but not both" is a partially compliant state — the contract is in partial execution. This terminology is drawn from contract law (a contract is "in execution" once both parties have fulfilled their obligations).

---

## 31. Async Document Generation

**What we chose:** All PDF generation runs off the main HTTP thread via Spring's `@Async` mechanism backed by a dedicated `ThreadPoolTaskExecutor` bean. Three async services handle different document types: `ContractPdfService` (on reservation create), `InvoicePdfService` (on reservation close), and `SubscriptionInvoicePdfService` (on subscription mark-paid). Clients poll a `DocumentStatusController` to check when their document is ready.

**Why async and not synchronous:**

PDF generation involves template rendering, image embedding, and multi-page layout computation. For a rental contract with agency branding and a signature image, this can take 1–5 seconds. Blocking the HTTP response thread for 5 seconds is unacceptable: it holds a thread pool slot, increases perceived latency, and can time out mobile clients. The user who just created a reservation does not need the PDF immediately — they need the reservation confirmed.

**The polling pattern instead of WebSocket or SSE:**

The client (frontend) receives a reservation ID in the create response. It then polls `GET /reservations/{id}/contract/status` (returns `{ ready: false, url: null }` initially, then `{ ready: true, url: "..." }` once the PDF is generated). This is simple to implement correctly and works with any HTTP client. WebSocket or Server-Sent Events would require the frontend to maintain a persistent connection, which adds complexity for a non-critical feature.

**Why a dedicated `ThreadPoolTaskExecutor` and not the default executor:**

Spring's default `SimpleAsyncTaskExecutor` creates a new thread for every `@Async` call — no thread pool, no bounded concurrency. Under load (many reservations closing simultaneously), this would spawn an unbounded number of threads. A configured `ThreadPoolTaskExecutor` with a fixed thread count and a queue bounds the resource usage.

**What triggers each document:**

- `ContractPdfService.generateAsync(reservationId)` — triggered inside `ReservationService.create` after the reservation is persisted.
- `InvoicePdfService.generateAsync(reservationId)` — triggered inside `ReservationService.close`.
- `SubscriptionInvoicePdfService.generateAsync(subscriptionId)` — triggered inside `SubscriptionService.markAsPaid`.

All three methods write the generated PDF URL back to the respective entity via a repository update after generation completes.

**Alternatives considered:**

- **Synchronous generation** — blocks HTTP thread. Rejected for latency reasons.
- **Message queue (RabbitMQ, Kafka)** — decouples generation from the web tier, enables retries. Correct at larger scale; overkill for the current deployment model.
- **Pre-generate templates at request time** — render HTML synchronously, generate PDF in background. No meaningful benefit over pure async.
- **Third-party PDF service (WeasyPrint, DocRaptor)** — external HTTP call, adds network dependency and cost. Rejected in favour of in-process generation (Flying Saucer / OpenPDF).

---

## 32. @ElementCollection for Vehicle Feature IDs

**What we chose:** Vehicle features are stored as `@ElementCollection Set<String> featureIds` — a join table (`vehicle_features`) of scalar UUID strings — rather than the `@ManyToMany Set<Feature> features` relationship that existed in the pre-multi-tenancy design.

**Why the original @ManyToMany cannot survive multi-tenancy:**

In the original single-schema design, `@ManyToMany` worked because `vehicles` and `features` lived in the same schema. In the multi-tenant architecture, `vehicles` lives in the tenant schema and `features` lives in the public schema. Hibernate's tenant `EntityManager` cannot navigate a `@ManyToMany` relationship to an entity in a different schema — it would look for `features` in the tenant schema, where the table doesn't exist.

**Why @ElementCollection instead of a plain @Column:**

`@ElementCollection` tells JPA to manage the `vehicle_features` join table automatically — it handles inserts, deletes, and fetches for the collection as part of the `Vehicle` entity lifecycle. Without it, you'd need to manage the join table manually via a separate repository and ensure transactional consistency manually. `@ElementCollection` gives you collection semantics (`add`, `remove`, `contains`) backed by the join table.

**Why `Set<String>` and not `List<String>`:**

Same reasoning as the original `@ManyToMany` design (see §11): a vehicle either has a feature or it doesn't — duplicates are meaningless. Hibernate's behaviour with `@ElementCollection List` deletes and reinserts all rows on any collection modification; with `Set` it deletes only the removed element.

**How the join table is structured:**

```sql
-- In the tenant schema
vehicle_features (
    vehicle_id VARCHAR(36) NOT NULL,  -- FK → vehicles.id (tenant)
    feature_id VARCHAR(36) NOT NULL   -- plain ref → public.features.id (no DB FK)
)
```

The `vehicle_id` column has a FK to `vehicles.id` in the same tenant schema. The `feature_id` column has no DB FK (PostgreSQL cannot enforce cross-schema FK constraints through Hibernate multi-tenancy). Application-layer existence checks via `PublicCatalogService.allFeaturesExist` enforce referential integrity on write.

**Alternatives considered:**

- **Keep @ManyToMany, configure public schema in tenant search_path** — would allow JPA to navigate the relationship, but exposes public-schema tables to all tenant queries. Breaks schema isolation. Rejected.
- **Duplicate feature data into each tenant schema** — copies public features into the tenant. Keeps JPA relationships intact but introduces eventual consistency: feature name changes in the public schema don't propagate automatically. Rejected.
- **Separate `VehicleFeature` entity with two String FKs** — explicit join entity instead of `@ElementCollection`. More control but more boilerplate for a simple join table with no extra columns.

---

*This document reflects the state of the project as of the initial development phase. Decisions documented here should be revisited as the project scales and requirements evolve.*
