import java
import jmri

# Sets up a dispatcher system to: 
 # run trains from anywhere on your layout 
 # using the dispatcher JMRI module 
 # by pressing 'Move Here' buttons (set up automatically) on the layout
 
# You need to have set up signal masts, logic and sections before running this system

# 1) Run the Toplevel DispatcherSystem.py (this file)

# 2) Read the help accessed from the menu to get an idea of what the system does

RunDispatcherSystem = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/DispatcherSystem.py')
execfile(RunDispatcherSystem)
