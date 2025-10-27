::Get version
SET /P version=Please enter the version:
ECHO Version: %version%

if exist operawebexttemp rmdir operawebexttemp /S /Q
mkdir operawebexttemp
xcopy chromewebext operawebexttemp /S /E
copy operawebext\manifest.json operawebexttemp /Y

cd operawebexttemp

::Compress zip
"%programfiles%\7-zip\7z.exe" a -tzip ..\bilderherunterladeroperawebext-v%version%.zip * -r

cd ..

rmdir operawebexttemp /S /Q
