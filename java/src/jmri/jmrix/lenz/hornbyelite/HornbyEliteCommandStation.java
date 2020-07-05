package jmri.jmrix.lenz.hornbyelite;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the routines that differentiate a Hornby Elite Command Station from a
 * Lenz command station.
 *
 * @author Paul Bender Copyright (C) 2008
 */
@API(status = EXPERIMENTAL)
public class HornbyEliteCommandStation extends jmri.jmrix.lenz.LenzCommandStation {

    /**
     * The Hornby Elite does support Ops Mode programming.
     */
    @Override
    public boolean isOpsModePossible() {
        return true;
    }

}
