package jmri.jmrix.lenz;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.AbstractConsistManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consist Manager for use with the XNetConsist class for the consists it builds
 * @author Paul Bender Copyright (C) 2004-2010
 * @navassoc 1 - * jmri.jmrix.lenz.XNetConsist
 */
public class XNetConsistManager extends AbstractConsistManager {

    private Thread initThread = null;
    protected XNetTrafficController tc = null;
    private boolean requestingUpdate = false;

    /**
     * Constructor - call the constructor for the superclass, and initialize the
     * consist reader thread, which retrieves consist information from the
     * command station.
     */
    public XNetConsistManager(XNetSystemConnectionMemo systemMemo) {
        super();
        tc = systemMemo.getXNetTrafficController();
        this.systemMemo = systemMemo;
    }
    XNetSystemConnectionMemo systemMemo;

    /**
     * This implementation does command station consists, so return true.
     */
    @Override
    public boolean isCommandStationConsistPossible() {
        return true;
    }

    /**
     * Does a CS consist require a separate consist address? CS consist
     * addresses are assigned by the command station, so no consist address is
     * needed, so return false.
     */
    @Override
    public boolean csConsistNeedsSeperateAddress() {
        return false;
    }

    /**
     * Add a new XNetConsist with the given address to consistTable/consistList.
     */
    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) { // no duplicates allowed.
            return consistTable.get(address);
        }
        XNetConsist consist;
        consist = new XNetConsist((DccLocoAddress) address, tc, systemMemo);
        consistTable.put(address, consist);
        return (consist);
    }

    /**
     * Request an update from the layout, loading Consists from the command
     * station.
     */
    @Override
    public void requestUpdateFromLayout() {
        if (shouldRequestUpdateFromLayout()) {
            // Initilize the consist reader thread.
            initThread = new Thread(new XNetConsistReader());
            int it = initThread.getPriority();
            it--;
            initThread.setPriority(it);
            initThread.start();
        }
    }

    @Override
    protected boolean shouldRequestUpdateFromLayout() {
        return !requestingUpdate;
    }

    /**
     * Internal class to read consists from the Command Station.
     */
    private class XNetConsistReader implements Runnable, XNetListener {

        // Storage for addresses
        int _lastMUAddress = 0;
        int _lastAddress = 0;
        int _lastMemberAddress = 0;
        XNetConsist currentConsist = null;
        // Possible States
        final static int IDLE = 0;
        final static int SEARCHREQUESTSENT = 1;
        final static int MUSEARCHSENT = 2;
        final static int MUINFOREQUESTSENT = 4;
        final static int DHADDRESS1INFO = 8;
        final static int DHADDRESS2INFO = 16;
        // Current State
        int currentState = IDLE;

        XNetConsistReader() {
            // Register as an XPressNet Listener
            tc.addXNetListener(XNetInterface.COMMINFO
                    | XNetInterface.THROTTLE
                    | XNetInterface.CONSIST,
                    this);
            searchNext();
        }

        @Override
        public void run() {
        }

        private void searchNext() {
            requestingUpdate = true;
            if (log.isDebugEnabled()) {
                log.debug("Sending search for next DB Entry, _lastAddress is: " + _lastAddress);
            }
            currentState = SEARCHREQUESTSENT;
            XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(_lastAddress, true);
            tc.sendXNetMessage(msg, this);
        }

        private void searchMU() {
            if (log.isDebugEnabled()) {
                log.debug("Sending search for next MU Entry, _lastMUAddress is: " + _lastMUAddress + " _lastMemberAddress is: " + _lastMemberAddress);
            }
            currentState = MUSEARCHSENT;
            XNetMessage msg = XNetMessage.getDBSearchMsgNextMULoco(_lastMUAddress, _lastMemberAddress, true);
            tc.sendXNetMessage(msg, this);
        }

        private void requestInfoMU() {
            if (log.isDebugEnabled()) {
                log.debug("Sending search for next MU Entry information , _lastMemberAddress is: " + _lastMemberAddress);
            }
            currentState = MUINFOREQUESTSENT;
            XNetMessage msg = XNetMessage.getLocomotiveInfoRequestMsg(_lastMemberAddress);
            tc.sendXNetMessage(msg, this);
        }

        // Listener for messages from the command station
        @Override
        public void message(XNetReply l) {
            switch (currentState) {
                case SEARCHREQUESTSENT:
                    // We sent a request to search the stack.
                    // We need to find out what type of message
                    // was recived as a response.  If We're
                    // told the message is for an MU base address
                    // a locomotive in a Double Header, we
                    // want to take further action.  If the
                    // message tells us we've reached the end
                    // of the stack, then we can quit. Otherwise,
                    // we just request the next address.
                    if (log.isDebugEnabled()) {
                        log.debug("Message Received in SEARCHREQUESTSENT state.  Message is: " + l.toString());
                    }
                    if (l.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
                        switch (l.getElement(1)) {
                            case XNetConstants.LOCO_SEARCH_RESPONSE_N:
                            case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
                                _lastAddress = l.getThrottleMsgAddr();
                                searchNext();
                                break;
                            case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
                                _lastAddress = l.getThrottleMsgAddr();
                                _lastMUAddress = _lastAddress;
                                _lastMemberAddress = _lastAddress;
                                if (log.isDebugEnabled()) {
                                    log.debug("Sending search for first DH Entry information , _lastMemberAddress is: "
                                            + _lastMemberAddress);
                                }
                                currentState = DHADDRESS1INFO;
                                XNetMessage msg = XNetMessage.getLocomotiveInfoRequestMsg(_lastMemberAddress);
                                tc.sendXNetMessage(msg, this);
                                break;
                            case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
                                _lastAddress = l.getThrottleMsgAddr();
                                _lastMUAddress = _lastAddress;
                                currentConsist = (XNetConsist) addConsist(
                                        new DccLocoAddress(_lastMUAddress, false));
                                searchMU();
                                break;
                            case XNetConstants.LOCO_SEARCH_NO_RESULT:
                                currentState = IDLE;
                                notifyConsistListChanged();
                                requestingUpdate = false;
                                break;
                            case XNetConstants.LOCO_NOT_AVAILABLE:
                            case XNetConstants.LOCO_FUNCTION_STATUS:
                            default: // Do Nothing by default
                        }
                    }
                    break;
                case MUSEARCHSENT:
                    if (log.isDebugEnabled()) {
                        log.debug("Message Received in MUSEARCHSENT state.  Message is: " + l.toString());
                    }
                    if (l.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
                        switch (l.getElement(1)) {
                            case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
                                _lastMemberAddress = l.getThrottleMsgAddr();
                                if (_lastMemberAddress != 0) {
                                    // Find out the direction information
                                    // for the address in question.
                                    requestInfoMU();
                                } else {
                                    // We reached the end of this consist,
                                    // find the next one
                                    searchNext();
                                }
                                break;
                            case XNetConstants.LOCO_SEARCH_NO_RESULT:
                                searchNext();
                                break;
                            case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
                            case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
                            case XNetConstants.LOCO_SEARCH_RESPONSE_N:
                            case XNetConstants.LOCO_NOT_AVAILABLE:
                            case XNetConstants.LOCO_FUNCTION_STATUS:
                            default: // Do Nothing by default
                        }
                    }
                    break;
                case MUINFOREQUESTSENT:
                    if (log.isDebugEnabled()) {
                        log.debug("Message Received in MUINFOREQUESTSENT state.  Message is: " + l.toString());
                    }
                    if (l.getElement(0) == XNetConstants.LOCO_INFO_MUED_UNIT) {
                        currentConsist.restore(new DccLocoAddress(_lastMemberAddress, _lastMemberAddress > 99),
                                (l.getElement(2) & 0x80) == 0x80);
                        searchMU();
                    }
                    break;
                case DHADDRESS1INFO:
                    if (log.isDebugEnabled()) {
                        log.debug("Message Received in DHADDRESS1INFO state.  Message is: " + l.toString());
                    }
                    if (l.getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT) {
                        DccLocoAddress firstMember = new DccLocoAddress(_lastMemberAddress, _lastMemberAddress > 99);
                        int AH = l.getElement(5);
                        int AL = l.getElement(6);
                        if (AH == 0x00) {
                            _lastMemberAddress = AL;
                        } else {
                            _lastMemberAddress = ((AH * 256) & 0xFF00)
                                    + (AL & 0xFF)
                                    - 0xC000;
                        }

                        // We need to check and see if this consist exists
                        if (!XNetConsistManager.this.consistTable.containsKey(firstMember)
                                && !XNetConsistManager.this.consistTable.containsKey(new DccLocoAddress(_lastMemberAddress, _lastMemberAddress > 99))) {
                            currentConsist = (XNetConsist) addConsist(firstMember);
                            currentConsist.setConsistType(Consist.CS_CONSIST);
                            currentConsist.restore(firstMember,
                                    (l.getElement(2) & 0x80) == 0x80);
                            if (log.isDebugEnabled()) {
                                log.debug("Sending search for second DH Entry information , _lastMemberAddress is: "
                                        + _lastMemberAddress);
                            }
                            currentState = DHADDRESS2INFO;
                            XNetMessage msg = XNetMessage
                                    .getLocomotiveInfoRequestMsg(
                                            _lastMemberAddress);
                            tc.sendXNetMessage(msg, this);
                        } else {
                            // This consist already exists
                            searchNext();
                        }
                    }
                    break;
                case DHADDRESS2INFO:
                    if (log.isDebugEnabled()) {
                        log.debug("Message Received in DHADDRESS2INFO state.  Message is: " + l.toString());
                    }
                    if (l.getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT) {
                        currentConsist.restore(new DccLocoAddress(_lastMemberAddress, _lastMemberAddress > 99),
                                (l.getElement(2) & 0x80) == 0x80);
                    }
                    // We reached the end of this consist,
                    // find the next one
                    searchNext();
                    break;
                case IDLE:
                default:
                    log.debug("Message Received in default(IDLE) state. Message is: {}", l.toString());
            }
        }

        /**
         * Listener for messages to the command station.
         */
        @Override
        public void message(XNetMessage l) {
        }

        /**
         * Handle a timeout notification.
         */
        @Override
        public void notifyTimeout(XNetMessage msg) {
            if (log.isDebugEnabled()) {
                log.debug("Notified of timeout on message" + msg.toString());
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(XNetConsistManager.class);
}
