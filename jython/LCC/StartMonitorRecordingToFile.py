# Start logging on an existing open LCC Monitor or OpenLCB Monitor window.
#
# This is useful if you e.g. want to capture startup activity. In that case, 
# have a startup action open the Monitor, then have another run this script.
#
# Bob Jacobsen      (C) 2026

import jmri
import jmri.util.JmriJFrame as JmriJFrame
import java


frame = JmriJFrame.getFrame("LCC Monitor")
if frame == None : frame = JmriJFrame.getFrame("OpenLCB Monitor")

contentpane = frame.getContentPane()

monitor = contentpane.getComponent(0)

monitor.logFileChooser.setSelectedFile(java.io.File("lccMonitorLog.txt"))
monitor.startLogButtonActionPerformed(None)

