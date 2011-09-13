#!/bin/bash
#
# Create the JMRI MacOS X disk image
#   This is structured to use the Mac OS X tools when run on a Mac, and native Linux tools when run on a Linux box.
#
#   The contents of the image are the same either way, and the tools are similar in capability, so we do it this way
#   for consistency of the final product.
#
# Copyright 2007,2011 Bob Jacobsen, david d zuhn
#

OPTS=`getopt -o v:o:d: -- "$@"`
if [ $? != 0 ]
then
  echo "Bad option parsing" >&2
  exit 1
fi

eval set -- "$OPTS"
while true
do
  case "$1" in 
    -v)
        REL_VER=$2; shift 2 ;;
    -o)
        OUTPUT=$2; shift 2 ;;
    -d)
        INPUT=$2; shift 2 ;;
    --)
        shift; break ;;
    *)
        echo "Unknown option '$1' [-d inputdir | -v vers | -o outputfile ]" >&2
	exit 1
	;;
  esac
done

if [ "$REL_VER" = "" ]
then
  echo "Must set version with -v"
  exit 1
fi

if [ "$OUTPUT" = "" ]
then
  echo "Must set output with -o"
  exit 1
fi

if [ "$INPUT" = "" ]
then
  echo "Must set input directory with -d"
  exit 1
fi

set -e   # bail on errors
set -x   # show our work

if [ -x /usr/bin/hdiutil ]
then
  # if a Linux box were to have hdiutil, I think that would be the preferable route to follow
  # although it's pretty unlikely
  SYSTEM=MACOSX
else
  SYSTEM=LINUX
fi

if ! tmpdir=`mktemp -d -t JMRI.XXXXXX`
then
  echo "Cannot create temporary directory"
  exit 1
fi

trap 'rm -rf temp.dmg "$IMAGEFILE" "$tmpdir" >/dev/null 2>&1'  0
trap 'exit 2' 1 2 3 15

# shouldn't be anything left over, but if so, clean up

IMAGEFILE="$INPUT/JMRI.${REL_VER}.dmg"
rm -f temp.dmg $IMAGEFILE


# create disk image and mount

if [ "$SYSTEM" = "MACOSX" ]
then
    hdiutil create -size 100MB -fs HFS+ -layout SPUD -volname "JMRI ${REL_VER}" "$IMAGEFILE"
    hdiutil attach $IMAGEFILE -mountpoint "$tmpdir" -nobrowse
    trap '[ "$EJECTED" = 0 ] && hdiutil eject "$IMAGEFILE"' 0
else
    dd if=/dev/zero of="$IMAGEFILE" bs=1M count=100
    mkfs.hfsplus -v "JMRI ${REL_VER}" "${IMAGEFILE}"
    sudo mount -t hfsplus -o loop,rw,uid=$UID "$IMAGEFILE" $tmpdir
    trap '[ "$EJECTED" = 0 ] && sudo umount "$tmpdir"' 0
fi

# wait for the mountpoint to settle down...
#  I don't think we need this...
#sleep 10

if [ -w "$tmpdir" ]
then
  SUDO=
else
  SUDO=sudo
fi

# copy contents of the Mac OS X distribution to the newly mounted filesysten
tar -C "$INPUT" -cf - JMRI | $SUDO tar -C "$tmpdir" -xf -

# add an Applications icon
$SUDO ln -s /Applications "$tmpdir"

# now, how do we make a nice background picture in the folder with directions on how to drag'n'drop to Applications?


# eject the mounted disk image
if [ "$SYSTEM" = "MACOSX" ]
then
  hdiutil detach "$tmpdir" && EJECTED=1
else
  sudo umount "$tmpdir" && EJECTED=1
fi


# pack into a smaller disk image for distribution
if [ "$SYSTEM" = "MACOSX" ]
then
  rm -f "$OUTPUT"
  hdiutil convert "$IMAGEFILE" -format UDZO -o "$OUTPUT"
else
  # we don't know how to do this on linux right now...
  # so we just create the output file directly from the input file
  mv "$IMAGEFILE" "$OUTPUT"
fi

# clean up
# this should also be handled by the trap, but it doesn't hurt...
rm -rf temp.dmg $tmpdir $IMAGEFILE
