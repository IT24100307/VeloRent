-- Insert roles if they don't exist
INSERT INTO roles (name) 
SELECT 'ROLE_CUSTOMER' 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_CUSTOMER');

INSERT INTO roles (name) 
SELECT 'ROLE_FLEET_MANAGER' 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_FLEET_MANAGER');

INSERT INTO roles (name) 
SELECT 'ROLE_SYSTEM_ADMIN' 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SYSTEM_ADMIN');

INSERT INTO roles (name) 
SELECT 'ROLE_OWNER' 
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_OWNER');

-- Insert sample users for testing (only if they don't exist)
INSERT INTO users (first_name, last_name, email, password, role_id)
SELECT 'John', 'Doe', 'john.doe@example.com', '$2a$10$DWKz.ABC123DEF456GHI789JKL012MNO345PQR678STU', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john.doe@example.com');

INSERT INTO users (first_name, last_name, email, password, role_id)
SELECT 'Jane', 'Smith', 'jane.smith@example.com', '$2a$10$DWKz.ABC123DEF456GHI789JKL012MNO345PQR678STU', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'jane.smith@example.com');

INSERT INTO users (first_name, last_name, email, password, role_id)
SELECT 'Mike', 'Johnson', 'mike.johnson@example.com', '$2a$10$DWKz.ABC123DEF456GHI789JKL012MNO345PQR678STU', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'mike.johnson@example.com');

-- Insert customer records for the sample users
INSERT INTO customers (user_id, contact_number, address_street, address_city, address_postal_code)
SELECT u.user_id, '+1-555-0101', '123 Main St', 'New York', '10001'
FROM users u
WHERE u.email = 'john.doe@example.com' 
AND NOT EXISTS (SELECT 1 FROM customers c WHERE c.user_id = u.user_id);

INSERT INTO customers (user_id, contact_number, address_street, address_city, address_postal_code)
SELECT u.user_id, '+1-555-0102', '456 Oak Ave', 'Los Angeles', '90210'
FROM users u
WHERE u.email = 'jane.smith@example.com' 
AND NOT EXISTS (SELECT 1 FROM customers c WHERE c.user_id = u.user_id);

INSERT INTO customers (user_id, contact_number, address_street, address_city, address_postal_code)
SELECT u.user_id, '+1-555-0103', '789 Pine Rd', 'Chicago', '60601'
FROM users u
WHERE u.email = 'mike.johnson@example.com' 
AND NOT EXISTS (SELECT 1 FROM customers c WHERE c.user_id = u.user_id);

-- Insert sample feedback data
INSERT INTO feedback (rating, comments, customer_name, customer_id, feedback_date)
SELECT 5, 'Excellent service! The BMW X5 was in perfect condition and the staff was very professional. Highly recommend VeloRent for luxury car rentals.', 'John Doe', u.user_id, DATEADD(day, -3, GETDATE())
FROM users u
WHERE u.email = 'john.doe@example.com'
AND NOT EXISTS (SELECT 1 FROM feedback f WHERE f.customer_id = u.user_id AND f.comments LIKE '%BMW X5%');

INSERT INTO feedback (rating, comments, customer_name, customer_id, feedback_date)
SELECT 4, 'Great experience overall. The Mercedes C-Class was beautiful and drove smoothly. Only minor issue was the pickup time was slightly delayed.', 'Jane Smith', u.user_id, DATEADD(day, -7, GETDATE())
FROM users u
WHERE u.email = 'jane.smith@example.com'
AND NOT EXISTS (SELECT 1 FROM feedback f WHERE f.customer_id = u.user_id AND f.comments LIKE '%Mercedes C-Class%');

INSERT INTO feedback (rating, comments, customer_name, customer_id, feedback_date)
SELECT 5, 'Outstanding service from start to finish! The Audi A8 exceeded my expectations. Clean, well-maintained, and the booking process was seamless.', 'Mike Johnson', u.user_id, DATEADD(day, -1, GETDATE())
FROM users u
WHERE u.email = 'mike.johnson@example.com'
AND NOT EXISTS (SELECT 1 FROM feedback f WHERE f.customer_id = u.user_id AND f.comments LIKE '%Audi A8%');
