@ECHO OFF
echo REGEDIT4 > bhie.reg
echo. >> bhie.reg
echo [-HKEY_CURRENT_USER\Software\Microsoft\Internet Explorer\MenuExt\Dateien mit BH herunterladen] >> bhie.reg
regedit /s bhie.reg
del bhie.reg
