# Configure a two-column CTC machine.
#
# Uses only internal Sensors and Turnouts so it can be run as a sample.
#
# Author: Bob Jacobsen, copyright 2017, 2021
# Part of the JMRI distribution
import java
import jmri
from jmri.jmrit.ussctc import *

# First, we define Turnouts and Sensors used by this example
# These are normally defined in a panel file with names specific
# to the layout

# initialize objects on layout
turnouts.getTurnout("Code Indication Start")    .state = CLOSED
turnouts.getTurnout("Code Send Start")          .state = CLOSED
turnouts.getTurnout("Bell")                     .state = CLOSED

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

# needed until signal logic in place
signals.getSignalHead("2 Upper").state = GREEN
signals.getSignalHead("2 Lower").state = GREEN
signals.getSignalHead("2 Main").state = GREEN
signals.getSignalHead("2 Siding").state = GREEN

signals.getSignalHead("4 Upper").state = GREEN
signals.getSignalHead("4 Lower").state = GREEN
signals.getSignalHead("4 Main").state = GREEN
signals.getSignalHead("4 Siding").state = GREEN


# service routine to make entries clearer
def arrayList(contents) :
    retval = java.util.ArrayList()
    for item in contents :
        retval.add(item)
    return retval

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

station.add(TrackCircuitSection("TC Sta 1 Left Approach", "Sta 1 Left Approach TC", station, bell))
station.add(TrackCircuitSection("TC Sta 1 OS", "Sta 1 OS TC", station))

rightward = arrayList(["2 Upper", "2 Lower"])
leftward  = arrayList(["2 Main", "2 Siding"])
signal2 = SignalHeadSection(rightward, leftward, "Sta 1 SI 2 L", "Sta 1 SI 2 C", "Sta 1 SI 2 R", "Sta 1 SI 2 L", "Sta 1 SI 2 R", station);
station.add(signal2)

occupancyLock = OccupancyLock("TC Sta 1 OS")
routeLock = RouteLock(["2 Upper", "2 Lower", "2 Main", "2 Siding"]);
turnout.addLocks([occupancyLock, routeLock, TimeLock(signal2)]);

# Set up Station 2 - levers 3 and 4

station = Station("2", codeline, CodeButton("Sta 2 Code", "Sta 2 Code"))

turnout = TurnoutSection("Sta 2 Layout TO", "Sta 2 TO 3 N", "Sta 2 TO 3 R", "Sta 2 TO 3 N", "Sta 2 TO 3 R", station)
station.add(turnout)

station.add(TrackCircuitSection("TC Sta 2 Main", "Sta 2 Main TC", station))
station.add(TrackCircuitSection("TC Sta 2 Siding", "Sta 2 Siding TC", station))
station.add(TrackCircuitSection("TC Sta 2 OS", "Sta 2 OS TC", station))
station.add(TrackCircuitSection("TC Sta 2 Right Approach", "Sta 2 Right Approach TC", station, bell))

rightward = arrayList(["4 Main", "4 Siding"])
leftward  = arrayList(["4 Upper", "4 Lower"])
signal4 = SignalHeadSection(rightward, leftward, "Sta 2 SI 4 L", "Sta 2 SI 4 C", "Sta 2 SI 4 R", "Sta 2 SI 4 L", "Sta 2 SI 4 R", station);
station.add(signal4)

occupancyLock = OccupancyLock("TC Sta 2 OS")
routeLock = RouteLock(["4 Upper", "4 Lower", "4 Main", "4 Siding"]);
turnout.addLocks([occupancyLock, routeLock, TimeLock(signal4)]);

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

jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH = 30000
memories.provideMemory("IMUSS CTC:SIGNALHEADSECTION:1:TIME").setValue(jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH)
print "Running time for", jmri.jmrit.ussctc.SignalHeadSection.DEFAULT_RUN_TIME_LENGTH/1000., "seconds"
