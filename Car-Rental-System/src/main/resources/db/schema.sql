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
