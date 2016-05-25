#! /bin/sh
#
# Script to check a single file for DOS/WINDOWS line ends
# 
# True/OK if _does_ have DOS/WINDOWS line ends
#
# typical use:
#      find xml -type f -exec ./scripts/checkCR.sh {} \; -print
#

cat -v "$*" | tail -1 | grep -q "\^M"



