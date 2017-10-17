package jmri.implementation;

import java.util.Arrays;
import jmri.SignalHead;
import jmri.Turnout;

/**
 * Abstract class providing the basic logic of the SignalHead interface.
 * <p>
 * SignalHead system names are always upper case.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class AbstractSignalHead extends AbstractNamedBean
        implements SignalHead, java.beans.VetoableChangeListener {

    public AbstractSignalHead(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractSignalHead(String systemName) {
        super(systemName);
    }

    @Override
    public String getAppearanceName(int appearance) {
        String ret = jmri.util.StringUtil.getNameFromState(
                appearance, getValidStates(), getValidStateNames());
        if (ret != null) {
            return ret;
        } else {
            return ("");
        }
    }

    @Override
    public String getAppearanceName() {
        return getAppearanceName(getAppearance());
    }

    protected int mAppearance = DARK;

    @Override
    public int getAppearance() {
        return mAppearance;
    }

    /**
     * Determine whether this signal shows an aspect or appearance
     * that allows travel past it, e.g. it's "been cleared".
     * This might be a yellow or green appearance, or an Approach or Clear
     * aspect
     */
    public boolean isCleared() { return !isAtStop() && !isShowingRestricting() && getAppearance()!=DARK; }

    /**
     * Determine whether this signal shows an aspect or appearance
     * that allows travel past it only at restricted speed.
     * This might be a flashing red appearance, or a 
     * Restricting aspect.
     */
    public boolean isShowingRestricting() { return getAppearance() == FLASHRED || getAppearance() == LUNAR || getAppearance() == FLASHLUNAR; }
    
    /**
     * Determine whether this signal shows an aspect or appearance
     * that forbid travel past it.
     * This might be a red appearance, or a 
     * Stop aspect. Stop-and-Proceed or Restricting would return false here.
     */
    public boolean isAtStop()  { return getAppearance() == RED; }


    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //  public void firePropertyChange(String propertyName,
    //      Object oldValue,
    //      Object newValue)
    // _once_ if anything has changed state
    /**
     * By default, signals are lit.
     */
    protected boolean mLit = true;

    /**
     * Default behavior for "lit" parameter is to track value and return it.
     *
     * @return is lit
     */
    @Override
    public boolean getLit() {
        return mLit;
    }

    /**
     * By default, signals are not held.
     */
    protected boolean mHeld = false;

    /**
     * "Held" parameter is just tracked and notified.
     * @return is held
     */
    @Override
    public boolean getHeld() {
        return mHeld;
    }

    /**
     * Implement a shorter name for setAppearance.
     * <p>
     * This generally shouldn't be used by Java code; use setAppearance instead.
     * The is provided to make Jython script access easier to read.
     * @param s new state
     */
    @Override
    public void setState(int s) {
        setAppearance(s);
    }

    /**
     * Implement a shorter name for getAppearance.
     * <p>
     * This generally shouldn't be used by Java code; use getAppearance instead.
     * The is provided to make Jython script access easier to read.
     * @return current state
     */
    @Override
    public int getState() {
        return getAppearance();
    }

    public static int[] getDefaultValidStates() {
        return Arrays.copyOf(validStates, validStates.length);
    }

    public static String[] getDefaultValidStateNames() {
        return Arrays.copyOf(validStateNames, validStateNames.length);
    }

    /**
     * Get a localized text describing appearance from the corresponding state index.
     *
     * @param appearance the index of the appearance
     * @return translated name for appearance
     */
    public static String getDefaultStateName(int appearance) {
        String ret = jmri.util.StringUtil.getNameFromState(
                appearance, getDefaultValidStates(), getDefaultValidStateNames());
        if (ret != null) {
            return ret;
        } else {
            return ("");
        }
    }

    private static final int[] validStates = new int[]{
        DARK,
        RED,
        YELLOW,
        GREEN,
        LUNAR,
        FLASHRED,
        FLASHYELLOW,
        FLASHGREEN,
        FLASHLUNAR
    };
    private static final String[] validStateNames = new String[]{
        Bundle.getMessage("SignalHeadStateDark"),
        Bundle.getMessage("SignalHeadStateRed"),
        Bundle.getMessage("SignalHeadStateYellow"),
        Bundle.getMessage("SignalHeadStateGreen"),
        Bundle.getMessage("SignalHeadStateLunar"),
        Bundle.getMessage("SignalHeadStateFlashingRed"),
        Bundle.getMessage("SignalHeadStateFlashingYellow"),
        Bundle.getMessage("SignalHeadStateFlashingGreen"),
        Bundle.getMessage("SignalHeadStateFlashingLunar"),};

    @Override
    public int[] getValidStates() {
        return Arrays.copyOf(validStates, validStates.length); // includes int for Lunar
    }

    @Override
    public String[] getValidStateNames() {
        return Arrays.copyOf(validStateNames, validStateNames.length); // includes Lunar
    }

    /**
     * Check if a given turnout is used on this head.
     *
     * @param t Turnout object to check
     * @return true if turnout is configured as output or driver of head
     */
    abstract boolean isTurnoutUsed(Turnout t);

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            if (isTurnoutUsed((Turnout) evt.getOldValue())) {
                java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);
                throw new java.beans.PropertyVetoException(Bundle.getMessage("InUseTurnoutSignalHeadVeto", getDisplayName()), e); //IN18N
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) {
            log.warn("not clear DoDelete operated? {}", getSystemName()); //NOI18N
        }
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalHead");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractSignalHead.class);
}
