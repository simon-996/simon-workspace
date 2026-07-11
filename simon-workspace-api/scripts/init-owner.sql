-- Configure these values for the target environment before running this file.
-- Restore the CHANGE_ME values immediately after execution so credentials are not committed.
SET @owner_username = 'CHANGE_ME_USERNAME';
SET @owner_nickname = 'CHANGE_ME_NICKNAME';
SET @owner_email = 'CHANGE_ME_EMAIL';
SET @owner_password = 'CHANGE_ME_PASSWORD';

SET @config_ready = (
    @owner_username IS NOT NULL
    AND TRIM(@owner_username) <> ''
    AND @owner_username <> 'CHANGE_ME_USERNAME'
    AND @owner_nickname IS NOT NULL
    AND TRIM(@owner_nickname) <> ''
    AND @owner_nickname <> 'CHANGE_ME_NICKNAME'
    AND @owner_password IS NOT NULL
    AND @owner_password <> 'CHANGE_ME_PASSWORD'
    AND CHAR_LENGTH(@owner_password) >= 8
);
SET @normalized_owner_email = NULLIF(NULLIF(TRIM(@owner_email), ''), 'CHANGE_ME_EMAIL');
SET @owner_role_id = (
    SELECT id
    FROM `role`
    WHERE role_code = 'OWNER' AND deleted = 0
    LIMIT 1
);
SET @username_exists = EXISTS(
    SELECT 1
    FROM `user`
    WHERE username = @owner_username
);
SET @created_user_id = NULL;
SET @owner_role_bound = 0;

START TRANSACTION;

INSERT INTO `user` (
    username,
    password_hash,
    nickname,
    email,
    status,
    reviewed_time,
    review_remark
)
SELECT
    @owner_username,
    CONCAT('sha256:', SHA2(@owner_password, 256)),
    @owner_nickname,
    @normalized_owner_email,
    'ENABLED',
    NOW(),
    'Initialized by scripts/init-owner.sql'
FROM DUAL
WHERE @config_ready = 1
  AND @owner_role_id IS NOT NULL
  AND @username_exists = 0;

SET @created_user_id = IF(ROW_COUNT() = 1, LAST_INSERT_ID(), NULL);

INSERT INTO `user_role` (user_id, role_id)
SELECT @created_user_id, @owner_role_id
FROM DUAL
WHERE @created_user_id IS NOT NULL;

SET @owner_role_bound = ROW_COUNT();

DELETE FROM `user`
WHERE id = @created_user_id
  AND @created_user_id IS NOT NULL
  AND @owner_role_bound <> 1;

SET @created_user_id = IF(@owner_role_bound = 1, @created_user_id, NULL);

COMMIT;

SELECT
    CASE
        WHEN @config_ready = 0 THEN 'CONFIG_REQUIRED'
        WHEN @owner_role_id IS NULL THEN 'OWNER_ROLE_MISSING'
        WHEN @username_exists = 1 THEN 'USERNAME_EXISTS'
        WHEN @created_user_id IS NOT NULL THEN 'OWNER_CREATED'
        ELSE 'OWNER_NOT_CREATED'
    END AS initialization_status,
    @created_user_id AS owner_user_id,
    IF(@created_user_id IS NOT NULL, @owner_username, NULL) AS owner_username;
