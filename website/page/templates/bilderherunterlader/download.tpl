{bh_release_version file=$bhUpdatesXMLFile}
{assign var="ffversion" value="4.5"}
{assign var="ff57version" value="4.7"}
{assign var="smversion" value="4.5"}
{assign var="operaversion" value="6"}
{assign var="ieversion" value="9.0"}
<script type="text/javascript" src="installext.js"></script>
<div class="download"><a name="requirements"></a>
<pre>
<img src="bilderherunterlader/java.gif" alt="Java" />
<b>Requirement:</b>
JRE (Java  Runtime Environment)
<a href="http://java.sun.com/javase/downloads/index.jsp">Download-Page</a>

<b>Supported Operating Systems:</b>
Windows, Linux, MacOS, BSD und any other operating system for which the JRE (Java Runtime Environment) is available.
</pre>
</div>
<div class="download"><a name="bh"></a>
<pre>
<img src="bilderherunterlader/bilderherunterlader2-50.png" alt="Bilderherunterlader" />
<span style="color: #FF0000; font-weight: bold;">Please read the <a href="?loc=bilderherunterlader/tutorial&amp;lng={$pageLanguage}">{gettext}tutorial{/gettext}</a> before installing</span>
<b>Version:</b>
{$BHSetupReleaseVersion}:
<table summary="downloads" class="downloads">
<tr><th>Download</th></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/BilderHerunterlader{$BHSetupReleaseVersionFilenameNumber}Setup.exe/download">BilderHerunterlader Setup (for Windows)</a></td></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/BilderHerunterlader{$BHSetupReleaseVersionFilenameNumber}Setup.jar/download">BilderHerunterlader Setup (platform independent)</a></td></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/BilderHerunterlader{$BHSetupReleaseVersionFilenameNumber}Binary.zip/download">BilderHerunterlader Binaries in Zip-Archiv (for other operating systems)</a></td></tr>
<tr><td><a href="https://github.com/BilderHerunterlader/BilderHerunterlader">BilderHerunterlader Source-Code on GitHub</a></td></tr>
</table>
</pre>
</div>
<div class="download"><a name="palemoon"></a>
<pre>
<img src="bilderherunterlader/palemoon.png" alt="Pale Moon" /><img src="bilderherunterlader/firefox.png" alt="Firefox" />
<b>Pale Moon / Firefox (until Version 56) - Extension:</b>
<b><i>Version {$ffversion}:
Only for Firefox 1.5 - 56.*
Only for Pale Moon 25.0 - 27.*</i></b>
<a href="https://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/bilderherunterlader-{$ffversion}-fx%2Bsm.xpi/download">Install Pale Moon / Firefox (until Version 56) - Extension</a>

Usage:
On a page with images: Rightclick -> Download files with BH
The function can also be called directly by using the keyboard-shortcut Shift + B.
</pre>
</div>
<div class="download"><a name="firefox"></a>
<pre>
<img src="bilderherunterlader/firefox57.png" alt="Firefox" />
<b>Firefox 57+ WebExtension:</b>
<b><i>Version {$ff57version}:
Only for Firefox 57.0 or higher
Requires BH 4.6.0 or higher</i></b>
<a href="https://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/bilderherunterladerwebext-{$ff57version}-an%2Bfx.xpi/download">Install Firefox - WebExtension</a>

Usage:
On a page with images: Rightclick -> BilderHerunterlader -> Download files with BH
</pre>
</div>
<div class="download"><a name="seamonkey"></a>
<pre>
<img src="bilderherunterlader/seamonkey.png" alt="Seamonkey" />
<b>Seamonkey-Extension:</b>
<b><i>Version {$smversion}: Only for Seamonkey 2.0 - *.*.* (Means the extension is marked as compatible with every future Seamonkey version, even if the extension is not actually working with that version)</i></b>
<!-- <a href="http://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/bilderherunterlader-v{$smversion}.xpi/download" onclick="return install('http://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/bilderherunterlader-v{$smversion}.xpi/download', '');">Install Seamonkey-Extension</a> -->
<a href="https://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/bilderherunterlader-{$smversion}-fx%2Bsm.xpi/download">Install Seamonkey-Extension</a>

Usage:
On a page with images: Rightclick -> Download files with BH
The function can also be called directly by using the keyboard-shortcut Shift + B.
</pre>
</div>
<div class="download"><a name="opera"></a>
<pre>
<img src="bilderherunterlader/opera.gif" alt="Opera" />
<b>Plugin for Opera:</b>
<table summary="downloads" class="downloads">
<tr><th>Download</th><th>Operating System</th><th>Opera Version</th></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/OperaPluginWindowsV{$operaversion}.zip/download">OperaPluginWindowsV{$operaversion}.zip</a></td><td>Windows</td><td>9.5 or Higher</td></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/OperaPluginLinuxV{$operaversion}.zip/download">OperaPluginLinuxV{$operaversion}.zip</a></td><td>Linux (maybe also BSD and MacOS)</td><td>9.5 or Higher</td></tr>
</table>
Usage:
Read the readme.txt in the zip-file.
</pre>
</div>
<div class="download"><a name="ie"></a>
<pre>
<img src="bilderherunterlader/ie.gif" alt="Internet Explorer" />
<b>Plugin for Internet Explorer:</b>
<b><i>Version {$ieversion}:</i></b>
<a href="http://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/BHIEPluginSetupv{$ieversion}.exe/download">BHIEPluginSetupv{$ieversion}.exe</a>

Usage:
On a page with images: Rightclick -> Download files with BH
</pre>
</div>
<div class="download"><a name="other"></a>
<pre>
<b>Other Browsers:</b>
There are no extensions available for other browser at the moment. But you can use the program too.
There are 2 ways:
1:
Turn on Clipboard-Monitoring
Copy the URL of the webpage into the clipboard (Ctrl + C)
2:
Select the images in the Browser and drag and drop the selection to the program
</pre>
</div>