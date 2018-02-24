/**
 * Consist Manager for use with the NceConsist class for the
 * consists it builds.
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Daniel Boudreau Copyright (C) 2012
 */
package jmri.jmrix.nce;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.AbstractConsistManager;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NceConsistManager extends AbstractConsistManager {

    private NceSystemConnectionMemo memo = null;

    public NceConsistManager(NceSystemConnectionMemo m) {
        super();
        memo = m;
    }

    /**
     * Request an update from the layout, loading
     * Consists from the command station.
     */
    @Override
    public void requestUpdateFromLayout() {
        if (shouldRequestUpdateFromLayout()) {
            startConsistReader();
        }
    }

    /**
     * This implementation does not support command station assisted consists,
     * so return false.
     *
     */
    @Override
    public boolean isCommandStationConsistPossible() {
        return false;
    }

    /**
     * Does a CS consist require a separate consist address?
     *
     */
    @Override
    public boolean csConsistNeedsSeperateAddress() {
        return false;
    }

    /**
     * Add a new NceConsist with the given address to consistTable/consistList
     */
    @Override
    public Consist addConsist(LocoAddress locoAddress) {
        if (! (locoAddress instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("locoAddress is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(locoAddress)) // no duplicates allowed.
        {
            return consistTable.get(locoAddress);
        }
        log.debug("Add consist, address " + locoAddress);
        NceConsist consist = new NceConsist((DccLocoAddress) locoAddress, memo);
        consistTable.put(locoAddress, consist);
        return consist;
    }

    private void addConsist(NceConsist consist) {
        log.debug("Add consist " + consist.getConsistAddress());
        //delConsist(consist.getConsistAddress()); // remove consist if one exists
        consistTable.put(consist.getConsistAddress(), consist);
    }

    @Override
    public Consist getConsist(LocoAddress locoAddress) {
        log.debug("Requesting NCE consist " + locoAddress);
        NceConsist consist = (NceConsist) super.getConsist(locoAddress);
        // Checking the CS memory each time a consist is requested creates lots of NCE messages!
        //consist.checkConsist();
        return consist;
    }

    // remove the old Consist
    @Override
    public void delConsist(LocoAddress locoAddress) {
        NceConsist consist = (NceConsist) getConsist(locoAddress);
        // kill this consist
        consist.dispose();
        super.delConsist(locoAddress);
    }

    public void startConsistReader() {
        // read command station memory (not USB and not simulator) Can't determine if simulator selected, but can determine if port name is the default
        if (memo.getNceUsbSystem() == NceTrafficController.USB_SYSTEM_NONE && !memo.getNceTrafficController().getPortName().equals(JmrixConfigPane.NONE_SELECTED)
                && memo.getNceTrafficController().getCommandOptions() > NceTrafficController.OPTION_1999) {
            new NceConsistReader().start();
        }
    }

    public class NceConsistReader extends Thread {

        int _consistNum = NceConsist.CONSIST_MAX;

        NceConsistReader() {
            setName("Initialize NCE consists");
        }

        @Override
        public void run() {
            searchNext();
        }

        // NCE allocates consist numbers starting at 127, so we do the same
        private void searchNext() {
            synchronized (this) {
                // we need to wait for the connection to be up and running
                while (!ConnectionStatus.instance().getConnectionState(memo.getNceTrafficController().getPortName()).equals(ConnectionStatus.CONNECTION_UP)) {
                    log.debug("Waiting for NCE connected");
                    try {
                        wait(2000); // wait 2 seconds and try again
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                    }
                }
            }
            while (_consistNum >= NceConsist.CONSIST_MIN) {
                if (log.isDebugEnabled()) {
                    log.debug("Reading consist from command station: " + _consistNum);
                }
                NceConsist consist = new NceConsist(_consistNum, memo);
                // wait until consist finishes CS read
                while (!consist.isValid()) {
                    synchronized (this) {
                        try {
                            //log.debug("Waiting for consist "+_consistNum+" to be valid");
                            wait(100); // wait 100 milliseconds and check again
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }
                    }
                }
                if (consist.getConsistList().size() > 0) {
                    addConsist(consist);
                }
                _consistNum--;
            }
            // when we finish reading, notify any listeners.
            notifyConsistListChanged();
        }
    }
    private final static Logger log = LoggerFactory.getLogger(NceConsistManager.class);
}
