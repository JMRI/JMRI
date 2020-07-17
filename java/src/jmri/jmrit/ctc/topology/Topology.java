package jmri.jmrit.ctc.topology;

import java.util.*;

import jmri.*;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.display.layoutEditor.*;

/**
 *
 * IF AND ONLY IF LayoutDesign is available:
 * 
 * This object creates a list of objects that describe the path
 * from the passed O.S. section to ALL other adjacent O.S. sections
 * with defined and enabled Traffic Direction Levers.
 * 
 * Ultimately, this will provide information to fill in completely
 * (for the direction indicated: left / right traffic) the table
 * _mTRL_TrafficLockingRulesSSVList in FrmTRL_Rules.java
 * 
 * Sorry to say that as of 7/16/2020, LayoutBlock routine
 * "getNeighbourAtIndex" does NOT work in complex track situations
 * (one that I know of: Double crossovers), which leads to a
 * failure to "auto-generate" properly Signal Mast Logic for specific
 * signal masts.  I wanted to avoid this.
 * 
 * Therefore, I decided to do my own simple topology here.  I did one in C++ in
 * the early 1990's: a system whereby a layout drawn is analyzed (topology) so
 * that the C++ code could act as engineer and dispatcher, and FULLY automate
 * a layout (which I believe is a design goal of JMRI, which I believe
 * does work).  It supported reverse loops too whereby the route would have to
 * traverse it to get to a location in the opposite direction from where
 * a starting point is......  But enough of that.  It was JMRI for it's day!
 * (except with none of the complexity, IMHO.  That drawn diagram was the
 * ONLY thing needed, no blocks, transits, etc.!)  I ran my "N" scale layout
 * with it for many years.  Off of a "simple" drawing.
 * 
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 */

/*
Basic idea:
    For ALL the paths OUT OF our O.S. section:
        For each path out of our O.S. section IN A GIVEN DIRECTION:
            Find ALL paths to O.S. sections in that direction.
*/

public class Topology {
    private final LayoutBlockManager _mLayoutBlockManager = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
    private Sensor _mStartingOSSectionSensor = null;
    private LayoutBlock _mStartingLayoutBlock = null;
    
    /**
     * 
     * @param OSSectionOccupiedExternalSensor String (text) name of starting O.S. block sensor.
     * @return boolean - true if available, else false.
     */
    public boolean isTopologyAvailable(String OSSectionOccupiedExternalSensor) {
        _mStartingOSSectionSensor = InstanceManager.getDefault(SensorManager.class).getSensor(OSSectionOccupiedExternalSensor);
        if (null != _mStartingOSSectionSensor) { // Available:
            _mStartingLayoutBlock = _mLayoutBlockManager.getBlockWithSensorAssigned(_mStartingOSSectionSensor);
            return _mStartingLayoutBlock != null;
        }
        return false;
    }
    
    /**
     * 
     * "isTopologyAvailable" left "_mStartingLayoutBlock" set to a LayoutBlock
     * where we trace the topology from in all directions.  We use that as a
     * starting point.
     * 
     * @return LinkedList<TopologyInfo> All of the possible paths from this O.S. section to all O.S. sections in every
     * direction from here.  If there is NO terminating O.S. section in a direction from here, that entry
     * is NOT put in the return value, as it is ALWAYS valid to "go there" since
     * there is no controlling (dispatcher) agency.
     * 
     * 
     * JMRI: West is left, East is right.
     */
    /**
     * 
     * @param leftTraffic - True if for left traffic, else false for right traffic.
     * @return  null - No useful topology information in that direction (probably goes to dead end?) otherwise TopologyInfo object.
     */
    public TopologyInfo getTrafficLockingRules(boolean leftTraffic) {
        ArrayList<Integer> directions = getDirectionArrayListFrom(leftTraffic ? Path.WEST : Path.EAST);
        if (null != _mStartingLayoutBlock) { // Safety
            Block block = _mStartingLayoutBlock.getBlock();
            return followTopology(block, directions);
        }
        return null;
    }

    /**
     * 
     * @param block
     * @param directions
     * @return Returns null if no topology (goes to dead end?), otherwise topology info for all possible
     *         routes from passed block in passed directions.
     */    
//  Watch for same sensor in a row! (due to multiple switches in same O.S. section?)
//SAV-PDC-OS-MAIN (7,8) Right Traffic (JMRI:east, my panel: west)
    private TopologyInfo followTopology(Block block, ArrayList<Integer> directions) {
        TopologyInfo topologyInfo = new TopologyInfo();
        for (Path path : block.getPaths()) { // For all paths:
            if (inSameDirectionGenerally(directions, path.getToBlockDirection())) { // In generally same direction:
                ArrayList<Integer> newDirections = getDirectionArrayListFrom(path.getToBlockDirection());   // Follow it's twists and turns for next recursion
                
            }
        }
        return topologyInfo;
    }
    
    /**
     * 
     * @param direction Direction to generate list from.
     * @return IF passed a valid direction, a 3 element set of "generally in the same direction" directions, else an EMPTY set (NOT null!)
     */
    private ArrayList<Integer> getDirectionArrayListFrom(int direction) {
        switch (direction) {
            case Path.NORTH:
                return new ArrayList<>(Arrays.asList(Path.NORTH_WEST, Path.NORTH, Path.NORTH_EAST));
            case Path.NORTH_EAST:
                return new ArrayList<>(Arrays.asList(Path.NORTH, Path.NORTH_EAST, Path.EAST));
            case Path.EAST:
                return new ArrayList<>(Arrays.asList(Path.NORTH_EAST, Path.EAST, Path.SOUTH_EAST));
            case Path.SOUTH_EAST:
                return new ArrayList<>(Arrays.asList(Path.EAST, Path.SOUTH_EAST, Path.SOUTH));
            case Path.SOUTH:
                return new ArrayList<>(Arrays.asList(Path.SOUTH_EAST, Path.SOUTH, Path.SOUTH_WEST));
            case Path.SOUTH_WEST:
                return new ArrayList<>(Arrays.asList(Path.SOUTH, Path.SOUTH_WEST, Path.WEST));
            case Path.WEST:
                return new ArrayList<>(Arrays.asList(Path.SOUTH_WEST, Path.WEST, Path.NORTH_WEST));
            case Path.NORTH_WEST:
                return new ArrayList<>(Arrays.asList(Path.WEST, Path.NORTH_WEST, Path.NORTH));
        }
        return new ArrayList<>();    // Huh?
    }
    
    /**
     * 
     * @param possibleDirections The set of possible directions to check.  IF this
     *                           array has no entries, then this routine returns true.
     *                           This implies that the prior section was a "joiner"
     *                           block (only two adjacent blocks).
     * 
     * @param direction Direction to check.
     * @return True if direction in "possibleDirections" with the above caveat.
     *         emptor else false.
     */    
    
    private boolean inSameDirectionGenerally(ArrayList<Integer> possibleDirections, int direction) {
        if (possibleDirections.isEmpty()) return true;  // If nothing passed in this, then probably a "straight section" between sections.....
        return possibleDirections.contains(direction);
    }
}

/*
for blk in blocks.getNamedBeanSet():
    print 'block = {}'.format(blk.getDisplayName())
    for path in blk.getPaths():
        print '    neighbor = {}: {}'.format(path.getBlock().getDisplayName(), jmri.Path.decodeDirection(path.getToBlockDirection()))
        for setting in path.getSettings():
            print '        bean = {}, state = {}'.format(setting.getBeanName(), setting.getBean().describeState(setting.getSetting()))
*/
//  https://www.jmri.org/JavaDoc/doc/constant-values.html#jmri.Path.EAST    
