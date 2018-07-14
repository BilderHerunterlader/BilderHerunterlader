Zuerst schauen ob der Ordner ~/.opera/menu/ existiert, falls nicht, diesen erstellen. Dann schauen ob im Ordner ~/.opera/menu/ die Datei standard_menu.ini vorhanden ist. Wenn nicht, dann einfach die in der Zip-Datei beiliegende standard_menu.ini in den Ordner kopieren.

In der standard_menu.ini muss dann folgende Zeile in der Sektion [Document Popup Menu] hinzugefuegt werden:
Item, "Dateien mit BH herunterladen" = Execute program, "/home/benutzername/BHTransmit","-u %u"

"/home/benutzername/BHTransmit" muss natürlich angepasst werden, auf den Ordner, wo ihr BHTransmit entpackt habt. Und ihr müsst den absoluten Pfad angeben!

Hinweise:
- Die diesem Archiv beigelegte standard_menu.ini enthält die oben genannte Zeile noch nicht!

- Damit das Herunterladen von Seiten, die eine Anmeldung erfordern, funktioniert, müssen Cookies in den Einstellungen vom BilderHerunterlader aktiviert werden.

- BHTransmit muss eventuell noch Ausführ-Rechte gewährt werden.
