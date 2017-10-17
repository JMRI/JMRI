REM Windows batch file to launch JHelpDev with the current directory set

java -DHOMEDIR=${PWD} -Xmx1200m -classpath ".;jhelpdev.jar;jars/jhall.jar;jars/xmlenc.jar" net.sourceforge.jhelpdev.JHelpDevFrame
