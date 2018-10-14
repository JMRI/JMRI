@REM Start the InstallTest program from JMRI
@REM @author Ken Cameron
@REM @author Matthew Harris

@echo off
set THISFILE=%~fx0

echo Testing for 32/64-bit Windows
echo.
set PROGFILES86ROOT=%PROGRAMFILES(X86)%
if "%PROGFILES86ROOT%"=="" (
	echo Running on 32-bit Windows
	set IS64=NO
) else (
	echo Running on 64-bit Windows
	echo.
	echo Testing for 32/64-bit process
	echo.
	if "%PROCESSOR_ARCHITECTURE%"=="AMD64" (
		echo Running as a 64-bit process
		set IS64=YES
	) else (
		echo Running as a 32-bit process
		set IS64=NO
	)
)
echo.

echo Testing for Java working
echo. 
java -version

if errorlevel 1 (
	echo.
	echo Some problem finding/running JAVA.

	if "%IS64%"=="YES" ( 
		echo Need to check for 32-bit JAVA install.
		echo.
		echo Now trying to re-run as a 32-bit process....
		echo.
		"%windir%\SysWoW64\cmd.exe" /c "%THISFILE%" %1 %2 %3 %4 %5 %6 %7 %8 %9
		exit
	) else (
		echo You must install JAVA first or fix your JAVA install.
	)
	echo.
) else (
	echo.
	echo Java is correctly working.
	pause
	echo.
	echo Now testing JMRI

	LaunchJMRI /debug /noisy apps.InstallTest.InstallTest %1 %2 %3 %4 %5 %6 %7 %8 %9

	if errorlevel 1 (
		echo.
		echo Something is wrong with invoking JMRI. Check JMRI installation.
	)
)
pause

echo.
echo Test complete
pause
