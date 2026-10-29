ALTER TABLE product
    ALTER COLUMN status SET DEFAULT 'ACTIVO';

UPDATE product
SET status = CASE status
                 WHEN 'ACTIVE' THEN 'ACTIVO'
                 WHEN 'ARCHIVED' THEN 'ARCHIVADO'
                 ELSE status
    END;