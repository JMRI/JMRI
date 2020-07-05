package jmri.jmrix.sprog;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * [describe the SlotListener class here]
 *
 * @author Bob Jacobsen Copyright (C) 2001 Andrew Crosland (c) 2006 ported for
 * SPROG
 */
@API(status = EXPERIMENTAL)
public interface SprogSlotListener extends java.util.EventListener {

    // This and the LocoNet equivalent should probably be re-factored
    // to a higher place in the hierarchy
    public void notifyChangedSlot(SprogSlot s);
}
