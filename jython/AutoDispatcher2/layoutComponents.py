from AutoDispatcher2.autoDispatcher import *
from java.awt import Color
from java.beans import PropertyChangeListener
from java.lang import System
from java.util import Locale
from javax.swing import JComboBox
from javax.swing import JLabel
from javax.swing import JRadioButton
from javax.swing import JTextField
from jmri import Block
from jmri import DccThrottle
from jmri import InstanceManager
from jmri import SignalHead
from jmri import Turnout
from jmri import PowerManager
from jmri.jmrit import Sound
from jmri.jmrit import XmlFile
from math import sqrt
from thread import start_new_thread
from time import sleep

class ADentry:
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
        if blockName == None:
            blockName = entry.getBlock().getSystemName()
        self.internalBlock = ADblock.getByName(blockName)
        # Skip entry if entry block not included in any section
        if self.internalBlock == None:
            return
        # Skip entry if entry block not included in this section
        # (should not occur!)
        if self.internalBlock.getSection() != section:
            return
        blockName = entry.getFromBlock().getUserName()
        if blockName == None:
            blockName = entry.getFromBlock().getSystemName()
        # Skip entry if external block not included in any section
        # (it may occur)
        self.externalBlock = ADblock.getByName(blockName)
        if self.externalBlock == None:
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
        
    def addXover(self, xOver):
        self.xOver.append(xOver)

    def areXoversAvailable(self):
        # Check if all Xovers of the route (if any) are available
        internalSection = self.internalBlock.getSection()
        for xOver in self.xOver:
            if not xOver.isAvailable(internalSection):
                return False
        return True

# BLOCK ==============
    
class ADblock (PropertyChangeListener):
    # Encapsulates JMRI Block and LayoutBlock, adding fields and methods 
    # used by AutoDispatcher

    # STATIC VARIABLES
    
    userNames = {}       # dictionary of blocks by userName
    systemNames = {}     # dictionary of blocks by systemName
    blocksList = {}     # dictionary of blocks by layoutBlock
    # Keep a count of how many blocks have a defined length and how many 
    # do not have it
    blocksWithLength = 0
    blocksWithoutLength = 0

    @staticmethod
    def getList():
        return ADblock.systemNames.values()

    @staticmethod
    def getByName(name):
        if ADblock.userNames.has_key(name):
            return ADblock.userNames[name]
        return ADblock.systemNames.get(name, None)

    @staticmethod
    def getByLayoutBlock(layoutBlock):
        return ADblock.blocksList.get(layoutBlock, None)

    @staticmethod
    def setListeners():
        # Add a property change listener for each block
        # In order to avoid race conditions, listeners are added to
        # JMRI Block.java, not to sensors!
        for block in ADblock.systemNames.values():
            if(block.jmriBlock != None):
                # add the listener
                block.jmriBlock.addPropertyChangeListener(block)

    @staticmethod
    def removeListeners():
        # Remove the listener of each block
        for block in ADblock.systemNames.values():
            if(block.jmriBlock != None):
                # remove the listener
                block.jmriBlock.removePropertyChangeListener(block)

    def __init__(self, section, layoutBlock, userName):
        # Take note of section (each block must be contained in 
        # one section only!)
        self.section = section
        self.layoutBlock = layoutBlock
        self.jmriBlock = self.layoutBlock.getBlock()
        self.name = self.layoutBlock.getUserName()
        if self.name == None or self.name.strip() == "":
            self.name = self.layoutBlock.getSystemName()
        ADblock.userNames[userName] = self
        ADblock.systemNames[layoutBlock.getSystemName()] = self
        ADblock.blocksList[layoutBlock] = self
        # Get block length and take note of how many blocks have it
        # The later info will be used in the Preferences window
        self.length = float(self.jmriBlock.getLengthMm())
        if self.length <= 0.0:
            self.length = 0.0
            ADblock.blocksWithoutLength += 1
        else:
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
        if self.getOccupancySensor() == None:
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
        
    def setPaths(self):
        # Create our paths, based on JMRI paths
        # Our paths are stored in a Jython dictionary, for fast retrieval
        # (destination block is the key)
        self.paths = {}
        jmriPaths = self.getJmriBlock().getPaths()
        for i in range(jmriPaths.size()):
            jmriPath = jmriPaths.get(i)
            fromName = jmriPath.getBlock().getUserName()
            if fromName == None:
                fromName = jmriPath.getBlock().getSystemName()
            destinationBlock = ADblock.getByName(fromName)
            if destinationBlock != None:
                self.paths[destinationBlock] = jmriPath.getSettings()

    def addTrack(self, track):
        # Add a track to the block
        self.tracks.append(track)
        
    def getSection(self):
        # return the section containing this block
        return self.section
        
    def getName(self):
        return self.name
        
    def getLength(self):
        return self.length

    def getJmriBlock(self):
        return self.jmriBlock

    def setValue(self, value):
        # Set the value (e.g. train name) of the corresponding JMRI block
        self.jmriBlock.setValue(value)

    def getOccupancy(self):
        return self.jmriBlock.getState()
        
    def getOccupancySensor(self):
        return self.layoutBlock.getOccupancySensor()

    def getSpeed(self, direction):
        foundSpeed = self.speed[direction]
        if foundSpeed > len(ADsettings.speedsList):
            foundSpeed = len(ADsettings.speedsList)
        return foundSpeed
        
    def setTurnouts(self, connectingBlock, turnoutList):
        # Set turnouts between present block and connecting block
        # Return:
        #  True if any turnout is "Thrown"
        #  False if all turnouts are "Closed" (or no turnout was found)
        thrown = False
        # Check that connection exists
        if self.paths.has_key(connectingBlock):
            # Connection found
            beanSettings = self.paths[connectingBlock]
            # Set all turnouts contained in the path
            for i in range(beanSettings.size()):
                beanSetting = beanSettings.get(i)
                turnout = beanSetting.getBean()
                # Make sure turnout is not included twice in paths
                # Seems to happen starting with JMRI 2.11.4
                if not turnout in turnoutList:
                    turnoutList.append(turnout)
                    position = beanSetting.getSetting()
                    # Take note if turnout must be throw
                    if position == Turnout.THROWN:
                        thrown = True
                    # Operate turnout only if not yet set in the proper position
                    # or if user disabled "Trust turnouts KnownState" indicator
                    if (not ADsettings.trustTurnouts or 
                        turnout.getState() != position):
                        AutoDispatcher.turnoutCommands[turnout] = [
                            position, System.currentTimeMillis()]
                        turnout.setState(position)
                        # No need of repainting, since LayoutEditor will do it
                        AutoDispatcher.repaint = False
                        # Wait if user specified a delay between turnouts operation
                        if ADsettings.turnoutDelay > 0:
                            AutoDispatcher.instance.waitMsec(
                                                             ADsettings.turnoutDelay)
        return thrown
                    
    def setColor(self, color):
        # Set block color
        self.layoutBlock.setBlockTrackColor(color)
        self.layoutBlock.setBlockOccupiedColor(color)

    def adjustWidth (self):
        # Set the width of tracks, depending on block occupancy
        if not ADsettings.useCustomWidth:
            return
        occupied = self.layoutBlock.getOccupancy() == Block.OCCUPIED
        for track in self.tracks:
            track.setWidth(occupied)

    def restore(self):
        # Restore both block colors and tracks' width (invoked before exiting)
        # Restore original block colors
        self.layoutBlock.setBlockTrackColor(self.trackColor)
        self.layoutBlock.setBlockOccupiedColor(self.occupiedColor)
        # Restore original tracks' width
        for track in self.tracks:
            track.restoreWidth()

    def propertyChange(self, event):
        # Listener, invoked when a train enters/leaves the block
        if (event.getPropertyName() != "state" or 
            event.newValue == event.oldValue):
            return
        ind = 0
        if event.newValue == Block.OCCUPIED:
            # Train entering the block
            train = self.section.getAllocated()
            if train == None:
                # No train expected
                self.section.setOccupied()
                # Section is not allocated - Wrong route?
                if self.entryBlock and not self.section.isManual():
                    if ((not AutoDispatcher.stopped) and (not 
                        AutoDispatcher.paused) and 
                        ADsettings.wrongRouteDetection !=
                        ADsettings.DETECTION_DISABLED):
                        # Check if section has crossings 
                        # (this could be a false detection due to a short on 
                        # the crossing)
                        wrongRoute = True
                        for x in self.section.xings:
                            if x.getAllocated() != None:
                                wrongRoute = False
                                break
                        if wrongRoute:      
                            if (ADsettings.wrongRouteDetection ==
                                ADsettings.DETECTION_PAUSE):
                                AutoDispatcher.instance.stopAll()
                            AutoDispatcher.chimeLog(ADsettings.WRONG_ROUTE_SOUND,
                                                    "Train entering wrong route in section \""
                                                    + self.section.getName() + "\", block \""
                                                    + self.name + "\"")
            else:
                # Train expected
                train.changeBlock(self)
                if ADsettings.blockTracking:
                    self.setValue(train.getName())
        elif event.newValue == Block.UNOCCUPIED:
            # Train leaving the block
            # Check if the section is still occupied
            if ADpowerMonitor.powerOn:
                self.section.checkOccupancy()
        # Adjust track width in accordance to occupancy status
        self.adjustWidth()

# TRACK ==============

class ADtrack:
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

class ADlocation:
    # List of sections coresponding to each Operations' location
    
    # STATIC VARIABLES
    
#    locationManager = LocationManager.instance()
    locations = {}   # dictionary of location instances

    @staticmethod
    def getList():
        return ADlocation.locations.values()

    @staticmethod
    def getNames():
        return ADlocation.locations.keys()

    @staticmethod
    def getByName(name):
        return ADlocation.locations.get(name, None)

    def __init__(self, name):
        self.name = name
        ADlocation.locations[name] = self
        self.text = ""
        self.list = []
        self.opLocation = None
        
    def setSections(self, text):
        textSpace  = text.replace(",", " ")
        tokens = textSpace.split()
        list = []
        for t in tokens:
            s = ADsection.getByName(t)
            if s == None:
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                        "Location \"" + self.name +
                                        "\": wrong format or unknown section")
                return
            list.append(s)
        self.list = list
        self.text = text

    def getSections(self):
        return self.list

# DOUBLE CROSSOVER ==============

class ADxover:
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
        for i in range(4):
            # If block not specified, assume it to be block A
            if block[i] == None:
                block[i] = block[0]
            else:
                block[i] = ADblock.getByLayoutBlock(block[i])
            if block[i] == None:
                # If one of the blocks is not included in any section
                # the Xover can be treated as a turnout and 
                # this instance can be be purged
                return
            self.section.append(block[i].getSection())
            # If any connected section is null (i.e. section not controlled 
            # by AutoDispatcher), we can ignore the Xover (it will be treated 
            # as a simple turnout)
            if self.section[i] == None:
                # (this instance will be purged)
                return
        # Check if the crossover is the intersection of two sections
        if (self.section[0] == self.section[2] and 
            self.section[1] == self.section[3]):
            # Intersection - is the crossover fully contained 
            # in a single section?
            if self.section[0] == self.section[1]:
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
        if sectionFrom == sectionTo:
            return
        # Find entry point
        for direction in [False, True]:
            for entry in sectionTo.getEntries(direction):
                if (entry.getExternalSection() == sectionFrom and
                    entry.getExternalBlock() == blockFrom and
                    entry.getInternalBlock() == blockTo):
                    entry.addXover(self)
                    return
                
    def isAvailable(self, comingFrom):
        # Checks if a route through the Xover can be used
        # Check only allocation of crossing routes
        # (other cases will anyway be solved when checking sections allocation)
        busyRoute = -1
        for i in range(2):
            train = self.section[i].getAllocated()
            if train != None:
                if self.section[i + 2].getAllocated() == train:
                    busyRoute = i
                    break
        # If no route allocated, Xover is available
        if busyRoute < 0:
            return True
        # Allocated route found
        # Xover not available, unless route allocated to the same train
        return (self.section[busyRoute] == comingFrom or
                self.section[busyRoute + 2] == comingFrom)
        

# GRID-LOCK PROTECTION GROUP ==============

class ADgridGroup:
    # Each  ADgridGroup instance contains the list of sections converging
    # into a transit-only section.  These are point where gridlock may occur.
    
    # STATIC VARIABLES
        
    list = {}

    @staticmethod
    def create():
        # Create all gridLock groups
            ADgridGroup.list = {}
            # Scan all sections
            for section in ADsection.getList():
                # Create groups for transit-only sections that accept
                # trains in both directions
                # (one-way sections do not create gridlock situations)
                if ((section.transitOnly[0] or section.transitOnly[1])
                    and section.direction == 3):
                    ADgridGroup(section)

    @staticmethod
    def lockRisk(train, fromSection, toSection):
        # Find if occupying a section can result in
        # a gridLock situation
        # Is the section included in a gridLock group
        group = ADgridGroup.list.get(fromSection.getName() + "$" +
                                     toSection.getName(), None)
        if group == None:
            # No
            return False
        # Yes - invoke the recursive inernal method
        ADgridGroup.debug = fromSection.getName() == "Lv1"
        return group.__lockRisk__(train, toSection, [])
            
    def __init__(self, transit):
        # Keep note of the transit-only section, which is the
        # focus of the group
        self.transit = transit
        self.sections = []
        self.bumpers = []
        # Scan connected sections in both directions
        for direction in range(2):
            dirSections = []
            dirBumpers = []
            for entry in transit.getEntries(not direction):
                # Get the connected section
                section = entry.getExternalSection()
                dirSections.append(section)
                # Take note if the section is a siding
                dirBumpers.append(len(section.getEntries(direction)) == 0)
                # Create a dictionary entries for this section,
                # provided it's not one-way and gets accessed from a transit-only section
                # (focal section could not be transit-only in this direction)
                if self.transit.transitOnly[direction] and section.direction == 3:
                    # Get the list of sections from which this section can be entered
                    for e in section.getEntries(not direction):
                        fromSection = e.getExternalSection()
                        # Omit dictionary entries for one-way sections
                        if (fromSection.direction & (direction + 1)) != 0:
                            # Create the entry, as follows:
                            # startSection$endSection
                            ADgridGroup.list[fromSection.getName() + "$" +
                                section.getName()] = self
            self.sections.append(dirSections)
            self.bumpers.append(dirBumpers)

    def __lockRisk__(self, train, toSection, processed):
        # Internal recursive method
        # Make sure section was not processed yet during this call
        # to avoid an endless loop!
        if toSection in processed:
            return False
        processed.append(toSection)
        # Compute direction with respect to sections storing order
        direction = toSection in self.sections[1]
        if not direction and not toSection in self.sections[0]:
            # If section is not included in this group (shouldn't happen)
            # ignore call
            return False
        # Examine sections parallel to the requested section
        # If one of them is free or occupied by a train running in opposite
        # direction, there is no gridLock risk
        for section in self.sections[direction]:
            if (section != toSection and (section.getAllocated() == None or
                section.trainDirection != direction)):
                return False
        ind = 0
        # Now look at sections on the other side of the transit only track
        for section in self.sections[not direction]:
            otherTrain = section.getAllocated()
            # Skip trivial case (oval with only one station)
            if train == otherTrain:
                return False
            # If section is free, or occupied by a train running in the same direction
            # of our train (and has no end-bumper), we need to make sure that we are not
            # creating a gridLock situation ahead
            if (otherTrain == None or (section.trainDirection == direction and 
                not self.bumpers[not ind])):
                next = ADgridGroup.list.get(self.transit.getName() + "$" +
                                            section.getName(), None)
                if next == None or next == self: 
                    return False
                if not next.__lockRisk__(train, section, processed):
                    return False
            ind += 1
        return True

# TRAIN ==============

class ADlocomotive:
    # Our locomotives. Contain speed tables (i.e. correspondence between 
    # speed levels and throttle settings). A locomotive is created for each
    # entry in JMRI roster. Additional locomotives are created for demo
    # layouts
    
    # STATIC VARIABLES

    locoIndex = {}
    
    @staticmethod
    def getNames():
        # Return the list of locomotive names
        return ADlocomotive.locoIndex.keys()

    @staticmethod
    def getList():
        # Return the list of locomotives
        return ADlocomotive.locoIndex.values()

    @staticmethod
    def getByName(name):
        # Find a locomotive by name
        return ADlocomotive.locoIndex.get(name, None)

    def __init__(self, name, address, speed, inJmri):
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
        if inJmri:
            self.addressSwing = AutoDispatcher.centerLabel("")
        else:
            self.addressSwing = JTextField("", 4)
        self.setAddress(address)
        # Table of speeds corresponding to each speed level
        self.speedSwing = []
        for ind in range(len(ADsettings.speedsList)):
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

    def setSpeedTable(self, speed):
        # Change speeds table and display its contents
        levelsNumber = len(ADsettings.speedsList)
        if speed == None:
            # Speed table not defined
            self.speed = []
            # Assign even spaced speeds from 0.01 to 1.0
            if levelsNumber > 1:
                step = 0.99 / float(levelsNumber - 1)
            else:
                step = 0.
            level = 0.01
            for i in range(levelsNumber):
                self.speed.append(round(level, 2))
                level += step
        else:
            # Speed table defined
            # Use it
            self.speed = speed
            # Extend table, if needed
            while len(self.speed) < levelsNumber:
                self.speed.append(1.)
        # Update swing fields
        for i in range(levelsNumber):
            self.speedSwing[i].setText(str(self.speed[i]))
            
    def getCloserLevel(self, throttleValue):
        # Return level corresponding to a given throttle setting.
        # If no exact match is found, the higher closer level is returned.
        # Returned value is in range 1-levelsNumber.
        levelsNumber = len(ADsettings.speedsList)
        closerLevel = levelsNumber
        ind = 1
        for s in self.speed:
            if s >= throttleValue:
                closerLevel = ind
                break
            ind += 1
        if closerLevel > levelsNumber:
            return levelsNumber
        return closerLevel
        
    def setMomentum(self, acceleration, deceleration):
        if acceleration < 0:
            acceleration = 0
        if acceleration > 255:
            acceleration = 255
        if deceleration < 0:
            deceleration = 0
        if deceleration > 255:
            deceleration = 255
        self.acceleration = acceleration
        self.deceleration = deceleration
        # Compute acceleration/deceleration rate
        # Same as NMRA DCC CV3 and CV4
        if acceleration == 0:
            self.accStep = 0
        else:
            try:
                self.accStep = 1. / (float(acceleration) * 8.96)
            except:
                self.accStep = self.acceleration = 0
        if deceleration == 0:
            self.decStep = 0
        else:
            try:
                self.decStep = 1. / (float(deceleration) * 8.96)
            except:
                self.decStep = self.deceleration = 0
        self.accSwing.setText(str(self.acceleration))
        self.decSwing.setText(str(self.deceleration))

    def getAcceleration(self):
        return self.accStep * 10

    def getDeceleration(self):
        return self.decStep * 10

    def setAddress(self, address):
        # Change dcc address
        if self.throttle != None:
            self.throttle.release()
        if address < 1:
            address = 1
        self.address = address
        self.addressSwing.setText(str(address))
        self.throttleAssigned = False
        
    def assignThrottle(self):
        # Ignore call if throttle already assigned
        if self.throttleAssigned:
            return
        self.throttleAssigned = True
        # Assign the throttle
        AutoDispatcher.message("Acquiring throttle for locomotive " + self.name
                               + " (" + str(self.address) + ")")
        if (self.address > 100):
            long = True
        else:
            long = False
        # request address, timeout set to 5 seconds
        self.throttle = AutoDispatcher.instance.getThrottle(self.address, long, 5)
        if (self.throttle == None):
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Couldn't assign throttle " + str(self.address) + "!")
            self.throttleAssigned = False
        else:
            if self.leadLoco != 0:
                AutoDispatcher.message("Acquired throttle for consist "
                                       + self.name + " (" + str(self.address) + ")")
                self.leadThrottle = AutoDispatcher.instance.getThrottle(
                                                                        self.leadLoco, long, 5)
                if (self.leadThrottle != None):
                    AutoDispatcher.message("Acquired throttle for consist "
                                           + self.name + " (" + str(self.leadLoco) + ")")
                else:
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                            "Couldn't assign throttle" + 
                                            str(self.leadLoco) + " for consist " + self.name + "!")
                    self.throttleAssigned = False
            else:
                AutoDispatcher.message("Acquired throttle for locomotive "
                                       + self.name + " (" + str(self.address) + ")")
                self.leadThrottle = self.throttle
            speedStepMode = self.throttle.getSpeedStepMode()
            if speedStepMode == DccThrottle.SpeedStepMode128:
                self.stepsNumber = 126.
            elif speedStepMode == DccThrottle.SpeedStepMode28:
                self.stepsNumber = 28.
            elif speedStepMode == DccThrottle.SpeedStepMode27:
                self.stepsNumber = 27.
            else:
                self.stepsNumber = 14.
            self.currentSpeed = self.throttle.getSpeedSetting()

            
    def releaseThrottle(self):
        if self.throttle != None:
            if ADsettings.lightMode != 0:
                self.setFunction(0, False)
            self.throttle.release()
            self.throttle = None
            self.throttleAssigned = False
            if self.leadLoco != 0:
                self.leadThrottle.release()
                AutoDispatcher.message("Released throttles of consist "
                                       + self.name + " (" + str(self.address) + ", " + 
                                       str(self.leadLoco) + ")")
            else:
                AutoDispatcher.message("Released throttle of locomotive "
                                       + self.name + " (" + str(self.address) + ")")
            self.leadThrottle = None

    def outputMileage(self):
        # Output values of mileage and operation hours
        minutes = int(self.runningTime / 60000)
        hours = int(minutes / 60)
        minutes -= hours * 60
        if minutes < 10:
            minutes = "0" + str(minutes)
        else:
            minutes = str(minutes)
        self.hoursSwing.text = str(hours) + ":" + minutes
        if self.warnedTime:
            self.hoursSwing.setForeground(Color.red)
        else:
            self.hoursSwing.setForeground(Color.black)
        if ADsettings.units == 25.4:
            multiplier = ADsettings.scale / 1609344.
        else:
            multiplier = ADsettings.scale / 1000000.
        self.milesSwing.text = str(round(self.mileage * multiplier, 1))
        if self.warnedMiles:
            self.milesSwing.setForeground(Color.red)
        else:
            self.milesSwing.setForeground(Color.black)

    def setOrientation(self, forward):
        # Set locomotive direction
        if self.throttle != None:
            self.throttle.setIsForward(forward)
            if ADsettings.dccDelay > 0:
                sleep(float(ADsettings.dccDelay) / 1000.)

    def setFunction(self, functionNumber, on):
        if self.leadThrottle != None:
            if functionNumber < 0 or functionNumber > 28:
                return
            command = "self.leadThrottle.setF" + str(functionNumber)
            if on:
                command += "(True)"
            else:
                command += "(False)"
            exec(command)
            if ADsettings.dccDelay > 0:
                sleep(float(ADsettings.dccDelay) / 1000.)
            
    def changeSpeed(self, speedLevel):
        # Stop ?
        if speedLevel <= 0:
            self.changeThrottleSpeed(speedLevel)
            if (self.usedBy != None and self.usedBy.running
                and self.usedBy.engineerSetLocomotive != None
                and not AutoDispatcher.simulation):
                self.usedBy.stop()
            return
        # Speed increase/decrease - speedControl thread will take care of it
        if speedLevel > len(self.speed):
            speedLevel = len(self.speed)
        self.changeThrottleSpeed(self.speed[speedLevel-1])

    def changeThrottleSpeed(self, speed):
        if self.locoPaused:
            self.savedSpeed = speed
            return
        if self.targetSpeed == speed:
            return
        self.targetSpeed = speed
        if speed <= 0:
            # STOP
            if (ADsettings.stopMode != ADsettings.PROGRESSIVE_STOP
                or speed < 0):
                speed = -1
                if self.throttle != None:
                    self.throttle.setSpeedSetting(speed)
                    # self.targetSpeed = self.rampingSpeed = self.currentSpeed = 0
                    # self.targetSpeed = 0
                    # self.currentSpeedSwing.setText("0")
                    # self.updateMeter()
                elif self.runningStart == -1L:
                    self.runningStart = System.currentTimeMillis()
        if not AutoDispatcher.paused and not AutoDispatcher.stopped:
            AutoDispatcher.message("Locomotive " + self.name + " at speed "
                                   + str(self.targetSpeed))
    
    def updateMeter(self):
        # Updates locomotive's operation time
        # Called when the locomotive is stopped
        if self.runningStart != -1L:
            self.runningTime += System.currentTimeMillis() - self.runningStart
            self.runningStart = -1L

    def getName(self):
        return self.name

    def getThrottleSpeed(self):
        return self.rampingSpeed

    def pause(self):
        self.savedSpeed = self.targetSpeed
        if ADsettings.pauseMode == ADsettings.STOP_TRAINS:
            self.changeThrottleSpeed(0)
        else:
            self.changeThrottleSpeed(-1)
        self.locoPaused = True
        # Clear learning data, since they are now meaningless
        self.brakeAdjusting = False

    def resume(self):
        self.locoPaused = False
        self.changeThrottleSpeed(self.savedSpeed)

# Self-learning methods of the locomotive ==============

    def learningClear(self):
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

    def learningBrake(self):
        # Train entering a braking block
        self.length = 0
        if (not ADsettings.selfLearning or self.decStep == 0
            or self.usedBy == None or self.throttle == None or
            self.brakeAdjusting or AutoDispatcher.simulation):
            # Self-learning not supported or already active
            return False
        # Make sure we have all information needed
        block = self.usedBy.block
        previousBlock = self.usedBy.previousBlock
        train = self.usedBy
        if block == None or previousBlock == None or train == None:
            return False
        self.learningClear()
        # Take note of time and speed
        self.initialTime = System.currentTimeMillis()
        self.initialSpeed = self.rampingSpeed
        # Since we must brake, make sure that speed is not being increased
        if self.targetSpeed > self.rampingSpeed:
            self.targetSpeed = self.rampingSpeed
        # Build identification key
        self.brakeKey = (self.name + "$" + block.getName() + "$"
                         + previousBlock.getName())
        # Did we already stop in this block?
        if train.brakingHistory.has_key(self.brakeKey):
            # Yes retrieve previous data
            data = train.brakingHistory[self.brakeKey]
            self.recordedValues = data[0]
            self.squareSum = data[1]
        else:
            # First time we are braking in this block
            # Initialize data
            self.recordedValues = 0
            self.squareSum = 0
        # Compute braking data 
        # (if we have needed info and we are not running yet at minimimum speed)
        if self.recordedValues > 0 and self.targetSpeed > self.speed[0]:
            self.length = sqrt(self.squareSum / float(self.recordedValues))
            # Reduce length, in order to reach minimum speed 2 seconds before
            # reaching the stop block
            length = self.length - self.speed[0] * 2.
            if length > 0.:
                # How much time is required to reach minimum speed?
                brakeTime = ((self.initialSpeed - self.speed[0]) / 
                             self.decStep * 100.)
                # How much space is required to reach minimum speed?
                brakeLength = ((self.initialSpeed + self.speed[0])
                               * brakeTime / 2000.)
                # Is block length sufficient?
                delaySpace = length - brakeLength
                if delaySpace < 0:
                    # No, we must reduce momentum
                    # Compute time required to break
                    # try
                    brakeTime = (length * 2000. / (self.initialSpeed +
                                 self.speed[0]))
                    # Compute acceleration
                    self.decAdjustment = ((self.initialSpeed - self.speed[0]) *
                                          100. / brakeTime)  - self.decStep
                    self.brakeAdjusting = True
                else:
                    # Enough space
                    # How much time is required to reach the target speed?
                    targetTime = ((self.initialSpeed - self.targetSpeed) / 
                                  self.decStep * 100.)
                    # Let's compute braking delay
                    self.delay = int(delaySpace * 1000. / self.targetSpeed + 
                                     targetTime)
                    if self.delay > 0:
                        start_new_thread(self.learningDelayedBrake,
                                         (self.brakeKey, ))
                        self.brakeAdjusting = True
                        return True
        else:
            self.brakeAdjusting = True
        # We don't have previous data or something went wrong
        # Let's locomotive start braking immediately
        self.brakingStartTime = self.initialTime
        self.brakingSpeed = self.initialSpeed
        return False


    def learningDelayedBrake(self, key):
        # Wait before braking
        sleep(float(self.delay) / 1000.)
        # Make sure that braking is still needed
        if (self.delay > 0 and self.stoppingTime == -1L and
            self.brakingStartTime == -1L and key == self.brakeKey):
            self.brakingStartTime = System.currentTimeMillis()
            self.brakingSpeed = self.rampingSpeed
            if self.usedBy == None:
                self.changeSpeed(1)
            else:
                self.usedBy.changeSpeed(1)

    def learningEnd(self):
        # Train completed braking
        if self.brakeAdjusting and self.brakingEndTime == -1L:
            self.brakingEndTime = System.currentTimeMillis()

    def learningStop(self):
        # Train entering a stop block
        # Were we controlling braking times?
        if not self.brakeAdjusting:
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
        if self.brakingStartTime == -1L:
            # No - braking delay was excessive!
            # Set length to half the length of block computed
            # in previous iteration (at least for the time being. A more
            # accurate computation can be implemented later)
            if AutoDispatcher.debug:
                print (self.brakeKey + " Delay " + str(self.delay)
                       + " Adjustment " + str(self.decAdjustment)
                       + " length " + str(self.length) + " braking not started")
            length = self.length * 0.5
        else:
            # Yes, train started braking
            # Compute distance at full speed
            length  = (float(self.brakingStartTime - self.initialTime) *
                       (self.brakingSpeed + self.initialSpeed) / 2000.)
            # Did train attain minimum speed?
            if self.brakingEndTime == -1L:
                # No, set end of braking time to stopping time
                if AutoDispatcher.debug:
                    print (self.brakeKey + " Delay " + str(self.delay)
                           + " Adjustment " + str(self.decAdjustment)
                           + " length " + str(length) + " minimum not reached")
                self.brakingEndTime = self.stoppingTime
                if length > self.length:
                    length = self.length
                length = length * 0.5
            else:
                # Yes, minimum speed attained
                # Compute distance run at minimum speed
                creepingTime = self.stoppingTime - self.brakingEndTime
                length += (self.rampingSpeed * float(creepingTime) / 1000.)
                if AutoDispatcher.debug:
                    print (self.brakeKey + " Delay " + str(self.delay)
                           + " Adjustment " + str(self.decAdjustment)
                           + " length " + str(length) + " creepingTime " + str(creepingTime))
            # Now add the length of the braking ramp (braking distance)
            brakingTime = self.brakingEndTime - self.brakingStartTime
            length += ((self.brakingSpeed + self.rampingSpeed) * 
                       float(brakingTime) / 2000.)
        # Update braking data
        if brakingTime >= 0 and creepingTime >= 0 and length > 0:
            self.recordedValues += 1
            self.squareSum += length * length
            train = self.usedBy
            if train != None:
                train.brakingHistory[self.brakeKey] = [self.recordedValues,
                    self.squareSum]
        # Clear data
        self.brakeKey = ""
        self.brakeAdjusting = False
        
# ENGINEER ==============

class ADengineer:
    # Our engineer is rather simple. It only lets know AutoDispatcher that
    # the ADlocomotive class should be used. ADlocomotive takes care of all
    # the rest.
    def setLocomotive(self, locomotive):
        return
        
# ROUTE ==============

class ADautoRoute:
    # Defines a route, i.e. the list of entries connecting two sections
    def __init__(self, startSection, endSection, direction, switching):
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
        if self.found:
            self.error = None

    def __fromTo__(self, startSection, endSection, direction):
        # Internal method. Recursively explores all possibe routes.
        # If more than one route is found the shorter one (i.e. that with
        # less steps) is returned.
        self.found = False
        # Exit if this section was already "visited" (to avoid an endless loop!)
        if startSection in self.searchedSections:
            return []
        # Check if direction is acceptable for this section or if train is
        # in "switching mode" (switching trains can enter any track)
        if (((direction + 1) & startSection.direction) == 0 and
            not self.switching):
            return []
        # Did we reach destination?
        if startSection == endSection:
            # Check if destination is transit-only
            if endSection.transitOnly[direction] and not self.switching:
                # Transit only destination
                self.error = endSection
            else:
                self.found = True
            return []
        # Mark this section as "visited" (to avoid an endless loop!)
        self.searchedSections.append(startSection)
        route = []
        routeLen = 0
        ## Get next sections in the desired direction
        for entry in startSection.getEntries(direction):
            nextSection = entry.getExternalSection()
            # Compute direction along next section
            if entry.getDirectionChange():
                newDirection = not direction
            else:
                newDirection = direction
            # Go ahead exploring
            newRoute = self.__fromTo__(nextSection, endSection, newDirection)
            if self.found:
                # A possible route found
                # Is it the first one found, or is it shorter than
                # previous routes?
                newLen = len(newRoute) + 1
                if routeLen == 0 or newLen < routeLen:
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
        for entry in self.step:
            # Check if possible Xovers are available
            if not entry.areXoversAvailable():
                # An Xover is allocated to another train. Terminate the route
                break
            # Get the corresponding section
            section = entry.getExternalSection()
            # Adjust train direction, if needed
            if entry.getDirectionChange():
                direction = not direction
            # Is use of this section allowed ?
            if (section.isManual() or (not section.transitOnly[direction] and
                ADgridGroup.lockRisk(train,
                entry.getInternalSection(), section))):
                # No
                break
            # get the train (if any) to which the section is allocated
            otherTrain = section.getAllocated()
            # Check if the section can be used by present train
            if (not section.isAvailable() and (otherTrain != train
                or section.isOccupied())):
                # The section is occupied or allocated to another train
                # Is this the first section along the route or is
                # "burst" mode not allowed?
                if (firstTime or (not section.burst and not previousBurst)):
                    # Present train cannot use it
                    break
                # Is a train using this section?
                if otherTrain == None:
                    # No train, section contains only rolling stock.
                    # Present train cannot use it
                    break
                # We have another train: is it running in the same direction?
                if otherTrain.getDirection() != direction:
                # Other train in opposite direction, present train cannot start
                    break
                # If this is not a Transit-Only section, train can run up to
                # previous  empty section
                if not section.transitOnly[direction]:
                    keep = lastFreeSection
                    break
            else:
                # Take note that section is free (or allocated to this train
                # but not occupied yet)
                lastFreeSection = ind
                # Count free sections encountered and equipped with exit signal
                if (not section.transitOnly[direction] or
                    section.getSignal(direction).hasHead()):
                    nSections += 1
                # Could the reduced route end in this section?
                if not section.transitOnly[direction] or self.switching:
                    # The section is not transit-only (or train
                    # in switching mode).
                    # Terminate the route here if the requested number
                    # of sections ahead was found
                    keep = lastFreeSection
                    if nSections >= allocationAhead:
                        break
            # Take note that we already encountered a section
            firstTime = False
            # PreviousBurst temporarily suppressed (could be implemented
            # as an option)
            # previousBurst = section.burst
            # Make sure signal is not held
            if section.getSignal(direction).isHeld():
                break
            ind += 1
        # Job almost done.  "keep" contains the number of entries to be kept
        # Remove possible trailing entries from the route
        while len(self.step) > keep:
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
        for entry in self.step:
            # Adjust direction, if needed
            if entry.getDirectionChange():
                direction = not direction
            # Retrieve relevant section
            section = entry.getExternalSection()
            # Make sure section is not occupied or allocated
            if section.isAvailable():
                # Allocate sections in front of other trains (if any)
                # only if they are not transit-only
                if freeRoute or not section.transitOnly[direction]:
                    section.allocate(train, direction)
            elif section.getAllocated() != train:
                # Section occupied or allocated to another train
                freeRoute = False
            if section.getAllocated() == train:
                train.lastRouteSection = section
            # Take note of last not occupied section
            if freeRoute:
                keep = ind
                if (not section.transitOnly[direction] or
                    section.getSignal(direction).hasHead()):
                    nSections += 1
                destination = section
                if not section in train.sectionsAhead:
                    train.sectionsAhead.append(section)
            # Make sure signal is not held
            if section.getSignal(direction).isHeld():
                break
            ind += 1
        # Remove trailing elements from the route (if needed)
        while len(self.step) > keep:
            self.step.pop()
        if destination != None:
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
        if len(self.step) == 0:
            return
        direction = self.direction
        # Start setting turnouts from stop block of starting section
        startBlock = self.step[0].getInternalSection().stopBlock[direction]
        for entry in self.step:
            endBlock = entry.getInternalBlock()
            # Set turnouts up to exit block included
            thrown = self.__setTurnouts__(startBlock, endBlock, direction)
            # Set turnouts from present section to next section
            startBlock = entry.getExternalBlock()
            if startBlock.setTurnouts(endBlock, self.turnoutList):
                thrown = True
            # and vice-versa
            if endBlock.setTurnouts(startBlock, self.turnoutList):
                thrown = True
            # Keep note if some turnouts were thrown
            # (info will be used when clearing signals)
            entry.getInternalSection().turnoutsThrown = thrown
            # Adjust train direction, if needed
            if entry.getDirectionChange():
                direction = not direction
        # Set turnouts from entry of last section to its stop block
        endBlock = startBlock.getSection().stopBlock[direction]
        if self.__setTurnouts__(startBlock, endBlock, direction):
            # Keep note that some turnouts were thrown
            startBlock.getSection().turnoutsThrown = True
        
    def __setTurnouts__(self, startBlock, endBlock, direction):
        # Internal method
        # Sets turnouts between two blocks of the same section
        # Returns True if any turnout was thrown.
        # Ignore call, if start and end block are the same one
        if startBlock == endBlock:
            self.blocksList.append(startBlock)
            return False
        thrown = False
        previousBlock = None
        for block in startBlock.getSection().getBlocks(direction):
            # Find starting block
            if previousBlock == None:
                if block == startBlock:
                    previousBlock = block
                    self.blocksList.append(block)
            else:
                # Starting block found, set turnouts
                # Note that we need to set turnouts included in both paths:
                # From previousBlock to block; and 
                # from block to previousBlock.
                if block.setTurnouts(previousBlock, self.turnoutList):
                    thrown = True
                if previousBlock.setTurnouts(block, self.turnoutList):
                    thrown = True
                self.blocksList.append(block)
                # Look for ending block
                if block == endBlock:
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
        for section in sections:
            if section in processedSections:
                continue
            processedSections.append(section)
            signal = section.getSignal(section.trainDirection)
            if setRed:
                signal.setIndication(0)
                continue
            if section.turnoutsThrown:
                anyThrown = True
            indication = ADindication.getIndication(oldIndication, anyThrown)
            signal.setIndication(indication)
            # No need of repainting panel, Layout Editor will do it anyway
            AutoDispatcher.repaint = False
            if signal.hasHead():
                anyThrown = False
                oldIndication = indication
            # Make sure train did not advance in the meantime
            if section == train.section:
                setRed = True

# SIGNAL HEAD ==============

class ADsignalHead:
    # Our signal head class 
    # Created also if there is no signal head in JMRI
    def __init__(self, signalName):
        self.name = signalName
        self.signalHead = None
        self.iconOnLayout = False
        if signalName.strip() != "":
            self.signalHead = InstanceManager.signalHeadManagerInstance(
                                                                        ).getSignalHead(signalName)
            if self.signalHead != None:
                self.setHeld(False)
                self.iconOnLayout = signalName in AutoDispatcher.signalIcons

    def setAppearance(self, appearance):
        # Record new signal appearance and modify also that of the signal head
        # (if available)
        self.appearance = appearance
        if self.signalHead != None:
            if (not ADsettings.trustSignals or
                self.signalHead.getAppearance() != self.appearance):
                AutoDispatcher.signalCommands[self.signalHead] = [
                    self.appearance, System.currentTimeMillis()]
                self.signalHead.setAppearance(self.appearance)
                # Wait if user specified a delay between signal operation
                if ADsettings.signalDelay > 0:
                    AutoDispatcher.instance.waitMsec(
                                                     ADsettings.signalDelay)

    def getAppearance(self):
        return self.appearance

    def isHeld(self):
        # Checks if the SignalHead (if any) is "HELD"
        if self.signalHead == None:
            return False
        else:
            return self.signalHead.getHeld()

    def setHeld(self, newHeld):
        # Set the SignalHead (if any) to "HELD"
        if self.signalHead != None:
            self.signalHead.setHeld(newHeld)
            
    def hasHead(self):
        return self.signalHead != None
            
    def hasIcon(self):
        return self.iconOnLayout

# SIGNAL MAST ==============

class ADsignalMast:
    # Our multi-head signal class 
    # Created also if there is no signal head in JMRI
    
    # STATIC VARIABLES
    
    signalsList = {}

    @staticmethod
    def getByName(name):
        return ADsignalMast.signalsList.get(name, None)

    @staticmethod
    def provideSignal(name):
        signal = ADsignalMast.getByName(name)
        if signal == None:
            signal = ADsignalMast(name, ADsettings.signalTypes[0], [name])
        return signal

    @staticmethod
    def getNames():
        return ADsignalMast.signalsList.keys()

    @staticmethod
    def getList():
        return ADsignalMast.signalsList.values()

    @staticmethod
    def getTable():
        outBuffer = []
        for signal in ADsignalMast.signalsList.values():
            outLine = [signal.name]
            outLine.append(signal.signalType.name)
            outHeads = []
            for h in signal.signalHeads:
                if h == None:
                    outHeads.append("")
                else:
                    outHeads.append(h.name)
            outLine.append(outHeads)
            outBuffer.append(outLine)
        return outBuffer

    @staticmethod
    def putTable(inBuffer):
        for inLine in inBuffer:
            type = ADsettings.signalTypes[0]
            for newType in ADsettings.signalTypes:
                if newType.name == inLine[1]:
                    type = newType
                    break
            ADsignalMast(inLine[0], type, inLine[2])            

    def __init__(self, signalName, signalType, signalHeads):
        self.name = signalName
        self.signalType = signalType
        self.signalType.changeUse(1)
        self.inUse = 0
        self.headsNumber = self.signalType.headsNumber
        self.signalHeads = [None] * self.headsNumber
        ind = 0
        for headName in signalHeads:
            if ind >= self.headsNumber:
                break
            self.signalHeads[ind] = ADsignalHead(headName)
            ind += 1
        self.indication = -1
        self.setIndication(0)
        ADsignalMast.signalsList[self.name] = self

    def setIndication(self, indication):
        if self.indication == indication:
            return
        self.indication = indication
        if self.indication >= len(self.signalType.aspects):
            return
        aspects = self.signalType.aspects[indication]
        for ind in range(self.headsNumber):
            if self.signalHeads[ind] != None:
                self.signalHeads[ind].setAppearance(aspects[ind])

    def changeUse(self, increment):
        self.inUse += increment
        if self.inUse < 0:
            self.inUse = 0
            self.signalType.changeUse(-1)
            newDic = {}
            for s in ADsignalMast.getList():
                if s != self:
                    newDic[s.name] = s
            ADsignalMast.signalsList = newDic           

    def getName(self):
        return self.name

    def getIndication(self):
        return self.indication

    def getSpeed(self):
        if self.indication >= len(self.signalType.speeds):
            return 0
        speed = self.signalType.speeds[self.indication]
        if speed < 0:
            if self.indication >= len(ADsettings.indicationsList):
                return 0
            return ADsettings.indicationsList[self.indication].speed
        return speed + 1

    def setHeld(self, newHeld):
        if self.signalHeads > 0:
            self.signalHeads[0].setHeld(newHeld)
    
    def isHeld(self):
        # Checks if any SignalHead is "HELD"
        for head in self.signalHeads:
            if head != None:
                if head.isHeld():
                    return True
        return False

    def hasIcon(self):
        # Checks if any SignalHead has an icon
        for head in self.signalHeads:
            if head != None:
                if head.hasIcon():
                    return True
        return False
            
    def hasHead(self):
        if self.headsNumber < 1 or self.signalHeads[0] == None:
            return False
        return self.signalHeads[0].hasHead()

# SIGNAL INDICATIONS ==============

class ADindication:
           
    @staticmethod
    def getIndication(nextIndication, nextTurnout):
        next = nextIndication
        for i in range(3):
            for j in range(2, len(ADsettings.indicationsList)):
                a = ADsettings.indicationsList[j]
                if ((a.nextIndication == next
                    or (a.nextIndication >= 0 and next >= 0 and 
                    ADsettings.indicationsList[a.nextIndication].name ==
                    ADsettings.indicationsList[next].name))
                    and a.nextTurnout == nextTurnout):
                    return j
            if (i & 1) == 0:
                next = -1
            else:
                next = nextIndication
            if i == 1:
                nextTurnout = -1
        return 1

    def __init__(self, name, nextIndication, nextTurnout, speed):
        self.name = name
        self.nextIndication = nextIndication
        self.nextTurnout = nextTurnout
        if self.nextTurnout < -1:
            self.nextTurnout = -1
        elif self.nextTurnout > 1:
            self.nextTurnout = 1
        self.speed = speed
        self.nameSwing = JTextField(self.name, 20)
        self.nextIndicationSwing = JComboBox()
        self.nextTurnoutSwing = JComboBox(["-", "Closed", "Thrown"])
        self.nextTurnoutSwing.setSelectedIndex(self.nextTurnout + 1)
        self.speedSwing = JComboBox()

# SIGNAL TYPE ==============

class ADsignalType:

    @staticmethod
    def adjust():
        for s in ADsettings.signalTypes:
            s.adjustIndications()

    def __init__(self, name, aspects, speeds):
        # aspects = [[aspect0, aspect1...],...]
        self.name = name
        self.inUse = 0
        # Compute number of signal heads
        self.headsNumber = 1
        for a in aspects:
            if len(a) > self.headsNumber:
                self.headsNumber = len(a)
        # Compute number of aspects
        if ADsettings == None:
            self.aspectsNumber = 2
        else:
            self.aspectsNumber = len(ADsettings.indicationsList)
        if len(aspects) > self.aspectsNumber:
            self.aspectsNumber = len(aspects)
        # Initialize aspects for each head
        # Default setting for stop = all heads RED
        self.aspects = [[SignalHead.RED] * self.headsNumber]
        # Default setting for other indications = all heads GREEN
        for i in range(self.aspectsNumber-1):
            self.aspects.append([SignalHead.GREEN] * self.headsNumber)
        # Set actual aspects
        i = 0
        for a in aspects:
            j = 0
            for aa in a:
                self.aspects[i][j] = aa
                j += 1
            i += 1
        self.speeds = [-1] * self.aspectsNumber
        i = 0
        for s in speeds:
            if i >= self.aspectsNumber:
                break
            self.speeds[i] = s
            i += 1
        self.nameSwing = JTextField(self.name, 20)

    def adjustIndications(self):
        diff = len(ADsettings.indicationsList) - self.aspectsNumber
        if diff > 0:
            for i in range(diff):
                self.aspects.append([SignalHead.GREEN] * self.headsNumber)
                self.speeds.append(-1)
        self.aspectsNumber = len(ADsettings.indicationsList)

    def changeUse(self, increment):
        self.inUse += increment
        if self.inUse < 0:
            self.inUse = 0

# SETTINGS ==============

class ADsettings:
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
    directionNames = ("CCW", "CW")
    ccwStart = ""
    ccwEnd = ""
    ccw = 0
    # Measurement units 1.0=mm. 10.0=cm. 25.4=inches
    if Locale.getDefault().getCountry() == "US":
        units = 25.4
    else:
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
    if units == 25.4:
        lostCarsTollerance = 2032.0
    else:
        lostCarsTollerance = 2000.0
    lostCarsSections = 3
    sectionTracking = False
    soundList = []
    defaultSounds = [1] * len(soundLabel)
    try:
        soundRoot = jmri.util.FileUtil.getUserFilesPath()
    except:
        try:
            soundRoot = XmlFile.userFileLocationDefault()
        except:
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

    @staticmethod
    def getSpeedName(speedLevel):
        if speedLevel == 0:
            return "Stop"
        if speedLevel > len(ADsettings.speedsList):
            return "Unknown: " + str(speedLevel)
        return ADsettings.speedsList[speedLevel-1] 

    @staticmethod
    def getScale():
        return ADsettings.scale 

    @staticmethod
    def getUnits():
        return ADsettings.units 

    @staticmethod
    def stringToColor(c):
        # Convert a string into a color
        if c.startswith("R:"):
            # Custom RGB color
            r = int(c[2:5])
            g = int(c[7:10])
            b = int(c[12:])
            return Color(r, g, b) 
        # Standard Java color
        return ADsettings.colors[c]

    @staticmethod
    def rgbToString(rgb):
        # Build a color string "R:rrrG:gggB:bbb"
        lab = ["R:", "G:", "B:"]
        out = ""
        for j in range(3):
            out += lab[j]
            c = rgb[j]
            if c < 100:
                out += "0"
                if c < 10:
                    out += "0"
            out += str(c)
        return out
 
    @staticmethod
    def initColors():
        # Convert colors from strings to JAVA constants
        ADsettings.sectionColor = []
        for c in ADsettings.colorTable:
            ADsettings.sectionColor.append(ADsettings.stringToColor(c))

    @staticmethod
    def save(file, sections, blocks):
        outIndications = []
        for indication in ADsettings.indicationsList:
            outIndications.append([indication.name, indication.nextIndication,
                                  indication.nextTurnout, indication.speed])
        outSignalTypes = []
        for signal in ADsettings.signalTypes:
            signalLine = [signal.name]
            indicationLines = []
            for i in range(len(ADsettings.indicationsList)):
                indicationLine = []
                for aa in signal.aspects[i]:
                    indicationLine.append(AutoDispatcher.inverseAspects[aa])
                indicationLines.append(indicationLine)
            signalLine.append(indicationLines)
            signalLine.append(signal.speeds)
            outSignalTypes.append(signalLine)
        sounds = []
        for s in ADsettings.soundList:
            sounds.append([s.name, s.path])
        locations = []
        for l in ADlocation.getList():
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

    @staticmethod
    def load(inData):
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
        if ADblock.blocksWithLength > 0:
            ADsettings.useLength = inData[22]
        ADsettings.blockTracking = inData[23]
        ADsettings.speedsList = inData[24]
        ADsettings.resistiveDefault = inData[25]
        ADsettings.dccDelay = inData[26]
        ADsettings.startDelayMin = inData[27]
        ADsettings.speedRamp = inData[28]
        ADsettings.lightMode = inData[29]
        ADsettings.indicationsList = []
        for a in inData[30]:
            ADsettings.indicationsList.append(ADindication(a[0], a[1], a[2], a[3]))
        ADsignalType.adjust()
        ADsettings.ringBell = inData[31]
        ADsettings.signalTypes = []
        for s in inData[32]:
            indicationLines = []
            for a in s[1]:
                indicationLine = []
                for aa in a:
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
        if ADsettings.stopMode > 1:
            ADsettings.stopMode = 1
        ADsettings.lostCarsDetection = inData[41]
        ADsettings.lostCarsTollerance = inData[42]
        ADsettings.lostCarsSections = inData[43]
        ADsettings.sectionTracking = inData[44]
        ADsettings.soundList = []
        for i in inData[45]:
            s = ADsound(i[0])
            s.setPath(i[1])
            ADsettings.soundList.append(s)
        ADsettings.newSoundDic()
        ADsettings.defaultSounds = inData[46]
        while len(ADsettings.defaultSounds) < len(ADsettings.soundLabel):
            ADsettings.defaultSounds.append(1)
        if inData[47] != "":
            ADsettings.soundRoot = inData[47]
        ADsettings.maintenanceTime = inData[48]
        ADsettings.maintenanceMiles = inData[49]
        ADsettings.scale = inData[50]
        for l in inData[51]:
            location = ADlocation(l[0])
            location.setSections(l[1])
        ADsettings.flashingCycle = inData[52]
        if len(inData) > 53:
            ADsettings.defaultStartAction = inData[53]
            if len(inData) > 54:
                ADsettings.autoRestart = inData[54]
        ADsettings.initColors()
      
    @staticmethod
    def newSoundDic():
        newDic = {}
        for s in ADsettings.soundList:
            newDic[s.name] = s
        ADsettings.soundDic = newDic

    def __init__(self):
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

class ADsound:
    # Encapsulates info relevant to a JMRI Sound
    def __init__(self, name):
        self.name = name
        self.path = ""
        self.sound = None
        
    def setPath(self, path):
        self.path = path
        if self.path.strip() != "":
            try:
                self.sound = Sound(path)
            except:
                self.sound = None
        
    def play(self):
        if self.sound != None:
            self.sound.play()
    

class ADpowerMonitor (PropertyChangeListener):
    # Monitors power on layout
    # Or simulates it, if the simulator used (e.g. XpressNet) is not 
    # supporting powerManager
	
    # STATIC VARIABLES

    powerOn = True
    savePause = False
    powerOffTime = -1L
	
    def __init__(self):
        self.powerManager = InstanceManager.powerManagerInstance()
        if self.powerManager != None:
            ADpowerMonitor.powerOn = (self.powerManager.getPower()
                                      == PowerManager.ON)
            self.powerManager.addPropertyChangeListener(self)
	
    def propertyChange(self, ev):
        value = self.powerManager.getPower()
        newStatus = ADpowerMonitor.powerOn
        if value == PowerManager.ON:
            newStatus = True
        elif value == PowerManager.OFF:
            newStatus = False
        if newStatus == ADpowerMonitor.powerOn:
            return
        ADpowerMonitor.powerOn = newStatus
        if ADpowerMonitor.powerOn:
            self.resetAccessories()
            if ADpowerMonitor.savePause and ADmainMenu.resumeButton.enabled:
                AutoDispatcher.instance.resume()
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Power On")
        else:
            ADpowerMonitor.powerOffTime = System.currentTimeMillis()
            ADpowerMonitor.savePause = ADmainMenu.pauseButton.enabled
            if ADpowerMonitor.savePause:
                AutoDispatcher.instance.stopAll()
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Power Off")

    def __del__(self):
        self.dispose()

    def dispose(self):
        if self.powerManager != None and not AutoDispatcher.lenzSimulation:
            self.powerManager.removePropertyChangeListener(self)
			
    def setPower(self, power):
        ADpowerMonitor.powerOn = power == PowerManager.ON
        if self.powerManager != None:
            self.powerManager.setPower(power)
        if not ADsettings.separateTurnouts or not ADsettings.separateSignals:
            sleep(1.0)
            self.resetAccessories()

    def resetAccessories(self):
        if ADpowerMonitor.powerOffTime == -1L:
            return
        if not ADsettings.separateTurnouts:
            checkTime = ADsettings.turnoutDelay * 2
            if checkTime < 3000:
                checkTime = 3000
            checkTime = ADpowerMonitor.powerOffTime - checkTime
            for turnout in AutoDispatcher.turnoutCommands.keys():
                packet = AutoDispatcher.turnoutCommands[turnout]
                if packet[1] > checkTime:
                    AutoDispatcher.turnoutCommands[turnout] = [packet[0],
                        System.currentTimeMillis()]
                    turnout.setState(packet[0])
                    # Wait if user specified a delay between turnout operation
                    if ADsettings.turnoutDelay > 0:
                        sleep(float(ADsettings.turnoutDelay) / 1000.)
        if not ADsettings.separateSignals:
            checkTime = ADsettings.signalDelay * 2
            if checkTime < 3000:
                checkTime = 3000
            checkTime = ADpowerMonitor.powerOffTime - checkTime
            for signal in AutoDispatcher.signalCommands.keys():
                packet = AutoDispatcher.signalCommands[signal]
                if packet[1] > checkTime:
                    AutoDispatcher.signalCommands[signal] = [packet[0],
                        System.currentTimeMillis()]
                    signal.setAppearance(packet[0])
                    # Wait if user specified a delay between signal operation
                    if ADsettings.signalDelay > 0:
                        sleep(float(ADsettings.signalDelay) / 1000.)
