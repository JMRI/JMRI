# Used in operations.
# Sample script to remove car kernels created by operation scripts.
# Use this script after terminating a train that services kernels created by operation scripts.
#
# Author: Daniel Boudreau, copyright 2015
#


import jmri

class RemoveCarKernels(jmri.jmrit.automat.AbstractAutomaton):
  def init(self):

    # used when creating kernel names
    self.regex = "_$_"

    print "Script to remove operation created kernels starts now!"

    return

  def handle(self):

    # get the car manager
    carManager = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarManager)

    # get a list of cars
    carList = carManager.getByIdList()

    for car in carList:
        if car.getKernel() == None:
            continue
        # only delete kernels created by scripts
        if not self.regex in car.getKernel().getName():
            continue
        # only delete kernels that don't have a train assignment or final destination
        if not car.getTrain() == None or not car.getFinalDestination() == None:
            continue
        print "Car (" + car.toString() + ") type (" + car.getTypeName() + ") is part of kernel (" + car.getKernel().getName() + ")"
        # delete the kernel
        carManager.deleteKernel(car.getKernel().getName())

    print "Done"
    return False              # all done, don't repeat again

RemoveCarKernels().start()          # create one of these, and start it running
