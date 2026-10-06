<?php
require __DIR__ . '/common.php'; require_key();
$n = phone_number(isset($_GET['number']) ? $_GET['number'] : ''); $h = hash_number($n);
$q = db()->prepare("SELECT category, COUNT(*) total FROM number_reports WHERE phone_hash=? AND created_at >= DATE_SUB(NOW(), INTERVAL 365 DAY) GROUP BY category ORDER BY total DESC"); $q->execute([$h]); $rows = $q->fetchAll(); $total = 0; $counts = []; foreach ($rows as $r) { $counts[$r['category']] = (int)$r['total']; $total += (int)$r['total']; }
$label = 'Unknown / not yet reported'; if ($total > 0) { arsort($counts); $top = key($counts); $label = ['spam'=>'Community-reported spam','business'=>'Community-reported business / organisation','safe'=>'Community-reported safe caller'][$top]; }
respond(200, ['label'=>$label, 'reports'=>$total, 'categories'=>$counts, 'confidence'=>'community signal only']);
