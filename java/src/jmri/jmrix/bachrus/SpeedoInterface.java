package jmri.jmrix.bachrus;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Define interface for receiving messages from the reader.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Andrew Crosland Copyright (C) 2010
 */
@API(status = EXPERIMENTAL)
public interface SpeedoInterface {

    public void addSpeedoListener(SpeedoListener l);

    public void removeSpeedoListener(SpeedoListener l);

    /**
     * Test operational status of interface.
     *
     * @return true is interface implementation is operational.
     */
    boolean status();

}
