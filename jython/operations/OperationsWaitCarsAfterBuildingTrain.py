# Used in operations.
# Sample script that will set a wait value for all cars in a specific train.
# Useful if you want the cars set out by the train to remain unavailable for
# other trains.  A wait value of 1, means the next train built will not use
# cars from this train.
#
# Use this script after the train has been built.
#
# Author: Daniel Boudreau, copyright 2011, 2012
#
# To use this script you must assign the train that you want the cars waited,
# and the wait value.
#

import jmri

class WaitCars(jmri.jmrit.automat.AbstractAutomaton):
  def init(self):

    # train (use train name)
    self.trainName = "Green Bay Hauler"

    # wait value
    self.wait = 3

    return

  def handle(self):

    # get the train and car managers
    tm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
    cm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarManager)

    # the following code checks the values entered
    train = tm.getTrainByName(self.trainName)
    if (train == None):
        print "Train (", self.trainName, ") does not exist!"
        return False
    if not (train.isBuilt()):
        print "Train (", self.trainName, ") not built!"
        return False

    # get a list of cars in this train
    carList = cm.getByTrainDestinationList(train)
    print "Train (", self.trainName,") has ", carList.size(), " cars assigned to it"

    for car in carList:
        if (car.getNextWait() == 0):
            car.setNextWait(self.wait)
            print "Setting next wait to ", self.wait, " for car ", car.toString()
        else:
            print "Car ",car.toString(), " has next wait value ", car.getNextWait()

    print "Done"
    return False              # all done, don't repeat again

WaitCars().start()          # create one of these, and start it running
