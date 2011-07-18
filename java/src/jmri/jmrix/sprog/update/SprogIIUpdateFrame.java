// SprogIIUpdateFrame.java

package jmri.jmrix.sprog.update;

import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogConstants.SprogState;

import javax.swing.*;

/**
 * Frame for SPROG firmware update utility.
 *
 * Extended to cover SPROG 3 which uses the same bootloader protocol
 * 
 * @author			Andrew Crosland   Copyright (C) 2004
 * @version			$Revision$
 */
public class SprogIIUpdateFrame
    extends SprogUpdateFrame
    implements SprogListener {

  public SprogIIUpdateFrame() {
    super();
  }

    /**
     * Set the help item
     */
    public void initComponents() throws Exception {
      // add help menu to window
      addHelpMenu("package.jmri.jmrix.sprog.update.SprogIIUpdateFrame", true);
      super.initComponents();
    }
    
  public void notifyMessage(SprogMessage m) {}
  
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SWL_SLEEP_WITH_LOCK_HELD")
  // When we're doing a SPROG firmware update we are not too worried about performance
  // or other threads waiting to aquire the lock. This annotation should stop the
  // static analysis warning about the thread.sleep(500).
  synchronized public void notifyReply(SprogReply m) {
    // If SPROG II is in boot mode, check message framing and checksum
    if ( (bootState != RESETSENT) && tc.isSIIBootMode() && !m.strip()) {
      JOptionPane.showMessageDialog(this, "Malformed  bootloader reply", 
                                        "Connect to Bootloader", JOptionPane.ERROR_MESSAGE);
      log.error("Malformed bootloader reply");
      bootState = IDLE;
      tc.setSprogState(SprogState.NORMAL);
      return;
    }
    if ( (bootState != RESETSENT) && tc.isSIIBootMode() && !m.getChecksum()) {
//      JOptionPane.showMessageDialog(this, "Bad Bootloader Checksum", 
//                                        "Connect to Bootloader", JOptionPane.ERROR_MESSAGE);
      log.error("Bad bootloader checksum");
      bootState = IDLE;
      tc.setSprogState(SprogState.NORMAL);
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
      msg.setOpCode('?');
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
        sprogVersionString = replyString.substring(replyString.indexOf(".") -
            1, replyString.indexOf(".") + 2);
        if (replyString.indexOf("II") >= 0) {
          sprogTypeString = "SPROG II v" + sprogVersionString;
          sprogTypeInt = 2;
          blockLen = 8;
        } else if (replyString.indexOf("3") >= 0) {
          sprogTypeString = "SPROG 3 v" + sprogVersionString;
          sprogTypeInt = 3;
          blockLen = 16;
        } else {
          // *** problem
        }
        if (log.isDebugEnabled()) {
          log.debug("Found " + sprogTypeString);
        }
        statusBar.setText("Found " + sprogTypeString);
        // Put SPROG in boot mode
        if (log.isDebugEnabled()) {
          log.debug("Putting SPROG in boot mode");
        }
        msg = new SprogMessage("b 1 1 1");
        tc.sendSprogMessage(msg, this);
        if ((sprogTypeInt == 2) || (sprogTypeInt == 3)) {
          // SPROG II and 3 will not reply to this so just wait a while
          tc.setSprogState(SprogState.SIIBOOTMODE);
          try {
            Thread.sleep(500);
          }
          catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
          }
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
        openFileChooserButton.setEnabled(true);
        if (bootVer.indexOf("11.0") >= 0) {
          sprogTypeString = "SPROG II";
          sprogTypeInt = 2;
          blockLen = 8;
        } else {
          sprogTypeString = "SPROG 3";
          sprogTypeInt = 3;
          blockLen = 16;
        }
        // We remain in this state until program button is pushed

      }
      else {
        log.error("Bad reply to RD_VER request");
        bootState = IDLE;
        tc.setSprogState(SprogState.NORMAL);
        JOptionPane.showMessageDialog(this, "Unable to connect to bootloader", 
                                        "Connect to Bootloader", JOptionPane.ERROR_MESSAGE);
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
        if ((sprogTypeInt == 2) && (eraseAddress < 0x7c00)
                || (sprogTypeInt == 3) && (eraseAddress < 0x3E00)) {
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
//        JOptionPane.showMessageDialog(this, "Bad reply to erase command", 
//                                        "SPROG II Bootloader", JOptionPane.ERROR_MESSAGE);
        log.error("Bad reply to erase request");
        bootState = IDLE;
        tc.setSprogState(SprogState.NORMAL);
        return;
      }
    }
    else if (bootState == WRITESENT) {
      stopTimer();
      if (log.isDebugEnabled()) {
        log.debug("reply in WRITESENT state");
      }
      // Check for correct response to type of write that was sent
      if (((sprogTypeInt == 2) || (sprogTypeInt == 3))
              && (m.getOpCode() == msg.getElement(2)) && (m.getNumDataElements() == 1)
          || (m.getElement(m.getNumDataElements() - 1) == '.')) {
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
//        JOptionPane.showMessageDialog(this, "Bad reply to write command", 
//                                        "SPROG II Bootloader", JOptionPane.ERROR_MESSAGE);
        log.error("Bad reply to write request");
        bootState = IDLE;
        tc.setSprogState(SprogState.NORMAL);
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
        msg = SprogMessage.getReset();
        bootState = RESETSENT;
        tc.sendSprogMessage(msg, this);
        startLongTimer();
      }
      else {
        // Houston, we have a problem
//        JOptionPane.showMessageDialog(this, "Bad reply to SPROG mode request", 
//                                        "SPROG II Bootloader", JOptionPane.ERROR_MESSAGE);
        log.error("Bad reply to SPROG Mode request");
        bootState = IDLE;
        tc.setSprogState(SprogState.NORMAL);
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

      tc.setSprogState(SprogState.NORMAL);
      bootState = IDLE;
    }
    else {
      // Houston, we have a problem
      if (log.isDebugEnabled()) {
        log.debug("Reply in unknown state");
      }
      bootState = IDLE;
      tc.setSprogState(SprogState.NORMAL);
      return;
    }
  }

  private void requestBoot() {
    // Look for SPROG in boot mode by requesting bootloader version.
    if (log.isDebugEnabled()) {
      log.debug("Request bootloader version");
    }
    // allow parsing of bootloader replies
    tc.setSprogState(SprogState.SIIBOOTMODE);
    bootState = VERREQSENT;
    msg = SprogMessage.getReadBootVersion();
    tc.sendSprogMessage(msg, this);
    startLongTimer();
  }

  private void sendWrite() {
    if (hexFile.getAddressU() >= 0xF0) {
      // Write to EEPROM
      if (log.isDebugEnabled()) {
        log.debug("Send write EE " + hexFile.getAddress());
      }
      msg = SprogMessage.getWriteEE(hexFile.
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
    else if (((sprogTypeInt == 2) && (hexFile.getAddress() >= 0x200))
             || ((sprogTypeInt == 3) && ((hexFile.getAddress() >= 0x2000)
                                                    && (hexFile.getAddress() < 0x3E00)))) {
      // Program code address is above bootloader range and below debug executive
      if (log.isDebugEnabled()) {
        log.debug("Send write Flash " + hexFile.getAddress());
      }
      msg = SprogMessage.getWriteFlash(hexFile.getAddress(),
                                             hexFile.getData(), blockLen);

      if (log.isDebugEnabled()) { log.debug(msg.toString()); }
      
    }
    else {
      // Do nothing
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
    msg = SprogMessage.getEraseFlash(eraseAddress,
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
    bootState = IDLE;
  }

  public synchronized void connectButtonActionPerformed(java.awt.event.
      ActionEvent e) {
    tc.setSprogState(SprogState.NORMAL);
    sprogTypeString = null;
    // At this point we do not know what sort of SPROG is connected
    // nor what state it is in
    // send CR to attempt to wake up SPROG

    // *** implicitly assume sprog II

    msg = new SprogMessage(1);
    msg.setOpCode(' ');
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
      if (sprogTypeInt < 3) {
        // SPROG II
        // Erase device above bootloader
        eraseAddress = 0x200;
        sendErase();
      }
      else if (sprogTypeInt == 3) {
        // SPROG 3
        // Erase device above bootloader
        eraseAddress = 0x2000;
        sendErase();
      }
      else {
        // *** Error
      }
    }
  }

  synchronized public void setSprogModeButtonActionPerformed(java.awt.event.
                                                ActionEvent e) {
    if (log.isDebugEnabled()) {
      log.debug("Set SPROG mode");
    }
    msg = SprogMessage.getWriteEE(0xff, new int[] {0});
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
        // we were looking for a SPROG II or 3 in normal mode but have had no reply
        // so maybe it was already in boot mode.
        // Try looking for bootloader version
      }
      requestBoot();
    }
    else if (bootState == VERREQSENT) {
      log.error("timeout in VERREQSENT!");
      JOptionPane.showMessageDialog(this, "Unable to connect to bootloader",
                                    "Fatal Error", JOptionPane.ERROR_MESSAGE);
      statusBar.setText("Fatal error - unable to connect");
      bootState = IDLE;
      tc.setSprogState(SprogState.NORMAL);
    }
    else if (bootState == WRITESENT) {
      log.error("timeout in WRITESENT!");
      // This is fatal!
      JOptionPane.showMessageDialog(this, "Timeout during write",
                                    "Fatal Error", JOptionPane.ERROR_MESSAGE);
      statusBar.setText("Fatal error - unable to write");
      bootState = IDLE;
      tc.setSprogState(SprogState.NORMAL);
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

  static org.apache.log4j.Logger log = org.apache.log4j.Logger
  .getLogger(SprogIIUpdateFrame.class.getName());

}
