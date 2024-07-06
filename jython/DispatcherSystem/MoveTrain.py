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
import time

from javax.swing import JTable, JScrollPane, JFrame, JPanel, JComboBox,  BorderFactory, DefaultCellEditor, JLabel, UIManager, SwingConstants, JFileChooser
from javax.swing.table import  TableCellRenderer, DefaultTableCellRenderer
from java.awt.event import MouseAdapter,MouseEvent, WindowListener, WindowEvent
from java.awt import GridLayout, Dimension, BorderLayout, Color
from javax.swing.table import AbstractTableModel, DefaultTableModel
from java.lang.Object import getClass
from jmri.jmrit.logix import WarrantPreferences
import jarray
from javax.swing.event import TableModelListener, TableModelEvent
from javax.swing.filechooser import FileNameExtensionFilter
from org.apache.commons.io import FilenameUtils
from java.io import File
#, defaultTableModel


#import platform

class MoveTrain(jmri.jmrit.automat.AbstractAutomaton):

    global trains_dispatched
    global trains
    global time_last_train

    def __init__(self, station_from_name, station_to_name, train_name, graph, stop_mode = None):
        self.logLevel = 0
        self.station_from_name = station_from_name
        self.station_to_name = station_to_name
        self.train_name = train_name
        self.graph = graph
        self.stop_mode = stop_mode

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
        # print "move_between_stations"
        if self.logLevel > 0: print "Moving from " + station_from_name + " to " + station_to_name
        i = 0
        if self.logLevel > 0: print "checking train in start block"
        while self.check_train_in_start_block(train_name, station_from_name) == False:
            if i > 2: # allow some time to recover
                title = ""
                msg = "Cannot run train, train not in start block\n" + \
                      train_name + " should be in block " + station_from_name + \
                      "\nmove it there manually and it might recover"
                opt1 = "have moved train, try again"
                opt2 = "cancel moving train"
                reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
                if reply == opt1:
                    pass
                else:  #opt2
                    return
            self.waitMsec(5000)
            i += 1
        # print "move_between_stations a"
        if self.logLevel > 0: print "train is in start block"
        #need to look up the required transit in the graph
        StateVertex_start = station_from_name
        StateVertex_end = station_to_name
        # for e in graph.edgeSet():
        # if self.logLevel > 1: print (graph.getEdgeSource(e) + " --> " + graph.getEdgeTarget(e))
        if self.logLevel > 0: print "calling shortest path", StateVertex_start, StateVertex_end
        paths = DijkstraShortestPath.findPathBetween(graph, StateVertex_start, StateVertex_end)
        # print "move_between_stations b"
        if paths == None:
            # print "cannot find shortest path, paths found is empty"
            # print "end of move between ", station_from_name, station_to_name
            return

        if self.logLevel > 1: print "graph", graph
        if self.logLevel > 1: print "paths", paths
        if self.logLevel > 1: print "returned from shortest path"
        if self.logLevel > 0: print "in move_between_stations trains = ", trains, "train_name = ", train_name

        if train_name in trains:
            train = trains[train_name]
        else:
            print "in case of key error: trains", trains
            print "******"
            print "train_name", train_name
            print "************Not Moving Train************"
            return
        if self.logLevel > 1: print "train" , train
        penultimate_block_name = train["penultimate_block_name"]
        if self.logLevel > 1: print "penultimate_block_name" , penultimate_block_name
        previous_edge = train["edge"]
        previous_direction = train["direction"]

        trains_dispatched.append(str(train_name))

        count_path = 0
        # print "move_between_stations c"
        if paths == None or paths == []:
            print "1Error cannot find shortest path. restart the system. " + \
                  "The stop dispatcher system routine does not work properly with multiple layout panels. Sorry"
            return

        for e in paths:
            # print "move_between_stations d"
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

            # wait for the allocated time
            speech_reqd = self.speech_required_flag()
            # print "move_between_stations e"
            # wait in station and announce the wait time (announcement only for debugging)
            if 'transit_direction' not in locals():
                td = "forwards"
            else:
                td = transit_direction
            time_to_stop_in_station = self.get_time_to_stop_in_station(e, td)
            t = time_to_stop_in_station / 1000
            msg = "started waiting for " + str(int(t)) + " seconds"
            if self.logLevel > 0: self.speak(msg)
            # self.speak(msg)
            self.waitMsec(int(time_to_stop_in_station))
            msg = "finished waiting for " + str(int(t)) + " seconds"
            if self.logLevel > 0: self.speak(msg)
            #self.speak(msg)
            # print "move_between_stations f"
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
                # make announcement as train enters platform
                # print "making announcement"
                self.announce1(e, transit_direction, transit_instruction, train)
            iter = 0
            result = False
            # print "move_between_stations g"
            while result == False:
                # print "move_between_stations h move%%%%%%%%%%%%%%%%"
                result = self.move(e, transit_direction, transit_instruction,  train_name)
                if self.logLevel > 1: print "returned from self.move, result = ", result
                if result == False:
                    if str(train_name) in trains_dispatched:
                        trains_dispatched.remove(str(train_name))
                if iter >= 1: #allow one retry without prompting
                    msg = "Failure to dispatch train " + train + " retrying moving from " + from_name + " to " + to_name
                    title = ""
                    opt1 = "try again"
                    opt2 = "cancel"
                    reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
                    if opt1:
                        pass
                    else:
                        break
                iter += 1
            # print "move_between_stations h"
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

        # We have two cases for the diretion to be changed:
        # 1) we have a back and forth situation where we can check that the previous_block == next_block
        # 2) we reverse and go through a point. there will be no through path from the previous_block to the next next_block
        #
        # these two cases are not exclusive.

        # print "set_direction"
        transit_instruction = "same"

        # if previous_block == next_block:
        #     transit_instruction = "change"

        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        current_layout_block = LayoutBlockManager.getLayoutBlock(current_block)
        if not current_layout_block.validThroughPath(previous_block, next_block):
            transit_instruction = "change"

        if transit_instruction == "change":
            if previous_direction == "forward":
                transit_direction = "reverse"
            else:
                transit_direction = "forward"
        else:
            transit_direction = previous_direction
        return [transit_direction, transit_instruction]

    def check_train_in_start_block(self, train_to_move, blockName):
        # print "check_train_in_start_block"
        # print "checking " , train_to_move, " in " , blockName
        block = blocks.getBlock(blockName)
        if self.blockOccupied(block):
            # print " block Occupied ", self.blockOccupied(block), " value ", block.getValue()
            if block.getValue() == train_to_move:
                return True
            else:
                startBlock = block.getUserName()
                # print "trying to move from blockName" , blockName, "but not occupied by", "train_to_move", train_to_move
                blockName = [block.getUserName() for block in blocks.getNamedBeanSet() if block.getValue() == train_to_move]
                if blockName != []:
                    blockName = blockName[0]
                    # print "train", train_to_move, "actually in" , blockName
                    return False
                else:
                    blockName = "train not in any block"
                    #as the block
                    block.setValue(train_to_move)
                    # print "train", train_to_move, "reset in" , blockName
                    return True
        else:
            # print "train_to_move", train_to_move, "not in" , blockName
            blockName = [block for block in blocks.getNamedBeanSet() if block.getValue() == train_to_move]
            if blockName != []:
                blockName = blockName[0]
            else:
                blockName = "train not in any block"
            # print "train_to_move", train_to_move, "in" , blockName
            return False

    def blockOccupied(self, block):
        # print "blockOccupied"
        if block.getState() == ACTIVE:
            state = True
        else:
            state = False
        return state

    def get_time_to_stop_in_station(self, edge, direction):

        # print "get_time_to_stop_in_station"

        if direction == "forward":
            filename_fwd = self.get_filename(edge, "fwd")
            trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
            station_wait_time = trainInfo_fwd.getWaitTime()
        else:
            filename_rvs = self.get_filename(edge, "rvs")
            # print filename_rvs
            trainInfo_rvs = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_rvs)
            station_wait_time = trainInfo_rvs.getWaitTime()
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
        # print "announce1"
        to_name = e.getTarget()
        from_name = e.getSource()
        speech_reqd = self.speech_required_flag()
        self.announce( from_name, to_name, speech_reqd, direction, instruction)

    def move(self, e, direction, instruction, train):
        # print "move"
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
        # print "calling move", train, from_name, to_name
        # print "move a"
        result = self.call_dispatch(e, direction, train)

        if self.logLevel > 1: print "______________________"
        if result == True:
            #if self.train_name == "shunter": print "     ",
            #print "result from calling move is True!!", train, from_name, to_name
            # Wait for the Active Trains List to not have the train monotored in it
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
                # active_train_names_list = [str(t.getTrainName()) for t in java_active_trains_Arraylist]
                java_active_trains_Arraylist= java.util.ArrayList(java_active_trains_list)
                active_train_names_list = [str(t.getTrainName()) for t in java_active_trains_Arraylist]
                if self.logLevel > 1: print "!!!!!!!! train = ", train, "active_train_names_list", active_train_names_list
            self.set_sensor(sensor_move_name, "inactive")
            if self.logLevel > 1: print ("+++++ sensor " + sensor_move_name + " inactive")
        else:
            if self.train_name == "shunter": print "     ",
            # print "result from calling move is False!!", train, from_name, to_name
            self.set_sensor(sensor_move_name, "inactive")
        return result

    def speech_required_flag(self):
        # print "speech_required_flag"
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
        # print "call dispatch"
        global check_action_route_flag
        global check_route_flag

        # print "call_dispatch"

        # for information only
        if self.logLevel > 1: print ("in dispatch")
        to_name = e.getTarget()
        from_name = e.getSource()
        if self.logLevel > 1: print ("in call_dispatch: move from " + from_name + " to " + to_name)

        # set traininfo filename
        if direction == "forward":
            filename = self.get_filename(e, "fwd")
        else:
            filename = self.get_filename(e, "rvs")
        if self.logLevel > 1: print "filename = ", filename, "direction = " , direction
        # print "call_dispatch a"
        check_route_active_flag = sensors.getSensor("checkRouteSensor").getKnownState()
        if check_route_active_flag == ACTIVE:
            check_route_flag = True
        else:
            check_route_flag = False
        if self.logLevel > 0: print "check_route_flag", check_route_flag
        # print "call_dispatch b"
        # initialise globals to False if not set
        if 'check_action_route_flag' not in globals():
            check_action_route_flag = False

        # wait for blocks to be clear before allocating (if required)
        if check_route_flag == True or check_action_route_flag == True:  # can ask for route to be checked globally or in action
            # print "call_dispatch b1"
            i = 0
            self.wait_route_is_clear(filename, from_name)
            # print "call_dispatch b2"
            #self.do_not_start_trains_simultaneously()
            # self.set_route_allocated(filename, from_name)  # can't wait for dispatcher to do this
                                        # else other routes will be allocated simultaneously
                # i += 1
                # if i == 1:
                #     if self.train_name == "shunter": print "     ",
                #     if self.logLevel > 0: print "waiting for route", filename, "to be clear"
                # self.waitMsec(2000)
        # mark as allocating
        # print "call_dispatch c"
        t = trains[self.train_name]   #train is train_name
        # print "call_dispatch a  $$$$$"
        t["allocating"] = True

        if self.logLevel > 0 and self.train_name == "shunter": print "     ",
        if self.logLevel > 0: print self.train_name, "route", filename, "is clear"
        # run dispatch
        result = False
        iter = 0
        while result == False:
            result = self.doDispatch(filename, "ROSTER", train)
            # if we failed to run the transit try again once before letting the operator have a go
            if result == False:
                self.waitMsec(5000)  # wait 5 secs
                msg = "Failure to dispatch train " + train + " retrying moving from " + from_name + " to " + to_name + "."
                title = ""
                opt1 = "try again"
                opt2 = "cancel"
                if iter >= 1: # allow one auto retry
                    reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
                    if opt1:
                        pass
                    else:
                        break
            iter += 1

        #return result
        if self.logLevel > 1: print "result", result
        return result

    def initialise_if_not_set(self, global_name, state):
         if 'global_name' not in globals():
            global_name = state

    def get_filename(self, e, suffix):

        # print "get_filename"

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

        # print "doDispatch"

        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        if self.logLevel > 1: print "traininfoFileName",traininfoFileName
        train_name = value
        train = trains[train_name]
        self.trainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(traininfoFileName)
        self.modify_trainInfo(train_name)  # sets the speed factor and other train dependent factors
        result = DF.loadTrainFromTrainInfo(self.trainInfo, type, value)
        if result == 0:
            self.set_whether_to_stop_at_sensor(DF)
            train["allocating"] = False   # this flag is used when checking to see whether path for dispatch is clear
        if result == -1:
            print "error: result from dispatcher frame" , result
            # delete the transit so can try loading the transit again
            self.trainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(traininfoFileName)
            transit_name = self.trainInfo.getTransitName()
            active_train_list = [active_train for active_train in DF.getActiveTrainsList() \
                            if active_train.getTransitName() == transit_name]
            if active_train_list == []:
                active_train = None
            else:
                active_train = active_train_list[0]
                DF.terminateActiveTrain(active_train)
                train_name == value
                if train_name in trains:
                    trains.remove(train_name)
            return False  #No train allocated
        else:
            DF = None
            return True

    def get_train_length(self, new_train_name):

        # print "get_train_length"

        EngineManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.engines.EngineManager)
        engineRoad = "Set by Dispatcher System"
        engineNumber = new_train_name
        engine = EngineManager.newRS(engineRoad, engineNumber)
        # get the current length of the engine
        default = "10"
        current_length = engine.getLength()
        if current_length == "0":
            current_length = default
        return [engine, current_length]

    def get_train_speed_factor(self, new_train_name):

        # print "get_train_speed_factor"
        EngineManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.engines.EngineManager)
        engineRoad = "Set by Dispatcher System"
        engineNumber = new_train_name
        engine = EngineManager.newRS(engineRoad, engineNumber)
        #get the current speed factor of the engine
        default = "100"  # prcentage
        comment = engine.getComment()
        split_comment = []
        if "speed factor" in comment:
            split_comment = comment.split(" ")
            index = split_comment.index("speed")
            # print "len(split_comment)", len(split_comment), "index + 2", index + 2
            if len(split_comment) > index + 2:
                speed_factor = split_comment[index+2]
            else:
                speed_factor = default
        else:
            speed_factor = default
        return [engine, speed_factor]

    def modify_trainInfo(self, train_name):

        # print "modify_trainInfo"

        [engine,current_length] = self.get_train_length(train_name)  #get the engine name
        # print "in modify_trainInfo1a length = ", current_length
        self.trainInfo.setMaxTrainLength(float(current_length))
        # print "in modify_trainInfo1 "
        [engine,current_speed_factor] = self.get_train_speed_factor(train_name)
        # print "in modify_trainInfo2 ", current_speed_factor
        speedFactor = float(current_speed_factor)/100.0
        # print "in modify_trainInfo2  speedFactor", speedFactor
        # print "in modify_trainInfo2 a"
        if speedFactor >= 2 or speedFactor <=0:
            speedFactor = 1
            msg = "speedFactor set is out of range " + str(current_speed_factor) + "\nSpeed Factor set to 100% " + "for train " + train_name
            OptionDialog().displayMessage(msg)
        self.trainInfo.setSpeedFactor(float(speedFactor))
        if self.logLevel > 0: print "self.forward_stopping_sensor_exists(self.trainInfo)",self.forward_stopping_sensor_exists(self.trainInfo)
        # print "sensors.getSensor('stopAtStopSensor').getKnownState()", sensors.getSensor("stopAtStopSensor").getKnownState(), ACTIVE

    def set_whether_to_stop_at_sensor(self, DF):

        # print "set_whether_to_stop_at_sensor"

        transit_name = self.trainInfo.getTransitName()
        if self.logLevel > 0: print "transit_name", transit_name
        active_train_list = [active_train for active_train in DF.getActiveTrainsList() \
                             if active_train.getTransitName() == transit_name]
        if self.logLevel > 0: print "active_train_list", active_train_list

        active_train = active_train_list[0]
        if self.logLevel > 0: print "active_train", active_train
        autoActiveTrain = active_train.getAutoActiveTrain()
        if self.forward_stopping_sensor_exists(self.trainInfo):
            if self.logLevel > 0: print "forward_stopping_sensor_exists"
            # set default
            if sensors.getSensor("stopAtStopSensor").getKnownState() == ACTIVE:
                if self.logLevel > 0: print "stop at stop sensor active", sensors.getSensor("stopAtStopSensor").getKnownState(), ACTIVE
                autoActiveTrain.set_useStopSensor(True)
            else:
                if self.logLevel > 0: print "stop at stop sensor inactive", sensors.getSensor("stopAtStopSensor").getKnownState(), INACTIVE
                if self.logLevel > 0: print "before", self.trainInfo.getStopBySpeedProfile(), self.trainInfo.getUseSpeedProfile()
                autoActiveTrain.set_useStopSensor(False)
            # overwrite with set values
            if self.stop_mode == None:
                if self.logLevel > 0: print "pass"
                pass
            elif self.stop_mode == "Use Stop Sensor":
                autoActiveTrain.set_useStopSensor(True)
                if self.logLevel > 0: print "set stop sensor true"
            elif self.stop_mode == "Stop using Speed Profile":
                autoActiveTrain.set_useStopSensor(False)
                if self.logLevel > 0: print "set_useStopSensor false"
            else:
                print "ERROR incorrect value for stop mode"
        else:
            if self.logLevel > 0: print "forward_stopping_sensor does not exist"

    def forward_stopping_sensor_exists(self, traininfo):

        # print "forward_stopping_sensor_exists"

        transit_name = traininfo.getTransitId()
        transit = transits.getTransit(transit_name)
        transit_section_list = transit.getTransitSectionList()
        section_list = transit.getSectionListBySeq(transit.getMaxSequence())
        section = section_list[0]
        forward_stopping_sensor = section.getForwardStoppingSensor()
        if forward_stopping_sensor != None:
            return True
        else:
            return False
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

        # print "wait_sensor"

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

        # print "announce"

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

        # print "get_station_name"

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

        # print "bell"

        if bell_on == "True":
            snd = jmri.jmrit.Sound("resources/sounds/Bell.wav")
            snd.play()

    def do_not_start_trains_simultaneously(self):

        # print "do_not_start_trains_simultaneously"

        global time_last_train
        time_now = int(round(time.time() * 1000))
        time_train = math.max(time_now,time_last_train)
        time_to_wait = time_train - time_now
        time_last_train = time_train + 1000  #store the time this train will start
        self.waitMsec(time_to_wait)  #make thiis train wait for 1 sec after previous train

    def wait_route_is_clear(self, traininfoFileName, startBlockName):

        # print "wait_route_is_clear"

        index = 0
        while index < 1: #requre 1 occurrences of route not occupied
            route_is_occupied = self.check_route_is_allocated_or_occupied(traininfoFileName, startBlockName)
            # print "wait_route_is_clear a route_is_occupied", route_is_occupied
            if route_is_occupied:
                index = 0
                if self.train_name == "shunter": print "     ",
                if self.logLevel > 0: print "waiting for route", traininfoFileName, "to be clear", "index", index
                self.waitMsec(1000)
            else:
                index+=1
        # print "wait_route_is_clear end"

    def check_route_is_allocated_or_occupied(self, traininfoFileName, startBlockName):

        # print "check_route_is_allocated_or_occupied"

        [transit_name, transit_id] = self.get_transit(traininfoFileName)

        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        transit = TransitManager.getTransit(transit_name)
        if self.logLevel > 0: print "transit_name", transit_name, "transit_id", transit_id
        block_list = [block for block in transit.getInternalBlocksList() if block.getUserName() != startBlockName]
        if self.logLevel > 0: print "block_list", [block.getUserName() for block in block_list]
        route_is_occupied = False

        #add the block that must be clear for the transit to run
        trainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(traininfoFileName)
        transit_block_name = trainInfo.getBlockName()
        if transit_block_name != "":
            transit_block = blocks.getBlock(transit_block_name)
            # print "appending", transit_block_name, transit_block
            block_list.append(transit_block)

        index = 0
        for block in block_list:
            index += 1
            #getValue is set if the track is occupied
            if block.getSensor().getKnownState() == ACTIVE:  # check if block is occupied
                route_is_occupied = True
                if self.logLevel > 0: print block.getUserName() , "is not clear value =", block.getSensor().getKnownState(), index
                break
            else:
                if self.logLevel > 0: print block.getUserName() , "is clear value =", block.getSensor().getKnownState(), index
            # getExtraColor is set if the block is allocated
            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
            layoutBlock = LayoutBlockManager.getLayoutBlock(block)
            if layoutBlock.getUseExtraColor():
                route_is_occupied = True
                if self.logLevel > 0: print block.getUserName() , "is not clear (extracolor) value =", block.getSensor().getKnownState(), index
                break
        if self.logLevel > 0: print "route_is_occupied", route_is_occupied; print
        # if route_is_clear == False:
        #     print "Train ON TRAINSIT",transit_name, " WAITING"

        # print "trains_allocated", trains_allocated

        for train_name in trains_allocated:
            train = trains[train_name]
            if train["allocating"] == True:
                route_is_occupied = True   # only allow one dispatch to be set up at a time else this routine does not work
                # we don't want to check that the route is clear, and then have an allocation take place immediately after
        return route_is_occupied
        
    def set_route_allocated(self, traininfoFileName, startBlockName):

        # print "set_route_allocated"

        [transit_name, transit_id] = self.get_transit(traininfoFileName)
        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        transit = TransitManager.getTransit(transit_name)
        if self.logLevel > 0: print "transit_name", transit_name, "transit_id", transit_id
        block_list = [block for block in transit.getInternalBlocksList() if block.getUserName() != startBlockName]
        for block in block_list:
            # getExtraColor is set if the block is allocated
            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
            layoutBlock = LayoutBlockManager.getLayoutBlock(block)
            layoutBlock.setUseExtraColor(True)
        if self.train_name == "shunter": print "     ",
        if self.logLevel > 0: print "allocated route", traininfoFileName

    def get_transit(self, filename):

        # print "get_transit"

        trainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename)
        transit_name = trainInfo.getTransitName()
        transit_id = trainInfo.getTransitId()
        return  [transit_name, transit_id]



class NewTrainMaster(jmri.jmrit.automat.AbstractAutomaton):

    # responds to the newTrainSensor, and allocates trains available for dispatching
    # we make the allocated flag global as we will use it in DispatchMaster when we dispatch a train

    global trains_allocated

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
        if 'trains_allocated' not in globals():
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
        title = "Setup trains"
        msg = "setup one or more trains"
        opt1 = "1 train"
        opt2 = "several trains"
        opt3 = "check/swap train direction"
        #opt4 = "reset trains"
        action = self.od.customQuestionMessage3str(msg, title, opt1, opt2, opt3)
        #msg = "choose"
        #actions = ["setup 1 train","setup several trains", "check/swap train direction", "reset trains"]
        #action = self.od.List(msg, actions)
        if action == "1 train":
            # msg = "choose"
            # actions = ["setup 1 train","setup 2+ trains"]
            # action = self.od.List(msg, actions)
            # if action == "setup 1 train":
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
                        # msg = self.get_all_trains_msg()
                        # title = None
                        # opt1 = "Select section"
                        # s = self.od.customMessage(msg, title, opt1)
                        # if self.logLevel > 0: print "station_block_name",station_block_name, "s", s
                        # if self.od.CLOSED_OPTION == False:

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
                                        self.set_speed_factor(new_train_name)
                else:
                    if self.logLevel > 0 : print "!!!!5"
                    trains_to_choose = self.get_non_allocated_trains()
                    msg = "In " + station_block_name + " Select train roster"
                    new_train_name = modifiableJComboBox(trains_to_choose,msg).return_val()
                    if new_train_name not in trains_allocated:
                        trains_allocated.append(new_train_name)

                    self.add_to_train_list_and_set_new_train_location(new_train_name, station_block_name)
                    self.set_blockcontents(station_block_name, new_train_name)
                    self.set_length(new_train_name)
                    self.set_speed_factor(new_train_name)
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
        elif action == "several trains":
            createandshowGUI(self)

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
        # elif action == "check/swap train direction" #"check train direction"  Keep in case new code has errors
        #     all_trains = self.get_all_roster_entries_with_speed_profile()
        #     if all_trains == []:
        #         msg = "There are no engines with speed profiles, cannot operate without any"
        #         JOptionPane.showMessageDialog(None,msg)
        #     else:
        #         # msg = self.get_allocated_trains_msg()
        #         # title = None
        #         # opt1 = "Select section"
        #         # s = self.od.customMessage(msg, title, opt1)
        #         # if self.logLevel > 0: print "station_block_name",station_block_name, "s", s
        #         # if self.od.CLOSED_OPTION == False:
        #
        #         msg = "Select section"
        #         sections_to_choose = self.get_allocated_trains_sections()
        #         new_section_name = self.od.List(msg, sections_to_choose)
        #         if self.od.CLOSED_OPTION == False:
        #             msg = "Select the train in " + new_section_name
        #             trains_to_choose = self.get_allocated_trains()
        #             if trains_to_choose == []:
        #                 s = OptionDialog().displayMessage("no more trains with speed profiles \nto select")
        #             else:
        #                 new_train_name = self.od.List(msg, trains_to_choose)
        #                 if self.od.CLOSED_OPTION == False:
        #                     #print "need to find the direction of train", new_train_name
        #                     self.check_train_direction(new_train_name, new_section_name)
        elif action == "check/swap train direction":
            trains_to_choose = self.get_allocated_trains()
            if trains_to_choose == []:
                s = OptionDialog().displayMessage("no allocated trains to select")
            else:
                msg = "Select the required train"
                new_train_name = self.od.List(msg, trains_to_choose)
                train_block = [block.getUserName() for block in blocks.getNamedBeanSet() if block.getValue() == new_train_name][0]
                if train_block is not None:
                    self.check_train_direction(new_train_name, train_block)
        return True

    def check_train_direction(self, train_name, station_block_name):
        global train
        if train_name in trains:
            train = trains[train_name]
            direction = train["direction"]
            penultimate_layout_block = self.get_penultimate_layout_block(station_block_name)

            saved_state = penultimate_layout_block.getUseExtraColor()
            in_siding = self.in_siding(station_block_name)

            closed = False
            while closed == False:
                penultimate_layout_block.setUseExtraColor(True)
                direction = train["direction"]
                msg = "train travelling " + self.swap_direction(direction) + " towards highlighted block"
                title = "swap directions of " +  train_name
                opt1 = "swap direction"
                opt2 = "Close"
                s = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
                if s == JOptionPane.CLOSED_OPTION:
                    closed = True
                if s == opt1:
                    self.swap_train_direction(train_name)
                    closed = False
                if s == opt2:
                    closed = True
            penultimate_layout_block.setUseExtraColor(saved_state)

    def swap_train_direction(self, train_name):
        global train
        if train_name in trains:
            train = trains[train_name]
            direction = train["direction"]
            train["direction"] = self.swap_direction(direction)

    def swap_direction(self, direction):
        if direction == "reverse":
            direction = "forward"
        else:
            direction = "reverse"
        return direction


    def get_train_length(self, new_train_name):
        EngineManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.engines.EngineManager)
        engineRoad = "Set by Dispatcher System"
        engineNumber = new_train_name
        engine = EngineManager.newRS(engineRoad, engineNumber)
        #get the current length of the engine
        default = "10"
        current_length = engine.getLength()
        # print "current_length", current_length
        # print "type", type(current_length) , "test", str(current_length) == "0"
        if str(current_length) == "0":
            current_length = default     # current length is in unicode
        # print "current_length2", current_length
        return [engine, current_length]

    def set_length(self, new_train_name):

        # jim = "{:.2f}".format( num )
        # print "jim", jim

        title = "Set the length of the engine/train"
        # msg = str(fred)
        request = "Change"
        while request == "Change":
            [engine,current_length] = self.get_train_length(new_train_name)
            # print "current_length3", current_length
            # current_length is an integer, and is set to a default of 10 scale metres
            gauge = WarrantPreferences.getDefault().getLayoutScale()
            length_in_cm_float = (float(current_length) / gauge) * 100.0
            length_in_cm_str = str("{:.2f}".format(length_in_cm_float))
            length_in_inches_float = (float(current_length) / gauge / 2.54) * 100.0
            length_in_inches_str = str("{:.2f}".format(length_in_inches_float))
            msg = "<html>length of " + new_train_name + " = " + str(current_length) + " scale metres" + \
                    "<br> gauge        : " + "1:" + str(int(gauge)) + \
                    "<br> length in cm : " + length_in_cm_str + \
                    "<br> length in inches: " + length_in_inches_str
            opt1 = "OK"
            opt2 = "Change"
            request = self.od.customQuestionMessage2str(msg,title,opt1, opt2)
            if request == "Change":
                #set the new length
                msg = "input length of " + new_train_name + " in scale metres"
                title = "length of " + new_train_name
                default_value = current_length
                new_length = self.od.input(msg, title, default_value)
                engine.setLength(new_length)

    def get_train_speed_factor(self, new_train_name):
        EngineManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.engines.EngineManager)
        engineRoad = "Set by Dispatcher System"
        engineNumber = new_train_name
        engine = EngineManager.newRS(engineRoad, engineNumber)
        #get the current speed factor of the engine
        default = "100"  # prcentage
        comment = engine.getComment()
        split_comment = []
        if "speed factor" in comment:
            split_comment = comment.split(" ")
            index = split_comment.index("speed")
            # print "len(split_comment)", len(split_comment), "index + 2", index + 2
            if len(split_comment) > index + 2:
                speed_factor = split_comment[index+2]
            else:
                speed_factor = default
        else:
            speed_factor = default
        return [engine, speed_factor]

    def set_speed_factor(self, new_train_name):
        [engine, current_speed_factor] = self.get_train_speed_factor(new_train_name)
        # if current_length == "0":
        #     default = "10"
        #     current_speed_factor = default
        #     engine.setSpeedFactor(default)
        #ask if want to change length
        title = "Scale the speed of the engine/train"
        msg = "speed factor of " + new_train_name + " = " + str(current_speed_factor) + "%"
        opt1 = "OK"
        opt2 = "Change"
        request = self.od.customQuestionMessage2str(msg,title,opt1, opt2)
        if request == "Change":
            #set the new speed factor
            msg = "input speed factor % of " + new_train_name
            title = "speed factor of " + new_train_name
            default_value = current_speed_factor
            new_speed_factor= self.od.input(msg, title, default_value)
            new_speed_factor = "speed factor " + new_speed_factor
            engine.setComment(new_speed_factor)

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

    def get_all_sections(self):
        return [section for section in sections.getNamedBeanSet()]

    def get_all_blocks(self):
        return [block for block in blocks.getNamedBeanSet()]
    
    def get_sections_for_trains_in_table(self, trains_in_table):
        return [str(train) for train in trains_in_table]

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
        #     # print "e" , e
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
        penultimate_layout_block.setUseExtraColor(True)
        [train_direction,result] = self.set_train_direction(station_block_name, in_siding)
        #check the condition set in set_train_direction
        train["direction"] = train_direction
        penultimate_layout_block.setUseExtraColor(saved_state)
        # print "edge" , edge
        # print "penultimate_block_name", penultimate_block_name
        # print "train_direction", train_direction

        # 4) add to allocated train list
        if str(train_name) not in trains_allocated:
            trains_allocated.append(str(train_name))
        if "allocating" not in train:
            train["allocating"] = False
        # print "done"

    def add_to_train_list_and_set_new_train_location0(self, train_name, station_block_name,
                                                      train_direction, train_length, train_speed_factor):

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

        # penultimate_block_name = edge.getItem("penultimate_block_name")
        # penultimate_layout_block = LayoutBlockManager.getLayoutBlock(penultimate_block_name)
        # saved_state = penultimate_layout_block.getUseExtraColor()
        # if not in_siding:
        #     # highlight the penultimate block
        #     penultimate_layout_block.setUseExtraColor(True)
        # train_direction = self.set_train_direction(station_block_name, in_siding)
        #check the condition set in set_train_direction

        train["direction"] = train_direction
        #penultimate_layout_block.setUseExtraColor(saved_state)
        # print "edge" , edge
        # print "penultimate_block_name", edge.getItem("penultimate_block_name")
        # print "train_direction", train_direction

        # 4) add to allocated train list
        if str(train_name) not in trains_allocated:
            trains_allocated.append(str(train_name))

        [engine,current_length] = self.get_train_length(train_name)  #get the engine name
        engine.setLength(str(train_length))    # save the length provided in the parameter

        #[engine,current_speed_factor] = self.get_train_speed_factor(train_name)
        speed_factor_str = "speed factor " + train_speed_factor
        engine.setComment(speed_factor_str)

        train["allocating"] = False

    def add_to_train_list_and_set_new_train_location2(self, train_name, station_block_name):

        # 1)
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
            # print("set_train_in_block")
            self.set_train_in_block(station_block_name, train_name)

        # print("calling highlight_penultimate_block")
        [edge, train_direction, result]  = self.highlight_penultimate_block(station_block_name)
        if edge == "Error" :
            print "error"
            return

        # print edge, train_direction, result

        #check the condition set in set_train_direction
        train["direction"] = train_direction
        train["edge"] = edge
        train["penultimate_block_name"] = edge.getItem("penultimate_block_name")

        # 4) add to allocated train list
        if str(train_name) not in trains_allocated:
            trains_allocated.append(str(train_name))

    def add_to_train_list_and_set_new_train_location1(self, train_name, station_block_name):

        #     self.allocate_train(train_name)
        #     [edge, train_direction, result]  = self.highlight_penultimate_block(station_block_name)
        #     self.register_train(edge, train_direction)
        #
        #
        # def allocate_train(self, train_name):

        # 1)
        # trains is a dictionary, with keys of the train_name
        # each value is itself a dictionary with 3 items
        # edge
        # penultimate_block_name
        # direction
        global train
        global trains_allocated
        # print ("in add_to_train_list_and_set_new_train_location")
        if train_name not in trains:
            # print("train_name", train_name)
            # print("trains", trains)
            # print("train_name not in trains")
            trains[train_name] = {}
            train = trains[train_name]
            train["train_name"] = train_name
        else:
            #train_name = self.get_train_name()
            # print("set_train_in_block")
            self.set_train_in_block(station_block_name, train_name)

        # # print("calling highlight_penultimate_block")
        # [edge, train_direction, result]  = self.highlight_penultimate_block(station_block_name)
        #
        # #check the condition set in set_train_direction
        # train["direction"] = train_direction
        # train["edge"] = edge
        # train["penultimate_block_name"] = edge.getItem("penultimate_block_name")

        # 4) add to allocated train list
        if str(train_name) not in trains_allocated:
            trains_allocated.append(str(train_name))

    def highlight_penultimate_block(self, station_block_name):
        # print("highlight_penultimate_block")
        # 2) set the last traversed edge to the edge going into the siding
        edge = None
        j = 0
        #print "edge_before" , edge
        #print "g.g_stopping.edgesOf(station_block_name)",g.g_stopping.edgesOf(station_block_name)
        break1 = False
        #print "no edges", g.g_stopping.edgeSet()
        # for e in g.g_express.edgeSet():
            # print "e" , e
        for e in g.g_express.edgeSet():
            j+=1
            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
            station_block = LayoutBlockManager.getLayoutBlock(station_block_name)
            number_neighbors = station_block.getNumberOfNeighbours()
            #print "station block number neighbors", number_neighbors
            in_siding = (number_neighbors == 1)
            # print "in_siding", in_siding
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
            #print "******************************++"
        if edge == None:
            print "Error the required block has not been found. restart and try again. Sorry!"
            return ["Error", "Error", "Error"]

         # 3) set direction so can check direction of transit

        penultimate_block_name = edge.getItem("penultimate_block_name")
        penultimate_layout_block = LayoutBlockManager.getLayoutBlock(penultimate_block_name)
        saved_state = penultimate_layout_block.getUseExtraColor()
        if not in_siding:
            # highlight the penultimate block
            penultimate_layout_block.setUseExtraColor(True)
        penultimate_layout_block.setUseExtraColor(True)
        [train_direction, result] = self.set_train_direction(station_block_name, in_siding)
        penultimate_layout_block.setUseExtraColor(saved_state)

        return [edge, train_direction, result]

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
            # print "Error the required block has not been found. restart and try again. Sorry!"
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
        in_siding = False
        options = ["forward", "reverse"]
        default = "forward"
        self.od.CLOSED_OPTION = True
        while self.od.CLOSED_OPTION == True:
            if in_siding:
                msg = "In block: " + block_name + "\n" +'What way is train facing\ntowards buffer?'
            else:
                msg = "In block: " + block_name + "\n" +'What way is train facing\ntowards highlighted block?'
            title = "Set Train Facing Direction"
            type = JOptionPane.QUESTION_MESSAGE
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
        return [train_direction, result]


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

    def occupied_blocks_allocated(self):
        occupied_blocks = [block for [block, train, state] in self.trains_in_sections_allocated() if state == "allocated"]
        return occupied_blocks

    def occupied_blocks_not_allocated(self):
        # print "self.trains_in_sections_allocated()", self.trains_in_sections_allocated()
        occupied_blocks = [block for [block, train,  state] in self.trains_in_sections_allocated() if state == "non-allocated"]
        return occupied_blocks

    def train_blocks(self, train_list, in_list):
        occupied_blocks = \
        [station_block_name for station_block_name in g.station_block_list \
         if self.check_sensor_state_given_block_name(station_block_name) == True]

        # print "occupied_blocks", occupied_blocks
        # print "train_list", train_list
        self.get_blockcontents(station_block_name),
        if in_list:
            items_in_list = \
                [[self.get_blockcontents(block_name), block_name, self.check_sensor_state_given_block_name(block_name)] \
                            for block_name in occupied_blocks if self.get_blockcontents(block_name) in train_list]
            # [train_name , block_name, block_state] = items_in_list     # for clarity
            return items_in_list
        else:
            items_not_in_list = \
                [[self.get_blockcontents(block_name), block_name, self.check_sensor_state_given_block_name(block_name)] \
                            for block_name in occupied_blocks if self.get_blockcontents(block_name) not in train_list]
            # [train_name , block_name, block_state] = items_in_list     # for clarity
            return items_not_in_list

    def train_blocks_in_list(self,train_list):
        return self.train_blocks(train_list, True)
    def train_blocks_not_in_list(self,train_list):
        return self.train_blocks(train_list, False)


    # def trains_in_sections(self, train_list):
    #     # given the train list, return list of all trains [[station_block_name, block_value, msg],...]
    #     # where msg says whether item in list or not
    #     trains_in_sections = []
    #     for station_block_name in g.station_block_list:
    #         block_value = self.get_blockcontents(station_block_name)
    #         block_occupied_state = self.check_sensor_state_given_block_name(station_block_name)
    #         if block_occupied_state == True:
    #             if block_value not in train_list:
    #                 trains_in_sections.append([station_block_name, block_value, "non-in-list"])
    #             elif (block_value != None and block_value != "" and block_value != "none"):
    #                 trains_in_sections.append([station_block_name, block_value, "in-list"])
    #             else:
    #                 trains_in_sections.append([station_block_name, block_value, "other"])
    # if self.logLevel > 0: print str(trains_in_sections)
    # return trains_in_sections


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

class createandshowGUI(TableModelListener):

    def __init__(self, super):
        self.logLevel = 0
        self.super = super
        #Create and set up the window.

        self.initialise_model(super)
        self.frame = JFrame("Set up trains")
        self.frame.setSize(600, 600);

        self.completeTablePanel()
        # print "about to populate"
        self.populate_action(None)
        self.cancel = False


    def completeTablePanel(self):

        self.topPanel= JPanel();
        self.topPanel.setLayout(BoxLayout(self.topPanel, BoxLayout.X_AXIS))
        self.self_table()

        scrollPane = JScrollPane(self.table);
        scrollPane.setSize(600,600);

        self.topPanel.add(scrollPane);

        self.buttonPane = JPanel();
        self.buttonPane.setLayout(BoxLayout(self.buttonPane, BoxLayout.LINE_AXIS))
        self.buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10))

        # button_add = JButton("Add Row", actionPerformed = self.add_row_action)
        # self.buttonPane.add(button_add);
        # self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_apply = JButton("Save", actionPerformed = self.apply_action)
        self.buttonPane.add(button_apply)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_cancel = JButton("Close", actionPerformed = self.cancel_action)
        self.buttonPane.add(button_cancel)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_populate = JButton("Populate", actionPerformed = self.populate_action)
        self.buttonPane.add(button_populate);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_tidy = JButton("Tidy", actionPerformed = self.tidy_action)
        self.buttonPane.add(button_tidy);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_savetofile = JButton("Save To File", actionPerformed = self.savetofile_action)
        self.buttonPane.add(button_savetofile)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_loadfromfile = JButton("Load From File", actionPerformed = self.loadfromfile_action)
        self.buttonPane.add(button_loadfromfile)
        self.buttonPane.add(Box.createHorizontalGlue());

        contentPane = self.frame.getContentPane()

        contentPane.removeAll()
        contentPane.add(self.topPanel, BorderLayout.CENTER)
        contentPane.add(self.buttonPane, BorderLayout.PAGE_END)

        self.frame.pack();
        self.frame.setVisible(True)

        return
    def buttonPanel(self):
        row1_1_button = JButton("Add Row", actionPerformed = self.add_row_action)
        row1_2_button = JButton("Save", actionPerformed = self.save_action)

        row1 = JPanel()
        row1.setLayout(BoxLayout(row1, BoxLayout.X_AXIS))

        row1.add(Box.createVerticalGlue())
        row1.add(Box.createRigidArea(Dimension(20, 0)))
        row1.add(row1_1_button)
        row1.add(Box.createRigidArea(Dimension(20, 0)))
        row1.add(row1_2_button)

        layout = BorderLayout()
        # layout.setHgap(10);
        # layout.setVgap(10);

        jPanel = JPanel()
        jPanel.setLayout(layout);
        jPanel.add(self.table,BorderLayout.NORTH)
        jPanel.add(row1,BorderLayout.SOUTH)

        #return jPanel
        return topPanel

    def initialise_model(self, super):

        self.model = None
        self.model = MyTableModel()
        self.table = JTable(self.model)
        self.model.addTableModelListener(MyModelListener(self, super));
        pass
    def self_table(self):

        #table.setPreferredScrollableViewportSize(Dimension(500, 70));
        #table.setFillsViewportHeight(True)
        #self.table.getModel().addtableModelListener(self)
        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);
        #table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        # self.resizeColumnWidth(table)

        #renderer = DefaultTableCellRenderer()

        #renderer.setToolTipText("Click for combo box");
        # set first 3 cols to combobox
        # comboBox = [1,2,3]
        # column = [1,2,3]


        self.trainColumn = self.table.getColumnModel().getColumn(0);
        self.combobox0 = JComboBox()

        self.all_trains = self.super.get_all_roster_entries_with_speed_profile()
        self.non_allocated_trains = self.super.get_non_allocated_trains()
        for train in self.non_allocated_trains:
            self.combobox0.addItem(train)
        self.trainColumn.setCellEditor(DefaultCellEditor(self.combobox0));
        renderer0 = ComboBoxCellRenderer()
        self.trainColumn.setCellRenderer(renderer0);

        self.all_sections = self.super.get_all_sections()
        self.all_blocks = self.super.get_all_blocks()

        self.sectionColumn = self.table.getColumnModel().getColumn(1);
        self.combobox1 = JComboBox()
        self.sections_to_choose = self.super.get_non_allocated_trains_sections()
        for section in self.sections_to_choose:
            self.combobox1.addItem(section)
            #self.set_train_selections(combobox0)
        self.sectionColumn.setCellEditor(DefaultCellEditor(self.combobox1));
        renderer1 = ComboBoxCellRenderer()
        self.sectionColumn.setCellRenderer(renderer1);
        jpane = JScrollPane(self.table)
        panel = JPanel()
        panel.add(jpane)
        result = JScrollPane(panel)
        return self.table

    def add_row_action(self, e):
        model = e.getSource()
        data = self.model.getValueAt(0, 0)
        count = self.model.getRowCount()
        colcount = self.model.getColumnCount()
        self.model.add_row()
        self.completeTablePanel()

    def populate_action(self, event):
        column = 1  #block
        all_blocks = [block.getUserName() for block in self.all_blocks]
        blocks_in_table = [block for block in (self.model.getValueAt(r, column) for r in range(self.table.getRowCount())) if block in all_blocks]
        #blocks_in_table1 = [section for section in (self.model.getValueAt(r, column) for r in range(self.table.getRowCount())) ]
        # print "self.all_sections", all_sections
        # print "sections in table", blocks_in_table
        # print "sections in table1", blocks_in_table1
        # # # starting with non_allocated_trains remove the ones in my_train_list
        # # print "sections to choose", self.sections_to_choose
        # # print "trains_in_table",trains_in_table
        # # print "sections True", self.super.train_blocks(trains_in_table, True)
        # # print "sections False", self.super.train_blocks(trains_in_table, False)
        # # allocated_blocks = self.super.occupied_blocks_allocated()
        not_allocated_blocks = self.super.occupied_blocks_not_allocated()
        # print "not_allocated_blocks", not_allocated_blocks
        blocks_to_put_in_dropdown = [s for s in not_allocated_blocks if s not in blocks_in_table]
        # print "blocks_to_put_in_dropdown", blocks_to_put_in_dropdown
        self.model.populate(blocks_to_put_in_dropdown)
        # print "COMPLETING TABLE PANEL"
        self.completeTablePanel()

    def tidy_action(self,e):
        self.model.remove_not_set_row()
        self.completeTablePanel()

    def savetofile_action(self, event):

        #Tidy
        self.model.remove_not_set_row()
        self.completeTablePanel()

        if self.model.getRowCount() == 0:
            msg = "There are no valid rows"
            result = OptionDialog().displayMessage(msg)
            return

        msg = "Saving Valid rows"
        result = OptionDialog().displayMessage(msg)


        dir = self.directory()
        j = JFileChooser(dir);
        j.setAcceptAllFileFilterUsed(False)
        filter = FileNameExtensionFilter("text files txt", ["txt"])
        j.addChoosableFileFilter(filter);
        j.setDialogTitle("Select a .txt file");



        ret = j.showSaveDialog(None);
        if (ret == JFileChooser.APPROVE_OPTION) :
            file = j.getSelectedFile()
            if file == "" or file == None:
                msg = "No file selected"
                result = OptionDialog().displayMessage(msg)
                return
            if FilenameUtils.getExtension(file.getName()).lower() == "txt" :
                #filename is OK as-is
                pass
            else:
                #file = File(file.toString() + ".txt");  # append .txt if "foo.jpg.txt" is OK
                file = File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".txt") # ALTERNATIVELY: remove the extension (if any) and replace it with ".xml"

        else:
            return
        if self.logLevel > 0: print "savetofile action", file
        my_list = []
        [train, block, direction, length, speed_factor] = [0, 1, 2, 4, 5]
        for row in range(len(self.model.data)):
            train_name = str(self.model.data[row][train])
            block_name = str(self.model.data[row][block])
            train_direction = str(self.model.data[row][direction])
            train_length = str(self.model.data[row][length])
            train_speed_factor = str(self.model.data[row][speed_factor])
            row_list = [train_name, block_name, train_direction,train_length,train_speed_factor]
            if self.logLevel > 0: print "x", row
            my_list.append(row_list)
            if self.logLevel > 0: print "y", row
        if self.logLevel > 0: print "A"
        self.write_list(my_list,file)


    def loadfromfile_action(self, event):
        # load the file
        dir = self.directory()
        j = JFileChooser(dir);
        j.setAcceptAllFileFilterUsed(False)
        filter = FileNameExtensionFilter("text files txt", ["txt"])
        j.setDialogTitle("Select a .txt file");
        j.addChoosableFileFilter(filter);
        ret = j.showOpenDialog(None);
        if (ret == JFileChooser.APPROVE_OPTION) :
            file = j.getSelectedFile()
            if self.logLevel > 0: print "about to read list", file
            my_list = self.read_list(file)
            if self.logLevel > 0: print "my_list", my_list
            for row in reversed(range(len(self.model.data))):
                self.model.data.pop(row)
            i = 0
            [train, block, direction, length, speed_factor] = [0, 1, 2, 4, 5]
            for row in my_list:
                [train_val, block_val, direction_val, length_val, speed_factor_val] = row
                self.model.add_row()
                self.model.data[i][train] = train_val.replace('"','')
                self.model.data[i][block] = block_val.replace('"','')
                self.model.data[i][direction] = direction_val.replace('"','')
                self.model.data[i][length] = length_val.replace('"','')
                self.model.data[i][speed_factor] = speed_factor_val.replace('"','')
                i += 1
            self.completeTablePanel()

            msg = "Deleting invalid rows"
            result = OptionDialog().displayMessage(msg)
            if result == JOptionPane.NO_OPTION:
                return

            # check the loaded contents
            # 1) check that the trains are valid
            # 2) ckeck that the blocks are occupied by valid trains
            # if either of the above are not valic we blank the entries
            # 3) Tidy

            # check the trains are valid
            b = False
            trains_to_put_in_dropdown = [t for t in self.non_allocated_trains]
            for row in reversed(range(len(self.model.data))):
                #if len(self.model.data) >1:
                    # print "row", row
                if self.model.data[row][train] not in trains_to_put_in_dropdown:
                    self.model.data.pop(row)

            not_allocated_blocks = self.super.occupied_blocks_not_allocated()
            for row in reversed(range(len(self.model.data))):
                # if len(self.model.data) >1:
                    # print "row", row
                if self.model.data[row][block] not in not_allocated_blocks:
                    self.model.data.pop(row)
            self.completeTablePanel()

    def cancel_action(self, event):
        title = ""
        msg = "Do you wish to exit without saving?"
        opt1 = "Exit, don't save"
        opt2 = "Exit and Save Results"
        reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
        if reply == opt1:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))
        else:  #opt2
            self.apply_action(None)


    def apply_action(self, event):
        [train, block, direction, length, speed_factor] = [0, 1, 2, 4, 5]
        # print "apply action"
        for row in reversed(range(len(self.model.data))):
            train_name = self.model.data[row][train]
            block_name = self.model.data[row][block]
            train_direction = self.model.data[row][direction]
            train_length = self.model.data[row][length]
            train_speed_factor = self.model.data[row][speed_factor]

            in_siding = False
            result = train_direction
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

            if train_name != "" and train_name != None and block_name != "" and block_name != None:
                self.super.add_to_train_list_and_set_new_train_location0(train_name, block_name,
                                                train_direction, train_length, train_speed_factor)
                self.super.set_blockcontents(block_name, train_name)
                [engine,current_length] = self.super.get_train_length(train_name)
                engine.setLength(train_length)
                [engine, current_speed_factor] = self.super.get_train_speed_factor(train_name)
                current_speed_factor_str = "speed factor " + current_speed_factor
                engine.setComment(current_speed_factor_str)
                self.model.data.pop(row)
        self.completeTablePanel()
        if self.model.getRowCount() == 0:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))
    def set_train_selections(self, combobox):
        pass
    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "setup_trains"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator
    def write_list(self, a_list, file):
        # store list in binary file so 'wb' mode
        #file = self.directory() + "blockDirections.txt"
        if self.logLevel > 0: print "block_info" , a_list
        if self.logLevel > 0: print "file" , file
        file = str(file)
        with open(file, 'wb') as fp:
            pass
        if self.logLevel > 0: print "V"
        with open(file, 'wb') as fp:
            if self.logLevel > 0: print "B"
            for items in a_list:
                if self.logLevel > 0: print "C", items
                i = 0
                for item in items:
                    if self.logLevel > 0: print "item", item
                    fp.write('"%s"' %item)
                    if i != 4: fp.write(",")
                    i+=1
                fp.write('\n')
                #fp.write('\n'.join(item))
                #fp.write(items)

    # Read list to memory
    def read_list(self, file):
        file = str(file)
        if self.logLevel > 0: print "read list", file
        # for reading also binary mode is important
        #file = self.directory() + "blockDirections.txt"
        n_list = []
        # try:
        with open(file, 'rb') as fp:
            for line in fp:
                if self.logLevel > 0: print "line" , line
                x = line[:-1]
                if self.logLevel > 0: print x
                y = x.split(",")
                #y = [item.replace('"','') for item in y]
                if self.logLevel > 0: print "y" , y
                n_list.append(y)

        return n_list
        # except:
        #     return ["",""]

class MyModelListener(TableModelListener):

    def __init__(self, class_createandshowGUI, class_NewTrainMaster):
        self.class_createandshowGUI = class_createandshowGUI
        self.class_NewTrainMaster = class_NewTrainMaster
        self.super = super
        self.cancel = False
    def tableChanged(self, e) :
        global train_direction_gbl
        global trains_allocated
        row = e.getFirstRow()
        column = e.getColumn()
        model = e.getSource()
        columnName = model.getColumnName(column)
        data = model.getValueAt(row, column)
        class_createandshowGUI = self.class_createandshowGUI
        class_NewTrainMaster = self.class_NewTrainMaster
        tablemodel = class_createandshowGUI.model
        if column == 0:     #trains
            class_createandshowGUI.combobox0.removeAllItems()
            #the non_allocated trains are stored in self.non_allocated_trains
            # each time a cell is edited we regenerate the list if trains in the drop down
            # we set to the non_allocated_trains less the ones marked ro be allocated in the table

            # for r in range(class_createandshowGUI.table.getRowCount()):
                # print "r",r,"column",column
                # print "r", r, "(model.getValueAt(r, column)", (model.getValueAt(r, column))
            #trains_in_table = [train for train in (model.getValueAt(r, column) for r in range(class_createandshowGUI.table.getRowCount()))
            trains_in_table = [train for train in (model.getValueAt(r, column) for r in range(class_createandshowGUI.table.getRowCount())) if train in class_createandshowGUI.all_trains]
            # print "trains in table", trains_in_table
            # starting with non_allocated_trains remove the ones in my_train_list
            #trains_to_put_in_dropdown = [t for t in class_createandshowGUI.non_allocated_trains if t not in trains_in_table]
            trains_to_put_in_dropdown = [t for t in class_createandshowGUI.non_allocated_trains]
            # print "trains_to_put_in_dropdown", trains_to_put_in_dropdown
            class_createandshowGUI.combobox0.removeAllItems()
            #put the remaining trains in the combo dropdown
            class_createandshowGUI.combobox0.addItem("")
            [class_createandshowGUI.combobox0.addItem(train) for train in trains_to_put_in_dropdown]
            class_createandshowGUI.trainColumn.setCellEditor(DefaultCellEditor(class_createandshowGUI.combobox0));

            # populate the length of the engine
            train_name = model.getValueAt(row, 0)
            if train_name != "":
                [engine, train_length] = class_NewTrainMaster.get_train_length(train_name)
                model.setValueAt(train_length, row,4)

            #populate the speed factor of the engine
            train_name = model.getValueAt(row, 0)
            if train_name != "":
                [engine, train_speed_factor] = class_NewTrainMaster.get_train_speed_factor(train_name)
                model.setValueAt(train_speed_factor, row,5)
            # print "%%%%%%%%%%%%%%%%end col1 %%%%%%%%%%%%%%%%%%%%%%%%"
        elif column == 1:       # sections
            class_createandshowGUI.combobox1.removeAllItems()
            # print "%%%%%%%%%%%%%%%%start col2 %%%%%%%%%%%%%%%%%%%%%%%%"
            # print "class_createandshowGUI.all_sections", class_createandshowGUI.all_sections
            # print "range class_createandshowGUI.table.getRowCount()", range(class_createandshowGUI.table.getRowCount())
            for r in range(class_createandshowGUI.table.getRowCount()):
                # print "r",r,"column",column
                # print "r", r, "(model.getValueAt(r, column)", (model.getValueAt(r, column))
                pass
            all_sections = [str(block.getUserName()) for block in class_createandshowGUI.all_sections]
            all_blocks = [str(block.getUserName()) for block in class_createandshowGUI.all_blocks]
            # print "all_sections", all_sections
            trains_in_table = \
                [train for train in (model.getValueAt(r, column) for r in range(class_createandshowGUI.table.getRowCount()))
                 if train in class_createandshowGUI.all_trains]
            X =  [str(model.getValueAt(r, column)) for r in range(class_createandshowGUI.table.getRowCount())]
            # print "X", X
            blocks_in_table = [block for block in X if block in all_blocks]
            # print "sections in table", blocks_in_table
            # starting with non_allocated_trains remove the ones in my_train_list
            # print "sections to choose", class_createandshowGUI.sections_to_choose
            # print "trains_in_table",trains_in_table
            # print "sections True", class_createandshowGUI.train_blocks(trains_in_table, True)
            # print "sections False", class_createandshowGUI.train_blocks(trains_in_table, False)
            allocated_blocks = class_createandshowGUI.super.occupied_blocks_allocated()
            not_allocated_blocks = class_createandshowGUI.super.occupied_blocks_not_allocated()
            #blocks_to_put_in_dropdown = [s for s in not_allocated_blocks if s not in blocks_in_table]
            blocks_to_put_in_dropdown = [s for s in not_allocated_blocks]
            # print("blocks_to_put_in_dropdown", blocks_to_put_in_dropdown)
            #put the remaining trains in the combo dropdown
            class_createandshowGUI.combobox1.removeAllItems()
            class_createandshowGUI.combobox1.addItem("")
            [class_createandshowGUI.combobox1.addItem(section) for section in blocks_to_put_in_dropdown]

            # [class_createandshowGUI.combobox1.addItem(section) for section in blocks_to_put_in_dropdown]
            class_createandshowGUI.sectionColumn.setCellEditor(DefaultCellEditor(class_createandshowGUI.combobox1));
            # print "%%%%%%%%%%%%%%%%end col2 %%%%%%%%%%%%%%%%%%%%%%%%"
        elif column == 3:       # show the direction on the layout to enable the facing direction to be chosen
            # print "cancel on entry", self.cancel
            if self.cancel == True:
                self.cancel = False
                # print "set cancel", self.cancel
                return
            station_block_name = model.getValueAt(row, 1)
            # print "station_block_name", station_block_name
            if station_block_name != None and station_block_name != "" and station_block_name != "None Available":
                # print "here"
                [edge, train_direction_gbl, result] = class_createandshowGUI.super.highlight_penultimate_block(station_block_name)
                # print [edge, train_direction_gbl, result]
                self.cancel = True
                model.setValueAt(result, row, 2)      #set the direction box to the result (forwards or reverse)
                model.setValueAt(False, row, 3)       #reset the check box (need the self.cancel code to stop retriggering of the event code)
            else:
                OptionDialog().displayMessage("must set Block first")


class ComboBoxCellRenderer (TableCellRenderer):
    def getTableCellRendererComponent(self, jtable, value, isSelected, hasFocus, row, column):
        combo = JComboBox()
        combo.setSelectedItem(value);
        return combo
#


    # def __init__(self, comboBox) :
    #     for i in range(comboBox.getItemCount()):
    #         self.combo.addItem(comboBox.getItemAt(i))
    #         pass
    #
    # combo = JComboBox()

    def getTableCellRendererComponent(self, jtable, value, isSelected, hasFocus, row, column) :
        panel = self.createPanel(value)
        return panel

    def createPanel(self, s) :
        p = JPanel(BorderLayout())
        p.add(JLabel(s, JLabel.LEFT), BorderLayout.WEST)
        icon = UIManager.getIcon("Table.descendingSortIcon");
        p.add(JLabel(icon, JLabel.RIGHT), BorderLayout.EAST);
        p.setBorder(BorderFactory.createLineBorder(Color.blue));
        return p;


class MyTableModel (DefaultTableModel):

    columnNames = ["Train",
                   "Block",
                   "Set Direction",
                   "Direction Facing",
                   "Length (scale metres)",
                   "Speed Factor"]

    def __init__(self):
        l1 = ["", "", "click ->", False, 10, 100]
        self.data = [l1]

    def remove_not_set_row(self):
        b = False
        for row in reversed(range(len(self.data))):
            # print "row", row
            if self.data[row][0] == "":
                self.data.pop(row)

    def add_row(self):
        # print "addidn row"
        # if row < len(self.data):
        # print "add"
        self.data.append(["", "", "click ->", False, 10, 100])
        # print self.data
        # print "added"

    def populate(self, blocks_to_put_in_dropdown):
        # append all blocks to put in dropdown
        for block in blocks_to_put_in_dropdown:
            self.data.append(["", block, "click ->", False, 10, 100])
        # delete rows with no blocks
        for row in reversed(range(len(self.data))):
            if self.data[row][1] == None or self.data[row][1] == "":
                if len(self.data)>1:
                    self.data.pop(row)

    def getColumnCount(self) :
        return len(self.columnNames)


    def getRowCount(self) :
        return len(self.data)


    def getColumnName(self, col) :
        return self.columnNames[col]


    def getValueAt(self, row, col) :
        return self.data[row][col]

    def getColumnClass(self, c) :
        if c <= 1:
            return java.lang.Boolean.getClass(JComboBox)
        return java.lang.Boolean.getClass(self.getValueAt(0,c))


    #only include if table editable
    def isCellEditable(self, row, col) :
        # Note that the data/cell address is constant,
        # no matter where the cell appears onscreen.
        if col != 2:
            return True
        else:
            return False

    # only include if data can change.
    def setValueAt(self, value, row, col) :
        self.data[row][col] = value
        self.fireTableCellUpdated(row, col)


