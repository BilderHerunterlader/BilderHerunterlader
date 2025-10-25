<?php
/**
 * Smarty plugin to translate text
 *
 * @package    Smarty
 * @subpackage PluginsBlock
 */
/**
 * Smarty {gettext}{/gettext} block plugin
 * Type:     block function
 * Name:     gettext
 * Purpose:  Translate text
 *
 * @param array                    $params   parameters
 * @param string                   $content  contents of the block
 * @param Smarty_Internal_Template $template template object
 * @param boolean                  &$repeat  repeat flag
 *
 * @return string content re-formatted
 * @author Monte Ohrt <monte at ohrt dot com>
 * @throws \SmartyException
 */
function smarty_block_t($params, $content, Smarty_Internal_Template $template, &$repeat)
{
    if (is_null($content)) {
        return;
    }

	$translatedTexts = $template->getTemplateVars("translatedTexts");
	if (array_key_exists($content, $translatedTexts)) {
		return $translatedTexts[$content];
	}
    return $content;
}
