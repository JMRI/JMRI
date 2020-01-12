# This script runs a loco around the track, controlling the speed
# according to signals and following the blocks.
#
# Author: Ken Cameron, copyright 2009
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
import javax.swing

HighDebug = 3
MediumDebug = 2
LowDebug = 1
NoneDebug = 0

# set up a throttle with the loco address
class LocoThrot(jmri.jmrit.automat.AbstractAutomaton) :
    # initialize variables
    locoAddress = None
    currentBlock = None
    currentDir = jmri.Path.NONE
    currentBlocks = []
    currentNextBlocks = []
    nextBlock = None
    beyondBlock = None
    priorBlocks = []
    priorNextBlocks = []
    currentSignal = None
    currentSignalAspect = None
    farSignal = None
    farSignalAspect = None
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
    redDelayTimer = None
    redDelayListener = None
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
    methodLocoDistanceRedStop = None
    haltOnSignalHeadAppearance = YELLOW
    debugLevel = LowDebug
    
    def init(self):
        #print("start begin:.\n")
        self.setName("RB2: no loco")
        self.setup()
        #print("start end:.\n")
        return
        
    def handle(self):
        if (self.isAborting == True) :
            return 0
        #self.msgText("handle begin:.\n")
        self.waitMsec(1000)
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
        if (self.methodLocoDistanceRedStop != None) :
            self.locoDistanceRedStop.text = self.methodLocoDistanceRedStop
            self.methodLocoDistanceRedStop = None
            self.whenLocoChanged(self)
        # This handles tracking how many overlapping events happened
        #   and insures we run the didWeMove the right number of times
        if ((self.didWeMoveCounter > 0 and self.holdMoving == False) or self.isStarting == True) :
             #self.msgText("didWeMoveCounterCheck: " + self.didWeMoveCounter.toString() + " - calling didWeMove\n")
             self.didWeMove()
             self.didWeMoveCounter = 0
             #self.msgText("didWeMoveCounterCheck: decremented counter down to " + self.didWeMoveCounter.toString() + "\n")
        #self.msgText("handle done\n")
        return 1 #continue if 1, run once if 0
    
    # allow changes of a signal dropping on the appearance to trigger halting
    def setSignalAppearanceHalt(self, signalIndication) :
        self.haltOnSignalHeadAppearance = signalIndication
        return
        
    # show what level of signal appearance causes a halt on dropping signal
    def returnSignalAppearanceHalt(self) :
        return(int(self.haltOnSignalHeadAppearance).toString())
        
    def getNewThrottle(self) :
        self.holdMoving = True
        if (self.currentThrottle != None) :
            oldId = self.currentThrottle.getLocoAddress()
            self.msgText("stop and release the current loco: " + oldId.toString() + "\n")
            self.doStop();
            self.currentThrottle.release(None)
            self.currentThrottle = None
            self.msgText("Throttle " + oldId.toString() + " released\n")
        self.msgText("Getting throttle - ") #add text to scroll field
        id = int(self.locoAddress.text)
        if (self.throttleManager.addressTypeUnique() == True) :
            #self.msgText("id values are not ambiguous\n")
            isLong = self.throttleManager.canBeLongAddress(id)
        else :
            self.msgText("id values are ambiguous\n")
            isLong = True
            if (self.throttleManager.canBeShortAddress(id)) :
                self.msgText("id could be a short.\n")
                if (self.locoLong.isSelected() == False) :
                    isLong = False
        self.msgText(" getting " + id.toString() + " " + isLong.toString() + " - ")
        throttle = self.getThrottle(id, isLong)
        self.currentThrottle = throttle
        if (self.currentThrottle == None) :
            self.msgText("Couldn't assign throttle! - Run stopped\n")
            self.doHalt()
        else : 
            #self.msgText("got throttle: " + self.currentThrottle.getLocoAddress().toString() + "\n")
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

    # Isolate the callback handling from the didWeMove processing
    #   this makes dealing with multiple events simpler
    def callBackForDidWeMove(self, event) :
        self.didWeMoveCounter = self.didWeMoveCounter + 1
        #self.msgText("callBackForDidWeMove(" + event.toString() + ")\n  counter = " + self.didWeMoveCounter.toString() + "\n")
        # the handle() will invoke the didWeMove() as needed
        return
         
    # figure out if we moved and where
    def didWeMove(self) :
        #self.msgText("didWeMove start: " + self.giveBlockName(self.currentBlock) + ":" + self.giveBlockName(self.nextBlock) + "\n")
        if (self.currentThrottle == None) :
            #self.msgText("didWeMove called while currentThrottle was None\n")
            return
        #if (self.currentBlock != None) :
            #self.msgText("Current block: " + self.giveBlockName(self.currentBlock) + "\n")
        newCurrentBlocks = self.findCurrentBlocks()
        if (len(newCurrentBlocks) == 0) :
            self.msgText("Can't find loco!! Doing halt!!")
            self.doHalt()
        # new current block must be farthest connected to current block in current direction chain
        oldCurrent = self.currentBlock
        oldSignal = self.currentSignal
        oldAspect = self.currentSignalAspect
        oldFarSignal = self.farSignal
        oldFarAspect = self.farSignalAspect
        oldFarBlock = self.nextBlock
        tryBlock = self.currentBlock
        nearSignal = None
        farSignal = None
        newCurrent = None
        giveUpTimer = 0
        while (giveUpTimer < 10) :
            giveUpTimer = giveUpTimer + 1
            newCurrent = self.findNewCurrentBlock(tryBlock, newCurrentBlocks, self.currentDir)
            if (newCurrent == None) :
                newBlockText = "None"
            else :
                newBlockText = self.giveBlockName(newCurrent)
            #self.msgText("try " + giveUpTimer.toString() + " " + self.giveBlockName(tryBlock) + " " + newBlockText + "\n")
            if ((newCurrent == tryBlock) or (newCurrent == None)) :
                break
            else :
                tryBlock = newCurrent
        #self.msgText("tryBlock: " + self.giveBlockName(tryBlock) + " oldCurrent: " + self.giveBlockName(oldCurrent) + "\n")
        if (tryBlock != oldCurrent or self.isStarting == True) :
            # we did move somewhere
            self.blockNow.text = " "
            self.blockNowLength.text = " "
            self.blockNext.text = " "
            self.blockNextLength.text = " "
            self.blockBeyond.text = " "
            self.blockBeyondLength.text = " "
            self.currentBlock = tryBlock
            self.blockStart.text = self.giveBlockName(self.currentBlock)
            self.blockNow.text = self.giveBlockName(self.currentBlock)
            self.blockNowLength.text = self.currentBlock.getLengthIn().toString()
            self.nextBlock = self.findNextBlock(self.currentBlock)
            self.beyondBlock = None
            self.testAddBlockListener(self.currentBlock)
            if (self.nextBlock != None) :
                self.blockNext.text = self.giveBlockName(self.nextBlock)
                self.blockNextLength.text = self.nextBlock.getLengthIn().toString()
                self.beyondBlock = self.findNextBlock(self.nextBlock)
                self.testAddBlockListener(self.nextBlock)
                if (self.beyondBlock != None) :
                    self.blockBeyond.text = self.giveBlockName(self.beyondBlock)
                    self.blockBeyondLength.text = self.beyondBlock.getLengthIn().toString()
                    self.testAddBlockListener(self.beyondBlock)
            self.priorBlock = oldCurrent
            self.priorBlocks = self.currentBlocks
        # find signals from currentBlock
        if (self.currentBlock != None and self.nextBlock != None) :
            nearSignal = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(self.currentBlock, self.nextBlock)
        if (self.nextBlock != None and self.beyondBlock != None) :
            farSignal = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(self.nextBlock, self.beyondBlock)
        if (self.blockAhead2.isSelected() == False) :
            #self.msgText("3 block test: " + self.giveBlockName(self.currentBlock) + ":" + self.giveBlockName(self.nextBlock) + ":" + self.giveBlockName(oldCurrent) + " signals:" + self.giveSignalName(oldSignal) + ":" + self.giveSignalName(nearSignal) + "\n")
            watchSignal = nearSignal
        else :
            #self.msgText("4 block test: " + self.giveBlockName(self.nextBlock) + "\n")
            watchSignal = farSignal
        # if we didn't find a signal, treat as RED
        if (watchSignal == None) :
            watchAspect = RED
        else :
            watchAspect = watchSignal.getAppearance()
        # if we moved or the signal head changed or the aspect changed
        if (oldCurrent != self.currentBlock or oldSignal != watchSignal or oldAspect != watchAspect or self.isStarting == True) :
            # something changed, we calc the new speed
            if (oldCurrent == self.currentBlock and oldSignal == watchSignal and self.compareSignalAspects(oldAspect, watchAspect) < 0 and self.isStarting == False)  :
                # signal dropped, that's bad
                self.msgText("signal dropped, same signal being watched.\n")
                if (self.compareSignalAspects(self.haltOnSignalHeadAppearance, watchAspect) >= 0) : # Only stop on dropping below this
                    self.findNewSpeed(self.currentBlock, self.nextBlock)
                else :
                    self.msgText("Signal dropped in front of train. Halting!!\n")
                    self.doHalt()
            else :
                #self.msgText("We moved, signal or aspect changed.\n")
                self.findNewSpeed(self.currentBlock, self.nextBlock)
        # this is for display updates
        if (nearSignal != None) :
            self.signalNext.setIcon(self.cvtAppearanceIcon(nearSignal))
            self.signalNextText.text = self.cvtAppearanceText(nearSignal)
            self.testAddSignalListener(nearSignal)
        else :
            self.signalNext.setIcon(None)
            self.signalNextText.text = ""
        if (farSignal != None) :
            self.signalBeyond.setIcon(self.cvtAppearanceIcon(farSignal))
            self.signalBeyondText.text = self.cvtAppearanceText(farSignal)
            self.testAddSignalListener(farSignal)
            self.farSignal = farSignal
            self.farSignalAspect = farSignal.getAppearance()
        else :
            self.signalBeyond.setIcon(None)
            self.signalBeyondText.text = ""
        # if we have a stop block
        if (self.stopBlock != None and self.currentBlock == self.stopBlock) :
            if (self.currentThrottle.getSpeedSetting() == 0) :
                self.msgText("Found stop block, doing stop.\n")
                self.doStop()
        self.isStarting = False
        self.releaseExtraListeners()
        #self.msgText("didMove: done\n")
        return
        
    # check lists of block and signal listeners and release ones not needed
    def releaseExtraListeners(self) :
        numBlocksL = len(self.listenerBlockListeners)
        numBlocks = len(self.listenerBlocks)
        #self.msgText("start with Blocks: " + numBlocks.toString() + " Listeners: " + numBlocksL.toString() + "\n")
        if (numBlocksL != numBlocks or numBlocksL <= 0 or numBlocks <= 0) :
            # blocks out of sync, stop and take it from the top
            self.msgText("Block lists out of sync! Blocks: " + numBlocks.toString() + " Listeners: " + numBlocksL.toString() + "\n")
            self.doHalt()
            self.releaseBlockListParts()
        else :
            while (numBlocks > 0) :
                numBlocks = numBlocks - 1
                testBlock = self.listenerBlocks[numBlocks]
                #self.msgText("testing block: " + self.giveBlockName(testBlock) + "\n")
                if (testBlock != None) :
                    if (testBlock != self.currentBlock) :
                        if (testBlock != self.nextBlock) :
                            if (testBlock != self.beyondBlock) :
                                #self.msgText("we don't need block: " + self.giveBlockName(testBlock) + "\n")
                                l = self.listenerBlockListeners[numBlocks]
                                testBlock.removePropertyChangeListener(l)
                                self.listenerBlockListeners.pop(numBlocks)
                                self.listenerBlocks.pop(numBlocks)
        numSignals = len(self.listenerSignals)
        numSignalsL = len(self.listenerSignalListeners)
        #self.msgText("starting Signals: " + numSignals.toString() + " Listeners:" + numSignalsL.toString() + "\n")
        if (numSignals != numSignalsL or numSignalsL <= 0 or numSignals <= 0) :
            self.msgText("Signal lists out of sync! Signals: " + numSignals.toString() + " Listeners:" + numSignalsL.toString() + "\n")
            self.doHalt()
            self.releaseSignalListParts()
        else :
            while (numSignals > 0) :
                numSignals = numSignals - 1
                testSignal = self.listenerSignals[numSignals]
                #self.msgText("testing signal: " + self.giveSignalName(testSignal) + "\n")
                if (testSignal != self.currentSignal) :
                    #self.msgText("not currentSignal: " + self.giveSignalName(self.currentSignal) + "\n")
                    if (testSignal != self.farSignal) :
                        #self.msgText("not farSignal: " + self.giveSignalName(self.farSignal) + "\n")
                        #self.msgText("we don't need signal: " + self.giveSignalName(testSignal) + "\n")
                        l = self.listenerSignalListeners[numSignals]
                        testSignal.removePropertyChangeListener(l)
                        self.listenerSignalListeners.pop(numSignals)
                        self.listenerSignals.pop(numSignals)
                        #self.msgText("num signalListeners: " + len(self.listenerSignalListeners).toString() + " " + len(self.listenerSignals).toString() + "\n")
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
        return
    
    # release signal listeners and clean lists
    def releaseSignalListParts(self) :
        while(len(self.listenerSignals) > 0) :
            s = self.listenerSignals.pop(0)
            l = self.listenerSignalListeners.pop(0)
            #self.msgText("RB2: releasing listener for signal " + self.giveSignalName(s) + "\n")
            s.removePropertyChangeListener(l)
        return

    # release block listeners and clean lists
    def releaseBlockListParts(self) :
        while(len(self.listenerBlocks) > 0) :
            b = self.listenerBlocks.pop(0)
            l = self.listenerBlockListeners.pop(0)
            #self.msgText("RB2: releasing listener for block " + self.giveBlockName(b) + "\n")
            b.removePropertyChangeListener(l)
        return
    
    # release all listeners, part of the exit cleanup
    def releaseAllListeners(self, event) :
        self.releaseSignalListParts()
        self.releaseBlockListParts()
        if (self.redDelayTimer != None) :
            for i in self.redDelayTimer.getActionListeners() :
                self.redDelayTimer.removeActionListener(i)
        if (self.hornDelayTimer != None) :
            for i in self.hornDelayTimer.getActionListeners() :
                self.hornDelayTimer.removeActionListener(i)
        if (self.currentThrottle != None) :
            #print("RB2: releasing throttle\n")
            self.currentThrottle.setSpeedSetting(0)
            self.currentThrottle.release(None)
        self.isAborting = True
        return

    # take list of new current blocks, a current block, and a current direction
    # return new current block at edge of current blocks
    def findNewCurrentBlock(self, cBlock, cList, cDir) :
        nBlock = None
        if (cDir == jmri.Path.NONE) :
            if (self.blockDirection.isSelected() == True) :
                cDir = cDir or jmri.Path.EAST
            else :
                cDir = cDir or jmri.Path.WEST
        if (cBlock == None) :
            self.msgText("findNewCurrentBlock, bad current block passed!\n")
            return None
        if (len(cList) <= 0) :
            self.msgText("findNewCurrentBlock, empty cList\n")
        else :
            pList = cBlock.getPaths()
            for p in pList :
                pB = p.getBlock()
                if (p.checkPathSet()) :
                    dirTest = p.getToBlockDirection()
                    #self.msgText("findNewCurrentBlock testing for " + jmri.Path.decodeDirection(cDir) + " from " + self.giveBlockName(cBlock) + " vs " + self.giveBlockName(pB) + " pointing " + jmri.Path.decodeDirection(dirTest) + "\n")
                    if (cDir & dirTest == cDir) :
                        for c in cList :
                            if (c == pB) :
                                nBlock = pB
                                #self.msgText("findNewCurrentBlock found " + self.giveBlockName(pB) + "\n")
                                break
                            #else :
                                #self.msgText("findNewCurrentBlock not in cList: " + self.giveBlockName(c) + "\n")
                    if (nBlock != None) :
                        break
                #else :
                    #self.msgText("findNewCurrentBlock path not traversable: " + self.giveBlockName(cBlock) + " to " + self.giveBlockName(pB) + "\n")
        return nBlock

    # figure out signal names and decide speeds
    def findNewSpeed(self, cBlock, nBlock) :
        if (self.isRunning) :
            if (cBlock == None) :
                if (nBlock == None) :
                    self.msgText("Failed to find either blocks\n")
                    self.doHalt()
                else :
                    self.msgText("Failed to find current block\n")
                    self.doHalt()
            else :
                if (nBlock == None) :
                    self.msgText("next block doesn't exist, treating as red.\n")
                    self.speedFromAppearance(RED)
                    self.currentSignal = None
                    self.currentSignalAspect = RED
                    self.farSignal = None
                    self.farSignalAspect = RED
                else :
                    if (self.debugLevel >= MediumDebug) :
                        self.msgText("looking for signal between " + self.giveBlockName(cBlock) + " and " + self.giveBlockName(nBlock) + "\n")
                    s = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(cBlock, nBlock)
                    if (s != None) :
                        if (self.debugLevel >= MediumDebug) :
                            self.msgText("Found currentSignal: " + self.giveSignalName(s) + " displaying: " + self.cvtAppearanceText(s) + "\n")
                        self.speedFromAppearance(s.getAppearance())
                        self.currentSignal = s
                        self.currentSignalAspect = s.getAppearance()
                    else :
                        self.msgText("Failed finding signal!\n")
                        self.doHalt()
        return
    
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
        
    # convert signal appearance to text
    def textSignalAspect(self, sigAspect) :
        ret = "???"
        if (sigAspect & RED != 0) :
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
            self.msgText("compare signals got a None for new signal state\n")
            self.doHalt()
            return
        if (oldSigState == None) :
            # startup case
            ret = 1
        else :
            newSigValue = self.rankSignalAspect(newSigState)
            oldSigValue = self.rankSignalAspect(oldSigState)
            #self.msgText("compareSignalAspects: " + self.textSignalAspect(oldSigState) + " went " + self.textSignalAspect(newSigState) + "\n")
            if (newSigValue < oldSigValue) :
                ret = -1
            elif (newSigValue > oldSigValue) :
                ret = 1
        return ret
        
    # set speed from signal appearance
    def speedFromAppearance(self, sigState) :
        rep = ""
        if (sigState == RED or self.currentBlock == self.stopBlock) :
            rep = rep + "doRed "
            self.doSpeedRed()
        elif (sigState == FLASHRED) :
            rep = rep + "doRedFlash "
            self.doSpeedRedFlash()
        elif (sigState == YELLOW or self.nextBlock == self.stopBlock) :
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
            self.msgText("speedFromAppearance, unknown value! " + sigState.toString() + "\n")
            self.doHalt()
        #self.msgText("speedFromAppearance: " + self.giveSignalName(sig) + " displaying: " + self.cvtAppearanceText(sig) + " so we did: " + rep + "\n")
        return
        
    # convert signal appearance to english
    def cvtAppearanceText(self, sig) :
        rep = ""
        if (sig.getHeld()) :
            rep = rep + "Held "
        if (sig.getLit()) :
            rep = rep + "Lit "
        sigState = sig.getAppearance()
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
        #self.msgText("cvtAppearanceText: " + self.giveSignalName(sig) + " displaying: " + rep + "\n")
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
        #self.msgText("cvtAppearanceIcon: " + self.giveSignalName(sig) + " displaying: " + rep + "\n")
        return rep
        
    # compare two lists, reply true or false
    def compareLists(self, aList, bList) :
        self.msgText("comparing lists\n")
        doesMatchA = True
        doesMatchB = True
        for a in aList :
            try :
                i = bList.index(a)
            except :
                doesMatchA = False
        if (doesMatchA) :
            self.msgText("comparing lists: all of a in b\n")
        for b in bList :
            try :
                i = aList.index(b)
            except :
                doesMatchB = False
        if (doesMatchB) :
            self.msgText("comparing lists: all of b in a\n")
        return doesMatchA and doesMatchB
    
    def doSpeedGreenFlash(self):
        if (self.redDelayTimer != None) :
            self.redDelayTimer.stop()
        if (self.currentThrottle != None) :
            i = int(self.locoSpeedGreenFlash.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedGreenFlash: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoSpeedGreenFlash.text
        return
        
    def doSpeedGreen(self):
        if (self.redDelayTimer != None) :
            self.redDelayTimer.stop()
        if (self.currentThrottle != None) :
            i = int(self.locoSpeedGreen.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedGreen: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoSpeedGreen.text
        return
        
    def doSpeedYellowFlash(self):
        if (self.redDelayTimer != None) :
            self.redDelayTimer.stop()
        if (self.currentThrottle != None) :
            i = int(self.locoSpeedYellowFlash.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedYellowFlash: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoSpeedYellowFlash.text
        return
        
    def doSpeedYellow(self):
        if (self.redDelayTimer != None) :
            self.redDelayTimer.stop()
        if (self.currentThrottle != None) :
            i = int(self.locoSpeedYellow.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedYellow: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoSpeedYellow.text
        return
        
    def doSpeedRedFlash(self):
        if (self.redDelayTimer != None) :
            self.redDelayTimer.stop()
        if (self.currentThrottle != None) :
            i = int(self.locoSpeedRedFlash.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedRedFlash: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoSpeedRedFlash.text
        return
        
    def doSpeedRed(self):
        if (self.currentThrottle != None and self.currentThrottle.getSpeedSetting() != 0 and (self.redDelayTimer == None or self.redDelayTimer.isRunning() == False)) :
            i = int(self.locoSpeedRed.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doSpeedRed: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoSpeedRed.text
            # compute how long to delay stopping
            dist = self.currentBlock.getLengthIn()
            rate = float(self.locoRateRed.text)
            stopDist = float(self.locoDistanceRedStop.text)
            if (dist != 0 and rate != 0) :
                # the stop distance is the reserved space plus 10% from far end of block
                # the less one covers the delay of the handle() routine
                delay = ((dist - stopDist) / rate * 0.90) - 1
                self.msgText("doSpeedRed: dist: " + dist.toString() + " rate: " + rate.toString() + " stopDist: " + stopDist.toString() + " delay: " + delay.toString() + "\n")
                if (delay > 1) :
                    currentDelay = 0
                    if (self.redDelayTimer == None) :
                        self.redDelayListener = self.RedStopTimeoutReceiver()
                        self.redDelayListener.setCallBack(self.redDelayHandler)
                        self.redDelayTimer = javax.swing.Timer(int(delay * 0), self.redDelayListener)
                        self.redDelayTimer.setInitialDelay(int(delay * 1000))
                        self.redDelayTimer.setRepeats(False);
                    self.redDelayTimer.setInitialDelay(int(delay * 1000))
                    self.redDelayTimer.start()
                else :
                    self.msgText("stop delay less that 1 second")
                    self.doStop()
            else :
                self.doStop()
        return
        
    # handle the timeout for stopping on red
    def redDelayHandler(self, event) :
        if (self.debugLevel >= LowDebug) :
                self.msgText("redDelayHandler, stopping now!\n")
        self.redDelayTimer.stop()
        self.doStop()
        return
        
    # stopping for normal issues, allows for restarting automaticly
    def doStop(self):
        if (self.redDelayTimer != None) :
            self.redDelayTimer.stop()
        if (self.currentThrottle != None) :
            self.currentThrottle.setSpeedSetting(0)
            if (self.debugLevel >= LowDebug) :
                self.msgText("doStop\n")
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
            self.msgText("doHalt, something was in error!!\n")
            self.locoSpeed.text = "0"
            if (self.currentBlock != None) :
                self.blockStart.text = self.giveBlockName(self.currentBlock)
        self.handleHalting()
        self.msgText("*** Run halted ***\n")
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

    # enable the button when OK
    def whenLocoChanged(self, event) : 
        # keep track of whether both fields have been changed
        if (self.isRunning) :
            self.doStop()
            self.msgText("whenLocoChanged, was running, now stopped\n")
        isOk = True
        startBlock = None
        self.locoAddress.text = self.locoAddress.text.strip()
        if (self.locoAddress.text == "") :
            isOk = False
        else :
            self.scriptFrame.setTitle("Run Loco " + self.locoAddress.text)
            self.setName("RB2: " + self.locoAddress.text)
            if (self.locoAddress.text != self.oldLocoAddress) :
                self.oldLocoAddress = self.locoAddress.text
                if (self.loadFromRoster.isSelected() == True) :
                    # take the loco id and try looking up values in roster
                    if (self.rosterInstance == None) :
                        self.rosterInstance = jmri.jmrit.roster.Roster.getDefault()
                        self.msgText("got roster instance\n")
                    rosterEntries = self.rosterInstance.matchingList(None, None, self.locoAddress.text, None, None, None, None)
                    self.msgText("found " + rosterEntries.size().toString() + " entries matching |" + id.toString() + "|\n")
                    for ent in rosterEntries :
                       self.msgText("posible entries: " + ent.fileName + "\n")
                    if (rosterEntries.size() == 1) :
                        ent = rosterEntries.get(0)
                        self.msgText("Reading roster: " + ent.fileName + "\n")
                        v = ent.getAttribute('RT_locoSpeedGreenFlash')
                        if (v != None and v != "") :
                            self.locoSpeedGreenFlash.text = v
                        v = ent.getAttribute('RT_locoRateGreenFlash')
                        if (v != None and v != "") :
                            self.locoRateGreenFlash.text = v
                        v = ent.getAttribute('RT_locoSpeedGreen')
                        if (v != None and v != "") :
                            self.locoSpeedGreen.text = v
                        v = ent.getAttribute('RT_locoRateGreen')
                        if (v != None and v != "") :
                            self.locoRateGreen.text = v
                        v = ent.getAttribute('RT_locoSpeedYellowFlash')
                        if (v != None and v != "") :
                            self.locoSpeedYellowFlash.text = v
                        v = ent.getAttribute('RT_locoRateYellowFlash')
                        if (v != None and v != "") :
                            self.locoRateYellowFlash.text = v
                        v = ent.getAttribute('RT_locoSpeedYellow')
                        if (v != None and v != "") :
                            self.locoSpeedYellow.text = v
                        v = ent.getAttribute('RT_locoRateYellow')
                        if (v != None and v != "") :
                            self.locoRateYellow.text = v
                        v = ent.getAttribute('RT_locoSpeedRedFlash')
                        if (v != None and v != "") :
                            self.locoSpeedRedFlash.text = v
                        v = ent.getAttribute('RT_locoRateRedFlash')
                        if (v != None and v != "") :
                            self.locoRateRedFlash.text = v
                        v = ent.getAttribute('RT_locoSpeedRed')
                        if (v != None and v != "") :
                            self.locoSpeedRed.text = v
                        v = ent.getAttribute('RT_locoRateRed')
                        if (v != None and v != "") :
                            self.locoRateRed.text = v
                        v = ent.getAttribute('RT_locoDistanceRedStop')
                        if (v != None and v != "") :
                            self.locoDistanceRedStop.text = v
                        self.locoLong.setSelected(ent.isLongAddress())
                        self.msgText("Read completed: " + ent.fileName + "\n")
                self.oldLocoAddress = self.locoAddress.text
        self.locoSpeedRed.text = self.locoSpeedRed.text.strip()
        self.locoRateRed.text = self.locoRateRed.text.strip()
        self.locoSpeedRedFlash.text = self.locoSpeedRedFlash.text.strip()
        self.locoRateRedFlash.text = self.locoRateRedFlash.text.strip()
        self.locoSpeedYellow.text = self.locoSpeedYellow.text.strip()
        self.locoRateYellow.text = self.locoRateYellow.text.strip()
        self.locoSpeedYellowFlash.text = self.locoSpeedYellowFlash.text.strip()
        self.locoRateYellowFlash.text = self.locoRateYellowFlash.text.strip()
        self.locoSpeedGreen.text = self.locoSpeedGreen.text.strip()
        self.locoRateGreen.text = self.locoRateGreen.text.strip()
        self.locoSpeedGreenFlash.text = self.locoSpeedGreenFlash.text.strip()
        self.locoRateGreenFlash.text = self.locoRateGreenFlash.text.strip()
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
                self.msgText("Invalid block name: " + self.blockStart.text + " please try again\n")
                isOk = False
            else:
                if (startBlock.getState() != ACTIVE) :
                    self.msgText("Block: " + self.blockStart.text + " is not occupied!\n")
                    isOk = False
        if (isOk) :
            # clear id from any existing blocks
            for b in blocks.getNamedBeanSet() :
                if (b != blocks.getBlock(self.blockStart.text) and b.getValue() == self.locoAddress.text) :
                    b.setValue("")
            self.startButton.setEnabled(True)
            self.haltButton.setEnabled(True)
            self.testAddBlockListener(blocks.getBlock(self.blockStart.text))
            self.msgText("Enabled Start\n")
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
            self.msgText("changed horn to: " + state.toString() + " was " + wasState.toString() + "\n")
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
            self.msgText("Started Timed Horn\n")
        return
        
    def hornDelayHandler(self, event) :
        if (self.hornDelayTimer != None) :
            self.hornDelayTimer.stop()
        if (self.currentThrottle != None) :
            self.currentThrottle.setF2(False)
        self.msgText("Stopped Timed Horn\n")
        return
    
    # handle the Headlight button
    def whenLocoHeadlight(self, event) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getF0()
            state = self.locoHeadlight.isSelected()
            self.currentThrottle.setF0(state)
            self.msgText("changed light to: " + state.toString() + " was " + wasState.toString() + "\n")
        return
    
    # handle the Bell button
    def whenLocoBell(self, event) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getF1()
            state = self.locoBell.isSelected()
            self.currentThrottle.setF1(state)
            self.msgText("changed bell to: " + state.toString() + " was " + wasState.toString() + "\n")
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
        self.msgText("Run started\n")     # add text
        if (self.testIfBlockNameValid(self.blockStart.text) == False) :
            self.msgText("Invalid block name: " + self.blockStart.text + " please try again\n")
        else :
            c = blocks.getBlock(self.blockStart.text)
            if (c == None) :
                self.msgText("Invalid block name: " + self.blockStart.text + " please try again\n")
            else :
                c.setValue(self.locoAddress.text)
                self.currentBlock = c
                self.currentSignal = None
                self.currentSignalAspect = None
                self.nextBlock = None
                self.farSignal = None
                self.farSignalAspect = None
                # set flags so things get done from handle() routine
                self.askChangeThrottle = True
                self.askFinishStartButton = True
        self.msgText("whenStartButtonClicked, done\n")     # add text
        return
        
    # split out so it can happen from the handle() routine
    def doFinishStartButton(self) :
        self.msgText("Change button states\n")     # add text
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
            self.msgText("Starting current:" + self.giveBlockName(self.currentBlock) + "\n")
        return
            
    def whenStopButtonClicked(self, event):   
        self.msgText("Slow loco to stop\n")     # add text
        self.doStop()
        self.msgText("*** Run stopped ***\n")
        self.stopButton.setEnabled(False)
        self.handleHalting()
        self.whenLocoChanged(event)
        return
    
    def whenHaltButtonClicked(self, event):   
        self.msgText("Button Halt loco NOW!\n")     # add text
        self.doHalt()
        self.msgText("*** Run halted ***\n")
        self.handleHalting()
        self.whenLocoChanged(event)
        return
    
    def whenShrinkButtonClicked(self, event):   
        if (self.shrinkGrow == True) :
            if (self.debugLevel >= HighDebug) :
                self.msgText("Shrink Display!\n")     # add text
            self.speedPane.setVisible(False)
            self.shrinkGrow = False
            self.fullScrollRows = self.scrollArea.getRows()
            self.scrollArea.setRows(self.fullScrollRows / 2)
            self.scriptFrame.pack()
        else :
            if (self.debugLevel >= HighDebug) :
                self.msgText("Grow Display!\n")
            self.speedPane.setVisible(True)
            self.shrinkGrow = True
            self.scrollArea.setRows(self.fullScrollRows)
            self.scriptFrame.pack()
        return
    
    def whenSaveToRosterButtonClicked(self, event):   
        if (self.locoAddress.text != "") :
            if (self.rosterInstance == None) :
                self.rosterInstance = jmri.jmrit.roster.Roster.getDefault()
                self.msgText("got roster instance\n")
            id = int(self.locoAddress.text)
            rosterEntries = self.rosterInstance.matchingList(None, None, id.toString(), None, None, None, None)
            self.msgText("found " + rosterEntries.size().toString() + " entries matching |" + id.toString() + "|\n")
            for ent in rosterEntries :
               self.msgText("posible entries: " + ent.fileName + "\n")
            if (rosterEntries.size() == 1) :
                ent = rosterEntries.get(0)
                self.msgText("Saving to roster: " + ent.fileName + "\n")
                ent.putAttribute('RT_locoSpeedGreenFlash', self.locoSpeedGreenFlash.text)
                ent.putAttribute('RT_locoRateGreenFlash', self.locoRateGreenFlash.text)
                ent.putAttribute('RT_locoSpeedGreen', self.locoSpeedGreen.text)
                ent.putAttribute('RT_locoRateGreen', self.locoRateGreen.text)
                ent.putAttribute('RT_locoSpeedYellowFlash', self.locoSpeedYellowFlash.text)
                ent.putAttribute('RT_locoRateYellowFlash', self.locoRateYellowFlash.text)
                ent.putAttribute('RT_locoSpeedYellow', self.locoSpeedYellow.text)
                ent.putAttribute('RT_locoRateYellow', self.locoRateYellow.text)
                ent.putAttribute('RT_locoSpeedRedFlash', self.locoSpeedRedFlash.text)
                ent.putAttribute('RT_locoRateRedFlash', self.locoRateRedFlash.text)
                ent.putAttribute('RT_locoSpeedRed', self.locoSpeedRed.text)
                ent.putAttribute('RT_locoRateRed', self.locoRateRed.text)
                ent.putAttribute('RT_locoDistanceRedStop', self.locoDistanceRedStop.text)
                ent.updateFile()
                self.rosterInstance.writeRosterFile()
                self.msgText("Save completed: " + ent.fileName + "\n")
        return
    
    def findCurrentBlocks(self) :
        # search the block list for the matching loco
        blockList = []
        for b in blocks.getNamedBeanSet() :
            if (b.getValue() == self.locoAddress.text and b.getState() == ACTIVE) :
                blockList.append(b)
        return blockList

    def findNextBlock(self, cB) :
        # look down list of getToBlockDirection for match
        # use 'suggestion' flag if current block doesn't have direction
        nB = None
        dirFlag = cB.getDirection()
        if (dirFlag == jmri.Path.NONE) :
            if (self.blockDirection.isSelected() == True) :
                dirFlag = dirFlag or jmri.Path.EAST
            else :
                dirFlag = dirFlag or jmri.Path.WEST
        pathList = cB.getPaths()
        if (self.debugLevel >= HighDebug) :
            self.msgText("searching " + len(pathList).toString() + " paths from " + self.giveBlockName(cB) + "\n")
        for p in pathList :
            blockTest = p.getBlock()
            if (p.checkPathSet()) :
                dirTest = p.getToBlockDirection()
                if (self.debugLevel >= HighDebug) :
                    self.msgText("findNextBlock path traversable: "  + self.giveBlockName(cB) + " to " + self.giveBlockName(blockTest) + " dirTest: " + jmri.Path.decodeDirection(dirTest) + ":" + dirTest.toString() + " dirFlag: " + jmri.Path.decodeDirection(dirFlag) + ":" + dirFlag.toString() + " result: " + (dirTest & dirFlag).toString() + "\n")
                if (dirTest & dirFlag != 0) :
                    nB = blockTest
                    if (self.debugLevel >= LowDebug) :
                        self.msgText("findNextBlock Found " + self.giveBlockName(blockTest) + "\n")
                    #break
            else :
                if (self.debugLevel >= MediumDebug) :
                    self.msgText("findNextBlock path not traversable: " + self.giveBlockName(cB) + " to " + self.giveBlockName(blockTest) + "\n")
        return nB
        
    # ActionListener - used for the stop timeout
    class RedStopTimeoutReceiver(java.awt.event.ActionListener):
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

    # handle adding to message window
    def msgText(self, txt) :
        self.scrollArea.append(txt)
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
        self.throttleManager = jmri.InstanceManager.throttleManagerInstance()
        if (self.throttleManager == None) :
            print("No command station found!!\nRB has no way to control the trains.\n")
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
        self.locoForward.actionPerformed = self.whenLocoChanged
        self.locoForward.focusLost = self.whenLocoChanged
        
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

        # create the speed fields for a Green Flash Signal
        self.locoSpeedGreenFlash = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedGreenFlash.setToolTipText("Green Flash Speed is a number from 1 to 100%")
        self.locoSpeedGreenFlash.actionPerformed = self.whenLocoChanged
        self.locoSpeedGreenFlash.focusLost = self.whenLocoChanged
        self.locoSpeedGreenFlash.text = "70"
        
        # create the physical speed field for a Green Flash Signal
        self.locoRateGreenFlash = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateGreenFlash.setToolTipText("Throttle as Distance/Second, approaching green flash signal")
        self.locoRateGreenFlash.actionPerformed = self.whenLocoChanged
        self.locoRateGreenFlash.focusLost = self.whenLocoChanged
        self.locoRateGreenFlash.text = "0"
        
        # create the speed fields for a Green Signal
        self.locoSpeedGreen = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedGreen.setToolTipText("Green Speed is a number from 1 to 100%")
        self.locoSpeedGreen.actionPerformed = self.whenLocoChanged
        self.locoSpeedGreen.focusLost = self.whenLocoChanged
        self.locoSpeedGreen.text = "45"
        
        # create the physical speed field for a Green Signal
        self.locoRateGreen = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateGreen.setToolTipText("Throttle as Distance/Second, approaching Green signal")
        self.locoRateGreen.actionPerformed = self.whenLocoChanged
        self.locoRateGreen.focusLost = self.whenLocoChanged
        self.locoRateGreen.text = "9"
        
        # create the speed fields for a Yellow Flash Signal
        self.locoSpeedYellowFlash = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedYellowFlash.setToolTipText("Yellow Flash Speed is a number from 1 to 100%")
        self.locoSpeedYellowFlash.actionPerformed = self.whenLocoChanged
        self.locoSpeedYellowFlash.focusLost = self.whenLocoChanged
        self.locoSpeedYellowFlash.text = "45"
        
        # create the physical speed field for a Yellow Flash Signal
        self.locoRateYellowFlash = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateYellowFlash.setToolTipText("Throttle as Distance/Second, approaching yello flash signal")
        self.locoRateYellowFlash.actionPerformed = self.whenLocoChanged
        self.locoRateYellowFlash.focusLost = self.whenLocoChanged
        self.locoRateYellowFlash.text = "0"
        
        # create the speed fields for a Yellow Signal
        self.locoSpeedYellow = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedYellow.setToolTipText("Yellow Speed is a number from 1 to 100%")
        self.locoSpeedYellow.actionPerformed = self.whenLocoChanged
        self.locoSpeedYellow.focusLost = self.whenLocoChanged
        self.locoSpeedYellow.text = "30"
        
        # create the physical speed field for a Yellow Signal
        self.locoRateYellow = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateYellow.setToolTipText("Throttle as Distance/Second, approaching yellow signal")
        self.locoRateYellow.actionPerformed = self.whenLocoChanged
        self.locoRateYellow.focusLost = self.whenLocoChanged
        self.locoRateYellow.text = "6"
        
        # create the speed fields for a Red Flash Signal
        self.locoSpeedRedFlash = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedRedFlash.setToolTipText("Red Flash Speed is a number from 1 to 100%")
        self.locoSpeedRedFlash.actionPerformed = self.whenLocoChanged
        self.locoSpeedRedFlash.focusLost = self.whenLocoChanged
        self.locoSpeedRedFlash.text = "20"
        
        # create the physical speed field for a Red Flash Signal
        self.locoRateRedFlash = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateRedFlash.setToolTipText("Throttle as Distance/Second, approaching red flash signal")
        self.locoRateRedFlash.actionPerformed = self.whenLocoChanged
        self.locoRateRedFlash.focusLost = self.whenLocoChanged
        self.locoRateRedFlash.text = "0"
        
        # create the speed fields for a Red Signal
        self.locoSpeedRed = javax.swing.JTextField(sizeSpeedField)    # sized to hold 5 characters
        self.locoSpeedRed.setToolTipText("Red Speed is a number from 1 to 100%, creep to Red Signal")
        self.locoSpeedRed.actionPerformed = self.whenLocoChanged
        self.locoSpeedRed.focusLost = self.whenLocoChanged
        self.locoSpeedRed.text = "15"
        
        # create the physical speed field for a Red Signal
        self.locoRateRed = javax.swing.JTextField(sizeRateField)    # sized to hold 5 characters
        self.locoRateRed.setToolTipText("Throttle as Distance/Second, approaching red signal")
        self.locoRateRed.actionPerformed = self.whenLocoChanged
        self.locoRateRed.focusLost = self.whenLocoChanged
        self.locoRateRed.text = "3"
        
        # create the distance field for a Red Signal
        self.locoDistanceRedStop = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoDistanceRedStop.setToolTipText("Distance to stop before Red signal, inches")
        self.locoDistanceRedStop.actionPerformed = self.whenLocoChanged
        self.locoDistanceRedStop.focusLost = self.whenLocoChanged
        self.locoDistanceRedStop.text = "10"
        
        # create current speed display
        self.locoSpeed = javax.swing.JLabel()
        self.locoSpeed.text = "0"
        
        # create the starting block field
        self.blockStart = javax.swing.JTextField(10)
        self.blockStart.setToolTipText("Starting Block Name")
        self.blockStart.actionPerformed = self.whenLocoChanged
        self.blockStart.focusLost = self.whenLocoChanged
        
        # create the starting block direction
        self.blockDirection = javax.swing.JCheckBox()
        self.blockDirection.setToolTipText("Starting Block Direction")
        self.blockDirection.actionPerformed = self.whenLocoChanged
        self.blockDirection.focusLost = self.whenLocoChanged
        self.blockDirection.setSelected(True)
        
        # create flag for looking any extra block ahead
        self.blockAhead2 = javax.swing.JCheckBox()
        self.blockAhead2.setSelected(False)
        self.blockAhead2.setToolTipText("for 4 block mode")
        
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

        # load from roster flag
        self.loadFromRoster = javax.swing.JCheckBox()
        self.loadFromRoster.setToolTipText("Load settings from roster entry if found.")
        self.loadFromRoster.setSelected(True)        
        
        # auto-scroll message window flag
        self.autoScroll = javax.swing.JCheckBox()
        self.autoScroll.setToolTipText("Sets message window to auto-scroll")
        self.autoScroll.setSelected(True)        
        
        # create a text area
        self.scrollArea = javax.swing.JTextArea(15, 70)    # define a text area with it's size
        self.msgText("Enter the loco number, direction and addr mode\nSet min and max speed\nEnter block name loco is in\n")
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
        temppanel1a.add(javax.swing.JLabel("Horn:"))
        temppanel1a.add(self.locoHorn)
        
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
        
        self.speedPane.add(javax.swing.JLabel("Indication"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("Throttle"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(" "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("Inch/Sec"), gConstraints)
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
        
        self.speedPane.add(javax.swing.JLabel(" "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(self.greenSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedGreen, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx+ 1
        self.speedPane.add(self.locoRateGreen, gConstraints)
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
        
        self.speedPane.add(javax.swing.JLabel("  "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(self.yellowSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedYellow, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoRateYellow, gConstraints)
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
        
        self.speedPane.add(javax.swing.JLabel("  "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(self.redSignalIcon), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoSpeedRed, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel("%"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoRateRed, gConstraints)
        gConstraints.gridx = 0
        gConstraints.gridy = gConstraints.gridy + 1
        
        self.speedPane.add(javax.swing.JLabel("Load From Roster: "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.loadFromRoster, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.saveToRosterButton, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(" Stopping Distance: "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(self.locoDistanceRedStop, gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        self.speedPane.add(javax.swing.JLabel(" inches "), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        
        # build block info
        temppanel3 = javax.swing.JPanel()
        temppanel3.add(javax.swing.JLabel("Block Starting: "))
        temppanel3.add(self.blockStart)
        temppanel3.add(javax.swing.JLabel(" Eastbound: "))
        temppanel3.add(self.blockDirection)
        temppanel3.add(javax.swing.JLabel(" Check block ahead: "))
        temppanel3.add(self.blockAhead2)
        temppanel3.add(javax.swing.JLabel(" AutoScroll Messages: "))
        temppanel3.add(self.autoScroll)
        temppanel3.add(javax.swing.JLabel(" Stopping Block: "))
        temppanel3.add(self.blockStop)
        
        temppanel4 = javax.swing.JPanel()
        temppanel4.add(javax.swing.JLabel(" Current: "), gConstraints)
        temppanel4.add(self.locoSpeed, gConstraints)
        temppanel4.add(javax.swing.JLabel("% "), gConstraints)
        temppanel4.add(javax.swing.JLabel("Block Status"))
        temppanel4.add(javax.swing.JLabel(" Now:"))
        temppanel4.add(self.blockNow)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.signalNext)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.blockNowLength)
        temppanel4.add(javax.swing.JLabel("in  Next:"))
        temppanel4.add(self.blockNext)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.signalBeyond)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.blockNextLength)
        temppanel4.add(javax.swing.JLabel("in  Beyond:"))
        temppanel4.add(self.blockBeyond)
        temppanel4.add(javax.swing.JLabel(" Length: "))
        temppanel4.add(self.blockBeyondLength)
        temppanel4.add(javax.swing.JLabel("in "))
        
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
        self.methodLocoDistanceRedStop = dist
        return
        
    def updateMemoryWithCurrentSpeed(self, memoryId) :
        mem = jmri.InstanceManager.memoryManagerInstance().provideMemory(memoryId)
        if (mem != None) :
            if (self.currentThrottle != None) :
                mem.setValue(((int)(round(self.currentThrottle.getSpeedSetting() * 100, 0))).toString())
            else :
                mem.setValue("0")
        return
        
    def returnCurrentSpeed(self) :
        if (self.currentThrottle != None) :
            v = (((int)(round(self.currentThrottle.getSpeedSetting() * 100, 0))).toString())
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

