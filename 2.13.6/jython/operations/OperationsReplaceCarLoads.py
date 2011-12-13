# Sample script showing how to replace car loads at a location.
# Used in operations.  This script will load all cars of a
# given type at a location and track with the car loads you specify.
#
#
# Author: Daniel Boudreau, copyright 2011
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision$
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
# The car type, (see self.carType, replace carTypeName)
#
# Car loads, (see self.carLoads, replace carLoadName1, carLoadName2, carLoadName3, etc)
# The first car will be loaded with carLoadName1, the second carLoadName2, etc, until
# all loads are used, and then will begin again with carLoadName1.
#
# The car loads you want to replace, (see self.carReplaceLoads)
#
# The number of car loads you want to replace. (see self.number)


import jmri

class loadCars(jmri.jmrit.automat.AbstractAutomaton):      
  def init(self):
  
    # location (use location name)
    self.locationName = "locationName"
    
    # tracks (use track names) at location to generate loads
    self.trackNames = ["trackName1", "trackName2",  "trackName3", "trackName4"]
    
    # car type (use car type name)
    self.carType = "carTypeName"
    
    # car loads to generate (use load names)
    self.carLoads = ["carLoadName1", "carLoadName2", "carLoadName3", "carLoadName4"]
    
    # car loads to replace
    self.carReplaceLoads = ["L", "E"]  
    
    # maximum number of loads to replace
    self.number = 100
    
    return

  def handle(self):   
  	
    # get the car manager
    cm = jmri.jmrit.operations.rollingstock.cars.CarManager.instance()
    
    # the following code checks the values entered
    # first check the location and tracks
    lm = jmri.jmrit.operations.locations.LocationManager.instance()
    testLocation = lm.getLocationByName(self.locationName)
    if (testLocation == None):
      print "Location (", self.locationName, ") does not exist"
      return False		# done error!
    for trackName in self.trackNames:
      testTrack = testLocation.getTrackByName(trackName, None)
      if (testTrack == None):
        print "Track (", trackName, ") does not exist at location (", self.locationName, ")"
        return False		# done error!
        
    # check car type entered
    ct = jmri.jmrit.operations.rollingstock.cars.CarTypes.instance()
    if (ct.containsName(self.carType) == False):
      print "Car type(", self.carType, ") not found"
      return False 
      
    # check car loads entered
    clm = jmri.jmrit.operations.rollingstock.cars.CarLoads.instance()
    for i in range(0, len(self.carLoads)):
      if (clm.containsName(self.carType, self.carLoads[i]) == False):
        print "Car load (", self.carLoads[i], ") not found for car type (", self.carType, ")"
        return False 
        
    # check car replace loads entered
    for i in range(0, len(self.carReplaceLoads)):
      if (clm.containsName(self.carType, self.carReplaceLoads[i]) == False):
        print "Car replace load (", self.carReplaceLoads[i], ") not found for car type (", self.carType, ")"
        return False 
              
    # get a list of cars from the manager
    carList = cm.getByIdList()
    # print "Found", carList.size(), "cars in roster"
    
    # index through new car loads
    i = 0
    
    for carId in carList:
      if (self.number > 0):
        car = cm.getById(carId)
        if (car.getType() == self.carType):
          if (car.getLocationName() == self.locationName):
            for replaceLoadName in self.carReplaceLoads:
              if (car.getLoad() == replaceLoadName):
                for trackName in self.trackNames:
                  if (car.getTrackName() == trackName):
                    print "Car (", car.toString(), ") at location (", self.locationName, ") track (", trackName, ") type (", self.carType, ") old load (", car.getLoad(), ") new load (", self.carLoads[i], ")"
                    car.setLoad(self.carLoads[i])
                    self.number = self.number - 1
                    i = i + 1
                    if (i >= len(self.carLoads)):
                      i = 0

    return False              # all done, don't repeat again

loadCars().start()          # create one of these, and start it running
