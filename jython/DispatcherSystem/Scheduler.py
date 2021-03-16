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

fast_clock_rate = 10

trains_to_be_scheduled = []
run_train_dict = {}
scheduled = {}
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
        
        if self.logLevel > 0: print "finished SchedulerMaster setup"

        return True
        
    def init(self):
        #self.waitSensorState(self.scheduler_master_sensor, ACTIVE)
        #
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
            #if self.showing_clock == False:
            self.show_analog_clock()
            #tListener.cancel()
            self.scheduler_show_clock_sensor.setKnownState(INACTIVE)
                #self.showing_clock = True
            
        
        # # need todo something to get this to work
        # if self.scheduler_show_clock_sensor.getKnownState() == INACTIVE:
            # if self.showing_clock == True and self.f != None:
                # self.f.setVisible(False)
                # self.showing_clock = False
                # self.f.dispose()
                
        self.waitMsec(500)
        return True
        
    def set_button_sensors_to_watch(self):
        button_sensors_to_watch = [self.scheduler_master_sensor, self.scheduler_view_scheduled_trains, self.scheduler_edit_routes, self.scheduler_start_time_sensor, self.scheduler_show_clock_sensor] 
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
        # timebase = jmri.InstanceManager.getDefault(jmri.Timebase)
        # time = timebase.getTime()
        # if self.logLevel > 0: print "time = " , time
        
        # TimeListener().process_operations_trains()
        # instanceList.append(TimeListener)
        
        # #attach a listener to the timebase. 
        # timebase.addMinuteChangeListener(TimeListener())          
        # self.init = True
    
    
        
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
    
    def show_analog_clock(self):
        if self.f == None: 
            self.f = jmri.jmrit.analogclock.AnalogClockFrame()
        self.f.setVisible(True)
         
    def set_fast_clock(self):
        jmri.jmrit.simpleclock.SimpleClockAction().actionPerformed(None)
        
    # def set_scheduled_trains(self):
        # strains = jmri.jmrit.operations.trains.TrainsTableAction()
        # strains.actionPerformed(None)
        
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
        self.logLevel = 2
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
    
        self.curr_time = event.newValue
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
        
class RunTrain(MoveTrain):

    def __init__(self, train, graph):
        self.logLevel = 1
        if train == None:
            if self.logLevel > 0: print "RunTrain: train == None"
        else:
            if self.logLevel > 0: print "RunTrain: train =", train
            self.logLevel = 1
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
                    self.move_between_stations(station_from, station_to, train_to_move, self.graph)
                else:
                    msg = "No train in block for scheduled train starting from " + station_from
                    title = "Scheduling Error"
                    opt1 = "Not scheduling train"
                    if self.logLevel > 0: print "No train in block for scheduled train starting from " + station_from
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
        

class RunRoute(MoveTrain):

    def __init__(self, route, graph, station_from, station_to, repeat):
        #note station_to and station_from are strings, while elements of route are locations
        self.logLevel = 1
        if route == None:
            if self.logLevel > 0: print "RunRoute: route == None"
        else:
            if self.logLevel > 0: print "RunRoute: route =", route
            self.graph = graph
            self.route = route
            self.station_from = station_from
            self.station_to = station_to
            self.repeat = repeat           
        
    def handle(self):    # Need to overload handle
        self.run_route()
        if self.repeat == True:
            return True
        else:
            return False

    def run_route(self):
        if self.logLevel > 0: print "************************************run train******************"
        if self.logLevel > 0:  "!     start run_route"
        station_list_locations = self.route.getLocationsBySequenceList()
        #convert station_list to strings
        station_list = [location.getName() for location in station_list_locations]
        if self.logLevel > 0: print "station_list before", station_list, "self.station_from",self.station_from,"self.station_to",self.station_to,"station_list[0]",station_list[0]
        if self.station_from == None:                       #ensure route starts at station_from
            pass
        elif self.station_from != station_list[0]:
            station_list.insert(0,self.station_from)
        if self.logLevel > 0: print "station_list",station_list
        if self.station_to == None:                         #ensure route ends at station_to
            pass
        elif self.station_to != station_list[-1]:
            
            station_list.append(self.station_to)
        if self.logLevel > 0: print "station_list",station_list
        if self.repeat == True:                             #ensure route end at start point if repeating
            if self.station_to != self.station_from:
                station_list.append(self.station_to)
        if self.logLevel > 0: print "station_list after", station_list        
        station_from = None
        for station in station_list:
            station_to = station  # both now strings
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
                    self.move_between_stations(station_from, station_to, train_to_move, self.graph)
                else:
                    msg = "No train in block for scheduled train starting from " + station_from
                    title = "Scheduling Error"
                    opt1 = "Not scheduling train"
                    if self.logLevel > 0: print "No train in block for scheduled train starting from " + station_from
                    OptionDialog().customMessage(msg, title, opt1)
                    doNotRun = True
                    break
                    
            station_from = station_to
        self.waitMsec(4000)
        if self.logLevel > 0:  "!     finished run_train"
        
