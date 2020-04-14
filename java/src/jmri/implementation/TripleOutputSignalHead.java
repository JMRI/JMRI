package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drive a single searchlight signal head via three "Turnout" objects.
 * <p>
 * "Triple Output RGB" to differentiate from the existing RYG triple output
 * head; The class name fits in with the quad output name which is the
 * equivalent discrete lamp head.
 * <p>
 * The three Turnout objects are provided during construction, and each drives a
 * specific color (RED, GREEN and BLUE). Normally, "THROWN" is on, and "CLOSED"
 * is off.
 * <p>
 * Red = Red Green = Green Yellow = Red and Green Lunar = Red, Green and Blue
 * <p>
 * This class doesn't currently listen to the Turnout's to see if they've been
 * changed via some other mechanism.
 *
 * @author Suzie Tall based on Bob Jacobsen's work
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 */
public class TripleOutputSignalHead extends DoubleTurnoutSignalHead {
    public TripleOutputSignalHead(String sys, String user, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> blue, NamedBeanHandle<Turnout> red) {
        super(sys, user, green, red);
        mBlue = blue;
    }

    public TripleOutputSignalHead(String sys, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> blue, NamedBeanHandle<Turnout> red) {
        super(sys, green, red);
        mBlue = blue;
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    @Override
    protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
        if (!mLit) {
            mRed.getBean().setCommandedState(Turnout.CLOSED);
            mBlue.getBean().setCommandedState(Turnout.CLOSED);
            mGreen.getBean().setCommandedState(Turnout.CLOSED);
        } else if (!mFlashOn
                && ((mAppearance == FLASHGREEN)
                || (mAppearance == FLASHYELLOW)
                || (mAppearance == FLASHLUNAR)
                || (mAppearance == FLASHRED))) {
            // flash says to make output dark
            mRed.getBean().setCommandedState(Turnout.CLOSED);
            mBlue.getBean().setCommandedState(Turnout.CLOSED);
            mGreen.getBean().setCommandedState(Turnout.CLOSED);
        } else {
            switch (mAppearance) {
                case RED:
                case FLASHRED:
                    mRed.getBean().setCommandedState(Turnout.THROWN);
                    mBlue.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.CLOSED);
                    break;
                case YELLOW:
                case FLASHYELLOW:
                    mRed.getBean().setCommandedState(Turnout.THROWN);
                    mBlue.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.THROWN);
                    break;
                case GREEN:
                case FLASHGREEN:
                    mRed.getBean().setCommandedState(Turnout.CLOSED);
                    mBlue.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.THROWN);
                    break;
                case LUNAR:
                case FLASHLUNAR:
                    mRed.getBean().setCommandedState(Turnout.THROWN);
                    mBlue.getBean().setCommandedState(Turnout.THROWN);
                    mGreen.getBean().setCommandedState(Turnout.THROWN);
                    break;
                default:
                    log.warn("Unexpected new appearance: " + mAppearance);
                // go dark by falling through
                case DARK:
                    mRed.getBean().setCommandedState(Turnout.CLOSED);
                    mBlue.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.CLOSED);
                    break;
            }
        }
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose() {
        mBlue = null;
        super.dispose();
    }

    private NamedBeanHandle<Turnout> mBlue;

    public NamedBeanHandle<Turnout> getBlue() {
        return mBlue;
    }

    public void setBlue(NamedBeanHandle<Turnout> t) {
        mBlue = t;
    }

    // claim support for Lunar aspects
    private final static int[] validStates = new int[]{
        DARK,
        RED,
        LUNAR,
        YELLOW,
        GREEN,
        FLASHRED,
        FLASHLUNAR,
        FLASHYELLOW,
        FLASHGREEN
    };
    private static final String[] validStateKeys = new String[]{
        "SignalHeadStateDark",
        "SignalHeadStateRed",
        "SignalHeadStateLunar",
        "SignalHeadStateYellow",
        "SignalHeadStateGreen",
        "SignalHeadStateFlashingRed",
        "SignalHeadStateFlashingLunar",
        "SignalHeadStateFlashingYellow",
        "SignalHeadStateFlashingGreen"
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getValidStates() {
        return Arrays.copyOf(validStates, validStates.length);
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
        String[] stateNames = new String[validStateKeys.length];
        int i = 0;
        for (String stateKey : validStateKeys) {
            stateNames[i++] = Bundle.getMessage(stateKey);
        }
        return stateNames;
    }

    @Override
    boolean isTurnoutUsed(Turnout t) {
        if (super.isTurnoutUsed(t)) {
            return true;
        }
        if (getBlue() != null && t.equals(getBlue().getBean())) {
            return true;
        }
        return false;
    }

    /**
     * Disables the feedback mechanism of the DoubleTurnoutSignalHead.
     */
    @Override
    void readOutput() { }

    private final static Logger log = LoggerFactory.getLogger(TripleOutputSignalHead.class);

}
