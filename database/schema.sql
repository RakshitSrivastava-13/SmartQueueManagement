-- Smart Hospital Queue Management System
-- MySQL Database Schema

-- Drop existing tables if they exist
DROP TABLE IF EXISTS queue_status;
DROP TABLE IF EXISTS tokens;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS patients;

-- Create departments table
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(255),
    floor_number INT,
    room_numbers VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create doctors table
CREATE TABLE doctors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100),
    qualification VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    department_id BIGINT NOT NULL,
    room_number VARCHAR(20),
    consultation_duration_minutes INT DEFAULT 15,
    max_patients_per_day INT DEFAULT 50,
    is_available BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Create patients table
CREATE TABLE patients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    pincode VARCHAR(10),
    blood_group VARCHAR(5),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(15),
    is_senior_citizen BOOLEAN DEFAULT FALSE,
    is_pregnant BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create staff table
CREATE TABLE staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    role ENUM('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE') NOT NULL,
    department_id BIGINT,
    doctor_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- Create tokens table
CREATE TABLE tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_number VARCHAR(20) NOT NULL,
    patient_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    doctor_id BIGINT,
    token_date DATE NOT NULL,
    priority ENUM('NORMAL', 'SENIOR_CITIZEN', 'PREGNANT', 'EMERGENCY', 'VIP') DEFAULT 'NORMAL',
    priority_score INT DEFAULT 0,
    status ENUM('WAITING', 'CALLED', 'IN_CONSULTATION', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'WAITING',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    called_at TIMESTAMP NULL,
    consultation_started_at TIMESTAMP NULL,
    consultation_ended_at TIMESTAMP NULL,
    notes VARCHAR(500),
    created_by BIGINT,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (created_by) REFERENCES staff(id),
    UNIQUE KEY unique_token_per_day (token_number, token_date)
);

-- Create queue_status table for real-time tracking
CREATE TABLE queue_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_id BIGINT NOT NULL,
    doctor_id BIGINT,
    current_token_id BIGINT,
    total_waiting INT DEFAULT 0,
    average_wait_time_minutes INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (current_token_id) REFERENCES tokens(id)
);

-- Create indexes for performance
CREATE INDEX idx_tokens_date ON tokens(token_date);
CREATE INDEX idx_tokens_status ON tokens(status);
CREATE INDEX idx_tokens_doctor ON tokens(doctor_id, token_date, status);
CREATE INDEX idx_tokens_department ON tokens(department_id, token_date, status);
CREATE INDEX idx_patients_phone ON patients(phone);
CREATE INDEX idx_doctors_department ON doctors(department_id);

-- Insert sample departments
INSERT INTO departments (name, code, description, floor_number, room_numbers) VALUES
('General Medicine', 'OPD', 'General OPD and Primary Care', 1, '101-110'),
('Cardiology', 'CARD', 'Heart and Cardiovascular Care', 2, '201-205'),
('Orthopedics', 'ORTH', 'Bone and Joint Care', 2, '206-210'),
('Pediatrics', 'PED', 'Child Healthcare', 1, '111-115'),
('Gynecology', 'GYN', 'Women Health Care', 3, '301-305'),
('Dermatology', 'DERM', 'Skin Care', 1, '116-118'),
('ENT', 'ENT', 'Ear Nose Throat', 2, '211-213'),
('Ophthalmology', 'EYE', 'Eye Care', 3, '306-308'),
('Diagnostics', 'DIAG', 'Laboratory and Diagnostic Tests', 0, 'G01-G10'),
('Emergency', 'EMER', 'Emergency Services', 0, 'E01-E05');

-- Insert sample doctors
INSERT INTO doctors (employee_id, first_name, last_name, specialization, qualification, email, phone, department_id, room_number, consultation_duration_minutes, max_patients_per_day) VALUES
('DOC001', 'Rajesh', 'Sharma', 'General Physician', 'MBBS, MD', 'rajesh.sharma@hospital.com', '9876543210', 1, '101', 10, 60),
('DOC002', 'Priya', 'Patel', 'Cardiologist', 'MBBS, DM Cardiology', 'priya.patel@hospital.com', '9876543211', 2, '201', 15, 40),
('DOC003', 'Amit', 'Kumar', 'Orthopedic Surgeon', 'MBBS, MS Ortho', 'amit.kumar@hospital.com', '9876543212', 3, '206', 15, 35),
('DOC004', 'Sneha', 'Reddy', 'Pediatrician', 'MBBS, MD Pediatrics', 'sneha.reddy@hospital.com', '9876543213', 4, '111', 12, 50),
('DOC005', 'Kavita', 'Singh', 'Gynecologist', 'MBBS, MS OBG', 'kavita.singh@hospital.com', '9876543214', 5, '301', 15, 40),
('DOC006', 'Suresh', 'Nair', 'General Physician', 'MBBS, MD', 'suresh.nair@hospital.com', '9876543215', 1, '102', 10, 60),
('DOC007', 'Meera', 'Iyer', 'Dermatologist', 'MBBS, MD Dermatology', 'meera.iyer@hospital.com', '9876543216', 6, '116', 12, 45),
('DOC008', 'Vikram', 'Joshi', 'ENT Specialist', 'MBBS, MS ENT', 'vikram.joshi@hospital.com', '9876543217', 7, '211', 15, 40);

-- Insert sample staff (passwords are BCrypt encoded - default: password123)
-- Note: In production, use proper password encoding
INSERT INTO staff (employee_id, username, password, first_name, last_name, email, phone, role, department_id, doctor_id) VALUES
('EMP001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGf9ANwI8cDK4Rq9oFmvXQdQv.Qy', 'Admin', 'User', 'admin@hospital.com', '9876543200', 'ADMIN', NULL, NULL),
('EMP002', 'doctor1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGf9ANwI8cDK4Rq9oFmvXQdQv.Qy', 'Rajesh', 'Sharma', 'rajesh.staff@hospital.com', '9876543201', 'DOCTOR', 1, 1),
('EMP003', 'staff1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGf9ANwI8cDK4Rq9oFmvXQdQv.Qy', 'Reception', 'Staff', 'reception@hospital.com', '9876543202', 'RECEPTIONIST', 1, NULL),
('EMP004', 'nurse1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGf9ANwI8cDK4Rq9oFmvXQdQv.Qy', 'Nurse', 'One', 'nurse1@hospital.com', '9876543203', 'NURSE', 1, NULL),
('EMP005', 'doctor2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGf9ANwI8cDK4Rq9oFmvXQdQv.Qy', 'Priya', 'Patel', 'priya.staff@hospital.com', '9876543204', 'DOCTOR', 2, 2);

-- Initialize queue_status for each doctor
INSERT INTO queue_status (department_id, doctor_id, total_waiting, average_wait_time_minutes)
SELECT department_id, id, 0, consultation_duration_minutes FROM doctors WHERE is_active = TRUE;

-- Create view for active queue
CREATE OR REPLACE VIEW active_queue_view AS
SELECT 
    t.id,
    t.token_number,
    t.token_date,
    t.priority,
    t.status,
    t.generated_at,
    p.first_name AS patient_first_name,
    p.last_name AS patient_last_name,
    p.phone AS patient_phone,
    d.name AS department_name,
    d.code AS department_code,
    CONCAT(doc.first_name, ' ', doc.last_name) AS doctor_name,
    doc.room_number
FROM tokens t
JOIN patients p ON t.patient_id = p.id
JOIN departments d ON t.department_id = d.id
LEFT JOIN doctors doc ON t.doctor_id = doc.id
WHERE t.status IN ('WAITING', 'CALLED', 'IN_CONSULTATION')
AND t.token_date = CURDATE()
ORDER BY 
    CASE t.priority 
        WHEN 'EMERGENCY' THEN 1 
        WHEN 'PREGNANT' THEN 2 
        WHEN 'SENIOR_CITIZEN' THEN 3 
        WHEN 'VIP' THEN 4 
        ELSE 5 
    END,
    t.priority_score DESC,
    t.generated_at ASC;

-- Stored procedure to generate token number
DELIMITER //
CREATE PROCEDURE generate_token_number(
    IN p_department_code VARCHAR(10),
    OUT p_token_number VARCHAR(20)
)
BEGIN
    DECLARE v_count INT;
    DECLARE v_date_str VARCHAR(8);
    
    SET v_date_str = DATE_FORMAT(CURDATE(), '%Y%m%d');
    
    SELECT COUNT(*) + 1 INTO v_count
    FROM tokens t
    JOIN departments d ON t.department_id = d.id
    WHERE d.code = p_department_code
    AND t.token_date = CURDATE();
    
    SET p_token_number = CONCAT(p_department_code, '-', v_date_str, '-', LPAD(v_count, 4, '0'));
END //
DELIMITER ;

-- Stored procedure to calculate estimated waiting time
DELIMITER //
CREATE PROCEDURE calculate_waiting_time(
    IN p_token_id BIGINT,
    OUT p_waiting_time INT
)
BEGIN
    DECLARE v_position INT;
    DECLARE v_avg_time INT;
    DECLARE v_doctor_id BIGINT;
    
    -- Get doctor ID for the token
    SELECT doctor_id INTO v_doctor_id FROM tokens WHERE id = p_token_id;
    
    -- Get average consultation time for the doctor
    SELECT COALESCE(
        AVG(TIMESTAMPDIFF(MINUTE, consultation_started_at, consultation_ended_at)),
        (SELECT consultation_duration_minutes FROM doctors WHERE id = v_doctor_id)
    ) INTO v_avg_time
    FROM tokens
    WHERE doctor_id = v_doctor_id
    AND status = 'COMPLETED'
    AND consultation_ended_at >= DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- Get position in queue
    SELECT COUNT(*) INTO v_position
    FROM tokens
    WHERE doctor_id = v_doctor_id
    AND token_date = CURDATE()
    AND status IN ('WAITING', 'CALLED', 'IN_CONSULTATION')
    AND (
        priority_score > (SELECT priority_score FROM tokens WHERE id = p_token_id)
        OR (priority_score = (SELECT priority_score FROM tokens WHERE id = p_token_id) 
            AND generated_at < (SELECT generated_at FROM tokens WHERE id = p_token_id))
    );
    
    SET p_waiting_time = v_position * COALESCE(v_avg_time, 15);
END //
DELIMITER ;
