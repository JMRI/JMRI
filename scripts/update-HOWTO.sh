#!/bin/bash
#
# Does an edit of the scripts/HOWTO-distribution.md page to 
#   update the version numbers within it.  Use this to 
#   make it easier to cut&paste commands.
#
#
# Arguments are last version, this version, next version e.g. when about to do 4.7.3
#   ./scripts/update-HOWTO 4.7.2 4.7.3 4.7.4
#
# Note that doing this twice isn't good, so perhaps we should 
# come up with something a bit less brute force.

sed -i .bak s/$2/$3/g scripts/HOWTO-distribution.md

sed -i .bak s/$1/$2/g scripts/HOWTO-distribution.md

