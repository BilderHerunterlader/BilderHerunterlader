;OperaPlugin install script
;Based upon example scripts from Joost Verburg

;--------------------------------
;Include Modern UI
	
	!include "MUI.nsh"
	!include Library.nsh
	
;--------------------------------
;General
	
	SetCompressor /SOLID lzma
	
	AutoCloseWindow false
	ShowInstDetails show
	ShowUninstDetails show
	
	;Name and file
	!define PROGRAM_NAME "BilderHerunterlader IE-Plugin" ;Name
	!define PROGRAM_VERSION "4.0" ;Version
	Name "${PROGRAM_NAME} ${PROGRAM_VERSION}"
	OutFile "BHOperaPluginSetupv${PROGRAM_VERSION}.exe"
	
	;Default installation folder
	InstallDir "$PROGRAMFILES\BilderHerunterlader\OperaPlugin"
	
	;Get installation folder from registry if available
	InstallDirRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\OperaBHTransmit.exe" "Path"
	
	;Request application privileges for Windows Vista / 7
	RequestExecutionLevel admin
	
;--------------------------------
;Variables
	
;--------------------------------
;Interface Settings
	
	!define MUI_ABORTWARNING
	
;--------------------------------
;Language Selection Dialog Settings
	
	
	
;--------------------------------
;Pages
	
	;Install Pages
	!insertmacro MUI_PAGE_COMPONENTS
	!insertmacro MUI_PAGE_DIRECTORY
	!insertmacro MUI_PAGE_INSTFILES
	
	;Uninstall Pages
	!insertmacro MUI_UNPAGE_CONFIRM
	!insertmacro MUI_UNPAGE_INSTFILES
	
;--------------------------------
;Languages
	
	!insertmacro MUI_LANGUAGE "English" # first language is the default language
	!include "languages\english.nsh"
	
	!insertmacro MUI_LANGUAGE "German"
	!include "languages\german.nsh"
	
;--------------------------------
;Reserve Files
	
	;These files should be inserted before other files in the data block
	;Keep these lines before any File command
	;Only for solid compression (by default, solid compression is enabled for BZIP2 and LZMA)
	
	!insertmacro MUI_RESERVEFILE_LANGDLL
	
;--------------------------------
;Installer Sections

Section !$(SecMainName) SecMain
	SectionIn RO
	SetOutPath "$INSTDIR"
	
	SetOverwrite on
	File "BHTransmit.exe"
	File "WS2_32.DLL"
	File "ws2help.dll"
	File "standard_menu.ini"
	
	SetOutPath "$INSTDIR"
	
	WriteINIStr "$INSTDIR\standard_menu.ini" "Document Popup Menu" 'Item, "$(BHMenuExt)"' 'Execute program, "C:\BHTransmit.exe","-u %u"'
	
	IfFileExists $WINDIR\notepad.exe 0 +2
	MessageBox MB_OK "notepad is installed"
	
	;Store installation folder
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\BHTransmit.exe" "" "$INSTDIR\OperaBHTransmit.exe"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\BHTransmit.exe" "Path" "$INSTDIR"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "DisplayName" "${PROGRAM_NAME} ${PROGRAM_VERSION}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "UninstallString" '"$INSTDIR\BHOperaPluginUninstall.exe"'
	
	;Create uninstaller
	WriteUninstaller "$INSTDIR\BHOperaPluginUninstall.exe"
	
SectionEnd

;--------------------------------
;Installer Functions

Function .onInit

FunctionEnd

;--------------------------------
;Descriptions
	
	;Assign language strings to sections
	!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMain} $(SecMainDesc)
	!insertmacro MUI_FUNCTION_DESCRIPTION_END
	
;--------------------------------
;Uninstaller Section

Section "Uninstall"
	;Delete Files and Folders in Installation-Directory
	Delete "$INSTDIR\BHTransmit.exe"
	Delete "$INSTDIR\BHIEScript.htm"
	Delete "$INSTDIR\WS2_32.DLL"
	Delete "$INSTDIR\ws2help.dll"
	Delete "$INSTDIR\standard_menu.ini"
	Delete "$INSTDIR\Uninstall.exe"
	
	RMDir "$INSTDIR"
	
	;Delete RegistryKeys
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}"
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\OperaBHTransmit.exe"
	
SectionEnd

;--------------------------------
;Uninstaller Functions

Function un.onInit

FunctionEnd
