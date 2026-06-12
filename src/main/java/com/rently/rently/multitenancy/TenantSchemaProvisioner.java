package com.rently.rently.multitenancy;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class TenantSchemaProvisioner {

    private final DataSource dataSource;

    public TenantSchemaProvisioner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void provisionSchema(String slug) {
        validateSlug(slug);
        try (Connection conn = dataSource.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS \"" + slug + "\"");
                stmt.execute("SET search_path TO \"" + slug + "\"");
                createBranchesTable(stmt);
                createHubsTable(stmt);
                createVehiclesTable(stmt);
                createVehicleFeaturesTable(stmt);
                createCustomersTable(stmt);
                createReservationsTable(stmt);
                createPaymentsTable(stmt);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to provision schema for tenant: " + slug, e);
        }
    }

    private void validateSlug(String slug) {
        if (slug == null || !slug.matches("[a-z][a-z0-9-]*")) {
            throw new IllegalArgumentException("Invalid tenant slug: " + slug);
        }
    }

    private void createBranchesTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS branches (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(150) NOT NULL UNIQUE,
                    city VARCHAR(255) NOT NULL,
                    address VARCHAR(255),
                    phone VARCHAR(50),
                    is_active BOOLEAN NOT NULL DEFAULT true,
                    created_at TIMESTAMPTZ NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                )
                """);
    }

    private void createHubsTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS hubs (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    city VARCHAR(255) NOT NULL,
                    address VARCHAR(255),
                    is_active BOOLEAN NOT NULL DEFAULT true,
                    branch_id VARCHAR(36) NOT NULL REFERENCES branches(id),
                    created_at TIMESTAMPTZ NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                )
                """);
    }

    private void createVehiclesTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS vehicles (
                    id VARCHAR(36) PRIMARY KEY,
                    license_plate VARCHAR(20) NOT NULL UNIQUE,
                    insurance_number VARCHAR(20) NOT NULL UNIQUE,
                    insurance_expires_at DATE NOT NULL,
                    year SMALLINT,
                    month SMALLINT,
                    color VARCHAR(50),
                    mileage INTEGER NOT NULL DEFAULT 0,
                    seats SMALLINT,
                    doors SMALLINT,
                    description TEXT,
                    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
                    transmission VARCHAR(20),
                    fuel_type VARCHAR(20),
                    daily_base_rate NUMERIC(10,2),
                    model_id VARCHAR(36) NOT NULL,
                    current_hub_id VARCHAR(36) REFERENCES hubs(id),
                    current_parking_slot VARCHAR(100),
                    created_at TIMESTAMPTZ NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                )
                """);
    }

    private void createVehicleFeaturesTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS vehicle_features (
                    vehicle_id VARCHAR(36) NOT NULL REFERENCES vehicles(id),
                    feature_id VARCHAR(36) NOT NULL,
                    PRIMARY KEY (vehicle_id, feature_id)
                )
                """);
    }

    private void createCustomersTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id VARCHAR(36) PRIMARY KEY,
                    first_name VARCHAR(255) NOT NULL,
                    last_name VARCHAR(255) NOT NULL,
                    phone VARCHAR(50) NOT NULL,
                    email VARCHAR(255),
                    id_type VARCHAR(20) NOT NULL,
                    id_number VARCHAR(255) NOT NULL,
                    driver_license_code VARCHAR(255) NOT NULL,
                    address VARCHAR(255),
                    created_at TIMESTAMPTZ NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL,
                    UNIQUE (id_type, id_number)
                )
                """);
    }

    private void createReservationsTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id VARCHAR(36) PRIMARY KEY,
                    customer_id VARCHAR(36) NOT NULL REFERENCES customers(id),
                    vehicle_id VARCHAR(36) NOT NULL REFERENCES vehicles(id),
                    pickup_hub_id VARCHAR(36) NOT NULL REFERENCES hubs(id),
                    return_hub_id VARCHAR(36) NOT NULL REFERENCES hubs(id),
                    start_date TIMESTAMP NOT NULL,
                    end_date TIMESTAMP NOT NULL,
                    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                    total_amount NUMERIC(10,2) NOT NULL,
                    is_digitally_signed BOOLEAN NOT NULL DEFAULT false,
                    is_physically_printed BOOLEAN NOT NULL DEFAULT false,
                    signature_base64 TEXT,
                    contract_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                    created_by VARCHAR(36) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                )
                """);
    }

    private void createPaymentsTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS payments (
                    id VARCHAR(36) PRIMARY KEY,
                    reservation_id VARCHAR(36) NOT NULL UNIQUE REFERENCES reservations(id),
                    total_contract_amount NUMERIC(10,2) NOT NULL,
                    cash_advanced NUMERIC(10,2) NOT NULL DEFAULT 0,
                    bank_transfer_reference VARCHAR(255),
                    bank_transfer_image_url VARCHAR(500),
                    deposit_type VARCHAR(30) NOT NULL,
                    deposit_amount NUMERIC(10,2) NOT NULL,
                    deposit_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE_HOLD',
                    cheque_number VARCHAR(100),
                    credit_card_auth_reference VARCHAR(255),
                    deposit_released_at TIMESTAMPTZ,
                    deposit_released_by VARCHAR(36),
                    created_at TIMESTAMPTZ NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL
                )
                """);
    }
}
