@ECHO OFF
echo REGEDIT4 > bhie.reg
echo. >> bhie.reg
echo [HKEY_CURRENT_USER\Software\Microsoft\Internet Explorer\MenuExt\Dateien mit BH herunterladen] >> bhie.reg
echo @="%cd%\BHIEScript.htm" > bhie.tmp
sed "s/\\/\\\\/g" bhie.tmp >> bhie.reg
echo "contexts"=hex:f3,00,00,00 >> bhie.reg
del bhie.tmp
regedit /s bhie.reg
del bhie.reg
