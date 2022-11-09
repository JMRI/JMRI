###############################################################################
#
# class MoveTrain
# Calls dispatcher to e train from one station to another
# given engine and start and end positions
#
###############################################################################

import os
import java
import jmri
import math
#import platform

class MoveTrain(jmri.jmrit.automat.AbstractAutomaton):

    global trains_dispatched
    global trains

    def __init__(self, station_from_name, station_to_name, train_name, graph):
        self.logLevel = 0
        self.station_from_name = station_from_name
        self.station_to_name = station_to_name
        self.train_name = train_name
        self.graph = graph

    def setup(self):
        return True

    def handle(self):
        #move between stations in the thread
        if self.logLevel > 1: print"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        if self.logLevel > 1: print "move between stations in the thread"
        if self.logLevel > 1: print"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        self.move_between_stations(self.station_from_name, self.station_to_name, self.train_name, self.graph)
        return False

    def move_between_stations(self, station_from_name, station_to_name, train_name, graph):
        if self.logLevel > 1: print "Moving from " + station_from_name + " to " + station_to_name
        #need to look up the required transit in the graph
        StateVertex_start = station_from_name
        StateVertex_end = station_to_name
        # for e in graph.edgeSet():
        # if self.logLevel > 1: print (graph.getEdgeSource(e) + " --> " + graph.getEdgeTarget(e))
        if self.logLevel > 1: print "calling shortest path", StateVertex_start, StateVertex_end
        paths = DijkstraShortestPath.findPathBetween(graph, StateVertex_start, StateVertex_end)
        if self.logLevel > 0: print "graph", graph
        if self.logLevel > 0: print "paths", paths
        if self.logLevel > 0: print "returned from shortest path"
        if self.logLevel > 1: print "in move_between_stations trains = ", trains, "train_name = ", train_name
        train = trains[train_name]
        if self.logLevel > 1: print "train" , train
        penultimate_block_name = train["penultimate_block_name"]
        if self.logLevel > 1: print "penultimate_block_name" , penultimate_block_name
        previous_edge = train["edge"]
        previous_direction = train["direction"]

        trains_dispatched.append(str(train_name))

        count_path = 0

        if paths == None or paths == []:
            print "1Error cannot find shortest path. restart the system. " + \
                  "The stop dispatcher system routine does not work properly with multiple layout panels. Sorry"
            return

        for e in paths:
            # need to check whether:
            #   last block of previous edge and current first block
            #   are the same

            # if the same the train must change direction. as we are going in and out the same path
            #
            previous_edge = train["edge"]
            penultimate_block_name = train["penultimate_block_name"]
            previous_direction = train["direction"]
            current_edge = e
            neighbor_name = e.getItem("neighbor_name")
            if self.logLevel > 0: print train
            if self.logLevel > 0: print "neighbor_name = ", neighbor_name
            if self.logLevel > 0: print "penultimate_block_name" , penultimate_block_name

            BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
            previous_block = BlockManager.getBlock(penultimate_block_name)
            current_block = BlockManager.getBlock(previous_edge.getItem("last_block_name"))
            next_block = BlockManager.getBlock(current_edge.getItem("second_block_name"))
            if count_path == 0:
                # we are on a new path and must determine the direction
                [transit_direction, transit_instruction]  = self.set_direction(previous_block, current_block, next_block, previous_direction)
                self.announce1(e, transit_direction, transit_instruction, train)
            else:
                # if there are several edges in a path, then we are on an express route, and there is a change in direction at each junction
                if previous_block.getUserName() == next_block.getUserName() : #we are at a stub/siding
                    if previous_direction == "forward":
                        transit_direction = "reverse"
                    else:
                        transit_direction = "forward"
                    transit_instruction = "stub"
                else:
                    [transit_direction, transit_instruction] = self.set_direction(previous_block, current_block, next_block, previous_direction)

                speech_reqd = self.speech_required_flag()
                # make announcement as train enters platform
                print "making announcement"
                self.announce1(e, transit_direction, transit_instruction, train)
                time_to_stop_in_station = self.get_time_to_stop_in_station(e, transit_direction)
                t = time_to_stop_in_station / 1000
                msg = "started waiting for " + str(int(t)) + " seconds"
                if self.logLevel > 0: self.speak(msg)
                self.waitMsec(int(time_to_stop_in_station))
                msg = "finished waiting for " + str(t) + " seconds"
                if self.logLevel > 0: self.speak(msg)

            result = self.move(e, transit_direction, transit_instruction,  train_name)
            if self.logLevel > 1: print "returned from self.move, result = ", result
            if result == False:
                trains_dispatched.remove(str(train_name))
                break
            #store the current edge for next move
            train["edge"] = e
            train["penultimate_block_name"] = e.getItem("penultimate_block_name")
            train["direction"] = transit_direction
            count_path +=1

        if self.logLevel > 1: print "transit finished, removing train from dispatch list"
        if str(train_name) in trains_dispatched:
            trains_dispatched.remove(str(train_name))
        if self.logLevel > 1: print "trains_dispatched", trains_dispatched

    def set_direction(self, previous_block, current_block, next_block, previous_direction):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        current_layout_block = LayoutBlockManager.getLayoutBlock(current_block)
        if current_layout_block.validThroughPath(previous_block, next_block):
            transit_instruction = "same"
        else:
            transit_instruction = "change"
        if transit_instruction == "change":
            if previous_direction == "forward":
                transit_direction = "reverse"
            else:
                transit_direction = "forward"
        else:
            transit_direction = previous_direction
        return [transit_direction, transit_instruction]


    def get_time_to_stop_in_station(self, edge, direction):

        if direction == "forward":
            filename_fwd = self.get_filename(edge, "fwd")
            trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
            station_wait_time = trainInfo_fwd.getWaitTime()
        else:
            filename_fwd = self.get_filename(edge, "rvs")
            trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
            station_wait_time = trainInfo_fwd.getWaitTime()
        if station_wait_time != None:
            return math.floor(float(station_wait_time+0)) * 1000  # set in milli secs
        else:
            return 0

    def is_integer(self, n):
        try:
            if n == None: return False
            float(n)
        except ValueError:
            return False
        else:
            return float(n).is_integer()

    def announce1(self, e, direction, instruction, train):
        to_name = e.getTarget()
        from_name = e.getSource()
        speech_reqd = self.speech_required_flag()
        self.announce( from_name, to_name, speech_reqd, direction, instruction)

    def move(self, e, direction, instruction, train):
        if self.logLevel > 1: print "++++++++++++++++++++++++"
        if self.logLevel > 1: print e, "Target", e.getTarget()
        if self.logLevel > 1: print e, "Source", e.getSource()
        if self.logLevel > 1: print e, "Train", train
        if self.logLevel > 1: print "++++++++++++++++++++++++"
        to_name = e.getTarget()
        from_name = e.getSource()
        sensor_move_name = "MoveInProgress"+to_name.replace(" ","_")

        self.set_sensor(sensor_move_name, "active")
        speech_reqd = self.speech_required_flag()
        #self.announce( from_name, to_name, speech_reqd, direction, instruction)  # now done when train arrives in platfor instead of when leaving
        if self.logLevel > 1: print "***************************"
        result = self.call_dispatch(e, direction, train)
        if self.logLevel > 1: print "______________________"
        if result == True:
            #Wait for the Active Trains List to have the
            DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
            java_active_trains_list = DF.getActiveTrainsList()
            java_active_trains_Arraylist= java.util.ArrayList(java_active_trains_list)
            for t in java_active_trains_Arraylist:
                if self.logLevel > 1: print "t=",t,t.getActiveTrainName()
                #active_trains_list = java.util.Arrays.asList(java_active_trains_list)
            if self.logLevel > 1: print "!!!!!!!! train = ", train, "active_trains_list", java_active_trains_Arraylist
            active_train_names_list = [str(t.getTrainName()) for t in java_active_trains_Arraylist]
            if self.logLevel > 1: print "!!!!!!!! train = ", train, "active_trains_name_list", active_train_names_list
            while train in active_train_names_list:
                self.waitMsec(500)
                DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
                active_trains_list = DF.getActiveTrainsList()
                active_train_names_list = [str(t.getTrainName()) for t in java_active_trains_Arraylist]
                java_active_trains_Arraylist= java.util.ArrayList(java_active_trains_list)
                active_train_names_list = [str(t.getTrainName()) for t in java_active_trains_Arraylist]
                if self.logLevel > 1: print "!!!!!!!! train = ", train, "active_train_names_list", active_train_names_list
            self.set_sensor(sensor_move_name, "inactive")
            if self.logLevel > 1: print ("+++++ sensor " + sensor_move_name + " inactive")
        else:
            self.set_sensor(sensor_move_name, "inactive")
        return result

    def speech_required_flag(self):
        self.sound_sensor = sensors.getSensor("soundSensor")
        if self.sound_sensor is None:
            OptionDialog().displayMessage("No sound Sensor set up")
            return None
        sound_state = self.sound_sensor.getKnownState()
        if self.logLevel > 1: print sound_state,ACTIVE
        if sound_state == ACTIVE:
            sound_flag = True
        else:
            sound_flag = False
        return sound_flag

    def call_dispatch(self, e, direction, train):
        if self.logLevel > 1: print ("in dispatch")
        to_name = e.getTarget()
        from_name = e.getSource()
        if self.logLevel > 1: print ("incall_dispatch: move from " + from_name + " to " + to_name)

        if direction == "forward":
            filename = self.get_filename(e, "fwd")
        else:
            filename = self.get_filename(e, "rvs")

        if self.logLevel > 1: print "filename = ", filename, "direction = " , direction
        result = self.doDispatch(filename, "ROSTER", train)
        if self.logLevel > 1: print "result", result
        return result

    def get_filename(self, e, suffix):

        # suffix is "fwd" or "rvs"
        # e is edge

        from_station_name = g.g_express.getEdgeSource(e)
        to_station_name = g.g_express.getEdgeTarget(e)
        neighbor_name = e.getItem("neighbor_name")
        index = e.getItem("index")

        filename = "From " + str(from_station_name) + " To " + str(to_station_name) + " Via " + str(neighbor_name) + " " + str(index)
        filename = filename.replace(" ", "_")
        filename = filename + "_" + suffix + ".xml"

        return filename

        #    Dispatch (<filename.xml>, [USER | ROSTER | OPERATIONS >,<dccAddress, RosterEntryName or Operations>

    def doDispatch(self, traininfoFileName, type, value):
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        #try:
        if self.logLevel > 1: print "traininfoFileName",traininfoFileName
        result = DF.loadTrainFromTrainInfo(traininfoFileName, type, value)
        if result == -1:
            if self.logLevel > 1: print "result from dispatcher frame" , result
            return False  #No train allocated
        else:
            if self.logLevel > 1: print "result from dispatcher frame" , result
            return True
        # except:
        # if self.logLevel > 1: print ("FAILURE tried to run dispatcher with file {} type {} value {}".format(traininfoFileName,  type, value))
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
            if self.logLevel > 1: print "set_sensor ", sensorName, 'inactive'
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

    ## ***********************************************************************************

    ## sound routines

    ## ***********************************************************************************

    def getOperatingSystem(self):
        #detecting the operating system using `os.name` System property
        os = java.lang.System.getProperty("os.name")
        os = os.lower()
        if "win" in os:
            return "WINDOWS"
        elif "nix" in os or "nux" in os or "aix" in os:
            return "LINUX"
        elif "mac" in os:
            return "MAC"
        return None

    def speak(self, msg):
        os = self.getOperatingSystem()
        if os == "WINDOWS":
            self.speak_windows(msg)
        elif os == "LINUX":
            self.speak_linux(msg)
        elif os == "MAC":
            self.speak_mac(msg)

    def speak_windows(self,msg) :
        try:
            cmd1 = "Add-Type -AssemblyName System.Speech"
            cmd2 = '$SpeechSynthesizer = New-Object -TypeName System.Speech.Synthesis.SpeechSynthesizer'
            cmd3 = "$SpeechSynthesizer.Speak('" + msg + "')"
            cmd = cmd1 + ";" + cmd2 + ";" + cmd3
            os.system("powershell " + cmd )
        except:
            msg = "Announcements not working \n Only supported on windows versions with powershell and SpeechSynthesizer"
            JOptionPane.showMessageDialog(None, msg, "Warning", JOptionPane.WARNING_MESSAGE)

    def speak_mac(self, msg):
        try:
            java.lang.Runtime.getRuntime().exec("say {}".format(msg))
        except:
            msg = "Announcements not working \n say not working on your Mac"
            JOptionPane.showMessageDialog(None, msg, "Warning", JOptionPane.WARNING_MESSAGE)

    def speak_linux(self, msg):
        try:
            #os.system("""echo %s | spd-say -e -w -t male1""" % (msg,))
            #os.system("""echo %s | spd-say -e -w -t female3""" % (msg,))
            #os.system("""echo %s | spd-say -e -w -t child_male""" % (msg,))
            os.system("""echo %s | spd-say -e -w -t child_female""" % (msg,))  #slightly slower
        except:
            msg = "Announcements not working \n spd-say not set up on your linux system"
            JOptionPane.showMessageDialog(None, msg, "Warning", JOptionPane.WARNING_MESSAGE)

    def announce(self, fromblockname, toblockname, speak_on, direction, instruction):

        from_station = self.get_station_name(fromblockname)
        to_station = self.get_station_name(toblockname)

        if speak_on == True:
            if direction == "forward":
                platform = " platform 1 "
            else:
                platform = " platform 2 "
            self.speak("The train in" + platform + " is due to depart to " + to_station)
            #self.speak("The train in "+ from_station + " is due to depart to " + to_station )

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
            snd.play(snd)

class NewTrainMaster(jmri.jmrit.automat.AbstractAutomaton):

    # responds to the newTrainSensor, and allocates trains available for dispatching
    # we make the allocated flag global as we will use it in DispatchMaster when we dispatch a train

    global trains_allocated
    logLevel = 0
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
        self.od = OptionDialog()
        return True

    def handle(self):

        global trains_allocated

        #this repeats
        # wait for a sensor requesting to check for new train
        if self.logLevel > 0: print ("wait for a sensor requesting to check for new train")

        self.waitSensorActive(self.new_train_sensor)
        self.new_train_sensor.setKnownState(INACTIVE)


        #display the allocated trains
        msg = "choose"
        actions = ["setup train","check train direction", "reset trains"]
        action = self.od.List(msg, actions)
        if action == "setup train":
            station_block_name, new_train_name = self.check_new_train_in_siding()
            if self.logLevel > 0: print "station_block_name",station_block_name, "existing train name", new_train_name
            if station_block_name != None:
                # take actions for new train
                if new_train_name == None:
                    all_trains = self.get_all_roster_entries_with_speed_profile()
                    if all_trains == []:
                        msg = "There are no engines with speed profiles, cannot operate without any"
                        JOptionPane.showMessageDialog(None,msg)
                    else:
                        msg = self.get_all_trains_msg()
                        title = None
                        opt1 = "Select section"
                        s = self.od.customMessage(msg, title, opt1)
                        if self.logLevel > 0: print "station_block_name",station_block_name, "s", s
                        if self.od.CLOSED_OPTION == False:
                            msg = "Select section"
                            sections_to_choose = self.get_non_allocated_trains_sections()
                            new_section_name = self.od.List(msg, sections_to_choose)
                            if self.od.CLOSED_OPTION == False:
                                msg = "Select the train in " + new_section_name
                                trains_to_choose = self.get_non_allocated_trains()
                                if trains_to_choose == []:
                                    s = OptionDialog().displayMessage("no more trains with speed profiles \nto select")
                                else:
                                    new_train_name = self.od.List(msg, trains_to_choose)
                                    if self.od.CLOSED_OPTION == False:
                                        if new_train_name not in trains_allocated:
                                            trains_allocated.append(new_train_name)
                                        #print "*****", "new_train_name", new_train_name, "new_section_name", new_section_name
                                        self.add_to_train_list_and_set_new_train_location(new_train_name, new_section_name)
                                        if self.od.CLOSED_OPTION == False:  #only do this if have not closed a frame in add_to_train_list_and_set_new_train_location
                                            self.set_blockcontents(new_section_name, new_train_name)
                                            self.set_length(new_train_name)
                else:
                    if self.logLevel > 1 : print "!!!!5"
                    trains_to_choose = self.get_non_allocated_trains()
                    msg = "In " + station_block_name + " Select train roster"
                    new_train_name = modifiableJComboBox(trains_to_choose,msg).return_val()
                    if new_train_name not in trains_allocated:
                        trains_allocated.append(new_train_name)

                    self.add_to_train_list_and_set_new_train_location(new_train_name, station_block_name)
                    self.set_blockcontents(station_block_name, new_train_name)
                    self.set_length(new_train_name)
            else:
                if self.logLevel > 0: print "about to show message no new train in siding"
                msg = self.get_all_trains_msg()
                msg +=  "\nPut a train in a section so it can be allocated!\n"
                title = "All trains allocated"
                opt1 = "Continue"
                opt2 = "Delete the trains already set up and start again"
                ans = self.od.customQuestionMessage2(msg, title, opt1, opt2)
                if self.od.CLOSED_OPTION == True:
                    pass
                elif ans == JOptionPane.NO_OPTION:
                    self.reset_allocation()
        elif action == "reset trains":
            msg = self.get_all_trains_msg()
            msg +=  "\nReset all these trains\n"
            title = "Reset"
            opt1 = "Continue"
            opt2 = "Delete the trains already set up and start again"
            ans = self.od.customQuestionMessage2(msg, title, opt1, opt2)
            if self.od.CLOSED_OPTION == True:
                pass
            elif ans == JOptionPane.NO_OPTION:
                self.reset_allocation1()
        #elif action == "reset control sensors"
        else: #"check train direction"
            all_trains = self.get_all_roster_entries_with_speed_profile()
            if all_trains == []:
                msg = "There are no engines with speed profiles, cannot operate without any"
                JOptionPane.showMessageDialog(None,msg)
            else:
                msg = self.get_allocated_trains_msg()
                title = None
                opt1 = "Select section"
                s = self.od.customMessage(msg, title, opt1)
                if self.logLevel > 0: print "station_block_name",station_block_name, "s", s
                if self.od.CLOSED_OPTION == False:
                    msg = "Select section"
                    sections_to_choose = self.get_allocated_trains_sections()
                    new_section_name = self.od.List(msg, sections_to_choose)
                    if self.od.CLOSED_OPTION == False:
                        msg = "Select the train in " + new_section_name
                        trains_to_choose = self.get_allocated_trains()
                        if trains_to_choose == []:
                            s = OptionDialog().displayMessage("no more trains with speed profiles \nto select")
                        else:
                            new_train_name = self.od.List(msg, trains_to_choose)
                            if self.od.CLOSED_OPTION == False:
                                #print "need to find the direction of train", new_train_name
                                self.check_train_direction(new_train_name, new_section_name)

        return True

    def check_train_direction(self, train_name, station_block_name):
        global train
        if train_name in trains:
            train = trains[train_name]
            direction = train["direction"]
            #print direction
            penultimate_layout_block = self.get_penultimate_layout_block(station_block_name)

            saved_state = penultimate_layout_block.getUseExtraColor()
            in_siding = self.in_siding(station_block_name)
            if not in_siding:
                # highlight the penultimate block
                penultimate_layout_block.setUseExtraColor(True)
                msg = "train travelling " + direction + " away from highlighted block"
                s = OptionDialog().displayMessage(msg)
                penultimate_layout_block.setUseExtraColor(saved_state)
            else:
                msg = "train travelling " + direction + " away from siding"
                s = OptionDialog().displayMessage(msg)
                penultimate_layout_block.setUseExtraColor(saved_state)

    def set_length(self, new_train_name):
        #create engine if does not exist
        EngineManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.engines.EngineManager)
        engineRoad = "Set by Dispatcher System"
        engineNumber = new_train_name
        engine = EngineManager.newRS(engineRoad, engineNumber)
        #get the current length of the engine
        default = "10"
        current_length = engine.getLength()
        if current_length == "0":
            current_length = default
            engine.setLength(default)
        #ask if want to change length
        title = "Set the length of the engine/train"
        msg = "length of " + new_train_name + " = " + str(current_length)
        opt1 = "OK"
        opt2 = "Change"
        request = self.od.customQuestionMessage2str(msg,title,opt1, opt2)
        if request == "Change":
            #set the new length
            msg = "input length of " + new_train_name
            title = "length of " + new_train_name
            default_value = current_length
            new_length = self.od.input(msg, title, default_value)
            engine.setLength(new_length)

    def get_allocated_trains_msg(self):
        allocated_trains =[ str(train) + " in block " + str(trains[train]["edge"].getTarget()) for train in trains_allocated]
        if allocated_trains ==[]:
            msg = "There are no allocated trains \n"
        else:
            msg = "The Allocated trains are: \n" +'\n'.join(allocated_trains)
        return msg

    def get_allocated_trains(self):
        return trains_allocated

    def get_non_allocated_trains(self):
        all_trains = self.get_all_roster_entries_with_speed_profile()
        non_allocated_trains = copy.copy(all_trains)
        for train in trains_allocated:
            if train in non_allocated_trains:
                non_allocated_trains.remove(train)
        return non_allocated_trains

    def get_non_allocated_trains_msg(self):
        trains_in_sections_allocated1 = self.trains_in_sections_allocated()
        msg = "the non-allocated trains are in sections: \n\n" + "\n".join(["  " + str(train[0]) for train in trains_in_sections_allocated1 if train[2] == "non-allocated"])
        return msg

    def get_non_allocated_trains_sections(self):
        trains_in_sections_allocated1 = self.trains_in_sections_allocated()
        return [str(train[0]) for train in trains_in_sections_allocated1 if train[2] == "non-allocated"]

    def get_allocated_trains_sections(self):
        trains_in_sections_allocated1 = self.trains_in_sections_allocated()
        return [str(train[0]) for train in trains_in_sections_allocated1 if train[2] == "allocated"]

    def get_all_trains_msg(self):
        return self.get_allocated_trains_msg() + "\n" + self.get_non_allocated_trains_msg()

    def reset_allocation(self):
        global trains_allocated
        if trains_allocated == []:
            if self.logLevel > 0: print ("a")
            msg = "Nothing to reset"
            OptionDialog().displayMessage(msg)
        else:
            if self.logLevel > 0: print ("b")
            msg = "Select train to modify"
            train_name_to_remove = modifiableJComboBox(trains_allocated,msg).return_val()
            trains_allocated.remove(train_name_to_remove)
            self.new_train_sensor.setKnownState(ACTIVE)

    def reset_allocation1(self):
        global trains_allocated
        if trains_allocated == []:
            if self.logLevel > 0: print ("a")
            msg = "Nothing to reset"
            OptionDialog().displayMessage(msg)
        else:
            if self.logLevel > 0: print ("b")
            #set trains_allocated to []
            for train_name_to_remove in trains_allocated:
                msg = "Select train to modify"
                #train_name_to_remove = modifiableJComboBox(trains_allocated,msg).return_val()
                trains_allocated.remove(train_name_to_remove)           #remove the train from trains_allocated
                self.reset_train_in_blocks(train_name_to_remove)  #remove the blockcontents text
            if self.logLevel > 0: print "trains_allocated",trains_allocated
            #self.new_train_sensor.setKnownState(ACTIVE)

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
        # each value is itself a dictionary with 3 items
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
        edge = None
        j = 0
        #print "edge_before" , edge
        #print "g.g_stopping.edgesOf(station_block_name)",g.g_stopping.edgesOf(station_block_name)
        break1 = False
        #print "no edges", g.g_stopping.edgeSet()
        # for e in g.g_stopping.edgeSet():
        #     print "e" , e
        for e in g.g_stopping.edgeSet():
            j+=1
            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
            station_block = LayoutBlockManager.getLayoutBlock(station_block_name)
            number_neighbors = station_block.getNumberOfNeighbours()
            #print "station block number neighbors", number_neighbors
            in_siding = (number_neighbors == 1)
            #print "in_siding", in_siding
            for i in range(station_block.getNumberOfNeighbours()):
                neighbor_name = station_block.getNeighbourAtIndex(i).getDisplayName()
                #print "neighbor_name", neighbor_name
                #print "station_block_name", station_block_name
                #print "penultimate_block_name", e.getItem("penultimate_block_name")
                #print "last_block_name", e.getItem("last_block_name")
                #print "***************"
                if e.getItem("penultimate_block_name") == neighbor_name and e.getItem("last_block_name") == station_block_name:
                    edge = e
                    break1 = True
            if break1 == True:
                break
            #print "******************************++"
        if edge == None:
            print "Error the required block has not been found. restart and try again. Sorry!"
            return
        train["edge"] = edge
        train["penultimate_block_name"] = edge.getItem("penultimate_block_name")

        # 3) set direction so can check direction of transit

        penultimate_block_name = edge.getItem("penultimate_block_name")
        penultimate_layout_block = LayoutBlockManager.getLayoutBlock(penultimate_block_name)
        saved_state = penultimate_layout_block.getUseExtraColor()
        if not in_siding:
            # highlight the penultimate block
            penultimate_layout_block.setUseExtraColor(True)
        train_direction = self.set_train_direction(station_block_name, in_siding)
        #check the condition set in set_train_direction
        train["direction"] = train_direction
        penultimate_layout_block.setUseExtraColor(saved_state)

        # 4) add to allocated train list
        if str(train_name) not in trains_allocated:
            trains_allocated.append(str(train_name))

    def get_penultimate_layout_block(self, station_block_name):
        # get the last traversed edge to the edge of the station_block
        edge = None
        j = 0
        #print "edge_before" , edge
        #print "g.g_stopping.edgesOf(station_block_name)",g.g_stopping.edgesOf(station_block_name)
        break1 = False
        #print "no edges", g.g_stopping.edgeSet()
        for e in g.g_stopping.edgeSet():
            j+=1
            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
            station_block = LayoutBlockManager.getLayoutBlock(station_block_name)
            number_neighbors = station_block.getNumberOfNeighbours()
            #print "station block number neighbors", number_neighbors
            in_siding = (number_neighbors == 1)
            for i in range(station_block.getNumberOfNeighbours()):
                neighbor_name = station_block.getNeighbourAtIndex(i).getDisplayName()
                # print "neighbor_name", neighbor_name
                # print "station_block_name", station_block_name
                # print "penultimate_block_name", e.getItem("penultimate_block_name")
                # print "last_block_name", e.getItem("last_block_name")
                # print "***************"
                if e.getItem("penultimate_block_name") == neighbor_name and e.getItem("last_block_name") == station_block_name:
                    edge = e
                    break1 = True
            if break1 == True:
                break
        if edge == None:
            print "Error the required block has not been found. restart and try again. Sorry!"
            return None
        penultimate_block_name = edge.getItem("penultimate_block_name")
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        penultimate_layout_block = LayoutBlockManager.getLayoutBlock(penultimate_block_name)
        return penultimate_layout_block

    def in_siding(self, station_block_name):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        station_block = LayoutBlockManager.getLayoutBlock(station_block_name)
        number_neighbors = station_block.getNumberOfNeighbours()
        #print "station block number neighbors", number_neighbors
        in_siding = (number_neighbors == 1)
        return in_siding

    def set_train_direction(self, block_name, in_siding):

        options = ["forward", "reverse"]
        default = "forward"
        if in_siding:
            msg = "In block: " + block_name + "\n" +'What way is train facing\ntowards buffer?'
        else:
            msg = "In block: " + block_name + "\n" +'What way is train facing\ntowards highlighted block?'
        title = "Set Train Facing Direction"
        type = JOptionPane.QUESTION_MESSAGE
        self.od.CLOSED_OPTION = True
        while self.od.CLOSED_OPTION == True:
            result = self.od.customQuestionMessage2str(msg, title, "forward", "reverse")
            if self.od.CLOSED_OPTION == True:
                self.od.displayMessage("Sorry Can't Cancel at this point")

        if in_siding:
            if result == "reverse":
                train_direction = "reverse"
            else:
                train_direction = "forward"
        else:
            if result == "forward":
                train_direction = "reverse"
            else:
                train_direction = "forward"
        return train_direction


    def set_train_in_block(self, block_name, train_name):
        mem_val = train_name
        self.set_blockcontents(block_name, mem_val)

    def reset_train_in_blocks(self, train_name):
        for block in blocks.getNamedBeanSet():
            #print "block name", block.getUserName(), "block.getValue()" , block.getValue()
            if block.getValue() == train_name:
                #print "yes"
                block.setValue("")
                #print "block name yes", block.getUserName(), "block.getValue()" , block.getValue()


    def trains_in_sections_allocated(self):
        trains_in_sections_allocated = []
        #trains_in_sections_nonallocated = []
        for station_block_name in g.station_block_list:
            block_value = self.get_blockcontents(station_block_name)
            block_occupied_state = self.check_sensor_state_given_block_name(station_block_name)
            if block_occupied_state == True:
                if block_value not in trains_allocated:
                    trains_in_sections_allocated.append([station_block_name, block_value, "non-allocated"])
                elif (block_value != None and block_value != "" and block_value != "none"):
                    trains_in_sections_allocated.append([station_block_name, block_value, "allocated"])
                else:
                    trains_in_sections_allocated.append([station_block_name, block_value, "other"])
        if self.logLevel > 0: print str(trains_in_sections_allocated)
        return trains_in_sections_allocated


    def check_new_train_in_siding(self):

        # go through all station
        global trains_allocated

        for station_block_name in g.station_block_list:

            #get a True if the block block_value has the train name in it
            block_value = self.get_blockcontents(station_block_name)
            if self.logLevel > 0: print " a trains_allocated:", trains_allocated, ": block_value", block_value

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
