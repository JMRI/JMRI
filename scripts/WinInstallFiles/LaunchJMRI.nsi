; -------------------------------------------------------------------------
; - JMRI Launcher
; -------------------------------------------------------------------------
; - This is used to launch a JMRI application on Microsoft Windows.
; - It performs the following:
; -   find the Java installation.
; -   build a dynamic ClassPath
; -   find the installed memory
; -   launch Java with the specified class
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Basic information
; - These should be edited to suit the application
; -------------------------------------------------------------------------
!define AUTHOR   "Matt Harris"     ; Author name
!define APP      "LaunchJMRI"      ; Application name
!define VER      "0.1.1.0"         ; Launcher version
!define PNAME    "${APP}"          ; Name of launcher
; -- Comment out next line to use {app}.ico
!define ICON     "decpro5.ico"     ; Launcher icon
!define MINMEM   10                ; Minimum memory in Mbyte
!define MAXMEM   200               ; Maximum memory in Mbyte

; -------------------------------------------------------------------------
; - End of basic information
; - Lines below here should not normally require editing
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Variable declarations
; -------------------------------------------------------------------------
Var JAVAPATH   ; holds the path to the location where JAVA files can be found
Var CLASSPATH  ; holds the class path for JMRI .jars
Var OPTIONS    ; holds the JRE options
Var CALCMAXMEM ; holds the calculated maximum memory
Var PARAMETERS ; holds the commandline parameters

; -------------------------------------------------------------------------
; - Compiler Flags (to reduce executable size, saves some bytes)
; -------------------------------------------------------------------------
SetDatablockOptimize on
SetCompress force
SetCompressor /SOLID /FINAL lzma

; -------------------------------------------------------------------------
; - Runtime Switches
; -------------------------------------------------------------------------
CRCCheck On ; do CRC check on launcher before start ("Off" for later EXE compression)
WindowIcon Off ; show no icon of the launcher
ShowInstDetails Show ; show the installation details
;SilentInstall Silent ; start as launcher, not as installer
;AutoCloseWindow True ; do not automatically close when finished
RequestExecutionLevel user ; set execution level for Windows Vista to user

; -------------------------------------------------------------------------
; - Set basic information
; -------------------------------------------------------------------------
Name "${APP}"
Caption "${APP} - ${VER}"
!ifdef ICON
     Icon "${ICON}"
!else
     Icon "${APP}.ico"
!endif
OutFile "${PNAME}.exe"

; -------------------------------------------------------------------------
; - Set version information
; -------------------------------------------------------------------------
LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
VIProductVersion "${VER}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "${APP}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "Used to launch a JMRI application."
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "Created by ${AUTHOR}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "by ${AUTHOR}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "${APP}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${VER}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "OriginalFilename" "${PNAME}.exe"

; -------------------------------------------------------------------------
; - Main section
; -------------------------------------------------------------------------
Section "Main"

  DetailPrint "CommandLine: $PARAMETERS"

; -- Find the JAVA install

; -- Read from machine registry
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\java.exe"

; -- Not found
  IfErrors 0 JreFound
    MessageBox MB_OK|MB_ICONSTOP "Java not found!"
    Goto Exit

  JreFound:
  StrCpy "$JAVAPATH" "$R0"
  DetailPrint "JavaPath: $JAVAPATH"

; -- Get the memory status
  Call GetSystemMemoryStatus
  System::Int64Op $4 / 1048576
  Pop $4
  StrCpy $CALCMAXMEM $4
  IntCmp $CALCMAXMEM ${MAXMEM} cmp_eq cmp_lt cmp_gt
  cmp_eq:
    Goto cmp_done
  cmp_lt:
    Goto cmp_done
  cmp_gt:
    StrCpy $CALCMAXMEM ${MAXMEM}
    Goto cmp_done
  cmp_done:
  DetailPrint "MaxMemory: $CALCMAXMEMm"

; -- Build options string
  ; -- JVM and RMI options
  StrCpy $OPTIONS "$OPTIONS -noverify"
  StrCpy $OPTIONS "$OPTIONS -Dsun.java2d.d3d=false"
  StrCpy $OPTIONS "$OPTIONS -Djava.security.policy=security.policy"
  StrCpy $OPTIONS "$OPTIONS -Djava.library.path=.;lib"
  StrCpy $OPTIONS "$OPTIONS -Djava.rmi.server.codebase=file:java/classes/"
  ; -- ddraw is disabled to get around Swing performance problems in Java 1.5.0
  StrCpy $OPTIONS "$OPTIONS -Dsun.java2d.noddraw"
  ; -- memory start and max limits
  StrCpy $OPTIONS "$OPTIONS -Xms10m"
  StrCpy $OPTIONS "$OPTIONS -Xmx$CALCMAXMEMm"

; -- Build the ClassPath
  StrCpy $CLASSPATH ".;classes"
  StrCpy $0 "$EXEDIR" ; normally 'C:\Program Files\JMRI'
  StrCpy $3 "jmri.jar" ; set to jmri.jar to skip jmri.jar
  StrCpy $4 "" ; no prefix required
  Call GetClassPath
  StrCmp $9 "" +2 0
  StrCpy $CLASSPATH "$CLASSPATH;$9"
  StrCpy $CLASSPATH "$CLASSPATH;jmri.jar"
  StrCpy $3 "" ; set to blank to include all .jar files
  StrCpy $4 "lib\" ; lib prefix
  StrCpy $0 "$EXEDIR\lib" ; normally 'C:\Program Files\JMRI\lib'
  Call GetClassPath
  StrCmp $9 "" +2 0
  StrCpy $CLASSPATH "$CLASSPATH;$9"

  StrCpy $0 '"$JAVAPATH" $OPTIONS -Djava.class.path="$CLASSPATH" $PARAMETERS'
;  MessageBox MB_OK|MB_ICONINFORMATION "$0" ; for debugging

  ; -- finally get ready to run the application
  SetOutPath $EXEDIR

  ; -- Launch the Java class.
  Exec $0

  Exit:
SectionEnd

Function .onInit
; -- Get commandline parameters
  StrCpy $0 $CMDLINE
  SetSilent silent
  cmdloop:
  Push $0
  Call GetParameters
  Pop $0
  StrCmp $0 "" 0 cmdlineok
    MessageBox MB_OK|MB_ICONSTOP "No command line parameter. Usage 'LaunchJMRI.exe [/debug] class [config]'"
    Abort

  cmdlineok:
  StrCpy $1 $0 1
  StrCmp $1 "/" cmdlineoptsget cmdlineoptsdone
  cmdlineoptsget:
  StrCpy $1 $0 6
  StrCmp $1 "/debug" optsdebug
; -- finished processing options
  Goto cmdlineoptsdone

  optsdebug:
  SetSilent normal
  Goto cmdloop
  
  cmdlineoptsdone:
  StrCpy $PARAMETERS $0
FunctionEnd


; -------------------------------------------------------------------------
; - JMRI Launcher Functions
; -------------------------------------------------------------------------

Function GetSystemMemoryStatus
; -------------------------------------------------------------------------
; - Get system memory status
; - input: none
; - output: $2 - Structure size (bytes)
; -         $3 - Memory load (%)
; -         $4 - Total physical memory (bytes)
; -         $5 - Free physical memory (bytes)
; -         $6 - Total page file (bytes)
; -         $7 - Free page file (bytes)
; -         $8 - Total virtual (bytes)
; -         $9 - Free virtual (bytes)
; -------------------------------------------------------------------------
; - Notes:
; - If more than 2Gb, this will return 2Gb
; - GlobalMemoryStatutsEx should be used for
; - > 2Gb but this is not supported until Win2K
; -
; - To convert to MBytes, use:
; -  System::Int64Op $VAR / 1048576
; -  Pop $VAR
; -------------------------------------------------------------------------

  ; -- Allocate memory
  System::Alloc 32 ; -- comment out for > 2Gb
  ;System::Alloc 64 ; -- uncomment for > 2Gb
  Pop $1
  ; -- Initialise
  System::Call "*$1(i64)"
  ; -- Make system call
  System::Call "Kernel32::GlobalMemoryStatus(i r1)" ; -- comment out for > 2Gb
  ;System::Call "Kernel32::GlobalMemoryStatusEx(i r1)"  ; -- uncomment for > 2Gb
  ; -- Move returned info into NSIS variables
  System::Call "*$1(i.r2, i.r3, i.r4, i.r5, i.r6, i.r7, i.r8, i.r9, i.r10)" ; -- comment out for > 2Gb
  ;System::Call "*$1(i.r2, i.r3, l.r4, l.r5, l.r6, l.r7, l.r8, l.r9, l.r10)" ; -- uncomment for > 2Gb
  ; -- Free allocated memory
  System::Free $1
FunctionEnd

Function GetClassPath
; -------------------------------------------------------------------------
; - Get the class path by searching for .jar files in specified path
; - input:  $0 path to search
; -         $3 single file to exclude (i.e. jmri.jar)
; -         $4 prefix to add (i.e. lib/)
; - output: $9 ClassPath
; -------------------------------------------------------------------------

  FindFirst $2 $1 $0\*.jar
  StrCpy $9 ""
  StrCmp $2 "" error
  again:
  StrCmp $1 "" done
  StrCmp $1 $3 readnext
  StrCmp $9 "" 0 append
  StrCpy $9 $4$1
  Goto readnext
  append:
  StrCpy $9 "$9;$4$1"
  readnext:
  FindNext $2 $1
  Goto again
  done:
  FindClose $2
  Goto finished
  error:
  ; Oops!
  finished:
FunctionEnd

Function GetParameters
; -------------------------------------------------------------------------
; - Gets command line parameters
; - input:  top of stack
; - output: top of stack
; - modifies no other variables
; -------------------------------------------------------------------------

  Exch $R0
  Push $R1
  Push $R2
  Push $R3
  Push $R4
  
  StrCpy $R4 $R0
  StrCpy $R2 1
  StrLen $R3 $R4
  
  ; -- Check for quote or space
  StrCpy $R0 $R4 $R2
  StrCmp $R0 '"' 0 +3
    StrCpy $R1 '"'
    Goto loop
  StrCpy $R1 " "

  loop:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $R4 1 $R2
    StrCmp $R0 $R1 get
    StrCmp $R2 $R3 get
    Goto loop

  get:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $R4 1 $R2
    StrCmp $R0 " " get
    StrCpy $R0 $R4 "" $R2

  Pop $R4
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0

FunctionEnd