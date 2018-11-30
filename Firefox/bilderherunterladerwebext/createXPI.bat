::Get version
SET /P version=Please enter the version:
ECHO Version: %version%

::Compress zip
"%programfiles%\7-zip\7z.exe" a -xr@excludelist.txt -tzip bilderherunterladerwebext-v%version%.xpi * -r
