{bh_release_version file=$bhUpdatesXMLFile}
{assign var="ffversion" value="4.8"}
{assign var="ff57version" value="4.7"}
{assign var="smversion" value="4.8"}
{assign var="operaversion" value="6"}
{assign var="ieversion" value="9.0"}
<script type="text/javascript" src="installext.js"></script>
<div class="download"><a name="requirements"></a>
<pre>
<img src="bilderherunterlader/java.gif" alt="Java" />
<b>Voraussetzung:</b>
JRE (Java  Runtime Environment)
<a href="http://java.sun.com/javase/downloads/index.jsp">Download-Seite</a>

<b>Supported Operating Systems:</b>
Windows, Linux, MacOS, BSD und alle anderen Betriebssysteme wofür die JRE (Java Runtime Environment) zur Verfügung steht.
</pre>
</div>
<div class="download"><a name="bh"></a>
<pre>
<img src="bilderherunterlader/bilderherunterlader2-50.png" alt="Bilderherunterlader" />
<span style="color: #FF0000; font-weight: bold;">Bitte lies dir vor der Installation erst noch die <a href="?loc=bilderherunterlader/tutorial&amp;lng={$pageLanguage}">{gettext}tutorial{/gettext}</a> durch vor der Installation</span>
<b>Version:</b>
{$BHSetupReleaseVersion}:
<table summary="downloads" class="downloads">
<tr><th>Download</th></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/BilderHerunterlader{$BHSetupReleaseVersionFilenameNumber}Setup.exe/download">BilderHerunterlader Setup (für Windows)</a></td></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/BilderHerunterlader{$BHSetupReleaseVersionFilenameNumber}Setup.jar/download">BilderHerunterlader Setup (Plattformunabhängig)</a></td></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/BilderHerunterlader{$BHSetupReleaseVersionFilenameNumber}Binary.zip/download">BilderHerunterlader Binaries in Zip-Archiv (für alle anderen Betriebsysteme)</a></td></tr>
<tr><td><a href="https://github.com/BilderHerunterlader/BilderHerunterlader">BilderHerunterlader Source-Code auf GitHub</a></td></tr>
</table>
</pre>
</div>
<div class="download"><a name="palemoon"></a>
<pre>
<img src="bilderherunterlader/palemoon.png" alt="Pale Moon" /><img src="bilderherunterlader/firefox.png" alt="Firefox" />
<b>Pale Moon / Firefox (Bis Version 56) - Extension:</b>
<b><i>Version {$ffversion}:
Nur für Firefox 1.5 - 56.*
Nur für Pale Moon 25.0 - 29.*</i></b>
<a href="https://www.dropbox.com/s/dorgswgwzkwbfr6/bilderherunterlader-v{$ffversion}.xpi?dl=1">Pale Moon / Firefox (Bis Version 56) - Extension installieren</a>

Verwendung:
Auf einer Webseite mit Bildern: Rechtsklick -> Dateien mit BH herunterladen
Mit der Tastenkombination Shift + B wird die Funktion direkt aufgerufen.
</pre>
</div>
<div class="download"><a name="firefox"></a>
<pre>
<img src="bilderherunterlader/firefox57.png" alt="Firefox" />
<b>Firefox 57+ WebExtension:</b>
<b><i>Version {$ff57version}:
Nur für Firefox 57.0 oder höher
Benötigt BH 4.6.0 oder höher</i></b>
<a href="https://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/bilderherunterladerwebext-{$ff57version}-an%2Bfx.xpi/download">Firefox - WebExtension installieren</a>

Verwendung:
Auf einer Webseite mit Bildern: Rechtsklick -> BilderHerunterlader -> Dateien mit BH herunterladen
</pre>
</div>
<div class="download"><a name="seamonkey"></a>
<pre>
<img src="bilderherunterlader/seamonkey.png" alt="Seamonkey" />
<b>Seamonkey-Extension:</b>
<b><i>Version {$smversion}: Nur für Seamonkey 2.0 - *.*.* (Bedeuted, dass die Extension als kompatibel mit jeder neueren Firefox Version markiert ist, auch wenn die Extension mit der neuen Version nicht funktioniert)</i></b>
<a href="https://www.dropbox.com/s/dorgswgwzkwbfr6/bilderherunterlader-v{$smversion}.xpi?dl=1">Seamonkey-Erweiterung installieren</a>

Verwendung:
Auf einer Webseite mit Bildern: Rechtsklick -> Dateien mit BH herunterladen
Mit der Tastenkombination Shift + B wird die Funktion direkt aufgerufen.
</pre>
</div>
<div class="download"><a name="opera"></a>
<pre>
<img src="bilderherunterlader/opera.gif" alt="Opera" />
<b>Plugin für Opera:</b>
<table summary="downloads" class="downloads">
<tr><th>Download</th><th>Betriebssystem</th><th>Opera Version</th></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/OperaPluginWindowsV{$operaversion}.zip/download">OperaPluginWindowsV{$operaversion}.zip</a></td><td>Windows</td><td>9.5 oder höher</td></tr>
<tr><td><a href="http://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/OperaPluginLinuxV{$operaversion}.zip/download">OperaPluginLinuxV{$operaversion}.zip</a></td><td>Linux (möglicherweise auch BSD und MacOS)</td><td>9.5 oder höher</td></tr>
</table>
Verwendung:
Lies die readme.txt in der zip-datei.
</pre>
</div>
<div class="download"><a name="ie"></a>
<pre>
<img src="bilderherunterlader/ie.gif" alt="Internet Explorer" />
<b>Plugin für Internet Explorer:</b>
<b><i>Version {$ieversion}:</i></b>
<a href="http://sourceforge.net/projects/bilderherunterlader/files/Browser%20Plugins/BHIEPluginSetupv{$ieversion}.exe/download">BHIEPluginSetupv{$ieversion}.exe</a>

Verwendung:
Auf einer Webseite mit Bildern: Rechtsklick -> Dateien mit BH herunterladen
</pre>
</div>
<div class="download"><a name="other"></a>
<pre>
<b>Andere Browser:</b>
Für andere Browser gibt es im Moment keine Extensions. Aber man kann das Programm trotzdem verwenden.
Es gibt 2 Wege:
1:
Schalte die Zwischenablagenüberwachung ein
Kopiert die URL der Webseite in die Zwischenablage (Ctrl + C)
2:
Markier die Bilder im Browser und ziehe die Markierung ins Programm.
</pre>
</div>