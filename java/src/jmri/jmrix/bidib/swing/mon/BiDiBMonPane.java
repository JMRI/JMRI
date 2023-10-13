package jmri.jmrix.bidib.swing.mon;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.BiDiBTrafficController;
import jmri.jmrix.bidib.swing.BiDiBPanelInterface;
import org.bidib.jbidibc.messages.AddressData;

import org.bidib.jbidibc.messages.CRC8;
import org.bidib.jbidibc.messages.BidibLibrary; //new
import org.bidib.jbidibc.messages.exception.ProtocolException; //new
import org.bidib.jbidibc.messages.utils.ByteUtils; //new
import org.bidib.jbidibc.messages.utils.NodeUtils;
import org.bidib.jbidibc.messages.base.RawMessageListener;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.Feature;
import org.bidib.jbidibc.messages.StringData;
import org.bidib.jbidibc.messages.enums.AddressTypeEnum;
import org.bidib.jbidibc.messages.enums.CommandStationProgState;
import org.bidib.jbidibc.messages.enums.CommandStationPt;
import org.bidib.jbidibc.messages.enums.LcOutputType;
import org.bidib.jbidibc.messages.enums.PortModelEnum;
import org.bidib.jbidibc.messages.message.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a MonFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 * @author Eckart Meyer    Copyright (c) 2020-2023
 */
public class BiDiBMonPane extends jmri.jmrix.AbstractMonPane implements BiDiBPanelInterface {

    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.bidib.swing.BiDiBSwingBundle"); // NOI18N

    protected BiDiBTrafficController tc = null;
    protected BiDiBSystemConnectionMemo memo = null;
    protected RawMessageListener rawMessageListener = null;
    private final BidibResponseFactory responseFactory = new BidibResponseFactory();
    private String output;
    private final Map<Long, String> debugStringBuffer = new HashMap<>();

    private final UserPreferencesManager pm;
    final JCheckBox suppressDiagMessagesCheckBox = new JCheckBox();
    final String suppressDiagMessagesCheck = this.getClass().getName() + ".SuppressDiagMessages";
    
    public BiDiBMonPane() {
        super();
        pm = InstanceManager.getDefault(UserPreferencesManager.class);
    }

//    @Override
//    public String getHelpTarget() {
//        // TODO: BiDiB specific help - if we need this
//        return "package.jmri.jmrix.bidib.MonFrame"; // NOI18N
//    }

    @Override
    public String getTitle() {
        return (rb.getString("BiDiBMonPaneTitle")); // NOI18N
    }

    @Override
    public void dispose() {
        log.debug("Stopping BiDiB Monitor Panel");
        if (rawMessageListener != null) {
            tc.removeRawMessageListener(rawMessageListener);        
            rawMessageListener = null;
        }
        pm.setSimplePreferenceState(suppressDiagMessagesCheck, suppressDiagMessagesCheckBox.isSelected());
        super.dispose();        
    }

    @Override
    public void init() {
    }

    @Override
    protected void addCustomControlPanes(JPanel parent) {

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                
        suppressDiagMessagesCheckBox.setText(rb.getString("CheckBoxSuppressDiagMessages"));
        suppressDiagMessagesCheckBox.setVisible(true);
        suppressDiagMessagesCheckBox.setSelected(pm.getSimplePreferenceState(suppressDiagMessagesCheck));
        p.add(suppressDiagMessagesCheckBox);

        parent.add(p);
        super.addCustomControlPanes(parent);
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof BiDiBSystemConnectionMemo) {
            initComponents((BiDiBSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(BiDiBSystemConnectionMemo memo) {
        log.debug("Starting BiDiB Monitor Panel");
        this.memo = memo;
        tc = memo.getBiDiBTrafficController();
        createMonListener();
    }
    
    private boolean suppressMessage(BidibMessageInterface message) {
        if (suppressDiagMessagesCheckBox.isSelected()) {
            int type = ByteUtils.getInt(message.getType());
            switch (type) {
                case BidibLibrary.MSG_BOOST_DIAGNOSTIC:
                case BidibLibrary.MSG_BM_SPEED:
                case BidibLibrary.MSG_BM_DYN_STATE:
                case BidibLibrary.MSG_BM_CURRENT:
                case BidibLibrary.MSG_CS_STATE:
                case BidibLibrary.MSG_CS_SET_STATE:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    private void log1Message(BidibMessageInterface message, String line) {
        Node node = tc.getNodeByAddr(message.getAddr());
        if (node != null) {
            output += String.format(" %010X (%s)", node.getUniqueId() & 0xffffffffffL, node.getStoredString(StringData.INDEX_USERNAME)) + ": ";
        }
        else {
            output += NodeUtils.formatAddress(message.getAddr()) + ": ";
        }
        if (rawCheckBox.isSelected()) {
            output += "[" + ByteUtils.bytesToHex(message.getContent()) + "] " + message.toString() + "  ";
        }
        output += line + "\n";
        
    }
    protected void logMessage(String prefix, byte[] data, List<BidibMessageInterface> messages, List<String> lines) {
        output = prefix + " ";
        if (messages.size() != 1) {
            if (rawCheckBox.isSelected()) {
                output += "[" + ByteUtils.bytesToHex(data) + "] ";
            }
            output += messages.size() + " Messages:\n";
        }
        if (messages.size() == 1) {
            log.debug("Monitor: show message: {}", ((BidibMessage)messages.get(0)).getName());
            if (suppressMessage(messages.get(0))) {
                return;
            }
            log1Message(messages.get(0), lines.get(0));
        }
        else {
            for (int i = 0; i < messages.size(); i++) {
                output += "        ";
                log1Message(messages.get(i), lines.get(i));
            }
        }
        nextLine(output, null);
    }
    
    private String evaluateMessage(final BidibMessageInterface message) {
        String line = "";
        Node node = tc.getNodeByAddr(message.getAddr());
        PortModelEnum portModel = PortModelEnum.type;
        if (node != null) {
            portModel = tc.getPortModel(node);
        }
        int type = ByteUtils.getInt(message.getType());
        switch (type) {
            // received messages
            case BidibLibrary.MSG_ACCESSORY_STATE:
            {
                AccessoryStateResponse m = (AccessoryStateResponse)message;
                if (m.getAccessoryState().getExecute() == 0) {
                    line = "accessory number: " + m.getAccessoryState().getAccessoryNumber() + ", aspect: " + m.getAccessoryState().getActiveAspect();
                }
                else {
                    line += m.getAccessoryState().toString();
                }
            }
                break;
            case BidibLibrary.MSG_BOOST_DIAGNOSTIC:
            {
                BoostDiagnosticResponse m = (BoostDiagnosticResponse)message;
                line = "Voltage: " + m.getVoltage() + " mV, Current: " + m.getCurrent() + " mA, Temperature: " + m.getTemperature() + " °C";
            }
                break;
            case BidibLibrary.MSG_BOOST_STAT:
            {
                BoostStatResponse m = (BoostStatResponse)message;
                line = "Booster State " + m.getState() + ", control: " + m.getControl();
            }
                break;
            case BidibLibrary.MSG_BM_ADDRESS:
            {
                FeedbackAddressResponse m = (FeedbackAddressResponse)message;
                line = "mnum: " + m.getDetectorNumber();
                line += ", locos: ";
                List<AddressData> addrList = m.getAddresses();
                if (addrList.size() > 0) {
                    for (AddressData addressData : addrList) {
                        //line += String.format("0x%d ", addressData.getAddress() & 0xff);
                        line += addressData + " ";
                    }
                }
            }
                break;
            case BidibLibrary.MSG_BM_CURRENT:
            {
                FeedbackCurrentResponse m = (FeedbackCurrentResponse)message;
                line = "mnum: " + m.getLocalDetectorAddress() + "current: " + m.getCurrent() + " mA";
            }
                break;
            case BidibLibrary.MSG_BM_DYN_STATE:
            {
                FeedbackDynStateResponse m = (FeedbackDynStateResponse)message;
                line = "mnum: " + m.getDetectorNumber() + ", decoder: " + m.getAddress() + " ";
                int dynNumber = m.getDynNumber();
                String dynText;
                switch (dynNumber) {
                    case 1:
                        dynText = rb.getString("BmDynState1"); // NOI18N
                        line += dynText + ": " + m.getDynValue() + "%";
                        break;
                    case 2:
                        dynText = rb.getString("BmDynState2"); // NOI18N
                        line += dynText + ": " + m.getDynValue() + " °C";
                        break;
                    case 3:
                        dynText = rb.getString("BmDynState3"); // NOI18N
                        line += dynText + ": " + m.getDynValue() + "%";
                        break;
                    case 4:
                        dynText = rb.getString("BmDynState4"); // NOI18N
                        line += dynText + ": " + m.getDynValue() + "%";
                        break;
                    case 5:
                        dynText = rb.getString("BmDynState5"); // NOI18N
                        line += dynText + ": " + m.getDynValue() + "%";
                        break;
                    case 6:
                        dynText = rb.getString("BmDynState6"); // NOI18N
                        line += dynText + ": " + m.getDynValue() + " mm";
                        if (m.getTimestamp() != null) {
                            dynText = rb.getString("BmDynStateTimeStamp"); // NOI18N
                            line += ", " + dynText + ": " + m.getTimestamp();
                        }
                        break;
                    default:
                        log.error("Unexpected case: {}", dynNumber);
                }
            }
                break;
            case BidibLibrary.MSG_BM_FREE:
            {
                FeedbackFreeResponse m = (FeedbackFreeResponse)message;
                line = "mnum: " + m.getDetectorNumber();
            }
                break;
            case BidibLibrary.MSG_BM_OCC:
            {
                FeedbackOccupiedResponse m = (FeedbackOccupiedResponse)message;
                line = "mnum: " + m.getDetectorNumber();
            }
                break;
            case BidibLibrary.MSG_BM_MULTIPLE:
            {
                FeedbackMultipleResponse m = (FeedbackMultipleResponse)message;
                line = "mnum: " + m.getBaseAddress() + ", size: " + m.getSize();
                line += ", state bits: ";
                byte[] stateBits = m.getDetectorData();
                if (stateBits.length > 0) {
                    for (int f : stateBits) {
                        line += String.format("0x%02X ", f & 0xff);
                    }
                }
            }
                break;
            case BidibLibrary.MSG_BM_SPEED:
            {
                FeedbackSpeedResponse m = (FeedbackSpeedResponse)message;
                AddressData addressData = m.getAddress();
                line = "Decoder: " + addressData + ", speed: " + m.getSpeed();
            }
                break;
            case BidibLibrary.MSG_BM_CV:
            {
                FeedbackCvResponse m = (FeedbackCvResponse)message;
                line = m.getAddress().toString() + ", CV" + m.getCvNumber() + " = " + m.getDat();
            }
                break;
            case BidibLibrary.MSG_CS_DRIVE_STATE:
            {
                CommandStationDriveStateResponse m = (CommandStationDriveStateResponse)message;
                AddressTypeEnum addressTypeEnum = AddressTypeEnum.LOCOMOTIVE_BACKWARD;
                if ((m.getSpeed() & 0x80) == 0x80) {
                    addressTypeEnum = AddressTypeEnum.LOCOMOTIVE_FORWARD;
                }
                AddressData addressData = new AddressData(m.getDecoderAddress(), addressTypeEnum);
                line = "Decoder: " + addressData + ", speed: " + (m.getSpeed() & 0x7F);
                line += ", function bits: ";
//                line += String.format("0x%02X  ", m.getFunctionBitsF0toF4());
                byte[] functionBits = m.getDriveState().getFunctions();
                if (functionBits.length > 0) {
                    for (int f : functionBits) {
                        line += String.format("0x%02X ", f & 0xff);
                    } 
                }
            }
                break;
            case BidibLibrary.MSG_CS_DRIVE_MANUAL:
            {
                CommandStationDriveManualResponse m = (CommandStationDriveManualResponse)message;
                AddressTypeEnum addressTypeEnum = AddressTypeEnum.LOCOMOTIVE_BACKWARD;
                if ((m.getSpeed() & 0x80) == 0x80) {
                    addressTypeEnum = AddressTypeEnum.LOCOMOTIVE_FORWARD;
                }
                AddressData addressData = new AddressData(m.getAddress(), addressTypeEnum);
                line = "Decoder: " + addressData + ", speed: " + (m.getSpeed() & 0x7F);
                line += ", function bits: ";
//                line += String.format("0x%02X  ", m.getFunctionBitsF0toF4());
                byte[] functionBits = m.getDriveState().getFunctions();
                if (functionBits.length > 0) {
                    for (int f : functionBits) {
                        line += String.format("0x%02X ", f & 0xff);
                    } 
                }
            }
                break;
            case BidibLibrary.MSG_CS_STATE:
            {
                CommandStationStateResponse m = (CommandStationStateResponse)message;
                line = "CS state " + m.getState();
            }
                break;
            case BidibLibrary.MSG_CS_POM_ACK:
            {
                CommandStationPomAcknowledgeResponse m = (CommandStationPomAcknowledgeResponse)message;
                line = "Addr: " + m.getAddress().toString() + ", Ack: " + m.getAcknState().toString();
            }
                break;
            case BidibLibrary.MSG_CS_PROG_STATE:
            {
                CommandStationProgStateResponse m = (CommandStationProgStateResponse)message;
                line = m.getState() + " CV" + (m.getCvNumber());
                if (m.getState() == CommandStationProgState.PROG_OKAY) {
                    line += " = " + m.getCvData();
                }
                line += ", remaining time: " + (m.getRemainingTime() * 100) + "ms";
            }
                break;
            case BidibLibrary.MSG_LC_STAT:
            {
                LcStatResponse m = (LcStatResponse)message;
                line = "port " + m.getPortNumber(portModel) + " (" + makePortTypeString(portModel, m.getPortType(portModel)) + "), state: " + (m.getPortStatus()& 0xFF);
            }
                break;
            case BidibLibrary.MSG_LC_NA:
            {
                LcNotAvailableResponse m = (LcNotAvailableResponse)message;
                line = "port " + m.getPortNumber(portModel) + " (" + makePortTypeString(portModel, m.getPortType(portModel)) + "), error code: " + (m.getErrorCode());
            }
                break;
            case BidibLibrary.MSG_NODETAB_COUNT:
            {
                NodeTabCountResponse m = (NodeTabCountResponse)message;
                line = "count: " + m.getCount();
            }
                break;
            case BidibLibrary.MSG_FEATURE_COUNT:
            {
                FeatureCountResponse m = (FeatureCountResponse)message;
                line = "count: " + m.getCount();
            }
                break;
            case BidibLibrary.MSG_FEATURE:
            {
                FeatureResponse m = (FeatureResponse)message;
                Feature f = m.getFeature();
                line = f.getFeatureName() + " (" + f.getType() + ") = " + f.getValue();
            }
                break;
            case BidibLibrary.MSG_STRING:
            {
                StringResponse m = (StringResponse)message;
                // handle debug messages from a node
                if (m.getStringData().getNamespace() == StringData.NAMESPACE_DEBUG) {
                    String prefix = "===== device";
                    int stringId = m.getStringData().getIndex();
                    String value = m.getStringData().getValue();
                    if (node == null) {
                        log.error("Found node null in MSG_STRING");
                        break;
                    }
                    long key = (node.getUniqueId() & 0x0000ffffffffffL) | (long)stringId << 40;
                    if (value.charAt(value.length() - 1) == '\n') {
                        String txt = "";
                        // check if we have previous received imcomplete text
                        if (debugStringBuffer.containsKey(key)) {
                            txt = debugStringBuffer.get(key);
                            debugStringBuffer.remove(key);
                        }
                        txt += value.replace("\n","");
                        String line2 = "";
                        switch(stringId) {
                            case StringData.INDEX_DEBUG_STDOUT:
                                line2 += prefix + " stdout: " + txt;
                                break;
                            case StringData.INDEX_DEBUG_STDERR:
                                line2 += prefix + " stderr: " + txt;
                                break;
                            case StringData.INDEX_DEBUG_WARN:
                                if (log.isWarnEnabled()) {
                                    line2 += prefix + " WARN: " + txt;
                                }
                                break;
                            case StringData.INDEX_DEBUG_INFO:
                                if (log.isInfoEnabled()) {
                                    line2 += prefix + " INFO: " + txt;
                                }
                                break;
                            case StringData.INDEX_DEBUG_DEBUG:
                                if (log.isDebugEnabled()) {
                                    line2 += prefix + " DEBUG: " + txt;
                                }
                                break;
                            case StringData.INDEX_DEBUG_TRACE:
                                if (log.isTraceEnabled()) {
                                    line2 += prefix + " TRACE: " + txt;
                                }
                                break;
                            default: break;
                        }
                        if (!line2.isEmpty()) {
                            line = line2;
                        }
                    }
                    else {
                        String txt = "";
                        if (debugStringBuffer.containsKey(key)) {
                            txt = debugStringBuffer.get(key);
                        }
                        debugStringBuffer.put(key, (txt + value));
                    }
                }
                else {
                    if (m.getStringData().getIndex() == 0) {
                        line = "Product Name: " + m.getStringData().getValue();
                    }
                    else if (m.getStringData().getIndex() == 1) {
                        line = "Username: " + m.getStringData().getValue();
                    }
                    else {
                        line = "index: " + m.getStringData().getIndex() + ", value: " + m.getStringData().getValue();
                    }
                }
            }
                break;
                
                
            // messages to send
            case BidibLibrary.MSG_ACCESSORY_GET:
            {
                AccessoryGetMessage m = (AccessoryGetMessage)message;
                line = "accessory number: " + m.getAccessoryNumber();
            }
                break;
            case BidibLibrary.MSG_ACCESSORY_SET:
            {
                AccessorySetMessage m = (AccessorySetMessage)message;
                line = "accessory number: " + m.getAccessoryNumber() + ", set aspect to " + m.getAspect();
            }    
                break;
            case BidibLibrary.MSG_CS_ACCESSORY:
            {
                CommandStationAccessoryMessage m = (CommandStationAccessoryMessage)message;
                line = "CS accessory decoder address: " + m.getDecoderAddress() + ", set aspect to " + m.getAspect();
            }    
                break;
            case BidibLibrary.MSG_CS_DRIVE:
            {
                CommandStationDriveMessage m = (CommandStationDriveMessage)message;
                line = "CS decoder address: " + m.getDecoderAddress() + ", speed: " + m.getSpeed();
                line += ", function bits: ";
                //line += String.format("0x%02X  ", m.getFunctionBitsF0toF4());
                int[] functionBits = m.getFunctionBits();
                if (functionBits.length > 0) {
                    for (int f : functionBits) {
                        line += String.format("0x%02X ", f & 0xff);
                    } 
                }
            }    
                break;
            case BidibLibrary.MSG_CS_SET_STATE:
            {
                CommandStationSetStateMessage m = (CommandStationSetStateMessage)message;
                line = "CS set state to " + m.getState();
            }    
                break;
            case BidibLibrary.MSG_CS_POM:
            {
                CommandStationPomMessage m = (CommandStationPomMessage)message;
                line = "OpCode " + ByteUtils.byteToHex(m.getOpCode()) + ", Addr: " + m.getDecoderAddress().toString() + ", CV" + m.getCvNumber();
                int op = m.getOpCode();
                if (op != 0x00  &&  op != 0x01  &&  op != 0x81) {
                    line += " = " + ByteUtils.getCvXValue(m.getData(), 9, m.getData().length - 9);
                }
            }
                break;
            case BidibLibrary.MSG_CS_PROG:
            {
                CommandStationProgMessage m = (CommandStationProgMessage)message;
                line = m.getOpCode() + " CV" + (m.getCvNumber());
                if (m.getOpCode() == CommandStationPt.BIDIB_CS_PROG_RDWR_BIT  ||  m.getOpCode() == CommandStationPt.BIDIB_CS_PROG_WR_BYTE) {
                    line += " = " + m.getCvData();
                }
            }
                break;
            case BidibLibrary.MSG_BM_ADDR_GET_RANGE:
            {
                FeedbackGetAddressRangeMessage m = (FeedbackGetAddressRangeMessage)message;
                line = "get feedback status from number " + m.getBegin() + " to " + m.getEnd();
            }    
                break;
            case BidibLibrary.MSG_LC_CONFIG_GET:
            {
                LcConfigGetMessage m = (LcConfigGetMessage)message;
                line = "get port config for port " + m.toString();
            }
                break;
            case BidibLibrary.MSG_LC_OUTPUT:
            {
                LcOutputMessage m = (LcOutputMessage)message;
                line = "output to port " + m.getOutputNumber(portModel) + " (" + makePortTypeString(portModel, m.getOutputType(portModel)) + "), state: " + (m.getOutputStatus() & 0xFF);
            }
                break;

            // - those messages either won't be used at all in JMRI or we just have not done it...:
            // received messages
            case BidibLibrary.MSG_BM_CONFIDENCE:
            case BidibLibrary.MSG_BM_POSITION:
            case BidibLibrary.MSG_BM_ACCESSORY: //what is this??
            case BidibLibrary.MSG_BM_XPOM:
            case BidibLibrary.MSG_BM_RCPLUS:
            case BidibLibrary.MSG_ACCESSORY_NOTIFY:
            case BidibLibrary.MSG_ACCESSORY_PARA:
            case BidibLibrary.MSG_LC_KEY:
            case BidibLibrary.MSG_LC_WAIT:
            case BidibLibrary.MSG_LC_CONFIG:
            case BidibLibrary.MSG_LC_CONFIGX:
            case BidibLibrary.MSG_LC_MACRO_PARA:
            case BidibLibrary.MSG_LC_MACRO:
            case BidibLibrary.MSG_LC_MACRO_STATE:
            case BidibLibrary.MSG_STALL:
            case BidibLibrary.MSG_NODE_NEW:
            case BidibLibrary.MSG_NODE_LOST:
            case BidibLibrary.MSG_NODE_NA:
            case BidibLibrary.MSG_NODETAB:
            case BidibLibrary.MSG_SYS_ERROR:
            case BidibLibrary.MSG_SYS_IDENTIFY_STATE:
            case BidibLibrary.MSG_SYS_PONG:
            case BidibLibrary.MSG_SYS_MAGIC:
            case BidibLibrary.MSG_SYS_P_VERSION:
            case BidibLibrary.MSG_SYS_SW_VERSION:
            case BidibLibrary.MSG_SYS_UNIQUE_ID:
            case BidibLibrary.MSG_CS_DRIVE_ACK:
            case BidibLibrary.MSG_CS_DRIVE_EVENT:
            case BidibLibrary.MSG_CS_ACCESSORY_ACK:
            case BidibLibrary.MSG_CS_ACCESSORY_MANUAL:
            case BidibLibrary.MSG_CS_RCPLUS_ACK:
            case BidibLibrary.MSG_CS_M4_ACK:
            case BidibLibrary.MSG_VENDOR_ACK:
            case BidibLibrary.MSG_VENDOR:
            case BidibLibrary.MSG_LOCAL_PONG:
            case BidibLibrary.MSG_LOCAL_BIDIB_UP:
            case BidibLibrary.MSG_FEATURE_NA:
            case BidibLibrary.MSG_FW_UPDATE_STAT:
            case BidibLibrary.MSG_LOGON:
            // messages to send
            case BidibLibrary.MSG_ACCESSORY_PARA_GET:
            case BidibLibrary.MSG_ACCESSORY_PARA_SET:
            case BidibLibrary.MSG_BOOST_OFF:
            case BidibLibrary.MSG_BOOST_ON:
            case BidibLibrary.MSG_BOOST_QUERY:
            case BidibLibrary.MSG_CS_BIN_STATE:
            case BidibLibrary.MSG_CS_M4:
            case BidibLibrary.MSG_CS_QUERY:
            case BidibLibrary.MSG_CS_RCPLUS:
            case BidibLibrary.MSG_FEATURE_GETALL:
            case BidibLibrary.MSG_FEATURE_GET:
            case BidibLibrary.MSG_FEATURE_GETNEXT:
            case BidibLibrary.MSG_FEATURE_SET:
            case BidibLibrary.MSG_BM_GET_CONFIDENCE:
            case BidibLibrary.MSG_BM_GET_RANGE:
            case BidibLibrary.MSG_BM_MIRROR_FREE:
            case BidibLibrary.MSG_BM_MIRROR_MULTIPLE:
            case BidibLibrary.MSG_BM_MIRROR_OCC:
            case BidibLibrary.MSG_BM_MIRROR_POSITION:
            case BidibLibrary.MSG_FW_UPDATE_OP:
            case BidibLibrary.MSG_LC_CONFIG_SET:
            case BidibLibrary.MSG_LC_CONFIGX_GET_ALL:
            case BidibLibrary.MSG_LC_CONFIGX_GET:
            case BidibLibrary.MSG_LC_CONFIGX_SET:
            case BidibLibrary.MSG_LC_KEY_QUERY:
            case BidibLibrary.MSG_LC_MACRO_GET:
            case BidibLibrary.MSG_LC_MACRO_HANDLE:
            case BidibLibrary.MSG_LC_MACRO_PARA_GET:
            case BidibLibrary.MSG_LC_MACRO_PARA_SET:
            case BidibLibrary.MSG_LC_MACRO_SET:
            case BidibLibrary.MSG_LC_PORT_QUERY:
            case BidibLibrary.MSG_LC_PORT_QUERY_ALL:
            case BidibLibrary.MSG_LOCAL_BIDIB_DOWN:
            case BidibLibrary.MSG_LOCAL_EMITTER:
            case BidibLibrary.MSG_LOCAL_PING:
            case BidibLibrary.MSG_NODE_CHANGED_ACK:
            case BidibLibrary.MSG_NODETAB_GETALL:
            case BidibLibrary.MSG_NODETAB_GETNEXT:
            case BidibLibrary.MSG_STRING_GET:
            case BidibLibrary.MSG_STRING_SET:
            case BidibLibrary.MSG_SYS_CLOCK:
            case BidibLibrary.MSG_SYS_DISABLE:
            case BidibLibrary.MSG_SYS_ENABLE:
            case BidibLibrary.MSG_SYS_GET_ERROR:
            case BidibLibrary.MSG_SYS_GET_P_VERSION:
            case BidibLibrary.MSG_SYS_GET_SW_VERSION:
            case BidibLibrary.MSG_SYS_GET_UNIQUE_ID:
            case BidibLibrary.MSG_SYS_IDENTIFY:
            case BidibLibrary.MSG_SYS_GET_MAGIC:
            case BidibLibrary.MSG_SYS_PING:
            case BidibLibrary.MSG_SYS_RESET:
            case BidibLibrary.MSG_VENDOR_DISABLE:
            case BidibLibrary.MSG_VENDOR_ENABLE:
            case BidibLibrary.MSG_VENDOR_GET:
            case BidibLibrary.MSG_VENDOR_SET:
            default:
                break;
        }
        BidibMessage m = (BidibMessage)message;
        if (type != BidibLibrary.MSG_STRING  ||  !line.isEmpty()) {
            return (line.isEmpty() ? m.getName() : m.getName() + ": " + line);
        }
        else {
            return "";
        }
    }
    
    private String makePortModelString(PortModelEnum portModel) {
        String portModelName = "unknown";
        switch (portModel) {
            case type:
                portModelName = "type-based";
                break;
            case flat:
                portModelName = "flat";
                break;
            case flat_extended:
                portModelName = "flat-extended";
                break;
            default:
                break;
        }
        return portModelName;
    }

    private String makePortTypeString(PortModelEnum portModel, LcOutputType portType) {
        String ret = makePortModelString(portModel);
        if (portModel == PortModelEnum.type) {
            ret += ", " + portType;
        }
        return ret;
    }
    
    private List<BidibMessageInterface> splitBidibMessages(byte[] data, boolean checkCRC)  throws ProtocolException {
        log.trace("splitMessages: {}", ByteUtils.bytesToHex(data));
        int index = 0;
        List<BidibMessageInterface> result = new LinkedList<>();

        while (index < data.length) {
            int size = ByteUtils.getInt(data[index]) + 1 /* len */;
            log.trace("Current size: {}", size);

            if (size <= 0) {
                throw new ProtocolException("cannot split messages, array size is " + size);
            }

            byte[] message = new byte[size];

            try {
                System.arraycopy(data, index, message, 0, message.length);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                log
                    .warn("Failed to copy, msg.len: {}, size: {}, output.len: {}, index: {}, output: {}",
                        message.length, size, data.length, index, ByteUtils.bytesToHex(data));
                throw new ProtocolException("Copy message data to buffer failed.");
            }
            result.add(responseFactory.create(message));
            index += size;

            if (checkCRC) {
                // CRC
                if (index == data.length - 1) {
                    int crc = 0;
                    int crcIndex = 0;
                    for (crcIndex = 0; crcIndex < data.length - 1; crcIndex++) {
                        crc = CRC8.getCrcValue((data[crcIndex] ^ crc) & 0xFF);
                    }
                    if (crc != (data[crcIndex] & 0xFF)) {
                        throw new ProtocolException(
                            "CRC failed: should be " + crc + " but was " + (data[crcIndex] & 0xFF));
                    }
                    break;
                }
            }
        }

        return result;

    }
    
    private void createMonListener() {
        rawMessageListener = new RawMessageListener() {
            @Override
            public void notifyReceived(byte[] data) {
                log.debug("MON received message");
                List<String> lines = new ArrayList<>();
                List<BidibMessageInterface> messages = new ArrayList<>();
                try {
//                    Collection<byte[]> messagesData = MessageUtils.splitBidibMessages(data, true);
//
//                    //log.debug("MON: Number of splited messages: {}", messagesData.size());
//
//                    for (byte[] messageArray : messagesData) {
//                        BidibMessageInterface message;
//                        try {
//                            message = responseFactory.create(messageArray);
//                            messages.add(message);
//                            String line = evaluateMessage(message);
//                            lines.add(line);
//                        }
//                        catch (ProtocolException ex) {
//                            log.error("Illegal BiDiB Message received: {} {}", messageArray, ex);
//                        }
                    List<BidibMessageInterface> commandMessages = splitBidibMessages(data, true);
                    for (BidibMessageInterface message : commandMessages) {
                        String line = evaluateMessage(message);
                        //log.debug("**line: \"{}\", isEmpty: {}", line, line.isEmpty());
                        if (!line.isEmpty()) {
                            messages.add(message);
                            lines.add(line);
                        }
                    }
                    if (messages.size() > 0) {
                        logMessage("<<", data, messages, lines);
                    }
                }
                catch (ProtocolException ex) {
                    log.warn("CRC failed.", ex);
                }
            }

            @Override
            public void notifySend(byte[] data) {
                log.debug("MON sending message");
                List<String> lines = new ArrayList<>();
                List<BidibMessageInterface> messages = new ArrayList<>();
                BidibRequestFactory requestFactory = tc.getBidib().getRootNode().getRequestFactory();
                try {
                    List<BidibMessageInterface> commandMessages = requestFactory.create(data);
                    for (BidibMessageInterface message : commandMessages) {
                        messages.add(message);
                        String line = evaluateMessage(message);
                        lines.add(line);
                    }
                    logMessage(">>", data, messages, lines);
                }
                catch (ProtocolException ex) {
                    log.error("Illegal BiDiB Message to send: {}", data, ex);
                }
            }
        };
        tc.addRawMessageListener(rawMessageListener);
    }
    

    /**
     * Nested class to create one of these using old-style defaults.
     */
//    static public class Default extends BiDiBNamedPaneAction {
//
//        public Default() {
//            super(Bundle.getMessage("MonitorXTitle", "RFID Device"),
//                    new JmriJFrameInterface(),
//                    BiDiBMonPane.class.getName(),
//                    InstanceManager.getDefault(BiDiBSystemConnectionMemo.class));
//        }
//    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBMonPane.class);

}
