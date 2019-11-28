package jmri.jmrix.srcp;

/**
 * Encodes a message to an SRCP server. The SRCPReply class handles the response
 * from the command station.
 *
 * The {@link SRCPReply} class handles the response from the command station.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2004, 2008
 */
public class SRCPMessage extends jmri.jmrix.AbstractMRMessage {

    public SRCPMessage() {
        super();
    }

    // create a new one
    public SRCPMessage(int i) {
        super(i);
    }

    // copy one
    public SRCPMessage(SRCPMessage m) {
        super(m);
    }

    // from String
    public SRCPMessage(String m) {
        super(m);
    }

    // diagnose format
    /**
     *  Detrmine if the message turns off track power
     *  @return true if the messages is a track power off message,false otherwise 
     */
    public boolean isKillMain() {
        String s = toString();
        return s.contains("POWER OFF") && s.contains("SET");
    }

    /**
     *  Detrmine if the message turns on track power
     *  @return true if the messages is a track power on message,false otherwise 
     */
    public boolean isEnableMain() {
        String s = toString();
        return s.contains("POWER ON") && s.contains("SET");
    }

    // static methods to return a formatted message

    /**
     * @return an SRCPMessage to turn the track power on
     */
    static public SRCPMessage getEnableMain() {
        SRCPMessage m = new SRCPMessage("SET 1 POWER ON\n");
        m.setBinary(false);
        return m;
    }

    /**
     * @return an SRCPMessage to turn the track power off
     */
    static public SRCPMessage getKillMain() {
        SRCPMessage m = new SRCPMessage("SET 1 POWER OFF\n");
        m.setBinary(false);
        return m;
    }


    /**
     * @param bus  a bus number
     * @return an SRCPMessage to initialize programming on the given bus.
     */
    static public SRCPMessage getProgMode(int bus) {
        String msg = "INIT " + bus + " SM NMRA\n";
        SRCPMessage m = new SRCPMessage(msg);
        return m;
    }

    /**
     * @param bus  a bus number
     * @return an SRCPMessage to terminate programming on the given bus.
     */
    static public SRCPMessage getExitProgMode(int bus) {
        String msg = "TERM " + bus + " SM\n";
        SRCPMessage m = new SRCPMessage(msg);
        return m;
    }

    /**
     * @param bus  a bus number
     * @param cv  the CV to read.
     * @return an SRCPMessage to read the given CV in direct mode the given bus.
     */
    static public SRCPMessage getReadDirectCV(int bus, int cv) {
        String msg = "GET " + bus + " SM 0 CV " + cv + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    /**
     * @param bus  a bus number
     * @param cv  the CV to read.
     * @param val  a value for the CV.
     * @return an SRCPMessage to check the given cv has the given val using the given bus.
     */
    static public SRCPMessage getConfirmDirectCV(int bus, int cv, int val) {
        String msg = "VERIFY " + bus + " SM 0 CV " + cv + " " + val + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;

    }

    /**
     * @param bus  a bus number
     * @param cv  the CV to write.
     * @param val  a value for the CV.
     * @return an SRCPMessage to write the given value to the provided cv using the given bus.
     */
    static public SRCPMessage getWriteDirectCV(int bus, int cv, int val) {
        String msg = "SET " + bus + " SM 0 CV " + cv + " " + val + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    /**
     * @param bus  a bus number
     * @param cv  the CV to read.
     * @param bit  the bit to read.
     * @return an SRCPMessage to read the given bit of the given CV uisng the provided bus.
     */
    static public SRCPMessage getReadDirectBitCV(int bus, int cv, int bit) {
        String msg = "GET " + bus + " SM 0 CVBIT " + cv + " " + bit + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    /**
     * @param bus  a bus number
     * @param cv  the CV to read.
     * @param bit  the bit to read.
     * @param val  the value to check
     * @return an SRCPMessage to verify the given bit of the given CV has the given val uisng the provided bus.
     */
    static public SRCPMessage getConfirmDirectBitCV(int bus, int cv, int bit, int val) {
        String msg = "VERIFY " + bus + " SM 0 CVBIT " + cv + " " + bit + " " + val + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;

    }

    /**
     * @param bus  a bus number
     * @param cv  the CV to write.
     * @param bit  the bit to write
     * @param val  the value to write
     * @return an SRCPMessage to write the given value to the given bit of the given CV uisng the provided bus.
     */
    static public SRCPMessage getWriteDirectBitCV(int bus, int cv, int bit, int val) {
        String msg = "SET " + bus + " SM 0 CVBIT " + cv + " " + bit + " " + val + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    /**
     * @param bus  a bus number
     * @param reg  a register to read.  Restricted to valuse less than 8.
     * @return an SRCPMessage to read the provided register using the given bus.
     * @throws IllegalArgumentException if the register value is out of range.
     */
    static public SRCPMessage getReadRegister(int bus, int reg) {
        if (reg > 8) {
            throw new IllegalArgumentException("register number too large: " + reg);
        }
        String msg = "GET " + bus + " SM 0 REG " + reg + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    /**
     * @param bus  a bus number
     * @param reg  a register to read.  Restricted to valuse less than 8.
     * @param val  a value for the register
     * @return an SRCPMessage to verify the provided register has the expected val using the given bus.
     * @throws IllegalArgumentException if the register value is out of range.
     */
    static public SRCPMessage getConfirmRegister(int bus, int reg, int val) {
        if (reg > 8) {
            throw new IllegalArgumentException("register number too large: " + reg);
        }
        String msg = "VERIFY " + bus + " SM 0 REG " + reg + " " + val + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    /**
     * @param bus  a bus number
     * @param reg  a register to write.  Restricted to valuse less than 8.
     * @param val  a value for the register
     * @return an SRCPMessage to write the given value to the provided register using the given bus.
     * @throws IllegalArgumentException if the register value is out of range.
     */
    static public SRCPMessage getWriteRegister(int bus, int reg, int val) {
        if (reg > 8) {
            throw new IllegalArgumentException("register number too large: " + reg);
        }
        String msg = "SET " + bus + " SM 0 REG " + reg + " " + val + "\n";
        SRCPMessage m = new SRCPMessage(msg);
        m.setTimeout(LONG_TIMEOUT);
        return m;
    }

    static final int LONG_TIMEOUT = 180000;  // e.g. for programming options

    // private final static Logger log = LoggerFactory.getLogger(SRCPMessage.class);

}


