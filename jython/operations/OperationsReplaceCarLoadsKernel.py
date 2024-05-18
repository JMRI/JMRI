# Sample script showing how to replace car loads at a location.
# Used in operations.  This script will load all cars of a
# given type at a location and track with the car loads you specify.
#
#
# Author: Daniel Boudreau, copyright 2011, 2024
# Part of the JMRI distribution
#
# To use this script you must assign the location, tracks, car type
# and car loads.
#
# Location to load cars (see self.locationName below, replace locationName)
#
# Tracks at the above location that will load your cars
# replace trackName1, trackName2, trackName3, etc. with the
# names of your tracks.  You can add more, or delete the one you
# don't need.
#
# The car type, (see self.carTypeName, replace carTypeName)
#
# Car loads, (see self.carLoadNames, replace carLoadName1, carLoadName2, carLoadName3, etc)
# The first car will be loaded with carLoadName1, the second carLoadName2, etc, until
# all loads are used, and then will begin again with carLoadName1.
#
# The car loads you want to replace, (see self.carReplaceLoads)
#
# The number of car loads you want to replace. (see self.number)
#
# The minimum number of cars loads you want to replace. (see self.minNumber)
#
# You can also assign the cars to a kernel (see self.carTypesKernel)

import jmri


class loadCars(jmri.jmrit.automat.AbstractAutomaton):

  def init(self):

    # location (use location name)
    self.locationName = "locationName"

    # tracks (use track names) at location to generate loads
    self.trackNames = ["trackName1", "trackName2", "trackName3", "trackName4"]

    # car types (use car type name)
    self.carTypeNames = ["carTypeName1", "carTypeName2"]

    # car loads to generate (use load names)
    self.carLoadNames = ["carLoadName1", "carLoadName2", "carLoadName3", "carLoadName4"]

    # car loads to replace
    self.carReplaceLoads = ["L", "E"]

    # maximum number of loads to replace
    self.number = 100
    
    # minimum number of loads to replace
    self.minNumber = 1
    
    # optional feature to place cars in a kernel. The 1st car type becomes the lead
    # Each position of the kernel can have several car types to choose from
    self.carTypesKernel = [["leadCarType1", "orLeadCarType2", "orLeadCarType3"], ["2ndCarType1", "or2ndCarType2"], ["3rdCarType1", "or3ndCarType2"], ["4thCarType1"]]
    
    # used when creating kernel names
    # see operations "OperationsRemoveCarKenels.py" to strip kernel name
    self.regex = "_$_"

    return

  def handle(self):

    # get the car manager
    cm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarManager)

    # the following code checks the values entered
    # first check the location and tracks
    lm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
    testLocation = lm.getLocationByName(self.locationName)
    if (testLocation == None):
      print ('Location ({}) does not exist'.format(self.locationName))
      return False  # done error!
    for trackName in self.trackNames:
      testTrack = testLocation.getTrackByName(trackName, None)
      if (testTrack == None):
        print ('Track ({}) does not exist at location ({}).'.format(trackName, self.locationName))
        return False  # done error!

    # check car types entered
    ct = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarTypes)
    for carTypeName in self.carTypeNames:
      if (ct.containsName(carTypeName) == False):
        print ('Car type({}) not found'.format(carTypeName))
        return False

    # check car loads entered
    clm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarLoads)
    for carTypeName in self.carTypeNames:
      for i in range(0, len(self.carLoadNames)):
        if (clm.containsName(carTypeName, self.carLoadNames[i]) == False):
          print ('Car load ({}) not found for car type ({})'.format(self.carLoadNames[i], carTypeName))
          return False

    # check car replace loads entered
    for carTypeName in self.carTypeNames:
        for i in range(0, len(self.carReplaceLoads)):
          if (clm.containsName(carTypeName, self.carReplaceLoads[i]) == False):
            print ('Car replace load ({}) not found for car type ({})'.format(self.carReplaceLoads[i], carTypeName))
            return False

    # get a list of cars from the manager
    carList = cm.getByIdList()
    # print ('Found", carList.size(), "cars in roster"
    
    countCars = 0
    
    # determine if the minimum number of cars are available
    for car in carList:
      if (self.number > 0):
        if (car.getTypeName() in self.carTypeNames):
          if (car.getLocationName() == self.locationName):
            for replaceLoadName in self.carReplaceLoads:
              if (car.getLoadName() == replaceLoadName):
                for trackName in self.trackNames:
                  if (car.getTrackName() == trackName):
                      countCars = countCars + 1
                      
    print ('Found {} cars with the correct type and load'.format(countCars))
    
    if (countCars < self.minNumber):
        return False

    # index through new car loads
    i = 0
    carsForKernel = []

    for car in carList:
      if (self.number > 0):
        if (car.getTypeName() in self.carTypeNames):
          if (car.getLocationName() == self.locationName):
            for replaceLoadName in self.carReplaceLoads:
              if (car.getLoadName() == replaceLoadName):
                for trackName in self.trackNames:
                  if (car.getTrackName() == trackName):
                    print ('Car ({}) at location ({}) track ({}) type ({}) old load ({}) new load ({})'.format(car.toString(), self.locationName, trackName, car.getTypeName(), car.getLoadName(), self.carLoadNames[i]))
                    carsForKernel.append(car)
                    car.setLoadName(self.carLoadNames[i])
                    self.number = self.number - 1
                    i = i + 1
                    if (i >= len(self.carLoadNames)):
                      i = 0

    # put cars in a kernel?
    # get the kernel manager
    kernelManager = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.KernelManager)
    kernel = None
    # index kernel car types
    for carTypes in self.carTypesKernel:
      for carType in carTypes:
        foundCar = False
        for car in carsForKernel:
          if (car.getKernel() == None and car.getTypeName() == carType):
            if (kernel == None):
                kernel = kernelManager.newKernel(self.regex + car.toString())
            car.setBlocking(kernel.getSize())
            car.setKernel(kernel)
            print ('Car ({}) type ({}) assigned to kernel ({}) blocking {}'.format(car.toString(), car.getTypeName(), kernel.getName(), car.getBlocking()))
            foundCar = True
            break
        if foundCar == True:
          break
      if foundCar == False:
        break
       
    if kernel == None:
      print('No cars assigned to kernel')
        
    return False  # all done, don't repeat again


loadCars().start()  # create one of these, and start it running
