package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drive a single signal head via one "Turnout" object.
 * <p>
 * After much confusion, the user-level terminology was changed to call these
 * "Single Output"; the class name remains the same to reduce recoding.
 * <p>
 * One Turnout object is provided during construction, and drives the appearance
 * to be either ON or OFF. Normally, "THROWN" is on, and "CLOSED" is off. The
 * facility to set the appearance via any of the basic four appearance colors +
 * Lunar is provided, however they all do the same.
 * <p>
 * Based upon DoubleTurnoutSignalHead by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2010
 */
public class SingleTurnoutSignalHead extends DefaultSignalHead implements PropertyChangeListener {

    /**
     * Ctor using only a system name.
     *
     * @param sys  system name for haed
     * @param user userName user name for mast
     * @param lit  named bean for turnout switching the Lit property
     * @param on   Appearance constant from {@link jmri.SignalHead} for the
     *             output on (Turnout thrown) appearance
     * @param off  Appearance constant from {@link jmri.SignalHead} for the
     *             signal off (Turnout closed) appearance
     */
    public SingleTurnoutSignalHead(String sys, String user, NamedBeanHandle<Turnout> lit, int on, int off) {
        super(sys, user);
        initialize(lit, on, off);
    }

    /**
     * Ctor including user name.
     *
     * @param sys system name for haed
     * @param lit named bean for turnout switching the Lit property
     * @param on  Appearance constant from {@link jmri.SignalHead} for the
     *            output on (Turnout thrown) appearance
     * @param off Appearance constant from {@link jmri.SignalHead} for the
     *            signal off (Turnout closed) appearance
     */
    public SingleTurnoutSignalHead(String sys, NamedBeanHandle<Turnout> lit, int on, int off) {
        super(sys);
        initialize(lit, on, off);
    }

    /**
     * Helper function for constructors.
     *
     * @param lit named bean for turnout switching the Lit property
     * @param on  Appearance constant from {@link jmri.SignalHead} for the
     *            output on (Turnout thrown) appearance
     * @param off Appearance constant from {@link jmri.SignalHead} for the
     *            signal off (Turnout closed) appearance
     */
    private void initialize(NamedBeanHandle<Turnout> lit, int on, int off) {
        setOutput(lit);
        mOnAppearance = on;
        mOffAppearance = off;
        switch (lit.getBean().getKnownState()) {
            case jmri.Turnout.CLOSED:
                setAppearance(off);
                break;
            case jmri.Turnout.THROWN:
                setAppearance(on);
                break;
            default:
                // Assumes "off" state to prevents setting turnouts at startup.
                mAppearance = off;
                break;
        }
    }

    private int mOnAppearance = DARK;
    private int mOffAppearance = LUNAR;

    /**
     * Holds the last state change we commanded our underlying turnout.
     */
    private int mTurnoutCommandedState = Turnout.CLOSED;

    private void setTurnoutState(int s) {
        mTurnoutCommandedState = s;
        mOutput.getBean().setCommandedState(s);
    }

    @Override
    protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
        if (!mLit) {
            setTurnoutState(Turnout.CLOSED);
        } else if (!mFlashOn && (mAppearance == mOnAppearance * 2)) {
            setTurnoutState(Turnout.CLOSED);
        } else if (!mFlashOn && (mAppearance == mOffAppearance * 2)) {
            setTurnoutState(Turnout.THROWN);
        } else {
            if ((mAppearance == mOffAppearance) || (mAppearance == (mOffAppearance * 2))) {
                setTurnoutState(Turnout.CLOSED);
            } else if ((mAppearance == mOnAppearance) || (mAppearance == (mOnAppearance * 2))) {
                setTurnoutState(Turnout.THROWN);
            } else {
                log.warn("Unexpected new appearance: {}", mAppearance);
            }
        }
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose() {
        setOutput(null);
        super.dispose();
    }

    private NamedBeanHandle<Turnout> mOutput;

    public int getOnAppearance() {
        return mOnAppearance;
    }

    public int getOffAppearance() {
        return mOffAppearance;
    }

    public void setOnAppearance(int on) {
        int old = mOnAppearance;
        mOnAppearance = on;
        firePropertyChange("ValidStatesChanged", old, on);
    }

    public void setOffAppearance(int off) {
        int old = mOffAppearance;
        mOffAppearance = off;
        firePropertyChange("ValidStatesChanged", old, off);
    }

    public NamedBeanHandle<Turnout> getOutput() {
        return mOutput;
    }

    public void setOutput(NamedBeanHandle<Turnout> t) {
        if (mOutput != null) {
            mOutput.getBean().removePropertyChangeListener(this);
        }
        mOutput = t;
        if (mOutput != null) {
            mOutput.getBean().addPropertyChangeListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getValidStates() {
        int[] validStates;
        if (mOnAppearance == mOffAppearance) {
            validStates = new int[2];
            validStates[0] = mOnAppearance;
            validStates[1] = mOffAppearance;
            return validStates;
        } if (mOnAppearance == DARK || mOffAppearance == DARK) { // we can make flashing with Dark only
            validStates = new int[3];
        } else {
            validStates = new int[2];
        }
        int x = 0;
        validStates[x] = mOnAppearance;
        x++;
        if (mOffAppearance == DARK) {
            validStates[x] = (mOnAppearance * 2);  // makes flashing of the one color
            x++;
        }
        validStates[x] = mOffAppearance;
        x++;
        if (mOnAppearance == DARK) {
            validStates[x] = (mOffAppearance * 2);  // makes flashing of the one color
        }
        return validStates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getValidStateKeys() {
        String[] validStateKeys = new String[getValidStates().length];
        int i = 0;
        // use the logic coded in getValidStates()
        for (int state : getValidStates()) {
            validStateKeys[i++] = getSignalColorKey(state);
        }
//        String contents = "";
//        for (String key : validStateKeys) {
//            contents = contents + key + ",";
//        };
//        log.debug(contents);
        return validStateKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getValidStateNames() {
        String[] validStateNames = new String[getValidStates().length];
        int i = 0;
        // use the logic coded in getValidStates()
        for (int state : getValidStates()) {
            validStateNames[i++] = getSignalColorName(state);
        }
        return validStateNames;
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    private String getSignalColorKey(int mAppearance) {
        switch (mAppearance) {
            case SignalHead.RED:
                return "SignalHeadStateRed";
            case SignalHead.FLASHRED:
                return "SignalHeadStateFlashingRed";
            case SignalHead.YELLOW:
                return "SignalHeadStateYellow";
            case SignalHead.FLASHYELLOW:
                return "SignalHeadStateFlashingYellow";
            case SignalHead.GREEN:
                return "SignalHeadStateGreen";
            case SignalHead.FLASHGREEN:
                return "SignalHeadStateFlashingGreen";
            case SignalHead.LUNAR:
                return "SignalHeadStateLunar";
            case SignalHead.FLASHLUNAR:
                return "SignalHeadStateFlashingLunar";
            default:
                log.warn("Unexpected appearance: {}", mAppearance);
            // go dark by falling through
            case SignalHead.DARK:
                return "SignalHeadStateDark";
        }
    }

    private String getSignalColorName(int mAppearance) {
        return Bundle.getMessage(getSignalColorKey(mAppearance));
    }

    @Override
    boolean isTurnoutUsed(Turnout t) {
        return getOutput() != null && t.equals(getOutput().getBean());
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(mOutput.getBean()) && evt.getPropertyName().equals("KnownState")) {
            // The underlying turnout has some state change. Check if its known state matches what we expected it to do.
            int newTurnoutValue = ((Integer) evt.getNewValue());
            /*String oldTurnoutString = turnoutToString(mTurnoutCommandedState);
            String newTurnoutString = turnoutToString(newTurnoutValue);
            log.warn("signal {}: underlying turnout changed. last set state {}, current turnout state {}, current appearance {}",
             this.mUserName, oldTurnoutString, newTurnoutString, getSignalColour(mAppearance));*/
            if (mTurnoutCommandedState != newTurnoutValue) {
                // The turnout state has changed against what we commanded.
                int oldAppearance = mAppearance;
                int newAppearance = mAppearance;
                if (newTurnoutValue == Turnout.CLOSED) {
                    newAppearance = mOffAppearance;
                }
                if (newTurnoutValue == Turnout.THROWN) {
                    newAppearance = mOnAppearance;
                }
                if (newAppearance != oldAppearance) {
                    mAppearance = newAppearance;
                    // Updates last commanded state.
                    mTurnoutCommandedState = newTurnoutValue;
                    // notify listeners, if any
                    firePropertyChange("Appearance", oldAppearance, newAppearance);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SingleTurnoutSignalHead.class);

}
