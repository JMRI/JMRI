// TurnoutManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Locate a Turnout object representing some specific turnout on the layout.
 *<P>
 * Turnout objects are obtained from a TurnoutManager, which in turn is generally located
 * from the InstanceManager. A typical call
 * sequence might be:
 *<PRE>
 * Turnout turnout = InstanceManager.turnoutManagerInstance().newTurnout(null,"23");
 *</PRE>
 * <P>
 * Each turnout has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 * <P>
 * Much of the book-keeping is implemented in the AbstractTurnoutManager class, which
 * can form the basis for a system-specific implementation.
 * <P>
 * A sample use of the TurnoutManager interface can be seen in the jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 * class, which provides a simple GUI for controlling a single turnout.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.11 $
 * @see             jmri.Turnout
 * @see             jmri.AbstractTurnoutManager
 * @see             jmri.InstanceManager
 * @see             jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlFrame
 */
public interface TurnoutManager extends Manager {

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new turnout using this as a
     * default name.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    public Turnout provideTurnout(String name);

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new turnout using this as a
     * default name.
     *
     * @param name
     * @return null if no match found
     */
    public Turnout getTurnout(String name);

    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.
     * @return requested Turnout object or null if none exists
     */
    public Turnout getBySystemName(String systemName);

    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists.
     * @return requested Turnout object or null if none exists
     */
    public Turnout getByUserName(String userName);

    /**
     * Return an instance with the specified system and user names.
     * Note that two calls with the same arguments will get the same instance;
     * there is only one Turnout object representing a given physical turnout
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     *<UL>
     *<LI>If a null reference is given for user name, no user name will be associated
     *    with the Turnout object created; a valid system name must be provided
     *<LI>If both names are provided, the system name defines the
     *    hardware access of the desired turnout, and the user address
     *    is associated with it.
     *</UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects.  This is a problem, and we don't have a
     * good solution except to issue warnings.
     * This will mostly happen if you're creating Turnouts when you should
     * be looking them up.
     * @return requested Turnout object (never null)
     */
    public Turnout newTurnout(String systemName, String userName);

    /**
     * Get a list of all Turnouts' system names.
     */
    public List getSystemNameList();

}


/* @(#)TurnoutManager.java */
