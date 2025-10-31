@ECHO OFF

::Version in Datei schreiben
java -jar BH.jar -version > v.txt

::Datei einlesen
for /f "tokens=1" %%i in (v.txt) do set version=%%i
::Dateie loeschen
del v.txt

::version einsetzen und zip datei erstellen
"%programfiles%\7-zip\7z.exe" a -xr@excludelist.txt -tzip BilderHerunterlader-%version%-Binary.zip hosts\*.class -r
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip lib\*.jar -r
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip updater\*.jar -r
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip updater\*.exe -r
"%programfiles%\7-zip\7z.exe" a -xr@excludelist.txt -tzip BilderHerunterlader-%version%-Binary.zip rules\*.xml -r
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip BH.exe
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip BH.jar
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip BHIcon.ico
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip license.txt
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip directories.properties.example
"%programfiles%\7-zip\7z.exe" a -tzip BilderHerunterlader-%version%-Binary.zip CHANGELOG.md
