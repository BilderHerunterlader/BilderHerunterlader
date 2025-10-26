<?php
$poFilePath = $pageLanguage . ".po";
if (!file_exists($poFilePath)) {
	$poFilePath = "en.po";
}
$poFileTranslatedTexts = loadTranslationsFromPoFile($poFilePath, true);

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

function loadTranslationsFromPoFile($poFile, $includeFuzzy) {
	$poFileTranslatedTexts = array();
	$poFileContent = file_get_contents($poFile);
	
	$poStartFound = false;
	$poEntryKey = false;
	$poEntryValue = false;
	$poEntryFuzzy = false;
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

		if (str_starts_with($line, "msgid") || str_starts_with($line, "#~ msgid")) {
			if (strlen($poEntryStrKey) > 0 && strlen($poEntryStrValue) > 0 && ($includeFuzzy == true || $poEntryFuzzy == false)) {
				$poFileTranslatedTexts[$poEntryStrKey] = $poEntryStrValue;
			}
			$poEntryKey = true;
			$poEntryValue = false;
			if (str_starts_with($line, "#~ msgid")) {
				$poEntryFuzzy = true;
			} else {
				$poEntryFuzzy = false;
			}
			$poEntryStrKey = "";
			$poEntryStrValue = "";
		} else if (str_starts_with($line, "msgstr") || str_starts_with($line, "#~ msgstr")) {
			$poEntryKey = false;
			$poEntryValue = true;
			if (str_starts_with($line, "#~ msgstr")) {
				$poEntryFuzzy = true;
			} else {
				$poEntryFuzzy = false;
			}
			$poEntryStrValue = "";
		}
		
		$strStartPos = strpos($line, "\"");
		$strEndPos = strrpos($line, "\"");
		if (($poEntryKey || $poEntryValue) && $strStartPos !== false && $strEndPos !== false) {
			$currentText = substr($line, $strStartPos + 1, $strEndPos - $strStartPos - 1);
			if ($poEntryKey) {
				$poEntryStrKey .= str_replace("\\n", "\n", str_replace("\\\"", "\"", $currentText));
			}
			if ($poEntryValue) {
				$poEntryStrValue .= str_replace("\\n", "\n", str_replace("\\\"", "\"", $currentText));
			}
		}
	}
	
	if (strlen($poEntryStrKey) > 0 && strlen($poEntryStrValue) > 0 && ($includeFuzzy == true || $poEntryFuzzy == false)) {
		$poFileTranslatedTexts[$poEntryStrKey] = $poEntryStrValue;
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
	return $mappedKey;
}
?>