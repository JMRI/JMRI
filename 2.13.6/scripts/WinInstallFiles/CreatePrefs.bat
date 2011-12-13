@echo off

dir /b /a:d "%HOMEDRIVE%%HOMEPATH%\JMRI" >nul 2>nul && goto end

echo Creating "%HOMEDRIVE%%HOMEPATH%\JMRI"
mkdir "%HOMEDRIVE%%HOMEPATH%\JMRI"

:end
echo Done.
