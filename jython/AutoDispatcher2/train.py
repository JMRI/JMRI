from java.lang import System
from javax.swing import JButton
from javax.swing import JCheckBox
from javax.swing import JComboBox
from javax.swing import JLabel
from javax.swing import JOptionPane
from javax.swing import JTextField
from jmri import InstanceManager
from jmri import Sensor
from jmri import Turnout
from thread import start_new_thread
from time import sleep

class ADtrain:
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

    @staticmethod
    def getList():
        return ADtrain.trains
    
    @staticmethod
    def remove(train):
        ADtrain.trains.remove(train)

    @staticmethod
    def buildRoster(trains):
    # Build trains roster, based on user settings
    # (retrieved from disk or from defaults for example panels)
        for t in trains:
            tt = ADtrain(t[0])
            tt.setDirection(t[2])
            section = ADsection.getByName(t[1])
            if section != None:
                tt.setSection(section, not ADsettings.autoRestart)
            tt.resistiveWheels = t[3]
            tt.setSchedule(t[4])
            tt.trainAllocation = t[5]
            tt.trainLength = t[6]
            # Set schedule info
            if tt.section != None:
                tt.schedule.stack = t[7]
                tt.schedule.pointer = tt.schedule.stack[
                    len(tt.schedule.stack)-1]
                tt.schedule.pop()
                if len(t) > 12:
                    if t[12] != "":
                        tt.lastRouteSection = ADsection.getByName(t[12])
            tt.changeLocomotive(t[8])
            tt.setReversed(t[9])
            # Rebuild pending commands, replacing names with instances
            for item in t[10]:
                section = ADsection.getByName(item[0])
                if section != None:
                    scheduleItem = ADscheduleItem()
                    scheduleItem.action = item[1]
                    scheduleItem.value = item[2]
                    if len(item) > 3:
                        scheduleItem.message = item[3]
                    if (scheduleItem.action == ADschedule.WAIT_FOR or 
                        scheduleItem.action == ADschedule.MANUAL_OTHER):
                        scheduleItem.value = ADsection.getByName(
                                                                 scheduleItem.value)
                        if scheduleItem.value == None:
                            continue
                    elif (scheduleItem.action == ADschedule.HELD or
                          scheduleItem.action == ADschedule.RELEASE or
                          scheduleItem.action == ADschedule.IFH):
                        scheduleItem.value = ADsignalMast.getByName(
                                                                    scheduleItem.value)
                        if scheduleItem.value == None:
                            continue
                    tt.itemSections.append(section)
                    tt.items.append(scheduleItem)
            tt.engineerName = t[11]
            tt.trainSpeed = t[13]
            tt.brakingHistory = t[14]
            if len(t) > 15:
                tt.canStopAtBeginning = t[15]
                if len(t) > 16:
                    tt.startAction = t[16]
            tt.updateSwing()

    def __init__(self, trainName):
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
        if ADtrain.sectionsList == None:
            ADtrain.sectionsList = [""]
            for section in ADsection.getList():
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

    def getName(self):
        return self.name
        
    def getCanStopAtBeginning(self):
        return self.canStopAtBeginning
        
    def getLength(self):
        return self.trainLength

    def whenDeleteTrainClicked(self, event):
        # Ask confirmation before deleting the train!
        if (JOptionPane.showConfirmDialog(None, "Remove train \""
            + self.name + "\"?", "Confirmation",
            JOptionPane.YES_NO_OPTION) == 1):
            return
        self.updating = True
        if self.running:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Train " + self.name + " running: cannot be deleted")
            self.updating = False
            return
        if self.opTrain != None and self.status == ADtrain.END_OF_SCHEDULE:
            try:
                self.opTrain.move()
            finally:
                i = 0 # Useless, just to complete the try construct
        AutoDispatcher.trainsFrame.deleteTrain(self)
        self.updating = True
              
    # define what Apply button in Trains Window does when clicked
    def whenChangeClicked(self, event):
        self.trainChange()

    # define what Set button in Train Detail Window does when clicked
    def whenSetClicked(self, event):
        self.scheduleSwing.text = AutoDispatcher.trainDetailFrame.scheduleSwing.text
        self.trainChange()
        
    def trainChange(self):
        # Get input values from "Trains" and/or "Train Detail" windows
        # Check if this train is shown in the "Train Detail" window
        detail = (AutoDispatcher.trainDetailFrame != None and 
                  AutoDispatcher.trainDetailFrame.train == self)
        # Take note that instance is being updated, preventing other 
        # threads from using it
        self.updating = True
        # Make sure another thread is not starting the train
        # (Let's cope with Jython's lack of synchronization)
        if self.running:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Train "
                                    + self.name + " running: changes cannot be applied")
            self.updating = False
            return
        # Change train's name
        newName = self.nameSwing.text
        if newName.strip() == "":
            self.nameSwing.setText(oldName)
        elif newName != self.name:
            self.name = newName
            AutoDispatcher.setTrainsDirty()
            # Update train name in jmri Blocks (if needed)
            if self.section != None:
                if not self.section in self.previousSections:
                    self.section.changeTrainName()
                for section in self.previousSections:
                    section.changeTrainName()
        # Change train's direction
        oldDirection = self.direction
        self.setDirection(self.directionSwing.getSelectedItem())
        # If direction changed, take note that we need to restart schedule
        reSchedule = oldDirection != self.direction
        # Change train's section
        sectionName = self.sectionSwing.getSelectedItem()
        if sectionName.strip() == "":
            newSection = None
        else:
            newSection = ADsection.getByName(sectionName)
        if newSection != self.section:
            # Train was manually moved to another section
            # Take note that we need to restart schedule
            reSchedule = True
            self.setSection(newSection, True)
            self.schedule = None
        # Change train's locomotive
        loco = self.locoRoster.getSelectedItem()
        if loco != "" and loco != self.locoName:
            self.changeLocomotive(loco)
            AutoDispatcher.setTrainsDirty()
        # Change locomotive orientation
        newReversed = self.reversedSwing.isSelected()
        if self.reversed != newReversed:
            self.setReversed(newReversed)
            AutoDispatcher.setTrainsDirty()
        # Change train's schedule
        newSchedule = self.scheduleSwing.text
        if (self.schedule == None or self.schedule.pointer >= len(self.schedule.source)
            or self.schedule.text != newSchedule or reSchedule):
            self.setSchedule(newSchedule)
            AutoDispatcher.setTrainsDirty()
        if detail:
            AutoDispatcher.trainDetailFrame.scheduleSwing.text = (
                                                                  self.scheduleSwing.text)
            # Change train's resistive wheels indicator
            newResistiveWheels = self.resistiveSwing.isSelected()
            if self.resistiveWheels != newResistiveWheels:
                self.resistiveWheels = newResistiveWheels
                AutoDispatcher.setTrainsDirty()                
            canStopAtBeginningSwing = self.canStopAtBeginningSwing.isSelected()
            if self.canStopAtBeginning != canStopAtBeginningSwing:
                self.canStopAtBeginning = canStopAtBeginningSwing
                AutoDispatcher.setTrainsDirty()
            # Change train length
            oldLength = self.trainLength
            try:
                self.trainLength = (float(self.trainLengthSwing.text) *
                                    ADsettings.units)
            except:
                self.trainLength = 0
                self.trainLengthSwing.text = "0"
            if oldLength != self.trainLength:
                AutoDispatcher.setTrainsDirty()
            # Change number of sections ahead to be allocated for this train
            newAllocation = self.trainAllocationSwing.getSelectedIndex()
            if newAllocation != self.trainAllocation:
                self.trainAllocation = newAllocation
                AutoDispatcher.setTrainsDirty()
            # Change engineer
            newEngineerName = self.engineerSwing.getSelectedItem()
            if newEngineerName != self.engineerName:
                self.setEngineer(newEngineerName)
                AutoDispatcher.setTrainsDirty()
            # Change train speeds table
            ind = 0
            for s in AutoDispatcher.trainDetailFrame.trainSpeedSwing:
                newSpeed = s.getSelectedIndex() + 1
                if newSpeed != self.trainSpeed[ind]:
                    self.trainSpeed[ind] = newSpeed
                    AutoDispatcher.setTrainsDirty()
                ind += 1
            if self.startAction != self.startActionSwing.text:
                self.startAction = self.startActionSwing.text
                AutoDispatcher.setTrainsDirty()
        # Update swing fields
        self.updateSwing()
        if AutoDispatcher.trainsFrame != None:
            AutoDispatcher.trainsFrame.reDisplay()
        # Updating complete, let other threads use this instance
        self.updating = False
        if detail:
            AutoDispatcher.trainDetailFrame.dispose()
            AutoDispatcher.trainDetailFrame = None
        AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND, "Changes for train \""
                                + self.name + "\" applied")

    def setDirection(self, direction):
        # Set train direction
        if (direction == "CCW" or direction == "EAST" or direction == "NORTH"
            or direction == "LEFT" or direction == "UP"):
            newDirection = ADsettings.ccw
        else:
            newDirection = 1 - ADsettings.ccw
        if newDirection == self.direction:
            return
        self.direction = newDirection
        if self.section != None:
            self.section.trainDirection = self.direction
    def getDirection(self):
        return self.direction

    def setSection(self, newSection, held):
        # Set initial position of train
        if newSection != self.section:
            # Train placed in a new section
            if self.section != None:
                # Remove train from the old sections
                self.section.trainHead = False
                self.releaseSections()
                # self.section.allocate(None, 0)
                self.section = None
            self.safe = False
            self.simSensor = None
            if newSection != None:
                # Check that section is not allocated to another train
                otherTrain = newSection.getAllocated()
                if otherTrain != None and otherTrain != self:
                    AutoDispatcher.message("Section " + newSection.getName() +
                                           " already allocated to train " + otherTrain.getName())
                    newSection = None
                else:
                    # Check that section is actually occupied
                    newSection.occupied = True
                    newSection.checkOccupancy()
                    if not newSection.isOccupied():
                        # Train is being placed in an empty section
                        # Are we running in simulation mode?
                        if AutoDispatcher.simulation:
                            # Simulation mode - Force section occupancy
                            # Find out block to be used
                            sensor = newSection.stopBlock[
                                self.direction].getOccupancySensor()
                            if sensor != None:
                                # Avoid "wrong route" error
                                wrongRoute = ADsettings.wrongRouteDetection
                                ADsettings.wrongRouteDetection = (
                                                                  ADsettings.DETECTION_DISABLED)
                                newSection.occupied = True
                                newSection.empty = False
                                # Force occupancy
                                sensor.setKnownState(Sensor.ACTIVE)
                                ADsettings.wrongRouteDetection = wrongRoute
                            else:
                                # Block has no sensor! :-O
                                newSection = None
                        else:
                            # We are not running in simulation mode.
                            # User should place train on tracks before 
                            # defining it (unless derailment detection is disabled)!
                            if (ADsettings.derailDetection != ADsettings.DETECTION_DISABLED):
                                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                                        "Section " + newSection.getName() +
                                                        " is empty. Place train "
                                                        + self.name + " on tracks!")
                                # Attempt to assign train to an empty section, 
                                if (ADsettings.derailDetection == ADsettings.DETECTION_PAUSE):
                                    # set section to undefined
                                    newSection = None
                # Was assignment successful?
                if newSection != None:
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
                    if held and s.hasIcon():
                        s.setHeld(True)
                else:
                    # Unsuccessful assignment - clear section name
                    self.sectionSwing.setSelectedItem("")

    def releaseSections(self):
        # Routine to release sections occupied by the train,
        # when manually moving it to another section or deleting it
        for section in ADsection.getList():
            if section.getAllocated() == self:
                section.allocate(None, 0)
                if AutoDispatcher.simulation:
                    section.occupied = False
                    section.empty = True
                    for block in section.getBlocks(True):
                        sensor = block.getOccupancySensor()
                        if sensor != None:
                            sensor.setKnownState(Sensor.INACTIVE)
                            block.adjustWidth()
                else:
                    section.checkOccupancy()
                section.setColor()
                AutoDispatcher.repaint = True

    def changeSection(self, newSection):
        # Change section containing train's head while train is running
        # Knowing where the head of the train is located is
        # necessary for proper action of signals and 
        # to detect derailments (or other malfunctions)
        # If a section containing the head of the train becomes empty,
        # something went wrong!
        # Avoid re-triggering a previous section
        if newSection in self.previousSections or newSection == self.section:
            return
        # Train moved to a new section
        if self.section != None:
            # Remove the head of the train from the old section (if any)
            self.section.trainHead = False
            # Reset signals
            if self.section.signal[0] != None:
                self.section.signal[0].setIndication(0)
            if self.section.signal[1] != None:
                self.section.signal[1].setIndication(0)
        # Since train is just entering a new section it cannot be safely
        # recovered in it
        self.safe = False
        self.section = newSection
        if self.section != None:
            # Must previous sections be still released?
            if len(self.previousSections) == 0:
                # No, clear distance from previous sections
                    self.distance = 0.
                    self.blocklength = 0.
            # Is train moving to a section along the route? (it should)
            if self.section in self.sectionsAhead:
                # Yes
                # Remove it and all previous sections from the route,
                # adjusting train direction (if changed)
                oldDirection = self.direction
                while self.section in self.sectionsAhead:
                    # Adjust direction
                    passedSection = self.sectionsAhead.pop(0)
                    self.direction = passedSection.trainDirection
                    # Mark section as occupied
                    passedSection.setOccupied()
                    passedSection.checkOccupancy()
                    # Update number of allocated section
                    if (self.allocatedSections > 0 and 
                        (not passedSection.transitOnly[self.direction] or
                        passedSection.getSignal(self.direction).hasHead())):
                        self.allocatedSections -= 1
                    # Keep note that this section was passed and must still 
                    # be released
                    self.previousSections.append(passedSection)
                self.section.trainHead = True
                # Update Trains window if direction changed
                if self.direction != oldDirection:
                    self.updateSwing()
                    # Update sections color if direction changed
                    for section in self.previousSections:
                        section.setColor()
                    for section in self.sectionsAhead:
                        section.setColor()
                # Update Trains window (if open)
                self.sectionSwing.setSelectedItem(self.section.getName())
                # Move ahead train position in Operations module (if any)
                if self.opTrain != None:
                    if self.opLength > 0.:
                        self.trainLength = self.opLength
                        self.opLength = 0.
                    try:
                        nextLocation = ADlocation.getByName(
                                                            self.opTrain.getNextLocationName())
                        if nextLocation != None:
                            if self.section in nextLocation.getSections():
                                self.opTrain.move()
                                opCurrent = self.opTrain.getCurrentLocation()
                                if opCurrent != None:
                                    self.opLength = round(float(
                                                          opCurrent.getTrainLength(
                                                          )) * 304.8 / ADsettings.scale)
                    finally:
                        i = 0
            else:
                # Train ended nowhere :-O
                self.sectionSwing.setSelectedItem("")
            # Should train stop at the start of this section?
            if self.shouldStopAtBeginning():
                if self.locomotive.learningBrake():
                    if self.speedLevel > self.maxSpeed:
                        self.changeSpeed(self.maxSpeed)
                else:
                    # Yes, brake
                    self.changeSpeed(1)

    def shouldStopAtBeginning(self):
        if not self.canStopAtBeginning:
            return False
        if self.section != self.destination:
            return False
        if self.section.stopAtBeginning[self.direction] < 0:
            return False
        return True

    def changeBlock(self, newBlock):
        # Check if we need to compute distance from previous sections
        if len(self.previousSections) == 0:
            firstPassedSection = self.section
        else:
            firstPassedSection = self.previousSections[0]
        speed = self.speedLevel
        # Remove block from list of expected blocks in order to
        # avoid re-triggering. Also blocks that were skipped
        # i.e. did not trigger any event (malfunctioning sensor?)
        # are processed
        while newBlock in self.blocksAhead:
            self.previousBlock = self.block
            self.block = self.blocksAhead.pop(0)
            self.lastMove = System.currentTimeMillis()
            section = self.block.getSection()
            # Update maximum speed
            newSpeed = self.block.getSpeed(self.direction)
            if newSpeed > 0:
                self.maxSpeed = newSpeed
                # Apply new speed if train is not braking or stopping
                if speed > 1:
                    speed = self.maxSpeed
            self.changeSection(section)
            # Update distance from previous sections (unless we are still 
            # in the first section of the route)
            if firstPassedSection != section:
                self.distance += self.blockLength
                self.blockLength = self.block.getLength()
                section.sectionLength += self.blockLength
            # Update locomotive mileage
            if self.locomotive != None:
                # We actually do it in advance, but for our purposes it's fine
                self.locomotive.mileage += self.block.getLength()
            # allocation point?
            if section.allocationPoint[self.direction] == self.block:
                self.allocationReady = True
            if section == self.section:
                # safe point?
                if self.section.safePoint[self.direction] == self.block:
                    # Take note that the train is safely contained within
                    # section boundaries
                    self.safe = True
                # Is train still starting ?
                if self.status != ADtrain.STARTING:
                    # No, get speed indicated by signal
                    restrictedSpeed = self.section.getSignal(
                                                             self.direction).getSpeed()
                    # Should train stop at start of section ?
                    shouldStop = self.shouldStopAtBeginning()
                    # stop block?
                    if self.section.stopBlock[self.direction] == self.block:
                        # Apply restricted speed
                        # if restrictedSpeed > 0 :
                        speed = restrictedSpeed
                        if speed == 0:
                            if not shouldStop:
                                self.arrived()
                            # If we have a locomotive, inform it that train
                            # reached stop block (in case it's implementing
                            # self-learning braking)
                            if self.locomotive != None:
                                self.locomotive.learningStop()
                            # If we don't have a locomotive or are running in
                            # simulation, assume that train stops
                            if (self.locomotive == None 
                                or self.engineerSetLocomotive == None
                                or AutoDispatcher.simulation):
                                self.stop()
                    elif shouldStop:
                        speed = self.speedLevel
                    # Brake block?
                    elif self.section.brakeBlock[self.direction] == self.block:
                        # Set speed to minimum if exit signal is red
                        if restrictedSpeed == 0:
                            # If we have a locomotive, inform it that train
                            # starts braking (in case it's implementing
                            # self-learning braking)
                            if self.locomotive != None:
                                if not self.locomotive.learningBrake():
                                    # self-learning not active
                                    # Brake immediately
                                    speed = 1
                            else:
                                # Self-learning braking not implemented
                                # Brake immediately
                                speed = 1
                        # If signal is not red, set speed to value indicated 
                        # by signal (if lower than present speed)
                        else:
                            speed = restrictedSpeed
            # Almost done.
            # Call change speed even if speed did not change, in order to inform
            # the Engineer that some change occurred
            self.changeSpeed(speed)
            # Start a separate thread to take care of block actions (if any)
            if self.block.action[self.direction].strip() != "":
                start_new_thread(self.doAction,
                                 (self.block.action[self.direction], ))
        # Release previously occupied sections (if empty)
        self.section.freeSectionIfTrainSafe()  
        
    def arrived(self):
        # Assume train is stopping
        self.lastMove = -1L
        if AutoDispatcher.runningTrains > 0:
            AutoDispatcher.runningTrains -= 1
        if self.section.isManual():
            self.section.setColor()

    def stop(self):
        # Train stops
        self.running = False
        self.enableSwing()

    def doAction(self, actionList):
        # Run in a separate thread to perform block actions
        textSpace  = actionList.replace(",", " ")
        # Break down input text into tokens
        splitted = textSpace.split()
        for sl in splitted:
            if sl.startswith("$") and len(sl) > 1:
                sl = sl[1:]
            action = ADschedule.ERROR
            s = sl.upper()
            # DCC function ON/OFF
            if s.startswith("ON:F"):
                action = ADschedule.SET_F_ON
                value = s[4:]
            elif s.startswith("OFF:F"):
                action = ADschedule.SET_F_OFF
                value = s[5:]
            if action != ADschedule.ERROR:
                # Retrieve ON OFF argument (function number)
                try:
                    value = int(value)
                    if value < 0 or value > 28:
                        continue
                except:
                    continue
                if action == ADschedule.SET_F_ON:
                    # Set decoder function ON
                    self.setFunction(value, True)
                else:
                    # Set decoder function OFF
                    self.setFunction(value, False)
            # Delay n seconds or m fastclock minutes
            elif s.startswith("D"):
                try:
                    s = s[1:]
                except:
                    continue
                if s.startswith("M"):
                    try:
                        s = s[1:]
                    except:
                        continue
                    useFastClock = True        
                else:
                    useFastClock = False        
                try:
                    value = float(s)
                except:
                    continue
                if useFastClock:
                    value = (value * 60.
                             / AutoDispatcher.fastBase.getRate())
                sleep(value)
            # Play AudioClip
            elif s.startswith("S:"):
                try:
                    value = ADsettings.soundDic.get(sl[2:], None)
                    if value != None:
                        value.play()
                except:
                    continue
            # Set turnout
            elif s.startswith("TC:") or s.startswith("TT:"):
                try:
                    value = sl[3:]
                    t = InstanceManager.turnoutManagerInstance().getTurnout(value)
                    if s.startswith("TC:"):
                        t.setState(Turnout.CLOSED)
                        AutoDispatcher.message("Closed turnout " + value)
                    else:
                        t.setState(Turnout.THROWN)
                        AutoDispatcher.message("Thrown turnout " + value)                   
                except:
                    continue

    def setSchedule(self, schedule):
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
        for section in ADsection.getList():
            if section.getAllocated() == self and not section.isOccupied():
                section.allocate(None, 0)
            else:
                section.setColor()
        if self.section != None:
            # Set destination signal to exit signal of current section
            self.destinationSignal = self.section.signal[self.direction]
            # Try to find current section in new schedule
            self.schedule.match(self.section, self.direction)
            self.queueCommands()
        # Force panel repainting
        AutoDispatcher.repaint = True

    def changeLocomotive(self, locoName):
        # Set/replace locomotive
        # If same locomotive as before ignore call
        if self.locoName == locoName:
            return
        self.locoName = locoName
        # Release previous locomotive
        locomotive = self.locomotive
        if locomotive != None:
            self.locomotive = None
            locomotive.usedBy = None
            locomotive.releaseThrottle()
        # If no new locomotive was selected, return
        if self.locoName.strip() == "":
            return
        # Retrieve locomotive instance
        self.locomotive = ADlocomotive.getByName(self.locoName)
        if self.locomotive == None:
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Locomotive " + locoName + " not found!")
        else:
            # Locomotive found
            self.locomotive.usedBy = self
            # Force assignment of engineer
            self.engineerAssigned = False
        self.status = ADtrain.IDLE
        
    def setReversed(self, reversed):
        # Set locomotive orientation
        self.reversed = reversed

    def clearBrakingHistory(self, locomotive):
        if locomotive == None:
            # No locomotive specified. Clear history of all locomotives
            self.brakingHistory = {}
        else:
            # Rebuild history dictionary skipping entries 
            # for specified locomotive
            keyStart = locomotive.getName() + "$"
            newHistory = {}
            for key in self.brakingHistory.keys():
                if not key.startswith(keyStart):
                    newHistory[key] = self.brakingHistory[key]
            self.brakingHistory = newHistory

    def setEngineer(self, engineerName):
        self.engineerName = engineerName
        self.engineerAssigned = False
        
    def assignEngineer(self):
        # If engineer already assigned, ignore call
        if self.engineerAssigned:
            return
        # Release previous Engineer, if any
        self.releaseEngineer()
        self.engineerAssigned = True
        # If user chose manual control, no other action is needed
        if self.engineerName == "Manual":
            return
        # Retrieve engineer class
        if not AutoDispatcher.engineers.has_key(self.engineerName):
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
        
    def getEngineerMethod (self, methodName):
        # Check if a method exists
        method = getattr(self.engineer, methodName, None)
        if not callable(method):
            return None
        return method            

    def setOrientation(self, forward):
        # Set locomotive orientation
        # Does the engineer provide the relevant method?
        if self.engineerSetOrientation == None:
            # No, if we have a locomotive pass the call directly 
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None:
                locomotive.setOrientation(forward)
        else:
            # Engineer provides the appropriate method, invoke it
            self.callEngineer(self.engineerSetOrientation, forward)
                    
    def setFunction(self, functionNumber, on):
        # Set/reset a locomotive function
        # Does the engineer provide the relevant method?
        if self.engineerSetFunction == None:
            # No, if we have a locomotive pass the call directly 
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None:
                locomotive.setFunction(functionNumber, on)
        else:
            # Engineer provides the appropriate method, invoke it
            self.callEngineer(self.engineerSetFunction, functionNumber, on)

    def changeSpeed(self, suggestedSpeed):
        # Change train speed
        # Make sure that speed does not exceed block's maximum speed
        if suggestedSpeed > self.maxSpeed  and self.maxSpeed != 0:
            suggestedSpeed = self.maxSpeed
        # If speed changed, update Trains window (if open)
        if self.speedLevel != suggestedSpeed:
            self.outputSpeed(suggestedSpeed)
            self.speedLevel = suggestedSpeed
        # Convert speed level, using train's correspendence table
        suggestedSpeed = self.convertSpeed(suggestedSpeed)
        # Does the engineer provide the relevant method?
        if self.engineerChangeSpeed == None:
            # No, if we have a locomotive pass the call directly
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None:
                locomotive.changeSpeed(suggestedSpeed)
        else:
            # Engineer provides the appropriate method, invoke it
            # Engineer is called even if speed value did not change, in order to
            # inform about other changes (e.g. section or block)
            # The Engineer class will decide if any action is required
            max = self.maxSpeed
            if max == 0:
                max = len(ADsettings.speedsList)
            max = self.convertSpeed(max)
            self.callEngineer(self.engineerChangeSpeed, self.section,
                              self.block, self.section.getSignal(self.direction),
                              suggestedSpeed, max)
        # If user chose to switch off lights when train stops, do it
        if self.speedLevel == 0 and ADsettings.lightMode == 1:
            self.setFunction(0, False)
            
    def outputSpeed(self, speed):
        # Output speed level to Trains window
        if speed == 0:        
            if self.departureTime > -1L or self.waitFor != None:
                return
            self.speedLevelSwing.text = "Stop"
        else:
            self.speedLevelSwing.text = ADsettings.speedsList[speed-1]

    def convertSpeed(self, speed):
        # Convert speed level, using train's correspondence table
        # (User can decide that this train should run at 10mph when 
        # the prescribed speed is 20mph)
        if speed > 0 and len(self.trainSpeed) >= speed:
            speed = self.trainSpeed[speed-1]
        if speed > len(ADsettings.speedsList):
            speed = len(ADsettings.speedsList)
        return speed

    def pause(self):
        # Script is pausing, train must be halted
        # Does the engineer provide the relevant method?
        if self.engineerPause == None:
            # No, if we have a locomotive pass the call directly
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None:
                locomotive.pause()
        else:
            # Engineer provides the appropriate method, invoke it
            self.callEngineer(self.engineerPause)
        # Take note that train stopped, to avoid "Stalled train" error
        self.lastMove = -1L

    def resume(self):
        # Script is resuming after a pause, train must be restarted
        # Set correct locomotive direction, in case it was manually changed
        # during the pause
        self.setOrientation(not self.reversed)
        # Does the engineer provide the relevant method?
        if self.engineerResume == None:
            # No, if we have a locomotive pass the call directly
            # to the locomotive
            locomotive = self.locomotive
            if locomotive != None:
                locomotive.resume()
        else:
            # Engineer provides the appropriate method, invoke it
            self.callEngineer(self.engineerResume)
        if self.running:
            self.lastMove = System.currentTimeMillis()

    def releaseEngineer(self):
        # Release the present engineer
        # Does the engineer provide a release method?
        if self.engineer != None and self.engineerRelease != None:
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

    def callEngineer(self, method, * args):
        # Invoke a method of the engineer class
        # Number of arguments may vary
        if method != None:
            start_new_thread(method, args)

    def startIfReady(self):
        # Start the train, if it can reach a section towards its destination
        # If train reached the end of the schedule or is in error simply exit
        # Avoid any action if train is being updated or started by
        # another thread
        if (self.updating or self.checking or self.status == ADtrain.ERROR or
            self.status == ADtrain.END_OF_SCHEDULE):
            return
        # If the train is nowhere, exit!
        if self.section == None:
            return
        loco = self.locomotive
        # Is train in a manually controlled section?
        if self.section.isManual():
            # Manual section, release throttle if train stopped
            if (not self.running and 
                self.locomotive != None and self.locomotive.throttle != None and
                self.locomotive.getThrottleSpeed() <= 0):
                self.locomotive.releaseThrottle()
        else:
            # Not manual section - Assign engineer, if not yet done
            if not self.engineerAssigned:
                self.assignEngineer()
                if loco != None:
                    if self.engineerSetLocomotive != None:
                        self.callEngineer(self.engineerSetLocomotive, loco)
                    else:
                        self.callEngineer(self.engineerSetLocoName, loco.getName())
            # Does train have a locomotive?
            if loco != None:
                # Assign throttle, if not yet done
                # (only if engineer implements setLocomotive method, otherwise
                # having a locomotive is useless)
                if (not loco.throttleAssigned and
                    self.engineerSetLocomotive != None):
                    loco.assignThrottle()
                    return
        # If train is paused, check if time has expired
        if self.departureTime > -1L:
            if self.fastClock:
                if self.departureTime > FastListener.fastTime:
                    return
            elif self.departureTime > System.currentTimeMillis():
                return
            self.departureTime = -1L
            self.outputSpeed(self.speedLevel)
        # If train is waiting for a section, see if the section is available
        # (or already occupied by the train)
        if self.waitFor != None:
            if (self.waitFor.isAvailable() or (self.waitFor == self.section and
                not self.section.isManual())):
                self.waitFor = None
                if self.speedLevelSwing.text.startswith("WAITING"):
                    self.outputSpeed(self.speedLevel)
            else:
                return
        # Process commands prefixed by $ that can be executed without
        # stopping the train
        # Such commands were transferred to "items" array before train departure
        if self.section in self.itemSections:
            self.itemSections.pop(0)
            self.processCommands(self.items.pop(0))
            return
        # If running, see if destination was reached
        if ((self.status == ADtrain.STARTED or self.status == ADtrain.IDLE) and
            self.section == self.destination):
            self.status = ADtrain.ARRIVED
        # Process commands prefixed by $ that can be executed only when
        # train is not running
        # (such commands are still contained in the schedule)
        scheduleItem = self.schedule.getFirstAlternative()
        if not self.running and self.status == ADtrain.ARRIVED:
            if scheduleItem.action != ADschedule.GOTO:
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
            self.destination.isManual()):
            return
        # If signal held, exit
        if self.destinationSignal.isHeld():
            if not self.running and self.destinationSwing.text != "Held":
                self.destinationSwing.text = "Held"
            return
        # Is train eligible for starting?
        if self.trainAllocation == 0:
            allocationAhead = ADsettings.allocationAhead
        else:
            allocationAhead = self.trainAllocation
        if (not self.allocationReady  or 
            self.allocatedSections >= allocationAhead):
            return
        # Limit number of running trains
        if (not self.running and (ADsettings.max_trains > 0 and
            AutoDispatcher.runningTrains >= ADsettings.max_trains)):
            if (not self.running and
                self.destinationSwing.text != "Waiting turn"):
                self.destinationSwing.text = "Waiting turn"
            return
        # Make sure anther thread is not starting this train or 
        # operating turnouts
        if ADtrain.turnoutsBusy or self.checking:
            return
        ADtrain.turnoutsBusy = self.checking = True
        # Span a separate thread in order to try and start the train
        # In this way commands for other trains can be processed while
        # starting this train (and operating turnouts)
        start_new_thread(self.checkAndStart, ())

    def checkAndStart(self):
        destinationText = ""
        # Compute number of sections ahead to be allocated
        if self.trainAllocation == 0:
            allocationAhead = ADsettings.allocationAhead
        else:
            allocationAhead = self.trainAllocation
        if (self.lastRouteSection != None and 
            self.destination != self.lastRouteSection):
            # Train running in burst mode - try reaching final section
            # of previous route
            route = ADautoRoute(self.destination, self.lastRouteSection,
                                self.finalDirection, self.switching)
            route.reduce(self, allocationAhead)
            foundLength = len(route.step)
            atLeastOne = True
        else:
            # Final section of previous schedule step reached
            # Find a possible route to one of alternate destinations included
            # in the new schedule step
            route = None
            atLeastOne = False
            foundLength = 0
            scheduleItem = self.schedule.getFirstAlternative()
            while scheduleItem.action == ADschedule.GOTO:
                if destinationText != "":
                    if not destinationText.startswith("["):
                        destinationText = "[" + destinationText
                    destinationText += " "
                destinationText += scheduleItem.value.getName()
                newRoute = ADautoRoute(self.destination, scheduleItem.value,
                                       self.finalDirection, self.switching)
                # Any route to this section found?
                if len(newRoute.step) > 0: 
                    # Yes
                    atLeastOne = True
                    # See if we can advance along it.
                    newRoute.reduce(self,
                                    allocationAhead - self.allocatedSections)
                    newLength = len(newRoute.step)
                    # Is this the longest reduced route?
                    if newLength > foundLength:
                        # Yes, take note
                        foundLength = newLength
                        route = newRoute
                scheduleItem = self.schedule.getNextAlternative()
            if scheduleItem.action == ADschedule.ERROR:
                # Wrong schedule
                if not self.running:
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                            "Error in schedule of train \"" + self.name + "\": "
                                            + scheduleItem.message)
                    self.status = ADtrain.ERROR
                    self.destinationSwing.text = "ERROR"
                self.checking = ADtrain.turnoutsBusy = False
                return
            if destinationText.startswith("["):
                destinationText += "]"
        # Check if any error was encounterd (i.e. transit-only destination)
        if route != None and route.error != None:
            # Error - Schedule contains transit-only destination
            if not self.running:
                # Output message only when train stops, otherwise we will
                #  flood user with messages
                AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                        "Schedule error: Train " + self.name + " headed to transit-only section "
                                        + route.error.getName())
                self.status = ADtrain.ERROR
                destinationText = "ERROR"
            foundLength = 0
            atLeastOne = True
        if foundLength == 0:
            # A reduced route was not found, check if at least one valid 
            # route was found
            if not atLeastOne:
                # No valid route was found, clearly an error
                # in schedule definition
                if not self.running:
                    # Output message only when train stops, otherwise we will
                    #  flood user with messages
                    AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                            "Train " + self.name + ": no valid route from section "
                                            + self.section.getName())
                    self.status = ADtrain.ERROR
                    destinationText = "ERROR"
            # Even if a valid route was found, next section is occupied or
            # allocated: exit
            if destinationText != "":
                self.destinationSwing.text = destinationText
            self.checking = ADtrain.turnoutsBusy = False
            return
        # Minimum route found, try allocating sections
        newAllocatedSections = route.allocate(self)
        if len(route.step) == 0:
            # First section along the route is occupied by another train or
            # new route is no longer valid: exit
            if destinationText != "":
                self.destinationSwing.text = destinationText
            self.checking = ADtrain.turnoutsBusy = False
            return
        # Successful allocation, train can start (or continue running)
        self.allocatedSections += newAllocatedSections
        self.destinationSwing.text = self.lastRouteSection.getName()
        # If present block has no maximum speed, use default speed
        startSpeed = self.maxSpeed
        if startSpeed == 0:
            startSpeed = len(ADsettings.speedsList)
        self.status = ADtrain.STARTING
        # Increase count of running trains
        if not self.running:
            self.running = True
            AutoDispatcher.runningTrains += 1
        # Disable input in Trains window
        self.enableSwing()
        # Make sure user is not updating train data
        # (Let's cope with Jython's lack of synchronization)
        while self.updating:
            AutoDispatcher.instance.waitMsec(100)
        if AutoDispatcher.simulation:
            self.entriesAhead.extend(route.step)
        # Create a list of sections that the train will use
        # (the list will be employed to track train movement)
        if not self.section in self.previousSections:
            self.previousSections.append(self.section)
        # Force panel re-drawing
        AutoDispatcher.repaint = True
        # Set turnouts
        route.setTurnouts()
        # Update list of blocks ahead
        for block in route.blocksList:
            if not block in self.blocksAhead:
                self.blocksAhead.append(block)
        # Train must reach next allocation point before being re-considered
        # for scheduling, unless allocated sections was lower than maximum
        self.allocationReady = self.allocatedSections < allocationAhead
        # Wait if user specified a delay before clearing signals
        if ADsettings.clearDelay > 0:
            AutoDispatcher.instance.waitMsec(ADsettings.clearDelay)
        # Set exit signals
        route.clearSignals(self)
        # Is our current destination the target of the present schedule item?
        scheduleItem = self.schedule.getFirstAlternative()
        while scheduleItem.action == ADschedule.GOTO:
            if self.destination == scheduleItem.value:
                # Yes - Take note of commands to be executed while running
                self.schedule.next()
                self.queueCommands()
                break
            scheduleItem = self.schedule.getNextAlternative()
        # Since train is (about) moving, user must be given the posibility
        # of saving its new position to disk before quitting the program.
        AutoDispatcher.setTrainsDirty()
        # Is train starting from still (or was it already running)?
        if self.speedLevel <= 0:
            # Starting from still. Set locomotive direction (could have changed)
            self.setOrientation(not self.reversed)
            # Delay departure, if user chose this option
            if (ADsettings.startDelayMin > 0 or
                ADsettings.startDelayMax > 0):
                delay = ADsettings.startDelayMin + int(
                                                       float(ADsettings.startDelayMax -
                                                       ADsettings.startDelayMin) * 
                                                       AutoDispatcher.random.nextDouble())
                AutoDispatcher.instance.waitMsec(delay)
            # Switch front light on, if user chose this option
            if ADsettings.lightMode != 0:
                self.setFunction(0, True)
            # Play start actions, if any
            if self.startAction != "":
                self.doAction(self.startAction)
            elif ADsettings.defaultStartAction != "":
                self.doAction(ADsettings.defaultStartAction)
        # Now start train!
        if (self.section.stopBlock[self.direction] == self.block or
            self.section.brakeBlock[self.direction] == self.block):
            restrictedSpeed = self.section.getSignal(self.direction).getSpeed()
            if restrictedSpeed <= self.maxSpeed or self.maxSpeed == 0:
                startSpeed = restrictedSpeed
        if self.locomotive != None:
            self.locomotive.brakeAdjusting = False
        self.changeSpeed(startSpeed)
        # Take note that train was started
        self.lastMove = System.currentTimeMillis()
        self.status = ADtrain.STARTED
        self.checking = ADtrain.turnoutsBusy = False
        return

    def queueCommands(self):
        # Transfer commands from schedule to pending comands queue (items)
        # Unless they were already transferrred (in the later case,
        # commands are discarded)
        toBeAdded = not self.destination in self.itemSections
        scheduleItem = self.schedule.getFirstAlternative()
        # Transfer only commands that can be executed while train is running
        while scheduleItem.action > ADschedule.GOTO:
            if toBeAdded:
                self.itemSections.append(self.destination)
                self.items.append(scheduleItem)
            self.schedule.next()
            scheduleItem = self.schedule.getFirstAlternative()
            
    def processCommands(self, scheduleItem):
        # Process schedule commands prefixed by $
        if scheduleItem.action == ADschedule.SWON:
            # Set train into "switching mode"
            self.switching = True
            AutoDispatcher.message("Train " + self.name +
                                   " entering switching mode")
            return
        if scheduleItem.action == ADschedule.SWOFF:
            # Clear "switching mode"
            self.switching = False
            AutoDispatcher.message("Train " + self.name +
                                   " exiting switching mode")
            return
        if scheduleItem.action == ADschedule.HELD:
            # Set signal to "held" state
            scheduleItem.value.setHeld(True)
            AutoDispatcher.message("Signal " + scheduleItem.value.getName()
                                   + " held")
            return
        if scheduleItem.action == ADschedule.RELEASE:
            # Clear "held" state of signal
            scheduleItem.value.setHeld(False)
            AutoDispatcher.message("Signal " + scheduleItem.value.getName()
                                   + " released")
            return
        if scheduleItem.action == ADschedule.SET_F_ON:
            # Set decoder function ON
            self.setFunction(scheduleItem.value, True)
            return
        if scheduleItem.action == ADschedule.SET_F_OFF:
            # Set decoder function OFF
            self.setFunction(scheduleItem.value, False)
            return
        if scheduleItem.action == ADschedule.WAIT_FOR:
            # Wait for section empty
            self.waitFor = scheduleItem.value
            AutoDispatcher.message("Train " + self.name
                                   + " waiting for section" + self.waitFor.getName())
            self.destinationSwing.setText("$WF:" + self.waitFor.getName())
            return
        if scheduleItem.action == ADschedule.DELAY:
            # Delay next commands
            delay = int(scheduleItem.value * 1000.)
            if delay > 0:
                self.fastClock = False
                self.departureTime = (System.currentTimeMillis() + delay)
            return
        if scheduleItem.action == ADschedule.MANUAL_PRESENT:
            self.switchToManual(self.section)
            return
        if scheduleItem.action == ADschedule.MANUAL_OTHER:
            self.switchToManual(scheduleItem.value)
            return
        if scheduleItem.action == ADschedule.ERROR:
            # Wrong schedule
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Error in schedule of train \"" + self.name + "\": "
                                    + scheduleItem.message)
            self.status = ADtrain.ERROR
            return
        if scheduleItem.action == ADschedule.STOP:
            # End of schedule reached
            self.status = ADtrain.END_OF_SCHEDULE
            if ADsettings.lightMode == 2:
                self.setFunction(0, False)
            AutoDispatcher.message("End of schedule for train "
                                   + self.name)
            self.destinationSwing.setText("End")
            if self.opTrain != None:
                try:
                    self.opTrain.move()
                finally:
                    i = 0 # Useless, just to complete the try construct
            return
        # Test for change of direction command
        newDirection = -1
        if scheduleItem.action == ADschedule.CCW:
            newDirection = ADsettings.ccw
        elif scheduleItem.action == ADschedule.CW:
            newDirection = 1-ADsettings.ccw
        if newDirection > -1:
            # Change of direction
            if newDirection != self.direction:
                # Allow enough time for simulation step to complete
                if AutoDispatcher.simulation:
                    AutoDispatcher.instance.waitMsec(1000)
                self.direction = self.finalDirection = newDirection
                # We are now facing the opposite signal
                self.destinationSignal = self.section.signal[self.direction]
                # Reverse locomotive direction
                self.reversed = not self.reversed
                # Change direction in section
                self.section.allocate(self, self.direction)
                # Change color of all sections occupied by the train
                if not self.section in self.previousSections:
                    self.section.setColor()
                for section in self.previousSections:
                    section.setColor()
                for section in self.sectionsAhead:
                    section.setColor()
                AutoDispatcher.repaint = True
            AutoDispatcher.message("Train " + self.name + " headed "
                                   + ADsettings.directionNames[1-newDirection])
            self.updateSwing()
            return
        if scheduleItem.action == ADschedule.PAUSE:
            AutoDispatcher.message("Train " + self.name + " pausing for "
                                   + str(scheduleItem.value) + " sec.")
            delay = int(scheduleItem.value * 1000.)
            if delay > 0:
                # Pause - Compute departure time
                self.fastClock = False
                self.departureTime = (System.currentTimeMillis() + delay)
                self.destinationSwing.setText("$P" + str(scheduleItem.value))
                self.updateSwing()
            return
        if scheduleItem.action == ADschedule.START_AT:
            if scheduleItem.value > FastListener.fastTime:
                self.fastClock = True
                self.departureTime = scheduleItem.value
                hours = int(scheduleItem.value / 60)
                minutes = scheduleItem.value - hours * 60
                if minutes > 9:
                    hours = str(hours) + ":" + str(minutes)
                else:
                    hours = str(hours) + ":0" + str(minutes)
                AutoDispatcher.message("Train " + self.name + " waiting until " + hours)
                self.destinationSwing.setText("$ST " + hours)
                self.updateSwing()
            return
        if scheduleItem.action == ADschedule.SOUND:
            # Play sound
            scheduleItem.value.play()
            return
        if (scheduleItem.action == ADschedule.TC or
            scheduleItem.action == ADschedule.TT):
            # Set turnout (or other accessory)
            try:
                t = InstanceManager.turnoutManagerInstance().getTurnout(scheduleItem.value)
            except:
                t = None
            if t == None:
                AutoDispatcher.log("Error in schedule of train \"" + self.name + "\":")
                AutoDispatcher.chimeLog("  Unknown turnout \"" + scheduleItem.value + "\"")
                self.status = ADtrain.ERROR
                return
            if scheduleItem.action == ADschedule.TC:
                t.setState(Turnout.CLOSED)
                AutoDispatcher.message("Train " + self.name + " : closed turnout "
                                       + scheduleItem.value)
            else:
                t.setState(Turnout.THROWN)
                AutoDispatcher.message("Train " + self.name + " : thrown turnout "
                                       + scheduleItem.value)
            return            
             
    def switchToManual(self, section):
        # Try switching the section to Manual control
        section.setManual(True)
        # Check result
        if section.isManual():
            # Fine
            AutoDispatcher.message("Section \"" + section.getName()
                                   + "\" switched to manual control")
        else:
            # Failed. Section probably does not have a "Manual control" sensor
            AutoDispatcher.chimeLog(ADsettings.ATTENTION_SOUND,
                                    "Cannot switch section \"" +
                                    section.getName() + "\" to manual control")

    def updateSwing(self):
        # Update SWING I/O fields for train
        self.directionSwing.removeAllItems()
        self.directionSwing.addItem(ADsettings.directionNames[0])
        self.directionSwing.addItem(ADsettings.directionNames[1])
        if ADsettings.ccw == 0:
            self.directionSwing.setSelectedIndex(self.direction)
        else:
            self.directionSwing.setSelectedIndex(1- self.direction)
        if self.section != None:
            self.sectionSwing.setSelectedItem(self.section.getName())
        else:
            self.sectionSwing.setSelectedItem("")
        self.resistiveSwing.setSelected(self.resistiveWheels)
        self.canStopAtBeginningSwing.setSelected(self.canStopAtBeginning)
        self.locoRoster.setSelectedItem(self.locoName)
        self.reversedSwing.setSelected(self.reversed)
        # Display schedule
        self.scheduleSwing.setText(self.schedule.text)

    def enableSwing(self):
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
        if self.engineerName == "Manual":
            self.locoRoster.setEnabled(False)
            self.reversedSwing.setEnabled(False)
        else:
            self.locoRoster.setEnabled(stopped)
            self.reversedSwing.setEnabled(stopped)
        self.scheduleSwing.setEnabled(stopped)
        self.deleteButton.setEnabled(stopped)
        self.changeButton.setEnabled(stopped)
        self.setButton.setEnabled(stopped)
        self.startActionSwing.setEnabled(stopped)

    def getCurrentBlock(self):
        return self.block
        
    def getPreviousBlock(self):
        return self.previousBlock
        
    def getNextBlock(self):
        if len(self.blocksAhead) > 0:
            return self.blocksAhead[0]
        return None
        
