# Script to show the decoder addresses of locos in occupied blocks on a control panel.
# Based on a modified version of Bob Jacobsen's 'ReporterFormatter.py' script.
#
# Author: Ian Price
# Part of the JMRI distribution
#
# The script comprises a class used to add a property change listener to one or more reporters on a layout.
# When a loco moves in or out of a block associated with a reporter, the listener is triggered. If the
# reporter's 'current report' value includes an enter or exit message, the collection of loco ID tags
# maintained by the reporter object is used to populate a memory variable with a space-separated list of
# the locos' decoder addresses. This list can be displayed on a control panel.
# The memory variable used by each reporter is specified as a parameter in the start method used to create each
# listener. See the examples at the end of this script.

# Notes:
# The script uses the IdTag informaton format introduced in JMRI 4.15.3 and cannot be used with earlier releases.
# If user names have been mapped to loco ID tags (in the ID Tags table), these will not appear on the control panel.
# The DCC controller may need to be power-cycled after the script is loaded to ensure locos already present in
# blocks are detected.
# A number of print statements are included in the script to help diagnose its operation. These can be enabled
# by uncommenting them.

import jmri
import java
import java.beans
from datetime import datetime

class LocoTracker(java.beans.PropertyChangeListener):
    def propertyChange(self, event):
        if (event.propertyName == "currentReport"):
            self.report = event.newValue    # Get the reporter event object
            if (isinstance(self.report, jmri.Reportable)):
                self.message = self.report.toReportString()    # Get the reporter message
                self.action = self.message[-5:]                # Extract the message action (eg. enter or exits)
                isUpdated = False
                if (self.action == "enter"):
                    isUpdated = True
                    #print datetime.now().strftime("%H:%M:%S.%f")[:-3] + " " + self.reporterName + " triggered: " + self.message
                elif (self.action == "exits"):
                    isUpdated = True
                    #print datetime.now().strftime("%H:%M:%S.%f")[:-3] + " " + self.reporterName + " triggered: " + self.message
                else:
                    #print "Unidentified reporter message " + self.message
                    pass

                if (isUpdated == True):    # Update the memory variable following an entry or exit message
                    reporterManager = jmri.InstanceManager.getDefault(jmri.ReporterManager)
                    repeater = reporterManager.getReporter(self.reporterName)
                    locoAddresses = ""
                    for tag in repeater.getCollection().toArray():
                        locoAddresses += tag.toString()[2:] + " "    # Remove 'LD' prefix from tag to derive decoder address
                    self.memory.setValue(locoAddresses)
                    #if (locoAddresses != ""): print "  Loco(s) " + locoAddresses + "in " + self.reporterName
        return

# start() : Starts the listener and initialise the reporter's associated memory variable to blank
    def start(self, reporterName, memoryName):
        self.memory = memories.provideMemory(memoryName)
        self.memory.setValue("")
        reporters.provideReporter(reporterName).addPropertyChangeListener(self)
        self.reporterName = reporterName
        return

# stop() : Stops the listener
    def stop(self):
        reporters.getReporter(self.reporterName).removePropertyChangeListener(self)
        return

# Start a listener for each reporter
# In this example, memory variables IM1, IM2, etc are associated with reporters LR1, LR2, etc.
m1 = LocoTracker().start("LR1", "IM1")
m2 = LocoTracker().start("LR2", "IM2")
# etc
