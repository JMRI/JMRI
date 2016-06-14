package jmri.implementation;

import jmri.SignalHead;
import jmri.Turnout;

/**
 * Abstract class providing the basic logic of the SignalHead interface.
 * <p>
 * SignalHead system names are always upper case.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public abstract class AbstractSignalHead extends AbstractNamedBean
        implements SignalHead, java.io.Serializable, java.beans.VetoableChangeListener {

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

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //						Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state
    /**
     * By default, signals are lit.
     */
    protected boolean mLit = true;

    /**
     * Default behavior for "lit" parameter is to track value and return it.
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
     * <P>
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
     * <P>
     * This generally shouldn't be used by Java code; use getAppearance instead.
     * The is provided to make Jython script access easier to read.
     * @return current state
     */
    @Override
    public int getState() {
        return getAppearance();
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "MS_EXPOSE_REP"}, justification = "OK until Java 1.6 allows return of cheap array copy")
    public static int[] getDefaultValidStates() {
        return validStates;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "MS_EXPOSE_REP"}, justification = "OK until Java 1.6 allows return of cheap array copy")
    public static String[] getDefaultValidStateNames() {
        return validStateNames;
    }

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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "OK until Java 1.6 allows return of cheap array copy")
    @Override
    public int[] getValidStates() {
        return validStates;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "OK until Java 1.6 allows return of cheap array copy")
    @Override
    public String[] getValidStateNames() {
        return validStateNames;
    }

    abstract boolean isTurnoutUsed(Turnout t);

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            if (isTurnoutUsed((Turnout) evt.getOldValue())) {
                java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);
                throw new java.beans.PropertyVetoException(Bundle.getMessage("InUseTurnoutSignalHeadVeto", getDisplayName()), e); //IN18N
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) {
            log.warn("not clear DoDelete operated? {}", getSystemName()); //IN18N
        }
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalHead");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractSignalHead.class.getName());
}
