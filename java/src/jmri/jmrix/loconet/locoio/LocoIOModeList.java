package jmri.jmrix.loconet.locoio;

import java.util.Vector;
import jmri.jmrix.loconet.LnConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the set of valid modes for a particular LocoIO port,
 * as well as the conversions between addresses and SV values.
 *
 * @author John Plocher, January 30, 2007
 */
public class LocoIOModeList {

    protected Vector<LocoIOMode> modeList = new Vector<LocoIOMode>();
    protected String[] validmodes;

    /**
     * Create a new instance of LocoIOModeList
     */
    public LocoIOModeList() {

        /**
         * Initialize various configuration modes.
         * @TODO: Need to tag these with which firmware rev supports
         * them and only allow choices that match.
         *
         * Inputs...
         */
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REQ, 0x0F, 0x00, "Toggle Switch, LocoIO 1.3.2"));

        modeList.add(new LocoIOMode(0, LnConstants.OPC_INPUT_REP, 0x5F, 0x00, "Block Detector, Active High"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_INPUT_REP, 0x1F, 0x10, "Block Detector, Active Low"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REQ, 0x0F, 0x10, "Toggle Switch, Direct Control"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REP, 0x07, 0x10, "Toggle Switch, Indirect Control"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REQ, 0x6F, 0x00, "Push Button, Active High, Direct Control"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REP, 0x67, 0x00, "Push Button, Active High, Indirect Control"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REQ, 0x2F, 0x10, "Push Button, Active Low, Direct Control"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REP, 0x27, 0x10, "Push Button, Active Low, Indirect Control"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REP, 0x17, 0x70, "Turnout Feedback, single sensor"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REP, 0x37, 0x70, "Turnout Feedback, dual sensor, #1"));
        modeList.add(new LocoIOMode(0, LnConstants.OPC_SW_REP, 0x37, 0x60, "Turnout Feedback, dual sensor, #2"));
        /**
         * and Outputs...
         */
        modeList.add(new LocoIOMode(1, LnConstants.OPC_INPUT_REP, 0xC0, 0x00, "Block Occupied Indication"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_INPUT_REP, 0xD0, 0x00, "Block Occupied Indication, Blinking"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x81, 0x10, "Steady State, single output, On at Power up"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x80, 0x10, "Steady State, single output, Off at Power up"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x81, 0x30, "Steady State, paired output, On at Power up"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x80, 0x30, "Steady State, paired output, Off at Power up"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x91, 0x10, "Steady State, single output, On at Power up, Blinking"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x90, 0x10, "Steady State, single output, Off at Power up, Blinking"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x91, 0x30, "Steady State, paired output, On at Power up, Blinking"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x90, 0x30, "Steady State, paired output, Off at Power up, Blinking"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x88, 0x20, "Pulsed, software controlled on time, single output"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x8C, 0x20, "Pulsed, firmware controlled on time, single output"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x88, 0x00, "Pulsed, software controlled on time, paired output"));
        modeList.add(new LocoIOMode(1, LnConstants.OPC_SW_REQ, 0x8C, 0x00, "Pulsed, firmware controlled on time, paired output"));

        validmodes = new String[modeList.size()];
        for (int i = 0; i <= modeList.size() - 1; i++) {
            LocoIOMode m = modeList.elementAt(i);
            validmodes[i] = m.getFullMode();
        }
    }

    protected String[] getValidModes() {
        return validmodes;
    }

    protected boolean isValidModeValue(Object value) {
        if (value instanceof String) {
            String sValue = (String) value;
            for (int i = 0; i < validmodes.length; i++) {
                if (sValue.equals(validmodes[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    protected LocoIOMode getLocoIOModeFor(String s) {
        for (int i = 0; i <= modeList.size() - 1; i++) {
            LocoIOMode m = modeList.elementAt(i);
            String ms = m.getFullMode();
            if (ms.matches(s)) {
                return m;
            }
        }
        return null;
    }

    protected LocoIOMode getLocoIOModeFor(int cv, int v1, int v2) {
        // v2 &= 0x0F;
        for (int i = 0; i <= modeList.size() - 1; i++) {
            LocoIOMode m = modeList.elementAt(i);
            if (m.getSV() == cv) {
                if ((m.getOpCode() == LnConstants.OPC_INPUT_REP)
                        && (m.getV2() == (v2 & 0xD0))) {
                    return m;
                } else if (((cv == 0x6F) || (cv == 0x67) || (cv == 0x2F) || (cv == 0x27))
                        && (m.getV2() == (v2 & 0x50))) {
                    return m;
                } else if ((m.getV2() == (v2 & 0xB0))) {
                    return m;
                } else if (((cv & 0x90) == 0x10)
                        && ((cv & 0x80) == 0)
                        && (m.getV2() == (v2 & 0x70))) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Convert Value1 (Low bits) from Port Address.
     *
     * @param lim one of a list of defined port operation modes
     * @param address the address for this port
     */
    protected int addressToValue1(LocoIOMode lim, int address) {
        if (lim == null) {
            return 0;
        }
        return addressToValues(lim.getOpCode(), lim.getSV(), lim.getV2(), address) & 0x7F;
    }

    /**
     * Convert Value2 (High bits) from Port Address.
     *
     * @param lim one of a list of defined port operation modes
     * @param address the address for this port
     */
    protected int addressToValue2(LocoIOMode lim, int address) {
        if (lim == null) {
            return 0;
        }
        return (addressToValues(lim.getOpCode(), lim.getSV(), lim.getV2(), address) / 256) & 0x7F;
    }

    /**
     * Convert bytes from LocoNet packet into a 1-based address for a sensor or
     * turnout.
     *
     * @param a1 Byte containing the upper bits
     * @param a2 Byte containing the lower bits
     * @return 1-4096 address as decimal
     */
    static private int SENSOR_ADR(int a1, int a2) {
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    }

    /**
     * Create 2 byte value from Port Address bits.
     *
     * @param opcode coded value for message type
     * @param sv index of SV value to create, ignored
     * @param v2mask mask to apply on Value2
     * @param address the address for this port
     */
    protected int addressToValues(int opcode, int sv, int v2mask, int address) {
        int v1 = 0;
        int v2 = 0;

        address--;

        if (opcode == LnConstants.OPC_INPUT_REP) {
            v1 = ((address / 2) & 0x7F);
            v2 = ((address / 256) & 0x0F);
            if ((address & 0x01) == 0x01) {
                v2 |= LnConstants.OPC_INPUT_REP_SW;
            }
            v2 |= v2mask;
        } else if (opcode == LnConstants.OPC_SW_REQ) {
            v1 = (address & 0x7F);
            v2 = (address / 128) & 0x0F;
            v2 &= ~(0x40);
            v2 |= v2mask;
        } else if (opcode == LnConstants.OPC_SW_REP) {
            v1 = (address & 0x7F);
            v2 = (address / 128) & 0x0F;
            v2 &= ~(0x40);
            v2 |= v2mask;
        }
        return v2 * 256 + v1;
    }

    /**
     * Extract Port Address from the 3 SV values.
     *
     * @param opcode coded value for message type
     * @param sv first SV value, ignored
     * @param v1 second value (upper bits)
     * @param v2 second value (lower bits)
     * @return address (int) of the port
     */
    protected int valuesToAddress(int opcode, int sv, int v1, int v2) {
        //int hi = 0;
        //int lo = 0;
        if (opcode == LnConstants.OPC_INPUT_REP) {  /* return 1-4096 address */

            return ((SENSOR_ADR(v1, v2) - 1) * 2 + ((v2 & LnConstants.OPC_INPUT_REP_SW) != 0 ? 2 : 1));
        } else if (opcode == LnConstants.OPC_SW_REQ) {
            // if ( ((v2 & 0xCF) == 0x0F)  && ((v1 & 0xFC) == 0x78) ) { // broadcast address LPU V1.0 page 12
            // "Request Switch to broadcast address with bits "+
            // "a="+ ((sw2&0x20)>>5)+((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0 ? " (Closed)" : " (Thrown)")+
            // " c="+ ((sw1 & 0x02)>>1) +
            // " b="+ ((sw1 & 0x01)) +
            // "\n\tOutput "+
            // ((sw2 & LnConstants.OPC_SW_REQ_OUT)!=0 ? "On"     : "Off")+"\n";
            // } else if ( ((v2 & 0xCF) == 0x07)  && ((v1 & 0xFC) == 0x78) ) { // broadcast address LPU V1.0 page 13
            // "Request switch command is Interrogate LocoNet with bits "+
            // "a="+ ((sw2 & 0x20)>>5) +
            // " c="+ ((sw1&0x02)>>1) +
            // " b="+ ((sw1&0x01)) +
            // "\n\tOutput "+
            // ((sw2 & LnConstants.OPC_SW_REQ_OUT)!=0 ? "On"     : "Off")+"\n"+
            // ( ( (sw2&0x10) == 0 ) ? "" : "\tNote 0x10 bit in sw2 is unexpectedly 0\n");
            // } else { // normal command
            return (SENSOR_ADR(v1, v2));
            //}
        } else if (opcode == LnConstants.OPC_SW_REP) {
            return (SENSOR_ADR(v1, v2));
        }
        return -1;
    }

    protected int valuesToAddress(LocoIOMode lim, int sv, int v1, int v2) {
        if (lim == null) {
            return 0;
        }
        return valuesToAddress(lim.getOpCode(), sv, v1, v2);
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoIOModeList.class);

}
