// LocoIdFrame.java

package jmri.jmrix.loconet.locoid;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import java.util.ResourceBundle;

import java.awt.Dimension;
import javax.swing.*;

/**
 * User interface for setting the LocoNet ID
 *
 * @author			Bob Jacobsen   Copyright (C) 2006
 * @version			$Revision: 1.6 $
 */
public class LocoIdFrame extends jmri.util.JmriJFrame implements LocoNetListener {

    // member declarations
    javax.swing.JButton readButton;
    javax.swing.JButton setButton;
    javax.swing.JTextField value;

    public LocoIdFrame() {
        super();
        setTitle(ResourceBundle.getBundle("jmri.jmrix.loconet.locoid.LocoId").getString("Title"));
    }

    public LocoIdFrame(String arg) {
        super(arg);
    }

    public void initComponents() throws Exception {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.loconet.locoid.LocoId");

        setButton = new javax.swing.JButton(rb.getString("ButtonSet"));
        readButton = new javax.swing.JButton(rb.getString("ButtonRead"));
        value = new javax.swing.JTextField(2);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(readButton);
        p.add(setButton);

        getContentPane().add(p);

        p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(new JLabel(rb.getString("LabelValue")));
        p.add(value);

        getContentPane().add(p);

        setButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setButtonActionPerformed();
                }
            });
        readButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    readButtonActionPerformed();
                }
            });

        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.loconet.locoid.LocoIdFrame", true);

        // pack to format display
        pack();

        // connect to the LnTrafficController
        connect(LnTrafficController.instance());

        // prompt for an update
        readButtonActionPerformed();
    }

    public void setButtonActionPerformed() {
        tc.sendLocoNetMessage(createSetPacket(value.getText()));
    }
    public void readButtonActionPerformed() {
        tc.sendLocoNetMessage(createReadPacket());
    }

    /**
     * Process the incoming message to look for the address
     * @param m
     */
    public void message(LocoNetMessage m) {
        if (m.getNumDataElements() != 6) return;
        if ( (m.getElement(0)&0xFF) != 0xD7) return;
        if ( ((m.getElement(1)&0xFF) != 0x1F) && ((m.getElement(1)&0xFF) != 0x17)) return;
        if ( (m.getElement(2)&0xFF) != 0x00) return;
        value.setText(""+m.getElement(3));
    }


    /**
     * Create a LocoNet packet to read the LocoNet ID
     * @return The packet, with contents filled-in
     */
    LocoNetMessage createReadPacket() {
        LocoNetMessage m = new LocoNetMessage(6);
        m.setElement(0, 0xDF);
        m.setElement(1, 0x00);
        m.setElement(2, 0x00);
        m.setElement(3, 0x00);
        m.setElement(4, 0x00);

        return m;
    }

    /**
     * Create a LocoNet packet to set the LocoNet ID
     * @param s The desired value as a string in decimal
     * @return The packet, with contents filled-in
     */
    LocoNetMessage createSetPacket(String s) {
        // convert to int value
        int data = Integer.parseInt(s);
        // format packet
        LocoNetMessage m = new LocoNetMessage(6);
        m.setElement(0, 0xDF);
        m.setElement(1, 0x40);
        m.setElement(2, 0x1F);
        m.setElement(3, data);
        m.setElement(4, 0x00);
        return m;
    }

    // connect to the LnTrafficController
    public void connect(LnTrafficController t) {
        tc = t;
        tc.addLocoNetListener(~0, this);
    }

    public void dispose() {
        tc.removeLocoNetListener(~0, this);
        super.dispose();
    }

    // private data
    private LnTrafficController tc = null;

}
