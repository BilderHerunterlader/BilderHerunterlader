;Language specific include file

!verbose 3

!ifdef CURLANG
  !undef CURLANG
!endif
!define CURLANG ${LANG_ENGLISH}

;Component-select dialog
LangString SecMainName ${CURLANG} "${PROGRAM_NAME}"

LangString SecMainDesc ${CURLANG} "${PROGRAM_NAME}"

;MenuExt
LangString BHMenuExt ${CURLANG} "Download files with BH"


!verbose 4
