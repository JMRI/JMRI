// SE8cSignalHead.java
package jmri.implementation;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.SignalHead for signals implemented by an SE8c
 * <P>
 * This implementation writes out to the physical signal when it's commanded to
 * change appearance, and updates its internal state when it hears commands from
 * other places.
 * <P>
 * To get a complete set of aspects, we assume that the SE8C board has been
 * configured such that the 4th aspect is "dark". We then do flashing aspects by
 * commanding the lit appearance to change.
 * <P>
 * We can't assume any form of numbering for Turnouts to address the digits, so
 * we take two turnout names as arguments. As a convenience, we manage the user
 * names if they're not already set.
 *
 * <p>
 * Only the DARK, RED, GREEN and YELLOW appearances will be properly tracked
 * when they occur on the LocoNet. The FLASHING aspects won't be, nor will the
 * Held or Lit states.
 *
 * <P>
 * The algorithms in this class are a collaborative effort of Digitrax, Inc and
 * Bob Jacobsen.
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2010, 2014
 * @version	$Revision$
 */
public class SE8cSignalHead extends DefaultSignalHead {

    /**
     *
     */
    private static final long serialVersionUID = 7046319284833504499L;

    /**
     * Primary ctor
     *
     * @param lowTO  Lower-numbered Turnout reference
     * @param highTO higher-numbered Turnout reference
     */
    public SE8cSignalHead(NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO,
            String userName) {
        // create systemname
        super(makeSystemName(lowTO, highTO), userName);
        this.lowTurnout = lowTO;
        this.highTurnout = highTO;
        systemName = makeSystemName(lowTO, highTO);
        init();

        // Add listeners to track other changes on LocoNet
        addListeners();
    }

    /**
     * Primary ctor without user name
     *
     * @param lowTO  Lower-numbered Turnout reference
     * @param highTO higher-numbered Turnout reference
     */
    public SE8cSignalHead(NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO) {
        // create systemname
        super(makeSystemName(lowTO, highTO));
        this.lowTurnout = lowTO;
        this.highTurnout = highTO;
        systemName = makeSystemName(lowTO, highTO);
        init();
    }

    /**
     * Ctor for specifying system name
     *
     * @param lowTO  Lower-numbered Turnout reference
     * @param highTO higher-numbered Turnout reference
     */
    public SE8cSignalHead(String sname, NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO,
            String userName) {
        // create systemname
        super(sname, userName);
        this.lowTurnout = lowTO;
        this.highTurnout = highTO;
        systemName = sname;
        init();
    }

    /**
     * Ctor for specifying system name
     *
     * @param lowTO  Lower-numbered Turnout reference
     * @param highTO higher-numbered Turnout reference
     */
    public SE8cSignalHead(String sname, NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO) {
        // create systemname
        super(sname);
        this.lowTurnout = lowTO;
        this.highTurnout = highTO;
        systemName = sname;
        init();
    }

    /**
     * Implement convention for making a system name. Must pass arguments, as
     * used before object is complete
     */
    static String makeSystemName(NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO) {
        return ("IH:SE8c:\"" + lowTO.getName() + "\";\"" + highTO.getName() + "\"").toUpperCase();
    }

    /**
     * Compatibility ctor
     *
     * @param pNumber number (address) of low turnout
     */
    public SE8cSignalHead(int pNumber, String userName) {
        super("LH" + pNumber, userName);
        this.lowTurnout = makeHandle(pNumber);
        this.highTurnout = makeHandle(pNumber + 1);
        systemName = "LH" + pNumber;
        init();
    }

    /**
     * Create a handle from a raw number. Static, so can be referenced before
     * ctor complete.
     */
    static NamedBeanHandle<Turnout> makeHandle(int i) {
        String number = "" + i;
        return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(
                number,
                InstanceManager.turnoutManagerInstance().provideTurnout(number)
        );
    }

    /**
     * Compatibility ctor
     *
     * @param pNumber number (address) of low turnout
     */
    public SE8cSignalHead(int pNumber) {
        super("LH" + pNumber);
        this.lowTurnout = makeHandle(pNumber);
        this.highTurnout = makeHandle(pNumber + 1);
        systemName = "LH" + pNumber;
        init();
    }

    NamedBeanHandle<Turnout> lowTurnout;
    NamedBeanHandle<Turnout> highTurnout;

    void init() {
        // basic operation, nothing but ON messages needed
        lowTurnout.getBean().setBinaryOutput(true);
        highTurnout.getBean().setBinaryOutput(true);

        // ensure default appearance
        mAppearance = DARK;  // start turned off
        updateOutput();
    }

    public String getSystemName() {
        return systemName;
    }
    String systemName;

    // Handle a request to change state by sending a LocoNet command
    protected void updateOutput() {
        if (!mLit) {
            highTurnout.getBean().setCommandedState(Turnout.CLOSED);
        } else if (!mFlashOn
                && ((mAppearance == FLASHGREEN)
                || (mAppearance == FLASHYELLOW)
                || (mAppearance == FLASHRED))) {
            // flash says to make output dark; 
            // flashing-but-lit is handled below
            highTurnout.getBean().setCommandedState(Turnout.CLOSED);
        } else {
            // which of the four states?
            switch (mAppearance) {
                case FLASHRED:
                case RED:
                    lowTurnout.getBean().setCommandedState(Turnout.THROWN);
                    break;
                case FLASHYELLOW:
                case YELLOW:
                    highTurnout.getBean().setCommandedState(Turnout.THROWN);
                    break;
                case FLASHGREEN:
                case GREEN:
                    lowTurnout.getBean().setCommandedState(Turnout.CLOSED);
                    break;
                case DARK:
                    highTurnout.getBean().setCommandedState(Turnout.CLOSED);
                    break;
                default:
                    log.error("Invalid state request: " + mAppearance);
                    return;
            }
        }
    }

    public NamedBeanHandle<Turnout> getLow() {
        return lowTurnout;
    }

    public NamedBeanHandle<Turnout> getHigh() {
        return highTurnout;
    }

    public void dispose() {
    }

    boolean isTurnoutUsed(Turnout t) {
        if (getLow() != null && t.equals(getLow().getBean())) {
            return true;
        }
        if (getHigh() != null && t.equals(getHigh().getBean())) {
            return true;
        }
        return false;
    }

    void addListeners() {
        lowTurnout.getBean().addPropertyChangeListener(
                new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        // we're not tracking state, we're tracking changes in state
                        if (e.getPropertyName().equals("CommandedState")) {
                            if (e.getNewValue().equals(Turnout.CLOSED) && !e.getOldValue().equals(Turnout.CLOSED)) {
                                setAppearance(GREEN);
                            } else if (e.getNewValue().equals(Turnout.THROWN) && !e.getOldValue().equals(Turnout.THROWN)) {
                                setAppearance(RED);
                            }
                        }
                    }
                }
        );
        highTurnout.getBean().addPropertyChangeListener(
                new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        // we're not tracking state, we're tracking changes in state
                        if (e.getPropertyName().equals("CommandedState")) {
                            if (e.getNewValue().equals(Turnout.CLOSED) && !e.getOldValue().equals(Turnout.CLOSED)) {
                                setAppearance(DARK);
                            } else if (e.getNewValue().equals(Turnout.THROWN) && !e.getOldValue().equals(Turnout.THROWN)) {
                                setAppearance(YELLOW);
                            }
                        }
                    }
                }
        );
    }

    private final static Logger log = LoggerFactory.getLogger(SE8cSignalHead.class.getName());
}

/* @(#)SE8cSignalHead.java */
