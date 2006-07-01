package jmri.jmrit.throttle;

/**
 * A Listener for a function state change.
 */
public interface FunctionListener extends java.util.EventListener
{
    /**
     * Receive notification that a function has changed state.
     * @param functionNumber The id of the function
     * @param isOn True if the function is activated, false otherwise.
     */
    public void notifyFunctionStateChanged(int functionNumber, boolean isOn);
}