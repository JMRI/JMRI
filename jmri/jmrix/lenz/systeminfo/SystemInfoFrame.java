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
 * @version			$Revision: 2.3 $
 */
public class SystemInfoFrame extends jmri.util.JmriJFrame implements XNetListener {

    public SystemInfoFrame() {
        super("XPressNet System Information");
        getContentPane().setLayout(new GridLayout(0,2));

          getContentPane().add(new JLabel("Command Station: "));
          getContentPane().add(CSType);

          getContentPane().add(new JLabel("Software Version:"));
          getContentPane().add(CSSoftwareVersion);

	  getContentPane().add(new JLabel("Status:"));
          getContentPane().add(CSStatus);

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

        // initilize the display values with what the LenzCommandStation 
        // class already knows.
        setCSVersionDisplay();

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
    JLabel CSStatus = new JLabel("Unknown");
    JLabel LIType = new JLabel("       ");
    JLabel LIHardwareVersion = new JLabel("");
    JLabel LISoftwareVersion = new JLabel("");

    JToggleButton getSystemInfoButton = new JToggleButton("Get System Info");
    JToggleButton closeButton = new JToggleButton("Close");

    //Send Information request to LI100/LI101
    void getSystemInfo() {
        /* First, we need to send a request for the Command Station
        hardware and software version */
	XNetMessage msg=XNetTrafficController.instance()
                                             .getCommandStation()
                                             .getCSVersionRequestMessage();
        //Then send to the controller
        XNetTrafficController.instance().sendXNetMessage(msg,this);
       	XNetMessage msg2=new XNetMessage(2);
	/* Second, we send a request for the Interface hardware and
	software version */
	msg2.setElement(0,XNetConstants.LI_VERSION_REQUEST);
        msg2.setParity(); // Set the parity bit
        //Then send to the controller
        XNetTrafficController.instance().sendXNetMessage(msg2,this);
	/* Finally, we send a request for the Command Station Status*/
	XNetMessage msg3=XNetTrafficController.instance()
                                             .getCommandStation()
                                             .getCSStatusRequestMessage();
        //Then send to the controller
        XNetTrafficController.instance().sendXNetMessage(msg3,this);
    }

    // listen for responses from the LI101
    public void message(XNetReply l) {
       // Check to see if this is a response for the LI version info
       // or the Command Station Version Info
       if (l.getElement(0)==XNetConstants.LI_VERSION_RESPONSE)
       {
              // This is an Interface response
              LIHardwareVersion.setText("" + (l.getElementBCD(1).floatValue()/10) );
              LISoftwareVersion.setText("" + (l.getElementBCD(2)) );
       }
       else if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE)
       {
              // This is the Command Station Software Version Response
              if(l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION)
	      {
   	        XNetTrafficController.instance()
                                     .getCommandStation()
                                     .setCommandStationSoftwareVersion(l);
  	        XNetTrafficController.instance()
                                     .getCommandStation()
                                     .setCommandStationType(l);
                setCSVersionDisplay();
              }
       } else if (l.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE) {
	      if (l.getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
		int statusByte=l.getElement(2);
		if((statusByte&0x01)==0x01) {
		   // Command station is in Emergency Off Mode
			CSStatus.setText("Emergency Off");
		} else if ((statusByte&0x02)==0x02){
		   // Command station is in Emergency Stop Mode
			CSStatus.setText("Emergency Stop");
		} else if ((statusByte&0x08)==0x08){
		   // Command station is in Service Mode
			CSStatus.setText("Service Mode");
		} else if ((statusByte&0x40)==0x40){
		   // Command station is in Power Up Mode
			if((statusByte&0x04)==0x04) CSStatus.setText("Powering up, Auto Mode");
			    else CSStatus.setText("Powering up, Manual Mode");
		} else if ((statusByte&0x80)==0x80){
		   // Command station has a experienced a ram check error
			CSStatus.setText("RAM check error!");
		} else CSStatus.setText("Normal");
	      }
       }
    }
    
    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }


    /**
     * This just displays the currently known version information from the 
     * LenzCommandStation class.
     **/
    private void setCSVersionDisplay() {
		CSSoftwareVersion.setText("" + XNetTrafficController.instance()
                                                 .getCommandStation()
                                                 .getCommandStationSoftwareVersion());
                int cs_type=XNetTrafficController.instance()
                                                 .getCommandStation()
                                                 .getCommandStationType();
                if(cs_type==0x00) {
			CSType.setText("LZ100/LZV100");
		}
		else if(cs_type==0x01) {
			CSType.setText("LH200");
		     }
		else if(cs_type==0x02) {
			CSType.setText("Compact or Other");
		     }
                else CSType.setText("<unknown>");
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
