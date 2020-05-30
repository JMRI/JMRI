package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.managers.AbstractPowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author B. Milhaupt Copyright (C)
 */
public class LnPowerManager extends AbstractPowerManager<LocoNetSystemConnectionMemo> implements LocoNetListener {

    public LnPowerManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
        // standard LocoNet - connect
        if (memo.getLnTrafficController() == null) {
            log.error("PowerManager Created, yet there is no Traffic Controller");
            return;
        }
        this.tc = memo.getLnTrafficController();
        tc.addLocoNetListener(~0, this);

        updateTrackPowerStatus();  // this delays a while then reads slot 0 to get current track status
    }

    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN;

        checkTC();
        if (v == ON) {
            // send GPON
            LocoNetMessage l = new LocoNetMessage(2);
            l.setOpCode(LnConstants.OPC_GPON);
            tc.sendLocoNetMessage(l);
        } else if (v == OFF) {
            // send GPOFF
            LocoNetMessage l = new LocoNetMessage(2);
            l.setOpCode(LnConstants.OPC_GPOFF);
            tc.sendLocoNetMessage(l);
        } else if ((v == IDLE) && (implementsIdle())) {
            // send OPC_IDLE
            LocoNetMessage l = new LocoNetMessage(2);
            l.setOpCode(LnConstants.OPC_IDLE);
            tc.sendLocoNetMessage(l);
        }

        firePowerPropertyChange(old, power);
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        if (thread != null) {
            try {
                thread.interrupt();
                thread.join();
            } catch (InterruptedException ex) {
                log.warn("dispose interrupted");
            } finally {
                thread = null;
            }
        }

        if (tc != null) {
            tc.removeLocoNetListener(~0, this);
        }
        tc = null;
    }

    LnTrafficController tc = null;

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("Use power manager after dispose"); // NOI18N
        }
    }

    // to listen for status changes from LocoNet
    @Override
    public void message(LocoNetMessage m) {
        int old = power;
        switch (m.getOpCode()) {
            case LnConstants.OPC_GPON:
                power = ON;
                break;
            case LnConstants.OPC_GPOFF:
                power = OFF;
                break;
            case LnConstants.OPC_IDLE:
                power = IDLE;
                break;
            case LnConstants.OPC_SL_RD_DATA:
                // grab the track status any time that a slot read of a "normal" slot passes thru.
                // Ignore "reserved" and "master control" slots in slot numbers 120-127
                if ((m.getElement(1) == 0x0E) && (m.getElement(2) < 120)) {
                    switch (m.getElement(7) & (0x03)) {
                        case LnConstants.GTRK_POWER:
                            power = IDLE;
                            break;
                        case (LnConstants.GTRK_POWER + LnConstants.GTRK_IDLE):
                            power = ON;
                            break;
                        case LnConstants.GTRK_IDLE:
                            power = OFF;
                            break;
                        default:
                            power = UNKNOWN;
                            break;
                    }
                }   break;
            default:
                break;
        }
        firePowerPropertyChange(old, power);
    }

    /**
     * Creates a thread which delays and then queries slot 0 to get the current
     * track status. The LnListener will see the slot read data and use the
     * current track status to update the LnPowerManager's internal track power
     * state info.
     */
    private void updateTrackPowerStatus() {
        thread = new LnTrackStatusUpdateThread(tc);
        thread.setName("LnPowerManager LnTrackStatusUpdateThread");
        thread.start();
    }

    volatile LnTrackStatusUpdateThread thread;

    /**
     * Class providing a thread to delay, then query slot 0. The LnPowerManager
     * can use the resulting OPC_SL_RD_DATA message to update its view of the
     * current track status.
     */
    static class LnTrackStatusUpdateThread extends Thread {

        private LnTrafficController tc;

        /**
         * Construct the thread.
         *
         * @param tc LocoNetTrafficController which can be used to send the
         *           LocoNet message.
         */
        public LnTrackStatusUpdateThread(LnTrafficController tc) {
            this.tc = tc;
        }

        /**
         * Runs the thread - Waits a while (to allow the managers to initialize),
         * then sends a query of slot 0 so that the PowerManager can inspect
         * the {@code "<trk>"} byte.
         */
        @Override
        public void run() {
            // wait a little bit to allow PowerManager to be initialized
            log.trace("LnTrackStatusUpdateThread start check loop");
            for (int i = 1; i <=10; i++) {
                if (tc.status()) break; // TrafficController is reporting ready
                
                log.trace("LnTrackStatusUpdateThread waiting {} time", i);
                // else wait, then try again
                try {
                    // Delay 500 mSec to allow init of traffic controller, listeners.
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                    return; // and stop work
                }
            }

            try {
                // Delay just a bit more, just in case.  Yes, this shouldn't be needed...
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                return; // and stop work
            }
            
            log.trace("LnTrackStatusUpdateThread sending request");
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_RQ_SL_DATA);
            msg.setElement(1, 0);
            msg.setElement(2, 0);

            tc.sendLocoNetMessage(msg);
            log.debug("LnTrackStatusUpdate sent");
        }
    }

    /**
     * Returns whether command station supports IDLE funcitonality
     *
     * @return true if connection's command station supports IDLE state, else false
     */
    @Override
    public boolean implementsIdle() {
        boolean supportsIdleState = false;
        if (tc == null) {
            log.error("TC is null in LnPowerManager");
            return false;
        }
        if (tc.memo == null) {
            log.error("TC.Memo is null in LnPowerManager");
            return false;
        }
        LnCommandStationType cmdStationType = tc.memo.getSlotManager().getCommandStationType();
        switch (cmdStationType) {
            case COMMAND_STATION_DB150:
            case COMMAND_STATION_DCS100:
            case COMMAND_STATION_DCS240:
            case COMMAND_STATION_DCS210:
            case COMMAND_STATION_DCS200:
                supportsIdleState = true;
                break;
            default:
                supportsIdleState = false;

        }
        return supportsIdleState;
    }

    private final static Logger log = LoggerFactory.getLogger(LnPowerManager.class);

}
