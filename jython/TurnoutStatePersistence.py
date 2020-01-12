# Sample script showing how to persist the state of turnouts between sessions.
#
# State is saved to a .csv file at shutdown and re-instated from said .csv file
# when script is launched.
#
# This shows how .csv files can be both written and read-back complete with
# a header row using the javacsv library
#
# This also shows how entries can be added to the log as opposed to using
# 'print' commands
#
# Author: Matthew Harris, copyright 2011
# Part of the JMRI distribution
#

import jmri

import java
import java.io
import java.util
import com.csvreader
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
    def execute(self):

        # Write an info entry to the log
        self.log.info("Write turnout state to file: '%s'" % turnoutFile)

        # Open file
        csvFile = com.csvreader.CsvWriter(turnoutFile)

        # Initialise counter
        turnoutCount = 0

        # Write header
        csvFile.write("System Name")
        csvFile.write("User Name")
        csvFile.write("Comment")
        csvFile.write("Is Inverted")
        csvFile.write("Saved State")
        csvFile.endRecord()

        # Loop through all known turnouts
        for to in turnouts.getNamedBeanSet():

            # Write a debug entry to the log
            if (self.log.isDebugEnabled()):
                self.log.debug("Storing turnout: {}", to.getSystemName())

            # Retrieve details to persist
            csvFile.write(to.getSystemName())
            csvFile.write(to.getUserName())
            csvFile.write(to.getComment())
            csvFile.write(self.booleanName(to.getInverted()))
            csvFile.write(self.stateName(to.getState()))

            # Notify end of record
            csvFile.endRecord()

            # Increment counter
            turnoutCount +=1

        # Write an info entry to the log
        self.log.info("Stored state of %d turnouts" % turnoutCount)

        # Append a comment to the end of the file
        csvFile.writeComment("Written by JMRI version %s on %s" % (jmri.Version.name(), (java.util.Date()).toString()))

        # Flush the write buffer and close the file
        csvFile.flush()
        csvFile.close()

        # All done
        return True     # True to allow ShutDown; False to abort

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
            csvFile = com.csvreader.CsvReader(turnoutFile)

            # Configure csv reader
            csvFile.setUseComments(True)

            # Write an info entry to the log
            self.log.info("Loading turnout state file: %s" % turnoutFile)

            # Read the headers
            csvFile.readHeaders()

            # Initialise counter
            turnoutCount = 0

            # Loop through each record
            # The readRecord() method returns False when the end of the file
            # is reached
            while (csvFile.readRecord()):

                # Read the record details
                systemName = csvFile.get("System Name")
                userName = csvFile.get("User Name")
                comment = csvFile.get("Comment")
                inverted = self.booleanName(csvFile.get("Is Inverted"))
                savedState = self.stateValue(csvFile.get("Saved State"))
                
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
