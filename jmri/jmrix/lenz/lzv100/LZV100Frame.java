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
 * @version			$Revision: 2.4 $
 */
public class LZV100Frame extends jmri.util.JmriJFrame implements XNetListener {


    private boolean autoMode = false; // Holds Auto/Manual Startup Mode.

    private int resetMode=0; // hold the reset mode;
    static final private int IDLE = 0;
    static final private int ONSENT = 1;
    static final private int OFFSENT = 2;

    private int sendCount=0; // count the number of times the on/off 
			      // sequence for F4 has been sent durring a reset

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

        JPanel pane4 = new JPanel();
	pane4.add(new JLabel("Command Station Start-up Mode:"));
	isAutoMode.setVisible(true);
	isAutoMode.setToolTipText("Restart locomotives with last settings on power up");
        pane4.add(isAutoMode);
	isManualMode.setVisible(true);
	isAutoMode.setToolTipText("Do Not restart locomotives with last settings on power up");
        pane4.add(isManualMode);
	pane4.add(amModeGetButton);
	pane4.add(amModeSetButton);
        getContentPane().add(pane4);

        JPanel pane3 = new JPanel();
	pane3.add(new JLabel("Command Station Options:"));
        pane3.add(resetCSButton);
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

        // install Auto/Manual mode retreive button handler.
        amModeGetButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	amModeGet();

                }
            }
        );

        // install Auto/Manual mode Save button handler.
        amModeSetButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	amModeSave();

                }
            }
        );

        // install Auto mode button handler.
        isAutoMode.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	AutoModeAction();
                }
            }
        );

        // install Manual  mode button handler.
        isManualMode.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	ManualModeAction();
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

    JRadioButton isAutoMode = new JRadioButton("Auto");
    JRadioButton isManualMode = new JRadioButton("Manual");
    JToggleButton amModeGetButton = new JToggleButton("Get Current Mode");
    JToggleButton amModeSetButton = new JToggleButton("Set Mode");

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
	   XNetMessage msg=XNetMessage.getWriteOpsModeCVMsg(00,00,07,50);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
          /* Next, send the ops mode programing command for the voltage 
          we want */
	   msg=XNetMessage.getWriteOpsModeCVMsg(00,00,07,validVoltageValues[voltBox.getSelectedIndex()]);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
        }
        if((String)eLineBox.getSelectedItem()!="" &&
           (String)eLineBox.getSelectedItem()!=null) {
          /* First, send the ops mode programing command to enter
          programing mode */
	   XNetMessage msg=XNetMessage.getWriteOpsModeCVMsg(00,00,07,50);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
          /* Next, send the ops mode programing command for the E line 
             Status we want */
	   msg=XNetMessage.getWriteOpsModeCVMsg(00,00,07,validELineStatusValues[eLineBox.getSelectedIndex()]);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
        }
    }

    // listen for responses from the LZV100
    synchronized public void message(XNetReply l) {
       if(l.isOkMessage()) {
	  /* this was an "OK" message
	     We're only paying attention to it if we're 
	     resetting the command station 
           */
	  if(resetMode==OFFSENT) {
            XNetMessage msgon=new XNetMessage(6);
            msgon.setElement(0,XNetConstants.LOCO_OPER_REQ);
            msgon.setElement(1,XNetConstants.LOCO_SET_FUNC_GROUP1);
            msgon.setElement(2,0); // set to the upper byte of the DCC address
      	    msgon.setElement(3,0);   // set to the lower byte of the DCC address
      	    msgon.setElement(4,0x08);
      	    msgon.setParity(); // Set the parity bit
	    sendCount--;
	    resetMode=ONSENT;
	    XNetTrafficController.instance().sendXNetMessage(msgon,this);
	  } else if(resetMode==ONSENT) {
      	    XNetMessage msgoff=new XNetMessage(6);
      	    msgoff.setElement(0,XNetConstants.LOCO_OPER_REQ);
      	    msgoff.setElement(1,XNetConstants.LOCO_SET_FUNC_GROUP1);
      	    msgoff.setElement(2,0); // set to the upper byte of the DCC address
      	    msgoff.setElement(3,0); // set to the lower byte of the DCC address
            msgoff.setElement(4,0x00);
            msgoff.setParity(); // Set the parity bit
	    if(sendCount>=0)
	    	resetMode=OFFSENT;
	    else {
		resetMode=IDLE;
		resetCSButton.setEnabled(true);
	    }
	    XNetTrafficController.instance().sendXNetMessage(msgoff,this);
	  }
       } else if (l.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE &&
                  l.getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
                int statusByte=l.getElement(2); 
                if((statusByte&0x04)==0x04) {
		    isAutoMode.setSelected(true);
		    isManualMode.setSelected(false);
		    autoMode=true;
		} else {
		    isAutoMode.setSelected(false);
		    isManualMode.setSelected(true);
		    autoMode=false;
		}
	}
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }


    // Set to default values.  Voltage is 16, E Line is Active. 
    void resetLZV100Settings() {
	voltBox.setSelectedIndex(10);
	eLineBox.setSelectedIndex(0);
    }

    // reset the command station to factory defaults
    void resetLZV100CS() {
      resetCSButton.setEnabled(false);
      // the Command station is reset by sending F4 25 times for address 00

      XNetMessage msgon=new XNetMessage(6);
      msgon.setElement(0,XNetConstants.LOCO_OPER_REQ);
      msgon.setElement(1,XNetConstants.LOCO_SET_FUNC_GROUP1);
      msgon.setElement(2,0);   // set to the upper byte of the DCC address
      msgon.setElement(3,0);   // set to the lower byte of the DCC address
      msgon.setElement(4,0x08);
      msgon.setParity(); // Set the parity bit

      resetMode = ONSENT;
      sendCount = 25;

      XNetTrafficController.instance().sendXNetMessage(msgon,this);
    }

    // get the current automatic/manual mode
    void amModeGet() {
	   XNetMessage msg=XNetTrafficController.instance()
						.getCommandStation()
						.getCSStatusRequestMessage();
           XNetTrafficController.instance().sendXNetMessage(msg,this);
	   amModeGetButton.setSelected(false);
    }

    // set the current automatic/manual mode
    void amModeSave() {
	   if(log.isDebugEnabled()) {
		if(autoMode) log.debug("Auto Mode True");
		   else log.debug("Auto Mode False");
	   }
	   XNetMessage msg=XNetTrafficController.instance()
						.getCommandStation()
						.getCSAutoStartMessage(autoMode);
           XNetTrafficController.instance().sendXNetMessage(msg,this);
	   amModeSetButton.setSelected(false);
    }


     // Toggle Auto Power-up Mode
     void AutoModeAction() {
	    if(log.isDebugEnabled()) log.debug("Auto Mode Action Called");
    	    isAutoMode.setSelected(true);
	    isManualMode.setSelected(false);
	    autoMode=true;
     }

     // Toggle Manual Power-up Mode
     void ManualModeAction() {
	    if(log.isDebugEnabled()) log.debug("Manual Mode Action Called");
    	    isAutoMode.setSelected(false);
	    isManualMode.setSelected(true);
	    autoMode=false;
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
