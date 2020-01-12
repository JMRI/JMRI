# Simulator for Dispatcher's AutoActiveTrains
#   while auto train(s) are "moving", repeatedly activate "next" allocated block, and deactivate "last" occupied block
#   waits for debounce time plus a bit, to allow signals, etc. to respond.
#   Runs as a background thread, ends itself when no trains are found in Dispatcher Active Trains list.
 
# NOTE: to enable logging, add "log4j.category.jmri.jmrit.jython.exec=DEBUG" to default.lcf 

import jmri
import time
from org.slf4j import Logger;
from org.slf4j import LoggerFactory;

log = LoggerFactory.getLogger("jmri.jmrit.jython.exec.AutoActiveTrains_Simulator");
    
# create a new class to run as thread
class AutoActiveTrains_Simulator(jmri.jmrit.automat.AbstractAutomaton) :
#   def init(self):

    def handle(self):
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        trainsList=DF.getActiveTrainsList() #loop thru all trains
        if (trainsList.size() == 0): # kill the thread if no trains found TODO: add something outside to restart
            log.info("AutoActiveTrains_Simulator thread ended")
            return False # no trains, end
        totDelay = 0 # keep track of delay time to give CPU some time for other stuff
        for i in range(trainsList.size()):
            at = trainsList.get(i) #: :type at: ActiveTrain
            #log.debug("ActiveTrain: "+ at.getTrainName() + " auto:" + str(at.getAutoRun()))
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
                log.debug(at.getTrainName() + " occupiedBlocks: " + str(occupiedBlocks)
                         + " next:" + ("None" if (nextBlock==None) else str(nextBlock.getDisplayName()))
                         + " last:" + ("None" if (lastBlock==None) else str(lastBlock.getDisplayName()))
                         + " speed:" + str(targetSpeed))
                if ((nextBlock != None) and (targetSpeed > 0)): # occupy next block if moving
                    s = nextBlock.getSensor()
                    sn = s.getSystemName()
                    s = sensors.getSensor(sn)
                    if s.getKnownState() != ACTIVE:
                        delay = s.getSensorDebounceGoingActiveTimer()
                        log.debug(at.getTrainName() + ": set {} ON  for block {}, debounce {}", sn, 
                                nextBlock.getDisplayName(), str(delay))
                        s.setKnownState(ACTIVE)
                        totDelay += delay + 200
                        time.sleep((delay + 200)/1000.0)  # wait for sensor to debounce
                if occupiedBlocks > 1:
                    s = lastBlock.getSensor()
                    sn = s.getSystemName()
                    s = sensors.getSensor(sn)
                    if s.getKnownState() != INACTIVE:
                        delay = s.getSensorDebounceGoingInActiveTimer()
                        log.debug(at.getTrainName() + ": set {} OFF for block {}, debounce {}", sn, 
                                lastBlock.getDisplayName(), str(delay))
                        s.setKnownState(INACTIVE)   
                        totDelay += delay + 500
                        time.sleep((delay + 500)/1000.0)  # wait for sensor to debounce
        if totDelay < 1500:
            time.sleep((1500 - totDelay)/1000.0)  # sleep for at least 1 second
            
        return True         
            
aats = AutoActiveTrains_Simulator("AutoActiveTrains_Simulator") # setup the thread class
aats.start() # run until it ends itself
