package jmri.jmrix.can;

/**
 * Constants to represent CAN protocols and adapters
 *
 * @author Andrew Crosland Copyright (C) 2008
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


