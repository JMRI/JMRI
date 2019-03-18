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
; - Version 0.1.25.1
; - Fix bug introduced by disabling alternate launcher with JDK 11
; -------------------------------------------------------------------------
; - Version 0.1.25.0
; - Add option to use standard launcher '/noalt'
; - Disable use of alternate launcher when JDK 11 in use.
; -------------------------------------------------------------------------
; - Version 0.1.24.0
; - Add support for Java 11 Registry Keys
; -------------------------------------------------------------------------
; - Version 0.1.23.0
; - Add JVM option 'Djogamp.gluegen.UseTempJarCache=false'
; -------------------------------------------------------------------------
; - Version 0.1.22.0
; - Support Java 9
; -------------------------------------------------------------------------
; - Version 0.1.21.0
; - Alter max and initial heap size calculations to be more in-line with
; - POSIX platforms
; - Allow options to be defaulted from %userprofile%\JMRI\jmri.conf
; - Set default L&F to WindowsLookAndFeel
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
!define COPYRIGHT  "(C) 1997-2019 JMRI Community" ; Copyright string
!define VER        "0.1.25.1"                     ; Launcher version
!define PNAME      "${APP}"                       ; Name of launcher
; -- Comment out next line to use {app}.ico
!define ICON       "decpro5.ico"                  ; Launcher icon
!define INITHEAP   96                             ; Initial heap size in Mbyte
!define MINMEM     192                            ; Minimum memory in Mbyte
!define X86MAX     1024                           ; Maximum heap size for x86 in Mbyte

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
Var P_CLASSPATH ; holds additional classpath to prepend to standard
Var CLASSPATH_A ; holds additional classpath to append to standard
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
Var DEFOPTIONS ; used to hold any default options
Var ALTLAUNCH  ; used to determine use of the alternate launcher

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
  DetailPrint "Default options: $DEFOPTIONS"
  DetailPrint "AppName: $APPNAME"
  DetailPrint "Class: $CLASS"
  DetailPrint "Parameters: $PARAMETERS"
  DetailPrint "Noisy: $NOISY"
  DetailPrint "Force32bit: $FORCE32BIT"
  DetailPrint "Profile: $JMRIPROFILE"
  DetailPrint "Alternate launcher: $ALTLAUNCH"

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
    DetailPrint "Checking 'JRE'..."
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\JRE" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\JRE\$R1" "JavaHome"
    IfErrors 0 FoundJavaInstallPoint
    DetailPrint "Checking 'Java Runtime Environment'..."
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    IfErrors 0 FoundJavaInstallPoint
    DetailPrint "Checking 'JDK'..."
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\JDK" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\JDK\$R1" "JavaHome"
    ; -- JDK 11 doesn't seem to like our 'default' behaviour of running from
    ; -- a temp directory with a renamed 'java.exe' so switch that off here
    ; -- We only need to do this if JDK has been found
    IfErrors 0 DisableAltLauncher
    ; -- As we've just cleared the error flag, we need to set it again
    ; -- otherwise we'll think that we've found an installation
    ; -- Gotta love the spaghetti...
    SetErrors
    Goto FoundJavaInstallPoint
    
  DisableAltLauncher:
    DetailPrint "Switching off alternate launcher..."
    StrCpy $ALTLAUNCH ${FLAG_NO}

  FoundJavaInstallPoint:
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
    DetailPrint "Java not found!"
    MessageBox MB_OK|MB_ICONSTOP "Java not found!"
    Goto Exit

  JreFound:
  StrCpy $JAVAPATH $R0
  DetailPrint "JavaPath: $JAVAPATH"

  ; -- Now we've determined Java is basically OK, copy the file to a
  ; -- temporary location and rename it if configured to do so
  StrCmp ${FLAG_NO} $ALTLAUNCH UseStandardLauncher

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
  ; -- Wasn't found so use regular launcher
  IfErrors UseStandardLauncher ExeRenameDone

  UseStandardLauncher:
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
  ; -- Default Java heap size is 1/4 total memory size
  ; -- Now it would be good to read this info from the JVM, but
  ; -- it's not as simple to do that compared to the methods available
  ; -- on POSIX systems so, for the time being, we will just assume
  ; -- that the default calculation is performed by the JVM

  ; -- Our required memory calculations will be as follows:
  ; -- - 1/4 total memory size on systems with more than 4GB RAM
  ; -- - 1/2 total memory size on systems with 1-4GB RAM
  ; -- - 3/4 total memory size on systems with less than 1GB RAM
  ; -- - with an absolute minimum of 192MB (MINMEM)
  ; -- If running on an x86 JVM, we peg the maximum to ${X86MAX}
  ; -- as, it seems, the x86 JVM cannot always allocate a larger
  ; -- amount of RAM even if there is sufficient on the machine

  ; -- Check that physical memory is >= MINMEM
  IntCmp $4 ${MINMEM} cmp_mem_gt_4 cmp_min_lt cmp_mem_gt_4
  cmp_min_lt:
    ; -- If insufficient physical memory, stop with an error
    MessageBox MB_OK|MB_ICONSTOP "Not enough available memory to start. JMRI requires at least ${MINMEM} MBytes memory."
    Goto Exit
  cmp_mem_gt_4:
    ; -- Check if physical memory is greater than 4GB
    DetailPrint "Check if more than 4GB memory"
    IntCmp $4 4096 cmp_mem_bt_1_and_4 cmp_mem_bt_1_and_4
    ; -- More than 4GB so use 1/4 memory size
    IntOp $CALCMAXMEM $4 / 4
    DetailPrint "More than 4GB"
    Goto cmp_done
  cmp_mem_bt_1_and_4:
    ; -- Check if physical memory is greater than 1GB
    DetailPrint "Less than 4GB"
    DetailPrint "Check if more than 1GB memory"
    IntCmp $4 1024 cmp_mem_lt_1 cmp_mem_lt_1
    ; -- More than 1GB so use 1/2 memory size
    IntOp $CALCMAXMEM $4 / 2
    DetailPrint "More than 1GB"
    Goto cmp_done
  cmp_mem_lt_1:
    DetailPrint "Less than 1GB"
    ; -- Less than 1GB so use 3/4 memory size
    IntOp $CALCMAXMEM $4 * 3
    IntOp $CALCMAXMEM $CALCMAXMEM / 4
  cmp_done:
  DetailPrint "InitHeap: ${INITHEAP}m"
  DetailPrint "MinMemory: ${MINMEM}m"
  DetailPrint "MaxMemory: $CALCMAXMEMm"

  ; -- Check if we're on a 32-bit JVM and adjust max heap down if necessary
  DetailPrint "Checking maximum heap size..."
  StrCmp $x64JRE ${ARCH_64BIT} check_heap_done
    ; -- we're on a 32-bit JRE, so check the calculated heap size
    DetailPrint "Running x86 JVM"
    IntCmp $CALCMAXMEM ${X86MAX} check_heap_done check_heap_done
    StrCpy $CALCMAXMEM ${X86MAX}
    DetailPrint "Adjusted MaxMemory: $CALCMAXMEMm"
  check_heap_done:
  DetailPrint "...finished"

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
  StrCpy $OPTIONS "$OPTIONS -Djogamp.gluegen.UseTempJarCache=false"
  StrCpy $OPTIONS "$OPTIONS -Djinput.plugins=net.bobis.jinput.hidraw.HidRawEnvironmentPlugin"
  StrCpy $OPTIONS "$OPTIONS -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
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
  StrCpy $OPTIONS "$OPTIONS -Xms${INITHEAP}m"
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

  ; -- Now prepend and/or append when required
  DetailPrint "Check for any prepended/appended classpath entries"
  StrCmp $P_CLASSPATH "" ClassPathAppend
  StrCpy $CLASSPATH "$P_CLASSPATH;$CLASSPATH"
  DetailPrint "Prepended $P_CLASSPATH"
  ClassPathAppend:
  StrCmp $CLASSPATH_A "" ClassPathDone
  StrCpy $CLASSPATH "$CLASSPATH;$CLASSPATH_A"
  DetailPrint "Appended $CLASSPATH_A"

  ClassPathDone:
  DetailPrint "Final ClassPath: $CLASSPATH"

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
  ; -- Setup the default environment
  SetSilent silent
  StrCpy $NOISY ${SW_MINIMIZE}
  StrCpy $FORCE32BIT ${FLAG_NO}
  StrCpy $ALTLAUNCH ${FLAG_YES}
  ; -- Read any default_options
  Call ReadConfFile
  Pop $DEFOPTIONS
  ; -- Check if we've got some
  StrCmp $DEFOPTIONS "" cmdlineProcess
  ; -- If so, process them
  StrCpy $0 $DEFOPTIONS
  Call ProcessParameters
  ; -- Now process commandline
  cmdlineProcess:
  ; -- Get commandline parameters
  StrCpy $0 $CMDLINE
  ; -- Start reading commandline parameters
  cmdLoop:
  Push $0
  Call GetParameters
  Pop $0
  StrCmp $0 "" 0 cmdlineOk
    MessageBox MB_OK|MB_ICONSTOP "No command line parameter. Usage 'LaunchJMRI.exe [/debug] [/noisy] [/32bit] [/profile <profileID>] [/JOPTION] [--cp:a=CLASSPATH] [--cp:p=CLASSPATH] class [config]'"
    Abort

  cmdlineOk:
  Call ProcessParameters
  IfErrors 0 cmdLoop

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

Function ProcessParameters
; -------------------------------------------------------------------------
; - Processes parameters
; - input:  parameter string in $0
; - output: none
; - modifies $0, $1, $2
; -------------------------------------------------------------------------
  ; -- Check if the first parameter is an option
  Push $0
  Call GetWord
  Pop $1
  StrCpy $2 $1 1
  StrCmp $2 "/" optsGet
  StrCmp $2 "-" optsGet optsDone
  optsGet:
  ; -- Process the possible commandline options
  ; -- Strip first character
  StrCpy $2 $1 "" 1
  StrCmp $2 "debug" optsDebug
  StrCmp $2 "noisy" optsNoisy
  StrCmp $2 "32bit" opts32bit
  StrCmp $2 "profile" optsProfile
  StrCmp $2 "noalt" optsNoAlt
  ; -- Now check if we've got a '/J | -J' option
  StrCpy $2 $2 1
  StrCmp $2 "J" optsJVMOpts
  ; -- Now check if we've got a '--cp:a= | --cp:p=' option
  ; -- Start from complete option in $1
  StrCpy $2 $1 7
  StrCmp $2 "--cp:a=" optsCPA
  StrCmp $2 "--cp:p=" optsPCP
  ; -- If we've got here, the commandline option is not known so give an error.
    MessageBox MB_OK|MB_ICONSTOP "Command line option '$1' not known."
    Abort

  ; -- Processing block for each option
  optsDebug:
  SetSilent normal
  Return

  optsNoisy:
  StrCpy $NOISY ${SW_NORMAL}
  Return

  opts32bit:
  StrCpy $FORCE32BIT ${FLAG_YES}
  Return

  optsProfile:
  Push $0
  Call GetParameters
  Pop $0
  Push $0
  Call GetWord
  Pop $JMRIPROFILE
  StrCpy $2 $JMRIPROFILE 1
  StrCmp $2 '"' 0 optsProfile_Done
    StrCpy $JMRIPROFILE $JMRIPROFILE "" 1
    optsProfile_Done:
    Return

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
  Return

  optsCPA:
  ; -- Format is '--cp:a=CLASSPATH'
  ; -- to append 'CLASSPATH' to classpath
  ; -- $1 already contains complete option with '--cp:a=' prefix
  StrCpy $CLASSPATH_A $1 "" 7 ; strip first 7 chars
  Return

  optsPCP:
  ; -- Format is '--cp:p=CLASSPATH'
  ; -- to prepend 'CLASSPATH' to classpath
  ; -- $1 already contains complete option with '--cp:p=' prefix
  StrCpy $P_CLASSPATH $1 "" 7 ; strip first 7 chars
  Return
  
  optsNoAlt:
  StrCpy $ALTLAUNCH ${FLAG_NO}
  Return

  optsDone:
  ; -- No more parameters to process
  ; -- so set error flag to signify
  SetErrors
FunctionEnd

Function ReadConfFile
; -------------------------------------------------------------------------
; - Gets default_options from '%userprofile%\JMRI\jmri.conf'
; - input:  none
; - output: top of stack
; - modifies no other variables
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0
  Push $1
  Push $2

  ClearErrors
  FileOpen $2 "$PROFILE\JMRI\jmri.conf" r
  IfErrors ReadConfFile_Exit

  ReadConfFile_ReadLine:
    ClearErrors
    FileRead $2 $1
    IfErrors ReadConfFile_done
    StrCpy $0 $1 1
    ; -- Skip any comments (line begins `#`)
    StrCmp $0 "#" ReadConfFile_ReadLine
    ; -- Remove any trailing whitespace
  ReadConfFile_WhiteSpaceLoop:
    StrCpy $0 $1 1 -1
    StrCmp $0 ` ` ReadConfFile_TrimRight
    StrCmp $0 `$\t` ReadConfFile_TrimRight
    StrCmp $0 `$\r` ReadConfFile_TrimRight
    StrCmp $0 `$\n` ReadConfFile_TrimRight
    Goto ReadConfFile_WhiteSpaceDone
  ReadConfFile_TrimRight:
    StrCpy $1 $1 -1
    Goto ReadConfFile_WhiteSpaceLoop
  ReadConfFile_WhiteSpaceDone:
  ; -- now parse
  StrCpy $0 $1 16
  ; -- check if line is `default_options=`, otherwise skip
  StrCmp $0 "default_options=" 0 ReadConfFile_ReadLine
  ; -- check first character is a quote
  StrCpy $0 $1 1 16
  ; -- if so, continue, otherwise skip
  StrCmp $0 `"` 0 ReadConfFile_ReadLine
  ; -- check last character is a quote
  StrCpy $0 $1 1 -1
  ; -- if so, continue, otherwise skip
  StrCmp $0 `"` 0 ReadConfFile_ReadLine
  ; -- grab options string
  StrCpy $0 $1 -1 17

  ReadConfFile_done:
    FileClose $2
  ReadConfFile_exit:
    ClearErrors

  ; -- Restore variables from the stack
  Pop $2
  Pop $1
  Exch $0

FunctionEnd

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
