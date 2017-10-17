package jmri.jmrix.loconet.locostats;

/**
 * Interface for objects that want to be notified when a
 * {@link jmri.jmrix.loconet.LocoNetSlot} is modified.
 
 * @author Bob Milhaupt
 */
public interface LocoNetInterfaceStatsListener extends java.util.EventListener {
 
    public void notifyChangedInterfaceStatus(Object s);
}
