package jmri.jmrix.acela;

import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Acela-specific Sensor implementation.
 * <p>
 * System names are "ASnnnn", where nnnn is the sensor number without padding.
 * <p>
 * Sensors are numbered from 0.
 * <p>
 * This is an AcelaListener to handle the replies to poll messages. Those are
 * forwarded to the specific AcelaNode object corresponding to their origin for
 * processing of the data.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007
 * @author Dave Duchamp, multi node extensions, 2004
 * @author Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaSensorManager extends jmri.managers.AbstractSensorManager
        implements AcelaListener {

    private AcelaSystemConnectionMemo _memo = null;

    public AcelaSensorManager(AcelaSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /**
     * Get the configured system prefix for this connection.
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    /**
     * Create a new sensor if all checks are passed. System name is normalized to
     * ensure uniqueness.
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            log.error("Invalid Acela Sensor system name: {}", systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            log.error("Sensor with this name already exists: {}", systemName);
            return null;
        }
        // check under alternate name
        String altName = AcelaAddress.convertSystemNameToAlternate(sName, getSystemPrefix());
        s = getBySystemName(altName);
        if (s != null) {
            log.error("Sensor with name: '{}' already exists as: '{}'", systemName, altName);
            return null;
        }
        // check bit number
        int bit = AcelaAddress.getBitFromSystemName(sName, getSystemPrefix());
        if ((bit < 0) || (bit >= 1023)) {
            log.error("Sensor bit number {} is outside the supported range 1-1024", Integer.toString(bit));
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new AcelaSensor(sName);
        } else {
            s = new AcelaSensor(sName, userName);
        }

        // ensure that a corresponding Acela Node exists
        AcelaNode node = AcelaAddress.getNodeFromSystemName(sName, _memo);
        if (node == null) {
            log.warn("Sensor: {} refers to an undefined Acela Node.", sName);
            return s;
        }
        if (!node.hasActiveSensors) {
            int newnodeaddress;
            newnodeaddress = node.getNodeAddress();
            log.warn("We got the wrong node: {}", newnodeaddress);
            return s;
        }
        // register this sensor with the Acela Node
        node.registerSensor(s, bit);
        return s;
    }

    /**
     * Public method to validate system name format.
     *
     * @return VALID if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (AcelaAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()));
    }

    /**
     * Public method to normalize a system name.
     *
     * @return a normalized system name if system name has a valid format,
     * else return ""
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (AcelaAddress.normalizeSystemName(systemName, getSystemPrefix()));
    }

    /**
     * Dummy routine
     */
    @Override
    public void message(AcelaMessage r) {
        log.warn("unexpected message");
    }

    /**
     * Process a reply to a poll of Sensors of one node.
     */
    @Override
    public void reply(AcelaReply r) {
        // Determine which state we are in: Initializing Acela Network or Polling Sensors
        boolean currentstate = _memo.getTrafficController().getAcelaTrafficControllerState();
        //  Flag to indicate which state we are in: 
        //  false == Initializing Acela Network
        //  true == Polling Sensors
        if (!currentstate) {
            int replysize = r.getNumDataElements();
            if (replysize == 0) {
                // The Acela Online command seems to return an empty message
                log.warn("We got an empty reply of size: {}", replysize);
            } else {
                if (replysize == 1) {
                    byte replyvalue = (byte) (r.getElement(0));
                    if (replyvalue == 0x00) {
                        //  Everything is OK.
                    } else {  //  Assume this is the response to the pollnodes
                        log.warn("We got a bad return code: {}", replyvalue);
                    }
                } else {
                    for (int i = 0; i < replysize; i++) {
                        byte replynodetype = (byte) (r.getElement(i));

                        int nodetype;
                        switch (replynodetype) {
                            case 0x00: {
                                nodetype = AcelaNode.AC;  // Should never get this
                                break;
                            }
                            case 0x01: {
                                nodetype = AcelaNode.TB;
                                break;
                            }
                            case 0x02: {
                                nodetype = AcelaNode.D8;
                                break;
                            }
                            case 0x03: {
                                nodetype = AcelaNode.WM;
                                break;
                            }
                            case 0x04: {
                                nodetype = AcelaNode.SM;
                                break;
                            }
                            case 0x05: {
                                nodetype = AcelaNode.SC;
                                break;
                            }
                            case 0x06: {
                                nodetype = AcelaNode.SW;
                                break;
                            }
                            case 0x07: {
                                nodetype = AcelaNode.YM;
                                break;
                            }
                            case 0x08: {
                                nodetype = AcelaNode.SY;
                                break;
                            }
                            default: {
                                nodetype = AcelaNode.UN;  // Should never get this
                            }
                        }
                        int tempaddr = i + 1;
                        new AcelaNode(tempaddr, nodetype, _memo.getTrafficController());
                        log.info("Created a new Acela Node [{}] as a result of Acela network Poll of type: {}", tempaddr, replynodetype);
                    }
                    _memo.getTrafficController().setAcelaTrafficControllerState(true);
                }
            }
        } else {
            int replysize = r.getNumDataElements();
            if (replysize > 1) {  // Bob C: not good if only one sensor module !!
                _memo.getTrafficController().updateSensorsFromPoll(r);
            }
        }
    }

    /**
     * Method to register any orphan Sensors when a new Acela Node is created.
     */
    public void registerSensorsForNode(AcelaNode node) {
        // get list containing all Sensors
        log.info("Trying to register sensor from Manager 2: {}Sxx", getSystemPrefix()); // multichar prefix
        java.util.Iterator<String> iter
                = getSystemNameList().iterator();
        // Iterate through the sensors
        AcelaNode tNode = null;
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName == null) {
                log.error("System Name null during register Sensor");
            } else {
                log.debug("system Name is {}", sName);
                if (sName.startsWith(getSystemPrefix() + "S")) { // multichar prefix
                    // This is an Acela Sensor
                    tNode = AcelaAddress.getNodeFromSystemName(sName, _memo);
                    if (tNode == node) {
                        // This sensor is for this new Acela Node - register it
                        node.registerSensor(getBySystemName(sName),
                                AcelaAddress.getBitFromSystemName(sName, getSystemPrefix()));
                    }
                }
            }
        }
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * Static function returning the AcelaSensorManager instance to use.
     *
     * @return The registered AcelaSensorManager instance for general use, if
     *         need be creating one.
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public AcelaSensorManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaSensorManager.class);

}
