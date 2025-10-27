::Get version
SET /P version=Please enter the version:
ECHO Version: %version%

if exist edgewebexttemp rmdir edgewebexttemp /S /Q
mkdir edgewebexttemp
xcopy chromewebext edgewebexttemp /S /E
copy edgewebext\manifest.json edgewebexttemp /Y

cd edgewebexttemp

::Compress zip
"%programfiles%\7-zip\7z.exe" a -tzip ..\bilderherunterladeredgewebext-v%version%.zip * -r

cd ..

rmdir edgewebexttemp /S /Q
