# Simulator for Dispatcher's AutoActiveTrains
#   while auto train(s) are "moving", repeatedly activate "next" allocated block, and deactivate "last" occupied block
#   waits for sensor changes plus a bit, to allow signals, etc. to respond.
#   Runs as a background thread, ends itself when no trains are found in Dispatcher Active Trains list.

# NOTE: to enable logging, add "log4j.category.jmri.jmrit.jython.exec=DEBUG" to default.lcf

import jmri
import datetime
from org.slf4j import Logger;
from org.slf4j import LoggerFactory;

log = LoggerFactory.getLogger("jmri.jmrit.jython.exec.AutoActiveTrains_Simulator");
minLoopMS = 2000        # minimum time in ms allowed for one loop
extraDelayMS = 250      # extra time in ms for processing

# Optional control sensor.  If it exists, the main loop can be paused and resumed by changing
# sensor state.  When there are no active trains, the sensor will be set Inactive and the
# thread will wait for the sensor to become Active again.
controlSensorName = ''

# create a new class to run as thread
class AutoActiveTrains_Simulator(jmri.jmrit.automat.AbstractAutomaton) :

    def init(self):
        self.controlSensor = sensors.getSensor(controlSensorName)

    def handle(self):
        if self.controlSensor is not None: self.waitSensorActive(self.controlSensor)
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        trainsList=DF.getActiveTrainsList() #loop thru all trains
        if (trainsList.size() == 0): # kill the thread if no trains found TODO: add something outside to restart
            if self.controlSensor is not None:
                self.controlSensor.setKnownState(INACTIVE)
                return True
            else:
                log.info("AutoActiveTrains_Simulator thread ended")
                return False # no trains, end
        start_time = datetime.datetime.now()
        # loop through all trains
        for i in range(trainsList.size()):
            at = trainsList.get(i) #: :type at: ActiveTrain
            if (at.getAutoRun()): #ignore if not auto
                aat=at.getAutoActiveTrain()
                targetSpeed = aat.getTargetSpeed()
                bl = at.getBlockList()
                lastBlock = None #most-rear occupied block of train
                nextBlock = None #first unoccupied block allocated to train
                occupiedBlocks = 0
                for j in range(bl.size()): #look for first NOT-allocated block (may be NONE)
                    b = bl.get(j)
                    if (b.getState()==jmri.Block.OCCUPIED):
                        if (lastBlock==None):
                            lastBlock = b
                        occupiedBlocks += 1
                    elif (occupiedBlocks > 0): # ignore any initial unoccupied blocks
                        nextBlock = b
                        break
                log.debug(at.getTrainName() + ": occupiedBlocks: " + str(occupiedBlocks)
                         + " next:" + ("None" if (nextBlock==None) else str(nextBlock.getDisplayName()))
                         + " last:" + ("None" if (lastBlock==None) else str(lastBlock.getDisplayName()))
                         + " speed:" + str(targetSpeed))
                if ((nextBlock != None) and (targetSpeed > 0)): # occupy next block if moving
                    s = nextBlock.getSensor()
                    sn = s.getSystemName()
                    s = sensors.getSensor(sn)
                    if s.getKnownState() != ACTIVE:
                        log.debug(at.getTrainName() + ": set {} ON  for block {}", sn,
                                nextBlock.getDisplayName())
                        s.setKnownState(ACTIVE)
                        self.waitSensorActive(s)
                        self.waitMsec(extraDelayMS)  # extra time for handling change
                if occupiedBlocks > 1: # unoccupy trailing block TODO: change this to check train length
                    s = lastBlock.getSensor()
                    sn = s.getSystemName()
                    s = sensors.getSensor(sn)
                    if s.getKnownState() != INACTIVE:
                        log.debug(at.getTrainName() + ": set {} OFF for block {}", sn,
                                lastBlock.getDisplayName())
                        s.setKnownState(INACTIVE)
                        self.waitSensorInactive(s)
                        self.waitMsec(extraDelayMS)  # extra time for handling change
        #pause for at least min specified time
        elapsedMS = int((datetime.datetime.now() - start_time).total_seconds() * 1000)
        if elapsedMS < minLoopMS:
            self.waitMsec(minLoopMS - elapsedMS)

        return True # keep looping

aats = AutoActiveTrains_Simulator("AutoActiveTrains_Simulator") # setup the thread class
aats.start() # run until it ends itself
