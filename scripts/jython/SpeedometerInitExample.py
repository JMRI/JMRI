# Sample script showing how to open the Speedometer frame and populate
# with sensor and distance information.
#
# Author: Matthew Harris, copyright 2011
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

from jmri.jmrit.speedometer import SpeedometerFrame
from javax.swing import JPanel, JTextField, JRadioButton, JButton

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

# Set other parameters
#
# This is a rather 'hacky' way of doing it as the SpeedometerFrame object
# does not publicly expose these settings so we need to directly manipulate
# the components in the frame - yuck!!
#
# Loop through components...
for comp in sf.contentPane.components:
    temp = JPanel()
    sensor = ""
    # Search for a JPanel
    if comp.getClass() == temp.getClass():
        # Loop through components in this panel
        for sComp in comp.components:
            # Search for a JTextField
            temp = JTextField()
            if sComp.getClass() == temp.getClass():
                # Found JTextField - now grab the contents
                sensor = sComp.getText()
            # Search for a JRadioButton
            temp = JRadioButton()
            if (sComp.getClass() == temp.getClass()):
                # Found JRadioButton - is it the 'exit' one?
                if (sComp.getText() == "exit"):
                    # Yes, so set it if required
                    if sensor == startSensor:
                        sComp.setSelected(startSensorExit)
                    elif sensor == stopSensor1:
                        sComp.setSelected(stopSensor1Exit)
                    elif sensor == stopSensor2:
                        sComp.setSelected(stopSensor2Exit)
            # Search for a JButton
            temp = JButton()
            if sComp.getClass() == temp.getClass():
                # Found JButton - is it the 'To metric units' one?
                if sComp.getText() == "To metric units":
                    # Yes, so activate the JButton if necessary
                    if isMetric==True:
                        sComp.doClick()

# Finally, show on screen
sf.setVisible(True)