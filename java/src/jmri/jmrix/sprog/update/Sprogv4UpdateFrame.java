package jmri.jmrix.sprog.update;

import javax.swing.JOptionPane;
import jmri.jmrix.sprog.SprogConstants.SprogState;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for SPROG v4 firmware update utility.
 *
 * Refactored
 *
 * @author	Andrew Crosland Copyright (C) 2004
 * 
 * @deprecated since 4.11.1; supports uncommon Sprog versions that are confused with Sprog II versions.
 */
@Deprecated
public class Sprogv4UpdateFrame
        extends SprogUpdateFrame
        implements SprogVersionListener {

    public Sprogv4UpdateFrame(SprogSystemConnectionMemo memo) {
        super(memo);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        setSprogModeButton.setVisible(false);

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.update.Sprogv4UpdateFrame", true);

        // Get the SPROG version
        _memo.getSprogVersionQuery().requestVersion(this);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void notifyVersion(SprogVersion v) {
        sv = v;
        if (sv.sprogType.isSprog() == false) {
            // Didn't recognize a SPROG so check if it is in boot mode already
            if (log.isDebugEnabled()) {
                log.debug("SPROG not found - looking for bootloader");
            }
            statusBar.setText(Bundle.getMessage("StatusSprogNotFound"));
            requestBoot();
        } else {
            // Check that it's a V4
            if (sv.sprogType.sprogType == SprogType.SPROGV4) {
                statusBar.setText(Bundle.getMessage("StatusFoundX", sv.toString()));
                // Put SPROG in boot mode
                if (log.isDebugEnabled()) {
                    log.debug("Putting SPROG in boot mode");
                }
                msg = new SprogMessage("b 1 1 1");
                tc.sendSprogMessage(msg, this);
                // SPROG v4 will reply
                bootState = BootState.SETBOOTSENT;
            } else {
                log.error("Incorrect SPROG Type detected");
                statusBar.setText(Bundle.getMessage("StatusIncorrectSprogType"));
                bootState = BootState.IDLE;
            }
        }
    }

    @Override
    synchronized protected void stateSetBootSent() {
        // Only old SPROG v4 reach this state
        if (log.isDebugEnabled()) {
            log.debug("reply in SETBOOTSENT state");
        }
        if (replyString.indexOf("L>") >= 0) {
            // Enable the file chooser button
            if (log.isDebugEnabled()) {
                log.debug("Found v4 bootloader prompt");
            }
            tc.setSprogState(SprogState.V4BOOTMODE);
            openFileChooserButton.setEnabled(true);

            // We remain in this state until program button is pushed
        } else {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("StatusBadBootloaderReply"),
                    Bundle.getMessage("SprogBootloaderXTitle", " v4"), JOptionPane.ERROR_MESSAGE);
            log.error("Bad reply to SETBOOT request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
        }
    }

    @Override
    synchronized protected void stateBootVerReqSent() {
        stopTimer();
        if (log.isDebugEnabled()) {
            log.debug("reply in VERREQSENT state " + replyString);
        }
        // Look for echo of extended address command
        if (replyString.indexOf(":02000004") > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Found SPROG v4 bootloader");
            }
            statusBar.setText(Bundle.getMessage("StatusConnectedToBootloader", "4"));
            // Enable the file chooser button
            openFileChooserButton.setEnabled(true);
            // Force SPROG version
            sv = new SprogVersion(new SprogType(SprogType.SPROGV4), "");
            tc.setSprogState(SprogState.V4BOOTMODE);

            // We remain in this state until program button is pushed
        } else {
            log.error("Bad reply to RD_VER request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            return;
        }
    }

    @Override
    synchronized protected void stateWriteSent() {
        stopTimer();
        if (log.isDebugEnabled()) {
            log.debug("reply in WRITESENT state");
        }
        // Check for correct response to type of write that was sent
        if ((reply.getElement(reply.getNumDataElements() - 1) == '.')) {
            if (hexFile.read() > 0) {
                // More data to write
                sendWrite();
            } else {
                doneWriting();
            }
        } else {
            // Houston, we have a problem
            log.error("Bad reply to write request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            return;
        }
    }

    @Override
    synchronized protected void stateEofSent() {
        // v4 end of file sent
        stopTimer();
        // Check for correct response to end of file
        if (replyString.indexOf("S") > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Good reply in EOFSENT state");
            }
            bootState = BootState.V4RESET;
            statusBar.setText(Bundle.getMessage("StatusResetting"));
        } else {
            log.error("Bad reply in EOFSENT state");
            bootState = BootState.IDLE;
        }
        tc.setSprogState(SprogState.NORMAL);
    }

    @Override
    synchronized protected void stateV4Reset() {
        // v4 should have auto reset
        stopTimer();
        // Check for correct response to end of file
        if (replyString.indexOf("S") > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Good reply in V4RESET state");
            }
            statusBar.setText(Bundle.getMessage("StatusSuccess"));
            bootState = BootState.IDLE;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Bad reply in V4RESET state");
            }
        }
        tc.setSprogState(SprogState.NORMAL);
    }

    @Override
    synchronized protected void requestBoot() {
        // Look for SPROG in boot mode by sending an extended address command
        // which should be echoed
        log.debug("Request bootloader version");
        // allow parsing of bootloader replies
        tc.setSprogState(SprogState.V4BOOTMODE);
        bootState = BootState.VERREQSENT;
        msg = SprogMessage.getv4ExtAddr();
        tc.sendSprogMessage(msg, this);
        startLongTimer();
    }

    @Override
    synchronized protected void sendWrite() {
        if (hexFile.getAddress() < 2 * 0x700) {
            log.debug("Send write Flash {}", hexFile.getAddress());
            msg = SprogMessage.getV4WriteFlash(hexFile.getAddress(), hexFile.getData(), 0);
        } else {
            log.debug("null write {}", hexFile.getAddress());
            msg = null;
        }
        if (msg != null) {
            bootState = BootState.WRITESENT;
            statusBar.setText(Bundle.getMessage("StatusWriteX", hexFile.getAddress()));
            tc.sendSprogMessage(msg, this);
            log.debug("Sent write command to address {}", hexFile.getAddress());
            startLongTimer();
        } else {
            // use timeout to kick off the next write
            bootState = BootState.NULLWRITE;
            startVShortTimer();
        }
    }

    @Override
    synchronized protected void doneWriting() {
        // Finished
        log.debug("Done writing");
        statusBar.setText(Bundle.getMessage("StatusWriteComplete"));
        openFileChooserButton.setEnabled(false);
        programButton.setEnabled(false);

        // send end of file record
        log.debug("Send end of file ");
        msg = SprogMessage.getV4EndOfFile();
        bootState = BootState.EOFSENT;
        statusBar.setText(Bundle.getMessage("StatusWriteEOF"));
        tc.sendSprogMessage(msg, this);
        log.debug("End of file sent");
        startLongTimer();

        // *** Check for reset
    }

    @Override
    public synchronized void programButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (hexFile != null) {
            openFileChooserButton.setEnabled(false);
            programButton.setEnabled(false);
            // Read first line from hexfile
            if (hexFile.read() > 0) {
                // Program line and wait for reply
                log.debug("First write {} {}", hexFile.getLen(), hexFile.getAddress());
                sendWrite();
            } else {
                doneWriting();
            }
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(Sprogv4UpdateFrame.class);
}
