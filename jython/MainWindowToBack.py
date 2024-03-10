# 
# Find the Main PanelPro window and send to back.
# This puts it behind any panels you might want to display (completely) at startup

import jmri
import java
import javax

class MainPanelToBack(jmri.jmrit.automat.AbstractAutomaton) :

    # handle() is called repeatedly until it returns false.
    def handle(self):
        self.waitMsec(8000)  # has to wait for main window to exist
        for frame in jmri.util.JmriJFrame.getFrameList() :
            if frame.getTitle() == "PanelPro" :
                frame.toBack()
                return
        print ("Did not find main window, probably need a longer delay")

# create one of these
a = MainPanelToBack()

# set the name, as a example of configuring it
a.setName("Move main PanelPro window to back")

# and start it running - this will only take a short time
a.start()

    