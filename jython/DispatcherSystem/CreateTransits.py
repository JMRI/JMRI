import java
import jmri
import sys
import re
import threading
from javax.swing import JOptionPane

# from   java.awt.event import ActionListener
# from   java.awt import *
# from   java.lang      import Runnable
# from   javax.swing    import *

###########################################################################
###########################################################################
# Progress Bar
###########################################################################

from javax.swing import JButton, JFrame, JPanel, JProgressBar, \
                    JScrollPane, JTextArea, JTextField, WindowConstants

###########################################################################

class CreateTransits(jmri.jmrit.automat.AbstractAutomaton):

    logLevel = 0

    def __init__(self):
        pass
        
    def run_transits(self, filename_icon, filename_run):
        if self.logLevel > 1: print "will store new panel in filename" , filename_run
        self.msg = "About to create all transits and train info files\nrequired for dispatcher operation"
        self.msg = self.msg + "\n***********************\n Do you wish to continue\n***********************"
        myAnswer = JOptionPane.showConfirmDialog(None, self.msg)
        if myAnswer == JOptionPane.YES_OPTION:
            JOptionPane.showMessageDialog(None, 'OK continuing\nThis will take about 30 secs on fast machine for small layout', "As you wish", JOptionPane.WARNING_MESSAGE)
            pass
        elif myAnswer == JOptionPane.NO_OPTION:
            msg = 'Stopping'
            JOptionPane.showMessageDialog(None, 'Stopping', "Fix Error" , JOptionPane.WARNING_MESSAGE)
            return 
        elif myAnswer == JOptionPane.CANCEL_OPTION:
            msg = 'Stopping'
            JOptionPane.showMessageDialog(None, 'Stopping', "Have a cup of Tea", JOptionPane.WARNING_MESSAGE)
            return 
        elif myAnswer == JOptionPane.CLOSED_OPTION:
            if self.logLevel > 1: print "You closed the window. How rude!"
        self.process_panels()
        filename_icon = filename_run.replace("run","icon")
        msg = "All Transits and TrainInfo Files produced\n and saved in " + filename_run +"\n - Restart JMRI and \n - load the file " + filename_run + "\n - instead of " + filename_icon + "\nThen run Stage3 to set the dispatcher options\nand run the dispatcher system from the panel"
        self.displayMessage(msg)
        self.store_panel(filename_run)
        #msg = "Result stored in \n" + filename_run+ "\nrestart JMRI with above panel loaded"
        #self.displayMessage(msg )
        
    def store_panel(self, filename):
        if self.logLevel > 1: print "storing file"
        file = java.io.File(filename)
        cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager)
        result = cm.storeUser(file)
        if result : 
            msg = "store was successful" 
        else: 
            msg = "store failed"
        if self.logLevel > 1: print(msg)
    
    
    def process_panels(self):
        #self.g = StationGraph()
        EditorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
        if self.logLevel > 1: print "finding panels"
        for panel in EditorManager.getEditorsList():
            if self.logLevel > 1: print "Panel = ", panel   
            if self.logLevel > 1: print "type = " , type(panel)
            if self.logLevel > 1: print "****"
            if type(panel) == jmri.jmrit.display.layoutEditor.LayoutEditor:
                self.create_transits_and_trainTrainInfos(panel)       
         
    def displayMessage(self, msg):
        JOptionPane.showMessageDialog(None, msg, 'information', JOptionPane.INFORMATION_MESSAGE)
           
    # function to return key for any value 
    def get_key(self,val, my_dict): 
        for key, value in my_dict.items(): 
             if val == value: 
                 return key 
        return "key doesn't exist"    
    
    move_button_list = []
    
    def set_button_list(self):
        for i in range (1,13):
            self.move_button_list.append("MoveTo" + str(i) + "_stored")

    #********************************************
    
    # g = DefaultDirectedGraph(DefaultEdge)
    # g_name = DefaultDirectedGraph(DefaultEdge)
    # path_dict = {}
    signal_mast_dict = {}
    transit_dict = {}
    transit_name_dict = {}
    # station_block_list= [] 
    # station_blk_list= []
    frame = None
    doreverse = False
    frame_list = []

            
    ## ***********************************************************************************   
    ## produce transits routines
    ## ***********************************************************************************
        
    def create_transits_and_trainTrainInfos(self, panel):
        if self.logLevel > 1: print "*********************************** producing transit ******************************"
        #produce all the transits, so use the stopping graph which is more complete than the stopping graph
        
        self.delete_transits()
        self.delete_train_TrainInfos()        
        
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&&&& get_signal_mast_lists &&&&"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        
        self.get_signal_mast_lists(panel)
        
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&&&& produce_transits &&&&"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"        
        
        self.produce_transits()
        
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&&&& end produce_transits &&&&"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"

            
    def delete_train_TrainInfos(self):
        import os
        import shutil

        folder = jmri.util.FileUtil.getExternalFilename('preference:dispatcher/traininfo/')
        
        #create the folder if it does not exist
        if not os.path.exists(folder):
            os.makedirs(folder)

        for filename in os.listdir(folder):
            file_path = os.path.join(folder, filename)
            try:
                if os.path.isfile(file_path) or os.path.islink(file_path):
                    os.unlink(file_path)
                elif os.path.isdir(file_path):
                    shutil.rmtree(file_path)
            except Exception as e:
                if self.logLevel > 1: print('Failed to delete %s. Reason: %s' % (file_path, e))
                
    def get_first_signal_mast(self,signal_mast,layout_block):
                                                                  
        # 1) get_sections containing_block_with_second_signal_mast
        if self.logLevel > 1: print "block - ", layout_block.getUserName(), layout_block
        block = layout_block.getBlock()
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        sections = [section for section in SectionManager.getNamedBeanSet() if block in section.getBlockList()]
        if self.logLevel > 0: print "sections for first signal mast = " , [s.getUserName() for s in sections]
        
        # 2) of these sections find the one that contains the second signal mast
        
        signal_mast_name = str(signal_mast.getUserName())
        found_section_name = None
        for section in sections:
            if self.logLevel > 1: print "mast", signal_mast.getUserName() , "section",section.getUserName()
            section_name = str(section.getUserName())
            test = signal_mast_name in section_name
            
          
            if self.logLevel > 1: print test
            if signal_mast_name in section_name:
                found_section_name = section_name
                if self.logLevel > 0: print "     signal_mast_name", signal_mast_name, "section_name", section_name, "found_section", found_section_name
                break
            if self.logLevel > 0: print "     signal_mast_name", signal_mast_name, "section_name", section_name, "found_section", found_section_name
            
        if self.logLevel > 0: print "*************"
        if self.logLevel > 0: print "found_section", found_section_name,"signal_mast_name",signal_mast_name
            
        # 3) if the section has two signal masts. return the signal mast that isn't the previous signal mast
        # for stubs/sidings no signal mast will be returned, and we will start with the second signal mast
        
        # This routine needs improvement It gets the name of the signal masts by assuming that the section name contains two signal mast names
        # This is a good assumption if the section has not been modified (which it should not have been) but if it has been edited by hand and not given a standard name
        # the routine will crash
        # At least we try to pick this up
        if found_section_name != None:
            other_signal_mast_in_section = self.get_other_signal_mast_in_section(found_section_name, signal_mast_name)
            if self.logLevel > 0: print "other_signal_mast_in_section", other_signal_mast_in_section
            SignalMastManager = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
            if other_signal_mast_in_section != None:
                first_signal_mast = SignalMastManager.getByUserName(other_signal_mast_in_section)
                if self.logLevel > 1: print "first_signal_mast", first_signal_mast.getUserName()
                if first_signal_mast == None:
                    msg = "The routine at present requires section names to be the names of the signal masts separated by a :"
                    msg = msg + "\nEither rename the section or (recommended)"
                    msg = msg + "\nRerun the automatic signal logic and section generation"
                    msg = msg + "\nproblematical section name: " + section_name
                    if self.logLevel > 0: JOptionPane.showMessageDialog(None, msg, 'Correct and re-run', JOptionPane.WARNING_MESSAGE)
            else:
                first_signal_mast = None
        else:
            first_signal_mast = None
        return first_signal_mast
        
    # def mast_name_reversed(self, mast_name):
        # start_of = mast_name.find('to')
        # end_of = start_of + len('to')
        # fromstr = mast_name[0:start_of]
        # tostr = mast_name[end_of:]
        # mast_name_rev = tostr + "to" + fromstr
        # if self.logLevel > 1: print "mast_name_reversed", mast_name_rev
        # return mast_name_rev
        
    def get_signal_mast_lists(self, panel):
        global g
        for e in g.g_express.edgeSet():
            if self.logLevel > 1: print "******* signal_mast_list *******" 

            if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
            if self.logLevel > 0: print "&&&& producing signal mast list for  &&&&", e.getItem("path_name")
            if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"            
            
            # 1) get the signal mast list excluding the last signal mast
                                                     
            #if self.logLevel > 1: print "stopping",g.dict_path_stopping
            if self.logLevel > 1: print "edge = " , e.to_string()
            #layout_block_list = g.dict_path_stopping[e]
            layout_block_list = e.getItem("path")
            if self.logLevel > 1: print "layout_block_list",layout_block_list
            layout_block_list_name = e.getItem("path_name")
            if self.logLevel > 1: print "layout_block_list_name",layout_block_list_name
            #get the list of signal masts
            #panel = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager).get('My Layout')
            signal_mast_class = jmri.SignalMast
            lbctools= jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools()
            signal_mast_list=lbctools.getBeansInPath(layout_block_list,panel,signal_mast_class)
            if self.logLevel > 1: print "signal_mast_list",[sm.getUserName() for sm in signal_mast_list]
            

            
            # 2b) get the last signal mast
            last_block = layout_block_list[-1]
            penultimate_signal_mast = signal_mast_list[-1]
            last_signal_mast = self.get_last_signal_mast(penultimate_signal_mast, last_block)
            if self.logLevel > 0: print "last_signal_mast",last_signal_mast.getUserName()
            
            # get first signal mast if not in a stub/siding
            # 2a) get the first signal mast
            if self.logLevel > 1: print "get_first_signal_mast"
            if self.logLevel > 1: print "---------------------"
            first_block = layout_block_list[0]
            second_signal_mast = signal_mast_list[0]
            first_signal_mast = self.get_first_signal_mast(second_signal_mast, first_block)
            if self.logLevel > 0: print "first_signal_mast", first_signal_mast
            
            if self.logLevel > 0: print "signal_mast_list",[sm.getUserName() for sm in signal_mast_list]
            
            # need to know if we are in the block outside a stub/siding. If we are we need to start at second block in transit
            if first_signal_mast != None:
                e.setItem(neighbor_is_stub=self.neighbor_is_stub(first_block, first_signal_mast))
            else:
                e.setItem(neighbor_is_stub=False)
            
            # 3a) add the first signal mast if it was possible to get it
            if first_signal_mast !=None:
                signal_mast_list.insert(0, first_signal_mast)
                first_signal_mast_not_present = False
            else:
                first_signal_mast_not_present = True
            if self.logLevel > 1: print "signal mast list 3a" , signal_mast_list
            if self.logLevel > 0: print "signal mast list with fsm" , [ signal.getUserName() for signal in signal_mast_list]
            
            # 3b) add the last signal mast
            signal_mast_list.append(last_signal_mast)
            if self.logLevel > 1: print "final signal mast list " , signal_mast_list
            if self.logLevel > 0: print "final signal mast list " , [ signal.getUserName() for signal in signal_mast_list]
            
            # 4) store signal_mast_list
            e.setItem(signal_mast_list=signal_mast_list)
            e.setItem(first_signal_mast_not_present=first_signal_mast_not_present)
            
    def neighbor_is_stub(self, first_layout_block, first_signal_mast):
        p = 0
        if first_layout_block.getUserName() == "block6" : p = 1
        SignalMastLogicManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        smlForFirstMast = SignalMastLogicManager.getSignalMastLogic(first_signal_mast);
        neighbor = smlForFirstMast.getFacingBlock();
        if p==1 : print "neighbor = " , neighbor.getUserName(), self.section_is_stub(neighbor), 
        return self.section_is_stub(neighbor)
        
    def section_is_stub(self, layout_block):
        # A stub track block has one neighbor
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        for section in SectionManager.getNamedBeanSet():
            # Look for a user defined section that has one block that matches the layout block
            if section.getSectionType() == jmri.Section.USERDEFINED :
                if section.getNumBlocks() == 1 and section.getEntryBlock() == layout_block.getBlock() :
                    #layout_block is stub
                    return True
        return False

        
            
    def get_last_signal_mast(self,signal_mast,layout_block):
    
        if self.logLevel > 1: print "get_last_signal_mast"
                                                           
        # 1) get_sections containing_block_with_previous_signal_mast
        if self.logLevel > 1: print "block - ", layout_block.getUserName(), layout_block
        block = layout_block.getBlock()
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        # for section in SectionManager.getNamedBeanSet():
            # if self.logLevel > 1: print section.getUserName(), "section.getBlockList()", section.getBlockList()
            # if block in section.getBlockList():
                # section_found  = [section ]
                # if self.logLevel > 1: print "section_found" ,section_found
        sections = [section for section in SectionManager.getNamedBeanSet() if block in section.getBlockList()]
        if self.logLevel > 1: print "sections = " , sections
        
        # 2) of these sections find the one that contains the previous signal mast
        
        signal_mast_name = str(signal_mast.getUserName())
        found_section_name = None
        for section in sections:
            if self.logLevel > 1: print "mast", signal_mast.getUserName() , "section",section.getUserName()
            section_name = str(section.getUserName())
            test = signal_mast_name in section_name
            if self.logLevel > 1: print "signal_mast_name", signal_mast_name
            if self.logLevel > 1: print test
            if signal_mast_name in section_name:
                found_section_name = section_name
                if self.logLevel > 0: print "found_section", found_section_name
                break
            
        # 3) the section has two signal masts. return the signal mast that isn't the previous signal mast
        
        # This routine needs improvement It gets the name of the signal masts by assuming that the section name contains two signal mast names
        # This is a good assumption if the section has not been modified (which it should not have been) but if it has been edited by hand and not given a standard name
        # the routine will crash
        # At least we try to pick this up
        other_signal_mast_in_section = self.get_other_signal_mast_in_section(found_section_name, signal_mast_name)
        
        SignalMastManager = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
        if other_signal_mast_in_section != None:
            last_signal_mast = SignalMastManager.getByUserName(other_signal_mast_in_section)
        if self.logLevel > 1: print "last_signal_mast", last_signal_mast
        if last_signal_mast == None:
            msg = "The routine at present requires section names to be the names of the signal masts separated by a :"
            msg = msg + "\nEither rename the section or (recommended)"
            msg = msg + "\nRerun the automatic signal logic and section generation"
            msg = msg + "\nproblematical section name: " + section_name
            JOptionPane.showMessageDialog(None, msg, 'Correct and re-run', JOptionPane.WARNING_MESSAGE)
        return last_signal_mast
            
    def get_other_signal_mast_in_section(self, section_name, signal_mast_name):
        signal_masts = section_name.split(":")
        if self.logLevel > 0: print "signal_masts",signal_masts,"signal_mast_name",signal_mast_name
        if signal_mast_name == signal_masts[0]:
            return signal_masts[1]
        else:
            return signal_masts[0]
        
    def produce_transits(self):
                
        max_no_transits = 20000       #always produce transists useful for testing
        t = []
        
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&&&& producing transits &&&&"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        i = -1
        
        if self.logLevel > 1: print "g-",g.g_express

        no_of_edges = 0
        for e in g.g_express.edgeSet():
            no_of_edges += 1
            
        progress = 20
            
        interval_percent = int((100.0-progress)/8.0)
        if self.logLevel > 1: print "interval" , interval_percent, "progress", progress, "no_of_edges", no_of_edges
        interval_count = int(no_of_edges/8)
        interval_count_total = interval_count

        dpg=DisplayProgress_global()
        dpg.Update(str(progress)+ "% complete")

                           
        for e in g.g_express.edgeSet():
            if i > max_no_transits:                                                                                                     
                if self.logLevel > 1: print "passing",i
                pass
            else:
                i+=1
                if  i > interval_count_total:
                    interval_count_total = interval_count_total + interval_count
                    progress = int(progress + interval_percent)
                    if self.logLevel > 1: print progress, i
                    p = int(min(progress, 100))
                    if self.logLevel > 1: print "p" , p
                    dpg.Update(str(progress)+ "% complete")
                
                if self.logLevel > 1: print "creating",i
                filename_fwd = self.get_filename(e, "fwd")
                filename_rvs = self.get_filename(e, "rvs")
                if self.logLevel > 0: print "processing " ,filename_fwd
                
                transit = self.create_transit(e)
                if transit != None:
                    transit_name = transit.getUserName()
                    transit_name = transit_name.replace("_temp","")    #we will rename the transits soon so store the renamed transit in the info file
                    sml= [signalmast.getUserName() for signalmast in e.getItem("signal_mast_list")]
                    if self.logLevel > 1: print "transit info, name, transit", transit_name, transit, e.getItem("transit") , "\n", e.getItem("signal_mast_list"), sml
                    self.store_TrainInfo(e, self.store_forward_train_TrainInfo, filename_fwd, transit_name, transit )
                    if self.logLevel > 1: print "*************************************"
                    self.store_TrainInfo(e, self.store_reverse_train_TrainInfo, filename_rvs, transit_name, transit )
                    if self.logLevel > 1: print "*************************************"
                    if self.logLevel > 1: print "created transits", i, filename_fwd, " & rvs"
                    #self.delete_transit(transit)
                    #check if transit exists
                    #try:
                    #    transit = self.create_transit(e)

                        # if transit != None:
                            # transit_name = transit.getUserName()
                            # sml= [signalmast.getUserName() for signalmast in e.getItem("signal_mast_list")]
                            # if self.logLevel > 1: print "transit info, name, transit", transit_name, transit, e.getItem("transit") , "\n", e.getItem("signal_mast_list"), sml
                            # self.store_TrainInfo(e, self.store_forward_train_TrainInfo, filename_fwd, transit_name, transit )
                            # if self.logLevel > 1: print "*************************************"
                            # self.store_TrainInfo(e, self.store_reverse_train_TrainInfo, filename_rvs, transit_name, transit )
                            # if self.logLevel > 1: print "*************************************"
                            # if self.logLevel > 1: print "created transits", i, filename_fwd, " & rvs"
                            # self.delete_transit(transit)
                    # except Exception as e:
                        # if self.logLevel > 1: print "problem with transit fwd", filename_fwd
                        # if self.logLevel > 1: print(e)
                        # if self.logLevel > 1: print "end of problem with transit"                

        # # #we produced the transits and deleted each one so we did not have to check if a transit existed
        # # we now have to re-create each transit
        # i=0
        # for e in g.g_express.edgeSet():
            # if i > max_no_transits:
                # pass
            # else:
                # i+=1
                # # the transits may be shared by the edge routes, so put in try except 
                # try:
                    # transit = self.create_transit(e)
                # except jmri.JmriException as ex:
                    # filename = self.get_filename(e, "fwd")
                    # if self.logLevel > 1: print (ex),
                    # if self.logLevel > 1: print "transit for" , filename, "not produced (duplicate)"
                # except:
                    # filename = self.get_filename(e, "fwd")
                    # if self.logLevel > 1: print "transit for" , filename, "not produced (duplicate)"
                    
                # try:
                    # transit = self.create_transit(e)
                # except jmri.JmriException as ex:
                    # filename = self.get_filename(e, "rvs")
                    # if self.logLevel > 1: print (ex),
                    # if self.logLevel > 1: print "transit for" , filename, "not produced (duplicate)"
                # except:
                    # filename = self.get_filename(e, "rvs")
                    # if self.logLevel > 1: print "transit for" , filename, "not produced (duplicate)"
                    
                    
        self.rename_temp_transits()   #the transits above have _temp put on the end. We need to remove this         
                   
        dpg.killLabel()
        # for frame in self.frame_list:
            # self.closeframe(frame)
    def get_existing_transit(self, e):
    
        signal_mast_list = e.getItem("signal_mast_list")
        start_signal_mast = signal_mast_list[0].getUserName()
        end_signal_mast = signal_mast_list[-1].getUserName()
        transit_name = "From " + start_signal_mast +  " to " + end_signal_mast
        if self.logLevel > 1: print "transit_name",transit_name
        transits = jmri.InstanceManager.getDefault(jmri.TransitManager)
        transit_list = [transit  for transit in transits.getNamedBeanSet() if transit.getUserName() == transit_name]
        if self.logLevel > 1: print "transit_list",transit_list
        
        if transit_list == []:
            return None
        else:
            transit = transit_list[0]
            if self.logLevel > 1: print "transit", transit.getUserName()
            if self.logLevel > 1: print "transit_username", transit.getUserName()
            if self.logLevel > 1: print "transit exists", transit.getUserName(), transit
            e.setItem(transit=transit)
            return transit  
            
    def create_transit( self, e):
    
        #check if transit already exists
        transit = self.get_existing_transit(e)                                                
        if transit != None:
            if self.logLevel > 1: print "TRANSIT =",transit
            return transit                             
        #create transit
        #if self.logLevel > 1: print " creating transit "
        TransitCreationTool = jmri.jmrit.display.layoutEditor.TransitCreationTool()
        transit = None
        
        #iterate through the signalmasts
        signal_mast_list = e.getItem("signal_mast_list")
        #print "signal_mast_list", signal_mast_list
        for signal_mast in signal_mast_list:
            if self.logLevel > 1: print "adding ", signal_mast.getUserName()
            TransitCreationTool.addNamedBean(signal_mast)
            if self.logLevel > 0: print "added", signal_mast.getUserName()
        #if self.logLevel > 1: print "about to create transit"
        

        
        #create transit
        try:
            transit = TransitCreationTool.createTransit()
            if self.logLevel > 0: print "transit after calling transitcreationtool = ", transit
            #make note of the transit in the graph
            e.setItem(transit=transit)
            
            #turn on the "transit in progress" sensor in case it has been turned off by another transit
            transit_section_list = transit.getTransitSectionList()
            if self.logLevel > 0: print "transit_section_list", transit_section_list
            
            #last_section = transit_section_list.get(transit_section_list.size() - 1)
            last_section = transit_section_list[-1]
            
            if self.logLevel > 0: print "last_section", last_section.getSectionName()
            
            transit_action = None
            to_station_name = g.g_express.getEdgeTarget(e)
            sensor_name = self.sensor_name(to_station_name)
            transit_action=self.transit_action_turn_on(sensor_name)
            last_section.addAction(transit_action)           
        except jmri.JmriException as ex:
            if self.logLevel > 1: print(ex),
            if self.logLevel > 1: print "could not create transit", signal_mast_list
        except Exception as ex:
            if self.logLevel > 1: print(ex),
            if self.logLevel > 1: print "could not create transit", signal_mast_list
        if self.logLevel > 0: print "finished transit"
        
        # first_signal_mast_not_present = e.getItem("first_signal_mast_not_present")
        
        # if first_signal_mast_not_present:
            # if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
            # if self.logLevel > 0: print "&&&& first signal mast addition &&&&"
            # if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"

            # #get parameters for the existing first transit section
            # if self.logLevel > 1 : print"first_signal_mast_not_present"
            
            # TransitSectionList= transit.getTransitSectionList()
            # firstTransitSection = TransitSectionList[0]
            
            # if self.logLevel > 1 : print"TransitSectionList", TransitSectionList
            # if self.logLevel > 1 : print"firstTransitSection", firstTransitSection
        
            # [sectionList, sectionNameList, sectionDirectionList] = self.get_list_possible_sections(transit, firstTransitSection)
            
            # if self.logLevel > 1 : print"sectionList", sectionList
            # if self.logLevel > 1 : print"sectionNameList", sectionNameList
            # if self.logLevel > 1 : print"sectionDirectionList", sectionDirectionList
            
            
            # layout_block_list = e.getItem("path")
            # first_block = layout_block_list[0].getBlock()

            # sectionToInsert = self.get_section_containing_block(first_block, sectionList)
            
            # if self.logLevel > 1 : print"sectionToInsert",sectionToInsert
            
            # index = sectionList.index(sectionToInsert)
            # sectionName = sectionNameList[index]
            # direction = sectionDirectionList[index]
            
            # seq = 0
            
            # if self.logLevel > 1 : print"index", index, "sectionName",sectionName,"direction",direction,"seq",seq
            
            # transit_section_to_insert = jmri.TransitSection(sectionToInsert, seq, direction)
            # if self.logLevel > 0: print "transit_section_to_insert",transit_section_to_insert
            # #insert first_transit_section at start of transit

            
            # FirstSignalMastName = self.getSignalMastName(sectionToInsert)
            # SignalMastManager = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
            # firstsignalmast = SignalMastManager.getSignalMast(FirstSignalMastName)
            # signal_mast_list = e.getItem("signal_mast_list")
            # penultimate_signal_mast = signal_mast_list[0].getUserName()
            # signal_mast_list.insert(0, firstsignalmast)
            # e.setItem(signal_mast_list=signal_mast_list)
            
            # start_signal_mast = signal_mast_list[0].getUserName()
            # end_signal_mast = signal_mast_list[-1].getUserName()
            # oldTransitName = transit.getUserName()
            # newTransitName = "From " + start_signal_mast + " to " + end_signal_mast
            # if self.logLevel > 0: print "penultimate_signal_mast",penultimate_signal_mast
            # if self.logLevel > 0: print "oldTransitName",oldTransitName
            # if self.logLevel > 0: print "newTransitName",newTransitName
            # transit.setUserName(newTransitName)
            # #TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
            # #TransitManager.deleteTransit(transit)
            # #transit = TransitManager.createNewTransit(newTransitName)
            # #e.setItem(transit=transit)
            # transit.removeAllSections()
            # #sn = 0
            # #transit_section_to_insert.setSequenceNumber(sn)   
            # transit.addTransitSection(transit_section_to_insert)
            # for ts in TransitSectionList:
                # #increment the sequence number if required
                # ##sn = ts.getSequenceNumber()
                # #sn += 1
                # #ts.setSequenceNumber(sn)
                # transit.addTransitSection(ts)
            # if self.logLevel > 0: print "newTransitNamecheck",transit.getUserName()    
            # if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
            # if self.logLevel > 0: print "&&&& end signal mast addition &&&&&&"
            # if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"

        #set username temporarily so that it is not overwritten by another
        if transit.getUserName() == None:
            JOptionPane.showMessageDialog(None, 'transit name null', "", JOptionPane.WARNING_MESSAGE)
            print "transit_section_list",transit_section_list
        temp_name = transit.getUserName() + "_temp"
        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        if TransitManager.getTransit(temp_name) == None:
            transit.setUserName(temp_name)
        else:
            TransitManager.deleteTransit(transit)
            transit = TransitManager.getTransit(temp_name)
        return transit
        
    def getSignalMastName(self, section):
        #we get the mast name from the name of the section. section will be a : b and the signal mast that we get will be b
        # as the section is to a buffer and we want the buffer signal mast
        section_name = section.getUserName()
        signal_masts = section_name.split(":")
        if self.logLevel > 0: print "signal_masts",signal_masts
        return signal_masts[1]
    
    def get_list_possible_sections(self, transit, firstTransitSection):
    
        
    
        firstSection = firstTransitSection.getSection()
        firstSectionName = firstSection.getUserName()
        if self.logLevel > 1 : print"firstSectionName", firstSectionName
        firstSectionDirection = firstTransitSection.getDirection()
        #print "firstSectionDirection", firstSectionDirection
        
        sectionList = []
        sectionName = []
        sectionDirection = []
        
        if firstSectionDirection == jmri.Section.FORWARD:
            testDirection = jmri.Section.REVERSE
        else:
            testDirection = jmri.Section.FORWARD
            
        if self.logLevel > 1 : print"firstSectionDirection", firstSectionDirection,"testDirection",testDirection,"jmri.Section.FORWARD",jmri.Section.FORWARD,"jmri.Section.REVERSE",jmri.Section.REVERSE
            
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        
        for s in SectionManager.getNamedBeanSet():
            sName = s.getDisplayName()
            fcon = self.forwardConnected(s, firstSection, testDirection)
            rcon = self.reverseConnected(s, firstSection, testDirection)
            if (s != firstSection) and (self.forwardConnected(s, firstSection, testDirection)):
                sectionName.append(sName)
                sectionList.append(s)
                sectionDirection.append( jmri.Section.REVERSE)
            elif ((s != firstSection) and (self.reverseConnected(s, firstSection, testDirection))) :
                sectionName.append(sName)
                sectionList.append(s)
                sectionDirection.append(jmri.Section.FORWARD)
            if self.logLevel > 1 : print"sName", sName, "firstSection", firstSection.getDisplayName(),"fcon", fcon, "rcon", rcon
        
        if self.logLevel > 0: print "sectionList",sectionList, "sectionName",sectionName, "sectionDirection",sectionDirection
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&&&& end get_list_possible_sections &&&&"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
            
        return [sectionList, sectionName, sectionDirection]
    
    #following 2 routines are 'copied' from JMRI
    def forwardConnected(self, s1, s2, restrictedDirection):
        if ((s1 != None) and (s2 != None)):
            s1ForwardEntries = s1.getForwardEntryPointList()
            #print "s1ForwardEntries",s1ForwardEntries
            #java.util.List<jmri.EntryPoint> s2Entries
            if restrictedDirection == jmri.Section.FORWARD:
                s2Entries = s2.getReverseEntryPointList()
            elif restrictedDirection == jmri.Section.REVERSE:
                s2Entries = s2.getForwardEntryPointList()
            else:
                s2Entries = s2.getEntryPointList();
            #print "s2Entries",s2Entries
            
            for i in range(s1ForwardEntries.size()):
                b1 = s1ForwardEntries.get(i).getFromBlock()
                #print "b1=", b1.getUserName()
                for j in range(s2Entries.size()):
                    b2 = s2Entries.get(j).getFromBlock()
                    #print "b2", b2.getUserName()
                    if b1 == s2Entries.get(j).getBlock() and b2 == s1ForwardEntries.get(i).getBlock():
                        return True
        return False;

    def reverseConnected(self, s1, s2, restrictedDirection):
        if ((s1 != None) and (s2 != None)) :
            s1ReverseEntries = s1.getReverseEntryPointList();
            #java.util.List<jmri.EntryPoint> s2Entries
            if (restrictedDirection == jmri.Section.FORWARD) :
                s2Entries = s2.getReverseEntryPointList()
            elif (restrictedDirection == jmri.Section.REVERSE) :
                s2Entries = s2.getForwardEntryPointList()
            else:
                s2Entries = s2.getEntryPointList();
            
            for i in range (s1ReverseEntries.size()):
                b1 = s1ReverseEntries.get(i).getFromBlock()
                for j in range(s2Entries.size()):
                    b2 = s2Entries.get(j).getFromBlock()
                    if b1 == s2Entries.get(j).getBlock() and b2 == s1ReverseEntries.get(i).getBlock():
                        return True
        return False
        
    def get_section_containing_block(self, block, section_list):
    
        for section in section_list:
            if section.containsBlock(block):
                return section
        return None
               

    def delete_transits(self):
    
        # need to avoid concurrency issues when deleting more that one transit
        # use java.util.concurrent.CopyOnWriteArrayList  so can iterate through the transits while deleting
        
        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        #if self.logLevel > 1: print "Section"
        TransitList = java.util.concurrent.CopyOnWriteArrayList()
        for transit in TransitManager.getNamedBeanSet():
            TransitList.add(transit)
        
        for transit in TransitList:
            if self.logLevel > 1: print "deleting Transit ", transit.getUserName()
            TransitManager.deleteTransit(transit) 

    def rename_temp_transits(self):
        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        for transit in TransitManager.getNamedBeanSet():
            temp_name = transit.getUserName()
            orig_name = temp_name.replace("_temp","")
            transit.setUserName(orig_name)
        
    def get_filename(self, e, suffix):
    
        # suffix is "fwd" or "rvs"
        # graph is g.g_stopping
        # e is edge
        
        from_station_name = g.g_stopping.getEdgeSource(e)
        to_station_name = g.g_stopping.getEdgeTarget(e)
        neighbor_name = e.getItem("neighbor_name")
        index = e.getItem("index")
        
        filename = "From " + str(from_station_name) + " To " + str(to_station_name) + " Via " + str(neighbor_name) + " " + str(index) 
        filename = filename.replace(" ", "_")
        # filename_fwd = filename + "_fwd.xml"
        # filename_rvs = filename + "_rvs.xml"
        filename = filename + "_" + suffix + ".xml"

        return filename
        
    def store_TrainInfo(self, e, store_fn, filename, transit_name, transit):
    
        #store_fn is either store_forward_train_TrainInfo or store_reverse_train_TrainInfo
    
        #if self.logLevel > 1: print " storing transit "

        # or self.doreverse: 
        #store the transit if it has not already been stored
        e.setItem(transitname=filename)
        self.transit_name_dict[e] = filename #store here to check for duplicates
        
        dispatcherframe = store_fn(e, filename, transit_name, transit)
        return dispatcherframe

   
    def is_duplicate(self,a,dict):
        
        z=False
        if a in dict:
            z = True
        return z

    def delete_transit(self, transit):        
        #**** Delete current Transit ******* 
        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        if transit != None:
            TransitManager.deleteTransit(transit)
            

            
    def sensor_name(self,station_name):
    
        to_block_name = station_name
                
        # to_alias_name = self.anti_alias_block(to_block_name)
        # to_index = to_alias_name.replace("block","")
        
        #if we don't want to do the above then we need to create sensors with name MoveInProgress+trim(to_block_name)
        
        sensor_move_name = "MoveInProgress" + to_block_name.replace(" ","_")
        
        return sensor_move_name
            
    def transit_action_turn_on(self, sensor_name):
        when = jmri.TransitSectionAction.ENTRY
        what = jmri.TransitSectionAction.SETSENSORACTIVE
        swhat = sensor_name
        TransitSectionAction = jmri.TransitSectionAction(when, what) 
        TransitSectionAction.setStringWhat(swhat)
        #mWhen = when;
        #mWhat = what;
        #mDataWhen = dataWhen;
        #mDataWhat1 = dataWhat1;
        #mDataWhat2 = dataWhat2;
        #mStringWhen = sWhen;
        #mStringWhat = sWhat;
        return TransitSectionAction 
        
    def transit_action_turn_off(self, sensor_name):
        when = jmri.TransitSectionAction.TRAINSTOP
        what = jmri.TransitSectionAction.SETSENSORINACTIVE
        swhat = sensor_name
        TransitSectionAction = jmri.TransitSectionAction(when, what) 
        TransitSectionAction.setStringWhat(swhat)
        #mWhen = when;
        #mWhat = what;
        #mDataWhen = dataWhen;
        #mDataWhat1 = dataWhat1;
        #mDataWhat2 = dataWhat2;
        #mStringWhen = sWhen;
        #mStringWhat = sWhat;
        return TransitSectionAction 
        
    def transit_action_turn_off_delay(self, sensor_name, delay):
        when = jmri.TransitSectionAction.ENTRY
        what = jmri.TransitSectionAction.SETSENSORINACTIVE
        swhat = sensor_name
        delay = 20000
        TransitSectionAction = TransitSectionAction = jmri.TransitSectionAction(when, what) 
        TransitSectionAction.setStringWhat(swhat)
        TransitSectionAction.setDataWhen(delay)
        
        #mWhen = when;
        #mWhat = what;
        #mDataWhen = dataWhen;
        #mDataWhat1 = dataWhat1;
        #mDataWhat2 = dataWhat2;
        #mStringWhen = sWhen;
        #mStringWhat = sWhat;
        return TransitSectionAction

    # **************************************************
    # new produce TrainInfo file routines
    # ************************************************** 
    
    def store_forward_train_TrainInfo(self, e, filename, transit_name, transit): 

        TrainInfo = jmri.jmrit.dispatcher.TrainInfo()
        
        self.set_standard_items(e, TrainInfo, transit_name, transit)
        
        #set reverse flag
        TrainInfo.setRunInReverse(False)
        
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(TrainInfo, filename)

    def store_reverse_train_TrainInfo(self, e, filename, transit_name, transit): 

        TrainInfo = jmri.jmrit.dispatcher.TrainInfo()
        
        self.set_standard_items(e, TrainInfo, transit_name, transit)
        
        #set reverse flag
        TrainInfo.setRunInReverse(True)
        
        jmri.jmrit.dispatcher.TrainInfoFile().writeTrainInfo(TrainInfo, filename)
        
    def get_all_roster_entries_with_speed_profile(self):
        roster_entries_with_speed_profile = []
        r = jmri.jmrit.roster.Roster.getDefault()
        for roster_entry in jmri.jmrit.roster.Roster.getAllEntries(r):
            #if self.logLevel > 1: print roster_entry.getId(), roster_entry.getSpeedProfile()
            if roster_entry.getSpeedProfile() != None:
                roster_entries_with_speed_profile.append(roster_entry.getId())
        return roster_entries_with_speed_profile    

    def set_standard_items(self, e, TrainInfo, transit_name, transit):
    
        #find one of the trains in the roster
        
        list = self.get_all_roster_entries_with_speed_profile()
        if list == []:
            JOptionPane.showMessageDialog(None, "No roster entries\nCannot produce train info files", 'Stopping', JOptionPane.WARNING_MESSAGE)
            raise Exception
        else:
            if self.logLevel > 1: print "list of transits" , list
            train_name = str(list[0])      #use the first roster entry with a speed profile
        
            TrainInfo.setTrainName(train_name)
            TrainInfo.setTransitId(transit_name)
            TrainInfo.setTransitName(transit_name)
            TrainInfo.setTrainInTransit(False)
            TrainInfo.setTrainFromRoster(True)
            TrainInfo.setTrainFromTrains(False)
            TrainInfo.setTrainFromUser(False)
            TrainInfo.setDccAddress(" ")
            # special action for stubs
            path_name = e.getItem("path_name")
            if e.getItem("neighbor_is_stub") == False:
                TrainInfo.setStartBlockId(path_name[0])
                TrainInfo.setStartBlockName(path_name[0])
                # start block seq starts counting at 1 at the first block in path_name
                # we want the first block
                TrainInfo.setStartBlockSeq(1)
            else:
                TrainInfo.setStartBlockId(path_name[0])
                TrainInfo.setStartBlockName(path_name[0])
                # start block seq starts counting at 1 at the first block in path_name
                # we want the second block
                TrainInfo.setStartBlockSeq(2)
            TrainInfo.setDestinationBlockId(path_name[-1])
            TrainInfo.setDestinationBlockName(path_name[-1])
            TrainInfo.setDestinationBlockId(path_name[-1])
            no_of_blocks_in_path = len(path_name)
            #end block seq starts counting at 0 at the second block in path_name
            #we want the last block
            if self.logLevel > 1: print "got here"
            if self.logLevel > 1: print "transit = ", transit
            blocks = jmri.InstanceManager.getDefault(jmri.BlockManager)
            startBlock = blocks.getBlock(path_name[0])
            transit.getDestinationBlocksList(startBlock, False)
            destinationBlockSeqList = transit.getDestBlocksSeqList()
            if self.logLevel > 1: print "got destinationBlockSeqList", destinationBlockSeqList
            if self.logLevel > 1: print "no_of_blocks_in_path-1",no_of_blocks_in_path-1
            if self.logLevel > 1: print "path_name", path_name
            seq = destinationBlockSeqList.get(0)
            if self.logLevel > 1: print "seq",seq
            TrainInfo.setDestinationBlockSeq(seq)                                                            
            ALLOCATE_AS_FAR_AS_IT_CAN = -1          # this value can go in TrainInfo.setAllocationMethod() 
            ALLOCATE_BY_SAFE_SECTIONS = 0           # this value can go in TrainInfo.setAllocationMethod() 
            No_Blocks_Ahead = 3                     # or other values. These values can go in TrainInfo.setAllocationMethod() 
            
            TrainInfo.setAllocationMethod(ALLOCATE_AS_FAR_AS_IT_CAN)         #as far as possible (set to this for now to see the dispatch, but change to...
            TrainInfo.setResetWhenDone(False)
            #TrainInfo.setResetRestartSensor(boolean b)
            #TrainInfo.setResetStartSensor(boolean b)
            TrainInfo.setReverseAtEnd(False)
            TrainInfo.setTerminateWhenDone(True)
            TrainInfo.setPriority(5)
            
            # public static final int NONE = 0x00;               // no train type defined
            #LOCAL_PASSENGER = 0x01    ## low priority local passenger train
            # public static final int LOCAL_FREIGHT = 0x02;      // low priority freight train performing local tasks
            # public static final int THROUGH_PASSENGER = 0x03;  // normal priority through passenger train
            # public static final int THROUGH_FREIGHT = 0x04;    // normal priority through freight train
            # public static final int EXPRESS_PASSENGER = 0x05;  // high priority passenger train
            # public static final int EXPRESS_FREIGHT = 0x06;    // high priority freight train
            # public static final int MOW = 0x07;          // low priority maintenance of way train
            
            TrainInfo.setTrainType("LOCAL_PASSENGER")
            #TrainInfo.setDelaySensorName(String sen)

            #TrainInfo.setDelayedRestart(int ds)
            #TrainInfo.setDelayedStart(int ds)
            # TrainInfo.setRestartDelayMin(int s)
            # TrainInfo.setRestartSensorName(String sen)
            #TrainInfo.setDepartureTimeHr(int hr) 
            #TrainInfo.setDepartureTimeMin(int min)            #accept defaults
            
            #TrainInfo.setLoadAtStartup(False) #accept default
            
            
            TrainInfo.setAutoRun(True)
            TrainInfo.setSpeedFactor(1)
            TrainInfo.setMaxSpeed(0.6)
            TrainInfo.setRampRate("Medium Slow")
            TrainInfo.setUseSpeedProfile(True)
            TrainInfo.setStopBySpeedProfile(True)
            TrainInfo.setStopBySpeedProfileAdjust(1.0)
            TrainInfo.setSoundDecoder(False)
            
            
            TrainInfo.setResistanceWheels(True)
            TrainInfo.setMaxTrainLength(20.0)       
               
    # **************************************************
    # produce TrainInfo file routines
    # **************************************************
        
    def addToClipBoard(self, text):
        command = 'echo | set /p nul=' + text.strip() + '| clip'
        os.system(command) 

        
    def closeframe(self,frame):
        # frame.setVisible(True);
        # frame.toFront();
        # frame.requestFocus()
        frame.dispatchEvent(java.awt.event.WindowEvent(frame, java.awt.event.WindowEvent.WINDOW_CLOSING));
        #frame = None
        #if self.logLevel > 1: print frame
        

    def append_station_block_list(self,*blocks):
        for block_alias in blocks:
            station_block_name = alias_block(block_alias)
            g.station_block_list.append(station_block_name)
            g.station_blk_list.append(layoutblocks.getLayoutBlock(station_block_name))
        #if self.logLevel > 1: print g.station_block_list        
        
    def set_memory_all(self, train_name):
        for station_block_name in g.station_block_list:
            ## Build Diagram
            #if self.logLevel > 1: print station_block_name
            layoutBlock = layoutblocks.getLayoutBlock(station_block_name)
            sensor = layoutBlock.getOccupancySensor()
            if sensor.getKnownState() == ACTIVE:
                #if self.logLevel > 1: print "layoutblock =", layoutBlock
                #if self.logLevel > 1: print "sensor =", sensor
                mem=layoutBlock.getMemory()
                #if self.logLevel > 1: print "mem =", mem, mem.getValue()
                if mem.getValue() == None:
                    mem.setValue(train_name)                  

    def wait_sensor(self, sensorName, sensorState):
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('Sensor {} not found'.format(sensorName))
            return
        if sensorState == 'active':
            #if self.logLevel > 1: print ("wait_sensor active: sensorName {} sensorState {}",format(sensorName, sensorState))
            self.waitSensorActive(sensor)
        elif sensorState == 'inactive':
            self.waitSensorInactive(sensor)
        else:
            self.displayMessage('Sensor state, {}, is not valid'.format(sensorState))        
            
    ## ***********************************************************************************
    
    ## set routines
    
    ## ***********************************************************************************
        
    def set_sensor(self, sensorName, sensorState):
        sensor = sensors.getSensor(sensorName)
        if sensor is None:
            self.displayMessage('Sensor {} not found'.format(sensorName))
            return
        if sensorState == 'active':
            newState = ACTIVE
        elif sensorState == 'inactive':
            newState = INACTIVE
        else:
            self.displayMessage('{} - Sensor state, {}, is not valid'.format(self.threadName, sensorState))
            return
        sensor.setKnownState(newState)    
        
    def setTurnout(self,turnoutName, turnoutState, turnoutDelay=0):
        #turnouts = jmri.TurnoutManager
        turnout = turnouts.getTurnout(turnoutName)
        if turnout is None:
            self.displayMessage('Turnout {} not found'.format(turnoutName))
            return
        if turnoutState == 'closed':
            newState = CLOSED
        elif turnoutState == 'thrown':
            newState = THROWN
        else:
            self.displayMessage('Turnout state, {}, is not valid'.format(turnoutState))
            return
        turnout.setCommandedState(newState)
        # Wait up to 5 seconds for feedback
        for i in range(0, 20):
            if turnout.getKnownState() == newState:
                break;
            if self.logLevel > 1: print 'Turnout feedback loop: {}'.format(i)
            self.waitMsec(250)
        self.waitMsec(turnoutDelay)
        
    def set_memory(self, block_name, train_name):
        #if self.logLevel > 1: print "in set_memory"
        #if self.logLevel > 1: print "train_name =", train_name
        #if self.logLevel > 1: print "block_name =", block_name
        layoutBlock = layoutblocks.getLayoutBlock(block_name)
        if layoutBlock == None:
            block_name = alias_block(block_name)
            layoutBlock = layoutblocks.getLayoutBlock(block_name)
        #if self.logLevel > 1: print "layoutblock =", layoutBlock
        mem = layoutBlock.getMemory()
        #if self.logLevel > 1: print "mem =", mem, mem.getValue()
        if mem.getValue() == None:
            mem.setValue(train_name)         
    

    #********************************************
    

                    
    def get_block(self, block_name):
        layoutBlock = layoutblocks.getLayoutBlock(block_name)
        return layoutBlock
                   
    def get_memory(self, block_name):
        layoutBlock = layoutblocks.getLayoutBlock(block_name)
        #if self.logLevel > 1: print "layoutNlock =", layoutBlock
        mem = layoutBlock.getMemory()
        mem_val = mem.getValue()
        return mem_val
        

        
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList        
        
class ClearTransits(CreateTransits):

    def __init__(self):
        #if self.logLevel > 1: print "deleting transits"
        self.delete_transits()
        #if self.logLevel > 1: print "deleted transits"
    
    def handle(self):  #just to make it close down
        pass


    def delete_transits(self):
    
        # need to avoid concurrency issues when deleting more that one transit
        # use java.util.concurrent.CopyOnWriteArrayList  so can iterate through the transits while deleting
        
        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        #if self.logLevel > 1: print "Section"
        TransitList = java.util.concurrent.CopyOnWriteArrayList()
        for transit in TransitManager.getNamedBeanSet():
            TransitList.add(transit)
        
        for transit in TransitList:
            if self.logLevel > 1: print "deleting Transit ", transit.getUserName()
            TransitManager.deleteTransit(transit)
            
           
class DisplayProgress:


    def __init__(self):
        #labels don't seem to work. This is the only thing I could get to work. Improvements welcome
        self.frame1 = JFrame('Hello, World!', defaultCloseOperation=JFrame.DISPOSE_ON_CLOSE, size=(300, 50), locationRelativeTo=None)

        self.frame1.setVisible(True)
    
    def Update(self,msg):
        self.frame1.setTitle(msg)
        
    def killLabel(self):
        self.frame1.setVisible(False)
        self.frame1 = None
