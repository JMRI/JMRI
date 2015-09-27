from AutoDispatcher.AutoDispatcher2 import AutoDispatcher
from java.awt import Color
from java.awt import GridLayout
from javax.swing import BorderFactory
from javax.swing import BoxLayout
from javax.swing import JButton
from javax.swing import JCheckBox
from javax.swing import JLabel
from javax.swing import JPanel
from javax.swing import JScrollPane
from jmri.util import JmriJFrame

class ADmainMenu (JmriJFrame):

    # create frame borders
    blackline = BorderFactory.createLineBorder(Color.black)
    spacing = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        
    saveSettingsButton = JButton("Save Settings")
    saveTrainsButton = JButton("Save Trains")
    startButton = JButton("Start")
    stopButton = JButton("Stop")
    pauseButton = JButton("Pause")
    resumeButton = JButton("Resume")
    autoButton = JCheckBox("Auto", True)

    def __init__(self):
        # super.init
        JmriJFrame.__init__(self, "AutoDispatcher " + AutoDispatcher.version)
        self.addWindowListener(ADcloseWindow()) # Handle window closure
        # (see last class at the bottom of file)
        self.contentPane.setLayout(BoxLayout(self.contentPane,
                                   BoxLayout.Y_AXIS))

        # create frame borders
        self.contentPane.setBorder(ADmainMenu.spacing)
        
        # Set a warning at the top of page
        
        temppane = JPanel()
        temppane.setLayout(GridLayout(2, 1))
        l = AutoDispatcher.centerLabel(" WARNING: DO NOT SAVE YOUR PANEL ")
        l.setForeground(Color.red)
        temppane.add(l)
        l = AutoDispatcher.centerLabel(" AFTER RUNNING THIS SCRIPT!!! ")
        l.setForeground(Color.red)
        temppane.add(l)
        self.contentPane.add(temppane)

        # Settings panel
        temppane = JPanel()
        temppane.setBorder(BorderFactory.createTitledBorder(ADmainMenu.blackline,
                           "Layout settings"))
        temppane.setLayout(GridLayout(3, 2))
        
        # create the Preferences button
        self.preferencesButton = JButton("Preferences")
        self.preferencesButton.enabled = False
        self.preferencesButton.actionPerformed = self.whenPreferencesClicked
        temppane.add(self.preferencesButton)
        
        # create the Panel button
        self.panelButton = JButton("Panel")
        self.panelButton.actionPerformed = self.whenPanelClicked
        self.panelButton.enabled = False
        temppane.add(self.panelButton)

        # create the Direction button
        self.directionButton = JButton("Direction")
        self.directionButton.actionPerformed = self.whenDirectionClicked
        self.directionButton.enabled = False
        temppane.add(self.directionButton)

        # create the Sections button
        self.sectionsButton = JButton("Sections")
        self.sectionsButton.actionPerformed = self.whenSectionsClicked
        self.sectionsButton.enabled = False
        temppane.add(self.sectionsButton)
        
        # create the Blocks button
        self.blocksButton = JButton("Blocks")
        self.blocksButton.actionPerformed = self.whenBlocksClicked
        self.blocksButton.enabled = False
        temppane.add(self.blocksButton)
        
        # create the Locations button
        self.locationsButton = JButton("Locations")
        self.locationsButton.enabled = False
        self.locationsButton.actionPerformed = self.whenLocationsClicked
        temppane.add(self.locationsButton)

        self.contentPane.add(temppane)

        temppane = JPanel()
        temppane.setBorder(BorderFactory.createTitledBorder(ADmainMenu.blackline,
                           "Signals"))
        temppane.setLayout(GridLayout(2, 2))

        # create the Speeds button
        self.speedsButton = JButton("Speeds")
        self.speedsButton.actionPerformed = self.whenSpeedsClicked
        self.speedsButton.enabled = False
        temppane.add(self.speedsButton)

        # create the Signal Indications button
        self.indicationsButton = JButton("Indications")
        self.indicationsButton.actionPerformed = self.whenIndicationsClicked
        self.indicationsButton.enabled = False
        temppane.add(self.indicationsButton)

        # create the Signal Types button
        self.signalTypesButton = JButton("Signal Types")
        self.signalTypesButton.actionPerformed = self.whenSignalTypesClicked
        self.signalTypesButton.enabled = False
        temppane.add(self.signalTypesButton)

        # create the Signal Masts button
        self.signalMastsButton = JButton("Signal Masts")
        self.signalMastsButton.actionPerformed = self.whenSignalMastsClicked
        self.signalMastsButton.enabled = False
        temppane.add(self.signalMastsButton)

        self.contentPane.add(temppane)

        # Sounds panel
        temppane = JPanel()
        temppane.setBorder(BorderFactory.createTitledBorder(ADmainMenu.blackline,
                           "Sounds"))
        temppane.setLayout(GridLayout(1, 2))

        # create the List button
        self.soundListButton = JButton("List")
        self.soundListButton.actionPerformed = self.whenSoundListClicked
        self.soundListButton.enabled = False
        temppane.add(self.soundListButton)

        # create the Default button
        self.soundDefaultButton = JButton("Default")
        self.soundDefaultButton.actionPerformed = (
                                                   self.whenSoundDefaultClicked)
        self.soundDefaultButton.enabled = False
        temppane.add(self.soundDefaultButton)

        self.contentPane.add(temppane)
        temppane = JPanel()
        temppane.setBorder(ADmainMenu.spacing)
        temppane.setLayout(GridLayout(1, 1))

        # create the Save Settings button
        ADmainMenu.saveSettingsButton.enabled = False
        ADmainMenu.saveSettingsButton.actionPerformed = (
                                                         self.whenSaveSettingsClicked)
        temppane.add(ADmainMenu.saveSettingsButton)

        self.contentPane.add(temppane)

        # temppane.add(JLabel(""))
        
        # Operations panel
        temppane = JPanel()
        temppane.setBorder(BorderFactory.createTitledBorder(ADmainMenu.blackline,
                           "Operations"))
        temppane.setLayout(GridLayout(3, 2))

        # create the Start button
        ADmainMenu.startButton.actionPerformed = self.whenStartClicked
        ADmainMenu.startButton.enabled = False
        temppane.add(ADmainMenu.startButton)

        # create the Stop button
        ADmainMenu.stopButton.enabled = False
        ADmainMenu.stopButton.actionPerformed = self.whenStopClicked
        temppane.add(ADmainMenu.stopButton)

        # create the Locomotives button
        self.locoButton = JButton("Locomotives")
        self.locoButton.actionPerformed = self.whenLocosClicked
        self.locoButton.enabled = False
        temppane.add(self.locoButton)

        # create the Trains button
        self.trainsButton = JButton("Trains")
        self.trainsButton.actionPerformed = self.whenTrainsClicked
        self.trainsButton.enabled = False
        temppane.add(self.trainsButton)

        # create the Save Trains button
        ADmainMenu.saveTrainsButton.actionPerformed = self.whenSaveTrainsClicked
        ADmainMenu.saveTrainsButton.enabled = False
        temppane.add(ADmainMenu.saveTrainsButton)

        temppane.add(JLabel(""))

        self.contentPane.add(temppane)

        # Emergency panel
        temppane = JPanel()
        temppane.setBorder(BorderFactory.createTitledBorder(ADmainMenu.blackline,
                           "Emergency"))
        temppane.setLayout(GridLayout(1, 2))

        # create the Pause button
        ADmainMenu.pauseButton.enabled = False
        ADmainMenu.pauseButton.actionPerformed = self.whenPauseClicked
        temppane.add(ADmainMenu.pauseButton)

        # create the Resume button
        ADmainMenu.resumeButton.enabled = False
        ADmainMenu.resumeButton.actionPerformed = self.whenResumeClicked
        temppane.add(ADmainMenu.resumeButton)

        self.contentPane.add(temppane)

        if AutoDispatcher.simulation:

            # Simulation panel
            temppane = JPanel()
            temppane.setBorder(BorderFactory.createTitledBorder(
                               ADmainMenu.blackline, "Simulation"))
            temppane.setLayout(GridLayout(1, 2))

            # create the Step button
            self.stepButton = JButton("Step")
            self.stepButton.actionPerformed = self.whenStepClicked
            self.stepButton.enabled = False
            temppane.add(self.stepButton)

            # create the Auto checkbox
            ADmainMenu.autoButton.enabled = False
            temppane.add(ADmainMenu.autoButton)
            self.contentPane.add(temppane)

        # Status message
        temppane = JPanel()
        scrollPane = JScrollPane(AutoDispatcher.statusScroll)
        scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, JPanel())
        temppane.add(scrollPane)
        self.contentPane.add(temppane)

        # Display frame
        self.setLocation(0, 0)
        self.pack()
        self.show()
        
# Main window buttons =================

    # Settings buttons

    # define what Direction button does when clicked
    def whenDirectionClicked(self, event):
        if AutoDispatcher.directionFrame == None:
            AutoDispatcher.directionFrame = ADdirectionFrame()
        else:
            AutoDispatcher.directionFrame.show()

    # define what Panel button does when clicked
    def whenPanelClicked(self, event):
        if AutoDispatcher.panelFrame == None:
            AutoDispatcher.panelFrame = ADpanelFrame()
        else:
            AutoDispatcher.panelFrame.show()
        
    # define what Speeds button does when clicked
    def whenSpeedsClicked(self, event):
        if AutoDispatcher.speedsFrame == None:
            AutoDispatcher.speedsFrame = ADspeedsFrame()
        else:
            AutoDispatcher.speedsFrame.show()
        
    # define what Indications button does when clicked
    def whenIndicationsClicked(self, event):
        if AutoDispatcher.indicationsFrame == None:
            AutoDispatcher.indicationsFrame = ADindicationsFrame()
        else:
            AutoDispatcher.indicationsFrame.show()
        
    # define what Signal Types button does when clicked
    def whenSignalTypesClicked(self, event):
        if AutoDispatcher.signalTypesFrame == None:
            AutoDispatcher.signalTypesFrame = ADsignalTypesFrame()
        else:
            AutoDispatcher.signalTypesFrame.show()
        return

    # define what Signal Masts button does when clicked
    def whenSignalMastsClicked(self, event):
        if AutoDispatcher.signalMastsFrame == None:
            AutoDispatcher.signalMastsFrame = ADsignalMastsFrame()
        else:
            AutoDispatcher.signalMastsFrame.show()
        return

    # define what Sections button does when clicked
    def whenSectionsClicked(self, event):
        if AutoDispatcher.sectionsFrame == None:
            AutoDispatcher.sectionsFrame = ADsectionsFrame()
        else:
            AutoDispatcher.sectionsFrame.show()
 
    # define what Blocks button does when clicked
    def whenBlocksClicked(self, event):
        if AutoDispatcher.blocksFrame == None:
            AutoDispatcher.blocksFrame = ADblocksFrame()
        else:
            AutoDispatcher.blocksFrame.show()

    # define what Locations button does when clicked
    def whenLocationsClicked(self, event):
        if AutoDispatcher.locationsFrame == None:
            AutoDispatcher.locationsFrame = ADlocationsFrame()
        else:
            AutoDispatcher.locationsFrame.show()

    # define what Preferences button does when clicked
    def whenPreferencesClicked(self, event):
        if AutoDispatcher.preferencesFrame == None:
            AutoDispatcher.preferencesFrame = ADpreferencesFrame()
        else:
            AutoDispatcher.preferencesFrame.show()

    # define what Save Settings button does when clicked
    def whenSaveSettingsClicked(self, event):
        # Save to disk
        AutoDispatcher.instance.saveSettings()
        return

    # define what Sound List button does when clicked
    def whenSoundListClicked(self, event):
        if AutoDispatcher.soundListFrame == None:
            AutoDispatcher.soundListFrame = ADsoundListFrame()

    # define what Sound Default button does when clicked
    def whenSoundDefaultClicked(self, event):
        if AutoDispatcher.soundDefaultFrame == None:
            AutoDispatcher.soundDefaultFrame = ADsoundDefaultFrame()

    # Operations buttons

    # define what Start button does when clicked
    def whenStartClicked(self, event):
        # leave the button off
        ADmainMenu.startButton.enabled = False
        AutoDispatcher.instance.start()

    # define what Stop button does when clicked
    def whenStopClicked(self, event):
        AutoDispatcher.loop = False
        # leave the button off
        ADmainMenu.stopButton.enabled = False
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                "Stopping trains. Please wait!")

    # define what Locomotives button does when clicked
    def whenLocosClicked(self, event):
        if AutoDispatcher.locosFrame == None:
            AutoDispatcher.locosFrame = ADlocosFrame()
        else:
            AutoDispatcher.locosFrame.show()

    # define what Trains button does when clicked
    def whenTrainsClicked(self, event):
        if AutoDispatcher.trainsFrame == None:
            AutoDispatcher.trainsFrame = ADtrainsFrame()
        else:
            AutoDispatcher.trainsFrame.show()

    # define what Save Trains button does when clicked
    def whenSaveTrainsClicked(self, event):
        AutoDispatcher.instance.saveTrains()
        return

    # Emergency buttons

    # define what Pause button does when clicked
    def whenPauseClicked(self, event):
        if ADsettings.pauseMode != ADsettings.IGNORE:
            AutoDispatcher.instance.stopAll()
            AutoDispatcher.log("Script paused!")

    # define what Resume button does when clicked
    def whenResumeClicked(self, event):
        if (ADsettings.pauseMode == ADsettings.IGNORE or
            not AutoDispatcher.paused):
            return
        AutoDispatcher.instance.resume()
        AutoDispatcher.log("Script resumed!")

    # Simulation buttons

    # define what Step button does when clicked
    def whenStepClicked(self, event):
        AutoDispatcher.instance.oneStep()

    def enableButtons(self, on):
        if not AutoDispatcher.error:
            ADmainMenu.startButton.enabled = on
            ADmainMenu.stopButton.enabled = not on
            if ADsettings.pauseMode != ADsettings.IGNORE:
                ADmainMenu.pauseButton.enabled = not on
            # Enable/disable simulation buttons
            if AutoDispatcher.simulation:
                self.stepButton.enabled = not on
                ADmainMenu.autoButton.enabled = not on
            self.directionButton.enabled = True
            self.panelButton.enabled = True
            self.speedsButton.enabled = True
            self.indicationsButton.enabled = True
            self.signalTypesButton.enabled = True
            self.signalMastsButton.enabled = True
            self.sectionsButton.enabled = True
            self.blocksButton.enabled = True
            self.locationsButton.enabled = True
            self.preferencesButton.enabled = True
            self.soundListButton.enabled = True
            self.soundDefaultButton.enabled = True
            ADmainMenu.saveSettingsButton.enabled = (
                                                     AutoDispatcher.preferencesDirty and on)
            ADmainMenu.saveTrainsButton.enabled = (AutoDispatcher.trainsDirty
                                                   and on)
            self.locoButton.enabled = True
            self.trainsButton.enabled = True
            AdFrame.enableApply(on)

    # Main window closure handler =================

