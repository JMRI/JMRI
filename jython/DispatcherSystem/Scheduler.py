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
from org.python.core.util import StringUtil


# fast_clock_rate = 12

trains_to_be_scheduled = []
run_train_dict = {}
scheduled = {}
RunTrain_instance = {}
tListener = None

class SchedulerMaster(jmri.jmrit.automat.AbstractAutomaton):

    def __init__(self):
        self.logLevel = 0

    def setup(self):
        global schedule_trains_hourly
        global schedule_trains_glb

        schedule_trains_glb = True        # do not schedule trains initially

        if 'schedule_trains_hourly' not in globals():
            schedule_trains_hourly = False

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
        global schedule_trains_hourly
        global schedule_trains_glb
        self.button_sensors_to_watch = self.set_button_sensors_to_watch()
        button_sensors_to_watch_JavaList = java.util.Arrays.asList(self.button_sensors_to_watch)

        self.waitSensorState(button_sensors_to_watch_JavaList, ACTIVE)

        if self.scheduler_master_sensor.getKnownState() == ACTIVE:   # pause processing if we turn the sensor off

            if self.logLevel > 0:  print("checking valid operations trains")
            if self.logLevel > 0: print "train_scheduler_setup",self.train_scheduler_setup

            # check whether can/want to schedule trains every hour
            schedule_trains_hourly = self.check_whether_schedule_trains_every_hour()
            # set ttme to midnight
            if self.logLevel > 0: print "set minute time listener"
            self.setup_minute_time_listener_to_schedule_trains()   # this
            timebase.setRun(True)
            self.train_scheduler_setup = True

            self.scheduler_master_sensor.setKnownState(INACTIVE)

        # if self.logLevel > 0:  print "1 ++++++++++++++++++++++++++++schedule_trains_glb", schedule_trains_glb
        # if schedule_trains_glb:
        #     #process the trains in trains_to_be_scheduled
        #     if self.logLevel > 0:  print("running run_trains")
        #     self.run_trains()

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
        # print "end 1"
        return True

    def check_whether_schedule_trains_every_hour(self):
        global timebase
        if self.all_trains_in_schedule_within_one_hour_period():
            # ask whether want to schedule each hour
            title = "want to schedule every hour?"
            msg = "you can schedule every hour if you want, as all schedules are within 1 hour period"
            opt1 = "schedule every hour"
            opt2 = "just schedule at given times"
            reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
            if reply == opt2:
                self.set_default_scheduling__non_hourly_values()
                self.set_period_trains_will_run()
                return False
            elif reply == opt1:
                # timebase.setRun(False)
                self.set_default_scheduling_hourly_values()
                # set period over which trains will run
                self.set_period_trains_will_run()
                return True
        else:
            self.set_default_scheduling__non_hourly_values()
            self.set_period_trains_will_run()
            return False
    def start_and_end_time_scheduling(self):
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
        return [start_time_hour, end_time_hour]
    def all_trains_in_schedule_within_one_hour_period(self):
        [start_time_hour, end_time_hour] = self.start_and_end_time_scheduling()
        # print "end_time_hour - start_time_hour < 1", end_time_hour - start_time_hour < 1
        if end_time_hour - start_time_hour < 1:
            if self.logLevel > 0: print "all_trains_in_schedule_within_one_hour_period"
            return True
        else:
            if self.logLevel > 0: print "all_trains_in_schedule more than one_hour_period"
            return False

        # TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        # train_list = TrainManager.getTrainsByTimeList()
        #
        # my_return = True
        # hour = "fred"
        # if train_list == []:
        #     return False
        # for i, train in enumerate(train_list):
        #     if self.logLevel > 1: print "*******************"
        #     if self.logLevel > 1: print ("train list: departure time: ", str(train.getDepartureTime()), str(train.getName()))
        #     if self.logLevel > 1: print "prev_time", self.prev_time, "curr_time", self.curr_time, \
        #         "train.getDepartTimeMinutes()", train.getDepartTimeMinutes()
        #
        #     # if i == 0:
        #     #     hour = train.getDepartTimeMinutes() // 60
        #     #     if self.logLevel > 0: print "hour", hour
        #     # else:
        #     #     if self.logLevel > 0: print "hour2", hour
        #     #     if hour != train.getDepartTimeMinutes() // 60:    train not in the same hour as first train
        #     #         my_return = False
        #     #     pass
        #
        # return my_return

    def set_period_trains_will_run(self):
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl

        frame = jmri.util.JmriJFrame('Scedule Trains Hourly');

        panel = JPanel()
        panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
        frame.add(panel)

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

        stringToDisplay = "scheduling in operation: " + str(scheduling_in_operation_gbl)
        rowFStage1Button_1 = JLabel(stringToDisplay)
        rowFStage1Button_1.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));
        rowFStage1Button_1.add(Box.createHorizontalGlue());
        rowFStage1Button_1.setAlignmentX(rowFStage1Button_1.LEFT_ALIGNMENT)
        
        rowStage1Button_1 = JButton("Stage1", actionPerformed = self.CheckHourlyParameters_action)
        stage1Button = rowStage1Button_1
        
        

        rowStage2Button = JPanel()
        rowStage2Button.setLayout(BoxLayout(rowStage2Button, BoxLayout.X_AXIS))
        rowrowStage2Button_2 = JLabel("Start Scheduling Trains")
        rowrowStage2Button_2.setFont(rowTitle_22.getFont().deriveFont(Font.BOLD, 13));

        rowrowStage2Button_2.add(Box.createHorizontalGlue());
        rowrowStage2Button_2.setAlignmentX(rowrowStage2Button_2.LEFT_ALIGNMENT)
        rowStage2Button_2 = JButton("Stage2", actionPerformed = self.StartSchedulingTrains_action)
        stage2Button = rowStage2Button_2

        rowStage3Button = JPanel()
        rowStage3Button.setLayout(BoxLayout(rowStage3Button, BoxLayout.X_AXIS))
        rowrowStage3Button_3 = JLabel("Stop Scheduling Trains")
        rowrowStage3Button_3.setFont(rowTitle_33.getFont().deriveFont(Font.BOLD, 13));

        rowrowStage3Button_3.add(Box.createHorizontalGlue());
        rowrowStage3Button_3.setAlignmentX(rowrowStage3Button_3.LEFT_ALIGNMENT)
        rowStage3Button_3 = JButton("Stage3", actionPerformed = self.StopSchedulingtrains_action)
        stage3Button = rowStage3Button_3


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
        panel.add(self.leftJustify(rowStage3Separator))

        frame.pack()
        frame.setVisible(True)
        frame.setSize(430, 250);

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
    def StartSchedulingTrains_action(self, event):
        global instanceList
        global schedule_trains_glb
        global scheduling_in_operation_gbl

        if self.logLevel > 0: print "StartSchedulingTrains_action"
        # self.close_this_panel(event)
        if self.logLevel > 0: print "B"
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl

        # read parameters
        [self.start_hour, self.end_hour, fast_clock_rate, self.speed_not_operational, \
         self.scheduling_margin, self.scheduling_in_operation_1] = self.read_list()
        if self.logLevel > 0: print "read list" , [self.start_hour, self.end_hour, self.speed_not_operational]
        if self.start_hour == "":
            self.start_hour = "04"
            self.end_hour = "22"
            fast_clock_rate = "10"
            self.speed_not_operational = "100"
            self.scheduling_margin = "10"
        start_hour_gbl = self.start_hour
        end_hour_gbl = self.end_hour
        speed_not_operational_gbl = self.speed_not_operational
        scheduling_margin_gbl = self.scheduling_margin

        if self.logLevel > 0: print "%%%%%%%%%%%%%%%%%%%%%%%%%  speed_not_operational_gbl has been set %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%", speed_not_operational_gbl
        if self.logLevel > 0: print "speed_not_operational_gbl defined ", TimeListener().speed_not_operational_gbl__is_defined()

        #start scheduler

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

        scheduling_in_operation_gbl = "True"
        stringToDisplay = "scheduling in operation: " + str(scheduling_in_operation_gbl)
        rowFStage1Button_1.setText(stringToDisplay) # Update the label
    def StopSchedulingtrains_action(self, event):
        global schedule_trains_glb
        global scheduling_in_operation_gbl

        schedule_trains_glb = False

        #stop Scheduler
        self.stop_schedule_trains_threads()

        scheduling_in_operation_gbl = "False"
        stringToDisplay = "scheduling in operation: " + str(scheduling_in_operation_gbl)
        rowFStage1Button_1.setText(stringToDisplay) # Update the label

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


    def set_default_scheduling_hourly_values(self):

        self.show_analog_clock()      # show the analog clock

        print "set_default_scheduling_hourly_values"

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
            scheduling_margin_gbl = "10"
            scheduling_in_operation_gbl = "False"

        self.write_list([start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                         scheduling_margin_gbl, scheduling_in_operation_gbl])

        print "[start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                         scheduling_margin_gbl, scheduling_in_operation_gbl]", [start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                                                                                scheduling_margin_gbl, scheduling_in_operation_gbl]

    def set_default_scheduling__non_hourly_values(self):

        self.show_analog_clock()      # show the analog clock

        print "set_default_scheduling__non_hourly_values"

        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl
        # read parameters
        [start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
         scheduling_margin_gbl, scheduling_in_operation_gbl] = self.read_list()

        if start_hour_gbl == "":
            fast_clock_rate = "10"
            speed_not_operational_gbl = "100"
            scheduling_margin_gbl = "10"
            scheduling_in_operation_gbl = "False"

        [start_hour_gbl, end_hour_gbl] = self.start_and_end_time_scheduling()
        end_hour_gbl += 1

        self.write_list([start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                         scheduling_margin_gbl, scheduling_in_operation_gbl])

        print "[start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                         scheduling_margin_gbl, scheduling_in_operation_gbl]", \
            [start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                scheduling_margin_gbl, scheduling_in_operation_gbl]

    def CheckHourlyParameters_action(self, event):
        global rowAStage1Button_1,  rowBStage1Button_1, rowCStage1Button_1, rowDStage1Button_1, rowEStage1Button_1, rowFStage1Button_1
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
        if int(reply) >20:
            self.scheduling_margin = 20
        else:
            self.scheduling_margin = int(reply)
        if self.logLevel > 0: print "scheduling_margin", self.scheduling_margin

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

        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl

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

    def is_time_format(self, input):
        try:
            time.strptime(input, '%H:%M')
            return True
        except ValueError:
            return False

    def set_button_sensors_to_watch(self):
        button_sensors_to_watch = [self.scheduler_master_sensor, self.scheduler_view_scheduled_trains, \
                                   self.scheduler_edit_routes, self.scheduler_start_time_sensor, \
                                   self.scheduler_show_clock_sensor, self.help_sensor]
        return button_sensors_to_watch

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
        if schedule_trains_hourly:
            self.set_timebase_start_hour(int(start_hour_gbl)-1, 45)
            timebase.setRun(False)
            timebase.setRate(float(speed_not_operational_gbl))
        else:
            self.set_timebase_start_hour(4, 0)
            timebase.setRate(float(fast_clock_rate))
        if self.logLevel > 0: print "***********************************************finished timebase hour"


        # run fast clock 10 times as fast as normal

        # timebase.setRate(fast_clock_rate)
        # time = timebase.getTime()

        if schedule_trains_hourly:
            self.set_default_scheduling_hourly_values()
        else:
            self.set_default_scheduling__non_hourly_values()

        # attach a listener to the timebase.
        tListener = TimeListener()
        timebase.addMinuteChangeListener(tListener)

        self.init = True

    def set_timebase_start_hour(self, hour, minute):
        from java.util import Date
        global timebase
        timebase = jmri.InstanceManager.getDefault(jmri.Timebase)
        date = Date(2020,10,21)     #any date really
        date.setHours(hour)
        date.setMinutes(minute)
        timebase.userSetTime(date)   ## set to 00:00 21/10/2020 (want the time to be 00:00)

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
        if self.logLevel > 0: print "A"
        if TimeListener().speed_not_operational_gbl__is_defined():   #we have set up the parameters for hourly working
            if self.logLevel > 0: print "C"
            msg = "Choose"
            title = "Set Start Time"
            opt1 = "Set to beginning of hourly train working"
            opt2 = "Open screen to set arbitrary time"
            reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
            if reply == opt1:
                self.set_time_to_beginning_of_hourly_train_working()
            else:
                jmri.jmrit.simpleclock.SimpleClockAction().actionPerformed(None)
        else:
            if self.logLevel > 0: print "B"
            jmri.jmrit.simpleclock.SimpleClockAction().actionPerformed(None)

    def set_time_to_beginning_of_hourly_train_working(self):
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl
        # start_hour_gbl is the start of hourly working
        start_hour = int(start_hour_gbl)
        if self.logLevel > 0: print "start_hour", start_hour
        self.set_timebase_start_hour(start_hour -1, 55)

    def show_routes(self):
        a = jmri.jmrit.operations.routes.RoutesTableAction()
        a.actionPerformed(None)


    def show_operations_trains(self):
        a = jmri.jmrit.operations.trains.TrainsTableAction()
        a.actionPerformed(None)


from java.util.concurrent import TimeUnit

class TimeListener(java.beans.PropertyChangeListener):
    # This is called every minute since it has been attached as timebase.addMinuteChangeListener(tListener)
    def __init__(self):
        self.scheduler_master_sensor = sensors.getSensor("SchedulerSensor3")
        self.logLevel = 0
        self.prev_time = 0
        self.inhibit_fast_clock_error_message()

    def inhibit_fast_clock_error_message(self):
        global timebase
        # stop a error message appearing when the fast clock is changed by TimeListener
        timebase.inhibitErrorMessage = True

    def propertyChange(self, event):

        global fast_clock_running_at_operational_speed
        if 'fast_clock_running_at_at_operational_speed' not in globals():
            fast_clock_running_at_operational_speed = True
        global minutes_old2, minutes_old3
        global schedule_trains_hourly
        if 'minutes_old2' not in globals():
            minutes_old2 = -1
        # if self.logLevel > 0: print "TimeListener: change",event.propertyName, "from", event.oldValue, "to", event.newValue
        # print "TimeListener: change",event.propertyName, "from", event.oldValue, "to", event.newValue
        minutes_old = int(event.getOldValue())
        minutes = int(event.getNewValue())
        try:
            if self.logLevel > 0: print "1 minutes_old", minutes_old, "minutes", minutes, "minutes_old2", minutes_old2, \
                "(minutes - minutes_old2) % 60 ", (minutes - minutes_old2) % 60
        except:
            minutes_old2 = -1    # default value for minutes_old2
            if self.logLevel > 0: print "2 minutes_old", minutes_old, "minutes", minutes, "minutes_old2", minutes_old2, \
                "(minutes - minutes_old2) % 60 ", (minutes - minutes_old2) % 60

        if self.logLevel > 0: print "property change", event.newValue
        if (minutes - minutes_old2) % 60 == 1:      # when we set the fast clock in the event timer it triggers a new event at the same time
                                                    # we then get into a recursion. This ignores the second call at the same time
            if self.logLevel > 0: print "3 minutes_old", minutes_old, "minutes", minutes, "minutes_old2", minutes_old2, \
                "(minutes - minutes_old2) % 60 ", (minutes - minutes_old2) % 60

            if int(minutes) % 10 == 0:               # only check every 10 minutes to prevent problens at non_operational_speeds
                                                # don't just check at 0 minutes in case train us started not on the hour

                if self.logLevel > 0: print "minutes", int(minutes), "int(minutes) % 10", int(minutes) % 10, "minutes", minutes
                minutes_old2 += 1
                self.set_fast_clock_rate()      # sets global fast_clock_at_operational_speed


        # if fast_clock_running_at_operational_speed:
        #     pass
            self.process_operations_trains(event)    # schedules trains
            print "attempting to send timetable via mqtt"
            self.send_timetable_via_mqtt(event)
        minutes_old2 = minutes

        # if self.scheduler_master_sensor.getKnownState() == ACTIVE:
        # self.process_operations_trains(event)
        #return

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

            if schedule_trains_hourly:
                pass
                hour = int(timebase.getTime().getHours())
                rate = timebase.getRate()

                if self.logLevel > 0: print "set_fast_clock_rate:", "schedule_trains_hourly", schedule_trains_hourly
                fast_clock_during_non_operational_times = speed_not_operational_gbl
                if self.logLevel > 0: print "fast clock during non operational_times", fast_clock_during_non_operational_times
                if self.logLevel > 0: print "hour", hour, "start_hour_gbl", start_hour_gbl, "end_hour_gbl", end_hour_gbl, \
                    "fast_clock_rate", fast_clock_rate, "speed_not_operational_gbl", speed_not_operational_gbl, \
                    "hour >= start_hour_gbl and hour <= end_hour_gbl", hour >= int(start_hour_gbl) and hour <= int(end_hour_gbl)
                if hour >= int(start_hour_gbl) and hour <= int(end_hour_gbl):
                    if rate != fast_clock_rate:       # check to stop recursion error
                        timebase.setRate(float(fast_clock_rate))
                        fast_clock_running_at_operational_speed = True
                    if self.logLevel > 0: print "set_fast_clock_rate:", "fast_clock_rate slow", fast_clock_rate
                    pass
                else:
                    if self.logLevel > 0: print "fast_clock_during_non_operational_times", fast_clock_during_non_operational_times
                    fcr = fast_clock_during_non_operational_times
                    if fcr > 100 : fcr = 100  # set to maximum
                    if rate != fcr:
                        timebase.setRate(float(fcr))
                        fast_clock_running_at_operational_speed = False
                    if self.logLevel > 0: print "set_fast_clock_rate fqst:", "fcr", fcr

    def send_timetable_via_mqtt(self, event):
        hour = int(timebase.getTime().getHours())
        minutes = event.newValue

        # get list of origins, destinations and times at intermediate stations
        timetable = self.get_timetable(hour, minutes)
        # print "timetable", timetable

        # send mqtt message
        self.send_timetable_messages(timetable)
        # try:
        #     print "sending message 2"
        #     self.send_mqtt_messages(timetable)
        #     print "message sent"
        # except:
        #     print "mqtt not set up: cannot publish timetable"

    def send_timetable_messages(self,timetable):

        for [station_name, station_departure_time, last_station, last_station_arrival_time, via] in timetable:
            msg = '{"station_name" : "' + str(station_name) + '", ' + \
                   '"station_departure_hour" : "' + str(station_departure_time) + '", ' + \
                   '"last_station" : "' + str(last_station) + '", ' + \
                   '"last_station_arrival_time" : "' + str(last_station_arrival_time) + '", ' + \
                   '"via" : "' + str(via) + '"}'
            self.send_mqtt_message(msg)

    def send_mqtt_message(self, msg):

        # Find the MqttAdapter
        mqttAdapter = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()

        # create content to send "/jmri/timetable message content"
        topic = "jmri/timetable"
        payload = msg

        # send
        mqttAdapter.publish(topic, payload)

    def get_timetable(self, hour, minutes):

        global schedule_trains_hourly

        self.curr_time = minutes + hour * 60

        timetable = []

        mqttAdapter = jmri.InstanceManager.getDefault( jmri.jmrix.mqtt.MqttSystemConnectionMemo ).getMqttAdapter()
        # iterate through the trains scheduled in the xurrent and nex hour
        # if schedule_trains_hourly:
        #     if int(start_hour_gbl) <= hour <= int(end_hour_gbl):
        #         pass    # need to process trains
        #     else:
        #         if self.logLevel > 0: print "returning in process_operational_trains"
        #         return  # outside operational time

        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        # if schedule_trains_hourly:
        print ("schedule_trains_hourly", schedule_trains_hourly)
        if schedule_trains_hourly:
            if self.logLevel > 0: print "A5"
            tts = [train for train in train_list
                   if "skip" not in train.getDescription()]    #if skip in description of scheduled Train do not run the train
            trains_to_start = []
            for train in tts:
                if self.logLevel > 0: print "hour_before", train.getDepartTimeMinutes() // 60
                train_mins = str(int(train.getDepartTimeMinutes()) % 60)
                if int(start_hour_gbl) <= hour <= int(end_hour_gbl):
                    if train_mins > minutes:
                        train_hour = str(hour)
                    else:
                        train_hour = str(hour+1)
                else:
                    train_hour = start_hour_gbl   # (beginning of following day)
                train_route_start_time = train_hour + ":" + train_mins
                train_route = train.getRoute()
                # location.getComment() will be set to the departure timed if the routine to do this has been run
                last_location = train_route.getTerminatesRouteLocation()
                [last_station, last_station_arrival_time] = [str(last_location.getName()), str(last_location.getComment())]
                via = train_route.getLocationsBySequenceList()

                for i, location in enumerate(train_route.getLocationsBySequenceList()):
                    timetable_entry_names = ["station_name", "station_departure_time", "last_station",
                                             "last_station_arrival_time", "via"]
                    # timetable.append(timetable_entry_names)
                    if i == 0:
                        station_departure_time = train_route_start_time
                        time_to_station = 0
                    else:
                        # if location.getComment() == "" or location.getComment() == None:
                        if True:
                            time_to_station = 10   # just a botch for now
                        else:
                            time_to_station = location.getComment()
                            print "time_to_station", time_to_station

                    station_name = str(location.getName())
                    station_departure_time = self.calc_time(station_departure_time, time_to_station)
                    location.setComment(str(time_to_station))
                    # timetable_entry = [station_name, station_departure_time, last_station, last_station_arrival_time, via]
                    timetable_entry = [station_name , \
                                       station_departure_time, \
                                       last_station, \
                                       last_station_arrival_time,
                                       via]

                    print "timetable_entry", timetable_entry
                    timetable.append(timetable_entry)
        else:
            timetable = [["a", "b", "c", "d", "e"]]
        return timetable

    def calc_time(self, station_departure_time, time_to_station):

        # add time_to_station to station_departure time
        # station_departure_time is in form hh:mm
        print "time_to_station", time_to_station  , "should be mins"
        [hours, mins] = station_departure_time.split(":")
        print "hours", hours, "mins", mins
        hour = int(hours) + int(time_to_station) // 60
        min = int(mins) + int(time_to_station) % 60
        print "hour", hour, "min", min
        station_departure_time_new = str(hour) + ":" + str(min)
        print "station_departure_time_new", station_departure_time_new
        return station_departure_time_new





    def process_operations_trains(self, event ):
        global timebase
        global schedule_trains_hourly
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, scheduling_margin_gbl, scheduling_in_operation_gbl
        global scheduled
        global trains_to_be_scheduled
        if self.logLevel > -1: print "A1"
        if 'schedule_trains_hourly' not in globals():
            schedule_trains_hourly = False
        if self.logLevel > 0: print "in process_operations_trains", "schedule_trains_hourly", schedule_trains_hourly


        hour = int(timebase.getTime().getHours())
        if self.logLevel > 0: print "type hour" , type(hour)
        minutes = event.newValue
        if self.logLevel > 0: print "type minutes", type(minutes)
        self.curr_time = minutes + hour * 60
        if self.logLevel > 0: print "curr_time", self.curr_time
        self.prev_time = self.curr_time -1
        if self.logLevel > 0: print "prev_time", self.prev_time

        if schedule_trains_hourly:
            if int(start_hour_gbl) <= hour <= int(end_hour_gbl):
                pass    # need to process trains
            else:
                if self.logLevel > 0: print "returning in process_operational_trains"
                return  # outside operational time

        # self.set_fast_clock_rate(timebase, hour)

        # if self.speed_not_operational_gbl__is_defined():
        #     if schedule_trains_hourly:
        #         fast_clock_multiplier = speed_not_operational_gbl
        #         if hour >= int(start_hour_gbl) and hour <= int(end_hour_gbl):
        #             fcr = fast_clock_rate * fast_clock_multiplier
        #             timebase.setRate(fcr)
        #         else:
        #             timebase.setRate(fast_clock_rate)

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

        # if schedule_trains_hourly:
        if schedule_trains_hourly:
            if self.logLevel > 0: print "A5"
            tts = [train for train in train_list
                               if (minutes-1 < (int(train.getDepartTimeMinutes()) % 60) <= minutes) and
                               "skip" not in train.getDescription()]    #if skip in description of scheduled Train do not run the train
            trains_to_start = []
            for train in tts:
                if self.logLevel > 0: print "hour_before", train.getDepartTimeMinutes() // 60
                train_mins = str(int(train.getDepartTimeMinutes()) % 60)
                train_hour = str(hour)
                if self.logLevel > 0: print "train_mins", train_mins
                train.setDepartureTime(train_hour, train_mins)
                if self.logLevel > 0: print "hour", train.getDepartTimeMinutes() // 60
                trains_to_start.append(train)

            # print "trains_to_start", trains_to_start
            # # need to set the time of a copy of the trains_to_start to the current hour (if within the desired running range)
            # copy_trains_to_start = copy.deepcopy(trains_to_start)
            # for train in trains_to_start:
            #     if self.logLevel > 0: print "hour_before", train.getDepartTimeMinutes() // 60
            #     train_mins = str(int(train.getDepartTimeMinutes()) % 60)
            #     train_hour = str(hour)
            #     if self.logLevel > 0: print "train_mins", train_mins
            #     train.setDepartureTime(train_hour, train_mins)
            #     if self.logLevel > 0: print "hour", train.getDepartTimeMinutes() // 60
        else:
            trains_to_start = [train for train in train_list
                               if (self.prev_time < int(train.getDepartTimeMinutes()) <= self.curr_time) and
                               "skip" not in train.getDescription()]    #if skip in description of scheduled Train do not run the train
            # copy_trains_to_start = trains_to_start  #just renames trains_to_start
            if self.logLevel > 0: print "A7"

        if self.logLevel > 0: print "trains to start " , trains_to_start
        #self.run_trains(trains_to_start)

        if self.logLevel > 0: print "A8"
        for train in trains_to_start:
            if train not in trains_to_be_scheduled:
                if self.logLevel > 0: print "A9"
                trains_to_be_scheduled.append(train)
            scheduled[train] = False
        if self.logLevel > 0: print "trains_to_be_scheduled", trains_to_be_scheduled
        if self.logLevel > 0: print "trains_to_be_scheduled", trains_to_be_scheduled
        # if self.logLevel > 0: print "End of process_operations_trains", "trains_to_be_scheduled",trains_to_be_scheduled,\
        #     "scheduled",scheduled
        if self.logLevel > -1: print "A10"

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
        global start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
            scheduling_margin_gbl, scheduling_in_operation_gbl

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
        # set up station_list
        station_list_locations = self.route.getLocationsBySequenceList()
        #convert station_list to strings
        station_list = [location.getName() for location in station_list_locations]
        station_comment_list = [location.getComment()  for location in station_list_locations]

        self.station_list = station_list
        self.station_comment_list = station_comment_list

        train_dispatched = False

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

                    if train_to_move != None:
                        try: myframeold.dispose()
                        except: pass
                        try: myframe.dispose()
                        except: pass
                        # move train
                        if self.logLevel > 0: print "************************************moving train******************",train_to_move
                        move_train = MoveTrain(station_from, station_to, train_to_move, self.graph, station_comment)
                        move_train.move_between_stations(station_from, station_to, train_to_move, self.graph)
                        move_train = None
                        if self.logLevel > 0: print "finished move between stations station_from = ", station_from, " station_to = ", station_to
                        end_block = blocks.getBlock(station_to)
                        if self.logLevel > 0: print "state of block" , end_block.getState()
                        # do following in case the block sensor is a bit dodgy
                        end_block.setValue(train_to_move)

                        train_dispatched = True

                    else:
                        for i in range(int(scheduling_margin_gbl)):

                            fast_minute = 1000/int(fast_clock_rate)
                            self.waitMsec(fast_minute)

                            # if i != 0: # if we are not at the beginning of the roure
                            #     # the start block should have the train in it because the train has finished the previous move
                            #     start_block = blocks.getBlock(station_from)
                            #     LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
                            #     layoutBlock = LayoutBlockManager.getLayoutBlock(start_block)
                            #     if layoutBlock.getOccupancySensor().getKnownState() == INACTIVE:
                            #         layoutBlock.getOccupancySensor().setKnownState(ACTIVE)

                            # display custom_message
                            msg = "No train in block for scheduled train starting from " + station_from
                            msg2 = "Trying again for " + str(scheduling_margin_gbl) + " fast mins: minute" + str(i)

                            try:    myframeold = myframe     # myframe may not be fefined at this point
                            except: pass

                            myframe = self.show_custom_message_box(msg, msg2)

                            try:    myframeold.dispose()     # myframeold may not be fefined
                            except: pass

                            fast_minute = 1000/int(fast_clock_rate)
                            self.waitMsec(fast_minute)

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
                trains_to_be_scheduled.remove(self.train)
                self.waitMsec(4000)
            else:
                self.waitMsec(4000)
            if self.logLevel > 0:  print "!     finished run_train"
        except:
            self.waitMsec(4000)

    def show_custom_message_box(self, msg, msg2):
        frame = JFrame("Custom Message Box")
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

class ScheduleTrains(jmri.jmrit.automat.AbstractAutomaton):

    def setup(self):
        self.logLevel = 0
        return True

    def handle(self):
        self.run_trains()    #schedule trains if schedule_trains_glb is set True external to this routine
        # continue scheduling trains
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
            if fast_clock_running_at_operational_speed:
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
                if self.logLevel > 0:  print "!!!!!!!!!!!!!!!!!!!!!run_trains finished"
                if self.logLevel > 0:  print "trains_to_be_scheduled ", trains_to_be_scheduled
                if self.logLevel > 0:  print "timebase.getRate()",timebase.getRate()

        noMsec = int(1000/timebase.getRate())
        self.waitMsec(noMsec)  # every fast minute





