// LV102Frame.java

package jmri.jmrix.lenz.lv102;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;
import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgrammerException;

/**
 * Frame displaying the LV102 configuration utility
 *
 * This is a configuration utility for the LV102.
 * It allows the user to set the Track Voltage  and E-line status.
 *
 * @author			Paul Bender  Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public class LV102Frame extends JFrame implements jmri.ProgListener {

    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.lv102.LV102Bundle");

   public LV102Frame() {
      this("LV102 Configuration Utility");
   }

   public LV102Frame(String FrameName) {

	super(FrameName);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel(rb.getString("LV102Power")));
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
        pane2.add(writeSettingsButton);
        pane2.add(resetButton);
        pane2.add(closeButton);
        getContentPane().add(pane2);

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

        CurrentStatus.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        CurrentStatus.setVisible(true);
        getContentPane().add(CurrentStatus);

        // and prep for display
        pack();


        writeSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	writeLV102Settings();
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
                	resetLV102Settings();

                }
            }
        );

        // add status
        getContentPane().add(CurrentStatus);

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });
    }

    boolean read = false;

    JComboBox voltBox = new javax.swing.JComboBox();
    JComboBox eLineBox = new javax.swing.JComboBox();

    JLabel CurrentStatus = new JLabel(" ");

    JToggleButton writeSettingsButton = new JToggleButton("Write to LV102");
    JToggleButton closeButton = new JToggleButton("Close");
    JToggleButton resetButton = new JToggleButton("Reset to Factory Defaults");

    protected String [] validVoltage= new String[]{"11V","11.5V","12V","12.5V","13V","13.5V","14V","14.5V","15V","15.5V","16V","16.5V","17V","17.5V","18V","18.5V","19V","19.5V","20V","20.5V","21V","21.5V","22V"};
    protected int [] validVoltageValues = new int[]{22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44};

    protected String [] validELineStatus = new String[]{rb.getString("LV102ELineActive"),rb.getString("LV102ELineInactive"),rb.getString("LV102ELineDefault")};
    protected int [] validELineStatusValues = new int[]{90,91,99};

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

     CurrentStatus.setText(rb.getString("LV102StatusWriteVolt"));

     jmri.InstanceManager.programmerManagerInstance()
                    .releaseOopsModeProgrammer(opsProg);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LV102Frame.class.getName());

}
