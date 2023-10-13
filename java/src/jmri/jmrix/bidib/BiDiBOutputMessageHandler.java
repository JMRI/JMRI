package jmri.jmrix.bidib;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bidib.jbidibc.messages.BidibLibrary; //new
import org.bidib.jbidibc.messages.AccessoryState;
import org.bidib.jbidibc.messages.AccessoryStateOptions;
import org.bidib.jbidibc.messages.AddressData;
import org.bidib.jbidibc.messages.BidibPort;
import org.bidib.jbidibc.core.DefaultMessageListener;
import org.bidib.jbidibc.messages.LcConfig;
import org.bidib.jbidibc.messages.LcConfigX;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.ProtocolVersion;
import org.bidib.jbidibc.messages.enums.AccessoryAcknowledge;
import org.bidib.jbidibc.messages.enums.ActivateCoilEnum;
import org.bidib.jbidibc.messages.enums.AddressTypeEnum;
import org.bidib.jbidibc.messages.enums.LcOutputType;
import org.bidib.jbidibc.messages.enums.PortModelEnum;
import org.bidib.jbidibc.messages.enums.TimeBaseUnitEnum;
import org.bidib.jbidibc.messages.enums.TimingControlEnum;
import org.bidib.jbidibc.messages.message.AccessoryGetMessage;
import org.bidib.jbidibc.messages.message.AccessorySetMessage;
import org.bidib.jbidibc.messages.message.BidibCommandMessage;
import org.bidib.jbidibc.messages.message.BidibRequestFactory;
import org.bidib.jbidibc.messages.message.CommandStationAccessoryMessage;
import org.bidib.jbidibc.messages.message.FeedbackGetRangeMessage;
import org.bidib.jbidibc.messages.port.ReconfigPortConfigValue;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.bidib.jbidibc.messages.utils.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles output to:
 * - BiDiB Accessories
 * - DCC Accessories via command station
 * - BiDiB LC Ports
 * 
 * Output value is sent to the type according to the address type.
 * Incoming messages a are catched by the BiDiB Message listener, then some common
 * processing takes place and the new value is sent back to the listener of this class instance.
 * 
 * @author Eckart Meyer Copyright (C) 2020-2023
 */
public class BiDiBOutputMessageHandler extends DefaultMessageListener {

    private final BiDiBNamedBeanInterface nb;
    protected BiDiBTrafficController tc = null;
    protected String type;// for log output only, e.g. "TURNOUT"
    protected LcConfigX portConfigx;
    protected LcOutputType lcType; //cached type from ConfigX or fixed in type based address
    protected BidibRequestFactory requestFactory = null;
    final Object portConfigLock = new Object();

    // the configLock is used to synchronize config messages
    //private final Object configLock = new Object();
    
    // internal cs accessory request aspect table since MSG_CS_ACCESSORY_ACK does not return the accessory state
    private final Map<BiDiBAddress, Integer> csAccessoryAspectMap = new LinkedHashMap<>();

    BiDiBOutputMessageHandler(BiDiBNamedBeanInterface nb, String type, BiDiBTrafficController tc) {
        this.type = type;
        this.nb = nb;
        this.tc = tc;
        BiDiBAddress addr = nb.getAddr();
        if (addr.isValid()  &&  tc != null) {
            lcType = addr.getPortType();
            requestFactory = tc.getBidib().getNode(addr.getNode()).getRequestFactory();
        }
    }
    
    /**
     * Get the port configuration if output is a BiDiB port
     * 
     * @return port ConfigX or null if not a BiDiB port
     */
    public LcConfigX getConfigX() {
        return portConfigx;
    }
    
    /**
     * Get the port output type if output is a BiDiB port
     * 
     * @return port output type or null if not a BiDiB port
     */
    public LcOutputType getLcType() {
        return lcType;
    }
    
    /**
     * Send output request to traffic controller
     * Send new port value or aspect value
     * 
     * @param portstat BiDiB output value (see protocol description for valid values)
     */
    public void sendOutput(int portstat) {
        BiDiBAddress addr = nb.getAddr();
        log.trace("sendOutput: portstat: {}", portstat);
        if (addr.isValid()) {
            log.info("send output message to BiDiB: addr: {}, state: {}", addr, portstat);
            Node node = addr.getNode();
            if (addr.isPortAddr()) {
                if (portExists()  &&  requestFactory != null) {
                    waitQueryConfig();
                    BidibCommandMessage m = requestFactory.createLcOutputMessage(tc.getPortModel(node), lcType, addr.getAddr(), portstat);
                    tc.sendBiDiBMessage(m, node);
                }
            }
            else if (addr.isAccessoryAddr()) {
                if (accessoryExists()) {
                    tc.sendBiDiBMessage(new AccessorySetMessage(addr.getAddr(), portstat), node);
                }
            }
            else if (addr.isTrackAddr()) { //send a CS Accessory Message
                // can't check address
                tc.sendBiDiBMessage(new CommandStationAccessoryMessage(addr.getAddr(), AddressTypeEnum.ACCESSORY,
                            TimingControlEnum.COIL_ON_OFF, ActivateCoilEnum.COIL_ON, portstat & 0x1F, TimeBaseUnitEnum.UNIT_100MS, 0), node);
                // remember the requested aspect since the ACK messgae does not contain any state and we also cannot query the state
                csAccessoryAspectMap.put(addr, portstat & 0x1F);
            }
            else {
                log.error("sending output message not supported for address type");
            }
        }
        else {
            log.warn("node is not available, UID: {}", ByteUtils.formatHexUniqueId(addr.getNodeUID()));
        }
    }
    
    public void sendQueryConfig() {
        BiDiBAddress addr = nb.getAddr();
        log.trace("queryOutput for addr: {}", addr);
        if (addr.isValid()) {
            log.debug("send query config message to BiDiB: addr: {}", addr);
            Node node = addr.getNode();
            if (addr.isPortAddr()  &&  portExists()  &&  requestFactory != null) {
                log.info("send port query config message to BiDiB: addr: {}", addr);
                // only ports have configurations
                BidibCommandMessage m;
                if (node.getProtocolVersion().isHigherThan(ProtocolVersion.VERSION_0_6)) { //ConfigX is available since V0.6
                    m = requestFactory.createLcConfigXGet(tc.getPortModel(node), lcType, addr.getAddr());
                }
                else {
                    m = requestFactory.createLcConfigGet(tc.getPortModel(node), lcType, addr.getAddr());
                }
                portConfigx = null;// invalidate
                tc.sendBiDiBMessage(m, node);
            }
        }
        else {
            log.warn("node is not available, UID: {}", ByteUtils.formatHexUniqueId(addr.getNodeUID()));
        }
    }
    
    public void waitQueryConfig() {
        BiDiBAddress addr = nb.getAddr();
        if (addr.isValid()) {
            if (addr.isPortAddr()) {
                synchronized (portConfigLock) {
                    while (portConfigx == null) {// "while" instead of "if" - see Doku of Java Object()
                        try {
                            log.debug("wait for config message from BiDiB: addr: {}", addr);
                            // wait will relinquish synchronization claim and wait for notifyAll()
                            portConfigLock.wait(500L);
                            // on return the synchronization claim is re-established
                        }
                        catch (InterruptedException ie) {
                            log.warn("Wait for port config was interrupted.", ie);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Send output query request to traffic controller
     */
    public void sendQuery() {
        BiDiBAddress addr = nb.getAddr();
        log.trace("queryOutput for addr: {}", addr);
        if (addr.isValid()) {
            log.info("send query output message to BiDiB: addr: {}", addr);
            Node node = addr.getNode();
            if (addr.isPortAddr()) {
                if (portExists()  &&  requestFactory != null) {
                    waitQueryConfig();
                    BidibCommandMessage m;
                    if (node.getProtocolVersion().isHigherThan(ProtocolVersion.VERSION_0_6)  ||  lcType.getType() <= 7) {
                        m = requestFactory.createLcPortQuery(tc.getPortModel(node), lcType, addr.getAddr());
                    }
                    else {
                        // this is only used for input ports on nodes with older firmware (<= 0.6)
                        m = requestFactory.createLcKey(addr.getAddr());
                    }
                    tc.sendBiDiBMessage(m, node);
                }
            }
            else if (addr.isAccessoryAddr()) {
                if (accessoryExists()) {
                    tc.sendBiDiBMessage(new AccessoryGetMessage(addr.getAddr()), node);
                }
            }
            else if (addr.isTrackAddr()) {
                // can't check and can't request
                // no warning/error level here please - would break LightManager test unit (some test expect specific warn/error)
                log.info("query of a CS accessory is not possible.");
            }
            else if (addr.isFeedbackAddr()) {
                if (feedbackExists()) {
                    int a = (addr.getAddr() / 8) * 8;
                    int b = ((addr.getAddr() + 8) / 8) * 8; //exclusive end address
                    log.debug("  requesting feedback from {} to {}", a, b);
                    tc.sendBiDiBMessage(new FeedbackGetRangeMessage(a, b), node);
                }
            }
            else {
                log.error("sending query output message not supported for address type, addr: {}", addr);
            }
        }
        else {
            log.warn("node is not available, UID: {}", ByteUtils.formatHexUniqueId(addr.getNodeUID()));
        }
    }

    /**
     * Get the number of ports for a node by type.
     * For type based addressing this is the real number of ports of this type, addresses starting from 0.
     * For flat addressing this is only a hint how many ports of this type exists in the flat address range.
     * In the latter case this feature may not been implemented and will return 0.
     * 
     * @param node to check
     * @param type to look for
     * @return number of ports for this type.
     */
    private int getPortTypeCount(Node node, LcOutputType type) {
        int id;
        switch (type) {
            case SWITCHPORT:
            case SWITCHPAIRPORT:
                id = BidibLibrary.FEATURE_CTRL_SWITCH_COUNT;
                break;
            case LIGHTPORT:
                id = BidibLibrary.FEATURE_CTRL_LIGHT_COUNT;
                break;
            case SERVOPORT:
                id = BidibLibrary.FEATURE_CTRL_SERVO_COUNT;
                break;
            case SOUNDPORT:
                id = BidibLibrary.FEATURE_CTRL_SOUND_COUNT;
                break;
            case MOTORPORT:
                id = BidibLibrary.FEATURE_CTRL_MOTOR_COUNT;
                break;
            case ANALOGPORT:
                id = BidibLibrary.FEATURE_CTRL_ANALOGOUT_COUNT;
                break;
            case BACKLIGHTPORT:
                id = BidibLibrary.FEATURE_CTRL_BACKLIGHT_COUNT;
                break;
            case INPUTPORT:
                id = BidibLibrary.FEATURE_CTRL_INPUT_COUNT;
                break;
            default:
                return 0;
        }
        return tc.getNodeFeature(node, id);
    }

    private boolean portExists() {
        BiDiBAddress addr = nb.getAddr();
        if (addr.isPortAddr()) {
            Node node = addr.getNode();
            if (addr.isPortTypeBasedModel()) {
                if (addr.getAddr() >= 0  &&  addr.getAddr() < getPortTypeCount(addr.getNode(), lcType)) {
                    return true;
                }
            }
            else {
                if (addr.getAddr() >= 0  &&  addr.getAddr() < node.getPortFlatModel()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean accessoryExists() {
        BiDiBAddress addr = nb.getAddr();
        if (addr.isAccessoryAddr()) {
            if (addr.getAddr() >= 0  &&  addr.getAddr() < tc.getNodeFeature(addr.getNode(), BidibLibrary.FEATURE_ACCESSORY_COUNT)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean feedbackExists() {
        BiDiBAddress addr = nb.getAddr();
        if (addr.isFeedbackAddr()) {
            if (addr.getAddr() >= 0  &&  addr.getAddr() < tc.getNodeFeature(addr.getNode(), BidibLibrary.FEATURE_BM_SIZE)) {
                return true;
            }
        }
        return false;
    }

// Overridable methods for notifications
    
    /**
     * Notify output state
     * @param state desired state from NamedBean list
     */
    public void newOutputState(int state) {
    }
    
    /**
     * Notify error state
     * @param err - BiDiB error number
     */
    public void errorState(int err) {
    }
    
    /**
     * Notify output will change later
     * @param time in msec
     */
    public void outputWait(int time) {
    }
    
    /**
     * Notify LC port ConfigX
     * 
     * @param lcConfigX input
     * @param lcType input
     */
    public void newLcConfigX(LcConfigX lcConfigX, LcOutputType lcType) {
    }
    
        
// Accessory related received messages
    
// - BiDiB Accessories
    
    @Override
    public void accessoryState(byte[] address, int messageNum, final AccessoryState accessoryState, final AccessoryStateOptions accessoryStateOptions) {
        BiDiBAddress addr = nb.getAddr();
        //log.trace("node UID: {}, node addr: {}, msg node addr: {}, state: {}", addr.getNodeUID(), addr.getNodeAddr(), address, accessoryState);
        if (addr.isAccessoryAddr()  &&  NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.getAddr() == accessoryState.getAccessoryNumber()) {
            log.info("{} accessory state was signalled, state: {}, opts: {}, node: {}",
                    type, accessoryState, accessoryStateOptions, addr);
            if (accessoryState.hasError()) {
                log.warn("Accessory state error: {}", accessoryState.getErrorInformation());
                errorState(accessoryState.getErrorCode());
            }
            else {
                if (accessoryState.getWait() == 0) {
                    newOutputState(accessoryState.getActiveAspect());
                }
                else {
                    outputWait(accessoryState.getWait());
                }
            }
        }
    }

// - DCC Accessories

    @Override
    public void csAccessoryAcknowledge(byte[] address, int messageNum, int decoderAddress, AccessoryAcknowledge acknowledge) {
        BiDiBAddress addr = nb.getAddr();
        //log.trace("node UID: {}, node addr: {}, msg node addr: {}", addr.getNodeUID(), addr.getNodeAddr(), address);
        if (addr.isTrackAddr()  &&  NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.getAddr() == decoderAddress) {
            log.info("{} CS accessory ackn was signalled, acknowledge: {}, decoderAddress: {}, node: {}", type, acknowledge, decoderAddress, addr);
            if (acknowledge == AccessoryAcknowledge.NOT_ACKNOWLEDGED) {
                log.warn("NOT acknowledged!");
                errorState(csAccessoryAspectMap.get(addr));
            }
            else {
                if (acknowledge == AccessoryAcknowledge.DELAYED) {
                    outputWait(0);
                }
                else {
                    // since the message does not contain an aspect, we just return the requested aspect from the map
                    newOutputState(csAccessoryAspectMap.get(addr));
                }
            }
        }
    }
    @Override
    public void csAccessoryManual(byte[] address, int messageNum, AddressData decoderAddress, ActivateCoilEnum activate, int aspect) {
        BiDiBAddress addr = nb.getAddr();
        //log.trace("node UID: {}, node addr: {}, msg node addr: {}, decoder address", addr.getNodeUID(), addr.getNodeAddr(), address, decoderAddress);
        if (addr.isAccessoryAddr()  &&  NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.getAddr() == decoderAddress.getAddress()) {
            log.info("{} accessory manual was signalled, activate coil: {}, aspect: {}, decoder address: {}, node: {}",
                    type, activate.getType(), aspect, decoderAddress.getAddress(), addr);
                newOutputState(aspect);
        }
    }

// LightControl related received messages
    
    private void notifyLcConfigX(LcConfigX lcConfigX) {
        BiDiBAddress addr = nb.getAddr();
        log.trace("portConfigx: {}", lcConfigX);
        if (addr.getNode().isPortFlatModelAvailable()) {
            ReconfigPortConfigValue p = (ReconfigPortConfigValue)lcConfigX.getPortConfig().get(BidibLibrary.BIDIB_PCFG_RECONFIG);
            log.warn("reconfig: {}, type: {}", p, p.getCurrentOutputType());
            if (lcType != p.getCurrentOutputType()) {
                log.warn("** reconfig: {}, type changed: {} -> {}", p, lcType, p.getCurrentOutputType());
            }
            lcType = p.getCurrentOutputType();
        }
        else {
            lcType = lcConfigX.getOutputType(PortModelEnum.type);
        }
        newLcConfigX(lcConfigX, lcType);
    }
    
    @Override
    public void lcStat(byte[] address, int messageNum, BidibPort bidibPort, int portStatus) {
        BiDiBAddress addr = nb.getAddr();
        //log.trace("lcStat: node UID: {}, node addr: {}, address: {}, port: {}, stat: {}", addr.getNodeUID(), addr.getNodeAddr(), address, bidibPort, portStatus);
        if (addr.isPortAddr()  &&  NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.isAddressEqual(bidibPort)) {
            log.info("{} LC status was signalled, state: {}, type: {}, node: {}", type, portStatus, lcType, addr);
            newOutputState(portStatus);
        }
    }
    @Override
    public void lcWait(byte[] address, int messageNum, BidibPort bidibPort, int time) {
        BiDiBAddress addr = nb.getAddr();
        //log.trace("lcStat: node UID: {}, node addr: {}, address: {}, port: {}, time: {}", addr.getNodeUID(), addr.getNodeAddr(), address, bidibPort, time);
        if (addr.isPortAddr()  &&  NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.isAddressEqual(bidibPort)) {
            log.info("{} LC Wait was signalled, wait: {}, type: {}, node: {}", type, time, lcType, addr);
            outputWait(time);
        }
    }
    @Override
    public void lcNa(byte[] address, int messageNum, BidibPort bidibPort, Integer errorCode) {
        BiDiBAddress addr = nb.getAddr();
        //log.trace("lcNa: node UID: {}, node addr: {}, address: {}, port: {}, errorCode: {}", addr.getNodeUID(), addr.getNodeAddr(), address, bidibPort, errorCode);
        if (addr.isPortAddr()  &&  NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.isAddressEqual(bidibPort)) {
            log.info("{} LC NA was signalled, error: {}, type: {}, node: {}", type, errorCode, lcType, addr);
            errorState(errorCode);
        }
    }
    @Override
    public void lcConfig(byte[] address, int messageNum, LcConfig lcConfig) {
        BiDiBAddress addr = nb.getAddr();
        //log.trace("lcConfig: node addr: {}, config: {}", address, lcConfig);
        if (addr.isPortAddr()  &&  NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.isAddressEqual(lcConfig)) {
            log.info("{} LC Config was signalled, config: {}, node: {}", type, lcConfig, addr);
            synchronized (portConfigLock) {
                portConfigx = tc.convertConfig2ConfigX(addr.getNode(), lcConfig);
                notifyLcConfigX(portConfigx);
                portConfigLock.notifyAll();
            }
        }
    }
    @Override
    public void lcConfigX(byte[] address, int messageNum, LcConfigX lcConfigX) {
        BiDiBAddress addr = nb.getAddr();
        //log.trace("lcConfigX: node addr: {}, configx: {}", address, lcConfigX);
        if (addr.isPortAddr()  &&  NodeUtils.isAddressEqual(addr.getNodeAddr(), address)  &&  addr.isAddressEqual(lcConfigX)) {
            log.info("{} LC ConfigX was signalled, configx: {}, node: {}", type, lcConfigX, addr);
            synchronized (portConfigLock) {
                portConfigx = new LcConfigX(addr.makeBidibPort(), new LinkedHashMap<>() );
                portConfigx.getPortConfig().putAll(lcConfigX.getPortConfig());
                notifyLcConfigX(portConfigx);
                portConfigLock.notifyAll();
            }
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(BiDiBOutputMessageHandler.class);
}
