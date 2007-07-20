// ClockMonFrame.java

package jmri.jmrix.nce.clockmon;

//import jmri.jmrix.nce.*;
import jmri.jmrix.nce.NceListener;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;

//import java.util.Date;
import java.util.ResourceBundle;
import java.awt.event.*;

import javax.swing.*;

/**
 * Frame displaying and programming a NCE clock monitor.
 * <P>
 * Some of the message formats used in this class are Copyright NCE Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact NCE Inc for separate permission.
 *
 * @author			Ken Cameron   Copyright (C) 2007
 * @version			$Revision: 1.1 $
 *
 * derived from loconet.clockmonframe by Bob Jacobsen Copyright (C) 2003
 */
public class ClockMonFrame extends jmri.util.JmriJFrame implements NceListener {
	
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.clockmon.ClockMonBundle");
	
	public static final int CS_CLOCK_MEM_ADDR = 0xDC00;
	public static final int CS_CLOCK_MEM_SIZE = 0x10;
	public static final int CS_CLOCK_SECONDS = 0x02;
	public static final int CS_CLOCK_MINUTES = 0x03;
	public static final int CS_CLOCK_HOURS = 0x04;
	public static final int CS_CLOCK_AMPM = 0x05;
	public static final int CS_CLOCK_1224 = 0x06;
	public static final int CS_CLOCK_STATUS = 0x0D;
	public static final int CS_CLOCK_SCALE = 0x00;
	public static final int CMD_CLOCK_SET_TIME_SIZE = 0x03;
	public static final int CMD_CLOCK_SET_PARAM_SIZE = 0x02;
	public static final int CMD_CLOCK_SET_RUN_SIZE = 0x01;
	public static final int CMD_CLOCK_SET_REPLY_SIZE = 0x01;
	
	private int waiting = 0;
	private int clockMode = 0;	//0 - clocks independent, 1 - Internal sets NCE, 2 - NCE sets Internal
	private boolean waitingForRead = false;
		private boolean updateTimeFromRead = false;
		private boolean updateRatioFromRead = false;
		private boolean updateFormatFromRead = false;
		private boolean updateStatusFromRead = false;
	private boolean waitingForCmdStop = false;
	private boolean waitingForCmdStart = false;
	private boolean waitingForCmdRatio = false;
	private boolean waitingForCmdTime = false;
	private boolean waitingForCmd1224 = false;

    JTextField hours = new JTextField("00");
    JTextField minutes = new JTextField("00");
    JTextField seconds = new JTextField("00");

    JTextField rateNce = new JTextField(2);
    JTextField amPm = new JTextField(2);
    JCheckBox twentyFour = new JCheckBox(rb.getString("CheckBox24HourFormat"));
    JTextField status = new JTextField(10);

    JRadioButton setInternal = new JRadioButton(rb.getString("ClockModeNCE"));
    JRadioButton setNce = new JRadioButton(rb.getString("ClockModeInternal"));
    JRadioButton setIndependent = new JRadioButton(rb.getString("ClockModeIndependent"));

    //Timebase clock ;

    java.beans.PropertyChangeListener minuteChangeListener ;

    JButton setSyncButton = new JButton(rb.getString("SetSyncMode"));
    JButton setClockButton = new JButton(rb.getString("SetHoursMinutes"));
    JButton setRatioButton = new JButton(rb.getString("SetRatio"));
    JButton set1224Button = new JButton(rb.getString("Set12/24Mode"));
    JButton setStopNceButton = new JButton(rb.getString("StopNceClock"));
    JButton setStartNceButton = new JButton(rb.getString("StartNceClock"));
    JButton readButton = new JButton(rb.getString("ReadAll"));

    boolean inSyncWithFastClockMaster = false ;

    public ClockMonFrame() {}
    	
    public void initComponents() throws Exception {
        setTitle(rb.getString("TitleNceClockMonitor"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add GUI items
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(rb.getString("LabelTime")));
        panel.add(hours);
        panel.add(new JLabel(rb.getString("LabelTimeSep")));
        panel.add(minutes);
        panel.add(new JLabel(rb.getString("LabelTimeSep")));
        panel.add(seconds);
        seconds.setEditable(false);
        panel.add(new JLabel(" "));
        panel.add(amPm);
        amPm.setEditable(false);
        panel.add(new JLabel(" "));
        panel.add(setClockButton);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(rb.getString("LabelRatio")));
        panel.add(rateNce);
        panel.add(new JLabel(rb.getString("LabelToOne")));
        panel.add(setRatioButton);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(" "));
        panel.add(twentyFour);
        panel.add(new JLabel(" "));
        panel.add(set1224Button);
        getContentPane().add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(" "));
        panel.add(status);
        getContentPane().add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(setStartNceButton);
        panel.add(new JLabel(" "));
        panel.add(setStopNceButton);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(readButton);
        getContentPane().add(panel);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(setInternal);
        modeGroup.add(setNce);
        modeGroup.add(setIndependent);

        getContentPane().add(setInternal);
        getContentPane().add(setNce);
        getContentPane().add(setIndependent);
        getContentPane().add(setSyncButton);
        setInternal.setEnabled(false);
        setNce.setEnabled(false);
        setIndependent.setEnabled(false);
        setSyncButton.setEnabled(false);
        
        // get current settings
        issueReadAllRequest();
        // install "read" button handler
        readButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	issueReadAllRequest();
                }
            }
        );
        // install "set" button handler
        setClockButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	issueClockSet();
                }
            }
        );
        // install "stop" clock button handler
        setStopNceButton.addActionListener( new ActionListener() {
        		public void actionPerformed(ActionEvent a) {
        			issueClockStop();
        		}
            }
        );
        // install "start" clock button handler
        setStartNceButton.addActionListener( new ActionListener() {
        		public void actionPerformed(ActionEvent a) {
        			issueClockStart();
        		}
            }
        );
        // install set fast clock ratio
        setRatioButton.addActionListener( new ActionListener() {
        		public void actionPerformed(ActionEvent a) {
        			issueClockRatio();
        		}
        	}
        );
        // install set 12/24 button
        set1224Button.addActionListener( new ActionListener() {
    		public void actionPerformed(ActionEvent a) {
    			issueClock1224();
    			}
        	}
        );

        if (clockMode == 0) {
        	setIndependent.setSelected(true);
        }
        if (clockMode == 1) {
        	setNce.setSelected(true);
        }
        if (clockMode == 2) {
        	setInternal.setSelected(true);
        }
        this.setSize(330, 180);

          // Create a Timebase listner for the Minute change events
        //minuteChangeListener = new java.beans.PropertyChangeListener() {
        //  public void propertyChange(java.beans.PropertyChangeEvent e) {
        //    newMinute();
        //  }
        //} ;
        
        // pack for display
        pack();
    }
    
    public void  message(NceMessage m) {}  // ignore replies

    public void reply(NceReply r) {
    	if (waiting <= 0) {
    		log.error(rb.getString("LogReplyEnexpected"));
    		return;
    	}
    	waiting--;
    	if (waitingForRead == true && r.getNumDataElements() == CS_CLOCK_MEM_SIZE) {
        	waitingForRead = false;
        	readClockPacket(r);
        	return;
    	}
    	if (waitingForCmdTime == true) {
    		if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
        		log.error(rb.getString("LogNceClockReplySizeError") + r.getNumDataElements());
        		return;
        	} else {
        		waitingForCmdTime = false;
        		if (r.getElement(0) != '!') {
        			log.error("NCE set clock replied: " + r.getElement(0));
        		}
        		return;
        	}
    	}
		if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
    		log.error(rb.getString("LogNceClockReplySizeError") + r.getNumDataElements());
    		return;
    	} else {
    		if (waitingForCmd1224 == true) {
        		waitingForCmd1224 = false;
        		if (r.getElement(0) != '!') {
        			log.error(rb.getString("LogNceClock1224CmdError") + r.getElement(0));
        		}
        		return;
        	}
    		if (waitingForCmdRatio == true) {
        		waitingForCmdRatio = false;
        		if (r.getElement(0) != '!') {
        			log.error(rb.getString("LogNceClockRatioCmdError") + r.getElement(0));
        		}
        		return;
        	}
    		if (waitingForCmdStop == true) {
        		waitingForCmdRatio = false;
        		if (r.getElement(0) != '!') {
        			log.error(rb.getString("LogNceClockStopCmdError") + r.getElement(0));
        		}
        		return;
        	}
    		if (waitingForCmdStart == true) {
        		waitingForCmdRatio = false;
        		if (r.getElement(0) != '!') {
        			log.error(rb.getString("LogNceClockStartCmdError") + r.getElement(0));
        		}
        		return;
        	}
    	}
		log.error(rb.getString("LogReplyEnexpected"));
		return;
    }
    
    private void readClockPacket (NceReply r) {
    	int hh = r.getElement(CS_CLOCK_HOURS) & 0xFF;
    	int mm = r.getElement(CS_CLOCK_MINUTES) & 0xFF;
    	int ss = r.getElement(CS_CLOCK_SECONDS) & 0xFF;
    	if (updateTimeFromRead == true) {
    		hours.setText("" + hh);
    		minutes.setText("" + mm);
    		seconds.setText("" + ss);
        	if (r.getElement(CS_CLOCK_1224) == 1) {
        		twentyFour.setSelected(true);
        		amPm.setText(" ");
        	} else {
        		twentyFour.setSelected(false);
            	if (r.getElement(CS_CLOCK_AMPM) == 'A') {
            		amPm.setText(rb.getString("TagAm"));
            	} else {
            		amPm.setText(rb.getString("TagPm"));
            	}
        	}
    		updateTimeFromRead = false;
    	}
    	int sc = r.getElement(CS_CLOCK_SCALE) & 0xFF;
    	if (updateRatioFromRead == true) {
    		rateNce.setText("" + 250 / sc);
    		updateRatioFromRead = false;
    	}
    	if (updateFormatFromRead == true) {
        	if (r.getElement(CS_CLOCK_1224) == 1) {
        		twentyFour.setSelected(true);
        	} else {
        		twentyFour.setSelected(false);
        	}
    		updateFormatFromRead = false;
    	}
    	if (updateStatusFromRead == true) {
    		if (r.getElement(CS_CLOCK_STATUS) == 1) {
    			status.setText(rb.getString("TagStopped"));
    		} else {
    			status.setText(rb.getString("TagRunning"));
    		}
    	}
    }
    
    private void issueReadAllRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateTimeFromRead = true;
		updateRatioFromRead = true;
		updateFormatFromRead = true;
		updateStatusFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueReadTimeRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateTimeFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueReadRatioRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateRatioFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueReadFormatRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateFormatFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueReadStatusRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateStatusFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueClockSet(){
    	int hh = Integer.parseInt(hours.getText());
    	int mm = Integer.parseInt(minutes.getText());
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClock(hh, mm);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdTime = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    	issueReadTimeRequest();
    }
    
    private void issueClockRatio() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClockRatio(Integer.parseInt(rateNce.getText()));
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdRatio = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    	issueReadRatioRequest();
    }
    
    private void issueClock1224() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClock1224(twentyFour.isSelected());
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmd1224 = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    	issueReadFormatRequest();
    }
    
    private void issueClockStop() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accStopClock();
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdStop = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    	issueReadStatusRequest();
    }
    
    private void issueClockStart() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accStartClock();
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdStart = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    	issueReadStatusRequest();
    }
    
    /*void correctFastClockMasterAction() {
      if( correctFastClockMaster.isSelected() )
      {
          // Set a flag to say we are not in sync
        inSyncWithFastClockMaster = false ;

          // Now enable the setting of the internal clock from the NCE Fast Clock Master
          // as this is the basis of us correcting the Fast Clock Master
        setInternal.setSelected( true );

          // Request Fast Clock Read
        //SlotManager.instance().sendReadSlot(LnConstants.FC_SLOT);
        InstanceManager.timebaseInstance().addMinuteChangeListener( minuteChangeListener );
      }
      else
      {
        log.debug( "correctExternalAction: Correction: Disabled" );
        InstanceManager.timebaseInstance().removeMinuteChangeListener( minuteChangeListener );
      }
    }*/

    public void newMinute()
    {
      //if( correctFastClockMaster.isSelected() && inSyncWithFastClockMaster )
      //{
        //Date now = clock.getTime();

        //LocoNetSlot s = SlotManager.instance().slot(LnConstants.FC_SLOT);
          // Set the Fast Clock Day to the current Day of the month 1-31
        //s.setFcDays(now.getDate());

        //s.setFcHours(now.getHours());
        //s.setFcMinutes(now.getMinutes());

        //long millis = now.getTime() ;
          // How many ms are we into the fast minute as we want to sync the
          // Fast Clock Master Frac_Mins to the right 65.535 ms tick
        //long elapsedMS = millis % 60000 ;
        //double frac_min = elapsedMS / 60000.0 ;
        //int ticks = 915 - (int)( 915 * frac_min ) ;

        //s.setFcFracMins( ticks );
        //LnTrafficController.instance().sendLocoNetMessage(s.writeSlot());
      //}
   }

    /**
     * Push GUI contents out to NCE Command Station.
     */
    void setContents() {
        //LocoNetSlot s = SlotManager.instance().slot(LnConstants.FC_SLOT);
        //s.setFcHours(Integer.parseInt(hours.getText()));
        //s.setFcMinutes(Integer.parseInt(minutes.getText()));
        //s.setFcRate(Integer.parseInt(rate.getText()));
        //s.setFcFracMins(Integer.parseInt(frac_mins.getText()));
        //LnTrafficController.instance().sendLocoNetMessage(s.writeSlot());
    }
    

    public void dispose() {

          // Remove ourselves from the Timebase minute rollover event
        //InstanceManager.timebaseInstance().removeMinuteChangeListener( minuteChangeListener );
        minuteChangeListener = null ;

        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ClockMonFrame.class.getName());

}
