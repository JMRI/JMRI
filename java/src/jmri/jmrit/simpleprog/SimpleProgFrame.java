// SimpleProgFrame.java
package jmri.jmrit.simpleprog;

import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import jmri.ProgListener;
import jmri.Programmer;

/**
 * Frame providing a simple command station programmer
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2007
 * @author Giorgio Terdina Copyright (C) 2007
 * @version	$Revision$
 */
public class SimpleProgFrame extends jmri.util.JmriJFrame implements jmri.ProgListener {

    /**
     *
     */
    // GUI member declarations
    javax.swing.JToggleButton readButton = new javax.swing.JToggleButton();
    javax.swing.JToggleButton writeButton = new javax.swing.JToggleButton();
    javax.swing.JTextField addrField = new javax.swing.JTextField(4);
    javax.swing.JTextField valField = new javax.swing.JTextField(4);

    jmri.jmrit.progsupport.ProgModePane modePane = new jmri.jmrit.progsupport.ProgModePane(BoxLayout.Y_AXIS);

    javax.swing.ButtonGroup radixGroup = new javax.swing.ButtonGroup();
    javax.swing.JRadioButton hexButton = new javax.swing.JRadioButton();
    javax.swing.JRadioButton decButton = new javax.swing.JRadioButton();

    javax.swing.JLabel resultsField = new javax.swing.JLabel(" ");

    public SimpleProgFrame() {
        super();

        // configure items for GUI
        readButton.setText(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("READ CV"));
        readButton.setToolTipText(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("READ THE VALUE FROM THE SELECTED CV"));

        writeButton.setText(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("WRITE CV"));
        writeButton.setToolTipText(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("WRITE THE VALUE TO THE SELECTED CV"));

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
        decButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decHexButtonChanged(e);
            }
        });
        hexButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decHexButtonChanged(e);
            }
        });

        resultsField.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // general GUI config
        setTitle(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("SIMPLE PROGRAMMER"));
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
        tPane.setLayout(new GridLayout(2, 2));
        tPane.add(new JLabel(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("CV NUMBER:")));
        tPane.add(addrField);
        tPane.add(new JLabel(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("VALUE:")));
        tPane.add(valField);
        getContentPane().add(tPane);

        getContentPane().add(new JSeparator());

        tPane = new JPanel();
        tPane.setLayout(new BoxLayout(tPane, BoxLayout.X_AXIS));

        tPane.add(modePane);

        tPane.add(new JSeparator(javax.swing.SwingConstants.VERTICAL));

        tPane2 = new JPanel();
        tPane2.setLayout(new BoxLayout(tPane2, BoxLayout.Y_AXIS));
        radixGroup.add(decButton);
        radixGroup.add(hexButton);
        tPane2.add(new JLabel(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("VALUE IS:")));
        tPane2.add(decButton);
        tPane2.add(hexButton);
        tPane2.add(Box.createVerticalGlue());
        tPane.add(tPane2);

        getContentPane().add(tPane);

        getContentPane().add(new JSeparator());

        getContentPane().add(resultsField);

        if (modePane.getProgrammer() != null) {
            readButton.setEnabled(modePane.getProgrammer().getCanRead());
        } else {
            readButton.setEnabled(false);
        }

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.simpleprog.SimpleProgFrame", true);

        pack();
    }

    // utility function to get value, handling radix
    private int getNewVal() {
        try {
            if (decButton.isSelected()) {
                return Integer.valueOf(valField.getText()).intValue();
            } else {
                return Integer.valueOf(valField.getText(), 16).intValue();
            }
        } catch (java.lang.NumberFormatException e) {
            valField.setText("");
            return 0;
        }
    }

    private String getNewAddr() {
        if (addrField.getText()!=null && !addrField.getText().equals("")) {
            return addrField.getText();
        } else {
            addrField.setText("0");
            return addrField.getText();
        }
    }

    private String statusCode(int status) {
        Programmer p = modePane.getProgrammer();
        if (status == ProgListener.OK) {
            return "OK";
        }
        if (p == null) {
            return "No programmer connected";
        } else {
            return p.decodeErrorCode(status);
        }
    }

    // listen for messages from the Programmer object
    public void programmingOpReply(int value, int status) {
        resultsField.setText(statusCode(status));

        //operation over, raise the buttons
        readButton.setSelected(false);
        writeButton.setSelected(false);

        // capture the read value
        if (value != -1) // -1 implies nothing being returned
        {
            if (decButton.isSelected()) {
                valField.setText("" + value);
            } else {
                valField.setText(Integer.toHexString(value));
            }
        }
    }

    // handle the buttons being pushed
    public void readPushed(java.awt.event.ActionEvent e) {
        Programmer p = modePane.getProgrammer();
        if (p == null) {
            resultsField.setText("No programmer connected");
            readButton.setSelected(false);
        } else {
            if (p.getCanRead()) {
                try {
                    resultsField.setText("programming...");
                    p.readCV(getNewAddr(), this);
                } catch (jmri.ProgrammerException ex) {
                    resultsField.setText("" + ex);
                    readButton.setSelected(false);
                }
            } else {
                resultsField.setText("can't read in this Mode");
                readButton.setSelected(false);
            }
        }
    }

    public void writePushed(java.awt.event.ActionEvent e) {
        Programmer p = modePane.getProgrammer();
        if (p == null) {
            resultsField.setText("No programmer connected");
            writeButton.setSelected(false);
        } else {
            try {
                resultsField.setText("programming...");
                p.writeCV(getNewAddr(), getNewVal(), this);
            } catch (jmri.ProgrammerException ex) {
                resultsField.setText("" + ex);
                writeButton.setSelected(false);
            }
        }
    }

    // provide simple data conversion if dec or hex button changed
    public void decHexButtonChanged(java.awt.event.ActionEvent e) {
        resultsField.setText("OK");
        if (valField.getText().equals("")) {
            return;
        }
        int value = 0;
        try {
            if (decButton.isSelected()) // convert from hex to dec
            {
                value = Integer.valueOf(valField.getText(), 16).intValue();
            } else // convert from dec to hex
            {
                value = Integer.valueOf(valField.getText()).intValue();
            }
        } catch (java.lang.NumberFormatException ee) {
            resultsField.setText("error");
        }
        if (value != 0) {
            if (decButton.isSelected()) {
                valField.setText(Integer.toString(value));
            } else {
                valField.setText(Integer.toHexString(value));
            }
        }
    }

    public void dispose() {
        modePane.dispose();
        super.dispose();
    }

}
