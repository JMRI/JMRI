; -------------------------------------------------------------------------
; - JMRI Launcher
; -------------------------------------------------------------------------
; - This is used to launch a JMRI application on Microsoft Windows.
; - It performs the following:
; -   find the Java installation
; -   determines appropriate native libraries based on JRE architecture
; -   build a dynamic ClassPath
; -   find the installed memory
; -   launch Java with the specified class
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Compilation instructions
; -------------------------------------------------------------------------
; - To build the resulting .exe file use one of:
; -   makensis.exe  (command-line)
; -   makensisw.exe (Windows GUI)
; - These are part of the NSIS installation suite available from:
; -   http://nsis.sourceforge.net/
; - This MUST be built using the 'Large strings' build available from:
; -   http://nsis.sourceforge.net/Special_Builds
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Version History
; -------------------------------------------------------------------------
; - Version 0.1.12.0
; - Change to use javaw.exe when not 'noisy' and remove window minimising
; - Re-introduced jinput.plugins option
; - Due to increasing length of classpath and option string, must be built
; - using large-string version of makensis (8192 byte length as opposed to
; - default 1024 byte length) otherwise launch failures will occur in many
; - configurations
; -------------------------------------------------------------------------
; - Version 0.1.11.0
; - Reverse out jinput.plugins option for now as it is causing problems
; - with some configurations
; -------------------------------------------------------------------------
; - Version 0.1.10.0
; - Reversed out environment variable modifications for now
; -------------------------------------------------------------------------
; - Version 0.1.9.0
; - Add jinput.plugins option to Java command line.
; - Add the possibility to modify launcher behaviour using environment
; - variables (as per Mac OS X and Linux platforms):
; -  JMRI_HOME     - determines the program location
; -  JMRI_OPTIONS  - specifies additional JVM options
; -  JMRI_PREFSDIR - specifies an alternative preferences directory
; -  JMRI_USERHOME - specifies an alternative user home directory
; - If both JMRI_PREFSDIR and JMRI_USERHOME are defined, JMRI_PREFSDIR will
; - take precedence for preference file location.
; -------------------------------------------------------------------------
; - Version 0.1.8.0
; - Correction of the sort-order for native libraries - is now architecture
; - specific first, followed by generic
; - Improved initial memory checks
; -------------------------------------------------------------------------
; - Version 0.1.7.0
; - Update to correctly identify JRE architecture on x64 systems and set
; - path to appropriate native libraries
; -------------------------------------------------------------------------
; - Version 0.1.6.0
; - correct bug that caused crash when launching with single quote in path
; -------------------------------------------------------------------------
; - Version 0.1.5.0
; - modified delay in window minimising routine to help with problems seen
; - on slower machines (change from 10ms to 60ms delay)
; - fixed problem in minimising loop (was infinite)
; - added window class to look for under Win98
; -------------------------------------------------------------------------
; - Version 0.1.4.0
; - modified free memory calculation to correctly work on x64 based systems
; -------------------------------------------------------------------------
; - Version 0.1.3.0
; - added path options for Jython and messages.log to locate these in the
; - user's profile folder, rather than the JMRI program directory when run
; - on Windows 2000 or later.
; -------------------------------------------------------------------------
; - Version 0.1.2.0
; - modified to minimise java console by default
; - added '/noisy' command-line option to display the java console
; -------------------------------------------------------------------------
; - Version 0.1.1.0
; - modified command-line parameter capture behaviour
; - added '/debug' command-line option
; - for Vista, ensured that launcher runs as a user level process
; -------------------------------------------------------------------------
; - Version 0.1.0.0
; - initial release
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Basic information
; - These should be edited to suit the application
; -------------------------------------------------------------------------
!define AUTHOR     "Matt Harris for JMRI"         ; Author name
!define APP        "LaunchJMRI"                   ; Application name
!define COPYRIGHT  "© 1997-2010 JMRI Community"   ; Copyright string
!define VER        "0.1.12.0"                     ; Launcher version
!define PNAME      "${APP}"                       ; Name of launcher
; -- Comment out next line to use {app}.ico
!define ICON       "decpro5.ico"                  ; Launcher icon
!define MINMEM     20                             ; Minimum memory in Mbyte
!define MAXMEM     640                            ; Maximum memory in Mbyte

; -------------------------------------------------------------------------
; - End of basic information
; - Lines below here should not normally require editing
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Variable declarations
; -------------------------------------------------------------------------
Var JAVAPATH   ; holds the path to the location where JAVA files can be found
Var CLASSPATH  ; holds the class path for JMRI .jars
#Var EXESTRING  ; holds the whole exe string
Var OPTIONS    ; holds the JRE options
#Var JMRIOPTIONS ; holds the JMRI-specific options (read from JMRI_OPTIONS)
#Var JMRIPREFS  ; holds the path to user preferences (read from JMRI_PREFSDIR)
#Var JMRIHOME   ; holds the path to JMRI program files (read from JMRI_HOME)
#Var JMRIUSERHOME ; holds the path to user files (read from JMRI_USERHOME)
Var CALCMAXMEM ; holds the calculated maximum memory
Var PARAMETERS ; holds the commandline parameters (class and config file)
Var NOISY      ; used to determine if console should be visible or not
Var x64        ; used to determine OS architecture
Var x64JRE     ; used to determine JRE architecture

; -------------------------------------------------------------------------
; - WinAPI constants
; -------------------------------------------------------------------------
!define SW_NORMAL   1
!define SW_MINIMIZE 6

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
RequestExecutionLevel user ; set execution level on Windows Vista to user

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
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "${COPYRIGHT}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "by ${AUTHOR}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "${APP}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${VER}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "OriginalFilename" "${PNAME}.exe"

; -------------------------------------------------------------------------
; - Main section
; -------------------------------------------------------------------------
Section "Main"

  DetailPrint "CommandLine: $PARAMETERS"

  ; -- First determine if we're running on x64
  DetailPrint "Testing for x64..."
  System::Call kernel32::GetCurrentProcess()i.s
  System::Call kernel32::IsWow64Process(is,*i.s)
  Pop $x64
  DetailPrint "Result: $x64"
  
  ; -- Find the JAVA install
  
  ; -- Initialise JRE architecture variable
  StrCpy $x64JRE 0
  
  ; -- If we're running x64, first check for 64-bit JRE
  StrCmp 0 $x64 JRESearch
    DetailPrint "Setting x64 registry view..."
    SetRegView 64
    StrCpy $x64JRE 1

  ; -- Read from machine registry
  JRESearch:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\java"
    StrCmp $NOISY SW_NORMAL IsNoisy
    StrCpy $R0 "$R0w"
    IsNoisy:
    StrCpy $R0 "$R0.exe"

  ; -- Not found
  IfErrors 0 JreFound
    ; -- If we've got an error here on x64, switch to the 32-bit registry
    ; -- and retry
    StrCmp 0 $x64JRE JRENotFound
      SetRegView 32
      DetailPrint "Setting x86 registry view..."
      StrCpy $x64JRE 0
      Goto JRESearch
    
  JreNotFound:
    MessageBox MB_OK|MB_ICONSTOP "Java not found!"
    Goto Exit

  JreFound:
  StrCpy $JAVAPATH '"$R0"'
  DetailPrint "JavaPath: $JAVAPATH"

  ; -- Get the memory status
  StrCmp 0 $x64 Notx64
  Call GetSystemMemoryStatus64
  Goto CalcFreeMem
  Notx64:
    Call GetSystemMemoryStatus
  CalcFreeMem:
  System::Int64Op $4 / 1048576
  Pop $4
  DetailPrint "FreeMemory: $4m"
  StrCpy $CALCMAXMEM $4
  IntCmp $CALCMAXMEM ${MAXMEM} cmp_done cmp_max_lt cmp_max_gt
  cmp_max_lt:
    ; -- Check that the free memory is >= MINMEM
    IntCmp $CALCMAXMEM ${MINMEM} cmp_done cmp_min_lt cmp_done
    Goto cmp_done
  cmp_min_lt:
    ; -- If insufficient free memory, stop with an error
    MessageBox MB_OK|MB_ICONSTOP "Not enough free memory to start. JMRI requires at least ${MINMEM} MBytes free memory."
    Goto Exit
  cmp_max_gt:
    ; -- If free memory greater than MAXMEM, set to MAXMEM
    StrCpy $CALCMAXMEM ${MAXMEM}
    Goto cmp_done
  cmp_done:
  DetailPrint "MinMemory: ${MINMEM}m"
  DetailPrint "MaxMemory: $CALCMAXMEMm"
  
  ; -- Build options string
  ; -- JVM and RMI options

#  ; -- Read environment variable
#  ; -- JMRI_OPTIONS - additional JMRI options
#  ; -- If not defined, it returns an empty value
#  ReadEnvStr $JMRIOPTIONS "JMRI_OPTIONS"

#  StrCpy $OPTIONS "$JMRIOPTIONS -noverify"
  StrCpy $OPTIONS "$OPTIONS -noverify"
  StrCpy $OPTIONS "$OPTIONS -Dsun.java2d.d3d=false"
  StrCpy $OPTIONS "$OPTIONS -Djava.security.policy=security.policy"
  StrCpy $OPTIONS "$OPTIONS -Djinput.plugins=net.bobis.jinput.hidraw.HidRawEnvironmentPlugin"
  StrCmp 1 $x64JRE x64Libs x86Libs
  x86Libs:
    ; -- 32-bit libraries
    StrCpy $OPTIONS "$OPTIONS -Djava.library.path=.;lib\x86;lib"
    Goto LibsDone
  x64Libs:
    ; -- 64-bit libraries
    StrCpy $OPTIONS "$OPTIONS -Djava.library.path=.;lib\x64;lib"
  LibsDone:
  StrCpy $OPTIONS "$OPTIONS -Djava.rmi.server.codebase=file:java/classes/"
  ; -- ddraw is disabled to get around Swing performance problems in Java 1.5.0
  StrCpy $OPTIONS "$OPTIONS -Dsun.java2d.noddraw"
  ; -- memory start and max limits
  StrCpy $OPTIONS "$OPTIONS -Xms${MINMEM}m"
  StrCpy $OPTIONS "$OPTIONS -Xmx$CALCMAXMEMm"
  
#  ; -- Read environment variable
#  ; -- JMRI_USERHOME - user files location
#  ClearErrors
#  ReadEnvStr $JMRIUSERHOME "JMRI_USERHOME"
#  ; -- If defined, set user.home property
#  IfErrors ReadPrefsDir
#    StrCpy $OPTIONS `$OPTIONS -Duser.home="$JMRIUSERHOME"`
#
#  ReadPrefsDir:
#  ; -- Read environment variable
#  ; -- JMRI_PREFSDIR - user preferences location
#  ClearErrors
#  ReadEnvStr $JMRIPREFS "JMRI_PREFSDIR"
#  ; -- If defined, set jmri.prefsdir property
#  IfErrors 0 SetPrefsDir
#
#    StrCmp $PROFILE "" Prefs98
#      StrCpy $JMRIPREFS "$PROFILE\JMRI"
#      Goto PathOptions
#    Prefs98:
#      StrCpy $JMRIPREFS "$WINDIR\JMRI"
#      Goto PathOptions
#
#    SetPrefsDir:
#      StrCpy $OPTIONS `$OPTIONS -Djmri.prefsdir="$JMRIPREFS"`
#
#  PathOptions:
  ; -- set paths for Jython and message log
  ; -- Creates the necessary directory if not existing
  ; -- User Profile is only valid for Win2K and later
  ; -- so skip on earlier versions
  StrCmp $PROFILE "" OptionsDone
#    IfFileExists "$JMRIPREFS\systemfiles\*.*" SetPaths
#      CreateDirectory "$JMRIPREFS\systemfiles"
    IfFileExists "$PROFILE\JMRI\systemfiles\*.*" SetPaths
      CreateDirectory "$PROFILE\JMRI\systemfiles"
    SetPaths:
#    StrCpy $OPTIONS '$OPTIONS -Dpython.home="$JMRIPREFS\systemfiles"'
#    StrCpy $OPTIONS '$OPTIONS -Djmri.log.path="$JMRIPREFS\systemfiles\\"'
    StrCpy $OPTIONS '$OPTIONS -Dpython.home="$PROFILE\JMRI\systemfiles"'
    StrCpy $OPTIONS '$OPTIONS -Djmri.log.path="$PROFILE\JMRI\systemfiles\\"'
    ; -- jmri.log.path needs a double trailing backslash to ensure a valid command-line
  OptionsDone:
  DetailPrint "Options: $OPTIONS"

#  ; -- Read environment variable
#  ; -- JMRI_HOME - location of JMRI program files
#  ClearErrors
#  ReadEnvStr $JMRIHOME "JMRI_HOME"
#  IfErrors 0 EnvJmriHomeDone
#    ; -- If not defined, use the launcher location
#    StrCpy $JMRIHOME $EXEDIR
#  EnvJmriHomeDone:

  ; -- Build the ClassPath
  StrCpy $CLASSPATH ".;classes"
#  StrCpy $0 "$JMRIHOME" ; normally 'C:\Program Files\JMRI'
  StrCpy $0 "$EXEDIR" ; normally 'C:\Program Files\JMRI'
  StrCpy $3 "jmri.jar" ; set to jmri.jar to skip jmri.jar
  StrCpy $4 "" ; no prefix required
  Call GetClassPath
  StrCmp $9 "" +2 0
  StrCpy $CLASSPATH "$CLASSPATH;$9"
  StrCpy $CLASSPATH "$CLASSPATH;jmri.jar"
  StrCpy $3 "" ; set to blank to include all .jar files
  StrCpy $4 "lib\" ; lib prefix
#  StrCpy $0 "$JMRIHOME\lib" ; normally 'C:\Program Files\JMRI\lib'
  StrCpy $0 "$EXEDIR\lib" ; normally 'C:\Program Files\JMRI\lib'
  Call GetClassPath
  StrCmp $9 "" +2 0
  StrCpy $CLASSPATH "$CLASSPATH;$9"
  DetailPrint "ClassPath: $CLASSPATH"

#  StrCpy $EXESTRING '$JAVAPATH $OPTIONS -Djava.class.path="$CLASSPATH" $PARAMETERS'
#  DetailPrint "Exestring: $EXESTRING"
  DetailPrint "MaxLen: ${NSIS_MAX_STRLEN}"
  DetailPrint `ExeString: $JAVAPATH $OPTIONS -Djava.class.path="$CLASSPATH" $PARAMETERS`

  ; -- Finally get ready to run the application
#  SetOutPath $JMRIHOME
  SetOutPath $EXEDIR
  ; -- Launch the Java class.
#  Exec `$JAVAPATH $OPTIONS -Djava.class.path="$CLASSPATH" $PARAMETERS`
  
  ; -- use $5 to hold STARTUPINFO structure
  ; -- use $6 to hold PROCESS_INFORMATION structure
  ; -- use $7 to hold return value

  ; -- create STARTUPINFO structure
  System::Alloc /NOUNLOAD 68 ; 4*16 + 2*2
  Pop $5
  System::Call /NOUNLOAD '*$5(i 68)'
  ; -- create PROCESS_INFORMATION structure
  System::Call /NOUNLOAD '*(i, i, i, i)i .r6'
  ; -- create the process
  System::Call /NOUNLOAD `kernel32::CreateProcess(i, t $\`$JAVAPATH $OPTIONS -Djava.class.path="$CLASSPATH" $PARAMETERS$\`, i, i, i 0, i 0, i, i, i r5, i r6)i .r7`
  System::Call /NOUNLOAD '*$6(i,i,i .r7,i)'
  DetailPrint "ProcessID: $7"
  System::Free /NOUNLOAD $5
  System::Free $6

#  ; -- Check if we should minimise the java console
#  StrCmp $NOISY SW_NORMAL Exit
#
#  ; -- Loop through Console windows to find one linked to this new process
#  ; -- This may loop several times as java process can take time to
#  ;    create its console window
#  DetailPrint "Search for console window to minimise..."
#  StrCpy $4 0
#  winloop:
#    ; -- Sleep for 60 ms to ensure process doesn't run away with resources
#    Sleep 60
#    ; -- Increment loop counter
#    IntOp $4 $4 + 1
#    ; -- If the console window for this new process still hasn't opened
#    ;    after 1000 iterations (~1 minute) exit anyway.
#    IntCmp $4 1000 winloopexit
#    ; -- Find top-most ConsoleWindowClass window (for Win2000 and later)
#    FindWindow $1 "ConsoleWindowClass"
#    ; -- Find the process that owns this window
#    System::Call 'user32::GetWindowThreadProcessId(i r1, *i .r3)i .r5'
#    ; -- If it is owned by the process we launched earlier, end loop - if not, loop
#    StrCmp $3 $7 winfound
#    ; -- Find top-most tty window (for Win98)
#    FindWindow $1 "tty"
#    ; -- Find the process that owns this window
#    System::Call 'user32::GetWindowThreadProcessId(i r1, *i .r3)i .r5'
#    ; -- If it is owned by the process we launched earlier, end loop - if not, loop
#    StrCmp $3 $7 winfound winloop
#  winfound:
#    ; -- We've found the window, so minimise it
#    DetailPrint "Found console window - minimising..."
#    Sleep 60
#    ShowWindow $1 ${SW_MINIMIZE}
#  winloopexit:

  Exit:
  DetailPrint "To copy this text to the clipboard, right click then choose"
  DetailPrint "  'Copy Details To Clipboard'"
SectionEnd

Function .onInit
  ; -- Get commandline parameters
  StrCpy $0 $CMDLINE
  ; -- Setup the default environment
  SetSilent silent
  StrCpy $NOISY SW_MINIMIZE
  ; -- Start reading commandline parameters
  cmdloop:
  Push $0
  Call GetParameters
  Pop $0
  StrCmp $0 "" 0 cmdlineok
    MessageBox MB_OK|MB_ICONSTOP "No command line parameter. Usage 'LaunchJMRI.exe [/debug] [/noisy] class [config]'"
    Abort

  cmdlineok:
  ; -- Check if the first parameter is an option
  StrCpy $1 $0 1
  StrCmp $1 "/" cmdlineoptsget cmdlineoptsdone
  cmdlineoptsget:
  ; -- Process the possible commandline options
  ; -- At the moment this is implemented in a rather lazy way
  ;    to work with 5 character options only
  ; -- It would need updating to handle different option lengths
  ;    in the future if so required
  StrCpy $1 $0 6
  StrCmp $1 "/debug" optsdebug
  StrCmp $1 "/noisy" optsnoisy
  ; -- If we've got here, the commandline option is not known so give an error.
    MessageBox MB_OK|MB_ICONSTOP "Command line option '$1' not known."
    Abort

  ; -- Processing block for each option
  optsdebug:
  SetSilent normal
  Goto cmdloop
  
  optsnoisy:
  StrCpy $NOISY SW_NORMAL
  Goto cmdloop

  cmdlineoptsdone:
  ; -- Copy any remaining commandline parameters to $PARAMETERS
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
  System::Alloc /NOUNLOAD 32 ; -- comment out for > 2Gb
  ;System::Alloc /NOUNLOAD 64 ; -- uncomment for > 2Gb
  Pop $1
  ; -- Initialise
  System::Call /NOUNLOAD "*$1(i64)"
  ; -- Make system call
  System::Call /NOUNLOAD "Kernel32::GlobalMemoryStatus(i r1)" ; -- comment out for > 2Gb
  ;System::Call /NOUNLOAD "Kernel32::GlobalMemoryStatusEx(i r1)"  ; -- uncomment for > 2Gb
  ; -- Move returned info into NSIS variables
  System::Call /NOUNLOAD "*$1(i.r2, i.r3, i.r4, i.r5, i.r6, i.r7, i.r8, i.r9, i.r10)" ; -- comment out for > 2Gb
  ;System::Call /NOUNLOAD "*$1(i.r2, i.r3, l.r4, l.r5, l.r6, l.r7, l.r8, l.r9, l.r10)" ; -- uncomment for > 2Gb
  ; -- Free allocated memory
  System::Free $1
FunctionEnd

Function GetSystemMemoryStatus64
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
  ;System::Alloc /NOUNLOAD 32 ; -- comment out for > 2Gb
  System::Alloc /NOUNLOAD 64 ; -- uncomment for > 2Gb
  Pop $1
  ; -- Initialise
  System::Call /NOUNLOAD "*$1(i64)"
  ; -- Make system call
  ;System::Call /NOUNLOAD "Kernel32::GlobalMemoryStatus(i r1)" ; -- comment out for > 2Gb
  System::Call /NOUNLOAD "Kernel32::GlobalMemoryStatusEx(i r1)"  ; -- uncomment for > 2Gb
  ; -- Move returned info into NSIS variables
  ;System::Call /NOUNLOAD "*$1(i.r2, i.r3, i.r4, i.r5, i.r6, i.r7, i.r8, i.r9, i.r10)" ; -- comment out for > 2Gb
  System::Call /NOUNLOAD "*$1(i.r2, i.r3, l.r4, l.r5, l.r6, l.r7, l.r8, l.r9, l.r10)" ; -- uncomment for > 2Gb
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