// Managed.java

package jmri;

/**
 * The Managed interface provides common services for classes representing objects
 * on the layout, and allows the of common code by their Managers.
 * <P>
 * Each object has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 * <P>
 * The AbstractManager class contains a basic implementation of the state and messaging
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
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version	$Revision: 1.7 $
 * @see         jmri.AbstractTurnout
 * @see         jmri.TurnoutManager
 * @see         jmri.InstanceManager
 * @see         jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 */
public interface Turnout extends NamedBean {

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
	public void setCommandedState(int s);

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
}


/* @(#)Turnout.java */
