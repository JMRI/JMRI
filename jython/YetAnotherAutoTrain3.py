# YetAnotherAutoTrain.py -- Data driven automatic train
# Use a list of actions to automatically run a train.
# v1.3 -- Add line numbers to the compiler error messages.
# v1.4 -- Add a master controller that can be used to terminate all of the threads.
# v1.5 -- Add signal mast and signal head options.
# v1.6 -- Add "Loop" to separate one time startup actions from the main loop.
#         Add "Repeat" to separate one time finish/clieanup actions from the main loop.
#         Add "Set route" to invoke a JMRI Route.
#         Add "Print" to send text to the Script output or system console.
# v2.0 -- Add nested If/Else/Endif support.
#         Add GoSub, Sub and EndSub to support sub routines.
#         Convert execution flow from multiple "execute" and "skip" modes to treating the token list as program addresses.
# v2.1 -- Add support to trigger Dispatcher automatic trains.
#         Add an optional source for the trainList.  The train list statements can be in the TrainList.txt file located in
#           the yaat directory in the user files location:  preference:yaat/TrainList.txt
# v2.2 -- Add the ability to use "compiled" trains.
#         Add the ability to create custom extensions.
# v2.3 -- Add Set turntable position command.
# v3.0 -- Eliminate the need to modify the script.
#            Options are at preference:/yaat/config.txt
#            The preference:/yaat/TrainList.txt file contains the train file names to be loaded during script startup.
#            Support for inline train definitions has been removed.
#            Custom extension support has been removed.
#         Trains can be loaded and started by setting a memory variable to a train file name.
# v3.1 -- The wait for seconds command has support for random wait times:  Wait for <n> [to <n>] seconds.
#         The function key limit has been changed to 68.
# v3.2 -- Add hold and release signal heads and masts, misc bug fixes, code improvements.
# v3.3 -- Add 'If value for memory <memory name> is <eq | ne | lt | gt | le | ge> <value>'
#         Add 'Wait for memory <memory name> value to change'
# v3.4 -- Add the ability to include text content using '@include <filename>'.
#
# Author:  Dave Sand copyright (c) 2018 - 2025

# The help information is available at https://jmri.org/help/en/html/scripthelp/yaat/YAAT.shtml

import io
import os
from time import time
import pickle

import java
import jmri
import re
from javax.swing import JOptionPane

# Location for the yaat config, the train list and the train defintion files.
yaatPath = jmri.util.FileUtil.getUserFilesPath() + 'yaat'
jmri.util.FileUtil.createDirectory(yaatPath)
yaatLocation = yaatPath + jmri.util.FileUtil.SEPARATOR
yaatIncludes = None

# Load YAAT configuration options.  If not found, create a file with default values.
try:
    with open(yaatLocation + 'config.txt') as file:
        exec(file.read())
except IOError:
    logLevel = 0
    saveYAATcompiles = False
    enableIncludes = False
    masterSensor = ''
    statusSensor = ''
    yaatMemory = ''

    # Create the config.txt file
    with open(yaatLocation + 'config.txt', 'w') as file:
        file.write("logLevel = 0                # 0 for no output, 4 for the most detail\n")
        file.write("saveYAATcompiles = False    # Load/Save compiled trains\n")
        file.write("enableIncludes = False      # Enable @include support\n")
        file.write("masterSensor = ''           # Optional sensor to stop all threads\n")
        file.write("statusSensor = ''           # Optional sensor to notify JMRI if any threads are active\n")
        file.write("yaatMemory = ''             # Optional memory variable that contains a filename for starting a train\n")

if logLevel > 2: print 'yaatLocation: {}'.format(yaatLocation)
try:
    if enableIncludes:
        jmri.util.FileUtil.createDirectory(yaatPath + jmri.util.FileUtil.SEPARATOR + 'includes')
        yaatIncludes = yaatLocation + 'includes' + jmri.util.FileUtil.SEPARATOR
        if logLevel > 2: print 'yaatIncludes location: {}'.format(yaatIncludes)
except NameError:
    # enableIncludes was added at 3.4 and might not be in the existing xconfig file.
    if logLevel > 3: print 'Handle enableIncludes not found'
    enableIncludes = False

# Set optional objects, will be None if not found
yaatMaster = sensors.getSensor(masterSensor)
yaatRunning = sensors.getSensor(statusSensor)
trainMemory = memories.getMemory(yaatMemory)

def setYaatMaster(state):
    if yaatMaster is not None:
        yaatMaster.setKnownState(state)

def setYaatRunnning(state):
    if yaatRunning is not None:
        yaatRunning.setKnownState(state)

class YetAnotherAutoTrain(jmri.jmrit.automat.AbstractAutomaton):
    threadCount = 0

    def init(self):
        self.throttle = None
        YetAnotherAutoTrain.threadCount += 1

    def setup(self, actionList, compileNeeded, fileName):
        self.actionTokens = []
        self.compileMessages = []
        self.lineNumber = 0
        self.threadName = self.getName()

        # Note:  The Loop, CallSub, Sub, If, Else and Endif addresses are actually the statement after the key word.

        self.progAddr = 0
        self.loopAddr = -1

        self.ifList = {}            # The list of if statements.  The key is the "If" program address.
                                    #    The value is a tuple with program addresses for Else, and Endif.
                                    #    The Else address can be zero.
        self.ifStack = []           # The active "If" program addresses.

        self.subList = {}           # The program address for each sub, keyed using the sub name
        self.subStack = []          # The active return program addresses

        if compileNeeded:
            if logLevel > 1: print 'Compile train {}'.format(self.threadName)

            self.compile(actionList)

            if saveYAATcompiles and fileName != '':
                pickleList = []
                pickleList.append(self.actionTokens)
                pickleList.append(self.ifList)
                pickleList.append(self.subList)
                pickleList.append(self.loopAddr)
                file = open(fileName, 'wb')
                pickle.dump(pickleList, file)
                file.close()

        else:
            if logLevel > 1: print 'Use the pickle file for train {}'.format(self.threadName)

            file = open(fileName, 'rb')
            pickleList = pickle.load(file)
            file.close()
            self.actionTokens = pickleList[0]
            self.ifList = pickleList[1]
            self.subList = pickleList[2]
            self.loopAddr = pickleList[3]

        if logLevel > 2:
            for ifKey in self.ifList.keys():
                elseAddr, endAddr = self.ifList[ifKey]
                print "if = {}, else = {}, endif = {}".format(ifKey, elseAddr, endAddr)

        if len(self.ifStack) > 0:
            self.compileMessages.append('{} - Missing Endif(s)'.format(self.threadName))

        if len(self.subStack) > 0:
            self.compileMessages.append('{} - Missing EndSub(s)'.format(self.threadName))

        if len(self.compileMessages) > 1:
            self.displayMessage("\n".join(self.compileMessages))
            YetAnotherAutoTrain.threadCount -= 1
            if YetAnotherAutoTrain.threadCount < 1:
                setYaatRunnning(INACTIVE)
            return False
        if len(self.actionTokens) == 0:
            self.displayMessage('{} - The action list is empty, terminating'.format(self.threadName))
            return False

        setYaatRunnning(ACTIVE)

        return True

    def handle(self):
        if logLevel > 0: print '{} - Start YAAT Program'.format(self.threadName)
        while True:
            if self.progAddr >= len(self.actionTokens):
                self.progAddr = 0
                continue
            action = self.actionTokens[self.progAddr]
            self.progAddr += 1

            if len(action) == 0:
                self.displayMessage('Empty Action row')
                continue

            if logLevel > 2: print '{:3d} :: {} - Action: {}'.format(self.progAddr, self.threadName, action)
            actionKey = action[0]

            if actionKey == 'Assign':
                self.doAssign(action)
            elif actionKey == 'CallSub':
                self.doCallSub(action)
            elif actionKey == 'Dispatch':
                self.doDispatch(action)
            elif actionKey == 'Else':
                ifKey = self.ifStack.pop()
                elseAddr, endIfAddr = self.ifList[ifKey]
                self.progAddr = endIfAddr
                continue        # End True block
            elif actionKey == 'Endif':
                ifKey = self.ifStack.pop()
                continue       # End Else block
            elif actionKey == 'EndSub':
                self.doEndSub(action)
            elif actionKey == 'Halt':
                break    # Direct execution
            elif actionKey == 'HoldHead':
                self.doHoldSignalHead(action)
            elif actionKey == 'HoldMast':
                self.doHoldSignalMast(action)
            elif actionKey == 'IfBlock':
                self.doIfBlock(action)
            elif actionKey == 'IfMemoryValue':
                self.doIfMemoryValue(action)
            elif actionKey == 'IfSensor':
                self.doIfSensor(action)
            elif actionKey == 'IfHead':
                self.doIfHead(action)
            elif actionKey == 'IfMast':
                self.doIfMast(action)
            elif actionKey == 'IfSpeed':
                self.doIfSpeed(action)
            elif actionKey == 'Loop':
                continue
            elif actionKey == 'Print':
                self.doPrint(action)
            elif actionKey == 'ReleaseHead':
                self.doReleaseSignalHead(action)
            elif actionKey == 'ReleaseMast':
                self.doReleaseSignalMast(action)
            elif actionKey == 'ReleaseThrottle':
                self.doReleaseThrottle(action)
            elif actionKey == 'Repeat':
                if self.doRepeat(action):
                    self.progAddr = self.loopAddr
                    continue
            elif actionKey == 'SetBlock':
                self.doSetBlock(action)
            elif actionKey == 'SetDirection':
                self.doSetDirection(action)
            elif actionKey == 'SetFKey':
                self.doSetFKey(action)
            elif actionKey == 'SetMemory':
                self.doSetMemory(action)
            elif actionKey == 'SetRoute':
                self.doSetRoute(action)
            elif actionKey == 'SetSensor':
                self.doSetSensor(action)
            elif actionKey == 'SetSpeed':
                self.doSetSpeed(action)
            elif actionKey == 'SetTurnout':
                self.doSetTurnout(action)
            elif actionKey == 'SetTurntable':
                self.doSetTurntable(action)
            elif actionKey == 'Start':
                self.doStart(action)
            elif actionKey == 'Stop':
                if not self.doStop(action):
                    if logLevel > 0: print '>> Stop YAAT for {} <<'.format(self.threadName)
                    YetAnotherAutoTrain.threadCount -= 1
                    if YetAnotherAutoTrain.threadCount == 0:
                        setYaatRunnning(INACTIVE)
                    break;
            elif actionKey == 'Sub':
                self.progAddr = 0
                continue
            elif actionKey == 'WaitBlock':
                self.doWaitBlock(action)
            elif actionKey == 'WaitMemory':
                self.doWaitMemory(action)
            elif actionKey == 'WaitSensor':
                self.doWaitSensor(action)
            elif actionKey == 'WaitHead':
                self.doWaitHead(action)
            elif actionKey == 'WaitMast':
                self.doWaitMast(action)
            elif actionKey == 'WaitSpeed':
                self.doWaitSpeed(action)
            elif actionKey == 'WaitTime':
                self.doWaitTime(action)
            else:
                self.displayMessage('Action, {}, is not valid'.format(actionKey))
        if logLevel > 0: print '{} -  End YAAT Program'.format(self.threadName)
        return False

    # ------ Perform token commands ------
    def doAssign(self, action):
        if self.throttle != None:
            return  # Throttle already assigned.  Normal for subsequent loops
        act, dccAddress, addrType, trainName, startBlock = action
        if addrType == 'long':
            dccLong = True
        elif addrType == 'short':
            dccLong = False
        else:
            self.displayMessage('{} - DCC address length, {}, is not valid'.format(self.threadName, addrType))
            return
        self.throttle = self.getThrottle(dccAddress, dccLong)
        if self.throttle == None:
            self.displayMessage('{} - Unable to assign a throttle.\nCheck the system log for errors.\nScript stopping.'.format(self.threadName))
            self.stop()
        if trainName != '' and startBlock != '':
            layoutBlock = layoutblocks.getLayoutBlock(startBlock)
            if layoutBlock is not None:
                layoutBlock.getBlock().setValue(trainName)

    def doCallSub(self, action):
        act, subName = action
        if not subName in self.subList:
            self.displayMessage('{} - Sub routine {} not found'.format(self.threadName, subName))
            return
        subAddress = self.subList[subName]
        returnAddress = self.progAddr
        self.subStack.append(returnAddress)
        self.progAddr = subAddress

    def doDispatch(self, action):
        act, dispFile, dispType, dispValue = action
        dispFrame = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        x = dispFrame.loadTrainFromTrainInfo(dispFile, dispType, dispValue)
        if x != 0:
            self.displayMessage('{} - Dispatcher failed to start, reason code = {}'.format(self.threadName, x))

    def doEndSub(self, action):
        returnAddress = self.subStack.pop()
        self.progAddr = returnAddress

    def doHoldSignalHead(self, action):
        act, headName = action
        head = signals.getSignalHead(headName)
        if head is None:
            self.displayMessage('{} - Signal head {} not found'.format(self.threadName, headName))
            return
        head.setHeld(True)

    def doHoldSignalMast(self, action):
        act, mastName = action
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.displayMessage('{} - Signal mast {} not found'.format(self.threadName, mastName))
            return
        mast.setHeld(True)

    def doIfBlock(self, action):
        act, blockName, blockState = action
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.displayMessage('{} - Layout block {} not found'.format(self.threadName, blockName))
            return
        sensor = layoutBlock.getOccupancySensor()
        if sensor is None:
            self.displayMessage('{} - Sensor for layout block {} not found'.format(self.threadName, blockName))
            return
        if blockState == 'occupied':
            currentState = True if sensor.getKnownState() == ACTIVE else False
        elif blockState == 'unoccupied':
            currentState = True if sensor.getKnownState() == INACTIVE else False
        elif blockState == 'reserved':
            currentState = True if layoutBlock.getUseExtraColor() else False
        elif blockState == 'free':
            currentState = True if not layoutBlock.getUseExtraColor() else False
        else:
            self.displayMessage('{} - block state, {}, is not valid'.format(self.threadName, blockState))
            return
        self.pushIfState(currentState)

    def doIfMemoryValue(self, action):
        act, memoryName, operator, value = action
        memory = memories.getMemory(memoryName)
        if memory is None:
            self.displayMessage('{} - Memory {} not found'.format(self.threadName, memoryName))
            return
        checkState = False
        currentValue = str(memory.getValue())
        if currentValue:
            if operator == 'eq':
                if currentValue == value: checkState = True
            elif operator == 'ne':
                if currentValue != value: checkState = True
            elif operator == 'gt':
                if currentValue > value: checkState = True
            elif operator == 'lt':
                if currentValue < value: checkState = True
            elif operator == 'ge':
                if currentValue >= value: checkState = True
            elif operator == 'le':
                if currentValue <= value: checkState = True
            else:
                self.displayMessage('{} - Invalid operator: {}'.format(self.threadName, operator))
                return
        self.pushIfState(checkState)

    def doIfSensor(self, action):
        act, sensorName, sensorState = action
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('{} - Sensor {} not found'.format(self.threadName, sensorName))
            return
        if sensorState == 'active':
            currentState = True if sensor.getKnownState() == ACTIVE else False
        elif sensorState == 'inactive':
            currentState = True if sensor.getKnownState() == INACTIVE else False
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))
            return
        self.pushIfState(currentState)

    def doIfHead(self, action):
        act, headName, stateList, notOption = action
        head = signals.getSignalHead(headName)
        if head is None:
            self.displayMessage('{} - Signal head {} not found'.format(self.threadName, headName))
            return
        currentAppearance = head.getAppearance()
        checkState = False
        if notOption:
            if currentAppearance not in stateList:
                checkState = True
        else:
            if currentAppearance in stateList:
                checkState = True
        self.pushIfState(checkState)

    def doIfMast(self, action):
        act, mastName, aspectList, notOption = action
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.displayMessage('{} - Signal mast {} not found'.format(self.threadName, mastName))
            return
        currentAspect = mast.getAspect()
        checkState = False
        if notOption:
            if currentAspect not in aspectList:
                checkState = True
        else:
            if currentAspect in aspectList:
                checkState = True
        self.pushIfState(checkState)

    def doIfSpeed(self, action):
        act, mastName, operator, speedName = action
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.displayMessage('{} - Signal mast {} not found'.format(self.threadName, mastName))
            return
        checkState = False
        signalSystem = mast.getSignalSystem()
        speedMap = jmri.InstanceManager.getDefault(jmri.implementation.SignalSpeedMap)
        checkSpeed = speedMap.getSpeed(speedName)
        aspectName = mast.getAspect()
        if aspectName is not None:
            aspectSpeedName = speedMap.getAspectSpeed(aspectName, signalSystem)
            currentSpeed = speedMap.getSpeed(aspectSpeedName)
            if operator == 'eq':
                if currentSpeed == checkSpeed: checkState = True
            elif operator == 'ne':
                if currentSpeed != checkSpeed: checkState = True
            elif operator == 'gt':
                if currentSpeed > checkSpeed: checkState = True
            elif operator == 'lt':
                if currentSpeed < checkSpeed: checkState = True
            elif operator == 'ge':
                if currentSpeed >= checkSpeed: checkState = True
            elif operator == 'le':
                if currentSpeed <= checkSpeed: checkState = True
            else:
                self.displayMessage('{} - Invalid operator: {}'.format(self.threadName, operator))
                return
        else:
            self.displayMessage('{} - Aspect for signal mast {} not found'.format(self.threadName, mastName))
            return
        self.pushIfState(checkState)

    def doPrint(self, action):
        act, printText = action
        print '{} - {}'.format(self.threadName, printText)

    def doReleaseSignalHead(self, action):
        act, headName = action
        head = signals.getSignalHead(headName)
        if head is None:
            self.displayMessage('{} - Signal head {} not found'.format(self.threadName, headName))
            return
        head.setHeld(False)

    def doReleaseSignalMast(self, action):
        act, mastName = action
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.displayMessage('{} - Signal mast {} not found'.format(self.threadName, mastName))
            return
        mast.setHeld(False)

    def doReleaseThrottle(self, action):
        if self.throttle is not None:
            self.throttle.release(None)
            self.throttle = None

    def doRepeat(self, action):
        act, repeatName, repeatState = action
        sensor = sensors.getSensor(repeatName)
        if repeatState == 'active':
            chkState = ACTIVE
        elif repeatState == 'inactive':
            chkState = INACTIVE
        else:
            self.displayMessage('{} - Repeat sensor state, {}, is not valid'.format(self.threadName, repeatState))
            return False
        return sensor.getKnownState() == chkState

    def doSetBlock(self, action):
        act, blockName, blockState = action
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.displayMessage('{} - Layout block {} not found'.format(self.threadName, blockName))
            return
        if blockState in ['occupied', 'unoccupied']:
            sensor = layoutBlock.getOccupancySensor()
            if sensor is None:
                self.displayMessage('{} - Sensor for layout block {} not found'.format(self.threadName, blockName))
                return
            if blockState == 'occupied':
                layoutBlock.getOccupancySensor().setKnownState(ACTIVE)
            else:
                layoutBlock.getOccupancySensor().setKnownState(INACTIVE)
            return
        useExtra = False    # Default to free
        if blockState == 'reserved':
            useExtra = True
        layoutBlock.setUseExtraColor(useExtra)

    def doSetDirection(self, action):
        if self.throttle == None:
            self.displayMessage('{} - Cannot set a direction until a throttle has been assigned'.format(self.threadName))
            return
        act, direction = action
        if direction == 'forward':
            dirForward = True
        elif direction == 'reverse':
            dirForward = False
        else:
            self.displayMessage('{} - Direction value, {}, is not valid'.format(self.threadName, direction))
            return
        self.throttle.setIsForward(dirForward)

    def doSetFKey(self, action):
        if self.throttle == None:
            self.displayMessage('{} - Cannot set a function key until a throttle has been assigned'.format(self.threadName))
            return
        act, keyNum, keyState, keyDuration = action
        if keyState == 'on':
            keyOn = True
            keyOff = False
        elif keyState == 'off':
            keyOn = False
            keyOff = True
        else:
            self.displayMessage('{} - Key state value, {}, is not valid'.format(self.threadName, keyState))
            return
        if keyDuration == 0:
            self.setKey(keyNum, keyOn)
        else:
            self.setKey(keyNum, keyOn)
            self.waitMsec(keyDuration)
            self.setKey(keyNum, keyOff)

    def doSetMemory(self, action):
        act, memoryName, memoryValue = action
        memory = memories.getMemory(memoryName)
        if memory is None:
            self.displayMessage('{} - Memory {} not found'.format(self.threadName, memoryName))
            return
        memory.setValue(memoryValue)

    def doSetRoute(self, action):
        act, routeName = action
        route = routes.getRoute(routeName)
        if route is None:
            self.displayMessage('{} - Route {} not found'.format(self.threadName, routeName))
            return
        route.activateRoute()
        route.setRoute()
        self.waitMsec(100)

    def doSetSensor(self, action):
        act, sensorName, sensorState = action
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('{} - Sensor {} not found'.format(self.threadName, sensorName))
            return
        if sensorState == 'active':
            newState = ACTIVE
        elif sensorState == 'inactive':
            newState = INACTIVE
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))
            return
        sensor.setKnownState(newState)

    def doSetSpeed(self, action):
        if self.throttle == None:
            self.displayMessage('{} - Cannot set the speed until a throttle has been assigned'.format(self.threadName))
            return
        act, newSpeed = action
        self.throttle.setSpeedSetting(newSpeed)

    def doSetTurnout(self, action):
        act, turnoutName, turnoutState, turnoutDelay = action
        turnout = turnouts.getTurnout(turnoutName)
        if turnout is None:
            self.displayMessage('{} - Turnout {} not found'.format(self.threadName, turnoutName))
            return
        if turnoutState == 'closed':
            newState = CLOSED
        elif turnoutState == 'thrown':
            newState = THROWN
        else:
            self.displayMessage('{} - Turnout state, {}, is not valid'.format(self.threadName, turnoutState))
            return
        turnout.setCommandedState(newState)
        # Wait up to 5 seconds for feedback
        for i in range(0, 20):
            if turnout.getKnownState() == newState:
                break;
            if logLevel > 2: print 'Turnout feedback loop: {}'.format(i)
            self.waitMsec(250)
        self.waitMsec(turnoutDelay)

    def doSetTurntable(self, action):
        act, turntableName, panelName, rayIndex = action
        editorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
        for layout in editorManager.getAll(jmri.jmrit.display.layoutEditor.LayoutEditor):
            if layout.getTitle() == panelName:
                for turntable in layout.getLayoutTurntableViews():
                    if turntable.getId() == turntableName:
                        for raytrack in turntable.getRayTrackList():
                            if raytrack.getConnectionIndex() == rayIndex:
                                turntable.setPosition(rayIndex)
                                return
                        self.displayMessage.append('{} - Turntable error: the ray index, {}, is not found'.format(self.threadName, rayIndex))
                        return
                self.displayMessage.append('{} - Turntable error : the turntable name, {}, is not found'.format(self.threadName, turntableName))
                return
        self.displayMessage.append('{} - Turntable error: the layout name, {}, is not found'.format(self.threadName, panelName))

    def doStart(self, action):
        act, startName, startState = action
        sensor = sensors.getSensor(startName)
        if startState == 'active':
            self.waitSensorActive(sensor)
        elif startState == 'inactive':
            self.waitSensorInactive(sensor)
        else:
            self.displayMessage('{} - Start sensor state, {}, is not valid'.format(self.threadName, startState))

    def doStop(self, action):
        act, stopName, stopState = action
        sensor = sensors.getSensor(stopName)
        if stopState == 'active':
            chkState = ACTIVE
        elif stopState == 'inactive':
            chkState = INACTIVE
        else:
            self.displayMessage('{} - Stop sensor state, {}, is not valid'.format(self.threadName, stopState))
            return
        if sensor.getKnownState() == chkState:
            # Release throttle
            if self.throttle is not None:
                self.throttle.release(None)
            return False
        return True

    def doWaitBlock(self, action):
        act, blockName, blockState = action
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.displayMessage('{} - Layout block {} not found'.format(self.threadName, blockName))
            return
        if blockState in ['occupied', 'unoccupied']:
            # Block sensor changes limited to simulation mode
            sensor = layoutBlock.getOccupancySensor()
            if sensor is None:
                self.displayMessage('{} - Sensor for layout block {} not found'.format(self.threadName, blockName))
                return
            if blockState == 'occupied':
                self.waitSensorActive(sensor)
            else:
                self.waitSensorInactive(sensor)
            return
        # wait for free - no sensor available so do it the hard way
        while layoutBlock.getUseExtraColor():
            self.waitMsec(1000)
        return

    def doWaitMemory(self, action):
        act, memoryName = action
        memory = memories.getMemory(memoryName)
        if memory is None:
            self.displayMessage('{} - Memory {} not found'.format(self.threadName, memoryName))
            return
        currValue = str(memory.getValue())
        newValue = currValue
        while newValue == currValue:
            self.waitChange([memory])
            newValue = str(memory.getValue())

    def doWaitSensor(self, action):
        act, sensorName, sensorState = action
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('{} - Sensor {} not found'.format(self.threadName, sensorName))
            return
        if sensorState == 'active':
            self.waitSensorActive(sensor)
        elif sensorState == 'inactive':
            self.waitSensorInactive(sensor)
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))

    def doWaitHead(self, action):
        act, headName, stateList, notOption = action
        head = signals.getSignalHead(headName)
        if head is None:
            self.displayMessage('{} - Signal head {} not found'.format(self.threadName, headName))
            return
        while True:
            currentAppearance = head.getAppearance()
            if notOption:
                if currentAppearance not in stateList:
                    return
            else:
                if currentAppearance in stateList:
                    return
            self.waitChange([head])

    def doWaitMast(self, action):
        act, mastName, aspectList, notOption = action
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.displayMessage('{} - Signal mast {} not found'.format(self.threadName, mastName))
            return
        while True:
            currentAspect = mast.getAspect()
            if notOption:
                if currentAspect not in aspectList:
                    return
            else:
                if currentAspect in aspectList:
                    return
            self.waitChange([mast])

    def doWaitSpeed(self, action):
        act, mastName, aspectSpeed = action
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.displayMessage('{} - Signal mast {} not found'.format(self.threadName, mastName))
            return
        signalSystem = mast.getSignalSystem()
        speedMap = jmri.InstanceManager.getDefault(jmri.implementation.SignalSpeedMap)
        while True:
            aspectName = mast.getAspect()
            if aspectName is not None:
                speedName = speedMap.getAspectSpeed(aspectName, signalSystem)
                currentSpeed = speedMap.getSpeed(speedName)
                print aspectName, speedName, currentSpeed
                if currentSpeed >= aspectSpeed:
                    return
            self.waitChange([mast])

    def doWaitTime(self, action):
        act, time1, time2 = action

        delay = time1
        if time1 != time2:
            delay = java.util.Random().nextInt(time2 - time1) + time1
            if logLevel > 1: print 'Randon time delay = {} :: {} :: {}'.format(delay, time1, time2)

        self.waitMsec(delay)

    def setKey(self, keyNum, keyOn):
        if logLevel > 2: print "{} - Function key = {}, On = {}".format(self.threadName, keyNum, keyOn)
        command = 'self.throttle.setF' + str(keyNum)
        if keyOn:
            command += '(True)'
        else:
            command += '(False)'
        exec(command)

    # ------ General Functions ------
    def displayMessage(self, msg):
        JOptionPane.showMessageDialog(None, msg, 'YAAT Error', JOptionPane.WARNING_MESSAGE)

    def createIf(self):
        key = len(self.actionTokens)
        self.ifList[key] = (0, 0)
        self.ifStack.append(key)

    def pushIfState(self, state):
        key = self.progAddr
        if not key in self.ifList:
            self.displayMessage('{} - ifList entry not found for key {}'.format(self.threadName, key))
            return
        elseAddr, endIfAddr = self.ifList[key]
        if state:                           # Condition is true, execute the first or only block
            self.ifStack.append(key)
        else:
            if elseAddr != 0:               # Condition is false, jump to the Else address and execute the Else block
                self.progAddr = elseAddr
                self.ifStack.append(key)
            else:
                self.progAddr = endIfAddr   # Condition is false with no Else, jump to the Endif address

    # ------ Convert the text phrases to tokens ------
    def compile(self, actionList):
        self.compileMessages.append('---- {} Compiler Errors ----'.format(self.threadName))
        for line in actionList:
            self.lineNumber += 1
            words = line.split()
            if len(words) == 0:
                continue
            if words[0][:1] == '#':
                continue
            if words[0] == 'Assign':
                self.compileAssign(line)
            elif words[0] == 'CallSub':
                self.compileCallSub(line)
            elif words[0] == 'Dispatch':
                self.compileDispatch(line)
            elif words[0] == 'Else':
                self.compileElse(line)
            elif words[0] == 'Endif':
                self.compileEndIf(line)
            elif words[0] == 'EndSub':
                self.compileEndSub(line)
            elif words[0] == 'Halt':
                if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
                self.actionTokens.append(['Halt'])
            elif words[0] == 'Hold' and words[1] == 'signal' and words[2] == 'head':
                self.compileHoldSignalHead(line)
            elif words[0] == 'Hold' and words[1] == 'signal' and words[2] == 'mast':
                self.compileHoldSignalMast(line)
            elif words[0] == 'If' and words[1] == 'block':
                self.compileIfBlock(line)
            elif words[0] == 'If' and words[1] == 'value' and words[3] == 'memory':
                self.compileIfMemoryValue(line)
            elif words[0] == 'If' and words[1] == 'sensor':
                self.compileIfSensor(line)
            elif words[0] == 'If' and words[1] == 'signal' and words[2] == 'head':
                self.compileIfSignalHead(line)
            elif words[0] == 'If' and words[1] == 'signal' and words[2] == 'mast':
                self.compileIfSignalMast(line)
            elif words[0] == 'If' and words[1] == 'speed' and words[4] == 'mast':
                self.compileIfSignalSpeed(line)
            elif words[0] == 'Loop':
                self.compileLoop(line)
            elif words[0] == 'Print':
                self.compilePrint(line)
            elif words[0] == 'Repeat':
                self.compileRepeat(line)
            elif words[0] == 'Release' and words[1] == 'signal' and words[2] == 'head':
                self.compileReleaseSignalHead(line)
            elif words[0] == 'Release' and words[1] == 'signal' and words[2] == 'mast':
                self.compileReleaseSignalMast(line)
            elif words[0] == 'Release':
                self.compileReleaseThrottle(line)
            elif words[0] == 'Set' and words[1] == 'block':
                self.compileSetBlock(line)
            elif words[0] == 'Set' and words[1] == 'direction':
                self.compileSetDirection(line)
            elif words[0] == 'Set' and words[1] == 'function':
                self.compileSetFKey(line)
            elif words[0] == 'Set' and words[1] == 'memory':
                self.compileSetMemory(line)
            elif words[0] == 'Set' and words[1] == 'route':
                self.compileSetRoute(line)
            elif words[0] == 'Set' and words[1] == 'sensor':
                self.compileSetSensor(line)
            elif words[0] == 'Set' and words[1] == 'speed':
                self.compileSetSpeed(line)
            elif words[0] == 'Set' and words[1] == 'turnout':
                self.compileSetTurnout(line)
            elif words[0] == 'Set' and words[1] == 'turntable':
                self.compileSetTurntable(line)
            elif words[0] == 'Start':
                self.compileStart(line)
            elif words[0] == 'Stop':
                self.compileStop(line)
            elif words[0] == 'Sub':
                self.compileSub(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[2] == 'block':
                self.compileWaitBlock(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[2] == 'memory':
                self.compileWaitMemory(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[2] == 'sensor':
                self.compileWaitSensor(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[3] == 'head':
                self.compileSignalHead(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[3] == 'mast':
                self.compileSignalMast(line)
            elif words[0] == 'Wait' and words[1] == 'while' and words[2] == 'signal':
                self.compileSignalSpeed(line)
            elif words[0] == 'Wait' and words[1] == 'for' and 'second' in line:
                self.compileWaitTime(line)
            else:
                self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))

    def compileAssign(self, line):
        # Assign <long | short> address <dccaddr> [[ as <train name>] in <blockname>]
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        words = line.split()
        flds = 2
        regex = '\s*Assign\s+(long|short)\s+address\s+(\d+)'
        if 'as' in words:
            regex += '\s+as\s+(.+\S)'
            flds += 1
            if 'in' in words:
                regex += '\s+in\s+(.+\S)'
                flds += 1
        pattern = re.compile(regex)
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != flds:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        grps = result[0]
        addrSize = grps[0]
        try:
            num = int(grps[1])
        except ValueError:
            self.compileMessages.append('{} - Assign error at line {}: the DCC address, {}, is not a number'.format(self.threadName, self.lineNumber, grps[1]))
            return
        addrNum = num
        trainName = '' if flds < 3 else grps[2]
        blockName = '' if flds < 4 else grps[3]
        if blockName != '':
            layoutBlock = layoutblocks.getLayoutBlock(blockName)
            if layoutBlock is None:
                self.compileMessages.append('{} - Assign error at line {}: start block "{}" does not exist'.format(self.threadName, self.lineNumber, blockName))
                return
        self.actionTokens.append(['Assign', addrNum, addrSize, trainName, blockName])

    def compileCallSub(self, line):
        # CallSub <subname>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*CallSub\s+(.+\S)')
        result = re.findall(pattern, line)
        if len(result) == 0: # or len(result[0]) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        self.actionTokens.append(['CallSub', result[0]])

    def compileDispatch(self, line):
        # Dispatch using file <traininfo.xml>[, type <USER, value <dccAddress> | ROSTER, value <roster entry name> | OPERATIONS, value <train name>>]
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)

        regex = '\s*Dispatch\s+using\s+file\s+(.+?\S)($|,\s+type\s+(USER|ROSTER|OPERATIONS),\s+value\s+(.+))'
        pattern = re.compile(regex)
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 4:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return

        grps = result[0]
        dispFile = grps[0]
        dispType = grps[2]
        dispValue = grps[3]

        if len(dispType) == 0:
            dispType = 'NONE'
            dispValue = ''

        # Validate type values
        if dispType == 'USER':
            try:
                num = int(dispValue)
            except ValueError:
                self.compileMessages.append('{} - Value error at line {}: the DCC address, {}, is not a number'
                        .format(self.threadName, self.lineNumber, dispValue))
                return

        elif dispType == 'ROSTER':
            rosterEntry = jmri.jmrit.roster.Roster.getDefault().getEntryForId(dispValue)
            if rosterEntry is None:
                self.compileMessages.append('{} - Value error at line {}: the roster entry, {}, does not exist'
                        .format(self.threadName, self.lineNumber, dispValue))
                return

        elif dispType == 'OPERATIONS':
            opsTrain = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager).getTrainByName(dispValue)
            if opsTrain is None:
                self.compileMessages.append('{} - Value error at line {}: the operations train, {}, does not exist'
                        .format(self.threadName, self.lineNumber, dispValue))
                return

        self.actionTokens.append(['Dispatch', dispFile, dispType, dispValue])

    def compileElse(self, line):
        # Else
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)

        if len(self.ifStack) == 0:
            self.compileMessages.append('{} - Else error at line {}: There is no matching If statement'.format(self.threadName, self.lineNumber))
            return
        key = self.ifStack[len(self.ifStack) - 1] # Get the current if key
        elseAddr, endAddr = self.ifList[key]        # And the Else and Endif addresses
        elseAddr = len(self.actionTokens) + 1       # Update the Else address
        self.ifList[key] = (elseAddr, endAddr)      # Update the list

        self.actionTokens.append(['Else'])

    def compileEndIf(self, line):
        # Endif
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)

        if len(self.ifStack) == 0:
            self.compileMessages.append('{} - EndIf error at line {}: There is no matching If statement'.format(self.threadName, self.lineNumber))
            return
        key = self.ifStack.pop()                   # Get the current if key
        elseAddr, endAddr = self.ifList[key]       # And the If, Else and Endif indexes
        endAddr = len(self.actionTokens) + 1       # Update the EndIf index
        self.ifList[key] = (elseAddr, endAddr)     # Update the list

        self.actionTokens.append(['Endif'])

    def compileEndSub(self, line):
        # EndSub <subname>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*EndSub\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0: # or len(result[0]) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        self.actionTokens.append(['EndSub', result[0]])

    def compileHoldSignalHead(self, line):
        # Hold signal head <headName>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Hold\s+signal\s+head\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        headName = result[0]
        head = signals.getSignalHead(headName)
        if head is None:
            self.compileMessages.append('{} - Hold signal head error at line {}: head "{}" not found'.format(self.threadName, self.lineNumber, headName))
            return
        if logLevel > 2: print 'HoldHead', headName
        self.actionTokens.append(['HoldHead', headName])

    def compileHoldSignalMast(self, line):
        # Hold signal mast <mastName>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Hold\s+signal\s+mast\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        mastName = result[0]
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.compileMessages.append('{} - Hold signal mast error at line {}: head "{}" not found'.format(self.threadName, self.lineNumber, mastName))
            return
        self.actionTokens.append(['HoldMast', mastName])

    def compileIfBlock(self, line):
        # If block <block name> is <occupied | unoccupied | reserved | free>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*If\s+block\s+(.+\S)\s+is\s+(occupied|unoccupied|reserved|free)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        blockName, blockState = result[0]
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.compileMessages.append('{} - Block error at line {}: block {} not found'.format(self.threadName, self.lineNumber, blockName))
            return
        if layoutBlock.getOccupancySensor() is None:
            self.compileMessages.append('{} - Block error at line {}: occupancy sensor for block {} not found'.format(self.threadName, self.lineNumber, blockName))
            return
        self.actionTokens.append(['IfBlock', blockName, blockState])
        self.createIf()

    def compileIfMemoryValue(self, line):
        # If value for memory <memory name> is <eq | ne | lt | gt | le | ge> <value>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*If\s+value\s+for\s+memory\s+(.+\S)\s+is\s+(eq|ne|gt|lt|ge|le)\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 3:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        memoryName, operator, value = result[0]
        memory = memories.getMemory(memoryName)
        if memory is None:
            self.compileMessages.append('{} - If memory value error at line {}: memory "{}" not found'.format(self.threadName, self.lineNumber, memoryName))
            return
        if not value:
            self.compileMessages.append('{} - If memory value error at line {}: the value is empty'.format(self.threadName, self.lineNumber))
            return
        if logLevel > 2: print 'IfMemoryValue', memoryName, operator, value
        self.actionTokens.append(['IfMemoryValue', memoryName, operator, value])
        self.createIf()

    def compileIfSensor(self, line):
        # If sensor <sensor name> is <active | inactive>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*If\s+sensor\s+(.+\S)\s+is\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('{} - If sensor error at line {}: sensor {} not found'.format(self.threadName, self.lineNumber, sensorName))
            return
        self.actionTokens.append(['IfSensor', sensorName, sensorState])
        self.createIf()

    def compileIfSignalHead(self, line):
        # If signal head <head name> does [not] show <appearance> [or ...]
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*If\s+signal\s+head\s+(.+\S)\s+does\s+(not\s)?show\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 3:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        headName, optionalNot, headStates = result[0]
        head = signals.getSignalHead(headName)
        if head is None:
            self.compileMessages.append('{} - If signal head error at line {}: head "{}" not found'.format(self.threadName, self.lineNumber, headName))
            return
        notOption = False
        if optionalNot == 'not ':
            notOption = True
        stateList = headStates.split(' or ')
        stateMap = {}
        for stateNumber in head.getValidStates():
            stateName = head.getAppearanceName(stateNumber)
            stateMap[stateName] = stateNumber
        stateNums = []
        for state in stateList:
            state = state.strip()
            if state in stateMap:
                stateNums.append(stateMap[state])
            else:
                self.compileMessages.append('{} - If signal head error at line {}: "{}" is not a valid appearance'.format(self.threadName, self.lineNumber, state))
                return
        if len(stateNums) == 0:
            self.compileMessages.append('{} - If signal head error at line {}: no signal head states found'.format(self.threadName, self.lineNumber))
            return
        if logLevel > 2: print 'IfHead', headName, stateNums, notOption
        self.actionTokens.append(['IfHead', headName, stateNums, notOption])
        self.createIf()

    def compileIfSignalMast(self, line):
        # If signal mast <mast name> does [not] display <aspect> [or ...]
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*If\s+signal\s+mast\s+(.+\S)\s+does\s+(not\s+)?display\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 3:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        mastName, optionalNot, mastStates = result[0]
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.compileMessages.append('{} - If signal mast error at line {}: mast "{}" not found'.format(self.threadName, self.lineNumber, mastName))
            return
        notOption = False
        if optionalNot == 'not ':
            notOption = True
        aspectList = mastStates.split(' or ')
        aspectMap = mast.getValidAspects()
        aspectNames = []
        for aspect in aspectList:
            aspect = aspect.strip()
            if aspect in aspectMap:
                aspectNames.append(aspect)
            else:
                self.compileMessages.append('{} - If signal mast error at line {}: "{}" is not a valid aspect'.format(self.threadName, self.lineNumber, aspect))
                return
        if len(aspectNames) == 0:
            self.compileMessages.append('{} - If signal mast error at line {}: no valid signal mast aspects found'.format(self.threadName, self.lineNumber))
            return
        self.actionTokens.append(['IfMast', mastName, aspectNames, notOption])
        self.createIf()

    def compileIfSignalSpeed(self, line):
        # If speed for signal mast <mast name> is <eq | ne | lt | gt | le | ge> <speed name>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*If\s+speed\s+for\s+signal\s+mast\s+(.+\S)\s+is\s+(eq|ne|gt|lt|ge|le)\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 3:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        mastName, operator, speedName = result[0]
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.compileMessages.append('{} - If mast speed error at line {}: mast "{}" not found'.format(self.threadName, self.lineNumber, mastName))
            return
        speedMap = jmri.InstanceManager.getDefault(jmri.implementation.SignalSpeedMap)
        if speedMap is None:
            self.compileMessages.append('{} - If mast speed error at line {}: Unexpected error: get SpeedMap'.format(self.threadName, self.lineNumber))
            return
        speedNameList = speedMap.getValidSpeedNames()
        if speedNameList is None:
            self.compileMessages.append('{} - If mast speed error at line {}: Unexpected error: getValidSpeedNames'.format(self.threadName, self.lineNumber))
            return
        if speedName not in speedNameList:
            self.compileMessages.append('{} - If mast speed error at line {}: "{}" is not a valid speed name'.format(self.threadName, self.lineNumber, speedName))
            return
        if logLevel > 2: print 'IfSpeed', mastName, operator, speedName
        self.actionTokens.append(['IfSpeed', mastName, operator, speedName])
        self.createIf()

    def compileLoop(self, line):
        # Loop
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        if self.loopAddr != -1:
            self.compileMessages.append('{} - Loop error at line {}: Duplicate Loop statement, only 1 allowed'.format(self.threadName, self.lineNumber))
            return
        self.actionTokens.append(['Loop'])
        self.loopAddr = len(self.actionTokens)
        print 'loopAddr = {}'.format(self.loopAddr)

    def compilePrint(self, line):
        # Print message text
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Print\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0: # or len(result[0]) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        self.actionTokens.append(['Print', result[0]])

    def compileReleaseSignalHead(self, line):
        # Release signal head <headName>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Release\s+signal\s+head\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        headName = result[0]
        head = signals.getSignalHead(headName)
        if head is None:
            self.compileMessages.append('{} - Release signal head error at line {}: head "{}" not found'.format(self.threadName, self.lineNumber, headName))
            return
        if logLevel > 2: print 'ReleaseHead', headName
        self.actionTokens.append(['ReleaseHead', headName])

    def compileReleaseSignalMast(self, line):
        # Release signal mast <mastName>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Release\s+signal\s+mast\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        mastName = result[0]
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.compileMessages.append('{} - Release signal mast error at line {}: head "{}" not found'.format(self.threadName, self.lineNumber, mastName))
            return
        self.actionTokens.append(['ReleaseMast', mastName])

    def compileReleaseThrottle(self, line):
        # Release throttle
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Release\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or result[0] != 'throttle':
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        self.actionTokens.append(['ReleaseThrottle'])

    def compileRepeat(self, line):
        # Repeat if sensor <sensor name> is <active | inactive>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        if self.loopAddr == -1:
            self.compileMessages.append('{} - Repeat error at line {}: A Loop point has not been defined'.format(self.threadName, self.lineNumber))
            return
        pattern = re.compile('\s*Repeat\s+if\s+sensor\s+(.+\S)\s+is\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('{} - Repeat error at line {}: sensor {} does not exist'.format(self.threadName, self.lineNumber, sensorName))
            return
        self.actionTokens.append(['Repeat', sensorName, sensorState])

    def compileSetBlock(self, line):
        # Set block <block name> <occupied | unoccupied | reserved | free>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Set\s+block\s+(.+\S)\s+(occupied|unoccupied|reserved|free)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        blockName, blockState = result[0]
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.compileMessages.append('{} - Block error at line {}: block "{}" not found'.format(self.threadName, self.lineNumber, blockName))
            return
        if layoutBlock.getOccupancySensor() is None:
            self.compileMessages.append('{} - Block error at line {}: occupancy sensor for block {} not found'.format(self.threadName, self.lineNumber, blockName))
            return
        self.actionTokens.append(['SetBlock', blockName, blockState])

    def compileSetDirection(self, line):
        # Set direction to <forward | reverse>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Set\s+direction\s+to\s+(forward|reverse)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        self.actionTokens.append(['SetDirection', result[0]])

    def compileSetFKey(self, line):
        # Set function key <n> <on | off>[, wait <n> seconds]
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        words = line.split()
        flds = 2
        regex = '\s*Set\s+function\s+key\s+(\d+)\s+(on|off)'
        if 'wait' in words:
            regex += ', wait (\d+) second'
            flds += 1
        pattern = re.compile(regex)
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != flds:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        grps = result[0]
        try:
            keyNum = int(grps[0])
        except ValueError:
            self.compileMessages.append('{} - Function key error at line {}: the key value, {}, is not an integer'.format(self.threadName, self.lineNumber, grps[0]))
            return
        else:
            if keyNum < 0 or keyNum > 68:
                self.compileMessages.append('{} - Function key error at line {}: the key value, {}, is not in the range 0-68'.format(self.threadName, self.lineNumber, grps[0]))
                return
        keyState = grps[1]
        if flds ==2:
            keyWait = 0
        else:
            try:
                keyWait = float(grps[2])
            except ValueError:
                self.compileMessages.append('{} - Function key error at line {}: the wait time, {}, is not a number'.format(self.threadName, self.lineNumber, grps[2]))
                return
        self.actionTokens.append(['SetFKey', keyNum, keyState, int(keyWait * 1000)])

    def compileSetMemory(self, line):
        # Set memory <name> to <value>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Set memory\s+(.+\S)\s+to\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        memoryName, memoryValue = result[0]
        memory = memories.getMemory(memoryName)
        if memory is None:
            self.compileMessages.append('{} - Memory error at line {}: memory "{}" not found'.format(self.threadName, self.lineNumber, memoryName))
            return
        self.actionTokens.append(['SetMemory', memoryName, memoryValue])

    def compileSetRoute(self,line):
        # Set route <route name>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Set\s+route\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0: # or len(result[0]) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        routeName = result[0]
        if routes.getRoute(routeName) is None:
            self.compileMessages.append('{} - Route error at line {}: route "{}" not found'.format(self.threadName, self.lineNumber, routeName))
            return
        self.actionTokens.append(['SetRoute', routeName])

    def compileSetSensor(self, line):
        # Set sensor <sensor name> <active | inactive>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Set\s+sensor\s+(.+\S)\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('{} - Sensor error at line {}: sensor "{}" not found'.format(self.threadName, self.lineNumber, sensorName))
            return
        self.actionTokens.append(['SetSensor', sensorName, sensorState])

    def compileSetSpeed(self, line):
        # Set speed to <0 to 1.0>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Set\s+speed\s+to\s+(\S+)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        try:
            num = float(result[0])
        except ValueError:
            self.compileMessages.append('{} - Train speed error at line {}: the speed, {}, is not a number'.format(self.threadName, self.lineNumber, result[0]))
        else:
            if num < 0.0:
                num = 0.0
            if num > 1.0:
                num = 1.0
            self.actionTokens.append(['SetSpeed', num])

    def compileSetTurnout(self, line):
        # Set turnout <turnout name> <closed | thrown>[, wait <n> seconds]
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        words = line.split()
        flds = 2
        regex = '\s*Set\s+turnout\s+(.+\S)\s+(closed|thrown)'
        if 'wait' in words:
            regex += ', wait (\d+) second'
            flds += 1
        pattern = re.compile(regex)
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != flds:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        grps = result[0]
        turnoutName = grps[0]
        turnoutState = grps[1]
        turnoutWait = 0 if flds < 3 else grps[2]
        if turnouts.getTurnout(turnoutName) is None:
            self.compileMessages.append('{} - Turnout error at line {}: turnout {} not found'.format(self.threadName, self.lineNumber, grps[0]))
            return
        try:
            num = float(turnoutWait)
        except ValueError:
            self.compileMessages.append('{} - Turnout error at line {}: the wait time, {}, is not a number'.format(self.threadName, self.lineNumber, turnoutWait))
        else:
            self.actionTokens.append(['SetTurnout', turnoutName, turnoutState, int(num * 1000)])

    def compileSetTurntable(self, line):
        # Set turntable <turntable name> on panel <panel name> to ray <#>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        words = line.split()
        flds = 3
        regex = '\s*Set\s+turntable\s+(.+)\s+on\s+panel\s(.+)\sto\s+ray\s(\d+)'

        pattern = re.compile(regex)
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != flds:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        grps = result[0]
        turntableName = grps[0]
        panelName = grps[1]
        rayIndex = grps[2]

        try:
            num = int(rayIndex)
        except ValueError:
            self.compileMessages.append('{} - Turntable error at line {}: the ray index, {}, is not a number'.format(self.threadName, self.lineNumber, turnoutWait))
            return

        editorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
        for layout in editorManager.getAll(jmri.jmrit.display.layoutEditor.LayoutEditor):
            if layout.getTitle() == panelName:
                for turntable in layout.getLayoutTurntableViews():
                    if turntable.getId() == turntableName:
                        for raytrack in turntable.getRayTrackList():
                            if raytrack.getConnectionIndex() == num:
                                self.actionTokens.append(['SetTurntable', turntableName, panelName, num])
                                return
                        self.compileMessages.append('{} - Turntable error at line {}: the ray index, {}, is not found'.format(self.threadName, self.lineNumber, num))
                        return
                self.compileMessages.append('{} - Turntable error at line {}: the turntable name, {}, is not found'.format(self.threadName, self.lineNumber, turntableName))
                return
        self.compileMessages.append('{} - Turntable error at line {}: the layout name, {}, is not found'.format(self.threadName, self.lineNumber, panelName))

    def compileStart(self, line):
        # Start when sensor <sensor name> is <active | inactive>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Start\s+when\s+sensor\s+(.+\S)\s+is\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error at line {}: {}'.format(line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('{} - Start error at line {}: sensor {} does not exist'.format(self.threadName, self.lineNumber, sensorName))
            return
        self.actionTokens.append(['Start', sensorName, sensorState])

    def compileStop(self, line):
        # Stop if sensor <sensor name> is <active | inactive>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Stop\s+if\s+sensor\s+(.+\S)\s+is\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('{} - Stop error at line {}: sensor {} does not exist'.format(self.threadName, self.lineNumber, sensorName))
            return
        self.actionTokens.append(['Stop', sensorName, sensorState])

    def compileSub(self, line):
        # Sub <subname>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Sub\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0: # or len(result[0]) != 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        self.actionTokens.append(['Sub', result[0]])
        self.subList[result[0]] = len(self.actionTokens)

    def compileWaitBlock(self, line):
        # Wait for block <block name> to become <occupied | unoccupied | free>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Wait\s+for\s+block\s+(.+\S)\s+to\s+become\s+(occupied|unoccupied|free)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        blockName, blockState = result[0]
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.compileMessages.append('{} - Wait block error at line {}: block {} not found'.format(self.threadName, self.lineNumber, blockName))
            return
        if layoutBlock.getOccupancySensor() is None:
            self.compileMessages.append('{} - Wait block error at line {}: occupancy sensor for block {} not found'.format(self.threadName, self.lineNumber, blockName))
            return
        self.actionTokens.append(['WaitBlock', blockName, blockState])
        return

    def compileWaitMemory(self, line):
        # Wait for memory <memory name> value to change
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Wait\s+for\s+memory\s+(.+\S)\s+value\s+to\s+change')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        memoryName = result[0]
        if memories.getMemory(memoryName) is None:
            self.compileMessages.append('{} - Wait memory error at line {}: memory {} not found'.format(self.threadName, self.lineNumber, memoryName))
            return
        self.actionTokens.append(['WaitMemory', memoryName])
        return

    def compileWaitSensor(self, line):
        # Wait for sensor <sensor name> to become <active | inactive>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Wait\s+for\s+sensor\s+(.+\S)\s+to\s+become\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('{} - Wait sensor error at line {}: sensor {} not found'.format(self.threadName, self.lineNumber, sensorName))
            return
        self.actionTokens.append(['WaitSensor', sensorName, sensorState])

    def compileWaitTime(self, line):
        # Wait for <n> [to <n>] seconds
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)

        flds = 1
        regex = '\s*Wait\s+(for)\s+(\S+)'  # Note: capture the word 'for' to force a tuple result
        if ' to ' in line:
            regex += '\s+to\s+(\S+)'
            flds += 1
        regex += '\s+second'
        pattern = re.compile(regex)
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)

        if len(result) == 0 or len(result[0]) != flds + 1:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return

        times = result[0]   # The first tuple entry is 'for', the 2nd and possible third entries are seconds duration numbers
        try:
            time1 = float(times[1])
        except ValueError:
            self.compileMessages.append('{} - Wait time error at line {}: the wait time, {}, is not a number'.format(self.threadName, self.lineNumber, times[1]))
            return
        else:
            if time1 < 0.0:
                self.compileMessages.append('{} - Wait time error at line {}: the wait time, {}, is less than zero'.format(self.threadName, self.lineNumber, time1))
                return
        time2 = time1

        if flds == 2:
            try:
                time2 = float(times[2])
            except ValueError:
                self.compileMessages.append('{} - Wait seconds error at line {}: the second time, {}, is not a number'.format(self.threadName, self.lineNumber, times[2]))
                return
            else:
                if not time2 > time1:
                    self.compileMessages.append('{} - Wait seconds error at line {}: the second time, {}, is not greater than the first seconds'.format(self.threadName, self.lineNumber, time2))
                    return

        self.actionTokens.append(['WaitTime', int(time1 * 1000), int(time2 * 1000)])

    def compileSignalHead(self, line):
        # Wait for signal head <head name> to [not] show <appearance name> [or ...]
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Wait\s+for\s+signal\s+head\s+(.+\S)\s+to\s+(not\s)?show\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 3:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        headName, optionalNot, headStates = result[0]
        head = signals.getSignalHead(headName)
        if head is None:
            self.compileMessages.append('{} - Wait signal head error at line {}: head "{}" not found'.format(self.threadName, self.lineNumber, headName))
            return
        notOption = False
        if optionalNot == 'not ':
            notOption = True
        stateList = headStates.split(' or ')
        stateMap = {}
        for stateNumber in head.getValidStates():
            stateName = head.getAppearanceName(stateNumber)
            stateMap[stateName] = stateNumber
        stateNums = []
        for state in stateList:
            state = state.strip()
            if state in stateMap:
                stateNums.append(stateMap[state])
            else:
                self.compileMessages.append('{} - Wait signal head error at line {}: "{}" is not a valid appearance'.format(self.threadName, self.lineNumber, state))
                return
        if len(stateNums) == 0:
            self.compileMessages.append('{} - Wait signal head error at line {}: no signal head states found'.format(self.threadName, self.lineNumber))
            return
        if logLevel > 2: print 'WaitHead', headName, stateNums, notOption
        self.actionTokens.append(['WaitHead', headName, stateNums, notOption])

    def compileSignalMast(self, line):
        # Wait for signal mast <mast name> to [not] display <aspect name> [or ...]
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Wait\s+for\s+signal\s+mast\s+(.+\S)\s+to\s+(not\s)?display\s+(.+\S)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 3:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        mastName, optionalNot, mastStates = result[0]
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.compileMessages.append('{} - Wait signal mast error at line {}: mast "{}" not found'.format(self.threadName, self.lineNumber, mastName))
            return
        notOption = False
        if optionalNot == 'not ':
            notOption = True
        aspectList = mastStates.split(' or ')
        aspectMap = mast.getValidAspects()
        aspectNames = []
        for aspect in aspectList:
            aspect = aspect.strip()
            if aspect in aspectMap:
                aspectNames.append(aspect)
            else:
                self.compileMessages.append('{} - Wait signal mast error at line {}: "{}" is not a valid aspect'.format(self.threadName, self.lineNumber, aspect))
                return
        if len(aspectNames) == 0:
            self.compileMessages.append('{} - Wait signal mast error at line {}: no valid signal mast aspects found'.format(self.threadName, self.lineNumber))
            return
        self.actionTokens.append(['WaitMast', mastName, aspectNames, notOption])

    def compileSignalSpeed(self, line):
        # Wait while signal mast <mast name> speed is less than <aspect name> speed
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        pattern = re.compile('\s*Wait\s+while\s+signal\s+mast\s+(.+\S)\s+speed\s+is\s+less\s+than\s+(.+\S)\s+speed')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    {} - result = {}'.format(self.threadName, result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('{} - Syntax error at line {}: {}'.format(self.threadName, self.lineNumber, line))
            return
        mastName, aspectName = result[0]
        mast = masts.getSignalMast(mastName)
        if mast is None:
            self.compileMessages.append('{} - Wait mast speed error at line {}: mast "{}" not found'.format(self.threadName, self.lineNumber, mastName))
            return
        aspectMap = mast.getValidAspects()
        if aspectName not in aspectMap:
            self.compileMessages.append('{} - Wait mast speed error at line {}: "{}" is not a valid aspect'.format(self.threadName, self.lineNumber, aspectName))
            return
        signalSystem = mast.getSignalSystem()
        if signalSystem is None:
            self.compileMessages.append('{} - Wait mast speed error at line {}: Unexpected error: getSignalSystem'.format(self.threadName, self.lineNumber))
            return
        speedMap = jmri.InstanceManager.getDefault(jmri.implementation.SignalSpeedMap)
        if speedMap is None:
            self.compileMessages.append('{} - Wait mast speed error at line {}: Unexpected error: get SpeedMap'.format(self.threadName, self.lineNumber))
            return
        speedName = speedMap.getAspectSpeed(aspectName, signalSystem)
        if speedName is None:
            self.compileMessages.append('{} - Wait mast speed error at line {}: Unexpected error: getAspectSpeed'.format(self.threadName, self.lineNumber))
            return
        aspectSpeed = speedMap.getSpeed(speedName)
        if logLevel > 2: print 'WaitSpeed', mastName, aspectName, speedName, aspectSpeed
        self.actionTokens.append(['WaitSpeed', mastName, aspectSpeed])

# End of class YetAnotherAutoTrain

class YAATMaster(jmri.jmrit.automat.AbstractAutomaton):
    def init(self):
        if logLevel > 0: print 'Create Master Thread'

    def setup(self):
        if yaatMaster is None:
            return False
        setYaatMaster(INACTIVE)
        return True

    def handle(self):
        self.waitSensorActive(yaatMaster)
        for thread in instanceList:
            if thread is not None:
                if thread.isRunning():
                    if logLevel > 0: print 'Stop "{}" thread'.format(thread.getName())
                    if thread.throttle is not None:
                        thread.throttle.setSpeedSetting(0.0)
                        thread.throttle.release(None)
                    thread.stop()

        setYaatRunnning(INACTIVE)
        setYaatMaster(INACTIVE)

        if memoryListener is not None:
            memoryListener.removeListener()

        return False;

# End of class YAATMaster

# Compile and run a train when the memory variable contains a train file name.
class YAATMemoryListener(java.beans.PropertyChangeListener):
    def __init__(self):
        if trainMemory is None:
            return None
        trainMemory.setValue('')    # Clear any residual values
        trainMemory.removePropertyChangeListener(self)
        trainMemory.addPropertyChangeListener(self)
        return

    def propertyChange(self, event):
        if event.getPropertyName() == 'value':
            fileName = str(event.getNewValue())
            if fileName is not None and len(fileName) > 4:
                startTrain(fileName.strip())

    def removeListener(self):
        if trainMemory is not None:
            trainMemory.removePropertyChangeListener(self)

# End of class YAATMemoryListener

##
# Check if a compile is needed.  The compile content is stored in a file using the Python pickle process.
# True if save option not active or source modification time is greater than the pickle file time.
##
def compileRequired(fullPath):
    # Always true when the compile option is not active
    if not saveYAATcompiles: return (True, '')

    # Create the yaatp directory if necessary
    pickleLocation = jmri.util.FileUtil.getUserFilesPath() + 'yaatp'
    jmri.util.FileUtil.createDirectory(pickleLocation)

    sourceTime = 0
    pickleTime = 0
    sourceName = os.path.basename(fullPath)
    fullPickleLocation = pickleLocation + os.sep + sourceName

    if os.path.exists(fullPath):
        sourceTime = os.path.getmtime(fullPath)
    if os.path.exists(fullPickleLocation):
        pickleTime = os.path.getmtime(fullPickleLocation)

    if logLevel > 1: print 'Source file = {}, source time = {}, compile time = {}'.format(sourceName, sourceTime, pickleTime)
    return (sourceTime > pickleTime, fullPickleLocation)

# Check the trainLines list for the @include keyword.  Replace the line with the lines from
# the included file.
def expandIncludes(sourceLines):
    expandedLines = []
    for line in sourceLines:
        if logLevel > 2: print 'expandIncludes: input line = {}'.format(line)
        if line[:8] == '@include':
            includeFName = line[9:].strip()
            if logLevel > 2: print 'expandIncludes: included file name = {}'.format(includeFName)
            includeFile = yaatIncludes + includeFName
            try:
                with open(includeFile) as file:
                    expandedLines.extend([includeLine.strip() for includeLine in file])
            except IOError, e:
                print 'Error loading include file, error :: {}'.format(e)
        else:
            expandedLines.append(line)
    return expandedLines

instanceList = []   # List of train instances
def startTrain(fileName):
    if len(fileName) == 0:
        return
    fullName = yaatLocation + fileName
    trainName = fileName[:-4]
    compileNeeded, pickleName = compileRequired(fullName)
    trainLines = []
    if compileNeeded:
        try:
            with open(fullName) as file:
                trainLines = [line.strip() for line in file]
            if enableIncludes:
                trainLines = expandIncludes(trainLines)
        except IOError, e:
            print 'Error loading trainLines, error :: {}'.format(e)

    idx = len(instanceList)
    instanceList.append(YetAnotherAutoTrain())          # Add a new instance
    instanceList[idx].setName(trainName)                # Set the instance name
    if instanceList[idx].setup(trainLines, compileNeeded, pickleName):   # Compile the train actions
        instanceList[idx].start()                       # Compile was successful

print 'YAAT v3.4'
startTime = time()

try:
    with open(yaatLocation + 'LoadTrains.txt') as file:
        for line in file:
            if len(line) > 4:
                startTrain(line.strip())
except IOError, e:
    pass    # Ignore file errors since this file is optional

endTime = time()
if logLevel > 1: print '\nTiming'
if logLevel > 1: print ('  Load duration: {}').format(endTime - startTime)

# Start YAAT memory listener
memoryListener = YAATMemoryListener()

# Keep last -- create the master thread
master = YAATMaster()
if master.setup():
    master.setName('YAAT Master')
    master.start()
