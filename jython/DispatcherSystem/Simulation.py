# Script to automatically Simulate Dispatched trains
#
# Author: Bill Fitch, copyright 2020
# Part of the JMRI distribution

trains_being_simulated = java.util.concurrent.CopyOnWriteArrayList()    # list of trains actively being simulated
auto_trains_list = java.util.concurrent.CopyOnWriteArrayList()
active_trains_list = java.util.concurrent.CopyOnWriteArrayList()
active_train_name_list = java.util.concurrent.CopyOnWriteArrayList()

removetrain = {}    # dictionary of pairs [train, boolean value]   the value will be set to true if we want to delete the train from auto_trains_list

class SimulationMaster(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self):
        self.logLevel = 0

    def setup(self):
        if self.logLevel > 0: print "starting SimulationMaster setup"
        
        self.simulation_master_sensor = sensors.getSensor("simulateSensor")

        # #get dictionary of buttons self.button_dict
        # self.get_buttons()
        # #set all move_to buttons inactive
        # for sensor in self.button_sensors:
            # sensor.setKnownState(INACTIVE)
        # #store the values in a clone
        # #self.store_button_states()
        # # #at moment there are no trains so:
        # self.button_sensors_to_watch = self.button_sensors

        # if self.logLevel > 0: print "self.button_sensors_to_watch_init", [str(sensor.getUserName()) for sensor in self.button_sensors_to_watch]

        # self.sensor_active = None
        if self.logLevel > 0: print "finished SimulationMaster setup"
        #self.testRoutines()
        return True
        
    def testRoutines(self):
        if self.logLevel > 0: print "locations"

            
    def handle(self):
        
        self.waitMsec(5000)
        
        ###########################################
        # update the activetrainlist (trains in DispatcherFrame)
        # for each train in the list
        #   if in list of trains being simulated, but the train has stopped (not in dispatch list)
        #       remove from trains being simulated
        #   if it is not in trains being simulated, and the train is in the dispatch list
        #       add to trains being simulated
        #       start simulate train thread if the train is running
        #           get blocks on transit
        #           get occupied blocks on transit => blocklist
        #           call simulate train
        ###########################################
        
        self.waitSensorState(self.simulation_master_sensor, ACTIVE)
        
        # get list of dispatched trains
        if self.logLevel > 0: print("checking dispatched trains")
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        autoTrainFrame = DF.getAutoTrainsFrame()
        if autoTrainFrame != None:

            # update the activetrainlist with all the trains currently being dispatched
            active_trains_list.clear()
            active_train_name_list.clear()
            for atrain in autoTrainFrame.getAutoTrainsList():
                activeTrain = atrain.getActiveTrain()
                if activeTrain not in active_trains_list:
                    activeTrainName = activeTrain.getActiveTrainName()
                    active_trains_list.add(activeTrain)
                    active_train_name_list.add(activeTrainName)
                
            # if train in trains_being simulated not being dispatched remove from list
            [trains_being_simulated.remove(atn) for atn in trains_being_simulated if atn not in active_train_name_list]
                
            for activeTrain in active_trains_list:
                activeTrainName = activeTrain.getActiveTrainName()
                    
                # if train being dispatched not in train_being simulated, and train is RUNNING simulate it
                if activeTrainName not in trains_being_simulated:   # do not want to suimulate a train already being simulated
                    if self.logLevel > 0: print "!!!!!!activeTrainName started simulation = " , activeTrain, "activeTrainName", activeTrainName,"trains_being_simulated", [train for train in trains_being_simulated]
                    
                    if activeTrain.getStatus() == activeTrain.RUNNING:  #only simulate if the train is running 
                        # add to trains being simulated
                        trains_being_simulated.append(activeTrainName)
                        
                        if self.logLevel > 0: print "!!!!!$activeTrainName = " , activeTrainName, "trains_being_simulated", [train for train in trains_being_simulated]
                        if self.logLevel > 0: print "active train " , activeTrain
                        #transit = activeTrain.getTransit()
                        # AllocatedSectionList = activeTrain.getAllocatedSectionList()
                        startBlock = activeTrain.getStartBlock()
                        transit = activeTrain.getTransit()
                        DestBlockList = transit.getDestinationBlocksList(startBlock,False)
                        for block in DestBlockList:
                            if self.logLevel > 0: print "destblocklist", block, block.getUserName(), self.blockOccupied(block)
                            
                        # set up the list of occupied blocks  (blocklist) 
                        # startblock is outside the transit DestBlockList contains the blocks in the transit
                        blocklist = []
                        if self.blockOccupied(startBlock):
                            blocklist.append(startBlock)               
                        for block in reversed(DestBlockList) :
                            if self.blockOccupied(block):
                                blocklist.append(block)    
                        for block in blocklist:
                            if self.logLevel > 0: print "occupied blocks", block, block.getUserName(), self.blockOccupied(block)
                        
                        # simulate the train
                        self.simulate_train(blocklist, activeTrain, activeTrainName)
                        
                        if self.logLevel > 0: print "********************"
                        if self.logLevel > 0: print "start block ",startBlock, self.blockOccupied(startBlock), " start block sequence no ", activeTrain.getStartBlockSectionSequenceNumber(), " end block sequence no ", activeTrain.getEndBlockSectionSequenceNumber()
                    else:
                        if self.logLevel > 0: print "attempted to simulate train ", activeTrainName, "but train not running: status" , activeTrain.getStatus()
            else:
                if self.logLevel > 0: print "No active trains"
        else:
            if self.logLevel > 0: print "No active trains"
            if self.logLevel > 0: print "trains_being_simulated before",trains_being_simulated
              
        return True
        
    def simulate_train(self, block_list, activeTrain, activeTrainName):
        simulate_instance = Simulate_instance(block_list, activeTrain, activeTrainName)
        instanceList.append(simulate_instance)
        if simulate_instance.setup():
            simulate_instance.setName(activeTrainName + "_simulation")
            simulate_instance.start() 
                
    def blockOccupied(self, block):
        if block.getState() == ACTIVE:
            state = "ACTIVE"
        else:
            state ="INACTIVE"
        return state        
            
        
class Simulate_instance(jmri.jmrit.automat.AbstractAutomaton):

        ###########################################
        # repeat
        #   take block off end or add one to start max no blocks == 2
        #   
        # make_first_block_unoccupied
        #
        #
        # make_next_block_occupied
        #   if at end returm FINISHED
        #   if not runnin go into wait state until running
        #   then set next block occupied and return SUCCESS
        #     
        ###########################################
    
    def __init__(self, block_list, activeTrain, activeTrainName):
        #global trains_being_simulated
        self.block_list = block_list
        self.activeTrain = activeTrain
        self.activeTrainName = activeTrainName
        #trains_being_simulated.append(activeTrainName)
        self.logLevel = 1
        if self.logLevel > 0: print 'Simulate_instance' + activeTrainName + activeTrain.getTrainName() + activeTrain.getActiveTrainName()

    def setup(self):
        i = 0
        for block in self.block_list:
            #print "block", block.getUserName()
            if block.getState() == ACTIVE:
                index = i 
                #print "Success occupied block is", "index",index
            i+=1
        self.start_position = index
        self.end_position = index
        return True

    def handle(self):
    
        global removetrain
        
        if self.number_blocks_occupied(self.block_list) >1:
            msg = "make_first_block_unoccupied"
            title = self.activeTrainName
            #JOptionPane.showMessageDialog(None, msg, title, JOptionPane.WARNING_MESSAGE)
            self.make_first_block_unoccupied(self.block_list)
            response = "Success"
        else:
            msg = "make_next_block_occupied"
            if self.logLevel > 0: print "make_next_block_occupied"
            title = self.activeTrainName
            #JOptionPane.showMessageDialog(None, msg, title, JOptionPane.WARNING_MESSAGE)
            response = "Waiting"
            while response == "Waiting":
                response = self.make_next_block_occupied(self.block_list)
                if self.logLevel > 0: print "response = " , response                
                if response == "Waiting":
                    if self.logLevel > 0: print "waiting"
                    self.waitMsec(500)

        if response == "Finished":
            if self.logLevel > 0: print "FINISHED"
            # (Note train will be removed from trains_being_simulated by looking at the dispatched train list 
            # which will have the train removed when the dispatch finishes)
            return False
        else:
            self.waitMsec(1000)
            # msg = "click to move to next step"
            # title = self.activeTrainName
            # JOptionPane.showMessageDialog(None, msg, title, JOptionPane.WARNING_MESSAGE)    # uncomment for debugging
            if self.logLevel > 0: print "SUCCESS"
            return True
        
    def number_blocks_occupied(self, block_list):
        return (self.end_position - self.start_position) +1
        
    def blockOccupied(self, block):
        if block.getSensor().getKnownState() == ACTIVE:
            state = "ACTIVE"
        else:
            state ="INACTIVE"
        return state
        
    def make_first_block_unoccupied(self, block_list):
        if self.logLevel > 0: print ("******make_first_block_unoccupied",self.activeTrainName, self.getPrintStatus(self.activeTrain.getStatus()), self.activeTrain.getTrainName(),"\n")
        block_list[self.start_position].getSensor().setState(INACTIVE)
        if self.logLevel > 0: print "Success", "Set block ", self.start_position, "inactive"
        self.start_position += 1
        
    def make_next_block_occupied(self, block_list):
        title = "debug"
        if self.logLevel > 0: print ("******make_next_block_occupied",self.activeTrainName, self.getPrintStatus(self.activeTrain.getStatus()), self.activeTrain.getTrainName(),"\n")
        if self.end_position == len(block_list)-1:
            if self.logLevel > 0: print "finished"
            ret = "Finished"
        else:
            if self.activeTrain.getStatus() == self.activeTrain.RUNNING:
                self.end_position +=1
                                                
                block_list[self.end_position].getSensor().setState(ACTIVE)
                if self.logLevel > 0: print "Success", "Set block ", self.end_position
                ret = "Success"
            else:
                #msg = "status is not Running, hence not proceeding " + str(self.activeTrain.getStatusText())
                #title = self.activeTrainName
                #JOptionPane.showMessageDialog(None, msg, title, JOptionPane.WARNING_MESSAGE)      # uncomment for debugging
                if self.logLevel > 0: print "Waiting"
                ret = "Waiting"
        self.waitMsec(1500)     # to stop an error message
        return ret
        
    def getPrintStatus(self, status):
        if status == self.activeTrain.RUNNING:
            return "status = running"
        else:
            return "status = not running"
