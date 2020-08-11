import java
import jmri
import re
from javax.swing import JOptionPane
import os
import imp
import copy
import org 

from javax.swing import JOptionPane, JFrame, JLabel, JButton, JTextField, JFileChooser, JMenu, JMenuItem, JMenuBar,JComboBox
# #include the create graph code
# my_dir = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateGraph.py')
# execfile(my_dir)
import sys
my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
sys.path.append(my_path_to_jars) # add the jar to your path
from org.jgrapht.alg import DijkstraShortestPath
from org.jgrapht.graph import DefaultEdge
from org.jgrapht.graph import DirectedMultigraph

logLevel = 4
trains = {}
instanceList=[]
g = None


class OptionDialog( java.lang.Runnable ) :
    #

        
    def variable_combo_box(self, options, default, msg, title = None, type = JOptionPane.QUESTION_MESSAGE):
    
        result = JOptionPane.showInputDialog(
            None,                                   # parentComponent
            msg,                                    # message text
            title,                                  # title
            type,                                   # messageType
            None,                                   # icon
            options,                                # selectionValues
            default                                 # initialSelectionValue
            )
        
        return result  
        
    def displayMessage(self, msg):
        JOptionPane.showMessageDialog(None, msg, 'Message', JOptionPane.WARNING_MESSAGE)
        
    #def JOptionPane.showConfirmDialog(None, msg, title, JOptionPane.YES_NO_OPTION)

        
    # def bell(self, bell_on = "True"):
        # if bell_on == "True":
            # snd = jmri.jmrit.Sound("resources/sounds/Bell.wav")
            # snd.play() 
            
class modifiableJComboBox:

    def __init__(self,list, msg):
        #list = self.get_all_roster_entries_with_speed_profile()
        jcb = JComboBox(list)
        jcb.setEditable(True)
        JOptionPane.showMessageDialog( None, jcb, msg, JOptionPane.QUESTION_MESSAGE);
        self.ans = str(jcb.getSelectedItem())
         
    def return_val(self):
        return self.ans
        
            
            
#        
# instanceList.append(RunDispatch())        
# instanceList[0].start() 
class NewTrainMaster(jmri.jmrit.automat.AbstractAutomaton):

    # responds to the newTrainSensor, and allocates trains available for dispatching
    # we make the allocated flag global as we will use it in DispatchMaster when we dispatch a train
    
    global trains_allocated
    
    #instanceList = []   # List of file based instances
    
    def init(self):
        self.logLevel = 0
        if self.logLevel > 0: print 'Create Stop Thread'

    def setup(self):
        global trains_allocated
        #self.initialise_train()
        newTrainSensor = "newTrainSensor"
        self.new_train_sensor = sensors.getSensor(newTrainSensor)
        if self.new_train_sensor is None:
            return False
        self.new_train_sensor.setKnownState(INACTIVE)
        trains_allocated = []
        return True
        
    def handle(self):
    
        global trains_allocated
        
        #this repeats
        # wait for a sensor requesting to check for new train
        if self.logLevel > 0: print ("wait for a sensor requesting to check for new train")
        
        self.waitSensorActive(self.new_train_sensor)
        self.new_train_sensor.setKnownState(INACTIVE)
        
        station_block_name, new_train_name = self.check_new_train_in_siding()
        if self.logLevel > 0: print "station_block_name",station_block_name, "existing train name", new_train_name
        if station_block_name != None:
            # take actions for new train
            if new_train_name == None:
                # roster_entries_with_speed_profile = self.get_all_roster_entries_with_speed_profile()  
                # if self.logLevel > 0: print roster_entries_with_speed_profile
                # new_train_name = JOptionPane.showInputDialog(None,"Enter engine name", "diesel")
                all_trains = self.get_all_roster_entries_with_speed_profile()
                if all_trains == []:
                    msg = "There are no engines with speed profiles, cannot operate without any"
                    JOptionPane.showMessageDialog(None,msg)
                else:    
                    trains_to_choose = copy.copy(all_trains)
                    for train in trains_allocated:
                        if self.logLevel > 0: print str(trains_to_choose)
                        if train in trains_to_choose:
                            trains_to_choose.remove(train)
                    msg = "In " + station_block_name + " Select train roster"
                    new_train_name = modifiableJComboBox(trains_to_choose,msg).return_val()
                    if new_train_name not in trains_allocated:
                        trains_allocated.append(new_train_name)
                
                    #new_train_flag = self.new_train_thread_required(new_train_name)
                    
                    #if new_train_flag == True :
                        #self.create_new_train_thread(new_train_name)
                    self.add_to_train_list_and_set_new_train_location(new_train_name, station_block_name)
                    self.set_blockcontents(station_block_name, new_train_name)
            else:
                #allow operator to verify the train
                all_trains = self.get_all_roster_entries_with_speed_profile()
                trains_to_choose = copy.copy(all_trains)
                for train in trains_allocated:
                    if self.logLevel > 0: print str(trains_to_choose)
                    if train in trains_to_choose:
                        trains_to_choose.remove(train)
                msg = "In " + station_block_name + " Select train roster"
                new_train_name = modifiableJComboBox(trains_to_choose,msg).return_val()
                if new_train_name not in trains_allocated:
                    trains_allocated.append(new_train_name)
                self.add_to_train_list_and_set_new_train_location(new_train_name, station_block_name)
                self.set_blockcontents(station_block_name, new_train_name)
                
        else:
            if self.logLevel > 0: print "about to show message no new train in siding"
            # if trains_allocated[1:-1] == []:
                # trains_allocated_msg = "None"
            # else:
            trains_allocated_msg = str(trains_allocated)[1:-1]
            title = "Oh Oh"
            msg = "Trains set up:\n"+ trains_allocated_msg + "\n\nNo new train to be allocated\n\nContinue(yes) or reset the allocation if wrong (no)"
            #JOptionPane.showOptionDialog(None,msg)
            result = JOptionPane.showConfirmDialog(None, msg, title, JOptionPane.YES_NO_OPTION)
            if result ==  JOptionPane.NO_OPTION:
                print ("no option")
                # trains_allocated = []
                # OptionDialog().displayMessage("try again")
                self.reset_allocation()
                
                
        return True 
        
    def reset_allocation(self):
        global trains_allocated
        if trains_allocated == []:
            print ("a")
            msg = "Nothing to reset"
            OptionDialog().displayMessage(msg)
        else:
            print ("b")
            msg = "Select train to modify"
            train_name_to_remove = modifiableJComboBox(trains_allocated,msg).return_val()
            trains_allocated.remove(train_name_to_remove)
            self.new_train_sensor.setKnownState(ACTIVE)
        
    
    def get_all_roster_entries_with_speed_profile(self):
        roster_entries_with_speed_profile = []
        r = jmri.jmrit.roster.Roster.getDefault()
        for roster_entry in jmri.jmrit.roster.Roster.getAllEntries(r):
            if self.logLevel > 0: print "roster_entry.getSpeedProfile()",roster_entry,roster_entry.getSpeedProfile()
            if roster_entry.getSpeedProfile() != None:
                roster_entries_with_speed_profile.append(roster_entry.getId())
                if self.logLevel > 0: print "roster_entry.getId()",roster_entry.getId()
        return roster_entries_with_speed_profile
                
                       
    def add_to_train_list_and_set_new_train_location(self, train_name, station_block_name):
        # trains is a dictionary, with keys of the train_name
        # each value is itself a dictioary with 3 items
        # edge
        # penultimate_block_name
        # direction
        global train
        global trains_allocated
        if train_name not in trains:
            trains[train_name] = {}
            train = trains[train_name]
            train["train_name"] = train_name
        else:
            #train_name = self.get_train_name()
            self.set_train_in_block(station_block_name, train_name)
            
        # 2) set the last traversed edge to the edge going into the siding
        for e in g.g_stopping.edgeSet():
            #if self.logLevel > 0: print "************************"
            #if self.logLevel > 0: print e, "Target", e.getTarget()
            #if self.logLevel > 0: print e, "Source", e.getSource()
            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
            station_block = LayoutBlockManager.getLayoutBlock(station_block_name)
            number_neighbors = station_block.getNumberOfNeighbours()
            in_siding = (number_neighbors == 1)
            for i in range(station_block.getNumberOfNeighbours()):
                neighbor_name = station_block.getNeighbourAtIndex(i).getDisplayName()
                if self.logLevel > 0: print e.getItem("penultimate_block_name"), "station_block_name",station_block_name,"neighbor_name",neighbor_name
                if e.getItem("penultimate_block_name") == neighbor_name:
                    edge = e
                    break
        train["edge"] = edge
        train["penultimate_block_name"] = edge.getItem("penultimate_block_name")
        print 
        
        # 3) set direction so can check direction of transit
        
        penultimate_block_name = edge.getItem("penultimate_block_name")
        penultimate_layout_block = LayoutBlockManager.getLayoutBlock(penultimate_block_name)
        saved_state = penultimate_layout_block.getUseExtraColor()
        if not in_siding:
            # highlight the penultimate block
            penultimate_layout_block.setUseExtraColor(True)  
        train["direction"] = self.set_train_direction(station_block_name, in_siding)
        penultimate_layout_block.setUseExtraColor(saved_state)
        
        # 4) add to allocated train list
        if str(train_name) not in trains_allocated:
            trains_allocated.append(str(train_name))
            
    def set_train_direction(self, block_name, in_siding):
    
        options = ["forward", "reverse"]
        default = "forward"
        if in_siding:
            msg = "In block: " + block_name + "\n" +'What way is train facing\nout of junction?'
        else:
            msg = "In block: " + block_name + "\n" +'What way is train facing\ntowards highlighted block?'
        title = "Set Train Facing Direction"
        type = JOptionPane.QUESTION_MESSAGE
    
        result = OptionDialog().variable_combo_box(options, default, msg, title, type)
        
        if result == "forward":
            train_direction = "reverse"
        else:
            train_direction = "forward"
        return train_direction
        
    def set_train_in_block(self, block_name, train_name):
        mem_val = train_name
        self.set_blockcontents(block_name, mem_val)
        
    def check_new_train_in_siding(self):

        # go through all station
        global trains_allocated
        
        for station_block_name in g.station_block_list:
        
            #get a True if the block block_value has the train name in it
            block_value = self.get_blockcontents(station_block_name)
            if self.logLevel > 0: print " a trains_allocated:", trains_allocated, ": block_value", block_value
            
               
            # if block_value != None:
                # if self.is_roster_entry(block_value):
                    # if self.logLevel > 0: print block_value.getId()
                    # block_value = block_value.getId()
            #get a True if the block is occupied
            block_occupied_state = self.check_sensor_state_given_block_name(station_block_name)
            
            if self.logLevel > 0: print ("station block name {} : block_value {}". format(station_block_name, str(block_value)))
            
            #check if the block is occupied and has the required train in it  
            if (block_value == None or block_value == "" or block_value == "none") and block_occupied_state == True:
                return [station_block_name, None]
            elif block_occupied_state == True and (block_value != None and block_value != "" and block_value != "none"):
                #check if there is already a thread for the train
                #check if the train has already been allocated
                #if self.new_train_thread_required(block_value):
                if block_value not in trains_allocated:
                    return [station_block_name, block_value]
                else:
                    if self.logLevel > 0: print "block_value in trains_allocated"
                    if self.logLevel > 0: print "b trains_allocated:", trains_allocated, ": block_value", block_value
                    pass
            else:
                pass
        return [None, None]

    def is_roster_entry(self, v):
        return type(v) is jmri.jmrit.roster.RosterEntry

    # def new_train_thread_required(self, train_name):
        # for thread in instanceList:
                # if thread is not None:
                    # if thread.isRunning():
                        # existing_train_name = thread.getName() 
                        # if existing_train_name == train_name:
                            # return False
        # return True
        
    def train_thread_exists(self, train_name):
        for thread in instanceList:
                if thread is not None:
                    if thread.isRunning():
                        existing_train_name = thread.getName() 
                        if existing_train_name == train_name:
                            return True
        return False
        
    def create_new_train_thread(self, train_name):
        idx = len(instanceList)
        instanceList.append(RunDispatch())          # Add a new instance
        instanceList[idx].setName(train_name)        # Set the instance name
        #if instanceList[idx].setup():               # Compile the train actions
        instanceList[idx].start()               # Compile was successful
        
        
    def get_blockcontents(self, block_name):
        block = blocks.getBlock(block_name)
        value =  block.getValue()
        return value
        
    def set_blockcontents(self, block_name, value):
        block = blocks.getBlock(block_name)
        value =  block.setValue(value)
        

    def check_sensor_state_given_block_name(self, station_block_name):
        #if self.logLevel > 0: print("station block name {}".format(station_block_name))
        layoutBlock = layoutblocks.getLayoutBlock(station_block_name)
        station_sensor = layoutBlock.getOccupancySensor() 
        if station_sensor is None:
            OptionDialog().displayMessage(' Sensor in block {} not found'.format(station_block_name))
            return
        currentState = True if station_sensor.getKnownState() == ACTIVE else False
        return currentState         
            
class StopMaster(jmri.jmrit.automat.AbstractAutomaton):
    
    def init(self):
        self.logLevel = 0
        if self.logLevel > 0: print 'Create Stop Thread'

    def setup(self):
        self.stop_master_sensor = sensors.getSensor("stopMasterSensor")
        if self.stop_master_sensor is None:
            return False
        self.stop_master_sensor.setKnownState(INACTIVE)
        return True

    def handle(self):
        self.waitSensorActive(self.stop_master_sensor)
        #stop all threads
        for thread in instanceList:
            if thread is not None:
                if thread.isRunning():
                    if self.logLevel > 0: print 'Stop "{}" thread'.format(thread.getName())
                    thread.stop()
        self.new_train_sensor = sensors.getSensor("startDispatcherSensor")
        self.new_train_sensor.setKnownState(INACTIVE)
        return False

# End of class StopMaster



class ResetButtonMaster(jmri.jmrit.automat.AbstractAutomaton):
    
    button_sensors_to_watch = []
    def __init__(self):
        self.logLevel = 0
        
    def init(self):
        if self.logLevel > 0: print 'Create ResetButtonMaster Thread'

    def setup(self):
        if self.logLevel > 0: print "starting ResetButtonMaster setup"

        #get dictionary of buttons self.button_dict
        self.get_buttons()
        #set all move_to buttons inactive
        for sensor in self.button_sensors:
            sensor.setKnownState(INACTIVE)
        #store the values in a clone
        #self.store_button_states()
        # #at moment there are no trains so:
        self.button_sensors_to_watch = self.button_sensors

        if self.logLevel > 0: print "self.button_sensors_to_watch_init", [sensor.getUserName() for sensor in self.button_sensors_to_watch]

        self.sensor_active = None
        # #wait for one to go active
        # aJavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        # if self.logLevel > 0: print aJavaList[0].getUserName()
        # self.waitSensorState(aJavaList, jmri.Sensor.ACTIVE)
        # #determine which one changed
        # self.sensor_active = [sensor for sensor in button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
        # #we really need to determine what train it was
          # #just work with one train for now
        # #get the list of sensors to watch
        # self.button_sensors_to_watch = self.button_sensors - [sensor_changed]

        if self.logLevel > 0: print "finished ResetButtonMaster setup"
        return True

    def handle(self):
        #only one button is active. We will keep it that way
        if self.logLevel > 0: print "handle"
        #wait for one to go active
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
        #determine which one changed
        #if self.logLevel > 0: print "sensor went active"
        sensor_changed = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
        #if self.logLevel > 0: print "sensor_changed",sensor_changed.getUserName()
        #set other buttons to inactive
        if self.sensor_active != None:
            self.sensor_active.setKnownState(INACTIVE)
            self.waitMsec(2000)
        #remember the active button
        self.sensor_active = sensor_changed
        #get the list of sensors to watch
        #if self.logLevel > 0: print "self.button_sensors_to_watch_before_remove", [sensor.getUserName() for sensor in self.button_sensors_to_watch]
        self.button_sensors_to_watch.remove(sensor_changed)
        #if self.logLevel > 0: print "self.button_sensors_to_watch", [sensor.getUserName() for sensor in self.button_sensors_to_watch]
        #if self.logLevel > 0: print "end handle"
        return True
       
    
    def get_buttons(self):    
        self.button_sensors = [self.get_button_sensor_given_block_name(station_block_name) for station_block_name in g.station_block_list]
        self.button_sensor_states = [self.check_sensor_state(button_sensor) for button_sensor in self.button_sensors]
        # for button_sensor in self.button_sensors:
            # self.button_dict[button_sensor] = self.check_sensor_state(button_sensor)
            
    def check_sensor_state(self, sensor):
        #if self.logLevel > 0: print("check_sensor_state",sensor)
        if sensor == None :
            #if self.logLevel > 0: print('Sensor in check_sensor_state is none')
            return None
        #sensor = sensors.getSensor(sensor_name)
        if sensor is None:
            OptionDialog().displayMessage('Sensor {} not found'.format( sensor_name))
            return
        currentState = True if sensor.getKnownState() == ACTIVE else False
        #if self.logLevel > 0: print("check_sensor_state {}".format(currentState))
        return currentState
       
    def store_button_states(self):
        self.button_sensor_states_old = copy.copy(self.button_sensor_states)
        if self.logLevel > 0: print "self.button_sensor_states_old",self.button_sensor_states_old
        #self.button_dict_old = dict(self.button_dict)
        
    def get_button_sensor_given_block_name(self, block_name):
        button_sensor_name = "MoveTo"+block_name.replace(" ","_") +"_stored"
        button_sensor = sensors.getSensor(button_sensor_name)
        return button_sensor        
           
    # def reset_buttons(self):
        # for button_dict_old_value, button_dict_value in zip(button_dict_old.iteritems(), button_dict.iteritems()):
            # if button_dict_old_value == button_dict_value:
                # if self.logLevel > 0: print 'Ok', button_dict_old_value, button_dict_value
                # sensor, val = button_dict_old_value
                # if val = True:
                    # sensor.setKnownState(INACTIVE)
            # else:
                # if self.logLevel > 0: print 'Not', button_dict_old_value, button_dict_value
                # sensor, val = button_dict_value
        
    
        
        
        return False

class MoveTrain(jmri.jmrit.automat.AbstractAutomaton):

    global trains_dispatched

    def __init__(self, station_from_name, station_to_name, train_name, graph):
        self.logLevel = 1
        self.station_from_name = station_from_name 
        self.station_to_name = station_to_name
        self.train_name = train_name 
        self.graph = graph

    def setup(self):
        return True

    def handle(self):
        #move between stations in the thread
        if self.logLevel > 0: print"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        if self.logLevel > 0: print "move between stations in the thread"
        if self.logLevel > 0: print"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        self.move_between_stations(self.station_from_name, self.station_to_name, self.train_name, self.graph)
        return False

    def move_between_stations(self, station_from_name, station_to_name, train_name, graph):
        if self.logLevel > 0: print "Moving from " + station_from_name + " to " + station_to_name
        #need to look up the required transit in the graph
        StateVertex_start = station_from_name
        StateVertex_end = station_to_name
        # for e in graph.edgeSet():
            # print (graph.getEdgeSource(e) + " --> " + graph.getEdgeTarget(e))
        print "calling shortest path", StateVertex_start, StateVertex_end
        paths = DijkstraShortestPath.findPathBetween(graph, StateVertex_start, StateVertex_end)
        print "returned from shortest path"
        #if self.logLevel > 0: print paths
        
        train = trains[train_name]
        penultimate_block_name = train["penultimate_block_name"]
        previous_edge = train["edge"]
        previous_direction = train["direction"]
        
        trains_dispatched.append(str(train_name))
        
        for e in paths:
            
            # need to check whether: 
            #   last block of previous edge and current first block 
            #   are the same
            
            # if the same the tranist must change direction
            #
            current_edge = e
            neighbor_name = e.getItem("neighbor_name")
            if self.logLevel > 0: print train
            if self.logLevel > 0: print "neighbor_name = ", neighbor_name
            if self.logLevel > 0: print "penultimate_block_name" , penultimate_block_name
            
            if penultimate_block_name == neighbor_name:
                transit_instruction = "change"
            else:
                transit_instruction = "same"
            if self.logLevel > 0: print "transit_instruction=",transit_instruction
            
            if transit_instruction == "change":
                if previous_direction == "forward":
                    transit_direction = "reverse"
                else:
                    transit_direction = "forward"
            else:
                transit_direction = previous_direction
                
            if self.logLevel > 0: print "transit_direction",transit_direction
                
            result = self.move(e, transit_direction, train_name)
            if self.logLevel > 0: print "returned from self.move, result = ", result
            if result == False:
                trains_dispatched.remove(str(train_name))
                break
            #store the current edge for next move
            train["edge"] = e
            train["penultimate_block_name"] = e.getItem("penultimate_block_name")
            train["direction"] = transit_direction
        if str(train_name) in trains_dispatched:    
            trains_dispatched.remove(str(train_name))

    def move(self, e, direction, train):
        if self.logLevel > 0: print e, "Target", e.getTarget()
        if self.logLevel > 0: print e, "Source", e.getSource()
        to_name = e.getTarget()
        from_name = e.getSource()
        sensor_move_name = "MoveInProgress"+to_name.replace(" ","_")
        
        self.set_sensor(sensor_move_name, "active")
        speech_reqd = self.speech_required_flag()
        self.announce( from_name, to_name, speech_reqd)
        result = self.call_dispatch(e, direction, train)
        if result == True:
            if self.logLevel > 0: print ("Wait for sensor " + sensor_move_name + " to become inactive")
            self.wait_sensor(sensor_move_name,"inactive")
            #
            if self.logLevel > 0: print ("+++++ sensor " + sensor_move_name + " inactive")
            self.waitMsec(5000)
        else:
            self.set_sensor(sensor_move_name, "inactive")
        return result
        
    def speech_required_flag(self):
        self.sound_sensor = sensors.getSensor("soundSensor")
        if self.sound_sensor is None:
            OptionDialog().displayMessage("No sound Sensor set up")
            return None
        sound_state = self.sound_sensor.getKnownState()
        if self.logLevel > 0: print sound_state,ACTIVE
        if sound_state == ACTIVE:
            sound_flag = True
        else:
            sound_flag = False
        return sound_flag

    def call_dispatch(self, e, direction, train):
        if self.logLevel > 0: print ("in dispatch")
        to_name = e.getTarget()
        from_name = e.getSource()
        if self.logLevel > 0: print ("incall_dispatch: move from " + from_name + " to " + to_name)
        
        if direction == "forward":
            filename = self.get_filename(e, "fwd")
            #filename1 = "From " + from_name + " To " + to_name
        else:
            filename = self.get_filename(e, "rvs")
            #filename1 = "From " + from_name + " To " + to_name + " reverse"
        #filename = filename.replace(" ","_")
        
        if self.logLevel > 0: print "filename = ", filename, "direction = " , direction
        result = self.doDispatch(filename, "ROSTER", train)
        if self.logLevel > 0: print "result", result        
        return result

    def get_filename(self, e, suffix):
    
        # suffix is "fwd" or "rvs"
        # graph is g.g_express
        # e is edge
        
        from_station_name = g.g_express.getEdgeSource(e)
        to_station_name = g.g_express.getEdgeTarget(e)
        neighbor_name = e.getItem("neighbor_name")
        index = e.getItem("index")
        
        filename = "From " + str(from_station_name) + " To " + str(to_station_name) + " Via " + str(neighbor_name) + " " + str(index) 
        filename = filename.replace(" ", "_")
        # filename_fwd = filename + "_fwd.xml"
        # filename_rvs = filename + "_rvs.xml"
        filename = filename + "_" + suffix + ".xml"

        return filename    
        
    #    Dispatch (<filename.xml>, [USER | ROSTER | OPERATIONS >,<dccAddress, RosterEntryName or Operations>
        
    def doDispatch(self, traininfoFileName, type, value):
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        #try: 
        print "traininfoFileName",traininfoFileName
        result = DF.loadTrainFromTrainInfo(traininfoFileName, type, value)
        if result == -1:
            if self.logLevel > 0: print "result from dispatcher frame" , result
            return False  #No train allocated
        else:
            if self.logLevel > 0: print "result from dispatcher frame" , result
            return True
        # except:
            # if self.logLevel > 0: print ("FAILURE tried to run dispatcher with file {} type {} value {}".format(traininfoFileName,  type, value))
            # pass
            # return False
   
    def set_sensor(self, sensorName, sensorState):
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('{} - Sensor {} not found'.format(self.threadName, sensorName))
            return
        if sensorState == 'active':
            newState = ACTIVE
        elif sensorState == 'inactive':
            if self.logLevel > 0: print "set_sensor ", sensorName, 'inactive'
            newState = INACTIVE
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))
        sensor.setKnownState(newState)    
        return
        
    def wait_sensor(self, sensorName, sensorState):
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('{} - Sensor {} not found'.format(self.threadName, sensorName))
            return
        if sensorState == 'active':
            self.waitSensorActive(sensor)
        elif sensorState == 'inactive':
            self.waitSensorInactive(sensor)
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))  
            
    ##
    # Loads a train into the Dispatcher from a traininfo file, overriding
    # trainlength to allow train to stop near buffer
    #
    # @param traininfoFileName  the file name of a traininfo file.
    # @param overRideType  "NONE", "USER", "ROSTER" or "OPERATIONS"
    # @param overRideValue  "" , dccAddress, RosterEntryName or Operations
    #            trainname.
    # @param overRideMaxTrainLen
    # @return 0 good, -1 create failure, -2 -3 file errors, -9 bother.
    #
    
    def loadTrainFromTrainInfo(self, traininfoFileName, overRideType, overRideValue, overRideMaxTrainLen = None):
        #read xml data from selected filename and move it into trainfo
        print "got here a"
        #DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        log = org.slf4j.LoggerFactory.getLogger(jmri.jmrit.dispatcher.DispatcherFrame)
        try:
            #maybe called from jthon protect our selves
            print "got here a1"
            tif = jmri.jmrit.dispatcher.TrainInfoFile()
            print "got here b",tif
            TrainInfo = None
            print "got here b11",traininfoFileName
            TrainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(traininfoFileName)
            print "got here b22",TrainInfo
            print dir(TrainInfo)
            count = 0
            for method in dir(TrainInfo):
                # the comma at the end of the print, makes it printing 
                # in the same line, 4 times (count)
                print "| {0: <20}".format(method),
                count += 1
                if count == 4:
                    count = 0
                    print
            #object_methods = [method_name for method_name in dir(TrainInfo) if callable(getattr(object, method_name))]
            #print object_methods
            tn = TrainInfo.getTrainName()
            print "got here b3"
            try:
                print "got here b1",traininfoFileName
                TrainInfo = tif.readTrainInfo(traininfoFileName)
                print "got here b2"
                tn = TrainInfo.getTrainName()
                print "got here b3"
            except java.io.IOException as ioe :
                print "got here b3"
                log.error("IO Exception when reading train info file {}: {}", traininfoFileName, ioe)
                return -2
            except org.jdom2.JDOMException as jde :
                log.error("JDOM Exception when reading train info file {}: {}", traininfoFileName, jde);
                return -3
            except Exception:
                print "got here x"
            print "got here c"
            return self.loadTrainFromTrainInfo2(TrainInfo, overRideType, overRideValue, overRideMaxTrainLen);
        except java.lang.RuntimeException as ex:
            log.error("Unexpected, uncaught exception loading traininfofile [{}]", traininfoFileName, ex)
            return -9

    #
    # Loads a train into the Dispatcher
    #
    # @param info  a completed TrainInfo class.
    # @param overRideType  "NONE", "USER", "ROSTER" or "OPERATIONS"
    # @param overRideValue  "" , dccAddress, RosterEntryName or Operations
    # @param overRideMaxTrainLen length of train
    #            trainname.
    # @return 0 good, -1 failure
     
    def loadTrainFromTrainInfo2(self, info, overRideType, overRideValue, overRideMaxTrainLen = None):
        print "got here 1"
        #log.debug("loading train:{}, startblockname:{}, destinationBlockName:{}", info.getTrainName(),
        #        info.getStartBlockName(), info.getDestinationBlockName())
        #print ("loading train:{}, startblockname:{}, destinationBlockName:{}", info.getTrainName(),
        #        info.getStartBlockName(), info.getDestinationBlockName())
        print info
        print  info.getTrainName()
        print  info.getStartBlockName()
        print  info.getDestinationBlockName()
        # // create a new Active Train
        print "got here 2"
        #set updefaults from traininfo
        tSource = jmri.jmrit.dispatcher.ActiveTrain.ROSTER
        if info.getTrainFromTrains():
            tSource = jmri.jmrit.dispatcher.ActiveTrain.OPERATIONS;
        elif info.getTrainFromUser():
            tSource = jmri.jmrit.dispatcher.ActiveTrain.USER
        print "got here 3"
        dccAddressToUse = info.getDccAddress()
        trainNameToUse = info.getTrainName()
        
        OVERRIDETYPE_NONE = "NONE"
        OVERRIDETYPE_USER = "USER"
        OVERRIDETYPE_DCCADDRESS = "DCCADDRESS"
        OVERRIDETYPE_OPERATIONS = "OPERATIONS"
        OVERRIDETYPE_ROSTER = "ROSTER"

        ##process override
        if overRideType == "" or overRideType == OVERRIDETYPE_NONE:
            pass
        elif overRideType == OVERRIDETYPE_USER or overRideType == OVERRIDETYPE_DCCADDRESS:
            tSource = jmri.jmrit.dispatcher.ActiveTrain.USER
            dccAddressToUse = overRideValue
            trainNameToUse = overRideValue
        elif overRideType == OVERRIDETYPE_OPERATIONS:    
            tSource = jmri.jmrit.dispatcher.ActiveTrain.OPERATIONS
            trainNameToUse = overRideValue
        elif overRideType == OVERRIDETYPE_ROSTER:    
            tSource = jmri.jmrit.dispatcher.ActiveTrain.ROSTER
            trainNameToUse = overRideValue
        else:
            # just leave as in traininfo
            pass
        print "got here 4"
        # create active train
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        at = DF.createActiveTrain(info.getTransitId(), trainNameToUse, tSource,
                info.getStartBlockId(), info.getStartBlockSeq(), info.getDestinationBlockId(),
                info.getDestinationBlockSeq(),
                info.getAutoRun(), dccAddressToUse, info.getPriority(),
                info.getResetWhenDone(), info.getReverseAtEnd(), True, None, info.getAllocationMethod())
        print "got here 5"        
        if (at != None):
            print "got here 6a"  
            if (tSource == jmri.jmrit.dispatcher.ActiveTrain.ROSTER):
                print "got here 6aaz"
                RosterEntry = jmri.jmrit.roster.Roster.getDefault().getEntryForId(trainNameToUse)
                print "got here 6ab"
                if (RosterEntry != None):
                    print "got here 6b" 
                    at.setRosterEntry(RosterEntry)
                    at.setDccAddress(RosterEntry.getDccAddress())
                else:
                    log.warn("Roster Entry '{}' not found, could not create ActiveTrain '{}'",
                            trainNameToUse, info.getTrainName())
                    return -1
            print "got here 6"
            at.setAllocateMethod(info.getAllocationMethod())
            at.setDelayedStart(info.getDelayedStart())              #this is a code: NODELAY, TIMEDDELAY, SENSORDELAY
            at.setDepartureTimeHr(info.getDepartureTimeHr())        # hour of day (fast-clock) to start this train
            at.setDepartureTimeMin(info.getDepartureTimeMin())      #minute of hour to start this train
            at.setDelayedRestart(info.getDelayedRestart())          #this is a code: NODELAY, TIMEDDELAY, SENSORDELAY
            at.setRestartDelay(info.getRestartDelayMin())           #this is number of minutes to delay between runs
            at.setDelaySensor(info.getDelaySensor())
            at.setResetStartSensor(info.getResetStartSensor())
            if ((DF.isFastClockTimeGE(at.getDepartureTimeHr(), at.getDepartureTimeMin()) and
                    info.getDelayedStart() != jmri.jmrit.dispatcher.ActiveTrain.SENSORDELAY) or
                    info.getDelayedStart() == jmri.jmrit.dispatcher.ActiveTrain.NODELAY):
                at.setStarted()
            at.setRestartSensor(info.getRestartSensor())
            at.setResetRestartSensor(info.getResetRestartSensor())
            at.setTrainType(info.getTrainType())
            at.setTerminateWhenDone(info.getTerminateWhenDone())
            if (info.getAutoRun()):
                # aat = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.AutoActiveTrain.AutoActiveTrain)
                # #aat = jmri.jmrit.dispatcher.AutoActiveTrain.AutoActiveTrain(at)
                aat = jmri.jmrit.dispatcher.AutoActiveTrain(at)
                aat.setSpeedFactor(info.getSpeedFactor())
                aat.setMaxSpeed(info.getMaxSpeed())
                aat.setRampRate(aat.getRampRateFromName(info.getRampRate()))
                aat.setResistanceWheels(info.getResistanceWheels())
                aat.setRunInReverse(info.getRunInReverse())
                aat.setSoundDecoder(info.getSoundDecoder())
                if overRideMaxTrainLen == None:
                    aat.setMaxTrainLength(info.getMaxTrainLength())
                else:
                    aat.setMaxTrainLength(overRideMaxTrainLen)
                aat.setStopBySpeedProfile(info.getStopBySpeedProfile())
                aat.setStopBySpeedProfileAdjust(info.getStopBySpeedProfileAdjust())
                aat.setUseSpeedProfile(info.getUseSpeedProfile())
                if (not aat.initialize()):
                    log.error("ERROR initializing autorunning for train {}", at.getTrainName())
                    JOptionPane.showMessageDialog(dispatcherFrame, jmri.jmrit.dispatcher.Bundle.getMessage(
                            "Error27", at.getTrainName()), jmri.jmrit.dispatcher.Bundle.getMessage("MessageTitle"),
                            JOptionPane.INFORMATION_MESSAGE)
                    return -1
                DF.getAutoTrainsFrame().addAutoActiveTrain(aat)
            DF.allocateNewActiveTrain(at)
            DF.newTrainDone(at)

        else:
            log.warn("failed to create Active Train '{}'", info.getTrainName())
            return -1
        return 0
            
    ## ***********************************************************************************
    
    ## sound routines
    
    ## ***********************************************************************************            
        
            
    # use external "nircmd" command to "speak" some text  (I prefer this voice to eSpeak)
    def speak(self,msg) :
        #if self.logLevel > 0: print("about to speak",msg)
        #java.lang.Runtime.getRuntime().exec('Z:\\ConfigProfiles\\jython\\sound2\\nircmd speak text "' + msg +'"')    
        my_dir = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/programs')
        if self.logLevel > 0: print "nircmd" + my_dir+'/nircmd'
        java.lang.Runtime.getRuntime().exec(my_dir+'/nircmd speak text "' + msg +'"')
        return
        
    def announce(self, fromblockname, toblockname, speak_on): 

        from_station = self.get_station_name(fromblockname)
        to_station = self.get_station_name(toblockname)

        if speak_on == True:
            self.speak("The train in "+ from_station + " is due to depart to " + to_station)
            
    def get_station_name(self, block_name):
        BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
        block = BlockManager.getBlock(block_name)
        comment = block.getComment()
        # % is the delimeter for block name
        delimeter = '"'
        if delimeter in comment:
            station_name = self.get_substring_between_delimeters(comment, delimeter)
        else:
            station_name = block_name
        return station_name
        
    def get_substring_between_delimeters(self, comment, delimeter):
        start = delimeter
        end = delimeter
        s = comment
        substring = s[s.find(start)+len(start):s.rfind(end)]
        return substring
        
        
    def bell(self, bell_on = "True"):
        if bell_on == "True":
            snd = jmri.jmrit.Sound("resources/sounds/Bell.wav")
            snd.play()

            
              

class DispatchMaster(jmri.jmrit.automat.AbstractAutomaton):
    
    button_sensors_to_watch = []
    button_dict = {}
    
    def __init__(self):
        self.logLevel = 1
        global trains_dispatched
        trains_dispatched = []
        #initialise all block_value variables
        for block in blocks.getNamedBeanSet():
            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
            if LayoutBlockManager.getLayoutBlock(block) != None:
                block.setValue(None)                                
    
    def init(self):
        if self.logLevel > 0: print 'Create DispatchMaster Thread'
        

    def setup(self):
        if self.logLevel > 0: print "starting DispatchMaster setup"

        #get dictionary of buttons self.button_dict
        self.get_buttons()
        #set all move_to buttons inactive
        for sensor in self.button_sensors:
            sensor.setKnownState(INACTIVE)
        #store the values in a clone
        #self.store_button_states()
        # #at moment there are no trains so:
        self.button_sensors_to_watch = self.button_sensors

        if self.logLevel > 0: print "self.button_sensors_to_watch_init", [str(sensor.getUserName()) for sensor in self.button_sensors_to_watch]

        self.sensor_active = None
        if self.logLevel > 0: print "finished DispatchMaster setup"
        return True

    def handle(self):
    
        global trains_allocated
        global trains_dispatched   
    
        #only one button is active. We will keep it that way
        
        if self.logLevel > 0: print "**********************"
        if self.logLevel > 0: print "handle DispatchMaster"
        if self.logLevel > 0: print "buttons to watch",[str(sensor.getUserName()) for sensor in self.button_sensors_to_watch]
        if self.logLevel > 0: print "**********************" 
        #wait for one to go active
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
        
        #determine the button
        if self.logLevel > 0: print "sensor went active"
        sensor_changed = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
        if self.logLevel > 0: print "sensor_changed",sensor_changed.getUserName()
        
        #find location of that want to move to
        button_sensor_name = sensor_changed.getUserName()
        button_station_name = self.get_block_name_from_button_sensor_name(button_sensor_name)
        if self.logLevel > 0: print "button_sensor_name",button_sensor_name,"button_station_name",button_station_name
        
        #find what train we want to move
        #select only from available trains  %%%%todo%%%%%
        all_trains = self.get_all_roster_entries_with_speed_profile()
        #trains to choose from are the allocated - dispatched
        trains_to_choose = copy.copy(trains_allocated)
        if self.logLevel > 0: print "trains_dispatchedx", trains_dispatched
        if self.logLevel > 0: print "trains_allocated",trains_allocated
        if self.logLevel > 0: print "trains_to_choose",trains_to_choose
        if trains_dispatched != []:
            for train in trains_dispatched:
                if self.logLevel > 0: print "removing" ,train
                trains_to_choose.remove(train)
                if self.logLevel > 0: print "trains_to_choose",trains_to_choose
        # msg = "trains_to_choose" + str(trains_to_choose)
        # JOptionPane.showMessageDialog(None,msg)
        if trains_to_choose == []:
            str_trains_dispatched= (' '.join(trains_dispatched))
            msg = "There are no trains available for dispatch\nTrains dispatched are:\n"+str_trains_dispatched+"\nOK (yes) RESET (no)"
            title = ""
            result = JOptionPane.showConfirmDialog(None, msg, title, JOptionPane.YES_NO_OPTION)
            if result == JOptionPane.NO_OPTION:
                trains_dispatched = []                      
            sensor_changed.setKnownState(INACTIVE)
        else:
            msg = "select train you want to move"
            train_to_move = modifiableJComboBox(trains_to_choose,msg).return_val()
            
            print "+++++++++++++++++++"
            print "checking pressed button"
            print "+++++++++++++++++++"
            
            ## Check the pressed button
            for station_block_name in g.station_block_list:
                if self.logLevel > 0: print "station_block_name", station_block_name
                #get a True if the block block_value has the train name in it
                block_value_state = self.check_train_in_block(station_block_name, train_to_move)
                if self.logLevel > 0: print "block_value_state= ",block_value_state
                #get a True if the block is occupied
                block_occupied_state = self.check_sensor_state_given_block_name(station_block_name)
                if self.logLevel > 0: print "block_occupied_state= ",block_occupied_state
                if self.logLevel > 0: print ("station block name {} : {}". format(station_block_name, str(block_occupied_state)))
                # do not attempt to move to where you are
                button_pressed_in_occupied_station = (button_station_name == station_block_name)
                #check if the block is occupied and has the required train in it  
                if block_value_state == True and block_occupied_state == True and button_pressed_in_occupied_station == False:
                    #move from station_block_Name to button_name_station

                    express = self.get_express_flag()   # flag determining whether want to stop at all stations or not stop
                    
                    title = "will the train stop at all stations?"
                    msg = "select the train type:"
                    if express:
                        options = ["express", "stopping"]
                        default = "express"
                        result = OptionDialog().variable_combo_box(options, default, msg, title, JOptionPane.QUESTION_MESSAGE)
                    else:
                        options = ["stopping", "express"]
                        default = "stopping"
                        result = OptionDialog().variable_combo_box( options, default, msg, title, JOptionPane.QUESTION_MESSAGE)
                    if self.logLevel > 0: print "result = ",result    
                    if result == "express":
                        print "moving express"                                          
                        if g == None:
                            if self.logLevel > 0: print "G IS NONE"
                        #graph = self.mark_occupied_blocks(g.g_express)
                        #self.move_between_stations(station_block_name, button_station_name, train_name, g.g_express)
                        move_train = MoveTrain(station_block_name, button_station_name, train_to_move, g.g_express)
                        instanceList.append(move_train)
                        if move_train.setup():
                            move_train.setName(train_to_move)
                            move_train.start()
                        if self.logLevel > 0: print "station_block_name",station_block_name
                        if self.logLevel > 0: print "button_station_name", button_station_name
                        #if self.logLevel > 0: print "button_block_name", button_block_name
                        if self.logLevel > 0: print "**********************"

                    elif express == False:
                        if self.logLevel > 0: print "moving slow"
                        #OptionDialog().displayMessage("a stopping train")
                        if g == None:
                            if self.logLevel > 0: print "G IS NONE"
                        move_train = MoveTrain(station_block_name, button_station_name, train_to_move, g.g_stopping)
                        instanceList.append(move_train)
                        if move_train.setup():
                            move_train.setName(train_to_move)
                            if self.logLevel > 0: print "********calling thread move**************"
                            move_train.start()
                            if self.logLevel > 0: print "********called thread move***************"
                        if self.logLevel > 0: print "station_block_name",station_block_name
                        if self.logLevel > 0: print "button_station_name", button_station_name
                        #if self.logLevel > 0: print "button_block_name", button_block_name
                        if self.logLevel > 0: print "**********************"                    
                        #self.move_between_stations(station_block_name, button_station_name, train_name, g.g_stopping)
                    else:
                        #express flag not set up
                        pass
                    break
            #set old button which activated the same train to inactive
            if self.button_dict != {}:
                if self.logLevel > 0: print "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                if self.logLevel > 0: print "self.button_dict = ",self.button_dict
                if self.logLevel > 0: print "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                if train_to_move in self.button_dict:
                    old_button_sensor = self.button_dict[train_to_move]
                    old_button_sensor.setKnownState(INACTIVE)
                    self.button_sensors_to_watch.append(old_button_sensor)
            #associate new button with train
            if self.logLevel > 0: print "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
            if self.logLevel > 0: print "train_to_move", train_to_move
            if self.logLevel > 0: print "sensor_changed", sensor_changed
            if self.logLevel > 0: print "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
            self.button_dict[train_to_move] = sensor_changed
            if self.logLevel > 0: print "self.button_dict = ",self.button_dict

            # #set other buttons to inactive
            # if self.sensor_active != None:
                # self.sensor_active.setKnownState(INACTIVE)
                # self.waitMsec(2000)
            #remember the active button
            # self.sensor_active = sensor_changed
            #get the list of sensors to watch
            #if self.logLevel > 0: print "self.button_sensors_to_watch_before_remove", [sensor.getUserName() for sensor in self.button_sensors_to_watch]
            self.button_sensors_to_watch.remove(sensor_changed)
        #if self.logLevel > 0: print "self.button_sensors_to_watch", [sensor.getUserName() for sensor in self.button_sensors_to_watch]
        if self.logLevel > 0: print "end handle"
        return True    

    def get_block_name_from_button_sensor_name(self, button_sensor_name):
        #button_sensor_name MoveToblock8_stored
        #block_name block8
        block_name = button_sensor_name.replace("MoveTo","").replace("_stored","").replace("_"," ")
        return block_name

    def get_button_sensor_given_block_name(self, block_name):
        button_sensor_name = "MoveTo"+block_name.replace(" ","_") +"_stored"
        button_sensor = sensors.getSensor(button_sensor_name)
        return button_sensor 

    def get_express_flag(self):
        self.express_sensor = sensors.getSensor("Express")
        if self.express_sensor is None:
            OptionDialog().displayMessage("No Express Sensor set up")
            return None
        express_state = self.express_sensor.getKnownState()
        if self.logLevel > 0: print express_state,jmri.Sensor.ACTIVE
        #msg = "express sensor is ", str(express_state)
        #OptionDialog().displayMessage(msg)
        if express_state == jmri.Sensor.ACTIVE:
            express_flag = True
        else:
            express_flag = False
        return express_flag
        
    def get_blockcontents(self, block_name):
        block = blocks.getBlock(block_name)
        value =  block.getValue()
        return value

    def check_train_in_block(self, block_name, train_name):
        mem_val = self.get_blockcontents(block_name)
        if train_name == mem_val:
            return True
        else:
            return False 

    def check_sensor_state_given_block_name(self, station_block_name):
        #if self.logLevel > 0: print("station block name {}".format(station_block_name))
        layoutBlock = layoutblocks.getLayoutBlock(station_block_name)
        station_sensor = layoutBlock.getOccupancySensor() 
        if station_sensor is None:
            OptionDialog().displayMessage(' Sensor in block {} not found'.format(station_block_name))
            return
        currentState = True if station_sensor.getKnownState() == ACTIVE else False
        return currentState    

    def get_all_roster_entries_with_speed_profile(self):
        roster_entries_with_speed_profile = []
        r = jmri.jmrit.roster.Roster.getDefault()
        for roster_entry in jmri.jmrit.roster.Roster.getAllEntries(r):
            if self.logLevel > 0: print "roster_entry.getSpeedProfile()",roster_entry,roster_entry.getSpeedProfile()
            if roster_entry.getSpeedProfile() != None:
                roster_entries_with_speed_profile.append(roster_entry.getId())
                if self.logLevel > 0: print "roster_entry.getId()",roster_entry.getId()
        return roster_entries_with_speed_profile      
 
    def get_buttons(self):    
        self.button_sensors = [self.get_button_sensor_given_block_name(station_block_name) for station_block_name in g.station_block_list]
        self.button_sensor_states = [self.check_sensor_state(button_sensor) for button_sensor in self.button_sensors]
        # for button_sensor in self.button_sensors:
            # self.button_dict[button_sensor] = self.check_sensor_state(button_sensor)
            
    def check_sensor_state(self, sensor):
        #if self.logLevel > 0: print("check_sensor_state",sensor)
        if sensor == None :
            #if self.logLevel > 0: print('Sensor in check_sensor_state is none')
            return None
        #sensor = sensors.getSensor(sensor_name)
        if sensor is None:
            OptionDialog().displayMessage('Sensor {} not found'.format( sensor_name))
            return
        currentState = True if sensor.getKnownState() == ACTIVE else False
        #if self.logLevel > 0: print("check_sensor_state {}".format(currentState))
        return currentState
       
    def store_button_states(self):
        self.button_sensor_states_old = copy.copy(self.button_sensor_states)
        if self.logLevel > 0: print "self.button_sensor_states_old",self.button_sensor_states_old
        #self.button_dict_old = dict(self.button_dict)
        
    def get_button_sensor_given_block_name(self, block_name):
        button_sensor_name = "MoveTo"+block_name.replace(" ","_") +"_stored"
        button_sensor = sensors.getSensor(button_sensor_name)
        return button_sensor        
           
    # def reset_buttons(self):
        # for button_dict_old_value, button_dict_value in zip(button_dict_old.iteritems(), button_dict.iteritems()):
            # if button_dict_old_value == button_dict_value:
                # if self.logLevel > 0: print 'Ok', button_dict_old_value, button_dict_value
                # sensor, val = button_dict_old_value
                # if val = True:
                    # sensor.setKnownState(INACTIVE)
            # else:
                # if self.logLevel > 0: print 'Not', button_dict_old_value, button_dict_value
                # sensor, val = button_dict_value        
    
        
        
        return False            
# End of class StopMaster

class RunDispatcherMaster():

    def __init__(self):
        global g
        global le
        my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
        import sys
        sys.path.append(my_path_to_jars) # add the jar to your path
        CreateGraph = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateGraph.py')
        exec(open (CreateGraph).read())
        #execfile(CreateGraph)
        le = LabelledEdge
        g = StationGraph()

        new_train_master = NewTrainMaster()
        instanceList.append(new_train_master)
        if new_train_master.setup():
            new_train_master.setName('New Train Master')
            new_train_master.start()  

        stop_master = StopMaster()
        if stop_master.setup():
            stop_master.setName('Stop Master')
            stop_master.start()

        reset_button_master = ResetButtonMaster()
        instanceList.append(reset_button_master)
        if reset_button_master.setup():
            pass
            reset_button_master.setName('Reset Button Master')
            reset_button_master.start()

        dispatch_master = DispatchMaster()
        instanceList.append(dispatch_master)
        if dispatch_master.setup():
            dispatch_master.setName('Dispatch Master')
            dispatch_master.start()           
                    
if __name__ == '__builtin__':
    RunDispatcherMaster()
    # NewTrainMaster checksfor the new train in siding. Needs to inform what station we are in 
    #DispatchMaster checks all button sensors 

    
# if __name__ == "__main__":
    # if self.logLevel > 0: print "hi"            
    # logLevel = 4
    # #stopMasterSensor = "stopMasterSensor"
    # #newTrainSensor = "newTrainSensor"
    # instanceList = []   # List of file based instances 
    # trains = {}
    # g = StationGraph()
    # if self.logLevel > 0: print "hi"
