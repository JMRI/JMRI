package jmri.jmrix.loconet.cmdstnconfig;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import jmri.jmrix.loconet.*;

import java.util.ResourceBundle;

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
 * @author			Bob Jacobsen  Copyright (C) 2006
 * @version			$Revision: 1.4 $
 */
public class CmdStnConfigFrame extends javax.swing.JFrame implements LocoNetListener {

  int CONFIG_SLOT = 127 ;
  int MIN_OPTION = 1 ;
  int MAX_OPTION = 72 ;
  String LabelT = "T";
  String LabelC = "C";
  String LabelTop = "Configure Command Station";
  
  String read = "Read";
  String write = "Write";
  
  int[] oldcontent = new int[10];
  
  public CmdStnConfigFrame() {
    super("Command Station Options Programmer");
  }

  ResourceBundle rb;
  // internal members to hold widgets
  JButton readButton;
  JButton writeButton;

  JRadioButton[] closedButtons = new JRadioButton[MAX_OPTION];
  JRadioButton[] thrownButtons = new JRadioButton[MAX_OPTION];
  
  public void initComponents() {

    // set up constants from properties file, if possible
    String name = "<unchanged>";
    try {
        name = SlotManager.instance().getCommandStationType();
        // get first token
        name = name.substring(0, name.indexOf(' '));
        log.debug("match /"+name+"/");
        rb = ResourceBundle.getBundle("jmri.jmrix.loconet.cmdstnconfig."+name+"options");
    } catch (Exception e) {
        log.error("Failed to find options properties file for /"+name+"/");
        rb = ResourceBundle.getBundle("jmri.jmrix.loconet.cmdstnconfig.Defaultoptions");
    }
    
    try {
        CONFIG_SLOT = Integer.parseInt(rb.getString("CONFIG_SLOT"));
        MIN_OPTION =  Integer.parseInt(rb.getString("MIN_OPTION"));
        MAX_OPTION =  Integer.parseInt(rb.getString("MAX_OPTION"));
        LabelT = rb.getString("LabelT");
        LabelC = rb.getString("LabelC");
        LabelTop = rb.getString("LabelTop");
        read = rb.getString("ButtonRead");
        write = rb.getString("ButtonWrite");
    } catch (Exception e) {
        log.error("Failed to load values from /"+name+"/ properties");
    }
    
    log.debug("Constants: "+CONFIG_SLOT+" "+MIN_OPTION+" "+MAX_OPTION);
        
    setTitle(LabelTop);

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    {
      // section holding buttons buttons
      readButton = new JButton(read);
      writeButton = new JButton(write);
      
      JPanel pane = new JPanel();
      pane.setLayout(new FlowLayout());
      pane.add(readButton);
      pane.add(writeButton);
      getContentPane().add(pane);
      
      // section holding options
      JPanel options = new JPanel();
      GridBagConstraints gc = new GridBagConstraints();
      GridBagLayout gl = new GridBagLayout();
      gc.gridy = 0;
      options.setLayout(gl);
      for (int i = MIN_OPTION; i<=MAX_OPTION; i++) {
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        ButtonGroup g = new ButtonGroup();
        JRadioButton c = new JRadioButton(LabelC);
        JRadioButton t = new JRadioButton(LabelT);
        g.add(c);
        g.add(t);
        
        p2.add(t);
        p2.add(c);
        
        closedButtons[i-MIN_OPTION] = c;
        thrownButtons[i-MIN_OPTION] = t;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.CENTER;
        gl.setConstraints(p2, gc);
        options.add(p2);
        gc.gridx = 1;
        gc.weightx = GridBagConstraints.REMAINDER;
        gc.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel(rb.getString("Option"+i));
        gl.setConstraints(l, gc);
        options.add(l);
        gc.gridy++;
      }
      JScrollPane js = new JScrollPane(options);
      js.setVerticalScrollBarPolicy(js.VERTICAL_SCROLLBAR_AS_NEEDED);
      js.setHorizontalScrollBarPolicy(js.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      getContentPane().add(js);
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

    // pack to prepare for display
    pack();
  }

  public void readButtonActionPerformed(java.awt.event.ActionEvent e) {
    // format and send request
    start();
  }

  public void writeButtonActionPerformed(java.awt.event.ActionEvent e) {
    LocoNetMessage msg = new LocoNetMessage(14);
    msg.setElement(0, LnConstants.OPC_WR_SL_DATA);
    msg.setElement(1, 0x0E);
    msg.setElement(2, CONFIG_SLOT);
    
    // load last seen contents into message
    for (int i = 0; i<10; i++)
        msg.setElement(3+i, oldcontent[i]);

    // load contents to message
    int j = 0;
    for (int i = 0; i*8+MIN_OPTION < MAX_OPTION; i++){
        // i indexes over data bytes in the message
        int index = i+3; // index = 0 is the first message byte
        msg.setElement(index,0);  // start with all reset, hence thrown

        if (closedButtons[j++].isSelected()) 
            msg.setElement(index, msg.getElement(index) | 0x01);
        else
            msg.setElement(index, msg.getElement(index) & ~0x01);
        
        if (closedButtons[j++].isSelected()) 
            msg.setElement(index, msg.getElement(index) | 0x02);
        else
            msg.setElement(index, msg.getElement(index) & ~0x02);
        
        if (closedButtons[j++].isSelected()) 
            msg.setElement(index, msg.getElement(index) | 0x04);
        else
            msg.setElement(index, msg.getElement(index) & ~0x04);
        
        if (closedButtons[j++].isSelected()) 
            msg.setElement(index, msg.getElement(index) | 0x08);
        else
            msg.setElement(index, msg.getElement(index) & ~0x08);
        
        if (closedButtons[j++].isSelected()) 
            msg.setElement(index, msg.getElement(index) | 0x10);
        else
            msg.setElement(index, msg.getElement(index) & ~0x10);
        
        if (closedButtons[j++].isSelected()) 
            msg.setElement(index, msg.getElement(index) | 0x20);
        else
            msg.setElement(index, msg.getElement(index) & ~0x20);
        
        if (closedButtons[j++].isSelected()) 
            msg.setElement(index, msg.getElement(index) | 0x40);
        else
            msg.setElement(index, msg.getElement(index) & ~0x40);
        
        j++; // the 8th option doesn't really exist
    }

    // send message
    LnTrafficController.instance().sendLocoNetMessage(msg);
    return;
  }

  /**
   *
   * Start the Frame operating by asking for a read
   */
  public void start() {
    // format and send request for slot contents
    LocoNetMessage l = new LocoNetMessage(4);
    l.setElement(0, LnConstants.OPC_RQ_SL_DATA);
    l.setElement(1, CONFIG_SLOT );
    l.setElement(2, 0);
    l.setElement(3, 0);
    LnTrafficController.instance().sendLocoNetMessage(l);
  }
  
  /**
   * Process the incoming message to look for Slot 127 Read
   */
  public void message(LocoNetMessage msg) {
    if (msg.getOpCode() != LnConstants.OPC_SL_RD_DATA)
      return;
    if (msg.getElement(2) != CONFIG_SLOT )
      return;

    // save contents for later
    for (int i = 0; i<10; i++)
        oldcontent[i] = msg.getElement(3+i);

    // set the GUI
    int j = 0;
    for (int i = 0; i*8+MIN_OPTION < MAX_OPTION; i++){
        // i indexes over bytes in the message
        int index = i+3; // index = 0 is the first payload byte
        int data = msg.getElement(index);  // data is the payload byte

        if ( (data&0x01) != 0) closedButtons[j++].setSelected(true);
        else thrownButtons[j++].setSelected(true);

        if ( (data&0x02) != 0) closedButtons[j++].setSelected(true);
        else thrownButtons[j++].setSelected(true);

        if ( (data&0x04) != 0) closedButtons[j++].setSelected(true);
        else thrownButtons[j++].setSelected(true);

        if ( (data&0x08) != 0) closedButtons[j++].setSelected(true);
        else thrownButtons[j++].setSelected(true);

        if ( (data&0x10) != 0) closedButtons[j++].setSelected(true);
        else thrownButtons[j++].setSelected(true);

        if ( (data&0x20) != 0) closedButtons[j++].setSelected(true);
        else thrownButtons[j++].setSelected(true);

        if ( (data&0x40) != 0) closedButtons[j++].setSelected(true);
        else thrownButtons[j++].setSelected(true);

        thrownButtons[j++].setSelected(true); // the 8th option doesn't really exist
    }

    log.debug("Config Slot Data: " + msg.toString());
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

  public static void main(String args[]) {

  }

}