package jmri.clock;

import javax.annotation.Nonnull;

/**
 * A clock that can be a master clock.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface ClockMaster {

    /**
     * Set internalMaster and update fields.
     *
     * @param master true if fast clock time is derived from internal computer clock,
     *                  false if derived from hardware clock.
     * @param update true to send update, else false.
     */
    void setInternalMaster(boolean master, boolean update);

    /**
     * Get internalMaster field.
     *
     * @return true if fast clock time is derived from internal computer clock,
     *  false if derived from hardware clock
     */
    boolean getInternalMaster();

    /**
     * Set the Master Clock Name.
     * @param name master clock name.
     */
    void setMasterName(@Nonnull String name);

    /**
     * Get the Master Clock Name.
     * @return master clock name.
     */
    String getMasterName();

    /**
     * Set if clock should synchronise.
     * @param synchronize  set true to synchronise hardware clocks with Time base.
     * @param update set true to update clock when function called.
     */
    void setSynchronize(boolean synchronize, boolean update);

    /**
     * Get if clock should synchronise with Time base.
     * @return true if should synchronise hardware clocks.
     */
    boolean getSynchronize();

    /**
     * Set if should correct or update hardware.
     * @param correct set true to correct hardware clocks.
     * @param update set true to update clock when function called.
     */
    void setCorrectHardware(boolean correct, boolean update);

    /**
     * Get if should correct Hardware clocks.
     * @return true to correct, else false.
     */
    boolean getCorrectHardware();

    /**
     * Initialize hardware clock at start up after all options are set up.
     * <p>
     * Note: This method is always called at start up. It should be ignored if
     * there is no communication with a hardware clock
     */
    void initializeHardwareClock();

}
