::Get version
SET /P version=Please enter the version:
ECHO Version: %version%

cd chromewebext

::Compress zip
"%programfiles%\7-zip\7z.exe" a -tzip ..\bilderherunterladerchromewebext-v%version%.zip * -r
