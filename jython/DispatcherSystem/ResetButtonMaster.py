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
            else:
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
            else:
                #stopping_sensor_choice = "setIndividualWaitTimes"
                msg = "Press station buttons to select a section in order to\nset wait time at beginning of that section"
                self.od.displayMessage(msg)
                if self.od.CLOSED_OPTION == True:
                    #stopping_sensor_choice = "setNoStoppingSensors"
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
        for e in g.g_express.edgeSet():
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
        self.run_route()
        sensors.getSensor("runRouteSensor").setKnownState(INACTIVE)

    def get_buttons(self):
        self.button_sensors = [self.get_button_sensor_given_block_name(station_block_name) for station_block_name in g.station_block_list]
        self.button_sensor_states = [self.check_sensor_state(button_sensor) for button_sensor in self.button_sensors]
        # for button_sensor in self.button_sensors:
        # self.button_dict[button_sensor] = self.check_sensor_state(button_sensor)

    def get_sensors_requiring_use_of_station_buttons(self):
        self.sensors_requiring_use_of_station_buttons = \
            [sensors.getSensor(sensorName) for sensorName in ["setDispatchSensor", "setRouteSensor", "setStoppingDistanceSensor", "setStationWaitTimeSensor" ]]
        self.route_dispatch_states = [self.check_sensor_state(rd_sensor) for rd_sensor in self.sensors_requiring_use_of_station_buttons]

    def get_route_run_button(self):
        self.route_run_sensor = [sensors.getSensor(sensorName) for sensorName in ["runRouteSensor"]]

    def get_set_stopping_length_button(self):
        self.stopping_distance_sensor = [sensors.getSensor(sensorName) for sensorName in ["setStoppingDistanceSensor"]]

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

        list_items = self.get_list_of_engines_to_move()
        # msg = "trains_to_choose" + str(trains_to_choose)
        if list_items == []:
            return
        title = "what train do you want to move?"
        engine = self.od.List(title, list_items)
        if self.od.CLOSED_OPTION == True:
            return
        station_from = self.get_position_of_train(engine)

        list_items = ["stop at end of route", "return to start position", "return to start position and repeat", "cancel"]
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
        elif option == "return to start position and repeat":
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
            run_train = RunRoute(route, g.g_express, station_from, station_to, no_repetitions)
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

        # JOptionPane.showMessageDialog(None,msg)
        if trains_to_choose == []:
            str_trains_dispatched= (' '.join(trains_dispatched))
            msg = "There are no trains available for dispatch\nTrains dispatched are:\n"+str_trains_dispatched+"\n"
            title = "Cannot move train"
            opt1 = "continue"
            opt2 = "reset all allocations"
            result = self.od.customQuestionMessage2str(msg, title, opt1, opt2)
            if result == "reset all allocations":
                trains_dispatched = []
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

