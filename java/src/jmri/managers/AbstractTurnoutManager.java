package jmri.managers;

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation of a TurnoutManager.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class AbstractTurnoutManager extends AbstractManager<Turnout>
        implements TurnoutManager {

    public AbstractTurnoutManager(SystemConnectionMemo memo) {
        super(memo);
        InstanceManager.getDefault(TurnoutOperationManager.class);		// force creation of an instance
        InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.TURNOUTS;
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'T';
    }

    /** {@inheritDoc} */
    @Override
    public Turnout provideTurnout(@Nonnull String name) {
        Turnout result = getTurnout(name);
        if (result == null) {
            if (name.startsWith(getSystemPrefix() + typeLetter())) {
                result = newTurnout(name, null);
            } else {
                result = newTurnout(makeSystemName(name), null);
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public Turnout getTurnout(@Nonnull String name) {
        Turnout result = getByUserName(name);
        if (result == null) {
            result = getBySystemName(name);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Turnout getBySystemName(@Nonnull String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public Turnout getByUserName(String key) {
        return _tuser.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Turnout newTurnout(@Nonnull String systemName, @CheckForNull String userName) {
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was " + ((userName == null) ? "null" : userName));  // NOI18N

        // add normalize? see AbstractSensor
        log.debug("newTurnout: {};{}", systemName, userName);

        // is system name in correct format?
        if (!systemName.startsWith(getSystemPrefix() + typeLetter())
                || !(systemName.length() > (getSystemPrefix() + typeLetter()).length())) {
            log.error("Invalid system name for turnout: {} needed {}{} followed by a suffix",
                    systemName, getSystemPrefix(), typeLetter());
            throw new IllegalArgumentException("Invalid system name for turnout: " + systemName
                    + " needed " + getSystemPrefix() + typeLetter());
        }

        // return existing if there is one
        Turnout s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})",
                        userName, systemName, s.getSystemName());
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found turnout via system name ({}) with non-null user name ({}). Turnout \"{} ({})\" cannot be used.",
                        systemName, s.getUserName(), systemName, userName);
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewTurnout(systemName, userName);

        // if that failed, blame it on the input arguments
        if (s == null) {
            throw new IllegalArgumentException("Unable to create turnout from " + systemName);
        }

        // Some implementations of createNewTurnout() register the new bean,
        // some don't. 
        if (getBeanBySystemName(s.getSystemName()) == null) {
            // save in the maps if successful
            register(s);
        }

        try {
            s.setStraightSpeed("Global");
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }

        try {
            s.setDivergingSpeed("Global");
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }
        return s;
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public String getClosedText() {
        return Bundle.getMessage("TurnoutStateClosed");
    }

    /** {@inheritDoc} */
    @Override
    public String getThrownText() {
        return Bundle.getMessage("TurnoutStateThrown");
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
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNumControlBitsSupported(@Nonnull String systemName) {
        return false;
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
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isControlTypeSupported(@Nonnull String systemName) {
        return false;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return never null
     */
    abstract protected Turnout createNewTurnout(@Nonnull String systemName, String userName);

    /** {@inheritDoc} */
    @Override
    public String[] getValidOperationTypes() {
        if (jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class) != null) {
            return new String[]{"Sensor", "Raw", "NoFeedback"};
        } else {
            return new String[]{"Sensor", "NoFeedback"};
        }
    }

    /**
     * A temporary method that determines if it is possible to add a range of
     * turnouts in numerical order eg 10 to 30, primarily used to enable/disable the Add
     * range box in the Add new turnout panel.
     *
     * @param systemName configured system connection name
     * @return false as default, unless overridden by implementations as supported
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        try {
            Integer.parseInt(curAddress);
        } catch (java.lang.NumberFormatException ex) {
            log.warn("Hardware Address passed should be a number, was {}", curAddress);
            throw new JmriException("Hardware Address passed should be a number");
        }
        return prefix + typeLetter() + curAddress;
    }

    /** {@inheritDoc} */
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), null, "", true, false);
            return null;
        }

        Turnout t = getBySystemName(tmpSName);
        if (t == null) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        try {
            iName = Integer.parseInt(curAddress);
        } catch (NumberFormatException ex) {
            log.error("Unable to convert {} Hardware Address to a number", curAddress);
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), null, "", true, false);
            return null;
        }
        // The Number of Output Bits of the previous turnout will help determine the next
        // valid address.
        iName = iName + t.getNumberOutputBits();
        // Check to determine if the systemName is in use;
        // return null if it is, otherwise return the next valid address.
        t = getBySystemName(prefix + typeLetter() + iName);
        if (t != null) {
            for (int x = 1; x < 10; x++) {
                iName = iName + t.getNumberOutputBits();
                t = getBySystemName(prefix + typeLetter() + iName);
                if (t == null) {
                    return Integer.toString(iName);
                }
            }
            // feedback when next address is also in use
            log.warn("10 hardware addresses starting at {} already in use. No new Turnouts added", curAddress);
            return null;
        } else {
            return Integer.toString(iName);
        }
    }

    String defaultClosedSpeed = "Normal";
    String defaultThrownSpeed = "Restricted";

    /** {@inheritDoc} */
    @Override
    public void setDefaultClosedSpeed(@Nonnull String speed) throws JmriException {
        Objects.requireNonNull(speed, "Value of requested turnout default closed speed can not be null");

        if (defaultClosedSpeed.equals(speed)) {
            return;
        }
        if (speed.contains("Block")) {
            speed = "Block";
            if (defaultClosedSpeed.equals(speed)) {
                return;
            }
        } else {
            try {
                Float.parseFloat(speed);
            } catch (NumberFormatException nx) {
                try {
                    jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(speed);
                } catch (Exception ex) {
                    throw new JmriException("Value of requested turnout default closed speed is not valid");
                }
            }
        }
        String oldSpeed = defaultClosedSpeed;
        defaultClosedSpeed = speed;
        firePropertyChange("DefaultTurnoutClosedSpeedChange", oldSpeed, speed);
    }

    /** {@inheritDoc} */
    @Override
    public void setDefaultThrownSpeed(@Nonnull String speed) throws JmriException {
        Objects.requireNonNull(speed, "Value of requested turnout default thrown speed can not be null");

        if (defaultThrownSpeed.equals(speed)) {
            return;
        }
        if (speed.contains("Block")) {
            speed = "Block";
            if (defaultThrownSpeed.equals(speed)) {
                return;
            }

        } else {
            try {
                Float.parseFloat(speed);
            } catch (NumberFormatException nx) {
                try {
                    jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(speed);
                } catch (Exception ex) {
                    throw new JmriException("Value of requested turnout default thrown speed is not valid");
                }
            }
        }
        String oldSpeed = defaultThrownSpeed;
        defaultThrownSpeed = speed;
        firePropertyChange("DefaultTurnoutThrownSpeedChange", oldSpeed, speed);
    }

    /** {@inheritDoc} */
    @Override
    public String getDefaultThrownSpeed() {
        return defaultThrownSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public String getDefaultClosedSpeed() {
        return defaultClosedSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return "Enter a number from 1 to 9999"; // Basic number format help
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractTurnoutManager.class);

}
