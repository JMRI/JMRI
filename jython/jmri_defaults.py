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
# $Revision$

#define shortcuts to some managers
import jmri
turnouts     = jmri.InstanceManager.turnoutManagerInstance()
sensors      = jmri.InstanceManager.sensorManagerInstance()
signals      = jmri.InstanceManager.signalHeadManagerInstance()
masts        = jmri.InstanceManager.signalMastManagerInstance()
lights       = jmri.InstanceManager.lightManagerInstance()
dcc          = jmri.InstanceManager.commandStationInstance()
reporters    = jmri.InstanceManager.reporterManagerInstance()
memories     = jmri.InstanceManager.memoryManagerInstance()
routes       = jmri.InstanceManager.routeManagerInstance()
blocks       = jmri.InstanceManager.blockManagerInstance()
powermanager = jmri.InstanceManager.powerManagerInstance()
programmers  = jmri.InstanceManager.programmerManagerInstance()
shutdown     = jmri.InstanceManager.shutDownManagerInstance()
audio        = jmri.InstanceManager.audioManagerInstance()
layoutblocks = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
warrants     = jmri.InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager)

# shortcut some constants
import jmri.Turnout.CLOSED         as CLOSED
import jmri.Turnout.THROWN         as THROWN
import jmri.Turnout.CABLOCKOUT         as CABLOCKOUT
import jmri.Turnout.PUSHBUTTONLOCKOUT  as PUSHBUTTONLOCKOUT
import jmri.Turnout.UNLOCKED       as UNLOCKED
import jmri.Turnout.LOCKED         as LOCKED

import jmri.Sensor.ACTIVE          as ACTIVE
import jmri.Sensor.INACTIVE        as INACTIVE

import jmri.NamedBean.UNKNOWN      as UNKNOWN
import jmri.NamedBean.INCONSISTENT as INCONSISTENT

import jmri.SignalHead.DARK        as DARK
import jmri.SignalHead.RED         as RED
import jmri.SignalHead.YELLOW      as YELLOW
import jmri.SignalHead.GREEN       as GREEN
import jmri.SignalHead.LUNAR       as LUNAR
import jmri.SignalHead.FLASHRED    as FLASHRED
import jmri.SignalHead.FLASHYELLOW as FLASHYELLOW
import jmri.SignalHead.FLASHGREEN  as FLASHGREEN
import jmri.SignalHead.FLASHLUNAR  as FLASHLUNAR

True = 1
False = 0

# define a convenient class for listening to changes
import java
class PropertyListener(java.beans.PropertyChangeListener):
    def propertyChange(self, event):
        print "Object", event.source, "changed",event.propertyName, "from", event.oldValue, "to", event.newValue
