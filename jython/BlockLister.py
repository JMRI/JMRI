# This script looks up a block and lists all related objects.
#
# Author: Ken Cameron, copyright 2010
# Part of the JMRI distribution

import jmri

import java
import java.awt
import java.awt.event
import javax.swing

HighDebug = 3
MediumDebug = 2
LowDebug = 1
NoneDebug = 0

# set up a swing window to pick and describe blocks
class BlockLister(jmri.jmrit.automat.AbstractAutomaton) :
    # initialize variables
    currentBlock = None
    askFinishLookupButton = False
    debugLevel = HighDebug
    
    def init(self):
        #print("start begin:.\n")
        self.setName("Block Lister")
        self.setup()
        #print("start end:.\n")
        return
        
    def handle(self):
        #self.msgText("handle begin:.\n")
        self.waitMsec(1000)
        if (self.askFinishLookupButton) :
            self.doFinishLookupButton()
            self.askFinishLookupButton = False
        #self.msgText("handle done\n")
        return 1 #continue if 1, run once if 0
            
    # show what level of signal appearance causes a halt on dropping signal
    def returnSignalAppearanceHalt(self) :
        return(str(self.haltOnSignalHeadAppearance))
        
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
                
    # return userName if available, else systemName
    def giveMastName(self, mast) :
        if (mast == None) :
            return 'None'
        else :
            if ((mast.getUserName() == None) or (mast.getUserName() == '')) :
                return mast.getSystemName()
            else :
                return mast.getUserName()
         
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
        
    # convert block state to english
    def cvtBlockStateToText(self, state) :
        rep = ""
        if (state == jmri.Block.OCCUPIED) :
            rep = rep + "Occupied "
        if (state == jmri.Block.UNOCCUPIED) :
            rep = rep + "Unoccupied "
        return rep
        
    # convert block curvature to english
    def cvtCurvatureToText(self, curve) :
        rep = ""
        if (curve == jmri.Block.NONE) :
            rep = rep + "None "
        if (curve == jmri.Block.GRADUAL) :
            rep = rep + "Gradual "
        if (curve == jmri.Block.TIGHT) :
            rep = rep + "Tight "
        if (curve == jmri.Block.SEVERE) :
            rep = rep + "Severe "
        return rep
        
    # convert signal appearance to english
    def cvtSignalToText(self, sig) :
        rep = ""
        sigState = sig.getAppearance()
        if (sig.getHeld()) :
            rep = rep + "Held "
        if (sig.getLit()) :
            rep = rep + "Lit "
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
        #self.msgText("cvtSignalToText: " + self.giveSignalName(sig) + " displaying: " + rep + "\n")
        return rep
        
    # convert mast appearance to english
    def cvtMastToText(self, mast) :
        rep =  ""
        sys = mast.getSignalSystem()
        aspect = mast.getAspect()
        validAspects = mast.getValidAspects()
        if (mast.getHeld()) :
            rep = rep + "Held "
        if (mast.getLit()) :
            rep = rep + "Lit "
        if (aspect != None) :
            rep = rep + aspect + " "
        if (validAspects.size() > 0) :
            rep = rep + "\nValid Aspects: " + validAspects.toString() + " "
        else :
            rep = rep + "No Valid Aspects! "
        rep = rep + "System: " + sys.getUserName()
        return rep
        
    # test for block name
    def testIfBlockNameValid(self, userName) :
        foundStart = False
        b = blocks.getByUserName(userName)
        if (b != None and self.giveBlockName(b) == userName) :
            foundStart = True
        return foundStart
        
    # define what button does when clicked and attach that routine to the button
    def whenLookupButtonClicked(self, event) :
        if (self.testIfBlockNameValid(self.blockValue.text) == False) :
            self.msgText("Invalid block name: " + self.blockValue.text + " please try again\n")
        else :
            c = blocks.getBlock(self.blockValue.text)
            if (c == None) :
                self.msgText("Invalid block name: " + self.blockValue.text + " please try again\n")
            else :
                self.currentBlock = c
                self.askFinishLookupButton = True
        #self.msgText("whenLookupButtonClicked, done\n")     # add text
        return
        
    # split out so it can happen from the handle() routine
    def doFinishLookupButton(self) :
        self.displayBlockData(self.currentBlock)
        self.displayPathData(self.currentBlock)
        self.displaySegmentData(self.currentBlock)
        self.msgText("\n")
        return
            
    def displayBlockData(self, b) :
        if (b.getValue() != None) :
            self.msgText("Block Value: " + b.getValue() + "\n")
        if (b.getSensor() != None) :
            self.msgText("Block Sensor: " + self.giveSensorName(b.getSensor()) + "\n")
        if (b.getCurvature() != None) :
            self.msgText("Block Curvature: " + self.cvtCurvatureToText(b.getCurvature()) + "\n")
        if (b.getLengthIn() != None) :
            self.msgText("Block Length: " + str(b.getLengthIn()) + "\n")
        if (b.getDirection() != None) :
            self.msgText("Block Direction: " + jmri.Path.decodeDirection(b.getDirection()) + "\n")
        #if (b.getWorkingDirection() != None) :
        #    self.msgText("Block Working Direction: " + jmri.Path.decodeDirection(b.getWorkingDirection()) + "\n")
        if (b.getState() != None) :
            self.msgText("Block State: " + self.cvtBlockStateToText(b.getState()) + "\n")
        if (b.getBlockSpeed() != "") :
            self.msgText("Block Speed: " + b.getBlockSpeed() + "\n")
        if (b.getSpeedLimit() != "") :
            self.msgText("Block Speed Limit: " + str(b.getSpeedLimit()) + "\n")
        if (b.getPermissiveWorking()) :
            self.msgText("Block Permissive: True\n")
        else :
            self.msgText("Block Permissive: False\n")
        return

    def displayPathData(self, block) :
        pathList = block.getPaths()
        self.msgText("Paths from " + self.giveBlockName(block) + ": " + str(len(pathList)) + "\n")
        for p in pathList :
            blockTest = p.getBlock()
            dirTest = p.getToBlockDirection()
            if (p.checkPathSet()) :
                self.msgText("Path traversable: "  + self.giveBlockName(self.currentBlock) + " to " + self.giveBlockName(blockTest) + " " + jmri.Path.decodeDirection(dirTest) + ":" + str(dirTest) + "\n")
            else :
                self.msgText("Path not traversable: " + self.giveBlockName(self.currentBlock) + " to " + self.giveBlockName(blockTest) + " " + jmri.Path.decodeDirection(dirTest) + ":" + str(dirTest) + "\n")
            sig = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalHead(block, blockTest)
            if (sig != None) :
                self.msgText("Path from " + self.giveBlockName(block) + " to " + self.giveBlockName(blockTest) + " signal: " + self.giveSignalName(sig) + " aspect: " + self.cvtSignalToText(sig) + "\n")
            else :
                self.msgText("Path from " + self.giveBlockName(block) + " to " + self.giveBlockName(blockTest) + " has no signal headsl!\n")
            mast = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getFacingSignalMast(block, blockTest)
            if (mast != None) :
                self.msgText("Path from " + self.giveBlockName(block) + " to " + self.giveBlockName(blockTest) + " mast: " + self.giveMastName(mast) + " aspect: " + self.cvtMastToText(mast) + "\n")
            else :
                self.msgText("Path from " + self.giveBlockName(block) + " to " + self.giveBlockName(blockTest) + " has no signal masts!\n")
        return
        
    def displaySegmentData(self, b) :
        PanelMenu = jmri.InstanceManager.getDefault(jmri.jmrit.display.PanelMenu)
        layout = PanelMenu.getLayoutEditorPanelList()
        for s in layout[0].getTrackSegments():
            #self.msgText("Segment: " + s.getID() + " block: " + s.getBlockName() + "\n")
            if ((s.getBlockName() == b.getSystemName()) or (s.getBlockName() == b.getUserName())) :
                if (s.getLayoutBlock() != None) :
                    self.msgText("Segment: " + s.getId() + " LayoutBlock: " + s.getLayoutBlock().getId() + "\n")
                if (s.isHidden()) :
                    self.msgText("Segment: " + s.getId() + " is hidden.\n")
                if (s.isMainline()) :
                    self.msgText("Segment: " + s.getId() + " is Mainline.\n")
                if (s.isDashed()) :
                    self.msgText("Segment: " + s.getId() + " is dashed.\n")
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
            if (self.f != None) :
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
     
    # handle adding to message window
    def msgText(self, txt) :
        self.scrollArea.append(txt)
        if (self.autoScroll.isSelected() == True) :
            self.scrollArea.setCaretPosition(self.scrollArea.getDocument().getLength())
        return
    
    # setup the user interface
    def setup(self) :
         
        # get other setup things

        # start to initialise the GUI
        
        # create buttons and define action
        self.lookupButton = javax.swing.JButton("Lookup")
        self.lookupButton.setEnabled(True)
        self.lookupButton.actionPerformed = self.whenLookupButtonClicked
        
        # create the starting block field
        self.blockValue = javax.swing.JTextField(10)
        self.blockValue.setToolTipText("Block Name")
        self.blockValue.setName("Block")
        
        # auto-scroll message window flag
        self.autoScroll = javax.swing.JCheckBox()
        self.autoScroll.setToolTipText("Sets message window to auto-scroll")
        self.autoScroll.setSelected(True)        
        
        # create a text area
        self.scrollArea = javax.swing.JTextArea(15, 70)    # define a text area with it's size
        srcollField = javax.swing.JScrollPane(self.scrollArea) # put text area in scroll field
        
        # create a frame to hold the buttons and fields
        # also create a window listener. This is used mainly to remove the property change listener
        # when the window is closed by clicking on the window close button
        w = self.WinListener()
        self.scriptFrame = javax.swing.JFrame("Block Lister")       # argument is the frames title
        self.scriptFrame.contentPane.setLayout(javax.swing.BoxLayout(self.scriptFrame.contentPane, javax.swing.BoxLayout.Y_AXIS))
        self.scriptFrame.addWindowListener(w)
        # put the text field on a line preceded by a label
        
        # build block info
        temppanel3 = javax.swing.JPanel()
        temppanel3.add(javax.swing.JLabel("Block: "))
        temppanel3.add(self.blockValue)
        
        butPanel = javax.swing.JPanel()
        butPanel.add(self.lookupButton)

        # Put contents in frame and display
        self.scriptFrame.contentPane.add(temppanel3)
        self.scriptFrame.contentPane.add(srcollField)
        self.scriptFrame.contentPane.add(butPanel)
        self.scriptFrame.pack()
        self.scriptFrame.show()
        self.isAborting = False
        return
        
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
        
bl = BlockLister()
bl.start()
