@REM Start the InstallTest program from JMRI 2.1.7 ($Revision: 1.1 $)
@REM @author Ken Cameron
@echo testing for Java working
@echo . 
java -version

@IF NOT ERRORLEVEL 1 GOTO javaOk
@echo .
@echo Some problem finding/running JAVA.

@echo You must install JAVA first or fix your JAVA install.
@echo .
@pause
@goto skipJMRI
:javaOk
@echo .
@echo Java is correctly working.
@pause
@echo Now testing JMRI

java -noverify -Dsun.java2d.d3d=false -Djava.security.policy=security.policy -Djava.library.path=.;lib -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw -Djava.class.path=".;classes;jmriplugins.jar;jmriweb.jar;jmriserver.jar;jmri.jar;comm.jar;Serialio.jar;log4j.jar;jhall.jar;crimson.jar;jdom.jar;jython.jar;javacsv.jar;jinput.jar;ch.ntb.usb.jar;MRJAdapter.jar;vecmath.jar" apps.InstallTest.InstallTest %1 %2 %3 %4 %5 %6 %7 %8 %9

@IF NOT ERRORLEVEL 1 GOTO skipJMRI

@echo .
@echo Something is wrong with invoking JMRI. Check JMRI installation.
@pause

:skipJMRI
