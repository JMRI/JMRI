# Sample script showing how to active the timetable window used in operations
#
# Part of the JMRI distribution
#
# Author: Daniel Boudreau copyright 2017
#

import jmri

class openTimetableWindow(jmri.jmrit.automat.AbstractAutomaton) : 
  def init(self):
    # Open the train schedules window
    f = jmri.jmrit.operations.trains.schedules.TrainsScheduleTableFrame()
    f.setVisible(True)
    return

  def handle(self):
    return False              # all done, don't repeat again

openTimetableWindow().start() # create one of these, and start it running
