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
which checks the dates of the control files to make sure they've been updated when lib/ is updated

### Specific components:

##### JavaVersionCheckWindow.jar
- Specific class files that have to be compiled with Java 8
  - This is used to launch the warning dialog when running on Java 8
        % jdk8
        % cd java/src
        % javac apps/JavaVersionCheckWindow.java
        % jar cf ../../lib/JavaVersionCheckWindow.jar apps/JavaVersionCheckWindow*.class
        % jar tf ../../lib/JavaVersionCheckWindow.jar
        META-INF/
        META-INF/MANIFEST.MF
        apps/JavaVersionCheckWindow$Compatibility.class
        apps/JavaVersionCheckWindow.class
        % rm apps/*.class

##### jmri.script.jsr223graalpython.jar
- Encapsulation of GraalVM-specific code.
- See java/graalvm/README.MD for build instructions

##### apiguardian-api-1.1.0.jar
- version 1.1.0
- provides Javadoc markers of API stability
- from https://github.org/apiguardian-team/apiguardian

#####  batik*
    batik-js-1.8.jar is a "patched version of Rhino" needed for native-image closure with batik 1.4
            https://mvnrepository.com/artifact/org.apache.xmlgraphics/batik-js/1.8

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

##### commons-compress-1.18.jar
- version 1.18
- webdrivermanager dependency

##### commons-lang3-3.7.jar
- version 3.7
- provides org.apache.commons.lang3
- from https://commons.apache.org/proper/commons-lang/

##### commons-logging-1.2.jar
- version 1.2

##### commons-net-3.9.0.jar
- version 3.9.0
- not used in direct compilation, not clear where it is used
- from https://commons.apache.org/proper/commons-net/download_net.cgi

##### commons-text-1.2.jar
- version 1.2
- provides Apache Commons string utilities
- from https://commons.apache.org/proper/commons-text/

##### commons-csv-1.9.0.jar
- version 1.9.0
- provides Apache Commons CSV file parsing
- from https://commons.apache.org/proper/commons-csv/

##### commons-io-2.11.0.jar
- version 2.11.0
- JMRI uses this for file selectors


##### jhall.jar
- version 2.03
- from <http://java.sun.com/javase/technologies/desktop/javahelp/>

##### log4j-api-2.20.0.jar, log4j-core-2.20.0.jar
- version 2.20.0
- from https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api/2.20.0
- from https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core/2.20.0

##### log4j-slf4j2-impl.jar
- from https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j2-impl/2.20.0
- slf4j bridge to log4j2

##### slf4j-api-2.0.7.jar
- slf4j API
- from https://mvnrepository.com/artifact/org.slf4j/slf4j-api

##### jul-to-slf4j-2.0.7.jar
- java.util.logging to slf4j
- from https://mvnrepository.com/artifact/org.slf4j/jul-to-slf4j/2.0.7

##### openlcb.jar
 - 0.7.37 from https://repo.maven.apache.org/maven2/org/openlcb/openlcb/0.7.35/openlcb-0.7.37.jar
 - plus PRs through 267 from https://github.com/openlcb/OpenLCB_Java/pulls?q=is%3Apr+is%3Amerged

##### jlfgr-1_0.jar
- icons from see http://www.coderanch.com/t/341737/GUI/java/Expand-Collapse-Panels

##### jSerialComm-2.10.4.jar
- Supported serial lib since JMRI 5.7.1
- from https://fazecast.github.io/jSerialComm/

##### purejavacomm-1.0.5.jar
- version 1.0.5 plus custom change to the Mac M1 support (only)
- from https://search.maven.org/artifact/org.opensmarthouse/purejavacomm/1.0.5/jar
- formerly from http://www.sparetimelabs.com/maven2/com/sparetimelabs/purejavacomm/1.0.1/
- javadoc still at https://static.javadoc.io/com.github.purejavacomm/purejavacomm/1.0.1.RELEASE
- we are migrating away from this for JMRI itself, but we keep distributing it because BiDib and some scripts use it

##### security.policy
- (JMRI file)

##### jdom2-2.0.6.jar
- version 2.0.6
- from <jdom.org>

##### jackson-annotations-2.13.4.jar, jackson-core-2.13.4.jar, jackson-databind-2.13.4.2.jar
- JSON processing library com.fasterxml.jackson
- see http://www.journaldev.com/2324/jackson-json-processing-api-in-java-example-tutorial

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

##### jinput (including jinput-2.0.9.jar and unpacked jinput-2.0.9-natives-all.jar)
- 2.0.9 from maven central

##### JavaMail 1.4.1 (used to validate email address formats)
- mailapi.jar

##### Joal 2.4.0-rc-20230507 (Windows x86 and Linux i386 from v2.4.0-rc-20210111)
- from https://jogamp.org/deployment/archive/rc/v2.5.0-rc-20230507/jar/ (no longer accessible)
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

- Windows and Linux libraries from v2.5.0-rc-20230507 <https://jogamp.org/deployment/archive/master/gluegen_954-joal_672-jogl_1522-jocl_1160/jar/>
  for Windows x64
    extracted from joal-natives-windows-amd64.jar
      joal.dll
      OpenAL32.dll
    extracted from glugen-rt-natives-windows-amd64.jar
      gluegen_rt.dll

  for Windows x86
    not provided, see v2.4.0-rc-20210111 below

  for Linux x86_64
    extracted from joal-natives-linux-amd64.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-amd64.jar
      libgluegen_rt.so

  for Linux armv6l
    extracted from joal-natives-linux-armv6hf.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-armv6hf.jar
      libgluegen_rt.so

  for Linux armv7l
    extracted from joal-natives-linux-armv6hf.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-armv6hf.jar
      libgluegen_rt.so

  for Linux aarch64
    extracted from joal-natives-linux-aarch64.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-aarch64.jar
      libgluegen_rt.so

  for Linux i386
    not provided, see v2.4.0-rc-20210111 below

- Windows and Linux libraries from v2.4.0-rc-20210111 <https://jogamp.org/deployment/archive/rc/v2.4.0-rc-20210111/jar/> (no longer accessible)
  for Windows x86
    extracted from joal-natives-windows-i586.jar
      joal.dll
      soft_oal.dll
    extracted from glugen-rt-natives-windows-i586.jar
      gluegen_rt.dll

  for Linux i386
    extracted from joal-natives-linux-i586.jar
      libjoal.so
      libopenal.so
    extracted from glugen-rt-natives-linux-i586.jar
      libgluegen_rt.so

##### jmdns.jar
- Version 3.5.5 (2018-12-04)
- from https://github.com/jmdns/jmdns/releases

##### jna-5.13.0.jar
- Java Native Access library
- from https://mvnrepository.com/artifact/net.java.dev.jna/jna/5.13.0
- See also https://github.com/java-native-access/jna

##### jna-platform-5.13.0.jar
- Java Native Access platform-specific utilities library
- from https://mvnrepository.com/artifact/net.java.dev.jna/jna-platform/5.13.0


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

##### xbee-java-library-1.3.1.jar
- Official XBee support library from Digi
- from https://github.com/digidotcom/XBeeJavaLibrary

##### xercesImpl-2.12.2.jar
- version Xerces-J 2.12.2
- from Maven https://mvnrepository.com/artifact/xerces/xercesImpl/2.12.2

##### xml-apis-1.4.01.jar
#- For xercesImpl 2.12.2
# but we use the one provided by the JRE - see pom.xml - so there's no file for this

##### xml-apis-ext-1.3.04.jar
- from Maven

##### usb-api-1.0.2.jar, usb4java-*.jar, libusb4java-*.jar
- usb4java version 1.3.0
- support for direct USB device usage
- from https://github.com/usb4java/usb4java/releases/tag/usb4java-1.3.0
  and https://github.com/usb4java/usb4java-javax/releases/tag/usb4java-javax-1.3.0
- lib/libusb4java-1.3.0-darwin-aarch64.jar made from a .dylib found at https://github.com/developersu/ns-usbloader/issues/91

##### beansbinding-1.2.1.jar
- version 1.2.1
- used in web server preference panels

##### cglib-nodep-2.2.2.jar
- version 2.2.2

##### checkstyle-8.29-all.jar
- version 8.29

##### datatable-1.1.12.jar and datatable-dependencies-1.1.12.jar
- version 1.1.12
- cucumber 4.3.1 dependency

##### gherkin-5.1.0.jar
- cucumber 4.3.1 dependency

##### gherkin-jvm-deps-1.0.4.jar
- version 1.0.4

##### hid4java-0.5.0.jar
- version 0.5.0
- cross-platform HID USB, eg RailDriverMenuItem

##### jarchivelib-1.0.0.jar
- version 1.0.0
- dependency for webdrivermanager

##### javacc.jar
- version 7.0.3

##### javassist-3.20.0-GA.jar
- initially used to mock an XBee Connection for testing.

##### javax.servlet-api-3.1.0.jar
- version 3.1.0
- Related to Jetty Web Server

##### jsoup-1.15.3.jar
- version 1.15.3
- used to rebuild Help metadata

##### objenesis-2.2.jar
- version 2.2
- initially used to mock an XBee Connection for testing.

##### opentest4j-1.2.0.jar
- version 1.2.0

##### tag-expressions-1.1.1.jar
- version 1.1.1
- cucumber related

##### typetools-0.5.0.jar
- version 0.5.0
- cucumber dependency

##### webdrivermanager-4.2.2.jar
- version 4.2.2

##### xmlgraphics-commons-2.6.jar
- version 2.6
- batik related

##### BiDiB protocol implementation: jbidibc-*-2.0.18.jar, bidibwizard-*-2.0.18.jar
- version 2.0.18
- bidibwizard*.jar supports the BiDiB connection simulation

##### commons-collections4-4.4.jar
- version 4.4
- used by jbidibc/bidibwizard

##### eventbus-1.4.jar
- version 1.4
- used by jbidibc/bidibwizard

##### javax.activation-api-1.2.0.jar
- version 1.2.0
- used by jbidibc/bidibwizard

##### jaxb-api-2.3.1.jar, jaxb-core-2.3.0.1.jar, jaxb-impl-2.3.2.jar
- version 2.3
- used by jbidibc/bidibwizard

##### jgoodies-binding-2.13.0.jar
- version 2.13.0
- used by jbidibc/bidibwizard

##### jgoodies-common-1.8.1.jar
- version 1.8.1
- used by jbidibc/bidibwizard

## For unit tests & development work only:

##### ArchUnit: archunit-*.jar, archunit-junit5-api-*.jar, archunit-junit5-engine-*.jar, archunit-junit5-engine-api*.jar
- See https://www.archunit.org
- Jars from https://search.maven.org/search?q=g:com.tngtech.archunit
- version 1.0.0-rc1

##### byte-buddy-1.10.14
- version 1.10.14

##### checker-framework directory and contents
- The Checker Framework 2.0.1 (1-Jun-2016)
- From http://types.cs.washington.edu/checker-framework/

##### cucumber-core-4.3.1.jar
- version 4.3.1
- Used for testing only, not at runtime

##### cucumber-expressions-6.2.2.jar
- version 6.2.2

##### cucumber-html-0.2.7.jar
- version 0.2.7

##### cucumber-java-4.3.1.jar
- version 4.3.1

##### cucumber-java8-4.3.1.jar
- version 4.3.1

##### cucumber-junit-4.3.1.jar
- version 4.3.1

##### cucumber-jvm-deps-1.0.6.jar
- version 1.0.6

##### cucumber-picocontainer-4.3.1.jar
- version 4.3.1

##### ecj.jar
- Eclipse compiler 4.19 from
    - https://archive.eclipse.org/eclipse/downloads/drops4/R-4.19-202103031800/ (via selecting ecj-4.10.jar) March 3, 2021
- used in ant warnings target

##### jemmy-2.3.1.1-RELEASE125.jar
- Sept 13, 2021
- From https://mvnrepository.com/artifact/org.netbeans.external/jemmy-2.3.1.1/RELEASE125

##### junit-jupiter-*
- version 5.9.1

##### junit-platform-*
- version 1.9.1

##### junit-vintage-engine-5.9.1.jar

##### mockito-core mockito-inline mockito-junit-jupiter
- version 3.5.11

##### OpenIDE Utilities
- org-openide-util-lookup-RELEASE150.jar
- Downloaded from Maven Central 2022-10-07
- https://mvnrepository.com/artifact/org.netbeans.api/org-openide-util-lookup/RELEASE150

##### PlantUML
- plantuml.jar
    from plantuml.org
    1.2023.1
- umldoclet.jar
    downloaded as umldoclet-2.0.10-javadoc.jar
    from https://github.com/talsma-ict/umldoclet/releases
    see https://github.com/talsma-ict/umldoclet

##### rscbundlecheck.jar
- check for duplicated properties

##### selenium-server-standalone-3.141.59

##### system-rules-1.16.0.jar
- Handle rules for testing calls to java.System methods

##### springframework-*
- version 5.1.14
- from https://search.maven.org/search?q=g:org.springframework%20v:5.1.14.RELEASE
- Mocks Java Servlet requests and responses

##### jcip-annotations-1.0.jar
- From Java Concurrency In Practice (http://jcip.net)
- Only needed at compile/build time, not runtime
- http://repo1.maven.org/maven2/net/jcip/jcip-annotations/1.0/

##### jsr305.jar
- From FindBugs 3.0.0 from http://findbugs.sourceforge.net
- Only needed at compile/build time, not runtime

##### spotbugs-annotations-4.7.3.jar
- From SpotBugs 4.7.3
- Only needed at compile/build time, not runtime
- https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs-annotations/4.7.3

##### picocontainer-2.15.jar
- version 2.15
- Required by Cucumber 4.3.1 Tests.
- Used for testing only, not at runtime


##### org.jacoco.ant-0.8.11-nodeps.jar
- version 0.8.11

## Older, no longer present:

##### junit-4.12.jar
- version 4.12
- from http://search.maven.org/#artifactdetails%7Cjunit%7Cjunit%7C4.12%7Cjar
- JUnit4 classes currently accessed via junit-platform-console-standalone

##### UmlGraph-5.7
- from http://www.umlgraph.org/download.html
- only used for ant javadoc-uml with earlier Java
- removed in JMRI 5.1.3

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

##### hamcrest-core-1.3.jar
- version 1.3
- Used for testing only, not at runtime
- from http://search.maven.org/#artifactdetails%7Corg.hamcrest%7Chamcrest-core%7C1.3%7Cjar
- No longer used as of JMRI 5.1.3

##### org-openide-util-RELEASE126.jar
- No longer used as of JMRI 5.1.5

#### SpotBugs static analysis
- used in pom.xml
