// LZ100InternalFrame.java

package jmri.jmrix.lenz.swing.lz100;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;
import jmri.jmrix.lenz.*;

/**
 * Internal Frame displaying the LZ100 configuration utility
 *
 * This is a configuration utility for the LZ100.
 * It allows the user to set the statup mode (automatic or manual) and to 
 * reset the command station.
 *
 * @author			Paul Bender  Copyright (C) 2005-2010
 * @version			$Revision$
 */
public class LZ100InternalFrame extends javax.swing.JInternalFrame implements XNetListener {

    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.lz100.LZ100Bundle");

    private boolean autoMode = false; // Holds Auto/Manual Startup Mode.

    private int resetMode=0; // hold the reset mode;
    static final private int IDLE = 0;
    static final private int ONSENT = 1;
    static final private int OFFSENT = 2;

    private int sendCount=0; // count the number of times the on/off 
			      // sequence for F4 has been sent durring a reset

    protected XNetTrafficController tc = null;

    public LZ100InternalFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {

        tc=memo.getXNetTrafficController();
	
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	setTitle(rb.getString("LZ100Command"));

        JPanel pane4 = new JPanel();
	pane4.add(new JLabel(rb.getString("LZ100StartMode")));

	isAutoMode.setVisible(true);
	isAutoMode.setText(rb.getString("LZ100AutoMode"));
	isAutoMode.setToolTipText(rb.getString("LZ100AutoModeToolTip"));
        pane4.add(isAutoMode);

	isManualMode.setVisible(true);
	isManualMode.setText(rb.getString("LZ100ManualMode"));
	isManualMode.setToolTipText(rb.getString("LZ100ManualModeToolTip"));
        pane4.add(isManualMode);

	amModeGetButton.setText(rb.getString("LZ100GetAMMode"));
	amModeGetButton.setToolTipText(rb.getString("LZ100GetAMModeToolTip"));
	pane4.add(amModeGetButton);

	amModeSetButton.setText(rb.getString("LZ100SetAMMode"));
	amModeSetButton.setToolTipText(rb.getString("LZ100SetAMModeToolTip"));
	pane4.add(amModeSetButton);
        getContentPane().add(pane4);

        JPanel pane3 = new JPanel();
	pane3.add(new JLabel(rb.getString("LZ100OptionLabel")));

	resetCSButton.setText(rb.getString("LZ100Reset"));
	resetCSButton.setToolTipText(rb.getString("LZ100ResetToolTip"));
        pane3.add(resetCSButton);
        getContentPane().add(pane3);

        // add status
        status.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
	status.setVisible(true);
        getContentPane().add(status);

        // and prep for display
        pack();

        // install reset Command Station button handler
        resetCSButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	resetLZ100CS();
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

       // configure internal frame options
                        
        setClosable(false);  // don't let the user close this frame
        setResizable(false);  // don't let the user resize this frame
        setIconifiable(false); // don't let the user minimize this frame
        setMaximizable(false); // don't let the user maximize this frame
          
        // make the internal frame visible
        this.setVisible(true);
    
	// Check for XPressNet Connection, add listener if present, 
	//  warn if not.
        if (tc != null)
	    tc.addXNetListener(~0, this);
        else
            log.warn("No XpressNet connection, so panel won't function");

    }

    boolean read = false;

    JComboBox voltBox = new javax.swing.JComboBox();
    JComboBox eLineBox = new javax.swing.JComboBox();

    JLabel status = new JLabel(" ");

    JToggleButton closeButton = new JToggleButton("Close");
    JToggleButton resetCSButton = new JToggleButton("Reset Command Station");

    JRadioButton isAutoMode = new JRadioButton("Auto");
    JRadioButton isManualMode = new JRadioButton("Manual");
    JToggleButton amModeGetButton = new JToggleButton("Get Current Mode");
    JToggleButton amModeSetButton = new JToggleButton("Set Mode");

    protected String [] validVoltage= new String[]{"11V","11.5V","12V","12.5V","13V","13.5V","14V","14.5V","15V","15.5V","16V","16.5V","17V","17.5V","18V","18.5V","19V","19.5V","20V","20.5V","21V","21.5V","22V"};
    protected int [] validVoltageValues = new int[]{22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44};

    protected String [] validELineStatus = new String[]{"Active","Inactive","default"};
    protected int [] validELineStatusValues = new int[]{90,91,99};

    // listen for responses from the LZ100
    synchronized public void message(XNetReply l) {
       if(l.isOkMessage()) {
	  /* this was an "OK" message
	     We're only paying attention to it if we're 
	     resetting the command station 
           */
	  if(status.getText().equals(rb.getString("LZ100StatusSetMode")))
		status.setText(rb.getString("LZ100StatusOK"));
	  if(resetMode==OFFSENT) {
            XNetMessage msgon=XNetMessage.getFunctionGroup1OpsMsg(0,false,false,false,false,true);
	    sendCount--;
	    resetMode=ONSENT;
	    tc.sendXNetMessage(msgon,this);
	  } else if(resetMode==ONSENT) {
            XNetMessage msgoff=XNetMessage.getFunctionGroup1OpsMsg(0,false,false,false,false,false);
	    if(sendCount>=0)
	    	resetMode=OFFSENT;
	    else {
		resetMode=IDLE;
		resetCSButton.setEnabled(true);
		status.setText(rb.getString("LZ100ResetFinished"));
	    }
	    tc.sendXNetMessage(msgoff,this);
	  }
       } else if (l.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE &&
                  l.getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
                int statusByte=l.getElement(2); 
                if((statusByte&0x04)==0x04) {
		    isAutoMode.setSelected(true);
		    isManualMode.setSelected(false);
		    autoMode=true;
	    	    status.setText(rb.getString("LZ100StatusOK"));
		} else {
		    isAutoMode.setSelected(false);
		    isManualMode.setSelected(true);
		    autoMode=false;
		    status.setText(rb.getString("LZ100StatusOK"));
		}
	}
    }

    // listen for the messages to the LI100/LI101
    synchronized public void message(XNetMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XNetMessage msg)
    {
       if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
    }

    // Set to default values.  Voltage is 16, E Line is Active. 
    synchronized void resetLZ100Settings() {
	voltBox.setSelectedIndex(10);
	eLineBox.setSelectedIndex(0);
    }

    // reset the command station to factory defaults
    synchronized void resetLZ100CS() {
      resetCSButton.setEnabled(false);
      status.setText(rb.getString("LZ100StatusReset"));
      // the Command station is reset by sending F4 25 times for address 00
      XNetMessage msgon=XNetMessage.getFunctionGroup1OpsMsg(0,false,false,false,true,false);
      resetMode = ONSENT;
      sendCount = 25;

      tc.sendXNetMessage(msgon,this);
    }

    // get the current automatic/manual mode
    synchronized void amModeGet() {
	   XNetMessage msg=XNetMessage.getCSStatusRequestMessage();
           tc.sendXNetMessage(msg,this);
	   amModeGetButton.setSelected(false);
	   status.setText(rb.getString("LZ100StatusRetrieveMode"));
    }

    // set the current automatic/manual mode
    synchronized void amModeSave() {
	   if(log.isDebugEnabled()) {
		if(autoMode) log.debug("Auto Mode True");
		   else log.debug("Auto Mode False");
	   }
	   XNetMessage msg=XNetMessage.getCSAutoStartMessage(autoMode);
           tc.sendXNetMessage(msg,this);
	   amModeSetButton.setSelected(false);
	   status.setText(rb.getString("LZ100StatusSetMode"));
    }


     // Toggle Auto Power-up Mode
     synchronized void AutoModeAction() {
	    if(log.isDebugEnabled()) log.debug("Auto Mode Action Called");
    	    isAutoMode.setSelected(true);
	    isManualMode.setSelected(false);
	    autoMode=true;
     }

     // Toggle Manual Power-up Mode
     synchronized void ManualModeAction() {
	    if(log.isDebugEnabled()) log.debug("Manual Mode Action Called");
    	    isAutoMode.setSelected(false);
	    isManualMode.setSelected(true);
	    autoMode=false;
     }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static Logger log = LoggerFactory.getLogger(LZ100Frame.class.getName());

}
