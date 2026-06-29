# Start logging immediately on an existing open LocoNet Monitor window.
#
# This is useful if you e.g. want to capture startup activity. In that case, 
# have a startup action open the Monitor, then have another run this script.
#
# Bob Jacobsen      (C) 2026

import jmri
import jmri.util.JmriJFrame as JmriJFrame
import java


frame = JmriJFrame.getFrame("Monitor LocoNet")

contentpane = frame.getContentPane()

monitor = contentpane.getComponent(0)

monitor.logFileChooser.setSelectedFile(java.io.File("loconetMonitorLog.txt"))
monitor.startLogButtonActionPerformed(None)

