// LI101Frame.java

package jmri.jmrix.lenz.li101;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Frame displaying the LI101 configuration utility
 *
 * Need to add documentation on how this works
 *
 * @author			Paul Bender  Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class LI101Frame extends JFrame {

    public LI101Frame() {
        super("LI101 Configuration Utility");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel("Xpressnet address: "));
        pane0.add(addrBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(new JLabel("LI101 Speed Setting"));
        pane1.add(speedBox);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

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

        speedBox.setVisible(true);
        speedBox.setToolTipText("Select the XpressNet address");
        for (int i=0; i<validSpeeds.length;i++)
        {
           speedBox.addItem(validSpeeds[i]);
        }


        // and prep for display
        pack();

        status.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(status);

        // install read settings, write settings button handlers
        readSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	//readLI101Settings();
                }
            }
        );

        writeSettingsButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	//writeLI101Settings();
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
                	//resetLI101Settings();

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

    }

    boolean read = false;

    JComboBox addrBox = new javax.swing.JComboBox();
    JComboBox speedBox = new javax.swing.JComboBox();

    JLabel status = new JLabel("No Status at this point");

    JToggleButton readSettingsButton = new JToggleButton("Read from LI101");
    JToggleButton writeSettingsButton = new JToggleButton("Write to LI101");
    JToggleButton closeButton = new JToggleButton("Close");
    JToggleButton resetButton = new JToggleButton("Reset to Factory Defaults");

    protected String [] validXNetAddresses= new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};

    protected String [] validSpeeds = new String[]{"19,200 baud","38,400 baud","57,600 baud","115,200 baud"};
    protected int [] validSpeedValues = new int[]{19200,38400,57600,115200};


    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LI101Frame.class.getName());

}
