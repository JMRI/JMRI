# Sample script showing how to move a train used in operations
# The train to be moved is identified by a memory
#
# Part of the JMRI distribution
#
# Author: Daniel Boudreau copyright 2010
#

import jmri

class moveTrain(jmri.jmrit.automat.AbstractAutomaton) : 
  def init(self):
    # get the train manager
    self.tm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
    return

  def handle(self):
    # define which memory the train id is located in
    memSysName = "12"    # Memory system name IM12
    # get the memory
    memTrainId = memories.provideMemory(memSysName)
    if (memTrainId.getValue() == None):
      print "No train id in memory", trainId
      return False              # all done, don't repeat again
    else:
      print "get train by id:", memTrainId.getValue()

    # get train by id
    train = self.tm.getTrainById(memTrainId.getValue())
    if (train != None):
      if (train.isBuilt() == True):
        print "Move train", train.getName(), train.getDescription()
        train.move()
        print "Status:", train.getStatus();
        print "Current location:", train.getCurrentLocationName()
      else:
        print "Train", train.getName(), train.getDescription(), "hasn't been built"
    else :
      print "Train id", memTrainId.getValue(), "does not exist"

    return False              # all done, don't repeat again

moveTrain().start()          # create one of these, and start it running
