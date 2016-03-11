// DefaultSignalHead.java
package jmri.implementation;

/**
 * Default implementation of the basic logic of the SignalHead interface.
 *
 * This class only claims support for the Red, Yellow and Green appearances, and
 * their corressponding flashing forms. Support for Lunar is deferred to
 * DefaultLunarSignalHead.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2009
 * @version $Revision$
 */
public abstract class DefaultSignalHead extends AbstractSignalHead {

    /**
     *
     */
    private static final long serialVersionUID = 1008833207689892684L;

    public DefaultSignalHead(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalHead(String systemName) {
        super(systemName);
    }

    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance;
        mAppearance = newAppearance;
        if (mLit && ((newAppearance == FLASHGREEN)
                || (newAppearance == FLASHYELLOW)
                || (newAppearance == FLASHRED)
                || (newAppearance == FLASHLUNAR))) {
            startFlash();
        }
        if ((!mLit) || ((newAppearance != FLASHGREEN)
                && (newAppearance != FLASHYELLOW)
                && (newAppearance != FLASHRED)
                && (newAppearance != FLASHLUNAR))) {
            stopFlash();
        }

        /* there are circumstances (admittedly rare) where signals and turnouts can get out of sync
         * allow 'newAppearance' to be set to resync these cases - P Cressman
         if (oldAppearance != newAppearance) */ {
            updateOutput();

            // notify listeners, if any
            firePropertyChange("Appearance", Integer.valueOf(oldAppearance), Integer.valueOf(newAppearance));
        }
    }

    public void setLit(boolean newLit) {
        boolean oldLit = mLit;
        mLit = newLit;
        if (oldLit != newLit) {
            if (mLit && ((mAppearance == FLASHGREEN)
                    || (mAppearance == FLASHYELLOW)
                    || (mAppearance == FLASHRED)
                    || (mAppearance == FLASHLUNAR))) {
                startFlash();
            }
            if (!mLit) {
                stopFlash();
            }
            updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", Boolean.valueOf(oldLit), Boolean.valueOf(newLit));
        }

    }

    /**
     * Set the held parameter.
     * <P>
     * Note that this does not directly effect the output on the layout; the
     * held parameter is a local variable which effects the aspect only via
     * higher-level logic
     */
    public void setHeld(boolean newHeld) {
        boolean oldHeld = mHeld;
        mHeld = newHeld;
        if (oldHeld != newHeld) {
            // notify listeners, if any
            firePropertyChange("Held", Boolean.valueOf(oldHeld), Boolean.valueOf(newHeld));
        }

    }

    /**
     * Type-specific routine to handle output to the layout hardware.
     *
     * Does not notify listeners of changes; that's done elsewhere. Should use
     * the following variables to determine what to send:
     * <UL>
     * <LI>mAppearance
     * <LI>mLit
     * <LI>mFlashOn
     * </ul>
     */
    abstract protected void updateOutput();

    /**
     * Should a flashing signal be on (lit) now?
     */
    protected boolean mFlashOn = true;

    javax.swing.Timer timer = null;
    /**
     * On or off time of flashing signal
     */
    int delay = 750;

    /*
     * Start the timer that controls flashing
     */
    protected void startFlash() {
        // note that we don't force mFlashOn to be true at the start
        // of this; that way a flash in process isn't disturbed.
        if (timer == null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    timeout();
                }
            });
            timer.setInitialDelay(delay);
            timer.setRepeats(true);
        }
        timer.start();
    }

    private void timeout() {
        if (mFlashOn) {
            mFlashOn = false;
        } else {
            mFlashOn = true;
        }

        updateOutput();
    }

    /*
     * Stop the timer that controls flashing.
     *
     * This is only a resource-saver; the actual use of 
     * flashing happens elsewere
     */
    protected void stopFlash() {
        if (timer != null) {
            timer.stop();
        }
        mFlashOn = true;
    }

    final static private int[] validStates = new int[]{
        DARK,
        RED,
        YELLOW,
        GREEN,
        FLASHRED,
        FLASHYELLOW,
        FLASHGREEN,};
    final static private String[] validStateNames = new String[]{
        Bundle.getMessage("SignalHeadStateDark"),
        Bundle.getMessage("SignalHeadStateRed"),
        Bundle.getMessage("SignalHeadStateYellow"),
        Bundle.getMessage("SignalHeadStateGreen"),
        Bundle.getMessage("SignalHeadStateFlashingRed"),
        Bundle.getMessage("SignalHeadStateFlashingYellow"),
        Bundle.getMessage("SignalHeadStateFlashingGreen"),};

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public int[] getValidStates() {
        return validStates;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public String[] getValidStateNames() {
        return validStateNames;
    }

    boolean isTurnoutUsed(jmri.Turnout t) {
        return false;
    }

}

/* @(#)DefaultSignalHead.java */
