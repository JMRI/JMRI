# Script to automatically Generate a Network graph of a layout
# Used to create a path between stations on layout
#
# Author: Bill Fitch, copyright 2020
# Part of the JMRI distribution

import sys
import jmri
import os

my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
sys.path.append(my_path_to_jars) # add the jar to your path

#Jgrapht reference
#http://www.java2s.com/Code/Jar/j/Downloadjgraphtjar.htm

from org.jgrapht.graph import DefaultEdge
from org.jgrapht.graph import DefaultWeightedEdge
from org.jgrapht.graph import DirectedWeightedMultigraph



class StationGraph(jmri.jmrit.automat.AbstractAutomaton):
    def __init__(self):
        self.g = DirectedWeightedMultigraph(DefaultWeightedEdge)
        self.g_stopping = DirectedWeightedMultigraph(DefaultWeightedEdge)
        self.g_express = DirectedWeightedMultigraph(DefaultWeightedEdge)
        self.station_block_list = []
        self.station_blk_list = []
        self.dict_path_stopping = {}
        self.dict_path_express = {}
        self.dict_path_name_stopping= {}
        self.dict_path_name_express= {}
        self.logLevel = 0
        if self.logLevel > 0: print "graph __init__"
        self.setup_station_block_list()
        if self.logLevel > 0: print "__init__2"
        self.setup_graph_vertices()
        if self.logLevel > 0: print "__init__3"
        self.get_list_inhibited_blocks()
        self.setup_graph_edges()
        if self.logLevel > 0: print "finished graph init"#

    # **************************************************
    # Set up station block list either from manual list or from Block Table
    # **************************************************        
        
    def setup_station_block_list(self):
        BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
        if self.logLevel > 0: print "Block", BlockManager.getNamedBeanSet()
        for block in BlockManager.getNamedBeanSet():
            #blocks with the word stop in the comment are stations
            comment = block.getComment()
            if comment != None:
                if "stop" in comment.lower():
                    station_block_name = block.getUserName()
                    self.station_block_list.append(station_block_name)
                    self.station_blk_list.append(self.get_layout_block(station_block_name))
        if self.logLevel > 0: print 'self.station_block_list' , self.station_block_list
        
    def get_layout_block(self, block_name):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        layoutBlock = LayoutBlockManager.getLayoutBlock(block_name)
        return layoutBlock        
        
    def setup_graph_vertices(self):

        for station_block_name in self.station_block_list:
            if self.logLevel > 0: print "station_block_name",station_block_name
            lblk = self.get_layout_block(station_block_name)
            if self.logLevel > 0: print "lblk",lblk
            self.g.addVertex(lblk)
            self.g_express.addVertex(station_block_name)
            self.g_stopping.addVertex(station_block_name)
        if self.logLevel > 0: print 'end setup_graph_vertices"

    def setup_graph_edges(self):
        if self.logLevel > 0: print "*****************************"
        if self.logLevel > 0: print "****setup_graph_edges********"
        if self.logLevel > 0: print "*****************************"
        LayoutBlockConnectivityTools=jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools()
        index = 0
        for station in self.station_block_list:
            if self.logLevel > 0: print "*********************"
            if self.logLevel > 0: print "station = " ,station
            if self.logLevel > 0: print "*********************"
            station_block = self.get_layout_block(station)
            station_block_name = station_block.getUserName()
            if self.logLevel > 0: print "station_block_name",station_block_name
            if self.logLevel > 0: print "no neighbors", station_block.getNumberOfNeighbours()
            for i in range(station_block.getNumberOfNeighbours()):
                if self.logLevel > 0: print "+++++++++++++++++++++++++"
                neighbor_name = station_block.getNeighbourAtIndex(i).getDisplayName()
                if self.logLevel > 0: print "neighbor_name",neighbor_name
                other_stations = [block for block in self.station_block_list if block not in [station]]
                if self.logLevel > 0: print "other_stations",other_stations
                for destination in other_stations:
                    index +=1
                    if self.logLevel > 0: print "--------------------------"
                    if self.logLevel > 0: print "destination",destination
                    if self.logLevel > 0: print "--------------------------"
                    sourceLayoutBlock = station_block
                    destinationLayoutBlock  = self.get_layout_block(destination)
                    protectingLayoutBlock = self.get_layout_block(neighbor_name)
                    validateOnly = True
                    pathMethod = LayoutBlockConnectivityTools.Routing.NONE
                    path = []
                    if self.logLevel > 0: print "got here 1"
                    try:
                        if self.logLevel > 0: print "got here 2a"
                        #sourceBlock = sourceLayoutBlock.getBlock()
                        #destinationBlock = destinationLayoutBlock.getBlock()
                        #a = sourceLayoutBlock.getThroughPathIndex(sourceBlock, destinationBlock)
                        if self.logLevel > 0: print "getThroughPathIndex", a
                        if self.logLevel > 0: print "printValidThroughPaths()",  printValidThroughPaths()
                        path = LayoutBlockConnectivityTools.getLayoutBlocks(sourceLayoutBlock, destinationLayoutBlock, protectingLayoutBlock, validateOnly, pathMethod)
                        if self.logLevel > 0: print path
                    except jmri.JmriException as e:
                        if self.logLevel > 0: print "got here 2b"
                        if self.logLevel > 0: print (e)
                        continue
                    finally:
                        if self.logLevel > 0: print "got here 3"
                        if path != [] and path != None :
                            if self.logLevel > 0: print "got here 4a"
                            #add an edge for all paths to form the express train graph
                            path_name = [str(x.getUserName()) for x in path]
                            path_weight = [x.getBlock().getLengthMm() for x in path]
                            pweight = sum(path_weight) + 1  # add 1 so paths of equal length will have a smaller weiht if the train stops less
                            path_inhibited = self.is_path_inhibited(path_name)
                            if path_inhibited: pweight = pweight * 1000
                            edge = le()     # le = LabelledEdge() set up outside CreateGraph.py
                            #edge.setItem(index = index)
                            edge.setItem(path = path)
                            edge.setItem(path_name = path_name)
                            #edge.setItem(neighbor_name = neighbor_name)
                            if self.logLevel > 0: print edge.to_string()
                            if self.logLevel > 0: print "adding edge ", station_block_name, destination
                            if self.logLevel > 0: print edge.to_string()
                            if self.logLevel > 0: print "got here 4a2"
                            if self.Sufficient_signal_masts_in_edge(edge):
                                self.g_express.addEdge(station_block_name,destination, edge)
                                self.g_express.setEdgeWeight(edge, pweight)
                                #print "edge", edge , "pweight", pweight
                                if self.logLevel > 0: print "got here 4a"
                                if self.logLevel > 0: print edge.to_string()
                                edge.setItem(index = index)
                                #edge.setItem(path = path)
                                #edge.setItem(path_name = path_name)
                                edge.setItem(neighbor_name = neighbor_name)
                                LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
                                firstLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[0])
                                first_block_name = firstLayoutBlock.getUserName()
                                edge.setItem(first_block_name = first_block_name)
                                secondLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[1])
                                second_block_name = secondLayoutBlock.getUserName()
                                edge.setItem(second_block_name = second_block_name)
                                lastLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[-1])
                                last_block_name = lastLayoutBlock.getUserName()
                                edge.setItem(last_block_name = last_block_name)
                                penultimateLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[-2])
                                penultimate_block_name = penultimateLayoutBlock.getUserName()
                                edge.setItem(penultimate_block_name = penultimate_block_name)
                                #edge.setItem(path_weight = path_weight)

                                if self.logLevel > 0: print "path weight", path_weight, "pweight",pweight
                                LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
                                penultimateLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[-2])
                                penultimate_block_name = penultimateLayoutBlock.getUserName()
                                edge.setItem(penultimate_block_name = penultimate_block_name)
                                if self.logLevel > 0: print edge.to_string()
                                if self.logLevel > 0: print "got here 4a3"
                                if self.logLevel > 0: print "self.g_express",self.g_express

                                #add only edges for paths that do not go through a station for stopping train graph
                                through_stations = [str(block_name) for block_name in self.station_block_list if block_name not in [station,destination]]
                                # if self.logLevel > 0: print "path", path_name
                                # if self.logLevel > 0: print "all through_stations not at start and end", through_stations
                                # if self.logLevel > 0: print "through stations check" ,[item in through_stations for item in path_name]
                                # if self.logLevel > 0: print "through stations in path" ,[ item  for item in path_name  if item in through_stations]
                                path_blocks_are_through_stations = [item in through_stations for item in path_name]
                                if self.logLevel > 0: print "any through stations" ,any(path_blocks_are_through_stations)
                                if self.logLevel > 0: print "not any through stations" , not any(path_blocks_are_through_stations)
                                if not any(path_blocks_are_through_stations):
                                    #add to stopping graph
                                    if self.logLevel > 0: print "adding to stopping graph"
                                    edge = le()     # l = LabelledEdge() set up outside class
                                    self.g_stopping.addEdge(station_block_name,destination,edge)
                                    self.g_stopping.setEdgeWeight(edge, pweight)
                                    path_name = [str(x.getUserName()) for x in path]
                                    edge.setItem(index = index)
                                    edge.setItem(path = path)
                                    edge.setItem(path_name = path_name)
                                    edge.setItem(neighbor_name = neighbor_name)
                                    firstLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[0])
                                    first_block_name = firstLayoutBlock.getUserName()
                                    edge.setItem(first_block_name = first_block_name)
                                    secondLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[1])
                                    second_block_name = secondLayoutBlock.getUserName()
                                    edge.setItem(second_block_name = second_block_name)
                                    lastLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[-1])
                                    last_block_name = lastLayoutBlock.getUserName()
                                    edge.setItem(last_block_name = last_block_name)
                                    penultimateLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[-2])
                                    penultimate_block_name = penultimateLayoutBlock.getUserName()
                                    edge.setItem(penultimate_block_name = penultimate_block_name)
                                    if self.logLevel > 0: print edge.to_string()
                                else:
                                    if self.logLevel > 0: print "not adding to stopping graph as insufficient masts", "edge " , edge.path_name()
                            else:
                                if self.logLevel > 0: print "not adding to stopping graph"
                                pass
                            if self.logLevel > 0: print "*********************************"    
                        else:
                            if self.logLevel > 0: print "got here 4b"
                            pass
                    if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
                    if self.logLevel > 0: print "&& graph up to now &&"
                    if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
                    #for e in self.g_express.edgeSet():
                        #print e.to_string()
                    if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
                    if self.logLevel > 0: print "&& end graph up to now &&"
                    if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&
                    
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&& express"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"        

        for e in self.g_express.edgeSet():
            if self.logLevel > 0: print (self.g_express.getEdgeSource(e) + " --> " + self.g_express.getEdgeTarget(e))
        
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
        if self.logLevel > 0: print "&& stopping"
        if self.logLevel > 0: print "&&&&&&&&&&&&&&&&&&&&&" 
                    
        for e in self.g_stopping.edgeSet():
            if self.logLevel > 0: print (self.g_stopping.getEdgeSource(e) + " --> " + self.g_stopping.getEdgeTarget(e))                 

        #set the indicators for the train to reverse
        for e in self.g_stopping.edgeSet():
            
            if self.logLevel > 0: print "edge = ",e

            try:
                if self.logLevel > 0: print e, "Target", e.getTarget()
                if self.logLevel > 0: print e, "Source", e.getSource()
                opposite_direction_edge = self.g_stopping.getEdge(e.getTarget(),e.getSource())
                opposite_direction_neighbor_name = opposite_direction_edge.getItem("neighbor_name")
                e.setItem(opposite_direction_neighbor_name=opposite_direction_neighbor_name)
                if self.logLevel > 0: print "set opposite direction neighbor_name for edge" , e.to_string()
            except:
                if self.logLevel > 0: print "unable to set opposite direction neighbor_name for edge" , e.to_string()
                continue

    def get_list_inhibited_blocks(self):
        list_inhibited_blocks = self.read_list()
        self.list_inhibited_blocks = list_inhibited_blocks
        # print "list_inhibited_blocks", list_inhibited_blocks
    def is_path_inhibited(self, path):
        existing = self.list_inhibited_blocks
        #print "existing", existing
        if existing != None:
            for check in existing:
                #print "check", check
                if self.sublist_in_list(check, path):
                    return True
        return False

    def sublist_in_list(self,sublist, test_list):
        res = False
        #print "(len(test_list) - len(sublist) + 1)", (len(test_list) - len(sublist) + 1)
        for idx in range(len(test_list) - len(sublist) + 1):
            #print "test_list[idx : idx + len(sublist)]", test_list[idx : idx + len(sublist)], "sublist", sublist
            if test_list[idx : idx + len(sublist)] == sublist:
                res = True
                break
        #print "test_list", test_list, "sublist", sublist, "in_list", res
        return res

    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "blockDirections"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator
    def write_list(self, a_list):
        # store list in binary file so 'wb' mode
        file = self.directory() + "blockDirections.txt"
        if self.logLevel > 0: print "block_info" , a_list
        if self.logLevel > 0: print "file"  +file
        with open(file, 'wb') as fp:
            for items in a_list:
                i = 0
                for item in items:
                    fp.write('%s' %item)
                    if i == 0: fp.write(",")
                    i+=1
                fp.write('\n')
                    #fp.write('\n'.join(item))
                    #fp.write(items)



    # Read list to memory
    def read_list(self):
        # for reading also binary mode is important
        file = self.directory() + "blockDirections.txt"
        n_list = []
        try:
            with open(file, 'rb') as fp:
                for line in fp:
                    x = line[:-1]
                    y = x.split(",")
                    n_list.append(y)
            return n_list
        except:
            return ["",""]



    def Sufficient_signal_masts_in_edge(self, e):
        global g

        EditorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
        layoutPanels = []
        for panel in EditorManager.getList():
            if type(panel) == jmri.jmrit.display.layoutEditor.LayoutEditor:
                layoutPanels.append(panel)

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
            return False
        else:
            return True


                          

# r.Setup_station_block_list

# /**
 # * Custom edge class labeled with relationship type.
 # */
# @example:edgeclass:begin
class LabelledEdge(DefaultWeightedEdge):

    #label = ""
    logLevel = 0

    # /**
     # * Constructs a relationship edge
     # *
     # * @param label the label of the new edge.
     # * 
     # */
    def __init__(self):

        self.dict = {}
        #self.label = label
        
        # for key, value in kwargs.items():
            # self.dict[key]=value

    # /**
     # * Gets the label associated with this edge.
     # *
     # * @return edge label
     # */
    def setItem(self, **kwargs):
        for key, value in kwargs.items():
            self.dict[key]=value
        
    def getItem(self, item):
        if self.logLevel > 0: print self.dict
        if self.logLevel > 0: print "item = ", item
        return self.dict[item]
        
    def getTarget(self):
        line = self.toString()
        line = line.lstrip("( ").rstrip(" )").split(" : ")
        if self.logLevel > 0: print "Target line", line
        target = str(line[1])
        if self.logLevel > 0: print "Target =", target
        return target 
        
    def getSource(self):
        line = self.toString()
        line = line.lstrip("( ").rstrip(" )").split(" : ")
        if self.logLevel > 0: print "Source line", line
        source = str(line[0])
        if self.logLevel > 0: print "Source =", source
        return source
    
    def to_string_one_line(self):
        item_list = ""
        for item, value in self.dict.items():
            item_list = item_list + str(item) + " = " + str(value) +" : "
            if self.logLevel > 0: print "item_list", item_list
        if self.logLevel > 0: print item_list    
     
        return "*****to_string*****(" + self.getSource() + " : " + self.getTarget()  + " : " + item_list.rstrip(": ") + ")"    

    def to_string(self):
        item_list = "\n"
        for item, value in self.dict.items():
            item_list = item_list + str(item) + " = " + str(value) +" :\n "
            if self.logLevel > 0: print "item_list", item_list
        if self.logLevel > 0: print item_list    
    
        return "*****to_string*****(\n" + self.getSource() + " : " + self.getTarget()  + " : " + item_list.rstrip(": ") + ")*****to_string end*******"
        
