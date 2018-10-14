#! /bin/sh -f
#
# For purposes of testing internationalization, this script creates
# temporary properties files in the "classes" tree in a JMRI development
# directory.
#
# See 
#    <http://jmri.org/help/en/html/doc/Technical/I8N.shtml>
#    <http://www.geocities.com/harry_robinson_testing/klingon.htm>
# 
# Assumes that the program is being run from the "java" build directory.
# Do "ant debug" first to copy files into the classes directory
#
# To test your code, use the "ant locale" target to run PanelPro and look for
# strings that are not in upper case.
#
# Bob Jacobsen, copyright 2008
#
# Limitations:
# 1) This doesn't really translate to Klingon, though that would be really cool.
# 2) Insertion keys, e.g. {0}, are not preserved in the strings.

# Make list of properties classes, removing a couple that are intended not for translation
VALS=`find target/classes -name \*.properties ! -name \*_\?\?.properties ! -name \*_\?\?\?.properties -print \
| grep -v apps/AppsStructureBundle.properties \
| grep -v jmri/web/server/Services.properties \
| grep -v jmri/web/server/FilePaths.properties`

# index over those to translate
for x in $VALS; do \
  # translate one file, writing to renamed file
  cat $x | awk '{ print $1 "=" toupper($2) } ' FS="\=" >`echo $x|sed "s/\./_tlh./"`; \
done


