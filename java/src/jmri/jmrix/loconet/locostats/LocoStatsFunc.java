package jmri.jmrix.loconet.locostats;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import jmri.util.StringUtil;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements functionality to query the LocoNet interface device for status.
 * 
 * @author Bob Milhaupt
 * @copyright 2017
 */
public class LocoStatsFunc implements LocoNetListener {
    private LocoNetSystemConnectionMemo memo;
    public LocoStatsFunc(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        updatePending = false;
        ifaceStatus = null;
        if (memo != null) {
            this.memo.getLnTrafficController().addLocoNetListener(0, this);
        }
    }
    private boolean updatePending;
    private Object ifaceStatus;
    
    /**
     * Request LocoNet interface Status
     */
    public void sendLocoNetInterfaceStatusQueryMessage() {
        if (memo == null) {
            return;
        }
        LocoNetMessage l = new LocoNetMessage(new int[] {0x81, 0x7f});
        memo.getLnTrafficController().sendLocoNetMessage(l);
        updatePending = true;
        log.info("Sent a query");
    }
    
    /**
     * LocoNet message handler.
     * 
     * @param msg - incoming LocoNet message to be interpreted
     */
    @Override
    public void message(LocoNetMessage msg) {
        if ((msg.getOpCode() == LnConstants.OPC_PEER_XFER)
                && (msg.getElement(1) == 0x10)
                && (msg.getElement(2) == 0x50)
                && (msg.getElement(3) == 0x50)
                && (msg.getElement(4) == 0x01)
                && ((msg.getElement(5) & 0xF0) == 0x0)
                && ((msg.getElement(10) & 0xF0) == 0x0)) {
            // LocoBuffer II form
            int[] data = msg.getPeerXfrData();
            ifaceStatus = new LocoBufferIIStatus(
                    data[0]*256+data[4],
                    (data[5] << 16) + (data[6] << 8) + data[7],
                    (data[1] << 16) + (data[2] << 8) + data[3]
            );
            updatePending = false;
            updateListeners();
            log.info("Got a query reply: LocoBufferII");

        } else if ((msg.getOpCode() == LnConstants.OPC_PEER_XFER)
                && (msg.getElement(1) == 0x10)
                && (msg.getElement(2) == 0x22)
                && (msg.getElement(3) == 0x22)
                && (msg.getElement(4) == 0x01)) {  // Digitrax form, check PR2/PR3 or MS100/PR3 mode

            if ((msg.getElement(8) & 0x20) == 0) {
                // PR2 format
                int[] data = msg.getPeerXfrData();
                ifaceStatus = new PR2Status(
                        data[1] * 256 + data[0],
                        data[2],
                        data[3],
                        data[4],
                        data[5]
                );
                log.info("Got a reply: PR2 mode");

            } else {
                // MS100 format
                int[] data = msg.getPeerXfrData();
                ifaceStatus = new PR3MS100ModeStatus(
                        data[1] * 256 + data[0],
                        data[5] * 256 + data[4],
                        data[2]
                );
                log.info("Got a reply: MS100 mode");
            }
            updatePending = false;
            updateListeners();

        } else if ((msg.getOpCode() == LnConstants.OPC_PEER_XFER)) {
            // raw mode
            try {
                int[] data = msg.getPeerXfrData();
                ifaceStatus = new RawStatus(data[0], data[1], data[2], data[3],
                        data[4], data[5], data[6], data[7]
                );

                updatePending = false;
                updateListeners();
                log.info("Got a reply: Raw mode");
            } catch (Exception e) {
                log.error("Error parsing update: " + msg);
            }
        } else if (!updatePending && (msg.getOpCode() == LnConstants.OPC_GPBUSY)) {
            updatePending = true;
        }
    }
    private void updateListeners() {
        listeners.stream().forEach((l) -> {
            l.notifyChangedInterfaceStatus(ifaceStatus);
        });
        return;
    }
    
    /**
     * Get the latest interface status
     * 
     * @return the latest interface status; will be null if status has 
     *          not been pulled.
     */
    public Object getInterfaceStatus() {
        return ifaceStatus;
    }
    
    /**
     * Free resources when no longer used
     */
    public void dispose() {
        listeners.removeAllElements();
        listeners = null;
    }
    
    // The methods to implement adding and removing listeners
    protected Vector<LocoNetInterfaceStatsListener> listeners = new Vector<LocoNetInterfaceStatsListener>();

    /**
     * Add a listener to the list of listeners which will be notified upon receipt 
     * a LocoNet message containing interface statistics.
     * 
     * @param l - LocoNetInterfaceStatsListener to be added
     */
    public synchronized void addLocoNetInterfaceStatsListener(LocoNetInterfaceStatsListener l) {
        // add only if not already registered
        if (l == null) {
            throw new java.lang.NullPointerException();
        }
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    /**
     * Remove a listener (if present) from the list of listeners which will be 
     * notified upon receipt LocoNet message containing interface statistics.
     * 
     * @param l - LocoNetInterfaceStatsListener to be removed
     */
    public synchronized void removeLocoNetInterfaceStatsListener(LocoNetInterfaceStatsListener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocoStatsFunc.class.getName());
}
