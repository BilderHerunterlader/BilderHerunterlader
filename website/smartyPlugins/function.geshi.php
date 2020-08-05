<?php
/**
 * Smarty {geshi} plugin
 * Type:     function
 * Name:     geshi
 * Purpose:  Geshi
 *
 * @param array                    $params   parameters
 * @param Smarty_Internal_Template $template template object
 *
 * @throws SmartyException
 */
function smarty_function_geshi($params, $template)
{
    if (empty($params['source'])) {
        trigger_error('[plugin] fetch parameter \'source\' cannot be empty', E_USER_NOTICE);
        return;
    }
	if (empty($params['language'])) {
        trigger_error('[plugin] fetch parameter \'language\' cannot be empty', E_USER_NOTICE);
        return;
    }
	if (empty($params['geshiInstance'])) {
        trigger_error('[plugin] fetch parameter \'geshiInstance\' cannot be empty', E_USER_NOTICE);
        return;
    }

	$geshiInstance = &$params['geshiInstance'];
	$geshiInstance->set_source($params['source']);
	$geshiInstance->set_language($params['language']);
	return $geshiInstance->parse_code();
}
?>