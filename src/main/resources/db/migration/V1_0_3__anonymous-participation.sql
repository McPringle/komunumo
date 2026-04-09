ALTER TABLE event ADD COLUMN anonymous_participation_allowed BOOLEAN NOT NULL DEFAULT TRUE AFTER image_id;
