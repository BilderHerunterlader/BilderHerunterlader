<pre>
Der BH lässt sich auch von eines USB-Stick aus betreiben. Vorraussetzung ist aber immer, dass auf den Computern das JRE installiert ist oder das ein portables JRE, wie z.B. jPortable, verfügbar ist.

Um BH von einem USB-Stick aus zu betreiben, muss man im Programmordner eine Textdatei anlegen und die Datei muss folgenden Namen haben: directories.properties
In dieser Textdatei kann man die Verzeichnisse angeben, in denen die Datenbanken, Einstellungen und alles weitere gespeichert werden sollen. Diese Daten werden normalerweise im Benutzerprofil abgespeichert, was man nicht möchte, wenn man das Programm auf einem USB-Stick hat.
Im Programmordner sollte bereits eine "directories.properties.example" Datei liegen, welche auf "directories.properties" umbenannt werden kann. Danach kann die Datei bearbeitet werden.

Die directories.properties Datei muss etwa so aussehen:
</pre>
<div class="code">
<pre>
DatabasePath=data/
SettingsPath=data/
DownloadLogPath=data/
LogsPath=data/
DownloadPath=downloads/
</pre>
</div>
<pre>
Im obigen Beispiel würden die Datenbanken, Einstellungen und Logs im Programmordner im Unterordner "data" gespeichert werden. Und heruntergeladene Dateien im Unterordner "downloads".

In der Textdatei kann man auch einzelne Angaben weglassen, dann würde diese Daten einfach wieder im Benutzerprofil gespeichert.
Im obigen Beispiel sind alle möglichen Angaben aufgelistet. Es gibt nur diese 5 Angaben.

Will man das Programm von einem USB-Stick benutzen, muss man natürlich die Verzeichnisse, wie im obigen Beispiel, relativ zum Programmverzeichnis angeben.
Es wäre allerdings auch möglich absolute Verzeichnisse anzugeben.
</pre>
