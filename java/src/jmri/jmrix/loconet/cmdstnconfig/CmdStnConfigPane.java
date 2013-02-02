package jmri.jmrix.loconet.cmdstnconfig;

import org.apache.log4j.Logger;
import java.awt.*;
import javax.swing.*;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.swing.LnPanel;

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
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author			Alex Shepherd   Copyright (C) 2004
 * @author			Bob Jacobsen  Copyright (C) 2006
 * @version			$Revision$
 */
public class CmdStnConfigPane extends LnPanel implements LocoNetListener {

  int CONFIG_SLOT = 127 ;
  int MIN_OPTION = 1 ;
  int MAX_OPTION = 72 ;
  String LabelT = "T";
  String LabelC = "C";
  String LabelTop = "Configure Command Station";

  String read = "Read";
  String write = "Write";

  int[] oldcontent = new int[10];

  JCheckBox optionBox;
  
  public CmdStnConfigPane() {
    super();
  }

  ResourceBundle rb;
  // internal members to hold widgets
  JButton readButton;
  JButton writeButton;

  JRadioButton[] closedButtons = new JRadioButton[MAX_OPTION];
  JRadioButton[] thrownButtons = new JRadioButton[MAX_OPTION];
  JLabel[] labels = new JLabel[MAX_OPTION];
  boolean[] isReserved = new boolean[MAX_OPTION];
  
    public String getHelpTarget() { return "package.jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigFrame"; }
    public String getTitle() { 
        String uName = "";
        if (memo!=null) {
            uName = memo.getUserName();
            if (!"LocoNet".equals(uName)) {
                uName = uName+": ";
            } else {
                uName = "";
            }
        }
        return uName+LocoNetBundle.bundle().getString("MenuItemCmdStnConfig"); 
    }

  public void initComponents(LocoNetSystemConnectionMemo memo) {
    super.initComponents(memo);

    // set up constants from properties file, if possible
    String name = "<unchanged>";
    try {
        name = memo.getSlotManager().getCommandStationType();
        // get first token
        if ( name.indexOf(' ') != -1) name = name.substring(0, name.indexOf(' '));
        log.debug("match /"+name+"/");
        rb = ResourceBundle.getBundle("jmri.jmrix.loconet.cmdstnconfig."+name+"options");
    } catch (Exception e) {
        log.warn("Failed to find properties for /"+name+"/ command station type", e);
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

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    {
      // section holding buttons buttons
      readButton = new JButton(read);
      writeButton = new JButton(write);

      JPanel pane = new JPanel();
      pane.setLayout(new FlowLayout());
      pane.add(readButton);
      pane.add(writeButton);
      add(pane);
      
      optionBox = new JCheckBox(rb.getString("CheckBoxReserved"));
      add(optionBox);

      // heading
      add(new JLabel(rb.getString("HeadingText")));
      
      // section holding options
      JPanel options = new JPanel();
      GridBagConstraints gc = new GridBagConstraints();
      GridBagLayout gl = new GridBagLayout();
      gc.gridy = 0;
      gc.ipady = 0;
      
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
        String label;
        try {
            label = rb.getString("Option"+i);
            isReserved[i-MIN_OPTION] = false;
        } catch (java.util.MissingResourceException e) {
            label = ""+i+": "+rb.getString("Reserved");
            isReserved[i-MIN_OPTION] = true;
        }
        JLabel l = new JLabel(label);
        labels[i-MIN_OPTION] = l;
        gl.setConstraints(l, gc);
        options.add(l);
        gc.gridy++;
      }
      JScrollPane js = new JScrollPane(options);
      js.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      js.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      add(js);
      
    }

    optionBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        updateVisibility(optionBox.isSelected());
      }
    });
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

    updateVisibility(optionBox.isSelected());
    
    // connect to the LnTrafficController
    memo.getLnTrafficController().addLocoNetListener(~0, this);
    
    // and start
    start();
  }

  void updateVisibility(boolean show) {
    for (int i = MIN_OPTION; i<=MAX_OPTION; i++) {
        if (isReserved[i-MIN_OPTION]) {
            closedButtons[i-MIN_OPTION].setVisible(show);
            thrownButtons[i-MIN_OPTION].setVisible(show);
            labels[i-MIN_OPTION].setVisible(show);
        }
    }
    revalidate();
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
    for (int i = 0; i <= (MAX_OPTION - MIN_OPTION); i++){
      // i indexes over closed buttons
      int byteIndex = i / 8; // byteIndex = 0 is the first payload byte
      if (byteIndex > 3)
        byteIndex++; // Skip the 4th payload byte for some reason

      byteIndex += 3 ; // Add base offset into slot message to first data byte

      int bitIndex = i % 8;
      int bitMask = 0x01 << bitIndex ;

      if (closedButtons[ i ].isSelected())
        msg.setElement(byteIndex, msg.getElement(byteIndex) | bitMask );
      else
        msg.setElement(byteIndex, msg.getElement(byteIndex) & ~bitMask );
    }

    // send message
    memo.getLnTrafficController().sendLocoNetMessage(msg);
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
    memo.getLnTrafficController().sendLocoNetMessage(l);
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
    for (int i = 0; i <= (MAX_OPTION - MIN_OPTION); i++){
      // i indexes over closed/thrown buttons
      int byteIndex = i / 8; // index = 0 is the first payload byte
      if (byteIndex > 3)
        byteIndex++; // Skip the 4th payload byte for some reason

      byteIndex += 3 ; // Add base offset to first data byte

      int bitIndex = i % 8;
      int bitMask = 0x01 << bitIndex ;

      int data = msg.getElement( byteIndex );  // data is the payload byte

      if ( (data & bitMask ) != 0)
        closedButtons[ i ].setSelected(true);
      else
        thrownButtons[ i ].setSelected(true);
    }

    log.debug("Config Slot Data: " + msg.toString());
  }

  public void dispose() {
    // disconnect from LnTrafficController
    memo.getLnTrafficController().removeLocoNetListener(~0, this);
    super.dispose();
  }

  // initialize logging
  static Logger log = Logger.getLogger( CmdStnConfigPane.class.getName());
}
