package jmri.jmrit.display.layoutEditor;

import jmri.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutConnectivity object represents a junction between two LayoutBlocks on
 * a LayoutEditor panel.
 * <p>
 * LayoutConnectivity objects do not persist (are not saved when panels are
 * saved). Instead they are initialized when a panel is loaded and changed
 * dynamically as a panel is edited.
 * <p>
 * The direction stored here is the direction for proceeding across the block
 * boundary from block1 to block2. The directions represent directions on the
 * LayoutEditor panel. Allowed values (using Path object definitions) are:
 * Path.NORTH (up on panel) Path.SOUTH (down on panel) Path.EAST (right on
 * panel) Path.WEST (left on panel) and points in between: Path.NORTH +
 * Path.EAST Path.NORTH_WEST, Path.SOUTH_EAST Path.SOUTH + Path.WEST
 * <p>
 * The connected object in the first block is usually a track segment. This
 * track segment is connected to an object in the second block. The connection
 * point in the second block can be either one end of a track segment, or one of
 * the connection points on a turnout, or one of the connection points on a
 * level crossing. The allowed values for the connection points at the second
 * block are defined in LayoutEditor.
 * <p>
 * The exception to the above is when a crossover turnout has multiple blocks.
 * If so, at least one block boundary is internal to the crossover turnout. Such
 * cases are handled differently, as "crossover block boundary types", see
 * definition of the type codes below. The first letter in the boundary type
 * corresponds to the first block, and the second letter corresponds to the
 * second block. All four block boundaries are possible for the double
 * crossover. One of the crossed over boundaries is not possible with each
 * single crossover.
 * <p>
 * Note that each LayoutEditor panel has its own list of LayoutConnectivity
 * objects, nominally called its "block connectivity". In contrast, there is
 * only one set of LayoutBlocks, Blocks, and Paths, which are used by all
 * LayoutEditor panels.
 *
 * @author Dave Duchamp Copyright (c) 2007-2008
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutConnectivity {

    /**
     * Constructor
     */
    public LayoutConnectivity(LayoutBlock b1, LayoutBlock b2) {
        block1 = b1;
        if (block1 == null) {
            log.error("null block1 when creating Layout Connectivity");
        }
        block2 = b2;
        if (block2 == null) {
            log.error("null block2 when creating Layout Connectivity");
        }
    }

    // defined constants for crossover block boundary types.
    final public static int NONE = 0;
    final public static int XOVER_BOUNDARY_AB = 1;  // continuing
    final public static int XOVER_BOUNDARY_CD = 2;  // continuing
    final public static int XOVER_BOUNDARY_AC = 3;  // xed over
    final public static int XOVER_BOUNDARY_BD = 4;  // xed over
    final public static int XOVER_BOUNDARY_AD = 1;  // continuing (slips)
    final public static int XOVER_BOUNDARY_BC = 2;  // continuing (slips)

    // instance variables
    private LayoutBlock block1 = null;
    private LayoutBlock block2 = null;

    private int direction = Path.NONE;
    private TrackSegment track1 = null;

    private LayoutTrack connect2 = null;
    private int typeConnect2 = 0;

    private LayoutTurnout xover = null;
    private int xoverBoundaryType = NONE;

    private PositionablePoint anchor = null;

    // this should only be used for debugging...
    @Override
    public String toString() {
        String result = "between " + block1 + " and " + block2 + " in direction " + Path.decodeDirection(direction);
        if (track1 != null) {
            result = result + ", track: " + track1.getId();
        }
        if (connect2 != null) {
            result = result + ", connect2: " + connect2.getId() + ", type2: " + typeConnect2;
        }
        if (xover != null) {
            result = result + ", xover: " + xover.getId() + ", xoverBoundaryType: " + xoverBoundaryType;
        }
        return result;
    }

    /**
     * Accessor routines
     */
    public LayoutBlock getBlock1() {
        return block1;
    }

    public LayoutBlock getBlock2() {
        return block2;
    }

    public int getDirection() {
        return direction;
    }

    public int getReverseDirection() {
        return Path.reverseDirection(direction);
    }

    public boolean setDirection(int dir) {
        if ((dir == Path.NORTH) || (dir == Path.SOUTH)
                || (dir == Path.EAST) || (dir == Path.WEST)
                || (dir == Path.NORTH_WEST) || (dir == (Path.NORTH_EAST))
                || (dir == (Path.SOUTH_WEST)) || (dir == (Path.SOUTH_EAST))) {
            direction = dir;
            return (true);
        }
        // not one of the allowed directions
        direction = Path.NONE;
        return (false);
    }

    public void setConnections(TrackSegment t, LayoutTrack o, int type, PositionablePoint p) {
        track1 = t;
        if (t == null) {
            log.error("null track1 when setting up LayoutConnectivity");
        }
        connect2 = o;
        if (o == null) {
            log.error("null connect track when setting up LayoutConnectivity");
        }
        typeConnect2 = type;
        anchor = p;
    }

    public void setXoverBoundary(LayoutTurnout t, int type) {
        xover = t;
        if (t == null) {
            log.error("null XOver when setting up LayoutConnectivity");
        }
        xoverBoundaryType = type;
    }

    public TrackSegment getTrackSegment() {
        return track1;
    }

    public LayoutTrack getConnectedObject() {
        return connect2;
    }

    public int getConnectedType() {
        return typeConnect2;
    }

    public LayoutTurnout getXover() {
        return xover;
    }

    public int getXoverBoundaryType() {
        return xoverBoundaryType;
    }

    public PositionablePoint getAnchor() {
        return anchor;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false; // assume failure (pessimist!)
        if ((o != null) && o instanceof LayoutConnectivity) {
            LayoutConnectivity lc = (LayoutConnectivity) o;
            do {    // poor mans throw block
                if (((block1 == null) != (lc.getBlock1() == null))
                        || ((block1 != null) && !block1.equals(lc.getBlock1()))) {
                    break;
                }
                if (((block2 == null) != (lc.getBlock2() == null))
                        || ((block2 != null) && !block2.equals(lc.getBlock2()))) {
                    break;
                }
                if (direction != lc.getDirection()) {
                    break;
                }
                if (((track1 == null) != (lc.getTrackSegment() == null))
                        || ((track1 != null) && !track1.equals(lc.getTrackSegment()))) {
                    break;
                }
                if (((connect2 == null) != (lc.getConnectedObject() == null))
                        || ((connect2 != null) && !connect2.equals(lc.getConnectedObject()))) {
                    break;
                }
                if (typeConnect2 != lc.getConnectedType()) {
                    break;
                }
                if (((xover == null) != (lc.getXover() == null))
                        || ((xover != null) && !xover.equals(lc.getXover()))) {
                    break;
                }
                if (((anchor == null) != (lc.getAnchor() == null))
                        || ((anchor != null) && !anchor.equals(lc.getAnchor()))) {
                    break;
                }
                result = true;
            } while (false);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.block1 != null ? this.block1.hashCode() : 0);
        hash = 37 * hash + (this.block2 != null ? this.block2.hashCode() : 0);
        hash = 37 * hash + direction;
        hash = 37 * hash + (this.track1 != null ? this.track1.hashCode() : 0);
        hash = 37 * hash + (this.connect2 != null ? this.connect2.hashCode() : 0);
        hash = 37 * hash + typeConnect2;
        hash = 37 * hash + (this.xover != null ? this.xover.hashCode() : 0);
        hash = 37 * hash + (this.anchor != null ? this.anchor.hashCode() : 0);
        return hash;
    }

    private final static Logger log
            = LoggerFactory.getLogger(LayoutConnectivity.class);
}   // class LayoutConnectivity
