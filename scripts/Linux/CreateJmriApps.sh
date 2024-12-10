#!/bin/bash

# Create the DecoderPro, PanelPro and LccPro Linux application definitions.
# Command format:  CreateJmriApps.sh [--desktop | -d]
# If the --desktop or the -d option is included, the desktop icons will also be created.

DESKTOP=0
while [ $# -gt 0 ]; do
    if [ "${1}" = "--desktop" -o "${1}" = "-d" ]; then
        DESKTOP=1
    fi
    shift 1
done

PROGDIR=$(cd "$( dirname "${0}" )" && pwd)

# Verify that the script is running in the JMRI program location
if [ -f ${PROGDIR}/DecoderPro ] && [ -f ${PROGDIR}/PanelPro ] && [ -f ${PROGDIR}/LccPro ]; then
    echo "Create Linux application definitions for DecoderPro, PanelPro and LccPro"
else
    echo "This script must be run from the JMRI program location"
    exit 1
fi

function createDP() {
cat << DP-App > $1
[Desktop Entry]
Type=Application
Encoding=UTF-8
Name=DecoderPro
Comment=JMRI Decoder Pro
Icon=${PROGDIR}/resources/decoderpro.gif
Exec=${PROGDIR}/DecoderPro
Terminal=false;
DP-App
}

function createPP() {
cat << PP-App > $1
[Desktop Entry]
Type=Application
Encoding=UTF-8
Name=PanelPro
Comment=JMRI Panel Pro
Icon=${PROGDIR}/resources/PanelPro.gif
Exec=${PROGDIR}/PanelPro
Terminal=false;
PP-App
}

function createLP() {
cat << LP-App > $1
[Desktop Entry]
Type=Application
Encoding=UTF-8
Name=LccPro
Comment=JMRI LCC Pro
Icon=${PROGDIR}/resources/LccPro-128x128.png
Exec=${PROGDIR}/LccPro
Terminal=false;
LP-App
}

createDP ${HOME}/.local/share/applications/DecoderPro.desktop
createPP ${HOME}/.local/share/applications/PanelPro.desktop
createLP ${HOME}/.local/share/applications/LccPro.desktop

if [ $DESKTOP = 1 ] ; then
    echo "Create desktop icons"
    createDP ${HOME}/Desktop/DecoderPro.desktop
    createPP ${HOME}/Desktop/PanelPro.desktop
    createLP ${HOME}/Desktop/LccPro.desktop

    chmod 755 ${HOME}/Desktop/DecoderPro.desktop
    chmod 755 ${HOME}/Desktop/PanelPro.desktop
    chmod 755 ${HOME}/Desktop/LccPro.desktop

    if hash gio 2>/dev/null; then
        echo "Enable Allow Launching"
        gio set ${HOME}/Desktop/DecoderPro.desktop metadata::trusted true
        gio set ${HOME}/Desktop/PanelPro.desktop metadata::trusted true
        gio set ${HOME}/Desktop/LccPro.desktop metadata::trusted true
    fi
fi

echo "DecoderPro, PanelPro and LccPro applications have been created"

