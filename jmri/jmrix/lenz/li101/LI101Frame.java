// LI101Frame.java

package jmri.jmrix.lenz.li101;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jmri.jmrix.lenz.*;

/**
 * Frame displaying the LI101 configuration utility
 *
 * This is a configuration utility for the LI101.
 * It allows the user to set the XPressNet Address and the
 * port speed used to communicate with the LI101.
 *
 * @author			Paul Bender  Copyright (C) 2003
 * @version			$Revision: 1.4 $
 */
public class LI101Frame extends JFrame implements XNetListener {

    public LI101Frame() {
        super("LI101 Configuration Utility");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel("Xpressnet address: "));
        pane0.add(addrBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(new JLabel("LI101 Speed Setting"));
        pane1.add(speedBox);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(readSettingsButton);
        pane2.add(writeSettingsButton);
        pane2.add(resetButton);
        pane2.add(closeButton);
        getContentPane().add(pane2);

        // Initilize the Combo Boxes
        addrBox.setVisible(true);
        addrBox.setToolTipText("Select the XpressNet address");
        for (int i=0; i<validXNetAddresses.length;i++)
        {
           addrBox.addItem(validXNetAddresses[i]);
        }
	addrBox.setSelectedIndex(32);

        speedBox.setVisible(true);
        speedBox.setToolTipText("Select the LI101 connection speed");
        for (int i=0; i<validSpeeds.length;i++)
        {
           speedBox.addItem(validSpeeds[i]);
        }
	speedBox.setSelectedIndex(4);


        // and prep for display
        pack();

        status.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(status);

        // install read settings, write settings button handlers
        readSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	readLI101Settings();
                }
            }
        );

        writeSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	writeLI101Settings();
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
                	resetLI101Settings();

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

    JComboBox addrBox = new javax.swing.JComboBox();
    JComboBox speedBox = new javax.swing.JComboBox();

    JLabel status = new JLabel("");

    JToggleButton readSettingsButton = new JToggleButton("Read from LI101");
    JToggleButton writeSettingsButton = new JToggleButton("Write to LI101");
    JToggleButton closeButton = new JToggleButton("Close");
    JToggleButton resetButton = new JToggleButton("Reset to Factory Defaults");

    protected String [] validXNetAddresses= new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31",""};

    protected String [] validSpeeds = new String[]{"19,200 baud","38,400 baud","57,600 baud","115,200 baud",""};
    protected int [] validSpeedValues = new int[]{19200,38400,57600,115200};

    //Send new address/baud rate to LI101
    void writeLI101Settings() {
        if((String)addrBox.getSelectedItem()!="" &&
           (String)addrBox.getSelectedItem()!=null) {
	   XNetMessage msg=new XNetMessage(4);
           /* First, we take care of sending an address request */
	   msg.setElement(0,XNetConstants.LI101_REQUEST);
	   msg.setElement(1,XNetConstants.LI101_REQUEST_ADDRESS);
           /* For element 2, we need to figure out what address to send based
           on user selections */
	   msg.setElement(2,addrBox.getSelectedIndex());
           msg.setParity(); // Set the parity bit
           //Then send to the controller
           XNetTrafficController.instance().sendXNetMessage(msg,this);
        }
        if((String)speedBox.getSelectedItem()!=""  &&
           (String)speedBox.getSelectedItem()!=null) {
	     XNetMessage msg=new XNetMessage(4);
             /* Now, we can send a baud rate request */
	     msg.setElement(0,XNetConstants.LI101_REQUEST);
	     msg.setElement(1,XNetConstants.LI101_REQUEST_BAUD);
             /* For element 2, we need to figure out what address to send based
             on user selections */
	     msg.setElement(2,(int)speedBox.getSelectedIndex()+1);
             msg.setParity(); // Set the parity bit
             //Then send to the controller
             XNetTrafficController.instance().sendXNetMessage(msg,this);
          }
    }

    //Send Information request to LI101
    void readLI101Settings() {
	XNetMessage msg=new XNetMessage(4);
        /* First, we take care of sending an address request */
	msg.setElement(0,XNetConstants.LI101_REQUEST);
	msg.setElement(1,XNetConstants.LI101_REQUEST_ADDRESS);
        /* For element 2, we need to send an out of range address to get
        the correct information back*/
	msg.setElement(2,32);
        msg.setParity(); // Set the parity bit
        //Then send to the controller
        XNetTrafficController.instance().sendXNetMessage(msg,this);

        /* Now, we can send a baud rate request */
	msg.setElement(0,XNetConstants.LI101_REQUEST);
	msg.setElement(1,XNetConstants.LI101_REQUEST_BAUD);
        /* For element 2, we need to send an out of range address to get
        the correct information back*/
	msg.setElement(2,6);
        msg.setParity(); // Set the parity bit
        //Then send to the controller
        XNetTrafficController.instance().sendXNetMessage(msg,this);
    }

    // listen for responces from the LI101
    public void message(XNetMessage l) {
       // Check to see if this is an LI101 info request messgage, if it
       //is, determine if it's the baud rate setting, or the address
       //setting
       if (l.getElement(0)==XNetConstants.LI101_REQUEST)
       {
          if(l.getElement(1)==XNetConstants.LI101_REQUEST_ADDRESS)
          {
              // The third element is the address
	      addrBox.setSelectedIndex(l.getElement(2));
              status.setText("Address" + l.getElement(2) + "recieved from LI101");
	  }
          else if(l.getElement(1)==XNetConstants.LI101_REQUEST_BAUD)
          {
              // The third element is the encoded Baud rate
	      speedBox.setSelectedIndex(l.getElement(2) - 1);
              status.setText("Baud rate" + validSpeeds[l.getElement(2) - 1 ] + "recieved from LI101");
	  }
       }
    }

    // For now, reset just resets the screen to factory defaults, and does
    // not send any information to the LI101F.  To do a reset, we need to
    // send a BREAK signal for 200 milliseconds
    // default values are 30 for the address, and 19,200 baud for the
    // speed.
    void resetLI101Settings() {
	addrBox.setSelectedIndex(30);
	speedBox.setSelectedIndex(0);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LI101Frame.class.getName());

}
