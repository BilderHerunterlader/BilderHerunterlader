<?php
function add_slashes($string) {
	// String escapen
	$ret = addslashes($string);
	$ret = htmlentities($ret);
	return $ret;
}
?>