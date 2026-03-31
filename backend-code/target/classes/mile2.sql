-- ==========================================
-- MILESTONE 2 - COMPLETE DATABASE SCHEMA
-- Site Survey Tool - Floor Plan & Data Import
-- Run this AFTER Milestone 1 schema
-- ==========================================

USE site_survey_db;

-- ==========================================
-- 1. ORGANIZATIONS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_org_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 2. PROPERTIES TABLE (Campus/Complex)
-- ==========================================
CREATE TABLE IF NOT EXISTS properties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'USA',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    INDEX idx_org (organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 3. BUILDINGS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS buildings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(50),
    floor_count INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    INDEX idx_property (property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4. FLOORS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS floors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building_id BIGINT NOT NULL,
    level_label VARCHAR(50) NOT NULL,
    plan_file_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE,
    INDEX idx_building (building_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 5. FILES TABLE (BLOB Storage)
-- Stores images directly in MySQL
-- Max size: 10MB per file
-- ==========================================
CREATE TABLE IF NOT EXISTS files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_type VARCHAR(50) NOT NULL COMMENT 'floor, space, etc',
    owner_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_data MEDIUMBLOB NOT NULL COMMENT 'Max 16MB',
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner_type, owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 6. SPACES TABLE (Rooms)
-- ==========================================
CREATE TABLE IF NOT EXISTS spaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    floor_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) COMMENT 'room, corridor, closet, etc',
    area_sqm DECIMAL(10,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (floor_id) REFERENCES floors(id) ON DELETE CASCADE,
    INDEX idx_floor (floor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 7. MEMBERSHIPS TABLE
-- Links users to organizations
-- ==========================================
CREATE TABLE IF NOT EXISTS memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'ENGINEER', 'VIEWER') DEFAULT 'VIEWER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_org_user (organization_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- SEED DATA
-- ==========================================

-- Create default organization
INSERT INTO organizations (name) VALUES ('Default ISP Organization')
ON DUPLICATE KEY UPDATE name=name;

-- Get organization ID
SET @org_id = (SELECT id FROM organizations WHERE name = 'Default ISP Organization' LIMIT 1);

-- Link existing admin user to organization
INSERT INTO memberships (organization_id, user_id, role)
SELECT @org_id, u.id, 'ADMIN'
FROM users u
WHERE u.email = 'admin@sitesurvey.com'
ON DUPLICATE KEY UPDATE role=role;

-- Create sample property
INSERT INTO properties (organization_id, name, city, state, country) VALUES 
(@org_id, 'Main Campus', 'San Francisco', 'California', 'USA')
ON DUPLICATE KEY UPDATE name=name;

-- Get property ID
SET @property_id = (SELECT id FROM properties WHERE name = 'Main Campus' LIMIT 1);

-- Create sample building
INSERT INTO buildings (property_id, name, code, floor_count) VALUES 
(@property_id, 'Building A', 'BLD-A', 5)
ON DUPLICATE KEY UPDATE name=name;

-- Get building ID
SET @building_id = (SELECT id FROM buildings WHERE code = 'BLD-A' LIMIT 1);

-- Create sample floors
INSERT INTO floors (building_id, level_label) VALUES 
(@building_id, 'Ground Floor'),
(@building_id, 'First Floor'),
(@building_id, 'Second Floor')
ON DUPLICATE KEY UPDATE level_label=level_label;

-- ==========================================
-- VERIFY INSTALLATION
-- ==========================================
SELECT 'Organizations' as TableName, COUNT(*) as RecordCount FROM organizations
UNION ALL
SELECT 'Properties', COUNT(*) FROM properties
UNION ALL
SELECT 'Buildings', COUNT(*) FROM buildings
UNION ALL
SELECT 'Floors', COUNT(*) FROM floors
UNION ALL
SELECT 'Spaces', COUNT(*) FROM spaces
UNION ALL
SELECT 'Files', COUNT(*) FROM files
UNION ALL
SELECT 'Memberships', COUNT(*) FROM memberships;

-- ==========================================
-- COMPLETE! Schema ready for Milestone 2
-- ==========================================
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');