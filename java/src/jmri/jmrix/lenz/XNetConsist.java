package jmri.jmrix.lenz;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;

/**
 * XNetConsist.java
 *
 * This is the Consist definition for a consist on an XPresNet system. it uses
 * the XpressNet specific commands to build a consist.
 *
 * @author Paul Bender Copyright (C) 2004-2010
 */
public class XNetConsist extends jmri.implementation.DccConsist implements XNetListener {

    // We need to wait for replies before completing consist
    // operations
    private static final int IDLESTATE = 0;
    private static final int ADDREQUESTSENTSTATE = 1;
    private static final int REMOVEREQUESTSENTSTATE = 2;

    private int _state = IDLESTATE;

    private DccLocoAddress _locoAddress = null; // address for the last request
    private boolean _directionNormal = false; // direction of the last request

    protected XNetTrafficController tc; // hold the traffic controller associated with this consist.

    /**
     * Initialize a consist for the specific address.
     * Default consist type is an advanced consist.
     * @param address loco address.
     * @param controller system connection traffic controller.
     * @param systemMemo system connection.
     */
    public XNetConsist(int address, XNetTrafficController controller, XNetSystemConnectionMemo systemMemo) {
        super(address);
        tc = controller;
        this.systemMemo = systemMemo;
        // At construction, register for messages
        tc.addXNetListener(XNetInterface.COMMINFO
                | XNetInterface.CONSIST,
                XNetConsist.this);
    }

    /**
     * Initialize a consist for the specific address.
     * Default consist type is an advanced consist.
     * @param address loco address.
     * @param controller system connection traffic controller.
     * @param systemMemo system connection.
     */
    public XNetConsist(DccLocoAddress address, XNetTrafficController controller, XNetSystemConnectionMemo systemMemo) {
        super(address);
        tc = controller;
        this.systemMemo = systemMemo;
        // At construction, register for messages
        tc.addXNetListener(XNetInterface.COMMINFO
                | XNetInterface.CONSIST,
                XNetConsist.this);
    }

    final XNetSystemConnectionMemo systemMemo;

    /**
     * Clean Up local storage, and remove the XNetListener.
     */
    @Override
    public synchronized void dispose() {
        super.dispose();
        tc.removeXNetListener(
                XNetInterface.COMMINFO
                | XNetInterface.CONSIST,
                this);
    }

    /**
     * Set the Consist Type.
     *
     * @param consistType An integer, should be either
     *                     jmri.Consist.ADVANCED_CONSIST or
     *                     jmri.Consist.CS_CONSIST.
     */
    @Override
    public void setConsistType(int consistType) {
        switch (consistType) {
            case Consist.ADVANCED_CONSIST:
            case Consist.CS_CONSIST:
                this.consistType = consistType;
                break;
            default:
                log.error("Consist Type Not Supported");
                notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
                break;
        }
    }

    /**
     * Is this address allowed?
     * <p>
     * On Lenz systems, All addresses but 0 can be used in a consist (Either and
     * Advanced Consist or a Double Header).
     * {@inheritDoc}
     * @param address {@link jmri.DccLocoAddress DccLocoAddress} object to
     *                check.
     */
    @Override
    public boolean isAddressAllowed(DccLocoAddress address) {
        return address.getNumber() != 0;
    }

    /**
     * Is there a size limit for this consist?
     *
     * @return 2 For Lenz double headers. -1 (no limit) For Decoder Assisted
     *         Consists. 0 for any other consist type.
     */
    @Override
    public int sizeLimit() {
        switch (consistType) {
            case ADVANCED_CONSIST:
                return -1;
            case CS_CONSIST:
                return 2;
            default:
                return 0;
        }
    }

    /**
     * Does the consist contain the specified address?
     * {@inheritDoc}
     * @param address {@link jmri.DccLocoAddress DccLocoAddress} object to
     *                check.
     */
    @Override
    public boolean contains(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST || consistType == CS_CONSIST) {
            return (consistList.contains(address));
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Get the relative direction setting for a specific locomotive in the
     * consist.
     *
     * @param address {@link jmri.DccLocoAddress DccLocoAddress} object to check
     * @return true means forward, false means backwards.
     */
    @Override
    public boolean getLocoDirection(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST || consistType == CS_CONSIST) {
            return consistDir.get(address);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Add an Address to the internal consist list object.
     *
     * @param locoAddress     {@link jmri.DccLocoAddress address} of the
     *                        locomotive to add.
     * @param directionNormal true for normal direction, false for reverse.
     */
    private synchronized void addToConsistList(DccLocoAddress locoAddress, boolean directionNormal) {
        if (!(consistList.contains(locoAddress))) {
            consistList.add(locoAddress);
        }
        consistDir.put(locoAddress, directionNormal);
        if (consistType == CS_CONSIST && consistList.size() == 2) {
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
     *
     * @param locoAddress {@link jmri.DccLocoAddress address} of the locomotive
     *                    to remove.
     */
    private synchronized void removeFromConsistList(DccLocoAddress locoAddress) {
        if (consistList.contains(locoAddress)) {
            consistDir.remove(locoAddress);
            consistList.remove(locoAddress);
        }
        notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /**
     * Add a Locomotive to a Consist.
     *
     * @param locoAddress     the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling the same
     *                        direction as the consist, or false otherwise
     */
    @Override
    public synchronized void add(DccLocoAddress locoAddress, boolean directionNormal) {
        switch (consistType) {
            case ADVANCED_CONSIST:
                addToAdvancedConsist(locoAddress, directionNormal);
                // save the address for the check after we get a response
                // from the command station
                _locoAddress = locoAddress;
                _directionNormal = directionNormal;
                break;
            case CS_CONSIST:
                if (consistList.size() < 2) {
                    // Lenz Double Headers require exactly 2 locomotives, so
                    // wait for the second locomotive to be added to start
                    if (consistList.size() == 1 && !consistList.contains(locoAddress)) {
                        addToCSConsist(locoAddress, directionNormal);
                        // save the address for the check after we get a response
                        // from the command station
                        _locoAddress = locoAddress;
                        _directionNormal = directionNormal;
                    } else if (consistList.size() < 1) {
                        // we're going to just add this directly, since we
                        // can't form the consist yet.
                        addToConsistList(locoAddress, directionNormal);
                    } else {
                        // we must have gotten here because we tried to add
                        // a locomotive already in this consist.
                        notifyConsistListeners(locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.ALREADY_CONSISTED);
                    }
                } else {
                    // The only way it is valid for us to do something
                    // here is if the locomotive we're adding is
                    // already in the consist and we want to change
                    // its direction
                    if (consistList.size() == 2
                            && consistList.contains(locoAddress)) {
                        addToCSConsist(locoAddress, directionNormal);
                        // save the address for the check after we get aresponse
                        // from the command station
                        _locoAddress = locoAddress;
                        _directionNormal = directionNormal;
                    } else {
                        notifyConsistListeners(locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.CONSIST_FULL);
                    }
                }
                break;
            default:
                log.error("Consist Type Not Supported");
                notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
                break;
        }
    }

    /**
     * Restore a Locomotive to an Advanced Consist, but don't write to the
     * command station.
     * <p>
     * This is used for restoring the consist from a file or adding a consist
     * read from the command station.
     *
     * @param locoAddress     the Locomotive address to add to the locomotive
     * @param directionNormal True if the locomotive is traveling the same
     *                        direction as the Consist, or false otherwise.
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
     * @param locoAddress the Locomotive address to add to the Consist
     */
    @Override
    public synchronized void remove(DccLocoAddress locoAddress) {
        log.debug("Consist {}: remove called for address {}", consistAddress, locoAddress);
        switch (consistType) {
            case ADVANCED_CONSIST:
                // save the address for the check after we get a response
                // from the command station
                _locoAddress = locoAddress;
                removeFromAdvancedConsist(locoAddress);
                break;
            case CS_CONSIST:
                // Lenz Double Headers must be formed with EXACTLY 2
                // addresses, so if there are two addresses in the list,
                // we'll actually send the commands to remove the consist
                if (consistList.size() == 2
                        && _state != REMOVEREQUESTSENTSTATE) {
                    // save the address for the check after we get a response
                    // from the command station
                    _locoAddress = locoAddress;
                    removeFromCSConsist(locoAddress);
                } else {
                    // we just want to remove this from the list.
                    if (_state != REMOVEREQUESTSENTSTATE
                            || _locoAddress != locoAddress) {
                        removeFromConsistList(locoAddress);
                    }
                }
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
     * @param locoAddress     the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling the same
     *                        direction as the consist, or false otherwise.
     */
    @Override
    protected synchronized void addToAdvancedConsist(DccLocoAddress locoAddress, boolean directionNormal) {
        log.debug("Adding locomotive {} to consist {}", locoAddress.getNumber(), consistAddress.getNumber());
        // First, check to see if the locomotive is in the consist already
        if (this.contains(locoAddress)) {
            // we want to remove the locomotive from the consist
            // before we re-add it. (we might just be switching
            // the direction of the locomotive in the consist)
            removeFromAdvancedConsist(locoAddress);
        }
        // set the speed of the locomotive to zero, to make sure we have
        // control over it.
        sendDirection(locoAddress, directionNormal);

        // All we have to do here is create an apropriate XNetMessage,
        // and send it.
        XNetMessage msg = XNetMessage.getAddLocoToConsistMsg(consistAddress.getNumber(), 
            locoAddress.getNumber(), directionNormal);
        tc.sendXNetMessage(msg, this);
        _state = ADDREQUESTSENTSTATE;
    }

    /**
     * Remove a Locomotive from an Advanced Consist.
     *
     * @param locoAddress the Locomotive address to add to the locomotive
     */
    @Override
    protected synchronized void removeFromAdvancedConsist(DccLocoAddress locoAddress) {
        // set the speed of the locomotive to zero, to make sure we
        // have control over it.
        sendDirection(locoAddress, getLocoDirection(locoAddress));
        // All we have to do here is create an apropriate XNetMessage,
        // and send it.
        XNetMessage msg = XNetMessage.getRemoveLocoFromConsistMsg(consistAddress.getNumber(), locoAddress.getNumber());
        tc.sendXNetMessage(msg, this);
        _state = REMOVEREQUESTSENTSTATE;
    }

    /**
     * Add a Locomotive to a Lenz Double Header
     *
     * @param locoAddress     the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling the same
     *                        direction as the consist, or false otherwise.
     */
    private synchronized void addToCSConsist(DccLocoAddress locoAddress, boolean directionNormal) {

        if (consistAddress.equals(locoAddress)) {
            // Something went wrong here, we are trying to add a
            // trailing locomotive to the consist with the same
            // address as the lead locomotive.  This isn't supposed to
            // happen.
            log.error("Attempted to add {} to consist {}", locoAddress, consistAddress);
            _state = IDLESTATE;
            notifyConsistListeners(_locoAddress,
                    ConsistListener.CONSIST_ERROR
                    | ConsistListener.ALREADY_CONSISTED);
            return;
        }

        // If the consist already contains the locomotive in
        // question, we need to disolve the consist
        if (consistList.size() == 2
                && consistList.contains(locoAddress)) {
            XNetMessage msg = XNetMessage.getDisolveDoubleHeaderMsg(
                    consistList.get(0).getNumber());
            tc.sendXNetMessage(msg, this);
        }

        // We need to set the speed and direction of both
        // locomotives to establish control.
        DccLocoAddress address = consistList.get(0);
        Boolean direction = consistDir.get(address);
        sendDirection(address, direction);
        sendDirection(locoAddress, directionNormal);

        // All we have to do here is create an apropriate XNetMessage,
        // and send it.
        XNetMessage msg = XNetMessage.getBuildDoubleHeaderMsg(address.getNumber(), locoAddress.getNumber());
        tc.sendXNetMessage(msg, this);
        _state = ADDREQUESTSENTSTATE;

    }

    /**
     * Remove a Locomotive from a Lenz Double Header.locoAddress
     * @param locoAddress is the Locomotive address, unused here.
     */
    public synchronized void removeFromCSConsist(DccLocoAddress locoAddress) {
        // All we have to do here is create an apropriate XNetMessage,
        // and send it.
        XNetMessage msg = XNetMessage.getDisolveDoubleHeaderMsg(consistList.get(0).getNumber());
        tc.sendXNetMessage(msg, this);
        _state = REMOVEREQUESTSENTSTATE;
    }

    /**
     * Listeners for messages from the command station.
     */
    @Override
    public synchronized void message(XNetReply l) {
        if (_state != IDLESTATE) {
            // we're waiting for a reply, so examine what we received
            if (l.isOkMessage()) {
                if (_state == ADDREQUESTSENTSTATE) {
                    addToConsistList(_locoAddress, _directionNormal);
                    if (consistType == ADVANCED_CONSIST) {
                       //set the value in the roster entry for CV19
                       setRosterEntryCVValue(_locoAddress);
                    }
                } else if (_state == REMOVEREQUESTSENTSTATE) {
                    if (consistType == ADVANCED_CONSIST) {
                       //reset the value in the roster entry for CV19
                       resetRosterEntryCVValue(_locoAddress);
                    }
                    removeFromConsistList(_locoAddress);
                }
                _state = IDLESTATE;
            } else if (l.getElement(0) == XNetConstants.LOCO_MU_DH_ERROR) {
                String text;
                switch (l.getElement(1)) {
                    case 0x81:
                        text = "Selected Locomotive has not been operated by this XpressNet device or address 0 selected";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.LOCO_NOT_OPERATED);
                        break;
                    case 0x82:
                        text = "Selected Locomotive is being operated by another XpressNet device";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.LOCO_NOT_OPERATED);
                        break;
                    case 0x83:
                        text = "Selected Locomotive already in MU or DH";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.ALREADY_CONSISTED);
                        break;
                    case 0x84:
                        text = "Unit selected for MU or DH has speed setting other than 0";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.NONZERO_SPEED);
                        break;
                    case 0x85:
                        text = "Locomotive not in a MU";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.NOT_CONSISTED);
                        break;
                    case 0x86:
                        text = "Locomotive address not a multi-unit base address";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.NOT_CONSIST_ADDR);

                        break;
                    case 0x87:
                        text = "It is not possible to delete the locomotive";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.DELETE_ERROR);
                        break;
                    case 0x88:
                        text = "The Command Station Stack is Full";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.STACK_FULL);
                        break;
                    default:
                        text = "Unknown";
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR);
                }
                log.error("XpressNet MU+DH error: {}",text);
            }
        }
    }

    @Override
    public void message(XNetMessage l) {
    }

   // Handle a timeout notification
    @Override
    public void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message{}", msg);
    }

    /**
     * Set the speed and direction of a locomotive; bypassing the commands in
     * the throttle, since they don't work for this application.
     * <p>
     * For this application, we also set the speed setting to 0, which also
     * establishes control over the locomotive in the consist.
     *
     * @param address   the DccLocoAddress of the locomotive.
     * @param isForward the boolean value representing the desired direction
     */
    private void sendDirection(DccLocoAddress address, boolean isForward) {
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(address.getNumber(),
                jmri.SpeedStepMode.NMRA_DCC_28,
                (float) 0.0,
                isForward);
        // now, we send the message to the command station
        tc.sendXNetMessage(msg, this);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XNetConsist.class);

}
