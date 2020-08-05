<h1>Usage</h1>
<div class="features">
<ul>
	<li>Download Java and install it, if not already done</li>
	<li>Download BilderHerunterlader and install it</li>
	<li>Download and install the extension or plugin for your browser, if available.</li>
	<li>In order to make the program internal updates working, you need to give your user-account full access rights on the program folder.<br />
	This is required under Windows Vista, Windows 7, Linux, MacOS and Windows 2000 / XP (if you are working with limited rights)</li>
	<li class="noListStyle">
		<ul>
			<li>Windows Vista / 7 / 8 / 10: Rightclick on the BilderHerunterlader-Program-Folder -> Properties -> Security -> Click on Edit -> Choose the user -> Allow Full control -> Click on OK until all Windows are closed.</li>
			<li>Windows 2000 / XP: First you must deactivate the simple file sharing in the Folder Options. Rightclick on the BilderHerunterlader-Program-Folder -> Properties -> Securtiy -> Choose the user -> Allow Full control -> Click on OK</li>
			<li>Linux: You can do it over the user-interface or by command line. There are many websites for every linux distribution, which will tell you how exactly you can do that.</li>
			<li>MacOS: I don't have a Mac and don't know the system at all, so i can't tell how to do it under MacOS.</li>
		</ul>
	</li>
	<li>Start BilderHerunterlader</li>
	<li class="noListStyle">
		<ul>
			<li>BilderHerunterlader does not have an .exe-File. There is a BH.jar, that file is like an .exe-File. In normal case you can just double-click the file or the entry in the start-menu and the program should start.</li>
			<li>There are programs like e.g. Winrar, which might register the .jar-Extension for itself. If so, the .jar-File would be opened by that program instead of starting BilderHerunterlader.<br />If this is the case you need to register the extension for Java. Under Windows XP you go to Tools - Folder Options - File Types in the Explorer. Scroll down to the JAR entry. Select that entry and click on Advanced. Select the "open" Action and click on "Edit". For "Application used to perform action" you need to choose the path to Java. Click on Browse and go to the Java-Program-Folder which can normally be found under C:\Program files\Java\jre6\bin. This could differ by the Java-Version and operating system. If you still have Java Version 5, you will find Java under C:\Program files\Java\jre1.5.0_xx\bin (xx stands for any number). Now you choose the javaw.exe in that folder. Now in the text-field, if there is none, add a " at the beginning and at the end. Now add -jar "%1" %* at the end.<br />The entry should now look like that: "C:\Program files\Java\jre6\bin\javaw.exe" -jar "%1" %*<br />Now close all windows by clicking on OK. Now you should be able the start BilderHerunterlader.<br /><br />For almost all Windows Versions including XP, Vista and 7 you can also use a programm called jarfix, which should fix the problem automatically for you: <a href="http://johann.loefflmann.net/en/software/jarfix/index.html">http://johann.loefflmann.net/en/software/jarfix/index.html</a></li>
		</ul>
	</li>
	<li>On the first start the default settings are used and you should change those to your needs.<br />
	The most important options are:</li>
	<li class="noListStyle">
		<ul>
			<li>Connection -> Direct Connection / Proxy: If you use a proxy, you must define that, otherwise the program is not working.</li>
			<li>Connection -> Cookies: Here you must choose your browser, if available, because the download from some websites will not work without cookies.</li>
			<li>Connection -> User-Agent: Here you should type in the User-Agent of your browser, because otherwise links could not be read out of some websites by using the Opera-Plugin or the Copy-to-Clipboard method.<br />
			Please note: The User-Agent of your browser is often changed when the browser is updated or new plugins/extension are installed. So you would have to change it also in BH.<br />
			To find out the User-Agent of your browser you can surf to <a href="http://whatsmyuseragent.com/">http://whatsmyuseragent.com/</a></li>
			<li>Folders -> Standard Savepath: Here you must choose a folder, in which you want to download the files.</li>
			<li>Other -> Updates: Many hosts change their websites from time to time, so that the download with BilderHerunterlader is not working anymore. To fix that, there is a program internal Update-Function which updates BilderHerunterlader and the Rules and Host-Plugins. If the option is activated, the program checks on every start if there are upates. If you don't want that, you can still check for updates manually, in order to do that go to Info -> Update.</li>
		</ul>
	</li>
	<li>Open a website which contains links to Images / Files in your browser</li>
	<li class="noListStyle">
		<ul>
			<li>In Firefox: Rightclick -> Download files with BH</li>
			<li>In Opera: Rightclick -> Download files with BH</li>
			<li>In the Internet Explorer: Rightclick -> Download files with BH</li>
			<li>Alternative: Copy the URL of the website into the clipboard (ctrl + c). In order make it working that way, you need to enable the "Clipboard - Check for particular links" option in the settings.<br/>
			If nothing happens, click on "Check Clipboard now" on the System-Tray-Icon-Menu or in the menubar.</li>
			<li>Another alternative: Select the images / links in the browser and drag and drop the selection with the mouse to the program-window.</li>
		</ul>
	</li>
	<li>Now a new window opens, which shows the available files</li>
	<li>Here you can change the download-directory or let it changed automatically by keywords. You can also select which files are downloaded and which not. And you can define some other things.<br />
	For a detailed description of the possibilities you should read the feature-page: <a href="?loc=bilderherunterlader/features&amp;lng=<?=$lng?>"><?=$lang["features"][$lng]?></a></li>
	<li>Now click on OK and the files are added to the download-queue</li>
	<li>Now click on Start on the download-queue and the files are downloading</li>
</ul>
</div>
<br />
<br />
<h1><a name="settings" class="noStyle">Settings</a></h1>
<p>
To use the new settings you must click on "Save" or "Apply"!<br />
Some settings are only changed after restarting the program.
</p>
<div class="features">
<h2>Interface</h2>
<ul>
	<li>Appearance</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies the appearance of the program.</li>
			<li>This settings is not working on all operating systems.</li>
		</ul>
	</li>
	<li>Language</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies the language of the program.</li>
			<li>Only changed after restart</li>
		</ul>
	</li>
	<li>Size</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies the display of the size on downloads.</li>
			<li>"Automatic adjustment" chooses automatically an appropriate unit</li>
		</ul>
	</li>
	<li>Progress</li>
	<li class="noListStyle">
		<ul>
			<li>Defines the display of the progress on downloads.</li>
		</ul>
	</li>
	<li>Fortschrittanzeige - Show download-speed</li>
	<li class="noListStyle">
		<ul>
			<li>Defines if the download-speed is displayed in the progress display</li>
		</ul>
	</li>
	<li>Window - Save size and position</li>
	<li class="noListStyle">
		<ul>
			<li>Defines if the size and position of the window will be saved.</li>
		</ul>
	</li>
	<li>Tables - Save column-sizes</li>
	<li class="noListStyle">
		<ul>
			<li>Defines if column sizes of tables are saved and loaded on next start</li>
		</ul>
	</li>
	<li>Tables - Save table sorting</li>
	<li class="noListStyle">
		<ul>
			<li>Defines if sort of tables are saved and loaded on next start</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Connection</h2>
<ul>
	<li>Direct Connection / HTTP-Proxy</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies the way how the program connects to the internet. Under normal conditions no change should be necessary here.</li>
			<li>Only when you are connected to the internet over a proxyserver, you must change these settings.</li>
		</ul>
	</li>
	<li>Number of connections</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies how many images are simultaneously downloaded</li>
			<li>Be careful with this setting. Webservers permit only a certain number of connections.</li>
			<li>When you specifie this setting to high then downloads of some images will fail. But you can start the download of failed downloads again.</li>
		</ul>
	</li>
	<li>Cookies</li>
	<li class="noListStyle">
		<ul>
			<li>Defines if and from which browser cookies are used</li>
			<li>At the moment it is only possible to use cookies from PaleMoon, Firefox, Opera and Internet Explorer</li>
			<li>Cookies are sometimes required by websites, otherwise the download will not work</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Folders</h2>
<ul>
	<li>Standard-Savepath</li>
	<li class="noListStyle">
		<ul>
			<li>Folder on the harddisk, where files are stored.</li>
			<li>The folder can still be changed in the download-selection or queue</li>
		</ul>
	</li>
	<li>Standard-Savepath - Remember last used directory</li>
	<li class="noListStyle">
		<ul>
			<li>The standard savepath is always the last used directory in the Download-Selection</li>
		</ul>
	</li>
	<li>Sub Directories</li>
	<li class="noListStyle">
		<ul>
			<li>This option can be used to sort files based on their size into sub folders after the download. If "Subdirectories enabled" is checked, then this will be done.</li>
			<li>For "Subdirectory" the name of the sub directory is defined and the minimum and maximum filesize is defined. After the download files will be automatically moved to the sub directory if the filesize is in the defined range.</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Keywords</h2>
<ul>
	<li>Keyword-Search</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies how the program is searching for keywords.</li>
			<li>This setting takes only affect when using the search by title.</li>
			<li>"Only exact matches"</li>
			<li class="noListStyle">
				<ul>
					<li>Only exact matches are accepted.<br />
Example:<br />
Keywords: Nora Tschirner; Nori Tschirner<br />
The program searches for "Nora Tschirner" and "Nori Tschirner".</li>
				</ul>
			</li>
			<li>"All matches (Strict)"</li>
			<li class="noListStyle">
				<ul>
					<li>Here also the individual words are used for the search.<br />
But only machtes are accepted, if before or after the word no letter is present.<br />
Example:<br />
Keywords: Nora Tschirner; Nori Tschirner<br />
The program searches for "Nora Tschirner", "Nori Tschirner", "Nora", "Tschirner" und "Nori".<br />
Before and after the words "Nora", "Tschirner"und "Nori" no letter may present.</li>
				</ul>
			</li>
			<li>"All matches"</li>
			<li class="noListStyle">
				<ul>
					<li>All matches are accepted.</li>
				</ul>
			</li>
		</ul>
	</li>
	<li>Keyword-Search - Don't select link on negative target recognition</li>
	<li class="noListStyle">
		<ul>
			<li>If in the Downlaod-Selection for a link no keyword was found then the link will not selected for download.</li>
			<li>This setting takes only affect when using the search by filename.</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Download</h2>
<ul>
	<li>Logs - Save Logs</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies if images which were downloaded are saved in a list. That list is used to check if in the download-selection if you already downloaded an image.</li>
		</ul>
	</li>
	<li>Downloads - Start downloads automatically</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies if the download is automatically started when images are added to the download queue.</li>
		</ul>
	</li>
	<li>Filename</li>
	<li class="noListStyle">
		<ul>
			<li>Defines which characters are allowed in filenames</li>
		</ul>
	</li>
	<li>Downloads - Deactivate after x failed attempts</li>
	<li class="noListStyle">
		<ul>
			<li>Downloads will be automatically deactivated after the defined number of failed attempts has been reached</li>
		</ul>
	</li>
	<li>Minimum Filesize (Bytes)</li>
	<li class="noListStyle">
		<ul>
			<li>Defines the minimum filesize. If a file was downloaded, which has a filesize lower than the defined minimum size, then the download will be treatet as a failure.</li>
			<li>If the value 0 is chosen, then this option is disabled</li>
		</ul>
	</li>
	<li>Timeout (ms)</li>
	<li class="noListStyle">
		<ul>
			<li>Defines the timespan in which BH tries to connect. If the timespan is reached, then the download will be treated as a failure</li>
			<li>This option should not be defined too high, but also not too low. The default setting should be used, unless a problem occurs.</li>
		</ul>
	</li>
	<li>Sort downloads on programstart</li>
	<li class="noListStyle">
		<ul>
			<li>Defines if and how downloads are sorted on start of BH</li>
		</ul>
	</li>
	<li>Notification</li>
	<li class="noListStyle">
		<ul>
			<li>If this option is enabled, then a notification is displayed when all downloads are complete</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Other</h2>
<ul>
	<li>Updates - Check for updates at start</li>
	<li class="noListStyle">
		<ul>
			<li>Specifies if the program is checking for new versions of the hosts when starting</li>
		</ul>
	</li>
	<li>Zwischenablage - Auf einzelne Links prüfen</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob das Programm die Zwischenablage auf Links prüft. Diese Einstellung muss aktiviert werden wenn man einen anderen Browser als Firefox, Opera oder Internet Explorer verwendet, da nur für diese Browser eine Extensions / Plugins zur Verfügung steht. Um diese Funktion zu nutzen, öffnet man eine Seite im Browser, die Links zu Dateien enthält. Nun kopiert man die URL (Adresse) der Seite in die Zwischenablage. Das Programm lädt nun diese Seite herunter und überprüft ob Links zu Dateien auf der Seite vorhanden sind und bietet diese dann zum Download an.</li>
		</ul>
	</li>
	<li>Target-Folder - Always add title to target folder</li>
	<li class="noListStyle">
		<ul>
			<li>Adds the title automatically to the target folder</li>
		</ul>
	</li>
	<li>Rules - Rules have higher priority than hoster-classes</li>
	<li class="noListStyle">
		<ul>
			<li>If this option is activated then the rules are scanned before the hoster-classes. (On adding links or when finding the direct URL to the image on download)</li>
		</ul>
	</li>
	<li>Backup - Sicherheitskopie der Datenbanken beim Start erstellen</li>
	<li class="noListStyle">
		<ul>
			<li>Ist diese Option aktiviert so wird beim Start vom BilderHerunterlader Sicherheitskopien der Datenbanken (Warteschlange, Schlüsselwörter, Logs) erstellt. Bei jedem Start wird die alte Sicherheitskopie überschrieben!</li>
		</ul>
	</li>
	<li>Defragmentieren - Datenbanken beim Start defragmentieren</li>
	<li class="noListStyle">
		<ul>
			<li>Treten durch diese Einstellung keine Probleme sollte diese nicht deaktiviert werden</li>
			<li>Die Einstellung bewirkt, dass die Datenbank-Dateien aufgeräumt werden und auf der Festplatte weniger Platz benötigen</li>
		</ul>
	</li>
	<li>Minimale Dateigrösse für Defragmentation (Bytes)</li>
	<li class="noListStyle">
		<ul>
			<li>Da das Defragmentieren einige Zeit in Anspruch nimmt, sollte man eine Minimale Dateigrösse festlegen, aber der defragmentiert wird, damit dies nicht jedesmal gemacht werden muss und vor allem nur dann, wenn es wirklich etwas aufzuräumen gibt. Hier sollte die Standard-Einstellung belassen werden.</li>
		</ul>
	</li>
	<li>Debug-Level</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest was für Fehler, Warnungen oder Informationen der BilderHerunterlader in die Log-Datei (BH.log) schreibt</li>
			<li>Die Standard-Einstellung (Warn) sollte belassen werden, damit werden Fehler und Warnungen geloggt, die für mich als Entwickler wichtig sind, um vorhandene Probleme zu lösen</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Hoster-Plugins</h2>
<ul>
	<li>Einige Hoster-Plugins haben eigene Einstellungen</li>
	<li>Diese sind aber nicht bei den anderen Einstellungen, sondern findet man unter Regeln -> Hoster-Plugins. Dort in der Liste gibt es dann einen Button "Einstellungen" bei den Hoster, die Einstellungen haben.</li>
	<li>Hier eine Beschreibung von Einstellungen, die bei mehreren Hoster vorhanden sind</li>
	<li class="noListStyle">
		<ul>
			<li>Diesen Hoster deaktivieren</li>
			<li class="noListStyle">
				<ul>
					<li>Wird diese Option aktiviert, so werden Links von diesem Hoster nicht mehr erkannt und können nicht mehr heruntergeladen werden. Dies kann z.B. gebrauchen, wenn man Rapidshare-Links lieber mit einem anderen Programm runterladen möchte und die Erkennung im BH dafür abschalten will.</li>
				</ul>
			</li>
			<li>Downloads von diesem Hoster immer in folgendem Ordner speichern:</li>
			<li class="noListStyle">
				<ul>
					<li>Diese Option dient dazu, Dateien von einem Hoster in einem bestimmten Verzeichnis abzuspeichern, anstatt im Standard-Download-Verzeichnis. Z.B. will man Dateien von Rapidshare in einem extra dafür erstellten Ordner, dann kann man diese Option aktivieren und das Verzeichnis auswählen.</li>
					<li>Die dazugehörige Option "BH erlauben Unterordner zu erstellen in dem angegebenen Ordner" bestimmt, ob man bei der Download-Auswahl trotz des extra Verzeichnisses noch den Titel als Unterverzeichnis verwenden kann.</li>
					<li>Wichtig: Ist diese Option aktiviert und man wählt in der Download-Auswahl ein anderes Verzeichnis, so würde dies nicht berücksichtigt. Es sei denn die Option für Unterverzeichnisse wäre aktiviert und es handelt sich wirklich um ein Unterverzeichnis des angegeben Verzeichnisses.</li>
				</ul>
			</li>
			<li>Maximale Anzahl Verbindungen</li>
			<li class="noListStyle">
				<ul>
					<li>Mit dieser Option kann für Downloads eines Hosters festlegen, wieviele gleichzeitige Downloads es geben darf.</li>
					<li>Wird bei dieser Einstellung ein grösserer Wert, als für die Einstellung für das ganze Programm gewählt, so gilt immer die Einstellung für das ganze Programm.</li>
					<li>Diese Option ist nützlich bei Hostern, die z.B. nur ein gleichzeitiger Download zulassen. Im BH würde ohne die Einstellung, dann versucht so viele Verbindungen zu öffnen, wie bei den Einstellungen für das ganze Programm festgelegt. Diese würde, falls der Hoster dann nur eine Verbindung zulässt, dafür sorgen, das nur ein Download wirklich ausgeführt wird, die anderen würden fehlschlagen. Begrenzt man aber mit der Einstellung die Anzahl, so wird BH hintereinander immer einen Download starten und warten bis dieser fertig ist, bevor der nächste Download gestartet wird.</li>
				</ul>
			</li>
			<li>Automatisch warten bis nächster Download beginnen kann</li>
			<li class="noListStyle">
				<ul>
					<li>Einige Hoster haben einen Traffic-Limit. Z.B. kann man bei einem Hoster nur 100 MB pro Stunde runterladen. Danach muss man eine gewisse Zeit warten. Bei einige Hostern ist BH in der Lage, diese Wartezeit zu erkennen und sie abzuwarten und dann automatisch den Download zu starten.</li>
				</ul>
			</li>
			<li>Premium-Account</li>
			<li class="noListStyle">
				<ul>
					<li>Einige Hoster bieten gegen Bezahlung Premium-Accounts (Kann je nach Hoster verschieden heissen) an. Für einige Hoster gibt es die Einstellung die Unterstützung für solche Accounts zu aktivieren. Wichtig ist, dass dies nur mit Cookies funktioniert. Möchte man also den Premium-Account im BH nutzen, so muss in den Einstellungen unter Verbindung -> Cookies, der entsprechende Browser ausgewählt werden. Verwendet man einen Browser der nicht unterstützt wird, so müsste man einen der unterstützten Browser bei sich installieren und sich mit dem bei dem entsprechenden Hoster einmal einloggen. Dann sollte der Browser die Cookies abspeichern und BH kann die einlesen.</li>
				</ul>
			</li>
			<li>HostDefaultFiles</li>
			<li class="noListStyle">
				<ul>
					<li>Dieses Hoster-Plugin ist keinem Hoster zugeordnet, sondern sorgt dafür, dass direkt verlinkte Dateien unabhängig vom Hoster erkannt werden.</li>
					<li>Content-Type prüfen bei direkt verlinkten Bildern</li>
					<li class="noListStyle">
						<ul>
							<li>Diese Option sorgt dafür, dass bei Bilder geprüft wird, ob es sich wirklich um ein Bild handelt. Dies sorgt allerdings dafür, dass der Download viel länger dauert und es kann trotzdem sein, dass es sich bei einer Datei um ein Bild handelt, BH dann dieses aber nicht als solches erkennt. Diese Option sollte man also besser nicht aktivieren und sie wird eventuell in neueren Versionen vom BH gar nicht mehr verfügbar sein.</li>
						</ul>
					</li>
					<li>Alle Dateitypen (Alle Links werden akzeptiert, egal ob es wirklich eine Datei ist oder nicht)</li>
					<li class="noListStyle">
						<ul>
							<li>Das sagt eigentlich schon alles. Alle Links werden vom BH erkannt und in der Download-Auswahl angezeigt.</li>
						</ul>
					</li>
					<li>Dann gibt es Einstellungen um die Erkennung von Bildern, Videos, Audio-Dateien und Archive einzuschalten oder abzuschalten. Unabhängig voneinander.</li>
					<li>Es gibt noch einen Weg andere Links von diesem Plugin erkennen zu lassen. Dieser ist unter dem Punkt "Direkt verlinkte Bilder" hier beschrieben: <a href="?loc=bilderherunterlader/rules&amp;lng=<?=$lng?>"><?=$lang["rules"][$lng]?></a></li>
				</ul>
			</li>
		</ul>
	</li>
</ul>
</div>