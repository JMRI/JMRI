package jmri.jmrit.display.layoutEditor;

import jmri.Block;

/**
 * TrackNode is a temporary object specifying and returning track node
 * information
 * <p>
 * Used in conjunction with ConnectivityUtil.java to return information about
 * track nodes following a search of layout connectivity.
 * <p>
 * Track nodes are nodes in the layout connectivity diagram. They may be:
 * positionable points - either anchor points that define a block boundary or
 * end bumpers (end of a track), turnouts, -OR_ level crossings
 * <p>
 * The components of a TrackNode are: Node Object - the object reached by
 * searching connectivity Node Type - connection types defined in Layout Editor,
 * for example, TURNOUT_A, indicates a turnout connected at A (the throat of a
 * RH, LH, or WYE turnout) Track Segment - the track segment connected at the
 * connection point specified in Node Type Reached End Bumper - 'true' if the
 * connectivity search has reached an end bumper (the end of the search track)
 * before reaching a Node Object. 'false' otherwise. Node State - if the Node
 * Object can have multiple states, for example, a turnout, this gives the state
 * it was when finding this track node.
 * <p>
 * Actually you could think of an End Bumper as a 'Node', but End Bumpers are
 * treated differently here. When an End Bumper is reached during a connectivity
 * search, Track Segment is returned, Reached End Bumper is set true, and Node
 * Object and Node Type, are not returned.
 *
 * @author Dave Duchamp Copyright (C) 2009
 * @author George Warner Copyright (c) 2017-2018
 */
public class TrackNode {

    public TrackNode(LayoutTrack node, int nodeType, TrackSegment segment, boolean endBumper,
            int nodeState) {
        _Node = node;
        _NodeType = nodeType;
        _TrackSegment = segment;
        _ReachedEndBumper = endBumper;
        _NodeState = nodeState;
    }

    // instance variables
    LayoutTrack _Node = null;
    int _NodeType = LayoutTrack.NONE;
    TrackSegment _TrackSegment = null;
    boolean _ReachedEndBumper = false;
    int _NodeState = 0;

    // temporary instance variables
    /**
     * Access methods
     */
    public void setNode(LayoutTrack node) {
        _Node = node;
    }

    public LayoutTrack getNode() {
        return _Node;
    }

    public void setNodeType(int type) {
        _NodeType = type;
    }

    public int getNodeType() {
        return _NodeType;
    }

    public void setTrackSegment(TrackSegment s) {
        _TrackSegment = s;
    }

    public TrackSegment getTrackSegment() {
        return _TrackSegment;
    }

    public void setReachedEndBumper(boolean end) {
        _ReachedEndBumper = end;
    }

    public boolean reachedEndOfTrack() {
        return _ReachedEndBumper;
    }

    public int getNodeState() {
        return _NodeState;
    }

    /**
     * Operational methods
     */
    /**
     * Returns the Block of the node Object at the nodeType position
     */
    public Block getNodeBlock() {
        if (LayoutTrack.POS_POINT == _NodeType) {
            return _TrackSegment.getLayoutBlock().getBlock();
        } else if (LayoutTrack.TURNOUT_A == _NodeType) {
            return ((LayoutTurnout) _Node).getLayoutBlock().getBlock();
        } else if (LayoutTrack.TURNOUT_B == _NodeType) {
            return ((LayoutTurnout) _Node).getLayoutBlockB().getBlock();
        } else if (LayoutTrack.TURNOUT_C == _NodeType) {
            return ((LayoutTurnout) _Node).getLayoutBlockC().getBlock();
        } else if (LayoutTrack.TURNOUT_D == _NodeType) {
            return ((LayoutTurnout) _Node).getLayoutBlockD().getBlock();
        } else if ((LayoutTrack.LEVEL_XING_A == _NodeType)
                || (LayoutTrack.LEVEL_XING_C == _NodeType)) {
            return ((LevelXing) _Node).getLayoutBlockAC().getBlock();
        } else if ((LayoutTrack.LEVEL_XING_B == _NodeType)
                || (LayoutTrack.LEVEL_XING_D == _NodeType)) {
            return ((LevelXing) _Node).getLayoutBlockBD().getBlock();
        }
        return null;
    }
}
