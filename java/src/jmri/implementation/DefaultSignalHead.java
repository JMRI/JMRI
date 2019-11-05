package jmri.implementation;

import java.util.Arrays;

/**
 * Default implementation of the basic logic of the SignalHead interface.
 *
 * This class only claims support for the Red, Yellow and Green appearances, and
 * their corresponding flashing forms. Support for Lunar is deferred to
 * DefaultLunarSignalHead or an extended class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2009
 */
public abstract class DefaultSignalHead extends AbstractSignalHead {

    public DefaultSignalHead(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalHead(String systemName) {
        super(systemName);
    }

    @Override
    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance; // store the current appearance
        mAppearance = newAppearance;
        appearanceSetsFlashTimer(newAppearance);

        /* there are circumstances (admittedly rare) where signals and turnouts can get out of sync
         * allow 'newAppearance' to be set to resync these cases - P Cressman
         * if (oldAppearance != newAppearance) */
        updateOutput();

        // notify listeners, if any
        firePropertyChange("Appearance", oldAppearance, newAppearance);
    }

    /**
     * Call to set timer when updating the appearance.
     *
     * @param newAppearance the new appearance
     */
    protected void appearanceSetsFlashTimer(int newAppearance) {
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
    }

    @Override
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
            firePropertyChange("Lit", oldLit, newLit);
        }
    }

    /**
     * Set the held parameter.
     * <p>
     * Note that this does not directly effect the output on the layout; the
     * held parameter is a local variable which effects the aspect only via
     * higher-level logic.
     *
     * @param newHeld new Held state, true if Held, to be compared with current
     *                Held state
     */
    @Override
    public void setHeld(boolean newHeld) {
        boolean oldHeld = mHeld;
        mHeld = newHeld;
        if (oldHeld != newHeld) {
            // notify listeners, if any
            firePropertyChange("Held", oldHeld, newHeld);
        }

    }

    /**
     * Type-specific routine to handle output to the layout hardware.
     * <p>
     * Does not notify listeners of changes; that's done elsewhere. Should use
     * the following variables to determine what to send:
     * <ul>
     * <li>mAppearance
     * <li>mLit
     * <li>mFlashOn
     * </ul>
     */
    abstract protected void updateOutput();

    /**
     * Should a flashing signal be on (lit) now?
     */
    protected boolean mFlashOn = true;

    javax.swing.Timer timer = null;
    /**
     * On or off time of flashing signal.
     * Public so that it can be overridden by 
     * scripting (before first use)
     */
    public int delay = masterDelay;

    public static int masterDelay = 750;
    
    /**
     * Start the timer that controls flashing.
     */
    protected void startFlash() {
        // note that we don't force mFlashOn to be true at the start
        // of this; that way a flash in process isn't disturbed.
        if (timer == null) {
            timer = new javax.swing.Timer(delay, (java.awt.event.ActionEvent e) -> {
                timeout();
            });
            timer.setInitialDelay(delay);
            timer.setRepeats(true);
        }
        timer.start();
    }

    private void timeout() {
        mFlashOn = !mFlashOn;

        updateOutput();
    }

    /*
     * Stop the timer that controls flashing.
     * <p>
     * This is only a resource-saver; the actual use of
     * flashing happens elsewhere.
     */
    protected void stopFlash() {
        if (timer != null) {
            timer.stop();
        }
        mFlashOn = true;
    }

    final static private int[] VALID_STATES = new int[]{
        DARK,
        RED,
        YELLOW,
        GREEN,
        FLASHRED,
        FLASHYELLOW,
        FLASHGREEN,
    }; // No int for Lunar

    final static private String[] VALID_STATE_KEYS = new String[]{
        "SignalHeadStateDark",
        "SignalHeadStateRed",
        "SignalHeadStateYellow",
        "SignalHeadStateGreen",
        "SignalHeadStateFlashingRed",
        "SignalHeadStateFlashingYellow",
        "SignalHeadStateFlashingGreen",
    }; // Lunar not included

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getValidStates() {
        return Arrays.copyOf(VALID_STATES, VALID_STATES.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getValidStateKeys() {
        return Arrays.copyOf(VALID_STATE_KEYS, VALID_STATE_KEYS.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getValidStateNames() {
        String[] stateNames = new String[VALID_STATE_KEYS.length];
        int i = 0;
        for (String stateKey : VALID_STATE_KEYS) {
            stateNames[i++] = Bundle.getMessage(stateKey);
        }
        return stateNames;
    }

    @Override
    boolean isTurnoutUsed(jmri.Turnout t) {
        return false;
    }

}
