# Put a speedometer on scree

s = jmri.jmrit.speedometer.SpeedometerFrame()
s.setInputs("LS150", "LS151", "LS152", "212", "425")
s.setLocation(1000, 917)
s.show()
s.setup()
