// CanConstants.java
package jmri.jmrix.can;

/**
 * CanConstants.java
 *
 * Description:	Constants to represent CAN protocols and adpters
 *
 * @author	Andrew Crosland Copyright (C) 2008
 * @version $Revision$
 */
public final class CanConstants {

    /**
     * CAN protocols supported
     */
    public static final int CBUS = 0;
    public static final int FOR_TESTING = 100;

    /**
     * CAN adapter hardware supported
     */
    public static final int CANRS = 0;
    public static final int CANUSB = 1;
    public static final int CAN232 = 2;
}

/* @(#)CanConstants.java */
