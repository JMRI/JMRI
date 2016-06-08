This is the JMRI distributed "lib" directory, available as the "lib" directory in JMRI/JMRI Git.   These are the library files, .jars and others, needed at build and run time.

## Contents:

Generally, we use subdirectories to hold the Git-resident versions of OS-specific code for Windows (.dll files) and Linux (.so files) so that we can separate various builds.

For example, the RXTX rxtxSerial.dll comes in separate versions for 32-bit and 64-bit Windows, but the files have the same name.  We store them in separate subdirectories under windows/, and let the installer sort them out.

A similar mechanism is used for Linux under the linux/ directory.

MacOS X fat binaries are treated slightly differently, see the README file there.

#### Updates

If you make a change in this directory (add/change/remove a file), please make corresponding changes in the control files that are used for various JMRI development and release operations:
- build.xml - used by Ant, and in turn by various IDEs
- .classpath - used by Eclipse
- nbproject/ide-file-targets.xml, nbproject/project.xml - used by NetBeans

Note that Windows installers don't necessarily remove existing library versions. (See [JMRI Issue #359](https://github.com/JMRI/JMRI/issues/359) for discussion on this)  Until that's changed, if you remove a library from here that really needs to _not_ be in user installs, you need to add an explicit delete to the scripts/WinInstallFiles/InstallJMRI.nsi file, in addition to modifying those above. 


### Specific components:

##### vecmath.jar
- from Java3D 1.3.2
- from <https://java3d.dev.java.net/binary-builds-old.html>

##### Serialio.jar
- from <http://serialio.com>

##### commons-lang3-3.2.1.jar
- version 3.2.1
- provides org.apache.commons.lang3
- from https://commons.apache.org/proper/commons-lang/
    
##### javacsv.jar
- version 2.0 of 2006-12-12
- from <http://javacsv.sourceforge.net/>

##### jhall.jar
- version 2.03
- from <http://java.sun.com/javase/technologies/desktop/javahelp/>
 
##### log4j.jar
- version 1.2.15

##### slf4j-api-1.7.13.jar, slf4j-log4j12-1.7.13.jar, jul-to-slf4j-1.7.13.jar
- slf4j logging interface to log4j
- from http://www.slf4j.org
- updated JMRI 4.1.4 from version 1.7.6, added jul-to-slf4j
    
##### openlcb.jar
- 0.6.5 from https://sourceforge.net/p/openlcb/svn/HEAD/tree/trunk/prototypes/java/
- Note (from 0.6.4): This implements the protocols as adopted by the OpenLCB group prior to February 2015.  There have been changes to their specifications since then with uncertain provenance. JMRI's use of this version means that it won't comply with those.
- Note: The OpenLCB group has orphaned development of this library. See: http://sourceforge.net/p/openlcb/svn/3912/

##### jlfgr-1_0.jar 
- icons from see http://www.coderanch.com/t/341737/GUI/java/Expand-Collapse-Panels

##### javax.comm.properties
- left over from javax.comm version 2.0 (minor version unknown) from Sun

##### RXTXcomm.jar, librxtxSerial.jnilib
- From Rxtx-2.2pre2  http://rxtx.qbang.org (http://rxtx.qbang.org/pub/rxtx/rxtx-2.2pre2-bins.zip)
- The win32 and win64 directories contain the necessary rxtxSerial.dll for the two windows varients
- The i686-pc-linux-gnu directory contains two .so libraries for 32-bit Linux
- For MacOS X:
    macosx/librxtxSerial.jnilib     169488  from rxtx-2.2pre2-bins distribution
    macosx/ppc/librxtxSerial.jnilib 301908  built for MacOS X 10.4 by John Plocher 2010-02-04

##### security.policy
- (JMRI file)

##### xercesImpl.jar
- version Xerces-J 2.9.1
- from http://www.apache.org/dist/xerces/j/

##### jdom.jar
- (deprecated, we've moved to JDOM2; will be removed from here and control files post JMRI 3.12, but remains for e.g. CATS now)         
- version 1.1
- from <jdom.org>
 
##### jdom-2.0.5.jar               
- version 2.0.5
- from <jdom.org>
 
##### jackson-annotations-2.0.6.jar, jackson-core-2.0.6.jar, jackson-databind-2.0.6.jar
- JSON processing library com.fasterxml.jackson
- version 2.0.6
- see http://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial
- JavaDoc http://fasterxml.github.io/jackson-databind/javadoc/2.0.6

bluecove-2.1.1-SNAPSHOT.jar
lib/bluecove-bluez-2.1.1-SNAPSHOT.jar
bluecove-gpl-2.1.1-SNAPSHOT.jar
    Installed from JMRI patch 1037
    BlueCove (http://bluecove.org/).
    The file bluecove-2.1.1-SNAPSHOT.jar is licensed under Apache License, 
        Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0) while the 
        file bluecove-gpl-2.1.1-SNAPSHOT.jar, needed for use on linux, i
        is licensed under GNU General Public License (http://www.gnu.org/licenses/gpl.html).
    Questions to johan.bostrom@mollehem.se
    The particular files are actually
        lib/github-machaval/bluecove-2.1.1-SNAPSHOT.jar
        lib/github-machaval/bluecove-bluez-2.1.1-SNAPSHOT.jar
        lib/github-machaval/bluecove-gpl-2.1.1-SNAPSHOT.jar
    from https://github.com/ieee8023/blucat/find/master
    The source for these is also available there.
    The related project seems to be https://github.com/machaval/bluecove
    The native libraries were installed on a best guess effort;
            exact locations might not be right.
        lib/windows/bluecove.dll
        lib/windows/bluecove_ce.dll
        lib/windows/intelbth_ce.dll
        lib/windows/x64/intelbth_x64.dll
        lib/windows/x86/intelbth.dll

##### jython.jar
- version 2.7.0 from jython.org (was jython-standalone-2.7.0.jar)
 
##### jakarta-regexp-1.5.jar
- (needed for jfcunit)
- Used for testing only, not at runtime
  
##### jinput (including jinput.jar, three jinput DLLs, and two libjinputs)
- from <https://jinput.dev.java.net/> jinput_dist_20090401
- (most recent as of 2010-Jan-02)

##### libusb-jar (ch.ntb.usb.jar)
        http://inf.ntb.ch/infoportal/help/index.jsp?topic=/ch.ntb.infoportal/tools.html
        http://libusb.wiki.sourceforge.net/
        libusb installers from "TWAIN SANE" http://www.ellert.se/twain-sane/
        
  version 0.5.7
  libusbJava.jnilib for MacOS X
        to get 64-bit, from http://wiki.ztex.de/doku.php?id=en:software:porting#macos_port
        requires /usr/local/lib/libusb-0.1.4.dylib via MacPorts or Homebrew or an installer from http://www.ellert.se/twain-sane/
  LibusbJava.dll for Windows is 0.2.3.0 (Feb 18, 2008)
  libusbJava.so for Linux was built on Ubuntu 7.10 w libusb 2:0.1.12-7
  
##### JavaMail 1.4.1
- mailapi.jar
- smtp.jar
  
##### Joal 2.3.1
  from <http://jogamp.org/deployment/archive/rc/v2.3.1/jar/>
  cross-platform .jar files
    joal.jar
    gluegen-rt.jar

  plus helper native libraries:
  for MacOS X 10.4+
    extracted from joal-natives-macosx-universal.jar
      libjoal.jnilib
    extracted from gluegen-rt-natives-macosx-universal.jar
      libgluegen-rt.jnilib

  for Windows x86
    extracted from joal-natives-windows-i586.jar
      joal.dll
    extracted from glugen-rt-natives-windows-i586.jar
      gluegen-rt.dll

  for Windows x64
    extracted from joal-natives-windows-amd64.jar
      joal.dll
    extracted from glugen-rt-natives-windows-amd64.jar
      gluegen-rt.dll

  for Linux i386
    extracted from joal-natives-linux-i586.jar
      libjoal.so
    extracted from glugen-rt-natives-linux-i586.jar
      libgluegen-rt.so

  for Linux x86_64
    extracted from joal-natives-linux-amd64.jar
      libjoal.so
    extracted from glugen-rt-natives-linux-amd64.jar
      libgluegen-rt.so

  for Linux armv6l
    extracted from joal-natives-linux-armv6.jar
      libjoal.so
    extracted from glugen-rt-natives-linux-armv6.jar
      libgluegen-rt.so

  for Linux armv7l
    extracted from joal-natives-linux-armv6hf.jar
      libjoal.so
    extracted from glugen-rt-natives-linux-armv6hf.jar
      libgluegen-rt.so

NOTE: joal.jar is currently replaced by an own-built version with modifications to correct the load of WAV files with appended metadata - see [GitHub PR](https://github.com/sgothel/joal/pull/15) for details of modifications.

##### jmdns.jar 
- Version 3.4.1, 429,083 bytes, 2011-08-25
- from http://sourceforge.net/projects/jmdns

##### jakarta-regexp-1.5.jar

##### ecj.jar
- Eclipse compiler 4.6RC3 from
    - http://download.eclipse.org/eclipse/downloads/drops4/S-4.6RC3-201605252000/  (via ecj-4.6RC3.jar)
    
##### WinRegistry4-4.jar
- Version 4.4
- https://sourceforge.net/projects/java-registry/
    
##### xAPlib.jar
- xAP automation protocol support
- From http://patrick.lidstone.net/html/dev_tools.html
- See license http://patrick.lidstone.net/html/xap.html

##### xbee-api-0.9.jar
- PBender 03-Mar-2014 This version comes from the XBee library source repository 
- (we needed some of the functionality, but the pre-compiled library has not been updated).


## For unit tests & development work only:

##### UmlGraph-5.7
- from http://www.umlgraph.org/download.html
- only used for ant javadoc-uml

##### junit-4.12.jar
- version 4.12
- Used for testing only, not at runtime
- from http://search.maven.org/#artifactdetails%7Cjunit%7Cjunit%7C4.12%7Cjar

##### hamcrest-core-1.3.jar
- version 1.3
- Used for testing only, not at runtime
- from http://search.maven.org/#artifactdetails%7Corg.hamcrest%7Chamcrest-core%7C1.3%7Cjar

##### jfcunit.jar
- version 2.08 
- Used for testing only, not at runtime
- from <http://jfcunit.sourceforge.net>

##### i18nchecker.jar
    Internationalization checker: used in source code development, for checking 
    proper implementation of text internationalization.  This archive need not
    be included in JMRI releases.
    From https://github.com/phamernik/i18nchecker .
    See license https://github.com/phamernik/i18nchecker/blob/master/i18nchecker/LICENSE-2.0.txt
    Usage info at https://github.com/phamernik/i18nchecker/blob/master/README.md 
    Additional useful information at https://blogs.oracle.com/geertjan/entry/i18nchecker 
        and https://blogs.oracle.com/geertjan/entry/i18nchecker_part_2

##### rscbundlecheck.jar
- check for duplicated properties

##### AppleJavaExtensions.jar
- version 1.5
- from <http://developer.apple.com/library/mac/samplecode/AppleJavaExtensions/>
- Used for building only, not at runtime

##### annotations.jar, jsr305.jar
- From Findbugs 3.0.0 from http://findbugs.sourceforge.net
- Only needed at compile/build time, not runtime
    

## Older, no longer present:

##### crimson.jar             
- version 1.1.3
- from <http://xml.apache.org/crimson/>
- No longer used as of JMRI 2.7.6

##### MRJAdaper.jar
- version, source unknown
- No longer used as of JMRI 2.13.4

##### ExternalLinkContentViewerUI.jar
- made with Java 1.6 by JMRI to handle the single jmri.util.ExternalLinkContentViewerUI class, now being carried in this jar file instead of source until we migrate to Java 1.6
- No longer used as of JMRI 2.99.1
  
##### servlet.jar:
- jakarta-servletapi-3.2.3-src/lib/servlet.jar but no longer included

