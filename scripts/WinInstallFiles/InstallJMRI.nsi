; -------------------------------------------------------------------------
; - Install JMRI
; -------------------------------------------------------------------------
; - This is used to install JMRI (a Java application) on Windows.
; - It will ensure that JAVA is installed - if not, it will connect to the
; - internet to download JAVA.
; -
; - An off-line installation option is also possible - place the previously
; - downloaded off-line JRE installation program into a separate JRE folder
; - alongside the JMRI installation program.
; -
; - It will also check for a previous installation of JMRI and will backup
; - any preferences and roster files etc.
; - Also, if previous version was installed using InstallerVise, will run
; - that uninstaller first
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Compilation instructions
; -------------------------------------------------------------------------
; - For the default setting of "SRCDIR", place this file in directory
; - 'Distribution\Windows\JMRI' prior to building the distribution .exe file.
; -
; - To build the resulting .exe file use one of:
; -   makensis.exe  (command-line)
; -   makensisw.exe (Windows GUI)
; -
; - These are part of the NSIS installation suite available from:
; -   http://nsis.sourceforge.net/
; -
; - makensis can also be compiled on POSIX systems such that the installer
; - .exe file can be built on such a platform.
; - For further details, refer to:
; -   http://nsis.sourceforge.net/Docs/AppendixG.html#G.3
; -   http://cho.hapgoods.com/wordpress/?p=138
; -
; - This installer makes use of the following NSIS plugins and macros
; - that are all included with the NSIS v2.35 or later distribution:
; -   ModernUI 2.0
; -   MultiUser
; -   System
; -   nsisDL
; -   WordFunc
; -
; - A good NSIS editor (and the one used when developing this installer)
; - is 'HM NIS Edit' available from:
; -   http://hmne.sourceforge.net/
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Version History
; -------------------------------------------------------------------------
; - Version 0.1.25.0
; - Backup and remove lib folder
; -------------------------------------------------------------------------
; - Version 0.1.24.1
; - Correct the support for Java 11 Registry Keys
; -------------------------------------------------------------------------
; - Version 0.1.24.0
; - Add support for Java 11 Registry Keys
; -------------------------------------------------------------------------
; - Version 0.1.22.15
; - Backup and remove classes folder
; -------------------------------------------------------------------------
; - Version 0.1.22.14
; - Remove insecure Jackson libraries to address CVE-2017-17485
; -------------------------------------------------------------------------
; - Version 0.1.22.13
; - Remove outmoded SLF4J libraries
; -------------------------------------------------------------------------
; - Version 0.1.22.12
; - Remove outmoded Jetty and WebSocket libraries
; -------------------------------------------------------------------------
; - Version 0.1.22.11
; - Remove outmoded lib\ch.ntb.usb.jar
; -------------------------------------------------------------------------
; - Version 0.1.22.10
; - Support Java 9
; -------------------------------------------------------------------------
; - Version 0.1.22.9
; - Remove outmoded lib\jna-4.2.2.jar and install jmri.conf
; - Remove RXTX and SerialIO files as now replaced by purejavacomm
; -------------------------------------------------------------------------
; - Version 0.1.22.8
; - Remove outmoded jackson files
; -------------------------------------------------------------------------
; - Version 0.1.22.7
; - Remove outmoded jython files
; -------------------------------------------------------------------------
; - Version 0.1.22.6
; - Remove outmoded vecmath files
; -------------------------------------------------------------------------
; - Version 0.1.22.5
; - Remove more outmoded jetty files
; -------------------------------------------------------------------------
; - Version 0.1.22.4
; - Remove more outmoded jetty files
; -------------------------------------------------------------------------
; - Version 0.1.22.3
; - Fix decpro5 file name (.ico missing)
; -------------------------------------------------------------------------
; - Version 0.1.22.2
; - Remove references to DecoderPro3
; - Remove download of Java as this hasn't worked properly for a while
; - instead halt with a message to download if not found
; -------------------------------------------------------------------------
; - Version 0.1.22.1
; - Remove old JOAL libraries
; - Change minimum JRE version to 1.8
; -------------------------------------------------------------------------
; - Version 0.1.22.0
; - Remove support for installing the old DecoderPro
; -------------------------------------------------------------------------
; - Version 0.1.21.5
; - Updated "-CleanUp" so that another set of old files are removed,
;    including:
;       jetty(release 8.1.0.v20120127)-related libraries
;       old log4j library
;       jspwin DLL
;    are removed before installation.
; -------------------------------------------------------------------------
; - Version 0.1.21.4
; - Updated "-CleanUp" so that any previous jetty-related .jar files and
;   servlet .jar files are removed before installation.
; - Delete files related to XmlIO which is removed as of JMRI 3.11.3
; - Delete files which were removed as part of re-work of Zimo decoder
;   definition rework for JMRI 3.11.3
; -------------------------------------------------------------------------
; - Version 0.1.21.3
; - Updated "-CleanUp" so that any previous slf4j .jar files are removed
; -   before installation.
; -------------------------------------------------------------------------
; - Version 0.1.21.2
; - Updated "-CleanUp" so that any previous desktop icons are removed before
; -   installation.
; - Updated "-CleanUp" so that if the Start Menu folder already exits, it
; -   is removed before installation.
; - Provided work-around for deletion of Preferences.lnk on systems which
; -   are slow to change file attributes.
; -------------------------------------------------------------------------
; - Version 0.1.21.1
; - Updated Uninstall operations so that DecoderPro3 icons on Desktop and in
; - Start Menu are deleted during Unninstall process
; -------------------------------------------------------------------------
; - Version 0.1.21.0
; - Add request for administrator level access when installing to correct
; - for bug highlighted by Suzie Tall
; -------------------------------------------------------------------------
; - Version 0.1.20.1
; - Added removal of obsolete library 'lib\servlet.jar'
; -------------------------------------------------------------------------
; - Version 0.1.20.0
; - Update installer to no longer install on Windows 98 and Windows ME
; - Change JRE version and copyright date to be determined by build.xml
; - ant script rather than being hard-coded here
; -------------------------------------------------------------------------
; - Version 0.1.19.0
; - Remove DecoderPro desktop shortcut from standard installation - now
; - only installed when selected
; -------------------------------------------------------------------------
; - Version 0.1.18.0
; - Remove DecoderPro3 from Tools and Demos and place in main JMRI start
; - menu along with Desktop shortcut
; -------------------------------------------------------------------------
; - Version 0.1.17.0
; - Change branding text
; -------------------------------------------------------------------------
; - Version 0.1.16.0
; - Add start menu shortcut for DecoderPro3
; -------------------------------------------------------------------------
; - Version 0.1.15.0
; - Remove shortcut to LocoTools JMRI application
; -------------------------------------------------------------------------
; - Version 0.1.14.0
; - Enabled the JMRI version to be determined by the ant dist.xml script
; - as opposed to being hard-coded in here
; - Update Multi-user installation configuration to default to the previous
; - settings when upgrading (i.e. Current User or All Users)
; - Updated URL for Windows 98 & ME JRE download to 1.5.0_22; was 1.5.0_17
; - see:
; -   http://www.java.com/en/download/faq/win98_me.xml
; -   http://www.java.com/en/download/windows98me_manual.jsp
; - As the build version is not checked, this will only be downloaded where
; - either no JRE exists or a version earlier than 1.5 is installed.
; -------------------------------------------------------------------------
; - Version 0.1.13.0
; - Removal of obsolete .bat and resource files
; -------------------------------------------------------------------------
; - Version 0.1.12.0
; - Update to remove comm.jar if found in JMRI\lib folder
; -------------------------------------------------------------------------
; - Version 0.1.11.0
; - Update to remove WiThrottle plug-in if previously installed
; - Modify backup check to default to off when installing over newer or
; - identical version
; - Changed backup behaviour to keep two backups:
; -  - JMRI_backup
; -  - JMRI_backup_old
; -------------------------------------------------------------------------
; - Version 0.1.10.0
; - Update to correctly identify JRE architecture when installing on 64-bit
; - systems.
; -------------------------------------------------------------------------
; - Version 0.1.9.0
; - Update installer for new library structure
; -------------------------------------------------------------------------
; - Version 0.1.8.0
; - Update installer for migration to new version of RXTX ensuring that
; - correct version of the .dll is installed for 32-bit or 64-bit
; - Also, changed the SerialIO installation to be localised to the JMRI
; - program directory, rather than in the system-wide JRE
; -------------------------------------------------------------------------
; - Version 0.1.7.0
; - Update to ensure removal of crimson.jar library file in both old and
; - new file layouts
; -------------------------------------------------------------------------
; - Version 0.1.6.0
; - Corrected an error where old install location was not being read back
; - if previously installed.
; - Improved check to avoid installation into Program Files root.
; - Updated installer to ensure JMRI uses the same library file layout as
; - on Linux and Mac OS X platforms.
; - Added additional obsolete decoder definitions to delete.
; - Use new icon for InstallTest application.
; - Add Start menu shortcut and optional Desktop shortcut for SoundPro
; - application.
; -------------------------------------------------------------------------
; - Version 0.1.5.0
; - Incorporated new routine to delete obsolete decoder definitions.
; - At the moment, it is purely done by deleting specified files from the
; - xml\decoders directory.
; -------------------------------------------------------------------------
; - Version 0.1.4.0
; - For a post-2.5.2 upgrade, defaulted the backup of preference and roster
; - files to be selected and also made this optional. Current behaviour
; - remains unchanged for a pre-2.5.2 upgrade (i.e. uninstallation with
; - backup is mandatory and must be explicitly selected by the user)
; -------------------------------------------------------------------------
; - Version 0.1.3.0
; - Removed the 'skipping' of program location and start menu selection
; - screens in all installation variants.
; - Renamed the 'Custom' installer
; - Enabled the install location to be read from the stored location in the
; - registry so that an upgrade will use that location, as opposed to the
; - default when installing.
; - General clean-up of obsolete code.
; -------------------------------------------------------------------------
; - Version 0.1.2.0
; - Modified offline JRE installer for Windows 98 and ME to look for a JRE
; - installer in the JRE_98ME sub-directory. This allows for a single CD to
; - be created for offline distribution.
; -------------------------------------------------------------------------
; - Version 0.1.1.0
; - Tidied up the previous version check.
; -------------------------------------------------------------------------
; - Version 0.1.0.0
; - First release for production.
; - Incorporated saving of the log to allow for cleaner uninstall.
; - Uninstaller now removes only those files and directories installed.
; - TODO: Remove reliance on access to global 'Program Files' directory
; -------------------------------------------------------------------------
; - Version 0.0.4.0
; - Reverted back to Multi-user installer with JMRI always installed into
; - 'Program Files' (need to come back and look at the scenario where a
; - user has insufficient rights to install there).
; - Removed most Desktop shortcuts and created a new 'Tools and Demos'
; - start menu folder containing less commonly used items plus a new
; - shortcut to the users Preferences folder.
; - Modified old InstallVise installer check to run the InstallVise
; - uninstaller first (with a backup of all settings and user files).
; -------------------------------------------------------------------------
; - Version 0.0.3.0
; - Removed the 'Current user' option for now as the exact behaviour of the
; - installer under Vista is not yet clear.
; - Removed custom pages and reliance on UAC and AccessControl plugins.
; -------------------------------------------------------------------------
; - Version 0.0.2.0
; - Converted to use ModernUI 2.0 (available as from NSIS v2.34).
; - Changed to use nsDialogs for custom pages instead of InstallOptions.
; - For installation on Vista, now using UAC and AccessControl plugins to
; - deal with gaining appropriate permissions for installer actions.
; -------------------------------------------------------------------------
; - Version 0.0.1.0
; - First internal test release.
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Basic information
; - These should be edited to suit the application
; -------------------------------------------------------------------------
!define AUTHOR    "Matt Harris for JMRI"        ; Author name
!define APP       "JMRI"                        ; Application name
!ifndef JMRI_COPY
  ; -- usually, this will be determined by the build.xml ant script
  !define JMRI_COPY  "by"                       ; Copyright dates
!endif
!define COPYRIGHT "(c) ${JMRI_COPY} JMRI Community"  ; Copyright string
!ifndef JMRI_VER
  ; -- usually, this will be determined by the build.xml ant script
  !define JMRI_VER  "unknown"                   ; Application version
!endif
!ifndef RELEASEDIR
  ; -- usually, this will be determined by the build.xml ant script
  !define RELEASEDIR ".."
!endif
!ifndef JRE_VER
  ; -- usually, this will be determined by the build.xml ant script
  !define JRE_VER   "1.8"                       ; Required JRE version
!endif
!define INST_VER  "0.1.25.0"                    ; Installer version
!define PNAME     "${APP}.${JMRI_VER}"          ; Name of installer.exe
!define SRCDIR    "."                           ; Path to head of sources
InstallDir        "$PROGRAMFILES\JMRI"          ; Default install directory

; -------------------------------------------------------------------------
; - End of basic information
; - Lines below here should not normally require editing
; -------------------------------------------------------------------------

; -------------------------------------------------------------------------
; - Variable declarations
; -------------------------------------------------------------------------
Var JAVADIR ; holds the path to the location where JAVA files can be found
Var SMFOLDER ; holds the returned Start Menu Folder
Var JREINSTALLER ; holds the path to the location of the JRE installer
Var OFFLINEINSTALL ; a flag determining if this is an offline install
Var JREINSTALLCOUNT ; a counter holding times around the JRE install loop
Var REMOVEOLDINSTALL ; a flag to determine if old installer to be removed
Var REMOVEOLDJMRI.BACKUPONLY ; a flag to determine if we should back-up
Var UPGRADING ; a flag to determine if we are upgrading
Var x64 ; flag to determine if we're installing in x64 or not
Var x64JRE ; flag to determine if we've got a 64-bit JRE

; -------------------------------------------------------------------------
; - Compiler Flags (to reduce executable size, saves some bytes)
; -------------------------------------------------------------------------
SetDatablockOptimize on
SetCompress force
SetCompressor /SOLID /FINAL lzma

; -------------------------------------------------------------------------
; - Defines for downloading
; -------------------------------------------------------------------------
!define JRE_URL     "http://java.com/winoffline_installer/"
!define INTERNET_CONNECTION_CONFIGURED 64  ; 0x40
!define INTERNET_CONNECTION_LAN 2          ; 0x02
!define INTERNET_CONNECTION_MODEM 1        ; 0x01
!define INTERNET_CONNECTION_OFFLINE 32     ; 0x20
!define INTERNET_CONNECTION_PROXY 4        ; 0x04
!define INTERNET_RAS_INSTALLED 16          ; 0x10

; -------------------------------------------------------------------------
; - Defines for multi-user installation
; -------------------------------------------------------------------------
!define MULTIUSER_EXECUTIONLEVEL Highest
!define MULTIUSER_MUI ; allow for use with ModernUI
!define MULTIUSER_INSTALLMODE_COMMANDLINE
!define MULTIUSER_INIT_TEXT_ALLUSERSNOTPOSSIBLE "Your user account does not have sufficient privileges to uninstall $(^Name) for all users of this computer."
!define MULTIUSER_INSTALLMODE_DEFAULT_REGISTRY_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI"
!define MULTIUSER_INSTALLMODE_DEFAULT_REGISTRY_VALUENAME ""

; -------------------------------------------------------------------------
; - Includes
; -------------------------------------------------------------------------
!include "MultiUser.nsh" ; MultiUser installation
!include "WordFunc.nsh" ; add header for word manipulation
!insertmacro VersionCompare ; add function to compare versions

; -------------------------------------------------------------------------
; - Defines for log saving
; -------------------------------------------------------------------------
!ifndef LVM_GETITEMCOUNT
!define LVM_GETITEMCOUNT 0x1004
!endif
!ifndef LVM_GETITEMTEXT
!define LVM_GETITEMTEXT 0x102D
!endif

; -------------------------------------------------------------------------
; - Runtime Switches
; -------------------------------------------------------------------------
CRCCheck On ; do CRC check on launcher before start ("Off" for later EXE compression)
ShowInstDetails Hide ; Hide the installation details
ShowUninstDetails Hide ; Hide the uninstallation details
AutoCloseWindow False ; do not automatically close when finished
RequestExecutionLevel admin ; Request administrator level access

; -------------------------------------------------------------------------
; - Set basic information
; -------------------------------------------------------------------------
Name "${APP} ${JMRI_VER}"
Caption "${APP} ${JMRI_VER} Setup"
BrandingText "${APP} - ${COPYRIGHT}"
OutFile "${RELEASEDIR}/${PNAME}.exe"

; -------------------------------------------------------------------------
; - Interface Settings
; -------------------------------------------------------------------------
!ifdef ICON
  !define MUI_ICON "${ICON}"
!else
  !define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!endif
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"
!define MUI_ABORTWARNING
!define MUI_UNABORTWARNING
!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange-uninstall.bmp"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\orange.bmp"
!define MUI_HEADERIMAGE_UNBITMAP "${NSISDIR}\Contrib\Graphics\Header\orange-uninstall.bmp"
!define MUI_COMPONENTSPAGE_SMALLDESC
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "JMRI"
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "SHCTX"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "StartMenuDir"
!define MUI_FINISHPAGE_NOAUTOCLOSE
#!define MUI_FINISHPAGE_RUN
#!define MUI_FINISHPAGE_RUN_TEXT "Execute DecoderPro Install Validation Wizard"
#!define MUI_FINISHPAGE_RUN_FUNCTION InstallValidationWizard
!define MUI_UNFINISHPAGE_NOAUTOCLOSE

; -------------------------------------------------------------------------
; - Define pages
; -------------------------------------------------------------------------
!define MUI_TEXT_WELCOME_INFO_TEXT "This wizard will guide you through the installation of $(^NameDA).$\r$\n$\r$\nIt is recommended that you close all other applications before starting Setup. This will make it possible to update relevant system files without having to reboot your computer.$\r$\n$\r$\n$(^NameDA) requires Java Runtime Environment ${JRE_VER} or later installed on your computer. During installation, this wizard will check for Java.$\r$\n$\r$\n$_CLICK"
!insertmacro MUI_PAGE_WELCOME
Page custom nsDialogRemoveOldJMRI RemoveOldJMRI
!insertmacro MULTIUSER_PAGE_INSTALLMODE
!insertmacro MUI_PAGE_COMPONENTS
!define MUI_PAGE_CUSTOMFUNCTION_LEAVE DirectoryLeave
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU JMRIStartMenu $SMFOLDER
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

InstType "Typical"
InstType "Full"
; -------------------------------------------------------------------------
; - Languages
; -------------------------------------------------------------------------
!insertmacro MUI_LANGUAGE "English"

; -------------------------------------------------------------------------
; - Reserve files (note some are already reserved by MUI2 and MultiUser)
; -------------------------------------------------------------------------
; - If solid compression is being used, files that are required before the
; - actual installation itself should be stored in the first data block -
; - this will ensure that the installer starts faster
; -------------------------------------------------------------------------
ReserveFile /plugin "System.dll"
ReserveFile /plugin "NSISdl.dll"
ReserveFile /plugin "UserInfo.dll"

; -------------------------------------------------------------------------
; - Set version information
; -------------------------------------------------------------------------
VIProductVersion "${INST_VER}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "${APP} Install"
VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "Installs JMRI ${JMRI_VER} suite."
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "${COPYRIGHT}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "by ${AUTHOR}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "${APP} Install"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${INST_VER}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "OriginalFilename" "${PNAME}.exe"

; -------------------------------------------------------------------------
; - Installer sections
; -------------------------------------------------------------------------
SectionGroup "JMRI Core Files" SEC_CORE

  Section "-CleanUp" ; SEC_CLEANUP
    SectionIn RO  ; This section always selected

    ; -- Clean up of JMRI folder
    SetOutPath "$INSTDIR"

    ; -- Recursively delete classes folder, which historically contained 
    ; -- individual .properties and .classes patch files
    ; -- that might not be consistent with this new version
    RMDir /R "$OUTDIR\classes"

    ; -- Recursively delete lib folder, which historically contained
    ; -- individually added JAR files, but which we have been encouraging
    ; -- users not to modify
    RMDir /R "$OUTDIR\lib"

    ; -- Delete old USB library files
    Delete "$OUTDIR\ch.ntb.usb.jar"

    ; -- Delete old .jar & support files in destination directory
    Delete "$OUTDIR\jh.1.1.2.jar"
    Delete "$OUTDIR\jh.jar"
    Delete "$OUTDIR\jdom-jdk11.jar"
 
    ; -- Delete XmlIO-related files, as of JMRI 3.11.3
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\2Throttles.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\AnalogClock.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\Help.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\inControlIcon.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\LocoSettings.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\Panel.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\QueryStringHelp.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\Scan2BeInControl.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\Settings.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\Throttle1.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\Throttle2.png"
    Delete "$OUTDIR\help\en\package\jmri\jmrit\inControl\images\.png"
    RmDir  "$OUTDIR\help\en\package\jmri\jmrit\inControl\images"
    RmDir  "$OUTDIR\help\en\package\jmri\jmrit\inControl"

    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\2Throttles.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\AnalogClock.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\Help.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\inControlIcon.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\LocoSettings.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\Panel.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\QueryStringHelp.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\Scan2BeInControl.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\Settings.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\Throttle1.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\Throttle2.png"
    Delete "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images\.png"
    RmDir  "$OUTDIR\help\fr\package\jmri\jmrit\inControl\images"
    RmDir  "$OUTDIR\help\fr\package\jmri\jmrit\inControl"

    Delete "$OUTDIR\web\css\inControl.css"
    Delete "$OUTDIR\web\css\JMRI_XMLIO_TEST.css"
    Delete "$OUTDIR\web\css\Scan2BeInControl.css"
    Delete "$OUTDIR\web\js\inControl.js"
    Delete "$OUTDIR\web\js\JMRI_XMLIO_TEST.js"
    Delete "$OUTDIR\web\js\Scan2BeInControl.js"
    Delete "$OUTDIR\web\inControl.html"
    Delete "$OUTDIR\web\inControl.jar"
    Delete "$OUTDIR\web\JMRI_XMLIO_test.html"
    Delete "$OUTDIR\web\Scan2BeInControl.html"

    ; Delete files which were removed as part of re-work of Zimo decoder
    ;   definition rework for JMRI 3.11.3
    Delete "$OUTDIR\xml\decoders\zimo\CV739-CV744threeSwitchIP.xml"
    Delete "$OUTDIR\xml\decoders\zimo\CV739-CV744twoSwitchIP.xml"
    Delete "$OUTDIR\xml\decoders\zimo\CV739-CV768.xml"

    ; -- Delete .jar & support files installed using previous layout
    Delete "$OUTDIR\activation.jar"
    Delete "$OUTDIR\ch.ntb.usb.jar"
    Delete "$OUTDIR\comm.jar"
    Delete "$OUTDIR\crimson.jar"
    Delete "$OUTDIR\ExternalLinkContentViewerUI.jar"
    Delete "$OUTDIR\gluegen-rt.dll"
    Delete "$OUTDIR\gluegen-rt.jar"
    Delete "$OUTDIR\javacsv.jar"
    Delete "$OUTDIR\javax.comm.properties"
    Delete "$OUTDIR\jdom-2.0.5.jar"
    Delete "$OUTDIR\jdom.jar"
    Delete "$OUTDIR\jhall.jar"
    Delete "$OUTDIR\jinput-dx8.dll"
    Delete "$OUTDIR\jinput-raw.dll"
    Delete "$OUTDIR\jinput-wintab.dll"
    Delete "$OUTDIR\jinput.jar"
    Delete "$OUTDIR\joal.jar"
    Delete "$OUTDIR\joal_native.dll"
    Delete "$OUTDIR\jspWin.dll"
    Delete "$OUTDIR\jython.jar"
    Delete "$OUTDIR\LibusbJava.dll"
    Delete "$OUTDIR\log4j.jar"
    Delete "$OUTDIR\mailapi.jar"
    Delete "$OUTDIR\MRJAdapter.jar"
    Delete "$OUTDIR\security.policy"
    Delete "$OUTDIR\Serialio.jar"
    Delete "$OUTDIR\servlet.jar"
    Delete "$OUTDIR\smtp.jar"
    Delete "$OUTDIR\vecmath.jar"
    Delete "$OUTDIR\win32com.dll"
    Delete "$OUTDIR\xercesImpl.jar"

    ; -- Delete old plug-ins from program folder
    Delete "$OUTDIR\WiThrottle.jar"

    ; -- Delete old log files from program folder
    Delete "$OUTDIR\messages.log"
    Delete "$OUTDIR\uninstal.log" ; from InstallerVise installer

    ; -- Delete obsolete .bat files from program folder
    Delete "$OUTDIR\CornwallRR.bat"
    Delete "$OUTDIR\DecoderPro.bat"
    Delete "$OUTDIR\JmriDemo.bat"
    Delete "$OUTDIR\LocoTools.bat"
    Delete "$OUTDIR\PacketPro.bat"
    Delete "$OUTDIR\PanelPro.bat"
    Delete "$OUTDIR\SoundPro.bat"

    ; -- Delete obsolete resource files
    Delete "$OUTDIR\resources\GreenPowerLED.gif"
    Delete "$OUTDIR\resources\RedPowerLED.gif"
    Delete "$OUTDIR\resources\YellowPowerLED.gif"

    ; -- If the current install Start Menu folder exists, remove any
    ; --        predictably-named JMRI-related contents.

    !insertmacro MUI_STARTMENU_GETFOLDER JMRIStartMenu $0

    ; -- Change file attributes early so they have time to propagate on systems
    ; --        with unexpected delays in the filesystem.
    SetFileAttributes "$SMPROGRAMS\$0\Tools and Demos\Preferences.lnk" NORMAL

    Delete "$SMPROGRAMS\$0\DecoderPro.lnk"
    Delete "$SMPROGRAMS\$0\DecoderPro3.lnk"
    Delete "$SMPROGRAMS\$0\PanelPro.lnk"
    Delete "$SMPROGRAMS\$0\SoundPro.lnk"
    Delete "$SMPROGRAMS\$0\Tools and Demos\JmriDemo.lnk"
    Delete "$SMPROGRAMS\$0\Tools and Demos\LocoTools.lnk"
    Delete "$SMPROGRAMS\$0\Tools and Demos\CornwallRR.lnk"
    Delete "$SMPROGRAMS\$0\Tools and Demos\InstallTest.lnk"
    Delete "$SMPROGRAMS\$0\Tools and Demos\InstallTest.pif" ; -- for Win98
    Delete "$SMPROGRAMS\$0\Tools and Demos\DecoderPro3.lnk"
    Delete "$SMPROGRAMS\$0\Tools and Demos\Preferences.lnk"
    Delete "$SMPROGRAMS\$0\Uninstall.lnk"
    RMDir "$SMPROGRAMS\$0\Tools and Demos\"
    RMDir "$SMPROGRAMS\$0\"

    ; -- Remove any predictably-named JMRI shortcuts from the Desktop

    Delete "$DESKTOP\DecoderPro.lnk"
    Delete "$DESKTOP\DecoderPro3.lnk"
    Delete "$DESKTOP\PanelPro.lnk"
    Delete "$DESKTOP\SoundPro.lnk"

  SectionEnd ; SEC_CLEANUP

  Section "Main" SEC_MAIN
    SectionIn RO  ; This section always selected
    ; -- Check for JRE
    Call CheckJRE
    ; -- Main JMRI files
    SetOutPath "$INSTDIR"

    ; -- Install main JMRI files
    ; -- Library & Support Files now moved from here
    File /a "${SRCDIR}\*.jar"
    File /a "${SRCDIR}\COPYING"
    File /a "${SRCDIR}\jmri.conf"
    File /a "${SRCDIR}\LaunchJMRI.exe"
    File /a "${SRCDIR}\*.bat"
    File /a "${SRCDIR}\default.lcf"
    File /a "${SRCDIR}\*.ico"
    File /a "${SRCDIR}\lib\security.policy"
    File /a "${SRCDIR}\python.properties"

  SectionEnd ; SEC_MAIN

  Section "COM Library" SEC_COMLIB
    SectionIn RO  ; This section always selected

    ; -- If we're upgrading, we need to make sure that
    ; -- any previously loaded library files in the JRE
    ; -- are removed

    StrCmp $UPGRADING "1" 0 InstallComLib
      Delete "$JAVADIR\lib\javax.comm.properties"
      Delete "$JAVADIR\lib\ext\Serialio.jar"
      Delete "$JAVADIR\bin\jspWin.dll"
      Delete "$JAVADIR\bin\win32com.dll"

    InstallComLib:
      SetOutPath "$INSTDIR\lib"

      ; -- SerialIO native library
      File /a "${SRCDIR}\jspWin.dll"

  SectionEnd ; SEC_COMLIB

  Section "Help" SEC_HELP
    SectionIn RO  ; This section always selected
    ; -- Help files installed here
    SetOutPath "$INSTDIR\help"
    File /a /r "${SRCDIR}\help\*.*"
  SectionEnd ; SEC_HELP

  Section "Library files" SEC_LIB
    SectionIn RO  ; This section always selected
    ; -- Library files installed here
    SetOutPath "$INSTDIR\lib"

    ; -- Match all files in 'lib' but do not recurse into sub-directories
    File /a "${SRCDIR}\lib\*.*"

    ; -- Install x86 library files
    SetOutPath "$INSTDIR\lib\x86"

    ; -- Match all files in 'lib\windows\x86' but do not recurse into sub-directories
    File /a "${SRCDIR}\lib\windows\x86\*.*"

    ; -- Install x64 library files
    SetOutPath "$INSTDIR\lib\x64"

    ; -- Match all files in 'lib\windows\x64' but do not recurse into sub-directories
    File /a "${SRCDIR}\lib\windows\x64\*.*"

    ; -- Extract and run OpenAL library installer in silent mode
    ; [Ignored for now]
    ;SetOutPath "$TEMP"
    ;File /a /r "${SRCDIR}\oalinst.exe"
    ;ExecWait `"$TEMP\oalinst.exe" -s`
    ;Delete "$TEMP\oalinst.exe"
  SectionEnd ; SEC_LIB

  Section "Jython files" SEC_JYTHON
    SectionIn RO  ; This section always selected
    ; -- Jython script files here
    SetOutPath "$INSTDIR\jython"
    File /a /r "${SRCDIR}\jython\*.*"
  SectionEnd ; SEC_JYTHON

  Section "Resource files" SEC_RES
    SectionIn RO  ; This section always selected
    ; -- Resource files here
    SetOutPath "$INSTDIR\resources"
    File /a /r "${SRCDIR}\resources\*.*"
  SectionEnd ; SEC_RES

  Section "XML files" SEC_XML
    SectionIn RO  ; This section always selected
    ; -- Check to see if we've been previously installed with the new installer
    ; -- and if so, remove obsolete decoder definitions
    StrCmp $REMOVEOLDJMRI.BACKUPONLY "1" RemoveObsolete InstallXML

    RemoveObsolete:
      Call RemoveObsoleteDecoderDefinitions

    InstallXML:
      ; -- XML files here
      SetOutPath "$INSTDIR\xml"
      File /a /r "${SRCDIR}\xml\*.*"
  SectionEnd ; SEC_XML

  Section "Web files" SEC_WEB
    SectionIn RO  ; This section always selected
    ; -- Web files here
    SetOutPath "$INSTDIR\web"
    File /a /r "${SRCDIR}\web\*.*"
  SectionEnd ; SEC_WEB
SectionGroupEnd ; SEC_CORE

SectionGroup "Start menu shortcuts" SEC_SMSC
  ; -- Create Start menu shortcuts
  Section "Standard Components" SEC_SCSMSC
    SectionIn RO  ; This section always selected
    !insertmacro MUI_STARTMENU_WRITE_BEGIN JMRIStartMenu
    SetOutPath "$INSTDIR"
    CreateDirectory "$SMPROGRAMS\$SMFOLDER"
    ; -- Remove any shortcuts to deprecated components
    Delete "$SMPROGRAMS\$SMFOLDER\Tools and Demos\LocoTools.lnk"
    Delete "$SMPROGRAMS\$SMFOLDER\Tools and Demos\DecoderPro3.lnk"
    ; -- Create shortcuts for standard JMRI components
    CreateShortcut "$SMPROGRAMS\$SMFOLDER\DecoderPro.lnk" \
                   "$INSTDIR\LaunchJMRI.exe" \
                   "apps.gui3.dp3.DecoderPro3" \
                   "$INSTDIR\decpro5.ico" 0 "" "" \
                   "Start DecoderPro"
    CreateShortcut "$SMPROGRAMS\$SMFOLDER\PanelPro.lnk" \
                   "$INSTDIR\LaunchJMRI.exe" \
                   "apps.PanelPro.PanelPro" \
                   "$INSTDIR\PanelPro80x80.ico" 0 "" "" \
                   "Start PanelPro"
    CreateShortcut "$SMPROGRAMS\$SMFOLDER\SoundPro.lnk" \
                   "$INSTDIR\LaunchJMRI.exe" \
                   "apps.SoundPro.SoundPro" \
                   "$INSTDIR\SoundPro80x80.ico" 0 "" "" \
                   "Start SoundPro"
    CreateDirectory "$SMPROGRAMS\$SMFOLDER\Tools and Demos"
    CreateShortcut "$SMPROGRAMS\$SMFOLDER\Tools and Demos\InstallTest.lnk" \
                   "$INSTDIR\InstallTest.bat" \
                   "" \
                   "$INSTDIR\InstallTest80x80.ico" 0 "" "" \
                   "Start JMRI Install Test"
    ; -- Create a preferences directory for this user
    IfFileExists "$PROFILE\JMRI\*.*" +2
      CreateDirectory "$PROFILE\JMRI"
      ; -- Now create a shortcut to it
      CreateShortcut "$SMPROGRAMS\$SMFOLDER\Tools and Demos\Preferences.lnk" \
                   "%HOMEDRIVE%%HOMEPATH%\JMRI" \
                   "" \
                   "" "" "" "" \
                   "Open JMRI Preferences Folder"
    SetFileAttributes "$SMPROGRAMS\$SMFOLDER\Tools and Demos\Preferences.lnk" READONLY
    CreateShortcut "$SMPROGRAMS\$SMFOLDER\Uninstall.lnk" \
                   "$INSTDIR\Uninstall.exe" \
                   "/$MultiUser.InstallMode"
    !insertmacro MUI_STARTMENU_WRITE_END
  SectionEnd ; SEC_SCSMSC

  Section /o "Additional Tools and Demos" SEC_OCSMSC
    SectionIn 2
    !insertmacro MUI_STARTMENU_WRITE_BEGIN JMRIStartMenu
    CreateShortcut "$SMPROGRAMS\$SMFOLDER\Tools and Demos\JmriDemo.lnk" \
                   "$INSTDIR\LaunchJMRI.exe" \
                   "apps.JmriDemo.JMRIdemo" \
                   "$INSTDIR\decpro5.ico" 0 "" "" \
                   "Start JMRI Demo"
    !insertmacro MUI_STARTMENU_WRITE_END
  SectionEnd ; SEC_OCSMSC
SectionGroupEnd ; SEC_SMSC

SectionGroup "Desktop Shortcuts" SEC_DTSC
  ; -- Create Desktop shortcuts
  Section "DecoderPro" SEC_DPDTSC
    SectionIn 1 2
    CreateShortcut "$DESKTOP\DecoderPro.lnk" \
                   "$INSTDIR\LaunchJMRI.exe" \
                   "apps.gui3.dp3.DecoderPro3" \
                   "$INSTDIR\decpro5.ico" 0 "" "" \
                   "Start DecoderPro"
  SectionEnd ; SEC_DPDTSC

  Section "PanelPro" SEC_PPDTSC
    SectionIn 1 2
    CreateShortcut "$DESKTOP\PanelPro.lnk" \
                   "$INSTDIR\LaunchJMRI.exe" \
                   "apps.PanelPro.PanelPro" \
                   "$INSTDIR\PanelPro80x80.ico" 0 "" "" \
                   "Start PanelPro"
  SectionEnd ; SEC_PPDTSC

  Section /o "SoundPro" SEC_SPDTSC
    SectionIn 2
    CreateShortcut "$DESKTOP\SoundPro.lnk" \
                   "$INSTDIR\LaunchJMRI.exe" \
                   "apps.SoundPro.SoundPro" \
                   "$INSTDIR\SoundPro80x80.ico" 0 "" "" \
                   "Start SoundPro"
  SectionEnd ; SEC_SPDTSC

SectionGroupEnd ; SEC_DTSC

Section "-Create Uninstaller" SEC_CRUNINST
  SectionIn RO  ; This section always selected
  ; -- Create Uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
SectionEnd ; SEC_CRUNINST

Section "-PostProcessing" SEC_POST
  SectionIn RO  ; This section always selected
  ; -- Register with Windows Add/Remove Programs
  WriteRegStr SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" \
                 "DisplayName" "JMRI - Java Model Railroad Interface"
  WriteRegStr SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" \
                 "UninstallString" `"$INSTDIR\Uninstall.exe" /$MultiUser.InstallMode`
  WriteRegStr SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" \
                 "InstallLocation" "$INSTDIR"
  WriteRegStr SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" \
                 "DisplayVersion" "${JMRI_VER}"
  WriteRegStr SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" \
                 "Publisher" "JMRI Community"
  WriteRegStr SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" \
                 "URLInfoAbout" "http://jmri.org/"
  WriteRegDWORD SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" \
                 "NoModify" 1
  WriteRegDWORD SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" \
                 "NoRepair" 1
  ; -- Register task to create JMRI Preferences directory with Active Setup
  ; -- to ensure created for each new user (for All Users on Win2k+ only)
  StrCmp $MultiUser.InstallMode "CurrentUser" Done
  ; -- Write the Local Machine registry entries
  WriteRegStr HKLM "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "" "JMRI Customizations"
  WriteRegStr HKLM "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "ComponentID" "JMRI"
  WriteRegDWORD HKLM "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "IsInstalled" 1
  WriteRegStr HKLM "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "Locale" "*"
  WriteRegStr HKLM "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "StubPath" "$INSTDIR\CreatePrefs.bat"
  WriteRegStr HKLM "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "Version" "0,0,0,1"
  ; -- Write the Current User registry entries
  WriteRegStr HKCU "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "" ""
  WriteRegStr HKCU "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "Locale" "*"
  WriteRegStr HKCU "Software\Microsoft\Active Setup\Installed Components\JMRI" \
              "Version" "0,0,0,1"
  Done:
    ; -- Save the log
    StrCpy $0 "$INSTDIR\install.log"
    Push $0
    Call SaveLog

SectionEnd ; SEC_POST

Section "Uninstall" ; SEC_CRUNINST

  ; -- Remove all program files
  StrCpy $0 "$INSTDIR\install.log"
  Push $0
  Call un.DeleteFromLog

  ; -- Remove all shortcuts
  !insertmacro MUI_STARTMENU_GETFOLDER JMRIStartMenu $0

    ; -- Change file attributes early so they have time to propagate on systems
    ; --        with unexpected delays in the filesystem.
    SetFileAttributes "$SMPROGRAMS\$0\Tools and Demos\Preferences.lnk" NORMAL

  Delete "$SMPROGRAMS\$0\DecoderPro.lnk"
  Delete "$SMPROGRAMS\$0\DecoderPro3.lnk"
  Delete "$SMPROGRAMS\$0\PanelPro.lnk"
  Delete "$SMPROGRAMS\$0\SoundPro.lnk"
  Delete "$SMPROGRAMS\$0\Tools and Demos\JmriDemo.lnk"
  Delete "$SMPROGRAMS\$0\Tools and Demos\LocoTools.lnk"
  Delete "$SMPROGRAMS\$0\Tools and Demos\CornwallRR.lnk"
  Delete "$SMPROGRAMS\$0\Tools and Demos\InstallTest.lnk"
  Delete "$SMPROGRAMS\$0\Tools and Demos\InstallTest.pif" ; -- for Win98
  Delete "$SMPROGRAMS\$0\Tools and Demos\DecoderPro3.lnk"
  Delete "$SMPROGRAMS\$0\Tools and Demos\Preferences.lnk"
  Delete "$SMPROGRAMS\$0\Uninstall.lnk"
  RMDir "$SMPROGRAMS\$0\Tools and Demos\"
  RMDir "$SMPROGRAMS\$0\"
  Delete "$DESKTOP\DecoderPro.lnk"
  Delete "$DESKTOP\PanelPro.lnk"
  Delete "$DESKTOP\SoundPro.lnk"
  Delete "$DESKTOP\DecoderPro3.lnk"

  ; -- Remove registry entries
  DeleteRegKey SHCTX "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI"
  StrCmp $MultiUser.InstallMode "CurrentUser" Done
  DeleteRegKey HKLM "Software\Microsoft\Active Setup\Installed Components\JMRI"

  Done:
    ; -- Remove log file
    Delete "$INSTDIR\install.log"
    ; -- Remove the uninstaller
    Delete "$INSTDIR\Uninstall.exe"
    ; -- Delete install directory if empty
    RmDir "$INSTDIR"

SectionEnd

; -------------------------------------------------------------------------
; - Section descriptions
; -------------------------------------------------------------------------

LangString DESC_SEC_CORE ${LANG_ENGLISH} "Core ${APP} files; these are all required for a functional ${APP} install"
LangString DESC_SEC_MAIN ${LANG_ENGLISH} "The main ${APP} program and essential resources"
LangString DESC_SEC_COMLIB ${LANG_ENGLISH} "Library files for Java COM port access"
LangString DESC_SEC_HELP ${LANG_ENGLISH} "${APP} Help files"
LangString DESC_SEC_LIB ${LANG_ENGLISH} "General ${APP} Library files"
LangString DESC_SEC_JYTHON ${LANG_ENGLISH} "Jython example script files"
LangString DESC_SEC_RES ${LANG_ENGLISH} "Additional ${APP} resource files (sounds, icons, etc.)"
LangString DESC_SEC_XML ${LANG_ENGLISH} "XML files (Decoder definitions, etc.)"
LangString DESC_SEC_WEB ${LANG_ENGLISH} "Web files"
LangString DESC_SEC_SMSC ${LANG_ENGLISH} "Select Start Menu Shortcuts to create"
LangString DESC_SEC_SCSMSC ${LANG_ENGLISH} "Creates Start menu shortcuts for DecoderPro, PanelPro and InstallTest"
LangString DESC_SEC_OCSMSC ${LANG_ENGLISH} "Creates Start menu shortcut for JMRI Demo"
LangString DESC_SEC_DTSC ${LANG_ENGLISH} "Select Desktop Shortcuts to create."
LangString DESC_SEC_DPDTSC ${LANG_ENGLISH} "Creates a Desktop shortcut for DecoderPro"
LangString DESC_SEC_PPDTSC ${LANG_ENGLISH} "Creates a Desktop shortcut for PanelPro"
LangString DESC_SEC_SPDTSC ${LANG_ENGLISH} "Creates a Desktop shortcut for SoundPro"
LangString DESC_SEC_CRUNINST ${LANG_ENGLISH} "Creates an Uninstaller for ${APP}"
LangString MESSAGE_INVALID_DIRECTORY ${LANG_ENGLISH} "This not a valid installation directory. Please reselect"
LangString MESSAGE_WIN2K_OR_LATER ${LANG_ENGLISH} "${APP} version ${JMRI_VER} is not supported on this version of Windows. Installation cannot continue."

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_CORE} $(DESC_SEC_CORE)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_MAIN} $(DESC_SEC_MAIN)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_COMLIB} $(DESC_SEC_COMLIB)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_HELP} $(DESC_SEC_HELP)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_LIB} $(DESC_SEC_LIB)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_JYTHON} $(DESC_SEC_JYTHON)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_RES} $(DESC_SEC_RES)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_XML} $(DESC_SEC_XML)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_WEB} $(DESC_SEC_WEB)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_SMSC} $(DESC_SEC_SMSC)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_SCSMSC} $(DESC_SEC_SCSMSC)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_OCSMSC} $(DESC_SEC_OCSMSC)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_DTSC} $(DESC_SEC_DTSC)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_DPDTSC} $(DESC_SEC_DPDTSC)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_PPDTSC} $(DESC_SEC_PPDTSC)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_SPDTSC} $(DESC_SEC_SPDTSC)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC_CRUNINST} $(DESC_SEC_CRUNINST)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

; -------------------------------------------------------------------------
; - Additional Functions
; -------------------------------------------------------------------------

Function .onInit
; -------------------------------------------------------------------------
; - On installer initialisation, initialise MultiUser header, check OS
; - architecture and check for running on Win2k or later
; -------------------------------------------------------------------------

  !insertmacro MULTIUSER_INIT

  ; -- Determine if we're running on Windows 98 or ME
  ; -- If so, show a message and then abort
  StrCmp $PROFILE "" 0 Check64 ; -- prior to Win2k this is blank
  ;MessageBox MB_OK|MB_ICONSTOP "${MESSAGE_WIN2K_OR_LATER}"
  Abort "${MESSAGE_WIN2K_OR_LATER}"

  Check64:
  ; -- Determine OS architecture
  Call CheckIf64bit
  Pop $x64

FunctionEnd

Function un.onInit
; -------------------------------------------------------------------------
; - On uninstaller initialisation, initialise MultiUser header and check
; - InstallMode
; -------------------------------------------------------------------------

  !insertmacro MULTIUSER_UNINIT

  ; -- Check if InstallMode is the same as the command-line
  ${un.GetParameters} $0
  StrCmp $0 "/$MultiUser.InstallMode" +2
    Abort

FunctionEnd

Function DirectoryLeave
; -------------------------------------------------------------------------
; - Check that the user has chosen a valid location
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0

  ; -- Check to see if Install Directory is set to 'Program Files'
  StrCmp $INSTDIR $PROGRAMFILES Invalid Valid

  Invalid:
    ; -- If so, display a message and ask user to re-select
    MessageBox MB_OK|MB_ICONEXCLAMATION "$(MESSAGE_INVALID_DIRECTORY)"

    ; -- Restore variables from the stack
    Pop $0
    ; -- Abort the movement to the next page
    Abort

  Valid:
    ; -- Restore variables from the stack
    Pop $0

FunctionEnd

#Function InstallValidationWizard
#; -------------------------------------------------------------------------
#; - Launches the JMRI InstallValidationWizard
#; - input:  none
#; - output: none
#; -------------------------------------------------------------------------
#  Exec "$INSTDIR/LaunchJMRI.exe apps.wizard.installvalidation.InstallValidation"
#FunctionEnd

Function CheckJRE
; -------------------------------------------------------------------------
; - Check for Java Runtime Environment
; - input:  none
; - output: none
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0
  Push $1
  Push $2
  Push $3

  ; -- Initialise JRE architecture variable
  StrCpy $x64JRE 0

  ; -- If we're running x64, first check for 64-bit JRE
  StrCmp 0 $x64 JRESearch
    DetailPrint "Setting x64 registry view..."
    SetRegView 64
    StrCpy $x64JRE 1

  ; -- Read from host machine registry
  JRESearch:
    IntOp $JREINSTALLCOUNT $JREINSTALLCOUNT + 1
    ClearErrors
    ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\JRE" "CurrentVersion"
    ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\JRE\$1" "JavaHome"
    IfErrors 0 JRECheck
    ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
    IfErrors 0 JRECheck
    ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\JDK" "CurrentVersion"
    ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\JDK\$1" "JavaHome"

    ; -- Not found
    IfErrors 0 JRECheck
      ; -- If we've got an error here on x64, switch to the 32-bit registry,
      ; -- decrease the counter and then re-try
      StrCmp 0 $x64JRE JREInitInstall
        SetRegView 32
        DetailPrint "Setting x86 registry view..."
        StrCpy $x64JRE 0
        IntOp $JREINSTALLCOUNT $JREINSTALLCOUNT - 1
        Goto JRESearch

  JREInitInstall:
      StrCpy $3 "No JAVA installation found"
      ; -- If this is the first time around the loop then try to run/download a JRE
      ; -- If this is a subsequent time, something went wrong with the JRE install
      StrCmp $JREINSTALLCOUNT 1 JREInstall NoJRE

  JRECheck:
    StrCpy "$JAVADIR" "$0"
    ; -- Check we have the required JRE version
    ; -- If so, jump to the end of this section
    ${VersionCompare} "$1" "${JRE_VER}" $0
    StrCmp $0 "2" JREVerCheck JREFound

  JREVerCheck:
    StrCpy $3 "Found JAVA version $1 - ${APP} requires version ${JRE_VER} or later"

  JREInstall:
    ; -- First, check to see if a suitable installer exists in the JRE sub-directory
    ; -- below that of the installer. It needs to be of the format jre*.exe
    ; -- (This is to allow for automatic off-line installation
    ; -- and creation of turn-key distribution CD's)

    FindFirst $2 $JREINSTALLER "$EXEDIR\JRE\jre*.exe"

    OfflineJREInstall:
    StrCmp $JREINSTALLER "" DownloadJREQuery
      StrCpy $JREINSTALLER "$EXEDIR\JRE\$JREINSTALLER"
      StrCpy $OFFLINEINSTALL "1"
      Goto StartJREInstall

  DownloadJREQuery:
    ; -- Now check to see if we have an Internet Connection
    ; -- If so, then ask the user to download the latest JRE.
    Call CheckInternetConnection
    Pop $0 ; Get the return value
    StrCmp $0 "offline" NoJRE
;    MessageBox MB_YESNO|MB_ICONQUESTION "$3$\nWould you like to download JAVA from the internet?" IDYES DownloadJRE IDNO NoJRE

  NoJRE:
    MessageBox MB_ICONSTOP "$3$\nYou need to install JAVA ${JRE_VER} or later$\nThis can be downloaded from ${JRE_URL}$\nInstallation of ${APP} ${JMRI_VER} cannot continue"
    Quit

;  DownloadJRE:
;    StrCpy $JREINSTALLER "$TEMP\JRE.exe"
;    nsisDL::Download /TIMEOUT=30000 ${JRE_URL} $JREINSTALLER
;    Pop $0 ; Get the return value
;    StrCmp $0 "success" DownloadOK
;      MessageBox MB_ICONSTOP "Failure downloading JAVA$\nPlease try manually from ${JRE_URL}$\nInstallation of ${APP} ${JMRI_VER} cannot continue"
;      Quit
;
;  DownloadOK:
;    StrCpy $OFFLINEINSTALL "0"

  StartJREInstall:
    ExecWait $JREINSTALLER ; Run the JRE installer
    StrCmp $OFFLINEINSTALL "1" JREInstallComplete
    Delete $JREINSTALLER ; If downloaded, delete the temporary downloaded file

  JREInstallComplete:
    Goto JRESearch

  JREFound:
    ; -- We've successfully located a suitable JRE version.
    ; -- If this was found within the x64 registry view, reset to x86
    ; -- registry view so that our entries are placed in the expected place.
    StrCmp 0 $x64JRE JREDone
      DetailPrint "Reverting to x86 registry view..."
      SetRegView 32

  JREDone:
    ; -- Restore variables from the stack
    Pop $3
    Pop $2
    Pop $1
    Pop $0

FunctionEnd

Function CheckInternetConnection
; -------------------------------------------------------------------------
; - Check for a valid Internet connection
; - input:  none
; - output: top of stack ("online" or "offline")
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0
  Push $1
  Push $2

  ; -- Determine the status of the Internet connection
  DetailPrint "Checking Internet Connection Status..."
  System::Call 'wininet.dll::InternetGetConnectedState(*l .r2, i 0) i.r0'
  StrCmp $0 "1" 0 +3
    StrCpy $0 "online"
    Goto +2
  StrCpy $0 "offline"
  IntOp $1 $2 & ${INTERNET_CONNECTION_CONFIGURED}
  StrCmp $1 ${INTERNET_CONNECTION_CONFIGURED} 0 +2
    DetailPrint "System has a valid Internet configuration, but it may not be connected"
  IntOp $1 $2 & ${INTERNET_CONNECTION_LAN}
  StrCmp $1 ${INTERNET_CONNECTION_LAN} 0 +2
    DetailPrint "System uses a local area network to connect to the Internet"
  IntOp $1 $2 & ${INTERNET_CONNECTION_MODEM}
  StrCmp $1 ${INTERNET_CONNECTION_MODEM} 0 +2
    DetailPrint "System uses a modem to connect to the Internet"
  IntOp $1 $2 & ${INTERNET_CONNECTION_PROXY}
  StrCmp $1 ${INTERNET_CONNECTION_PROXY} 0 +2
    DetailPrint "System uses a proxy server to connect to the Internet"
  IntOp $1 $2 & ${INTERNET_RAS_INSTALLED}
  StrCmp $1 ${INTERNET_RAS_INSTALLED} 0 +2
    DetailPrint "System has RAS installed"
  IntOp $1 $2 & ${INTERNET_CONNECTION_OFFLINE}
  StrCmp $1 ${INTERNET_CONNECTION_OFFLINE} 0 +2
    DetailPrint "System is in offline mode"

  DetailPrint "...done. Internet Connection is $0"

  ; -- Restore variables from the stack
  Pop $2
  Pop $1
  Exch $0

FunctionEnd

Function nsDialogRemoveOldJMRI
; -------------------------------------------------------------------------
; - Identifies install created by old InstallVise installer and prompts for
; - removal
; - input:  none
; - output: none
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0
  Push $1
  Push $2
  Push $3
  Push $4

  ; -- Default to not upgrading
  StrCpy $UPGRADING 0

  ; -- First check if JMRI has been installed (Current User first, then All Users)
  ReadRegStr $0 HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" "DisplayName"
  StrCmp $0 "" 0 CheckOld
  ReadRegStr $0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" "DisplayName"
  StrCmp $0 "" Done

  CheckOld:
    ; -- If we get to here, then an old JMRI installation exists
    StrCpy $UPGRADING 1

    ; -- Default to uninstall and backup
    StrCpy $REMOVEOLDJMRI.BACKUPONLY 0
    StrCpy $2 "This previous installation should be removed.$\r$\nThis wizard will backup any existing roster files and settings."
    StrCpy $3 "Remove old ${APP} installation and backup existing files"

    ; -- Now check if installed via old InstallVise installer
    ; -- by checking if the 'DisplayVersion' registry key exists
    ; -- (this key is not created by the old installer)
    ReadRegStr $4 HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" "DisplayVersion"
    StrCmp $4 "" 0 Backup
    ReadRegStr $4 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" "DisplayVersion"
    StrCmp $4 "" Remove Backup

  Backup:
    ; -- If we get to here, we've been previously installed by the new installer
    ; -- so, we can read the install location and prompt for backup

    ; -- As we've not yet set the installation context (i.e. All Users or Current User)
    ; -- first attempt to get the registry string from 'Current User'
    ReadRegStr $0 HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" "InstallLocation"
    StrCmp $0 "" 0 CopyToInstDir
    ; -- Location wasn't in 'Current User' - attempt to retrieve from 'All Users'
    ReadRegStr $0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" "InstallLocation"
    StrCmp $0 "" PrepareBackup CopyToInstDir

  CopyToInstDir:
    StrCpy $INSTDIR $0

  PrepareBackup:
    StrCpy $REMOVEOLDJMRI.BACKUPONLY 1
    StrCpy $2 "This wizard will backup any existing roster files and settings."
    StrCpy $3 "Backup existing files"

  Remove:
    ; -- If we've got this far, we need to create the page
    Var /GLOBAL REMOVEOLDJMRI.CHECKBOX

    !insertmacro MUI_HEADER_TEXT "Check for previous version" "Check for a previous installation of ${APP}."

    ; -- Return the HWND of this dialog
    nsDialogs::Create 1018
    Pop $0

    ; -- Create the controls and assign call-back functions
    ${NSD_CreateLabel} 0u 0u 100% 12u "${APP} setup has detected a previous installation of ${APP}."
    Pop $0
    ${NSD_CreateLabel} 0u 14u 100% 20u "$2"
    Pop $0
    ${NSD_CreateLabel} 0u 36u 100% 12u "Backup files will be stored in the following location:"
    Pop $0
    ${NSD_CreateText} 0u 50u 100% 12u "$PROFILE\JMRI_backup"
    Pop $0
    SendMessage $0 ${EM_SETREADONLY} 1 0

    ${NSD_CreateLabel} 0u 70u 100% 12u "Any existing backup will be moved to the following location:"
    Pop $0
    ${NSD_CreateText} 0u 84u 100% 12u "$PROFILE\JMRI_backup_old"
    Pop $0
    SendMessage $0 ${EM_SETREADONLY} 1 0

    ${NSD_CreateCheckBox} 0u 120u 100% 8u "$3"
    Pop $REMOVEOLDJMRI.CHECKBOX
    ${NSD_OnClick} $REMOVEOLDJMRI.CHECKBOX RemoveOldJMRICheckboxChange
    ; -- If this is a backup only install, select the checkbox if installed version is older
    StrCmp $REMOVEOLDJMRI.BACKUPONLY "0" ShowDialog
      ${VersionCompare} "${JMRI_VER}" "$4" $0
      StrCmp $0 "1" 0 ShowDialog
      ${NSD_SetState} $REMOVEOLDJMRI.CHECKBOX ${BST_CHECKED}

  ShowDialog:
    Call RemoveOldJMRICheckboxChange

    ; -- Show the dialog
    nsDialogs::Show

  Done:
    ; -- Restore variables from the stack
    Pop $4
    Pop $3
    Pop $2
    Pop $1
    Pop $0

FunctionEnd

Function RemoveOldJMRICheckboxChange
; -------------------------------------------------------------------------
; - Enable or disable the 'Next >' button whenever the checkbox is changed
; - input:  none
; - output: none
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0
  Push $1

  ; -- Get pointer to the 'Next >' button
  GetDlgItem $0 $HWNDPARENT 1

  ; -- Get current state
  ${NSD_GetState} $REMOVEOLDJMRI.CHECKBOX $1
  StrCpy $REMOVEOLDINSTALL $1

  ; -- Check to see if this is a backup only install which means that we
  ; -- don't need to disable the 'Next >' button
  StrCmp $REMOVEOLDJMRI.BACKUPONLY "1" Done

  ; -- If we get here, we need to consider disabling the 'Next >' button
  StrCmp $1 ${BST_CHECKED} Enable

  EnableWindow $0 0
  Goto Done

  Enable:
    EnableWindow $0 1

  Done:
    ; -- Restore variables from the stack
    Pop $1
    Pop $0

FunctionEnd

Function RemoveOldJMRI
; -------------------------------------------------------------------------
; - Backup existing preferences and roster files etc. and if required
; - removes install created by old InstallVise installer.
; - input:  none
; - output: none
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0

  StrCmp $REMOVEOLDINSTALL ${BST_UNCHECKED} Done

  ; -- Get pointer to the 'Next >' button
  GetDlgItem $0 $HWNDPARENT 1
  EnableWindow $0 0

  ; -- Perform backup
  RMDir /r "$PROFILE\JMRI_backup_old"
  Rename "$PROFILE\JMRI_backup" "$PROFILE\JMRI_backup_old"
  CreateDirectory "$PROFILE\JMRI_backup"
  CopyFiles "$PROFILE\JMRI\*.*" "$PROFILE\JMRI_backup"
  CopyFiles "$INSTDIR\classes" "$PROFILE\JMRI_backup"
  CopyFiles "$INSTDIR\lib" "$PROFILE\JMRI_backup"

  ; -- Check if uninstall required
  StrCmp $REMOVEOLDJMRI.BACKUPONLY "1" Done

  ; -- Retrieve the InstallVise uninstall string and run the uninstaller
  ReadRegStr $0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" "UninstallString"
  HideWindow
  ExecWait "$0"
  BringToFront

  ; -- Check if InstallerVise uninstaller has run
  ReadRegStr $0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\JMRI" "DisplayName"
  StrCmp $0 "" Done

  MessageBox MB_OK|MB_ICONSTOP "The previous installation of ${APP} was not successfully removed. This installation cannot continue."
  Quit

  Done:
    ; -- Restore variables from the stack
    Pop $0

FunctionEnd

Function SaveLog
; -------------------------------------------------------------------------
; - Saves the log to specified file
; - input:  filename to store at top of stack
; - output: none
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Exch $5
  Push $0
  Push $1
  Push $2
  Push $3
  Push $4
  Push $6

  ; -- Find the log SysListView32 control
  FindWindow $0 "#32770" "" $HWNDPARENT
  GetDlgItem $0 $0 1016
  StrCmp $0 0 Exit

  ; -- Create the file
  FileOpen $5 $5 "w"
  StrCmp $5 "" Exit

    ; -- Retrieve number of items in the log
    SendMessage $0 ${LVM_GETITEMCOUNT} 0 0 $6

    ; -- Allocate memory for LVITEM structure
    System::Alloc ${NSIS_MAX_STRLEN}
    Pop $3
    StrCpy $2 0
    System::Call "*(i, i, i, i, i, i, i, i, i) i \
      (0, 0, 0, 0, 0, r3, ${NSIS_MAX_STRLEN}) .r1"

    ; -- Loop through the log
    Loop:
      ; -- Check if at end of log
      StrCmp $2 $6 Done
      ; -- Retrieve log item
      System::Call "User32::SendMessageA(i, i, i, i) i \
        ($0, ${LVM_GETITEMTEXT}, $2, r1)"
      System::Call "*$3(&t${NSIS_MAX_STRLEN} .r4)"
      ; -- Write it to the file
      FileWrite $5 "$4$\r$\n"
      IntOp $2 $2 + 1
      Goto Loop

    Done:
      ; -- Close the file and free memory
      FileClose $5
      System::Free $1
      System::Free $3

  Exit:
    ; -- Restore variables from the stack
    Pop $6
    Pop $4
    Pop $3
    Pop $2
    Pop $1
    Pop $0
    Exch $5

FunctionEnd

Function un.DeleteFromLog
; -------------------------------------------------------------------------
; - Deletes installed files based on the specified log file
; - input:  log filename on top of stack
; - output: none
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Exch $0 ; -- Logfile filename
  Push $1 ; -- Current Directory
  Push $2 ; -- Current File
  Push $3 ; -- File handle
  Push $4 ; -- Current line
  Push $5 ; -- Temporary variable
  Push $6 ; -- Temporary variable

  ; -- Open the log file readonly
  FileOpen $3 "$0" "r"

  NextLogEntry:
    ; -- Read the next line of the file
    FileRead $3 $4

    ; -- Determine the log entry type
    ; -- Check if this entry is for a directory
    StrCpy $5 $4 15
    StrCmp $5 "Output folder: " RemoveDir

    ; -- Check if this entry is for a file
    StrCpy $5 $4 9
    StrCmp $5 "Extract: " RemoveFile

    ; -- Check if at end of log
    StrCmp $4 "" Done

    ; -- Don't care about this log entry type - retrieve next log entry
    Goto NextLogEntry

  RemoveDir:
    ; -- Try to remove this directory (only if empty)
    RMDir $1
    StrCpy $5 $1

  DeleteParents:
    ; -- Will attempt to delete parent directorys until it runs out.
    ; -- Not aggressive, just removes empty directories recursively
    ; -- before moving onto a new directory to delete files from
    Push $5
    Call un.GetParent
    Pop $5
    StrCmp $5 "" +2
    RMDir $5
    StrCmp $5 "" 0 DeleteParents

    ; -- Get the directory name
    StrCpy $1 $4 1024 15
    StrLen $5 $1
    ; -- trim off the newline
    IntOp $5 $5 - "2"
    StrCpy $1 $1 $5
    Goto NextLogEntry

  RemoveFile:
    ; -- Get the filename
    StrCpy $2 $4 1024 9
    StrLen $5 $2
    ; -- trim off the newline
    IntOp $5 $5 - "2"
    StrCpy $2 $2 $5
    ; -- check if ends with "... 100%"
    StrCpy $6 $2 8 -8
    StrCmp $6 "... 100%" 0 DeleteFile
    ; -- trim off the "... 100%"
    IntOp $5 $5 - "8"
    StrCpy $2 $2 $5

  DeleteFile:
    Delete "$1\$2" ; Try to delete the new file
    Goto NextLogEntry

  Done:
    ; -- Close the file
    FileClose $3

    ; -- Restore variables from the stack
    Pop $6
    Pop $5
    Pop $4
    Pop $3
    Pop $2
    Pop $1
    Exch $0

FunctionEnd

Function un.GetParent
; -------------------------------------------------------------------------
; - Retrieve Parent directory of specified child directory
; - input:  Child directory name on top of stack
; - output: Parent directory name on top of stack
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Exch $0 ; -- Child directory
  Push $1 ; -- Counter
  Push $2 ; -- String length
  Push $3 ; -- Temporary storage

  ; -- Initialise counter
  StrCpy $1 0

  ; -- Retrieve length of string
  StrLen $2 $0

  Loop:
    ; -- Increment counter
    IntOp $1 $1 + 1
    ; -- Check if we're at end of string
    IntCmp $1 $2 Get 0 Get
    ; -- Get the n'th character from the right
    StrCpy $3 $0 1 -$1
    ; -- Check if it's a directory separator
    StrCmp $3 "\" Get
  Goto Loop

  Get:
    ; -- Remove n characters from the end of the string
    StrCpy $0 $0 -$1

    ; -- Restore variables from the stack
    Pop $3
    Pop $2
    Pop $1
    Exch $0

FunctionEnd

Function CheckIf64bit
; -------------------------------------------------------------------------
; - Check if this installer is running on a 64-bit system
; - input:  none
; - output: result on top of stack (0 if 32-bit; 1 if 64-bit)
; -------------------------------------------------------------------------

  ; -- Save variables to the stack
  Push $0

  ; -- Determine if we're running on x64
  System::Call kernel32::GetCurrentProcess()i.s
  System::Call kernel32::IsWow64Process(is,*i.s)
  Pop $0

  ; -- Restore variables from the stack
  Exch $0

FunctionEnd

Function RemoveObsoleteDecoderDefinitions
; -------------------------------------------------------------------------
; - Remove obsolete decoder definition files
; - input:  none
; - output: none
; -------------------------------------------------------------------------
  ; -- For now, doing this the 'quick & dirty way (TM)'
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_1Diesel.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_1EMD567.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_1EMD645.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Diesel_ALCO1.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Diesel_ALCO2.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Diesel_EMD567.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Diesel_EMD645.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Diesel_EMD710.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Diesel_FM1.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Steam_1Hvy.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Steam_2Med.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Steam_3Light.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Steam_4Lt_Log.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Steam_DRGC.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Steam_DRGK.xml"
  Delete "$INSTDIR\xml\decoders\SoundTraxx_Tsu_Steam_Cab_Frwd.xml"
FunctionEnd
