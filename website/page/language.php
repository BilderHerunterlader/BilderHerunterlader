<?php
$poFilePath = $pageLanguage . ".po";
if (!file_exists($poFilePath)) {
	$poFilePath = "en.po";
}
$poFileTranslatedTexts = loadTranslationsFromPoFile($poFilePath);

//The following array is not actually used in this code, it is only here, so that PoEdit will find the texts in the code
$translatedTexts = array();
$translatedTexts["home"] = gettext("home");
$translatedTexts["useful"] = gettext("useful");
$translatedTexts["contact"] = gettext("contact");
$translatedTexts["features"] = gettext("features");
$translatedTexts["download"] = gettext("download");
$translatedTexts["changelog"] = gettext("changelog");
$translatedTexts["screenshots"] = gettext("screenshots");
$translatedTexts["faq"] = gettext("faq");
$translatedTexts["useful"] = gettext("useful");
$translatedTexts["tutorial"] = gettext("tutorial");
$translatedTexts["rules"] = gettext("rules");
$translatedTexts["hostplugins"] = gettext("hostplugins");
$translatedTexts["options"] = gettext("options");
$translatedTexts["portable"] = gettext("portable");
$translatedTexts["bilderherunterlader"] = gettext("bilderherunterlader");
$translatedTexts["error404"] = gettext("error404");
$translatedTexts["irada"] = gettext("irada");
$translatedTexts["anleitung"] = gettext("anleitung");
$translatedTexts["hostini"] = gettext("hostini");

function loadTranslationsFromPoFile($poFile) {
	$poFileContent = file_get_contents($poFile);
	$poMsgIdAndMsgStrRegex = '/^#\s*.+?\nmsgid "(.+?)"\nmsgstr "(.+?)"/m';
	preg_match_all($poMsgIdAndMsgStrRegex, $poFileContent, $matches, PREG_SET_ORDER);
	
	$poFileTranslatedTexts = array();

	foreach ($matches as $val) {
		$poFileTranslatedTexts[$val[1]] = $val[2];
	}
	return $poFileTranslatedTexts;
}

function getLocalizedText($key) {
	global $poFileTranslatedTexts;
	if (array_key_exists($key, $poFileTranslatedTexts)) {
		return $poFileTranslatedTexts[$key];
	}
	return $key;
}
?>