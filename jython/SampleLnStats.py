# This is script for samples the LocoNet statistics
# once per minute and prints them out
#
# Author: Bob Jacobsen, copyright 2004
# Part of the JMRI distribution

# make the jmri libraries easily availble
import jmri
import java

class SampleLnStats(jmri.jmrit.automat.AbstractAutomaton) :
    
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
        self.lastReceivedMsgs = 0
        self.lastReceivedBytes = 0
        self.lastTransmittedMsgs = 0
        self.lastBreaks = -1
        self.lastErrors = -1
        self.firstTime = True
        
        # create, but don't show, a LocoBufferStatsFrame
        #print("fetching default memo")
        self.memo = jmri.InstanceManager.getDefault(jmri.jmrix.loconet.LocoNetSystemConnectionMemo)
        #print("got memo")
        self.monitor = jmri.jmrix.loconet.locostats.LocoStatsPanel(self.memo)
        #self.monitor = jmri.jmrix.loconet.swing.LnNamedPaneAction("Loco Stats", 
        #	jmri.util.swing.sdi.JmriJFrameInterface(), 
        #	"jmri.jmrix.loconet.locostats.LocoStatsPanel", 
        #	self.memo).actionPerformed(None)
        #print("got monitor")
        return
        
        
    # define how long to wait between samples (in seconds)
    delay = 15
    
    # define the filename used for the output file
    filename = 'LocoNetStatsSamples.csv'
    
    # handle() is called repeatedly until it returns false.
    #
    # In this case, it prints a line each time around
    def handle(self):

        # request the current error counts
        self.monitor.setBreaksText('-1')
        self.monitor.setErrorsText('-1')
        self.monitor.requestUpdate()
        self.waitMsec(1000)  
        nowErrors = int(self.monitor.getErrorsText())
        nowBreaks = int(self.monitor.getBreaksText())
        
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
        t = jmri.jmrix.loconet.LnTrafficController.instance()
        nowReceivedMsgs = t.getReceivedMsgCount()
        nowReceivedBytes = t.getReceivedByteCount()
        nowTransmittedMsgs = t.getTransmittedMsgCount()

        # get the current values for slots in use
        s = jmri.jmrix.loconet.SlotManager(t)
        nowSlotsUsed = s.getInUseCount()
        
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
        if (self.firstTime) :
        	header = 'Date,RecvMsg,RecvBytes,XmitMsgs,SlotsUsed,Breaks,Errors'
        	#print header
        result = ''+date+','+str(delReceivedMsgs)+','+str(delReceivedBytes)+','+str(delTransmittedMsgs)+','+str(nowSlotsUsed)+','+str(delBreaks)+','+str(delErrors)
        #print result

        # also open the file for appending, append the line, and close
        #print("writing to file")
        f = open(self.filename, "a")
        if (self.firstTime) :
        	f.write(header+'\n')
        	self.firstTime = False
        f.write(result+'\n')
        f.close()
        #print("File Closed")
        
        # wait to do next sample ( 1 second was used in the error count)
        self.waitMsec((self.delay-2)*1000)
                
        # and continue around again
        return 1    # to continue
    
# end of class definition

# create one of these
a = SampleLnStats()

# if you want to change the sample interval,
# just change the delay variable here
# a.delay = 60

# you can also change the filename used
# a.filename = 'BetterName.foo'

# set the name, so you see it to cancel it
a.setName("Sample LocoNet Statistics")

# and start it running
a.start()


