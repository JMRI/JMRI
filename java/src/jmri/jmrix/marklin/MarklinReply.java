package jmri.jmrix.marklin;

/**
 * Carries the reply to an MarklinMessage.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Kevin Dickerson Copyright (C) 2007
 *
 */
public class MarklinReply extends jmri.jmrix.AbstractMRReply {

    // create a new one
    public MarklinReply() {
        super();
    }

    public MarklinReply(String s) {
        super(s);
    }

    public MarklinReply(MarklinReply l) {
        super(l);
    }

    // create a new one from an array
    public MarklinReply(int[] d) {
        //this(header);
        this();
        _nDataChars = d.length;
        System.arraycopy(d, 0, _dataChars, 0, d.length);
    }

    //Maximum size of a reply packet is 13 bytes.
    @Override
    public int maxSize() {
        return 13;
    }

    // no need to do anything
    @Override
    protected int skipPrefix(int index) {
        return index;
    }

    @Override
    public int value() {
        if (isBinary()) {
            return getElement(0);
        } else {
            return super.value();
        }
    }

    @Override  // avoid stupid sign extension
    public int getElement(int n) {
        return super.getElement(n) & 0xff;
    }

    //An event message is Unsolicited
    @Override
    public boolean isUnsolicited() {
        return !isResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append("0x").append(Integer.toHexString(_dataChars[0]));
        for (int i = 1; i < _nDataChars; i++) {
            buf.append(", 0x").append(Integer.toHexString(_dataChars[i]));
        }
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toMonitorString(){
        // eventually, the MarklinMon class should probably be integrated here.
        return jmri.jmrix.marklin.swing.monitor.MarklinMon.displayReply(this);
    }

    public boolean isResponse() {
        return (getElement(1) & 0x01) == 0x01;
    }

    public int getCanDataLength() {
        return getElement(4);
    }

    public int[] getCanData() {
        int[] arr = new int[maxSize() - 5];
        for (int i = 5; i < maxSize(); i++) {
            arr[i - 5] = getElement(i);
        }
        return arr;
    }

    public int[] getCanAddress() {
        int[] arr = new int[4];
        for (int i = 5; i < 9; i++) {
            arr[i - 5] = getElement(i);
        }
        return arr;
    }

    /**
     * Get the 4-Byte Address.
     * @return the value from CANADDRESSBYTE1 (hi) to CANADDRESSBYTE4 (low)
     */
    public long getAddress() {
        long addr = (getElement(MarklinConstants.CANADDRESSBYTE1));
        addr = (addr << 8) + (getElement(MarklinConstants.CANADDRESSBYTE2));
        addr = (addr << 8) + (getElement(MarklinConstants.CANADDRESSBYTE3));
        addr = (addr << 8) + (getElement(MarklinConstants.CANADDRESSBYTE4));
        return addr;
    }

    /**
     * Sets the 4-byte address by splitting an integer into four bytes.
     * @param address the 32-bit integer representing the full address
     */
    public void setAddress(long address) {
        setElement(MarklinConstants.CANADDRESSBYTE1, (byte) ((address >> 24) & 0xFF)); // hi
        setElement(MarklinConstants.CANADDRESSBYTE2, (byte) ((address >> 16) & 0xFF));
        setElement(MarklinConstants.CANADDRESSBYTE3, (byte) ((address >> 8) & 0xFF));
        setElement(MarklinConstants.CANADDRESSBYTE4, (byte) (address & 0xFF)); // lo
    }

    public int getPriority() {
        return (getElement(0) >> 4);
    }

    /**
     * Get the Control Command.
     * @return command, e.g. MarklinConstants.CMDHALTSYS
     */
    public int getCommand() {
        int result = getElement(0) << 7;
        result = result + getElement(1) >> 1;
        return result;
    }

    /**
     * Set the Command value.
     * @param command new value.
     */
    public void setCommand(int command) {
        // Update only the relevant bits in element0 (upper 7 bits)
        int element0 = (getElement(0) & ~0x7F) | ((command >> 7) & 0x7F);

        // Update only the relevant bits in element1 (lower 7 bits, shifted left)
        int element1 = (getElement(1) & ~0xFE) | ((command & 0x7F) << 1);

        // Set the updated elements
        setElement(0, element0);
        setElement(1, element1);
    }

    public int[] getHash() {
        int[] arr = new int[2];
        arr[0] = getElement(2);
        arr[1] = getElement(3);
        return arr;
    }
}
