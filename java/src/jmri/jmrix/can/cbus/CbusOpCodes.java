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
        result.put(CbusConstants.CBUS_ACK, Bundle.getMessage("CBUS_ACK") + " (ACK)"); // NOI18N
        result.put(CbusConstants.CBUS_NAK, Bundle.getMessage("CBUS_NAK") + " (NAK)"); // NOI18N
        result.put(CbusConstants.CBUS_HLT, Bundle.getMessage("CBUS_HLT") + " (HLT)"); // NOI18N
        result.put(CbusConstants.CBUS_BON, Bundle.getMessage("CBUS_BON") + " (BON)"); // NOI18N
        result.put(CbusConstants.CBUS_TOF, Bundle.getMessage("CBUS_TOF") + " (TOF)"); // NOI18N
        result.put(CbusConstants.CBUS_TON, Bundle.getMessage("CBUS_TON") + " (TON)"); // NOI18N
        result.put(CbusConstants.CBUS_ESTOP, Bundle.getMessage("CBUS_ESTOP") + " (ESTOP)"); // NOI18N
        result.put(CbusConstants.CBUS_ARST, Bundle.getMessage("CBUS_ARST") + " (ARST)"); // NOI18N
        result.put(CbusConstants.CBUS_RTOF, Bundle.getMessage("CBUS_RTOF") + " (RTOF)"); // NOI18N
        result.put(CbusConstants.CBUS_RTON, Bundle.getMessage("CBUS_RTON") + " (RTON)"); // NOI18N
        result.put(CbusConstants.CBUS_RESTP, Bundle.getMessage("CBUS_RESTP") + " (RESTP)"); // NOI18N
        result.put(CbusConstants.CBUS_RSTAT, Bundle.getMessage("CBUS_RSTAT") + " (RSTAT)"); // NOI18N
        result.put(CbusConstants.CBUS_QNN, Bundle.getMessage("CBUS_QNN") + " (QNN)"); // NOI18N
        result.put(CbusConstants.CBUS_RQNP, Bundle.getMessage("CBUS_RQNP") + " (RQNP)"); // NOI18N
        result.put(CbusConstants.CBUS_RQMN, Bundle.getMessage("CBUS_RQMN") + " (RQMN)"); // NOI18N

        // Opcodes with 1 data
        result.put(CbusConstants.CBUS_KLOC, Bundle.getMessage("CBUS_KLOC") + " (KLOC) " + 
        Bundle.getMessage("OPC_SN") + ": ,%1"); // NOI18N
        result.put(CbusConstants.CBUS_QLOC, Bundle.getMessage("CBUS_QLOC") + " (QLOC) " + 
        Bundle.getMessage("OPC_SN") + ": ,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DKEEP, Bundle.getMessage("CBUS_DKEEP") + " (DKEEP) " + 
        Bundle.getMessage("OPC_SN") + ": ,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DBG1, Bundle.getMessage("CBUS_DBG1") + " (DBG1) " + 
        Bundle.getMessage("OPC_DA") + ": ,%1"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC, Bundle.getMessage("CBUS_EXTC") + " (EXTC): ,%1");

        // Opcodes with 2 data
        result.put(CbusConstants.CBUS_RLOC, Bundle.getMessage("CBUS_RLOC") + " (RLOC) " + 
        Bundle.getMessage("OPC_AD") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_QCON, Bundle.getMessage("CBUS_QCON") + " (QCON) " + 
        Bundle.getMessage("OPC_AD") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_SNN, Bundle.getMessage("CBUS_SNN") + " (SNN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_ALOC, Bundle.getMessage("CBUS_ALOC") + " (ALOC) " + 
        Bundle.getMessage("OPC_AD") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_STMOD, Bundle.getMessage("CBUS_STMOD") + " (STMOD) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_MD") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_PCON, Bundle.getMessage("CBUS_PCON") + " (PCON) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CA") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_KCON, Bundle.getMessage("CBUS_KCON") + " (KCON) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CA") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DSPD, Bundle.getMessage("CBUS_DSPD") + " (DSPD) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_SD") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DFLG, Bundle.getMessage("CBUS_DFLG") + " (DFLG) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_FL") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DFNON, Bundle.getMessage("CBUS_DFNON") + " (DFNON) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_FN") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_DFNOF, Bundle.getMessage("CBUS_DFNOF") + " (DFNOF) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_FN") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_SSTAT, Bundle.getMessage("CBUS_SSTAT") + " (SSTAT) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_ST") + ":,%1"); // NOI18N
        result.put(CbusConstants.CBUS_RQNN, Bundle.getMessage("CBUS_RQNN") + " (RQNN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNREL, Bundle.getMessage("CBUS_NNREL") + " (NNREL) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNACK, Bundle.getMessage("CBUS_NNACK") + " (NNREF) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNLRN, Bundle.getMessage("CBUS_NNLRN") + " (NNLRN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNULN, Bundle.getMessage("CBUS_NNULN") + " (NNULN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNCLR, Bundle.getMessage("CBUS_NNCLR") + " (NNCLR) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NNEVN, Bundle.getMessage("CBUS_NNEVN") + " (NNEVN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_NERD, Bundle.getMessage("CBUS_NERD") + " (NERD) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_RQEVN, Bundle.getMessage("CBUS_RQEVN") + " (RQEVN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_WRACK, Bundle.getMessage("CBUS_WRACK") + " (WRACK) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_RQDAT, Bundle.getMessage("CBUS_RQDAT") + " (RQDAT) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_RQDDS, Bundle.getMessage("CBUS_RQDDS") + " (RQDDS) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_BOOTM, Bundle.getMessage("CBUS_BOOTM") + " (BOOTM) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_ENUM, Bundle.getMessage("CBUS_ENUM") + " (ENUM) " + 
        Bundle.getMessage("OPC_NN") + ":,%2"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC1, Bundle.getMessage("CBUS_EXTC1") + " (EXTC1):,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N

        // Opcodes with 3 data
        result.put(CbusConstants.CBUS_DFUN, Bundle.getMessage("CBUS_DFUN") + " (DFUN) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_RG") + ":,%1, " + 
        Bundle.getMessage("OPC_FN") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_GLOC, Bundle.getMessage("CBUS_GLOC") + " (GLOC) " + 
        Bundle.getMessage("OPC_AD") + ":,%2, " + Bundle.getMessage("OPC_FL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ERR, Bundle.getMessage("CBUS_ERR") + " (ERR) " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_CMDERR, Bundle.getMessage("CBUS_CMDERR") + " (CMDERR) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_ER") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVNLF, Bundle.getMessage("CBUS_EVNLF") + " (ENNLF) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_SP") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NVRD, Bundle.getMessage("CBUS_NVRD") + " (NVRD) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_NV") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NENRD, Bundle.getMessage("CBUS_NENRD") + " (NENRD) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_RQNPN, Bundle.getMessage("CBUS_RQNPN") + " (RQNPN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_PA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NUMEV, Bundle.getMessage("CBUS_NUMEV") + " (NUMEV) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("CBUSEVENTS") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_CANID, Bundle.getMessage("CBUS_CANID") + " (CANID) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("CanID") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC2, Bundle.getMessage("CBUS_EXTC2") + " (EXTC2):,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N

        // Opcodes with 4 data
        result.put(CbusConstants.CBUS_RDCC3, Bundle.getMessage("CBUS_RDCC3") + " (RDCC3) " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + " 1:,%1, 2:,%1, 3:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVO, Bundle.getMessage("CBUS_WCVO") + " (WCVO) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVB, Bundle.getMessage("CBUS_WCVB") + " (WCVB) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_QCVS, Bundle.getMessage("CBUS_QCVS") + " (QCVS) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_MD") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PCVS, Bundle.getMessage("CBUS_PCVS") + " (PCVS) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON, Bundle.getMessage("CBUS_ACON") + " (ACON) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF, Bundle.getMessage("CBUS_ACOF") + " (ACOF) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_AREQ, Bundle.getMessage("CBUS_AREQ") + " (AREQ) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON, Bundle.getMessage("CBUS_ARON") + " (ARON) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF, Bundle.getMessage("CBUS_AROF") + " (AROF) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVULN, Bundle.getMessage("CBUS_EVULN") + " (EVULN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2 " + Bundle.getMessage("OPC_EN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_NVSET, Bundle.getMessage("CBUS_NVSET") + " (NVSET) " + 
        Bundle.getMessage("OPC_NN") + ":,%2 " + Bundle.getMessage("OPC_NV") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NVANS, Bundle.getMessage("CBUS_NVANS") + " (NVANS) " + 
        Bundle.getMessage("OPC_NN") + ":,%2 " + Bundle.getMessage("OPC_NV") + ":,%1 " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON, Bundle.getMessage("CBUS_ASON") + " (ASON) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF, Bundle.getMessage("CBUS_ASOF") + " (ASOF) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASRQ, Bundle.getMessage("CBUS_ASRQ") + " (ASRQ) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_PARAN, Bundle.getMessage("CBUS_PARAN") + " (PARAN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_PA") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_REVAL, Bundle.getMessage("CBUS_REVAL") + " (REVAL) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%1, EV:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON, Bundle.getMessage("CBUS_ARSON") + " (ARSON) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF, Bundle.getMessage("CBUS_ARSOF") + " (ARSOF) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC3, Bundle.getMessage("CBUS_EXTC3") + " (EXTC3):,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 5 data
        result.put(CbusConstants.CBUS_RDCC4, Bundle.getMessage("CBUS_RDCC4") + " (RDCC4) " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + " 1:,%1, 2:,%1, 3:,%1, 4:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVS, Bundle.getMessage("CBUS_WCVS") + " (WCVS) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_MD") + ":,%1, " + Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON1, Bundle.getMessage("CBUS_ACON1") + " (ACON1) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF1, Bundle.getMessage("CBUS_ACOF1") + " (ACOF1) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_REQEV, Bundle.getMessage("CBUS_REQEV") + " (REQEV) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_EV") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON1, Bundle.getMessage("CBUS_ARON1") + " (ARON1) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF1, Bundle.getMessage("CBUS_AROF1") + " (AROF1) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NEVAL, Bundle.getMessage("CBUS_NEVAL") + " (NEVAL) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PNN, Bundle.getMessage("CBUS_PNN") + " (NEVAL) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_MF") + ":,%1, " + 
        Bundle.getMessage("OPC_MI") + ":,%1, " + Bundle.getMessage("OPC_FL") + ":,%1");
        
        result.put(CbusConstants.CBUS_ASON1, Bundle.getMessage("CBUS_ASON1") + " (ASON1) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF1, Bundle.getMessage("CBUS_ASOF1") + " (ASOF1) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON1, Bundle.getMessage("CBUS_ARSON1") + " (ARSON1) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF1, Bundle.getMessage("CBUS_ARSOF1") + " (ARSOF1) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC4, Bundle.getMessage("CBUS_EXTC4") + " (EXTC4):,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 6 data
        result.put(CbusConstants.CBUS_RDCC5, Bundle.getMessage("CBUS_RDCC5") + " (RDCC5) " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + 
        " 1:,%1, 2:,%1, 3:,%1, 4:,%1, 5:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVOA, Bundle.getMessage("CBUS_WCVOA") + " (WCVOA) " + 
        Bundle.getMessage("OPC_AD") + ":,%2, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_MD") + ":,%1, " + Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_FCLK, Bundle.getMessage("CBUS_FCLK") + " (FCLK) " + 
        Bundle.getMessage("OPC_FI") + ":,%1, " + Bundle.getMessage("OPC_FH") + ":,%1, " + 
        Bundle.getMessage("OPC_FW") + ":,%1, " + Bundle.getMessage("OPC_FD") + ":,%1, " + 
        Bundle.getMessage("OPC_FM") + ":,%1, " + Bundle.getMessage("OPC_FT") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON2, Bundle.getMessage("CBUS_ACON2") + " (ACON2) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF2, Bundle.getMessage("CBUS_ACOF2") + " (ACOF2) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVLRN, Bundle.getMessage("CBUS_EVLRN") + " (EVLRN) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EV") + ":,%2, " + 
        Bundle.getMessage("OPC_E1") + ":,%1, " + Bundle.getMessage("OPC_E2") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVANS, Bundle.getMessage("CBUS_EVANS") + " (EVANS) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EV") + ":,%2, " + 
        Bundle.getMessage("OPC_E1") + ":,%1, " + Bundle.getMessage("OPC_E2") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON2, Bundle.getMessage("CBUS_ARON2") + " (ARON2) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF2, Bundle.getMessage("CBUS_AROF2") + " (AROF2) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON2, Bundle.getMessage("CBUS_ASON2") + " (ASON2) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF2, Bundle.getMessage("CBUS_ASOF2") + " (ASOF2) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON2, Bundle.getMessage("CBUS_ARSON2") + " (ARSON2) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF2, Bundle.getMessage("CBUS_ARSOF2") + " (ARSOF2) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC5, Bundle.getMessage("CBUS_EXTC5") + " (EXTC5):,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 7 data
        result.put(CbusConstants.CBUS_RDCC6, Bundle.getMessage("CBUS_RDCC6") + " (RDCC6) " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + 
        " 1:,%1, 2:,%1, 3:,%1, 4:,%1, 5:,%1, 6:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PLOC, Bundle.getMessage("CBUS_PLOC") + " (PLOC) " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_AD") + ":,%2, " + 
        Bundle.getMessage("OPC_SE") + ":,%1, " + Bundle.getMessage("OPC_F1") + ":,%1, " + 
        Bundle.getMessage("OPC_F2") + ":,%1, " + Bundle.getMessage("OPC_F3") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NAME, Bundle.getMessage("CBUS_NAME") + " (NAME) " + 
        Bundle.getMessage("OPC_CH") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_STAT, Bundle.getMessage("CBUS_STAT") + " (STAT): " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_CS") + ":,%1, " + 
        Bundle.getMessage("OPC_FL") + ":,%1, " + Bundle.getMessage("OPC_VN") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PARAMS, Bundle.getMessage("CBUS_PARAMS") + " (PARAMS) " + 
        Bundle.getMessage("OPC_PA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON3, Bundle.getMessage("CBUS_ACON3") + " (ACON3) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF3, Bundle.getMessage("CBUS_ACOF3") + " (ACOF3) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ENRSP, Bundle.getMessage("CBUS_ENRSP") + " (ENRSP) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%4, " + 
        Bundle.getMessage("OPC_EN") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON3, Bundle.getMessage("CBUS_ARON3") + " (ARON3) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF3, Bundle.getMessage("CBUS_AROF3") + " (AROF3) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVLRNI, Bundle.getMessage("CBUS_EVLRNI") + " (EVLRNI) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%2, " + 
        Bundle.getMessage("OPC_ET") + ":,%1, " + Bundle.getMessage("OPC_EV") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACDAT, Bundle.getMessage("CBUS_ACDAT") + " (ACDAT) " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARDAT, Bundle.getMessage("CBUS_ARDAT") + " (ARDAT) " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON3, Bundle.getMessage("CBUS_ASON3") + " (ASON3) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF3, Bundle.getMessage("CBUS_ASOF3") + " (ASOF3) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_DDES, Bundle.getMessage("CBUS_DDES") + " (DDES) " + 
        Bundle.getMessage("OPC_DN") + ":,%2, " + Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_DDRS, Bundle.getMessage("CBUS_DDRS") + " (DDRS) " + 
        Bundle.getMessage("OPC_DN") + ":,%2, " + Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON3, Bundle.getMessage("CBUS_ARSON3") + " (ARSON3) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF3, Bundle.getMessage("CBUS_ARSOF3") + " (ARSOF3) " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_DN") + ":,%2, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC6, Bundle.getMessage("CBUS_EXTC6") + " (EXTC6):,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N

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
