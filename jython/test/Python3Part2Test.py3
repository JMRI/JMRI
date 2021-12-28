print ("Python3Part2Test")
# see if same context using settings from 1st script
global pyPersistanceCheck
if (pyPersistanceCheck != 27) : raise AssertionError('Variable not persistant')
