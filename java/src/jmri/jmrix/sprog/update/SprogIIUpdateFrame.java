package jmri.jmrix.sprog.update;

import static jmri.jmrix.sprog.SprogConstants.TC_BOOT_REPLY_TIMEOUT;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.swing.JOptionPane;
import jmri.jmrix.sprog.SprogConstants.SprogState;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for SPROG II firmware update utility.
 *
 * Extended to cover SPROG 3 which uses the same bootloader protocol Refactored
 *
 * @author	Andrew Crosland Copyright (C) 2004
 */
public class SprogIIUpdateFrame
        extends SprogUpdateFrame
        implements SprogVersionListener {

    public SprogIIUpdateFrame(SprogSystemConnectionMemo memo) {
        super(memo);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.update.SprogIIUpdateFrame", true);

        // Set a shorter timeout in the TC. Must be shorter than SprogUpdateFrame long timeout
        tc.setTimeout(TC_BOOT_REPLY_TIMEOUT);
        
        // Get the SPROG version
        _memo.getSprogVersionQuery().requestVersion(this);
    }

    int bootVer = 0;

    /** 
     * {@inheritDoc}
     * @param v SPROG version to be decoded
     */
    @SuppressFBWarnings(value = "SWL_SLEEP_WITH_LOCK_HELD")
    @Override
    synchronized public void notifyVersion(SprogVersion v) {
        sv = v;
        if (sv!=null && sv.sprogType.isSprog() == false) {
            // Didn't recognize a SPROG so check if it is in boot mode already
            if (log.isDebugEnabled()) {
                log.debug("SPROG not found - looking for bootloader");
            }
            statusBar.setText(Bundle.getMessage("StatusSprogNotFound"));
            blockLen = -1;
            requestBoot();
        } else {
            // Check that it's not a V4
            if (sv!=null && sv.sprogType.sprogType > SprogType.SPROGV4) {
                statusBar.setText(Bundle.getMessage("StatusFoundX", sv.toString()));
                blockLen = sv.sprogType.getBlockLen();
                // Put SPROG in boot mode
                if (log.isDebugEnabled()) {
                    log.debug("Putting SPROG in boot mode");
                }
                msg = new SprogMessage("b 1 1 1");
                bootState = BootState.SETBOOTSENT;
                tc.sendSprogMessage(msg, this);
                // SPROG II and 3 will not reply to this if successfull. Will
                // reply with error if firmware is locked. Wait a while to allow
                // Traffic Controller to time out
                startLongTimer();
            } else {
                log.error("Incorrect SPROG Type detected");
                statusBar.setText(Bundle.getMessage("StatusIncorrectSprogType"));
                bootState = BootState.IDLE;
            }
        }
    }

    @Override
    synchronized protected void frameCheck() {
        // If SPROG II is in boot mode, check message framing and checksum
        if ((bootState != BootState.RESETSENT) && tc.isSIIBootMode() && !reply.strip()) {
            stopTimer();
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorFrameDialogString"),
                    Bundle.getMessage("ErrorFrameDialogTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Malformed bootloader reply");
            statusBar.setText(Bundle.getMessage("StatusMalformedbootLoaderReply"));
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            return;
        }
        if ((bootState != BootState.RESETSENT) && tc.isSIIBootMode() && !reply.getChecksum()) {
            log.error("Bad bootloader checksum");
            statusBar.setText(Bundle.getMessage("StatusBadBootloaderChecksum"));
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
        }
    }

    @Override
    synchronized protected void stateSetBootSent() {
        stopTimer();
        log.debug("reply in SETBOOTSENT state");
        // A reply to the enter bootloader command means the firmware is locked.
        bootState = BootState.IDLE;
        tc.setSprogState(SprogState.NORMAL);
        JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorFirmwareLocked"),
                Bundle.getMessage("SprogXFirmwareUpdate"), JOptionPane.ERROR_MESSAGE);
        statusBar.setText(Bundle.getMessage("ErrorFirmwareLocked"));
    }
    
    @Override
    synchronized protected void stateBootVerReqSent() {
        stopTimer();
        if (log.isDebugEnabled()) {
            log.debug("reply in VERREQSENT state");
        }
        // see if reply is the version
        if ((reply.getOpCode() == SprogMessage.RD_VER) && (reply.getElement(1) == 2)) {
            bootVer = reply.getElement(2);
            if (log.isDebugEnabled()) {
                log.debug("Found bootloader version " + bootVer);
            }
            statusBar.setText(Bundle.getMessage("StatusConnectedToBootloader", bootVer));
            // Enable the file chooser button
            setSprogModeButton.setEnabled(true);
            openFileChooserButton.setEnabled(true);
            if (blockLen > 0) {
                // We think we already know the version
                if (blockLen != SprogType.getBlockLen(bootVer)) {
                    log.error("Bootloader version does not match SPROG type");
                    bootState = BootState.IDLE;
                }
            } else {
                // Don't yet have correct SPROG version
                if (bootVer <= 11) {
                    // Force SPROG version SPROG II 1.x or 2.x
                    sv = new SprogVersion(new SprogType(SprogType.SPROGII), "");
                } else {
                    // Force SPROG version SPROG SPROG II v3.x (also covers IIv4, SPROG 3 and Nano)
                    sv = new SprogVersion(new SprogType(SprogType.SPROGIIv3), "");
                }
                blockLen = sv.sprogType.getBlockLen();
                // We remain in this state until program button is pushed
            }
        } else {
            log.error("Bad reply to RD_VER request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            JOptionPane.showMessageDialog(this, Bundle.getMessage("StatusUnableToConnectBootloader"),
                    Bundle.getMessage("SprogXFirmwareUpdate"), JOptionPane.ERROR_MESSAGE);
            statusBar.setText(Bundle.getMessage("StatusUnableToConnectBootloader"));
        }
    }

    @Override
    synchronized protected void stateWriteSent() {
        stopTimer();
        if (log.isDebugEnabled()) {
            log.debug("reply in WRITESENT state");
        }
        // Check for correct response to type of write that was sent
        if ((reply.getOpCode() == msg.getElement(2)) && (reply.getNumDataElements() == 1)
                || (reply.getElement(reply.getNumDataElements() - 1) == '.')) {
            if (hexFile.read() > 0) {
                // More data to write
                sendWrite();
            } else {
                doneWriting();
            }
        } else {
            // Houston, we have a problem
//            JOptionPane.showMessageDialog(this, Bundle.getMessage("StatusBadReplyWriteRequest"),
//                                        Bundle.getMessage("SprogXFirmwareUpdate", " II"), JOptionPane.ERROR_MESSAGE);
            log.error("Bad reply to write request");
            statusBar.setText(Bundle.getMessage("StatusBadReplyWriteRequest"));
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
        }
    }

    @Override
    synchronized protected void stateEraseSent() {
        stopTimer();
        if (log.isDebugEnabled()) {
            log.debug("reply in ERASESENT state");
        }
        // Check for correct response to erase that was sent
        if ((reply.getOpCode() == msg.getElement(2)) && (reply.getNumDataElements() == 1)) {
            // Don't erase ICD debug executive if in use
            if ((sv.sprogType.sprogType < SprogType.SPROGIIv3) && (eraseAddress < 0x7c00)
                    || (sv.sprogType.sprogType >= SprogType.SPROGIIv3) && (eraseAddress < 0x3F00)) {
                // More data to erase
                sendErase();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Finished erasing");
                }
                statusBar.setText(Bundle.getMessage("StatusEraseComplete"));
                // Read first line from hexfile
                if (hexFile.read() > 0) {
                    // Program line and wait for reply
                    if (log.isDebugEnabled()) {
                        log.debug("First write {} {}", hexFile.getLen(), hexFile.getAddress());
                    }
                    sendWrite();
                } else {
                    doneWriting();
                }
            }
        } else {
            // Houston, we have a problem
//        JOptionPane.showMessageDialog(this, Bundle.getMessage("StatusBadReplyErase"),
//                                        Bundle.getMessage("SprogXFirmwareUpdate", " II"), JOptionPane.ERROR_MESSAGE);
            log.error("Bad reply to erase request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
        }
    }

    @Override
    synchronized protected void stateSprogModeSent() {
        stopTimer();
        if (log.isDebugEnabled()) {
            log.debug("reply in SROGMODESENT state");
        }
        // Check for correct response to type of write that was sent
        if ((reply.getOpCode() == msg.getElement(2)) && (reply.getNumDataElements() == 1)) {
            if (log.isDebugEnabled()) {
                log.debug("Reset SPROG");
            }
            msg = SprogMessage.getReset();
            bootState = BootState.RESETSENT;
            tc.sendSprogMessage(msg, this);
            startLongTimer();
        } else {
            // Houston, we have a problem
//        JOptionPane.showMessageDialog(this, Bundle.getMessage("StatusBadReplyModeRequest"),
//                                        Bundle.getMessage("SprogXFirmwareUpdate", " II"), JOptionPane.ERROR_MESSAGE);
            log.error("Bad reply to SPROG Mode request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
        }
    }

    @Override
    synchronized protected void stateResetSent() {
        stopTimer();
        if (log.isDebugEnabled()) {
            log.debug("reply in RESETSENT state");
        }
        // Check for correct response to type of write that was sent

        statusBar.setText(Bundle.getMessage("DefaultStatusText")); // Ready, is in jmrixBundle

        tc.setSprogState(SprogState.NORMAL);
        bootState = BootState.IDLE;
    }

    @Override
    synchronized protected void requestBoot() {
        // Look for SPROG in boot mode by requesting bootloader version.
        if (log.isDebugEnabled()) {
            log.debug("Request bootloader version");
        }
        // allow parsing of bootloader replies
        tc.setSprogState(SprogState.SIIBOOTMODE);
        bootState = BootState.VERREQSENT;
        msg = SprogMessage.getReadBootVersion();
        tc.sendSprogMessage(msg, this);
        startLongTimer();
    }

    @Override
    synchronized protected void sendWrite() {
        if ((hexFile.getAddressU()&0xFF) >= 0xF0) {
            // Write to EEPROM
            if (log.isDebugEnabled()) {
                log.debug("Send write EE " + hexFile.getAddress());
            }
            msg = SprogMessage.getWriteEE(hexFile.getAddress(), hexFile.getData());
        } else if ((hexFile.getAddressU()&0xFF) >= 0x20) {
            // Write to user data or config data not supported
            if (log.isDebugEnabled()) {
                log.debug("null write " + hexFile.getAddress());
            }
            msg = null;
        } else if (sv.sprogType.isValidFlashAddress(hexFile.getAddress())) {
            // Program code address is above bootloader range and below debug executive
            if (log.isDebugEnabled()) {
                log.debug("Send write Flash " + hexFile.getAddress());
            }
            msg = SprogMessage.getWriteFlash(hexFile.getAddress(), hexFile.getData(), blockLen);
            if (log.isDebugEnabled()) {
                log.debug(msg.toString(true));
            }
        } else {
            // Do nothing
            if (log.isDebugEnabled()) {
                log.debug("null write " + hexFile.getAddress());
            }
            msg = null;
        }
        if (msg != null) {
            bootState = BootState.WRITESENT;
            statusBar.setText(Bundle.getMessage("StatusWriteX", hexFile.getAddress()));
            tc.sendSprogMessage(msg, this);
            if (log.isDebugEnabled()) {
                log.debug("Sent write command to address " + hexFile.getAddress());
            }
            startLongTimer();
        } else {
            // use timeout to kick off the next write
            bootState = BootState.NULLWRITE;
            statusBar.setText(Bundle.getMessage("StatusSkipX", hexFile.getAddress()));
            startVShortTimer();
        }
    }

    synchronized private void sendErase() {
        if (log.isDebugEnabled()) {
            log.debug("Erase Flash " + eraseAddress);
        }
        int rows = 8; // 512 bytes
        msg = SprogMessage.getEraseFlash(eraseAddress, rows);
        bootState = BootState.ERASESENT;
        statusBar.setText(Bundle.getMessage("StatusEraseX", eraseAddress));
        tc.sendSprogMessage(msg, this);
        if (log.isDebugEnabled()) {
            log.debug("Sent erase command to address " + eraseAddress);
        }
        eraseAddress += (rows * 64);
        startLongTimer();
    }

    @Override
    synchronized protected void doneWriting() {
        // Finished
        if (log.isDebugEnabled()) {
            log.debug("Done writing");
        }
        statusBar.setText(Bundle.getMessage("StatusWriteComplete"));
        openFileChooserButton.setEnabled(false);
        programButton.setEnabled(false);

        setSprogModeButton.setEnabled(true);
        bootState = BootState.IDLE;
    }

    @Override
    synchronized public void programButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (hexFile != null) {
            openFileChooserButton.setEnabled(false);
            programButton.setEnabled(false);
            setSprogModeButton.setEnabled(false);
            eraseAddress = sv.sprogType.getEraseStart();
            if (eraseAddress > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Start erasing @" + eraseAddress);
                }
                sendErase();
            }
        }
    }

    @Override
    synchronized public void setSprogModeButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Set SPROG mode");
        }
        msg = SprogMessage.getWriteEE(0xff, new int[]{0});
        bootState = BootState.SPROGMODESENT;
        // Set TC timeout back to normal
        tc.resetTimeout();
        tc.sendSprogMessage(msg, this);
        startLongTimer();
    }

    private final static Logger log = LoggerFactory
            .getLogger(SprogIIUpdateFrame.class);
}
