<?php
$protocol = stripos($_SERVER['SERVER_PROTOCOL'],'https') === 0 ? 'https://' : 'http://';
$host = $_SERVER['HTTP_HOST'];
$uri = rtrim(dirname($_SERVER['PHP_SELF']), '/\\');
$folder = 'page/';
$redirectURL = $protocol . $host . $uri . '/' . $folder;
header("Location: $redirectURL");
exit();
?>
Error