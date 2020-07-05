package jmri.jmrit.withrottle;

/**
 *
 * @author Brett
 */
import java.util.EventListener;
import jmri.DccLocoAddress;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = MAINTAINED)
public interface ThrottleControllerListener extends EventListener {

    public void notifyControllerAddressFound(ThrottleController TC);

    public void notifyControllerAddressReleased(ThrottleController TC);
    
    public void notifyControllerAddressDeclined(ThrottleController tc, DccLocoAddress address, String reason);
}
