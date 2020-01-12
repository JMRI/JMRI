# Sample script showing how to terminate trains used in operations
# Prints a list of trains, and then if a train was built, terminates
# the train by moving it to the end of its route
#
# Author: Daniel Boudreau, copyright 2010
# Part of the JMRI distribution

import jmri

class terminateTrains(jmri.jmrit.automat.AbstractAutomaton) :      
  def init(self):
    # get the train manager
    self.tm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
    return

  def handle(self):
    print "number of trains", self.tm.numEntries()
    # get a list of trains from the manager
    trainList = self.tm.getTrainsByIdList()
    count = 1

    # show a list of trains
    for train in trainList :
      print "train", count, train.getName(), train.getDescription()
      count = count + 1

    # now terminate trains that were built by moving them
    for train in trainList :
      if (train.isBuilt() == True):
         print "train", train.getName(), "was built"
         while (train.isBuilt() == True):
            print "move train", train.getName() 
            train.move() 
         print "train", train.getName(), "terminated"

    return False              # all done, don't repeat again

terminateTrains().start()          # create one of these, and start it running
