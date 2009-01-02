#! /bin/csh -f
#
# csh script to launch JHelpDex with the current directory set
#
#($Revision: 1.2 $)
#

java -DHOMEDIR=${PWD} -classpath ".:jhelpdev.jar:lib/jhall.jar:lib/xmlenc.jar" net.sourceforge.jhelpdev.JHelpDevFrame
