package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drive a single signal head via one "Turnout" objects.
 * <P>
 * After much confusion, the user-level terminology was changed to call these
 * "Single Output"; the class name remains the same to reduce recoding.
 * <P>
 * One Turnout object is provided during construction, and drives the aspect to
 * be either ON or OFF. Normally, "THROWN" is on, and "CLOSED" is off. The
 * facility to set the aspect via any of the four aspect colors is provided,
 * however they all do the same.
 *
 * Based Upon DoubleTurnoutSignalHead by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2010
 */
public class SingleTurnoutSignalHead extends DefaultSignalHead implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -6275671809339636536L;

    /**
     * @param on  Appearance constant from {@link jmri.SignalHead} for the
     *            output on (Turnout thrown) appearance
     * @param off Appearance constant from {@link jmri.SignalHead} for the
     *            signal off (Turnout closed) appearance
     */
    public SingleTurnoutSignalHead(String sys, String user, NamedBeanHandle<Turnout> lit, int on, int off) {
        super(sys, user);
        Initialize(lit, on, off);
    }

    /**
     * @param on  Appearance constant from {@link jmri.SignalHead} for the
     *            output on (Turnout thrown) appearance
     * @param off Appearance constant from {@link jmri.SignalHead} for the
     *            signal off (Turnout closed) appearance
     */
    public SingleTurnoutSignalHead(String sys, NamedBeanHandle<Turnout> lit, int on, int off) {
        super(sys);
        Initialize(lit, on, off);
    }

    /** Helper function for constructors. */
    private void Initialize(NamedBeanHandle<Turnout> lit, int on, int off) {
        setOutput(lit);
        mOnAppearance = on;
        mOffAppearance = off;
        if (lit.getBean().getKnownState() == jmri.Turnout.CLOSED) {
          setAppearance(off);
        } else if (lit.getBean().getKnownState() == jmri.Turnout.THROWN) {
          setAppearance(on);
        } else {
          // Assumes "off" state to prevents setting turnouts at startup.
          mAppearance = off;
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

    protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
        if (mLit == false) {
            setTurnoutState(Turnout.CLOSED);
            return;
        } else if (!mFlashOn
                && (mAppearance == mOnAppearance * 2)) {
            setTurnoutState(Turnout.CLOSED);
            return;
        } else if (!mFlashOn
                && (mAppearance == mOffAppearance * 2)) {
            setTurnoutState(Turnout.THROWN);
            return;
        } else {
            if ((mAppearance == mOffAppearance) || (mAppearance == (mOffAppearance * 2))) {
                setTurnoutState(Turnout.CLOSED);
            } else if ((mAppearance == mOnAppearance) || (mAppearance == (mOnAppearance * 2))) {
                setTurnoutState(Turnout.THROWN);
            } else {
                log.warn("Unexpected new appearance: " + mAppearance);
            }
        }
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
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
      mOutput=t;
      if (mOutput != null) {
        mOutput.getBean().addPropertyChangeListener(this);
      }
    }

    public int[] getValidStates() {
        int[] validStates;
        if (mOnAppearance == mOffAppearance) {
            validStates = new int[2];
            validStates[0] = mOnAppearance;
            validStates[1] = mOffAppearance;
            return validStates;
        } else if (mOnAppearance == DARK || mOffAppearance == DARK) {
            validStates = new int[3];
        } else {
            validStates = new int[2];
        }
        int x = 0;
        validStates[x] = mOnAppearance;
        x++;
        if (mOffAppearance == DARK) {
            validStates[x] = (mOnAppearance * 2);  // makes flashing
            x++;
        }
        validStates[x] = mOffAppearance;
        x++;
        if (mOnAppearance == DARK) {
            validStates[x] = (mOffAppearance * 2);  // makes flashing
        }
        return validStates;
    }

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
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
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
                log.warn("Unexpected appearance: " + mAppearance);
            // go dark by falling through
            case SignalHead.DARK:
                return Bundle.getMessage("SignalHeadStateDark");
        }
    }

    boolean isTurnoutUsed(Turnout t) {
        if (getOutput() != null && t.equals(getOutput().getBean())) {
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(SingleTurnoutSignalHead.class.getName());

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == mOutput.getBean() && evt.getPropertyName() == "KnownState") {
            // The underlying turnout has some state change. Check if its known state matches what we expected it to do.
            int newTurnoutValue = ((Integer)evt.getNewValue()).intValue();
            /*String oldTurnoutString = turnoutToString(mTurnoutCommandedState);
            String newTurnoutString = turnoutToString(newTurnoutValue);
            log.warn("signal " + this.mUserName + ": underlying turnout changed. last set state " +
            oldTurnoutString + ", current turnout state " + newTurnoutString + ", current appearance " + getSignalColour(mAppearance));*/
            if (mTurnoutCommandedState != newTurnoutValue) {
                // The turnout state has changed against what we commanded.
                int oldAppearance = mAppearance;
                int newAppearance = mAppearance;
                if (newTurnoutValue == Turnout.CLOSED) newAppearance = mOffAppearance;
                if (newTurnoutValue == Turnout.THROWN) newAppearance = mOnAppearance;
                if (newAppearance != oldAppearance) {
                    mAppearance = newAppearance;
                    // Updates last commanded state.
                    mTurnoutCommandedState = newTurnoutValue;
                    // notify listeners, if any
                    firePropertyChange("Appearance", Integer.valueOf(oldAppearance), Integer.valueOf(newAppearance));
                }
            }
        }
    }
}

