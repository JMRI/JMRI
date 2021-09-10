# Configure a two-column CTC machine.
#
# Uses only internal Sensors and Turnouts so it can be run as a sample.
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution
import jmri
from jmri.jmrit.ussctc import *

# First, we define Turnouts and Sensors used by this example
# These are normally defined in a panel file with names specific
# to the layout

# Define objects on layout
turnouts.provideTurnout("IT1").setUserName("Code Indication Start")
turnouts.provideTurnout("IT2").setUserName("Code Send Start")
turnouts.provideTurnout("IT3").setUserName("Bell")

sensors.provideSensor("IS101").setUserName("TC Left Approach")
sensors.provideSensor("IS102").setUserName("TC Sta 1 OS")
sensors.provideSensor("IS103").setUserName("TC Main")
sensors.provideSensor("IS104").setUserName("TC Siding")
sensors.provideSensor("IS105").setUserName("TC Sta 2 OS")
sensors.provideSensor("IS106").setUserName("TC Right Approach")

turnouts.provideTurnout("IT11").setUserName("Sta 1 Layout TO")

turnouts.provideTurnout("IT21").setUserName("Sta 2 Layout TO")

# Define objects on panel
turnouts.provideTurnout("IT12").setUserName("Sta 1 Code")
sensors.provideSensor  ("IS12").setUserName("Sta 1 Code")

turnouts.provideTurnout("IT13").setUserName("Sta 1 TO 1 N")
sensors.provideSensor  ("IS13").setUserName("Sta 1 TO 1 N")
sensors.provideSensor  ("IS13").state = ACTIVE
turnouts.provideTurnout("IT14").setUserName("Sta 1 TO 1 R")
sensors.provideSensor  ("IS14").setUserName("Sta 1 TO 1 R")

turnouts.provideTurnout("IT15").setUserName("Sta 1 SI 2 L")
sensors.provideSensor  ("IS15").setUserName("Sta 1 SI 2 L")
turnouts.provideTurnout("IT16").setUserName("Sta 1 SI 2 C")
sensors.provideSensor  ("IS16").setUserName("Sta 1 SI 2 C")
sensors.provideSensor  ("IS16").state = ACTIVE
turnouts.provideTurnout("IT17").setUserName("Sta 1 SI 1 R")
sensors.provideSensor  ("IS17").setUserName("Sta 1 SI 1 R")

turnouts.provideTurnout("IT18").setUserName("Sta 1 Left Approach TC")
turnouts.provideTurnout("IT19").setUserName("Sta 1 OS TC")


turnouts.provideTurnout("IT22").setUserName("Sta 2 Code")
sensors.provideSensor  ("IS22").setUserName("Sta 2 Code")

turnouts.provideTurnout("IT23").setUserName("Sta 2 TO 3 N")
sensors.provideSensor  ("IS23").setUserName("Sta 2 TO 3 N")
sensors.provideSensor  ("IS23").state = ACTIVE
turnouts.provideTurnout("IT24").setUserName("Sta 2 TO 3 R")
sensors.provideSensor  ("IS24").setUserName("Sta 2 TO 3 R")

turnouts.provideTurnout("IT25").setUserName("Sta 2 SI 4 L")
sensors.provideSensor  ("IS25").setUserName("Sta 2 SI 4 L")
turnouts.provideTurnout("IT26").setUserName("Sta 2 SI 4 C")
sensors.provideSensor  ("IS26").setUserName("Sta 2 SI 4 C")
sensors.provideSensor  ("IS26").state = ACTIVE
turnouts.provideTurnout("IT27").setUserName("Sta 2 SI 4 R")
sensors.provideSensor  ("IS27").setUserName("Sta 2 SI 4 R")

turnouts.provideTurnout("IT28").setUserName("Sta 2 Main TC")
turnouts.provideTurnout("IT29").setUserName("Sta 2 Siding TC")
turnouts.provideTurnout("IT30").setUserName("Sta 2 OS TC")
turnouts.provideTurnout("IT31").setUserName("Sta 2 Right Approach TC")

# The core of the sample script starts here, defining & connecting
# the USS CTC objects to run the panel

# The bell and code line are shared by all Stations

bell = PhysicalBell("Bell")
codeline = CodeLine("Code Indication Start", "Code Send Start", "IT101", "IT102", "IT103", "IT104")

# Set up Station 1 - stations are numbered 1, 2, 3 etc.
# Station 1 is levers 1 and 2

station = Station("1", codeline, CodeButton("Sta 1 Code", "Sta 1 Code"))

turnout = TurnoutSection("Sta 1 Layout TO", "Sta 1 TO 1 N", "Sta 1 TO 1 R", "Sta 1 TO 1 N", "Sta 1 TO 1 R", station)
station.add(turnout)

station.add(TrackCircuitSection("TC Left Approach", "Sta 1 Left Approach TC", station, bell))
station.add(TrackCircuitSection("TC Sta 1 OS", "Sta 1 OS TC", station))

# Set up Station 2 - levers 3 and 4

station = Station("2", codeline, CodeButton("Sta 2 Code", "Sta 2 Code"))

station.add(TurnoutSection("Sta 2 Layout TO", "Sta 2 TO 3 N", "Sta 2 TO 3 R", "Sta 2 TO 3 N", "Sta 2 TO 3 R", station))
station.add(TrackCircuitSection("TC Main", "Sta 2 Main TC", station))
station.add(TrackCircuitSection("TC Siding", "Sta 2 Siding TC", station))
station.add(TrackCircuitSection("TC Sta 2 OS", "Sta 2 OS TC", station))
station.add(TrackCircuitSection("TC Right Approach", "Sta 2 Right Approach TC", station, bell))

# Optionally, set timings
print "Setting timings"

jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = 5000  # turnout throw time
print "Turnout throw delay: ", jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL/1000., "seconds"

jmri.jmrit.ussctc.CodeLine.CODE_SEND_DELAY = 3000
print "Code send delay: ", jmri.jmrit.ussctc.CodeLine.CODE_SEND_DELAY/1000., "seconds"

# Start pulses for code and indication set to 1 second
jmri.jmrit.ussctc.CodeLine.START_PULSE_LENGTH = 1000
jmri.jmrit.ussctc.CodeLine.INTER_INDICATION_DELAY = 1000

jmri.jmrit.ussctc.SignalHeadSection.MOVEMENT_DELAY = 5000
print"Signal movement delay: ", jmri.jmrit.ussctc.SignalHeadSection.MOVEMENT_DELAY/1000., "seconds"

jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH = 5000
memories.provideMemory("IMUSS CTC:SIGNALHEADSECTION:1:TIME").setValue(jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH)
print "Running time for", jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH/1000., "seconds"

