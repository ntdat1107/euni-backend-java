-- V20240507_01: Create User Management & RBAC Tables
-- Author: Antigravity (AI Engineering Assistant)
-- Purpose: Schema for Departments, Users, Roles, and Permissions.

DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS faculties CASCADE;

DROP SEQUENCE IF EXISTS faculties_id_seq CASCADE;
DROP SEQUENCE IF EXISTS roles_id_seq CASCADE;
DROP SEQUENCE IF EXISTS permissions_id_seq CASCADE;
DROP SEQUENCE IF EXISTS users_id_seq CASCADE;

CREATE SEQUENCE faculties_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE roles_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE permissions_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 1;

-- 1. Faculties Table
CREATE TABLE faculties (
    id BIGINT DEFAULT nextval('faculties_id_seq') PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 2. Roles Table
CREATE TABLE roles (
    id BIGINT DEFAULT nextval('roles_id_seq') PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 3. Permissions Table
CREATE TABLE permissions (
    id BIGINT DEFAULT nextval('permissions_id_seq') PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 4. Users Table
CREATE TABLE users (
    id BIGINT DEFAULT nextval('users_id_seq') PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    employee_id VARCHAR(50) UNIQUE,
    faculty_id BIGINT REFERENCES faculties(id) ON DELETE SET NULL,
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'Inactive',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    token_version BIGINT NOT NULL DEFAULT 0
);

-- 5. Role-Permissions Mapping
CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 6. User-Roles Mapping
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Performance Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_roles_code ON roles(code);
CREATE INDEX idx_permissions_code ON permissions(code);
