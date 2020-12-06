
trains_being_simulated = java.util.concurrent.CopyOnWriteArrayList()    # list of trains actively being simulated
auto_trains_list = java.util.concurrent.CopyOnWriteArrayList()

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
        global removetrain
        self.waitSensorState(self.simulation_master_sensor, ACTIVE)
        #check active trains
        if self.logLevel > 0: print("checking active trains")
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        autoTrainFrame = DF.getAutoTrainsFrame()
        if autoTrainFrame != None:
            auto_trains_list.clear()
            for atrain in autoTrainFrame.getAutoTrainsList():
                if atrain not in auto_trains_list:
                    auto_trains_list.add(atrain)
            for autoactivetrain in auto_trains_list:
                activeTrain = autoactivetrain.getActiveTrain()
                activeTrainName = activeTrain.getActiveTrainName()
                removetrain[activeTrainName] = False     # add the activeTrainName key
            # #if autoTrainFrame != None:
            # if len(trains_being_simulated) !=0:
                if activeTrainName not in trains_being_simulated:   # do not want to suimulate a train already being simulated
                    # #when we simulate a train we will add to this list, and remove when finished
                    # pass
                # else:
                    if self.logLevel > 0: print "!!!!!!activeTrainName = " , activeTrainName, "trains_being_simulated", [train for train in trains_being_simulated]
                    if activeTrain.getStatus() == activeTrain.RUNNING:  #only simulate if the train is running 
                        trains_being_simulated.append(activeTrainName)
                        if self.logLevel > 0: print "!!!!!$activeTrainName = " , activeTrainName, "trains_being_simulated", [train for train in trains_being_simulated]
                        if self.logLevel > 0: print "active train " , activeTrainName
                        transit = activeTrain.getTransit()
                        # AllocatedSectionList = activeTrain.getAllocatedSectionList()
                        startBlock = activeTrain.getStartBlock()
                        transit = activeTrain.getTransit()
                        DestBlockList = transit.getDestinationBlocksList(startBlock,False)
                        for block in DestBlockList:
                            if self.logLevel > 0: print "destblocklist", block, block.getUserName(), self.blockOccupied(block)
                        blocklist = []
                        if self.blockOccupied(startBlock):
                            blocklist.append(startBlock)
                        occupied = False
                        for block in reversed(DestBlockList) :
                            if self.blockOccupied(block) or occupied:
                                blocklist.append(block)
                                occupied = True
                        #we now have the list of blocks to traverse
                        for block in blocklist:
                            if self.logLevel > 0: print "traverselist", block, block.getUserName(), self.blockOccupied(block)
                        
                        self.simulate_train(blocklist, activeTrain, activeTrainName)
                        if self.logLevel > 0: print "********************"
                        if self.logLevel > 0: print "start block ",startBlock, self.blockOccupied(startBlock), " start block sequence no ", activeTrain.getStartBlockSectionSequenceNumber(), " end block sequence no ", activeTrain.getEndBlockSectionSequenceNumber()
                        # if activeTrain != None:
                            # for allocatedSection in AllocatedSectionList:
                                # section = allocatedSection.getSection()
                                # section_direction = allocatedSection.getDirection()
                                # if self.logLevel > 0: print "allocated sections ", section, allocatedSection.getSectionName(), " direction ", section_direction
                                # for block in section.getBlockList():
                                    # if block.getState() == ACTIVE:
                                        # state = "ACTIVE"
                                    # else:
                                        # state ="INACTIVE"
                                    # if self.logLevel > 0: print "block list ", block.getUserName(), state 
                    else:
                        if self.logLevel > 0: print "status" , activeTrain.getStatus()
            else:
                if self.logLevel > 0: print "No active trains"
        else:
            if self.logLevel > 0: print "No active trains"
            
        #do this outside loop to avoid concurrency issues
        # for myActiveTrainName in removetrain.keys
            # removeTrainItem[myActiveTrainName] == True: 
        # trains_being_simulated.remove(myActiveTrainName)
        
        #remove the train from 
        [trains_being_simulated.remove(myActiveTrainName) for myActiveTrainName in removetrain.keys() if  removetrain[myActiveTrainName] == True]
        #print "trains_being_simulated",trains_being_simulated
        removetrain = {key:val for key, val in removetrain.items() if val == False}
        #print "removetrain",removetrain
        self.waitMsec(1000)
            
        return True
        
    def simulate_train(self, block_list, activeTrain, activeTrainName):
        simulate_instance = Simulate_instance(block_list, activeTrain, activeTrainName)
        instanceList.append(simulate_instance)
        if simulate_instance.setup():
            simulate_instance.setName(activeTrainName + "_simulation")
            simulate_instance.start() 
        
    # def traverse(self, block_list):

            
        # if number_blocks_occupied(block_list) >1:
            # make_first_block_unoccupied(block_list)
        # else:
            # if make_another_block_occupied(block_list) == false:
                # return
        # delay(1000)
        
    def blockOccupied(self, block):
        if block.getState() == ACTIVE:
            state = "ACTIVE"
        else:
            state ="INACTIVE"
        return state        
            
        

        
class Simulate_instance(jmri.jmrit.automat.AbstractAutomaton):
    
    def __init__(self, block_list, activeTrain, activeTrainName):
        #global trains_being_simulated
        self.block_list = block_list
        self.activeTrain = activeTrain
        self.activeTrainName = activeTrainName
        #trains_being_simulated.append(activeTrainName)
        self.logLevel = 0
        if self.logLevel > 0: print 'Simulate_instance' + activeTrainName

    def setup(self):
        self.start_position = 0
        self.end_position = 0
        return True

    def handle(self):
        global removetrain
        if self.number_blocks_occupied(self.block_list) >1:
            msg = "make_first_block_unoccupied"
            title = self.activeTrainName
            #JOptionPane.showMessageDialog(None, msg, title, JOptionPane.WARNING_MESSAGE)
            self.make_first_block_unoccupied(self.block_list)
        else:
            msg = "make_next_block_occupied"
            title = self.activeTrainName
            #JOptionPane.showMessageDialog(None, msg, title, JOptionPane.WARNING_MESSAGE)
            if self.make_next_block_occupied(self.block_list) == False:
                while self.activeTrain.getStatus() == self.activeTrain.RUNNING:
                    self.waitMsec(100)
                # need towait till train has closed down completely. May be able to do this a bit neater but can't at moment
                #self.waitMsec(10000)  
                #trains_being_simulated.remove(self.activeTrainName)
                removetrain[self.activeTrainName] = True
                #print "instance stopped removetrain",removetrain
                return False
        self.waitMsec(1000)
        msg = "click to move to next step"
        title = self.activeTrainName
        #JOptionPane.showMessageDialog(None, msg, title, JOptionPane.WARNING_MESSAGE)
        return True
        
    def number_blocks_occupied(self, block_list):
        return (self.end_position - self.start_position) +1
        # count = 0
        # for block in block_list:
            # if blockOccupied(block) == "ACTIVE":
                # count  +=1
        # return count
        
    def blockOccupied(self, block):
        if block.getSensor().getKnownState() == ACTIVE:
            state = "ACTIVE"
        else:
            state ="INACTIVE"
        return state
        
    def make_first_block_unoccupied(self, block_list):
        block_list[self.start_position].getSensor().setState(INACTIVE)
        self.start_position += 1
        
    def make_next_block_occupied(self, block_list):
        if self.end_position == len(block_list)-1:
            return False
        else:
            if self.activeTrain.getStatus() == self.activeTrain.RUNNING:
                self.end_position +=1
                if self.element_in_allocation():
                    block_list[self.end_position].getSensor().setState(ACTIVE)
                    return True
            else:
                msg = "status is not Running, hence not proceeding " + str(self.activeTrain.getStatusText())
                title = self.activeTrainName
                JOptionPane.showMessageDialog(None, msg, title, JOptionPane.WARNING_MESSAGE)
                return False
        return True
                
    def element_in_allocation(self):
        return True
        

# ###################################
# # As engines are added from roster add to locomotives
# #
# # We ceate routes with names list of stations the train will visit
# # We create Trains which have start times and travel over routes
# # Move can operate with a clock. At 
# #
# ###################################

  # def clear_locations():
  
  # def create_locations():
    # clear_locations()
    # for station in stations:
        # add location
  
  # def create_route():
    # #ask for name
    # for clicks in route
        # add location  (name station)
        # specify whether in stopping mode or express in comment
        
    
  
    
  # # do by hand
  # def create_train():
    # #ask for descriptive name of train  
    # #ask for route
    # #ask for start time
  
  # def run_trains(option wait):
    
    # at start time of train
    # if a train is in location1 with engine and there is no dispatch in operation for that engine
    # run dispatcher with
        # engine name = engine
        # transit = transit done in same way as we create a transit by pressing the buttons.
    # else
        # ask whether wish to wait till train arrives or cancel
        
    # at each location pause for appropriate time