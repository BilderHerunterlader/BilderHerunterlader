{literal}
<div class="irada">
	<h1><a name="h00">Inhalt</a></h1>
	<div>
		<table class="inh"><tr>
			<td>1.</td>
			<td><a href="#h01">Grundaufbau</a></td>
		</tr><tr>
			<td>2.<em>a</em>&nbsp;&nbsp;</td>
			<td><a href="#h02">Sektion [General]</a></td>
		</tr><tr>
			<td>2.<em>b</em></td>
			<td><a href="#h03">Sektion [AdditionalPostValues]</a></td>
		</tr><tr>
			<td>2.<em>c</em></td>
			<td><a href="#h031">Sektion [FilenameRegExp]</a></td>
		</tr><tr>
			<td>3.<em>a</em>&nbsp;&nbsp;</td>
			<td><a href="#h04">Beispiel: ImageVenue</a></td>
		</tr><tr>
			<td>3.<em>b</em></td>
			<td><del><a href="#h05">Beispiel: Rapidshare</a></del></td>
		</tr></table>
	</div>

	<h1><a name="h01"><strong>1.</strong> Grundaufbau</a></h1>
	<div>
		Jede <b>HOST.INI</b> ist gleich aufgebaut. Die Konventionen sind wie die einer Standard-INI-Datei unter Windows: es gibt Sektionen, Schlüssel und Werte.
		<br /><br />
		Sektionen starten mit einer Zeile, an deren Anfang ein <b>[</b> und an deren Ende ein <b>]</b> stehen, gefüllt von dem Sektionsnamen. Sie gelten bis zum Anfang der nächsten Sektion oder (falls keine mehr kommt) bis zum Ende der Datei. Alle übrigen Zeilen sind Paare aus Schlüssel und Werten, wobei die Zeichen vom Anfang einer Zeile bis zum ersten Gleichheitszeichen (<b>=</b>) den <b>Schlüssel</b> darstellen, und alle Zeichen hinter dem ersten Gleichheitszeichen den <b>Wert</b>. Für nähere Belesung empfehle ich den englischen <a class="out" href="http://en.wikipedia.org/wiki/INI_file">Artikel in Wikipedia</a> - die hier verwendete Implementierung entspricht der WinAPI-Funktion <a class="out" href="http://msdn2.microsoft.com/en-us/library/ms724353.aspx">GetPrivateProfileStringW</a>. Wichtig ist jedoch: auch Werte können in <i>Gänsefüßchen</i> stehen, (also eins gleich hinter dem Gleichheitszeichen und eins als letztes der Zeile) - diese werden jedoch beim interpretieren <i>geschluckt</i>. Der Sinn solch einer Einschließung ist, dass damit auch z.B. Leerzeichen als erstes und letztes Zeichen des Wertes mit interpretiert werden (ansonsten findet eine <i>Trimmung</i> statt).
		<br /><br />
		Das Dateiformat kann entweder <b>ANSI</b> oder auch <b>Unicode (Little Endian)</b> sein.
	</div>

	<h1><a name="h02"><strong>2.<em>a</em></strong> Sektion [General]</a></h1>
	<div>

		Hier finden sich alle Haupteinstellungen zum Hoster, von denen viele nötig sind. Je nach Hoster kann man einige weglassen und benötigt dafür andere. Lässt man einen weg, gilt immer ein Standardwert dafür. Zur Übersicht nun alle samt Erklärung:
		<br /><br />
		<table><thead><tr>
			<th>Schlüssel</th>
			<th>Bedeutung</th>
			<th>Standard</th>
		</tr></thead><tr>
			<td class="col" colspan="3">allgemeine Einstellungen</td>
		</tr><tr>
			<td>Version</td>
			<td>(Text) Dateiversionierung: Reine Info für jeden Anwender und Entwickler, in welcher Version die Datei vorliegt. Sollte möglichst im Format <b>yyyy-MM-dd, Autor</b> sein (4stellige Jahreszahl, Bindestrich, 2stellige Monatszahl, Bindestrich, 2stellige Tageszahl, Komma, Leerzeichen, Autorenname)</td>
			<td>leer</td>
		</tr><tr>
			<td>Icon</td>
			<td>(Text) Hosterbild: Dateiname zum Anzeigen eines kleinen Bildchens zu dem Eintrag in der ComboBox. Dieselbe Datei muss sich dann unter <b>.\host.ico\</b> befinden. Das Icon sollte in der Größe 16x16 in 256 Farben vorliegen (und möglichst <u>nur</u> dieses eine Format).</td>
			<td>leer (keine Bildchenanzeige)</td>
		</tr><tr>
			<td>DisplayName</td>
			<td>(Text) Anzeigename des Eintrags in der ComboBox. Dieser Name ist frei wählbar und somit unabhängig vom Dateinamen</td>
			<td>leer (kein Anzeigename)</td>
		</tr><tr>
			<td>StartDelay</td>
			<td>(Zahl) Wartezeit in Millisekunden, bevor ein Thread mit dem Hochladen bei diesem Hoster starten soll</td>
			<td>0 (keine Wartezeit)</td>
		</tr><tr>
			<td>StartDisconnect</td>
			<td>(Zahl) Schalter, ob vor dem Hochladen die Internetverbindung getrennt werden soll (<b>0</b>=nein, <b>1</b>=ja). <b><i>Derzeit noch nicht implementiert, da Wiederverbindungsmechanismus noch nicht vorhanden!</i></b></td>
			<td>0 (nein)</td>
		</tr><tr>
			<td>MaxFileSize</td>
			<td>(Zahl) Höchstdateigröße für den Hoster. Viele Hoster erlauben nur Dateien bis zu einer bestimmten Größe. Und wenn sie von Kilo oder Mega sprechen, meinen sie meist den Faktor 1000 (statt 1024). Hier muss die komplette Zahl angegeben werden, Buchstaben (wie z.B. <b>k</b> und <b>M</b> sind <u>nicht</u> zulässig!). Dieser Eintrag verhindert das Hinzufügen zu großer Dateien zur Liste. Enthält dieser Eintrag jedoch <b>0</b>, so ist die Dateigröße unbegrenzt.</td>
			<td>0 (keine Größenbeschränkung)</td>
		</tr><tr>
			<td>FileFilterRegExp</td>
			<td>(Text) Dateiendungsfilter: Regulärer Ausdruck, mit dem geregelt werden kann, welche Dateiendungen beim Hinzufügen erlaubt sind. Viele Hoster erlauben nur bestimmte Dateiendungen. Ist diese Angabe leer, wird nicht auf Dateiendungen geprüft.</td>
			<td>leer (keine Prüfung auf Dateiendung)</td>
		</tr><tr>
			<td>ForceThreads</td>
			<td>(Zahl) Höchstanzahl paralleler Prozesse für <u>diesen</u> Hoster: wenn im Programm eingestellt ist, dass z.B. 5 Threads gleichzeitig laufen können, dann kann mit diesem ein restriktiveres Verhalten pro Hoster erzeugt werden. Eine <b>0</b> hat keine Auswirkung. Eine <b>1</b> bewirkt, dass für speziell <u>diesen</u> Hoster immer nur 1 Hochladeprozess aktiv sein darf. Eine <b>2</b>, dass es nur 2 parallel sein dürfen, usw... Die Reglementierung ist aus Sicherheitsgründen (zunächst für <a class="out" href="http://www.rapidshare.com">RapidShare</a>-Premium) gedacht, wo parallele Uploads mit unterschiedlichen Sessions möglicherweise ein Sperren des Kontos zur Folge haben könnten.</td>
			<td>0 (keine Einschränkung)</td>
		</tr><tr>
			<td>Protocol</td>
			<td>(Zahl) HTTP-Protokollversion, welches genutzt werden soll: die <b>9</b> ist für <i>HTTP 0.9</i>, die <b>1</b> für <i>HTTP 1.1</i> und jede andere Zahl für <i>HTTP 1.0</i>.</td>
			<td>HTTP 1.0</td>
		</tr><tr>
			<td>KeepAlive</td>
			<td>(Zahl) Schalter, ob die Verbindung geschlossen werden soll (<i>Connection: close</i>) oder nicht. Eine <b>1</b> hält sie offen, die <b>0</b> nicht.</td>
			<td>0 (schließen)</td>
		</tr><tr>
			<td>CustomUserAgent</td>
			<td>(Text) Setzt für den einzelnen Hoster einen User-Agent und übergeht damit die globale Einstellung - damit kann man sich bei unterschiedlichen Hostern als unterschiedliche Browser ausgeben</td>
			<td>leer (Programmeinstellungen gelten)</td>
		</tr><tr>
			<td>ClipboardRegExp</td>
			<td>(Text) Kopieren in Zwischenablage: Regulärer Ausdruck, mit dem ein Ergebnis zurechtgestutzt wird anstatt komplett in die Zwischenablage aufgenommen zu werden. Beispielsweise sollen <i>[tag]</i>s um den eigentlichen Link entfernt werden.</td>
			<td>leer (Programmeinstellungen gelten)</td>
		</tr><tr>
			<td>RapidshareFilesRegExpPattern</td>
			<td>(Text) Rapidshare Premium / Collector: Regulärer Ausdruck, mit dem bei der Ergebnisseite die Anzahl gehosteter Dateien ermitteln soll. Nötig für <b>RapidshareFilesSkipSubst</b>, damit auch hochgeladene Dateien gefunden werden, wenn das Konto über 500 Dateien zählt.</td>
			<td>leer</td>
		</tr><tr>
			<td>RapidshareFilesSkipSubst</td>
			<td>(Text) Rapidshare Premium / Collector: URL mit Substitionsplatzhalter (<i>$1</i>), um durch die Ergebnisseiten blättern zu können bei mehr als 500 Dateien im Konto.</td>
			<td>leer</td>
		</tr><tr>
			<td class="col" colspan="3">Proxy</td>
		</tr><tr>
			<td>ProxyHost</td>
			<td>(Text) Host: Rechneradresse des Proxys. Falls angegeben, hat dieser Wert Vorrang vor den globalen Programmeinstellungen.</td>
			<td>leer (Programmeinstellungen gelten)</td>
		</tr><tr>
			<td>ProxyPort</td>
			<td>(Text) Port: Portnummer des Proxys. Falls angegeben, hat dieser Wert Vorrang vor den globalen Programmeinstellungen.</td>
			<td>leer (Programmeinstellungen gelten)</td>
		</tr><tr>
			<td>ProxyUser</td>
			<td>(Text) Benutzer: Benutzername zur Autentifizierung auf dem Proxy. Falls angegeben, hat dieser Wert Vorrang vor den globalen Programmeinstellungen.</td>
			<td>leer (Programmeinstellungen gelten)</td>
		</tr><tr>
			<td>ProxyPass</td>
			<td>(Text) Passwort: Passwort zum Autentifizierung auf dem Proxy. Falls angegeben, hat dieser Wert Vorrang vor den globalen Programmeinstellungen.</td>
			<td>leer (Programmeinstellungen gelten)</td>
		</tr><tr>
			<td class="col" colspan="3">POST oder GET vor dem Hochladen</td>
		</tr><tr>
			<td>PrePostURL</td>
			<td>(Text) Vorläufer-POST: vor dem eigentlichen Hochladen der Datei muss evt. ein gewöhnlicher POST durchgeführt werden (bsp.weise, um sich auf einer Seite anzumelden. Hier gibt man die Zieladresse für den POST an, was natürlich mit <b>PrePostData</b> kombiniert werden sollte. Ist diese Angabe leer, erfolgt kein POST vor dem eigentlichen Hochladen.</td>
			<td>leer (kein vorheriges POST)</td>
		</tr><tr>
			<td>PrePostData</td>
			<td>(Text) Vorläufer-POST-Daten: an die unter <b>PrePostURL</b> angegebene Adresse werden die hier angegebenen Daten gesendet. Das Format ist denkbar einfach: Name-/Wert-Trennung erfolgt durch Gleichheitszeichen (<b>=</b>), Parametertrennung durch kaufmännisches <i>Und</i> (<b>&amp;</b>) - also z.B. <b>login=myname&amp;password=god&amp;x=0&amp;y=0</b>.</td>
			<td>leer</td>
		</tr><tr>
			<td>PrePostReferer</td>
			<td>(Text) Vorläufer-POST Referer: Referer für die unter <b>PrePostURL</b> angegebene Adresse. Verhalten wie <b>TargetReferer</b></td>
			<td>leer (kein Referer)</td>
		</tr><tr>
			<td>PreloadURL</td>
			<td>(Text) Vorläufer-GET: vor dem eigentlichen Hochladen der Datei muss evt. erst eine Seite aufgerufen werden (bsp.weise um ein Cookie zu erzeugen, oder um die dynamische Hochladeadresse ermitteln zu können). Hier gibt man die Zieladresse für die Seite an, was natürlich mit <b>PreloadRegExpPattern</b> kombiniert werden sollte. Ist diese Angabe leer, erfolgt kein Seitenaufruf vor dem eigentlichen Hochladen.</td>
			<td>leer (kein vorheriger Seitenaufruf)</td>
		</tr><tr>
			<td>PreloadRegExpPattern</td>
			<td>(Text) Vorläufer-Suche: in der Antwort der unter <b>PreloadURL</b> angeforderten Seite <u>oder</u> der unter <b>PrePostURL</b> benutzen POST-Adresse wird mit diesem regulären Ausdruck durchsucht. Die Ergebnisse (Klammernpaare) können sowohl in <b>TargetUploadURL</b>, als auch in allen <b>AdditionalPostValues</b> verwendet werden. Damit können sowohl die Hochladeadresse, als auch die mit ihr mitzuschickenden Variablen dynamisch aufgebaut werden.</td>
			<td>leer (keine Auswertung der Antwort)</td>
		</tr><tr>
			<td>PreloadRegExpSubst</td>
			<td>(Text) Vorläufer-Suchergebnis: Dieser Text wird entsprechend den Platzhaltern mit den Ergebnissen aus <b>PreloadRegExpPattern</b> substituiert. Siehe <b>ResultRegExpSubst</b></td>
			<td>leer (keine Ersetzung)</td>
		</tr><tr>
			<td>PreloadRegExpNeg</td>
			<td>(Text) Test auf negative Antwort: findet dieser reguläre Ausdruck in dem Antwortdokument des Vorläufer-POST oder -GET einen Treffer oder mehr, gilt das Hochladen bereits jetzt als gescheitert. Siehe auch <b>ResultRegExpNeg</b></td>
			<td>leer (keine Negativprüfung)</td>
		</tr><tr>
			<td>PreloadReferer</td>
			<td>(Text) Vorläufer-GET Referer: Referer für die unter <b>PreloadURL</b> angegebene Adresse. Verhalten wie <b>TargetReferer</b></td>
			<td>leer (kein Referer)</td>
		</tr><tr>
			<td class="col" colspan="3">Kernfunktion Hochladen</td>
		</tr><tr>
			<td>NoEmptyBracketsAdd</td>
			<td>(Zahl) Sollte der Schlüssel <b>TargetFieldName</b> ein Feld darstellen (leeres eckiges Klammernpaar <b>[]</b> am Namensende), so scheinen manche Browser trotz alledem zusätzlich noch einen Namen mit Index mitzuschicken. Diese Option regelt, ob Irada dieses Verhalten nachstellen soll. Aber auch hier reagieren Hoster unterschiedlich, deshalb gilt: <b>0</b>=nein (hinzufügen), <b>1</b>=ja (nicht beachten). Bisher war es nur <a class="out" href="http://www.imagevenue.com">ImageVenue</a>, der diese Einstellung erforderte.</td>
			<td>0 (hinzufügen)</td>
		</tr><tr>
			<td>TargetUploadURL</td>
			<td>(Text) Zieladresse, auf die der POST gerichtet sein soll - also die Datei hochgeladen werden soll. Kernstück und Hauptaufgabe von Irada. Bei dem gewünschten Hoster ist dies die Adresse, die im <b>&lt;FORM&gt;</b>-Tag als <b>Action</b> definiert ist. Hier muss selbstverständlich die vollständige Adresse stehen, relative machen keinen Sinn!</td>
			<td>leer</td>
		</tr><tr>
			<td>TargetReferer</td>
			<td>(Text) Referer fürs Ziel: erwartet der Hoster Referer im Header der Anfragen, so kann dieser hier angegeben werden. <b><i>Allerdings muss jeder Anwender von Irada selbständig auf seinem System sicherstellen, dass nicht etwa eine <b>Desktop-Firewall</b> generell Referer unterbindet.</i></b></td>
			<td>leer (kein Referer)</td>
		</tr><tr>
			<td>TargetFieldName</td>
			<td>(Text) Name der POST-Variable, die für die hochzuladene Datei verantwortlich ist. Erkennbar an dem HTML-Element <b>&lt;INPUT TYPE=&quot;file&quot;...&gt;</b>. Erstaunlicherweise heißt er bei den meisten Hostern <i>userfile</i>. Ist bei den Hostern ein Feld mit angegeben (also ein leeres eckiges Klammernpaar <b>[]</b> am Ende), so muss auch dieses mit aufgenommen werden. Gehört also mit zum Namen.</td>
			<td>leer</td>
		</tr><tr>
			<td class="col" colspan="3">Ergebnis nach dem Hochladen</td>
		</tr><tr>
			<td>ResultIncludeHeaders</td>
			<td>(Zahl) Antwort-Header: Die Antwortdatei auf den POST wird anschließend den regulären Ausdrücken zur Verfügung gestellt. Doch manchmal reicht das nicht. Hiermit kann man zusätzlich die Header der Antwort mit vor das eigentliche Dokument setzen - so hat man dann auch Sachen wie Hoster, URL usw... (<b>1</b>=Header mit ausgeben, <b>0</b>=Nur Antwortdokument)</td>
			<td>0 (keine Header)</td>
		</tr><tr>
			<td>ResultRegExpPattern</td>
			<td>(Text) Antwort-Suche: Über die Antwortdatei (ggf. mit deren Headern) wird dieser reguläre Ausdruck rübergejagt. Sinnigerweise arbeitet man hier am besten mit Klammerpaaren, die man dann später bei <b>ResultRegExpSubst</b> sinnvoll adressieren kann. Dieser Ausdruck sucht also nach dem, was wir wissen wollen - und das ist meist der Link für andere zu unserem Upload. In Irada sind alle Hoster-Dateien werkseitig darauf ausgerichtet, einen BBCode-Link zu erspähen.</td>
			<td>leer (keine Suche, Antwortdokument ist egal)</td>
		</tr><tr>
			<td>ResultRegExpSubst</td>
			<td>(Text) Antwort-Suchergebnis: Dieser Text wird entsprechend den Platzhaltern mit den Ergebnissen aus <b>ResultRegExpPattern</b> substituiert. Für das komplette Ergebnis kann man hier natürlich <b>$0</b> angeben, oder z.B. <b>$2</b> für das zweite gefundene Klammernpaar. Genauso ist es möglich, hier mittels Nichtplatzhaltern (also simpler Konstanten, also Text) die Ausgabe zu erweitern. Denn erst das Ergebnis dieser Substitution erscheint dem Programm als Ergebnis.</td>
			<td>leer (keine Ersetzung)</td>
		</tr><tr>
			<td>ResultRegExpNeg</td>
			<td>(Text) Test auf negative Antwort: findet dieser reguläre Ausdruck in dem Antwortdokument auf den POST einen Treffer oder mehr, gilt das Hochladen als gescheitert. Sehr nützlich, um z.B. auf Meldungen wie &quot;<i>Too many connections</i>&quot;, &quot;<i>File is too big</i>&quot; usw... entsprechend zu reagieren. Dazu muss man entsprechende Fehlerantwortseiten natürlich kennen. Findet dieser Ausdruck jedoch nichts und das Hochladen ist trotzdem wegen irgendetwas schiefgelaufen, wird (bis zur Maximalwiederholung) immer wieder von neuem versucht, die Datei hochzuladen. Dieser Ausdruck hat Vorrang vor dem für das (BBCode-Link)Ergebnis.</td>
			<td>leer (keine Negativprüfung)</td>
		</tr><tr>
			<td class="col" colspan="3">Seitenaufruf / -anforderung nach dem Hochladen</td>
		</tr><tr>
			<td>Follow1URL</td>
			<td>(Text) Verfolger 1: nach dem Hochladen und Auswerten der Antwortseite, soll evt. ein Link weiterverfolgt werden. Macht schon dann Sinn, wenn man bei speziellen Hostern nicht die Anzeigeseite <u>mit</u> dem Bild haben will, sondern den direkten Link <u>zum</u> Bild. Hier kann als Platzhalter auch die Ergebnisse aus dem regulären Ausdruck von <b>ResultRegExpPattern</b> verwenden. Wird hier kein Wert angegeben, gilt der Hochladeprozess als beendet.</td>
			<td>leer (keine weitere Verfolgung)</td>
		</tr><tr>
			<td>Follow1PostData</td>
			<td>(Text) Verfolger 1 POST: Wird dieser Wert befüllt (nach Regeln wie unter <b>PrePostData</b>), so wird auf die URL unter <b>Follow1URL</b> ein POST abgesetzt mit den hier eingetragenen Werten als Sendeinformationen. Andernfalls wird auf die Adresse nur ein GET gemacht. Das POSTen macht auch wieder nur in speziellen Fällen Sinn, z.B. kann man bei einigen Hostern nach dem Hochladen noch spezielle Einstellungen vornehmen (Schatten für Vorschaubilder etc.), die dann hiermit realisiert werden könnten.</td>
			<td>leer (kein POST)</td>
		</tr><tr>
			<td>Follow1RegExpPattern</td>
			<td>(Text) Verfolger 1 Suche: die unter <b>Follow1URL</b> aufgerufene Seite wird mittels diesem regulären Ausdruck durchsucht. Die Funktionsweise ist dieselbe wie bei <b>ResultRegExpPattern</b>.</td>
			<td>leer (keine Suche)</td>
		</tr><tr>
			<td>Follow1RegExpSubst</td>
			<td>(Text) Verfolger 1 Suchergebnis: Dieser Text wird entsprechend den Platzhaltern mit den Ergebnissen aus <b>Follow1RegExpPattern</b> substituiert. Funktioniert genauso wie <b>ResultRegExpPattern</b>.</td>
			<td>leer (keine Ersetzung)</td>
		</tr><tr>
			<td>Follow1RegExpNeg</td>
			<td>(Text) Test auf negative Antwort: findet dieser reguläre Ausdruck in dem unter <b>Follow1URL</b> angefordertem Antwortdokument einen Treffer oder mehr, gilt das Hochladen als gescheitert. Siehe auch <b>ResultRegExpNeg</b></td>
			<td>leer (keine Negativprüfung)</td>
		</tr><tr>
			<td>Follow1Referer</td>
			<td>(Text) Verfolger 1 Referer: Referer für die unter <b>Follow1URL</b> angegebene Adresse. Verhalten wie <b>TargetReferer</b></td>
			<td>leer (kein Referer)</td>
		</tr><tr>
			<td class="col" colspan="3">Zweiter Seitenaufruf / -anforderung nach dem Hochladen</td>
		</tr><tr>
			<td>Follow2URL</td>
			<td>(Text) Verfolger 2: Wie <b>Follow1URL</b>, nur mit Bezug auf allen Angaben aus den <b>Follow1</b>-Werten.</td>
			<td>leer (keine weitere Verfolgung)</td>
		</tr><tr>
			<td>Follow2PostData</td>
			<td>(Text) Verfolger 2 POST: Wie <b>Follow1PostData</b></td>
			<td>leer (kein POST)</td>
		</tr><tr>
			<td>Follow2RegExpPattern</td>
			<td>(Text) Verfolger 2 Suche: Wie <b>Follow1RegExpPattern</b></td>
			<td>leer (keine Suche)</td>
		</tr><tr>
			<td>Follow2RegExpSubst</td>
			<td>(Text) Verfolger 2 Suchergebnis: Wie <b>Follow1RegExpSubst</b></td>
			<td>leer (keine Ersetzung)</td>
		</tr><tr>
			<td>Follow2RegExpNeg</td>
			<td>(Text) Test auf negative Antwort: Wie <b>Follow1RegExpNeg</b></td>
			<td>leer (keine Negativprüfung)</td>
		</tr><tr>
			<td>Follow2Referer</td>
			<td>(Text) Verfolger 2 Referer: Wie <b>Follow1Referer</b></td>
			<td>leer (kein Referer)</td>
		</tr><tr>
			<td class="col" colspan="3">Dritter Seitenaufruf / -anforderung nach dem Hochladen</td>
		</tr><tr>
			<td>Follow3URL</td>
			<td>(Text) Verfolger 3: Wie <b>Follow1URL</b>, nur mit Bezug auf allen Angaben aus den <b>Follow2</b>-Werten.</td>
			<td>leer (keine weitere Verfolgung)</td>
		</tr><tr>
			<td>Follow3PostData</td>
			<td>(Text) Verfolger 3 POST: Wie <b>Follow1PostData</b></td>
			<td>leer (kein POST)</td>
		</tr><tr>
			<td>Follow3RegExpPattern</td>
			<td>(Text) Verfolger 3 Suche: Wie <b>Follow1RegExpPattern</b></td>
			<td>leer (keine Suche)</td>
		</tr><tr>
			<td>Follow3RegExpSubst</td>
			<td>(Text) Verfolger 3 Suchergebnis: Wie <b>Follow1RegExpSubst</b></td>
			<td>leer (keine Ersetzung)</td>
		</tr><tr>
			<td>Follow3RegExpNeg</td>
			<td>(Text) Test auf negative Antwort: Wie <b>Follow1RegExpNeg</b></td>
			<td>leer (keine Negativprüfung)</td>
		</tr><tr>
			<td>Follow3Referer</td>
			<td>(Text) Verfolger 3 Referer: Wie <b>Follow1Referer</b></td>
			<td>leer (kein Referer)</td>
		</tr></table>

		<br />
		Es gibt noch eine <u>Besonderheit</u> für alle *<b>Pattern</b>-Angaben: wird hier die Zeichenfolge <b>%%filename%%</b> verwendet, so wird diese vor Anwendung des regulären Ausdrucks durch den hochgeladenen Dateinamen ersetzt (natürlich entsprechend escaped). Braucht man bei irgendeiner Suche den Dateinamen der hochgeladenen Datei (um diesen in einer Fülle von Links der Antwortseite wiederzufinden), dann braucht man nur <b>%%filename%%</b> verwenden. <b><i>Diese Zeichenfolge gilt ausschließlich in Patterns, nicht in Substitutionen oder anderen Einstellungen!</i></b>
	</div>

	<h1><a name="h03"><strong>2.<em>b</em></strong> Sektion [AdditionalPostValues]</a></h1>
	<div>

		Alle Schlüssel-Wert-Paare hierdrin werden beim Hochladen als Name-Wert-Parameter dem POST (neben der eigentlichen Datei) hinzugefügt. Die Anzahl ist quasi unbeschränkt.
		<br /><br />
		Bei den Werten können sogar Platzhalter wie <b>$1</b>, <b>$2</b> usw... angegeben werden, wenn die Einstellung <b>PreloadRegExpPattern</b> entsprechend benutzt wurde. Dann werden jene Platzhalter mit den Ergebnissen des Vorläufers gefüllt und können so dynamisch angepasst werden.
	</div>

	<h1><a name="h031"><strong>2.<em>c</em></strong> Sektion [FilenameRegExp]</a></h1>
	<div>

		Der für die Zeichenfolge <b>%%filename%%</b> zurückgegebene Dateiname muss ggf. pro Hoster noch weiter bearbeitet werden. Dazu können in dieser Sektion Paare von Regulären Ausdrücken und Substitutionen angegeben werden. Pro Ersetzung müssen sich immer zwei Schlüsselpaare finden, die denselben frei wählbaren Präfix haben (z.B. <i>01</i>) und als Suffix jeweils <i>Pattern</i> (für das Suchmuster) und <i>Subst</i> (für die Substitution).
	</div>

	<h1><a name="h04"><strong>3.<em>a</em></strong> Beispiel: ImageVenue</a></h1>
	<div>

		<a class="out" href="http://www.imagevenue.com">ImageVenue</a> ist ein einfacher Fall - das Hochladen ist trivial und die anschließende Linkermittlung ebenfalls leicht. Nehmen wir uns also die entsprechende INI-Datei zur Brust:
		<br /><br />
		<table><thead><tr>
			<th>Schlüssel</th>
			<th>Wert</th>
			<th>Funktion / Aufgabe</th>
		</tr></thead><tr>
			<td class="col" colspan="3">Sektion [General]</td>
		</tr><tr>
			<td>Version</td>
			<td>2008-02-03, AmigoJack</td>
			<td>Info für Entwickler: diese Dateiversion ist vom 03. Februar 2008 und wurde von AmigoJack erstellt</td>
		</tr><tr>
			<td>Icon</td>
			<td>imagevenue.ico</td>
			<td>Im Verzeichnis <b>.\host.ico</b> existiert die Datei <b>imagevenue.ico</b>, die zum Anzeigen genutzt werden kann. Designfeature ;-)</td>
		</tr><tr>
			<td>DisplayName</td>
			<td>ImageVenue (safe for work)</td>
			<td>Anzuzeigender Name im Programm. Dieser sollte am aussagekräftigsten sein und auch nirgends doppelt auftauchen!</td>
		</tr><tr>
			<td>MaxFileSize</td>
			<td>3000000</td>
			<td><a class="out" href="http://www.imagevenue.com">ImageVenue</a> erlaubt nur Dateien bis zu einer bestimmten Größe. Sie sprechen von &quot;<i>3 MB</i>&quot;, meinen aber entweder nur <b>3.000 KB</b> oder gar nur <b>3.000.000 Byte</b>, daher wählen wir das kleinste. Fügt man jetzt unter Auswahl dieses Hosters der Liste von Irada Dateien hinzu, die größer sind, werden diese gar nicht erst aufgenommen</td>
		</tr><tr>
			<td>FileFilterRegExp</td>
			<td>#jpeg|jpg#i</td>
			<td><a class="out" href="http://www.imagevenue.com">ImageVenue</a> erlaubt auch nur entsprechende Dateiendungen. Dieser reguläre Ausdruck prüft, ob die Dateiendung (alles hinter dem letzten Punkt) <b>jpg</b> oder <b>jpeg</b> ist. Fügt man jetzt unter Auswahl dieses Hosters der Liste von Irada Dateien hinzu, die eine andere Dateiendung haben, werden diese gar nicht erst aufgenommen</td>
		</tr><tr>
			<td>NoEmptyBracketsAdd</td>
			<td>1</td>
			<td>Ein Spezialfall von <a class="out" href="http://www.imagevenue.com">ImageVenue</a>: verhindert, dass aufgrund des Feldes des Namens unter <b>TargetFieldName</b> nochmal ein weiterer Wert mitgeneriert und -verschickt wird</td>
		</tr><tr>
			<td>TargetUploadURL</td>
			<td>http://www.imagevenue.com/upload.php</td>
			<td>Die Adresse, an die der POST gesendet wird. Lässt sich aus dem Quellcode der Ausgangsseite (wo man die Datei zum Hochladen auswählt) finden, indem man die <b>ACTION</b>-Eigenschaft des entsprechenden <b>&lt;FORM ... /&gt;</b>-Tags auswertet und eine absolute Adresse daraus erstellt</td>
		</tr><tr>
			<td>TargetFieldName</td>
			<td>userfile[]</td>
			<td>Dies ist der Wert für die hochzuladende Datei. Erkennbar im Quellcode an dem <b>&lt;INPUT TYPE=&quot;file&quot; ... /&gt;</b>-Tag. Der Name muss 100%ig übernommen werden - also auch eckige Klammern, wie hier deutlich sichtbar</td>
		</tr><tr>
			<td>ResultRegExpPattern</td>
			<td>#(\[URL=.*/URL\])#ism</td>
			<td>Der BBCode-Link, den wir in der Antwortdatei des Servers suchen. Dieser reguläre Ausdruck sucht also nach <b>[URL= ... /URL]</b></td>
		</tr><tr>
			<td>ResultRegExpSubst</td>
			<td>$1</td>
			<td>Das Ergebnis des ersten (eigentlich einzigen) Klammernpaares unter <b>ResultRegExpPattern</b> soll zurückgegeben werden, also nur der Substitionstext <b>$1</b></td>
		</tr><tr>
			<td>ResultRegExpNeg</td>
			<td>#(/IMG\][^\[])|(File too big)|(can not store this type)#ism</td>
			<td>Negativerkennung: Liefert dieser reguläre Ausdruck ein Ergebnis, gilt das Hochladen als gescheitert. Wie man unschwer erkennen kann, wollen wir hier Antworten erkennen wie z.B. dass das Bild zu groß sei oder dass der Dateiinhalt irgendwie falsch war (wo <b>jpg</b> draufsteht muss noch lange nicht <b>jpg</b> drin sein). Oder der zurückgegebene Link ist gar keiner, sondern schon das Bild direkt (weil es viel zu klein war und entsprechend von <a class="out" href="http://www.imagevenue.com">ImageVenue</a> gar kein Vorschaubild extra erzeugt wurde)</td>
		</tr><tr>
			<td class="col" colspan="3">Sektion [AdditionalPostValues]</td>
		</tr><tr>
			<td>MAX_FILE_SIZE</td>
			<td></td>
			<td>Warum <a class="out" href="http://www.imagevenue.com">ImageVenue</a> sich selbst einen Wert ohne Inhalt schickt? Keine Ahnung...</td>
		</tr><tr>
			<td>imgcontent</td>
			<td>safe</td>
			<td>Dies ist die ComboBox, bei der man zwischen dem Bildinhalt entscheidet. Der andere mögliche Wert wäre <b>unsafe</b></td>
		</tr><tr>
			<td>action</td>
			<td>1</td>
			<td>Auch keine Ahnung, warum wir das mitschicken sollen</td>
		</tr><tr>
			<td>img_resize</td>
			<td></td>
			<td>Es gibt die Option, die Bilddimensionen automatisch zu verändern. Das wollen wir aber nicht, daher bleibt der Inhalt leer</td>
		</tr><tr>
			<td>submit</td>
			<td>Send file(s)</td>
			<td>Der Hochlade-Button. Alle Browser schicken auch diesen als Wert bei einem POST mit</td>
		</tr></table>

		<br />
		Die einzige <u>Besonderheit</u> ist die Einstellung <b>NoEmptyBracketsAdd</b>: bisher ist sie nur für <a class="out" href="http://www.imagevenue.com">ImageVenue</a> nötig. Alle Angaben in der Sektion <b>AdditionalPostValues</b> sind so gewählt, wie sie bei einem normalen POST auch stattfinden würden.
		<br /><br />
		Zum Testen (und Herausfinden) eignet sich hier hervorragend der <a class="out" href="http://www.mozilla-europe.org/de/products/firefox/">Mozilla Firefox</a> mit der Erweiterung <a class="out" href="https://addons.mozilla.org/de/firefox/addon/3829">Live HTTP Headers</a>:
		<ul>
			<li>Man macht auf der entsprechenden Hoster-Hochlade-Seite alle Angaben, wählt die Datei aus, aktiviert die Erweiterung und lädt das ganze hoch.</li>
			<li>Mit Hilfe der aufgezeicheten <b>Header</b> und <b>POST-Daten</b> kann man recht schnell erkennen, welche Werte z.B. bei der Sektion <b>AdditionalPostValues</b> mit welchem Inhalt eingetragen werden müssten. Die Sache hat nur einen Haken: da der Inhalt der hochzladenden Datei ebenfalls als Inhalt eines Wertes gilt, bricht das Plugin die Anzeige der Erweiterung meist aufgrund eines bestimmten ASCII-Wertes innerhalb dieser Datei ab. Daher empfiehlt es sich, statt einer <i>echten</i> JPG-Datei eine Textdatei mit wenig Inhalt (drei Wörter reichen) zu nehmen, diese in z.B. <b>TEST.JPG</b> umzubenennen und diese dann hochzuladen. Somit bekommt man erstmal eine Anzeige aller tatsächlich verschickten Werte samt Inhalt.</li>
			<li>Die Antwortseite des Servers ist dann natürlich unbrauchbar, da er erkannt haben wird, dass es sich <u>nicht</u> um ein Bild handelt. Also das ganze nochmal - nur jetzt mit einer <u>echten</u> JPG-Datei (diesmal interessieren uns die Header auch nicht mehr).</li>
			<li>Aus der Struktur der Antwortdatei müssen wir bloß noch erkennen, wie wir einen sinnvollen regulären Ausdruck für <b>ResultRegExpPattern</b> erstellen können.</li>
			<li>Und dann das ganze nochmal für Extremfälle, wie z.B. JPG-Bilder mit sehr großen Dimensionen, zu kleinen Dimensionen und evt. nochmal die umbenannte Textdatei, damit wir auch für <b>ResultRegExpNeg</b> einen regulären Ausdruck erstellen können.</li>
		</ul><br />
		Beispielsweise ergibt die Erweiterung bei diesem Hoster folgende Ausgabe (mit einer Textdatei als JPG umbenannt):<pre>
http://www.imagevenue.com/upload.php

POST /upload.php HTTP/1.1
Host: www.imagevenue.com
User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.0.9) Gecko/20061206 Firefox/1.5.0.9
Accept: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5
Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3
Accept-Encoding: gzip,deflate
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
Keep-Alive: 300
Connection: keep-alive
Referer: http://www.imagevenue.com/
Cookie: __qca=1194716692-81631305-42196473; __utmz=122915731.1199815563.1.1.utmccn=(direct)|utmcsr=(direct)|utmcmd=(none); ctriv2=1; __utma=122915731.1818741812.1199815563.1200841709.1203268321.4; __utmb=122915731; __utmc=122915731; __qcb=764999590
Content-Type: multipart/form-data; boundary=---------------------------41184676334
Content-Length: 1201
-----------------------------41184676334
Content-Disposition: form-data; name="<b>userfile[]</b>"; filename="<b>Neu Textdokument.jpg</b>"
Content-Type: image/jpeg

fdsafdsa
-----------------------------41184676334
Content-Disposition: form-data; name="userfile[]"; filename=""
Content-Type: application/octet-stream


-----------------------------41184676334
Content-Disposition: form-data; name="userfile[]"; filename=""
Content-Type: application/octet-stream


-----------------------------41184676334
Content-Disposition: form-data; name="userfile[]"; filename=""
Content-Type: application/octet-stream


-----------------------------41184676334
Content-Disposition: form-data; name="userfile[]"; filename=""
Content-Type: application/octet-stream


-----------------------------41184676334
Content-Disposition: form-data; name="<b>imgcontent</b>"

<b>safe</b>
-----------------------------41184676334
Content-Disposition: form-data; name="<b>MAX_FILE_SIZE</b>"


-----------------------------41184676334
Content-Disposition: form-data; name="<b>action</b>"

<b>1</b>
-----------------------------41184676334
Content-Disposition: form-data; name="<b>img_resize</b>"


-----------------------------41184676334--

HTTP/1.x 200 OK
Date: Sun, 17 Feb 2008 17:06:36 GMT
Server: Apache/2.0.52 (CentOS)
X-Powered-By: PHP/4.3.9
Set-Cookie: tsctr=2641161400; expires=Tue, 18-Mar-2008 17:06:36 GMT; path=/; domain=.imagevenue.com
Content-Length: 3272
Connection: close
Content-Type: text/html; charset=UTF-8</pre>
	</div>

	<h1><a name="h05"><strong>3.<em>b</em></strong> Beispiel: Rapidshare</a></h1>
	<div>

		<ins>Anmerkung: inzwischen wird Rapidshare nicht mehr unterstützt, daher kann das folgende Beispiel nicht mehr nachvollzogen werden.</ins>
		<br/><br/>

		<a class="out" href="http://www.rapidshare.com">RapidShare</a> hat einen etwas gehobeneren Anspruch. Die Einstellungen zu <b>Version</b>, <b>Icon</b>, <b>DisplayName</b> und <b>MaxFileSize</b> lasse ich an dieser Stelle weg, ihr Zweck und ihre Funktion ist genau dieselbe wie die im <a href="#h04">Beispiel ImageVenue</a>. Auch die <b>AdditionalPostValues</b>-Sektion lasse ich weg, die Werte hier sind ebenso ermittelt worden und haben keinen tieferen Sinn:
		<br /><br />
		<table><thead><tr>
			<th>Schlüssel</th>
			<th>Wert</th>
			<th>Funktion / Aufgabe</th>
		</tr></thead><tr>
			<td class="col" colspan="3">Sektion [General]</td>
		</tr><tr>
			<td>PreloadURL</td>
			<td>http://www.rapidshare.com/</td>
			<td>Vor dem eigentlichen Hochladen müssen wir erst die Seite besuchen, von der wir im Browser hochladen müssen. Das ist deshalb nötig, weil die Hochladeadresse jedesmal anders aussehen könnten. Also rufen wir die Seite auf und durchsuchen sie nach der entscheidenden dynamischen Stelle</td>
		</tr><tr>
			<td>PreloadRegExpPattern</td>
			<td>/form name="ul" method="post" action="([^"]+)" enctype="multipart/ism</td>
			<td>Was soll denn in der mit <b>PreloadURL</b> bekommenen Seite gesucht werden? Konkret suchen wir den <b>&lt;FORM .../&gt;</b>-Tag und dadrin seinen Wert für die Eigenschaft <b>ACTION</b></td>
		</tr><tr>
			<td>TargetUploadURL</td>
			<td>$1</td>
			<td>Die Adresse, an die der POST gesendet wird. Hier können wir Ergebnisse aus <b>PreloadRegExpPattern</b> eintragen - also machen wir das auch: mit <b>$1</b> wählen wir das Ergebnis des ersten Klammernpaares des regulären Ausdrucks</td>
		</tr><tr>
			<td>TargetFieldName</td>
			<td>filecontent</td>
			<td>Dies ist der Wert für die hochzuladende Datei. Erkennbar im Quellcode an dem <b>&lt;INPUT TYPE=&quot;file&quot; ... /&gt;</b>-Tag</td>
		</tr><tr>
			<td>ResultRegExpPattern</td>
			<td>/Download-Link #1[^ ]+ href="([^"]+)"/ism</td>
			<td>Der Link, den wir in der Antwortdatei des Servers suchen. Dieser reguläre Ausdruck sucht also nach einem <b>&lt;A ...&gt;</b>-Tag, der vorher den Wortlaut &quot;<i>Download-Link</i>&quot; hatte</td>
		</tr><tr>
			<td>ResultRegExpSubst</td>
			<td>[url]$1[/url]</td>
			<td>Das Ergebnis des ersten (eigentlich einzigen) Klammernpaares unter <b>ResultRegExpPattern</b> soll zurückgegeben und gleich als BBCode dargestellt werden, also der Substitionstext <b>$1</b> umschlossen von den BBCode-Tags <b>[URL] ... [/URL]</b></td>
		</tr></table>

		<br />
		<u>Besonderheiten</u> hier wären:
		<ul>
			<li>die vorher zu ladende Seite,</li>
			<li>das setzen der damit herausgefundenen Hochladeadresse und</li>
			<li>der Umstand, dass <a class="out" href="http://www.rapidshare.com">RapidShare</a> keinen BBCode-Link liefert.</li>
		</ul><br />
		Die unter dem <a href="#h04">Beispiel ImageVenue</a> erwähnte Erweiterung liefert bei diesem Hoster folgende Ausgabe (mit dem Hochladen einer Textdatei):<pre>
http://rs372l3.rapidshare.com/cgi-bin/upload.cgi?rsuploadid=143789340485944512

POST /cgi-bin/upload.cgi?rsuploadid=143789340485944512 HTTP/1.1
Host: rs372l3.rapidshare.com
User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.0.9) Gecko/20061206 Firefox/1.5.0.9
Accept: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5
Accept-Language: de-de,de;q=0.8,en-us;q=0.5,en;q=0.3
Accept-Encoding: gzip,deflate
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
Keep-Alive: 300
Connection: keep-alive
Referer: http://www.rapidshare.com/
Content-Type: multipart/form-data; boundary=---------------------------265001916915724
Content-Length: 414
-----------------------------265001916915724
Content-Disposition: form-data; name="<b>mirror</b>"

<b>on</b>
-----------------------------265001916915724
Content-Disposition: form-data; name="<b>german</b>"

<b>1</b>
-----------------------------265001916915724
Content-Disposition: form-data; name="<b>filecontent</b>"; filename="<b>Neu Textdokument.jpg</b>"
Content-Type: image/jpeg

fdsafdsa
-----------------------------265001916915724--

HTTP/1.x 200 OK
P3P: CP="ALL DSP COR CURa ADMa DEVa TAIa PSAa PSDa IVAa IVDa CONa TELa OUR STP UNI NAV STA PRE"
Date: Sun, 17 Feb 2008 17:12:42 GMT
Connection: close
Accept-Ranges: bytes
Content-Type: text/html; charset=ISO-8859-1
Cache-Control: no-cache
Content-Length: 5867</pre>
	</div>

	<br />
	<div style="background-color: #A0FFA0;">
		Letzte Aktualisierung: 2012-10-14
	</div><br />

</div>



{/literal}
