# Python code to define common JMRI defaults
#
# Assumes JMRI has already been initialized, so this
# can reference various managers, etc.
#
# $Id: jmri_defaults.py,v 1.1 2003-10-24 04:21:29 jacobsen Exp $

#define shortcuts to some managers
import jmri
turnouts = jmri.InstanceManager.turnoutManagerInstance()
sensors  = jmri.InstanceManager.sensorManagerInstance()
signals  = jmri.InstanceManager.signalHeadManagerInstance()
dcc      = jmri.InstanceManager.commandStationInstance()

# shortcut some constants
import jmri.Turnout.CLOSED         as CLOSED
import jmri.Turnout.THROWN         as THROWN

import jmri.Sensor.ACTIVE          as ACTIVE
import jmri.Sensor.INACTIVE        as INACTIVE

import jmri.NamedBean.UNKNOWN      as UNKNOWN
import jmri.NamedBean.INCONSISTENT as INCONSISTENT

import jmri.SignalHead.RED         as RED
import jmri.SignalHead.YELLOW      as YELLOW
import jmri.SignalHead.GREEN       as GREEN


# define a convenient class for listening to changes
import java
class PropertyListener(java.beans.PropertyChangeListener):
    def propertyChange(self, event):
        print "Object", event.source, "changed",event.propertyName, "from", event.oldValue, "to", event.newValue
