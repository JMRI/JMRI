package jmri.implementation;

import java.util.Arrays;
import jmri.NamedBeanHandle;
import jmri.Turnout;

/**
 * Drive a single signal head via four "Turnout" objects.
 * <p>
 * After much confusion, the user-level terminology was changed to call these
 * "Triple Output"; the class name remains the same to reduce recoding.
 * <p>
 * The four Turnout objects are provided during construction, and each drives a
 * specific color (RED, YELLOW, GREEN, and LUNAR). Normally, "THROWN" is on, and
 * "CLOSED" is off.
 * <p>
 * This class doesn't currently listen to the Turnout's to see if they've been
 * changed via some other mechanism.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class QuadOutputSignalHead extends TripleTurnoutSignalHead {

    public QuadOutputSignalHead(String sys, String user, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> yellow, NamedBeanHandle<Turnout> red, NamedBeanHandle<Turnout> lunar) {
        super(sys, user, green, yellow, red);
        mLunar = lunar;
    }

    public QuadOutputSignalHead(String sys, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> yellow, NamedBeanHandle<Turnout> red, NamedBeanHandle<Turnout> lunar) {
        super(sys, green, yellow, red);
        mLunar = lunar;
    }

    @Override
    protected void updateOutput() {
        if (mLit == false) {
            super.updateOutput();
        } else if (!mFlashOn
                && ((mAppearance == FLASHGREEN)
                || (mAppearance == FLASHYELLOW)
                || (mAppearance == FLASHRED)
                || (mAppearance == FLASHLUNAR))) {
            // flash says to make output dark
            mRed.getBean().setCommandedState(Turnout.CLOSED);
            mYellow.getBean().setCommandedState(Turnout.CLOSED);
            mGreen.getBean().setCommandedState(Turnout.CLOSED);
            mLunar.getBean().setCommandedState(Turnout.CLOSED);

        } else {
            switch (mAppearance) {
                case LUNAR:
                case FLASHLUNAR:
                    mLunar.getBean().setCommandedState(Turnout.THROWN);
                    mRed.getBean().setCommandedState(Turnout.CLOSED);
                    mYellow.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.CLOSED);
                    break;
                default:
                    // let parent handle rest of cases
                    mLunar.getBean().setCommandedState(Turnout.CLOSED);
                    super.updateOutput();
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
        mLunar = null;
        super.dispose();
    }

    NamedBeanHandle<Turnout> mLunar;

    public NamedBeanHandle<Turnout> getLunar() {
        return mLunar;
    }

    public void setLunar(NamedBeanHandle<Turnout> t) {
        mLunar = t;
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
        return getLunar() != null && t.equals(getLunar().getBean());
    }
}
