package jmri.jmrix.loconet;

/**
 * Interface for marking a class as using slot status information
 * from the loconet.SlotManager.
 *
 * Objects of this type registereed with the
 * SlotManager via {@link SlotManager#addSlotStatusUser} to indicate
 * that status information is being used, so the {@link SlotManager}
 * should accumulate it.
 *
 * @author Bob Jacobsen   (C) 2026
 */
public interface SlotStatusUser {

}
