REM Windows batch file to launch JHelpDev with the current directory set

java -DHOMEDIR=${PWD} -Xmx2000m -classpath ".;jhelpdev.jar;jars/jhall.jar;jars/xmlenc.jar" net.sourceforge.jhelpdev.JHelpDevFrame
