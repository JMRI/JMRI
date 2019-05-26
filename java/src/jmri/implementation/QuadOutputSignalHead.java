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
    final static private int[] validStates = new int[]{
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
    final static private String[] validStateNames = new String[]{
        Bundle.getMessage("SignalHeadStateDark"),
        Bundle.getMessage("SignalHeadStateRed"),
        Bundle.getMessage("SignalHeadStateLunar"),
        Bundle.getMessage("SignalHeadStateYellow"),
        Bundle.getMessage("SignalHeadStateGreen"),
        Bundle.getMessage("SignalHeadStateFlashingRed"),
        Bundle.getMessage("SignalHeadStateFlashingLunar"),
        Bundle.getMessage("SignalHeadStateFlashingYellow"),
        Bundle.getMessage("SignalHeadStateFlashingGreen")
    };

    @Override
    public int[] getValidStates() {
        return Arrays.copyOf(validStates, validStates.length);
    }

    @Override
    public String[] getValidStateNames() {
        return Arrays.copyOf(validStateNames, validStateNames.length);
    }

    @Override
    boolean isTurnoutUsed(Turnout t) {
        if (super.isTurnoutUsed(t)) {
            return true;
        }
        return getLunar() != null && t.equals(getLunar().getBean());
    }
}
