package jmri.jmrix.acela;

import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Acela-specific Sensor implementation.
 * <P>
 * System names are "ASnnnn", where nnnn is the sensor number without padding.
 * <P>
 * Sensors are numbered from 0.
 * <P>
 * This is a AcelaListener to handle the replies to poll messages. Those are
 * forwarded to the specific AcelaNode object corresponding to their origin for
 * processing of the data.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2003, 2007
 * @author Dave Duchamp, multi node extensions, 2004
 *
 * @author Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaSensorManager extends jmri.managers.AbstractSensorManager
        implements AcelaListener {

    public AcelaSensorManager() {
        super();
    }

    /**
     * Return the Acela system letter
     */
    public String getSystemPrefix() {
        return "A";
    }

    /**
     * Create a new sensor if all checks are passed System name is normalized to
     * ensure uniqueness.
     */
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = AcelaAddress.normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            log.error("Invalid Acela Sensor system name: " + systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            log.error("Sensor with this name already exists: " + systemName);
            return null;
        }
        // check under alternate name
        String altName = AcelaAddress.convertSystemNameToAlternate(sName);
        s = getBySystemName(altName);
        if (s != null) {
            log.error("Sensor with name: '" + systemName + "' already exists as: '" + altName + "'");
            return null;
        }
        // check bit number
        int bit = AcelaAddress.getBitFromSystemName(sName);
        if ((bit < 0) || (bit >= 1023)) {
            log.error("Sensor bit number: " + Integer.toString(bit)
                    + ", is outside the supported range, 1-1024");
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new AcelaSensor(sName);
        } else {
            s = new AcelaSensor(sName, userName);
        }

        // ensure that a corresponding Acela Node exists
        AcelaNode node = AcelaAddress.getNodeFromSystemName(sName);
        if (node == null) {
            log.warn("Sensor: " + sName + ", refers to an undefined Acela Node.");
            return s;
        }
        if (!node.hasActiveSensors) {
            int newnodeaddress;
            newnodeaddress = node.getNodeAddress();
            log.warn("We got the wrong node: " + newnodeaddress);
            return s;
        }
        // register this sensor with the Acela Node
        node.registerSensor(s, bit);
        return s;
    }

    /**
     * Dummy routine
     */
    public void message(AcelaMessage r) {
        log.warn("unexpected message");
    }

    /**
     * Process a reply to a poll of Sensors of one node
     */
    public void reply(AcelaReply r) {
        // Determine which state we are in: Initiallizing Acela Network or Polling Sensors
        boolean currentstate = AcelaTrafficController.instance().getAcelaTrafficControllerState();
        //  Flag to indicate which state we are in: 
        //  false == Initiallizing Acela Network
        //  true == Polling Sensors
        if (!currentstate) {
            int replysize = r.getNumDataElements();
            if (replysize == 0) {
                // The Acela Online command seems to return an empty message
                log.warn("We got an empty reply of size: " + replysize);
            } else {
                if (replysize == 1) {
                    byte replyvalue = (byte) (r.getElement(0));
                    if (replyvalue == 0x00) {
                        //  Everything is OK.
                    } else {  //  Assume this is the response to the pollnodes
                        log.warn("We got a bad return code: " + replyvalue);
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
                        new AcelaNode(tempaddr, nodetype);
                        log.info("Created a new Acela Node [" + tempaddr + "] as a result of Acela network Poll of type: " + replynodetype);
                    }
                    AcelaTrafficController.instance().setAcelaTrafficControllerState(true);
                }
            }
        } else {
            int replysize = r.getNumDataElements();
            if (replysize > 1) {  // Bob C: not good if only one sensor module !!
                AcelaTrafficController.instance().updateSensorsFromPoll(r);
            }
        }
    }

    /**
     * Method to register any orphan Sensors when a new Acela Node is created
     */
    public void registerSensorsForNode(AcelaNode node) {
        // get list containing all Sensors
        log.info("Trying to register sensor from Manager 2: ASxx");
        java.util.Iterator<String> iter
                = getSystemNameList().iterator();
        // Iterate through the sensors
        AcelaNode tNode = null;
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName == null) {
                log.error("System name null during register Sensor");
            } else {
                log.debug("system name is " + sName);
                if ((sName.charAt(0) == 'A') && (sName.charAt(1) == 'S')) {
                    // This is a Acela Sensor
                    tNode = AcelaAddress.getNodeFromSystemName(sName);
                    if (tNode == node) {
                        // This sensor is for this new Acela Node - register it
                        node.registerSensor(getBySystemName(sName),
                                AcelaAddress.getBitFromSystemName(sName));
                    }
                }
            }
        }
    }

    public boolean allowMultipleAdditions() {
        return true;
    }

    /**
     * static function returning the AcelaSensorManager instance to use.
     *
     * @return The registered AcelaSensorManager instance for general use, if
     *         need be creating one.
     */
    static public AcelaSensorManager instance() {
        if (_instance == null) {
            _instance = new AcelaSensorManager();
        }
        return _instance;
    }

    static volatile AcelaSensorManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(AcelaSensorManager.class.getName());
}
