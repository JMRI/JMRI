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
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.9 $

#define shortcuts to some managers
import jmri
turnouts  = jmri.InstanceManager.turnoutManagerInstance()
sensors   = jmri.InstanceManager.sensorManagerInstance()
signals   = jmri.InstanceManager.signalHeadManagerInstance()
dcc       = jmri.InstanceManager.commandStationInstance()
reporters = jmri.InstanceManager.reporterManagerInstance()
memories  = jmri.InstanceManager.memoryManagerInstance()

# shortcut some constants
import jmri.Turnout.CLOSED         as CLOSED
import jmri.Turnout.THROWN         as THROWN

import jmri.Sensor.ACTIVE          as ACTIVE
import jmri.Sensor.INACTIVE        as INACTIVE

import jmri.NamedBean.UNKNOWN      as UNKNOWN
import jmri.NamedBean.INCONSISTENT as INCONSISTENT

import jmri.SignalHead.DARK        as DARK
import jmri.SignalHead.RED         as RED
import jmri.SignalHead.YELLOW      as YELLOW
import jmri.SignalHead.GREEN       as GREEN
import jmri.SignalHead.FLASHRED    as FLASHRED
import jmri.SignalHead.FLASHYELLOW as FLASHYELLOW
import jmri.SignalHead.FLASHGREEN  as FLASHGREEN

True = 1
False = 0

# define a convenient class for listening to changes
import java
class PropertyListener(java.beans.PropertyChangeListener):
    def propertyChange(self, event):
        print "Object", event.source, "changed",event.propertyName, "from", event.oldValue, "to", event.newValue
