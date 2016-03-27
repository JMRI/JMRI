package jmri;

/**
 * Represent a Turnout on the layout.
 * <P>
 * A Turnout has two states:
 * <ul>
 * <li>The "commandedState" records the state that's been commanded in the
 * program. It might take some time, perhaps a long time, for that to actually
 * take effect.
 * <li>The "knownState" is the program's best idea of the actual state on the
 * the layout.
 * </ul>
 * <P>
 * There are a number of reasons that commandedState and knownState differ:
 * <ul>
 * <li>A change has been commanded, but it hasn't had time to happen yet
 * <li>Something has gone wrong, and a commanded change isn't actually going to
 * happen
 * <li>Although the program hasn't commanded a change, something on the layout
 * has made the turnout change. This could be a local electrical button, a
 * mechanical movement of the points, or something else.
 * <li>For a bus-like system, e.g. LocoNet or XPressNet, some other device might
 * have sent a command to change the turnout.
 * </ul>
 * <P>
 * Turnout feedback is involved in the connection between these two states; for
 * more information see the
 * <a href="http://jmri.org/help/en/html/doc/Technical/TurnoutFeedback.shtml">feedback
 * page</a>.
 * <P>
 * The AbstractTurnout class contains a basic implementation of the state and
 * messaging code, and forms a useful start for a system-specific
 * implementation. Specific implementations in the jmrix package, e.g. for
 * LocoNet and NCE, will convert to and from the layout commands.
 * <P>
 * The states and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <P>
 * A sample use of the Turnout interface can be seen in the
 * jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame class, which provides a
 * simple GUI for controlling a single turnout.
 * <P>
 * Each Turnout object has a two names. The "user" name is entirely free form,
 * and can be used for any purpose. The "system" name is provided by the
 * system-specific implementations, and provides a unique mapping to the layout
 * control system (e.g. LocoNet, NCE, etc) and address within that system.
 * <p>
 * Turnouts exhibit some complex behaviors. At the same time, they are sometimes
 * used as generic binary outputs where those get in the way. Eventually, we
 * need to have a separate e.g. Output class, but for now you can defeat much of
 * the advanced behaviors with the setBinaryOutput(true) method. This is a
 * configuration property; changing it on the fly may give unexpected results.
 * It's value is not persisted.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 * @see jmri.TurnoutManager
 * @see jmri.InstanceManager
 * @see jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 */
public interface Turnout extends NamedBean {

    /**
     * Constant representing an "closed" state, either in readback or as a
     * commanded state. Note that it's possible to be both CLOSED and THROWN at
     * the same time on some systems, which should be called INCONSISTENT
     */
    public static final int CLOSED = 0x02;

    /**
     * Constant representing an "thrown" state, either in readback or as a
     * commanded state. Note that it's possible to be both CLOSED and THROWN at
     * the same time on some systems, which should be called INCONSISTENT
     */
    public static final int THROWN = 0x04;

    /**
     * Query the known state. This is a bound parameter, so you can also
     * register a listener to be informed of changes. A result is always
     * returned; if no other feedback method is available, the commanded state
     * will be used.
     */
    public int getKnownState();

    /**
     * Change the commanded state, which results in the relevant command(s)
     * being sent to the hardware. The exception is thrown if there are problems
     * communicating with the layout hardware.
     */
    public void setCommandedState(int s);

    /**
     * Query the commanded state. This is a bound parameter, so you can also
     * register a listener to be informed of changes.
     */
    public int getCommandedState();

    /**
     * Show whether state is one you can safely run trains over
     *
     * @return	true iff state is a valid one and the known state is the same as
     *         commanded
     */
    public boolean isConsistentState();

    /**
     * Constant representing "direct feedback method". In this case, the
     * commanded state is provided when the known state is requested. The two
     * states never differ. This mode is always possible!
     */
    public static final int DIRECT = 1;

    /**
     * Constant representing "exact feedback method". In this case, the layout
     * hardware can sense both positions of the turnout, which is used to set
     * the known state.
     */
    public static final int EXACT = 2;

    /**
     * Constant representing "indirect feedback". In this case, the layout
     * hardware can only sense one setting of the turnout. The known state is
     * inferred from that info.
     */
    public static final int INDIRECT = 4;  // only one side directly sensed

    /**
     * Constant representing "feedback by monitoring sent commands". In this
     * case, the known state tracks commands seen on the rails or bus.
     */
    public static final int MONITORING = 8;

    /**
     * Constant representing "feedback by monitoring one sensor". The sensor
     * sets the state CLOSED when INACTIVE and THROWN when ACTIVE
     */
    public static final int ONESENSOR = 16;

    /**
     * Constant representing "feedback by monitoring two sensors". The first
     * sensor sets the state THROWN when ACTIVE; the second sensor sets the
     * state CLOSED when ACTIVE.
     */
    public static final int TWOSENSOR = 32;

    /**
     * Constant representing "feedback for signals" . This is DIRECT feedback,
     * with minimal delay (for use with systems that wait for responses returned
     * by from the command station).
     */
    public static final int SIGNAL = 64;

    /**
     * Get a representation of the feedback type. This is the OR of possible
     * values: DIRECT, EXACT, etc. The valid combinations depend on the
     * implemented system.
     */
    public int getValidFeedbackTypes();

    /**
     * Get a human readable representation of the feedback type. The values
     * depend on the implemented system.
     */
    public String[] getValidFeedbackNames();

    /**
     * Set the feedback mode from a human readable name. This must be one of the
     * names defined in a previous {@link #getValidFeedbackNames} call.
     */
    public void setFeedbackMode(String mode) throws IllegalArgumentException;

    /**
     * Set the feedback mode from a integer. This must be one of the bit values
     * defined in a previous {@link #getValidFeedbackTypes} call. Having more
     * than one bit set is an error.
     */
    public void setFeedbackMode(int mode) throws IllegalArgumentException;

    /**
     * Get the feedback mode in human readable form. This will be one of the
     * names defined in a {@link #getValidFeedbackNames} call.
     */
    public String getFeedbackModeName();

    /**
     * Get the feedback mode in machine readable form. This will be one of the
     * bits defined in a {@link #getValidFeedbackTypes} call.
     */
    public int getFeedbackMode();

    /**
     * Get the indicator for whether automatic operation (retry) has been
     * inhibited for this turnout
     */
    public boolean getInhibitOperation();

    /**
     * Change the value of the inhibit operation indicator
     *
     * @param io
     */
    public void setInhibitOperation(boolean io);

    /**
     * @return current operation automation class
     */
    public TurnoutOperation getTurnoutOperation();

    /**
     * set current automation class
     *
     * @param toper TurnoutOperation subclass instance
     */
    public void setTurnoutOperation(TurnoutOperation toper);

    /**
     * Provide Sensor objects needed for some feedback types.
     *
     * Since we defined two feeedback methods that require monitoring, we
     * provide these methods to define those sensors to the Turnout.
     * <P>
     * The second sensor can be null if needed.
     * <P>
     * Sensor-based feedback will not function until these sensors have been
     * provided.
     */
    //public void provideFirstFeedbackSensor(NamedBeanHandle<Sensor> s);
    public void provideFirstFeedbackSensor(String pName) throws JmriException;

    public void provideSecondFeedbackSensor(String pName) throws JmriException;

    /**
     * Get the first sensor, if defined.
     * <P>
     * Returns null if no Sensor recorded.
     */
    public Sensor getFirstSensor();

    /**
     * Get the first sensor, if defined.
     * <P>
     * Returns null if no Sensor recorded.
     */
    public NamedBeanHandle<Sensor> getFirstNamedSensor();

    /**
     * Get the Second sensor, if defined.
     * <P>
     * Returns null if no Sensor recorded.
     */
    public Sensor getSecondSensor();

    /**
     * Get the first sensor, if defined.
     * <P>
     * Returns null if no Sensor recorded.
     */
    public NamedBeanHandle<Sensor> getSecondNamedSensor();

    /**
     * Sets the initial known state (CLOSED,THROWN,UNKNOWN) from feedback
     * information, if appropriate.
     * <P>
     * This method is designed to be called only when Turnouts are loaded and
     * when a new Turnout is defined in the Turnout table.
     * <P>
     * No change to known state is made if feedback information is not
     * available. If feedback information is inconsistent, or if sensor
     * definition is missing in ONESENSOR and TWOSENSOR feedback, turnout state
     * is set to UNKNOWN.
     */
    public void setInitialKnownStateFromFeedback();

    /**
     * Get number of output bits.
     * <P>
     * Currently must be one or two.
     */
    public int getNumberOutputBits();

    /**
     * Set number of output bits.
     * <P>
     * Currently must be one or two.
     */
    public void setNumberOutputBits(int num);

    /**
     * Get control type.
     * <P>
     * Currently must be either 0 for steady state, or n for pulse for n time
     * units.
     */
    public int getControlType();

    /**
     * Set control type.
     * <P>
     * Currently must be either 0 for steady state, or n for pulse for n time
     * units.
     */
    public void setControlType(int num);

    /**
     * Get turnout inverted
     * <P>
     * If true commands are reversed to layout
     */
    public boolean getInverted();

    /**
     * Set turnout inverted
     * <P>
     * If true commands are reversed to layout.
     * <p>
     * Changing this changes the known state from CLOSED to THROWN and
     * vice-versa, with notifications; UNKNOWN and INCONSISTENT are left
     * unchanged, as is the commanded state.
     */
    public void setInverted(boolean inverted);

    /**
     * Determine if turnout can be inverted
     * <P>
     * If true turnouts can be inverted
     */
    public boolean canInvert();

    /**
     * Constant representing turnout lockout cab commands
     */
    public static final int CABLOCKOUT = 1;

    /**
     * Constant representing turnout lockout pushbuttons
     */
    public static final int PUSHBUTTONLOCKOUT = 2;

    /**
     * Constant representing a unlocked turnout
     */
    public static final int UNLOCKED = 0;

    /**
     * Constant representing a locked turnout
     */
    public static final int LOCKED = 1;

    /**
     * Get turnout locked
     * <P>
     * If true turnout is locked, must specify which type of lock, will return
     * true if both tested and either type is locked.
     */
    public boolean getLocked(int turnoutLockout);

    /**
     * Enable turnout lock operators.
     * <P>
     * If true the type of lock specified is enabled.
     */
    public void enableLockOperation(int turnoutLockout, boolean locked);

    /**
     * Determine if turnout can be locked. Must specify the type of lock.
     * <P>
     * If true turnouts can be locked.
     */
    public boolean canLock(int turnoutLockout);

    /**
     * Lock a turnout. Must specify the type of lock.
     * <P>
     * If true turnout is to be locked.
     */
    public void setLocked(int turnoutLockout, boolean locked);

    /**
     * Determine if we should send a message to console when we detect that a
     * turnout that is locked has been accessed by a cab on the layout. If true,
     * report cab attempt to change turnout.
     */
    public boolean getReportLocked();

    /**
     * Set turnout report
     * <P>
     * If true report any attempts by a cab to modify turnout state
     */
    public void setReportLocked(boolean reportLocked);

    /**
     * Get a human readable representation of the decoder types.
     */
    public String[] getValidDecoderNames();

    /**
     * Get a human readable representation of the decoder type for this turnout.
     */
    public String getDecoderName();

    /**
     * Set a human readable representation of the decoder type for this turnout.
     */
    public void setDecoderName(String decoderName);

    /**
     * Turn this object into just a binary output.
     */
    public void setBinaryOutput(boolean state);

    public float getDivergingLimit();

    public String getDivergingSpeed();

    public void setDivergingSpeed(String s) throws JmriException;

    public float getStraightLimit();

    public String getStraightSpeed();

    public void setStraightSpeed(String s) throws JmriException;

}
