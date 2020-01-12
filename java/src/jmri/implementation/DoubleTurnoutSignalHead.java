package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drive a single signal head via two "Turnout" objects.
 * <p>
 * After much confusion, the user-level terminology was changed to call these
 * "Double Output"; the class name remains the same to reduce recoding.
 * <p>
 * The two Turnout objects are provided during construction, and each drives a
 * specific color (RED and GREEN). Normally, "THROWN" is on, and "CLOSED" is
 * off. YELLOW is provided by turning both on ("THROWN")
 * <p>
 * This class also listens to the Turnouts to see if they've been
 * changed via some other mechanism.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 */
public class DoubleTurnoutSignalHead extends DefaultSignalHead {

    public DoubleTurnoutSignalHead(String sys, String user, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> red) {
        super(sys, user);
        setRed(red);
        setGreen(green);
    }

    public DoubleTurnoutSignalHead(String sys, NamedBeanHandle<Turnout> green, NamedBeanHandle<Turnout> red) {
        super(sys);
        setRed(red);
        setGreen(green);
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    @Override
    protected void updateOutput() {
        // assumes that writing a turnout to an existing state is cheap!
        if (mLit == false) {
            commandState(Turnout.CLOSED, Turnout.CLOSED);
            return;
        } else if (!mFlashOn
                && ((mAppearance == FLASHGREEN)
                || (mAppearance == FLASHYELLOW)
                || (mAppearance == FLASHRED))) {
            // flash says to make output dark
            commandState(Turnout.CLOSED, Turnout.CLOSED);
            return;

        } else {
            switch (mAppearance) {
                case RED:
                case FLASHRED:
                    commandState(Turnout.THROWN, Turnout.CLOSED);
                    break;
                case YELLOW:
                case FLASHYELLOW:
                    commandState(Turnout.THROWN, Turnout.THROWN);
                    break;
                case GREEN:
                case FLASHGREEN:
                    commandState(Turnout.CLOSED, Turnout.THROWN);
                    break;
                default:
                    log.warn("Unexpected new appearance: " + mAppearance);
                // go dark by falling through
                case DARK:
                    commandState(Turnout.CLOSED, Turnout.CLOSED);
                    break;
            }
        }
    }

    /**
     * Sets the output turnouts' commanded state.
     *
     * @param red   state to set the mRed turnout
     * @param green state to set the mGreen turnout.
     */
    void commandState(int red, int green) {
        mRedCommanded = red;
        mRed.getBean().setCommandedState(red);
        mGreenCommanded = green;
        mGreen.getBean().setCommandedState(green);
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose() {
        if (mRed != null) {
            mRed.getBean().removePropertyChangeListener(turnoutChangeListener);
        }
        if (mGreen != null) {
            mGreen.getBean().removePropertyChangeListener(turnoutChangeListener);
        }
        mRed = null;
        mGreen = null;
        jmri.InstanceManager.turnoutManagerInstance().removeVetoableChangeListener(this);
        super.dispose();
    }

    NamedBeanHandle<Turnout> mRed;
    NamedBeanHandle<Turnout> mGreen;
    int mRedCommanded;
    int mGreenCommanded;

    public NamedBeanHandle<Turnout> getRed() {
        return mRed;
    }

    public NamedBeanHandle<Turnout> getGreen() {
        return mGreen;
    }

    public void setRed(NamedBeanHandle<Turnout> t) {
        if (mRed != null) {
            mRed.getBean().removePropertyChangeListener(turnoutChangeListener);
        }
        mRed = t;
        if (mRed != null) {
            mRed.getBean().addPropertyChangeListener(turnoutChangeListener);
        }
    }

    public void setGreen(NamedBeanHandle<Turnout> t) {
        if (mGreen != null) {
            mGreen.getBean().removePropertyChangeListener(turnoutChangeListener);
        }
        mGreen = t;
        if (mGreen != null) {
            mGreen.getBean().addPropertyChangeListener(turnoutChangeListener);
        }
    }

    @Override
    boolean isTurnoutUsed(Turnout t) {
        if (getRed() != null && t.equals(getRed().getBean())) {
            return true;
        }
        if (getGreen() != null && t.equals(getGreen().getBean())) {
            return true;
        }
        return false;
    }

    javax.swing.Timer readUpdateTimer;

    private PropertyChangeListener turnoutChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (propertyChangeEvent.getPropertyName().equals("KnownState")) {
                if (propertyChangeEvent.getSource().equals(mRed.getBean()) && propertyChangeEvent.getNewValue().equals(mRedCommanded)) {
                    return; // ignore change that we commanded
                }
                if (propertyChangeEvent.getSource().equals(mGreen.getBean()) && propertyChangeEvent.getNewValue().equals(mGreenCommanded)) {
                    return; // ignore change that we commanded
                }
                if (readUpdateTimer == null) {
                    readUpdateTimer = new javax.swing.Timer(200, (ActionEvent actionEvent) ->
                            readOutput());
                    readUpdateTimer.setRepeats(false);
                    readUpdateTimer.start();
                } else {
                    readUpdateTimer.restart();
                }
            }
        }
    };

    /**
     * Checks if the turnouts' output state matches the commanded output state; if not, then
     * changes the appearance to match the output's current state.
     */
    void readOutput() {
        if ((mAppearance == FLASHGREEN)
                || (mAppearance == FLASHYELLOW)
                || (mAppearance == FLASHRED)
                || (mAppearance == FLASHLUNAR)) {
            // If we are actively flashing right now, then we ignore external changes, since
            // those might be coming from ourselves and will be overwritten shortly.
            return;
        }
        int red = mRed.getBean().getKnownState();
        int green = mGreen.getBean().getKnownState();
        if (mRedCommanded == red && mGreenCommanded == green) return;
        // The turnouts' known state has diverged from what we set. We attempt to decode the
        // actual state to an appearance. This is a lossy operation, but the user has done
        // something very explicitly to make this happen, like manually clicking the turnout throw
        // button, or setting up an external signaling logic system.
        if (red == Turnout.CLOSED && green == Turnout.CLOSED) {
            setAppearance(DARK);
        } else if (red == Turnout.THROWN && green == Turnout.CLOSED) {
            setAppearance(RED);
        } else if (red == Turnout.THROWN && green == Turnout.THROWN) {
            setAppearance(YELLOW);
        } else if (red == Turnout.CLOSED && green == Turnout.THROWN) {
            setAppearance(GREEN);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DoubleTurnoutSignalHead.class);
}
