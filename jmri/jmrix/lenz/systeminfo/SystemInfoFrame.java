// SysteInfoFrame.java

package jmri.jmrix.lenz.systeminfo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jmri.jmrix.lenz.*;

/**
 * Frame displaying Version information for Xpressnet hardware.
 * <P>
 * This is a utility for reading the software version and type of
 * the command station, and, the Hardware and software versions of your
 * XPressNet Computer Interface.
 * <P>
 * Some of this code may be moved to facilitate automatic enabling of
 * features that are not available on all XPressNet Command Stations
 * (as an example, the fact that you can't program using the computer on a
 * Commander or Compact)
 *
 * @author			Paul Bender  Copyright (C) 2003
 * @version			$Revision: 1.5 $
 */
public class SystemInfoFrame extends JFrame implements XNetListener {

    public SystemInfoFrame() {
        super("XPressNet System Information");
        getContentPane().setLayout(new GridLayout(0,2));

          getContentPane().add(new JLabel("Command Station: "));
          getContentPane().add(CSType);

          getContentPane().add(new JLabel("Software Version:"));
          getContentPane().add(CSSoftwareVersion);

          getContentPane().add(new JLabel("Interface: "));
          getContentPane().add(LIType);

          getContentPane().add(new JLabel("Hardware Version:"));
          getContentPane().add(LIHardwareVersion);

          getContentPane().add(new JLabel("Software Version:"));
          getContentPane().add(LISoftwareVersion);

          getContentPane().add(getSystemInfoButton);
          getContentPane().add(closeButton);

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

        if (XNetTrafficController.instance()!=null)
	    XNetTrafficController.instance().addXNetListener(~0, this);
        else
            log.warn("No XPressNet connection, panel won't function");

    }

    boolean read = false;

    JLabel CSType = new JLabel("                ");
    JLabel CSSoftwareVersion = new JLabel("");
    JLabel LIType = new JLabel("       ");
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
              LIHardwareVersion.setText("" + (l.getElementBCD(1).floatValue()/10) );
              LISoftwareVersion.setText("" + (l.getElementBCD(2)) );
       }
       else if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONCE)
       {
              // This is the Command Station Software Version Responce
              if(l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION)
	      {
		CSSoftwareVersion.setText("" + (l.getElementBCD(2).floatValue())/10);
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
