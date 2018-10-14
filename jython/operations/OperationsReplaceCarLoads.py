# Sample script showing how to replace car loads at a location.
# Used in operations.  This script will load all cars of a
# given type at a location and track with the car loads you specify.
#
#
# Author: Daniel Boudreau, copyright 2011
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


import jmri

class loadCars(jmri.jmrit.automat.AbstractAutomaton):
  def init(self):

    # location (use location name)
    self.locationName = "locationName"

    # tracks (use track names) at location to generate loads
    self.trackNames = ["trackName1", "trackName2",  "trackName3", "trackName4"]

    # car type (use car type name)
    self.carTypeName = "carTypeName"

    # car loads to generate (use load names)
    self.carLoadNames = ["carLoadName1", "carLoadName2", "carLoadName3", "carLoadName4"]

    # car loads to replace
    self.carReplaceLoads = ["L", "E"]

    # maximum number of loads to replace
    self.number = 100

    return

  def handle(self):

    # get the car manager
    cm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarManager)

    # the following code checks the values entered
    # first check the location and tracks
    lm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
    testLocation = lm.getLocationByName(self.locationName)
    if (testLocation == None):
      print "Location (", self.locationName, ") does not exist"
      return False      # done error!
    for trackName in self.trackNames:
      testTrack = testLocation.getTrackByName(trackName, None)
      if (testTrack == None):
        print "Track (", trackName, ") does not exist at location (", self.locationName, ")"
        return False        # done error!

    # check car type entered
    ct = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarTypes)
    if (ct.containsName(self.carTypeName) == False):
      print "Car type(", self.carTypeName, ") not found"
      return False

    # check car loads entered
    clm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarLoads)
    for i in range(0, len(self.carLoadNames)):
      if (clm.containsName(self.carTypeName, self.carLoadNames[i]) == False):
        print "Car load (", self.carLoadNames[i], ") not found for car type (", self.carTypeName, ")"
        return False

    # check car replace loads entered
    for i in range(0, len(self.carReplaceLoads)):
      if (clm.containsName(self.carTypeName, self.carReplaceLoads[i]) == False):
        print "Car replace load (", self.carReplaceLoads[i], ") not found for car type (", self.carTypeName, ")"
        return False

    # get a list of cars from the manager
    carList = cm.getByIdList()
    # print "Found", carList.size(), "cars in roster"

    # index through new car loads
    i = 0

    for car in carList:
      if (self.number > 0):
        if (car.getTypeName() == self.carTypeName):
          if (car.getLocationName() == self.locationName):
            for replaceLoadName in self.carReplaceLoads:
              if (car.getLoadName() == replaceLoadName):
                for trackName in self.trackNames:
                  if (car.getTrackName() == trackName):
                    print "Car (", car.toString(), ") at location (", self.locationName, ") track (", trackName, ") type (", self.carTypeName, ") old load (", car.getLoadName(), ") new load (", self.carLoadNames[i], ")"
                    car.setLoadName(self.carLoadNames[i])
                    self.number = self.number - 1
                    i = i + 1
                    if (i >= len(self.carLoadNames)):
                      i = 0

    return False              # all done, don't repeat again

loadCars().start()          # create one of these, and start it running
