###############################################################################
#
# class MoveTrain
# Calls dispatcher to move train from one station to another
# given engine and start and end positions
#
###############################################################################

class MoveTrain(jmri.jmrit.automat.AbstractAutomaton):

    global trains_dispatched

    def __init__(self, station_from_name, station_to_name, train_name, graph):
        self.logLevel = 0
        self.station_from_name = station_from_name
        self.station_to_name = station_to_name
        self.train_name = train_name
        self.graph = graph

    def setup(self):
        return True

    def handle(self):
        #move between stations in the thread
        if self.logLevel > 1: print"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        if self.logLevel > 1: print "move between stations in the thread"
        if self.logLevel > 1: print"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        self.move_between_stations(self.station_from_name, self.station_to_name, self.train_name, self.graph)
        return False

    def move_between_stations(self, station_from_name, station_to_name, train_name, graph):
        if self.logLevel > 1: print "Moving from " + station_from_name + " to " + station_to_name
        #need to look up the required transit in the graph
        StateVertex_start = station_from_name
        StateVertex_end = station_to_name
        # for e in graph.edgeSet():
            # if self.logLevel > 1: print (graph.getEdgeSource(e) + " --> " + graph.getEdgeTarget(e))
        if self.logLevel > 1: print "calling shortest path", StateVertex_start, StateVertex_end
        paths = DijkstraShortestPath.findPathBetween(graph, StateVertex_start, StateVertex_end)
        if self.logLevel > 0: print "graph", graph
        if self.logLevel > 0: print "paths", paths
        if self.logLevel > 0: print "returned from shortest path"
        if self.logLevel > 1: print "in move_between_stations trains = ", trains, "train_name = ", train_name
        train = trains[train_name]
        if self.logLevel > 1: print "train" , train
        penultimate_block_name = train["penultimate_block_name"]
        if self.logLevel > 1: print "penultimate_block_name" , penultimate_block_name
        previous_edge = train["edge"]
        previous_direction = train["direction"]

        trains_dispatched.append(str(train_name))

        count_path = 0

        for e in paths:

            # need to check whether:
            #   last block of previous edge and current first block
            #   are the same

            # if the same the train must change direction. as we are going in and out the same path
            #
            previous_edge = train["edge"]
            penultimate_block_name = train["penultimate_block_name"]
            previous_direction = train["direction"]
            current_edge = e
            neighbor_name = e.getItem("neighbor_name")
            if self.logLevel > -1: print train
            if self.logLevel > -1: print "neighbor_name = ", neighbor_name
            if self.logLevel > -1: print "penultimate_block_name" , penultimate_block_name


            #following is wrong
            # if penultimate_block_name == neighbor_name:
                # transit_instruction = "buffer"
            # else:
                # transit_instruction = "straight on"
            # if self.logLevel > -1: print "transit_instruction=",transit_instruction

            # if transit_instruction == "buffer":
                # if previous_direction == "forward":
                    # transit_direction = "reverse"
                # else:
                    # transit_direction = "forward"
            # else:
                # transit_direction = previous_direction

            # if self.logLevel > 1: print "transit_direction",transit_direction

            BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
            previous_block = BlockManager.getBlock(penultimate_block_name)
            current_block = BlockManager.getBlock(previous_edge.getItem("last_block_name"))
            next_block = BlockManager.getBlock(current_edge.getItem("second_block_name"))
            if count_path == 0:
                # we are on a new path and must determine the direction
                [transit_direction, transit_instruction]  = self.set_direction(previous_block, current_block, next_block, previous_direction)
            else:
                # if there are several edges in a path, then we are on an express route, and there is a change in direction at each junction
                if previous_block.getUserName() == next_block.getUserName() : #we are at a stub/siding
                    if previous_direction == "forward":
                        transit_direction = "reverse"
                    else:
                        transit_direction = "forward"
                    transit_instruction = "stub"
                else:
                    [transit_direction, transit_instruction] = self.set_direction(previous_block, current_block, next_block, previous_direction)
            result = self.move(e, transit_direction, transit_instruction,  train_name)
            if self.logLevel > 1: print "returned from self.move, result = ", result
            if result == False:
                trains_dispatched.remove(str(train_name))
                break
            #store the current edge for next move
            train["edge"] = e
            train["penultimate_block_name"] = e.getItem("penultimate_block_name")
            train["direction"] = transit_direction
            count_path +=1

        if self.logLevel > 1: print "transit finished, removing train from dispatch list"
        if str(train_name) in trains_dispatched:
            trains_dispatched.remove(str(train_name))
        if self.logLevel > 1: print "trains_dispatched", trains_dispatched

    def set_direction(self, previous_block, current_block, next_block, previous_direction):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        current_layout_block = LayoutBlockManager.getLayoutBlock(current_block)
        if current_layout_block.validThroughPath(previous_block, next_block):
            transit_instruction = "same"
        else:
            transit_instruction = "change"
        if transit_instruction == "change":
            if previous_direction == "forward":
                transit_direction = "reverse"
            else:
                transit_direction = "forward"
        else:
            transit_direction = previous_direction
        return [transit_direction, transit_instruction]

    def move(self, e, direction, instruction, train):
        if self.logLevel > 1: print "++++++++++++++++++++++++"
        if self.logLevel > 1: print e, "Target", e.getTarget()
        if self.logLevel > 1: print e, "Source", e.getSource()
        if self.logLevel > 1: print e, "Train", train
        if self.logLevel > 1: print "++++++++++++++++++++++++"
        to_name = e.getTarget()
        from_name = e.getSource()
        sensor_move_name = "MoveInProgress"+to_name.replace(" ","_")

        self.set_sensor(sensor_move_name, "active")
        speech_reqd = self.speech_required_flag()
        self.announce( from_name, to_name, speech_reqd, direction, instruction)
        if self.logLevel > 1: print "***************************"
        result = self.call_dispatch(e, direction, train)
        if self.logLevel > 1: print "______________________"
        if result == True:
            #Wait for the Active Trains List to have the
            DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
            java_active_trains_list = DF.getActiveTrainsList()
            java_active_trains_Arraylist= java.util.ArrayList(java_active_trains_list)
            for t in java_active_trains_Arraylist:
                if self.logLevel > 1: print "t=",t,t.getActiveTrainName()
            #active_trains_list = java.util.Arrays.asList(java_active_trains_list)
            if self.logLevel > 1: print "!!!!!!!! train = ", train, "active_trains_list", java_active_trains_Arraylist
            active_train_names_list = [str(t.getTrainName()) for t in java_active_trains_Arraylist]
            if self.logLevel > 1: print "!!!!!!!! train = ", train, "active_trains_name_list", active_train_names_list
            while train in active_train_names_list:
                self.waitMsec(500)
                DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
                active_trains_list = DF.getActiveTrainsList()
                active_train_names_list = [str(t.getTrainName()) for t in java_active_trains_Arraylist]
                java_active_trains_Arraylist= java.util.ArrayList(java_active_trains_list)
                active_train_names_list = [str(t.getTrainName()) for t in java_active_trains_Arraylist]
                if self.logLevel > 1: print "!!!!!!!! train = ", train, "active_train_names_list", active_train_names_list
            self.set_sensor(sensor_move_name, "inactive")
            if self.logLevel > 1: print ("+++++ sensor " + sensor_move_name + " inactive")
            self.waitMsec(time_to_stop_in_station)
        else:
            self.set_sensor(sensor_move_name, "inactive")
        return result

    def speech_required_flag(self):
        self.sound_sensor = sensors.getSensor("soundSensor")
        if self.sound_sensor is None:
            OptionDialog().displayMessage("No sound Sensor set up")
            return None
        sound_state = self.sound_sensor.getKnownState()
        if self.logLevel > 1: print sound_state,ACTIVE
        if sound_state == ACTIVE:
            sound_flag = True
        else:
            sound_flag = False
        return sound_flag

    def call_dispatch(self, e, direction, train):
        if self.logLevel > 1: print ("in dispatch")
        to_name = e.getTarget()
        from_name = e.getSource()
        if self.logLevel > 1: print ("incall_dispatch: move from " + from_name + " to " + to_name)

        if direction == "forward":
            filename = self.get_filename(e, "fwd")
            #filename1 = "From " + from_name + " To " + to_name
        else:
            filename = self.get_filename(e, "rvs")
            #filename1 = "From " + from_name + " To " + to_name + " reverse"
        #filename = filename.replace(" ","_")

        if self.logLevel > 1: print "filename = ", filename, "direction = " , direction
        result = self.doDispatch(filename, "ROSTER", train)
        if self.logLevel > 1: print "result", result
        return result

    def get_filename(self, e, suffix):

        # suffix is "fwd" or "rvs"
        # graph is g.g_express
        # e is edge

        from_station_name = g.g_express.getEdgeSource(e)
        to_station_name = g.g_express.getEdgeTarget(e)
        neighbor_name = e.getItem("neighbor_name")
        index = e.getItem("index")

        filename = "From " + str(from_station_name) + " To " + str(to_station_name) + " Via " + str(neighbor_name) + " " + str(index)
        filename = filename.replace(" ", "_")
        # filename_fwd = filename + "_fwd.xml"
        # filename_rvs = filename + "_rvs.xml"
        filename = filename + "_" + suffix + ".xml"

        return filename

    #    Dispatch (<filename.xml>, [USER | ROSTER | OPERATIONS >,<dccAddress, RosterEntryName or Operations>

    def doDispatch(self, traininfoFileName, type, value):
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        #try:
        if self.logLevel > 1: print "traininfoFileName",traininfoFileName
        result = DF.loadTrainFromTrainInfo(traininfoFileName, type, value)
        if result == -1:
            if self.logLevel > 1: print "result from dispatcher frame" , result
            return False  #No train allocated
        else:
            if self.logLevel > 1: print "result from dispatcher frame" , result
            return True
        # except:
            # if self.logLevel > 1: print ("FAILURE tried to run dispatcher with file {} type {} value {}".format(traininfoFileName,  type, value))
            # pass
            # return False

    def doDispatchxxxx(self, traininfoFileName, type, train):
        if self.logLevel > 1: print "1"
        info = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(traininfoFileName)
        if self.logLevel > 1: print "2"
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        if self.logLevel > 1: print "3"
        DF.setVisible(True)
        if self.logLevel > 1: print "4"
        atf = DF.getActiveTrainFrame()
        re = None
        event = None
        block = None
        #atFrame.showActivateFrame(None)
        atf.initiateTrain(event,re, block)
        #atf.showActivateFrame(None)
        if self.logLevel > 1: print "6"
        atf.trainInfoToDialog(info)
        if self.logLevel > 1: print "7"
        #self.waitMsec(5000)
        robot = java.awt.Robot()
        KeyEvent = java.awt.event.KeyEvent
        robot.keyPress(KeyEvent.VK_SHIFT)
        robot.keyPress(KeyEvent.VK_TAB)
        robot.keyRelease(KeyEvent.VK_TAB)
        robot.keyRelease(KeyEvent.VK_SHIFT)
        #self.waitMsec(5000)
        #atf.addNewTrain(None)
        robot.keyPress(KeyEvent.VK_SPACE)
        robot.keyRelease(KeyEvent.VK_SPACE)
        #atf.addNewTrainButton.doClick()
        if self.logLevel > 1: print "8"
        DF.newTrainDone(null);
        return True

    def doDispatchyy(self, traininfoFileName, type, train):
        if self.logLevel > 1: print "In doDispatch: traininfoFileName",traininfoFileName,"type", type, "train", train
        info = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(traininfoFileName)
        if self.logLevel > 1: print "info.trainName", info.trainName
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        roster = 0x01
        tSource = roster
        initiateFrame = None
        at = DF.createActiveTrain(info.transitName, train, tSource, info.startBlockName,
                info.startBlockSeq, info.destinationBlockName, info.destinationBlockSeq, info.autoRun, info.dccAddress, info.priority,
                info.resetWhenDone, info.reverseAtEnd,  True, DF, info.allocationMethod)
        if (at == None):
            return  # error message sent by createActiveTrain
        if info.trainFromRoster == True:
            #if (info.tSource == ActiveTrain.ROSTER):
            RosterEntry = jmri.jmrit.roster.Roster.getDefault().getEntryForId(train)
            if (RosterEntry != None):
                at.setRosterEntry(RosterEntry)
                at.setDccAddress(RosterEntry.getDccAddress())
            else:
                if self.logLevel > 1: print("Roster Entry '{}' not found, could not create ActiveTrain '{}'",
                        trainNameToUse, info.getTrainName())
                return False
        at.setStarted()
        at.setAllocateMethod(info.allocationMethod)
        at.setDelayedStart(info.delayedStart)
        at.setDelayedRestart(info.delayedRestart)
        at.setDepartureTimeHr(info.departureTimeHr)
        at.setDepartureTimeMin(info.departureTimeMin)
        at.setRestartDelay(info.restartDelayMin)
        at.setDelaySensor(info.delaySensorName)
        at.setResetStartSensor(info.resetStartSensor)
        print "info.delayedStart",info.delayedStart
        if ((DF.isFastClockTimeGE(info.departureTimeHr, info.departureTimeMin) and info.delayedStart != at.SENSORDELAY)
                or info.delayedStart == at.NODELAY):
            print "set started"
            at.setStarted()
        at.setRestartSensor(info.restartSensorName)
        at.setResetRestartSensor(info.resetRestartSensor)
        at.setTrainType(info.trainType)
        at.setTerminateWhenDone(info.terminateWhenDone)

        aat = jmri.jmrit.dispatcher.AutoActiveTrain(at)
        if aat == None:
            JOptionPane.showMessageDialog(self.frame,'Could not create Auto Active Train','Active Train',JOptionPane.PLAIN_MESSAGE)
            return
        aat.setSpeedFactor(info.speedFactor);
        aat.setMaxSpeed(info.maxSpeed);

        RAMP_NONE = 0x00;  # No ramping - set speed immediately
        RAMP_FAST = 0x01;     # Fast ramping
        RAMP_MEDIUM = 0x02;  # Medium ramping
        RAMP_MED_SLOW = 0x03;  # Medium/slow ramping
        RAMP_SLOW = 0x04;  # Slow ramping
        aat.setRampRate(aat.getRampRateFromName(info.getRampRate()))
        aat.setResistanceWheels(info.resistanceWheels);
        aat.setRunInReverse(info.runInReverse);
        aat.setSoundDecoder(info.soundDecoder);
        aat.setMaxTrainLength(info.maxTrainLength);
        aat.setStopBySpeedProfile(info.stopBySpeedProfile);
        aat.setStopBySpeedProfileAdjust(info.stopBySpeedProfileAdjust);
        aat.setUseSpeedProfile(info.useSpeedProfile);
        if (not aat.initialize()):
            JOptionPane.showMessageDialog(initiateFrame, Bundle.getMessage(
                    "Error27", at.getTrainName()), Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE)
        DF.getAutoTrainsFrame().addAutoActiveTrain(aat)
        DF.allocateNewActiveTrain(at)
        # initiateFrame.setVisible(false)
        # initiateFrame.dispose()  # prevent this window from being listed in the Window menu.
        # initiateFrame = null
        DF.newTrainDone(at)
        #DF = None

        return True


    def set_sensor(self, sensorName, sensorState):
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('{} - Sensor {} not found'.format(self.threadName, sensorName))
            return
        if sensorState == 'active':
            newState = ACTIVE
        elif sensorState == 'inactive':
            if self.logLevel > 1: print "set_sensor ", sensorName, 'inactive'
            newState = INACTIVE
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))
        sensor.setKnownState(newState)
        return

    def wait_sensor(self, sensorName, sensorState):
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('{} - Sensor {} not found'.format(self.threadName, sensorName))
            return
        if sensorState == 'active':
            self.waitSensorActive(sensor)
        elif sensorState == 'inactive':
            self.waitSensorInactive(sensor)
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))


    def load_and_run_TrainInfo(self, traininfoFileName):

        # get train frame

        # load traininfo

        # with robot activate new train

        TrainInfo = jmri.jmrit.dispatcher.TrainInfo(traininfoFileName)

        self.load_standard_items(e, TrainInfo)

        #set reverse flag
        TrainInfo.setRunInReverse(False)

        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(TrainInfo, filename)

    ##
    # Loads a train into the Dispatcher from a traininfo file, overriding
    # trainlength to allow train to stop near buffer
    #
    # @param traininfoFileName  the file name of a traininfo file.
    # @param overRideType  "NONE", "USER", "ROSTER" or "OPERATIONS"
    # @param overRideValue  "" , dccAddress, RosterEntryName or Operations
    #            trainname.
    # @param overRideMaxTrainLen
    # @return 0 good, -1 create failure, -2 -3 file errors, -9 bother.
    #

    def loadTrainFromTrainInfo(self, traininfoFileName, overRideType, overRideValue, overRideMaxTrainLen = None):
        #read xml data from selected filename and move it into trainfo
        if self.logLevel > 1: print "got here a"
        #DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        log = org.slf4j.LoggerFactory.getLogger(jmri.jmrit.dispatcher.DispatcherFrame)
        try:
            #maybe called from jthon protect our selves
            if self.logLevel > 1: print "got here a1"
            tif = jmri.jmrit.dispatcher.TrainInfoFile()
            if self.logLevel > 1: print "got here b",tif
            TrainInfo = None
            if self.logLevel > 1: print "got here b11",traininfoFileName
            TrainInfo = jmri.jmrit.dispatcher.TrainInfoFile().readTrainInfo(traininfoFileName)
            if self.logLevel > 1: print "got here b22",TrainInfo
            if self.logLevel > 1: print dir(TrainInfo)
            count = 0
            for method in dir(TrainInfo):
                # the comma at the end of the print, makes it printing
                # in the same line, 4 times (count)
                if self.logLevel > 1: print "| {0: <20}".format(method),
                count += 1
                if count == 4:
                    count = 0
                    if self.logLevel > 1: print
            #object_methods = [method_name for method_name in dir(TrainInfo) if callable(getattr(object, method_name))]
            #print object_methods
            tn = TrainInfo.getTrainName()
            if self.logLevel > 1: print "got here b3"
            try:
                if self.logLevel > 1: print "got here b1",traininfoFileName
                TrainInfo = tif.readTrainInfo(traininfoFileName)
                if self.logLevel > 1: print "got here b2"
                tn = TrainInfo.getTrainName()
                if self.logLevel > 1: print "got here b3"
            except java.io.IOException as ioe :
                if self.logLevel > 1: print "got here b3"
                log.error("IO Exception when reading train info file {}: {}", traininfoFileName, ioe)
                return -2
            except org.jdom2.JDOMException as jde :
                log.error("JDOM Exception when reading train info file {}: {}", traininfoFileName, jde)
                return -3
            except Exception:
                if self.logLevel > 1: print "got here x"
            if self.logLevel > 1: print "got here c"
            return self.loadTrainFromTrainInfo2(TrainInfo, overRideType, overRideValue, overRideMaxTrainLen)
        except java.lang.RuntimeException as ex:
            log.error("Unexpected, uncaught exception loading traininfofile [{}]", traininfoFileName, ex)
            return -9

    #
    # Loads a train into the Dispatcher
    #
    # @param info  a completed TrainInfo class.
    # @param overRideType  "NONE", "USER", "ROSTER" or "OPERATIONS"
    # @param overRideValue  "" , dccAddress, RosterEntryName or Operations
    # @param overRideMaxTrainLen length of train
    #            trainname.
    # @return 0 good, -1 failure

    def loadTrainFromTrainInfo2(self, info, overRideType, overRideValue, overRideMaxTrainLen = None):
        if self.logLevel > 1: print "got here 1"
        #log.debug("loading train:{}, startblockname:{}, destinationBlockName:{}", info.getTrainName(),
        #        info.getStartBlockName(), info.getDestinationBlockName())
        #if self.logLevel > 1: print ("loading train:{}, startblockname:{}, destinationBlockName:{}", info.getTrainName(),
        #        info.getStartBlockName(), info.getDestinationBlockName())
        if self.logLevel > 1: print info
        if self.logLevel > 1: print  info.getTrainName()
        if self.logLevel > 1: print  info.getStartBlockName()
        if self.logLevel > 1: print  info.getDestinationBlockName()
        # # create a new Active Train
        if self.logLevel > 1: print "got here 2"
        #set updefaults from traininfo
        tSource = jmri.jmrit.dispatcher.ActiveTrain.ROSTER
        if info.getTrainFromTrains():
            tSource = jmri.jmrit.dispatcher.ActiveTrain.OPERATIONS
        elif info.getTrainFromUser():
            tSource = jmri.jmrit.dispatcher.ActiveTrain.USER
        if self.logLevel > 1: print "got here 3"
        dccAddressToUse = info.getDccAddress()
        trainNameToUse = info.getTrainName()

        OVERRIDETYPE_NONE = "NONE"
        OVERRIDETYPE_USER = "USER"
        OVERRIDETYPE_DCCADDRESS = "DCCADDRESS"
        OVERRIDETYPE_OPERATIONS = "OPERATIONS"
        OVERRIDETYPE_ROSTER = "ROSTER"

        ##process override
        if overRideType == "" or overRideType == OVERRIDETYPE_NONE:
            pass
        elif overRideType == OVERRIDETYPE_USER or overRideType == OVERRIDETYPE_DCCADDRESS:
            tSource = jmri.jmrit.dispatcher.ActiveTrain.USER
            dccAddressToUse = overRideValue
            trainNameToUse = overRideValue
        elif overRideType == OVERRIDETYPE_OPERATIONS:
            tSource = jmri.jmrit.dispatcher.ActiveTrain.OPERATIONS
            trainNameToUse = overRideValue
        elif overRideType == OVERRIDETYPE_ROSTER:
            tSource = jmri.jmrit.dispatcher.ActiveTrain.ROSTER
            trainNameToUse = overRideValue
        else:
            # just leave as in traininfo
            pass
        if self.logLevel > 1: print "got here 4"
        # create active train
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        at = DF.createActiveTrain(info.getTransitId(), trainNameToUse, tSource,
                info.getStartBlockId(), info.getStartBlockSeq(), info.getDestinationBlockId(),
                info.getDestinationBlockSeq(),
                info.getAutoRun(), dccAddressToUse, info.getPriority(),
                info.getResetWhenDone(), info.getReverseAtEnd(), True, None, info.getAllocationMethod())
        if self.logLevel > 1: print "got here 5"
        if (at != None):
            if self.logLevel > 1: print "got here 6a"
            if (tSource == jmri.jmrit.dispatcher.ActiveTrain.ROSTER):
                if self.logLevel > 1: print "got here 6aaz"
                RosterEntry = jmri.jmrit.roster.Roster.getDefault().getEntryForId(trainNameToUse)
                if self.logLevel > 1: print "got here 6ab"
                if (RosterEntry != None):
                    if self.logLevel > 1: print "got here 6b"
                    at.setRosterEntry(RosterEntry)
                    at.setDccAddress(RosterEntry.getDccAddress())
                else:
                    log.warn("Roster Entry '{}' not found, could not create ActiveTrain '{}'",
                            trainNameToUse, info.getTrainName())
                    return -1
            if self.logLevel > 1: print "got here 6"
            at.setAllocateMethod(info.getAllocationMethod())
            at.setDelayedStart(info.getDelayedStart())              #this is a code: NODELAY, TIMEDDELAY, SENSORDELAY
            at.setDepartureTimeHr(info.getDepartureTimeHr())        # hour of day (fast-clock) to start this train
            at.setDepartureTimeMin(info.getDepartureTimeMin())      #minute of hour to start this train
            at.setDelayedRestart(info.getDelayedRestart())          #this is a code: NODELAY, TIMEDDELAY, SENSORDELAY
            at.setRestartDelay(info.getRestartDelayMin())           #this is number of minutes to delay between runs
            at.setDelaySensor(info.getDelaySensor())
            at.setResetStartSensor(info.getResetStartSensor())
            if ((DF.isFastClockTimeGE(at.getDepartureTimeHr(), at.getDepartureTimeMin()) and
                    info.getDelayedStart() != jmri.jmrit.dispatcher.ActiveTrain.SENSORDELAY) or
                    info.getDelayedStart() == jmri.jmrit.dispatcher.ActiveTrain.NODELAY):
                at.setStarted()
            at.setRestartSensor(info.getRestartSensor())
            at.setResetRestartSensor(info.getResetRestartSensor())
            at.setTrainType(info.getTrainType())
            at.setTerminateWhenDone(info.getTerminateWhenDone())
            if (info.getAutoRun()):
                # aat = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.AutoActiveTrain.AutoActiveTrain)
                # #aat = jmri.jmrit.dispatcher.AutoActiveTrain.AutoActiveTrain(at)
                aat = jmri.jmrit.dispatcher.AutoActiveTrain(at)
                aat.setSpeedFactor(info.getSpeedFactor())
                aat.setMaxSpeed(info.getMaxSpeed())
                aat.setRampRate(aat.getRampRateFromName(info.getRampRate()))
                aat.setResistanceWheels(info.getResistanceWheels())
                aat.setRunInReverse(info.getRunInReverse())
                aat.setSoundDecoder(info.getSoundDecoder())
                if overRideMaxTrainLen == None:
                    aat.setMaxTrainLength(info.getMaxTrainLength())
                else:
                    aat.setMaxTrainLength(overRideMaxTrainLen)
                aat.setStopBySpeedProfile(info.getStopBySpeedProfile())
                aat.setStopBySpeedProfileAdjust(info.getStopBySpeedProfileAdjust())
                aat.setUseSpeedProfile(info.getUseSpeedProfile())
                if (not aat.initialize()):
                    log.error("ERROR initializing autorunning for train {}", at.getTrainName())
                    JOptionPane.showMessageDialog(dispatcherFrame, jmri.jmrit.dispatcher.Bundle.getMessage(
                            "Error27", at.getTrainName()), jmri.jmrit.dispatcher.Bundle.getMessage("MessageTitle"),
                            JOptionPane.INFORMATION_MESSAGE)
                    return -1
                DF.getAutoTrainsFrame().addAutoActiveTrain(aat)
            DF.allocateNewActiveTrain(at)
            DF.newTrainDone(at)

        else:
            log.warn("failed to create Active Train '{}'", info.getTrainName())
            return -1
        return 0

    ## ***********************************************************************************

    ## sound routines

    ## ***********************************************************************************


    # use external "nircmd" command to "speak" some text  (I prefer this voice to eSpeak)
    def speak(self,msg) :
        #if self.logLevel > 1: print("about to speak",msg)
        #java.lang.Runtime.getRuntime().exec('Z:\\ConfigProfiles\\jython\\sound2\\nircmd speak text "' + msg +'"')
        my_dir = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/programs')
#         if self.logLevel > 1: print "nircmd" + my_dir+'/nircmd'
#         java.lang.Runtime.getRuntime().exec(my_dir+'/nircmd speak text "' + msg +'"')
        return

    def announce(self, fromblockname, toblockname, speak_on, direction, instruction):

        from_station = self.get_station_name(fromblockname)
        to_station = self.get_station_name(toblockname)

        if speak_on == True:
            #self.speak("The train in "+ from_station + " is due to depart to " + to_station + " " + direction + " " + instruction )
            self.speak("The train in "+ from_station + " is due to depart to " + to_station )

    def get_station_name(self, block_name):
        BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
        block = BlockManager.getBlock(block_name)
        comment = block.getComment()
        # % is the delimeter for block name
        delimeter = '"'
        if delimeter in comment:
            station_name = self.get_substring_between_delimeters(comment, delimeter)
        else:
            station_name = block_name
        return station_name

    def get_substring_between_delimeters(self, comment, delimeter):
        start = delimeter
        end = delimeter
        s = comment
        substring = s[s.find(start)+len(start):s.rfind(end)]
        return substring


    def bell(self, bell_on = "True"):
        if bell_on == "True":
            snd = jmri.jmrit.Sound("resources/sounds/Bell.wav")
            snd.play(snd)

