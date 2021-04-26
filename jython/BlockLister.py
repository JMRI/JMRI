#
# This script looks up a block and lists all related objects.
#
# Author: Ken Cameron, copyright 2010
# Part of the JMRI distribution
#

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
class BlockLister():
    def __init__(self):
        self.currentBlock = None
        self.setup()

    # show what level of signal appearance causes a halt on dropping signal
    def returnSignalAppearanceHalt(self) :
        return(str(self.haltOnSignalHeadAppearance))

    # return block display name
    def giveBlockName(self, block) :
        return 'None' if block is None else block.getDisplayName()

    # return signal head display name
    def giveSignalName(self, sig) :
        return 'None' if sig is None else sig.getDisplayName()

    # return turnout display name
    def giveTurnoutName(self, to) :
        return 'None' if to is None else to.getDisplayName()

    # return sensor display name
    def giveSensorName(self, sen) :
        return 'None' if sen is None else sen.getDisplayName()

    # return signal mast display name
    def giveMastName(self, mast) :
        return 'None' if mast is None else mast.getDisplayName()

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

    # define what button does when clicked and attach that routine to the button
    def whenLookupButtonClicked(self, event) :
        c = self.blockCombo.getSelectedItem()
        if c is None:
            self.msgText("Select a block, please try again\n")
            return

        self.currentBlock = c
        self.msgText("Block Name: " + self.giveBlockName(self.currentBlock) + "\n")
        self.displayBlockData(self.currentBlock)
        self.displayPathData(self.currentBlock)
        self.displaySegmentData(self.currentBlock)
        self.msgText("***********************************************************************\n")
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
        panels = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
        layouts = panels.getList(jmri.jmrit.display.layoutEditor.LayoutEditor)
        for layout in layouts:
            self.msgText('-- Panel: {} --\n'.format(layout.getTitle()))
            for s in layout.getTrackSegmentViews():
                #self.msgText("Segment: " + s.getID() + " block: " + s.getBlockName() + "\n")
                if ((s.getBlockName() == b.getSystemName()) or (s.getBlockName() == b.getUserName())) :
                    if (s.getLayoutBlock() != None) :
                        self.msgText("Segment: " + s.getId() + " LayoutBlock: " + s.getLayoutBlock().getId() + "\n")
                    if (s.isHidden()) :
                        self.msgText("Segment: " + s.getId() + " is hidden.\n")
                    if (s.getTrackSegment().isMainline()) :
                        self.msgText("Segment: " + s.getId() + " is Mainline.\n")
                    if (s.isDashed()) :
                        self.msgText("Segment: " + s.getId() + " is dashed.\n")
        return

    # handle adding to message window
    def msgText(self, txt) :
        self.scrollArea.append(txt)

    # setup the user interface
    def setup(self) :
        # create buttons and define action
        self.lookupButton = javax.swing.JButton("Lookup")
        self.lookupButton.setEnabled(True)
        self.lookupButton.actionPerformed = self.whenLookupButtonClicked

        # create the starting block field
        self.blockCombo = jmri.swing.NamedBeanComboBox(jmri.InstanceManager.getDefault(jmri.BlockManager))
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(self.blockCombo);

        # create a text area
        self.scrollArea = javax.swing.JTextArea(30, 75)    # define a text area with it's size
        srcollField = javax.swing.JScrollPane(self.scrollArea) # put text area in scroll field

        # create a frame to hold the buttons and fields
        self.scriptFrame = javax.swing.JFrame("Block Lister")       # argument is the frames title
        self.scriptFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
        self.scriptFrame.contentPane.setLayout(javax.swing.BoxLayout(self.scriptFrame.contentPane, javax.swing.BoxLayout.Y_AXIS))
        # put the text field on a line preceded by a label

        # build block selector
        temppanel3 = javax.swing.JPanel()
        temppanel3.add(javax.swing.JLabel("Block: "))
        temppanel3.add(self.blockCombo)

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

BlockLister()
