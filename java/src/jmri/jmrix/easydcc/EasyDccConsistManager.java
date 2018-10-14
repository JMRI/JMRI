/**
 * Consist Manager for use with the EasyDccConsist class for the
 * consists it builds.
 *
 * @author Paul Bender Copyright (C) 2006
 */
package jmri.jmrix.easydcc;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.AbstractConsistManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDccConsistManager extends AbstractConsistManager {

    private EasyDccConsistReader reader;
    private EasyDccSystemConnectionMemo _memo = null;
    private EasyDccTrafficController trafficController = null;

    /**
     * Constructor - call the constructor for the superclass, and initialize the
     * consist reader thread, which retrieves consist information from the
     * command station.
     *
     * @param memo the associated connection memo
     */
    public EasyDccConsistManager(EasyDccSystemConnectionMemo memo) {
        super();
        reader = new EasyDccConsistReader();
        _memo = memo;
        // connect to the TrafficManager
        trafficController = memo.getTrafficController();
    }

    /**
     * This implementation does support advanced consists, so return true.
     *
     */
    @Override
    public boolean isCommandStationConsistPossible() {
        return true;
    }

    /**
     * Does a CS consist require a separate consist address? CS consist
     * addresses are assigned by the user, so return true.
     *
     */
    @Override
    public boolean csConsistNeedsSeperateAddress() {
        return true;
    }

    /**
     * Add a new EasyDccConsist with the given address to
     * consistTable/consistList.
     */
    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) { // no duplicates allowed across all connections
            return consistTable.get(address);
        }
        EasyDccConsist consist;
        consist = new EasyDccConsist((DccLocoAddress) address, _memo);
        consistTable.put(address, consist);
        return consist;
    }

    /* Request an update from the layout, loading
     * Consists from the command station.
     */
    @Override
    public void requestUpdateFromLayout() {
        if (this.shouldRequestUpdateFromLayout()) {
            reader.searchNext();
        }
    }

    @Override
    protected boolean shouldRequestUpdateFromLayout() {
        return (reader.currentState == EasyDccConsistReader.IDLE);
    }

    /**
     * Internal class to read consists from the command station.
     */
    private class EasyDccConsistReader implements Runnable, EasyDccListener {

        // Storage for addresses
        int _lastAddress = 0;
        // Possible States
        final static int IDLE = 0;
        final static int SEARCHREQUESTSENT = 1;
        // Current State
        int currentState = IDLE;

        EasyDccConsistReader() {
        }

        @Override
        public void run() {
        }

        private void searchNext() {
            log.debug("Sending request for next consist, _lastAddress is: {}", _lastAddress);
            currentState = SEARCHREQUESTSENT;
            EasyDccMessage msg = EasyDccMessage.getDisplayConsist(++_lastAddress);
            trafficController.sendEasyDccMessage(msg, this);
        }

        // Listener for messages from the command station
        @Override
        public void reply(EasyDccReply r) {
            if (currentState == SEARCHREQUESTSENT) {
                // We sent a request for a consist address.
                // We need to find out what type of message 
                // was received as a response.  If the message
                // has an opcode of 'G', then it is a response 
                // to the Display Consist instruction we sent 
                // previously.  If the message has any other
                // opcode, we can ignore the message.
                if (log.isDebugEnabled()) {
                    log.debug("Message Received in SEARCHREQUESTSENT state. Message is: {}", r.toString());
                }
                if (r.getOpCode() == 'G') {
                    // This is the response we're looking for
                    // The bytes 2 and 3 are the ...

                    int consistAddr;
                    Boolean newConsist = true;
                    EasyDccConsist currentConsist = null;
                    String sa = "" + (char) r.getElement(1)
                            + (char) r.getElement(2);
                    consistAddr = Integer.valueOf(sa, 16).intValue();

                    // The rest of the message consists of 4 hex digits
                    // for each of up to 8 locomotives.
                    for (int i = 3; i < r.getNumDataElements(); i += 4) {
                        DccLocoAddress locoAddress;
                        int tempAddr;
                        boolean directionNormal;
                        if ((char) r.getElement(i) == ' ') {
                            i++; // skip a space
                        } else if (java.lang.Character.digit((char) r.getElement(i), 16) == -1) {
                            break; // stop the loop if we don't have a hex digit.
                        }
                        String sb = "" + (char) r.getElement(i)
                                + (char) r.getElement(i + 1)
                                + (char) r.getElement(i + 2)
                                + (char) r.getElement(i + 3);
                        tempAddr = Integer.valueOf(sb, 16).intValue();
                        directionNormal = ((tempAddr & 0x8000) == 0);
                        if (tempAddr != 0) {
                            if (newConsist) {
                                // This is the first address, add the
                                // consist
                                currentConsist = (EasyDccConsist) addConsist(
                                        new DccLocoAddress(consistAddr, false));
                                newConsist = false;
                            }
                            locoAddress = new DccLocoAddress(
                                    tempAddr & 0x7fff, (tempAddr & 0x7fff) > 99);
                            if (currentConsist != null) {
                                currentConsist.restore(locoAddress, directionNormal);
                            } else //should never happen since currentConsist gets set in the first pass
                            {
                                log.error("currentConsist is null!");
                            }
                        }
                    }
                    if (_lastAddress < 255) {
                        searchNext();
                    } else {
                        currentState = IDLE;
                        notifyConsistListChanged();
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Message Received in IDLE state. Message is: {}", r.toString());
                    }
                }
            }
        }

        // Listener for messages to the command station
        @Override
        public void message(EasyDccMessage m) {
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccConsistManager.class);

}
