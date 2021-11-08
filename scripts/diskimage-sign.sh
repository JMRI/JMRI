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
#    Location of keychain file containing that certificate
#    Password to unlock that keychain file
#
# Although it might be consuming additional space on the final disk image,
# we do the signing and jar-updating there to ensure this doesn't
# cause issues for other uses of the raw as-built files
#
# Copyright 2007, 2011, 2016, 2019 Bob Jacobsen, david d zuhn
#

set -e   # bail on errors
set -x   # show our work

whoami

REL_VER=$1
OUTPUT=$2
INPUTIMAGEFILE=$3
CERTIFICATE=$4
AC_USER=$5
AC_PASSWORD=$6
KEYCHAIN_FILE=$7

JAR=/usr/bin/jar

# -----------------------------------------
function trapExitHandler {
    trap - 1 2 3 15
    umount "$tmpimage2" && echo "Unmounted tmpimage2"
    umount "$tmpindir" && echo "Unmounted tempindir"
    rm -rf "$INPUTIMAGEFILE" && echo "Deleted input image file"
    umount "$tmpoutdir" && echo "Unmounted tmpoutdir image"    
    rm -rf "$tmpimage1" "$tmpimage2" "$tmpoutdir" "$tmpindir" >/dev/null 2>&1
}
# -----------------------------------------
# Retry a command up to a specific number of times until it exits successfully.
# Waits 1 minute between retries
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
    wait=60
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
# Sign a file (2nd arg) with a jar file (1st arg) in place
#  
function signJarMember {
  local jar=$1
  local file=$2

  if [ -f "$jar" ]
  then
    export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
    $JAR xf $jar $file
    signFile $file
    $JAR uvf $jar $file
    rm $file
  fi
  
  return 0
}
# -----------------------------------------
# Sign a single file (1st arg)
#  
function signFile {
  local file=$1
  echo sign $file
  
  if [ -e "$file" ]
  then
    xattr -lr $file
    xattr -cr $file
    sudo -u jake codesign -v -s "$CERTIFICATE" --force --keychain "$KEYCHAIN_FILE" --deep $file
  fi
  return 0
}
# -----------------------------------------


if [ "$REL_VER" = "" -o "$OUTPUT" = "" -o "$INPUTIMAGEFILE" = "" ]
then
  echo "usage: $0 VERSION OUTPUTIMAGEFILE INPUTIMAGEFILE" 1>&2
  exit 1
fi

# switch to a directory with known-good permissions
if ! workdir=`mktemp -d -t workdir`
then
  echo "Cannot create temporary working directory"
  exit 1
fi
cd $workdir
echo 'workdir' $workdir

if [ -x /usr/bin/hdiutil ]
then
  # if a Linux box were to have hdiutil, I think that would be the preferable route to follow
  # although it's pretty unlikely
  SYSTEM=MACOSX
  
  export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
  java -version
else
  SYSTEM=LINUX
fi

if ! tmpoutdir=`mktemp -d -t JMRI.output`
then
  echo "Cannot create output temporary directory"
  exit 1
fi
echo "tmpoutdir" $tmpoutdir

if ! tmpindir=`mktemp -d -t JMRI.input`
then
  echo "Cannot create input temporary directory"
  exit 1
fi
echo 'tmpindir' $tmpindir
 
if ! tmpimage1=`mktemp -t JMRI.tmp.image.1`
then
  echo "Cannot create temp image 1"
  exit
fi
echo 'tempimage1' $tmpimage1

if ! tmpimage2=`mktemp -t JMRI.tmp.image.2`
then
  echo "Cannot create temp image 1"
  exit 1
fi
echo 'tmpimage2' $tmpimage2

# handle cleanup on exit
trap trapExitHandler  0
# handle error signals by aborting
trap 'exit 2' 1 2 3 15

# shouldn't be anything left over, but if so, clean up

rm -f "$tmpimage1" "$tmpimage2"

# mount input image
sync
hdiutil attach "$INPUTIMAGEFILE" -mountpoint "$tmpindir" -nobrowse
echo "INPUTIMAGEFILE" $INPUTIMAGEFILE

# create disk image and mount
jmrisize=`du -ms "$tmpindir" | awk '{print $1}'`
imagesize=`expr $jmrisize + 80`

if [ "$SYSTEM" = "MACOSX" ]
then 
    hdiutil create -size ${imagesize}MB -fs HFS+ -layout SPUD -volname "JMRI ${REL_VER}" "$tmpimage2"
    hdiutil attach ${tmpimage2}.dmg -mountpoint "$tmpoutdir" -nobrowse
else
    dd if=/dev/zero of="$tmpimage2" bs=1M count=${imagesize}
    mkfs.hfsplus -v "JMRI ${REL_VER}" "${tmpimage2}"
    sudo mount -t hfsplus -o loop,rw,uid=$UID "$tmpimage2" $tmpoutdir
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

# display debug info for the keychain containing the certification
#security list-keychains
#security -v default-keychain
#security -v login-keychain
#security -v show-keychain-info "$KEYCHAIN_FILE"
#security -v find-certificate -c "$CERTIFICATE"  "$KEYCHAIN_FILE"

# sign the app files in output
signFile $tmpoutdir/JMRI/PanelPro.app
signFile $tmpoutdir/JMRI/DecoderPro.app
signFile $tmpoutdir/JMRI/SoundPro.app

# sign the individual library files
signFile $tmpoutdir/JMRI/lib/macosx/libgluegen-rt.jnilib
signFile $tmpoutdir/JMRI/lib/macosx/libjinput-osx.jnilib
signFile $tmpoutdir/JMRI/lib/macosx/libjoal.jnilib
signFile $tmpoutdir/JMRI/lib/macosx/libopenal.1.15.1.dylib
signFile $tmpoutdir/JMRI/lib/macosx/libopenal.1.dylib
signFile $tmpoutdir/JMRI/lib/macosx/libopenal.dylib

# sign libraries inside jar files
signJarMember $tmpoutdir/JMRI/lib/libusb4java-1.3.0-darwin-x86-64.jar org/usb4java/darwin-x86-64/libusb4java.dylib
signJarMember $tmpoutdir/JMRI/lib/bluecove-2.1.1-SNAPSHOT.jar libbluecove.jnilib

signJarMember $tmpoutdir/JMRI/lib/jna-4.4.0.jar com/sun/jna/darwin/libjnidispatch.jnilib
signJarMember $tmpoutdir/JMRI/lib/jna-5.9.0.jar com/sun/jna/darwin-x86-64/libjnidispatch.jnilib
signJarMember $tmpoutdir/JMRI/lib/jna-5.9.0.jar com/sun/jna/darwin-aarch64/libjnidispatch.jnilib

signJarMember $tmpoutdir/JMRI/lib/hid4java-0.5.0.jar darwin/libhidapi.dylib
signJarMember $tmpoutdir/JMRI/lib/jython-standalone-2.7.2.jar META-INF/native/osx/libjansi.jnilib
signJarMember $tmpoutdir/JMRI/lib/jython-standalone-2.7.2.jar jni/Darwin/libjffi-1.2.jnilib

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
signFile "$OUTPUT"

# notarize distribution: start by uploading
xcrun altool --notarize-app --verbose --primary-bundle-id "org.jmri" --username "$AC_USER" --password "$AC_PASSWORD" --file "$OUTPUT"
# stapling result will temporarily fail while the notatization is still happening at Apple
sleep 30
retry 20 xcrun stapler staple "$OUTPUT"

# clean up
hdiutil detach $tmpindir 
# this should also be handled by the trap, but it doesn't hurt...
rm -rf $tmpimage1 $tmpimage2 $tmpoutdir 
