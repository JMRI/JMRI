package jmri;

import java.awt.geom.Point2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jmri.util.MathUtil;

/**
 * Represents a particular set of NamedBean (usually turnout) settings to put a
 * path through trackwork to a Block.
 * <p>
 * Directions are defined for traffic along this path "to" the block, and "from"
 * the block. Being more specific:
 * <ul>
 *   <li>The "to" direction is the direction that a train is going when it
 *   traverses this path "to" the final block.
 *   <li>The "from" direction is the direction that a train is going when it
 *   traverses this path "from" the final block.
 * </ul>
 * Although useful constants are defined, you don't have to restrict to those,
 * and there's no assumption that they have to be opposites; NORTH for "to" does
 * not imply SOUTH for "from". This allows you to e.g. handle a piece of curved
 * track where you can be going LEFT at one point and UP at another. The
 * constants are defined as bits, so you can use more than one at a time, for
 * example a direction can simultanously be EAST and RIGHT if desired. What that
 * means needs to be defined by whatever object is using this Path.
 * <p>
 * This implementation handles paths with a list of bean settings. This has been
 * extended from the initial implementation.
 * <p>
 * The length of the path may also optionally be entered if desired. This
 * attribute is for use in automatic running of trains. Length should be the
 * actual length of model railroad track in the path. It is always stored here
 * in millimeter units. A length of 0.0 indicates no entry of length by the
 * user. If there is no entry the length of the block the path is in will be
 * returned. An Entry is only needed when there are paths of greatly different
 * lengths in the block.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2008
 */
public class Path {

    /**
     * Create an object with default directions of NONE, and no setting element.
     */
    public Path() {
    }

    /**
     * Convenience constructor to set the destination/source block and
     * directions in one call.
     *
     * @param dest               the destination
     * @param toBlockDirection   direction to next block
     * @param fromBlockDirection direction from prior block
     */
    public Path(Block dest, int toBlockDirection, int fromBlockDirection) {
        this();
        _toBlockDirection = toBlockDirection;
        _fromBlockDirection = fromBlockDirection;
        Path.this.setBlock(dest);
    }

    /**
     * Convenience constructor to set the destination/source block, directions
     * and a single setting element in one call.
     *
     * @param dest               the destination
     * @param toBlockDirection   direction to next block
     * @param fromBlockDirection direction from prior block
     * @param setting            the setting to add
     */
    public Path(Block dest, int toBlockDirection, int fromBlockDirection, BeanSetting setting) {
        this(dest, toBlockDirection, fromBlockDirection);
        Path.this.addSetting(setting);
    }

    public void addSetting(BeanSetting t) {
        _beans.add(t);
    }

    public List<BeanSetting> getSettings() {
        return _beans;
    }

    public void removeSetting(BeanSetting t) {
        _beans.remove(t);
    }

    public void clearSettings() {
        for (int i = _beans.size(); i > 0; i--) {
            _beans.remove(i - 1);
        }
    }

    public void setBlock(Block b) {
        _block = b;
    }

    public Block getBlock() {
        return _block;
    }

    public int getToBlockDirection() {
        return _toBlockDirection;
    }

    public void setToBlockDirection(int d) {
        _toBlockDirection = d;
    }

    public int getFromBlockDirection() {
        return _fromBlockDirection;
    }

    public void setFromBlockDirection(int d) {
        _fromBlockDirection = d;
    }

    /**
     * Check that the Path can be traversed. This means that any path elements
     * are set to the proper state, e.g. that the Turnouts on this path are set
     * to the proper CLOSED or OPEN status.
     *
     * @return true if the path can be traversed; always true if no path
     *         elements (BeanSettings) are defined.
     */
    public boolean checkPathSet() {
        // empty conditions are always set
        if (_beans.isEmpty()) {
            return true;
        }
        // check the status of all BeanSettings
        for (BeanSetting bean : _beans) {
            if (!bean.check()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Direction not known or not specified. May also represent "stopped", in
     * the sense of not moving in any direction.
     */
    public static final int NONE = 0x00000;
    /**
     * Northward
     */
    public static final int NORTH = 0x00010;
    /**
     * Southward
     */
    public static final int SOUTH = 0x00020;
    /**
     * Eastward
     */
    public static final int EAST = 0x00040;
    /**
     * Westward
     */
    public static final int WEST = 0x00080;

    /**
     * North-East
     */
    public static final int NORTH_EAST = NORTH | EAST;
    /**
     * South-East
     */
    public static final int SOUTH_EAST = SOUTH | EAST;
    /**
     * South-West
     */
    public static final int SOUTH_WEST = SOUTH | WEST;
    /**
     * North-West
     */
    public static final int NORTH_WEST = NORTH | WEST;

    /**
     * Clockwise
     */
    public static final int CW = 0x00100;
    /**
     * Counter-clockwise
     */
    public static final int CCW = 0x00200;
    /**
     * Leftward, e.g. on a schematic diagram or CTC panel
     */
    public static final int LEFT = 0x00400;
    /**
     * Rightward, e.g. on a schematic diagram or CTC panel
     */
    public static final int RIGHT = 0x00800;
    /**
     * Upward, e.g. on a schematic diagram or CTC panel
     */
    public static final int UP = 0x01000;
    /**
     * Downward, e.g. on a schematic diagram or CTC panel
     */
    public static final int DOWN = 0x02000;

    /**
     * Decode the direction constants into a human-readable form.
     *
     * @param d the direction
     * @return the direction description
     */
    static public String decodeDirection(int d) {
        if (d == NONE) {
            return Bundle.getMessage("None"); // UI strings i18n using NamedBeanBundle.properties
        }
        StringBuffer b = new StringBuffer();
        if (((d & NORTH) != 0) && ((d & EAST) != 0) ) {
            appendOne(b, Bundle.getMessage("NorthEast"));
        }
        else if (((d & NORTH) != 0) && ((d & WEST) != 0) ) {
            appendOne(b, Bundle.getMessage("NorthWest"));
        }
        else if (((d & SOUTH) != 0) && ((d & EAST) != 0) ) {
            appendOne(b, Bundle.getMessage("SouthEast"));
        }
        else if (((d & SOUTH) != 0) && ((d & WEST) != 0) ) {
            appendOne(b, Bundle.getMessage("SouthWest"));
        }
        else {
            if ((d & NORTH) != 0) {
                appendOne(b, Bundle.getMessage("North"));
            }
            if ((d & SOUTH) != 0) {
                appendOne(b, Bundle.getMessage("South"));
            }
            if ((d & EAST) != 0) {
                appendOne(b, Bundle.getMessage("East"));
            }
            if ((d & WEST) != 0) {
                appendOne(b, Bundle.getMessage("West"));
            }
        }
        if ((d & CW) != 0) {
            appendOne(b, Bundle.getMessage("Clockwise"));
        }
        if ((d & CCW) != 0) {
            appendOne(b, Bundle.getMessage("CounterClockwise"));
        }
        if ((d & LEFT) != 0) {
            appendOne(b, Bundle.getMessage("Leftward"));
        }
        if ((d & RIGHT) != 0) {
            appendOne(b, Bundle.getMessage("Rightward"));
        }
        if ((d & UP) != 0) {
            appendOne(b, Bundle.getMessage("ButtonUp")); // reuse "Up" in NBB
        }
        if ((d & DOWN) != 0) {
            appendOne(b, Bundle.getMessage("ButtonDown")); // reuse "Down" in NBB
        }
        final int mask = NORTH | SOUTH | EAST | WEST | CW | CCW | LEFT | RIGHT | UP | DOWN;
        if ((d & ~mask) != 0) {
            appendOne(b, "Unknown: 0x" + Integer.toHexString(d & ~mask));
        }
        return b.toString();
    }

    /**
     * Set path length.
     * Length may override the block length default.
     *
     * @param l length in millimeters
     */
    public void setLength(float l) {
        _length = l;
    }

    /**
     * Get actual stored length.
     *
     * @return length in millimeters or 0
     */
    public float getLength() {
        return _length;
    }

    /**
     * Get length in millimeters.
     *
     * @return the stored length if greater than 0 or the block length
     */
    public float getLengthMm() {
        if (_length <= 0.0f) {
            return _block.getLengthMm();
        }
        return _length;
    }

    /**
     * Get length in centimeters.
     *
     * @return the stored length if greater than 0 or the block length
     */
    public float getLengthCm() {
        if (_length <= 0.0f) {
            return _block.getLengthCm();
        }
        return (_length / 10.0f);
    }

    /**
     * Get length in inches.
     *
     * @return the stored length if greater than 0 or the block length
     */
    public float getLengthIn() {
        if (_length <= 0.0f) {
            return _block.getLengthIn();
        }
        return (_length / 25.4f);
    }

    static private void appendOne(StringBuffer b, String t) {
        if (b.length() != 0) {
            b.append(", ");
        }
        b.append(t);
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "equals operator should actually check for equality")
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(getClass() == obj.getClass())) {
            return false;
        } else {
            Path p = (Path) obj;

            if (!Float.valueOf(p._length).equals(this._length)) {
                return false;
            }

            if (p._toBlockDirection != this._toBlockDirection) {
                return false;
            }
            if (p._fromBlockDirection != this._fromBlockDirection) {
                return false;
            }

            if (p._block == null && this._block != null) {
                return false;
            }
            if (p._block != null && this._block == null) {
                return false;
            }
            if (p._block != null && this._block != null && !p._block.equals(this._block)) {
                return false;
            }

            if (p._beans.size() != this._beans.size()) {
                return false;
            }
            for (int i = 0; i < p._beans.size(); i++) {
                if (!p._beans.get(i).equals(this._beans.get(i))) {
                    return false;
                }
            }
        }
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String separator = ""; // no separator on first item // NOI18N
        for (BeanSetting beanSetting : this.getSettings()) {
            result.append(separator).append(MessageFormat.format("{0} with state {1}", beanSetting.getBean().getDisplayName(), beanSetting.getBean().describeState(beanSetting.getSetting()))); // NOI18N
            separator = ", "; // NOI18N
        }
        return MessageFormat.format("Path: \"{0}\" ({1}): {2}", getBlock().getDisplayName(), decodeDirection(getToBlockDirection()), result); // NOI18N
    }

    // Can't include _toBlockDirection, _fromBlockDirection, or block information as they can change
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this._beans);
        hash = 89 * hash + Objects.hashCode(this._block);
        hash = 89 * hash + this._toBlockDirection;
        hash = 89 * hash + this._fromBlockDirection;
        hash = 89 * hash + Float.floatToIntBits(this._length);
        return hash;
    }

    private final ArrayList<BeanSetting> _beans = new ArrayList<>();
    private Block _block;
    private int _toBlockDirection;
    private int _fromBlockDirection;
    private float _length = 0.0f;  // always stored in millimeters

    /**
     * Compute octagonal direction of vector from p1 to p2.
     * <p>
     * Note: the octagonal (8) directions are: North, North-East, East,
     * South-East, South, South-West, West and North-West
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the octagonal direction from p1 to p2
     */
    public static int computeDirection(Point2D p1, Point2D p2) {
        double angleDEG = MathUtil.computeAngleDEG(p2, p1);
        angleDEG = MathUtil.wrap360(angleDEG);  // don't want to deal with negative numbers here...

        // convert the angleDEG into an octant index (ccw from south)
        // note: because we use round here, the octants are offset by half (+/-22.5 deg)
        // so SOUTH isn't from 0-45 deg; it's from -22.5 deg to +22.5 deg; etc. for other octants.
        // (and this is what we want!)
        int octant = (int) Math.round(angleDEG / 45.0);

        // use the octant index to lookup its direction
        int dirs[] = {SOUTH, SOUTH_EAST, EAST, NORTH_EAST,
            NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH};
        return dirs[octant];
    }   // computeOctagonalDirection

    /**
     * Get the reverse octagonal direction.
     *
     * @param inDir the direction
     * @return the reverse direction or {@value #NONE} if inDir is not a
     *         direction
     */
    public static int reverseDirection(int inDir) {
        switch (inDir) {
            case NORTH:
                return SOUTH;
            case NORTH_EAST:
                return SOUTH_WEST;
            case EAST:
                return WEST;
            case SOUTH_EAST:
                return NORTH_WEST;
            case SOUTH:
                return NORTH;
            case SOUTH_WEST:
                return NORTH_EAST;
            case WEST:
                return EAST;
            case NORTH_WEST:
                return SOUTH_EAST;
            default:
                return NONE;
        }
    }

}
