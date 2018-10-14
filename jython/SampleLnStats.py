# This is script for samples the LocoNet statistics
# once per minute and prints them out
#
# Author: Bob Jacobsen, copyright 2004
# Author: B. Milhaupt, copyright 2017
# Part of the JMRI distribution

# make the jmri libraries easily availble
import jmri
import java
import java.util

class SampleLnStats(jmri.jmrit.automat.AbstractAutomaton) :

    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
        self.lastReceivedMsgs = 0
        self.lastReceivedBytes = 0
        self.lastTransmittedMsgs = 0
        self.lastBreaks = -1
        self.lastErrors = -1
        self.errors = 0
        self.breaks = 0

        # get the LocoNet connection (the first of potentially several LocoNet connections)
        myLocoNetConnection = jmri.InstanceManager.getList(jmri.jmrix.loconet.LocoNetSystemConnectionMemo).get(0);

        # create LocoStatsFunc instance
        self.monitor = jmri.jmrix.loconet.locostats.LocoStatsFunc(myLocoNetConnection)

        # create a listener for LocoNet Status events
        myLocoStatsListener = MyListener()

        # add the listener
        self.monitor.addLocoNetInterfaceStatsListener(myLocoStatsListener)

        # get the slot manager instance and traffic controller instance, for use later
        self.slotManager = myLocoNetConnection.getSlotManager()
        self.trafficController = myLocoNetConnection.getLnTrafficController()

        return


    # define how long to wait between samples (in seconds)
    #
    # If you want to change the sample interval,
    # just change the delay variable here
    delay = 15

    # define the filename used for the output file
    filename = 'LocoNetStatsSamples'

    # handle() is called repeatedly until it returns false.
    #
    # In this case, it prints a line each time around
    def handle(self):

        # request the current error counts
        self.monitor.sendLocoNetInterfaceStatusQueryMessage()
        self.waitMsec(1000)
        nowErrors = self.errors;
        nowBreaks = self.breaks;

        # if the LocoBuffer wasn't read, that's a special case
        if ( nowErrors == -1 or self.lastErrors == -1) :
            delErrors = -1
        else :
            delErrors = nowErrors - self.lastErrors
        if ( nowBreaks == -1 or self.lastBreaks == -1) :
            delBreaks = -1
        else :
            delBreaks = nowBreaks - self.lastBreaks

        # get the current values for traffic
        nowReceivedMsgs = self.trafficController.getReceivedMsgCount()
        nowReceivedBytes = self.trafficController.getReceivedByteCount()
        nowTransmittedMsgs = self.trafficController.getTransmittedMsgCount()

        # get the current values for slots in use
        nowSlotsUsed = self.slotManager.getInUseCount()

        # calculate changes
        delReceivedMsgs = nowReceivedMsgs - self.lastReceivedMsgs
        delReceivedBytes = nowReceivedBytes - self.lastReceivedBytes
        delTransmittedMsgs = nowTransmittedMsgs - self.lastTransmittedMsgs

        self.lastReceivedMsgs = nowReceivedMsgs
        self.lastReceivedBytes = nowReceivedBytes
        self.lastTransmittedMsgs = nowTransmittedMsgs
        self.lastErrors = nowErrors
        self.lastBreaks = nowBreaks

        date = java.util.Date().toString()
        # convert numbers to a single output string in the CSV format
        result = ''+date+','+str(delReceivedMsgs)+','+str(delReceivedBytes)+','+str(delTransmittedMsgs)+','+str(nowSlotsUsed)+','+str(delBreaks)+','+str(delErrors)
        print result

        # also open the file for appending, append the line, and close
        f = open(self.filename, "a")
        f.write(result+'\n')
        f.close()

        # wait to do next sample ( 1 second was used in the error count)
        self.waitMsec((self.delay-2)*1000)

        # and continue around again
        return 1    # to continue

# end of class definition

class MyListener(jmri.jmrix.loconet.locostats.LocoNetInterfaceStatsListener):
    # a listener for LocoNet status information events
    def notifyChangedInterfaceStatus(self, o) :
        if (o.class == jmri.jmrix.loconet.locostats.PR3MS100ModeStatus):
            # PR3 does not provide a count of breaks
            # PR3 provides "bad message count", which we equate with "errors"
            self.errors = o.badMsgCnt
        elif (o.class == jmri.jmrix.loconet.locostats.LocoBufferIIStatus):
            #locoBufferII and LocoBuffer-USB provide "errors" and "breaks"
            self.errors = o.errors
            self.breaks = o.breaks
        # NOTE: other interface types apparantly do not provide useful information

# end of class definition

# create one of these
a = SampleLnStats()

# you can change the filename used
# a.filename = 'BetterName.foo'

# set the name, so you see it to cancel it
a.setName("Sample LocoNet Statistics")

# and start it running
a.start()

