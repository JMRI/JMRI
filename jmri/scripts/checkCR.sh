#! /bin/sh
#
# Script to check a single file for DOS/WINDOWS line ends
# 
# True/OK if _does_ have DOS/WINDOWS line ends
#

cat -v "$*" | tail -1 | grep -q "\^M"



