import java
import jmri

# Sets up a dispatcher system to: 
 # run trains from anywhere on your layout 
 # using the dispatcher JMRI module 
 # by pressing 'Move Here' buttons (set up automatically) on the layout
 # or by scheduling trains to run on routes.
 
# You need to have set up blocks and signal masts before running this system
# Sections, transits and train_info files are set up automatically for you.

# 1) Run the Toplevel DispatcherSystem.py (this file)

# 2) Read the help accessed from the menu to get an idea of what the system does

RunDispatcherSystem = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/DispatcherSystem.py')
execfile(RunDispatcherSystem)
