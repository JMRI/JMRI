# This script runs a loco around the track, controlling the speed
# according to signals and following the blocks.
#
# Author: Ken Cameron, copyright 2009,2010
# Part of the JMRI distribution
#
# The start button is inactive until data has been entered.
#
# The test button is to force re-evaluation of all block and signal values.
#
# If the loco id isn't showing in the block table, do a 'stop'/'start the
# run' to reestablish the loco position.
#
# Notes: 
# 1. This script expects a fully developed set of blocks and signals
#    to exist.
# 2. Best way for that is using the 'Layout Editor' to develop the
#    configuration.
# 3. However, on turnouts, the non-throat (frog) ends of the turnout should
#    not have a track segment attached, the block boundary must be the
#    turnout anchor point. The throat end (points) may have additional
#    track segments in the same block  as the turnout.
# 4. The layout must be laid out like a CTC type panel, meaning string line
#    left to right across the panel. If you need to have multiple lines
#    to build the whole layout place a track segment of the same block
#    at the end of the first line and the start of the next line and
#    use a 'hidden' track segment (same block) to connect
#    those track segments.
# 5. The script keys off the loco id being passed from block to block
#    by the block manager. If you have detector issues and the blocks
#    falsely cycle, this may not work. You may have to add internal
#    sensors to 'buffer' these false cycles using Logix to clean
#    things up.
# 6. FIXED: You no longer need usernames for everything. It will use
#    system or user names depending on what you have in your panel.
# 7. If you create memory values and memory labels on your panel and
#    tie them to the blocks, you will always see the loco id displayed.
#    That is the block value that the script is looking for when
#    tracking the loco.
# NEW FOR Version 2
# 8. It now has options for a rate of speed (still needs the matching
#    throttle setting) for speeds to compute stopping. But it only uses
#    this for stopping on approach to a red signal. When this is added as
#    an 'autopilot' to the throttle interface, it will consider more of
#    these inputs. A zero in the rate will cause it to ignore it.
# 9. If the blocks have the length set (and the rate above) it will try
#    to delay stopping in a red block until 80% into the block.
# NEW FOR Version 3
# 10. Requires the length of blocks be known. In turn it computes the
#   fastest speed that will stop within the next two blocks based
#   on the most restrictive speeds that may be in the next blocks.
#
# Still needing work:
# 1. Improve the method for 'findNextBlock' as it currently uses the
#    direction from the current block. To work for 'physically designed'
#    panels, it would need to focus on the connectivity of the blocks
#    more and less on the direction attributes. It currently fails
#    if a block transitioned more than 90 degrees within a single
#    block.
# 2. Add option to delay halting if it looses sight of the loco.
#    This may help some layouts where the detection 'blinks' out
#    for a few seconds due to dirt etc...
#    Currently you must use something like 'DebounceSensor.py' if you
#    have sensors that don't hold due to the above.
# 3. Learn about the neat features the 'operations' data is adding.
#    This would help with knowing lengths of trains, grades of track,
#    etc to aid in controlling how to stop within a block.
#
# Changes:
# 07/23/2009 - Added a Halt button that tries to -1 the throttle to
#              stop now and not be smooth. Plus a few other fixes
#              like the system vs user name thing.
#
# 08/30/2009 - Added methods so this can be run from a master script
#              for running mulitple throttles. Also improved many
#              reliblity issues in the code.
#
# Much thanks go to the Medina Railroad Museum who was asked for
# something to help out when they have larger number of visitors
# and few staff but want the trains to run around without taking
# staff to keep them from running into each other. What started
# as a simple request grew into this script. I am planning on
# turning it into real code as a next step.
# http://railroadmuseum.net/
#
# Portions of this script are taken from a number of scripts
# by Bob Jacobsen
#
# A AbstractAutomaton is used to get a throttle and run the loco.
#
# A PropertyChangeListener is used to report block information.
# It could have been with AbstractAutomaton but this allows for
# independent action with a block change.
#
# Same thing for also watching signals.
#
# A WindowListener is used to report when the window is closing and
# allow for the removal of the PropertyChangeListener.
#

import jmri
import java
import java.awt
import java.awt.event
import java.beans
import java.util
import javax.swing
import java.util.Calendar

HighDebug = 3
MediumDebug = 2
LowDebug = 1
NoneDebug = 0

# set up a throttle with the loco address
class LocoThrot(jmri.jmrit.automat.AbstractAutomaton) :
    # initialize variables
    locoAddress = None
    currentBlock = None
    currentDirection = jmri.Path.NONE
    currentBlocks = []
    currentNextBlocks = []
    next1Block = None
    next2Block = None
    next3Block = None
    priorBlocks = []
    priornext1Blocks = []
    priorSignal = None
    priorSignalAspect = None
    currentSignal = None
    currentSignalAspect = None
    nearSignal = None
    nearSignalAspect = None
    next1Signal = None
    next1SignalAspect = None
    next2Signal = None
    next2SignalAspect = None
    isRunning = False
    isStarting = False
    isAborting = True
    currentThrottle = None
    scriptFrame = None
    scriptFrameOldX = None
    scriptFarmeOldY = None
    listenerBlocks = []
    listenerBlockListeners = []
    listenerSignals = []
    listenerSignalListeners = []
    greenSignalIcon = None
    greenFlashSignalIcon = None
    yellowSignalIcon = None
    yellowFlashSignalIcon = None
    redSignalIcon = None
    redFlashSignalIcon = None
    darkSignalIcon = None
    unknownSignalIcon = None
    speedChangeTimer = None
    speedChangeListener = None
    shrinkGrow = True
    fullScrollRows = 15
    speedPane = None
    rosterInstance = None
    oldLocoAddress = None
    didWeMoveCounter = 0
    holdMoving = False
    stopBlock = None
    hornDelayTimer = None
    hornDelayListener = None
    throttleManager = None
    askChangeThrottle = False
    askFinishStartButton = False
    methodLocoAddress = None
    methodBlockDirection = None
    methodLocoDirection = None
    methodLocoHeadlight = None
    methodShortHorn = None
    methodLongHorn = None
    methodBlockStart = None
    methodPushShrink = None
    methodPushStart = None
    methodPushStop = None
    methodPushTest = None
    methodBlockStop = None
    methodlocoDistRed = None
    haltOnSignalHeadAppearance = YELLOW
    debugLevel = LowDebug
    movementDetected = False
    
    def init(self):
        #print("start begin:.\n")
        self.setName("RT3: no loco")
        self.setup()
        #print("start end:.\n")
        return
        
    def handle(self):
        if (self.isAborting == True) :
            return 0
        #self.msgText("handle begin:.")
        self.waitMsec(100)
        if (self.askChangeThrottle) :
            self.getNewThrottle()
            self.askChangeThrottle = False
        if (self.askFinishStartButton) :
            self.doFinishStartButton()
            self.askFinishStartButton = False
        if (self.methodLocoAddress != None) :
            self.locoAddress.text = self.methodLocoAddress
            self.methodLocoAddress = None
            self.whenLocoChanged(self)
        if (self.methodBlockDirection != None) :
            if (self.methodBlockDirection == True) :
                self.blockDirection.setSelected(True)
            else  :
                self.blockDirection.setSelected(False)
            self.methodBlockDirection = None
            self.whenLocoChanged(self)
        if (self.methodLocoDirection != None) :
            if (self.methodLocoDirection == True) :
                self.locoForward.setSelected(True)
            else :
                self.locoForward.setSelected(False)
            self.methodLocoDirection = None
            self.whenLocoChanged(self)
        if (self.methodLocoHeadlight != None) :
            if (self.methodLocoHeadlight == True) :
                self.locoHeadlight.setSelected(True)
            else :
                self.locoHeadlight.setSelected(False)
            self.methodLocoHeadlight = None
            self.whenLocoHeadlight(self)
        if (self.methodShortHorn != None) :
            self.methodShortHorn = None
            self.doShortHorn()
        if (self.methodLongHorn != None) :
            self.methodLongHorn = None
            self.doLongHorn()
        if (self.methodBlockStart != None) :
            self.blockStart.text = self.methodBlockStart
            self.methodBlockStart = None
            self.whenLocoChanged(self)
        if (self.methodPushShrink != None) :
            self.methodPushShrink = None
            self.whenShrinkButtonClicked(self)
        if (self.stopButton.isEnabled() == True and self.methodPushStop != None) :
            self.methodPushStop = None
            self.whenStopButtonClicked(self)
        if (self.testButton.isEnabled() == True and self.methodPushTest != None) :
            self.methodPushTest = None
            self.callBackForDidWeMove(self)
        if (self.startButton.isEnabled() == True and self.methodPushStart != None) :
            self.methodPushStart = None
            self.whenStartButtonClicked(self)
        if (self.methodBlockStop != None) :
            self.blockStop.text = self.methodBlockStop
            self.methodBlockStop = None
            self.whenStopBlockChanged(self)
        if (self.methodlocoDistRed != None) :
            self.locoDistRed.text = self.methodlocoDistRed
            self.methodlocoDistRed = None
            self.whenLocoChanged(self)
        # This handles tracking how many overlapping events happened
        #   and insures we run the didWeMove the right number of times
        if ((self.didWeMoveCounter > 0 and self.holdMoving == False) or self.isStarting == True) :
             #self.msgText("didWeMoveCounterCheck: " + str(self.didWeMoveCounter) + " - calling didWeMove")
             self.didWeMove()
             self.didWeMoveCounter = 0
             #self.msgText("didWeMoveCounterCheck: decremented counter down to " + str(self.didWeMoveCounter))
      #self.msgText("handle done")
        return 1 #continue if 1, run once if 0
    
    # allow changes of a signal dropping on the appearance to trigger halting
    def setSignalAppearanceHalt(self, signalIndication) :
        self.haltOnSignalHeadAppearance = signalIndication
        return
        
    # show what level of signal appearance causes a halt on dropping signal
    def returnSignalAppearanceHalt(self) :
        return(str(self.haltOnSignalHeadAppearance))
        
    def getNewThrottle(self) :
        self.holdMoving = True
        if (self.currentThrottle != None) :
            oldId = self.currentThrottle.getLocoAddress()
            if (self.debugLevel >= LowDebug) :
                self.msgText("stop and release the current loco: " + str(oldId))
            self.doStop();
            self.currentThrottle.release(None)
            self.currentThrottle = None
            if (self.debugLevel >= LowDebug) :
                self.msgText("Throttle " + str(oldId) + " released")
        if (self.debugLevel >= LowDebug) :
            self.msgText("Getting throttle") #add text to scroll field
        id = int(self.locoAddress.text)
        if (self.throttleManager.addressTypeUnique() == True) :
            if (self.debugLevel >= HighDebug) :
                self.msgText("id values are not ambiguous")
            isLong = self.throttleManager.canBeLongAddress(id)
        else :
            if (self.debugLevel >= LowDebug) :
                self.msgText("id values are ambiguous")
            isLong = True
            if (self.throttleManager.canBeShortAddress(id)) :
                if (self.debugLevel >= LowDebug) :
                    self.msgText("id could be a short.")
                if (self.locoLong.isSelected() == False) :
                    isLong = False
        if (self.debugLevel >= LowDebug) :
            self.msgText("Getting throttle " + str(id) + " " + str(isLong))
        throttle = self.getThrottle(id, isLong)
        self.currentThrottle = throttle
        if (self.currentThrottle == None) :
            if (self.debugLevel >= LowDebug) :
                self.msgText("Couldn't assign throttle! - Run stopped")
            self.doHalt()
        else : 
            if (self.debugLevel >= LowDebug) :
                self.msgText("got throttle: " + str(self.currentThrottle.getLocoAddress()))
            self.currentThrottle.setIsForward(self.locoForward.isSelected())
            self.currentThrottle.setF0(self.locoHeadlight.isSelected())
            self.currentThrottle.setF1(self.locoBell.isSelected())
            self.holdMoving = False
        return
        
    # return userName if available, else systemName
    def giveBlockName(self, block) :
        if (block == None) :
            return 'None'
        else :
            if ((block.getUserName() == None) or (block.getUserName() == '')) :
                return block.getSystemName()
            else :
                return block.getUserName()

    # return userName if available, else systemName
    def giveSignalName(self, sig) :
        if (sig == None) :
            return 'None'
        else :
            if ((sig.getUserName() == None) or (sig.getUserName() == '')) :
                return sig.getSystemName()
            else :
                return sig.getUserName()

    # return userName if available, else systemName
    def giveTurnoutName(self, to) :
        if (to == None) :
            return 'None'
        else :
            if ((to.getUserName() == None) or (to.getUserName() == '')) :
                return to.getSystemName()
            else :
                return to.getUserName()

    # return userName if available, else systemName
    def giveSensorName(self, sen) :
        if (sen == None) :
            return 'None'
        else :
            if ((sen.getUserName() == None) or (sen.getUserName() == '')) :
                return sen.getSystemName()
            else :
                return sen.getUserName()
                
    # Isolate the callback handling from the didWeMove processing
    #   this makes dealing with multiple events simpler
    def callBackForDidWeMove(self, event) :
        self.didWeMoveCounter = self.didWeMoveCounter + 1
        if (self.debugLevel >= HighDebug) :
            self.msgText("callBackForDidWeMove(" + str(event) + ")")
            self.msgText("counter = " + str(self.didWeMoveCounter))
        # the handle() will invoke the didWeMove() as needed
        return
         
    # figure out if we moved and where
    def didWeMove(self) :
        self.movementDetected = False
        if (self.debugLevel >= HighDebug) :
            self.msgText("didWeMove start: " + self.giveBlockName(self.currentBlock) + ":" + self.giveBlockName(self.next1Block))
        if (self.currentThrottle == None) :
            if (self.debugLevel >= LowDebug) :
                self.msgText("didWeMove called while currentThrottle was None")
            return
        # allow for a 2nd try. In case the id in block didn't move, but we hit the next signal
        # try reading the blocks one more time in case we now see that we did move.
        
        tryCount = 0
        tryCountLimit = 2
        while (tryCount < tryCountLimit) :
            tryCount = tryCount + 1
            if (self.debugLevel >= HighDebug) :
                if (self.currentBlock != None) :
                    self.msgText("Current block: " + self.giveBlockName(self.currentBlock))
            newCurrentBlocks = self.findCurrentBlocks()
            if (len(newCurrentBlocks) == 0) :
                self.msgText("Can't find loco!! Doing halt!!")
                self.doHalt()
            # new current block must be farthest connected to current block in current direction chain
            oldCurrent = self.currentBlock
            oldSignal = self.currentSignal
            oldnext1Signal = self.next1Signal
            oldnext2Signal = self.next2Signal
            oldAspect = self.currentSignalAspect
            oldFarAspect = self.next1SignalAspect
            oldnext2Block = self.next1Block
            tryBlock = self.currentBlock
            nearSignal = None
            next1Signal = None
            next2Signal = None
            newCurrent = None
            watchSignal = None
            giveUpTimer = 0
            while (giveUpTimer < 10) :
                giveUpTimer = giveUpTimer + 1
                newCurrent = self.findNewCurrentBlock(tryBlock, newCurrentBlocks, self.currentDirection)
                if (newCurrent == None) :
                    newBlockText = "None"
                else :
                    newBlockText = self.giveBlockName(newCurrent)
                #self.msgText("try " + str(giveUpTimer) + " " + self.giveBlockName(tryBlock) + " " + newBlockText)
                if ((newCurrent == tryBlock) or (newCurrent == None)) :
                    break
                else :
                    tryBlock = newCurrent
            if (self.debugLevel >= MediumDebug) :
                self.msgText("tryBlock: " + self.giveBlockName(tryBlock) + " oldCurrent: " + self.giveBlockName(oldCurrent) + " isStarting: " + str(self.isStarting))
            if (tryBlock != oldCurrent or self.isStarting == True) :
                # we did move somewhere
                self.blockNow.text = " "
                self.blockNowLength.text = " "
                self.blockNext.text = " "
                self.blockNextLength.text = " "
                self.blockBeyond.text = " "
                self.blockBeyondLength.text = " "
                self.stopDistance.text = "0"
                self.stopDistanceForSpeed.text = "0"
                self.currentBlock = tryBlock
                self.blockStart.text = self.giveBlockName(self.currentBlock)
                self.blockNow.text = self.giveBlockName(self.currentBlock)
                self.blockNowLength.text = str(self.currentBlock.getLengthIn())
                self.next1Block = self.findNextBlock(self.currentBlock)
                self.next2Block = None
                self.next3Block = None
                self.testAddBlockListener(self.currentBlock)
                if (self.next1Block != None) :
                    self.blockNext.text = self.giveBlockName(self.next1Block)
                    self.blockNextLength.text = str(self.next1Block.getLengthIn())
                    self.stopDistance.text = str(float(self.stopDistance.text) + self.next1Block.getLengthIn())
                    self.testAddBlockListener(self.next1Block)
                    self.next2Block = self.findNextBlock(self.next1Block)
                    if (self.next2Block != None) :
                        self.blockBeyond.text = self.giveBlockName(self.next2Block)
                        self.blockBeyondLength.text = str(self.next2Block.getLengthIn())
                        self.stopDistance.text = str(float(self.stopDistance.text) + self.next2Block.getLengthIn())
                        self.testAddBlockListener(self.next2Block)
                        self.next3Block = self.findNextBlock(self.next2Block)
                self.priorBlock = oldCurrent
                self.priorBlocks = self.currentBlocks
            # find signals from currentBlock
            if (self.currentBlock != None and self.next1Block != None) :
                nearSignal = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(self.currentBlock, self.next1Block)
                if (nearSignal != None) :
                    self.testAddSignalListener(nearSignal)
                if (self.debugLevel >= MediumDebug) :
                    self.msgText("finding nearSignal between " + self.giveBlockName(self.currentBlock) + " and " + self.giveBlockName(self.next1Block) + " found " + self.giveSignalName(nearSignal))
            if (self.next1Block != None and self.next2Block != None) :
                next1Signal = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(self.next1Block, self.next2Block)
                if (next1Signal != None) :
                    self.testAddSignalListener(next1Signal)
                if (self.debugLevel >= MediumDebug) :
                    self.msgText("finding next1Signal between " + self.giveBlockName(self.next1Block) + " and " + self.giveBlockName(self.next2Block) + " found " + self.giveSignalName(next1Signal))
            if (self.next2Block != None and self.next3Block != None) :
                next2Signal = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(self.next2Block, self.next3Block)
                if (next2Signal != None) :
                    self.testAddSignalListener(next2Signal)
                if (self.debugLevel >= MediumDebug) :
                    self.msgText("finding next2Signal between " + self.giveBlockName(self.next2Block) + " and " + self.giveBlockName(self.next3Block) + " found " + self.giveSignalName(next2Signal))
            if (self.blockAhead2.isSelected() == False) :
                if (self.debugLevel >= HighDebug) :
                    self.msgText("3 block test: " + self.giveBlockName(self.currentBlock))
                if (nearSignal != None) :
                    watchSignal = nearSignal
                else :
                    if (next1Signal != None) :
                        watchSignal = next1Signal
                    else :
                        if (next2Signal != None) :
                            watchSignal = next2Signal
            else :
                if (self.debugLevel >= MediumDebug) :
                    self.msgText("4 block test: " + self.giveBlockName(self.next1Block))
                if (next1Signal != None) :
                    watchSignal = next1Signal
                else :
                    if (next2Signal != None) :
                        watchSignal = next2Signal
            # if we didn't find a signal, treat as RED
            if (watchSignal == None) :
                watchAspect = RED
            else :
                watchAspect = watchSignal.getAppearance()
            # if we moved or the signal head changed or the aspect changed
            if (self.debugLevel >= MediumDebug) :
                self.msgText("test blocks: " + self.giveBlockName(oldCurrent) + ":" + self.giveBlockName(self.currentBlock) + ":" + self.giveBlockName(self.next1Block))
                self.msgText("test signals: " + self.giveSignalName(oldSignal) + ":" + self.giveSignalName(watchSignal))
                self.msgText("test aspects: " + self.textSignalAspect(oldAspect) + ":" + self.textSignalAspect(watchAspect))
            # set flag is we just moved, needed for some timer controls
            if oldCurrent != self.currentBlock :
                self.movementDetected = True
            if (self.movementDetected or oldSignal != watchSignal or oldAspect != watchAspect or self.isStarting == True) :
                # something changed, we calc the new speed
                if (oldCurrent == self.currentBlock and oldSignal == watchSignal and self.compareSignalAspects(oldAspect, watchAspect) < 0 and self.isStarting == False)  :
                    # signal dropped, that's bad
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("signal dropped, same signal being watched.")
                    if (self.compareSignalAspects(self.haltOnSignalHeadAppearance, watchAspect) >= 0) : # Only stop on dropping below this
                        self.findNewSpeed(self.currentBlock, self.next1Block, watchSignal)
                    else :
                        self.msgText("Signal dropped in front of train. Halting!!")
                        if (tryCount < tryCountLimit) :
                            if (self.debugLevel >= LowDebug) :
                                self.msgText("Doing change retry: " + str(tryCount))
                            continue
                        else :
                            self.doHalt()
                else :
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("We moved, signal or aspect changed.")
                    self.findNewSpeed(self.currentBlock, self.next1Block, watchSignal)
            else :
                if (self.debugLevel >= MediumDebug) :
                    self.msgText("nothing changed to effect speed")
            # this is for display updates
            if (nearSignal != None) :
                self.signalNext.setIcon(self.cvtAppearanceIcon(nearSignal))
                self.signalNextText.text = self.giveSignalName(nearSignal)
            else :
                self.signalNext.setIcon(None)
                self.signalNextText.text = ""
            if (next1Signal != None) :
                self.signalBeyond.setIcon(self.cvtAppearanceIcon(next1Signal))
                self.signalBeyondText.text = self.giveSignalName(next1Signal)
                self.next1SignalAspect = next1Signal.getAppearance()
            else :
                self.signalBeyond.setIcon(None)
                self.signalBeyondText.text = ""
            if (next2Signal != None) :
                self.next2SignalAspect = next2Signal.getAppearance()
            self.nearSignal = nearSignal
            self.next1Signal = next1Signal
            self.next2Signal = next2Signal
            # we passed normal, so don't repeat
            tryCount = tryCountLimit
        # if we have a stop block
        if (self.stopBlock != None and self.currentBlock == self.stopBlock) :
            if (self.currentThrottle.getSpeedSetting() == 0) :
                if (self.debugLevel >= LowDebug) :
                    self.msgText("Found stop block, doing stop.")
                self.doStop()
        self.isStarting = False
        self.releaseExtraListeners()
        #self.msgText("didMove: done")
        return
        
    # check lists of block and signal listeners and release ones not needed
    def releaseExtraListeners(self) :
        numBlocksL = len(self.listenerBlockListeners)
        numBlocks = len(self.listenerBlocks)
        #self.msgText("start with Blocks: " + str(numBlocks) + " Listeners: " + str(numBlocksL))
        if (numBlocksL != numBlocks or numBlocksL <= 0 or numBlocks <= 0) :
            # blocks out of sync, stop and take it from the top
            if (self.debugLevel >= LowDebug) :
                self.msgText("Block lists out of sync! Blocks: " + str(numBlocks) + " Listeners: " + str(numBlocksL))
            self.doHalt()
            self.releaseBlockListParts()
        else :
            while (numBlocks > 0) :
                numBlocks = numBlocks - 1
                testBlock = self.listenerBlocks[numBlocks]
                #self.msgText("testing block: " + self.giveBlockName(testBlock))
                if (testBlock != None) :
                    if (testBlock != self.currentBlock) :
                        if (testBlock != self.next1Block) :
                            if (testBlock != self.next2Block) :
                                #self.msgText("we don't need block: " + self.giveBlockName(testBlock))
                                l = self.listenerBlockListeners[numBlocks]
                                testBlock.removePropertyChangeListener(l)
                                self.listenerBlockListeners.pop(numBlocks)
                                self.listenerBlocks.pop(numBlocks)
        numSignals = len(self.listenerSignals)
        numSignalsL = len(self.listenerSignalListeners)
        #self.msgText("starting Signals: " + str(numSignals) + " Listeners:" + str(numSignalsL))
        if (numSignals != numSignalsL or numSignalsL <= 0 or numSignals <= 0) :
            if (self.debugLevel >= LowDebug) :
                self.msgText("Signal lists out of sync! Signals: " + str(numSignals) + " Listeners:" + str(numSignalsL))
            self.doHalt()
            self.releaseSignalListParts()
        else :
            while (numSignals > 0) :
                numSignals = numSignals - 1
                testSignal = self.listenerSignals[numSignals]
                #self.msgText("releaseExtraListeners() testing signal: " + self.giveSignalName(testSignal))
                if (testSignal != self.currentSignal and testSignal != self.nearSignal and testSignal != self.next1Signal and testSignal != self.next2Signal) :
                    if (self.debugLevel >= MediumDebug) :
                        self.msgText("releaseExtraListeners() releasing: " + self.giveSignalName(testSignal))
                    l = self.listenerSignalListeners[numSignals]
                    testSignal.removePropertyChangeListener(l)
                    self.listenerSignalListeners.pop(numSignals)
                    self.listenerSignals.pop(numSignals)
                    #self.msgText("num signalListeners: " + str(len(self.listenerSignalListeners)) + " " + str(len(self.listenerSignals)))
        return
        
    #  return true if thing is in thingList
    def isInList(self, thing, thingList) :
        found = False
        for b in thingList :
            if (b == thing) :
                found = True
        return found

    # see if block is in the listenerBlocks, add listener if not
    def testAddBlockListener(self, bk) :
        if (self.isInList(bk, self.listenerBlocks) == False) :
            # isn't in list, setup listener and add to list
            bl = self.BlockListener()
            bl.setCallBack(self.callBackForDidWeMove)
            bk.addPropertyChangeListener(bl)
            self.listenerBlocks.append(bk)
            self.listenerBlockListeners.append(bl)
        return

    # see if signal is in the listenerSignals, add listener if not
    def testAddSignalListener(self, sig) :
        if (self.isInList(sig, self.listenerSignals) == False) :
            # isn't in list, setup listener and add to list
            sl = self.SignalListener()
            sl.setCallBack(self.callBackForDidWeMove)
            sig.addPropertyChangeListener(sl)
            self.listenerSignals.append(sig)
            self.listenerSignalListeners.append(sl)
            if (self.debugLevel >= MediumDebug) :
                self.msgText("testAddSignalListener(" + self.giveSignalName(sig) + ")")
        return
    
    # release signal listeners and clean lists
    def releaseSignalListParts(self) :
        while(len(self.listenerSignals) > 0) :
            s = self.listenerSignals.pop(0)
            l = self.listenerSignalListeners.pop(0)
            if (self.debugLevel >= MediumDebug) :
                self.msgText("releasing listener for signal " + self.giveSignalName(s))
            s.removePropertyChangeListener(l)
        return

    # release block listeners and clean lists
    def releaseBlockListParts(self) :
        while(len(self.listenerBlocks) > 0) :
            b = self.listenerBlocks.pop(0)
            l = self.listenerBlockListeners.pop(0)
            #self.msgText("releasing listener for block " + self.giveBlockName(b))
            b.removePropertyChangeListener(l)
        return
    
    # release all listeners, part of the exit cleanup
    def releaseAllListeners(self, event) :
        self.releaseSignalListParts()
        self.releaseBlockListParts()
        if (self.speedChangeTimer != None) :
            for i in self.speedChangeTimer.getActionListeners() :
                self.speedChangeTimer.removeActionListener(i)
        if (self.hornDelayTimer != None) :
            for i in self.hornDelayTimer.getActionListeners() :
                self.hornDelayTimer.removeActionListener(i)
        if (self.currentThrottle != None) :
            #self.msgText("releasing throttle")
            self.currentThrottle.setSpeedSetting(0)
            self.currentThrottle.release(None)
        self.isAborting = True
        return

    # take list of new current blocks, a current block, and a current direction
    # return new current block at edge of current blocks
    def findNewCurrentBlock(self, cBlock, cList, cDir) :
        nBlock = None
        dMask = jmri.Path.EAST | jmri.Path.WEST
        if (self.debugLevel >= HighDebug) :
            self.msgText("dmask: " + str(dMask) + " orig cDir: " + str(cDir))
        cDir = cDir & dMask  # only looking for east/west
        if (self.debugLevel >= HighDebug) :
            self.msgText(" filtered cDir: " + str(cDir) + " currentDirection: " + str(self.currentDirection))
        if (cDir == jmri.Path.NONE) :
            if (self.blockDirection.isSelected() == True) :
                cDir = jmri.Path.EAST
            else :
                cDir = jmri.Path.WEST
        if (cBlock == None) :
            if (self.debugLevel >= LowDebug) :
                self.msgText("findNewCurrentBlock: bad current block passed!")
            return None
        if (len(cList) <= 0) :
            if (self.debugLevel >= LowDebug) :
                self.msgText("findNewCurrentBlock: empty cList")
        else :
            pList = cBlock.getPaths()
            for p in pList :
                pB = p.getBlock()
                if (p.checkPathSet()) :
                    dirTest = p.getToBlockDirection() & dMask
                    if (self.debugLevel >= MediumDebug) :
                        self.msgText("findNewCurrentBlock: testing for " + jmri.Path.decodeDirection(cDir) + " from " + self.giveBlockName(cBlock) + " to " + self.giveBlockName(pB) + " pointing " + jmri.Path.decodeDirection(dirTest))
                    if ((cDir & dirTest) == cDir) :
                        for c in cList :
                            if (c == pB) :
                                nBlock = pB
                                if (self.debugLevel >= MediumDebug) :
                                    self.msgText("findNewCurrentBlock found " + self.giveBlockName(pB))
                                break
                            else :
                                if (self.debugLevel >= MediumDebug) :
                                    self.msgText("findNewCurrentBlock not in cList: " + self.giveBlockName(c))
                    if (nBlock != None) :
                        break
                else :
                    if (self.debugLevel >= MediumDebug) :
                        self.msgText("findNewCurrentBlock path not traversable: " + self.giveBlockName(cBlock) + " to " + self.giveBlockName(pB))
        return nBlock

    # figure out signal names and decide speeds
    def findNewSpeed(self, cBlock, nBlock, wSig) :
        if (self.isRunning) :
            if (cBlock == None) :
                if (nBlock == None) :
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("Failed to find either blocks")
                    self.doHalt()
                else :
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("Failed to find current block")
                    self.doHalt()
            else :
                if (nBlock == None) :
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("next block doesn't exist, treating as red.")
                    self.speedFromAppearance(RED)
                    self.priorSignal = self.currentSignal
                    self.priorSignalAspect = self.currentSignalAspect
                    self.currentSignal = None
                    self.currentSignalAspect = RED
                    self.next1Signal = None
                    self.next1SignalAspect = RED
                else :
                    useAspect = RED
                    if (wSig != None) :
                        if (self.debugLevel >= LowDebug) :
                            self.msgText("Found currentSignal: " + self.giveSignalName(wSig) + " displaying: " + self.cvtAppearanceText(wSig))
                        useAspect = wSig.getAppearance()
                        if (self.useStopFromDistance.isSelected() == True) :
                            stopDistAspect = self.speedFromStopDistance()
                            signalRank = self.rankSignalAspect(signalAspect)
                            stopDistRank = self.rankSignalAspect(stopDistAspect)
                            if (stopDistRank >= signalRank) :
                                useAspect = signalAspect
                            else :
                                useAspect = stopDistAspect
                        self.speedFromAppearance(useAspect)
                        self.priorSignal = self.currentSignal
                        self.priorSignalAspect = self.currentSignalAspect
                        self.currentSignal = wSig
                        self.currentSignalAspect = wSig.getAppearance()
                    else :
                        self.msgText("Failed finding signal!")
                        self.doHalt()
        return
    
    # return rate from signal aspect
    def rateFromSignalAspect(self, sigAspect) :
        ret = 0
        txt = "UNKNOWN"
        try :
            if (sigAspect & RED != 0) :
                txt = "RED"
                ret = float(self.locoRateRed.text)
            elif (sigAspect & FLASHRED != 0) :
                txt = "FLASHRED"
                ret = float(self.locoRateRedFlash.text)
            elif (sigAspect & YELLOW != 0) :
                txt = "YELLOW"
                ret = float(self.locoRateYellow.text)
            elif (sigAspect & FLASHYELLOW != 0) :
                txt = "FLASHYELLOW"
                ret = float(self.locoRateYellowFlash.text)
            elif (sigAspect & GREEN != 0) :
                txt = "GREEN"
                ret = float(self.locoRateGreen.text)
            elif (sigAspect & FLASHGREEN != 0) :
                txt = "FLASHGREEN"
                ret = float(self.locoRateGreenFlash.text)
        except ValueError :
            if (self.debugLevel >= LowDebug) :
                self.msgText("rateFromSignalAspect: no value for aspect " + txt)
        if (self.debugLevel >= MediumDebug) :
            self.msgText("rateFromSignalAspect(" + self.textSignalAspect(sigAspect) + "): " + str(ret))
        return ret
        
    # convert signal appearance to a ranked value
    def rankSignalAspect(self, sigAspect) :
        ret = 0
        if (sigAspect & RED != 0) :
            ret = 1
        elif (sigAspect & FLASHRED != 0) :
            ret = 2
        elif (sigAspect & YELLOW != 0) :
            ret = 3
        elif (sigAspect & FLASHYELLOW != 0) :
            ret = 4
        elif (sigAspect & GREEN != 0) :
            ret = 5
        elif (sigAspect & FLASHGREEN != 0) :
            ret = 6
        return ret
        
    # return speed change distance sum for rank change
    def rankSpeedChangeDistance(self, rankOld, rankNew) :
        ret = 0
        if (rankOld >= 1 and rankNew <= 1) :
            ret = ret + float(self.locoDistRed.text)
        if (rankOld >= 2 and rankNew <= 2) :
            ret = ret + float(self.locoDistRedFlash.text)
        if (rankOld >= 3 and rankNew <= 3) :
            ret = ret + float(self.locoDistYellow.text)
        if (rankOld >= 4 and rankNew <= 4) :
            ret = ret + float(self.locoDistYellowFlash.text)
        if (rankOld >= 5 and rankNew <= 5) :
            ret = ret + float(self.locoDistGreen.text)
        if (rankOld >= 6 and rankNew <= 6) :
            ret = ret + float(self.locoDistGreenFlash.text)
        if (self.debugLevel >= MediumDebug) :
            self.msgText("rankSpeedChangeDistance(" + str(rankOld) + ", " + str(rankNew) + "): " + str(ret))
        return ret
            
    # convert signal appearance to text
    def textSignalAspect(self, sigAspect) :
        ret = "???"
        if (sigAspect == None) :
            ret = "None"
        elif (sigAspect & RED != 0) :
            ret = "RED"
        elif (sigAspect & FLASHRED != 0) :
            ret = "FLASHRED"
        elif (sigAspect & YELLOW != 0) :
            ret = "YELLOW"
        elif (sigAspect & FLASHYELLOW != 0) :
            ret = "FLASHYELLOW"
        elif (sigAspect & GREEN != 0) :
            ret = "GREEN"
        elif (sigAspect & FLASHGREEN != 0) :
            ret = "FLASHGREEN"
        return ret
        
    # compare two signal appearances
    def compareSignalAspects(self, oldSigState, newSigState) :
        ret = "0"
        if (newSigState == None) :
            # this is wrong
            if (self.debugLevel >= LowDebug) :
                self.msgText("compare signals got a None for new signal state")
            self.doHalt()
            return
        if (oldSigState == None) :
            # startup case
            ret = 1
        else :
            newSigValue = self.rankSignalAspect(newSigState)
            oldSigValue = self.rankSignalAspect(oldSigState)
            #self.msgText("compareSignalAspects: " + self.textSignalAspect(oldSigState) + " went " + self.textSignalAspect(newSigState))
            if (newSigValue < oldSigValue) :
                ret = -1
            elif (newSigValue > oldSigValue) :
                ret = 1
        return ret
        
    # set speed from signal appearance. update stopDistanceForSpeed with distance and return aspect to match.
    def speedFromStopDistance(self) :
        distLimit = float(self.stopDistance.text)
        speedAspect = RED
        stopDistance = 0
        next = float(self.locoDistRed.text)
        distLimit = distLimit - next
        if (distLimit >= 0) :
            stopDistance = stopDistance + next
            speedAspect = RED
        next = float(self.locoDistRedFlash.text)
        distLimit = distLimit - next
        if (distLimit >= 0) :
            stopDistance = stopDistance + next
            speedAspect = FLASHRED
        next = float(self.locoDistYellow.text)
        distLimit = distLimit - next
        if (distLimit >= 0) :
            stopDistance = stopDistance + next
            speedAspect = YELLOW
        next = float(self.locoDistYellowFlash.text)
        distLimit = distLimit - next
        if (distLimit >= 0) :
            stopDistance = stopDistance + next
            speedAspect = FLASHYELLOW
        next = float(self.locoDistGreen.text)
        distLimit = distLimit - next
        if (distLimit >= 0) :
            stopDistance = stopDistance + next
            speedAspect = GREEN
        next = float(self.locoDistGreenFlash.text)
        distLimit = distLimit - next
        if (distLimit >= 0) :
            stopDistance = stopDistance + next
            speedAspect = FLASHGREEN
        if (self.debugLevel >= MediumDebug) :
            self.msgText("speedFromStopDistance() " + str(stopDistance) + " " + self.cvtAspectToText(speedAspect))
        self.stopDistanceForSpeed.text = str(stopDistance)
        return speedAspect
        
    # set speed from signal appearance
    def speedFromAppearance(self, sigState) :
        rep = ""
        if (sigState == RED or self.currentBlock == self.stopBlock) :
            rep = rep + "doRed "
            self.doSpeedRed()
        elif (sigState == FLASHRED) :
            rep = rep + "doRedFlash "
            self.doSpeedRedFlash()
        elif (sigState == YELLOW or self.next1Block == self.stopBlock) :
            rep = rep + "doYellow "
            self.doSpeedYellow()
        elif (sigState == FLASHYELLOW) :
            rep = rep + "doYellowFlash "
            self.doSpeedYellowFlash()
        elif (sigState == GREEN) :
            rep = rep + "doGreen "
            self.doSpeedGreen()
        elif (sigState == FLASHGREEN) :
            rep = rep + "doGreenFlash "
            self.doSpeedGreenFlash()
        else :
            rep = rep + "unknown "
            self.msgText("speedFromAppearance, unknown value! " + str(sigState))
            self.doHalt()
        #self.msgText("speedFromAppearance: " + self.giveSignalName(sig) + " displaying: " + self.cvtAppearanceText(sig) + " so we did: " + rep)
        return
        
    # convert signal appearance to english
    def cvtAppearanceText(self, sig) :
        rep = ""
        if (sig.getHeld()) :
            rep = rep + "Held "
        if (sig.getLit()) :
            rep = rep + "Lit "
        rep = rep + self.cvtAspectToText(sig.getAppearance())
        #self.msgText("cvtAppearanceText: " + self.giveSignalName(sig) + " displaying: " + rep)
        return rep
        
    # convert signal appearance to english
    def cvtAspectToText(self, sigState) :
        rep = ""
        if (sigState == RED) :
            rep = rep + "Red "
        elif (sigState == FLASHRED) :
            rep = rep + "Flashing Red "
        elif (sigState == YELLOW) :
            rep = rep + "Yellow "
        elif (sigState == FLASHYELLOW) :
            rep = rep + "Flashing Yellow "
        elif (sigState == GREEN) :
            rep = rep + "Green "
        elif (sigState == FLASHGREEN) :
            rep = rep + "Flashing Green "
        elif (sigState == DARK) :
            rep = rep + "Dark "
        else :
            rep = rep + "Unknown "
        #self.msgText("cvtAppearanceText: " + self.giveSignalName(sig) + " displaying: " + rep)
        return rep
        
    # convert signal appearance to icon
    def cvtAppearanceIcon(self, sig) :
        rep = self.darkSignalIcon
        if (sig.getLit()) :
            sigState = sig.getAppearance()
            if (sigState == RED) :
                rep = self.redSignalIcon
            elif (sigState == FLASHRED) :
                rep = self.redFlashSignalIcon
            elif (sigState == YELLOW) :
                rep = self.yellowSignalIcon
            elif (sigState == FLASHYELLOW) :
                rep = self.yellowFlashSignalIcon
            elif (sigState == GREEN) :
                rep = self.greenSignalIcon
            elif (sigState == FLASHGREEN) :
                rep = self.greenFlashSignalIcon
            else :
                rep = self.unknownSignalIcon
        #self.msgText("cvtAppearanceIcon: " + self.giveSignalName(sig) + " displaying: " + rep)
        return rep
        
    # compare two lists, reply true or false
    def compareLists(self, aList, bList) :
        if (self.debugLevel >= HighDebug) :
            self.msgText("comparing lists")
        doesMatchA = True
        doesMatchB = True
        for a in aList :
            try :
                i = bList.index(a)
            except :
                doesMatchA = False
        if (doesMatchA) :
            if (self.debugLevel >= HighDebug) :
                self.msgText("comparing lists: all of a in b")
        for b in bList :
            try :
                i = aList.index(b)
            except :
                doesMatchB = False
        if (doesMatchB) :
            if (self.debugLevel >= HighDebug) :
                self.msgText("comparing lists: all of b in a")
        return doesMatchA and doesMatchB
    
    def doSpeedGreenFlash(self):
        if (self.speedChangeTimer != None) :
            self.speedChangeTimer.stop()
        if (self.currentThrottle != None) :
            i = 0
            try :
                i = int(self.locoSpeedGreenFlash.text) * 0.01
            except :
                if (self.debugLevel >= NoneDebug) :
                    self.msgText("doSpeedGreenFlash: Invalid value! " + self.locoSpeedGreenFlash.text)
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedGreenFlash: " + str(i))
            self.locoSpeed.text = str(100 * i)
        return
        
    def doSpeedGreen(self):
        if (self.speedChangeTimer != None) :
            self.speedChangeTimer.stop()
        if (self.currentThrottle != None) :
            i = 0
            try :
                i = int(self.locoSpeedGreen.text) * 0.01
            except :
                if (self.debugLevel >= NoneDebug) :
                    self.msgText("doSpeedGreen: Invalid value! " + self.locoSpeedGreen.text)
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedGreen: " + str(i))
            self.locoSpeed.text = str(100 * i)
        return
        
    def doSpeedYellowFlash(self):
        if (self.speedChangeTimer != None) :
            self.speedChangeTimer.stop()
        if (self.currentThrottle != None) :
            i = 0
            try :
                i = int(self.locoSpeedYellowFlash.text) * 0.01
            except :
                if (self.debugLevel >= NoneDebug) :
                    self.msgText("doSpeedYellowFlash: Invalid value! " + self.locoSpeedYellowFlash.text)
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedYellowFlash: " + str(i))
            self.locoSpeed.text = str(100 * i)
        return
        
    def doSpeedYellow(self):
        if (self.speedChangeTimer != None) :
            self.speedChangeTimer.stop()
        if (self.currentThrottle != None) :
            doYellowNow = True
            if (self.movementDetected) :
                # compute how long to delay speed change
                dist = self.currentBlock.getLengthIn()
                rate = self.rateFromSignalAspect(FLASHGREEN)
                speedChgDist = self.rankSpeedChangeDistance(self.rankSignalAspect(FLASHGREEN), self.rankSignalAspect(FLASHYELLOW))
                if (self.priorSignalAspect != None) :
                    rate = self.rateFromSignalAspect(self.priorSignalAspect)
                    speedChgDist = self.rankSpeedChangeDistance(self.rankSignalAspect(self.priorSignalAspect), self.rankSignalAspect(FLASHYELLOW))
                if (dist > 0 and rate > 0) :
                    # the delay distance is the reserved space plus 10% from far end of block
                    # the less one covers the delay of the handle() routine
                    delay = (((dist* 0.90) - speedChgDist) / rate ) - 1
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("doSpeedYellow: dist: " + str(dist) + " rate: " + str(rate) + " speedChgDist: " + str(speedChgDist) + " delay: " + str(delay))
                    if (delay > 1) :
                        currentDelay = 0
                        if (self.speedChangeTimer == None) :
                            if (self.speedChangeListener == None) :
                                self.speedChangeListener = self.SpeedChangeTimeoutReceiver()
                            self.speedChangeTimer = javax.swing.Timer(int(delay * 0), self.speedChangeListener)
                            self.speedChangeTimer.setInitialDelay(int(delay * 1000))
                            self.speedChangeTimer.setRepeats(False);
                        self.speedChangeListener.setCallBack(self.yellowDelayHandler)
                        self.speedChangeTimer.setInitialDelay(int(delay * 1000))
                        self.speedChangeTimer.start()
                        doYellowNow = False
                    else :
                        if (self.debugLevel >= LowDebug) :
                            self.msgText("yellow delay less that 1 second")
            if (doYellowNow) :
                i = 0
                try :
                    i = int(self.locoSpeedYellow.text) * 0.01
                except :
                    if (self.debugLevel >= NoneDebug) :
                        self.msgText("doSpeedYellow: Invalid value! " + self.locoSpeedYellow.text)
                self.currentThrottle.setSpeedSetting(i)
                if (self.debugLevel >= LowDebug) :
                    self.msgText("doSpeedYellow: " + str(i))
                self.locoSpeed.text = str(100 * i)
        return
        
    def doSpeedRedFlash(self):
        if (self.speedChangeTimer != None) :
            self.speedChangeTimer.stop()
        if (self.currentThrottle != None) :
            i = 0
            try :
                i = int(self.locoSpeedRedFlash.text) * 0.01
            except :
                if (self.debugLevel >= NoneDebug) :
                    self.msgText("doSpeedRedFlash: Invalid value! " + self.locoSpeedRedFlash.text)
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedRedFlash: " + str(i))
            self.locoSpeed.text = str(100 * i)
        return
        
    def doSpeedRed(self):
        if (self.speedChangeTimer != None) :
            self.speedChangeTimer.stop()
        if (self.currentThrottle != None and self.currentThrottle.getSpeedSetting() != 0) :
            i = 0
            try :
                i = int(self.locoSpeedRed.text) * 0.01
            except :
                if (self.debugLevel >= NoneDebug) :
                    self.msgText("doSpeedRed: Invalid value! " + self.locoSpeedRed.text)
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedRed: " + str(i))
            self.locoSpeed.text = str(100 * i)
            # compute how long to delay stopping
            dist = self.currentBlock.getLengthIn()
            rate = float(self.locoRateRed.text)
            stopDist = float(self.locoDistRed.text)
            if (self.priorSignalAspect != None) :
                stopDist = self.rankSpeedChangeDistance(self.rankSignalAspect(self.priorSignalAspect), self.rankSignalAspect(RED))
            if (self.movementDetected == True and dist != 0 and rate != 0) :
                # the stop distance is the reserved space plus 10% from far end of block
                # the less one covers the delay of the handle() routine
                delay = ((dist - stopDist) / rate * 0.90) - 1
                if (self.debugLevel >= LowDebug) :
                    self.msgText("doSpeedRed: dist: " + str(dist) + " rate: " + str(rate) + " stopDist: " + str(stopDist) + " delay: " + str(delay))
                if (delay > 1) :
                    currentDelay = 0
                    if (self.speedChangeTimer == None) :
                        if (self.speedChangeListener == None) :
                            self.speedChangeListener = self.SpeedChangeTimeoutReceiver()
                        self.speedChangeTimer = javax.swing.Timer(int(delay * 0), self.speedChangeListener)
                        self.speedChangeTimer.setInitialDelay(int(delay * 1000))
                        self.speedChangeTimer.setRepeats(False);
                    self.speedChangeListener.setCallBack(self.redDelayHandler)
                    self.speedChangeTimer.setInitialDelay(int(delay * 1000))
                    self.speedChangeTimer.start()
                else :
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("stop delay less that 1 second")
                    self.doStop()
            else :
                self.doStop()
        return
        
    # handle the timeout for slowing to yellow
    def yellowDelayHandler(self, event) :
        if (self.debugLevel >= MediumDebug) :
                self.msgText("yellowDelayHandler: slow to yellow now!")
        self.speedChangeTimer.stop()
        if (self.currentThrottle != None) :
            i = int(self.locoSpeedYellow.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= MediumDebug) :
                self.msgText("yellowDelayHandler: " + str(i))
            self.locoSpeed.text = self.locoSpeedYellow.text
        return
        
    # handle the timeout for stopping on red
    def redDelayHandler(self, event) :
        if (self.debugLevel >= MediumDebug) :
            self.msgText("redDelayHandler, stopping now!")
        self.speedChangeTimer.stop()
        self.doStop()
        return
        
    # stopping for normal issues, allows for restarting automaticly
    def doStop(self):
        if (self.speedChangeTimer != None) :
            self.speedChangeTimer.stop()
        if (self.currentThrottle != None) :
            self.currentThrottle.setSpeedSetting(0)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doStop")
            self.locoSpeed.text = "0"
            if (self.currentBlock != None) :
                self.blockStart.text = self.giveBlockName(self.currentBlock)
        if (self.stopBlock != None and self.currentBlock == self.stopBlock) :
            self.handleHalting()
        return
               
    # doHalt is for stopping due to error conditions, won't restart
    def doHalt(self) :
        if (self.currentThrottle != None) :
            self.currentThrottle.setSpeedSetting(-1)
            self.msgText("doHalt, something was in error!!")
            self.locoSpeed.text = "0"
            if (self.currentBlock != None) :
                self.blockStart.text = self.giveBlockName(self.currentBlock)
        self.handleHalting()
        self.msgText("*** Run halted ***")
        return
        
    # deal with the buttons and stuff when we wait for humans
    def handleHalting(self) :
        self.stopButton.setEnabled(False)
        self.haltButton.setEnabled(False)
        self.startButton.setEnabled(True)
        self.isRunning = False
        return
        
    # process the stop block
    def whenStopBlockChanged(self, event) :
        self.blockStop.text = self.blockStop.text.strip()
        if (self.blockStop.text == "") :
            self.stopBlock = None
        else :
            self.stopBlock = blocks.getBlock(self.blockStop.text)
        return

    # validate loco values
    def whenLocoFieldChanged(self, event) :
        isBad = False
        ptr = event.getSource()
        msg = "whenLocoFieldChanged: " + ptr.getName() + " - "
        try :
            i = int(ptr.text)
            if (i < 0 or i > 9999) :
                msg = msg + "Loco id out of range: " + ptr.text
            ptr.text = str(i)
            msg = msg + ptr.text
        except :
            isBad = True
            msg = msg + "Invalid loco value: " + ptr.text
            ptr.text = ""
        if (isBad) or (self.debugLevel >= MediumDebug) :
            self.msgText(msg)
        return
        
    # validate speed values
    def whenSpeedFieldChanged(self, event) :
        isBad = False
        ptr = event.getSource()
        msg = "whenSpeedFieldChanged: " + ptr.getName() + " - "
        i = 0
        try :
            i = int(ptr.text)
            ptr.text = str(i)
            msg = msg + ptr.text
        except ValueError :
            isBad = True
            msg = msg + "Invalid speed value: " + ptr.text
            ptr.text = ""
        if (i < 0) :
            isBad = True
            msg = msg + "Negitive value for speed: " + ptr.text
            ptr.text = ""
        if (i > 100) :
            isBad = True
            msg = msg + "Too large value for speed: " + ptr.text
            ptr.text = ""
        if (isBad) or (self.debugLevel >= MediumDebug) :
            self.msgText(msg)
        return
        
    # validate distance values
    def whenDistanceFieldChanged(self, event) :
        isBad = False
        ptr = event.getSource()
        msg = "whenDistanceFieldChanged: " + ptr.getName() + " - "
        f = 0
        try :
            f = float(round(float(ptr.text) * 100) / 100.00) 
            ptr.text = str(f)
            msg = msg + ptr.text
        except ValueError :
            isBad = True
            msg = msg + "Invalid distance value: " + ptr.text
            ptr.text = ""
        if (f < 0) :
            isBad = True
            msg = msg + "Negitive value for distance: " + ptr.text
            ptr.text = ""
        if (isBad) or (self.debugLevel >= MediumDebug) :
            self.msgText(msg)
        return
        
    # enable the button when OK
    def whenLocoChanged(self, event) : 
        # keep track of whether both fields have been changed
        if (self.isRunning) :
            self.doStop()
            if (self.debugLevel >= LowDebug) :
                self.msgText("whenLocoChanged: was running, now stopped")
        isOk = True
        startBlock = None
        self.locoAddress.text = self.locoAddress.text.strip()
        if (self.locoAddress.text == "") :
            isOk = False
        else :
            self.scriptFrame.setTitle("Run Loco " + self.locoAddress.text)
            self.setName("RT3: " + self.locoAddress.text)
            if (self.locoAddress.text != self.oldLocoAddress) :
                # clear old block assignments
                self.clearLocoFromBlocks(self.oldLocoAddress)
                self.oldLocoAddress = self.locoAddress.text
                if (self.loadFromRoster.isSelected() == True) :
                    # take the loco id and try looking up values in roster
                    if (self.rosterInstance == None) :
                        self.rosterInstance = jmri.jmrit.roster.Roster.getDefault()
                        if (self.debugLevel >= MediumDebug) :
                            self.msgText("got roster instance")
                    rosterEntries = self.rosterInstance.matchingList(None, None, self.locoAddress.text, None, None, None, None)
                    if (self.debugLevel >= MediumDebug) :
                        self.msgText("found " + str(rosterEntries.size()) + " entries matching |" + str(id) + "|")
                    for ent in rosterEntries :
                        if (self.debugLevel >= LowDebug) :
                            self.msgText("posible entries: " + ent.fileName)
                    if (rosterEntries.size() == 1) :
                        ent = rosterEntries.get(0)
                        if (self.debugLevel >= LowDebug) :
                            self.msgText("Reading roster: " + ent.fileName)
                        v = ent.getAttribute('RT_locoSpeedGreenFlash')
                        if (v != None and v.strip() != "") :
                            self.locoSpeedGreenFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoRateGreenFlash')
                        if (v != None and v.strip() != "") :
                            self.locoRateGreenFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoDistGreenFlash')
                        if (v != None and v.strip() != "") :
                            self.locoDistGreenFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoSpeedGreen')
                        if (v != None and v.strip() != "") :
                            self.locoSpeedGreen.text = v.strip()
                        v = ent.getAttribute('RT_locoRateGreen')
                        if (v != None and v.strip() != "") :
                            self.locoRateGreen.text = v.strip()
                        v = ent.getAttribute('RT_locoDistGreen')
                        if (v != None and v.strip() != "") :
                            self.locoDistGreen.text = v.strip()
                        v = ent.getAttribute('RT_locoSpeedYellowFlash')
                        if (v != None and v.strip() != "") :
                            self.locoSpeedYellowFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoRateYellowFlash')
                        if (v != None and v.strip() != "") :
                            self.locoRateYellowFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoDistYellowFlash')
                        if (v != None and v.strip() != "") :
                            self.locoDistYellowFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoSpeedYellow')
                        if (v != None and v.strip() != "") :
                            self.locoSpeedYellow.text = v.strip()
                        v = ent.getAttribute('RT_locoRateYellow')
                        if (v != None and v.strip() != "") :
                            self.locoRateYellow.text = v.strip()
                        v = ent.getAttribute('RT_locoDistYellow')
                        if (v != None and v.strip() != "") :
                            self.locoDistYellow.text = v.strip()
                        v = ent.getAttribute('RT_locoSpeedRedFlash')
                        if (v != None and v.strip() != "") :
                            self.locoSpeedRedFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoRateRedFlash')
                        if (v != None and v.strip() != "") :
                            self.locoRateRedFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoDistRedFlash')
                        if (v != None and v.strip() != "") :
                            self.locoDistRedFlash.text = v.strip()
                        v = ent.getAttribute('RT_locoSpeedRed')
                        if (v != None and v.strip() != "") :
                            self.locoSpeedRed.text = v.strip()
                        v = ent.getAttribute('RT_locoRateRed')
                        if (v != None and v.strip() != "") :
                            self.locoRateRed.text = v.strip()
                        v = ent.getAttribute('RT_locoDistRed')
                        if (v != None and v.strip() != "") :
                            self.locoDistRed.text = v.strip()
                        self.locoLong.setSelected(ent.isLongAddress())
                        if (self.debugLevel >= LowDebug) :
                            self.msgText("Read completed: " + ent.fileName)
                self.oldLocoAddress = self.locoAddress.text
        if (self.locoSpeedRed.text == "") :
            isOk = False
        if (self.locoSpeedRedFlash.text == "") :
            isOk = False
        if (self.locoSpeedYellow.text == "") :
            isOk = False
        if (self.locoSpeedYellowFlash.text == "") :
            isOk = False
        if (self.locoSpeedGreen.text == "") :
            isOk = False
        if (self.locoSpeedGreenFlash.text == "") :
            isOk = False
        self.blockStart.text = self.blockStart.text.strip()
        if (self.blockStart.text == "") :
            isOk = False
        else :
            startBlock = blocks.getBlock(self.blockStart.text)
            if (self.testIfBlockNameValid(self.blockStart.text) == False) :
                if (self.debugLevel >= LowDebug) :
                    self.msgText("Invalid block name: " + self.blockStart.text + " please try again")
                isOk = False
            else:
                if (startBlock.getState() != ACTIVE) :
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("Block: " + self.blockStart.text + " is not occupied!")
                    isOk = False
        if (isOk) :
            # clear id from any existing blocks
            for b in blocks.getNamedBeanSet() :
                if (b != blocks.getBlock(self.blockStart.text) and b.getValue() == self.locoAddress.text) :
                    b.setValue("")
            if (self.blockDirection.isSelected()) :
                self.currentDirection = jmri.Path.EAST
            else :
                self.currentDirecion = jmri.Path.WEST
            self.startButton.setEnabled(True)
            self.haltButton.setEnabled(True)
            self.testAddBlockListener(blocks.getBlock(self.blockStart.text))
            if (self.debugLevel >= LowDebug) :
                self.msgText("Enabled Start")
        return
            
    # handle the Move Green button on
    def whenLocoMoveOnGreen(self, event) :
        self.doLocoMove(event, int(self.locoSpeedGreen.text) * 0.01)
        return

    # handle the Move Yellow button on
    def whenLocoMoveOnYellow(self, event) :
        self.doLocoMove(event, int(self.locoSpeedYellow.text) * 0.01)
        return

    # handle the Move Red button on
    def whenLocoMoveOnRed(self, event) :
        self.doLocoMove(event, int(self.locoSpeedRed.text) * 0.01)
        return

    # handle the Move button off
    def whenLocoMoveOff(self, event) :
        self.doLocoMove(event, 0)
        return

    def doLocoMove(self, event, state) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getSpeedSetting()
            self.currentThrottle.setSpeedSetting(state)
            if (self.debugLevel >= LowDebug) and (state != wasState) :
                self.msgText("changed speed to: " + str(state) + " was " + str(wasState))
        return
    
    # handle the horn button on
    def whenLocoHornOn(self, event) :
        self.doLocoHorn(event, True)
        return

    # handle the horn button off
    def whenLocoHornOff(self, event) :
        self.doLocoHorn(event, False)
        return

    def doLocoHorn(self, event, state) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getF2()
            self.currentThrottle.setF2(state)
            if (self.debugLevel >= HighDebug) :
                self.msgText("changed horn to: " + str(state) + " was " + str(wasState))
        return
    
    def doShortHorn(self) :
        self.doTimedHorn(1*1000)
        return
        
    def doLongHorn(self) :
        self.doTimedHorn(2*1000)
        return
        
    def doTimedHorn(self, delay) :
        if (self.currentThrottle != None) :
            self.currentThrottle.setF2(True)
            if (self.hornDelayTimer == None) :
                self.hornDelayListener = self.HornTimeoutReceiver()
                self.hornDelayListener.setCallBack(self.hornDelayHandler)
                self.hornDelayTimer = javax.swing.Timer(int(delay), self.hornDelayListener)
                self.hornDelayTimer.setInitialDelay(int(delay))
                self.hornDelayTimer.setRepeats(False);
            self.hornDelayTimer.setInitialDelay(int(delay))
            self.hornDelayTimer.start()
            if (self.debugLevel >= HighDebug) :
                self.msgText("Started Timed Horn")
        return
        
    def hornDelayHandler(self, event) :
        if (self.hornDelayTimer != None) :
            self.hornDelayTimer.stop()
        if (self.currentThrottle != None) :
            self.currentThrottle.setF2(False)
        if (self.debugLevel >= HighDebug) :
            self.msgText("Stopped Timed Horn")
        return
    
    # handle the Headlight button
    def whenLocoHeadlight(self, event) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getF0()
            state = self.locoHeadlight.isSelected()
            self.currentThrottle.setF0(state)
            if (self.debugLevel >= HighDebug) :
                self.msgText("changed light to: " + str(state) + " was " +str( wasState))
        return
    
    # handle the Bell button
    def whenLocoBell(self, event) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getF1()
            state = self.locoBell.isSelected()
            self.currentThrottle.setF1(state)
            if (self.debugLevel >= HighDebug) :
                self.msgText("changed bell to: " + str(state) + " was " + str(wasState))
        return
    
    # test for block name
    def testIfBlockNameValid(self, userName) :
        foundStart = False
        b = blocks.getByUserName(userName)
        if (b != None and self.giveBlockName(b) == userName) :
            foundStart = True
        return foundStart
        
    # define what button does when clicked and attach that routine to the button
    def whenStartButtonClicked(self, event) :
        self.msgText("Run started")     # add text
        if (self.testIfBlockNameValid(self.blockStart.text) == False) :
            if (self.debugLevel >= LowDebug) :
                self.msgText("Invalid block name: " + self.blockStart.text + " please try again")
        else :
            c = blocks.getBlock(self.blockStart.text)
            if (c == None) :
                if (self.debugLevel >= LowDebug) :
                    self.msgText("Invalid block name: " + self.blockStart.text + " please try again")
            else :
                # clear any prior block entries
                self.clearLocoFromBlocks(self.locoAddress.text)
                c.setValue(self.locoAddress.text)
                self.currentBlock = c
                self.priorSignal = None
                self.priorSignalAspect = None
                self.currentSignal = None
                self.currentSignalAspect = None
                self.next1Block = None
                self.next1Signal = None
                self.next1SignalAspect = None
                # set flags so things get done from handle() routine
                self.askChangeThrottle = True
                self.askFinishStartButton = True
        if (self.debugLevel >= MediumDebug) :
            self.msgText("whenStartButtonClicked, done")     # add text
        return
        
    # split out so it can happen from the handle() routine
    def doFinishStartButton(self) :
        if (self.debugLevel >= MediumDebug) :
            self.msgText("Change button states")     # add text
        self.stopButton.setEnabled(True)
        self.haltButton.setEnabled(True)
        self.startButton.setEnabled(False)
        self.isRunning = True
        self.isStarting = True
        self.currentBlocks = None
        self.priorBlocks = None
        if (self.blockDirection.isSelected() == True) :
            self.currentDirection = jmri.Path.EAST
            self.currentBlock.setDirection(jmri.Path.EAST)
        else :
            self.currentDirection = jmri.Path.WEST
            self.currentBlock.setDirection(jmri.Path.WEST)
        self.didWeMoveCounter = self.didWeMoveCounter + 1
        if (self.isRunning) :
            if (self.debugLevel >= LowDebug) :
                self.msgText("Starting current:" + self.giveBlockName(self.currentBlock))
        return
            
    def whenStopButtonClicked(self, event):   
        if (self.debugLevel >= MediumDebug) :
            self.msgText("Slow loco to stop")     # add text
        self.doStop()
        if (self.debugLevel >= LowDebug) :
            self.msgText("*** Run stopped ***")
        self.stopButton.setEnabled(False)
        self.handleHalting()
        self.whenLocoChanged(event)
        return
    
    def whenHaltButtonClicked(self, event):   
        if (self.debugLevel >= LowDebug) :
            self.msgText("Button Halt loco NOW!")     # add text
        self.doHalt()
        if (self.debugLevel >= LowDebug) :
            self.msgText("*** Run halted ***")
        self.handleHalting()
        self.whenLocoChanged(event)
        return
    
    def whenLocoDirectionButtonClicked(self, event) :
        if (self.debugLevel >= MediumDebug) :
            self.msgText("Button Loco Direction clicked")
        if (self.currentThrottle != None) :
            self.currentThrottle.setIsForward(self.locoForward.isSelected())
        return
        
    def whenBlockDirectionButtonClicked(self, event) :
        if (self.debugLevel >= MediumDebug) :
            self.msgText("Button Block Direction clicked")
        self.methodBlockDirection = self.blockDirection.isSelected()
        return
        
    def whenShrinkButtonClicked(self, event):   
        if (self.shrinkGrow == True) :
            if (self.debugLevel >= HighDebug) :
                self.msgText("Shrink Display!")     # add text
            self.speedPane.setVisible(False)
            self.shrinkGrow = False
            self.fullScrollRows = self.scrollArea.getRows()
            self.scrollArea.setRows(self.fullScrollRows / 2)
            self.scriptFrame.pack()
        else :
            if (self.debugLevel >= HighDebug) :
                self.msgText("Grow Display!")
            self.speedPane.setVisible(True)
            self.shrinkGrow = True
            self.scrollArea.setRows(self.fullScrollRows)
            self.scriptFrame.pack()
        return
    
    def whenSaveToRosterButtonClicked(self, event):   
        if (self.locoAddress.text != "") :
            if (self.rosterInstance == None) :
                self.rosterInstance = jmri.jmrit.roster.Roster.getDefault()
                if (self.debugLevel >= MediumDebug) :
                    self.msgText("got roster instance")
            id = int(self.locoAddress.text)
            rosterEntries = self.rosterInstance.matchingList(None, None, str(id), None, None, None, None)
            if (self.debugLevel >= HighDebug) :
                self.msgText("found " + str(rosterEntries.size()) + " entries matching |" + str(id) + "|")
            for ent in rosterEntries :
                if (self.debugLevel >= LowDebug) :
                    self.msgText("posible entries: " + ent.fileName)
            if (rosterEntries.size() == 1) :
                ent = rosterEntries.get(0)
                if (self.debugLevel >= LowDebug) :
                    self.msgText("Saving to roster: " + ent.fileName)
                ent.putAttribute('RT_locoSpeedGreenFlash', self.locoSpeedGreenFlash.text)
                ent.putAttribute('RT_locoRateGreenFlash', self.locoRateGreenFlash.text)
                ent.putAttribute('RT_locoDistGreenFlash', self.locoDistGreenFlash.text)
                ent.putAttribute('RT_locoSpeedGreen', self.locoSpeedGreen.text)
                ent.putAttribute('RT_locoRateGreen', self.locoRateGreen.text)
                ent.putAttribute('RT_locoDistGreen', self.locoDistGreen.text)
                ent.putAttribute('RT_locoSpeedYellowFlash', self.locoSpeedYellowFlash.text)
                ent.putAttribute('RT_locoRateYellowFlash', self.locoRateYellowFlash.text)
                ent.putAttribute('RT_locoDistYellowFlash', self.locoDistYellowFlash.text)
                ent.putAttribute('RT_locoSpeedYellow', self.locoSpeedYellow.text)
                ent.putAttribute('RT_locoRateYellow', self.locoRateYellow.text)
                ent.putAttribute('RT_locoDistYellow', self.locoDistYellow.text)
                ent.putAttribute('RT_locoSpeedRedFlash', self.locoSpeedRedFlash.text)
                ent.putAttribute('RT_locoRateRedFlash', self.locoRateRedFlash.text)
                ent.putAttribute('RT_locoDistRedFlash', self.locoDistRedFlash.text)
                ent.putAttribute('RT_locoSpeedRed', self.locoSpeedRed.text)
                ent.putAttribute('RT_locoRateRed', self.locoRateRed.text)
                ent.putAttribute('RT_locoDistRed', self.locoDistRed.text)
                ent.updateFile()
                self.rosterInstance.writeRosterFile()
                if (self.debugLevel >= LowDebug) :
                    self.msgText("Save completed: " + ent.fileName)
        return
    
    def findCurrentBlocks(self) :
        # search the block list for the matching loco
        blockList = []
        for b in blocks.getNamedBeanSet() :
            if (b.getValue() == self.locoAddress.text and b.getState() == ACTIVE) :
                blockList.append(b)
        return blockList

    def clearLocoFromBlocks(self, oldId) :
        # search the block list for the matching loco
        blockList = []
        blockArray = blocks.getSystemNameList().toArray()
        for x in blockArray :
            b = blocks.getBySystemName(x)
            if (b.getValue() == oldId) :
                b.setValue(None)
        return
        
    def findNextBlock(self, cB) :
        # look down list of getToBlockDirection for match
        # use 'suggestion' flag if current block doesn't have direction
        bTrav = None
        bNoTrav = None
        dirFlag = cB.getDirection()
        if (dirFlag == jmri.Path.NONE) :
            if (self.currentDirection == None) :
                if (self.blockDirection.isSelected() == True) :
                    dirFlag = jmri.Path.EAST
                    self.currentDirection = jmri.Path.EAST
                else :
                    dirFlag = jmri.Path.WEST
                    self.currentDirection = jmri.Path.WEST
            else :
                dirFlag = self.currentDirection
        pathList = cB.getPaths()
        if (self.debugLevel >= HighDebug) :
            self.msgText("searching " + str(len(pathList)) + " paths from " + self.giveBlockName(cB))
        for p in pathList :
            blockTest = p.getBlock()
            dirTest = p.getToBlockDirection()
            if (dirTest & dirFlag != 0) :
                if (p.checkPathSet()) :
                    if (self.debugLevel >= MediumDebug) :
                        self.msgText("findNextBlock path traversable: "  + self.giveBlockName(cB) + " to " + self.giveBlockName(blockTest) + " dirTest: " + jmri.Path.decodeDirection(dirTest) + ":" + str(dirTest) + " dirFlag: " + jmri.Path.decodeDirection(dirFlag) + ":" + str(dirFlag) + " result: " + str(dirTest & dirFlag))
                    bTrav = blockTest
                else :
                    if (self.debugLevel >= MediumDebug) :
                        self.msgText("findNextBlock path not traversable: " + self.giveBlockName(cB) + " to " + self.giveBlockName(blockTest))
                    bNoTrav = blockTest
                if (self.debugLevel >= LowDebug) :
                    self.msgText("findNextBlock Found " + self.giveBlockName(blockTest))
        if (bTrav == None) :
            return bNoTrav
        return bTrav
        
    # ActionListener - used for the stop timeout
    class SpeedChangeTimeoutReceiver(java.awt.event.ActionListener):
        cb = None

        def actionPerformed(self, event) :
            if (self.cb != None) :
                self.cb(event)
            return
        
        def setCallBack(self, cbf) :
            self.cb = cbf
            return

    # ActionListener - used for the horn timeout
    class HornTimeoutReceiver(java.awt.event.ActionListener):
        cb = None

        def actionPerformed(self, event) :
            if (self.cb != None) :
                self.cb(event)
            return
        
        def setCallBack(self, cbf) :
            self.cb = cbf
            return

    # WindowListener is a interface class and therefore all of it's
    # methods should be implemented even if not used to avoid AttributeErrors
    class WinListener(java.awt.event.WindowListener):
        f = None
        cleanUp = None

        def setCallBack(self, fr, c):
            self.f = fr
            self.cleanUp = c
            return
        
        def windowClosing(self, event):
            if (self.cleanUp != None) :
                self.cleanUp(event)
            self.f.dispose()         # close the pane (window)
            return
            
        def windowActivated(self,event):
            return

        def windowDeactivated(self,event):
            return

        def windowOpened(self,event):
            return

        def windowClosed(self,event):
            return
            
        def windowIconified(self, event):
            return
            
        def windowDeiconified(self, event):
            return
     
    #To detect block status, first define the listener. 
    class BlockListener(java.beans.PropertyChangeListener):
        cb = None

        def setCallBack(self, ptr) :
            self.cb = ptr
            return

        def propertyChange(self, event):
            if (self.cb != None) :
                self.cb(event)
            return
    
    #To detect block status, first define the listener. 
    class SignalListener(java.beans.PropertyChangeListener):
        cb = None

        def setCallBack(self, ptr) :
            self.cb = ptr
            return

        def propertyChange(self, event):
            if (self.cb != None) :
                self.cb(event)
            return

    # method for creating a TimeStamp.
    def formatTime(self, ts):
        ptHrs = str(ts.get(java.util.Calendar.HOUR_OF_DAY))
        if len(ptHrs) == 1 :
            ptHrs = "0" + ptHrs

        ptMins = str(ts.get(java.util.Calendar.MINUTE))
        if len(ptMins) == 1 :
            ptMins = "0" + ptMins

        ptSecs = str(ts.get(java.util.Calendar.SECOND))
        if len(ptSecs) == 1 :
            ptSecs = "0" + ptSecs
        ptMs = str(ts.get(java.util.Calendar.MILLISECOND))
        pTime = ptHrs + ":" + ptMins + ":" + ptSecs + "." + ptMs
        return pTime

    # method for creating a DateStamp.
    def formatDate(self, ts):
        ptDays = str(ts.get(java.util.Calendar.DATE))
        if len(ptDays) == 1 :
            ptDays = "0" + ptDays

        ptMonths = str(ts.get(java.util.Calendar.MONTH) + 1)
        if len(ptMonths) == 1 :
            ptMonths = "0" + ptMonths

        ptYears = str(ts.get(java.util.Calendar.YEAR))
        pDate = ptMonths + "/" + ptDays + "/" + ptYears
        return pDate

    # handle adding to message window
    def msgText(self, txt) :
        if (self.scrollTimeStamp.isSelected() == True) :
            ts = java.util.Calendar.getInstance()
            self.scrollArea.append(self.formatTime(ts) + ": " + txt + "\n")
        else :
            self.scrollArea.append(txt + "\n")
        if (self.autoScroll.isSelected() == True) :
            self.scrollArea.setCaretPosition(self.scrollArea.getDocument().getLength())
        return
    
    # setup the user interface
    def setup(self) :
         
        # get other setup things

        self.greenSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-green-short.gif", "GreenCabSignal")
        self.greenFlashSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-flashgreen-short.gif", "GreenFlashCabSignal")
        self.yellowSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-yellow-short.gif", "YellowCabSignal")
        self.yellowFlashSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-flashyellow-short.gif", "YellowFlashCabSignal")
        self.redSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-red-short.gif", "RedCabSignal")
        self.redFlashSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-flashred-short.gif", "RedFlashCabSignal")
        self.darkSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-dark-short.gif", "DarkCabSignal")
        self.unknownSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/misc/Question-black.gif", "UnknownCabSignal")
        self.throttleManager = jmri.InstanceManager.getDefault(jmri.ThrottleManager)
        if (self.throttleManager == None) :
            print("No command station found!!\nRT has no way to control the trains.\n")
            return
   
        # start to initialise the GUI
        sizeRateField = 4
        sizeSpeedField = 3
        
        # create buttons and define action
        self.startButton = javax.swing.JButton("Start")
        self.startButton.setEnabled(False)           # button starts as grayed out (disabled)
        self.startButton.actionPerformed = self.whenStartButtonClicked
        
        self.stopButton = javax.swing.JButton("Stop")
        self.stopButton.setEnabled(False)           # button starts as grayed out (disabled)
        self.stopButton.setToolTipText("Stops the run - there is a delay as the loco slows")
        self.stopButton.actionPerformed = self.whenStopButtonClicked
        
        self.haltButton = javax.swing.JButton("Halt")
        self.haltButton.setEnabled(False)           # button starts as grayed out (disabled)
        self.haltButton.setToolTipText("Emergency halt the run - should be an abrupt stop")
        self.haltButton.actionPerformed = self.whenHaltButtonClicked
        
        self.shrinkButton = javax.swing.JButton("Shrink")
        self.shrinkButton.setEnabled(True)           
        self.shrinkButton.setToolTipText("Shrink/Grow the window")
        self.shrinkButton.actionPerformed = self.whenShrinkButtonClicked
        
        self.testButton = javax.swing.JButton("Test")
        self.testButton.setEnabled(True)           # button starts as grayed out (disabled)
        self.testButton.setToolTipText("run the didWeMove test")
        self.testButton.actionPerformed = self.callBackForDidWeMove
        
        self.saveToRosterButton = javax.swing.JButton("Save Settings")
        self.saveToRosterButton.setEnabled(True)           
        self.saveToRosterButton.setToolTipText("Saves setting in roster, if found.")
        self.saveToRosterButton.actionPerformed = self.whenSaveToRosterButtonClicked
        
        # address of the loco
        self.locoAddress = javax.swing.JTextField(5)    # sized to hold 5 characters, initially empty
        self.locoAddress.actionPerformed = self.whenLocoChanged   # if user hit return or enter
        self.locoAddress.focusLost = self.whenLocoChanged         # if user tabs away
        self.locoAddress.requestFocusInWindow()
        
        # long/short address flag
        self.locoLong = javax.swing.JCheckBox()
        self.locoLong.setToolTipText("Check to use long address")
        self.locoLong.setSelected(True)
        if (self.throttleManager.addressTypeUnique() == True) :
            self.locoLong.setEnabled(False)
        self.locoLong.actionPerformed = self.whenLocoChanged
        self.locoLong.focusLost = self.whenLocoChanged
        
        # loco direction flag
        self.locoForward = javax.swing.JCheckBox()
        self.locoForward.setToolTipText("Sets forward loco direction")
        self.locoForward.setSelected(True)
        self.locoForward.actionPerformed = self.whenLocoDirectionButtonClicked
        self.locoForward.focusLost = self.whenLocoDirectionButtonClicked
        
        # loco headlight flag
        self.locoHeadlight = javax.swing.JCheckBox()
        self.locoHeadlight.setToolTipText("Controls loco hightlight")
        self.locoHeadlight.actionPerformed = self.whenLocoHeadlight
        self.locoHeadlight.focusLost = self.whenLocoHeadlight
        
        # loco bell flag
        self.locoBell = javax.swing.JCheckBox()
        self.locoBell.setToolTipText("Controls loco bell")
        self.locoBell.actionPerformed = self.whenLocoBell
        self.locoBell.focusLost = self.whenLocoBell
        
        # loco horn/whistle flag
        self.locoHorn = javax.swing.JButton("Horn")
        self.locoHorn.setToolTipText("Controls loco horn")
        self.locoHorn.mousePressed = self.whenLocoHornOn
        self.locoHorn.mouseReleased = self.whenLocoHornOff

        # loco move buttons
        self.locoMoveGreen = javax.swing.JButton("Move @ Clear")
        self.locoMoveGreen.setToolTipText("Moves loco at Clear speed")
        self.locoMoveGreen.mousePressed = self.whenLocoMoveOnGreen
        self.locoMoveGreen.mouseReleased = self.whenLocoMoveOff
        self.locoMoveYellow = javax.swing.JButton("Move @ Limited")
        self.locoMoveYellow.setToolTipText("Moves loco at Limited speed")
        self.locoMoveYellow.mousePressed = self.whenLocoMoveOnYellow
        self.locoMoveYellow.mouseReleased = self.whenLocoMoveOff
        self.locoMoveRed = javax.swing.JButton("Move @ Restricted")
        self.locoMoveRed.setToolTipText("Moves loco at Restricted speed")
        self.locoMoveRed.mousePressed = self.whenLocoMoveOnRed
        self.locoMoveRed.mouseReleased = self.whenLocoMoveOff

        # create the speed fields for a Green Flash Signal
        self.locoSpeedGreenFlash = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedGreenFlash.setToolTipText("Green Flash Speed is a number from 1 to 100%")
        self.locoSpeedGreenFlash.actionPerformed = self.whenSpeedFieldChanged
        self.locoSpeedGreenFlash.focusLost = self.whenSpeedFieldChanged
        self.locoSpeedGreenFlash.text = "70"
        self.locoSpeedGreenFlash.setName("locoSpeedGreenFlash")
        
        # create the physical speed field for a Green Flash Signal
        self.locoRateGreenFlash = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateGreenFlash.setToolTipText("Throttle as Distance/Second, approaching green flash signal")
        self.locoRateGreenFlash.actionPerformed = self.whenDistanceFieldChanged
        self.locoRateGreenFlash.focusLost = self.whenDistanceFieldChanged
        self.locoRateGreenFlash.text = "0"
        self.locoRateGreenFlash.setName("locoRateGreenFlash")
        
        # create the distance field for a Green Flash Signal
        self.locoDistGreenFlash = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoDistGreenFlash.setToolTipText("Distance to Green Speed from Green Flash signal speed, inches")
        self.locoDistGreenFlash.actionPerformed = self.whenDistanceFieldChanged
        self.locoDistGreenFlash.focusLost = self.whenDistanceFieldChanged
        self.locoDistGreenFlash.text = "6"
        self.locoDistGreenFlash.setName("locoDistGreenFlash")
        
        # create the speed fields for a Green Signal
        self.locoSpeedGreen = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedGreen.setToolTipText("Green Speed is a number from 1 to 100%")
        self.locoSpeedGreen.actionPerformed = self.whenSpeedFieldChanged
        self.locoSpeedGreen.focusLost = self.whenSpeedFieldChanged
        self.locoSpeedGreen.text = "45"
        self.locoSpeedGreen.setName("locoSpeedGreen")
        
        # create the physical speed field for a Green Signal
        self.locoRateGreen = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateGreen.setToolTipText("Throttle as Distance/Second, approaching Green signal")
        self.locoRateGreen.actionPerformed = self.whenDistanceFieldChanged
        self.locoRateGreen.focusLost = self.whenDistanceFieldChanged
        self.locoRateGreen.text = "9"
        self.locoRateGreen.setName("locoRateGreen")
        
        # create the distance field for a Green Signal
        self.locoDistGreen = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoDistGreen.setToolTipText("Distance to Yellow Flash Speed from Green signal speed, inches")
        self.locoDistGreen.actionPerformed = self.whenDistanceFieldChanged
        self.locoDistGreen.focusLost = self.whenDistanceFieldChanged
        self.locoDistGreen.text = "6"
        self.locoDistGreen.setName("locoDistGreen")
        
        # create the speed fields for a Yellow Flash Signal
        self.locoSpeedYellowFlash = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedYellowFlash.setToolTipText("Yellow Flash Speed is a number from 1 to 100%")
        self.locoSpeedYellowFlash.actionPerformed = self.whenSpeedFieldChanged
        self.locoSpeedYellowFlash.focusLost = self.whenSpeedFieldChanged
        self.locoSpeedYellowFlash.text = "45"
        self.locoSpeedYellowFlash.setName("locoSpeedYellowFlash")
        
        # create the physical speed field for a Yellow Flash Signal
        self.locoRateYellowFlash = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateYellowFlash.setToolTipText("Throttle as Distance/Second, approaching yello flash signal")
        self.locoRateYellowFlash.actionPerformed = self.whenDistanceFieldChanged
        self.locoRateYellowFlash.focusLost = self.whenDistanceFieldChanged
        self.locoRateYellowFlash.text = "0"
        self.locoRateYellowFlash.setName("locoRateYellowFlash")
        
        # create the distance field for a Yellow Flash Signal
        self.locoDistYellowFlash = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoDistYellowFlash.setToolTipText("Distance to Yellow Speed from Yellow Flash signal speed, inches")
        self.locoDistYellowFlash.actionPerformed = self.whenDistanceFieldChanged
        self.locoDistYellowFlash.focusLost = self.whenDistanceFieldChanged
        self.locoDistYellowFlash.text = "6"
        self.locoDistYellowFlash.setName("locoDistYellowFlash")
        
        # create the speed fields for a Yellow Signal
        self.locoSpeedYellow = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedYellow.setToolTipText("Yellow Speed is a number from 1 to 100%")
        self.locoSpeedYellow.actionPerformed = self.whenSpeedFieldChanged
        self.locoSpeedYellow.focusLost = self.whenSpeedFieldChanged
        self.locoSpeedYellow.text = "30"
        self.locoSpeedYellow.setName("locoSpeedYellow")
        
        # create the physical speed field for a Yellow Signal
        self.locoRateYellow = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateYellow.setToolTipText("Throttle as Distance/Second, approaching yellow signal")
        self.locoRateYellow.actionPerformed = self.whenDistanceFieldChanged
        self.locoRateYellow.focusLost = self.whenDistanceFieldChanged
        self.locoRateYellow.text = "6"
        self.locoRateYellow.setName("locoRateYellow")
        
        # create the distance field for a Yellow Signal
        self.locoDistYellow = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoDistYellow.setToolTipText("Distance to Red Flash Speed from Yellow signal speed, inches")
        self.locoDistYellow.actionPerformed = self.whenDistanceFieldChanged
        self.locoDistYellow.focusLost = self.whenDistanceFieldChanged
        self.locoDistYellow.text = "6"
        self.locoDistYellow.setName("locoDistYellow")
        
        # create the speed fields for a Red Flash Signal
        self.locoSpeedRedFlash = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedRedFlash.setToolTipText("Red Flash Speed is a number from 1 to 100%")
        self.locoSpeedRedFlash.actionPerformed = self.whenSpeedFieldChanged
        self.locoSpeedRedFlash.focusLost = self.whenSpeedFieldChanged
        self.locoSpeedRedFlash.text = "20"
        self.locoSpeedRedFlash.setName("locoSpeedRedFlash")
        
        # create the physical speed field for a Red Flash Signal
        self.locoRateRedFlash = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateRedFlash.setToolTipText("Throttle as Distance/Second, approaching red flash signal")
        self.locoRateRedFlash.actionPerformed = self.whenDistanceFieldChanged
        self.locoRateRedFlash.focusLost = self.whenDistanceFieldChanged
        self.locoRateRedFlash.text = "0"
        self.locoRateRedFlash.setName("locoRateRedFlash")
        
        # create the distance field for a Red Flash Signal
        self.locoDistRedFlash = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoDistRedFlash.setToolTipText("Distance to Red Speed from Red Flash signal speed, inches")
        self.locoDistRedFlash.actionPerformed = self.whenDistanceFieldChanged
        self.locoDistRedFlash.focusLost = self.whenDistanceFieldChanged
        self.locoDistRedFlash.text = "0"
        self.locoDistRedFlash.setName("locoDistRedFlash")
        
        # create the speed fields for a Red Signal
        self.locoSpeedRed = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedRed.setToolTipText("Red Speed is a number from 1 to 100%, creep to Red Signal")
        self.locoSpeedRed.actionPerformed = self.whenSpeedFieldChanged
        self.locoSpeedRed.focusLost = self.whenSpeedFieldChanged
        self.locoSpeedRed.text = "15"
        self.locoSpeedRed.setName("locoSpeedRed")
        
        # create the physical speed field for a Red Signal
        self.locoRateRed = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateRed.setToolTipText("Throttle as Distance/Second, approaching red signal")
        self.locoRateRed.actionPerformed = self.whenDistanceFieldChanged
        self.locoRateRed.focusLost = self.whenDistanceFieldChanged
        self.locoRateRed.text = "3"
        self.locoRateRed.setName("locoRateRed")
        
        # create the distance field for a Red Signal
        self.locoDistRed = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoDistRed.setToolTipText("Distance to stop from Red signal speed, inches")
        self.locoDistRed.actionPerformed = self.whenDistanceFieldChanged
        self.locoDistRed.focusLost = self.whenDistanceFieldChanged
        self.locoDistRed.text = "10"
        self.locoDistRed.setName("locoDistRed")
        
        # create current speed display
        self.locoSpeed = javax.swing.JLabel()
        self.locoSpeed.text = "0"
        
        # create the starting block field
        self.blockStart = javax.swing.JTextField(10)
        self.blockStart.setToolTipText("Starting Block Name")
        self.blockStart.actionPerformed = self.whenLocoChanged
        self.blockStart.focusLost = self.whenLocoChanged
        self.blockStart.setName("blockStart")
        
        # create the starting block direction
        self.blockDirection = javax.swing.JCheckBox()
        self.blockDirection.setToolTipText("Starting Block Direction")
        self.blockDirection.actionPerformed = self.whenBlockDirectionButtonClicked
        self.blockDirection.focusLost = self.whenBlockDirectionButtonClicked
        self.blockDirection.setSelected(True)
        
        # create flag for looking any extra block ahead
        self.blockAhead2 = javax.swing.JCheckBox()
        self.blockAhead2.setSelected(False)
        self.blockAhead2.setToolTipText("for 4 block mode")
        
        # create flag for enable lookahead stopping
        self.useStopFromDistance = javax.swing.JCheckBox()
        self.useStopFromDistance.setSelected(False)
        self.useStopFromDistance.setToolTipText("reduce current signal viewed due to distant stop")
        
        # create the stopping block field
        self.blockStop = javax.swing.JTextField(10)
        self.blockStop.setToolTipText("Stopping Block Name")
        self.blockStop.actionPerformed = self.whenStopBlockChanged
        self.blockStop.focusLost = self.whenStopBlockChanged
        
        # create the current block field
        self.blockNow = javax.swing.JLabel()
        self.blockNowLength = javax.swing.JLabel()

        # create the current/next signal field
        self.signalNext = javax.swing.JLabel()
        self.signalNextText = javax.swing.JLabel()
        
        # create the next block field
        self.blockNext = javax.swing.JLabel()
        self.blockNextLength = javax.swing.JLabel()
        
        # create the next/beyond signal field
        self.signalBeyond = javax.swing.JLabel()
        self.signalBeyondText = javax.swing.JLabel()
        
        # create the beyond block field
        self.blockBeyond = javax.swing.JLabel()
        self.blockBeyondLength = javax.swing.JLabel()
        
        # create the stop distance field
        self.stopDistance = javax.swing.JLabel()
        self.stopDistanceForSpeed = javax.swing.JLabel()

        # load from roster flag
        self.loadFromRoster = javax.swing.JCheckBox()
        self.loadFromRoster.setToolTipText("Load settings from roster entry if found.")
        self.loadFromRoster.setSelected(True)        
        
        # auto-scroll message window flag
        self.autoScroll = javax.swing.JCheckBox()
        self.autoScroll.setToolTipText("Sets message window to auto-scroll")
        self.autoScroll.setSelected(True)    
        
        # time stamp message window flag
        self.scrollTimeStamp = javax.swing.JCheckBox()
        self.scrollTimeStamp.setToolTipText("Insert time stamp on message window")
        self.scrollTimeStamp.setSelected(True)        
        
        # create a text area
        self.scrollArea = javax.swing.JTextArea(15, 70)    # define a text area with it's size
        if (self.debugLevel >= LowDebug) :
            self.msgText("Enter the loco number, direction and addr mode")
            self.msgText("Set min and max speed")
            self.msgText("Enter block name loco is in")
        srcollField = javax.swing.JScrollPane(self.scrollArea) # put text area in scroll field
        
        # create a frame to hold the buttons and fields
        # also create a window listener. This is used mainly to remove the property change listener
        # when the window is closed by clicking on the window close button
        w = self.WinListener()
        self.scriptFrame = javax.swing.JFrame("Run Loco")       # argument is the frames title
        self.scriptFrame.contentPane.setLayout(javax.swing.BoxLayout(self.scriptFrame.contentPane, javax.swing.BoxLayout.Y_AXIS))
        self.scriptFrame.addWindowListener(w)
        w.setCallBack(self.scriptFrame, self.releaseAllListeners)
        # put the text field on a line preceded by a label
        temppanel1 = javax.swing.JPanel()
        temppanel1.add(javax.swing.JLabel("Loco Address:"))
        temppanel1.add(self.locoAddress)
        temppanel1.add(javax.swing.JLabel(" LongAddr?"))
        temppanel1.add(self.locoLong)
        temppanel1.add(javax.swing.JLabel(" Forward?"))
        temppanel1.add(self.locoForward)
        
        temppanel1a = javax.swing.JPanel()
        temppanel1a.add(javax.swing.JLabel("Headlight:"))
        temppanel1a.add(self.locoHeadlight)
        temppanel1a.add(javax.swing.JLabel("Bell:"))
        temppanel1a.add(self.locoBell)
        temppanel1a.add(self.locoHorn)
        temppanel1a.add(self.locoMoveGreen)
        temppanel1a.add(self.locoMoveYellow)
        temppanel1a.add(self.locoMoveRed)
        
        # build speed table
        gLayout = java.awt.GridBagLayout()
        gConstraints = java.awt.GridBagConstraints()
        self.speedPane = javax.swing.JPanel()
        pane2Border = javax.swing.BorderFactory.createEtchedBorder()
        pane2Titled = javax.swing.BorderFactory.createTitledBorder(pane2Border, "Speed Settings")
        self.speedPane.setBorder(pane2Titled)
        self.speedPane.setLayout(gLayout)
        gConstraints.gridx = 0
        gConstraints.gridy = 0
        gConstraints.gridwidth = 1
        gConstraints.gridheight = 1
        gConstraints.ipadx = 12
        gConstraints.ipady = 3
        gConstraints.insets = java.awt.Insets(3, 3, 3, 3)
        
        self.speedPane.add(javax.swing.JLabel("locoDistRed"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("Throttle"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(" "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("Inch/Sec"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("Speed Chg Distance"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        
        self.speedPane.add(javax.swing.JLabel(" "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("Indication"), gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        self.speedPane.add(javax.swing.JLabel("Throttle"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(" "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("Inch/Sec"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("Speed Chg Distance"), gConstraints)
        gConstraints.gridx = 0
        gConstraints.gridy = gConstraints.gridy + 1
        
        self.speedPane.add(javax.swing.JLabel(self.greenFlashSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedGreenFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        self.speedPane.add(self.locoRateGreenFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        self.speedPane.add(self.locoDistGreenFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        
        self.speedPane.add(javax.swing.JLabel(" "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(self.greenSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedGreen, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        self.speedPane.add(self.locoRateGreen, gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        self.speedPane.add(self.locoDistGreen, gConstraints)
        gConstraints.gridx = 0
        gConstraints.gridy = gConstraints.gridy + 1
        
        self.speedPane.add(javax.swing.JLabel(self.yellowFlashSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedYellowFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoRateYellowFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoDistYellowFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        
        self.speedPane.add(javax.swing.JLabel("  "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(self.yellowSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedYellow, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoRateYellow, gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        self.speedPane.add(self.locoDistYellow, gConstraints)
        gConstraints.gridx = 0
        gConstraints.gridy = gConstraints.gridy + 1
        
        self.speedPane.add(javax.swing.JLabel(self.redFlashSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedRedFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoRateRedFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoDistRedFlash, gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        
        self.speedPane.add(javax.swing.JLabel("  "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(self.redSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedRed, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoRateRed, gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        self.speedPane.add(self.locoDistRed, gConstraints)
        gConstraints.gridx = 0
        gConstraints.gridy = gConstraints.gridy + 1
        
        self.speedPane.add(javax.swing.JLabel("Load From Roster: "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.loadFromRoster, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.saveToRosterButton, gConstraints)
        
        # build block info
        temppanel3 = javax.swing.JPanel()
        temppanel3.add(javax.swing.JLabel("Block Starting: "))
        temppanel3.add(self.blockStart)
        temppanel3.add(javax.swing.JLabel(" Eastbound: "))
        temppanel3.add(self.blockDirection)
        temppanel3.add(javax.swing.JLabel(" Check block ahead: "))
        temppanel3.add(self.blockAhead2)
        temppanel3.add(javax.swing.JLabel(" Lookahead for Stop: "))
        temppanel3.add(self.useStopFromDistance)
        temppanel3.add(javax.swing.JLabel(" Stopping Block: "))
        temppanel3.add(self.blockStop)
        temppanel3.add(javax.swing.JLabel(" AutoScroll Messages: "))
        temppanel3.add(self.autoScroll)
        temppanel3.add(javax.swing.JLabel(" Time Stamp Messages: "))
        temppanel3.add(self.scrollTimeStamp)
        
        temppanel4 = javax.swing.JPanel()
        temppanel4.add(javax.swing.JLabel("Speed: "), gConstraints)
        temppanel4.add(self.locoSpeed, gConstraints)
        temppanel4.add(javax.swing.JLabel("% "), gConstraints)
        temppanel4.add(javax.swing.JLabel(" Now:"))
        temppanel4.add(self.blockNow)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.signalNext)
        temppanel4.add(self.signalNextText)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.blockNowLength)
        temppanel4.add(javax.swing.JLabel("in  Next:"))
        temppanel4.add(self.blockNext)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.signalBeyond)
        temppanel4.add(self.signalBeyondText)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.blockNextLength)
        temppanel4.add(javax.swing.JLabel("in  Beyond:"))
        temppanel4.add(self.blockBeyond)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.blockBeyondLength)
        temppanel4.add(javax.swing.JLabel(" in "))
        temppanel4.add(javax.swing.JLabel("Min Stop Dist: "))
        temppanel4.add(self.stopDistance)
        temppanel4.add(javax.swing.JLabel(" : "))
        temppanel4.add(self.stopDistanceForSpeed)
                
        self.greenFlashSignalIcon.setRotation(1, temppanel4)
        self.greenSignalIcon.setRotation(1, temppanel4)
        self.yellowFlashSignalIcon.setRotation(1, temppanel4)
        self.yellowSignalIcon.setRotation(1, temppanel4)
        self.redFlashSignalIcon.setRotation(1, temppanel4)
        self.redSignalIcon.setRotation(1, temppanel4)
        self.darkSignalIcon.setRotation(1, temppanel4)
        
        butPanel = javax.swing.JPanel()
        butPanel.add(self.startButton)
        butPanel.add(self.stopButton)
        butPanel.add(self.testButton)
        butPanel.add(self.haltButton)
        butPanel.add(self.shrinkButton)

        # Put contents in frame and display
        self.scriptFrame.contentPane.add(temppanel1)
        self.scriptFrame.contentPane.add(temppanel1a)
        self.scriptFrame.contentPane.add(self.speedPane)
        self.scriptFrame.contentPane.add(temppanel3)
        self.scriptFrame.contentPane.add(temppanel4)
        self.scriptFrame.contentPane.add(srcollField)
        self.scriptFrame.contentPane.add(butPanel)
        self.scriptFrame.pack()
        self.scriptFrame.show()
        self.isAborting = False
        return
        
    def setLoco(self, locoId) :
        self.methodLocoAddress = locoId
        return
        
    def setLocoEast(self) :
        self.methodBlockDirection = True
        return
        
    def setLocoWest(self) :
        self.methodBlockDirection = False
        return
        
    def setLocoForward(self) :
        self.methodLocoForward = True
        return
        
    def setLocoReverse(self) :
        self.methodLocoForward = False
        return
        
    def locoHeadlightOn(self) :
        self.methodLocoHeadlight = True
        return
        
    def locoHeadlightOff(self) :
        self.methodLocoHeadlight = False
        return
        
    def soundShortHorn(self) :
        self.methodShortHorn = True
        return
        
    def soundLongHorn(self) :
        self.methodLongHorn = True
        return
        
    def pushShrink(self) :
        self.methodPushShrink = True
        return
        
    def setStartBlock(self, blockId) :
        self.methodBlockStart = blockId
        return
        
    def pushStart(self) :
        self.methodPushStart = True
        return
        
    def pushStop(self) :
        self.methodPushStop = True
        return
        
    def pushTest(self) :
        self.methodPushTest = True
        return
        
    def setStopBlock(self, blockId) :
        self.methodBlockStop = blockId
        return
        
    def setStopDistance(self, dist) :
        self.methodlocoDistRed = dist
        return
        
    def updateMemoryWithCurrentSpeed(self, memoryId) :
        mem = jmri.InstanceManager.memoryManagerInstance().provideMemory(memoryId)
        if (mem != None) :
            if (self.currentThrottle != None) :
                mem.setValue(str((int)(round(self.currentThrottle.getSpeedSetting() * 100, 0))))
            else :
                mem.setValue("0")
        return
        
    def returnCurrentSpeed(self) :
        if (self.currentThrottle != None) :
            v = str((int)(round(self.currentThrottle.getSpeedSetting() * 100, 0)))
        else :
            v = "0"
        return(v)
        
    def updateMemoryWithCurrentBlock(self, memoryId) :
        mem = jmri.InstanceManager.memoryManagerInstance().provideMemory(memoryId)
        if (mem != None) :
            if (self.currentBlock != None) :
                mem.setValue(self.giveBlockName(self.currentBlock))
            else :
                mem.setValue("")
        return
        
    def returnCurrentBlock(self) :
        if (self.currentBlock != None) :
            v = self.giveBlockName(self.currentBlock)
        else :
            v = ""
        return(v)
        
    def setDebugNone(self) :
        self.debugLevel = NoneDebug
        return()
        
    def setDebugLow(self) :
        self.debugLevel = LowDebug
        return()
        
    def setDebugMedium(self) :
        self.debugLevel = MediumDebug
        return()
        
    def setDebugHigh(self) :
        self.debugLevel = HighDebug
        return()
        
# if you are running the RobotThrottle completely interactive,
# the following two lines are all you need
rb1 = LocoThrot()
rb1.start()
#rb1.setSignalAppearanceHalt(GREEN)
# However, if you are automating the automation, then 
## Options for doing more via scripts or Logix Jython command line option
## this will set the loco number, if a matching roster entry is found, it will load the values
## rb1.setLoco("111")
## rb1.setLocoEast()
## rb1.setLocoWest()
## rb1.locoHeadlightOn()
## rb1.locoHeadlightOff()
## rb1.soundShortHorn()
## rb1.soundLongHorn()
## this will set the starting block
## rb1.setStartBlock("LB25")
## this will enable the loco to move according to the signals
## rb1.pushStart()
## rb1.pushStop()
## this sets a block to stop at
## rb1.setStopBlock("LB27")
## shrink the display size
## rb1.pushShrink()
## turn off all debug messages
## rb1.setDebugNone()
## turn on all debug messages
## rb1.setDebugHigh()

