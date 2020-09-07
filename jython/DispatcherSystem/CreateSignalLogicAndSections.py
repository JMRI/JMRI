# Script to automatically build Signal Logic and Sections
#
# Author: Bill Fitch, copyright 2020
# Part of the JMRI distribution

#
# NOTE: Currently the automatic method to build signal logic 
# is faulty. This script:
# - Deletes all existing Transits, Sections and Signal Logic
# - Runs the standard auto signal logic builder
# - corrects the results
# - builds Sections
#
# To correct the standard auto signal logic builder it uses 
# other standard code found elsewhere in JMRI
#
# All signal logic for a route that passes through a turnout 
# is replaced with corrected 'manual' logic


import jmri
from javax.swing import JButton, JFrame,JPanel,BoxLayout,Box,JScrollPane, JOptionPane, AbstractAction
#import javax.swing.AbstractAction

class Update_Signal_Logic():

    loglevel = 0
    
    # order of calling
    # - create_sections()
    # - create_transits()
    # - Update_logic()
    
    def create_autologic_and_sections(self):
        self.msg = "About to create all signal logic and sections\nrequired for dispatcher operation"
        self.msg = self.msg + "\n***********************\n Do you wish to continue\n***********************"
        myAnswer = JOptionPane.showConfirmDialog(None, self.msg)
        self.set_layout_editor()
        self.delete_transits()
        self.delete_sections()
        self.delete_signal_mast_logic()
        self.create_auto_signal_mast_logic()
        self.create_sections()
        
    # update_logic must be called after transits have been built with the auto_signal_mast_logic
    # or else the sections will not correspond to the signal_masts_logic at the time of 
    # transit creation and the transit build will fail
    
    def update_logic(self, filename_run):
        self.set_layout_editor()
        self.update_auto_signal_mast_logic()
        self.store_panel(filename_run)
        
    def store_panel(self, filename):
        if self.loglevel > 0:  print "storing file"
        file = java.io.File(filename)
        cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager)
        result = cm.storeUser(file)
        if result : 
            msg = "store was successful" 
        else: 
            msg = "store failed"
        if self.loglevel > 0:  print(msg)        
        
    def set_layout_editor(self):
        EditorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
        for layout_editor in EditorManager.getEditorsList():
            if type(layout_editor) == jmri.jmrit.display.layoutEditor.LayoutEditor:
                self.layout_editor = layout_editor        
    
    def delete_transits(self):
    
        # need to avoid concurrency issues when deleting more that one transit
        # use java.util.concurrent.CopyOnWriteArrayList  so can iterate through the transits while deleting
        
        TransitManager = jmri.InstanceManager.getDefault(jmri.TransitManager)
        #if self.loglevel > 0:  print "Section"
        TransitList = java.util.concurrent.CopyOnWriteArrayList()
        for transit in TransitManager.getNamedBeanSet():
            TransitList.add(transit)
        
        for transit in TransitList:
            if self.loglevel > 0:  print "deleting Transit ", transit.getUserName()
            TransitManager.deleteTransit(transit)    
    
    def delete_sections(self):
    
        # need to avoid concurrency issues when deleting more that one section
        # use java.util.concurrent.CopyOnWriteArrayList  so can iterate through the sections while deleting
    
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        #if self.loglevel > 0:  print "Section"
        SectionList = java.util.concurrent.CopyOnWriteArrayList()
        for section in SectionManager.getNamedBeanSet():
            SectionList.add(section)
        
        for section in SectionList:
            if self.loglevel > 0:  print "deleting Section ", section.getUserName()
            SectionManager.deleteSection(section)
            
    def delete_signal_mast_logic(self):
    
        # need to avoid concurrency issues when deleting more that one SignalMastLogic
        # use java.util.concurrent.CopyOnWriteArrayList  so can iterate through the SignalMastLogic while deleting
    
        SignalMastLogicManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        #if self.loglevel > 0:  print "SignalMastLogic"
        SignalMastLogicList = java.util.concurrent.CopyOnWriteArrayList()
        for SignalMastLogic in SignalMastLogicManager.getNamedBeanSet():
            SignalMastLogicList.add(SignalMastLogic)
        
        for SignalMastLogic in SignalMastLogicList:
            if self.loglevel > 0:  print "deleting SignalMastLogic ", SignalMastLogic.getUserName()
            SignalMastLogicManager.removeSignalMastLogic(SignalMastLogic)

    def create_auto_signal_mast_logic(self):
        SignalMastLogicManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        SignalMastLogicManager.automaticallyDiscoverSignallingPairs()
        method = jmri.jmrit.entryexit.EntryExitPairs.SETUPSIGNALMASTLOGIC
        EntryExitPairs = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs)
        EntryExitPairs.automaticallyDiscoverEntryExitPairs(self.layout_editor, method)
        
    def create_sections(self):
        (jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)).generateSection()
            
    def update_auto_signal_mast_logic(self):
        pass
        #jmri.jmrit.display.layoutEditor.ConnectivityUtil.getLayoutTurnoutsThisBlock(Block)
        # can list turnouts
        # get layout turnouts
        #jmri.jmrit.display.layoutEditor.LayoutEditorFindItems.findLayoutTurnoutByTurnoutName(String)
        # if have layout turnout
        # get layout turnout block
        # through paths going through a block
        # => start block, block , end block  turnout settings
        # => correspomding layout blocks
        
        
        #a we get the blocks either side of the turnout
        turnout_dict = {}
        turnout_dict1 = {}
        for turnout in turnouts.getNamedBeanSet():
            #if turnout.getUserName() == "Bd3Turnout3" or turnout.getUserName() == "Bd1Turnout1":
            layout_turnout = jmri.jmrit.display.layoutEditor.LayoutEditorFindItems(self.layout_editor).findLayoutTurnoutByTurnoutName(turnout.getUserName())
            if layout_turnout != None:
                layout_block = layout_turnout.getLayoutBlock()
                # layout_block_B = layout_turnout.getLayoutBlockB()         #do not need Bor C (path threough A goes through B or C
                # layout_block_C = layout_turnout.getLayoutBlockC()
                # layout_block_D = layout_turnout.getLayoutBlockD()         #need to check for D
                routes = self.get_routes(layout_block)
                if self.loglevel > 0:  print "++++++*****"
                if self.loglevel > 0:  print "routes" , routes
                if self.loglevel > 0:  print "++++"
                
                routes_names = [[str(r.getUserName()) for r in route] for route in routes ]
                if self.loglevel > 0:  print routes_names
                if self.loglevel > 0:  print "@@@@@"
                turnout_dict[turnout.getUserName()] = tuple(routes)


        #        % block turnout %
        #a         block turnout   block    turnout
        #b block   block turnout            autoblock
        
        #   % block trunout block %
        #a    block turnout block
        #b    block turnout block   block
        
        #        % block turnout turnout %
        # a       
        
        # a must be a subset of b
        # turnout must be a subset of autoblock
       
        # b we get the blocks between the signalmasts and the end block
        signal_mast_dict = {}
        signal_mast_orig_dict = {}
        SignalMastManager = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
        for sm_source in SignalMastManager.getNamedBeanSet():
            SignalMastLogicManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
            #if sm_source.getUserName() == "1AtoPT33" or  sm_source.getUserName() == "Board2toPT11":
            if self.loglevel > 0:  print "sm_source =", sm_source.getUserName()
            sml = SignalMastLogicManager.getSignalMastLogic(sm_source)
            if sml != None:
                if self.loglevel > 0:  print "sml not none"
                sm_dest_list = sml.getDestinationList()
                for sm_dest in sm_dest_list:
                    if self.loglevel > 0:  print "sm_dest", sm_dest.getUserName()
                    # get blocks in path
                    autoblocks_orig = sml.getAutoBlocksBetweenMasts(sm_dest)
                    autoblocks = sml.getAutoBlocksBetweenMasts(sm_dest)
                    if self.loglevel > 0:  print "autoblocks_orig", [a.getUserName() for a in autoblocks], autoblocks
                    #add facing block
                    facing_block = sml.getFacingBlock()
                    if facing_block != None:
                        autoblocks.append(facing_block)
                    else:
                        if self.loglevel > 0:  print("facing block None", sm_dest.getUserName(), last_block.getUserName())
                    #add on the block after dest for uniqueness
                    last_block = autoblocks[-1]
                    afterdest_block = self.getBlockAfterDestMast(sm_dest, last_block)
                    if afterdest_block != None:
                        autoblocks.append(afterdest_block)
                    else:
                        if self.loglevel > 0:  print("facing block None", sm_dest.getUserName(), last_block.getUserName())
                        
                    if self.loglevel > 0:  print "autoblocks", [a.getUserName() for a in autoblocks], autoblocks
                    signal_mast_dict[autoblocks] = (sm_source.getUserName(), sm_dest.getUserName(), sml, autoblocks_orig)
        if self.loglevel > 0:  print "!!!!!!!"
        
        # number_autoblocks = 0
        # for autoblocks in signal_mast_dict.keys():
            # number_autoblocks += 1
            
        # progress = 0
        # final = 30
        # interval_percent = int(10)
        # print "interval" , interval_percent, "progress", progress, "number_autoblocks", number_autoblocks
        # divisor = (final - progress) / interval_percent       
        # interval_count = int(number_autoblocks/divisor)
        # interval_count_total = interval_count

        # dpg=DisplayProgress_global()
        # dpg.Update(str(progress)+ "% complete")                  
        # find route corresponding to autoblocks

        # i = 0    
        for autoblocks in signal_mast_dict.keys():  #go through all the signal mast paths
            # i += 1
            # if  i > interval_count_total:
                # interval_count_total = interval_count_total + interval_count
                # progress = int(progress + interval_percent)
                # print progress, i
                # p = int(min(progress, 100))
                # print "p" , p
                # dpg.Update(str(progress)+ "% complete")
        
            ab_names = [b.getUserName() for b in autoblocks]
            sm = signal_mast_dict[autoblocks]
            sm_source, sm_dest, sml, autoblocks_orig = sm
            routes_in_autoblocks = []
            route_names_is_subset = False
            for turnout in turnout_dict.keys():  #
                #if self.loglevel > 0:  print "*****************"
                #if self.loglevel > 0:  print turnout
                td = turnout_dict[turnout]
                if self.loglevel > 0:  print td
                y = [ [x.getUserName() for x in t]   for t in td]
                routes = turnout_dict[turnout]
                if self.loglevel > 0:  print "routes", routes
                for route in routes:
                    route_names = [r.getUserName() for r in route]
                    # if self.loglevel > 0:  print "qwerty"
                    # if self.loglevel > 0:  print "set_routes", set(route_names) 
                    
                    # if self.loglevel > 0:  print "ab_names", set(ab_names), "from", sm_source, "to", sm_dest
                    # if self.loglevel > 0:  print "qwerty"
                    if set(route_names).issubset(ab_names):
                        if self.loglevel > 0:  print "success"
                        if self.loglevel > 0:  print "ab_names = ", ab_names
                        if self.loglevel > 0:  print "routes",routes
                        if self.loglevel > 0:  print "route",[str(x.getUserName()) for x in route]
                        if self.loglevel > 0:  print "autoblocks",[str(x.getUserName()) for x in autoblocks],str(sm_source), str(sm_dest)
                        routes_in_autoblocks.append(route)
                        route_names_is_subset = True
            #if any of the turnouts ar on the signal mast path we have stored the appropriate route in routes_in_autoblocks            
            if route_names_is_subset == True:
                self.update_signal_logic(sm, routes_in_autoblocks)
                
        #dpg.killLabel()
        msg = "Logic replaced"
        JOptionPane.showMessageDialog(None, msg, 'information', JOptionPane.INFORMATION_MESSAGE)
        
    def get_routes(self, LayoutBlock):

        LayoutBlock.addAllThroughPaths()
        
        ConnectivityUtil=jmri.jmrit.display.layoutEditor.ConnectivityUtil(self.layout_editor)
        
        list = []
        
        for index in range(1, LayoutBlock.getNumberOfThroughPaths()):

            prev = LayoutBlock.getThroughPathSource(index)
            curr = LayoutBlock.getBlock()
            next = LayoutBlock.getThroughPathDestination(index)
            
            prev_curr_next = [prev,curr,next]
            list.append(prev_curr_next)
            
        return list        
                        
    def getBlockAfterDestMast(self, sm_dest, last_block):
        SignalMastLogicManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        sml_dest = SignalMastLogicManager.getSignalMastLogic(sm_dest)
        if sml_dest != None:
            sm_dest_list = sml_dest.getDestinationList()
            for sm_next in sm_dest_list:
                if self.loglevel > 0:  print "sm_dest", sm_dest.getUserName()
                # get blocks in path
                autoblocks = sml_dest.getAutoBlocksBetweenMasts(sm_next)
                if autoblocks != []:
                    if self.loglevel > 0:  print "autoblocks = " , autoblocks
                    return autoblocks[0]
                
        return None                      
                        
    def update_signal_logic(self, signal_masts, routes_in_autoblocks):
        print "in update signal logic"
        #preliminary
        sm_source_name, sm_dest_name, sml, autoblocks = signal_masts
        SignalMastManager = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
        sm_dest = SignalMastManager.getSignalMast(str(sm_dest_name))
        
        #prepare blocks
        hashBlocks = java.util.Hashtable()
        ab_names = [b.getUserName() for b in autoblocks]
        for block_name in ab_names:
            block = blocks.getBlock(block_name)
            #nbh = jmri.NamedBeanHandle(block_name, block)
            hashBlocks.put(block, 4)        # 1 Unoccupied
        #sml.setBlocks(hashBlocks, sm_dest)
        if self.loglevel > 0:  print "hashBlocks", hashBlocks
        
        #prepare turnouts
        hashTurnouts = java.util.Hashtable()
        for route in routes_in_autoblocks:
            prev, curr, next = route
            ConnectivityUtil=jmri.jmrit.display.layoutEditor.ConnectivityUtil(self.layout_editor)
            list1 = ConnectivityUtil.getTurnoutList(curr, prev, next)
            if self.loglevel > 0:  print list1
            for l in list1:
                es = l.getExpectedState()  
                layout_turnout = l.getObject()
                turnout_name = layout_turnout.getTurnoutName()
                turnout = turnouts.getByUserName(turnout_name)
                nbh = jmri.NamedBeanHandle(turnout_name, turnout)
                
                turnout_state = es
                
                hashTurnouts.put(nbh, turnout_state)        # 1 closed 2 = Thrown
        #sml.setTurnouts(hashTurnouts, sm_dest)
        if self.loglevel > 0:  print "hashTurnouts",hashTurnouts
        
        #replace the signal logic
        SignalMastLogicManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        
        #get details of section so we can create a new one once the signal mast lofic has been replaced
        if self.loglevel > 0:  print "sm_dest", sm_dest.getUserName(), "sml", sml
        a_section = sml.getAssociatedSection(sm_dest)
        if self.loglevel > 0:  print "a_section", a_section.getUserName()
        block_list = a_section.getBlockList()
        forward_entry_point_list = a_section.getForwardEntryPointList()
        if self.loglevel > 0:  print forward_entry_point_list
        reverse_entry_point_list = a_section.getReverseEntryPointList()
        forward_sensor_name = a_section.getForwardBlockingSensorName()
        reverse_sensor_name = a_section.getReverseBlockingSensorName()
        forward_stopping_sensor_name = a_section.getForwardStoppingSensorName()
        reverse_stopping_sensor_name = a_section.getReverseStoppingSensorName()
        #sml.removeDestination(sm_dest)
        #SignalMastLogicManager.removeSignalMastLogic(sml,sm_dest)
        
        #replace section
        SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
        section_username = a_section.getUserName()
        SectionManager.deleteSection(a_section)
        a_section = SectionManager.createNewSection(section_username)
       
        for block in block_list:
            a_section.addBlock(block)
        #set entry point list
        for fep in forward_entry_point_list:
            a_section.addToForwardList(fep)
        for rep in reverse_entry_point_list:
            a_section.addToForwardList(rep)
        a_section.setForwardBlockingSensorName(forward_sensor_name)
        a_section.setReverseBlockingSensorName(forward_sensor_name)
        a_section.setForwardStoppingSensorName(forward_stopping_sensor_name)
        a_section.setReverseStoppingSensorName(reverse_stopping_sensor_name)

        # if self.loglevel > 0:  print "about to add smdest", sm_dest, sm_dest.getUserName()
        # if self.loglevel > 0:  print  "sml", sml, sml.getUserName()
        # #sml.setDestinationMast(sm_dest)
        # sml.setEnabled(sm_dest)
        
        #set signallogic
        list = sml.getDestinationList()
        if self.loglevel > 0:  print "destlist " , list , [ l.getUserName() for l in list]
        #
        sml.setBlocks(hashBlocks, sm_dest)
        if self.loglevel > 0:  print "hashBlocks", hashBlocks
        sml.setTurnouts(hashTurnouts, sm_dest)
        if self.loglevel > 0:  print "hashTurnouts",hashTurnouts
        sml.useLayoutEditor(False, sm_dest)      # we need the blocks to be set, otherwise the transits won't work
        use_turnout_details = False             # the turnout details come from the items we have set above
        use_block_details = False               # the block details come from the items we have set above
        sml.useLayoutEditorDetails(use_turnout_details, use_block_details, sm_dest)
        sml.setAssociatedSection(a_section, sm_dest)
        #sml.initialise(sm_dest)
        #sml.firePropertyChange("newDestination", None, sm_dest_name); # to show new SML in underlying table  // NOI18N
        


        #self.run_update()
        
    def run_update(self):
        #from javax.swing import JFrame, JButton

        frame = JFrame("Hello")
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.setLocation(100,100)
        frame.setSize(300,200)

        def updatePressed(event):
           jmri.jmrit.signalling.SignallingPanel.updatePressed(ActionEvent)(e)

        btn = JButton("Add", actionPerformed = updatePressed)
        frame.add(btn)

        frame.setVisible(True)
        btn.doClick
        frame.dispose()
