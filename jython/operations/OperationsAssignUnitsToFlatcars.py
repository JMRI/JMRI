# Sample script to assign containers and trailers to flat cars.
#
#
# Author: Daniel Boudreau, copyright 2015
# Part of the JMRI distribution
#
# To use this script you must provide the type names for containers and trailers, and the type names for the flatcars to use. 
# You must also enter the train name that will service them. The train doesn't have to service the flatcars, this script will
# do the flatcar assignments based on the container or trailer destinations. Run this script after the train is built.
#
# When there isn't enough flatcars for the containers or trailers, the script will produce a warning message and remove them
# from the train.
#
# Some minor issues with this script:
# It will never assign two 20 foot containers and one 40 foot container to the same flatcar. It can do one of each.
# It doesn't consider the best allocation of flatcars, it can assign a single trailer to a flatcar that can hold two trailers,
# even of there's a flatcar available that can only hold one trailer.
#


import jmri
import javax.swing.JOptionPane;

class AssignTrailersToCars(jmri.jmrit.automat.AbstractAutomaton):  
        
  def init(self):
  
    # train (use train name)
    self.trainName = "85"
    
    # the flatcar load name
    self.loadNameTrailers = "Trailers"
    self.loadNameContainers = "Containers"
    
    # flatcar names and the number of container that can be carried
    self.container20 = [["Flatcar-COFC", 4]]
    self.container40 = [["Flatcar-COFC", 2]]
        
    # flatcar names and the number of trailers that can be carried. This defines the order in which flatcars are allocated.
    self.trailerPup = [["Flatcar-TOFC-(X)", 3], ["Flatcar-TOFC", 2], ["Flatcar-TOFC-(S)", 1], ["Flatcar-COFC", 2]]
    self.trailer = [["Flatcar-TOFC", 2], ["Flatcar-TOFC-(S)", 1], ["Flatcar-TOFC-(X)", 2], ["Flatcar-COFC", 2]]
    # Try to place reefer on single flatcars first, as an example of how to change the order flatcars are allocated
    self.trailerReefer = [["Flatcar-TOFC-(S)", 1], ["Flatcar-TOFC", 2], ["Flatcar-TOFC-(X)", 2], ["Flatcar-COFC", 2]]

    # containers and trailers, which flatcars can carry them, flatcar load name
    self.units = [["Container-20", self.container20, self.loadNameContainers], ["Container-40", self.container40, self.loadNameContainers],
                     ["Trailer-Pup", self.trailerPup, self.loadNameTrailers], ["Trailer-UPS", self.trailer, self.loadNameTrailers],
                     ["Trailer-USMail", self.trailer, self.loadNameTrailers], ["Trailer-Reefer", self.trailerReefer, self.loadNameTrailers],
                     ["Trailer", self.trailer, self.loadNameTrailers]]
    
    # used when creating kernel names
    self.regex = "_$_"
    
    print "Assigning flatcars to train (" + self.trainName + ")"
    
    return

  def handle(self):     

    # first some checking
    # determine if trailer, container, and flatcar names exist
    carTypes = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarTypes)
    for unitArray in self.units:
        unitName = unitArray[0]
        if (carTypes.containsName(unitName) == False):
          print "Error type(" + unitName + ") not found"
          return False
        flatcarOptions = unitArray[1]
        flatcarLoadName = unitArray[2]
        for flatcarOption in flatcarOptions:
            number = flatcarOption[1]
            flatcarName = flatcarOption[0]
            if (carTypes.containsName(flatcarName) == False):
                print "Error flatcar(" + flatcarName + ") for (" + unitName + ") not found"
                return False
            print number, "(" + unitName + ") can be carried by flatcar (" + flatcarName + ") load name (" + flatcarLoadName + ")"
            
    # get the train manager
    trainManager = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
    # determine if train exists and is built
    train = trainManager.getTrainByName(self.trainName)
    if train == None:
        print "Error Train (" + self.trainName + ") not found"
        return False
    if not train.isBuilt():
        print "Error Train (" + self.trainName + ") not built"
        return False
    
    # get the car manager
    carManager = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarManager)
          
    # get a list of cars from the manager
    carList = carManager.getByTrainDestinationList(train)
    flatcarList = carManager.getAvailableTrainList(train)
    
    print carList.size(), "cars assigned to train (" + self.trainName + ")"
    
    for unitArray in self.units:
        unitName = unitArray[0]
        flatcarOptions = unitArray[1]
        flatcarLoadName = unitArray[2]
        print "Searching for (" + unitName + "):"
        for unit in carList:
            if unit.getTypeName() == unitName:
                print "    Loading (" + unit.toString() + ") destination (" + unit.getDestinationName() + ", " + unit.getDestinationTrackName() + ") final destination (" + unit.getFinalDestinationName() + ", " + unit.getFinalDestinationTrackName() + ")"
                if not unit.getKernel() == None:
                    print "    (" + unit.toString() + ") assigned to kernel (" + unit.getKernelName() + ")"
                    continue
                else:
                    print "    (" + unit.toString() + ") isn't assigned to a kernel"
                    for flatcarOption in flatcarOptions:
                        flatcarName = flatcarOption[0]
                        number = flatcarOption[1]
                        for flatcar in flatcarList:
                            if flatcar.getTypeName() == flatcarName and flatcar.getTrack() == unit.getTrack():
                                print "    Found flatcar(" + flatcar.toString() + ") type (" + flatcarName + ")"
                                if flatcar.getKernel() == None:
                                    # create new kernel using train name and unit's id
                                    kernel = carManager.newKernel(self.trainName + self.regex + unit.toString()) 
                                    print "    Add (" + unit.toString() + ") and flatcar (" + flatcar.toString() + ") to kernel (" + kernel.getName() + ")"                                   
                                    unit.setKernel(kernel)
                                    flatcar.setKernel(kernel)
                                    # show flatcar first in the manifest or switch list, then the units being carried
                                    flatcar.setBlocking(0)
                                    unit.setBlocking(1)
                                    # now add flatcar to train
                                    # force flatcar to destination
                                    flatcar.setDestination(unit.getDestination(), unit.getDestinationTrack(), True)
                                    flatcar.setFinalDestination(unit.getFinalDestination())
                                    flatcar.setFinalDestinationTrack(unit.getFinalDestinationTrack())
                                    flatcar.setRouteLocation(unit.getRouteLocation())
                                    flatcar.setRouteDestination(unit.getRouteDestination())
                                    flatcar.setLoadName(flatcarLoadName)
                                    flatcar.setTrain(train)
                                    train.setModified(True)
                                    break
                                elif flatcar.getKernel().getSize() <= number:
                                    kernel = flatcar.getKernel()
                                    # determine if kernel is going to the same destination as this unit
                                    if not unit.getDestination() == flatcar.getDestination() or not unit.getDestinationTrack() == flatcar.getDestinationTrack():
                                        print "    Can't use flatcar (" + flatcar.toString() + ") destination (" + flatcar.getDestinationName() + ", " + flatcar.getDestinationTrackName() + ")"
                                        continue
                                    if not unit.getFinalDestination() == flatcar.getFinalDestination() or not unit.getFinalDestinationTrack() == flatcar.getFinalDestinationTrack():
                                        print "    Can't use flatcar (" + flatcar.toString() + ") final destination (" + flatcar.getFinalDestinationName() + ", " + flatcar.getFinalDestinationTrackName() + ")"
                                        continue
                                    if not flatcarLoadName == flatcar.getLoadName():
                                        print "    Can't use flatcar (" + flatcar.toString() + ") load name {" + flatcar.getLoadName() + ")"
                                        continue                                                                 
                                    print "    Add (" + unit.toString() + ") to kernel (" + kernel.getName() + ")"
                                    unit.setKernel(kernel)
                                    unit.setBlocking(kernel.getSize())
                                    train.setModified(True)
                                    break
                                else:
                                    print "    Flatcar (" + flatcar.toString() + ") full"
                        else:
                            continue
                        break
                    if unit.getKernel() == None:
                        title = "Warning couldn't find enough flatcars for train (" + self.trainName + ")"
                        message = "Warning (" + unit.toString() + ")  type (" + unit.getTypeName() + ") at (" + unit.getLocationName() + ") not assigned to a flatcar, train (" + self.trainName + ")"
                        print message
                        javax.swing.JOptionPane.showMessageDialog(None, message, title, javax.swing.JOptionPane.WARNING_MESSAGE)
                        # remove trailer from train
                        unit.setRouteLocation(None)
                        unit.setRouteDestination(None)
                        unit.setTrain(None)
                        train.setModified(True)
    return False  # all done, don't repeat again

AssignTrailersToCars().start()  # create one of these, and start it running
