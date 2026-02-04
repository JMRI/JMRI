package jmri.jmrix.powerline.dmx512;

import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;

import java.io.IOException;

import jmri.jmrix.SerialPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from messages. The "SerialInterface" side
 * sends/receives message objects.
 * <p>
 * The connection to a SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2005, 2006, 2008 Converted to
 * multiple connection
 * @author Ken Cameron Copyright (C) 2023
 */
public class SpecificTrafficController extends SerialTrafficController {

    public SpecificTrafficController(SerialSystemConnectionMemo memo) {
        super();
        this.memo = memo;
        logDebug = log.isDebugEnabled();

        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send
    }

    private boolean oneTimeLog = true;
    public byte[] dmxArray = new byte[513];
    private int intensitySteps = 255;
    private SerialPort activePort = null;

    /**
     * set value in dmxArray
     * @param unitId offset in dmxArray
     * @param intensityValue value to put in dmxArray
     * @return true when values ok
     */
    public boolean setDmxIntensity(int unitId, byte intensityValue) {
        if ((unitId > 0) && (unitId <= 512)) {
            dmxArray[unitId] = intensityValue;
            return(true);
        }
        return(false);
    }

    @Override
    protected void transmitLoop() {
        if (oneTimeLog) {
            oneTimeLog = false;
            for (int i = 0; i < dmxArray.length; i++) {
                dmxArray[i] = 0;
            }
            dmxArray[0] = (byte) 0; // type of buffer going out
            /**
             * deal with thread sync of main tc still getting setup by time of
             * first call in the transmit loop. Give it time and retry
             */
            int tryLimit = 0;
            while ((activePort == null) && (tryLimit <= 10)) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                }
                tryLimit++;
                activePort = memo.getActiveSerialPort();
                if (activePort == null) {
                    log.info("try {} to get activePort", tryLimit);
                }
            }
        }

        // loop forever
        while (!connectionError && !threadStopRequest) {
            try {
                if (ostream != null) {
                    // break should be for at least 176 uSec
                    if (activePort != null) {
                        //log.info("Start Break");
                        activePort.setBreak();
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            log.warn("transmitLoop did not expected to be interrupted during break");
                            break;
                        }
                        activePort.clearBreak();
                        //log.info("Break Sent");
                        // wait at least 8 usec (not msec, usec)
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            log.warn("transmitLoop did not expected to be interrupted during clear");
                            break;
                        }
                    }
                    /**
                     * send the buffer of data
                     */
                    try {
                        ostream.write(dmxArray);
                    } catch (com.fazecast.jSerialComm.SerialPortTimeoutException ex) {
                        if (!threadStopRequest) {
                            log.warn("DMX512 write operation ended early");
                        }
                        return;
                    }
                    /**
                     * wait 25 mSec, then repeat
                     */
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    connectionError = true;
                }
            } catch (IOException | RuntimeException e) {
                // TODO Currently there's no port recovery if an exception occurs
                // must restart JMRI to clear xmtException.
                xmtException = true;
                portWarn(e);
            }
        }
    }

    // not used, no reback
    @Override
    public void receiveLoop() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * This system provides 256 dim steps
     */
    @Override
    public int getNumberOfIntensitySteps() {
        return intensitySteps;
    }

    /**
     * Send a sequence of Dmx messages
     * <p>
     * Makes call to update array
     */
    @Override
    public boolean sendDmxSequence(int unitid, byte newStep) {
        // log.info("Unit {} value {}", unitid, (int) newStep);
        boolean didIt = setDmxIntensity(unitid, newStep);
        if (!didIt) {
            log.error("Invalid Dmx Message for unit {} value {}", unitid, newStep);
        }
        return didIt;
    }
    private final static Logger log = LoggerFactory.getLogger(SpecificTrafficController.class);

}

