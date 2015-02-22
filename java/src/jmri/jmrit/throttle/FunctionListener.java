package jmri.jmrit.throttle;

/**
 * A Listener for a function state change.
 */
public interface FunctionListener extends java.util.EventListener {

    /**
     * Receive notification that a function has changed state.
     *
     * @param functionNumber The id of the function
     * @param isOn           True if the function is activated, false otherwise.
     */
    public void notifyFunctionStateChanged(int functionNumber, boolean isOn);

    /**
     * Get notification that a function's lockable status has changed.
     *
     * @param functionNumber The function that has changed (0-9).
     * @param isLockable     True if the function is now Lockable (continuously
     *                       active).
     */
    public void notifyFunctionLockableChanged(int functionNumber, boolean isLockable);
}
