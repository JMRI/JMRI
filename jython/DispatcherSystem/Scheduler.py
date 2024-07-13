# Script to automatically Schedule trains
#
# Author: Bill Fitch, copyright 2020
# Part of the JMRI distribution

# Description:
# We scan the operations:trains table and get a list of the trains that need to be scheduled
# in SchedulerMaster init we set up a listener which triggers every minute
# in the appropriate minute in the listener we append the train to the trains_to_be_scheduled
# in SchedulerMaster handle we process trains_to_be_scheduled by running run_trains(trains_to_be_scheduled)
# run_trains sets up a thread which moves the train, and removed the train from the trains_to_be_scheduled list

from javax.swing import JFrame, JPanel, JButton, BoxLayout, Box, JComponent
from java.awt import Color, Font
from java.awt import Dimension
from javax.swing import SwingWorker, SwingUtilities
import os
import copy
import jmri
import java
import time
from org.python.core.util import StringUtil
from threading import Thread


CreateSchedulerPanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/SchedulerPanel.py')
execfile(CreateSchedulerPanel)

CreateRoutesPanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/RoutesPanel.py')
execfile(CreateRoutesPanel)

CreateEditRoutePanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/EditRoutePanel.py')
execfile(CreateEditRoutePanel)

CreateEditRoutePanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Timetable.py')
execfile(CreateEditRoutePanel)


# fast_clock_rate = 12

trains_to_be_scheduled = []
run_train_dict = {}
scheduled = {}
RunTrain_instance = {}
tListener = None

class SchedulerMaster(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self):
        global scheduling_in_operation_gbl
        # print "class SchedulerMaster(jmri.jmrit.automat.AbstractAutomaton):"
        self.logLevel = 0
        self.frame = None
        self.f = None
        scheduling_in_operation_gbl = False
        # print "scheduling_in_operation_gbl 5", scheduling_in_operation_gbl

    def exit(self):       # called explicitly when scheduler thread is killed to stop multiple frames being visible
        try:
            self.frame.setVisible(False)
            self.f.setVisible(False)
            if "timetable_frame_gbl" in globals():
                timetable_frame_gbl.setVisible(False)
        except:
            pass

    def setup(self):
        global schedule_trains_hourly
        global schedule_trains_glb
        global scheduling_in_operation_gbl
        # schedule trains initially
        # schedule_trains_glb = False
        scheduling_in_operation_gbl = "True"
        # print "scheduling_in_operation_gbl 6 start setup", scheduling_in_operation_gbl
        self.ToggleSchedulingtrains_action(None)      # Toggle schedule_trains_glb
        # print "scheduling_in_operation_gbl 6a", scheduling_in_operation_gbl

        if 'schedule_trains_hourly' not in globals():
            schedule_trains_hourly = False

        #run before setting up Schedule Master
        if self.logLevel > 0: print "starting SchedulerMaster setup"

        self.scheduler_master_sensor = sensors.getSensor("startSchedulerSensor")
        self.scheduler_view_scheduled_trains = sensors.getSensor("viewScheduledSensor")
        self.scheduler_edit_routes = sensors.getSensor("editRoutesSensor")
        self.scheduler_show_clock_sensor = sensors.getSensor("showClockSensor")
        self.timetable_sensor = sensors.getSensor("timetableSensor")
        self.departure_time_sensor = sensors.getSensor("departureTimeSensor")
        self.help_sensor = sensors.getSensor("helpSensor")


        if self.logLevel > 0: print "finished SchedulerMaster setup"
        if self.logLevel > 0: print "returned from setup"
        # print "scheduling_in_operation_gbl 7 end setup", scheduling_in_operation_gbl
        return True

    def init(self):
        global run_timetable_gbl
        # print "scheduling_in_operation_gbl 7 - init", scheduling_in_operation_gbl
        if "run_timetable_gbl" not in globals():
            run_timetable_gbl = False
        self.train_scheduler_setup = False
        # print "scheduling_in_operation_gbl 7 - init end", scheduling_in_operation_gbl
        if self.logLevel > 0: print "returned from init"

    showing_clock = False
    showing_trains = False
    def handle(self):
        # print "start handle 0"
        global schedule_trains_hourly
        global schedule_trains_glb
        global run_timetable_gbl
        global station_name_gbl
        global timetable_triggered_gbl
        if self.logLevel > 0: print "start handle"
        # print "start handle"
        # print "scheduling_in_operation_gbl 7 handle", scheduling_in_operation_gbl
        self.button_sensors_to_watch = [self.scheduler_master_sensor, self.scheduler_view_scheduled_trains, \
                                   self.scheduler_edit_routes, \
                                   self.scheduler_show_clock_sensor, self.timetable_sensor, \
                                   self.departure_time_sensor, self.help_sensor]
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)

        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
        # print "Z"
        if self.departure_time_sensor.getKnownState() == ACTIVE:
            self.set_departure_trains()
            # print "ZZ"
            self.departure_time_sensor.setKnownState(INACTIVE)
        # print "Y"
        if self.timetable_sensor.getKnownState() == ACTIVE:
            title = ""
            if self.logLevel > 0: print "station list", station_list
            if "station_name_gbl" not in globals():
                station_name_gbl = ""
            # print "b"
            TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
            train_list = TrainManager.getTrainsByTimeList()
            my_scheduled_route_list = [train.getRoute() for train in train_list]
            # print "my_scheduled_route_list", my_scheduled_route_list
            # print "c"
            RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
            station_list = []
            # print "d"
            station_list = []
            for route in my_scheduled_route_list:
                route_locations_list = route.getLocationsBySequenceList()
                # print "route", route.getName(), "route_locations_list", route_locations_list
                station_list1 = [str(route_location.getName()) for route_location in route_locations_list \
                                if ".py" not in route_location.getName()]
                # print "e" , "station_list1", station_list1
                for x in station_list1:
                    if x not in station_list:
                        station_list.append(x)
                station_list.sort()
                # print "station_list", station_list, type(station_list)

            # LocationsManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
            # locations_list = LocationsManager.getLocationsByNameList()
            # station_list = [str(location.getName()) for location in locations_list \
            #                 if ".py" not in location.getName()]
            msg = "timetables can be shown locally on this computer or\n" + \
                  "on remote computers/tablets communicating by mqtt\n\n" + \
                   "scheduler needs to be on and clock running"
            opt1 = "turn timetable off"
            opt2 = "select station for local timetable"
            opt3 = "select station for mqtt timetable"
            if 'timetable_triggered_gbl' not in globals():
                reply = OptionDialog().customQuestionMessage2str(msg, title, opt2, opt3)
            else:
                reply = OptionDialog().customQuestionMessage3str(msg, title, opt1, opt2, opt3)
            if self.logLevel > 0: print "timetable_sensor active"
            if reply == JOptionPane.CANCEL_OPTION:
                pass
            elif reply == opt1:
                run_timetable_gbl = False
                # OptionDialog().displayMessage("turned timetabling off")
                # timetable_gbl = None
            elif reply == opt2:
                # OptionDialog().displayMessage("turned timetabling on\n Select the station you wish to display")
                msg = "show timetable of what station?"
                # print "station list", station_list
                if "station_name_gbl" not in globals():
                    station_name_gbl = ""
                title = "Show timetable of what station?"
                list_items_no_trains = self.get_scheduled_routes("no_train")
                list_items_with_trains = self.get_scheduled_routes("with_train")
                options = ["Cancel", "Show Timetable"]
                result = OptionDialog().ListOptions(station_list, title, options, preferred_size = "default")
                # print "result", result
                station_name_gbl = result[0]
                option = result[1]
                if option == "Cancel":
                    run_timetable_gbl = False
                    self.timetable_sensor.setKnownState(INACTIVE)
                else:
                    # station_name_gbl = modifiableJComboBox(station_list,msg, station_name_gbl).return_val()
                    # print "station_name_gbl", station_name_gbl
                    # if 'run_timetable_gbl' not in globals():
                    #     OptionDialog().displayMessage("You need to schedule trains before the timetable appears")
                    run_timetable_gbl = True
                    # print "run_timetable_gbl set", run_timetable_gbl
                    if not self.conditions_for_timetable_to_show_are_met():
                        # print "You need to schedule trains before the timetable appears"
                        OptionDialog().displayMessage("You need to schedule trains before the timetable appears")
            elif reply == opt3:
                # msg = "show timetable of what station?"
                # # print "station list", station_list
                # if "station_name_gbl" not in globals():
                #     station_name_gbl = ""
                # station_name_mqtt = modifiableJComboBox(station_list,msg, station_name_gbl).return_val()

                title = "Show timetable of what station?"
                options = ["Cancel", "Generate Timetable"]
                od = OptionDialog()
                result = od.ListOptions(station_list, title, options, preferred_size = "default")
                # print "result", result
                station_name_mqtt = result[0]
                option = result[1]
                if option == "Cancel" or od.CLOSED_OPTION == True:
                    run_timetable_gbl = False
                    self.timetable_sensor.setKnownState(INACTIVE)
                else:
                    station_name_mqtt = station_name_mqtt

                    title = "Display Train Operator Emblem?"
                    emblem_list = ["GB (British Rail)", "Germany (DB)", "No Emblem"]
                    options = ["Cancel", "Generate Timetable"]
                    result = od.ListOptions(emblem_list, title, options, preferred_size = "default")
                    # print "result", result
                    train_operator_emblem = result[0]
                    option1 = result[1]
                    if option1 == "Cancel" or od.CLOSED_OPTION == True:
                        run_timetable_gbl = False
                        self.timetable_sensor.setKnownState(INACTIVE)
                    else:

                        self.generate_node_red_code(station_name_mqtt, train_operator_emblem)
                        # file = self.directory() + "train_operator_emblem.txt"
                        self.write_list2([train_operator_emblem])

            self.timetable_sensor.setKnownState(INACTIVE)
        # print "X"
        if self.scheduler_master_sensor.getKnownState() == ACTIVE:   # pause processing if we turn the sensor off

            if self.logLevel > 0:  print("checking valid operations trains")
            if self.logLevel > 0: print "train_scheduler_setup",self.train_scheduler_setup

            # # check whether can/want to schedule trains every hour
            # schedule_trains_hourly = self.check_whether_schedule_trains_every_hour()

            # print "scheduling_in_operation_gbl 8", scheduling_in_operation_gbl
            self.set_default_scheduling_values()
            # print "scheduling_in_operation_gbl 70", scheduling_in_operation_gbl
            self.set_period_trains_will_run_frame()
            # print "scheduling_in_operation_gbl 71", scheduling_in_operation_gbl
            self.show_analog_clock()      # show the analog clock

            # print "!"
            # set time to midnight
            if self.logLevel > 0: print "set minute time listener"
            self.setup_minute_time_listener_to_schedule_trains()   # this
            # print "!!"
            # timebase.setRun(False)
            # print "!!!"
            self.train_scheduler_setup = True
            # print "!!!!"

            self.scheduler_master_sensor.setKnownState(INACTIVE)
            # print "!!!!!"
        # print "W"
        if self.scheduler_view_scheduled_trains.getKnownState() == ACTIVE:
            self.show_operations_trains()
            self.scheduler_view_scheduled_trains.setKnownState(INACTIVE)
        # print "A"
        if self.scheduler_edit_routes.getKnownState() == ACTIVE:
            self.show_routes()
            self.scheduler_edit_routes.setKnownState(INACTIVE)
        # print "C"
        if self.scheduler_show_clock_sensor.getKnownState() == ACTIVE:
            self.show_analog_clock()
            self.scheduler_show_clock_sensor.setKnownState(INACTIVE)
        # print "D"
        if self.help_sensor.getKnownState() == ACTIVE:
            self.display_help()
            self.help_sensor.setKnownState(INACTIVE)
        # print "E"
        self.waitMsec(500)
        if self.logLevel > 0: print "end handle"
        # print "scheduling_in_operation_gbl 7 handle end", scheduling_in_operation_gbl
        # print "F"
        return True

    def conditions_for_timetable_to_show_are_met(self):
        global scheduling_in_operation_gbl
        global timebase
        if "timebase" in globals():
            if timebase.getRun() == False:
                # print "clock is stopped, must start before timetable can run"
                OptionDialog().displayMessage("Clock is stopped, must start before timetable can run")
                return False
            if scheduling_in_operation_gbl == "False":
                # print "scheduling must be operational for trains to run to timetable"
                OptionDialog().displayMessage("Scheduling must be operational for trains to run to timetable")
                return False
        return True

    def generate_node_red_code(self, station_name, train_operator_emblem):

        node_red_template_path = \
            jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/timetable_template/node_red_flow.json"')
        # read the file into a string
        #check if file is present
        if os.path.isfile(node_red_template_path):
            #open text file in read mode
            text_file = open(node_red_template_path, "r")

            #read whole file to a string
            data = text_file.read()

            #close file
            text_file.close()

            # change the file to work with station_name
            # websocket names cannot have spaces in them
            # so where we need to replace spaces we have My_Station$ instaed of MyStation
            new_data = data.replace('My_Station$', station_name.replace(" ",""))\
                .replace("My_Station", station_name)\
                .replace("My_Emblem", train_operator_emblem)



            # store the modified file to a file in the user directory
            new_node_red_template_directory = jmri.util.FileUtil.getExternalFilename('preference:dispatcher/mqtt_timetables/')

            #create the folder if it does not exist
            if not os.path.exists(new_node_red_template_directory):
                os.makedirs(new_node_red_template_directory)

            file_path = new_node_red_template_directory + "/" + station_name + ".json"

            f = open(file_path, "w")
            f.write(new_data)
            f.close()
        else:
            OptionDialog().displayMessage(file_path + "does not exist, reinstall latest JMRI")

        msg = "node red file for station is in " + file_path + "\n" + \
            "import the file into a node_red instance on computer on same network \n" + \
            "and edit as illustrated in the help, and open indicated web page on tablet/laptop \n" +\
            "e.g. http://localhost:1880/" + station_name + ", where localhost should be replaced by network address \n" + \
            "of computer hosting node_red instance\n\n" + \
            "ALSO ensure an MQTT Connection is set up in preferences, as detailed in help."
        OptionDialog().displayMessage(msg)

    def set_departure_trains(self):
        # allow running of scheduled routes to set journey times and wait times,
        # which allow durations and departure times to be set up

        global wait_time_gbl
        global scheduled
        global scheduling_margin_gbl
        global set_departure_trains_gbl
        global CreateAndShowGUI5_glb

        set_departure_trains_gbl = True
        self.set_default_scheduling_values()
        msg = "set departure times"
        opt1 = "set wait time in stations"
        opt2 = "run train on route to set journey times"
        opt3 = "set departure times"
        reply = OptionDialog().customQuestionMessage3str(msg, "", opt1, opt3, opt2)
        if reply == opt1:
            memory = memories.getMemory("IM:" + "DS_wait_time")
            # print "memory", type(memory)
            if memory is None or memory.getValue() == "":
                memory = memories.provideMemory("IM:" + "DS_wait_time")
                memory.setValue(3)

            title = "wait time at station"
            msg = "set wait time at all stations"
            default_value = str(memory.getValue())

            wait_time = OptionDialog().input(msg, title, default_value)
            # memory = memories.getMemory("IM:" + "DS_wait_time")
            if memory is not None:
                memory.setValue(wait_time)
            # print "wait_time", wait_time
            return
        elif reply == opt2:
            title = "Run Route and Record Journey Times"
            list_items_no_trains = self.get_scheduled_routes("no_train")
            list_items_with_trains = self.get_scheduled_routes("with_train")
            if list_items_no_trains == []:
                OptionDialog().displayMessage("Can only record journey times for scheduled trains.\nThere are no scheduled trains")
                return
            # options = ["Cancel", "Run Route", "show all routes/scheduled routes"]
            options = ["Cancel", "Run Route"]
            reply1 = OptionDialog().ListOptions(list_items_no_trains, title, options, preferred_size = "default")
            # print "reply1", reply1
            my_list = reply1[0]
            option = reply1[1]
            # print "list", my_list, "option", option
            route_name = str(my_list)
            # print "route", route_name

            # print "A"
            # print "train1 ", train
            # train = list[1]
            # print "train", train
            # print "train", train.getName()
            option = str(option)
            if OptionDialog().CLOSED_OPTION == True or option == "Cancel":
                # print "cancelling"
                return
            elif option == "Run Route":
                train = [trn for [rte, trn] in list_items_with_trains if rte == route_name][0]
                # print "running route", route_name
                set_departure_times = True
                param_scheduled_start = "00:00"
                journey_time_row_displayed = True
                if "CreateAndShowGUI5_glb" in globals():
                    if CreateAndShowGUI5_glb != None:
                        CreateAndShowGUI5_glb.frame.dispose()
                CreateAndShowGUI5_glb = CreateAndShowGUI5(None, route_name, param_scheduled_start, journey_time_row_displayed)
                title = "Run Train"
                msg = "Last time to cancel"
                opt1 = "Cancel"
                opt2 = "Run Train"
                reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
                if reply == JOptionPane.CANCEL_OPTION or reply == opt1:
                    CreateAndShowGUI5_glb.frame.dispose()
                    return
                OptionDialog().displayMessageNonModal("Run Train along route " + str(route_name) + " now","<html>Check train is in required station<br>Then click to run route")
                # print "Ended non modal, wait for non modal"
                self.waitForNonModal()
                # print "finished wait for non modal"

                if "stopping" in train.getDescription():
                    # print "running train %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", train.getDescription(), train.getName()
                    running_train = RunTrain(train, g.g_stopping, set_departure_times)
                else:
                    # print "running train %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", train.getDescription(), train.getName()
                    running_train = RunTrain(train, g.g_express, set_departure_times)

                running_train.setName("setting_journey_time_" + train.getName())
                # print "starting running_train"
                running_train.handle()
                # scheduled[train] = True
                return

        elif reply == opt3:
            title = "Select Route to set Departure Times"
            list_items_no_trains = self.get_scheduled_routes("no_train")
            list_items_with_trains = self.get_scheduled_routes("with_train")
            if list_items_no_trains == []:
                OptionDialog().displayMessage("Can only record journey times for scheduled trains.\nThere are no scheduled trains")
                return
            options = ["Cancel", "Set Departure Times"]
            reply1 = OptionDialog().ListOptions(list_items_no_trains, title, options, preferred_size = "default")
            my_list = reply1[0]
            option = reply1[1]
            # print "list", list, "option", option
            route_name = str(my_list)
            # print "route", route_name
            train = [trn for [rte, trn] in list_items_with_trains if rte == route_name][0]
            # print "train ", train
            # train = list[1]
            # print "train", train
            # print "train", train.getName()
            option = str(option)
            if OptionDialog().CLOSED_OPTION == True or option == "Cancel":
                # print "cancelling"
                return
            else:
                param_scheduled_start = "00:00"
                journey_time_row_displayed = True
                # print "a"
                if "CreateAndShowGUI5_glb" not in globals():
                    CreateAndShowGUI5_glb = CreateAndShowGUI5(None, route_name, param_scheduled_start, journey_time_row_displayed)
                else:
                    CreateAndShowGUI5_glb.frame.dispose()
                    CreateAndShowGUI5_glb = CreateAndShowGUI5(None, route_name, param_scheduled_start, journey_time_row_displayed)
                    # print "c", CreateAndShowGUI5_glb
            # print "%%%%%type%%%%%%%%%", type(CreateAndShowGUI5_glb)
            # CreateAndShowGUI5_glb.frame.setVisible(True)

    def waitForNonModal(self):

        Jdialog_closed = sensors.getSensor("Jdialog_closed")
        Jdialog_closed.setKnownState(INACTIVE)
        btn = [Jdialog_closed]
        btn_to_watch = java.util.Arrays.asList(btn)
        # print "waiting for button" , Jdialog_closed.getKnownState()
        self.waitSensorState(btn_to_watch, ACTIVE)

    def get_scheduled_routes(self, option):
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        if option == "with_train":
            my_list = [[train.getRoute().getName(), train] for train in train_list]
        else:
            my_list = [train.getRoute().getName() for train in train_list]
        return sorted(my_list)

    def start_and_end_time_scheduling(self):
        if self.logLevel > 0: print "start_and_end_time_scheduling"
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        start_time_hour = 23
        end_time_hour = 0
        for train in train_list:
            train_hour = int(train.getDepartureTimeHour())
            if train_hour < start_time_hour:
                start_time_hour = train_hour
            else:
                pass
            if train_hour > end_time_hour:
                end_time_hour = train_hour
            else:
                pass
        # print "[start_time_hour, end_time_hour]", [start_time_hour, end_time_hour]
        if self.logLevel > 0: print "start_and_end_time_scheduling end"
        return [start_time_hour, end_time_hour]

    # def all_trains_in_schedule_within_one_hour_period(self):
    #     [start_time_hour, end_time_hour] = self.start_and_end_time_scheduling()
    #     # print "end_time_hour - start_time_hour < 1", end_time_hour - start_time_hour < 1
    #     if end_time_hour - start_time_hour < 1:
    #         if self.logLevel > 0: print "all_trains_in_schedule_within_one_hour_period"
    #         return True
    #     else:
    #         if self.logLevel > 0: print "all_trains_in_schedule more than one_hour_period"
    #         return False

    def set_period_trains_will_run_frame(self):
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl
        if self.frame == None:
            # print "frame is None"
            self.frame = jmri.util.JmriJFrame('Schedule Trains Hourly');

            panel = JPanel()
            panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
            self.frame.add(panel)

            row0 = JPanel()
            row0.setLayout(BoxLayout(row0, BoxLayout.X_AXIS))
            txt = JTextField(140)
            txt.setMaximumSize( txt.getPreferredSize() );
            txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                txt.getBorder()));
            label_panel_location = JLabel()
            # btnpanelLocation = JButton("Set Panel Location", actionPerformed = btnpanelLocation_action)
            btnpanelLocation = JButton("Set Panel Location")
            row0.add(Box.createVerticalGlue())
            row0.add(Box.createRigidArea(Dimension(20, 0)))
            row0.add(btnpanelLocation)
            row0.add(Box.createRigidArea(Dimension(20, 0)))
            row0.add(label_panel_location)
            row0.add(Box.createRigidArea(Dimension(20, 0)))


            rowTitle_22 = JPanel()
            rowTitle_22.setLayout(BoxLayout(rowTitle_22, BoxLayout.X_AXIS))
            rowTitle_23 = JPanel()
            rowTitle_23.setLayout(BoxLayout(rowTitle_23, BoxLayout.X_AXIS))
            rowStage1Title_1 = JLabel("Sets Up Syatem to run hourly trains ")
            rowStage1Title_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            # get_start_filename()
            # get_backup_filename()
            # rowStage1Title_1 = JLabel("    Modifies: " + start_filename + "  Creates backup: " + backup_filename)
            # rowStage1Title_1.add(Box.createHorizontalGlue());
            # rowStage1Title_1.setAlignmentX(rowStage1Title_1.LEFT_ALIGNMENT)
            # rowStage1Title_2 = JLabel("Run Scheduled Trains")
            # rowStage1Title_3 = JLabel("    Read Help to see how system works")     #start_filename + "_icons"

            rowTitle_22.add(Box.createVerticalGlue())
            rowTitle_22.add(Box.createRigidArea(Dimension(20, 0)))
            rowTitle_22.add(rowStage1Title_1)
            # rowTitle_22.add(Box.createRigidArea(Dimension(20, 0)))
            # rowTitle_22.add(rowStage1Title_2)

            # rowTitle_22.add(Box.createVerticalGlue())
            # rowTitle_22.add(Box.createRigidArea(Dimension(20, 0)))
            # rowTitle_22.add(JLabel(""))
            # rowTitle_23.add(rowStage1Title_3)

            rowTitle_33 = JPanel()
            rowTitle_33.setLayout(BoxLayout(rowTitle_33, BoxLayout.X_AXIS))
            # rowStage1Title_1 = JLabel("Sets Up Syatem to run hourly trains ")
            # rowStage1Title_1.setFont(rowTitle_33.getFont().deriveFont(Font.BOLD, 13));
            rowTitle_33.add(Box.createVerticalGlue())
            rowTitle_33.add(Box.createRigidArea(Dimension(30, 0)))
            rowTitle_33.add(rowStage1Title_1)

            #buttons

            # Separators
            rowStage2Separator = JPanel()
            rowStage2Separator.setLayout(BoxLayout(rowStage2Separator, BoxLayout.X_AXIS))
            rowStage2Separator_1 = JLabel("*******************************************************************")
            rowStage2Separator_1.add(Box.createHorizontalGlue());
            rowStage2Separator_1.setAlignmentX(rowStage2Separator_1.LEFT_ALIGNMENT)
            rowStage2Separator_2 = JLabel("")

            rowStage2Separator.add(Box.createVerticalGlue())
            rowStage2Separator.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage2Separator.add(rowStage2Separator_1)
            rowStage2Separator.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage2Separator.add(rowStage2Separator_2)

            rowStage1Separator = JPanel()
            rowStage1Separator.setLayout(BoxLayout(rowStage1Separator, BoxLayout.X_AXIS))
            rowStage1Separator_1 = JLabel("*******************************************************************")
            rowStage1Separator_1.add(Box.createHorizontalGlue());
            rowStage1Separator_1.setAlignmentX(rowStage1Separator_1.LEFT_ALIGNMENT)
            rowStage1Separator_2 = JLabel("")

            rowStage1Separator.add(Box.createVerticalGlue())
            rowStage1Separator.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage1Separator.add(rowStage1Separator_1)
            rowStage1Separator.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage1Separator.add(rowStage1Separator_2)

            rowStage3Separator = JPanel()
            rowStage3Separator.setLayout(BoxLayout(rowStage3Separator, BoxLayout.X_AXIS))
            rowStage3Separator_1 = JLabel("*******************************************************************")
            rowStage3Separator_1.add(Box.createHorizontalGlue());
            rowStage3Separator_1.setAlignmentX(rowStage3Separator_1.LEFT_ALIGNMENT)
            rowStage3Separator_3 = JLabel("")

            rowStage3Separator.add(Box.createVerticalGlue())
            rowStage3Separator.add(Box.createRigidArea(Dimension(30, 0)))
            rowStage3Separator.add(rowStage3Separator_1)
            rowStage3Separator.add(Box.createRigidArea(Dimension(30, 0)))
            rowStage3Separator.add(rowStage3Separator_3)

            # buttons

            rowStage1Button = JPanel()
            rowStage1Button.setLayout(BoxLayout(rowStage1Button, BoxLayout.X_AXIS))

            global rowAStage1Button_1,  rowBStage1Button_1, rowCStage1Button_1, rowDStage1Button_1, rowEStage1Button_1, rowFStage1Button_1
            rowAStage1Button = JPanel()
            rowAStage1Button.setLayout(BoxLayout(rowAStage1Button, BoxLayout.X_AXIS))

            rowBStage1Button = JPanel()
            rowBStage1Button.setLayout(BoxLayout(rowBStage1Button, BoxLayout.X_AXIS))

            rowCStage1Button = JPanel()
            rowCStage1Button.setLayout(BoxLayout(rowCStage1Button, BoxLayout.X_AXIS))

            rowDStage1Button = JPanel()
            rowDStage1Button.setLayout(BoxLayout(rowDStage1Button, BoxLayout.X_AXIS))

            rowEStage1Button = JPanel()
            rowEStage1Button.setLayout(BoxLayout(rowEStage1Button, BoxLayout.X_AXIS))

            rowFStage1Button = JPanel()
            rowFStage1Button.setLayout(BoxLayout(rowFStage1Button, BoxLayout.X_AXIS))

            rowrowStage1Button_1 = JLabel("Change parameters to run trains")
            rowrowStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            rowrowStage1Button_1.add(Box.createHorizontalGlue());
            rowrowStage1Button_1.setAlignmentX(rowrowStage1Button_1.LEFT_ALIGNMENT)

            stringToDisplay = "start hour: " + str(start_hour_gbl)
            rowAStage1Button_1 = JLabel(stringToDisplay)
            rowAStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            rowAStage1Button_1.add(Box.createHorizontalGlue());
            rowAStage1Button_1.setAlignmentX(rowAStage1Button_1.LEFT_ALIGNMENT)

            stringToDisplay = "end hour: " + str(end_hour_gbl)
            rowBStage1Button_1 = JLabel(stringToDisplay)
            rowBStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            rowBStage1Button_1.add(Box.createHorizontalGlue());
            rowBStage1Button_1.setAlignmentX(rowBStage1Button_1.LEFT_ALIGNMENT)

            stringToDisplay = "fast clock (when scheduling trains): x " + str(fast_clock_rate)
            rowCStage1Button_1 = JLabel(stringToDisplay)
            rowCStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            rowCStage1Button_1.add(Box.createHorizontalGlue());
            rowCStage1Button_1.setAlignmentX(rowCStage1Button_1.LEFT_ALIGNMENT)

            stringToDisplay = "fast clock (outside running times): x " + str(speed_not_operational_gbl)
            rowDStage1Button_1 = JLabel(stringToDisplay)
            rowDStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            rowDStage1Button_1.add(Box.createHorizontalGlue());
            rowDStage1Button_1.setAlignmentX(rowDStage1Button_1.LEFT_ALIGNMENT)

            stringToDisplay = "scheduling margin: " + str(scheduling_margin_gbl) + " fast mins"
            rowEStage1Button_1 = JLabel(stringToDisplay)
            rowEStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            rowEStage1Button_1.add(Box.createHorizontalGlue());
            rowEStage1Button_1.setAlignmentX(rowEStage1Button_1.LEFT_ALIGNMENT)
            # print "scheduling_in_operation_gbl 7", scheduling_in_operation_gbl
            stringToDisplay = "scheduling in operation: " + str(scheduling_in_operation_gbl)
            rowFStage1Button_1 = JLabel(stringToDisplay)
            rowFStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            rowFStage1Button_1.add(Box.createHorizontalGlue());
            rowFStage1Button_1.setAlignmentX(rowFStage1Button_1.LEFT_ALIGNMENT)

            rowStage1Button_1 = JButton("Initialisation", actionPerformed = self.CheckHourlyParameters_action)

            stage1Button = rowStage1Button_1



            rowStage2Button = JPanel()
            rowStage2Button.setLayout(BoxLayout(rowStage2Button, BoxLayout.X_AXIS))
            rowrowStage2Button_2 = JLabel("Set Clock to Session Start")
            rowrowStage2Button_2.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));

            rowrowStage2Button_2.add(Box.createHorizontalGlue());
            rowrowStage2Button_2.setAlignmentX(rowrowStage2Button_2.LEFT_ALIGNMENT)
            rowStage2Button_2 = JButton("Set Time  ", actionPerformed = self.SetTime_action)
            stage2Button = rowStage2Button_2

            rowStage3Button = JPanel()
            rowStage3Button.setLayout(BoxLayout(rowStage3Button, BoxLayout.X_AXIS))
            rowrowStage3Button_3 = JLabel("Stop/Start Scheduling Trains")
            rowrowStage3Button_3.setFont(rowTitle_33.getFont().deriveFont(Font.BOLD, 13));

            rowrowStage3Button_3.add(Box.createHorizontalGlue());
            rowrowStage3Button_3.setAlignmentX(rowrowStage3Button_3.LEFT_ALIGNMENT)
            rowStage3Button_3 = JButton("Schedule ", actionPerformed = self.ToggleSchedulingtrains_action)
            stage3Button = rowStage3Button_3

            rowStage4Button = JPanel()
            rowStage4Button.setLayout(BoxLayout(rowStage4Button, BoxLayout.X_AXIS))
            rowrowStage4Button_4 = JLabel("Stop/Start Clock")
            rowrowStage4Button_4.setFont(rowTitle_33.getFont().deriveFont(Font.BOLD, 13));

            rowrowStage4Button_4.add(Box.createHorizontalGlue());
            rowrowStage4Button_4.setAlignmentX(rowrowStage4Button_4.LEFT_ALIGNMENT)
            rowStage4Button_4 = JButton("Stop/Start", actionPerformed = self.StopStartClock_action)
            stage4Button = rowStage4Button_4


            rowStage1Button.add(Box.createVerticalGlue())
            rowStage1Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage1Button.add(rowStage1Button_1)
            rowStage1Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage1Button.add(rowrowStage1Button_1)
            # rowStage1Button.add(Box.createVerticalGlue())
            # rowStage1Button.add(rowAStage1Button_1)

            rowAStage1Button.add(Box.createVerticalGlue())
            rowAStage1Button.add(Box.createRigidArea(Dimension(120, 0)))
            rowAStage1Button.add(rowAStage1Button_1)

            rowBStage1Button.add(Box.createVerticalGlue())
            rowBStage1Button.add(Box.createRigidArea(Dimension(120, 0)))
            rowBStage1Button.add(rowBStage1Button_1)

            rowCStage1Button.add(Box.createVerticalGlue())
            rowCStage1Button.add(Box.createRigidArea(Dimension(120, 0)))
            rowCStage1Button.add(rowCStage1Button_1)

            rowDStage1Button.add(Box.createVerticalGlue())
            rowDStage1Button.add(Box.createRigidArea(Dimension(120, 0)))
            rowDStage1Button.add(rowDStage1Button_1)

            rowEStage1Button.add(Box.createVerticalGlue())
            rowEStage1Button.add(Box.createRigidArea(Dimension(120, 0)))
            rowEStage1Button.add(rowEStage1Button_1)

            rowFStage1Button.add(Box.createVerticalGlue())
            rowFStage1Button.add(Box.createRigidArea(Dimension(120, 0)))
            rowFStage1Button.add(rowFStage1Button_1)

            # rowDStage1Button.add(Box.createRigidArea(Dimension(20, 0)))
            # rowStage1Button.add(rowDStage1Button_1)



            rowStage2Button.add(Box.createVerticalGlue())
            rowStage2Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage2Button.add(rowStage2Button_2)
            rowStage2Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage2Button.add(rowrowStage2Button_2)

            rowStage3Button.add(Box.createVerticalGlue())
            rowStage3Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage3Button.add(rowStage3Button_3)
            rowStage3Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage3Button.add(rowrowStage3Button_3)

            rowStage4Button.add(Box.createVerticalGlue())
            rowStage4Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage4Button.add(rowStage4Button_4)
            rowStage4Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage4Button.add(rowrowStage4Button_4)

            #Title
            # panel.add(self.leftJustify(rowTitle_2))
            panel.add(self.leftJustify(rowTitle_22))
            # panel.add(self.leftJustify(rowTitle_23))
            # panel.add(self.leftJustify(row_Title_3))
            panel.add(self.leftJustify(rowStage1Separator))

            #stage1
            panel.add(self.leftJustify(rowStage1Button))
            panel.add(self.leftJustify(rowAStage1Button))
            panel.add(self.leftJustify(rowBStage1Button))
            panel.add(self.leftJustify(rowCStage1Button))
            panel.add(self.leftJustify(rowDStage1Button))
            panel.add(self.leftJustify(rowEStage1Button))
            panel.add(self.leftJustify(rowStage1Separator))

            #stage2
            panel.add(self.leftJustify(rowStage2Button))
            # panel.add(self.leftJustify(rowStage2Separator))

            panel.add(self.leftJustify(rowStage3Button))
            panel.add(self.leftJustify(rowFStage1Button))

            panel.add(self.leftJustify(rowStage4Button))
            panel.add(self.leftJustify(rowStage3Separator))

        self.frame.pack()
        self.frame.setVisible(True)
        self.frame.setSize(430, 300)
        self.frame.setLocation(10,10)

    def leftJustify(self, panel):
        b = Box.createHorizontalBox()
        b.add( panel )
        b.add( Box.createHorizontalGlue() )
        # (Note that you could throw a lot more components
        # and struts and glue in here.)
        return b

        # self.close_this_panel(event)
        #
        # self.run_trains()

    def close_this_panel(self, event):
        if self.logLevel > 0: print "closing panel"
        comp = event.getSource()
        win = SwingUtilities.getWindowAncestor(comp);
        win.dispose();
    def SetTime_action(self, event):
        global fast_clock_running_at_operational_speed
        global timebase

        # timebase needs to be running to set rate and time
        timebase.setRun(True)
        timebase.userSetRate(float(speed_not_operational_gbl))

        # set clock to beginning of session
        self.set_timebase_start_hour(int(start_hour_gbl)-1, 55)
        # self.set_timebase_start_hour(0, 0)

        # set timebase to stop
        timebase.setRun(False)

    def ToggleSchedulingtrains_action(self, event):
        global schedule_trains_glb
        global scheduling_in_operation_gbl
        global rowFStage1Button_1

        # schedule_trains_glb = False
        if self.logLevel > 0: print "ToggleSchedulingtrains_action", "scheduling_in_operation_gbl", scheduling_in_operation_gbl
        #stop Scheduler
        self.stop_schedule_trains_threads()
        if 'scheduling_in_operation_gbl' not in globals():
            scheduling_in_operation_gbl = "True"
            # print "scheduling_in_operation_gbl 8", scheduling_in_operation_gbl
        if scheduling_in_operation_gbl == "False":
            self.setup_values_for_running()
            scheduling_in_operation_gbl = "True"
        else:
            scheduling_in_operation_gbl = "False"
        # print "scheduling_in_operation_gbl 0", scheduling_in_operation_gbl

        stringToDisplay = "scheduling in operation: " + str(scheduling_in_operation_gbl)

        if 'rowFStage1Button_1' in globals():
            rowFStage1Button_1.setText(stringToDisplay) # Update the label
        if self.logLevel > 0: print "ToggleSchedulingtrains_action end", "scheduling_in_operation_gbl", scheduling_in_operation_gbl

    def StopStartClock_action(self, event):
        self.swap_timebase_state_run_stop()

    def swap_timebase_state_run_stop(self):
        global timebase
        global fast_clock_rate
        if timebase.getRun() == True:
            timebase.removeMinuteChangeListener(tListener)
            timebase.setRun(False)
            # print "fast_clock_rate", fast_clock_rate
        else:
            timebase.addMinuteChangeListener(tListener)
            timebase.setRun(True)
            # print "fast_clock_rate", fast_clock_rate
    def setup_values_for_running(self):

        global instanceList
        global rowFStage1Button_1

        # start scheduler
        if self.logLevel > 0: print "setup values for running"

        schedule_trains_master = ScheduleTrains()
        schedule_trains_master.setName('Schedule Trains Master')

        list_existing_schedule_trains = [iL.getName() for iL in instanceList if iL.getName() == 'Schedule Trains Master']
        # print "list_existing_schedule_trains", list_existing_schedule_trains

        if list_existing_schedule_trains == []:
            # print "appending instance list"
            instanceList.append(schedule_trains_master)
            if schedule_trains_master.setup():
                # print "setting name"
                schedule_trains_master.setName('Schedule Trains Master')
                schedule_trains_master.start()

        scheduling_in_operation_gbl = "False"
        # print "scheduling_in_operation_gbl 2", scheduling_in_operation_gbl
        stringToDisplay = "scheduling in operation: " + str(scheduling_in_operation_gbl)

        if 'rowFStage1Button_1' in globals():
            rowFStage1Button_1.setText(stringToDisplay) # Update the label
        if self.logLevel > 0: print "end setup values for running"

    def stop_schedule_trains_threads(self):
        global instanceList
        #stop all thresds even if there are duplications

        summary = jmri.jmrit.automat.AutomatSummary.instance()
        automatsList = java.util.concurrent.CopyOnWriteArrayList()
        schedule_trains_master = ScheduleTrains()
        # print "schedule_trains_master", schedule_trains_master

        # for x in instanceList:
        #     print "name", x.getName()
        #     print "item", str(x)
        #     if x.getName() == 'Schedule Trains Master':
        #         print "'Schedule Trains Master' in instanceList"
        #     if x ==  schedule_trains_master:
        #         print "schedule_trains_master in instanceList"

        for automat in summary.getAutomats():
            automatsList.add(automat)
        # print "automatsList", automatsList
        for automat in automatsList:
            # print "automat", automat
            if "ScheduleTrains" in str(automat): automat.stop()
            # print automat, "stopped"
        # print "automatsList2", automatsList
        # print "end stop_all_threads"

        instanceList = [iL for iL in instanceList if str(iL.getName()) != 'Schedule Trains Master']

    def set_default_scheduling_values(self):

        # self.show_analog_clock()      # show the analog clock

        # print "set_default_scheduling_hourly_values"

        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl
        # read parameters
        [start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
         scheduling_margin_gbl, scheduling_in_operation_gbl] = self.read_list()
        if self.logLevel > 0: print "read list" , [start_hour_gbl, end_hour_gbl, speed_not_operational_gbl]
        if start_hour_gbl == "":
            start_hour_gbl = "04"
            end_hour_gbl = "22"
            fast_clock_rate = "10"
            speed_not_operational_gbl = "100"
            scheduling_margin_gbl = "3"
            scheduling_in_operation_gbl = "False"
            # print "1scheduling_in_operation_gbl", scheduling_in_operation_gbl

        self.write_list([start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                         scheduling_margin_gbl, scheduling_in_operation_gbl])
        # print "fast_clock_rate in set_default_scheduling_values", fast_clock_rate
    def CheckHourlyParameters_action(self, event):
        global rowAStage1Button_1,  rowBStage1Button_1, rowCStage1Button_1, rowDStage1Button_1, rowEStage1Button_1, rowFStage1Button_1
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl
        # read parameters
        [self.start_hour, self.end_hour, fast_clock_rate, self.speed_not_operational, \
         self.scheduling_margin, self.scheduling_in_operation] = self.read_list()
        if self.logLevel > 0: print "read list" , [self.start_hour, self.end_hour, self.speed_not_operational]
        if self.start_hour == "":
            self.start_hour = "04"
            self.end_hour = "22"
            fast_clock_rate = "10"
            self.speed_not_operational = "100"
            self.scheduling_margin = "10"
            self.scheduling_in_operation = "False"

        # start hour
        title = "Start Hour"
        msg = "input hour at which scheduling will start xx:00"
        default_value = self.start_hour
        repeat = True
        while repeat == True:
            reply = OptionDialog().input(msg, title, default_value)
            if self.is_time_format(reply) or reply.isdigit():
                # OptionDialog().displayMessage("correct format")
                repeat = False
            else:
                repeat = True
                OptionDialog().displayMessage("wrong format must be hh:mm")
        self.start_hour = int(str(reply).split(":")[0])
        if self.logLevel > 0: print "start_hour", self.start_hour

        # end hour
        title = "End Hour"
        msg = "input hour at which scheduling will end xx:00"
        default_value = self.end_hour
        repeat = True
        while repeat == True:
            reply = OptionDialog().input(msg, title, default_value)
            if self.is_time_format(reply) or reply.isdigit():
                # OptionDialog().displayMessage("correct format")
                repeat = False
            else:
                repeat = True
                OptionDialog().displayMessage("wrong format must be hh:mm")
        self.end_hour = int(str(reply).split(":")[0])
        if self.logLevel > 0: print "end_hour", self.end_hour

        title = "Fast Clock Speed"
        msg = "input fast clock speed during schduled hours x10 say"
        default_value = fast_clock_rate
        repeat = True
        repeat = True
        while repeat == True:
            reply = OptionDialog().input(msg, title, default_value)
            reply = reply.replace("x", "")
            if reply.isdigit():
                # OptionDialog().displayMessage("correct format")
                repeat = False
            else:
                repeat = True
                OptionDialog().displayMessage("wrong format must be integer")
        fast_clock_rate = int(str(reply).split(":")[0])
        if self.logLevel > 0: print "end_hour", self.end_hour

        # speed of running clock in non-operational times
        title = "speed_not_operational"
        msg = "input fast clock speed in non-operational times x100 say (max 100)"
        default_value = self.speed_not_operational
        repeat = True
        while repeat == True:
            reply = OptionDialog().input(msg, title, default_value)
            reply = reply.replace("x", "")
            if reply.isdigit():
                # OptionDialog().displayMessage("correct format")
                repeat = False
            else:
                repeat = True
                OptionDialog().displayMessage("wrong format must be integer")
        if int(reply) >100:
            self.speed_not_operational = 100
        else:
            self.speed_not_operational = int(reply)
        if self.logLevel > 0: print "speed_not_operational", self.speed_not_operational

        # speed of running clock in non-operational times
        title = "scheduling_margin: (max 20 fast mins)"
        msg = "input scheduling margin "
        default_value = self.scheduling_margin
        repeat = True
        while repeat == True:
            reply = OptionDialog().input(msg, title, default_value)
            reply = reply.replace("x", "")
            if reply.isdigit():
                # OptionDialog().displayMessage("correct format")
                repeat = False
            else:
                repeat = True
                OptionDialog().displayMessage("wrong format must be integer")
        if int(reply) > 20:
            self.scheduling_margin = 20
        else:
            self.scheduling_margin = int(reply)
        if self.logLevel > 0: print "scheduling_margin", self.scheduling_margin

        scheduling_in_operation_gbl = False

        # # speed of running clock in non-operational times
        # title = "scheduling_in_operation"
        # msg = "input speedup of running clock in non-operational times x100 say (max 100)"
        # default_value = self.scheduling_in_operation
        # repeat = True
        # while repeat == True:
        #     reply = OptionDialog().input(msg, title, default_value)
        #     reply = reply.replace("x", "")
        #     if reply == "False" or reply == "True":
        #         # OptionDialog().displayMessage("correct format")
        #         repeat = False
        #     else:
        #         repeat = True
        #         OptionDialog().displayMessage("wrong format must be integer")
        # if int(reply) >100:
        #     self.scheduling_in_operation = 100
        # else:
        #     self.scheduling_in_operation = bool(reply)
        # if self.logLevel > 0: print "scheduling_in_operation", self.scheduling_in_operation

        items = [str(item) for item in [self.start_hour,self.end_hour, fast_clock_rate, \
                                        self.speed_not_operational,\
                                        self.scheduling_margin, scheduling_in_operation_gbl]]
        if self.logLevel > 0: print "items to write", items
        self.write_list(items)
        # store in globals

        start_hour_gbl = self.start_hour
        end_hour_gbl = self.end_hour
        speed_not_operational_gbl = self.speed_not_operational

        # update the jlabel texts

        stringToDisplay = "start hour: " + str(start_hour_gbl)
        rowAStage1Button_1.setText(stringToDisplay) # Update the label

        stringToDisplay = "end hour: " + str(end_hour_gbl)
        rowBStage1Button_1.setText(stringToDisplay) # Update the label

        stringToDisplay = "fast clock (when scheduling trains): x " + str(fast_clock_rate)
        rowCStage1Button_1.setText(stringToDisplay) # Update the label

        stringToDisplay = "fast clock (outside running times): x " + str(speed_not_operational_gbl)
        rowDStage1Button_1.setText(stringToDisplay) # Update the label

        stringToDisplay = "scheduling margin: " + str(scheduling_margin_gbl) + " fast mins"
        rowEStage1Button_1.setText(stringToDisplay) # Update the label

    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "hourlySchedule"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator

    def write_list(self, a_list):
        # store list in binary file so 'wb' mode
        file = self.directory() + "hourlySchedule.txt"
        if self.logLevel > 0: print "hourlySchedule" , a_list
        if self.logLevel > 0: print "file" , file
        with open(file, 'wb') as fp:
            if self.logLevel > 0: print "a_list", a_list
            i = 0
            for items in a_list:
                if self.logLevel > 0: print "items", items
                fp.write('%s' %items)
                if i < 5 : fp.write(",")
                i+=1
                # for item in items:
                #     if self.logLevel > 0: print "item", item
                #     fp.write('%s' %item)
                #     if i < 3 : fp.write(",")
                #     i+=1
                # fp.write('\n')

    # Read list to memory
    def read_list(self):
        # for reading also binary mode is important
        file = self.directory() + "hourlySchedule.txt"
        n_list = []
        try:
            with open(file, 'rb') as fp:
                for line in fp:
                    # print "line", line
                    # x = line[:-1]
                    # print x
                    y = line.split(",")
                    if self.logLevel > 0: print "y" , y
                    n_list = y
            return n_list
        except:
            return ["", "", "", "", "", ""]

    def directory2(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "train_operators_emblem"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator

    def write_list2(self, a_list):
        # store list in binary file so 'wb' mode
        file = self.directory2() + "train_operator_emblem.txt"
        if self.logLevel > 0: print "train_operator_emblem" , a_list
        if self.logLevel > 0: print "file" , file
        with open(file, 'wb') as fp:
            if self.logLevel > 0: print "a_list", a_list
            i = 0
            for items in a_list:
                if self.logLevel > 0: print "items", items
                fp.write('%s' %items)
                if i < 5 : fp.write(",")
                i+=1


    # Read list to memory
    def read_list2(self):
        # for reading also binary mode is important
        file = self.directory2() + "train_operator_emblem.txt"
        n_list = []
        try:
            with open(file, 'rb') as fp:
                for line in fp:
                    # print "line", line
                    # x = line[:-1]
                    # print x
                    y = line.split(",")
                    if self.logLevel > 0: print "y" , y
                    n_list = y
            return n_list
        except:
            return ["", "", "", "", "", ""]

    def is_time_format(self, input):
        try:
            time.strptime(input, '%H:%M')
            return True
        except IndexError:
            if self.logLevel > 0: print "a"
        except ValueError:
            return False

    def setup_minute_time_listener_to_schedule_trains(self):
        global tListener
        global timebase
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl
        global schedule_trains_hourly

        if self.logLevel > 0: print "Setting up Time Scheduler"
        timebase = jmri.InstanceManager.getDefault(jmri.Timebase)

        # set up timebase and start at midnight
        if self.logLevel > 0: print "******************************************set timebase hour"

        self.set_default_scheduling_values()
        self.set_timebase_start_hour(int(start_hour_gbl)-1, 45)
        self.set_timebase_start_hour(0, 0)

        # attach a listener to the timebase.
        tListener = TimeListener()

        # to set the rate, the timebase needs to be running
        mystate = timebase.getRun()
        if timebase.getRun() == False:
            self.swap_timebase_state_run_stop()
        timebase.userSetRate(float(speed_not_operational_gbl))
        desired_state = False
        if False != timebase.getRun():
            self.swap_timebase_state_run_stop()

        self.init = True

    def set_timebase_start_hour(self, hour, minute):
        from java.util import Date
        global timebase
        mystate = timebase.getRun()
        if mystate == False:
            self.swap_timebase_state_run_stop()
        timebase = jmri.InstanceManager.getDefault(jmri.Timebase)
        date = Date(2020,10,21)     #any date really
        date.setHours(hour)
        date.setMinutes(minute)
        timebase.userSetTime(date)   ## set to 00:00 21/10/2020 (want the time to be 00:00)
        if mystate != timebase.getRun():
            self.swap_timebase_state_run_stop()

    def train_scheduler(self):
        #reset fast clock
        self.reset_clock()

        #go through operations:trains and get the train starting now

    def reset_clock(self):
        pass

    f = None

    def display_help(self):
        ref = "html.scripthelp.DispatcherSystem.DispatcherSystem"
        jmri.util.HelpUtil.displayHelpRef(ref)

    def show_analog_clock(self):
        if self.f == None:
            self.f = jmri.jmrit.analogclock.AnalogClockFrame()
            self.f.setSize(300, 200)
            self.f.setLocationRelativeTo(None)
            if self.frame != None:
                self.f.setLocation(self.frame.getX() + self.frame.getWidth(), self.frame.getY());
        self.f.setVisible(True)

    def set_time_to_beginning_of_hourly_train_working(self):
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl
        # start_hour_gbl is the start of hourly working
        start_hour = int(start_hour_gbl)
        if self.logLevel > 0: print "start_hour", start_hour
        self.set_timebase_start_hour(start_hour -1, 55)

    def show_routes(self):
        # a = jmri.jmrit.operations.routes.RoutesTableAction()
        # a.actionPerformed(None)
        CreateAndShowGUI6(self)

    def show_operations_trains(self):
        global CreateAndShowGUI4_frame
        # delete any previous frames
        if "CreateAndShowGUI4_frame" in globals():
            CreateAndShowGUI4_frame.setVisible(False)
        else:
            # print "not in globals"
            pass

        # show dispatcher system form
        CreateAndShowGUI4(self)

    def get_train_list(self):
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        return train_list

from java.util.concurrent import TimeUnit

class TimeListener(java.beans.PropertyChangeListener):
    # This is called every minute since it has been attached as timebase.addMinuteChangeListener(tListener)
    def __init__(self):
        # global timetable_gbl
        self.logLevel = 0
        if self.logLevel > 0: print "set up TimeListener"
        self.prev_time = 0
        if self.logLevel > 0: print "end TimeListener"

    def propertyChange(self, event):

        # print "property change"
        global fast_clock_running_at_operational_speed
        if 'fast_clock_running_at_at_operational_speed' not in globals():
            fast_clock_running_at_operational_speed = True
        global minutes_old2, minutes_old3
        global schedule_trains_hourly
        global timetable_gbl, run_timetable_gbl
        global fast_clock_rate
        global send_mqtt_messages_gbl

        # print "a"
        minutes_old = int(event.getOldValue())
        # print "b"
        minutes = int(event.getNewValue())
        # print "c", minutes

        if 'minutes_old2' not in globals():
            minutes_old2 = minutes_old

        if self.logLevel > 0: print "minutes_old", minutes_old, "minutes", minutes, "minutes_old2", minutes_old2, \
            "(minutes - minutes_old2) % 60 ", (minutes - minutes_old2) % 60, \
            "(minutes_old - minutes_old2) % 60 == 0", (minutes_old - minutes_old2) % 60 == 0

        if self.logLevel > 0: print "property change", event.newValue
        if (minutes_old - minutes_old2) % 60 == 0:      # when we set the fast clock in the event timer it triggers a new event at the same time
                                                    # we then get into a recursion. This ignores the second call at the same time
            # print "x"
            if self.logLevel > 0: print "3 minutes_old", minutes_old, "minutes", minutes, "minutes_old2", minutes_old2, \
                "(minutes - minutes_old2) % 60 ", (minutes - minutes_old2) % 60
            # print "y", minutes, type(minutes), int(minutes)
            if int(minutes) % 10 == 0:               # only check every 10 minutes to prevent problens at non_operational_speeds
                                                # don't just check at 0 minutes in case train us started not on the hour
                # print "d"
                if self.logLevel > 0: print "minutes", int(minutes), "int(minutes) % 10", int(minutes) % 10, "minutes", minutes
                # print "e"
                minutes_old2 = minutes
                self.set_fast_clock_rate()      # sets global fast_clock_at_operational_speed

            self.process_operations_trains(event)    # scheduled trains
            # print "attempting to send timetable via mqtt"

            # if show
            send_mqtt_messages_gbl = True
            if 'send_mqtt_messages_gbl' not in globals():
                send_mqtt_messages_gbl = False
            if 'run_timetable_gbl' not in globals():
                run_timetable_gbl = False
            # print "send_mqtt_messages_gbl", send_mqtt_messages_gbl
            if run_timetable_gbl or send_mqtt_messages_gbl:
                # do not display more frequently than 5 secs
                # print "f1", fast_clock_rate
                fcr = int(str(fast_clock_rate))
                x = (5.0 / 60.0)
                # print "x", x, "fcr", fcr, "x*fcr", x * fcr
                no_fast_minutes = int(x * fcr)
                if no_fast_minutes == 0: no_fast_minutes = 1
                # print "no_fast_minutes", no_fast_minutes
                if minutes % no_fast_minutes == 0:
                    # print "H0"
                    Trigger_Timetable(minutes)
                # print "H1"
            else:
                # print "HIDING TIMETABLE WINDOW8888888888888888888888888888888888888888888888"
                # print "run_timetable_gbl", run_timetable_gbl, "send_mqtt_messages_gbl", send_mqtt_messages_gbl
                if 'timetable_gbl' in globals():
                    # timetable_gbl = None
                    if timetable_gbl != None:
                        timetable_gbl.hideWindow()

        minutes_old2 = minutes    # use minutes_old2 to prevent recursion
        # print "end property change"

    def stop(self):
        tListener.cancel()

    def set_fast_clock_rate(self):
        # set the fast clock rate
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl
        global schedule_trains_hourly
        global timebase
        global fast_clock_running_at_operational_speed

        # if self.logLevel > 0:  print "TimeListener: change",event.propertyName, "from", event.oldValue, "to", event.newValue

        if self.logLevel > 0: print "set_fast_clock_rate: 1"
        if self.speed_not_operational_gbl__is_defined():
            if self.logLevel > 0: print "set_fast_clock_rate: 2", speed_not_operational_gbl

            # if schedule_trains_hourly:
            if True:
                pass
                hour = int(timebase.getTime().getHours())
                minutes = int(timebase.getTime().getMinutes())
                rate = timebase.userGetRate()

                if self.logLevel > 0: print "set_fast_clock_rate:", "schedule_trains_hourly", schedule_trains_hourly
                fast_clock_during_non_operational_times = speed_not_operational_gbl
                if self.logLevel > 0: print "fast clock during non operational_times", fast_clock_during_non_operational_times
                if self.logLevel > 0: print "hour", hour, "start_hour_gbl", start_hour_gbl, "end_hour_gbl", end_hour_gbl, \
                    "fast_clock_rate", fast_clock_rate, "speed_not_operational_gbl", speed_not_operational_gbl, \
                    "hour >= start_hour_gbl and hour <= end_hour_gbl", hour >= int(start_hour_gbl) and hour <= int(end_hour_gbl)
                if hour >= int(start_hour_gbl) and hour <= int(end_hour_gbl):
                    # or hour == int(start_hour_gbl - 1) % 24 and minutes == 59:
                    if rate != fast_clock_rate:       # check to stop recursion error
                        timebase.userSetRate(float(fast_clock_rate))
                        fast_clock_running_at_operational_speed = True
                    if self.logLevel > 0: print "set_fast_clock_rate:", "fast_clock_rate slow", fast_clock_rate
                    pass
                else:
                    if self.logLevel > 0: print "fast_clock_during_non_operational_times", fast_clock_during_non_operational_times
                    fcr = fast_clock_during_non_operational_times
                    if fcr > 100 : fcr = 100  # set to maximum
                    if rate != fcr:
                        timebase.userSetRate(float(fcr))
                        fast_clock_running_at_operational_speed = False
                    if self.logLevel > 0: print "set_fast_clock_rate:", "fcr", fcr
        else:
            print "speed_not_operational_gbl__is_defined()", False

    def process_operations_trains(self, event ):
        global timebase
        global schedule_trains_hourly
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl
        global scheduled
        global trains_to_be_scheduled
        if self.logLevel > 0: print "A1"
        if 'schedule_trains_hourly' not in globals():
            schedule_trains_hourly = False
        if self.logLevel > 0: print "in process_operations_trains", "schedule_trains_hourly", schedule_trains_hourly

        hour = int(timebase.getTime().getHours())
        if self.logLevel > 1: print "type hour" , type(hour)
        minutes = event.newValue
        if self.logLevel > 1: print "type minutes", type(minutes)
        self.curr_time = minutes + hour * 60
        if self.logLevel > 1: print "curr_time", self.curr_time
        self.prev_time = self.curr_time -1
        if self.logLevel > 1: print "prev_time", self.prev_time

        # only schedule within stat_hour and end_hour
        if int(start_hour_gbl) <= hour <= int(end_hour_gbl):
            pass    # need to process trains
        else:
            if self.logLevel > 0: print "returning in process_operational_trains"
            return  # outside operational time

        if self.logLevel > 0: print "A2"
        if self.logLevel > 1: print "TimeListener: process_operations_trains"
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        if self.logLevel > 0: print "train_list",train_list
        #if self.logLevel > 1: print "prev_time", self.prev_time, "curr_time", self.curr_time
        if self.logLevel > 0: print "a3"
        for train in train_list:
            if self.logLevel > 1: print "*******************"
            if self.logLevel > 1: print ("train list: departure time: ", str(train.getDepartureTime()), str(train.getName()))
            if self.logLevel > 1: print "prev_time", self.prev_time, "curr_time", self.curr_time, "train.getDepartTimeMinutes()", train.getDepartTimeMinutes()
        #get the train that is triggered in the current minute
        if event == None:
            if self.logLevel > 0: print "event is none , returning"
            return

        if self.logLevel > 0: print "A4"

        # schedule the train taking account of the repeat command
        trains_to_start = []
        for train in train_list:
            comment = train.getComment()
            repeat_command = self.find_between(comment, "[repeat-", "-repeat]")
            # print "repeat1", repeat_command

            max = minutes
            min = (minutes - 1)
            mid = int(train.getDepartTimeMinutes())
            # else:
            #     max = minutes + 5               # add arbitrary value to test values to avoid texting with 59 and 0
            #     min = (minutes - 1) + 5
            #     mid = int(train.getDepartTimeMinutes()) + 5


            if repeat_command == "Once":
                if self.prev_time < int(train.getDepartTimeMinutes()) <= self.curr_time and \
                        "skip" not in train.getDescription():   # if skip in description of scheduled Train do not run the train
                    if train not in trains_to_start:
                        trains_to_start.append(train)
                        scheduled[train] = False
            elif repeat_command == "Repeat every 20 mins":
                if max % 20 == 0:
                    min += 1; mid += 1; max += 1      # ensure mid lies between min amd max (ensure we don't have 59 < 0 <= 0)
                if (min % 20 < (mid % 20) <= max % 20):
                    if train not in trains_to_start:
                        trains_to_start.append(train)
                        scheduled[train] = False
            elif repeat_command == "Repeat every 30 mins":
                if max % 30 == 0:
                    min += 1; mid += 1; max += 1
                if (min % 30 < (mid % 30) <= max % 30):
                    if train not in trains_to_start:
                        trains_to_start.append(train)
                        scheduled[train] = False
            elif repeat_command == "Repeat every Hour":
                if max == 0:
                    min += 1; mid += 1; max += 1
                if (min < (mid % 60) <= max):
                    if train not in trains_to_start:
                        trains_to_start.append(train)
                        scheduled[train] = False
            elif repeat_command == "Repeat every 2 Hours":
                if max == 0:
                    min += 1; mid += 1; max += 1
                min1 = hour - 1
                mid1 = int(train.getDepartureTimeHour())
                max1 = hour
                if max1 % 2 == 0:      # ensure mid lies between min amd max (ensure we don't have 1 < 0 <= 0)
                    min1 += 1; mid1 += 1; max1 += 1
                if (min < (mid % 60) <= max) and \
                        ((min1 % 2) <  (mid1 % 2) <= (max1 % 2)):
                    if train not in trains_to_start:
                        trains_to_start.append(train)
                        scheduled[train] = False
            else:
                if self.logLevel > 0: print "incorrect repeat command", repeat_command
                # assume set to once
                if self.prev_time < int(train.getDepartTimeMinutes()) <= self.curr_time and \
                        "skip" not in train.getDescription():   # if skip in description of scheduled Train do not run the train
                    if train not in trains_to_start:
                        trains_to_start.append(train)
                        scheduled[train] = False

            # print "trains_to_start", trains_to_start
        # print "scheduled[train]", scheduled

        if self.logLevel > 0: print "A8"
        for train in trains_to_start:
            if train not in trains_to_be_scheduled:
                if self.logLevel > 0: print "A9"
                trains_to_be_scheduled.append(train)
            scheduled[train] = False

        if self.logLevel > 0: print "trains_to_be_scheduled", trains_to_be_scheduled
        if self.logLevel > 0: print "scheduled", scheduled
        if self.logLevel > 0: print "trains_to_be_scheduled", trains_to_be_scheduled

    def speed_not_operational_gbl__is_defined(self):
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl
        try:
            speed_not_operational_gbl
        except NameError:
            if self.logLevel > 0: print("well, it WASN'T defined after all!")
            return False
        else:
            if self.logLevel > 0: print("sure, it was defined.")
            return True

    def find_between(self, s, first, last):
        try:
            start = s.index(first) + len(first)
            end = s.index(last, start)
            return s[start:end]
        except IndexError:
            if self.logLevel > 0: print "D"
        except ValueError:
            return ""

class Trigger_Timetable:
    def __init__(self, minutes):
        global timetable_triggered_gbl
        if minutes == None: return
        self.run(minutes)
        timetable_triggered_gbl = True

    def run(self, minutes):
        t1 = Thread(target=self.send_timetable_and_clock_via_mqtt, args=(minutes,))
        t1.start()
        t1.join()

    def send_timetable_and_clock_via_mqtt(self, minutes):
        global station_name_gbl, run_timetable_gbl
        self.logLevel = 0
        global timebase
        # print "****************start send_timetable_and_clock_via_mqtt"
        hour = int(timebase.getTime().getHours())
        time = str(hour).zfill(2) + ":" + str(minutes).zfill(2)
        file = self.directory2() + "train_operator_emblem.txt"
        event = ""
        # try:
        #     event = self.read_list2(file)
        #     print "event", event
        # except:
        #     event = "jim"
        # print "event1", event
        event = self.read_list2()[0]
        # print "event2", event


        # event = "fred"
        try:
            self.send_clock_message(hour, minutes, event)
        except:
            print "clock message not sent"


        # get list of origins, destinations and times at intermediate stations
        timetable = self.get_timetable(hour, minutes)
        # print "timetable", timetable
        if 'station_name_gbl' in globals():
            station = station_name_gbl
        else:
            station = 'Not Set'
        # print "******run_timetable_gbl", run_timetable_gbl
        if run_timetable_gbl:
            if "timetable_gbl" in globals():
                # print "timetable_gbl", timetable_gbl
                if timetable_gbl != None:
                    # print "showing window"
                    timetable_gbl.showWindow()
            # print "*********************generating local timetable"
            self.generate_local_timetable(station, time, timetable)
            # print "J"
        else:
            if "timetable_gbl" in globals():
                if timetable_gbl == None:
                    # print "HIDING WINDOW********************************************************"
                    timetable_gbl.hideWindow()
        # send mqtt message
        self.send_timetable_messages(timetable)
        # try:
        #     self.send_timetable_messages(timetable)
        # except:
        #     pass
        # print "end send_timetable_and_clock_via_mqtt"

    def find_between(self, s, first, last):
        try:
            start = s.index(first) + len(first)
            end = s.index(last, start)
            return str(s[start:end])
        except ValueError:
            return ""
        except IndexError:
            print "index error find_between", "first", first, "last", last
    def get_timetable(self, hour, minutes1):

        global schedule_trains_hourly
        global start_hour_gbl, end_hour_gbl
        global fast_clock_rate
        self.curr_time = minutes1 + hour * 60
        timetable = []

        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()

        if self.logLevel > 0: print "A5"
        for train in train_list:
            if "skip" not in train.getDescription():    #if skip in description of scheduled Train do not run the train
                comment = train.getComment()
                repeat = self.find_between(comment, "[repeat-", "-repeat]")
                # arrival_time = self.find_between(comment, "[arrival_time-", "-arrival_time]"   # only set if routine to set has been run (not written yet)
                departure_time_minutes = train.getDepartTimeMinutes()
                departure_time_hour = train.getDepartureTimeHour()
                train_name = train.getName()

                if repeat == "Once":
                    minutes = [departure_time_minutes]
                elif repeat == "Repeat every 20 mins":
                    minutes = [minutes for minutes in range(0,59) if (minutes % 20) == (int(departure_time_minutes) % 20)]
                elif repeat == "Repeat every 30 mins":
                    minutes = [minutes for minutes in range(0,59) if (minutes % 30) == (int(departure_time_minutes) % 30)]
                elif repeat == "Repeat every Hour":
                    minutes = [departure_time_minutes]
                elif repeat == "Repeat every 2 Hours":
                    minutes = [departure_time_minutes]
                else:
                    minutes = [0]

                if repeat == "Once":
                    hours = [departure_time_hour]
                else:
                    if hour < int(end_hour_gbl):
                        start_hour = max(hour, int(start_hour_gbl))
                        end_hour = max(hour + 3, int(start_hour_gbl))
                    else:
                        start_hour = int(start_hour_gbl)
                        end_hour = int(start_hour_gbl) + 3
                    hours = [hour1 for hour1 in range(start_hour, end_hour)]
                    if repeat == "Repeat every 2 Hours": #update hours if 2 hourly
                        hours = [hour1 for hour1 in hours if (int(train.getDepartureTimeHour()) % 2 == hour1 % 2)]

                for train_mins in minutes:
                    for train_hour in hours:
                        depart_time = int(str(train_mins)) + int(str(train_hour)) * 60
                        if self.logLevel > 0: print "train_hour", train_hour, "self.curr_time", self.curr_time, "depart_time", depart_time

                        train_route_start_time = str(train_hour).zfill(2) + ":" + str(train_mins).zfill(2)
                        train_route = train.getRoute()
                        # location.getComment() will be set to the departure timed if the routine to do this has been run

                        # last_location = train_route.getTerminatesRouteLocation()
                        # last_station = str(last_location.getName())
                        locations = [location for location in train_route.getLocationsBySequenceList() \
                         if ".py" not in location.getName()]
                        first_location = locations[0]
                        first_station = str(first_location.getName())
                        last_location = locations[-1]
                        last_station = str(last_location.getName())
                        comments = [location.getComment() for location in train_route.getLocationsBySequenceList() \
                                    if ".py" not in location.getName()]
                        journey_duration = 0
                        for comment in comments:
                            duration_sec = self.find_between(comment, "[duration_sec-", "-duration_sec]")
                            # print "duration_sec", duration_sec
                            if duration_sec != "":
                                duration = ((float(duration_sec) * int(str(fast_clock_rate))) / 60.0)  # fast minutes
                            else:
                                # print "setting duration 0"
                                duration = 0
                            journey_duration += int(duration)
                        # last_station_arrival_time = self.add_times(train_route_start_time, journey_duration)

                        for i, location in enumerate(train_route.getLocationsBySequenceList()):
                            station_name = str(location.getName())
                            if self.logLevel > 2: print "****************************", station_name, "***************************"
                            if ".py" in station_name:   # exclude actions
                                break
                            timetable_entry_names = ["train_name", "station_name", "station_departure_time", "last_station",
                                                     "last_station_arrival_time", "via"]
                            # timetable_entry_names = ["station_name", "station_departure_time", "last_station",
                            #                          "last_station_arrival_time", "via"]
                            # remove items from start of via list, and the destination
                            locations1 = train_route.getLocationsBySequenceList()
                            locations = [str(loc) for loc in locations1]

                            via = locations[i+1:-1]
                            via = [location2 for location2 in via if ".py" not in location2]
                            if via == []:
                                via = "Fast Train"
                            if last_station == station_name:
                                via = ["Terminates Here"]
                            via = str(via).replace('[','').replace(']','').replace("'", "")
                            # [last_station, last_station_arrival_time] = [str(last_location.getName()), str(last_location.getComment())]
                            # via = train_route.getLocationsBySequenceList()

                            # for i, location in enumerate(train_route.getLocationsBySequenceList()):
                            # timetable_entry_names = ["station_name", "station_departure_time", "last_station",
                            # "last_station_arrival_time", "via"]
                            # timetable.append(timetable_entry_names)
                            comment = location.getComment()
                            if i == 0:
                                station_departure_time = train_route_start_time
                                time_to_station = 0
                            else:
                                duration_sec = self.find_between(comment, "[duration_sec-", "-duration_sec]")
                                if str(duration_sec) == "":
                                    duration_sec = 0

                                if self.logLevel > 2: print location, "duration_sec", duration_sec
                                duration = float((float(duration_sec) * int(str(fast_clock_rate))) / 60.0)
                                if self.logLevel > 2: print location, "duration", duration

                                time_to_station = int(duration)
                                if self.logLevel > 2: print "time_to_station", time_to_station
                                    # print "time_to_station", time_to_station

                            previous_departure_time = station_departure_time
                            if self.logLevel > 2:
                                print train_name, "previous_departure_time", previous_departure_time
                            station_departure_time = self.add_times(station_departure_time, time_to_station)
                            if self.logLevel > 2: print train_name, location, "station_departure_time", station_departure_time


                            # if the wait_time and journey_time have been set we can set the arrival time
                            wait_time = self.find_between(comment, "[wait_time-", "-wait_time]")
                            journey_time = self.find_between(comment, "[journey_time-", "-journey_time]")
                            if journey_time == "":
                                journey_time = "0"
                            # convert journey time to fast minutes
                            journey_time_fast_mins = ((float(str(journey_time)) * int(str(fast_clock_rate))) / 60.0)
                            if wait_time != "" and journey_time != "":
                                if self.logLevel > 2: print train_name, location, "journey_time", journey_time
                                if self.logLevel > 2: print train_name, location, "journey_time_fast_mins", journey_time_fast_mins
                                station_arrival_time = self.add_times(previous_departure_time, journey_time_fast_mins)
                            else:
                                if i == 0:
                                    station_arrival_time = ""
                                else:
                                    station_arrival_time = station_departure_time

                            [h, m] = station_departure_time.split(":")
                            station_departure_time_in_mins = int(m) + int(h) * 60
                            # [h, m] = station_arrival_time.split(":")
                            # station_arrival_time_in_mins = int(m) + int(h) * 60

                            if last_station == station_name:
                                station_departure_time = ""

                            # make sure we don't display trains that have a departure time < current time
                            if self.curr_time < station_departure_time_in_mins:
                                # location.setComment(str(time_to_station))
                                # timetable_entry = [station_name, station_departure_time, last_station, last_station_arrival_time, via]
                                # timetable_entry = [train_name, \
                                #                    station_name , \
                                #                    station_departure_time, \
                                #                    last_station, \
                                #                    station_arrival_time, \
                                #                    via]

                                timetable_entry = [train_name, \
                                                   station_name , \
                                                   station_arrival_time, \
                                                   station_departure_time, \
                                                   first_station, \
                                                   last_station, \
                                                   via]

                                timetable.append(timetable_entry)
        #sort timetable by time
        timetable.sort(key = lambda row: max(row[2],row[3])         )
        return timetable

    def generate_local_timetable(self, station, time, timetable):
        global timetable_gbl
        if self.logLevel > 0: print "generating timetable"
        if "timetable_gbl" not in globals():
            # print "A"
            timetable_gbl = Timetable(station)
        elif timetable_gbl == None:
            # print "****** call Timetable"
            timetable_gbl = Timetable(station)
        else:
            # print "update"
            # update the Timetable
            # timetable_gbl.update(["jim1", "fred"])
            # timetable_gbl.update_time(time)
            # print "timetable", timetable
            timetable_gbl.update_timetable(station, time, timetable)

        if self.logLevel > 0: print "generated timetable"


    def send_timetable_messages(self,timetable):
        i = 0
        msg = "["
        for [train_name, \
             station_name , \
             station_arrival_time, \
             station_departure_time, \
             first_station, \
             last_station, \
             via] in timetable:
            # msg += '{"type" : "' + "schedule" + '", ' + \
            #        '"train_name" : "' + str(train_name) + '", ' + \
            #        '"station_name" : "' + str(station_name) + '", ' + \
            #        '"station_departure_hour" : "' + str(station_departure_time) + '", ' + \
            #        '"last_station" : "' + str(last_station) + '", ' + \
            #        '"last_station_arrival_time" : "' + str(last_station_arrival_time) + '", ' + \
            #        '"via" : "' + str(via) + '"},'
            msg += '{"type" : "' + "schedule" + '", ' + \
                   '"train_name" : "' + str(train_name) + '", ' + \
                   '"station_name" : "' + str(station_name) + '", ' + \
                   '"station_arrival_time" : "' + str(station_arrival_time) + '", ' + \
                   '"station_departure_time" : "' + str(station_departure_time) + '", ' + \
                   '"first_station" : "' + str(first_station) + '", ' + \
                   '"last_station" : "' + str(last_station) + '", ' + \
                   '"via" : "' + str(via) + '"},'
            i += 1
        msg = msg[:-1]
        msg += "]"
        # print
        # print "************************************"
        # print "x"
        # print i, " msg sent to node_red: ", msg
        # print "x1"
        # print "************************************"
        self.send_mqtt_message(msg)

    def send_mqtt_message(self, msg):
        # print
        # print
        # print "sending mqtt message", msg
        try:
            # Find the MqttAdapter
            mqttAdapter = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()

            # create content to send "/jmri/timetable message content"
            topic = "jmri/timetable"
            payload = msg

            # send
            mqttAdapter.publish(topic, payload)
        except:
            pass

    def send_clock_message(self, hour, minutes, event):

        msg = '[{"type" : "' + "clock" + '", ' + \
              '"time" : "' + str(hour).zfill(2) + ":" + str(minutes).zfill(2) + '",'\
              '"emblem" : "' + str(event) + '"}]'
        self.send_mqtt_message(msg)

    def add_times(self, station_departure_time, time_to_station):

        # add time_to_station to station_departure time
        # station_departure_time is in form hh:mm
        # print "time_to_station", time_to_station  , "should be mins"
        [hours, mins] = station_departure_time.split(":")
        # print "hours", hours, "mins", mins
        hour = int(hours) + int(time_to_station) // 60
        min = (int(mins) + int(time_to_station)) % 60
        # print "hour", hour, "min", min
        station_departure_time_new = str(hour).zfill(2) + ":" + str(min).zfill(2)
        # print "station_departure_time_new", station_departure_time_new
        return station_departure_time_new


    def directory2(self):
        # print "directory2"
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "train_operators_emblem"
        # print "path", path
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator

    def write_list2(self, a_list):
        # store list in binary file so 'wb' mode
        file = self.directory2() + "train_operator_emblem.txt"
        if self.logLevel > 0: print "train_operator_emblem" , a_list
        if self.logLevel > 0: print "file" , file
        with open(file, 'wb') as fp:
            if self.logLevel > 0: print "a_list", a_list
            i = 0
            for items in a_list:
                if self.logLevel > 0: print "items", items
                fp.write('%s' %items)
                if i < 5 : fp.write(",")
                i+=1


    # Read list to memory
    def read_list2(self):
        # for reading also binary mode is important
        file = self.directory2() + "train_operator_emblem.txt"
        # print "file", file
        n_list = []
        try:
            with open(file, 'rb') as fp:
                for line in fp:
                    # print "line", line
                    # x = line[:-1]
                    # print x
                    y = line.split(",")
                    if self.logLevel > 0: print "y" , y
                    n_list = y
            return n_list
        except:
            return ["", "", "", "", "", ""]

class RunTrain(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self, train, graph, set_departure_times  = False):
        # print " ***  in running train"
        self.logLevel = 0
        self.set_departure_times = set_departure_times
        if train == None:
            self.logLevel = 0
            if self.logLevel > 0: print "RunTrain: train == None"
        else:
            if self.logLevel > 0: print "RunTrain: train =", train
            if self.logLevel > 0: print "RunTrain"
            global trains_to_be_scheduled
            if self.logLevel > 0: print "trains_to_be_scheduled", trains_to_be_scheduled
            self.graph = graph
            self.train = train
        # print " *** ended init RunTrain"

    def handle(self):    # Need to overload handle
        # print "run train handle"
        if self.logLevel > 0: print "start run train"
        self.run_train()
        if self.logLevel > 0: print "end run train"
        if "repeat" in self.train.getDescription():
            # print "handle return True"
            return True
        else:
            # print "handle return False"
            return False

    def run_train(self):
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl
        global set_departure_trains_gbl
        global timebase
        if 'timebase' not in globals():
            timebase = jmri.InstanceManager.getDefault(jmri.Timebase)

        if self.logLevel > 0: print "************************************run train******************"
        if self.logLevel > 0:  "!     start run_train"
        route = self.train.getRoute()
        if route == None:
            msg = "train " + train.getName() + " has no route"
            JOptionPane.showMessageDialog(None, msg, 'Message', JOptionPane.WARNING_MESSAGE)
            return
        station_list = route.getLocationsBySequenceList()
        if self.logLevel > 0:  print "!     self.train: ", self.train, "station_list: ", station_list
        station_from = None

        self.route = route
        # print "route", self.route
        # set up station_list
        station_list_locations = self.route.getLocationsBySequenceList()
        #convert station_list to strings
        station_list = [location.getName() for location in station_list_locations]
        station_comment_list = [location.getComment()  for location in station_list_locations]
        # print "station_comment_list", station_comment_list


        self.station_list = station_list
        self.station_comment_list = station_comment_list

        # time = str(hour).zfill(2) + ":" + str(minutes).zfill(2)

        train_dispatched = False

        # if station_list == []:
            # print "station_list", station_list

        # print "station_list", station_list
        for station_index, station in enumerate(self.station_list):

            durations = [MyTableModel5().find_between(comment, "[duration-", "-duration]") for comment in station_comment_list]
            # print "durations 4", durations
            accumulated_durations = []
            total = 0
            for n in durations:
                try:
                    total += int(n)
                except:
                    pass
                accumulated_durations.append(total)


            station_comment = self.station_comment_list[station_index]
            accumulated_duration = accumulated_durations[station_index]
            # print "station", station

            if self.station_is_action(station):  #if the station_name is a python_file
                # some of the python files take an argument of the dispatch
                # make this the default for simplic
                # [next_station, next_station_index] = self.get_next_item_in_list(station,self.station_list)
                action = station
                self.execute_action(action)     # execute the python file
            else:
                station_to = station  # both now strings
                if station_from != None:    # first time round station_from is not set up
                    if self.logLevel > 0:  print "!     moving from", station_from, "to", station_to
                    self.station_from_name = station_from
                    self.station_to_name = station_to
                    start_block = blocks.getBlock(station_from)
                    if self.logLevel > 0:  "start_block",start_block, "station_to", station_to
                    train_to_move = start_block.getValue()
                    self.train_name = train_to_move
                    # self.train_name = self.train_name_in
                    # train_to_move = self.train_name_in
                    if self.logLevel > 0: print "calling move_between_stations","station_from",station_from,\
                        "station_to",station_to,"train_to_move",train_to_move

                    doNotRun = False
                    repeat = False
                    if self.logLevel > 0: print "train_to_move", train_to_move
                    # try to move for scheduling_margin_gbl fast seconds, then give up
                    # print "scheduling_margin_gbl", scheduling_margin_gbl

                    train_to_move = start_block.getValue()

                    train_dispatched = False
                    myframe = None
                    # print "scheduling_margin_gbl", scheduling_margin_gbl
                    for j in range(int(scheduling_margin_gbl)):    # try to schedule train for scheduling_margin_gbl fast minutes

                        # if we have turned scheduling off stop waiting for train to arrive
                        if scheduling_in_operation_gbl == "False" and self.set_departure_times == False:
                            # print "breaking as sceduling not in operation"
                            break
                        train_in_block = self.blockOccupied(start_block)
                        if train_to_move != None and train_in_block:

                            # try: myframeold.dispose()
                            # except: pass
                            # try: myframe.dispose()
                            # except: pass
                            # move  train
                            if self.logLevel > 0: print "************************************moving train******************",train_to_move
                            # set_departure_trains_gbl = False
                            # if "set_departure_trains_gbl" not in globals():
                            #     set_departure_trains_gbl = False
                            if self.set_departure_times == False:
                                # print "accumulated_duration",accumulated_duration
                                self.wait_for_scheduled_time(accumulated_duration)

                            # print "station_from, station_to, train_to_move, self.graph, station_comment", \
                            #     station_from, station_to, train_to_move, self.graph, station_comment

                            if self.set_departure_times:
                                # print "setting previous time"
                                previous_time = int(round(time.time()))  # in secs
                                # print "a"

                            move_train = MoveTrain(station_from, station_to, train_to_move, self.graph, station_comment)
                            move_train.move_between_stations(station_from, station_to, train_to_move, self.graph)

                            # train has moved, if we are in departure_time_setting mode, store the journey time

                            if self.set_departure_times:
                                # print "b"
                                current_time = int(round(time.time()))  # in secs
                                journey_time_in_secs = current_time - previous_time
                                self.store_journey_time(self.route, station_index, str(journey_time_in_secs))
                                # print "C"

                            move_train = None
                            if self.logLevel > 0: print "finished move between stations station_from = ", station_from, " station_to = ", station_to
                            end_block = blocks.getBlock(station_to)
                            if self.logLevel > 0: print "state of block" , end_block.getState()
                            # do following in case the block sensor is a bit dodgy
                            end_block.setValue(train_to_move)

                            train_dispatched = True

                        if train_dispatched: break


                        current_date = timebase.getTime() # in secs
                        minutes = current_date.getMinutes()
                        # print "No train in block for scheduled train", self.train, \
                        #     "starting from " + station_from + \
                        #     " waited: " + str(j) + " fast minutes " + \
                        #     " current minutes " + str(minutes)

                        # msg = "No train in block for scheduled train starting from " + station_from
                        # msg2 = "Trying again for " + str(scheduling_margin_gbl) + " fast minutes"
                        # if myframe == None:
                        #     myframe = self.show_custom_message_box(msg, msg2)

                        fast_minute = 1000*60/int(str(fast_clock_rate))
                        self.waitMsec(fast_minute)
                        # current_date = timebase.getTime() # in secs
                        # minutes = current_date.getMinutes()
                        # print "waited fast minute, time = " + str(minutes), " index " , station_index, "j", j

                    try: myframe.dispose()
                    except: pass

                    if self.logLevel > 0: print "finished move between stations station_from = ", station_from, " station_to = ", station_to
                    end_block = blocks.getBlock(station_to)
                    # do following in case the block sensor is a bit dodgy
                    # end_block.setValue(train_to_move)

                    check_action_route_flag = False     # This flag may have been set by the action appearing in the route
                    # before this move. It has to be reset.
                    # print "check_action_route_flag reset", check_action_route_flag

                    if train_dispatched == False:
                        break

            station_from = station_to

        #remove train from train list
        try:
            if "repeat" in self.train.getDescription() or train_dispatched == False:
                repeat = True
            if repeat == False or doNotRun == True:
                global trains_to_be_scheduled
                if self.train != None:
                    trains_to_be_scheduled.remove(self.train)
                self.waitMsec(4000)
            else:
                self.waitMsec(4000)
            if self.logLevel > 0:  print "!     finished run_train"
        except IndexError:
            print "Index Error E"
        except:
            self.waitMsec(4000)

    def store_journey_time(self, route, row, value):
        global CreateAndShowGUI5_glb
        routeLocationList = route.getLocationsBySequenceList()
        routeLocation = routeLocationList[row]
        # print "routeLocation", routeLocation, "row", row, "value", value, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5"
        # OptionDialog().displayMessageNonModal("about to set_value_in_comment", "OK")
        self.set_value_in_comment(routeLocation, value, "journey_time")
        # OptionDialog().displayMessageNonModal("about to populate_action", "OK")
        CreateAndShowGUI5_glb.populate_action(None)
        # OptionDialog().displayMessageNonModal("about to update_journey_time_action", "OK")
        CreateAndShowGUI5_glb.update_journey_time_action(None)
        # OptionDialog().displayMessageNonModal("just did update_journey_time_action", "OK")
        # CreateAndShowGUI5_glb.update_duration_action(None)
        #

    def set_value_in_comment(self, routeLocation, value, duration_string):
        global CreateAndShowGUI5_glb

        comment = routeLocation.getComment()    #Null

        if comment == None:
            comment = ""

        delim_start = "[" + duration_string + "-"
        delim_end = "-" + duration_string + "]"

        comment = CreateAndShowGUI5_glb.insert_between(comment, delim_start, delim_end, value)
        routeLocation.setComment(comment)

    def find_between(self, s, first, last):
        try:
            start = s.index(first) + len(first)
            end = s.index(last, start)
            return s[start:end]
        except ValueError:
            return ""
    def delete_between(self, string, delim1, delim2):
        first, _, rest = string.partition(delim1)
        _, _, rest = rest.partition(delim2)
        cleaned_text = ' '.join([first.strip(), rest.strip()])
        return cleaned_text

    def insert_between(self, string, delim1, delim2, value):
        first, _, rest = string.partition(delim1)
        _, _, rest = rest.partition(delim2)
        # print "string", string, "first.strip()", first.strip(), "rest.strip()", rest.strip()
        new_val = delim1 + str(value) + delim2
        modified_text = new_val.join([first.strip(), rest.strip()])
        # print "modified_text",modified_text
        return modified_text



    def blockOccupied(self, block):
        if block.getState() == ACTIVE:
            state = True
        else:
            state = False
        return state

    def wait_for_scheduled_time(self, accumulated_durations):
        global fast_clock_rate
        global timebase
        # print "TIMEBASE",
        if 'timebase' not in globals():
            timebase = jmri.InstanceManager.getDefault(jmri.Timebase)

        comment = self.train.getComment()
        if self.logLevel > 0: print "x1"
        repeat_command = TimeListener().find_between(comment, "[repeat-", "-repeat]")
        if self.logLevel > 0: print "x3"
        current_minutes = int(timebase.getTime().getMinutes())
        if self.logLevel > 0: print "x2", "current_minutes", current_minutes
        # the departure time for the train is the first one in the hour
        # get what would be the current time if we were running the first train in the hour
        # so we can get the difference of the two aaaand hence get the wait time
        # this is easier than getting the actual departure time minutes of the train
        if repeat_command == "Once":
            current_minutes_mod = current_minutes
        elif repeat_command == "Repeat every 20 mins":
            current_minutes_mod = current_minutes % 20
        elif repeat_command == "Repeat every 30 mins":
            current_minutes_mod = current_minutes % 30
        elif repeat_command == "Repeat every Hour":
            current_minutes_mod = current_minutes
        elif repeat_command == "Repeat every 2 Hours":
            current_minutes_mod = current_minutes
        else:
            print "error wrong repeat command"
        if self.logLevel > 0: print "d", current_minutes, "current_minutes_mod", current_minutes_mod
        current_hour = int(str(timebase.getTime().getHours()))
        if self.logLevel > 0: print("a"), current_hour
        current_time = str(current_hour).zfill(2) + ":" + str(current_minutes_mod).zfill(2)
        if self.logLevel > 0: print "c", current_time
        train_start_time = self.train.getDepartureTime()
        if self.logLevel > 0: print "b", train_start_time
        station_start_time = self.add_minutes_to_time(train_start_time, accumulated_durations)
        if self.logLevel > 0: print "scheduled time", station_start_time
        minutes_to_wait = self.subtract_times(current_time, station_start_time)
        if self.logLevel > 0: print "minutes_to_wait", minutes_to_wait
        fast_ms_to_wait =  abs(minutes_to_wait) * 60 * 1000
        if self.logLevel > 0: print "v"
        ms_to_wait = fast_ms_to_wait / int(fast_clock_rate)
        if self.logLevel > 0: print "w"
        if self.logLevel > 0: print "waiting", "ms_to_wait", ms_to_wait, "fast_secs_to_wait", fast_ms_to_wait/1000
        if self.logLevel > 0: print "time before wait", str(timebase.getTime())
        self.waitMsec(ms_to_wait)
        if self.logLevel > 0: print "time after wait", str(timebase.getTime())
        if self.logLevel > 0: print

    def add_minutes_to_time(self, time, minutes):

        if self.logLevel > 0: print "z"

        [time_hours, time_mins] = time.split(":")

        if self.logLevel > 0: print "add minutes to time"

        if self.logLevel > 0: print "time", time, "minutes", minutes

        # print "hours", hours, "mins", mins
        hour = int(str(time_hours)) + (int(time_mins) + int(minutes)) // 60
        if self.logLevel > 0: print "hour"
        min = (int(minutes) + int(time_mins) ) % 60
        if self.logLevel > 0: print "min", min
        if self.logLevel > 0: print "hour", hour, "min", min

        station_departure_time_new = str(hour).zfill(2) + ":" + str(min).zfill(2)
        if self.logLevel > 0: print "station_departure_time_new", station_departure_time_new

        return station_departure_time_new

    def subtract_times(self, current_time, station_time):
        if self.logLevel > 0: print "subtract times"
        [curr_hours, curr_mins] = current_time.split(":")
        # current_mins = int(curr_hours) * 60 + int(curr_mins)
        current_mins = int(curr_mins)
        if self.logLevel > 0: print "curr_hours", curr_hours, "curr_mins", curr_mins, "current_mins", current_mins

        [dep_hours, dep_mins] = station_time.split(":")
        # station_mins = int(dep_hours) * 60 + int(dep_mins)
        station_mins = int(dep_mins)
        if self.logLevel > 0: print "dep_hours", dep_hours, "dep_mins", dep_mins, "station_mins", station_mins
        # print "current_time", current_time, "mins", mins
        wait_time = station_mins - current_mins
        if self.logLevel > 0: print "wait_time", wait_time
        wait_time = wait_time % 60
        if self.logLevel > 0: print "wait_time", wait_time
        return wait_time

    def show_custom_message_box(self, msg, msg2):
        # JDialog dialog = new JDialog(mainFrame, "Non-Modal Dialog", false);
        frame = JDialog(None, "Custom Message Box", False)
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
        frame.setSize(400, 200)
        frame.setLocationRelativeTo(None)

        panel = JPanel(None)
        frame.add(panel)

        label = JLabel(msg)
        label.setBounds(50, 10, 350, 20)
        panel.add(label)

        label2 = JLabel(msg2)
        label2.setBounds(50, 30, 350, 20)
        panel.add(label2)

        yes_button = JButton("Close", actionPerformed=lambda event: frame.dispose())
        yes_button.setLayout(None)
        yes_button.setBounds(10, 50, 80, 30)
        panel.add(yes_button)

        # no_button = JButton("No", actionPerformed=lambda event: frame.dispose())
        # no_button.setBounds(200, 50, 80, 30)
        # panel.add(no_button)

        frame.setVisible(True)
        return frame

    def close_custom_message_bpx(self):
        JOptionPane.getRootFrame().dispose()

    def station_is_action(self, station):
        if station[-3:] == ".py":
            return True
        else:
            return False

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
    def execute_action(self, action):
        # execute a python file in the dispatcher directory
        file = self.action_directory() + action
        if not os.path.isfile(file):
            file = self.action_directory_in_DispatcherSystem() + action
        if not os.path.isfile(file):
            self.displayMessage("action file " + action + " does not exist, it must have been deleted\n" + \
                                "should be in directories:\n" + \
                                self.action_directory_in_DispatcherSystem() + " or\n" + \
                                self.action_directory())
        if self.logLevel > 0: print "file", file
        exec(open(file).read())     # execute the file


class RunRoute(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self, route, graph, station_from, station_to, no_repetitions, train_name, delay = 0):

        # station_from is set to the initial position of the train, not necessarily
        # the start position of the route
        # station_to is set only if returning to start position
        # in that case it is set to station_from
        # the route gives the stations on the route

        # note station_to and station_from are strings, while elements of route are locations

        #print "in init"

        self.logLevel = 0
        #print "loglevel", self.logLevel

        if route == None:
            if self.logLevel > 0: print "RunRoute: route == None"
        else:
            if self.logLevel > 0: print "RunRoute: route =", route
            self.graph = graph
            self.route = route
            self.station_from = station_from
            self.station_to = station_to
            self.no_repetitions = no_repetitions
            self.mycount = 0
            self.train_name_in = train_name
            self.delay = delay

            # set up station_list
            station_list_locations = self.route.getLocationsBySequenceList()
            #convert station_list to strings
            station_list = [location.getName() for location in station_list_locations]
            station_comment_list = [location.getComment()  for location in station_list_locations]
            self.initial_station_in_route = station_list[0]

            # prepend station_from if required
            self.prepended = False
            if self.logLevel > 0: print "station_list before", station_list, "self.station_from",self.station_from,"self.station_to",self.station_to,"station_list[0]",station_list[0]
            if self.station_from == None:                       #ensure route starts at station_from
                pass
            elif self.station_from != station_list[0]:
                try:
                    station_list.insert(0,self.station_from)
                    station_comment_list.insert(0, None)
                except:
                    if self.logLevel > 0: print "fred"
                self.prepended = True                           # we have to remove this initial station if we are repeating
            if self.logLevel > 0: print "station_list",station_list

            # append station_to if required
            if self.station_to == None:                         #ensure route ends at station_to
                pass
            elif self.station_to != station_list[-1]:
                station_list.append(self.station_to)
                try:
                    station_comment_list.append(None)
                except:
                    if self.logLevel > 0: print "jim"
            if self.logLevel > 0: print "station_list",station_list

            # if repeating append initial station in route
            if self.no_repetitions > 0:                             #ensure route end at start point if repeating
                if self.station_to != self.initial_station_in_route:
                    if station_list[-1] != self.initial_station_in_route:
                        station_list.append(self.initial_station_in_route)
                        station_comment_list.append(None)
            if self.logLevel > 0: print "station_list after", station_list

            self.station_list = station_list
            self.station_comment_list = station_comment_list

            # ignore the number of repetitions if station_to was not set to station_from
            if self.station_list[0] != self.station_list[-1]:
                self.no_repetitions = 0

    def handle(self):
        if self.logLevel > 0: print "in handle", self.mycount
        if self.delay > 0 and self.mycount == 0:  # only delay on the first iteration
            self.waitMsec(self.delay)
        if int(self.mycount) <= int(self.no_repetitions):
            #print "station_list in handle", self.station_list, "in handle", self.mycount
            self.run_route()
            #print "prepended", self.prepended
            if self.mycount == 0 and self.prepended:
                if self.logLevel > 0: print "station_list before pop", self.station_list
                self.station_list.pop(0)
                self.station_comment_list.pop(0)
                if self.logLevel > 0: print "station_list after pop", self.station_list
            if self.logLevel > 0: print "returning true", "train_name", self.train_name, "mycount", self.mycount, "reps" , self.no_repetitions

            self.mycount += 1     # 0 first time round
            return True
        else:
            if self.logLevel > 0: print "returning true", "train_name", self.train_name, "mycount", self.mycount, "reps" , self.no_repetitions
            return False

    def run_route(self):
        global check_action_route_flag
        if self.logLevel > 0: print "************************************run train******************"
        if self.logLevel > 0:  print "!     start run_route"

        station_from = None
        for i, station in enumerate(self.station_list):
            station_comment = self.station_comment_list[i]
            # print "station", station

            if self.station_is_action(station):  #if the station_name is a python_file
                # some of the python files take an argument of the dispatch
                # make this the default for simplic
                # [next_station, next_station_index] = self.get_next_item_in_list(station,self.station_list)
                action = station
                self.execute_action(action)     # execute the python file
            else:
                station_to = station  # both now strings
                if station_from != None:    # first time round station_from is not set up
                    if self.logLevel > 0:  print "!     moving from", station_from, "to", station_to
                    self.station_from_name = station_from
                    self.station_to_name = station_to
                    start_block = blocks.getBlock(station_from)
                    if self.logLevel > 0:  "start_block",start_block, "station_to", station_to
                    #train_to_move = start_block.getValue()
                    #self.train_name = train_to_move
                    self.train_name = self.train_name_in
                    train_to_move = self.train_name_in
                    if self.logLevel > 0: print "calling move_between_stations","station_from",station_from,"station_to",station_to,"train_to_move",train_to_move

                    doNotRun = False
                    repeat = False
                    if self.logLevel > 0: print "train_to_move", train_to_move
                    if train_to_move != None:
                        if self.logLevel > 0: print "************************************moving train******************",train_to_move
                        move_train = MoveTrain(station_from, station_to, train_to_move, self.graph, station_comment)
                        #if self.check_train_in_start_block(train_to_move, station_from)
                        move_train.move_between_stations(station_from, station_to, train_to_move, self.graph)
                        move_train = None
                        if self.logLevel > 0: print "finished move between stations station_from = ", station_from, " station_to = ", station_to
                        end_block = blocks.getBlock(station_to)
                        msg = "finished move between stations station_from = " + station_from + "state of block" + str(end_block.getState())
                        if self.logLevel > 0: print "state of block" , end_block.getState()
                        title = "Information after moving"
                        opt1 = "OK"

                        #OptionDialog().customMessage(msg, title, opt1)
                        #end_block.setValue(train_to_move)
                    else:
                        msg = "2No train in block for scheduled train starting from " + station_from
                        title = "Scheduling Error"
                        opt1 = "Not scheduling train"
                        if train_to_move == None:
                            if self.logLevel > 0: print "2No train in block for scheduled train starting from " + station_from
                            OptionDialog().customMessage(msg, title, opt1)
                            start_block = blocks.getBlock(station_from)
                            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
                            layoutBlock = LayoutBlockManager.getLayoutBlock(start_block)
                            if layoutBlock.getOccupancySensor().getKnownState() == INACTIVE:
                                layoutBlock.getOccupancySensor().setKnownState(ACTIVE)
                                self.waitMsec(2000)
                            if self.logLevel > 0:  "start_block",start_block, "station_to", station_to
                            train_to_move = start_block.getValue()


                        move_train = MoveTrain(station_from, station_to, train_to_move, self.graph)
                        move_train.move_between_stations(station_from, station_to, train_to_move, self.graph)
                        move_train = None
                        if self.logLevel > 0: print "finished move between stations station_from = ", station_from, " station_to = ", station_to
                        end_block = blocks.getBlock(station_to)  #do following in case the block sensor is a bit dodgy
                        end_block.setValue(train_to_move)

                    check_action_route_flag = False     # This flag may have been set by the action appearing in the route
                                                    # before this move. It has to be reset.
                    # print "check_action_route_flag reset", check_action_route_flag
                station_from = station_to

        self.waitMsec(4000)

        if self.logLevel > 0:  print "!     finished run_train"

    def get_next_item_in_list(self,elem, li ):
        if (li.index(elem))+1 != len(li):
            thiselem = elem
            nextelem = li[li.index(elem)+1]
            indexNextElem = li.index(elem)+1
            # print 'thiselem',thiselem
            # print 'nextel',nextelem
        else:
            thiselem = elem
            nextelem = elem
            indexNextElem = li.index(elem)
            # print 'thiselem',li[li.index(elem)]
            # print 'nextel',li[li.index(elem)]
        return [nextelem, indexNextElem]

    def check_train_in_start_block(self, train_to_move, blockName):
        block = blocks.getBlock(blockName)
        if self.blockOccupied(block):
            if block.getValue() == train_to_move:
                return True
            else:
                "blockName" , blockName, "not occupied by", "train_to_move", train_to_move
                blockName = [block for block in blocks.getNamedBeanSet() if block.getValue() == train_to_move]
                if blockName != []:
                    blockName = blockName[0]
                else:
                    blockName = "train not in any block"
                #print "train_to_move", train_to_move, "in" , blockName
                return False
        else:
            #print "train_to_move", train_to_move, "not in" , blockName
            blockName = [block for block in blocks.getNamedBeanSet() if block.getValue() == train_to_move]
            if blockName != []:
                blockName = blockName[0]
            else:
                blockName = "train not in any block"
            #print "train_to_move", train_to_move, "in" , blockName
            return False

    def blockOccupied(self, block):
        if block.getState() == ACTIVE:
            state = True
        else:
            state = False
        return state

    def station_is_action(self, station):
        if station[-3:] == ".py":
            return True
        else:
            return False

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

    def execute_action(self, action):
        # execute a python file in the dispatcher directory
        file = self.action_directory() + action
        if not os.path.isfile(file):
            file = self.action_directory_in_DispatcherSystem() + action
        if not os.path.isfile(file):
            self.displayMessage("action file " + action + " does not exist, it must have been deleted\n" + \
                                "should be in directories:\n" + \
                                self.action_directory_in_DispatcherSystem() + " or\n" + \
                                self.action_directory())
        if self.logLevel > 0: print "file", file
        exec(open(file).read())     # execute the file

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

class ScheduleTrains(jmri.jmrit.automat.AbstractAutomaton):

    def setup(self):
        self.logLevel = 0
        return True

    def handle(self):
        # print "run trains"
        self.run_trains()    #schedule trains if schedule_trains_glb is set True external to this routine
        # continue scheduling trains
        # print "end run trains"
        return True

    def run_trains(self):

        global trains_to_be_scheduled
        global schedule_trains_glb
        global fast_clock_running_at_operational_speed
        global scheduled
        global timebase
        if self.logLevel > 0 : print "************************************run trains******************"
        schedule_trains_glb = True
        if schedule_trains_glb:
            if 'fast_clock_running_at_operational_speed' not in globals():
                fast_clock_running_at_operational_speed = True
            if self.logLevel > 0 : print "run trains started: loop: scheduled trains", trains_to_be_scheduled, \
                "fast_clock_running_at_operational_speed", fast_clock_running_at_operational_speed
            # print "scheduled", scheduled
            # print "run_train_dict", run_train_dict
            if fast_clock_running_at_operational_speed:
                for train in trains_to_be_scheduled:
                    # print "train", train
                    if scheduled[train] == False:
                        if self.logLevel > 0: print "train",train,"scheduled[train]",scheduled[train]

                        if "stopping" in train.getDescription():
                            # print "running train %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
                            run_train_dict[train] = RunTrain(train, g.g_stopping)
                        else:
                            # print "running train %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
                            run_train_dict[train] = RunTrain(train, g.g_express)
                        run_train_dict[train].setName("schedule_" + train.getName())
                        run_train_dict[train].start()
                        scheduled[train] = True
                        if self.logLevel > 0: print "scheduled train ", train
                if self.logLevel > 0:  print "!!!!!!!!!!!!!!!!!!!!!run_trains finished"
                if self.logLevel > 0:  print "trains_to_be_scheduled ", trains_to_be_scheduled
                if 'timebase' in globals():
                    if self.logLevel > 0:  print "timebase.getRate()",timebase.getRate()

        if 'timebase' in globals():
            noMsec = int(1000/timebase.getRate())
            self.waitMsec(noMsec)  # every fast minute





