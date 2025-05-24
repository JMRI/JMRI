# Script to initialize:
#
#   ALL known physical turnouts from UNKNOWN to the CLOSED state
#       ignoring turnouts that have already been set to either THROWN or CLOSED
#       and
#   ALL known physical sensors from UNKNOWN to the INACTIVE state
#       ignoring sensors that have already been set to either ACTIVE or INACTIVE


#      (
#          The Internal Turnouts & Sensors are NOT DIRECTLY touched
#            BUT responses from actions that trigger 
#                a Route or a Logix or a LogixNG
#                conditional can cause indirect responses
#                to some internal sensors and/or turnouts
#       )
#
#   In this case "known" means turnouts and sensors that are physically on the layout
#   and also are described in one or more Panel files that have been loaded into the running
#   JMRI program, usually the PanelPro flavor.
#
#######    The layout must be turned on and the Panel loaded 
#######         before activating this script!!!!
#
#######    WE HAVE LEARNED THE HARD WAY, THAT PANEL FILES THAT ACTIVATE Turnouts
#######    AT LOAD TIME, RISK THE CONSEQUENCES OF MOVING A Turnout UNDER A TRAIN

# This script kicks off an independent series of actions that will take some time to 
# complete once the script itself completes.  If you are running this script as a 
# startup action, you might need to include a pause startup action before you run any
# additional scripts later.


# Original script, circa 2014, was loosely based on several example scripts supplied in the distribution folder
#   program:jython
# by various authors.  Update suggestions are from Dave Sand, Bob Jacobsen, and others.
# Most recent update conforms with PanelPro version 5.3.6

# Modified: Cliff Anderson, Dave Sand, 2025
# Author: Cliff Anderson, Copyright 2023
# Part of the JMRI distribution


import jmri
import java
from org.slf4j import LoggerFactory;

# Inherit and override methods from AbstractAutomaton parent class
class InitializeTurnoutsSensors(jmri.jmrit.automat.AbstractAutomaton):

    # Get reference to the Logger
    log = LoggerFactory.getLogger("jmri.jmrit.jython.exec.script.InitializeTurnoutsSensors");
    #    All messages written to self.log.info( "text" ) are
    #        visible in the JMRI System console window at runtime and 
    #        are appended to file: settings:log\messages.log
    #        are stored in file: settings:log\session.log
    #   # NOTE: to enable logging, see https://www.jmri.org/help/en/html/apps/Debug.shtml
    #        Add the Logger Category name "jmri.jmrit.jython.exec" at TRACE Level.

    # override AbstractAutomaton parent class initialization
    def init(self):
        # Probably redundant self protection
        powermanager.setPower(jmri.PowerManager.ON)
        # Pause to allow track power restoration, if it was off
        self.waitMsec ( 500 )   # Inherit from AbstractAutomaton

        self.closeTurnouts()
        self.deacivateSensors()
        return

########    END OF InitializeTurnoutsSensors.init()

    # override AbstractAutomaton parent class managed loop
    def handle(self):
        # Make a record for the log to indicate that we have completed the task
        self.log.info( "All done" )
        return False    # To run at most once, and die

########    END OF InitializeTurnoutsSensors.handle()

    # initialization of each physical Turnout in order of appearance on the system list
    # Close ALL known physical layout turnouts but ignore the JMRI internal ones
    def closeTurnouts(self) :

        internalCounter = 0
        physicalCounter = 0
        skippingCounter = 0
        self.log.info( "Loop through all known turnouts" )
        for turnout in turnouts.getNamedBeanSet() :

            turnoutSystemName = turnout.getSystemName()
            # Allow user to locally modify decisions based on userName
            # turnoutUserName = turnout.getUserName()

            # self.log.debug ( \
            #    'Turnout systemName = "{0}" userName = "{1}"'
            #    .format(turnoutSystemName, turnoutUserName) 
            #    )

            turnoutState = turnout.getState()
            if turnoutSystemName[0:1] == "I" :
                #do nothing for internal turnouts
                self.log.debug(
                    'SKIPPING internal Turnout: "{0}"'.format(turnoutSystemName)
                    )
                internalCounter += 1

            elif ( turnoutState != CLOSED and turnoutState != THROWN ) :
                self.log.debug(
                    'Closing physical Turnout: "{0}"'.format(turnoutSystemName)
                    )
                turnout.setState( CLOSED )
                self.waitMsec(125)  # stall for Command Station action
                physicalCounter += 1

            else:
                self.log.debug(
                    'Skipping physical {0} Turnout: "{1}"\
                    '.format(self.turnoutStateName(turnoutState), turnoutSystemName)
                    )
                skippingCounter += 1

        # Tell the log how many turnouts we found, for sanity checking
        self.log.info ( "Internal Turnout Count = {0}".format( internalCounter ) )
        self.log.info ( "Initialized Physical Turnout Count = {0}".format( physicalCounter ) )
        self.log.info ( "Skipped Physical Turnout Count = {0}".format( skippingCounter ) )
        return

########    END OF InitializeTurnoutsSensors.closeTurnouts()

    # Function to convert state values to names
    def turnoutStateName(self, state):
        if (state == CLOSED):
            return "CLOSED"
        if (state == THROWN):
            return "THROWN"
        if (state == INCONSISTENT):
            return "INCONSISTENT"
        # Anything else is UNKNOWN
        return "UNKNOWN"


    # initialization of each physical Sensor in order of appearance on the system list
    # Deactivate ALL known physical layout sensors but ignore the JMRI internal ones
    def deacivateSensors(self) :

        self.log.info( "Loop through all known sensors" )

        internalCounter = 0
        physicalCounter = 0
        skippingCounter = 0
        for sensor in sensors.getNamedBeanSet() :

            sensorSystemName = sensor.getSystemName()
            # Allow user to locally modify decisions based on userName
            # sensorUserName = sensor.getUserName()

            # self.log.debug (
            #    'Sensor systemName = "{0}" userName = "{1}"'
            #    .format(sensorSystemName,sensorUserName)
            #    )

            sensorState = sensor.getState()
            if sensorSystemName[0:1] == "I" :
                #do nothing for internal sensors
                if (sensorSystemName == "ISCLOCKRUNNING") :
                    pass    # Do not even count special internal Sensor
                else :
                    self.log.debug(
                        'SKIPPING internal Sensor: "{0}"'.format(sensorSystemName)
                        )
                    internalCounter += 1

            elif ( sensorState != ACTIVE and sensorState != INACTIVE ) :
                self.log.debug(
                    'Deactivating physical Sensor: "{0}"'.format(sensorSystemName)
                    )
                sensor.setState( INACTIVE )
                self.waitMsec(125)  # stall for Command Station action
                physicalCounter += 1

            else:
                self.log.debug(
                    'Skipping physical {0} Sensor: "{1}"\
                    '.format(self.sensorStateName(sensorState),sensorSystemName)
                    )
                skippingCounter += 1

        # Tell the log how many sensors we found, for sanity checking
        self.log.info ( "Internal Sensor Count = {0}".format( internalCounter ) )
        self.log.info ( "Initialized Physical Sensor Count = {0}".format( physicalCounter ) )
        self.log.info ( "Skipped Physical Sensor Count = {0}".format( skippingCounter ) )
        return

########    END OF InitializeTurnoutsSensors.deacivateSensors()

    # Define routine to map status numbers to text
    def sensorStateName(self, state) :
        if (state == ACTIVE) :
            return "ACTIVE"
        if (state == INACTIVE) :
            return "INACTIVE"
        if (state == INCONSISTENT) :
            return "INCONSISTENT"
        if (state == UNKNOWN) :
            return "UNKNOWN"
        return "(invalid)"

###############################
########    END OF class InitializeTurnoutsSensors

# Launch the initialization task
ITS = InitializeTurnoutsSensors("Init Turnouts Sensors")
ITS.start()

