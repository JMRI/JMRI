# Sample script showing how to deselect the build checkbox when a 
# train terminates in operations.
#
# Author: Daniel Boudreau, copyright 2011
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import jmri

class terminateCheckBoxTrain(jmri.jmrit.automat.AbstractAutomaton) :      
  def init(self):
    # get the train manager
    self.tm = jmri.jmrit.operations.trains.TrainManager.instance()
    return

  def handle(self):
    # get a list of trains from the manager
    tList = self.tm.getTrainsByIdList()

    # show a list of trains
    for trainId in tList :
      train = self.tm.getTrainById(trainId)
      # print "checking train", train.getName(), train.getDescription(), "status", train.getStatus()
      if (train.getStatus() == jmri.jmrit.operations.trains.Train.TERMINATED):
        print "train", train.getName(), train.getDescription(), "is terminated"
        train.setBuild(False);

    return False              # all done, don't repeat again

terminateCheckBoxTrain().start()          # create one of these, and start it running
