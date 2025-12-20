import java
import jmri

# Dispatcher System extends Dispatcher by automatically creating stations and associated dispatcher files
# allowing trains to be dispatched between the stations and scheduled with timetables.
# Simulation is supported and can be used to try out the system before the physical layout is built.

# You have to first set up:

# 1) a layout panel setup with track, blocks and signal masts,
# 2) certain blocks marked as stations (instructions given), and
# 3) some engines set up with speed profiles (dummy speed profiles provided if you only want to simulate
#    to see what is possible)

# You then run the system and the following are created automatically:

# 1) signal logic, sections, transits and train info files enabling trains to run anywhere on the layout
#    using dispatcher
# 2) A set of station buttons inserted on the panel which allow trains to run station to station under dispatcher
# 3) Provision to set up routes so that trains can be sent along these routes
# 4) A scheduler to enable trains to run at preset times
# 5) Timetables showing the scheduled trains
# 6) A set of buttons provided on the panel which enable you to setup and run the system easily

# The system is stored in the Dispatcher System folder.

# Instructions
##############
# 1) Run the Toplevel DispatcherSystem.py (this file, or the one in the DispatcherSystem directory)
# 2) Read the help accessed from the menu of the panel produced to get an detailed idea of what the system does
# 3) The instructions can also be found by searching for 'Dispatcher System' in JMRI Help

RunDispatcherSystem = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/DispatcherSystem.py')
execfile(RunDispatcherSystem)
