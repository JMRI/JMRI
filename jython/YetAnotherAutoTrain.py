# YetAnotherAutoTrain.py -- Data driven automatic train
# Use a list of actions to automatically run a train.
# v1.3 -- Add line numbers to the compiler error messages.
# v1.4 -- Add a master controller that can be used to terminate all of the threads.
# v1.5 -- Add signal mast and signal head options.
# v1.6 -- Add "Loop" to separate one time startup actions from the main loop.
#         Add "Repeat" to separate one time finish/clieanup actions from the main loop.
#         Add "Set route" to invoke a JMRI Route.
#         Add "Print" to send text to the Script output or system console.
# Author:  Dave Sand copyright (c) 2018

# Action Phrase Descriptions:
#       <?? | ??> represent choices
#       <????> require values, normally names or numbers
#       Brackets, [], are optional phrases
#       Text and names are case sensitive.
#
# Start when sensor <sensor name> is <active | inactive>
#       An optional action that defers running the train until the condition has been satisfied.
#       This can also be used to pause a train between runs.
# Assign <long | short> address <dccaddr>[ as <train name>[ in <blockname>]]
#       If a block name is supplied, the optional train name will be used for block tracking
# Loop
#       Marks the end of the one time start up actions, such as the throttle assignment, positioning a train before
#       starting repeating actions, etc.
# Print <message text>
#       Display a message in the script output window or the system console log.  Useful for debugging.
# Set speed to <0 to 1.0>
# Set direction to <forward | reverse>
# Set function key <0 to 28> <on | off>[, wait <n> seconds]
#       Set the function key on or off.  The number can be from 0 to 28.
#       If seconds is greater than zero, the opposite action will be
#       performed after the number of seconds has passed.
# Set turnout <turnout name> <closed | thrown>[, wait <n> seconds]
#       The process will wait for up to 5 seconds for turnout feedback.
#       If seconds is entered and greater than zero, a wait allows the turnout command to complete, capacitors to recharge, etc.
# Set sensor <sensor name> <active | inactive>
#       Can be used to pass status to other trains
# Set block <block name> <occupied | unoccupied | reserved | free>
#       The occupied and unoccupied states are used to simulate train movement.  It works best if a simulation sensor
#       is used in conjunction with an If statement.
#       Reserved and free control the alternate track color.
# Set route <route name>
# Wait for <n> seconds
#       Wait until the time has expired.  Normally used for station stops.
# Wait for sensor <sensor name> to become <active | inactive>
# Wait for block <block name> to become <occupied | unoccupied | reserved | free>
# Wait for signal head <head name> to [not] show <appearance name> [or ...]
#       The appearance names are language specific.  Use the signal head table to get the available appearance names.
# Wait for signal mast <mast name> to [not] display <aspect name> [or ...]
#       Use the signal mast table to get the valid aspect names.  Remember that the names vary based on signal mast type.
# Wait while signal mast <mast name> speed is less than <aspect name> speed
#       Use the signal mast table to get the valid aspect names.  Remember that the names vary based on signal mast type.
# Repeat if sensor <sensor name> is <active | inactive>
#       Skip the remaining steps and start over.  Use the same sensor as Stop with the opposite test.
#       This provides a cleanup section before Stop.
#       Requires that a "Loop" action was included in the action list.
# Stop if sensor <sensor name> is <active | inactive>
#       This action needs to be the last one in the list.
#       If the sensor state matches, the throttle will be released and the script stopped.
#       If it does not, the script will do the sequence of actions again.
#       If the Stop action is missing, the script will run forever, until the script thread is killed, or JMRI is stopped.

# -- If / Else / Endif support --
#   The "If" and "Endif" actions are required.  The "Else" action is optional and is used to separate the true and false actions.
# If sensor <sensor name> is <active | inactive>
# If block <block name> is <occupied | unoccupied | reserved | free>
# If signal head <head name> does [not] show <appearance> [or ...]
# If signal mast <mast name> does [not] display <aspect> [or ...]
# If speed for signal mast <mast name> is <eq | ne | lt | gt | le | ge> <speed name>
#       For information on speed names, look at the JMRI install location: xml/signals/signalSpeeds.xml
# Else
# Endif

# Usage
#   Copy the script from the JMRI install location to your user files location.  See "Help >> Locations".
#   Change the log level from 0 to 1 through 4 if log output is desired.  4 provides maximun detail.
#   Optionally enter a valid sensor name in the statusSensor variable.  This is used to provide feedback to JMRI.
#   Optionally enter a valid sensor name in the masterSensor variable.  This is used to stop all threads.
#   Define the actions for each train.  The actions can be embedded in the script or placed in an external file.
#      External file:  Create a text file with one action per line.  Blank lines and lines starting with a comment character, #, are ok.
#                      Add the train name and file name to the "trainList".  The file name can be the complete
#                      path or the file name can include a keyword for the location, such as "preference:"
#                      which is replaced by the path to the user files location at run time.
#      Embedded: The actions are added to a Python list.  Each action is enclosed in single or double quotes
#                and end with a comma at the end of the line.  The embedded method requires a unique block
#                of code at the end of the script for each embedded action list.

import java
import jmri
import re
from javax.swing import JOptionPane

logLevel = 1    # 0 for no output, 4 for the most detail.
statusSensor = 'Run Script'   # Optional sensor to notify JMRI if any threads are active
masterSensor = 'YAAT Master'   # If the optional sensor becomes active, all of the threads will be stopped

trainList = {}
trainList['Train 12'] = 'preference:Train 12.txt'
trainList['Train 16'] = 'preference:Train 16.txt'
trainList['RT Train 1'] = 'preference:RT Train 1.txt'
trainList['RT Train 2'] = 'preference:RT Train 2.txt'
trainList['Signal Test'] = 'preference:Signal Test.txt'
#
BackAndForth = [
'Start when sensor BF-Start is active',
'Set block BF-Left occupied',
'Assign short address 63 as Shuttle in BF-Left',

# Set APB sensors
'Set sensor BF-APB-EB active',
'Set sensor BF-APB-WB inactive',

'Wait for 5 seconds',

# Move east
'Set function key 0 on',
'Set direction to forward',
'Set speed to .5',

'If sensor SimMode is active',
    # Simulate train movement
    'Set block BF-Middle occupied',
    'Wait for 1 second',
    'Set block BF-Left unoccupied',
    'Wait for 1 second',
    'Set block BF-Right occupied',
    'Wait for 1 second',
    'Set block BF-Middle unoccupied',
    'Wait for 1 second',
'Endif',

# Station Stop
'Wait for sensor BF-Right to become active',
'Set speed to 0',
'Set function key 0 off',

# Set APB sensors
'Set sensor BF-APB-EB   inactive',
'Set sensor BF-APB-WB   active',

'Wait for 5 seconds',

# Move west
'Set function key 0 on',
'Set direction to reverse',
'Set speed to .5',

'If sensor SimMode is active',
    # Simulate train movement
    'Set block BF-Middle occupied',
    'Wait for 1 second',
    'Set block BF-Right unoccupied',
    'Wait for 1 second',
    'Set block BF-Left occupied',
    'Wait for 1 second',
    'Set block BF-Middle unoccupied',
    'Wait for 1 second',
'Endif',

# Station Stop
'Wait for sensor BF-Left to become active',
# Gradual stop - Tweak as necessary for block length and train performance
'Set speed to .4',
'Wait for 1 second',
'Set speed to .3',
'Wait for 1 second',
'Set speed to .2',
'Wait for 1 second',
'Set speed to .1',
'Wait for 1 second',
'Set speed to 0',
'Wait for 1 second',
'Set function key 0 off',

'Stop if sensor BF-Stop is active'
]

class YetAnotherAutoTrain(jmri.jmrit.automat.AbstractAutomaton):
    threadCount = 0

    def init(self):
        self.throttle = None
        self.TrueFalseBlock = '- none -'
        self.TrueFalseState = True
        self.execMode = 'Run'
        YetAnotherAutoTrain.threadCount += 1

    def setup(self, actionList):
        self.actionTokens = []
        self.compileMessages = []
        self.lineNumber = 0
        self.hasPreLoop = False
        self.inPreLoop = False
        self.threadName = self.getName()
        self.compile(actionList)
        if len(self.compileMessages) > 1:
            self.displayMessage("\n".join(self.compileMessages))
            YetAnotherAutoTrain.threadCount -= 1
            if YetAnotherAutoTrain.threadCount < 1:
                statSensor = sensors.getSensor(statusSensor)
                if statSensor is not None:
                    statSensor.setKnownState(INACTIVE)
            return False
        if len(self.actionTokens) == 0:
            self.compileMessages.append('The action list is empty')
            return False
        return True

    def handle(self):
        if (self.throttle is None):
            if logLevel > 0: print '>> Start YAAT for {} <<'.format(self.threadName)

        runYAAT = True
        if logLevel > 1: print '{} - Start YAAT Loop'.format(self.threadName)
        for action in self.actionTokens:
            if len(action) == 0:
                self.displayMessage('Empty Action row')
                continue

            if logLevel > 2: print '{} - Action: {}'.format(self.threadName, action)
            actionKey = action[0]

            # Check for the end of the pre loop section, if any.
            #
            if actionKey == 'Loop':
                self.execMode = 'Run'
                continue
            if self.hasPreLoop and self.execMode == 'Skip':
                continue

            # Process Else and Endif actions
            if actionKey == 'Else':
                self.TrueFalseBlock = '- false -'   # Direct execution
                continue
            if actionKey == 'Endif':
                self.TrueFalseBlock = '- none -'    # Direct execution
                continue

            # Check if/else status
            if self.TrueFalseBlock == '- true -':
                if self.TrueFalseState == False:
                    if logLevel > 3: print 'Skip true action: {}'.format(action)
                    continue
            if self.TrueFalseBlock == '- false -':
                if self.TrueFalseState == True:
                    if logLevel > 3: print 'Skip false action: {}'.format(action)
                    continue

            if actionKey == 'Assign':
                self.doAssign(action)
            elif actionKey == 'IfBlock':
                self.doIfBlock(action)
            elif actionKey == 'IfSensor':
                self.doIfSensor(action)
            elif actionKey == 'IfHead':
                self.doIfHead(action)
            elif actionKey == 'IfMast':
                self.doIfMast(action)
            elif actionKey == 'IfSpeed':
                self.doIfSpeed(action)
            elif actionKey == 'Print':
                self.doPrint(action)
            elif actionKey == 'Repeat':
                if self.doRepeat(action):
                    self.execMode = 'Skip'
            elif actionKey == 'SetBlock':
                self.doSetBlock(action)
            elif actionKey == 'SetDirection':
                self.doSetDirection(action)
            elif actionKey == 'SetFKey':
                self.doSetFKey(action)
            elif actionKey == 'SetRoute':
                self.doSetRoute(action)
            elif actionKey == 'SetSensor':
                self.doSetSensor(action)
            elif actionKey == 'SetSpeed':
                self.doSetSpeed(action)
            elif actionKey == 'SetTurnout':
                self.doSetTurnout(action)
            elif actionKey == 'Start':
                self.doStart(action)
            elif actionKey == 'Stop':
                if not self.doStop(action):
                    if logLevel > 0: print '>> Stop YAAT for {} <<'.format(self.threadName)
                    runYAAT = False
                    YetAnotherAutoTrain.threadCount -= 1
                    if YetAnotherAutoTrain.threadCount == 0:
                        statSensor = sensors.getSensor(statusSensor)
                        if statSensor is not None:
                            statSensor.setKnownState(INACTIVE)
                    break;
            elif actionKey == 'WaitBlock':
                self.doWaitBlock(action)
            elif actionKey == 'WaitSensor':
                self.doWaitSensor(action)
            elif actionKey == 'WaitHead':
                self.doWaitHead(action)
            elif actionKey == 'WaitMast':
                self.doWaitMast(action)
            elif actionKey == 'WaitSpeed':
                self.doWaitSpeed(action)
            elif actionKey == 'WaitTime':
                self.waitMsec(action[1])    # Direct execution
            else:
                self.displayMessage('Action, {}, is not valid'.format(actionKey))
        if logLevel > 1: print '{} -  End YAAT Loop'.format(self.threadName)
        self.TrueFalseBlock = '- none -'    # Force Endif
        if self.hasPreLoop:
            self.execMode = 'Skip'
        return runYAAT


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
        self.TrueFalseBlock = '- true -'
        self.TrueFalseState = currentState

    def doIfSensor(self, action):
        act, sensorName, sensorState = action
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('{} - Sensor {} not found'.format(self.threadName, sensorName))
            return
        if sensorState == 'active':
            checkState = ACTIVE
        elif sensorState == 'inactive':
            checkState = INACTIVE
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))
            return

        knownState = sensor.getKnownState()
        self.TrueFalseBlock = '- true -'
        self.TrueFalseState = checkState == knownState

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
        self.TrueFalseBlock = '- true -'
        self.TrueFalseState = checkState

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
        self.TrueFalseBlock = '- true -'
        self.TrueFalseState = checkState

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
        self.TrueFalseBlock = '- true -'
        self.TrueFalseState = checkState

    def doPrint(self, action):
        act, printText = action
        print '{} - {}'.format(self.threadName, printText)
        self.waitMsec(1000)

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

    def displayMessage(self, msg):
        JOptionPane.showMessageDialog(None, msg, 'YAAT Error', JOptionPane.WARNING_MESSAGE)

    def setKey(self, keyNum, keyOn):
        if logLevel > 2: print "{} - Function key = {}, On = {}".format(self.threadName, keyNum, keyOn)
        command = 'self.throttle.setF' + str(keyNum)
        if keyOn:
            command += '(True)'
        else:
            command += '(False)'
        exec(command)


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
            elif words[0] == 'Else':
                self.actionTokens.append(['Else'])
            elif words[0] == 'Endif':
                self.actionTokens.append(['Endif'])
            elif words[0] == 'If' and words[1] == 'block':
                self.compileIfBlock(line)
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
            elif words[0] == 'Set' and words[1] == 'block':
                self.compileSetBlock(line)
            elif words[0] == 'Set' and words[1] == 'direction':
                self.compileSetDirection(line)
            elif words[0] == 'Set' and words[1] == 'function':
                self.compileSetFKey(line)
            elif words[0] == 'Set' and words[1] == 'route':
                self.compileSetRoute(line)
            elif words[0] == 'Set' and words[1] == 'sensor':
                self.compileSetSensor(line)
            elif words[0] == 'Set' and words[1] == 'speed':
                self.compileSetSpeed(line)
            elif words[0] == 'Set' and words[1] == 'turnout':
                self.compileSetTurnout(line)
            elif words[0] == 'Start':
                self.compileStart(line)
            elif words[0] == 'Stop':
                self.compileStop(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[2] == 'block':
                self.compileWaitBlock(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[2] == 'sensor':
                self.compileWaitSensor(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[3] == 'head':
                self.compileSignalHead(line)
            elif words[0] == 'Wait' and words[1] == 'for' and words[3] == 'mast':
                self.compileSignalMast(line)
            elif words[0] == 'Wait' and words[1] == 'while' and words[2] == 'signal':
                self.compileSignalSpeed(line)
            elif words[0] == 'Wait' and words[1] == 'for' and (words[3] == 'seconds' or words[3] == 'second'):
                self.compileWaitTime(words[2])
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
        if logLevel > 2: print 'IfMast', mastName, aspectNames, notOption
        self.actionTokens.append(['IfMast', mastName, aspectNames, notOption])

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

    def compileLoop(self, line):
        # Loop
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
        self.hasPreLoop = True
        self.actionTokens.append(['Loop'])

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

    def compileRepeat(self, line):
        # Repeat if sensor <sensor name> is <active | inactive>
        if logLevel > 2: print '  {} - {}'.format(self.threadName, line)
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
        if not self.hasPreLoop:
            self.compileMessages.append('{} - Repeat error at line {}: The repeat action cannot be used without a preceeding Loop action'.format(self.threadName, self.lineNumber))
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
            if keyNum < 0 or keyNum > 28:
                self.compileMessages.append('{} - Function key error at line {}: the key value, {}, is not in the range 0-28'.format(self.threadName, self.lineNumber, grps[0]))
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

    def compileWaitTime(self, timeValue):
        # Wait for <n> seconds
        if logLevel > 2: print '  {} - time value = {}'.format(self.threadName, timeValue)
        try:
            num = float(timeValue)
        except ValueError:
            self.compileMessages.append('{} - Wait time error at line {}: the wait time, {}, is not a number'.format(self.threadName, self.lineNumber, timeValue))
        else:
            self.actionTokens.append(['WaitTime', int(num * 1000)])

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
        if logLevel > 2: print 'WaitMast', mastName, aspectNames, notOption
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
        self.mSensor = sensors.getSensor(masterSensor)
        if self.mSensor is None:
            return False
        self.mSensor.setKnownState(INACTIVE)
        return True

    def handle(self):
        self.waitSensorActive(self.mSensor)
        for thread in instanceList:
            if thread is not None:
                if thread.isRunning():
                    if logLevel > 0: print 'Stop "{}" thread'.format(thread.getName())
                    thread.stop()
        return False;

# End of class YAATMaster

print 'YAAT v1.6'

# Process text file train definitions.
instanceList = []   # List of file based instances
for trainName, fileName in trainList.iteritems():
    trainLines = []
    fullName = fileName
    if jmri.util.FileUtil.isPortableFilename(fileName):
        fullName = jmri.util.FileUtil.getExternalFilename(fileName)
    with open(fullName) as file:
        trainLines = [line.strip() for line in file]
    idx = len(instanceList)
    instanceList.append(YetAnotherAutoTrain())  # Add a new instance
    instanceList[idx].setName(trainName)        # Set the instance name
    if instanceList[idx].setup(trainLines):     # Compile the train actions
        instanceList[idx].start()               # Compile was successful

# Process embedded train definitions
# Repeat for each embedded definition

idx = len(instanceList)
instanceList.append(YetAnotherAutoTrain())  # Add a new instance
instanceList[idx].setName('Back and Forth') # <<<< this is the name of the Python list
if instanceList[idx].setup(BackAndForth):   # <<<< Compile the train actions using the embedded list
    instanceList[idx].start()               # Compile was successful

# Keep last -- create the master thread
master = YAATMaster()
if master.setup():
    master.setName('YAAT Master')
    master.start()
