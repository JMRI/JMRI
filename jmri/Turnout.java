// Turnout.java

package jmri;


/**
 * Represent a Turnout on the layout.
 * <P>
 * The AbstractTurnout class contains a basic implementation of the state and messaging
 * code, and forms a useful start for a system-specific implementation.
 * Specific implementations in the jmrix package, e.g. for LocoNet and NCE, will
 * convert to and from the layout commands.
 * <P>
 * The states  and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <P>
 * A sample use of the Turnout interface can be seen in the jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 * class, which provides a simple GUI for controlling a single turnout.
 * <P>
 * Each Turnout object has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 * <P>
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version	$Revision: 1.13 $
 * @see         jmri.AbstractTurnout
 * @see         jmri.TurnoutManager
 * @see         jmri.InstanceManager
 * @see         jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 */
public interface Turnout extends NamedBean {

    /**
     * Constant representing an "closed" state, either in readback
     * or as a commanded state. Note that it's possible to be
     * both CLOSED and THROWN at the same time on some systems,
     * which should be called INCONSISTENT
     */
    public static final int CLOSED       = 0x02;

    /**
     * Constant representing an "thrown" state, either in readback
     * or as a commanded state. Note that it's possible to be
     * both CLOSED and THROWN at the same time on some systems,
     * which should be called INCONSISTENT
     */
    public static final int THROWN       = 0x04;

    /**
     * Query the known state.  This is a bound parameter, so
     * you can also register a listener to be informed of changes.
     * A result is always returned; if no other feedback method is
     * available, the commanded state will be used.
     */
    public int getKnownState();

    /**
     * Change the commanded state, which results in the relevant command(s) being
     * sent to the hardware. The exception is thrown if there are
     * problems communicating with the layout hardware.
     */
    public void setCommandedState(int s);

    /**
     * Query the commanded state.  This is a bound parameter, so
     * you can also register a listener to be informed of changes.
     */
    public int getCommandedState();

    /**
     * Constant representing "direct feedback method".  In this case,
     * the commanded state is provided when the known state is requested.
     * The two states never differ.
     * This mode is always possible!
     */
    public static final int DIRECT     = 1;

    /**
     * Constant representing "exact feedback method".  In this case,
     * the layout hardware can sense both positions of the turnout, which is
     * used to set the known state.
     */
    public static final int EXACT    = 2;

    /**
     * Constant representing "indirect feedback".  In this case,
     * the layout hardware can only sense one setting of the turnout. The known
     * state is inferred from that info.
     */
    public static final int INDIRECT = 4;  // only one side directly sensed

    /**
     * Constant representing "feedback by monitoring sent commands".  In this case,
     * the known state tracks commands seen on the rails or bus.
     */
    public static final int MONITORING     = 8;

    /**
     * Constant representing "feedback by monitoring one sensor".  
     * The sensor sets the state CLOSED when INACTIVE
     * and THROWN when ACTIVE
     */
    public static final int ONESENSOR     = 16;

    /**
     * Constant representing "feedback by monitoring two sensors".  
     * The first sensor sets the state THROWN when ACTIVE;
     * the second sensor sets the state CLOSED when ACTIVE.
     */
    public static final int TWOSENSOR     =32;

    /**
     * Get a representation of the feedback type.  This is the OR of
     * possible values: DIRECT, EXACT, etc.
     * The valid combinations depend on the implemented system.
     */
    public int getValidFeedbackTypes();
    
    /**
     * Get a human readable
     * representation of the feedback type.  
     * The values depend on the implemented system.
     */
    public String[] getValidFeedbackNames();
    
    /**
     * Set the feedback mode from a human readable name.
     * This must be one of the names defined
     * in a previous {@link #getValidFeedbackNames} call.
     */
    public void setFeedbackMode(String mode);

    /**
     * Set the feedback mode from a integer.
     * This must be one of the bit values defined
     * in a previous {@link #getValidFeedbackTypes} call.
     * Having more than one bit set is an error.
     */
    public void setFeedbackMode(int mode);
    
    /**
     * Get the feedback mode in human readable form.
     * This will be one of the names defined
     * in a {@link #getValidFeedbackNames} call.
     */
    public String getFeedbackModeName();
    /**
     * Get the feedback mode in machine readable form.
     * This will be one of the bits defined
     * in a {@link #getValidFeedbackTypes} call.
     */
    public int getFeedbackMode();
    /**
     * Get the indicator for whether automatic operation (retry) has been
     * inhibited for this turnout
     */
    public boolean getInhibitOperation();
    /**
     * Change the value of the inhibit operation indicator
     * @param io
     */
    public void setInhibitOperation(boolean io);
    /**
     * @return current operation automation class
     */
    public TurnoutOperation getTurnoutOperation();
    /**
     * set current automation class
     * @param toper TurnoutOperation subclass instance
     */
    public void setTurnoutOperation(TurnoutOperation toper);

    /**
     * Provide Sensor objects needed for some feedback types.
     *
     * Since we defined two feeedback methods that require monitoring,
     * we provide these methods to define those sensors to the Turnout.
     *<P>
     * The second sensor can be null if needed.
     * <P>
     * Sensor-based feedback will not function until these
     * sensors have been provided.
     */
    public void provideFirstFeedbackSensor(Sensor s);
    public void provideSecondFeedbackSensor(Sensor s);
    
    /**
     * Get the first sensor, if defined.
     *<P>
     * Returns null if no Sensor recorded.
     */
    public Sensor getFirstSensor();
    
    /**
     * Get the Second sensor, if defined.
     *<P>
     * Returns null if no Sensor recorded.
     */
    public Sensor getSecondSensor();    
    
    /**
     * Sets the initial known state (CLOSED,THROWN,UNKNOWN) from feedback
	 *    information, if appropriate.
     *<P>
	 * This method is designed to be called only when Turnouts are loaded
	 *    and when a new Turnout is defined in the Turnout table.
     *<P>
     * No change to known state is made if feedback information is not
	 *    available.  If feedback information is inconsistent, or if 
	 *    sensor definition is missing in ONESENSOR and TWOSENSOR feedback,
	 *    turnout state is set to UNKNOWN.
     */
    public void setInitialKnownStateFromFeedback();
    
}

/* @(#)Turnout.java */
