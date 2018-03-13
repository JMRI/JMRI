# YetAnotherAutoTrain.py -- Data driven automatic train
# Use a list of actions to automatically run a train.
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
# Wait for <n> seconds
#       Wait until the time has expired.  Normally used for station stops.
# Wait for sensor <sensor name> to become <active | inactive>
# Wait for block <block name> to become <occupied | unoccupied | reserved | free>
# Stop if sensor <sensor name> is <active | inactive>
#       This action needs to be the last one in the list.
#       If the sensor state matches, the throttle will be released and the script stopped.
#       If it does not, the script will do the sequence of actions again.
#       If the Stop action is missing, the script will run forever, until the script is killed, or JMRI is stopped.

# -- If / Else / Endif support --
#   The "If" and "Endif" actions are required.  The "Else" action is optional and is used to separate the true and false actions.
# If sensor <sensor name> is <active | inactive>
# If block <block name> is <occupied | unoccupied | reserved | free>
# Else
# Endif

# Usage
#   Copy the script from the JMRI install location to your user files location.  See "Help >> Locations".
#   Change the log level from 0 to 1 through 4 if log output is desired.  4 provides maximun detail.
#   Enter a valid sensor name in the statusSensor variable.  This is used to provide feedback to JMRI.
#   Define the actions for a train.  The actions can be embedded in the script or placed in an external file.
#      External file:  Create a text file with one action per line.  Blank lines and lines starting with a comment character, #, are ok.
#                      Add the train name and file name to the "fileList".  The file name can be the complete
#                      path or the file name can include a keyword for the location, such as "preference:"
#                      which is replaced by the path to the user files location at run time.
#      Embedded: The actions are added to a Python list.  Each action is enclosed in single or double quotes
#                and end with a comma at the end of the line.  The embedded method requires a unique block
#                of code at the end of the script for each embedded action list.

import java
import jmri
import re
from javax.swing import JOptionPane

logLevel = 4    # 0 for no output, 4 for the most detail.
statusSensor = 'Run Script'

trainList = {}
trainList['Train 12'] = 'preference:Train 12.txt'
trainList['Train 16'] = 'preference:Train 16.txt'
trainList['RT Train 1'] = 'preference:RT Train 1.txt'
trainList['RT Train 2'] = 'preference:RT Train 2.txt'

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

## Set APB sensors
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

'Stop if sensor BF-Stop is active',
]

class YetAnotherAutoTrain(jmri.jmrit.automat.AbstractAutomaton):
    threadCount = 0

    def init(self):
        self.throttle = None
        self.TrueFalseBlock = '- none -'
        self.TrueFalseState = True
        YetAnotherAutoTrain.threadCount += 1

    def setup(self, actionList):
        self.actionTokens = []
        self.compileMessages = []
        self.compile(actionList)
        if len(self.compileMessages) > 1:
            self.displayMessage("\n".join(self.compileMessages))
            YetAnotherAutoTrain.threadCount -= 1
            if YetAnotherAutoTrain.threadCount < 1:
                sensors.getSensor(statusSensor).setKnownState(INACTIVE)
            return False
        return True

    def handle(self):
        if (self.throttle is None):
            if logLevel > 0: print '>> Start YAAT <<'

        runShuttle = True
        if logLevel > 1: print 'Start YAAT Loop'
        for action in self.actionTokens:
            if len(action) == 0:
                self.displayMessage('Empty Action row')
                continue

            if logLevel > 2: print '-- Action: {}'.format(action)
            actionKey = action[0]

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
            elif actionKey == 'SetBlock':
                self.doSetBlock(action)
            elif actionKey == 'SetDirection':
                self.doSetDirection(action)
            elif actionKey == 'SetFKey':
                self.doSetFKey(action)
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
                    if logLevel > 0: print '>> Stop YAAT <<'
                    runShuttle = False
                    YetAnotherAutoTrain.threadCount -= 1
                    if YetAnotherAutoTrain.threadCount == 0:
                        sensors.getSensor(statusSensor).setKnownState(INACTIVE)
                    break;
            elif actionKey == 'WaitBlock':
                self.doWaitBlock(action)
            elif actionKey == 'WaitSensor':
                self.doWaitSensor(action)
            elif actionKey == 'WaitTime':
                self.waitMsec(action[1])    # Direct execution
            else:
                self.displayMessage('Action, {}, is not valid'.format(actionKey))
        if logLevel > 1: print 'End YAAT Loop'
        self.TrueFalseBlock = '- none -'    # Force Endif
        return runShuttle


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
            self.displayMessage('DCC address length, {}, is not valid'.format(addrType))
            return
        self.throttle = self.getThrottle(dccAddress, dccLong)
        if self.throttle == None:
            self.displayMessage('Unable to assign a throttle.\nCheck the system log for errors.\nScript stopping.')
            self.stop()
        if trainName != '' and startBlock != '':
            layoutBlock = layoutblocks.getLayoutBlock(startBlock)
            if layoutBlock is not None:
                layoutBlock.getBlock().setValue(trainName)

    def doIfBlock(self, action):
        act, blockName, blockState = action
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.displayMessage('Layout block {} not found'.format(blockName))
            return
        sensor = layoutBlock.getOccupancySensor()
        if sensor is None:
            self.displayMessage('Sensor for layout block {} not found'.format(blockName))
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
            self.displayMessage('block state, {}, is not valid'.format(blockState))
            return
        self.TrueFalseBlock = '- true -'
        self.TrueFalseState = currentState

    def doIfSensor(self, action):
        act, sensorName, sensorState = action
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('Sensor {} not found'.format(sensorName))
            return
        if sensorState == 'active':
            checkState = ACTIVE
        elif sensorState == 'inactive':
            checkState = INACTIVE
        else:
            self.displayMessage('Sensor state, {}, is not valid'.format(sensorState))
            return

        knownState = sensor.getKnownState()
        self.TrueFalseBlock = '- true -'
        self.TrueFalseState = checkState == knownState

    def doSetBlock(self, action):
        act, blockName, blockState = action
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.displayMessage('Layout block {} not found'.format(blockName))
            return
        if blockState in ['occupied', 'unoccupied']:
            sensor = layoutBlock.getOccupancySensor()
            if sensor is None:
                self.displayMessage('Sensor for layout block {} not found'.format(blockName))
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
            self.displayMessage('Cannot set a direction until a throttle has been assigned')
            return
        act, direction = action
        if direction == 'forward':
            dirForward = True
        elif direction == 'reverse':
            dirForward = False
        else:
            self.displayMessage('Direction value, {}, is not valid'.format(direction))
            return
        self.throttle.setIsForward(dirForward)

    def doSetFKey(self, action):
        if self.throttle == None:
            self.displayMessage('Cannot set a function key until a throttle has been assigned')
            return
        act, keyNum, keyState, keyDuration = action
        if keyState == 'on':
            keyOn = True
            keyOff = False
        elif keyState == 'off':
            keyOn = False
            keyOff = True
        else:
            self.displayMessage('Key state value, {}, is not valid'.format(keyState))
            return
        if keyDuration == 0:
            self.setKey(keyNum, keyOn)
        else:
            self.setKey(keyNum, keyOn)
            self.waitMsec(keyDuration)
            self.setKey(keyNum, keyOff)

    def doSetSensor(self, action):
        act, sensorName, sensorState = action
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('Sensor {} not found'.format(sensorName))
            return
        if sensorState == 'active':
            newState = ACTIVE
        elif sensorState == 'inactive':
            newState = INACTIVE
        else:
            self.displayMessage('Sensor state, {}, is not valid'.format(sensorState))
            return
        sensor.setKnownState(newState)

    def doSetSpeed(self, action):
        if self.throttle == None:
            self.displayMessage('Cannot set the speed until a throttle has been assigned')
            return
        act, newSpeed = action
        self.throttle.setSpeedSetting(newSpeed)

    def doSetTurnout(self, action):
        act, turnoutName, turnoutState, turnoutDelay = action
        turnout = turnouts.getTurnout(turnoutName)
        if turnout is None:
            self.displayMessage('Turnout {} not found'.format(turnoutName))
            return
        if turnoutState == 'closed':
            newState = CLOSED
        elif turnoutState == 'thrown':
            newState = THROWN
        else:
            self.displayMessage('Turnout state, {}, is not valid'.format(turnoutState))
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
            self.displayMessage('Start sensor state, {}, is not valid'.format(startState))

    def doStop(self, action):
        act, stopName, stopState = action
        sensor = sensors.getSensor(stopName)
        if stopState == 'active':
            chkState = ACTIVE
        elif stopState == 'inactive':
            chkState = INACTIVE
        else:
            self.displayMessage('Stop sensor state, {}, is not valid'.format(stopState))
            return
        if sensor.getKnownState() == chkState:
            # Release throttle
            if self.throttle is not None:
                self.throttle.release()
            return False
        return True

    def doWaitBlock(self, action):
        act, blockName, blockState = action
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.displayMessage('Layout block {} not found'.format(blockName))
            return
        if blockState in ['occupied', 'unoccupied']:
            # Block sensor changes limited to simulation mode
            sensor = layoutBlock.getOccupancySensor()
            if sensor is None:
                self.displayMessage('Sensor for layout block {} not found'.format(blockName))
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
            self.displayMessage('Sensor {} not found'.format(sensorName))
            return
        if sensorState == 'active':
            self.waitSensorActive(sensor)
        elif sensorState == 'inactive':
            self.waitSensorInactive(sensor)
        else:
            self.displayMessage('Sensor state, {}, is not valid'.format(sensorState))

    def displayMessage(self, msg):
        JOptionPane.showMessageDialog(None, msg, 'YAAT Error', JOptionPane.WARNING_MESSAGE)

    def setKey(self, keyNum, keyOn):
        if logLevel > 2: print "Function key = {}, On = {}".format(keyNum, keyOn)
        command = 'self.throttle.setF' + str(keyNum)
        if keyOn:
            command += '(True)'
        else:
            command += '(False)'
        exec(command)


    # ------ Convert the text phrases to tokens ------
    def compile(self, actionList):
        self.compileMessages.append('---- Compiler Errors ----')
        for line in actionList:
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
            elif words[0] == 'Set' and words[1] == 'block':
                self.compileSetBlock(line)
            elif words[0] == 'Set' and words[1] == 'direction':
                self.compileSetDirection(line)
            elif words[0] == 'Set' and words[1] == 'function':
                self.compileSetFKey(line)
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
            elif words[0] == 'Wait' and words[1] == 'for' and (words[3] == 'seconds' or words[3] == 'second'):
                self.compileWaitTime(words[2])
            else:
                self.compileMessages.append('Syntax error: line = {}'.format(line))

    def compileAssign(self, line):
        # Assign <long | short> address <dccaddr> [[ as <train name>] in <blockname>]
        if logLevel > 2: print '  {}'.format(line)
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
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != flds:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        grps = result[0]
        addrSize = grps[0]
        try:
            num = int(grps[1])
        except ValueError:
            self.compileMessages.append('Assign error: the DCC address, {}, is not a number'.format(grps[1]))
            return
        addrNum = num
        trainName = '' if flds < 3 else grps[2]
        blockName = '' if flds < 4 else grps[3]
        if blockName != '':
            layoutBlock = layoutblocks.getLayoutBlock(blockName)
            if layoutBlock is None:
                self.compileMessages.append('Assign error: start block "{}" does not exist'.format(blockName))
                return
        self.actionTokens.append(['Assign', addrNum, addrSize, trainName, blockName])

    def compileIfBlock(self, line):
        # If block <block name> is <occupied | unoccupied | reserved | free>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*If\s+block\s+(.+\S)\s+is\s+(occupied|unoccupied|reserved|free)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        blockName, blockState = result[0]
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.compileMessages.append('Block error: block {} not found'.format(blockName))
            return
        if layoutBlock.getOccupancySensor() is None:
            self.compileMessages.append('Block error: occupancy sensor for block {} not found'.format(blockName))
            return
        self.actionTokens.append(['IfBlock', blockName, blockState])

    def compileIfSensor(self, line):
        # If sensor <sensor name> is <active | inactive>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*If\s+sensor\s+(.+\S)\s+is\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('If sensor error: sensor {} not found'.format(sensorName))
            return
        self.actionTokens.append(['IfSensor', sensorName, sensorState])

    def compileSetBlock(self, line):
        # Set block <block name> <occupied | unoccupied | reserved | free>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*Set\s+block\s+(.+\S)\s+(occupied|unoccupied|reserved|free)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        blockName, blockState = result[0]
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.compileMessages.append('Block error: block "{}" not found'.format(blockName))
            return
        if layoutBlock.getOccupancySensor() is None:
            self.compileMessages.append('Block error: occupancy sensor for block {} not found'.format(blockName))
            return
        self.actionTokens.append(['SetBlock', blockName, blockState])

    def compileSetDirection(self, line):
        # Set direction to <forward | reverse>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*Set\s+direction\s+to\s+(forward|reverse)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) != 1:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        self.actionTokens.append(['SetDirection', result[0]])

    def compileSetFKey(self, line):
        # Set function key <n> <on | off>[, wait <n> seconds]
        if logLevel > 2: print '  {}'.format(line)
        words = line.split()
        flds = 2
        regex = '\s*Set\s+function\s+key\s+(\d+)\s+(on|off)'
        if 'wait' in words:
            regex += ', wait (\d+) second'
            flds += 1
        pattern = re.compile(regex)
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != flds:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        grps = result[0]
        try:
            keyNum = int(grps[0])
        except ValueError:
            self.compileMessages.append('Function key error: the key value, {}, is not an integer'.format(grps[0]))
            return
        else:
            if keyNum < 0 or keyNum > 28:
                self.compileMessages.append('Function key error: the key value, {}, is not in the range 0-28'.format(grps[0]))
                return
        keyState = grps[1]
        if flds ==2:
            keyWait = 0
        else:
            try:
                keyWait = float(grps[2])
            except ValueError:
                self.compileMessages.append('Function key error: the wait time, {}, is not a number'.format(grps[2]))
                return
        self.actionTokens.append(['SetFKey', keyNum, keyState, int(keyWait * 1000)])

    def compileSetSensor(self, line):
        # Set sensor <sensor name> <active | inactive>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*Set\s+sensor\s+(.+\S)\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('Sensor error: sensor "{}" not found'.format(sensorName))
            return
        self.actionTokens.append(['SetSensor', sensorName, sensorState])

    def compileSetSpeed(self, line):
        # Set speed to <0 to 1.0>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*Set\s+speed\s+to\s+(\S+)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) != 1:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        try:
            num = float(result[0])
        except ValueError:
            self.compileMessages.append('Train speed error: the speed, {}, is not a number'.format(result[0]))
        else:
            if num < 0.0:
                num = 0.0
            if num > 1.0:
                num = 1.0
            self.actionTokens.append(['SetSpeed', num])

    def compileSetTurnout(self, line):
        # Set turnout <turnout name> <closed | thrown>[, wait <n> seconds]
        if logLevel > 2: print '  {}'.format(line)
        words = line.split()
        flds = 2
        regex = '\s*Set\s+turnout\s+(.+\S)\s+(closed|thrown)'
        if 'wait' in words:
            regex += ', wait (\d+) second'
            flds += 1
        pattern = re.compile(regex)
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != flds:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        grps = result[0]
        turnoutName = grps[0]
        turnoutState = grps[1]
        turnoutWait = 0 if flds < 3 else grps[2]
        if turnouts.getTurnout(turnoutName) is None:
            self.compileMessages.append('Turnout error: turnout {} not found'.format(grps[0]))
            return
        try:
            num = float(turnoutWait)
        except ValueError:
            self.compileMessages.append('Turnout error: the wait time, {}, is not a number'.format(turnoutWait))
        else:
            self.actionTokens.append(['SetTurnout', turnoutName, turnoutState, int(num * 1000)])

    def compileStart(self, line):
        # Start when sensor <sensor name> is <active | inactive>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*Start\s+when\s+sensor\s+(.+\S)\s+is\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('Start error: sensor {} does not exist'.format(sensorName))
            return
        self.actionTokens.append(['Start', sensorName, sensorState])

    def compileStop(self, line):
        # Stop if sensor <sensor name> is <active | inactive>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*Stop\s+if\s+sensor\s+(.+\S)\s+is\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('Stop error: sensor {} does not exist'.format(sensorName))
            return
        self.actionTokens.append(['Stop', sensorName, sensorState])

    def compileWaitBlock(self, line):
        # Wait for block <block name> to become <occupied | unoccupied | free>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*Wait\s+for\s+block\s+(.+\S)\s+to\s+become\s+(occupied|unoccupied|free)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        blockName, blockState = result[0]
        layoutBlock = layoutblocks.getLayoutBlock(blockName)
        if layoutBlock is None:
            self.compileMessages.append('Wait block error: block {} not found'.format(blockName))
            return
        if layoutBlock.getOccupancySensor() is None:
            self.compileMessages.append('Wait block error: occupancy sensor for block {} not found'.format(blockName))
            return
        self.actionTokens.append(['WaitBlock', blockName, blockState])
        return

    def compileWaitSensor(self, line):
        # Wait for sensor <sensor name> to become <active | inactive>
        if logLevel > 2: print '  {}'.format(line)
        pattern = re.compile('\s*Wait\s+for\s+sensor\s+(.+\S)\s+to\s+become\s+(active|inactive)')
        result = re.findall(pattern, line)
        if logLevel > 3: print '    result = {}'.format(result)
        if len(result) == 0 or len(result[0]) != 2:
            self.compileMessages.append('Syntax error: line = {}'.format(line))
            return
        sensorName, sensorState = result[0]
        if sensors.getSensor(sensorName) is None:
            self.compileMessages.append('Wait sensor error: sensor {} not found'.format(sensorName))
            return
        self.actionTokens.append(['WaitSensor', sensorName, sensorState])

    def compileWaitTime(self, timeValue):
        # Wait for <n> seconds
        if logLevel > 2: print '  time value = {}'.format(timeValue)
        try:
            num = float(timeValue)
        except ValueError:
            self.compileMessages.append('Wait time error: the wait time, {}, is not a number'.format(timeValue))
        else:
            self.actionTokens.append(['WaitTime', int(num * 1000)])

# End of class YetAnotherAutoTrain

print 'YAAT v1.2'

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
yaat1 = YetAnotherAutoTrain()
yaat1.setName('Back and Forth')
if yaat1.setup(BackAndForth):
    yaat1.start()
