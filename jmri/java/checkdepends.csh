#
#
# script to check for dependencies; output is bad
#
# other approaches: use javap; grep in the class files?

# OK checks

echo '==== The apps package should not be referenced in the main jmri directory ===='
grep 'apps\.' src/jmri/*.java
echo

echo '==== The jmri package should not refer to lower jmri.* packages ===='
grep 'jmri\.[a-z]' src/jmri/*.java
echo

echo '==== No Swing in base interfaces ===='
grep 'javax.swing\.' src/jmri/*
echo

echo '==== The jmri.jmrix package should not reference jmri.jmrit ===='
grep -r 'jmri\.jmrit\.' src/jmri/jmrix
echo

echo '==== The apps package should not be referenced outside its own tree ===='
grep -r 'apps\.' src/jmri
echo

#
# below here is currently helpless, but we hope to get to it eventually
#

# and needs to look for non-imports
#grep jmri. src/jmri/* | grep -v package | grep -v @
echo

#grep -r 'jmri\.jmrit\.' src/jmri/jmrix
echo

#grep -r 'jmri\.j' src/jmri/util/
echo

# checks to add:  jdom, swing, awt in places they shouldn't be

# This one will need lots of exceptions:
#grep -r 'org\.jdom' src/ | grep -v configurexml
#echo
