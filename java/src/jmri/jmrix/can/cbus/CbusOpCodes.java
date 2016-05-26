/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.can.cbus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrix.AbstractMessage;

/**
 * CbusOpCodes.java
 *
 * Description:	methods to decode CBUS opcodes
 *
 * @author	Andrew Crosland Copyright (C) 2009
 * @version $Revision$
 */
public class CbusOpCodes {

    /**
     * Return a string representation of a decoded CBUS Message
     *
     * @param msg CbusMessage to be decoded Return String decoded message
     * @return decoded CBUS message
     */
    public static String decode(AbstractMessage msg) {
        StringBuilder buf = new StringBuilder();
        int bytes;
        int value;

        // look for the opcode
        String format = opcodeMap.get(msg.getElement(0));
        if (format == null) {
            return "Reserved opcode";
        }

        // split the format string at each comma
        String[] fields = format.split(",");

        int idx = 1;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].startsWith("%")) {
                // replace with bytes from the message
                value = 0;
                bytes = Integer.parseInt(fields[i].substring(1, 2));
                for (; bytes > 0; bytes--) {
                    value = value * 256 + msg.getElement(idx++);
                }
                fields[i] = String.valueOf(value);
            }
            // concatenat to the result
            buf.append(fields[i]);
        }
        return buf.toString();
    }

    /**
     * Return a string representation of a decoded CBUS Message
     *
     * @param msg CbusMessage to be decoded
     * @param ext flag for extended message Return String decoded message
     * @param header CAN Header
     */
    public static String decode(AbstractMessage msg, Boolean ext, int header) {
        if (ext == false) {
            return decode(msg);
        }

        return "Bootloader Message Type: " + header;
    }

    /**
     * Hashmap for decoding CBUS opcodes {@code <opc, string description>}
     */
    public static final Map<Integer, String> opcodeMap = createMap();

    /*
     * Populate hashmap with format strings keyed by opcode
     *
     * The format string is used to decode and display the CBUS message. At the
     * moment only very simple %x formats are supported where x is a single
     * digit specifying the number of bytes from the message to be displayed.
     * The format string must be separated into fragments to be displayed and
     * format specifiers with comma characters.
     */
    private static Map<Integer, String> createMap() {
        Map<Integer, String> result = new HashMap<>();
        // Opcodes with no data
        result.put(CbusConstants.CBUS_ACK, "General Acknowledgement (ACK)");
        result.put(CbusConstants.CBUS_NAK, "No Ack (NAK)");
        result.put(CbusConstants.CBUS_HLT, "Bus Halt (HLT)");
        result.put(CbusConstants.CBUS_BON, "Bus ON (BON)");
        result.put(CbusConstants.CBUS_TOF, "Track Off (TOF)");
        result.put(CbusConstants.CBUS_TON, "Track On (TON)");
        result.put(CbusConstants.CBUS_ESTOP, "Emergency Stop (ESTOP)");
        result.put(CbusConstants.CBUS_ARST, "System Reset (ARST)");
        result.put(CbusConstants.CBUS_RTOF, "Request Track Off (RTOF)");
        result.put(CbusConstants.CBUS_RTON, "Request Track On (RTON)");
        result.put(CbusConstants.CBUS_RESTP, "Request Emergency Stop All (RESTP)");

        result.put(CbusConstants.CBUS_RSTAT, "Request Command Station Status (RSTAT)");
        result.put(CbusConstants.CBUS_QNN, "Query Node number (QNN)");

        result.put(CbusConstants.CBUS_RQNP, "Request Node Parameters (RQNP)");
        result.put(CbusConstants.CBUS_RQMN, "Request Module Name (RQMN)");

        // Opcodes with 1 data
        result.put(CbusConstants.CBUS_KLOC, "Release Engine (KLOC) Session: ,%1");
        result.put(CbusConstants.CBUS_QLOC, "Query Engine (QLOC) Session: ,%1");
        result.put(CbusConstants.CBUS_DKEEP, "Keep Alive (DKEEP) Session: ,%1");

        result.put(CbusConstants.CBUS_DBG1, "Debug (DBG1) Data: ,%1");

        result.put(CbusConstants.CBUS_EXTC, "Extended op-code (EXTC): ,%1");

        // Opcodes with 2 data
        result.put(CbusConstants.CBUS_RLOC, "Request Session (RLOC) Addr:,%2");
        result.put(CbusConstants.CBUS_SNN, "Set Node Number (SNN) NN:,%2");
        result.put(CbusConstants.CBUS_STMOD, "Throttle speed steps (STMOD) Session:,%1, Mode:,%1");
        result.put(CbusConstants.CBUS_PCON, "Consist (PCON) Session:,%1, Consist addr:,%1");
        result.put(CbusConstants.CBUS_KCON, "De-Consist (KCON) Session:,%1, Consist addr:,%1");
        result.put(CbusConstants.CBUS_DSPD, "Speed/Dir (DSPD) Session:,%1, Speed/dir:,%1");
        result.put(CbusConstants.CBUS_DFLG, "Set Engine Flags (DFLG) Session:,%1, Flags:,%1");
        result.put(CbusConstants.CBUS_DFNON, "Set Engine Function On (DFNON) Session:,%1, Fn:,%1");
        result.put(CbusConstants.CBUS_DFNOF, "Set Engine Function Off (DFNOF) Session:,%1, Fn:,%1");
        result.put(CbusConstants.CBUS_SSTAT, "Service mode status (SSTAT) Session:,%1, Status:,%1");

        result.put(CbusConstants.CBUS_RQNN, "NN Request (RQNN) NN:,%2");
        result.put(CbusConstants.CBUS_NNREL, "NN Release (NNREL) NN:,%2");
        result.put(CbusConstants.CBUS_NNACK, "NN Acknowledge (NNREF) NN:,%2");
        result.put(CbusConstants.CBUS_NNLRN, "Enter Learn Mode (NNLRN) NN:,%2");
        result.put(CbusConstants.CBUS_NNULN, "Exit Learn Mode (NNULN) NN:,%2");
        result.put(CbusConstants.CBUS_NNCLR, "Clear All Events (NNCLR) NN:,%2");
        result.put(CbusConstants.CBUS_NNEVN, "Request Event Space (NNEVN) NN:,%2");
        result.put(CbusConstants.CBUS_NERD, "Read All Events (NERD) NN:,%2");
        result.put(CbusConstants.CBUS_RQEVN, "Read Number of Events (RQEVN) NN:,%2");
        result.put(CbusConstants.CBUS_WRACK, "Write Acknowledge (WRACK) NN:,%2");
        result.put(CbusConstants.CBUS_RQDAT, "Request Data Event (RQDAT) NN:,%2");
        result.put(CbusConstants.CBUS_RQDDS, "Request Device Data (RQDDS) NN:,%2");
        result.put(CbusConstants.CBUS_BOOTM, "Enter Boot Mode (BOOTM) NN:,%2");
        result.put(CbusConstants.CBUS_ENUM, "Force enumeration (ENUM) NN:,%2");

        result.put(CbusConstants.CBUS_EXTC1, "Extended Op-code (EXTC1):,%1, Data:,%1");

        // Opcodes with 3 data
        result.put(CbusConstants.CBUS_DFUN, "Set functions (DFUN) Session:,%1, Range:,%1, Fn:,%1");
        result.put(CbusConstants.CBUS_GLOC, "Get Session (GLOC) Addr:,%2, Flags:,%1");
        result.put(CbusConstants.CBUS_ERR, "Error (ERR) Data:,%1, ,%1, ,%1");

        result.put(CbusConstants.CBUS_CMDERR, "Configuration Error (CMDERR) NN:,%2, Error:,%1");

        result.put(CbusConstants.CBUS_EVNLF, "Event Space Reply (ENNLF) NN:,%2, Space:,%1");
        result.put(CbusConstants.CBUS_NVRD, "Request Node Variable (NVRD) NN:,%2, NV:,%1");
        result.put(CbusConstants.CBUS_NENRD, "Request Event By Index (NENRD) NN:,%2, EN:,%1");
        result.put(CbusConstants.CBUS_RQNPN, "Request Parameter By Index (RQNPN) NN:,%2, Para:,%1");
        result.put(CbusConstants.CBUS_NUMEV, "Number of Events (NUMEV) NN:,%2, Events:,%1");
        result.put(CbusConstants.CBUS_CANID, "Set CAN ID (CANID) NN:,%2, CAN ID:,%1");

        result.put(CbusConstants.CBUS_EXTC2, "Extended Op-code (EXTC2):,%1, Data:,%1, ,%1");

        // Opcodes with 4 data
        result.put(CbusConstants.CBUS_RDCC3, "DCC 3 byte pkt (RDCC3) Repeat:,%1, Byte 1:,%1, 2:,%1, 3:,%1");
        result.put(CbusConstants.CBUS_WCVO, "Write CV byte (WCVO) Session:,%1, CV:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_WCVB, "Write CV bit (WCVB) Session:,%1, CV:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_QCVS, "Read CV (QCVS) Session:,%1, CV:,%2, Mode:,%1");
        result.put(CbusConstants.CBUS_PCVS, "Report CV (PCVS) Session:,%1, CV:,%2, Data:,%1");

        result.put(CbusConstants.CBUS_ACON, "Accessory ON (ACON) NN:,%2, EN:,%2");
        result.put(CbusConstants.CBUS_ACOF, "Accessory OFF (ACOF) NN:,%2, EN:,%2");
        result.put(CbusConstants.CBUS_AREQ, "Accessory Request (AREQ) NN:,%2, EN:,%2");
        result.put(CbusConstants.CBUS_ARON, "Accessory Response ON (ARON) [NN:,%2,] EN:,%2");
        result.put(CbusConstants.CBUS_AROF, "Accessory Response OFF (AROF) [NN:,%2,] EN:,%2");
        result.put(CbusConstants.CBUS_EVULN, "Unlearn Event (EVULN) NN:,%2 EN:,%2");
        result.put(CbusConstants.CBUS_NVSET, "Set Node Variable (NVSET) NN:,%2 NV:,%1, VAL:,%1");
        result.put(CbusConstants.CBUS_NVANS, "Returned Node Variable (NVANS) NN:,%2 NV:,%1 VAL:,%1");
        result.put(CbusConstants.CBUS_ASON, "Accessory Short ON (ASON) NN:,%2, DN:,%2");
        result.put(CbusConstants.CBUS_ASOF, "Accessory Short OFF (ASOF) NN:,%2, DN:,%2");
        result.put(CbusConstants.CBUS_ASRQ, "Accessory Short Request (ASRQ) NN:,%2, DN:,%2");
        result.put(CbusConstants.CBUS_PARAN, "Accessory Short Request (PARAN) NN:,%2, Para:,%1, Val:,%1");
        result.put(CbusConstants.CBUS_REVAL, "Accessory Short Request (REVAL) NN:,%2, EN:,%1, EV:,%1");
        result.put(CbusConstants.CBUS_ARSON, "Accessory Short Request (ARSON) NN:,%2, DN:,%2");
        result.put(CbusConstants.CBUS_ARSOF, "Accessory Short Request (ARSOF) NN:,%2, DN:,%2");
        result.put(CbusConstants.CBUS_EXTC3, "Extended Op-code (EXTC3):,%1, Data:,%1, ,%1, ,%1");

        // Opcodes with 5 data
        result.put(CbusConstants.CBUS_RDCC4, "DCC 4 byte pkt (RDCC4) Repeat:,%1, Byte 1:,%1, 2:,%1, 3:,%1, 4:,%1");
        result.put(CbusConstants.CBUS_WCVS, "Write CV (WCVS) Session:,%1, CV:,%2, Mode:,%1, Data:,%1");

        result.put(CbusConstants.CBUS_ACON1, "Accessory ON (ACON1) NN:,%2, EN:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_ACOF1, "Accessory OFF (ACOF1) NN:,%2, EN:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_REQEV, "Learn Mode Read Event (REQEV) NN:,%2, EN:,%2, EV:,%1");
        result.put(CbusConstants.CBUS_ARON1, "Accessory Response ON (ARON1) [NN:,%2,] EN:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_AROF1, "Accessory Response OFF (AROF1) [NN:,%2,] EN:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_NEVAL, "Event Value Response (NEVAL) NN:,%2, EN:,%2, Val:,%1");
        result.put(CbusConstants.CBUS_PNN, "Query Node Response (NEVAL) NN:,%2, Man ID:,%1, Mod ID:,%1, Flags:,%1");
        result.put(CbusConstants.CBUS_ASON1, "Accessory Short ON (ASON1) NN:,%2, DN:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_ASOF1, "Accessory Short OFF (ASOF1) NN:,%2, DN:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_ARSON1, "Accessory Short Request (ARSON1) NN:,%2, DN:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_ARSOF1, "Accessory Short Request (ARSOF1) NN:,%2, DN:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_EXTC4, "Extended Op-code (EXTC4):,%1, Data:,%1, ,%1, ,%1, ,%1");

        // Opcodes with 6 data
        result.put(CbusConstants.CBUS_RDCC5, "DCC 5 byte pkt (RDCC5) Repeat:,%1, Byte 1:,%1, 2:,%1, 3:,%1, 4:,%1, 5:,%1");
        result.put(CbusConstants.CBUS_WCVOA, "Write CV (WCVOA) Address:,%2, CV:,%2, Mode:,%1, Data:,%1");

        result.put(CbusConstants.CBUS_FCLK, "Fast Clock (FCLK) M:,%1, H:,%1, wdmon:,%1, div:,%1, mday:,%1, temp:,%1");

        result.put(CbusConstants.CBUS_ACON2, "Accessory ON (ACON2) NN:,%2, EN:,%2, Data:,%1, ,%1");
        result.put(CbusConstants.CBUS_ACOF2, "Accessory OFF (ACOF2) NN:,%2, EN:,%2, Data:,%1, ,%1");
        result.put(CbusConstants.CBUS_EVLRN, "Teach Event (EVLRN) NN:,%2, EV:,%2, EV1:,%1, EV2:,%1");
        result.put(CbusConstants.CBUS_EVANS, "Returned Event (EVANS) NN:,%2, EV:,%2, EV1:,%1, EV2:,%1");
        result.put(CbusConstants.CBUS_ARON2, "Accessory Response ON (ARON2) [NN:,%2,] EN:,%2, Data:,%1, ,%1");
        result.put(CbusConstants.CBUS_AROF2, "Accessory Response OFF (AROF2) [NN:,%2,] EN:,%2, Data:,%1, ,%1");

        result.put(CbusConstants.CBUS_ASON2, "Accessory Short ON (ASON2) NN:,%2, DN:,%2, Data:,%1, ,%1");
        result.put(CbusConstants.CBUS_ASOF2, "Accessory Short OFF (ASOF2) NN:,%2, DN:,%2, Data:,%1, ,%1");

        result.put(CbusConstants.CBUS_ARSON2, "Accessory Short Request (ARSON2) NN:,%2, DN:,%2, Data:,%1, ,%1");
        result.put(CbusConstants.CBUS_ARSOF2, "Accessory Short Request (ARSOF2) NN:,%2, DN:,%2, Data:,%1, ,%1");
        result.put(CbusConstants.CBUS_EXTC5, "Extended Op-code (EXTC5):,%1, Data:,%1, ,%1, ,%1, ,%1, ,%1");

        // Opcodes with 7 data
        result.put(CbusConstants.CBUS_RDCC6, "DCC 6 byte pkt (RDCC6) Repeat:,%1, Byte 1:,%1, 2:,%1, 3:,%1, 4:,%1, 5:,%1, 6:,%1");
        result.put(CbusConstants.CBUS_PLOC, "Engine report (PLOC) Session:,%1, Addr:,%2, Spd:,%1, F1:,%1, F2:,%1, F3:,%1");
        result.put(CbusConstants.CBUS_NAME, "Node Name (NAME) chars:,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_STAT, "Command Station Status Report (STAT): NN:,%2, CS:,%1, Flags:,%1, Version:,%1, ,%1, ,%1");

        result.put(CbusConstants.CBUS_PARAMS, "Node Parameters (PARAMS) Paras:,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1");

        result.put(CbusConstants.CBUS_ACON3, "Accessory ON (ACON3) NN:,%2, EN:,%2, Data:,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_ACOF3, "Accessory OFF (ACOF3) NN:,%2, EN:,%2, Data:,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_ENRSP, "Node Event (ENRSP) NN:,%2, EN:,%4, EN:,%1");
        result.put(CbusConstants.CBUS_ARON3, "Accessory Response ON (ARON3) [NN:,%2,] EN:,%2, Data:,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_AROF3, "Accessory Response OFF (AROF3) [NN:,%2,] EN:,%2, Data:,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_EVLRNI, "Learn Mode Event By Index (EVLRNI) NN:,%2, EN:,%2, EN#:,%1, EV#:,%1, Value:,%1");
        result.put(CbusConstants.CBUS_ACDAT, "Accessory Data Event (ACDAT) Data:,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_ARDAT, "Accessory Data Response (ARDAT) Data:,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_ASON3, "Accessory Short ON (ASON3) NN:,%2, DN:,%2, Data:,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_ASOF3, "Accessory Short OFF (ASOF3) NN:,%2, DN:,%2, Data:,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_DDES, "Device Data Event Short (DDES) DN:,%2, Data:,%1, ,%1, ,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_DDRS, "Device Data Response Short (DDRS) DN:,%2, Data:,%1, ,%1, ,%1, ,%1, ,%1");

        result.put(CbusConstants.CBUS_ARSON3, "Accessory Short Request (ARSON3) NN:,%2, DN:,%2, Data:,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_ARSOF3, "Accessory Short Request (ARSOF3) NN:,%2, DN:,%2, Data:,%1, ,%1, ,%1");
        result.put(CbusConstants.CBUS_EXTC6, "Extended Op-code (EXTC6):,%1, Data:,%1, ,%1, ,%1, ,%1, ,%1, ,%1");

        return Collections.unmodifiableMap(result);
    }

    /**
     * Set of CBUS short event opcodes
     */
    public static final Set<Integer> shortOpcodes = createShort();

    /**
     * Test if CBUS opcode represents a short event
     *
     * @param opc CBUS op code
     * @return True if opcode represents a short event
     */
    public static boolean isShortEvent(int opc) {
        return shortOpcodes.contains(opc);
    }

    /*
     * Populate hashset with list of short opcodes
     */
    private static Set<Integer> createShort() {
        Set<Integer> result = new HashSet<>();
        // Opcodes with 4 data
        result.add(CbusConstants.CBUS_ASON);
        result.add(CbusConstants.CBUS_ASOF);
        result.add(CbusConstants.CBUS_ASRQ);
        result.add(CbusConstants.CBUS_ARSON);
        result.add(CbusConstants.CBUS_ARSOF);

        // Opcodes with 5 data
        result.add(CbusConstants.CBUS_ASON1);
        result.add(CbusConstants.CBUS_ASOF1);
        result.add(CbusConstants.CBUS_ARSON1);
        result.add(CbusConstants.CBUS_ARSOF1);

        // Opcodes with 6 data
        result.add(CbusConstants.CBUS_ASON2);
        result.add(CbusConstants.CBUS_ASOF2);
        result.add(CbusConstants.CBUS_ARSON2);
        result.add(CbusConstants.CBUS_ARSOF2);

        // Opcodes with 7 data
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ASOF3);
        result.add(CbusConstants.CBUS_ARSON3);
        result.add(CbusConstants.CBUS_ARSOF3);

        return Collections.unmodifiableSet(result);
    }

}

/* @(#)CbusOpCodes.java */
