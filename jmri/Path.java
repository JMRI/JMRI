package jmri;

/**
 * Represents a particular set of 
 * NamedBean (usually turnout) settings to put a path through 
 * trackwork to a Block.
 * <P>
 * Directions are defined for traffic along this path "to" the block, and
 * "from" the block. Although useful constants are defined, you don't 
 * have to restrict to those, and there's no assumption that they have
 * to be opposites; NORTH for "to" does not imply SOUTH for "from".
 * Being more specific:
 *<UL>
 *<LI>The "to" direction is the direction that a train is going 
 * when it traverses this path "to" the final block.
 *<LI>The "from" direction is the direction that a train is going 
 * when it traverses this path "from" the final block.
 *</UL>
 * <P>
 * This implementation only handles paths with zero or one elements;
 * clearly, this should be extended to a list of elements!
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
public class Path  {

    public Path() {
    }
    
    /**
     * Convenience constructor to set the destination/source block
     * and directions in one call
     */
    public Path(Block dest, int toBlockDirection, int fromBlockDirection) {
        this();
        _toBlockDirection = toBlockDirection;
        _fromBlockDirection = fromBlockDirection;
        setBlock(dest);
    }

    /**
     * Convenience constructor to set the destination/source block,
     * directions and a single setting in one call
     */
    public Path(Block dest, int toBlockDirection, int fromBlockDirection, BeanSetting setting) {
        this(dest, toBlockDirection, fromBlockDirection);
        addSetting(setting);
    }

    public void addSetting(BeanSetting t) {
        if (_element!=null) log.error("element already set; this implementation only handles one!");
        _element = t;
    }
    
    public void setBlock(Block b) {
        _block = b;
    }
    
    public Block getBlock() {
        return _block;
    }
    
    public int getToBlockDirection() { return _toBlockDirection; }
    public void setToBlockDirection(int d) { _toBlockDirection = d; }
    
    public int getFromBlockDirection() { return _fromBlockDirection; }
    public void setFromBlockDirection(int d) { _fromBlockDirection = d; }
    
    public boolean checkPathSet() {
        // empty conditions are always set
        if (_element == null) return true;
        
        // since we only have one element in this implementation, 
        // just return it's status
        return _element.check();
    }
    
    public static final int NONE  =0x000;
    public static final int NORTH =0x010;
    public static final int SOUTH =0x020;
    public static final int EAST  =0x040;
    public static final int WEST  =0x080;

    /**
     * Clockwise
     */
    public static final int CW    =0x100;
    /**
     * Counter-clockwise
     */
    public static final int CCW   =0x200;
        
    /**
     * Decode the direction constants into a human-readable
     * form
     */
    public String decodeDirection(int d) {
        switch (d) {
            case NONE:  return "None";
            case NORTH: return "North";
            case SOUTH: return "South";
            case EAST:  return "East";
            case WEST:  return "West";
            case CW:    return "CW";
            case CCW:   return "CCW";
            default:    return "(Unknown: "+d+")";
        }
    }
    
    private BeanSetting _element;
    private Block _block;
    private int _toBlockDirection;
    private int _fromBlockDirection;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Path.class.getName());
}
