// LZV100Frame.java

package jmri.jmrix.lenz.lzv100;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jmri.jmrix.lenz.*;

/**
 * Frame displaying the LZV100 configuration utility
 *
 * This is a configuration utility for the LZV100.
 * It allows the user to set the:
 *                 Track Voltage 
 *
 * @author			Paul Bender  Copyright (C) 2003
 * @version			$Revision: 1.2 $
 */
public class LZV100Frame extends JFrame implements XNetListener {

    public LZV100Frame() {
        super("LZV100 Configuration Utility");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel("Power Station Configuration:"));
        pane0.add(new JLabel("Track Voltage: "));
        pane0.add(voltBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(new JLabel("Set The E-Line to:"));
        pane1.add(eLineBox);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(writeSettingsButton);
        pane2.add(resetButton);
        pane2.add(closeButton);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
	pane3.add(new JLabel("Command Station Options:"));
        pane3.add(resetCSButton);
        pane3.add(amModeButton);
        getContentPane().add(pane3);

        // Initilize the Combo Boxes
        voltBox.setVisible(true);
        voltBox.setToolTipText("Select the track voltage");
        for (int i=0; i<validVoltage.length;i++)
        {
           voltBox.addItem(validVoltage[i]);
        }
	voltBox.setSelectedIndex(10);

        eLineBox.setVisible(true);
        eLineBox.setToolTipText("Set the E-line Status");
        for (int i=0; i<validELineStatus.length;i++)
        {
           eLineBox.addItem(validELineStatus[i]);
        }
	eLineBox.setSelectedIndex(0);


        // and prep for display
        pack();

        status.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(status);

        writeSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	writeLZV100Settings();
                }
            }
        );

        // install close button handler
        closeButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	setVisible(false);
        		dispose();
                }
            }
        );

        // install reset button handler
        resetButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	resetLZV100Settings();

                }
            }
        );
        // install reset Command Station button handler
        resetCSButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	resetLZV100CS();
                }
            }
        );

        // install Auto/Manual mode toggle button.
        amModeButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	amModeToggle();

                }
            }
        );

        // add status
        getContentPane().add(status);

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

        if (XNetTrafficController.instance() != null)
	    XNetTrafficController.instance().addXNetListener(~0, this);
        else
            log.warn("No XpressNet connection, so panel won't function");

    }

    boolean read = false;

    JComboBox voltBox = new javax.swing.JComboBox();
    JComboBox eLineBox = new javax.swing.JComboBox();

    JLabel status = new JLabel("");

    JToggleButton writeSettingsButton = new JToggleButton("Write to LZV100");
    JToggleButton closeButton = new JToggleButton("Close");
    JToggleButton resetButton = new JToggleButton("Reset to Factory Defaults");
    JToggleButton resetCSButton = new JToggleButton("Reset Command Station");
    JToggleButton amModeButton = new JToggleButton("Auto/Manual mode toggle");

    protected String [] validVoltage= new String[]{"11V","11.5V","12V","12.5V","13V","13.5V","14V","14.5V","15V","15.5V","16V","16.5V","17V","17.5V","18V","18.5V","19V","19.5V","20V","20.5V","21V","21.5V","22V"};
    protected int [] validVoltageValues = new int[]{22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44};

    protected String [] validELineStatus = new String[]{"Active","Inactive","default"};
    protected int [] validELineStatusValues = new int[]{90,91,99};

    //Send Power Station settings
    void writeLZV100Settings() {
        if((String)voltBox.getSelectedItem()!="" &&
           (String)voltBox.getSelectedItem()!=null) {
          /* First, send the ops mode programing command to enter
          programing mode */
	   XNetMessage msg=XNetTrafficController.instance().getCommandStation().getWriteOpsModeCVMsg(00,00,07,50);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
          /* Next, send the ops mode programing command for the voltage 
          we want */
	   msg=XNetTrafficController.instance().getCommandStation().getWriteOpsModeCVMsg(00,00,07,validVoltageValues[voltBox.getSelectedIndex()]);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
        }
        if((String)eLineBox.getSelectedItem()!="" &&
           (String)eLineBox.getSelectedItem()!=null) {
          /* First, send the ops mode programing command to enter
          programing mode */
	   XNetMessage msg=XNetTrafficController.instance().getCommandStation().getWriteOpsModeCVMsg(00,00,07,50);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
          /* Next, send the ops mode programing command for the E line 
             Status we want */
	   msg=XNetTrafficController.instance().getCommandStation().getWriteOpsModeCVMsg(00,00,07,validELineStatusValues[eLineBox.getSelectedIndex()]);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
        }
    }

    // listen for responces from the LZV100
    synchronized public void message(XNetMessage l) {

    if(l.getElement(0)==XNetConstants.LI_MESSAGE_RESPONCE_HEADER &&
       l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONCE_SEND_SUCCESS) {
	  /* this was an "OK" message*/
       }
    }

    // Set to default values.  Voltage is 16, E Line is Active. 
    void resetLZV100Settings() {
	voltBox.setSelectedIndex(10);
	eLineBox.setSelectedIndex(0);
    }

    // reset the command station to factory defaults
    void resetLZV100CS() {
      // the Command station is reset by sending F4 25 times for address 00
      XNetThrottle zerothrottle=new XNetThrottle(0);
      zerothrottle.setSpeedSetting(0);
      for (int i=0;i<25;i++)
      {
        zerothrottle.setF4(true);
      }
      zerothrottle.dispose();
    }

    // toggle automatic/manual mode
    void amModeToggle() {
      // the Command Station is toggled between auto and manual mode by 
      // sending F1 6 times for address 0.
      XNetThrottle zerothrottle=new XNetThrottle(0);
      zerothrottle.setSpeedSetting(0);
      for (int i=0;i<6;i++)
      {
        zerothrottle.setF1(true);
      }
      zerothrottle.dispose();
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LZV100Frame.class.getName());

}
