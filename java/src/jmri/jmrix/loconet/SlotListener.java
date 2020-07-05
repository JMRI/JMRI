package jmri.jmrix.loconet;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Interface for objects that want to be notified when a
 * {@link jmri.jmrix.loconet.LocoNetSlot} is modified.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
@API(status = EXPERIMENTAL)
public interface SlotListener extends java.util.EventListener {

    public void notifyChangedSlot(LocoNetSlot s);
}
