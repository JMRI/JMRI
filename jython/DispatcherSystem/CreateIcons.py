# Script to automatically Generate Icons on Panel for automation purposes
#
# Author: Bill Fitch, copyright 2020
# Part of the JMRI distribution


#from elementtree.ElementTree import ElementTree
from xml.etree.ElementTree import ElementTree
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import SubElement
import xml.etree.ElementTree as ET

from javax.swing import JOptionPane

class processXML():

    logLevel = 0

    # doc = None
    # doc_moveTo_sensor = None
    # doc_moveTo_sensor_stored = None
    # doc_MoveTo_stored = 1
    iter = None
    list_of_stopping_points = []
    output = None

    def __init__(self,filename,finalPanelFilename):
        if self.logLevel > 0: print "filename",filename
        self.doc = ET.parse(filename)
         
        if self.perform_initial_checks() == True:
            self.remove_old_sensors_and_icons()
            self.remove_old_memories()
            self.remove_old_transits()
            # if self.logLevel > 0: print "removed oldsensors"
            self.get_list_of_stopping_points()
            # if self.logLevel > 0: print "got stopping points"
            self.add_required_sensors_and_icons()
            # if self.logLevel > 0: print "about to add_required_logix1"
            self.add_required_logix()
            # if self.logLevel > 0: print "got add_required_logix1"
            self.add_required_memories()
            # if self.logLevel > 0: print "added sensors and icons"
            #self.associate_blocks_with_memories()
            self.write(finalPanelFilename)
            if self.logLevel > 0: print "wrote panel",finalPanelFilename
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
                JOptionPane.showMessageDialog(None, 'OK continuing', "As you wish", JOptionPane.WARNING_MESSAGE)
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
                JOptionPane.showMessageDialog(None, 'OK continuing', "As you wish", JOptionPane.WARNING_MESSAGE)
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
                JOptionPane.showMessageDialog(None, 'OK continuing', "As you wish", JOptionPane.WARNING_MESSAGE)
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
                JOptionPane.showMessageDialog(None, 'OK continuing', "As you wish", JOptionPane.WARNING_MESSAGE)
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
        # if self.logLevel > 0: printing initial_dictionary 
        #if self.logLevel > 0: print("initial_dictionary", str(dict)) 
          
        # finding duplicate values 
        # from dictionary 
        # using a naive approach 
        rev_dict = {} 
          
        for key, value in dict.items(): 
            rev_dict.setdefault(value, set()).add(key) 
              
        result = ["blocks " +', '.join(values) + " have the same sensor " + str( key) for key, values in rev_dict.items() 
                                      if len(values) > 1] 
          
        # if self.logLevel > 0: printing result 
        #msg = (', '.join(result))
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
        # for message in list_of_stops:
            # self.ms2 = self.msg2 +"\n" + message
        if self.logLevel > 0: print self.msg2
        self.msg3 = ""
        self.msg3 = '\n - '.join(list_of_blocks)
        # for message in list_of_blocks:
            # self.ms3 = self.msg3 +"\n" + message
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
                    
    def remove_old_icons(self):
        #moveTo icons
        self.remove_icons_with_string_in_name("MoveTo")
        #moveInProgress icons
        self.remove_icons_with_string_in_name("MoveInProgress") 
        
    # **************************************************
    # remove existing sensors which willbe added later
    # ************************************************** 

    def remove_sensors_with_string_in_name(self, string_to_look_for):
        for sensors in self.doc.findall('sensors'):
            for sensor in sensors.findall('sensor'):
                if sensor.find('userName') != None:
                    userName = sensor.find('userName').text
                    if userName != None:
                        if string_to_look_for in userName:
                            sensors.remove(sensor)                         
                    
    def remove_old_sensors(self):
        #moveToxx and MoveToxx_stored sensors
        self.remove_sensors_with_string_in_name("MoveTo" ) 
        #moveTo sensors
        self.remove_sensors_with_string_in_name("MoveInProgress" ) 
        
    # **************************************************
    # remove existing sensors and icons which will be added later
    # **************************************************
    
    def remove_old_sensors_and_icons(self):
        self.remove_old_sensors()
        self.remove_old_icons()
                
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
                
    # **************************************************
    # adds the sensors and icon xml code for each block to the main panel xml code
    # **************************************************                 
    
    # self.list_of_stopping_points has been set up in get_stopping points()
    def add_required_sensors_and_icons(self):           
        index=0
        for block_name in self.list_of_stopping_points:
            # track_segment_ident = p.get_track_segments(block_name)
            if self.logLevel > 0: print "block_name " ,block_name 
            
            #, "track_segment_ident = ", track_segment_ident
            
            block_coordinate = self.get_block_coordinates(block_name)
            if self.logLevel > 0: print "block_coordinate = ", block_coordinate
            if block_coordinate != None:
                if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                self.set_up_all_icons(block_name,block_coordinate,index)
                self.set_up_all_sensors(block_name, index)
                index+=1
                
        #icons at non-stopping sections        
        for block in blocks.getNamedBeanSet():
            block_name = block.getUserName()
            if block_name != None:
                if block_name not in self.list_of_stopping_points:
                    # track_segment_ident = p.get_track_segments(block_name)
                    if self.logLevel > 0: print "!!!!!!!block_name " ,block_name 
                    
                    #, "track_segment_ident = ", track_segment_ident
                    
                    block_coordinate = self.get_block_coordinates(block_name)
                    if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                    if block_coordinate != None:
                        if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                        self.set_up_all_block_icons(block_name,block_coordinate,index)
                        index+=1
                    
        #control icons            
        self.set_up_control_items()
        
    def add_required_logix(self):
        if self.logLevel > 0: print "about to setup_Logix"
        self.set_up_logix_code()
        self.set_up_conditionals_code()
        if self.logLevel > 0: print "about to insert_logix"
        self.insert_logixandconditionals()

        

    # **************************************************
    # adds the sensors and icon xml code for each block to the main panael xml code
    # **************************************************                 
    
    # self.list_of_stopping_points has been set up in get_stopping points()
    def add_required_memories(self):           
        index=0
        #for block in blocks:
        BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
        #if self.logLevel > 0: print "Block"
        for block in BlockManager.getNamedBeanSet():
            #exclude blocks with no sensors
            if block.getSensor() != None:
                block_name = block.getUserName()
                if self.logLevel > 0: print "block_name = ", block_name
                block_coordinate = self.get_block_coordinates(block_name)
                if block_coordinate != None:
                    if self.logLevel > 0: print "block_coordinate = ", block_coordinate
                    #self.set_up_all_memory_icons(block_name,block_coordinate,index)
                    #self.set_up_all_memories(block_name, index)
                    self.set_up_all_blockcontent_icons(block_name,block_coordinate,index)
                
                index+=1             
                
    # **************************************************
    # Get the coordinates of a track segment in the block, so that the icons can be positioned on i
    # **************************************************                
            
        # iter=self.doc.getiterator('tracksegment')
        # ident_list =[]
        # i=0
        # for element in iter:
            # if element.get("blockname") == block_name:
                # ident_list.append(element.get("ident"))
                # i+=1        
        # if len(ident_list) >0:  # there may be some unused blocks
            # min_dist = 99999
            # mid_segment = None
            # for element in ident_list:
                # other_segments = copy.copy(ident_list)
                # other_segments.remove(segment)
                # for os in other_segments:
                    # distance = hop_distance(segment, os)
                    # if distance < min_dist:
                        # min_dist =distance
                        # mid_segment = Track_segment
                        # ident = element.get("ident")
                        # mid_element = element
        
            # connect1 = mid_element.get("connect1name")
            # type1 = mid_element.get("type1")
            # connect2 = mid_element.get("connect2name")
            # type2 = mid_element.get("type2")
            # if self.logLevel > 0: print "connect1 = ",connect1
            # if self.logLevel > 0: print "connect2 = ",connect2
            # x1,y1 = self.get_positionable_point_coordinate(connect1,type1)
            # x2,y2 = self.get_positionable_point_coordinate(connect2,type2)
            # x_button = int((float(x1)+float(x2))/2.0)
            # y_button = int((float(y1)+float(y2))/2.0)-10      #-10 to raise the button up a bit on the track
            # button_coordinates = [x_button,y_button]
            # if self.logLevel > 0: print "button coordinate = " , button_coordinates
            # return button_coordinates 

    # def hop_distance(self, from_track_segment,to_track_segment):
        # hop_dist = 

            
    def get_block_coordinates(self, block_name):
    
        if self.logLevel > 0: print "getting block coordinates ", block_name
    
        # get the first track segmant we get to for the block
        # it would look more aestheticlly pleasing if we got the middle track segment of the block
        # but life isn't perfect, and anyway people will want to move the icons around
        
        # but try this (assume the tracksegments are in order, and get the mid one)
          
        iter=self.doc.getiterator('tracksegment')
        ident_list =[]
        i=0
        for element in iter:
            if element.get("blockname") == block_name:
                ident_list.append(element.get("ident"))
                i+=1
        if len(ident_list) >0:  # there may be some unused blocks
            required_track_segment_index = int(len(ident_list)/2)
            if self.logLevel > 0: print "required_track_segment_index = ", required_track_segment_index
            # now with the mid track index it might just work
            
            iter=self.doc.getiterator('tracksegment')
            for element in iter:
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
                    x1,y1 = self.get_positionable_point_coordinate(connect1,type1)
                    x2,y2 = self.get_positionable_point_coordinate(connect2,type2)
                    x_button = int((float(x1)+float(x2))/2.0)
                    y_button = int((float(y1)+float(y2))/2.0)-10      #-10 to raise the button up a bit on the track
                    button_coordinates = [x_button,y_button]
                    if self.logLevel > 0: print "button coordinate = " , button_coordinates
                    return button_coordinates
        else:
            #check for turnouts
            iter=self.doc.getiterator('layoutturnout')
            ident_list =[]
            i=0
            for element in iter:
                if element.get("blockname") == block_name:
                    ident_list.append(element.get("ident"))
                    i+=1
                    
            if len(ident_list) >0:  # there may be some unused blocks
                required_turnout_index = int(len(ident_list)/2)
                if self.logLevel > 0: print "required_turnout_index = ", required_turnout_index
                # now with the mid track index it might just work
                
                iter=self.doc.getiterator('layoutturnout')
                for element in iter:
                    #if self.logLevel > 0: print element.get("blockname"), element.get("ident"), required_turnout_index, ident_list[required_turnout_index]
                    if element.get("blockname") == block_name and element.get("ident") == ident_list[required_turnout_index]:
                        #if self.logLevel > 0: print "got here"
                        ident = element.attrib["ident"]
                        if self.logLevel > 0: print "ident = ",  ident
                        xcen = element.get("xcen")
                        ycen = element.get("ycen")
                        #x1,y1 = self.get_positionable_point_coordinate(connect1,type1)
                        #x2,y2 = self.get_positionable_point_coordinate(connect2,type2)
                        x_button = int(float(xcen))
                        y_button = int(float(ycen))-10      #-10 to raise the button up a bit on the track
                        button_coordinates = [x_button,y_button]
                        if self.logLevel > 0: print "button coordinate = " , button_coordinates
                        return button_coordinates
        
        return None
                
    def get_positionable_point_coordinate(self, connection_name,type):
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
        else: 
            raise NameError('Unsupported track type, need to modify code for X-overs:' + type)
        # if self.logLevel > 0: print "connection " , connection
        iter=self.doc.getiterator(connection)
        for element in iter:
            if element.get("ident") == connection_name:
                # if self.logLevel > 0: print "here"
                x = element.get(x_ident)
                y = element.get(y_ident)
                coordinates = [x,y]
                # if self.logLevel > 0: print coordinates
                return coordinates
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
    
    def insert_blockcontent_icon_code(self, main_document_element_tree, icon_element_tree):

        # find the insertion point in main document
        if self.logLevel > 0: print "inserting bloccontent code"
        iter=main_document_element_tree.getiterator('LayoutEditor')
        for result in iter:
            xml_element_tree = main_document_element_tree 
            insertion_point = xml_element_tree.findall("./LayoutEditor")[0]
                        
        # now we have found the insetion point, insert the button data
        iter_icon = icon_element_tree.getiterator('LayoutEditor')
        for result in iter_icon:
            insertion_point.extend(result) 
        #self.output = xml_element_tree
        return xml_element_tree    

   
        
    def insert_all_blockcontent_icon_codes(self):
        #blockcontent icon
        self.doc = self.insert_blockcontent_icon_code(self.doc, self.doc_blockcontent)
    
    def insert_memory_icon_code(self, main_document_element_tree, icon_element_tree):

        # find the insertion point in main document
        iter=main_document_element_tree.getiterator('LayoutEditor')
        for result in iter:
            xml_element_tree = main_document_element_tree 
            insertion_point = xml_element_tree.findall("./LayoutEditor")[0]
                        
        # now we have found the insetion point, insert the button data
        iter_icon = icon_element_tree.getiterator('LayoutEditor')
        for result in iter_icon:
            insertion_point.extend(result) 
        #self.output = xml_element_tree
        return xml_element_tree    

   
        
    def insert_all_memory_icon_codes(self):
        #memory icon
        self.doc = self.insert_memory_icon_code(self.doc, self.doc_Memory)
        
    # **************************************************
    # Create and insert icons in the main document
    # **************************************************    

    def set_up_all_memory_icons(self, block_name, button_coordinate, index):
        if self.logLevel > 0: print "********mem icon set up*****************"
        self.set_up_all_memory_icon_codes(block_name, button_coordinate, index)       #stores the code temporarily
        if self.logLevel > 0: print "********mem icon code set up finished*****************"
        self.insert_all_memory_icon_codes()
        if self.logLevel > 0: print "icon set up", block_name
        if self.logLevel > 0: print "*************************"
        
    def set_up_all_blockcontent_icons(self, block_name, button_coordinate, index):
        if self.logLevel > 0: print "********mem icon set up*****************"
        self.set_up_all_blockcontent_icon_codes(block_name, button_coordinate, index)       #stores the code temporarily
        if self.logLevel > 0: print "********mem icon code set up finished*****************"
        self.insert_all_blockcontent_icon_codes()
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
            
    def set_up_all_icon_codes(self, block_name, icon_coordinates, index):
    
        # MoveTo icon
        icon_text = block_name[:9]
        icon_sensor_name = "MoveTo"+block_name.replace(" ","_") +"_stored"
        sensor_location = "IS98"+str(index).zfill(4)  
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
            sensor_location = sensor.getSystemName() 
            #icon_tooltip = icon_sensor_name + sensor_location
            icon_offset = [-20,10]
            icon_coordinates_withoffset = [a + b for a, b in zip(icon_coordinates, icon_offset)]
            self.doc_BlockSensor = ElementTree(file=small_icon_xml)
            self.set_up_small_icon_code(icon_sensor_name, icon_coordinates_withoffset, self.doc_BlockSensor)
            
    def set_up_all_block_icon_codes(self, block_name, icon_coordinates, index):
        # Block Sensor icon
        layoutBlock = layoutblocks.getLayoutBlock(block_name)
        block = layoutBlock.getBlock()
        sensor = block.getSensor()
        if sensor != None:
            icon_sensor_name = sensor.getUserName()
            sensor_location = sensor.getSystemName() 
            #icon_tooltip = icon_sensor_name + sensor_location
            icon_offset = [-20,10]
            icon_coordinates_withoffset = [a + b for a, b in zip(icon_coordinates, icon_offset)]
            small_icon_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/small_icon.xml')
            self.doc_BlockSensor = ElementTree(file=small_icon_xml)
            self.set_up_small_icon_code(icon_sensor_name, icon_coordinates_withoffset, self.doc_BlockSensor)
            
    def set_control_icon_coordinates(self):
    
        max_x = 0
        min_x = 0
        max_y = 0
        min_y = 0
        iter=self.doc.getiterator('tracksegment')
        for element in iter:
            ident = element.attrib["ident"]
            if self.logLevel > 0: print "ident = ",  ident
            connect1 = element.get("connect1name")
            type1 = element.get("type1")
            connect2 = element.get("connect2name")
            type2 = element.get("type2")
            if self.logLevel > 0: print "connect1 = ",connect1
            if self.logLevel > 0: print "connect2 = ",connect2
            x1,y1 = self.get_positionable_point_coordinate(connect1,type1)
            x2,y2 = self.get_positionable_point_coordinate(connect2,type2)
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
        return icon_coordinates
        
    def set_up_control_icon_codes(self):
        
        icon_coordinates = self.set_control_icon_coordinates()
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
        

    def insert_icon_code(self, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
        child = "LayoutEditor"
        root1 = main_document_element_tree.getroot()
        root2 = icon_element_tree.getroot()
        self.insertChildFromRoot2InRoot1(root1, root2,parent,child)

        return main_document_element_tree

    def insert_label_code(self, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
        child = "LayoutEditor"
        root1 = main_document_element_tree.getroot()
        root2 = icon_element_tree.getroot()
        self.insertChildFromRoot2InRoot1(root1, root2,parent,child)

        return main_document_element_tree      

    def insert_logix_code(self, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
        child = "logixs"
        root1 = main_document_element_tree.getroot()
        root2 = icon_element_tree.getroot()
        self.insertChildFromRoot2InRoot1(root1, root2,parent,child)
        
        return main_document_element_tree        

    def insert_conditionals_code(self, main_document_element_tree, icon_element_tree):
    
        parent = "layout-config"
       
        child = "conditionals"
        root1 = main_document_element_tree.getroot()
        root2 = icon_element_tree.getroot()
        self.insertChildFromRoot2InRoot1(root1, root2,parent,child)
        
        return main_document_element_tree 
        
    def insert_all_icon_codes(self):
        #wide_MoveTo icon
        self.doc = self.insert_icon_code(self.doc, self.doc_MoveTo)
        #wstandard_MoveInProgess icon
        self.doc = self.insert_icon_code(self.doc, self.doc_MoveInProgress)
        #Block Sensor icon
        self.doc = self.insert_icon_code(self.doc, self.doc_BlockSensor)
        
    def insert_all_block_icon_codes(self):
        #Block Sensor icon
        self.doc = self.insert_icon_code(self.doc, self.doc_BlockSensor)
        
        
    def insert_control_icons(self):
        self.doc = self.insert_icon_code(self.doc, self.doc_Express_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_new_train_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_start_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_stop_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_sound_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_simulate_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_view_scheduled_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_set_scheduler_time_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_start_scheduler_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_set_route_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_set_dispatch_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_run_route_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_show_clock_icon)
        self.doc = self.insert_icon_code(self.doc, self.doc_edit_routes_icon)          
        
        self.doc = self.insert_label_code(self.doc, self.doc_Express_label)
        self.doc = self.insert_label_code(self.doc, self.doc_new_train_label)
        self.doc = self.insert_label_code(self.doc, self.doc_start_label)
        self.doc = self.insert_label_code(self.doc, self.doc_stop_label)
        self.doc = self.insert_label_code(self.doc, self.doc_sound_label)
        self.doc = self.insert_label_code(self.doc, self.doc_simulate_label)
        self.doc = self.insert_label_code(self.doc, self.doc_view_scheduled_label)
        self.doc = self.insert_label_code(self.doc, self.doc_set_scheduler_time_label)
        self.doc = self.insert_label_code(self.doc, self.doc_start_scheduler_label)
        self.doc = self.insert_label_code(self.doc, self.doc_set_route_label)
        self.doc = self.insert_label_code(self.doc, self.doc_set_dispatch_label)
        self.doc = self.insert_label_code(self.doc, self.doc_run_route_label) 
        self.doc = self.insert_label_code(self.doc, self.doc_show_clock_label) 
        self.doc = self.insert_label_code(self.doc, self.doc_edit_routes_label) 
        
    def insert_logixandconditionals(self):
        if self.logLevel > 0: print "insert_logix"
        self.doc = self.insert_logix_code(self.doc, self.doc_logix)
        self.doc = self.insert_conditionals_code(self.doc, self.doc_conditionals)
        
    # **************************************************
    # Create and insert icons in the main document
    # **************************************************    

    def set_up_all_icons(self, block_name, button_coordinate, index):
        self.set_up_all_icon_codes(block_name, button_coordinate, index)       #stores the code temporarily
        self.insert_all_icon_codes()                                           #inserts the stored code
        
        
    def set_up_control_items(self):
        self.set_up_control_icon_codes()
        self.insert_control_icons()
        self.set_up_control_sensors()
        
    def set_up_all_block_icons(self, block_name, button_coordinate, index):
        self.set_up_all_block_icon_codes(block_name, button_coordinate, index)       #stores the code temporarily
        self.insert_all_block_icon_codes()
        
        
    # **************************************************
    # Create XML code for Memories, and store temporarily
    # **************************************************                
                
    def set_up_all_memory_codes(self,block_name, index):
    
        # movetoxx_stored sensor: store temorarily in self.doc_moveTo_sensor_stored
        memory_xml = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/sensorsAndIcons/memory.xml')
        self.doc_memory = ElementTree(file = memory_xml)
        iter=self.doc_memory.getiterator('memory')
        for element in iter:
            for child in element.getchildren():
                if child.tag == "systemName": child.text = "IM" + str(99000+index) #hopefully IM99xxx will not be used
                if child.tag == "userName": child.text = "M_"+block_name.replace(" ","_")      
        
    # **************************************************
    # Insert XML code for Memories, from stored values
    # **************************************************                
                
    def insert_memory_code(self, data, data1):

        iter=data.getiterator('memories')
        for result in iter:
            xml_element_tree = data 
            insertion_point = xml_element_tree.findall("./memories")[0]
                        
        #now we have found the insetion point, insert the memory data
        
        iter_memory=data1.getiterator('memories')
        for result in iter_memory:
            insertion_point.extend(result) 
            
        return xml_element_tree#               
                
    def insert_all_memory_codes(self):
        # MoveToxx_stored sensor
        if self.logLevel > 0: print "insert_all_memory_codes"
        self.doc = self.insert_memory_code(self.doc, self.doc_memory)
        if self.logLevel > 0: print "end insert_all_memory_codes"
        
    def associate_blocks_with_memories(self):
        if self.logLevel > 0: print "associate_blocks_with_memories"
        # need to associate layout blocks with memory system name           
        layoutblocks_iter = self.doc.getiterator('layoutblocks')
        for layoutblocks1 in layoutblocks_iter:
            layoutblock_iter = layoutblocks1.getiterator('layoutblock')     
            for layoutblock in layoutblock_iter:
                if self.logLevel > 0: print layoutblock.tag   #if self.logLevel > 0: prints 'layoutblock'
                block_system_name = layoutblock.get("systemName")
                if self.logLevel > 0: print block_system_name

                lblock =layoutblocks.getLayoutBlock(block_system_name)
                block_user_name = lblock.getBlock().getUserName()
                memory_name = "M_"+block_user_name.replace(" ","_")
                layoutblock.set("memory", memory_name)
                memory = layoutblock.get("memory")
                if self.logLevel > 0: print memory

                
            # block_system_name = layoutblock.get('systemName').text
            # if block_system_name != None:
                # if self.logLevel > 0: print block_system_name
                # fred=layoutblocks.getLayoutBlock(block_system_name)
                # if self.logLevel > 0: print fred
                # block_name = fred.getBlock().getUserName()
                # if self.logLevel > 0: print block_name
                # memory_name = "M_"+block_name.replace(" ","_")
                # element.set("memory", memory_name)
 

    def set_up_all_memories(self, block_name, index):
    
        self.set_up_all_memory_codes(block_name, index)  #stores the code temporarily
        self.insert_all_memory_codes()
        #self.associate_blocks_with_memories(block_name)
        #inserts the stored code         
    

    # **************************************************
    # Create XML code for Sensors, and store temporarily
    # **************************************************                
                
    def set_up_all_sensor_codes(self,block_name, index):
    
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
        
    # **************************************************
    # Insert XML code for Sensors, from stored values
    # **************************************************                
                
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
        #self.output = xml_element_tree
        
        # data = self.doc_moveTo_sensor_stored
        # iter_sensor_stored=data.getiterator('sensors')
        # for result in iter_sensor_stored:
            # insertion_point.extend(result) 
        # self.output = xml_element_tree                
                
    def insert_all_sensor_codes(self):
        # MoveToxx_stored sensor
        # if self.logLevel > 0: print "insert_all_sensor_codes","self.doc",self.doc
        self.doc = self.insert_sensor_code(self.doc, self.doc_moveTo_sensor_stored)
        # MoveToxx icon
        self.doc = self.insert_sensor_code(self.doc, self.doc_moveTo_sensor)
        #wstandard_MoveInProgess icon
        self.doc = self.insert_sensor_code(self.doc, self.doc_moveInProgress)       

    def insert_control_sensor_codes(self):
        # express_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_express_sensor)
        # new_train_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_new_train_sensor)
        #stop_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_stop_sensor) 
        #start_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_start_sensor) 
        #sound_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_sound_sensor)
        #simulate_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_simulate_sensor)
        #view_scheduled_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_view_scheduled_sensor)
        #set_scheduler_time_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_set_scheduler_time_sensor)
        #start_scheduler_sensor
        self.doc = self.insert_sensor_code(self.doc, self.doc_start_scheduler_sensor)
        
        self.doc = self.insert_sensor_code(self.doc, self.doc_set_route_sensor)
        self.doc = self.insert_sensor_code(self.doc, self.doc_set_dispatch_sensor)
        self.doc = self.insert_sensor_code(self.doc, self.doc_run_route_sensor)
        
        self.doc = self.insert_sensor_code(self.doc, self.doc_show_clock_sensor)
        self.doc = self.insert_sensor_code(self.doc, self.doc_edit_route_sensor)
        

        

    def set_up_all_sensors(self, block_name, index):
        self.set_up_all_sensor_codes(block_name, index)  #stores the code temporarily
        self.insert_all_sensor_codes()                   #inserts the stored code 
              
        
    def set_up_control_sensors(self):
        self.set_up_control_sensor_codes()
        self.insert_control_sensor_codes()
    
    # **************************************************
    # Main Program
    # **************************************************     

#create an ElementTree instance from an XML file

#doc = ElementTree("D:\\bill\\Documents\\ElementTree\\WR2working.xml")

# Start with list of blocks
# Find a track segment in the block
# If there are 3 trck segments take the middle one
# Get the anchor points
# Get the mid point
# Add the sensor
# Add  the movetostored<BlockName> sendor


# Note for this to work the required stopping points (blocks) must be marked in the comments
# field of the blocks table with the word 'stop' (lower or upper case)


if __name__ == "__main__":
    initialPanelFilename = "Z:\\WallRunDevl.jmri\\dispatcher\\Automation\\test.xml"
    finalPanelFilename = "Z:\\WallRunDevl.jmri\\dispatcher\\Automation\\test_out.xml"

    msg = "About to create file " + finalPanelFilename + "\n from " + initialPanelFilename 
    msg = msg + "\n  *****************************************************"
    msg = msg + "\nPanel " + initialPanelFilename + " should be open for this stage to work" 
    msg = msg + "\n  *****************************************************"
    msg = msg + "\nContinue?"
    myAnswer = JOptionPane.showConfirmDialog(None, msg)
    if myAnswer == JOptionPane.YES_OPTION:
        p = processXML(initialPanelFilename, finalPanelFilename)
    elif myAnswer == JOptionPane.NO_OPTION:
        msg = 'Stopping'
        JOptionPane.showMessageDialog(None, msg, 'Stopping', JOptionPane.WARNING_MESSAGE)
        
    elif myAnswer == JOptionPane.CANCEL_OPTION:
        msg = 'Stopping'
        JOptionPane.showMessageDialog(None, msg, 'Stopping', JOptionPane.WARNING_MESSAGE)
        
    elif myAnswer == JOptionPane.CLOSED_OPTION:
        if self.logLevel > 0: print "You closed the window. How rude!"

    




