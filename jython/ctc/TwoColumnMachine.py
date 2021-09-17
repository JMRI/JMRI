# Configure a two-column CTC machine.
#
# Uses only internal Sensors and Turnouts so it can be run as a sample.
#
# Author: Bob Jacobsen, copyright 2017, 2021
# Part of the JMRI distribution
import java
import jmri
from jmri.jmrit.ussctc import *
from jmri.jmrit import Sound

# First, we define Turnouts and Sensors used by this example
# These are normally defined in a panel file with names specific
# to the layout

# initialize objects on layout
turnouts.getTurnout("Code Indication Start")    .state = CLOSED
turnouts.getTurnout("Code Send Start")          .state = CLOSED
turnouts.getTurnout("Bell")                     .state = CLOSED
sensors.getSensor("Bell Cutout")                .state = INACTIVE

sensors. getSensor( "TC Sta 1 Left Approach")   .state = INACTIVE
sensors. getSensor( "TC Sta 1 OS")              .state = INACTIVE
sensors. getSensor( "TC Sta 2 Main")            .state = INACTIVE
sensors. getSensor( "TC Sta 2 Siding")          .state = INACTIVE
sensors. getSensor( "TC Sta 2 OS")              .state = INACTIVE
sensors. getSensor( "TC Sta 2 Right Approach")  .state = INACTIVE

turnouts.getTurnout("Sta 1 Layout TO")          .state = CLOSED

turnouts.getTurnout("Sta 2 Layout TO")          .state = CLOSED

# initialize_options objects on panel
turnouts.getTurnout("Sta 1 Code")               .state = CLOSED
sensors. getSensor( "Sta 1 Code")               .state = INACTIVE

turnouts.getTurnout("Sta 1 TO 1 N")             .state = CLOSED
sensors. getSensor( "Sta 1 TO 1 N")             .state = ACTIVE
turnouts.getTurnout("Sta 1 TO 1 R")             .state = CLOSED
sensors. getSensor( "Sta 1 TO 1 R")             .state = INACTIVE

turnouts.getTurnout("Sta 1 SI 2 L")             .state = CLOSED
sensors. getSensor( "Sta 1 SI 2 L")             .state = INACTIVE
turnouts.getTurnout("Sta 1 SI 2 C")             .state = CLOSED
sensors. getSensor( "Sta 1 SI 2 C")             .state = ACTIVE
turnouts.getTurnout("Sta 1 SI 2 R")             .state = CLOSED
sensors. getSensor( "Sta 1 SI 2 R")             .state = INACTIVE

turnouts.getTurnout("Sta 1 Left Approach TC")   .state = CLOSED
turnouts.getTurnout("Sta 1 OS TC")              .state = CLOSED


turnouts.getTurnout("Sta 2 Code")               .state = CLOSED
sensors. getSensor( "Sta 2 Code")               .state = INACTIVE

turnouts.getTurnout("Sta 2 TO 3 N")             .state = CLOSED
sensors. getSensor( "Sta 2 TO 3 N")             .state = ACTIVE
turnouts.getTurnout("Sta 2 TO 3 R")             .state = CLOSED
sensors. getSensor( "Sta 2 TO 3 R")             .state = INACTIVE

turnouts.getTurnout("Sta 2 SI 4 L")             .state = CLOSED
sensors. getSensor( "Sta 2 SI 4 L")             .state = INACTIVE
turnouts.getTurnout("Sta 2 SI 4 C")             .state = CLOSED
sensors. getSensor( "Sta 2 SI 4 C")             .state = ACTIVE
turnouts.getTurnout("Sta 2 SI 4 R")             .state = CLOSED
sensors. getSensor( "Sta 2 SI 4 R")             .state = INACTIVE

turnouts.getTurnout("Sta 2 Main TC")            .state = CLOSED
turnouts.getTurnout("Sta 2 Siding TC")          .state = CLOSED
turnouts.getTurnout("Sta 2 OS TC")              .state = CLOSED
turnouts.getTurnout("Sta 2 Right Approach TC")  .state = CLOSED

# signals must be provided by panel file, including signal logic


# The core of the sample script starts here, defining & connecting
# the USS CTC objects to run the sample panel

# The bell and code line are shared by all Stations

bell = VetoedBell("Bell Cutout", PhysicalBell("Bell", Sound("program:resources/sounds/Bell.wav")))   # both a layout output (real bell) and a computer sound, see ComputerBell for option

codeline = CodeLine("Code Indication Start", "Code Send Start", "IT101", "IT102", "IT103", "IT104")

# Set up Station 1 - stations are numbered 1, 2, 3 etc.
#    Station 1 is levers 1 and 2

station1 = Station("1", codeline, CodeButton("Sta 1 Code", "Sta 1 Code"))

turnout1 = TurnoutSection("Sta 1 Layout TO", "Sta 1 TO 1 N", "Sta 1 TO 1 R", "Sta 1 TO 1 N", "Sta 1 TO 1 R", station1)
station1.add(turnout1)

station1.add(TrackCircuitSection("TC Sta 1 Left Approach", "Sta 1 Left Approach TC", station1, bell))
station1.add(TrackCircuitSection("TC Sta 1 OS", "Sta 1 OS TC", station1, bell))

rightward = ["2 Upper", "2 Lower"]
leftward  = ["2 Main", "2 Siding"]
signal2 = SignalHeadSection(rightward, leftward, "Sta 1 SI 2 L", "Sta 1 SI 2 C", "Sta 1 SI 2 R", "Sta 1 SI 2 L", "Sta 1 SI 2 R", station1);
station1.add(signal2)

occupancyLock1 = OccupancyLock("TC Sta 1 OS")                # Turnout locked if occupied
routeLock1 = RouteLock(["2 Upper", "2 Lower", "2 Main", "2 Siding"]);   # Turnout locked if route set across it
timeLock1 = TimeLock(signal2);                               # Provide time lock after certain signal changes
turnout1.addLocks([occupancyLock1, routeLock1, timeLock1]);  # Add to turnout; see below for Traffic locks on signal

# Set up Station 2 - levers 3 and 4

station2 = Station("2", codeline, CodeButton("Sta 2 Code", "Sta 2 Code"))

turnout3 = TurnoutSection("Sta 2 Layout TO", "Sta 2 TO 3 N", "Sta 2 TO 3 R", "Sta 2 TO 3 N", "Sta 2 TO 3 R", station2)
station2.add(turnout3)

station2.add(TrackCircuitSection("TC Sta 2 Main", "Sta 2 Main TC", station2))
station2.add(TrackCircuitSection("TC Sta 2 Siding", "Sta 2 Siding TC", station2))
station2.add(TrackCircuitSection("TC Sta 2 OS", "Sta 2 OS TC", station2, bell))
station2.add(TrackCircuitSection("TC Sta 2 Right Approach", "Sta 2 Right Approach TC", station2, bell))

rightward = ["4 Main", "4 Siding"]
leftward  = ["4 Upper", "4 Lower"]
signal4 = SignalHeadSection(rightward, leftward, "Sta 2 SI 4 L", "Sta 2 SI 4 C", "Sta 2 SI 4 R", "Sta 2 SI 4 L", "Sta 2 SI 4 R", station2);
station2.add(signal4)

occupancyLock2 = OccupancyLock("TC Sta 2 OS")
routeLock2 = RouteLock(["4 Upper", "4 Lower", "4 Main", "4 Siding"]);
timeLock2 = TimeLock(signal4);
turnout3.addLocks([occupancyLock2, routeLock2, timeLock2]);

# traffic locks - lock signal if route is set toward it already - note far route depends on Turnout settings, i.e. siding or main
viaMain2 = TrafficLock(signal4, SignalHeadSection.CODE_LEFT,    [jmri.BeanSetting(turnouts.getTurnout("Sta 1 Layout TO"), THROWN), jmri.BeanSetting(turnouts.getTurnout("Sta 2 Layout TO"), THROWN)])
viaSiding2 = TrafficLock(signal4, SignalHeadSection.CODE_LEFT,  [jmri.BeanSetting(turnouts.getTurnout("Sta 1 Layout TO"), CLOSED), jmri.BeanSetting(turnouts.getTurnout("Sta 2 Layout TO"), CLOSED)])
signal2.addRightwardLocks([viaMain2,viaSiding2])

viaMain4 = TrafficLock(signal2, SignalHeadSection.CODE_RIGHT,   [jmri.BeanSetting(turnouts.getTurnout("Sta 2 Layout TO"), THROWN), jmri.BeanSetting(turnouts.getTurnout("Sta 1 Layout TO"), THROWN)])
viaSiding4 = TrafficLock(signal2, SignalHeadSection.CODE_RIGHT, [jmri.BeanSetting(turnouts.getTurnout("Sta 2 Layout TO"), CLOSED), jmri.BeanSetting(turnouts.getTurnout("Sta 1 Layout TO"), CLOSED)])
signal4.addLeftwardLocks([viaMain4,viaSiding4])



# Optionally, set timings
print "Setting timings"

jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = 5000  # turnout throw time
print "Turnout throw delay: ", jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL/1000., "seconds"

jmri.jmrit.ussctc.SignalHeadSection.MOVEMENT_DELAY = 4000
print"Signal movement delay: ", jmri.jmrit.ussctc.SignalHeadSection.MOVEMENT_DELAY/1000., "seconds"

jmri.jmrit.ussctc.CodeLine.CODE_SEND_DELAY = 1000
print "Code send delay: ", jmri.jmrit.ussctc.CodeLine.CODE_SEND_DELAY/1000., "seconds"

# Start pulses for code and indication
jmri.jmrit.ussctc.CodeLine.START_PULSE_LENGTH = 500
print "Length of start pulse to relay box: ", jmri.jmrit.ussctc.CodeLine.START_PULSE_LENGTH/1000., "seconds"

# force some time between indications
jmri.jmrit.ussctc.CodeLine.INTER_INDICATION_DELAY = 500
print "Length of inter-indication delay: ", jmri.jmrit.ussctc.CodeLine.INTER_INDICATION_DELAY/1000., "seconds"

# set the "run time" duration.  Prototypically several minutes, model railroaders don't want to wait that long
jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH = 30000
memories.provideMemory("IMUSS CTC:SIGNALHEADSECTION:1:TIME").setValue(jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH)
print "Running time for", jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH/1000., "seconds"
