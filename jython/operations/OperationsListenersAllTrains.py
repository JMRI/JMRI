# Sample script showing how to listen for all trains used in operations.
# Not to be used as a train move or termination script!
#
# Author: Bob Jacobsen, copyright 2004
# Author: Daniel Boudreau, copyright 2010
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision$

import jmri

# First, define the listener.  This one just prints some
# information on the change, but more complicated code is
# of course possible.
class MyListener(java.beans.PropertyChangeListener):    
  def propertyChange(self, event):
    print "Train name:",event.source.getName()
    print "Train id:",event.source.getId()
    print "Train status:",event.source.getStatus()
    print "Change:", event.propertyName," from: ",event.oldValue," to: ",event.newValue 
    
    # Some sample code
    # Get the train and show if it was built.
    # And if not built if it has been terminated.
    trainId = event.source.getId()
    tm = jmri.jmrit.operations.trains.TrainManager.instance()
    train = tm.getTrainById(trainId)
    if (train.isBuilt() == True):
    	print "Train", train.getName(), "is built"
    else: 
   		print "Train", train.getName(), "not built"
   		if (train.getStatus() == "Terminated"):
   			print "Train", train.getName(), "has been terminated"

class listenAllTrains(jmri.jmrit.automat.AbstractAutomaton) :      
  def init(self):
    # get the train manager
    self.tm = jmri.jmrit.operations.trains.TrainManager.instance()
    print "Listen to all trains in operations"
    return

  def handle(self):
    print "Number of trains", self.tm.numEntries()
    # get a list of trains from the manager
    tList = self.tm.getTrainsByIdList()
    count = 1

    # show a list of trains and connect property change
    for trainId in tList :
      train = self.tm.getTrainById(trainId)
      print "Train", count, train.getName(), train.getDescription()
      m = MyListener()
      train.addPropertyChangeListener(m)
      count = count + 1

    return False              # all done, don't repeat again

listenAllTrains().start()          # create one of these, and start it running
