package jmri.clock;

/**
 * A clock that can start and stop.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface ClockRunning {

    /**
     * Defines what to do with the fast clock when JMRI starts up.
     */
    enum ClockInitialRunState {
        /**
         * Changes the clock to stopped when JMRI starts.
         */
        DO_STOP,
        /**
         * Changes the clock to running when JMRI starts.
         */
        DO_START,
        /**
         * Does not change the clock when JMRI starts.
         */
        DO_NOTHING
    }

    /**
     * Property Change sent when the run status changes.
     */
    String PROPERTY_CHANGE_RUN = "run";

    /**
     * Set if Timebase is running.
     * @param y true if running else false.
     */
    void setRun(boolean y);

    /**
     * Get if Timebase is running.
     * @return true if running, else false.
     */
    boolean getRun();

    /**
     * Set the Clock Initial Run State ENUM.
     *
     * @param initialState Initial state.
     */
    void setClockInitialRunState(ClockInitialRunState initialState);

    /**
     * Get the Clock Initial Run State ENUM.
     *
     * @return Initial state.
     */
    ClockInitialRunState getClockInitialRunState();

}
