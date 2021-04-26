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

    g = DirectedWeightedMultigraph(DefaultWeightedEdge)
    g_stopping = DirectedWeightedMultigraph(DefaultWeightedEdge)
    g_express = DirectedWeightedMultigraph(DefaultWeightedEdge)
    
    station_block_list = []
    station_blk_list = []
    dict_path_stopping = {}
    dict_path_express = {}
    dict_path_name_stopping= {}
    dict_path_name_express= {}
    
    logLevel = 0

    def __init__(self):
        if self.logLevel > 0: print "graph __init__"
        self.setup_station_block_list()
        if self.logLevel > 0: print "__init__2"
        self.setup_graph_vertices()
        if self.logLevel > 0: print "__init__3"
        self.setup_graph_edges()
        if self.logLevel > 0: print "finished graph init"

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
        if self.logLevel > 0: print 'PPPPPPPPPPP'
        
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
                        if path != []:
                            if self.logLevel > 0: print "got here 4a"
                            #add an edge for all paths to form the express train graph
                            path_name = [str(x.getUserName()) for x in path]
                            path_weight = [x.getBlock().getLengthMm() for x in path]
                            pweight = sum(path_weight) + 1  # add 1 so paths of equal length will have a maller weiht if the train stops less
                            edge = le()     # le = LabelledEdge() set up outside CreateGraph.py
                            if self.logLevel > 0: print edge.to_string()
                            if self.logLevel > 0: print "adding edge ", station_block_name, destination
                            if self.logLevel > 0: print edge.to_string()
                            if self.logLevel > 0: print "got here 4a2"
                            self.g_express.addEdge(station_block_name,destination, edge)
                            self.g_express.setEdgeWeight(edge, pweight)
                            if self.logLevel > 0: print "got here 4a"
                            if self.logLevel > 0: print edge.to_string()
                            edge.setItem(index = index)
                            edge.setItem(path = path)
                            edge.setItem(path_name = path_name)
                            edge.setItem(neighbor_name = neighbor_name)
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
                            if self.logLevel > 0: print "a","b",a,b
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
                                penultimateLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[-2])
                                penultimate_block_name = penultimateLayoutBlock.getUserName()
                                edge.setItem(penultimate_block_name = penultimate_block_name)
                                if self.logLevel > 0: print edge.to_string()
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
    
        #return self.toString() + " : " + item_list 
        return "*****to_string*****(" + self.getSource() + " : " + self.getTarget()  + " : " + item_list.rstrip(": ") + ")"    

    def to_string(self):
        item_list = "\n"
        for item, value in self.dict.items():
            item_list = item_list + str(item) + " = " + str(value) +" :\n "
            if self.logLevel > 0: print "item_list", item_list
        if self.logLevel > 0: print item_list    
    
        #return self.toString() + " : " + item_list 
        return "*****to_string*****(\n" + self.getSource() + " : " + self.getTarget()  + " : " + item_list.rstrip(": ") + ")*****to_string end*******"
        
#if __name__ == "__main__":        
    #g =StationGraph()
    #print "Graph = ", g
    #for v in vertices:
        #print("Station "+str(v)+" --> "+ str(Graphs.neighborListOf(self.g_name,v)))
