package jmri.jmrix.openlcb;

/**
 * OlcbConstants.java
 *
 * Left over from CBUS migration, these references should go to the OpenLCB
 * libraries instead
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2010
 */
public final class OlcbConstants {

    public static final int DEFAULT_STANDARD_ID = 0x7a;
    public static final int DEFAULT_EXTENDED_ID = 0x7a;

    @Deprecated // CBUS, but still in OpenLcbAddress
    public static final int CBUS_ACON = 0x90;
    @Deprecated // CBUS, but still in OpenLcbAddress
    public static final int CBUS_ACOF = 0x91;

}


