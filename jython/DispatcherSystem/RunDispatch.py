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
# looks at the allocated trains and ensures that if the blocks are occupied
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
import sys
from java.awt import Dimension

from javax.swing import JScrollPane, JOptionPane, JFrame, JLabel, JButton, JTextField, \
    JFileChooser, JMenu, JMenuItem, JMenuBar,JComboBox,JDialog,JList, WindowConstants

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
if 'trains' not in globals():
    trains = {}           # dictionary of trains shared over classes
global instanceList       # instance list of threads shared over classes
global g
g = None              # graph shared over classes

time_to_stop_in_station = 10000   # time to stop in station in stopping mode(msec)

stopping_sensor_choice = "not_set" # has value

#############################################################################################
# the file was split up to avoid errors
# so now include the split files

FileResetButtonMaster = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/ResetButtonMaster.py')
execfile(FileResetButtonMaster)

StopDispatcherSystem = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/StopDispatcherSystem.py')
execfile(StopDispatcherSystem)


# FileMoveTrain has to go before CreateScheduler
FileMoveTrain = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/MoveTrain.py')
execfile(FileMoveTrain)

CreateScheduler = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Scheduler.py')
execfile(CreateScheduler)

CreateSchedulerPanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/SchedulerPanel.py')
execfile(CreateSchedulerPanel)

CreateSimulation = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Simulation.py')
execfile(CreateSimulation)

my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
sys.path.append(my_path_to_jars) # add the jar to your path
CreateGraph = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/CreateGraph.py')
exec(open (CreateGraph).read())
le = LabelledEdge
g = StationGraph()

#############################################################################################

#class OptionDialog( java.lang.Runnable ) :
class OptionDialog( jmri.jmrit.automat.AbstractAutomaton ) :
    CLOSED_OPTION = False
    logLevel = 0

    def List(self, title, list_items, preferred_size = "default"):
        my_list = \
            JList(list_items)
        my_list.setSelectedIndex(0)
        scrollPane = JScrollPane(my_list);
        if preferred_size != "default":
            scrollPane.setPreferredSize(preferred_size)     # preferred_size should be set to Dimension(300, 500) say
        else:
            no_rows_to_display = min(40, len(list_items))
            my_list.setVisibleRowCount(no_rows_to_display)
            dim = my_list.getPreferredScrollableViewportSize()
            w = int(dim.getWidth())
            h = int(dim.getHeight()) + 10  # to leave a bit of space at bottom. Height of row = approx 20
            scrollPane.setPreferredSize(Dimension(w,h))
        i = []
        self.CLOSED_OPTION = False
        options = ["OK"]
        while len(i) == 0:
            s = JOptionPane().showOptionDialog(None,
            scrollPane,
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
            i = my_list.getSelectedIndices()
        index = i[0]
        return list_items[index]


    #list and option buttons
    def ListOptions(self, list_items, title, options, preferred_size = "default"):
        my_list = JList(list_items)
        if list_items != []:
            my_list.setSelectedIndex(0)
        scrollPane = JScrollPane(my_list);
        if preferred_size != "default":
            scrollPane.setPreferredSize(preferred_size)   # preferred_size should be set to Dimension(300, 500) say
        else:
            no_rows_to_display = min(40, len(list_items))
            my_list.setVisibleRowCount(no_rows_to_display)
            dim = my_list.getPreferredScrollableViewportSize()
            w = int(dim.getWidth()) + 20
            h = int(dim.getHeight()) + 20  # to leave a bit of space at bottom. Height of row = approx 20
            scrollPane.setPreferredSize(Dimension(w,h))
        self.CLOSED_OPTION = False
        s = JOptionPane.showOptionDialog(None,
            scrollPane,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            None,
            options,
            options[1])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return [None,None]
        if list_items == []:
            return [None, options[s]]
        index = my_list.getSelectedIndices()[0]
        return [list_items[index], options[s]]

        # call using
        # list_items = ["list1","list2"]
        # options = ["opt1", "opt2", "opt3"]
        # title = "title"
        # [list, option] = OptionDialog().ListOptions(list_items, title, options)
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

    def displayMessageNonModal(self, msg, jButtonMsg = "OK"):
        global customDialog
        customDialog = JDialog(None, msg, False); # 'true' for modal
        # customDialog.addWindowListener(WindowAdapter())
        #     def windowClosing(self, e):
        #         print("jdialog window closing event received")
        #         # Add your custom closing logic herecustomDialog.addWindowListener(WindowAdapter():


        # Add components to the customDialog
        # customDialog.setSize(1200, 1200)
        dimension = Dimension(400,150)
        customDialog.setPreferredSize(dimension)


        pane = customDialog.getContentPane();
        pane.setLayout(None);
        button = JButton(jButtonMsg, actionPerformed = self.click_action) ;

        button.setBounds(10,10,300,60);
        pane.add(button)
        customDialog.setLocationRelativeTo(None);
        # customDialog.setUndecorated(True)
        customDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
        customDialog.pack();
        customDialog.setVisible(True);

    def click_action(self,e):
        global customDialog
        # global jdialog_closed
        sensors.getSensor("Jdialog_closed").setKnownState(ACTIVE)
        # print "&&&&&&&&&&&&&& jdialog_closed", jdialog_closed
        customDialog.dispose()
        return

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

    def customQuestionMessage4str(self, msg, title, opt1, opt2, opt3, opt4):
        self.CLOSED_OPTION = False
        options = [opt1, opt2, opt3, opt4]
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.DEFAULT_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        if s == 0:
            s1 = opt1
        elif s == 1:
            s1 = opt2
        elif s == 2:
            s1 = opt3
        else:
            s1 = opt4
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

    def __init__(self, list, msg, default = ""):
        jcb = JComboBox(list)
        jcb.setMaximumRowCount(30);
        jcb.setSelectedItem(default);
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
        # if self.stop_master_sensor is None:
        #     return False
        # self.stop_master_sensor.setKnownState(INACTIVE)

        self.modify_master_sensor = sensors.getSensor("modifyMasterSensor")
        # if self.modify_master_sensor is None:
        #     return False
        # self.modify_master_sensor.setKnownState(INACTIVE)

        self.start_scheduler = sensors.getSensor("startSchedulerSensor")
        self.start_scheduler.setKnownState(INACTIVE)
        return True

    def handle(self):
        global timebase
        self.stop_modify_sensors = [sensors.getSensor(sensorName) for sensorName in ["stopMasterSensor", "modifyMasterSensor"]]
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.stop_modify_sensors)
        sensor_active = [sensor.getUserName() for sensor in self.stop_modify_sensors if sensor.getKnownState() == ACTIVE]
        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
        sensor_that_went_active = [sensor for sensor in self.stop_modify_sensors if sensor.getKnownState() == ACTIVE][0]
        sensor_that_went_active1 = [sensor.getUserName() for sensor in self.stop_modify_sensors if sensor.getKnownState() == ACTIVE]
        # start_sensor = sensors.getSensor("startDispatcherSensor")
        stop_sensor =  sensors.getSensor("stopMasterSensor")
        modify_sensor = sensors.getSensor("modifyMasterSensor")

        if sensor_that_went_active == modify_sensor:
            self.stop_via_table()
            # modify_sensor.setKnownState(INACTIVE)
            modify_sensor.setKnownState(INACTIVE)
            return True
        elif sensor_that_went_active == stop_sensor:
            self.remove_timebase_listener()
            self.optionally_reset_all_trains()
            stop_sensor.setKnownState(INACTIVE)
            return

    def optionally_reset_all_trains(self):
        global stored_simulate
        opt1= "keep as is"
        opt2 = "reset all trains"
        res = OptionDialog().customQuestionMessage2str("reset positions of trains?", "", opt1, opt2)
        # store the state of the simulate button
        stored_simulate = sensors.getSensor("simulateSensor").getKnownState()
        if res == opt2:

            self.stop_route_threads()
            # print "self.stop_route_threads()"
            self.remove_train_values()
            # print "self.remove_train_values()"
            self.delete_active_transits()
            # print "self.delete_active_transits()"
            self.stop_all_threads()
            # print "self.stop_all_threads()"
            self.remove_all_trains_from_trains_allocated()
        else:
            self.delete_active_transits()
            # print "self.delete_active_transits()"
            self.stop_all_threads()
            # print "self.stop_all_threads()"

    def stop_via_table(self):
        createandshowGUI3(self)

    def remove_timebase_listener(self):
        global timebase
        global tListener
        self.new_train_sensor = sensors.getSensor("startDispatcherSensor")
        self.new_train_sensor.setKnownState(INACTIVE)
        try:
            #stop the scheduler timebase listener
            if self.logLevel > 0: print "removing listener"
            timebase.removeMinuteChangeListener(tListener)
            return False
        except NameError:
            if self.logLevel > 0: print "Name error"
            return False
        else:
            return False

    def stop_route_threads(self):
        # #remove the train from the transit
        # msg = "Delete all active Transits?\n"+"\nCaution this may disrupt running trains\n"
        # title = "Transits"
        # opt1 = "just remove the route threads (stops trains at end of current transit)"
        # opt2 = "delete transits as well (stops trains immediately)"
        # requested_delete_transits = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)

        instance_list = java.util.concurrent.CopyOnWriteArrayList()
        for train in instanceList:
            instance_list.add(train)
        #stop all threads
        for thread in instance_list:
            thread_name = "" + thread.getName()
            if thread_name.startswith("running_route_"):
                #determine the train nme
                train_name = self.determine_train_name(thread_name,thread)
                #remove the train from the transit
                #self.delete_transits()
                #remove the train from the list of trains
                self.remove_train_name(train_name)
                if thread is not None:
                    if thread.isRunning():
                        if self.logLevel > 0: print 'Stop "{}" thread'.format(thread.getName())
                        thread.stop()
                        instance_list = [instance for instance in instance_list if instance != thread]
                    else:
                        #need this for scheduler in wait state
                        thread.stop()
                        instance_list = [instance for instance in instance_list if instance != thread]

    def determine_train_name(self,thread_name, thread):
        route = thread
        train_name = route.train_name_in
        return train_name

    def remove_train_from_transit(self, train_name):
        if self.logLevel > 0: print "train_name to remove from trainsit", train_name
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        #DF.setState(DF.ICONIFIED);
        activeTrainsList = DF.getActiveTrainsList()
        for i in range(0, activeTrainsList.size()) :
            activeTrain = activeTrainsList.get(i)
            if train_name == activeTrain.getTrainName():
                DF.terminateActiveTrain(activeTrain)
        DF = None

    def remove_train_name(self, train_name):
        global trains_allocated
        global trains_dispatched
        if self.logLevel > 0: print "train to remove", train_name
        # for train in trains_allocated:
        #     if self.logLevel > 0: print "train in trains_allocated", train, ": trains_allocated", trains_allocated
        #     if train == train_name:
        #         trains_allocated.remove(train)
        trains_dispatched_list = java.util.concurrent.CopyOnWriteArrayList()
        for train in trains_dispatched:
            trains_dispatched_list.add(train)

        for train in trains_dispatched_list:
            #print "train in trains_alloceted", train, ": trains_allocated", trains_allocated
            if train == train_name:
                trains_dispatched.remove(train)

    def remove_all_trains_from_trains_allocated(self):
        global trains_allocated
        global trains_dispatched
        if self.logLevel > 0: print "train to remove", train_name
        for train in trains_allocated:
            if self.logLevel > 0: print "train in trains_allocated", train, ": trains_allocated", trains_allocated
            trains_allocated.remove(train)
        trains_dispatched_list = java.util.concurrent.CopyOnWriteArrayList()
        for train in trains_dispatched:
            trains_dispatched_list.add(train)

        for train in trains_dispatched_list:
            #print "train in trains_alloceted", train, ": trains_allocated", trains_allocated
            trains_dispatched.remove(train)

    def stop_all_threads(self):

        # perform actions required before stopping threads
        global scheduler_master      #global so cas be referenced before killing threads
        # scheduler_master = SchedulerMaster()   this has been set in RunDispatchMaster
        scheduler_master.exit()

        # stop all thresds even if there are duplications
        summary = jmri.jmrit.automat.AutomatSummary.instance()
        automatsList = java.util.concurrent.CopyOnWriteArrayList()

        for automat in summary.getAutomats():
            automatsList.add(automat)
        # print "automatsList", automatsList
        for automat in automatsList:
            # print "automat", automat
            if "StopMaster" not in str(automat): automat.stop()
            # print automat, "stopped"
        # print "automatsList2", automatsList
        # print "end stop_all_threads"

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
        #DF.setState(DF.ICONIFIED);
        activeTrainsList = DF.getActiveTrainsList()

        active_trains_list = java.util.concurrent.CopyOnWriteArrayList()
        for activeTrain in activeTrainsList:
            active_trains_list.add(activeTrain)

        for activeTrain in active_trains_list:
            # print "i", i
            # activeTrain = activeTrainsList.get(i)
            if self.logLevel > 0: print "active train", activeTrain
            DF.terminateActiveTrain(activeTrain)
        DF = None

        # set the colours of the tracks back to normal
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        for block in blocks.getNamedBeanSet():
            layoutBlock = LayoutBlockManager.getLayoutBlock(block)
            if layoutBlock != None:
                layoutBlock.setUseExtraColor(False)


        #
        # TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        # #if self.logLevel > 1: print "Section"
        # TransitList = java.util.concurrent.CopyOnWriteArrayList()
        # for transit in TransitManager.getNamedBeanSet():
        #     TransitList.add(transit)
        #
        # for transit in TransitList:
        #     if self.logLevel > 1: print "deleting Transit ", transit.getUserName()
        #     TransitManager.deleteTransit(transit)

    def remove_values(self, train_name):
        for block in blocks.getNamedBeanSet():
            if block.getValue() == train_name:
                block.setValue(None)

    def remove_train_values(self):
        for block in blocks.getNamedBeanSet():
            if block.getValue() != None:
                block.setValue(None)

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
        modify_sensor =  sensors.getSensor("modifyMasterSensor")
        if self.logLevel > 0: print "start_sensor" , start_sensor
        if self.logLevel > 0: print "stop_sensor" , stop_sensor
        if sensor_that_went_inactive in self.run_stop_sensors:
            if self.logLevel > 0: print "run stop sensor went inactive"

            if sensor_that_went_inactive == start_sensor:
                self.sensor_to_look_for = stop_sensor
                if self.logLevel > 0: print "start sensor went inactive"
                if self.logLevel > 0: print "setting stop sensor active"
                stop_sensor.setKnownState(ACTIVE)
                # modify_sensor.setKnownState(ACTIVE)
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
            modify_sensor =  sensors.getSensor("modifyMasterSensor")
            if sensor_that_went_inactive == start_sensor:
                self.sensor_to_look_for = stop_sensor
                if self.logLevel > 0: print "start sensor went inactive"
                if self.logLevel > 0: print "setting stop sensor active"
                stop_sensor.setKnownState(ACTIVE)
                # modify_sensor.setKnownState(ACTIVE)
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
            [sensors.getSensor(sensorName) for sensorName in ["setDispatchSensor", "setRouteSensor", "setStoppingDistanceSensor", "setStationWaitTimeSensor", "setStationDirectionSensor"]]
        #self.route_dispatch_states = [self.check_sensor_state(rd_sensor) for rd_sensor in self.setup_route_or_run_dispatch_sensors]
        pass

    def get_run_buttons(self):
        self.run_stop_sensors = [sensors.getSensor(sensorName) for sensorName in ["startDispatcherSensor"]]



#DF = None

class DispatchMaster(jmri.jmrit.automat.AbstractAutomaton):

    # Monitors the Station buttons and perforns actins dependent upon what mode one is in e.g.:
    # Run Dispatch
    # Setup Route
    # Set stopping Length
    # Set Station Wait Time
    # Set Station Direction
    # Restrict Operation of Transit (only run if a block is not occupied)

    button_sensors_to_watch = []
    button_dict = {}

    def __init__(self):
        self.logLevel = 0
        global trains_dispatched
        trains_dispatched = []
        #initialise all block_value variables
        # for block in blocks.getNamedBeanSet():
        #     LayoutBlockManagerdispa=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        #     if LayoutBlockManager.getLayoutBlock(block) != None:
        #         block.setValue(None)

    def init(self):
        if self.logLevel > 0: print 'Create DispatchMaster Thread'


    def setup(self):
        #global DF

        if self.logLevel > 1: print "starting DispatchMaster setup"

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
        self.rbm = ResetButtonMaster()
        return True

    def  handle(self):

        global trains_allocated
        global trains_dispatched

        #only one button is active. We will keep it that way
        # print "dispatch master"
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
        modify_station_direction_sensor = sensors.getSensor("setStationDirectionSensor")
        modify_stop_sensors_sensor = sensors.getSensor("setStopSensor")
        inhibit_running_transit_if_block_occupied_sensor = sensors.getSensor("setTransitBlockRestrictionSensor")
        if self.logLevel > 0: print "set_route_sensor.getKnownState()",set_route_sensor.getKnownState(),
        self.reset_buttons(button_sensors_to_watch_JavaList)
        if set_route_sensor.getKnownState() == ACTIVE:
            if self.logLevel > 0: print ("set_route")
            test = self.set_route(sensor_changed, button_sensor_name, button_station_name)
            if self.logLevel > 0: print "test = " , test
            sensor_changed.setKnownState(INACTIVE)
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
        elif setup_dispatch_sensor.getKnownState() == ACTIVE:
            if self.logLevel > 0: print ("dispatch_train")
            self.dispatch_train(sensor_changed, button_sensor_name, button_station_name)
            #self.button_sensors_to_watch = copy.copy(self.button_sensors)
            sensor_changed.setKnownState(INACTIVE)
        elif modify_stopping_length_sensor.getKnownState() == ACTIVE:
            # print "A"
            sensor_changed.setKnownState(INACTIVE)
            if self.modify_individual_stopping_length(sensor_changed, button_sensor_name, button_station_name):
                sensor_changed.setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
            else:
                # reset all buttons so we check all of them
                self.reset_selection_buttons()
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
        elif modify_station_wait_time_sensor.getKnownState() == ACTIVE:
            if self.modify_individual_station_wait_time(sensor_changed, button_sensor_name, button_station_name):
                sensor_changed.setKnownState(INACTIVE)
            else:
                #cancelled: reset all buttons so we check all of them
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
        elif modify_station_direction_sensor.getKnownState() == ACTIVE:
            # sensor_changed_saved = sensor_changed
            if self.modify_individual_station_direction(sensor_changed, button_sensor_name, button_station_name):
                # The self.trainInfo files for the express routes need to be regenerated
                # so that the express routes are the shortest path allowed
                ResetButtonMaster().regenerate_traininfo_files("Regenerated TrainInfo Files")
                sensor_changed.setKnownState(INACTIVE)
            else:
                #cancelled: reset all buttons so we check all of them
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
        elif modify_stop_sensors_sensor.getKnownState() == ACTIVE:
            if self.modify_stop_sensors1(sensor_changed, button_sensor_name, button_station_name):
                sensor_changed.setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
            else:
                sensor_changed.setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
        elif inhibit_running_transit_if_block_occupied_sensor.getKnownState() == ACTIVE:
            if self.restrict_transit_operation(sensor_changed, button_sensor_name, button_station_name):
                sensor_changed.setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
                #ensure that we can press inhibit_running_transit_if_block_occupied_sensor
                #self.rbm.switch_sensors_requiring_station_buttons(inhibit_running_transit_if_block_occupied_sensor, "sensor_off")
            else:
                #cancelled: reset all buttons so we check all of them
                sensor_changed.setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
        else:
            title = "station button error"
            msg = "select one of 'Run Dispatch', 'Setup Route', 'Set Stopping Length', 'Set Station Wait Time' \nfor the station buttons to have effect"
            self.od.displayMessage(msg,title)
            pass

        if self.logLevel > 0: print "end handle1"
        self.waitMsec(1000)
        # print "end dispatch master"
        return True

    def reset_buttons(self, button_sensors_to_watch_JavaList):
        #print "resetting buttons", button_sensors_to_watch_JavaList
        for button in button_sensors_to_watch_JavaList:
            button.setKnownState(INACTIVE)


    def set_route(self, sensor_changed, button_sensor_name, button_station_name):

        sensor_changed.setKnownState(ACTIVE)     #put the state of the button back to active so we can see it
        no_stations_chosen = 1
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route = RouteManager.newRoute("temp_name")

        LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
        location = LocationManager.newLocation(button_station_name)
        #print "addLocation1", location
        route.addLocation(location)


        first_station = button_station_name
        prev_station = first_station
        last_station = first_station

        self.button_sensors_to_watch = copy.copy(self.button_sensors)
        self.button_sensors_to_watch.remove(sensor_changed)
        selected_actions = []
        stop_mode = None
        complete = False
        i = 0
        while complete == False:
            i +=1
            #select station start
            no_stations_chosen = i+1
            if self.logLevel > 0: print ("In loop")
            opt1 = "Select another station"
            opt2 = "Complete Route"
            opt3 = "Set action to run in this station"
            opt4 = "Specify Stopping Mode at Station"
            if i == 1:
                msg = "Start of Route Selectiom"
                msg = msg + "\nselected station " + button_station_name + "."
                title = "Continue selecting stations?"
                s = self.od.customQuestionMessage2str(msg, title, opt1, opt3)
            else:
                msg = "selected station " + button_station_name + ". \n"
                if selected_actions != None and selected_actions != []:
                      msg += " and actions " + ", ".join(selected_actions) + "\n"
                msg += "Have you more stations on route?"
                title = "Continue selecting stations"
                if prev_station == last_station:   # start of route
                    s = self.od.customQuestionMessage3str(msg,title, opt1, opt2, opt3)
                else:
                    transit_name = self.get_transit_name(prev_station, last_station)
                    if self.forward_stopping_sensor_exists(transit_name):
                        s = self.od.customQuestionMessage4str(msg,title, opt1, opt2, opt3, opt4)
                    else:
                        s = self.od.customQuestionMessage3str(msg,title, opt1, opt2, opt3)

            if self.od.CLOSED_OPTION == True:
                sensor_changed.setKnownState(INACTIVE)
                RouteManager.deregister(route)
                return
            elif s == opt1:
                selected_actions = []
                self.get_buttons()
                # self.button_sensors_to_watch = copy.copy(self.button_sensors)
                # self.button_sensors_to_watch.remove(sensor_changed)
                prev_sensor_changed = sensor_changed
                prev_station = last_station
                [last_station, sensor_changed] = self.wait_for_button(prev_sensor_changed)
                button_station_name = last_station
                location = LocationManager.newLocation(button_station_name)
                routeLocation = route.addLocation(location)
                loc = route.getLastLocationByName(button_station_name)
                stop_mode = None
            elif s == opt2:
                complete = True
            elif s == opt3:
                selected_actions = self.add_actions(route,LocationManager, button_station_name, no_stations_chosen, selected_actions)
                complete = False
            elif s == opt4:
                stop_mode = self.get_stop_mode()
                routeLocation = route.getLastLocationByName(button_station_name)
                if stop_mode != None:
                    self.save_stop_mode(stop_mode, routeLocation)
                complete = False
            Firstloop = False


        route_name_prefix = first_station + "_to_" + last_station
        route_name = route_name_prefix
        i = 0
        while RouteManager.getRouteByName(route_name) != None:
            i+=1
            route_name = route_name_prefix + "_" + str(i)
        route.setName(route_name)
        msg = "completed route  " + route_name + ". you may see the route by clicking View/Edit Routes."
        opt1 = "Create another route"
        opt2 = "Finish selecting routes"
        opt3 = "View Route"
        reply = self.od.customQuestionMessage2str(msg, title, opt1, opt2)
        sensor_changed.setKnownState(INACTIVE)
        if reply == opt3:
            self.show_routes()
        elif reply == opt1:
            self.save_routes()
        elif reply == opt2:
            self.save_routes()
            set_route_sensor = sensors.getSensor("setRouteSensor")
            ResetButtonMaster().switch_sensors_requiring_station_buttons(set_route_sensor, "sensor_off")
            self.reset_selection_buttons()
        if self.logLevel > 0: print ("terminated dispatch")
        return True

    def get_transit_name(self, station_from_name, station_to_name):
        StateVertex_start = station_from_name
        StateVertex_end = station_to_name
        paths = DijkstraShortestPath.findPathBetween(g.g_express, StateVertex_start, StateVertex_end)
        e = paths[paths.size()-1]
        traininfoFileName = self.get_filename(e, "fwd")
        trainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(traininfoFileName)
        transit_name = trainInfo.getTransitName()
        return transit_name
    def forward_stopping_sensor_exists(self, transit_name):
        forward_stopping_sensor = self.forward_stopping_sensor(transit_name)
        if forward_stopping_sensor != None:
            return True
        else:
            return False

    def forward_stopping_sensor(self, transit_name):
        transit = transits.getTransit(transit_name)
        transit_section_list = transit.getTransitSectionList()
        transit_section = transit_section_list[transit.getMaxSequence()-1]
        section = transit_section.getSection()
        forward_stopping_sensor = section.getForwardStoppingSensor()
        return forward_stopping_sensor
    def wait_for_button(self, prev_sensor_changed):
        self.button_sensors_to_watch = copy.copy(self.button_sensors)
        if prev_sensor_changed in self.button_sensors_to_watch:
            self.button_sensors_to_watch.remove(prev_sensor_changed)
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
        prev_sensor_changed.setKnownState(INACTIVE)
        sensor_changed = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
        button_sensor_name = sensor_changed.getUserName()
        button_station_name = self.get_block_name_from_button_sensor_name(button_sensor_name)
        return [button_station_name, sensor_changed]
    def get_stop_mode(self):
        title = "Set Stop mode"

        msg = "set Stop mode"
        opt1 = "Use Stop Sensor"
        opt2 = "Use Default"
        opt3 = "Stop using Speed Profile"
        reply = self.od.customQuestionMessage3str(msg,title,opt1,opt2,opt3)
        return reply

    def save_stop_mode(self, stop_mode, location):
        if stop_mode != "Use Default":
            location.setComment(stop_mode)

    def action_directory_in_DispatcherSystem(self):
        path = jmri.util.FileUtil.getScriptsPath() + "DispatcherSystem" + java.io.File.separator + "actions"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator
    def action_directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "pythonfiles"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator

    def add_actions(self, route, LocationManager, button_station_name, no_stations_chosen, selected_actions):
        directory = self.action_directory_in_DispatcherSystem()
        files = os.listdir(directory)
        python_files = [f for f in files if f.endswith(".py")]


        directory = self.action_directory()
        files = os.listdir(directory)
        python_files2 = [f for f in files if f.endswith(".py")]
        python_files.extend(python_files2)
        # display the list to select the required python file
        if python_files == []:
            msg =  "no python files in directory " + directory
            self.od.displayMessage((msg))
            return 'cancel'
        else:
            select_action_file = True
            iteration = 1
            # selected_actions = []
            while select_action_file:
                if iteration == 0:
                    msg = "select action file \n(must be in directory " + directory + " )"
                    python_file = modifiableJComboBox(python_files,msg).return_val()
                    self.add_python_file_to_route(python_file, route, LocationManager)
                    selected_actions.append(python_file)
                else:
                    if selected_actions == []:
                        selected_actions1 = ["none selected yet"]
                    else:
                        selected_actions1 = selected_actions
                    title = "selected " + ','.join(selected_actions1)
                    if iteration == 1:
                        opt1 = "select action file"
                    else:
                        opt1 = "select another action file"
                    opt2 = "finished adding action files"
                    options = [opt1, opt2]
                    [python_file, option] = OptionDialog().ListOptions(python_files, title, options)
                    if option == opt1:
                        selected_actions.append(python_file)
                        self.add_python_file_to_route(python_file, route, LocationManager)
                    elif option == opt2:
                        select_action_file = False
                iteration += 1
            # what_to_do = self.check_whether_select_another_station(button_station_name, selected_actions, no_stations_chosen)
            return selected_actions

    def add_python_file_to_route(self, python_file, route, LocationManager):
        location = LocationManager.newLocation(python_file)
        route.addLocation(location)

    def check_whether_select_another_station(self, button_station_name, selected_actions, no_of_stations_chosen):
        if no_of_stations_chosen == 1:
            msg = "selected station " + button_station_name + ". \n" + \
                  " and actions " + ", ".join(selected_actions) + "\n" + \
                  "Continue with selecting route?"
        else:
            msg = "selected station " + button_station_name + ". \n" + \
                  " and actions " + ", ".join(selected_actions) + "\n" + \
                  "Have you more stations on route?"
        title = "Continue selecting stations"

        opt1 = "Select another station"
        opt2 = "Complete Route"
        opt3 = 'cancel'

        if no_of_stations_chosen == 1:
            s = self.od.customQuestionMessage2str(msg,title,opt1,opt3)
            if s == self.od.CLOSED_OPTION:
                return 'cancel'
            if s == opt1:
                return 'continue'
            if s == opt3:
                return 'cancel'

        else:
            s = self.od.customQuestionMessage3str(msg,title,opt1,opt2,opt3)
            if s == self.od.CLOSED_OPTION:
                return 'cancel'
            if s == opt1:
                return 'continue'
            if s == opt2:
                return 'complete'
            if s == opt3:
                return 'cancel'

    def show_routes(self):
        a = jmri.jmrit.operations.routes.RoutesTableAction()
        a.actionPerformed(None)

    def save_routes(self):
        jmri.jmrit.operations.OperationsXml.save()

    def modify_individual_stopping_length(self, sensor_changed, button_sensor_name, button_station_name):
        msg = "selected station " + button_station_name + ". \nSelect the next station to modify the stopping length?"
        title = "Select next Station"

        opt1 = "Select next station"
        opt2 = "Cancel stopping length modification"

        s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
        if self.od.CLOSED_OPTION == True:
            self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
            return False
        if s == opt2:
            self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
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
        station_from = first_station
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

            msg = "selected transit " + filename_fwd + ". \nDo you wish to modify the stopping length fraction ?"
            title = "Continue selecting stations"

            opt1 = "Cancel Stopping length modification"
            opt2 = "Modify length"

            s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
            if self.od.CLOSED_OPTION == True :
                sensor_changed.setKnownState(INACTIVE)
                # sensors.getSensor("setStoppingDistanceSensor").setKnownState(INACTIVE)
                self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
                return False
            elif s == opt1:
                sensor_changed.setKnownState(INACTIVE)
                # sensors.getSensor("setStoppingDistanceSensor").setKnownState(INACTIVE)
                self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
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

        #go to this bit when complete == True

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
              "   (" + str(round(length_of_last_section/2.54, 1)) + " inches)" + \
              "\nPrevious stopping position: " + str(round(previous_stopping_position,1)) + " cm " + \
              "   (" + str(round(previous_stopping_position/2.54, 1)) + " inches )" + \
              "\nbefore the calculated stopping position" + \
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
            self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
            return False
        if s == opt2:
            self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
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
            self.reset_selection_buttons()
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            self.button_sensors_to_watch.remove(sensor_changed)
            sensor_changed.setKnownState(INACTIVE)

        #go to this bit when complete == True

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

    def modify_stop_sensors1(self, sensor_changed, button_sensor_name, button_station_name):
        msg = "selected station " + button_station_name + ". \nSelect the next station to modify the stopping length?"
        title = "Select next Station"

        opt1 = "Select next station"
        opt2 = "Cancel stop sensor modification"

        s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
        if self.od.CLOSED_OPTION == True:
            self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
            return False
        if s == opt2:
            self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
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
        station_from = first_station
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

            msg = "selected transit " + filename_fwd + ". \nDo you wish to set the stop sensor ?"
            title = "Continue selecting stations"

            opt1 = "Cancel setting Stop Sensor"
            opt2 = "Set Stop Sensor"

            s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
            if self.od.CLOSED_OPTION == True :
                sensor_changed.setKnownState(INACTIVE)
                # sensors.getSensor("setStoppingDistanceSensor").setKnownState(INACTIVE)
                self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
                return False
            elif s == opt1:
                sensor_changed.setKnownState(INACTIVE)
                # sensors.getSensor("setStoppingDistanceSensor").setKnownState(INACTIVE)
                self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
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

        #go to this bit when complete == True

        #get traininfo and stopping fraction
        # print "A"
        filename_fwd = self.get_filename(found_edge, "fwd")
        trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
        # stopping_fraction = trainInfo_fwd.getStopBySpeedProfileAdjust()

        #modify stopping fraction in traininfo
        last_section = self.last_section_of_transit(trainInfo_fwd)
        # print "last section", last_section
        sensor_name = self.choose_stop_sensor(last_section)
        # print "sensor_name", sensor_name
        last_section.setForwardStoppingSensorName(sensor_name)
        sensor_changed.setKnownState(INACTIVE)
        self.button_sensors_to_watch = copy.copy(self.button_sensors)
        self.reset_selection_buttons()    #set/reset a dummy sensor to reset the buttons
        return True


    def choose_stop_sensor(self, section):
        title = "set stop sensor for section " + section.getUserName()
        list_items = [sensor.getUserName() \
                      for sensor in sensors.getNamedBeanSet() \
                      if sensor.getUserName() != None and not sensor.getUserName().startswith("IY:AUTO")]
        list_items.sort()
        if self.logLevel > 1: print "list_items", list_items
        options = ["choose stop sensor","fred"]
        ans = modifiableJComboBox(list_items, title).return_val()

        if self.logLevel > 1: print "option", ans
        sensor= sensors.getSensor(str(ans))
        if self.logLevel > 1: print "sensor", sensor.getUserName()
        return str(ans)

    def modify_individual_station_direction(self, sensor_changed, button_sensor_name, button_station_name):
        global g
        msg = "selected station " + button_station_name + ". \nSelect the next station to modify the station direction?"
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
        if self.logLevel > 0: print ("in modify station direction")

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

            # We now have the blocks we want to use, provided the station blocks are next to each other.
            # Probably they are not,
            # so we need to find the two adjacent blocks on the route between the two station blocks

            # location = LocationManager.newLocation(button_station_name)
            # route.addLocation(location)
            last_station = button_station_name

            # print "last station", last_station , "first station", first_station, "button_sensor_name", button_sensor_name, "button_station_name", button_station_name

            #get the transit corresponding to first_station last_station
            done = False
            for e in g.g_express.edgeSet():
                from_station_name = g.g_stopping.getEdgeSource(e)
                to_station_name = g.g_stopping.getEdgeTarget(e)
                # print "from_station_name",str(from_station_name), "to_station_name", str(to_station_name)
                if from_station_name == first_station and to_station_name == last_station:
                    found_edge = e
                    # print "breaking ", "last station", last_station , "first station", first_station, "button_sensor_name", button_sensor_name, "button_station_name", button_station_name
                    # print "breaking from_station_name",str(from_station_name), "to_station_name", str(to_station_name)
                    done = True
                    break
            if done == False:
                done1 = False
                for e in g.g_express.edgeSet():
                    to_station_name = g.g_stopping.getEdgeSource(e)
                    from_station_name = g.g_stopping.getEdgeTarget(e)
                    # print "from_station_name",str(from_station_name), "to_station_name", str(to_station_name)
                    if from_station_name == first_station and to_station_name == last_station:
                        found_edge = e
                        # print "breaking ", "last station", last_station , "first station", first_station, "button_sensor_name", button_sensor_name, "button_station_name", button_station_name
                        # print "breaking from_station_name", from_station_name, "to_station_name", to_station_name
                        done1 = True
                        break
                if done1 == False:
                    msg = "there is no direct route between " + to_station_name + " and " + from_station_name + \
                          "\nThis is probably because the route is not allowed due to missing signal masts\n" + \
                          "Please select two other stations"
                    self.od.displayMessage(msg,title)
                    self.get_buttons()
                    self.button_sensors_to_watch = copy.copy(self.button_sensors)
                    self.button_sensors_to_watch.remove(sensor_changed)
                    sensor_changed.setKnownState(INACTIVE)
                    return

            # filename_fwd = self.get_filename(found_edge, "fwd")
            # filename_rvs = self.get_filename(found_edge, "rvs")

            msg = "selected station " + button_station_name + ". \nDo you wish to modify the station direction ?"
            title = "Continue selecting stations"

            opt1 = "Allow only the direction from " + from_station_name + " towards " + str(to_station_name)
            opt2 = "Allow only the direction from " + to_station_name + " towards " + from_station_name
            opt3 = "Allow 2-way working "

            s = self.od.customQuestionMessage3str(msg,title,opt1,opt2, opt3)
            if self.od.CLOSED_OPTION == True :
                sensor_changed.setKnownState(INACTIVE)
                sensors.getSensor("setStationDirectionSensor").setKnownState(INACTIVE)
                self.button_sensors_to_watch = copy.copy(self.button_sensors)
                return False
            elif s == opt1:
                sensor_changed.setKnownState(INACTIVE)
                first_two_blocks = self.getFirstTwoBlocksInAllowedDirection(e, first_station)
                first_two_blocks = self.swapPositions(first_two_blocks,0,1)
                list_of_inhibited_blocks = self.store_the_two_blocks(first_two_blocks)
                g = StationGraph()  # recalculate the weights on the edges
                sensor_changed.setKnownState(INACTIVE)
                return True
            if s == opt2:
                sensor_changed.setKnownState(INACTIVE)
                first_two_blocks = self.getFirstTwoBlocksInAllowedDirection(e, first_station)
                list_of_inhibited_blocks = self.store_the_two_blocks(first_two_blocks)
                g = StationGraph() # recalculate the weights on the edges
                sensor_changed.setKnownState(INACTIVE)
                return True
            if s == opt3:
                sensor_changed.setKnownState(INACTIVE)
                first_two_blocks = self.getFirstTwoBlocksInAllowedDirection(e, first_station)
                list_of_inhibited_blocks = self.remove_the_two_blocks(first_two_blocks)
                first_two_blocks = self.swapPositions(first_two_blocks,0,1)
                list_of_inhibited_blocks = self.remove_the_two_blocks(first_two_blocks)  #remove from file
                g = StationGraph()      # recalculate the weights on the edges
                sensor_changed.setKnownState(INACTIVE)
                return True
            Firstloop = False
            self.get_buttons()
            self.reset_selection_buttons()
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            self.button_sensors_to_watch.remove(sensor_changed)
            sensor_changed.setKnownState(INACTIVE)

    def restrict_transit_operation(self, sensor_changed, button_sensor_name, button_station_name):
        msg = "selected station " + button_station_name + ". \nSelect the next station to restrict operation of transit?"
        title = "Select next Station"
        opt1 = "Select next station"
        opt2 = "Cancel station transit restriction"
        s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
        if self.od.CLOSED_OPTION == True:
            return False
        if s == opt2:
            return False
        if self.logLevel > 0: print "button_station_name", button_station_name
        if self.logLevel > 0: print "button_sensor_name", button_sensor_name
        #set name of route
        if self.logLevel > 0: print ("in restrict transit")

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
            sensor_changed1 = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
            button_sensor_name1 = sensor_changed1.getUserName()
            button_station_name1 = self.get_block_name_from_button_sensor_name(button_sensor_name1)

            last_station = button_station_name1

            #get the transit corresponding to first_station last_station
            found_edge = None
            for e in g.g_express.edgeSet():
                from_station_name = g.g_stopping.getEdgeSource(e)
                to_station_name = g.g_stopping.getEdgeTarget(e)
                if from_station_name == first_station and to_station_name == last_station:
                    found_edge = e
                    break

            success = True
            if found_edge == None:
                msg = "selected station " + first_station + ". \nCannot be reached from " + last_station
                title = "Error"
                opt1 = "Try Again: Select second Station Again"
                opt2 = "Cancel station transit restriction"
                s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
                if self.od.CLOSED_OPTION == True:
                    return False
                if s == opt2:
                    return False
                if s == opt1:
                    sensors.getSensor(sensor_changed1.getUserName()).setKnownState(INACTIVE)
                    success = False

            if success:
                filename_fwd = self.get_filename(found_edge, "fwd")
                filename_rvs = self.get_filename(found_edge, "rvs")

                msg = "selected station " + button_station_name + ". \nDo you wish to set the block that must be clear for the transit to run ?"
                title = "Select Option"

                opt2 = "Select Station to Select its Block"
                opt1 = "Cancel: Choose another transit"


                s = self.od.customQuestionMessage2str(msg,title,opt2,opt1)
                if self.od.CLOSED_OPTION == True :
                    sensor_changed1.setKnownState(INACTIVE)
                    sensors.getSensor("setTransitBlockRestrictionSensor").setKnownState(INACTIVE)
                    self.button_sensors_to_watch = copy.copy(self.button_sensors)
                    return False
                elif s == opt1:   #cancel
                    sensor_changed1.setKnownState(INACTIVE)
                    sensors.getSensor("setTransitBlockRestrictionSensor").setKnownState(INACTIVE)
                    self.button_sensors_to_watch = copy.copy(self.button_sensors)
                    return False
                if s == opt2:    #proceed
                    sensor_changed1.setKnownState(INACTIVE)
                    #choose another station block: the dependent station block. The transit set by the first two station blocks only runs if the last station block is free
                    [result, sensor_changed, button_sensor_name2, button_station_name2]  = \
                        self.set_transit_block(sensor_changed, button_sensor_name, button_station_name, \
                                               sensor_changed1, button_sensor_name1, button_station_name1)
                    if result == False:
                        sensor_changed1.setKnownState(INACTIVE)
                        sensors.getSensor("setTransitBlockRestrictionSensor").setKnownState(INACTIVE)
                        self.button_sensors_to_watch = copy.copy(self.button_sensors)
                        return False
                    else:
                        complete = True
                Firstloop = False

            sensor_to_change = sensors.getSensor("setTransitBlockRestrictionSensor")
            self.rbm.switch_sensors_requiring_station_buttons(sensor_to_change, "sensor_off")
            self.reset_selection_buttons()
            self.get_buttons()
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            #self.button_sensors_to_watch.remove(sensor_changed)
            sensor_to_change.setKnownState(INACTIVE)

        #go to this bit when complete == True

        # # set transit block
        filename_fwd = self.get_filename(found_edge, "fwd")
        filename_rvs = self.get_filename(found_edge, "rvs")

        new_transit_block_name = button_station_name2

        self.write_to_TrainInfo(found_edge, new_transit_block_name)

        if self.logLevel > 0: print "saved new block_name for Transit", new_transit_block_name

        return True

    def reset_selection_buttons(self):
        # set the dummy contrtol sensor active which triggers the routine in ResetButtonnMaster
        # the buttons are reset so we can select buttons Set Stopping Length throug restrict transit operation again
        sensors.getSensor("DummyControlSensor").setKnownState(ACTIVE)
        self.waitMsec(3000)
        sensors.getSensor("DummyControlSensor").setKnownState(INACTIVE)


    def write_to_TrainInfo(self, edge, new_transit_block_name):

        filename_fwd = self.get_filename(edge, "fwd")
        filename_rvs = self.get_filename(edge, "rvs")

        trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
        transit_block_name = trainInfo_fwd.getBlockName()
        trainInfo_fwd.setBlockName(new_transit_block_name)

        #transit_block_name1 = trainInfo_fwd.getBlockName()
        # write the newtraininfo back to file
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(trainInfo_fwd, filename_fwd)

        # do same with reverse
        # filename_rvs = self.get_filename(found_edge, "rvs")
        trainInfo_rvs = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_rvs)
        trainInfo_rvs.setBlockName(new_transit_block_name)
        # write the newtraininfo back to file
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(trainInfo_rvs, filename_rvs)
    def retrieve_from_Traininfo(self, edge):
        filename_fwd = self.get_filename(edge, "fwd")
        trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
        transit_block_name = trainInfo_fwd.getBlockName()
        return [transit_block_name]
    def set_transit_block(self, sensor_changed, button_sensor_name, button_station_name,
                                sensor_changed1, button_sensor_name1, button_station_name1):


        # we have the sensor, sensor_name and block name for the first two stations defining the transit
        # we now wish to specify the triple for the block which needs to be free for the transit to run

        # msg = "selected stations " + button_station_name + " and " + button_station_name1 + \
        #       "\nSelect the next station to specify the block that has to be free to enabler the transit to run?"
        # title = "Select next Station"
        #
        # opt1 = "Select next station"
        # opt2 = "Cancel station transit restriction"
        #
        # s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
        # if self.od.CLOSED_OPTION == True:
        #     return False
        # if s == opt2:
        #     return False
        if self.logLevel > 0: print "button_station_name", button_station_name
        if self.logLevel > 0: print "button_sensor_name", button_sensor_name
        #
        if self.logLevel > 0: print ("in restrict transit: choose 3rd Station")

        first_station = button_station_name
        last_station = button_station_name1
        if self.logLevel > 0: print "first_station", first_station, "last_station", last_station
        transit_block_station = None
        # route.addLocation(location)
        self.button_sensors_to_watch = copy.copy(self.button_sensors)
        self.button_sensors_to_watch.remove(sensor_changed)
        self.button_sensors_to_watch.remove(sensor_changed1)

        if self.logLevel > 0: print ("In loop")
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
        sensor_changed = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE][0]
        button_sensor_name2 = sensor_changed.getUserName()
        button_station_name2 = self.get_block_name_from_button_sensor_name(button_sensor_name2)

        transit_block_station = button_station_name2

        #get the transit corresponding to first_station last_station

        # for e in g.g_express.edgeSet():
        #     from_station_name = g.g_stopping.getEdgeSource(e)
        #     to_station_name = g.g_stopping.getEdgeTarget(e)
        #     if from_station_name == first_station and to_station_name == last_station:
        #         found_edge = e
        #         break
        # filename_fwd = self.get_filename(found_edge, "fwd")
        # filename_rvs = self.get_filename(found_edge, "rvs")


        msg = "for transit from station" + first_station + "to station " + last_station + \
            "\nselected station " + transit_block_station + \
            ". \nDo you wish the transit not to run when this station is occupied ?"
        title = "Selected stations"

        opt1 = "Cancel and select another transit"
        opt2 = "restrict this transit"

        s = self.od.customQuestionMessage2str(msg,title,opt2,opt1)
        if self.od.CLOSED_OPTION == True :
            sensor_changed.setKnownState(INACTIVE)
            sensors.getSensor("setTransitBlockRestrictionSensor").setKnownState(INACTIVE)
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            return [False, sensor_changed, button_sensor_name2, button_station_name2]
        elif s == opt1:
            sensor_changed.setKnownState(INACTIVE)
            sensors.getSensor("setTransitBlockRestrictionSensor").setKnownState(INACTIVE)
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            return [False, sensor_changed, button_sensor_name2, button_station_name2]
        if s == opt2:
            sensor_changed.setKnownState(INACTIVE)
            complete = True
            self.get_buttons()
            self.button_sensors_to_watch = copy.copy(self.button_sensors)
            return [True, sensor_changed, button_sensor_name2, button_station_name2]


    def swapPositions(self, list, pos1, pos2):
        #swap positions of list
        list[pos1], list[pos2] = list[pos2], list[pos1]
        return list

    def getFirstTwoBlocksInAllowedDirection(self, e, first_station_block):
        path_name =  e.getItem("path_name")
        # get last occurence in path in case path goes through turntable and return
        index = path_name[::-1].index(first_station_block)
        position_of_station_block = len(path_name) - index - 1
        first = path_name[position_of_station_block]
        second = path_name[position_of_station_block+1]
        first2blocks = [first,second]
        return first2blocks

    def get_section(self, path_name, last_block_name, penultimate):
        # first_block = blocks.getBlock(first_block_name)
        # last_block = blocks.getBlock(last_block_name)
        # print "path_name", path_name
        if self.logLevel > 1: print "in get section"
        list_of_sections = []
        for section in sections.getNamedBeanSet():
            if section.getEntryBlock() != None:
                # print "z", section.getUserName()
                seq_no = section.getBlockSequenceNumber(section.getExitBlock())
                # print "a" , seq_no
                if seq_no == 0:
                    pb_no = 1
                else:
                    pb_no = seq_no - 1
                    # print "b", seq_no -1
                try:
                    # print
                    penultimate_block = section.getBlockBySequenceNumber(pb_no)
                except:
                    pass
                if penultimate_block is not None:
                    # print "c"
                    # print "penultimate_block", penultimate_block.getUserName(), "penultimate", penultimate
                    if section.getEntryBlock().getUserName() in path_name and \
                            section.getExitBlock().getUserName() == last_block_name and \
                            penultimate_block.getUserName() == penultimate and \
                            section.getUserName().__contains__(":"):   # section is not first section from siding
                        list_of_sections.append(section)
        if self.logLevel > -1: print "exiting get_section"
        return list_of_sections

    def getLastBlockInAllowedDirection(self, e, first_station_block):
        path_name =  e.getItem("path_name")
        if self.logLevel > -1: print "path_name", path_name
        # get last occurence in path in case path goes through turntable and return
        index = path_name[::-1].index(first_station_block)
        position_of_station_block = len(path_name) - 1 #- index - 1
        last = path_name[position_of_station_block]
        penultimate = path_name[position_of_station_block-1]
        last2blocks = [last,penultimate]
        return last2blocks

    def delete_block_pair_from_list(self, entry_to_delete):
        existing = self.read_list()
        if entry_to_delete != "no inhibited directions":
            existing.remove(entry_to_delete)
        self.write_list(existing)
        return existing

    def store_the_two_blocks(self, first_two_blocks):
        list_inhibited_blocks = self.read_list()
        existing = list_inhibited_blocks
        to_add = first_two_blocks
        #to_add = first_two_blocks[0]+"."+first_two_blocks[1]
        if to_add not in existing:
            # print "not in existing", "to_add", to_add, "existing", existing
            existing.append(to_add)
        self.write_list(existing)
        # print "final" , existing
        return existing

    def remove_the_two_blocks(self, first_two_blocks):
        list_inhibited_blocks = self.read_list()
        existing = list_inhibited_blocks
        to_remove = first_two_blocks
        if to_remove in existing:
            # print "in existing", "to_remove", to_remove, "existing", existing
            existing.remove(to_remove)
        self.write_list(existing)
        # print "final" , existing
        return existing

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
        global g

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
            # if express:
            options = ["express", "stopping"]
            #     default = "express"
            # else:
            #     options = ["stopping", "express"]
            #     default = "stopping"
            title = "title"
            result = self.od.ListOptions(list_items, msg, options)
            if self.od.CLOSED_OPTION == False:
                my_list = result[0]
                option = result[1]
                if self.logLevel > 0: print "option= " ,option, " list = ",my_list
                train_to_move = str(my_list)
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
                                if self.logLevel > 1: print("b")
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

        #write list to file
    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "blockDirections"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator
    def write_list(self, a_list):
        # store list in binary file so 'wb' mode
        file = self.directory() + "blockDirections.txt"
        #print "block_info" , a_list
        #print "file"  +file
        with open(file, 'wb') as fp:
            for items in a_list:
                i = 0
                for item in items:
                    fp.write('%s' %item)
                    if i == 0: fp.write(",")
                    i+=1
                fp.write('\n')
                #fp.write('\n'.join(item))
                #fp.write(items)

    # Read list to memory
    def read_list(self):
        # for reading also binary mode is important
        file = self.directory() + "blockDirections.txt"
        n_list = []
        try:
            with open(file, 'rb') as fp:
                for line in fp:
                    x = line[:-1]
                    #print x
                    y = x.split(",")
                    #print "y" , y
                    n_list.append(y)
            return n_list
        except:
            return ["",""]

# End of class StopMaster

