This is the JMRI distributed "lib" directory, available as the "lib" directory in JMRI/JMRI Git.   These are the library files, .jars and others, needed at build and run time.

## Contents:

Generally, we use subdirectories to hold the Git-resident versions of OS-specific code for Windows (.dll files) and Linux (.so files) so that we can separate various builds.

For example, the intelbth.dll comes in separate versions for 32-bit and 64-bit Windows, but the files have the same name.  We store them in separate subdirectories under windows/, and let the installer sort them out.

A similar mechanism is used for Linux under the linux/ directory.

macOS binaries are treated slightly differently, see the README file there.

#### Updates

If you make a change in this directory (add/change/remove a file), please make corresponding changes in the control files that are used for various JMRI development and release operations:
- build.xml - used by Ant; note that in addition to changing the classpath entry or entries, you should also ensure the three javadoc targets are linking to the proper sources
- pom.xml - used by Maven (see notes below)

If the specific library being added or updated is not published to [Maven Central](http://maven.org) by the upstream provider, run the following command after updating the pom.xml file, replacing the tokens in ALL CAPS with the correct values for that library:
```
mvn deploy:deploy-file -DgroupId=GROUP -DartifactId=ARTIFACT -Dversion=VERSION -Durl=file:./lib -DrepositoryId=lib -DupdateReleaseInfo=true -Dfile=./lib/FILE.jar
```
for example:
```
mvn deploy:deploy-file -DgroupId=net.bobis.jinput.hidraw -DartifactId=jhidrawplugin -Dversion=0.0 -Durl=file:./lib -DrepositoryId=lib -DupdateReleaseInfo=true -Dfile=./lib/jhidrawplugin.jar
```
After that, add and commit the additional files that were created within lib/

After you have committed your changes, please run
```
./scripts/check_lib_dates
```
which checks the dates of the control files to make sure they've benen updated when lib/ is updated

### Specific components:

##### JavaVersionCheckWindow.jar
- Specific class files that have to be compiled with Java 8
  - This is used to launch the warning dialog when running on Java 8

##### apiguardian-api-1.1.0.jar
- version 1.1.0
- provides Javadoc markers of API stability
- from https://github.org/apiguardian-team/apiguardian

##### jetty-*.jar
- version 9.4.28.v20200408
- provides the HTTP and WebSocket servers
- from https://search.maven.org/search?q=g:org.eclipse.jetty%20v:9.4.28.v20200428

##### jsplitbutton-1.3.1.jar
- version 1.3.1
- provides a Swing split button
- contributed by Randall Wood
- from https://github.com/rhwood/jsplitbutton
- javadoc at https://www.javadoc.io/doc/com.alexandriasoftware.swing/jsplitbutton/1.3.1

##### jinputvalidator-0.6.0.jar
- version 0.6.0
- provides validation for JComponents
- contributed by Randall Wood
- from https://github.com/rhwood/jinputvalidator
- javadoc at https://www.javadoc.io/doc/com.alexandriasoftware.swing/jinputvalidator/0.6.0

##### assertJ: assertj-core-3.12.0.jar, assertj-swing-3.9.2.jar, assertj-swing-junit-3.9.2.jar
- testing only

##### commons-lang3-3.2.1.jar
- version 3.2.1
- provides org.apache.commons.lang3
- from https://commons.apache.org/proper/commons-lang/

##### commons-text-1.2.jar
- version 1.2
- provides Apache Commons string utilities
- from https://commons.apache.org/proper/commons-text/

##### commons-csv-1.7.jar
- version 1.7
- provides Apache Commons CSV file parsing
- from https://commons.apache.org/proper/commons-csv/

##### jhall.jar
- version 2.03
- from <http://java.sun.com/javase/technologies/desktop/javahelp/>

##### log4j.jar
- version 1.2.15

##### slf4j-api-1.7.25.jar, slf4j-log4j12-1.7.25.jar, jul-to-slf4j-1.7.25.jar
- slf4j logging interface to log4j
- from http://www.slf4j.org

##### openlcb.jar
- 0.7.28 from https://oss.sonatype.org/service/local/repositories/releases/content/org/openlcb/openlcb/0.7.28/openlcb-0.7.28.jar or the maven central repository.

##### jlfgr-1_0.jar
- icons from see http://www.coderanch.com/t/341737/GUI/java/Expand-Collapse-Panels

##### purejavacomm-1.0.5.jar
- version 1.0.5
- from https://search.maven.org/artifact/org.opensmarthouse/purejavacomm/1.0.5/jar
- formerly from http://www.sparetimelabs.com/maven2/com/sparetimelabs/purejavacomm/1.0.1/
- javadoc still at https://static.javadoc.io/com.github.purejavacomm/purejavacomm/1.0.1.RELEASE

##### security.policy
- (JMRI file)

##### jdom2-2.0.6.jar
- version 2.0.6
- from <jdom.org>

##### jackson-annotations-2.10.0.jar, jackson-core-2.10.0.jar, jackson-databind-2.10.0.jar
- JSON processing library com.fasterxml.jackson
- version 2.10.0
- see http://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial
- JavaDoc http://www.javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/2.10.0

##### json-schema-validator-1.0.28.jar
- JSON Schema validation library
- from https://github.com/networknt/json-schema-validator/releases

##### org.eclipse.paho.client.mqttv3-1.2.5.jar
 - Eclipse Paho library  https://www.eclipse.org/paho/
 - mqtt-client-0.4.0.jar starting in JMRI 4.11.5, move to 1.2.5 in JMRI 4.21.3

##### BlueCove access to bluetooth
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

##### jython-standalone-2.7.2.jar
- from http://repo1.maven.org/maven2/org/python/jython-standalone/2.7.2/
- unlike jython-2.7.2.jar, includes embedded standard python libs
- unlike jython-slim-2.7.2.jar, includes embedded Java dependencies

##### jinput (including jinput.jar, three jinput DLLs, and two libjinputs)
- from <https://jinput.dev.java.net/> jinput_dist_20090401
- (most recent as of 2010-Jan-02)

##### JavaMail 1.4.1 (used to validate email address formats)
- mailapi.jar

##### Joal 2.4.0-rc-20210111
- from <https://jogamp.org/deployment/archive/rc/v2.4.0-rc-20210111/jar/>
- -javadoc at https://jogamp.org/deployment/jogamp-next/javadoc/joal/javadoc/
- cross-platform .jar files
    joal.jar
    gluegen-rt.jar

- plus helper native libraries:
  for MacOS X 10.4+
    extracted from joal-natives-macosx-universal.jar
      libjoal.dylib
      libopenal.dylib
    extracted from gluegen-rt-natives-macosx-universal.jar
      libgluegen_rt.dylib

- for Windows x86
    extracted from joal-natives-windows-i586.jar
      joal.dll
      soft_oal.dll
    extracted from glugen-rt-natives-windows-i586.jar
      gluegen_rt.dll

- for Windows x64
    extracted from joal-natives-windows-amd64.jar
      joal.dll
      soft_oal.dll
    extracted from glugen-rt-natives-windows-amd64.jar
      gluegen_rt.dll

- for Linux i386
    extracted from joal-natives-linux-i586.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-i586.jar
      libgluegen_rt.so

- for Linux x86_64
    extracted from joal-natives-linux-amd64.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-amd64.jar
      libgluegen_rt.so

- for Linux armv6l
    extracted from joal-natives-linux-armv6hf.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-armv6hf.jar
      libgluegen_rt.so

- for Linux armv7l
    extracted from joal-natives-linux-armv6hf.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-armv6hf.jar
      libgluegen_rt.so

- for Linux aarch64
    extracted from joal-natives-linux-aarch64.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-aarch64.jar
      libgluegen_rt.so

##### jmdns.jar
- Version 3.5.5 (2018-12-04)
- from https://github.com/jmdns/jmdns/releases

##### jna-5.9.0.jar
- Java Native Access library
- from https://mvnrepository.com/artifact/net.java.dev.jna/jna/5.9.0
- See also https://github.com/java-native-access/jna

##### jna-platform-5.9.0.jar
- Java Native Access platform-specific utilities library
- from https://mvnrepository.com/artifact/net.java.dev.jna/jna-platform/5.9.0


##### pi4j-core-1.2.jar, pi4j-device-1.2.jar, pi4j-gpio-extension-1.2.jar
- Pi4j
- from https://pi4j.com/
- Used for supporting GPIO pins on a raspberry pi. pi4j-core is required at compile time.  pi4j-device and pi4j-gpio-extension may be used at runtime (by scripts) to control devices attached to the raspberry pi.

##### thumbnailator-0.4.8.jar
- Thumbnailator
- from https://github.com/coobird/thumbnailator
- Used by jmri.util.swing.ResizableImagePanel to read Exif information in JPEG files.

##### vecmath-1.5.2.jar
- from Java3D 1.5.2
- from http://search.maven.org/#search%7Cga%7C1%7Cg%3Ajavax.vecmath

##### xAPlib.jar
- xAP automation protocol support
- From http://patrick.lidstone.net/html/dev_tools.html
- See license http://patrick.lidstone.net/html/xap.html

##### xbee-java-library-1.2.1.jar
- Official XBee support library from Digi
- from https://github.com/digidotcom/XBeeJavaLibrary

##### xercesImpl.jar
- version Xerces-J 2.11.0
- from http://www.apache.org/dist/xerces/j/

##### usb-api-1.0.2.jar, usb4java-*.jar, libusb4java-*.jar
- usb4java version 1.3.0
- support for direct USB device usage
- from https://github.com/usb4java/usb4java/releases/tag/usb4java-1.3.0
  and https://github.com/usb4java/usb4java-javax/releases/tag/usb4java-javax-1.3.0
- lib/libusb4java-1.3.0-darwin-aarch64.jar made from a .dylib found at https://github.com/developersu/ns-usbloader/issues/91

## For unit tests & development work only:

##### ArchUnit: archunit-0.11.0.jar archunit-junit4-0.11.0.jar
- See https://www.archunit.org
- Jars from https://search.maven.org/search?q=g:com.tngtech.archunit

##### checker-framework directory and contents
- The Checker Framework 2.0.1 (1-Jun-2016)
- From http://types.cs.washington.edu/checker-framework/

##### ecj.jar
- Eclipse compiler 4.10 from
    - https://download.eclipse.org/eclipse/downloads/drops4/R-4.10-201812060815/ (via selecting ecj-4.10.jar) January 3, 2019
- used in ant warnings target

##### jemmy-22-00c9f753cd0a.jar
- Built from rev 22 repo after changeset 22:00c9f753cd0a; see http://hg.openjdk.java.net/code-tools/jemmy/v2/rev/00c9f753cd0a
- See also http://hg.openjdk.java.net/code-tools/jemmy/v2/

##### junit-4.12.jar
- version 4.12
- from http://search.maven.org/#artifactdetails%7Cjunit%7Cjunit%7C4.12%7Cjar

##### hamcrest-core-1.3.jar
- version 1.3
- Used for testing only, not at runtime
- from http://search.maven.org/#artifactdetails%7Corg.hamcrest%7Chamcrest-core%7C1.3%7Cjar

##### i18nchecker.jar
- Internationalization checker: used in source code development, for checking proper implementation of text internationalization.
- From https://github.com/JMRI/i18nchecker
- Note: We use a custom version of the original in https://github.com/phamernik/i18nchecker
- See license https://github.com/phamernik/i18nchecker/blob/master/i18nchecker/LICENSE-2.0.txt
- Usage info at https://github.com/phamernik/i18nchecker/blob/master/README.md
- Additional useful information at https://blogs.oracle.com/geertjan/entry/i18nchecker and https://blogs.oracle.com/geertjan/entry/i18nchecker_part_2

##### PlantUML
- plantuml.jar
    was from plantuml.org, now from https://github.com/plantuml/plantuml.git
- umldoclet.jar
    downloaded as umldoclet-2.0.10-javadoc.jar
    from https://github.com/talsma-ict/umldoclet/releases
    see https://github.com/talsma-ict/umldoclet

##### rscbundlecheck.jar
- check for duplicated properties

##### system-rules-1.16.0.jar
- Handle rules for testing calls to java.System methods

##### springframework-*
- version 5.1.14
- from https://search.maven.org/search?q=g:org.springframework%20v:5.1.14.RELEASE
- Mocks Java Servlet requests and responses

##### AppleJavaExtensions.jar
- version 1.5
- from <http://developer.apple.com/library/mac/samplecode/AppleJavaExtensions/>
- Used for building only, not at runtime

#### SpotBugs static analysis

##### jcip-annotations-1.0.jar
- From Java Concurrency In Practice (http://jcip.net)
- Only needed at compile/build time, not runtime
- http://repo1.maven.org/maven2/net/jcip/jcip-annotations/1.0/

##### jsr305.jar
- From FindBugs 3.0.0 from http://findbugs.sourceforge.net
- Only needed at compile/build time, not runtime

##### spotbugs-annotations.jar
- From SpotBugs 3.1.7
- Only needed at compile/build time, not runtime
- http://repo1.maven.org/maven2/com/github/spotbugs/spotbugs-annotations/3.1.7/


## Older, no longer present:

##### UmlGraph-5.7
- from http://www.umlgraph.org/download.html
- only used for ant javadoc-uml with earlier Java

##### javacsv.jar
- version 2.0
- from http://javacsv.sourceforge.net
- No longer used as of JMRI 4.19.3

##### crimson.jar
- version 1.1.3
- from http://xml.apache.org/crimson/
- No longer used as of JMRI 2.7.6

##### MRJAdaper.jar
- version, source unknown
- No longer used as of JMRI 2.13.4

##### ExternalLinkContentViewerUI.jar
- made with Java 1.6 by JMRI to handle the single jmri.util.ExternalLinkContentViewerUI class, now being carried in this jar file instead of source until we migrate to Java 1.6
- No longer used as of JMRI 2.99.1

##### servlet.jar:
- jakarta-servletapi-3.2.3-src/lib/servlet.jar but no longer included

##### RXTXcomm.jar, librxtxSerial.jnilib
- From Rxtx-2.2pre2  http://rxtx.qbang.org (http://rxtx.qbang.org/pub/rxtx/rxtx-2.2pre2-bins.zip)
- The win32 and win64 directories contain the necessary rxtxSerial.dll for the two windows varients
- The i686-pc-linux-gnu directory contains two .so libraries for 32-bit Linux
- For MacOS X:
    macosx/librxtxSerial.jnilib     169488  from rxtx-2.2pre2-bins distribution
    macosx/ppc/librxtxSerial.jnilib 301908  built for MacOS X 10.4 by John Plocher 2010-02-04
- No longer used as of JMRI 4.7.X

##### Serialio.jar
- from <http://serialio.com>
- No longer used as of JMRI 4.7.X

##### libusb-jar (ch.ntb.usb.jar)
        http://inf.ntb.ch/infoportal/help/index.jsp?topic=/ch.ntb.infoportal/tools.html
        http://libusb.wiki.sourceforge.net/
        libusb installers from "TWAIN SANE" http://www.ellert.se/twain-sane/

- version 0.5.7
- libusbJava.jnilib for MacOS X
        to get 64-bit, from http://wiki.ztex.de/doku.php?id=en:software:porting#macos_port
        requires /usr/local/lib/libusb-0.1.4.dylib via MacPorts or Homebrew or an installer from http://www.ellert.se/twain-sane/
- LibusbJava.dll for Windows is 0.2.3.0 (Feb 18, 2008)
- libusbJava.so for Linux was built on Ubuntu 7.10 w libusb 2:0.1.12-7
- No longer used as of JMRI 4.9.1
