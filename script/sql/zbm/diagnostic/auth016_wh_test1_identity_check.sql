SELECT user_name, status, del_flag, CHAR_LENGTH(password) AS password_length,
       LEFT(password, 4) AS password_prefix
FROM sys_user
WHERE user_name IN ('test_customer', 'test_warehouse', 'test_wh_manager')
ORDER BY user_name;
