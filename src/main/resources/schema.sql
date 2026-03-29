-- Site Survey Tool Database Schema
-- Drop existing tables if they exist
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS otp_requests;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- Roles Table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    is_enabled BOOLEAN DEFAULT TRUE,
    is_account_locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_username (username)
);

-- User Roles (Many-to-Many relationship)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- OTP Requests Table (for Forgot Password)
CREATE TABLE otp_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email_otp (email, otp_code),
    INDEX idx_expiry (expiry_time)
);

-- Insert Default Roles
INSERT INTO roles (name, description) VALUES 
    ('ONSITE_ENGINEER', 'Field engineer who conducts site surveys'),
    ('CLIENT', 'Client who owns properties and requests surveys'),
    ('ACCOUNT_MANAGER', 'Manages client accounts and projects'),
    ('FINANCE_MANAGER', 'Handles financial aspects and cost estimates');

-- Create a default admin user (password: Admin@123)
-- Password hash for 'Admin@123' using BCrypt
INSERT INTO users (username, email, phone_number, password_hash, full_name) VALUES 
    ('admin', 'admin@sitesurvey.com', '+1234567890', '$2a$10$xJ3lTqP7G5V8Y9Z1mN2B3OqW4R5E6T7U8I9K0L1M2N3O4P5Q6R7S8', 'System Administrator');

-- Assign admin role (assuming role_id 1 is ONSITE_ENGINEER, create ADMIN role first)
INSERT INTO roles (name, description) VALUES ('ADMIN', 'System Administrator with full access');
INSERT INTO user_roles (user_id, role_id) VALUES (1, 5);