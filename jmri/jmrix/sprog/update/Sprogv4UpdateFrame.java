// Sprogv4UpdateFrame.java

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
public class Sprogv4UpdateFrame
    extends SprogUpdateFrame
    implements SprogListener {

  public Sprogv4UpdateFrame() {
    super();
  }

  synchronized public void reply(SprogReply m) {
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
        // Maybe it's already in boot mode - look for bootloader
        requestBoot();
      }
      else {
        sprogVersion = new String(replyString.substring(replyString.indexOf(".") -
            1, replyString.indexOf(".") + 2));
        sprogType = "SPROG v" + sprogVersion;
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
        // SPROG v4 will reply
        bootState = SETBOOTSENT;
      }
    }
    else if (bootState == SETBOOTSENT) {
      // Only old SPROG v4 reach this state
      if (log.isDebugEnabled()) {
        log.debug("reply in SETBOOTSENT state");
      }
      if (replyString.indexOf("L>") >= 0) {
        // Enable the file chooser button
        if (log.isDebugEnabled()) {
          log.debug("Found v4 bootloader prompt");
        }
        tc.setSprogState(tc.V4BOOTMODE);
        openFileChooserButton.setEnabled(true);

        // We remain in this state until program button is pushed

      }
      else {
        log.error("Bad reply to SETBOOT request");
        bootState = IDLE;
        tc.setSprogState(tc.NORMAL);
        return;
      }
    }
    else if (bootState == VERREQSENT) {
      stopTimer();
      if (log.isDebugEnabled()) { log.debug("reply in VERREQSENT state " + replyString);}
      // Look for echo of extended address command
      if ( replyString.indexOf(":02000004") > 0 ) {
        if (log.isDebugEnabled()) {log.debug("Found SPROG v4 bootloader");}
        statusBar.setText("Connected to SPROG v4 bootloader");
        // Enable the file chooser button
        openFileChooserButton.setEnabled(true);
        if (sprogType == null) {
          sprogType = new String("SPROG");
        }
        tc.setSprogState(tc.V4BOOTMODE);

        // We remain in this state until program button is pushed

      }
      else {
        log.error("Bad reply to RD_VER request");
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
      if ( (m.getElement(m.getNumDataElements() - 1) == (int) '.')) {
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
    else if (bootState == EOFSENT) {
      // v4 end of file sent
      stopTimer();
      // Check for correct response to end of file
      if (m.toString().indexOf("S") > 0) {
        if (log.isDebugEnabled()) {
          log.debug("Good reply in EOFSENT state");
        }
        bootState = V4RESET;
        statusBar.setText("Resetting");
      }
      else {
        log.error("Bad reply in EOFSENT state");

        bootState = IDLE;
      }
      tc.setSprogState(tc.NORMAL);
    }
    else if (bootState == V4RESET) {
      // v4 should have auto reset
      stopTimer();
      // Check for correct response to end of file
      if (m.toString().indexOf("S") > 0) {
        if (log.isDebugEnabled()) {
          log.debug("Good reply in V4RESET state");
        }
        statusBar.setText("Success!");
        bootState = IDLE;
      }
      else {
        if (log.isDebugEnabled()) {
          log.debug("Bad reply in V4RESET state");
        }

      }

      tc.setSprogState(tc.NORMAL);
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
    // Look for SPROG in boot mode by sending an extended address command
    // which should be echoed
    if (log.isDebugEnabled()) {
      log.debug("Request bootloader version");
    }
    // allow parsing of bootloader replies
    tc.setSprogState(tc.V4BOOTMODE);
    bootState = VERREQSENT;
    msg = SprogMessage.getv4ExtAddr();
    tc.sendSprogMessage(msg, this);
    startLongTimer();
  }

  private void sendWrite() {
    if (hexFile.getAddress() < 2 * 0x700) {
      //
      if (log.isDebugEnabled()) {
        log.debug("Send write Flash " + hexFile.getAddress());
      }
      msg = new SprogMessage(SprogMessage.MAXSIZE
                             ).getV4WriteFlash(hexFile.getAddress(),
                                               hexFile.getData(), 0);
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

  private void doneWriting() {
    // Finished
    if (log.isDebugEnabled()) {
      log.debug("Done writing");
    }
    statusBar.setText("Write Complete");
    connectButton.setEnabled(true);
    openFileChooserButton.setEnabled(false);
    programButton.setEnabled(false);

    // send end of file record
    if (log.isDebugEnabled()) {
      log.debug("Send end of file ");
    }
    msg = new SprogMessage(SprogMessage.MAXSIZE).getV4EndOfFile();
    bootState = EOFSENT;
    statusBar.setText("Write End Of File");
    tc.sendSprogMessage(msg, this);
    if (log.isDebugEnabled()) {
      log.debug("Sent end of file ");
    }
    startLongTimer();

    // *** Check for reset
  }

  public void initComponents() throws Exception {
    super.initComponents();

    setSprogModeButton.setVisible(false);

    setCSModeButton.setVisible(false);

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
    // At this point we do not know what state SPROG is in
    // send CR to attempt to wake it up
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

  /**
   * Internal routine to handle a timeout
   */
  synchronized protected void timeout() {
    if (bootState == CRSENT) {
      if (log.isDebugEnabled()) {
        log.debug("timeout in CRSENT - assuming boot mode");
        // we were looking for a SPROG v4 in normal mode but have had no reply
        // so maybe it was already in boot mode.
        // Try looking for bootloader
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
      getInstance(Sprogv4UpdateFrame.class.getName());

}
