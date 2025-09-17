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

from javax.swing import JFrame, JPanel, JButton, BoxLayout, Box, JComponent, BorderFactory, JLabel, JTextField
from java.awt import GridLayout
from java.awt import Color, Font
from java.awt import Dimension, FlowLayout
from java.beans import PropertyChangeEvent
from java.awt.event import FocusAdapter, ActionListener
from javax.swing import SwingWorker, SwingUtilities
import os
import copy
import jmri
import java
import time
from org.python.core.util import StringUtil
from threading import Thread
from collections import Counter

# from javax.swing import JFrame
# from java.awt.event import WindowAdapter
from java.util.concurrent import CountDownLatch


CreateSchedulerPanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/SchedulerPanel.py')
execfile(CreateSchedulerPanel)

CreateRoutesPanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/RoutesPanel.py')
execfile(CreateRoutesPanel)

CreateEditRoutePanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/EditRoutePanel.py')
execfile(CreateEditRoutePanel)

CreateEditRoutePanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Timetable.py')
execfile(CreateEditRoutePanel)

try:
    basestring
except NameError:
    basestring = str

if "list" in globals() and type(globals()["list"]).__name__ != "type":
    # print(" Detected shadowed 'list' type: ", type(globals()["list"]))  # list is being used in JMRI. This enables us to use list in Jython
    del globals()["list"]

CreateEditRoutePanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/DisplayMqttTimetable.py')
execfile(CreateEditRoutePanel)

trains_to_be_scheduled = []
run_train_dict = {}
scheduled = {}
RunTrain_instance = {}
tListener = None

class SchedulerMaster(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self):
        global scheduling_in_operation_gbl
        self.logLevel = 0
        self.frame = None
        self.f = None
        scheduling_in_operation_gbl = False
        self.od = OptionDialog()

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
        scheduling_in_operation_gbl = "False"
        self.stop_schedule_trains_master()    #don't really need this.
        # print "scheduling_in_operation_gbl 6 start setup", scheduling_in_operation_gbl
        # self.ToggleSchedulingtrains_action(None)      # Toggle schedule_trains_glb
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
        global run_local_timetable_gbl
        # print "scheduling_in_operation_gbl 7 - init", scheduling_in_operation_gbl
        if "run_timetable_gbl" not in globals():
            run_local_timetable_gbl = False
        self.train_scheduler_setup = False
        # print "scheduling_in_operation_gbl 7 - init end", scheduling_in_operation_gbl
        if self.logLevel > 0: print "returned from init"

    showing_clock = False
    showing_trains = False
    def handle(self):
        # print "start handle 0"
        global schedule_trains_hourly
        global schedule_trains_glb
        global run_local_timetable_gbl
        global station_name_list_gbl
        global station_name_list_mqtt_gbl
        global group_location_gbl
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
            # if "station_name_list_gbl" not in globals():
            #     station_name_list_gbl = ""
            # print "b"
            TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
            train_list = TrainManager.getTrainsByTimeList()
            my_scheduled_route_list = [train.getRoute() for train in train_list]
            if None in my_scheduled_route_list:
                OptionDialog().displayMessage("check scheduled routes are entered correctly\ncannot proceed with timetable")
            else:
                # print "my_scheduled_route_list", my_scheduled_route_list
                # print "c"
                RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
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
                # print "station_list", station_list

                # get station_group list
                platform_list = []
                station_group_list = []
                station_group_list.append("All Stations")
                station_group_location_list = []
                PlatformPanel = MyTableModel7()
                for location_name in station_list:
                    LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
                    location = LocationManager.getLocationByName(location_name)
                    platform = PlatformPanel.get_location_platform(location)
                    station_group = PlatformPanel.get_location_station_group(location)
                    if station_group.strip() is not None and station_group.strip() is not "":
                        if station_group.strip() not in station_group_list:
                            station_group_list.append(station_group.strip())
                            # print "type station_group", type(station_group.strip())
                        station_group_location_list.append([station_group.strip(), location_name])
                    station_group_location_list.append(["All Stations", location_name])
                repeat = True
                while repeat:
                    if self.logLevel > 0: print "station list", station_list
                    msg = "timetables can be shown locally on this computer or\n" + \
                          "on remote computers/tablets communicating by mqtt\n\n" + \
                          "scheduler needs to be on and clock running"
                    opt0 = "Cancel"
                    if run_local_timetable_gbl == True:
                        opt1 = "hide local timetable"
                    else:
                        opt1 = "show local timetable"
                    opt4 = "set platforms and station groups"
                    opt2 = "select station for local timetable"
                    opt3 = "mqtt timetables"
                    if 'timetable_triggered_gbl' not in globals():
                        reply = OptionDialog().customQuestionMessage4str(msg, title, opt0, opt4, opt2, opt3)
                    else:
                        reply = OptionDialog().customQuestionMessage5str(msg, title, opt0, opt1, opt4, opt2, opt3)
                    # print "reply", reply
                    if self.logLevel > 0: print "timetable_sensor active"
                    if reply == OptionDialog().CLOSED_OPTION or reply == opt0:
                        repeat = False
                    elif reply == opt1:
                        if run_local_timetable_gbl == True:
                            run_local_timetable_gbl = False
                        else:
                            run_local_timetable_gbl = True

                    elif reply == opt4:
                        gui7 = CreateAndShowGUI7()
                        # Create a latch that waits for CreateAndShowGUI7 frame to be closed
                        # In CreateAndShowGUI7 the latch is decremented whe  the frame is closed
                        global latch
                        latch = CountDownLatch(1)
                        # Wait until the frame is closed
                        latch.await()
                        reply = True

                    elif reply == opt2:
                        repeat1 = True
                        select_from_stations = True
                        while repeat1 == True:
                            if "station_name_list_gbl" not in globals():
                                station_name_list_gbl = ""
                            list_items_no_trains = self.get_scheduled_routes("no_train")
                            list_items_with_trains = self.get_scheduled_routes("with_train")
                            if select_from_stations:
                                title = "select station(s) for timetable"
                                options = ["Cancel", "Select from Station Groups", "Show Timetable"]
                                result = OptionDialog().MultipleListOptions(station_list, title, options, preferred_size = "default")
                            else:
                                title = "select station_group for timetable"
                                options = ["Cancel", "Select from Stations", "Show Timetable"]
                                result = OptionDialog().ListOptions(station_group_list, title, options, preferred_size = "default")
                            option = result[1]
                            if option == "Cancel" or self.od.CLOSED_OPTION == True:
                                run_local_timetable_gbl = False
                                self.timetable_sensor.setKnownState(INACTIVE)
                                repeat1 = False
                            elif option == "Show Timetable":
                                if select_from_stations:
                                    station_name_list_gbl = result[0]
                                    # get group_station_name
                                    if len(station_name_list_gbl) == 1:
                                        group_location_gbl = station_name_list_gbl[0]   # the first and only station  station1
                                    else:
                                        group_location_gbl = self.get_group_station_name(station_name_list_gbl)
                                else:
                                    group_location_gbl = result[0]
                                    # get station list
                                    station_name_list = []
                                    for l in station_group_location_list:
                                        if l[0] == group_location_gbl:
                                            station_name_list.append(l[1])
                                    station_name_list_gbl = station_name_list

                                run_local_timetable_gbl = True
                                self.ensure_conditions_for_timetable_to_show_are_met()
                                repeat1 = True
                            else:
                                if select_from_stations == True:
                                    select_from_stations = False
                                else:
                                    select_from_stations = True
                                repeat1 = True
                        repeat = True
                    elif reply == opt3:
                        repeat = True
                        while repeat:
                            title = "MQTT timetables"
                            msg = "timetables can be shown on remote computers/tablets communicating by mqtt\n\n" + \
                                  "scheduler needs to be on and clock running"
                            opt0 = "Cancel"
                            opt1 = "Generate mqtt timetable"
                            opt2 = "View/delete mqtt timetables"
                            opt3 = "Display mqtt timetables"

                            reply = OptionDialog().customQuestionMessage4str(msg, title, opt0, opt1, opt2, opt3)
                            if self.od.CLOSED_OPTION == True or reply == opt0:
                                repeat = False
                            elif reply == opt1:
                                select_from_stations = True
                                if "station_name_list_gbl" not in globals():
                                    station_name_list_gbl = ""
                                list_items_no_trains = self.get_scheduled_routes("no_train")
                                list_items_with_trains = self.get_scheduled_routes("with_train")
                                if select_from_stations:
                                    title = "select station(s) for timetable"
                                    options = ["Cancel", "Select from Station Groups", "Generate Timetable"]
                                    result = OptionDialog().MultipleListOptions(station_list, title, options, preferred_size = "default")
                                else:
                                    title = "select station_group for timetable"
                                    options = ["Cancel", "Select from Stations", "Generate Timetable"]
                                    result = OptionDialog().ListOptions(station_group_list, title, options, preferred_size = "default")
                                option = result[1]
                                if option == "Cancel" or self.od.CLOSED_OPTION == True:
                                    run_local_timetable_gbl = False
                                    self.timetable_sensor.setKnownState(INACTIVE)
                                    repeat = False
                                elif option == "Generate Timetable":
                                    if select_from_stations:
                                        station_name_list_mqtt_gbl = result[0]
                                        # get group_station_name
                                        if len(station_name_list_gbl) == 1:
                                            group_location_mqtt_gbl = station_name_list_gbl[0]   # the first and only station  station1
                                        else:
                                            group_location_mqtt_gbl = self.get_group_station_name(station_name_list_mqtt_gbl)
                                            print "group_location_mqtt_gbl", group_location_mqtt_gbl
                                    else:
                                        group_location_mqtt_gbl = result[0]
                                        # print "group_location_mqtt_gbl", group_location_mqtt_gbl
                                        # get station list
                                        station_name_list = []
                                        for l in station_group_location_list:
                                            if l[0] == group_location_mqtt_gbl:
                                                station_name_list.append(l[1])
                                        station_name_list_mqtt_gbl = station_name_list
                                        # print "station_name_list_mqtt_gbl", station_name_list_mqtt_gbl

                                    # get emblem
                                    title = "Display Train Operator Emblem?"
                                    emblem_list = ["GB (British Rail)", "Germany (DB)", "No Emblem"]
                                    options = ["Cancel", "Generate Timetable"]
                                    result = self.od.ListOptions(emblem_list, title, options, preferred_size = "default")
                                    # print "result", result
                                    train_operator_emblem = result[0]
                                    option1 = result[1]
                                    if option1 == "Cancel" or self.od.CLOSED_OPTION == True:
                                        # run_local_timetable_gbl = False
                                        self.timetable_sensor.setKnownState(INACTIVE)
                                    else:
                                        self.generate_node_red_code(station_name_list_mqtt_gbl, group_location_mqtt_gbl, train_operator_emblem)
                                        self.write_list2([train_operator_emblem])
                                    repeat = True
                                else:
                                    if select_from_stations == True:
                                        select_from_stations = False
                                    else:
                                        select_from_stations = True
                                    repeat = True
                            elif reply == opt2: # view/delete mqtt timetables

                                msg = "The generated timetables are saved in the following directory \n" +\
                                    "Delete any that are not required"
                                OptionDialog().displayMessage(msg)

                                self.view_delete_mqtt_timetables()
                                repeat = True

                            elif reply == opt3: # display mqtt timetables
                                msg = "Will attempt to display all generated timetables in default browser \n" +\
                                    "If Node Red is installed the timetables will be displayed \n" + \
                                    "If MQTT is set up in jmri and mosquitto is running the timetables will work \n" +\
                                    " when the scheduler is running"
                                OptionDialog().displayMessage(msg)

                                dmt = DisplayMqttTimetable()
                                repeat = True
                        repeat = True
                self.timetable_sensor.setKnownState(INACTIVE)

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

            # set time to midnight
            if self.logLevel > 0: print "set minute time listener"

            global minute_time_listener_setup
            if "minute_time_listener_setup" not in globals():
                minute_time_listener_setup = False
            if minute_time_listener_setup == False:
                self.setup_minute_time_listener_to_schedule_trains()    # only do this first time setup
            self.train_scheduler_setup = True

            self.scheduler_master_sensor.setKnownState(INACTIVE)

        if self.scheduler_view_scheduled_trains.getKnownState() == ACTIVE:
            self.show_operations_trains()
            self.scheduler_view_scheduled_trains.setKnownState(INACTIVE)

        if self.scheduler_edit_routes.getKnownState() == ACTIVE:
            self.show_routes()
            self.scheduler_edit_routes.setKnownState(INACTIVE)

        if self.scheduler_show_clock_sensor.getKnownState() == ACTIVE:
            self.show_analog_clock()
            self.scheduler_show_clock_sensor.setKnownState(INACTIVE)

        if self.help_sensor.getKnownState() == ACTIVE:
            self.display_help()
            self.help_sensor.setKnownState(INACTIVE)
        self.waitMsec(500)
        if self.logLevel > 0: print "end handle"
        # print "scheduling_in_operation_gbl 7 handle end", scheduling_in_operation_gbl
        # print "F"
        return True

    def mqtt_timetable_directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "mqtt_timetables"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator

    def view_delete_mqtt_timetables(self):

        dir = self.mqtt_timetable_directory()
        j = JFileChooser(dir);
        j.setAcceptAllFileFilterUsed(False)
        filter = FileNameExtensionFilter("json files json", ["json"])
        j.addChoosableFileFilter(filter);

        j.setDialogTitle("Delete not wanted mqtt timetables");

        ret = j.showDialog(None, "Delete");
        if ret == JFileChooser.APPROVE_OPTION:
            file = j.getSelectedFile()
            if file == "" or file == None:
                return
            if file.exists():
                os.remove(file.getAbsolutePath())
                # print("File " + file.getName() + " has been deleted successfully.")
            else:
                print("The selected file does not exist.")
        else:
            print("File selection cancelled.")
            return

    def get_group_station_name(self, station_name_list_gbl):

        # print "station_name_list_gbl", station_name_list_gbl
        concatenated_names = " ".join(station_name_list_gbl)    # [station1,station2]
        print "concatenated_names", concatenated_names

        station_groups = []
        for station_name in station_name_list_gbl:
            LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
            location = LocationManager.getLocationByName(station_name)
            station_group = MyTableModel7().get_location_station_group(location)
            print "station_name", station_name, "station_group", station_group.replace(" ", "*")
            if station_group != "" and station_group != " ":
                station_groups.append(station_group)
                # print "appending station_groups", station_groups
        counter = Counter(station_groups)
        print "counter", counter
        print "counter.most_common(1)", counter.most_common(1)
        most_common_item = " "
        if counter.most_common() != []:
            most_common_item = counter.most_common(1)[0][0]
            print "most_common_item", most_common_item

        if most_common_item != " ":
            group_location_gbl = most_common_item.strip()   # need an option to rename this
        else:
            group_location_gbl = concatenated_names
        print "group_location_gbl", group_location_gbl

        return group_location_gbl

    def ensure_conditions_for_timetable_to_show_are_met(self):
        global scheduling_in_operation_gbl
        global timebase
        global start_hour_gbl, end_hour_gbl
        if "timebase" not in globals():
            timebase = jmri.InstanceManager.getDefault(jmri.Timebase)

        if "start_hour_gbl" not in globals():
            start_hour_gbl = None
        if start_hour_gbl == None:     # check if Start Scheduler has been run
            sm = SchedulerMaster()     # if not set up everything which would have been run
            sm.set_default_scheduling_values()
            # sm.set_period_trains_will_run_frame()
            # sm.show_analog_clock()      # show the analog clock
            sm.setup_minute_time_listener_to_schedule_trains()
            sm.train_scheduler_setup = True

        if timebase.getRun() == False:
            # get the current time without using timebase
            date = jmri.implementation.DefaultClockControl().getTime()
            calendar = java.util.Calendar.getInstance()
            calendar.setTime(date)
            minute = calendar.get(calendar.MINUTE)

            # Manually create and trigger a PropertyChangeEvent
            # have to call it twice to get the timetable to display
            event = PropertyChangeEvent("TimeSource", "time",(minute - 2) % 60, (minute - 1) % 60)
            TimeListener().propertyChange(event)  # Directly call the listener's method
            event = PropertyChangeEvent("TimeSource", "time", (minute - 1) % 60, minute)
            TimeListener().propertyChange(event)
        else:
            # print "should be OK to show timetable"
            pass

    def generate_node_red_code(self, station_name_list, station_name, train_operator_emblem):
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
            # create a unique reference for the websocketout node
            import uuid
            id = str(uuid.uuid1())
            # change the file to work with station_name
            # websocket names cannot have spaces in them
            # so where we need to replace spaces we have My_Station$ instead of MyStation
            new_data = (data.replace('My_Station$_ref', id) \
                .replace('My_Station$', station_name.replace(" ","-")) \
                .replace("My_Station_List", str(station_name_list)) \
                .replace("My_Station", station_name) \
                .replace("My_Emblem", train_operator_emblem))
            # 'My_Station$' for url without spaces
            # "My_Station" for title
            # "My_Station_List" for list of stations to include


            # store the modified file to a file in the user directory
            new_node_red_template_directory = jmri.util.FileUtil.getExternalFilename('preference:dispatcher/mqtt_timetables/')

            #create the folder if it does not exist
            if not os.path.exists(new_node_red_template_directory):
                os.makedirs(new_node_red_template_directory)

            file_path = new_node_red_template_directory + java.io.File.separator + station_name + ".json"

            f = open(file_path, "w")
            f.write(new_data)
            f.close()
        else:
            OptionDialog().displayMessage(file_path + "does not exist, reinstall latest JMRI")

        msg = "node red file for '" + station_name + "' is in " + file_path + "\n" + \
              "import the file into a node_red instance on computer on same network \n" + \
              "and edit as illustrated in the help, and open indicated web page on tablet/laptop \n" + \
              "e.g. http://localhost:1880/" + station_name.replace(" ", "-") + ", where localhost should be replaced by network address \n" + \
              "of computer hosting node_red instance\n\n" + \
              "Note any spaces in the station name have been replaced by hyphens in the web address\n\n" + \
              "ALSO ensure an MQTT Connection is set up in preferences, as detailed in help."
        OptionDialog().displayMessage(msg)

    def set_departure_trains(self):
        # allow running of scheduled routes to set journey times and wait times,
        # which allow durations and departure times to be set up
        # print "set departure trains"
        global wait_time_gbl
        global scheduled
        global scheduling_margin_gbl
        global set_departure_trains_gbl
        global CreateAndShowGUI5_glb

        set_departure_trains_gbl = True
        self.set_default_scheduling_values()
        msg = "Set Departure Times"
        opt1 = "set wait time in stations"
        opt2 = "run train on route to set journey times"
        opt3 = "set departure times manually"
        reply = OptionDialog().customQuestionMessage3str(msg, "", opt1, opt2, opt3)
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
            list_items_no_trains = self.get_scheduled_routes("no_train")
            # print "list_items_no_trains", list_items_no_trains
            list_items_with_trains = self.get_scheduled_routes("with_train")
            # print "list_items_with_trains", list_items_with_trains
            list_items_starting_from_occupied_blocks = self.get_routes_starting_from_occupied_blocks()
            # print "list_items_starting_from_occupied_blocks", list_items_starting_from_occupied_blocks
            show_trains_in_occupied_blocks = True
            repeat = True
            while repeat:
                if show_trains_in_occupied_blocks:
                    title = "Select Route to Record Journey Times: Showing routes from occupied blocks"
                    options = ["Cancel", "Run Route", "Show all scheduled routes"]
                    reply1 = OptionDialog().ListOptions(list_items_starting_from_occupied_blocks, title, options, preferred_size = "default")
                else:
                    title = "Select Route to Record Journey Times: Showing scheduled routes"
                    options = ["Cancel", "Run Route", "Show routes starting from occupied blocks"]
                    reply1 = OptionDialog().ListOptions(list_items_no_trains, title, options, preferred_size = "default")

                my_list = reply1[0]
                route_name = str(my_list)
                option = str(reply1[1])

                if OptionDialog().CLOSED_OPTION == True or option == "Cancel":
                    # print "cancelling"
                    return
                elif option == "Run Route":
                    # print "Run Route"
                    train = [trn for [rte, trn] in list_items_with_trains if rte == route_name][0]
                    set_departure_times = True
                    param_scheduled_start = "00:00"
                    journey_time_row_displayed = True
                    if "CreateAndShowGUI5_glb" in globals():
                        if CreateAndShowGUI5_glb != None:
                            CreateAndShowGUI5_glb.frame.dispose()
                    CreateAndShowGUI5_glb = CreateAndShowGUI5(None, route_name, param_scheduled_start, journey_time_row_displayed = journey_time_row_displayed)
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
                    if self.logLevel > 0: print "running train %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", train.getDescription(), train.getName()
                    # running_train = RunTrain(train, g.g_stopping, set_departure_times)
                    route = train.getRoute()
                    routeName = route.getName()
                    station_from, station_to = self.get_first_and_last_station(route)   # starting from beginning of route
                    if self.logLevel > 0: print "station_from", station_from, "station_to", station_to
                    start_block = blocks.getBlock(station_from)
                    if self.logLevel > 0:  "start_block",start_block, "station_to", station_to
                    train_name = start_block.getValue()
                    no_repetitions = 0
                    delay_val = 0
                    # print "%%%%%%%%%%%%%%train_name%%%%%%%%%%%%%%", train_name
                    if train_name is None:
                        OptionDialog().displayMessage("No train is in the start position \n\n(maybe it has not been set up so the system recognises it)\npress setup train - register the train -  and try again")
                        return
                    else:
                        # print "train_name is not none"
                        pass

                    if "stopping" in train.getDescription():
                        if self.logLevel > 0: print "A"
                        run_train = RunRoute(route, g.g_stopping, station_from, station_to, no_repetitions, train_name, \
                                             set_departure_times = True)
                        run_train.setName("running_route_" + routeName)
                        instanceList.append(run_train)
                        run_train.start()
                    else:
                        if self.logLevel > 0: print "B"
                        run_train = RunRoute(route, g.g_express, station_from, station_to, no_repetitions, train_name, \
                                             set_departure_times = True)
                        run_train.setName("running_route_" + routeName)
                        instanceList.append(run_train)
                        run_train.start()
                    repeat = False
                else:
                    if show_trains_in_occupied_blocks == True:
                        show_trains_in_occupied_blocks = False
                    else:
                        show_trains_in_occupied_blocks = True
                    repeat = True

            return

        elif reply == opt3:

            list_items_no_trains = self.get_scheduled_routes("no_train")
            # print "list_items_no_trains", list_items_no_trains
            list_items_with_trains = self.get_scheduled_routes("with_train")
            # print "list_items_with_trains", list_items_with_trains
            list_items_all_routes = self.get_all_routes()
            # print "list_items_all_routes", list_items_all_routes
            show_all_routes = True
            repeat = True
            while repeat:
                if show_all_routes:
                    title = "Select Route to Record Journey Times: Showing all routes"
                    options = ["Cancel", "Set Departure Times", "Show scheduled routes"]
                    reply1 = OptionDialog().ListOptions(list_items_all_routes, title, options, preferred_size = "default")
                else:
                    title = "Select Route to Record Journey Times: Showing scheduled routes"
                    options = ["Cancel", "Set Departure Times", "Show all routes"]
                    reply1 = OptionDialog().ListOptions(list_items_no_trains, title, options, preferred_size = "default")

                my_list = reply1[0]
                option = reply1[1]
                route_name = str(my_list)

                option = str(option)
                if OptionDialog().CLOSED_OPTION == True or option == "Cancel":
                    # print "cancelling"
                    return
                elif option == "Set Departure Times":
                    param_scheduled_start = "00:00"
                    journey_time_row_displayed = True
                    if "CreateAndShowGUI5_glb" not in globals():
                        CreateAndShowGUI5_glb = CreateAndShowGUI5(None, route_name, param_scheduled_start, journey_time_row_displayed = journey_time_row_displayed)
                    else:
                        CreateAndShowGUI5_glb.frame.dispose()
                        CreateAndShowGUI5_glb = CreateAndShowGUI5(None, route_name, param_scheduled_start, journey_time_row_displayed = journey_time_row_displayed)
                    repeat = False
                else:
                    if show_all_routes == True:
                        show_all_routes = False
                    else:
                        show_all_routes = True
                    repeat = True

            return

    def get_first_and_last_station(self, route):

        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                              for routelocation in route.getLocationsBySequenceList() \
                                              if ".py" not in routelocation.getName()]

        if self.logLevel > 0: print "routelocationsSequenceNumber_list", routelocationsSequenceNumber_list
        first_routelocation = [routelocation \
                               for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                               ][0]
        last_routelocation = [routelocation \
                              for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                              ][-1]

        if self.logLevel > 0: print  "routelocation", routelocation
        return [str(first_routelocation), str(last_routelocation)]  # row number starts from 0

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
            my_list = [[train.getRoute().getName() if train is not None else "" , train] for train in train_list if train.getRoute() is not None]
        else:
            my_list = [train.getRoute().getName() if train is not None else "" for train in train_list if train.getRoute() is not None]
        return sorted(my_list)

    def get_routes_starting_from_occupied_blocks(self):
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route_list = RouteManager.getRoutesByNameList()
        my_list = [route.getName() for route in route_list if blocks.getBlock(route.getLocationsBySequenceList()[0].getName()).getSensor().getState() == ACTIVE ]
        return sorted(my_list)

    def get_all_routes(self):
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route_list = RouteManager.getRoutesByNameList()
        my_list = [route.getName() for route in route_list ]
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

    def set_period_trains_will_run_frame(self):
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl
        if self.frame == None:
            # print "frame is None"
            self.frame = jmri.util.JmriJFrame('Schedule Trains');

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
            rowStage1Title_1 = JLabel("Sets Up System to run hourly trains ")
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
            rowTitle_33.add(Box.createVerticalGlue())
            rowTitle_33.add(Box.createRigidArea(Dimension(30, 0)))
            rowTitle_33.add(rowStage1Title_1)

            # buttons

            rowStage1Button = JPanel()
            rowStage1Button.setLayout(BoxLayout(rowStage1Button, BoxLayout.X_AXIS))

            global rowrowStage4Button_4, rowAStage1Button_1,  rowBStage1Button_1, rowCStage1Button_1, rowDStage1Button_1, rowEStage1Button_1, rowFStage1Button_1
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

            stringToDisplay = "scheduling in operation: " + str(scheduling_in_operation_gbl)
            rowFStage1Button_1 = JLabel(stringToDisplay)
            rowFStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
            rowFStage1Button_1.add(Box.createHorizontalGlue());
            rowFStage1Button_1.setAlignmentX(rowFStage1Button_1.LEFT_ALIGNMENT)

            rowStage1Button_1 = JButton("Initialisation", actionPerformed = self.setup_schedule_parameters_panel)

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

            rowStage5Button = JPanel()
            rowStage5Button.setLayout(BoxLayout(rowStage5Button, BoxLayout.X_AXIS))
            rowrowStage5Button_5 = JLabel("<html>Initialise time, start scheduler, start time<br>Stop scheduler, stop time</html>")
            rowrowStage5Button_5.setFont(rowTitle_33.getFont().deriveFont(Font.BOLD, 13));
    
            rowrowStage5Button_5.add(Box.createHorizontalGlue());
            rowrowStage5Button_5.setAlignmentX(rowrowStage5Button_5.LEFT_ALIGNMENT)
            rowStage5Button_5 = JButton("Initialise", actionPerformed = self.StopStartScheduler_action)
            stage5Button = rowStage5Button_5

            rowStage6Button = JPanel()
            rowStage6Button.setLayout(BoxLayout(rowStage6Button, BoxLayout.X_AXIS))
            rowrowStage6Button_6 = JLabel("<html>Start scheduler, start time<br>Stop scheduler, stop time</html>")
            rowrowStage6Button_6.setFont(rowTitle_33.getFont().deriveFont(Font.BOLD, 13));
    
            rowrowStage6Button_6.add(Box.createHorizontalGlue());
            rowrowStage6Button_6.setAlignmentX(rowrowStage6Button_6.LEFT_ALIGNMENT)
            rowStage6Button_6 = JButton("   Run   ", actionPerformed = self.Running_StopStartScheduler_action)
            stage6Button = rowStage6Button_6

            rowStage1Button.add(Box.createVerticalGlue())
            rowStage1Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage1Button.add(rowStage1Button_1)
            rowStage1Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage1Button.add(rowrowStage1Button_1)

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

            rowStage5Button.add(Box.createVerticalGlue())
            rowStage5Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage5Button.add(rowStage5Button_5)
            rowStage5Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage5Button.add(rowrowStage5Button_5)

            rowStage6Button.add(Box.createVerticalGlue())
            rowStage6Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage6Button.add(rowStage6Button_6)
            rowStage6Button.add(Box.createRigidArea(Dimension(20, 0)))
            rowStage6Button.add(rowrowStage6Button_6)

        #Title
            # panel.add(self.leftJustify(rowTitle_2))
            panel.add(self.leftJustify(rowTitle_22))
            panel.add(self._create_separator_row())

            #stage1
            panel.add(self.leftJustify(rowStage1Button))
            panel.add(self.leftJustify(rowAStage1Button))
            panel.add(self.leftJustify(rowBStage1Button))
            panel.add(self.leftJustify(rowCStage1Button))
            panel.add(self.leftJustify(rowDStage1Button))
            panel.add(self.leftJustify(rowEStage1Button))
            panel.add(self._create_separator_row())

            #stage2
            panel.add(self.leftJustify(rowStage2Button))
            # panel.add(self.leftJustify(rowStage2Separator))

            panel.add(self.leftJustify(rowStage3Button))
            panel.add(self.leftJustify(rowFStage1Button))

            panel.add(self.leftJustify(rowStage4Button))
            panel.add(self._create_separator_row())
            panel.add(self.leftJustify(rowStage5Button))
            panel.add(self._create_separator_row())
            panel.add(self.leftJustify(rowStage6Button))
            panel.add(self._create_separator_row())

        self.frame.pack()
        self.frame.setVisible(True)
        self.frame.setSize(450, 400)
        self.frame.setLocation(10,10)

    def _create_separator_row(self):
        """Creates and returns a new separator panel object."""
        separatorPanel = JPanel()
        separatorPanel.setLayout(BoxLayout(separatorPanel, BoxLayout.X_AXIS))
        separatorLabel = JLabel("*******************************************************************")
        separatorLabel.add(Box.createHorizontalGlue())
        separatorLabel.setAlignmentX(separatorLabel.LEFT_ALIGNMENT)

        separatorPanel.add(Box.createVerticalGlue())
        separatorPanel.add(Box.createRigidArea(Dimension(20, 0)))
        separatorPanel.add(separatorLabel)
        separatorPanel.add(Box.createRigidArea(Dimension(20, 0)))
        separatorPanel.add(JLabel("")) # Empty label for spacing

        return self.leftJustify(separatorPanel)

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
        win.dispose()

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
        if "rowrowStage4Button_4" in globals():
            if timebase.getRun():
                state = "Started"
            else:
                state = "Stopped"
            stringToDisplay = "Stop/Start Clock:  " + state
            rowrowStage4Button_4.setText(stringToDisplay) # Update the label

    def ToggleSchedulingtrains_action(self, event):
        global schedule_trains_glb
        global scheduling_in_operation_gbl
        global rowFStage1Button_1

        # schedule_trains_glb = False
        if self.logLevel > 0: print "ToggleSchedulingtrains_action", "scheduling_in_operation_gbl", scheduling_in_operation_gbl
        #stop Scheduler

        if 'scheduling_in_operation_gbl' not in globals():
            scheduling_in_operation_gbl = "True"
            # print "scheduling_in_operation_gbl 8", scheduling_in_operation_gbl
        if scheduling_in_operation_gbl == "False":
            self.start_schedule_trains_master()
            scheduling_in_operation_gbl = "True"
        else:
            self.stop_schedule_trains_master()
            scheduling_in_operation_gbl = "False"
        # print "scheduling_in_operation_gbl 0", scheduling_in_operation_gbl

        stringToDisplay = "scheduling in operation: " + str(scheduling_in_operation_gbl)

        if 'rowFStage1Button_1' in globals():
            rowFStage1Button_1.setText(stringToDisplay) # Update the label
        if self.logLevel > 0: print "ToggleSchedulingtrains_action end", "scheduling_in_operation_gbl", scheduling_in_operation_gbl

    def StopStartClock_action(self, event):
        self.swap_timebase_state_run_stop()

    def StopStartScheduler_action(self, event):
        if scheduling_in_operation_gbl == "False":   # start scheduler
            self.ToggleSchedulingtrains_action(event)
            # if timebase.getRun() == True:          # set time
            self.SetTime_action(event)          # sets time and sets timebase to stop
            # if timebase.getRun() == False:
            self.swap_timebase_state_run_stop() # start timebase
        else:
            # stop scheduler (was running)
            self.ToggleSchedulingtrains_action(event)
            # leave time at same time as before
            # stop timebase
            self.set_timebase_state_stop()

    def Running_StopStartScheduler_action(self, event):
        if scheduling_in_operation_gbl == "False":   # start scheduler
            self.ToggleSchedulingtrains_action(event)
            if timebase.getRun() == False:
                self.swap_timebase_state_run_stop() # start timebase
        else:
            # stop scheduler (was running)
            self.ToggleSchedulingtrains_action(event)
            # leave time at same time as before
            # stop timebase
            self.set_timebase_state_stop()

    def swap_timebase_state_run_stop(self):
        global timebase
        global tListener
        global fast_clock_rate
        global rowrowStage4Button_4
        if timebase.getRun() == True:
            timebase.removeMinuteChangeListener(tListener)
            timebase.setRun(False)
            # print "fast_clock_rate", fast_clock_rate
        else:
            timebase.setRun(True)
            if tListener is None:
                tListener = TimeListener()
            timebase.addMinuteChangeListener(tListener)
        if "rowrowStage4Button_4" in globals():
            if timebase.getRun():
                state = "Started"
            else:
                state = "Stopped"
            stringToDisplay = "Stop/Start Clock:  " + state
            rowrowStage4Button_4.setText(stringToDisplay) # Update the label
        else:
            # print '"rowrowStage4Button_4" not in globals()'
            pass

    def set_timebase_state_stop(self):
        global timebase
        global tListener
        global fast_clock_rate
        global rowrowStage4Button_4

        timebase.removeMinuteChangeListener(tListener)
        timebase.setRun(False)

        if "rowrowStage4Button_4" in globals():
            state = "Stopped"
            stringToDisplay = "Stop/Start Clock:  " + state
            rowrowStage4Button_4.setText(stringToDisplay) # Update the label
        else:
            # print '"rowrowStage4Button_4" not in globals()'
            pass

    def start_schedule_trains_master(self):

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

    def stop_schedule_trains_master(self):
        global instanceList
        #stop all threads even if there are duplications

        summary = jmri.jmrit.automat.AutomatSummary.instance()
        automatsList = java.util.concurrent.CopyOnWriteArrayList()
        schedule_trains_master = ScheduleTrains()

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
        scheduling_in_operation_gbl1 = False   # change of logic do not set scheduling_in_operation_gbl here
        [start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
         scheduling_margin_gbl, scheduling_in_operation_gbl1] = self.read_list()
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

    def setup_schedule_parameters_panel(self, event):
        """Creates and displays a panel for setting the scheduler parameters."""
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

        # Create and set up the window.
        self.params_frame = JFrame("Set Schedule Parameters") #NOI18N
        self.params_frame.setPreferredSize(Dimension(550, 250))

        # Create and set up the panel using a simple grid layout.
        self.params_panel = JPanel(GridLayout(0, 2, 10, 5)) # hgap, vgap
        self.params_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)) # top, left, bottom, right

        # Add the widgets to the panel.
        self.add_schedule_parameter_widgets()

        # Set the panel to be the content pane of the window.
        self.params_frame.setContentPane(self.params_panel)

        # Display the window.
        self.params_frame.pack()
        self.params_frame.setVisible(True)

    class FieldValidator(FocusAdapter, ActionListener):
        """
        A listener that validates a text field when focus is lost or Enter is pressed.
        If validation fails, it reverts the field to its previous value.
        """
        def __init__(self, validation_function, field):
            self.validation_function = validation_function
            self.field = field
            self.old_value = None

        def focusGained(self, event):
            """Store the current value when the field gains focus."""
            self.old_value = self.field.getText()

        def _validate_and_revert(self):
            """Helper method to run validation and revert on failure."""
            if not self.validation_function(self.field):
                # The validation function already shows the error message.
                # Now, revert the text field to the last known good value.
                self.field.setText(self.old_value)

        def focusLost(self, event):
            """Validate when focus is lost."""
            self._validate_and_revert()

        def actionPerformed(self, event):
            """Validate when Enter is pressed."""
            self._validate_and_revert()

    def add_schedule_parameter_widgets(self):
        """Adds labels, fields, and buttons to the parameters panel."""

        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                     scheduling_margin_gbl, scheduling_in_operation_gbl

        # --- Create Widgets ---
        start_hour_label = JLabel("Start Hour (0-23):") #NOI18N
        self.start_hour_field = JTextField(4)

        end_hour_label = JLabel("End Hour (0-23):") #NOI18N
        self.end_hour_field = JTextField(4)

        fast_clock_label = JLabel("Fast Clock Rate (Running Period):") #NOI18N
        self.fast_clock_field = JTextField(4)

        fast_clock_off_label = JLabel("Fast Clock Rate (Non Running Period) (1-100):") #NOI18N
        self.fast_clock_off_field = JTextField(4)

        margin_label = JLabel("Scheduling Margin (Fast Mins, 0-20):") #NOI18N
        self.margin_field = JTextField(4)

        save_button = JButton("Save") #NOI18N
        cancel_button = JButton("Cancel") #NOI18N

        # --- Pre-fill fields with existing values ---
        # This assumes the values are stored as attributes of your class.
        # If they are globals, you would reference them directly.
        self.start_hour_field.setText(str(self.start_hour))
        self.end_hour_field.setText(str(self.end_hour))
        self.fast_clock_field.setText(str(fast_clock_rate))
        self.fast_clock_off_field.setText(str(self.speed_not_operational))
        self.margin_field.setText(str(self.scheduling_margin))

        # --- Add Listeners for real-time validation (on focus loss or Enter key) ---
        start_hour_validator = self.FieldValidator(self.validate_start_hour, self.start_hour_field)
        self.start_hour_field.addFocusListener(start_hour_validator)
        self.start_hour_field.addActionListener(start_hour_validator)

        end_hour_validator = self.FieldValidator(self.validate_end_hour, self.end_hour_field)
        self.end_hour_field.addFocusListener(end_hour_validator)
        self.end_hour_field.addActionListener(end_hour_validator)

        fast_clock_validator = self.FieldValidator(self.validate_fast_clock_rate, self.fast_clock_field)
        self.fast_clock_field.addFocusListener(fast_clock_validator)
        self.fast_clock_field.addActionListener(fast_clock_validator)

        fast_clock_off_validator = self.FieldValidator(self.validate_fast_clock_off_rate, self.fast_clock_off_field)
        self.fast_clock_off_field.addFocusListener(fast_clock_off_validator)
        self.fast_clock_off_field.addActionListener(fast_clock_off_validator)

        margin_validator = self.FieldValidator(self.validate_margin, self.margin_field)
        self.margin_field.addFocusListener(margin_validator)
        self.margin_field.addActionListener(margin_validator)

        # --- Add Action Listeners ---
        save_button.actionPerformed = self.save_schedule_parameters_action
        cancel_button.actionPerformed = self.cancel_schedule_parameters_action

        # --- Add Widgets to Panel in Order, wrapping fields to control size ---
        self.params_panel.add(start_hour_label)
        field_panel1 = JPanel(FlowLayout(FlowLayout.LEFT, 10, 0))
        field_panel1.add(self.start_hour_field)
        self.params_panel.add(field_panel1)

        self.params_panel.add(end_hour_label)
        field_panel2 = JPanel(FlowLayout(FlowLayout.LEFT, 10, 0))
        field_panel2.add(self.end_hour_field)
        self.params_panel.add(field_panel2)

        self.params_panel.add(fast_clock_label)
        field_panel3 = JPanel(FlowLayout(FlowLayout.LEFT, 10, 0))
        field_panel3.add(self.fast_clock_field)
        self.params_panel.add(field_panel3)

        self.params_panel.add(fast_clock_off_label)
        field_panel4 = JPanel(FlowLayout(FlowLayout.LEFT, 10, 0))
        field_panel4.add(self.fast_clock_off_field)
        self.params_panel.add(field_panel4)

        self.params_panel.add(margin_label)
        field_panel5 = JPanel(FlowLayout(FlowLayout.LEFT, 10, 0))
        field_panel5.add(self.margin_field)
        self.params_panel.add(field_panel5)

        # --- Add Buttons ---
        # Create a panel to hold both buttons, so they can be centered together.
        button_panel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 5)) # hgap=10, vgap=5
        button_panel.add(save_button)
        button_panel.add(cancel_button)

        # Add an empty label to the first column of the last row to push the button panel to the right.
        self.params_panel.add(JLabel(""))
        self.params_panel.add(button_panel)


    def save_schedule_parameters_action(self, event):
        """Saves the parameters back to the class and closes the window."""

        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl

        # --- Run all validations one last time before saving ---
        if not self.validate_start_hour(self.start_hour_field): return
        if not self.validate_end_hour(self.end_hour_field): return
        if not self.validate_fast_clock_rate(self.fast_clock_field): return
        if not self.validate_fast_clock_off_rate(self.fast_clock_off_field): return
        if not self.validate_margin(self.margin_field): return

        # All validation passed, now save the values.
        self.start_hour = int(self.start_hour_field.getText())
        self.end_hour = int(self.end_hour_field.getText())
        fast_clock_rate = int(self.fast_clock_field.getText())
        self.speed_not_operational = int(self.fast_clock_off_field.getText())
        self.scheduling_margin = int(self.margin_field.getText())

        items = [str(item) for item in [self.start_hour,self.end_hour, fast_clock_rate, \
                                        self.speed_not_operational, \
                                        self.scheduling_margin, scheduling_in_operation_gbl]]
        if self.logLevel > 0: print "items to write", items
        self.write_list(items)

        # store in globals

        start_hour_gbl = self.start_hour
        end_hour_gbl = self.end_hour
        speed_not_operational_gbl = self.speed_not_operational
        scheduling_margin_gbl = self.scheduling_margin

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

        print
        "Scheduler parameters saved."

        # Close the window after saving.
        self.params_frame.dispose()


    def cancel_schedule_parameters_action(self, event):
        """Closes the window without saving."""
        self.params_frame.dispose()

    # --- Validation Methods ---

    def validate_start_hour(self, field):
        """Validates the Start Hour field."""
        try:
            value = int(field.getText())
            if not (0 <= value <= 23):
                OptionDialog().displayMessage("Start Hour must be between 0 and 23.")
                return False
        except ValueError:
            OptionDialog().displayMessage("Start Hour must be a valid number.")
            return False
        return True

    def validate_end_hour(self, field):
        """Validates the End Hour field."""
        try:
            value = int(field.getText())
            if not (0 <= value <= 23):
                OptionDialog().displayMessage("End Hour must be between 0 and 23.")
                return False
        except ValueError:
            OptionDialog().displayMessage("End Hour must be a valid number.")
            return False
        return True

    def validate_fast_clock_rate(self, field):
        """Validates the Fast Clock Rate (Running) field."""
        try:
            value = int(field.getText())
            if value <= 0:
                OptionDialog().displayMessage("Fast Clock Rate (Running) must be a positive number.")
                return False
        except ValueError:
            OptionDialog().displayMessage("Fast Clock Rate (Running) must be a valid number.")
            return False
        return True

    def validate_fast_clock_off_rate(self, field):
        """Validates the Fast Clock Rate (Outside Hours) field."""
        try:
            value = int(field.getText())
            if not (1 <= value <= 100):
                OptionDialog().displayMessage("Fast Clock Rate (Outside Hours) must be between 1 and 100.")
                return False
        except ValueError:
            OptionDialog().displayMessage("Fast Clock Rate (Outside Hours) must be a valid number.")
            return False
        return True

    def validate_margin(self, field):
        """Validates the Scheduling Margin field."""
        try:
            value = int(field.getText())
            if not (0 <= value <= 20):
                OptionDialog().displayMessage("Scheduling Margin must be between 0 and 20.")
                return False
        except ValueError:
            OptionDialog().displayMessage("Scheduling Margin must be a valid number.")
            return False
        return True

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
        global minute_time_listener_setup

        if self.logLevel > 0: print "Setting up Time Scheduler"
        timebase = jmri.InstanceManager.getDefault(jmri.Timebase)

        # set up timebase and start at midnight
        if self.logLevel > 0: print "******************************************set timebase hour"

        self.set_default_scheduling_values()

        self.set_timebase_start_hour(int(start_hour_gbl)-1, 45)
        self.set_timebase_start_hour(12, 0)
        #
        # attach a listener to the timebase.
        tListener = TimeListener()


        # to set the rate, the timebase needs to be running
        mystate = timebase.getRun()
        if timebase.getRun() == False:
            self.swap_timebase_state_run_stop()
        timebase.userSetRate(float(fast_clock_rate))
        desired_state = False
        if False != timebase.getRun():
            self.swap_timebase_state_run_stop()

        minute_time_listener_setup = True
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

        global fast_clock_running_at_operational_speed
        if 'fast_clock_running_at_at_operational_speed' not in globals():
            fast_clock_running_at_operational_speed = True
        global minutes_old2, minutes_old3
        global schedule_trains_hourly
        global timetable_gbl, run_local_timetable_gbl
        global fast_clock_rate
        global send_mqtt_messages_gbl

        if 'minutes_old3' not in globals():
            minutes_old3 = 0
        # print "a"
        minutes_old = int(event.getOldValue())
        # print "b"
        minutes = int(event.getNewValue())
        # print "c", minutes

        if self.logLevel > 0: print "property change", "minutes", minutes,

        if 'minutes_old2' not in globals():
            minutes_old2 = minutes_old

        if self.logLevel > 0: print "before: minutes_old3", minutes_old3

        if self.logLevel > 0: print "minutes_old", minutes_old, "minutes", minutes, "minutes_old2", minutes_old2, \
            "(minutes_old - minutes_old2)", (minutes_old - minutes_old2), \
            "(minutes_old - minutes_old2) % 60 == 1", (minutes_old - minutes_old2) % 60 == -1

        # if self.logLevel > 0: print "property change", event.newValue
        if (minutes_old - minutes_old2) % 60 != 0:
            # when we set the fast clock in the event timer it triggers a new event at the same time
            # we then get into a recursion. This ignores the second call at the same time
            if self.logLevel > 0: print "two events are triggered with the same minutes"
        else:
            if self.logLevel > 0: print "timeListener: normal Operation"
            if self.logLevel > 0: print "minutes_old", minutes_old, "minutes", minutes, "minutes_old2", minutes_old2, \
                "(minutes - minutes_old2) % 60 ", (minutes - minutes_old2) % 60
            # print "y", minutes, type(minutes), int(minutes)
            if int(minutes) % 10 == 0:               # only check every 10 minutes to prevent problens at non_operational_speeds
                # don't just check at 0 minutes in case train us started not on the hour
                if self.logLevel > 0: print "minutes", int(minutes), "int(minutes) % 10", int(minutes) % 10, "minutes", minutes
                # print "e"
                minutes_old2 = minutes
                # print "set fast clock rate"
                self.set_fast_clock_rate()      # sets global fast_clock_at_operational_speed
            self.process_operations_trains(event)    # scheduled trains

            send_mqtt_messages_gbl = True
            if 'send_mqtt_messages_gbl' not in globals():
                send_mqtt_messages_gbl = False
            if 'run_local_timetable_gbl' not in globals():
                run_local_timetable_gbl = False
            # print "send_mqtt_messages_gbl", send_mqtt_messages_gbl
            if run_local_timetable_gbl or send_mqtt_messages_gbl:
                # do not display more frequently than 5 secs
                # print "f1", fast_clock_rate
                fcr = int(str(fast_clock_rate))
                x = (5.0 / 60.0)
                # print "x", x, "fcr", fcr, "x*fcr", x * fcr
                no_fast_minutes = int(x * fcr)
                if no_fast_minutes == 0: no_fast_minutes = 1
                if minutes % no_fast_minutes == 0:
                    Trigger_Timetable(minutes)
            else:
                # print "HIDING TIMETABLE WINDOW"
                # print "run_timetable_gbl", run_timetable_gbl, "send_mqtt_messages_gbl", send_mqtt_messages_gbl
                if 'timetable_gbl' in globals():
                    # timetable_gbl = None
                    if timetable_gbl != None:
                        timetable_gbl.hideWindow()

        minutes_old2 = minutes    # use minutes_old2 to prevent recursion
        minutes_old3 = minutes_old
        if self.logLevel > 0: print "after: minutes_old3", minutes_old3
        if self.logLevel > 0: print "end property change" ; print ""

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
        if self.logLevel > 0: print " ******   in process_operations_trains"
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
        train_list = [train for train in train_list if "skip" not in train.getComment()]  # exclude trains marked as 'don't schedule' in Scheduled trains table
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
        # trains_to_start = []
        if self.logLevel > 0: print "train_list", train_list, "trains_to_be_scheduled", trains_to_be_scheduled
        for train in train_list:
            comment = train.getComment()
            repeat_command = self.find_between(comment, "[repeat-", "-repeat]")
            if self.logLevel > 0: print "repeat_command", repeat_command
            max = int(minutes)
            min = (int(minutes) - 1)
            mid = int(train.getDepartTimeMinutes())

            if repeat_command == "Once":
                if self.prev_time < int(train.getDepartTimeMinutes()) <= self.curr_time and \
                        "skip" not in train.getDescription():   # if skip in description of scheduled Train do not run the train
                    if train not in trains_to_be_scheduled:
                        trains_to_be_scheduled.append(train)
                        scheduled[train] = False
            elif repeat_command == "Repeat every 20 mins":
                if max % 20 == 0:
                    min += 1; mid += 1; max += 1      # ensure mid lies between min amd max (ensure we don't have 59 < 0 <= 0)
                if (min % 20 < (mid % 20) <= max % 20):
                    if train not in trains_to_be_scheduled:
                        trains_to_be_scheduled.append(train)
                        scheduled[train] = False
            elif repeat_command == "Repeat every 30 mins":
                if max % 30 == 0:
                    min += 1; mid += 1; max += 1
                if (min % 30 < (mid % 30) <= max % 30):
                    if train not in trains_to_be_scheduled:
                        trains_to_be_scheduled.append(train)
                        scheduled[train] = False
            elif repeat_command == "Repeat every Hour":
                if self.logLevel > 0: print "min", min, "max", max, "mid", mid
                if max == 0:
                    min += 1; mid += 1; max += 1
                    if self.logLevel > 0: print "min", min, "max", max, "mid", mid
                if (min < (mid % 60) <= max):
                    if train not in trains_to_be_scheduled:
                        trains_to_be_scheduled.append(train)
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
                    if train not in trains_to_be_scheduled:
                        trains_to_be_scheduled.append(train)
                        scheduled[train] = False
            else:
                if self.logLevel > 0: print "incorrect repeat command", repeat_command
                # assume set to once
                if self.prev_time < int(train.getDepartTimeMinutes()) <= self.curr_time and \
                        "skip" not in train.getDescription():   # if skip in description of scheduled Train do not run the train
                    if train not in trains_to_be_scheduled:
                        trains_to_be_scheduled.append(train)
                        scheduled[train] = False
            if self.logLevel > 0: print "trains_to_be_scheduled", trains_to_be_scheduled

        # for train in trains_to_start:
        #     # print "x"
        #     if train not in trains_to_be_scheduled:
        #         trains_to_be_scheduled.append(train)
        #     scheduled[train] = False
        if self.logLevel > 0: print "Time listener: trains_to_be_scheduled", trains_to_be_scheduled
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
        global print_count
        timetable_triggered_gbl = True
        if minutes == None: return
        self.run(minutes)

    def run(self, minutes):
        t1 = Thread(target=self.send_timetable_and_clock_via_mqtt, args=(minutes,))
        t1.start()
        t1.join()

    def send_timetable_and_clock_via_mqtt(self, minutes):
        global station_name_list_gbl, group_location_gbl, run_local_timetable_gbl
        global timetable_gbl
        self.logLevel = 0
        global timebase, print_count
        # print "****************start send_timetable_and_clock_via_mqtt"
        hour = int(timebase.getTime().getHours())
        time = str(hour).zfill(2) + ":" + str(minutes).zfill(2)
        file = self.directory2() + "train_operator_emblem.txt"
        event = ""

        event = self.read_list2()[0]
        try:
            self.send_clock_message(hour, minutes, event)
        except:
            if "print_count" not in globals(): print_count = 0
            if print_count < 1: print "clock message not sent"
            print_count += 1
        if "station_name_list_gbl" not in globals():
            station_name_list_gbl = ""
        if "group_location_gbl" not in globals():
            group_location_gbl = ""
        # get list of origins, destinations and times at intermediate stations
        timetable = self.get_timetable(hour, minutes)
        if 'group_location_gbl' != "" and 'station_name_list_gbl' != "":
            station_name = group_location_gbl
            station_names_list = station_name_list_gbl
        else:
            station_name = 'Not Set'
            station_names_list = ['Not Set']

        self.generate_local_timetable(station_name, station_names_list, time, timetable)

        if "run_local_timetable_gbl" not in globals():
            print "run_local_timetable_gbl not defined"
            return

        if run_local_timetable_gbl == None:
            print "run_local_timetable_gbl None"
            return

        if run_local_timetable_gbl:
            if "timetable_gbl" in globals():
                if timetable_gbl != None:
                    timetable_gbl.frame.setVisible(True)
        else:
            if "timetable_gbl" in globals():
                if timetable_gbl != None:
                    timetable_gbl.frame.setVisible(False)
        try:
            self.send_timetable_messages(timetable)
        except:
            pass

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

        for train in train_list:
            if self.logLevel > 0: print "train", train, "train.getDescription()", train.getDescription()

            if train.getDescription() is not None and "skip" not in train.getDescription():
                if self.logLevel > 0: print "train", train, train.getDescription()
                comment = train.getComment()
                repeat = self.find_between(comment, "[repeat-", "-repeat]")
                departure_time_minutes = train.getDepartTimeMinutes() % 60
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

                        train_route_start_time = str(train_hour).zfill(2) + ":" + str(train_mins).zfill(2)
                        if self.logLevel > 0: print "train", train, train.getDescription(), "train_route_start_time", train_route_start_time
                        train_route = train.getRoute()
                        if train_route is not None:
                            if self.logLevel > 0: print "train_route", train_route
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
                                if self.logLevel > 0: print "duration_sec", duration_sec
                                if duration_sec != "":
                                    duration = ((float(duration_sec) * int(str(fast_clock_rate))) / 60.0)  # fast minutes
                                else:
                                    # print "setting duration 0"
                                    duration = 0
                                journey_duration += int(duration)

                            for i, route_location in enumerate(train_route.getLocationsBySequenceList()):
                                station_name = str(route_location.getName())
                                platform_name = str(MyTableModel7().get_location_platform(route_location.getLocation()))
                                if self.logLevel > 0: print "platform_name", platform_name
                                if platform_name == "":
                                    platform_name = station_name
                                if self.logLevel > 0: print "platform_name", platform_name
                                if self.logLevel > 2: print "****************************", station_name, "***************************"
                                if ".py" in station_name:   # exclude actions
                                    break
                                timetable_entry_names = ["train_name", "station_name", "station_departure_time", "last_station",
                                                         "last_station_arrival_time", "via"]
                                # remove items from start of via list, and the destination
                                locations1 = train_route.getLocationsBySequenceList()
                                locations = [str(loc) for loc in locations1]

                                via = locations[i+1:-1]
                                via = [location2 for location2 in via if ".py" not in location2]
                                if via == []:
                                    via = "-"
                                if i != 0 and last_station == station_name:
                                    via = ["Terminates Here"]
                                via = str(via).replace('[','').replace(']','').replace("'", "")

                                comment = route_location.getComment()
                                if i == 0:
                                    station_departure_time = train_route_start_time
                                    time_to_station = 0
                                else:
                                    duration_sec = self.find_between(comment, "[duration_sec-", "-duration_sec]")
                                    if str(duration_sec) == "":
                                        duration_sec = 0

                                    if self.logLevel > 2: print route_location, "duration_sec", duration_sec
                                    duration = float((float(duration_sec) * int(str(fast_clock_rate))) / 60.0)
                                    if self.logLevel > 2: print route_location, "duration", duration

                                    time_to_station = int(duration)
                                    if self.logLevel > 2: print "time_to_station", time_to_station

                                previous_departure_time = station_departure_time
                                if self.logLevel > 0:
                                    print train_name, "previous_departure_time", previous_departure_time
                                station_departure_time = self.add_times(station_departure_time, time_to_station)
                                if self.logLevel > 0: print train_name, route_location, "station_departure_time", station_departure_time

                                # if the wait_time and journey_time have been set we can set the arrival time
                                wait_time = self.find_between(comment, "[wait_time-", "-wait_time]")
                                journey_time = self.find_between(comment, "[journey_time-", "-journey_time]")
                                if journey_time == "":
                                    journey_time = "0"
                                # convert journey time to fast minutes
                                journey_time_fast_mins = ((float(str(journey_time)) * int(str(fast_clock_rate))) / 60.0)
                                if wait_time != "" and journey_time != "":
                                    if i == -1:
                                        station_arrival_time = station_departure_time
                                    else:
                                        if self.logLevel > 0: print train_name, route_location, "journey_time", journey_time
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

                                if i != 0 and last_station == station_name:
                                    station_departure_time = ""

                                # make sure we don't display trains that have a departure time > current time
                                # keep departures up for 1 fast minutes after the departure time
                                if self.curr_time - 1 < station_departure_time_in_mins:

                                    timetable_entry = [train_name, \
                                                       station_name , \
                                                       platform_name , \
                                                       station_arrival_time, \
                                                       station_departure_time, \
                                                       first_station, \
                                                       last_station, \
                                                       via]

                                    timetable.append(timetable_entry)
        #sort timetable by time
        timetable.sort(key = lambda row: max(row[3],row[4]))
        return timetable

    def generate_local_timetable(self, station_name, station_names_list, time, timetable):
        global timetable_gbl
        if "timetable_gbl" not in globals():
            timetable_gbl = Timetable(station_name)
            timetable_gbl.update_timetable(station_name, station_names_list, time, timetable)
        elif timetable_gbl == None:
            timetable_gbl = Timetable(station_name)
            timetable_gbl.update_timetable(station_name, station_names_list, time, timetable)
        else:
            # update the Timetable, Timetable class has already been initiated
            timetable_gbl.update_timetable(station_name, station_names_list, time, timetable)

    def send_timetable_messages(self,timetable):
        i = 0
        msg = "["
        for [train_name, \
             station_name , \
             platform , \
             station_arrival_time, \
             station_departure_time, \
             first_station, \
             last_station, \
             via] in timetable:
            msg += '{"type" : "' + "schedule" + '", ' + \
                   '"train_name" : "' + str(train_name) + '", ' + \
                   '"station_name" : "' + str(station_name) + '", ' + \
                   '"platform" : "' + str(platform) + '", ' + \
                   '"station_arrival_time" : "' + str(station_arrival_time) + '", ' + \
                   '"station_departure_time" : "' + str(station_departure_time) + '", ' + \
                   '"first_station" : "' + str(first_station) + '", ' + \
                   '"last_station" : "' + str(last_station) + '", ' + \
                   '"via" : "' + str(via) + '"},'
            i += 1
        msg = msg[:-1]
        msg += "]"
        self.send_mqtt_message(msg)

    def send_mqtt_message(self, msg):
        global print_count
        try:
            # Find the MqttAdapter
            mqttAdapter = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()
            # create content to send "/jmri/timetable message content"
            topic = "jmri/timetable"
            payload = msg
            mqttAdapter.publish(topic, payload)
        except:
            if "print_count" not in globals(): print_count = 0
            if print_count < 1: print "failure mqtt message"
            print_count += 1

    def send_clock_message(self, hour, minutes, event):

        msg = '[{"type" : "' + "clock" + '", ' + \
              '"time" : "' + str(hour).zfill(2) + ":" + str(minutes).zfill(2) + '",' \
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

class RunRoute(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self, route, graph, station_from, station_to, no_repetitions, train_name, \
                 delay = 0, scheduling_train = False, set_departure_times = False, train = None):

        # print ("route" , route, "station_from", station_from, "station_to", station_to, \
        #                "no_repetitions", no_repetitions, "train_name", train_name, \
        #                "scheduling_train", scheduling_train)

        # station_from is set to the initial position of the train, not necessarily
        # the start position of the route
        # station_to is set only if returning to start position
        # in that case it is set to station_from
        # the route gives the stations on the route

        # note station_to and station_from are strings, while elements of route are locations
        self.logLevel = 0
        if self.logLevel > 0: print "loglevel", self.logLevel

        if self.logLevel > 0: print "in init RunRoute"
        if self.logLevel > 0: print route, station_from, station_to, no_repetitions, train_name, delay

        self.delay = delay
        self.scheduling_train = scheduling_train
        self.set_departure_times = set_departure_times
        if self.logLevel > 0: print "set_departure_times", set_departure_times
        self.route = route
        self.train = train   # only used if scheduling_train = True
        self.train_name = train_name

        if route is None or train_name is None:
            self.mycount = None
            self.no_repetitions = -1
            if self.logLevel > 0: print "RunRoute: route == ", route, " train_name == ", train_name
        else:
            if self.logLevel > 0: print "RunRoute: route =", route
            self.graph = graph
            self.station_from = station_from
            self.station_to = station_to
            self.no_repetitions = no_repetitions
            self.mycount = 0
            # self.train_name_in = train_name
            self.train_name = train_name        # null if train not in start block

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

            if self.logLevel > 0: print "self.station_comment_list", self.station_comment_list

            # ignore the number of repetitions if station_to was not set to station_from
            if self.station_list[0] != self.station_list[-1]:
                self.no_repetitions = 0

    def handle(self):
        if self.route is not None and self.train_name is not None:
            if self.logLevel > 0: print "in handle", self.mycount
            if self.delay > 0 and self.mycount == 0:  # only delay on the first iteration
                self.waitMsec(self.delay)
            if int(self.mycount) <= int(self.no_repetitions):
                # print "repeating", "self.mycount", self.mycount, "self.no_repetitions", self.no_repetitions
                if self.logLevel > 0: print "station_list in handle", self.station_list, "in handle", self.mycount
                response = self.run_route(self.train_name)

                if self.logLevel > 0: print "prepended", self.prepended
                if self.mycount == 0 and self.prepended:
                    if self.logLevel > 0: print "station_list before pop", self.station_list
                    self.station_list.pop(0)
                    self.station_comment_list.pop(0)
                    if self.logLevel > 0: print "station_list after pop", self.station_list
                if self.logLevel > 0: print "returning true", "train_name", self.train_name, "mycount", self.mycount, "reps" , self.no_repetitions

                self.mycount += 1     # 0 first time round
                return True
            else:
                if self.logLevel > 0: print "returning false", "train_name", self.train_name, "mycount", self.mycount, "reps" , self.no_repetitions
                return False

    def run_route(self, train_to_move):
        global check_action_route_flag
        global fast_clock_rate
        if self.logLevel > 0: print "************************************run train******************"
        if self.logLevel > 0:  print "!     start run_route"

        station_from = None
        prev_station_index = 0
        for station_index, station in enumerate(self.station_list):
            # print "station_index", station_index, "station", station, "train_to_move", train_to_move
            if self.logLevel > 0: print "self.scheduling_train", self.scheduling_train

            # do action if one has been requested
            if self.station_is_action(station):  #if the station_name is a python_file
                action = station
                self.execute_action(action)     # execute the python file
            else:
                # print "running at station", "station_from", station_from
                station_to = station  # both now strings
                if station_from != None:    # first time round station_from is not set up
                    # find the accumulated durations
                    if self.scheduling_train or self.set_departure_times:
                        durations = [MyTableModel5().find_between(comment, "[duration_sec-", "-duration_sec]")
                                     for comment in self.station_comment_list]
                        # print "durations 4", durations
                        accumulated_durations = []
                        total = 0
                        for n in durations:
                            try:
                                total += int(n)
                            except:
                                pass
                            accumulated_durations.append(total)
                        if station_index != self.find_row_first_location(self.route):
                            previous_station_index = self.find_row_prev_location(station_index, self.route)
                            station_comment = self.station_comment_list[previous_station_index]  # do not use prev_station_index as it includes actions
                            accumulated_duration = accumulated_durations[previous_station_index]
                        else:
                            station_comment = None
                            accumulated_duration = 0

                        if self.logLevel > 0: print "station", station, "station_comment", station_comment, \
                            "station_index", station_index, "prev_station_index", prev_station_index
                        if self.logLevel > 0: print "accumulated_duration", accumulated_duration

                    if self.logLevel > 0:  print self.route.getName(), "!     moving from", station_from, "to", station_to

                    self.station_from_name = station_from
                    self.station_to_name = station_to
                    start_block = blocks.getBlock(station_from)

                    if self.logLevel > 0:  "start_block",start_block, "station_to", station_to
                    if self.logLevel > 0: print "calling move_between_stations","station_from",station_from,"station_to",station_to,"train_to_move",train_to_move
                    if self.set_departure_times:
                        # print "setting previous time"
                        previous_time = int(round(time.time()))  # in secs

                    if self.scheduling_train:

                        self.wait_for_scheduled_time(self.route, previous_station_index, accumulated_duration, train_to_move)
                        # done when we know the transit name
                        # print "__________________________Start__" + train_to_move + "___________________________________"
                        strpad = station_index * "  "
                        success = self.check_train_in_block_for_scheduling_margin_fast_minutes(start_block, train_to_move, strpad)
                        if success:
                            move_train = MoveTrain(station_from, station_to, train_to_move, self.graph, mode = "scheduling", route = self.route)
                            move_train.move_between_stations(station_from, station_to, train_to_move, self.graph, mode = "scheduling")
                            transit_name = move_train.transit_name
                            print "__________________________End____" + train_to_move + "__transit: " + transit_name
                        else:
                            print "failed to move train - no train in block - have waited for scheduling margin"
                            transit_name = move_train.transit_name
                            print "__________________________End____" + train_to_move + "__transit: " + transit_name
                        # success = self.check_train_in_block_allow_manual_repositioning(train_to_move, self.station_from_name)
                        # if success:
                        #     move_train = MoveTrain(station_from, station_to, self.train_name, self.graph, route = self.route)
                        #     move_train.move_between_stations(station_from, station_to, self.train_name, self.graph)
                    # move_train = None

                    # train has moved, if we are in departure_time_setting mode, store the journey time
                    if self.logLevel > 0: print "about to store departure times"
                    if self.set_departure_times:
                        if self.logLevel > 0: print "storing departure times"
                        current_time = int(round(time.time()))  # in secs
                        journey_time_in_secs = current_time - previous_time
                        print "previous_time", previous_time, "current_time", current_time
                        if self.logLevel > 0: print "before store", "journey_time_in_secs", journey_time_in_secs
                        self.store_journey_time(self.route, station_index, str(journey_time_in_secs))
                        if self.logLevel > 0: print "after store"

                    if self.logLevel > 0: print "finished move between stations station_from = ", station_from, " station_to = ", station_to
                    end_block = blocks.getBlock(station_to)  #do following in case the block sensor is a bit dodgy
                    end_block.setValue(self.train_name)

                check_action_route_flag = False     # This flag may have been set by the action appearing in the route
                # before this move. It has to be reset.
                # print "check_action_route_flag reset", check_action_route_flag
                station_from = station_to
                prev_station_index = station_index

        if self.scheduling_train:
            fast_minute = 1000*60/int(str(fast_clock_rate))
            self.waitMsec(fast_minute)
        else:
            self.waitMsec(4000)

        if self.logLevel > 0:  print "!     finished run_train"

    def find_row_first_location(self, route):
        # print "find_row_first_location"
        # get the row (sequenceNo) of the first location that is not an action (a python file  xx.py)
        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                              for routelocation in route.getLocationsBySequenceList() \
                                              if ".py" not in routelocation.getName()]

        # print "routelocationsSequenceNumber_list", routelocationsSequenceNumber_list
        current_val = [[routelocation, sequenceNo] \
                       for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                       if 1 == sequenceNo][0]
        # print "current_val", current_val

        current_index = routelocationsSequenceNumber_list.index(current_val)
        # print "current_index", current_index
        # print "routelocations_list", routelocations_list, "index", index
        try:
            [routelocation, row] = routelocationsSequenceNumber_list[current_index]
            # row = routelocationsSequenceNumber_list[currentIndex + 1]
        except:
            row = None
        # print "row", row
        return row - 1    # row number starts from 0

    def find_row_prev_location(self, row, route):
        # get the row (sequenceNo) of the first location that is not an action (a python file  xx.py)
        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                              for routelocation in route.getLocationsBySequenceList() \
                                              if ".py" not in routelocation.getName()]
        current_val_list = [[routelocation, sequenceNo] \
                       for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                       if row == sequenceNo-1]

        current_val = current_val_list[0]
        current_index = routelocationsSequenceNumber_list.index(current_val)
        [routelocation, row] = routelocationsSequenceNumber_list[current_index - 1]

        return row - 1     # row number starts from 1

    def check_train_in_block_for_scheduling_margin_fast_minutes(self, start_block, train_to_move, strpad = ""):
        global scheduling_margin_gbl
        global timebase
        # print "__________________________Start__" + train_to_move + "___________________________________"
        # timehm = str(timebase.getTime().getHours()) + ":" + str(timebase.getTime().getMinutes())
        timehm = "{:02d}:{:02d}".format(timebase.getTime().getHours(), timebase.getTime().getMinutes())
        print timehm, strpad, "check_train", train_to_move, "in_block", start_block.getUserName(), "for", scheduling_margin_gbl, "fast_minutes"
        global fast_clock_rate
        if self.logLevel > 0: print "check_train_in_block_for_scheduling_margin_fast_minutes"

        for j in range(int(scheduling_margin_gbl)):    # try to schedule train for scheduling_margin_gbl fast minutes
            train_in_block = self.blockOccupied(start_block)
            train_block_name = start_block.getValue()
            if train_in_block and (train_block_name == train_to_move):
                # print strpad + "--" + str(train_to_move) + " train in start block " + str(start_block.getUserName())
                return True
            else:
                print (strpad + "--" + str(train_to_move) + " not in start_block: " + str(start_block.getUserName()) \
                        + " time waited: " + str(j) + " fast minutes")
                fast_minute = 1000*60/int(str(fast_clock_rate))
                self.waitMsec(fast_minute)
        return False

    def check_train_in_block_allow_manual_repositioning(self, train_name, station_from_name):
        i = 0
        # print "train_name", train_name, "station_from_name", station_from_name
        while self.check_train_in_start_block(train_name, station_from_name) == False:
            if i > 2: # allow some time to recover
                title = ""
                msg = "Cannot run train, train not in start block\n" + \
                      train_name + " should be in block " + station_from_name + \
                      "\nmove it there manually and it might recover"
                opt1 = "have moved train, try again"
                opt2 = "cancel moving train"
                reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
                if reply == opt1:  # "have moved train, try again"
                    i = -1
                else:  # opt2
                    return False   # "cancel moving train"
            self.waitMsec(5000)
            i += 1
        # move_train = MoveTrain(station_from, station_to, train_to_move, self.graph, station_comment)
        # move_train.move_between_stations(station_from, station_to, train_to_move, self.graph)
        return True

    def store_journey_time(self, route, row, journey_time):
        global CreateAndShowGUI5_glb
        routeLocationList = route.getLocationsBySequenceList()
        routeLocation = routeLocationList[row]
        print "routeLocation", routeLocation, "row", row, "value", journey_time, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5"
        # OptionDialog().displayMessageNonModal("about to set_value_in_comment", "OK")
        self.set_value_in_comment(routeLocation, journey_time, "journey_time")
        wait_time = self.get_value_in_comment(routeLocation, "wait_time")
        duration_sec = int(journey_time) + int(wait_time)
        self.set_value_in_comment(routeLocation, duration_sec, "duration_sec")
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

    def get_value_in_comment(self, routeLocation, duration_string):

        delim_start = "[" + duration_string + "-"
        delim_end = "-" + duration_string + "]"

        comment = routeLocation.getComment()
        value = self.find_between(comment, "[" + duration_string + "-", "-" + duration_string + "]")

        return value

    def find_between(self, s, first, last):
        try:
            start = s.index(first) + len(first)
            end = s.index(last, start)
            return s[start:end]
        except ValueError:
            return ""

    def wait_for_scheduled_time(self, route, row, accumulated_durations, train_to_move):
        global fast_clock_rate
        global timebase
        global scheduling_margin_gbl
        if 'timebase' not in globals():
            timebase = jmri.InstanceManager.getDefault(jmri.Timebase)

        routeLocationList = route.getLocationsBySequenceList()
        routeLocation = routeLocationList[row]
        train_comment = self.train.getComment()
        if self.logLevel > 0: print "train_to_move", train_to_move, "routeLocation", routeLocation
        comment = routeLocation.getComment()
        # print "train_comment", train_comment, "comment", comment
        if self.logLevel > 0: print "x1"
        repeat_command = TimeListener().find_between(train_comment, "[repeat-", "-repeat]")
        if self.logLevel > 0: print "x3"
        current_minutes = int(timebase.getTime().getMinutes())
        if self.logLevel > 0: print "x2", "current_minutes", current_minutes

        # the departure time for the train is the first one in the hour
        # get what would be the current time if we were running the first train in the hour
        # so we can get the difference of the two and hence get the wait time
        # this is easier than getting the actual departure time minutes of the train
        if repeat_command == "Once":
            current_minutes_mod = current_minutes
            current_minutes_mod_array = [current_minutes_mod]
        elif repeat_command == "Repeat every 20 mins":
            current_minutes_mod = current_minutes % 20
            cmm20 = self.add_minutes(current_minutes_mod, 20)
            cmm40 = self.add_minutes(current_minutes_mod, 40)
            current_minutes_mod_array = [current_minutes_mod, cmm20, cmm40]
        elif repeat_command == "Repeat every 30 mins":
            current_minutes_mod = current_minutes % 30
            cmm30 = self.add_minutes(current_minutes_mod, 30)
            current_minutes_mod_array = [current_minutes_mod, cmm30]
        elif repeat_command == "Repeat every Hour":
            current_minutes_mod = current_minutes
            current_minutes_mod_array = [current_minutes_mod]
        elif repeat_command == "Repeat every 2 Hours":
            current_minutes_mod = current_minutes
            current_minutes_mod_array = [current_minutes_mod]
        else:
            print "error wrong repeat command"
            return
        if self.logLevel > 0: print "d", current_minutes, "current_minutes_mod", current_minutes_mod
        current_hour = int(str(timebase.getTime().getHours()))
        if self.logLevel > 0: print("a"), current_hour
        current_time = str(current_hour).zfill(2) + ":" + str(current_minutes_mod).zfill(2)
        if self.logLevel > 0: print "c", current_time
        train_start_time = self.train.getDepartureTime()
        if self.logLevel > 0: print "train_start_time", train_start_time, "accumulated_durations", accumulated_durations
        accumulated_durations_fast_mins = (accumulated_durations / 60.0) * int(str(fast_clock_rate)) # convert to fast min
        if self.logLevel > 0: print "accumulated_durations_fast_mins", accumulated_durations_fast_mins
        station_start_time = self.add_minutes_to_time(train_start_time, accumulated_durations_fast_mins)
        [station_start_hours, station_start_mins] = station_start_time.split(":")

        if self.logLevel > 0: print "station_start_time", station_start_time
        if self.logLevel > 0: print "current_time", current_time, "station_start_time", station_start_time

        #calculate minutes to wait
        minutes_to_wait_array = [self.subtract_minutes(station_start_mins, cm) for cm in current_minutes_mod_array]
        if self.logLevel > 0: print "minutes_to_wait_array", minutes_to_wait_array

        minutes_to_wait = min(minutes_to_wait_array)
        if self.logLevel > 0: print "minutes_to_wait", minutes_to_wait

        minutes_late = max([m-60 for m in minutes_to_wait_array]) # get the minutes late
        abs_minutes_late = abs(minutes_late)
        if self.logLevel > 0: print "minutes_late", minutes_late
        if self.logLevel > 0: print "minutes_late", abs_minutes_late

        index = minutes_to_wait_array.index(minutes_to_wait)
        if abs_minutes_late < int(scheduling_margin_gbl):
            if self.logLevel > 0: print "minutes_late2", minutes_late, "scheduling_margin_gbl", scheduling_margin_gbl
            fast_ms_to_wait = 0
        else:
            if self.logLevel > 0: print "minutes_to_wait", minutes_to_wait
            fast_ms_to_wait =  abs(minutes_to_wait) * 60 * 1000
            if self.logLevel > 0: print "waiting for ", current_minutes_mod_array[index]
        if self.logLevel > 0: print "v"
        ms_to_wait = fast_ms_to_wait / int(fast_clock_rate)
        if self.logLevel > 0: print "w"
        if self.logLevel > 0: print "waiting", "minutes_to_wait", minutes_to_wait
        if self.logLevel > 0: print "waiting", "ms_to_wait", ms_to_wait, "fast_secs_to_wait", fast_ms_to_wait/1000
        if self.logLevel > 0: print "time before wait", str(timebase.getTime())
        self.waitMsec(ms_to_wait)
        if self.logLevel > 0: print "time after wait", str(timebase.getTime())
        if self.logLevel > 0: print "waited till start time"

    def add_minutes(self, min1, min2):
        min = (int(min1) + int(min2) ) % 60
        return min

    def add_minutes_to_time(self, time, minutes):

        if self.logLevel > 0: print "z", time, minutes

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

    def subtract_minutes(self, min1, min2):
        wait_time = int(min1) - int(min2)
        if self.logLevel > 0: print "wait_time", wait_time
        wait_time = wait_time % 60
        if self.logLevel > 0: print "wait_time", wait_time
        return wait_time

    def subtract_times_neg(self, current_time, station_time):
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
        return wait_time - 60

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
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "actions"
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
        if self.logLevel > 0: print self.route.getName()," executing file", file
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
        global fast_clock_rate
        if self.logLevel > 0 : print "************************************run trains******************"
        # schedule_trains_glb = True
        if True:
            if 'fast_clock_running_at_operational_speed' not in globals():
                fast_clock_running_at_operational_speed = True
            if self.logLevel > 0 : print "run trains started: loop: scheduled trains", trains_to_be_scheduled, \
                "fast_clock_running_at_operational_speed", fast_clock_running_at_operational_speed
            # print "scheduled", scheduled
            if fast_clock_running_at_operational_speed:
                for train in trains_to_be_scheduled:
                    # print "train", train, "type", type(train)
                    if train.getDescription() is not None:
                        # print "description", train.getDescription()
                        # print "scheduled[train]", scheduled[train]
                        if scheduled[train] == False:
                            if self.logLevel > 0: print "train",train,"scheduled[train]",scheduled[train]
                            route = train.getRoute()
                            if route is not None:
                                routeName = route.getName()
                                station_from, station_to = SchedulerMaster().get_first_and_last_station(route)   # starting from beginning of route
                                start_block = blocks.getBlock(station_from)
                                if self.logLevel > 0:  "start_block",start_block, "station_to", station_to
                                # train_block_name = start_block.getValue()
                                train_to_be_scheduled = train.getDescription()
                                # this is obsolete: now checked in check_train_in_block_for_scheduling_margin_fast_minutes in run_route in class RunRoute
                                # run_route_flag = self.check_train_ok_to_start(train, train_block_name)
                                no_repetitions = 0
                                if True:
                                    if "stopping" in train.getDescription():
                                        # print "running train %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", train
                                        run_train = RunRoute(route, g.g_stopping, station_from, station_to, no_repetitions, train_to_be_scheduled, \
                                                             scheduling_train = True, train = train)
                                        run_train.setName("running_route_" + routeName)
                                        instanceList.append(run_train)
                                        run_train.start()
                                    else:
                                        # print "running train %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", train
                                        run_train = RunRoute(route, g.g_express, station_from, station_to, no_repetitions, train_to_be_scheduled, \
                                                             scheduling_train = True, train = train)
                                        run_train.setName("running_route_" + routeName)
                                        instanceList.append(run_train)
                                        run_train.start()
                                    scheduled[train] = True
                                    trains_to_be_scheduled.pop(trains_to_be_scheduled.index(train))
                                    if self.logLevel > 0: print "scheduled train ", train
                if self.logLevel > 0:  print "!!!!!!!!!!!!!!!!!!!!!run_trains finished"
                if self.logLevel > 0:  print "trains_to_be_scheduled ", trains_to_be_scheduled
                if 'timebase' in globals():
                    if self.logLevel > 0:  print "timebase.getRate()",timebase.getRate()


        msecs_in_half_fast_minute = int(500.0 / float(str(fast_clock_rate)) * 60.0)
        # noMsec = int(1000/timebase.getRate())
        self.waitMsec(msecs_in_half_fast_minute)  # twice every fast minute

    def check_train_ok_to_start(self, train, train_block_name):

        # check the scheduled train is in the starting block
        if train_block_name == train.getDescription():
            # print "returning true"
            return True
        else:
            # print "returning false", "train_block_name", train_block_name, "train.getDescription()", train.getDescription()
            return False
