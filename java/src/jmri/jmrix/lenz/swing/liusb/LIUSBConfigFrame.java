// LIUSBConfigFrame.java

package jmri.jmrix.lenz.swing.liusb;

import org.apache.log4j.Logger;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jmri.jmrix.lenz.*;

/**
 * Frame displaying the LIUSB configuration utility
 *
 * This is a configuration utility for the LIUSB.
 * It allows the user to set the XPressNet Address and the
 * port speed used to communicate with the LIUSB.
 *
 * @author			Paul Bender  Copyright (C) 2009-2010
 * @version			$Revision$
 */
public class LIUSBConfigFrame extends jmri.util.JmriJFrame implements XNetListener {

    protected XNetTrafficController tc = null;

    public LIUSBConfigFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super("LIUSB Configuration Utility");
        tc=memo.getXNetTrafficController();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel("Xpressnet address: "));
        pane0.add(addrBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

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

        // and prep for display
        pack();

        status.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(status);

        // install read settings, write settings button handlers
        readSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	readLIUSBSettings();
                }
            }
        );

        writeSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	writeLIUSBSettings();
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
                	resetLIUSBSettings();

                }
            }
        );


        // add status
        getContentPane().add(status);

        if (tc != null)
	    tc.addXNetListener(~0, this);
        else
            log.warn("No XpressNet connection, so panel won't function");

    }

    boolean read = false;

    JComboBox addrBox = new javax.swing.JComboBox();

    JLabel status = new JLabel("");

    JToggleButton readSettingsButton = new JToggleButton("Read from LIUSB");
    JToggleButton writeSettingsButton = new JToggleButton("Write to LIUSB");
    JToggleButton closeButton = new JToggleButton("Close");
    JToggleButton resetButton = new JToggleButton("Reset to Factory Defaults");

    protected String [] validXNetAddresses= new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31",""};

    //Send new address to LIUSB
    void writeLIUSBSettings() {
        if((String)addrBox.getSelectedItem()!="" &&
           (String)addrBox.getSelectedItem()!=null) {
           /* we take care of generating an address request */
           XNetMessage msg=XNetMessage.getLIAddressRequestMsg(
                                                addrBox.getSelectedIndex());
           //Then send to the controller
           tc.sendXNetMessage(msg,this);
        }
    }

    //Send Information request to LIUSB
    void readLIUSBSettings() {
        /* we request setting an out of range address 
           to get the current value. */
        XNetMessage msg=XNetMessage.getLIAddressRequestMsg(32);
        //Then send to the controller
        tc.sendXNetMessage(msg,this);
    }

    // listen for responces from the LI101
    public void message(XNetReply l) {
       // Check to see if this is an LI101 info request messgage, if it
       //is, determine if it's the baud rate setting, or the address
       //setting
       if (l.getElement(0)==XNetConstants.LI101_REQUEST)
       {
          if(l.getElement(1)==XNetConstants.LI101_REQUEST_ADDRESS)
          {
              // The third element is the address
	      addrBox.setSelectedIndex(l.getElement(2));
              status.setText("Address" + l.getElement(2) + "recieved from LIUSB");
	  }
       }
    }

    // listen for the messages to the LIUSB
    public void message(XNetMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XNetMessage msg)
    {
       if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
    }


    // For now, reset just resets the screen to factory defaults
    // default values is 30 for the address
    void resetLIUSBSettings() {
	addrBox.setSelectedIndex(30);
    }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static Logger log = Logger.getLogger(LIUSBConfigFrame.class.getName());

}
