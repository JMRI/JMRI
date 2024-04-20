# Sample script showing how to move a train used in operations
# The train to be moved is identified by a memory
#
# Part of the JMRI distribution
#
# Author: Daniel Boudreau copyright 2010, 2024
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
      print ('No train id in memory {}'.format(memSysName))
      return False              # all done, don't repeat again
    else:
      print ('get train by id: {}'.format(memTrainId.getValue())) 

    # get train by id
    train = self.tm.getTrainById(memTrainId.getValue())
    if (train != None):
      if (train.isBuilt() == True):
        print ('Move train {}, {}'.format(train.getName(), train.getDescription()))
        train.move()
        print ('Status: {}'.format(train.getStatus()))
        print ('Current location: {}'.format(train.getCurrentLocationName()))
      else:
        print ('Train {}, {}, is not built'.format(train.getName(), train.getDescription()))
    else :
      print ('Train id {} does not exist'.format(memTrainId.getValue()))

    return False              # all done, don't repeat again

moveTrain().start()          # create one of these, and start it running
