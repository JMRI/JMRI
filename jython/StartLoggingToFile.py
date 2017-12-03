# Sample script to find all open LocoNet Monitor windows
# and tell them to start logging to their (preselected) log file.
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import jmri
import javax.swing

for frame in jmri.util.JmriJFrame.getFrameList() :
    # We can't use isinstance because monitor frames are just JmriJFrames
    # so we just try ...
    try :
        for panel in frame.contentPane.components :
            if isinstance(panel, jmri.jmrix.loconet.locomon.LocoMonPane) :
                panel.startLogButtonActionPerformed(None)
                print "Started logging on '", frame.title,"' window"
    except BaseException :
        # just ignore
        continue
    