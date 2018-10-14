# Sample script showing how to active the train window used in operations
#
# Part of the JMRI distribution
#
# Author: Daniel Boudreau copyright 2010

import jmri

class openTrainWindow(jmri.jmrit.automat.AbstractAutomaton) : 
  def init(self):
    # Open the train window
    f = jmri.jmrit.operations.trains.TrainsTableFrame()
    f.setVisible(True)
    return

  def handle(self):
    return False              # all done, don't repeat again

openTrainWindow().start()          # create one of these, and start it running
