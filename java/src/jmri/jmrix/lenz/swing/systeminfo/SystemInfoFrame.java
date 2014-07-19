// SysteInfoFrame.java

package jmri.jmrix.lenz.swing.systeminfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author			Paul Bender  Copyright (C) 2003-2010
 * @author			Giorgio Terdina  Copyright (C) 2007
 * @version			$Revision$
 */
public class SystemInfoFrame extends jmri.util.JmriJFrame implements XNetListener {

    protected XNetTrafficController tc = null;

    public SystemInfoFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super("XPressNet System Information");
          tc=memo.getXNetTrafficController();
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

        addHelpMenu("package.jmri.jmrix.lenz.systeminfo.SystemInfoFrame", true);

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
       
        if (tc!=null)
	    tc.addXNetListener(~0, this);
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
	XNetMessage msg=XNetMessage.getCSVersionRequestMessage();
        //Then send to the controller
        tc.sendXNetMessage(msg,this);
        /* Next, get the message to request the Computer Interface 
           Hardware/Software Version */
       	XNetMessage msg2=XNetMessage.getLIVersionRequestMessage();
        // Then send it to the controller
        tc.sendXNetMessage(msg2,this);
	/* Finally, we send a request for the Command Station Status*/
	XNetMessage msg3=XNetMessage.getCSStatusRequestMessage();
        //Then send to the controller
        tc.sendXNetMessage(msg3,this);
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
   	        tc.getCommandStation().setCommandStationSoftwareVersion(l);
  	        tc.getCommandStation().setCommandStationType(l);
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

    // Handle a timeout notification
    public void notifyTimeout(XNetMessage msg)
    {
       if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
    }


    /**
     * This just displays the currently known version information from the 
     * LenzCommandStation class.
     **/
    private void setCSVersionDisplay() {
		CSSoftwareVersion.setText("" + tc.getCommandStation()
                                                 .getCommandStationSoftwareVersion());
                int cs_type=tc.getCommandStation().getCommandStationType();
                if(cs_type==jmri.jmrix.lenz.XNetConstants.CS_TYPE_LZ100) {
			CSType.setText("LZ100/LZV100");
		}
		else if(cs_type==jmri.jmrix.lenz.XNetConstants.CS_TYPE_LH200) {
			CSType.setText("LH200");
		     }
		else if(cs_type==jmri.jmrix.lenz.XNetConstants.CS_TYPE_COMPACT) {
			CSType.setText("Compact or Other");
		     }
		else if(cs_type==jmri.jmrix.lenz.XNetConstants.CS_TYPE_MULTIMAUS) {
			CSType.setText("multiMAUS");
		     }
		else if(cs_type==jmri.jmrix.lenz.XNetConstants.CS_TYPE_Z21) {
			CSType.setText("Z21");
		     }
        else CSType.setText("<unknown>");
	}

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static Logger log = LoggerFactory.getLogger(SystemInfoFrame.class.getName());

}
