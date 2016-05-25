// SlotListener.java
package jmri.jmrix.loconet;

/**
 * Interface for objects that want to be notified when a
 * {@link jmri.jmrix.loconet.LocoNetSlot} is modified.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision$
 */
public interface SlotListener extends java.util.EventListener {

    public void notifyChangedSlot(LocoNetSlot s);
}


/* @(#)SlotListener.java */
