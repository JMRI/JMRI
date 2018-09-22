# Python code to define common JMRI defaults
#
# Assumes JMRI has already been initialized, so this
# can reference various managers, etc.
#
# This is only read once, when the JMRI library first executes
# a script, so changes will not take effect until after restarting
# the program
#
# Author: Bob Jacobsen, copyright 2003, 2004
# Part of the JMRI distribution

import jmri
import java.beans

# define a helper function
def decodeJmriFilename(name) :
    return jmri.util.FileUtil.getExternalFilename(name)
    
# define a convenient class for listening to changes
import java
class PropertyListener(java.beans.PropertyChangeListener):
    def propertyChange(self, event):
        print "Object", event.source, "changed",event.propertyName, "from", event.oldValue, "to", event.newValue
