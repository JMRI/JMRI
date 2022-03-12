package jmri.jmrix.acela;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Acela-specific Sensor implementation.
 * <p>
 * System names are "ASnnnn", where A is the user configurable system prefix,
 * nnnn is the sensor number without padding.
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

    public AcelaSensorManager(AcelaSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public AcelaSystemConnectionMemo getMemo() {
        return (AcelaSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     * <p>
     * System name is normalized to ensure uniqueness.
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        // Validate the systemName
        if (AcelaAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()) == NameValidity.INVALID) {
            log.error("Invalid Sensor system Name format: {}", systemName);
            throw new IllegalArgumentException("Invalid Sensor System Name format: " + systemName);
        }
        Sensor s;
        String sName = systemName;
        if (sName.isEmpty()) {
            // system name is not valid
            throw new IllegalArgumentException("Invalid Acela Sensor system name - " +  // NOI18N
                    systemName);
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            throw new IllegalArgumentException("Acela Sensor with this name already exists - " +  // NOI18N
                    systemName);
        }
        // check under alternate name
        String altName = AcelaAddress.convertSystemNameToAlternate(sName, getSystemPrefix());
        s = getBySystemName(altName);
        if (s != null) {
            throw new IllegalArgumentException("Acela Sensor with name  " +  // NOI18N
                    systemName + " already exists as " + altName);
        }
        // check bit number
        int bit = AcelaAddress.getBitFromSystemName(sName, getSystemPrefix());
        if ((bit < AcelaAddress.MINSENSORADDRESS) || (bit > AcelaAddress.MAXSENSORADDRESS)) {
            log.error("Sensor bit number {} is outside the supported range {}-{}", bit, AcelaAddress.MINSENSORADDRESS, AcelaAddress.MAXSENSORADDRESS);
            throw new IllegalArgumentException("Sensor bit number " +  // NOI18N
                    Integer.toString(bit) + " is outside the supported range " + // NOI18N
                    Integer.toString(AcelaAddress.MAXSENSORADDRESS) + "-" +
                    Integer.toString(AcelaAddress.MAXSENSORADDRESS));
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new AcelaSensor(sName);
        } else {
            s = new AcelaSensor(sName, userName);
        }

        // ensure that a corresponding Acela Node exists
        AcelaNode node = AcelaAddress.getNodeFromSystemName(sName, getMemo());
        if (node == null) {
            log.warn("Sensor: {} refers to an undefined Acela Node.", sName);
            return s;
        }
        if (!node.hasActiveSensors) {
            int newNodeAddress;
            newNodeAddress = node.getNodeAddress();
            log.warn("We got the wrong node: {}", newNodeAddress);
            return s;
        }
        // register this sensor with the Acela Node
        node.registerSensor(s, bit);
        return s;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Verifies system name has valid prefix and is an integer from
     * {@value AcelaAddress#MINSENSORADDRESS} to
     * {@value AcelaAddress#MAXSENSORADDRESS}.
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        return super.validateIntegerSystemNameFormat(systemName,
                AcelaAddress.MINSENSORADDRESS,
                AcelaAddress.MAXSENSORADDRESS,
                locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (AcelaAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()));
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
        boolean currentstate = getMemo().getTrafficController().getAcelaTrafficControllerState();
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
                        new AcelaNode(tempaddr, nodetype, getMemo().getTrafficController());
                        log.info("Created a new Acela Node [{}] as a result of Acela network Poll of type: {}", tempaddr, replynodetype);
                    }
                    getMemo().getTrafficController().setAcelaTrafficControllerState(true);
                }
            }
        } else {
            int replysize = r.getNumDataElements();
            if (replysize > 1) {  // Bob C: not good if only one sensor module !!
                getMemo().getTrafficController().updateSensorsFromPoll(r);
            }
        }
    }

    /**
     * Register any orphan Sensors when a new Acela Node is created.
     * @param node which node to search for sensors.
     */
    public void registerSensorsForNode(AcelaNode node) {
        // get list containing all Sensors
        log.info("Trying to register sensor from Manager 2: {}Sxx", getSystemPrefix()); // multichar prefix
        // Iterate through the sensors
        AcelaNode tNode;
        for (Sensor s : getNamedBeanSet()) {
            String sName = s.getSystemName();
            log.debug("system Name is {}", sName);
            if (sName.startsWith(getSystemNamePrefix())) { // multichar prefix
                // This is an Acela Sensor
                tNode = AcelaAddress.getNodeFromSystemName(sName, getMemo());
                if (tNode == node) {
                    // This sensor is for this new Acela Node - register it
                    node.registerSensor(s,
                            AcelaAddress.getBitFromSystemName(sName, getSystemPrefix()));
                }
            }
        }
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaSensorManager.class);

}
