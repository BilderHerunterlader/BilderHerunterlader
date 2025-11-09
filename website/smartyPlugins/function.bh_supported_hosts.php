<?php
/**
 * Smarty {bh_supported_hosts} plugin
 * Type:     function
 * Name:     bh_supported_hosts
 * Purpose:  Load BH updates XML file and extract supported hosts
 *
 * @param array                    $params   parameters
 * @param Smarty_Internal_Template $template template object
 *
 * @throws SmartyException
 */
function smarty_function_bh_supported_hosts($params, $template)
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

	$supportedHostsArr = array();
	if ($xml != null) {
		$redirects = $xml->redirectUpdates[0];
		foreach ($redirects->redirect as $redirect) {
			$hostName = getSupportedHostName($redirect);
			if ($hostName != null) {
				$supportedHostsArr[] = $hostName;
			}
		}
		
		$hosters = $xml->hosterUpdates[0];
		foreach ($hosters->host as $host) {
			$hostName = getSupportedHostName($host);
			if ($hostName != null) {
				$supportedHostsArr[] = $hostName;
			}
		}
		
		$rules = $xml->ruleUpdates[0];
		foreach ($rules->rule as $rule) {
			$hostName = getSupportedHostName($rule);
			if ($hostName != null) {
				$supportedHostsArr[] = $hostName;
			}
		}
		
		usort($supportedHostsArr, "arraycmp");
	}
	
	$template->assign("BHSupportedHostsArray", $supportedHostsArr); 
}

function getSupportedHostName($redirectOrHostOrRule) {
	$deleleAttribPresent = findAttribute($redirectOrHostOrRule, 'delete') !== null;
	if ($deleleAttribPresent == true) {
		return null;
	}

	$hostName = findAttribute($redirectOrHostOrRule, 'name');
	$redirect = false;
	if (substr($hostName, 0, 4) === "Host" || substr($hostName, 0, 4) === "Rule") {
		$hostName = substr($hostName, 4);
	}
	if (substr($hostName, 0, 8) === "Redirect") {
		$hostName = substr($hostName, 8);
		$redirect = true;
	}
	if (substr($hostName, -4) === ".xml") {
		$hostName = substr($hostName, 0, strlen($hostName) - 4);
	}
	if ($redirect) {
		$hostName .= " (Redirect)";
	}
	return $hostName;
}

function arraycmp($a, $b) {
	return strcasecmp($a, $b);
}

function findAttribute($object, $attribute) {
	foreach($object->attributes() as $a => $b) {
		if ($a == $attribute) {
			return $b;
		}
	}
	return null;
}
?>