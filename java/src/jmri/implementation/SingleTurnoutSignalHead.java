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
 * <P>
 * After much confusion, the user-level terminology was changed to call these
 * "Single Output"; the class name remains the same to reduce recoding.
 * <P>
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

    int mOnAppearance = DARK;
    int mOffAppearance = LUNAR;

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
        if (mLit == false) {
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

    NamedBeanHandle<Turnout> mOutput;

    public int getOnAppearance() {
        return mOnAppearance;
    }

    public int getOffAppearance() {
        return mOffAppearance;
    }

    public void setOnAppearance(int on) {
        int old = on;
        mOnAppearance = on;
        firePropertyChange("ValidStatesChanged", old, on);
    }

    public void setOffAppearance(int off) {
        int old = off;
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

    @Override
    public int[] getValidStates() {
        int[] validStates;
        if (mOnAppearance == mOffAppearance) {
            validStates = new int[2];
            validStates[0] = mOnAppearance;
            validStates[1] = mOffAppearance;
            return validStates;
        } else if (mOnAppearance == DARK || mOffAppearance == DARK) { // we can make flashing with Dark only
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

    @Override
    public String[] getValidStateNames() {
        String[] validStateName;
        if (mOnAppearance == mOffAppearance) {
            validStateName = new String[2];
            validStateName[0] = getSignalColour(mOnAppearance);
            validStateName[1] = getSignalColour(mOffAppearance);
            return validStateName;
        }
        if (mOnAppearance == DARK || mOffAppearance == DARK) {
            validStateName = new String[3];
        } else {
            validStateName = new String[2];
        }
        int x = 0;
        validStateName[x] = getSignalColour(mOnAppearance);
        x++;
        if (mOffAppearance == DARK) {
            validStateName[x] = getSignalColour((mOnAppearance * 2));  // makes flashing
            x++;
        }
        validStateName[x] = getSignalColour(mOffAppearance);
        x++;
        if (mOnAppearance == DARK) {
            validStateName[x] = getSignalColour((mOffAppearance * 2));  // makes flashing
        }
        return validStateName;
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    private String getSignalColour(int mAppearance) {
        switch (mAppearance) {
            case SignalHead.RED:
                return Bundle.getMessage("SignalHeadStateRed");
            case SignalHead.FLASHRED:
                return Bundle.getMessage("SignalHeadStateFlashingRed");
            case SignalHead.YELLOW:
                return Bundle.getMessage("SignalHeadStateYellow");
            case SignalHead.FLASHYELLOW:
                return Bundle.getMessage("SignalHeadStateFlashingYellow");
            case SignalHead.GREEN:
                return Bundle.getMessage("SignalHeadStateGreen");
            case SignalHead.FLASHGREEN:
                return Bundle.getMessage("SignalHeadStateFlashingGreen");
            case SignalHead.LUNAR:
                return Bundle.getMessage("SignalHeadStateLunar");
            case SignalHead.FLASHLUNAR:
                return Bundle.getMessage("SignalHeadStateFlashingLunar");
            default:
                log.warn("Unexpected appearance: {}", mAppearance);
            // go dark by falling through
            case SignalHead.DARK:
                return Bundle.getMessage("SignalHeadStateDark");
        }
    }

    @Override
    boolean isTurnoutUsed(Turnout t) {
        return getOutput() != null && t.equals(getOutput().getBean());
    }

    private final static Logger log = LoggerFactory.getLogger(SingleTurnoutSignalHead.class);

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
            log.warn("signal " + this.mUserName + ": underlying turnout changed. last set state " +
            oldTurnoutString + ", current turnout state " + newTurnoutString + ", current appearance " + getSignalColour(mAppearance));*/
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
}
