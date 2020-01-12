# Example script to control audio objects with a simple GUI
#
# Author: Matthew Harris, copyright 2010
# Part of the JMRI distribution

import jmri

from java.awt import FlowLayout
from java.awt.event import ItemEvent
from java.util import Hashtable
from javax.swing import Box, BoxLayout, JButton, JComboBox, JPanel, JSlider, JLabel, JSpinner, SpinnerNumberModel
from jmri.util import JmriJFrame
from jmri.util.swing import VerticalLabelUI
from jmri import Audio

class AudioPlayerFrame (JmriJFrame):

    # Define the range for position values
    # By default, this is -10 to +10
    range = 10
    
    # Calculate slider range
    posMinMax = int(10**Audio.DECIMAL_PLACES)
    
    # Define controls
    playButton = JButton("Play")
    stopButton = JButton("Stop")
    pauseButton = JButton("Pause")
    refreshButton = JButton("Refresh")
    rangeSpinner = JSpinner(SpinnerNumberModel(range, 1, 100, 1))
    sourceCombo = JComboBox()
    positionXSlider = JSlider()
    positionYSlider = JSlider()
    positionZSlider = JSlider()

    # Reference to AudioSource
    source = None

    # Define various constants
    PLAY = "AUDIO.PLAY"
    PAUSE = "AUDIO.PAUSE"
    RESUME = "AUDIO.RESUME"
    STOP = "AUDIO.STOP"
    REFRESH = "AUDIO.REFRESH"
    SELECT = "Select source from list"
    POSX = "X"
    POSY = "Y"
    POSZ = "Z"

    def __init__(self):

        # Setup controls - buttons first
        self.playButton.preferredSize = self.refreshButton.preferredSize
        self.playButton.actionCommand = self.PLAY
        self.playButton.actionPerformed = self.whenButtonClicked
        self.pauseButton.preferredSize = self.refreshButton.preferredSize
        self.pauseButton.actionCommand = self.PAUSE
        self.pauseButton.actionPerformed = self.whenButtonClicked
        self.stopButton.preferredSize = self.refreshButton.preferredSize
        self.stopButton.actionCommand = self.STOP
        self.stopButton.actionPerformed = self.whenButtonClicked
        self.refreshButton.actionCommand = self.REFRESH
        self.refreshButton.actionPerformed = self.whenButtonClicked

        # Now combobox and text field
        self.sourceCombo.itemStateChanged = self.whenSourceChanged
        self.updateSourcesList()
        self.rangeSpinner.stateChanged = self.whenRangeChanged

        # Now sliders
        ticksMajor = int(self.posMinMax/4)
        ticksMinor = int(ticksMajor/5)
        labels = Hashtable(3)
        labels.put(-self.posMinMax, JLabel("Left"))
        labels.put(              0, JLabel("Centre"))
        labels.put( self.posMinMax, JLabel("Right"))
        self.positionXSlider.labelTable = labels
        self.positionXSlider.minimum = -self.posMinMax
        self.positionXSlider.maximum = self.posMinMax
        self.positionXSlider.majorTickSpacing = ticksMajor
        self.positionXSlider.minorTickSpacing = ticksMinor
        self.positionXSlider.paintTicks = True
        self.positionXSlider.paintLabels = True
        self.positionXSlider.snapToTicks = True
        self.positionXSlider.value = 0
        self.positionXSlider.stateChanged = self.whenSliderXChanged
        labels = Hashtable(3)
        labels.put(-self.posMinMax, JLabel("Behind"))
        labels.put(              0, JLabel("Centre"))
        labels.put( self.posMinMax, JLabel("In-front"))
        self.positionYSlider.labelTable = labels
        self.positionYSlider.minimum = -self.posMinMax
        self.positionYSlider.maximum = self.posMinMax
        self.positionYSlider.majorTickSpacing = ticksMajor
        self.positionYSlider.minorTickSpacing = ticksMinor
        self.positionYSlider.paintTicks = True
        self.positionYSlider.paintLabels = True
        self.positionYSlider.snapToTicks = True
        self.positionYSlider.value = 0
        self.positionYSlider.orientation = JSlider.VERTICAL
        self.positionYSlider.stateChanged = self.whenSliderYChanged
        labels = Hashtable(3)
        labels.put(-self.posMinMax, JLabel("Below"))
        labels.put(              0, JLabel("Centre"))
        labels.put( self.posMinMax, JLabel("Above"))
        self.positionZSlider.labelTable = labels
        self.positionZSlider.minimum = -self.posMinMax
        self.positionZSlider.maximum = self.posMinMax
        self.positionZSlider.majorTickSpacing = ticksMajor
        self.positionZSlider.minorTickSpacing = ticksMinor
        self.positionZSlider.paintTicks = True
        self.positionZSlider.paintLabels = True
        self.positionZSlider.snapToTicks = True
        self.positionZSlider.value = 0
        self.positionZSlider.orientation = JSlider.VERTICAL
        self.positionZSlider.stateChanged = self.whenSliderZChanged

        # Setup frame
        self.title = "Simple JMRI Audio Player"
        self.contentPane.layout = BoxLayout(self.contentPane, BoxLayout.Y_AXIS)

        # Add controls to frame - combo & buttons first
        p = JPanel(FlowLayout(FlowLayout.LEADING))
        p.add(self.sourceCombo)
        p.add(self.refreshButton)
        p.add(self.playButton)
        p.add(self.pauseButton)
        p.add(self.stopButton)
        #p.add(JLabel("Range"))
        #p.add(self.rangeSpinner)
        self.add(p)
        self.add(Box.createVerticalGlue())

        # Now sliders
        p = JPanel(FlowLayout(FlowLayout.LEADING))
        label = JLabel("Y Position")
        label.UI = VerticalLabelUI() # Default behaviour is anti-clockwise
        p.add(label)
        p.add(self.positionYSlider)
        p2 = JPanel()
        p2.layout = BoxLayout(p2, BoxLayout.Y_AXIS)
        p3 = JPanel()
        p3.add(JLabel("Range"))
        p3.add(self.rangeSpinner)
        p2.add(p3)
        #p2.add(Box.createVerticalGlue())
        p3 = JPanel()
        p3.layout = BoxLayout(p3, BoxLayout.Y_AXIS)
        label = JLabel("X Position")
        label.alignmentX = JLabel.CENTER_ALIGNMENT
        p3.add(label)
        p3.add(self.positionXSlider)
        p2.add(p3)
        p.add(p2)
        label = JLabel("Z Position")
        label.UI = VerticalLabelUI()
        p.add(label)
        p.add(self.positionZSlider)
        self.add(p)

        # Finally pack and show
        self.pack()
        self.show()

    def updateSourcesList(self):
        # Clear the ComboBox
        self.sourceCombo.removeAllItems()
        # Now populate
        self.sourceCombo.addItem(self.SELECT)
        # Retrieve system name list of AudioSources
        for source in audio.getNamedBeanSet(Audio.SOURCE):
            # Add available sources to the list
            self.sourceCombo.addItem(source.getSystemName())

    def whenRangeChanged(self, event):
        # store value & update sliders
        self.range = self.rangeSpinner.value
        self.updateSliders()

    def whenSourceChanged(self, event):
        # Only do something when an item is selected
        if event.getStateChange() == ItemEvent.SELECTED:
            # Stop playing source
            self.stopSource()
            if (event.getItem() != self.SELECT):
                # Set reference to source
                self.source = audio.provideAudio(event.getItem())
                # Enable buttons & update sliders
                self.enableControls(True)
                self.updateSliders()
            else:
                # Clear reference to source
                self.source = None
                # Disable buttons
                self.enableControls(False)

    def whenButtonClicked(self, event):
        # Get the action command
        command = event.getActionCommand()
        # Execute appropriate action
        if self.source != None:
            if command == self.PLAY:
                self.pauseButton.actionCommand = self.PAUSE
                self.pauseButton.text = "Pause"
                self.playSource()
            elif command == self.PAUSE:
                self.pauseButton.actionCommand = self.RESUME
                self.pauseButton.text = "Resume"
                self.pauseSource()
            elif command == self.RESUME:
                self.pauseButton.actionCommand = self.PAUSE
                self.pauseButton.text = "Pause"
                self.resumeSource()
            elif command == self.STOP:
                self.pauseButton.actionCommand = self.PAUSE
                self.pauseButton.text = "Pause"
                self.stopSource()
        elif command == self.REFRESH:
            self.updateSourcesList()
        else:
            print "No action defined!"

    def playSource(self):
        if self.source != None:
            self.source.play()

    def pauseSource(self):
        if self.source != None:
            self.source.pause()

    def resumeSource(self):
        if self.source != None:
            self.source.resume()

    def stopSource(self):
        if self.source != None:
            self.source.stop()

    def whenSliderXChanged(self, event):
        self.changePosition(self.POSX, (float(self.positionXSlider.value)/self.posMinMax*self.range))

    def whenSliderYChanged(self, event):
        self.changePosition(self.POSY, (float(self.positionYSlider.value)/self.posMinMax*self.range))

    def whenSliderZChanged(self, event):
        self.changePosition(self.POSZ, (float(self.positionZSlider.value)/self.posMinMax*self.range))

    def changePosition(self, which, value):
        if self.source != None:
            # Get the current position
            pos = self.source.getPosition()
            # Determine which axis to alter
            if which == self.POSX:
                pos.x = value
            elif which == self.POSY:
                pos.y = value
            elif which == self.POSZ:
                pos.z = value
            # Now change the position
            self.source.position = pos

    def updateSliders(self):
        # Get the current position
        pos = self.source.getPosition()
        # Update sliders
        self.positionXSlider.value = int(pos.x * self.posMinMax / self.range)
        self.positionYSlider.value = int(pos.y * self.posMinMax / self.range)
        self.positionZSlider.value = int(pos.z * self.posMinMax / self.range)

    def enableControls(self,enable):
        self.playButton.enabled = enable
        self.pauseButton.enabled = enable
        self.stopButton.enabled = enable
        self.positionXSlider.enabled = enable
        self.positionYSlider.enabled = enable
        self.positionZSlider.enabled = enable
        self.rangeSpinner.enabled = enable

a = AudioPlayerFrame()
