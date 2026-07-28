-- Run this first to create the database:
-- CREATE DATABASE arc_database;

-- Then connect to 'arc_database' and run the following to create the tables:

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Insert default MANAGER and OPERATOR for testing
-- Default credentials: arc_manager / Manager@123 & arc_operator / Operator@123
INSERT INTO users (username, password, role) 
VALUES 
('arc_manager', '$2a$10$Ew.q4w4y2pBOnWzFzO0/0eaK9iI5QzZ7R6s1k4n4L/6/wN7r9o3xG', 'MANAGER'),
('arc_operator', '$2a$10$Ew.q4w4y2pBOnWzFzO0/0eaK9iI5QzZ7R6s1k4n4L/6/wN7r9o3xG', 'OPERATOR')
ON CONFLICT (username) DO NOTHING;

-- Batch Table
CREATE TABLE IF NOT EXISTS batches (
    id SERIAL PRIMARY KEY,
    batch_id VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_count INT DEFAULT 0,
    completed_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Batch Part Number Series
CREATE TABLE IF NOT EXISTS batch_part_series (
    batch_id INT REFERENCES batches(id) ON DELETE CASCADE,
    part_number_series VARCHAR(255) NOT NULL
);

-- Batch Serial Number Series
CREATE TABLE IF NOT EXISTS batch_serial_series (
    batch_id INT REFERENCES batches(id) ON DELETE CASCADE,
    serial_number_series VARCHAR(255) NOT NULL
);

-- Uploaded Document Table
CREATE TABLE IF NOT EXISTS uploaded_documents (
    id SERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    batch_fk_id INT REFERENCES batches(id) ON DELETE SET NULL
);

-- Machines Table
CREATE TABLE IF NOT EXISTS machines (
    id SERIAL PRIMARY KEY,
    machine_name VARCHAR(255) NOT NULL,
    machine_code VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    threshold_config DOUBLE PRECISION
);

-- Production Logs Table
CREATE TABLE IF NOT EXISTS production_logs (
    id SERIAL PRIMARY KEY,
    batch_id INT REFERENCES batches(id) ON DELETE CASCADE,
    production_line_details VARCHAR(255),
    machine_code VARCHAR(100),
    machine_response TEXT,
    status VARCHAR(50),
    log_message TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Leakage Testing Table
CREATE TABLE IF NOT EXISTS leakage_tests (
    id SERIAL PRIMARY KEY,
    batch_id INT REFERENCES batches(id) ON DELETE CASCADE,
    serial_number VARCHAR(255) NOT NULL,
    test_result VARCHAR(50) NOT NULL,
    pressure_value DOUBLE PRECISION,
    tested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
