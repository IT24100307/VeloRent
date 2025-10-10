-- Migration script to fix package booking constraints
-- This allows vehicle_id to be NULL for package bookings

-- Make vehicle_id nullable in bookings table
ALTER TABLE bookings ALTER COLUMN vehicle_id INT NULL;

-- Add a check constraint to ensure either vehicle_id or package_id is set, but not both
ALTER TABLE bookings ADD CONSTRAINT CK_booking_type
CHECK (
    (vehicle_id IS NOT NULL AND package_id IS NULL) OR
    (vehicle_id IS NULL AND package_id IS NOT NULL)
);

-- Expand status column in vehicle_packages to accommodate "Temporarily Unavailable"
ALTER TABLE vehicle_packages ALTER COLUMN status VARCHAR(30);
