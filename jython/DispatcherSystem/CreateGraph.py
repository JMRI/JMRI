  
import sys
import jmri
import os
my_path_to_jars = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/jars/jgrapht.jar')
sys.path.append(my_path_to_jars) # add the jar to your path
##http://www.java2s.com/Code/Jar/j/Downloadjgraphtjar.htm
from org.jgrapht.graph import DefaultEdge
from org.jgrapht.graph import DirectedMultigraph


# Definitions stored for future reference
# from org.jgrapht import DirectedGraph
# #from org.jgrapht.EdgeFactory import EdgeFactory
# from org.jgrapht import Graph
# from org.jgrapht import GraphHelper
# from org.jgrapht import GraphMapping
# from org.jgrapht import GraphPath
# from org.jgrapht import Graphs
# from org.jgrapht import ListenableGraph
# from org.jgrapht import UndirectedGraph
# from org.jgrapht import VertexFactory
# from org.jgrapht import WeightedGraph
# from org.jgrapht.alg import AbstractPathElement
# from org.jgrapht.alg import AbstractPathElementList
# from org.jgrapht.alg import BellmanFordIterator
# from org.jgrapht.alg import BellmanFordPathElement
# from org.jgrapht.alg import BellmanFordShortestPath
# from org.jgrapht.alg import BiconnectivityInspector
# from org.jgrapht.alg import BlockCutpointGraph
# from org.jgrapht.alg import BronKerboschCliqueFinder
# from org.jgrapht.alg import ConnectivityInspector
# from org.jgrapht.alg import CycleDetector
# from org.jgrapht.alg import DijkstraShortestPath
# from org.jgrapht.alg import DirectedNeighborIndex
# from org.jgrapht.alg import KShortestPaths
# from org.jgrapht.alg import KShortestPathsIterator
# from org.jgrapht.alg import NeighborIndex
# from org.jgrapht.alg import RankingPathElement
# from org.jgrapht.alg import RankingPathElementList
# from org.jgrapht.alg import StrongConnectivityInspector
# from org.jgrapht.alg import TransitiveClosure
# from org.jgrapht.alg import VertexCovers
# from org.jgrapht.alg.util import VertexDegreeComparator
# from org.jgrapht.demo import HelloJGraphT
# #from org.jgrapht.demo import JGraphAdapterDemo
# from org.jgrapht.demo import PerformanceDemo
# from org.jgrapht.event import ConnectedComponentTraversalEvent
# from org.jgrapht.event import EdgeTraversalEvent
# from org.jgrapht.event import GraphChangeEvent
# from org.jgrapht.event import GraphEdgeChangeEvent
# from org.jgrapht.event import GraphListener
# from org.jgrapht.event import GraphVertexChangeEvent
# from org.jgrapht.event import TraversalListener
# from org.jgrapht.event import TraversalListenerAdapter
# from org.jgrapht.event import VertexSetListener
# from org.jgrapht.event import VertexTraversalEvent
# from org.jgrapht.experimental import GraphReader
# from org.jgrapht.experimental import GraphSquare
# from org.jgrapht.experimental import GraphTests
# from org.jgrapht.experimental import PartiteRandomGraphGenerator
# from org.jgrapht.experimental import RandomGraphHelper
# from org.jgrapht.experimental import UniformRandomGraphGenerator
# from org.jgrapht.experimental.alg import ApproximationAlgorithm
# from org.jgrapht.experimental.alg import ExactAlgorithm
# from org.jgrapht.experimental.alg import IntArrayGraphAlgorithm
# from org.jgrapht.experimental.alg.color import BrownBacktrackColoring
# from org.jgrapht.experimental.alg.color import GreedyColoring
# from org.jgrapht.experimental.equivalence import EquivalenceComparator
# from org.jgrapht.experimental.equivalence import EquivalenceComparatorChain
# from org.jgrapht.experimental.equivalence import EquivalenceComparatorChainBase
# from org.jgrapht.experimental.equivalence import EquivalenceSet
# from org.jgrapht.experimental.equivalence import EquivalenceSetCreator
# from org.jgrapht.experimental.equivalence import UniformEquivalenceComparator
# from org.jgrapht.experimental.isomorphism import AbstractExhaustiveIsomorphismInspector
# from org.jgrapht.experimental.isomorphism import AdaptiveIsomorphismInspectorFactory
# from org.jgrapht.experimental.isomorphism import EquivalenceIsomorphismInspector
# from org.jgrapht.experimental.isomorphism import GraphIsomorphismInspector
# from org.jgrapht.experimental.isomorphism import GraphOrdering
# from org.jgrapht.experimental.isomorphism import IsomorphismRelation
# from org.jgrapht.experimental.isomorphism import PermutationIsomorphismInspector
# from org.jgrapht.experimental.isomorphism import VertexDegreeEquivalenceComparator
# from org.jgrapht.experimental.permutation import ArrayPermutationsIter
# from org.jgrapht.experimental.permutation import CollectionPermutationIter
# from org.jgrapht.experimental.permutation import CompoundPermutationIter
# from org.jgrapht.experimental.permutation import IntegerPermutationIter
# from org.jgrapht.experimental.permutation import PermutationFactory
# #from org.jgrapht.experimental.touchgraph import SimpleTouchgraphApplet
# #from org.jgrapht.experimental.touchgraph import TouchgraphConverter
# #from org.jgrapht.experimental.touchgraph import TouchgraphPanel
# from org.jgrapht.ext import DOTExporter
# from org.jgrapht.ext import EdgeNameProvider
# from org.jgrapht.ext import GmlExporter
# from org.jgrapht.ext import GraphMLExporter
# from org.jgrapht.ext import IntegerEdgeNameProvider
# from org.jgrapht.ext import IntegerNameProvider
# #from org.jgrapht.ext import JGraphModelAdapter
# from org.jgrapht.ext import MatrixExporter
# from org.jgrapht.ext import StringEdgeNameProvider
# from org.jgrapht.ext import StringNameProvider
# from org.jgrapht.ext import VertexNameProvider
# from org.jgrapht.ext import VisioExporter
# from org.jgrapht.generate import EmptyGraphGenerator
# from org.jgrapht.generate import GraphGenerator
# from org.jgrapht.generate import LinearGraphGenerator
# from org.jgrapht.generate import RandomGraphGenerator
# from org.jgrapht.generate import RingGraphGenerator
# from org.jgrapht.generate import WheelGraphGenerator
# from org.jgrapht.graph import AbstractBaseGraph
# from org.jgrapht.graph import AbstractGraph
# from org.jgrapht.graph import AsUndirectedGraph
# from org.jgrapht.graph import AsUnweightedDirectedGraph
# from org.jgrapht.graph import AsUnweightedGraph
# from org.jgrapht.graph import AsWeightedGraph
# #from org.jgrapht import graphBasedEdgeFactory
# #from org.jgrapht import graphBasedVertexFactory
# from org.jgrapht.graph import DefaultDirectedGraph
# from org.jgrapht.graph import DefaultDirectedWeightedGraph
# from org.jgrapht.graph import DefaultEdge
# from org.jgrapht.graph import DefaultGraphMapping
# from org.jgrapht.graph import DefaultListenableGraph
# from org.jgrapht.graph import DefaultWeightedEdge
# from org.jgrapht.graph import DirectedMaskSubgraph
# from org.jgrapht.graph import DirectedMultigraph
# from org.jgrapht.graph import DirectedPseudograph
# from org.jgrapht.graph import DirectedSubgraph
# from org.jgrapht.graph import DirectedWeightedMultigraph
# from org.jgrapht.graph import DirectedWeightedSubgraph
# from org.jgrapht.graph import EdgeReversedGraph
# from org.jgrapht.graph import EdgeSetFactory
# from org.jgrapht.graph import GraphDelegator
# from org.jgrapht.graph import IntrusiveEdge
# from org.jgrapht.graph import ListenableDirectedGraph
# from org.jgrapht.graph import ListenableDirectedWeightedGraph
# from org.jgrapht.graph import ListenableUndirectedGraph
# from org.jgrapht.graph import ListenableUndirectedWeightedGraph
# from org.jgrapht.graph import MaskEdgeSet
# from org.jgrapht.graph import MaskFunctor
# from org.jgrapht.graph import MaskSubgraph
# from org.jgrapht.graph import MaskVertexSet
# from org.jgrapht.graph import Multigraph
# from org.jgrapht.graph import ParanoidGraph
# from org.jgrapht.graph import Pseudograph
# from org.jgrapht.graph import SimpleDirectedGraph
# from org.jgrapht.graph import SimpleDirectedWeightedGraph
# from org.jgrapht.graph import SimpleGraph
# from org.jgrapht.graph import SimpleWeightedGraph
# from org.jgrapht.graph import Subgraph
# from org.jgrapht.graph import UndirectedMaskSubgraph
# from org.jgrapht.graph import UndirectedSubgraph
# from org.jgrapht.graph import UndirectedWeightedSubgraph
# from org.jgrapht.graph import UnmodifiableDirectedGraph
# from org.jgrapht.graph import UnmodifiableGraph
# from org.jgrapht.graph import UnmodifiableUndirectedGraph
# from org.jgrapht.graph import WeightedMultigraph
# from org.jgrapht.graph import WeightedPseudograph
# from org.jgrapht.traverse import AbstractGraphIterator
# from org.jgrapht.traverse import BreadthFirstIterator
# from org.jgrapht.traverse import ClosestFirstIterator
# from org.jgrapht.traverse import CrossComponentIterator
# from org.jgrapht.traverse import DepthFirstIterator
# from org.jgrapht.traverse import GraphIterator
# from org.jgrapht.traverse import TopologicalOrderIterator
# from org.jgrapht.util import ArrayUnenforcedSet
# from org.jgrapht.util import FibonacciHeap
# from org.jgrapht.util import FibonacciHeapNode
# from org.jgrapht.util import MathUtil
# from org.jgrapht.util import ModifiableInteger
# from org.jgrapht.util import PrefetchIterator
# from org.jgrapht.util import TypeUtil



class StationGraph(jmri.jmrit.automat.AbstractAutomaton):

    g = DirectedMultigraph(DefaultEdge)
    g_stopping = DirectedMultigraph(DefaultEdge)
    g_express = DirectedMultigraph(DefaultEdge)
    
    station_block_list = []
    station_blk_list = []
    dict_path_stopping = {}
    dict_path_express = {}
    dict_path_name_stopping= {}
    dict_path_name_express= {}
    
    loglevel = 0

    def __init__(self):
        if self.loglevel > 0: print "graph __init__"
        self.setup_station_block_list()
        if self.loglevel > 0: print "__init__2"
        self.setup_graph_vertices()
        if self.loglevel > 0: print "__init__3"
        self.setup_graph_edges()
        if self.loglevel > 0: print "finished graph init"

    # **************************************************
    # Set up station block list either from manual list or from Block Table
    # **************************************************        
        
    def setup_station_block_list(self):
        BlockManager = jmri.InstanceManager.getDefault(jmri.BlockManager)
        if self.loglevel > 0: print "Block", BlockManager.getNamedBeanSet()
        for block in BlockManager.getNamedBeanSet():
            #blocks with the word stop in the comment are stations
            comment = block.getComment()
            if comment != None:
                if "stop" in comment.lower():
                    station_block_name = block.getUserName()
                    self.station_block_list.append(station_block_name)
                    self.station_blk_list.append(self.get_layout_block(station_block_name))
        if self.loglevel > 0: print 'self.station_block_list' , self.station_block_list
        if self.loglevel > 0: print 'PPPPPPPPPPP'
        
    def get_layout_block(self, block_name):
        LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
        layoutBlock = LayoutBlockManager.getLayoutBlock(block_name)
        return layoutBlock        
        
    def setup_graph_vertices(self):

        for station_block_name in self.station_block_list:
            if self.loglevel > 0: print "station_block_name",station_block_name
            lblk = self.get_layout_block(station_block_name)
            if self.loglevel > 0: print "lblk",lblk
            self.g.addVertex(lblk)
            self.g_express.addVertex(station_block_name)
            self.g_stopping.addVertex(station_block_name)
        if self.loglevel > 0: print 'end setup_graph_vertices"
    
        
    def setup_graph_edges(self):
        if self.loglevel > 0: print "*****************************"
        if self.loglevel > 0: print "****setup_graph_edges********"
        if self.loglevel > 0: print "*****************************"
        LayoutBlockConnectivityTools=jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools()
        index = 0
        for station in self.station_block_list:
            if self.loglevel > 0: print "*********************"
            if self.loglevel > 0: print "station = " ,station
            if self.loglevel > 0: print "*********************"
            station_block = self.get_layout_block(station)
            station_block_name = station_block.getUserName()
            if self.loglevel > 0: print "station_block_name",station_block_name
            if self.loglevel > 0: print "no neighbors", station_block.getNumberOfNeighbours()
            for i in range(station_block.getNumberOfNeighbours()):
                if self.loglevel > 0: print "+++++++++++++++++++++++++"
                neighbor_name = station_block.getNeighbourAtIndex(i).getDisplayName()
                if self.loglevel > 0: print "neighbor_name",neighbor_name
                other_stations = [block for block in self.station_block_list if block not in [station]]
                if self.loglevel > 0: print "other_stations",other_stations
                for destination in other_stations:
                    index +=1
                    if self.loglevel > 0: print "--------------------------"
                    if self.loglevel > 0: print "destination",destination
                    if self.loglevel > 0: print "--------------------------"
                    sourceLayoutBlock = station_block
                    destinationLayoutBlock  = self.get_layout_block(destination)
                    protectingLayoutBlock = self.get_layout_block(neighbor_name)
                    validateOnly = True
                    pathMethod = LayoutBlockConnectivityTools.Routing.NONE
                    path = []
                    if self.loglevel > 0: print "got here 1"
                    try:
                        if self.loglevel > 0: print "got here 2a"
                        #sourceBlock = sourceLayoutBlock.getBlock()
                        #destinationBlock = destinationLayoutBlock.getBlock()
                        #a = sourceLayoutBlock.getThroughPathIndex(sourceBlock, destinationBlock)
                        if self.loglevel > 0: print "getThroughPathIndex", a
                        if self.loglevel > 0: print "printValidThroughPaths()",  printValidThroughPaths()
                        path = LayoutBlockConnectivityTools.getLayoutBlocks(sourceLayoutBlock, destinationLayoutBlock, protectingLayoutBlock, validateOnly, pathMethod)
                        if self.loglevel > 0: print path
                    except jmri.JmriException as e:
                        if self.loglevel > 0: print "got here 2b"
                        if self.loglevel > 0: print (e)
                        continue
                    finally:
                        if self.loglevel > 0: print "got here 3"
                        if path != []:
                            if self.loglevel > 0: print "got here 4a"
                            #add an edge for all paths to form the espress train graph
                            path_name = [str(x.getUserName()) for x in path]
                            edge = le()     # l = LabelledEdge() set up outside CreateGraph.py
                            if self.loglevel > 0: print edge.to_string()
                            if self.loglevel > 0: print "adding edge ", station_block_name, destination
                            if self.loglevel > 0: print edge.to_string()
                            if self.loglevel > 0: print "got here 4a2"
                            self.g_express.addEdge(station_block_name,destination, edge)
                            if self.loglevel > 0: print "got here 4a"
                            if self.loglevel > 0: print edge.to_string()
                            edge.setItem(index = index)
                            edge.setItem(path = path)
                            edge.setItem(path_name = path_name)
                            edge.setItem(neighbor_name = neighbor_name)
                            LayoutBlockManager=jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager)
                            penultimateLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[-2])
                            penultimate_block_name = penultimateLayoutBlock.getUserName()
                            edge.setItem(penultimate_block_name = penultimate_block_name)
                            if self.loglevel > 0: print edge.to_string()
                            if self.loglevel > 0: print "got here 4a3"
                            if self.loglevel > 0: print "self.g_express",self.g_express
                            
                            #add only edges for paths that do not go through a station for stopping train graph
                            if self.loglevel > 0: print "a","b",a,b
                            through_stations = [str(block_name) for block_name in self.station_block_list if block_name not in [station,destination]]
                            # if self.loglevel > 0: print "path", path_name
                            # if self.loglevel > 0: print "all through_stations not at start and end", through_stations
                            # if self.loglevel > 0: print "through stations check" ,[item in through_stations for item in path_name]
                            # if self.loglevel > 0: print "through stations in path" ,[ item  for item in path_name  if item in through_stations]
                            path_blocks_are_through_stations = [item in through_stations for item in path_name]
                            if self.loglevel > 0: print "any through stations" ,any(path_blocks_are_through_stations)
                            if self.loglevel > 0: print "not any through stations" , not any(path_blocks_are_through_stations)
                            if not any(path_blocks_are_through_stations):
                                #add to stopping graph
                                if self.loglevel > 0: print "adding to stopping graph"
                                edge = le()     # l = LabelledEdge() set up outside class
                                self.g_stopping.addEdge(station_block_name,destination,edge)
                                path_name = [str(x.getUserName()) for x in path]
                                edge.setItem(index = index)
                                edge.setItem(path = path)
                                edge.setItem(path_name = path_name)
                                edge.setItem(neighbor_name = neighbor_name)
                                penultimateLayoutBlock = LayoutBlockManager.getLayoutBlock(path_name[-2])
                                penultimate_block_name = penultimateLayoutBlock.getUserName()
                                edge.setItem(penultimate_block_name = penultimate_block_name)
                                if self.loglevel > 0: print edge.to_string()
                            else:
                                if self.loglevel > 0: print "not adding to stopping graph"
                                pass
                            if self.loglevel > 0: print "*********************************"    
                        else:
                            if self.loglevel > 0: print "got here 4b"
                            pass
                    if self.loglevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
                    if self.loglevel > 0: print "&& graph up to now &&"
                    if self.loglevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
                    #for e in self.g_express.edgeSet():
                        #print e.to_string()
                    if self.loglevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
                    if self.loglevel > 0: print "&& end graph up to now &&"
                    if self.loglevel > 0: print "&&&&&&&&&&&&&&&&&&&&&
                    
        if self.loglevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
        if self.loglevel > 0: print "&& express"
        if self.loglevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"        

        for e in self.g_express.edgeSet():
            if self.loglevel > 0: print (self.g_express.getEdgeSource(e) + " --> " + self.g_express.getEdgeTarget(e))
        
        if self.loglevel > 0: print "&&&&&&&&&&&&&&&&&&&&&"
        if self.loglevel > 0: print "&& stopping"
        if self.loglevel > 0: print "&&&&&&&&&&&&&&&&&&&&&" 
                    
        for e in self.g_stopping.edgeSet():
            if self.loglevel > 0: print (self.g_stopping.getEdgeSource(e) + " --> " + self.g_stopping.getEdgeTarget(e))                 
        
        #set the indicators for the train to reverse
        for e in self.g_stopping.edgeSet():
            
            if self.loglevel > 0: print "edge = ",e

            try:
                if self.loglevel > 0: print e, "Target", e.getTarget()
                if self.loglevel > 0: print e, "Source", e.getSource()
                opposite_direction_edge = self.g_stopping.getEdge(e.getTarget(),e.getSource())
                opposite_direction_neighbor_name = opposite_direction_edge.getItem("neighbor_name")
                e.setItem(opposite_direction_neighbor_name=opposite_direction_neighbor_name)
                if self.loglevel > 0: print "set opposite direction neighbor_name for edge" , e.to_string()
            except:
                if self.loglevel > 0: print "unable to set opposite direction neighbor_name for edge" , e.to_string()
                continue
                          

# r.Setup_station_block_list

# /**
 # * Custom edge class labeled with relationship type.
 # */
# @example:edgeclass:begin
class LabelledEdge(DefaultEdge):

    #label = ""
    loglevel = 0

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
        if self.loglevel > 0: print self.dict
        if self.loglevel > 0: print "item = ", item
        return self.dict[item]
        
    def getTarget(self):
        line = self.toString()
        line = line.lstrip("( ").rstrip(" )").split(" : ")
        if self.loglevel > 0: print "Target line", line
        target = str(line[1])
        if self.loglevel > 0: print "Target =", target
        return target 
        
    def getSource(self):
        line = self.toString()
        line = line.lstrip("( ").rstrip(" )").split(" : ")
        if self.loglevel > 0: print "Source line", line
        source = str(line[0])
        if self.loglevel > 0: print "Source =", source
        return source
    
    def to_string_one_line(self):
        item_list = ""
        for item, value in self.dict.items():
            item_list = item_list + str(item) + " = " + str(value) +" : "
            if self.loglevel > 0: print "item_list", item_list
        if self.loglevel > 0: print item_list    
    
        #return self.toString() + " : " + item_list 
        return "*****to_string*****(" + self.getSource() + " : " + self.getTarget()  + " : " + item_list.rstrip(": ") + ")"    

    def to_string(self):
        item_list = "\n"
        for item, value in self.dict.items():
            item_list = item_list + str(item) + " = " + str(value) +" :\n "
            if self.loglevel > 0: print "item_list", item_list
        if self.loglevel > 0: print item_list    
    
        #return self.toString() + " : " + item_list 
        return "*****to_string*****(\n" + self.getSource() + " : " + self.getTarget()  + " : " + item_list.rstrip(": ") + ")*****to_string end*******"
        
#if __name__ == "__main__":        
    #g =StationGraph()
    #print "Graph = ", g
    #for v in vertices:
        #print("Station "+str(v)+" --> "+ str(Graphs.neighborListOf(self.g_name,v)))
