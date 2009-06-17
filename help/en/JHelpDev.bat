REM Windows batch file to launch JHelpDev with the current 

java -DHOMEDIR=${PWD} -classpath ".;jhelpdev.jar;lib/jhall.jar;lib/xmlenc.jar" net.sourceforge.jhelpdev.JHelpDevFrame
