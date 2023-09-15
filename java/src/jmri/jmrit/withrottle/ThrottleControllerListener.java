package jmri.jmrit.withrottle;

/**
 *
 * @author Brett
 */
import java.util.EventListener;
import jmri.DccLocoAddress;

public interface ThrottleControllerListener extends EventListener {

    void notifyControllerAddressFound(ThrottleController TC);

    void notifyControllerAddressReleased(ThrottleController TC);
    
    void notifyControllerAddressDeclined(ThrottleController tc, DccLocoAddress address, String reason);
}
