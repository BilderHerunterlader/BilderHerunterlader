<?php
/**
 * Smarty {bh_release_version} plugin
 * Type:     function
 * Name:     bh_release_version
 * Purpose:  Load BH updates XML file and extract supported hosts
 *
 * @param array                    $params   parameters
 * @param Smarty_Internal_Template $template template object
 *
 * @throws SmartyException
 */
function smarty_function_bh_release_version($params, $template)
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

	$setupReleaseVersion = "";
	$mainReleaseVersion = "";
	if ($xml != null) {
		$setupReleaseVersion = $xml->setuprelease[0]["version"];
		$mainReleaseVersion = $xml->main[0]["version"];
	}
	
	$template->assign("BHSetupReleaseVersion", $setupReleaseVersion);
	$template->assign("BHSetupReleaseVersionFilenameNumber", str_replace(".", "", $setupReleaseVersion));
	$template->assign("BHMainReleaseVersion", $mainReleaseVersion);
	$template->assign("BHMainReleaseVersionFilenameNumber", str_replace(".", "", $mainReleaseVersion));
}
?>