#
# Add Launchers kind of jynstruments to the JMRI application main window.
#
# This script is intended to be run at JMRI startup (setting in Preferences)
#

import jmri

import apps.Apps as Apps
import jmri.util.JmriJFrame as JmriJFrame

Apps.ynstrument("jython/Jynstruments/Launchers/Throttles.jyn")
Apps.ynstrument("jython/Jynstruments/Launchers/DecoderPro.jyn")

#JmriJFrame.getFrame("PanelPro").pack()
