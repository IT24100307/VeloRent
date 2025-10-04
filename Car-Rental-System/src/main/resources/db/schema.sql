-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id INT PRIMARY KEY IDENTITY(1,1),
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id INT,
    is_2fa_enabled BIT DEFAULT 0,
    secret VARCHAR(255),
    CONSTRAINT FK_users_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    user_id INT PRIMARY KEY,
    contact_number VARCHAR(20),
    address_street VARCHAR(100),
    address_city VARCHAR(50),
    address_postal_code VARCHAR(10),
    CONSTRAINT FK_customers_users FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Create staff table
CREATE TABLE IF NOT EXISTS staff (
    user_id INT PRIMARY KEY,
    staff_id_code VARCHAR(20) UNIQUE,
    hire_date DATE,
    CONSTRAINT FK_staff_users FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Create vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id INT PRIMARY KEY IDENTITY(1,1),
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    daily_rate DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'Available',
    image_url VARCHAR(255),
    created_at DATETIME DEFAULT GETDATE()
);

-- Create vehicle_packages table
CREATE TABLE IF NOT EXISTS vehicle_packages (
    package_id INT PRIMARY KEY IDENTITY(1,1),
    package_name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'Activated',
    duration INT NOT NULL,
    image_url VARCHAR(255),
    description TEXT,
    created_at DATETIME DEFAULT GETDATE()
);

-- Create junction table for package vehicles
CREATE TABLE IF NOT EXISTS vehicle_package_vehicles (
    package_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    PRIMARY KEY (package_id, vehicle_id),
    CONSTRAINT FK_package_vehicles_packages FOREIGN KEY (package_id) REFERENCES vehicle_packages(package_id),
    CONSTRAINT FK_package_vehicles_vehicles FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
);

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY IDENTITY(1,1),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    booking_status VARCHAR(20) DEFAULT 'Confirmed',
    customer_id INT NOT NULL,
    vehicle_id INT,
    package_id INT,
    managed_by_staff_id INT,
    booking_type VARCHAR(20) DEFAULT 'VEHICLE', -- 'VEHICLE' or 'PACKAGE'
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_bookings_customers FOREIGN KEY (customer_id) REFERENCES customers(user_id),
    CONSTRAINT FK_bookings_vehicles FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    CONSTRAINT FK_bookings_packages FOREIGN KEY (package_id) REFERENCES vehicle_packages(package_id),
    CONSTRAINT FK_bookings_staff FOREIGN KEY (managed_by_staff_id) REFERENCES staff(user_id)
);

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY IDENTITY(1,1),
    payment_date DATETIME NOT NULL DEFAULT GETDATE(),
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'Completed',
    transaction_id VARCHAR(100),
    booking_id INT NOT NULL UNIQUE,
    CONSTRAINT FK_payments_bookings FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);
