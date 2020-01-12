package jmri;

/**
 * Represent a digital I/O on the layout.
 * 
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public interface DigitalIO extends NamedBean {

    /**
     * State value indicating output is on.
     */
    public static final int ON = 0x02;

    /**
     * State value indicating output is off.
     */
    public static final int OFF = 0x04;

    /**
     * Show whether state is stable.
     * 
     * For turnouts, a consistent state is one you can safely run trains over.
     * For lights, it's a state which is either on or off, not in between.
     *
     * @return true if state is valid and the known state is the same as commanded
     */
    public boolean isConsistentState();

    /**
     * Change the commanded state, which results in the relevant command(s)
     * being sent to the hardware. The exception is thrown if there are problems
     * communicating with the layout hardware.
     *
     * @param s the desired state
     */
    @InvokeOnLayoutThread
    public void setCommandedState(int s);

    /**
     * Query the commanded state. This is a bound parameter, so you can also
     * register a listener to be informed of changes.
     *
     * @return the commanded state
     */
    public int getCommandedState();
    
    /**
     * Query the known state. This is a bound parameter, so you can also
     * register a listener to be informed of changes. A result is always
     * returned; if no other feedback method is available, the commanded state
     * will be used.
     *
     * @return the known state
     */
    public int getKnownState();

    /**
     * Request an update from the layout soft/hardware. May not even happen, and
     * if it does it will happen later; listen for the result.
     */
    @InvokeOnLayoutThread
    public void requestUpdateFromLayout();
    
}
