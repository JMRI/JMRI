package jmri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent an EntryPoint to a Section of track. Specifies a Block within the
 * Section, and a Path of that Block.
 * <p>
 * An EntryPoint can be "forward" or "reverse" type, depending on if a train
 * entering the Section at this entry point will be travelling in the forward
 * direction or the reverse direction.
 * <p>
 * An EntryPoint is referenced via lists in its parent Section, and is stored on
 * disk when its parent section is stored.
 * <p>
 * This module delays initialization of Blocks until first reference after an
 * Entry Point is loaded from a configuration file.
 *
 * @author Dave Duchamp Copyright (C) 2008
 */
public class EntryPoint {

    public EntryPoint(jmri.Block b, jmri.Block pb, String fbDir) {
        mBlock = b;
        mFromBlock = pb;
        mFromBlockDirection = fbDir;    // direction from Path that triggered entry point
    }

    // special constructor for delayed initialization
    public EntryPoint(String bName, String fbName, String fbDir) {
        needsInitialize = true;
        blockName = bName;
        fromBlockName = fbName;
        mFromBlockDirection = fbDir;    // direction from Path that triggered entry point
    }

    /**
     * Constants representing the Direction of the Entry Point.
     */
    public static final int UNKNOWN = 0x02;
    public static final int FORWARD = 0x04;
    public static final int REVERSE = 0x08;

    // instance variables
    private Block mBlock = null;
    private Block mFromBlock = null;
    private int mDirection = UNKNOWN;
    private boolean mFixed = false;
    private String mFromBlockDirection = "";

    // temporary instance variables
    private boolean needsInitialize = false;
    private String blockName = "";
    private String fromBlockName = "";

    private void initialize() {
        mBlock = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(blockName);
        if (mBlock == null) {
            log.error("Missing block - " + blockName + " - when initializing entry point");
        }
        mFromBlock = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(fromBlockName);
        if (mFromBlock == null) {
            log.error("Missing block - " + fromBlockName + " - when initializing entry point");
        }
        needsInitialize = false;
    }

    /**
     * Get the block.
     *
     * @return the block, initialized if needed
     */
    public Block getBlock() {
        if (needsInitialize) {
            initialize();
        }
        return mBlock;
    }

    public String getFromBlockName() {
        if (needsInitialize) {
            initialize();
        }
        String s = mFromBlock.getSystemName();
        String u = mFromBlock.getUserName();
        if ((u != null) && (!u.equals(""))) {
            s = s + "( " + u + " )";
        }
        if ((mFromBlockDirection != null) && (!mFromBlockDirection.equals(""))) {
            s = s + "( " + mFromBlockDirection + " )";
        }
        return s;
    }

    public Block getFromBlock() {
        if (needsInitialize) {
            initialize();
        }
        return mFromBlock;
    }

    public void setTypeForward() {
        mDirection = FORWARD;
    }

    public void setTypeReverse() {
        mDirection = REVERSE;
    }

    public void setTypeUnknown() {
        mDirection = UNKNOWN;
    }

    public boolean isForwardType() {
        return mDirection == FORWARD;
    }

    public boolean isReverseType() {
        return mDirection == REVERSE;
    }

    public boolean isUnknownType() {
        return mDirection == UNKNOWN;
    }

    public int getDirection() {
        return mDirection;
    }

    public void setDirection(int dir) {
        mDirection = dir;
    }

    public void setFixed(boolean f) {
        mFixed = f;
    }

    public boolean isFixed() {
        return mFixed;
    }

    public String getFromBlockDirection() {
        return mFromBlockDirection;
    }

    private final static Logger log = LoggerFactory.getLogger(EntryPoint.class);
}
