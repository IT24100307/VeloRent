-- SQL script to add the required roles if they don't exist
-- This script should be run once when setting up the application

-- Check if roles table exists
IF OBJECT_ID('roles', 'U') IS NULL
BEGIN
    CREATE TABLE roles (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE
    );
END

-- Insert roles if they don't exist
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_CUSTOMER')
BEGIN
    INSERT INTO roles (name) VALUES ('ROLE_CUSTOMER');
END

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_FLEET_MANAGER')
BEGIN
    INSERT INTO roles (name) VALUES ('ROLE_FLEET_MANAGER');
END

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SYSTEM_ADMIN')
BEGIN
    INSERT INTO roles (name) VALUES ('ROLE_SYSTEM_ADMIN');
END

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_OWNER')
BEGIN
    INSERT INTO roles (name) VALUES ('ROLE_OWNER');
END

SELECT 'Roles initialized successfully' AS Message;
