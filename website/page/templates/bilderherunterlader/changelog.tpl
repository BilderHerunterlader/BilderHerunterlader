{nocache}
{bh_changelog file=$bhUpdatesXMLFile}
{foreach $BHChangelogArray as $change}
	<div class="download"><pre>{$change}</pre></div>
{/foreach}
{/nocache}