// SprogIIUpdateFrame.java
package jmri.jmrix.sprog.update;

import javax.swing.JOptionPane;
import jmri.jmrix.sprog.SprogConstants.SprogState;
import jmri.jmrix.sprog.SprogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for SPROG firmware update utility.
 *
 * Extended to cover SPROG 3 which uses the same bootloader protocol Refactored
 *
 * @author	Andrew Crosland Copyright (C) 2004
 * @version	$Revision$
 */
public class SprogIIUpdateFrame
        extends SprogUpdateFrame
        implements SprogVersionListener {

    /**
     *
     */
    private static final long serialVersionUID = 4424302689786420208L;

    public SprogIIUpdateFrame() {
        super();
    }

    /**
     * Set the help item
     */
    public void initComponents() throws Exception {
        super.initComponents();

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.update.SprogIIUpdateFrame", true);

        // Get the SPROG version
        SprogVersionQuery.requestVersion(this);
    }

    int bootVer = 0;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SWL_SLEEP_WITH_LOCK_HELD")
    synchronized public void notifyVersion(SprogVersion v) {
        sv = v;
        if (sv.sprogType.isSprog() == false) {
            // Didn't recognize a SPROG so check if it is in boot mode already
            if (log.isDebugEnabled()) {
                log.debug("SPROG not found - looking for bootloader");
            }
            statusBar.setText("SPROG not found - looking for bootloader");
            blockLen = -1;
            requestBoot();
        } else {
            // Check that it's not a V4
            if (sv.sprogType.sprogType > SprogType.SPROGV4) {
                statusBar.setText("Found " + sv.toString());
                blockLen = sv.sprogType.getBlockLen();
                // Put SPROG in boot mode
                if (log.isDebugEnabled()) {
                    log.debug("Putting SPROG in boot mode");
                }
                msg = new SprogMessage("b 1 1 1");
                tc.sendSprogMessage(msg, this);
                // SPROG II and 3 will not reply to this so just wait a while
                tc.setSprogState(SprogState.SIIBOOTMODE);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                }
                // Look for bootloader version
                requestBoot();
            } else {
                log.error("Incorrect SPROG Type detected");
                statusBar.setText("Incorrect SPROG Type detected");
                bootState = BootState.IDLE;
            }
        }
    }

    synchronized protected void frameCheck() {
        // If SPROG II is in boot mode, check message framing and checksum
        if ((bootState != BootState.RESETSENT) && tc.isSIIBootMode() && !reply.strip()) {
            stopTimer();
            JOptionPane.showMessageDialog(this, "Malformed  bootloader reply/nDid you remember to unlock the firmware?",
                    "Connect to Bootloader", JOptionPane.ERROR_MESSAGE);
            log.error("Malformed bootloader reply");
            statusBar.setText("Malformedboot loader reply");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            return;
        }
        if ((bootState != BootState.RESETSENT) && tc.isSIIBootMode() && !reply.getChecksum()) {
            log.error("Bad bootloader checksum");
            statusBar.setText("Bad bootloader checksum");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
        }
    }

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
            statusBar.setText("Connected to bootloader version " + bootVer);
            // Enable the file chooser button
            setSprogModeButton.setEnabled(true);
            openFileChooserButton.setEnabled(true);
            if (blockLen > 0) {
                // We think we already know the version
                if (blockLen != SprogType.getBlockLen(bootVer)) {
                    log.error("Bootloader version does not match SPROG type");
                    bootState = BootState.IDLE;
                    return;
                }
            } else {
                // Don't yet have correct SPROG version
                if (bootVer <= 11) {
                    // Force SPROG version SPROG II 1.x or 2.x
                    sv = new SprogVersion(new SprogType(SprogType.SPROGII), "");
                } else {
                    // Force SPROG version SPROG SPROG II v3.x (also covers SPROG 3 and Nano)
                    sv = new SprogVersion(new SprogType(SprogType.SPROGIIv3), "");
                }
                blockLen = sv.sprogType.getBlockLen();
                // We remain in this state until program button is pushed
            }
        } else {
            log.error("Bad reply to RD_VER request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            JOptionPane.showMessageDialog(this, "Unable to connect to bootloader",
                    "Connect to Bootloader", JOptionPane.ERROR_MESSAGE);
            statusBar.setText("Unable to connect to bootloader");
            return;
        }
    }

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
//            JOptionPane.showMessageDialog(this, "Bad reply to write command",
//                                        "SPROG II Bootloader", JOptionPane.ERROR_MESSAGE);
            log.error("Bad reply to write request");
            statusBar.setText("Bad reply to write request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            return;
        }
    }

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
                statusBar.setText("Erase Complete");
                // Read first line from hexfile
                if (hexFile.read() > 0) {
                    // Program line and wait for reply
                    if (log.isDebugEnabled()) {
                        log.debug("First write " + hexFile.getLen() + " " + hexFile.getAddress());
                    }
                    sendWrite();
                } else {
                    doneWriting();
                }
            }
        } else {
            // Houston, we have a problem
//        JOptionPane.showMessageDialog(this, "Bad reply to erase command",
//                                        "SPROG II Bootloader", JOptionPane.ERROR_MESSAGE);
            log.error("Bad reply to erase request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            return;
        }
    }

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
//        JOptionPane.showMessageDialog(this, "Bad reply to SPROG mode request",
//                                        "SPROG II Bootloader", JOptionPane.ERROR_MESSAGE);
            log.error("Bad reply to SPROG Mode request");
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
            return;
        }
    }

    synchronized protected void stateResetSent() {
        stopTimer();
        if (log.isDebugEnabled()) {
            log.debug("reply in RESETSENT state");
        }
        // Check for correct response to type of write that was sent

        statusBar.setText("Ready");

        tc.setSprogState(SprogState.NORMAL);
        bootState = BootState.IDLE;
    }

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

    synchronized protected void sendWrite() {
        if (hexFile.getAddressU() >= 0xF0) {
            // Write to EEPROM
            if (log.isDebugEnabled()) {
                log.debug("Send write EE " + hexFile.getAddress());
            }
            msg = SprogMessage.getWriteEE(hexFile.getAddress(), hexFile.getData());
        } else if (hexFile.getAddressU() >= 0x20) {
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
                log.debug(msg.toString());
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
            statusBar.setText("Write " + hexFile.getAddress());
            tc.sendSprogMessage(msg, this);
            if (log.isDebugEnabled()) {
                log.debug("Sent write command to address " + hexFile.getAddress());
            }
            startLongTimer();
        } else {
            // use timeout to kick off the next write
            bootState = BootState.NULLWRITE;
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
        statusBar.setText("Erase " + eraseAddress);
        tc.sendSprogMessage(msg, this);
        if (log.isDebugEnabled()) {
            log.debug("Sent erase command to address " + eraseAddress);
        }
        eraseAddress += (rows * 64);
        startLongTimer();
    }

    synchronized protected void doneWriting() {
        // Finished
        if (log.isDebugEnabled()) {
            log.debug("Done writing");
        }
        statusBar.setText("Write Complete");
        openFileChooserButton.setEnabled(false);
        programButton.setEnabled(false);

        setSprogModeButton.setEnabled(true);
        bootState = BootState.IDLE;
    }

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

    synchronized public void setSprogModeButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Set SPROG mode");
        }
        msg = SprogMessage.getWriteEE(0xff, new int[]{0});
        bootState = BootState.SPROGMODESENT;
        tc.sendSprogMessage(msg, this);
        startLongTimer();
    }

    private final static Logger log = LoggerFactory
            .getLogger(SprogIIUpdateFrame.class.getName());
}
