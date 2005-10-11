// SprogUpdateFrame.java

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
 * @version			$Revision: 1.2 $
 */
public class SprogUpdateFrame
    extends JFrame
    implements SprogListener {

  // member declarations
  protected JButton connectButton = new JButton();
  protected JButton programButton = new JButton();
  protected JButton openFileChooserButton = new JButton();
  protected JButton setSprogModeButton = new JButton();
  protected JButton setCSModeButton = new JButton();

  // to find and remember the hex file
  final javax.swing.JFileChooser hexFileChooser = new JFileChooser(" ");

  JLabel statusBar = new JLabel();
  SprogAlertDialog ad;

  // File to hold name of hex file
  SprogHexFile hexFile = null;

  SprogMessage msg;

  // members for handling the bootloader interface

  int bootState = 0;
  static final int IDLE = 0;
  static final int CRSENT = 1;         // awaiting reply to " "
  static final int QUERYSENT = 2;      // awaiting reply to "?"
  static final int SETBOOTSENT = 3;    // awaiting reply from bootloader
  static final int VERREQSENT = 4;     // awaiting reply to version request
  static final int WRITESENT = 5;      // write flash command sent, waiting reply
  static final int NULLWRITE = 6;      // no write sent
  static final int ERASESENT = 7;      // erase sent
  static final int SPROGMODESENT = 8;  // enable sprog mode sent
  static final int RESETSENT = 9;      // reset sent
  static final int EOFSENT = 10;      // v4 end of file sent
  static final int V4RESET = 11;       // wait for v4 to reset
  protected int eraseAddress;

  static final boolean UNKNOWN = false;
  static final boolean KNOWN = true;

  String replyString;
  String sprogVersion;
  String sprogType = null;

  SprogTrafficController tc = null;

  public SprogUpdateFrame() {
    super();
  }

  protected String title() {
    return "SPROG Firmware Update";
  }

  protected void init() {
    // connect to the TrafficManager
    tc = SprogTrafficController.instance();
    tc.addSprogListener(this);
    tc.setSprogState(tc.NORMAL);
  }

  public void dispose() {
    tc.removeSprogListener(this);
    tc = null;
  }

  public void message(SprogMessage m) {}   // Ignore

  synchronized public void reply(SprogReply m) {
  }

  private void requestBoot() {
  }

  private void sendWrite() {
  }

  private void sendErase() {
  }

  private void doneWriting() {
  }

  public void initComponents() throws Exception {
    // the following code sets the frame's initial state
    connectButton.setText("Connect");
    connectButton.setVisible(true);
    connectButton.setToolTipText("Identify and connect to SPROG bootloader");

    programButton.setText("Program");
    programButton.setVisible(true);
    programButton.setEnabled(false);
    programButton.setToolTipText("Re-program SPROG with new firmware");

    openFileChooserButton.setText("Choose hex file");
    openFileChooserButton.setVisible(true);
    openFileChooserButton.setEnabled(false);
    openFileChooserButton.setToolTipText("Click here to select hex file to download");

    setSprogModeButton.setText("Set SPROG Mode");
    setSprogModeButton.setVisible(true);
    setSprogModeButton.setEnabled(false);
    setSprogModeButton.setToolTipText("Click here to set SPROG II in SPROG mode");

    setCSModeButton.setText("Set Command Station Mode");
    setCSModeButton.setVisible(true);
    setCSModeButton.setEnabled(false);
    setCSModeButton.setToolTipText("Click here to set SPROG II in Command Station mode");

    statusBar.setVisible(true);
    statusBar.setText(" ");
    statusBar.setHorizontalTextPosition(SwingConstants.LEFT);

    setTitle(title());
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                                             BoxLayout.Y_AXIS));

    JPanel paneA = new JPanel();
    paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));

    JPanel buttons1 = new JPanel();
    buttons1.setLayout(new BoxLayout(buttons1, BoxLayout.X_AXIS));
    buttons1.add(connectButton);
    buttons1.add(openFileChooserButton);
    buttons1.add(programButton);

    JPanel buttons2 = new JPanel();
    buttons2.setLayout(new BoxLayout(buttons2, BoxLayout.X_AXIS));
    buttons2.add(setSprogModeButton);
    buttons2.add(setCSModeButton);

    JPanel status = new JPanel();
    status.setLayout(new BoxLayout(status, BoxLayout.X_AXIS));
    status.add(statusBar);

    paneA.add(buttons1);
    paneA.add(buttons2);
    paneA.add(status);

    getContentPane().add(paneA);

    // connect actions to buttons
    connectButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        connectButtonActionPerformed(e);
      }
    });

    openFileChooserButton.addActionListener(new java.awt.event.
                                            ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        openFileChooserButtonActionPerformed(e);
      }
    });

    programButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        programButtonActionPerformed(e);
      }
    });

    setSprogModeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        setSprogModeButtonActionPerformed(e);
      }
    });

    setCSModeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        setCSModeButtonActionPerformed(e);
      }
    });

    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        thisWindowClosing(e);
      }
    });

    // connect to data source
    init();

    // prevent button areas from expanding
    pack();
    paneA.setMaximumSize(paneA.getSize());
    pack();
  }

  private boolean mShown = false;

  // *** What does addNotify do?
  public void addNotify() {
    super.addNotify();

    if (mShown) {
      return;
    }

    // resize frame to account for menubar
    JMenuBar jMenuBar = getJMenuBar();
    if (jMenuBar != null) {
      int jMenuBarHeight = jMenuBar.getPreferredSize().height;
      java.awt.Dimension dimension = getSize();
      dimension.height += jMenuBarHeight;
      setSize(dimension);
    }

    mShown = true;
  }

  // Close the window when the close box is clicked
  void thisWindowClosing(java.awt.event.WindowEvent e) {
    setVisible(false);
    dispose();
  }

  public synchronized void connectButtonActionPerformed(java.awt.event.
      ActionEvent e) {
  }

  public void openFileChooserButtonActionPerformed(java.awt.event.
      ActionEvent e) {
    // start at current file, show dialog
    int retVal = hexFileChooser.showOpenDialog(this);

    // handle selection or cancel
    if (retVal == JFileChooser.APPROVE_OPTION) {
      hexFile = new SprogHexFile(hexFileChooser.getSelectedFile().getPath());
      if (log.isDebugEnabled()) log.debug("hex file chosen: " + hexFile.getName());
      if ((hexFile.getName().indexOf("sprog") < 0)) {
        SprogAlertDialog ad = new SprogAlertDialog(this, "Hex File Select", "File does not appear to be a valid SPROG II hex file");
        hexFile = null;
      } else {
        hexFile.openRd();
        programButton.setEnabled(true);
      }
    }
  }

  public synchronized void programButtonActionPerformed(java.awt.event.
      ActionEvent e) {
  }

  public void setSprogModeButtonActionPerformed(java.awt.event.
      ActionEvent e) {
  }

  public void setCSModeButtonActionPerformed(java.awt.event.
      ActionEvent e) {
  }

  /**
   * Internal routine to handle a timeout
   */
  synchronized protected void timeout() {
  }

  protected int V_SHORT_TIMEOUT=5;
  protected int SHORT_TIMEOUT=500;
  protected int LONG_TIMEOUT=4000;

  javax.swing.Timer timer = null;

  /**
   * Internal routine to start very short timer for null writes.
   */
  protected void startVShortTimer() {
      restartTimer(V_SHORT_TIMEOUT);
  }

  /**
   * Internal routine to start timer to protect the mode-change.
   */
  protected void startShortTimer() {
      restartTimer(SHORT_TIMEOUT);
  }

  /**
   * Internal routine to restart timer with a long delay
   */
  protected void startLongTimer() {
      restartTimer(LONG_TIMEOUT);
  }

  /**
   * Internal routine to stop timer, as all is well
   */
  protected void stopTimer() {
      if (timer!=null) timer.stop();
  }

  /**
   * Internal routine to handle timer starts & restarts
   */
  protected void restartTimer(int delay) {
      if (timer==null) {
          timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent e) {
                      timeout();
                  }
              });
      }
      timer.stop();
      timer.setInitialDelay(delay);
      timer.setRepeats(false);
      timer.start();
  }

  static org.apache.log4j.Category log = org.apache.log4j.Category.
      getInstance(SprogUpdateFrame.class.getName());

}
