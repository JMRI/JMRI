###############################################################################
#
# class OptionDialog
# Some Swing dialogs
#
# class NewTrainMaster *
# Sets up a train in a section
#
# class StopMaster *
# Turns the dispatch system off
#
# class OffActionMaster *
# allows actions when buttons are turned off
# a) toggles the Setupdispatch SetupRoute buttons
#
# class ResetButtonMaster *
# if a button is turned on, this class turns off all the others.
# allows only one station button to be active at a time
#
# class MoveTrain
# Calls dispatcher to move train from one station to another
# given engine and start and end positions
#
# class DispatchMaster *
# monitors the station buttons, and dependent on the mode one is in
# Setup dispatch, setup route, run route
# calls the appropriate action
#
# class RunDispatchMaster
# starts the classes marked with * above in threads so they can do their work
# also starts class scheduler and class simulation which are in different files
#
# class MonitorTrack
# looks at the allocated trains and ensurs that if the blocks are occupied
# the name of the engine is displayed
#
###############################################################################
import java
import jmri
import re
from javax.swing import JOptionPane
import os
import imp
import copy
import org

from javax.swing import JOptionPane, JFrame, JLabel, JButton, JTextField, JFileChooser, JMenu, JMenuItem, JMenuBar,JComboBox,JDialog,JList

import sys

# include the graphcs library
my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
sys.path.append(my_path_to_jars) # add the jar to your path
from org.jgrapht.alg import DijkstraShortestPath
from org.jgrapht.graph import DefaultWeightedEdge
from org.jgrapht.graph import DirectedWeightedMultigraph

#############################################################################################
#
# Set some global variables
#

logLevel = 0          # for debugging
trains = {}           # dictionary of trains shared over classes
instanceList=[]       # instance list of threads shared over classes
g = None              # graph shared over classes

time_to_stop_in_station = 10000   # time to stop in station in stopping mode(msec)

stopping_sensor_choice = "not_set" # has value

#############################################################################################
# the file was split up to avoid errors
# so now include the split files

FileResetButtonMaster = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/ResetButtonMaster.py')
execfile(FileResetButtonMaster)

# FileMoveTrain has to go before CreateScheduler
FileMoveTrain = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/MoveTrain.py')
execfile(FileMoveTrain)

CreateScheduler = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Scheduler.py')
execfile(CreateScheduler)

CreateSimulation = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Simulation.py')
execfile(CreateSimulation)

#############################################################################################

#class OptionDialog( java.lang.Runnable ) :
class OptionDialog( jmri.jmrit.automat.AbstractAutomaton ) :
    CLOSED_OPTION = False
    logLevel = 0

    def List(self, title, list_items):
        list = JList(list_items)
        list.setSelectedIndex(0)
        i = []
        self.CLOSED_OPTION = False
        options = ["OK"]
        while len(i) == 0:
            s = JOptionPane.showOptionDialog(None,
            list,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            None,
            options,
            options[0])
            if s == JOptionPane.CLOSED_OPTION:
                self.CLOSED_OPTION = True
                if self.logLevel > 1 : print "closed Option"
                return
            i = list.getSelectedIndices()
        index = i[0]
        return list_items[index]


    #list and option buttons
    def ListOptions(self, list_items, title, options):
        list = JList(list_items)
        list.setSelectedIndex(0)
        self.CLOSED_OPTION = False
        s = JOptionPane.showOptionDialog(None,
            list,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            None,
            options,
            options[1])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        index = list.getSelectedIndices()[0]
        return [list_items[index], options[s]]

        # call using
        # list_items = ["list1","list2"]
        # options = ["opt1", "opt2", "opt3"]
        # title = "title"
        # result = OptionDialog().ListOptions(list_items, title, options)
        # list= result[0]
        # option = result[1]
        # print "option= " ,option, " list = ",list

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

    def displayMessage(self, msg, title = ""):
        self.CLOSED_OPTION = False
        s = JOptionPane.showOptionDialog(None,
                msg,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                None,
                ["OK"],
                None)
        #JOptionPane.showMessageDialog(None, msg, 'Message', JOptionPane.WARNING_MESSAGE)
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        return s

    def customQuestionMessage(self, msg, title, opt1, opt2, opt3):
        self.CLOSED_OPTION = False
        options = [opt1, opt2, opt3]
        s = JOptionPane.showOptionDialog(None,
        msg,
        title,
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        None,
        options,
        options[2])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        return s

    def customQuestionMessage3str(self, msg, title, opt1, opt2, opt3):
        self.CLOSED_OPTION = False
        options = [opt1, opt2, opt3]
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        if s == JOptionPane.YES_OPTION:
            s1 = opt1
        elif s == JOptionPane.NO_OPTION:
            s1 = opt2
        else:
            s1 = opt3
        return s1

    def customQuestionMessage2(self, msg, title, opt1, opt2):
        self.CLOSED_OPTION = False
        options = [opt1, opt2]
        s = JOptionPane.showOptionDialog(None,
        msg,
        title,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        None,
        options,
        options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        return s

    def customQuestionMessage2str(self, msg, title, opt1, opt2):
        self.CLOSED_OPTION = False
        options = [opt1, opt2]
        s = JOptionPane.showOptionDialog(None,
        msg,
        title,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        None,
        options,
        options[1])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        if s == JOptionPane.YES_OPTION:
            s1 = opt1
        else:
            s1 = opt2
        return s1

    def customMessage(self, msg, title, opt1):
        self.CLOSED_OPTION = False
        options = [opt1]
        s = JOptionPane.showOptionDialog(None,
        msg,
        title,
        JOptionPane.YES_OPTION,
        JOptionPane.PLAIN_MESSAGE,
        None,
        options,
        options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        return s

    def input(self,msg, title, default_value):
        options = None
        x = JOptionPane.showInputDialog( None, msg,title, JOptionPane.QUESTION_MESSAGE, None, options, default_value);
        if x == None:
            self.CLOSED_OPTION = True
            return
        return x

class modifiableJComboBox:

    def __init__(self,list, msg):
        #list = self.get_all_roster_entries_with_speed_profile()
        jcb = JComboBox(list)
        jcb.setEditable(True)
        JOptionPane.showMessageDialog( None, jcb, msg, JOptionPane.QUESTION_MESSAGE)
        self.ans = str(jcb.getSelectedItem())

    def return_val(self):
        return self.ans



class StopMaster(jmri.jmrit.automat.AbstractAutomaton):

    def init(self):
        self.logLevel = 0
        if self.logLevel > 0: print 'Create Stop Thread'

    def setup(self):
        self.stop_master_sensor = sensors.getSensor("stopMasterSensor")
        if self.stop_master_sensor is None:
            return False
        self.stop_master_sensor.setKnownState(INACTIVE)

        self.start_scheduler = sensors.getSensor("startSchedulerSensor")
        self.start_scheduler.setKnownState(INACTIVE)
        return True

    def handle(self):
        global timebase
        self.waitSensorActive(self.stop_master_sensor)
        #stop all threads
        if self.logLevel > 0: print "instancelist", instanceList
        msg = "Delete all active Transits?\n"+"\nCaution this may disrupt running trains\n"
        title = "Transits"
        opt1 = "stop route threads"
        opt2 = "stop all threads"
        opt3 = "stop threads and delete transits"
        requested_action = OptionDialog().customQuestionMessage3str(msg, title, opt1, opt2, opt3)
        if requested_action == "stop route threads":
            self.stop_route_threads()
            self.stop_sensor = sensors.getSensor("stopMasterSensor")
            self.stop_sensor.setKnownState(INACTIVE)
            return True
        elif requested_action == "stop all threads":
            self.remove_timebase_listener()
            self.stop_all_threads()
        else:  #stop all threads and delete transits
            self.remove_timebase_listener()
            self.delete_active_transits()
            self.stop_all_threads()
        if self.logLevel > 0: print "finished"

    def remove_timebase_listener(self):
        self.new_train_sensor = sensors.getSensor("startDispatcherSensor")
        self.new_train_sensor.setKnownState(INACTIVE)
        try:
            #stop the scheduler timebase listener
            if self.logLevel > 0: print "removing listener"
            timebase.removeMinuteChangeListener(TimeListener())
            return False
        except NameError:
            if self.logLevel > 0: print "Name error"
            return False
        else:
            return False

    def stop_route_threads(self):
        #remove the train from the transit
        msg = "Delete all active Transits?\n"+"\nCaution this may disrupt running trains\n"
        title = "Transits"
        opt1 = "just remove the route threads (stops trains at end of current transit)"
        opt2 = "delete transits as well (stops trains immediately)"
        requested_delete_transits = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
        self.remove_train_from_transit
        #stop all threads
        for thread in instanceList:
            thread_name = "" + thread.getName()
            if thread_name.startswith("running_route_"):
                #determine the train nme
                train_name = self.determine_train_name(thread_name,thread)
                #remove the train from the transit
                if requested_delete_transits:

                    #remove the train from the list of trains
                    self.remove_train_name(train_name)
                if thread is not None:
                    if thread.isRunning():
                        if self.logLevel > 0: print 'Stop "{}" thread'.format(thread.getName())
                        thread.stop()
                    else:
                        #need this for scheduler in wait state
                        thread.stop()

    def determine_train_name(self,thread_name, thread):
        route = thread
        train_name = route.train_name
        return train_name

    def remove_train_from_transit(self, train_name):
        if self.logLevel > 0: print "train_name to remove from trainsit", train_name
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        activeTrainsList = DF.getActiveTrainsList()
        for i in range(0, activeTrainsList.size()) :
            activeTrain = activeTrainsList.get(i)
            if train_name == activeTrain.getTrainName():
                DF.terminateActiveTrain(activeTrain)

    def remove_train_name(self, train_name):
        global trains_allocated
        global trains_dispatched
        if self.logLevel > 0: print "train to remove", train_name
        # for train in trains_allocated:
        #     if self.logLevel > 0: print "train in trains_allocated", train, ": trains_allocated", trains_allocated
        #     if train == train_name:
        #         trains_allocated.remove(train)
        for train in trains_dispatched:
            #print "train in trains_alloceted", train, ": trains_allocated", trains_allocated
            if train == train_name:
                trains_dispatched.remove(train)

    def stop_all_threads(self):
        #stop all threads
        for thread in instanceList:
            if thread is not None:
                if thread.isRunning():
                    if self.logLevel > 0: print 'Stop "{}" thread'.format(thread.getName())
                    thread.stop()
                else:
                    #need this for scheduler in wait state
                    thread.stop()

    def remove_listener(self):
        try:
            #stop the scheduler timebase listener
            if self.logLevel > 0: print "removing listener"
            timebase.removeMinuteChangeListener(TimeListener())
            return False
        except NameError:
            if self.logLevel > 0: print "Name error"
            return False
        else:
            return False

    def delete_active_transits(self):

        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        activeTrainsList = DF.getActiveTrainsList()
        for i in range(0, activeTrainsList.size()) :
            activeTrain = activeTrainsList.get(i)
            DF.terminateActiveTrain(activeTrain)

# End of class StopMaster

class OffActionMaster(jmri.jmrit.automat.AbstractAutomaton):

    button_sensors_to_watch = []
    def __init__(self):
        self.logLevel = 0

    def init(self):
        if self.logLevel > 0: print 'Create OffActionMaster Thread'
        self.get_run_buttons()
        self.get_route_dispatch_buttons()

        self.button_sensors_to_watch = self.run_stop_sensors
        if self.logLevel > 0: print "button to watch" , str(self.button_sensors_to_watch)
        #wait for one to go inactive
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        self.waitSensorState(button_sensors_to_watch_JavaList, INACTIVE)

        if self.logLevel > 0: print "button went inactive"
        sensor_that_went_inactive = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == INACTIVE][0]
        if self.logLevel > 0: print "sensor_that_went_inactive" , sensor_that_went_inactive
        start_sensor = sensors.getSensor("startDispatcherSensor")
        stop_sensor =  sensors.getSensor("stopMasterSensor")
        if self.logLevel > 0: print "start_sensor" , start_sensor
        if self.logLevel > 0: print "stop_sensor" , stop_sensor
        if sensor_that_went_inactive in self.run_stop_sensors:
            if self.logLevel > 0: print "run stop sensor went inactive"

            if sensor_that_went_inactive == start_sensor:
                self.sensor_to_look_for = stop_sensor
                if self.logLevel > 0: print "start sensor went inactive"
                if self.logLevel > 0: print "setting stop sensor active"
                stop_sensor.setKnownState(ACTIVE)
                # self.waitMsec(5000)
                # if self.logLevel > 0: print "setting start sensor active"
                # start_sensor.setKnownState(ACTICE)
            elif sensor_that_went_inactive == stop_sensor:
                self.sensor_to_look_for = start_sensor
                if self.logLevel > 0: print "stop sensor went inactive"
                if self.logLevel > 0: print "setting start sensor active"
                start_sensor.setKnownState(ACTIVE)
                # self.waitMsec(5000)
                # start_sensor.setKnownState(ACTICE)
                pass#

        if self.logLevel > 0: print "finished OffActionMaster setup"

    def setup(self):
        if self.logLevel > 0: print "starting OffActionMaster setup"
        #get dictionary of buttons self.button_dict
        #self.get_route_dispatch_buttons()

        return True

    def handle(self):
        if self.logLevel > 0: print "started handle"
        #for pairs of buttons, if one goes off the other is set on
        #self.button_sensors_to_watch = self.run_sensor_to_look_for
        if self.logLevel > 0: print "button to watch" , str(self.button_sensors_to_watch)
        #wait for one to go active
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        self.waitSensorState(button_sensors_to_watch_JavaList, INACTIVE)
        #determine which one changed
        if self.logLevel > 0: print "sensor went inactive"
        sensor_that_went_inactive = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == INACTIVE][0]

        if sensor_that_went_inactive in self.run_stop_sensors:
            if self.logLevel > 0: print "run stop sensor went inactive"
            start_sensor = sensors.getSensor("startDispatcherSensor")
            stop_sensor =  sensors.getSensor("stopMasterSensor")
            if sensor_that_went_inactive == start_sensor:
                self.sensor_to_look_for = stop_sensor
                if self.logLevel > 0: print "start sensor went inactive"
                if self.logLevel > 0: print "setting stop sensor active"
                stop_sensor.setKnownState(ACTIVE)
                # self.waitMsec(5000)
                # if self.logLevel > 0: print "setting start sensor active"
                # start_sensor.setKnownState(ACTICE)
            elif sensor_that_went_inactive == stop_sensor:
                self.sensor_to_look_for = start_sensor
                if self.logLevel > 0: print "stop sensor went inactive"
                if self.logLevel > 0: print "setting start sensor active"
                start_sensor.setKnownState(ACTIVE)

        if self.logLevel > 0: print "end handle"
        #self.waitMsec(20000)
        return False

    def get_route_dispatch_buttons(self):
        self.setuproute_or_rundispatch_or_setstoppingdistance_sensors = \
            [sensors.getSensor(sensorName) for sensorName in ["setDispatchSensor", "setRouteSensor", "setStoppingDistanceSensor", "setStationWaitTime"]]
        #self.route_dispatch_states = [self.check_sensor_state(rd_sensor) for rd_sensor in self.setup_route_or_run_dispatch_sensors]
        pass

    def get_run_buttons(self):
        self.run_stop_sensors = [sensors.getSensor(sensorName) for sensorName in ["startDispatcherSensor"]]




DF = None

class DispatchMaster(jmri.jmrit.automat.AbstractAutomaton):

    button_sensors_to_watch = []
    button_dict = {}

    def __init__(self):
        self.logLevel = 0
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
        global DF

        if self.logLevel > 0: print "starting DispatchMaster setup"

        #get dictionary of buttons self.button_dict
        self.get_buttons()
        #set all move_to buttons inactive
        for sensor in self.button_sensors:
            sensor.setKnownState(INACTIVE)
        #store the values in a clone
        #self.store_button_states()
        # #at moment there are no trains so:
        self.button_sensors_to_watch = copy.copy(self.button_sensors)

        if self.logLevel > 0: print "self.button_sensors_to_watch_init", [str(sensor.getUserName()) for sensor in self.button_sensors_to_watch]

        self.sensor_active = None
        if self.logLevel > 0: print "finished DispatchMaster setup"

        #DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        self.od = OptionDialog()
        return True

    def  handle(self):

        global trains_allocated
        global trains_dispatched

        #only one button is active. We will keep it that way

        if self.logLevel > 0: print "**********************"
        if self.logLevel > 0: print "handle DispatchMaster1"
        #if self.logLevel > 0: print "buttons to watch",[str(sensor.getUserName()) for sensor in self.button_sensors_to_watch]
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
        if self.logLevel > 0: print "button_sensor_name",button_sensor_name
        if self.logLevel > 0: print "button_station_name",button_station_name

        #set up route, dispatch train or run route
        if self.logLevel > 0: print "!!!!!!!!!!!!!!!!!!!!!!!!"
        setup_dispatch_sensor = sensors.getSensor("setDispatchSensor")
        set_route_sensor = sensors.getSensor("setRouteSensor")
        run_route_sensor = sensors.getSensor("runRouteSensor")
        modify_stopping_length_sensor = sensors.getSensor("setStoppingDistanceSensor")
        modify_station_wait_time_sensor = sensors.getSensor("setStationWaitTimeSensor")
        if self.logLevel > 0: print "set_route_sensor.getKnownState()",set_route_sensor.getKnownState(),
        self.reset_buttons(button_sensors_to_watch_JavaList)
        if set_route_sensor.getKnownState() == ACTIVE:
            if self.logLevel > 0: print ("set_route")
            test = self.set_route(sensor_changed, button_sensor_name, button_station_name)
            if self.logLevel > 0: print "test = " , test
            # if test == False:
                # self.button_sensors_to_watch = copy.copy(self.button_sensors)
            sensor_changed.setKnownState(INACTIVE)
        elif setup_dispatch_sensor.getKnownState() == ACTIVE:
            if self.logLevel > 0: print ("dispatch_train")
            self.dispatch_train(sensor_changed, button_sensor_name, button_station_name)
            #self.button_sensors_to_watch = copy.copy(self.button_sensors)
            sensor_changed.setKnownState(INACTIVE)
        elif modify_stopping_length_sensor.getKnownState() == ACTIVE:
            if self.modify_individual_stopping_length(sensor_changed, button_sensor_name, button_station_name):
                sensor_changed.setKnownState(INACTIVE)
            else:
                #cancelled: reset all buttons so we check all of them
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
        elif modify_station_wait_time_sensor.getKnownState() == ACTIVE:
            if self.modify_individual_station_wait_time(sensor_changed, button_sensor_name, button_station_name):
                sensor_changed.setKnownState(INACTIVE)
            else:
                #cancelled: reset all buttons so we check all of them
                self.button_sensors_to_watch = copy.copy(self.button_sensors)

        else:
            title = "station button error"
            msg = "select one of 'Run Dispatch', 'Setup Route', 'Set Stopping Length', 'Set Station Wait Time' \nfor the station buttons to have effect"
            self.od.displayMessage(msg,title)
            pass

        if self.logLevel > 0: print "end handle"
        self.waitMsec(1000)
        return True

    def reset_buttons(self, button_sensors_to_watch_JavaList):
        #print "resetting buttons", button_sensors_to_watch_JavaList
        for button in button_sensors_to_watch_JavaList:
            button.setKnownState(INACTIVE)


    def set_route(self, sensor_changed, button_sensor_name, button_station_name):

        msg = "selected station " + button_station_name + ". \nHave you more stations on route?"
        title = "Continue selecting stations"

        opt1 = "Select another station"
        opt2 = "Cancel Route"

        s = self.od.customQuestionMessage2(msg,title,opt1,opt2)

        if self.od.CLOSED_OPTION == True:
            return False
        if s == JOptionPane.NO_OPTION:
            return False
        if self.logLevel > 0: print "button_station_name", button_station_name
        if self.logLevel > 0: print "button_sensor_name", button_sensor_name
        #set name of route
        if self.logLevel > 0: print ("in dispatch train")
        # msg = "Name of Route"
        # route_name = JOptionPane.showInputDialog(None,msg)
        #create route
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route = RouteManager.newRoute("temp_name")

        LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
        #if self.logLevel > 0: print "button_station_name", button_station_name
        location = LocationManager.newLocation(button_station_name)
        first_station = button_station_name
        last_station = first_station
        route.addLocation(location)
        self.button_sensors_to_watch = copy.copy(self.button_sensors)
        self.button_sensors_to_watch.remove(sensor_changed)
        complete = False
        while complete == False:
            if self.logLevel > 0: print ("In loop")
            button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
            self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
            sensor_changed = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
            button_sensor_name = sensor_changed.getUserName()
            button_station_name = self.get_block_name_from_button_sensor_name(button_sensor_name)

            location = LocationManager.newLocation(button_station_name)
            route.addLocation(location)
            last_station = button_station_name

            msg = "selected station " + button_station_name + ". \nHave you more stations on route?"
            title = "Continue selecting stations"

            opt1 = "Select another station"
            opt2 = "Complete Route"
            opt3 = "Cancel Route"

            s = self.od.customQuestionMessage3str(msg,title,opt1,opt2,opt3)
            if s == self.od.CLOSED_OPTION:
                s = opt3  #cancel
            if s == opt2:
                complete = True
            if s == opt3:
                sensor_changed.setKnownState(INACTIVE)
                RouteManager.deregister(route)
                return
            Firstloop = False
            self.get_buttons()
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            self.button_sensors_to_watch.remove(sensor_changed)

        route_name_prefix = first_station + "_to_" + last_station
        route_name = route_name_prefix
        i = 0
        while RouteManager.getRouteByName(route_name) != None:
            i+=1
            route_name = route_name_prefix + "_" + str(i)
        route.setName(route_name)
        msg = "completed route  " + route_name + ". you may see the route by clicking View/Edit Routes."
        opt1 = "Finish"
        opt2 = "View Route"
        reply = self.od.customQuestionMessage2(msg, title, opt1, opt2)
        sensor_changed.setKnownState(INACTIVE)
        if reply == opt2:
            self.show_routes()
        if self.logLevel > 0: print ("terminated dispatch")
        return True

    def show_routes(self):
        a = jmri.jmrit.operations.routes.RoutesTableAction()
        a.actionPerformed(None)

    def modify_individual_stopping_length(self, sensor_changed, button_sensor_name, button_station_name):
        msg = "selected station " + button_station_name + ". \nSelect the next station to modify the stopping length?"
        title = "Select next Station"

        opt1 = "Select next station"
        opt2 = "Cancel stopping length modification"

        s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
        if self.od.CLOSED_OPTION == True:
            return False
        if s == opt2:
            return False
        if self.logLevel > 0: print "button_station_name", button_station_name
        if self.logLevel > 0: print "button_sensor_name", button_sensor_name
        #set name of route
        if self.logLevel > 0: print ("in modify stopping length")

        # #modify stopping length
        # RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        # route = RouteManager.newRoute("temp_name")
        #
        # LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
        # #if self.logLevel > 0: print "button_station_name", button_station_name
        # location = LocationManager.newLocation(button_station_name)
        first_station = button_station_name
        last_station = first_station
        # route.addLocation(location)
        self.button_sensors_to_watch = copy.copy(self.button_sensors)
        self.button_sensors_to_watch.remove(sensor_changed)
        complete = False
        while complete == False:
            if self.logLevel > 0: print ("In loop")
            button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
            self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
            sensor_changed = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
            button_sensor_name = sensor_changed.getUserName()
            button_station_name = self.get_block_name_from_button_sensor_name(button_sensor_name)

            # location = LocationManager.newLocation(button_station_name)
            # route.addLocation(location)
            last_station = button_station_name

            #get the transit corresponding to first_station last_station

            for e in g.g_express.edgeSet():
                from_station_name = g.g_stopping.getEdgeSource(e)
                to_station_name = g.g_stopping.getEdgeTarget(e)
                if from_station_name == first_station and to_station_name == last_station:
                    found_edge = e
                    break
            filename_fwd = self.get_filename(found_edge, "fwd")
            filename_rvs = self.get_filename(found_edge, "rvs")

            msg = "selected station " + button_station_name + ". \nDo you wish to modify the stopping length fraction ?"
            title = "Continue selecting stations"

            opt1 = "Cancel Stopping length modification"
            opt2 = "Modify length"

            s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
            if self.od.CLOSED_OPTION == True :
                sensor_changed.setKnownState(INACTIVE)
                sensors.getSensor("setStoppingDistanceSensor").setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
                return False
            elif s == opt1:
                sensor_changed.setKnownState(INACTIVE)
                sensors.getSensor("setStoppingDistanceSensor").setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
                return False
            if s == opt2:
                sensor_changed.setKnownState(INACTIVE)
                complete = True
            Firstloop = False
            self.get_buttons()
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            self.button_sensors_to_watch.remove(sensor_changed)
            sensor_changed.setKnownState(INACTIVE)

        #go to trhis bit when complete == True

        #get traininfo and stopping fraction
        filename_fwd = self.get_filename(found_edge, "fwd")
        trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
        stopping_fraction = trainInfo_fwd.getStopBySpeedProfileAdjust()



        #modify stopping fraction in traininfo
        last_section = self.last_section_of_transit(trainInfo_fwd)
        length_of_last_section = float(self.length_of_last_section(last_section))/10.0 #cm
        # print "last_section", last_section.getUserName()
        # print "length_of_last_section", length_of_last_section , " in cm"

        previous_stopping_position = round((1.0-round(stopping_fraction,2))*length_of_last_section,2)

        default_value = round(previous_stopping_position,1)
        title = "Stop train before end of section"
        msg = "Transit name: " + str(filename_fwd) + \
                ".\nStopping fraction is: " + str(round(stopping_fraction,2)) + \
                "\nLength of last section: " + str(round(length_of_last_section, 1)) + " cm " + \
                "   (" + str(round(length_of_last_section/2.54, 1)) + " inches)" +\
                "\nPrevious stopping position: " + str(round(previous_stopping_position,1)) + " cm " + \
                "   (" + str(round(previous_stopping_position/2.54, 1)) + " inches )" +\
                "\n\nEnter new stopping position in cm:"
        new_stopping_position = self.od.input(msg, title, default_value)
        if new_stopping_position == None: return
        # print "new_stopping_position",  new_stopping_position
        new_stopping_fraction = 1.0-(float(new_stopping_position)/float(length_of_last_section))
        # print "new_stopping_fraction", new_stopping_fraction
        msg = "new stopping fraction: "+ str(round(float(new_stopping_fraction),1)) + "\n" + \
              "new stopping position: " + str(round(float(new_stopping_position),1)) + " cm    ( " + \
              str(round(float(new_stopping_position)/2.54,1)) + " inches)"
        self.od.displayMessage(msg)
        trainInfo_fwd.setStopBySpeedProfileAdjust(float(new_stopping_fraction))

        #write the newtraininfo back to file
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(trainInfo_fwd, filename_fwd)

        #do same with reverse
        filename_rvs = self.get_filename(found_edge, "rvs")
        trainInfo_rvs = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_rvs)
        #stopping_fraction = trainInfo_rvs.getStopBySpeedProfileAdjust()
        trainInfo_rvs.setStopBySpeedProfileAdjust(float(new_stopping_fraction))

        #write the newtraininfo back to file
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(trainInfo_rvs, filename_rvs)

        if self.logLevel > 0: print "saved new stopping fraction", float(new_stopping_fraction)

        return True

    def modify_individual_station_wait_time(self, sensor_changed, button_sensor_name, button_station_name):
        msg = "selected station " + button_station_name + ". \nSelect the next station to modify the station wait time?"
        title = "Select next Station"

        opt1 = "Select next station"
        opt2 = "Cancel station wait time modification"

        s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
        if self.od.CLOSED_OPTION == True:
            return False
        if s == opt2:
            return False
        if self.logLevel > 0: print "button_station_name", button_station_name
        if self.logLevel > 0: print "button_sensor_name", button_sensor_name
        #set name of route
        if self.logLevel > 0: print ("in modify station wait time")

        # #modify stopping length
        # RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        # route = RouteManager.newRoute("temp_name")
        #
        # LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
        # #if self.logLevel > 0: print "button_station_name", button_station_name
        # location = LocationManager.newLocation(button_station_name)
        first_station = button_station_name
        last_station = first_station
        # route.addLocation(location)
        self.button_sensors_to_watch = copy.copy(self.button_sensors)
        self.button_sensors_to_watch.remove(sensor_changed)
        complete = False
        while complete == False:
            if self.logLevel > 0: print ("In loop")
            button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
            self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
            sensor_changed = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
            button_sensor_name = sensor_changed.getUserName()
            button_station_name = self.get_block_name_from_button_sensor_name(button_sensor_name)

            # location = LocationManager.newLocation(button_station_name)
            # route.addLocation(location)
            last_station = button_station_name

            #get the transit corresponding to first_station last_station

            for e in g.g_express.edgeSet():
                from_station_name = g.g_stopping.getEdgeSource(e)
                to_station_name = g.g_stopping.getEdgeTarget(e)
                if from_station_name == first_station and to_station_name == last_station:
                    found_edge = e
                    break
            filename_fwd = self.get_filename(found_edge, "fwd")
            filename_rvs = self.get_filename(found_edge, "rvs")

            msg = "selected station " + button_station_name + ". \nDo you wish to modify the station wait time ?"
            title = "Continue selecting stations"

            opt1 = "Cancel Station Wait Time modification"
            opt2 = "Modify station wait time"

            s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
            if self.od.CLOSED_OPTION == True :
                sensor_changed.setKnownState(INACTIVE)
                sensors.getSensor("setStationWaitTimeSensor").setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
                return False
            elif s == opt1:
                sensor_changed.setKnownState(INACTIVE)
                sensors.getSensor("setStationWaitTimeSensor").setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
                return False
            if s == opt2:
                sensor_changed.setKnownState(INACTIVE)
                complete = True
            Firstloop = False
            self.get_buttons()
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            self.button_sensors_to_watch.remove(sensor_changed)
            sensor_changed.setKnownState(INACTIVE)

        #go to trhis bit when complete == True

        #get traininfo and stopping fraction
        filename_fwd = self.get_filename(found_edge, "fwd")
        trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
        station_wait_time = trainInfo_fwd.getWaitTime()

        default_value = round(station_wait_time,1)
        title = "Pause train at beginning of section"
        msg = "Transit name: " + str(filename_fwd) + \
              ".\nStation Wait Time is: " + str(round(station_wait_time,2)) + \
              "\n\nEnter new station wait time in secs:"
        new_station_wait_time = self.od.input(msg, title, default_value)
        if new_station_wait_time == None: return
        msg = "new Station Wait Time: "+ str(round(float(new_station_wait_time),1))
        self.od.displayMessage(msg)
        trainInfo_fwd.setWaitTime(float(new_station_wait_time))

        #write the newtraininfo back to file
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(trainInfo_fwd, filename_fwd)

        #do same with reverse
        filename_rvs = self.get_filename(found_edge, "rvs")
        trainInfo_rvs = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_rvs)
        #stopping_fraction = trainInfo_rvs.getStopBySpeedProfileAdjust()
        trainInfo_rvs.setWaitTime(float(new_station_wait_time))

        #write the newtraininfo back to file
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(trainInfo_rvs, filename_rvs)

        if self.logLevel > 0: print "saved new station wait time", float(new_stopping_fraction)

        return True

    def last_section_of_transit(self, trainInfo_fwd):

        transit_id = trainInfo_fwd.getTransitId()
        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        transit = TransitManager.getTransit(transit_id)
        # print "transit_id", transit_id
        last_seq_no = transit.getMaxSequence()
        section_list = transit.getSectionListBySeq(last_seq_no)
        last_section = section_list[0]    #assume no alternate section
        # print "last_section", last_section
        return last_section

    def length_of_last_section(self, last_section):
        length_in_mm = last_section.getActualLength()
        return length_in_mm

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

    def get_transit(self, transit_name):
        # print e
        # signal_mast_list = e.getItem("signal_mast_list")
        # start_signal_mast = signal_mast_list[0].getUserName()
        # end_signal_mast = signal_mast_list[-1].getUserName()
        # transit_name = "From " + start_signal_mast +  " to " + end_signal_mast
        # if self.logLevel > 1: print "transit_name",transit_name
        # transits = jmri.InstanceManager.getDefault(jmri.TransitManager)
        transit_list = [transit  for transit in transits.getNamedBeanSet() if transit.getUserName() == transit_name]
        if self.logLevel > 1: print "transit_list",transit_list
        if transit_list == []:
            return None
        else:
            transit = transit_list[0]
            if self.logLevel > 1: print "transit", transit.getUserName()
            if self.logLevel > 1: print "transit_username", transit.getUserName()
            if self.logLevel > 1: print "transit exists", transit.getUserName(), transit
            #e.setItem(transit=transit)
            return transit

    def get_existing_transit(self, e):
        # print "edge =" , e
        signal_mast_list = e.getItem("signal_mast_list")
        start_signal_mast = signal_mast_list[0].getUserName()
        end_signal_mast = signal_mast_list[-1].getUserName()
        transit_name = "From " + start_signal_mast +  " to " + end_signal_mast
        if self.logLevel > 1: print "transit_name",transit_name
        transits = jmri.InstanceManager.getDefault(jmri.TransitManager)
        transit_list = [transit  for transit in transits.getNamedBeanSet() if transit.getUserName() == transit_name]
        if self.logLevel > 1: print "transit_list",transit_list

        if transit_list == []:
            return None
        else:
            transit = transit_list[0]
            if self.logLevel > 1: print "transit", transit.getUserName()
            if self.logLevel > 1: print "transit_username", transit.getUserName()
            if self.logLevel > 1: print "transit exists", transit.getUserName(), transit
            e.setItem(transit=transit)
            return transit

    def dispatch_train(self, sensor_changed, button_sensor_name, button_station_name):
        global trains_allocated
        global trains_dispatched

        #find what train we want to move
        #select only from available trains  %%%%todo%%%%%
        all_trains = self.get_all_roster_entries_with_speed_profile()
        #trains to choose from are the allocated - dispatched
        trains_to_choose = copy.copy(trains_allocated)
        if self.logLevel > 0: print "trains_dispatched", trains_dispatched
        if self.logLevel > 0: print "trains_allocated",trains_allocated
        if self.logLevel > 0: print "trains_to_choose",trains_to_choose
        if trains_dispatched != []:
            for train in trains_dispatched:
                if self.logLevel > 0: print "removing" ,train
                trains_to_choose.remove(train)
                if self.logLevel > 0: print "trains_to_choose",trains_to_choose
        if trains_to_choose == []:
            str_trains_dispatched= (' '.join(trains_dispatched))
            msg = "There are no trains available for dispatch\nTrains dispatched are:\n"+str_trains_dispatched+"\nYou have to wait 20 secs after a train has stopped\nbefore dispatching it again"
            title = "Cannot move train"
            opt1 = "continue"
            opt2 = "reset all allocations"
            result = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
            if result == "reset all allocations":
                trains_dispatched = []
            sensor_changed.setKnownState(INACTIVE)
        else:
            #####################################################
            #
            # get train to move & whether express or slow
            # if the block pressed is not the station where the train is#
            #     the station where the train is is occupied
            #####################################################
            if self.logLevel > 0: print "+++++++++++++++++++"
            if self.logLevel > 0: print "checking pressed button"
            if self.logLevel > 0: print "+++++++++++++++++++"
            msg = "select train you want to move"
            list_items = trains_to_choose
            express = self.get_express_flag()
            if express:
                options = ["express", "stopping"]
                default = "express"
            else:
                options = ["stopping", "express"]
                default = "stopping"
            title = "title"
            result = self.od.ListOptions(list_items, msg, options)
            if self.od.CLOSED_OPTION == False:
                list = result[0]
                option = result[1]
                if self.logLevel > 0: print "option= " ,option, " list = ",list
                train_to_move = str(list)
                train_type = str(option)

                if self.logLevel > 0: print "train_to_move",train_to_move
                if self.logLevel > 0: print "train_type" , train_type

                if train_type is None or train_to_move is None:
                    if self.logLevel > 0: print ("train_type is None or train_to_move is None")
                else:
                    for station_block_name in g.station_block_list:

                        # choose the block in station_block_list that has the required train in it
                        # block_value_state must be true
                        block_value_state = self.check_train_in_block(station_block_name, train_to_move)

                        # the block must be occupied
                        # block_occupied_state must be true
                        block_occupied_state = self.check_sensor_state_given_block_name(station_block_name)

                        # do not attempt to move to where you are
                        # button_pressed_in_occupied_station must be false
                        button_pressed_in_occupied_station = (button_station_name == station_block_name)

                        if block_value_state == True and block_occupied_state == True and button_pressed_in_occupied_station == False:
                            if train_type == "express":
                                if self.logLevel > 0: print "moving express"
                                if g == None:
                                    if self.logLevel > 0: print "G IS NONE"

                                move_train = MoveTrain(station_block_name, button_station_name, train_to_move, g.g_express)

                                instanceList.append(move_train)
                                if move_train.setup():
                                    move_train.setName(train_to_move)
                                    move_train.start()
                                if self.logLevel > 0: print "station_block_name",station_block_name
                                if self.logLevel > 0: print "button_station_name", button_station_name
                                if self.logLevel > 0: print "**********************"
                            elif False == False:
                                if self.logLevel > 0: print "moving slow"
                                if g == None:
                                    if self.logLevel > 0: print "G IS NONE"

                                move_train = MoveTrain(station_block_name, button_station_name, train_to_move, g.g_stopping)

                                instanceList.append(move_train)
                                if move_train.setup():
                                    move_train.setName(train_to_move)
                                    if self.logLevel > 0: print "********calling thread move**************"
                                    move_train.start()
                                    if self.logLevel > 0: print "********called thread move***************"
                                self.waitMsec(time_to_stop_in_station)
                                if self.logLevel > 0: print "station_block_name",station_block_name
                                if self.logLevel > 0: print "button_station_name", button_station_name
                                if self.logLevel > 0: print "**********************"
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
        self.waitMsec(1000)

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
        #if sellayoutBlockf.logLevel > 0: print("station block name {}".format(station_block_name))
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
                if self.logLevel > 0: print "roster_entries_with_speed_profile",roster_entries_with_speed_profile
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
        self.button_sensor_states_old = self.button_sensor_states
        if self.logLevel > 0: print "self.button_sensor_states_old",self.button_sensor_states_old
        #self.button_dict_old = dict(self.button_dict)

    def get_button_sensor_given_block_name(self, block_name):
        button_sensor_name = "MoveTo"+block_name.replace(" ","_") +"_stored"
        button_sensor = sensors.getSensor(button_sensor_name)
        return button_sensor

    def show_operations_trains(self):
        a = jmri.jmrit.operations.trains.TrainsTableAction()
        a.actionPerformed(None)

# End of class StopMaster

class MonitorTrackMaster(jmri.jmrit.automat.AbstractAutomaton):

    # 1) ensure that the trains in the train list are being displayed on the panel if the block is occupied
    # 2) ensure that a non allocated train and an allocated train cannot exist in the same edge

    global trains_allocated
    logLevel = 0

    def setup(self):
        self.old_train_edge = {}
        self.moved_under_dispatch = {}
        return True

    def handle(self):

        # trains is a dictionary, with keys of the train_name
        # each value is itself a dictionary with 3 items
        # edge
        # penultimate_block_name
        # direction

        # ensure that the trains in the train list are being displayed on the panel if the block is occupied
        #
        # if the end block of the route is occupied
        #   set th end block memname to the train_name
        if trains != {}:
            if self.logLevel > 0: print "trains", trains
            for train_name in trains:      #defaults tothe key which is the train name
                # if self.logLevel > 0: print "train" , train
                # train_name = str(train[0])
                # train_list = train[1]
                mytrain = trains[train_name]     #mytrain is a dictionary
                if self.logLevel > 0: print "train_name", train_name, #, "train_list", train_list
                #edge = train_list("edge")
                #penultimate_block_name = train_list["penultimate_block_name"]
                edge = str(mytrain["edge"])
                if self.logLevel > 0: print "edge",edge,


                #if the train is at the start of the dispatch or at the end we
                #block_name = str(mytrain["edge"].getSource())   #get the destination edge- the edges are reversed

                #block_occupancy = self.check_sensor_state_given_block_name(block_name)
                #if self.logLevel > 0: print "block_name", block_name,"block_occupancy", block_occupancy,
                active_train = self.get_active_train(train_name)    #get the active train if one exists (if a dispatch is in progress)
                if active_train != None:
                    #[block_list, start_block, end_block] = self.get_occupied_blocks(active_train)
                    if self.logLevel > 0: print "active train is not None", active_train,
                else:
                    if self.logLevel > 0: print "active train is None", active_train,
                if train_name in self.old_train_edge:
                    if self.logLevel > 0: print "old edge", self.old_train_edge[train_name]
                #if the edge for the train has changed we initialise moved_under_dispatch to False
                if train_name in self.old_train_edge:
                    if self.old_train_edge[train_name] == edge:
                        pass
                    else:
                        #initialise the moved_under_dispatch variable
                        self.moved_under_dispatch[train_name] = False
                if train_name not in self.moved_under_dispatch:
                    self.moved_under_dispatch[train_name] = False

                if active_train == None:
                    # train is at the start block or the end block as the dispatch has not started or finished
                    if train_name in self.moved_under_dispatch:
                        if self.moved_under_dispatch[train_name] == False:
                            #block is start block
                            current_block = str(mytrain["edge"].getTarget())
                            block_occupancy = self.check_sensor_state_given_block_name(current_block)
                            if self.logLevel > 0: print "current_block", current_block,"block_occupancy", block_occupancy,
                            self.set_mem_variable(current_block,train_name,block_occupancy)
                        else:
                            #block is end block
                            current_block = str(mytrain["edge"].getSource())
                            block_occupancy = self.check_sensor_state_given_block_name(current_block)
                            if self.logLevel > 0: print "current_block", current_block,"block_occupancy", block_occupancy,
                            self.set_mem_variable(current_block,train_name,block_occupancy)
                else:
                    #trai
                    # n is moving under dispatch
                    #we note this
                    self.moved_under_dispatch[train_name] = True

                if edge != None:
                    self.old_train_edge[train_name] = edge

        self.waitMsec(500)
        return True

    def set_mem_variable(self, block_name, train_name, block_occupancy):
        if block_name != None:
            #print "block_name", block_name
            #print "self.check_train_in_block:",self.check_train_in_block(block_name, train_name) ,"xxxx"
            if block_occupancy == True:
                #check and set the mem_name
                if self.check_train_in_block(block_name, train_name) == False:
                    #print "setting train", train_name, "in block", block_name
                    self.set_train_in_block(block_name, train_name)


    def get_active_train(self, train_name):
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        java_active_trains_list = DF.getActiveTrainsList()
        java_active_trains_Arraylist= java.util.ArrayList(java_active_trains_list)
        #print "java_active_trains_Arraylist",java_active_trains_Arraylist
        #print "train_name", train_name
        for t in java_active_trains_Arraylist:
            #print "activetrainname=",t.getActiveTrainName()
            #print "train_name", train_name
            #print "t.getActiveTrainName().count(train_name)", t.getActiveTrainName().count(train_name)
            if t.getActiveTrainName().count(train_name) >0:     #check if train_name is contained in
                return t
        return None

    def get_occupied_blocks(self,active_train):
        block_list = active_train.getBlockList()
        section_list = active_train.getAllocatedSectionList()
        start_block = active_train.getStartBlock()
        end_block = active_train.getEndBlock()
        seq_no = active_train.getStartBlockSectionSequenceNumber()
        LastAllocatedSectionSeqNumber = active_train.getLastAllocatedSectionSeqNumber()
        NextSectionToAllocate = active_train.getNextSectionToAllocate()
        LastAllocatedSection = active_train.getLastAllocatedSection()
        LastAllocatedSectionName = active_train.getLastAllocatedSectionName()
        NextSectionToAllocateName = active_train.getNextSectionToAllocateName()
        # print "block_list", block_list
        # print "LastAllocatedSectionSeqNumber",LastAllocatedSectionSeqNumber
        # print "section_list",section_list
        # print "start block", start_block
        # print "end_block", end_block
        # print "getStartBlockSectionSequenceNumber", seq_no
        # print "section_list", section_list
        # print "NextSectionToAllocate", NextSectionToAllocate
        # print "LastAllocatedSection", LastAllocatedSection
        # print "LastAllocatedSectionName", LastAllocatedSectionName
        # print "getNextSectionToAllocateName", NextSectionToAllocateName
        return [block_list, start_block, end_block]

    def check_train_in_block(self, block_name, train_name):
        mem_val = self.get_blockcontents(block_name)
        #print "mem_val", mem_val, "train_name", train_name
        if train_name == mem_val:
            #print "return true"
            return True
        else:
            return False

    def set_train_in_block(self, block_name, train_name):
        self.set_blockcontents(block_name,train_name)

    def get_block_position_of_train(self, train_name):
            allocated_trains = self.get_allocated_trains()


    def get_allocated_trains(self):
        return trains_allocated

    def get_non_allocated_trains(self):
        all_trains = self.get_all_roster_entries_with_speed_profile()
        non_allocated_trains = copy.copy(all_trains)
        for train in trains_allocated:
            if train in non_allocated_trains:
                non_allocated_trains.remove(train)
        return non_allocated_trains

    def get_all_roster_entries_with_speed_profile(self):
        roster_entries_with_speed_profile = []
        r = jmri.jmrit.roster.Roster.getDefault()
        for roster_entry in jmri.jmrit.roster.Roster.getAllEntries(r):
            if self.logLevel > 0: print "roster_entry.getSpeedProfile()",roster_entry,roster_entry.getSpeedProfile()
            if roster_entry.getSpeedProfile() != None:
                roster_entries_with_speed_profile.append(roster_entry.getId())
                if self.logLevel > 0: print "roster_entry.getId()",roster_entry.getId()
        return roster_entries_with_speed_profile

    def get_blockcontents(self, block_name):
        block = blocks.getBlock(block_name)
        value =  block.getValue()
        return value


    def set_blockcontents(self, block_name, value):
        block = blocks.getBlock(block_name)
        value =  block.setValue(value)

    def get_station_and_occupancy_and_block_value_of_train(self, train_to_move):
        ## Check the pressed button
        for station_block_name in g.station_block_list:
            if self.logLevel > 0: print "station_block_name", station_block_name

            #get a True if the block block_value has the train name in it
            block_value_state = self.check_train_in_block(station_block_name, train_to_move)
            block_occupancy_state = self.check_sensor_state_given_block_name(station_block_name)
            if self.logLevel > 0: print "block_value_state1= ",block_value_state
            # # do not attempt to move to where you are
            # button_pressed_in_occupied_station = (button_station_name == station_block_name)

            #check if the block is occupied and has the required train in it
            if block_occupancy_state:
                # and button_pressed_in_occupied_station == False:
                return [station_block_name, block_value_state, block_occupancy_state]
        return None

    def get_position_of_train(self, train_to_move):
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

            # # do not attempt to move to where you are
            # button_pressed_in_occupied_station = (button_station_name == station_block_name)

            #check if the block is occupied and has the required train in it
            if block_value_state == True and block_occupied_state == True:
                # and button_pressed_in_occupied_station == False:
                return station_block_name
        return None

    def check_sensor_state_given_block_name(self, station_block_name):
        #if self.logLevel > 0: print("station block name {}".format(station_block_name))
        layoutBlock = layoutblocks.getLayoutBlock(station_block_name)
        station_sensor = layoutBlock.getOccupancySensor()
        if station_sensor is None:
            OptionDialog().displayMessage(' Sensor in block {} not found'.format(station_block_name))
            return
        currentState = True if station_sensor.getKnownState() == ACTIVE else False
        return currentState

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

        simulation_master = SimulationMaster()
        instanceList.append(simulation_master)
        if simulation_master.setup():
            simulation_master.setName('Simulation Master')
            simulation_master.start()

        scheduler_master = SchedulerMaster()
        instanceList.append(scheduler_master)
        if scheduler_master.setup():
            scheduler_master.setName('Scheduler Master')
            scheduler_master.start()

        monitorTrack_master = MonitorTrackMaster()
        instanceList.append(monitorTrack_master)
        if monitorTrack_master.setup():
            monitorTrack_master.setName('Monitor Track Master')
            monitorTrack_master.start()

        off_action_master = OffActionMaster()
        instanceList.append(off_action_master)

        if off_action_master.setup():
            off_action_master.setName('Off-Action Master')
            off_action_master.start()
        else:
            if self.logLevel > 0: print("Off-Action Master not started")

        #set default valus of buttons
        sensors.getSensor("Express").setKnownState(ACTIVE)
        sensors.getSensor("simulateSensor").setKnownState(INACTIVE)
        sensors.getSensor("setDispatchSensor").setKnownState(ACTIVE)


if __name__ == '__builtin__':
    RunDispatcherMaster()
    # NewTrainMaster checksfor the new train in siding. Needs to inform what station we are in
    #DispatchMaster checks all button sensors
