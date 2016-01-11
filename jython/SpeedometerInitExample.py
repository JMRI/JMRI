# Sample script showing how to open the Speedometer frame and populate
# with sensor and distance information.
#
# Author: Matthew Harris, copyright 2011
# Part of the JMRI distribution
#

import jmri

from jmri.jmrit.speedometer import SpeedometerFrame

# Define the sensors
startSensor = "IS1"
stopSensor1 = "IS2"
stopSensor2 = "IS3"

# Define the distances
distance1 = "100"
distance2 = "110"

# Define sensor change parameter
startSensorExit = False
stopSensor1Exit = True
stopSensor2Exit = True

# Define units
isMetric = False

# Normally, nothing should be changed below this line

# Create a SpeedometerFrame instance
sf = SpeedometerFrame()

# Set the sensors and distances
sf.setInputs(startSensor, stopSensor1, stopSensor2, distance1, distance2)

# Set the sensor change parameters
# Done this way round as setInputBehavior requires the inverse of our parameter
# settings.
# Useful as an example of how to achieve boolean inversion.
sf.setInputBehavior((startSensorExit==False), (stopSensor1Exit==False), (stopSensor2Exit==False))

# Set unit parameters
sf.setUnitsMetric(isMetric)

# Finally, show on screen
sf.setVisible(True)
