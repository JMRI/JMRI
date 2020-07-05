package jmri.jmrix.can;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Constants to represent CAN protocols and adapters
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
@API(status = EXPERIMENTAL)
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


