
#
# A Digitrax BXP88 Short-Circuit-to-Sensor "follower"
#
# This script provides a set of JMRI sensors where each sensor's state "follows"
# one of the Digitrax BXP88 device's detection sections reported "Short Circuit state",
# as reported by LocoNet messaging.
#
# This script can be configured to monitor one BXP88 device or all devices.  Within
# any BXP88 device, this script separately monitors all 8 of the device's detection
# sections.  Each separate BXP88 detection section is monitored via a separate JMRI
# "internal" sensor.
#
# Sensor naming is of the form "ISPM88ShortX-Y".  This is interpreted as:
#    "I" for JMRI's "Internal" object grouping
#    "S" for "Sensor" (in the "Internal" object grouping)
#    "PM" for "Power Management" effects
#    "88" for a source from a "BXP88" device
#    "Short" to denote the sensor as reporting "Short-circuit detection" state
#    "X" represents the BoardID number reported in the LocoNet message from the BXP88
#         device
#    "-" is a character to separate the device IDfrom the "Detection Section" number
#    "Y" represents the "Detection Section" number
#
# Some Examples"
#    ISPM88Short1-1     the JMRI internal sensor for BXP88 (board ID 1) Detection Section 1
#    ISPM88Short1-2     the JMRI internal sensor for BXP88 (board ID 1) Detection Section 2
#    ISPM88Short1-3     the JMRI internal sensor for BXP88 (board ID 1) Detection Section 3
#      ...
#    ISPM88Short1-8     the JMRI internal sensor for BXP88 (board ID 1) Detection Section 8
#
#    ISPM88Short2-1     the JMRI internal sensor for BXP88 (board ID 2) Detection Section 1
#    ISPM88Short2-2     the JMRI internal sensor for BXP88 (board ID 2) Detection Section 2
#    ISPM88Short2-3     the JMRI internal sensor for BXP88 (board ID 2) Detection Section 3
#      ...
#    ISPM88Short2-8     the JMRI internal sensor for BXP88 (board ID 2) Detection Section 8
#      ...
#    ISPM88Shout127-1   the JMRI internal sensor for BXP88 (board ID 2) Detection Section 8
#
# As written, this script supports either
#   - monitoring a single BXP88 device, specified by BoardId number; _all
#         eight "Detection sections"_, via separate sensors
#   - monitoring of _all_ BXP88 devices and each 8 Detection sensors
#        of each BXP88, via separate sensors (8 sensors per BXP88).
#
# Configuring the script
# ----------------------
# - See comments below for information on configuring the behavior of this
#     script:
#     -- the "interestingBoardIdNumber" determines what BXP88 device BoardID
#        value to watch for, or, alternately, configures the script to monitor
#        for LocoNet messages from _all_ BXP88 BoardID values, and causes this
#        script to follow each unique BXP88 BoardID number using a unique Sensor.
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
# defines a "Bxp88ShortCircuitStateListener" class which implements a JMRI
# "LocoNetListener" which has BXP88-specific features.  The class's "message()"
# method is triggered upon JMRI receipt of a LocoNet message.  It "parses" the
# received LocoNet message to determine if it is a valid "Short-circuit" status
# message from a BXP88 device.  If it is, the "BoardID" value is extracted and
# checked to determine if it is a BXP88 for which the message should be reported
# via a JMRI Sensor object(s).  This determination is made in the
# isInterestingBoardId()method in Section 2, described below.
#
# For a message that is interesting to the script, the BoardId number from the
# message is used to create eight variables representing the JMRI Sensor name(s)
# associated with the eight detection sections which report short circuit status.  Once
# created (if necessary), those variables are updated with the current short circuit
# status from the message.
#
# The isInterestingBoardId() method uses the user-customization variables of
# Section 1, along with the LocoNet BXP88 Short-circuit status message's extracted
# boardID value, to determine whether or not to update a sensor.  It returns True
# if the message's BoardID value refers to a BXP88 which should be tracked via a
# JMRI sensor(s), or it returns False to indicate that the message should be ignored.
#
# The third part of the script creates an instance of the LocoNet Listener class
# and "connects" that instance to the LocoNet connection.
#
# Notes and Limitations
# ---------------------
#
# Note the following limitations:
# - So far as the script author knows, there is _no_ way to query the short-circuit
#     state of a BXP88 device.  As such, until a BXP88 device's detection section
#     changes its short-circuit state, there is _no_ way to tell what the device's
#     detection section's current state is.
#
# - This script creates, if necessary, the Internal Sensor used to "follow" the
#     BXP88 device's detection section's short-circuit state.  Before a BXP88 reports
#     its short-circuit state, JMRI will _not_ have a corresponding Sensor object,
#     unless you have opened a JMRI "panel" XML file which was saved when the
#     corresponding sensor was known to JMRI.
#
# - If you open a saved JMRI Panel XML file and that file had one or more JMRI
#     Sensor objects created by this script, such as if you "save" a panel XML
#     file, JMRI will create the associated Sensors but will leave those sensors
#     in the "unknown" state.  Once a BXP88 sends a "Short-circuit" event
#     LocoNet message, the associated sensor(s) will be updated to "Inactive" or
#     "Active", as appropriate.
#
# - This script provides little if any "error-checking" of the configuration
#     variables.  Specifying an "out-of-range" connection index will result in
#     an exception reported in the JMRI Console log as well as failure of the
#     script to perform.  Specifying a BoardID value of 0 will not result in
#     capture of _any_ BXP88 short-circuit data.
#     Various other configuration boo-boos may result in exceptions in the log
#     and/or failure of this script to perform.
#
# Script version 1.0 created 25Nov2024 by Bob M.

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

reportAllBxp88s = False # assume that only a specific BXP88 Board ID number
                        # will be reported.  This value may be overridden below.

# Initialize the variable "interestingBoardIdNumber" to reflect the
# BoardId of the BXP88 you care about.  If you want to create a sensor
# for _each_ BXP88 BoardId that reports autoreversing state, set
# interestingBoardIdNumber to a negative value.  Uncomment (and modify, # as
# needed) one of the examples shown below.
#
# Example: follow only Autoreversing messages from BXP88 BoardID 1
#interestingBoardIdNumber = 1
#
# Example: follow only Autoreversing messages from BXP88 BoardID 12
interestingBoardIdNumber = 12

# Example: follow every BXP88 Short-Circuit message, regardless of BoardID, by
# overriding the previous value of reportAllBxp88s.  Note that the value of the
# interestingBoardIdNumber variable has no effect which reportAllBxp88s is True.:
reportAllBxp88s = True

# Print debugging messages to the "script output" window?
#  - True enables the debugging messages.
#  - False disables the debugging messages.
DebuggingMessages = True

# Part 2:

# Define a LocoNet "BXP88 Short-Circuit Event" listener class
class Bxp88ShortCircuitStateListener(jmri.jmrix.loconet.LocoNetListener) :
    def isInterestingBoardId(self, boardId) :
        # This method is used to determine whether a particular BXP88's
        # short-circuit messages will be used to update appropriately-named
        # JMRI sensors.
        #
        #   Returns True to update the sensor based on the LocoNet message contents.
        #   Returns False to ignore the BXP88's LocoNet Message.
        #
        # The user may modify this method to suit his/her personal "selection"
        # criteria.

        if (reportAllBxp88s == True) :
            return True
        if (boardId == interestingBoardIdNumber) :
            return True
        return False

    def message(self, msg) :
        # This method (if registered as a LocoNet listener!) parses all incoming
        # LocoNet messages and deals with BXP88 autoreversing messages.

        # Is this message an autoreversing message from a BPX88 device?
        if ((msg.getNumDataElements() == 6) and (msg.getElement(0) == 0xD0) \
            and ((msg.getElement(1) & 0x7E) == 0x62) and ((msg.getElement(3) & 0xF0) == 0x20) \
            and ((msg.getElement(4) & 0xF0) == 0x20)) :

            # Yes, the message is for a BXP88 autoreversing message!
            boardId = 1 + (msg.getElement(2) & 0x7F) + ((msg.getElement(1) & 0x1) << 7)

            # Determine whether to update a sensor for the reported
            # BoardId number seen in this LocoNet message
            if (self.isInterestingBoardId(boardId) == False) :
                # Not interested in this particular board number
                if (DebuggingMessages == True) :
                    print ("Ignoring message apparantly from BXP88 with boardID #"),
                    print (boardId),
                    print (".")
                return

            for section in range (0,8):
                # Specify the sensor to be updated (create it if it isn't
                # present!)
                s = sensors.provideSensor("ISPM88Short"+str(boardId)+"-"+str(section+1))

                # Update the sensor based on data from the LocoNet message
                if (section < 4) :
                    data = (msg.getElement(4) >> section) & 0x01
                else:
                    data = (msg.getElement(3) >> (section - 4) ) & 0x01

                if (data == 0) :
                    # Not Shorted!
                    s.state = INACTIVE
                    if (DebuggingMessages == True) :
                        print ("Sensor"),
                        print ( s.getSystemName()),
                        print ("is set to Inactive (i.e. 'live' track) for"),
                        print ("short-circuit state of section "),
                        print (str(section+1)),
                        print ("of BXP88 with boardID #"),
                        print (boardId),
                        print (".")

                else :
                    # Shorted!
                    s.state = ACTIVE
                    if (DebuggingMessages == True) :
                        print ("Sensor"),
                        print ( s.getSystemName()),
                        print ("is set to Active (i.e. 'shorted' track) for"),
                        print ("short-circuit state of section "),
                        print (str(section+1)),
                        print ("of BXP88 with boardID #"),
                        print (boardId),
                        print (".")


        # Nothing more to do, so ...
        return

# Part 3:

# Create an instance of the listener class
ln = Bxp88ShortCircuitStateListener()

# Register the Listener
jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(connectionIndex - 1).getLnTrafficController().addLocoNetListener(0xFF,ln)

if (DebuggingMessages == True) :
    print "Registered the BXP88 Short-Circuit Message listener"

# end of script
