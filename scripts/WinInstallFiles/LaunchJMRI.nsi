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
; - Version 0.1.20.0
; - Allow options to be specified either as '/' or '-'
; - Add option '/J' to pass JVM option
; -------------------------------------------------------------------------
; - Version 0.1.19.0
; - Bring heap size calculation into line with Linux and OS X launchers
; - Increase minimum memory requirement to 96 MB
; - Change maximum memory requirement to be between 192 and 768 and to use
; - maximum of half physical physical available RAM when above 192
; - Allow use of '/profile' parameter to specify specific profile
; -------------------------------------------------------------------------
; - Version 0.1.18.0
; - Check if application is already running with option to continue
; - or abort
; -------------------------------------------------------------------------
; - Version 0.1.17.0
; - Modification to pass JMRI process return code back to caller
; -------------------------------------------------------------------------
; - Version 0.1.16.0
; - Modification to pass flag for correct usage with UTF-8 encoded files
; -------------------------------------------------------------------------
; - Version 0.1.15.0
; - Add flag to allow 64-bit Windows to force the use of a 32-bit JRE
; -------------------------------------------------------------------------
; - Version 0.1.14.0
; - Modification to monitor launched Java process for a return code that
; - signals a re-launch of JMRI
; - Tidied up source code
; -------------------------------------------------------------------------
; - Version 0.1.13.0
; - Modification to enable the process name in Windows Task Manager to
; - match the JMRI application being launched
; - Improve command line option processing
; - Restore environment variable usage (see notes below for 0.1.9.0)
; - Ensure user.home is correctly set - see Sun Java bug:
; -   http://bugs.sun.com/view_bug.do?bug_id=4787931
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
!define COPYRIGHT  "© 1997-2016 JMRI Community"   ; Copyright string
!define VER        "0.1.20.0"                     ; Launcher version
!define PNAME      "${APP}"                       ; Name of launcher
; -- Comment out next line to use {app}.ico
!define ICON       "decpro5.ico"                  ; Launcher icon
!define MINMEM     96                             ; Minimum memory in Mbyte
!define HIMAXMEM   768                            ; Highest Maximum memory
!define LOMAXMEM   192                            ; Lowest Maximum memory

; -------------------------------------------------------------------------
; - End of basic information
; - Lines below here should not normally require editing
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Variable declarations
; -------------------------------------------------------------------------
Var JAVAPATH   ; holds the path to the location where JAVA files can be found
Var JAVAEXE    ; holds the name of the JAVA exe to use
Var JEXEPATH   ; holds the path to the temporary JAVA exe used
Var CLASSPATH  ; holds the class path for JMRI .jars
Var CLASS      ; holds the class to launch
Var APPNAME    ; holds the application name
Var OPTIONS    ; holds the JRE options
Var JVMOPTIONS ; holds the additonal JRE options passed via '/J'
Var JMRIOPTIONS ; holds the JMRI-specific options (read from JMRI_OPTIONS)
Var JMRIPREFS  ; holds the path to user preferences (read from JMRI_PREFSDIR)
Var JMRIHOME   ; holds the path to JMRI program files (read from JMRI_HOME)
Var JMRIUSERHOME ; holds the path to user files (read from JMRI_USERHOME)
Var JMRIPROFILE ; holds the file to use for profile
Var CALCMAXMEM ; holds the calculated maximum memory
Var PARAMETERS ; holds the commandline parameters (class and config file)
Var NOISY      ; used to determine if console should be visible or not
Var x64        ; used to determine OS architecture
Var x64JRE     ; used to determine JRE architecture
Var FORCE32BIT ; used to determine if 32-bit JRE should always be used

; -------------------------------------------------------------------------
; - Various constants
; -------------------------------------------------------------------------
!define SW_NORMAL   1   ; from WinAPI for Normal window
!define SW_MINIMIZE 6   ; from WinAPI for Minimized window
!define ARCH_32BIT  0   ; represents 32-bit architecture
!define ARCH_64BIT  1   ; represents 64-bit architecture
!define FLAG_NO     0   ; represents a NO return value from subroutines
!define FLAG_YES    1   ; represents a YES return value from subroutines

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

  DetailPrint "CommandLine: $CMDLINE"
  DetailPrint "AppName: $APPNAME"
  DetailPrint "Class: $CLASS"
  DetailPrint "Parameters: $PARAMETERS"
  DetailPrint "Noisy: $NOISY"
  DetailPrint "Force32bit: $FORCE32BIT"
  DetailPrint "Profile: $JMRIPROFILE"

  ; -- First determine if we're running on x64
  DetailPrint "Testing for x64..."
  System::Call kernel32::GetCurrentProcess()i.s
  System::Call kernel32::IsWow64Process(is,*i.s)
  Pop $x64
  DetailPrint "Result: $x64"
  
  ; -- Find the JAVA install
  
  ; -- Initialise JRE architecture variable
  StrCpy $x64JRE ${ARCH_32BIT}
  
  ; -- Determine which JAVA exe to use
  StrCpy $R0 "java"
  StrCmp $NOISY ${SW_NORMAL} IsNoisy
  StrCpy $R0 "$R0w"
  IsNoisy:
  StrCpy $JAVAEXE "$R0.exe"
  
  ; -- If we're running x64, first check for 64-bit JRE
  StrCmp ${ARCH_32BIT} $x64 JRESearch
    ; -- Now check if we should force 32-bit JRE usage on x64
    StrCmp ${FLAG_YES} $FORCE32BIT JRESearch
      ; -- No need to force
      DetailPrint "Setting x64 registry view..."
      SetRegView 64
      StrCpy $x64JRE ${ARCH_64BIT}

  ; -- Read from machine registry
  JRESearch:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\$JAVAEXE"

  ; -- Not found
  IfErrors 0 JreFound
    ; -- If we've got an error here on x64, switch to the 32-bit registry
    ; -- and retry (not needed if we've forced 32-bit above)
    StrCmp ${ARCH_32BIT} $x64JRE JRENotFound
      SetRegView 32
      DetailPrint "Setting x86 registry view..."
      StrCpy $x64JRE ${ARCH_32BIT}
      Goto JRESearch
    
  JreNotFound:
    MessageBox MB_OK|MB_ICONSTOP "Java not found!"
    Goto Exit

  JreFound:
  StrCpy $JAVAPATH $R0
  DetailPrint "JavaPath: $JAVAPATH"
  
  ; -- Now we've determined Java is basically OK, copy the file to a
  ; -- temporary location and rename it
  
  ; -- First try to remove any old temporary launchers
  RMDir /r $TEMP\LaunchJMRI
  
  ; -- Now create temporary directory and copy JAVA launcher across
  CreateDirectory `$TEMP\LaunchJMRI`
  StrCpy $JEXEPATH `$TEMP\LaunchJMRI\$APPNAME.exe`
  DetailPrint `CopyFiles: $JAVAPATH $JEXEPATH`
  System::Call "kernel32::CopyFile(t `$JAVAPATH`, t `$JEXEPATH`, b `0`) ?e"
  Pop $0
  DetailPrint "Result: $0"
  
  ; -- Check that the temporary launcher file exists
  ClearErrors
  FindFirst $0 $1 $JEXEPATH
  IfErrors 0 ExeRenameDone
    ; -- Wasn't found so use regular launcher
    StrCpy $JEXEPATH $JAVAPATH
  ExeRenameDone:
  DetailPrint "JExePath: $JEXEPATH"

  ; -- Get the memory status
  StrCmp ${ARCH_32BIT} $x64 Notx64
  Call GetSystemMemoryStatus64
  Goto CalcMem
  Notx64:
    Call GetSystemMemoryStatus
  CalcMem:
  System::Int64Op $4 / 1048576
  Pop $4
  DetailPrint "PhysicalMemory: $4m"
  ; -- Check that physical memory is >= MINMEM
  IntCmp $4 ${MINMEM} cmp_max cmp_min_lt cmp_max
  cmp_min_lt:
    ; -- If insufficient physical memory, stop with an error
    MessageBox MB_OK|MB_ICONSTOP "Not enough available memory to start. JMRI requires at least ${MINMEM} MBytes memory."
    Goto Exit
  cmp_max:
    ; -- Check if 1/2 physical memory is between LOMAXMEM and HIMAXMEM
    IntOp $CALCMAXMEM $4 / 2
    DetailPrint "Check if $CALCMAXMEMm is between ${LOMAXMEM}m and ${HIMAXMEM}m"
    IntCmp $CALCMAXMEM ${HIMAXMEM} cmp_done cmp_max_lt cmp_max_gt
  cmp_max_gt:
    ; -- 1/2 physical memory is greater than HIMAXMEM,
    ; -- so set to HIMAXMEM
    DetailPrint "Greater than HIMAXMEM: ${HIMAXMEM}m"
    StrCpy $CALCMAXMEM ${HIMAXMEM}
    Goto cmp_done
  cmp_max_lt:
    ; -- 1/2 physical memory is less than HIMAXMEM
    ; --
    DetailPrint "Less than HIMAXMEM: ${HIMAXMEM}m"
    DetailPrint "Check if $CALCMAXMEMm is less than ${LOMAXMEM}m"
    IntCmp $CALCMAXMEM ${LOMAXMEM} cmp_done cmp_use_lomax cmp_done
    cmp_use_lomax:
      DetailPrint "Use LOMAXMEM: ${LOMAXMEM}m"
      StrCpy $CALCMAXMEM ${LOMAXMEM}
  cmp_done:
  DetailPrint "MinMemory: ${MINMEM}m"
  DetailPrint "MaxMemory: $CALCMAXMEMm"
  
  ; -- Build options string
  ; -- JVM and RMI options

  ; -- Read environment variable
  ; -- JMRI_OPTIONS - additional JMRI options
  ; -- If not defined, it returns an empty value
  ReadEnvStr $JMRIOPTIONS "JMRI_OPTIONS"

  ; -- Add profile (if specified)
  StrCmp $JMRIPROFILE "" contOptions
    StrCpy $JMRIOPTIONS '$JMRIOPTIONS -Dorg.jmri.profile="$JMRIPROFILE"'
  
  contOptions:
  StrCpy $OPTIONS "$JMRIOPTIONS $JVMOPTIONS -noverify"
  StrCpy $OPTIONS "$OPTIONS -Dsun.java2d.d3d=false"
  StrCpy $OPTIONS "$OPTIONS -Djava.security.policy=security.policy"
  StrCpy $OPTIONS "$OPTIONS -Djinput.plugins=net.bobis.jinput.hidraw.HidRawEnvironmentPlugin"
  StrCmp ${ARCH_64BIT} $x64JRE x64Libs x86Libs
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
  ; -- default file coding
  StrCpy $OPTIONS "$OPTIONS -Dfile.encoding=UTF-8"
  
  ; -- Read environment variable
  ; -- JMRI_USERHOME - user files location
  ClearErrors
  ReadEnvStr $JMRIUSERHOME "JMRI_USERHOME"
  ; -- If defined, set user.home property
  IfErrors CheckUserHome
    DetailPrint "Set user.home to JMRI_USERHOME: $JMRIUSERHOME"
    StrCpy $OPTIONS `$OPTIONS -Duser.home="$JMRIUSERHOME"`
    Goto ReadPrefsDir
    
  CheckUserHome:
  ; -- If not defined, check user home is consistent
  Call CheckUserHome
  Pop $0
  StrCmp $0 ${FLAG_YES} ReadPrefsDir
    ; -- Not consistent - set to Profile
    DetailPrint "Set user.home to %USERPROFILE%: $PROFILE"
    StrCpy $OPTIONS `$OPTIONS -Duser.home="$PROFILE"`

  ReadPrefsDir:
  ; -- Read environment variable
  ; -- JMRI_PREFSDIR - user preferences location
  ClearErrors
  ReadEnvStr $JMRIPREFS "JMRI_PREFSDIR"
  ; -- If defined, set jmri.prefsdir property
  IfErrors 0 SetPrefsDir

    StrCmp $PROFILE "" Prefs98
      StrCpy $JMRIPREFS "$PROFILE\JMRI"
      Goto PathOptions
    Prefs98:
      StrCpy $JMRIPREFS "$WINDIR\JMRI"
      Goto PathOptions

    SetPrefsDir:
      StrCpy $OPTIONS `$OPTIONS -Djmri.prefsdir="$JMRIPREFS"`

  PathOptions:
  ; -- set paths for Jython and message log
  ; -- Creates the necessary directory if not existing
  ; -- User Profile is only valid for Win2K and later
  ; -- so skip on earlier versions
  StrCmp $PROFILE "" OptionsDone
    IfFileExists "$JMRIPREFS\systemfiles\*.*" SetPaths
      CreateDirectory "$JMRIPREFS\systemfiles"
    SetPaths:
    StrCpy $OPTIONS '$OPTIONS -Dpython.home="$JMRIPREFS\systemfiles"'
    StrCpy $OPTIONS '$OPTIONS -Djmri.log.path="$JMRIPREFS\systemfiles\\"'
    ; -- jmri.log.path needs a double trailing backslash to ensure a valid command-line
  OptionsDone:
  DetailPrint "Options: $OPTIONS"

  ; -- Read environment variable
  ; -- JMRI_HOME - location of JMRI program files
  ClearErrors
  ReadEnvStr $JMRIHOME "JMRI_HOME"
  IfErrors 0 EnvJmriHomeDone
    ; -- If not defined, use the launcher location
    StrCpy $JMRIHOME $EXEDIR
  EnvJmriHomeDone:

  ; -- Build the ClassPath
  StrCpy $CLASSPATH ".;classes"
  StrCpy $0 "$JMRIHOME" ; normally 'C:\Program Files\JMRI'
  StrCpy $3 "jmri.jar" ; set to jmri.jar to skip jmri.jar
  StrCpy $4 "" ; no prefix required
  Call GetClassPath
  StrCmp $9 "" +2 0
  StrCpy $CLASSPATH "$CLASSPATH;$9"
  StrCpy $CLASSPATH "$CLASSPATH;jmri.jar"
  StrCpy $3 "" ; set to blank to include all .jar files
  StrCpy $4 "lib\" ; lib prefix
  StrCpy $0 "$JMRIHOME\lib" ; normally 'C:\Program Files\JMRI\lib'
  Call GetClassPath
  StrCmp $9 "" +2 0
  StrCpy $CLASSPATH "$CLASSPATH;$9"
  DetailPrint "ClassPath: $CLASSPATH"

  DetailPrint "MaxLen: ${NSIS_MAX_STRLEN}"
  DetailPrint `ExeString: "$JEXEPATH" $OPTIONS -Djava.class.path="$CLASSPATH" $CLASS $PARAMETERS`

  ; -- Finally get ready to run the application
  SetOutPath $JMRIHOME

  ; -- Launch the Java class.
  LaunchJMRI:
  DetailPrint "Launching JMRI"
  ; -- use $7 to hold return value
  ExecWait `"$JEXEPATH" $OPTIONS -Djava.class.path="$CLASSPATH" $CLASS $PARAMETERS` $7

  ; -- We're no longer active
  DetailPrint "Return code from process: $7"
  
  ; -- Check the return code is 100 - if so, re-launch
  StrCmp $7 100 LaunchJMRI
  
  ; -- Set ErrorLevel to return code
  SetErrorLevel $7
  
  Exit:
  DetailPrint "To copy this text to the clipboard, right click then choose"
  DetailPrint "  'Copy Details To Clipboard'"
SectionEnd

Function .onInit
  ; -- Get commandline parameters
  StrCpy $0 $CMDLINE
  ; -- Setup the default environment
  SetSilent silent
  StrCpy $NOISY ${SW_MINIMIZE}
  StrCpy $FORCE32BIT ${FLAG_NO}
  ; -- Start reading commandline parameters
  cmdLoop:
  Push $0
  Call GetParameters
  Pop $0
  StrCmp $0 "" 0 cmdlineOk
    MessageBox MB_OK|MB_ICONSTOP "No command line parameter. Usage 'LaunchJMRI.exe [/debug] [/noisy] [/32bit] [/profile <profileID>] [/JOPTION] class [config]'"
    Abort

  cmdlineOk:
  ; -- Check if the first parameter is an option
  Push $0
  Call GetWord
  Pop $1
  StrCpy $2 $1 1
  StrCmp $2 "/" cmdlineOptsGet
  StrCmp $2 "-" cmdlineOptsGet cmdlineOptsDone
  cmdlineOptsGet:
  ; -- Process the possible commandline options
  ; -- Strip first character
  StrCpy $2 $1 "" 1
  StrCmp $2 "debug" optsDebug
  StrCmp $2 "noisy" optsNoisy
  StrCmp $2 "32bit" opts32bit
  StrCmp $2 "profile" optsProfile
  ; -- Now check if we've got a '/J | -J' option
  StrCpy $2 $2 1
  StrCmp $2 "J" optsJVMOpts
  ; -- If we've got here, the commandline option is not known so give an error.
    MessageBox MB_OK|MB_ICONSTOP "Command line option '$1' not known."
    Abort

  ; -- Processing block for each option
  optsDebug:
  SetSilent normal
  Goto cmdLoop
  
  optsNoisy:
  StrCpy $NOISY ${SW_NORMAL}
  Goto cmdLoop
  
  opts32bit:
  StrCpy $FORCE32BIT ${FLAG_YES}
  Goto cmdLoop
  
  optsProfile:
  Push $0
  Call GetParameters
  Pop $0
  Push $0
  Call GetWord
  Pop $JMRIPROFILE
  StrCpy $2 $JMRIPROFILE 1
  StrCmp $2 '"' 0 cmdLoop
    StrCpy $JMRIPROFILE $JMRIPROFILE "" 1
    Goto cmdLoop
    
  optsJVMOpts:
  ; -- Format is '-J-Dsun.java2d.hdiaware=true'
  ; -- to pass '-Dsun.java2d.hdiaware=true'
  ; -- $1 already contains complete option with '-J' prefix
  StrCpy $2 $1 "" 2  ; strip first 2 chars
  StrCmp $JVMOPTIONS "" optsJVMcont
    ; -- add space if more than one option
    StrCpy $JVMOPTIONS `$JVMOPTIONS `
  optsJVMcont:
  StrCpy $JVMOPTIONS `$JVMOPTIONS$2`
  Goto cmdLoop

  cmdlineOptsDone:
  ; -- Read the class name
  Push $0
  Call GetWord
  Pop $CLASS
  
  ; -- Determine the application name (last part of class name)
  StrLen $1 $CLASS
  appNameLoop:
    IntOp $1 $1 - 1
    StrCpy $3 $CLASS 1 $1
    StrCmp $3 "." appNameGot
    StrCmp $1 "0" appNameDone appNameLoop
  appNameGot:
    IntOp $1 $1 + 1
  appNameDone:
    StrCpy $APPNAME $CLASS "" $1

  ; -- Now check if we've already got an instance of this application running
  
  System::Call 'kernel32::CreateMutex(i 0, i 0, t "JMRI.$CLASS") ?e'
  Pop $R0
  StrCmp $R0 0 okToLaunch
    MessageBox MB_YESNO|MB_ICONSTOP|MB_DEFBUTTON2 "JMRI $APPNAME is already running. Do you want to continue?" IDYES okToLaunch
    Abort
  okToLaunch:

  ; -- Copy any remaining commandline parameters to $PARAMETERS
  Push $0
  Call GetParameters
  Pop $PARAMETERS
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
  System::Alloc /NOUNLOAD 32
  Pop $1
  ; -- Initialise
  System::Call /NOUNLOAD "*$1(i64)"
  ; -- Make system call
  System::Call /NOUNLOAD "Kernel32::GlobalMemoryStatus(i r1)"
  ; -- Move returned info into NSIS variables
  System::Call /NOUNLOAD "*$1(i.r2, i.r3, i.r4, i.r5, i.r6, i.r7, i.r8, i.r9, i.r10)"
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
  System::Alloc /NOUNLOAD 64
  Pop $1
  ; -- Initialise
  System::Call /NOUNLOAD "*$1(i64)"
  ; -- Make system call
  System::Call /NOUNLOAD "Kernel32::GlobalMemoryStatusEx(i r1)"
  ; -- Move returned info into NSIS variables
  System::Call /NOUNLOAD "*$1(i.r2, i.r3, l.r4, l.r5, l.r6, l.r7, l.r8, l.r9, l.r10)"
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

Function GetWord
; -------------------------------------------------------------------------
; - Gets first word from a string
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
    StrCpy $R0 $R4 $R2

  Pop $R4
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0

FunctionEnd

Function CheckUserHome
; -------------------------------------------------------------------------
; - Check if the value of the registry key that Java uses to detemine
; - user.home points to the user profile directory.
; - For non NT-based systems, always return FLAG_YES
; - input:  none
; - output: result on top of stack (FLAG_YES if OK; FLAG_NO if not)
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0

  DetailPrint "Checking user.home..."
  ; -- Check if we're on Win2K or later
  ; -- If not, return OK
  StrCmp $PROFILE "" CheckUserHomeOK

  ; -- Read the registry key Java uses to determine user.home
  DetailPrint "Reading Desktop Shell Folder registry key..."
  ReadRegStr $0 HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders" "Desktop"
  DetailPrint "...read: $0"

  ; -- Check if path is equal to user profile
  DetailPrint "Checking if equal to %USERPROFILE%..."
  ; -- Retrieve parent directory
  Push $0
  Call GetParent
  Pop $0
  DetailPrint "Comparing: $0"
  DetailPrint "to: $PROFILE"
  StrCmp $0 $PROFILE CheckUserHomeOK

  ; -- Not equal
  DetailPrint "user.home not OK"
  StrCpy $0 ${FLAG_NO}
  Goto CheckUserHomeDone

  CheckUserHomeOK:
  DetailPrint "user.home OK"
  StrCpy $0 ${FLAG_YES}

  CheckUserHomeDone:
  ; -- Restore variables from the stack
  Exch $0

FunctionEnd

Function GetParent
; -------------------------------------------------------------------------
; - Return the parent directory of specified file or folder
; - input:  complete filename on top of stack
; - output: parent directory on top of stack
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Exch $0
  Push $1
  Push $2
  Push $3

  ; -- Initialise character counter and string length
  StrCpy $1 0
  StrLen $2 $0

  ; -- Loop through until right-most '\' found
  ; -- or counter >= string length
  GetParentLoop:
    ; -- Increase character counter
    IntOp $1 $1 + 1
    ; -- Check if we're at the end of the string
    IntCmp $1 $2 GetParentDir 0 GetParentDir
    ; -- Grab the character at current counter position
    ; -- working from right-hand end
    StrCpy $3 $0 1 -$1
    ; -- Check if the character is a path seperator
    StrCmp $3 "\" GetParentDir
    ; -- If not, back round again
    Goto GetParentLoop

  ; -- Strip characters from right-hand end of string
  GetParentDir:
    StrCpy $0 $0 -$1

  ; -- Restore variables from the stack
  Pop $3
  Pop $2
  Pop $1
  Exch $0

FunctionEnd
