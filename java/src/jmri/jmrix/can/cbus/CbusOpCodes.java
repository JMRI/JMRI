package jmri.jmrix.can.cbus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrix.AbstractMessage;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

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
     * Used in CBUS Console Log
     * @param msg CbusMessage to be decoded Return String decoded message
     * @return decoded CBUS message
     */
    public static String decode(AbstractMessage msg) {
        StringBuilder buf = new StringBuilder();
        int bytes;
        int value;
        int opc=msg.getElement(0);

        // look for the opcode
        String format = opcodeMap.get(opc);
        if (format == null) {
            return Bundle.getMessage("OPC_RESERVED");
        }
        
        // split the format string at each comma
        String[] fields = format.split(",");

        int idx = 1;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].startsWith("%")) { // replace with bytes from the message
                value = 0;
                bytes = Integer.parseInt(fields[i].substring(1, 2));
                for (; bytes > 0; bytes--) {
                    value = value * 256 + msg.getElement(idx++);
                }
                fields[i] = String.valueOf(value);
            }
            else if (fields[i].startsWith("^2")) { // replace with loco id from 2 bytes
                fields[i] = locoFromBytes(msg.getElement(idx++), msg.getElement(idx++) );
            }
            else if (fields[i].startsWith("$4")) { // replace the 4 bytes with event / node name ( if possible )
                int nn = (256*msg.getElement(idx++))+(msg.getElement(idx++));
                int en = (256*msg.getElement(idx++))+(msg.getElement(idx++));
                fields[i] = new CbusNameService().getEventNodeString(nn,en);
            }
            else if (fields[i].startsWith("$2")) { // replace the 2 bytes with node name ( if possible )
                int nodenum = (256*msg.getElement(idx++))+(msg.getElement(idx++));
                fields[i] = "NN:" + nodenum + " " + new CbusNameService().getNodeName(nodenum);
            }
            
            // concatenat to the result
            buf.append(fields[i]);
        }
        
        // extra info for ERR opc
        if (opc==CbusConstants.CBUS_ERR) {
            // elements 1 & 2 depend on element 3
            switch (msg.getElement(3)) {
                case 1:
                    buf.append(Bundle.getMessage("ERR_LOCO_STACK_FULL"));
                    buf.append(locoFromBytes(msg.getElement(1),msg.getElement(2)));
                    break;
                case 2:
                    buf.append(Bundle.getMessage("ERR_LOCO_ADDRESS_TAKEN", 
                    locoFromBytes(msg.getElement(1),msg.getElement(2))));
                    break;
                case 3:
                    buf.append(Bundle.getMessage("ERR_SESSION_NOT_PRESENT",msg.getElement(1)));
                    break;
                case 4:
                    buf.append(Bundle.getMessage("ERR_CONSIST_EMPTY"));
                    buf.append(msg.getElement(1));
                    break;
                case 5:
                    buf.append(Bundle.getMessage("ERR_LOCO_NOT_FOUND"));
                    buf.append(msg.getElement(1));
                    break;
                case 6:
                    buf.append(Bundle.getMessage("ERR_CAN_BUS_ERROR"));
                    break;
                case 7:
                    buf.append(Bundle.getMessage("ERR_INVALID_REQUEST"));
                    buf.append(locoFromBytes(msg.getElement(1),msg.getElement(2)));
                    break;
                case 8:
                    buf.append(Bundle.getMessage("ERR_SESSION_CANCELLED",msg.getElement(1)));
                    break;
                default:
                    break;
            }
        }
        
        // extra info for CMDERR opc
        if (opc==CbusConstants.CBUS_CMDERR) {
            if ((msg.getElement(3) > 0 ) && (msg.getElement(3) < 13 )) {
                buf.append(Bundle.getMessage("CMDERR"+msg.getElement(3)));
            }
        }
        return buf.toString();
    }
    
    public static String locoFromBytes(int byteA, int byteB ) {
        String shortLong = "S";
        // boolean rcvdIsLong = (byteA & 0xc0) != 0;
        if ((byteA & 0xc0) != 0) {
            shortLong = "L";
        }
        // int rcvdIntAddr = ((byteA & 0x3f) * 256 + byteB );
        return ((byteA & 0x3f) * 256 + byteB ) + " " + shortLong;
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
    private static final Map<Integer, String> opcodeMap = createMap();

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
        Bundle.getMessage("OPC_AD") + ": ,^2"); // NOI18N
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
        Bundle.getMessage("OPC_AD") + ": ,^2, " + Bundle.getMessage("OPC_FL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ERR, Bundle.getMessage("CBUS_ERR") + " "); // NOI18N
        
        result.put(CbusConstants.CBUS_CMDERR, Bundle.getMessage("CBUS_CMDERR") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, "); // NOI18N
        
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
        
        result.put(CbusConstants.CBUS_ACON, Bundle.getMessage("CBUS_ACON") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF, Bundle.getMessage("CBUS_ACOF") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_AREQ, Bundle.getMessage("CBUS_AREQ") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON, Bundle.getMessage("CBUS_ARON") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF, Bundle.getMessage("CBUS_AROF") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_EVULN, Bundle.getMessage("CBUS_EVULN") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_NVSET, Bundle.getMessage("CBUS_NVSET") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_NV") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NVANS, Bundle.getMessage("CBUS_NVANS") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_NV") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON, Bundle.getMessage("CBUS_ASON") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF, Bundle.getMessage("CBUS_ASOF") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_ASRQ, Bundle.getMessage("CBUS_ASRQ") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_PARAN, Bundle.getMessage("CBUS_PARAN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_PA") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_REVAL, Bundle.getMessage("CBUS_REVAL") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_EN") + ":,%1, EV:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON, Bundle.getMessage("CBUS_ARSON") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF, Bundle.getMessage("CBUS_ARSOF") + " ,$4, "); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC3, Bundle.getMessage("CBUS_EXTC3") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 5 data
        result.put(CbusConstants.CBUS_RDCC4, Bundle.getMessage("CBUS_RDCC4") + " " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + " 1:,%1, 2:,%1, 3:,%1, 4:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVS, Bundle.getMessage("CBUS_WCVS") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_MD") + ":,%1, " + Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON1, Bundle.getMessage("CBUS_ACON1") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF1, Bundle.getMessage("CBUS_ACOF1") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_REQEV, Bundle.getMessage("CBUS_REQEV") + " ,$4, " +
        Bundle.getMessage("OPC_EV") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON1, Bundle.getMessage("CBUS_ARON1") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF1, Bundle.getMessage("CBUS_AROF1") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NEVAL, Bundle.getMessage("CBUS_NEVAL") + " ,$2, " +
        Bundle.getMessage("OPC_EV") + ":,%1, " +
        Bundle.getMessage("OPC_EV") + ":,%1, " + Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PNN, Bundle.getMessage("CBUS_PNN") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_MF") + ":,%1, " + 
        Bundle.getMessage("OPC_MI") + ":,%1, " + Bundle.getMessage("OPC_FL") + ":,%1");
        
        result.put(CbusConstants.CBUS_ASON1, Bundle.getMessage("CBUS_ASON1") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF1, Bundle.getMessage("CBUS_ASOF1") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON1, Bundle.getMessage("CBUS_ARSON1") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF1, Bundle.getMessage("CBUS_ARSOF1") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC4, Bundle.getMessage("CBUS_EXTC4") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 6 data
        result.put(CbusConstants.CBUS_RDCC5, Bundle.getMessage("CBUS_RDCC5") + " " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + 
        " 1:,%1, 2:,%1, 3:,%1, 4:,%1, 5:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_WCVOA, Bundle.getMessage("CBUS_WCVOA") + " " + 
        Bundle.getMessage("OPC_AD") + ": ,^2, " + Bundle.getMessage("OPC_CV") + ":,%2, " + 
        Bundle.getMessage("OPC_MD") + ":,%1, " + Bundle.getMessage("OPC_DA") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_FCLK, Bundle.getMessage("CBUS_FCLK") + " " + 
        Bundle.getMessage("OPC_FI") + ":,%1, " + Bundle.getMessage("OPC_FH") + ":,%1, " + 
        Bundle.getMessage("OPC_FW") + ":,%1, " + Bundle.getMessage("OPC_FD") + ":,%1, " + 
        Bundle.getMessage("OPC_FM") + ":,%1, " + Bundle.getMessage("OPC_FT") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON2, Bundle.getMessage("CBUS_ACON2") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF2, Bundle.getMessage("CBUS_ACOF2") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVLRN, Bundle.getMessage("CBUS_EVLRN") + " ,$4, " +
        Bundle.getMessage("OPC_EV") + ":,%1, " + Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVANS, Bundle.getMessage("CBUS_EVANS") + " ,$4, " +
        Bundle.getMessage("OPC_EV") + ":,%1, " + Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON2, Bundle.getMessage("CBUS_ARON2") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF2, Bundle.getMessage("CBUS_AROF2") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON2, Bundle.getMessage("CBUS_ASON2") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF2, Bundle.getMessage("CBUS_ASOF2") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON2, Bundle.getMessage("CBUS_ARSON2") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF2, Bundle.getMessage("CBUS_ARSOF2") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EXTC5, Bundle.getMessage("CBUS_EXTC5") + " :,%1, " + 
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N

        // Opcodes with 7 data
        result.put(CbusConstants.CBUS_RDCC6, Bundle.getMessage("CBUS_RDCC6") + " " + 
        Bundle.getMessage("OPC_RP") + ":,%1, " + Bundle.getMessage("Byte") + 
        " 1:,%1, 2:,%1, 3:,%1, 4:,%1, 5:,%1, 6:,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PLOC, Bundle.getMessage("CBUS_PLOC") + " " + 
        Bundle.getMessage("OPC_SN") + ":,%1, " + Bundle.getMessage("OPC_AD") + ": ,^2, " + 
        Bundle.getMessage("OPC_SE") + ":,%1, " + Bundle.getMessage("OPC_F1") + ":,%1, " + 
        Bundle.getMessage("OPC_F2") + ":,%1, " + Bundle.getMessage("OPC_F3") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_NAME, Bundle.getMessage("CBUS_NAME") + " " + 
        Bundle.getMessage("OPC_CH") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_STAT, Bundle.getMessage("CBUS_STAT") + " : " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_CS") + ":,%1, " + 
        Bundle.getMessage("OPC_FL") + ":,%1, " + Bundle.getMessage("OPC_VN") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_PARAMS, Bundle.getMessage("CBUS_PARAMS") + " " + 
        Bundle.getMessage("OPC_PA") + ":,%1, ,%1, ,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACON3, Bundle.getMessage("CBUS_ACON3") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACOF3, Bundle.getMessage("CBUS_ACOF3") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ENRSP, Bundle.getMessage("CBUS_ENRSP") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " + Bundle.getMessage("OPC_NN") + ":,%2, " + 
        Bundle.getMessage("OPC_EN") + ":,%2, " + Bundle.getMessage("OPC_EV") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARON3, Bundle.getMessage("CBUS_ARON3") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_AROF3, Bundle.getMessage("CBUS_AROF3") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_EVLRNI, Bundle.getMessage("CBUS_EVLRNI") + " ,$4, " +
        Bundle.getMessage("OPC_ET") + ":,%1, " + Bundle.getMessage("OPC_EV") + ":,%1, " + 
        Bundle.getMessage("OPC_VL") + ":,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ACDAT, Bundle.getMessage("CBUS_ACDAT") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARDAT, Bundle.getMessage("CBUS_ARDAT") + " " + 
        Bundle.getMessage("OPC_NN") + ":,%2, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASON3, Bundle.getMessage("CBUS_ASON3") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ASOF3, Bundle.getMessage("CBUS_ASOF3") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_DDES, Bundle.getMessage("CBUS_DDES") + " " + 
        Bundle.getMessage("OPC_DN") + ":,%2, " + Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_DDRS, Bundle.getMessage("CBUS_DDRS") + " " + 
        Bundle.getMessage("OPC_DN") + ":,%2, " + Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSON3, Bundle.getMessage("CBUS_ARSON3") + " ,$4, " +
        Bundle.getMessage("OPC_DA") + ":,%1, ,%1, ,%1"); // NOI18N
        
        result.put(CbusConstants.CBUS_ARSOF3, Bundle.getMessage("CBUS_ARSOF3") + " ,$4, " +
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
    private static final Map<Integer, String> opcMap = createoMap();

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
    private static final Set<Integer> eventOpcodes = createEventOPC();

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
     * Populate hashset with list of event opcodes
     * Defined in the CBUS Dev manual as accessory commands.
     * excludes fast clock
     */
    private static Set<Integer> createEventOPC() {
        Set<Integer> result = new HashSet<>();

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
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ASOF3);
        result.add(CbusConstants.CBUS_ARSON3);
        result.add(CbusConstants.CBUS_ARSOF3);
        
        return Collections.unmodifiableSet(result);
    }

    
    
    /**
     * Set of CBUS event opcodes excluding requests + fastclock
     */
    private static final Set<Integer> eventNotRequestOpCodes = createEventNROPC();

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
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ASOF3);
        result.add(CbusConstants.CBUS_ARSON3);
        result.add(CbusConstants.CBUS_ARSOF3);
        
        return Collections.unmodifiableSet(result);
    }
    
    
    
    /**
     * Set of CBUS DCC opcodes
     */
    private static final Set<Integer> dccOpcodes = createDccOPC();

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
    private static final Set<Integer> onEvOpcodes = createOnEv();

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
    private static final Set<Integer> evRequestOpcodes = createRequests();

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
    private static final Set<Integer> shortOpcodes = createShort();

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

    // private final static Logger log = LoggerFactory.getLogger(CbusOpCodes.class);
}
