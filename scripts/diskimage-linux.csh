#! /bin/csh -f
#
# Short csh script to create JMRI MacOS X disk image ($Revision: 1.1 $)
#
# Assumes that the program is being run from the distribution directory.
#
# First argument is the "Release" value, e.g. CVS tag, 2nd is the number,
# e.g. 1.2.3
#
# Copyright 2007 Bob Jacobsen
#


# shouldn't be anything left over, but if so, clean up
mkdir mntpoint
rm -f JMRI.${2}.dmg

# create disk image and mount
# hdiutil create -size 100MB -fs HFS+ -layout SPUD -volname "JMRI ${2}" temp.dmg
# open temp.dmg
dd if=/dev/zero of="JMRI.${2}".dmg bs=1M count=100
mkfs.hfsplus -v "JMRI ${2}" JMRI.${2}.dmg
sudo mount -t hfsplus -o loop JMRI.${2}.dmg mntpoint

# have to wait for disk to mount
sleep 20

# copy contents
(cd MacOSX; tar c JMRI) | (cd mntpoint ; sudo tar x)

# add an Applications icon
# parens around following make the cd temporary
(cd mntpoint ; sudo ln -s /Applications .)

# eject
sudo umount mntpoint

# pack for distribution
#hdiutil convert temp.dmg -format UDZO -o JMRI.${2}.dmg

# clean up
#rm -f temp.dmg
rm -rf mntpoint
