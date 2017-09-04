/**
 * XNetConsist.java
 *
 * This is the Consist definition for a consist on an XPresNet system. it uses
 * the XpressNet specific commands to build a consist.
 *
 * @author Paul Bender Copyright (C) 2004-2010
 */
package jmri.jmrix.lenz;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XNetConsist extends jmri.implementation.DccConsist implements XNetListener {

    // We need to wait for replies before completing consist 
    // operations
    private final int IDLESTATE = 0;
    private final int ADDREQUESTSENTSTATE = 1;
    private final int REMOVEREQUESTSENTSTATE = 2;

    private int _state = IDLESTATE;

    private DccLocoAddress _locoAddress = null; // address for the last request
    private boolean _directionNormal = false; // direction of the last request

    protected XNetTrafficController tc = null; // hold the traffic controller associated with this consist.

    /**
     * Initialize a consist for the specific address.
     * Default consist type is an advanced consist.
     */
    public XNetConsist(int address, XNetTrafficController controller, XNetSystemConnectionMemo systemMemo) {
        super(address);
        tc = controller;
        this.systemMemo = systemMemo;
        // At construction, register for messages
        tc.addXNetListener(XNetInterface.COMMINFO
                | XNetInterface.CONSIST,
                this);
    }

    /**
     * Initialize a consist for the specific address.
     * Default consist type is an advanced consist.
     */
    public XNetConsist(DccLocoAddress address, XNetTrafficController controller, XNetSystemConnectionMemo systemMemo) {
        super(address);
        tc = controller;
        this.systemMemo = systemMemo;
        // At construction, register for messages
        tc.addXNetListener(XNetInterface.COMMINFO
                | XNetInterface.CONSIST,
                this);
    }

    XNetSystemConnectionMemo systemMemo;

    /**
     * Clean Up local storage, and remove the XNetListener.
     */
    @Override
    synchronized public void dispose() {
        super.dispose();
        tc.removeXNetListener(
                XNetInterface.COMMINFO
                | XNetInterface.CONSIST,
                this);
    }

    /**
     * Set the Consist Type.
     *
     * @param consist_type, an integer, should be either 
     * jmri.Consist.ADVANCED_CONSIST or jmri.Consist.CS_CONSIST.
     */
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
     * <p>
     * On Lenz systems, All addresses but 0 can be used in a consist 
     * (Either and Advanced Consist or a Double Header).
     *
     * @param address {@link jmri.DccLocoAddress DccLocoAddress} object to check.
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
     * @return 2 For Lenz double headers. -1 (no limit) For 
     *         Decoder Assisted Consists. 0 for any other consist type.
     */
    @Override
    public int sizeLimit() {
        if (consistType == ADVANCED_CONSIST) {
            return -1;
        } else if (consistType == CS_CONSIST) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * Does the consist contain the specified address?
     *
     * @param address {@link jmri.DccLocoAddress DccLocoAddress} object to check.
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
     * Get the relative direction setting for a specific
     * locomotive in the consist.
     *
     * @param address {@link jmri.DccLocoAddress DccLocoAddress} object to check
     * @return true means forward, false means backwards.
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
     *
     * @param LocoAddress {@link jmri.DccLocoAddress address} of the locomotive to add.
     * @param directionNormal true for normal direction, false for reverse.
     */
    private synchronized void addToConsistList(DccLocoAddress LocoAddress, boolean directionNormal) {
        Boolean Direction = Boolean.valueOf(directionNormal);
        if (!(consistList.contains(LocoAddress))) {
            consistList.add(LocoAddress);
        }
        consistDir.put(LocoAddress, Direction);
        if (consistType == CS_CONSIST && consistList.size() == 2) {
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
     *
     * @param LocoAddress {@link jmri.DccLocoAddress address} of the locomotive to remove.
     */
    private synchronized void removeFromConsistList(DccLocoAddress LocoAddress) {
        if(consistList.contains(LocoAddress)) {
           consistDir.remove(LocoAddress);
           consistList.remove(LocoAddress);
        }
        notifyConsistListeners(LocoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /**
     * Add a Locomotive to a Consist.
     *
     * @param LocoAddress the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise
     */
    @Override
    public synchronized void add(DccLocoAddress LocoAddress, boolean directionNormal) {
        if (consistType == ADVANCED_CONSIST) {
            addToAdvancedConsist(LocoAddress, directionNormal);
            // save the address for the check after we get a response 
            // from the command station
            _locoAddress = LocoAddress;
            _directionNormal = directionNormal;
        } else if (consistType == CS_CONSIST) {
            if (consistList.size() < 2) {
                // Lenz Double Headers require exactly 2 locomotives, so 
                // wait for the second locomotive to be added to start
                if (consistList.size() == 1 && !consistList.contains(LocoAddress)) {
                    addToCSConsist(LocoAddress, directionNormal);
                    // save the address for the check after we get a response 
                    // from the command station
                    _locoAddress = LocoAddress;
                    _directionNormal = directionNormal;
                } else if (consistList.size() < 1) {
                    // we're going to just add this directly, since we 
                    // can't form the consist yet.
                    addToConsistList(LocoAddress, directionNormal);
                } else {
                    // we must have gotten here because we tried to add
                    // a locomotive already in this consist.
                    notifyConsistListeners(LocoAddress,
                            ConsistListener.CONSIST_ERROR
                            | ConsistListener.ALREADY_CONSISTED);
                }
            } else {
                // The only way it is valid for us to do something
                // here is if the locomotive we're adding is
                // already in the consist and we want to change
                // it's direction
                if (consistList.size() == 2
                        && consistList.contains(LocoAddress)) {
                    addToCSConsist(LocoAddress, directionNormal);
                    // save the address for the check after we get aresponse
                    // from the command station
                    _locoAddress = LocoAddress;
                    _directionNormal = directionNormal;
                } else {
                    notifyConsistListeners(LocoAddress,
                            ConsistListener.CONSIST_ERROR
                            | ConsistListener.CONSIST_FULL);
                }
            }
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Restore a Locomotive to an Advanced Consist, but don't write to
     * the command station.
     * <p>
     * This is used for restoring the consist from a file or adding a
     * consist read from the command station.
     *
     *  @param LocoAddress the Locomotive address to add to the locomotive
     *  @param directionNormal True if the locomotive is traveling
     *        the same direction as the Consist, or false otherwise.
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
     *  Remove a Locomotive from this Consist.
     *
     *  @param LocoAddress the Locomotive address to add to the Consist
     */
    @Override
    public synchronized void remove(DccLocoAddress LocoAddress) {
        log.debug("Consist {}: remove called for address {}",consistAddress, LocoAddress );
        if (consistType == ADVANCED_CONSIST) {
            // save the address for the check after we get a response 
            // from the command station
            _locoAddress = LocoAddress;
            removeFromAdvancedConsist(LocoAddress);
        } else if (consistType == CS_CONSIST) {
            // Lenz Double Headers must be formed with EXACTLY 2 
            // addresses, so if there are two addresses in the list, 
            // we'll actually send the commands to remove the consist
            if (consistList.size() == 2
                    && _state != REMOVEREQUESTSENTSTATE) {
                // save the address for the check after we get a response 
                // from the command station
                _locoAddress = LocoAddress;
                removeFromCSConsist(LocoAddress);
            } else {
                // we just want to remove this from the list.
                if (_state != REMOVEREQUESTSENTSTATE
                        || _locoAddress != LocoAddress) {
                    removeFromConsistList(LocoAddress);
                }
            }
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     *  Add a Locomotive to an Advanced Consist.
     *
     *  @param LocoAddress the Locomotive address to add to the locomotive
     *  @param directionNormal is True if the locomotive is traveling 
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    protected synchronized void addToAdvancedConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
        if (log.isDebugEnabled()) {
            log.debug("Adding locomotive " + LocoAddress.getNumber() + " to consist " + consistAddress.getNumber());
        }
        // First, check to see if the locomotive is in the consist already
        if (this.contains(LocoAddress)) {
            // we want to remove the locomotive from the consist 
            // before we re-add it. (we might just be switching
            // the direction of the locomotive in the consist)
            removeFromAdvancedConsist(LocoAddress);
            /*while(_state!=IDLESTATE) {
             try {
             wait(1000);
             }
             } catch (java.lang.InterruptedException e) {
             Thread.currentThread().interrupt(); // retain if needed later
             }
             }*/
        }
        // set the speed of the locomotive to zero, to make sure we have
        // control over it.
        sendDirection(LocoAddress, directionNormal);

        // All we have to do here is create an apropriate XNetMessage, 
        // and send it.
        XNetMessage msg = XNetMessage.getAddLocoToConsistMsg(consistAddress.getNumber(), LocoAddress.getNumber(), directionNormal);
        tc.sendXNetMessage(msg, this);
        _state = ADDREQUESTSENTSTATE;
    }

    /**
     *  Remove a Locomotive from an Advanced Consist.
     *
     *  @param LocoAddress the Locomotive address to add to the locomotive
     */
    @Override
    protected synchronized void removeFromAdvancedConsist(DccLocoAddress LocoAddress) {
        // set the speed of the locomotive to zero, to make sure we 
        // have control over it.
        sendDirection(LocoAddress, getLocoDirection(LocoAddress));
        // All we have to do here is create an apropriate XNetMessage, 
        // and send it.
        XNetMessage msg = XNetMessage.getRemoveLocoFromConsistMsg(consistAddress.getNumber(), LocoAddress.getNumber());
        tc.sendXNetMessage(msg, this);
        _state = REMOVEREQUESTSENTSTATE;
    }

    /**
     *  Add a Locomotive to a Lenz Double Header
     *
     *  @param LocoAddress the Locomotive address to add to the locomotive
     *  @param directionNormal is True if the locomotive is traveling 
     *        the same direction as the consist, or false otherwise.
     */
    private synchronized void addToCSConsist(DccLocoAddress LocoAddress, boolean directionNormal) {

        if (consistAddress.equals(LocoAddress)) {
            // Something went wrong here, we are trying to add a
            // trailing locomotive to the consist with the same
            // address as the lead locomotive.  This isn't supposed to 
            // happen.
            log.error("Attempted to add " + LocoAddress.toString()
                    + " to consist " + consistAddress.toString());
            _state = IDLESTATE;
            notifyConsistListeners(_locoAddress,
                    ConsistListener.CONSIST_ERROR
                    | ConsistListener.ALREADY_CONSISTED);
            return;
        }

        // If the consist already contains the locomotive in
        // question, we need to disolve the consist
        if (consistList.size() == 2
                && consistList.contains(LocoAddress)) {
            XNetMessage msg = XNetMessage.getDisolveDoubleHeaderMsg(
                    consistList.get(0).getNumber());
            tc.sendXNetMessage(msg, this);
        }

        // We need to make sure the directions are set correctly
        // In order to do this, we have to pull up both throttles,
        // and check that the direction of the trailing locomotive
        // is correct relative to the lead locomotive.
        DccLocoAddress address = consistList.get(0);
        XNetThrottle lead = new XNetThrottle(systemMemo, address, tc);

        XNetThrottle trail = new XNetThrottle(systemMemo, LocoAddress, tc);

        if (directionNormal) {
            if (log.isDebugEnabled()) {
                log.debug("DOUBLE HEADER: Set direction of trailing locomotive same as lead locomotive");
            }
            trail.setIsForward(lead.getIsForward());
            sendDirection(lead, lead.getIsForward());
            sendDirection(trail, lead.getIsForward());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("DOUBLE HEADER: Set direction of trailing locomotive opposite lead locomotive");
            }
            trail.setIsForward(!lead.getIsForward());
            sendDirection(lead, lead.getIsForward());
            sendDirection(trail, !lead.getIsForward());
        }

        // All we have to do here is create an apropriate XNetMessage, 
        // and send it.
        XNetMessage msg = XNetMessage.getBuildDoubleHeaderMsg(address.getNumber(), LocoAddress.getNumber());
        tc.sendXNetMessage(msg, this);
        _state = ADDREQUESTSENTSTATE;
    }

    /**
     *  Remove a Locomotive from a Lenz Double Header.
     *
     *  @param LocoAddress is the Locomotive address to add to the locomotive
     */
    public synchronized void removeFromCSConsist(DccLocoAddress LocoAddress) {
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
            String text;
            if (l.isOkMessage()) {
                if (_state == ADDREQUESTSENTSTATE) {
                    addToConsistList(_locoAddress, _directionNormal);
                } else if (_state == REMOVEREQUESTSENTSTATE) {
                    removeFromConsistList(_locoAddress);
                }
                _state = IDLESTATE;
            } else if (l.getElement(0) == XNetConstants.LOCO_MU_DH_ERROR) {
                text = "XpressNet MU+DH error: ";
                switch (l.getElement(1)) {
                    case 0x81:
                        text = text + "Selected Locomotive has not been operated by this XpressNet device or address 0 selected";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.LOCO_NOT_OPERATED);
                        break;
                    case 0x82:
                        text = text + "Selected Locomotive is being operated by another XpressNet device";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.LOCO_NOT_OPERATED);
                        break;
                    case 0x83:
                        text = text + "Selected Locomotive already in MU or DH";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.ALREADY_CONSISTED);
                        break;
                    case 0x84:
                        text = text + "Unit selected for MU or DH has speed setting other than 0";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.NONZERO_SPEED);
                        break;
                    case 0x85:
                        text = text + "Locomotive not in a MU";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.NOT_CONSISTED);
                        log.error(text);
                        break;
                    case 0x86:
                        text = text + "Locomotive address not a multi-unit base address";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.NOT_CONSIST_ADDR);

                        log.error(text);
                        break;
                    case 0x87:
                        text = text + "It is not possible to delete the locomotive";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.DELETE_ERROR);
                        break;
                    case 0x88:
                        text = text + "The Command Station Stack is Full";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR
                                | ConsistListener.STACK_FULL);
                        log.error(text);
                        break;
                    default:
                        text = text + "Unknown";
                        log.error(text);
                        _state = IDLESTATE;
                        notifyConsistListeners(_locoAddress,
                                ConsistListener.CONSIST_ERROR);
                }
            }
        }
    }

    @Override
    public void message(XNetMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /**
     * Set the speed and direction of a locomotive; bypassing the 
     * commands in the throttle, since they don't work for this 
     * application.
     * <p>
     * For this application, we also set the speed setting to 0, which 
     * also establishes control over the locomotive in the consist.
     *
     * @param t an XNetThrottle (XpressNet) throttle
     * @param isForward the boolean value representing the desired
     * direction
     */
    private void sendDirection(XNetThrottle t, boolean isForward) {
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(t.getDccAddress(),
                t.getSpeedStepMode(),
                (float) 0.0,
                isForward);
        // now, we send the message to the command station
        tc.sendXNetMessage(msg, this);
    }

    /**
     * Set the speed and direction of a locomotive; bypassing the 
     * commands in the throttle, since they don't work for this 
     * application.
     * <p>
     * For this application, we also set the speed setting to 0, which 
     * also establishes control over the locomotive in the consist.
     *
     * @param t an XNetThrottle (XpressNet) throttle
     * @param isForward the boolean value representing the desired
     * direction
     */
    private void sendDirection(DccLocoAddress address, boolean isForward) {
        XNetMessage msg = XNetMessage.getSpeedAndDirectionMsg(address.getNumber(),
                jmri.DccThrottle.SpeedStepMode28,
                (float) 0.0,
                isForward);
        // now, we send the message to the command station
        tc.sendXNetMessage(msg, this);
    }

    private final static Logger log = LoggerFactory.getLogger(XNetConsist.class);

}
