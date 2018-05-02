#! /bin/sh -f
#
# Run native2ascii on all .properties files.
#
# http://docs.oracle.com/javase/7/docs/technotes/tools/windows/native2ascii.html
#
# See also java/translate.sh
#
#
# Bob Jacobsen, copyright 2013
#

# Make list of properties classes, removing a couple that are intended not for translation
VALS=`find java/src -name \*.properties -print \
| grep -v apps/AppsStructureBundle.properties \
| grep -v jmri/web/server/Services.properties \
| grep -v jmri/web/server/FilePaths.properties`

# index over those to translate
for x in $VALS; do \
  # translate one file, writing to renamed file
  native2ascii $x $x
done


