# Script to automatically Generate Icons on Panel for automation purposes
#
# Author: Dave Sand, Bill Fitch, copyright 2022
# Part of the JMRI distribution

from javax.swing import JOptionPane

# IS:DSCT:nnn  Control sensors
# IS:DSMT:nnn  Move TO sensors
# IS:DSMP:nnn  Move Progress sensors
# IX:DSLX:1    Logix

# Remove Icons
#   Block content labels
#   Labels
#   Sensors
# Remove Logix
# Remove Transits
# Remove Sections
# Remove SML
# Remove Sensors
#
# Add sensors
# Generate SML and Sections
# Add Logix
# Add Icons
#   Sensors
#   Labels
#   Block content labels


class processPanels(jmri.jmrit.automat.AbstractAutomaton):

    logLevel = 0
    version_no = 0.2    #used to delete DispatcherPanel for new versions if the number of controlsensors/icons has changed

    list_of_stopping_points = []
    blockPoints = {}   # Block center points used by direct access process
    editorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)

    # row number, user name, label name, x offset, y offset
    i = 1
    controlSensors = []
    controlSensors.append([i, 'startDispatcherSensor', 'Run Dispatcher System', 0, 0]); i += 1
    controlSensors.append([i, 'stopMasterSensor', 'Stop Dispatcher System', 0, 0]); i += 1
    controlSensors.append([i, 'modifyMasterSensor', 'Modify Dispatcher System', 0, 0]); i += 1

    controlSensors.append([i, 'Express', 'Express Train (no stopping)', 10, 5]); i += 1
    controlSensors.append([i, 'newTrainSensor', 'Setup Train in Section', 10, 5]); i += 1
    controlSensors.append([i, 'soundSensor', 'Enable Announcements', 10, 5]); i += 1
    controlSensors.append([i, 'simulateSensor', 'Simulate Dispatched Trains', 10, 5]); i += 1
    controlSensors.append([i, 'checkRouteSensor', 'Dispatch Path must be clear', 10, 5]); i += 1
    controlSensors.append([i, 'stopAtStopSensor', 'Stop at Stop Sensors (Default)', 10, 5]); i += 1

    controlSensors.append([i, 'setDispatchSensor', 'Run Dispatch', 0, 5]); i += 1
    controlSensors.append([i, 'setRouteSensor', 'Setup Route', 0, 5]); i += 1
    controlSensors.append([i, 'setStoppingDistanceSensor', 'Set Stopping Length', 0, 5]); i += 1
    controlSensors.append([i, 'setStopSensor', 'Set Stop Sensor', 0, 5]); i += 1
    controlSensors.append([i, 'setStationWaitTimeSensor', 'Set Station Wait Time', 0, 5]); i += 1
    controlSensors.append([i, 'setStationDirectionSensor', 'Set Station Direction', 0, 5]); i += 1
    controlSensors.append([i, 'setTransitBlockRestrictionSensor', 'Restrict Transit Operation', 0, 5]); i += 1

    controlSensors.append([i, 'runRouteSensor', 'Run Route', 10, 5]); i += 1
    controlSensors.append([i, 'editRoutesSensor', 'View/Edit Routes', 10, 5]); i += 1
    controlSensors.append([i, 'viewScheduledSensor', 'View/Edit Scheduled Trains', 10, 5]); i += 1
    controlSensors.append([i, 'showClockSensor', 'Show Analog Clock', 10, 5]); i += 1
    controlSensors.append([i, 'startSchedulerSensor', 'Start Scheduler', 10, 5]); i += 1
    controlSensors.append([i, 'timetableSensor', 'Show Timetable', 10, 5]); i += 1
    controlSensors.append([i, 'departureTimeSensor', 'Setup Departure Times', 10, 5]); i += 1
    controlSensors.append([i, 'helpSensor', 'Help', 0, 5]); i += 1


    def __str__(self):
        self.result = "Success"    #value is returned in __str__ and set to "Failure" in self.tryme()
        self.define_DisplayProgress_global()
        if self.perform_initial_checks():
            self.show_progress(0)
            self.tryme(self.saveForwardStoppingSensors, "Cannot save Forward Stopping Sensors: Contact Developer")
            self.tryme(self.removeIconsAndLabels, "Cannot remove Icons And Labels: Contact Developer")
            self.tryme(self.removeLogix, "Cannot remove startup Logix: Contact Developer")
            self.tryme(self.removeTransits, "Cannot remove Transits: Contact Developer")
            self.tryme(self.removeSML, "Cannot remove SML: Contact Developer")            # do before removeSections in case direction sensors have been added to the SML
            self.tryme(self.removeSections, "Cannot remove Sections: Contact Developer")
            self.show_progress(20)
            self.tryme(self.removeSensors, "Cannot generate startup Logix: Contact Developer")
            self.show_progress(40)
            self.tryme(self.updatePanels, "Cannot update Panels: Contact Developer")
            self.tryme(self.get_list_of_stopping_points, "Cannot get list of stopping points, Contact Developer")
            self.addSensors()
            self.tryme(self.generateSML, "Cannot generate Signal Mast Logic: Signal Masts not set up correctly. Needs to be fixed before using Dispatcher System.")
            self.show_progress(60)
            self.tryme(self.generateSections, "Cannot generate Sections: Signal Masts not set up correctly. Needs to be fixed before using Dispatcher System.")
            self.show_progress(80)
            self.tryme(self.addLogix, "Cannot generate startup Logix: Contact Developer")
            self.addIcons()
            self.tryme(self.retrieveForwardStoppingSensors, "Cannot retrieve Stopping Sensors: Contact Developer")
            self.setVersionNo()
            self.stop_all_threads()
            self.end_show_progress()

        else:
            self.result = "Failure"
        return self.result

    def setVersionNo(self):
        memory = memories.provideMemory('IS:ISMEM:' + "versionNo")
        if memory is not None:
            memory.setValue(self.version_no)

    def version_number_changed(self):
        memory = memories.getMemory('IMIS:ISMEM:' + "versionNo")
        # print "memory", memory, type(memory)
        if memory is None:
            # print "version_no changed", "memory:", "version", self.version_no
            return True
        elif memory.getValue() != self.version_no:
            # print "version_no changed", "memory:", memory.getValue(), "version", self.version_no
            return True
        else:
            print "version_no not changed", "memory:", memory.getValue(), "version", self.version_no
            return False


    def stop_all_threads(self):
        summary = jmri.jmrit.automat.AutomatSummary.instance()
        automatsList = java.util.concurrent.CopyOnWriteArrayList()
        for automat in summary.getAutomats():
            automatsList.add(automat)

        for automat in automatsList:
            automat.stop()

    def tryme(self, func, failure_message):
        try:
            func()
        except:
            title = "Error in Routine"
            Query().displayMessage(failure_message,title)
            self.result = "Failure"
        pass

    def define_DisplayProgress_global(self):
        global dpg
        dpg = DisplayProgress()

    def show_progress(self, progress):
        global dpg
        dpg.Update("creating icons: " + str(progress)+ "% complete")

    def end_show_progress(self):
        global dpg
        dpg.killLabel()

    # **************************************************
    # perform initial checks
    # **************************************************

    def perform_initial_checks(self):

        sensors_OK = False
        block_sensors_OK = False
        stops_OK = False
        lengths_OK = False
        speed_profiles_OK = False

        #JOptionPane.showMessageDialog(None, "Performing some preliminary checks to ensure the trains run correctly\nAll errors will need to be fixed for Dispatcher to run correctly\nSome errors will cause the panel to be set up incorrectly in this stage", 'Checks', JOptionPane.WARNING_MESSAGE)

        # check all blocks have sensors
        if self.check_all_blocks_have_sensors() == False:
            self.msg = self.msg + "\n***********************\n Do you wish to continue\n***********************"
            myAnswer = JOptionPane.showConfirmDialog(None, self.msg)
            if myAnswer == JOptionPane.YES_OPTION:
                #JOptionPane.showMessageDialog(None, 'OK continuing', "As you wish", JOptionPane.WARNING_MESSAGE)
                pass
            elif myAnswer == JOptionPane.NO_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, 'Stopping', "Fix Error" , JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CANCEL_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, 'Stopping', "Have a cup of Tea", JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CLOSED_OPTION:
                if self.logLevel > 0: print "You closed the window. How rude!"
        else:
            sensors_OK = True


        if self.check_no_blocks_have_same_sensor() == False:
            self.msg1 = self.msg1 + "\n***********************\n Do you wish to continue\n***********************"
            myAnswer = JOptionPane.showConfirmDialog(None, self.msg1)
            if self.logLevel > 0: print(1)
            if myAnswer == JOptionPane.YES_OPTION:
                #JOptionPane.showMessageDialog(None, 'OK continuing', "As you wish", JOptionPane.WARNING_MESSAGE)
                pass
            elif myAnswer == JOptionPane.NO_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, 'Stopping', "You need a cup of Tea" , JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CANCEL_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, 'Stopping', "Have a cup of Tea", JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CLOSED_OPTION:
                if self.logLevel > 0: print "You closed the window. How rude!"
        else:
            block_sensors_OK  = True

        if self.check_sufficient_number_of_blocks() == False:
            self.msg2 = "There are insufficient stopping points for Dispatcher to work\n" + self.msg2 + "\n***********************\n Do you wish to continue? You are advised to stop as the current stage cannot perfom correctly\n***********************"
            if self.logLevel > 0: print(3)
            myAnswer = JOptionPane.showConfirmDialog(None, self.msg2)
            if myAnswer == JOptionPane.YES_OPTION:
                #JOptionPane.showMessageDialog(None, 'OK continuing', "As you wish", JOptionPane.WARNING_MESSAGE)
                pass
            elif myAnswer == JOptionPane.NO_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, "Specify the stopping points by inserting 'stop' in the comment fields of the blocks" , "Stopping" , JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CANCEL_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, 'Stopping', "Have a cup of Tea", JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CLOSED_OPTION:
                if self.logLevel > 0: print "You closed the window. How rude!"
        else:
            stops_OK = True

        if self.check_all_blocks_have_lengths() == False:
            self.msg5 = "Not all blocks have lengths\n" + self.msg5 + "\n***********************\n Do you wish to continue? Trains will not run correctly.\n***********************"
            if self.logLevel > 0: print(3)
            myAnswer = JOptionPane.showConfirmDialog(None, self.msg5)
            if myAnswer == JOptionPane.YES_OPTION:
                #JOptionPane.showMessageDialog(None, 'OK continuing', "As you wish", JOptionPane.WARNING_MESSAGE)
                pass
            elif myAnswer == JOptionPane.NO_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, "Specify the stopping points by inserting 'stop' in the comment fields of the blocks" , "Stopping" , JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CANCEL_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, 'Stopping', "Have a cup of Tea", JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CLOSED_OPTION:
                if self.logLevel > 0: print "You closed the window. How rude!"
        else:
            lengths_OK = True

        if self.check_engines_with_speed_profiles_exist() == False:
            self.msg5 = "There are no engines with speed profiles\n" + self.msg5 + "\n***********************\n To continue either set up speed profiles for a train, \nor for a quick examination of Dispatcher System you can install the speed profiles stored in the Dispatcher System Folder\n*********Do you wish to continue?**************"
            if self.logLevel > 0: print(3)
            myAnswer = JOptionPane.showConfirmDialog(None, self.msg5)
            if myAnswer == JOptionPane.YES_OPTION:
                JOptionPane.showMessageDialog(None, 'Please install some of the speed profiles provided', "Look in the speed profile folder in the Dispatcher System Folder", JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.NO_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, "Please run your trains over a suitable track with 3 blocks - see help" , "Install speed profiles" , JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CANCEL_OPTION:
                msg = 'Stopping'
                JOptionPane.showMessageDialog(None, 'Stopping', "Have a cup of Tea", JOptionPane.WARNING_MESSAGE)
                return False
            elif myAnswer == JOptionPane.CLOSED_OPTION:
                if self.logLevel > 0: print "You closed the window. How rude!"

        else:
            speed_profiles_OK = True

        msg =  ""
        some_checks_OK = False
        if sensors_OK:
            msg = msg + "All blocks have lengths\n"
            some_checks_OK = True
        if block_sensors_OK:
            msg = msg + "All blocks have lengths\n"
            some_checks_OK = True
        if stops_OK:
            msg = msg + "All blocks have lengths\n"
            some_checks_OK = True
        if lengths_OK:
            msg = msg + "All blocks have lengths\n"
            some_checks_OK = True
        if speed_profiles_OK:
            msg = msg + "You have engine(s) with speed profile\n"
            some_checks_OK = True

        if some_checks_OK:
            msg = "Performed some prelimiary checks to ensure the trains run correctly\n\nAll Checks OK"
            reply = Query().customQuestionMessage2(msg, "Checks", "Continue", "Look in more detail")
            if reply == JOptionPane.NO_OPTION:
                if sensors_OK:
                    Message = "All blocks have sensors"
                    JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
                if block_sensors_OK:
                    Message = "no two blocks have the same sensor\nPassed check OK"
                    JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
                if stops_OK:
                    Message = "The following blocks have been specified as stopping points\n" + self.msg2 + "\n there are sufficient blocks set up"
                    JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
                if lengths_OK:
                    Message = "All blocks have lengths\n OK to continue \nNote that trains should also be set up with a speed profile to stop correctly"
                    JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
                if speed_profiles_OK:
                    msg = ""
                    for engine in self.get_all_roster_entries_with_speed_profile():
                        msg += "\n" + str(engine)
                    Message = "You have the following trains with speed profiles" + msg
                    JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
        return True

    def check_all_blocks_have_sensors(self):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        list_of_errors = []
        success = True
        for block in blocks.getNamedBeanSet():
            if block.getSensor() == None:
                if LayoutBlockManager.getLayoutBlock(block) != None:    #only include blocks included in a layout panel
                    if block.getUserName() != None:                     #all layout blocks have usernames, should not need this check
                        msg = "block {} does not have a sensor".format(block.getUserName())
                    else:
                        msg = "block {} does not have a sensor".format(block.getSystemName())
                        msg = msg + "\nblock {} does not have a username".format(block.getSystemName())
                    list_of_errors.append(msg)
                    self.msg = ""
                    for message in list_of_errors:
                        self.msg = self.msg +"\n" + message
                    success = False
        return success

    def check_no_blocks_have_same_sensor(self):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        dict = {}
        success = True
        for block in blocks.getNamedBeanSet():
            if LayoutBlockManager.getLayoutBlock(block) != None:    #only include blocks included in a layout panel
                if block.getUserName() != None:                     #all layout blocks have usernames, should not need this check
                    block_name = block.getUserName()
                else:
                    block_name = block.getSystemName()
                sensor = block.getSensor()
                if sensor != None:
                    if sensor.getUserName() != None:
                        sensor_name = sensor.getUserName()
                    else:
                        sensor_name = sensor.getSystemName()
                    dict[block_name] = sensor_name

        list_of_errors = self.get_duplicate_values_in_dict(dict)
        if self.logLevel > 0: print list_of_errors
        if list_of_errors == []:
            success = True
        else:
            success = False
        self.msg1 = ""
        for message in list_of_errors:
            self.msg1 = self.msg1 + "\n" + message

        return success


    def get_duplicate_values_in_dict(self, dict):

        # finding duplicate values
        # from dictionary
        # using a naive approach
        rev_dict = {}

        for key, value in dict.items():
            rev_dict.setdefault(value, set()).add(key)

        result = ["blocks " +', '.join(values) + " have the same sensor " + str( key) for key, values in rev_dict.items()
                                      if len(values) > 1]
        return result

    def check_sufficient_number_of_blocks(self):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        list_of_stops = []
        list_of_blocks = []
        for block in blocks.getNamedBeanSet():
            if LayoutBlockManager.getLayoutBlock(block) != None:    #only include blocks included in a layout panel
                if block.getUserName() != None:                     #all layout blocks have usernames, should not need this check
                    block_name = block.getUserName()
                else:
                    block_name = block.getSystemName()
                comment = str(block.getComment())
                if comment !=None:
                    if "stop" in comment.lower():
                        list_of_stops.append("block " + block_name + " has a stop")
                        if self.logLevel > 0: print list_of_stops
                    else:
                        list_of_blocks.append("block " + block_name + " has no stop")
                        if self.logLevel > 0: print list_of_blocks
                else:
                    list_of_blocks.append("block " + block_name + " has no stop")
        #countthe number of blocks in dictionary
        no_stops = len(list_of_stops)
        if self.logLevel > 0: print "no_stops", no_stops
        no_blocks = len(list_of_blocks)
        if self.logLevel > 0: print "no blocks", no_blocks
        if no_stops < 2:
            success = False
        else:
            success = True
        self.msg2 = " - "
        self.msg2 = self.msg2 + '\n - '.join(list_of_stops)
        if self.logLevel > 0: print self.msg2
        self.msg3 = ""
        self.msg3 = '\n - '.join(list_of_blocks)
        if self.logLevel > 0: print self.msg3
        if no_stops == 0:
            self.msg2 = " - there are no stops"
        return success

    def check_all_blocks_have_lengths(self):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        list_of_errors = []
        success = True
        for block in blocks.getNamedBeanSet():
            if LayoutBlockManager.getLayoutBlock(block) != None:     #only include blocks included in a layout panel
                if block.getLengthMm() < 0.01:
                    if block.getUserName() != None:                 #all layout blocks have usernames, should not need this check
                        msg = "block {} does not have a length".format(block.getUserName())
                    else:
                        msg = "block {} does not have a length".format(block.getSystemName())
                        msg = msg + "\nblock {} does not have a username".format(block.getSystemName())
                    list_of_errors.append(msg)
                    success = False
        self.msg5 = " - "
        self.msg5 = self.msg5 + '\n - '.join(list_of_errors)

        return success

    def check_engines_with_speed_profiles_exist(self):
        roster_entries_with_speed_profile = self.get_all_roster_entries_with_speed_profile()
        if roster_entries_with_speed_profile == []:
            return False
        else:
            return True
            # return True

    def get_all_roster_entries_with_speed_profile(self):
        roster_entries_with_speed_profile = []
        r = jmri.jmrit.roster.Roster.getDefault()
        for roster_entry in jmri.jmrit.roster.Roster.getAllEntries(r):
            if self.logLevel > 0: print "roster_entry.getSpeedProfile()",roster_entry,roster_entry.getSpeedProfile()
            if roster_entry.getSpeedProfile() != None:
                roster_entries_with_speed_profile.append(roster_entry.getId())
                if self.logLevel > 0: print "roster_entry.getId()",roster_entry.getId()
        return roster_entries_with_speed_profile

    def updatePanels(self):
        for panel in self.editorManager.getAll(jmri.jmrit.display.layoutEditor.LayoutEditor):
            if panel.getTitle() != 'Dispatcher System':
                panel.invalidate()
                panel.validate()
                panel.repaint()
            pass

    # **************************************************
    # remove icons and labels from panels
    # **************************************************

    def removeIconsAndLabels(self):

        for panel in self.editorManager.getAll(jmri.jmrit.display.layoutEditor.LayoutEditor):
            if panel.getTitle() == 'Dispatcher System':
                if self.version_number_changed():
                    print "removing panel, version number changed"
                    self.editorManager.remove(panel)
                    panel.dispose()
                    # msg = "should have removed panel"
                    # Query().displayMessage(msg,"")
                # Skip the Dispatcher System control panel if it exists
                continue

            self.removeBlockContentIcons(panel)
            self.removeLabels(panel)
            self.removeSensorIcons(panel)

    def removeBlockContentIcons(self, panel):
        deleteList = []     # Prevent concurrent modification
        icons = panel.getBlockContentsLabelList()
        for icon in icons:
            blk = icon.getBlock()
            if blk is not None:
                deleteList.append(icon)

        for item in deleteList:
            panel.removeFromContents(item)

    def removeLabels(self, panel):
        labelText = []
        for control in self.controlSensors:
            labelText.append(control[2])

        deleteList = []     # Prevent concurrent modification
        for label in panel.getLabelImageList():
            if label.isText():
                if label.getText() in labelText:
                    deleteList.append(label)

        for item in deleteList:
            panel.removeFromContents(item)

    def removeSensorIcons(self, panel):
        blockSensors = []
        for block in blocks.getNamedBeanSet():
            sensor = block.getSensor()
            if sensor is not None:
                blockSensors.append(sensor)

        deleteList = []     # Prevent concurrent modification
        icons = panel.getSensorList()
        for icon in icons:
            sensor = icon.getSensor()
            if sensor is not None:
                name = sensor.getDisplayName()
                if 'MoveTo' in name or 'MoveInProgress' in name:
                    # dispatcher system sensors
                    deleteList.append(icon)
                else:
                    # block sensors
                    if sensor in blockSensors:
                        deleteList.append(icon)

        for item in deleteList:
            panel.removeFromContents(item)

    # **************************************************
    # remove Logix
    # **************************************************
    def removeLogix(self):
        logixManager = jmri.InstanceManager.getDefault(jmri.LogixManager)
        logix = logixManager.getLogix('Run Dispatcher')
        if logix is not None:
            logix.deActivateLogix()
            logixManager.deleteLogix(logix)

    # **************************************************
    # remove Transits
    # **************************************************
    def removeTransits(self):
        deleteList = []     # Prevent concurrent modification
        for transit in transits.getNamedBeanSet():
            deleteList.append(transit)

        for item in deleteList:
            transits.deleteBean(item, 'DoDelete')

    # **************************************************
    # remove Sections
    # **************************************************
    def removeSections(self):

        #remove sections
        deleteList = []     # Prevent concurrent modification
        directionSensorDeleteList = []
        for section in sections.getNamedBeanSet():
            deleteList.append(section)

            forward_sensor = section.getForwardBlockingSensor()
            if forward_sensor is not None:
                directionSensorDeleteList.append(forward_sensor)

            reverse_sensor = section.getReverseBlockingSensor()
            if reverse_sensor is not None:
                directionSensorDeleteList.append(reverse_sensor)

        for item in deleteList:
            sections.deleteBean(item, 'DoDelete')

        for item in directionSensorDeleteList:
            sensors.deleteBean(item, 'DoDelete')

        deleteList = []
        directionSensorDeleteList = []

    # **************************************************
    # remove signal mast logic
    # **************************************************
    def removeSML(self):
        smlManger = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        deleteList = []     # Prevent concurrent modification
        for sml in smlManger.getNamedBeanSet():
            deleteList.append(sml)

        for item in deleteList:
            smlManger.deleteBean(item, 'DoDelete')

    # **************************************************
    # remove sensors
    # **************************************************
    def removeSensors(self):
        controlName = []
        if self.editorManager.get("Dispatcher System") is None:
            # OK to delete control sensors
            for control in self.controlSensors:
                controlName.append(control[1])

        deleteList = []     # Prevent concurrent modification
        for sensor in sensors.getNamedBeanSet():
            userName = sensor.getUserName()
            sysName = sensor.getSystemName()
            if userName is not None:
                if 'MoveTo' in userName or 'MoveInProgress' in userName:
                    deleteList.append(sensor)
                elif userName in controlName:
                    deleteList.append(sensor)

        for item in deleteList:
            #print 'remove sensor {}'.format(item.getDisplayName())
            sensors.deleteBean(item, 'DoDelete')

    # ***********************************************************
    # gets the list of stopping points (stations, sidings etc.)
    # ***********************************************************
    def get_list_of_stopping_points(self):
        for block in blocks.getNamedBeanSet():
            comment = block.getComment()
            if comment != None:
                if "stop" in comment.lower():
                    self.list_of_stopping_points.append(block.getUserName())

    # **************************************************
    # add sensors
    # **************************************************
    def addSensors(self):
        # Create the control sensors
        for control in self.controlSensors:
            sensor = sensors.provideSensor('IS:DSCT:' + str(control[0]))
            if sensor is not None:
                sensor.setUserName(control[1])
        # Create a dummy sensor
        sensor = sensors.provideSensor('IS:DSCT:' + str(0))
        sensor.setUserName("DummyControlSensor")
        sensor = sensors.provideSensor('IS:DSCTA:' + str(0))
        sensor.setUserName("Jdialog_closed")

        # Create the stop sensors
        index = 0
        for stop in self.list_of_stopping_points:
            block = blocks.getBlock(stop)
            if block is not None:
                index += 1
                moveto = sensors.provideSensor('IS:DSMT:' + str(index))
                if moveto is not None:
                    moveto.setUserName('MoveTo' + block.getDisplayName().replace(" ","_") + '_stored')
                inproc = sensors.provideSensor('IS:DSMP:' + str(index))
                if inproc is not None:
                    inproc.setUserName('MoveInProgress' + block.getDisplayName().replace(" ","_"))

    # **************************************************
    # generate SML
    # **************************************************
    def generateSML(self):
        layoutblocks.enableAdvancedRouting(True)
        smlManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        smlManager.automaticallyDiscoverSignallingPairs()

    # **************************************************
    # generate sections
    # **************************************************
    def generateSections(self):
        smlManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        smlManager.generateSection()
        self.show_progress(80)
        sections.generateBlockSections()

    # **************************************************
    # add Logix
    # **************************************************
    def addLogix(self):
        lgxManager = jmri.InstanceManager.getDefault(jmri.LogixManager)
        cdlManager = jmri.InstanceManager.getDefault(jmri.ConditionalManager)
        lgx = lgxManager.createNewLogix('IX:DSLX:1', 'Run Dispatcher')
        cdl = cdlManager.createNewConditional('IX:DSLX:1C1', 'Run Dispatcher')
        lgx.addConditional('IX:DSLX:1C1', 0)
        if cdl is not None:
            cdl.setUserName('Run Dispatcher')
            vars = []
            vars.append(jmri.ConditionalVariable(False, jmri.Conditional.Operator.AND, jmri.Conditional.Type.SENSOR_ACTIVE, 'startDispatcherSensor', True))
            cdl.setStateVariables(vars)
            actions = []
            actions.append(jmri.implementation.DefaultConditionalAction(1, jmri.Conditional.Action.RUN_SCRIPT, '', -1, 'program:jython/DispatcherSystem/RunDispatchMaster.py'))
            cdl.setAction(actions)
            lgx.activateLogix()

    # **************************************************
    # add Icons
    # **************************************************
    def addIcons(self):
        for panel in self.editorManager.getAll(jmri.jmrit.display.layoutEditor.LayoutEditor):
            self.getBlockCenterPoints(panel)

            self.addStopIcons(panel)
            self.addOccupancyIconsAndLabels(panel)
        #add control icons in separate editor panel
        self.addControlIconsAndLabels()

    def getBlockCenterPoints(self, panel):
        self.blockPoints.clear()
        for tsv in panel.getTrackSegmentViews():
            blk = tsv.getBlockName()

            pt1 = panel.getCoords(tsv.getConnect1(), tsv.getType1())
            pt2 = panel.getCoords(tsv.getConnect2(), tsv.getType2())

            mid = jmri.util.MathUtil.midPoint(pt1, pt2)

            self.updateCoords(blk, mid)

        for tov in panel.getLayoutTurnoutAndSlipViews():
            blkA = tov.getBlockName()
            blkB = tov.getBlockBName()
            blkC = tov.getBlockCName()
            blkD = tov.getBlockDName()

            xyA = tov.getCoordsA()
            xyB = tov.getCoordsB()
            xyC = tov.getCoordsC()
            xyD = tov.getCoordsD()

            self.updateCoords(blkA, xyA)
            self.updateCoords(blkB, xyB)
            self.updateCoords(blkC, xyC)
            self.updateCoords(blkD, xyD)

        for lxv in panel.getLevelXingViews():
            blkAC = lxv.getBlockNameAC()
            blkBD = lxv.getBlockNameBD()

            # A level crossing has 4 points but only two blocks.  To prevent both points being in the
            # middle, use the A and D points.
            xyA = lxv.getCoordsA()
            xyD = lxv.getCoordsD()

            self.updateCoords(blkAC, xyA)
            self.updateCoords(blkBD, xyD)

    def updateCoords(self, blk, xy):
        if blk is not None:
            if blk in self.blockPoints:
                self.blockPoints[blk] = jmri.util.MathUtil.midPoint(self.blockPoints[blk], xy)
            else:
                self.blockPoints[blk] = xy

    # **************************************************
    # stop icons
    # **************************************************
    def addStopIcons(self, panel):
        for blockName in self.list_of_stopping_points:
            if blockName in self.blockPoints.keys():
                x = self.blockPoints[blockName].getX()
                y = self.blockPoints[blockName].getY()

                mtSensor = sensors.getSensor('MoveTo' + blockName.replace(" ","_") + '_stored')
                if mtSensor is not None:
                    self.addMarkerIcon(panel, mtSensor, blockName, x, y)

                mpSensor = sensors.getSensor('MoveInProgress' + blockName.replace(" ","_"))
                if mpSensor is not None:
                    self.addSmallIcon(panel, mpSensor.getDisplayName(), x - 10, y)

    # **************************************************
    # occupancy sensor icons and block content labels
    # **************************************************
    def addOccupancyIconsAndLabels(self, panel):
        for blockName in self.blockPoints.keys():
            x = self.blockPoints[blockName].getX() - 10
            y = self.blockPoints[blockName].getY() + 10
            block = blocks.getBlock(blockName)
            if block is not None:
                sensor = block.getSensor()
                if sensor is not None:
                    self.addSmallIcon(panel, sensor.getDisplayName(), x, y)

                    y = int(y) - 30 if int(y) > 35 else 5
                    self.addBlockContentLabel(panel, block, x, y)

    # **************************************************
    # control sensor icons and label
    # **************************************************
    def addControlIconsAndLabels(self):
        if (not self.version_number_changed()) and self.dispatcher_system_panel_exists():
            if self.logLevel > -1: print "not adding control Icons and labels"
            return

        # Create the Dispatcher System control panel
        panel = jmri.jmrit.display.layoutEditor.LayoutEditor("Dispatcher System")
        self.editorManager.add(panel)
        for control in self.controlSensors:
            sensor = sensors.getSensor('IS:DSCT:' + str(control[0]))
            if sensor is not None:
                x = 20 + control[3]
                y = (control[0]  * 20) + 0 + control[4]
                self.addMediumIcon(panel, sensor, x, y)

                x += 20
                self.addTextLabel(panel, control[2], x, y)

        panel.setSize(300,600)
        panel.setAllEditable(False)
        panel.setVisible(True)

    def dispatcher_system_panel_exists(self):
        for frame1 in java.awt.Frame.getFrames():
            # print "frame", frame1.getName()
            if frame1.getName() == "Dispatcher System":
                if frame1.isVisible():
                    return True
        # print "Dispatcher System Panel does not exist"
        return False

    # **************************************************
    # small icon
    # **************************************************
    def addSmallIcon(self, panel, sensorName, x, y):
        icn = jmri.jmrit.display.SensorIcon(panel)
        icn.setIcon("SensorStateActive", jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/tracksegments/circuit-occupied.gif", "active"));
        icn.setIcon("SensorStateInactive", jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "inactive"));
        icn.setIcon("BeanStateInconsistent", jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "incons"));
        icn.setIcon("BeanStateUnknown", jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "unknown"));

        # Assign the sensor and set the location
        icn.setSensor(sensorName)
        icn.setLocation(int(x), int(y))

        # Add the icon to the layout editor panel
        panel.putSensor(icn)

    # **************************************************
    # medium icon
    # **************************************************
    def addMediumIcon(self, panel, sensor, x, y):
        icn = jmri.jmrit.display.SensorIcon(panel)
        icn.setIcon("SensorStateActive", jmri.jmrit.catalog.NamedIcon("resources/icons/mediumschematics/LEDs/AMBERLED.gif", "active"));
        icn.setIcon("SensorStateInactive", jmri.jmrit.catalog.NamedIcon("resources/icons/mediumschematics/LEDs/GRAYLED.gif", "inactive"));
        icn.setIcon("BeanStateInconsistent", jmri.jmrit.catalog.NamedIcon("resources/icons/mediumschematics/LEDs/REDLED.gif", "incons"));
        icn.setIcon("BeanStateUnknown", jmri.jmrit.catalog.NamedIcon("resources/icons/mediumschematics/LEDs/REDLED.gif", "unknown"));

        # Assign the sensor and set the location
        icn.setSensor(sensor.getDisplayName())
        icn.setLocation(int(x), int(y))

        # Add the icon to the layout editor panel
        panel.putSensor(icn)

    # **************************************************
    # marker icon
    # **************************************************
    def addMarkerIcon(self, panel, sensor, blockName, x, y):
        icn = jmri.jmrit.display.SensorIcon(panel)
        icn.setIcon("SensorStateActive", jmri.jmrit.catalog.NamedIcon("resources/icons/markers/loco-green.gif", "active"));
        icn.setIcon("SensorStateInactive", jmri.jmrit.catalog.NamedIcon("resources/icons/markers/loco-red.gif", "inactive"));
        icn.setIcon("BeanStateInconsistent", jmri.jmrit.catalog.NamedIcon("resources/icons/markers/loco-yellow.gif", "incons"));
        icn.setIcon("BeanStateUnknown", jmri.jmrit.catalog.NamedIcon("resources/icons/markers/loco-gray.gif", "unknown"));
        if len(blockName) > 9:
            icn.setText(blockName[:11])
            icn.getPopupUtility().setFontSize(9)
        else:
            icn.setText(blockName[:9])
            icn.getPopupUtility().setFontSize(11)


        icn.setTextActive(Color.RED)
        icn.setTextInActive(Color.YELLOW)
        icn.setTextInconsistent(Color.BLACK)
        icn.setTextUnknown(Color.BLUE)



        # Assign the sensor and set the location
        icn.setSensor(sensor.getDisplayName())
        icn.setLocation(int(x), int(y))

        # Add the icon to the layout editor panel
        panel.putSensor(icn)

    # **************************************************
    # text label
    # **************************************************
    def addTextLabel(self, panel, text, x, y):
        label = jmri.jmrit.display.PositionableLabel(text, panel)
        label.setLocation(int(x), int(y))
        label.setSize(label.getPreferredSize().width, label.getPreferredSize().height);
        label.setDisplayLevel(4)
        panel.putItem(label)

    # **************************************************
    # block content label
    # **************************************************
    def addBlockContentLabel(self, panel, block, x, y):
        label = jmri.jmrit.display.BlockContentsIcon(block.getDisplayName(), panel)
        label.setBlock(block.getDisplayName())
        label.setLocation(int(x), int(y))
        panel.putItem(label)

    def saveForwardStoppingSensors(self):
        forward_stop_sensors = \
            [["section: " , str(section.getUserName()), " stop sensor: " , str(section.getForwardStoppingSensor().getUserName())] \
             for section in sections.getNamedBeanSet() if section.getForwardStoppingSensor() != None]
        self.write_list(forward_stop_sensors)

    def retrieveForwardStoppingSensors(self):
        forward_stop_sensors = self.read_list()
        if forward_stop_sensors != []:
            [sections.getSection(section_name).setForwardStoppingSensorName(forward_stopping_sensor_name) \
             for [sn_prompt, section_name, fss_prompt, forward_stopping_sensor_name] in forward_stop_sensors \
             if forward_stop_sensors is not [] and sections.getSection(section_name) is not None]
    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "forwardStoppingSensors"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator
    def write_list(self, a_list):
        # store list in binary file so 'wb' mode
        file = self.directory() + "forwardStoppingSensors.txt"
        #print "block_info" , a_list
        #print "file"  +file
        with open(file, 'wb') as fp:
            for items in a_list:
                i = 0
                for item in items:
                    fp.write('%s' %item)
                    if i < 3 : fp.write(",")
                    i+=1
                fp.write('\n')

    # Read list to memory
    def read_list(self):
        # for reading also binary mode is important
        file = self.directory() + "forwardStoppingSensors.txt"
        n_list = []
        try:
            with open(file, 'rb') as fp:
                for line in fp:
                    x = line[:-1]
                    #print x
                    y = x.split(",")
                    #print "y" , y
                    n_list.append(y)
            return n_list
        except:
            return []

class DisplayProgress:
    def __init__(self):
        #labels don't seem to work. This is the only thing I could get to work. Improvements welcome
        self.frame1 = JFrame('Starting Processing!', defaultCloseOperation=JFrame.DISPOSE_ON_CLOSE, size=(500, 50), locationRelativeTo=None)
        self.frame1.setVisible(True)

    def Update(self,msg):
        self.frame1.setTitle(msg)

    def killLabel(self):
        self.frame1.setVisible(False)
        self.frame1 = None




class Query:
    def customQuestionMessage2(self, msg, title, opt1, opt2):
        self.CLOSED_OPTION = False
        options = [opt1, opt2]
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        return s

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
