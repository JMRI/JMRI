package jmri.jmrix.easydcc;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;

/**
 * This is the Consist definition for a consist on an EasyDCC system. it uses
 * the EasyDcc specific commands to build a consist.
 *
 * @author Paul Bender Copyright (C) 2006
 */
public class EasyDccConsist extends jmri.implementation.DccConsist {

    private final EasyDccSystemConnectionMemo _memo;

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

    // Set the Consist Type.
    @Override
    public void setConsistType(int consist_type) {
        switch (consist_type) {
            case Consist.ADVANCED_CONSIST:
            case Consist.CS_CONSIST:
                consistType = consist_type;
                break;
            default:
                log.error("Consist Type Not Supported");
                notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
                break;
        }
    }

    /**
     * Is this address allowed?
     * On EasyDCC systems, all addresses but 0 can be used in a consist
     * (either an Advanced Consist or a Standard Consist).
     * {@inheritDoc}
     */
    @Override
    public boolean isAddressAllowed(DccLocoAddress address) {
        return address.getNumber() != 0;
    }

    /**
     * Is there a size limit for this consist?
     *
     * @return 8 for EasyDcc Standard Consist,
     * -1 for Decoder Assisted Consists (no limit),
     * 0 for any other consist type
     * {@inheritDoc}
     */
    @Override
    public int sizeLimit() {
        switch (consistType) {
            case ADVANCED_CONSIST:
                return -1;
            case CS_CONSIST:
                return 8;
            default:
                return 0;
        }
    }

    /**
     * Does the consist contain the specified address?
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public boolean getLocoDirection(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST || consistType == CS_CONSIST) {
            return consistDir.getOrDefault(address, false);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Add an Address to the internal consist list object.
     */
    private synchronized void addToConsistList(DccLocoAddress locoAddress, boolean directionNormal) {
        if (!(consistList.contains(locoAddress))) {
            consistList.add(locoAddress);
        }
        consistDir.put(locoAddress, directionNormal);
        if (consistType == CS_CONSIST && consistList.size() == 8) {
            notifyConsistListeners(locoAddress,
                    ConsistListener.OPERATION_SUCCESS
                    | ConsistListener.CONSIST_FULL);
        } else {
            notifyConsistListeners(locoAddress,
                    ConsistListener.OPERATION_SUCCESS);
        }
    }

    /**
     * Remove an address from the internal consist list object.
     */
    private synchronized void removeFromConsistList(DccLocoAddress locoAddress) {
        consistDir.remove(locoAddress);
        consistList.remove(locoAddress);
        notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /**
     * Add a Locomotive to a Consist.
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void add(DccLocoAddress locoAddress, boolean directionNormal) {
        if (consistType == ADVANCED_CONSIST) {
            addToConsistList(locoAddress, directionNormal);
            addToAdvancedConsist(locoAddress, directionNormal);
            //set the value in the roster entry for CV19
            setRosterEntryCVValue(locoAddress);
        } else if (consistType == CS_CONSIST) {
            if (consistList.size() < 8) {
                addToConsistList(locoAddress, directionNormal);
                addToCSConsist(locoAddress, directionNormal);
            } else {
                notifyConsistListeners(locoAddress,
                        ConsistListener.CONSIST_ERROR
                        | ConsistListener.CONSIST_FULL);
            }
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Restore a Locomotive to an Advanced Consist, but don't write to
     * the command station.  This is used for restoring the consist
     * from a file or adding a consist read from the command station.
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void restore(DccLocoAddress locoAddress, boolean directionNormal) {
        switch (consistType) {
            case ADVANCED_CONSIST:
            case CS_CONSIST:
                addToConsistList(locoAddress, directionNormal);
                break;
            default:
                log.error("Consist Type Not Supported");
                notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
                break;
        }
    }

    /**
     * Remove a Locomotive from this Consist.
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    public synchronized void remove(DccLocoAddress locoAddress) {
        switch (consistType) {
            case ADVANCED_CONSIST:
                //reset the value in the roster entry for CV19
                resetRosterEntryCVValue(locoAddress);
                removeFromAdvancedConsist(locoAddress);
                removeFromConsistList(locoAddress);
                break;
            case CS_CONSIST:
                removeFromCSConsist(locoAddress);
                removeFromConsistList(locoAddress);
                break;
            default:
                log.error("Consist Type Not Supported");
                notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
                break;
        }
    }

    /**
     * Add a Locomotive to an Advanced Consist.
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    protected synchronized void addToAdvancedConsist(DccLocoAddress locoAddress, boolean directionNormal) {
        log.debug("Add Locomotive {} to advanced consist {} With Direction Normal {}.", 
            locoAddress, consistAddress, directionNormal);
        // create the message and fill it,
        byte[] contents = jmri.NmraPacket.consistControl(locoAddress.getNumber(),
                locoAddress.isLongAddress(),
                consistAddress.getNumber(),
                directionNormal);
        // FIELD REPORT (Andrew Deak): consistControl() returns null instead
        // of throwing whenever either address fails its own validity check
        // -- confirmed live: an Advanced Consist address above the short-
        // address range (consist addresses are always validated as short,
        // regardless of the locomotive's own long/short form) returned null
        // here, and the unguarded contents.length below NPE'd. That NPE
        // propagated all the way up through the JSON WebSocket handler and
        // closed the client's entire connection (confirmed: JMRI logged
        // "Unanticipated error" and sent a close frame) -- one bad request
        // took down the whole session, not just that operation. Every other
        // failure path in this class reports CONSIST_ERROR instead.
        if (contents == null) {
            log.error("Unable to build advanced consist control packet for {} / consist {} -- invalid address",
                    locoAddress, consistAddress);
            notifyConsistListeners(locoAddress, ConsistListener.CONSIST_ERROR);
            return;
        }
        EasyDccMessage msg = new EasyDccMessage(4 + 3 * contents.length);
        msg.setOpCode('S');
        msg.setElement(1, ' ');
        msg.setElement(2, '0');
        msg.setElement(3, '5');
        int j = 4;
        for (int i = 0; i < contents.length; i++) {
            // FIELD REPORT (Andrew Deak, tested against real EasyDCC
            // hardware): the buffer is sized 3 chars per content byte
            // (" XX" -- space then two hex digits), but this used to
            // pre-increment j before writing the space, then write the
            // hex digits AT THE SAME j -- immediately overwriting the
            // space it just wrote, and (because of the pre-increment)
            // leaving the position before it never written to at all.
            // The actual message sent to the command station ended up
            // with garbage/uninitialized bytes interspersed with the
            // hex fields instead of clean " XX XX XX..." formatting.
            msg.setElement(j, ' ');
            msg.addIntAsTwoHex(contents[i] & 0xFF, j + 1);
            j += 3;
        }

        // send it
        _memo.getTrafficController().sendEasyDccMessage(msg, null);
    }

    /**
     * Remove a Locomotive from an Advanced Consist
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    protected synchronized void removeFromAdvancedConsist(DccLocoAddress locoAddress) {
        log.debug(" Remove Locomotive {} from advanced consist {}", locoAddress, consistAddress);
        // create the message and fill it,
        byte[] contents = jmri.NmraPacket.consistControl(locoAddress.getNumber(),
                locoAddress.isLongAddress(),
                0, true);
        // FIELD REPORT (Andrew Deak): see the matching null-check in
        // addToAdvancedConsist() -- same NPE-into-dropped-connection risk
        // if locoAddress itself is invalid.
        if (contents == null) {
            log.error("Unable to build advanced consist control packet to remove {} -- invalid address", locoAddress);
            notifyConsistListeners(locoAddress, ConsistListener.CONSIST_ERROR);
            return;
        }
        EasyDccMessage msg = new EasyDccMessage(4 + 3 * contents.length);
        msg.setOpCode('S');
        msg.setElement(1, ' ');
        msg.setElement(2, '0');
        msg.setElement(3, '5');
        int j = 4;
        for (int i = 0; i < contents.length; i++) {
            // FIELD REPORT (Andrew Deak, tested against real EasyDCC
            // hardware): the buffer is sized 3 chars per content byte
            // (" XX" -- space then two hex digits), but this used to
            // pre-increment j before writing the space, then write the
            // hex digits AT THE SAME j -- immediately overwriting the
            // space it just wrote, and (because of the pre-increment)
            // leaving the position before it never written to at all.
            // The actual message sent to the command station ended up
            // with garbage/uninitialized bytes interspersed with the
            // hex fields instead of clean " XX XX XX..." formatting.
            msg.setElement(j, ' ');
            msg.addIntAsTwoHex(contents[i] & 0xFF, j + 1);
            j += 3;
        }

        // send it
        _memo.getTrafficController().sendEasyDccMessage(msg, null);
    }

    /**
     * Add a Locomotive to an EasyDCC Standard Consist.
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    private synchronized void addToCSConsist(DccLocoAddress locoAddress, boolean directionNormal) {
        log.debug("Add Locomotive {} to Standard Consist {} With Direction Normal {}.", 
            locoAddress, consistAddress, directionNormal);
        EasyDccMessage m;
        if (directionNormal) {
            m = EasyDccMessage.getAddConsistNormal(consistAddress.getNumber(), locoAddress);
        } else {
            m = EasyDccMessage.getAddConsistReverse(consistAddress.getNumber(), locoAddress);
        }
        _memo.getTrafficController().sendEasyDccMessage(m, null);
    }

    /**
     * Remove a Locomotive from an EasyDCC Standard Consist.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     */
    public synchronized void removeFromCSConsist(DccLocoAddress LocoAddress) {
        log.debug("Remove Locomotive {} from Standard Consist {}.", LocoAddress, consistAddress);
        EasyDccMessage m = EasyDccMessage.getSubtractConsist(consistAddress.getNumber(), LocoAddress);
        _memo.getTrafficController().sendEasyDccMessage(m, null);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EasyDccConsist.class);

}
