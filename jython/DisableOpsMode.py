# Sample script to navigate through the GUI and disable
# the Ops Mode button on the main DecoderPro window.
#
# Set up to work with the JMRI 2.0 and later GUI layout.
#
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution

import jmri

import apps

# because the 1st window doesn't show up for a while, 
# we have to run this later

class DisableOpsMode(jmri.jmrit.automat.AbstractAutomaton) :
  def handle(self):
    # wait for long enough for window
    self.waitMsec(10*1000)
    # navigate through the window structure
    mainWindow = jmri.util.JmriJFrame.getFrameList().get(0)
    if (mainWindow.getTitle() != "DecoderPro") :
        mainWindow = jmri.util.JmriJFrame.getFrameList().get(1)
    if (mainWindow.getTitle() != "DecoderPro") :
        mainWindow = jmri.util.JmriJFrame.getFrameList().get(2)
    #print mainWindow.getTitle()
    
    contentPane = mainWindow.getContentPane()
    
    decoderProPane = contentPane.getComponents()[0]
    
    statusPane = decoderProPane.getComponents()[0]
    
    opsButton = statusPane.getComponents()[2]
    
    # now disable it and say why
    opsButton.setEnabled(False)
    opsButton.setToolTipText("We have disabled this button for the club layout")
    
    # alternately, uncomment the following to have the button not appear at all
    #
    # opsButton.setVisible(False)
    return 0

# now make it go
DisableOpsMode().start()
