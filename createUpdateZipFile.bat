@ECHO OFF

::Version in Datei schreiben
java -jar BH.jar -version > v.txt

::Datei einlesen
for /f "tokens=1" %%i in (v.txt) do set version=%%i
::Dateie loeschen
del v.txt

::version einsetzen und zip datei erstellen
"%programfiles%\7-zip\7z.exe" a -tzip BH-%version%.zip lib\*.jar -r
"%programfiles%\7-zip\7z.exe" a -tzip BH-%version%.zip BH.jar
"%programfiles%\7-zip\7z.exe" a -tzip BH-%version%.zip BH.exe
