Zuerst schauen ob im Ordner %appdata%\Opera\Opera\profile\menu\ die Datei standard_menu.ini vorhanden ist. Wenn nicht, dann einfach die in der Zip-Datei beiliegende standard_menu.ini in den Ordner kopieren.

In der standard_menu.ini muss dann folgende Zeile in der Sektion [Document Popup Menu] hinzugefuegt werden:
Item, "Dateien mit BH herunterladen" = Execute program, "C:\BHTransmit.exe","-u %u"

"C:\BHTransmit.exe" muss natürlich angepasst werden, ausser BHTransmit.exe liegt wirklich direkt in C:\

Hinweise:
- Die diesem Archiv beigelegte standard_menu.ini enthält die oben genannte Zeile noch nicht!

- Damit das Herunterladen von Seiten, die eine Anmeldung erfordern, funktioniert, müssen Cookies in den Einstellungen vom BilderHerunterlader aktiviert werden.
