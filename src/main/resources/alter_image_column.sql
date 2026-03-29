-- Alter properties table to support larger images
-- This fixes the "Data too long for column 'image_data'" error

ALTER TABLE properties MODIFY COLUMN image_data MEDIUMBLOB;
