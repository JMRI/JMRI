package jmri.jmrix.can.cbus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrix.AbstractMessage;

/**
 * Methods to decode CBUS opcodes
 *
 * https://github.com/MERG-DEV/CBUSlib
 * @author Andrew Crosland Copyright (C) 2009
 * @author Steve Young (C) 2018
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
            return Bundle.getMessage("OPC_RESERVED");
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

        return Bundle.getMessage("OPC_BOOT_TYP") + header;
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
        result.put(CbusConstants.CBUS_ACK, Bundle.getMessage("CBUS_ACK")); // NOI18N
        result.put(CbusConstants.CBUS_NAK, Bundle.getMessage("CBUS_NAK")); // NOI18N
        result.put(CbusConstants.CBUS_HLT, Bundle.getMessage("CBUS_HLT")); // NOI18N
        result.put(CbusConstants.CBUS_BON, Bundle.getMessage("CBUS_BON")); // NOI18N
        result.put(CbusConstants.CBUS_TOF, Bundle.getMessage("CBUS_TOF")); // NOI18N
        result.put(CbusConstants.CBUS_TON, Bundle.getMessage("CBUS_TON")); // NOI18N
        result.put(CbusConstants.CBUS_ESTOP, Bundle.getMessage("CBUS_ESTOP")); // NOI18N
        result.put(CbusConstants.CBUS_ARST, Bundle.getMessage("CBUS_ARST")); // NOI18N
        result.put(CbusConstants.CBUS_RTOF, Bundle.getMessage("CBUS_RTOF")); // NOI18N
        result.put(CbusConstants.CBUS_RTON, Bundle.getMessage("CBUS_RTON")); // NOI18N
        result.put(CbusConstants.CBUS_RESTP, Bundle.getMessage("CBUS_RESTP")); // NOI18N
        result.put(CbusConstants.CBUS_RSTAT, Bundle.getMessage("CBUS_RSTAT")); // NOI18N
        result.put(CbusConstants.CBUS_QNN, Bundle.getMessage("CBUS_QNN")); // NOI18N
        result.put(CbusConstants.CBUS_RQNP, Bundle.getMessage("CBUS_RQNP")); // NOI18N
        result.put(CbusConstants.CBUS_RQMN, Bundle.getMessage("CBUS_RQMN")); // NOI18N

        // Opcodes with 1 data
        result.put(CbusConstants.CBUS_KLOC, Bundle.getMessage("CBUS_KLOC") + " " + 
        Bundle.getMessage("OPC_SN") + ": ,%1"); // NOI18N
        result.put(CbusConstants.CBUS_QLOC, Bundle.getMessage("CBUS_QLOC") + " " + 
        Bundle.getMessage("OPC_SN") + ": ,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DKEEP, Bundle.getMessage("CBUS_DKEEP") + " " + 
        Bundle.getMessage("OPC_SN") + ": ,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DBG1, Bundle.getMessage("CBUS_DBG1") + " " + 
        Bundle.getMessage("OPC_DA") + ": ,%1"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC, Bundle.getMessage("CBUS_EXTC") + " : ,%1");

        // Opcodes with 2 data
        result.put(CbusConstants.CBUS_RLOC, Bundle.getMessage("CBUS_RLOC") + " " + 
        Bundle.getMessage("OPC_AD") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_QCON, Bundle.getMessage("CBUS_QCON") + " " + 
        Bundle.getMessage("OPC_AD") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_SNN, Bundle.getMessage("CBUS_SNN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_ALOC, Bundle.getMessage("CBUS_ALOC") + " " + 
        Bundle.getMessage("OPC_AD") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_STMOD, Bundle.getMessage("CBUS_STMOD") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_MD") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_PCON, Bundle.getMessage("CBUS_PCON") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CA") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_KCON, Bundle.getMessage("CBUS_KCON") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CA") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DSPD, Bundle.getMessage("CBUS_DSPD") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_SD") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DFLG, Bundle.getMessage("CBUS_DFLG") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_FL") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DFNON, Bundle.getMessage("CBUS_DFNON") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_FN") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DFNOF, Bundle.getMessage("CBUS_DFNOF") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_FN") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_SSTAT, Bundle.getMessage("CBUS_SSTAT") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_ST") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_RQNN, Bundle.getMessage("CBUS_RQNN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNREL, Bundle.getMessage("CBUS_NNREL") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNACK, Bundle.getMessage("CBUS_NNACK") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNLRN, Bundle.getMessage("CBUS_NNLRN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNULN, Bundle.getMessage("CBUS_NNULN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNCLR, Bundle.getMessage("CBUS_NNCLR") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNEVN, Bundle.getMessage("CBUS_NNEVN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NERD, Bundle.getMessage("CBUS_NERD") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_RQEVN, Bundle.getMessage("CBUS_RQEVN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_WRACK, Bundle.getMessage("CBUS_WRACK") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_RQDAT, Bundle.getMessage("CBUS_RQDAT") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_RQDDS, Bundle.getMessage("CBUS_RQDDS") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_BOOTM, Bundle.getMessage("CBUS_BOOTM") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_ENUM, Bundle.getMessage("CBUS_ENUM") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC1, Bundle.getMessage("CBUS_EXTC1") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N

        // Opcodes with 3 data
        result.put(CbusConstants.CBUS_DFUN, Bundle.getMessage("CBUS_DFUN") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_RG") + ":,%1, " + 
        Bundle.getMessage("OPC_FN") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_GLOC, Bundle.getMessage("CBUS_GLOC") + " " + 
        Bundle.getMessage("OPC_AD") + ":,%2, " + Bundle.getMessage("OPC_FL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ERR, Bundle.getMessage("CBUS_ERR") + " " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_CMDERR, Bundle.getMessage("CBUS_CMDERR") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_ER") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVNLF, Bundle.getMessage("CBUS_EVNLF") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_SP") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NVRD, Bundle.getMessage("CBUS_NVRD") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_NV") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NENRD, Bundle.getMessage("CBUS_NENRD") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_RQNPN, Bundle.getMessage("CBUS_RQNPN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_PA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NUMEV, Bundle.getMessage("CBUS_NUMEV") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("CbusEvents") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_CANID, Bundle.getMessage("CBUS_CANID") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("CanID") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC2, Bundle.getMessage("CBUS_EXTC2") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N

        // Opcodes with 4 data
        result.put(CbusConstants.CBUS_RDCC3, Bundle.getMessage("CBUS_RDCC3") + " " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + " 1:,%1, 2:,%1, 3:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVO, Bundle.getMessage("CBUS_WCVO") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVB, Bundle.getMessage("CBUS_WCVB") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_QCVS, Bundle.getMessage("CBUS_QCVS") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_MD") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PCVS, Bundle.getMessage("CBUS_PCVS") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON, Bundle.getMessage("CBUS_ACON") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF, Bundle.getMessage("CBUS_ACOF") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_AREQ, Bundle.getMessage("CBUS_AREQ") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON, Bundle.getMessage("CBUS_ARON") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF, Bundle.getMessage("CBUS_AROF") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVULN, Bundle.getMessage("CBUS_EVULN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_NVSET, Bundle.getMessage("CBUS_NVSET") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_NV") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NVANS, Bundle.getMessage("CBUS_NVANS") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_NV") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON, Bundle.getMessage("CBUS_ASON") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF, Bundle.getMessage("CBUS_ASOF") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASRQ, Bundle.getMessage("CBUS_ASRQ") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_PARAN, Bundle.getMessage("CBUS_PARAN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_PA") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_REVAL, Bundle.getMessage("CBUS_REVAL") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%1, EV:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON, Bundle.getMessage("CBUS_ARSON") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF, Bundle.getMessage("CBUS_ARSOF") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC3, Bundle.getMessage("CBUS_EXTC3") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 5 data
        result.put(CbusConstants.CBUS_RDCC4, Bundle.getMessage("CBUS_RDCC4") + " " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + " 1:,%1, 2:,%1, 3:,%1, 4:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVS, Bundle.getMessage("CBUS_WCVS") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_MD") + ":,%1, " + Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON1, Bundle.getMessage("CBUS_ACON1") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF1, Bundle.getMessage("CBUS_ACOF1") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_REQEV, Bundle.getMessage("CBUS_REQEV") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_EV") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON1, Bundle.getMessage("CBUS_ARON1") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF1, Bundle.getMessage("CBUS_AROF1") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NEVAL, Bundle.getMessage("CBUS_NEVAL") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%1, " + 
        Bundle.getMessage("OPC_EV") + ":,%1, " + Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PNN, Bundle.getMessage("CBUS_PNN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_MF") + ":,%1, " + 
        Bundle.getMessage("OPC_MI") + ":,%1, " + Bundle.getMessage("OPC_FL") + ":,%1");
        
        result.put(CbusConstants.CBUS_ASON1, Bundle.getMessage("CBUS_ASON1") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF1, Bundle.getMessage("CBUS_ASOF1") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON1, Bundle.getMessage("CBUS_ARSON1") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF1, Bundle.getMessage("CBUS_ARSOF1") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC4, Bundle.getMessage("CBUS_EXTC4") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 6 data
        result.put(CbusConstants.CBUS_RDCC5, Bundle.getMessage("CBUS_RDCC5") + " " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + 
        " 1:,%1, 2:,%1, 3:,%1, 4:,%1, 5:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVOA, Bundle.getMessage("CBUS_WCVOA") + " " + 
        Bundle.getMessage("OPC_AD") + ":,%2, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_MD") + ":,%1, " + Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_FCLK, Bundle.getMessage("CBUS_FCLK") + " " + 
        Bundle.getMessage("OPC_FI") + ":,%1, " + Bundle.getMessage("OPC_FH") + ":,%1, " + 
        Bundle.getMessage("OPC_FW") + ":,%1, " + Bundle.getMessage("OPC_FD") + ":,%1, " + 
        Bundle.getMessage("OPC_FM") + ":,%1, " + Bundle.getMessage("OPC_FT") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON2, Bundle.getMessage("CBUS_ACON2") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF2, Bundle.getMessage("CBUS_ACOF2") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVLRN, Bundle.getMessage("CBUS_EVLRN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_EV") + ":,%1, " + Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVANS, Bundle.getMessage("CBUS_EVANS") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_EV") + ":,%1, " + Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON2, Bundle.getMessage("CBUS_ARON2") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF2, Bundle.getMessage("CBUS_AROF2") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON2, Bundle.getMessage("CBUS_ASON2") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF2, Bundle.getMessage("CBUS_ASOF2") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON2, Bundle.getMessage("CBUS_ARSON2") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF2, Bundle.getMessage("CBUS_ARSOF2") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC5, Bundle.getMessage("CBUS_EXTC5") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 7 data
        result.put(CbusConstants.CBUS_RDCC6, Bundle.getMessage("CBUS_RDCC6") + " " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + 
        " 1:,%1, 2:,%1, 3:,%1, 4:,%1, 5:,%1, 6:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PLOC, Bundle.getMessage("CBUS_PLOC") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_AD") + ":,%2, " + 
        Bundle.getMessage("OPC_SE") + ":,%1, " + Bundle.getMessage("OPC_F1") + ":,%1, " + 
        Bundle.getMessage("OPC_F2") + ":,%1, " + Bundle.getMessage("OPC_F3") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NAME, Bundle.getMessage("CBUS_NAME") + " " + 
        Bundle.getMessage("OPC_CH") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_STAT, Bundle.getMessage("CBUS_STAT") + " : " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_CS") + ":,%1, " + 
        Bundle.getMessage("OPC_FL") + ":,%1, " + Bundle.getMessage("OPC_VN") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PARAMS, Bundle.getMessage("CBUS_PARAMS") + " " + 
        Bundle.getMessage("OPC_PA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON3, Bundle.getMessage("CBUS_ACON3") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF3, Bundle.getMessage("CBUS_ACOF3") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ENRSP, Bundle.getMessage("CBUS_ENRSP") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_NN") + ":,%2, " + 
        Bundle.getMessage("OPC_EN") + ":,%2, " + Bundle.getMessage("OPC_EV") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON3, Bundle.getMessage("CBUS_ARON3") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF3, Bundle.getMessage("CBUS_AROF3") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVLRNI, Bundle.getMessage("CBUS_EVLRNI") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_ET") + ":,%1, " + Bundle.getMessage("OPC_EV") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACDAT, Bundle.getMessage("CBUS_ACDAT") + " " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARDAT, Bundle.getMessage("CBUS_ARDAT") + " " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON3, Bundle.getMessage("CBUS_ASON3") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF3, Bundle.getMessage("CBUS_ASOF3") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_DDES, Bundle.getMessage("CBUS_DDES") + " " + 
        Bundle.getMessage("OPC_DN") + ":,%2, " + Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_DDRS, Bundle.getMessage("CBUS_DDRS") + " " + 
        Bundle.getMessage("OPC_DN") + ":,%2, " + Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON3, Bundle.getMessage("CBUS_ARSON3") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF3, Bundle.getMessage("CBUS_ARSOF3") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC6, Bundle.getMessage("CBUS_EXTC6") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N

        return Collections.unmodifiableMap(result);
    }

    
    
    
    /**
     * Return a string representation of a decoded CBUS Message
     *
     * @param msg CbusMessage to be decoded Return String decoded message
     * @return decoded CBUS message
     */
    public static String decodeopc(AbstractMessage msg) {
        // look for the opcode
        String format = opcMap.get(msg.getElement(0));
        if (format == null) {
            return Bundle.getMessage("OPC_RESERVED");
        } else {
            return format; 
        }
    }

    /**
     * Return a string OPC of a CBUS Message
     *
     * @param msg CbusMessage 
     * @param ext flag for extended message
     * @param header CAN Header
     */
    public static String decodeopc(AbstractMessage msg, Boolean ext, int header) {
        if (ext == false) {
            return decodeopc(msg);
        }
        return Bundle.getMessage("OPC_BOOT_TYP") + header;
    }

    /**
     * Hashmap for decoding CBUS opcodes {@code <opc, string description>}
     */
    public static final Map<Integer, String> opcMap = createoMap();

    /*
     * Populate hashmap with format strings keyed by opcode
     *
     * The format string is used to decode and display the CBUS message. At the
     * moment only very simple %x formats are supported where x is a single
     * digit specifying the number of bytes from the message to be displayed.
     * The format string must be separated into fragments to be displayed and
     * format specifiers with comma characters.
     */
    private static Map<Integer, String> createoMap() {
        Map<Integer, String> result = new HashMap<>();
        // Opcodes with no data
        result.put(CbusConstants.CBUS_ACK, "ACK"); // NOI18N
        result.put(CbusConstants.CBUS_NAK, "NAK"); // NOI18N
        result.put(CbusConstants.CBUS_HLT, "HLT"); // NOI18N
        result.put(CbusConstants.CBUS_BON, "BON"); // NOI18N
        result.put(CbusConstants.CBUS_TOF, "TOF"); // NOI18N
        result.put(CbusConstants.CBUS_TON, "TON"); // NOI18N
        result.put(CbusConstants.CBUS_ESTOP, "ESTOP"); // NOI18N
        result.put(CbusConstants.CBUS_ARST, "ARST"); // NOI18N
        result.put(CbusConstants.CBUS_RTOF, "RTOF"); // NOI18N
        result.put(CbusConstants.CBUS_RTON, "RTON"); // NOI18N
        result.put(CbusConstants.CBUS_RESTP, "RESTP"); // NOI18N
        result.put(CbusConstants.CBUS_RSTAT, "RSTAT"); // NOI18N
        result.put(CbusConstants.CBUS_QNN,   "QNN"); // NOI18N
        result.put(CbusConstants.CBUS_RQNP,  "RQNP"); // NOI18N
        result.put(CbusConstants.CBUS_RQMN,  "RQMN"); // NOI18N
        result.put(CbusConstants.CBUS_KLOC,  "KLOC"); // NOI18N
        result.put(CbusConstants.CBUS_QLOC,  "QLOC"); // NOI18N
        result.put(CbusConstants.CBUS_DKEEP, "DKEEP"); // NOI18N
        result.put(CbusConstants.CBUS_DBG1,  "DBG1"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC,  "EXTC"); // NOI18N
        result.put(CbusConstants.CBUS_RLOC,  "RLOC"); // NOI18N
        result.put(CbusConstants.CBUS_QCON,  "QCON"); // NOI18N
        result.put(CbusConstants.CBUS_SNN,   "SNN"); // NOI18N
        result.put(CbusConstants.CBUS_ALOC,  "ALOC"); // NOI18N
        result.put(CbusConstants.CBUS_STMOD, "STMOD"); // NOI18N
        result.put(CbusConstants.CBUS_PCON,  "PCON"); // NOI18N
        result.put(CbusConstants.CBUS_KCON,  "KCON"); // NOI18N
        result.put(CbusConstants.CBUS_DSPD,  "DSPD"); // NOI18N
        result.put(CbusConstants.CBUS_DFLG,  "DFLG"); // NOI18N
        result.put(CbusConstants.CBUS_DFNON, "DFNON"); // NOI18N
        result.put(CbusConstants.CBUS_DFNOF, "DFNOF"); // NOI18N
        result.put(CbusConstants.CBUS_SSTAT, "SSTAT"); // NOI18N
        result.put(CbusConstants.CBUS_RQNN,  "RQNN"); // NOI18N
        result.put(CbusConstants.CBUS_NNREL, "NNREL"); // NOI18N
        result.put(CbusConstants.CBUS_NNACK, "NNACK"); // NOI18N
        result.put(CbusConstants.CBUS_NNLRN, "NNLRN"); // NOI18N
        result.put(CbusConstants.CBUS_NNULN, "NNULN"); // NOI18N
        result.put(CbusConstants.CBUS_NNCLR, "NNCLR"); // NOI18N
        result.put(CbusConstants.CBUS_NNEVN, "NNEVN"); // NOI18N
        result.put(CbusConstants.CBUS_NERD,  "NERD"); // NOI18N
        result.put(CbusConstants.CBUS_RQEVN, "RQEVN"); // NOI18N
        result.put(CbusConstants.CBUS_WRACK, "WRACK"); // NOI18N
        result.put(CbusConstants.CBUS_RQDAT, "RQDAT"); // NOI18N
        result.put(CbusConstants.CBUS_RQDDS, "RQDDS"); // NOI18N
        result.put(CbusConstants.CBUS_BOOTM, "BOOTM"); // NOI18N
        result.put(CbusConstants.CBUS_ENUM, "ENUM"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC1, "EXTC1"); // NOI18N
        result.put(CbusConstants.CBUS_DFUN, "DFUN"); // NOI18N
        result.put(CbusConstants.CBUS_GLOC, "GLOC"); // NOI18N
        result.put(CbusConstants.CBUS_ERR, "ERR"); // NOI18N
        result.put(CbusConstants.CBUS_CMDERR, "CMDERR"); // NOI18N
        result.put(CbusConstants.CBUS_EVNLF, "EVNLF"); // NOI18N
        result.put(CbusConstants.CBUS_NVRD, "NVRD"); // NOI18N
        result.put(CbusConstants.CBUS_NENRD, "NENRD"); // NOI18N
        result.put(CbusConstants.CBUS_RQNPN, "RQNPN"); // NOI18N
        result.put(CbusConstants.CBUS_NUMEV, "NUMEV"); // NOI18N
        result.put(CbusConstants.CBUS_CANID, "CANID"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC2, "EXTC2"); // NOI18N
        result.put(CbusConstants.CBUS_RDCC3, "RDCC3"); // NOI18N
        result.put(CbusConstants.CBUS_WCVO, "WCVO"); // NOI18N
        result.put(CbusConstants.CBUS_WCVB, "WCVB"); // NOI18N
        result.put(CbusConstants.CBUS_QCVS, "QCVS"); // NOI18N
        result.put(CbusConstants.CBUS_PCVS, "PCVS"); // NOI18N
        result.put(CbusConstants.CBUS_ACON, "ACON"); // NOI18N
        result.put(CbusConstants.CBUS_ACOF, "ACOF"); // NOI18N
        result.put(CbusConstants.CBUS_AREQ, "AREQ"); // NOI18N
        result.put(CbusConstants.CBUS_ARON, "ARON"); // NOI18N
        result.put(CbusConstants.CBUS_AROF, "AROF"); // NOI18N
        result.put(CbusConstants.CBUS_EVULN, "EVULN"); // NOI18N
        result.put(CbusConstants.CBUS_NVSET, "NVSET"); // NOI18N
        result.put(CbusConstants.CBUS_NVANS, "NVANS"); // NOI18N
        result.put(CbusConstants.CBUS_ASON, "ASON"); // NOI18N
        result.put(CbusConstants.CBUS_ASOF, "ASOF"); // NOI18N
        result.put(CbusConstants.CBUS_ASRQ, "ASRQ"); // NOI18N
        result.put(CbusConstants.CBUS_PARAN, "PARAN"); // NOI18N
        result.put(CbusConstants.CBUS_REVAL, "REVAL"); // NOI18N
        result.put(CbusConstants.CBUS_ARSON, "ARSON"); // NOI18N
        result.put(CbusConstants.CBUS_ARSOF, "ARSOF"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC3, "EXTC3"); // NOI18N
        result.put(CbusConstants.CBUS_RDCC4, "RDCC4"); // NOI18N
        result.put(CbusConstants.CBUS_WCVS, "WCVS"); // NOI18N
        result.put(CbusConstants.CBUS_ACON1, "ACON1"); // NOI18N
        result.put(CbusConstants.CBUS_ACOF1, "ACOF1"); // NOI18N
        result.put(CbusConstants.CBUS_REQEV, "REQEV"); // NOI18N
        result.put(CbusConstants.CBUS_ARON1, "ARON1"); // NOI18N
        result.put(CbusConstants.CBUS_AROF1, "AROF1"); // NOI18N
        result.put(CbusConstants.CBUS_NEVAL, "NEVAL"); // NOI18N
        result.put(CbusConstants.CBUS_PNN, "PNN"); // NOI18N
        result.put(CbusConstants.CBUS_ASON1, "ASON1"); // NOI18N
        result.put(CbusConstants.CBUS_ASOF1, "ASOF1"); // NOI18N
        result.put(CbusConstants.CBUS_ARSON1, "ARSON1"); // NOI18N
        result.put(CbusConstants.CBUS_ARSOF1, "ARSOF1"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC4, "EXTC4"); // NOI18N
        result.put(CbusConstants.CBUS_RDCC5, "RDCC5"); // NOI18N
        result.put(CbusConstants.CBUS_WCVOA, "WCVOA"); // NOI18N
        result.put(CbusConstants.CBUS_FCLK, "FCLK"); // NOI18N
        result.put(CbusConstants.CBUS_ACON2, "ACON2"); // NOI18N
        result.put(CbusConstants.CBUS_ACOF2, "ACOF2"); // NOI18N
        result.put(CbusConstants.CBUS_EVLRN, "EVLRN"); // NOI18N
        result.put(CbusConstants.CBUS_EVANS, "EVANS"); // NOI18N
        result.put(CbusConstants.CBUS_ARON2, "ARON2"); // NOI18N
        result.put(CbusConstants.CBUS_AROF2, "AROF2"); // NOI18N
        result.put(CbusConstants.CBUS_ASON2, "ASON2"); // NOI18N
        result.put(CbusConstants.CBUS_ASOF2, "ASOF2"); // NOI18N
        result.put(CbusConstants.CBUS_ARSON2, "ARSON2"); // NOI18N
        result.put(CbusConstants.CBUS_ARSOF2, "ARSOF2"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC5, "EXTC5"); // NOI18N
        result.put(CbusConstants.CBUS_RDCC6, "RDCC6"); // NOI18N
        result.put(CbusConstants.CBUS_PLOC, "PLOC"); // NOI18N
        result.put(CbusConstants.CBUS_NAME, "NAME"); // NOI18N
        result.put(CbusConstants.CBUS_STAT, "STAT"); // NOI18N
        result.put(CbusConstants.CBUS_PARAMS, "PARAMS"); // NOI18N
        result.put(CbusConstants.CBUS_ACON3, "ACON3"); // NOI18N
        result.put(CbusConstants.CBUS_ACOF3, "ACOF3"); // NOI18N
        result.put(CbusConstants.CBUS_ENRSP, "ENRSP"); // NOI18N
        result.put(CbusConstants.CBUS_ARON3, "ARON3"); // NOI18N
        result.put(CbusConstants.CBUS_AROF3, "AROF3"); // NOI18N
        result.put(CbusConstants.CBUS_EVLRNI, "EVLRNI"); // NOI18N
        result.put(CbusConstants.CBUS_ACDAT, "ACDAT"); // NOI18N
        result.put(CbusConstants.CBUS_ARDAT, "ARDAT"); // NOI18N
        result.put(CbusConstants.CBUS_ASON3, "ASON3"); // NOI18N
        result.put(CbusConstants.CBUS_ASOF3, "ASOF3"); // NOI18N
        result.put(CbusConstants.CBUS_DDES, "DDES"); // NOI18N
        result.put(CbusConstants.CBUS_DDRS, "DDRS"); // NOI18N
        result.put(CbusConstants.CBUS_ARSON3, "ARSON3"); // NOI18N
        result.put(CbusConstants.CBUS_ARSOF3, "ARSOF3"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC6, "EXTC6"); // NOI18N
        
        return Collections.unmodifiableMap(result);
    }
    
    
    
    /**
     * Set of CBUS event opcodes
     */
    public static final Set<Integer> eventOpcodes = createEventOPC();

    /**
     * Test if CBUS opcode represents an event
     *
     * @param opc CBUS op code
     * @return True if opcode represents an event
     */
    public static boolean isEvent(int opc) {
        return eventOpcodes.contains(opc);
    }

    /*
     * Populate hashset with list of short opcodes
     * Defined in the CBUS Dev manual as accessory commands.
     * includes fast clock as per dev manual
     */
    private static Set<Integer> createEventOPC() {
        Set<Integer> result = new HashSet<>();

        result.add(CbusConstants.CBUS_RQDAT);
        result.add(CbusConstants.CBUS_RQDDS);
        result.add(CbusConstants.CBUS_ACON);
        result.add(CbusConstants.CBUS_ACOF);
        result.add(CbusConstants.CBUS_AREQ);
        result.add(CbusConstants.CBUS_ARON);
        result.add(CbusConstants.CBUS_AROF);
        result.add(CbusConstants.CBUS_ASON);
        result.add(CbusConstants.CBUS_ASOF);
        result.add(CbusConstants.CBUS_ASRQ);
        result.add(CbusConstants.CBUS_ARSON);
        result.add(CbusConstants.CBUS_ARSOF);
        
        result.add(CbusConstants.CBUS_ACON1);
        result.add(CbusConstants.CBUS_ACOF1);
        result.add(CbusConstants.CBUS_ARON1);
        result.add(CbusConstants.CBUS_AROF1);
        result.add(CbusConstants.CBUS_ASON1);
        result.add(CbusConstants.CBUS_ASOF1);
        result.add(CbusConstants.CBUS_ARSON1);
        result.add(CbusConstants.CBUS_ARSOF1);
        
        result.add(CbusConstants.CBUS_FCLK);
        result.add(CbusConstants.CBUS_ACON2);
        result.add(CbusConstants.CBUS_ACOF2);
        result.add(CbusConstants.CBUS_ARON2);
        result.add(CbusConstants.CBUS_AROF2);        
        result.add(CbusConstants.CBUS_ASON2);        
        result.add(CbusConstants.CBUS_ASOF2);
        result.add(CbusConstants.CBUS_ARSON2);
        result.add(CbusConstants.CBUS_ARSOF2);
        
        result.add(CbusConstants.CBUS_ACON3);
        result.add(CbusConstants.CBUS_ACOF3);
        result.add(CbusConstants.CBUS_ARON3);
        result.add(CbusConstants.CBUS_AROF3);
        result.add(CbusConstants.CBUS_ACDAT);
        result.add(CbusConstants.CBUS_ARDAT);
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ASOF3);
        result.add(CbusConstants.CBUS_DDES);
        result.add(CbusConstants.CBUS_DDRS);
        result.add(CbusConstants.CBUS_ARSON3);
        result.add(CbusConstants.CBUS_ARSOF3);
        
        return Collections.unmodifiableSet(result);
    }

    
    
    /**
     * Set of CBUS event opcodes excluding requests + fastclock
     */
    public static final Set<Integer> eventNotRequestOpCodes = createEventNROPC();

    /**
     * Test if CBUS opcode represents a JMRI event table event
     * Event codes excluding request codes + fastclock
     * @param opc CBUS op code
     * @return True if opcode represents an event
     */
    public static boolean isEventNotRequest(int opc) {
        return eventNotRequestOpCodes.contains(opc);
    }

    /*
     * Populate hashset with list of event opcodes
     * Excludes fastclock + response requests.
     */
    private static Set<Integer> createEventNROPC() {
        Set<Integer> result = new HashSet<>();

        result.add(CbusConstants.CBUS_ACON);
        result.add(CbusConstants.CBUS_ACOF);
        result.add(CbusConstants.CBUS_ARON);
        result.add(CbusConstants.CBUS_AROF);
        result.add(CbusConstants.CBUS_ASON);
        result.add(CbusConstants.CBUS_ASOF);
        result.add(CbusConstants.CBUS_ARSON);
        result.add(CbusConstants.CBUS_ARSOF);
        
        result.add(CbusConstants.CBUS_ACON1);
        result.add(CbusConstants.CBUS_ACOF1);
        result.add(CbusConstants.CBUS_ARON1);
        result.add(CbusConstants.CBUS_AROF1);
        result.add(CbusConstants.CBUS_ASON1);
        result.add(CbusConstants.CBUS_ASOF1);
        result.add(CbusConstants.CBUS_ARSON1);
        result.add(CbusConstants.CBUS_ARSOF1);
        
        result.add(CbusConstants.CBUS_ACON2);
        result.add(CbusConstants.CBUS_ACOF2);
        result.add(CbusConstants.CBUS_ARON2);
        result.add(CbusConstants.CBUS_AROF2);        
        result.add(CbusConstants.CBUS_ASON2);        
        result.add(CbusConstants.CBUS_ASOF2);
        result.add(CbusConstants.CBUS_ARSON2);
        result.add(CbusConstants.CBUS_ARSOF2);
        
        result.add(CbusConstants.CBUS_ACON3);
        result.add(CbusConstants.CBUS_ACOF3);
        result.add(CbusConstants.CBUS_ARON3);
        result.add(CbusConstants.CBUS_AROF3);
        result.add(CbusConstants.CBUS_ACDAT);
        result.add(CbusConstants.CBUS_ARDAT);
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ASOF3);
        result.add(CbusConstants.CBUS_DDES);
        result.add(CbusConstants.CBUS_DDRS);
        result.add(CbusConstants.CBUS_ARSON3);
        result.add(CbusConstants.CBUS_ARSOF3);
        
        return Collections.unmodifiableSet(result);
    }
    
    
    
    /**
     * Set of CBUS DCC opcodes
     */
    public static final Set<Integer> dccOpcodes = createDccOPC();

    /**
     * Test if CBUS opcode represents a dcc message
     *
     * @param opc CBUS op code
     * @return True if opcode represents a dcc command
     */
    public static boolean isDcc(int opc) {
        return dccOpcodes.contains(opc);
    }

    /*
     * Populate hashset with list of dcc opcodes
     * Defined in the CBUS Dev manual as dcc commands.
     */
    private static Set<Integer> createDccOPC() {
        Set<Integer> result = new HashSet<>();

        result.add(CbusConstants.CBUS_TOF);
        result.add(CbusConstants.CBUS_TON);
        result.add(CbusConstants.CBUS_ESTOP);
        result.add(CbusConstants.CBUS_RTOF);
        result.add(CbusConstants.CBUS_RTON);
        result.add(CbusConstants.CBUS_RESTP);
        result.add(CbusConstants.CBUS_KLOC);
        result.add(CbusConstants.CBUS_QLOC);
        result.add(CbusConstants.CBUS_DKEEP);
        
        result.add(CbusConstants.CBUS_RLOC);
        result.add(CbusConstants.CBUS_QCON);
        result.add(CbusConstants.CBUS_ALOC);
        result.add(CbusConstants.CBUS_STMOD);
        result.add(CbusConstants.CBUS_PCON);
        result.add(CbusConstants.CBUS_KCON);
        result.add(CbusConstants.CBUS_DSPD);
        result.add(CbusConstants.CBUS_DFLG);
        result.add(CbusConstants.CBUS_DFNON);
        result.add(CbusConstants.CBUS_SSTAT);
        
        result.add(CbusConstants.CBUS_DFUN);
        result.add(CbusConstants.CBUS_GLOC);
        result.add(CbusConstants.CBUS_ERR);
        
        result.add(CbusConstants.CBUS_RDCC3);        
        result.add(CbusConstants.CBUS_WCVO);        
        result.add(CbusConstants.CBUS_WCVB);
        result.add(CbusConstants.CBUS_QCVS);
        result.add(CbusConstants.CBUS_PCVS);
        
        result.add(CbusConstants.CBUS_RDCC4);
        result.add(CbusConstants.CBUS_WCVS);
        
        result.add(CbusConstants.CBUS_RDCC5);
        result.add(CbusConstants.CBUS_WCVOA);
        
        result.add(CbusConstants.CBUS_RDCC6);
        result.add(CbusConstants.CBUS_PLOC);
        result.add(CbusConstants.CBUS_RSTAT);
        
        return Collections.unmodifiableSet(result);
    }

    
    
    /**
     * Set of CBUS ON event opcodes
     */
    public static final Set<Integer> onEvOpcodes = createOnEv();

    /**
     * Test if CBUS opcode represents an on event
     *
     * @param opc CBUS op code
     * @return True if opcode represents an on event
     */
    public static boolean isOnEvent(int opc) {
        return onEvOpcodes.contains(opc);
    }

    /*
     * Populate hashset with list of on opcodes
     */
    private static Set<Integer> createOnEv() {
        Set<Integer> result = new HashSet<>();
        // Opcodes with 4 data
        result.add(CbusConstants.CBUS_ACON);
        result.add(CbusConstants.CBUS_ARON);
        result.add(CbusConstants.CBUS_ASON);
        result.add(CbusConstants.CBUS_ARSON);

        // Opcodes with 5 data
        result.add(CbusConstants.CBUS_ACON1);
        result.add(CbusConstants.CBUS_ARON1);
        result.add(CbusConstants.CBUS_ASON1);
        result.add(CbusConstants.CBUS_ARSON1);

        // Opcodes with 6 data
        result.add(CbusConstants.CBUS_ACON2);
        result.add(CbusConstants.CBUS_ARON2);
        result.add(CbusConstants.CBUS_ASON2);
        result.add(CbusConstants.CBUS_ARSON2);

        // Opcodes with 7 data
        result.add(CbusConstants.CBUS_ACON3);
        result.add(CbusConstants.CBUS_ARON3);
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ARSON3);

        return Collections.unmodifiableSet(result);
    }
    


    /**
     * Set of CBUS event request opcodes
     */
    public static final Set<Integer> evRequestOpcodes = createRequests();

    /**
     * Test if CBUS opcode represents an event request
     * excludes node data requests RQDAT + RQDDS
     * @param opc CBUS op code
     * @return True if opcode represents a short event
     */
    public static boolean isEventRequest(int opc) {
        return evRequestOpcodes.contains(opc);
    }

    /*
     * Populate hashset with list of event requests
     */
    private static Set<Integer> createRequests() {
        Set<Integer> result = new HashSet<>();
        // Opcodes with 4 data
        result.add(CbusConstants.CBUS_AREQ);
        result.add(CbusConstants.CBUS_ASRQ);

        return Collections.unmodifiableSet(result);
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
    
    /**
     * Return a string representation of a decoded Module Manufacturer
     * @param man manufacturer int
     * @return decoded CBUS message
     */
    public static String getManu(int man) {
        // look for the opcode
        String format = manMap.get(man);
        if (format == null) {
            return "Unknown";
        } else {
            return format; 
        }
    }
    
    /**
     * Hashmap for decoding Module Manufacturers
     */
    public static final Map<Integer, String> manMap = createManMap();

    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createManMap() {
        Map<Integer, String> result = new HashMap<>();
        result.put(70, "ROCRAIL"); // NOI18N
        result.put(80, "SPECTRUM"); // NOI18N
        result.put(165, "MERG"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Return a string representation of a decoded Bus Type
     * @param type Bus type
     * @return decoded CBUS message
     */
    public static String getBusType(int type) {
        // look for the opcode
        String format = busMap.get(type);
        if (format == null) {
            return "Unknown";
        } else {
            return format; 
        }
    }
    
    /**
     * Hashmap for decoding Bus Type
     */
    public static final Map<Integer, String> busMap = createBusMap();

    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createBusMap() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "CAN"); // NOI18N
        result.put(2, "ETH"); // NOI18N
        result.put(3, "MIWI"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    // manufacturer specific stuff from here down
    // do not rely on these as defs, may be moved to module config file.
    
    // getModuleTypeExtra
    // getModuleSupportLink

    /**
     * Return a string representation of a decoded Module Name for
     * manufacturer 165 MERG.
     * @param man int manufacturer
     * @param type module type int
     * @return decoded String module type name
     */
    public static String getModuleType(int man, int type) {
        String format=null;
        if (man == 165) {
            format = type165Map.get(type);
        }
        else if (man == 70) {
            format = type70Map.get(type);
        }
        else if (man == 80) {
            format = type80Map.get(type);
        }
        if (format == null) {
            return "Type " + type;
        } else {
            return format; 
        }
    }
    
    /**
     * Hashmap for decoding Module Names
     */
    public static final Map<Integer, String> type165Map = createType165Map();
    public static final Map<Integer, String> type70Map = createType70Map();
    public static final Map<Integer, String> type80Map = createType80Map();
    
    /*
     * Populate hashmap with format strings for manufacturer 165 MERG
     */
    private static Map<Integer, String> createType165Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(0, "SLIM"); // NOI18N
        result.put(1, "CANACC4"); // NOI18N
        result.put(2, "CANACC5"); // NOI18N
        result.put(3, "CANACC8"); // NOI18N
        result.put(4, "CANACE3"); // NOI18N
        result.put(5, "CANACE8C"); // NOI18N
        result.put(6, "CANLED"); // NOI18N
        result.put(7, "CANLED64"); // NOI18N
        result.put(8, "CANACC4_2"); // NOI18N
        result.put(9, "CANCAB"); // NOI18N
        result.put(10, "CANCMD"); // NOI18N
        result.put(11, "CANSERVO"); // NOI18N
        result.put(12, "CANBC"); // NOI18N
        result.put(13, "CANRPI"); // NOI18N
        result.put(14, "CANTTCA"); // NOI18N
        result.put(15, "CANTTCB"); // NOI18N
        result.put(16, "CANHS"); // NOI18N
        result.put(17, "CANTOTI"); // NOI18N
        result.put(18, "CAN8I8O"); // NOI18N
        result.put(19, "CANSERVO8C"); // NOI18N
        result.put(20, "CANRFID"); // NOI18N
        result.put(21, "CANTC4"); // NOI18N
        result.put(22, "CANACE16C"); // NOI18N
        result.put(23, "CANIO8"); // NOI18N
        result.put(24, "CANSNDX"); // NOI18N
        result.put(25, "CANEther"); // NOI18N
        result.put(26, "CANSIG64"); // NOI18N
        result.put(27, "CANSIG8"); // NOI18N
        result.put(28, "CANCOND8C"); // NOI18N
        result.put(29, "CANPAN"); // NOI18N
        result.put(30, "CANACE3C"); // NOI18N
        result.put(31, "CANPanel"); // NOI18N
        result.put(32, "CANMIO"); // NOI18N
        result.put(33, "CANACE8MIO"); // NOI18N
        result.put(34, "CANSOL"); // NOI18N
        result.put(35, "CANBIP"); // NOI18N
        result.put(36, "CANCDU"); // NOI18N
        result.put(37, "CANACC4CDU"); // NOI18N
        result.put(38, "CANWiBase"); // NOI18N
        result.put(39, "WiCAB"); // NOI18N
        result.put(40, "CANWiFi"); // NOI18N
        result.put(41, "CANFTT"); // NOI18N
        result.put(42, "CANHNDST"); // NOI18N
        result.put(43, "CANTCHNDST"); // NOI18N
        result.put(44, "CANRFID8"); // NOI18N
        result.put(45, "CANmchRFID"); // NOI18N
        result.put(46, "CANPiWi"); // NOI18N
        result.put(47, "CAN4DC"); // NOI18N
        result.put(48, "CANELEV"); // NOI18N
        result.put(49, "CANSCAN"); // NOI18N
        result.put(50, "CANMIO_SVO"); // NOI18N
        result.put(51, "CANMIO_INP"); // NOI18N
        result.put(52, "CANMIO_OUT"); // NOI18N
        result.put(53, "CANBIP_OUT"); // NOI18N
        result.put(54, "CANASTOP"); // NOI18N
        result.put(55, "CANCSB"); // NOI18N
        result.put(56, "CANMAGOT"); // NOI18N
        result.put(57, "CANACE16CMIO"); // NOI18N
        result.put(58, "CANPiNODE"); // NOI18N
        result.put(59, "CANDISP"); // NOI18N
        result.put(60, "CANCOMPUTE"); // NOI18N
        
        result.put(253, "CANUSB"); // NOI18N
        result.put(254, "EMPTY"); // NOI18N
        result.put(255, "CAN_SW"); // NOI18N
        return Collections.unmodifiableMap(result);
    }

    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createType70Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "CANGC1"); // NOI18N
        result.put(2, "CANGC2"); // NOI18N
        result.put(3, "CANGC3"); // NOI18N
        result.put(4, "CANGC4"); // NOI18N
        result.put(5, "CANGC5"); // NOI18N
        result.put(6, "CANGC6"); // NOI18N
        result.put(7, "CANGC7"); // NOI18N
        result.put(11, "CANGC1e"); // NOI18N
        return Collections.unmodifiableMap(result);
    }

    
    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createType80Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "AMCTRLR"); // NOI18N
        result.put(2, "DUALCAB"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    
        /**
     * Return a string representation of extra module info
     * @param man int manufacturer code
     * @param type int module type
     * @return string value of extra module info
     */
    public static String getModuleTypeExtra(int man, int type) {
        String format=null;
        if (man == 165) {
            format = extra165Map.get(type);
        }
        else if (man == 70) {
            format = extra70Map.get(type);
        }
        else if (man == 80) {
            format = extra80Map.get(type);
        }
        if (format == null) {
            return "";
        } else {
            return format; 
        }
    }
    
    /**
     * Hashmap for decoding Module extra info
     */
    public static final Map<Integer, String> extra165Map = createExtra165Map();
    public static final Map<Integer, String> extra70Map = createExtra70Map();
    public static final Map<Integer, String> extra80Map = createExtra80Map();
    
    /*
     * Populate hashmap with format strings
     */
    private static Map<Integer, String> createExtra165Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(0, "Default for SLiM nodes");
        result.put(1, "Solenoid point driver");
        result.put(2, "Motorised point driver");
        result.put(3, "8 digital outputs ( + 8 inputs if modded)");
        result.put(4, "Control panel switch/button encoder");
        result.put(5, "8 digital inputs");
        result.put(6, "64 led driver");
        result.put(7, "64 led driver (multi leds per event)");
        result.put(8, "12v version of CANACC4 Solenoid point driver");
        result.put(9, "CANCAB hand throttle");
        result.put(10, "CANCMD command station");
        result.put(11, "8 servo driver (on canacc8 or similar hardware)");
        result.put(12, "BC1a command station");
        result.put(13, "RPI and RFID interface");
        result.put(14, "Turntable controller (turntable end)");
        result.put(15, "Turntable controller (control panel end)");
        result.put(16, "Handset controller for old BC1a type handsets");
        result.put(17, "Track occupancy detector");
        result.put(18, "8 inputs 8 outputs");
        result.put(19, "Canservo with servo position feedback");
        result.put(20, "RFID input");
        result.put(21, "CANTC4");
        result.put(22, "16 inputs");
        result.put(23, "8 way I/O");
        result.put(24, "CANSNDX");
        result.put(25, "Ethernet interface");
        result.put(26, "Multiple aspect signalling for CANLED module");
        result.put(27, "Multiple aspect signalling for CANACC8 module");
        result.put(28, "Conditional event generation");
        result.put(29, "Control panel 32 Outputs + 32 Inputs");
        result.put(30, "Newer version of CANACE3 firmware");
        result.put(31, "Control panel 64 Inputs / 64 Outputs");
        result.put(32, "Multiple I/O");
        result.put(33, "Multiple IO module emulating ACE8C");
        result.put(34, "Solenoid driver module");
        result.put(35, "Bipolar IO module with additional 8 I/O pins");
        result.put(36, "Solenoid driver module with additional 6 I/O pins");
        result.put(37, "CANACC4 firmware ported to CANCDU");
        result.put(38, "CAN to MiWi base station");
        result.put(39, "Wireless cab using MiWi protocol");
        result.put(40, "CAN to WiFi connection with Withrottle to CBUS protocol conversion");
        result.put(41, "Turntable controller configured using FLiM");
        result.put(42, "Handset (alternative to CANCAB)");
        result.put(43, "Touchscreen handset");
        result.put(44, "multi-channel RFID reader");
        result.put(45, "either a 2ch or 8ch RFID reader");
        result.put(46, "Raspberry Pi based module for WiFi");
        result.put(47, "DC train controller");
        result.put(48, "Nelevator controller");
        result.put(49, "128 switch inputs");
        result.put(50, "16MHz 25k80 version of CANSERVO8c");
        result.put(51, "16MHz 25k80 version of CANACE8MIO");
        result.put(52, "16MHz 25k80 version of CANACC8");
        result.put(53, "16MHz 25k80 version of CANACC5");
        result.put(54, "DCC stop generator");
        result.put(55, "CANCMD with on board 3A booster");
        result.put(56, "Magnet on Track detector");
        result.put(57, "16 input equivaent to CANACE8C");
        result.put(58, "CBUS module based on Raspberry Pi");
        result.put(59, "25K80 version of CANLED64");
        result.put(60, "Event processing engine");
        
        result.put(253, "USB interface");
        result.put(254, "Empty module, bootloader only");
        result.put(255, "Software nodes");
        return Collections.unmodifiableMap(result);
    }
    
    /*
     * Populate hashmap with format strings
     * extra text for Rocrail Modules
     */
    private static Map<Integer, String> createExtra70Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "RS232 PC interface.");
        result.put(2, "16 I/O.");
        result.put(3, "Command station (derived from cancmd).");
        result.put(4, "8 channel RFID reader.");        
        result.put(5, "Cab for fixed panels (derived from cancab).");        
        result.put(6, "4 channel servo controller.");        
        result.put(7, "Fast clock module.");        
        result.put(11, "CAN Ethernet interface.");
        return Collections.unmodifiableMap(result);
    }    
    
    /*
     * Populate hashmap with format strings
     * extra text for Animated Modeller module types
     */
    private static Map<Integer, String> createExtra80Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "Animation controller (firmware derived from cancmd).");
        result.put(2, "Dual cab based on cancab.");
        return Collections.unmodifiableMap(result);
    }   

    
    /**
     * Return a string representation of Module Support Link
     * @param man int manufacturer ID
     * @param type int module type ID
     * @return string module support link
     */
    public static String getModuleSupportLink(int man, int type) {
        String format=null;
        if (man == 165) {
            format = link165Map.get(type);
        }
        else if (man == 70) {
            format = link70Map.get(type);
        }
        if (format == null) {
            return "";
        } else {
            return format; 
        }
    }
    
    public static final Map<Integer, String> link165Map = createLink165Map();
    public static final Map<Integer, String> link70Map = createLink70Map();
    
    /*
     * Populate hashmap with merg module support links
     */
    private static Map<Integer, String> createLink165Map() {
        Map<Integer, String> result = new HashMap<>();
        
        result.put(1, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canacc4"); // NOI18N
        result.put(2, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canacc5"); // NOI18N
        result.put(3, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canacc8"); // NOI18N
        result.put(4, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canace3"); // NOI18N
        result.put(5, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canace8c"); // NOI18N
        // result.put(6, "CANLED"); // NOI18N
        result.put(7, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canled64"); // NOI18N
        result.put(8, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canacc4"); // NOI18N
        result.put(9, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancab"); // NOI18N
        result.put(10, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancmd"); // NOI18N
        // result.put(11, "CANSERVO"); // NOI18N
        // result.put(12, "CANBC"); // NOI18N
        // result.put(13, "CANRPI"); // NOI18N
        result.put(14, "https://www.merg.org.uk/merg_wiki/doku.php?id=other_download:turntable"); // NOI18N
        result.put(15, "https://www.merg.org.uk/merg_wiki/doku.php?id=other_download:turntable"); // NOI18N
        // result.put(16, "CANHS"); // NOI18N
        result.put(17, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canace8c"); // NOI18N
        // result.put(18, "CAN8I8O"); // NOI18N
        result.put(19, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canservo8"); // NOI18N
        result.put(20, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canrfid"); // NOI18N
        // result.put(21, "CANTC4"); // NOI18N
        // result.put(22, "CANACE16C"); // NOI18N
        // result.put(23, "CANIO8"); // NOI18N
        // result.put(24, "CANSNDX"); // NOI18N
        result.put(25, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canether"); // NOI18N
        result.put(26, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cansig"); // NOI18N
        result.put(27, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cansig"); // NOI18N
        result.put(28, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canccond8c"); // NOI18N
        result.put(29, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canpan"); // NOI18N
        result.put(30, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canace3"); // NOI18N
        result.put(31, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canpanel"); // NOI18N
        result.put(32, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmio"); // NOI18N
        // result.put(33, "CANACE8MIO"); // NOI18N
        result.put(34, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cansol"); // NOI18N
        result.put(35, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canbip"); // NOI18N
        result.put(36, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancdu"); // NOI18N
        // result.put(37, "CANACC4CDU"); // NOI18N
        // result.put(38, "CANWiBase"); // NOI18N
        // result.put(39, "WiCAB"); // NOI18N
        // result.put(40, "CANWiFi"); // NOI18N
        // result.put(41, "CANFTT"); // NOI18N
        // result.put(42, "CANHNDST"); // NOI18N
        // result.put(43, "CANTCHNDST"); // NOI18N
        result.put(44, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canrfid8"); // NOI18N
        result.put(45, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmchrfid"); // NOI18N
        result.put(46, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canwi"); // NOI18N
        result.put(47, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:can4dc"); // NOI18N
        // result.put(48, "CANELEV"); // NOI18N
        result.put(49, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canscan"); // NOI18N
        result.put(50, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmio"); // NOI18N
        result.put(51, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmio"); // NOI18N
        result.put(52, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmio"); // NOI18N
        // result.put(53, "CANBIP_OUT"); // NOI18N
        result.put(54, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canastop"); // NOI18N
        result.put(55, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancsb"); // NOI18N
        // result.put(56, "CANMAGOT"); // NOI18N
        // result.put(57, "CANACE16CMIO"); // NOI18N
        // result.put(58, "CANPiNODE"); // NOI18N
        result.put(59, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:candisp"); // NOI18N
        result.put(60, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancompute"); // NOI18N
        
        // result.put(253, "CANUSB"); // NOI18N
        // result.put(254, "EMPTY"); // NOI18N
        // result.put(255, "CAN_SW"); // NOI18N        
        
        return Collections.unmodifiableMap(result);
    }
    
    /*
     * Populate hashmap with rocrail module support links
     */
    private static Map<Integer, String> createLink70Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "https://wiki.rocrail.net/doku.php?id=can-gca1-en"); // NOI18N
        result.put(2, "https://wiki.rocrail.net/doku.php?id=can-gca2-en"); // NOI18N
        result.put(3, "https://wiki.rocrail.net/doku.php?id=can-gc3-en"); // NOI18N
        result.put(4, "https://wiki.rocrail.net/doku.php?id=can-gc4-en"); // NOI18N
        result.put(5, "https://wiki.rocrail.net/doku.php?id=can-gca5-en"); // NOI18N
        result.put(6, "https://wiki.rocrail.net/doku.php?id=can-gc6-en"); // NOI18N
        result.put(7, "https://wiki.rocrail.net/doku.php?id=can-gc7-en"); // NOI18N
        result.put(11, "https://wiki.rocrail.net/doku.php?id=can-gca1e-en"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    
    /**
     * Return a string representation of avreserved node number
     * @param modnum node number
     * @return reserved node number reason
     */
    public static String getReservedModule(int modnum) {
        // look for the opcode
        String format = resMod.get(modnum);
        if (format == null) {
            return "";
        } else {
            return format; 
        }
    }
    
    /**
     * Hashmap for fixed Module Numbers
     */
    public static final Map<Integer, String> resMod = createModMap();

    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createModMap() {
        Map<Integer, String> result = new HashMap<>();
        // Opcodes with no data
        result.put(100, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(101, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(102, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(103, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(104, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(105, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(106, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(107, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(108, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(109, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(110, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(111, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(112, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(113, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(114, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(115, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(116, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(117, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(118, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(119, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(120, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(121, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(122, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(123, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(124, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(125, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(126, "Reserved for CAN_RS Modules");
        result.put(127, "Reserved for CAN_USB Modules");
        result.put(65534, "Reserved for Command Station");
        result.put(65535, "Reserved, used by all CABS");
        return Collections.unmodifiableMap(result);
    }
    
}
