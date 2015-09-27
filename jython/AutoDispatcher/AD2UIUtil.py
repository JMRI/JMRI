from java.awt import BorderLayout
from java.awt import GridLayout
from java.awt.event import ActionListener
from java.awt.event import WindowAdapter
from javax.swing import BoxLayout
from javax.swing import ButtonGroup
from javax.swing import JButton
from javax.swing import JComboBox
from javax.swing import JLabel
from javax.swing import JPanel
from javax.swing import JRadioButton
from javax.swing import JScrollPane
from javax.swing import JSpinner
from javax.swing import JTextField
from javax.swing import SpinnerNumberModel
from javax.swing.event import ChangeListener
from jmri.util import JmriJFrame

class ADcloseWindow(WindowAdapter):

    # define what window closure does
    # (overrides empty method of WindowAdapter)
    def windowClosing(self, event):
        # Close window
        event.getWindow().dispose()
        # Then stop everything,
        # otherwise the script will continue running
        # until JMRI exits and user will be unable to
        # stop it!
        # Close all other windows
        AdFrame.disposeAll()
        # Make sure ADpowerMonitor listener is removed
        if AutoDispatcher.powerMonitor != None:
            AutoDispatcher.powerMonitor.dispose()
        # Stop background handler (it will take care of stopping other
        # threads, etc.)
        if AutoDispatcher.loop:
            AutoDispatcher.exiting = True
            AutoDispatcher.loop = False
        else:
        # If background handler si not running, allow user to save data
            AutoDispatcher.instance.saveBeforeExit()


    # Our Abstract frame class =================

class AdFrame (JmriJFrame):
    
    framesList = []
    applyEnabled = True

    @staticmethod
    def enableApply(on):
        AdFrame.applyEnabled = on
        for f in AdFrame.framesList:
            f.applyButton.enabled = on
    
    @staticmethod
    def disposeAll():
        while len(AdFrame.framesList) > 0:
            AdFrame.framesList[0].dispose()
    
    def __init__(self, title):
        # super.init
        JmriJFrame.__init__(self, title)
        self.setDefaultCloseOperation(JmriJFrame.HIDE_ON_CLOSE)
        self.contentPane.setLayout(BoxLayout(self.contentPane,
                                   BoxLayout.Y_AXIS))
        self.contentPane.setBorder(ADmainMenu.spacing)
        AdFrame.framesList.append(self)
        self.cancelButton = JButton("Cancel")
        self.applyButton = JButton("Apply")
        self.applyButton.enabled = AdFrame.applyEnabled
    
    def dispose(self):
        JmriJFrame.dispose(self)
        AdFrame.framesList.remove(self)
        
    # Direction window =================
    
class ADdirectionFrame (AdFrame):

    directionsSwing = JComboBox(["CCW-CW", "EAST-WEST", "NORTH-SOUTH",
                                "LEFT-RIGHT", "UP-DOWN"])
    selectedDirectionNames = ""
    
    def __init__(self):
        # Create and display Direction window
        # super.init
        AdFrame.__init__(self, "Direction")
        
        temppane = JPanel()
        temppane.setLayout(BoxLayout(temppane, BoxLayout.Y_AXIS))
        temppane1 = JPanel()
        temppane1.setLayout(GridLayout(5, 1))
        temppane1.add(AutoDispatcher.centerLabel(
                      " The script runs trains in two directions. "))
        temppane1.add(AutoDispatcher.centerLabel(
                      " Directions can be assigned a pair of names of your choice "))
        temppane1.add(AutoDispatcher.centerLabel(
                      " i.e. CCW and CW, or EAST and WEST or NORTH and SOUTH, etc. "))
        temppane1.add(JLabel(""))
        temppane.add(temppane1)
        
        temppane1 = JPanel()
        temppane1.setLayout(GridLayout(1, 2))
        temppane1.add(JLabel(" Choose direction names:"))
        ADdirectionFrame.directionsSwing.setSelectedItem(
                                                         ADsettings.directionNames[0] + "-"
                                                         + ADsettings.directionNames[1])
        self.comboListener = ADcomboListener()
        ADdirectionFrame.directionsSwing.addActionListener(self.comboListener)
        temppane1.add(ADdirectionFrame.directionsSwing)
        temppane.add(temppane1)
        
        self.directionQuestion = AutoDispatcher.centerLabel("")
        self.displayDirectionQuestion()
        temppane1 = JPanel()
        temppane1.setLayout(GridLayout(4, 1))
        temppane1.add(AutoDispatcher.centerLabel(
                      " In order to allow the correct identification of "))
        temppane1.add(AutoDispatcher.centerLabel(
                      " directions, choose the start and end sections "))
        temppane1.add(self.directionQuestion)
        temppane1.add(JLabel(""))
        temppane.add(temppane1)
        
        temppane1 = JPanel()
        temppane1.setLayout(GridLayout(1, 2))
        temppane1.add(JLabel("  Start section:"))
        self.ccwStartSwing = JTextField(ADsettings.ccwStart, 6)
        temppane1.add(self.ccwStartSwing)
        temppane1.add(JLabel("  End section:"))
        self.ccwEndSwing = JTextField(ADsettings.ccwEnd, 6)
        temppane1.add(self.ccwEndSwing)
        temppane.add(temppane1)
        self.contentPane.add(temppane)
        
        # Buttons*
        temppane = JPanel()
        temppane.setLayout(BoxLayout(temppane, BoxLayout.X_AXIS))
        
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        temppane.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        temppane.add(self.applyButton)
        self.contentPane.add(temppane)
        
        # Display frame
        self.pack()
        self.show()

    # routine to display direction question
    def displayDirectionQuestion(self):
        ADdirectionFrame.selectedDirectionNames = (
                                                   ADdirectionFrame.directionsSwing.getSelectedItem())
        selected = ADdirectionFrame.selectedDirectionNames[
            :ADdirectionFrame.selectedDirectionNames.find("-")]
        self.directionQuestion.setText(" of a short \"" + selected
                                       + "\" bound route:")

    # Buttons of Direction window =================
    
    # define what Cancel button in Direction Window does when clicked
    def whenCancelClicked(self, event):
        AdFrame.dispose(self)
        AutoDispatcher.directionFrame = None

    # define what Apply button in Direction Window does when clicked
    def whenApplyClicked(self, event):
        selected = ADdirectionFrame.selectedDirectionNames[
            :ADdirectionFrame.selectedDirectionNames.find("-")]
        if selected != ADsettings.directionNames[0]:
            ADsettings.directionNames = [selected,
                ADdirectionFrame.selectedDirectionNames[
                ADdirectionFrame.selectedDirectionNames.find("-") + 1:]]
        ADsettings.ccwStart = self.ccwStartSwing.text
        ADsettings.ccwEnd = self.ccwEndSwing.text
        # Recompute direction along sections
        completion = AutoDispatcher.instance.setDirections()
        self.ccwStartSwing.text = ADsettings.ccwStart
        self.ccwEndSwing.text = ADsettings.ccwEnd
        # Update train directions
        for t in ADtrain.getList():
            t.updateSwing()
        # Force redisplay of other windows with appropriate direction names
        if AutoDispatcher.panelFrame != None:
            AutoDispatcher.panelFrame.setColorLabels()
        if AutoDispatcher.sectionsFrame != None:
            AutoDispatcher.sectionsFrame.reDisplay()
        if AutoDispatcher.blocksFrame != None:
            AutoDispatcher.blocksFrame.reDisplay()
        if AutoDispatcher.trainsFrame != None:
            AutoDispatcher.trainsFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        if completion:
            AutoDispatcher.instance.setSignals()
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Direction changes applied")

    # Combo listener for the Direction frame =================
       
class ADcomboListener(ActionListener):
    # Updates direction names in Directions frame
    def actionPerformed(self, event):
        if (ADdirectionFrame.directionsSwing.getSelectedItem()
            == ADdirectionFrame.selectedDirectionNames):
            return
        AutoDispatcher.directionFrame.displayDirectionQuestion()

    # Panel settings window =================
    
class ADpanelFrame (AdFrame):
    def __init__(self):
        # Create and display Panel window
        # super.init
        AdFrame.__init__(self, "Panel")

        temppane = JPanel()
        temppane.setLayout(BorderLayout())
        temppane.setBorder(ADmainMenu.spacing)
        
        temppane1 = JPanel()
        temppane1.setLayout(BoxLayout(temppane1, BoxLayout.Y_AXIS))
        self.colorGroup = ButtonGroup()
        self.standardColorButton = JRadioButton(
                                                "Keep block colors defined in Layout Editor")
        self.standardColorButton.selected = not ADsettings.useCustomColors
        self.standardColorButton.actionPerformed = (
                                                    self.whenStandardColorsClicked)
        self.colorGroup.add(self.standardColorButton);
        temppane1.add(self.standardColorButton)
        self.customColorButton = JRadioButton(
                                              "Use colors to show sections status")
        self.customColorButton.selected = ADsettings.useCustomColors
        self.customColorButton.actionPerformed = self.whenCustomColorsClicked
        self.colorGroup.add(self.customColorButton);
        temppane1.add(self.customColorButton)
        temppane.add(temppane1, BorderLayout.NORTH);
        
        temppane1 = JPanel()
        temppane1.setBorder(ADmainMenu.spacing)

        temppane2 = JPanel()
        temppane2.setBorder(ADmainMenu.blackline)
        temppane2.setLayout(GridLayout(len(ADsettings.colorTable) + 1, 3))
        temppane2.add(JLabel(" Section"))
        temppane2.add(AutoDispatcher.centerLabel("Color"))

        temppane3 = JPanel()
        temppane3.setLayout(GridLayout(1, 4))
        temppane3.add(AutoDispatcher.centerLabel("R"))
        temppane3.add(AutoDispatcher.centerLabel("G"))
        temppane3.add(AutoDispatcher.centerLabel("B"))
        temppane3.add(JLabel(""))
        temppane2.add(temppane3)

        self.colorLabels = []
        self.colorSwing = []
        self.colorPanes = []
        self.rgb = []
        colorList = ADsettings.colors.keys()
        colorList.append("CUSTOM")
 
        self.colorListener = ADcolorListener()

        for i in range(len(ADsettings.colorTable)):
            self.colorLabels.append(JLabel(""))
            temppane2.add(self.colorLabels[i])
            self.colorSwing.append(JComboBox(colorList))
            c = ADsettings.colorTable[i]
            isCustom = c.startswith("R:")
            if isCustom:
                self.colorSwing[i].setSelectedItem("CUSTOM")
                rgbValues = [int(c[2:5]), int(c[7:10]), int(c[12:])]
            else:
                self.colorSwing[i].setSelectedItem(c)
                rgbValues = [0, 0, 0]
            self.colorSwing[i].setActionCommand(str(i))
            self.colorSwing[i].addActionListener(self.colorListener)
            self.colorSwing[i].enabled = ADsettings.useCustomColors
            temppane2.add(self.colorSwing[i])
            colorPane = JPanel()
            colorPane.setBorder(ADmainMenu.blackline)
            colorPane.setBackground(ADsettings.sectionColor[i])
            self.colorPanes.append(colorPane)
            rgbItem = []
            rgbPane = JPanel()
            rgbPane.setLayout(GridLayout(1, 4))
            rgbListener = ADrgbListener(i)
            for j in range(3):
                rgbItem.append(JSpinner(SpinnerNumberModel(rgbValues[j], 0, 255, 1)))
                tf = rgbItem[j].getEditor().getTextField()
                tf.setColumns(2)
                rgbItem[j].setEnabled(isCustom)
                rgbItem[j].addChangeListener(rgbListener)
                rgbPane.add(rgbItem[j])
            self.rgb.append(rgbItem)
            rgbPane.add(self.colorPanes[i])
            temppane2.add(rgbPane)        
        self.setColorLabels()
        temppane1.add(temppane2)
        temppane.add(temppane1, BorderLayout.CENTER);

        temppane1 = JPanel()
        temppane1.setLayout(BoxLayout(temppane1, BoxLayout.Y_AXIS))
        self.widthGroup = ButtonGroup()
        self.standardWidthButton = JRadioButton(
                                                "Keep track width defined in Layout Editor")
        self.standardWidthButton.selected = not ADsettings.useCustomWidth
        self.widthGroup.add(self.standardWidthButton);
        temppane1.add(self.standardWidthButton)
        self.customWidthButton = JRadioButton(
                                              "Use track width to show blocks occupancy")
        self.customWidthButton.selected = ADsettings.useCustomWidth
        self.widthGroup.add(self.customWidthButton);
        temppane1.add(self.customWidthButton)
        temppane.add(temppane1, BorderLayout.SOUTH);

        self.contentPane.add(temppane)
        
        # Buttons*
        temppane = JPanel()
        temppane.setLayout(BoxLayout(temppane, BoxLayout.X_AXIS))

        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        temppane.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        temppane.add(self.applyButton)
        self.contentPane.add(temppane)
        
        # Display frame
        self.pack()
        self.show()

    # routine to display color labels
    def setColorLabels(self):
        sectionType = ("  empty", 
                       "  in manual mode", 
                       "  occupied by rolling stock ", 
                       "  allocated to " 
                       + ADsettings.directionNames[0] + " train",
                       "  allocated to " 
                       + ADsettings.directionNames[1] + " train",
                       "  occupied by "
                       + ADsettings.directionNames[0] + " train",
                       "  occupied by "
                       + ADsettings.directionNames[1] + " train")
        for i in range(len(self.colorLabels)):
            self.colorLabels[i].setText(sectionType[i])

    # Get input RGB values and convert them to string
    def getRGBinput(self, i):
        c = []
        for j in range(3):
            c.append(self.rgb[i][j].getValue())
        return ADsettings.rgbToString(c)
       
    
    # Buttons of Panel window =================

    # define what Standard Colors button in Panel Window does when clicked
    def whenStandardColorsClicked(self, event):
        for i in range(len(self.colorLabels)):
            self.colorSwing[i].enabled = False
        AutoDispatcher.setPreferencesDirty()

    # define what Custom Colors button in Panel Window does when clicked
    def whenCustomColorsClicked(self, event):
        for i in range(len(self.colorLabels)):
            self.colorSwing[i].enabled = True
        AutoDispatcher.setPreferencesDirty()
    
    # define what Cancel button in Direction Window does when clicked
    def whenCancelClicked(self, event):
        AdFrame.dispose(self)
        AutoDispatcher.panelFrame = None

    # define what Apply button in Panel Window does when clicked
    def whenApplyClicked(self, event):
        for i in range(len(self.colorLabels)):
            c = self.colorSwing[i].getSelectedItem()
            if c == "CUSTOM":
                c = AutoDispatcher.panelFrame.getRGBinput(i)
            ADsettings.colorTable[i] = c
        ADsettings.initColors()
        ADsettings.useCustomColors = self.customColorButton.selected
        ADsettings.useCustomWidth = self.customWidthButton.selected
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Panel changes applied")

    # Listener for the color ComboBox =================       
class ADcolorListener(ActionListener):
    def actionPerformed(self, event):
        i = int(event.getActionCommand())
        c = AutoDispatcher.panelFrame.colorSwing[i].getSelectedItem()
        isCustom = c == "CUSTOM"
        for j in range(3):
            AutoDispatcher.panelFrame.rgb[i][j].setEnabled(isCustom)
        if isCustom:
            c = AutoDispatcher.panelFrame.getRGBinput(i)
        AutoDispatcher.panelFrame.colorPanes[i].setBackground(ADsettings.stringToColor(c))

    # Listener for the rgb text fields =================       
class ADrgbListener(ChangeListener):
    def __init__(self, ind):
        self.i = ind
        
    def stateChanged(self, event):
        c = AutoDispatcher.panelFrame.colorSwing[self.i].getSelectedItem()
        if c != "CUSTOM":
            return
        c = AutoDispatcher.panelFrame.getRGBinput(self.i)
        p = AutoDispatcher.panelFrame.colorPanes[self.i]
        p.setBackground(ADsettings.stringToColor(c))

    # Our Abstract frame with a scroll pane =================

class AdScrollFrame (AdFrame):
    # Subclasses must define methods:
    #   self.createHeader() (output returned in self.header JPanel)
    #   self.createDetail() (output returned in self.detail JPanel)
    #   self.createButtons()  (output returned in self.buttons JPanel)
    def __init__(self, title, firstLine):
        # super.init
        AdFrame.__init__(self, title)
        
        # Create first Line
        if firstLine != None:
            temppane = JPanel()
            temppane.setLayout(BoxLayout(temppane, BoxLayout.X_AXIS))
            temppane.add(firstLine)
            self.contentPane.add(temppane)

        # Create scroll pane
        self.scrollPane = JScrollPane()
        self.firstTime = True
        self.createScroll()
        self.contentPane.add(self.scrollPane)
        
        # Create buttons at bottom
        self.buttons = JPanel()
        self.buttons.setLayout(BoxLayout(self.buttons, BoxLayout.X_AXIS))
        self.createButtons()
        self.contentPane.add(self.buttons)
        
        # Adjust size
        self.adjustSize()

        # Display frame
        self.show()
        
    def createScroll(self):
        # Create header and scroll pane contents
        self.header = JPanel()
        self.createHeader()
        self.detail = JPanel()
        self.createDetail()

        self.scrollSize = self.detail.getPreferredSize()
        if self.header != None:
            #Get panel size
            headerSize = self.header.getPreferredSize()
            # Make sure header and scrollarea have the same width
            if headerSize.width < self.scrollSize.width:
                headerSize.width = self.scrollSize.width
                self.header.setPreferredSize(headerSize)
            elif self.scrollSize.width < headerSize.width:
                self.scrollSize.width = headerSize.width
                self.detail.setPreferredSize(self.scrollSize)
            self.scrollPane.setColumnHeaderView(self.header)
            self.scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, JPanel())

        self.scrollPane.setViewportView(self.detail)
        
        # Adjust size (when updating window - adding or removing speeds)
        frameSize = self.getPreferredSize()
        if self.header != None:
            frameSize.height = self.scrollSize.height + 110
        else:
            frameSize.height = self.scrollSize.height + 95
        self.setSize(frameSize)
        self.firstTime = False

    def reDisplay(self):
        self.createScroll()
        self.adjustSize()

    def adjustSize(self):
        self.pack()
        frameSize = self.getPreferredSize()
        frameSize.width = self.scrollSize.width + 30
        if frameSize.width > AutoDispatcher.screenSize.width:
            frameSize.width = AutoDispatcher.screenSize.width
        self.setSize(frameSize)

    # Speeds window =================
    
