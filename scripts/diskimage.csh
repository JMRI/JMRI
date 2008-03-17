#! /bin/csh -f
#
# Short csh script to create JMRI MacOS X disk image ($Revision: 1.2 $)
#
# Assumes that the program is being run from the distribution directory.
#
# First argument is the "Release" value, e.g. CVS tag, 2nd is the number,
# e.g. 1.2.3
#
# Copyright 2007 Bob Jacobsen
#


# shouldn't be anything left over, but if so, clean up
hdiutil detach /Volumes/"JMRI ${2}"
rm -f temp.dmg
rm -f JMRI.${2}.dmg

# create disk image and mount
hdiutil create -size 40MB -fs HFS+ -layout SPUD -volname "JMRI ${2}" temp.dmg
open temp.dmg

# have to wait for disk to mount
sleep 10

# copy contents
(cd MacOSX; tar c JMRI) | (cd /Volumes/"JMRI ${2}"; tar x)

# add an Applications icon
# parens around following make the cd temporary
(cd /Volumes/"JMRI ${2}" ; ln -s /Applications .)

# eject
hdiutil detach /Volumes/"JMRI ${2}"

# pack for distribution
hdiutil convert temp.dmg -format UDZO -o JMRI.${2}.dmg

# clean up
rm -f temp.dmg
