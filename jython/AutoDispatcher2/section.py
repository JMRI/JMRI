from java.beans import PropertyChangeListener
from javax.swing import ButtonGroup
from javax.swing import JCheckBox
from javax.swing import JComboBox
from javax.swing import JLabel
from javax.swing import JRadioButton
from javax.swing import JTextField
from jmri import Block
from jmri import InstanceManager
from jmri import Section
from jmri import Sensor
from thread import start_new_thread
from time import sleep

class ADsection (PropertyChangeListener):
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
    userNames = {}       # dictionary of sections by userName
    systemNames = {}     # dictionary of sections by systemName
    sensorNames = None  # list of sensors that may be used for manual control
        # (i.e. those not used for block occupancy)

    @staticmethod
    def getList():
        return ADsection.systemNames.values()

    @staticmethod
    def getNames():
        return ADsection.userNames.keys()

    @staticmethod
    def getByName(name):
        if ADsection.userNames.has_key(name):
            return ADsection.userNames[name]
        return ADsection.systemNames.get(name, None)

    @staticmethod
    def getSectionsTable():
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
        for s in ADsection.systemNames.values():
            if s.direction == 3:
                direction = ""
            elif s.direction == ADsettings.ccw + 1:
                direction = ADsettings.directionNames[0]
            else:
                direction = ADsettings.directionNames[1]
            if s.burst:
                burst = "+"
            else:
                burst = ""
            if s.transitOnly[ADsettings.ccw]:
                if s.transitOnly[1-ADsettings.ccw]:
                    transitOnly = (ADsettings.directionNames[0] + "-" 
                                   + ADsettings.directionNames[1] + burst)
                else:
                    transitOnly = ADsettings.directionNames[0] + burst
            elif s.transitOnly[1-ADsettings.ccw]:
                transitOnly = ADsettings.directionNames[1] + burst
            else:
                transitOnly = ""
            signals = ["", ""]
            heldIndicators = ["", ""]
            for i in range(2):
                if s.signal[i] != None:
                    signals[i] = s.signal[i].name
                    if s.signal[i].isHeld():
                        heldIndicators[i] = "Held"
            if s.manuallyFlipped:
                inverted = "INVERTED"
            else:
                inverted = ""
            if s.isManual():
                manualSection = "Manual"
            else:
                manualSection = ""
            if s.manualSensor == None:
                manualSensor = ""
            else:
                manualSensor = s.manualSensor.getUserName()
                if manualSensor == None or manualSensor == "":
                    manualSensor = s.manualSensor.getSystemName()
            out.append([s.name, direction, transitOnly, signals[0],
                       signals[1], inverted, manualSensor, s.stopAtBeginning,
                       heldIndicators, manualSection])
        out.sort()
        return out

    @staticmethod
    def putSectionsTable():
        # Updates sections, based on a table of attributes in string format.
        # Uses as input the table in the format provided by the 
        # getSectionsTable method and contained in current settings
        for i in ADsettings.sections:
            section = ADsection.getByName(i[0])
            if section != None:
                if i[1] == "NO":
                    section.direction = 3
                elif (i[1] == "CCW" or i[1] == "EAST" or i[1] == "NORTH"
                      or i[1] == "LEFT" or i[1] == "UP"):
                    section.direction = ADsettings.ccw + 1
                    section.transitOnly[ADsettings.ccw] = False
                elif (i[1] == "CW" or i[1] == "WEST" or i[1] == "SOUTH"
                      or i[1] == "RIGHT" or i[1] == "DOWN"):
                    section.direction = 2 - ADsettings.ccw
                    section.transitOnly[1-ADsettings.ccw] = False
                i2 = i[2]
                if i2.endswith("+"):
                    i2 = i2[:len(i2)-1]
                    burst = True
                else:
                    burst = False
                if i2 == "NO":
                    section.transitOnly[0] = section.transitOnly[1] = False
                    section.burst = False
                elif (i2 == "CCW-CW" or i2 == "EAST-WEST" or i2 == "NORTH-SOUTH"
                      or i2 == "LEFT-RIGHT" or i2 == "UP-DOWN"):
                    section.transitOnly[0] = section.transitOnly[1] = True
                    section.burst = burst
                elif (i2 == "CCW" or i2 == "EAST" or i2 == "NORTH"
                      or i2 == "LEFT" or i2 == "UP"):
                    section.transitOnly[ADsettings.ccw] = True
                    section.transitOnly[1-ADsettings.ccw] = False
                    section.burst = burst
                elif (i2 == "CW" or i2 == "WEST" or i2 == "SOUTH"
                      or i2 == "RIGHT" or i2 == "DOWN"):
                    section.transitOnly[1-ADsettings.ccw] = True
                    section.transitOnly[ADsettings.ccw] = False
                    section.burst = burst
                for j in range(2):
                    if (i[j + 3] != "" and (section.signal[j] == None or 
                        i[j + 3] != section.signal[j].name)):
                        if section.signal[j] != None:
                            section.signal[j].changeUse(-2)
                        section.signal[j] = ADsignalMast.provideSignal(i[j + 3])
                        section.signal[j].changeUse(1)
                if i[5] == "INVERTED":
                    section.manuallyFlip()
                sensorName = i[6]
                if sensorName == "NO":
                    section.manualSensor = None
                elif sensorName.strip() != "":
                    section.manualSensor = (
                                            InstanceManager.sensorManagerInstance(
                                            ).getSensor(sensorName))
                if len(i) > 7:
                    section.stopAtBeginning = i[7]
                    if len(i) > 9 and ADsettings.autoRestart:
                        for j in range(2):
                            if (i[8][j] == "Held" and section.signal[j] != None
                                and section.signal[j].hasIcon()):
                                section.signal[j].setHeld(True)
                        if i[9] == "Manual":
                            section.setManual(True)

    @staticmethod
    def getBlocksTable():
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
        for sectionName in sections:
            section = ADsection.getByName(sectionName)
            for block in section.getBlocks(True):
                outData = [block.getName()]
                for j in range (2):
                    if block == section.stopBlock[j]:
                        outData.append("STOP")
                    else:
                        outData.append("")
                    if block == section.allocationPoint[j]:
                        outData.append("ALLOCATE")
                    else:
                        outData.append("")
                    if block == section.safePoint[j]:
                        outData.append("SAFE")
                    else:
                        outData.append("")
                    speedIndex = block.getSpeed(j)
                    if speedIndex == 0:
                        outData.append("")
                    else:
                        outData.append(
                                       ADsettings.speedsList[speedIndex-1])
                    if block == section.brakeBlock[j]:
                        outData.append("BRAKE")
                    else:
                        outData.append("")
                    outData.append(block.action[j])
                out.append(outData)
        return out

    @staticmethod
    def putBlocksTable():
        # Updates blocks, based on a table of attributes in string format.
        # Uses as input the table in the format provided by the 
        # getBlocksTable method and contained in current settings
        for i in ADsettings.blocks:
            block = ADblock.getByName(i[0])
            if block != None:
                section = block.getSection()
                jj = 1
                for j in range(2):
                    if i[jj] == "STOP":
                        section.stopBlock[j] = block
                    jj += 1
                    if i[jj] == "ALLOCATE":
                        section.allocationPoint[j] = block
                    jj += 1
                    if i[jj] == "SAFE":
                        section.safePoint[j] = block
                    elif i[jj] == "NO"  and section.safePoint[j] == block:
                        section.safePoint[j] = None
                    jj += 1
                    speedName = i[jj]
                    for speedIndex in range(len(ADsettings.speedsList)):
                        if (speedName == 
                            ADsettings.speedsList[speedIndex]):
                            block.speed[j] = speedIndex + 1
                            break
                    jj += 1
                    if i[jj] == "BRAKE":
                        section.brakeBlock[j] = block
                    jj += 1
                    block.action[j] = i[jj]
                    jj += 1

    @staticmethod
    def setListeners():
        # Add a property change listener for each section that has a sensor 
        # for manual control
        for section in ADsection.systemNames.values():
            if(section.manualSensor != None):
                # add the listener
                section.manualSensor.addPropertyChangeListener(section)

    @staticmethod
    def removeListeners():
        # Remove the manual control sensor listener of each section
        for section in ADsection.systemNames.values():
            if(section.manualSensor != None):
                # remove the listener
                section.manualSensor.removePropertyChangeListener(section)

    def __init__(self, systemName):
        # Retrieve Section.java instance from JMRI
        self.jmriSection = (
                            InstanceManager.sectionManagerInstance().getBySystemName(systemName))
        # Get section name
        self.name = self.jmriSection.getUserName()
        if self.name == None or self.name.strip() == "":
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
        while newBlock != None:
            # Get the corresponding LayoutBlock
            # Without LayoutBlock we cannot setup connectivity
            layoutBlock = None
            blockName = newBlock.getUserName()
            if blockName != None and blockName.strip() != "":
                layoutBlock = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getLayoutBlock(blockName)
            if layoutBlock == None:
                blockName = newBlock.getSystemName()
                layoutBlock = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager).getLayoutBlock(blockName)
            if layoutBlock == None:
                AutoDispatcher.log("No LayoutBlock for Block " + blockName
                                   + " found: block skipped!")
            else:
                # make sure that block is included in one section only
                block = ADblock.getByName(blockName)
                if block != None:
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
        if blocksNumber < 1:
            AutoDispatcher.log("Section " + self.name
                               + "contains no valid  block. Section skipped!")
            return
        # Update the maximum number of blocks per section (will be used to 
        # determine default settings)
        if AutoDispatcher.maxBlocksPerSection < blocksNumber:
            AutoDispatcher.maxBlocksPerSection = blocksNumber
        # Include names in dictionaries
        ADsection.systemNames[AutoDispatcher.cleanName(systemName)] = self
        ADsection.userNames[self.name] = self
        self.forwardEntries = []      # Entries
        self.reverseEntries = []      # Exits
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
        if ADsection.sensorNames == None:
            ADsection.sensorNames = [""]
            # Remove from the list sensors associated with blocks
            # (this makes user selection easier and faster)
            # Get all blocks
            blocks = InstanceManager.blockManagerInstance().getSystemNameList()
            blockSensors = []
            for b in blocks:
                sensor = InstanceManager.blockManagerInstance(
                                                              ).getBlock(b).getSensor()
                if sensor != None:
                    blockSensors.append(sensor.getSystemName())
            sensors = InstanceManager.sensorManagerInstance(
                                                            ).getSystemNameList()
            for s in sensors:
                if not s in blockSensors:
                    sensorName = InstanceManager.sensorManagerInstance(
                                                                       ).getSensor(s).getUserName()
                    if sensorName == None or sensorName.strip() == "":
                        sensorName = s
                    if sensorName != "ISCLOCKRUNNING":
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
        if len(self.blockList) > 1:
            # Set default brake block to block preceding stop block
            for i in range(2):
                found = False
                for block in self.getBlocks(1-i):
                    if found:
                        self.brakeBlock[i] = block
                        break
                    found = block == self.stopBlock[i]
        else:
            # Section contains only one block. 
            # Brake and safe points are meaningless
            self.brakeBlock[0] = self.brakeBlock[1] = None
            self.safePoint[0] = self.safePoint[1] = None
            
    def __setEntries__(self, entries):
        # Internal method used to retrieve entry points from JMRI
        entryPoints = []
        for i in range(entries.size()):
            # Get a JMRI EntryPoint
            entry = entries.get(i)
            # Create our own entry instance
            entryPoint = ADentry(self, entry)
            # If the creation failed, discard the instance
            if not entryPoint.inError():
                entryPoints.append(entryPoint)
                entryPoint.getInternalBlock().entryBlock = True
        return entryPoints

    def __setStopBlocks__(self, blocks, entries):
        # Stop point is set to the first exit block encountered
        block = None
        for b in blocks:
            for e in entries:
                if b == e.getInternalBlock():
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
            AutoDispatcher.maxBlocksPerSection > 1)):
            self.transitOnly[0] = self.transitOnly[1] = True

    def getName(self):
        return self.name
        
    def getJmriSection(self):
        return self.jmriSection
        
    def getEntries(self, backwards):
        # Return entries (backwards == False)
        # or exits (backwards == True)
        # swapping them if order of blocks is reversed
        if backwards == self.reversed:
            return self.forwardEntries
        else:
            return self.reverseEntries

    def isReversed (self):
        # Is the order of blocks reversed?
        return self.reversed

    def setReversed (self, reversed):
        # Set the indicator of reversed order of blocks
        if reversed == self.reversed:
            return
        self.reversed = reversed
        # Flip contents of tables
        self.stopBlock.reverse()
        self.brakeBlock.reverse()
        self.allocationPoint.reverse()
        self.safePoint.reverse()
        # Do the same for blocks
        for block in self.blockList:
            block.speed.reverse()
            block.action.reverse()
        
    def getSignal(self, direction):
        if direction < 0:
            direction = 0
        elif direction > 1:
            direction = 1
        return self.signal[direction]

    def getStopAtBeginning(self, direction):
        if direction < 0:
            direction = 0
        elif direction > 1:
            direction = 1
        return self.stopAtBeginning[direction]

    def manuallyFlip(self):
        # Reverse section's direction and take note that user requested flipping
        # (Can occur only for reversing-sections)
        self.setReversed(not self.reversed)
        self.manuallyFlipped = not self.manuallyFlipped
        
    def setOccupied(self):
        # Set section to occupied (unless already set)
        if not self.occupied:
            self.empty = False
            self.occupied = True
            # Update section color
            self.setColor()

    def checkOccupancy(self):
        # If already empty, ignore (shouldn't happen)
        if not self.occupied and self.allocated == None:
            return
        # Clear occupancy status only if all sub-blocks are empty
        for b in self.blockList:
            if b.getOccupancy() == Block.OCCUPIED:
                return
        # report error if the train "disappeared"
        if self.trainHead and self.allocated != None:
            # Avoid the warning when user is allowed to manually perform
            # switching operations
            if (not AutoDispatcher.stopped and not AutoDispatcher.paused and 
                not self.isManual() and
                ADsettings.derailDetection != ADsettings.DETECTION_DISABLED):
                if (ADsettings.derailDetection == 
                    ADsettings.DETECTION_PAUSE):
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

    def freeSectionIfTrainSafe(self):
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
        if not self.occupied and train == None:
            # Already released (can occur if sensor is re-triggered)
            return
        # Is section allocated to a train?
        if train != None:
            # Is train's tail in this section?
            if (len(train.previousSections) > 0 and
                train.previousSections[0] != self):
                # No - Try releasing previous sections (if empty)
                previousCount = 0
                while True:
                    # Recursivelly call "freeSectionIfTrainSafe" while 
                    # sections continue being released
                    currentCount = len(train.previousSections)
                    if currentCount == 0 or currentCount == previousCount:
                        break
                    previousCount = currentCount
                    train.previousSections[0].freeSectionIfTrainSafe()
                return
            # Yes, section contains train's tail - Is train using 
            # resistive wheels?
            if not train.resistiveWheels:
                # No - Did train reach the safe point?
                if not train.safe:
                    # No - Can we release the section based on train's length?
                    if (not ADsettings.useLength or
                        train.trainLength <= 0.):
                        # No - We can thus not release the section
                        return
                    # Yes - See if the head of the train is far enough
                    if train.trainLength > train.distance:
                        # Not yet
                        return
        if not self.empty:
            # At least one block sensor still active
            # Test for lost cars
            if (ADsettings.lostCarsDetection ==
                ADsettings.DETECTION_DISABLED or AutoDispatcher.paused or
                AutoDispatcher.stopped or train == None):
                return
            # Can we base detection of lost cars on train length?
            if (ADblock.blocksWithoutLength == 0 and
                train.trainLength > 0.):
                # Yes, is the head of the train not too far from the tail?
                # (Let's introduce some tollerance to compensate 
                # for sensors debouncing)
                if (train.distance - train.trainLength
                    < ADsettings.lostCarsTollerance):
                    # Not too far
                    return
            else:
                # No length available
                # Let's base detection on the number of sections 
                # not yet released
                if (len(train.previousSections) <=
                    ADsettings.lostCarsSections):
                    # Not too far
                    return
            if (ADsettings.lostCarsDetection ==
                ADsettings.DETECTION_PAUSE):
                AutoDispatcher.instance.stopAll()
            AutoDispatcher.chimeLog(ADsettings.LOST_CARS_SOUND,
                                    "Train \"" + train.getName() + 
                                    "\" lost cars in section \"" + self.name + "\"")
            return
        # One way or another we found that we can release the section
        self.occupied = False
        if train != None:
            # Update distance of train head from section boundary
            if len(train.previousSections) < 3:
                # If train is in the next section, clear distance
                # We cannot release next section while the train is in it!
                train.distance = 0.
                train.blockLength = 0.
            else:
                # If train head is two or more sections ahead
                # decrease total distance by present section length
                train.distance -= self.sectionLength
                if train.distance < 0.:
                    train.distance = 0.
                train.blockLength = train.block.getLength()
            # Remove section from the list of train's passed sections (if any)
            while self in train.previousSections:
                train.previousSections.pop(0)
            # Take note that the section is not allocated any more
            self.allocate(None, 0)
        # Reset signals
        if self.signal[0] != None:
            self.signal[0].setIndication(0)
        if self.signal[1] != None:
            self.signal[1].setIndication(0)
        # Update section color
        self.setColor()
        # Should train stop at the start of next section?
        if train == None or not train.shouldStopAtBeginning():
            return
        if(len(train.previousSections) > 0 and
           train.previousSections[0] != train.section):
            return
        # Yes, stop train
        start_new_thread(self.stopTrain,
                         (train, train.section.stopAtBeginning[train.direction], ))
        
    def stopTrain(self, train, delay):
        # Stop a train after an optional delay
        if delay > 0:
            sleep(delay)
        # Make sure train is still braking
        if train.speedLevel != 1:
            return
        train.arrived()
        if (train.locomotive == None or train.engineerSetLocomotive == None
            or AutoDispatcher.simulation):
            train.stop()
        if train.locomotive != None:
            train.locomotive.learningStop()
        train.changeSpeed(0)
        
    def isOccupied (self):
        return self.occupied

    def allocate(self, train, direction):
        # Called when:
        #   section is allocated to a new train
        #   section is de-allocated
        #   train direction changed 
        # Update JMRI section state, if requested
        if ADsettings.sectionTracking:
            if self.trainDirection != direction or self.allocated != train:
                if train == None:
                    if self.allocated != None:
                        self.jmriSection.setState(Section.FREE)
                else:
                    if direction == self.reversed:
                        self.jmriSection.setState(Section.REVERSE)
                    else:
                        self.jmriSection.setState(Section.FORWARD)
        self.trainDirection = direction
        # If only direction changed, return
        if self.allocated == train:
            return
        self.trainHead = train != None and self.occupied
        self.allocated = train
        # Change train name in blocks (if required)
        self.changeTrainName()
        # Clear length.  Will be recomputed as the train advances
        self.sectionLength = 0.
        self.setColor()
                            
    def changeTrainName(self):
        # Place/remove train name in jmri Memory Variable (if defined)
        # and in jmri Blocks (only if user enabled this option)
        if self.memoryVariable != None:
            if self.allocated == None:
                self.memoryVariable.setValue("")
            else:
                self.memoryVariable.setValue(self.allocated.getName())
        if not ADsettings.blockTracking:
            return
        if self.allocated == None:
            if self.occupied:
                for block in self.blockList:
                    block.setValue("")
        else:
            if self.occupied:
                trainName = self.allocated.getName()
                for block in self.blockList:
                    if block.getOccupancy() == Block.OCCUPIED:
                        block.setValue(trainName)
       
    def getAllocated (self):
        return self.allocated

    def isAvailable(self):
        # Check if section is available
        if self.occupied or self.allocated != None:
            # Section occupied or allocated
            return False
        # If the section has crossings, check the status of crossed sections
        for x in self.xings:
            if x.isOccupied() or x.getAllocated() != None:
                return False
        # Check if section is under manual control
        return not self.isManual()

    def isManual(self):
        if self.manualSensor == None:
            return False
        return self.manualSensor.getKnownState() == Sensor.ACTIVE

    def setManual(self, on):
        # DO it only if user defined a manual control sensor
        if self.manualSensor != None:
            if on:
                self.manualSensor.setKnownState(Sensor.ACTIVE)
            else:
                self.manualSensor.setKnownState(Sensor.INACTIVE)

    def setColor(self):
        # Set section color depending on section status
        # (only if user enabled this option)
        if AutoDispatcher.stopped or not ADsettings.useCustomColors:
            return
        if self.allocated != None:
            if self.occupied:
                if not self.allocated.running and self.isManual():
                    color = ADsettings.sectionColor[
                        ADsection.MANUAL_SECTION_COLOR]
                elif self.allocated.getDirection() == ADsettings.ccw:
                    color = ADsettings.sectionColor[ADsection.CCW_TRAIN_COLOR]
                else:
                    color = ADsettings.sectionColor[ADsection.CW_TRAIN_COLOR]
            elif self.allocated.getDirection() == ADsettings.ccw:
                color = ADsettings.sectionColor[ADsection.CCW_ALLOCATED_COLOR]
            else:
                color = ADsettings.sectionColor[ADsection.CW_ALLOCATED_COLOR]
        elif self.isManual():
            color = ADsettings.sectionColor[ADsection.MANUAL_SECTION_COLOR]
        elif self.occupied:
            color = ADsettings.sectionColor[ADsection.OCCUPIED_SECTION_COLOR]
        else:
            color = ADsettings.sectionColor[ADsection.EMPTY_SECTION_COLOR]
        for block in self.blockList:
            block.setColor(color)

    def getBlocks(self, direction):
        # Returns the list of blocks contained in the section
        # sorted in accordance to train direction
        if direction != self.reversed:
            return self.blockList
        l = []
        l.extend(self.blockList)
        l.reverse()
        return l

    def updateFromSwing(self):
        # Update section settings in accordance to input swing fields
        direction = self.oneWaySwing.getSelectedItem()
        transitOnly = self.transitOnlySwing.getSelectedItem()
        if transitOnly.endswith("+"):
            burst = True
            transitOnly = transitOnly[:len(transitOnly)-1]
        else:
            burst = False
        self.direction = 3
        if direction == ADsettings.directionNames[0]:
            self.direction = ADsettings.ccw + 1
        elif direction == ADsettings.directionNames[1]:
            self.direction = 2 - ADsettings.ccw
        self.transitOnly[0] = self.transitOnly[1] = False
        self.burst = False
        if (transitOnly == ADsettings.directionNames[0] or 
            transitOnly == ADsettings.directionNames[0] + 
            "-" + ADsettings.directionNames[1]):
            self.transitOnly[ADsettings.ccw] = True
            self.burst = burst
        if (transitOnly == ADsettings.directionNames[1] or 
            transitOnly == ADsettings.directionNames[0] + 
            "-" + ADsettings.directionNames[1]):
            self.transitOnly[1-ADsettings.ccw] = True
            self.burst = burst
        for j in range(2):
            newSignalName = self.signalSwing[j].getSelectedItem()[3:]
            if newSignalName != self.signal[j].name and newSignalName.strip() != "":
                if self.signal[j] != None:
                    self.signal[j].changeUse(-1)
                self.signal[j] = ADsignalMast.provideSignal(newSignalName)
                self.signal[j].changeUse(1)
            if self.stopAtBeginningSwing[j].isSelected():
                try:
                    self.stopAtBeginning[j] = float(self.stopAtBeginningDelay[j].text)
                except:
                    self.stopAtBeginning[j] = 0.0
                    self.stopAtBeginningDelay[j].text = "0.0"
            else:
                self.stopAtBeginning[j] = -1.0
                self.stopAtBeginningDelay[j].text = "0.0"
        sensorName = self.manualSensorSwing.getSelectedItem()
        if sensorName == None:
            self.manualSensor = None
        else:
            self.manualSensor = InstanceManager.sensorManagerInstance(
                                                                      ).getSensor(sensorName)


    def propertyChange(self, event):
        # Listener, invoked when the Manual sensor status changes
        if (event.getPropertyName() != "KnownState" or 
            event.newValue == event.oldValue):
            return
        if (event.newValue == Sensor.ACTIVE 
            or event.newValue == Sensor.INACTIVE):
            # Status changed
            self.setColor()
                    
# ENTRY POINT ==============

