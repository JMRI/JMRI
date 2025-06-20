package jmri.jmrix.bidib;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.utils.NodeUtils;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.bidib.jbidibc.messages.LcConfig;
import org.bidib.jbidibc.messages.LcConfigX;
import org.bidib.jbidibc.messages.BidibPort;
import org.bidib.jbidibc.messages.enums.LcOutputType;
import org.bidib.jbidibc.messages.enums.PortModelEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for handling BiDiB addresses.
 *
 * @author Eckart Meyer Copyright (C) 2019-2023
 * 
 */
public class BiDiBAddress {
    
    private String aString = null;
    private long nodeuid = 0;
    private int addr = -1; //port address or DCC address
    private String addrType = ""; //t: DCC address ("on the track"), p: local port, default is DCC address if a command station node is present
    private LcOutputType portType; //used in type address mode only, not in flat address mode
    private Node node = null;

    static final String addrRegex = "^(?:[xX]([0-9a-fA-F]+):|([a-zA-Z0-9_\\-\\.]+):|)([afptAFPT]{0,1})(\\d+)([SLVUMABPI]{0,1})$";
    
    // Groups:
    // 0 - all
    // 1 - node (hex with X prefix) - null of not present
    // 2 - node (name starting with a letter, but not X) - null if not present
    // 3 - address type letter (a, f, p or t), empty string if not present
    // 4 - address (decimal), required
    // 5 - port type letter (type address model only), empty string of not present
    
    private static volatile Pattern addrPattern = Pattern.compile(addrRegex);
    
    private static final Map<Character, LcOutputType> portTypeList = createPortTypeList(); //port type map
    
    private static Map<Character, LcOutputType> createPortTypeList() {
        Map<Character, LcOutputType> l = new HashMap<>();
        l.put('S', LcOutputType.SWITCHPORT);
        l.put('L', LcOutputType.LIGHTPORT);
        l.put('V', LcOutputType.SERVOPORT);
        l.put('U', LcOutputType.SOUNDPORT);
        l.put('M', LcOutputType.MOTORPORT);
        l.put('A', LcOutputType.ANALOGPORT);
        l.put('B', LcOutputType.BACKLIGHTPORT);
        l.put('P', LcOutputType.SWITCHPAIRPORT);
        l.put('I', LcOutputType.INPUTPORT);
        return Collections.unmodifiableMap(l);
    }
    
    
    /**
     * Construct from system name - needs prefix and type letter
     * 
     * @param systemName the JMRI system name for which the adress object is to be created
     * @param typeLetter the type letter from the calling manager (T, L, S, R)
     * @param memo connection memo object
     */
    public BiDiBAddress(String systemName, char typeLetter, BiDiBSystemConnectionMemo memo) {
        aString = systemName.substring(memo.getSystemPrefix().length() + 1);
        log.debug("ctor: systemName: {}, typeLetter: {}, systemPrefix: {}", systemName, typeLetter, memo.getSystemPrefix());
        
        parse(systemName, typeLetter, memo);
    }
    
    // now parse
    // supported formats are 
    //      <nodeuid>:<addr>
    //      <addr>            use root node
    // For outputs (Turnouts and signals, type "T"), addr may start with "t" (DCC address), "p" (local port) or "a" (local assessory).
    // If no address prefix is given, it defaults to DCC address ("t") as long as the node is a command station.
    // If the node is not command station, it defaults to BiDiB accessory number ("a") for Turnouts and Signals (type letter T)
    // otherwise to a local port number ("p").
    // For inputs (Sensors), addr may start with "f" (Bidib feedback) or "p" (just an input port). Default is "f".

    // type addressing for ports: S=Switch, L=Light, V=Servo, U=Sound, M=Motor, A=Analogout, B=Backlight, P=Switchpair, I=Input
    // addr: p123S

    private void parse(String systemName, char typeLetter, BiDiBSystemConnectionMemo memo) {
        BiDiBTrafficController tc = memo.getBiDiBTrafficController();
        if (!aString.isEmpty()  &&  systemName.charAt(memo.getSystemPrefix().length()) == typeLetter) {
            Node foundNode;
            try {
                Matcher matcher = addrPattern.matcher(aString);
                if (!matcher.matches()) {
                    log.trace("systemName {} does not match regular expression", systemName);
                    //throw new Exception("Illegal address: " + aString);
                    throw new jmri.NamedBean.BadSystemNameException(Locale.getDefault(), "InvalidSystemName",systemName,"");
                }
                  // DEBUG
//                for (int i = 0; i <= matcher.groupCount(); i++) {
//                    log.trace("  {}: {}", i, matcher.group(i));
//                }
                if (matcher.group(1) != null) {
                    nodeuid = Long.parseLong(matcher.group(1), 16); //nodeuid in hex
                }
                else if (matcher.group(2) != null) {
                    Node n = tc.getNodeByUserName(matcher.group(2));
                    if (n != null) {
                        nodeuid = n.getUniqueId() & 0xFFFFFFFFFFL;
                    }
                    else {
                        throw new Exception("No such node: " + matcher.group(2));
                    }
                }
                addrType = matcher.group(3).toLowerCase();
                addr = Integer.parseInt(matcher.group(4));
                String t = matcher.group(5).toUpperCase();
                if (!t.isEmpty()) {
                    portType = portTypeList.get(t.charAt(0));
                }
                
                if (nodeuid == 0) {
                    // no unique id given - use root node which always has node address 0
                    foundNode = tc.getRootNode();
                    if (foundNode != null) {
                        nodeuid = foundNode.getUniqueId() & 0xFFFFFFFFFFL;
                    }
                }
                else {
                    log.trace("trying UID {}", ByteUtils.formatHexUniqueId(nodeuid));
                    foundNode = tc.getNodeByUniqueID(nodeuid);
                }
                log.trace("found node: {}", foundNode); 
                if (foundNode != null) {
                    long uid = foundNode.getUniqueId();
                    if (typeLetter == 'S') {
                        switch(addrType) {
                            case "t":
                                addrType = "f"; //what does "t" mean here? Silently convert to "f"
                                if (!NodeUtils.hasFeedbackFunctions(uid)) addrType = "";
                                break; //don't use "fall through" as some code checkers does not like it...
                            case "f":
                                if (!NodeUtils.hasFeedbackFunctions(uid)) addrType = "";
                                break;
                            case "p":
                                if (!NodeUtils.hasSwitchFunctions(uid)) addrType = "";
                                break;
                            case "":
                                if (NodeUtils.hasFeedbackFunctions(uid)) addrType = "f";
                                else if (NodeUtils.hasSwitchFunctions(uid)) addrType = "p";
                                break;
                            default:
                                addrType = "";
                                break;
                        }
                        if (addrType.equals("p")) {
                            if (portType == null) {
                                portType = LcOutputType.INPUTPORT; //types other than Input do not make sense...
                            }
                            if (!portType.equals(LcOutputType.INPUTPORT)) {
                                addrType = "";
                            }
                        }
                    }
                    else if (typeLetter == 'R') {
                        if (addrType.isEmpty()) {
                            addrType = "f";
                        }
                        if (!addrType.equals("f")) {
                            addrType = "";
                        }
                    }
                    else if (typeLetter == 'T') {
                        switch(addrType) {
                            case "a":
                                if (!NodeUtils.hasAccessoryFunctions(uid)) addrType = "";
                                break;
                            case "p":
                                if (!NodeUtils.hasSwitchFunctions(uid)) addrType = "";
                                break;
                            case "t":
                                if (!NodeUtils.hasCommandStationFunctions(uid)) addrType = "";
                                break;
                            case "":
                                if (NodeUtils.hasCommandStationFunctions(uid)) addrType = "t";
                                else if (NodeUtils.hasAccessoryFunctions(uid)) addrType = "a";
                                else if (NodeUtils.hasSwitchFunctions(uid)) addrType = "p";
                                break;
                            default:
                                addrType = "";
                                break;
                        }
                        if (addrType.equals("p")  &&  portType != null  &&  portType.equals(LcOutputType.INPUTPORT)) {
                            addrType = "";
                        }
                    }
                    else if (typeLetter == 'L') {
                        switch(addrType) {
                            case "p":
                                if (!NodeUtils.hasSwitchFunctions(uid)) addrType = "";
                                break;
                            case "t":
                                if (!NodeUtils.hasCommandStationFunctions(uid)) addrType = "";
                                break;
                            case "":
                                if (NodeUtils.hasSwitchFunctions(uid)) addrType = "p";
                                else if (NodeUtils.hasCommandStationFunctions(uid)) addrType = "t";
                                break;
                            default:
                                addrType = "";
                                break;
                        }
                        if (addrType.equals("p")  &&  portType != null  &&  portType.equals(LcOutputType.INPUTPORT)) {
                            addrType = "";
                        }
                    }
                    if (addrType.equals("p")) {
                        if (!foundNode.isPortFlatModelAvailable()  &&  portType == null) {
//                            addrType = ""; //type addr model must have a port type
                            portType = LcOutputType.SWITCHPORT;
                        }
                    }
                    else {
                        if (portType != null) {
                            addrType = ""; //port type not allowed on other address types than 'p'
                        }
                    }
                    if (addr >= 0  &&  !addrType.isEmpty()) {
                        node = foundNode;
                    }
                }
                if (!isValid()) {
                    throw new Exception("Invalid BiDiB address: " + systemName);
                }
            }
            catch (Exception e) {
                //log.trace("parse of BiDiBAddress throws {}", e);
                node = null;
            }
        }
        
        if (isValid()) {
            log.debug("BiDiB \"{}\" -> {}", systemName, toString());
        }
        else {
            log.warn("*** BiDiB system name \"{}\" is invalid", systemName);
        }
    }

    /**
     * Static method to check system name syntax. Does not check if the node is available
     * 
     * @param systemName the JMRI system name for which the adress object is to be created
     * @param typeLetter the type letter from the calling manager (T, L, S, R)
     * @param memo connection memo object
     * @return true if the system name is syntactically valid.
     */
    static public boolean isValidSystemNameFormat(String systemName, char typeLetter, BiDiBSystemConnectionMemo memo) {
        String aString = systemName.substring(memo.getSystemPrefix().length() + 1);
        if (addrPattern == null) {
            addrPattern = Pattern.compile(addrRegex);
            log.trace("regexp: {}", addrRegex);
        }
        if (!aString.isEmpty()  &&  systemName.charAt(memo.getSystemPrefix().length()) == typeLetter) {
            Matcher matcher = addrPattern.matcher(aString);
            if (matcher.matches()) {
                return true;
            }
            else {
                log.trace("systemName {} does not match regular expression", systemName);
                //throw new Exception("Illegal address: " + aString);
                //throw new jmri.NamedBean.BadSystemNameException(Locale.getDefault(), "InvalidSystemName",systemName);
                return false;
            }
        }
        return false;
    }
    
    /**
     * Invalidate this BiDiBAddress by removing the node.
     * Used when the node gets lost.
     */
    public void invalidate() {
        log.warn("BiDiB address invalidated: {}", this);
        node = null;
        nodeuid = 0;
    }
    
    /**
     * Check if the object contains a valid BiDiB address
     * The object is invalied the the system is syntactically wrong or if the requested node is not available
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return (node != null);
    }
    
    /**
     * Check if the address is a BiDiB Port address (LC)
     * 
     * @return true if the object represents a BiDiB Port address 
     */
    public boolean isPortAddr() {
        return (addrType.equals("p"));
    }
    
    /**
     * Check if the address is a BiDiB Accessory address.
     * 
     * @return true if the object represents a BiDiB Accessory address 
     */
    public boolean isAccessoryAddr() {
        return (addrType.equals("a"));
    }
    
    /**
     * Check if the address is a BiDiB feedback Number (BM).
     * 
     * @return true if the object represents a BiDiB feedback Number
     */
    public boolean isFeedbackAddr() {
        return (addrType.equals("f"));
    }
    
    /**
     * Check if the address is a BiDiB track address (i.e. a DCC accessory address).
     * 
     * @return true if the object represents a BiDiB track address
     */
    public boolean isTrackAddr() {
        return (addrType.equals("t"));
    }
    
    /**
     * Get the 40 bit unique ID of the found node
     * 
     * @return the 40 bit node unique ID
     */
    public long getNodeUID() {
        return nodeuid;
    }
    
    /**
     * Get the address inside the node.
     * This may be a DCC address (for DCC-Accessories), an accessory number (for BiDiB accessories)
     * or a port number (for LC ports)
     * 
     * @return address inside node
     */
    public int getAddr() {
        return addr;
    }
    
    /**
     * Get the address as string exactly as given when the instance has been created
     * 
     * @return address as string
     */
    public String getAddrString() {
        return aString;
    }
    
    /**
     * Get the BiDiB Node object.
     * If the node is not available, null is returned.
     * 
     * @return Node object or null, if node is not available
     */
    public Node getNode() {
        return node;
    }
    
    /**
     * Get the BiDiB Node address.
     * If the node is not available, an empty address array is returned.
     * Note: The BiDiB node address is dynamically created address from the BiDiBbus
     * and is not suitable as a node ID. Use the Unique ID for that purpose.
     * 
     * @return Node address (byte array) or empty address array, if node is not available
     */
    public byte[] getNodeAddr() {
        byte[] ret = {};
        if (node != null) {
            ret = node.getAddr();
        }
        return ret;
    }
    
    /**
     * Get the port type as an LcOutputType object (SWITCHPORT, LIGHTPORT, ...)
     * 
     * @return LcOutputType object
     */
    public LcOutputType getPortType() {
        return portType;
    }
    
    /**
     * Get the address type as a lowercase single letter:
     * t    - DCC address of decoder (t stands for "Track")
     * a    - BiDiB Accessory Number
     * p    - BiDiB Port
     * f    - BiDiB Feedback Number (BM)
     * 
     * Not a public method since we want to hide this letter,
     * use isPortAddr() or isAccessoryAddr() instead.
     * 
     * @return single letter address type
     */
    protected String getAddrtype() {
        return addrType;
    }
    
    // some convenience methods
    
    /**
     * Check if the object contains an address in the BiDiB type based address model.
     * Returns false if the address is in the flat address model.
     * 
     * @return true if address is in the BiDiB type based address model.
     */
    public boolean isPortTypeBasedModel() {
        if (node != null) {
            if (isPortAddr()  &&  !node.isPortFlatModelAvailable()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check address against a BiDiBAddress object.
     * 
     * @param other as BiDiBAddress
     * @return true if same
     */
    public boolean isAddressEqual(BiDiBAddress other) {
        if (node == null  ||  other.getNodeUID() != getNodeUID()  ||  other.getAddr() != addr  ||  !other.getAddrtype().equals(addrType)) {
            return false;
        }
        if (isPortAddr()  &&  isPortTypeBasedModel()) {
            return other.getPortType() == portType;
        }
        return true;
    }

    /**
     * Check address against a LcConfig object.
     * 
     * @param lcConfig as LcConfig
     * @return true if the address contained in the LcConfig object is the same
     */
    public boolean isAddressEqual(LcConfig lcConfig) {
        return isAddressEqual(lcConfig.getBidibPort());
    }
    
    /**
     * Check address against a LcConfigX object.
     * 
     * @param lcConfigX as LcConfig
     * @return true if the address contained in the LcConfigX object is the same
     */
    public boolean isAddressEqual(LcConfigX lcConfigX) {
        return isAddressEqual(lcConfigX.getBidibPort());
    }
    
    /**
     * Check address against a BiDiBAddress object.
     * 
     * @param bidibPort as BiDiBPort
     * @return true if same
     */
    public boolean isAddressEqual(BidibPort bidibPort) {
        if (node == null  ||  !isPortAddr()) {
            return false;
        }
        if (node.isPortFlatModelAvailable()) {
            return bidibPort.getPortNumber(PortModelEnum.flat_extended) == addr;
        }
        else {
            return (bidibPort.getPortNumber(PortModelEnum.type) == addr  &&  bidibPort.getPortType(PortModelEnum.type) == portType);
        }
    }
    
    /**
     * Create a BiDiBPort object from this object
     * 
     * @return new BiDiBPort object 
     */
    public BidibPort makeBidibPort() {
        if (node == null  ||  !isPortAddr()) {
            return null;
        }
        return BidibPort.prepareBidibPort(PortModelEnum.getPortModel(node), portType, addr);
    }
    
    /**
     * Static method to parse a system Name.
     * A temporary BiDiDAdress object is created.
     * 
     * @param systemName the JMRI system name for which the adress object is to be created
     * @param typeLetter the type letter from the calling manager (T, L, S, R)
     * @param memo connection memo object
     * @return true if the system name is valid and the BiDiB Node is available
     */
    static public boolean isValidAddress(String systemName, char typeLetter, BiDiBSystemConnectionMemo memo) throws IllegalArgumentException {
        BiDiBAddress addr = new BiDiBAddress(systemName, typeLetter, memo);
        return addr.isValid();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String s = "";
        if (isPortAddr()) {
            s = "(" + (isPortTypeBasedModel() ? "type-based" : "flat") + "),portType=" + portType;
        }
        return "BiDiBAdress[UID=" + ByteUtils.formatHexUniqueId(nodeuid) + ",addrType=" + addrType + ",addr=" + addr + s + "]";
    }
    
    private final static Logger log = LoggerFactory.getLogger(BiDiBAddress.class);
}
