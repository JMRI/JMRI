# Sample script showing how to listen for all trains used in operations.
# Not to be used as a train move or termination script!
#
# Author: Bob Jacobsen, copyright 2004
# Author: Daniel Boudreau, copyright 2010, 2012
# Part of the JMRI distribution

import java
import java.beans
import java.beans.PropertyChangeListener as PropertyChangeListener
import jmri

# First, define the listener.  This one just prints some
# information on the change, but more complicated code is
# of course possible.
class MyListener(PropertyChangeListener):
  def propertyChange(self, event):
    print " "   # add a line between updates to make it easier to read
    print "Train name:",event.source.getName()
    print "Train id:",event.source.getId()
    print "Train status:",event.source.getStatus()
    print "Change:", event.propertyName," from: ",event.oldValue," to: ",event.newValue

    # Some sample code
    # Get the train and show if it was built.
    # And if not built if it has been terminated.
    trainId = event.source.getId()
    tm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
    train = tm.getTrainById(trainId)
    if (train.isBuilt() == True):
        print "Train", train.getName(), "is built"
        # the train move complete property change was added in jmri version 3.2
        if (event.propertyName == jmri.jmrit.operations.trains.Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY):
            print "Cars in train:", train.getNumberCarsInTrain()
            print "Train length:", train.getTrainLength()
            print "Train weight:", train.getTrainWeight()
    else:
        print "Train", train.getName(), "not built"

    # determine using property change if train has just been built
    if (event.propertyName == jmri.jmrit.operations.trains.Train.BUILT_CHANGED_PROPERTY and event.newValue == "true"):
        print "Train", train.getName(), "built status changed to true"

class listenAllTrains(jmri.jmrit.automat.AbstractAutomaton) :
  def init(self):
    # get the train manager
    self.tm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
    print "Listen to all trains in operations"
    return

  def handle(self):
    print "Number of trains", self.tm.numEntries()
    # get a list of trains from the manager
    trainList = self.tm.getTrainsByIdList()
    count = 1

    # show a list of trains and connect property change
    for train in trainList :
      print "Train", count, train.getName(), train.getDescription()
      train.addPropertyChangeListener(MyListener())
      count = count + 1

    return False              # all done, don't repeat again

listenAllTrains().start()          # create one of these, and start it running
