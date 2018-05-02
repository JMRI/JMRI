#! /bin/csh -f
#
# csh script to launch JHelpDev with the current directory set
#

java -DHOMEDIR=${PWD} -Djava.io.tmpdir=/tmp -Xmx2000m -classpath ".:jhelpdev.jar:jars/jhall.jar:jars/xmlenc.jar" net.sourceforge.jhelpdev.JHelpDevFrame
