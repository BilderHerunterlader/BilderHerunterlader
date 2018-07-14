::Version in Datei schreiben
java -jar BH.jar -versionNumber > vy.txt
java -jar BH.jar -version > vz.txt

::Datei einlesen
for /f "tokens=1" %%i in (vy.txt) do set version=%%i
for /f "tokens=1" %%o in (vz.txt) do set versionx=%%o
::Dateie loeschen
del vy.txt
del vz.txt

IF EXIST Setup\BH%version%Setup.nsi GOTO compile

:template
setup\sed\sed.exe "s/TemplateY/%versionx%/g" Setup\BHTemplateSetup.nsi >> BHTemplateSetupX.nsi
setup\sed\sed.exe "s/TemplateX/%version%/g" BHTemplateSetupX.nsi >> Setup\BH%version%Setup.nsi
del BHTemplateSetupX.nsi

:compile
if exist "%PROGRAMFILES(X86)%\NSIS\makensisw.exe" goto x86

"%programfiles%\NSIS\makensisw.exe" Setup\BH%version%Setup.nsi
goto continue

:x86
"%PROGRAMFILES(X86)%\NSIS\makensisw.exe" Setup\BH%version%Setup.nsi

:continue

del Setup\BH%version%Setup.nsi

move Setup\BilderHerunterlader%version%Setup.exe .
