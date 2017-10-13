package jmri.jmrit.withrottle;

/**
 *
 * @author Brett
 */
import java.util.EventListener;
import jmri.LocoAddress;

public interface ThrottleControllerListener extends EventListener {

    public void notifyControllerAddressFound(ThrottleController TC);

    public void notifyControllerAddressReleased(ThrottleController TC);
    
    public void notifyControllerAddressDeclined(ThrottleController tc, LocoAddress address);
}
