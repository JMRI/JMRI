/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmri.jmrix.can.cbus;

import java.util.*;
import jmri.jmrix.AbstractMessage;

/**
 * CbusOpCodes.java
 *
 * Description:		methods to decode CBUS opcodes
 *
 * @author		Andrew Crosland   Copyright (C) 2009
 * @version $Revision$
 */
public class CbusOpCodes {
    /**
     * Return a string representation of a decoded CBUS Message
     *
     * @param msg CbusMessage to be decoded
     * Return String decoded message
     */
    public static String decode(AbstractMessage msg) {
        //String str = "";
        StringBuffer buf = new StringBuffer();
        int bytes;
        int value;

        // look for the opcode
        String format = opcodeMap.get(msg.getElement(0));
        if (format == null) return "Reserved opcode";

        // split the format string at each comma
        String [] fields = format.split(",");
        
        int idx = 1;
        for (int i = 0; i < fields.length; i++){
            if (fields[i].startsWith("%")) {
                // replace with bytes from the message
                value = 0;
                bytes = Integer.parseInt(fields[i].substring(1, 2));
                for ( ; bytes > 0; bytes--) {
                    value = value*256 + msg.getElement(idx++);
                }
                fields[i] = String.valueOf(value);
            }
            // concatenat to the result
            //str = str + fields[i];
            buf.append(fields[i]);
        }
        //return str;
        return buf.toString();
    }

    public static final Map<Integer, String> opcodeMap = createMap();

    /*
     * Populate hashmap with format strings keyed by opcode
     *
     * The format string is used to decode and display the CBUS message. At the
     * moment only very simple %x formats are supported where x is a single
     * digit specifying the number of bytes form the message to be displayed.
     * The format string must be separated into fragments to be displayed and
     * format specifiers with comma characters.
     */
    private static Map<Integer, String> createMap() {
        Map<Integer, String> result = new HashMap<Integer, String>();
        // Opcodes with no data
        result.put(CbusConstants.CBUS_HLT,"Bus Halt (HLT)");
        result.put(CbusConstants.CBUS_BON,"Bus ON (BON)");
        result.put(CbusConstants.CBUS_TOF,"Track Off (TOF)");
        result.put(CbusConstants.CBUS_TON,"Track On (TON)");
        result.put(CbusConstants.CBUS_ESTOP,"Track Stopped (ESTOP)");
        result.put(CbusConstants.CBUS_ARST,"System Reset (ARST)");
        result.put(CbusConstants.CBUS_RTOF,"Request Track Off (RTOF)");
        result.put(CbusConstants.CBUS_RTON,"Request Track On (RTON)");
        result.put(CbusConstants.CBUS_RESTP,"Request Emergency Stop All (RESTP)");

        result.put(CbusConstants.CBUS_RDPAR,"Read Node Parameters (RDPAR)");

        // Opcodes with 1 data
        result.put(CbusConstants.CBUS_KLOC,"Release Engine (KLOC)");

        // Opcodes with 2 data
        result.put(CbusConstants.CBUS_RLOC,"Request Session (RLOC) Addr:,%2");
        result.put(CbusConstants.CBUS_SNN,"Set Node Number (SNN) NN:,%2");
        result.put(CbusConstants.CBUS_STMOD,"Throttle speed steps (STMOD) Handle:,%1, Mode:,%1");
        result.put(CbusConstants.CBUS_PCON,"Consist (PCON) Handle:,%1, Consist addr:,%1");
        result.put(CbusConstants.CBUS_DSPD,"Speed/Dir (DSPD) Handle:,%1, Speed/dir:,%1");
        result.put(CbusConstants.CBUS_SSTAT,"Service mode status (SSTAT) Handle:,%1, Status:,%1");
        
        result.put(CbusConstants.CBUS_NNACK,"NN Acknowledge (NNACK) NN:,%2");
        result.put(CbusConstants.CBUS_NNREL,"NN Release (NNREL) NN:,%2");
        result.put(CbusConstants.CBUS_NNREF,"Keep Alive (NNREF) NN:,%2");
        result.put(CbusConstants.CBUS_NNLRN,"Enter Learn Mode (NNLRN) NN:,%2");
        result.put(CbusConstants.CBUS_NNULN,"Exit Learn Mode (NNULN) NN:,%2");
        result.put(CbusConstants.CBUS_NNCLR,"Clear All Events (NNCLR) NN:,%2");
        result.put(CbusConstants.CBUS_NNEVN,"Request Event Space (NNEVN) NN:,%2");
        result.put(CbusConstants.CBUS_BOOT,"Enter Boot Mode (BOOT) NN:,%2");

        // Opcodes with 3 data
        result.put(CbusConstants.CBUS_DFUN,"Set functions (DFUN) Handle:,%1, Range:,%1, Fn:,%1");
        result.put(CbusConstants.CBUS_ERR,"Error (ERR) Addr:,%2, Error:,%1");

        result.put(CbusConstants.CBUS_ENNLF,"Event Space Reply (ENNLF) NN:,%2, Space:,%1");
        result.put(CbusConstants.CBUS_NVRD,"Request Node Variable (NVRD) NN:,%2, NV:,%1");

        // Opcodes with 4 data
        result.put(CbusConstants.CBUS_RDCC3,"DCC 3 byte pkt (RDCC3) Repeat:,%1, Byte 1:,%1, 2:,%1, 3:,%1");
        result.put(CbusConstants.CBUS_WCVO,"Write CV byte (WCVO) Handle:,%1, CV:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_WCVB,"Write CV bit (WCVB) Handle:,%1, CV:,%2, Data:,%1");
        result.put(CbusConstants.CBUS_QCVS,"Read CV (QCVS) Handle:,%1, CV:,%2, Mode:,%1");
        result.put(CbusConstants.CBUS_PCVS,"Report CV (PCVS) Handle:,%1, CV:,%2, Data:,%1");

        result.put(CbusConstants.CBUS_ACON,"Accessory ON (ACON) NN:,%2, EV:,%2");
        result.put(CbusConstants.CBUS_ACOF,"Accessory OFF (ACOF) NN:,%2, EV:,%2");
        result.put(CbusConstants.CBUS_AREQ,"Accessory Request (AREQ) NN:,%2, EV:,%2");

        result.put(CbusConstants.CBUS_ASON,"Accessory Short ON (ASON) [NN:,%2,] EV:,%2");
        result.put(CbusConstants.CBUS_ASOF,"Accessory Short OFF (ASOF) [NN:,%2,] EV:,%2");

        result.put(CbusConstants.CBUS_EVRD,"Read Event (EVRD) NN:,%2 EV:,%2");
        result.put(CbusConstants.CBUS_EVULN,"Unlearn Event (EVULN) NN:,%2 EV:,%2");
        result.put(CbusConstants.CBUS_NVSET,"Set Node Variable (NVSET) NN:,%2 NV:,%1, VAL:,%1");
        result.put(CbusConstants.CBUS_NVANS,"Returned Node Variable (NVANS) NN:,%2 NV:,%1 VAL:,%1");

        // Opcodes with 5 data
        result.put(CbusConstants.CBUS_WCVS,"Write CV (WCVS) Handle:,%1, CV:,%2, Mode:,%1, Data:,%1");

        // Opcodes with 6 data
        result.put(CbusConstants.CBUS_WCVOA,"Write CV (WCVOA) Address:,%2, CV:,%2, Mode:,%1, Data:,%1");

        result.put(CbusConstants.CBUS_EVLRN,"Teach Event (EVLRN) NN:,%2, EV:,%2, EV1:,%1, EV2:,%1");
        result.put(CbusConstants.CBUS_EVANS,"Returned Event (EVANS) NN:,%2, EV:,%2, EV1:,%1, EV2:,%1");

        // Opcodes with 7 data
        result.put(CbusConstants.CBUS_PLOC,"Engine report (PLOC) Handle:,%1, Addr:,%2, Spd:,%1, F1:,%1, F2:,%1, F3:,%1");

        return Collections.unmodifiableMap(result);
    }

}

/* @(#)CbusOpCodes.java */
