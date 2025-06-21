package jmri.implementation;

import java.util.Arrays;

import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.ArrayUtil;
import jmri.util.StringUtil;

import javax.annotation.Nonnull;

/**
 * Abstract class providing the basic logic of the SignalHead interface.
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

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getAppearanceName(int appearance) {
        String ret = jmri.util.StringUtil.getNameFromState(
                appearance, getValidStates(), getValidStateNames());
        if (ret != null) {
            return ret;
        }
        return ("");
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getAppearanceName() {
        return getAppearanceName(getAppearance());
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getAppearanceKey(int appearance) {
        String ret = StringUtil.getNameFromState(
                appearance, getValidStates(), getValidStateKeys());
        if (ret != null) {
            return ret;
        }
        return ("");
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getAppearanceKey() {
        return getAppearanceKey(getAppearance());
    }

    protected int mAppearance = DARK;

    /**
     * {@inheritDoc}
     */
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
    @Override
    public boolean isCleared() { return !isAtStop() && !isShowingRestricting() && getAppearance()!=DARK; }

    /**
     * Determine whether this signal shows an aspect or appearance
     * that allows travel past it only at restricted speed.
     * This might be a flashing red appearance, or a
     * Restricting aspect.
     */
    @Override
    public boolean isShowingRestricting() {
        return getAppearance() == FLASHRED || getAppearance() == LUNAR || getAppearance() == FLASHLUNAR;
    }

    /**
     * Determine whether this signal shows an aspect or appearance
     * that forbid travel past it.
     * This might be a red appearance, or a
     * Stop aspect. Stop-and-Proceed or Restricting would return false here.
     */
    @Override
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
        String[] stateNames = new String[validStateKeys.length];
        int i = 0;
        for (String stateKey : validStateKeys) {
            stateNames[i++] = Bundle.getMessage(stateKey);
        }
        return stateNames;
    }

    /**
     * Get a localized text describing appearance from the corresponding state index.
     *
     * @param appearance the index of the appearance
     * @return translated name for appearance
     */
    @Nonnull
    public static String getDefaultStateName(int appearance) {
        String ret = jmri.util.StringUtil.getNameFromState(
                appearance, getDefaultValidStates(), getDefaultValidStateNames());
        return ( ret != null ? ret : "" );
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

    private static final String[] validStateKeys = new String[]{
        "SignalHeadStateDark",
        "SignalHeadStateRed",
        "SignalHeadStateYellow",
        "SignalHeadStateGreen",
        "SignalHeadStateLunar",
        "SignalHeadStateFlashingRed",
        "SignalHeadStateFlashingYellow",
        "SignalHeadStateFlashingGreen",
        "SignalHeadStateFlashingLunar"
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getValidStates() {
        return Arrays.copyOf(validStates, validStates.length); // includes int for Lunar
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getValidStateKeys() {
        return Arrays.copyOf(validStateKeys, validStateKeys.length); // includes int for Lunar
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getValidStateNames() {
        return getDefaultValidStateNames();
    }

    /**
     * Describe SignalHead state.
     * Does not have to be a valid state for this SignalHead instance hence
     * suitable for state error logging.
     * Can include multiple head states, with exception of DARK,
     * the only state which must exist on its own.
     * Includes the HELD state if present.
     * @see SignalHead#getAppearanceName(int)
     * @param state the state to describe.
     * @return description of state from Bundle.
     */
    @Override
    public String describeState(int state) {
        var bundleStrings = StringUtil.getNamesFromState(state, allStateNumbers, allStateNames);
        StringBuilder sb = new StringBuilder();
        for (String str : bundleStrings) {
            sb.append(Bundle.getMessage(str));
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    // all states, including HELD
    private static final int[] allStateNumbers = ArrayUtil.appendArray(validStates, new int[]{HELD});

    // all state names, including HELD
    private static final String[] allStateNames = ArrayUtil.appendArray(validStateKeys, new String[]{"SignalHeadStateHeld"});

    /**
     * Check if a given turnout is used on this head.
     *
     * @param t Turnout object to check
     * @return true if turnout is configured as output or driver of head
     */
    public abstract boolean isTurnoutUsed(Turnout t);

    /**
     * {@inheritDoc}
     */
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if (jmri.Manager.PROPERTY_CAN_DELETE.equals(evt.getPropertyName())) {
            if (isTurnoutUsed((Turnout) evt.getOldValue())) {
                var e = new java.beans.PropertyChangeEvent(this, jmri.Manager.PROPERTY_DO_NOT_DELETE, null, null);
                throw new java.beans.PropertyVetoException(
                    Bundle.getMessage("InUseTurnoutSignalHeadVeto", getDisplayName()), e); // NOI18N
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull String getBeanType() {
        return Bundle.getMessage("BeanNameSignalHead");
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractSignalHead.class);

}
