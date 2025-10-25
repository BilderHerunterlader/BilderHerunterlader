<?php
$poFilePath = $pageLanguage . ".po";
if (!file_exists($poFilePath)) {
	$poFilePath = "en.po";
}
$poFileTranslatedTexts = loadTranslationsFromPoFile($poFilePath);

//The following array is defined so that PoEdit will find the texts in the code. The actual texts are used dynamically, so they can't be translated otherwise.
// The assigned values in the array are not translated, but the String without gettext() around it. Example key: home value: Home
$translatedTexts = array();
$translatedTexts["home"] = gettext("Home");
$translatedTexts["useful"] = gettext("Useful");
$translatedTexts["contact"] = gettext("Contact");
$translatedTexts["features"] = gettext("Features");
$translatedTexts["download"] = gettext("Download");
$translatedTexts["changelog"] = gettext("Changelog");
$translatedTexts["screenshots"] = gettext("Screenshots");
$translatedTexts["faq"] = gettext("FAQ");
$translatedTexts["tutorial"] = gettext("Tutorial");
$translatedTexts["rules"] = gettext("Rules");
$translatedTexts["hostplugins"] = gettext("Host-Plugins");
$translatedTexts["options"] = gettext("Options");
$translatedTexts["portable"] = gettext("Portable");
$translatedTexts["bilderherunterlader"] = gettext("BilderHerunterlader");
$translatedTexts["error404"] = "Error 404";
$translatedTexts["irada"] = gettext("Irada");
$translatedTexts["anleitung"] = gettext("Anleitung");
$translatedTexts["hostini"] = gettext("hostini");

function loadTranslationsFromPoFile($poFile) {
	$poFileTranslatedTexts = array();
	$poFileContent = file_get_contents($poFile);
	
	$poStartFound = false;
	$poEntryKey = false;
	$poEntryValue = false;
	$poEntryStrKey = "";
	$poEntryStrValue = "";
	$lines = explode("\n", $poFileContent);
	foreach ($lines as $line) {
		if ($poStartFound == false && str_starts_with($line, "#")) {
			$poStartFound = true;
		}
		
		if ($poStartFound == false) {
			continue;
		}

		if (str_starts_with($line, "msgid")) {
			$poEntryKey = true;
			$poEntryValue = false;
			if (strlen($poEntryStrKey) > 0 && strlen($poEntryStrValue) > 0) {
				$poFileTranslatedTexts[str_replace("\\n", "\n", $poEntryStrKey)] = $poEntryStrValue;
				
			}
			$poEntryStrKey = "";
			$poEntryStrValue = "";
		} else if (str_starts_with($line, "msgstr")) {
			$poEntryKey = false;
			$poEntryValue = true;
			$poEntryStrValue = "";
		}
		
		if (($poEntryKey || $poEntryValue) && preg_match('/"([^"]*)"/', $line, $matches)) {
			if ($poEntryKey) {
				$poEntryStrKey .= $matches[1];
			}
			if ($poEntryValue) {
				$poEntryStrValue .= $matches[1];
			}
		}
	}
	
	if (strlen($poEntryStrKey) > 0 && strlen($poEntryStrValue) > 0) {
		$poFileTranslatedTexts[str_replace("\\n", "\n", $poEntryStrKey)] = $poEntryStrValue;
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

function getLocalizedPageTitlePartText($key) {
	global $poFileTranslatedTexts;
	global $translatedTexts;
	$mappedKey = $key;
	if (array_key_exists($key, $translatedTexts)) {
		$mappedKey = $translatedTexts[$key];
	}
	if (array_key_exists($mappedKey, $poFileTranslatedTexts)) {
		return $poFileTranslatedTexts[$mappedKey];
	}
	return $key;
}
?>