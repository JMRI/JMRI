package jmri.jmrix.can.cbus;

import jmri.ProgrammingMode;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to allow use of CBUS concepts to access the underlying can message
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
public class CbusMessage {
    /* Methods that take a CanMessage as argument */

    public static int getId(CanMessage m) {
        if (m.isExtended()) {
            return m.getHeader() & 0x1FFFFFF;
        } else {
            return m.getHeader() & 0x7f;
        }
    }

    public static int getPri(CanMessage m) {
        if (m.isExtended()) {
            return (m.getHeader() >> 25) & 0x0F;
        } else {
            return (m.getHeader() >> 7) & 0x0F;
        }
    }

    public static int getOpcode(CanMessage m) {
        return m.getElement(0);
    }

    public static int getDataLength(CanMessage m) {
        return m.getElement(0) >> 5;
    }

    public static int getNodeNumber(CanMessage m) {
        if (isEvent(m)) {
            return m.getElement(1) * 256 + m.getElement(2);
        } else {
            return 0;
        }
    }

    public static int getEvent(CanMessage m) {
        if (isEvent(m)) {
            return m.getElement(3) * 256 + m.getElement(4);
        } else {
            return 0;
        }
    }

    public static int getEventType(CanMessage m) {
        if ((m.getElement(0) & 1) == 1) {
            return CbusConstants.EVENT_OFF;
        } else {
            return CbusConstants.EVENT_ON;
        }
    }

    public static boolean isEvent(CanMessage m) {
        if ((m.getElement(0) == 0x90) || (m.getElement(0) == 0x91)) {
            return true;
        } else {
            return false;
        }
    }

    public static void setId(CanMessage m, int id) {
        if (m.isExtended()) {
            if ((id & ~0x1ffffff) != 0) {
                throw new IllegalArgumentException("invalid extended ID value: " + id);
            }
            int update = m.getHeader();
            m.setHeader((update & ~0x01ffffff) | id);
        } else {
            if ((id & ~0x7f) != 0) {
                throw new IllegalArgumentException("invalid standard ID value: " + id);
            }
            int update = m.getHeader();
            m.setHeader((update & ~0x07f) | id);
        }
    }

    public static void setPri(CanMessage m, int pri) {
        if ((pri & ~0x0F) != 0) {
            throw new IllegalArgumentException("invalid CBUS Pri value: " + pri);
        }
        int update = m.getHeader();
        if (m.isExtended()) {
            m.setHeader((update & ~0x1e000000) | (pri << 25));
        } else {
            m.setHeader((update & ~0x780) | (pri << 7));
        }
    }

    public static String toAddress(CanMessage m) {
        if (m.getElement(0) == CbusConstants.CBUS_ACON) {
            // + form
            return "+n" + (m.getElement(1) * 256 + m.getElement(2)) + "e" + (m.getElement(3) * 256 + m.getElement(4));
        } else if (m.getElement(0) == CbusConstants.CBUS_ACOF) {
            // - form
            return "-n" + (m.getElement(1) * 256 + m.getElement(2)) + "e" + (m.getElement(3) * 256 + m.getElement(4));
        } else if (m.getElement(0) == CbusConstants.CBUS_ASON) {
            // + short form
            return "+" + (m.getElement(3) * 256 + m.getElement(4));
        } else if (m.getElement(0) == CbusConstants.CBUS_ASOF) {
            // - short form
            return "-" + (m.getElement(3) * 256 + m.getElement(4));
        } else {
            // hex form
            return "x" + m.toString().replaceAll(" ", "");
        }
    }

    public static boolean isRequestTrackOff(CanMessage m) {
        if (m.getOpCode() == CbusConstants.CBUS_RTOF) {
            return true;
        }
        return false;
    }

    public static boolean isRequestTrackOn(CanMessage m) {
        if (m.getOpCode() == CbusConstants.CBUS_RTON) {
            return true;
        }
        return false;
    }

    /* 
     * Methods that take a CanReply as argument
     */
    public static int getId(CanReply r) {
        if (r.isExtended()) {
            return r.getHeader() & 0x1FFFFF;
        } else {
            return r.getHeader() & 0x7f;
        }
    }

    public static int getPri(CanReply r) {
        if (r.isExtended()) {
            return (r.getHeader() >> 25) & 0x0F;
        } else {
            return (r.getHeader() >> 7) & 0x0F;
        }
    }

    public static int getOpcode(CanReply r) {
        return r.getElement(0);
    }

    public static int getDataLength(CanReply r) {
        return r.getElement(0) >> 5;
    }

    public static int getNodeNumber(CanReply r) {
        if (isEvent(r)) {
            return r.getElement(1) * 256 + r.getElement(2);
        } else {
            return 0;
        }
    }

    public static int getEvent(CanReply r) {
        if (isEvent(r)) {
            return r.getElement(3) * 256 + r.getElement(4);
        } else {
            return 0;
        }
    }

    public static int getEventType(CanReply r) {
        if ((r.getElement(0) & 1) == 1) {
            return CbusConstants.EVENT_OFF;
        } else {
            return CbusConstants.EVENT_ON;
        }
    }

    public static boolean isEvent(CanReply r) {
        if ((r.getElement(0) == 0x90) || (r.getElement(0) == 0x91)) {
            return true;
        } else {
            return false;
        }
    }

    public static void setId(CanReply r, int id) {
        if (r.isExtended()) {
            if ((id & ~0x1fffff) != 0) {
                throw new IllegalArgumentException("invalid extended ID value: " + id);
            }
            int update = r.getHeader();
            r.setHeader((update & ~0x01fffff) | id);
        } else {
            if ((id & ~0x7f) != 0) {
                throw new IllegalArgumentException("invalid standard ID value: " + id);
            }
            int update = r.getHeader();
            r.setHeader((update & ~0x07f) | id);
        }
    }

    public static void setPri(CanReply r, int pri) {
        if ((pri & ~0x0F) != 0) {
            throw new IllegalArgumentException("invalid CBUS Pri value: " + pri);
        }
        int update = r.getHeader();
        if (r.isExtended()) {
            r.setHeader((update & ~0x1e00000) | (pri << 25));
        } else {
            r.setHeader((update & ~0x780) | (pri << 7));
        }
    }

    public static String toAddress(CanReply r) {
        if (r.getElement(0) == CbusConstants.CBUS_ACON) {
            // + form
            return "+n" + (r.getElement(1) * 256 + r.getElement(2)) + "e" + (r.getElement(3) * 256 + r.getElement(4));
        } else if (r.getElement(0) == CbusConstants.CBUS_ACOF) {
            // - form
            return "-n" + (r.getElement(1) * 256 + r.getElement(2)) + "e" + (r.getElement(3) * 256 + r.getElement(4));
        } else if (r.getElement(0) == CbusConstants.CBUS_ASON) {
            // + short form
            return "+" + (r.getElement(3) * 256 + r.getElement(4));
        } else if (r.getElement(0) == CbusConstants.CBUS_ASOF) {
            // - short form
            return "-" + (r.getElement(3) * 256 + r.getElement(4));
        } else {
            // hex form
            return "x" + r.toString().replaceAll(" ", "");
        }
    }

    public static boolean isTrackOff(CanReply m) {
        if (m.getOpCode() == CbusConstants.CBUS_TOF) {
            return true;
        }
        return false;
    }

    public static boolean isTrackOn(CanReply m) {
        if (m.getOpCode() == CbusConstants.CBUS_TON) {
            return true;
        }
        return false;
    }

    public static boolean isArst(CanReply m) {
        if (m.getOpCode() == CbusConstants.CBUS_ARST) {
            return true;
        }
        return false;
    }

    /**
     * CBUS programmer commands
     */
    static public CanMessage getReadCV(int cv, ProgrammingMode mode, int header) {
        CanMessage m = new CanMessage(5, header);
        m.setElement(0, CbusConstants.CBUS_QCVS);
        m.setElement(1, CbusConstants.SERVICE_HANDLE);
        m.setElement(2, (cv / 256) & 0xff);
        m.setElement(3, cv & 0xff);
        if (mode.equals(ProgrammingMode.PAGEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_PAGED);
        } else if (mode.equals(ProgrammingMode.DIRECTBITMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BIT);
        } else if (mode.equals(ProgrammingMode.DIRECTBYTEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BYTE);
        } else {
            m.setElement(4, CbusConstants.CBUS_PROG_REGISTER);
        }
        setPri(m, 0xb);
        return m;
    }

    static public CanMessage getWriteCV(int cv, int val, ProgrammingMode mode, int header) {
        CanMessage m = new CanMessage(6, header);
        m.setElement(0, CbusConstants.CBUS_WCVS);
        m.setElement(1, CbusConstants.SERVICE_HANDLE);
        m.setElement(2, (cv / 256) & 0xff);
        m.setElement(3, cv & 0xff);
        if (mode.equals(ProgrammingMode.PAGEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_PAGED);
        } else if (mode.equals(ProgrammingMode.DIRECTBITMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BIT);
        } else if (mode.equals(ProgrammingMode.DIRECTBYTEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BYTE);
        } else {
            m.setElement(4, CbusConstants.CBUS_PROG_REGISTER);
        }
        m.setElement(5, val);
        setPri(m, 0xb);
        return m;
    }

    /**
     * CBUS Ops mode programmer commands
     */
    static public CanMessage getOpsModeWriteCV(int mAddress, boolean mLongAddr, int cv, int val, int header) {
        CanMessage m = new CanMessage(7, header);
        int address = mAddress;
        m.setElement(0, CbusConstants.CBUS_WCVOA);
        if (mLongAddr) {
            address = address | 0xc000;
        }
        m.setElement(1, address / 256);
        m.setElement(2, address & 0xff);
        m.setElement(3, (cv / 256) & 0xff);
        m.setElement(4, cv & 0xff);
        m.setElement(5, CbusConstants.CBUS_OPS_BYTE);
        m.setElement(6, val);
        setPri(m, 0xb);
        return m;
    }

    /**
     * CBUS Power commands
     */
    static public CanMessage getRequestTrackOn(int header) {
        CanMessage m = new CanMessage(1, header);
        m.setElement(0, CbusConstants.CBUS_RTON);
        setPri(m, 0xb);
        return m;
    }

    static public CanMessage getRequestTrackOff(int header) {
        CanMessage m = new CanMessage(1, header);
        m.setElement(0, CbusConstants.CBUS_RTOF);
        setPri(m, 0xb);
        return m;
    }

    /**
     * CBUS bootloader commands
     */
    /*
     * This is a strict CBUS message to put a node into boot mode
     */
    static public CanMessage getBootEntry(int nn, int header) {
        CanMessage m = new CanMessage(3, header);
        m.setElement(0, CbusConstants.CBUS_BOOTM);
        m.setElement(1, (nn / 256) & 0xFF);
        m.setElement(2, nn & 0xFF);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Microchip AN247 format NOP message to set address
     *
     * The CBUS bootloader uses extended ID frames
     */
    static public CanMessage getBootNop(int a, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, (a / 65536) & 0xFF);
        m.setElement(1, (a / 256) & 0xFF);
        m.setElement(2, a & 0xFF);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, 0);
        m.setElement(6, 0);
        m.setElement(7, 0);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Microchip AN247 format message to reset and enter normal mode
     */
    static public CanMessage getBootReset(int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, 0);
        m.setElement(1, 0);
        m.setElement(2, 0);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, 1);
        m.setElement(6, 0);
        m.setElement(7, 0);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Microchip AN247 format message to initialise the bootloader and set the
     * start address
     */
    static public CanMessage getBootInitialise(int a, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, (a / 65536) & 0xFF);
        m.setElement(1, (a / 256) & 0xFF);
        m.setElement(2, a & 0xFF);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, 2);
        m.setElement(6, 0);
        m.setElement(7, 0);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Microchip AN247 format message to send the checksum for comparison
     */
    static public CanMessage getBootCheck(int c, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, 0);
        m.setElement(1, 0);
        m.setElement(2, 0);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, 3);
        m.setElement(6, (c / 256) & 0xff);
        m.setElement(7, c & 0xff);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Microchip AN247 format message to check if a module is in boot mode
     */
    static public CanMessage getBootTest(int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, 0);
        m.setElement(1, 0);
        m.setElement(2, 0);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, 4);
        m.setElement(6, 0);
        m.setElement(7, 0);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Microchip AN247 format message to write 8 bytes of data
     */
    static public CanMessage getBootWriteData(int[] d, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x5);
        try {
            m.setElement(0, d[0] & 0xff);
            m.setElement(1, d[1] & 0xff);
            m.setElement(2, d[2] & 0xff);
            m.setElement(3, d[3] & 0xff);
            m.setElement(4, d[4] & 0xff);
            m.setElement(5, d[5] & 0xff);
            m.setElement(6, d[6] & 0xff);
            m.setElement(7, d[7] & 0xff);
        } catch (Exception e) {
            log.error("Exception in bootloader data" + e);
        }
        setPri(m, 0xb);
        return m;
    }

    public static boolean isBootError(CanReply r) {
        if (r.isExtended() && (r.getHeader() == 0x10000004) && (r.getElement(0) == 0)) {
            return (true);
        }
        return (false);
    }

    public static boolean isBootOK(CanReply r) {
        if (r.isExtended() && (r.getHeader() == 0x10000004) && (r.getElement(0) == 1)) {
            return (true);
        }
        return (false);
    }

    public static boolean isBootConfirm(CanReply r) {
        if (r.isExtended() && (r.getHeader() == 0x10000004) && (r.getElement(0) == 2)) {
            return (true);
        }
        return (false);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusMessage.class);
}
