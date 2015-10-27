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

import java
import jmri
from AutoDispatcher2.examples import *
from AutoDispatcher2.layoutComponents import *
from AutoDispatcher2.userInterface import *
from AutoDispatcher2.train import ADtrain
from apps import Apps
from java.awt import Color
from java.awt import GridLayout
from java.awt import Toolkit
from java.io import FileInputStream
from java.io import FileOutputStream
from java.io import IOException
from java.io import ObjectInputStream
from java.io import ObjectOutputStream
from java.lang import System
from java.util import Random
from javax.swing import BorderFactory
from javax.swing import BoxLayout
from javax.swing import JButton
from javax.swing import JCheckBox
from javax.swing import JLabel
from javax.swing import JOptionPane
from javax.swing import JPanel
from javax.swing import JScrollPane
from javax.swing import JTextArea
from jmri import Block
from jmri import InstanceManager
from jmri import PowerManager
from jmri import Sensor
from jmri import SignalHead
from jmri.jmrit.consisttool import ConsistToolFrame
from jmri.jmrit.roster import Roster
from jmri.util import JmriJFrame
from thread import start_new_thread
from time import sleep

# Fast Clock Listener
class FastListener(java.beans.PropertyChangeListener):
    fastTime = 0

    def propertyChange(self, event):
        time = InstanceManager.timebaseInstance().getTime()
        FastListener.fastTime = time.getHours() * 60 + time.getMinutes()
        return

# MAIN CLASS ==============

class AutoDispatcher(jmri.jmrit.automat.AbstractAutomaton):

# CONSTANTS ==========================

    version = "2.44"
    
    # Retrieve DOUBLE_XOVER constant, depending on JMRI Version
    # (LayoutTurnout class was moved to a new package, starting with JMRI 2.9.3)
    try:
        DOUBLE_XOVER = jmri.jmrit.display.layoutEditor.LayoutTurnout.DOUBLE_XOVER
    except:
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
    for key in headsAspects.keys():
        inverseAspects[headsAspects[key]] = key
    
# STATIC VARIABLES ==========================
    
    # Our unique instance
    instance = None
    # Power monitor instance
    powerMonitor = None
    
    # Fast Clock
    fastBase = InstanceManager.timebaseInstance()
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
    locomotives = [
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
    
    @staticmethod
    def getVersion():
        return AutoDispatcher.version

    @staticmethod
    def setDebug(on):
        AutoDispatcher.debug = on

    @staticmethod
    def message(text):
        # Output a message only in verbose mode
        if ADsettings.verbose:
            AutoDispatcher.log(text)
        
    @staticmethod
    def log(text):
        # Output a message, both to script main window and to log file
        AutoDispatcher.statusScroll.append("\n" + text)
        while (AutoDispatcher.statusScroll.getLineCount() >
               AutoDispatcher.MAX_LINES):
            AutoDispatcher.statusScroll.replaceRange(None, 0,
                                                     AutoDispatcher.statusScroll.getLineEndOffset(0))
        AutoDispatcher.statusScroll.setCaretPosition(
                                                     AutoDispatcher.statusScroll.getDocument().getLength())
        print text

    @staticmethod
    def chimeLog(sound, text):
        # As log, but also plays a sound
        AutoDispatcher.log(text)
        ind = ADsettings.defaultSounds[sound]
        if ADsettings.ringBell and ind > 0 and ind >= len(ADsettings.soundList):
            ADsettings.soundList[ind-1].play()

    @staticmethod
    def addEngineer(engineerName, engineerClass):
        # Add an Engineer script to our list
        if (engineerName != "Auto" or not
            AutoDispatcher.engineers.has_key(engineerName)):
            AutoDispatcher.engineers[engineerName] = engineerClass
            if AutoDispatcher.trainsFrame != None:
                AutoDispatcher.trainsFrame.reDisplay()
            if engineerName != "Auto":
                AutoDispatcher.log("Added engineer: " + engineerName)
            return True
        AutoDispatcher.log("Warning: Engineer " + engineerName 
                           + "already registered!")
        return False
    
    @staticmethod
    def removeEngineer(engineerName):
        # Remove an Engineer script from our list
        if (engineerName != "Auto" and
            AutoDispatcher.engineers.has_key(engineerName)):
            del AutoDispatcher.engineers[engineerName]
            for train in ADtrain.getList():
                if train.engineerName == engineerName:
                    train.setEngineer("Auto")
            AutoDispatcher.log("Removed engineer: " + engineerName)
            return True
        AutoDispatcher.log("Warning: Engineer " + engineerName + 
                           " not found. Cannot be removed!")
        return False

    @staticmethod
    def setTrainsDirty():
        # Take note that trains info changed
        if AutoDispatcher.trainsDirty:
            return
        AutoDispatcher.trainsDirty = True
        if not AutoDispatcher.loop:
            ADmainMenu.saveTrainsButton.enabled = True
            
    @staticmethod
    def setPreferencesDirty():
        # Take note that preferences were changed
        if AutoDispatcher.preferencesDirty:
            return
        AutoDispatcher.preferencesDirty = True
        if not AutoDispatcher.loop:
            ADmainMenu.saveSettingsButton.enabled = True

    @staticmethod
    def centerLabel(string):
        # Create a centered JLabel
        tempLabel = JLabel(string)
        tempLabel.setHorizontalAlignment(JLabel.CENTER)
        return tempLabel

    @staticmethod
    def printTime(text):
        # Debug utility.  Can be used to measure time lapsed between events
        newTime = System.currentTimeMillis()
        if AutoDispatcher.lastTime != 0:
            print (newTime - AutoDispatcher.lastTime), text
        AutoDispatcher.lastTime = newTime
    
    @staticmethod
    def cleanName(name):
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

    #Check if we are running in simulation mode 
    # (when running as statup script we must wait for connections 
    # to be established)
        for i in range(60):
            try:
                if (Apps.getConnection1().upper().find("SIMULATOR") < 0 and
                    Apps.getConnection2().upper().find("SIMULATOR") < 0 and
                    Apps.getConnection3().upper().find("SIMULATOR") < 0 and
                    Apps.getConnection4().upper().find("SIMULATOR") < 0):
                    AutoDispatcher.simulation = False
                else:
                    AutoDispatcher.simulation = True
                    if (Apps.getConnection1().upper() != "XPRESSNETSIMULATOR" < 0 and
                        Apps.getConnection2().upper() != "XPRESSNETSIMULATOR" and
                        Apps.getConnection3().upper() != "XPRESSNETSIMULATOR" < 0 and
                        Apps.getConnection4().upper() != "XPRESSNETSIMULATOR" < 0):
                        AutoDispatcher.lenzSimulation = False
                    else:
                        AutoDispatcher.lenzSimulation = True
                retrieveError = False
                break
            except:
                retrieveError = True
            self.waitMsec(1000)
        if retrieveError:
            print ("Sorry, unable to run AutoDispatcher as startup script!" +
                   "Launch it manually.")
            AutoDispatcher.error = True
            return
        
        # Now perform initialization
        # Display the main window
        AutoDispatcher.mainFrame = ADmainMenu()

            # Get layout  connectivity
        self.getLayoutData()
        if AutoDispatcher.error:
            return

        # Workout default settings
        self.defaultSettings()
         
        # Keep copy of automatic settings, to compare them with
        # user defined settings at saving time
        self.autoSections = ADsection.getSectionsTable()
        self.autoBlocks = ADsection.getBlocksTable()

        # If running in simulation mode, clear all sections before 
        # placing trains on tracks
        if AutoDispatcher.simulation:
            AutoDispatcher.log("Clearing sensors for simulation")           
            for block in ADblock.getList():
                sensor = block.getOccupancySensor()
                if sensor != None:
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
        if ADexamples1.examples.has_key(title):
            ADsettings.load(ADexamples1.examples[title])
            self.trains = ADexamples1.exampleTrains[title]
        elif ADexamples2.examples.has_key(title):
            ADsettings.load(ADexamples2.examples[title])
            self.trains = ADexamples2.exampleTrains[title]
        else:
            self.trains = []

        # Get user settings from disk (if any)
        # User settings files are named on the basis of the first word
        # in Layout Editor panel title.
        # Get first word of panel title
        firstWord = title.split()
        if len(firstWord) == 0:
            firstWord = ""
        else:
            firstWord = firstWord[0]
        baseName = ""
        # Strip out all characters but letters and numbers
        for c in firstWord:
            if c.isalnum():
                baseName += c
        # If we didn't get a valid name, use "AutoDispatcher"
        if baseName == "":
            baseName = "AutoDispatcher"
        self.settingsFile = (Roster.instance().getFileLocation() + 
                             baseName + "_AdP.bin")
        self.trainFile = (Roster.instance().getFileLocation() + 
                          baseName + "_AdT.bin")
        # Locomotive settings are not based on panel title, since there is
        # only one JMRI roster
        self.locoFile = (Roster.instance().getFileLocation() + 
                         "AutoDispatcher_Loc.bin")
        # Now try loading files
        self.loadSettings()
        self.loadLocomotives()
        self.loadTrains()
 
        # Apply user (or default) settings
        self.userSettings() 
        
        # Get data from Operations module
        # ADlocation.getOpLocations()

        # Setup completed
        # Enable buttons, unless some error occurred
        if not AutoDispatcher.error:
            AutoDispatcher.mainFrame.enableButtons(True)
            AutoDispatcher.chimeLog(ADsettings.START_STOP_SOUND, "Layout ready!")                   
        
# LAYOUT CONNECTIVITY ==============

    def getLayoutData(self):
    
        # Retrieve power monitor (will be used to check whether 
        # the layout is powered)
        AutoDispatcher.powerMonitor = ADpowerMonitor()

        # Retrieve LayoutEditor instance by searching the relevant window
        # If more than one panel are open, we will get the first one we find
        windowsList = JmriJFrame.getFrameList()
        self.layoutEditor = None
        for i in range(windowsList.size()):
            window = windowsList.get(i)
            windowClass = str(window.getClass())
            if(windowClass.find(".LayoutEditor") > 0):
                self.layoutEditor = window
                break
        if self.layoutEditor == None:
            AutoDispatcher.log("No Layout Editor window found," + 
                               " script cannot continue!")
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
                                    "Load your Layout Editor panel" + 
                                    " before running AutoDispatcher!")
            AutoDispatcher.error = True
            return
            
        # Retrieve SignalHead names from JMRI (will be used to populate menus)
        signalHeads = InstanceManager.signalHeadManagerInstance(
                                                                ).getSystemNameList()
        AutoDispatcher.signalHeadNames = [""]
        for s in signalHeads:
            # Use UserName (if available), otherwise SystemName
            signalName = InstanceManager.signalHeadManagerInstance(
                                                                   ).getSignalHead(s).getUserName()
            if signalName == None or signalName.strip() == "":
                signalName = s
            AutoDispatcher.signalHeadNames.append(signalName)
        AutoDispatcher.signalHeadNames.sort()

        # Retrieve List of SignalHeads that have an icon on the panel
        nIcons = self.layoutEditor.signalHeadImage.size()
        for i in range(nIcons):
            signalIcon = self.layoutEditor.signalHeadImage.get(i)
            signalHead = signalIcon.getSignalHead()
            if signalHead != None:
                signalName = signalHead.getUserName()
                if signalName == None:
                    signalName = signalHead.getSystemName()
                AutoDispatcher.signalIcons.append(signalName)
            # Keep note of original signal icon click mode
            AutoDispatcher.signalClick.append(
                                              [signalIcon, signalIcon.getClickMode()])

        # Retrieve sections from JMRI
        sections = InstanceManager.sectionManagerInstance().getSystemNameList()
        if sections.size() < 1:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
                                    "Layout contains no sections, script" +
                                    " cannot continue!")
            
        # Create section and block instances
        for section in sections:
            ADsection(section)
            
        if len(ADsection.getList()) == 0:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
                                    "No valid section found, script" +
                                    " cannot continue!")
            AutoDispatcher.error = True
            return
            

        # Create entries (connections between sections)
        for section in ADsection.getList():
            section.setEntries()

        # Retrieve paths (connections between blocks)
        for block in ADblock.getList():
            block.setPaths()
            
        # Retrieve Crossings and establish inter-dependecy between 
        # crossed sections
        nXings = self.layoutEditor.xingList.size()
        for i in range(nXings):
            xing = self.layoutEditor.xingList.get(i)
            # Ignore crossing if crossed blocks are not included in sections
            blockAC = ADblock.getByName(xing.blockNameAC)
            if blockAC != None:
                blockBD = ADblock.getByName(xing.blockNameBD)
                if blockBD != None:
                    sectionAC = blockAC.getSection()
                    sectionBD = blockBD.getSection()
                    # Ignore crossing if both crossed bocks belong to
                    # the same section
                    # (results can, however, be unpredictable!)
                    if sectionAC != sectionBD:
                        sectionAC.addXing(sectionBD)
                        sectionBD.addXing(sectionAC)

        # Retrieve double crossovers by scanning all tunouts
        # contained in LayoutEditor
        turnoutsNumber = self.layoutEditor.turnoutList.size()
        for i in range(turnoutsNumber):
            layoutTurnout = self.layoutEditor.turnoutList.get(i)
            # If the turnout is a double crossover, process it
            if layoutTurnout.getTurnoutType() == AutoDispatcher.DOUBLE_XOVER:
                ADxover(layoutTurnout)

        # Retrieve Layout Editor tracks and save their original width
        nTracks = self.layoutEditor.trackList.size()
        if nTracks < 1:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
                                    "Warning: layout contains no tracks!")
        else:
            for i in range(nTracks):
                track = self.layoutEditor.trackList.get(i)
                blockName = track.getBlockName()
                block = ADblock.getByName(blockName)
                # Process track only if block contained in some section
                if block != None:
                    block.addTrack(ADtrack(track))
        # Reorganize sections' directions
        # Blocks within each section are listed in arbitrary order.
        # Make sure that the order is consistent.
        # Starting from the first section, mark sections with inconsistent 
        # order as "reversed"
        self.processedSections = []
        # If all sections are connected, the following for-loop
        # is actually performed ony once
        for section in ADsection.getList():
            self.__alignSection__(section, False)
        # Find and mark reversing tracks (if any)
        self.findTransitPoints()
        
# INTERNAL METHODS OF getLayoutData ==============

    def __alignSection__(self, section, changeOrientation):
        # Internal method. Recursively aligns all sections
        # If section already processed, exit
        if section in self.processedSections:
            return
        # Take note that this section was processed (in order to avoid a loop!)
        self.processedSections.append(section)
        # Change orientation, if needed
        if changeOrientation:
            section.setReversed(not section.isReversed())
        # Scan both entries and exits
        for direction in [False, True]:
            for entry in section.getEntries(direction):
                nextSection = entry.getExternalSection()
                # Adjust orientation of each entry/exit
                self.__alignSection__(nextSection, 
                                      self.__hasOrientationChanged__(entry, direction))

    def findTransitPoints(self):
        # Find transit points on possible reversing tracks
        # Train color will change when transiting these points
        for section in ADsection.getList():
            # Scan both entries and exits
            for direction in [False, True]:
                for entry in section.getEntries(direction):
                    change = self.__hasOrientationChanged__(entry, direction)
                    entry.setDirectionChange(change)

    def __hasOrientationChanged__(self, startEntry, direction):
        # Internal method. 
        # Checks if two sections are aligned, by verifying
        # if the start section is included in the entries of 
        # destination section (keeping into account direction)
        startSection = startEntry.getInternalSection()
        startBlock = startEntry.getInternalBlock()
        endSection = startEntry.getExternalSection()
        endBlock = startEntry.getExternalBlock()
        for endEntry in endSection.getEntries(not direction):
            if (endEntry.getExternalSection() == startSection and
                endEntry.getExternalBlock() == startBlock and
                endEntry.getInternalBlock() == endBlock):
                # section found, directions are consistent
                return False
        # section not found, directions are inconsistent
        return True                    
    
# DEFAULT SETTINGS ==============

    def defaultSettings(self):
        # Set sections' settings to default value
        # They may then be overriden by user (or by settings stored on disk)           
        # Mark transit-only sections
        # (Sections where trains cannot stop without blocking traffic)
        for section in ADsection.getList():
            section.setDefault()

# USER SETTINGS ==============
 
    def userSettings(self):

    # Override default settings with user defined settings loaded from disk
    # (or embedded settings for demo panels)
    
        # First of all define direction names, based on user choices
        if ADsettings.ccwStart.strip() != "":
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
        for section in ADsection.getList():
            if not section.isManual():
                section.setManual(False)

        # Set user defined brake, stop, assignment and safe-point blocks
        ADsection.putBlocksTable()

        # Retrieve info from JMRI roster and create locomotives with 
        # standard speeds
        AutoDispatcher.log("Getting JMRI locomotives roster")           
        jmriRoster = Roster.instance().matchingList(None, None, None, None,
                                                    None, None, None)
        for i in range(jmriRoster.size()):
            name = jmriRoster.get(i).getId()
            address = int(jmriRoster.get(i).getDccAddress())
            ADlocomotive(name, address, None, True)
            # Roster contains also long address indicator
            # We should likely use it but, getThrottle method
            # of AbstractAutomaton don't cares!

        # Get consists from JMRI
        # Any consist must be defined before starting AutoDispatcher
        consistMan = InstanceManager.consistManagerInstance()
        consists = consistMan.getConsistList()
        # Any consist?
        if len(consists) == 0:
            # No, make sure consist file was loaded
            consistFrame = ConsistToolFrame()
            # and retry
            consists = consistMan.getConsistList()
            consistFrame.dispose()
        for a in consists:
            c = consistMan.getConsist(a)
            # Get consist address
            a = a.getNumber()
            # Get consist ID
            i = c.getConsistID()
            # Create locomotive
            l = ADlocomotive(i, a, None, True)
            # Get address of first locomotive in consist
            ll = c.getConsistList()
            if len(ll) > 0:
                f = ll[0].getNumber()
                if f != a:
                    # Record address of first locomotive
                    # Function commands will be sent to it.
                    # May need revising. 
                    l.leadLoco = f

        # Match jmri roster with user defined locomotives (and consists) and set 
        # relevant speeds
        for l in AutoDispatcher.locomotives:
            ll = ADlocomotive.getByName(l[0])
            if ll == None:
                ll = ADlocomotive(l[0], l[1], l[2], False)
            else:
                ll.setSpeedTable(l[2])
            ll.setMomentum(l[3], l[4])
            ll.runningTime = l[5]
            ll.mileage = l[6]
        # Now build trains roster, based on user settings
        if len(self.trains) > 0:
            AutoDispatcher.log("Placing trains on tracks :-)")
            ADtrain.buildRoster(self.trains)
 
    def setDirections(self):
        # Find mapping between internal direction and user defined direction
        # To this purpose, two sections are specified, the shorter route 
        # between them is assumed to be CCW direction (or whatever name user 
        # chose). Return True if the selection was successful.
        
        # Check correctness of section names
        result = True
        startSection = ADsection.getByName(ADsettings.ccwStart)
        if startSection == None:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Unknown start section " + ADsettings.ccwStart 
                                    + " in direction definition")
            ADsettings.ccwStart = ""
            result = False
        endSection = ADsection.getByName(ADsettings.ccwEnd)
        if endSection == None:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Unknown end section " + ADsettings.ccwEnd
                                    + " in direction definition")
            ADsettings.ccwEnd = ""
            result = False
        if result:
            # Find route in direction 0
            route0 = ADautoRoute(startSection, endSection, 0, False)
            # Find route in direction 1
            route1 = ADautoRoute(startSection, endSection, 1, False)
            # Which one is shorter?
            len0 = len(route0.step)
            len1 = len(route1.step)
            if len0 == 0 or (len1 != 0 and len1 < len0):
                ADsettings.ccw = 1
            else:
                ADsettings.ccw = 0
        return result

    def setSignals(self):
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
        for s in ADsection.getList():
            # In both directions
            for i in range(2):
                newName = "H" + s.getName() + extension[i]
                # Check if a SignalMast or a SignalHead with such a name exists
                newSignal = ADsignalMast.getByName(newName)
                newHead = InstanceManager.signalHeadManagerInstance(
                                                                    ).getSignalHead(newName)
                # Assign a new SignalMast only if it was not yet assigned
                # or it was automatically created in a previous call
                # (provided we found a replacement!) Replacing signals
                # is needed since user could change direction names.
                if (s.signal[i] == None or (s.signal[i].getName() != newName and
                    not s.signal[i].hasHead() and s.signal[i].inUse < 2 and 
                    s.signal[i].signalType == ADsettings.signalTypes[0] and
                    (newSignal != None or newHead != None))):
                    # If a SignalMast was cretaing using the old direction name
                    # delete it
                    if s.signal[i] != None:
                        s.signal[i].changeUse(-2)
                    # Now assign/create the new signal
                    if newSignal == None:
                        s.signal[i] = ADsignalMast.provideSignal(newName)
                    else:
                        s.signal[i] = newSignal
                    # Take note that the signal is being used
                    s.signal[i].changeUse(1)
            # Try and find the (optional) sensor to set the section under 
            # manual control
            if s.manualSensor == None:
                sensorName = s.getName() + "man"
                if sensorName in ADsection.sensorNames:
                    s.manualSensor = InstanceManager.sensorManagerInstance(
                                                                           ).getSensor(sensorName)

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
        for section in ADsection.getList():
            section.occupied = True
            section.empty = False
            section.checkOccupancy()
            section.occupied = False
            if not section.empty:
                section.setOccupied()
        # Force occupation of sections assigned to trains
        for train in ADtrain.getList():
            for section in train.previousSections:
                section.empty = not section.occupied
                section.occupied = True
                section.allocate(train, train.direction)
            if (train.destination != train.lastRouteSection and
                train.lastRouteSection != None):
                train.lastRouteSection.allocate(train, train.direction)
        # Set initial width of tracks
        for block in ADblock.getList():
            block.adjustWidth()
        # Place/remove train names in jmri Blocks
        # (only if user enabled this option)
        if ADsettings.blockTracking:
            for section in ADsection.getList():
                section.changeTrainName()
        # Set click-mode of panel SignalHeadIcons to "alternate held"
        # to avoid that user may change signal aspects
        for s in AutoDispatcher.signalClick:
            s[0].setClickMode(2)        
        # Set variables for the handle method
        self.train = 0
            # Force updating of LayoutEditor panel
        AutoDispatcher.repaint = True
        # Enable user interface buttons (unless errors occurred)
        if not AutoDispatcher.error:
            AutoDispatcher.mainFrame.enableButtons(False)
            if AutoDispatcher.simulation:
                # Separate thread, advancing trains in simulation mode
                start_new_thread(self.autoStep, ())
            # Separate thread, controlling trains speed
            start_new_thread(self.speedControl, ())
            # Separate thread, controlling locomotives maintenance time
            start_new_thread(self.maintenanceControl, ())
            # Separate thread, blinking signal head icons on panle
            start_new_thread(self.blinkSignals, ())
            # If power is off we start in "paused" mode
            if ADpowerMonitor.powerOn:
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Have fun!")
            else:
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Power Off!")
                ADpowerMonitor.savePause = True
                self.stopAll()

# BACKGROUND TASK ==============

    def handle(self):
        # Handle thread.  Runs in background and takes care of trains departure
        # Exit if there was an error or user clicked "STOP" button
        if AutoDispatcher.error or not AutoDispatcher.loop:
            # Cleanup before exiting
            # Wait for all trains to stop
            self.waitForStop()
            # Release engineers and throttles (if any)
            for t in ADtrain.getList():
                if t.engineer != None:
                    t.releaseEngineer()
                locomotive = t.locomotive
                if locomotive != None:
                    locomotive.releaseThrottle()
            # Inform other threads that program is exiting
            AutoDispatcher.stopped = True
            # Remove listeners
            ADblock.removeListeners()
            ADsection.removeListeners()
            AutoDispatcher.fastBase.removeMinuteChangeListener(AutoDispatcher.fastListener)
            # Restore original block colors and track width
            for block in ADblock.getList():
                block.restore()
            self.layoutEditor.redrawPanel()
            # Restore original click mode of signalHeadIcons on panel
            for s in AutoDispatcher.signalClick:
                s[0].setClickMode(s[1])
            # Are we quitting?
            if AutoDispatcher.exiting:
                # Yes, give user the possibility of saving settings and 
                # trains position
                self.saveBeforeExit()
            else:
                # Script not quitting yet
                # Allow user to change settings
                AutoDispatcher.mainFrame.enableButtons(True)
                # Allow user to restart operations, unless errors occurred
            if not AutoDispatcher.error:
                ADmainMenu.startButton.enabled = True
                AutoDispatcher.chimeLog(ADsettings.START_STOP_SOUND, "Goodbye!")
            return 0
        # Normal loop
        # Redraw Layout Editor panel, if needed
        if AutoDispatcher.repaint:
            AutoDispatcher.repaint = False
            self.layoutEditor.redrawPanel()
        # Start trains that are eligible (unless script is paused)
        if not AutoDispatcher.paused:
            # At each iteration we try and start one train
            if len(ADtrain.trains) > 0:
                if self.train >= len(ADtrain.trains):
                    self.train = 0
                ADtrain.trains[self.train].startIfReady()
                self.train += 1
        # Wait for a while, letting other threads do their work
        self.waitMsec(100)
        return 1

    def waitForStop(self):
        # Wait until all trains halt (unless script stopped)
        if AutoDispatcher.stopped:
            return
        # Make sure script is not paused, otherwise we risk to wait forever
        wasPaused = AutoDispatcher.paused
        self.resume()
        # If we are in simulation mode, make sure layout is powered
        if AutoDispatcher.simulation:
            AutoDispatcher.powerMonitor.setPower(PowerManager.ON)
        # Wait only if layout is powered
        # This can actually result in inconsistent situations, but
        # we can neither switch power on (causing shorts or collisions)
        # neither wait here forever!
        if ADpowerMonitor.powerOn:
            for t in ADtrain.trains:
                # Lets wait if locomotive's speed is > 0, or
                # running indicator is set (if the train is being run
                # manually or by another script, we may not know
                # which locomotive is used), or
                # train is being started right now
                while (t.running or (t.locomotive != None and
                       t.locomotive.getThrottleSpeed() > 0) or ADtrain.turnoutsBusy):
                    self.waitMsec(100)
                    # Since train is running, occupation state of blocks and
                    # sections may change and the panel may need redrawing
                    if AutoDispatcher.repaint:
                        AutoDispatcher.repaint = False
                        self.layoutEditor.redrawPanel()
        # Set script into "pause mode", if it was so when this method was called
        if wasPaused:
            self.stopAll()

    def stopAll(self):
        # Stop trains (or remove power) when paused or an error occurs
        # Ignore, if user disabled "Pause" option
        if(ADsettings.pauseMode == ADsettings.IGNORE or
           AutoDispatcher.exiting):
            return
        # Make sure we are not called more than once 
        # (owing to Jython lack of synchronization)
        if not ADmainMenu.resumeButton.enabled:
            ADmainMenu.resumeButton.enabled = True
            ADmainMenu.pauseButton.enabled = False
            ADmainMenu.stopButton.enabled = False
            # Did user chose to power-off layout?
            if(ADsettings.pauseMode == ADsettings.POWER_OFF):
                # Stop layout
                AutoDispatcher.paused = True
                AutoDispatcher.powerMonitor.setPower(PowerManager.OFF)
            else:
                # STOP_TRAINS case
                for train in ADtrain.getList():
                    train.pause()
        AutoDispatcher.paused = True

    def resume(self):
        # Resume script after a pause
        # Ignore if we were not paused
        if not AutoDispatcher.paused:
            return
        AutoDispatcher.paused = False
        if ADsettings.pauseMode == ADsettings.POWER_OFF:
            # Switch power ON
                AutoDispatcher.powerMonitor.setPower(PowerManager.ON)
        else:
            # or restart trains
            for train in ADtrain.getList():
                train.resume()
        ADmainMenu.resumeButton.enabled = False
        ADmainMenu.pauseButton.enabled = True
        ADmainMenu.stopButton.enabled = AutoDispatcher.loop
            
    def saveBeforeExit(self):
        # Give user the possibility of saving settings and trains position
        # before quitting
        # Check if trains settings or position were changed
        if AutoDispatcher.trainsDirty:
            # Yes, allow user to save them
            msg = "Save trains settings and positions before quitting? "
            if not ADpowerMonitor.powerOn:
                msg += ("(Warning, power is off and trains position could"
                        + " be inconsistent!)")
            if (JOptionPane.showConfirmDialog(None, msg, "Confirmation",
                JOptionPane.YES_NO_OPTION) == 0):
                self.saveTrains()
                self.saveSettings()
                return
        # Check if settings were changed
        if AutoDispatcher.preferencesDirty:
            # Yes, allow user to save them
            if (JOptionPane.showConfirmDialog(None, 
                "Save preferences before quitting? ", "Confirmation", 
                JOptionPane.YES_NO_OPTION) == 0):
                self.saveSettings()

# SPEED CONTROL TASK ==============

    def speedControl(self):
        # Handle to control train speeds
        # Executed in a separate thread
        # Loops until the background handle exits
        while not AutoDispatcher.stopped:
            # Take current time - Will be used when testing
            # for stalled trains and to avoid throttle timeout
            # (on some command stations)
            currentTime = System.currentTimeMillis()
            # Check all trains
#            if not AutoDispatcher.paused :
    for train in ADtrain.getList():
        # Check for train stalled
        if (train.running and ADsettings.stalledDetection !=
            ADsettings.DETECTION_DISABLED and train.lastMove != -1L
            and not AutoDispatcher.paused
            and not AutoDispatcher.simulation):
            if (currentTime - train.lastMove >
                ADsettings.stalledTime):
                train.lastMove = -1L
                if (ADsettings.stalledDetection ==
                    ADsettings.DETECTION_PAUSE):
                    AutoDispatcher.instance.stopAll()
                AutoDispatcher.chimeLog(ADsettings.STALLED_SOUND,
                                        "Train " + train.getName() + " stalled in section \""
                                        + train.section.getName() + "\"")
        # Take care of speed changes only for trains with locomotive 
        # and throttle
        locomotive = train.locomotive
        if locomotive != None and locomotive.throttle != None:
            # Do nothing if train already running at target speed.
            # Compare speeds rounding them, to cope with different 
            # precisions in Jython (double) and Java (float)
            targetSpeed = round(locomotive.targetSpeed, 4)
            presentSpeed = round(locomotive.currentSpeed, 4)
            if presentSpeed < 0:
                presentSpeed = 0
            if presentSpeed != targetSpeed:
                # Train not running at target speed
                # Should we stop train ?
                if targetSpeed < 0:
                    # Yes - Stop it immediately!
                    if presentSpeed > 0:
                        locomotive.throttle.setSpeedSetting(targetSpeed)
                        locomotive.rampingSpeed = locomotive.currentSpeed = 0
                        locomotive.currentSpeedSwing.setText("0")
                        locomotive.updateMeter()
                else:
                    # Should we increase or decrease speed?
                    if presentSpeed < targetSpeed:
                        # Acceleration
                        delta = locomotive.accStep
                    else:
                        # Deceleration (adjusted for self-learning)
                        delta = (-locomotive.decStep -
                                 locomotive.decAdjustment)
                    # Should we progressively vary speed?
                    if delta == 0:
                        # No - Apply target speed
                        locomotive.throttle.setSpeedSetting(targetSpeed)
                        locomotive.currentSpeed = targetSpeed
                        locomotive.lastSent = currentTime
                        locomotive.rampingSpeed = targetSpeed
                        if targetSpeed <= 0:
                            locomotive.updateMeter()
                        locomotive.currentSpeedSwing.setText(
                                                             str(targetSpeed))
                    else:
                        # Progressive speed change
                        delta *= ADsettings.speedRamp
                        locomotive.rampingSpeed += delta
                        # Make sure speed is within target 
                        if ((locomotive.rampingSpeed > targetSpeed 
                            and delta > 0) or (locomotive.rampingSpeed <
                            targetSpeed and delta < 0)):
                            locomotive.rampingSpeed = targetSpeed
                        # Were we braking in self-learning mode?
                        if locomotive.brakeAdjusting:
                            #Yes, did we reach target (i.e. minimum) 
                            # speed?
                            if locomotive.rampingSpeed == targetSpeed:
                                # Inform self-learning method
                                locomotive.learningEnd()
                        # Apply new speed only if change is meaningful
                        locomotive.rampingSpeed = round(
                                                        locomotive.rampingSpeed, 4)
                        # Round values based on number of speed steps
                        # used by the locomotive
                        if (round(float(locomotive.rampingSpeed) *
                            locomotive.stepsNumber) !=
                            round(float(presentSpeed) *
                            locomotive.stepsNumber)):
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
                            if targetSpeed <= 0:
                                locomotive.updateMeter()
            # If requested, periodically change speed (even if not 
            # needed), in order to avoid time-out.
            # Some command stations stop a locomotive if it did not
            # receive commands for a while.
            if (locomotive.rampingSpeed > 0 and ADsettings.maxIdle 
                > 0 and currentTime - locomotive.lastSent >
                ADsettings.maxIdle):
                locomotive.throttle.setSpeedSetting(
                                                    locomotive.rampingSpeed)
                locomotive.currentSpeed = locomotive.rampingSpeed
                locomotive.lastSent = currentTime
    # Wait n/10 of second before repeating
    # This thread is no time critical, since it only varies speeds 
    # of running trains .
    # Trains stopping is dealt with by block's ChangeListener
    sleep(float(ADsettings.speedRamp) * 0.1)

# LOCOMOTIVES MAINTENANCE TIME CONTROL TASK ==============

    def maintenanceControl(self):
        # Periodically check if any locomotive exceedes the maximum
        # mileage or operation time 
        while not AutoDispatcher.stopped:
            # Convert hours to milliseconds
            intTime = int(ADsettings.maintenanceTime * 3600000.)
            for l in ADlocomotive.getList():
                newWarnedTime = newWarnedMiles = False
                if (ADsettings.maintenanceTime > 0. 
                    and l.runningTime > intTime):
                    newWarnedTime = True
                if (ADsettings.maintenanceMiles > 0. 
                    and l.mileage > ADsettings.maintenanceMiles):
                    newWarnedMiles = True
                # make sure the warning is given only once
                if (not l.warnedTime and not l.warnedMiles and
                    (newWarnedTime or newWarnedMiles)):
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, 
                                            "Locomotive " + l.getName()  + " needs maintenance")
                l.warnedTime = newWarnedTime
                l.warnedMiles = newWarnedMiles
                # If mileage and opertions time are being displayed,
                # update swing fields
                if AutoDispatcher.locosFrame != None:
                    l.outputMileage()
            # Not time critical: wait for one minute
            sleep(60.)

    def blinkSignals(self):
        # Separate thread to blink flashing signals on LayoutEditor panel
        # (does not affect blinking of model signals on layout)
        # Quit if there are no Signal Heads on the panel
        if len(self.layoutEditor.signalHeadImage) == 0:
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
        if newIcons:
            # New JMRI version (use getIcon, setIcon)
            # Get aspect names
            rbean = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle")
            for i in range(len(aspectNames)):
                aspectNames[i] = rbean.getString(aspectNames[i])
            for s in self.layoutEditor.signalHeadImage:
                a = []
                for i in range(7):
                    a.append(s.getIcon(aspectNames[i]))
                signals.append([s, s.getIcon(aspectNames[8]), a])
        else:
            # Old JMRI version (use getRedIcon... setRedIcon...)
            # Is Lunar aspect (introduced since JMRI 2.7.8) supported?
            hasLunar = callable(getattr(self.layoutEditor.signalHeadImage[0],
                                "getFlashLunarIcon", None))
            for s in self.layoutEditor.signalHeadImage:
                a = []
                a.append([s.getRedIcon(), s.getFlashRedIcon()])
                a.append([s.getGreenIcon(), s.getFlashGreenIcon()])
                a.append([s.getYellowIcon(), s.getFlashYellowIcon()])
                if hasLunar:
                    a.append([s.getLunarIcon(), s.getFlashLunarIcon()])
                signals.append([s, s.getDarkIcon(), a])
        on = True
        cycling = False
        # Loop until handle exits
        while not AutoDispatcher.stopped:
            # Did user enable flashing?
            if ADsettings.flashingCycle > 0:
                cycling = True
                for s in signals:
                    a = s[2]
                    # Light-on half cycle?
                    if on:
                        # Yes - switch on signal head
                        if newIcons:
                            for i in range(3):
                                if a[i + 4] != None:
                                    s[0].setIcon(aspectNames[i + 4], a[i])  
                        else:
                            s[0].setFlashRedIcon(a[0][0])
                            s[0].setFlashGreenIcon(a[1][0])
                            s[0].setFlashYellowIcon(a[2][0])
                            if hasLunar:
                                s[0].setFlashLunarIcon(a[3][0])
                    else:
                        # No - set signal head to dark aspect
                        if newIcons:
                            for i in range(3):
                                if a[i + 4] != None:
                                    s[0].setIcon(aspectNames[i + 4], s[1])  
                        else:
                            s[0].setFlashRedIcon(s[1])
                            s[0].setFlashGreenIcon(s[1])
                            s[0].setFlashYellowIcon(s[1])
                            if hasLunar:
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
            elif cycling > 0:
                # Yes - take note we disable it
                cycling = False
                # restore flashing icon in signal heads
                for s in signals:
                    a = s[2]
                    if newIcons:
                        for i in range(3):
                            if a[i + 4] != None:
                                s[0].setIcon(aspectNames[i + 4], a[i + 4]) 
                    else:
                        s[0].setFlashRedIcon(a[0][1])
                        s[0].setFlashGreenIcon(a[1][1])
                        s[0].setFlashYellowIcon(a[2][1])
                        if hasLunar:
                            s[0].setFlashLunarIcon(a[3][1])
                # Force panel redrawing
                AutoDispatcher.repaint = True
            # Wait a second
            sleep(1.0)
        # Exiting thread
        # Restore original aspects
        for s in signals:
            a = s[2]
            if newIcons:
                for i in range(3):
                    if a[i + 4] != None:
                        s[0].setIcon(aspectNames[i + 4], a[i + 4]) 
            else:
                s[0].setFlashRedIcon(a[0][1])
                s[0].setFlashGreenIcon(a[1][1])
                s[0].setFlashYellowIcon(a[2][1])
                if hasLunar:
                    s[0].setFlashLunarIcon(a[3][1])
        # Force panel redrawing
        AutoDispatcher.repaint = True

# I/O =================

    # Data are stored as Java objects.
    # Saving them as XML file would be nicer, but would
    # require adding new DTD files in JMRI
    
    def saveSettings(self):
        # Save settings to disk
        # Get current settings
        newSections = ADsection.getSectionsTable()
        newBlocks = ADsection.getBlocksTable()
        # Compare current settings with those
        # automatically computed by the program at startup
        for i in range(len(newSections)):
            newS = newSections[i]
            autoS = self.autoSections[i]
            for j in range(1, 3):
                # Is user selection different from settings computed by program?
                if newS[j] != autoS[j]:
                    # Yes - if new settings are "No selection",
                    # set value to "NO"
                    if newS[j] == "":
                        newS[j] = "NO"
        for i in range(len(newBlocks)):
            newS = newBlocks[i]
            autoS = self.autoBlocks[i]
            for j in range(1, len(autoS)):
                if j == 6 or j == 12:
                    continue
                # Is user selection different from settings computed by program?
                if newS[j] != autoS[j]:
                    # Yes - if new settings are "No selection", 
                    # set value to "NO"
                    if newS[j] == "":
                        newS[j] = "NO"
        # Write to file
        try:
            outs = ObjectOutputStream(FileOutputStream(self.settingsFile))
            try:
                ADsettings.save(outs, newSections, newBlocks)
                ADmainMenu.saveSettingsButton.enabled = False
                AutoDispatcher.preferencesDirty = False
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                        "Preferences saved to disk")
            finally:
                outs.close()        
        except IOException, ioe:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Error writing to file " + self.settingsFile)

    def loadSettings(self):
        # Get settings from disk
        ins = None
        try:
            ins = ObjectInputStream(FileInputStream(self.settingsFile))
            try:
                ADsettings.load(ins.readObject())
            except IOException, ioe:
                AutoDispatcher.log("Warning, could not read file " +
                                   self.settingsFile + ". Default layout settings will be used.")
            ins.close()
            ins = None
        except IOException, ioe:
            if ins != None:
                ins.close()

    def saveLocomotives(self):
        locomotives = []
        locos = ADlocomotive.getNames()
        locos.sort()
        # Extract persistent info
        for l in locos:
            ll = ADlocomotive.getByName(l)
            locomotives.append([ll.name, ll.address, ll.speed,
                               ll.acceleration, ll.deceleration, ll.runningTime, ll.mileage])
        # Write to file
        try:
            outs = ObjectOutputStream(FileOutputStream(self.locoFile))
            try:
                outs.writeObject(locomotives)
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                        "Locomotives changes saved to disk")
            finally:
                outs.close()        
        except IOException, ioe:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Error writing file " + self.locoFile)

    def loadLocomotives(self):
        try:
            ins = ObjectInputStream(FileInputStream(self.locoFile))
            try:
                newLocomotives = ins.readObject()
                AutoDispatcher.locomotives = newLocomotives
            except IOException, ioe:
                AutoDispatcher.log("Warning, could not read file " +
                                   self.locoFile + " Default settings will be used.")
            ins.close()
        except IOException, ioe:
            return
            
    def saveTrains(self):
        # First of all, save locomotives
        self.saveLocomotives()
        trains = []
        # Extract persistent info
        for t in ADtrain.getList():
            if t.direction == ADsettings.ccw:
                direction = ADsettings.directionNames[0]
            else:
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
            if t.section != None:
                sectionName = t.section.getName()
            else:
                sectionName = ""
            # Get pending commands (if any)
            pendingCommands = []
            for i in range(len(t.itemSections)):
                section = t.itemSections[i].getName()
                action = t.items[i].action
                value = t.items[i].value
                message = t.items[i].message
                # Convert instances (sections or signals) to names
                if (action == ADschedule.WAIT_FOR or
                    action == ADschedule.MANUAL_OTHER or
                    action == ADschedule.HELD or
                    action == ADschedule.RELEASE or
                    action == ADschedule.IFH):
                    value = value.getName()
                pendingCommands.append([section, action, value, message])
            # Get name of last section on the route
            if t.lastRouteSection == None:
                lastSection = ""
            else:
                lastSection = t.lastRouteSection.getName()
            # Add a record
            trains.append([t.name, sectionName, direction, 
                          t.resistiveWheels, t.schedule.text, t.trainAllocation, 
                          t.trainLength, stack, t.locoName, t.reversed,
                          pendingCommands, t.engineerName, lastSection, t.trainSpeed,
                          t.brakingHistory, t.canStopAtBeginning, t.startAction])
        # Write everything to file
        try:
            outs = ObjectOutputStream(FileOutputStream(self.trainFile))
            try:
                outs.writeObject(trains)
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                        "Trains status saved to disk")
            finally:
                outs.close()        
        except IOException, ioe:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Error writing to file " + self.trainFile)
        ADmainMenu.saveTrainsButton.enabled = AutoDispatcher.trainsDirty = False
 
    def loadTrains(self):
        # Read train data into memory
        # Appropriate data conversions will be performed later by
        # ADtrain.buildRoster method
        try:
            ins = ObjectInputStream(FileInputStream(self.trainFile))
            try:
                newTrains = ins.readObject()
                self.trains = newTrains
            except IOException, ioe:
                AutoDispatcher.log("Warning, could not read file " +
                                   self.trainFile + ", Default train settings will be used.")
            ins.close()
        except IOException, ioe:
            return
                
    # SIMULATION =================

    # The following methods are used only in simulation mode

    def oneStep(self):
        # Advance every train of one block, unless it
        # reached the stop block in front of a red signal.
        # Ignore calls if stopped, paused or without power
        if (AutoDispatcher.stopped or AutoDispatcher.paused or not
            ADpowerMonitor.powerOn):
            return
        # Move running trains one block ahead
        for t in ADtrain.getList():
            # Did train advance during previous call?
            if t.simSensor != None:
                # Yes, remove it from previous block
                t.simSensor.setKnownState(INACTIVE)
                t.simSensor = None
                continue
            # No - Is train running?
            if (not t.running or (t.locomotive != None and
                t.engineerSetLocomotive != None and
                t.locomotive.getThrottleSpeed() <= 0)):
                continue
            # Is the train anywhere?
            section = t.section
            direction = t.direction
            if section != None:
                # Yes, find present block
                blocks = section.getBlocks(direction)
                ind = -1
                for i in range(len(blocks)):
                    if blocks[i].getOccupancy() == Block.OCCUPIED:
                        ind = i
                        break
                if ind < 0:
                    continue
                # Did train reach a stop block in front of a red signal?
                if (blocks[ind] == section.stopBlock[direction] and
                    section.getSignal(direction).getIndication() == 0):
                    continue
                # No - get sensor of current block
                currentSensor = blocks[ind].getOccupancySensor()
                # Is this block section's exit block?
                block = None
                for e in t.entriesAhead:
                    if e.getInternalBlock() == blocks[ind]:
                        # Yes - Get entry block in the next section
                        block = e.getExternalBlock()
                        while e in t.entriesAhead:
                            t.entriesAhead.pop(0)
                        break
                if block == None:
                    # Train did not reach the exit block
                    # Get next block in the same section
                    ind += 1
                    # Should not occur, but it's better to check
                    if ind >= len(blocks):
                        continue
                    block = blocks[ind]
            # Get next sensor
                nextSensor = block.getOccupancySensor()
                # Move train from current block to next one,
                # making sure the block has a sensor!
                if nextSensor != currentSensor and nextSensor != None:
                    nextSensor.setKnownState(ACTIVE)
                    # Take note that next time we must release the old block
                    t.simSensor = currentSensor

    def autoStep(self):
        # Step trains forward every two seconds
        while not AutoDispatcher.stopped:
            # Call "oneStep" once per second
            # actual advancement will occur once every two seconds,
            # since during one call next block is occupied
            # and during next call previous block is released
            sleep(1.0)
            if ADmainMenu.autoButton.isSelected():
                self.oneStep()

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

