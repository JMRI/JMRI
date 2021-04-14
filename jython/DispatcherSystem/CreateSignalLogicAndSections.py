# Script to automatically build Signal Logic and Sections
#
# Author: Bill Fitch, copyright 2020
# Part of the JMRI distribution

#
# NOTE: Currently the automatic method to build signal logic 
# does not work in some circumstances. This script:
# - Deletes all existing Transits, Sections and Signal Logic
# - Runs the standard auto signal logic builder
# - allows the option to replace auto signal logic as needed
# - builds Sections
#
# To correct the standard auto signal logic builder it uses 
# other standard code found elsewhere in JMRI
#
# The user can 
# - use standard signal logic
# - replace signal logic for a particular signal mast
# - replace all signal logic for routes that pass through a turnout 
#


import jmri
from javax.swing import JButton, JFrame,JPanel,BoxLayout,Box,JScrollPane, JOptionPane, AbstractAction
#import javax.swing.AbstractAction

class Update_Signal_Logic():

    loglevel = 0
    
    # order of calling
    # - create_sections()
    # - create_transits()
    # - Update_logic()
    
    def customQuestionMessage3(self, msg, title, opt1, opt2, opt3):
        self.CLOSED_OPTION = False
        options = [opt1, opt2, opt3]
        s = JOptionPane.showOptionDialog(None,
        msg,
        title,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        None,
        options,
        options[0])
        if s == JOptionPane.CLOSED_OPTION:
            s1 = "cancelled"
        elif s == JOptionPane.YES_OPTION:
            s1 = opt1
        elif s == JOptionPane.NO_OPTION:
            s1 = opt2
        else:
            s1 = opt3
        return s1 
    
    def create_autologic_and_sections(self):
        self.msg = "About to create all signal logic and sections\nrequired for dispatcher operation"
        self.msg = self.msg + "\n***********************\n Do you wish to continue\n***********************"
        myAnswer = JOptionPane.showConfirmDialog(None, self.msg)
        if myAnswer == JOptionPane.YES_OPTION:
            self.delete_transits()
            self.delete_sections()
            self.delete_signal_mast_logic()
            self.set_layout_editor()
            self.create_auto_signal_mast_logic()
            self.create_sections()
            return True
        elif myAnswer == JOptionPane.NO_OPTION:
            msg = 'Stopping'
            JOptionPane.showMessageDialog(None, 'Stopping', "Stopping" , JOptionPane.WARNING_MESSAGE)
            return False
        elif myAnswer == JOptionPane.CANCEL_OPTION:
            msg = 'Stopping'
            JOptionPane.showMessageDialog(None, 'Stopping', "Stopping", JOptionPane.WARNING_MESSAGE)
            return False
        elif myAnswer == JOptionPane.CLOSED_OPTION:
            msg = 'Stopping'
            JOptionPane.showMessageDialog(None, 'Stopping', "Stopping", JOptionPane.WARNING_MESSAGE)
            return False

        
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

    
    def update_auto_signal_mast_logic2(self):
    
        print ""
        print "******************************************"
        print "********* update_auto_signal_mast_logic2 *"
        print "******************************************"
        
    
        self.delete_signal_mast_logic()
        
        # automaticallyDiscoverSignallingPairs()
        lbm = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        smlm = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)

        validPaths1 = lbm.getLayoutBlockConnectivityTools().discoverValidBeanPairs(None, jmri.SignalMast, lbm.getLayoutBlockConnectivityTools().Routing.MASTTOMAST)
        validPaths = java.util.HashMap(validPaths1) 
        print validPaths.size()
        for e in validPaths.entrySet():
            print "e",e
            #print [f.getUserName() for f in e]
            print "getkey", e.getKey()
            key = e.getKey()
            sourceMast = key
            sml = smlm.getSignalMastLogic(key)
            if (sml == None) :
                sml = smlm.newSignalMastLogic(key)
            
            validDestMast = validPaths.get(key)
            print "validDestMast", validDestMast
            for nb in validDestMast :
                if (sml.isDestinationValid(nb)) :
                    print "sml.isDestinationValid", nb
                else:
                    print "not sml.isDestinationValid", nb
                    destMast = nb
                    self.setupLayoutEditorDetails(key, nb,sml);
                    sml.useLayoutEditor(True, nb);
                    
                    # except (ex) :
                        # # #log.debug("we shouldn't get an exception here!");
                        # # print(ex.getLocalizedMessage(), ex)
                        # pass
                
                    
            
            # # if (sml.getDestinationList().size() == 1 and sml.getAutoTurnouts(sml.getDestinationList().get(0)).isEmpty()) :
                # # key.setProperty("intermediateSignal", true);
            
   
    def setupLayoutEditorDetails(self, SourceMast, destination,sml):
    
        # def setupLayoutEditorDetails(self, sourcemast, destination):
    
        lbm = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        em = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
        layout = em.getAll(jmri.jmrit.display.layoutEditor.LayoutEditor);
        editor = jmri.jmrit.display.layoutEditor.LayoutEditor
        for editor in layout:
            facingBlock = lbm.getFacingBlockByNamedBean(SourceMast, editor);
            protectingBlocks = lbm.getProtectingBlocksByNamedBean(SourceMast, editor)
            destinationBlock = lbm.getFacingBlockByNamedBean(destination, editor)
            remoteProtectingBlock = lbm.getProtectedBlockByNamedBean(destination, editor)
            protectingBlock = None
            
            #print "facingBlock", facingBlock,facingBlock.getUserName()
            #print "protectingBlocks", protectingBlocks, [pb.getUserName() for pb in protectingBlocks]
            #print "destinationBlock", destinationBlock, destinationBlock.getUserName()
            if remoteProtectingBlock != None: 
                rp_name = remoteProtectingBlock.getUserName() 
            else: 
                rp_name = None
            #print "remoteProtectingBlock", remoteProtectingBlock, rp_name
            
            pBlkNames = ""
            lBlksNamesBuf = java.lang.StringBuffer()
            
            for pBlk in protectingBlocks:
                if remoteProtectingBlock == None:
                    print "pBlkNames == None"
                    valid= False
                    
                else:
                    valid = lbm.getLayoutBlockConnectivityTools().checkValidDest(facingBlock, pBlk, destinationBlock, remoteProtectingBlock, jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.Routing.MASTTOMAST)
                #print "valid = ",valid
                if valid:
                    pBlkNames = pBlkNames + pBlk.getDisplayName() + " (" + str(valid) + "), ";
                    #print "remoteProtectingBlock != None"
                    #print "facingBlock",facingBlock, "pBlk",pBlk, "destinationBlock", destinationBlock,"remoteProtectingBlock",remoteProtectingBlock
                    #print  lbm.getLayoutBlockConnectivityTools().Routing.MASTTOMAST
                    #valid = lbm.getLayoutBlockConnectivityTools().checkValidDest(facingBlock, pBlk, destinationBlock,remoteProtectingBlock, lbm.getLayoutBlockConnectivityTools().Routing.MASTTOMAST)
                #if valid:
                    lblks = lbm.getLayoutBlockConnectivityTools().getLayoutBlocks(facingBlock, destinationBlock, pBlk, True, jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.Routing.MASTTOMAST);
                    #protectingBlock = jmri.jmrit.display.layoutEditor.LayoutBlock(pBlk.getSystemName(),pBlk.getUserName());
                    protectingBlock = pBlk
                                
                    for lBlk in lblks :
                        lBlksNamesBuf.append(" ");
                        lBlksNamesBuf.append(lBlk.getDisplayName());
                
                lBlksNames = lBlksNamesBuf; 
                print "************************"

            if (destinationBlock != None and protectingBlock != None and facingBlock != None) :
                # smlc = jmri.implementation.DefaultSignalMastLogic(SourceMast)
                # instance = smlc.DestinationMast(smlc, SourceMast)
                # instance.setAutoMasts(None, True)
                lblks = lbm.getLayoutBlockConnectivityTools().getLayoutBlocks(facingBlock, destinationBlock, protectingBlock, True, jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.Routing.MASTTOMAST);
                # LinkedHashMap<Block, Integer> 
                block = self.setupLayoutEditorTurnoutDetails(lblks, sml, SourceMast, destination, destinationBlock, editor);
                
                #sml.setAutoBlocks(block)
                #sml.setupAutoSignalMast(None, False);
                #smlinitialise();

    def get_routes2(self, LayoutBlock):

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
            
    def getLayoutBlockAfterDestMast(self, signal_mast, editor):#, prev_layout_block, last_layout_block):
    
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        layoutBlock = LayoutBlockManager.getProtectedBlockByMast(signal_mast,editor)
        print "LayoutBlock" , layoutBlock
        return layoutBlock
    
        # list = self.get_routes(last_layout_block)
        # print "list = " , list
    
        # #list consists of prev_curr_next
        # LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        # for l in list:
            # [prev, curr, next] = l
            # print "prev", prev, "curr", curr, "next" , next, "last_layout_block", last_layout_block
            # if prev == prev_layout_block.getBlock() and next = nextBlock:
                # return LayoutBlockManager.getLayoutBlock(next)      #returns the next layout blocl
    
        # #SignalMastLogicManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
        # #sml_dest = SignalMastLogicManager.getSignalMastLogic(sm_dest)
        # sml_dest = sml
        # print "sml_dest", sml_dest
        # if sml_dest != None:
            # sm_dest_list = sml_dest.getDestinationList()
            # print "sm_dest_list", sm_dest_list
            # for sm_next in sm_dest_list:
                # if self.loglevel > 0:  print "sm_dest", sm_dest.getUserName()
                # # get blocks in path
                # autoblocks = sml_dest.getAutoBlocksBetweenMasts(sm_next)
                # if autoblocks != []:
                    # if self.loglevel > 0:  print "autoblocks = " , autoblocks
                    # return autoblocks[0]            
        

    #LinkedHashMap<Block, Integer> 
    def setupLayoutEditorTurnoutDetails(self, lblks, sml, SourceMast, destination, destLayoutBlock, editor) :
        print "**********************************"
        print "****** Setting Turnout Details ***", SourceMast.getUserName(), destination.getUserName(), destLayoutBlock.getUserName()
        print "**********************************"
        print ""
        
        if SourceMast.getUserName() != "Board2toPT11" or  destination.getUserName() !=  "PT41toGoods":
            print "Not required mast set"
            return
            
        sm_dest = destination
        # last_block = destLayoutBlock
        # print "destBlock",destBlock
        
        # LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        #destLayoutBlock = self.getBlockAfterDestMast2(sourceLayoutBlock, destLayoutBlock)
        #print "destLayoutBlock", destLayoutBlock.getUserName()
        #print "before", " lblks.size()", lblks.size(), lblks
        #lblks.append(destLayoutBlock)
        print "after", " lblks.size()", lblks.size(),lblks
        #ConnectivityUtil 
        connection= None
        #List<LayoutTrackExpectedState<LayoutTurnout>> 
        turnoutList = None
        turnoutSettings = java.util.Hashtable()
        block_hash = java.util.LinkedHashMap();
        #for (int i = 0; i < lblks.size(); i++) {
        print "lblks", [lblks.get(i).getBlock().getUserName() for i in range (lblks.size())]
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        for i in range(lblks.size()):
            # if (log.isDebugEnabled()) {
                # log.debug(lblks.get(i).getDisplayName());
            # }
            block_hash.put(lblks.get(i).getBlock(), jmri.Block.UNOCCUPIED)
            destLayoutBlock = None
            if i > 0 :
                nxtBlk = i + 1;
                preBlk = i - 1;
                if i == lblks.size() - 1 :
                    nxtBlk = i;
                    # BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
                    # destBlock = BlockManager.getBlock(destination)
                    # sm_dest = sml
                    # last_block = i
                    prevLayoutBlock = lblks.get(i-1)
                    currLayoutBlock = lblks.get(i)
                    destLayoutBlock = self.getLayoutBlockAfterDestMast(destination, editor) #(prevLayoutBlock, currLayoutBlock)
                    
                    print "currLayoutBlock", currLayoutBlock
                    print "destLayoutBlock", destLayoutBlock
                    
                    # autoblocks = sml.getAutoBlocksBetweenMasts(destination)
                    # print "autoblocks", autoblocks
                    # destLayoutBlock = autoblocks[-1]
                    # print "destLayoutBlock", destLayoutBlock.getUserName()
                    #nxtBlk = destLayoutBlock;
                
                #We use the best connectivity for the current block, ";";
                connection = jmri.jmrit.display.layoutEditor.ConnectivityUtil(lblks.get(i).getMaxConnectedPanel());
                
                if i == lblks.size() - 1 :
                    print "getTurnoutList",lblks.get(i).getBlock().getUserName(), ";",lblks.get(preBlk).getBlock().getUserName(), ";", destLayoutBlock.getBlock().getUserName()
                    turnoutList = connection.getTurnoutList(lblks.get(i).getBlock(), lblks.get(preBlk).getBlock(), destLayoutBlock.getBlock());
                else:
                    print ",getTurnoutList",lblks.get(i).getBlock().getUserName(), ";",lblks.get(preBlk).getBlock().getUserName(), ";", lblks.get(nxtBlk).getBlock().getUserName()
                    turnoutList = connection.getTurnoutList(lblks.get(i).getBlock(), lblks.get(preBlk).getBlock(), lblks.get(nxtBlk).getBlock());
                print "turnoutList", [[t , t.getExpectedState()] for t in turnoutList]
                #for (int x = 0; x < turnoutList.size(); x++) {
                for x in range(turnoutList.size()):
                    #LayoutTurnout
                    lt = turnoutList.get(x).getObject();
                    print "lt = " , lt
                    if isinstance(lt, jmri.jmrit.display.layoutEditor.LayoutSlip):
                        ls = jmri.jmrit.display.layoutEditor.LayoutSlip(lt)
                        slipState = turnoutList.get(x).getExpectedState();
                        taState = ls.getTurnoutState(slipState);
                        turnoutSettings.put(ls.getTurnout(), taState);
                        tbState = ls.getTurnoutBState(slipState);
                        turnoutSettings.put(ls.getTurnoutB(), tbState);
                        print "Layout Slip", "ls = " , ls, "slipState = " , slipState, "tbState = " , tbState
                    else :
                        t = lt.getTurnoutName();
                        turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(t);
                        print "Not Layout Slip", turnout
                        # if (True) :
                            # if (    (lt.getTurnoutType() == jmri.jmrit.display.layoutEditor.LayoutTurnout.TurnoutType.RH_TURNOUT or
                                     # lt.getTurnoutType() == jmri.jmrit.display.layoutEditor.LayoutTurnout.TurnoutType.LH_TURNOUT or
                                     # lt.getTurnoutType() == jmri.jmrit.display.layoutEditor.LayoutTurnout.TurnoutType.WYE_TURNOUT) 
                                    # and (!lt.getBlockName().equals(""))) :
                                # if self.logLevel > 1 : print("turnout in list is straight left/right wye");
                                # if self.logLevel > 1 : print("turnout block Name {}", lt.getBlockName());
                                # if self.logLevel > 1 : print("current {} - pre {}", lblks.get(i).getBlock().getDisplayName(), lblks.get(preBlk).getBlock().getDisplayName());
                                # if self.logLevel > 1 : print("A {}", lt.getConnectA());
                                # if self.logLevel > 1 : print("B {}", lt.getConnectB());
                                # if self.logLevel > 1 : print("C {}", lt.getConnectC());
                                # if self.logLevel > 1 : print("D {}", lt.getConnectD());
                            
                        
                        if (turnout != None ) :
                            turnoutSettings.put(turnout, turnoutList.get(x).getExpectedState());
                        
                        if (lt.getSecondTurnout() != None) :
                            turnoutSettings.put(lt.getSecondTurnout(), turnoutList.get(x).getExpectedState());
                        
                        # /* TODO: We could do with a more intelligent way to deal with double crossovers, other than
                            # just looking at the state of the other conflicting blocks, such as looking at Signalmasts
                            # that protect the other blocks and the settings of any other turnouts along the way.
                         # */
                        # if (lt.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER) {
                            # if (turnoutList.get(x).getExpectedState() == jmri.Turnout.THROWN) {
                                # if (lt.getLayoutBlock() == lblks.get(i) || lt.getLayoutBlockC() == lblks.get(i)) {
                                    # if (lt.getLayoutBlockB() != null) {
                                        # dblCrossoverAutoBlocks.add(lt.getLayoutBlockB().getBlock());
                                        # block.put(lt.getLayoutBlockB().getBlock(), Block.UNOCCUPIED);
                                    # }
                                    # if (lt.getLayoutBlockD() != null) {
                                        # dblCrossoverAutoBlocks.add(lt.getLayoutBlockD().getBlock());
                                        # block.put(lt.getLayoutBlockD().getBlock(), Block.UNOCCUPIED);
                                    # }
                                # } else if (lt.getLayoutBlockB() == lblks.get(i) || lt.getLayoutBlockD() == lblks.get(i)) {
                                    # if (lt.getLayoutBlock() != null) {
                                        # dblCrossoverAutoBlocks.add(lt.getLayoutBlock().getBlock());
                                        # block.put(lt.getLayoutBlock().getBlock(), Block.UNOCCUPIED);
                                    # }
                                    # if (lt.getLayoutBlockC() != null) {
                                        # dblCrossoverAutoBlocks.add(lt.getLayoutBlockC().getBlock());
                                        # block.put(lt.getLayoutBlockC().getBlock(), Block.UNOCCUPIED);
                                    # }
                                # }
                            # }
                        # }
                    
                
            # }
        # }
        #if (useLayoutEditorTurnouts) :
        if True:
            #sml.setAutoTurnouts(turnoutSettings);
            sml.setAutoTurnouts(turnoutSettings, destination)
            print "set turnouts for ", SourceMast.getUserName(), destination.getUserName()
            pass
        
        return block_hash;
    # } 

    # def setAutoTurnouts(self, turnouts) {
        # log.debug("{} called setAutoTurnouts with {}", destination.getDisplayName(), (turnouts != null ? "" + turnouts.size() + " turnouts in hash table" : "null hash table reference"));
        # if (autoTurnouts != None) {
            # keys = autoTurnouts.keys();
            # while (keys.hasMoreElements()) {
                # key = keys.nextElement();
                # #key.removePropertyChangeListener(propertyTurnoutListener);
            # }
            # //minimumBlockSpeed = 0;
        # }
        # destMastInit = false;
        # if (turnouts == null) {
            # this.autoTurnouts = new Hashtable<Turnout, Integer>(0);
        # } else {
            # this.autoTurnouts = turnouts;
        # }
        # firePropertyChange("autoturnouts", null, this.destination);
    #}    
        
    def update_auto_signal_mast_logic(self):
        
        # determine whether we wish to
        # - not replace the logic
        # - selectively replace the logic
        # - replace the logic
        opt1 = "use auto logic"
        opt2 = "replace logic for selected masts"
        opt3 = "replace all logic"
        
        title = "Replacing auto-logic"
        msg = "Provision is made to replace auto-logic with so-called manual-logic\n\nYou should use standard auto logic wherever possible\n\nWhere auto logic needs replacing do it selectively\nby marking in the appropriate signal mast comment field 'replace logic'\nand clicking 'replace logic for selected masts'\n\nUse 'replace all logic' with great caution"

        self.update_flag = self.customQuestionMessage3(msg, title, opt1, opt2, opt3)  #returns cancelled or opt1, opt2 opt3 text
        
        # if self.update_flag == opt3:
            # self.update_auto_signal_mast_logic2()
            # print "returning"
            # return
        
        if self.update_flag == opt2 or self.update_flag == opt3:

        
        
            #jmri.jmrit.display.layoutEditor.ConnectivityUtil.getLayoutTurnoutsThisBlock(Block)
            # 1) from all turnouts
            #     get all layout turnouts, and the block containing the layout turnout
            #jmri.jmrit.display.layoutEditor.LayoutEditorFindItems.findLayoutTurnoutByTurnoutName(String)
            # if have layout turnout
            # get layout turnout block
                # LayoutBlock.addAllThroughPaths()
                # LayoutBlock.getNumberOfThroughPaths()):
                # prev = LayoutBlock.getThroughPathSource(index)
                # curr = LayoutBlock.getBlock()
                # next = LayoutBlock.getThroughPathDestination(index)
                
                # ConnectivityUtil=jmri.jmrit.display.layoutEditor.ConnectivityUtil(self.layout_editor)
                # list1 = ConnectivityUtil.getTurnoutList(curr, prev, next)
                # for l in list1:
                    # es = l.getExpectedState() 
                
            # 2) from the through paths going through a block
            #    use 
            # => start block, block , end block  turnout settings??
            # => correspomding layout blocks
            
            
            #a we get the blocks either side of the turnout
            turnout_dict = {}
            turnout_dict1 = {}
            for turnout in turnouts.getNamedBeanSet():
                layout_turnout = jmri.jmrit.display.layoutEditor.LayoutEditorFindItems(self.layout_editor).findLayoutTurnoutByTurnoutName(turnout.getUserName())
                if layout_turnout != None:
                    layout_block = layout_turnout.getLayoutBlock()
                    # layout_block_B = layout_turnout.getLayoutBlockB()         #do not need B or C (path threough A goes through B or C
                    # layout_block_C = layout_turnout.getLayoutBlockC()
                    # layout_block_D = layout_turnout.getLayoutBlockD()         #need to check for D
                    routes = self.get_routes(layout_block)                      # get [prev,curr,next]
                    if self.loglevel > 0:  print "++++++*****"
                    if self.loglevel > 0:  print "routes" , routes
                    if self.loglevel > 0:  print "++++"
                    
                    routes_names = [[str(r.getUserName()) for r in route] for route in routes ]
                    if self.loglevel > 0:  print routes_names
                    if self.loglevel > 0:  print "@@@@@"
                    turnout_dict[turnout.getUserName()] = tuple(routes)

            # What we do:
            
            # we can get the required turnout orientations and store them in turnout_dict
            # we can get the required sml 
            
            # 0a) For each turnout we get the turnouts on the panel
            # 0b) we get the routes through the turnout
            # 0c) We store the results 
            #     turnout_dict[turnout.getUserName()] = tuple(routes) = [curr, prev, next]
            
            # 1a) For each sml we get the source and destination block
            # 1b) We get the blocks in the path between the source and destination autoblocks_orig using 
            #      sml = SignalMastLogicManager.getSignalMastLogic(sm_source)
            #      sml.getAutoBlocksBetweenMasts(sm_dest)
            # 1c) We append the facing block to the autoblocks
            # 1d) and add the last end block for uniqueness to autoblocks
            # 1e) We store the the results 
            #     signal_mast_dict[autoblocks] = (sm_source.getUserName(), sm_dest.getUserName(), sml, autoblocks_orig) 
            
            # 2a) We get: 
            #   ab_names the set of names of the blocks in auto_blocks for each source - destination pair
            #       
            #         note: sm = signal_mast_dict[autoblocks]
            #   route_names the set of blocks in a route through each turnout
            #         note: routes_in_autoblocks contains the blocks in route
            # 2b) if route_names is included in ab_names then we have a match and we can update the signal logic calling
            #    self.update_signal_logic(sm, routes_in_autoblocks
            
            # 3a) To update the signal logic we call update_signal_logic(self, signal_masts, routes_in_autoblocks):
            #   prepare the blocks hashtable from signal_masts
            #       note: sm_source_name, sm_dest_name, sml, autoblocks = signal_masts
            #       note signal_masts = 
            #   prepare the turnout hashtable from routes_in_autoblocks
            #       note turnouts = 
            # 3b) 
            
            
                

            #        % block turnout %
            #a         block turnout   block    turnout
            #b block   block turnout            autoblock
            
            #   % block turnout block %
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
                        # replace selectively (opt2) or all (opt3)
                        if self.update_flag == opt2:
                            sm_source_comment = sm_source.getComment()
                            sm_dest_comment = sm_dest.getComment()
                            if sm_source_comment == "replace logic" or sm_dest_comment == "replace logic":
                                signal_mast_dict[autoblocks] = (sm_source.getUserName(), sm_dest.getUserName(), sml, autoblocks_orig)
                        else:
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
                #if any of the turnouts are on the signal mast path we have stored the appropriate route in routes_in_autoblocks            
                if route_names_is_subset == True:
                    self.update_signal_logic(sm, routes_in_autoblocks)
                
        #dpg.killLabel()
        msg = "update of logic completed"
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

    # def update_auto_signal_mast_logic(self, signal_masts, routes_in_autoblocks):
    
        # # the code for this is either in 
    
        # if self.loglevel > 0: print "in update auto signal logic"
        # #preliminary
        # sm_source_name, sm_dest_name, sml, autoblocks = signal_masts
        # SignalMastManager = jmri.InstanceManager.getDefault(jmri.SignalMastManager)
        # sourceMast = SignalMastManager.getSignalMast(str(sm_source_name))
        # destMast = SignalMastManager.getSignalMast(str(sm_dest_name))
        
        # LayoutBlockManager = InstanceManager.getDefault(jmri.LayoutBlockManager)
        # valid = LayoutBlockManager.getLayoutBlockConnectivityTools().checkValidDest(sourceMast,
                        # destMast, LayoutBlockConnectivityTools.Routing.MASTTOMAST)
        # if not valid:
            # print "ErrorUnReachableDestination"
            
        # sml.allowAutoMaticSignalMastGeneration(True, destMast);
        

            
    def update_signal_logic(self, signal_masts, routes_in_autoblocks):
    
        # signal_masts
        
        # routes_in_autoblocks
    
        if self.loglevel > 0: print "in update signal logic"
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
        hashTurnouts1 = java.util.Hashtable()
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
                
                #nbh1 = jmri.NamedBeanHandle(turnout_name, turnout)
                #nbh = jmri.Turnout(nbh1)
                turnout_state = es
                
                hashTurnouts1.put(turnout, turnout_state)        # 1 closed 2 = 
                
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
        #sml.setBlocks(hashBlocks, sm_dest)
        if self.loglevel > 0:  print "hashBlocks", hashBlocks
        sml.setTurnouts(hashTurnouts, sm_dest)
        
        if self.loglevel > 0:  print "hashTurnouts",hashTurnouts
        sml.useLayoutEditor(True, sm_dest)      # we need the blocks to be set, otherwise the transits won't work
        use_turnout_details = False             # the turnout details come from the items we have set above
        use_block_details = True               # the block details come from the items we have set above
        sml.useLayoutEditorDetails(use_turnout_details, use_block_details, sm_dest)
        #sml.setAutoTurnouts(hashTurnouts1, sm_dest)
        #sml.setAutoBlocks(hashBlocks, sm_dest)
        sml.setAssociatedSection(a_section, sm_dest)
        #sml.initialise(sm_dest)
        #sml.firePropertyChange("newDestination", None, sm_dest_name); # to show new SML in underlying table  // NOI18N
        


        #self.run_update()
        
    def useLayoutEditorDetails(self, turnouts, blocks, dest):
        
        pass
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
