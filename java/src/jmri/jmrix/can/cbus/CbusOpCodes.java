package jmri.jmrix.can.cbus;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanFrame;
import jmri.util.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to decode CBUS opcodes
 *
 * https://github.com/MERG-DEV/CBUSlib
 * @author Andrew Crosland Copyright (C) 2009, 2021
 * @author Steve Young (C) 2018
 */
public class CbusOpCodes {

    private final static Logger log = LoggerFactory.getLogger(CbusOpCodes.class);

    /**
     * Return a string representation of a decoded CBUS Message
     *
     * Used in CBUS Console Log
     * @param msg CbusMessage to be decoded Return String decoded message
     * @return decoded CBUS message
     */
    @Nonnull
    public static final String fullDecode(AbstractMessage msg) {
        StringBuilder buf = new StringBuilder();
        // split the format string at each comma
        String[] fields = MAP.getOrDefault(msg.getElement(0),getDefaultOpc()).getDecode().split(",");

        int idx = 1;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].startsWith("%")) { // replace with bytes from the message
                int value = 0;
                int bytes = Integer.parseInt(fields[i].substring(1, 2));
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

        // special cases
        switch (msg.getElement(0)) {
            case CbusConstants.CBUS_ERR: // extra info for ERR opc
                buf.append(getCbusErr(msg));
                break;
            case CbusConstants.CBUS_CMDERR: // extra info for CMDERR opc
                if ((msg.getElement(3) > 0 ) && (msg.getElement(3) < 13 )) {
                    buf.append(Bundle.getMessage("CMDERR"+msg.getElement(3)));
                }
                break;
            case CbusConstants.CBUS_GLOC: // extra info GLOC OPC
                appendGloc(msg,buf);
                break;
            case CbusConstants.CBUS_FCLK:
                return CbusClockControl.dateFromCanFrame(msg);
            default:
                break;
        }
        return buf.toString();
    }

    private static void appendGloc(AbstractMessage msg, StringBuilder buf) {
        buf.append(" ");
        if (( ( ( msg.getElement(3) ) & 1 ) == 1 ) // bit 0 is 1
            && ( ( ( msg.getElement(3) >> 1 ) & 1 ) == 1 )) { // bit 1 is 1
            buf.append(Bundle.getMessage("invalidFlags"));
        }
        else if ( ( ( msg.getElement(3) ) & 1 ) == 1 ){ // bit 0 is 1
            buf.append(Bundle.getMessage("stealRequest"));
        }
        else if ( ( ( msg.getElement(3) >> 1 ) & 1 ) == 1 ){ // bit 1 is 1
            buf.append(Bundle.getMessage("shareRequest"));
        }
        else { // bit 0 and bit 1 are 0
            buf.append(Bundle.getMessage("standardRequest"));
        }
    }

    /**
     * Return CBUS ERR OPC String.
     * @param msg CanMessage or CanReply containing the CBUSERR OPC
     * @return Error String
     */
    @Nonnull
    public static final String getCbusErr(AbstractMessage msg){
        StringBuilder buf = new StringBuilder();
        // elements 1 & 2 depend on element 3
        switch (msg.getElement(3)) {
            case 1:
                buf.append(Bundle.getMessage("ERR_LOCO_STACK_FULL"))
                    .append(locoFromBytes(msg.getElement(1),msg.getElement(2)));
                break;
            case 2:
                buf.append(Bundle.getMessage("ERR_LOCO_ADDRESS_TAKEN",
                locoFromBytes(msg.getElement(1),msg.getElement(2))));
                break;
            case 3:
                buf.append(Bundle.getMessage("ERR_SESSION_NOT_PRESENT",msg.getElement(1)));
                break;
            case 4:
                buf.append(Bundle.getMessage("ERR_CONSIST_EMPTY"))
                .append(msg.getElement(1));
                break;
            case 5:
                buf.append(Bundle.getMessage("ERR_LOCO_NOT_FOUND"))
                .append(msg.getElement(1));
                break;
            case 6:
                buf.append(Bundle.getMessage("ERR_CAN_BUS_ERROR"));
                break;
            case 7:
                buf.append(Bundle.getMessage("ERR_INVALID_REQUEST"))
                .append(locoFromBytes(msg.getElement(1),msg.getElement(2)));
                break;
            case 8:
                buf.append(Bundle.getMessage("ERR_SESSION_CANCELLED",msg.getElement(1)));
                break;
            default:
                break;
            }
        return buf.toString();
    }

    /**
     * Return Loco Address String
     *
     * @param byteA 1st loco byte
     * @param byteB 2nd loco byte
     * @return Loco Address String
     */
    @Nonnull
    public static final String locoFromBytes(int byteA, int byteB ) {
        return new jmri.DccLocoAddress(((byteA & 0x3f) * 256 + byteB ),
            ((byteA & 0xc0) != 0)).toString();
    }

    /**
     * Return a string representation of a decoded CBUS Message
     *
     * @param msg CbusMessage to be decoded
     * @return decoded message after extended frame check
     */
    @Nonnull
    public static final String decode(AbstractMessage msg) {
        if (msg instanceof CanFrame) {
            if (!((CanFrame) msg).isExtended()) {
                return fullDecode(msg);
            }
            else {
                return decodeExtended((CanFrame)msg);
            }
        }
        return "";
    }

    /**
     * Return a string representation of a decoded Extended CBUS Message
     *
     * @param msg Extended CBUS CAN Frame to be decoded
     * @return decoded message after extended frame check
     */
    @Nonnull
    public static final String decodeExtended(CanFrame msg) {
        StringBuilder sb = new StringBuilder(Bundle.getMessage("decodeBootloader"));
        switch (msg.getHeader()) {
            case 4: // outgoing Bootload Command
                int newChecksum;
                switch (msg.getElement(5)) { // data payload of bootloader control frames
                    case CbusConstants.CBUS_BOOT_NOP: // 0
                        sb.append(Bundle.getMessage("decodeCBUS_BOOT_NOP"));
                        break;
                    case CbusConstants.CBUS_BOOT_RESET: // 1
                        sb.append(Bundle.getMessage("decodeCBUS_BOOT_RESET"));
                        break;
                    case CbusConstants.CBUS_BOOT_INIT: // 2
                        newChecksum = ( msg.getElement(2)*65536+msg.getElement(1)*256+msg.getElement(0)  );
                        sb.append(Bundle.getMessage("decodeCBUS_BOOT_INIT",newChecksum));
                        break;
                    case CbusConstants.CBUS_BOOT_CHECK: // 3
                        newChecksum = ( msg.getElement(7)*256+msg.getElement(6)  );
                        sb.append(Bundle.getMessage("decodeCBUS_BOOT_CHECK",newChecksum));
                        break;
                    case CbusConstants.CBUS_BOOT_TEST: // 4
                        sb.append(Bundle.getMessage("decodeCBUS_BOOT_TEST"));
                        break;
                    case CbusConstants.CBUS_BOOT_DEVID: // 5
                        sb.append(Bundle.getMessage("decodeCBUS_BOOT_DEVID"));
                        break;
                    case CbusConstants.CBUS_BOOT_BOOTID: // 6
                        sb.append(Bundle.getMessage("decodeCBUS_BOOT_BOOTID"));
                        break;
                    case CbusConstants.CBUS_BOOT_ENABLES: // 7
                        sb.append(Bundle.getMessage("decodeCBUS_BOOT_ENABLES"));
                        break;
                    default:
                        break;
                }
                break;
            case 5: // outgoing pure data frame
                sb.append( Bundle.getMessage("OPC_DA")).append(" :");
                msg.appendHexElements(sb);
                break;
            case 0x10000004: // incoming Bootload Info
                switch (msg.getElement(0)) { // data payload of bootloader control frames
                    case CbusConstants.CBUS_EXT_BOOT_ERROR: // 0
                        sb.append(Bundle.getMessage("decodeCBUS_EXT_BOOT_ERROR"));
                        break;
                    case CbusConstants.CBUS_EXT_BOOT_OK: // 1
                        sb.append(Bundle.getMessage("decodeCBUS_EXT_BOOT_OK"));
                        break;
                    case CbusConstants.CBUS_EXT_BOOTC: // 2
                        sb.append(Bundle.getMessage("decodeCBUS_EXT_BOOTC"));
                        break;
                    case CbusConstants.CBUS_EXT_DEVID: // 3
                        sb.append(Bundle.getMessage("decodeCBUS_EXT_DEVID"));
                        break;
                    case CbusConstants.CBUS_EXT_BOOTID: // 4
                        sb.append(Bundle.getMessage("decodeCBUS_EXT_BOOTID"));
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        if (sb.toString().equals(Bundle.getMessage("decodeBootloader"))){
            return(Bundle.getMessage("decodeUnknownExtended"));
        }
        return sb.toString();
    }

    /**
     * Return a string representation of a decoded CBUS OPC
     *
     * @param msg CbusMessage to be decoded Return String decoded OPC
     * @return decoded CBUS OPC, eg. "RTON" or "ACON2", else Reserved string.
     */
    @Nonnull
    public static final String decodeopcNonExtended(AbstractMessage msg) {
        return MAP.getOrDefault(msg.getElement(0),getDefaultOpc()).getName();
    }

    /**
     * Return a string OPC of a CBUS Message
     *
     * @param msg CbusMessage
     * @return decoded CBUS OPC, eg. "RTON" or "ACON2", else Reserved string.
     * Empty String for Extended Frames as no OPC concept.
     */
    @Nonnull
    public static final String decodeopc(AbstractMessage msg) {
        if ((msg instanceof CanFrame) &&  !((CanFrame) msg).extendedOrRtr()) {
            return decodeopcNonExtended(msg);
        }
        else {
            return "";
        }
    }

    /**
     * Test if CBUS OpCode is known to JMRI.
     * Performs Extended / RTR Frame check.
     *
     * @param msg CanReply or CanMessage
     * @return True if opcode is known
     */
    public static final boolean isKnownOpc(AbstractMessage msg){
        return ( MAP.get(msg.getElement(0))!=null
                && ( msg instanceof CanFrame)
                && (!((CanFrame) msg).extendedOrRtr()));
    }

    /**
     * Test if CBUS OpCode represents a CBUS event.
     * <p>
     * Defined in the CBUS Developer Manual as accessory commands.
     * Excludes fast clock.
     * <p>
     * ACON, ACOF, AREQ, ARON, AROF, ASON, ASOF, ASRQ, ARSON, ARSOF,
     * ACON1, ACOF1, ARON1, AROF1, ASON1, ASOF1, ARSON1, ARSOF1,
     * ACON2, ACOF2, ARON2, AROF2, ASON2, ASOF2, ARSON2, ARSOF2
     *
     * @param opc CBUS op code
     * @return True if opcode represents an event
     */
    public static final boolean isEvent(int opc) {
        return MAP.getOrDefault(opc,getDefaultOpc()).getFilters().contains(CbusFilterType.CFEVENT);
    }

    /**
     * Test if CBUS opcode represents a JMRI event table event.
     * Event codes excluding request codes + fastclock.
     * <p>
     * ACON, ACOF, ARON, AROF, ASON, ASOF, ARSON, ARSOF,
     * ACON1, ACOF1, ARON1, AROF1, ASON1, ASOF1, ARSON1, ARSOF1,
     * ACON2, ACOF2, ARON2, AROF2, ASON2, ASOF2, ARSON2, ARSOF2,
     * ACON3, ACOF3, ARON3, AROF3, ASON3, ASOF3, ARSON3, ARSOF3,
     *
     * @param opc CBUS op code
     * @return True if opcode represents an event
     */
    public static final boolean isEventNotRequest(int opc) {
        return (MAP.getOrDefault(opc,getDefaultOpc()).getFilters().contains(CbusFilterType.CFEVENT)
            && !MAP.getOrDefault(opc,getDefaultOpc()).getFilters().contains(CbusFilterType.CFREQUEST));
    }

    /**
     * Test if CBUS opcode represents a DCC Command Station Message
     * <p>
     * TOF, TON, ESTOP, RTOF, RTON, RESTP, KLOC, QLOC, DKEEP,
     * RLOC, QCON, ALOC, STMOD, PCON, KCON, DSPD, DFLG, DFNON, DFNOF, SSTAT,
     * DFUN, GLOC, ERR, RDCC3, WCVO, WCVB, QCVS, PCVS, RDCC4, WCVS, VCVS,
     * RDCC5, WCVOA, RDCC6, PLOC, STAT, RSTAT
     *
     * @param opc CBUS op code
     * @return True if opcode represents a dcc command
     */
    public static final boolean isDcc(int opc) {
        return MAP.getOrDefault(opc,getDefaultOpc()).getFilters().contains(CbusFilterType.CFCS);
    }

    /**
     * Test if CBUS opcode represents an on event.
     * <p>
     * ACON, ARON, ASON, ARSON
     * ACON1, ARON1, ASON1, ARSON1
     * ACON2, ARON2, ASON2, ARSON2
     * ACON3, ARON3, ASON3, ARSON3
     *
     * @param opc CBUS op code
     * @return True if opcode represents an on event
     */
    public static final boolean isOnEvent(int opc) {
        return MAP.getOrDefault(opc,getDefaultOpc()).getFilters().contains(CbusFilterType.CFON);
    }

    /**
     * Test if CBUS opcode represents an event request.
     * Excludes node data requests RQDAT + RQDDS.
     * AREQ, ASRQ
     *
     * @param opc CBUS op code
     * @return True if opcode represents a short event
     */
    public static final boolean isEventRequest(int opc) {
        return MAP.getOrDefault(opc,getDefaultOpc()).getFilters().contains(CbusFilterType.CFREQUEST);
    }

    /**
     * Test if CBUS opcode represents a short event.
     * <p>
     * ASON, ASOF, ASRQ, ARSON, ARSOF
     * ASON1, ASOF1, ARSON1, ARSOF1
     * ASON2, ASOF2, ARSON2, ARSOF2
     * ASON3, ASOF3, ARSON3, ARSOF3
     *
     * @param opc CBUS op code
     * @return True if opcode represents a short event
     */
    public static final boolean isShortEvent(int opc) {
        return MAP.getOrDefault(opc,getDefaultOpc()).getFilters().contains(CbusFilterType.CFSHORT);
    }

    /**
     * Get the filters for a CBUS OpCode.
     *
     * @param opc CBUS op code
     * @return Filter EnumSet
     */
    @Nonnull
    public static final EnumSet<CbusFilterType> getOpcFilters(int opc){
        return MAP.getOrDefault(opc,getDefaultOpc()).getFilters();
    }

    /**
     * Get the Name of a CBUS OpCode.
     *
     * @param opc CBUS op code
     * @return Name if known, else empty String.
     */
    @Nonnull
    public static final String getOpcName(int opc){
        if ( MAP.get(opc)!=null){
            return MAP.get(opc).getName();
        }
        return "";
    }

    /**
     * Get the Minimum Priority for a CBUS OpCode.
     *
     * @param opc CBUS op code
     * @return Minimum Priority
     */
    public static final int getOpcMinPriority(int opc){
        return MAP.getOrDefault(opc,getDefaultOpc()).getMinPri();
    }

    private static final Map<Integer, CbusOpc> MAP = createMainMap();

    private static Map<Integer, CbusOpc> createMainMap()  {
        Map<Integer, CbusOpc> result = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(FileUtil.getFile("program:xml/cbus/CbusOpcData.xml"));
            document.getDocumentElement().normalize();

            //Get all opcs
            NodeList nList = document.getElementsByTagName("CbusOpc");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node node = nList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;

                    // split the format string at each comma
                    String[] fields = eElement.getAttribute("decode").split("~");
                    StringBuilder fieldbuf = new StringBuilder();

                    for (String field : fields) {
                        if (field.startsWith("OPC_")) {
                            field = Bundle.getMessage(field);
                        }
                        fieldbuf.append(field);
                    }

                    EnumSet<CbusFilterType> filterSet = EnumSet.noneOf(CbusFilterType.class);
                    String[] filters = eElement.getAttribute("filter").split(",");
                    for (String filter : filters) {
                        CbusFilterType tmp = CbusFilterType.valueOf(filter);
                        filterSet.add(tmp);
                    }

                    result.put(jmri.util.StringUtil.getByte(0,eElement.getAttribute("hex")),
                        new CbusOpc(
                            Integer.parseInt(eElement.getAttribute("minPri")),
                            eElement.getAttribute("name"),
                            fieldbuf.toString(),
                            filterSet
                        ));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            log.error("Error importing xml file", ex);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Get a CBUS OpCode with default unknown values.
     *
     * @return Default OPC
     */
    @Nonnull
    private static CbusOpc getDefaultOpc(){
        return new CbusOpc(
            3,Bundle.getMessage("OPC_RESERVED"),"",
            EnumSet.of(CbusFilterType.CFMISC,CbusFilterType.CFUNKNOWN));
    }

    private static class CbusOpc {
        private final int _minPri;
        private final String _name;
        private final String _decodeText;
        private final EnumSet<CbusFilterType> _filterMap;

        private CbusOpc(int minPri, String name, String decode, EnumSet<CbusFilterType> filterMap){
            _minPri = minPri;
            _name = name;
            _decodeText = decode;
            _filterMap = filterMap;
        }

        private int getMinPri(){
            return _minPri;
        }

        private String getName(){
            return _name;
        }

        private String getDecode(){
            return _decodeText;
        }

        private EnumSet<CbusFilterType> getFilters(){
            return EnumSet.copyOf(_filterMap);
        }
    }

}
