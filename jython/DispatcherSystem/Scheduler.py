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

from javax.swing import JFrame, JPanel, JButton, BoxLayout, Box
from java.awt import Dimension
import os

fast_clock_rate = 10

trains_to_be_scheduled = []
run_train_dict = {}
scheduled = {}
RunTrain_instance = {}
tListener = None

class SchedulerMaster(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self):
        self.logLevel = 0

    def setup(self):
        #run before setting up Schedule Master
        if self.logLevel > 0: print "starting SchedulerMaster setup"

        self.scheduler_master_sensor = sensors.getSensor("startSchedulerSensor")
        self.scheduler_view_scheduled_trains = sensors.getSensor("viewScheduledSensor")
        self.scheduler_edit_routes = sensors.getSensor("editRoutesSensor")
        self.scheduler_start_time_sensor = sensors.getSensor("schedulerStartTimeSensor")
        self.scheduler_show_clock_sensor = sensors.getSensor("showClockSensor")
        self.help_sensor = sensors.getSensor("helpSensor")


        if self.logLevel > 0: print "finished SchedulerMaster setup"

        return True

    def init(self):
        self.train_scheduler_setup = False
        pass

    showing_clock = False
    showing_trains = False
    def handle(self):

        self.button_sensors_to_watch = self.set_button_sensors_to_watch()
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)
        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)

        if self.scheduler_master_sensor.getKnownState() == ACTIVE:   # pause processing if we turn the sensor off

            if self.logLevel > 0: print("checking valid operations trains")
            if self.logLevel > 0: print "train_scheduler_setup",self.train_scheduler_setup
            if self.train_scheduler_setup == False:
                self.setup_train_scheduler()
                self.train_scheduler_setup = True

                #process the trains in trains_to_be_scheduled
            self.run_trains()

        if self.scheduler_view_scheduled_trains.getKnownState() == ACTIVE:
            self.show_operations_trains()
            self.scheduler_view_scheduled_trains.setKnownState(INACTIVE)

        if self.scheduler_edit_routes.getKnownState() == ACTIVE:
            self.show_routes()
            self.scheduler_edit_routes.setKnownState(INACTIVE)


        if self.scheduler_start_time_sensor.getKnownState() == ACTIVE:
            self.set_fast_clock()
            self.scheduler_start_time_sensor.setKnownState(INACTIVE)

        if self.scheduler_show_clock_sensor.getKnownState() == ACTIVE:
            self.show_analog_clock()
            self.scheduler_show_clock_sensor.setKnownState(INACTIVE)

        if self.help_sensor.getKnownState() == ACTIVE:
            self.display_help()
            self.help_sensor.setKnownState(INACTIVE)

        self.waitMsec(500)
        return True



    def set_button_sensors_to_watch(self):
        button_sensors_to_watch = [self.scheduler_master_sensor, self.scheduler_view_scheduled_trains, \
                                   self.scheduler_edit_routes, self.scheduler_start_time_sensor, \
                                   self.scheduler_show_clock_sensor, self.help_sensor]
        return button_sensors_to_watch


    def run_trains(self):

        global trains_to_be_scheduled
        global scheduled
        global timebase
        if self.logLevel > 0: print "************************************run trains******************"
        if self.logLevel > 0: print "run trains started: loop: scheduled trains", trains_to_be_scheduled
        for train in trains_to_be_scheduled:
            if scheduled[train] == False:
                if self.logLevel > 0: print "train",train,"scheduled[train]",scheduled[train]
                if "stopping" in train.getDescription():
                    run_train_dict[train] = RunTrain(train, g.g_stopping)
                else:
                    run_train_dict[train] = RunTrain(train, g.g_express)
                run_train_dict[train].setName("schedule_" + train.getName())
                run_train_dict[train].start()
                scheduled[train] = True
                if self.logLevel > 0: print "scheduled train ", train
        if self.logLevel > 0: print "!!!!!!!!!!!!!!!!!!!!!run_trains finished"
        if self.logLevel > 0: print "trains_to_be_scheduled ", trains_to_be_scheduled
        if self.logLevel > 0: print "timebase.getRate()",timebase.getRate()
        noMsec = int(60000/timebase.getRate())
        self.waitMsec(noMsec)  ##every fast minute
        return True

    def setup_train_scheduler(self):
        global tListener
        global timebase
        if self.logLevel > 0: print "Setting up Time Scheduler"

        timebase = jmri.InstanceManager.getDefault(jmri.Timebase)
        from java.util import Date
        date = Date(2020,10,21)
        if self.logLevel > 0: print "date = ", date
        timebase.userSetTime(date)   ## set to 00:00 21/10/2020 (want the time to be 00:00)
        # run fast clock 10 times as fast as normal
        timebase.setRate(fast_clock_rate)
        time = timebase.getTime()
        if self.logLevel > 0: print "time = " , time
        #TimeListener().process_operations_trains()
        tListener = TimeListener()
        # attach a listener to the timebase.
        timebase.addMinuteChangeListener(tListener)
        self.init = True

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
        self.f.setVisible(True)

    def set_fast_clock(self):
        jmri.jmrit.simpleclock.SimpleClockAction().actionPerformed(None)

    def show_routes(self):
        a = jmri.jmrit.operations.routes.RoutesTableAction()
        a.actionPerformed(None)


    def show_operations_trains(self):
        a = jmri.jmrit.operations.trains.TrainsTableAction()
        a.actionPerformed(None)


from java.util.concurrent import TimeUnit

class TimeListener(java.beans.PropertyChangeListener):

    def __init__(self):
        self.scheduler_master_sensor = sensors.getSensor("SchedulerSensor3")
        self.logLevel = 0
        self.prev_time = 0


    def propertyChange(self, event):
        if self.logLevel > 0: print "TimeListener: change",event.propertyName, "from", event.oldValue, "to", event.newValue
        self.process_operations_trains(event)
        # if self.scheduler_master_sensor.getKnownState() == ACTIVE:
        # self.process_operations_trains(event)
        #return

    def stop():
        tListener.cancel()

    def process_operations_trains(self, event ):
        global timebase

        hour = timebase.getTime().getHours()
        self.curr_time = event.newValue + hour * 60
        self.prev_time = self.curr_time -1

        if self.logLevel > 1: print "TimeListener: process_operations_trains"
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        if self.logLevel > 0: print "train_list",train_list
        #if self.logLevel > 1: print "prev_time", self.prev_time, "curr_time", self.curr_time
        for train in train_list:
            if self.logLevel > 1: print "*******************"
            if self.logLevel > 1: print ("train list: departure time: ", str(train.getDepartureTime()), str(train.getName()))
            if self.logLevel > 1: print "prev_time", self.prev_time, "curr_time", self.curr_time, "train.getDepartTimeMinutes()", train.getDepartTimeMinutes()
        #get the train that is triggered in the current minute
        if event == None:
            if self.logLevel > 0: print "event is none , returning"
            return

        trains_to_start = [train for train in train_list
                           if (self.prev_time < train.getDepartTimeMinutes() <= self.curr_time) and
                           "skip" not in train.getDescription()]    #if skip in description of scheduled Train do not run the train

        if self.logLevel > 0: print "trains to start " , trains_to_start
        #self.run_trains(trains_to_start)
        global trains_to_be_scheduled
        trains_to_be_scheduled += trains_to_start
        if self.logLevel > 0: print "trains_to_be_scheduled", trains_to_be_scheduled
        global scheduled
        for t in trains_to_start:
            scheduled[t] = False

        if self.logLevel > 0: print "End of process_operations_trains", "trains_to_be_scheduled",trains_to_be_scheduled,"scheduled",scheduled

class RunTrain(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self, train, graph):
        self.logLevel = 0
        if train == None:
            if self.logLevel > 0: print "RunTrain: train == None"
        else:
            if self.logLevel > 0: print "RunTrain: train =", train
            self.logLevel = 0
            if self.logLevel > 0: print "RunTrain"
            global trains_to_be_scheduled
            if self.logLevel > 0: print "trains_to_be_scheduled", trains_to_be_scheduled
            self.logLevel = 0
            self.graph = graph
            self.train = train

    def handle(self):    # Need to overload handle
        self.run_train()
        if "repeat" in self.train.getDescription():
            return True
        else:
            return False

    def run_train(self):
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
        for station in station_list:
            if self.logLevel > 0:  print "!     self.train: ", self.train, "station_list: ", station_list, "station", station
            station_to = station.getName()  #convert from operations:location to string
            if station_from != None:
                if self.logLevel > 0:  print "!     moving from", station_from, "to", station_to
                self.station_from_name = station_from
                self.station_to_name = station_to
                start_block = blocks.getBlock(station_from)
                train_to_move = start_block.getValue()
                self.train_name = train_to_move
                if self.logLevel > 0: print "calling move_between_stations","station_from",station_from,"station_to",station_to,"train_to_move",train_to_move

                doNotRun = False
                repeat = False
                if self.logLevel > 0: print "train_to_move", train_to_move
                if train_to_move != None:
                    if self.logLevel > 0: print "************************************moving train******************",train_to_move
                    move_train = MoveTrain(station_from, station_to, train_to_move, self.graph)
                    move_train.move_between_stations(station_from, station_to, train_to_move, self.graph)
                    move_train = None
                else:
                    msg = "1No train in block for scheduled train starting from " + station_from
                    title = "Scheduling Error"
                    opt1 = "Not scheduling train"
                    if self.logLevel > 0: print "1No train in block for scheduled train starting from " + station_from
                    OptionDialog().customMessage(msg, title, opt1)
                    doNotRun = True
                    break

            station_from = station_to

        #remove train from train list
        if "repeat" in self.train.getDescription():
            repeat = True
        if repeat == False or doNotRun == True:
            global trains_to_be_scheduled
            trains_to_be_scheduled.remove(self.train)
            self.waitMsec(4000)
        else:
            self.waitMsec(4000)
        if self.logLevel > 0:  "!     finished run_train"


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
                station_list.insert(0,self.station_from)
                station_comment_list.insert(0, None)
                self.prepended = True                           # we have to remove this initial station if we are repeating
            if self.logLevel > 0: print "station_list",station_list

            # append station_to if required
            if self.station_to == None:                         #ensure route ends at station_to
                pass
            elif self.station_to != station_list[-1]:
                station_list.append(self.station_to)
                station_comment_list.insert(None)
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

        if self.logLevel > 0:  "!     finished run_train"

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
            blockName = [block for block in blocks if block.getValue() == train_to_move]
            if blockName != []:
                blockName = blockName[0]
            else:
                blockName = "train not in any block"
            #print "train_to_move", train_to_move, "in" , blockName
            return False

    def blockOccupied(self, block):
        if block.getState() == ACTIVE:
            state = "ACTIVE"
        else:
            state ="INACTIVE"
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



