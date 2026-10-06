<?php
// Copy to config.php and set real values. Keep config.php out of public source control.
return [
 'dsn' => 'mysql:host=localhost;dbname=callguard_community;charset=utf8mb4',
 'db_user' => 'CHANGE_ME',
 'db_pass' => 'CHANGE_ME',
 // Long random secret shared with the Android app; rotate before public launch.
 'api_key' => 'REPLACE_WITH_A_LONG_RANDOM_SECRET',
 'hash_salt' => 'REPLACE_WITH_A_DIFFERENT_LONG_RANDOM_SECRET',
 'daily_report_limit' => 20,
];
