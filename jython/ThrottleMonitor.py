##################################################################################
# Link throttles to memory variables in order to provide throttle monitoring.
# This will permit any client to be informed of throttle status change.
#
# Currently, throttle monitoring is not possible using web sockets or XMLIO.
# With XMLIO, throttle status can be read only using polling.
# Memory variables can be monitored using web sockets.
#
# Memory variables name syntaxe:
# IM#THROTTLE:NEW_THROTTLE_ADDRESS
# IM#THROTTLE:<dcc address>_SPEED
# IM#THROTTLE:<dcc address>_FORWARD
# IM#THROTTLE:<dcc address>_F<0-28>
#
# Examples for JSON through web sockets:
# Initiate a throttle for address 3:
# {"type":"memory","data":{"name":"IM#THROTTLE:NEW_THROTTLE_ADDRESS","value":"3"}}
# Set speed at 20% for address 3 (and monitor it):
# {"type":"memory","data":{"name":"IM#THROTTLE:3_SPEED","value":"0.2"}}
# Emergency stop for address 1001 (and monitor speed change):
# {"type":"memory","data":{"name":"IM#THROTTLE:1001_SPEED","value":"-1"}}
# Set reverse direction for address 3 (and monitor it):
# {"type":"memory","data":{"name":"IM#THROTTLE:3_FORWARD","value":"0"}}
# Set light ON for address 3 (and monitor it):
# {"type":"memory","data":{"name":"IM#THROTTLE:3_F0","value":"1"}}
# Get F5 status for address 3  (and monitor it):
# {"type":"memory","data":{"name":"IM#THROTTLE:3_F5"}}
#
# Possible responses for throttle initiation (example with address 3):
# {"type":"memory","data":{"name":"IM#THROTTLE:NEW_THROTTLE_ADDRESS","value":"3-OK"}}
# {"type":"memory","data":{"name":"IM#THROTTLE:NEW_THROTTLE_ADDRESS","value":"3-NOK"}}
# {"type":"memory","data":{"name":"IM#THROTTLE:NEW_THROTTLE_ADDRESS","value":""}}
# (only the first one means that the throttle was assigned and is ready for use)
# (the empty value is the standard between actions)
#
# The responses for throttle properties are the current values (example for F0):
# {"type":"memory","data":{"name":"IM#THROTTLE:3_F0","value":"1"}}
#
# JSON error returned if memory variable doesn't exists:
# {"type":"error","data":{"code":404,"message":"Unable to access memory <mem.var.sysname>."}}
#
# The following response means this script was not loaded (or this mem.var. was deleted):
#{"type":"error","data":{"code":404,"message":"Unable to access memory IM#THROTTLE:NEW_THROTTLE_ADDRESS."}}
#
# If someone deletes the memory variables created by this script, restart JMRI and reload the script.
# (all the system names of these memory variables start with: IM#THROTTLE:)
#
# Version 1.0
#
# Author: Oscar Moutinho, copyright 2013
##################################################################################

import jmri

#*********************************************************************************
# module level: global variables and helper global functions

ThrottleMonitor_debugActive = False	# to debug (print) or not to debug - it's user's choice

ThrottleMonitor_intLoopWait = 500	# wait in handle loop (ms) - check for new throttle address to create

ThrottleMonitor_strMemVarPrefix = "IM#THROTTLE:"
ThrottleMonitor_strMemVar_NewThrottleAddress = "NEW_THROTTLE_ADDRESS"
ThrottleMonitor_strMemVar_Speed = "_SPEED"
ThrottleMonitor_strMemVar_Forward = "_FORWARD"
ThrottleMonitor_strMemVar_Functions = "_F"
ThrottleMonitor_strDoNotDelete = "Do not delete"
ThrottleMonitor_strFromThrottle = "From throttle"
ThrottleMonitor_strThrottleEmpty = ""
ThrottleMonitor_strThrottleOK = "-OK"
ThrottleMonitor_strThrottleNotOK = "-NOK"

# execute if in debug (open Script Output panel)
if (ThrottleMonitor_debugActive == True):
	jmri.jmrit.jython.JythonWindow().actionPerformed(None)

# execute if in debug
def ThrottleMonitor_debugPrint(text):
	if (ThrottleMonitor_debugActive == True):
		print text
	return

# return integer (None if impossible)
def ThrottleMonitor_getInt(inputVar):
	var = str(inputVar).strip()
	try:
		var = int(var)
	except ValueError:
		var = None
	return var

# return float (None if impossible)
def ThrottleMonitor_getFloat(inputVar):
	var = str(inputVar).strip()
	try:
		var = float(var)
	except ValueError:
		var = None
	return var

# return boolean (None if impossible)
def ThrottleMonitor_getBool(inputVar):
	var = str(inputVar).strip()
	if (var == "0" or var == "false"):
		var = False
	elif (var == "1" or var == "true"):
		var = True
	else:		
		var = None
	return var

#*********************************************************************************
# define class to manage throttles monitoring
class ThrottleMonitor(jmri.jmrit.automat.AbstractAutomaton):

	#-----------------------------------------------------------------------------
	# methods

	# init() runs only once
	def init(self):
		ThrottleMonitor_debugPrint("ThrottleMonitor:init start")
		# create the memory variable that identifies this script
		memVar = memories.newMemory(ThrottleMonitor_strMemVarPrefix + ThrottleMonitor_strMemVar_NewThrottleAddress, None)
		memVar.comment = ThrottleMonitor_strDoNotDelete
		memVar.value = ""
		ThrottleMonitor_debugPrint("ThrottleMonitor:init end")
		return

	# handle() runs in a loop (if 'return True')
	def handle(self):
		self.waitMsec(ThrottleMonitor_intLoopWait)
		global ThrottleMonitor_throttles
		memVarName = ThrottleMonitor_strMemVarPrefix + ThrottleMonitor_strMemVar_NewThrottleAddress
		memVar_NewThrottleAddress = memories.getBySystemName(memVarName)
		throttleAddress = ThrottleMonitor_getInt(memVar_NewThrottleAddress.value)
		if (throttleAddress != None and (throttleAddress >= 0 and throttleAddress <= 9999)):
			ThrottleMonitor_debugPrint("THROTTLEMONITOR_Handle:valueChange start")
			ThrottleMonitor_debugPrint("THROTTLEMONITOR_Handle: throttles=" + str(ThrottleMonitor_throttles))
			ThrottleMonitor_debugPrint("THROTTLEMONITOR...Throttle Address (cleaned): " + str(throttleAddress))
			# create/assign a throttle
			varNameThrottle = ThrottleMonitor_strMemVarPrefix + str(throttleAddress)
			throttle = ThrottleMonitor_throttles.get(varNameThrottle)
			ThrottleMonitor_debugPrint("THROTTLEMONITOR...Throttle: " + str(throttle))
			if (throttle == None):
				# try to get new throttle
				throttle = self.getThrottle(throttleAddress, (throttleAddress > 127))	# Lond Address is > 127
				if (throttle != None):
					# attach listener to it
					throttle.addPropertyChangeListener(REALTHROTTLE_Listener())
					ThrottleMonitor_debugPrint("THROTTLEMONITOR...Listener attached to throttle with address " + str(throttleAddress))
					# store new throttle
					ThrottleMonitor_throttles[varNameThrottle] = throttle
					# create/assign the memory variables for each throttle property
					# IM#THROTTLE:<dcc address>_SPEED
					memVarName = varNameThrottle + ThrottleMonitor_strMemVar_Speed
					memVar = memories.newMemory(memVarName, None)
					memVar.comment = ThrottleMonitor_strDoNotDelete
					memVar.value = str(throttle.getSpeedSetting())
					# attach listener (only one) to it
					memVar.addPropertyChangeListener(THROTTLECHANGE_Listener())
					ThrottleMonitor_debugPrint("THROTTLEMONITOR...Listener attached to " + memVarName  + " memory variable")
					# IM#THROTTLE:<dcc address>_FORWARD
					memVarName = varNameThrottle + ThrottleMonitor_strMemVar_Forward
					memVar = memories.newMemory(memVarName, None)
					memVar.comment = ThrottleMonitor_strDoNotDelete
					memVar.value = str(throttle.getIsForward())
					# attach listener (only one) to it
					memVar.addPropertyChangeListener(THROTTLECHANGE_Listener())
					ThrottleMonitor_debugPrint("THROTTLEMONITOR...Listener attached to " + memVarName  + " memory variable")
					# IM#THROTTLE:<dcc address>_F<0-28>
					for i in range(29):	# 0-28
						memVarName = varNameThrottle + ThrottleMonitor_strMemVar_Functions + str(i)
						memVar = memories.newMemory(memVarName, None)
						memVar.comment = ThrottleMonitor_strDoNotDelete
						exec("memVar.value = str(throttle.getF" + str(i) + "())")
						# attach listener (only one) to it
						memVar.addPropertyChangeListener(THROTTLECHANGE_Listener())
						ThrottleMonitor_debugPrint("THROTTLEMONITOR...Listener attached to " + memVarName  + " memory variable")
					# Mark the Throttle Address memory variable as OK (new throttle assigned)
					memVar_NewThrottleAddress.value = str(memVar_NewThrottleAddress.value) + ThrottleMonitor_strThrottleOK
					ThrottleMonitor_debugPrint("THROTTLEMONITOR_Handle: throttles=" + str(ThrottleMonitor_throttles))
				else:
					# Mark the Throttle Address memory variable as NOT OK (could not assign throttle)
					memVar_NewThrottleAddress.value = str(memVar_NewThrottleAddress.value) + ThrottleMonitor_strThrottleNotOK
					ThrottleMonitor_debugPrint("THROTTLEMONITOR...Couldn't assign throttle with address " + str(throttleAddress))
			else:
				# Mark the Throttle Address memory variable as OK (throttle already assigned)
				memVar_NewThrottleAddress.value = str(memVar_NewThrottleAddress.value) + ThrottleMonitor_strThrottleOK
			ThrottleMonitor_debugPrint("THROTTLEMONITOR_Handle:valueChange end")
		else:
			# Clean the Throttle Address memory variable if needed
			if (memVar_NewThrottleAddress.value != ThrottleMonitor_strThrottleEmpty):
				memVar_NewThrottleAddress.value = ThrottleMonitor_strThrottleEmpty
		return True   # continue looping

#*********************************************************************************
# define the listener class to apply to throttle the new value from IM#THROTTLE:<dcc address>_<property> memory variable when it changes
class THROTTLECHANGE_Listener(java.beans.PropertyChangeListener):

	#-----------------------------------------------------------------------------
	# methods

    def propertyChange(self, event):
		ThrottleMonitor_debugPrint("THROTTLECHANGE_Listener:propertyChange start")
		ThrottleMonitor_debugPrint("THROTTLECHANGE_Listener: property=" + event.propertyName + " oldValue=" + str(event.oldValue) + " newValue=" + str(event.newValue))
		if (event.propertyName == "value" and event.getSource().comment != ThrottleMonitor_strFromThrottle):
			memVarName = event.getSource().getSystemName()
			ThrottleMonitor_debugPrint("THROTTLECHANGE...Memory Variable: " + memVarName)
			if (memVarName[:12] == ThrottleMonitor_strMemVarPrefix):
				i = memVarName.index("_")
				varThrottleAddress = memVarName[12:i]
				ThrottleMonitor_debugPrint("THROTTLECHANGE...Throttle Address: " + varThrottleAddress)
				varNameThrottle = ThrottleMonitor_strMemVarPrefix + varThrottleAddress
				throttle = ThrottleMonitor_throttles.get(varNameThrottle)
				ThrottleMonitor_debugPrint("THROTTLECHANGE...Throttle: " + str(throttle))
				if (memVarName[-6:] == ThrottleMonitor_strMemVar_Speed):
					ThrottleMonitor_debugPrint("THROTTLECHANGE...Speed (uncleaned): " + str(event.newValue))
					memVarNewValue = ThrottleMonitor_getFloat(event.newValue)
					ThrottleMonitor_debugPrint("THROTTLECHANGE...Speed (cleaned): " + str(memVarNewValue))
					if (memVarNewValue == None or (memVarNewValue != -1 and (memVarNewValue < 0 or memVarNewValue > 1))):
						# restore memory variable value if invalid speed
						event.getSource().value = event.oldValue
					else:
						# set new speed for the throttle
						ThrottleMonitor_debugPrint("THROTTLECHANGE...Set Speed to " + str(memVarNewValue) + " for throttle " + varThrottleAddress)
						throttle.setSpeedSetting(memVarNewValue)
				elif (memVarName[-8:] == ThrottleMonitor_strMemVar_Forward):
					ThrottleMonitor_debugPrint("THROTTLECHANGE...Forward (uncleaned): " + str(event.newValue))
					memVarNewValue = ThrottleMonitor_getBool(event.newValue)
					ThrottleMonitor_debugPrint("THROTTLECHANGE...Forward (cleaned): " + str(memVarNewValue))
					if (memVarNewValue == None):
						# restore memory variable value if invalid boolean
						event.getSource().value = event.oldValue
					else:
						# set new direction for the throttle
						ThrottleMonitor_debugPrint("THROTTLECHANGE...Set Forward to " + str(memVarNewValue) + " for throttle " + varThrottleAddress)
						throttle.setIsForward(memVarNewValue)
				elif (memVarName[i:i + 2] == ThrottleMonitor_strMemVar_Functions):
					s = memVarName[i + 2:]
					ThrottleMonitor_debugPrint("THROTTLECHANGE...F" + s + " (uncleaned): " + str(event.newValue))
					memVarNewValue = ThrottleMonitor_getBool(event.newValue)
					ThrottleMonitor_debugPrint("THROTTLECHANGE...F" + s + " (cleaned): " + str(memVarNewValue))
					if (memVarNewValue == None):
						# restore memory variable value if invalid boolean
						event.getSource().value = event.oldValue
					else:
						# set new function value for the throttle
						ThrottleMonitor_debugPrint("THROTTLECHANGE...Set F" + s + " to " + str(memVarNewValue) + " for throttle " + varThrottleAddress)
						exec("throttle.setF" + s + "(memVarNewValue)")
		elif (event.propertyName == "value"):
			event.getSource().comment = ThrottleMonitor_strDoNotDelete
		ThrottleMonitor_debugPrint("THROTTLECHANGE_Listener:propertyChange end")
		return

#*********************************************************************************
# define the listener class to serve throttle changes
class REALTHROTTLE_Listener(java.beans.PropertyChangeListener):

	#-----------------------------------------------------------------------------
	# methods

    def propertyChange(self, event):
		ThrottleMonitor_debugPrint("REALTHROTTLE_Listener:propertyChange start")
		throttle = event.getSource()
		ThrottleMonitor_debugPrint("REALTHROTTLE...Throttle: " + str(throttle))
		throttleAddress = throttle.getLocoAddress().getNumber()
		ThrottleMonitor_debugPrint("REALTHROTTLE...Throttle Address: " + str(throttleAddress))
		varNameThrottle = ThrottleMonitor_strMemVarPrefix + str(throttleAddress)
		# write each different throttle property to their memory variable
		# IM#THROTTLE:<dcc address>_SPEED
		memVarName = varNameThrottle + ThrottleMonitor_strMemVar_Speed
		memVar = memories.getBySystemName(memVarName)
		if (memVar != None):
			value = str(throttle.getSpeedSetting())
			if (value != memVar.value):
				ThrottleMonitor_debugPrint("REALTHROTTLE...Get Speed status " + value + " for throttle " + str(throttleAddress))
				memVar.comment = ThrottleMonitor_strFromThrottle
				memVar.value = value
		# IM#THROTTLE:<dcc address>_FORWARD
		memVarName = varNameThrottle + ThrottleMonitor_strMemVar_Forward
		memVar = memories.getBySystemName(memVarName)
		if (memVar != None):
			value = str(throttle.getIsForward())
			if (value != memVar.value):
				ThrottleMonitor_debugPrint("REALTHROTTLE...Get Forward status " + value + " for throttle " + str(throttleAddress))
				memVar.comment = ThrottleMonitor_strFromThrottle
				memVar.value = value
		# IM#THROTTLE:<dcc address>_F<0-28>
		for i in range(29):	# 0-28
			memVarName = varNameThrottle + ThrottleMonitor_strMemVar_Functions + str(i)
			memVar = memories.getBySystemName(memVarName)
			if (memVar != None):
				exec("value = str(throttle.getF" + str(i) + "())")
				if (value != memVar.value):
					ThrottleMonitor_debugPrint("REALTHROTTLE...Get F" + str(i) + " status " + value + " for throttle " + str(throttleAddress))
					memVar.comment = ThrottleMonitor_strFromThrottle
					memVar.value = value
		ThrottleMonitor_debugPrint("REALTHROTTLE_Listener:propertyChange end")
		return

#*********************************************************************************

if (globals().get("ThrottleMonitor_throttles") != None):
	# Script already loaded so exit script
	ThrottleMonitor_debugPrint("Script already loaded and running")
else:
	# start this script
	ThrottleMonitor_throttles = {}	# throttles in use (create and initialize only if first time loading)
	ThrottleMonitor_debugPrint("ThrottleMonitor is going to start")
	ThrottleMonitor().start()
