::Version aus Datei einlesen
for /f "tokens=1" %%i in (version.txt) do set version=%%i

::version einsetzen und zip datei erstellen
cd Linux
"%programfiles%\7-zip\7z.exe" a -xr@..\excludelist.txt -tzip -r ..\OperaPluginLinuxV%version%.zip *
cd ..

cd Windows
"%programfiles%\7-zip\7z.exe" a -xr@..\excludelist.txt -tzip -r ..\OperaPluginWindowsV%version%.zip *
cd ..
