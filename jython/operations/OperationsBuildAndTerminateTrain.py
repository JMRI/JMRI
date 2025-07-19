# Sample script showing how to build and terminate a train. Used in operations
#
# Author: Daniel Boudreau, copyright 2010, 2024
# Part of the JMRI distribution

import jmri

class buildAndTerminate(jmri.jmrit.automat.AbstractAutomaton) :
  def init(self):
    # get the train manager
    self.tm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
    return

  def handle(self):

    # Build and terminate train
      trainName = "BL"
      train = self.tm.getTrainByName(trainName)
      print ('Build and terminate train {}'.format(trainName)) 

    # Build train
      if (train != None):
        train.build()
        built = train.isBuilt()
        train.setBuildEnabled(False)    # deselect build option (Checkbox in Trains window)
        if (built == True):
          print ('Train {} has been built'.format(trainName))
          train.terminate() # now terminate the train
          print ('Train {} has been terminated'.format(trainName))
        else:
          print ('Train {} build failed'.format(trainName))
      else:
        print ('Train {} does not exist'.format(trainName))
      return False              # all done, don't repeat again

buildAndTerminate().start()    # create one of these, and start it running
