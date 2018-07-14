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
	!define PROGRAM_NAME "BilderHerunterlader" ;Name
	!define PROGRAM_VERSION "TemplateY" ;Version
	!define PROGRAM_STARTMENU_DIR "BilderHerunterlader"
	!define PROGRAM_SHORTCUT_NAME "BilderHerunterlader"
	!define PROGRAM_EXECUTABLE "BH.jar"
	!define PROGRAM_ICON "BHIcon.ico"
	Name "${PROGRAM_NAME} ${PROGRAM_VERSION}"
	OutFile "${PROGRAM_NAME}TemplateXSetup.exe"
	
	;Default installation folder
	InstallDir "$PROGRAMFILES\${PROGRAM_NAME}"
	
	;Get installation folder from registry if available
	InstallDirRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\${PROGRAM_EXECUTABLE}" "Path"
	
	;Request application privileges for Windows Vista / 7
	RequestExecutionLevel admin
	
;--------------------------------
;Variables
	
	Var StartMenuFolder
	
;--------------------------------
;Interface Settings
	
	!define MUI_STARTMENUPAGE_NODISABLE
	
	!define MUI_ABORTWARNING
	
;--------------------------------
;Language Selection Dialog Settings
	
	
	
;--------------------------------
;Pages
	
	;Install Pages
	!insertmacro MUI_PAGE_LICENSE "..\license.txt"
	!insertmacro MUI_PAGE_COMPONENTS
	!insertmacro MUI_PAGE_DIRECTORY
	
	;Start Menu Folder Page Configuration
	!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKLM" 
	!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\${PROGRAM_NAME}" 
	!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "StartMenuFolder"
	!define MUI_STARTMENUPAGE_DEFAULTFOLDER "${PROGRAM_STARTMENU_DIR}"
	!insertmacro MUI_PAGE_STARTMENU Application $StartMenuFolder
	
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
	
	;Install files
	SetOverwrite on
	File "..\BH.jar"
	File "..\license.txt"
	File "..\BHIcon.ico"
	
	File "..\directories.properties.example"
	File "..\HostzDefaultImages.txt.example"
	File "..\ChangelogVersion4.txt"
	
	SetOutPath "$INSTDIR\hosts"
	File "..\hosts\*.class"
	
	SetOutPath "$INSTDIR\rules"
	File "..\rules\*.xml"
	
	SetOutPath "$INSTDIR\lib"
	File "..\lib\*.jar"
	
	SetOutPath "$INSTDIR"
	
	;Set Write permissions on programm directory
	;ExecWait 'CACLS "$INSTDIR" /E /T /C /G "%username%":F'
	
	;Create profile directory and set write permissions on it
	;CreateDirectory "$PROFILE\.BH"
	;Exec 'CACLS "$PROFILE\.BH" /E /T /C /G "%username%":F'
	
	;Store installation folder
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\${PROGRAM_EXECUTABLE}" "" "$INSTDIR\${PROGRAM_EXECUTABLE}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\${PROGRAM_EXECUTABLE}" "Path" "$INSTDIR"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "DisplayName" "${PROGRAM_NAME} ${PROGRAM_VERSION}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}" "UninstallString" '"$INSTDIR\Uninstall.exe"'
	
	;Create uninstaller
	WriteUninstaller "$INSTDIR\Uninstall.exe"
	
SectionEnd

Section $(SecStartMenuName) SecStartMenu
	!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
		CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
		CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
		CreateShortCut "$SMPROGRAMS\$StartMenuFolder\${PROGRAM_SHORTCUT_NAME}.lnk" "$INSTDIR\${PROGRAM_EXECUTABLE}" "" "$INSTDIR\${PROGRAM_ICON}" 0
		CreateShortCut "$SMPROGRAMS\$StartMenuFolder\${PROGRAM_SHORTCUT_NAME}_Java9.lnk" "$\"%ProgramData%\Oracle\Java\javapath\javaw.exe$\"" "--add-modules ALL-SYSTEM -jar $\"$INSTDIR\${PROGRAM_EXECUTABLE}$\"" "$INSTDIR\${PROGRAM_ICON}" 0
	!insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section /o $(SecDesktopIconName) SecDesktopIcon
	CreateShortCut "$DESKTOP\${PROGRAM_SHORTCUT_NAME}.lnk" "$INSTDIR\${PROGRAM_EXECUTABLE}" "" "$INSTDIR\${PROGRAM_ICON}" 0
	CreateShortCut "$DESKTOP\${PROGRAM_SHORTCUT_NAME}_Java9.lnk" "$\"%ProgramData%\Oracle\Java\javapath\javaw.exe$\"" "--add-modules ALL-SYSTEM -jar $\"$INSTDIR\${PROGRAM_EXECUTABLE}$\"" "$INSTDIR\${PROGRAM_ICON}" 0
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
		!insertmacro MUI_DESCRIPTION_TEXT ${SecStartMenu} $(SecStartMenuDesc)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecDesktopIcon} $(SecDesktopIconDesc)
	!insertmacro MUI_FUNCTION_DESCRIPTION_END
	
;--------------------------------
;Uninstaller Section

Section "Uninstall"
	
	;Delete Files and Folders in Installation-Directory
	Delete "$INSTDIR\${PROGRAM_EXECUTABLE}"
	Delete "$INSTDIR\log4j.xml"
	Delete "$INSTDIR\license.txt"
	Delete "$INSTDIR\BHIcon.ico"
	Delete "$INSTDIR\directories.properties.example"
	Delete "$INSTDIR\HostzDefaultImages.txt.example"
	Delete "$INSTDIR\ChangelogVersion4.txt"
	Delete "$INSTDIR\lib\*.jar"
	Delete "$INSTDIR\Uninstall.exe"
	
	MessageBox MB_YESNO "Remove also all Rules and Host-Classes?" IDYES true IDNO false
	true:
		Delete "$INSTDIR\hosts\*.class"
		RMDir "$INSTDIR\hosts"
		Delete "$INSTDIR\rules\*.xml"
		RMDir "$INSTDIR\rules"
		Goto next
	false:
		Goto next
	next:
	
	RMDir "$INSTDIR\lib"
	RMDir "$INSTDIR"
	
	;Startmenu Shortcuts
	!insertmacro MUI_STARTMENU_GETFOLDER Application $R0
	Delete "$SMPROGRAMS\$R0\*.*"
	RMDir "$SMPROGRAMS\$R0"
	
	;Delete Desktop Shortcut
	Delete "$DESKTOP\${PROGRAM_SHORTCUT_NAME}.lnk"
	Delete "$DESKTOP\${PROGRAM_SHORTCUT_NAME}_Java9.lnk"
	
	;Delete RegistryKeys
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROGRAM_NAME}"
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\App Paths\${PROGRAM_EXECUTABLE}"
	DeleteRegKey HKLM "Software\${PROGRAM_NAME}"
	
SectionEnd

;--------------------------------
;Uninstaller Functions

Function un.onInit
	
FunctionEnd
