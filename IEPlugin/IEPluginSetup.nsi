;IEPlugin install script
;Based upon example scripts from Joost Verburg

;--------------------------------
;Include Modern UI
	
	!include "MUI.nsh"
	!include Library.nsh
	!include "Registry.nsh"
	
;--------------------------------
;General
	
	SetCompressor /SOLID lzma
	
	AutoCloseWindow false
	ShowInstDetails show
	ShowUninstDetails show
	
	;Name and file
	!define PROGRAM_NAME "BilderHerunterlader IE-Plugin" ;Name
	!define PROGRAM_VERSION "9.0" ;Version
	Name "${PROGRAM_NAME} ${PROGRAM_VERSION}"
	OutFile "BHIEPluginSetupv${PROGRAM_VERSION}.exe"
	
	;Default installation folder
	InstallDir "$PROGRAMFILES\BilderHerunterlader\IEPlugin"
	
	;Get installation folder from registry if available
	InstallDirRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\BHIEPlugin" "Path"
	
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
	File "BHIEScript.htm"
	File "WS2_32.DLL"
	File "ws2help.dll"
	
	SetOutPath "$INSTDIR"
	
	; Example registry plugin
	; ${registry::CreateKey} "[fullpath]" $var
	; $var == 1   # [fullpath] already exists
	; $var == 0   # [fullpath] successfully created
	; $var == -1  # error
	
	; Create Regristry Keys
	${registry::CreateKey} "[HKEY_CURRENT_USER\Software\Microsoft\Internet Explorer\MenuExt\$(BHMenuExt)]" $0
	WriteRegStr HKCU "Software\Microsoft\Internet Explorer\MenuExt\$(BHMenuExt)" "" "$INSTDIR\BHIEScript.htm"
	WriteRegBin HKCU "Software\Microsoft\Internet Explorer\MenuExt\$(BHMenuExt)" "contexts" f3000000
	
	;Store installation folder
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\BHIEPlugin" "" "$INSTDIR\BHTransmit.exe"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\BHIEPlugin" "Path" "$INSTDIR"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "DisplayName" "${PROGRAM_NAME} ${PROGRAM_VERSION}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "UninstallString" '"$INSTDIR\BHIEPluginUninstall.exe"'
	
	;Create uninstaller
	WriteUninstaller "$INSTDIR\BHIEPluginUninstall.exe"
	
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
	Delete "$INSTDIR\Uninstall.exe"
	
	RMDir "$INSTDIR"
	
	;Delete RegistryKeys
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}"
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\BHIEPlugin"
	
	DeleteRegKey HKCU "Software\Microsoft\Internet Explorer\MenuExt\$(BHMenuExt)"
	
SectionEnd

;--------------------------------
;Uninstaller Functions

Function un.onInit

FunctionEnd
