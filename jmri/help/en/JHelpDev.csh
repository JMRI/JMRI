#! /bin/csh -f
#
# csh script to launch JHelpDex with the current directory set
#
#($Revision$)
#

java -DHOMEDIR=${PWD} -Djava.io.tmpdir=/tmp -Xmx2000m -classpath ".:jhelpdev.jar:lib/jhall.jar:lib/xmlenc.jar" net.sourceforge.jhelpdev.JHelpDevFrame
