# This is an example script for a JMRI "Siglet" in Python

import jarray
import jmri

# defineIO() is called exactly once at the beginning, and is
# required to load the "inputs" array. Modify this to 
# define all of your turnouts, sensors and signal heads.
def defineIO():
	global to12, bo21, bo22, si35   # so these can be referenced in setOutput
	
	to12 = turnouts.provideTurnout("12")

	bo21 = sensors.provideSensor("21")
	bo22 = sensors.provideSensor("22")

	# signalhead should have been previously defined. Since this
	# is an example, we detect whether it was already defined, and
	# if not we create a particular signal head.
	si35 = signals.getSignalHead("35")
	if (si35 == None) :
		si35 = jmri.TripleTurnoutSignalHead("si35", "", 
					turnouts.provideTurnout("101"),
					turnouts.provideTurnout("102"),
					turnouts.provideTurnout("103"))
		signals.register(si35)
 	
 	# Register the inputs so setOutput will be called when needed.
 	# Note that the output si35 should _not_ in included as an input.
	self.setInputs(jarray.array([to12, bo21, bo22], jmri.NamedBean))

	return

# setOutput is called when one of the inputs changes, and is
# responsible for setting the correct output
def setOutput():
	global to12, bo21, bo22, si35
	newvalue = RED
	
	if to12.commandedState==THROWN:
		if bo21.knownState==INACTIVE:
			newvalue = GREEN
	else:
		if bo22.knownState==INACTIVE:
			newvalue = GREEN
		
	print "output set to ", newvalue
	si35.appearance = newvalue;
	
	return
	
