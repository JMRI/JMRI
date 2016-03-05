// DoubleTurnoutSignalHead.java
package jmri.implementation;

import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drive a single signal head via two "Turnout" objects.
 * <P>
 * After much confusion, the user-level terminology was changed to call these
 * "Double Output"; the class name remains the same to reduce recoding.
 * <P>
 * The two Turnout objects are provided during construction, and each drives a
 * specific color (RED and GREEN). Normally, "THROWN" is on, and "CLOSED" is
 * off. YELLOW is provided by turning both on ("THROWN")
 * <P>
 * This class doesn't currently listen to the Turnout's to see if they've been
 * changed via some other mechanism.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 * @version	$Revision$
 */
public class DoubleTurnoutSignalHead extends DefaultSignalHead implements java.beans.VetoableChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 7106439855805533364L;

    public DoubleTurnoutSignalHead(String sys, String user, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> red) {
        super(sys, user);
        mRed = red;
        mGreen = green;
    }

    public DoubleTurnoutSignalHead(String sys, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> red) {
        super(sys);
        mRed = red;
        mGreen = green;
    }

    @SuppressWarnings("fallthrough")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SF_SWITCH_FALLTHROUGH")
    protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
        if (mLit == false) {
            mRed.getBean().setCommandedState(Turnout.CLOSED);
            mGreen.getBean().setCommandedState(Turnout.CLOSED);
            return;
        } else if (!mFlashOn
                && ((mAppearance == FLASHGREEN)
                || (mAppearance == FLASHYELLOW)
                || (mAppearance == FLASHRED))) {
            // flash says to make output dark
            mRed.getBean().setCommandedState(Turnout.CLOSED);
            mGreen.getBean().setCommandedState(Turnout.CLOSED);
            return;

        } else {
            switch (mAppearance) {
                case RED:
                case FLASHRED:
                    mRed.getBean().setCommandedState(Turnout.THROWN);
                    mGreen.getBean().setCommandedState(Turnout.CLOSED);
                    break;
                case YELLOW:
                case FLASHYELLOW:
                    mRed.getBean().setCommandedState(Turnout.THROWN);
                    mGreen.getBean().setCommandedState(Turnout.THROWN);
                    break;
                case GREEN:
                case FLASHGREEN:
                    mRed.getBean().setCommandedState(Turnout.CLOSED);
                    mGreen.getBean().setCommandedState(Turnout.THROWN);
                    break;
                default:
                    log.warn("Unexpected new appearance: " + mAppearance);
                // go dark by falling through
                case DARK:
                    mRed.getBean().setCommandedState(Turnout.CLOSED);
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
        mRed = null;
        mGreen = null;
        jmri.InstanceManager.turnoutManagerInstance().removeVetoableChangeListener(this);
        super.dispose();
    }

    NamedBeanHandle<Turnout> mRed;
    NamedBeanHandle<Turnout> mGreen;

    public NamedBeanHandle<Turnout> getRed() {
        return mRed;
    }

    public NamedBeanHandle<Turnout> getGreen() {
        return mGreen;
    }

    public void setRed(NamedBeanHandle<Turnout> t) {
        mRed = t;
    }

    public void setGreen(NamedBeanHandle<Turnout> t) {
        mGreen = t;
    }

    boolean isTurnoutUsed(Turnout t) {
        if (getRed() != null && t.equals(getRed().getBean())) {
            return true;
        }
        if (getGreen() != null && t.equals(getGreen().getBean())) {
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(DoubleTurnoutSignalHead.class.getName());
}

/* @(#)DoubleTurnoutSignalHead.java */
