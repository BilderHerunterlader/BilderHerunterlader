cd ..
::Version in Datei schreiben
java -jar BilderHerunterlader2\BH.jar -versionNumber > vx.txt

::Datei einlesen
for /f "tokens=1" %%i in (vx.txt) do set version=%%i
::Dateie loeschen
del vx.txt

::version einsetzen und zip datei erstellen
"%programfiles%\7-zip\7z.exe" a -xr@BilderHerunterlader2\excludelist.txt -tzip BilderHerunterlader2\BilderHerunterlader%version%Source.zip BilderHerunterlader2\* -r
