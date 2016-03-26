// TripleTurnoutSignalHead.java
package jmri.implementation;

import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drive a single signal head via three "Turnout" objects.
 * <P>
 * After much confusion, the user-level terminology was changed to call these
 * "Triple Output"; the class name remains the same to reduce recoding.
 * <P>
 * The three Turnout objects are provided during construction, and each drives a
 * specific color (RED, YELLOW and GREEN). Normally, "THROWN" is on, and
 * "CLOSED" is off.
 * <P>
 * This class doesn't currently listen to the Turnout's to see if they've been
 * changed via some other mechanism.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 * @version	$Revision$
 */
public class TripleTurnoutSignalHead extends DoubleTurnoutSignalHead {

    /**
     *
     */
    private static final long serialVersionUID = -139076982387444927L;

    public TripleTurnoutSignalHead(String sys, String user, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> yellow, NamedBeanHandle<Turnout> red) {
        super(sys, user, green, red);
        mYellow = yellow;
    }

    public TripleTurnoutSignalHead(String sys, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> yellow, NamedBeanHandle<Turnout> red) {
        super(sys, green, red);
        mYellow = yellow;
    }

    @SuppressWarnings("fallthrough")
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
        if (mLit == false) {
            mRed.getBean().setCommandedState(Turnout.CLOSED);
            mYellow.getBean().setCommandedState(Turnout.CLOSED);
            mGreen.getBean().setCommandedState(Turnout.CLOSED);
            return;
        } else if (!mFlashOn
                && ((mAppearance == FLASHGREEN)
                || (mAppearance == FLASHYELLOW)
                || (mAppearance == FLASHRED))) {
            // flash says to make output dark
            mRed.getBean().setCommandedState(Turnout.CLOSED);
            mYellow.getBean().setCommandedState(Turnout.CLOSED);
            mGreen.getBean().setCommandedState(Turnout.CLOSED);
            return;

        } else {
            switch (mAppearance) {
                case RED:
                case FLASHRED:
                    mRed.getBean().setCommandedState(Turnout.THROWN);
                    mYellow.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.CLOSED);
                    break;
                case YELLOW:
                case FLASHYELLOW:
                    mRed.getBean().setCommandedState(Turnout.CLOSED);
                    mYellow.getBean().setCommandedState(Turnout.THROWN);
                    mGreen.getBean().setCommandedState(Turnout.CLOSED);
                    break;
                case GREEN:
                case FLASHGREEN:
                    mRed.getBean().setCommandedState(Turnout.CLOSED);
                    mYellow.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.THROWN);
                    break;
                default:
                    log.warn("Unexpected new appearance: " + mAppearance);
                // go dark by falling through
                case DARK:
                    mRed.getBean().setCommandedState(Turnout.CLOSED);
                    mYellow.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.CLOSED);
                    break;
            }
        }
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    public void dispose() {
        mYellow = null;
        super.dispose();
    }

    NamedBeanHandle<Turnout> mYellow;

    public NamedBeanHandle<Turnout> getYellow() {
        return mYellow;
    }

    public void setYellow(NamedBeanHandle<Turnout> t) {
        mYellow = t;
    }

    boolean isTurnoutUsed(Turnout t) {
        if (super.isTurnoutUsed(t)) {
            return true;
        }
        if (getYellow() != null && t.equals(getYellow().getBean())) {
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(TripleTurnoutSignalHead.class.getName());
}

/* @(#)TripleTurnoutSignalHead.java */
