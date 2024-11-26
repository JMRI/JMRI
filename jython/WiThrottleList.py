# WiThrottleList.py -- Sample script to access the WiThrottle session table.
# Note:  Depends on the jmri.jmrit.withrottle.UserInterface.getThrottleList() public method introduced with JMRI 5.9.7.
#
# The WiThrottle server must be running before the script is run.  When the script runs, it finds the
# WiThrottle window and then attaches a TableModelListener to the table that holds the current
# WiThrottle sessions.  When the session list changes, the "tableChanged" event occurs.  The only
# event is UPDATE.  There are no details about what changed.
#
# Author:  Dave Sand copyright (c) 2024

import java
import javax.swing.event.TableModelListener
import jmri

throttleList = None

# Define listener
class TableListener(javax.swing.event.TableModelListener):
    def tableChanged(self, event):
        print 'Event type = {}'.format(event.getType())
        rows = throttleList.getRowCount()
        for row in range(0, rows):
            val = throttleList.getValueAt(row, 1)  # Column 1 is the DCC address
            print '  addr = {}'.format(val)
wtListener = TableListener()

# Find the throttle list table
for frame in jmri.util.JmriJFrame.getFrameList():
    if isinstance(frame, jmri.jmrit.withrottle.UserInterface):
        throttleList = frame.getThrottleList()

# Finish setup
if throttleList is not None:
    throttleList.addTableModelListener(wtListener)
    print "WiThrottle server is running, the listener is attached to the WiThrottle session table"
else:
    print "WiThrottle server is NOT running"
