// Turnout.java

package jmri;

/**
 * The Turnout interface represents a single Turnout on the layout.  It provides
 * a way to command the state of that turnout, and to inquire about the commanded
 * and known (e.g. read-back from layout) states.
 * <P>
 * Both commanded and known states are bit masks, so that they can represent
 * several possible values at the same time.  Constants are provided for each
 * represented concept. It is possible for a Turnout to be both closed and thrown
 * at the same time, or closed and inconsistent.
 * <P>
 * There are several ways that a hardware system can get information on the known
 * state of the turnout hardware.  Any particular system might only have some
 * of them, and we don't assume that this can be configured via software.  In this
 * version of the interface, its not possible to set this, and its not a bound
 * parameter; no callbacks are available. Rather, we assume that some configuation
 * information reflects the actual hardware that's on the layout.
 * <P>
 * Available feedback mechanisms are:
 * <UL>
 * <LI>NONE - No layout feedback available, only commands originating
 * in the program can be considered.  The KnownState and CommandedState
 * track each other based on commands in the software.
 * <LI>EXACT - The hardware can sense both states of the turnout, so the
 * KnownState is really a known state.  CommandedState changes on command,
 * the KnownState changes when the turnout changes.  Note that in this
 * case KnownState may or may not move through an ICONSISTENT or UNKNOWN
 * state while transitioning.
 * <LI>INDIRECT - The hardware can sense one state of the turnout, and
 * the assumption is made that there are only THROWN and CLOSED states
 * based on that sensing.  CommandedState changes on command, the KnownState
 * changes when the sensed state changes.  Normally this KnownState will
 * not go through an INCONSISTENT or UNKNOWN state when transitioning.
 * <LI>SENT - Commands controlling the turnout can be sensed, so (at least some )
 * commands not originating in the program can be observed.  The CommandedState
 * and KnownState follow commands, including the sensed commands from other sources.
 * Note that this is essentially the same as NONE if there are no other sources.
 * Individual classes should describe which message sources they can hear,
 * as not all are required.
 * </UL>
 * <P>
 * Each turnout has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 * <P>
 * Turnout objects are obtained from a TurnoutManager, which in turn is generally located
 * from the InstanceManager. See TurnoutManager for more information.
 * <P>
 * The AbstractTurnout package contains a basic implementation of the state and messaging
 * code, and forms a useful start for a system-specific implementation.
 * Specific implementations in the jmrix package, e.g. for LocoNet and NCE, will
 * convert to and from the layout commands.
 * <P>
 * The states  and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <P>
 * A sample use of the Turnout interface can be seen in the jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 * class, which provides a simple GUI for controlling a single turnout.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.5 $
 * @see             jmri.AbstractTurnout
 * @see             jmri.TurnoutManager
 * @see             jmri.InstanceManager
 * @see             jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 */
public interface Turnout {

	// user identification, _bound_ parameter so manager(s) can listen
	public String getUserName();
	public void   setUserName(String s);

	/**
	 * Get a system-specific name.  This encodes the hardware addressing
	 * information. Defined formats:
	 *<UL>
	 *<LI>LTnnn  LocoNet turnouts
	 *<LI>NTnnn  NCE turnouts
	 *<LI>XTnnn  XpressNet turnouts
	 *<LI>DTnnn  direct-packet-drive turnouts
	 *</UL>
	 */
	public String getSystemName();

	// states are parameters; 'both closed and thrown' is possible!

	/**
	 * Constant representing an "unknown" state, indicating that the
	 * object's state is not necessarily that of the actual layout hardware.
	 * This is the initial state of a newly created object before
	 * communication with the layout.
	 */
	public static final int UNKNOWN      = 0x01;

	/**
	 * Constant representing an "closed" state, either in readback
	 * or as a commanded state.
	 */
	public static final int CLOSED       = 0x02;

	/**
	 * Constant representing an "thrown" state, either in readback
	 * or as a commanded state.
	 */
	public static final int THROWN       = 0x04;

	/**
	 * Constant representing an "inconsistent" state, indicating that
	 * some inconsistency has been detected in the hardware readback.
	 */
	public static final int INCONSISTENT = 0x08;

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
	public void setCommandedState(int s) throws jmri.JmriException;

	/**
	 * Query the commanded state.  This is a bound parameter, so
	 * you can also register a listener to be informed of changes.
	 */
	public int getCommandedState();

	/**
	 * Constant representing "no feedback method".  In this case,
	 * the commanded state is provided when the known state is requested.
	 */
	public static final int NONE     = 0;

	/**
	 * Constant representing "exact feedback method".  In this case,
	 * the hardware can sense both positions of the turnout, which is
	 * used to set the known state.
	 */
	public static final int EXACT    = 2;

	/**
	 * Constant representing "indirect feedback".  In this case,
	 * the hardware can only sense one setting of the turnout. The known
	 * state is inferred from that info.
	 */
	public static final int INDIRECT = 3;  // only one side directly sensed

	/**
	 * Constant representing "feedback by monitoring sent commands".  In this case,
	 * the known state tracks commands seen on the rails or bus.
	 */
	public static final int SENT     = 4;

	/**
	 * Get a representation of the feedback type.  This is the OR of
	 * possible values: NONE, UNKNOWN, EXACT, INDIRECT, SENT.
	 * The valid combinations depend on the implemented system.
	 */
	public int getFeedbackType();

	/**
	 * Request a call-back when a bound property changes.
	 * Bound properties are the known state, commanded state, user and system names.
	 */
	public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

	/**
	 * Remove a request for a call-back when a bound property changes.
	 */
	public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

	/**
	 * Remove references to and from this object, so that it can
	 * eventually be garbage-collected.
	 */
	public void dispose();  // remove _all_ connections!

}


/* @(#)Turnout.java */
