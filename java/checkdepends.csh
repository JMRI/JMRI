#
#
# script to check for dependencies; output is bad
#
# other approaches: use javap; grep in the class files?

# OK checks

# apps should not be referenced in the main jmri directory
grep 'apps\.' src/jmri/*

# next should really check for whitespace not just one blank,
#grep 'import\[:punct:\]*jmri' src/jmri/*

# no Swing in base interfaces
#grep 'javax.swing\.' src/jmri/*

#apps should not be referenced outside it's own tree
grep -r 'apps\.' src/jmri

# and needs to look for non-imports
#grep jmri. src/jmri/* | grep -v package | grep -v @

#grep -r 'jmri\.jmrit\.' src/jmri/jmrix

#grep -r 'jmri\.j' src/jmri/util/

# checks to add:  jdom, swing, awt in places they shouldn't be

# This one will need lots of exceptions:
#grep -r 'org\.jdom' src/ | grep -v configurexml