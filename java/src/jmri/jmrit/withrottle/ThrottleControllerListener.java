package jmri.jmrit.withrottle;

/**
 *
 * @author Brett
 * @version $Revision$
 */
import java.util.EventListener;

public interface ThrottleControllerListener extends EventListener {

    public void notifyControllerAddressFound(ThrottleController TC);

    public void notifyControllerAddressReleased(ThrottleController TC);

}
