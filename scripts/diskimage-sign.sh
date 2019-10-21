#!/bin/bash
#
# Create a signed and notarized JMRI macOS disk image (from diskimage.sh) from an unsigned one
#   This is structured to use the Mac OS X tools when run on a Mac, and native Linux tools when run on a Linux box,
#   but only the OS X version is being actively developed because it relies on some Apple-specific
#   tools for notarization.
#
# arguments are:
#    Release version string e.g. "4.5.5", e.g. "${release.version-string}" from Ant
#    Output DMG file pathname e.g. dist/release/JMRI.4.5.5.dmg, "${dist.release}/JMRI.${release.version-string}.dmg" in Ant
#    Input DMG file pathname e.g. dist/release/JMRI.4.5.5-unsigned.dmg,
#    ID of signing certificate, i.e. "Developer ID Application: My Name"
#    Apple ID for notarization
#    Application-specific password on that Apple ID (see appleid.apple.com)
#
# Copyright 2007, 2011, 2016, 2019 Bob Jacobsen, david d zuhn
#

set -e   # bail on errors
set -x   # show our work

REL_VER=$1
OUTPUT=$2
INPUTIMAGEFILE=$3
CERTIFICATE=$4
AC_USER=$5
AC_PASSWORD=$6


# -----------------------------------------
# Retry a command up to a specific number of times until it exits successfully.
# Waits 20 seconds times exponential back off.
#
#  $ retry 5 echo Hello
#  Hello
#
#  
function retry {
  local retries=$1
  shift

  local count=0
  until "$@"; do
    exit=$?
    wait=$(( (2 ** $count) * 20))
    count=$(($count + 1))
    if [ $count -lt $retries ]; then
      echo "Retry $count/$retries exited $exit, retrying in $wait seconds..."
      sleep $wait
    else
      echo "Retry $count/$retries exited $exit, no more retries left."
      return $exit
    fi
  done
  return 0
}
# -----------------------------------------


if [ "$REL_VER" = "" -o "$OUTPUT" = "" -o "$INPUTIMAGEFILE" = "" ]
then
  echo "usage: $0 VERSION OUTPUTIMAGEFILE INPUTIMAGEFILE" 1>&2
  exit 1
fi

if [ -x /usr/bin/hdiutil ]
then
  # if a Linux box were to have hdiutil, I think that would be the preferable route to follow
  # although it's pretty unlikely
  SYSTEM=MACOSX
else
  SYSTEM=LINUX
fi

if ! tmpoutdir=`mktemp -d -t JMRI.output`
then
  echo "Cannot create output temporary directory"
  exit 1
fi
if ! tmpindir=`mktemp -d -t JMRI.input`
then
  echo "Cannot create input temporary directory"
  exit 1
fi
if ! tmpimage1=`mktemp -t JMRI.tmp.image.1`
then
  echo "Cannot create temp image 1"
  exit 1
fi
if ! tmpimage2=`mktemp -t JMRI.tmp.image.2`
then
  echo "Cannot create temp image 1"
  exit 1
fi


trap 'rm -rf "$tmpimage1" "$tmpimage2" "$tmpoutdir" "$tmpindir" >/dev/null 2>&1'  0
trap 'exit 2' 1 2 3 15

# shouldn't be anything left over, but if so, clean up

rm -f "$tmpimage1" "$tmpimage2"

# mount input image (needs Linux varient)
hdiutil attach "$INPUTIMAGEFILE" -mountpoint "$tmpindir" -nobrowse

# create disk image and mount
jmrisize=`du -ms "$tmpindir" | awk '{print $1}'`
imagesize=`expr $jmrisize + 40`

if [ "$SYSTEM" = "MACOSX" ]
then 
    hdiutil create -size ${imagesize}MB -fs HFS+ -layout SPUD -volname "JMRI ${REL_VER}" "$tmpimage2"
    hdiutil attach ${tmpimage2}.dmg -mountpoint "$tmpoutdir" -nobrowse
    trap '[ "$EJECTED" = 0 ] && hdiutil eject "$tmpimage2"' 0
else
    dd if=/dev/zero of="$tmpimage2" bs=1M count=${imagesize}
    mkfs.hfsplus -v "JMRI ${REL_VER}" "${tmpimage2}"
    sudo mount -t hfsplus -o loop,rw,uid=$UID "$tmpimage2" $tmpoutdir
    trap '[ "$EJECTED" = 0 ] && sudo umount "$tmpoutdir"' 0
fi

# wait for the mountpoint to settle down...
#  I don't think we need this on macOS
#sleep 10

if [ -w "$tmpoutdir" ]
then
  SUDO=
else
  SUDO=sudo
fi

# copy contents of the Mac OS X distribution to the newly mounted filesysten
tar -C "$tmpindir" -cf - JMRI | $SUDO tar -C "$tmpoutdir" -xf -

# sign the app files in output
codesign -v -s "$CERTIFICATE" --deep $tmpoutdir/JMRI/PanelPro.app
codesign -v -s "$CERTIFICATE" --deep $tmpoutdir/JMRI/DecoderPro.app
codesign -v -s "$CERTIFICATE" --deep $tmpoutdir/JMRI/SoundPro.app

# add an Applications icon
$SUDO ln -s /Applications "$tmpoutdir"

# now, how do we make a nice background picture in the folder with directions on how to drag'n'drop to Applications?

# eject the mounted disk image
if [ "$SYSTEM" = "MACOSX" ]
then
  hdiutil detach "$tmpoutdir" && EJECTED=1
else
  sudo umount "$tmpoutdir" && EJECTED=1
fi


# pack into a smaller disk image for distribution
if [ "$SYSTEM" = "MACOSX" ]
then
  rm -f "$OUTPUT"
  hdiutil convert ${tmpimage2}.dmg -format UDZO -imagekey zlib-level=9 -o "$OUTPUT"
else
  # this relies on the 'dmg' tool from https://github.com/erwint/libdmg-hfsplus
  dmg dmg "$tmpimage2" "$OUTPUT"
fi

# sign image file
codesign -v -s "$CERTIFICATE" --deep "$OUTPUT"

# notarize distribution: start by uploading
xcrun altool --notarize-app --verbose --primary-bundle-id "org.jmri" --username "$AC_USER" --password "$AC_PASSWORD" --file "$OUTPUT"
# stapling result will temporarily fail while the notatization is still happening at Apple
retry 8 xcrun stapler staple "$OUTPUT"

# clean up
hdiutil detach $tmpindir 
# this should also be handled by the trap, but it doesn't hurt...
rm -rf $tmpimage1 $tmpimage2 $tmpoutdir 
