<ul>
{nocache}
{bh_supported_hosts file=$bhUpdatesXMLFile}
{foreach $BHSupportedHostsArray as $host}
   <li>{$host}</li>
{/foreach}
{/nocache}
</ul>
