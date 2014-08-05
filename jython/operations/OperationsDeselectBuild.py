# Sample script showing how to deselect the build checkbox when a 
# train terminates in operations.
#
# Author: Daniel Boudreau, copyright 2011
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision$

import jmri

class terminateCheckBoxTrain(jmri.jmrit.automat.AbstractAutomaton) :      
  def init(self):
    # get the train manager
    self.tm = jmri.jmrit.operations.trains.TrainManager.instance()
    return

  def handle(self):
    print "Deselect build checkbox for terminated trains"
    # get a list of trains from the manager
    trainList = self.tm.getTrainsByIdList()

    # show a list of trains
    for train in trainList :
      print "checking train", train.getName(), train.getDescription(), "status:", train.getStatus()
      if (train.getStatus().startswith(jmri.jmrit.operations.trains.Train.TERMINATED)):
        print "train", train.getName(), train.getDescription(), "is terminated, deselect build checkbox"
        train.setBuildEnabled(False);

    return False              # all done, don't repeat again

terminateCheckBoxTrain().start()          # create one of these, and start it running
