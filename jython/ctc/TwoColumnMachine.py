# Configure a two-column CTC machine.
#
# Uses only internal Sensors and Turnouts so it can be run as a sample.
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution
import jmri
from jmri.jmrit.ussctc import *

# Define objects on layout

# Define objects on panel
turnouts.provideTurnout("IT1").setUserName("Code Sequencer Start")

turnouts.provideTurnout("IT11").setUserName("Sta 1 Layout TO")
turnouts.provideTurnout("IT12").setUserName("Sta 1 Code")
sensors.provideSensor  ("IS12").setUserName("Sta 1 Code")
turnouts.provideTurnout("IT13").setUserName("Sta 1 TO 1 N")
sensors.provideSensor  ("IS13").setUserName("Sta 1 TO 1 N")
turnouts.provideTurnout("IT13").setUserName("Sta 1 TO 1 R")
sensors.provideSensor  ("IS13").setUserName("Sta 1 TO 1 R")

turnouts.provideTurnout("IT21").setUserName("Sta 2 Layout TO")
turnouts.provideTurnout("IT22").setUserName("Sta 2 Code")
sensors.provideSensor  ("IS22").setUserName("Sta 2 Code")
turnouts.provideTurnout("IT23").setUserName("Sta 2 TO 3 N")
sensors.provideSensor  ("IS23").setUserName("Sta 2 TO 3 N")
turnouts.provideTurnout("IT23").setUserName("Sta 2 TO 3 R")
sensors.provideSensor  ("IS23").setUserName("Sta 2 TO 3 R")

# The code line is shared by all Stations

codeline = CodeLine("Code Sequencer Start", "IT101", "IT102", "IT103", "IT104")

# Set up Station 1

button1 = CodeButton("Sta 1 Code", "Sta 1 Code")
turnout1 = TurnoutSection("Sta 1 Layout TO", "Sta 1 TO 1 N", "Sta 1 TO 1 R", "Sta 1 TO 1 N", "Sta 1 TO 1 R", codeline)
station1 = Station(codeline, button1)
station1.add(turnout1)
button1.addStation(station1)
turnout1.addStation(station1)

# Set up Station 2

button2 = CodeButton("Sta 2 Code", "Sta 2 Code")
turnout2 = TurnoutSection("Sta 2 Layout TO", "Sta 2 TO 3 N", "Sta 2 TO 3 R", "Sta 2 TO 3 N", "Sta 2 TO 3 R", codeline)
station2 = Station(codeline, button2)
station2.add(turnout2)
button2.addStation(station2)
turnout2.addStation(station2)
      
