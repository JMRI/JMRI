@REM Start the InstallTest program from JMRI ($Revision$)
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

@LaunchJMRI /debug /noisy apps.InstallTest.InstallTest %1 %2 %3 %4 %5 %6 %7 %8 %9

@IF NOT ERRORLEVEL 1 GOTO skipJMRI

@echo .
@echo Something is wrong with invoking JMRI. Check JMRI installation.

:skipJMRI
@echo .
@echo InstallTest completed
@pause
