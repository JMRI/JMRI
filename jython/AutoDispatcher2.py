# AutoDispatcher 2
#
#    This script provides full layout automation, using connectivity info
#    provided by Layout Editor panels.
#
# This file is part of JMRI.
#
# JMRI is free software; you can redistribute it and/or modify it under
# the terms of version 2 of the GNU General Public License as published
# by the Free Software Foundation. See the "COPYING" file for a copy
# of this license.
#
# JMRI is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# for more details.
#
# Author:  Giorgio Terdina copyright (c) 2009, 2010, 2011
#
# 2.01 beta - Fixed problem wih signalheads without UserName
# 2.02 beta - Added test for empty sections
# 2.03 beta - Added test for no valid section found
# 2.04 beta - Corrected removal of SignalMasts
# 2.05 beta - Reverted signals to red aspect as soon as train enters next block
# 2.06 beta - Added new methods (getScale, getDeceleration, etc.) for custom AE
# 2.07 beta - Corrected bug when minimum speed is selected in SignalType
# 2.08 beta - Removed default speed from Speeds window, since it was confusing
# 2.09 beta - Corrected problem when manually changing train's section
# 2.10 beta - Corrected initialization of allocationReady at train departure
# 2.11 beta - Removed bell chime for warnings printed before loading preferences
# 2.12 beta - Modified Signal Edit window to make it more understandable
# 2.13 beta - Compensated difference for emergency stop between Multimaus and other CS
# 2.14 beta - Fixed bug for unbalanced brackets in schedule
# 2.15 beta - Added detailed error messages for wrong schedule
# 2.16 beta - Fixed problem of $P stopping trains controlled by "Braker" engineer 
# 2.17 beta - Added possibility of separating schedule tokens with comas ","
# 2.18 beta - Added error message when destination is a transit-only section
# 2.19 beta - Set default engineer to Auto, when custom script is not found
# 2.20 beta - Added blinking of new flashingLunar signal aspect (since JMRI 2.7.8)
# 2.21 beta - Corrected problem with OFF:Fx block action
# 2.22 beta - Added test for wrong input in Locomotives window
# 2.23 beta - Added possibility of stopping trains at the beginning of sections
# 2.24 beta - Fixed hang-up when schedule alternative has only one section and its name is wrong
# 2.25 beta - Added error message for nested "[" in schedule
# 2.26 beta - Fixed problem with static variable in ADTrain addressed as self.variable
# 2.27 beta - Fixed direction in transit-only sections when dealing with reversing tracks
# 2.28 beta - Closed "Train Detail" window when "Apply" is clicked
# 2.29 beta - Corrected loop in match method caused when an unknown section name was found
# 2.30 beta - Modified "import" statements to reflect package structure of JMRI 2.9.x
# 2.31 beta - Unified versions for JMRI 2.8 and 2.9.x and initialized block tracking at startup
# 2.32 beta - Implementd "train start actions" and "AutoStart trains" option.
# 2.33 beta - Enabled pause/resume buttons while script being stopped.
# 2.34 beta - Updated blinkSignals as previous methods were deprecated (Greg).
# 2.35 - Added $IFH (if held) command in schedule.
# 2.35 - Added $TC and $TT (set turnout or accessory) commands in schedule.
# 2.35 - Added $ST (Start at fast clock time) command in schedule.
# 2.35 - Released throttle when train is in manual section.
# 2.35 - Added custom section RGB colors.
# 2.36 - Avoided duplicate operation of turnouts due to changes in Layout Editor.
# 2.37 - Added section tracking using JMRI memory variables.
# 2.38 - Modified to ignored Power OFF when using XpressNet Simulator (since it's unreliable).
# 2.39 - Removed Operations interface (nobody ever used it!)
# 2.40 - Fixed thread race condition when stopping trains
# 2.41 - Set default minimum interval between speed commands (maxIdle) to 60 seconds
# 2.42 - Adapted to refactoring occurred in JMRI 3.32 (different access to default directory)
# 2.43 - timeout and retry on acquisition failure, handle new syntax of LayoutEditor class

# JAVA imports

import java
import java.awt
import java.awt.event
import java.beans
import java.io
import java.util

import jmri

from java.beans import PropertyChangeListener

from java.lang import System

from java.awt import BorderLayout
from java.awt import Color
from java.awt import GridLayout
from java.awt import Toolkit

from java.awt.event import ActionListener
from java.awt.event import WindowAdapter

from java.io import FileInputStream
from java.io import FileOutputStream
from java.io import IOException
from java.io import ObjectInputStream
from java.io import ObjectOutputStream

from java.util import Locale
from java.util import Random

from javax.swing import BorderFactory
from javax.swing import BoxLayout
from javax.swing import ButtonGroup
from javax.swing import JButton
from javax.swing import JCheckBox
from javax.swing import JComboBox
from javax.swing import JFileChooser
from javax.swing import JLabel
from javax.swing import JOptionPane
from javax.swing import JPanel
from javax.swing import JRadioButton
from javax.swing import JScrollPane
from javax.swing import JTextArea
from javax.swing import JTextField
from javax.swing import JToggleButton
from javax.swing import JSpinner
from javax.swing import SpinnerNumberModel

from javax.swing.event import ChangeListener

from javax.swing.filechooser import FileFilter

from math import sqrt

from time import sleep

from thread import start_new_thread

# JMRI imports

from apps import Apps

from jmri import Block
from jmri import DccThrottle
from jmri import InstanceManager
from jmri import PowerManager
from jmri import Section
from jmri import SectionManager
from jmri import Sensor
from jmri import SignalHead
from jmri import Turnout

from jmri.jmrit import Sound
from jmri.jmrit import XmlFile

from jmri.jmrit.consisttool import ConsistToolFrame

from jmri.implementation import AbstractShutDownTask

# from jmri.jmrit.operations.locations import LocationManager
#from jmri.jmrit.operations.rollingstock.cars import CarManager
#from jmri.jmrit.operations.trains import TrainManager

from jmri.jmrit.roster import Roster

from jmri.util import JmriJFrame

# Utility class for static methods ==============
# Must be defined before being used!

class ADstaticMethod :
    def __init__(self, anycallable):
        self.__call__ = anycallable

# Fast Clock Listener
class FastListener(java.beans.PropertyChangeListener):
  fastTime = 0

  def propertyChange(self, event):
    time = InstanceManager.getDefault(jmri.Timebase).getTime()
    FastListener.fastTime = time.getHours() * 60 + time.getMinutes()
    return

# MAIN CLASS ==============

class AutoDispatcher(jmri.jmrit.automat.AbstractAutomaton) :

# CONSTANTS ==========================

    version = "2.43"
    
    
    # Retrieve DOUBLE_XOVER constant, depending on JMRI Version
    # (LayoutTurnout class was moved to a new package, starting with JMRI 2.9.3)
    try :
        DOUBLE_XOVER = jmri.jmrit.display.layoutEditor.LayoutTurnout.DOUBLE_XOVER
    except :
        DOUBLE_XOVER = jmri.jmrit.display.LayoutTurnout.DOUBLE_XOVER


    
    # Maximum number of lines kept in the status scroll area
    MAX_LINES = 200
    
    # Names of Signalhead aspects
    headsAspects = {
        "Dark": SignalHead.DARK,
        "Red": SignalHead.RED,
        "Yellow": SignalHead.YELLOW,
        "Green": SignalHead.GREEN,
        "Lunar": SignalHead.LUNAR,
        "Flash-Red": SignalHead.FLASHRED,
        "Flash-Yellow": SignalHead.FLASHYELLOW,
        "Flash-Green": SignalHead.FLASHGREEN,
        "Flash-Lunar": SignalHead.FLASHLUNAR
    }

    # Signalhead aspects inverse dictionary
    inverseAspects = {}
    for key in headsAspects.keys() :
        inverseAspects[headsAspects[key]] = key
    
# STATIC VARIABLES ==========================
    
    # Our unique instance
    instance = None
    # Power monitor instance
    powerMonitor = None
    
    # Fast Clock
    fastBase = InstanceManager.getDefault(jmri.Timebase)
    fastListener = FastListener()

    # Status variables
    error = False
    loop = False
    stopped = True
    exiting = False
    paused = False
    repaint = False
    simulation = False
    lenzSimulation = False
    trainsDirty = False
    preferencesDirty = False
    debug = False

    # Our random numbers generator
    random = Random()

    # Window frames
    mainFrame = None
    directionFrame = None
    panelFrame = None
    speedsFrame = None
    indicationsFrame = None
    signalTypesFrame = None
    signalEditFrame = None
    signalMastsFrame = None
    sectionsFrame = None
    blocksFrame = None
    locationsFrame = None
    preferencesFrame = None
    soundListFrame = None
    soundDefaultFrame = None
    locosFrame = None
    trainsFrame = None
    trainDetailFrame = None
    importFrame = None
    
    # Status area at the bottom of the main window
    statusScroll = JTextArea("Getting layout description (be patient!)", 4, 22)
    statusScroll.setEditable(False)
    statusScroll.setLineWrap(True)
    statusScroll.setWrapStyleWord(True)

    # Info gathered from JAVA
    screenSize = Toolkit.getDefaultToolkit().getScreenSize()
    
    # Info gathered from JMRI
    # List of signalHeads defined in JMRI
    signalHeadNames = []
    # List of signalHeadIcons displayed on the panel
    signalIcons = []
    # Original click mode of signalHeadIcons (used to restore them on exit)
    signalClick = []
    # maximum number of blocks encountered in any section
    maxBlocksPerSection = 0

    # Number of trains running
    runningTrains = 0
    # List of available engineer scripts
    engineers = {}
    
    # Lists of last DCC commands sent
    # Used when resuming operations after "Power off"
    turnoutCommands = {}
    signalCommands = {}
    
    # Table used for test purposes if file AutoDispatcher_Loc.bin is not found
    locomotives =  [
        ['1017', 1017, [0.01, 0.26, 0.51, 0.75, 1.0], 0, 0, 0, 0.0],
        ['1019', 1019, [0.15, 0.5, 0.55, 0.6, 0.65], 12, 7, 0, 90.0],
        ['1633', 1633, [0.2, 0.65, 0.7, 0.75, 0.8], 10, 6, 0, 0.0],
        ['1641', 1641, [0.2, 0.5, 0.7, 0.8, 0.8], 15, 10, 0, 0.0],
        ['3023', 3023, [0.2, 0.6, 0.7, 0.8, 0.85], 8, 7, 0, 0.0],
        ['3029', 3029, [0.01, 0.26, 0.51, 0.75, 1.0], 0, 0, 0, 0.0],
        ['4802', 4802, [0.01, 0.26, 0.51, 0.75, 1.0], 0, 0, 0, 0.0],
        ['4805', 4805, [0.2, 0.7, 0.8, 0.9, 0.95], 10, 8, 0, 0.0],
        ['5010', 5010, [0.2, 0.6, 0.75, 0.85, 0.95], 10, 8, 0, 0.0],
        ['5151', 5151, [0.3, 0.6, 0.7, 0.8, 0.8], 12, 12, 0, 0.0],
        ['5160', 5160, [0.2, 0.6, 0.7, 0.8, 0.8], 15, 6, 0, 0.0],
        ['5162', 5162, [0.01, 0.26, 0.51, 0.75, 1.0], 0, 0, 0, 0.0],
        ['5166', 5166, [0.01, 0.26, 0.51, 0.75, 1.0], 0, 0, 0, 0.0],
        ['5294', 5294, [0.01, 0.26, 0.51, 0.75, 1.0], 0, 0, 0, 0.0],
        ['5307', 5307, [0.2, 0.8, 0.8, 0.85, 0.9], 5, 10, 0, 0.0]]

    # Variable used by "printTime" debug utility
    lastTime = 0
    
# STATIC METHODS ==========================
    
    def getVersion() :
        return AutoDispatcher.version
    getVersion = ADstaticMethod(getVersion)

    def setDebug(on) :
        AutoDispatcher.debug = on
    setDebug = ADstaticMethod(setDebug)

    def message(text) :
        # Output a message only in verbose mode
        if ADsettings.verbose :
            AutoDispatcher.log(text)
    message = ADstaticMethod(message)
        
    def log(text) :
        # Output a message, both to script main window and to log file
        AutoDispatcher.statusScroll.append("\n" + text)
        while (AutoDispatcher.statusScroll.getLineCount() >
           AutoDispatcher.MAX_LINES) :
            AutoDispatcher.statusScroll.replaceRange(None, 0,
              AutoDispatcher.statusScroll.getLineEndOffset(0))
        AutoDispatcher.statusScroll.setCaretPosition(
          AutoDispatcher.statusScroll.getDocument().getLength())
        print text
    log = ADstaticMethod(log)

    def chimeLog(sound, text) :
        # As log, but also plays a sound
        AutoDispatcher.log(text)
        ind = ADsettings.defaultSounds[sound]
        if ADsettings.ringBell and ind > 0 and ind >= len(ADsettings.soundList) :
            ADsettings.soundList[ind-1].play()
    chimeLog = ADstaticMethod(chimeLog)

    def addEngineer(engineerName, engineerClass) :
        # Add an Engineer script to our list
        if (engineerName != "Auto" or not
           AutoDispatcher.engineers.has_key(engineerName)) :
            AutoDispatcher.engineers[engineerName] = engineerClass
            if AutoDispatcher.trainsFrame != None :
                AutoDispatcher.trainsFrame.reDisplay()
            if engineerName != "Auto" :
                AutoDispatcher.log("Added engineer: " + engineerName)
            return True
        AutoDispatcher.log("Warning: Engineer " + engineerName 
          + "already registered!")
        return False
    addEngineer = ADstaticMethod(addEngineer)
    
    def removeEngineer(engineerName) :
        # Remove an Engineer script from our list
        if (engineerName != "Auto" and
           AutoDispatcher.engineers.has_key(engineerName)) :
            del AutoDispatcher.engineers[engineerName]
            for train in ADtrain.getList() :
                if train.engineerName == engineerName :
                    train.setEngineer("Auto")
            AutoDispatcher.log("Removed engineer: " + engineerName)
            return True
        AutoDispatcher.log("Warning: Engineer " + engineerName + 
          " not found. Cannot be removed!")
        return False
    removeEngineer = ADstaticMethod(removeEngineer)

    def setTrainsDirty() :
        # Take note that trains info changed
        if AutoDispatcher.trainsDirty :
            return
        AutoDispatcher.trainsDirty = True
        if not AutoDispatcher.loop :
            ADmainMenu.saveTrainsButton.enabled = True
    setTrainsDirty = ADstaticMethod(setTrainsDirty)
            
    def setPreferencesDirty() :
        # Take note that preferences were changed
        if AutoDispatcher.preferencesDirty :
            return
        AutoDispatcher.preferencesDirty = True
        if not AutoDispatcher.loop :
            ADmainMenu.saveSettingsButton.enabled = True
    setPreferencesDirty = ADstaticMethod(setPreferencesDirty)

    def centerLabel(string) :
        # Create a centered JLabel
        tempLabel = JLabel(string)
        tempLabel.setHorizontalAlignment(JLabel.CENTER)
        return tempLabel
    centerLabel = ADstaticMethod(centerLabel)

    def printTime(text) :
        # Debug utility.  Can be used to measure time lapsed between events
        newTime = System.currentTimeMillis()
        if AutoDispatcher.lastTime != 0 :
            print (newTime - AutoDispatcher.lastTime), text
        AutoDispatcher.lastTime = newTime
    printTime = ADstaticMethod(printTime)
    
    def cleanName(name) :
        # Replace spaces, brackest and $ contained in section or signal names
        # to avoid errors in schedules
        name  = name.replace(" ", "_")
        name  = name.replace("$", "#")
        name  = name.replace("(", "{")
        name  = name.replace("[", "{")
        name  = name.replace(")", "}")
        name  = name.replace("]", "}")
        name  = name.replace(",", ".")
        return name
    cleanName = ADstaticMethod(cleanName)

# INSTANCE METHODS ==========================

# INITIAL SETUP ==============

    def setup(self):

    # Done once
        
        # Save the (unique) instance in a static variable, to allow access 
        # from different classes
        AutoDispatcher.instance = self
        # Create settings
        ADsettings()
        # Register our own engineer
        AutoDispatcher.addEngineer("Auto", ADengineer)
                                    
    # Span a separate thread to setup script without blocking JMRI
        start_new_thread(self.__setup__, ())

    def __setup__(self):
        # The real setup.
        
        # Now perform initialization
        # Display the main window
        AutoDispatcher.mainFrame = ADmainMenu()

         # Get layout  connectivity
        self.getLayoutData()
        if AutoDispatcher.error :
            return

        # Workout default settings
        self.defaultSettings()
         
        # Keep copy of automatic settings, to compare them with
        # user defined settings at saving time
        self.autoSections = ADsection.getSectionsTable()
        self.autoBlocks = ADsection.getBlocksTable()

        # If running in simulation mode, clear all sections before 
        # placing trains on tracks
        if AutoDispatcher.simulation :
            AutoDispatcher.log("Clearing sensors for simulation")           
            for block in ADblock.getList() :
                sensor = block.getOccupancySensor()
                if sensor != None :
                    sensor.setKnownState(Sensor.INACTIVE)
            # Wait to allow JMRI processing of sensor change events
            self.waitMsec(1000)
            # Set power ON, otherwise user will wonder why trains
            # don't start :-)
            AutoDispatcher.powerMonitor.setPower(PowerManager.ON)

        # Set demo preferences (will be used if preferences files are not found)
        AutoDispatcher.log("Restoring settings")
        # Choose preferences on the basis of Layout Editor panel title       
        title = self.layoutEditor.getTitle()
        if ADexamples1.examples.has_key(title) :
            ADsettings.load(ADexamples1.examples[title])
            self.trains = ADexamples1.exampleTrains[title]
        elif ADexamples2.examples.has_key(title) :
            ADsettings.load(ADexamples2.examples[title])
            self.trains = ADexamples2.exampleTrains[title]
        else :
            self.trains = []

        # Get user settings from disk (if any)
        # User settings files are named on the basis of the first word
        # in Layout Editor panel title.
        # Get first word of panel title
        firstWord = title.split()
        if len(firstWord) == 0 :
            firstWord = ""
        else :
            firstWord = firstWord[0]
        baseName = ""
        # Strip out all characters but letters and numbers
        for c in firstWord :
            if c.isalnum() :
                baseName += c
        # If we didn't get a valid name, use "AutoDispatcher"
        if baseName == "" :
            baseName = "AutoDispatcher"
        self.settingsFile = (Roster.getDefault().getFileLocation() + 
          baseName + "_AdP.bin")
        self.trainFile = (Roster.getDefault().getFileLocation() + 
          baseName + "_AdT.bin")
        # Locomotive settings are not based on panel title, since there is
        # only one JMRI roster
        self.locoFile = (Roster.getDefault().getFileLocation() + 
          "AutoDispatcher_Loc.bin")
        # Now try loading files
        self.loadSettings()
        self.loadLocomotives()
        self.loadTrains()
 
        # Apply user (or default) settings
        self.userSettings() 
        
        # Get data from Operations module
#        ADlocation.getOpLocations()

        # Setup completed
        # Enable buttons, unless some error occurred
        if not AutoDispatcher.error :
            AutoDispatcher.mainFrame.enableButtons(True)
            AutoDispatcher.chimeLog(ADsettings.START_STOP_SOUND, "Layout ready!")                   
        
# LAYOUT CONNECTIVITY ==============

    def getLayoutData(self) :
    
        # Retrieve power monitor (will be used to check whether 
        # the layout is powered)
        AutoDispatcher.powerMonitor = ADpowerMonitor()

        # Retrieve LayoutEditor instance by searching the relevant window
        # If more than one panel are open, we will get the first one we find
        windowsList = JmriJFrame.getFrameList()
        self.layoutEditor = None
        for i in range(windowsList.size()) :
            window = windowsList.get(i)
            windowClass = str(window.getClass())
            if(windowClass.find(".LayoutEditor") > 0) :
                self.layoutEditor =  window
                break
        if self.layoutEditor == None :
            AutoDispatcher.log("No Layout Editor window found," + 
              " script cannot continue!")
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
              "Load your Layout Editor panel" + 
              " before running AutoDispatcher!")
            AutoDispatcher.error = True
            return
            
        # Retrieve SignalHead names from JMRI (will be used to populate menus)
        signalHeads = InstanceManager.getDefault(SignalHeadManager).getNamedBeanSet()
        AutoDispatcher.signalHeadNames = [""]
        for s in signalHeads :
            # Use UserName (if available), otherwise SystemName
            signalName = s.getUserName()
            if signalName == None or signalName.strip() == "" :
                signalName = s.getSystemName()
            AutoDispatcher.signalHeadNames.append(signalName)
        AutoDispatcher.signalHeadNames.sort()

        # Retrieve List of SignalHeads that have an icon on the panel
        nIcons = self.layoutEditor.signalHeadImage.size()
        for i in range(nIcons) :
            signalIcon = self.layoutEditor.signalHeadImage.get(i)
            signalHead = signalIcon.getSignalHead()
            if signalHead != None :
                signalName = signalHead.getUserName()
                if signalName == None :
                    signalName = signalHead.getSystemName()
                AutoDispatcher.signalIcons.append(signalName)
            # Keep note of original signal icon click mode
            AutoDispatcher.signalClick.append(
              [signalIcon, signalIcon.getClickMode()])

        # Retrieve sections from JMRI
        # (it's unfortunate that this uses "sections" differently than the jmri_defaults style)
        sections = InstanceManager.getDefault(jmri.SectionManager).getNamedBeanSet()
        if sections.size() < 1 :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
              "Layout contains no sections, script" +
              " cannot continue!")
            
        # Create section and block instances
        for section in sections :
            ADsection(section.getSystemName())
            
        if len(ADsection.getList()) == 0 :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
              "No valid section found, script cannot continue!")
            AutoDispatcher.error = True
            return
            

        # Create entries (connections between sections)
        for section in ADsection.getList() :
            section.setEntries()

        # Retrieve paths (connections between blocks)
        for block in ADblock.getList() :
            block.setPaths()
            
        # Retrieve Crossings and establish inter-dependecy between 
        # crossed sections
        nXings = self.layoutEditor.xingList.size()
        for i in range(nXings) :
            xing = self.layoutEditor.xingList.get(i)
            # Ignore crossing if crossed blocks are not included in sections
            blockAC = ADblock.getByName(xing.blockNameAC)
            if blockAC != None :
                blockBD = ADblock.getByName(xing.blockNameBD)
                if blockBD != None :
                    sectionAC = blockAC.getSection()
                    sectionBD = blockBD.getSection()
                    # Ignore crossing if both crossed bocks belong to
                    # the same section
                    # (results can, however, be unpredictable!)
                    if sectionAC != sectionBD :
                        sectionAC.addXing(sectionBD)
                        sectionBD.addXing(sectionAC)

        # Retrieve double crossovers by scanning all tunouts
        # contained in LayoutEditor
        turnoutsNumber = self.layoutEditor.turnoutList.size()
        for i in range(turnoutsNumber) :
            layoutTurnout = self.layoutEditor.turnoutList.get(i)
            # If the turnout is a double crossover, process it
            if layoutTurnout.getTurnoutType() == AutoDispatcher.DOUBLE_XOVER :
                ADxover(layoutTurnout)

        # Retrieve Layout Editor tracks and save their original width
        nTracks = self.layoutEditor.trackList.size()
        if nTracks < 1 :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
              "Warning: layout contains no tracks!")
        else :
            for i in range(nTracks) :
                track = self.layoutEditor.trackList.get(i)
                blockName = track.getBlockName()
                block = ADblock.getByName(blockName)
                # Process track only if block contained in some section
                if block != None :
                    block.addTrack(ADtrack(track))
        # Reorganize sections' directions
        # Blocks within each section are listed in arbitrary order.
        # Make sure that the order is consistent.
        # Starting from the first section, mark sections with inconsistent 
        # order as "reversed"
        self.processedSections = []
        # If all sections are connected, the following for-loop
        # is actually performed ony once
        for section in ADsection.getList() :
            self.__alignSection__(section, False)
        # Find and mark reversing tracks (if any)
        self.findTransitPoints()
        
# INTERNAL METHODS OF getLayoutData ==============

    def __alignSection__(self, section, changeOrientation) :
        # Internal method. Recursively aligns all sections
        # If section already processed, exit
        if section in self.processedSections :
            return
        # Take note that this section was processed (in order to avoid a loop!)
        self.processedSections.append(section)
        # Change orientation, if needed
        if changeOrientation :
            section.setReversed(not section.isReversed())
        # Scan both entries and exits
        for direction in [False, True] :
            for entry in section.getEntries(direction) :
                nextSection = entry.getExternalSection()
                # Adjust orientation of each entry/exit
                self.__alignSection__(nextSection, 
                  self.__hasOrientationChanged__(entry, direction))

    def findTransitPoints(self) :
        # Find transit points on possible reversing tracks
        # Train color will change when transiting these points
        for section in ADsection.getList() :
            # Scan both entries and exits
            for direction in [False, True] :
                for entry in section.getEntries(direction) :
                    change = self.__hasOrientationChanged__(entry, direction)
                    entry.setDirectionChange(change)

    def __hasOrientationChanged__(self, startEntry, direction) :
        # Internal method. 
        # Checks if two sections are aligned, by verifying
        # if the start section is included in the entries of 
        # destination section (keeping into account direction)
        startSection = startEntry.getInternalSection()
        startBlock = startEntry.getInternalBlock()
        endSection = startEntry.getExternalSection()
        endBlock = startEntry.getExternalBlock()
        for endEntry in endSection.getEntries(not direction) :
            if (endEntry.getExternalSection() == startSection and
               endEntry.getExternalBlock() == startBlock and
               endEntry.getInternalBlock() == endBlock) :
                # section found, directions are consistent
                return False
        # section not found, directions are inconsistent
        return True                    
    
# DEFAULT SETTINGS ==============

    def defaultSettings(self) :
        # Set sections' settings to default value
        # They may then be overriden by user (or by settings stored on disk)           
        # Mark transit-only sections
        # (Sections where trains cannot stop without blocking traffic)
        for section in ADsection.getList() :
            section.setDefault()

# USER SETTINGS ==============
 
    def userSettings(self):

    # Override default settings with user defined settings loaded from disk
    # (or embedded settings for demo panels)
    
        # First of all define direction names, based on user choices
        if ADsettings.ccwStart.strip() != "" :
            self.setDirections()
        self.setSignals()

        # Set user defined one-way and transit-only sections
        ADsection.putSectionsTable()

        # Adjust entry points (in case some sections were manually flipped)
        self.findTransitPoints()
        
        # Mark sections where gridlock situations can occur
        ADgridGroup.create()

        # Set manual indicator sensors to INACTIVE, otherwise they can be
        # confused with signals (owing to the red aspect)
        for section in ADsection.getList() :
            if not section.isManual() :
                section.setManual(False)

        # Set user defined brake, stop, assignment and safe-point blocks
        ADsection.putBlocksTable()

        # Retrieve info from JMRI roster and create locomotives with 
        # standard speeds
        AutoDispatcher.log("Getting JMRI locomotives roster")           
        jmriRoster = Roster.getDefault().matchingList(None, None, None, None,
          None, None, None)
        for i in range(jmriRoster.size()) :
            name = jmriRoster.get(i).getId()
            address = int(jmriRoster.get(i).getDccAddress())
            ADlocomotive(name, address, None, True)
            # Roster contains also long address indicator
            # We should likely use it but, getThrottle method
            # of AbstractAutomaton don't cares!

        # Get consists from JMRI
        # Any consist must be defined before starting AutoDispatcher
        consistMan = InstanceManager.getDefault(jmri.ConsistManager)
        consists = consistMan.getConsistList()
        # Any consist?
        if len(consists) == 0 :
            # No, make sure consist file was loaded
            consistFrame = ConsistToolFrame()
            # and retry
            consists = consistMan.getConsistList()
            consistFrame.dispose()
        for a in consists :
            c = consistMan.getConsist(a)
            # Get consist address
            a = a.getNumber()
            # Get consist ID
            i = c.getConsistID()
            # Create locomotive
            l = ADlocomotive(i, a, None, True)
            # Get address of first locomotive in consist
            ll = c.getConsistList()
            if len(ll) > 0 :
                f = ll[0].getNumber()
                if f != a :
                    # Record address of first locomotive
                    # Function commands will be sent to it.
                    # May need revising. 
                    l.leadLoco = f

        # Match jmri roster with user defined locomotives (and consists) and set 
        # relevant speeds
        for l in AutoDispatcher.locomotives :
            ll = ADlocomotive.getByName(l[0])
            if ll == None :
                ll = ADlocomotive(l[0], l[1], l[2], False)
            else :
                ll.setSpeedTable(l[2])
            ll.setMomentum(l[3], l[4])
            ll.runningTime = l[5]
            ll.mileage = l[6]
        # Now build trains roster, based on user settings
        if len(self.trains) > 0 :
            AutoDispatcher.log("Placing trains on tracks :-)")
            ADtrain.buildRoster(self.trains)
 
    def setDirections(self) :
        # Find mapping between internal direction and user defined direction
        # To this purpose, two sections are specified, the shorter route 
        # between them is assumed to be CCW direction (or whatever name user 
        # chose). Return True if the selection was successful.
        
        # Check correctness of section names
        result = True
        startSection = ADsection.getByName(ADsettings.ccwStart)
        if startSection == None :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Unknown start section " + ADsettings.ccwStart 
              + " in direction definition")
            ADsettings.ccwStart = ""
            result =  False
        endSection = ADsection.getByName(ADsettings.ccwEnd)
        if endSection == None :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Unknown end section " + ADsettings.ccwEnd
              + " in direction definition")
            ADsettings.ccwEnd = ""
            result =  False
        if result :
            # Find route in direction 0
            route0 = ADautoRoute(startSection, endSection, 0, False)
            # Find route in direction 1
            route1 = ADautoRoute(startSection, endSection, 1, False)
            # Which one is shorter?
            len0 = len(route0.step)
            len1 = len(route1.step)
            if len0 == 0 or (len1 != 0 and len1 < len0) :
                ADsettings.ccw = 1
            else :
                ADsettings.ccw = 0
        return result

    def setSignals(self) :
        # Set signals at section exits, using direction names to identify 
        # signal heads.
        # Assume that the name of signal head is "H" + name of the 
        # section + (lower case) direction
        # Example HS01east is the East exit signal of section S01
        # User can override the choice by manually selecting a signal head
        # or signal mast in the "Sections" window
        extension = [ADsettings.directionNames[0].lower(), 
          ADsettings.directionNames[1].lower()]
        if ADsettings.ccw != 0:
            extension.reverse()
        # Scan all sections
        for s in ADsection.getList() :
            # In both directions
            for i in range(2) :
                newName = "H" + s.getName() + extension[i]
                # Check if a SignalMast or a SignalHead with such a name exists
                newSignal = ADsignalMast.getByName(newName)
                newHead = InstanceManager.getDefault(jmrix.SignalHeadManager).getSignalHead(newName)
                # Assign a new SignalMast only if it was not yet assigned
                # or it was automatically created in a previous call
                # (provided we found a replacement!) Replacing signals
                # is needed since user could change direction names.
                if (s.signal[i] == None or (s.signal[i].getName() != newName and
                  not s.signal[i].hasHead() and s.signal[i].inUse < 2 and 
                  s.signal[i].signalType == ADsettings.signalTypes[0] and
                  (newSignal != None or newHead != None))) :
                    # If a SignalMast was cretaing using the old direction name
                    # delete it
                    if s.signal[i] != None :
                        s.signal[i].changeUse(-2)
                    # Now assign/create the new signal
                    if newSignal == None :
                        s.signal[i] = ADsignalMast.provideSignal(newName)
                    else :
                        s.signal[i] = newSignal
                    # Take note that the signal is being used
                    s.signal[i].changeUse(1)
            # Try and find the (optional) sensor to set the section under 
            # manual control
            if s.manualSensor == None :
                sensorName = s.getName() + "man"
                if sensorName in ADsection.sensorNames :
                    s.manualSensor = InstanceManager.sensorManagerInstance().getSensor(sensorName)

# OPERATIONS ==============

    def init(self):
    # Start - Performed each time "Start" button is clicked
        # Clear number of running trains
        AutoDispatcher.runningTrains = 0
        # Start fast clock listener
        AutoDispatcher.fastBase.addMinuteChangeListener(AutoDispatcher.fastListener)
        # Set block occupancy listeners
        ADblock.setListeners()
        # Set sections manual control sensors listeners
        ADsection.setListeners()
        # Let other methods and classes know that we started
        AutoDispatcher.loop = True
        AutoDispatcher.stopped = False
        # Check the initial occupancy state of sections
        for section in ADsection.getList() :
            section.occupied = True
            section.empty = False
            section.checkOccupancy()
            section.occupied = False
            if not section.empty :
                section.setOccupied()
        # Force occupation of sections assigned to trains
        for train in ADtrain.getList() :
            for section in train.previousSections :
                section.empty = not section.occupied
                section.occupied = True
                section.allocate(train, train.direction)
            if (train.destination != train.lastRouteSection and
               train.lastRouteSection != None) :
                train.lastRouteSection.allocate(train, train.direction)
        # Set initial width of tracks
        for block in ADblock.getList() :
            block.adjustWidth()
        # Place/remove train names in jmri Blocks
        # (only if user enabled this option)
        if ADsettings.blockTracking :
            for section in ADsection.getList() :
                section.changeTrainName()
        # Set click-mode of panel SignalHeadIcons to "alternate held"
        # to avoid that user may change signal aspects
        for s in AutoDispatcher.signalClick :
            s[0].setClickMode(2)        
        # Set variables for the handle method
        self.train = 0
         # Force updating of LayoutEditor panel
        AutoDispatcher.repaint = True
        # Enable user interface buttons (unless errors occurred)
        if not AutoDispatcher.error :
            AutoDispatcher.mainFrame.enableButtons(False)
            if AutoDispatcher.simulation :
                # Separate thread, advancing trains in simulation mode
                start_new_thread(self.autoStep, ())
            # Separate thread, controlling trains speed
            start_new_thread(self.speedControl, ())
            # Separate thread, controlling locomotives maintenance time
            start_new_thread(self.maintenanceControl, ())
            # Separate thread, blinking signal head icons on panle
            start_new_thread(self.blinkSignals, ())
            # If power is off we start in "paused" mode
            if ADpowerMonitor.powerOn :
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Have fun!")
            else :
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Power Off!")
                ADpowerMonitor.savePause = True
                self.stopAll()

# BACKGROUND TASK ==============

    def handle(self):
        # Handle thread.  Runs in background and takes care of trains departure
        # Exit if there was an error or user clicked "STOP" button
        if AutoDispatcher.error or not AutoDispatcher.loop :
            # Cleanup before exiting
            # Wait for all trains to stop
            self.waitForStop()
            # Release engineers and throttles (if any)
            for t in ADtrain.getList() :
                if t.engineer != None :
                    t.releaseEngineer()
                locomotive = t.locomotive
                if locomotive != None :
                    locomotive.releaseThrottle()
            # Inform other threads that program is exiting
            AutoDispatcher.stopped = True
            # Remove listeners
            ADblock.removeListeners()
            ADsection.removeListeners()
            AutoDispatcher.fastBase.removeMinuteChangeListener(AutoDispatcher.fastListener)
            # Restore original block colors and track width
            for block in ADblock.getList() :
                block.restore()
            self.layoutEditor.redrawPanel()
            # Restore original click mode of signalHeadIcons on panel
            for s in AutoDispatcher.signalClick :
                s[0].setClickMode(s[1])
            # Are we quitting?
            if AutoDispatcher.exiting :
                # Yes, give user the possibility of saving settings and 
                # trains position
                self.saveBeforeExit()
            else :
                # Script not quitting yet
                # Allow user to change settings
                AutoDispatcher.mainFrame.enableButtons(True)
                # Allow user to restart operations, unless errors occurred
            if not AutoDispatcher.error :
                ADmainMenu.startButton.enabled = True
                AutoDispatcher.chimeLog(ADsettings.START_STOP_SOUND, "Goodbye!")
            return 0
        # Normal loop
        # Redraw Layout Editor panel, if needed
        if AutoDispatcher.repaint :
            AutoDispatcher.repaint = False
            self.layoutEditor.redrawPanel()
        # Start trains that are eligible (unless script is paused)
        if not AutoDispatcher.paused :
            # At each iteration we try and start one train
            if len(ADtrain.trains) > 0 :
                if self.train >= len(ADtrain.trains) :
                    self.train = 0
                ADtrain.trains[self.train].startIfReady()
                self.train += 1
        # Wait for a while, letting other threads do their work
        self.waitMsec(100)
        return 1

    def waitForStop(self) :
        # Wait until all trains halt (unless script stopped)
        if AutoDispatcher.stopped :
            return
        # Make sure script is not paused, otherwise we risk to wait forever
        wasPaused = AutoDispatcher.paused
        self.resume()
        # If we are in simulation mode, make sure layout is powered
        if AutoDispatcher.simulation :
            AutoDispatcher.powerMonitor.setPower(PowerManager.ON)
        # Wait only if layout is powered
        # This can actually result in inconsistent situations, but
        # we can neither switch power on (causing shorts or collisions)
        # neither wait here forever!
        if ADpowerMonitor.powerOn :
            for t in ADtrain.trains :
                # Lets wait if locomotive's speed is > 0, or
                # running indicator is set (if the train is being run
                # manually or by another script, we may not know
                # which locomotive is used), or
                # train is being started right now
                while (t.running or (t.locomotive != None and
                t.locomotive.getThrottleSpeed() > 0) or ADtrain.turnoutsBusy) :
                    self.waitMsec(100)
                    # Since train is running, occupation state of blocks and
                    # sections may change and the panel may need redrawing
                    if AutoDispatcher.repaint :
                        AutoDispatcher.repaint = False
                        self.layoutEditor.redrawPanel()
        # Set script into "pause mode", if it was so when this method was called
        if wasPaused :
            self.stopAll()

    def stopAll(self) :
        # Stop trains (or remove power) when paused or an error occurs
        # Ignore, if user disabled "Pause" option
        if(ADsettings.pauseMode == ADsettings.IGNORE or
          AutoDispatcher.exiting) :
            return
        # Make sure we are not called more than once 
        # (owing to Jython lack of synchronization)
        if not ADmainMenu.resumeButton.enabled :
            ADmainMenu.resumeButton.enabled = True
            ADmainMenu.pauseButton.enabled = False
            ADmainMenu.stopButton.enabled = False
            # Did user chose to power-off layout?
            if(ADsettings.pauseMode == ADsettings.POWER_OFF) :
                # Stop layout
                AutoDispatcher.paused = True
                AutoDispatcher.powerMonitor.setPower(PowerManager.OFF)
            else :
                # STOP_TRAINS case
                for train in ADtrain.getList() :
                    train.pause()
        AutoDispatcher.paused = True

    def resume(self) :
        # Resume script after a pause
        # Ignore if we were not paused
        if not AutoDispatcher.paused :
            return
        AutoDispatcher.paused = False
        if ADsettings.pauseMode == ADsettings.POWER_OFF :
           # Switch power ON
            AutoDispatcher.powerMonitor.setPower(PowerManager.ON)
        else :
            # or restart trains
            for train in ADtrain.getList() :
                train.resume()
        ADmainMenu.resumeButton.enabled = False
        ADmainMenu.pauseButton.enabled = True
        ADmainMenu.stopButton.enabled = AutoDispatcher.loop
            
    def saveBeforeExit(self) :
        # Give user the possibility of saving settings and trains position
        # before quitting
        # Check if trains settings or position were changed
        if AutoDispatcher.trainsDirty :
            # Yes, allow user to save them
            msg = "Save trains settings and positions before quitting? "
            if not ADpowerMonitor.powerOn :
                msg += ("(Warning, power is off and trains position could"
                  + " be inconsistent!)")
            if (JOptionPane.showConfirmDialog(None, msg, "Confirmation",
              JOptionPane.YES_NO_OPTION) == 0) :
                self.saveTrains()
                self.saveSettings()
                return
        # Check if settings were changed
        if AutoDispatcher.preferencesDirty :
            # Yes, allow user to save them
            if (JOptionPane.showConfirmDialog(None, 
              "Save preferences before quitting? ", "Confirmation", 
              JOptionPane.YES_NO_OPTION) == 0) :
                self.saveSettings()

# SPEED CONTROL TASK ==============

    def speedControl(self) :
        # Handle to control train speeds
        # Executed in a separate thread
        # Loops until the background handle exits
        while not AutoDispatcher.stopped :
            # Take current time - Will be used when testing
            # for stalled trains and to avoid throttle timeout
            # (on some command stations)
            currentTime = System.currentTimeMillis()
            # Check all trains
#            if not AutoDispatcher.paused :
            for train in ADtrain.getList() :
                # Check for train stalled
                if (train.running and ADsettings.stalledDetection !=
                  ADsettings.DETECTION_DISABLED and train.lastMove != -1L
                  and not AutoDispatcher.paused
                  and not AutoDispatcher.simulation) :
                    if (currentTime - train.lastMove >
                      ADsettings.stalledTime) :
                        train.lastMove = -1L
                        if (ADsettings.stalledDetection ==
                           ADsettings.DETECTION_PAUSE) :
                            AutoDispatcher.instance.stopAll()
                        AutoDispatcher.chimeLog(ADsettings.STALLED_SOUND,
                          "Train " + train.getName() + " stalled in section \""
                          + train.section.getName() + "\"")
                # Take care of speed changes only for trains with locomotive 
                # and throttle
                locomotive = train.locomotive
                if locomotive != None and locomotive.throttle != None :
                    # Do nothing if train already running at target speed.
                    # Compare speeds rounding them, to cope with different 
                    # precisions in Jython (double) and Java (float)
                    targetSpeed = round(locomotive.targetSpeed, 4)
                    presentSpeed = round(locomotive.currentSpeed, 4)
                    if presentSpeed < 0 :
                        presentSpeed = 0
                    if presentSpeed != targetSpeed :
                        # Train not running at target speed
                        # Should we stop train ?
                        if targetSpeed < 0 :
                            # Yes - Stop it immediately!
                            if presentSpeed > 0 :
                                locomotive.throttle.setSpeedSetting(targetSpeed)
                                locomotive.rampingSpeed = locomotive.currentSpeed = 0
                                locomotive.currentSpeedSwing.setText("0")
                                locomotive.updateMeter()
                        else :
                            # Should we increase or decrease speed?
                            if presentSpeed < targetSpeed :
                                # Acceleration
                                delta = locomotive.accStep
                            else :
                                # Deceleration (adjusted for self-learning)
                                delta = (-locomotive.decStep -
                                  locomotive.decAdjustment)
                            # Should we progressively vary speed?
                            if delta == 0 :
                                # No - Apply target speed
                                locomotive.throttle.setSpeedSetting(targetSpeed)
                                locomotive.currentSpeed = targetSpeed
                                locomotive.lastSent = currentTime
                                locomotive.rampingSpeed = targetSpeed
                                if targetSpeed <= 0 :
                                    locomotive.updateMeter()
                                locomotive.currentSpeedSwing.setText(
                                  str(targetSpeed))
                            else :
                                # Progressive speed change
                                delta *= ADsettings.speedRamp
                                locomotive.rampingSpeed += delta
                                # Make sure speed is within target 
                                if ((locomotive.rampingSpeed > targetSpeed 
                                   and delta > 0) or (locomotive.rampingSpeed <
                                   targetSpeed and delta < 0)) :
                                    locomotive.rampingSpeed = targetSpeed
                                # Were we braking in self-learning mode?
                                if locomotive.brakeAdjusting :
                                    #Yes, did we reach target (i.e. minimum) 
                                    # speed?
                                    if locomotive.rampingSpeed == targetSpeed :
                                        # Inform self-learning method
                                        locomotive.learningEnd()
                                # Apply new speed only if change is meaningful
                                locomotive.rampingSpeed = round(
                                  locomotive.rampingSpeed,4)
                                # Round values based on number of speed steps
                                # used by the locomotive
                                if (round(float(locomotive.rampingSpeed) *
                                  locomotive.stepsNumber) !=
                                  round(float(presentSpeed) *
                                  locomotive.stepsNumber)) :
                                      # Meaningful change - apply new speed
                                    locomotive.throttle.setSpeedSetting(
                                      locomotive.rampingSpeed)
                                    locomotive.currentSpeed = locomotive.rampingSpeed
                                    locomotive.lastSent = currentTime
                                       # Update locomotives window (if open)
                                    locomotive.currentSpeedSwing.setText(
                                      str(locomotive.rampingSpeed))
                                    # If train stopped, update mileage and
                                    # operation hours
                                    if targetSpeed <= 0 :
                                        locomotive.updateMeter()
                    # If requested, periodically change speed (even if not 
                    # needed), in order to avoid time-out.
                    # Some command stations stop a locomotive if it did not
                    # receive commands for a while.
                    if (locomotive.rampingSpeed > 0 and ADsettings.maxIdle 
                       > 0 and currentTime - locomotive.lastSent >
                       ADsettings.maxIdle) :
                        locomotive.throttle.setSpeedSetting(
                          locomotive.rampingSpeed)
                        locomotive.currentSpeed = locomotive.rampingSpeed
                        locomotive.lastSent = currentTime
            # Wait n/10 of second before repeating
            # This thread is no time critical, since it only varies speeds 
            # of running trains .
            # Trains stopping is dealt with by block's ChangeListener
            sleep(float(ADsettings.speedRamp)*0.1)

# LOCOMOTIVES MAINTENANCE TIME CONTROL TASK ==============

    def maintenanceControl(self) :
        # Periodically check if any locomotive exceedes the maximum
        # mileage or operation time 
        while not AutoDispatcher.stopped :
            # Convert hours to milliseconds
            intTime = int(ADsettings.maintenanceTime * 3600000.)
            for l in ADlocomotive.getList() :
                newWarnedTime = newWarnedMiles = False
                if (ADsettings.maintenanceTime > 0. 
                  and l.runningTime > intTime) :
                    newWarnedTime = True
                if (ADsettings.maintenanceMiles > 0. 
                  and l.mileage > ADsettings.maintenanceMiles) :
                    newWarnedMiles = True
                # make sure the warning is given only once
                if (not l.warnedTime and not l.warnedMiles and
                  (newWarnedTime or newWarnedMiles)) :
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
                      "Locomotive " + l.getName()  + " needs maintenance")
                l.warnedTime = newWarnedTime
                l.warnedMiles = newWarnedMiles
                # If mileage and opertions time are being displayed,
                # update swing fields
                if AutoDispatcher.locosFrame != None :
                    l.outputMileage()
            # Not time critical: wait for one minute
            sleep(60.)

    def blinkSignals(self) :
        # Separate thread to blink flashing signals on LayoutEditor panel
        # (does not affect blinking of model signals on layout)
        # Quit if there are no Signal Heads on the panel
        if len(self.layoutEditor.signalHeadImage) == 0 :
            return
        
        # Retrieve signalHeads
        signals = []
        aspectNames = ["SignalHeadStateRed", "SignalHeadStateGreen",
            "SignalHeadStateYellow", "SignalHeadStateLunar", 
            "SignalHeadStateFlashingRed", "SignalHeadStateFlashingGreen",
            "SignalHeadStateFlashingYellow", "SignalHeadStateFlashingLunar",
            "SignalHeadStateDark"]
        hasLunar = True
       # Are we running a JMRI version released after 24/9/2010?
        newIcons = not callable(getattr(self.layoutEditor.signalHeadImage[0],
              "getRedIcon", None))
        if newIcons :
            # New JMRI version (use getIcon, setIcon)
            # Get aspect names
            rbean = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle")
            for i in range(len(aspectNames)) :
                aspectNames[i] = rbean.getString(aspectNames[i])
            for s in self.layoutEditor.signalHeadImage :
                a = []
                for i in range(7) :
                    a.append(s.getIcon(aspectNames[i]))
                signals.append([s, s.getIcon(aspectNames[8]), a])
        else:
            # Old JMRI version (use getRedIcon... setRedIcon...)
            # Is Lunar aspect (introduced since JMRI 2.7.8) supported?
            hasLunar = callable(getattr(self.layoutEditor.signalHeadImage[0],
              "getFlashLunarIcon", None))
            for s in self.layoutEditor.signalHeadImage :
                a = []
                a.append([s.getRedIcon(), s.getFlashRedIcon()])
                a.append([s.getGreenIcon(), s.getFlashGreenIcon()])
                a.append([s.getYellowIcon(), s.getFlashYellowIcon()])
                if hasLunar :
                    a.append([s.getLunarIcon(), s.getFlashLunarIcon()])
                signals.append([s, s.getDarkIcon(), a])
        on = True
        cycling = False
        # Loop until handle exits
        while not AutoDispatcher.stopped :
            # Did user enable flashing?
            if ADsettings.flashingCycle > 0 :
                cycling = True
                for s in signals :
                    a = s[2]
                    # Light-on half cycle?
                    if on :
                        # Yes - switch on signal head
                        if newIcons :
                            for i in range(3) :
                                if a[i+4] != None :
                                    s[0].setIcon(aspectNames[i+4], a[i])  
                        else :
                            s[0].setFlashRedIcon(a[0][0])
                            s[0].setFlashGreenIcon(a[1][0])
                            s[0].setFlashYellowIcon(a[2][0])
                            if hasLunar :
                                s[0].setFlashLunarIcon(a[3][0])
                    else :
                        # No - set signal head to dark aspect
                        if newIcons :
                             for i in range(3) :
                                 if a[i+4] != None :
                                    s[0].setIcon(aspectNames[i+4], s[1])  
                        else:
                            s[0].setFlashRedIcon(s[1])
                            s[0].setFlashGreenIcon(s[1])
                            s[0].setFlashYellowIcon(s[1])
                            if hasLunar :
                                s[0].setFlashLunarIcon(s[1])
                # Force panel redrawing
                AutoDispatcher.repaint = True
                # Invert cycle
                on = not on
                # Wait for half cycle
                sleep(ADsettings.flashingCycle * 0.5)
                continue
            # User has disabled flashing
            # Was flashing enabled before?
            elif cycling > 0 :
                # Yes - take note we disable it
                cycling = False
                # restore flashing icon in signal heads
                for s in signals :
                    a = s[2]
                    if newIcons :
                        for i in range(3) :
                            if a[i+4] != None :
                                s[0].setIcon(aspectNames[i+4], a[i+4]) 
                    else:
                        s[0].setFlashRedIcon(a[0][1])
                        s[0].setFlashGreenIcon(a[1][1])
                        s[0].setFlashYellowIcon(a[2][1])
                        if hasLunar :
                            s[0].setFlashLunarIcon(a[3][1])
                # Force panel redrawing
                AutoDispatcher.repaint = True
            # Wait a second
            sleep(1.0)
        # Exiting thread
        # Restore original aspects
        for s in signals :
            a = s[2]
            if newIcons :
                for i in range(3) :
                    if a[i+4] != None :
                        s[0].setIcon(aspectNames[i+4], a[i+4]) 
            else:
                s[0].setFlashRedIcon(a[0][1])
                s[0].setFlashGreenIcon(a[1][1])
                s[0].setFlashYellowIcon(a[2][1])
                if hasLunar :
                    s[0].setFlashLunarIcon(a[3][1])
        # Force panel redrawing
        AutoDispatcher.repaint = True

# I/O =================

    # Data are stored as Java objects.
    # Saving them as XML file would be nicer, but would
    # require adding new DTD files in JMRI
    
    def saveSettings(self) :
        # Save settings to disk
        # Get current settings
        newSections = ADsection.getSectionsTable()
        newBlocks = ADsection.getBlocksTable()
        # Compare current settings with those
        # automatically computed by the program at startup
        for i in range(len(newSections)) :
            newS = newSections[i]
            autoS = self.autoSections[i]
            for j in range(1, 3) :
                # Is user selection different from settings computed by program?
                if newS[j] != autoS[j] :
                    # Yes - if new settings are "No selection",
                    # set value to "NO"
                    if newS[j] == "" :
                        newS[j] = "NO"
        for i in range(len(newBlocks)) :
            newS = newBlocks[i]
            autoS = self.autoBlocks[i]
            for j in range(1, len(autoS)) :
                if j == 6 or j == 12 :
                    continue
                # Is user selection different from settings computed by program?
                if newS[j] != autoS[j] :
                    # Yes - if new settings are "No selection", 
                    # set value to "NO"
                    if newS[j] == "" :
                        newS[j] = "NO"
        # Write to file
        try :
            outs=ObjectOutputStream(FileOutputStream(self.settingsFile))
            try :
                ADsettings.save(outs, newSections, newBlocks)
                ADmainMenu.saveSettingsButton.enabled = False
                AutoDispatcher.preferencesDirty = False
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                  "Preferences saved to disk")
            finally :
                outs.close()        
        except IOException, ioe :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Error writing to file " + self.settingsFile)

    def loadSettings(self) :
        # Get settings from disk
        ins = None
        try :
            ins=ObjectInputStream(FileInputStream(self.settingsFile))
            try :
                ADsettings.load(ins.readObject())
            except IOException, ioe :
                AutoDispatcher.log("Warning, could not read file " +
                  self.settingsFile + ". Default layout settings will be used.")
            ins.close()
            ins = None
        except IOException, ioe :
            if ins != None :
                ins.close()

    def saveLocomotives(self) :
        locomotives = []
        locos = ADlocomotive.getNames()
        locos.sort()
        # Extract persistent info
        for l in locos :
            ll = ADlocomotive.getByName(l)
            locomotives.append([ll.name, ll.address, ll.speed,
              ll.acceleration, ll.deceleration, ll.runningTime, ll.mileage])
        # Write to file
        try :
            outs=ObjectOutputStream(FileOutputStream(self.locoFile))
            try :
                outs.writeObject(locomotives)
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                  "Locomotives changes saved to disk")
            finally :
                outs.close()        
        except IOException, ioe :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Error writing file " + self.locoFile)

    def loadLocomotives(self) :
        try :
            ins=ObjectInputStream(FileInputStream(self.locoFile))
            try :
                newLocomotives=ins.readObject()
                AutoDispatcher.locomotives = newLocomotives
            except IOException, ioe :
                AutoDispatcher.log("Warning, could not read file " +
                  self.locoFile + " Default settings will be used.")
            ins.close()
        except IOException, ioe :
            return
            
    def saveTrains(self) :
        # First of all, save locomotives
        self.saveLocomotives()
        trains = []
        # Extract persistent info
        for t in ADtrain.getList() :
            if t.direction == ADsettings.ccw :
                direction = ADsettings.directionNames[0]
            else :
                direction = ADsettings.directionNames[1]
            # Force saving of schedule status in the stack
            # (This is why trains must be stopped before saving!)
            t.schedule.push()
            # Copy stack contents
            stack = []
            stack.extend(t.schedule.stack)
            # Restore original stack contents
            t.schedule.pop()
            # Get current section name
            if t.section != None :
                sectionName = t.section.getName()
            else :
                sectionName = ""
            # Get pending commands (if any)
            pendingCommands = []
            for i in range(len(t.itemSections)) :
                section = t.itemSections[i].getName()
                action = t.items[i].action
                value = t.items[i].value
                message = t.items[i].message
                # Convert instances (sections or signals) to names
                if (action == ADschedule.WAIT_FOR or
                   action == ADschedule.MANUAL_OTHER or
                   action == ADschedule.HELD or
                   action == ADschedule.RELEASE or
                   action == ADschedule.IFH) :
                    value = value.getName()
                pendingCommands.append([section, action, value, message])
            # Get name of last section on the route
            if t.lastRouteSection == None :
                lastSection = ""
            else :
                lastSection = t.lastRouteSection.getName()
            # Add a record
            trains.append([t.name, sectionName, direction, 
                t.resistiveWheels, t.schedule.text, t.trainAllocation, 
                t.trainLength, stack, t.locoName, t.reversed,
                pendingCommands, t.engineerName, lastSection, t.trainSpeed,
                t.brakingHistory, t.canStopAtBeginning, t.startAction])
        # Write everything to file
        try :
            outs=ObjectOutputStream(FileOutputStream(self.trainFile))
            try :
                outs.writeObject(trains)
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                  "Trains status saved to disk")
            finally :
                outs.close()        
        except IOException, ioe :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Error writing to file " + self.trainFile)
        ADmainMenu.saveTrainsButton.enabled = AutoDispatcher.trainsDirty = False
 
    def loadTrains(self) :
        # Read train data into memory
        # Appropriate data conversions will be performed later by
        # ADtrain.buildRoster method
        try :
            ins=ObjectInputStream(FileInputStream(self.trainFile))
            try :
                newTrains=ins.readObject()
                self.trains = newTrains
            except IOException, ioe :
                AutoDispatcher.log("Warning, could not read file " +
                  self.trainFile + ", Default train settings will be used.")
            ins.close()
        except IOException, ioe :
            return
                
    # SIMULATION =================

    # The following methods are used only in simulation mode

    def oneStep(self) :
        # Advance every train of one block, unless it
        # reached the stop block in front of a red signal.
        # Ignore calls if stopped, paused or without power
        if (AutoDispatcher.stopped or AutoDispatcher.paused or not
           ADpowerMonitor.powerOn) :
            return
        # Move running trains one block ahead
        for t in ADtrain.getList() :
            # Did train advance during previous call?
            if t.simSensor != None :
                # Yes, remove it from previous block
                t.simSensor.setKnownState(INACTIVE)
                t.simSensor = None
                continue
            # No - Is train running?
            if (not t.running or (t.locomotive != None and
              t.engineerSetLocomotive != None and
              t.locomotive.getThrottleSpeed() <= 0)) :
                continue
            # Is the train anywhere?
            section = t.section
            direction = t.direction
            if section != None :
                # Yes, find present block
                blocks = section.getBlocks(direction)
                ind = -1
                for i in range(len(blocks)) :
                    if blocks[i].getOccupancy() == Block.OCCUPIED :
                        ind = i
                        break
                if ind < 0 :
                    continue
                # Did train reach a stop block in front of a red signal?
                if (blocks[ind] == section.stopBlock[direction] and
                   section.getSignal(direction).getIndication() == 0) :
                    continue
                # No - get sensor of current block
                currentSensor = blocks[ind].getOccupancySensor()
                # Is this block section's exit block?
                block = None
                for e in t.entriesAhead :
                    if e.getInternalBlock() == blocks[ind] :
                        # Yes - Get entry block in the next section
                        block = e.getExternalBlock()
                        while e in t.entriesAhead :
                            t.entriesAhead.pop(0)
                        break
                if block == None :
                    # Train did not reach the exit block
                    # Get next block in the same section
                    ind += 1
                    # Should not occur, but it's better to check
                    if ind >= len(blocks) :
                        continue
                    block = blocks[ind]
               # Get next sensor
                nextSensor = block.getOccupancySensor()
                # Move train from current block to next one,
                # making sure the block has a sensor!
                if nextSensor != currentSensor and nextSensor != None :
                    nextSensor.setKnownState(ACTIVE)
                    # Take note that next time we must release the old block
                    t.simSensor = currentSensor

    def autoStep(self) :
        # Step trains forward every two seconds
        while not AutoDispatcher.stopped :
            # Call "oneStep" once per second
            # actual advancement will occur once every two seconds,
            # since during one call next block is occupied
            # and during next call previous block is released
            sleep(1.0)
            if ADmainMenu.autoButton.isSelected() :
                self.oneStep()
                
# CHILDREN CLASSES ==============

# SECTION ==============

class ADsection (PropertyChangeListener) :
    # Encapsulates JMRI Section, adding fields and methods used 
    # by AutoDispatcher

    # Section colors enumeration
    EMPTY_SECTION_COLOR = 0
    MANUAL_SECTION_COLOR = 1
    OCCUPIED_SECTION_COLOR = 2
    CCW_ALLOCATED_COLOR = 3
    CW_ALLOCATED_COLOR = 4
    CCW_TRAIN_COLOR = 5
    CW_TRAIN_COLOR = 6
    
    # Static variables
    userNames ={}       # dictionary of sections by userName
    systemNames ={}     # dictionary of sections by systemName
    sensorNames = None  # list of sensors that may be used for manual control
                        # (i.e. those not used for block occupancy)

    # STATIC METHODS

    def getList() :
        return ADsection.systemNames.values()
    getList = ADstaticMethod(getList)

    def getNames() :
        return ADsection.userNames.keys()
    getNames = ADstaticMethod(getNames)

    def getByName(name) :
        if ADsection.userNames.has_key(name) :
            return ADsection.userNames[name]
        return ADsection.systemNames.get(name, None)
    getByName = ADstaticMethod(getByName)

    def getSectionsTable() :
        # Creates a table containing sections' attributes as strings.
        # Uses current settings in order to translate direction names.
        # The output table, sorted by section name, contains:
        #   Section Name
        #   One-Way Indicator
        #   Transit-Only (and burst) Indicator
        #   Signal names
        #   Manual flipping Indicator
        #   Name of manual control sensor
        #   Signal held indicators
        out = []
        for s in ADsection.systemNames.values() :
            if s.direction == 3 :
                direction = ""
            elif s.direction == ADsettings.ccw + 1 :
                direction = ADsettings.directionNames[0]
            else :
                direction = ADsettings.directionNames[1]
            if s.burst :
                burst = "+"
            else :
                burst = ""
            if s.transitOnly[ADsettings.ccw] :
                if s.transitOnly[1-ADsettings.ccw] :
                    transitOnly = (ADsettings.directionNames[0] + "-" 
                      + ADsettings.directionNames[1] + burst)
                else :
                    transitOnly = ADsettings.directionNames[0] + burst
            elif s.transitOnly[1-ADsettings.ccw] :
                transitOnly = ADsettings.directionNames[1] + burst
            else :
                transitOnly = ""
            signals =["", ""]
            heldIndicators =["", ""]
            for i in range(2) :
                if s.signal[i] != None :
                    signals[i] = s.signal[i].name
                    if s.signal[i].isHeld() :
                        heldIndicators[i] = "Held"
            if s.manuallyFlipped :
                inverted = "INVERTED"
            else :
                inverted = ""
            if s.isManual() :
                manualSection = "Manual"
            else :
                manualSection = ""
            if s.manualSensor == None :
                manualSensor = ""
            else :
                manualSensor = s.manualSensor.getUserName()
                if manualSensor == None or manualSensor == "" :
                    manualSensor = s.manualSensor.getSystemName()
            out.append([s.name, direction, transitOnly, signals[0],
              signals[1], inverted, manualSensor, s.stopAtBeginning,
              heldIndicators, manualSection])
        out.sort()
        return out
    getSectionsTable = ADstaticMethod(getSectionsTable)

    def putSectionsTable() :
        # Updates sections, based on a table of attributes in string format.
        # Uses as input the table in the format provided by the 
        # getSectionsTable method and contained in current settings
        for i in ADsettings.sections :
            section = ADsection.getByName(i[0])
            if section != None :
                if i[1] == "NO" :
                    section.direction = 3
                elif (i[1] == "CCW" or i[1] == "EAST" or i[1] == "NORTH"
                   or i[1] == "LEFT" or i[1] == "UP") :
                    section.direction = ADsettings.ccw + 1
                    section.transitOnly[ADsettings.ccw] = False
                elif (i[1] == "CW" or i[1] == "WEST" or i[1] == "SOUTH"
                   or i[1] == "RIGHT" or i[1] == "DOWN") :
                    section.direction = 2 - ADsettings.ccw
                    section.transitOnly[1-ADsettings.ccw] = False
                i2 = i[2]
                if i2.endswith("+") :
                    i2 = i2[:len(i2)-1]
                    burst = True
                else :
                    burst = False
                if i2 == "NO" :
                    section.transitOnly[0] = section.transitOnly[1] = False
                    section.burst = False
                elif (i2 == "CCW-CW" or i2 == "EAST-WEST" or i2 == "NORTH-SOUTH"
                   or i2 == "LEFT-RIGHT" or i2 == "UP-DOWN") :
                    section.transitOnly[0] = section.transitOnly[1] = True
                    section.burst = burst
                elif (i2 == "CCW" or i2 == "EAST" or i2 == "NORTH"
                   or i2 == "LEFT" or i2 == "UP") :
                    section.transitOnly[ADsettings.ccw] = True
                    section.transitOnly[1-ADsettings.ccw] = False
                    section.burst = burst
                elif (i2 == "CW" or i2 == "WEST" or i2 == "SOUTH"
                   or i2 == "RIGHT" or i2 == "DOWN") :
                    section.transitOnly[1-ADsettings.ccw] = True
                    section.transitOnly[ADsettings.ccw] = False
                    section.burst = burst
                for j in range(2) :
                    if (i[j+3] != "" and (section.signal[j] == None or 
                       i[j+3] != section.signal[j].name)) :
                        if section.signal[j] != None :
                            section.signal[j].changeUse(-2)
                        section.signal[j] = ADsignalMast.provideSignal(i[j+3])
                        section.signal[j].changeUse(1)
                if i[5] == "INVERTED" :
                    section.manuallyFlip()
                sensorName = i[6]
                if sensorName == "NO" :
                    section.manualSensor = None
                elif sensorName.strip() != "" :
                    section.manualSensor = (
                    InstanceManager.sensorManagerInstance().getSensor(sensorName))
                if len(i) > 7 :
                    section.stopAtBeginning = i[7]
                    if len(i) > 9 and ADsettings.autoRestart :
                        for j in range(2) :
                            if (i[8][j] == "Held" and section.signal[j] != None
                             and section.signal[j].hasIcon()) :
                                section.signal[j].setHeld(True)
                        if i[9] == "Manual":
                            section.setManual(True)
                        
    putSectionsTable = ADstaticMethod(putSectionsTable)

    def getBlocksTable() :
        # Creates a table containing blocks' attributes as strings.
        # The output table, sorted by section name, contains:
        #   Block Name
        #   Stop-Block Indicator
        #   Allocation-Block Indicator
        #   Safe-Point Indicator
        #   Maximum speed name
        #   Brake-Block Indicator
        #   List of block actions for each direction
        # Within the section, blocks are sorted in accordance to 
        # internal direction
        out = []
        sections = ADsection.getNames()
        sections.sort()
        for sectionName in sections :
            section = ADsection.getByName(sectionName)
            for block in section.getBlocks(True) :
                outData = [block.getName()]
                for j in range (2) :
                    if block == section.stopBlock[j]:
                        outData.append("STOP")
                    else :
                        outData.append("")
                    if block == section.allocationPoint[j]:
                        outData.append("ALLOCATE")
                    else :
                        outData.append("")
                    if block == section.safePoint[j]:
                        outData.append("SAFE")
                    else :
                        outData.append("")
                    speedIndex = block.getSpeed(j)
                    if speedIndex == 0 :
                        outData.append("")
                    else :
                        outData.append(
                          ADsettings.speedsList[speedIndex-1])
                    if block == section.brakeBlock[j]:
                        outData.append("BRAKE")
                    else :
                        outData.append("")
                    outData.append(block.action[j])
                out.append(outData)
        return out
    getBlocksTable = ADstaticMethod(getBlocksTable)

    def putBlocksTable() :
        # Updates blocks, based on a table of attributes in string format.
        # Uses as input the table in the format provided by the 
        # getBlocksTable method and contained in current settings
        for i in ADsettings.blocks :
            block = ADblock.getByName(i[0])
            if block != None :
                section = block.getSection()
                jj = 1
                for j in range(2) :
                    if i[jj] == "STOP" :
                        section.stopBlock[j] = block
                    jj +=1
                    if i[jj] == "ALLOCATE" :
                        section.allocationPoint[j] = block
                    jj +=1
                    if i[jj] == "SAFE" :
                        section.safePoint[j] = block
                    elif i[jj] == "NO"  and section.safePoint[j] == block :
                        section.safePoint[j] = None
                    jj +=1
                    speedName = i[jj]
                    for speedIndex in range(len(ADsettings.speedsList)) :
                        if (speedName == 
                           ADsettings.speedsList[speedIndex]) :
                            block.speed[j] = speedIndex + 1
                            break
                    jj +=1
                    if i[jj] == "BRAKE" :
                        section.brakeBlock[j] = block
                    jj +=1
                    block.action[j] = i[jj]
                    jj +=1
    putBlocksTable = ADstaticMethod(putBlocksTable)

    def setListeners() :
        # Add a property change listener for each section that has a sensor 
        # for manual control
        for section in ADsection.systemNames.values() :
            if(section.manualSensor != None) :
                # add the listener
                section.manualSensor.addPropertyChangeListener(section)
    setListeners = ADstaticMethod(setListeners)

    def removeListeners() :
        # Remove the manual control sensor listener of each section
        for section in ADsection.systemNames.values() :
            if(section.manualSensor != None) :
                # remove the listener
                section.manualSensor.removePropertyChangeListener(section)
    removeListeners = ADstaticMethod(removeListeners)

    # INSTANCE METHODS

    def __init__(self, systemName):
        # Retrieve Section.java instance from JMRI
        self.jmriSection = (
          InstanceManager.getDefault(jmri.SectionManager).getBySystemName(systemName))
        # Get section name
        self.name = self.jmriSection.getUserName()
        if self.name == None or self.name.strip() == "" :
            self.name = systemName
        self.name = AutoDispatcher.cleanName(self.name)
        # Swing variables used in Blocks window
        self.stopGroup = [ButtonGroup(), ButtonGroup()]
        self.brakeGroup = [ButtonGroup(), ButtonGroup()]
        self.brakeNoneSwing = [JRadioButton(""), JRadioButton("")]
        self.brakeNoneSwing[0].setHorizontalAlignment(JLabel.CENTER)
        self.brakeNoneSwing[1].setHorizontalAlignment(JLabel.CENTER)
        self.brakeGroup[0].add(self.brakeNoneSwing[0])
        self.brakeGroup[1].add(self.brakeNoneSwing[1])
        self.safeGroup = [ButtonGroup(), ButtonGroup()]
        self.safeNoneSwing = [JRadioButton(""), JRadioButton("")]
        self.safeNoneSwing[0].setHorizontalAlignment(JLabel.CENTER)
        self.safeNoneSwing[1].setHorizontalAlignment(JLabel.CENTER)
        self.safeGroup[0].add(self.safeNoneSwing[0])
        self.safeGroup[1].add(self.safeNoneSwing[1])
        self.allocationGroup = [ButtonGroup(), ButtonGroup()]
        # Find out all blocks contained in the section
        # Make sure JMRI section direction is forward!
        # Otherwise we will get the list in reversed order.
        self.blockList = []         # Blocks contained in the section
        self.jmriSection.setState(Section.FREE)
        newBlock = self.jmriSection.getEntryBlock()
        while newBlock != None :
            # Get the corresponding LayoutBlock
            # Without LayoutBlock we cannot setup connectivity
            layoutBlock = None
            blockName = newBlock.getUserName()
            if blockName != None and blockName.strip() != "" :
                layoutBlock = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getLayoutBlock(blockName)
            if layoutBlock == None :
                blockName = newBlock.getSystemName()
                layoutBlock = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getLayoutBlock(blockName)
            if layoutBlock == None :
                AutoDispatcher.log("No LayoutBlock for Block " + blockName
                  + " found: block skipped!")
            else :
                # make sure that block is included in one section only
                block = ADblock.getByName(blockName)
                if block != None :
                    AutoDispatcher.log("Block " + blockName + " included in both " 
                        + block.getSection().getName() +
                        " and " + self.name + 
                        " sections. AutoDispatcher " + 
                        "is unable to handle this case.")
                    # Continue even if results are unpredictable
                # Add the layoutBlock as sub-block
                self.blockList.append(ADblock(self, layoutBlock, blockName))
            newBlock = self.jmriSection.getNextBlock()
        # Make sure section contains at least one block
        blocksNumber = len(self.blockList)
        if blocksNumber < 1 :
            AutoDispatcher.log("Section " + self.name
              + "contains no valid  block. Section skipped!")
            return
        # Update the maximum number of blocks per section (will be used to 
        # determine default settings)
        if AutoDispatcher.maxBlocksPerSection < blocksNumber :
            AutoDispatcher.maxBlocksPerSection = blocksNumber
        # Include names in dictionaries
        ADsection.systemNames[AutoDispatcher.cleanName(systemName)] = self
        ADsection.userNames[self.name] = self
        self.forwardEntries=[]      # Entries
        self.reverseEntries=[]      # Exits
        self.xings = []             # Sections crossed by this section
        self.stopBlock = [None, None]
        self.brakeBlock = [None, None]
        self.allocationPoint = [None, None]
        self.safePoint = [None, None]
        self.manualSensor = None
        self.memoryVariable = jmri.InstanceManager.memoryManagerInstance().getMemory(self.name)
        self.stopAtBeginning = [-1.0, -1.0]
        # Assume that order of blocks and entries is not reversed
        self.reversed = False
        # Assume that user did not flip yet the order of blocks
        self.manuallyFlipped = False
        # Signals at the two exits of the section
        self.signal = [None, None]
        # Permitted train direction
        self.direction = 3  # 3 = both
        # Trains can stop
        self.transitOnly = [False, False]
        # "Burst" mode on transit-ony sections disabled
        self.burst = False
        # Section does not contain yet the head of a train
        self.trainHead = False
        # Section is not occupied yet
        self.occupied = False 
        # Section is not allocated yet
        self.allocated = None
        # Section length.  The value si recomputed each time a train travels
        # through the section, since actual length depends upon the entry and
        # exit points the train comes thru
        self.sectionLength = 0.0
        # Allocated train direction in this block
        self.trainDirection = 0
        # Indicator of exit turnouts position
        # True if at least one turnout is thrown
        self.turnoutsThrown = False
 
        # Retrieve the list of sensor names from JMRI, unless already done.
        # It will be used to populate sensor combo boxes
        if ADsection.sensorNames == None :
            ADsection.sensorNames = [""]
            # Remove from the list sensors associated with blocks
            # (this makes user selection easier and faster)
            # Get all blocks
            blocks = InstanceManager.getDefault(jmri.BlockManager).getNamedBeanSet()
            blockSensors = []
            for b in blocks :
                sensor = b.getSensor()
                if sensor != None :
                    blockSensors.append(sensor)
            sensors = InstanceManager.sensorManagerInstance().getNamedBeanSet()
            for s in sensors:
                if not s in blockSensors :
                    sensorName = s.getUserName()
                    if sensorName == None or sensorName.strip() == "" :
                        sensorName = s
                    if sensorName != "ISCLOCKRUNNING" :
                        ADsection.sensorNames.append(sensorName)
            ADsection.sensorNames.sort()
            
        # Swing variables used in Sections window
        # We should probably move them to the GUI
        self.oneWaySwing = JComboBox()
        self.transitOnlySwing = JComboBox()
        self.signalSwing = [JComboBox(), JComboBox()]
        self.manualSensorSwing = JComboBox()
        self.stopAtBeginningSwing = [JCheckBox("", False), JCheckBox("", False)]
        self.stopAtBeginningDelay = [JTextField("0.0", 4), JTextField("0.0", 4)]
                
    def setEntries(self):
        # Find section's entry points
        self.forwardEntries = self.__setEntries__(
          self.jmriSection.getForwardEntryPointList())
        self.reverseEntries = self.__setEntries__(
          self.jmriSection.getReverseEntryPointList())
        # Set default stop blocks
        self.brakeBlock[0] = self.stopBlock[0] = self.__setStopBlocks__(
          self.getBlocks(False), self.forwardEntries)
        self.brakeBlock[1] = self.stopBlock[1] = self.__setStopBlocks__(
          self.getBlocks(True), self.reverseEntries)
        # Set default allocation blocks and safe points
        self.safePoint[0] = self.allocationPoint[1] = self.stopBlock[0]
        self.safePoint[1] = self.allocationPoint[0] = self.stopBlock[1]
        if len(self.blockList) > 1 :
            # Set default brake block to block preceding stop block
            for i in range(2) :
                found = False
                for block in self.getBlocks(1-i) :
                    if found :
                        self.brakeBlock[i] = block
                        break
                    found =  block == self.stopBlock[i]
        else :
            # Section contains only one block. 
            # Brake and safe points are meaningless
            self.brakeBlock[0] = self.brakeBlock[1] = None
            self.safePoint[0] = self.safePoint[1] = None
            
    def __setEntries__(self, entries) :
        # Internal method used to retrieve entry points from JMRI
        entryPoints=[]
        for i in range(entries.size()) :
            # Get a JMRI EntryPoint
            entry = entries.get(i)
            # Create our own entry instance
            entryPoint = ADentry(self, entry)
            # If the creation failed, discard the instance
            if not entryPoint.inError() :
                entryPoints.append(entryPoint)
                entryPoint.getInternalBlock().entryBlock = True
        return entryPoints

    def __setStopBlocks__(self, blocks, entries) :
        # Stop point is set to the first exit block encountered
        block = None
        for b in blocks :
            for e in entries :
                if b == e.getInternalBlock() :
                    return b
            block = b
        return block

    def addXing(self, otherSection):
        # Add an item to the list of sections crossed by this section
        self.xings.append(otherSection)

    def setDefault(self):
        # Set transit-only indicators to default values
        # Any section containing a Xing or only one block
        # (unless all sections are one block long) is 
        # initially considered as transit-only in both directions
        # (user can change settings later in "Blocks" window)
        if (len(self.xings) > 0 or (len(self.blockList) < 2 and
           AutoDispatcher.maxBlocksPerSection > 1)) :
            self.transitOnly[0] = self.transitOnly[1] = True

    def getName(self):
        return self.name
        
    def getJmriSection(self):
        return self.jmriSection
        
    def getEntries(self, backwards):
        # Return entries (backwards == False)
        # or exits (backwards == True)
        # swapping them if order of blocks is reversed
        if backwards == self.reversed :
            return self.forwardEntries
        else :
            return self.reverseEntries

    def isReversed (self) :
        # Is the order of blocks reversed?
        return self.reversed

    def setReversed (self, reversed) :
        # Set the indicator of reversed order of blocks
        if reversed == self.reversed :
            return
        self.reversed = reversed
        # Flip contents of tables
        self.stopBlock.reverse()
        self.brakeBlock.reverse()
        self.allocationPoint.reverse()
        self.safePoint.reverse()
        # Do the same for blocks
        for block in self.blockList :
            block.speed.reverse()
            block.action.reverse()
        
    def getSignal(self, direction):
        if direction < 0 :
            direction = 0
        elif direction > 1 :
            direction = 1
        return self.signal[direction]

    def getStopAtBeginning(self, direction):
        if direction < 0 :
            direction = 0
        elif direction > 1 :
            direction = 1
        return self.stopAtBeginning[direction]

    def manuallyFlip(self) :
        # Reverse section's direction and take note that user requested flipping
        # (Can occur only for reversing-sections)
        self.setReversed(not self.reversed)
        self.manuallyFlipped = not self.manuallyFlipped
        
    def setOccupied(self) :
        # Set section to occupied (unless already set)
        if not self.occupied :
            self.empty = False
            self.occupied = True
            # Update section color
            self.setColor()

    def checkOccupancy(self) :
        # If already empty, ignore (shouldn't happen)
        if not self.occupied and self.allocated == None :
            return
        # Clear occupancy status only if all sub-blocks are empty
        for b in self.blockList :
            if b.getOccupancy() == Block.OCCUPIED :
                return
        # report error if the train "disappeared"
        if self.trainHead and self.allocated != None :
            # Avoid the warning when user is allowed to manually perform
            # switching operations
            if (not AutoDispatcher.stopped and not AutoDispatcher.paused and 
              not self.isManual() and
              ADsettings.derailDetection != ADsettings.DETECTION_DISABLED) :
                if (ADsettings.derailDetection == 
                   ADsettings.DETECTION_PAUSE) :
                    AutoDispatcher.instance.stopAll()
                AutoDispatcher.chimeLog(ADsettings.DERAILED_SOUND,
                  "Train \"" + self.allocated.getName() + 
                  "\" derailed in section \"" + self.name + "\"")
                self.allocated.lastMove = -1L
            return
        # Now this section is empty
        self.empty = True
        # Release it if train reached the "safe point" in next section
        self.freeSectionIfTrainSafe()

    def freeSectionIfTrainSafe(self) :
        # Relase the section if :
        #   section is empty;
        # and
        #   train is equipped with resistive wheels
        # or
        #   the distance of train head from section boundary
        #   is greater than train length
        # or
        #   train reached the safe-point of next section
        
        train = self.allocated
        if not self.occupied and train == None :
            # Already released (can occur if sensor is re-triggered)
            return
        # Is section allocated to a train?
        if train != None :
            # Is train's tail in this section?
            if (len(train.previousSections) > 0 and
               train.previousSections[0] != self) :
                # No - Try releasing previous sections (if empty)
                previousCount = 0
                while True :
                    # Recursivelly call "freeSectionIfTrainSafe" while 
                    # sections continue being released
                    currentCount = len(train.previousSections)
                    if currentCount ==  0 or currentCount ==  previousCount :
                        break
                    previousCount = currentCount
                    train.previousSections[0].freeSectionIfTrainSafe()
                return
            # Yes, section contains train's tail - Is train using 
            # resistive wheels?
            if not train.resistiveWheels :
                # No - Did train reach the safe point?
                if not train.safe :
                    # No - Can we release the section based on train's length?
                    if (not ADsettings.useLength or
                       train.trainLength <= 0.) :
                        # No - We can thus not release the section
                        return
                    # Yes - See if the head of the train is far enough
                    if train.trainLength > train.distance :
                        # Not yet
                        return
        if not self.empty :
            # At least one block sensor still active
            # Test for lost cars
            if (ADsettings.lostCarsDetection ==
               ADsettings.DETECTION_DISABLED or AutoDispatcher.paused or
               AutoDispatcher.stopped or train == None) :
                return
            # Can we base detection of lost cars on train length?
            if (ADblock.blocksWithoutLength == 0 and
               train.trainLength > 0.) :
                # Yes, is the head of the train not too far from the tail?
                # (Let's introduce some tollerance to compensate 
                # for sensors debouncing)
                if (train.distance - train.trainLength
                   < ADsettings.lostCarsTollerance) :
                    # Not too far
                    return
            else :
                # No length available
                # Let's base detection on the number of sections 
                # not yet released
                if (len(train.previousSections) <=
                   ADsettings.lostCarsSections) :
                    # Not too far
                    return
            if (ADsettings.lostCarsDetection ==
               ADsettings.DETECTION_PAUSE) :
                AutoDispatcher.instance.stopAll()
            AutoDispatcher.chimeLog(ADsettings.LOST_CARS_SOUND,
              "Train \"" + train.getName() + 
              "\" lost cars in section \"" + self.name + "\"")
            return
        # One way or another we found that we can release the section
        self.occupied = False
        if train != None :
            # Update distance of train head from section boundary
            if len(train.previousSections) < 3 :
                # If train is in the next section, clear distance
                # We cannot release next section while the train is in it!
                train.distance = 0.
                train.blockLength = 0.
            else :
                # If train head is two or more sections ahead
                # decrease total distance by present section length
                train.distance -= self.sectionLength
                if train.distance < 0. :
                    train.distance = 0.
                train.blockLength = train.block.getLength()
            # Remove section from the list of train's passed sections (if any)
            while self in train.previousSections :
                train.previousSections.pop(0)
            # Take note that the section is not allocated any more
            self.allocate(None, 0)
        # Reset signals
        if self.signal[0] != None :
            self.signal[0].setIndication(0)
        if self.signal[1] != None :
            self.signal[1].setIndication(0)
        # Update section color
        self.setColor()
        # Should train stop at the start of next section?
        if train == None or not train.shouldStopAtBeginning() :
            return
        if(len(train.previousSections) > 0 and
          train.previousSections[0] != train.section) :
            return
        # Yes, stop train
        start_new_thread(self.stopTrain,
          (train, train.section.stopAtBeginning[train.direction],))
        
    def stopTrain(self, train, delay) :
        # Stop a train after an optional delay
        if delay > 0 :
            sleep(delay)
        # Make sure train is still braking
        if train.speedLevel != 1 :
            return
        train.arrived()
        if (train.locomotive == None or train.engineerSetLocomotive == None
          or AutoDispatcher.simulation) :
            train.stop()
        if train.locomotive != None :
            train.locomotive.learningStop()
        train.changeSpeed(0)
        
    def isOccupied (self) :
        return self.occupied

    def allocate(self, train, direction) :
        # Called when:
        #   section is allocated to a new train
        #   section is de-allocated
        #   train direction changed 
        # Update JMRI section state, if requested
        if ADsettings.sectionTracking :
            if self.trainDirection != direction or self.allocated != train :
                if train == None :
                    if self.allocated != None :
                        self.jmriSection.setState(Section.FREE)
                else :
                    if direction == self.reversed :
                        self.jmriSection.setState(Section.REVERSE)
                    else :
                        self.jmriSection.setState(Section.FORWARD)
        self.trainDirection = direction
        # If only direction changed, return
        if self.allocated == train :
            return
        self.trainHead = train != None and self.occupied
        self.allocated = train
        # Change train name in blocks (if required)
        self.changeTrainName()
        # Clear length.  Will be recomputed as the train advances
        self.sectionLength = 0.
        self.setColor()
                            
    def changeTrainName(self) :
        # Place/remove train name in jmri Memory Variable (if defined)
        # and in jmri Blocks (only if user enabled this option)
        if self.memoryVariable != None :
            if self.allocated == None :
                self.memoryVariable.setValue("")
            else :
                self.memoryVariable.setValue(self.allocated.getName())
        if not ADsettings.blockTracking :
            return
        if self.allocated == None :
            if self.occupied :
                for block in self.blockList :
                    block.setValue("")
        else :
            if self.occupied :
                trainName = self.allocated.getName()
                for block in self.blockList :
                    if block.getOccupancy() == Block.OCCUPIED :
                        block.setValue(trainName)
       
    def getAllocated (self) :
        return self.allocated

    def isAvailable(self) :
        # Check if section is available
        if self.occupied or self.allocated != None :
            # Section occupied or allocated
            return False
        # If the section has crossings, check the status of crossed sections
        for x in self.xings :
            if x.isOccupied() or x.getAllocated() != None :
                return False
        # Check if section is under manual control
        return not self.isManual()

    def isManual(self) :
        if self.manualSensor == None :
            return False
        return self.manualSensor.getKnownState() == Sensor.ACTIVE

    def setManual(self, on) :
        # DO it only if user defined a manual control sensor
        if self.manualSensor != None :
            if on :
                self.manualSensor.setKnownState(Sensor.ACTIVE)
            else :
                self.manualSensor.setKnownState(Sensor.INACTIVE)

    def setColor(self) :
        # Set section color depending on section status
        # (only if user enabled this option)
        if AutoDispatcher.stopped or not ADsettings.useCustomColors :
            return
        if self.allocated != None :
            if self.occupied :
                if not self.allocated.running and self.isManual() :
                    color = ADsettings.sectionColor[
                      ADsection.MANUAL_SECTION_COLOR]
                elif self.allocated.getDirection() == ADsettings.ccw :
                    color = ADsettings.sectionColor[ADsection.CCW_TRAIN_COLOR]
                else :
                    color = ADsettings.sectionColor[ADsection.CW_TRAIN_COLOR]
            elif self.allocated.getDirection() == ADsettings.ccw :
                color = ADsettings.sectionColor[ADsection.CCW_ALLOCATED_COLOR]
            else :
                color = ADsettings.sectionColor[ADsection.CW_ALLOCATED_COLOR]
        elif self.isManual() :
            color = ADsettings.sectionColor[ADsection.MANUAL_SECTION_COLOR]
        elif self.occupied :
            color = ADsettings.sectionColor[ADsection.OCCUPIED_SECTION_COLOR]
        else :
            color = ADsettings.sectionColor[ADsection.EMPTY_SECTION_COLOR]
        for block in self.blockList :
            block.setColor(color)

    def getBlocks(self, direction) :
        # Returns the list of blocks contained in the section
        # sorted in accordance to train direction
        if direction != self.reversed :
            return self.blockList
        l = []
        l.extend(self.blockList)
        l.reverse()
        return l

    def updateFromSwing(self) :
        # Update section settings in accordance to input swing fields
        direction = self.oneWaySwing.getSelectedItem()
        transitOnly = self.transitOnlySwing.getSelectedItem()
        if transitOnly.endswith("+") :
            burst = True
            transitOnly = transitOnly[:len(transitOnly)-1]
        else :
            burst = False
        self.direction = 3
        if direction == ADsettings.directionNames[0] :
            self.direction = ADsettings.ccw + 1
        elif direction == ADsettings.directionNames[1] :
            self.direction = 2 - ADsettings.ccw
        self.transitOnly[0] = self.transitOnly[1] = False
        self.burst = False
        if (transitOnly == ADsettings.directionNames[0] or 
           transitOnly == ADsettings.directionNames[0] + 
           "-" + ADsettings.directionNames[1]) :
            self.transitOnly[ADsettings.ccw] = True
            self.burst = burst
        if (transitOnly == ADsettings.directionNames[1] or 
           transitOnly == ADsettings.directionNames[0] + 
           "-" + ADsettings.directionNames[1]) :
            self.transitOnly[1-ADsettings.ccw] = True
            self.burst = burst
        for j in range(2) :
            newSignalName = self.signalSwing[j].getSelectedItem()[3:]
            if newSignalName != self.signal[j].name and newSignalName.strip() != "" :
                if self.signal[j] != None :
                    self.signal[j].changeUse(-1)
                self.signal[j] = ADsignalMast.provideSignal(newSignalName)
                self.signal[j].changeUse(1)
            if self.stopAtBeginningSwing[j].isSelected() :
                try :
                    self.stopAtBeginning[j] = float(self.stopAtBeginningDelay[j].text)
                except :
                    self.stopAtBeginning[j] = 0.0
                    self.stopAtBeginningDelay[j].text = "0.0"
            else :
                self.stopAtBeginning[j] = -1.0
                self.stopAtBeginningDelay[j].text = "0.0"
        sensorName = self.manualSensorSwing.getSelectedItem()
        if sensorName == None :
            self.manualSensor = None
        else :
            self.manualSensor = InstanceManager.sensorManagerInstance(
              ).getSensor(sensorName)


    def propertyChange(self, event) :
        # Listener, invoked when the Manual sensor status changes
        if (event.getPropertyName() != "KnownState" or 
           event.newValue == event.oldValue) :
            return
        if (event.newValue == Sensor.ACTIVE 
          or event.newValue == Sensor.INACTIVE) :
            # Status changed
            self.setColor()
                    
# ENTRY POINT ==============

class ADentry :
    # Replaces JMRI class EntryPoint
    def __init__(self, section, entry):
        self.internalBlock = None
        self.externalBlock = None
        # Should transiting trains invert direction (i.e. color)?
        self.directionChange = False
        # Does entry include double crossovers?
        self.xOver = []
        # Error during creation
        self.error = True
        blockName = entry.getBlock().getUserName()
        if blockName == None :
            blockName = entry.getBlock().getSystemName()
        self.internalBlock = ADblock.getByName(blockName)
        # Skip entry if entry block not included in any section
        if self.internalBlock == None :
            return
        # Skip entry if entry block not included in this section
        # (should not occur!)
        if self.internalBlock.getSection() != section :
            return
        blockName = entry.getFromBlock().getUserName()
        if blockName == None :
            blockName = entry.getFromBlock().getSystemName()
        # Skip entry if external block not included in any section
        # (it may occur)
        self.externalBlock = ADblock.getByName(blockName)
        if self.externalBlock == None :
            return
        # Creation successful: clear error flag
        self.error = False   
        
    def inError(self):
        # Did any error occur during instantation?
        return self.error
        
    def getInternalBlock(self):
        # return the block internal to the section
        return self.internalBlock
    
    def getExternalBlock(self):
        # return the block external to the section
        return self.externalBlock
    
    def getInternalSection(self):
        # return the section of this entry
        return self.internalBlock.getSection()
    
    def getExternalSection(self):
        # return the section connected by this entry
        return self.externalBlock.getSection()
    
    def getDirectionChange(self):
        # Does train direction change when transiting over this entry?
        # (e.g. Eastbound trains become Westbound)
        return self.directionChange
    
    def setDirectionChange(self, change):
        self.directionChange = change
        
    def addXover(self,xOver):
        self.xOver.append(xOver)

    def areXoversAvailable(self):
        # Check if all Xovers of the route (if any) are available
        internalSection = self.internalBlock.getSection()
        for xOver in self.xOver :
            if not xOver.isAvailable(internalSection) :
                return False
        return True

# BLOCK ==============
    
class ADblock (PropertyChangeListener) :
    # Encapsulates JMRI Block and LayoutBlock, adding fields and methods 
    # used by AutoDispatcher

    # STATIC VARIABLES
    
    userNames ={}       # dictionary of blocks by userName
    systemNames ={}     # dictionary of blocks by systemName
    blocksList = {}     # dictionary of blocks by layoutBlock
    # Keep a count of how many blocks have a defined length and how many 
    # do not have it
    blocksWithLength = 0
    blocksWithoutLength = 0

    # STATIC METHODS

    def getList() :
        return ADblock.systemNames.values()
    getList = ADstaticMethod(getList)

    def getByName(name) :
        if ADblock.userNames.has_key(name) :
            return ADblock.userNames[name]
        return ADblock.systemNames.get(name, None)
    getByName = ADstaticMethod(getByName)

    def getByLayoutBlock(layoutBlock) :
        return ADblock.blocksList.get(layoutBlock, None)
    getByLayoutBlock = ADstaticMethod(getByLayoutBlock)

    def setListeners() :
        # Add a property change listener for each block
        # In order to avoid race conditions, listeners are added to
        # JMRI Block.java, not to sensors!
        for block in ADblock.systemNames.values() :
            if(block.jmriBlock != None) :
                # add the listener
                block.jmriBlock.addPropertyChangeListener(block)
    setListeners = ADstaticMethod(setListeners)

    def removeListeners() :
        # Remove the listener of each block
        for block in ADblock.systemNames.values() :
            if(block.jmriBlock != None) :
                # remove the listener
                block.jmriBlock.removePropertyChangeListener(block)
    removeListeners = ADstaticMethod(removeListeners)

    # INSTANCE METHODS

    def __init__(self, section, layoutBlock, userName) :
        # Take note of section (each block must be contained in 
        # one section only!)
        self.section = section
        self.layoutBlock = layoutBlock
        self.jmriBlock = self.layoutBlock.getBlock()
        self.name = self.layoutBlock.getUserName()
        if self.name == None or self.name.strip() == "" :
            self.name = self.layoutBlock.getSystemName()
        ADblock.userNames[userName] = self
        ADblock.systemNames[layoutBlock.getSystemName()] = self
        ADblock.blocksList[layoutBlock] = self
        # Get block length and take note of how many blocks have it
        # The later info will be used in the Preferences window
        self.length = float(self.jmriBlock.getLengthMm())
        if self.length <= 0.0 :
            self.length = 0.0
            ADblock.blocksWithoutLength += 1
        else :
            ADblock.blocksWithLength += 1
        # We do not know yet if this block is an entry point
        self.entryBlock = False
        # Set maximum speeds to undefined
        self.speed = [0, 0]
        # Block actions
        self.action = ["", ""]
        # Save original block colors in order to restore them before exiting
        self.trackColor = layoutBlock.getBlockTrackColor()
        self.occupiedColor = layoutBlock.getBlockOccupiedColor()
        # Path to other blocks     
        self.paths = {}
        # Tracks contained in the Block
        self.tracks = []
        # Warn user if block has no sensor
        if self.getOccupancySensor() == None :
            AutoDispatcher.log("No sensor for block \"" + self.name +
             "\" found")
        # Swing variables. (To be moved to GUI?)
        self.stopSwing = [JRadioButton(""), JRadioButton("")]
        self.stopSwing[0].setHorizontalAlignment(JLabel.CENTER)
        self.stopSwing[1].setHorizontalAlignment(JLabel.CENTER)
        self.section.stopGroup[0].add(self.stopSwing[0])
        self.section.stopGroup[1].add(self.stopSwing[1])
        self.brakeSwing = [JRadioButton(""), JRadioButton("")]
        self.brakeSwing[0].setHorizontalAlignment(JLabel.CENTER)
        self.brakeSwing[1].setHorizontalAlignment(JLabel.CENTER)
        self.section.brakeGroup[0].add(self.brakeSwing[0])
        self.section.brakeGroup[1].add(self.brakeSwing[1])
        self.allocationSwing = [JRadioButton(""), JRadioButton("")]
        self.allocationSwing[0].setHorizontalAlignment(JLabel.CENTER)
        self.allocationSwing[1].setHorizontalAlignment(JLabel.CENTER)
        self.section.allocationGroup[0].add(self.allocationSwing[0])
        self.section.allocationGroup[1].add(self.allocationSwing[1])
        self.safeSwing = [JRadioButton(""), JRadioButton("")]
        self.safeSwing[0].setHorizontalAlignment(JLabel.CENTER)
        self.safeSwing[1].setHorizontalAlignment(JLabel.CENTER)
        self.section.safeGroup[0].add(self.safeSwing[0])
        self.section.safeGroup[1].add(self.safeSwing[1])
        self.speedSwing = [JComboBox(), JComboBox()]
        self.actionSwing = [JTextField(self.action[0], 5),
          JTextField(self.action[1], 5)]
        
    def setPaths(self) :
        # Create our paths, based on JMRI paths
        # Our paths are stored in a Jython dictionary, for fast retrieval
        # (destination block is the key)
        self.paths = {}
        jmriPaths = self.getJmriBlock().getPaths()
        for i in range(jmriPaths.size()) :
            jmriPath = jmriPaths.get(i)
            fromName = jmriPath.getBlock().getUserName()
            if fromName == None :
                fromName = jmriPath.getBlock().getSystemName()
            destinationBlock = ADblock.getByName(fromName)
            if destinationBlock != None :
                self.paths[destinationBlock] = jmriPath.getSettings()

    def addTrack(self, track) :
        # Add a track to the block
        self.tracks.append(track)
        
    def getSection(self) :
        # return the section containing this block
        return self.section
        
    def getName(self) :
        return self.name
        
    def getLength(self) :
        return self.length

    def getJmriBlock(self) :
        return self.jmriBlock

    def setValue(self, value) :
        # Set the value (e.g. train name) of the corresponding JMRI block
        self.jmriBlock.setValue(value)

    def getOccupancy(self) :
        return self.jmriBlock.getState()
        
    def getOccupancySensor(self) :
        return self.layoutBlock.getOccupancySensor()

    def getSpeed(self, direction) :
        foundSpeed = self.speed[direction]
        if foundSpeed > len(ADsettings.speedsList) :
            foundSpeed = len(ADsettings.speedsList)
        return foundSpeed
        
    def setTurnouts(self, connectingBlock, turnoutList) :
        # Set turnouts between present block and connecting block
        # Return:
        #  True if any turnout is "Thrown"
        #  False if all turnouts are "Closed" (or no turnout was found)
        thrown = False
        # Check that connection exists
        if self.paths.has_key(connectingBlock) :
            # Connection found
            beanSettings = self.paths[connectingBlock]
            # Set all turnouts contained in the path
            for i in range(beanSettings.size()) :
                beanSetting = beanSettings.get(i)
                turnout = beanSetting.getBean()
                # Make sure turnout is not included twice in paths
                # Seems to happen starting with JMRI 2.11.4
                if not turnout in turnoutList :
                    turnoutList.append(turnout)
                    position = beanSetting.getSetting()
                    # Take note if turnout must be throw
                    if position == Turnout.THROWN :
                        thrown = True
                    # Operate turnout only if not yet set in the proper position
                    # or if user disabled "Trust turnouts KnownState" indicator
                    if (not ADsettings.trustTurnouts or 
                        turnout.getState() != position) :
                        AutoDispatcher.turnoutCommands[turnout] = [
                          position, System.currentTimeMillis()]
                        turnout.setState(position)
                        # No need of repainting, since LayoutEditor will do it
                        AutoDispatcher.repaint = False
                        # Wait if user specified a delay between turnouts operation
                        if ADsettings.turnoutDelay > 0 :
                            AutoDispatcher.instance.waitMsec(
                              ADsettings.turnoutDelay)
        return thrown
                    
    def setColor(self, color):
        # Set block color
        self.layoutBlock.setBlockTrackColor(color)
        self.layoutBlock.setBlockOccupiedColor(color)

    def adjustWidth (self) :
        # Set the width of tracks, depending on block occupancy
        if not ADsettings.useCustomWidth :
            return
        occupied = self.layoutBlock.getOccupancy() == Block.OCCUPIED
        for track in self.tracks :
            track.setWidth(occupied)

    def restore(self) :
        # Restore both block colors and tracks' width (invoked before exiting)
        # Restore original block colors
        self.layoutBlock.setBlockTrackColor(self.trackColor)
        self.layoutBlock.setBlockOccupiedColor(self.occupiedColor)
        # Restore original tracks' width
        for track in self.tracks :
            track.restoreWidth()

    def propertyChange(self, event) :
        # Listener, invoked when a train enters/leaves the block
        if (event.getPropertyName() != "state" or 
           event.newValue == event.oldValue) :
            return
        ind = 0
        if event.newValue == Block.OCCUPIED :
            # Train entering the block
            train = self.section.getAllocated()
            if train == None :
                # No train expected
                self.section.setOccupied()
                # Section is not allocated - Wrong route?
                if self.entryBlock and not self.section.isManual() :
                    if ((not AutoDispatcher.stopped) and (not 
                      AutoDispatcher.paused) and 
                       ADsettings.wrongRouteDetection !=
                       ADsettings.DETECTION_DISABLED) :
                        # Check if section has crossings 
                        # (this could be a false detection due to a short on 
                        # the crossing)
                        wrongRoute = True
                        for x in self.section.xings :
                            if x.getAllocated() != None :
                                wrongRoute = False
                                break
                        if wrongRoute :      
                            if (ADsettings.wrongRouteDetection ==
                              ADsettings.DETECTION_PAUSE) :
                                AutoDispatcher.instance.stopAll()
                            AutoDispatcher.chimeLog(ADsettings.WRONG_ROUTE_SOUND,
                              "Train entering wrong route in section \""
                              + self.section.getName() + "\", block \""
                              + self.name + "\"")
            else :
                # Train expected
                train.changeBlock(self)
                if ADsettings.blockTracking :
                    self.setValue(train.getName())
        elif event.newValue == Block.UNOCCUPIED :
            # Train leaving the block
            # Check if the section is still occupied
            if ADpowerMonitor.powerOn :
                 self.section.checkOccupancy()
        # Adjust track width in accordance to occupancy status
        self.adjustWidth()

# TRACK ==============

class ADtrack :
    # Encapsulates JMRI Track class, recording the original width
    def __init__(self, track):
        self.track = track
        # Save original track width
        self.width = track.getMainline()
        
    def setWidth(self, occupied):
        # Set width, depending on block's occupancy
        self.track.setMainline(occupied)

    def restoreWidth(self):
        # Restore original track width
        self.track.setMainline(self.width)

# LOCATION ==============

class ADlocation :
    # List of sections coresponding to each Operations' location
    
    # STATIC VARIABLES
    
#    locationManager = LocationManager.instance()
    locations ={}   # dictionary of location instances

    # STATIC METHODS

    def getList() :
        return ADlocation.locations.values()
    getList = ADstaticMethod(getList)

    def getNames() :
        return ADlocation.locations.keys()
    getNames = ADstaticMethod(getNames)

    def getByName(name) :
        return ADlocation.locations.get(name, None)
    getByName = ADstaticMethod(getByName)
    
#    def getOpLocations() :
        # Get location IDs from Operations
#        opIds = ADlocation.locationManager.getLocationsByNameList()
#        for opId in opIds :
#            # get Operation's location instance
#            opLocation = ADlocation.locationManager.getLocationById(opId)
#            name = opLocation.getName()
#            # get our corresponding location or create it
#            if ADlocation.locations.has_key(name) :
#                location = ADlocation.locations[name]
#           else :
#                location = ADlocation(name)
#            # Link our location with Operation's instance
#            location.opLocation = opLocation
#    getOpLocations = ADstaticMethod(getOpLocations)

    # INSTANCE METHODS

    def __init__(self, name):
        self.name = name
        ADlocation.locations[name] = self
        self.text = ""
        self.list = []
        self.opLocation = None
        
    def setSections(self, text) :
        textSpace  = text.replace(",", " ")
        tokens = textSpace.split()
        list = []
        for t in tokens :
            s = ADsection.getByName(t)
            if s == None :
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                  "Location \"" + self.name +
                   "\": wrong format or unknown section")
                return
            list.append(s)
        self.list = list
        self.text = text

    def getSections(self) :
        return self.list

# DOUBLE CROSSOVER ==============

class ADxover :
    # Keeps information about double crossovers (for single crossovers 
    # there is no need, since they are simply two turnouts)
    def __init__(self, jmriXover):
        # Get the 4 layout blocks connected by the crossover
        block = []
        self.section = []
        block.append(jmriXover.getLayoutBlock())
        block.append(jmriXover.getLayoutBlockB())
        block.append(jmriXover.getLayoutBlockC())
        block.append(jmriXover.getLayoutBlockD())
        # Pick the corresponding ADblocks and ADsections
        for i in range(4) :
            # If block not specified, assume it to be block A
            if block[i] == None :
                block[i] = block[0]
            else :
                block[i] = ADblock.getByLayoutBlock(block[i])
            if block[i] == None :
                # If one of the blocks is not included in any section
                # the Xover can be treated as a turnout and 
                # this instance can be be purged
                return
            self.section.append(block[i].getSection())
            # If any connected section is null (i.e. section not controlled 
            # by AutoDispatcher), we can ignore the Xover (it will be treated 
            # as a simple turnout)
            if self.section[i] == None :
                # (this instance will be purged)
                return
        # Check if the crossover is the intersection of two sections
        if (self.section[0] == self.section[2] and 
           self.section[1] == self.section[3]) :
            # Intersection - is the crossover fully contained 
            # in a single section?
            if self.section[0] == self.section[1] :
                # Yes - Ignore it, since there is little we can do!
                # (this instance will be purged)
                return
            # Intersection between two different sections
            # Treat it as a simple crossing
            self.section[0].addXing(self.section[1])
            self.section[1].addXing(self.section[0])
            # (this instance will be purged)
            return
        # Sections are not intersecting         
        # Link the Xover with the relevant section entries
        self.__link__(self.section[0], block[0], self.section[2], block[2])
        self.__link__(self.section[0], block[0], self.section[3], block[3])
        self.__link__(self.section[1], block[1], self.section[2], block[2])
        self.__link__(self.section[1], block[1], self.section[3], block[3])
        self.__link__(self.section[2], block[2], self.section[0], block[0])
        self.__link__(self.section[2], block[2], self.section[1], block[1])
        self.__link__(self.section[3], block[3], self.section[0], block[0])
        self.__link__(self.section[3], block[3], self.section[1], block[1])

    def __link__(self, sectionFrom, blockFrom, sectionTo, blockTo):
        # Internal method - links the Xover to an entry point
        # Skip trivial case (both blocks in the same section)
        if sectionFrom == sectionTo :
            return
        # Find entry point
        for direction in [False, True] :
            for entry in sectionTo.getEntries(direction) :
                if (entry.getExternalSection() == sectionFrom and
                   entry.getExternalBlock() == blockFrom and
                   entry.getInternalBlock() == blockTo) :
                    entry.addXover(self)
                    return
                
    def isAvailable(self, comingFrom):
        # Checks if a route through the Xover can be used
        # Check only allocation of crossing routes
        # (other cases will anyway be solved when checking sections allocation)
        busyRoute = -1
        for i in range(2) :
            train = self.section[i].getAllocated()
            if train != None :
                if self.section[i+2].getAllocated() == train :
                    busyRoute = i
                    break
        # If no route allocated, Xover is available
        if busyRoute < 0 :
            return True
        # Allocated route found
        # Xover not available, unless route allocated to the same train
        return (self.section[busyRoute] == comingFrom or
          self.section[busyRoute+2] == comingFrom)
        

# GRID-LOCK PROTECTION GROUP ==============

class ADgridGroup :
    # Each  ADgridGroup instance contains the list of sections converging
    # into a transit-only section.  These are point where gridlock may occur.
    
    # STATIC VARIABLES
        
    list = {}

    # STATIC METHODS

    def create() :
        # Create all gridLock groups
           ADgridGroup.list = {}
           # Scan all sections
           for section in ADsection.getList() :
               # Create groups for transit-only sections that accept
               # trains in both directions
               # (one-way sections do not create gridlock situations)
            if ((section.transitOnly[0] or section.transitOnly[1])
              and section.direction == 3) :
                ADgridGroup(section)
    create = ADstaticMethod(create)

    def lockRisk(train, fromSection, toSection) :
        # Find if occupying a section can result in
        # a gridLock situation
        # Is the section included in a gridLock group
        group = ADgridGroup.list.get(fromSection.getName() + "$" +
          toSection.getName(), None)
        if group == None :
            # No
            return False
        # Yes - invoke the recursive inernal method
        ADgridGroup.debug = fromSection.getName() == "Lv1"
        return group.__lockRisk__(train, toSection, [])
    lockRisk = ADstaticMethod(lockRisk)
            
    # INSTANCE METHODS

    def __init__(self, transit) :
        # Keep note of the transit-only section, which is the
        # focus of the group
        self.transit = transit
        self.sections = []
        self.bumpers = []
        # Scan connected sections in both directions
        for direction in range(2) :
            dirSections = []
            dirBumpers = []
            for entry in transit.getEntries(not direction) :
                # Get the connected section
                section = entry.getExternalSection()
                dirSections.append(section)
                # Take note if the section is a siding
                dirBumpers.append(len(section.getEntries(direction)) == 0)
                # Create a dictionary entries for this section,
                # provided it's not one-way and gets accessed from a transit-only section
                # (focal section could not be transit-only in this direction)
                if self.transit.transitOnly[direction] and section.direction == 3 :
                    # Get the list of sections from which this section can be entered
                    for e in section.getEntries(not direction) :
                        fromSection = e.getExternalSection()
                        # Omit dictionary entries for one-way sections
                        if (fromSection.direction & (direction + 1)) != 0 :
                            # Create the entry, as follows:
                            # startSection$endSection
                            ADgridGroup.list[fromSection.getName() + "$" +
                                section.getName()] = self
            self.sections.append(dirSections)
            self.bumpers.append(dirBumpers)

    def __lockRisk__(self, train, toSection, processed) :
        # Internal recursive method
        # Make sure section was not processed yet during this call
        # to avoid an endless loop!
        if toSection in processed :
            return False
        processed.append(toSection)
        # Compute direction with respect to sections storing order
        direction = toSection in self.sections[1]
        if not direction and not toSection in self.sections[0] :
            # If section is not included in this group (shouldn't happen)
            # ignore call
            return False
        # Examine sections parallel to the requested section
        # If one of them is free or occupied by a train running in opposite
        # direction, there is no gridLock risk
        for section in self.sections[direction] :
           if (section != toSection and (section.getAllocated() == None or
              section.trainDirection != direction)) :
                return False
        ind = 0
        # Now look at sections on the other side of the transit only track
        for section in self.sections[not direction] :
            otherTrain = section.getAllocated()
            # Skip trivial case (oval with only one station)
            if train == otherTrain :
                return False
            # If section is free, or occupied by a train running in the same direction
            # of our train (and has no end-bumper), we need to make sure that we are not
            # creating a gridLock situation ahead
            if (otherTrain == None or (section.trainDirection == direction and 
              not self.bumpers[not ind])) :
                next = ADgridGroup.list.get(self.transit.getName() + "$" +
                  section.getName(), None)
                if next == None or next == self : 
                    return False
                if not next.__lockRisk__(train, section, processed) :
                    return False
            ind += 1
        return True

# TRAIN ==============

class ADtrain :
    # Our train class

    # CONSTANTS

    # Train status
    IDLE = 0
    ERROR = 1
    END_OF_SCHEDULE = 2
    STOPPED = 3
    ARRIVED = 4
    STARTING = 5
    STARTED = 6
    
    # STATIC VARIABLES
        
    trains = []       # list of trains
    # Busy indicator (used to avoid that different threads may
    # operate turnouts at once)
    turnoutsBusy = False
    # List of sections used for Swing Interface
    sectionsList = None

    # STATIC METHODS

    def getList() :
        return ADtrain.trains
    getList = ADstaticMethod(getList)
    
    def remove(train) :
        ADtrain.trains.remove(train)
    remove = ADstaticMethod(remove)

    def buildRoster(trains) :
    # Build trains roster, based on user settings
    # (retrieved from disk or from defaults for example panels)
        for t in trains :
            tt = ADtrain(t[0])
            tt.setDirection(t[2])
            section = ADsection.getByName(t[1])
            if section != None :
                tt.setSection(section, not ADsettings.autoRestart)
            tt.resistiveWheels = t[3]
            tt.setSchedule(t[4])
            tt.trainAllocation = t[5]
            tt.trainLength = t[6]
            # Set schedule info
            if tt.section != None :
                tt.schedule.stack = t[7]
                tt.schedule.pointer = tt.schedule.stack[
                  len(tt.schedule.stack)-1]
                tt.schedule.pop()
                if len(t) > 12 :
                    if t[12] != "" :
                        tt.lastRouteSection = ADsection.getByName(t[12])
            tt.changeLocomotive(t[8])
            tt.setReversed(t[9])
            # Rebuild pending commands, replacing names with instances
            for item in t[10] :
                section = ADsection.getByName(item[0])
                if section != None :
                    scheduleItem = ADscheduleItem()
                    scheduleItem.action = item[1]
                    scheduleItem.value = item[2]
                    if len(item) > 3 :
                        scheduleItem.message = item[3]
                    if (scheduleItem.action == ADschedule.WAIT_FOR or 
                       scheduleItem.action == ADschedule.MANUAL_OTHER) :
                        scheduleItem.value = ADsection.getByName(
                          scheduleItem.value)
                        if scheduleItem.value == None :
                            continue
                    elif (scheduleItem.action == ADschedule.HELD or
                       scheduleItem.action == ADschedule.RELEASE or
                       scheduleItem.action == ADschedule.IFH) :
                        scheduleItem.value = ADsignalMast.getByName(
                          scheduleItem.value)
                        if scheduleItem.value == None :
                            continue
                    tt.itemSections.append(section)
                    tt.items.append(scheduleItem)
            tt.engineerName = t[11]
            tt.trainSpeed = t[13]
            tt.brakingHistory = t[14]
            if len(t) > 15 :
                tt.canStopAtBeginning = t[15]
                if len(t) > 16 :
                    tt.startAction = t[16]
            tt.updateSwing()
    buildRoster = ADstaticMethod(buildRoster)

    # INSTANCE METHODS

    def __init__(self, trainName) :
        # record this instance in the list of trains
        ADtrain.trains.append(self)
        # Initailize instance variables
        # Train name
        self.name = trainName
        # Corresponding train of Operations module (if any)
        self.opTrain = None
        # Locomotive name
        self.locoName = ""
        # Locomotive instance
        self.locomotive = None
        # Indicator of locomotive running backwards
        self.reversed = False
        # Indicator that train stops at start of sections that provide this option
        self.canStopAtBeginning = False
        # Indicator that train is presently running
        self.running = False
        # Train status
        self.status = ADtrain.IDLE
        # Present train speed level
        self.speedLevel = 0
        # Speed conversion table 
        self.trainSpeed = []
        # Train departure time (-1L = immediate)
        self.departureTime = -1L
        # Departure time based on fast clock
        self.fastClock = False
        # Indicator that train is waiting for a section to become free
        # (None = no wait)
        self.waitFor = None
        # Indicator that train is operating in switching mode and can thus
        # enter any section
        self.switching = False
        # Protection flag. Suspends train processing while input data 
        # are being updated
        self.updating = False
        # Protection flag. Suspends train processing while another thread 
        # is already taking care of it
        self.checking = False
        # Direction of the train (referred to the internal conventional
        # direction)
        self.direction = 0
        # Section where the head of the train is located
        self.section = None
        # Block where the head of the train is located
        self.block = None
        # Current destination section along current route
        self.destination = None
        # Direction at destination
        self.finalDirection = 0
        # Exit signal of destination section
        self.destinationSignal = None
        # Last section of the route (can be different from present destination)
        self.lastRouteSection = None
        # Sections of current route to be still encountered
        self.sectionsAhead = []
        # Blocks of current route to be still encountered
        self.blocksAhead = []
        # Other sections still occupied by the train
        self.previousSections = []
        # Sections ahead where commands need to be executed
        self.itemSections = []
        # Commands to be executed (there is an item for each itemSection)
        self.items = []
        # Train schedule
        self.schedule = ADschedule(" ")
        # Indicator that train is fully recovered in the current section
        self.safe = True
        # Indicator that train already reached the allocation point
        self.allocationReady = True
        # Number of sections along the route allocated
        self.allocatedSections = 0
        # Indicator that cars are equipped with resistive wheels
        self.resistiveWheels = ADsettings.resistiveDefault
        # Number of sections ahead to be allocated for the train (0 = default)
        self.trainAllocation = 0
        # Train length in mm.
        self.trainLength = 0.0
        # Train length when leaving present section, provided by Operations module
        # (if used)
        self.opLength = 0.0
        # Train distance from first section contained in previousSections
        # (used to release previous sections)
        self.distance = 0.
        # Length of current block
        self.blockLength = 0.
        # Last time train entered a block (used to detect stalled trains)
        self.lastMove = -1L
        # Maximum allowed train speed (may vary from block to block)
        self.maxSpeed = 0
        # Actions to be played before train departure
        self.startAction = ""
        # Our engineer name
        self.engineerName = "Auto"
        # Our engineer class
        self.engineer = None
        # Available methods of the engineer
        # (Engineer classes may implement only some methods)
        self.engineerAssigned = False
        self.engineerSetTrain = None
        self.engineerSetLocomotive = None
        self.engineerSetLocoName = None
        self.engineerSetOrientation = None
        self.engineerSetFunction = None
        self.engineerChangeSpeed = None
        self.engineerPause = None
        self.engineerResume = None
        self.engineerRelease = None
        # Fields used for simulation mode
        # Current train route
        self.entriesAhead = []
        # Sensor of previous block
        self.simSensor = None
        # Fields for self-learning braking
        # Previous block
        self.previousBlock = None
        # Collected braking data
        self.brakingHistory = {}
        # Swing fields, used in Trains window
        self.directionSwing = JComboBox([ADsettings.directionNames[0],
          ADsettings.directionNames[1]])
        # Initialize sections' list if not yet done
        # The list will be used to populate combo boxes
        if ADtrain.sectionsList == None :
            ADtrain.sectionsList = [""]
            for section in ADsection.getList() :
                ADtrain.sectionsList.append(section.getName())
            ADtrain.sectionsList.sort()
        self.sectionSwing = JComboBox(ADtrain.sectionsList)
        self.nameSwing = JTextField(self.name, 4)
        self.locoRoster = JComboBox()
        self.reversedSwing = JCheckBox("", self.reversed)
        self.reversedSwing.setHorizontalAlignment(JLabel.CENTER)
        self.scheduleSwing = JTextField("", 8)
        self.speedLevelSwing = AutoDispatcher.centerLabel("Idle")
        self.resistiveSwing = JCheckBox("", self.resistiveWheels)
        self.canStopAtBeginningSwing = JCheckBox("", self.canStopAtBeginning)        
        self.trainLengthSwing = JTextField("0", 4)
        self.trainAllocationSwing = JComboBox(["Default", "1", "2", "3", "4", "5"])
        self.trainAllocationSwing.setSelectedIndex(self.trainAllocation)
        self.engineerSwing = JComboBox()
        self.deleteButton = JButton("Delete")
        self.deleteButton.actionPerformed = self.whenDeleteTrainClicked
        self.changeButton = JButton("Apply")
        self.changeButton.actionPerformed = self.whenChangeClicked
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenSetClicked
        self.detailButton = JButton("Detail")
        self.destinationSwing = AutoDispatcher.centerLabel("None")
        self.startActionSwing = JTextField("", 5)
        self.enableSwing()

    def getName(self) :
        return self.name
        
    def getCanStopAtBeginning(self) :
        return self.canStopAtBeginning
        
    def getLength(self) :
        return self.trainLength

    def whenDeleteTrainClicked(self,event) :
        # Ask confirmation before deleting the train!
        if (JOptionPane.showConfirmDialog(None, "Remove train \""
          + self.name + "\"?", "Confirmation",
          JOptionPane.YES_NO_OPTION) == 1) :
            return
        self.updating = True
        if self.running :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Train " + self.name + " running: cannot be deleted")
            self.updating = False
            return
        if self.opTrain != None and self.status == ADtrain.END_OF_SCHEDULE :
            try:
                self.opTrain.move()
            finally :
                i = 0 # Useless, just to complete the try construct
        AutoDispatcher.trainsFrame.deleteTrain(self)
        self.updating = True
              
    # define what Apply button in Trains Window does when clicked
    def whenChangeClicked(self,event) :
        self.trainChange()

    # define what Set button in Train Detail Window does when clicked
    def whenSetClicked(self,event) :
        self.scheduleSwing.text = AutoDispatcher.trainDetailFrame.scheduleSwing.text
        self.trainChange()
        
    def trainChange(self) :
        # Get input values from "Trains" and/or "Train Detail" windows
        # Check if this train is shown in the "Train Detail" window
        detail = (AutoDispatcher.trainDetailFrame != None and 
          AutoDispatcher.trainDetailFrame.train == self)
        # Take note that instance is being updated, preventing other 
        # threads from using it
        self.updating = True
        # Make sure another thread is not starting the train
        # (Let's cope with Jython's lack of synchronization)
        if self.running :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Train "
              + self.name + " running: changes cannot be applied")
            self.updating = False
            return
        # Change train's name
        newName = self.nameSwing.text
        if newName.strip() == "" :
            self.nameSwing.setText(oldName)
        elif newName != self.name :
            self.name = newName
            AutoDispatcher.setTrainsDirty()
            # Update train name in jmri Blocks (if needed)
            if self.section != None :
                if not self.section in self.previousSections :
                    self.section.changeTrainName()
                for section in self.previousSections :
                    section.changeTrainName()
        # Change train's direction
        oldDirection = self.direction
        self.setDirection(self.directionSwing.getSelectedItem())
        # If direction changed, take note that we need to restart schedule
        reSchedule = oldDirection != self.direction
        # Change train's section
        sectionName = self.sectionSwing.getSelectedItem()
        if sectionName.strip() == "" :
            newSection = None
        else :
            newSection = ADsection.getByName(sectionName)
        if newSection != self.section :
            # Train was manually moved to another section
            # Take note that we need to restart schedule
            reSchedule = True
            self.setSection(newSection, True)
            self.schedule = None
        # Change train's locomotive
        loco = self.locoRoster.getSelectedItem()
        if loco != "" and loco != self.locoName :
            self.changeLocomotive(loco)
            AutoDispatcher.setTrainsDirty()
        # Change locomotive orientation
        newReversed = self.reversedSwing.isSelected()
        if self.reversed != newReversed :
            self.setReversed(newReversed)
            AutoDispatcher.setTrainsDirty()
        # Change train's schedule
        newSchedule = self.scheduleSwing.text
        if (self.schedule == None or self.schedule.pointer >= len(self.schedule.source)
          or self.schedule.text != newSchedule or reSchedule) :
            self.setSchedule(newSchedule)
            AutoDispatcher.setTrainsDirty()
        if detail :
            AutoDispatcher.trainDetailFrame.scheduleSwing.text = (
              self.scheduleSwing.text)
            # Change train's resistive wheels indicator
            newResistiveWheels = self.resistiveSwing.isSelected()
            if self.resistiveWheels != newResistiveWheels :
                self.resistiveWheels = newResistiveWheels
                AutoDispatcher.setTrainsDirty()                
            canStopAtBeginningSwing = self.canStopAtBeginningSwing.isSelected()
            if self.canStopAtBeginning != canStopAtBeginningSwing :
                self.canStopAtBeginning = canStopAtBeginningSwing
                AutoDispatcher.setTrainsDirty()
            # Change train length
            oldLength = self.trainLength
            try :
                self.trainLength = (float(self.trainLengthSwing.text) *
                  ADsettings.units)
            except :
                self.trainLength = 0
                self.trainLengthSwing.text = "0"
            if oldLength != self.trainLength :
                AutoDispatcher.setTrainsDirty()
            # Change number of sections ahead to be allocated for this train
            newAllocation = self.trainAllocationSwing.getSelectedIndex()
            if newAllocation != self.trainAllocation :
                self.trainAllocation = newAllocation
                AutoDispatcher.setTrainsDirty()
            # Change engineer
            newEngineerName = self.engineerSwing.getSelectedItem()
            if newEngineerName != self.engineerName :
                self.setEngineer(newEngineerName)
                AutoDispatcher.setTrainsDirty()
            # Change train speeds table
            ind = 0
            for s in AutoDispatcher.trainDetailFrame.trainSpeedSwing :
                newSpeed = s.getSelectedIndex()+1
                if newSpeed != self.trainSpeed[ind] :
                    self.trainSpeed[ind] = newSpeed
                    AutoDispatcher.setTrainsDirty()
                ind += 1
            if self.startAction != self.startActionSwing.text :
                self.startAction = self.startActionSwing.text
                AutoDispatcher.setTrainsDirty()
        # Update swing fields
        self.updateSwing()
        if AutoDispatcher.trainsFrame != None :
            AutoDispatcher.trainsFrame.reDisplay()
        # Updating complete, let other threads use this instance
        self.updating = False
        if detail :
            AutoDispatcher.trainDetailFrame.dispose()
            AutoDispatcher.trainDetailFrame = None
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Changes for train \""
          + self.name + "\" applied")

    def setDirection(self, direction) :
        # Set train direction
        if (direction == "CCW" or direction == "EAST" or direction == "NORTH"
           or direction == "LEFT" or direction == "UP") :
            newDirection = ADsettings.ccw
        else :
            newDirection = 1 - ADsettings.ccw
        if newDirection == self.direction :
            return
        self.direction = newDirection
        if self.section != None :
            self.section.trainDirection = self.direction
    def getDirection(self) :
        return self.direction

    def setSection(self, newSection, held) :
        # Set initial position of train
        if newSection != self.section :
            # Train placed in a new section
            if self.section != None :
                # Remove train from the old sections
                self.section.trainHead = False
                self.releaseSections()
#                self.section.allocate(None, 0)
                self.section = None
            self.safe = False
            self.simSensor = None
            if newSection != None :
                # Check that section is not allocated to another train
                otherTrain = newSection.getAllocated()
                if otherTrain != None and otherTrain != self :
                    AutoDispatcher.message("Section " + newSection.getName() +
                      " already allocated to train " + otherTrain.getName())
                    newSection = None
                else :
                    # Check that section is actually occupied
                    newSection.occupied = True
                    newSection.checkOccupancy()
                    if not newSection.isOccupied() :
                        # Train is being placed in an empty section
                        # Are we running in simulation mode?
                        if AutoDispatcher.simulation :
                            # Simulation mode - Force section occupancy
                            # Find out block to be used
                            sensor = newSection.stopBlock[
                              self.direction].getOccupancySensor()
                            if sensor != None :
                                # Avoid "wrong route" error
                                wrongRoute = ADsettings.wrongRouteDetection
                                ADsettings.wrongRouteDetection = (
                                  ADsettings.DETECTION_DISABLED)
                                newSection.occupied = True
                                newSection.empty = False
                                # Force occupancy
                                sensor.setKnownState(Sensor.ACTIVE)
                                ADsettings.wrongRouteDetection = wrongRoute
                            else :
                                # Block has no sensor! :-O
                                newSection = None
                        else :
                            # We are not running in simulation mode.
                            # User should place train on tracks before 
                            # defining it (unless derailment detection is disabled)!
                            if (ADsettings.derailDetection != ADsettings.DETECTION_DISABLED) :
                                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Section " + newSection.getName() +
                                     " is empty. Place train "
                                    + self.name + " on tracks!")
                                # Attempt to assign train to an empty section, 
                                if (ADsettings.derailDetection == ADsettings.DETECTION_PAUSE) :
                                      # set section to undefined
                                    newSection = None
                # Was assignment successful?
                if newSection != None :
                    self.section = newSection
                    # Yes - Update allocation
                    self.section.allocate(self, self.direction)
                    self.destination = self.lastRouteSection = self.section
                    self.finalDirection = self.direction
                    self.destinationSignal = self.section.getSignal(
                      self.direction)
                    self.block = self.section.stopBlock[self.direction]
                    self.previousBlock = None
                    # Clear distance from previous sections
                    self.distance = 0.
                    self.blocklength = 0.
                    # Set signal in front of train to held
                    # (only if the signal icon is displayed on the panel,
                    # otherwise user will not be able to release it!
                    s = self.section.getSignal(self.direction)
                    if held and s.hasIcon() :
                        s.setHeld(True)
                else :
                    # Unsuccessful assignment - clear section name
                    self.sectionSwing.setSelectedItem("")

    def releaseSections(self) :
        # Routine to release sections occupied by the train,
        # when manually moving it to another section or deleting it
        for section in ADsection.getList() :
            if section.getAllocated() == self :
                section.allocate(None, 0)
                if AutoDispatcher.simulation :
                    section.occupied = False
                    section.empty = True
                    for block in section.getBlocks(True) :
                        sensor = block.getOccupancySensor()
                        if sensor != None :
                            sensor.setKnownState(Sensor.INACTIVE)
                            block.adjustWidth()
                else :
                    section.checkOccupancy()
                section.setColor()
                AutoDispatcher.repaint = True

    def changeSection(self, newSection) :
        # Change section containing train's head while train is running
        # Knowing where the head of the train is located is
        # necessary for proper action of signals and 
        # to detect derailments (or other malfunctions)
        # If a section containing the head of the train becomes empty,
        # something went wrong!
        # Avoid re-triggering a previous section
        if newSection in self.previousSections or newSection == self.section :
            return
        # Train moved to a new section
        if self.section != None :
            # Remove the head of the train from the old section (if any)
            self.section.trainHead = False
            # Reset signals
            if self.section.signal[0] != None :
                self.section.signal[0].setIndication(0)
            if self.section.signal[1] != None :
                self.section.signal[1].setIndication(0)
        # Since train is just entering a new section it cannot be safely
        # recovered in it
        self.safe = False
        self.section = newSection
        if self.section != None :
            # Must previous sections be still released?
            if len(self.previousSections) == 0 :
                # No, clear distance from previous sections
                 self.distance = 0.
                 self.blocklength = 0.
            # Is train moving to a section along the route? (it should)
            if self.section in self.sectionsAhead :
                # Yes
                # Remove it and all previous sections from the route,
                # adjusting train direction (if changed)
                oldDirection = self.direction
                while self.section in self.sectionsAhead :
                    # Adjust direction
                    passedSection = self.sectionsAhead.pop(0)
                    self.direction = passedSection.trainDirection
                    # Mark section as occupied
                    passedSection.setOccupied()
                    passedSection.checkOccupancy()
                    # Update number of allocated section
                    if (self.allocatedSections > 0 and 
                       (not passedSection.transitOnly[self.direction] or
                       passedSection.getSignal(self.direction).hasHead())) :
                        self.allocatedSections -= 1
                    # Keep note that this section was passed and must still 
                    # be released
                    self.previousSections.append(passedSection)
                self.section.trainHead = True
                # Update Trains window if direction changed
                if self.direction != oldDirection :
                    self.updateSwing()
                    # Update sections color if direction changed
                    for section in self.previousSections :
                        section.setColor()
                    for section in self.sectionsAhead :
                        section.setColor()
                # Update Trains window (if open)
                self.sectionSwing.setSelectedItem(self.section.getName())
                # Move ahead train position in Operations module (if any)
                if self.opTrain != None :
                    if self.opLength > 0. :
                        self.trainLength = self.opLength
                        self.opLength = 0.
                    try :
                        nextLocation = ADlocation.getByName(
                          self.opTrain.getNextLocationName())
                        if nextLocation != None :
                            if self.section in nextLocation.getSections() :
                                self.opTrain.move()
                                opCurrent = self.opTrain.getCurrentLocation()
                                if opCurrent != None :
                                    self.opLength = round(float(
                                    opCurrent.getTrainLength(
                                    )) * 304.8 / ADsettings.scale)
                    finally :
                        i = 0
            else :
                # Train ended nowhere :-O
                self.sectionSwing.setSelectedItem("")
            # Should train stop at the start of this section?
            if self.shouldStopAtBeginning() :
                if self.locomotive.learningBrake() :
                    if self.speedLevel > self.maxSpeed :
                        self.changeSpeed(self.maxSpeed)
                else :
                    # Yes, brake
                    self.changeSpeed(1)

    def shouldStopAtBeginning(self) :
        if not self.canStopAtBeginning :
            return False
        if self.section != self.destination :
            return False
        if self.section.stopAtBeginning[self.direction] < 0 :
            return False
        return True

    def changeBlock(self, newBlock) :
        # Check if we need to compute distance from previous sections
        if len(self.previousSections) == 0 :
            firstPassedSection = self.section
        else :
            firstPassedSection = self.previousSections[0]
        speed = self.speedLevel
        # Remove block from list of expected blocks in order to
        # avoid re-triggering. Also blocks that were skipped
        # i.e. did not trigger any event (malfunctioning sensor?)
        # are processed
        while newBlock in self.blocksAhead :
            self.previousBlock = self.block
            self.block = self.blocksAhead.pop(0)
            self.lastMove = System.currentTimeMillis()
            section = self.block.getSection()
            # Update maximum speed
            newSpeed = self.block.getSpeed(self.direction)
            if newSpeed > 0 :
                self.maxSpeed = newSpeed
                # Apply new speed if train is not braking or stopping
                if speed > 1 :
                    speed = self.maxSpeed
            self.changeSection(section)
            # Update distance from previous sections (unless we are still 
            # in the first section of the route)
            if firstPassedSection != section :
                self.distance += self.blockLength
                self.blockLength = self.block.getLength()
                section.sectionLength += self.blockLength
            # Update locomotive mileage
            if self.locomotive != None :
                # We actually do it in advance, but for our purposes it's fine
                self.locomotive.mileage += self.block.getLength()
            # allocation point?
            if section.allocationPoint[self.direction] == self.block :
                self.allocationReady = True
            if section == self.section :
                # safe point?
                if self.section.safePoint[self.direction] == self.block :
                    # Take note that the train is safely contained within
                    # section boundaries
                    self.safe = True
                # Is train still starting ?
                if self.status != ADtrain.STARTING :
                    # No, get speed indicated by signal
                    restrictedSpeed = self.section.getSignal(
                      self.direction).getSpeed()
                    # Should train stop at start of section ?
                    shouldStop = self.shouldStopAtBeginning()
                    # stop block?
                    if self.section.stopBlock[self.direction] == self.block :
                        # Apply restricted speed
#                        if restrictedSpeed > 0 :
                        speed = restrictedSpeed
                        if speed == 0 :
                            if not shouldStop :
                                self.arrived()
                            # If we have a locomotive, inform it that train
                            # reached stop block (in case it's implementing
                            # self-learning braking)
                            if self.locomotive != None :
                                self.locomotive.learningStop()
                            # If we don't have a locomotive or are running in
                            # simulation, assume that train stops
                            if (self.locomotive == None 
                              or self.engineerSetLocomotive == None
                              or AutoDispatcher.simulation) :
                                self.stop()
                    elif shouldStop :
                        speed = self.speedLevel
                    # Brake block?
                    elif self.section.brakeBlock[self.direction] == self.block :
                        # Set speed to minimum if exit signal is red
                        if restrictedSpeed == 0 :
                            # If we have a locomotive, inform it that train
                            # starts braking (in case it's implementing
                            # self-learning braking)
                            if self.locomotive != None :
                                if not self.locomotive.learningBrake() :
                                    # self-learning not active
                                    # Brake immediately
                                    speed = 1
                            else :
                                # Self-learning braking not implemented
                                # Brake immediately
                                speed = 1
                        # If signal is not red, set speed to value indicated 
                        # by signal (if lower than present speed)
                        else :
                            speed = restrictedSpeed
            # Almost done.
            # Call change speed even if speed did not change, in order to inform
            # the Engineer that some change occurred
            self.changeSpeed(speed)
            # Start a separate thread to take care of block actions (if any)
            if self.block.action[self.direction].strip() != "" :
                start_new_thread(self.doAction,
                  (self.block.action[self.direction],))
        # Release previously occupied sections (if empty)
        self.section.freeSectionIfTrainSafe()  
        
    def arrived(self) :
        # Assume train is stopping
        self.lastMove = -1L
        if AutoDispatcher.runningTrains > 0 :
            AutoDispatcher.runningTrains -= 1
        if self.section.isManual() :
            self.section.setColor()

    def stop(self) :
        # Train stops
        self.running = False
        self.enableSwing()

    def doAction(self, actionList) :
        # Run in a separate thread to perform block actions
        textSpace  = actionList.replace(",", " ")
        # Break down input text into tokens
        splitted = textSpace.split()
        for sl in splitted :
            if sl.startswith("$") and len(sl) > 1 :
                sl = sl[1:]
            action = ADschedule.ERROR
            s = sl.upper()
            # DCC function ON/OFF
            if s.startswith("ON:F") :
                action = ADschedule.SET_F_ON
                value = s[4:]
            elif s.startswith("OFF:F") :
                action = ADschedule.SET_F_OFF
                value = s[5:]
            if action != ADschedule.ERROR :
                # Retrieve ON OFF argument (function number)
                try :
                    value = int(value)
                    if value < 0 or value > 28 :
                        continue
                except :
                    continue
                if action == ADschedule.SET_F_ON :
                    # Set decoder function ON
                    self.setFunction(value, True)
                else :
                    # Set decoder function OFF
                    self.setFunction(value, False)
            # Delay n seconds or m fastclock minutes
            elif s.startswith("D") :
                try :
                    s = s[1:]
                except :
                    continue
                if s.startswith("M") :
                    try :
                        s = s[1:]
                    except :
                        continue
                    useFastClock = True        
                else :
                    useFastClock = False        
                try :
                    value = float(s)
                except :
                    continue
                if useFastClock :
                    value = (value * 60.
                  / AutoDispatcher.fastBase.getRate())
                sleep(value)
            # Play AudioClip
            elif s.startswith("S:") :
                try :
                    value = ADsettings.soundDic.get(sl[2:], None)
                    if value != None :
                        value.play()
                except :
                    continue
            # Set turnout
            elif s.startswith("TC:") or s.startswith("TT:") :
                try :
                    value = sl[3:]
                    t = InstanceManager.turnoutManagerInstance().getTurnout(value)
                    if s.startswith("TC:") :
                        t.setState(Turnout.CLOSED)
                        AutoDispatcher.message("Closed turnout " + value)
                    else :
                        t.setState(Turnout.THROWN)
                        AutoDispatcher.message("Thrown turnout " + value)                   
                except :
                    continue

    def setSchedule(self, schedule) :
        # Set train schedule
        self.schedule = ADschedule(schedule)
        self.status = ADtrain.IDLE
        self.speedLevelSwing.text == "Idle"
        self.waitFor = None
        self.departureTime = -1L
        self.destination = lastRouteSection = self.section
        self.finalDirection = self.direction
        self.sectionsAhead = []
        self.entriesAhead = []
        self.blocksAhead = []
        self.itemSections = []
        self.items = []
        # Update sections' colors and status (releasing possible sections
        # allocated to the train)
        for section in ADsection.getList() :
            if section.getAllocated() == self and not section.isOccupied() :
                section.allocate(None, 0)
            else :
                section.setColor()
        if self.section != None :
            # Set destination signal to exit signal of current section
            self.destinationSignal = self.section.signal[self.direction]
            # Try to find current section in new schedule
            self.schedule.match(self.section, self.direction)
            self.queueCommands()
        # Force panel repainting
        AutoDispatcher.repaint = True

    def changeLocomotive(self, locoName) :
        # Set/replace locomotive
        # If same locomotive as before ignore call
        if self.locoName == locoName :
            return
        self.locoName = locoName
        # Release previous locomotive
        locomotive = self.locomotive
        if locomotive != None :
            self.locomotive = None
            locomotive.usedBy = None
            locomotive.releaseThrottle()
        # If no new locomotive was selected, return
        if self.locoName.strip() == "" :
            return
        # Retrieve locomotive instance
        self.locomotive = ADlocomotive.getByName(self.locoName)
        if self.locomotive == None :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Locomotive " + locoName + " not found!")
        else :
            # Locomotive found
            self.locomotive.usedBy = self
            # Force assignment of engineer
            self.engineerAssigned = False
        self.status = ADtrain.IDLE
        
    def setReversed(self, reversed) :
        # Set locomotive orientation
        self.reversed = reversed

    def clearBrakingHistory(self, locomotive) :
        if locomotive == None :
            # No locomotive specified. Clear history of all locomotives
            self.brakingHistory = {}
        else :
            # Rebuild history dictionary skipping entries 
            # for specified locomotive
            keyStart = locomotive.getName() + "$"
            newHistory = {}
            for key in self.brakingHistory.keys() :
                if not key.startswith(keyStart) :
                    newHistory[key] = self.brakingHistory[key]
            self.brakingHistory = newHistory

    def setEngineer(self, engineerName) :
        self.engineerName = engineerName
        self.engineerAssigned = False
        
    def assignEngineer(self) :
        # If engineer already assigned, ignore call
        if self.engineerAssigned :
            return
        # Release previous Engineer, if any
        self.releaseEngineer()
        self.engineerAssigned = True
        # If user chose manual control, no other action is needed
        if self.engineerName == "Manual" :
            return
        # Retrieve engineer class
        if not AutoDispatcher.engineers.has_key(self.engineerName) :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Engineer script " + self.engineerName + " not found!")
            self.engineerName = "Auto"
        # Create engineer instance
        self.engineer = AutoDispatcher.engineers[self.engineerName]()
        # Check what methods are implemented by this engineer class
        self.engineerSetTrain  = self.getEngineerMethod("setTrain")
        self.engineerSetLocomotive = self.getEngineerMethod("setLocomotive")
        self.engineerSetLocoName = self.getEngineerMethod("engineerSetLocoName")
        self.engineerSetOrientation = self.getEngineerMethod("setOrientation")
        self.engineerSetFunction = self.getEngineerMethod("setFunction")
        self.engineerChangeSpeed = self.getEngineerMethod("changeSpeed")
        self.engineerPause = self.getEngineerMethod("pause")
        self.engineerResume = self.getEngineerMethod("resume")
        self.engineerRelease = self.getEngineerMethod("release")
        # Inform engineer that he is assigned to this train :-)
        self.callEngineer(self.engineerSetTrain, self)
        
    def getEngineerMethod (self, methodName) :
        # Check if a method exists
        method = getattr(self.engineer, methodName, None)
        if not callable(method) :
            return None
        return method            

    def setOrientation(self, forward) :
        # Set locomotive orientation
        # Does the engineer provide the relevant method?
        if self.engineerSetOrientation == None :
            # No, if we have a locomotive pass the call directly 
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None :
                locomotive.setOrientation(forward)
        else :
            # Engineer provides the appropriate method, invoke it
            self.callEngineer(self.engineerSetOrientation, forward)
                    
    def setFunction(self, functionNumber, on) :
        # Set/reset a locomotive function
        # Does the engineer provide the relevant method?
        if self.engineerSetFunction == None :
            # No, if we have a locomotive pass the call directly 
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None :
                locomotive.setFunction(functionNumber, on)
        else :
            # Engineer provides the appropriate method, invoke it
            self.callEngineer(self.engineerSetFunction, functionNumber, on)

    def changeSpeed(self, suggestedSpeed) :
        # Change train speed
        # Make sure that speed does not exceed block's maximum speed
        if suggestedSpeed > self.maxSpeed  and self.maxSpeed != 0 :
            suggestedSpeed = self.maxSpeed
        # If speed changed, update Trains window (if open)
        if self.speedLevel != suggestedSpeed :
            self.outputSpeed(suggestedSpeed)
            self.speedLevel = suggestedSpeed
        # Convert speed level, using train's correspendence table
        suggestedSpeed = self.convertSpeed(suggestedSpeed)
        # Does the engineer provide the relevant method?
        if self.engineerChangeSpeed == None :
            # No, if we have a locomotive pass the call directly
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None :
                locomotive.changeSpeed(suggestedSpeed)
        else :
            # Engineer provides the appropriate method, invoke it
            # Engineer is called even if speed value did not change, in order to
            # inform about other changes (e.g. section or block)
            # The Engineer class will decide if any action is required
            max = self.maxSpeed
            if max == 0 :
                max = len(ADsettings.speedsList)
            max = self.convertSpeed(max)
            self.callEngineer(self.engineerChangeSpeed, self.section,
              self.block, self.section.getSignal(self.direction),
              suggestedSpeed, max)
        # If user chose to switch off lights when train stops, do it
        if self.speedLevel == 0 and ADsettings.lightMode == 1 :
            self.setFunction(0, False)
            
    def outputSpeed(self, speed) :
        # Output speed level to Trains window
        if speed == 0 :        
            if self.departureTime > -1L or self.waitFor != None :
                return
            self.speedLevelSwing.text = "Stop"
        else :
            self.speedLevelSwing.text = ADsettings.speedsList[speed-1]

    def convertSpeed(self, speed) :
        # Convert speed level, using train's correspondence table
        # (User can decide that this train should run at 10mph when 
        # the prescribed speed is 20mph)
        if speed > 0 and len(self.trainSpeed) >= speed :
            speed =  self.trainSpeed[speed-1]
        if speed > len(ADsettings.speedsList) :
            speed = len(ADsettings.speedsList)
        return speed

    def pause(self) :
        # Script is pausing, train must be halted
        # Does the engineer provide the relevant method?
        if self.engineerPause == None :
            # No, if we have a locomotive pass the call directly
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None :
                locomotive.pause()
        else :
            # Engineer provides the appropriate method, invoke it
            self.callEngineer(self.engineerPause)
        # Take note that train stopped, to avoid "Stalled train" error
        self.lastMove = -1L

    def resume(self) :
        # Script is resuming after a pause, train must be restarted
        # Set correct locomotive direction, in case it was manually changed
        # during the pause
        self.setOrientation(not self.reversed)
        # Does the engineer provide the relevant method?
        if self.engineerResume == None :
            # No, if we have a locomotive pass the call directly
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None :
                locomotive.resume()
        else :
            # Engineer provides the appropriate method, invoke it
            self.callEngineer(self.engineerResume)
        if self.running :
            self.lastMove = System.currentTimeMillis()

    def releaseEngineer(self) :
        # Release the present engineer
        # Does the engineer provide a release method?
        if self.engineer != None and self.engineerRelease != None :
            # Yes, invoke it
            self.callEngineer(self.engineerRelease)
        # Clear all engineer related variables
        self.engineer = None
        self.engineerSetTrain = None
        self.engineerSetLocomotive = None
        self.engineerSetLocoName = None
        self.engineerSetOrientation = None
        self.engineerSetFunction = None
        self.engineerChangeSpeed = None
        self.engineerPause = None
        self.engineerResume = None
        self.engineerRelease = None
        self.engineerAssigned = False

    def callEngineer(self, method, *args) :
        # Invoke a method of the engineer class
        # Number of arguments may vary
        if method != None :
            start_new_thread(method, args)

    def startIfReady(self) :
        # Start the train, if it can reach a section towards its destination
        # If train reached the end of the schedule or is in error simply exit
        # Avoid any action if train is being updated or started by
        # another thread
        if (self.updating or self.checking or self.status == ADtrain.ERROR or
           self.status == ADtrain.END_OF_SCHEDULE) :
            return
        # If the train is nowhere, exit!
        if self.section == None :
            return
        loco = self.locomotive
        # Is train in a manually controlled section?
        if self.section.isManual() :
            # Manual section, release throttle if train stopped
            if (not self.running and 
               self.locomotive != None and self.locomotive.throttle != None and
               self.locomotive.getThrottleSpeed() <= 0) :
                self.locomotive.releaseThrottle()
        else:
            # Not manual section - Assign engineer, if not yet done
            if not self.engineerAssigned :
                self.assignEngineer()
                if loco != None :
                    if self.engineerSetLocomotive != None :
                        self.callEngineer(self.engineerSetLocomotive, loco)
                    else :
                        self.callEngineer(self.engineerSetLocoName, loco.getName())
            # Does train have a locomotive?
            if loco != None :
                # Assign throttle, if not yet done
                # (only if engineer implements setLocomotive method, otherwise
                # having a locomotive is useless)
                if (not loco.throttleAssigned and
                   self.engineerSetLocomotive != None) :
                    loco.assignThrottle()
                    return
        # If train is paused, check if time has expired
        if self.departureTime > -1L :
            if self.fastClock :
                if self.departureTime > FastListener.fastTime :
                    return
            elif self.departureTime > System.currentTimeMillis() :
                return
            self.departureTime = -1L
            self.outputSpeed(self.speedLevel)
        # If train is waiting for a section, see if the section is available
        # (or already occupied by the train)
        if self.waitFor != None :
            if (self.waitFor.isAvailable() or (self.waitFor == self.section and
               not self.section.isManual())) :
                self.waitFor = None
                if self.speedLevelSwing.text.startswith("WAITING") :
                    self.outputSpeed(self.speedLevel)
            else :
                return
        # Process commands prefixed by $ that can be executed without
        # stopping the train
        # Such commands were transferred to "items" array before train departure
        if self.section in self.itemSections :
            self.itemSections.pop(0)
            self.processCommands(self.items.pop(0))
            return
        # If running, see if destination was reached
        if ((self.status == ADtrain.STARTED or self.status == ADtrain.IDLE) and
           self.section == self.destination) :
            self.status = ADtrain.ARRIVED
        # Process commands prefixed by $ that can be executed only when
        # train is not running
        # (such commands are still contained in the schedule)
        scheduleItem = self.schedule.getFirstAlternative()
        if not self.running and self.status == ADtrain.ARRIVED :
            if scheduleItem.action != ADschedule.GOTO :
                # Take care of possible $IF commands
                scheduleItem = self.schedule.testCondition(scheduleItem,
                  self.destination, self.direction)
                # Now execute this command
                self.processCommands(scheduleItem)
                # and pick up the next one
                self.schedule.next()
                return
            self.status = ADtrain.STOPPED
        # Whether running or not, GOTO command can be implemented without
        # waiting for train to stop
        if (scheduleItem.action != ADschedule.GOTO or
           self.destination.isManual()) :
            return
        # If signal held, exit
        if self.destinationSignal.isHeld() :
            if not self.running and self.destinationSwing.text != "Held":
                self.destinationSwing.text = "Held"
            return
        # Is train eligible for starting?
        if self.trainAllocation == 0 :
            allocationAhead = ADsettings.allocationAhead
        else :
            allocationAhead = self.trainAllocation
        if (not self.allocationReady  or 
           self.allocatedSections >=  allocationAhead) :
            return
        # Limit number of running trains
        if (not self.running and (ADsettings.max_trains > 0 and
           AutoDispatcher.runningTrains >= ADsettings.max_trains)) :
            if (not self.running and
               self.destinationSwing.text != "Waiting turn"):
                self.destinationSwing.text = "Waiting turn"
            return
        # Make sure anther thread is not starting this train or 
        # operating turnouts
        if ADtrain.turnoutsBusy or self.checking :
            return
        ADtrain.turnoutsBusy = self.checking = True
        # Span a separate thread in order to try and start the train
        # In this way commands for other trains can be processed while
        # starting this train (and operating turnouts)
        start_new_thread(self.checkAndStart, ())

    def checkAndStart(self) :
        destinationText = ""
        # Compute number of sections ahead to be allocated
        if self.trainAllocation == 0 :
            allocationAhead = ADsettings.allocationAhead
        else :
            allocationAhead = self.trainAllocation
        if (self.lastRouteSection != None and 
           self.destination != self.lastRouteSection) :
            # Train running in burst mode - try reaching final section
            # of previous route
            route = ADautoRoute(self.destination, self.lastRouteSection,
              self.finalDirection, self.switching)
            route.reduce(self, allocationAhead)
            foundLength = len(route.step)
            atLeastOne = True
        else :
            # Final section of previous schedule step reached
            # Find a possible route to one of alternate destinations included
            # in the new schedule step
            route = None
            atLeastOne = False
            foundLength = 0
            scheduleItem = self.schedule.getFirstAlternative()
            while scheduleItem.action == ADschedule.GOTO :
                if destinationText != "" :
                    if not destinationText.startswith("[") :
                        destinationText = "[" + destinationText
                    destinationText += " "
                destinationText += scheduleItem.value.getName()
                newRoute = ADautoRoute(self.destination, scheduleItem.value,
                  self.finalDirection, self.switching)
                # Any route to this section found?
                if len(newRoute.step) > 0 : 
                    # Yes
                    atLeastOne = True
                    # See if we can advance along it.
                    newRoute.reduce(self,
                      allocationAhead - self.allocatedSections)
                    newLength = len(newRoute.step)
                    # Is this the longest reduced route?
                    if newLength > foundLength :
                        # Yes, take note
                        foundLength = newLength
                        route = newRoute
                scheduleItem = self.schedule.getNextAlternative()
            if scheduleItem.action == ADschedule.ERROR :
                # Wrong schedule
                if not self.running :
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                    "Error in schedule of train \"" + self.name + "\": "
                    + scheduleItem.message)
                    self.status = ADtrain.ERROR
                    self.destinationSwing.text = "ERROR"
                self.checking = ADtrain.turnoutsBusy = False
                return
            if destinationText.startswith("[") :
                destinationText += "]"
        # Check if any error was encounterd (i.e. transit-only destination)
        if route != None and route.error != None :
            # Error - Schedule contains transit-only destination
            if not self.running :
                # Output message only when train stops, otherwise we will
                #  flood user with messages
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                  "Schedule error: Train " + self.name + " headed to transit-only section "
                  + route.error.getName())
                self.status = ADtrain.ERROR
                destinationText = "ERROR"
            foundLength = 0
            atLeastOne = True
        if foundLength == 0 :
            # A reduced route was not found, check if at least one valid 
            # route was found
            if not atLeastOne :
                # No valid route was found, clearly an error
                # in schedule definition
                if not self.running :
                    # Output message only when train stops, otherwise we will
                    #  flood user with messages
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                      "Train " + self.name + ": no valid route from section "
                      + self.section.getName())
                    self.status = ADtrain.ERROR
                    destinationText = "ERROR"
            # Even if a valid route was found, next section is occupied or
            # allocated: exit
            if destinationText != "" :
                self.destinationSwing.text = destinationText
            self.checking = ADtrain.turnoutsBusy = False
            return
        # Minimum route found, try allocating sections
        newAllocatedSections = route.allocate(self)
        if len(route.step) == 0 :
            # First section along the route is occupied by another train or
            # new route is no longer valid: exit
            if destinationText != "" :
                self.destinationSwing.text = destinationText
            self.checking = ADtrain.turnoutsBusy = False
            return
        # Successful allocation, train can start (or continue running)
        self.allocatedSections += newAllocatedSections
        self.destinationSwing.text = self.lastRouteSection.getName()
        # If present block has no maximum speed, use default speed
        startSpeed = self.maxSpeed
        if startSpeed == 0 :
            startSpeed = len(ADsettings.speedsList)
        self.status = ADtrain.STARTING
        # Increase count of running trains
        if not self.running :
            self.running = True
            AutoDispatcher.runningTrains += 1
        # Disable input in Trains window
        self.enableSwing()
        # Make sure user is not updating train data
        # (Let's cope with Jython's lack of synchronization)
        while self.updating :
            AutoDispatcher.instance.waitMsec(100)
        if AutoDispatcher.simulation :
            self.entriesAhead.extend(route.step)
        # Create a list of sections that the train will use
        # (the list will be employed to track train movement)
        if not self.section in self.previousSections :
            self.previousSections.append(self.section)
        # Force panel re-drawing
        AutoDispatcher.repaint = True
        # Set turnouts
        route.setTurnouts()
        # Update list of blocks ahead
        for block in route.blocksList :
            if not block in self.blocksAhead :
                self.blocksAhead.append(block)
        # Train must reach next allocation point before being re-considered
        # for scheduling, unless allocated sections was lower than maximum
        self.allocationReady = self.allocatedSections < allocationAhead
        # Wait if user specified a delay before clearing signals
        if ADsettings.clearDelay > 0 :
            AutoDispatcher.instance.waitMsec(ADsettings.clearDelay)
        # Set exit signals
        route.clearSignals(self)
        # Is our current destination the target of the present schedule item?
        scheduleItem = self.schedule.getFirstAlternative()
        while scheduleItem.action == ADschedule.GOTO :
            if self.destination == scheduleItem.value :
                # Yes - Take note of commands to be executed while running
                self.schedule.next()
                self.queueCommands()
                break
            scheduleItem = self.schedule.getNextAlternative()
        # Since train is (about) moving, user must be given the posibility
        # of saving its new position to disk before quitting the program.
        AutoDispatcher.setTrainsDirty()
        # Is train starting from still (or was it already running)?
        if self.speedLevel <= 0 :
            # Starting from still. Set locomotive direction (could have changed)
            self.setOrientation(not self.reversed)
            # Delay departure, if user chose this option
            if (ADsettings.startDelayMin > 0 or
               ADsettings.startDelayMax > 0) :
                delay = ADsettings.startDelayMin + int(
                   float(ADsettings.startDelayMax -
                     ADsettings.startDelayMin) * 
                   AutoDispatcher.random.nextDouble())
                AutoDispatcher.instance.waitMsec(delay)
            # Switch front light on, if user chose this option
            if ADsettings.lightMode != 0 :
                self.setFunction(0, True)
            # Play start actions, if any
            if self.startAction != "" :
                self.doAction(self.startAction)
            elif ADsettings.defaultStartAction != "" :
                self.doAction(ADsettings.defaultStartAction)
        # Now start train!
        if (self.section.stopBlock[self.direction] == self.block or
           self.section.brakeBlock[self.direction] == self.block) :
            restrictedSpeed = self.section.getSignal(self.direction).getSpeed()
            if restrictedSpeed <= self.maxSpeed or self.maxSpeed == 0 :
                startSpeed = restrictedSpeed
        if self.locomotive != None :
            self.locomotive.brakeAdjusting = False
        self.changeSpeed(startSpeed)
        # Take note that train was started
        self.lastMove = System.currentTimeMillis()
        self.status = ADtrain.STARTED
        self.checking = ADtrain.turnoutsBusy = False
        return

    def queueCommands(self) :
        # Transfer commands from schedule to pending comands queue (items)
        # Unless they were already transferrred (in the later case,
        # commands are discarded)
        toBeAdded = not self.destination in self.itemSections
        scheduleItem = self.schedule.getFirstAlternative()
        # Transfer only commands that can be executed while train is running
        while scheduleItem.action > ADschedule.GOTO :
            if toBeAdded :
                self.itemSections.append(self.destination)
                self.items.append(scheduleItem)
            self.schedule.next()
            scheduleItem = self.schedule.getFirstAlternative()
            
    def processCommands(self, scheduleItem) :
        # Process schedule commands prefixed by $
        if scheduleItem.action == ADschedule.SWON :
            # Set train into "switching mode"
            self.switching = True
            AutoDispatcher.message("Train " + self.name +
              " entering switching mode")
            return
        if scheduleItem.action == ADschedule.SWOFF :
            # Clear "switching mode"
            self.switching = False
            AutoDispatcher.message("Train " + self.name +
              " exiting switching mode")
            return
        if scheduleItem.action == ADschedule.HELD :
            # Set signal to "held" state
            scheduleItem.value.setHeld(True)
            AutoDispatcher.message("Signal " + scheduleItem.value.getName()
              + " held")
            return
        if scheduleItem.action == ADschedule.RELEASE :
            # Clear "held" state of signal
            scheduleItem.value.setHeld(False)
            AutoDispatcher.message("Signal " + scheduleItem.value.getName()
              + " released")
            return
        if scheduleItem.action == ADschedule.SET_F_ON :
            # Set decoder function ON
            self.setFunction(scheduleItem.value, True)
            return
        if scheduleItem.action == ADschedule.SET_F_OFF :
            # Set decoder function OFF
            self.setFunction(scheduleItem.value, False)
            return
        if scheduleItem.action == ADschedule.WAIT_FOR :
            # Wait for section empty
            self.waitFor = scheduleItem.value
            AutoDispatcher.message("Train " + self.name
              + " waiting for section" + self.waitFor.getName())
            self.destinationSwing.setText("$WF:"+self.waitFor.getName())
            return
        if scheduleItem.action == ADschedule.DELAY :
            # Delay next commands
            delay = int(scheduleItem.value * 1000.)
            if delay > 0 :
                self.fastClock = False
                self.departureTime = (System.currentTimeMillis() + delay)
            return
        if scheduleItem.action == ADschedule.MANUAL_PRESENT :
            self.switchToManual(self.section)
            return
        if scheduleItem.action == ADschedule.MANUAL_OTHER :
            self.switchToManual(scheduleItem.value)
            return
        if scheduleItem.action == ADschedule.ERROR :
            # Wrong schedule
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Error in schedule of train \"" + self.name + "\": "
              + scheduleItem.message)
            self.status = ADtrain.ERROR
            return
        if scheduleItem.action == ADschedule.STOP :
            # End of schedule reached
            self.status = ADtrain.END_OF_SCHEDULE
            if ADsettings.lightMode == 2 :
                self.setFunction(0, False)
            AutoDispatcher.message("End of schedule for train "
              + self.name)
            self.destinationSwing.setText("End")
            if self.opTrain != None :
                try:
                    self.opTrain.move()
                finally :
                    i = 0 # Useless, just to complete the try construct
            return
        # Test for change of direction command
        newDirection = -1
        if scheduleItem.action == ADschedule.CCW :
            newDirection = ADsettings.ccw
        elif scheduleItem.action == ADschedule.CW :
            newDirection = 1-ADsettings.ccw
        if newDirection > -1 :
            # Change of direction
            if newDirection != self.direction :
                # Allow enough time for simulation step to complete
                if AutoDispatcher.simulation :
                    AutoDispatcher.instance.waitMsec(1000)
                self.direction = self.finalDirection = newDirection
                # We are now facing the opposite signal
                self.destinationSignal = self.section.signal[self.direction]
                # Reverse locomotive direction
                self.reversed = not self.reversed
                # Change direction in section
                self.section.allocate(self, self.direction)
                # Change color of all sections occupied by the train
                if not self.section in self.previousSections :
                    self.section.setColor()
                for section in self.previousSections :
                    section.setColor()
                for section in self.sectionsAhead :
                    section.setColor()
                AutoDispatcher.repaint = True
            AutoDispatcher.message("Train " + self.name + " headed "
              + ADsettings.directionNames[1-newDirection])
            self.updateSwing()
            return
        if scheduleItem.action == ADschedule.PAUSE :
            AutoDispatcher.message("Train " + self.name + " pausing for "
              + str(scheduleItem.value) + " sec.")
            delay = int(scheduleItem.value * 1000.)
            if delay > 0 :
                # Pause - Compute departure time
                self.fastClock = False
                self.departureTime = (System.currentTimeMillis() + delay)
                self.destinationSwing.setText("$P"+str(scheduleItem.value))
                self.updateSwing()
            return
        if scheduleItem.action == ADschedule.START_AT :
            if scheduleItem.value > FastListener.fastTime :
                self.fastClock = True
                self.departureTime = scheduleItem.value
                hours = int(scheduleItem.value/60)
                minutes = scheduleItem.value - hours * 60
                if minutes > 9 :
                    hours = str(hours) + ":" + str(minutes)
                else :
                    hours = str(hours) + ":0" + str(minutes)
                AutoDispatcher.message("Train " + self.name + " waiting until " + hours)
                self.destinationSwing.setText("$ST "+ hours)
                self.updateSwing()
            return
        if scheduleItem.action == ADschedule.SOUND :
            # Play sound
            scheduleItem.value.play()
            return
        if (scheduleItem.action == ADschedule.TC or
           scheduleItem.action == ADschedule.TT) :
            # Set turnout (or other accessory)
            try :
                t = InstanceManager.turnoutManagerInstance().getTurnout(scheduleItem.value)
            except :
                t = None
            if t == None :
                AutoDispatcher.log("Error in schedule of train \"" + self.name + "\":")
                AutoDispatcher.chimeLog("  Unknown turnout \"" + scheduleItem.value + "\"")
                self.status = ADtrain.ERROR
                return
            if scheduleItem.action == ADschedule.TC :
                t.setState(Turnout.CLOSED)
                AutoDispatcher.message("Train " + self.name + " : closed turnout "
                 + scheduleItem.value)
            else :
                t.setState(Turnout.THROWN)
                AutoDispatcher.message("Train " + self.name + " : thrown turnout "
                 + scheduleItem.value)
            return            
             
    def switchToManual(self, section) :
        # Try switching the section to Manual control
        section.setManual(True)
        # Check result
        if section.isManual() :
            # Fine
            AutoDispatcher.message("Section \"" + section.getName()
              + "\" switched to manual control")
        else :
            # Failed. Section probably does not have a "Manual control" sensor
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Cannot switch section \"" +
              section.getName() + "\" to manual control")

    def updateSwing(self) :
        # Update SWING I/O fields for train
        self.directionSwing.removeAllItems()
        self.directionSwing.addItem(ADsettings.directionNames[0])
        self.directionSwing.addItem(ADsettings.directionNames[1])
        if ADsettings.ccw == 0 :
            self.directionSwing.setSelectedIndex(self.direction)
        else :
            self.directionSwing.setSelectedIndex(1- self.direction)
        if self.section != None :
            self.sectionSwing.setSelectedItem(self.section.getName())
        else :
            self.sectionSwing.setSelectedItem("")
        self.resistiveSwing.setSelected(self.resistiveWheels)
        self.canStopAtBeginningSwing.setSelected(self.canStopAtBeginning)
        self.locoRoster.setSelectedItem(self.locoName)
        self.reversedSwing.setSelected(self.reversed)
        # Display schedule
        self.scheduleSwing.setText(self.schedule.text)

    def enableSwing(self) :
        # Enable-disable SWING input fields, depending on train status
        # Since self.running can change while processing,
        # use a local copy of it in order to obtain consistent output
        stopped = not self.running
        self.nameSwing.setEnabled(stopped)
        self.directionSwing.setEnabled(stopped)
        self.sectionSwing.setEnabled(stopped)
        self.resistiveSwing.setEnabled(stopped)
        self.canStopAtBeginningSwing.setEnabled(stopped)
        self.trainLengthSwing.setEnabled(stopped)
        self.trainAllocationSwing.setEnabled(stopped)
        self.engineerSwing.setEnabled(stopped)
        if self.engineerName == "Manual" :
            self.locoRoster.setEnabled(False)
            self.reversedSwing.setEnabled(False)
        else :
            self.locoRoster.setEnabled(stopped)
            self.reversedSwing.setEnabled(stopped)
        self.scheduleSwing.setEnabled(stopped)
        self.deleteButton.setEnabled(stopped)
        self.changeButton.setEnabled(stopped)
        self.setButton.setEnabled(stopped)
        self.startActionSwing.setEnabled(stopped)

    def getCurrentBlock(self) :
        return self.block
        
    def getPreviousBlock(self) :
        return self.previousBlock
        
    def getNextBlock(self) :
        if len(self.blocksAhead) > 0 :
            return self.blocksAhead[0]
        return None
        
class ADlocomotive :
    # Our locomotives. Contain speed tables (i.e. correspondence between 
    # speed levels and throttle settings). A locomotive is created for each
    # entry in JMRI roster. Additional locomotives are created for demo
    # layouts
    
    # STATIC VARIABLES

    locoIndex = {}
    
    # STATIC METHODS

    def getNames() :
        # Return the list of locomotive names
        return ADlocomotive.locoIndex.keys()
    getNames = ADstaticMethod(getNames)

    def getList() :
        # Return the list of locomotives
       return ADlocomotive.locoIndex.values()
    getList = ADstaticMethod(getList)

    def getByName(name) :
        # Find a locomotive by name
        return ADlocomotive.locoIndex.get(name, None)
    getByName = ADstaticMethod(getByName)

    # INSTANCE METHODS

    def __init__(self, name, address, speed, inJmri) :
        self.name = name
        ADlocomotive.locoIndex[name] = self
        self.throttle = None
        self.leadThrottle = None
        # Is this locomotive contained in JMRI roster?
        self.inJmriRoster = inJmri
        # Throttle not assigned yet
        self.throttleAssigned = False
        # Default 128 speed steps
        self.stepsNumber = 126.
        self.locoPaused = False
        self.currentSpeed = 0
        self.targetSpeed = 0
        self.rampingSpeed = 0
        self.savedSpeed = 0
        self.usedBy = None
        self.lastSent = -1L
        self.leadLoco = 0
        # Allow user to define/change address only if locomotive
        # is not in JMRI roster
        if inJmri :
            self.addressSwing = AutoDispatcher.centerLabel("")
        else :
            self.addressSwing = JTextField("", 4)
        self.setAddress(address)
        # Table of speeds corresponding to each speed level
        self.speedSwing = []
        for ind in range(len(ADsettings.speedsList)) :
            self.speedSwing.append(JTextField("", 4))
        self.setSpeedTable(speed)
        self.currentSpeedSwing = AutoDispatcher.centerLabel("0")
        self.accSwing = JTextField("0", 4)
        self.decSwing = JTextField("0", 4)
        self.runningTime = 0
        self.hoursSwing = JLabel("")
        self.hoursSwing.setHorizontalAlignment(JLabel.RIGHT)
        self.warnedTime = False
        self.mileage = 0.0
        self.milesSwing = JLabel("")
        self.milesSwing.setHorizontalAlignment(JLabel.RIGHT)
        self.warnedMiles = False
        self.runningStart = -1L
        self.setMomentum(0, 0)
        self.brakeKey = ""
        self.learningClear()

    def setSpeedTable(self, speed) :
        # Change speeds table and display its contents
        levelsNumber = len(ADsettings.speedsList)
        if speed == None :
            # Speed table not defined
            self.speed = []
            # Assign even spaced speeds from 0.01 to 1.0
            if levelsNumber > 1 :
                step = 0.99 / float(levelsNumber - 1)
            else :
                step = 0.
            level = 0.01
            for i in range(levelsNumber) :
                self.speed.append(round(level,2))
                level += step
        else :
            # Speed table defined
            # Use it
            self.speed = speed
            # Extend table, if needed
            while len(self.speed) < levelsNumber :
                self.speed.append(1.)
        # Update swing fields
        for i in range(levelsNumber) :
            self.speedSwing[i].setText(str(self.speed[i]))
            
    def getCloserLevel(self, throttleValue) :
        # Return level corresponding to a given throttle setting.
        # If no exact match is found, the higher closer level is returned.
        # Returned value is in range 1-levelsNumber.
        levelsNumber = len(ADsettings.speedsList)
        closerLevel = levelsNumber
        ind = 1
        for s in self.speed :
            if s >= throttleValue :
                closerLevel = ind
                break
            ind += 1
        if closerLevel > levelsNumber :
            return levelsNumber
        return closerLevel
        
    def setMomentum(self, acceleration, deceleration) :
        if acceleration < 0 :
            acceleration = 0
        if acceleration > 255 :
            acceleration = 255
        if deceleration < 0 :
            deceleration = 0
        if deceleration > 255 :
            deceleration = 255
        self.acceleration = acceleration
        self.deceleration = deceleration
        # Compute acceleration/deceleration rate
        # Same as NMRA DCC CV3 and CV4
        if acceleration == 0 :
            self.accStep = 0
        else :
            try :
                self.accStep = 1./(float(acceleration) * 8.96)
            except :
                self.accStep = self.acceleration = 0
        if deceleration == 0 :
            self.decStep = 0
        else :
            try :
                self.decStep = 1./(float(deceleration) * 8.96)
            except :
                self.decStep = self.deceleration = 0
        self.accSwing.setText(str(self.acceleration))
        self.decSwing.setText(str(self.deceleration))

    def getAcceleration(self) :
        return self.accStep * 10

    def getDeceleration(self) :
        return self.decStep * 10

    def setAddress(self, address) :
        # Change dcc address
        if self.throttle != None :
            self.throttle.release(None)
        if address <1 :
                address = 1
        self.address = address
        self.addressSwing.setText(str(address))
        self.throttleAssigned = False
        
    def assignThrottle(self) :
        # Ignore call if throttle already assigned
        if self.throttleAssigned :
            return
        self.throttleAssigned = True
        # Assign the throttle
        AutoDispatcher.message("Acquiring throttle for locomotive " + self.name
          + " (" + str(self.address) + ")")
        if (self.address > 100) :
            long = True
        else :
            long = False
        # request address, timeout set to 5 seconds
        self.throttle = AutoDispatcher.instance.getThrottle(self.address, long, 5)
        if (self.throttle == None) :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Couldn't assign throttle " + str(self.address) + "!")
            self.throttleAssigned = False
        else :
            if self.leadLoco != 0 :
                AutoDispatcher.message("Acquired throttle for consist "
                  + self.name + " (" + str(self.address) + ")")
                self.leadThrottle = AutoDispatcher.instance.getThrottle(
                  self.leadLoco, long, 5)
                if (self.leadThrottle != None) :
                    AutoDispatcher.message("Acquired throttle for consist "
                      + self.name + " (" + str(self.leadLoco) + ")")
                else :
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                     "Couldn't assign throttle" + 
                    str(self.leadLoco)+ " for consist " + self.name + "!")
                    self.throttleAssigned = False
            else :
                AutoDispatcher.message("Acquired throttle for locomotive "
                  + self.name + " (" + str(self.address) + ")")
                self.leadThrottle = self.throttle
            speedStepMode = self.throttle.getSpeedStepMode()
            if speedStepMode == SpeedStepMode.NMRA_DCC_128 :
                self.stepsNumber = 126.
            elif speedStepMode == SpeedStepMode.NMRA_DCC_28 :
                self.stepsNumber = 28.
            elif speedStepMode == SpeedStepMode.NMRA_DCC_27 :
                self.stepsNumber = 27.
            else :
                self.stepsNumber = 14.
            self.currentSpeed = self.throttle.getSpeedSetting()

            
    def releaseThrottle(self) :
        if self.throttle != None :
            if ADsettings.lightMode != 0 :
                self.setFunction(0, False)
            self.throttle.release(None)
            self.throttle = None
            self.throttleAssigned = False
            if self.leadLoco != 0 :
                self.leadThrottle.release(None)
                AutoDispatcher.message("Released throttles of consist "
                  + self.name + " (" + str(self.address) + ", " + 
                  str(self.leadLoco) + ")")
            else :
                AutoDispatcher.message("Released throttle of locomotive "
                + self.name + " (" + str(self.address) + ")")
            self.leadThrottle = None

    def outputMileage(self) :
        # Output values of mileage and operation hours
        minutes = int(self.runningTime/60000)
        hours = int(minutes/60)
        minutes -= hours * 60
        if minutes < 10 :
            minutes = "0" + str(minutes)
        else :
            minutes = str(minutes)
        self.hoursSwing.text = str(hours) + ":" + minutes
        if self.warnedTime :
            self.hoursSwing.setForeground(Color.red)
        else :
            self.hoursSwing.setForeground(Color.black)
        if ADsettings.units == 25.4 :
            multiplier = ADsettings.scale / 1609344.
        else :
            multiplier = ADsettings.scale / 1000000.
        self.milesSwing.text = str(round(self.mileage * multiplier,1))
        if self.warnedMiles :
            self.milesSwing.setForeground(Color.red)
        else :
            self.milesSwing.setForeground(Color.black)

    def setOrientation(self, forward) :
        # Set locomotive direction
        if self.throttle != None :
            self.throttle.setIsForward(forward)
            if ADsettings.dccDelay > 0 :
                sleep(float(ADsettings.dccDelay)/1000.)

    def setFunction(self, functionNumber, on) :
        if self.leadThrottle != None :
            if functionNumber < 0 or functionNumber > 28 :
                return
            command = "self.leadThrottle.setF" + str(functionNumber)
            if on :
                command += "(True)"
            else :
                command += "(False)"
            exec(command)
            if ADsettings.dccDelay > 0 :
                sleep(float(ADsettings.dccDelay)/1000.)
            
    def changeSpeed(self, speedLevel) :
        # Stop ?
        if speedLevel <= 0 :
            self.changeThrottleSpeed(speedLevel)
            if (self.usedBy != None and self.usedBy.running
              and self.usedBy.engineerSetLocomotive != None
              and not AutoDispatcher.simulation) :
                self.usedBy.stop()
            return
        # Speed increase/decrease - speedControl thread will take care of it
        if speedLevel > len(self.speed) :
            speedLevel = len(self.speed)
        self.changeThrottleSpeed(self.speed[speedLevel-1])

    def changeThrottleSpeed(self, speed) :
        if self.locoPaused :
            self.savedSpeed = speed
            return
        if self.targetSpeed == speed :
            return
        self.targetSpeed = speed
        if speed <= 0 :
            # STOP
           if (ADsettings.stopMode != ADsettings.PROGRESSIVE_STOP
             or speed < 0) :
                speed = -1
                if self.throttle != None :
                    self.throttle.setSpeedSetting(speed)
#                self.targetSpeed = self.rampingSpeed = self.currentSpeed = 0
#                self.targetSpeed = 0
#                self.currentSpeedSwing.setText("0")
#                self.updateMeter()
        elif self.runningStart == -1L :
                self.runningStart = System.currentTimeMillis()
        if not AutoDispatcher.paused and not AutoDispatcher.stopped :
            AutoDispatcher.message("Locomotive " + self.name + " at speed "
              + str(self.targetSpeed))
    
    def updateMeter(self) :
        # Updates locomotive's operation time
        # Called when the locomotive is stopped
        if self.runningStart != -1L :
            self.runningTime += System.currentTimeMillis() - self.runningStart
            self.runningStart = -1L

    def getName(self) :
        return self.name

    def getThrottleSpeed(self) :
        return self.rampingSpeed
        
    def pause(self) :
        self.savedSpeed = self.targetSpeed
        if ADsettings.pauseMode == ADsettings.STOP_TRAINS :
            self.changeThrottleSpeed(0)
        else :
            self.changeThrottleSpeed(-1)
        self.locoPaused = True
        # Clear learning data, since they are now meaningless
        self.brakeAdjusting = False

    def resume(self) :
        self.locoPaused = False
        self.changeThrottleSpeed(self.savedSpeed)
        
# Self-learning methods of the locomotive ==============

    def learningClear(self) :
        # Variables for self-learning braking method
        self.brakeAdjusting = False
        self.delay = 0
        self.decAdjustment = 0.
        self.initialTime = -1L
        self.initialSpeed = 0.
        self.brakingStartTime = -1L
        self.brakingSpeed = 0
        self.brakingEndTime = -1L
        self.stoppingTime = -1L

    def learningBrake(self) :
        # Train entering a braking block
        self.length = 0
        if (not ADsettings.selfLearning or self.decStep == 0
           or self.usedBy == None or self.throttle == None or
           self.brakeAdjusting or AutoDispatcher.simulation) :
            # Self-learning not supported or already active
            return False
        # Make sure we have all information needed
        block = self.usedBy.block
        previousBlock = self.usedBy.previousBlock
        train = self.usedBy
        if block == None or previousBlock == None or train == None :
            return False
        self.learningClear()
        # Take note of time and speed
        self.initialTime = System.currentTimeMillis()
        self.initialSpeed = self.rampingSpeed
        # Since we must brake, make sure that speed is not being increased
        if self.targetSpeed > self.rampingSpeed :
            self.targetSpeed = self.rampingSpeed
        # Build identification key
        self.brakeKey = (self.name + "$" + block.getName() + "$"
          + previousBlock.getName())
        # Did we already stop in this block?
        if train.brakingHistory.has_key(self.brakeKey) :
            # Yes retrieve previous data
            data = train.brakingHistory[self.brakeKey]
            self.recordedValues = data[0]
            self.squareSum = data[1]
        else :
            # First time we are braking in this block
            # Initialize data
            self.recordedValues = 0
            self.squareSum = 0
        # Compute braking data 
        # (if we have needed info and we are not running yet at minimimum speed)
        if self.recordedValues > 0 and self.targetSpeed > self.speed[0] :
            self.length = sqrt(self.squareSum/float(self.recordedValues))
            # Reduce length, in order to reach minimum speed 2 seconds before
            # reaching the stop block
            length = self.length - self.speed[0] * 2.
            if length > 0. :
                # How much time is required to reach minimum speed?
                brakeTime = ((self.initialSpeed - self.speed[0]) / 
                  self.decStep * 100.)
                # How much space is required to reach minimum speed?
                brakeLength = ((self.initialSpeed + self.speed[0])
                  * brakeTime / 2000.)
                # Is block length sufficient?
                delaySpace = length - brakeLength
                if delaySpace < 0 :
                    # No, we must reduce momentum
                    # Compute time required to break
                    # try
                    brakeTime = (length * 2000. / (self.initialSpeed +
                      self.speed[0]))
                    # Compute acceleration
                    self.decAdjustment = ((self.initialSpeed - self.speed[0]) *
                     100. / brakeTime)  - self.decStep
                    self.brakeAdjusting =True
                else :
                    # Enough space
                    # How much time is required to reach the target speed?
                    targetTime = ((self.initialSpeed - self.targetSpeed) / 
                      self.decStep *100.)
                    # Let's compute braking delay
                    self.delay = int(delaySpace * 1000. / self.targetSpeed + 
                      targetTime)
                    if self.delay > 0 :
                        start_new_thread(self.learningDelayedBrake,
                          (self.brakeKey,))
                        self.brakeAdjusting = True
                        return True
        else :
            self.brakeAdjusting = True
        # We don't have previous data or something went wrong
        # Let's locomotive start braking immediately
        self.brakingStartTime = self.initialTime
        self.brakingSpeed = self.initialSpeed
        return False


    def learningDelayedBrake(self, key) :
        # Wait before braking
        sleep(float(self.delay)/1000.)
        # Make sure that braking is still needed
        if (self.delay > 0 and self.stoppingTime == -1L and
           self.brakingStartTime == -1L and key == self.brakeKey) :
            self.brakingStartTime = System.currentTimeMillis()
            self.brakingSpeed = self.rampingSpeed
            if self.usedBy == None :
                self.changeSpeed(1)
            else :
                self.usedBy.changeSpeed(1)

    def learningEnd(self) :
        # Train completed braking
        if self.brakeAdjusting and self.brakingEndTime == -1L :
            self.brakingEndTime = System.currentTimeMillis()

    def learningStop(self) :
        # Train entering a stop block
        # Were we controlling braking times?
        if not self.brakeAdjusting :
            # No, ignore call
            return
        # Yes, take note of stop time
        self.stoppingTime = System.currentTimeMillis()
        # Recompute braking data
        # (Computation are made assuming a constant speed/throttle-setting
        # ratio. This is seldom true, but each iteration will improve results)
        # Evaluate block length, based on speeds and timing
        # Computed length is expressed as throttle-setting * time and does
        # thus not refer to the actual length in inches or mm.
        brakingTime = creepingTime = 0
        # Did train start braking (at least!)
        if self.brakingStartTime == -1L :
            # No - braking delay was excessive!
            # Set length to half the length of block computed
            # in previous iteration (at least for the time being. A more
            # accurate computation can be implemented later)
            if AutoDispatcher.debug :
                print (self.brakeKey + " Delay " + str(self.delay)
                  + " Adjustment " + str(self.decAdjustment)
                  + " length " + str(self.length) + " braking not started")
            length = self.length * 0.5
        else :
            # Yes, train started braking
            # Compute distance at full speed
            length  = (float(self.brakingStartTime - self.initialTime) *
              (self.brakingSpeed + self.initialSpeed) / 2000.)
            # Did train attain minimum speed?
            if self.brakingEndTime == -1L :
                # No, set end of braking time to stopping time
                if AutoDispatcher.debug :
                    print (self.brakeKey + " Delay " + str(self.delay)
                      + " Adjustment " + str(self.decAdjustment)
                      + " length " + str(length) + " minimum not reached")
                self.brakingEndTime = self.stoppingTime
                if length > self.length :
                    length = self.length
                length = length * 0.5
            else :
                # Yes, minimum speed attained
                # Compute distance run at minimum speed
                creepingTime = self.stoppingTime - self.brakingEndTime
                length += (self.rampingSpeed * float(creepingTime) / 1000.)
                if AutoDispatcher.debug :
                    print (self.brakeKey + " Delay " + str(self.delay)
                      + " Adjustment " + str(self.decAdjustment)
                      + " length " + str(length) + " creepingTime " + str(creepingTime))
            # Now add the length of the braking ramp (braking distance)
            brakingTime = self.brakingEndTime - self.brakingStartTime
            length += ((self.brakingSpeed + self.rampingSpeed) * 
              float(brakingTime) / 2000.)
        # Update braking data
        if brakingTime >=0 and creepingTime >= 0 and length > 0 :
            self.recordedValues += 1
            self.squareSum += length * length
            train = self.usedBy
            if train != None :
                train.brakingHistory[self.brakeKey] = [self.recordedValues,
                  self.squareSum]
        # Clear data
        self.brakeKey = ""
        self.brakeAdjusting = False
        
# ENGINEER ==============

class ADengineer :
    # Our engineer is rather simple. It only lets know AutoDispatcher that
    # the ADlocomotive class should be used. ADlocomotive takes care of all
    # the rest.
    def setLocomotive(self, locomotive) :
        return
        
# ROUTE ==============

class ADautoRoute :
    # Defines a route, i.e. the list of entries connecting two sections
    def __init__(self, startSection, endSection, direction, switching) :
        # Train direction
        self.direction = direction
        # Is train running in switching mode?
        self.switching = switching
        # Error indicator. Set if destination is "transit-only"
        self.error = None
        # List of blocks contained in the route (filled by setTurnouts method)
        self.blocksList = []
        # Create an array to keep note of which sections were "visited"
        # in order to avoid an endless loop
        self.searchedSections = []
        # And now search - step will contain the list of entries found
        self.step = self.__fromTo__(startSection, endSection, direction)
        if self.found :
            self.error = None

    def __fromTo__(self, startSection, endSection, direction):
        # Internal method. Recursively explores all possibe routes.
        # If more than one route is found the shorter one (i.e. that with
        # less steps) is returned.
        self.found = False
        # Exit if this section was already "visited" (to avoid an endless loop!)
        if startSection in self.searchedSections :
            return []
        # Check if direction is acceptable for this section or if train is
        # in "switching mode" (switching trains can enter any track)
        if (((direction + 1) & startSection.direction) == 0 and
           not self.switching) :
            return []
        # Did we reach destination?
        if startSection == endSection :
            # Check if destination is transit-only
            if endSection.transitOnly[direction] and not self.switching :
                # Transit only destination
                self.error = endSection
            else :
                self.found = True
            return []
        # Mark this section as "visited" (to avoid an endless loop!)
        self.searchedSections.append(startSection)
        route = []
        routeLen = 0
        ## Get next sections in the desired direction
        for entry in startSection.getEntries(direction) :
            nextSection = entry.getExternalSection()
            # Compute direction along next section
            if entry.getDirectionChange() :
                newDirection = not direction
            else :
                newDirection = direction
            # Go ahead exploring
            newRoute = self.__fromTo__(nextSection, endSection, newDirection)
            if self.found :
                # A possible route found
                # Is it the first one found, or is it shorter than
                # previous routes?
                newLen = len(newRoute) + 1
                if routeLen == 0 or newLen < routeLen :
                    # Yes, keep it
                    route = [entry]
                    route.extend(newRoute)
                    routeLen = newLen
        # Allow this section to be considered in alternative routes
        self.searchedSections.pop()
        self.found = routeLen > 0
        return route

    def reduce(self, train, allocationAhead):
        # Reduces the length of the route (i.e. number of sections 
        # contained in it) to the minimum between:
        #   its total length;
        #   allocationAhead value.
        # Unless the train runs in "switching mode", the reduced
        # route is always terminated with a non transit-only section
        # (making the route longer or shorter, as needed) in order to
        # avoid (as far as possible) "grid lock" situations.
        # The route is anyway terminated before the first occupied section
        # encountered, unless:
        #   The occupied section is transit-only and
        #   "burst mode" is enabled
        #   (i.e. its direction name is suffixed by "+) and
        #   the section is occupied by a train running in the same 
        #   direction of the present train and the first non
        #   transit-only  section encountered after it is free.
        keep = lastFreeSection = 0
        firstTime = True
        # Number of sections with signal (or not transit-only) encountered
        nSections = 0
        previousBurst = False
        direction = self.direction
        ind = 1
        # Process entries contained in the route
        for entry in self.step :
            # Check if possible Xovers are available
            if not entry.areXoversAvailable() :
                # An Xover is allocated to another train. Terminate the route
                break
            # Get the corresponding section
            section = entry.getExternalSection()
            # Adjust train direction, if needed
            if entry.getDirectionChange() :
                direction = not direction
            # Is use of this section allowed ?
            if (section.isManual() or (not section.transitOnly[direction] and
              ADgridGroup.lockRisk(train,
              entry.getInternalSection(), section))) :
                # No
                break
            # get the train (if any) to which the section is allocated
            otherTrain = section.getAllocated()
            # Check if the section can be used by present train
            if (not section.isAvailable() and (otherTrain != train
               or section.isOccupied())) :
                # The section is occupied or allocated to another train
                # Is this the first section along the route or is
                # "burst" mode not allowed?
                if (firstTime or (not section.burst and not previousBurst)) :
                    # Present train cannot use it
                    break
                # Is a train using this section?
                if otherTrain == None :
                    # No train, section contains only rolling stock.
                    # Present train cannot use it
                    break
                # We have another train: is it running in the same direction?
                if otherTrain.getDirection() != direction :
                # Other train in opposite direction, present train cannot start
                    break
                # If this is not a Transit-Only section, train can run up to
                # previous  empty section
                if not section.transitOnly[direction] :
                    keep = lastFreeSection
                    break
            else :
                # Take note that section is free (or allocated to this train
                # but not occupied yet)
                lastFreeSection = ind
                # Count free sections encountered and equipped with exit signal
                if (not section.transitOnly[direction] or
                   section.getSignal(direction).hasHead()) :
                    nSections += 1
                # Could the reduced route end in this section?
                if not section.transitOnly[direction] or self.switching :
                    # The section is not transit-only (or train
                    # in switching mode).
                    # Terminate the route here if the requested number
                    # of sections ahead was found
                    keep = lastFreeSection
                    if nSections >= allocationAhead :
                        break
            # Take note that we already encountered a section
            firstTime = False
            # PreviousBurst temporarily suppressed (could be implemented
            # as an option)
#            previousBurst = section.burst
            # Make sure signal is not held
            if section.getSignal(direction).isHeld() :
                break
            ind += 1
        # Job almost done.  "keep" contains the number of entries to be kept
        # Remove possible trailing entries from the route
        while len(self.step) > keep :
            self.step.pop()
        return nSections

    def allocate(self, train):
        # Allocates all sections contained in the (reduced) route,
        # further reducing the route if part of it is occupied by 
        # another train (even if running in the same direction)
        keep = 0
        nSections = 0
        freeRoute = True
        direction = self.direction
        destination = None
        ind = 1
        # Process all entries in the route
        for entry in self.step :
            # Adjust direction, if needed
            if entry.getDirectionChange() :
                direction = not direction
            # Retrieve relevant section
            section = entry.getExternalSection()
            # Make sure section is not occupied or allocated
            if section.isAvailable() :
                # Allocate sections in front of other trains (if any)
                # only if they are not transit-only
                if freeRoute or not section.transitOnly[direction] :
                    section.allocate(train, direction)
            elif section.getAllocated() != train :
                # Section occupied or allocated to another train
                freeRoute = False
            if section.getAllocated() == train :
                train.lastRouteSection = section
            # Take note of last not occupied section
            if freeRoute :
                keep = ind
                if (not section.transitOnly[direction] or
                   section.getSignal(direction).hasHead()) :
                    nSections += 1
                destination = section
                if not section in train.sectionsAhead :
                    train.sectionsAhead.append(section)
            # Make sure signal is not held
            if section.getSignal(direction).isHeld() :
                break
            ind += 1
        # Remove trailing elements from the route (if needed)
        while len(self.step) > keep :
            self.step.pop()
        if destination != None :
            train.destination = destination
            direction = destination.trainDirection
            train.finalDirection = direction
            train.destinationSignal = destination.signal[direction]
        return nSections

    def setTurnouts(self):
        # Set all turnouts contained in the (reduced) route
        # Record also blocks of the route in blocksList
        self.blocksList = []
        self.turnoutList = []
        # Before starting, make sure we have a valid route :-)
        if len(self.step) == 0 :
            return
        direction = self.direction
        # Start setting turnouts from stop block of starting section
        startBlock = self.step[0].getInternalSection().stopBlock[direction]
        for entry in self.step :
            endBlock = entry.getInternalBlock()
            # Set turnouts up to exit block included
            thrown = self.__setTurnouts__(startBlock, endBlock, direction)
            # Set turnouts from present section to next section
            startBlock = entry.getExternalBlock()
            if startBlock.setTurnouts(endBlock, self.turnoutList) :
                thrown = True
            # and vice-versa
            if endBlock.setTurnouts(startBlock, self.turnoutList) :
                thrown = True
            # Keep note if some turnouts were thrown
            # (info will be used when clearing signals)
            entry.getInternalSection().turnoutsThrown = thrown
            # Adjust train direction, if needed
            if entry.getDirectionChange() :
                direction = not direction
        # Set turnouts from entry of last section to its stop block
        endBlock = startBlock.getSection().stopBlock[direction]
        if self.__setTurnouts__(startBlock, endBlock, direction) :
            # Keep note that some turnouts were thrown
            startBlock.getSection().turnoutsThrown = True
        
    def __setTurnouts__(self, startBlock, endBlock, direction) :
        # Internal method
        # Sets turnouts between two blocks of the same section
        # Returns True if any turnout was thrown.
        # Ignore call, if start and end block are the same one
        if startBlock == endBlock :
            self.blocksList.append(startBlock)
            return False
        thrown = False
        previousBlock = None
        for block in startBlock.getSection().getBlocks(direction) :
            # Find starting block
            if previousBlock == None :
                if block == startBlock :
                    previousBlock = block
                    self.blocksList.append(block)
            else:
                # Starting block found, set turnouts
                # Note that we need to set turnouts included in both paths:
                # From previousBlock to block; and 
                # from block to previousBlock.
                if block.setTurnouts(previousBlock, self.turnoutList) :
                    thrown = True
                if previousBlock.setTurnouts(block, self.turnoutList) :
                    thrown = True
                self.blocksList.append(block)
                # Look for ending block
                if block == endBlock :
                    break
                previousBlock = block
        return thrown

    def clearSignals(self, train):
        # Clears all signals along the route
        # Cannot be run before setTurnouts method!
        # First of all, make a copy of sectionsAhead
        sections = []
        sections.extend(train.previousSections)
        sections.append(train.section)
        sections.extend(train.sectionsAhead)
        # Remove last section (its signal will stay red)
        sections.pop()
        # Reverse sections order
        # (set the farer signal first)
        sections.reverse()
        # Next signal is red
        oldIndication = 0
        anyThrown = False
        processedSections = []
        setRed = False
        for section in sections :
            if section in processedSections :
                continue
            processedSections.append(section)
            signal = section.getSignal(section.trainDirection)
            if setRed :
                signal.setIndication(0)
                continue
            if section.turnoutsThrown :
                anyThrown = True
            indication = ADindication.getIndication(oldIndication, anyThrown)
            signal.setIndication(indication)
            # No need of repainting panel, Layout Editor will do it anyway
            AutoDispatcher.repaint = False
            if signal.hasHead() :
                anyThrown = False
                oldIndication = indication
            # Make sure train did not advance in the meantime
            if section == train.section :
                setRed = True
                           
# SIGNAL HEAD ==============

class ADsignalHead :
    # Our signal head class 
    # Created also if there is no signal head in JMRI
    def __init__(self, signalName) :
        self.name = signalName
        self.signalHead = None
        self.iconOnLayout = False
        if signalName.strip() != "" :
            self.signalHead = InstanceManager.getDefault(SignalHeadManager).getSignalHead(signalName)
            if self.signalHead != None :
                self.setHeld(False)
                self.iconOnLayout = signalName in AutoDispatcher.signalIcons

    def setAppearance(self, appearance) :
        # Record new signal appearance and modify also that of the signal head
        # (if available)
        self.appearance = appearance
        if self.signalHead != None :
            if (not ADsettings.trustSignals or
               self.signalHead.getAppearance() != self.appearance) :
                AutoDispatcher.signalCommands[self.signalHead] = [
                  self.appearance, System.currentTimeMillis()]
                self.signalHead.setAppearance(self.appearance)
                # Wait if user specified a delay between signal operation
                if ADsettings.signalDelay > 0 :
                    AutoDispatcher.instance.waitMsec(
                      ADsettings.signalDelay)

    def getAppearance(self) :
        return self.appearance

    def isHeld(self) :
        # Checks if the SignalHead (if any) is "HELD"
        if self.signalHead == None :
                return False
        else :
                return self.signalHead.getHeld()

    def setHeld(self, newHeld) :
        # Set the SignalHead (if any) to "HELD"
        if self.signalHead != None :
            self.signalHead.setHeld(newHeld)
            
    def hasHead(self) :
        return self.signalHead != None
            
    def hasIcon(self) :
        return self.iconOnLayout

# SIGNAL MAST ==============

class ADsignalMast :
    # Our multi-head signal class 
    # Created also if there is no signal head in JMRI
    
    # STATIC VARIABLES
    
    signalsList = {}
    
    # STATIC METHODS
    
    def getByName(name) :
        return ADsignalMast.signalsList.get(name, None)
    getByName = ADstaticMethod(getByName)

    def provideSignal(name) :
        signal = ADsignalMast.getByName(name)
        if signal == None :
            signal = ADsignalMast(name, ADsettings.signalTypes[0], [name])
        return signal
    provideSignal = ADstaticMethod(provideSignal)

    def getNames() :
        return ADsignalMast.signalsList.keys()
    getNames = ADstaticMethod(getNames)

    def getList() :
        return ADsignalMast.signalsList.values()
    getList = ADstaticMethod(getList)

    def getTable() :
        outBuffer = []
        for signal in ADsignalMast.signalsList.values() :
            outLine = [signal.name]
            outLine.append(signal.signalType.name)
            outHeads = []
            for h in signal.signalHeads :
                if h == None :
                    outHeads.append("")
                else :
                    outHeads.append(h.name)
            outLine.append(outHeads)
            outBuffer.append(outLine)
        return outBuffer
    getTable = ADstaticMethod(getTable)

    def putTable(inBuffer) :
        for inLine in inBuffer :
            type = ADsettings.signalTypes[0]
            for newType in ADsettings.signalTypes :
                if newType.name == inLine[1] :
                    type = newType
                    break
            ADsignalMast(inLine[0], type, inLine[2])            
    putTable = ADstaticMethod(putTable)

    def __init__(self, signalName, signalType, signalHeads) :
        self.name = signalName
        self.signalType = signalType
        self.signalType.changeUse(1)
        self.inUse = 0
        self.headsNumber = self.signalType.headsNumber
        self.signalHeads = [None] * self.headsNumber
        ind = 0
        for headName in signalHeads :
            if ind >= self.headsNumber :
                break
            self.signalHeads[ind] = ADsignalHead(headName)
            ind += 1
        self.indication = -1
        self.setIndication(0)
        ADsignalMast.signalsList[self.name] = self

    def setIndication(self, indication) :
        if self.indication == indication :
            return
        self.indication = indication
        if self.indication >= len(self.signalType.aspects) :
            return
        aspects = self.signalType.aspects[indication]
        for ind in range(self.headsNumber) :
            if self.signalHeads[ind] != None :
                self.signalHeads[ind].setAppearance(aspects[ind])

    def changeUse(self, increment) :
        self.inUse += increment
        if self.inUse < 0 :
            self.inUse = 0
            self.signalType.changeUse(-1)
            newDic = {}
            for s in ADsignalMast.getList() :
                if s != self :
                    newDic[s.name] = s
            ADsignalMast.signalsList = newDic           

    def getName(self) :
        return self.name

    def getIndication(self) :
        return self.indication

    def getSpeed(self) :
        if self.indication >= len(self.signalType.speeds) :
            return 0
        speed = self.signalType.speeds[self.indication]
        if speed < 0 :
            if self.indication >= len(ADsettings.indicationsList) :
                return 0
            return ADsettings.indicationsList[self.indication].speed
        return speed + 1

    def setHeld(self, newHeld) :
        if self.signalHeads > 0 :
            self.signalHeads[0].setHeld(newHeld)
    
    def isHeld(self) :
        # Checks if any SignalHead is "HELD"
        for head in self.signalHeads :
            if head != None :
                if head.isHeld() :
                    return True
        return False

    def hasIcon(self) :
        # Checks if any SignalHead has an icon
        for head in self.signalHeads :
            if head != None :
                if head.hasIcon() :
                    return True
        return False
            
    def hasHead(self) :
        if self.headsNumber < 1 or self.signalHeads[0] == None :
            return False
        return self.signalHeads[0].hasHead()

# SIGNAL INDICATIONS ==============

class ADindication :
           
    # STATIC METHODS

    def getIndication(nextIndication, nextTurnout) :
        next = nextIndication
        for i in range(3) :
            for j in range(2, len(ADsettings.indicationsList)) :
                a = ADsettings.indicationsList[j]
                if ((a.nextIndication == next
                or (a.nextIndication >=0 and next >= 0 and 
                ADsettings.indicationsList[a.nextIndication].name ==
                ADsettings.indicationsList[next].name))
                and a.nextTurnout == nextTurnout) :
                    return j
            if (i & 1) == 0 :
                next = -1
            else :
                next = nextIndication
            if i == 1 :
                nextTurnout = -1
        return 1
    getIndication = ADstaticMethod(getIndication)

    def __init__(self, name, nextIndication, nextTurnout, speed) :
        self.name = name
        self.nextIndication = nextIndication
        self.nextTurnout = nextTurnout
        if self.nextTurnout < -1 :
            self.nextTurnout = -1
        elif self.nextTurnout > 1 :
            self.nextTurnout = 1
        self.speed = speed
        self.nameSwing = JTextField(self.name, 20)
        self.nextIndicationSwing = JComboBox()
        self.nextTurnoutSwing = JComboBox(["-", "Closed", "Thrown"])
        self.nextTurnoutSwing.setSelectedIndex(self.nextTurnout+1)
        self.speedSwing = JComboBox()

# SIGNAL TYPE ==============

class ADsignalType :

    # STATIC METHODS

    def adjust() :
        for s in ADsettings.signalTypes :
            s.adjustIndications()
    adjust = ADstaticMethod(adjust)
           
    # INSTANCE METHODS

    def __init__(self, name, aspects, speeds) :
        # aspects = [[aspect0, aspect1...],...]
        self.name = name
        self.inUse = 0
        # Compute number of signal heads
        self.headsNumber = 1
        for a in aspects :
            if len(a) > self.headsNumber :
                self.headsNumber = len(a)
        # Compute number of aspects
        if ADsettings == None :
            self.aspectsNumber = 2
        else :
            self.aspectsNumber = len(ADsettings.indicationsList)
        if len(aspects) > self.aspectsNumber :
            self.aspectsNumber = len(aspects)
        # Initialize aspects for each head
        # Default setting for stop = all heads RED
        self.aspects = [[SignalHead.RED] * self.headsNumber]
        # Default setting for other indications = all heads GREEN
        for i in range(self.aspectsNumber-1) :
            self.aspects.append([SignalHead.GREEN] * self.headsNumber)
        # Set actual aspects
        i = 0
        for a in aspects :
            j = 0
            for aa in a :
                self.aspects[i][j] = aa
                j += 1
            i += 1
        self.speeds = [-1] * self.aspectsNumber
        i = 0
        for s in speeds :
            if i >= self.aspectsNumber :
                break
            self.speeds[i] = s
            i += 1
        self.nameSwing = JTextField(self.name, 20)

    def adjustIndications(self) :
        diff = len(ADsettings.indicationsList) - self.aspectsNumber
        if diff > 0 :
            for i in range(diff) :
                self.aspects.append([SignalHead.GREEN] * self.headsNumber)
                self.speeds.append(-1)
        self.aspectsNumber = len(ADsettings.indicationsList)

    def changeUse(self, increment) :
        self.inUse += increment
        if self.inUse < 0 :
            self.inUse = 0

# SETTINGS ==============

class ADsettings :
    # Contains script settings, in a format suitable to save-retrieve them from preferences file

    # CONSTANTS
    
    # Pause modes
    IGNORE = 0
    STOP_TRAINS = 1
    EMERGENCY_STOP_TRAINS = 2
    POWER_OFF = 3 
    ATTENTION_SOUND = 0
    START_STOP_SOUND = 1
    DERAILED_SOUND = 2
    STALLED_SOUND = 3
    LOST_CARS_SOUND = 4
    WRONG_ROUTE_SOUND = 5
    # Color names dictionary
    colors = {"BLACK": Color.BLACK,
              "BLUE": Color.BLUE,
              "CYAN": Color.CYAN,
              "DARK_GRAY": Color.DARK_GRAY,
              "GRAY": Color.GRAY,
              "GREEN": Color.GREEN,
              "LIGHT_GRAY": Color.LIGHT_GRAY,
              "MAGENTA": Color.MAGENTA,
              "ORANGE": Color.ORANGE,
              "PINK": Color.PINK,
              "RED": Color.RED,
              "WHITE": Color.WHITE,
              "YELLOW": Color.YELLOW}
    # Default sounds (None = 0)
    # Default sound names
    soundLabel = ["Attention",
                  "Script start/stop",
                  "Derailed train",
                  "Stalled train",
                  "Lost cars",
                  "Wrong route"]
    # Detection action
    DETECTION_DISABLED = 0
    DETECTION_WARNING = 1
    DETECTION_PAUSE = 2

    # Stop Mode
    PROGRESSIVE_STOP = 0
    IMMEDIATE_STOP = 1

    # STATIC VARIABLE - Current settings              

    # Default directions
    directionNames =("CCW", "CW")
    ccwStart = ""
    ccwEnd = ""
    ccw = 0
    # Measurement units 1.0=mm. 10.0=cm. 25.4=inches
    if Locale.getDefault().getCountry() == "US" :
        units = 25.4
    else :
        units = 1.0
    useLength = False
    max_trains = 0
    allocationAhead = 1
    blockTracking = False
    verbose = False
    ringBell = True
    pauseMode = STOP_TRAINS
    derailDetection = DETECTION_PAUSE
    wrongRouteDetection = DETECTION_PAUSE
    # Maximum idle time between speed commands
    maxIdle = 60000
    useCustomColors = True
    # Default section colors (as strings)
    colorTable = ["BLACK", "BLUE", "RED", "YELLOW", "ORANGE",
      "MAGENTA", "CYAN"]
    sectionColor = None
    useCustomWidth = True
    trustTurnouts = True
    # Delay between turnouts operations
    turnoutDelay = 1000
    trustSignals = True
    # Delay between signals operations
    signalDelay = 0
    # Delay before clearing signals
    clearDelay = 0
    # table of sections' settings in human readable format
    sections = []
    # table of blocks' settings in human readable format
    blocks = []
    # Speeds
    speedsList = ["Min.", "Low", "Med.", "High", "Max."]
    dccDelay = 10
    startDelayMin = 0
    # Speed change frequency (in 1/10h of second)
    speedRamp = 2
    # Ligths Mode: 
    #   0 = No lights;
    #   1 = ON/OFF when train starts/stops;
    #   2 = ON/OFF when schedule starts/ends
    lightMode = 1
    indicationsList = []
    signalTypes = []
    startDelayMax = 0
    separateTurnouts = False
    separateSignals = False
    stalledDetection = DETECTION_WARNING
    stalledTime = 60000.
    selfLearning = False
    stopMode = IMMEDIATE_STOP
    lostCarsDetection = DETECTION_WARNING
    if units == 25.4 :
        lostCarsTollerance = 2032.0
    else :
        lostCarsTollerance = 2000.0
    lostCarsSections = 3
    sectionTracking = False
    soundList = []
    defaultSounds = [1] * len(soundLabel)
    try :
        soundRoot = jmri.util.FileUtil.getUserFilesPath()
    except :
        try:
            soundRoot = XmlFile.userFileLocationDefault()
        except :
            AutoDispatcher.log("Unable to find user sound's directory")
        soundRoot = ""
    soundDic = {}
    maintenanceTime = 0.
    maintenanceMiles = 0.
    scale = 87
    flashingCycle = 1.0
    resistiveDefault = False
    defaultStartAction = ""
    autoRestart = False

    # STATIC METHODS
    
    def getSpeedName(speedLevel) :
        if speedLevel == 0 :
            return "Stop"
        if speedLevel > len(ADsettings.speedsList) :
            return "Unknown: " + str(speedLevel)
        return ADsettings.speedsList[speedLevel-1] 
    getSpeedName = ADstaticMethod(getSpeedName)

    def getScale() :
        return ADsettings.scale 
    getScale = ADstaticMethod(getScale)

    def getUnits() :
        return ADsettings.units 
    getUnits = ADstaticMethod(getUnits)

    def stringToColor(c) :
        # Convert a string into a color
        if c.startswith("R:") :
            # Custom RGB color
            r = int(c[2:5])
            g = int(c[7:10])
            b = int(c[12:])
            return Color(r, g, b) 
        # Standard Java color
        return ADsettings.colors[c]
    stringToColor = ADstaticMethod(stringToColor)

    def rgbToString(rgb) :
        # Build a color string "R:rrrG:gggB:bbb"
        lab = ["R:", "G:", "B:"]
        out = ""
        for j in range(3) :
            out += lab[j]
            c = rgb[j]
            if c < 100 :
                out += "0"
                if c < 10 :
                    out += "0"
            out += str(c)
        return out
    rgbToString = ADstaticMethod(rgbToString)
 
    def initColors() :
        # Convert colors from strings to JAVA constants
        ADsettings.sectionColor = []
        for c in ADsettings.colorTable :
            ADsettings.sectionColor.append(ADsettings.stringToColor(c))
    initColors = ADstaticMethod(initColors)

    def save(file, sections, blocks) :
        outIndications = []
        for indication in ADsettings.indicationsList :
            outIndications.append([indication.name, indication.nextIndication,
              indication.nextTurnout, indication.speed])
        outSignalTypes = []
        for signal in ADsettings.signalTypes :
            signalLine = [signal.name]
            indicationLines = []
            for i in range(len(ADsettings.indicationsList)) :
                indicationLine = []
                for aa in signal.aspects[i] :
                    indicationLine.append(AutoDispatcher.inverseAspects[aa])
                indicationLines.append(indicationLine)
            signalLine.append(indicationLines)
            signalLine.append(signal.speeds)
            outSignalTypes.append(signalLine)
        sounds = []
        for s in ADsettings.soundList :
            sounds.append([s.name, s.path])
        locations = []
        for l in ADlocation.getList() :
            locations.append([l.name, l.text])
        outData = (AutoDispatcher.version, ADsettings.directionNames,
         ADsettings.ccwStart, ADsettings.ccwEnd, ADsettings.turnoutDelay, 
         ADsettings.max_trains, ADsettings.allocationAhead, ADsettings.verbose, 
         ADsettings.pauseMode, ADsettings.derailDetection, 
         ADsettings.wrongRouteDetection, ADsettings.maxIdle, 
         ADsettings.useCustomColors, ADsettings.colorTable, ADsettings.useCustomWidth,
         sections, blocks, ADsettings.trustTurnouts,
         ADsettings.trustSignals, ADsettings.signalDelay, ADsettings.units,
         ADsettings.clearDelay, ADsettings.useLength, ADsettings.blockTracking,
         ADsettings.speedsList, ADsettings.resistiveDefault, ADsettings.dccDelay,
         ADsettings.startDelayMin, ADsettings.speedRamp, ADsettings.lightMode,
         outIndications, ADsettings.ringBell, outSignalTypes, ADsignalMast.getTable(),
         ADsettings.startDelayMax, ADsettings.separateTurnouts,
         ADsettings.separateSignals, ADsettings.stalledDetection, 
         ADsettings.stalledTime, ADsettings.selfLearning, ADsettings.stopMode, 
         ADsettings.lostCarsDetection, ADsettings.lostCarsTollerance,
         ADsettings.lostCarsSections, ADsettings.sectionTracking, sounds,
         ADsettings.defaultSounds, ADsettings.soundRoot, ADsettings.maintenanceTime,
         ADsettings.maintenanceMiles, ADsettings.scale, locations,
         ADsettings.flashingCycle, ADsettings.defaultStartAction,
         ADsettings.autoRestart)

        file.writeObject(outData)
    save = ADstaticMethod(save)

    def load(inData) :
        creationVersion = inData[0]
        ADsettings.directionNames = inData[1]
        ADsettings.ccwStart = inData[2]
        ADsettings.ccwEnd = inData[3]
        ADsettings.turnoutDelay = inData[4]
        ADsettings.max_trains = inData[5]
        ADsettings.allocationAhead = inData[6]
        ADsettings.verbose = inData[7]
        ADsettings.pauseMode = inData[8]
        ADsettings.derailDetection = inData[9]
        ADsettings.wrongRouteDetection = inData[10]
        ADsettings.maxIdle = inData[11]
        ADsettings.useCustomColors = inData[12]
        ADsettings.colorTable = inData[13]
        ADsettings.useCustomWidth = inData[14]
        ADsettings.sections = inData[15]
        ADsettings.blocks = inData[16]
        ADsettings.trustTurnouts = inData[17]
        ADsettings.trustSignals = inData[18]
        ADsettings.signalDelay = inData[19]
        ADsettings.units = inData[20]
        ADsettings.clearDelay = inData[21]
        if ADblock.blocksWithLength > 0 :
            ADsettings.useLength = inData[22]
        ADsettings.blockTracking = inData[23]
        ADsettings.speedsList = inData[24]
        ADsettings.resistiveDefault = inData[25]
        ADsettings.dccDelay = inData[26]
        ADsettings.startDelayMin = inData[27]
        ADsettings.speedRamp = inData[28]
        ADsettings.lightMode = inData[29]
        ADsettings.indicationsList = []
        for a in inData[30] :
            ADsettings.indicationsList.append(ADindication(a[0], a[1], a[2], a[3]))
        ADsignalType.adjust()
        ADsettings.ringBell = inData[31]
        ADsettings.signalTypes = []
        for s in inData[32] :
            indicationLines = []
            for a in s[1] :
                indicationLine = []
                for aa in a :
                    indicationLine.append(AutoDispatcher.headsAspects[aa])
                indicationLines.append(indicationLine)
            ADsettings.signalTypes.append(ADsignalType(s[0], indicationLines, s[2]))
        ADsignalMast.putTable(inData[33])
        ADsettings.startDelayMax = inData[34]
        ADsettings.separateTurnouts = inData[35]
        ADsettings.separateSignals = inData[36]
        ADsettings.stalledDetection = inData[37]
        ADsettings.stalledTime = inData[38]
        ADsettings.selfLearning = inData[39]
        ADsettings.stopMode = inData[40]
        if ADsettings.stopMode > 1 :
            ADsettings.stopMode = 1
        ADsettings.lostCarsDetection = inData[41]
        ADsettings.lostCarsTollerance = inData[42]
        ADsettings.lostCarsSections = inData[43]
        ADsettings.sectionTracking = inData[44]
        ADsettings.soundList = []
        for i in inData[45] :
            s = ADsound(i[0])
            s.setPath(i[1])
            ADsettings.soundList.append(s)
        ADsettings.newSoundDic()
        ADsettings.defaultSounds = inData[46]
        while len(ADsettings.defaultSounds) < len(ADsettings.soundLabel) :
            ADsettings.defaultSounds.append(1)
        if inData[47] != "" :
            ADsettings.soundRoot = inData[47]
        ADsettings.maintenanceTime = inData[48]
        ADsettings.maintenanceMiles = inData[49]
        ADsettings.scale = inData[50]
        for l in inData[51] :
            location = ADlocation(l[0])
            location.setSections(l[1])
        ADsettings.flashingCycle = inData[52]
        if len(inData) > 53 :
            ADsettings.defaultStartAction = inData[53]
            if len(inData) > 54 :
                ADsettings.autoRestart = inData[54]
        ADsettings.initColors()
        
    load = ADstaticMethod(load)
      
    def newSoundDic() :
        newDic = {}
        for s in ADsettings.soundList :
            newDic[s.name] = s
        ADsettings.soundDic = newDic
    newSoundDic = ADstaticMethod(newSoundDic)

    # INSTANCE METHODS!

    def __init__(self) :
        ADsettings.initColors()
        ADsettings.indicationsList = [ADindication("Stop", -1, -1, 0),
          ADindication("Clear", -1, -1, len(ADsettings.speedsList))]        
        ADsettings.signalTypes = [ADsignalType("Single Head", [[SignalHead.RED],
          [SignalHead.GREEN]], [])]
        alarmSound = ADsound("Bell")
        alarmSound.setPath("resources/sounds/bell.wav")
        ADsettings.soundList = [alarmSound]
        ADsettings.newSoundDic()
        


# SCHEDULE ==============

class ADschedule :
    # Encapsulates all info relevant to a schedule

    # CONSTANTS
    
    # Action types
    # Actions that require train stopping
    ERROR = -2
    END_ALTERNATIVE = -1
    STOP = 0
    PAUSE = 1
    START_AT = 2
    CCW = 3
    CW = 4
    WAIT_FOR = 5
    IFE = 6
    IFAT = 7
    IFH = 8
    MANUAL_PRESENT = 9
    # Actions that can be executed while train is running
    GOTO = 10
    SWON = 11
    SWOFF = 12
    HELD = 13
    RELEASE = 14
    SET_F_ON = 15
    SET_F_OFF = 16
    DELAY = 17
    MANUAL_OTHER = 18
    SOUND = 19
    TC = 20
    TT = 21
    
    def __init__(self, text) :
        # Save original text
        self.text = text
        self.source = []
        textSpace  = text.replace(",", " ")
        # Break down input text into tokens
        splitted = textSpace.split()
        # Break down tokens containing open brackets "("
        charList = ")[]"
        for s in splitted :
            i=s.find("(")
            while i >= 0 and i < len(s)-1 :
                self.__split(s[:i+1], charList)
                s = s[i+1:]
                i=s.find("(")
            self.__split(s, charList)
        self.__clearFields()

    def __split(self, s, charList) :
        # Internal method
        # Recursively breaks down tokens containing special characters 
        # (open/closed brackets)
        if charList == "" :
            self.source.append(s)
        else :
            term = charList[0]
            if len(charList) > 1 :
                charList = charList[1:]
            else :
                charList = ""
            i=s.find(term)
            while i >= 0 and len(s) > 1 :
                if i > 0 :
                    self.__split(s[:i], charList)
                    s = s[i:]
                if len(s) > 1 :
                    self.source.append(term)
                    s = s[1:]
                i=s.find(term)
            self.__split(s, charList)

    def __clearFields(self) :
        # Set initial status of fields, in order to start schedule scanning
        self.pointer = 0
        self.iteration = 0
        self.iterations = 0
        self.stack = []
        self.error = False
        self.alternative = False
        self.repeating = False
        self.test = False
        self.condition = True
        self.ifStart = False
        self.endAlternative = 0
        self.currentItem = self.__getNextItem()
        self.looping = False

    def __getNextItem(self) :
        # Get next item in the schedule (internal method)
        self.firstCall = True
        newItem = ADscheduleItem()
        # Are we at the beginning of a $IF?
        if self.ifStart :
            # Skip commands until condition becomes True
            count = 0
            while not self.condition :
                if self.pointer >= len(self.source) :
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "$IF not closed by $END"
                    return newItem
                sl = self.source[self.pointer]
                s = sl.upper()
                self.pointer += 1
                if s.startswith("$IF") :
                    # Nested IF
                    count += 1
                elif s == "$END" :
                    # Decrease number of nested IFs
                    count -= 1
                    if count < 0 :
                        # End of main IF reached
                        self.pop()
                        self.condition = True
                elif s == "$ELSE" :
                    if count == 0 :
                        # ELSE of main IF reached
                        self.condition = True
            self.ifStart = False
        # End of schedule?
        if self.pointer >= len(self.source) :
            return newItem
        # No, get next token?
        sl = self.source[self.pointer]
        s = sl.upper()
        self.pointer += 1
        if s.endswith("(") :
            # Start of repetition
            self.push()
            self.test = False
            self.alternative = False
            self.repeating = True
            self.iteration = 0
            if len(s) > 1 :
                s = s[:len(s)-1]
                try :
                    self.iterations = int(s)
                except :
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Wrong value \"" + sl +"\""
                    return newItem
            return self.__getNextItem()
        if s == ")" :
            # End of repetition
            if not self.repeating :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unbalanced close bracket \")\""
                self.looping = True
                return newItem
            # Should we iterate?
            self.iteration += 1
            # Is this an endless loop?
            self.looping = self.iterations == 0
            # Should we repeat?
            if self.looping or self.iteration < self.iterations :
                # Repeat again
                self.pointer = self.stack[len(self.stack)-1]
                if self.looping :
                    self.iteration = 0
                return self.__getNextItem()
            # Repetition completed
            self.pop()
            return self.__getNextItem()
        if s == "[" :
            # Start of alternative
            if self.alternative :
                newItem.action = ADschedule.ERROR
                newItem.message = "Nested square brackets \"[\" are not supported!"
                self.looping = True
                return newItem
            self.push()
            self.alternative = True
            self.test = False
            self.repeating = False
            return self.__getNextItem()
        if s == "]" :
            # End of alternative
            if not self.alternative :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unbalanced close bracket \"]\""
                return newItem
            self.endAlternative = self.pointer
            self.pointer = self.stack[len(self.stack)-1]
            newItem.action = ADschedule.END_ALTERNATIVE
            return newItem
        # Test for $ prefixed commands
        if s.startswith("$IF") :
           # Start of test
            self.push()
            self.test = True
            self.alternative = False
            self.repeating = False
            i=s.find(":")
            if s.startswith("$IFH") :
                newItem.action = ADschedule.IFH
                if s == "$IFH" :
                    newItem.value = None
                    self.condition = True
                    self.ifStart = True
                    return newItem
                if i < 0 :
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Wrong format \"" + sl +"\""
                    return newItem
                self.__getSignal(sl, newItem)
                if newItem.action == ADschedule.ERROR :
                    self.error = True
                else :
                    self.condition = True
                    self.ifStart = True
                return newItem
            if i < 0 :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Wrong format \"" + sl +"\""
                return newItem
            newItem.value = self.__getArgs(sl[i+1:])
            if len(newItem.value) == 0 :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Wrong/missing section name \"" + sl +"\""
                return newItem
            if s.startswith("$IFAT:") :
                newItem.action = ADschedule.IFAT
                self.condition = True
                self.ifStart = True
                return newItem
            elif s.startswith("$IFE:") :
                newItem.action = ADschedule.IFE
                self.condition = True
                self.ifStart = True
                return newItem
            else :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown command \"" + sl +"\""
                return newItem
        if s == "$ELSE" :
            # Third term of test
            if not self.test :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "$ELSE not preceded by $IF"
                return newItem
            self.condition = False
            # Skip tokens untile $END is found
            count = 0
            while not self.condition :
                if self.pointer >= len(self.source) :
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "$IF not closed by $END"
                    return newItem
                sl = self.source[self.pointer]
                s = sl.upper()
                self.pointer += 1
                if s.startswith("$IF") :
                    # Nested IF
                    count += 1
                elif s == "$END" :
                    # Decrease number of nested IFs
                    count -= 1
                    if count < 0 :
                        # End of main IF reached
                        self.pop()
                        self.condition = True
                elif s == "$ELSE" :
                    if count == 0 :
                        # Too many ELSE
                        self.error = True
                        newItem.action = ADschedule.ERROR
                        newItem.message = "$ELSE not preceded by $IF"
                        return newItem
            return self.__getNextItem()
        if s == "$END" :
            # End of test
            if not self.test :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "$END not preceded by $IF"
                return newItem
            self.pop()
            return self.__getNextItem()
        # $Pn - pause n seconds.  $Dn delay n seconds
        if s.startswith("$P") or s.startswith("$D") :
            st = s
            try :
                st = s[2:]
            except :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Missing value \"" + sl + "\""
                return newItem
            if st.startswith("M") :
                try :
                    st = st[1:]
                except :
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Missing value \"" + sl + "\""
                    return newItem
                useFastClock = True        
            else :
                useFastClock = False        
            try :
                newItem.value = float(st)
            except :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Wrong value \"" + sl + "\""
                return newItem
            if useFastClock :
                newItem.value = (newItem.value * 60.
                  / AutoDispatcher.fastBase.getRate())
            if s.startswith("$P") :         
                newItem.action = ADschedule.PAUSE
            else :
                newItem.action = ADschedule.DELAY
            return newItem
        # Direction change
        if (s == "$CCW" or s == "$EAST" or s == "$NORTH" or s == "$LEFT"
           or s == "$UP") :
            newItem.action = ADschedule.CCW
            return newItem
        if (s == "$CW" or s == "$WEST" or s == "$SOUTH" or s == "RIGHT"
           or s == "DOWN") :
            newItem.action = ADschedule.CW
            return newItem
        # Switching mode
        # Allows train to enter restricted tracks
        # i.e. ONE-WAY and TRANSIT-ONLY
        if s == "$SWON" :
            newItem.action = ADschedule.SWON
            return newItem
        if s == "$SWOFF" :
            newItem.action = ADschedule.SWOFF
            return newItem
        # Signals control
        newItem.action = ADschedule.ERROR
        if s.startswith("$H:") :
            # $H:signalName sets a signal to "Held" state
            newItem.action = ADschedule.HELD
            self.__getSignal(sl, newItem)
            self.error = newItem.action == ADschedule.ERROR
            return newItem
        elif s.startswith("$R:") :
            # $R:signalName removes the "Held" state
            newItem.action = ADschedule.RELEASE
            self.__getSignal(sl, newItem)
            self.error = newItem.action == ADschedule.ERROR
            return newItem
        # Decoder functions (F0-F28)
        if s.startswith("$ON:F") :
            newItem.action = ADschedule.SET_F_ON
            newItem.value = s[5:]
        elif s.startswith("$OFF:F") :
            newItem.action = ADschedule.SET_F_OFF
            newItem.value = s[6:]
        if newItem.action != ADschedule.ERROR :
            # Retrieve $ON $OFF argument (function number)
            try :
                newItem.value = int(newItem.value)
                if newItem.value < 0 or newItem.value > 28 :
                    self.error = True
                    newItem.action = ADschedule.ERROR
                    newItem.message = "Function number out of range \"" + sl + "\""
            except :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Wrong/missing function number \"" + sl + "\""
            return newItem
        # Wait for empty section
        if s.startswith("$WF:") :
            st = sl
            try :
                st = sl[4:]
            except :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Missing section name \"" + sl + "\""
                return newItem
            newItem.value = ADsection.getByName(st)
            if newItem.value == None :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown section \"" + sl + "\""
                return newItem
            newItem.action = ADschedule.WAIT_FOR
            return newItem
        # Set present section to manual mode
        if s == "$M" :
            newItem.action = ADschedule.MANUAL_PRESENT
            return newItem
        # Set another section to manual mode
        if s.startswith("$M:") :
            try :
                newItem.value = ADsection.getByName(sl[3:])
            except :
                newItem.value = None
            if newItem.value == None :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown/missing section \"" + sl + "\""
                self.looping = True
                return newItem
            newItem.action = ADschedule.MANUAL_OTHER
            return newItem
        if s.startswith("$S:") :
        # Play sound
            try :
                newItem.value = ADsettings.soundDic.get(sl[3:], None)
            except :
                newItem.value = None
            if newItem.value == None :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown/missing sound \"" + sl + "\""
                return newItem
            newItem.action = ADschedule.SOUND
            return newItem
        # Set turnout
        if s.startswith("$TC:") or s.startswith("$TT:") :
            try :
                newItem.value = sl[4:]
            except :
                newItem.value = None
            if newItem.value == None or newItem.value == "" :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Missing turnout name \"" + sl + "\""
                return newItem
            if s.startswith("$TC:") :
                newItem.action = ADschedule.TC
            else :
                newItem.action = ADschedule.TT
            return newItem
        # Start time (using Fast Clock)
        if s.startswith("$ST:") :
            try:
                minutes = hours = 0
                time = sl[4:]
                i=time.find(":")
                if i < 0 :
                    hours = int(time)
                else :
                    hours = time[0:i]
                    hours = int (hours)
                    minutes = time[i+1:]
                    minutes = int (minutes)
                newItem.value = hours * 60 + minutes
            except :
                self.error = True
                newItem.action = ADschedule.ERROR
                newItem.message = "Wrong time value \"" + sl + "\""
                return newItem
            newItem.action = ADschedule.START_AT
            return newItem
        #If no command prefixed by $ was found
        # argument should be a section name
        newItem.value = ADsection.getByName(sl)
        if newItem.value == None :
            self.error = True
            newItem.action = ADschedule.ERROR
            if sl.startswith("$") :
                newItem.message = "Unknown command \"" + sl + "\""
            else :
                newItem.message = "Unknown section \"" + sl + "\""
            return newItem
        newItem.action = ADschedule.GOTO
        return newItem

    def __getArgs(self, arg) :
        # Get arguments for commands expecting a section name or
        # a list of section names
        argList = []
        if arg != "" :
            # Argument is a single section name
            arg = ADsection.getByName(arg)
            if arg != None :
                argList.append(arg)
        else :
            # Argument is a list of section names
            newItem = self.__getNextItem()
            while newItem.action == ADschedule.GOTO :
                argList.append(newItem.value)
                newItem = self.__getNextItem()
            self.next()
            self.pointer -=1
            self.alternative = False
        return argList

    def __getSignal(self, arg, newItem) :
        # Get argument for commands expecting a signal name
            i=arg.find(":")
            try :
                signalName = arg[i+1:]
            except :
                newItem.action = ADschedule.ERROR
                newItem.message = "Missing signal name \"" + arg + "\""
                return
            # retrieve signal
            newItem.value = ADsignalMast.getByName(signalName)
            if newItem.value == None :
                newItem.action = ADschedule.ERROR
                newItem.message = "Unknown signal \"" + arg + "\""
            return

    def getNextAlternative(self) :
        # Loop among alternative destinations
        # i.e. list of destinations enclosed in square brackets "[...]"
        outItem = self.currentItem
        if self.error :
            return outItem
        if self.alternative :
            self.currentItem = self.__getNextItem()
            if self.error :
                return self.currentItem
        elif (not self.firstCall) and self.currentItem.action == ADschedule.GOTO :
            outItem = ADscheduleItem()
            outItem.action = ADschedule.END_ALTERNATIVE
            outItem.value = 0
        self.firstCall = not self.firstCall
        return outItem
    
    def next(self) :
        # step to next item (or next list of alternatives)
        if self.error :
            self.currentItem.action = ADschedule.ERROR
            return      
        if self.alternative :
            self.pointer = self.endAlternative
            self.pop()
        self.currentItem = self.__getNextItem()
        
    def getFirstAlternative(self) :
        # Get the first alternative destination
        # i.e. first destination enclosed in square brackets "[x...]"
        if self.error:
            return self.currentItem
        if self.alternative :
            while self.currentItem.action == ADschedule.GOTO :
                self.currentItem = self.__getNextItem()
                if self.error:
                    return self.currentItem
            self.currentItem = self.__getNextItem()
        else :
            self.firstCall = True
        return self.getNextAlternative()
        
    def testCondition(self, item, section, direction) :
        # Set test results for $IFAT and $IFE
        condition = False
        if item.action == ADschedule.IFAT :
            # Test for current section
            for arg in item.value :
                if arg == section :
                    condition = True
                    break
        elif item.action == ADschedule.IFE :
            # Test for empty section
            for arg in item.value :
                if arg.isAvailable() :
                    condition = True
                    break
        # Set test results for $IFH
        elif item.action == ADschedule.IFH :
            signal = item.value
            if signal == None :
                signal = section.getSignal(direction)
            condition = signal.isHeld()
        else :
            # No $IF command - return present item
            return item
        # Make sure that we are at the beginning of a test
        if self.ifStart :
            # Apply test results
            self.condition = condition

        # Return next schedule item
        item.action = ADschedule.END_ALTERNATIVE
#        return self.getFirstAlternative()
        return item

    def push(self) :
        # Internal method - pushes status into the internal stack
        self.stack.append(self.ifStart)
        self.stack.append(self.condition)
        self.stack.append(self.test)
        self.stack.append(self.endAlternative)
        self.stack.append(self.alternative)
        self.stack.append(self.repeating)
        self.stack.append(self.iterations)
        self.stack.append(self.iteration)
        self.stack.append(self.pointer)

    def pop(self) :
        # Internal method - pops status from the internal stack
        # skip pointer (must be restored manually, if needed)
        self.stack.pop()    
        self.iteration = self.stack.pop()
        self.iterations = self.stack.pop()
        self.repeating = self.stack.pop()
        self.alternative = self.stack.pop()
        self.endAlternative = self.stack.pop()
        self.test = self.stack.pop()
        self.condition = self.stack.pop()
        self.ifStart = self.stack.pop()
        
    def match(self, section, direction) :
        # Try to find the first occurence of a section in the schedule
        self.__clearFields()
        minDistance = 0
        while True :
            if self.looping :
                break
            item = self.getFirstAlternative()
            item = self.testCondition(item, section, direction)
            while item.action == ADschedule.GOTO :
                if item.value == section :
                    # Move to next destination
                    self.next()
                    return
                # Try and find a route to present destination
                route = ADautoRoute(section, item.value, direction, False)
                # Take note of route length
                routeLength = len(route.step)
                if (routeLength > 0 and (minDistance == 0 or 
                   routeLength < minDistance)) :
                    minDistance = routeLength
                item = self.getNextAlternative()
            if item.action == ADschedule.ERROR :
                self.__clearFields()
                self.currentItem.action = ADschedule.ERROR
                self.currentItem.message = item.message
                self.error = True
                return
            if item.action == ADschedule.STOP :
                break
            self.next()
        # No explicit reference to section found
        self.__clearFields()
        # Did we find at least one route?
        if minDistance == 0 :
            return
        # At least one route found - synchronize with it
        while True :
            if self.looping :
                # Should not occur
                break
            item = self.getFirstAlternative()
            while item.action  == ADschedule.GOTO :
                # Get the route to present destination
                route = ADautoRoute(section, item.value, direction, False)
                # Has it the required length?
                if len(route.step) == minDistance :
                    return
                item = self.getNextAlternative()
            if item.action == ADschedule.ERROR :
                # Should not occur
                self.__clearFields()
                self.currentItem.action = ADschedule.ERROR
                self.error = True
                return
            if item.action == ADschedule.STOP :
                # Should not occur
                break
            self.next()
        # Just in case (we should never get here)
        self.__clearFields()
        

class ADscheduleItem :
    # Encapsulates info relevant to a schedule item
    def __init__(self) :
        self.action = ADschedule.STOP
        self.value = 0
        self.message = ""
        
class ADsound :
    # Encapsulates info relevant to a JMRI Sound
    def __init__(self, name) :
        self.name = name
        self.path = ""
        self.sound = None
        
    def setPath(self, path) :
        self.path = path
        if self.path.strip() != "" :
            try :
                self.sound = Sound(path)
            except :
                self.sound = None
        
    def play(self) :
        if self.sound != None :
            self.sound.play()
    

class ADpowerMonitor (PropertyChangeListener) :
    # Monitors power on layout
    # Or simulates it, if the simulator used (e.g. XpressNet) is not 
    # supporting powerManager
    
    # STATIC VARIABLES

    powerOn = True
    savePause = False
    powerOffTime = -1L
    
    def __init__(self):
        self.powerManager = InstanceManager.getDefault(jmri.PowerManager)
        if self.powerManager != None and not AutoDispatcher.lenzSimulation :
            ADpowerMonitor.powerOn = (self.powerManager.getPower()
              == PowerManager.ON)
            self.powerManager.addPropertyChangeListener(self)
    
    def propertyChange(self, ev) :
        value = self.powerManager.getPower()
        newStatus = ADpowerMonitor.powerOn
        if value == PowerManager.ON :
            newStatus = True
        elif value == PowerManager.OFF :
            newStatus = False
        if newStatus == ADpowerMonitor.powerOn :
            return
        ADpowerMonitor.powerOn = newStatus
        if ADpowerMonitor.powerOn :
            self.resetAccessories()
            if ADpowerMonitor.savePause and ADmainMenu.resumeButton.enabled :
                AutoDispatcher.instance.resume()
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Power On")
        else :
            ADpowerMonitor.powerOffTime = System.currentTimeMillis()
            ADpowerMonitor.savePause = ADmainMenu.pauseButton.enabled
            if ADpowerMonitor.savePause :
                AutoDispatcher.instance.stopAll()
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Power Off")

    def __del__(self) :
        self.dispose()

    def dispose(self) :
        if self.powerManager != None and not AutoDispatcher.lenzSimulation :
            self.powerManager.removePropertyChangeListener(self)
            
    def setPower(self, power) :
        ADpowerMonitor.powerOn = power == PowerManager.ON
        if self.powerManager != None :
            self.powerManager.setPower(power)
        if not ADsettings.separateTurnouts or not ADsettings.separateSignals :
            sleep(1.0)
            self.resetAccessories()

    def resetAccessories(self) :
        if ADpowerMonitor.powerOffTime == -1L :
            return
        if not ADsettings.separateTurnouts :
            checkTime = ADsettings.turnoutDelay * 2
            if checkTime < 3000 :
                checkTime = 3000
            checkTime =  ADpowerMonitor.powerOffTime - checkTime
            for turnout in AutoDispatcher.turnoutCommands.keys() :
                packet = AutoDispatcher.turnoutCommands[turnout]
                if packet[1] > checkTime :
                    AutoDispatcher.turnoutCommands[turnout] = [packet[0],
                      System.currentTimeMillis()]
                    turnout.setState(packet[0])
                    # Wait if user specified a delay between turnout operation
                    if ADsettings.turnoutDelay > 0 :
                        sleep(float(ADsettings.turnoutDelay)/1000.)
        if not ADsettings.separateSignals :
            checkTime = ADsettings.signalDelay * 2
            if checkTime < 3000 :
                checkTime = 3000
            checkTime =  ADpowerMonitor.powerOffTime - checkTime
            for signal in AutoDispatcher.signalCommands.keys() :
                packet = AutoDispatcher.signalCommands[signal]
                if packet[1] > checkTime :
                    AutoDispatcher.signalCommands[signal] = [packet[0],
                      System.currentTimeMillis()]
                    signal.setAppearance(packet[0])
                    # Wait if user specified a delay between signal operation
                    if ADsettings.signalDelay > 0 :
                        sleep(float(ADsettings.signalDelay)/1000.)

# USER INTERFACE CLASSES ============== Long and boring :-)

    # Main window =================
    
class ADmainMenu (JmriJFrame) :

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

    def __init__(self) :
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

#        temppane.add(JLabel(""))
        
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

        if AutoDispatcher.simulation :

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
    def whenDirectionClicked(self,event) :
        if AutoDispatcher.directionFrame == None :
            AutoDispatcher.directionFrame = ADdirectionFrame()
        else :
            AutoDispatcher.directionFrame.show()

    # define what Panel button does when clicked
    def whenPanelClicked(self,event) :
        if AutoDispatcher.panelFrame == None :
            AutoDispatcher.panelFrame = ADpanelFrame()
        else :
            AutoDispatcher.panelFrame.show()
        
    # define what Speeds button does when clicked
    def whenSpeedsClicked(self,event) :
        if AutoDispatcher.speedsFrame == None :
            AutoDispatcher.speedsFrame = ADspeedsFrame()
        else :
            AutoDispatcher.speedsFrame.show()
        
    # define what Indications button does when clicked
    def whenIndicationsClicked(self,event) :
        if AutoDispatcher.indicationsFrame == None :
            AutoDispatcher.indicationsFrame = ADindicationsFrame()
        else :
            AutoDispatcher.indicationsFrame.show()
        
    # define what Signal Types button does when clicked
    def whenSignalTypesClicked(self,event) :
        if AutoDispatcher.signalTypesFrame == None :
            AutoDispatcher.signalTypesFrame = ADsignalTypesFrame()
        else :
            AutoDispatcher.signalTypesFrame.show()
        return

    # define what Signal Masts button does when clicked
    def whenSignalMastsClicked(self,event) :
        if AutoDispatcher.signalMastsFrame == None :
            AutoDispatcher.signalMastsFrame = ADsignalMastsFrame()
        else :
            AutoDispatcher.signalMastsFrame.show()
        return

    # define what Sections button does when clicked
    def whenSectionsClicked(self,event) :
        if AutoDispatcher.sectionsFrame == None :
            AutoDispatcher.sectionsFrame = ADsectionsFrame()
        else :
            AutoDispatcher.sectionsFrame.show()
 
    # define what Blocks button does when clicked
    def whenBlocksClicked(self,event) :
        if AutoDispatcher.blocksFrame == None :
            AutoDispatcher.blocksFrame = ADblocksFrame()
        else :
            AutoDispatcher.blocksFrame.show()

    # define what Locations button does when clicked
    def whenLocationsClicked(self,event) :
        if AutoDispatcher.locationsFrame == None :
            AutoDispatcher.locationsFrame = ADlocationsFrame()
        else :
            AutoDispatcher.locationsFrame.show()

    # define what Preferences button does when clicked
    def whenPreferencesClicked(self,event) :
        if AutoDispatcher.preferencesFrame == None :
            AutoDispatcher.preferencesFrame = ADpreferencesFrame()
        else :
            AutoDispatcher.preferencesFrame.show()

    # define what Save Settings button does when clicked
    def whenSaveSettingsClicked(self,event) :
        # Save to disk
        AutoDispatcher.instance.saveSettings()
        return

    # define what Sound List button does when clicked
    def whenSoundListClicked(self,event) :
        if AutoDispatcher.soundListFrame == None :
            AutoDispatcher.soundListFrame = ADsoundListFrame()

    # define what Sound Default button does when clicked
    def whenSoundDefaultClicked(self,event) :
        if AutoDispatcher.soundDefaultFrame == None :
            AutoDispatcher.soundDefaultFrame = ADsoundDefaultFrame()

    # Operations buttons

    # define what Start button does when clicked
    def whenStartClicked(self,event) :
        # leave the button off
        ADmainMenu.startButton.enabled = False
        AutoDispatcher.instance.start()

    # define what Stop button does when clicked
    def whenStopClicked(self,event) :
        AutoDispatcher.loop = False
        # leave the button off
        ADmainMenu.stopButton.enabled = False
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Stopping trains. Please wait!")

    # define what Locomotives button does when clicked
    def whenLocosClicked(self,event) :
        if AutoDispatcher.locosFrame == None :
            AutoDispatcher.locosFrame = ADlocosFrame()
        else :
            AutoDispatcher.locosFrame.show()

    # define what Trains button does when clicked
    def whenTrainsClicked(self,event) :
        if AutoDispatcher.trainsFrame == None :
            AutoDispatcher.trainsFrame = ADtrainsFrame()
        else :
            AutoDispatcher.trainsFrame.show()

    # define what Save Trains button does when clicked
    def whenSaveTrainsClicked(self,event) :
       AutoDispatcher.instance.saveTrains()
       return

    # Emergency buttons

    # define what Pause button does when clicked
    def whenPauseClicked(self,event) :
        if ADsettings.pauseMode != ADsettings.IGNORE :
            AutoDispatcher.instance.stopAll()
            AutoDispatcher.log("Script paused!")

    # define what Resume button does when clicked
    def whenResumeClicked(self,event) :
        if (ADsettings.pauseMode == ADsettings.IGNORE or
           not AutoDispatcher.paused) :
            return
        AutoDispatcher.instance.resume()
        AutoDispatcher.log("Script resumed!")

    # Simulation buttons

    # define what Step button does when clicked
    def whenStepClicked(self,event) :
        AutoDispatcher.instance.oneStep()

    def enableButtons(self, on) :
        if not AutoDispatcher.error :
            ADmainMenu.startButton.enabled = on
            ADmainMenu.stopButton.enabled = not on
            if ADsettings.pauseMode != ADsettings.IGNORE :
                ADmainMenu.pauseButton.enabled = not on
            # Enable/disable simulation buttons
            if AutoDispatcher.simulation :
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

class ADcloseWindow(WindowAdapter) :

    # define what window closure does
    # (overrides empty method of WindowAdapter)
    def windowClosing(self,event) :
        # Close window
        event.getWindow().dispose()
        # Then stop everything,
        # otherwise the script will continue running
        # until JMRI exits and user will be unable to
        # stop it!
        # Close all other windows
        AdFrame.disposeAll()
        # Make sure ADpowerMonitor listener is removed
        if AutoDispatcher.powerMonitor != None :
            AutoDispatcher.powerMonitor.dispose()
        # Stop background handler (it will take care of stopping other
        # threads, etc.)
        if AutoDispatcher.loop :
            AutoDispatcher.exiting = True
            AutoDispatcher.loop = False
        else :
        # If background handler si not running, allow user to save data
            AutoDispatcher.instance.saveBeforeExit()


    # Our Abstract frame class =================

class AdFrame (JmriJFrame) :
    
    framesList = []
    applyEnabled = True

    def enableApply(on) :
        AdFrame.applyEnabled = on
        for f in AdFrame.framesList :
            f.applyButton.enabled = on
    enableApply = ADstaticMethod(enableApply)
    
    def disposeAll() :
        while len(AdFrame.framesList) > 0 :
            AdFrame.framesList[0].dispose()
    disposeAll = ADstaticMethod(disposeAll)
    
    def __init__(self, title) :
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
    
    def dispose(self) :
        JmriJFrame.dispose(self)
        AdFrame.framesList.remove(self)
        
    # Direction window =================
    
class ADdirectionFrame (AdFrame) :

    directionsSwing = JComboBox(["CCW-CW", "EAST-WEST", "NORTH-SOUTH",
      "LEFT-RIGHT", "UP-DOWN"])
    selectedDirectionNames = ""
    
    def __init__(self) :
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
    def displayDirectionQuestion(self) :
        ADdirectionFrame.selectedDirectionNames = ( 
          ADdirectionFrame.directionsSwing.getSelectedItem())
        selected = ADdirectionFrame.selectedDirectionNames[
          :ADdirectionFrame.selectedDirectionNames.find("-")]
        self.directionQuestion.setText(" of a short \"" + selected
          + "\" bound route:")

    # Buttons of Direction window =================
    
    # define what Cancel button in Direction Window does when clicked
    def whenCancelClicked(self,event) :
        AdFrame.dispose(self)
        AutoDispatcher.directionFrame = None

    # define what Apply button in Direction Window does when clicked
    def whenApplyClicked(self,event) :
        selected = ADdirectionFrame.selectedDirectionNames[
          :ADdirectionFrame.selectedDirectionNames.find("-")]
        if selected != ADsettings.directionNames[0] :
            ADsettings.directionNames = [selected,
             ADdirectionFrame.selectedDirectionNames[
             ADdirectionFrame.selectedDirectionNames.find("-")+1:]]
        ADsettings.ccwStart = self.ccwStartSwing.text
        ADsettings.ccwEnd = self.ccwEndSwing.text
        # Recompute direction along sections
        completion = AutoDispatcher.instance.setDirections()
        self.ccwStartSwing.text = ADsettings.ccwStart
        self.ccwEndSwing.text = ADsettings.ccwEnd
        # Update train directions
        for t in ADtrain.getList() :
            t.updateSwing()
        # Force redisplay of other windows with appropriate direction names
        if AutoDispatcher.panelFrame != None :
            AutoDispatcher.panelFrame.setColorLabels()
        if AutoDispatcher.sectionsFrame != None :
            AutoDispatcher.sectionsFrame.reDisplay()
        if AutoDispatcher.blocksFrame != None :
            AutoDispatcher.blocksFrame.reDisplay()
        if AutoDispatcher.trainsFrame != None :
            AutoDispatcher.trainsFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        if completion :
            AutoDispatcher.instance.setSignals()
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Direction changes applied")

    # Combo listener for the Direction frame =================
       
class ADcomboListener(ActionListener) :
    # Updates direction names in Directions frame
    def actionPerformed(self, event) :
        if (ADdirectionFrame.directionsSwing.getSelectedItem()
           == ADdirectionFrame.selectedDirectionNames) :
            return
        AutoDispatcher.directionFrame.displayDirectionQuestion()

    # Panel settings window =================
    
class ADpanelFrame (AdFrame) :
    def __init__(self) :
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
        temppane2.setLayout(GridLayout(len(ADsettings.colorTable)+1, 3))
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

        for i in range(len(ADsettings.colorTable)) :
            self.colorLabels.append(JLabel(""))
            temppane2.add(self.colorLabels[i])
            self.colorSwing.append(JComboBox(colorList))
            c = ADsettings.colorTable[i]
            isCustom = c.startswith("R:")
            if isCustom :
                self.colorSwing[i].setSelectedItem("CUSTOM")
                rgbValues = [int(c[2:5]), int(c[7:10]), int(c[12:])]
            else :
                self.colorSwing[i].setSelectedItem(c)
                rgbValues = [0,0,0]
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
            for j in range(3) :
                rgbItem.append(JSpinner(SpinnerNumberModel(rgbValues[j],0,255,1)))
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
    def setColorLabels(self) :
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
        for i in range(len(self.colorLabels)) :
            self.colorLabels[i].setText(sectionType[i])

    # Get input RGB values and convert them to string
    def getRGBinput(self,i) :
        c = []
        for j in range(3) :
            c.append(self.rgb[i][j].getValue())
        return ADsettings.rgbToString(c)
       
    
    # Buttons of Panel window =================

    # define what Standard Colors button in Panel Window does when clicked
    def whenStandardColorsClicked(self,event) :
        for i in range(len(self.colorLabels)) :
            self.colorSwing[i].enabled = False
        AutoDispatcher.setPreferencesDirty()

    # define what Custom Colors button in Panel Window does when clicked
    def whenCustomColorsClicked(self,event) :
        for i in range(len(self.colorLabels)) :
            self.colorSwing[i].enabled = True
        AutoDispatcher.setPreferencesDirty()
    
    # define what Cancel button in Direction Window does when clicked
    def whenCancelClicked(self,event) :
        AdFrame.dispose(self)
        AutoDispatcher.panelFrame = None

    # define what Apply button in Panel Window does when clicked
    def whenApplyClicked(self,event) :
        for i in range(len(self.colorLabels)) :
            c = self.colorSwing[i].getSelectedItem()
            if c == "CUSTOM" :
                c = AutoDispatcher.panelFrame.getRGBinput(i)
            ADsettings.colorTable[i] = c
        ADsettings.initColors()
        ADsettings.useCustomColors = self.customColorButton.selected
        ADsettings.useCustomWidth = self.customWidthButton.selected
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Panel changes applied")

    # Listener for the color ComboBox =================       
class ADcolorListener(ActionListener) :
    def actionPerformed(self, event) :
        i = int(event.getActionCommand())
        c = AutoDispatcher.panelFrame.colorSwing[i].getSelectedItem()
        isCustom = c == "CUSTOM"
        for j in range(3) :
            AutoDispatcher.panelFrame.rgb[i][j].setEnabled(isCustom)
        if isCustom :
            c = AutoDispatcher.panelFrame.getRGBinput(i)
        AutoDispatcher.panelFrame.colorPanes[i].setBackground(ADsettings.stringToColor(c))

    # Listener for the rgb text fields =================       
class ADrgbListener(ChangeListener) :
    def __init__(self, ind) :
        self.i = ind
        
    def stateChanged(self, event) :
        c = AutoDispatcher.panelFrame.colorSwing[self.i].getSelectedItem()
        if c != "CUSTOM" :
            return
        c = AutoDispatcher.panelFrame.getRGBinput(self.i)
        p = AutoDispatcher.panelFrame.colorPanes[self.i]
        p.setBackground(ADsettings.stringToColor(c))

    # Our Abstract frame with a scroll pane =================

class AdScrollFrame (AdFrame) :
    # Subclasses must define methods:
    #   self.createHeader() (output returned in self.header JPanel)
    #   self.createDetail() (output returned in self.detail JPanel)
    #   self.createButtons()  (output returned in self.buttons JPanel)
    def __init__(self, title, firstLine) :
        # super.init
        AdFrame.__init__(self, title)
        
        # Create first Line
        if firstLine != None :
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
        
    def createScroll(self) :
        # Create header and scroll pane contents
        self.header = JPanel()
        self.createHeader()
        self.detail = JPanel()
        self.createDetail()

        self.scrollSize = self.detail.getPreferredSize()
        if self.header != None :
            #Get panel size
            headerSize = self.header.getPreferredSize()
            # Make sure header and scrollarea have the same width
            if headerSize.width < self.scrollSize.width :
                headerSize.width = self.scrollSize.width
                self.header.setPreferredSize(headerSize)
            elif self.scrollSize.width < headerSize.width :
                self.scrollSize.width = headerSize.width
                self.detail.setPreferredSize(self.scrollSize)
            self.scrollPane.setColumnHeaderView(self.header)
            self.scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, JPanel())

        self.scrollPane.setViewportView(self.detail)
        
        # Adjust size (when updating window - adding or removing speeds)
        frameSize = self.getPreferredSize()
        if self.header != None :
            frameSize.height = self.scrollSize.height + 110
        else :
            frameSize.height = self.scrollSize.height + 95
        self.setSize(frameSize)
        self.firstTime = False

    def reDisplay(self) :
        self.createScroll()
        self.adjustSize()

    def adjustSize(self) :
        self.pack()
        frameSize = self.getPreferredSize()
        frameSize.width = self.scrollSize.width + 30
        if frameSize.width > AutoDispatcher.screenSize.width :
            frameSize.width = AutoDispatcher.screenSize.width
        self.setSize(frameSize)

    # Speeds window =================
    
class ADspeedsFrame (AdScrollFrame) :
    def __init__(self) :
        # Create and display Speeds window
        # super.init
        AdScrollFrame.__init__(self, "Speed Levels",
          AutoDispatcher.centerLabel(
          "List of supported speed levels (minimum speed listed first)"))

    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 2))
        self.header.add(AutoDispatcher.centerLabel("Name"))
        self.header.add(JLabel(""))

    def createDetail(self):
        # Fill contents of scroll area
        self.detail.setLayout(GridLayout(len(ADsettings.speedsList), 2))
        if self.firstTime :
            self.speedNamesSwing = []
            self.speedDefaultSwing = []
            self.speedGroup = ButtonGroup()
        ind = 0
        for s in ADsettings.speedsList :
            if self.firstTime :
                self.speedNamesSwing.append(JTextField(s, 20))
            self.detail.add(self.speedNamesSwing[ind])
            if ind == 0 :
                self.detail.add(JLabel(""))
            else :
                deleteButton = JButton("Delete")
                deleteButton.setActionCommand(str(ind))
                deleteButton.actionPerformed = self.whenDeleteClicked
                self.detail.add(deleteButton)
            ind += 1

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Speeds window =================

    # define what Cancel button in Speeds Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.speedsFrame = None

    # define what Add button in Speeds Window does when clicked
    def whenAddClicked(self,event) :
        ADsettings.speedsList.append("")
        self.speedNamesSwing.append(JTextField("", 20))
        radioButton = JRadioButton("Default speed")
        self.speedDefaultSwing.append(radioButton)
        self.speedGroup.add(radioButton)
        self.speedsChanged()

    # define what Delete button in Speeds Window does when clicked
    def whenDeleteClicked(self,event) :
        ind = int(event.getActionCommand())
        if (JOptionPane.showConfirmDialog(None, "Remove speed level \""
           + ADsettings.speedsList[ind] + "\"?", "Confirmation",
           JOptionPane.YES_NO_OPTION) == 1) :
            return
        ADsettings.speedsList.pop(ind)
        self.speedsChanged()

    # define what Apply button in Speeds Window does when clicked
    def whenApplyClicked(self,event) :
        ind = 0
        for s in ADsettings.speedsList :
            ADsettings.speedsList[ind] = self.speedNamesSwing[ind].text
            ind += 1 
        self.speedsChanged()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Speed names changed")

    def speedsChanged(self) :
        if AutoDispatcher.blocksFrame != None :
            AutoDispatcher.blocksFrame.reDisplay()
        if AutoDispatcher.indicationsFrame != None :
            AutoDispatcher.indicationsFrame.reDisplay()
        if AutoDispatcher.signalEditFrame != None :
            AutoDispatcher.signalEditFrame.reDisplay()
        if AutoDispatcher.locosFrame != None :
            AutoDispatcher.locosFrame.reDisplay()
        if AutoDispatcher.trainDetailFrame != None :
            AutoDispatcher.trainDetailFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()
    
    # Indications window =================
    
class ADindicationsFrame (AdScrollFrame) :
    def __init__(self) :
        # Create and display Speeds window
        # super.init
        AdScrollFrame.__init__(self, "Signal Indications",
          AutoDispatcher.centerLabel("List of supported signal indications"))

    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 5))
        header1 = JPanel()
        header1.setLayout(GridLayout(1, 2))
        self.header.add(AutoDispatcher.centerLabel("Indication"))
        header1.add(AutoDispatcher.centerLabel("Next section"))
        header1.add(AutoDispatcher.centerLabel("Turnouts ahead"))
        self.header.add(header1)
        self.header.add(AutoDispatcher.centerLabel("Next signal indication"))
        self.header.add(AutoDispatcher.centerLabel("Speed"))
        self.header.add(JLabel(""))

    def createDetail(self):
        # Fill contents of scroll area
        indicationNames = ["-"]
        for a in ADsettings.indicationsList :
            indicationNames.append(a.name)
        speedNames = []
        for s in ADsettings.speedsList :
            speedNames.append(s)
        maxSpeeds = len(speedNames)
        self.detail.setLayout(GridLayout(len(ADsettings.indicationsList), 5))
        ind = 0
        for a in ADsettings.indicationsList :
            self.detail.add(a.nameSwing)
            temppane1 = JPanel()
            temppane1.setLayout(GridLayout(1, 2))
            if ind == 0 :
                temppane1.add(AutoDispatcher.centerLabel("Occupied"))
                temppane1.add(AutoDispatcher.centerLabel("-"))
                self.detail.add(temppane1)
                self.detail.add(AutoDispatcher.centerLabel("-"))
                self.detail.add(AutoDispatcher.centerLabel("Stop"))
                self.detail.add(JLabel(""))
            elif ind == 1 :
                temppane1.add(AutoDispatcher.centerLabel("Available"))
                temppane1.add(AutoDispatcher.centerLabel("-"))
                self.detail.add(temppane1)
                self.detail.add(AutoDispatcher.centerLabel("-"))
                a.speedSwing = JComboBox(speedNames)
                if a.speed > maxSpeeds :
                    a.speedSwing.setSelectedIndex(maxSpeeds-1)
                else :
                    a.speedSwing.setSelectedIndex(a.speed-1)
                self.detail.add(a.speedSwing)
                self.detail.add(JLabel(""))
            else :
                temppane1.add(AutoDispatcher.centerLabel("Available"))
                temppane1.add(a.nextTurnoutSwing)
                self.detail.add(temppane1)
                a.nextIndicationSwing = JComboBox(indicationNames)
                a.nextIndicationSwing.setSelectedIndex(a.nextIndication+1)
                self.detail.add(a.nextIndicationSwing)
                a.speedSwing = JComboBox(speedNames)
                if a.speed > maxSpeeds :
                    a.speedSwing.setSelectedIndex(maxSpeeds-1)
                else :
                    a.speedSwing.setSelectedIndex(a.speed-1)
                self.detail.add(a.speedSwing)
                deleteButton = JButton("Delete")
                deleteButton.setActionCommand(str(ind))
                deleteButton.actionPerformed = self.whenDeleteIndicationClicked
                self.detail.add(deleteButton)
            ind += 1

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Indications window =================

    # define what Cancel button in Indications Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.indicationsFrame = None

    # define what Add button in Indications Window does when clicked
    def whenAddClicked(self,event) :
        ADsettings.indicationsList.append(ADindication("New indication", -1, -1,
          len(ADsettings.speedsList)))
        ADsignalType.adjust()
        self.indicationsChanged()

    # define what Delete button in Indications Window does when clicked
    def whenDeleteIndicationClicked(self,event) :
        ind = int(event.getActionCommand())
        if (JOptionPane.showConfirmDialog(None, "Remove signal indication \""
          + ADsettings.indicationsList[ind].name + "\"?", "Confirmation",
          JOptionPane.YES_NO_OPTION) == 1) :
            return
        ADsettings.indicationsList.pop(ind)
        for a in ADsettings.indicationsList :
            if a.nextIndication == ind :
                a.nextIndication = -1
            elif a.nextIndication > ind :
                a.nextIndication -=1
        AutoDispatcher.setPreferencesDirty()
        self.indicationsChanged()

    # define what Apply button in Indications Window does when clicked
    def whenApplyClicked(self,event) :
        ind = 0
        for a in ADsettings.indicationsList :
            a.name = a.nameSwing.text
            if ind > 0 :
                a.speed = a.speedSwing.getSelectedIndex() + 1
                if ind > 1 :
                    a.nextIndication = a.nextIndicationSwing.getSelectedIndex()-1
                    a.nextTurnout = a.nextTurnoutSwing.getSelectedIndex()-1
            ind += 1 
        self.indicationsChanged()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Signal indications changed")

    def indicationsChanged(self) :
        if AutoDispatcher.signalEditFrame != None :
            AutoDispatcher.signalEditFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()

    # Signal Types window =================
    
class ADsignalTypesFrame (AdScrollFrame) :
    def __init__(self) :
        # Create and display Speeds window
        # super.init
        AdScrollFrame.__init__(self, "Signal Types",
          AutoDispatcher.centerLabel("List of supported signal types"))

    def createHeader(self):
        # Fill contents of Header
        self.header = None
    
    def createDetail(self):
        # Fill contents of scroll area
        self.detail.setLayout(GridLayout(len(ADsettings.signalTypes), 2))
        ind = 0
        for s in ADsettings.signalTypes :
            self.detail.add(JLabel(s.name))
            temppane1 = JPanel()
            temppane1.setLayout(GridLayout(1, 4))
            if s.headsNumber == 1 :
                temppane1.add(AutoDispatcher.centerLabel("1 Head"))
            else :
                temppane1.add(AutoDispatcher.centerLabel(str(s.headsNumber)
                  + " Heads"))
            editButton = JButton("Edit")
            editButton.setActionCommand(str(ind))
            editButton.actionPerformed = self.whenEditClicked
            temppane1.add(editButton)
            duplicateButton = JButton("Duplicate")
            duplicateButton.setActionCommand(str(ind))
            duplicateButton.actionPerformed = self.whenDuplicateClicked
            temppane1.add(duplicateButton)
            if ind == 0 :
                temppane1.add(JLabel(""))
            else :
                if s.inUse > 0 :
                    temppane1.add(AutoDispatcher.centerLabel("In use"))
                else :
                    deleteButton = JButton("Delete")
                    deleteButton.setActionCommand(str(ind))
                    deleteButton.actionPerformed = self.whenDeleteClicked
                    temppane1.add(deleteButton)
            self.detail.add(temppane1)
            ind += 1

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

    # Buttons of Signal Types window =================

    # define what Cancel button in Signal Types Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.signalTypesFrame = None
            if AutoDispatcher.signalEditFrame != None :
                AutoDispatcher.signalEditFrame.dispose()
                AutoDispatcher.signalEditFrame = None
                        
    # define what Edit button in Signal Types Window does when clicked
    def whenEditClicked(self,event) :
        ind = int(event.getActionCommand())
        if AutoDispatcher.signalEditFrame != None :
            AutoDispatcher.signalEditFrame.show()
            if (ADsettings.signalTypes[ind]
               == AutoDispatcher.signalEditFrame.editSignal) :
                return
            if (JOptionPane.showConfirmDialog(None,
               "Save changes to signal type \""
               + AutoDispatcher.signalEditFrame.editSignal.name
               + "\" before editing signal type \""
               + ADsettings.signalTypes[ind].name
               + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION) != 1) :
                    AutoDispatcher.signalEditFrame.whenApplyClicked(None)
            AutoDispatcher.signalEditFrame.dispose()
        AutoDispatcher.signalEditFrame = (
          ADsignalEditFrame(ADsettings.signalTypes[ind]))

    # define what Add button in Signal Types Window does when clicked
    def whenAddClicked(self,event) :
        ADsettings.signalTypes.append(ADsignalType(
          "New signal type", [], []))
        self.signalTypesChanged()

    # define what Delete button in Signal Types Window does when clicked
    def whenDeleteClicked(self,event) :
        ind = int(event.getActionCommand())
        if (JOptionPane.showConfirmDialog(None, "Remove signal type \""
           + ADsettings.signalTypes[ind].name
           + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION) == 1) :
            return
        ADsettings.signalTypes.pop(ind)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Signal type \"" + ADsettings.signalTypes[ind].name + " removed")
        self.signalTypesChanged()

    # define what Duplicate button in Signal Types Window does when clicked
    def whenDuplicateClicked(self,event) :
        ind = int(event.getActionCommand())
        ADsettings.signalTypes.append(
          ADsignalType(ADsettings.signalTypes[ind].name + 
          " copy", ADsettings.signalTypes[ind].aspects,
          ADsettings.signalTypes[ind].speeds))
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Signal type \"" + ADsettings.signalTypes[ind].name + " duplicated")
        self.signalTypesChanged()

    def signalTypesChanged(self) :
        if AutoDispatcher.signalMastsFrame != None :
            AutoDispatcher.signalMastsFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()

    # Signal Edit Window =================

class ADsignalEditFrame (AdScrollFrame) :
    def __init__(self, signal) :
        # Create and display Speeds window
        self.editSignal = signal
        first = JPanel()
        first.setLayout(BoxLayout(first, BoxLayout.X_AXIS))
        first.add(AutoDispatcher.centerLabel("Name: "))
        first.add(self.editSignal.nameSwing)
        first.add(AutoDispatcher.centerLabel("Heads: "))
        self.headsSwing = JComboBox(["1", "2", "3", "4", "5"])
        self.headsSwing.setSelectedIndex(self.editSignal.headsNumber -1)
        if self.editSignal == ADsettings.signalTypes[0] :
            self.headsSwing.enabled = False
        first.add(self.headsSwing)
        # super.init
        AdScrollFrame.__init__(self, "Edit signal type", first)

    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 3 + self.editSignal.headsNumber))
        self.header.add(AutoDispatcher.centerLabel("Signal indication"))
        for i in range(self.editSignal.headsNumber) :
            self.header.add(AutoDispatcher.centerLabel("Head" + str(i+1)))
        self.header.add(AutoDispatcher.centerLabel("Speed"))
        self.header.add(AutoDispatcher.centerLabel("Override speed"))
    
    def createDetail(self):
        # Fill contents of scroll area
        self.detail.setLayout(GridLayout(self.editSignal.aspectsNumber, 3
           + self.editSignal.headsNumber))
        speedNames = ["No"]
        for s in ADsettings.speedsList :
            speedNames.append(s)
        self.signalSpeedSwing = []
        self.signalAspectsSwing = []
        ind = 0
        headsAspects = AutoDispatcher.headsAspects.keys()
        headsAspects.sort()
        for a in self.editSignal.aspects :
            if ind >= len(ADsettings.indicationsList) :
 #               self.detail.add(JLabel("Not defined"))
                break
 #           else :
            self.detail.add(JLabel(ADsettings.indicationsList[ind].name))
            aspectLine = []
            for aa in a :
                aspectSwing = JComboBox(headsAspects)
                aspectSwing.setSelectedItem(AutoDispatcher.inverseAspects[aa])
                aspectLine.append(aspectSwing)
                self.detail.add(aspectSwing)
            self.signalAspectsSwing.append(aspectLine)
            self.detail.add(AutoDispatcher.centerLabel(
              ADsettings.getSpeedName(ADsettings.indicationsList[ind].speed)))
            speedSwing = JComboBox(speedNames)
            speedIndex = self.editSignal.speeds[ind]+1
            if speedIndex >= len(speedNames) :
                speedIndex = len(speedNames) -1
            speedSwing.setSelectedIndex(speedIndex)
            if ind == 0 :
                speedSwing.enabled = False
            self.signalSpeedSwing.append(speedSwing)
            self.detail.add(speedSwing)
            ind += 1

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Signal Edit window =================

    # define what Cancel button in Signal Edit Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.signalEditFrame = None

    # define what Apply button in Signal Edit Window does when clicked
    def whenApplyClicked(self,event) :
        if self.editSignal != None :
            self.editSignal.name = self.editSignal.nameSwing.text
            for i in range(self.editSignal.aspectsNumber) :
                for j in range(self.editSignal.headsNumber) :
                    self.editSignal.aspects[i][j] = AutoDispatcher.headsAspects[
                      self.signalAspectsSwing[i][j].getSelectedItem()]
                self.editSignal.speeds[i] = self.signalSpeedSwing[
                      i].getSelectedIndex()-1
            newHeads = self.headsSwing.getSelectedIndex() + 1
            if newHeads != self.editSignal.headsNumber :
                diff = newHeads - self.editSignal.headsNumber
                if diff > 0 :
                    for i in range(diff) :
                        self.editSignal.aspects[0].append(SignalHead.RED)
                    for i in range(len(self.editSignal.aspects) -1) :
                        for j in range(diff) :
                            self.editSignal.aspects[i+1].append(
                              SignalHead.GREEN)
                else :
                    for a in self.editSignal.aspects :
                        for i in range(-diff) :
                            a.pop()
                self.editSignal.headsNumber = (
                  self.headsSwing.getSelectedIndex() + 1)
            if AutoDispatcher.signalTypesFrame != None :
                AutoDispatcher.signalTypesFrame.reDisplay()
            if AutoDispatcher.signalMastsFrame != None :
                AutoDispatcher.signalMastsFrame.reDisplay()
            AutoDispatcher.setPreferencesDirty()
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Signal type \"" + self.editSignal.name + " modified")
            self.reDisplay()

    # Signal Types window =================
    
class ADsignalMastsFrame (AdScrollFrame) :
    def __init__(self) :
        # Create and display Speeds window
        # super.init
        AdScrollFrame.__init__(self, "Signal masts", None)

    def createHeader(self):
        # Fill contents of Header
        if self.firstTime :
            names = ADsignalMast.getNames()
            names.sort()
            self.signals = []
            self.maxHeads = 1
            for name in names :
                s = ADsignalMast.getByName(name)
                # Shouldn't happen
                if s != None :
                    if s.headsNumber > self.maxHeads :
                        self.maxHeads = s.headsNumber
                    self.signals.append(s)
        self.header.setLayout(GridLayout(1, 3))
        self.header.add(AutoDispatcher.centerLabel("Signal name"))
        self.header.add(AutoDispatcher.centerLabel("Signal type"))
        header1 = JPanel()
        header1.setLayout(GridLayout(1, self.maxHeads + 1))
        for i in range(self.maxHeads) :
            header1.add(AutoDispatcher.centerLabel("Head" + str(i+1)))
        header1.add(JLabel(""))
        self.header.add(header1)
    
    def createDetail(self):
        # Fill contents of scroll area
        self.detail.setLayout(GridLayout(len(self.signals), 3))
        self.namesSwing = []
        self.typesSwing = []
        self.headsSwing = []
        types = []
        for t in ADsettings.signalTypes :
            types.append(t.name)
        ind = 0
        for s in self.signals :
            nameSwing = JTextField(s.name, 20)
            self.detail.add(nameSwing)
            self.namesSwing.append(nameSwing)
            typeSwing = JComboBox(types)
            typeSwing.setSelectedItem(s.signalType.name)
            self.detail.add(typeSwing)
            self.typesSwing.append(typeSwing)
            temppane1 = JPanel()
            temppane1.setLayout(GridLayout(1, self.maxHeads))
            i = 0
            headsLineSwing = []
            for h in s.signalHeads :
                headSwing = JComboBox(AutoDispatcher.signalHeadNames)
                if i < self.maxHeads :
                    if h != None :
                        headSwing.setSelectedItem(h.name)
                    temppane1.add(headSwing)
                    headsLineSwing.append(headSwing)
                    i += 1
            self.headsSwing.append(headsLineSwing)
            while i < self.maxHeads :
                temppane1.add(JLabel(""))
                i += 1
            if s.inUse > 0 :
                temppane1.add(AutoDispatcher.centerLabel("In use"))
            else :
                deleteButton = JButton("Delete")
                deleteButton.setActionCommand(str(ind))
                deleteButton.actionPerformed = self.whenDeleteClicked
                temppane1.add(deleteButton)
            self.detail.add(temppane1)
            ind += 1

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Signal Types window =================

    # define what Cancel button in Signal Masts Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.signalMastsFrame = None
                        
    # define what Add button in Signal Masts Window does when clicked
    def whenAddClicked(self,event) :
        newName = "New signal"
        i = 1
        while ADsignalMast.signalsList.has_key(newName) :
            i += 1
            newName = "New signal " + str(i)
        self.signals.append(ADsignalMast.provideSignal(newName))
        self.reDisplay()

    # define what Apply button in Signal Masts Window does when clicked
    def whenApplyClicked(self,event) :
        ind = 0
        for s in self.signals :
            newName = self.namesSwing[ind].text
            if newName != s.name :
                if ADsignalMast.signalsList.has_key(newName) :
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                      "Duplicate signal masts name \""
                      + newName + "\" - ignored")
                    self.namesSwing[ind].text = s.name
                else :
                    s.name = newName
            typeName = self.typesSwing[ind].getSelectedItem()
            newType = s.signalType
            for t in ADsettings.signalTypes :
                if t.name == typeName :
                    newType = t
                    break
            if newType != s.signalType :
                s.signalType.changeUse(-1)
                while newType.headsNumber > s.headsNumber :
                    s.signalHeads.append(None)
                    s.headsNumber += 1
                s.signalType = newType
                s.signalType.changeUse(1)
                if newType.headsNumber > self.maxHeads :
                    self.maxHeads = newType.headsNumber
            i = 0
            for h in self.headsSwing[ind] :
                headName = h.getSelectedItem()
                if headName == "" :
                    s.signalHeads[i] = None
                else :
                    s.signalHeads[i] = ADsignalHead(headName)
                i += 1
            ind += 1
        newDic = {}
        for s in ADsignalMast.getList() :
            newDic[s.name] = s
        ADsignalMast.signalsList = newDic           
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Signal masts updated")
        self.signalMastsChanged()

    # define what Delete button in Signal Masts Window does when clicked
    def whenDeleteClicked(self,event) :
        ind = int(event.getActionCommand())
        name = self.signals[ind].name
        if (JOptionPane.showConfirmDialog(None, "Remove signal mast \"" + name
           + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION) == 1) :
            return
        removed = self.signals.pop(ind)
        removed.changeUse(-1)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Signal mast \"" + name + " removed")
        self.signalMastsChanged()
        
    def signalMastsChanged(self) :
        if AutoDispatcher.signalTypesFrame != None :
            AutoDispatcher.signalTypesFrame.reDisplay()
        if AutoDispatcher.sectionsFrame != None :
            AutoDispatcher.sectionsFrame.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()

    # Sections Window =================
    
class ADsectionsFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Sections window
        # super.init
        AdScrollFrame.__init__(self, "Sections", None)

    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 8))
        self.header.add(AutoDispatcher.centerLabel("Section"))
        self.header.add(AutoDispatcher.centerLabel("One-Way"))
        self.header.add(AutoDispatcher.centerLabel("Transit-Only"))
        self.header.add(AutoDispatcher.centerLabel(
          ADsettings.directionNames[0] + " signal"))
        self.header.add(AutoDispatcher.centerLabel(
          ADsettings.directionNames[0] + " stop at beginning"))
        self.header.add(AutoDispatcher.centerLabel(
          ADsettings.directionNames[1] + " signal"))
        self.header.add(AutoDispatcher.centerLabel(
          ADsettings.directionNames[1] + " stop at beginning"))
        self.header.add(AutoDispatcher.centerLabel("Man. sensor"))
        
    def createDetail(self):
        # Fill contents of scroll area
        sections = ADsection.getSectionsTable()
        self.detail.setLayout(GridLayout(len(sections), 6))
        both = (ADsettings.directionNames[0] + "-"
          + ADsettings.directionNames[1])
        signalMastList = ADsignalMast.getNames()
        signalMastList.sort()
        signalPopUp = [""]
        for s in signalMastList :
            signalPopUp.append("s: " + s)
        for s in AutoDispatcher.signalHeadNames :
            if not s in signalMastList and s.strip() != "" :
                signalPopUp.append("h: " + s)        
        for s in sections :
            self.detail.add(AutoDispatcher.centerLabel(s[0]))
            ss = ADsection.getByName(s[0])
            ss.oneWaySwing.removeAllItems()
            ss.oneWaySwing.addItem("")
            ss.oneWaySwing.addItem(ADsettings.directionNames[0])
            ss.oneWaySwing.addItem(ADsettings.directionNames[1])
            self.detail.add(ss.oneWaySwing)
            ss.oneWaySwing.setSelectedItem(s[1])
            ss.transitOnlySwing.removeAllItems()
            ss.transitOnlySwing.addItem("")
            ss.transitOnlySwing.addItem(both)
            ss.transitOnlySwing.addItem(both + "+")
            ss.transitOnlySwing.addItem(ADsettings.directionNames[0])
            ss.transitOnlySwing.addItem(ADsettings.directionNames[0]
              + "+")
            ss.transitOnlySwing.addItem(ADsettings.directionNames[1])
            ss.transitOnlySwing.addItem(ADsettings.directionNames[1]
              + "+")
            self.detail.add(ss.transitOnlySwing)
            ss.transitOnlySwing.setSelectedItem(s[2])
            j = ADsettings.ccw
            for k in range(2) :
                ss.signalSwing[j] = JComboBox(signalPopUp)
                self.detail.add(ss.signalSwing[j])
                if ss.signal[j] != None :
                    ss.signalSwing[j].setSelectedItem("s: " + ss.signal[j].name)
                temppane = JPanel()
                temppane.setLayout(GridLayout(1, 2))
                if ss.stopAtBeginning[j] >= 0 :
                    ss.stopAtBeginningSwing[j].selected = True
                    ss.stopAtBeginningDelay[j].text = str(ss.stopAtBeginning[j])
                else :
                    ss.stopAtBeginningSwing[j].selected = False
                    ss.stopAtBeginningDelay[j].text = "0.0"
                temppane.add(ss.stopAtBeginningSwing[j])
                temppane.add(ss.stopAtBeginningDelay[j])
                self.detail.add(temppane)
                j = 1-j
            ss.manualSensorSwing = JComboBox(ADsection.sensorNames)
            if s[6] in ADsection.sensorNames :
                ss.manualSensorSwing.setSelectedItem(s[6])
            self.detail.add(ss.manualSensorSwing)

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Sections window =================
    
    # define what Cancel button in Sections Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.sectionsFrame = None

    # define what Apply button in Sections Window does when clicked
    def whenApplyClicked(self,event) :
        for s in ADsection.getList() :
            s.updateFromSwing()
        if AutoDispatcher.signalMastsFrame != None :
            AutoDispatcher.signalMastsFrame.reDisplay()
        ADgridGroup.create()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Sections changes applied")

    # Blocks Window =================
    
class ADblocksFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Blocks window
        # super.init
        AdScrollFrame.__init__(self, "Blocks", None)

    def createHeader(self):
        # Fill contents of Header
        # Retrieve section names
        self.sectionNames = ADsection.getNames()
        self.sectionNames.sort()
            
        # Check if the direction of any section can be reversed
        self.columns = 10
        self.canBeReversed = []
        self.inversionPoints = []
        for sectionName in self.sectionNames :
            section = ADsection.getByName(sectionName)
            canBe = False
            # Direction can be reversed only if all entry points
            # in one direction connect with sections in opposite direction
            # (i.e. the section is at the end of a reversing loop)
            invPoints = ["Inv. Points"]
            for direction in [False, True] :
                entries = section.getEntries(direction)
                if len(entries) > 0 :
                    canBe1 = True
                    invPoints1 = []
                    for entry in entries :
                        if not entry.getDirectionChange() :
                            canBe1 = False
                            break
                        invPoints1.append(entry.getExternalSection().getName())
                    if canBe1 :
                        canBe = True
                        invPoints.extend(invPoints1)
            # Keep track of the condition
            self.canBeReversed.append(canBe)
            if canBe :
                self.columns = 11
            else :
                invPoints = []
            self.inversionPoints.append(invPoints)
                
        self.header.setLayout(GridLayout(2, self.columns))
        self.header.add(AutoDispatcher.centerLabel("Section"))
        self.header.add(AutoDispatcher.centerLabel("Block"))
        for i in range(2) :
            for j in range(2) :
                header1 = JPanel()
                header1.setLayout(GridLayout(1, 2))
                for k in range(2) :
                    header1.add(AutoDispatcher.centerLabel(
                      ADsettings.directionNames[i]))
                self.header.add(header1)
            self.header.add(AutoDispatcher.centerLabel(
              ADsettings.directionNames[i]))
            self.header.add(AutoDispatcher.centerLabel(
              ADsettings.directionNames[i]))
        if self.columns > 10 :
            self.header.add(JLabel(""))
        self.header.add(JLabel(""))
        self.header.add(JLabel(""))
        for i in range(2) :
            header1 = JPanel()
            header1.setLayout(GridLayout(1, 2))
            header1.add(AutoDispatcher.centerLabel("alloc."))
            header1.add(AutoDispatcher.centerLabel("safe"))
            self.header.add(header1)
            header1 = JPanel()
            header1.setLayout(GridLayout(1, 2))
            header1.add(AutoDispatcher.centerLabel("stop"))
            header1.add(AutoDispatcher.centerLabel("brake"))
            self.header.add(header1)
            self.header.add(AutoDispatcher.centerLabel("speed"))
            self.header.add(AutoDispatcher.centerLabel("action"))

        if self.columns > 10 :
            self.header.add(AutoDispatcher.centerLabel("Reverse"))

    def createDetail(self):
        # Fill contents of scroll area
        # Compute total number of lines
        linesNumber = len(self.sectionNames) * 2
        ind = 0
        for sectionName in self.sectionNames :
            s = ADsection.getByName(sectionName)
            lines = len(s.getBlocks(True))
            if lines < len(self.inversionPoints[ind]) :
                lines = len(self.inversionPoints[ind])
            linesNumber += lines
            ind +=1
        self.detail.setLayout(GridLayout(linesNumber, self.columns))
        # Prepare list for speeds ComboBox
        speeds = [""]
        speeds.extend(ADsettings.speedsList)
        for sectionName in self.sectionNames :
            canBe = self.canBeReversed.pop(0)
            invPoints = self.inversionPoints.pop(0)
            section = ADsection.getByName(sectionName)
            for block in section.getBlocks(ADsettings.ccw) :
                self.detail.add(AutoDispatcher.centerLabel(sectionName))
                self.detail.add(AutoDispatcher.centerLabel(block.getName()))
                j = ADsettings.ccw
                for k in range(2) :
                    temppane = JPanel()
                    temppane.setLayout(GridLayout(1, 2))
                    block.allocationSwing[j].setSelected(block == (
                      section.allocationPoint[j]))
                    temppane.add(block.allocationSwing[j])
 
                    block.safeSwing[j].setSelected(block == (
                      section.safePoint[j]))
                    temppane.add(block.safeSwing[j])
                    self.detail.add(temppane)
                    
                    temppane = JPanel()
                    temppane.setLayout(GridLayout(1, 2))
                    block.stopSwing[j].setSelected(block == (
                      section.stopBlock[j]))
                    temppane.add(block.stopSwing[j])

                    block.brakeSwing[j].setSelected(block == (
                      section.brakeBlock[j]))
                    temppane.add(block.brakeSwing[j])
                    self.detail.add(temppane)

                    block.speedSwing[j] = JComboBox(speeds)
                    block.speedSwing[j].setSelectedIndex(block.getSpeed(j))
                    self.detail.add(block.speedSwing[j])
                    block.actionSwing[j].text = block.action[j]
                    self.detail.add(block.actionSwing[j])
                    
                    j = 1 - j
                if self.columns > 10 :
                    if canBe and sectionName != "" :
                        reverseButton = JButton("Reverse")
                        reverseButton.setActionCommand(sectionName)
                        reverseButton.actionPerformed = self.whenReverseClicked
                        self.detail.add(reverseButton)
                    else :
                        if len(invPoints) > 0 :
                            self.detail.add(AutoDispatcher.centerLabel(invPoints.pop(0)))
                        else :
                            self.detail.add(JLabel(""))
                sectionName = ""
            # Add a "None" line
            self.detail.add(JLabel(""))
            self.detail.add(AutoDispatcher.centerLabel("None"))
            j = ADsettings.ccw
            for k in range(2) :
                temppane = JPanel()
                temppane.setLayout(GridLayout(1, 2))
                temppane.add(JLabel(""))
                section.safeNoneSwing[j].setSelected(
                  section.safePoint[j] == None)
                temppane.add(section.safeNoneSwing[j])
                self.detail.add(temppane)
                temppane = JPanel()
                temppane.setLayout(GridLayout(1, 2))
                temppane.add(JLabel(""))
                section.brakeNoneSwing[j].setSelected(
                  section.brakeBlock[j] == None)
                temppane.add(section.brakeNoneSwing[j])
                self.detail.add(temppane)
                self.detail.add(JLabel(""))
                self.detail.add(JLabel(""))
                j = 1 - j
            if self.columns > 10 :
                if len(invPoints) > 0 :
                    self.detail.add(AutoDispatcher.centerLabel(invPoints.pop(0)))
                    while len(invPoints) > 0 :
                        for i in range(self.columns - 1) :
                            self.detail.add(JLabel(""))
                        self.detail.add(AutoDispatcher.centerLabel(invPoints.pop(0)))
                else :
                    self.detail.add(JLabel(""))
             # Add an empty line between sections
            for i in range(self.columns) :
                self.detail.add(JLabel(""))

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Blocks window =================
    
    # define what Cancel button in Blocks Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.blocksFrame = None

    # define what Reverse button in Blocks Window does when clicked
    def whenReverseClicked(self,event) :
        sectionName = event.getActionCommand()
        section = ADsection.getByName(sectionName)
        # Reverse blocks order
        section.manuallyFlip()
        # Adjust entry points
        AutoDispatcher.instance.findTransitPoints()
        self.reDisplay()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Direction of section " + sectionName + " reversed")
        return

    # define what Apply button in Blocks Window does when clicked
    def whenApplyClicked(self,event) :
        for section in ADsection.getList() :
            section.safePoint[0] = section.safePoint[1] = None
            for block in section.getBlocks(True) :
                for j in range(2) :
                    if block.allocationSwing[j].isSelected() :
                        section.allocationPoint[j] = block
                    if block.safeSwing[j].isSelected() :
                        section.safePoint[j] = block
                    if block.stopSwing[j].isSelected() :
                        section.stopBlock[j] = block
                    if block.brakeSwing[j].isSelected() :
                        section.brakeBlock[j] = block
                    block.speed[j] = block.speedSwing[j].getSelectedIndex()
                    block.action[j] = block.actionSwing[j].text
            for j in range(2) :
                if section.safeNoneSwing[j].isSelected() :
                    section.safePoint[j] = None
                if section.brakeNoneSwing[j].isSelected() :
                    section.brakeBlock[j] = None
        self.reDisplay()
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Blocks changes applied")
          
    # Locations Window =================
    
class ADlocationsFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Locations window
        # Retrieve location names
        # super.init
        AdScrollFrame.__init__(self, "Locations", JLabel("Define the list"
        + " of sections corresponding to each Operations' location"))
        frameSize = self.getMinimumSize()
        if frameSize.width < 800 :
            frameSize.width = 800
            self.setMinimumSize(frameSize)
            self.pack()
    def createHeader(self):
        # Fill contents of Header
        self.header.setLayout(GridLayout(1, 2))
        self.header.add(AutoDispatcher.centerLabel("Location"))
        self.header.add(AutoDispatcher.centerLabel("Sections"))

    def createDetail(self):
        # Fill contents of scroll area
        # Compute total number of lines
        self.locationNames = ADlocation.getNames()
        self.locationNames.sort()
        self.detail.setLayout(GridLayout(len(self.locationNames), 2))
        self.valuesSwing = []
        for locationName in self.locationNames :
            location = ADlocation.getByName(locationName)
            if location.opLocation == None :
                locationName += " (Unknown)"
            self.detail.add(JLabel(locationName))
            value = location.text
            value =  JTextField(value, 20)
            self.detail.add(value)
            self.valuesSwing.append(value)

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.applyButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.applyButton)

    # Buttons of Locations window =================
    
    # define what Cancel button in Locations Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.locationsFrame = None

    # define what Apply button in Locations Window does when clicked
    def whenApplyClicked(self,event) :
        ind = 0 
        for locationName in self.locationNames :
            location = ADlocation.getByName(locationName)
            location.setSections(self.valuesSwing[ind].text)
            ind += 1
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Locations changes applied")

    # Preferences Window =================   
   
class ADpreferencesFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Preferences window
        # super.init
        AdScrollFrame.__init__(self, "Preferences", None)

    def createHeader(self):
        self.header = None

    def createDetail(self):
        self.detail.setLayout(GridLayout(43, 2))
        self.detail.add(JLabel("COMMON SETTINGS"))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("Verbose output:"))
        self.verboseSwing = JCheckBox("", ADsettings.verbose)
        self.detail.add(self.verboseSwing)
        self.detail.add(JLabel("Ring bell for main events:"))
        self.ringBellSwing = JCheckBox("", ADsettings.ringBell)
        self.detail.add(self.ringBellSwing)
        self.detail.add(JLabel("Pause mode:"))
        self.pauseSwing = JComboBox(["Disabled", "Stop trains",
          "Emergency stop trains", "Turn power off"])
        self.pauseSwing.setSelectedIndex(ADsettings.pauseMode)
        self.detail.add(self.pauseSwing)
        self.detail.add(JLabel("Turnouts controlled by separate system: "))
        self.separateTurnoutsSwing = JCheckBox("",
          ADsettings.separateTurnouts)
        self.detail.add(self.separateTurnoutsSwing)
        self.detail.add(JLabel("Signals controlled by separate system: "))
        self.separateSignalsSwing = JCheckBox("",
          ADsettings.separateSignals)
        self.detail.add(self.separateSignalsSwing)
        self.detail.add(JLabel("Scale: "))
        self.scaleSwing = JTextField(str(ADsettings.scale), 5)
        temppane = JPanel()
        temppane.setLayout(BoxLayout(temppane, BoxLayout.X_AXIS))
        temppane.add(JLabel("1:"))
        temppane.add(self.scaleSwing)
        self.detail.add(temppane)
        self.detail.add(JLabel("Flashing cycle of signal icons (seconds):"))
        self.flashingSwing = JTextField(str(ADsettings.flashingCycle), 5)        
        self.detail.add(self.flashingSwing)
        self.detail.add(JLabel(""))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("DISPATCHER SETTINGS"))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("Derailed trains detection:"))
        self.derailedSwing = JComboBox(["Disabled", "Enabled: only warning",
          "Enabled: pause script"])
        self.derailedSwing.setSelectedIndex(ADsettings.derailDetection)
        self.detail.add(self.derailedSwing)
        self.detail.add(JLabel("Trains entering wrong route detection: "))
        self.wrongRouteSwing = JComboBox(["Disabled", "Enabled: only warning",
          "Enabled: pause script"])
        self.wrongRouteSwing.setSelectedIndex(
          ADsettings.wrongRouteDetection)
        self.detail.add(self.wrongRouteSwing)
        self.detail.add(JLabel("Stalled trains detection: "))
        self.stalledSwing = JComboBox(["Disabled", "Enabled: only warning",
          "Enabled: pause script"])
        self.stalledSwing.setSelectedIndex(ADsettings.stalledDetection)
        self.detail.add(self.stalledSwing)
        self.detail.add(JLabel(
          "Maximum time required to travel a block (seconds): "))
        self.stalledTimeSwing = JTextField(
          str(float(ADsettings.stalledTime)/1000.), 5)
        self.detail.add(self.stalledTimeSwing)
        self.detail.add(JLabel("Lost cars detection:"))
        self.lostCarsSwing = JComboBox(["Disabled", "Enabled: only warning",
          "Enabled: pause script"])
        self.lostCarsSwing.setSelectedIndex(ADsettings.lostCarsDetection)
        self.detail.add(self.lostCarsSwing)
        if ADsettings.units == 1.0 :
            self.detail.add(JLabel("Tollerance for lost cars detection (mm.):"))
        elif ADsettings.units == 10.0 :
            self.detail.add(JLabel("Tollerance for lost cars detection (cm.):"))
        else :
            self.detail.add(JLabel(
              "Tolerance for lost cars detection (inches):"))
        self.lostLengthSwing = JTextField(
          str(ADsettings.lostCarsTollerance / ADsettings.units), 4)
        self.detail.add(self.lostLengthSwing)
        self.detail.add(JLabel(
          "Maximum number of sections occupied by a train:"))
        self.lostMaxSwing = JTextField(
          str(ADsettings.lostCarsSections), 4)        
        self.detail.add(self.lostMaxSwing)        
        self.detail.add(JLabel(
          "(Most) cars are equipped with resistive wheel-sets:"))        
        self.useResistiveSwing = JCheckBox("", ADsettings.resistiveDefault)
        self.detail.add(self.useResistiveSwing)
        self.detail.add(JLabel("Train length expressed in: "))
        self.unitsSwing = JComboBox(["mm.", "cm.", "inches"])
        if ADsettings.units == 1.0 :
            self.unitsSwing.setSelectedIndex(0)
        elif ADsettings.units == 10.0 :
            self.unitsSwing.setSelectedIndex(1)
        else :
            self.unitsSwing.setSelectedIndex(2)
        self.detail.add(self.unitsSwing)
        self.detail.add(JLabel("Release sections based on train lenght: "))
        self.useLengthSwing = JCheckBox("", ADsettings.useLength)
        self.detail.add(self.useLengthSwing)
        if ADblock.blocksWithLength == 0 :
            self.useLengthSwing.setEnabled(False)
            self.detail.add(JLabel("  (Block lengths not defined!) "))
        elif ADblock.blocksWithoutLength == 0 :
            self.detail.add(JLabel("  (Length defined for all blocks)"))
        else :            
            self.detail.add(JLabel("  (Length not defined for "
              + str(ADblock.blocksWithoutLength) + " blocks!)"))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("Number of sections ahead to be allocated: "))
        self.aheadSwing = JComboBox(["1", "2", "3", "4", "5"])
        self.aheadSwing.setSelectedIndex(ADsettings.allocationAhead -1)
        self.detail.add(self.aheadSwing)
        self.detail.add(JLabel("Locomotives maintenance interval (hours): "))
        self.maintenanceSwing = JTextField(str(ADsettings.maintenanceTime), 5)
        self.detail.add(self.maintenanceSwing)
        if ADsettings.units == 25.4 :
            self.detail.add(JLabel(
              "Mileage maintenance interval (scale miles): "))
            multiplier = ADsettings.scale / 1609344.
        else :
            self.detail.add(JLabel(
              "Mileage maintenance interval (scale Km.): "))
            multiplier = ADsettings.scale / 1000000.
        self.milesSwing = JTextField(str(round(ADsettings.maintenanceMiles *
          multiplier,1)), 5)
        self.detail.add(self.milesSwing)

        self.detail.add(JLabel("Override JMRI block tracking:"))
        self.blockTrackingSwing = JCheckBox("", ADsettings.blockTracking)
        self.detail.add(self.blockTrackingSwing)
        self.detail.add(JLabel("Update JMRI sections state:"))
        self.sectionTrackingSwing = JCheckBox("",
          ADsettings.sectionTracking)
        self.detail.add(self.sectionTrackingSwing)
        self.detail.add(JLabel("Trust turnouts KnownState:"))
        self.trustTurnoutsSwing = JCheckBox("", ADsettings.trustTurnouts)
        self.detail.add(self.trustTurnoutsSwing)
        self.detail.add(JLabel("Delay between turnout commands (seconds): "))
        self.turnoutDelaySwing = JTextField(
          str(float(ADsettings.turnoutDelay)/1000.), 5)
        self.detail.add(self.turnoutDelaySwing)
        self.detail.add(JLabel("Delay before clearing signals (seconds): "))
        self.clearDelaySwing = JTextField(
          str(float(ADsettings.clearDelay)/1000.), 5)
        self.detail.add(self.clearDelaySwing)
        self.detail.add(JLabel("Trust signals KnownState:"))
        self.trustSignalsSwing = JCheckBox("", ADsettings.trustSignals)
        self.detail.add(self.trustSignalsSwing)
        self.detail.add(JLabel("Delay between signal commands (seconds): "))
        self.signalDelaySwing = JTextField(
          str(float(ADsettings.signalDelay)/1000.), 5)
        self.detail.add(self.signalDelaySwing)
        self.detail.add(JLabel("Automatically restart trains at script startup:"))
        self.autoRestartSwing = JCheckBox("",
          ADsettings.autoRestart)
        self.detail.add(self.autoRestartSwing)
        self.detail.add(JLabel(""))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("ENGINEER SETTINGS"))
        self.detail.add(JLabel(""))
        self.detail.add(JLabel("In front of red signals:"))
        self.stopModeSwing = JComboBox(["Progressively stop train", 
          "Immediately stop train"])
        self.stopModeSwing.setSelectedIndex(ADsettings.stopMode)
        self.detail.add(self.stopModeSwing)
        self.detail.add(JLabel(
          "Delay between clear signal and train departure (seconds): "))
        self.detail1 = JPanel()
        self.startDelayMinSwing = JTextField(
          str(float(ADsettings.startDelayMin)/1000.), 5)
        self.startDelayMaxSwing = JTextField(
          str(float(ADsettings.startDelayMax)/1000.), 5)
        self.detail1.add(JLabel("Between "))
        self.detail1.add(self.startDelayMinSwing)
        self.detail1.add(JLabel(" and "))
        self.detail1.add(self.startDelayMaxSwing)
        self.detail.add(self.detail1)
        self.detail.add(JLabel("Default actions before train departure:"))
        self.defaultStartSwing = JTextField(ADsettings.defaultStartAction, 5)
        self.detail.add(self.defaultStartSwing)
        self.detail.add(JLabel("Switch headlights ON/OFF:"))
        self.lightsSwing = JComboBox(["Never", "When train starts/stops",
          "When schedule starts/ends"])
        self.lightsSwing.setSelectedIndex(ADsettings.lightMode)
        self.detail.add(self.lightsSwing)
        self.detail.add(JLabel("Delay between throttle commands (seconds):"))
        self.dccDelaySwing = JTextField(
          str(float(ADsettings.dccDelay)/1000.), 5)
        self.detail.add(self.dccDelaySwing)
        self.detail.add(JLabel(
          "Maximum interval between throttle commands (seconds): "))
        self.maxIdleSwing = JTextField(
          str(float(ADsettings.maxIdle)/1000.), 5)
        self.detail.add(self.maxIdleSwing)
        self.detail.add(JLabel("Acceleration/deceleration interval: "))
        self.speedRampSwing = JComboBox(["1/10 sec.", "2/10 sec.", "3/10 sec.",
          "4/10 sec.", "5/10 sec."])
        self.speedRampSwing.setSelectedIndex(ADsettings.speedRamp -1)
        self.detail.add(self.speedRampSwing)
        self.detail.add(JLabel("Enable self-adjustment of braking distance: "))
        self.selfLearningSwing = JCheckBox("",
          ADsettings.selfLearning)
        if AutoDispatcher.simulation :
            self.selfLearningSwing.enabled = False
        self.detail.add(self.selfLearningSwing)

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button (don't use the default one, since we wish 
        # the button always on)
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.setButton)
        
    # Buttons of Preferences window =================

    # define what Cancel button in Preferences Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.preferencesFrame = None

    # define what Apply button in Preferences Window does when clicked
    def whenApplyClicked(self,event) :
        ADsettings.verbose = self.verboseSwing.isSelected()
        self.ringBellSwing.isSelected()
        ADsettings.ringBell = self.ringBellSwing.isSelected()
        ADsettings.pauseMode = self.pauseSwing.getSelectedIndex()
        ADsettings.separateTurnouts = (
          self.separateTurnoutsSwing.isSelected())
        ADsettings.separateSignals = (
          self.separateSignalsSwing.isSelected())
        try :
            ADsettings.scale = float(self.scaleSwing.text)
        except :
            ADsettings.scale = 1
            self.scaleSwing.text = "1"
        try :
            ADsettings.flashingCycle = float(self.flashingSwing.text)
        except :
            ADsettings.flashingCycle = 1.0
            self.flashingSwing.text = "1"
        ADsettings.derailDetection = self.derailedSwing.getSelectedIndex()
        ADsettings.stalledDetection = self.stalledSwing.getSelectedIndex()
        try :
            ADsettings.stalledTime = int(
              float(self.stalledTimeSwing.text)*1000.)
        except :
            ADsettings.stalledTime = 1000
            self.stalledTimeSwing.text = "1"
        ADsettings.lostCarsDetection = (
          self.lostCarsSwing.getSelectedIndex())
        oldValue = ADsettings.lostCarsTollerance
        try :
            ADsettings.lostCarsTollerance = int(
              float(self.lostLengthSwing.text) * ADsettings.units)
        except :
            ADsettings.lostCarsTollerance = oldValue
            self.lostLengthSwing.text = str(oldValue
              / ADsettings.units)
        oldValue = ADsettings.lostCarsTollerance
        try :
            ADsettings.lostCarsSections = int(
              self.lostMaxSwing.text)
        except :
            ADsettings.lostCarsSections = oldValue
            self.lostMaxSwing.text = str(oldValue)
        ADsettings.wrongRouteDetection = (
          self.wrongRouteSwing.getSelectedIndex())
        i = self.unitsSwing.getSelectedIndex()
        if i == 0 :
            newUnits = 1.0
        elif i == 1 :
            newUnits = 10.0
        else :
            newUnits = 25.4
        if newUnits != ADsettings.units :
            ADsettings.units = newUnits
            if AutoDispatcher.trainsFrame != None :
                AutoDispatcher.trainsFrame.reDisplay()
                AutoDispatcher.preferencesFrame.show()
        ADsettings.useLength = self.useLengthSwing.isSelected()
        ADsettings.resistiveDefault = self.useResistiveSwing.isSelected()
        ADsettings.allocationAhead = (self.aheadSwing.getSelectedIndex() + 1)
        ADsettings.blockTracking = self.blockTrackingSwing.isSelected()
        ADsettings.sectionTracking = (
          self.sectionTrackingSwing.isSelected())
        ADsettings.trustTurnouts = self.trustTurnoutsSwing.isSelected()
        try :
            ADsettings.turnoutDelay = int(
              float(self.turnoutDelaySwing.text)*1000.)
        except :
            ADsettings.turnoutDelay = 1000
            self.turnoutDelaySwing.text = "1"
        try :
            ADsettings.clearDelay = int(
              float(self.clearDelaySwing.text)*1000.)
        except :
            ADsettings.clearDelay = 0
            self.clearDelaySwing.text = "0"
        ADsettings.trustSignals = self.trustSignalsSwing.isSelected()
        try :
            ADsettings.signalDelay = int(
              float(self.signalDelaySwing.text)*1000.)
        except :
            ADsettings.signalDelay = 0
            self.signalDelaySwing.text = "0"
        ADsettings.autoRestart = self.autoRestartSwing.isSelected()
        try :
            ADsettings.maintenanceTime = float(self.maintenanceSwing.text)
        except :
            ADsettings.maintenanceTime = 0
            self.maintenanceSwing.text = "0"
        try :
            if ADsettings.units == 25.4 :
                multiplier = 1609344. / ADsettings.scale
            else :
                multiplier = 1000000. / ADsettings.scale
            ADsettings.maintenanceMiles = (float(self.milesSwing.text) *
              multiplier)
        except :
            ADsettings.maintenanceMiles = 0
            self.milesSwing.text = "0"
        try :
            ADsettings.dccDelay = int(
              float(self.dccDelaySwing.text)*1000.)
        except :
            ADsettings.dccDelay = 10
            self.dccDelaySwing.text = "0.01"
        ADsettings.stopMode = self.stopModeSwing.getSelectedIndex()
        try :
            ADsettings.startDelayMin = int(
              float(self.startDelayMinSwing.text)*1000.)
        except :
            ADsettings.startDelayMin = 0
            self.startDelayMinSwing.text = "0"
        try :
            ADsettings.startDelayMax = int(
              float(self.startDelayMaxSwing.text)*1000.)
        except :
            ADsettings.startDelayMax = 0
            self.startDelayMaxSwing.text = "0"
        ADsettings.defaultStartAction = self.defaultStartSwing.text
        ADsettings.lightMode = self.lightsSwing.getSelectedIndex()
        ADsettings.speedRamp = self.speedRampSwing.getSelectedIndex() + 1
 
        try :
            ADsettings.maxIdle = int(float(self.maxIdleSwing.text)*1000.)
        except :
            ADsettings.maxIdle = 0
            self.maxIdleSwing.text = "0"
        ADsettings.selfLearning = self.selfLearningSwing.isSelected()
            
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
         "Preferences changes applied")

    # Sound List Window =================

class ADsoundListFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Sound List window
        # super.init
        AdScrollFrame.__init__(self, "List of Sounds", None)

    def createHeader(self):
        header1 = JPanel()
        header1.setLayout(GridLayout(1, 4))
        header1.add(AutoDispatcher.centerLabel("Name"))
        header1.add(JLabel(""))
        header1.add(JLabel(""))
        header1.add(JLabel(""))

        self.header.setLayout(GridLayout(1, 2))
        self.header.add(header1)
        self.header.add(AutoDispatcher.centerLabel("Path"))

    def createDetail(self):
        self.detail.setLayout(GridLayout(len(ADsettings.soundList), 2))
        self.namesSwing = []
        ind = 0
        for s in ADsettings.soundList :
            temppane = JPanel()
            temppane.setLayout(GridLayout(1, 4))
            nameSwing = JTextField(s.name, 5)
            self.namesSwing.append(nameSwing)
            temppane.add(nameSwing)
            browseButton = JButton("Browse")
            browseButton.setActionCommand(str(ind))
            browseButton.actionPerformed = self.whenBrowseClicked
            temppane.add(browseButton)
            if ind == 0 :
                temppane.add(JLabel(""))
            else :
                deleteButton = JButton("Delete")
                deleteButton.setActionCommand(str(ind))
                deleteButton.actionPerformed = self.whenDeleteClicked
                temppane.add(deleteButton)
            playButton = JButton("Play")
            playButton.setActionCommand(str(ind))
            playButton.actionPerformed = self.whenPlayClicked
            temppane.add(playButton)
            self.detail.add(temppane)
            self.detail.add(JLabel(s.path))
            ind += 1
            
    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Apply button
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.setButton)

    # Buttons of Sound List window =================
    
    # define what Cancel button in Sound List Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.soundListFrame = None

    # define what Add button in Sound List Window does when clicked
    def whenAddClicked(self,event) :
        ADsettings.soundList.append(ADsound("New sound"))
        self.soundListChanged()

    # define what Apply button in Sound List Window does when clicked
    def whenApplyClicked(self,event) :
        ind = 0
        for s in ADsettings.soundList :
            s.name = self.namesSwing[ind].text
            ind += 1
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Sound list updated")
        self.soundListChanged()

    # define what Browse button in Sound List Window does when clicked
    def whenBrowseClicked(self,event) :
        ind = int(event.getActionCommand())
        fc = JFileChooser(ADsettings.soundRoot)
        fc.addChoosableFileFilter(ADsoundFilter())
        retVal = fc.showOpenDialog(None)
        if retVal != JFileChooser.APPROVE_OPTION :
            return
        file = fc.getSelectedFile()
        if file == None :
            return
        ADsettings.soundRoot = file.getParent()
        ADsettings.soundList[ind].setPath(file.getPath())
        if self.namesSwing[ind].text != "New sound" :
            ADsettings.soundList[ind].name = self.namesSwing[ind].text
        if ADsettings.soundList[ind].name == "New sound" :
            fileName = file.getName()
            upperName = fileName.upper()
            if upperName.endswith(".WAV") :
                fileName = fileName[0:len(fileName)-4]
            if upperName.endswith(".AU") :
                fileName = fileName[0:len(fileName)-3]
            ADsettings.soundList[ind].name = fileName
        self.soundListChanged()

    # define what Delete button in Sound List Window does when clicked
    def whenDeleteClicked(self,event) :
        ind = int(event.getActionCommand())
        name = ADsettings.soundList[ind].name
        if (JOptionPane.showConfirmDialog(None, "Remove sound \""
           + name
           + "\"?", "Confirmation", JOptionPane.YES_NO_OPTION) == 1) :
            return
        ADsettings.soundList.pop(ind)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Sound \"" + name + "\" removed")
        self.soundListChanged()

    # define what Play button in Sound List Window does when clicked
    def whenPlayClicked(self,event) :
        ind = int(event.getActionCommand())
        ADsettings.soundList[ind].play()
        
    def soundListChanged(self) :
        if AutoDispatcher.soundDefaultFrame != None :
            AutoDispatcher.soundDefaultFrame.reDisplay()
        ADsettings.newSoundDic()
        AutoDispatcher.setPreferencesDirty()
        self.reDisplay()

class ADsoundFilter (FileFilter) :
    def __init__(self) :
        FileFilter.__init__(self)
    def accept(self, f) :
        if f.isDirectory() :
            return True
        name = str(f.getName()).upper()
        if (name.endswith(".WAV") or 
          name.endswith(".AU")) :
            return True;
        return False;
        
    def getDescription(self) :
        return "Sound Clip (*.wav, *.au)"

    # Sound Default Window =================

class ADsoundDefaultFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Sound Default window
        # super.init
        AdScrollFrame.__init__(self, "Default Sounds", None)

    def createHeader(self):
        self.header.setLayout(GridLayout(1, 3))
        self.header.add(AutoDispatcher.centerLabel("Event"))
        self.header.add(AutoDispatcher.centerLabel("Sound"))
        self.header.add(JLabel(""))

    def createDetail(self):
        self.detail.setLayout(GridLayout(len(ADsettings.soundLabel), 3))
        self.soundsSwing = []
        sounds = ["None"]
        for s in ADsettings.soundList :
            sounds.append(s.name)
        ind = 0
        for s in ADsettings.soundLabel :
            self.detail.add(JLabel(s))
            soundSwing = JComboBox(sounds)
            soundSwing.setSelectedIndex(ADsettings.defaultSounds[ind])
            self.soundsSwing.append(soundSwing)
            self.detail.add(soundSwing)
            playButton = JButton("Play")
            playButton.setActionCommand(str(ind))
            playButton.actionPerformed = self.whenPlayClicked
            if ADsettings.defaultSounds[ind] < 1 :
                playButton.enabled = False
            self.detail.add(playButton)
            ind += 1
            
    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.setButton)

    # Buttons of Sound Default window =================
    
    # define what Cancel button in Sound List Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.soundDefaultFrame = None

    # define what Apply button in Sound Default Window does when clicked
    def whenApplyClicked(self,event) :
        ind = 0
        for s in self.soundsSwing :
            ADsettings.defaultSounds[ind] = s.getSelectedIndex()
            ind += 1
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Default sounds changed")
        self.reDisplay()

    # define what Play button in Sound List Window does when clicked
    def whenPlayClicked(self,event) :
        ind = int(event.getActionCommand())
        ind = self.soundsSwing[ind].getSelectedIndex()-1
        if ind < 0 :
            return
        ADsettings.soundList[ind].play()

    # Locomotives Window =================
    
class ADlocosFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Locomotives window
        # super.init
        AdScrollFrame.__init__(self, "Locomotives", None)

    def createHeader(self):
        self.columns = len(ADsettings.speedsList) + 8
        self.header.setLayout(GridLayout(1, self.columns))
        self.header.add(AutoDispatcher.centerLabel("Loco"))
        self.header.add(AutoDispatcher.centerLabel("Addr."))
        for s in ADsettings.speedsList :
            self.header.add(AutoDispatcher.centerLabel(s))
        self.header.add(AutoDispatcher.centerLabel("Acc."))
        self.header.add(AutoDispatcher.centerLabel("Dec."))
        self.header.add(AutoDispatcher.centerLabel("Throttle"))
        runTime = JLabel("RunTime")
        runTime.setHorizontalAlignment(JLabel.RIGHT)
        self.header.add(runTime)
        if ADsettings.units == 25.4 :
            miles = JLabel("Miles")
        else :
            miles = JLabel("Km.")
        miles.setHorizontalAlignment(JLabel.RIGHT)
        self.header.add(miles)
        self.header.add(JLabel(""))

    def createDetail(self):
        # Fill contents of scroll area
        self.locos = ADlocomotive.getNames()
        self.locos.sort()
        self.detail.setLayout(GridLayout(len(self.locos), self.columns))
        ind = 0 
        for ll in self.locos :
            l = ADlocomotive.getByName(ll)
            self.detail.add(AutoDispatcher.centerLabel(str(l.name)))
            self.detail.add(l.addressSwing)
            for s in l.speedSwing :
                self.detail.add(s)
            self.detail.add(l.accSwing)
            self.detail.add(l.decSwing)
            self.detail.add(l.currentSpeedSwing)
            l.outputMileage()
            self.detail.add(l.hoursSwing)
            self.detail.add(l.milesSwing)
            clearButton = JButton("Clear")
            clearButton.setActionCommand(str(ind))
            clearButton.actionPerformed = self.whenClearClicked
            self.detail.add(clearButton)
            ind += 1

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.setButton = JButton("Apply")
        self.setButton.actionPerformed = self.whenApplyClicked
        self.buttons.add(self.setButton)

        # Remove button
        self.removeButton = JButton("Remove locos not in JMRI roster")
        self.removeButton.actionPerformed = self.whenRemoveClicked
        self.buttons.add(self.removeButton)

    # Buttons of Locomotives window =================
    
    # define what Cancel button in Locomotives Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.locosFrame = None

    # define what Remove button in Locomotives Window does when clicked
    def whenRemoveClicked(self,event) :
        locos = ADlocomotive.getList()
        newDic = {}
        removed = 0
        for l in locos :
            if l.inJmriRoster or l.usedBy != None :
                newDic[l.name] = l
            else :
                removed += 1
        if removed > 0 :
            # Ask confirmation!
            if (JOptionPane.showConfirmDialog(None, "Remove " + str(removed)
             + " locomotives not included in JMRI roster?",
             "Confirmation", JOptionPane.YES_NO_OPTION) == 1) :
                removed = 0
        if removed > 0 :
            ADlocomotive.locoIndex = newDic
            self.reDisplay()
            if AutoDispatcher.trainsFrame != None :
                AutoDispatcher.trainsFrame.reDisplay()
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, str(removed)
              + " locomotives removed")
            AutoDispatcher.instance.saveLocomotives()
        else :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              " No locomotive removed")
        
    # define what Apply button in Locomotives Window does when clicked
    def whenApplyClicked(self,event) :
        for l in ADlocomotive.getList() :
            if not l.inJmriRoster :
                l.setAddress(int(l.addressSwing.text))
            ss = []
            for i in range(len(l.speedSwing)) :
                try :
                    value = float(l.speedSwing[i].text)
                except :
                    value = 0.5
                    l.speedSwing[i].text = "0.5"
                ss.append(value)
            l.setSpeedTable(ss)
            try :
                acc = int(l.accSwing.text)
            except :
                acc = 0
                l.accSwing.text = "0"
            try :
                dec = int(l.decSwing.text)
            except :
                dec = 0
                l.decSwing.text = "0"
            l.setMomentum(acc, dec)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Locomotives changes applied")
        AutoDispatcher.instance.saveLocomotives()

    # define what Clear button in Locomotives Window does when clicked
    def whenClearClicked(self,event) :
        ind = int(event.getActionCommand())
        locoName = self.locos[ind]
        loco = ADlocomotive.getByName(locoName)
        loco.runningTime = 0
        loco.mileage = 0
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Mileage and timer of locomotive \"" + locoName + "\" cleared")
        AutoDispatcher.instance.saveLocomotives()
        self.reDisplay()

    # Trains Window =================
    
class ADtrainsFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Trains window
        temppane = JPanel()
        temppane1 = JPanel()
        temppane1.setLayout(BoxLayout(temppane1, BoxLayout.X_AXIS))
        temppane1.add(JLabel(
          " Maximum number of trains running at once (0 = no limit) : "))
        self.maxTrainsSwing = JTextField(str(ADsettings.max_trains), 4)
        temppane1.add(self.maxTrainsSwing)
        self.applyButton = JButton("Set")
        self.applyButton.actionPerformed = self.whenApplyClicked
        temppane1.add(self.applyButton)
        temppane.add(temppane1)
        # super.init
        AdScrollFrame.__init__(self, "Trains", temppane)

    def createHeader(self):
        header1 = JPanel()
        header1.setLayout(GridLayout(1, 2))
        header1.add(AutoDispatcher.centerLabel("Train"))
        header1.add(AutoDispatcher.centerLabel("Direction"))
        header2 = JPanel()
        header2.setLayout(GridLayout(1, 2))
        header2.add(AutoDispatcher.centerLabel("Section"))
        header2.add(AutoDispatcher.centerLabel("Locomotive"))
        header3 = JPanel()
        header3.setLayout(GridLayout(1, 2))
        header3.add(AutoDispatcher.centerLabel("Reversed"))
        header3.add(AutoDispatcher.centerLabel("Dest./State"))
        header4 = JPanel()
        header4.setLayout(GridLayout(1, 2))
        header4.add(AutoDispatcher.centerLabel("Speed"))
        header4.add(JLabel(""))
        header5 = JPanel()
        header5.setLayout(GridLayout(1, 2))
        header5.add(JLabel(""))
        header5.add(JLabel(""))

        header6 = JPanel()
        header6.setLayout(GridLayout(1, 1))
        header6.add(AutoDispatcher.centerLabel("Schedule"))

        self.header.setLayout(GridLayout(1, 6))
        self.header.add(header1)
        self.header.add(header2)
        self.header.add(header3)
        self.header.add(header4)
        self.header.add(header5)
        self.header.add(header6)

    def createDetail(self):
        # Fill contents of scroll area
        locos = []
        for l in ADlocomotive.getList() :
            if l.usedBy == None :
                locos.append(l.name)
                
        trains = ADtrain.getList()
        nTrains = len(trains)

        temppane1 = JPanel()
        temppane1.setLayout(GridLayout(nTrains, 2))
        temppane2 = JPanel()
        temppane2.setLayout(GridLayout(nTrains, 2))       
        temppane3 = JPanel()
        temppane3.setLayout(GridLayout(nTrains, 2))
        temppane4 = JPanel()
        temppane4.setLayout(GridLayout(nTrains, 2))
        temppane5 = JPanel()
        temppane5.setLayout(GridLayout(nTrains, 2))
        temppane6 = JPanel()
        temppane6.setLayout(GridLayout(nTrains, 1))

        ind = 0
        for t in trains :
        
            locosList = [t.locoName]
            locosList.extend(locos)
            locosList.sort()
            t.locoRoster = JComboBox(locosList)
            t.locoRoster.setSelectedItem(t.locoName)
            enableLoco = (t.engineerSwing.getSelectedItem() != "Manual" and
              not t.running )
            t.locoRoster.setEnabled(enableLoco)
            t.reversedSwing.setEnabled(enableLoco)
        
            temppane1.add(t.nameSwing)
            temppane1.add(t.directionSwing)

            temppane2.add(t.sectionSwing)
            temppane2.add(t.locoRoster)
            
            temppane3.add(t.reversedSwing)
            temppane3.add(t.destinationSwing)

            temppane4.add(t.speedLevelSwing)
            t.detailButton.setActionCommand(str(ind))
            t.detailButton.actionPerformed = self.whenDetailClicked
            temppane4.add(t.detailButton)
            
            temppane5.add(t.changeButton)
            temppane5.add(t.deleteButton)

            temppane6.add(t.scheduleSwing)
            ind += 1
            
        self.detail.setLayout(GridLayout(1, 4))
        self.detail.add(temppane1)
        self.detail.add(temppane2)
        self.detail.add(temppane3)
        self.detail.add(temppane4)
        self.detail.add(temppane5)
        self.detail.add(temppane6)

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Add button
        self.addButton = JButton("Add")
        self.addButton.actionPerformed = self.whenAddClicked
        self.buttons.add(self.addButton)

        # Import button
        self.importButton = JButton("Import")
        self.importButton.actionPerformed = self.whenImportClicked
        self.buttons.add(self.importButton)

    # Buttons of Trains window =================
    
    # define what Cancel button in Locomotives Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.trainsFrame = None
            if AutoDispatcher.trainDetailFrame != None :
                AutoDispatcher.trainDetailFrame.dispose()
                AutoDispatcher.trainDetailFrame = None

    # define what Add button in Trains Window does when clicked
    def whenAddClicked(self,event) :
        ADtrain("New train")
        AutoDispatcher.setTrainsDirty()
        self.reDisplay()
        
    # define what Import button in Trains Window does when clicked
    def whenImportClicked(self,event) :
        if AutoDispatcher.importFrame == None :
            AutoDispatcher.importFrame = ADImportTrainFrame()
        else :
            AutoDispatcher.importFrame.reDisplay()

    # define what Detail button in Trains Window does when clicked
    def whenDetailClicked(self,event) :
        ind = int(event.getActionCommand())
        if AutoDispatcher.trainDetailFrame != None :
            AutoDispatcher.trainDetailFrame.show()
            if ADtrain.trains[ind] == AutoDispatcher.trainDetailFrame.train :
                return
            if (JOptionPane.showConfirmDialog(None, "Save details of train \""
               + AutoDispatcher.trainDetailFrame.train.getName()
               + "\" before editing details of train \""
               + ADtrain.trains[ind].getName() + "\"?", "Confirmation",
               JOptionPane.YES_NO_OPTION) != 1) :
                    AutoDispatcher.trainDetailFrame.train.whenSetClicked(None)
            AutoDispatcher.trainDetailFrame.dispose()
        AutoDispatcher.trainDetailFrame = ADtrainDetailFrame(ADtrain.trains[ind])

    # define what Apply button in Trains Window does when clicked
    def whenApplyClicked(self,event) :
        try :
            ADsettings.max_trains = int(self.maxTrainsSwing.text)
        except :
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
              "Wrong maximum number of trains: "
              + self.maxTrainsSwing.text + " ignored")
            self.maxTrainsSwing.text = str(ADsettings.max_trains)
            return
        if ADsettings.max_trains < 0 :
            ADsettings.max_trains = 0
            self.maxTrainsSwing.text = "0"
        AutoDispatcher.setPreferencesDirty()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Maximum number of trains set to "
          + str(ADsettings.max_trains))

    def deleteTrain(self, train):
    # Routine to delete a train, called when the DEL button on train's
    # row is clicked
        # Release sections allocated to removed train
        train.releaseSections()
        # If the train has a locomotive, release it
        if train.locomotive != None :
            train.locomotive.usedBy = None
        # remove train from table
        ADtrain.remove(train)
        AutoDispatcher.setTrainsDirty()
        # force redisplay of Trains Window contents
        self.reDisplay()
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Train \"" + train.name + "\" removed")

    # Trains Window =================
    
class ADtrainDetailFrame (AdScrollFrame) :
    def __init__(self, train) :
        # Create Train Detail window
        self.train = train
        # super.init
        AdScrollFrame.__init__(self, "Train " + train.getName()
          + " detail", None)

    def createHeader(self):
        self.header = None

    def createDetail(self):
        # Fill contents of scroll area
        engineerList = AutoDispatcher.engineers.keys()
        engineerList.sort()
        engineerList.append("Manual")

        if self.train.locomotive == None :
            rows = 7
        else :
            rows = 8

        self.detail.setLayout(BoxLayout(self.detail, BoxLayout.Y_AXIS))

        detail1 = JPanel()
        detail1.setLayout(GridLayout(rows, 2))
        
        detail1.add(JLabel("Cars are equipped with resistive wheels: "))
        detail1.add(self.train.resistiveSwing)
        detail1.add(JLabel("Actions before train departure: "))
        self.train.startActionSwing.text = self.train.startAction
        detail1.add(self.train.startActionSwing)        
        detail1.add(JLabel("Stop at beginning of sections that support this option: "))
        detail1.add(self.train.canStopAtBeginningSwing)
        if ADsettings.units == 1.0 :
            detail1.add(JLabel("Train length (mm.), including"
              " locomotive: "))
        elif ADsettings.units == 10.0 :
            detail1.add(JLabel("Train length (cm.), including"
              " locomotive: "))
        else :
            detail1.add(JLabel("Train length (inches), including"
              " locomotive: "))
        self.train.trainLengthSwing.text = str(round(self.train.trainLength
          / ADsettings.units,2))
        detail1.add(self.train.trainLengthSwing)

        detail1.add(JLabel("Sections ahead to be allocated: "))
        detail1.add(self.train.trainAllocationSwing)

        detail1.add(JLabel("Engineer: "))
        self.train.engineerSwing.removeAllItems()
        for i in engineerList:
            self.train.engineerSwing.addItem(i)
        if self.train.engineerName in engineerList :
            self.train.engineerSwing.setSelectedItem(self.train.engineerName)
        else :
            self.train.engineerSwing.setSelectedItem("Auto")
            self.train.engineerAssigned = False
        detail1.add(self.train.engineerSwing)
        if self.train.locomotive != None :
            detail1.add(JLabel(
              "Clear braking history of this train for locomotive \""
              + self.train.locoName + "\": "))
            clearLoco = JButton("Clear history for current locomotive")
            clearLoco.actionPerformed = self.whenClearLocoClicked
            detail1.add(clearLoco)
        detail1.add(JLabel(
          "Clear braking history of this train for all locomotives: "))
        clearAll = JButton("Clear history for all locomotives")
        clearAll.actionPerformed = self.whenClearAllClicked
        detail1.add(clearAll)
        self.detail.add(detail1)
        
        detail1 = JPanel()
        detail1.add(JLabel("Schedule"))
        self.detail.add(detail1)

        detail1 = JPanel()
        self.scheduleSwing = JTextArea(self.train.scheduleSwing.text, 4, 60)
        self.scheduleSwing.setLineWrap(True)
        self.scheduleSwing.setWrapStyleWord(True)
        detail1.add(JScrollPane(self.scheduleSwing))
        self.detail.add(detail1)

        detail1 = JPanel()
        detail1.add(JLabel("Train speeds correspondence"))
        self.detail.add(detail1)

        detail1 = JPanel()
        detail1.setLayout(GridLayout(len(ADsettings.speedsList)+1, 2))
        detail1.add(JLabel("Instead of"))
        detail1.add(JLabel("Use"))
        self.trainSpeedSwing = []
        ind = 1
        max = len(ADsettings.speedsList)-1
        for s in ADsettings.speedsList :
            if ind > len(self.train.trainSpeed) :
                self.train.trainSpeed.append(ind)
            detail1.add(JLabel(s))
            speedSwing = JComboBox(ADsettings.speedsList)
            i = self.train.trainSpeed[ind-1]-1
            if i > max :
                i = max
            speedSwing.setSelectedIndex(i)
            self.trainSpeedSwing.append(speedSwing)
            detail1.add(speedSwing)
            ind += 1
        self.detail.add(detail1)

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

        # Apply button
        self.buttons.add(self.train.setButton)

    # Buttons of Train Detail window =================
    
    # define what Cancel button in Train Detail Window does when clicked
    def whenCancelClicked(self,event) :
            AdScrollFrame.dispose(self)
            AutoDispatcher.trainDetailFrame = None

    # define what Clear Loco button in Train Detail Window does when clicked
    def whenClearLocoClicked(self,event) :
        self.train.clearBrakingHistory(self.train.locomotive)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Braking history for locomotive cleared")

    # define what Clear All button in Train Detail Window does when clicked
    def whenClearAllClicked(self,event) :
        self.train.clearBrakingHistory(None)
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
          "Braking history for train cleared")

    # Import Trains Window =================
    
class ADImportTrainFrame (AdScrollFrame) :
    def __init__(self) :
        # Create Operations Interface window
        # Allows user to import an Operations train in AutoDispatcher
        # super.init
        AdScrollFrame.__init__(self, "Import train", JLabel("Choose train to be imported"))

    def createHeader(self):
        self.header.setLayout(GridLayout(1, 3))
        self.header.add(AutoDispatcher.centerLabel("Train name"))
        self.header.add(AutoDispatcher.centerLabel("Status"))
        self.header.add(JLabel(""))

    def createDetail(self):
        trainIds = []
        # Fill contents of scroll area
#        trainManager = TrainManager.instance()
#        trainIds = trainManager.getTrainsByNameList()
#        nTrains = len(trainIds)
#        self.detail.setLayout(GridLayout(nTrains, 3))
#        self.opTrains = []
#        for i in range(nTrains) :
#            opTrain = trainManager.getTrainById(trainIds[i])
#            self.opTrains.append(opTrain)
#            self.detail.add(JLabel(opTrain.getName()))
#            if opTrain.getBuilt() :
#                self.detail.add(AutoDispatcher.centerLabel("Built"))
#                importButton = JButton("Import")
#                importButton.setActionCommand(str(i))
#                importButton.actionPerformed = self.whenImportClicked
#                self.detail.add(importButton)
#            else :
#                self.detail.add(JLabel(""))
#                self.detail.add(JLabel(""))

    def createButtons(self) : 
        # Cancel button
        self.cancelButton.actionPerformed = self.whenCancelClicked
        self.buttons.add(self.cancelButton)

    # Buttons of Import Trains window =================
    
    # define what Cancel button in Import Trains Window does when clicked
    def whenCancelClicked(self,event) :
        AdScrollFrame.dispose(self)
        AutoDispatcher.importFrame = None
            
    # define what Import button in Import Trains Window does when clicked
    def whenImportClicked(self,event) :
        ind = 0
#        ind = int(event.getActionCommand())
#        # Get Operations train
#        opTrain = self.opTrains[ind]
#        name = opTrain.getIconName()
#        engine = opTrain.getLeadEngine()
#        if engine == None :
#            engine = ""
#        else :
#            engine = engine.getNumber()
#        # Get the list of cars to be hauled by the train
#        carManager = CarManager.instance()
#        carIds = carManager.getCarsByTrainList(opTrain)
#        cars = []
#        for carId in carIds :
#            cars.append(carManager.getCarById(carId))
#        # Get train route
#        route = opTrain.getRoute()
#        routeIds = route.getLocationsBySequenceList()
#        routeLocations = []
#        for id in routeIds :
#            routeLocations.append(route.getLocationById(id))
#        # Now build our schedule
#        departStation = True
#        schedule = ""
#        previousDirection = startDirection = ""
#        startLocation = None
#        hauled = []
#        while len(routeLocations) > 0 :
#            routeLocation = routeLocations.pop(0)
#            direction = routeLocation.getTrainDirectionString().upper()
#            location = ADlocation.getByName(routeLocation.getName())
#            manualSwitching = ""
#            # Check if any car must be picked up or dropped here
#            for car in cars :
#                source = car.getRouteLocation()
#                destination = car.getRouteDestination()
#                if source == destination :
#                    continue
#                if source == routeLocation and not car in hauled :
#                    pickUp = True
#                    # does train pass twice in this location ?
#                    if routeLocation in routeLocations :
#                        # Yes. See if car must be picked up now or next time
#                        for nextLocation in routeLocations :
#                            if nextLocation == routeLocation :
#                                pickUp = False
#                                break
#                            if nextLocation == destination :
#                                break
#                    if pickUp :
#                        hauled.append(car)
#                        manualSwitching = " $M"
#                elif destination == routeLocation and car in hauled :
#                    manualSwitching = " $M"
#                    hauled.remove(car)
#            if departStation :
#                startDirection = direction
#                manualSwitching = ""
#                startLocation = location
#                routeStart = routeLocation
#                departStation = False
#            if location != None :
#                if len(schedule) > 0 :
#                    schedule += " "
#                if previousDirection != direction :
#                    schedule += "$" + direction + " "
#                    previousDirection = direction
#                schedule += "[" + location.text + "]" + manualSwitching
#        train = ADtrain(name)
#        train.opTrain = opTrain
#        if startDirection.strip() != "" :
#            train.setDirection(startDirection)
#        if engine != None and ADlocomotive.getByName(engine) != None :
#            train.changeLocomotive(engine)
#        else :
#            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
#              "Locomotive \"" + engine +
#              "\" not found. Manual running assumed!")
#            train.setEngineer("Manual")
#        # Find start section
#        if startLocation != None :
#            for section in startLocation.getSections() :
#                section.setManual(True)
#                if section.getAllocated() == None :
#                    train.setSection(section, True)
#                    if section.getAllocated() == train :
#                        break
#                section.setManual(False)
#            if section.getAllocated() != train :
#                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
#                "Canot place train \"" + name +
#                "\" in location " + startLocation.name
#                + " (all sections occupied)")
#            train.trainLength = round(float(routeStart.getTrainLength())
#              * 304.8 / ADsettings.scale)
#        if schedule.strip() != "" :
#            train.setSchedule(schedule)
#        train.updateSwing()
#        
#        AutoDispatcher.setTrainsDirty()
#        if AutoDispatcher.trainsFrame != None :
#            AutoDispatcher.trainsFrame.reDisplay()
#        self.whenCancelClicked(None)
            
class ADexamples1 :

# DEFAULT DATA FOR EXAMPLE PANELS ==========================
    # Used if no setting file is found
    # Settings are chosen on the basis of layout Title
    examples = {}
    exampleTrains =  {}

    examples["JavaOne remake"] = (
        '2.0 Beta', ('CCW', 'CW'), 'N1', 'W', 1000, 0, 1, 0, 1, 2, 2, 30000
        , 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA', 'CYAN']
        , 1, [['E', '', 'CCW-CW', 'HEccw', 'HEcw', '', ''], ['N1', '', ''
        , 'HN1ccw', 'HN1cw', '', ''], ['N2', '', '', 'HN2ccw', 'HN2cw', ''
        , ''], ['S1', '', '', 'HS1ccw', 'HS1cw', '', ''], ['S2', '', ''
        , 'HS2ccw', 'HS2cw', '', ''], ['W', '', 'CCW-CW', 'HWccw', 'HWcw', ''
        , '']], [['Ea', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
        , '', '', ''], ['Eb', '', '', '', '', 'BRAKE', '', '', '', '', '', ''
        , ''], ['Ec', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'Ed', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', '']
        , ['N1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['N1b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['N1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'N1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
        ], ['N2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['N2b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['N2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'N2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
        ], ['S1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['S1b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['S1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'S1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
        ], ['S2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['S2b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['S2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'S2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
        ], ['Wa', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['Wb', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], [
        'Wc', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Wd', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', '']], 1, 1
        , 0, 25.4, 0, 1, 0, ['Min.', 'Low', 'Med.', 'High', 'Max.'], 2, 10, 0
        , 2, 1, [['Stop', -1, -1, 0], ['Clear', -1, -1, 3]], 1, [['Single '
        'Head', [['Red'], ['Green']], [-1, -1]]], [['HXbccw', 'Single Head'
        , ['HXbccw']], ['HWcw', 'Single Head', ['HWcw']], ['HEcw', 'Single '
        'Head', ['HEcw']], ['HS1cw', 'Single Head', ['HS1cw']], ['HSWcw'
        , 'Single Head', ['HSWcw']], ['HSEcw', 'Single Head', ['HSEcw']], [
        'HNWccw', 'Single Head', ['HNWccw']], ['HSWccw', 'Single Head', [
        'HSWccw']], ['HWccw', 'Single Head', ['HWccw']], ['HN1cw', 'Single '
        'Head', ['HN1cw']], ['HNWcw', 'Single Head', ['HNWcw']], ['HXacw'
        , 'Single Head', ['HXacw']], ['HNEcw', 'Single Head', ['HNEcw']], [
        'HNEccw', 'Single Head', ['HNEccw']], ['HN1ccw', 'Single Head', [
        'HN1ccw']], ['HSEccw', 'Single Head', ['HSEccw']], ['HS2cw', 'Single '
        'Head', ['HS2cw']], ['HS1ccw', 'Single Head', ['HS1ccw']], ['HEccw'
        , 'Single Head', ['HEccw']], ['HN2ccw', 'Single Head', ['HN2ccw']], [
        'HS2ccw', 'Single Head', ['HS2ccw']], ['HXaccw', 'Single Head', [
        'HXaccw']], ['HN2cw', 'Single Head', ['HN2cw']], ['HXbcw', 'Single '
        'Head', ['HXbcw']]], 0, 0, 0, 1, 20000, 0, 1, 1, 1270, 3, 0, [['Bell'
        , 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
        , '', 12.0, 0.0, 87.0, [], 1.0)

    exampleTrains["JavaOne remake"] = [
        ['T1017', 'N1', 'CCW', 0, '(3([N1 N2] [S1 S2]) $P25)', 0, 889.0, [0, 1
        , 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1, 3
        , 1, 7, 0, 1, 0, 10, 1, 0, 3, 1, 9], '1017', 0, [], 'Auto', 'N1', [1
        , 2, 3, 4, 5], {}], ['T1019', 'S1', 'CW', 0, '(2([S1 S2] [N1 N2]) '
        '$P15)', 0, 508.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0
        , 2, 0, 1, 0, 0, 0, 1, 2, 1, 7, 0, 1, 0, 10, 1, 0, 2, 1, 9], '1019'
        , 0, [], 'Auto', 'S1', [1, 2, 3, 4, 5], {}]]

    examples["Operations Example"] = (
        '2.0 Beta', ['EAST', 'WEST'], 'Sv1', 'SvFr', 1000, 0, 2, 0, 1, 2, 2
        , 0, 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA'
        , 'CYAN'], 1, [['Bf1', '', '', 'HBf1west', 'HBf1east', '', 'BFman']
        , ['Bf2', '', '', 'HBf2west', 'HBf2east', '', 'BFman'], ['BfPa', ''
        , 'EAST-WEST', 'HBfPawest', 'HBfPaeast', '', ''], ['Dv1', '', ''
        , 'HDv1west', 'HDv1east', '', 'DVman'], ['Dv2', '', '', 'HDv2west'
        , 'HDv2east', '', 'DVman'], ['DvHb', '', 'EAST-WEST', 'HDvHbwest'
        , 'HDvHbeast', '', ''], ['Fa1', '', '', 'HFa1west', 'HFa1east', ''
        , 'FAman'], ['Fa2', '', '', 'HFa2west', 'HFa2east', '', 'FAman'], [
        'FaLv1', '', 'EAST-WEST+', 'HFaLv1west', 'HFaLv1east', '', ''], [
        'FaLv2', '', 'EAST-WEST+', 'HFaLv2west', 'HFaLv2east', '', ''], [
        'FaLv3', '', 'EAST-WEST+', 'HFaLv3west', 'HFaLv3east', '', ''], [
        'Fr1', '', '', 'HFr1west', 'HFr1east', '', 'FRman'], ['Fr2', '', ''
        , 'HFr2west', 'HFr2east', '', 'FRman'], ['FrBf', '', 'EAST-WEST'
        , 'HFrBfwest', 'HFrBfeast', '', ''], ['Hb1', '', '', 'HHb1west'
        , 'HHb1east', '', 'HBman'], ['Hb2', '', '', 'HHb2west', 'HHb2east'
        , '', 'HBman'], ['HbFa', '', 'EAST-WEST', 'HHbFawest', 'HHbFaeast'
        , '', ''], ['Lv1', '', '', 'HLv1west', 'HLv1cw', '', 'LVman'], ['Lv2'
        , '', '', 'HLv2west', 'HLv2cw', '', 'LVman'], ['Pa1', '', ''
        , 'HPa1west', 'HPa1east', '', 'PAman'], ['Pa2', '', '', 'HPa2west'
        , 'HPa2east', '', 'PAman'], ['PaDv', '', 'EAST-WEST', 'HPaDvwest'
        , 'HPaDveast', '', ''], ['Sv1', '', '', 'HSv1ccw', 'HSv1east', ''
        , 'SVman'], ['Sv2', '', '', 'HSv2ccw', 'HSv2east', '', 'SVman'], [
        'SvFr', '', 'EAST-WEST', 'HSvFrwest', 'HSvFreast', '', '']], [['Bf1a'
        , 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], [
        'Bf1b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['Bf1c'
        , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Bf1d', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
        'Bf2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['Bf2b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['Bf2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'Bf2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
        , ''], ['BfPa1', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
        , '', '', ''], ['BfPa2', '', '', '', '', 'BRAKE', '', '', '', '', ''
        , '', ''], ['BfPa3', '', '', '', '', '', '', '', '', '', '', '', '']
        , ['BfPa4', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'BfPa5', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
        , ''], ['Dv1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
        , '', '', ''], ['Dv1b', '', '', '', '', 'BRAKE', '', '', '', '', ''
        , '', ''], ['Dv1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
        , ''], ['Dv1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE'
        , '', '', ''], ['Dv2a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['Dv2b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['Dv2c', '', '', '', '', '', '', '', ''
        , '', '', 'BRAKE', ''], ['Dv2d', '', 'ALLOCATE', '', '', '', ''
        , 'STOP', '', 'SAFE', '', '', ''], ['DvHb1', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '', '', ''], ['DvHb2', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', '', ''], ['DvHb3', '', '', '', '', ''
        , '', '', '', '', '', '', ''], ['DvHb4', '', '', '', '', '', '', ''
        , '', '', '', 'BRAKE', ''], ['DvHb5', '', 'ALLOCATE', '', '', '', ''
        , 'STOP', '', 'SAFE', '', '', ''], ['Fa1a', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '', '', ''], ['Fa1b', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', '', ''], ['Fa1c', '', '', '', '', ''
        , '', '', '', '', '', 'BRAKE', ''], ['Fa1d', '', 'ALLOCATE', '', ''
        , '', '', 'STOP', '', 'SAFE', '', '', ''], ['Fa2a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['Fa2b', ''
        , '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['Fa2c', '', ''
        , '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Fa2d', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
        'FaLv1', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['FaLv2', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['FaLv3', '', '', '', '', '', '', '', '', '', '', '', ''], ['FaLv4'
        , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['FaLv5', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
        'LaFv6', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['FaLv7', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['FaLv8', '', '', '', '', '', '', '', '', '', '', '', ''], ['FaLv9'
        , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['FaLv10', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
        'FaLv11', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['FaLv12', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''
        ], ['FaLv13', '', '', '', '', '', '', '', '', '', '', '', ''], [
        'FaLv14', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'FaLv15', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
        , ''], ['Fr1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
        , '', '', ''], ['Fr1b', '', '', '', '', 'BRAKE', '', '', '', '', ''
        , '', ''], ['Fr1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
        , ''], ['Fr1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE'
        , '', '', ''], ['Fr2a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['Fr2b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['Fr2c', '', '', '', '', '', '', '', ''
        , '', '', 'BRAKE', ''], ['Fr2d', '', 'ALLOCATE', '', '', '', ''
        , 'STOP', '', 'SAFE', '', '', ''], ['FrBf1', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '', '', ''], ['FrBf2', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', '', ''], ['FrBf3', '', '', '', '', ''
        , '', '', '', '', '', '', ''], ['FrBf4', '', '', '', '', '', '', ''
        , '', '', '', 'BRAKE', ''], ['FrBf5', '', 'ALLOCATE', '', '', '', ''
        , 'STOP', '', 'SAFE', '', '', ''], ['Hb1a', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '', '', ''], ['Hb1b', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', '', ''], ['Hb1c', '', '', '', '', ''
        , '', '', '', '', '', 'BRAKE', ''], ['Hb1d', '', 'ALLOCATE', '', ''
        , '', '', 'STOP', '', 'SAFE', '', '', ''], ['Hb2a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['Hb2b', ''
        , '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['Hb2c', '', ''
        , '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Hb2d', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
        'HbFa1', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['HbFa2', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['HbFa3', '', '', '', '', '', '', '', '', '', '', '', ''], ['HbFa4'
        , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['HbFa5', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
        'Lv1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['Lv1b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['Lv1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'Lv1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
        , ''], ['Lv2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
        , '', '', ''], ['Lv2b', '', '', '', '', 'BRAKE', '', '', '', '', ''
        , '', ''], ['Lv2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
        , ''], ['Lv2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE'
        , '', '', ''], ['Pa1a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['Pa1b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['Pa1c', '', '', '', '', '', '', '', ''
        , '', '', 'BRAKE', ''], ['Pa1d', '', 'ALLOCATE', '', '', '', ''
        , 'STOP', '', 'SAFE', '', '', ''], ['Pa2a', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '', '', ''], ['Pa2b', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', '', ''], ['Pa2c', '', '', '', '', ''
        , '', '', '', '', '', 'BRAKE', ''], ['Pa2d', '', 'ALLOCATE', '', ''
        , '', '', 'STOP', '', 'SAFE', '', '', ''], ['PaDv1', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['PaDv2', ''
        , '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['PaDv3', '', ''
        , '', '', '', '', '', '', '', '', '', ''], ['PaDv4', '', '', '', ''
        , '', '', '', '', '', '', 'BRAKE', ''], ['PaDv5', '', 'ALLOCATE', ''
        , '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['Sv1a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['Sv1b', ''
        , '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['Sv1c', '', ''
        , '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Sv1d', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
        'Sv2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['Sv2b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['Sv2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'Sv2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
        , ''], ['SvFr1', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
        , '', '', ''], ['SvFr2', '', '', '', '', 'BRAKE', '', '', '', '', ''
        , '', ''], ['SvFr3', '', '', '', '', '', '', '', '', '', '', '', '']
        , ['SvFr4', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'SvFr5', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
        , '']], 1, 1, 0, 25.4, 0, 0, 0, ['Min.', 'Low', 'Med.', 'High'
        , 'Max.'], 3, 10, 0, 2, 1, [['Stop', -1, -1, 0], ['Clear', -1, -1, 5]
        , ['Approach', 0, -1, 3]], 1, [['Single Head', [['Red'], ['Green'], [
        'Yellow']], [-1, -1, -1]]], [['HFaLv3west', 'Single Head', [
        'HFaLv3west']], ['HDvHbccw', 'Single Head', ['HDvHbccw']], [
        'HFr1east', 'Single Head', ['HFr1east']], ['HFaLveast', 'Single Head'
        , ['HFaLveast']], ['HHbFacw', 'Single Head', ['HHbFacw']], [
        'HFaLv1east', 'Single Head', ['HFaLv1east']], ['HSv2east', 'Single '
        'Head', ['HSv2east']], ['HDv2west', 'Single Head', ['HDv2west']], [
        'HBf1west', 'Single Head', ['HBf1west']], ['HHb1east', 'Single Head'
        , ['HHb1east']], ['HHbFawest', 'Single Head', ['HHbFawest']], [
        'HSvFrccw', 'Single Head', ['HSvFrccw']], ['HFa2east', 'Single Head'
        , ['HFa2east']], ['HFr2west', 'Single Head', ['HFr2west']], [
        'HBfPawest', 'Single Head', ['HBfPawest']], ['HSvFreast', 'Single '
        'Head', ['HSvFreast']], ['HFaLv2west', 'Single Head', ['HFaLv2west']]
        , ['HFaLvccw', 'Single Head', ['HFaLvccw']], ['HSv1east', 'Single '
        'Head', ['HSv1east']], ['HDv1west', 'Single Head', ['HDv1west']], [
        'HHb2west', 'Single Head', ['HHb2west']], ['HLv2east', 'Single Head'
        , ['HLv2east']], ['HBfPaccw', 'Single Head', ['HBfPaccw']], [
        'HFa1east', 'Single Head', ['HFa1east']], ['HFrBfccw', 'Single Head'
        , ['HFrBfccw']], ['HFr1west', 'Single Head', ['HFr1west']], [
        'HFaLvwest', 'Single Head', ['HFaLvwest']], ['HSv1ccw', 'Single Head'
        , ['HSv1ccw']], ['HPa2east', 'Single Head', ['HPa2east']], [
        'HFaLv1west', 'Single Head', ['HFaLv1west']], ['HFaLvcw', 'Single '
        'Head', ['HFaLvcw']], ['HSv2west', 'Single Head', ['HSv2west']], [
        'HSv2ccw', 'Single Head', ['HSv2ccw']], ['HFrBfeast', 'Single Head'
        , ['HFrBfeast']], ['HFrBfcw', 'Single Head', ['HFrBfcw']], [
        'HPaDveast', 'Single Head', ['HPaDveast']], ['HSvFrcw', 'Single Head'
        , ['HSvFrcw']], ['HHb1west', 'Single Head', ['HHb1west']], ['HSv2cw'
        , 'Single Head', ['HSv2cw']], ['HDvHbeast', 'Single Head', [
        'HDvHbeast']], ['HFa2west', 'Single Head', ['HFa2west']], ['HSv1cw'
        , 'Single Head', ['HSv1cw']], ['HLv1east', 'Single Head', ['HLv1east'
        ]], ['HSvFrwest', 'Single Head', ['HSvFrwest']], ['HPa1east', 'Single'
        ' Head', ['HPa1east']], ['HBf2east', 'Single Head', ['HBf2east']], [
        'HHbFaccw', 'Single Head', ['HHbFaccw']], ['HSv1west', 'Single Head'
        , ['HSv1west']], ['HBfPacw', 'Single Head', ['HBfPacw']], ['HDvHbcw'
        , 'Single Head', ['HDvHbcw']], ['HFaLv3east', 'Single Head', [
        'HFaLv3east']], ['HLv2west', 'Single Head', ['HLv2west']], [
        'HFa1west', 'Single Head', ['HFa1west']], ['HDv2east', 'Single Head'
        , ['HDv2east']], ['HPa2west', 'Single Head', ['HPa2west']], [
        'HPaDvcw', 'Single Head', ['HPaDvcw']], ['HBf1east', 'Single Head', [
        'HBf1east']], ['HFrBfwest', 'Single Head', ['HFrBfwest']], ['HLv2cw'
        , 'Single Head', ['HLv2cw']], ['HPaDvwest', 'Single Head', [
        'HPaDvwest']], ['HHbFaeast', 'Single Head', ['HHbFaeast']], ['HLv1cw'
        , 'Single Head', ['HLv1cw']], ['HFr2east', 'Single Head', ['HFr2east'
        ]], ['HBfPaeast', 'Single Head', ['HBfPaeast']], ['HDvHbwest'
        , 'Single Head', ['HDvHbwest']], ['HFaLv2east', 'Single Head', [
        'HFaLv2east']], ['HLv1west', 'Single Head', ['HLv1west']], [
        'HDv1east', 'Single Head', ['HDv1east']], ['HPa1west', 'Single Head'
        , ['HPa1west']], ['HBf2west', 'Single Head', ['HBf2west']], [
        'HHb2east', 'Single Head', ['HHb2east']], ['HPaDvccw', 'Single Head'
        , ['HPaDvccw']]], 0, 0, 0, 1, 60000, 0, 1, 1, 2032, 3, 0, [['Bell'
        , 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
        , '', 0.0, 0.0, 87.0, [['Lakeview', 'Lv1 Lv2'], ['Hillsboro',
        'Hb1 Hb2'], ['Fremont', 'Fr1 Fr2'], ['Port Arthur', 'Pa1 Pa2'],
        ['Danville', 'Dv1 Dv2'], ['Farmington', 'Fa1 Fa2'], ['Bakersfield',
        'Bf1 Bf2'], ['Susanville', 'Sv1 Sv2']], 1.0)

    exampleTrains["Operations Example"] = []

    examples["Reversing Track Example"] = (
        '2.0 Beta', ('CCW', 'CW'), 'B1', 'B4', 1000, 0, 1, 0, 1, 2, 2, 0, 1
        , ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA', 'CYAN'], 1
        , [['B1', '', '', 'HB1cw', 'HB1ccw', '', 'B1man'], ['B2', '', ''
        , 'HB2cw', 'HB2ccw', '', 'B2man'], ['B3', '', 'CCW-CW', 'HB3cw'
        , 'HB3ccw', '', ''], ['B4', '', 'CCW-CW', 'HB4cw', 'HB4ccw', '', '']
        , ['B5', '', '', 'HB5cw', 'HB5ccw', 'INVERTED', 'B5man'], ['B6', ''
        , '', 'HB6cw', 'HB6ccw', '', ''], ['B7', '', '', 'HB7cw', 'HB7ccw'
        , '', 'B7man']], [['B1a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['B1b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['B1c', '', '', '', '', '', '', '', '', ''
        , '', 'BRAKE', ''], ['B1d', '', 'ALLOCATE', '', '', '', '', 'STOP'
        , '', 'SAFE', '', '', ''], ['B2a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['B2b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['B2c', '', '', '', '', '', '', '', '', ''
        , '', 'BRAKE', ''], ['B2d', '', 'ALLOCATE', '', '', '', '', 'STOP'
        , '', 'SAFE', '', '', ''], ['B3a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['B3b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['B3c', '', '', '', '', '', '', '', '', ''
        , '', '', ''], ['B3d', '', '', '', '', '', '', '', '', '', ''
        , 'BRAKE', ''], ['B3e', '', 'ALLOCATE', '', '', '', '', 'STOP', ''
        , 'SAFE', '', '', ''], ['B4a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['B4b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', 'BRAKE', ''], ['B4c', '', 'ALLOCATE', '', '', ''
        , '', 'STOP', '', 'SAFE', '', '', ''], ['B5a', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '', '', ''], ['B5b', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B5c', '', 'ALLOCATE'
        , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B6a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['B6b', '', ''
        , '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B6c', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B7e'
        , 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], [
        'B7d', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B7c'
        , '', '', '', '', '', '', '', '', '', '', '', ''], ['B7b', '', '', ''
        , '', '', '', '', '', '', '', 'BRAKE', ''], ['B7a', '', 'ALLOCATE'
        , '', '', '', '', 'STOP', '', 'SAFE', '', '', '']], 1, 1, 0, 25.4, 0
        , 1, 0, ['Min.', 'Low', 'Med.', 'High', 'Max.'], 3, 10, 0, 2, 1, [[
        'Stop', -1, -1, 0], ['Clear', -1, -1, 5]], 1, [['Single Head', [[
        'Red'], ['Green']], [-1, -1]]], [['HB2ccw', 'Single Head', ['HB2ccw']
        ], ['HB4cw', 'Single Head', ['']], ['HB5ccw', 'Single Head', [
        'HB5ccw']], ['HB1ccw', 'Single Head', ['HB1ccw']], ['HB5cw', 'Single '
        'Head', ['HB5cw']], ['HB4ccw', 'Single Head', ['']], ['HB1cw'
        , 'Single Head', ['HB1cw']], ['HB6cw', 'Single Head', ['HB6cw']], [
        'HB7ccw', 'Single Head', ['HB7ccw']], ['HB2cw', 'Single Head', [
        'HB2cw']], ['HB3ccw', 'Single Head', ['']], ['HB7cw', 'Single Head'
        , ['HB7cw']], ['HB3cw', 'Single Head', ['']], ['HB6ccw', 'Single '
        'Head', ['HB6ccw']]], 0, 0, 0, 1, 60000, 0, 1, 1, 2032, 3, 0, [[
        'Bell', 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
        , '', 0.0, 0.0, 87.0, [], 1.0)

    exampleTrains["Reversing Track Example"] = [
        ['T1017', 'B1', 'CW', 0, '(2(B6 [B1 B2]) $P10)', 0, 508.0, [0, 1, 0
        , 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1, 2, 1
        , 3], '1017', 0, [], 'Auto', 'B1', [1, 2, 3, 4, 5], {}], ['T1633'
        , 'B2', 'CCW', 0, '(2(B6 [B1 B2]) $P15)', 0, 304.79999999999995, [0
        , 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1
        , 2, 0, 3], '1633', 0, [], 'Auto', 'B2', [1, 2, 3, 4, 5], {}], [
        'T3023', 'B5', 'CW', 0, '(2( B7 $P10 [B1 B2] B5) $P20)', 0, 762.0, [0
        , 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1
        , 2, 0, 3], '3023', 0, [], 'Auto', 'B5', [1, 2, 3, 4, 5], {}]]
        
class ADexamples2 :

# DEFAULT DATA FOR EXAMPLE PANELS ==========================
# Examples are divided into two different classes to avoid exceeding Jython 64K limit
    examples = {}
    exampleTrains =  {}

    examples["SignalMasts Example"] = (
        '2.0 Beta', ('CCW', 'CW'), 'B30', 'B29', 1500, 0, 2, 0, 2, 2, 2
        , 30000, 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA'
        , 'CYAN'], 1, [['B01', '', '', 'HB01cw', 'HB01ccw', '', 'B01man'], [
        'B02', '', '', 'HB02cw', 'HB02ccw', '', 'B02man'], ['B03', '', ''
        , 'HB03cw', 'HB03ccw', '', 'B03man'], ['B04', '', '', 'HB04cw'
        , 'HB04ccw', '', 'B04man'], ['B05', '', '', 'HB05cw', 'HB05ccw', ''
        , 'B05man'], ['B06', '', '', 'HB06cw', 'HB06ccw', '', 'B11man'], [
        'B07', '', '', 'HB07cw', 'HB07ccw', '', 'B11man'], ['B08', '', ''
        , 'HB08cw', 'HB08ccw', '', 'B11man'], ['B09', '', '', 'HB09cw'
        , 'HB09ccw', '', 'B09man'], ['B10', '', '', 'HB10cw', 'HB10ccw', ''
        , 'B10man'], ['B11', '', 'CCW-CW', 'HB11cw', 'HB11ccw', '', 'B11man']
        , ['B12', '', 'CCW-CW', 'HB12cw', 'HB12ccw', '', 'B12man'], ['B13'
        , '', '', 'HB13cw', 'HB13ccw', '', 'B13man'], ['B14', '', ''
        , 'HB14cw', 'HB14ccw', '', 'B13man'], ['B15', '', '', 'HB15cw'
        , 'HB15ccw', '', 'B13man'], ['B16', '', '', 'HB16cw', 'HB16ccw', ''
        , 'B13man'], ['B17', '', 'CCW-CW', 'HB17cw', 'HB17ccw', '', 'B26man']
        , ['B18', '', 'CCW-CW', 'HB18cw', 'HB18ccw', '', 'B28man'], ['B19'
        , 'CCW', '', 'HB19cw', 'HB19ccw', '', 'B19man'], ['B20', 'CW', ''
        , 'HB20cw', 'HB20ccw', '', 'B20man'], ['B21', 'CW', '', 'HB21cw'
        , 'HB21ccw', '', ''], ['B22', 'CCW', '', 'HB22cw', 'HB22ccw', '', '']
        , ['B23', 'CW', '', 'HB23cw', 'HB23ccw', '', ''], ['B24', '', ''
        , 'HB24cw', 'HB24ccw', '', ''], ['B25', '', 'CW+', 'HB25cw'
        , 'HB25ccw', '', ''], ['B26', 'CCW', '', 'HB26cw', 'HB26ccw', ''
        , 'B26man'], ['B27', '', 'CCW', 'HB27cw', 'HB27ccw', '', 'B27man'], [
        'B28', '', 'CCW+', 'HB28cw', 'HB28ccw', '', 'B28man'], ['B29', ''
        , 'CCW-CW+', 'HB29cw', 'HB29ccw', '', 'B29man'], ['B30', '', ''
        , 'HB30cw', 'HB30ccw', '', 'B30man'], ['B31', '', '', 'HB31cw'
        , 'HB31ccw', '', 'B31man'], ['B32', '', '', 'HB32cw', 'HB32ccw', ''
        , 'B32man'], ['B33', 'CCW', 'CCW', 'HB33cw', 'HB33ccw', '', ''], [
        'B34', 'CW', 'CW', 'HB34cw', 'HB34ccw', '', '']], [['B1a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B1b'
        , '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B1c', ''
        , '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B1d', ''
        , 'ALLOCATE', '', '30 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
        , ['B2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '30 '
        'MPH', '', ''], ['B2b', '', '', '', '', 'BRAKE', '', '', '', '', ''
        , '', ''], ['B2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
        , ''], ['B2d', '', 'ALLOCATE', '', '30 MPH', '', '', 'STOP', ''
        , 'SAFE', '', '', ''], ['B3a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '30 MPH', '', ''], ['B3b', '', '', '', '', 'BRAKE'
        , '', '', '', '', '', '', ''], ['B3c', '', '', '', '', '', '', '', ''
        , '', '', 'BRAKE', ''], ['B3d', '', 'ALLOCATE', '', '30 MPH', '', ''
        , 'STOP', '', 'SAFE', '', '', ''], ['B4a', 'STOP', '', 'SAFE', '', ''
        , '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B4b', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', '', ''], ['B4c', '', '', '', '', ''
        , '', '', '', '', '', 'BRAKE', ''], ['B4d', '', 'ALLOCATE', '', '30 '
        'MPH', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B5a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B5b'
        , '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B5c', ''
        , '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B5d', ''
        , 'ALLOCATE', '', '30 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
        , ['B6a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', ''
        , 'BRAKE', ''], ['B6b', '', '', '', '', 'NO', '', '', '', '', ''
        , 'NO', ''], ['B6c', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP', ''
        , 'SAFE', '', '', ''], ['B7a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', 'BRAKE', ''], ['B7b', '', '', '', '', 'NO', ''
        , '', '', '', '', 'NO', ''], ['B7c', '', 'ALLOCATE', '', '', 'BRAKE'
        , '', 'STOP', '', 'SAFE', '', '', ''], ['B8a', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '', 'BRAKE', ''], ['B8b', '', '', ''
        , '', 'NO', '', '', '', '', '', 'NO', ''], ['B8c', '', 'ALLOCATE', ''
        , '', 'BRAKE', '', 'STOP', '', 'SAFE', '', '', ''], ['B9b', 'STOP'
        , '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', 'BRAKE', ''], [
        'B9a', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP', '', 'SAFE', ''
        , '', ''], ['B10a', 'NO', '', 'NO', '', '', '', '', 'NO', '', '', ''
        , ''], ['B10b', 'STOP', '', 'SAFE', '', 'NO', '', '', 'ALLOCATE', ''
        , '', 'BRAKE', ''], ['B10c', '', 'ALLOCATE', '', '', 'BRAKE', ''
        , 'STOP', '', 'SAFE', '', '', ''], ['B11a', 'STOP', '', 'NO', '', ''
        , '', '', 'ALLOCATE', '', '', 'BRAKE', ''], ['B11b', '', 'ALLOCATE'
        , '', '', 'BRAKE', '', 'STOP', '', 'NO', '', '', ''], ['B11c', '', ''
        , '', '', '', '', '', '', '', '', '', ''], ['B12a', '', '', '', ''
        , '', '', '', '', '', '', '', ''], ['B12b', 'STOP', '', 'NO', '', ''
        , '', '', 'ALLOCATE', '', '', 'BRAKE', ''], ['B12c', '', 'ALLOCATE'
        , '', '', 'BRAKE', '', 'STOP', '', 'NO', '', '', ''], ['B13a', 'STOP'
        , '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', 'BRAKE', ''], [
        'B13b', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP', '', 'SAFE', ''
        , '', ''], ['B14a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE'
        , '', '', 'BRAKE', ''], ['B14b', '', 'ALLOCATE', '', '', 'BRAKE', ''
        , 'STOP', '', 'SAFE', '', '', ''], ['B15a', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '', 'BRAKE', ''], ['B15b', ''
        , 'ALLOCATE', '', '', 'BRAKE', '', 'STOP', '', 'SAFE', '', '', ''], [
        'B16a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', ''
        , 'BRAKE', ''], ['B16b', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP'
        , '', 'SAFE', '', '', ''], ['B17a', 'STOP', 'ALLOCATE', '', '30 MPH'
        , '', '', 'STOP', 'ALLOCATE', '', '30 MPH', '', ''], ['B18a', 'STOP'
        , 'ALLOCATE', '', '30 MPH', 'BRAKE', '', 'STOP', 'ALLOCATE', '', '30 '
        'MPH', 'BRAKE', ''], ['B19a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['B19b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', 'BRAKE', ''], ['B19c', '', 'ALLOCATE', '', '', ''
        , '', 'STOP', '', 'SAFE', '40 MPH', '', ''], ['B20a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['B20b', ''
        , '', '', '', 'BRAKE', '', '', '', '', '40 MPH', '', ''], ['B20c', ''
        , '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B20d', ''
        , 'ALLOCATE', '', '40 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
        , ['B21a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['B21b', '', '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE'
        , ''], ['B21c', '', 'ALLOCATE', '', '50 MPH', '', '', 'STOP', ''
        , 'SAFE', '', '', ''], ['B22a', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['B22b', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', 'BRAKE', ''], ['B22c', '', 'ALLOCATE', '', '', ''
        , '', 'STOP', '', 'SAFE', '50 MPH', '', ''], ['B23a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['B23b', ''
        , '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B23c', ''
        , 'ALLOCATE', '', '50 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
        , ['B24a', 'STOP', '', 'NO', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['B24b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['B24c', '', '', 'SAFE', '', '', '', '', '', 'SAFE', '40 MPH', ''
        , ''], ['B24d', '', '', '', '40 MPH', '', '', '', '', '', '', 'BRAKE'
        , ''], ['B24e', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'NO', ''
        , '', ''], ['B25a', 'STOP', '', 'NO', '', '', '', '', 'NO', '', ''
        , '', ''], ['B25b', '', '', '', '', 'BRAKE', '', '', '', '', '', ''
        , ''], ['B25c', '', 'ALLOCATE', 'SAFE', 'Max.', '', '', ''
        , 'ALLOCATE', 'SAFE', '40 MPH', '', ''], ['B25d', '', '', '', '', ''
        , '', '', '', '', '', 'BRAKE', ''], ['B25e', '', 'NO', '', '40 MPH'
        , '', '', 'STOP', '', 'NO', '', '', ''], ['B26d', 'STOP', '', 'SAFE'
        , '', '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B26c', '', ''
        , '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B26b', '', '', ''
        , '', '', '', '', '', '', '', 'BRAKE', ''], ['B26a', '', 'ALLOCATE'
        , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B27d', 'STOP'
        , '', 'SAFE', '', '', '', '', 'ALLOCATE', '', 'Max.', '', ''], [
        'B27c', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B27b'
        , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B27a', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
        'B28d', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['B28c', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
        , ['B28b', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
        'B28a', '', 'ALLOCATE', '', 'Max.', '', '', 'STOP', '', 'SAFE', ''
        , '', ''], ['B29a', 'STOP', '', 'NO', '40 MPH', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['B29b', '', '', 'SAFE', '', 'BRAKE'
        , '', '', '', '', '', '', ''], ['B29c', '', '', '', '', '', '', ''
        , '', 'SAFE', '', 'BRAKE', ''], ['B29d', '', 'ALLOCATE', '', '', ''
        , '', 'STOP', '', 'NO', '', '', ''], ['B30a', 'STOP', '', 'SAFE', ''
        , '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B30b', '', '', ''
        , '', 'BRAKE', '', '', '', '', '', '', ''], ['B30c', '', '', '', ''
        , '', '', '', '', '', '', 'BRAKE', ''], ['B30d', '', 'ALLOCATE', ''
        , '30 MPH', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B31a', 'STOP'
        , '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], [
        'B31b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B31c'
        , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B31d', ''
        , 'ALLOCATE', '', '30 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
        , ['B32a', 'NO', '', 'NO', '', '', '', '', 'NO', '', '', '', ''], [
        'B32b', 'STOP', '', 'SAFE', '', 'NO', '', '', 'ALLOCATE', '', ''
        , 'BRAKE', ''], ['B32c', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP'
        , '', 'SAFE', '', '', ''], ['B33a', 'STOP', 'ALLOCATE', '', '', ''
        , '', 'STOP', 'ALLOCATE', '', '50 MPH', '', ''], ['B34a', 'STOP'
        , 'ALLOCATE', '', '40 MPH', '', '', 'STOP', 'ALLOCATE', '', '', ''
        , '']], 1, 1, 0, 1.0, 0, 1, 0, ['Min.', '30 MPH', '40 MPH', '50 MPH'
        , 'Max.'], 2, 10, 0, 2, 2, [['Stop', -1, -1, 0], ['Clear', -1, -1, 5]
        , ['Diverging Clear Limited', -1, 1, 3], ['Approach', 0, 0, 2], [
        'Diverging Approach', 0, 1, 2], ['Approach Limited', 2, 0, 3], [
        'Diverging Approach Limited', 2, 1, 4]], 1, [['Single Head', [['Red']
        , ['Green'], ['Yellow'], ['Flash-Green'], ['Flash-Yellow'], ['Green']
        , ['Green']], [-1, -1, -1, -1, -1, -1, -1]], ['Double head', [['Red'
        , 'Red'], ['Green', 'Red'], ['Red', 'Flash-Green'], ['Yellow', 'Red']
        , ['Red', 'Yellow'], ['Flash-Yellow', 'Red'], ['Red', 'Flash-Yellow']
        ], [-1, -1, -1, -1, -1, -1, -1]]], [['HB03ccw', 'Double head', [
        'HB03ccw', 'HB03ccwL']], ['HB24cw', 'Double head', ['HB24cw'
        , 'HB24cwL']], ['HB19cw', 'Single Head', ['']], ['HB22ccw', 'Double '
        'head', ['HB22ccw', 'HB22ccwL']], ['HB08ccw', 'Single Head', ['']], [
        'HB10cw', 'Single Head', ['']], ['HB05cw', 'Double head', ['HB05cw'
        , 'HB05cwL']], ['HB27ccw', 'Double head', ['HB27ccw', 'HB27ccwL']], [
        'HB23cw', 'Double head', ['HB23cw', 'HB23cwL']], ['HB14ccw', 'Double '
        'head', ['HB14ccw', 'HB14ccwL']], ['HB18cw', 'Single Head', ['']], [
        'HB33ccw', 'Single Head', ['']], ['HB01ccw', 'Double head', [
        'HB01ccw', 'HB01ccwL']], ['HB04cw', 'Double head', ['HB04cw'
        , 'HB04cwL']], ['HB19ccw', 'Double head', ['HB19ccw', 'HB19ccwL']], [
        'HB20ccw', 'Single Head', ['']], ['HB06ccw', 'Single Head', ['']], [
        'HB22cw', 'Single Head', ['']], ['HB17cw', 'Single Head', ['']], [
        'HB25ccw', 'Double head', ['HB25ccw', 'HB25ccwL']], ['HB03cw'
        , 'Double head', ['HB03cw', 'HB03cwL']], ['HB12ccw', 'Single Head', [
        '']], ['HB21cw', 'Double head', ['HB21cw', 'HB21cwL']], ['HB31ccw'
        , 'Double head', ['HB31ccw', 'HB31ccwL']], ['HB16cw', 'Single Head'
        , ['']], ['HB17ccw', 'Single Head', ['']], ['HB04ccw', 'Double head'
        , ['HB04ccw', 'HB04ccwL']], ['HB02cw', 'Single Head', ['']], [
        'HB34cw', 'Single Head', ['']], ['HB29cw', 'Double head', ['HB29cw'
        , 'HB29cwL']], ['HB23ccw', 'Single Head', ['']], ['HB09ccw', 'Single '
        'Head', ['']], ['HB20cw', 'Double head', ['HB20cw', 'HB20cwL']], [
        'HB15cw', 'Single Head', ['']], ['HB10ccw', 'Double head', ['HB10ccw'
        , 'HB10ccwL']], ['HB28ccw', 'Single Head', ['']], ['HB33cw', 'Single '
        'Head', ['']], ['HB01cw', 'Single Head', ['']], ['HB28cw', 'Double '
        'head', ['HB28cw', 'HB28cwL']], ['HB15ccw', 'Double head', ['HB15ccw'
        , 'HB15ccwL']], ['HB02ccw', 'Double head', ['HB02ccw', 'HB02ccwL']]
        , ['HB34ccw', 'Single Head', ['']], ['HB14cw', 'Single Head', ['']]
        , ['HB09cw', 'Double head', ['HB09cw', 'HB09cwL']], ['HB21ccw'
        , 'Single Head', ['']], ['HB07ccw', 'Single Head', ['']], ['HB32cw'
        , 'Single Head', ['']], ['HB27cw', 'Double head', ['HB27cw'
        , 'HB27cwL']], ['HB26ccw', 'Double head', ['HB26ccw', 'HB26ccwL']], [
        'HB13cw', 'Single Head', ['']], ['HB08cw', 'Double head', ['HB08cw'
        , 'HB08cwL']], ['HB13ccw', 'Double head', ['HB13ccw', 'HB13ccwL']], [
        'HB32ccw', 'Double head', ['HB32ccw', 'HB32ccwL']], ['HB31cw'
        , 'Double head', ['HB31cw', 'HB31cwL']], ['HB26cw', 'Single Head', [
        '']], ['HB18ccw', 'Single Head', ['']], ['HB05ccw', 'Double head', [
        'HB05ccw', 'HB05ccwL']], ['HB12cw', 'Single Head', ['']], ['HB07cw'
        , 'Double head', ['HB07cw', 'HB07cwL']], ['HB24ccw', 'Double head', [
        'HB24ccw', 'HB24ccwL']], ['HB30cw', 'Double head', ['HB30cw'
        , 'HB30cwL']], ['HB25cw', 'Double head', ['HB25cw', 'HB25cwL']], [
        'HB11ccw', 'Single Head', ['']], ['HB29ccw', 'Double head', [
        'HB29ccw', 'HB29ccwL']], ['HB30ccw', 'Double head', ['HB30ccw'
        , 'HB30ccwL']], ['HB11cw', 'Single Head', ['']], ['HB16ccw', 'Double '
        'head', ['HB16ccw', 'HB16ccwL']], ['HB06cw', 'Double head', ['HB06cw'
        , 'HB06cwL']]], 0, 1, 1, 1, 60000, 1, 1, 1, 2000, 3, 0, [['Bell'
        , 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
        , '', 0.0, 0.0, 87.0, [], 1.0)

    exampleTrains["SignalMasts Example"] = [
        ['T1017', 'B01', 'CCW', 0, '(3(B24 [B01 B02]) $P30 [B30 B31] [B01 '
        'B02])', 0, 650.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0
        , 10, 0, 1, 0, 13, 1, 0, 0, 0, 12], '1017', 0, [], 'Auto', 'B01', [1
        , 2, 2, 3, 4], {}], ['T1019', 'B02', 'CCW', 0, '(3(B24 [B02 B01]) '
        '$P30) ', 0, 680.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0
        , 0, 2, 0, 1, 0, 0, 0, 1, 3, 0, 3], '1019', 0, [], 'Auto', 'B02', [1
        , 2, 3, 4, 5], {}], ['T1633', 'B05', 'CW', 0, '(2(B20 $H:HB26ccw B24 '
        '$R:HB26ccw [B05 B04 B03]) $P15 [B31 B30] $P10 [B05 B04 B03])', 0
        , 650.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 14, 0, 1
        , 0, 17, 1, 0, 0, 0, 16], '1633', 0, [], 'Auto', 'B05', [1, 2, 3, 4
        , 5], {}], ['T1641', 'B30', 'CCW', 0, '([B02 B03 B01] B24 2([B03 B02 '
        'B01] B30) $P10)', 0, 410.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0
        , 0, 1, 0, 0, 2, 0, 1, 0, 6, 1, 0, 0, 0, 4], '1641', 0, [], 'Auto'
        , 'B30', [1, 2, 3, 4, 5], {}], ['T3023', 'B31', 'CW', 0, '(2([B05 B04'
        ' B03] B31) $P25 [B05 B04 B03] B24)', 0, 490.0, [0, 1, 0, 0, 0, 0, 0
        , 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1, 2, 1, 3, 0, 1, 0
        , 7, 1, 0, 2, 1, 5], '3023', 0, [], 'Auto', 'B31', [1, 2, 3, 4, 5], {
        }], ['T3029', 'B06', 'CW', 0, '($SWOFF $CW 2(B24 [B04 B03]) [B31 B30]'
        ' [B04 B03] B20 $SWON $H:HB06cw $R:HB07cw $CCW B06 $D3 $OFF:F0)', 0
        , 810.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 4, 0, 1
        , 0, 0, 0, 1, 2, 0, 6, 0, 1, 0, 9, 1, 0, 2, 0, 8], '3029', 0, []
        , 'Auto', 'B06', [1, 2, 3, 4, 5], {}], ['T4802', 'B07', 'CW', 0
        , '($SWOFF $CW 3(B24 [B05 B04 B03]) B20 $SWON $H:HB07cw $R:HB06cw '
        '$CCW B07 $D3 $OFF:F0)', 0, 250.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1
        , 0, 0, 0, 1, 0, 0, 4, 0, 1, 0, 0, 0, 1, 3, 0, 6, 0, 1, 0, 10, 1, 0
        , 3, 0, 8], '4802', 1, [], 'Auto', 'B07', [1, 2, 3, 4, 5], {}], [
        'T4805', 'B32', 'CCW', 0, '($SWOFF $CCW [B02 B03 B01] B24 [B02 B03 '
        'B01] [B31 B30] $SWON $H:HB32ccw B29 $CW B32 $D3 $OFF:F0)', 0, 380.0
        , [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 4, 0, 1, 0, 8
        , 1, 0, 0, 1, 6], '4805', 0, [], 'Auto', 'B32', [1, 2, 3, 4, 5], {}]]

    examples["Xing Example"] = (
        '2.0 Beta', ('CCW', 'CW'), 'N1', 'W', 1000, 0, 1, 0, 1, 2, 2, 30000
        , 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA', 'CYAN']
        , 1, [['E', '', 'CCW-CW', 'HEcw', 'HEccw', '', ''], ['N1', '', ''
        , 'HN1cw', 'HN1ccw', '', ''], ['N2', '', '', 'HN2cw', 'HN2ccw', ''
        , ''], ['NE', '', '', 'HNEcw', 'HNEccw', '', ''], ['NW', '', ''
        , 'HNWcw', 'HNWccw', '', ''], ['S1', '', '', 'HS1cw', 'HS1ccw', ''
        , ''], ['S2', '', '', 'HS2cw', 'HS2ccw', '', ''], ['SE', '', ''
        , 'HSEcw', 'HSEccw', '', ''], ['SW', '', '', 'HSWcw', 'HSWccw'
        , 'INVERTED', ''], ['W', '', 'CCW-CW', 'HWcw', 'HWccw', '', ''], [
        'Xa', '', 'CCW-CW', 'HXacw', 'HXaccw', '', ''], ['Xb', '', 'CCW-CW'
        , 'HXbcw', 'HXbccw', '', '']], [['Ed', 'STOP', '', 'SAFE', '', '', ''
        , '', 'ALLOCATE', '', '', '', ''], ['Ec', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['Eb', '', '', '', '', '', '', '', '', ''
        , '', 'BRAKE', ''], ['Ea', '', 'ALLOCATE', '', '', '', '', 'STOP', ''
        , 'SAFE', '', '', ''], ['N1d', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['N1c', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['N1b', '', '', '', '', '', '', '', '', ''
        , '', 'BRAKE', ''], ['N1a', '', 'ALLOCATE', '', '', '', '', 'STOP'
        , '', 'SAFE', '', '', ''], ['N2d', 'STOP', '', 'SAFE', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['N2c', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', '', ''], ['N2b', '', '', '', '', '', '', '', '', ''
        , '', 'BRAKE', ''], ['N2a', '', 'ALLOCATE', '', '', '', '', 'STOP'
        , '', 'SAFE', '', '', ''], ['NEc', 'STOP', '', 'NO', '', '', '', ''
        , 'ALLOCATE', '', '', '', ''], ['NEb', '', '', '', '', 'BRAKE', ''
        , '', '', '', '', 'BRAKE', ''], ['NEa', '', 'ALLOCATE', '', '', ''
        , '', 'STOP', '', 'NO', '', '', ''], ['NWc', 'STOP', '', 'NO', '', ''
        , '', '', 'ALLOCATE', '', '', '', ''], ['NWb', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['NWa', '', 'ALLOCATE'
        , '', '', '', '', 'STOP', '', 'NO', '', '', ''], ['S1d', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['S1c', '', ''
        , '', '', 'BRAKE', '', '', '', '', '', '', ''], ['S1b', '', '', ''
        , '', '', '', '', '', '', '', 'BRAKE', ''], ['S1a', '', 'ALLOCATE'
        , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['S2d', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['S2c', '', ''
        , '', '', 'BRAKE', '', '', '', '', '', '', ''], ['S2b', '', '', ''
        , '', '', '', '', '', '', '', 'BRAKE', ''], ['S2a', '', 'ALLOCATE'
        , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['SEa', 'STOP', ''
        , 'NO', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['SEb', '', ''
        , '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['SEc', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'NO', '', '', ''], ['SWa'
        , 'STOP', '', 'NO', '', '', '', '', 'ALLOCATE', '', '', '', ''], [
        'SWb', '', '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], [
        'SWc', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'NO', '', '', '']
        , ['Wd', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['Wc', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], [
        'Wb', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Wa', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['Xa'
        , 'STOP', 'ALLOCATE', '', '', '', '', 'STOP', 'ALLOCATE', '', '', ''
        , ''], ['Xb', 'STOP', 'ALLOCATE', '', '', '', '', 'STOP', 'ALLOCATE'
        , '', '', '', '']], 1, 1, 0, 25.4, 0, 1, 0, ['Min.', 'Low', 'Med.'
        , 'High', 'Max.'], 3, 10, 0, 2, 1, [['Stop', -1, -1, 0], ['Clear', -1
        , -1, 5]], 1, [['Single Head', [['Red'], ['Green']], [-1, -1]]], [[
        'HXbccw', 'Single Head', ['HXbccw']], ['HWcw', 'Single Head', ['HWcw'
        ]], ['HEcw', 'Single Head', ['HEcw']], ['HS1cw', 'Single Head', [
        'HS1cw']], ['HSWcw', 'Single Head', ['HSWcw']], ['HSEcw', 'Single '
        'Head', ['HSEcw']], ['HNWccw', 'Single Head', ['HNWccw']], ['HSWccw'
        , 'Single Head', ['HSWccw']], ['HWccw', 'Single Head', ['HWccw']], [
        'HN1cw', 'Single Head', ['HN1cw']], ['HNWcw', 'Single Head', ['HNWcw'
        ]], ['HXacw', 'Single Head', ['HXacw']], ['HNEcw', 'Single Head', [
        'HNEcw']], ['HNEccw', 'Single Head', ['HNEccw']], ['HN1ccw', 'Single '
        'Head', ['HN1ccw']], ['HSEccw', 'Single Head', ['HSEccw']], ['HS2cw'
        , 'Single Head', ['HS2cw']], ['HEccw', 'Single Head', ['HEccw']], [
        'HS1ccw', 'Single Head', ['HS1ccw']], ['HN2ccw', 'Single Head', [
        'HN2ccw']], ['HS2ccw', 'Single Head', ['HS2ccw']], ['HN2cw', 'Single '
        'Head', ['HN2cw']], ['HXaccw', 'Single Head', ['HXaccw']], ['HXbcw'
        , 'Single Head', ['HXbcw']]], 0, 0, 0, 1, 20000, 0, 1, 1, 1778, 3, 0
        , [['Bell', 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
        , '', 12.0, 0.0, 87.0, [])

    exampleTrains["Xing Example"] = [
        ['T1017', 'S1', 'CCW', 0, '([N1 N2] [S1 S2] $P10)', 0, 889.0, [0, 1, 0
        , 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 5, 1, 0, 0, 0
        , 4], '1017', 0, [], 'Auto', 'S1', [1, 2, 3, 4, 5], {}], ['T1019', 'N1'
        , 'CW', 0, '([S1 S2] $P15 [N1 N2])', 0, 1143.0, [0, 1, 0, 0, 0, 0, 0
        , 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 5, 1, 0, 0, 0, 4], '1019'
        , 0, [], 'Auto', 'N1', [1, 2, 3, 4, 5], {}], ['T1633', 'NE', 'CW', 0
        , '(SE NW [S1 S2] [N1 N2] SW NE [S1 S2] [N1 N2] )', 0
        , 355.6, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0
        , 0, 14, 0, 1, 0, 17, 1, 0, 0, 0, 16], '1633', 0, [], 'Auto', 'NE', [
        1, 2, 3, 4, 5], {}], ['T1641', 'SW', 'CW', 0, '(SW SE)', 0, 203.2, [0
        , 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 3], '1641', 0, []
        , 'Auto', 'SW', [1, 2, 3, 4, 5], {}]]

    examples["Xover Example"] = (
        '2.0 Beta', ('CCW', 'CW'), 'B1', 'B6', 1000, 0, 1, 1, 1, 2, 2, 30000
        , 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA', 'CYAN']
        , 1, [['B1', '', '', 'HB1ccw', 'HB1cw', '', ''], ['B2', '', ''
        , 'HB2ccw', 'HB2cw', '', ''], ['B3', '', '', 'HB3ccw', 'HB3cw', ''
        , ''], ['B4', '', '', 'HB4ccw', 'HB4cw', '', ''], ['B5', ''
        , 'CCW-CW+', 'HB5ccw', 'HB5cw', '', ''], ['B6', '', 'CCW-CW+'
        , 'HB6ccw', 'HB6cw', '', '']], [['B1a', 'STOP', '', 'SAFE', '', ''
        , '', '', 'ALLOCATE', '', '', '', ''], ['B1b', '', '', '', ''
        , 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B1c', '', 'ALLOCATE'
        , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B2a', 'STOP', ''
        , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['B2b', '', ''
        , '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B2c', ''
        , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B3a'
        , 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], [
        'B3b', '', '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], [
        'B3c', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
        ], ['B4a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
        , ''], ['B4b', '', '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE'
        , ''], ['B4c', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', ''
        , '', ''], ['B5a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
        , '', '', ''], ['B5b', '', '', '', '', 'BRAKE', '', '', '', '', ''
        , '', ''], ['B5c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
        , ''], ['B5d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', ''
        , '', ''], ['B6a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
        , '', '', ''], ['B6b', '', '', '', '', 'BRAKE', '', '', '', '', ''
        , '', ''], ['B6c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
        , ''], ['B6d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', ''
        , '', '']], 1, 1, 0, 25.4, 0, 1, 0, ['Min.', 'Low', 'Med.', 'High'
        , 'Max.'], 3, 10, 0, 2, 1, [['Stop', -1, -1, 0], ['Clear', -1, -1, 5]
        ], 1, [['Single Head', [['Red'], ['Green']], [-1, -1]]], [['HB2ccw'
        , 'Single Head', ['HB2ccw']], ['HB4cw', 'Single Head', ['HB4cw']], [
        'HB5ccw', 'Single Head', ['HB5ccw']], ['HB1ccw', 'Single Head', [
        'HB1ccw']], ['HB5cw', 'Single Head', ['HB5cw']], ['HB4ccw', 'Single '
        'Head', ['HB4ccw']], ['HB1cw', 'Single Head', ['HB1cw']], ['HB6cw'
        , 'Single Head', ['HB6cw']], ['HB3ccw', 'Single Head', ['HB3ccw']], [
        'HB2cw', 'Single Head', ['HB2cw']], ['HB3cw', 'Single Head', ['HB3cw'
        ]], ['HB6ccw', 'Single Head', ['HB6ccw']]], 0, 0, 0, 1, 60000, 0, 1
        , 1, 2032, 3, 0, [['Bell', 'resources/sounds/bell.wav']], [1, 1, 1, 1
        , 1, 1], '', 0.0, 0.0, 87.0, [], 1.0)

    exampleTrains["Xover Example"] = [
        ['T1017', 'B1', 'CCW', 0, '(2([B3 B4] [B1 B2]) $P15)', 0
        , 304.79999999999995, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0
        , 0, 2, 0, 1, 0, 0, 0, 1, 2, 1, 3, 0, 1, 0, 6, 1, 0, 2, 1, 5], '1017'
        , 0, [], 'Auto', 'B1', [1, 2, 3, 4, 5], {}], ['T1019', 'B2', 'CW', 0
        , '(2([B3 B4] [B1 B2]) $P20)', 0, 609.5999999999999, [0, 1, 0, 0, 0
        , 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1, 2, 1, 3, 0
        , 1, 0, 6, 1, 0, 2, 1, 5], '1019', 0, [], 'Auto', 'B2', [1, 2, 3, 4
        , 5], {}]]
                        
# MAIN PROGRAMM ==============
    
a = AutoDispatcher()
 
a.setup()
