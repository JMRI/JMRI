import jmri

RunDispatch = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/RunDispatch.py')
exec(open(RunDispatch).read())

FileResetButtonMaster = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/ResetButtonMaster.py')
exec(open(FileResetButtonMaster).read())

StopDispatcherSystem = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/StopDispatcherSystem.py')
exec(open(FileResetButtonMaster).read())

# FileMoveTrain has to go before CreateScheduler
FileMoveTrain = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/MoveTrain.py')
exec(open(FileMoveTrain).read())

CreateScheduler = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Scheduler.py')
exec(open(CreateScheduler).read())

CreateSchedulerPanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/SchedulerPanel.py')
exec(open(CreateSchedulerPanel).read())

CreateSimulation = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Simulation.py')
exec(open(CreateSimulation).read())

CreatePlatformPanel = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/PlatformPanel.py')
exec(open(CreatePlatformPanel).read())

global instanceList

instanceList = []

class RunDispatcherMaster(jmri.jmrit.automat.AbstractAutomaton ):

    def __init__(self):
        global g
        global le
        global glb_reset_all_trains

        self.logLevel = 0
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

        global scheduler_master      #global so cas be referenced before killing threads
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

        # ensure the memory label contents are wiped out if we are starting from scratch

        if "glb_reset_all_trains" not in globals():
            glb_reset_all_trains = True     # first time round delete all memory variables

        # print "glb_reset_all_trains", glb_reset_all_trains

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

        self.waitMsec(2000)   #wait for panel to load, it may have train values
        if glb_reset_all_trains == True:
            # print "removing train values"
            StopMaster().remove_train_values()
            # StopMaster().remove_all_trains_from_trains_allocated()
