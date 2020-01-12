package jmri.jmrix.nce;


/**
 * Carries the reply to an NceMessage.
 * <p>
 * Some rudimentary support is provided for the "binary" option.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2007
 */
public class NceReply extends jmri.jmrix.AbstractMRReply {

    NceTrafficController tc;
    protected static final jmri.jmrix.nce.ncemon.NceMonBinary nceMon = new jmri.jmrix.nce.ncemon.NceMonBinary();

    // create a new one
    public NceReply(NceTrafficController tc) {
        super();
        this.tc = tc;
    }

    public NceReply(NceTrafficController tc, String s) {
        super(s);
        this.tc = tc;
    }

    public NceReply(NceTrafficController tc, NceReply l) {
        super(l);
        this.tc = tc;
    }

    @Override
    protected int skipPrefix(int index) {
        // start at index, passing any control characters at the start of the buffer
        int len = "COMMAND: ".length();
        if (getNumDataElements() >= index + len - 1
                && 'C' == (char) getElement(index)
                && 'O' == (char) getElement(index + 1)
                && 'M' == (char) getElement(index + 2)
                && 'M' == (char) getElement(index + 3)
                && 'A' == (char) getElement(index + 4)
                && 'N' == (char) getElement(index + 5)
                && 'D' == (char) getElement(index + 6)
                && ':' == (char) getElement(index + 7)
                && ' ' == (char) getElement(index + 8)) {
            index = index + "COMMAND: ".length();
        }
        return index;
    }

    @Override
    public int value() {
        if (isBinary()) {
            return getElement(0) & 0xFF;  // avoid stupid sign extension
        } else {
            return super.value();
        }
    }

    /**
     * Extract poll values from binary reply
     */
    @Override
    public int pollValue() {  // integer value of first two bytes
        int first = 0xFF & ((byte) getElement(0));
        int second = 0xFF & ((byte) getElement(1));

        return first * 256 + second;
    }

    /**
     * Examine message to see if it is an asynchronous sensor (AIU) state report
     *
     * @return true if message asynch sensor message Boudreau: Improved
     *         detection to check three bytes and message length of exactly 3
     */
    public boolean isSensorMessage() {
        return getElement(0) == 0x61 && getElement(1) >= 0x30
                && getElement(2) >= 0x41 && getElement(2) <= 0x6F
                && getNumDataElements() == 3;
    }

    @Override
    public boolean isUnsolicited() {
// Boudreau: check for unsolicited AIU messages in pre 2006 EPROMs     
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
            return false;
        }
        if (isSensorMessage()) {
            setUnsolicited();
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toMonitorString(){
	    return nceMon.displayReply(this);
    }

}



