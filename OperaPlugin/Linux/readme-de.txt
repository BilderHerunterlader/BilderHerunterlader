Zuerst schauen ob der Ordner ~/.opera/menu/ existiert, falls nicht, diesen erstellen. Dann schauen ob im Ordner ~/.opera/menu/ die Datei standard_menu.ini vorhanden ist. Wenn nicht, dann einfach die in der Zip-Datei beiliegende standard_menu.ini in den Ordner kopieren.

In der standard_menu.ini muss dann folgende Zeile in der Sektion [Document Popup Menu] hinzugefuegt werden:
Item, "Dateien mit BH herunterladen" = Execute program, "/home/benutzername/BHTransmit","-u %u"

"/home/benutzername/BHTransmit" muss nat�rlich angepasst werden, auf den Ordner, wo ihr BHTransmit entpackt habt. Und ihr m�sst den absoluten Pfad angeben!

Hinweise:
- Die diesem Archiv beigelegte standard_menu.ini enth�lt die oben genannte Zeile noch nicht!

- Damit das Herunterladen von Seiten, die eine Anmeldung erfordern, funktioniert, m�ssen Cookies in den Einstellungen vom BilderHerunterlader aktiviert werden.

- BHTransmit muss eventuell noch Ausf�hr-Rechte gew�hrt werden.
