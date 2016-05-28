package jmri.managers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a TurnoutManager that can serves as a proxy for multiple
 * system-specific implementations.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2010
 */
public class ProxyTurnoutManager extends AbstractProxyManager implements TurnoutManager {

    public ProxyTurnoutManager() {
        super();
    }

    protected AbstractManager makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getTurnoutManager();
    }

    /**
     * Revise superclass behavior: support TurnoutOperations
     */
    @Override
    public void addManager(Manager m) {
        super.addManager(m);
        TurnoutOperationManager.getInstance().loadOperationTypes();
    }

    /**
     * Locate via user name, then system name if needed.
     *
     * @return Null if nothing by that name exists
     */
    public Turnout getTurnout(String name) {
        return (Turnout) super.getNamedBean(name);
    }

    protected NamedBean makeBean(int i, String systemName, String userName) {
        return ((TurnoutManager) getMgr(i)).newTurnout(systemName, userName);
    }

    public Turnout provideTurnout(String name) {
        return (Turnout) super.provideNamedBean(name);
    }

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @return requested Turnout object or null if none exists
     */
    public Turnout getBySystemName(String systemName) {
        return (Turnout) super.getBeanBySystemName(systemName);
    }

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @return requested Turnout object or null if none exists
     */
    public Turnout getByUserName(String userName) {
        return (Turnout) super.getBeanByUserName(userName);
    }

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Sensor object representing a given physical turnout and
     * therefore only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the Turnout object created; a valid system name must be
     * provided
     * <LI>If a null reference is given for the system name, a system name will
     * _somehow_ be inferred from the user name. How this is done is system
     * specific. Note: a future extension of this interface will add an
     * exception to signal that this was not possible.
     * <LI>If both names are provided, the system name defines the hardware
     * access of the desired turnout, and the user address is associated with
     * it.
     * </UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Sensors when you should be looking them up.
     *
     * @return requested Sensor object (never null)
     */
    public Turnout newTurnout(String systemName, String userName) {
        return (Turnout) newNamedBean(systemName, userName);
    }

    /**
     * Get text to be used for the Turnout.CLOSED state in user communication.
     * Allows text other than "CLOSED" to be use with certain hardware system to
     * represent the Turnout.CLOSED state. Defaults to the primary manager. This
     * means that the primary manager sets the terminology used. Note: the
     * primary manager need not override the method in AbstractTurnoutManager if
     * "CLOSED" is the desired terminology.
     */
    public String getClosedText() {
        return ((TurnoutManager) getMgr(0)).getClosedText();
    }

    /**
     * Get text to be used for the Turnout.THROWN state in user communication.
     * Allows text other than "THROWN" to be use with certain hardware system to
     * represent the Turnout.THROWN state. Defaults to the primary manager. This
     * means that the primary manager sets the terminology used. Note: the
     * primary manager need not override the method in AbstractTurnoutManager if
     * "THROWN" is the desired terminology.
     */
    public String getThrownText() {
        return ((TurnoutManager) getMgr(0)).getThrownText();
    }

    /**
     * Get from the user, the number of addressed bits used to control a
     * turnout. Normally this is 1, and the default routine returns 1
     * automatically. Turnout Managers for systems that can handle multiple
     * control bits should override this method with one which asks the user to
     * specify the number of control bits. If the user specifies more than one
     * control bit, this method should check if the additional bits are
     * available (not assigned to another object). If the bits are not
     * available, this method should return 0 for number of control bits, after
     * informing the user of the problem.
     */
    public int askNumControlBits(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((TurnoutManager) getMgr(i)).askNumControlBits(systemName);
        }
        return ((TurnoutManager) getMgr(0)).askNumControlBits(systemName);
    }

    /**
     * Get from the user, the type of output to be used bits to control a
     * turnout. Normally this is 0 for 'steady state' control, and the default
     * routine returns 0 automatically. Turnout Managers for systems that can
     * handle pulsed control as well as steady state control should override
     * this method with one which asks the user to specify the type of control
     * to be used. The routine should return 0 for 'steady state' control, or n
     * for 'pulsed' control, where n specifies the duration of the pulse
     * (normally in seconds).
     */
    public int askControlType(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((TurnoutManager) getMgr(i)).askControlType(systemName);
        }
        return ((TurnoutManager) getMgr(0)).askControlType(systemName);
    }

    public boolean isControlTypeSupported(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((TurnoutManager) getMgr(i)).isControlTypeSupported(systemName);
        }
        return ((TurnoutManager) getMgr(0)).isControlTypeSupported(systemName);
    }

    public boolean isNumControlBitsSupported(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((TurnoutManager) getMgr(i)).isNumControlBitsSupported(systemName);
        }
        return ((TurnoutManager) getMgr(0)).isNumControlBitsSupported(systemName);
    }

    /**
     * TurnoutOperation support. Return a list which is just the concatenation
     * of all the valid operation types
     */
    public String[] getValidOperationTypes() {
        List<String> typeList = new LinkedList<String>();
        for (int i = 0; i < nMgrs(); ++i) {
            String[] thisTypes = ((TurnoutManager) getMgr(i)).getValidOperationTypes();
            typeList.addAll(Arrays.asList(thisTypes));
        }
        return TurnoutOperationManager.concatenateTypeLists(typeList.toArray(new String[0]));
    }

    public boolean allowMultipleAdditions(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return ((TurnoutManager) getMgr(i)).allowMultipleAdditions(systemName);
        }
        return ((TurnoutManager) getMgr(0)).allowMultipleAdditions(systemName);
    }

    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        for (int i = 0; i < nMgrs(); i++) {
            if (prefix.equals(
                    ((TurnoutManager) getMgr(i)).getSystemPrefix())) {
                try {
                    return ((TurnoutManager) getMgr(i)).createSystemName(curAddress, prefix);
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        throw new jmri.JmriException("Turnout Manager could not be found for System Prefix " + prefix);
    }

    public String getNextValidAddress(String curAddress, String prefix) throws jmri.JmriException {
        for (int i = 0; i < nMgrs(); i++) {
            if (prefix.equals(
                    ((TurnoutManager) getMgr(i)).getSystemPrefix())) {
                try {
                    return ((TurnoutManager) getMgr(i)).getNextValidAddress(curAddress, prefix);
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        return null;
    }

    public void setDefaultClosedSpeed(String speed) throws jmri.JmriException {
        for (int i = 0; i < nMgrs(); i++) {
            try {
                ((TurnoutManager) getMgr(i)).setDefaultClosedSpeed(speed);
            } catch (jmri.JmriException ex) {
                log.error(ex.toString());
                throw ex;
            }
        }
    }

    public void setDefaultThrownSpeed(String speed) throws jmri.JmriException {
        for (int i = 0; i < nMgrs(); i++) {
            try {
                ((TurnoutManager) getMgr(i)).setDefaultThrownSpeed(speed);
            } catch (jmri.JmriException ex) {
                log.error(ex.toString());
                throw ex;
            }
        }
    }

    public String getDefaultThrownSpeed() {
        return ((TurnoutManager) getMgr(0)).getDefaultThrownSpeed();
    }

    public String getDefaultClosedSpeed() {
        return ((TurnoutManager) getMgr(0)).getDefaultClosedSpeed();
    }

    public int getXMLOrder() {
        return jmri.Manager.TURNOUTS;
    }

    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameTurnout");
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ProxyTurnoutManager.class.getName());
}
