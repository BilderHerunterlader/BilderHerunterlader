<h1>Verwendung</h1>
<div class="features">
<ul>
	<li>Java herunterladen und installieren, falls es noch nicht installiert ist</li>
	<li>BilderHerunterlader herunterladen und installieren</li>
	<li>Auf der Download-Seite falls vorhanden, die Extension oder das Plugin für den Browser herunterladen und installieren</li>
	<li>Nun muss damit die programminternen Updates funktionieren dem Benutzeraccount volle Rechte auf den Programmordner eingeräumt werden.<br />
	Das ist nötig unter Windows Vista, Windows 7, Linux, MacOS und unter Windows 2000 / XP (falls man mit eingeschränkten Rechten arbeitet)</li>
	<li class="noListStyle">
		<ul>
			<li>Windows Vista / 7 / 8 / 10: Rechtsklick auf BilderHerunterlader-Programmordner -> Eigenschaften -> Sicherheit -> Auf Bearbeiten klicken -> Benutzer auswählen -> Haken bei Vollzugriff Zulassen setzen -> OK klicken bis alle Fenster wieder geschlossen sind.</li>
			<li>Windows 2000 / XP: Zuerst in den Ordneroptionen die Einfache Dateifreigabe ausschalten. Rechtsklick auf BilderHerunterlader-Programmordner -> Eigenschaften -> Sicherheit -> Benutzer auswählen -> Haken bei Vollzugriff Zulassen setzen -> OK klicken</li>
			<li>Linux: Entweder über Benutzeroberfläche die Rechte ändern oder über die Kommandozeile. Es gibt viele Webseiten für jede Linux-Distribution, die zeigen wie das genau geht.</li>
			<li>MacOS: Da ich kein Mac besitze und das System überhaupt nicht kenne, kann ich nicht sagen, wie es unter MacOS geht.</li>
		</ul>
	</li>
	<li>BilderHerunterlader starten</li>
	<li class="noListStyle">
		<ul>
			<li>BilderHerunterlader hat keine .exe-Datei. Es hat eine BH.jar, diese Datei ist wie eine .exe-Datei. Im Normalfall kann man auf diese einfach nur doppelt klicken oder im Startmenü den Eintrag anklicken und das Programm sollte starten.</li>
			<li>Es gibt Programme wie z.B. Winrar, die eventuell die .jar-Dateiendung für sich registrieren. Dann wird anstatt dem BilderHerunterlader die .jar-Datei mit Winrar oder so geöffnet.<br />Ist dies der Fall, so muss die Dateiendung wieder Java zugeordnet werden. Unter Windows geht man dafür im Explorer auf Extras - Ordneroptionen - Dateitypen. Da scrollt man runter bis zum Eintrag JAR. Den Eintrag auswählen und auf Erweitert klicken. Die Aktion "open" anklicken und auf "Bearbeiten" klicken. Bei "Anwendung für diesen Vorgang" muss man nun den Pfad zu Java eintragen. <br />Üblicherweise wird Java in C:\Programme\Java\jre6\bin installiert. Je nach Java-Version und Betriebssystem kann dies abweichen. Wer noch Java in Version 5 hat, findet Java unter C:\Programme\Java\jre1.5.0_xx\bin (xx steht dabei für eine beliebige Zahl). Hat man das entsprechende Verzeichnis gefunden, kopiert man diesen Pfad und löscht alles bei "Anwendung für diesen Vorgang" und fügt den Pfad dann ein. Danach muss man noch ganz am Anfang ein " einfügen und ganz hinten (hinter \bin) noch javaw.exe" -jar "%1" %* einfügen. <br />Der Eintrag sollte schlussendlich so Aussehen: "C:\Programme\Java\jre6\bin\javaw.exe" -jar "%1" %*<br />
			Danach alle Fenster mit OK schliessen. Und nun sollte man BilderHerunterlader starten können.<br /><br />Für fast alle Windows Versionen (auch XP, Vista und 7) gibt es das Programm jarfix, welches das Problem automatisch für euch lösen sollte: <a href="http://johann.loefflmann.net/de/software/jarfix/index.html">http://johann.loefflmann.net/de/software/jarfix/index.html</a></li>
		</ul>
	</li>
	<li>Beim ersten Start werden die Standard-Einstellungen verwendet, diese sollte man nun an seine Bedürftnisse anpassen.<br />
	Am wichtigsten sind erstmal folgende Einstellungen:</li>
	<li class="noListStyle">
		<ul>
			<li>Verbindung -> Direkte Verbindung / HTTP-Proxy: Falls du über einen Proxy ins Internet gehst, musst du diese bei dieser Einstellung angeben, sonst funktioniert gar nichts.</li>
			<li>Verbindung -> Cookies: Hier musst du einen Browser, falls vorhanden, auswählen. Denn der Download von gewissen Webseiten funktioniert ohne Cookies nicht.</li>
			<li>Verbindung -> User-Agent: Hier solltest du den User-Agent von deinem Browser eintragen. Denn sonst kann es vorkommen, dass das Senden der Links an BH über das Opera-Plugin oder die In-die-Zwischenablage-Kopieren Methode nicht funktioniert für einige Webseiten.<br />
			Wichtig: Der User-Agent von deinem Browser ändert sich oft, wenn der Browser aktualisiert wird oder neue Plugins/Extensions installiert werden. Dann müsstest du dies auch im BH ändern.<br />
			Um den User-Agent deines Browsers herauszufinden kannst du auf folgende Seite surfen: <a href="http://whatsmyuseragent.com/">http://whatsmyuseragent.com/</a></li>
			<li>Verzeichnisse -> Standard-Speicherort: Hier wählst du ein Verzeichnis aus, in welches die Dateien heruntergeladen werden sollen.</li>
			<li>Sonstige -> Updates: Viele Hostern ändern immer mal wieder ihre Webseiten, womit der Download mit BilderHerunterlader nicht mehr funktioniert. Dafür gibt es eine programminterne Update-Funktion die BilderHerunterlader auf dem aktuellen Stand hält und auch Regeln und Hoster-Plugins aktualisiert. Ist die Einstellung aktiviert, so wird bei jedem Start von BilderHerunterlader geprüft, ob es Updates gibt. Wenn man das nicht will, so kann man auch manuell auf Updates prüfen, dazu geht man auf Info -> Update.</li>
		</ul>
	</li>
	<li>Webseite im Browser öffnen, die Links zu Bildern / Dateien enthalten</li>
	<li class="noListStyle">
		<ul>
			<li>Bei Firefox: Rechtsklick -> Dateien mit BH herunterladen</li>
			<li>Bei Opera: Rechtsklick -> Dateien mit BH herunterladen</li>
			<li>Beim Internet Explorer: Rechtsklick -> Dateien mit BH herunterladen</li>
			<li>Alternativ: URL der Webseite in die Zwischenablage kopieren (ctrl + c). Damit das funktioniert muss in den Einstellungen unter Sonstige die Option "Zwischenablage - Auf einzelne Links prüfen" aktiviert sein.<br/>
			Falls nichts passiert, im System-Tray-Icon-Menü oder in der Menüleiste "Prüfe Zwischenablage" anklicken</li>
			<li>Weitere Alternative: Im Browser die Bilder / Links markieren und mit der Maus packen und im Programm-Fenster loslassen (Drag &amp; Drop)</li>
		</ul>
	</li>
	<li>Nun öffnet sich ein neues Fenster, welches die verfügbaren Dateien anzeigt</li>
	<li>Hier kann man nun das Download-Verzeichnis ändern, oder es automatisch bestimmen lassen anhand von Schlüsselwörtern. Auch kann man festlegen welche Dateien heruntergeladen werden sollen. Und noch weitere Dinge festlegen.<br />
	Für eine genaue Beschreibung aller Möglichkeiten sollte man die Feature-Seite gut durchlesen: <a href="?loc=bilderherunterlader/features&amp;lng=<?=$lng?>"><?=$lang["features"][$lng]?></a></li>
	<li>Danach auf OK klicken und die Dateien werden in die Download-Warteschlange aufgenommen</li>
	<li>Nun bei der Warteschlange auf Start klicken und die Dateien werden heruntergeladen</li>
</ul>
</div>
<br />
<br />
<h1><a name="settings" class="noStyle">Einstellungen</a></h1>
<p>
Damit die Einstellungen angewendet werden muss auf "Speichern" oder "Anwenden" gedrückt werden!<br />
Einige Änderungen werden erst nach dem neustart des Programms wirksam.
</p>
<div class="features">
<h2>Oberfläche</h2>
<ul>
	<li>Aussehen</li>
	<li class="noListStyle">
		<ul>
			<li>Legt das Aussehen des Programms fest</li>
			<li>"An Betriebsystem angepasst" funktioniert möglicherweise nicht auf allen Systemen</li>
		</ul>
	</li>
	<li>Sprache</li>
	<li class="noListStyle">
		<ul>
			<li>Legt die Sprache des Programms fest</li>
			<li>Damit eine Änderung übernommen wird, muss BilderHerunterlader neu gestartet werden</li>
		</ul>
	</li>
	<li>Grössenanzeige</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest in welcher Grösseneinheit Dateigrössen angezeigt werden</li>
			<li>"Automatische Anpassung" wählt selber jeweils eine geeignete Einheit</li>
		</ul>
	</li>
	<li>Fortschrittanzeige</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest wie die Fortschrittanzeige dargestellt wird</li>
		</ul>
	</li>
	<li>Fortschrittanzeige - Downloadgeschwindigkeit anzeigen</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob bei der Fortschrittanzeige die Downloadgeschwindigkeit angezeigt wird</li>
		</ul>
	</li>
	<li>Fenster - Grösse und Position speichern</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob die Grösse und die Position des Fenster gespeichert wird und das Fenster beim nächsten Start wieder an der selben Position angezeigt wird und in der gleichen Grösse</li>
		</ul>
	</li>
	<li>Tabellen - Spaltenbreiten speichern</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob die Spaltenbreiten von Tabellen gespeichert werden und diese beim nächsten Start wieder gleich breit angezeigt werden</li>
		</ul>
	</li>
	<li>Tabellen - Tabellen-Sortierung speichern</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob die Sortierung von Tabellen gespeichert werden und diese beim nächsten Start wieder gleich sortiert werden</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Verbindung</h2>
<ul>
	<li>Direkte Verbindung / HTTP-Proxy</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest wie sich das Programm mit dem Internet verbindet</li>
			<li>Die Direkte Verbindung ist der Normalfall</li>
			<li>Nur wenn man über einen Proxyserver mit dem Internet verbunden ist, muss dieser in den Einstellungen angegeben werden</li>
		</ul>
	</li>
	<li>Anzahl Verbindungen</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest wieviele Dateien gleichzeitig heruntergeladen werden</li>
			<li>Bei dieser Einstellung sollte man vorsichtig sein. Webserver auf dehnen die Dateien gespeichert sind, lassen nur eine bestimmte Anzahl Verbindungen vom einem Benutzer zu.</li>
			<li>Setzt man nun die Anzahl Verbindungen auf 9 ein, und ein Webserver lässt nur 8 Verbindungen zu, dann wird der Download des 9. Bildes fehlschlagen. Selbstverständlich kann der Download dieses Bildes noch einmal gestartet werden, nur ist das mühsam. Also sollte diese Einstellung nicht zu hoch gesetzt werden.</li>
		</ul>
	</li>
	<li>Cookies</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob und von welchem Browser die Cookies verwendet werden</li>
			<li>Momentan ist es nur möglich die Cookies von Firefox, Opera und Internet Explorer zu verwenden</li>
			<li>Cookies werden teilweise von Webseiten verlangt, ansonsten funktioniert der Download nicht</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Verzeichnisse</h2>
<ul>
	<li>Standard-Speicherort</li>
	<li class="noListStyle">
		<ul>
			<li>Verzeichnis auf der Festplatte, in dem die Dateien standardmässig abgespeichert werden</li>
			<li>In der Download-Auswahl und in der Warteschlange kann aber für alle Dateien das Verzeichnis noch angepasst werden</li>
		</ul>
	</li>
	<li>Standard-Speicherort - Letztes benutztes Verzeichnis speichern</li>
	<li class="noListStyle">
		<ul>
			<li>Ist diese Option aktiviert, so wird als Standard-Speicherort immer das Verzeichnis benutzt, welches als letztes in der Download-Auswahl ausgewählt wurde</li>
		</ul>
	</li>
	<li>Unterverzeichnisse</li>
	<li class="noListStyle">
		<ul>
			<li>Diese Einstellung dient dazu Dateien aufgrund ihrer Grösse nach dem Download automatisch in entsprechende Unterverzeichnisse zu sortieren. Ist der Haken bei "Unterverzeichnisse aktiviert" gesetzt, so wird dies gemacht.</li>
			<li>Bei "Unterverzeichnis" gibt man den Namen des Unterverzeichnis an und dazu legt man eine minimale und eine Maximale Dateigrösse fest. Nach dem Download wird dann automatisch eine Datei, falls die Dateigrösse in dem festgelegten Bereich liegt, in das Unterverzeichnis verschoeben.</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Schlüsselwörter</h2>
<ul>
	<li>Schlüsselwörter-Suche</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest wie das Programm nach Schlüsselwörtern sucht</li>
			<li>Diese Einstellung bewirkt allerdings nur etwas bei der Suche über den Titel, 
da diese Einstellung nur da sinnvoll ist</li>
			<li>"Nur exakte Übereinstimmungen"</li>
			<li class="noListStyle">
				<ul>
					<li>Nur exakte Übereinstimmungen werden akzeptiert.<br />
Beispiel:<br />
Schlüsselwöter: Nora Tschirner; Nori Tschirner<br />
Es wird nach "Nora Tschirner" und "Nori Tschirner" gesucht.</li>
				</ul>
			</li>
			<li>"Alle Übereinstimmungen (Streng)"</li>
			<li class="noListStyle">
				<ul>
					<li>Hier werden die einzelnen Wörter auch zur Suche benutzt.<br />
Es werden allerdings nur Übereinstimmungen akzeptiert, wenn vor oder nach dem Wort kein Buchstabe kommt.<br />
Beispiel:<br />
Schlüsselwörter: Nora Tschirner; Nori Tschirner<br />
Es wird nach "Nora Tschirner", "Nori Tschirner", "Nora", "Tschirner" und "Nori" gesucht.<br />
Vor und hinter den Wörtern "Nora", "Tschirner"und "Nori" darf allerdings kein Buchstabe vorkommen.</li>
				</ul>
			</li>
			<li>"Alle Übereinstimmungen"</li>
			<li class="noListStyle">
				<ul>
					<li>Es werden alle Übereinstimmungen akzeptiert</li>
				</ul>
			</li>
		</ul>
	</li>
	<li>Schlüsselwörter-Suche - Bei negativer Zielerkennung Link nicht markieren</li>
	<li class="noListStyle">
		<ul>
			<li>Diese Einstellung bewirkt, dass wenn bei der Suche nach Schlüsselwörtern keines gefunden wird für eine Datei, so wird diese in der Download-Auswahl nicht ausgewählt, würde also nicht heruntergeladen. Natürlich kann man in der Download-Auswahl danach diese Datei trotzdem wieder für den Download auswählen.</li>
			<li>Diese Einstellung bewirkt allerdings nur etwas bei der Suche über die Dateinamen (Links), 
da diese Einstellung nur da sinnvoll ist</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Download</h2>
<ul>
	<li>Logs - Logs speichern</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob Dateien, die heruntergeladen wurden, in einer Liste gespeichert werden. Anhand dieser Liste kann das Programm später erkennen ob ein Bild schon mal heruntergeladen worden ist. Ist diese Einstellung deaktiviert, so kann diese Überprüfung nicht mehr gemacht werden.</li>
		</ul>
	</li>
	<li>Downloads - Downloads automatisch starten</li>
	<li class="noListStyle">
		<ul>
			<li>Bewirkt das Downloads beim hinzufügen automatisch gestartet werden</li>
		</ul>
	</li>
	<li>Dateiname</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest welche Zeichen in Dateinamen erlaubt sind</li>
		</ul>
	</li>
	<li>Downloads - Deaktivieren nach x Fehlversuchen</li>
	<li class="noListStyle">
		<ul>
			<li>Bewirkt das Downloads nach der eingestellten Anzahl Fehlschläge automatisch deaktiviert werden</li>
		</ul>
	</li>
	<li>Minimale Dateigrösse (Bytes)</li>
	<li class="noListStyle">
		<ul>
			<li>Legt eine Minimale Dateigrösse fest. Wird eine Datei heruntergeladen, deren Grösse kleiner ist als die festgelegte minimale Grösse, so wird Download als Fehlschlage gewertet.</li>
			<li>Wird der Wert 0 gewählt, so ist diese Einstellung deaktiviert</li>
		</ul>
	</li>
	<li>Timeout (ms)</li>
	<li class="noListStyle">
		<ul>
			<li>Legt ein Zeitspanne fest, in der BilderHerunterlader zeit hat eine Verbindung herzustellen. Ist diese Zeit abgelaufen, so wird der Download als Fehlschlag gewertet</li>
			<li>Diese Einstellung sollte nicht zu gross gewählt werden, aber auch nicht zu klein. Am besten ist es, die Standard-Einstellung so zu belassen und nur bei Problemen, diese Einstellung anpassen.</li>
		</ul>
	</li>
	<li>Downloads beim Programmstart sortieren</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob und wie Downloads beim Start vom BilderHerunterlader in der Warteschlange sortiert werden</li>
		</ul>
	</li>
	<li>Benachrichtigung</li>
	<li class="noListStyle">
		<ul>
			<li>Ist diese Einstellung aktiviert, so wird eine Benachrichtigung am unteren rechten Bildschirmrand angezeigt, sobald alle Downloads abgeschlossen sind</li>
		</ul>
	</li>
</ul>
</div>

<div class="features">
<h2>Sonstige</h2>
<ul>
	<li>Updates - Beim Start auf Updates prüfen</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob beim Start auf Updates geprüft wird</li>
		</ul>
	</li>
	<li>Zwischenablage - Auf einzelne Links prüfen</li>
	<li class="noListStyle">
		<ul>
			<li>Legt fest ob das Programm die Zwischenablage auf Links prüft. Diese Einstellung muss aktiviert werden wenn man einen anderen Browser als Firefox, Opera oder Internet Explorer verwendet, da nur für diese Browser eine Extensions / Plugins zur Verfügung steht. Um diese Funktion zu nutzen, öffnet man eine Seite im Browser, die Links zu Dateien enthält. Nun kopiert man die URL (Adresse) der Seite in die Zwischenablage. Das Programm lädt nun diese Seite herunter und überprüft ob Links zu Dateien auf der Seite vorhanden sind und bietet diese dann zum Download an.</li>
		</ul>
	</li>
	<li>Zielverzeichnis - Titel immer zum Zielverzeichnis hinzufügen</li>
	<li class="noListStyle">
		<ul>
			<li>Bewirkt das in der Download-Auswahl der Titel automatisch zum Zielverzeichnis hinzugefügt wird</li>
		</ul>
	</li>
	<li>Regeln - Regeln haben höhere Priorität als Hoster-Klassen</li>
	<li class="noListStyle">
		<ul>
			<li>Ist diese Option aktiviert so werden zuerst die Regeln durchsucht und erst dann die Hoster-Plugins. (Beim hinzufügen von Links und beim herausfinden der Bild-URL beim Download)</li>
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