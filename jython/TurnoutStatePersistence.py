# Sample script showing how to persist the state of turnouts between sessions.
#
# State is saved to a .csv file at shutdown and re-instated from said .csv file
# when script is launched.
#
# This shows how .csv files can be both written and read-back complete with
# a header row using the Apache Commons CSV library
#
# This also shows how entries can be added to the log as opposed to using
# 'print' commands
#
# Author: Matthew Harris, copyright 2011
# Author: Randall Wood, copyright 2020
# Part of the JMRI distribution
#

import jmri

import java
import java.io
import java.util
import org.apache.commons.csv
from org.apache.log4j import Logger

# Define turnout state file
# Default is 'TurnoutState.csv' stored in the preferences directory
turnoutFile = jmri.util.FileUtil.getUserFilesPath() + "TurnoutState.csv"

# Define task to persist turnout state at shutdown
class PersistTurnoutStateTask(jmri.implementation.AbstractShutDownTask):

    # Get reference to the logger
    #
    # This reference is unique to instances of this class, hence the use of
    # 'self.log' whenever it needs to be used
    #
    # The logger has been instantiated within the pseudo package:
    #   'jmri.jmrit.jython.exec'
    # This allows for easy identification and configuration of log4j.
    #
    # To show debug messages, add the following line (without quotes) to
    # the file 'default.lcf' located in the JMRI program directory:
    #   'log4j.category.jmri.jmrit.jython.exec=DEBUG'
    log = Logger.getLogger("jmri.jmrit.jython.exec.TurnoutStatePersistence.PersistTurnoutStateTask")

    # Define task to run at ShutDown
    def run(self):

        # Write an info entry to the log
        self.log.info("Write turnout state to file: '%s'" % turnoutFile)

        # Open file
        
        csvFormat = org.apache.commons.csv.CSVFormat.Builder.create(org.apache.commons.csv.CSVFormat.DEFAULT).setCommentMarker('#').build()
        csvFile = org.apache.commons.csv.CSVPrinter(java.io.FileWriter(turnoutFile), csvFormat)

        # Initialise counter
        turnoutCount = 0

        # Write header
        csvFile.print("System Name")
        csvFile.print("User Name")
        csvFile.print("Comment")
        csvFile.print("Is Inverted")
        csvFile.print("Saved State")
        csvFile.println()

        # Loop through all known turnouts
        for to in turnouts.getNamedBeanSet():

            # Write a debug entry to the log
            if (self.log.isDebugEnabled()):
                self.log.debug("Storing turnout: {}", to.getSystemName())

            # Retrieve details to persist
            csvFile.print(to.getSystemName())
            csvFile.print(to.getUserName())
            csvFile.print(to.getComment())
            csvFile.print(self.booleanName(to.getInverted()))
            csvFile.print(self.stateName(to.getState()))

            # Notify end of record
            csvFile.println()

            # Increment counter
            turnoutCount +=1

        # Write an info entry to the log
        self.log.info("Stored state of %d turnouts" % turnoutCount)

        # Append a comment to the end of the file
        csvFile.printComment("Written by JMRI version %s on %s" % (jmri.Version.name(), (java.util.Date()).toString()))

        # Flush the write buffer and close the file
        csvFile.flush()
        csvFile.close()

        # All done
        return

    # Function to convert state values to names
    def stateName(self, state):
        if (state == CLOSED):
            return "CLOSED"
        if (state == THROWN):
            return "THROWN"
        if (state == INCONSISTENT):
            return "INCONSISTENT"
        # Anything else is UNKNOWN
        return "UNKNOWN"

    # Function to convert boolean values to names
    def booleanName(self, value):
        if (value == True):
            return "Yes"
        # Anything else is No
        return "No"

# Define task to load turnout state at script start
#
# This is implemented as a seperate class so that it can run on a
# different thread in the background rather than holding up the main
# thread while executing
class LoadTurnoutState(jmri.jmrit.automat.AbstractAutomaton):

    # Get reference to the logger
    log = Logger.getLogger("jmri.jmrit.jython.exec.TurnoutStatePersistence.LoadTurnoutState")

    # Perform any initialisation
    def init(self):
        return

    # Define task to run
    def handle(self):

        # Retrieve the state file as a File object
        inFile = java.io.File(turnoutFile)

        # Check if state file exists
        if inFile.exists():

            # It does, so load it
            csvFormat = org.apache.commons.csv.CSVFormat.Builder.create(org.apache.commons.csv.CSVFormat.DEFAULT).setHeader().setCommentMarker('#').build()
            csvFile = org.apache.commons.csv.CSVParser.parse(inFile, java.nio.charset.StandardCharsets.UTF_8, csvFormat)

            # Write an info entry to the log
            self.log.info("Loading turnout state file: %s" % turnoutFile)

            # Initialise counter
            turnoutCount = 0

            # Loop through each record
            for record in csvFile.getRecords():

                # Read the record details
                systemName = record.get("System Name")
                userName = record.get("User Name")
                comment = record.get("Comment")
                inverted = self.booleanName(record.get("Is Inverted"))
                savedState = self.stateValue(record.get("Saved State"))
                
                # Get reference to the turnout
                turnout = turnouts.provideTurnout(systemName)

                # Write a debug entry to the log
                if (self.log.isDebugEnabled()):
                    self.log.debug("Setting state of turnout: %s" % systemName)

                # Set other parameters if specified
                if (userName != ""):
                    turnout.setUserName(userName)
                if (comment != ""):
                    turnout.setComment(comment)
                if (turnout.canInvert()):
                    turnout.setInverted(inverted)

                # Finally, set the state
                turnout.setState(savedState)

                # Increment counter
                turnoutCount +=1

            # Close the file
            csvFile.close()

            # Write an info entry to the log
            self.log.info("Loaded state of %d turnouts" % turnoutCount)

        else:
            # It doesn't, so log this fact and carry on
            self.log.warn("Turnout state file '%s' does not exist" % turnoutFile)

        # All done
        return False    # Only need to run once

    # Function to convert state names to values
    def stateValue(self, state):
        if (state == "CLOSED"):
            return CLOSED
        if (state == "THROWN"):
            return THROWN
        if (state == "INCONSISTENT"):
            return INCONSISTENT
        # Anything else is UNKNOWN
        return UNKNOWN

    # Function to convert boolean names to values
    def booleanName(self, value):
        if (value == "Yes"):
            return True
        # Anything else is False
        return False

# Register the turnout persistence shutdown task
shutdown.register(PersistTurnoutStateTask("PersistTurnoutState"))

# Launch the load task
LoadTurnoutState().start()
