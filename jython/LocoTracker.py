# This script uses JMRI reporters to identify locos (fitted with transponding decoders) that occupy blocks on a layout. The 
# decoder addresses or user names of locos present are formatted in a memory variable that can be displayed on a control panel.
# Based on a modified version of Bob Jacobsen's 'ReporterFormatter.py' script.
#
# Author: Ian Price
# Version: 1.21  17-Jun-22
# Part of the JMRI distribution
#
# Change history:
# 1.10  Added support for ID Tags with multiple-character connection prefixes.
#       Removed checks for LocoNet enter and exits messages.
# 1.20  Used getTagID() method to derive loco decoder addresses.
#       Added methods to select whether loco decoder addresses or user names are returned.
# 1.21  Changed import statement for java datetime module to resolve naming issue that arose with recent Java/JMRI releases.
#       Corrected typo in name of variable used for target reporter.
#       Removed trailing space from loco list stored in memory variable.
#       Extended notes explaining how to start a property change listener for each reporter.
#
# The script comprises a class used to add a property change listener to one or more reporters on a layout. When a loco 
# moves into or out of a block associated with a reporter, the listener is triggered and the collection of ID Tags 
# maintained by the reporter object is used to populate a memory variable with a list of the locos' identities. This list 
# can be displayed on a JMRI layout control panel.
#
# Notes:
# The script uses the ID Tag informaton format introduced in JMRI 4.15.3 and cannot be used with earlier releases.
# DCC controllers may need to be power-cycled after loading the script to ensure locos already in blocks are detected.
# Print statements are included in the script to help diagnose its operation. Enable these by uncommenting them.

import jmri
import java
import java.beans
import datetime

class LocoTracker(java.beans.PropertyChangeListener):
    def propertyChange(self, event):
        if (event.propertyName == "currentReport"):
            self.report = event.newValue    # Get the reporter event object
            if (isinstance(self.report, jmri.Reportable)):            
                #print datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3] + " " + self.reporterName + " triggered: " + self.report.toReportString()      
                
                # Compile a list of loco decoder addresses or user names from the reporter's ID Tag collection
                reporterManager = jmri.InstanceManager.getDefault(jmri.ReporterManager)
                reporter = reporterManager.getReporter(self.reporterName)
                locoAddresses = ""
                for idTag in reporter.getCollection().toArray():
                    locoAddress = None
                    if (self.returnUserNames):
                        locoAddress = idTag.getUserName()
                    if (locoAddress is None):
                        locoAddress = jmri.IdTag.getTagID(idTag)
                    locoAddresses += locoAddress + " "
                    
                # Update the reporter's associated memory variable with the list of locos
                self.memory.setValue(locoAddresses.rstrip())
                #if (locoAddresses != ""): print datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3] + " Loco(s) " + locoAddresses + "in " + self.reporterName
        return 

# start() : Starts the listener and initialises the reporter's associated memory variable to blank. 
# Method paramaters:
#   reporterName:    System name of the reporter
#   memoryName:      System name of the memory variable associated with the reporter
    def start(self, reporterName, memoryName):
        self.memory = memories.provideMemory(memoryName)
        self.memory.setValue("")
        reporters.provideReporter(reporterName).addPropertyChangeListener(self)        
        self.reporterName = reporterName
        self.returnUserNames = False      # Loco decoder addresses will be returned by default
        return self

# useDecoderAddresses() : Decoder addresses of identified locos will be returned in the memory variable 
# If this or the useUserNames() method is not called, loco decoder addresses will be returned by default
    def useDecoderAddresses(self):
        self.returnUserNames = False
        return

# useUserNames() : User names of identified locos will be returned in the memory variable 
# If a loco is not assigned a user name, its decoder address will be returned instead
    def useUserNames(self):
        self.returnUserNames = True
        return

# stop() : Stops the listener 
    def stop(self):
        reporters.getReporter(self.reporterName).removePropertyChangeListener(self)
        return
# End of class definition
#
#
# Sample code for using the class
# A property change listener needs to be setup for each reporter and its associated memory variable.
# In the example statements below, memory variables IM1, IM2, etc are associated with reporters LR1, LR2, etc. 
# Listeners can be setup in one of two ways. Use this syntax to identify locos using their decoder addresses:
#m1 = LocoTracker().start("LR1", "IM1").useDecoderAddresses()
#m2 = LocoTracker().start("LR2", "IM2").useDecoderAddresses()
#m3 = LocoTracker().start("LR3", "IM3").useDecoderAddresses()
#etc
#
# If you've assigned user names to locos in your JMRI ID Tags table, use this syntax to identify locos by their names:
#m4 = LocoTracker().start("LR4", "IM4").useUserNames()
#m5 = LocoTracker().start("LR5", "IM5").useUserNames()
#m6 = LocoTracker().start("LR6", "IM6").useUserNames()
#etc
#
# Note that you must only use one of the above methods for starting each reporter listener.
