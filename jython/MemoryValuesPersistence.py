# Persist the values of memory variables between sessions.
#
# Values are saved to a .csv file at shutdown and re-instated from said .csv file
# when script is launched.
#
# This script is based on TurnoutStatePersistence.py.
# Author: Dave Sand, copyright 2025
# Part of the JMRI distribution
#

import jmri
import java

import org.apache.commons.csv
from org.slf4j import LoggerFactory

# Define memory values file
# Default is 'MemoryValues.csv' stored in the user files location.
memoryFile = jmri.util.FileUtil.getUserFilesPath() + "MemoryValues.csv"

# -------------------------- Notes ----------------------
# Memory variable values can be any type of object.  String objects are stored as is.  Null values are
# stored as the word "None" and the word is used during loading.  Other objects store the object
# reference which has no usable value when loaded.

# Copy this script to the profile's user files location and modify the "memoryNames" list.
# Append the system or user name of each memory variable value to be retained.
memoryNames = []
memoryNames.append('IMRATEFACTOR')
# memoryNames.append('nullTest')
# memoryNames.append('objTest')

# The script needs to be run after loading the layout data xml file so that the stored values replace
# any values in the layout data xml file.

# Define task to persist memory values at shutdown
class PersistMemoryValuesTask(jmri.implementation.AbstractShutDownTask):

    # Get reference to the logger
    #
    # This reference is unique to instances of this class, hence the use of
    # 'self.log' whenever it needs to be used
    #
    # The logger has been instantiated within the pseudo package:
    #   'jmri.jmrit.jython.exec'
    # This allows for easy identification and configuration of logging.
    #
    # NOTE: to enable logging, see https://www.jmri.org/help/en/html/apps/Debug.shtml
    # Add the Logger Category name "jmri.jmrit.jython.exec" at DEBUG Level.

    log = LoggerFactory.getLogger("jmri.jmrit.jython.exec.MemoryValuePersistence.PersistMemoryValuesTask")

    # Define task to run at ShutDown
    def run(self):

        # Write an info entry to the log
        self.log.info("Write memory values to file: '%s'" % memoryFile)

        # Open file

        csvFormat = org.apache.commons.csv.CSVFormat.Builder.create(org.apache.commons.csv.CSVFormat.DEFAULT).setCommentMarker('#').build()
        csvFile = org.apache.commons.csv.CSVPrinter(java.io.FileWriter(memoryFile), csvFormat)

        # Initialise counter
        memoryCount = 0

        # Write header
        csvFile.print("System Name")
        csvFile.print("User Name")
        csvFile.print("Comment")
        csvFile.print("Saved Value")
        csvFile.println()

        # Loop through all listed memories
        for memoryName in memoryNames:
            mem = memories.getMemory(memoryName)
            if mem is None:
                self.log.warn("Memory variable, {}, not found", memoryName)
                continue

            # Write a debug entry to the log
            if (self.log.isDebugEnabled()):
                self.log.debug("Storing memory value for {}", mem.getSystemName())

            # Retrieve details to persist
            csvFile.print(mem.getSystemName())
            csvFile.print(mem.getUserName())
            csvFile.print(mem.getComment())
            csvFile.print(str(mem.getValue()))

            # Notify end of record
            csvFile.println()

            # Increment counter
            memoryCount += 1

        # Write an info entry to the log
        self.log.info("Stored values of %d memory variables" % memoryCount)

        # Append a comment to the end of the file
        csvFile.printComment("Written by JMRI version %s on %s" % (jmri.Version.name(), (java.util.Date()).toString()))

        # Flush the write buffer and close the file
        csvFile.flush()
        csvFile.close()

        # All done
        return

# Define task to load memory values at script start
#
# This is implemented as a separate class so that it can run on a
# different thread in the background rather than holding up the main
# thread while executing
class LoadMemoryValues(jmri.jmrit.automat.AbstractAutomaton):

    # Get reference to the logger
    log = LoggerFactory.getLogger("jmri.jmrit.jython.exec.MemoryValuePersistence.LoadMemoryValues")

    # Perform any initialisation
    def init(self):
        return

    # Define task to run
    def handle(self):

        # Retrieve the values file as a File object
        inFile = java.io.File(memoryFile)

        # Check if state file exists
        if inFile.exists():

            # It does, so load it
            csvFormat = org.apache.commons.csv.CSVFormat.Builder.create(org.apache.commons.csv.CSVFormat.DEFAULT).setHeader().setCommentMarker('#').build()
            csvFile = org.apache.commons.csv.CSVParser.parse(inFile, java.nio.charset.StandardCharsets.UTF_8, csvFormat)

            # Write an info entry to the log
            self.log.info("Loading memory values file: %s" % memoryFile)

            # Initialise counter
            memoryCount = 0

            # Loop through each record
            for record in csvFile.getRecords():

                # Read the record details
                systemName = record.get("System Name")
                userName = record.get("User Name")
                comment = record.get("Comment")
                savedValue = record.get("Saved Value")

                # Get reference to the memory variable
                memory = memories.provideMemory(systemName)

                # Write a debug entry to the log
                if (self.log.isDebugEnabled()):
                    self.log.debug("Setting value of memory: %s" % systemName)

                # Set other parameters if specified
                if (userName != ""):
                    memory.setUserName(userName)
                if (comment != ""):
                    memory.setComment(comment)

                # Finally, set the value
                memory.setValue(savedValue)

                # Increment counter
                memoryCount += 1

            # Close the file
            csvFile.close()

            # Write an info entry to the log
            self.log.info("Loaded value of %d memory variables" % memoryCount)

        else:
            # It doesn't, so log this fact and carry on
            self.log.warn("Memory values file '%s' does not exist" % memoryFile)

        # All done
        return False    # Only need to run once

# Register the memory value persistence shutdown task
shutdown.register(PersistMemoryValuesTask("PersistMemoryValues"))

# Launch the load task
LoadMemoryValues().start()
