// TrackNode.java

package jmri.jmrit.display.layoutEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

/**
 * TrackNode is a temporary object specifying and returning track node information 
 * <P>
 * Used in conjunction with ConnectivityUtil.java to return information about track nodes 
 *      following a search of layout connectivity.
 * <P>
 * Track nodes are nodes in the layout connectivity diagram. They may be: 
 *		positionable points - either anchor points that define a block boundary or end bumpers 
 *               (end of a track), 
 *		turnouts, -OR_
 *		level crossings 
 *<P>
 * The components of a TrackNode are:
 *      Node Object - the object reached by searching connectivity
 *		Node Type - connection types defined in Layout Editor, for example, TURNOUT_A, indicates
 *			a turnout connected at A (the throat of a RH, LH, or WYE turnout)
 *      Track Segment - the track segment connected at the connection point specified in 
 *          Node Type
 *		Reached End Bumper - 'true' if the connectivity search has reached an end bumper (the 
 *			end of the search track) before reaching a Node Object. 'false' otherwise.
 *		Node State - if the Node Object can have multiple states, for example, a turnout, 
 *			this gives the state it was when finding this track node.
 *<P>
 * Actually you could think of an End Bumper as a 'Node', but End Bumpers are treated 
 *		differently here. When an End Bumper is reached during a connectivity search, 
 *		Track Segment is returned, Reached End Bumper is set true, and Node Object and Node 
 *		Type, are not returned.
 *
 * @author	Dave Duchamp  Copyright (C) 2009
 * @version	$Revision$
 */
public class TrackNode {

    public TrackNode(Object node, int nodeType, TrackSegment segment, boolean endBumper, 
									int nodeState) {
		_Node = node;
		_NodeType = nodeType;
		_TrackSegment = segment;
		_ReachedEndBumper = endBumper;
		_NodeState = nodeState;
    }
    
	// instance variables
	Object _Node = null;
	int _NodeType = LayoutEditor.NONE;
	TrackSegment _TrackSegment = null;
	boolean _ReachedEndBumper = false;
	int _NodeState = 0;
	
	// temporary instance variables
	
	/**
     * Access methods 
     */
	public void setNode(Object node) {_Node = node;}
	public Object getNode() {return _Node;}
	public void setNodeType(int type) {_NodeType = type;}
	public int getNodeType() {return _NodeType;}
	public void setTrackSegment(TrackSegment s) { _TrackSegment = s;}
	public TrackSegment getTrackSegment() {return _TrackSegment;}
	public void setReachedEndBumper(boolean end) {_ReachedEndBumper = end;}
	public boolean reachedEndOfTrack() {return _ReachedEndBumper;}
	public int getNodeState() {return _NodeState;}
	
	/**
	 * Operational methods
	 */
	 
	/**
	 * Returns the Block of the node Object at the nodeType position
	 */
	public Block getNodeBlock() {
		if (LayoutEditor.POS_POINT==_NodeType) {
			return _TrackSegment.getLayoutBlock().getBlock();
		}
		else if (LayoutEditor.TURNOUT_A==_NodeType) {
			return ((LayoutTurnout)_Node).getLayoutBlock().getBlock();
		}
		else if (LayoutEditor.TURNOUT_B==_NodeType) {
			return ((LayoutTurnout)_Node).getLayoutBlockB().getBlock();
		}
		else if (LayoutEditor.TURNOUT_C==_NodeType) {
			return ((LayoutTurnout)_Node).getLayoutBlockC().getBlock();
		}
		else if (LayoutEditor.TURNOUT_D==_NodeType) {
			return ((LayoutTurnout)_Node).getLayoutBlockD().getBlock();
		}
		else if ( (LayoutEditor.LEVEL_XING_A==_NodeType) ||
					(LayoutEditor.LEVEL_XING_C==_NodeType) ) {
			return ((LevelXing)_Node).getLayoutBlockAC().getBlock();
		}
		else if ( (LayoutEditor.LEVEL_XING_B==_NodeType) ||
					(LayoutEditor.LEVEL_XING_D==_NodeType) ) {
			return ((LevelXing)_Node).getLayoutBlockBD().getBlock();
		}
		return null;
	}
    
    static Logger log = LoggerFactory.getLogger(TrackNode.class.getName());
}

/* @(#)TrackNode.java */
