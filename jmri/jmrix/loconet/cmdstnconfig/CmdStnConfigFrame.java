package jmri.jmrix.loconet.cmdstnconfig;

import java.awt.*;

import javax.swing.*;

import jmri.jmrix.loconet.*;

/**
 * User interface for Command Station Option Programming
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author			Alex Shepherd   Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public class CmdStnConfigFrame
    extends javax.swing.JFrame
    implements LocoNetListener {

  static final int CONFIG_SLOT = 127 ;
  static final int MIN_OPTION = 1 ;
  static final int MAX_OPTION = 56 ;

  LocoNetMessage ConfigSlotData = null;

  public CmdStnConfigFrame() {
    super("Command Station Options Programmer");
  }

  // internal members to hold widgets
  JButton readButton = new JButton("Read");
  JButton writeButton = new JButton("Write");

  public void initComponents() {

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    // buttons
    {
      JPanel pane = new JPanel();
      pane.setLayout(new FlowLayout());
      pane.add(readButton);
      pane.add(writeButton);

      getContentPane().add(pane);
    }

    // address
    {
      JPanel pane = new JPanel();
      pane.setLayout(new FlowLayout());

      getContentPane().add(pane);
    }

    // results
    {
      JPanel pane = new JPanel();
      pane.setLayout(new FlowLayout());

      getContentPane().add(pane);
    }

    readButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        readButtonActionPerformed(e);
      }
    });
    writeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        writeButtonActionPerformed(e);
      }
    });

    // pack to cause display
    pack();
  }

  public void readButtonActionPerformed(java.awt.event.ActionEvent e) {
    // format and send request
    LocoNetMessage l = new LocoNetMessage(4);
    l.setElement(0, LnConstants.OPC_RQ_SL_DATA);
    l.setElement(1, CONFIG_SLOT );
    l.setElement(2, 0);
    l.setElement(3, 0);
    LnTrafficController.instance().sendLocoNetMessage(l);
  }

  public void writeButtonActionPerformed(java.awt.event.ActionEvent e) {
    // format message and send

    if( ConfigSlotData != null ) {
      ConfigSlotData.setOpCode(LnConstants.OPC_WR_SL_DATA);
      LnTrafficController.instance().sendLocoNetMessage(ConfigSlotData);
    }
    return;
  }

  /**
   * Process the incoming message to look for Slot 127 Read
   */
  public void message(LocoNetMessage msg) {
    if (msg.getOpCode() != LnConstants.OPC_SL_RD_DATA)
      return;
    if (msg.getElement(2) != CONFIG_SLOT )
      return;

    ConfigSlotData = msg;
    log.debug("Config Slot Data: " + msg.toString());
    int option;
    for (option = MIN_OPTION; option <= MAX_OPTION; option++) {
      if (getOption(option))
        log.debug("Option: " + option + " State: On");
    }
  }

  boolean getOption(int optNum) {
    if (ConfigSlotData == null)
      throw new RuntimeException("Config Message is null");

    int element, bit;
    element = (optNum / 8) + 3;
    if (element >= 7)
      element++;

    bit = (optNum - 1) % 8;

    if ( (ConfigSlotData.getElement(element) & (1 << bit)) != 0) {
//      log.debug("Option: " + optNum + " Element: " + element + " Bit: " + bit + " Set");
      return true;
    }

    return false;
  }

  private boolean mShown = false;

  public void addNotify() {
    super.addNotify();

    if (mShown)
      return;

    // resize frame to account for menubar
    JMenuBar jMenuBar = getJMenuBar();
    if (jMenuBar != null) {
      int jMenuBarHeight = jMenuBar.getPreferredSize().height;
      Dimension dimension = getSize();
      dimension.height += jMenuBarHeight;
      setSize(dimension);
    }

    mShown = true;
  }

  // Close the window when the close box is clicked
  void thisWindowClosing(java.awt.event.WindowEvent e) {
    setVisible(false);
    dispose();
    // disconnect from LnTrafficController
    tc = null;
    tc.removeLocoNetListener(~0, this);
  }

  // connect to the LnTrafficController
  public void connect(LnTrafficController t) {
    tc = t;
    tc.addLocoNetListener(~0, this);
  }

  // private data
  private LnTrafficController tc = null;

  // initialize logging
  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance( CmdStnConfigFrame.class.getName());
}