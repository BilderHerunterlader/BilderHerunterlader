<pre>
<b>Werden weitere Hoster unterstützt werden?</b>
Ja, falls es technisch möglich ist. Schicke eine E-Mail falls du einen Hoster hast, der noch nicht unterstützt wird.
Und schicke auch gleich Links zu Beispielen mit. Du kannst aber auch selber ein Regel erstellen und mir dann die XML-Datei davon schicken.

<b>Das Programm hat einen Fehler verursacht. Was soll ich tun?</b>
Schreib mir eine E-Mail mit einer möglichst genauen Beschreibung des Fehler.
Als Anhang solltest du das Logfile (BH.log) mitschicken. Das Logfile findest du in deinem Profil-Ordner im Unterordner .BH.

<b>Wo befindet sich mein Profil-Ordner?</b>
Bei Windows findet man den Ordner indem man im Explorer %userprofile%\.BH eingibt.
Bei Linux findet man den Ordner über ~/.BH

<b>Wofür sind all diese Dateien im Profil-Ordner?</b>
<i>BH.lock</i> -> Diese Datei wird vom Programm gesperrt, damit nicht mehrere Instanzen des Programms gestartet werden können.
<i>BH.log</i> -> Log-Datei in der Fehlermeldungen und andere Meldungen gespeichert werden.
Für jeden Tag wird eine Log-Datei angelegt. (z.B. BH.log.2007-05-20)
<i>BH-Downloads.db4o</i> -> Datenbank in der die Warteschlange gespeichert wird
<i>BH-Keywords.db4o</i> -> Datenbank in der die Schlüsselwörter gespeichert werden
<i>BH-Logs.txt</i> -> Diese Datei enthält Informationen über die heruntergeladenen Bilder
<i>settings.xml</i> -> Diese Datei enthält die Einstellungen
<i>port.txt</i> -> Darin wird der Port gespeichert auf dem das Programm Daten entgegen nimmt (von Firefox/Seamonkey-Extension, IE-Plugin)

<b>Ein Bild wird zwar heruntergeladen, aber die Fortschrittanzeige bewegt sich nicht. Wieso?</b>
Das liegt daran, das bei einigen Servern die Grösse des Bildes nicht ermittelt werden kann. Und ohne diese Grösse kann die Fortschrittanzeige nichts sinnvolles anzeigen, deshalb bewegt sie sich nicht.

<b>Beim Starten mit JRE6 tritt ein Problem mit dem SystemTray auf, was tun?</b>
Das Kommandozeilen-Argument "-noTray" startet das Programm mit JRE6 ohne den SystemTray zu benutzen.

<b>Gibt es noch andere Kommandozeilen-Argumente?</b>
Ja hier ist die volle Liste:
-noTray -> BH startet ohne System-Tray
-noDebug -> Fehler werden auf der Konsole ausgegeben, anstatt im Debug-Fenster

Und diese beiden Argumente sind nur für das automatisch generieren der Setups:
-version -> Gibt die Version aus (z.B. 1.2.0)
-versionNumber -> Gibt die Version-Nummer aus (z.B. 120)
</pre>