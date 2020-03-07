package jmri.jmrix.easydcc;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Consist definition for a consist on an EasyDCC system. it uses
 * the EasyDcc specific commands to build a consist.
 *
 * @author Paul Bender Copyright (C) 2006
 */
public class EasyDccConsist extends jmri.implementation.DccConsist implements EasyDccListener {

    private EasyDccSystemConnectionMemo _memo = null;

    // Initialize a consist for the specific address.
    // The Default consist type is an advanced consist
    public EasyDccConsist(int address, EasyDccSystemConnectionMemo memo) {
        super(address);
        _memo = memo;
    }

    // Initialize a consist for the specific address.
    // The Default consist type is an advanced consist
    public EasyDccConsist(DccLocoAddress address, EasyDccSystemConnectionMemo memo) {
        super(address);
        _memo = memo;
    }

    // Clean Up local storage.
    @Override
    public void dispose() {
        super.dispose();
    }

    // Set the Consist Type.
    @Override
    public void setConsistType(int consist_type) {
        if (consist_type == Consist.ADVANCED_CONSIST) {
            consistType = consist_type;
            return;
        } else if (consist_type == Consist.CS_CONSIST) {
            consistType = consist_type;
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
        }
    }

    /**
     * Is this address allowed?
     * On EasyDCC systems, all addresses but 0 can be used in a consist
     * (either an Advanced Consist or a Standard Consist).
     */
    @Override
    public boolean isAddressAllowed(DccLocoAddress address) {
        if (address.getNumber() != 0) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Is there a size limit for this consist?
     *
     * @return 8 for EasyDcc Standard Consist,
     * -1 for Decoder Assisted Consists (no limit),
     * 0 for any other consist type
     */
    @Override
    public int sizeLimit() {
        if (consistType == ADVANCED_CONSIST) {
            return -1;
        } else if (consistType == CS_CONSIST) {
            return 8;
        } else {
            return 0;
        }
    }

    /**
     * Does the consist contain the specified address?
     */
    @Override
    public boolean contains(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST || consistType == CS_CONSIST) {
            return consistList.contains(address);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Get the relative direction setting for a specific
     * locomotive in the consist.
     */
    @Override
    public boolean getLocoDirection(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST || consistType == CS_CONSIST) {
            Boolean Direction = consistDir.get(address);
            return (Direction.booleanValue());
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Add an Address to the internal consist list object.
     */
    private synchronized void addToConsistList(DccLocoAddress LocoAddress, boolean directionNormal) {
        Boolean Direction = Boolean.valueOf(directionNormal);
        if (!(consistList.contains(LocoAddress))) {
            consistList.add(LocoAddress);
        }
        consistDir.put(LocoAddress, Direction);
        if (consistType == CS_CONSIST && consistList.size() == 8) {
            notifyConsistListeners(LocoAddress,
                    ConsistListener.OPERATION_SUCCESS
                    | ConsistListener.CONSIST_FULL);
        } else {
            notifyConsistListeners(LocoAddress,
                    ConsistListener.OPERATION_SUCCESS);
        }
    }

    /**
     * Remove an address from the internal consist list object.
     */
    private synchronized void removeFromConsistList(DccLocoAddress LocoAddress) {
        consistDir.remove(LocoAddress);
        consistList.remove(LocoAddress);
        notifyConsistListeners(LocoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /**
     * Add a Locomotive to a Consist.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void add(DccLocoAddress LocoAddress, boolean directionNormal) {
        if (consistType == ADVANCED_CONSIST) {
            addToConsistList(LocoAddress, directionNormal);
            addToAdvancedConsist(LocoAddress, directionNormal);
            //set the value in the roster entry for CV19
            setRosterEntryCVValue(LocoAddress);
        } else if (consistType == CS_CONSIST) {
            if (consistList.size() < 8) {
                addToConsistList(LocoAddress, directionNormal);
                addToCSConsist(LocoAddress, directionNormal);
            } else {
                notifyConsistListeners(LocoAddress,
                        ConsistListener.CONSIST_ERROR
                        | ConsistListener.CONSIST_FULL);
            }
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Restore a Locomotive to an Advanced Consist, but don't write to
     * the command station.  This is used for restoring the consist
     * from a file or adding a consist read from the command station.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void restore(DccLocoAddress LocoAddress, boolean directionNormal) {
        if (consistType == ADVANCED_CONSIST) {
            addToConsistList(LocoAddress, directionNormal);
        } else if (consistType == CS_CONSIST) {
            addToConsistList(LocoAddress, directionNormal);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Remove a Locomotive from this Consist.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    public synchronized void remove(DccLocoAddress LocoAddress) {
        if (consistType == ADVANCED_CONSIST) {
            //reset the value in the roster entry for CV19
            resetRosterEntryCVValue(LocoAddress);
            removeFromAdvancedConsist(LocoAddress);
            removeFromConsistList(LocoAddress);
        } else if (consistType == CS_CONSIST) {
            removeFromCSConsist(LocoAddress);
            removeFromConsistList(LocoAddress);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Add a Locomotive to an Advanced Consist.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    protected synchronized void addToAdvancedConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
        if (log.isDebugEnabled()) {
            log.debug("Add Locomotive "
                    + LocoAddress.toString()
                    + " to advanced consist "
                    + consistAddress.toString()
                    + " With Direction Normal "
                    + directionNormal + ".");
        }
        // create the message and fill it,
        byte[] contents = jmri.NmraPacket.consistControl(LocoAddress.getNumber(),
                LocoAddress.isLongAddress(),
                consistAddress.getNumber(),
                directionNormal);
        EasyDccMessage msg = new EasyDccMessage(4 + 3 * contents.length);
        msg.setOpCode('S');
        msg.setElement(1, ' ');
        msg.setElement(2, '0');
        msg.setElement(3, '5');
        int j = 4;
        for (int i = 0; i < contents.length; i++) {
            msg.setElement(j++, ' ');
            msg.addIntAsTwoHex(contents[i] & 0xFF, j);
            j = j + 2;
        }

        // send it
        _memo.getTrafficController().sendEasyDccMessage(msg, this);
    }

    /**
     * Remove a Locomotive from an Advanced Consist
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    protected synchronized void removeFromAdvancedConsist(DccLocoAddress LocoAddress) {
        if (log.isDebugEnabled()) {
            log.debug(" Remove Locomotive "
                    + LocoAddress.toString()
                    + " from advanced consist "
                    + consistAddress.toString());
        }
        // create the message and fill it,
        byte[] contents = jmri.NmraPacket.consistControl(LocoAddress.getNumber(),
                LocoAddress.isLongAddress(),
                0, true);
        EasyDccMessage msg = new EasyDccMessage(4 + 3 * contents.length);
        msg.setOpCode('S');
        msg.setElement(1, ' ');
        msg.setElement(2, '0');
        msg.setElement(3, '5');
        int j = 4;
        for (int i = 0; i < contents.length; i++) {
            msg.setElement(j++, ' ');
            msg.addIntAsTwoHex(contents[i] & 0xFF, j);
            j = j + 2;
        }

        // send it
        _memo.getTrafficController().sendEasyDccMessage(msg, this);
    }

    /**
     * Add a Locomotive to an EasyDCC Standard Consist.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    private synchronized void addToCSConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
        if (log.isDebugEnabled()) {
            log.debug("Add Locomotive "
                    + LocoAddress.toString()
                    + " to Standard Consist "
                    + consistAddress.toString()
                    + " With Direction Normal "
                    + directionNormal + ".");
        }
        EasyDccMessage m;
        if (directionNormal) {
            m = EasyDccMessage.getAddConsistNormal(consistAddress.getNumber(), LocoAddress);
        } else {
            m = EasyDccMessage.getAddConsistReverse(consistAddress.getNumber(), LocoAddress);
        }
        _memo.getTrafficController().sendEasyDccMessage(m, this);
    }

    /**
     * Remove a Locomotive from an EasyDCC Standard Consist.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     */
    public synchronized void removeFromCSConsist(DccLocoAddress LocoAddress) {
        if (log.isDebugEnabled()) {
            log.debug("Remove Locomotive "
                    + LocoAddress.toString()
                    + " from Standard Consist "
                    + consistAddress.toString()
                    + ".");
        }
        EasyDccMessage m = EasyDccMessage.getSubtractConsist(consistAddress.getNumber(), LocoAddress);
        _memo.getTrafficController().sendEasyDccMessage(m, this);
    }

    /**
     * Listeners for messages from the command station.
     */
    @Override
    public void message(EasyDccMessage m) {
        log.error("message received unexpectedly: {}", m.toString());
    }

    @Override
    public void reply(EasyDccReply r) {
        // There isn't anything meaningful coming back at this time.
        if (log.isDebugEnabled()) {
            log.debug("reply received unexpectedly: {}", r.toString());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccConsist.class);

}
