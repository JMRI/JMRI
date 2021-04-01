
#
# A Digitrax BXPA1 AutoReverser-to-Sensor "follower"
#
# This script provides a JMRI sensor where the sensor's state "follows" the 
# reported "autoreverser state" of a Digitrax BXPA1 device, as reported by 
# LocoNet messaging.
#
# This script can be configured to monitor all BXPA1 devices, reporting each 
# individual device's autoreversing state in its own device-specific sensor.
#
# Sensor naming is of the form "ISPM1aReversedX".  This is interpreted as: 
#    "I" for JMRI's "Internal" object grouping
#    "S" for "Sensor" (in the "Internal" object grouping)
#    "PM" for "Power Management" effects
#    "1a" for a source from a "BXP1a" device
#    "Reversed" to denote the sensor as reporting "autoreversing" state (Active
#         means "Reversed", Inactive means "Normal")
#    "X" is the BoardID number reported in the LocoNet message from the BXPA1 
#         device
#
# As written, this script supports either
#   - monitoring a single BXPA1 device, specified by BoardId number
#   - monitoring of _all_ BXPA1 devices and reporting each via a separate sensor.
#
# Configuring the script
# ----------------------
# - See comments below for information on configuring the behavior of this 
#     script:
#     -- the "interestingBoardIdNumber" determines what BXPA1 device BoardID 
#        value to watch for, or, alternately, configures the script to monitor
#        for LocoNet messages from _all_ BXPA1 BoardID values, and causes this
#        script to follow each unique BXPA1 BoardID number using a unique Sensor.
#     -- DebuggingMessages determines whether debugging messages will be sent to 
#        the JMRI "Script Output" window.
#
# Script details
# --------------
# This script has three main functional parts (in order of appearance).
#
# The first part provides some "import" statements and declares some variables
# that influence script behavior.  Users may wish to modify some variable values 
# in this section.
#
# The second part is where the bulk of the work is done, because it declares and
# defines a "Bxpa1ReverserStateListener" class which implements a JMRI 
# "LocoNetListener" which has BXPA1-specific features.  The class's "message()" 
# method is triggered upon JMRI receipt of a LocoNet message.  It "parses" the
# received LocoNet message to determine if it is a valid "Autoreversing" status 
# message from a BXPA1 device.  If it is, the "BoardID" value is extracted and
# checked to determine if it is a BXPA1 for which the message should be reported
# via a JMRI Sensor object.  This determination is made in the isInterestingBoardId()
# method in Section 2, described below.
#
# For a message that is interesting to the script, the BoardId number from the
# message is used to create a variable representing the JMRI Sensor name, and
# that sensor name is used to create a JMRI sensor (if it does not already 
# exist) and update the sensor's value.
#
# The isInterestingBoardId() method uses the user-customization variables of 
# Section 1, along with the LocoNet BXPA1 Autoreverse status message's extracted 
# boardID value, to determine whether or not to update a sensor.  It returns True
# if the message's BoardID value refers to a BXPA1 which should be tracked via a 
# JMRI sensor, or it returns False to indicate that the message should be ignored.
#
# The third part of the script creates an instance of the LocoNet Listner class
# and "connects" that instance to the LocoNet connection.
#
# Notes and Limitations
# ---------------------
#
# Note the following limitations:
# - So far as the script author knows, there is _no_ way to query the autoreversing
#     state of a BXPA1 device.  As such, until a BXPA1 device changes its 
#     auto-reversing state, there is _no_ way to tell what the device's current
#     state is.
#
# - This script creates, if necessary, the Internal Sensor used to "follow" the
#     BXPA1 device's autoreversing state.  Before a BXPA1 reports its autoreversing
#     state, JMRI will _not_ have a corresponding Sensor object, unless you have
#     opened a JMRI "panel" XML file which was saved when the corresponding sensor
#     was known to JMRI.
#
# - If you open a saved JMRI Panel XML file and that file had one or more JMRI 
#     Sensor objects created by this script, such If you "save" a panel XML 
#     file, JMRI will create the associate Sensors but will leave those sensors
#     in the "unknown" state.  Once a BXPA1 sends an "Autoreversing" event 
#     LocoNet message, the associated sensor will be updated to "Inactive" or 
#     "Active", as appropriate.
#
# - This script provides little if any "error-checking" of the configuration
#     variables.  Specifying an "out-of-range" connection index will result in 
#     an exception reported in the JMRI Console log as well as failure of the 
#     script to perform.  Specifying a BoardID value of 0 will not result in 
#     capture of _any_ BXPA1 autoreversing data.
#     Various other configuration boo-boos may result in exceptions in the log 
#     and/or failure of this script to perform. 
#
# - Because of an apparent BXPA1 firmware issue seen in at least some BXPA1 devices,
#     BXPA1 devices with some BoardID values report their status as if they had 
#     different BoardID values.  To avoid this problem, avoid using BoardID 
#     values where
#           (your BoardID value) / 8
#     has a fractional part of .0, .625, .75, or .825.
#     As examples:
#                             BoardID / 8
#                  BoardId    fractional 
#        BoardId     / 8         part        OK to use?
#           1        0.125       .125          Yes
#           2        0.25        .25           Yes
#           3        0.375       .375          Yes
#           4        0.5         .5            Yes
#           5        0.625       .625          No
#           6        0.75        .75           No
#           7        0.825        .825          No
#           8        1.0         .0            No
#           9        1.125       .125          Yes
#           10       1.25        .25           Yes
#           11       1.375       .375          Yes
#           12       1.5         .5            Yes
#           13       1.625       .625          No
#           14       1.75        .75           No
#           15       1.825        .825          No
#           16       2.0         .0            No
#           17       2.125       .125          Yes
#      ...
#
# - It is possible that a BXPA1 firmware revision that resolves the above BoardId 
#     issue _could_ require re-work of the message parsing found in the LocoNet 
#     Listener implementation.
#
# Script version 1.0 created 30Mar2021 by Bob M.

# Part 1: 

import jmri
import java

# Initialize the variable "connectionIndex" to specify which of potentially 
# several LocoNet connections this script watches.  This is specified by the 
# LocoNet connection's "index":
#    - When you have just a single connection, the only usable index is 1.
#    - When you have more than one LocoNet connection defined, the index is the
#      count, from the left, of the "tabs" for LocoNet connections as seen 
#      in the "Connections" page of the JMRI "Preferences".
connectionIndex = 1 # this is appropriate for a JMRI install with only a single 
                    # LocoNet connection, or when monitoring the _first_ of  
                    # available (and active) LocoNet connections.

reportAllBxpa1s = False # assume that only a specific BXPA1 Board ID number
                        # will be reported.  This value may be overridden below.

# Initialize the variable "interestingBoardIdNumber" to reflect the
# BoardId of the BXPA1 you care about.  If you want to create a sensor
# for _each_ BXPA1 BoardId that reports autoreversing state, set
# interestingBoardIdNumber to a negative value.  Uncomment (and modify, # as 
# needed) one of the examples shown below.
#
# Example: follow only Autoreversing messages from BXPA1 BoardID 1
#interestingBoardIdNumber = 1
#
# Example: follow only Autoreversing messages from BXPA1 BoardID 12
interestingBoardIdNumber = 12

# Example: follow every BXPA1 Autoreversing message, regardless of BoardID, by 
# overriding the previous value of reportAllBxpa1s.  Note that the value of the
# interestingBoardIdNumber variable has no effect which reportAllBxpa1s is True.:
reportAllBxpa1s = True

# Print debugging messages to the "script output" window?
#  - True enables the debugging messages.
#  - False disables the debugging messages.
DebuggingMessages = True

# Part 2: 

# Define a LocoNet "BXPA1 Autoreversing Event" listener class
class Bxpa1ReverserStateListener(jmri.jmrix.loconet.LocoNetListener) :
    def isInterestingBoardId(self, boardId) :
        # This method is used to determine whether a particular BXPA1's 
        # autoreversing messages will be used to update an appropriately-named
        # JMRI sensor.  
        #
        #   Returns True to update the sensor based on the LocoNet message contents.  
        #   Returns False to ignore the BXPA1's LocoNet Message.
        #
        # The user may modify this method to suit his/her personal "selection"
        # criteria.

        if (reportAllBxpa1s == True) :
            return True
        if (boardId == interestingBoardIdNumber) :
            return True
        return False

    def message(self, msg) :
        # This method (if registered as a LocoNet listener!) parses all incoming
        # LocoNet messages and deals with BXPA1 autoreversing messages.

        # Is this message an autoreversing message from a BPXA1 device?
        if ((msg.getNumDataElements() == 6) and (msg.getElement(0) == 0xD0) and ((msg.getElement(1) & 0x60) == 0x60) and ((msg.getElement(3) & 0xF0) == 0x50) ) :

            # Yes, the message is for a BXPA1 autoreversing message!
            boardId = 1 + (msg.getElement(2) * 8) + (msg.getElement(3) & 0x7)

            # Determine whether to update a sensor for the reported
            # BoardId number seen in this LocoNet message
            if (self.isInterestingBoardId(boardId) == False) :
                # Not interested in this particular board number
                if (DebuggingMessages == True) :
                    print ("Ignoring message apparantly from BXPA1 with boardID #"),
                    print (boardId),
                    print (".")
                return

            # Specify the sensor to be updated (create it if it isn't
            # present!)
            s = sensors.provideSensor("ISPM1aReversed"+str(boardId))

            # Update the sensor based on data from the LocoNet message
            if ((msg.getElement(3) & 0x08) == 0) :

                # update the sensor for "Normal" polarity
                s.state = INACTIVE
                if (DebuggingMessages == True) :
                    print ("Sensor"),
                    print ( s.getSystemName()),
                    print ("is set to Inactive (i.e. normal polarity) for"),
                    print ("autoreversing state of BXPA1 with boardID #"),
                    print (boardId),
                    print (".")

            else :
                # Update the sensor for "Reversed" polarity
                s.state = ACTIVE
                if (DebuggingMessages == True) :
                    print ("Sensor"),
                    print ( s.getSystemName()),
                    print ("is set to Active (i.e. reversed polarity) for"),
                    print ("autoreversing state of BXPA1 with boardID #"),
                    print (boardId),
                    print (".")

        # Nothing more to do, so ...
        return

# Part 3: 

# Create an instance of the listener class
ln = Bxpa1ReverserStateListener()

# Register the Listener
jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex - 1).getLnTrafficController().addLocoNetListener(0xFF,ln)

if (DebuggingMessages == True) :
    print "Registered the BXPA1 Autoreversing Message listener"

# end of script
