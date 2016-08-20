# Test the PowerSensor.py script

execfile("jython/PowerSensor.py")

# check that power drives sensor 100
powermanager.setPower(jmri.PowerManager.OFF)
if (sensors.getSensor("100").getState() != INACTIVE) : raise AssertionError('Set OFF did not set INACTIVE')

powermanager.setPower(jmri.PowerManager.ON)
if (sensors.getSensor("100").getState() != ACTIVE) : raise AssertionError('Set ON did not set ACTIVE')

powermanager.setPower(jmri.PowerManager.OFF)
if (sensors.getSensor("100").getState() != INACTIVE) : raise AssertionError('2nd set OFF did not set INACTIVE')
