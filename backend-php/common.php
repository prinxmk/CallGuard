<?php
header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');
header('X-Content-Type-Options: nosniff');
$config = require __DIR__ . '/config.php';
function respond($code, $data) { http_response_code($code); echo json_encode($data); exit; }
function db() { global $config; static $pdo = null; if ($pdo === null) { $pdo = new PDO($config['dsn'], $config['db_user'], $config['db_pass'], [PDO::ATTR_ERRMODE=>PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE=>PDO::FETCH_ASSOC]); } return $pdo; }
function phone_number($n) { $n = preg_replace('/[^0-9+]/', '', (string)$n); if (strlen($n) < 5 || strlen($n) > 20) respond(400, ['error'=>'Invalid phone number']); return $n; }
function hash_number($n) { global $config; return hash_hmac('sha256', $n, $config['hash_salt']); }
function require_key() { global $config; $key = isset($_SERVER['HTTP_X_CALLGUARD_KEY']) ? $_SERVER['HTTP_X_CALLGUARD_KEY'] : ''; if (!hash_equals($config['api_key'], $key)) respond(401, ['error'=>'Invalid API key']); }
function json_body() { $d = json_decode(file_get_contents('php://input'), true); if (!is_array($d)) respond(400, ['error'=>'Expected JSON']); return $d; }
