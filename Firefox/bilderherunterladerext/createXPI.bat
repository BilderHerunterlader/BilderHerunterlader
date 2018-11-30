::Get version
SET /P version=Please enter the version:
ECHO Version: %version%

::Compress chrome to jar
cd chrome
IF EXIST bilderherunterlader.jar (
	del bilderherunterlader.jar
)
"%programfiles%\7-zip\7z.exe" a -xr@excludelist.txt -tzip bilderherunterlader.jar * -r

::Compress xpi
cd..
"%programfiles%\7-zip\7z.exe" a -xr@excludelist.txt -tzip bilderherunterlader-v%version%.xpi * -r

::Delete chrome jar
del chrome\bilderherunterlader.jar
