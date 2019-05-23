# This script runs a loco around the track, controlling the speed
# according to signals and following the blocks.
#
# Author: Ken Cameron, copyright 2008
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
#
# Still needing work:
# 1. Improve the method for 'findNextBlock' as it currently uses the
#    direction from the current block. To work for 'physically designed'
#    panels, it would need to focus on the connectivity of the blocks
#    more and less on the direction attributes. It currently fails
#    if a block transitioned more than 90 degrees within a single
#    block.
# 2. Currently the speed change is made as soon as it crosses into
#    the block. So if block ahead is red, it will stop at the
#    entry of the block since there is no way to know how long
#    the block is and how long it might take to stop. RobotThrottle2
#    is the cure for this issue.
# 3. Add option to delay halting if it looses sight of the loco.
#    This may help some layouts where the detection 'blinks' out
#    for a few seconds due to dirt etc... But using the DebounceSensors
#    script cures this type of thing.
# 4. Learn about the neat features the 'operations' data is adding.
#    This would help with knowing lengths of trains, grades of track,
#    etc to aid in controlling how to stop within a block.
#
# Much thanks go to the Medina Railroad Museum who was asked for
# something to help out when they have larger number of visitors
# and few staff but want the trains to run around without taking
# staff to keep them from running into each other. What started
# as a simple request grew into this script. I am planning on
# turning it into real code as a next step.
# http://railroadmuseum.net/
#
# 7/23/2009 - Changes to use system or user names depending on what
#   is available. This was code stolen from the RobotThrottle2 scrip.
#
# 3/20/2009 - Change to deal with overlaping events
#   Thanks to ConrailBill for spotting where I didn't protect against that
#   and his suggestions to provide a simple fix for this. This will also be
#   part of RobotThrottle2.
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

# set up a throttle with the loco address
class LocoThrot(jmri.jmrit.automat.AbstractAutomaton) :
    # initialize variables
    locoAddress = None
    currentBlock = None
    currentDir = jmri.Path.NONE
    currentBlocks = []
    currentNextBlocks = []
    priorBlocks = []
    priorNextBlocks = []
    isRunning = False
    currentThrottle = None
    scriptFrame = None
    listenerBlocks = []
    listenerBlockListeners = []
    listenerSignals = []
    listenerSignalListeners = []
    greenSignalIcon = None
    yellowSignalIcon = None
    redSignalIcon = None
    darkSignalIcon = None
    unknownSignalIcon = None
    didWeMoveCounter = 0

    def init(self):
        self.msgText("Getting throttle - ") #add text to scroll field
        number = int(self.locoAddress.text)
        dir = self.locoLong.isSelected()
        self.throttle = self.getThrottle(number, dir)
        self.currentThrottle = self.throttle
        if (self.throttle == None) :
           self.msgText("Couldn't assign throttle! - Run stopped\n")
           self.doHalt()
        else : 
           self.msgText("got throttle\n")
           self.throttle.setIsForward(self.locoForward.isSelected())
           self.throttle.setF0(self.locoHeadlight.isSelected())
           self.throttle.setF1(self.locoBell.isSelected())
        return
        
    def handle(self):
        #self.msgText("handle begin:.\n")
        #self.didWeMove(None)
        #self.msgText("handle done\n")
        self.waitMsec(5000)
        return 0 #continue if 1, run once if 0
    
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
    def callBackFromChange(self, event) :
        #self.msgText("callBackFromChange...start routine...counter = " + self.didWeMoveCounter.toString() + " ...incrementing counter\n")
        self.didWeMoveCounter = self.didWeMoveCounter + 1
        if (self.didWeMoveCounter == 1) :
           #self.msgText("callbackFromChange - counter = 1 ... calling didWeMoveCounterCheck routine\n")
           self.didWeMoveCounterCheck(None)
        return
        
    # This handles tracking how many overlapping events happened
    #   and insures we run the didWeMove the right number of times
    def didWeMoveCounterCheck(self, event) :
        #self.msgText("didWeMoveCounterCheck: start of routine...counter = " + self.didWeMoveCounter.toString() + "\n")
        while (self.didWeMoveCounter > 0) :
             #self.msgText("didWeMoveCounterCheck: non-zero counter - calling didWeMove\n")
             self.didWeMove(None)
             self.didWeMoveCounter = self.didWeMoveCounter - 1
             #self.msgText("didWeMoveCounterCheck: decremented counter down to " + self.didWeMoveCounter.toString() + "\n")
        #self.msgText("didWeMoveCounterCheck: end of routine\n")
        return
 
    # figure out if we moved and where
    #   this is the real core of the beast
    def didWeMove(self, event) :
        if (self.isRunning == False) :
            #self.msgText("didWeMove called while isRunning was false\n")
            return
        #if (self.currentBlock != None) :
            #self.msgText("Current block: " + self.giveBlockName(self.currentBlock) + "\n")
        newCurrentBlocks = self.findCurrentBlocks()
        if (len(newCurrentBlocks) == 0) :
            self.msgText("Can't find loco!! Doing halt!!")
            self.doHalt()
        # new current block must be farthest connected to current block in current direction chain
        oldCurrent = self.currentBlock
        tryBlock = self.currentBlock
        newCurrent = None
        giveUpTimer = 0
        while (giveUpTimer < 10) :
            giveUpTimer = giveUpTimer + 1
            newCurrent = self.findNewCurrentBlock(tryBlock, newCurrentBlocks, self.currentDir)
            if (newCurrent == None) :
                newBlockText = "None"
            else :
                newBlockText = self.giveBlockName(newCurrent)
            self.msgText("try " + giveUpTimer.toString() + " " + self.giveBlockName(tryBlock) + " " + newBlockText + "\n")
            if ((newCurrent == tryBlock) or (newCurrent == None)) :
                break
            else :
                tryBlock = newCurrent
        if tryBlock != None :
            self.blockNow.text = " "
            self.blockNext.text = " "
            self.blockBeyond.text = " "
            self.currentBlock = tryBlock
            self.blockStart.text = self.giveBlockName(tryBlock)
            self.blockNow.text = self.giveBlockName(tryBlock)
            self.nextBlock = self.findNextBlock(self.currentBlock)
            self.testAddBlockListener(self.currentBlock)
            if (self.nextBlock != None) :
                self.blockNext.text = self.giveBlockName(self.nextBlock)
                self.beyondBlock = self.findNextBlock(self.nextBlock)
                self.testAddBlockListener(self.nextBlock)
                if (self.beyondBlock != None) :
                    self.blockBeyond.text = self.giveBlockName(self.beyondBlock)
                    self.testAddBlockListener(self.beyondBlock)
            self.priorBlock = oldCurrent
            self.priorBlocks = self.currentBlocks
        # find next block from currentBlock
        if (self.currentBlock != None) :
            if (self.blockAhead2.isSelected() == False) :
                self.msgText("3 block test: " + self.giveBlockName(self.currentBlock) + "\n")
                # if only looking ahead to next block
                self.findNewSpeed(self.currentBlock, self.nextBlock)
            else :
                self.msgText("4 block test: " + self.giveBlockName(self.nextBlock) + "\n")
                # if looking ahead beyond block (4 block system)
                if (self.beyondBlock == None) :
                    self.msgText("failed to find next block: " + self.giveBlockName(self.nextBlock) + "\n")
                    self.doHalt()
                else :
                    self.findNewSpeed(self.nextBlock, self.beyondBlock)
            nearSig = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(self.currentBlock, self.nextBlock)
            farSig = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(self.nextBlock, self.beyondBlock)
            if (self.isRunning and nearSig != None) :
                self.signalNext.setIcon(self.cvtAppearanceIcon(nearSig))
                self.signalNextText.text = self.cvtAppearanceText(nearSig)
                self.testAddSignalListener(nearSig)
                if (self.isRunning and farSig != None) :
                    self.signalBeyond.setIcon(self.cvtAppearanceIcon(farSig))
                    self.signalBeyondText.text = self.cvtAppearanceText(nearSig)
                    self.testAddSignalListener(farSig)
                else :
                    self.msgText("No signal found for far block: " + self.giveBlockName(self.beyondBlock) + "\n")
                    if (self.isRunning) :
                        self.doHalt()
            else :
                self.msgText("No signal found for next block: " + self.giveBlockName(self.nextBlock) + "\n")
                if (self.isRunning) :
                    self.doHalt()
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
            bl.setCallBack(self.callBackFromChange)
            bk.addPropertyChangeListener(bl)
            self.listenerBlocks.append(bk)
            self.listenerBlockListeners.append(bl)
        return

    # see if signal is in the listenerSignals, add listener if not
    def testAddSignalListener(self, sig) :
        if (self.isInList(sig, self.listenerSignals) == False) :
            # isn't in list, setup listener and add to list
            sl = self.SignalListener()
            sl.setCallBack(self.callBackFromChange)
            sig.addPropertyChangeListener(sl)
            self.listenerSignals.append(sig)
            self.listenerSignalListeners.append(sl)
        return

    # release all listeners
    #   important to reduce the load when using multiple throttles
    def releaseAllListeners(self, event) :
        i = 0
        while(len(self.listenerBlockListeners) > i) :
            b = self.listenerBlocks[i]
            self.msgText("releasing listener for block " + self.giveBlockName(b) + "\n")
            b.removePropertyChangeListener(self.listenerBlockListeners[i])
            i = i + 1
        i = 0
        while(len(self.listenerSignalListeners) > i) :
            s = self.listenerSignals[i]
            self.msgText("releasing listener for signal " + self.giveSignalName(s) + "\n")
            s.removePropertyChangeListener(self.listenerSignalListeners[i])
            i = i + 1
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
                                #self.msgText("findNewCurrentBlock not in cList: " + self.giveBlockname(c) + "\n")
                    if (nBlock != None) :
                        break
                #else :
                    #self.msgText("findNewCurrentBlock path not traversable: " + self.giveBlockName(cBlock) + " to " + self.giveBlockname(pB) + "\n")
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
                    self.msgText("Failed to find next block\n")
                    self.doHalt()
                else :
                    self.msgText("looking for signal between " + self.giveBlockName(cBlock) + " and " + self.giveBlockName(nBlock) + "\n")
                    s = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(cBlock, nBlock)
                    if (s != None) :
                        self.msgText("Found signal: " + self.giveSignalName(s) + " displaying: " + self.cvtAppearanceText(s) + "\n")
                        self.speedFromAppearance(s)
                    else :
                        self.msgText("Failed finding signal!\n")
                        self.doHalt()
        return
    
    # set speed from signal appearance
    def speedFromAppearance(self, sig) :
        rep = ""
        if (sig.getAppearance() == RED) :
            rep = rep + "doStop "
            self.doStop()
        if (sig.getAppearance() == FLASHRED) :
            rep = rep + "doStop "
            self.doStop()
        if (sig.getAppearance() == YELLOW) :
            rep = rep + "doSlow "
            self.doSlow()
        if (sig.getAppearance() == FLASHYELLOW) :
            rep = rep + "doSlow "
            self.doSlow()
        if (sig.getAppearance() == GREEN) :
            rep = rep + "doFast "
            self.doFast()
        if (sig.getAppearance() == FLASHGREEN) :
            rep = rep + "doFast "
            self.doFast()
        #self.msgText("Found signal " + self.giveSignalName(sig) + " displaying: " + self.cvtAppearanceText(sig) + " so we will: " + rep + "\n")
        return
        
    # convert signal appearance to english
    def cvtAppearanceText(self, sig) :
        rep = ""
        if (sig.getHeld()) :
            rep = rep + "Held "
        if (sig.getLit()) :
            rep = rep + "Lit "
        if (sig.getAppearance() == RED) :
            rep = rep + "Red "
        elif (sig.getAppearance() == FLASHRED) :
            rep = rep + "Flashing Red "
        elif (sig.getAppearance() == YELLOW) :
            rep = rep + "Yellow "
        elif (sig.getAppearance() == FLASHYELLOW) :
            rep = rep + "Flashing Yellow "
        elif (sig.getAppearance() == GREEN) :
            rep = rep + "Green "
        elif (sig.getAppearance() == FLASHGREEN) :
            rep = rep + "Flashing Green "
        elif (sig.getAppearance() == DARK) :
            rep = rep + "Dark "
        else :
            rep = rep + "Unknown "
        #self.msgText("Found signal " + self.giveSignalName(sig) + " displaying: " + rep + "\n")
        return rep
        
    # convert signal appearance to icon
    def cvtAppearanceIcon(self, sig) :
        rep = self.darkSignalIcon
        if (sig.getLit()) :
            if (sig.getAppearance() == RED) :
                rep = self.redSignalIcon
            elif (sig.getAppearance() == FLASHRED) :
                rep = self.redFlashSignalIcon
            elif (sig.getAppearance() == YELLOW) :
                rep = self.yellowSignalIcon
            elif (sig.getAppearance() == FLASHYELLOW) :
                rep = self.yellowFlashSignalIcon
            elif (sig.getAppearance() == GREEN) :
                rep = self.greenSignalIcon
            elif (sig.getAppearance() == FLASHGREEN) :
                rep = self.greenFlashSignalIcon
            else :
                rep = self.unknownSignalIcon
        #self.msgText("Found signal " + self.giveSignalName(sig) + " displaying: " + rep + "\n")
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
    
    # handle shifting to slow speed
    def doSlow(self):
        if (self.currentThrottle != None) :
            i = int(self.locoSlow.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            self.msgText("doSlow: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoSlow.text
        return
        
    # handle shifting to fast speed
    def doFast(self):
        if (self.currentThrottle != None) :
            i = int(self.locoFast.text) * .01
            self.currentThrottle.setSpeedSetting(i)
            self.msgText("doFast: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoFast.text
        return

    # stopping for normal issues, allows for restarting automaticly
    def doStop(self):
        if (self.currentThrottle != None) :
            self.currentThrottle.setSpeedSetting(0)
            self.msgText("doStop\n")
            self.locoSpeed.text = "0"
            if (self.currentBlock != None) :
                self.blockStart.text = self.giveBlockName(self.currentBlock)
        return
               
    # doHalt is for stopping due to error conditions, won't restart
    def doHalt(self) :
        self.isRunning = False
        if (self.currentThrottle != None) :
            self.currentThrottle.setSpeedSetting(-1)
            self.msgText("doHalt, something was in error!!\n")
            self.locoSpeed.text = "0"
            if (self.currentBlock != None) :
                self.blockStart.text = self.giveBlockName(self.currentBlock)
        self.msgText("*** Run halted ***\n")
        self.stopButton.setEnabled(False)
        self.haltButton.setEnabled(False)
        self.startButton.setEnabled(True)
        return

    # enable the button when OK
    #   this is the real test that we have all the right data and
    #   conditions to allow us to start
    def whenLocoChanged(self, event) : 
        # keep track of whether both fields have been changed
        if (self.isRunning) :
            self.doHalt()
            self.msgText("whenLocoChanged, was running, now stopped\n")
        isOk = True
        startBlock = None
        if (self.locoAddress.text == "") :
            isOk = False
        else :
            self.scriptFrame.setTitle("Run Loco " + self.locoAddress.text)
        if (self.locoSlow.text == "") :
            isOk = False
        if (self.locoFast.text == "") :
            isOk = False
        if (self.blockStart.text == "") :
            isOk = False
        else :
            startBlock = blocks.getBlock(self.blockStart.text)
            if (self.testIfBlockNameValid(self.blockStart.text) == False) :
                self.msgText("Invalid block name: " + self.blockStart.text + " please try again\n")
                isOk = False
            else:
                self.msgText("Block: " + self.blockStart.text + " is " + startBlock.getState().toString() + "\n")
                if (startBlock.getState() != ACTIVE) :
                    self.msgText("Block: " + self.blockStart.text + " is not occupied!\n")
                    isOk = False
        if (isOk) :
            self.startButton.setEnabled(True)
            self.haltButton.setEnabled(True)
            self.testAddBlockListener(blocks.getBlock(self.blockStart.text))
            self.msgText("Enabled Start\n")
        return
            
    # handle the horn button on
    #   this should look for a mouse in type event
    def whenLocoHornOn(self, event) :
        self.whenLocoHorn(event, True)
        return

    # handle the horn button off
    #   this should look for a mouse out (focus lost) type event
    def whenLocoHornOff(self, event) :
        self.whenLocoHorn(event, False)
        return

    def whenLocoHorn(self, event, state) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getF2()
            self.currentThrottle.setF2(state)
            self.msgText("changed horn to: " + state + " was " + wasState + "\n")
        return
    
    # handle the Headlight button
    def whenLocoHeadlight(self, event) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getF0()
            state = self.locoHeadlight.getEnabled()
            self.currentThrottle.setF0(state)
            self.msgText("changed light to: " + state + " was " + wasState + "\n")
        return
    
    # handle the Bell button
    def whenLocoBell(self, event) :
        if (self.currentThrottle != None) :
            wasState = self.currentThrottle.getF1()
            state = self.locoBell.getEnabled()
            self.currentThrottle.setF1(state)
            self.msgText("changed bell to: " + state + " was " + wasState + "\n")
        return
    
    # test for block name
    def testIfBlockNameValid(self, userName) :
        foundStart = False
        b = blocks.getByUserName(userName)
        if (b != None and b.getUserName() == userName) :
            foundStart = True
        else :
            b = blocks.getBySystemName(userName)
            if (b != None and b.getSystemName() == userName) :
                foundStart = True
        return foundStart
        
    # define what button does when clicked and attach
    #   that routine to the button
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
                self.currentBlocks = self.findCurrentBlocks()
                self.priorBlocks = self.currentBlocks
                if (self.blockDirection.isSelected() == True) :
                    self.currentDirection = jmri.Path.EAST
                else :
                    self.currentDirection = jmri.Path.WEST
                self.start()
                self.msgText("Change button states\n")     # add text
                self.stopButton.setEnabled(True)
                self.haltButton.setEnabled(True)
                self.startButton.setEnabled(False)
                self.isRunning = True
                self.didWeMoveCounter = 1
                self.didWeMoveCounterCheck(None)
                if (self.isRunning) :
                    self.msgText("Starting current:" + self.giveBlockName(self.currentBlock) + "\n")
        self.msgText("whenStartButtonClicked, done\n")     # add text
        return
            
    # handle user requested stopping
    def whenStopButtonClicked(self, event):   
        self.msgText("Slow loco to stop\n")     # add text
        self.doStop()
        self.msgText("*** Run stopped ***\n")
        self.stopButton.setEnabled(False)
        self.haltButton.setEnabled(False)
        self.startButton.setEnabled(True)
        self.isRunning = False
        self.didWeMoveCounter = 0
        self.whenLocoChanged(event)
        return
    
    def whenHaltButtonClicked(self, event):   
        self.msgText("Halt loco NOW!\n")     # add text
        self.doHalt()
        self.msgText("*** Run halted ***\n")
        self.stopButton.setEnabled(False)
        self.haltButton.setEnabled(False)
        self.startButton.setEnabled(True)
        self.isRunning = False
        self.whenLocoChanged(event)
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
        for p in pathList :
            blockTest = p.getBlock()
            if (p.checkPathSet()) :
                dirTest = p.getToBlockDirection()
                #self.msgText("findNextBlock path traversable: "  + self.giveBlockName(cB) + " to " + self.giveBlockName(blockTest) + " dirTest: " + jmri.Path.decodeDirection(dirTest) + ":" + dirTest.toString() + " dirFlag: " + jmri.Path.decodeDirection(dirFlag) + ":" + dirFlag.toString() + " result: " + (dirTest & dirFlag).toString() + "\n")
                if (dirTest & dirFlag != 0) :
                    nB = blockTest
                    #self.msgText("findNextBlock Found " + self.giveBlockName(blockTest) + "\n")
                    #break
            #else :
                #self.msgText("findNextBlock path not traversable: " + self.giveBlockName(cB) + " to " + self.giveBlockName(blockTest) + "\n")
        return nB

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
    #   all the GUI stuff
    def setup(self) :
        # start to initialise the GUI
        # create buttons and define action
        self.startButton = javax.swing.JButton("Start the Run")
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
        
        self.testButton = javax.swing.JButton("Test")
        self.testButton.setEnabled(True)           # button starts as grayed out (disabled)
        self.testButton.setToolTipText("run the didWeMove test")
        self.testButton.actionPerformed = self.didWeMove
        
        # address of the loco
        self.locoAddress = javax.swing.JTextField(5)    # sized to hold 5 characters, initially empty
        self.locoAddress.actionPerformed = self.whenLocoChanged   # if user hit return or enter
        self.locoAddress.focusLost = self.whenLocoChanged         # if user tabs away
        self.locoAddress.requestFocusInWindow()
        
        # long/short address flag
        self.locoLong = javax.swing.JCheckBox()
        self.locoLong.setToolTipText("Check to use long address")
        self.locoLong.setSelected(True)
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
        
        # create the speed fields similarly
        self.locoSlow = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoSlow.setToolTipText("Slow Speed is a number from 1 to 100%")
        self.locoSlow.actionPerformed = self.whenLocoChanged
        self.locoSlow.focusLost = self.whenLocoChanged
        self.locoSlow.text = "30"
        
        # create the speed fields similarly
        self.locoFast = javax.swing.JTextField(5)    # sized to hold 5 characters
        self.locoFast.setToolTipText("Fast Speed is a number from 1 to 100%")
        self.locoFast.actionPerformed = self.whenLocoChanged
        self.locoFast.focusLost = self.whenLocoChanged
        self.locoFast.text = "60"
        
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
        
        # create the current block field
        self.blockNow = javax.swing.JLabel()

        # create the current/next signal field
        self.signalNext = javax.swing.JLabel()
        self.signalNextText = javax.swing.JLabel()
        
        # create the next block field
        self.blockNext = javax.swing.JLabel()
        
        # create the next/beyond signal field
        self.signalBeyond = javax.swing.JLabel()
        self.signalBeyondText = javax.swing.JLabel()
        
        # create the beyond block field
        self.blockBeyond = javax.swing.JLabel()

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
        
        temppanel2 = javax.swing.JPanel()
        temppanel2.add(javax.swing.JLabel("Speed Min:"))
        temppanel2.add(self.locoSlow)
        temppanel2.add(javax.swing.JLabel("%"))
        temppanel2.add(javax.swing.JLabel(" Max:"))
        temppanel2.add(self.locoFast)
        temppanel2.add(javax.swing.JLabel("%"))
        temppanel2.add(javax.swing.JLabel(" Current: "))
        temppanel2.add(self.locoSpeed)
        temppanel2.add(javax.swing.JLabel("%"))
        
        temppanel3 = javax.swing.JPanel()
        temppanel3.add(javax.swing.JLabel("Block Starting: "))
        temppanel3.add(self.blockStart)
        temppanel3.add(javax.swing.JLabel(" Eastbound: "))
        temppanel3.add(self.blockDirection)
        temppanel3.add(javax.swing.JLabel(" Check block ahead: "))
        temppanel3.add(self.blockAhead2)
        temppanel3.add(javax.swing.JLabel(" AutoScroll Messages: "))
        temppanel3.add(self.autoScroll)
        
        temppanel4 = javax.swing.JPanel()
        temppanel4.add(javax.swing.JLabel("Block Status"))
        temppanel4.add(javax.swing.JLabel(" Now:"))
        temppanel4.add(self.blockNow)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.signalNext)
        temppanel4.add(javax.swing.JLabel(" Next:"))
        temppanel4.add(self.blockNext)
        temppanel4.add(javax.swing.JLabel(" "))
        temppanel4.add(self.signalBeyond)
        temppanel4.add(javax.swing.JLabel(" Beyond:"))
        temppanel4.add(self.blockBeyond)
        
        butPanel = javax.swing.JPanel()
        butPanel.add(self.startButton)
        butPanel.add(self.stopButton)
        butPanel.add(self.testButton)
        butPanel.add(self.haltButton)

        # get other setup things

        self.greenSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-green-short.gif", "GreenCabSignal")
        self.greenSignalIcon.setRotation(1, temppanel4)
        self.greenFlashSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-flashgreen-short.gif", "GreenFlashCabSignal")
        self.greenFlashSignalIcon.setRotation(1, temppanel4)
        self.yellowSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-yellow-short.gif", "YellowCabSignal")
        self.yellowSignalIcon.setRotation(1, temppanel4)
        self.yellowFlashSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-flashyellow-short.gif", "YellowFlashCabSignal")
        self.yellowFlashSignalIcon.setRotation(1, temppanel4)
        self.redSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-red-short.gif", "RedCabSignal")
        self.redSignalIcon.setRotation(1, temppanel4)
        self.redFlashSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-flashred-short.gif", "RedFlashCabSignal")
        self.redFlashSignalIcon.setRotation(1, temppanel4)
        self.darkSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/searchlights/right-dark-short.gif", "DarkCabSignal")
        self.darkSignalIcon.setRotation(1, temppanel4)
        self.unknownSignalIcon = jmri.jmrit.catalog.NamedIcon("resources/icons/misc/Question-black.gif", "UnknownCabSignal")
        
        # Put contents in frame and display
        self.scriptFrame.contentPane.add(temppanel1)
        self.scriptFrame.contentPane.add(temppanel1a)
        self.scriptFrame.contentPane.add(temppanel2)
        self.scriptFrame.contentPane.add(temppanel3)
        self.scriptFrame.contentPane.add(temppanel4)
        self.scriptFrame.contentPane.add(srcollField)
        self.scriptFrame.contentPane.add(butPanel)
        self.scriptFrame.pack()
        self.scriptFrame.show()
        return
        
# create one of these
a = LocoThrot()
a.setup()
