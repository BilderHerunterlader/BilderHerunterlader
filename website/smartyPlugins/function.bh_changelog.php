<?php
/**
 * Smarty {bh_changelog} plugin
 * Type:     function
 * Name:     bh_changelog
 * Purpose:  Load BH updates XML file and extract changelog
 *
 * @param array                    $params   parameters
 * @param Smarty_Internal_Template $template template object
 *
 * @throws SmartyException
 */
function smarty_function_bh_changelog($params, $template)
{
    if (empty($params['file'])) {
        trigger_error('[plugin] fetch parameter \'file\' cannot be empty', E_USER_NOTICE);
        return;
    }	

	$xmlFile = $params['file'];
	$xml = null;
	if (file_exists($xmlFile)) {
		$xml = simplexml_load_file($xmlFile);
	}

	$changelogArr = array();
	if ($xml != null) {
		$changelog = $xml->changelog[0];
		foreach ($changelog->changes as $change) {
			$version = $change["version"] . ":\n";
			$desc = str_replace("\\n", "\n", $change) . "\n";
			$changelogArr[] = $version . $desc;
		}
		
	}
	
	$template->assign("BHChangelogArray", $changelogArr); 
}
?>