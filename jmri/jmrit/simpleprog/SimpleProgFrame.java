// SimpleProgFrame.java

package jmri.jmrit.simpleprog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgModePane;

/**
 * Frame providing a simple command station programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.4 $
 */
public class SimpleProgFrame extends javax.swing.JFrame implements jmri.ProgListener {

    // GUI member declarations
    javax.swing.JToggleButton readButton 	= new javax.swing.JToggleButton();
    javax.swing.JToggleButton writeButton 	= new javax.swing.JToggleButton();
    javax.swing.JTextField  addrField       = new javax.swing.JTextField(4);
    javax.swing.JTextField  valField        = new javax.swing.JTextField(4);

    jmri.ProgModePane       modePane        = new jmri.ProgModePane(BoxLayout.Y_AXIS);

    javax.swing.ButtonGroup radixGroup 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton hexButton    	= new javax.swing.JRadioButton();
    javax.swing.JRadioButton decButton   	= new javax.swing.JRadioButton();

    javax.swing.JLabel       resultsField   = new javax.swing.JLabel(" ");

    public SimpleProgFrame() {

        // configure items for GUI
        readButton.setText("Read CV");
        readButton.setToolTipText("Read the value from the selected CV");

        writeButton.setText("Write CV");
        writeButton.setToolTipText("Write the value to the selected CV");

        hexButton.setText("Hexadecimal");
        decButton.setText("Decimal");
        decButton.setSelected(true);

        // add the actions to the buttons
        readButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                readPushed(e);
            }
        });
        writeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                writePushed(e);
            }
        });

        resultsField.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // general GUI config
        setTitle("Simple Programmer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        javax.swing.JPanel tPane;  // temporary pane for layout
        javax.swing.JPanel tPane2;

        tPane = new JPanel();
        tPane.setLayout(new BoxLayout(tPane, BoxLayout.X_AXIS));
        tPane.add(readButton);
        tPane.add(writeButton);
        getContentPane().add(tPane);

        tPane = new JPanel();
        tPane.setLayout(new GridLayout(2,2));
        tPane.add(new JLabel("Address:"));
        tPane.add(addrField);
        tPane.add(new JLabel("Value:"));
        tPane.add(valField);
        getContentPane().add(tPane);

        getContentPane().add(new JSeparator());

        tPane = new JPanel();
        tPane.setLayout(new BoxLayout(tPane, BoxLayout.X_AXIS));

        tPane.add(modePane);

        tPane.add(new JSeparator(javax.swing.SwingConstants.VERTICAL));

        tPane2 = new JPanel();
        tPane2.setLayout(new GridLayout(3,1));
        radixGroup.add(decButton);
        radixGroup.add(hexButton);
        tPane2.add(new JLabel("Value is:"));
        tPane2.add(decButton);
        tPane2.add(hexButton);
        tPane.add(tPane2);

        getContentPane().add(tPane);

        getContentPane().add(new JSeparator());

        getContentPane().add(resultsField);

        pack();
    }

    // utility function to get value, handling radix
    private int getNewVal() {
        try {
            if (decButton.isSelected())
                return Integer.valueOf(valField.getText()).intValue();
            else
                return Integer.valueOf(valField.getText(),16).intValue();
        } catch (java.lang.NumberFormatException e) {
            valField.setText("");
            return 0;
        }
    }
    private int getNewAddr() {
        try {
            return Integer.valueOf(addrField.getText()).intValue();
        } catch (java.lang.NumberFormatException e) {
            addrField.setText("");
            return 0;
        }
    }
    private int getNewMode() {
        return modePane.getMode();
    }

    public String statusCode(int status) {
        String temp;
        if (status == jmri.ProgListener.OK)
            temp = "OK. ";
        else
            temp = "Error. ";
        if ((status & jmri.ProgListener.NoLocoDetected) != 0)
            temp += "No Locomotive on programming track. ";
        if ((status & jmri.ProgListener.NoAck) != 0)
            temp += "Decoder acknowledge not seen. ";
        return temp;
    }

    // listen for messages from the Programmer object
    public void programmingOpReply(int value, int status) {
        resultsField.setText(statusCode(status));

        //operation over, raise the buttons
        readButton.setSelected(false);
        writeButton.setSelected(false);

        // capture the read value
        if (value !=-1)  // -1 implies nothing being returned
            if (decButton.isSelected())
                valField.setText(""+value);
            else
                valField.setText(Integer.toHexString(value));
    }

    // handle the buttons being pushed
    public void readPushed(java.awt.event.ActionEvent e) {
        Programmer p = jmri.InstanceManager.programmerManagerInstance().getServiceModeProgrammer();
        if (p == null) {
            resultsField.setText("No programmer connected");
            readButton.setSelected(false);
        } else {
            try {
                resultsField.setText("programming...");
                p.readCV(getNewAddr(), this);
            } catch (jmri.ProgrammerException ex) {
                resultsField.setText(""+ex);
                readButton.setSelected(false);
            }
        }
    }

    public void writePushed(java.awt.event.ActionEvent e) {
        Programmer p = jmri.InstanceManager.programmerManagerInstance().getServiceModeProgrammer();
        if (p == null) {
            resultsField.setText("No programmer connected");
            writeButton.setSelected(false);
        } else {
            try {
                resultsField.setText("programming...");
                p.writeCV(getNewAddr(),getNewVal(), this);
            } catch (jmri.ProgrammerException ex) {
                resultsField.setText(""+ex);
                writeButton.setSelected(false);
            }
        }
    }

    // handle resizing when first shown
    private boolean mShown = false;
    public void addNotify() {
    	super.addNotify();
        if (mShown)
            return;
        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        modePane.dispose();
        dispose();
	// and disconnect from the SlotManager

    }

}
