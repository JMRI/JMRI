package jmri.managers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a TurnoutManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2010
 */
public class ProxyTurnoutManager extends AbstractProvidingProxyManager<Turnout> implements TurnoutManager {

    public ProxyTurnoutManager() {
        super();
    }

    @Override
    protected AbstractManager<Turnout> makeInternalManager() {
        return jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class).getTurnoutManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addManager(@Nonnull Manager<Turnout> m) {
        super.addManager(m);
        InstanceManager.getDefault(TurnoutOperationManager.class).loadOperationTypes();
    }

    /**
     * Locate via user name, then system name if needed.
     *
     * @return Null if nothing by that name exists
     */
    @Override
    public Turnout getTurnout(@Nonnull String name) {
        return super.getNamedBean(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Turnout makeBean(Manager<Turnout> manager, String systemName, String userName) throws IllegalArgumentException {
        return ((TurnoutManager) manager).newTurnout(systemName, userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Turnout provideTurnout(@Nonnull String name) throws IllegalArgumentException {
        return super.provideNamedBean(name);
    }


    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Turnout provide(@Nonnull String name) throws IllegalArgumentException { return provideTurnout(name); }

    /**
     * Get an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Sensor object representing a given physical turnout and
     * therefore only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Turnout object created; a valid system name must be
     * provided
     * <li>If a null reference is given for the system name, a system name will
     * _somehow_ be inferred from the user name. How this is done is system
     * specific. Note: a future extension of this interface will add an
     * exception to signal that this was not possible.
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired turnout, and the user address is associated with
     * it.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Sensors when you should be looking them up.
     *
     * @return requested Turnout object (never null)
     */
    @Override
    @Nonnull
    public Turnout newTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return newNamedBean(systemName, userName);
    }

    /**
     * Get text to be used for the Turnout.CLOSED state in user communication.
     * Allows text other than "CLOSED" to be use with certain hardware system to
     * represent the Turnout.CLOSED state. Defaults to the primary manager. This
     * means that the primary manager sets the terminology used. Note: the
     * primary manager need not override the method in AbstractTurnoutManager if
     * "CLOSED" is the desired terminology.
     */
    @Override
    @Nonnull
    public String getClosedText() {
        return ((TurnoutManager) getDefaultManager()).getClosedText();
    }

    /**
     * Get text to be used for the Turnout.THROWN state in user communication.
     * Allows text other than "THROWN" to be use with certain hardware system to
     * represent the Turnout.THROWN state. Defaults to the primary manager. This
     * means that the primary manager sets the terminology used. Note: the
     * primary manager need not override the method in AbstractTurnoutManager if
     * "THROWN" is the desired terminology.
     */
    @Override
    @Nonnull
    public String getThrownText() {
        return ((TurnoutManager) getDefaultManager()).getThrownText();
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
    @Override
    public int askNumControlBits(@Nonnull String systemName) {
        return ((TurnoutManager) getManagerOrDefault(systemName)).askNumControlBits(systemName);
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
    @Override
    public int askControlType(@Nonnull String systemName) {
        return ((TurnoutManager) getManagerOrDefault(systemName)).askControlType(systemName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isControlTypeSupported(@Nonnull String systemName) {
        return ((TurnoutManager) getManagerOrDefault(systemName)).isControlTypeSupported(systemName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNumControlBitsSupported(@Nonnull String systemName) {
        return ((TurnoutManager) getManagerOrDefault(systemName)).isNumControlBitsSupported(systemName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String[] getValidOperationTypes() {
        List<String> typeList = new LinkedList<>();
        getManagerList().forEach(m -> typeList.addAll(Arrays.asList(((TurnoutManager) m).getValidOperationTypes())));
        return TurnoutOperationManager.concatenateTypeLists(typeList.toArray(new String[0]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return ((TurnoutManager) getManagerOrDefault(systemName)).allowMultipleAdditions(systemName);
    }

    @SuppressWarnings("deprecation") // user warned by actual manager class
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return getNextValidAddress(curAddress, prefix, typeLetter());
    }
    
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws jmri.JmriException {
        return getNextValidAddress(curAddress, prefix, ignoreInitialExisting, typeLetter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultClosedSpeed(@Nonnull String speed) throws jmri.JmriException {
        for (Manager<Turnout> m : getManagerList()) {
            try {
                ((TurnoutManager) m).setDefaultClosedSpeed(speed);
            } catch (jmri.JmriException ex) {
                log.error(ex.toString());
                throw ex;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultThrownSpeed(@Nonnull String speed) throws jmri.JmriException {
        for (Manager<Turnout> m : getManagerList()) {
            try {
                ((TurnoutManager) m).setDefaultThrownSpeed(speed);
            } catch (jmri.JmriException ex) {
                log.error(ex.toString());
                throw ex;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultThrownSpeed() {
        return ((TurnoutManager) getDefaultManager()).getDefaultThrownSpeed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultClosedSpeed() {
        return ((TurnoutManager) getDefaultManager()).getDefaultClosedSpeed();
    }

    /** {@inheritDoc}
     * @return outputInterval from default TurnoutManager
     */
    @Override
    public int getOutputInterval() {
        return ((TurnoutManager) getDefaultManager()).getOutputInterval();
    }

    /**
     * {@inheritDoc}
     * This method is only used in jmri.jmrix.internal.InternalTurnoutManagerTest and should not be
     * used in actual code, as it can overwrite individual per connection values set by the user.
     */
    @Override
    public void setOutputInterval(int newInterval) {
        log.debug("setOutputInterval called in ProxyTurnoutManager");
        // only intended for testing; do not set interval via ProxyTurnoutManager in actual code
        for (Manager<Turnout> manager : getManagerList()) {
            ((TurnoutManager) manager).setOutputInterval(newInterval);
        }
    }

    /**
     * {@inheritDoc}
     * @return end time of latest OutputInterval as LocalDateTime from default TurnoutManager
     */
    @Nonnull
    @Override
    public LocalDateTime outputIntervalEnds() {
        log.debug("outputIntervalEnds called in ProxyTurnoutManager");
        return ((TurnoutManager) getDefaultManager()).outputIntervalEnds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getXMLOrder() {
        return jmri.Manager.TURNOUTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameTurnouts" : "BeanNameTurnout");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Turnout> getNamedBeanClass() {
        return Turnout.class;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ProxyTurnoutManager.class);

}
