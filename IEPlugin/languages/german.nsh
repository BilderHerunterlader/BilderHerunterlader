;Language specific include file

!verbose 3

!ifdef CURLANG
  !undef CURLANG
!endif
!define CURLANG ${LANG_GERMAN}

;Component-select dialog
LangString SecMainName ${CURLANG} "${PROGRAM_NAME}"

LangString SecMainDesc ${CURLANG} "${PROGRAM_NAME}"

;MenuExt
LangString BHMenuExt ${CURLANG} "Dateien mit BH herunterladen"


!verbose 4
