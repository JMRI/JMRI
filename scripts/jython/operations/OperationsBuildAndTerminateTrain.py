# Sample script showing how to build and terminate a train. Used in operations
# 
# Author: Daniel Boudreau, copyright 2010
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import jmri

class buildAndTerminate(jmri.jmrit.automat.AbstractAutomaton) :      
  def init(self):
    # get the train manager
    self.tm = jmri.jmrit.operations.trains.TrainManager.instance()
    return

  def handle(self):
 
    # Build and terminate train
      trainName = "BL"
      train = self.tm.getTrainByName(trainName)

    # Build train 
      if (train != None):
        train.build()
        built = train.isBuilt()
        train.setBuild(False)	# deselect build option
        if (built == True):
          print "train", trainName, "has been built"
          train.terminate()	# now terminate the train
          print "train", trainName, "has been terminated"
        else:
          print "train", trainName, "build failed"
      else:
        print "train", trainName, "does not exist"
      return False              # all done, don't repeat again

buildAndTerminate().start()    # create one of these, and start it running
