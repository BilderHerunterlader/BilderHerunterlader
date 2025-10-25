<?php
require_once('../smarty/libs/Smarty.class.php');

header('Content-Type: text/html; charset=utf-8');

ini_set('error_reporting', E_ALL);

// Start Output Buffer
ob_start();

include_once("includes/inc_escape.php");

// Include the GeSHi library
include_once("geshi/src/geshi.php");
$geshiInstance = new GeSHi();

$pageLanguage = "en";
if (isset($_SERVER["HTTP_ACCEPT_LANGUAGE"])) {
	$lngx = $_SERVER["HTTP_ACCEPT_LANGUAGE"];
	if (stripos('de', $lngx) === false) {
		$pageLanguage = "en";
	} else {
		$pageLanguage = "de";
	}
}

if (isset($_GET['lng']) && ($_GET['lng'] != "")) {
	$lngx = add_slashes($_GET['lng']);
	if ( ($lngx == "de") || ($lngx == "en") ) {
		$pageLanguage = $lngx;
	}
}

include_once("language.php");

// Determine Template File
$pageLocation = "home";
$pageTemplateFile = "home.tpl";
$pageSubMenuTemplateFile = "";
$pageLocationSubStyle = "";
$pageTitleArr = ["home"];
if (isset($_GET['loc']) && ($_GET['loc'] != "")) {
	$loc = add_slashes($_GET['loc']);
	
	$locRedirects = [
		"bilderherunterlader/bh4" => "bilderherunterlader/download"
	];
	
	foreach ($locRedirects as $key => $value) {
		if ($loc === $key) {
			$loc = $value;
			break;
		}
	}
	
	$arrLocs = preg_split("/\//", $loc);
	
	$foundTemplateFile = false;
	$subMenuTemplateFile = "";
	if (is_dir('templates/' . $loc)) {
		$folderIndexTemplateFile = $loc;
		if (substr($loc, -1) !== '/') {
			$folderIndexTemplateFile .= "/";
		}
		$subMenuTemplateFile = $folderIndexTemplateFile . "menu.tpl";
		$folderIndexTemplateFile .= "index.tpl";
		if (file_exists('templates/' . $folderIndexTemplateFile)) {
			$pageTemplateFile = $folderIndexTemplateFile;
			$foundTemplateFile = true;
		}
	} else if (file_exists('templates/' . $loc . '.tpl')) {
		$pageTemplateFile = $loc . ".tpl";
		$subMenuTemplateFile = dirname($loc . '.tpl') . "/menu.tpl";
		$foundTemplateFile = true;
	}
	
	if ($foundTemplateFile) {
		$pageLocation = $loc;
		if (substr($loc, 0, 5) === "irada") {
			$pageLocationSubStyle = "irada/irada.css";
		}
		$pageTitleArr = $arrLocs;
		
		if (file_exists('templates/' . $subMenuTemplateFile)) {
			$pageSubMenuTemplateFile = $subMenuTemplateFile;
		}
	} else {
		http_response_code(404);
		$pageTitleArr = ["error404"];
		$pageTemplateFile = "error404.tpl";
	}
}

$pageTitleArrTranslated = array();
foreach ($pageTitleArr as $pageTitlePart) {
	$pageTitleArrTranslated[] = getLocalizedPageTitlePartText($pageTitlePart);
}

$smarty = new Smarty();

$smarty->setTemplateDir(dirname(__FILE__) . '/templates/');
$smarty->setCompileDir(dirname(__FILE__) . '/templates_c/');
$smarty->setConfigDir(dirname(__FILE__) . '/configs/');
$smarty->setCacheDir(dirname(__FILE__) . '/cache/');
$smarty->addPluginsDir(dirname(__FILE__) . '/../smartyPlugins/');

$smarty->assign('bhUpdatesXMLFile', realpath('../bh/updatev6.xml'));
$smarty->assign('pageLocationTemplate', $pageTemplateFile);
$smarty->assign('pageLocationSubStyle', $pageLocationSubStyle);
$smarty->assign('pageTitleArray', $pageTitleArr);
$smarty->assign('pageTitleArrayTranslated', $pageTitleArrTranslated);
$smarty->assign('pageLanguage', $pageLanguage);
$smarty->assign('pageLocation', $pageLocation);
$smarty->assign('pageSubMenuTemplate', $pageSubMenuTemplateFile);
$smarty->assign('geshiInstance', $geshiInstance);
$smarty->assign('translatedTexts', $poFileTranslatedTexts);

//** un-comment the following line to show the debug console
//$smarty->debugging = true;

$smarty->display('index.tpl');
?>