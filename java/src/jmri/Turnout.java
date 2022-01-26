package jmri;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

/**
 * Represent a Turnout on the layout.
 * <p>
 * A Turnout has two states:
 * <ul>
 * <li>The "commandedState" records the state that's been commanded in the
 * program. It might take some time, perhaps a long time, for that to actually
 * take effect.
 * <li>The "knownState" is the program's best idea of the actual state on the
 * the layout.
 * </ul>
 * <p>
 * There are a number of reasons that commandedState and knownState differ:
 * <ul>
 * <li>A change has been commanded, but it hasn't had time to happen yet
 * <li>Something has gone wrong, and a commanded change isn't actually going to
 * happen
 * <li>Although the program hasn't commanded a change, something on the layout
 * has made the turnout change. This could be a local electrical button, a
 * mechanical movement of the points, or something else.
 * <li>For a bus-like system, e.g. LocoNet or XpressNet, some other device might
 * have sent a command to change the turnout.
 * </ul>
 * <p>
 * Turnout feedback is involved in the connection between these two states; for
 * more information see the
 * <a href="http://jmri.org/help/en/html/doc/Technical/TurnoutFeedback.shtml">feedback
 * page</a>.
 * <p>
 * The AbstractTurnout class contains a basic implementation of the state and
 * messaging code, and forms a useful start for a system-specific
 * implementation. Specific implementations, e.g. for
 * LocoNet and NCE, will convert to and from the layout commands.
 * <p>
 * The states and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <p>
 * A sample use of the Turnout interface can be seen in the
 * jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame class, which provides a
 * simple GUI for controlling a single turnout.
 * <p>
 * Each Turnout object has a two names. The "user" name is entirely free form,
 * and can be used for any purpose. The "system" name is provided by the
 * system-specific implementations, and provides a unique mapping to the layout
 * control system (for example LocoNet or NCE) and address within that system.
 * <p>
 * Turnouts exhibit some complex behaviors. At the same time, they are sometimes
 * used as generic binary outputs where those get in the way. Eventually, we
 * need to have a separate e.g. Output class, but for now you can defeat much of
 * the advanced behaviors with the setBinaryOutput(true) method. This is a
 * configuration property; changing it on the fly may give unexpected results.
 * It's value is not persisted.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @see jmri.TurnoutManager
 * @see jmri.InstanceManager
 * @see jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 */
public interface Turnout extends DigitalIO {

    /**
     * Constant representing a "closed" state, either in readback or as a
     * commanded state. Note that it's possible to be both CLOSED and THROWN at
     * the same time on some systems, which should be called INCONSISTENT
     */
    public static final int CLOSED = DigitalIO.ON;

    /**
     * Constant representing a "thrown" state, either in readback or as a
     * commanded state. Note that it's possible to be both CLOSED and THROWN at
     * the same time on some systems, which should be called INCONSISTENT
     */
    public static final int THROWN = DigitalIO.OFF;

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
     * Constant representing "automatic delayed feedback" . This is DIRECT feedback
     * with a fixed delay before the feedback (known state) takes effect.
     */
    public static final int DELAYED = 128;

    /**
     * Constant representing "loconet alternate feedback method". In this case, the layout
     * hardware can sense both positions of the turnout, which is used to set
     * the known state. Hardware use OPS_SW_REP alternate message.
     */
    public static final int LNALTERNATE = 256;

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
     * Get a list of valid feedback types. The valid types depend on the
     * implemented system.
     *
     * @return array of feedback types
     */
    public Set<Integer> getValidFeedbackModes();

    /**
     * Get a representation of the feedback type. This is the OR of possible
     * values: DIRECT, EXACT, etc. The valid combinations depend on the
     * implemented system.
     *
     * @return the ORed combination of feedback types
     */
    public int getValidFeedbackTypes();

    /**
     * Get a human readable representation of the feedback type. The values
     * depend on the implemented system.
     *
     * @return the names of the feedback types or an empty list if no feedback
     *         is available
     */
    @Nonnull
    public String[] getValidFeedbackNames();

    /**
     * Set the feedback mode from a human readable name. This must be one of the
     * names defined in a previous {@link #getValidFeedbackNames} call.
     *
     * @param mode the feedback type name
     * @throws IllegalArgumentException if mode is not valid
     */
    @InvokeOnLayoutThread
    public void setFeedbackMode(@Nonnull String mode) throws IllegalArgumentException;

    /**
     * Set the feedback mode from a integer. This must be one of the bit values
     * defined in a previous {@link #getValidFeedbackTypes} call. Having more
     * than one bit set is an error.
     *
     * @param mode the feedback type to set
     * @throws IllegalArgumentException if mode is not valid
     */
    @InvokeOnLayoutThread
    public void setFeedbackMode(int mode) throws IllegalArgumentException;

    /**
     * Get the feedback mode in human readable form. This will be one of the
     * names defined in a {@link #getValidFeedbackNames} call.
     *
     * @return the feedback type
     */
    @Nonnull
    public String getFeedbackModeName();

    /**
     * Get the feedback mode in machine readable form. This will be one of the
     * bits defined in a {@link #getValidFeedbackTypes} call.
     *
     * @return the feedback type
     */
    public int getFeedbackMode();

    /**
     * Get if automatically retrying an operation is blocked for this turnout.
     *
     * @return true if retrying is disabled; false otherwise
     */
    public boolean getInhibitOperation();

    /**
     * Set if automatically retrying an operation is blocked for this turnout.
     *
     * @param io true if retrying is to be disabled; false otherwise
     */
    public void setInhibitOperation(boolean io);

    /**
     * @return current operation automation class
     */
    @CheckForNull
    public TurnoutOperation getTurnoutOperation();

    /**
     * set current automation class
     *
     * @param toper TurnoutOperation subclass instance
     */
    @InvokeOnLayoutThread
    public void setTurnoutOperation(@CheckForNull TurnoutOperation toper);

    /**
     * Return the inverted state of the specified state
     * Does NOT invert INCONSISTENT
     * @param inState the specified state
     * @return the inverted state
     */
    public static int invertTurnoutState(int inState) {
        int result = UNKNOWN;
        if (inState == CLOSED) {
            result = THROWN;
        } else if (inState == THROWN){
            result = CLOSED;
        } else if (inState == INCONSISTENT){
            result = INCONSISTENT;
        }
        return result;
    }

    /**
     * Provide Sensor objects needed for some feedback types.
     *
     * Since we defined two feedback methods that require monitoring, we provide
     * these methods to define those sensors to the Turnout.
     * <p>
     * The second sensor can be null if needed.
     * <p>
     * Sensor-based feedback will not function until these sensors have been
     * provided.
     *
     * @param name the user or system name of the sensor
     * @param number the feedback number of the sensor, indexed from 0
     * @throws jmri.JmriException if unable to assign the feedback sensor
     */
    public default void provideFeedbackSensor(@CheckForNull String name, int number) throws JmriException {
        switch (number) {
            case 0:
                provideFirstFeedbackSensor(name);
                break;
            case 1:
                provideSecondFeedbackSensor(name);
                break;
            default:
                throw new IllegalArgumentException("Turnouts have no more than two sensors");
        }
    }

    public void provideFirstFeedbackSensor(@CheckForNull String pName) throws JmriException;

    public void provideSecondFeedbackSensor(@CheckForNull String pName) throws JmriException;

    /**
     * Get the first feedback sensor.
     *
     * @return the sensor or null if no Sensor set
     */
    @CheckForNull
    public Sensor getFirstSensor();

    /**
     * Get the handle for the first feedback sensor.
     *
     * @return the sensor handle or null if no Sensor set
     */
    @CheckForNull
    public NamedBeanHandle<Sensor> getFirstNamedSensor();

    /**
     * Get the second feedback sensor.
     *
     * @return the sensor or null if no Sensor set
     */
    @CheckForNull
    public Sensor getSecondSensor();

    /**
     * Get the second feedback sensor handle.
     *
     * @return the sensor handle or null if no Sensor set
     */
    @CheckForNull
    public NamedBeanHandle<Sensor> getSecondNamedSensor();

    /**
     * Sets the initial known state (CLOSED,THROWN,UNKNOWN) from feedback
     * information, if appropriate.
     * <p>
     * This method is designed to be called only when Turnouts are loaded and
     * when a new Turnout is defined in the Turnout table.
     * <p>
     * No change to known state is made if feedback information is not
     * available. If feedback information is inconsistent, or if sensor
     * definition is missing in ONESENSOR and TWOSENSOR feedback, turnout state
     * is set to UNKNOWN.
     */
    @InvokeOnLayoutThread
    public void setInitialKnownStateFromFeedback();

    /**
     * Get number of output bits.
     *
     * @return the size of the output, currently 1 or 2
     */
    public int getNumberOutputBits();

    /**
     * Set number of output bits.
     *
     * @param num the size of the output, currently 1 or 2
     */
    @InvokeOnLayoutThread
    public void setNumberOutputBits(int num);

    /**
     * Get control type.
     *
     * @return 0 for steady state or the number of time units the control pulses
     */
    public int getControlType();

    /**
     * Set control type.
     *
     * @param num 0 for steady state or the number of time units the control
     *            pulses
     */
    @InvokeOnLayoutThread
    public void setControlType(int num);

    /**
     * Get turnout inverted. When a turnout is inverted the {@link #CLOSED} and
     * {@link #THROWN} states are reversed on the layout.
     *
     * @return true if inverted; false otherwise
     */
    public boolean getInverted();

    /**
     * Get turnout inverted. When a turnout is inverted the {@link #CLOSED} and
     * {@link #THROWN} states are reversed on the layout.
     *
     * @param inverted true if inverted; false otherwise
     */
    public void setInverted(boolean inverted);

    /**
     * Determine if turnout can be inverted. When a turnout is inverted the
     * {@link #CLOSED} and {@link #THROWN} states are inverted on the layout.
     *
     * @return true if can be inverted; false otherwise
     */
    public boolean canInvert();

    /**
     * Get the locked state of the turnout. A turnout can be locked to prevent
     * it being thrown from a cab or push button on the layout if supported by
     * the protocol.
     *
     * @param turnoutLockout the type of lock
     * @return true if turnout is locked using specified lock method
     */
    public boolean getLocked(int turnoutLockout);

    /**
     * Enable turnout lock operators. A turnout can be locked to prevent it
     * being thrown from a cab or push button on the layout if supported by the
     * protocol.
     *
     * @param turnoutLockout the type of lock
     * @param locked         true if locking is enabled for the given type;
     *                       false otherwise
     */
    @InvokeOnLayoutThread
    public void enableLockOperation(int turnoutLockout, boolean locked);

    /**
     * Determine if turnout can be locked as currently configured. A turnout can be locked to prevent it
     * being thrown from a cab or push button on the layout if supported by the
     * protocol.
     *
     * @param turnoutLockout the type of lock, one of CABLOCKOUT, PUSHBUTTONLOCKOUT
     * or BOTH = CABLOCKOUT | PUSHBUTTONLOCKOUT
     * @return true if turnout is locked using specified lock method; false
     *         otherwise
     */
    public boolean canLock(int turnoutLockout);

    /**
     * Provide the possible locking modes for a turnout.  
     * These may require additional configuration, e.g. 
     * setting of a decoder definition for PUSHBUTTONLOCKOUT,
     * before {@link #canLock(int)} will return true.
     *
     * @return One of 0 for none, CABLOCKOUT, PUSHBUTTONLOCKOUT
     * or CABLOCKOUT | PUSHBUTTONLOCKOUT for both
     */
    public int getPossibleLockModes();

    /**
     * Lock a turnout. A turnout can be locked to prevent it being thrown from a
     * cab or push button on the layout if supported by the protocol.
     *
     * @param turnoutLockout the type of lock
     * @param locked         true if turnout is locked using specified lock
     *                       method; false otherwise
     */
    @InvokeOnLayoutThread
    public void setLocked(int turnoutLockout, boolean locked);

    /**
     * Get reporting of use of locked turnout by a cab or throttle.
     *
     * @return true to report; false otherwise
     */
    public boolean getReportLocked();

    /**
     * Set reporting of use of locked turnout by a cab or throttle.
     *
     * @param reportLocked true to report; false otherwise
     */
    @InvokeOnLayoutThread
    public void setReportLocked(boolean reportLocked);

    /**
     * Get a human readable representation of the decoder types.
     *
     * @return a list of known stationary decoders that can be specified for locking
     */
    @Nonnull
    public String[] getValidDecoderNames();

    /**
     * Get a human readable representation of the locking decoder type for this turnout.
     *
     * In AbstractTurnout this String defaults to PushbuttonPacket.unknown , ie "None"
     * @return the name of the decoder type; null indicates none defined
     */
    @CheckForNull
    public String getDecoderName();

    /**
     * Set a human readable representation of the locking decoder type for this turnout.
     *
     * @param decoderName the name of the decoder type
     */
    public void setDecoderName(@CheckForNull String decoderName);

    /**
     * Use a binary output for sending commands. This appears to expose a
     * LocoNet-specific feature.
     *
     * @param state true if the outputs are binary; false otherwise
     */
    @InvokeOnLayoutThread
    public void setBinaryOutput(boolean state);

    public float getDivergingLimit();

    public String getDivergingSpeed();

    public void setDivergingSpeed(String s) throws JmriException;

    public float getStraightLimit();

    public String getStraightSpeed();

    public void setStraightSpeed(String s) throws JmriException;

    /**
     * Check if this Turnout can follow the state of another Turnout.
     *
     * @return true if this Turnout is capable of following; false otherwise
     */
    // Note: not `canFollow()` to allow JavaBeans introspection to find
    // the property "canFollow"
    public boolean isCanFollow();

    /**
     * Get the Turnout this Turnout is following.
     *
     * @return the leading Turnout or null if none; null if
     *         {@link #isCanFollow()} is false
     */
    @CheckForNull
    public Turnout getLeadingTurnout();

    /**
     * Set the Turnout this Turnout will follow.
     * <p>
     * It is valid for two or more turnouts to follow each other in a circular
     * pattern.
     * <p>
     * It is recommended that a following turnout's feedback mode be
     * {@link #DIRECT}.
     * <p>
     * It is recommended to explicitly call
     * {@link #setFollowingCommandedState(boolean)} after calling this method or
     * to use {@link #setLeadingTurnout(jmri.Turnout, boolean)} to ensure this
     * Turnout follows the leading Turnout in the expected manner.
     *
     * @param turnout the leading Turnout or null if this Turnout should not
     *                follow another Turnout; silently ignored if
     *                {@link #isCanFollow()} is false
     */
    public void setLeadingTurnout(@CheckForNull Turnout turnout);

    /**
     * Set both the leading Turnout and if the commanded state of the leading
     * Turnout is followed. This is a convenience method for calling both
     * {@link #setLeadingTurnout(jmri.Turnout)} and
     * {@link #setFollowingCommandedState(boolean)}.
     *
     * @param turnout                 the leading Turnout or null if this
     *                                Turnout should not follow another Turnout;
     *                                silently ignored if {@link #isCanFollow()}
     *                                is false
     * @param followingCommandedState true to have all states match leading
     *                                turnout; false to only have non-commanded
     *                                states match
     */
    public void setLeadingTurnout(@CheckForNull Turnout turnout, boolean followingCommandedState);

    /**
     * Check if this Turnout is following all states or only the non-commanded
     * states of the leading Turnout.
     *
     * @return true if following all states; false otherwise
     */
    public boolean isFollowingCommandedState();

    /**
     * Set if this Turnout follows all states or only the non-commanded states
     * of the leading Turnout.
     * <p>
     * A Turnout can be commanded to be {@link #THROWN} or {@link #CLOSED}, but
     * can also have additional states {@link #INCONSISTENT} and
     * {@link #UNKNOWN}. There are some use cases where a following Turnout
     * should match all states of the leading Turnout, in which case this should
     * be true, but there are also use cases where the following Turnout should
     * only match the INCONSISTENT and UNKNOWN states of the leading Turnout,
     * but should otherwise be independently commanded, in which case this
     * should be false.
     *
     * @param following true to have all states match leading turnout; false to
     *                  only have non-commanded states match
     */
    public void setFollowingCommandedState(boolean following);

    /**
     * Before setting commanded state, if required by manager, apply wait interval until
     * outputIntervalEnds() to put less pressure on the connection.
     * <p>
     * Used to insert a delay before calling {@link #setCommandedState(int)} to spread out a series of
     * output commands, as in {@link jmri.implementation.MatrixSignalMast#updateOutputs(char[])} and
     * {@link jmri.implementation.DefaultRoute} class SetRouteThread#run().
     * Interval value is kept in the Memo per hardware connection, default = 0
     *
     * @param s turnout state to forward
     */
    public void setCommandedStateAtInterval(int s);

}
