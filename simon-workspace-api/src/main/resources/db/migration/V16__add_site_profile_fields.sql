ALTER TABLE site_config
    ADD COLUMN profile_bio TEXT NULL COMMENT 'Public profile biography' AFTER hero_subtitle,
    ADD COLUMN tech_stack_json TEXT NULL COMMENT 'Public technology stack JSON' AFTER profile_bio;
