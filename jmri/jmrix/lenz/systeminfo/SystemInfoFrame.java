// SysteInfoFrame.java

package jmri.jmrix.lenz.systeminfo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jmri.jmrix.lenz.*;

/**
 * Frame displaying Version information for Xpressnet hardware.
 *
 * Need to add documentation on how this works
 *
 * @author			Paul Bender  Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class SystemInfoFrame extends JFrame implements XNetListener {

    public SystemInfoFrame() {
        super("XPressNetSystemInformation");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel("Command Station: "));
        pane0.add(CSType);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(new JLabel("Hardware Version:"));
        pane1.add(CSHardwareVersion);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(new JLabel("Software Version:"));
        pane2.add(CSSoftwareVersion);
        pane2.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
        pane3.add(new JLabel("Interface: "));
        pane3.add(LIType);
        pane3.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane3);

        JPanel pane4 = new JPanel();
        pane4.add(new JLabel("Hardware Version:"));
        pane4.add(LIHardwareVersion);
        pane4.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane4);

        JPanel pane5 = new JPanel();
        pane5.add(new JLabel("Software Version:"));
        pane5.add(LISoftwareVersion);
        pane5.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane5);

        JPanel pane6 = new JPanel();
        pane6.add(getSystemInfoButton);
        pane6.add(closeButton);
        getContentPane().add(pane6);

        // and prep for display
        pack();

        // Add Get SystemInfo button handler
        getSystemInfoButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	getSystemInfo();
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

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

	XNetTrafficController.instance().addXNetListener(~0, this);

    }

    boolean read = false;

    JLabel CSType = new JLabel("");
    JLabel CSHardwareVersion = new JLabel("");
    JLabel CSSoftwareVersion = new JLabel("");
    JLabel LIType = new JLabel("");
    JLabel LIHardwareVersion = new JLabel("");
    JLabel LISoftwareVersion = new JLabel("");

    JToggleButton getSystemInfoButton = new JToggleButton("Get System Info");
    JToggleButton closeButton = new JToggleButton("Close");

    //Send Information request to LI100/LI101
    void getSystemInfo() {
	XNetMessage msg=new XNetMessage(3);
        /* First, we need to send a request for the Command Station 
        hardware and software version */
	msg.setElement(0,XNetConstants.CS_REQUEST);
	msg.setElement(1,XNetConstants.CS_VERSION);
        msg.setParity(); // Set the parity bit
        //Then send to the controller
        XNetTrafficController.instance().sendXNetMessage(msg,this);
	
	XNetMessage msg2=new XNetMessage(2);
	/* Second, we send a request for the Interface hardware and 
	software version */
	msg2.setElement(0,XNetConstants.LI_VERSION_REQUEST);
        msg2.setParity(); // Set the parity bit
        //Then send to the controller
        XNetTrafficController.instance().sendXNetMessage(msg2,this);

    }
   
    // listen for responces from the LI101
    public void message(XNetMessage l) {
       // Check to see if this is a responce for the LI version info
       // or the Command Station Version Info
       if (l.getElement(0)==XNetConstants.LI_VERSION_RESPONCE)
       {
              // This is an Interface responce
              LIHardwareVersion.setText("" + Integer.toHexString(l.getElement(1)) );
              LISoftwareVersion.setText("" + Integer.toHexString(l.getElement(2)) );
       }
       else if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONCE)
       {
              // This is the Command Station Software Version Responce
              if(l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION)
	      {
		CSSoftwareVersion.setText("" + Integer.toHexString(l.getElement(2)));
                int cs_type=l.getElement(3);
                if(cs_type==0x00) {
			CSType.setText("LZ100/LZV100");
		}
		else if(cs_type==0x01) {
			CSType.setText("LH200");
		     }
		else if(cs_type==0x02) {
			CSType.setText("Compact or Other");
		     }
              }
       }
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SystemInfoFrame.class.getName());

}
