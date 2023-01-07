from javax.swing import JTable, JScrollPane, JFrame, JPanel, JComboBox,  BorderFactory, DefaultCellEditor, JLabel, UIManager, SwingConstants, JFileChooser
from javax.swing.table import  TableCellRenderer, DefaultTableCellRenderer
from java.awt.event import MouseAdapter,MouseEvent, WindowListener, WindowEvent
from java.awt import GridLayout, Dimension, BorderLayout, Color
from javax.swing.table import AbstractTableModel, DefaultTableModel
from java.lang.Object import getClass
import jarray
from javax.swing.event import TableModelListener, TableModelEvent
from javax.swing.filechooser import FileNameExtensionFilter
from org.apache.commons.io import FilenameUtils
from java.io import File

global g
class ResetButtonMaster(jmri.jmrit.automat.AbstractAutomaton):

    # if a button is turned on, this routing turns it off
    # another class will actually respond to the button and do something

    # also monitors Setup Dispatch and Setup Route and also Run Route

    button_sensors_to_watch = []
    count = 0
    def __init__(self):
        self.logLevel = 0

    def init(self):
        if self.logLevel > 0: print 'Create ResetButtonMaster Thread'
        self.od = OptionDialog()

    def setup(self):
        if self.logLevel > 0: print "starting ResetButtonMaster setup"

        #get dictionary of buttons self.button_dict
        self.get_buttons()
        self.get_sensors_requiring_use_of_station_buttons()
        self.get_route_run_button()
        self.get_set_stopping_length_button()
        self.get_station_wait_time_button()
        self.get_station_direction_button()
        #set all move_to buttons inactive
        for sensor in self.button_sensors:
            if sensor != None:
                sensor.setKnownState(INACTIVE)
        for sensor in self.sensors_requiring_use_of_station_buttons:
            sensor.setKnownState(INACTIVE)
        for sensor in self.route_run_sensor:
            sensor.setKnownState(INACTIVE)
        for sensor in self.stopping_distance_sensor:
            sensor.setKnownState(INACTIVE)
        for sensor in self.station_wait_time_sensor:
            sensor.setKnownState(INACTIVE)
        for sensor in self.station_direction_sensor:
            sensor.setKnownState(INACTIVE)


        self.button_sensors_to_watch = self.route_run_sensor + self.button_sensors + self.sensors_requiring_use_of_station_buttons

        if self.logLevel > 0: print "self.button_sensors_to_watch_init", [sensor.getUserName() for sensor in self.button_sensors_to_watch]

        self.sensor_active = None
        self.sensor_active_sensors_requiring_use_of_station_buttons = None
        self.sensor_active_run_dispatch = None
        self.sensor_active_old = None
        self.sensor_active_sensors_requiring_use_of_station_buttons_old = None

        if self.logLevel > 0: print "finished ResetButtonMaster setup"
        return True

    def handle(self):
        global setAllStoppingSensors
        global stopping_sensor_choice
        #wait for a sensor to go active
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)
        #determine which one changed
        if self.logLevel > 0: print "self.button_sensors_to_watch",self.button_sensors_to_watch
        sensor_active_all_array = [sensor for sensor in self.button_sensors_to_watch if sensor.getKnownState() == ACTIVE]

        if len(sensor_active_all_array) > 0:
            sensor_changed = sensor_active_all_array[0]   # there should be only one or zero items in this array, and that not in self.setup_route_or_run_dispatch_sensors
        else:
            sensor_changed = None

        #reset button_sensors_to_watch
        self.button_sensors_to_watch = self.route_run_sensor + self.button_sensors + self.sensors_requiring_use_of_station_buttons

        # 1) modify button_sensors_to_watch so we don't keep triggering same sensor active
        # 2) perform the correct action if a new button has been triggered
        #    note we have to see whether a new sensor has been triggered by looking at old values

        # print statements
        if self.logLevel > 0: print "sensor_active_all_array" , [s.getUserName() for s in sensor_active_all_array if sensor_active_all_array != None]
        if self.logLevel > 0: print "self.sensor_active_route_dispatch_old" , self.sensor_active_sensors_requiring_use_of_station_buttons_old
        #if self.logLevel > 0: print "self.sensor_active_route_dispatch" , self.sensor_active_sensors_requiring_use_of_station_buttons
        #if self.logLevel > 0: print "self.sensor_active_route_dispatch", self.sensor_active_sensors_requiring_use_of_station_buttons.getUserName()
        if self.logLevel > 0: print "self.sensor_active", self.sensor_active
        if self.logLevel > 0: print "self.sensor_active_old", self.sensor_active_old
        if self.logLevel > 0: print "stopping_sensor_choice", stopping_sensor_choice

        # if self.sensor_active_setuproute_or_rundispatch_or_stoppinglength_old != None:
        #     self.button_sensors_to_watch.remove(self.sensor_active_setuproute_or_rundispatch_or_stoppinglength_old)

        if sensor_changed in self.button_sensors:

            #we only want to process if previous button != current buttom
            self.sensor_active = sensor_changed
            if self.sensor_active != self.sensor_active_old :
                self.process_button_sensors(self.sensor_active)
                self.sensor_active_old = self.sensor_active
            #dont trigger this one again
            self.button_sensors_to_watch.remove(self.sensor_active)
            if self.sensor_active != self.sensor_active_old:
                self.button_sensors_to_watch.append(self.sensor_active_old)

        elif sensor_changed in self.route_run_sensor:
            #we want to process in all circumstances
            self.process_run_route()

        elif sensor_changed in self.sensors_requiring_use_of_station_buttons:
            #we want to process in all circumstances
            if self.logLevel > 0: print "sensor in sensor_active_all_array", sensor.getUserName()
            #self.sensor_active_sensor_requiring_use_of_station_buttons = sensor_changed

            # if self.sensor_active_setuproute_or_rundispatch_or_stoppinglength != None and \
            #     self.sensor_active_setuproute_or_rundispatch_or_stoppinglength != \
            #         self.sensor_active_setuproute_or_rundispatch_or_stoppinglength_old:
            #
            #     self.process_setuproute_or_rundispatch_or_setstoppinglength_sensors()
            #
            #     self.sensor_active_setuproute_or_rundispatch_or_stoppinglength_old = \
            #         self.sensor_active_setuproute_or_rundispatch_or_stoppinglength

            self.process_sensors_requiring_use_of_station_buttons(sensor_changed)

            #print statements
            if self.logLevel > 0: print "removing ", self.sensor_active_sensors_requiring_use_of_station_buttons_old

            if self.sensor_active_sensors_requiring_use_of_station_buttons_old == None:
                if self.logLevel > 0: print "self.sensor_active_setuproute_or_rundispatch_or_stoppinglength", "None"
            else:
                if self.logLevel > 0: print "self.sensor_active_setuproute_or_rundispatch_or_stoppinglength", \
                                            self.sensor_active_sensors_requiring_use_of_station_buttons_old.getUserName()

        if self.logLevel > 0: print "returning"
        return True

    def switch_sensors_requiring_station_buttons(self, sensor, mode):

        sensor_name = sensor.getUserName()

        # if sensor remains on after being switched on, the sensor must be removed from the list of sensors we are waiting
        # for.
        # if the sensor is switched off, the sensor must be in the list of sensors we are waiting for
        if mode == "sensor_on":
            #reset self.sensors_requiring_use_of_station_buttons to full list
            self.get_sensors_requiring_use_of_station_buttons()
            [s.setKnownState(INACTIVE) for s in self.sensors_requiring_use_of_station_buttons]
            #turn sensor on
            sensor.setKnownState(ACTIVE)
            # remove from list of sensors we are waiting for
            if sensor in self.sensors_requiring_use_of_station_buttons:
                self.sensors_requiring_use_of_station_buttons.remove(sensor)

            # allow the button to be pressed again
            self.sensor_active_sensors_requiring_use_of_station_buttons_old = None

        elif mode == "sensor_off":
            # turn sensor off
            sensor.setKnownState(INACTIVE)
            #reset self.sensors_requiring_use_of_station_buttons to full list
            self.get_sensors_requiring_use_of_station_buttons()
            # inhibit the same sensor being pressed again
            self.sensor_active_sensors_requiring_use_of_station_buttons_old = sensor

        else:
            #error
            print ("!!!!! Error in switch_sensors_requiring_station_buttons: contact developer!!!!!!")

    def process_button_sensors(self, sensor_changed):
        [sensor.setKnownState(INACTIVE) for sensor in self.button_sensors if sensor != sensor_changed]

    def  process_sensors_requiring_use_of_station_buttons(self, sensor_changed):

        # all these sensors when active require the use of station buttons. Only one of these can be active at any one
        # time, hence the grouing of these in a routine and the grouping of the buttons on the panel

        global stopping_sensor_choice
        self.count += 1
        if self.logLevel > 0: print "sensor_active_setuproute_or_rundispatch_or_stoppinglength", \
                                    self.sensor_active_sensors_requiring_use_of_station_buttons

        self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_on")

        if sensor_changed == sensors.getSensor("setDispatchSensor"):
            #stopping_sensor_choice = None
            #sensors.getSensor("setRouteSensor").setKnownState(INACTIVE)
            #sensors.getSensor("setStoppingDistanceSensor").setKnownState(INACTIVE)
            msg = "Press section buttons to set dispatch \nA train needs to be set up in a section first"
            self.od.displayMessage(msg)
            if self.od.CLOSED_OPTION == True:
                #print "closed option"
                #stopping_sensor_choice = "setNoDispatchSensors"
                #make so can select DispatchSensor again
                #sensors.getSensor("setDispatchSensor").setKnownState(INACTIVE)
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                return
            else:
                # if self.sensor_active_sensors_requiring_use_of_station_buttons_old == None:
                #     # set old sensor non-null to make sure sensor setDispatchSensor remains active
                #     self.sensor_active_sensors_requiring_use_of_station_buttons_old = sensors.getSensor("setDispatchSensor")
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_on")
        elif sensor_changed == sensors.getSensor("setRouteSensor"):
            #stopping_sensor_choice = None
            #sensors.getSensor("setStoppingDistanceSensor").setKnownState(INACTIVE)
            #sensors.getSensor("setDispatchSensor").setKnownState(INACTIVE)
            msg = "Press section buttons to set route \nThe route may be used to schedule a train"
            self.od.displayMessage(msg)
            if self.od.CLOSED_OPTION == True:
                #print "closed option"
                #stopping_sensor_choice = "setNoRouteSensors"
                #make so can select RouteSensor again
                #sensors.getSensor("setRouteSensor").setKnownState(INACTIVE)
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                return
            else:
                # if self.sensor_active_sensors_requiring_use_of_station_buttons_old == None:
                #     # set old sensor non-null to make sure sensor setRouteSensor remains active
                #     self.sensor_active_sensors_requiring_use_of_station_buttons_old = sensors.getSensor("setRouteSensor")
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_on")

        elif sensor_changed == sensors.getSensor("setStoppingDistanceSensor"):

            #optionbox
            title = "Stopping distances?"
            msg = "modify all stopping distances?"
            opt1 = "All"
            opt2 = "From one station to another"
            s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
            if self.od.CLOSED_OPTION == True: #check of optionbox was closed prematurely
                #stopping_sensor_choice = "setNoStoppingSensors"
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                return
            elif s == opt1:
                #stopping_sensor_choice = "setAllStoppingSensors"
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                self.modify_all_stopping_distances()
            else:    #opt2
                stopping_sensor_choice = "setIndividualStoppingSensors"
                msg = "Press station buttons to select a section in order to\nset stopping length for that section"
                self.od.displayMessage(msg)
                if self.od.CLOSED_OPTION == True:
                    #stopping_sensor_choice = "setNoStoppingSensors"
                    self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                else:
                    self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_on")

        elif sensor_changed == sensors.getSensor("setStationWaitTimeSensor"):

            #optionbox
            title = "Station Wait Times"
            msg = "modify all station wait times?"
            opt1 = "All"
            opt2 = "At particular station"
            s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
            if self.od.CLOSED_OPTION == True: #check of optionbox was closed prematurely
                #stopping_sensor_choice = "setNoStoppingSensors"
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                return
            elif s == opt1:
                #stopping_sensor_choice = "setAllStoppingSensors"
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                self.modify_all_station_wait_times()
            else:  #opt2
                #stopping_sensor_choice = "setIndividualWaitTimes"
                msg = "Press station buttons to select a section in order to\nset wait time at beginning of that section"
                self.od.displayMessage(msg)
                if self.od.CLOSED_OPTION == True:
                    #stopping_sensor_choice = "setNoStoppingSensors"
                    self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                else:
                    self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_on")

        elif sensor_changed == sensors.getSensor("setStationDirectionSensor"):

            #optionbox
            title = "Station Directions"
            msg = "modify station directions?"
            opt1 = "Reset All"
            opt2 = "Set At particular station"
            s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
            if self.od.CLOSED_OPTION == True: #check of optionbox was closed prematurely
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                return
            elif s == opt1:
                #stopping_sensor_choice = "setAllStoppingSensors"
                self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                self.reset_all_station_directions()
            else:     #opt2
                #stopping_sensor_choice = "setIndividualWaitTimes"
                msg = "Press station buttons to select a section in order to\nset one way working at that station\n"
                msg = msg + "secect the second station adjacent to the first to indicate the direction"
                self.od.displayMessage(msg)
                if self.od.CLOSED_OPTION == True:
                    self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_off")
                else:
                    self.switch_sensors_requiring_station_buttons(sensor_changed, "sensor_on")
        else:
            msg = "error" + sensor_changed.getUserName()
            OptionDialog().displayMessage(msg)

        #self.sensor_active_setuproute_or_rundispatch_or_stoppinglength_old = None
        #self.button_sensors_to_watch = self.route_run_sensor + self.button_sensors + self.setuproute_or_rundispatch_or_setstoppinglength_sensors

    def modify_all_stopping_distances(self):
        title = "Modify all stopping distances"
        msg = "Change all stopping distances?"
        opt1 = "Increase/decrease all existing stopping distances"
        opt2 = "Set all stopping distances to the same value"
        s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
        if self.od.CLOSED_OPTION == True:
            return #if one has cancelled
        #s is used in a little bit
        new_stopping_position = self.get_new_stopping_position()
        if new_stopping_position == None:
            return  # if one has cancelled
        for e in g_express.edgeSet():
            from_station_name = g.g_stopping.getEdgeSource(e)
            to_station_name = g.g_stopping.getEdgeTarget(e)
            found_edge = e
            length_of_last_section = self.get_length_of_last_section(found_edge)
            old_stopping_position = self.get_existing_stopping_position(found_edge, length_of_last_section)
            if s == opt2:
                combined_stopping_position = new_stopping_position
            else:
                combined_stopping_position = new_stopping_position + old_stopping_position
            combined_stopping_fraction = self.get_new_stopping_fraction(combined_stopping_position, length_of_last_section)
            filename_fwd = self.get_filename(found_edge, "fwd")
            self.modify_stopping_distance(found_edge, combined_stopping_fraction, filename_fwd)
            filename_rvs = self.get_filename(found_edge, "rvs")
            self.modify_stopping_distance(found_edge, combined_stopping_fraction, filename_rvs)

    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "blockDirections"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator
    def reset_all_station_directions(self):
        global g
        self.od.displayMessage("About to set All stations set to 2-way working")
        if self.od.CLOSED_OPTION == True:
            self.od.displayMessage("Cancelled setting all stations to 2-way working")
        else:
            file = self.directory() + "blockDirections.txt"
            with open(file  ,'w') as f:  # empty the file containing the direction information
                pass
            g = StationGraph()        # recalculate the weights on the edges
            self.od.displayMessage("All stations set to 2-way working")


    def modify_all_station_wait_times(self):
        title = "Modify all station wait times"
        msg = "Change all station wait times?"
        opt1 = "Increase/decrease all existing station wait times"
        opt2 = "Set all station wait times to the same value"
        s = self.od.customQuestionMessage2str(msg,title,opt1,opt2)
        if self.od.CLOSED_OPTION == True:
            return #if one has cancelled
        #s is used in a little bit
        new_station_wait_time = self.get_new_station_wait_time()
        if new_station_wait_time == None:
            return  # if one has cancelled
        for e in g.g_express.edgeSet():
            from_station_name = g.g_stopping.getEdgeSource(e)
            to_station_name = g.g_stopping.getEdgeTarget(e)
            found_edge = e
            old_station_wait_time = self.get_existing_station_wait_time(found_edge)
            if s == opt2:
                combined_station_wait_time = new_station_wait_time
            else:
                combined_station_wait_time = new_station_wait_time + old_station_wait_time
            filename_fwd = self.get_filename(found_edge, "fwd")
            self.modify_station_wait_time(found_edge, combined_station_wait_time, filename_fwd)
            filename_rvs = self.get_filename(found_edge, "rvs")
            self.modify_station_wait_time(found_edge, combined_station_wait_time, filename_rvs)

    def get_existing_stopping_position(self, found_edge, length_of_last_section):
        filename_fwd = self.get_filename(found_edge, "fwd")
        trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
        old_stopping_fraction = trainInfo_fwd.getStopBySpeedProfileAdjust()
        old_stopping_position = (1.0 - old_stopping_fraction) * float(length_of_last_section)
        return old_stopping_position

    def get_existing_station_wait_time(self, found_edge):
        filename_fwd = self.get_filename(found_edge, "fwd")
        trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
        old_station_wait_time = trainInfo_fwd.getWaitTime()
        return old_station_wait_time

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

    def get_length_of_last_section(self, found_edge):
        filename_fwd = self.get_filename(found_edge, "fwd")
        trainInfo_fwd = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename_fwd)
        last_section = DispatchMaster().last_section_of_transit(trainInfo_fwd)  #last_section_of_transit is defined in DispatchMaster, and don't want to repeat code
        if self.logLevel > 0: print "last_section",last_section.getUserName()
        length_of_last_section = float(DispatchMaster().length_of_last_section(last_section))/10.0 #cm
        if self.logLevel > 0: print "length_of_last_section", length_of_last_section
        return length_of_last_section

    def modify_stopping_distance(self, found_edge, new_stopping_fraction, filename):
        trainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename)
        #stopping_fraction = trainInfo_rvs.getStopBySpeedProfileAdjust()
        trainInfo.setStopBySpeedProfileAdjust(float(new_stopping_fraction))

        #write the newtraininfo back to file
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(trainInfo, filename)

    def modify_station_wait_time(self, found_edge, new_stopping_fraction, filename):
        trainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(filename)
        #stopping_fraction = trainInfo_rvs.getStopBySpeedProfileAdjust()
        trainInfo.setWaitTime(float(new_stopping_fraction))

        #write the newtraininfo back to file
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(trainInfo, filename)

    def is_integer(self, n):
        try:
            if n == None: return False
            float(n)
        except ValueError:
            return False
        else:
            return float(n).is_integer()

    def get_new_stopping_position(self):
        s = "redo"
        while s == "redo":
            #modify stopping fraction in traininfo
            title = "Stop train before end of section"
            msg ="enter how many cm to reduce the stopping distance by (-ve increase)"
            default_value = 0
            new_stopping_position = self.od.input(msg, title, default_value)
            if not self.is_integer(new_stopping_position): return
            # print "new_stopping_position",  new_stopping_position
            # print "new_stopping_fraction", new_stopping_fraction
            if float(new_stopping_position) > 0:
                msg = "new stopping position: " + str(round(float(new_stopping_position),1)) + " cm (" + \
                      str(round(float(new_stopping_position)/2.54,1)) + " inches) before calculated position."
            else:
                sp =  0 - float(new_stopping_position)
                msg = "new stopping position: " + str(round(float(sp),1)) + " cm (" + \
                      str(round(float(new_stopping_position)/2.54,1)) + " inches) after calculated position"
            opt1 = "OK"
            opt2 = "redo"
            s = self.od.customQuestionMessage2str(msg, title, opt1, opt2)
        msg = "stop position = " + str(new_stopping_position)
        self.od.displayMessage(msg,title)
        return float(new_stopping_position)

    def get_new_station_wait_time(self):
        s = "redo"
        while s == "redo":
            #modify station wait time in traininfo
            title = "Pause train at beginning of section"
            msg ="enter how many secs to wait at station"
            default_value = 0
            new_wait_time = self.od.input(msg, title, default_value)
            if not self.is_integer(new_wait_time): return
            # print "new_stopping_position",  new_stopping_position
            # print "new_stopping_fraction", new_stopping_fraction
            if float(new_wait_time) > 0:
                msg = "new wait time: " + str(round(float(new_wait_time),1)) + " secs "
            else:
                sp =  0  # set to zero as cannot be -ve
                msg = "new wait time: " + str(round(float(sp),1)) + " secs "
            opt1 = "OK"
            opt2 = "redo"
            s = self.od.customQuestionMessage2str(msg, title, opt1, opt2)
        msg = "wait time = " + str(new_wait_time)
        self.od.displayMessage(msg,title)
        return float(new_wait_time)

    def get_new_stopping_fraction(self, new_stopping_position, length_of_last_section):
        if self.logLevel > 0: "new_stopping_position",new_stopping_position, "length_of_last_section", length_of_last_section
        new_stopping_fraction = 1.0-(float(new_stopping_position)/float(length_of_last_section))
        return new_stopping_fraction

    def process_run_route(self):
        title = ""
        msg = "run one or more routes"
        opt1 = "1 route"
        opt2 = "several routes"
        reply = self.od.customQuestionMessage2str(msg, title, opt1, opt2)
        if reply == opt1:
            self.run_route()
        else:
            self.run_routes()
        sensors.getSensor("runRouteSensor").setKnownState(INACTIVE)

    def get_buttons(self):
        self.button_sensors = [self.get_button_sensor_given_block_name(station_block_name) for station_block_name in g.station_block_list]
        self.button_sensor_states = [self.check_sensor_state(button_sensor) for button_sensor in self.button_sensors]
        # for button_sensor in self.button_sensors:
        # self.button_dict[button_sensor] = self.check_sensor_state(button_sensor)

    def get_sensors_requiring_use_of_station_buttons(self):
        sensor_list = ["setDispatchSensor", "setRouteSensor", "setStoppingDistanceSensor", \
                       "setStationWaitTimeSensor", "setStationDirectionSensor" ]
        self.sensors_requiring_use_of_station_buttons = \
            [sensors.getSensor(sensorName) for sensorName in sensor_list]
        self.route_dispatch_states = [self.check_sensor_state(rd_sensor) for rd_sensor in self.sensors_requiring_use_of_station_buttons]

    def get_route_run_button(self):
        self.route_run_sensor = [sensors.getSensor(sensorName) for sensorName in ["runRouteSensor"]]

    def get_set_stopping_length_button(self):
        self.stopping_distance_sensor = [sensors.getSensor(sensorName) for sensorName in ["setStoppingDistanceSensor"]]

    def get_station_wait_time_button(self):
        self.station_wait_time_sensor = [sensors.getSensor(sensorName) for sensorName in ["setStationWaitTimeSensor"]]

    def get_station_direction_button(self):
        self.station_direction_sensor = [sensors.getSensor(sensorName) for sensorName in ["setStationDirectionSensor"]]


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
        if button_sensor == None: 
            if self.logLevel > 0: print button_sensor_name + "= None"
        return button_sensor

    def run_route(self):
        global trains_dispatched
        # list_items = ("Run Route", "Cancel")
        # title = "choose option"
        # result = self.od.List(title, list_items)
        # if self.od.CLOSED_OPTION == True:
        # return
        # if result == "Run Route":
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        list_items = RouteManager.getRoutesByNameList()
        title = "choose route"
        s = self.od.List(title, list_items)
        if self.od.CLOSED_OPTION == True:
            return
        routeName = str(s)
        if self.logLevel > 0: print "routeName", routeName
        route = RouteManager.getRouteByName(routeName)

        trains_to_choose = self.get_list_of_engines_to_move()
        # msg = "trains_to_choose" + str(trains_to_choose)
        if trains_to_choose == []:
            # JOptionPane.showMessageDialog(None,msg)
            str_trains_dispatched= (' '.join(trains_dispatched))
            msg = "There are no trains available for dispatch\nTrains dispatched are:\n"+str_trains_dispatched+"\n"
            title = "Cannot move train"
            opt1 = "continue"
            opt2 = "reset all allocations"
            result = self.od.customQuestionMessage2str(msg, title, opt1, opt2)
            if result == "reset all allocations":
                trains_dispatched = []
            return
        title = "what train do you want to move?"
        engine = self.od.List(title, trains_to_choose)
        if self.od.CLOSED_OPTION == True:
            return
        station_from = self.get_position_of_train(engine)

        list_items = ["stop at end of route", "return to start position", "return to start and repeat", "cancel"]
        title = "What do you want to do"
        option = self.od.List(title, list_items)
        if self.od.CLOSED_OPTION == True:
            return
        repeat = False
        dont_run_route = False
        if option == "stop at end of route":
            station_to = None
            repeat = False
        elif option == "return to start position":
            station_to = station_from
            repeat = False
        elif option == "return to start and repeat":
            station_to = station_from
            repeat = True
        else:
            dont_run_route = True
        if repeat:
            title = "repeat how many times?"
            default_value = 3
            msg = "repeat how many times"
            no_repetitions = self.od.input(msg, title, default_value)
        else:
            no_repetitions = 0

        if dont_run_route == False:
            if self.logLevel > 0: print "station_from",    station_from, "station_to",station_to, "repeat",repeat
            run_train = RunRoute(route, g.g_express, station_from, station_to, no_repetitions, engine)
            run_train.setName("running_route_" + routeName)
            instanceList.append(run_train)
            run_train.start()

    def get_list_of_engines_to_move(self):
        global trains_allocated
        global trains_dispatched

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

        # # JOptionPane.showMessageDialog(None,msg)
        # if trains_to_choose == []:
        #     str_trains_dispatched= (' '.join(trains_dispatched))
        #     msg = "There are no trains available for dispatch\nTrains dispatched are:\n"+str_trains_dispatched+"\n"
        #     title = "Cannot move train"
        #     opt1 = "continue"
        #     opt2 = "reset all allocations"
        #     result = self.od.customQuestionMessage2str(msg, title, opt1, opt2)
        #     if result == "reset all allocations":
        #         trains_dispatched = []
        return trains_to_choose

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

    def run_routes(self):
        createandshowGUI2(self)

class createandshowGUI2(TableModelListener):

    def __init__(self, class_ResetButtonMaster):
        self.logLevel = 0
        self.class_ResetButtonMaster = class_ResetButtonMaster
        #Create and set up the window.

        self.initialise_model(class_ResetButtonMaster)
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

        button_add = JButton("Add Row", actionPerformed = self.add_row_action)
        self.buttonPane.add(button_add);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_populate = JButton("Populate", actionPerformed = self.populate_action)
        self.buttonPane.add(button_populate);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_tidy = JButton("Tidy", actionPerformed = self.tidy_action)
        self.buttonPane.add(button_tidy);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_apply = JButton("Run Routes", actionPerformed = self.apply_action)
        self.buttonPane.add(button_apply)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_close = JButton("Close", actionPerformed = self.close_action)
        self.buttonPane.add(button_close)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_task = JButton("Task", actionPerformed = self.task_action)
        self.buttonPane.add(button_task)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_repetitions = JButton("No. repetitions", actionPerformed = self.repetitions_action)
        self.buttonPane.add(button_repetitions)
        self.buttonPane.add(Box.createHorizontalGlue());

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

    def initialise_model(self, class_ResetButtonMaster):

        self.model = None
        self.model = MyTableModel1()
        self.table = JTable(self.model)
        self.model.addTableModelListener(MyModelListener1(self, class_ResetButtonMaster));
        self.class_ResetButtonMaster = class_ResetButtonMaster


        pass
    def self_table(self):

        #table.setPreferredScrollableViewportSize(Dimension(500, 70));
        #table.setFillsViewportHeight(True)
        #self.table.getModel().addtableModelListener(self)
        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);
        #table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        # self.resizeColumnWidth(table)
        columnModel = self.table.getColumnModel();

        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        columnModel.getColumn(route_col).setPreferredWidth(200);
        columnModel.getColumn(task_col).setPreferredWidth(150);

        # first column is the trains
        self.trainColumn = self.table.getColumnModel().getColumn(train_col);
        self.combobox0 = JComboBox()

        for train in self.class_ResetButtonMaster.get_list_of_engines_to_move():
             self.combobox0.addItem(train)

        self.trainColumn.setCellEditor(DefaultCellEditor(self.combobox0));
        renderer0 = ComboBoxCellRenderer1()
        self.trainColumn.setCellRenderer(renderer0);

        # second column is the routes

        self.routesColumn = self.table.getColumnModel().getColumn(route_col);
        self.combobox1 = JComboBox()

        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        routes = RouteManager.getRoutesByNameList()
        for route in routes:
            self.combobox1.addItem(route)
        self.routesColumn.setCellEditor(DefaultCellEditor(self.combobox1));
        renderer1 = ComboBoxCellRenderer1()
        self.routesColumn.setCellRenderer(renderer1);

        # first column is the trains
        self.taskColumn = self.table.getColumnModel().getColumn(task_col);
        self.combobox3 = JComboBox()

        tasks = ["stop at end of route", "return to start position","return to start and repeat"]
        for task in tasks:
            self.combobox3.addItem(task)

        self.taskColumn.setCellEditor(DefaultCellEditor(self.combobox3));
        renderer3 = ComboBoxCellRenderer1()
        self.taskColumn.setCellRenderer(renderer3);

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
        trains_to_put_in_dropdown = self.class_ResetButtonMaster.get_list_of_engines_to_move()
        self.model.populate(trains_to_put_in_dropdown)
        self.completeTablePanel()
        pass

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
        [train, route, task, delay, repetitions] = [0, 1, 3, 4, 5]
        for row in range(len(self.model.data)):
            train_name = str(self.model.data[row][train])
            route_name = str(self.model.data[row][route])
            task_name = str(self.model.data[row][task])
            delay_name = str(self.model.data[row][delay])
            repetitions_name = str(self.model.data[row][repetitions])
            row_list = [train_name, route_name, task_name, delay_name, repetitions_name]
            my_list.append(row_list)
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
            [train, route, task, delay, repetitions] = [0, 1, 3, 4, 5]
            for row in my_list:
                [train_val, route_val, task_val, delay_val, repetitions_val] = row
                self.model.add_row()
                self.model.data[i][train] = train_val.replace('"','')
                self.model.data[i][route] = route_val.replace('"','')
                self.model.data[i][task] = task_val.replace('"','')
                self.model.data[i][delay] = delay_val.replace('"','')
                self.model.data[i][repetitions] = repetitions_val.replace('"','')
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

            [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]

            # check the trains are valid

            trains_to_put_in_dropdown = [t for t in self.class_ResetButtonMaster.get_list_of_engines_to_move()]
            for row in reversed(range(len(self.model.data))):
                if self.model.data[row][train_col] not in trains_to_put_in_dropdown:
                    self.model.data.pop(row)

            RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
            routes = [str(route) for route in RouteManager.getRoutesByNameList()]
            for row in reversed(range(len(self.model.data))):
                if self.model.data[row][route_col] not in routes:
                    self.model.data.pop(row)
            self.completeTablePanel()

    def close_action(self, event):
        self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING));

    def repetitions_action(self, event):
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        for row in reversed(range(len(self.model.data))):
            old_val = int(self.model.data[0][repetition_col])
            if old_val == None: old_val = 0
            new_val = self.new_val(old_val)
            self.model.data[row][repetition_col] = new_val
        self.completeTablePanel()
    def new_val(self, old_val):
        if old_val < 3:
            new_val = 3
        elif old_val < 10:
            new_val = 10
        elif old_val < 30:
            new_val = 30
        elif old_val < 1000:
            new_val = 1000
        else:
            new_val = 0
        return new_val
    
    def task_action(self, event):
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        for row in reversed(range(len(self.model.data))):
            old_val = str(self.model.data[0][task_col])
            if old_val == None: old_val = 0
            new_val = self.new_task(old_val)
            self.model.data[row][task_col] = new_val
        self.completeTablePanel()

    def new_task(self, old_val):
        tasks = ["stop at end of route", "return to start position","return to start and repeat"]
        if old_val == "stop at end of route":
            new_val = "return to start position"
        elif old_val == "return to start position":
            new_val = "return to start and repeat"
        else:
            return "stop at end of route"
        return new_val


    def apply_action(self, event):
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        # print "apply action"
        for row in reversed(range(len(self.model.data))):
            train_name = str(self.model.data[row][train_col])
            route_name = str(self.model.data[row][route_col])
            delay_val = str(self.model.data[row][delay_col])
            if train_name != "" and route_name != "" and delay_val != "":
                self.run_route(row, self.model, self, self.class_ResetButtonMaster)
            else:
                msg = "not running route, train, route or delay is not set"
                OptionDialog().displayMessage(msg,"")
        self.completeTablePanel()
        if self.model.getRowCount() == 0:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))


    def run_route(self, row, model, class_createandshowGUI2, class_ResetButtonMaster):
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        route_name = str(model.getValueAt(row, route_col))
        if route_name == None:
            msg = "not running route is not set"
            self.od.displayMessage(msg,"")
            return
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route = RouteManager.getRouteByName(route_name)

        train_name = str(model.getValueAt(row, train_col))
        if train_name == None or train_name == "":
            msg = "not running route, train is not set"
            self.od.displayMessage(msg,"")
            return
        station_from = class_ResetButtonMaster.get_position_of_train(train_name)

        option = str(model.getValueAt(row, task_col))

        repeat = False
        dont_run_route = False
        no_repetitions = 0
        if option == "stop at end of route":
            station_to = None
            repeat = False
        elif option == "return to start position":
            station_to = station_from
            repeat = False
        elif option == "return to start and repeat":
            station_to = station_from
            repeat = True
        else:
            dont_run_route = True

        if repeat:
            no_repetitions = str(model.getValueAt(row, repetition_col))
        else:
            no_repetitions = 0

        # delay by delay_val before starting route
        delay_val = int(model.getValueAt(row, delay_col)) *1000

        if dont_run_route == False:
            if self.logLevel > 0: print "station_from",    station_from, "station_to",station_to, \
                                        "repeat",repeat, "delay", delay
            run_train = RunRoute(route, g.g_express, station_from, station_to, no_repetitions, train_name, delay_val)
            #run_train = RunRoute(route, g.g_express, station_from, station_to, no_repetitions, train_name)
            run_train.setName("running_route_" + route_name)
            instanceList.append(run_train)
            run_train.start()
            model.data.pop(row)
            class_createandshowGUI2.completeTablePanel()


    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "routes"
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

class MyModelListener1(TableModelListener):

    def __init__(self, class_createandshowGUI2, class_ResetButtonMaster):
        self.class_createandshowGUI2 = class_createandshowGUI2
        self.class_ResetButtonMaster = class_ResetButtonMaster
        self.cancel = False
        self.logLevel = 1
    def tableChanged(self, e) :
        global trains_allocated
        row = e.getFirstRow()
        column = e.getColumn()
        model = e.getSource()
        columnName = model.getColumnName(column)
        data = model.getValueAt(row, column)
        class_createandshowGUI2 = self.class_createandshowGUI2
        class_ResetButtonMaster = self.class_ResetButtonMaster
        tablemodel = class_createandshowGUI2.model
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        if column == 0:     #trains
            pass
        elif column == 1:       # sections
            pass
        elif column == run_route_col:
            class_createandshowGUI2.run_route(row, model, class_createandshowGUI2, class_ResetButtonMaster)

class ComboBoxCellRenderer1 (TableCellRenderer):

    def getTableCellRendererComponent(self, jtable, value, isSelected, hasFocus, row, column) :
        panel = self.createPanel(value)
        return panel

    def createPanel(self, s) :
        p = JPanel(BorderLayout())
        p.add(JLabel(str(s), JLabel.LEFT), BorderLayout.WEST)
        icon = UIManager.getIcon("Table.descendingSortIcon");
        p.add(JLabel(icon, JLabel.RIGHT), BorderLayout.EAST);
        p.setBorder(BorderFactory.createLineBorder(Color.blue));
        return p;


class MyTableModel1 (DefaultTableModel):

    columnNames = ["Train", "Route", "Run Route", "Task", "Delay", "No. Repetitons"]

    def __init__(self):
        l1 = ["", "", False, "stop at end of route", 10, 0]
        self.data = [l1]

    def remove_not_set_row(self):
        b = False
        for row in reversed(range(len(self.data))):
            # print "row", row
            if self.data[row][1] == "":
                self.data.pop(row)

    def add_row(self):
        # print "addidn row"
        # if row < len(self.data):
        # print "add"
        self.data.append(["", "", False, "stop at end of route", 10, 0])
        # print self.data
        # print "added"

    def populate(self, trains_to_put_in_dropdown):
        for row in reversed(range(len(self.data))):
            self.data.pop(row)
        self.data = []
        # append all trains to put in dropdown
        for train in trains_to_put_in_dropdown:
            self.data.append([train, "", False, "stop at end of route", 10, 3])
        # # delete rows with no trains
        # for row in reversed(range(len(self.data))):
        #     if self.data[row][1] == None or self.data[row][0] == "":
        #         self.data.pop(row)

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
        return True

    # only include if data can change.
    def setValueAt(self, value, row, col) :
        self.data[row][col] = value
        self.fireTableCellUpdated(row, col)
