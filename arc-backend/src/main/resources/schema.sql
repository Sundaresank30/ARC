-- PostgreSQL 17 Table Creation Script for ARC Manufacturing System

-- 1. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Embossing Jobs Table
CREATE TABLE IF NOT EXISTS embossing_jobs (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    part_number VARCHAR(50) NOT NULL UNIQUE,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    embossing_status VARCHAR(20) NOT NULL,
    created_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    embossing_start_time TIMESTAMP WITHOUT TIME ZONE,
    embossing_completed_time TIMESTAMP WITHOUT TIME ZONE,
    machine_status VARCHAR(20) NOT NULL,
    remarks VARCHAR(500),
    test_value DOUBLE PRECISION,
    direction VARCHAR(10),
    attempt VARCHAR(20),
    action VARCHAR(20)
);

-- 3. Embossing Queue Table
CREATE TABLE IF NOT EXISTS embossing_queue (
    id BIGSERIAL PRIMARY KEY,
    part_number VARCHAR(50) NOT NULL,
    serial_number VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    printed_at TIMESTAMP WITHOUT TIME ZONE,
    printed_date DATE
);

-- 4. Production Batches Table
CREATE TABLE IF NOT EXISTS production_batches (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(255) NOT NULL UNIQUE,
    part_no_series VARCHAR(255) NOT NULL,
    part_no_count INTEGER NOT NULL,
    serial_no_series VARCHAR(255) NOT NULL,
    serial_no_count INTEGER NOT NULL,
    total_items INTEGER NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Production Batch Items Table
CREATE TABLE IF NOT EXISTS production_batch_items (
    id BIGSERIAL PRIMARY KEY,
    production_batch_id BIGINT NOT NULL,
    item_index INTEGER NOT NULL,
    part_number VARCHAR(255) NOT NULL,
    serial_number VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_production_batch FOREIGN KEY (production_batch_id) 
        REFERENCES production_batches (id) ON DELETE CASCADE
);

-- 6. Leakage Failures Table
CREATE TABLE IF NOT EXISTS leakage_failures (
    id BIGSERIAL PRIMARY KEY,
    part_no VARCHAR(50) NOT NULL,
    serial_no VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    test_value DOUBLE PRECISION NOT NULL,
    direction VARCHAR(10) NOT NULL,
    timestamp VARCHAR(50) NOT NULL,
    attempt VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL
);

-- 7. Carry Forward Embossing Table
CREATE TABLE IF NOT EXISTS carry_forward_embossing (
    id BIGSERIAL PRIMARY KEY,
    part_no VARCHAR(50) NOT NULL,
    serial_no VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    remaining_since VARCHAR(50) NOT NULL,
    next_shift VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL
);
