<pre>
<b><a name="rules" class="noStyle">Regeln</a></b>
In BH kann jeder eigene Regeln definieren. Es ist also möglich selber die Unterstützung von anderen Hostern zu ermöglichen. Allerdings muss man sich mit Regulären Ausdrücken auskennen.
</pre>
<div class="download">
<pre>
<b>Möglichkeit 1</b>
Wenn es möglich ist in der Container-URL oder der Thumbnail-URL etwas zu ersetzen um an die URL des eigentlichen Bildes zu kommen, dann kann man folgendes tun:

Man erstellt eine neue Regel. Als erstes muss das Muster der Container-URL als Regulärer Ausdruck eingegeben werden, damit die Regel weiss für welche URLs sie zuständig ist.
Nun ist es möglich mehrere Ersetzungen hintereinander durchzuführen.
Dabei wird das Resultat von jeder Ersetzung für die nächste Verwendet, ausser die erste Ersetzung, die verwendet die Container-URL oder Thumbnail-URL.

<b>Beispiel</b>
Die Thumbnail-URL ist: http://img411.imageshack.us/img411/5221/935871ia1.th.jpg
Das Suchmuster sieht dann wie folgt aus:
http://(img[0-9]*)\.([a-zA-Z0-9.]*)/([a-zA-Z0-9]*)/([a-zA-Z0-9_%]*)/([a-zA-Z0-9-_%]*)\.th\.([a-zA-Z]*)
Und das kann gleich ersetzt werden mit folgendem Ersetz-Muster:
http://$1.$2/$3/$4/$5.$6
Wir erhalten also die URL:
http://img411.imageshack.us/img411/5221/935871ia1.jpg
</pre>
</div>

<div class="download">
<pre>
<b>Möglichkeit 2</b>
Man erstellt eine neue Regel. Als erstes muss das Muster der Container-URL als Regulärer Ausdruck eingegeben werden, damit die Regel weiss für welche URLs sie zuständig ist.
Bei der Auswahl wählt man dann die zweite Möglichkeit an.
Falls die URL nicht aus der Container-URL ausfindig gemacht werden kann, so hat man die Möglichkeit den Quelltext der Seite herunterladen zu lassen und kann darin Suchen und Ersetzen.
Auch hier kann man mehrere Suchen hintereinander schalten. Das Resultat der Suche, falls es nicht die letzte ist, ist die Position des Fundes im Quelltext. Die nächste Suche verwendet für die Suche dann das Resultat der letzten also die Position und sucht von dort aus weiter. Die letzte Suche nimmt das Resultat und ersetzt es.

Beim Ersetz-Muster kann man zwei Variablen benutzen:
Als URL nehmen wir mal an: http://bla.irgendwas.net/ordner/bild.jpg
$SRV wird vorher durch die Domain ersetzt. Also wird daraus http://bla.irgendwas.net/
$URL wird vorher durch die URL ersetzt. Also wird daraus http://bla.irgendwas.net/ordner/

Im Such-Muster kann man durch setzen von Klammern diese als Referenz beim Ersetzen verwenden.
Wir nehmen als Quelltext-Ausschnitt mal an: SRC="aAfkjfp01fo1i-28074/loc164/54394_NoraTschirner_FCVenusPremiere_01.jpg">
Im Such-Muster geben wir nun an: SRC="(.*)">
Für diese Klammer kann man nun die Referenz $1 benutzen.
Beim ersetzen wird also $1 durch den Inhalt des SRC-Attributs ersetzt.
Man kann mehrere Klammern setzen, die Referenzen werden dann einfach durchnummeriert.
Also erste Klammer ist $1, zweite Klammer ist $2 und so weiter.

<b>Beispiel</b>
Der Quelltext wird also zuerst heruntergeladen.
Als URL nehmen wir mal: http://img7.imagevenue.com/img.php?loc=loc164&amp;image=54394_NoraTschirner_FCVenusPremiere_01.jpg
Hier ein Ausschnitt aus dem Quelltext von Imagevenue:
</pre>
<div class="code">
	{include file="bilderherunterlader/rules_html.tpl" assign="exampleHTML"}
	{geshi geshiInstance=$geshiInstance source=$exampleHTML language="html4strict"}
</div>
<pre>
Das erste Such-Muster sieht so aus:
id=&quot;thepic&quot;
Die Suche liefert die Position wo der Ausdruck gefunden wurde.
Nun sind wir aber noch nicht am richtigen Ort, da wir ja das Bild wollen, also das was in SRC="" steht.
Also benötigen wir ein zweites Such-Muster das so aussieht:
SRC=&quot;(.*)&quot;&gt;
Da wir nun das haben was wir wollen, wird im Programm intern das Ergebnis in eine Variable geschrieben.
In der Variable steht dann also folgendes: SRC="aAfkjfp01fo1i-28074/loc164/54394_NoraTschirner_FCVenusPremiere_01.jpg">

Nun müssen wir der Regel noch ein Ersetz-Muster angeben.
Für unser Beispiel benötigen wir $SRV$1
Das Programm ersetzt also die Variable mit dem Muster und wir bekommen die URL:
http://img7.imagevenue.com/aAfkjfp01fo1i-28074/loc164/54394_NoraTschirner_FCVenusPremiere_01.jpg
</pre>
</div>
<pre>
Weiter gibt es noch die Möglichkeit den Dateinamen zu korrigieren. Das ganze funktioniert genau gleich wie es bei Möglichkeit 1 der Fall ist. Nur wird zum Suchen nicht die URL verwendet sondern nur der Dateiname der Bild-URL.
Ist die URL also http://img7.imagevenue.com/aAfkjfp01fo1i-28074/loc164/54394_NoraTschirner_FCVenusPremiere_01.jpg
so wird dann davon nur 54394_NoraTschirner_FCVenusPremiere_01.jpg verwendet.

<b>Regeln mit anderen teilen</b>
Falls du eine Regel erstellt hast und diese anderen Benutzern des Programs zur Verfügung stellen willst, so kannst du mir diese zusenden. Ich werde die Regel prüfen und dann über die Update-Funktion zur Verfügung stellen.

<b>XML-Dateien der Regeln</b>
Die Regeln werden in XML-Dateien gespeichert und zwar im Unterordner "rules" des Programmordners.
Der Aufbau der Dateien ist wie folgt:

<b>Direkt verlinkte Bilder</b>
Falls es nötig sein sollte andere direkt verlinkte Bilder oder sonstige Dateien runterzuladen, kann man mit Regulären Ausdrücken dafür sorgen.
Im Programmordner im Unterordner Hosts befinden sich die Hoster-Klassen. Die Datei HostzDefaultImages.class muss vorhanden sein um direkt verlinkte Bilder zu laden.
Diese Klasse erkennt standardmässig aber nur gewisse URLs. Um nun weitere anzugeben muss in diesem Ordner eine Text-Datei mit dem Namen HostzDefaultImages.txt angelegt werden.
In dieser Datei muss dann pro Zeile ein Regulärer Ausdruck angegeben werden.
Hier mal ein Beispiel wie es aussehen könnte:
<div class="code">^http:\/\/.*\/.*\.(gif|jpg|jpeg|jpe|png|tif|tiff)
^http:\/\/(.*\.|).*\..*\/.*\?page=Attachment&attachmentID=[0-9]+&h=[0-9a-z]+
</div>
Der erste Ausruck erlaubt Dateien mit gewissen Endungen herunterzuladen.
Der zweite Ausdruck erlaubt das herunterladen von Attachements in der Boardsoftware WBB.
Gross- und Kleinschreibung wird an dieser Stelle des Programms nicht beachtet.

<b><a name="hostplugins" class="noStyle">Hoster-Klassen / Hoster-Plugins</a></b>
Falls diese Möglichkeiten nicht reichen und man Java programmieren kann, so kann man eigene Klassen schreiben die als Hoster-Plugins funktionieren.
Zum kompilieren der Klassen benötigt man den Sourcecode des Programmes.
Die Klasse muss nämlich von ch.supertomcat.bh.hoster.Host ableiten und muss das Interface ch.supertomcat.bh.hoster.IHoster implementieren.
</pre>