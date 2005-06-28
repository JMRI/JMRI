// LV102InternalFrame.java

package jmri.jmrix.lenz.lv102;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgrammerException;

/**
 * Internal Frame displaying the LV102 configuration utility
 *
 * This is a configuration utility for the LV102.
 * It allows the user to set the Track Voltage  and E-line status.
 *
 * @author			Paul Bender  Copyright (C) 2005
 * @version			$Revision: 1.1 $
 */
public class LV102InternalFrame extends javax.swing.JInternalFrame implements jmri.ProgListener {

    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.lv102.LV102Bundle");

   public LV102InternalFrame() {

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	setTitle(rb.getString("LV102Power"));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel(rb.getString("LV102Track")));
        pane0.add(voltBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(new JLabel(rb.getString("LV102ELine")));
        pane1.add(eLineBox);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(new JLabel(rb.getString("LV102RailCom")));
        pane2.add(railComBox);
        pane2.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane2);


        JPanel pane3 = new JPanel();
	
	// Set the write button label and tool tip
	writeSettingsButton.setText(rb.getString("LV102WriteSettingsButtonLabel"));
	writeSettingsButton.setToolTipText(rb.getString("LV102WriteSettingsButtonToolTip"));
	
        pane3.add(writeSettingsButton);

	// Set the reset button label and tool tip
	resetButton.setText(rb.getString("LV102ResetButtonLabel"));
	resetButton.setToolTipText(rb.getString("LV102ResetButtonToolTip"));

        pane3.add(resetButton);
        getContentPane().add(pane3);

        // Initilize the Combo Boxes
        voltBox.setVisible(true);
        voltBox.setToolTipText(rb.getString("LV102TrackTip"));
        for (int i=0; i<validVoltage.length;i++)
        {
           voltBox.addItem(validVoltage[i]);
        }
	voltBox.setSelectedIndex(10);

        eLineBox.setVisible(true);
        eLineBox.setToolTipText(rb.getString("LV102ELineTip"));
        for (int i=0; i<validELineStatus.length;i++)
        {
           eLineBox.addItem(validELineStatus[i]);
        }
	eLineBox.setSelectedIndex(0);

        railComBox.setVisible(true);
        railComBox.setToolTipText(rb.getString("LV102RailComTip"));
        for (int i=0; i<validRailComStatus.length;i++)
        {
           railComBox.addItem(validRailComStatus[i]);
        }
	railComBox.setSelectedIndex(0);

        CurrentStatus.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        CurrentStatus.setVisible(true);
	CurrentStatus.setText(rb.getString("LV102StatusInitial"));
        getContentPane().add(CurrentStatus);

        // and prep for display
        pack();

        writeSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	writeLV102Settings();
			writeSettingsButton.setSelected(false);
                }
            }
        );

        // install reset button handler
        resetButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	resetLV102Settings();

                }
            }
        );

	// install a handler to set the status line when the selected item 
	// changes in the e-Line box
	eLineBox.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			CurrentStatus.setText(rb.getString("LV102StatusChanged"));
		}
	   }
	);

	// install a handler to set the status line when the selected item 
	// changes in the RailCom box
	railComBox.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			CurrentStatus.setText(rb.getString("LV102StatusChanged"));
		}
	   }
	);

	// install a handler to set the status line when the selected item 
	// changes in the volt box
	voltBox.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			CurrentStatus.setText(rb.getString("LV102StatusChanged"));
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
    }

    boolean read = false;

    JComboBox voltBox = new javax.swing.JComboBox();
    JComboBox eLineBox = new javax.swing.JComboBox();
    JComboBox railComBox = new javax.swing.JComboBox();

    JLabel CurrentStatus = new JLabel(" ");

    JToggleButton writeSettingsButton = new JToggleButton("Write to LV102");
    JToggleButton resetButton = new JToggleButton("Reset to Factory Defaults");

    protected String [] validVoltage= new String[]{"11V","11.5V","12V","12.5V","13V","13.5V","14V","14.5V","15V","15.5V","16V (factory default)","16.5V","17V","17.5V","18V","18.5V","19V","19.5V","20V","20.5V","21V","21.5V","22V"};
    protected int [] validVoltageValues = new int[]{22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44};

    protected String [] validELineStatus = new String[]{rb.getString("LV102ELineActive"),rb.getString("LV102ELineInactive"),rb.getString("LV102ELineDefault")};
    protected int [] validELineStatusValues = new int[]{90,91,99};

    protected String [] validRailComStatus = new String[]{rb.getString("LV102RailComActive"),rb.getString("LV102RailComInactive")};
    protected int [] validRailComStatusValues = new int[]{93,92};

    //Send Power Station settings
    void writeLV102Settings() {

        Programmer opsProg = jmri.InstanceManager.programmerManagerInstance()
                                    .getOpsModeProgrammer(false,00);

        if((String)voltBox.getSelectedItem()!="" &&
           (String)voltBox.getSelectedItem()!=null) {
        
          if(log.isDebugEnabled()) log.debug("Selected Voltage: " +voltBox.getSelectedItem()); 

          /* First, send the ops mode programing command to enter
          programing mode */
 	  try {
              opsProg.writeCV(7,50,this);
            } catch(ProgrammerException e) {
              // Don't do anything with this yet
            }

          /* Next, send the ops mode programing command for the voltage 
          we want */
 	  try {
              opsProg.writeCV(7,validVoltageValues[voltBox.getSelectedIndex()],this);
            } catch(ProgrammerException e) {
              // Don't do anything with this yet
            }
        } else { 
		if(log.isDebugEnabled()) log.debug("No Voltage Selected");
	}

	CurrentStatus.setText(rb.getString("LV102StatusWriteVolt"));

        if((String)eLineBox.getSelectedItem()!="" &&
           (String)eLineBox.getSelectedItem()!=null) {
          if(log.isDebugEnabled()) log.debug("E-Line Setting: " +eLineBox.getSelectedItem());
          /* First, send the ops mode programing command to enter
          programing mode */
   	  try {
              opsProg.writeCV(7,50,this);
            } catch(ProgrammerException e) {
              // Don't do anything with this yet
            }
          /* Next, send the ops mode programing command for the E line 
             Status we want */
    	  try {
              opsProg.writeCV(7,validELineStatusValues[eLineBox.getSelectedIndex()],this);
            } catch(ProgrammerException e) {
              // Don't do anything with this yet
            }
        } else { 
    	        if(log.isDebugEnabled()) log.debug("No E-Line value Selected");
        }

        CurrentStatus.setText(rb.getString("LV102StatusWriteELine"));


        if((String)railComBox.getSelectedItem()!="" &&
           (String)railComBox.getSelectedItem()!=null) {
          if(log.isDebugEnabled()) log.debug("RailCom Setting: " +eLineBox.getSelectedItem());
          /* First, send the ops mode programing command to enter
          programing mode */
   	  try {
              opsProg.writeCV(7,50,this);
            } catch(ProgrammerException e) {
              // Don't do anything with this yet
            }
          /* Next, send the ops mode programing command for the E line 
             Status we want */
    	  try {
              opsProg.writeCV(7,validRailComStatusValues[railComBox.getSelectedIndex()],this);
            } catch(ProgrammerException e) {
              // Don't do anything with this yet
            }
        } else { 
    	        if(log.isDebugEnabled()) log.debug("No RailCom value Selected");
        }

	CurrentStatus.setText(rb.getString("LV102StatusWriteRailCom"));

        jmri.InstanceManager.programmerManagerInstance()
                    .releaseOpsModeProgrammer(opsProg);
    }

    /**
      *  This class is a programmer listener, so we implement the 
      * programmingOpReply() function
      */
    public void programmingOpReply(int value, int status) {
                if(log.isDebugEnabled()) log.debug("Programming Operation reply recieved, value is " + value + " ,status is " +status);
		if(status==ProgListener.ProgrammerBusy) {
			CurrentStatus.setText(rb.getString("LV102StatusBUSY"));
	        } else if(status==ProgListener.OK) {
			CurrentStatus.setText(rb.getString("LV102StatusOK"));
		} else {
			CurrentStatus.setText(rb.getString("LV102StatusUnknown"));
	        }
        }      

    // Set to default values.  Voltage is 16, E Line is Active. 
    void resetLV102Settings() {
	voltBox.setSelectedIndex(10);
	eLineBox.setSelectedIndex(0);
	CurrentStatus.setText(rb.getString("LV102StatusInitial"));
    }

    public void dispose() {
        // take apart the JInternalFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LV102InternalFrame.class.getName());

}
