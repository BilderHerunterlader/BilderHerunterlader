{bh_release_version file=$bhUpdatesXMLFile}
{assign var="palemoonversion" value="4.8"}
{assign var="palemoonURL" value="https://www.dropbox.com/s/dorgswgwzkwbfr6/bilderherunterlader-v{$palemoonversion}.xpi?dl=1"}
{assign var="firefoxversion" value="4.9"}
{assign var="firefoxURL" value="https://www.dropbox.com/scl/fi/jr8inbj2eynwrmd4bn827/bilderherunterladerwebext-4.9-fx.xpi?rlkey=9ak8q374chy95kxzd69qf7cn0&dl=1"}
{assign var="seamonkeyversion" value="4.8"}
{assign var="seamonkeyURL" value="https://www.dropbox.com/s/dorgswgwzkwbfr6/bilderherunterlader-v{$seamonkeyversion}.xpi?dl=1"}
{assign var="chromeversion" value="5.0"}
{assign var="chromeURL" value="https://chromewebstore.google.com/detail/bilderherunterlader/dhakkekdifhcedejafeameclokbkcdkp"}
{assign var="operaversion" value="5.0"}
{assign var="operaURL" value="TODO"}
{assign var="edgeversion" value="5.0"}
{assign var="edgeURL" value="TODO"}
<script type="text/javascript" src="bilderherunterlader/installext.js"></script>
<div class="download"><a name="requirements"></a>
<b>{t}Requirements:{/t}</b>
<br/>
JRE (Java  Runtime Environment) Version 21 {t}or higher{/t}
<br/>
<a href="https://adoptium.net/temurin/releases/">{t}Download-Page{/t}</a>
</div>
<div class="download"><a name="bh"></a>
<img src="bilderherunterlader/bilderherunterlader2-50.png" alt="Bilderherunterlader" />
<br/>
<span style="color: #FF0000; font-weight: bold;">{t}Please read the{/t} <a href="?loc=bilderherunterlader/tutorial&amp;lng={$pageLanguage}">{t}tutorial{/t}</a> {t}before installing{/t}</span>
<br/>
<b>Version:</b> {$BHSetupReleaseVersion}
<table summary="downloads" class="downloads">
<tr><th>{t}Downloads{/t}</th></tr>
<tr><td><a href="https://github.com/BilderHerunterlader/BilderHerunterlader/releases/download/bilderherunterlader-{$BHSetupReleaseVersion}/BilderHerunterlader-{$BHSetupReleaseVersion}-Setup.exe">{t}BilderHerunterlader Setup (for Windows){/t}</a></td></tr>
<tr><td><a href="https://github.com/BilderHerunterlader/BilderHerunterlader/releases/download/bilderherunterlader-{$BHSetupReleaseVersion}/BilderHerunterlader-{$BHSetupReleaseVersion}-Setup.jar">{t}BilderHerunterlader Setup (platform independent){/t}</a></td></tr>
<tr><td><a href="https://github.com/BilderHerunterlader/BilderHerunterlader/releases/download/bilderherunterlader-{$BHSetupReleaseVersion}/BilderHerunterlader-{$BHSetupReleaseVersion}-Binary.zip">{t}BilderHerunterlader Binaries in Zip-Archiv (for other operating systems){/t}</a></td></tr>
<tr><td><a href="https://github.com/BilderHerunterlader/BilderHerunterlader">BilderHerunterlader Source-Code on GitHub</a></td></tr>
</table>
</div>
<div class="download"><a name="palemoon"></a>
<img src="bilderherunterlader/palemoon.png" alt="Pale Moon" />
<br/>
<b>Pale Moon - Extension:</b>
<br/>
<b>Version:</b> {$palemoonversion}
<br/>
<a href="{$palemoonURL}" onclick="return bhInstExt(&quot;{$palemoonURL}&quot;, &quot;BilderHerunterlader&quot;)">Install Pale Moon - Extension</a>
<br/>
<a href="{$palemoonURL}">Download Pale Moon - Extension</a>
<br/><br/>
{t}Usage:{/t}
<br/>
{t}On a page with images: Rightclick -> Download files with BH{/t}
<br/>
{t}The function can also be called directly by using the keyboard-shortcut Shift + B.{/t}
</div>
<div class="download"><a name="firefox"></a>
<img src="bilderherunterlader/firefox.svg" alt="Firefox" width="50" height="50" />
<br/>
<b>Firefox WebExtension:</b>
<br/>
<b>Version:</b> {$firefoxversion}
<br/>
<a href="{$firefoxURL}">Download Firefox - WebExtension</a>
<br/><br/>
{t}Usage:{/t}
<br/>
{t}On a page with images: Rightclick -> BilderHerunterlader -> Download files with BH{/t}
</div>
<div class="download"><a name="chrome"></a>
<img src="bilderherunterlader/chrome.svg" alt="Chrome" width="50" height="50" /><img src="bilderherunterlader/vivaldi.svg" alt="Vivaldi" width="50" height="50" />
<br/>
<b>Chrome / Vivaldi WebExtension:</b>
<br/>
<b>Version:</b> {$chromeversion}
<br/>
<a href="{$chromeURL}">Install Chrome / Vivaldi - WebExtension</a>
<br/><br/>
{t}Usage:{/t}
<br/>
{t}On a page with images: Rightclick -> BilderHerunterlader -> Download files with BH{/t}
</div>
<div class="download"><a name="opera"></a>
<img src="bilderherunterlader/opera.svg" alt="Opera" width="50" height="50" />
<br/>
<b>Opera WebExtension:</b>
<br/>
<b>Version:</b> {$operaversion}
<br/>
Not yet available
<!-- <a href="{$operaURL}">Install Opera - WebExtension</a> -->
<br/><br/>
{t}Usage:{/t}
<br/>
{t}On a page with images: Rightclick -> BilderHerunterlader -> Download files with BH{/t}
</div>
<div class="download"><a name="edge"></a>
<img src="bilderherunterlader/edge.svg" alt="Edge" width="50" height="50" />
<br/>
<b>Edge WebExtension:</b>
<br/>
<b>Version:</b> {$edgeversion}
<br/>
Not yet available
<!-- <a href="{$edgeURL}">Install Edge - WebExtension</a> -->
<br/><br/>
{t}Usage:{/t}
<br/>
{t}On a page with images: Rightclick -> BilderHerunterlader -> Download files with BH{/t}
</div>
<div class="download"><a name="seamonkey"></a>
<img src="bilderherunterlader/seamonkey.svg" alt="Seamonkey" width="50" height="50" />
<br/>
<b>Seamonkey-Extension:</b>
<br/>
<b>Version:</b> {$seamonkeyversion}
<br/>
<a href="{$seamonkeyURL}" onclick="return bhInstExt(&quot;{$seamonkeyURL}&quot;, &quot;BilderHerunterlader&quot;)">Install Seamonkey-Extension</a>
<br/>
<a href="{$seamonkeyURL}">Download Seamonkey-Extension</a>
<br/><br/>
{t}Usage:{/t}
<br/>
{t}On a page with images: Rightclick -> Download files with BH{/t}
<br/>
{t}The function can also be called directly by using the keyboard-shortcut Shift + B.{/t}
</div>
<div class="download"><a name="other"></a>
<b>{t}Other Browsers:{/t}</b>
<br/>
{t}There are no extensions available for other browser at the moment. But you can use the program too.{/t}
<br/>
{t}There are 2 ways:{/t}
<br/>
1: {t}Turn on Clipboard-Monitoring and copy the URL of the webpage into the clipboard{/t} (Ctrl + C)
<br/>
2: {t}Select the images in the Browser and drag and drop the selection to the program{/t}
</div>