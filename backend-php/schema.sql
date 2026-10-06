CREATE DATABASE IF NOT EXISTS callguard_community CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE callguard_community;
CREATE TABLE IF NOT EXISTS number_reports (id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, phone_hash CHAR(64) NOT NULL, category ENUM('spam','business','safe') NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_hash(phone_hash), INDEX idx_created(created_at));
CREATE TABLE IF NOT EXISTS api_rate_limits (id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, ip_hash CHAR(64) NOT NULL, day_key DATE NOT NULL, report_count INT UNSIGNED NOT NULL DEFAULT 0, UNIQUE KEY uq_ip_day(ip_hash,day_key));
