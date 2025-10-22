CREATE TABLE roles (
    id INT PRIMARY KEY IDENTITY(1,1),
    name VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id INT
);


CREATE TABLE customers (
    user_id INT PRIMARY KEY,
    contact_number VARCHAR(20),
    address_street VARCHAR(100),
    address_city VARCHAR(50),
    address_postal_code VARCHAR(10)
);


CREATE TABLE staff (
    user_id INT PRIMARY KEY,
    staff_id_code VARCHAR(20) UNIQUE,
    hire_date DATE
);


CREATE TABLE vehicles (
    vehicle_id INT PRIMARY KEY IDENTITY(1,1),
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    registration_number VARCHAR(20) NOT NULL UNIQUE,
    rental_rate_per_day DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'Available'
);


CREATE TABLE bookings (
    booking_id INT PRIMARY KEY IDENTITY(1,1),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    booking_status VARCHAR(50) DEFAULT 'Confirmed',
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    managed_by_staff_id INT
);


CREATE TABLE payments (
    payment_id INT PRIMARY KEY IDENTITY(1,1),
    payment_date DATETIME NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    booking_id INT NOT NULL UNIQUE
);


CREATE TABLE maintenance_records (
    maintenance_id INT PRIMARY KEY IDENTITY(1,1),
    maintenance_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    cost DECIMAL(10, 2),
    vehicle_id INT NOT NULL
);


CREATE TABLE feedback (
    feedback_id INT PRIMARY KEY IDENTITY(1,1),
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comments TEXT,
    feedback_date DATETIME DEFAULT GETDATE(),
    customer_id INT NOT NULL
);


CREATE TABLE offers (
    offer_id INT PRIMARY KEY IDENTITY(1,1),
    name VARCHAR(255) NOT NULL,
    discount DECIMAL(5,2) NOT NULL,
    start_date DATE,
    end_date DATE,
    is_active BIT NOT NULL DEFAULT 1
);

CREATE TABLE vehicle_packages (
    package_id INT PRIMARY KEY IDENTITY(1,1),
    package_name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'Activated', -- 'Activated' or 'Deactivated'
    duration INT NOT NULL, -- duration in days (can adjust as needed)
    image_url VARCHAR(255)
);

-- Junction table for many-to-many relationship
CREATE TABLE vehicle_package_vehicles (
    id INT PRIMARY KEY IDENTITY(1,1),
    package_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    CONSTRAINT FK_vpv_package FOREIGN KEY (package_id) REFERENCES vehicle_packages(package_id),
    CONSTRAINT FK_vpv_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    CONSTRAINT UQ_package_vehicle UNIQUE (package_id, vehicle_id) -- prevent duplicates
);

-- Relationships (Foreign Keys)
ALTER TABLE users
ADD CONSTRAINT FK_users_roles FOREIGN KEY (role_id) REFERENCES roles(id);


ALTER TABLE customers
ADD CONSTRAINT FK_customers_users FOREIGN KEY (user_id) REFERENCES users(user_id);


ALTER TABLE staff
ADD CONSTRAINT FK_staff_users FOREIGN KEY (user_id) REFERENCES users(user_id);


ALTER TABLE bookings
ADD CONSTRAINT FK_bookings_customers FOREIGN KEY (customer_id) REFERENCES customers(user_id),
    CONSTRAINT FK_bookings_vehicles FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    CONSTRAINT FK_bookings_staff FOREIGN KEY (managed_by_staff_id) REFERENCES staff(user_id);


ALTER TABLE payments
ADD CONSTRAINT FK_payments_bookings FOREIGN KEY (booking_id) REFERENCES bookings(booking_id);


ALTER TABLE maintenance_records
ADD CONSTRAINT FK_maintenance_vehicles FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id);


ALTER TABLE feedback
ADD CONSTRAINT FK_feedback_customers FOREIGN KEY (customer_id) REFERENCES customers(user_id);


-- Insert Default Roles
INSERT INTO roles (name) VALUES
('ROLE_CUSTOMER'),
('ROLE_FLEET_MANAGER'),
('ROLE_SYSTEM_ADMIN'),
('ROLE_OWNER');


-- View Data in All Tables
-- User and Role Tables
SELECT * FROM roles;
SELECT * FROM users;
SELECT * FROM customers;
SELECT * FROM staff;

-- Core Business Tables
SELECT * FROM vehicles;
SELECT * FROM bookings;
SELECT * FROM payments;
SELECT * FROM offers; 
SELECT * FROM vehicle_packages;
SELECT * FROM vehicle_package_vehicles;

-- Supporting Tables
SELECT * FROM maintenance_records;
SELECT * FROM feedback;
