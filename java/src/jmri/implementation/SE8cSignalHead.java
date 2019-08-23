package jmri.implementation;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.SignalHead for signals implemented by an SE8C.
 * <p>
 * This implementation writes out to the physical signal when it's commanded to
 * change appearance, and updates its internal state when it hears commands from
 * other places.
 * <p>
 * To get a complete set of aspects, we assume that the SE8C board has been
 * configured such that the 4th aspect is "dark". We then do flashing aspects by
 * commanding the lit appearance to change.
 * <p>
 * We can't assume any form of numbering for Turnouts to address the digits, so
 * we take two turnout names as arguments. As a convenience, we manage the user
 * names if they're not already set.
 * <p>
 * Only the DARK, RED, GREEN and YELLOW appearances will be properly tracked
 * when they occur on the LocoNet. The FLASHING aspects won't be, nor will the
 * Held or Lit states.
 * <p>
 * The algorithms in this class are a collaborative effort of Digitrax, Inc and
 * Bob Jacobsen.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2010, 2014
 */
public class SE8cSignalHead extends DefaultSignalHead {

    /**
     * Primary ctor.
     *
     * @param lowTO    lower-numbered Turnout reference
     * @param highTO   higher-numbered Turnout reference
     * @param userName user name for mast
     */
    public SE8cSignalHead(NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO,
            String userName) {
        // create systemname
        super(makeSystemName(lowTO, highTO), userName);
        this.lowTurnout = lowTO;
        this.highTurnout = highTO;
        init();
    }

    /**
     * Primary ctor without user name.
     *
     * @param lowTO  lower-numbered Turnout reference
     * @param highTO higher-numbered Turnout reference
     */
    public SE8cSignalHead(NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO) {
        // create systemname
        super(makeSystemName(lowTO, highTO));
        this.lowTurnout = lowTO;
        this.highTurnout = highTO;
        init();
    }

    /**
     * Ctor specifying system name and user name.
     *
     * @param sname    system name for mast
     * @param lowTO    lower-numbered Turnout reference
     * @param highTO   higher-numbered Turnout reference
     * @param userName user name for mast
     */
    public SE8cSignalHead(String sname, NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO,
            String userName) {
        // create systemname
        super(sname, userName);
        this.lowTurnout = lowTO;
        this.highTurnout = highTO;
        init();
    }

    /**
     * Ctor specifying system name.
     *
     * @param sname  system name for mast
     * @param lowTO  lower-numbered Turnout reference
     * @param highTO higher-numbered Turnout reference
     */
    public SE8cSignalHead(String sname, NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO) {
        // create systemname
        super(sname);
        this.lowTurnout = lowTO;
        this.highTurnout = highTO;
        init();
    }

    /**
     * Compatibility ctor.
     *
     * @param pNumber  number (address) of low turnout
     * @param userName name to use for this signal head
     */
    public SE8cSignalHead(int pNumber, String userName) {
        super("LH" + pNumber, userName);
        this.lowTurnout = makeHandle(pNumber);
        this.highTurnout = makeHandle(pNumber + 1);
        init();
    }

    /**
     * Implement convention for making a system name.
     * <p>
     * Must pass arguments, as it is used before object is complete.
     *
     * @param lowTO  lower-numbered Turnout reference
     * @param highTO higher-numbered Turnout reference
     * @return system name with fixed elements, i.e. IH:SE8c:to1\to2
     */
    static String makeSystemName(NamedBeanHandle<Turnout> lowTO,
            NamedBeanHandle<Turnout> highTO) {
        return ("IH:SE8c:\"" + lowTO.getName() + "\";\"" + highTO.getName() + "\"");
    }

    /**
     * Create a handle from a raw number.
     * <p>
     * Static, so can be referenced before ctor complete.
     *
     * @param i index number (address) of a turnout on the signal head
     * @return NamedBeanHandle&lt;Turnout&gt; object to use as output for head
     * @throws IllegalArgumentException when creation from i fails
     */
    static NamedBeanHandle<Turnout> makeHandle(int i) throws IllegalArgumentException {
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

    /**
     * Type-specific routine to handle output to the layout hardware.
     * Implemented to handle a request to change state by sending a LocoNet
     * command.
     * <p>
     * Does not notify listeners of changes; that's done elsewhere. Should
     * consider the following variables to determine what to send:
     * <ul>
     * <li>mAppearance
     * <li>mLit
     * <li>mFlashOn
     * </ul>
     */
    @Override
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
                    log.error("Invalid state request: {}", mAppearance);
            }
        }
    }

    public NamedBeanHandle<Turnout> getLow() {
        return lowTurnout;
    }

    public NamedBeanHandle<Turnout> getHigh() {
        return highTurnout;
    }

    @Override
    public void dispose() {
    }

    @Override
    boolean isTurnoutUsed(Turnout t) {
        return (getLow() != null && t.equals(getLow().getBean()))
                || (getHigh() != null && t.equals(getHigh().getBean()));
    }

    private final static Logger log = LoggerFactory.getLogger(SE8cSignalHead.class);
}
