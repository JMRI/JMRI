// SprogIIUpdateFrame.java

package jmri.jmrix.sprog.update;

import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogTrafficController;

import java.io.File;
import javax.swing.*;

/**
 * Frame for SPROG firmware update utility
 * @author			Andrew Crosland   Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public class SprogIIUpdateFrame
    extends SprogUpdateFrame
    implements SprogListener {

  public SprogIIUpdateFrame() {
    super();
  }

  synchronized public void reply(SprogReply m) {
    // If SPROG II is in boot mode, check message framing and checksum
    if ( (bootState != RESETSENT) && tc.isSIIBootMode() && !m.strip()) {
      log.error("Malformed bootloader reply");
      bootState = IDLE;
      tc.setSprogState(tc.NORMAL);
      return;
    }
    if ( (bootState != RESETSENT) && tc.isSIIBootMode() && !m.getChecksum()) {
      log.error("Bad bootloader checksum");
      bootState = IDLE;
      tc.setSprogState(tc.NORMAL);
      return;
    }
    replyString = m.toString();
    if (bootState == IDLE) {
      if (log.isDebugEnabled()) {
        log.debug("reply in IDLE state");
      }
      return;
    }
    else if (bootState == CRSENT) {
      stopTimer();
      if (log.isDebugEnabled()) {
        log.debug("reply in CRSENT state");
      }
      if ( (replyString.indexOf("P>")) >= 0) {
        if (log.isDebugEnabled()) {
          log.debug("Found SPROG prompt");
        }
        statusBar.setText("Found SPROG Prompt");
      }
      // Send ? to look for SPROG version regardless of what we found this time
      msg = new SprogMessage(1);
      msg.setOpCode( (int) '?');
      tc.sendSprogMessage(msg, this);
      bootState = QUERYSENT;
    }
    else if (bootState == QUERYSENT) {
      if (log.isDebugEnabled()) {
        log.debug("reply in QUERYSENT state");
      }
      // see if reply is from a SPROG
      if (replyString.indexOf("SPROG") < 0) {
        if (log.isDebugEnabled()) {
          log.debug("SPROG not found - looking for bootloader");
        }
        statusBar.setText("SPROG not found - looking for bootloader");
        // Maybe it's already in boot mode - try for bootloader version
        requestBoot();
      }
      else {
        sprogVersion = new String(replyString.substring(replyString.indexOf(".") -
            1, replyString.indexOf(".") + 2));
        if (replyString.indexOf("II") >= 0) {
          sprogType = "SPROG II v" + sprogVersion;
        }
        else {
          // *** problem
        }
        if (log.isDebugEnabled()) {
          log.debug("Found " + sprogType);
        }
        statusBar.setText("Found " + sprogType);
        // Put SPROG in boot mode
        if (log.isDebugEnabled()) {
          log.debug("Putting SPROG in boot mode");
        }
        msg = new SprogMessage("b 1 1 1");
        tc.sendSprogMessage(msg, this);
        if (sprogType.indexOf("II") > 0) {
          // SPROG II will not reply to this so just wait a while
          tc.setSprogState(tc.SIIBOOTMODE);
          try {
            Thread.sleep(500);
          }
          catch (InterruptedException e) {}
          ;
          // Look for bootloader version
          requestBoot();
        }
      }
    }
    else if (bootState == VERREQSENT) {
      stopTimer();
      if (log.isDebugEnabled()) {
        log.debug("reply in VERREQSENT state");
      }
      // see if reply is the version
      if ( (m.getOpCode() == SprogMessage.RD_VER) && (m.getElement(1) == 2)) {
        String bootVer = "" + m.getElement(2) + "." + m.getElement(3);
        if (log.isDebugEnabled()) {
          log.debug("Found bootloader version " + bootVer);
        }
        statusBar.setText("Connected to bootloader version " + bootVer);
        // Enable the file chooser button
        setSprogModeButton.setEnabled(true);
        setCSModeButton.setEnabled(true);
        openFileChooserButton.setEnabled(true);
        if (sprogType == null) {
          sprogType = new String("SPROG II");
        }
        // We remain in this state until program button is pushed

      }
      else {
        log.error("Bad reply to RD_VER request");
        bootState = IDLE;
        tc.setSprogState(tc.NORMAL);
        return;
      }
    }
    else if (bootState == ERASESENT) {
      stopTimer();
      if (log.isDebugEnabled()) {
        log.debug("reply in ERASESENT state");
      }
      // Check for correct response to erase that was sent
      if ( (m.getOpCode() == msg.getElement(2)) && (m.getNumDataElements() == 1)) {
        // Don't erase ICD debug executive if in use
        if (eraseAddress < 0x7c00) {
          // More data to erase
          sendErase();
        }
        else {
          if (log.isDebugEnabled()) {
            log.debug("Finished erasing");
          }
          statusBar.setText("Erase Complete");
          // Read first line from hexfile
          if (hexFile.read() > 0) {
            // Program line and wait for reply
            if (log.isDebugEnabled()) {
              log.debug("First write " + hexFile.getLen() + " " +
                        hexFile.getAddress());
            }
            sendWrite();
          }
          else {
            doneWriting();
          }
        }
      }
      else {
        // Houston, we have a problem
        log.error("Bad reply to erase request");
        bootState = IDLE;
        tc.setSprogState(tc.NORMAL);
        return;
      }
    }
    else if (bootState == WRITESENT) {
      stopTimer();
      if (log.isDebugEnabled()) {
        log.debug("reply in WRITESENT state");
      }
      // Check for correct response to type of write that was sent
      if ( (sprogType.indexOf("II") >= 0) && (m.getOpCode() == msg.getElement(2)) &&
          (m.getNumDataElements() == 1)
          || (m.getElement(m.getNumDataElements() - 1) == (int) '.')) {
        if (hexFile.read() > 0) {
          // More data to write
          sendWrite();
        }
        else {
          doneWriting();
        }
      }
      else {
        // Houston, we have a problem
        log.error("Bad reply to write request");
        bootState = IDLE;
        tc.setSprogState(tc.NORMAL);
        return;
      }
    }
    else if (bootState == SPROGMODESENT) {
      stopTimer();
      if (log.isDebugEnabled()) {
        log.debug("reply in SROGMODESENT state");
      }
      // Check for correct response to type of write that was sent
      if ( (m.getOpCode() == msg.getElement(2)) && (m.getNumDataElements() == 1)) {
        if (log.isDebugEnabled()) {
          log.debug("Reset SPROG");
        }
        msg = new SprogMessage(SprogMessage.MAXSIZE).getReset();
        bootState = RESETSENT;
        tc.sendSprogMessage(msg, this);
        startLongTimer();
      }
      else {
        // Houston, we have a problem
        log.error("Bad reply to SPROG Mode request");
        bootState = IDLE;
        tc.setSprogState(tc.NORMAL);
        return;
      }
    }
    else if (bootState == RESETSENT) {
      stopTimer();
      if (log.isDebugEnabled()) {
        log.debug("reply in RESETSENT state");
      }
      // Check for correct response to type of write that was sent

      statusBar.setText("Ready");

      tc.setSprogState(tc.NORMAL);
      bootState = IDLE;
    }
    else {
      // Houston, we have a problem
      if (log.isDebugEnabled()) {
        log.debug("Reply in unknown state");
      }
      bootState = IDLE;
      tc.setSprogState(tc.NORMAL);
      return;
    }
  }

  private void requestBoot() {
    // Look for SPROG in boot mode by requesting bootloader version.
    if (log.isDebugEnabled()) {
      log.debug("Request bootloader version");
    }
    // allow parsing of bootloader replies
    tc.setSprogState(tc.SIIBOOTMODE);
    bootState = VERREQSENT;
    msg = SprogMessage.getReadBootVersion();
    tc.sendSprogMessage(msg, this);
    startLongTimer();
  }

  private void sendWrite() {
    if (hexFile.getAddressU() >= 0xF0) {
      if (log.isDebugEnabled()) {
        log.debug("Send write EE " + hexFile.getAddress());
      }
      msg = new SprogMessage(SprogMessage.MAXSIZE).getWriteEE(hexFile.
          getAddress(),
          hexFile.getData());
    }
    else if (hexFile.getAddressU() >= 0x30) {
      // Write to user data not supported
      if (log.isDebugEnabled()) {
        log.debug("null write " + hexFile.getAddress());
      }
      msg = null;
    }
    else if (hexFile.getAddressU() >= 0x20) {
      // Write to config data not supported
      if (log.isDebugEnabled()) {
        log.debug("null write " + hexFile.getAddress());
      }
      msg = null;
    }
    else if (hexFile.getAddress() >= 0x200) {
      // Address is above bootloader range
      if (log.isDebugEnabled()) {
        log.debug("Send write Flash " + hexFile.getAddress());
      }
      msg = new SprogMessage(SprogMessage.MAXSIZE
                             ).getWriteFlash(hexFile.getAddress(),
                                             hexFile.getData());
    }
    else {
      if (log.isDebugEnabled()) {
        log.debug("null write " + hexFile.getAddress());
      }
      msg = null;
    }
    if (msg != null) {
      bootState = WRITESENT;
      statusBar.setText("Write " + hexFile.getAddress());
      tc.sendSprogMessage(msg, this);
      if (log.isDebugEnabled()) {
        log.debug("Sent write command to address " + hexFile.getAddress());
      }
      startLongTimer();
    }
    else {
      // use timeout to kick off the next write
      bootState = NULLWRITE;
      startVShortTimer();
    }
  }

  private void sendErase() {
    if (log.isDebugEnabled()) {
      log.debug("Erase Flash " + eraseAddress);
    }
    int rows = 8; // 512 bytes
    msg = new SprogMessage(SprogMessage.MAXSIZE).getEraseFlash(eraseAddress,
        rows);
    bootState = ERASESENT;
    statusBar.setText("Erase " + eraseAddress);
    tc.sendSprogMessage(msg, this);
    if (log.isDebugEnabled()) {
      log.debug("Sent erase command to address " + eraseAddress);
    }
    eraseAddress += (rows * 64);
    startLongTimer();
  }

  private void doneWriting() {
    // Finished
    if (log.isDebugEnabled()) {
      log.debug("Done writing");
    }
    statusBar.setText("Write Complete");
    connectButton.setEnabled(true);
    openFileChooserButton.setEnabled(false);
    programButton.setEnabled(false);

    setSprogModeButton.setEnabled(true);
    setCSModeButton.setEnabled(true);
    bootState = IDLE;
  }

  public void initComponents() throws Exception {
    super.initComponents();
  }

  // Close the window when the close box is clicked
  void thisWindowClosing(java.awt.event.WindowEvent e) {
    setVisible(false);
    dispose();
  }

  public synchronized void connectButtonActionPerformed(java.awt.event.
      ActionEvent e) {
    tc.setSprogState(tc.NORMAL);
    sprogType = null;
    // At this point we do not know what sort of SPROG is connected
    // nor what state it is in
    // send CR to attempt to wake up SPROG

    // *** implicitly assume sprog II

    msg = new SprogMessage(1);
    msg.setOpCode( (int) ' ');
    tc.sendSprogMessage(msg, this);
    bootState = CRSENT;
    startShortTimer();
  }

  public synchronized void programButtonActionPerformed(java.awt.event.
      ActionEvent e) {
    if (hexFile != null) {
      connectButton.setEnabled(false);
      openFileChooserButton.setEnabled(false);
      programButton.setEnabled(false);
      setSprogModeButton.setEnabled(false);
      setCSModeButton.setEnabled(false);
      if ( (sprogType == null) || (sprogType.indexOf("II") > 0)) {
        // SPROG II
        // Erase device above bootloader
        eraseAddress = 0x200;
        sendErase();
      }
      else {
        // v4
        // Read first line from hexfile
        if (hexFile.read() > 0) {
          // Program line and wait for reply
          if (log.isDebugEnabled()) {
            log.debug("First write " + hexFile.getLen() + " " +
                      hexFile.getAddress());
          }
          sendWrite();
        }
        else {
          doneWriting();
        }
      }
    }
  }

  public void setSprogModeButtonActionPerformed(java.awt.event.
                                                ActionEvent e) {
    if (log.isDebugEnabled()) {
      log.debug("Set SPROG mode");
    }
    msg = new SprogMessage(SprogMessage.MAXSIZE).getWriteEE(0xff, new int[] {0});
    bootState = SPROGMODESENT;
    tc.sendSprogMessage(msg, this);
    startLongTimer();
  }

  public void setCSModeButtonActionPerformed(java.awt.event.
                                             ActionEvent e) {
    if (log.isDebugEnabled()) {
      log.debug("Set SPROG mode");
    }
    msg = new SprogMessage(SprogMessage.MAXSIZE).getWriteEE(0xfe, new int[] {0});
    bootState = SPROGMODESENT;
    tc.sendSprogMessage(msg, this);
    startLongTimer();
  }

  /**
   * Internal routine to handle a timeout
   */
  synchronized protected void timeout() {
    if (bootState == CRSENT) {
      if (log.isDebugEnabled()) {
        log.debug("timeout in CRSENT - assuming boot mode");
        // we were looking for a SPROG II in normal mode but have had no reply
        // so maybe it was already in boot mode.
        // Try looking for bootloader version
      }
      requestBoot();
    }
    else if (bootState == VERREQSENT) {
      if (log.isDebugEnabled()) {
        log.debug("timeout in VERREQSENT!");
        // This is fatal!
      }
      statusBar.setText("Fatal error - unable to connect");
      bootState = IDLE;
      tc.setSprogState(tc.NORMAL);
    }
    else if (bootState == WRITESENT) {
      log.error("timeout in WRITESENT!");
      // This is fatal!
      statusBar.setText("Fatal error - unable to write");
      bootState = IDLE;
      tc.setSprogState(tc.NORMAL);
    }
    else if (bootState == NULLWRITE) {
      if (hexFile.read() > 0) {
        // More data to write
        sendWrite();
      }
      else {
        doneWriting();
      }
    }
  }

  static org.apache.log4j.Category log = org.apache.log4j.Category.
      getInstance(SprogIIUpdateFrame.class.getName());

}
