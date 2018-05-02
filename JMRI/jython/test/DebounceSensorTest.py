# Test the DebounceSensor.py script

execfile("jython/DebounceSensor.py")

a = DebounceSensor() # create one of these 
a.init('IS775', 'IS2775', 0.1, 1) # invoke this for the sensor pair

# don't actually check functionality, just that we can create it and it matches sensors
if (sensors.getSensor('IS775') == None) : raise AssertionError('IS775 not created')
if (sensors.getSensor('IS2775') == None) : raise AssertionError('IS2775 not created')
