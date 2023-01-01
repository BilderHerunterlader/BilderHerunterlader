{* Smarty *}
<!DOCTYPE html>
<html>
	<head>
		<title>{foreach $pageTitleArrayTranslated as $pageTitlePart name=titleLoop}{$pageTitlePart}{if not $smarty.foreach.titleLoop.last} - {/if}{/foreach}</title>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<meta name="title" content="{foreach $pageTitleArrayTranslated as $pageTitlePart name=titleLoop}{$pageTitlePart}{if not $smarty.foreach.titleLoop.last} - {/if}{/foreach}" />
		<meta name="keywords" content="BilderHerunterlader, Irada, Image Downloader, Imagehost Batch Downloader, BH, BH Downloader" />
		<link rel="shortcut icon" href="/favicon.ico" />
		<link rel="stylesheet" type="text/css" href="style_main.css" />
{if isset($pageLocationSubStyle) && !empty($pageLocationSubStyle)}
		<link rel="stylesheet" type="text/css" href="{$pageLocationSubStyle}" />
{else}
		<link rel="stylesheet" type="text/css" href="style_default.css" />
{/if}
		<link rel="stylesheet" type="text/css" href="style_print.css" media="print" />
	</head>
	<body>
		<div id="menu">
			<ul id="menumainindex" class="menumain">
				<li><a href="?loc=home&amp;lng={$pageLanguage}">{gettext}home{/gettext}</a></li>
				<li><a href="?loc=bilderherunterlader&amp;lng={$pageLanguage}">{gettext}bilderherunterlader{/gettext}</a></li>
				<li><a href="?loc=irada&amp;lng={$pageLanguage}">{gettext}irada{/gettext}</a></li>
				<li><a href="?loc=contact&amp;lng={$pageLanguage}">{gettext}contact{/gettext}</a></li>
			</ul>
			<ul id="menumainkofi" class="menumain">
				<li><script type='text/javascript' src='https://storage.ko-fi.com/cdn/widget/Widget_2.js'></script><script type='text/javascript'>kofiwidget2.init('Support Me on Ko-fi', '#29abe0', 'V7V5HEVRH');kofiwidget2.draw();</script></li>
			</ul>
			<ul id="menumainlanguage" class="menumain">
				<li><a href="?loc={$pageLocation}&amp;lng=de">DE</a></li>
				<li><a href="?loc={$pageLocation}&amp;lng=en">EN</a></li>
			</ul>
		</div>
		<p class="title">{foreach $pageTitleArrayTranslated as $pageTitlePart name=titleLoop}{$pageTitlePart}{if not $smarty.foreach.titleLoop.last} / {/if}{/foreach}</p>
{if isset($pageSubMenuTemplate) && !empty($pageSubMenuTemplate)}
{include file=$pageSubMenuTemplate}
		<div id="content" class="content">
{else}
		<div id="content">
{/if}
{if isset($pageLocationTemplate) && !empty($pageLocationTemplate)}
{include file=$pageLocationTemplate}
{/if}
		</div>
		<div id="footer">
		&nbsp;
		</div>
	</body>
</html>
