#! /bin/csh -f
#
# csh script to launch JHelpDex with the current directory set
#
#($Revision: 1.1 $)
#

java -jar -DHOMEDIR=${PWD} jhelpdev.jar 
