# Script to automatically Generate Icons on Panel for automation purposes
#
# Author: Bill Fitch, copyright 2020
# Part of the JMRI distribution

from xml.etree.ElementTree import ElementTree
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import SubElement
import xml.etree.ElementTree as ET

from javax.swing import JOptionPane

class processXML():

    logLevel = 0

    doc = None
    iter = None
    list_of_stopping_points = []
    output = None

    def __init__(self,filename,finalPanelFilename):
        if self.logLevel > 0: print "filename",filename
        self.doc = ET.parse(filename)
        if self.logLevel > 3:  print "self.doc 1", self.doc
        if self.perform_initial_checks() == True:
            self.remove_old_sensors_and_icons()
            if self.logLevel > 3:  print "self.doc 2", self.doc
            self.remove_old_memories()
            if self.logLevel > 3:  print "self.doc 3", self.doc
            self.remove_old_transits()
            if self.logLevel > 3:  print "self.doc 4", self.doc
            self.remove_old_logix()
            if self.logLevel > 0: print "removed oldlogix"
            self.get_list_of_stopping_points()
            # if self.logLevel > 0: print "got stopping points"
            #self.remove_stopping_point_sensors()
            self.remove_non_stopping_point_sensors()
            self.add_required_sensors_and_icons()
            if self.logLevel > 3:  print "self.doc 5", self.doc
            # if self.logLevel > 0: print "about to add_required_logix1"
            self.add_required_logix()
            if self.logLevel > 3:  print "self.doc 6", self.doc
            # if self.logLevel > 0: print "got add_required_logix1"
            self.add_required_blockcontents()
            if self.logLevel > 3:  print "self.doc 7", self.doc
            # if self.logLevel > 0: print "added sensors and icons"
            #self.associate_blocks_with_memories()
            self.write(finalPanelFilename)
            msg = "Wrote panel to:\n" + finalPanelFilename + "\nPlease \n - restart JMRI and \n - load the file " + finalPanelFilename + "\n - instead of " + filename + "\n - and run Stage 2 of the Dispatcher System to set up the transits"
            JOptionPane.showMessageDialog(None, msg, 'Message', JOptionPane.WARNING_MESSAGE)
        
    # **************************************************
    # Write the output tree stored in self.output to a file
    # **************************************************
    
    def write(self, fileout):
        self.doc.write(fileout)
        # tree = ElementTree(self.doc)
        # tree.write(fileout)
        #self.doc.write(open(fileout, 'w'))
        
    # **************************************************
    # perform initial checks
    # ************************************************** 
        
    def perform_initial_checks(self):
    
        JOptionPane.showMessageDialog(None, "Performing some prelimiary checks to ensure the trains run correctly\nAll errors will need to be fixed for Dispatcher to run correctly\nSome errors will cause the panel to be set up incorrectly in this stage", 'Checks', JOptionPane.WARNING_MESSAGE)
    
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
            Message = "All blocks have sensors"
            JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
            
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
            Message = "no two blocks have the same sensor\nPassed check OK"
            JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
            if self.logLevel > 0: print(2)
            
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
            if self.logLevel > 0: print("4a")
            Message = "The following blocks have been specified as stopping points\n" + self.msg2 + "\n there are sufficient blocks set up"
            JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
            if self.logLevel > 0: print(4)
            
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
            if self.logLevel > 0: print("4a")
            Message = "All blocks have lengths\n OK to continue \nNote that trains should also be set up with a speed profile to stop correctly"
            JOptionPane.showMessageDialog(None, Message, 'Message', JOptionPane.INFORMATION_MESSAGE)
            if self.logLevel > 0: print(4)
            
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
            self.msg1 = self.msg1 +"\n" + message
         
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
    # **************************************************
    # remove existing memories
    # ************************************************** 

    def remove_memories(self):
        
        for memories in self.doc.findall('memories'):
            for memory in memories.findall('memory'):
                memories.remove(memory) 
        #icons
        for LayoutEditor in self.doc.findall('LayoutEditor'):
            for memoryicon in LayoutEditor.findall('memoryicon'):
                LayoutEditor.remove(memoryicon)   

    def remove_blockcontent(self):
        
        #icons
        for LayoutEditor in self.doc.findall('LayoutEditor'):
            for blockcontenticon in LayoutEditor.findall('BlockContentsIcon'):
                LayoutEditor.remove(blockcontenticon)  
                    
    def remove_old_memories(self):
        #moveToxx and MoveToxx_stored sensors
        self.remove_memories()
        self.remove_blockcontent()
        
    def remove_old_logix(self):
        self.remove_logix_with_string_in_name("Run Dispatcher") 

    # **************************************************
    # remove existing transits
    # ************************************************** 

    def remove_transits(self):
        for transits in self.doc.findall('transits'):
            for transit in transits.findall('transit'):
                transits.remove(transit)                         
                    
    def remove_old_transits(self):
        #moveToxx and MoveToxx_stored sensors
        self.remove_transits()          
               
    # **************************************************
    # remove existing icons which will be added later
    # **************************************************

    def remove_icons_with_string_in_name(self, string_to_look_for ):
        for LayoutEditor in self.doc.findall('LayoutEditor'):
            for sensoricon in LayoutEditor.findall('sensoricon'):
                #if self.logLevel > 0: print sensor
                sensor = sensoricon.get('sensor')
                if string_to_look_for in sensor:
                    LayoutEditor.remove(sensoricon)
                    
    def remove_labels_with_string_in_name(self, string_to_look_for ):
        for LayoutEditor in self.doc.findall('LayoutEditor'):
            for lable in LayoutEditor.findall('positionablelabel'):
                #if self.logLevel > 0: print sensor
                text = lable.get('text')
                if string_to_look_for in text:
                    LayoutEditor.remove(lable)                    
                    
    def remove_sensor_icon_with_string_in_name(self, string_to_look_for):
        for LayoutEditor in self.doc.findall('LayoutEditor'):
            for sensoricon in LayoutEditor.findall('sensoricon'):
                sensor_name = sensoricon.get('sensor')
                if string_to_look_for in sensor_name:
                    LayoutEditor.remove(sensoricon)

    def remove_sensor_icon_with_name(self, string_to_look_for):
        for LayoutEditor in self.doc.findall('LayoutEditor'):
            for sensoricon in LayoutEditor.findall('sensoricon'):
                sensor_name = sensoricon.get('sensor')
                if string_to_look_for == sensor_name:
                    LayoutEditor.remove(sensoricon)
            
    def remove_sensor_icon_with_string_in_name_old(self, sensor_name):
        for LayoutEditor in self.doc.findall('LayoutEditor'):
            for sensoricon in LayoutEditor.findall('sensoricon'):
                if sensoricon.find('sensor') != None:
                    sensorName = sensoricon.find('sensor').text
                    if sensorName != None:
                        if string_to_look_for in sensorName:
                            if self.logLevel > 3:  print "deleting sensoricon " , string_to_look_for, "in" , LayouEditor
                            LayoutEditor.remove(sensoricon) 
                        else:
                            if self.logLevel > 3:  print "not deleting sensoricon " , string_to_look_for, "in" , LayouEditor                            
                    
    def remove_old_icons(self):
        #moveTo icons
        self.remove_icons_with_string_in_name("MoveTo")
        #moveInProgress icons
        self.remove_icons_with_string_in_name("MoveInProgress")
        #control icons
        self.remove_control_icons()
        
    def remove_icons_with_sensor_in_name(self):
        for sensors in self.doc.findall('sensors'):
            for sensor in sensors.findall('sensor'):
                if sensor.find('userName') != None:
                    userName = sensor.find('userName').text
                    if userName != None:
                        self.remove_icons_with_string_in_name(userName) 
    
    def remove_control_icons(self):
        self.remove_sensor_icon_with_string_in_name("startDispatcherSensor")            
        self.remove_labels_with_string_in_name("Run Dispatcher System")
       
        self.remove_sensor_icon_with_string_in_name("stopMasterSensor")        
        self.remove_labels_with_string_in_name("Stop Dispatcher System")
           
        self.remove_sensor_icon_with_string_in_name("Express")              
        self.remove_labels_with_string_in_name("Express Train (no stopping)")
        
        self.remove_sensor_icon_with_string_in_name("newTrainSensor")      
        self.remove_labels_with_string_in_name("Setup Train in Section")
        
        self.remove_sensor_icon_with_string_in_name("soundSensor")       
        self.remove_labels_with_string_in_name("Enable Announcements")
        
        self.remove_sensor_icon_with_string_in_name("simulateSensor")
        self.remove_labels_with_string_in_name("Simulate Dispatched Trains")
            
        self.remove_sensor_icon_with_string_in_name("setDispatchSensor")
        self.remove_labels_with_string_in_name("Run Dispatch")
        
        self.remove_sensor_icon_with_string_in_name("setRouteSensor")
        self.remove_labels_with_string_in_name("Setup Route")  
        
        self.remove_sensor_icon_with_string_in_name("runRouteSensor")
        self.remove_labels_with_string_in_name("Run Route")
        
        self.remove_sensor_icon_with_string_in_name("editRoutesSensor")
        self.remove_labels_with_string_in_name("View/Edit Routes")
        
        self.remove_sensor_icon_with_string_in_name("viewScheduledSensor")
        self.remove_labels_with_string_in_name("View/Edit Scheduled Trains")
        
        self.remove_sensor_icon_with_string_in_name("schedulerStartTimeSensor")
        self.remove_labels_with_string_in_name("Set Scheduler Start Time")
        
        self.remove_sensor_icon_with_string_in_name("showClockSensor")
        self.remove_labels_with_string_in_name("Show Analog Clock")
        
        self.remove_sensor_icon_with_string_in_name("startSchedulerSensor")      
        self.remove_labels_with_string_in_name("Start Scheduler")

        self.remove_sensor_icon_with_string_in_name("setStoppingDistanceSensor")
        self.remove_labels_with_string_in_name("Set Stopping Length")
                
    # **************************************************
    # remove existing sensors which willbe added later
    # ************************************************** 
    
    def remove_logix_with_string_in_name(self, string_to_look_for):
        for logixs in self.doc.findall('logixs'):
            for logix in logixs.findall('logix'):
                if logix.find('userName') != None:
                    userName = logix.find('userName').text
                    if userName != None:
                        if string_to_look_for in userName:
                            logixs.remove(logix) 
        for conditionals in self.doc.findall('conditionals'):
            for conditional in conditionals.findall('conditional'):
                if conditional.find('userName') != None:
                    userName = conditional.find('userName').text
                    if userName != None:
                        if string_to_look_for in userName:
                            conditionals.remove(conditional)

    def remove_sensors_with_string_in_name(self, string_to_look_for):
        for sensors in self.doc.findall('sensors'):
            for sensor in sensors.findall('sensor'):
                if sensor.find('userName') != None:
                    userName = sensor.find('userName').text
                    if userName != None:
                        if string_to_look_for in userName:
                            sensors.remove(sensor) 

    def remove_sensors_with_string_in_system_name(self, string_to_look_for):
        for sensors in self.doc.findall('sensors'):
            for sensor in sensors.findall('sensor'):
                if sensor.find('userName') != None:
                    systemName = sensor.find('systemName').text
                    if systemName != None:
                        if string_to_look_for in systemName:
                            sensors.remove(sensor)                            
                    
    def remove_old_sensors(self):
        #moveToxx and MoveToxx_stored sensors
        self.remove_sensors_with_string_in_name("MoveTo" ) 
        #moveTo sensors
        self.remove_sensors_with_string_in_name("MoveInProgress" ) 
        self.remove_sensors_with_string_in_system_name("IS960")
        
    
        
    # **************************************************
    # remove existing sensors and icons which will be added later
    # **************************************************
    
    def remove_old_sensors_and_icons(self):
        self.remove_old_sensors()
        self.remove_old_icons()
        #block sensors
        #self.remove_block_sensors()
        
    def remove_block_sensors(self):
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        for section in SectionManager.getNamedBeanSet():
            section_user_name = section.getUserName()
            self.remove_sensors_with_string_in_system_name(section_user_name)
                
    # **************************************************
    # gets the list of stopping points (stations, sidings etc.)
    # **************************************************    

    def get_list_of_stopping_points(self):
        usingJMRI = True
        if not usingJMRI:
            #get the list of blocks (we need to read these from the block table)
            self.list_of_stopping_points = ["block6","block5"]
        else:
            BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
            if self.logLevel > 0: print "Block"
            for block in BlockManager.getNamedBeanSet():
                comment = block.getComment()
                if comment != None:
                    if "stop" in comment.lower():
                        self.list_of_stopping_points.append(block.getUserName())

    def remove_stopping_point_sensors(self):
        for stop_name in self.list_of_stopping_points:
            self.remove_sensor_icon_with_name(str(stop_name))

    def remove_non_stopping_point_sensors(self):
        for LayoutEditor in self.doc.findall('LayoutEditor'):
        # get all blocks in the current LayoutEditor
            block_names = self.get_block_names(LayoutEditor)
            for block_name in block_names:
                self.remove_sensor_icon_with_name(str(block_name))
                
    # **************************************************
    # adds the sensors and icon xml code for each block to the main panel xml code
    # **************************************************                 
    
    # self.list_of_stopping_points has been set up in get_stopping points()
    def add_required_sensors_and_icons(self):           
                        
        # account for different layout editors
        # iterate over the layout editors in the main document
        # get the block coordinate for the track segments in the appropriate 

        index = 0   # used for setting up the sensor number
        stopping_point_sensors_tally=[]
        if self.logLevel > 3:  print "self.doc aa", self.doc
        for LayoutEditor in self.doc.findall('LayoutEditor'):
            # get all blocks in the current LayoutEditor
            block_names = self.get_block_names(LayoutEditor)
            track_ident_block_name_pairs = self.get_track_ident_block_name_pairs(LayoutEditor)
            #print "block_names =" ,block_names
            # find block_coordinates
            for track_ident_block_name_pair in track_ident_block_name_pairs:
                track_ident, block_name = track_ident_block_name_pair
                block_coordinate = self.get_block_coordinates(block_name, LayoutEditor)
                if block_name in self.list_of_stopping_points:
                    if block_coordinate != None:
                        if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                        if self.logLevel > 3:  print "block_name ", block_name, "stopping_point_sensors_tally", stopping_point_sensors_tally
                        self.set_up_all_stopping_point_icons(block_name, block_coordinate, index, LayoutEditor)
                        if block_name not in stopping_point_sensors_tally:     #ensure sensors are not added multiple times
                            if self.logLevel > 3:  print "tally", block_name, block_coordinate, index, LayoutEditor
                            self.set_up_all_stopping_point_sensors(block_name, index)
                            stopping_point_sensors_tally.append(block_name)
                        index+=1
                else:
                    if block_coordinate != None:
                        if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                        self.set_up_all_non_stopping_point_block_icons(block_name, block_coordinate, index, LayoutEditor)
                        self.set_up_all_non_stopping_point_block_icon_codes(block_name, block_coordinate, index)
                        index+=1

            #set up stopping point sensors for blocks with no track segments, only turnouts
            block_names = self.get_turnout_block_names(LayoutEditor)
            turnout_ident_block_name_pairs = self.get_turnout_ident_block_name_pairs(LayoutEditor)
            # find block_coordinates
            for turnout_ident_block_name_pair in turnout_ident_block_name_pairs:
                turnout_ident, block_name = turnout_ident_block_name_pair
                block_coordinate = self.get_block_coordinates(block_name, LayoutEditor)
                if block_name in self.list_of_stopping_points:
                    if block_coordinate != None:
                        if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                        if self.logLevel > 3:  print "block_name ", block_name, "stopping_point_sensors_tally", stopping_point_sensors_tally
                        self.set_up_all_stopping_point_icons(block_name, block_coordinate, index, LayoutEditor)
                        if block_name not in stopping_point_sensors_tally:     #ensure sensors are not added multiple times
                            if self.logLevel > 3:  print "tally", block_name, block_coordinate, index, LayoutEditor
                            self.set_up_all_stopping_point_sensors(block_name, index)
                            stopping_point_sensors_tally.append(block_name)
                        index+=1
                else:
                    if block_coordinate != None:
                        if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                        self.set_up_all_non_stopping_point_block_icons(block_name, block_coordinate, index, LayoutEditor)
                        self.set_up_all_non_stopping_point_block_icon_codes(block_name, block_coordinate, index)
                        index+=1
                        #control icons
            self.set_up_control_items(LayoutEditor)
        self.set_up_control_sensors()
               
               
    def add_required_logix(self):
        if self.logLevel > 0: print "about to setup_Logix"
        self.set_up_logix_code()
        self.set_up_conditionals_code()
        if self.logLevel > 0: print "about to insert_logix"
        self.insert_logixandconditionals()

    def get_block_names(self, LayoutEditorElementMainDocument):
        block_names = []
        tracksegments = LayoutEditorElementMainDocument.findall('tracksegment')
        for tracksegment in tracksegments:
            block_name = tracksegment.get("blockname")
            if block_name not in block_names:
                block_names.append(block_name) 
        return block_names
        
    def get_track_ident_block_name_pairs(self, LayoutEditorElementMainDocument):
        track_idents_block_names = []
        tracksegments = LayoutEditorElementMainDocument.findall('tracksegment')
        for tracksegment in tracksegments:
            track_ident = tracksegment.get("ident")
            block_name = tracksegment.get("blockname")
            track_ident_block_name = [track_ident,block_name]
            if track_ident_block_name not in track_idents_block_names:
                track_idents_block_names.append(track_ident_block_name)
        return track_idents_block_names

    def get_turnout_block_names(self, LayoutEditorElementMainDocument):
        block_names = []
        layoutturnouts = LayoutEditorElementMainDocument.findall('layoutturnout')
        for layoutturnout in layoutturnouts:
            block_name = layoutturnout.get("blockname")
            if block_name not in block_names:
                block_names.append(block_name)
        return block_names

    def get_turnout_ident_block_name_pairs(self, LayoutEditorElementMainDocument):
        turnout_idents_block_names = []
        layoutturnouts = LayoutEditorElementMainDocument.findall('layoutturnout')
        for layoutturnout in layoutturnouts:
            turnout_ident = layoutturnout.get("ident")
            block_name = layoutturnout.get("blockname")
            turnout_ident_block_name = [turnout_ident, block_name]
            if turnout_ident_block_name not in turnout_idents_block_names:
                turnout_idents_block_names.append(turnout_ident_block_name)
        return turnout_idents_block_names

    # **************************************************
    # adds the sensors and icon xml code for each block to the main panael xml code
    # **************************************************                 
    
    # self.list_of_stopping_points has been set up in get_stopping points()
    def add_required_blockcontents(self):           
        index=0
        #for block in blocks:
        BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
        #if self.logLevel > 0: print "Block"
        for block in BlockManager.getNamedBeanSet():
            #exclude blocks with no sensors
            if block.getSensor() != None:
                for LayoutEditor in self.doc.findall('LayoutEditor'):
                    block_name = block.getUserName()
                    if self.logLevel > 0: print "block_name = ", block_name
                    block_coordinate = self.get_block_coordinates(block_name, LayoutEditor)
                    if block_coordinate != None:
                        if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                        #self.set_up_all_memory_icons(block_name,block_coordinate,index)
                        #self.set_up_all_memories(block_name, index)
                        self.set_up_all_blockcontent_icons(block_name,block_coordinate,index)
                
                index+=1             
                
    # **************************************************
    # Get the coordinates of a track segment in the block, so that the icons can be positioned on i
    # **************************************************                
                       
    def get_block_coordinates(self, block_name, LayoutEditorElementMainDocument):
    
        if self.logLevel > 0: print "getting block coordinates ", block_name
    
        # get the first track segmant we get to for the block
        # it would look more aestheticlly pleasing if we got the middle track segment of the block
        # but life isn't perfect, and anyway people will want to move the icons around
        
        # but try this (assume the tracksegments are in order, and get the mid one)
        
        tracksegment = LayoutEditorElementMainDocument.findall('tracksegment')
          
        # iter=.getiterator('tracksegment')
        ident_list =[]
        i=0
        for element in tracksegment:
            if element.get("blockname") == block_name:
                ident_list.append(element.get("ident"))
                i+=1
        if len(ident_list) >0:  # there may be some unused blocks
            required_track_segment_index = int(len(ident_list)/2)
            if self.logLevel > 0: print "required_track_segment_index = ", required_track_segment_index
            # now with the mid track index it might just work
            tracksegment = LayoutEditorElementMainDocument.findall('tracksegment')
            for element in tracksegment:
                #if self.logLevel > 0: print element.get("blockname"), element.get("ident"), required_track_segment_index, ident_list[required_track_segment_index]
                if element.get("blockname") == block_name and element.get("ident") == ident_list[required_track_segment_index]:
                    #if self.logLevel > 0: print "got here"
                    ident = element.attrib["ident"]
                    if self.logLevel > 0: print "ident = ",  ident
                    connect1 = element.get("connect1name")
                    type1 = element.get("type1")
                    connect2 = element.get("connect2name")
                    type2 = element.get("type2")
                    if self.logLevel > 0: print "connect1 = ",connect1
                    if self.logLevel > 0: print "connect2 = ",connect2
                    x1,y1 = self.get_positionable_point_coordinate(connect1,type1,LayoutEditorElementMainDocument)
                    x2,y2 = self.get_positionable_point_coordinate(connect2,type2,LayoutEditorElementMainDocument)
                    x_button = int((float(x1)+float(x2))/2.0)
                    y_button = int((float(y1)+float(y2))/2.0)-10      #-10 to raise the button up a bit on the track
                    button_coordinates = [x_button,y_button]
                    if self.logLevel > 0: print "button coordinate = " , button_coordinates
                    return button_coordinates
        else:
            #check for turnouts
            #iter=self.doc.getiterator('layoutturnout')
            layoutturnout = LayoutEditorElementMainDocument.findall('layoutturnout')
            ident_list =[]
            i=0
            for element in layoutturnout:
                if element.get("blockname") == block_name:
                    ident_list.append(element.get("ident"))
                    i+=1
                    
            if len(ident_list) >0:  # there may be some unused blocks
                required_turnout_index = int(len(ident_list)/2)
                if self.logLevel > 0: print "required_turnout_index = ", required_turnout_index
                # now with the mid track index it might just work
                
                #iter=self.doc.getiterator('layoutturnout')
                layoutturnout = LayoutEditorElementMainDocument.findall('layoutturnout')
                for element in layoutturnout:
                    #if self.logLevel > 0: print element.get("blockname"), element.get("ident"), required_turnout_index, ident_list[required_turnout_index]
                    if element.get("blockname") == block_name and element.get("ident") == ident_list[required_turnout_index]:
                        #if self.logLevel > 0: print "got here"
                        ident = element.attrib["ident"]
                        if self.logLevel > 0: print "ident = ",  ident
                        xcen = element.get("xcen")
                        ycen = element.get("ycen")
                        # type = element.get("type")
                        # connectaname = element.get("CONNECTANAME")
                        # connectbname = element.get("CONNECTBNAME")
                        # connectcname = element.get("CONNECTBNAME")
                        #x1,y1 = self.get_positionable_point_coordinate(connectAname,type1)
                        #x2,y2 = self.get_positionable_point_coordinate(connectBName,type2)
                        x_button = int(float(xcen))
                        y_button = int(float(ycen))-10      #-10 to raise the button up a bit on the track
                        button_coordinates = [x_button,y_button]
                        if self.logLevel > 0: print "button coordinate = " , button_coordinates
                        return button_coordinates
        
        return None
                
    def get_positionable_point_coordinate(self, connection_name,type,insertion_point):
        if self.logLevel > 3:  print type[:9]
        if type == "TURNOUT_A":
            connection = "layoutturnout"
            x_ident="xa"
            y_ident="ya"            
        elif type == "TURNOUT_B":
            connection = "layoutturnout"
            x_ident="xb"
            y_ident="yb"            
        elif type == "TURNOUT_C":
            connection = "layoutturnout"
            x_ident="xc"
            y_ident="yc"
        elif type == "POS_POINT":
            connection = "positionablepoint"
            x_ident="x"
            y_ident="y"
        elif type[:9] == "TURNTABLE":
            connection = "layoutturntable"
            x_ident="xcen"
            y_ident="ycen"
            if self.logLevel > 3:  print "in turntable", "connection", connection ,"type", type
        elif type[0] == "A":   #anchor
            connection = "positionablepoint"
            x_ident="x"
            y_ident="y"
        else: 
            raise NameError('Unsupported track type, need to modify code for X-overs:' + type)
        if self.logLevel > 0: print "connection " , connection, "type", type
        iter=insertion_point.getiterator(connection)
        for element in iter:
            if element.get("ident") == connection_name:
                # if self.logLevel > 0: print "here"
                x = element.get(x_ident)
                y = element.get(y_ident)
                coordinates = [x,y]
                # if self.logLevel > 0: print coordinates
                return coordinates
        if self.logLevel > 3:  print "returning none", "connection", connection ,"type", type
        return None
        
    # **************************************************
    # Create XML code for Icons, and store temporarily
    # **************************************************
    
    def set_up_blockcontent_icon_code(self, icon_blockcontent_name, icon_tooltip, icon_coordinates, element_tree):
        if self.logLevel > 0: print "set_up_blockcontent_icon_code"
        if self.logLevel > 0: print element_tree
        if self.logLevel > 0: print icon_coordinates
        if self.logLevel > 0: print icon_blockcontent_name
        
        
        x,y = icon_coordinates
        if self.logLevel > 0: print x,y
        iter = element_tree.getiterator('BlockContentsIcon')     
        for element in iter:
            element.set("blockcontents", icon_blockcontent_name)
            element.set("x",str(x))
            element.set("y",str(y))
            for child in element.getchildren():
                if child.tag == "tooltip": child.text = icon_tooltip   
        # except Exception as e:
            # if self.logLevel > 0: print e
            
        if self.logLevel > 0: print "end set_up_blockcontent_icon_code"
            
          
            
    def set_up_all_blockcontent_icon_codes(self, block_name, icon_coordinates, index):
    
        if self.logLevel > 0: print "icon_coordinates" , icon_coordinates == None
        icon_blockcontent_name = block_name
        blockcontent_location = blocks.getBlock(block_name).getSystemName() 
        icon_tooltip = icon_blockcontent_name + "("+blockcontent_location+")"
        blockcontent_icon_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/blockcontent_icon.xml')
        self.doc_blockcontent = ElementTree(file=blockcontent_icon_xml)
        icon_offset = [-5,-30]
        icon_coordinates_withoffset = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        self.set_up_blockcontent_icon_code(icon_blockcontent_name, icon_tooltip, icon_coordinates_withoffset, self.doc_blockcontent)

    def set_up_memory_icon_code(self, icon_memory_name, icon_tooltip, icon_coordinates, element_tree):
        if self.logLevel > 0: print "set_up_memory_icon_code"
        if self.logLevel > 0: print element_tree
        if self.logLevel > 0: print icon_coordinates
        if self.logLevel > 0: print icon_memory_name
        
        
        x,y = icon_coordinates
        if self.logLevel > 0: print x,y
        iter = element_tree.getiterator('memoryicon')     
        for element in iter:
            element.set("memory", icon_memory_name)
            element.set("x",str(x))
            element.set("y",str(y))
            for child in element.getchildren():
                if child.tag == "tooltip": child.text = icon_tooltip   
        # except Exception as e:
            # if self.logLevel > 0: print e
            
        if self.logLevel > 0: print "end set_up_memory_icon_code"
            
          
            
    def set_up_all_memory_icon_codes(self, block_name, icon_coordinates, index):
    
        if self.logLevel > 0: print "icon_coordinates" , icon_coordinates == None
        icon_memory_name = "M_"+block_name.replace(" ","_") 
        memory_location = "IM99"+str(index).zfill(4)  
        icon_tooltip = icon_memory_name + "("+memory_location+")"
        memory_icon_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/memory_icon.xml')
        self.doc_Memory = ElementTree(file=memory_icon_xml)
        icon_offset = [-5,-30]
        icon_coordinates_withoffset = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        self.set_up_memory_icon_code(icon_memory_name, icon_tooltip, icon_coordinates_withoffset, self.doc_Memory)
                
    # **************************************************
    # Insert the above XML code for the icons in the main document
    # **************************************************
    
    def insert_blockcontent_icon_code(self, block_name, main_document_element_tree, icon_element_tree):

        # find the insertion point in main document
        if self.logLevel > 0: print "inserting blockcontent code"
        iter=main_document_element_tree.findall('LayoutEditor')
        i = 0
        for insertion_point in iter:  
            # now we have found the insertion points, insert the blockcontent_icon data
            # if the appropriate LayoutEditor includes a track element containing that block
            if self.track_elements_for_block_name(insertion_point, block_name):
                iter_icon = icon_element_tree.getiterator('LayoutEditor')
                for result in iter_icon:
                    insertion_point.extend(result) 
        return main_document_element_tree

    def insert_sensor_code_for_block(self, block_name, data, data1):
        # if self.logLevel > 0: print "insert_sensor_code","data",data
        iter=data.findall('LayoutEditor')
        for insertion_point in iter:
            if self.track_elements_for_block_name(insertion_point, block_name):
                iter_sensor=data1.getiterator('LayoutEditor')
                for result in iter_sensor:
                    insertion_point.extend(result) 
        return data        

    def track_elements_for_block_name(self, insertion_point, block_name): 
        for tracksegment in insertion_point.findall('tracksegment'):
            #print tracksegment
            #print str(tracksegment.get('blockname')) + " : " + block_name
            if block_name == tracksegment.get('blockname'):
                return True
        for layoutturnout in insertion_point.findall('layoutturnout'):
            if block_name == layoutturnout.get('blockname'):
                return True
        return False
        
    def insert_all_blockcontent_icon_codes(self, block_name):
        #blockcontent icon
        self.doc = self.insert_blockcontent_icon_code(block_name, self.doc, self.doc_blockcontent)
            
    # **************************************************
    # Create and insert icons in the main document
    # **************************************************    
        
    def set_up_all_blockcontent_icons(self, block_name, button_coordinate, index):
        if self.logLevel > 0: print "********mem icon set up*****************"
        self.set_up_all_blockcontent_icon_codes(block_name, button_coordinate, index)       #stores the code temporarily
        if self.logLevel > 0: print "********mem icon code set up finished*****************"
        self.insert_all_blockcontent_icon_codes(block_name)
        if self.logLevel > 0: print "icon set up", block_name
        if self.logLevel > 0: print "*************************"
           
    # **************************************************
    # Create XML code for Icons, and store temporarily
    # **************************************************
    
    def set_up_label_code(self, icon_text, icon_coordinates, element_tree):
        
        try:
            x,y = icon_coordinates
        except Exception as e:
            if self.logLevel > 0: print e
        
        iter = element_tree.getiterator('positionablelabel')     
        for element in iter:
            element.set("text", icon_text)
            element.set("x",str(x))
            element.set("y",str(y))
    
    def set_up_large_icon_code(self, icon_sensor_name, icon_coordinates, element_tree):
        
        try:
            x,y = icon_coordinates
            iter = element_tree.getiterator('sensoricon')     
            for element in iter:
                element.set("sensor", icon_sensor_name)
                element.set("x",str(x))
                element.set("y",str(y))
        except Exception as e:
            if self.logLevel > 0: print e
        
        
    
    def set_up_small_icon_code(self, icon_sensor_name, icon_coordinates, element_tree):
        
        try:
            x,y = icon_coordinates
            iter = element_tree.getiterator('sensoricon')     
            for element in iter:
                element.set("sensor", icon_sensor_name)
                element.set("x",str(x))
                element.set("y",str(y))            
        except Exception as e:
            if self.logLevel > 0: print e
                
    def set_up_wide_icon_code(self, icon_text, icon_sensor_name, icon_tooltip, icon_coordinates, element_tree):
        try:
            x,y = icon_coordinates
            iter = element_tree.getiterator('sensoricon')     
            for element in iter:
                element.set("sensor", icon_sensor_name)
                element.set("text", icon_text)
                element.set("x",str(x))
                element.set("y",str(y))
                for child in element.getchildren():
                    #if self.logLevel > 0: print child, child.tag
                    
                    if child.tag == "tooltip": child.text = icon_tooltip
                    if child.tag == "activeText": child.set("text", icon_sensor_name)
                    if child.tag == "inactiveText": child.set("text", icon_sensor_name)
                    if child.tag == "unknownText": child.set("text", icon_sensor_name)
                    if child.tag == "inconsistentText": child.set("text", icon_sensor_name)    
        except Exception as e:
            if self.logLevel > 0: print e
            
    def set_up_all_stopping_point_block_icon_codes(self, block_name, icon_coordinates, index):
    
        # MoveTo icon
        icon_text = block_name[:9]
        icon_sensor_name = "MoveTo"+block_name.replace(" ","_") +"_stored"
        sensor_location = "IS99"+str(index).zfill(4)
        icon_tooltip = icon_sensor_name + sensor_location
        wide_icon_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/wide_icon.xml')
        self.doc_MoveTo = ElementTree(file=wide_icon_xml)
        icon_offset = [-25,-10]
        icon_coordinates_withoffset = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        self.set_up_wide_icon_code(icon_text, icon_sensor_name, icon_tooltip, icon_coordinates_withoffset, self.doc_MoveTo)
        
        # MoveInProgress icon
        icon_sensor_name = "MoveInProgress"+block_name.replace(" ","_")
        sensor_location = "IS97"+str(index).zfill(4)  
        #icon_tooltip = icon_sensor_name + sensor_location
        small_icon_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/small_icon.xml')
        self.doc_MoveInProgress = ElementTree(file=small_icon_xml)
        icon_offset = [-20,-20]
        icon_coordinates_withoffset = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        self.set_up_small_icon_code(icon_sensor_name, icon_coordinates_withoffset, self.doc_MoveInProgress)
        
        # Block Sensor icon
        layoutBlock = layoutblocks.getLayoutBlock(block_name)
        block = layoutBlock.getBlock()
        sensor = block.getSensor()
        if sensor != None:
            icon_sensor_name = sensor.getUserName()
            icon_sensor_user_name = sensor.getSystemName() 
            icon_tooltip = icon_sensor_name + icon_sensor_user_name
            icon_offset = [-20,10]
            icon_coordinates_withoffset = [a + b for a, b in zip(icon_coordinates, icon_offset)]
            self.doc_BlockSensor = ElementTree(file=small_icon_xml)    
            self.set_up_small_icon_code(icon_sensor_name, icon_coordinates_withoffset, self.doc_BlockSensor)
            
    def set_up_all_non_stopping_point_block_icon_codes(self, block_name, icon_coordinates, index):
        # Block Sensor icon
        layoutBlock = layoutblocks.getLayoutBlock(block_name)
        block = layoutBlock.getBlock()
        sensor = block.getSensor()
        if sensor != None:
            icon_sensor_name = sensor.getUserName()
            icon_sensor_user_name = sensor.getSystemName() 
            icon_tooltip = icon_sensor_name + icon_sensor_user_name
            icon_offset = [-20,10]
            icon_coordinates_withoffset = [a + b for a, b in zip(icon_coordinates, icon_offset)]
            small_icon_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/small_icon.xml')
            self.doc_BlockSensor = ElementTree(file=small_icon_xml)
            self.set_up_small_icon_code(icon_sensor_name, icon_coordinates_withoffset, self.doc_BlockSensor)
            
    def delete_block_icons(self, block_name, lay):
        layoutBlock = layoutblocks.getLayoutBlock(block_name)
        block = layoutBlock.getBlock()
        sensor = block.getSensor()
        if sensor != None:
            icon_sensor_name = sensor.getUserName()
            self.remove_sensor_icon_with_string_in_name(icon_sensor_name)
            
    def set_control_icon_coordinates(self, LayoutEditor):
    
        max_x = 0
        min_x = 0
        max_y = 0
        min_y = 0
        if self.logLevel > 3:  print "**************************************************"
        iter=LayoutEditor.findall('tracksegment')
        for element in iter:
            ident = element.attrib["ident"]
            if self.logLevel > 0: print "ident = ",  ident
            connect1 = element.get("connect1name")
            type1 = element.get("type1")
            connect2 = element.get("connect2name")
            type2 = element.get("type2")
            if self.logLevel > 0: print "connect1 = ",connect1
            if self.logLevel > 0: print "connect2 = ",connect2
            x1,y1 = self.get_positionable_point_coordinate(connect1,type1,LayoutEditor)
            x2,y2 = self.get_positionable_point_coordinate(connect2,type2,LayoutEditor)
            x1 = int(float(x1))
            x2 = int(float(x2))
            y1 = int(float(y1))
            y2 = int(float(y2))
            
    
            max_x = max(max_x, x1, x2)
            min_x = min(min_x, x1 ,x2)
            max_y = max(max_y, y1, y2)
            min_y = min(min_y, y1 ,y2)
            
        if self.logLevel > 0: print ("min_x ", min_x,"max_x ", max_x,"min_y ", min_y,"max_y ", max_y)
            
        x_coord =  max_x + 30       # put a bit to the right of the tracks near the top
        y_coord =  30
        icon_coordinates = [x_coord, y_coord]
        if self.logLevel > 3:  print "**************************************************"
        return icon_coordinates
        
    def set_up_control_icon_codes(self,LayoutEditor):
        
        icon_coordinates = self.set_control_icon_coordinates(LayoutEditor)   #iterate througout the track segments, get their coordinates, and position the control icons to the right of them
        #icon_coordinates = [600,30]   #my best guess
        label_offset = [25,0]
        icon_offset = [0,20]
        indent = [5,5]
        unindent = [-5,5]
        indentALot = [150,0]
        unindentALot = [-150,0]
        
        large_icon_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/large_icon.xml')
        label_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/label.xml')        
        
        icon_sensor_name = "startDispatcherSensor"
        self.doc_start_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_start_icon)       
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]              
        icon_label = "Run Dispatcher System"
        self.doc_start_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_start_label)
        
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]        
        icon_sensor_name = "stopMasterSensor"
        self.doc_stop_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_stop_icon)        
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]              
        icon_label = "Stop Dispatcher System"
        self.doc_stop_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_stop_label)
        
        # the following icons need to have the dispatcher System working, so indent
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, indent)]  

        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]       
        icon_sensor_name = "Express"
        self.doc_Express_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_Express_icon)        
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]               
        icon_label = "Express Train (no stopping)"
        self.doc_Express_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_Express_label)
        
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]       
        icon_sensor_name = "newTrainSensor"  
        self.doc_new_train_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_new_train_icon)       
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]         
        icon_label = "Setup Train in Section"
        self.doc_new_train_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_new_train_label)
        
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]        
        icon_sensor_name = "soundSensor"
        self.doc_sound_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_sound_icon)        
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]              
        icon_label = "Enable Announcements"
        self.doc_sound_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_sound_label)
         
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]       
        icon_sensor_name = "simulateSensor"
        self.doc_simulate_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_simulate_icon)   
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]
        icon_label = "Simulate Dispatched Trains"
        self.doc_simulate_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_simulate_label)
        
        # unindent
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, unindent)]
        
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]       
        icon_sensor_name = "setDispatchSensor"
        self.doc_set_dispatch_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_set_dispatch_icon)                 
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)] 
        icon_label = "Run Dispatch"
        self.doc_set_dispatch_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_set_dispatch_label)
        
        # indent for two buttons on a line
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, indentALot)]
        icon_sensor_name = "setRouteSensor"
        self.doc_set_route_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_set_route_icon)
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]
        icon_label = "Setup Route"
        self.doc_set_route_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_set_route_label)
        
        # unindent for one buttons on a line
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, unindentALot)]
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        icon_sensor_name = "setStoppingDistanceSensor"
        self.doc_set_stopping_length_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_set_stopping_length_icon)
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]
        icon_label = "Set Stopping Length"
        self.doc_set_stopping_length_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_set_stopping_length_label)

        # indent
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, indent)] 
        # next icon
                
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]       
        icon_sensor_name = "runRouteSensor"
        self.doc_run_route_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_run_route_icon)                 
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)] 
        icon_label = "Run Route"
        self.doc_run_route_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_run_route_label)
        
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        icon_sensor_name = "editRoutesSensor"
        self.doc_edit_routes_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_edit_routes_icon)
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]
        icon_label = "View/Edit Routes"
        self.doc_edit_routes_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_edit_routes_label)
        
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        icon_sensor_name = "viewScheduledSensor"
        self.doc_view_scheduled_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_view_scheduled_icon)
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)] 
        icon_label = "View/Edit Scheduled Trains"
        self.doc_view_scheduled_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_view_scheduled_label)
                       
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        icon_sensor_name = "schedulerStartTimeSensor"
        self.doc_set_scheduler_time_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_set_scheduler_time_icon)
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]
        icon_label = "Set Scheduler Start Time"
        self.doc_set_scheduler_time_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_set_scheduler_time_label)        
        
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)]
        icon_sensor_name = "showClockSensor"
        self.doc_show_clock_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_show_clock_icon)   
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]
        icon_label = "Show Analog Clock"
        self.doc_show_clock_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_show_clock_label)        
        
        icon_coordinates = [a + b for a, b in zip(icon_coordinates, icon_offset)] 
        icon_sensor_name = "startSchedulerSensor"
        self.doc_start_scheduler_icon = ElementTree(file=large_icon_xml)
        self.set_up_large_icon_code(icon_sensor_name, icon_coordinates, self.doc_start_scheduler_icon) 
        icon_coordinates_label = [a + b for a, b in zip(icon_coordinates, label_offset)]        
        icon_label = "Start Scheduler"
        self.doc_start_scheduler_label = ElementTree(file=label_xml)
        self.set_up_label_code(icon_label, icon_coordinates_label, self.doc_start_scheduler_label)
                
        
    def set_up_logix_code(self):
        logix_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/logix.xml')
        self.doc_logix = ElementTree(file=logix_xml)
        
    def set_up_conditionals_code(self):
        conditionals_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/conditionals.xml')
        self.doc_conditionals = ElementTree(file=conditionals_xml) 
        
        
    # **************************************************
    # Insert the above XML code for the icons in the main document
    # **************************************************
    
    def insertChildFromRoot2InRoot1(self, root1, root2,parent,child):
        childstr = ".//"+child      #Xpath notation, find all occurrences of child
        parentstr = ".//"+parent    
        findstr = childstr
        insertionNode = None
        for root in (root1, root2):
            if insertionNode is None:
                ans = root
                insertionNode = root.find(findstr)
                if insertionNode == None:
                    findstr = parentstr
                    insertionNode = root.find(findstr)
                    if insertionNode == None:
                        insertionNode = root               
            else:
                elements = root.find(findstr) 
                if elements == None:
                    elements = root
                for element in elements._children:
                    insertionNode.append(element)
        return ans
        
    def insert_icon_code(self, insertion_point, icon_element_tree):
        insertion_point.append(icon_element_tree) 
        #return data
        

    def insert_icon_code_old(self, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
        child = "LayoutEditor"
        root1 = main_document_element_tree.getroot()
        root2 = icon_element_tree.getroot()
        self.insertChildFromRoot2InRoot1(root1, root2,parent,child)

        return main_document_element_tree

        
    def insert_icon_code_for_block(self, block_name, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
        child = "LayoutEditor"
        
        root1 = main_document_element_tree.getroot()
        iter=root1.getiterator(parent)
        for insertion_point in iter:
            
            if self.track_elements_for_block_name(insertion_point, block_name):
                root2 = icon_element_tree.getroot()
                self.insertChildFromRoot2InRoot1(insertion_point, root2,parent,child)

        return main_document_element_tree

    def insert_code_for_block(self, block_name, data, data1):
        if self.logLevel > 0: print "insert_sensor_code","data",data
        iter=data.findall('LayoutEditor')
        for insertion_point in iter:
            if self.track_elements_for_block_name(insertion_point, block_name):
                iter_sensor=data1.getiterator('LayoutEditor')
                for result in iter_sensor:
                    insertion_point.extend(result) 
        return data          
        

    def insert_label_code(self, insertion_point, icon_element_tree):
        insertion_point.append(icon_element_tree)

    def insert_label_code_old(self, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
        child = "LayoutEditor"
        root1 = main_document_element_tree.getroot()
        root2 = icon_element_tree.getroot()
        self.insertChildFromRoot2InRoot1(root1, root2,parent,child)

        #return main_document_element_tree 

    def insert_logix_code_new(self, insertion_point, icon_element_tree):
        insertion_point.append(icon_element_tree)

    def insert_logix_code(self, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
        child = "logixs"
        root1 = main_document_element_tree.getroot()
        root2 = icon_element_tree.getroot()
        self.insertChildFromRoot2InRoot1(root1, root2,parent,child)
        
        return main_document_element_tree     

    def insert_conditionals_code_new(self, insertion_point, icon_element_tree):
        insertion_point.append(icon_element_tree)        

    def insert_conditionals_code(self, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
       
        child = "conditionals"
        root1 = main_document_element_tree.getroot()
        root2 = icon_element_tree.getroot()
        self.insertChildFromRoot2InRoot1(root1, root2,parent,child)
        
        return main_document_element_tree 
        
    def insert_all_stopping_point_block_icon_codes(self, LayoutEditor):
        #wide_MoveTo icon
        self.insert_code(self.doc_MoveTo, LayoutEditor)
        #standard_MoveInProgress icon
        self.insert_code(self.doc_MoveInProgress, LayoutEditor)
        #Block Sensor icon
        self.insert_code(self.doc_BlockSensor, LayoutEditor)
        #return LayoutEditor
        
    def insert_all_non_stopping_point_block_icon_codes(self, LayoutEditor):       
        self.insert_code(self.doc_BlockSensor, LayoutEditor)
        return LayoutEditor
        
    def insert_code(self, inserted_doc, LayoutEditorInsertionPoint):
        scraps_to_be_inserted = inserted_doc.find("LayoutEditor")  #scraps to be inserted ae below this point
        LayoutEditorInsertionPoint.extend(scraps_to_be_inserted)
        return LayoutEditorInsertionPoint
        
    def insert_control_icons(self, LayoutEditor):
        # print "LayoutEditor a" , LayoutEditor
        # insertion_point = LayoutEditor.find('sensoricon')
        # if insertion_point == None:
        insertion_point = LayoutEditor
        self.insert_code(self.doc_Express_icon, insertion_point)
        self.insert_code(self.doc_Express_icon, insertion_point)
        self.insert_code(self.doc_new_train_icon, insertion_point)
        self.insert_code(self.doc_start_icon, insertion_point)
        self.insert_code(self.doc_stop_icon, insertion_point)
        self.insert_code(self.doc_sound_icon, insertion_point)
        self.insert_code(self.doc_simulate_icon, insertion_point)
        self.insert_code(self.doc_view_scheduled_icon, insertion_point)
        self.insert_code(self.doc_set_scheduler_time_icon, insertion_point)
        self.insert_code(self.doc_start_scheduler_icon, insertion_point)
        self.insert_code(self.doc_set_route_icon, insertion_point)
        self.insert_code(self.doc_set_dispatch_icon, insertion_point)
        self.insert_code(self.doc_set_stopping_length_icon, insertion_point)
        self.insert_code(self.doc_run_route_icon, insertion_point)
        self.insert_code(self.doc_show_clock_icon, insertion_point)
        self.insert_code(self.doc_edit_routes_icon, insertion_point)
        # print "LayoutEditor b" , LayoutEditor
        # insertion_point = LayoutEditor.find('positionablelabel')
        # if insertion_point == None:
        insertion_point = LayoutEditor
        self.insert_code(self.doc_Express_label, insertion_point)
        self.insert_code(self.doc_new_train_label, insertion_point)
        self.insert_code(self.doc_start_label, insertion_point)
        self.insert_code(self.doc_stop_label, insertion_point)
        self.insert_code(self.doc_sound_label, insertion_point)
        self.insert_code(self.doc_simulate_label, insertion_point)
        self.insert_code(self.doc_view_scheduled_label, insertion_point)
        self.insert_code(self.doc_set_scheduler_time_label, insertion_point)
        self.insert_code(self.doc_start_scheduler_label, insertion_point)
        self.insert_code(self.doc_set_route_label, insertion_point)
        self.insert_code(self.doc_set_dispatch_label, insertion_point)
        self.insert_code(self.doc_set_stopping_length_label, insertion_point)
        self.insert_code(self.doc_run_route_label, insertion_point) 
        self.insert_code(self.doc_show_clock_label, insertion_point) 
        self.insert_code(self.doc_edit_routes_label, insertion_point) 
        
    def insert_logixandconditionals_new(self):
        if self.logLevel > 0: print "insert_logix"
        insertion_point = self.doc.find('logixs')
        self.insert_logix_code(insertion_point, self.doc_logix)
        self.insert_conditionals_code(insertion_point, self.doc_conditionals)
        
    def insert_logixandconditionals(self):
        if self.logLevel > 0: print "insert_logix"
        self.doc = self.insert_logix_code(self.doc, self.doc_logix)
        self.doc = self.insert_conditionals_code(self.doc, self.doc_conditionals)        
        
    # **************************************************
    # Create and insert icons in the main document
    # **************************************************    

    def set_up_all_stopping_point_icons(self, block_name, button_coordinate, index, LayoutEditor):
        self.set_up_all_stopping_point_block_icon_codes(block_name, button_coordinate, index)       #stores the code temporarily
        self.insert_all_stopping_point_block_icon_codes(LayoutEditor)                                           #inserts the stored code
        
    def set_up_control_items(self, LayoutEditor):

        if self.logLevel > 3:  print "self.doc a", self.doc
        if self.logLevel > 3:  print "LayoutEditor a" , LayoutEditor
        self.set_up_control_icon_codes(LayoutEditor)
        if self.logLevel > 3:  print "self.doc b", self.doc
        if self.logLevel > 3:  print "LayoutEditor b" , LayoutEditor
        self.insert_control_icons(LayoutEditor)
        if self.logLevel > 3:  print "self.doc c", self.doc
        if self.logLevel > 3:  print "LayoutEditor c" , LayoutEditor
        #self.set_up_control_sensors()
        
    def set_up_all_non_stopping_point_block_icons(self, block_name, button_coordinate, index, LayoutEditor):
        #self.delete_block_icons(block_name)    # in case we are processing a 'run' panel
        #print"YYYYYYYYYYYYYYYYYYYYYYYYYYYYYY"
        self.set_up_all_non_stopping_point_block_icon_codes(block_name, button_coordinate, index)       #stores the code temporarily
        self.insert_all_non_stopping_point_block_icon_codes(LayoutEditor)

    # **************************************************
    # Create XML code for Sensors, and store temporarily
    # **************************************************                
                
    def set_up_all_sensor_codes(self, block_name, index):
    
        # movetoxx_stored sensor: store temorarily in self.doc_moveTo_sensor_stored
        
        sensor_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/sensor.xml')
        self.doc_moveTo_sensor_stored = ElementTree(file=sensor_xml)
        iter=self.doc_moveTo_sensor_stored.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(99000+index) #hopefully IS99xxx will not be used
                if child.tag == "userName": child.text = "MoveTo"+block_name.replace(" ","_")+"_stored"
        
        #movetoxx sensor: store temorarily in self.doc_moveTo_sensor        
        self.doc_moveTo_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_moveTo_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(98000+index)  #hopefully IS98xxxx will not be used
                if child.tag == "userName": child.text = "MoveTo"+block_name.replace(" ","_") 
        
        #moveInProgress sensor: store temorarily in self.doc_moveInProgress
        self.doc_moveInProgress = ElementTree(file=sensor_xml)
        iter=self.doc_moveInProgress.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(97000+index)  #hopefully IS97xxxx will not be used
                if child.tag == "userName": child.text = "MoveInProgress"+block_name.replace(" ","_")
                
    def set_up_control_sensor_codes(self):
    
        sensor_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/sensor.xml')
        self.doc_express_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_express_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+1) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "Express"
                
        self.doc_stop_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_stop_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+2) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "stopMasterSensor"

        self.doc_new_train_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_new_train_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+3) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "newTrainSensor" 

        self.doc_start_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_start_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+4) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "startDispatcherSensor"

        self.doc_sound_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_sound_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+5) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "soundSensor" 
                
        self.doc_simulate_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_simulate_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+6) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "simulateSensor"

        self.doc_view_scheduled_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_view_scheduled_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+7) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "viewScheduledSensor"
 

        self.doc_set_scheduler_time_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_set_scheduler_time_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+8) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "schedulerStartTimeSensor" 

        self.doc_start_scheduler_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_start_scheduler_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+9) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "startSchedulerSensor" 

        self.doc_set_route_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_set_route_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+10) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "setRouteSensor"

        self.doc_set_dispatch_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_set_dispatch_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+11) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "setDispatchSensor"

        self.doc_show_clock_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_show_clock_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+12) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "showClockSensor"  


        self.doc_edit_route_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_edit_route_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+13) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "editRoutesSensor" 

        self.doc_run_route_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_run_route_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+14) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "runRouteSensor"

        self.doc_set_stopping_length_sensor = ElementTree(file=sensor_xml)
        iter=self.doc_set_stopping_length_sensor.getiterator('sensor')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IS" + str(96000+15) #hopefully IS96xxx will not be used
                if child.tag == "userName": child.text = "setStoppingDistanceSensor"

    # **************************************************
    # Insert XML code for Sensors, from stored values
    # ************************************************** 

    def insert_code_before_item(self, item_name, insertion_tree, code_to_be_inserted):
        iter=data.iter(item_name)
        for result in iter:
            search_text = "./" + item_name    
            insertion_point = insertion_tree.findall(search_text)[0]       
        insertion_point.extend(code_to_be_inserted)
                    
    def insert_sensor_code(self, data, data1):
        #read in the the button code#
        #data = self.output
        # if self.logLevel > 0: print "insert_sensor_code","data",data
        iter=data.getiterator('sensors')
        #or iter = data.iter('sensors')
        for result in iter:
            xml_element_tree = data 
            insertion_point = xml_element_tree.findall("./sensors")[0]
                        
        #now we have found the insetion point, insert the button data
        
        #data = self.doc_moveTo_sensor
        iter_sensor=data1.getiterator('sensors')
        for result in iter_sensor:
            insertion_point.extend(result) 
            
        return xml_element_tree#
                      
    def insert_all_sensor_codes(self, block_name):
        # MoveToxx_stored sensor
        # if self.logLevel > 0: print "insert_all_sensor_codes","self.doc",self.doc
        self.doc = self.insert_sensor_code(self.doc, self.doc_moveTo_sensor_stored)
        # MoveToxx icon
        self.doc = self.insert_sensor_code(self.doc, self.doc_moveTo_sensor)
        #wstandard_MoveInProgress icon
        self.doc = self.insert_sensor_code(self.doc, self.doc_moveInProgress)       

    def insert_control_sensor_codes(self):
        # express_sensor
        self.insert_sensor_code(self.doc, self.doc_express_sensor)
        # new_train_sensor
        self.insert_sensor_code(self.doc, self.doc_new_train_sensor)
        #stop_sensor
        self.insert_sensor_code(self.doc, self.doc_stop_sensor) 
        #start_sensor
        self.insert_sensor_code(self.doc, self.doc_start_sensor) 
        #sound_sensor
        self.insert_sensor_code(self.doc, self.doc_sound_sensor)
        #simulate_sensor
        self.insert_sensor_code(self.doc, self.doc_simulate_sensor)
        #view_scheduled_sensor
        self.insert_sensor_code(self.doc, self.doc_view_scheduled_sensor)
        #set_scheduler_time_sensor
        self.insert_sensor_code(self.doc, self.doc_set_scheduler_time_sensor)
        #start_scheduler_sensor
        self.insert_sensor_code(self.doc, self.doc_start_scheduler_sensor)
        
        self.insert_sensor_code(self.doc, self.doc_set_route_sensor)
        self.insert_sensor_code(self.doc, self.doc_set_dispatch_sensor)
        self.insert_sensor_code(self.doc, self.doc_run_route_sensor)
        self.insert_sensor_code(self.doc, self.doc_set_stopping_length_sensor)
        
        self.insert_sensor_code(self.doc, self.doc_show_clock_sensor)
        self.insert_sensor_code(self.doc, self.doc_edit_route_sensor)

    def set_up_all_stopping_point_sensors(self, block_name, index):
        self.set_up_all_sensor_codes(block_name, index)             #stores the code temporarily
        self.insert_all_sensor_codes(block_name)                    #inserts the stored code
              
        
    def set_up_control_sensors(self):
        if self.logLevel > 3:  print "self.doc 1", self.doc
        self.set_up_control_sensor_codes()
        if self.logLevel > 3:  print "self.doc 2", self.doc
        self.insert_control_sensor_codes()
