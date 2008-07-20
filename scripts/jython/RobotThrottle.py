# This script runs a loco around the track, controlling the speed
# according to signals and following the blocks.
#
# Author: Ken Cameron, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.2 $
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
# 6. It is expected that usernames are added for all blocks and signals as
#    those are displayed by the script in the RobotThrottle window.
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
#    the block is and how long it might take to stop.
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

import java
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

    def init(self):
        self.msgText("Getting throttle - ") #add text to scroll field
        number = int(self.locoAddress.text)
        dir = self.locoLong.isSelected()
        self.throttle = self.getThrottle(number, dir)
        self.currentThrottle = self.throttle
        if (self.throttle == None) :
           self.msgText("Couldn't assign throttle! - Run stopped\n")
           doStop()
        else : 
           self.msgText("got throttle\n")
           self.throttle.setIsForward(self.locoForward.isSelected())
           self.throttle.setF0(self.locoHeadlight.isSelected())
           self.throttle.setF1(self.locoBell.isSelected())
        return
        
    def handle(self):
        #self.msgText("handle begin:.\n")
        self.didWeMove(None)
        #self.msgText("handle done\n")
        self.waitMsec(5000)
        return 0 #continue if 1, run once if 0
    
    # figure out if we moved and where
    def didWeMove(self, event) :
        if (self.isRunning == False) :
            #self.msgText("didWeMove called while isRunning was false\n")
            return
        #if (self.currentBlock != None) :
            #self.msgText("Current block: " + self.currentBlock.getUserName() + "\n")
        newCurrentBlocks = self.findCurrentBlocks()
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
                newBlockText = newCurrent.getUserName()
            self.msgText("try " + giveUpTimer.toString() + " " + tryBlock.getUserName() + " " + newBlockText + "\n")
            if ((newCurrent == tryBlock) or (newCurrent == None)) :
                break
            else :
                tryBlock = newCurrent
        if tryBlock != None :
            self.blockNow.text = " "
            self.blockNext.text = " "
            self.blockBeyond.text = " "
            self.currentBlock = tryBlock
            self.blockStart.text = tryBlock.getUserName()
            self.blockNow.text = tryBlock.getUserName()
            self.nextBlock = self.findNextBlock(self.currentBlock)
            self.testAddBlockListener(self.currentBlock)
            if (self.nextBlock != None) :
                self.blockNext.text = self.nextBlock.getUserName()
                self.beyondBlock = self.findNextBlock(self.nextBlock)
                self.testAddBlockListener(self.nextBlock)
                if (self.beyondBlock != None) :
                    self.blockBeyond.text = self.beyondBlock.getUserName()
                    self.testAddBlockListener(self.beyondBlock)
            self.priorBlock = oldCurrent
            self.priorBlocks = self.currentBlocks
        # find next block from currentBlock
        if (self.currentBlock != None) :
            if (self.blockAhead2.isSelected() == False) :
                self.msgText("3 block test: " + self.currentBlock.getUserName() + "\n")
                # if only looking ahead to next block
                self.findNewSpeed(self.currentBlock, self.nextBlock)
            else :
                self.msgText("4 block test: " + self.nextBlock.getUserName() + "\n")
                # if looking ahead beyond block (4 block system)
                if (self.beyondBlock == None) :
                    self.msgText("failed to find next block: " + self.nextBlock.getUserName() + "\n")
                else :
                    self.findNewSpeed(self.nextBlock, self.beyondBlock)
            nearSig = jmri.InstanceManager.layoutBlockManagerInstance().getFacingSignalHead(self.currentBlock, self.nextBlock)
            farSig = jmri.InstanceManager.layoutBlockManagerInstance().getFacingSignalHead(self.nextBlock, self.beyondBlock)
            self.signalNext.setIcon(self.cvtAppearanceIcon(nearSig))
            self.signalNextText.text = self.cvtAppearanceText(nearSig)
            self.signalBeyond.setIcon(self.cvtAppearanceIcon(farSig))
            self.signalBeyondText.text = self.cvtAppearanceText(nearSig)
            self.testAddSignalListener(nearSig)
            self.testAddSignalListener(farSig)
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
            bl.setCallBack(self.didWeMove)
            bk.addPropertyChangeListener(bl)
            self.listenerBlocks.append(bk)
            self.listenerBlockListeners.append(bl)
        return

    # see if signal is in the listenerSignals, add listener if not
    def testAddSignalListener(self, sig) :
        if (self.isInList(sig, self.listenerSignals) == False) :
            # isn't in list, setup listener and add to list
            sl = self.SignalListener()
            sl.setCallBack(self.didWeMove)
            sig.addPropertyChangeListener(sl)
            self.listenerSignals.append(sig)
            self.listenerSignalListeners.append(sl)
        return

    # release all listeners
    def releaseAllListeners(self, event) :
        i = 0
        while(len(self.listenerBlockListeners) > i) :
            b = self.listenerBlocks[i]
            self.msgText("releasing listener for block " + b.getUserName() + "\n")
            b.removePropertyChangeListener(self.listenerBlockListeners[i])
            i = i + 1
        i = 0
        while(len(self.listenerSignalListeners) > i) :
            s = self.listenerSignals[i]
            self.msgText("releasing listener for signal " + s.getUserName() + "\n")
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
                    #self.msgText("findNewCurrentBlock testing for " + jmri.Path.decodeDirection(cDir) + " from " + cBlock.getUserName() + " vs " + pB.getUserName() + " pointing " + jmri.Path.decodeDirection(dirTest) + "\n")
                    if (cDir & dirTest == cDir) :
                        for c in cList :
                            if (c == pB) :
                                nBlock = pB
                                #self.msgText("findNewCurrentBlock found " + pB.getUserName() + "\n")
                                break
                            #else :
                                #self.msgText("findNewCurrentBlock not in cList: " + c.getUserName() + "\n")
                    if (nBlock != None) :
                        break
                #else :
                    #self.msgText("findNewCurrentBlock path not traversable: " + cBlock.getUserName() + " to " + pB.getUserName() + "\n")
        return nBlock

    # figure out signal names and decide speeds
    def findNewSpeed(self, cBlock, nBlock) :
        if (cBlock == None) :
            if (nBlock == None) :
                self.msgText("Failed to find either blocks\n")
            else :
                self.msgText("Failed to find current block\n")
        else :
            if (nBlock == None) :
                self.msgText("Failed to find next block\n")
            else :
                self.msgText("looking for signal between " + cBlock.getUserName() + " and " + nBlock.getUserName() + "\n")
                s = jmri.InstanceManager.layoutBlockManagerInstance().getFacingSignalHead(cBlock, nBlock)
                if (s != None) :
                    self.msgText("Found signal: " + s.getUserName() + " displaying: " + self.cvtAppearanceText(s) + "\n")
                    self.speedFromAppearance(s)
                else :
                    self.msgText("Failed finding signal!\n")
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
        #self.msgText("Found signal " + sig.getUserName() + " displaying: " + self.cvtAppearanceText(sig) + " so we will: " + rep + "\n")
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
        #self.msgText("Found signal " + sig.getUserName() + " displaying: " + rep + "\n")
        return rep
        
    # convert signal appearance to english
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
        #self.msgText("Found signal " + sig.getUserName() + " displaying: " + rep + "\n")
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
    
    def doSlow(self):
        if (self.currentThrottle != None) :
            i = int(self.locoSlow.text) * 0.01
            self.currentThrottle.setSpeedSetting(i)
            self.msgText("doSlow: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoSlow.text
        return
        
    def doFast(self):
        if (self.currentThrottle != None) :
            i = int(self.locoFast.text) * .01
            self.currentThrottle.setSpeedSetting(i)
            self.msgText("doFast: " + i.toString() + "\n")
            self.locoSpeed.text = self.locoFast.text
        return
               
    def doStop(self):
        if (self.currentThrottle != None) :
            self.currentThrottle.setSpeedSetting(0)
            self.msgText("doStop\n")
            self.locoSpeed.text = "0"
            if (self.currentBlock != None) :
                self.blockStart.text = self.currentBlock.getUserName()
        return
               
    # enable the button when OK
    def whenLocoChanged(self, event) : 
        # keep track of whether both fields have been changed
        if (self.isRunning) :
            self.doStop()
            self.isRunning = False
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
        if (isOk) :
            self.enterButton.setEnabled(True)
            self.testAddBlockListener(blocks.getBlock(self.blockStart.text))
            self.msgText("Enabled Start\n")
        return
            
    # handle the horn button on
    def whenLocoHornOn(self, event) :
        self.whenLocoHorn(event, True)
        return

    # handle the horn button off
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
            self.msgText("changed horn to: " + state + " was " + wasState + "\n")
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
                self.currentBlocks = self.findCurrentBlocks()
                self.priorBlocks = self.currentBlocks
                if (self.blockDirection.isSelected() == True) :
                    self.currentDirection = jmri.Path.EAST
                else :
                    self.currentDirection = jmri.Path.WEST
                self.start()
                self.msgText("Change button states\n")     # add text
                self.stopButton.setEnabled(True)
                self.enterButton.setEnabled(False)
                self.isRunning = True
                self.didWeMove(None)
                self.msgText("Starting current:" + self.currentBlock.getUserName() + "\n")
        self.msgText("whenStartButtonClicked, done\n")     # add text
        return
            
    def whenStopButtonClicked(self, event):   
        self.msgText("Slow loco to stop\n")     # add text
        self.doStop()
        self.msgText("*** Run stopped ***\n")
        self.stopButton.setEnabled(False)
        self.enterButton.setEnabled(True)
        self.isRunning = False
        self.whenLocoChanged(event)
        return
    
    def findCurrentBlocks(self) :
        # search the block list for the matching loco
        blockList = []
        for x in blocks.getSystemNameList().toArray() :
            b = blocks.getBySystemName(x)
            if (b.getValue() == self.locoAddress.text) :
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
                #self.msgText("findNextBlock path traversable: "  + cB.getUserName() + " to " + blockTest.getUserName() + " dirTest: " + jmri.Path.decodeDirection(dirTest) + ":" + dirTest.toString() + " dirFlag: " + jmri.Path.decodeDirection(dirFlag) + ":" + dirFlag.toString() + " result: " + (dirTest & dirFlag).toString() + "\n")
                if (dirTest & dirFlag != 0) :
                    nB = blockTest
                    #self.msgText("findNextBlock Found " + blockTest.getUserName() + "\n")
                    #break
            #else :
                #self.msgText("findNextBlock path not traversable: " + cB.getUserName() + " to " + blockTest.getUserName() + "\n")
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
    def setup(self) :
        # start to initialise the GUI
        # create buttons and define action
        self.enterButton = javax.swing.JButton("Start the Run")
        self.enterButton.setEnabled(False)           # button starts as grayed out (disabled)
        self.enterButton.actionPerformed = self.whenStartButtonClicked
        
        self.stopButton = javax.swing.JButton("Stop")
        self.stopButton.setEnabled(False)           # button starts as grayed out (disabled)
        self.stopButton.setToolTipText("Stops the run - there is a delay as the loco slows")
        self.stopButton.actionPerformed = self.whenStopButtonClicked
        
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
        self.locoHorn.setToolTipText("Controls loco bell")
        self.locoHorn.focusGained = self.whenLocoHornOn
        self.locoHorn.focusLost = self.whenLocoHornOff
        
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
        butPanel.add(self.enterButton)
        butPanel.add(self.stopButton)
        butPanel.add(self.testButton)

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
