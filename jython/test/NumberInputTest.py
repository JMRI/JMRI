# Test the NumberInput.py script
import jmri

execfile("jython/NumberInput.py")

sensors.provideSensor("IS13").state = ACTIVE
sensors.provideSensor("IS13").state = INACTIVE

sensors.provideSensor("IS14").state = ACTIVE
sensors.provideSensor("IS14").state = INACTIVE

if (memories.provideMemory("IM1").value != "34") : raise AssertionError('Did not set memory')
