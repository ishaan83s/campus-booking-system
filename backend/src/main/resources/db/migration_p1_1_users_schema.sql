-- ==============================================================================
-- Migration / Diagnostic Script: P1-1 users.is_active and users.role schema repair
-- Target Database: MySQL 8 (Aiven)
-- Description:
--   Ensures table 'users' conforms strictly to backend.common.entity.User JPA model:
--     role VARCHAR(50) NOT NULL
--     is_active BOOLEAN/BIT(1) NOT NULL DEFAULT TRUE
--
-- IMPORTANT:
--   DO NOT execute blindly. Run Step 1 (Diagnostics) first to inspect live schema.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- STEP 1: Live Schema Diagnostics (Read-Only)
-- ------------------------------------------------------------------------------
-- Check current columns, types, and defaults for 'users'
SELECT 
    column_name, 
    data_type, 
    column_type, 
    is_nullable, 
    column_default 
FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'users';

-- Check sample data representation
-- SELECT id, email, full_name, role, is_active FROM users LIMIT 10;


-- ------------------------------------------------------------------------------
-- STEP 2: Conditional Non-Destructive Migration Plan
-- ------------------------------------------------------------------------------

-- Scenario A: Legacy bug state
-- In this state, 'is_active' was created as VARCHAR(255) holding Role enum strings ('STUDENT', 'PROFESSOR', 'ADMIN'),
-- and a boolean column named 'active' was created holding the boolean active flag.

-- 1. If 'role' column does not exist:
-- ALTER TABLE users ADD COLUMN role VARCHAR(50) NULL AFTER password_hash;

-- 2. Populate 'role' from 'is_active' if 'is_active' contains role enum strings:
-- UPDATE users 
-- SET role = is_active 
-- WHERE (role IS NULL OR role = '') 
--   AND is_active IN ('STUDENT', 'PROFESSOR', 'ADMIN');

-- 3. Set NOT NULL on 'role' once populated:
-- ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL;

-- 4. If boolean 'active' column exists and 'is_active' is VARCHAR:
--    Safely swap/archive columns:
-- ALTER TABLE users RENAME COLUMN is_active TO legacy_role_column;
-- ALTER TABLE users RENAME COLUMN active TO is_active;
-- (Optionally drop legacy_role_column after verification)
-- ALTER TABLE users DROP COLUMN legacy_role_column;


-- Scenario B: Hibernate ddl-auto=update partially altered schema
-- In this state, 'role' column was added by Hibernate, but 'is_active' is still VARCHAR(255)
-- and 'active' boolean may or may not exist.

-- If 'active' exists (boolean) and 'is_active' is VARCHAR:
-- ALTER TABLE users DROP COLUMN is_active;
-- ALTER TABLE users RENAME COLUMN active TO is_active;

-- If 'active' does NOT exist and 'is_active' is VARCHAR:
-- ALTER TABLE users ADD COLUMN is_active_bool BOOLEAN NOT NULL DEFAULT TRUE;
-- ALTER TABLE users DROP COLUMN is_active;
-- ALTER TABLE users RENAME COLUMN is_active_bool TO is_active;


-- ------------------------------------------------------------------------------
-- STEP 3: Final Verification
-- ------------------------------------------------------------------------------
-- Expected columns in 'users':
--   id: BIGINT AUTO_INCREMENT PRIMARY KEY
--   created_at: DATETIME(6)
--   updated_at: DATETIME(6)
--   full_name: VARCHAR(150) NOT NULL
--   email: VARCHAR(150) UNIQUE NOT NULL
--   password_hash: VARCHAR(255) NOT NULL
--   role: VARCHAR(50) NOT NULL
--   is_active: TINYINT(1) / BIT(1) / BOOLEAN NOT NULL DEFAULT 1
--
-- After verifying the schema matches the above, the production environment can safely
-- set SPRING_JPA_HIBERNATE_DDL_AUTO=validate.
-- ==============================================================================
