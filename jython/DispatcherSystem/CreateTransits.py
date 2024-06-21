import java
import jmri
import sys
import re
import threading

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

    def run_transits(self):

        #self.msg = "About to create all transits and train info files\nrequired for dispatcher operation"


        self.process_panels()
        #msg = "All Transits and TrainInfo Files produced\n and saved in " + filename_run +"\n - Restart JMRI and \n - load the file " + filename_run + "\n - instead of " + filename_icon + "\nThen run Stage3 to set the dispatcher options\nand run the dispatcher system from the panel"

        # msg = msg + "A backup of the original file has been saved in " + backupfilename + "\n\n"

        #self.displayMessage(msg)
        msg = "All Sections, Transits and TrainInfo Files produced.\n\n"
        msg = msg + 'The JMRI tables and panels have been updated to support the Dispatcher System\nA store is recommended.'
        self.displayMessage(msg)
        #self.store_panel(filename)

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
        EditorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
        if self.logLevel > 1: print "finding panels"
        LayoutPanels = []
        for panel in EditorManager.getList():
            if self.logLevel > 1: print "Panel = ", panel
            if self.logLevel > 1: print "type = " , type(panel)
            if self.logLevel > 1: print "****"
            if type(panel) == jmri.jmrit.display.layoutEditor.LayoutEditor:
                #self.create_transits_and_trainTrainInfos(panel)
                LayoutPanels.append(panel)
        self.create_transits_and_trainTrainInfos(LayoutPanels)

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

    signal_mast_dict = {}
    transit_dict = {}
    transit_name_dict = {}
    frame = None
    doreverse = False
    frame_list = []


    ## ***********************************************************************************
    ## produce transits routines
    ## ***********************************************************************************

    def create_transits_and_trainTrainInfos(self, layoutPanels):
        if self.logLevel > 1: print "*********************************** producing transit ******************************"
        # produce all the transits, so use the stopping graph which is more complete than the stopping graph

        self.delete_transits()
        self.delete_train_TrainInfos()

        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&&&& get_signal_mast_lists &&&&"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"

        self.get_signal_mast_lists(layoutPanels)

        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&&&& produce_transits &&&&"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"

        display_progress = True
        self.produce_transits(display_progress)

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
            test = self.signal_mast_name_in_section_name(signal_mast_name, section_name)


            if self.logLevel > 1: print test
            if self.signal_mast_name_in_section_name(signal_mast_name, section_name):
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
            #if other_signal_mast is None, then the section is a stub originating at the buffer and going to the first signal mast
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

    def signal_mast_name_in_section_name(self, signal_mast_name, section_name):
        #need to check whether the signal_mast_name is one of the masts comprising the section_name
        # section_nam e is of the form [signal_mast_name1 : signal_mast_name2]

        #split the section_name into two signal_masts

        if section_name.find(":") != -1:
            # get the two signal masts
            signal_masts = section_name.split(":")
            if signal_mast_name == signal_masts[0]:
                return True
            elif signal_mast_name == signal_masts[1]:
                return True
            else:
                return False
            if self.logLevel > 0: print "signal_masts",signal_masts,"signal_mast_name",signal_mast_name
        else:
            return None
                                                                           
    def get_signal_masts(self, layout_block, signal_mast_list_all):
        signal_masts_associated_with_current_block = []
        block = layout_block.getBlock()
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        sections = [section for section in SectionManager.getNamedBeanSet() if block in section.getBlockList()]
        if self.logLevel > 0: print "sections", sections
        if self.logLevel > 0: print "signal_mast_list_all", signal_mast_list_all
        #there may be several sections containing the block
        for signal_mast in signal_mast_list_all:
            signal_mast_name = signal_mast.getUserName()
            for section in sections:
                if self.logLevel > 0: print "signal_mast_name",signal_mast_name
                if self.logLevel > 0: print "section.getUserName()", section.getUserName()
                if self.logLevel > 0: print "signal_mast_name in section.getUserName()",signal_mast_name in section.getUserName()
                if signal_mast_name in section.getUserName():
                    signal_masts_associated_with_current_block.append(signal_mast)
                    if self.logLevel > 0: print "signal_masts_associated_with_current_block",signal_masts_associated_with_current_block
        return signal_masts_associated_with_current_block

    def get_signal_mast_lists(self, layoutPanels):
        global g
        for e in g.g_express.edgeSet():
            if self.logLevel > 1: print "******* signal_mast_list *******"

            if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
            if self.logLevel > 0: print "&&&& producing signal mast list for  &&&&", e.getItem("path_name")
            if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
            signal_mast_list = java.util.ArrayList()
            signal_mast_list_all = java.util.ArrayList()   #all the signal masts, possibly in a jumbled list, due to them being appended haphazardly
            signal_mast_list_view = []
            signal_mast_list_views = java.util.ArrayList()
            panelNo = 0
            no_panels_used = 0
            for panel in layoutPanels:
                panelNo += 1
                if self.logLevel > 0: print "*****panel" ,panelNo,"**********panelName", panel.getLayoutName()
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
                if self.logLevel > 0: print "layout_block_list"
                signal_mast_list_for_panel=lbctools.getBeansInPath(layout_block_list,panel,signal_mast_class)
                #signal_mast_list_for_panel=lbctools.getBeansInPath(layout_block_list,None,signal_mast_class)

                if self.logLevel > 1: print "signal_mast_list_for_panel",[sm.getUserName() for sm in signal_mast_list_for_panel]
                if signal_mast_list_for_panel == [] :
                    if self.logLevel > 0: print "continuing"
                    continue   #ignore panels where list of signal masts is blank

                no_panels_used += 1
                signal_mast_list_views.append([sm.getUserName() for sm in signal_mast_list_for_panel])
                if len(signal_mast_list_for_panel) > len(signal_mast_list):
                    signal_mast_list = signal_mast_list_for_panel
                signal_mast_list_all.addAll([sm for sm in signal_mast_list_for_panel])
                #remove duplicates
                if self.logLevel > 0: print "signal_mast_list_all with dups", signal_mast_list_all
                signal_mast_list_all = java.util.ArrayList(java.util.LinkedHashSet(signal_mast_list_all))

                if self.logLevel > 0: print "signal_mast_list_all without dups", signal_mast_list_all
                #if self.logLevel > 1: print "signal_mast_list",[sm.getUserName() for sm in signal_mast_list]
                if self.logLevel > 0: print "signalmast list ", [sm.getUserName() for sm in signal_mast_list]
                if self.logLevel > 0: print "signal_mast_list_views ", signal_mast_list_views
            if self.logLevel > 0: print
            if self.logLevel > 0: print "signal_mast_list_all", signal_mast_list_all
            if self.logLevel > 0: print "signal_mast_list_all", [s.getUserName() for s in signal_mast_list_all]
            if self.logLevel > 0: print "no_panels_used", no_panels_used
            
            if signal_mast_list_all.size() == 0 :
                msg = "there must be signal masts between and beyond each pair of stopping points. "
                msg = msg + "please insert a signal mast, or remove a station for the station pair: "
                msg = msg + e.getItem("first_block_name") + " : " + e.getItem("last_block_name")
                dpg=DisplayProgress_global()
                dpg.resize()
                dpg.Update(msg)

            if no_panels_used > 1:

                # signal_mast-list_all now has all the signal masts, but not necessarily in the right sequence
                # we now sort them bearing in mind the name of the section has the two signal masts joining them in the name
                #
                # create a list of the final sorted values
                signal_mast_list.clear()
                #
                # a) make a useful list (itermediate task)
                #
                signal_mast_useful_list = []
                sorted_list = []
                if self.logLevel > 0: print "***********"
                if self.logLevel > 0: print "make useful signal mast list"
                if self.logLevel > 0: print "***********"
                signal_mast_useful_list = self.set_up_useful_signal_mast_list(signal_mast_list_all, signal_mast_useful_list)
                if self.logLevel > 0: print "signal_mast_useful_list", signal_mast_useful_list
                if self.logLevel > 0: print
                if self.logLevel > 0: print "***********"
                if self.logLevel > 0: print "sorted_list to be done"
                if self.logLevel > 0: print "***********"
                #
                # b) setup sorted_list
                #
                sorted_list = self.setup_sorted_list(signal_mast_useful_list)
                if self.logLevel > 0: print
                if self.logLevel > 0: print "***********"
                if self.logLevel > 0: print "sorted_list_list done"
                if self.logLevel > 0: print "***********"
                if self.logLevel > 0: print "sorted_list_list", sorted_list
                if self.logLevel > 0: print "signal_mast_useful_list should be []", signal_mast_useful_list
                #
                # c) get the final signal_mast_list
                #

                sm1_tag, sm1_name_tag, sm2_tag, sm2_name_tag, section_tag, section_name_tag = range(6)
                signal_mast_list = [s[sm1_tag] for s in sorted_list]
                if self.logLevel > 0: print
                if self.logLevel > 0: print "****************"
                if self.logLevel > 0: print "signal_mast_list done"
                if self.logLevel > 0: print "****************"
                if self.logLevel > 0: print "signal_mast_list", signal_mast_list

                if self.logLevel > 0: print "signal_mast_list (names)", [s.getUserName() for s in signal_mast_list]

            else:
                signal_mast_list = signal_mast_list_all


            # 2b) get the last signal mast
            if self.logLevel > 0: print "signal_mast_list",signal_mast_list
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

    def add_item_to_end_of_list(self, item, list):
        list.append(item)

    def add_item_to_beginning_of_list(self, item, list):
        list.insert(0, item)

    def get_first_signal_mast_name_from_list_item(self, useful_list_item):
        sm1_tag, sm1_name_tag, sm2_tag, sm2_name_tag, section_tag, section_name_tag = range(6)
        section_name = useful_list_item[section_tag].getUserName() #3rd item is section 4th item is section_name
        #print "useful_list_item", useful_list_item
        #print "section", useful_list_item[section_tag]             #3rd item is section 4th item is section_name
        #print "section_name", section_name
        [signal_mast,signal_mast_name] = self.get_first_signal_mast_name(section_name)
        return signal_mast_name

    def get_first_signal_mast_name(self, section_name):
        #sm1, sm1_name, sm2, sm2_name, section, section_name = range(6)
        SignalMastManager = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
        signal_mast_names = []
        #print "section_name",section_name
        signal_mast_names = section_name.split(":")
        #print "get_first_signal_mast_name: signal_mast_names",signal_mast_names,"section_name",section_name
        signal_mast = SignalMastManager.getSignalMast(signal_mast_names[0])
        return [signal_mast,signal_mast_names[0]]

    def get_second_signal_mast_name_from_list_item(self,useful_list_item):
        if self.logLevel > 0: print "useful_list_item",useful_list_item
        sm1_tag, sm1_name_tag, sm2_tag, sm2_name_tag, section_tag, section_name_tag = range(6)
        section_name = useful_list_item[section_name_tag]
        if self.logLevel > 0: print "section_name", section_name
        [signal_mast,signal_mast_name] = self.get_second_signal_mast_name(section_name)
        return signal_mast_name


    def get_second_signal_mast_name(self, section_name):
        #if self.logLevel > 0: print "get_second_signal_mast_name: section_name",section_name
        SignalMastManager = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
        signal_mast_names = []
        signal_mast_names = section_name.split(":")
        #if self.logLevel > 0: print "get_second_signal_mast_name: signal_mast_names",signal_mast_names,"section_name",section_name
        if len(signal_mast_names) == 2:
            #if self.logLevel > 0: print "qwe"
            signal_mast = SignalMastManager.getSignalMast(signal_mast_names[1])
            #if self.logLevel > 0: print "qwert"
            x = signal_mast_names[1]
            #if self.logLevel > 0: print "x", x
            return [signal_mast,signal_mast_names[1]]
        else:
            #if self.logLevel > 0: print"asdf"
            return [None, None]      # section_name is the name of a stub-block

    #sm1, sm1_name, sm2, sm2_name, section, section_name = range(6)

    def set_up_useful_signal_mast_list(self, signal_mast_list_all, useful_signal_mast_list):
        if self.logLevel > 0: print
        if self.logLevel > 0: print "*********************"
        if self.logLevel > 0: print "**setup_useful_list**"
        if self.logLevel > 0: print "*********************"
        # look at all sections.
        # For each section get the start and end signal mast,
        # If the first and last signalmast is in the signal_mast_list_all, then we have found the corresponding section.
        # we store this in useful_signal_mast_list
        item_to_be_added = None
        list_of_signal_mast_names = [sm.getUserName() for sm in signal_mast_list_all]
        if self.logLevel > 0: print "list_of_signal_mast_names", list_of_signal_mast_names
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        for signal_mast in signal_mast_list_all:
            signal_mast_name = signal_mast.getUserName()
            if self.logLevel > 0: print "looking for",signal_mast_name
            for section in SectionManager.getNamedBeanSet():
                section_name = section.getUserName()
                [signalmast1, signalmast1name] = self.get_first_signal_mast_name(section_name)
                [signalmast2, signalmast2name] = self.get_second_signal_mast_name(section_name)
                if self.logLevel > 0: print "*************"
                #if self.logLevel > 0: print "signalmast1", signalmast1, "signal_mast_name", signal_mast_name
                if signalmast1name == signal_mast_name:
                    #if self.logLevel > 0: print "\tfound sm1", "signalmast1", signalmast1, "signalmast2", signalmast2, "looking for", signal_mast_name
                    # store signalmast1
                    item_to_be_added = [signalmast1, signalmast1name, None, None, section, section_name]
                    if signalmast2 != None and signalmast2name in list_of_signal_mast_names:
                        item_to_be_added = [signalmast1, signalmast1name, signalmast2, signalmast2name, section, section_name]
                        # if self.logLevel > 0: print "\t\tfound sm2 in list of sms", "sm1", signalmast1, "sm2", signalmast2, "list of poss s2", list_of_signal_mast_names
                        # we have the correct section
                        break
                    else:
                        pass
            if self.logLevel > 0: print "found ", "item_to_be_added", item_to_be_added,  "list_of_signal_mast_names", list_of_signal_mast_names
            if item_to_be_added != None:
                useful_signal_mast_list.append(item_to_be_added)
        return useful_signal_mast_list

    def setup_sorted_list(self, signal_mast_useful_list):
        # the useful lst has all the information to sort it
        # each node has the previous node and the next node

        if self.logLevel > 0: print
        if self.logLevel > 0: print "********************"
        if self.logLevel > 0: print "**setup_sorted_list**"
        if self.logLevel > 0: print "**************************"
        sorted_list = []
        if self.logLevel > 0: print "signal_mast_useful_list before", signal_mast_useful_list
        #initialise
        #delete the 0th item from signal_mast_list_all, and puts it in signal_mast_list
        sorted_list.append(signal_mast_useful_list[0])
        signal_mast_useful_list.pop(0)
        if self.logLevel > 0: print "Initialise: sorted_list", sorted_list
        if self.logLevel > 0: print "Initialise: signal_mast_useful_list", signal_mast_useful_list
        if self.logLevel > 0: print "len(signal_mast_useful_list)", len(signal_mast_useful_list)

        #additems to end of list
        while len(signal_mast_useful_list) > 0:
            item_added = True
            while item_added == True: #only keep on round this loop until we cannot add any more
                item_added = False
                if len(signal_mast_useful_list) == 0:
                    break
                if self.logLevel > 0: print "sorted_list x", sorted_list
                if self.logLevel > 0: print "sorted_list y", sorted_list[-1]
                required_signal_mast_name = self.get_second_signal_mast_name_from_list_item(sorted_list[-1])
                if required_signal_mast_name == None : break  #None has been inserted if end of path
                if self.logLevel > 0: print "required signal mast", required_signal_mast_name
                item_added = False
                for sm in signal_mast_useful_list:
                    if self.get_first_signal_mast_name_from_list_item(sm) == required_signal_mast_name:
                        item_added = True
                        sorted_list.append(sm)
                        signal_mast_useful_list.remove(sm)
                        if self.logLevel > 0: print "item",sm, "added"
                        break
                    else:
                        if self.logLevel > 0: print "item",sm, " not added"

            if self.logLevel > 0: print "after adding to end", sorted_list

            #add items to end of list
            while len(signal_mast_useful_list) > 0:
                required_signal_mast_name = self.get_first_signal_mast_name_from_list_item(sorted_list[0])
                if len(signal_mast_useful_list) == 0:
                    break
                for sm in signal_mast_useful_list:
                    if self.get_second_signal_mast_name_from_list_item(sm) == required_signal_mast_name:
                        sorted_list.insert(0,sm)
                        signal_mast_useful_list.remove(sm)
            if self.logLevel > 0: print "after adding to beginning", sorted_list
        if self.logLevel > 0: print "final sorted_list", sorted_list
        return sorted_list

    def get_masts_for_first_two_blocks(self, first_layout_block, second_layout_block, signal_mast_list_all):

        first_block_signal_masts = self.get_signal_masts(first_layout_block, signal_mast_list_all)
        second_block_signal_masts = self.get_signal_masts(second_layout_block, signal_mast_list_all)
        if self.logLevel > 0: print "first_block_signal_masts",first_block_signal_masts
        if self.logLevel > 0: print "second_block_signal_masts",second_block_signal_masts

        if self.logLevel > 0: print("remove duplicates")
        first_block_signal_masts=self.remove_duplicates_in_java_list(java.util.ArrayList(first_block_signal_masts))
        second_block_signal_masts=self.remove_duplicates_in_java_list(java.util.ArrayList(second_block_signal_masts))
        if self.logLevel > 0: print "first_block_signal_masts",first_block_signal_masts
        if self.logLevel > 0: print "second_block_signal_masts",second_block_signal_masts

        #many of the below may be None
        first_signal_mast = set(first_block_signal_masts).intersection(set(second_block_signal_masts))
        second_signal_mast = set(second_block_signal_masts).difference(set(first_signal_mast))
        third_signal_mast = set(second_block_signal_masts).difference(set(second_signal_mast))
        #there may be a zeroth mast
        zeroth_signal_mast = set(first_block_signal_masts).difference(set(first_signal_mast))

        #return the signal masts in order
        signal_masts = java.util.ArrayList()
        if self.logLevel > 0: print java.util.ArrayList(zeroth_signal_mast)
        if len(java.util.ArrayList(zeroth_signal_mast))>0:
            signal_masts.append(java.util.ArrayList(zeroth_signal_mast)[0])
        if len(java.util.ArrayList(first_signal_mast))>0:
            signal_masts.append(java.util.ArrayList(first_signal_mast)[0])
        if len(java.util.ArrayList(second_signal_mast))>0:
            signal_masts.append(java.util.ArrayList(second_signal_mast)[0])
        if len(java.util.ArrayList(third_signal_mast))>0:
            signal_masts.append(java.util.ArrayList(third_signal_mast)[0])
        if self.logLevel > 0: print "signal_masts",signal_masts
        return signal_masts

    def remove_duplicates (self, mylist):
        return list(dict.fromkeys(mylist))

    def remove_duplicates_in_java_list (self, list):
        set = java.util.LinkedHashSet()

        # Add the elements to set
        set.addAll(list);

        # Clear the list
        list.clear();

        # add the elements of set
        # with no duplicates to the list
        list.addAll(set);
        return list


    def get_signal_masts(self, layout_block, signal_mast_list_all):
        signal_masts_associated_with_current_block = []
        block = layout_block.getBlock()
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        sections = [section for section in SectionManager.getNamedBeanSet() if block in section.getBlockList()]
        if self.logLevel > 0: print "sections", sections
        if self.logLevel > 0: print "signal_mast_list_all", signal_mast_list_all
        #there may be several sections containing the block
        for signal_mast in signal_mast_list_all:
            signal_mast_name = signal_mast.getUserName()
            for section in sections:
                if self.logLevel > 0: print "signal_mast_name",signal_mast_name
                if self.logLevel > 0: print "section.getUserName()", section.getUserName()
                if self.logLevel > 0: print "signal_mast_name in section.getUserName()",signal_mast_name in section.getUserName()
                if signal_mast_name in section.getUserName():
                    signal_masts_associated_with_current_block.append(signal_mast)
                    if self.logLevel > 0: print "signal_masts_associated_with_current_block",signal_masts_associated_with_current_block
        return signal_masts_associated_with_current_block



    # def get_next_mast(self,signal_mast,previous_layout_block,layout_block):
    # print "layout_block",layout_block
    # print "signal_mast", signal_mast.getUserName()
    # next_mast = self.get_last_signal_mast(signal_mast,layout_block)
    # return next_mast

    def get_next_mast(self, first_layout_block, second_layout_block, signal_mast_list_all):

        first_block_signal_masts = self.get_signal_masts(first_layout_block, signal_mast_list_all)
        second_block_signal_masts = self.get_signal_masts(second_layout_block, signal_mast_list_all)
        if self.logLevel > 0: print "first_block_signal_masts",first_block_signal_masts
        if self.logLevel > 0: print "second_block_signal_masts",second_block_signal_masts

        if self.logLevel > 0: print("remove duplicates")
        first_block_signal_masts=self.remove_duplicates_in_java_list(java.util.ArrayList(first_block_signal_masts))
        second_block_signal_masts=self.remove_duplicates_in_java_list(java.util.ArrayList(second_block_signal_masts))
        if self.logLevel > 0: print "first_block_signal_masts",first_block_signal_masts
        if self.logLevel > 0: print "second_block_signal_masts",second_block_signal_masts

        #many of the below may be None
        first_signal_mast = set(first_block_signal_masts).intersection(set(second_block_signal_masts))
        second_signal_mast = set(second_block_signal_masts).difference(set(first_signal_mast))
        third_signal_mast = set(second_block_signal_masts).difference(set(second_signal_mast))
        #there may be a zeroth mast
        zeroth_signal_mast = set(first_block_signal_masts).difference(set(first_signal_mast))

        #return the signal masts in order
        signal_masts = java.util.ArrayList()
        if self.logLevel > 0: print java.util.ArrayList(zeroth_signal_mast)
        if len(java.util.ArrayList(zeroth_signal_mast))>0:
            #signal_masts.append(java.util.ArrayList(zeroth_signal_mast)[0])
            pass
        if len(java.util.ArrayList(first_signal_mast))>0:
            signal_masts.append(java.util.ArrayList(first_signal_mast)[0])
        if len(java.util.ArrayList(second_signal_mast))>0:
            #signal_masts.append(java.util.ArrayList(second_signal_mast)[0])
            pass
        if len(java.util.ArrayList(third_signal_mast))>0:
            #signal_masts.append(java.util.ArrayList(third_signal_mast)[0])
            pass
        if self.logLevel > 0: print "signal_masts",signal_masts
        return signal_masts[0]

    def neighbor_is_stub(self, first_layout_block, first_signal_mast):  # UK neighbour_is_siding
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

        block = layout_block.getBlock()
        if self.logLevel > 1: print "block - ", layout_block.getUserName(), layout_block, block.getUserName(), block
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        # for section in SectionManager.getNamedBeanSet():
        # if self.logLevel > 1: print section.getUserName(), "section.getBlockList()", section.getBlockList()
        # if block in section.getBlockList():
        # section_found  = [section ]
        # if self.logLevel > 1: print "section_found" ,section_found
        sections = [section for section in SectionManager.getNamedBeanSet() if block in section.getBlockList()]
        if sections == []:
            s = "You have not put signals between two stopping blocks, one being " + block.getUserName() + ".\n Unfortunately this system needs you to put them in"
            self.displayMessage(s,"fatal error, have a cup of tea!")
        if self.logLevel > 1: print "sections = " , [s.getUserName() for s in sections]

        # 2) of these sections find the one that contains the previous signal mast

        signal_mast_name = str(signal_mast.getUserName())
        found_section_name = None
        for section in sections:
            if self.logLevel > 1: print "mast", signal_mast.getUserName() , "section",section.getUserName()
            section_name = str(section.getUserName())
            test = signal_mast_name in section_name
            if self.logLevel > 1: print "signal_mast_name", signal_mast_name
            if self.logLevel > 1: print test
            if self.signal_mast_name_in_section_name(signal_mast_name, section_name):
                found_section_name = section_name
                if self.logLevel > 0: print "found_section", found_section_name
                break

        # 3) the section has two signal masts. return the signal mast that isn't the previous signal mast

        # This routine needs improvement It gets the name of the signal masts by assuming that the section name contains two signal mast names
        # This is a good assumption if the section has not been modified (which it should not have been) but if it has been edited by hand and not given a standard name
        # the routine will crash
        # At least we try to pick this up
        if found_section_name == None:
            self.displayMessage("There is probably a missing signal mast in block "+block.getUserName(),"Fatal Error that needs correcting")
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

    def displayMessage(self, msg, title = ""):

        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.PLAIN_MESSAGE,
                                         None,
                                         ["OK"],
                                         None)
        return s

    def get_other_signal_mast_in_section(self, section_name, signal_mast_name):
        #a section name usually contains two names of signal masts separated by a :
        # if the section is a stub it just consists of the name of the block
        if section_name.find(":") != -1:
            # get the two signal masts
            signal_masts = section_name.split(":")
            if signal_mast_name == signal_masts[0]:
                return signal_masts[1]
            else:
                return signal_masts[0]
            if self.logLevel > 0: print "signal_masts",signal_masts,"signal_mast_name",signal_mast_name
        else:
            return None

    def produce_transits(self, display_progress = False):
        global dpg
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

        if 'dpg' not in globals():
            dpg=DisplayProgress_global()
        if display_progress:
            dpg.Update("creating transits: " + str(progress) + "% complete")


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
                    if display_progress:
                        dpg.Update("creating transits: " + str(progress)+ "% complete")

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

        self.rename_temp_transits()   #the transits above have _temp put on the end. We need to remove this
        if display_progress:
            dpg.killLabel()

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
        if self.logLevel > 0: print "signal_mast_list", [sm.getUserName() for sm in signal_mast_list]
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

        #set username temporarily so that it is not overwritten by another
        if transit.getUserName() == None:
            JOptionPane.showMessageDialog(None, 'transit name null', "", JOptionPane.WARNING_MESSAGE)
            if self.logLevel > 0: print "transit_section_list",transit_section_list
        via = e.getItem("second_block_name")
        temp_name = transit.getUserName() + " via " + via + "_temp"
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

    # def get_list_possible_sections(self, transit, firstTransitSection):

    # firstSection = firstTransitSection.getSection()
    # firstSectionName = firstSection.getUserName()
    # if self.logLevel > 1 : print"firstSectionName", firstSectionName
    # firstSectionDirection = firstTransitSection.getDirection()

    # sectionList = []
    # sectionName = []
    # sectionDirection = []

    # if firstSectionDirection == jmri.Section.FORWARD:
    # testDirection = jmri.Section.REVERSE
    # else:
    # testDirection = jmri.Section.FORWARD

    # if self.logLevel > 1 : print"firstSectionDirection", firstSectionDirection,"testDirection",testDirection,"jmri.Section.FORWARD",jmri.Section.FORWARD,"jmri.Section.REVERSE",jmri.Section.REVERSE

    # SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)

    # for s in SectionManager.getNamedBeanSet():
    # sName = s.getDisplayName()
    # fcon = self.forwardConnected(s, firstSection, testDirection)
    # rcon = self.reverseConnected(s, firstSection, testDirection)
    # if (s != firstSection) and (self.forwardConnected(s, firstSection, testDirection)):
    # sectionName.append(sName)
    # sectionList.append(s)
    # sectionDirection.append( jmri.Section.REVERSE)
    # elif ((s != firstSection) and (self.reverseConnected(s, firstSection, testDirection))) :
    # sectionName.append(sName)
    # sectionList.append(s)
    # sectionDirection.append(jmri.Section.FORWARD)
    # if self.logLevel > 1 : print"sName", sName, "firstSection", firstSection.getDisplayName(),"fcon", fcon, "rcon", rcon

    # if self.logLevel > 0: print "sectionList",sectionList, "sectionName",sectionName, "sectionDirection",sectionDirection
    # if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
    # if self.logLevel > 0: print "&&&& end get_list_possible_sections &&&&"
    # if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"

    # return [sectionList, sectionName, sectionDirection]

    # #following 2 routines are 'copied' from JMRI
    # def forwardConnected(self, s1, s2, restrictedDirection):
    # if ((s1 != None) and (s2 != None)):
    # s1ForwardEntries = s1.getForwardEntryPointList()
    # #print "s1ForwardEntries",s1ForwardEntries
    # #java.util.List<jmri.EntryPoint> s2Entries
    # if restrictedDirection == jmri.Section.FORWARD:
    # s2Entries = s2.getReverseEntryPointList()
    # elif restrictedDirection == jmri.Section.REVERSE:
    # s2Entries = s2.getForwardEntryPointList()
    # else:
    # s2Entries = s2.getEntryPointList();
    # #print "s2Entries",s2Entries

    # for i in range(s1ForwardEntries.size()):
    # b1 = s1ForwardEntries.get(i).getFromBlock()
    # #print "b1=", b1.getUserName()
    # for j in range(s2Entries.size()):
    # b2 = s2Entries.get(j).getFromBlock()
    # #print "b2", b2.getUserName()
    # if b1 == s2Entries.get(j).getBlock() and b2 == s1ForwardEntries.get(i).getBlock():
    # return True
    # return False;

    # def reverseConnected(self, s1, s2, restrictedDirection):
    # if ((s1 != None) and (s2 != None)) :
    # s1ReverseEntries = s1.getReverseEntryPointList();
    # #java.util.List<jmri.EntryPoint> s2Entries
    # if (restrictedDirection == jmri.Section.FORWARD) :
    # s2Entries = s2.getReverseEntryPointList()
    # elif (restrictedDirection == jmri.Section.REVERSE) :
    # s2Entries = s2.getForwardEntryPointList()
    # else:
    # s2Entries = s2.getEntryPointList();

    # for i in range (s1ReverseEntries.size()):
    # b1 = s1ReverseEntries.get(i).getFromBlock()
    # for j in range(s2Entries.size()):
    # b2 = s2Entries.get(j).getFromBlock()
    # if b1 == s2Entries.get(j).getBlock() and b2 == s1ReverseEntries.get(i).getBlock():
    # return True
    # return False

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

        my_list = self.get_all_roster_entries_with_speed_profile()
        if my_list == []:
            JOptionPane.showMessageDialog(None, "No roster entries\nCannot produce train info files", 'Stopping', JOptionPane.WARNING_MESSAGE)
            raise Exception
        else:
            if self.logLevel > 1: print "list of transits" , my_list
            train_name = str(my_list[0])      #use the first roster entry with a speed profile

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
        progress = 0
        self.frame1 = JFrame("creating transits: " + str(progress) + "% complete", defaultCloseOperation=JFrame.DISPOSE_ON_CLOSE, size=(500, 50), locationRelativeTo=None)

        self.frame1.setVisible(True)

    def Update(self,msg):
        self.frame1.setTitle(msg)

    def killLabel(self):
        self.frame1.setVisible(False)
        self.frame1 = None
        
    def resize(self):
        self.frame1.size=(1300, 50)
