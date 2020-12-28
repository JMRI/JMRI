import java
import jmri

# I could never get Dispatcher to run reliably and consistently. It also took ages to set up.
# This system attempts to deal with both of these issues

# Starting from a system with

# 1) a layout panel setup with track, blocks and signals, 
# 2) certain blocks marked as stations, and 
# 3) some engines set up with speed profiles 

# it creates a system in three easy stages (three button presses) with

# 1) a set of transits and train info files enabling trains to run anywhere on the layout using dispatcher
# 2) A set of station buttons inserted on the panel which allow trains to run from station to station under dispatcher or
# 3) routes to be set up and the trains run along these routes
# 4) The trains can optionally be scheduled to run at preset times
# 5) A set of buttons are provided on the panel which enable you to setup and run the system easily

# The system is stored in the Dispatcher System folder, but you can run it using this file

# Procedure
# 1) Run the Toplevel DispatcherSystem.py (this file)
# 2) Read the help accessed from the menu to get detailed instructions on how to run the system

# I can now run one train reliably under dispatcher, and two or more trains with a proviso detailed in the help.
# I hope you will be able to as well, and that the proviso will be dealt with soon.

# Author:  Bill Fitch copyright (c) 2020

RunDispatcherSystem = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/DispatcherSystem.py')
execfile(RunDispatcherSystem)
