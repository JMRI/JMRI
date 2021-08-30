package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.SystemConnectionMemo;
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
        InstanceManager.getDefault(TurnoutOperationManager.class); // force creation of an instance
        init();
    }
    
    final void init() {
        InstanceManager.getDefault(SensorManager.class).addVetoableChangeListener(this);
        // set listener for changes in memo
        memo.addPropertyChangeListener(pcl);
    }
    
    final PropertyChangeListener pcl = (PropertyChangeEvent e) -> {
        if (e.getPropertyName().equals(SystemConnectionMemo.INTERVAL)) {
            handleIntervalChange((Integer) e.getNewValue());
        }
    };

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
    @Nonnull
    public Turnout provideTurnout(@Nonnull String name) {
        log.debug("provide turnout {}", name);
        Turnout result = getTurnout(name);
        return result == null ? newTurnout(makeSystemName(name, true), null) : result;
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
    @Nonnull
    public Turnout newTurnout(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException {
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was " + ((userName == null) ? "null" : userName));  // NOI18N
        // add normalize? see AbstractSensor
        log.debug("newTurnout: {};{}", systemName, userName);

        // is system name in correct format?
        if (!systemName.startsWith(getSystemNamePrefix())
                || !(systemName.length() > (getSystemNamePrefix()).length())) {
            log.error("Invalid system name for Turnout: {} needed {}{} followed by a suffix",
                    systemName, getSystemPrefix(), typeLetter());
            throw new IllegalArgumentException("Invalid system name for Turnout: " + systemName
                    + " needed " + getSystemNamePrefix());
        }

        // return existing if there is one
        Turnout t;
        if (userName != null) {
            t = getByUserName(userName);
            if (t != null) {
                if (getBySystemName(systemName) != t) {
                    log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})",
                        userName, systemName, t.getSystemName());
                }
            return t;
            }
        }
        t = getBySystemName(systemName);
        if (t != null) {
            if ((t.getUserName() == null) && (userName != null)) {
                t.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found turnout via system name ({}) with non-null user name ({}). Turnout \"{} ({})\" cannot be used.",
                        systemName, t.getUserName(), systemName, userName);
            }
            return t;
        }

        // doesn't exist, make a new one
        t = createNewTurnout(systemName, userName);
        // if that failed, will throw an IllegalArgumentException

        // Some implementations of createNewTurnout() register the new bean,
        // some don't. 
        if (getBySystemName(t.getSystemName()) == null) {
            // save in the maps if successful
            register(t);
        }

        try {
            t.setStraightSpeed("Global");
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }

        try {
            t.setDivergingSpeed("Global");
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }
        return t;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getClosedText() {
        return Bundle.getMessage("TurnoutStateClosed");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
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
     * an existing Turnout has been invoked.
     *
     * @param systemName the system name to use for the new Turnout
     * @param userName   the user name to use for the new Turnout
     * @return the new Turnout or
     * @throws IllegalArgumentException if unsuccessful
     */
    @Nonnull
    abstract protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException;

    /** {@inheritDoc} */
    @Override
    @Nonnull
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

    /**
     * Default Turnout ensures a numeric only system name.
     * {@inheritDoc} 
     */
    @Nonnull
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        return prefix + typeLetter() + checkNumeric(curAddress);
    }
    
    private String defaultClosedSpeed = "Normal";
    private String defaultThrownSpeed = "Restricted";

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
                } catch (IllegalArgumentException ex) {
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
                } catch (IllegalArgumentException ex) {
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
        return Bundle.getMessage("EnterNumber1to9999ToolTip");
    }

    private void handleIntervalChange(int newVal) {
        turnoutInterval = newVal;
        log.debug("in memo turnoutInterval changed to {}", turnoutInterval);
    }

    /** {@inheritDoc} */
    @Override
    public int getOutputInterval() {
        return turnoutInterval;
    }

    /** {@inheritDoc} */
    @Override
    public void setOutputInterval(int newInterval) {
        memo.setOutputInterval(newInterval);
        turnoutInterval = newInterval; // local field will hear change and update automatically?
        log.debug("turnoutInterval set to: {}", newInterval);
    }

    /**
     * Duration in milliseconds of interval between separate Turnout commands on the same connection.
     * <p>
     * Change from e.g. XNetTurnout extensions and scripts using {@link #setOutputInterval(int)}
     */
    private int turnoutInterval = memo.getOutputInterval();
    private LocalDateTime waitUntil = LocalDateTime.now();

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public LocalDateTime outputIntervalEnds() {
        log.debug("outputIntervalEnds called in AbstractTurnoutManager");
        if (waitUntil.isAfter(LocalDateTime.now())) {
            waitUntil = waitUntil.plus(turnoutInterval, ChronoUnit.MILLIS);
        } else {
            waitUntil = LocalDateTime.now().plus(turnoutInterval, ChronoUnit.MILLIS); // default interval = 250 Msec
        }
        return waitUntil;
    }
    
    @Override
    public void dispose(){
        memo.removePropertyChangeListener(pcl);
        InstanceManager.getDefault(SensorManager.class).removeVetoableChangeListener(this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractTurnoutManager.class);

}
