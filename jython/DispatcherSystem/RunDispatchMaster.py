from javax.swing import JOptionPane, JFrame, JLabel, JButton, JTextField, JFileChooser, JMenu, JMenuItem, JMenuBar,JComboBox,JDialog,JList
import sys
import java
import jmri
import re
from javax.swing import JOptionPane
import os
import imp
import copy
import org
import sys

FileResetButtonMaster = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/ResetButtonMaster.py')
execfile(FileResetButtonMaster)

StopDispatcherSystem = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/StopDispatcherSystem.py')
execfile(FileResetButtonMaster)

# FileMoveTrain has to go before CreateScheduler
FileMoveTrain = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/MoveTrain.py')
execfile(FileMoveTrain)

CreateScheduler = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Scheduler.py')
execfile(CreateScheduler)

CreateSchedulerPanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/SchedulerPanel.py')
execfile(CreateSchedulerPanel)

CreateSimulation = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Simulation.py')
execfile(CreateSimulation)

RunDispatch = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/RunDispatch.py')
exec(open(RunDispatch).read())

global instanceList

instanceList = []

class RunDispatcherMaster():

    def __init__(self):
        global g
        global le
        g = StationGraph()

        new_train_master = NewTrainMaster()
        instanceList.append(new_train_master)
        if new_train_master.setup():
            new_train_master.setName('New Train Master')
            new_train_master.start()
        # print "A"
        stop_master = StopMaster()
        if stop_master.setup():
            stop_master.setName('Stop Master')
            stop_master.start()
        # print "B"
        reset_button_master = ResetButtonMaster()
        instanceList.append(reset_button_master)
        if reset_button_master.setup():
            pass
            reset_button_master.setName('Reset Button Master')
            reset_button_master.start()
        # print "C"
        dispatch_master = DispatchMaster()
        instanceList.append(dispatch_master)
        if dispatch_master.setup():
            dispatch_master.setName('Dispatch Master')
            dispatch_master.start()
        # print "D"
        simulation_master = SimulationMaster()
        instanceList.append(simulation_master)
        if simulation_master.setup():
            simulation_master.setName('Simulation Master')
            simulation_master.start()
        # print "E"
        global scheduler_master      #global so cas be referenced before killing threads
        scheduler_master = SchedulerMaster()
        instanceList.append(scheduler_master)
        # print "E0"
        if scheduler_master.setup():
            # print "E1"
            scheduler_master.setName('Scheduler Master')
            # print "E2"
            scheduler_master.start()
            # print "E3"
        # print "F"
        monitorTrack_master = MonitorTrackMaster()
        instanceList.append(monitorTrack_master)
        if monitorTrack_master.setup():
            monitorTrack_master.setName('Monitor Track Master')
            monitorTrack_master.start()
        # print "G"
        off_action_master = OffActionMaster()
        instanceList.append(off_action_master)
        if off_action_master.setup():
            off_action_master.setName('Off-Action Master')
            off_action_master.start()
        else:
            if self.logLevel > 0: print("Off-Action Master not started")

        #set default values of buttons
        sensors.getSensor("Express").setKnownState(INACTIVE)
        sensors.getSensor("simulateSensor").setKnownState(INACTIVE)
        sensors.getSensor("setDispatchSensor").setKnownState(ACTIVE)
        sensors.getSensor("stopMasterSensor").setKnownState(INACTIVE)
        sensors.getSensor("modifyMasterSensor").setKnownState(INACTIVE)
        sensors.getSensor("checkRouteSensor").setKnownState(INACTIVE)
        sensors.getSensor("checkRouteSensor").setKnownState(ACTIVE)
        sensors.getSensor("soundSensor").setKnownState(INACTIVE)
        sensors.getSensor("stopAtStopSensor").setKnownState(ACTIVE)
        sensors.getSensor("editRoutesSensor").setKnownState(INACTIVE)
        sensors.getSensor("viewScheduledSensor").setKnownState(INACTIVE)
        sensors.getSensor("showClockSensor").setKnownState(INACTIVE)
        sensors.getSensor("timetableSensor").setKnownState(INACTIVE)
        sensors.getSensor("departureTimeSensor").setKnownState(INACTIVE)
        sensors.getSensor("helpSensor").setKnownState(INACTIVE)

        global stored_simulate
        if 'stored_simulate' in globals():
            if stored_simulate == ACTIVE:
                sensors.getSensor("simulateSensor").setKnownState(ACTIVE)


if __name__ == '__builtin__':
    sensors.getSensor("stopMasterSensor").setKnownState(INACTIVE)
    sensors.getSensor("modifyMasterSensor").setKnownState(INACTIVE)
    # for frame1 in java.awt.Frame.getFrames():
    #     # print "frame", frame1.getName()
    #     if frame1.getName() == "Dispatcher System":
    #         # print "deleting frame", frame.getName()
    #         frame1.invalidate();
    #         frame1.validate();
    #         frame1.repaint();
    msg = "Press section buttons to set dispatch \nA train needs to be set up in a section first"
    OptionDialog().displayMessage(msg)


    RunDispatcherMaster()
    pass
