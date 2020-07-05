package jmri.jmrix.loconet.locostats;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Interface for objects that want to be notified when a
 * {@link jmri.jmrix.loconet.LocoNetSlot} is modified.
 
 * @author Bob Milhaupt
 */
@API(status = EXPERIMENTAL)
public interface LocoNetInterfaceStatsListener extends java.util.EventListener {
 
    public void notifyChangedInterfaceStatus(Object s);
}
