package jmri.jmrix.loconet.locoid;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * User interface for setting the LocoNet ID.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2010
 */
public class LocoIdPanel extends jmri.jmrix.loconet.swing.LnPanel implements
        LocoNetListener {

    // member declarations
    javax.swing.JButton readButton;
    javax.swing.JButton setButton;
    javax.swing.JTextArea value;

    javax.swing.JComboBox<String> idBox;
    String IDValues[] = {"-", "0", "1", "2", "3", "4", "5", "6", "7"};

    public LocoIdPanel() {
        super();
        idBox = new javax.swing.JComboBox<String>(IDValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {

        // Create our UI elements, two buttons and a drop-down.
        setButton = new javax.swing.JButton(Bundle.getMessage("ButtonSet"));
        readButton = new javax.swing.JButton(Bundle.getMessage("ButtonRead"));

        // Do our layout, two buttons side by side, drop down below.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(readButton);
        p.add(setButton);

        add(p);

        p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(new JLabel(Bundle.getMessage("LabelValue")));
        p.add(idBox);

        add(p);

        // Set our callbacks
        setButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setButtonActionPerformed();
            }
        });
        readButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                readButtonActionPerformed();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.locoid.LocoIdFrame"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemSetID"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        // connect to the LnTrafficController
        connect(memo.getLnTrafficController());

        // press the Read button for the user, so we populate the current value
        readButtonActionPerformed();
    }

    /**
     * Callback when someone presses the Set button
     */
    public void setButtonActionPerformed() {
        String value = (String) idBox.getSelectedItem();

        if (!value.equals("-")) {
            memo.getLnTrafficController().sendLocoNetMessage(
                    createSetPacket(value));
        }
    }

    /**
     * Callback when someone presses the Read button
     */
    public void readButtonActionPerformed() {
        // We set the display to "-" until the callback gets the value from the
        // LocoNet
        idBox.setSelectedIndex(0);
        memo.getLnTrafficController().sendLocoNetMessage(createReadPacket());
    }

    /**
     * Process the incoming message, see if it is a panel response, and if so
     * parse the LocoNet ID. Use that value to set the ID box.
     *
     * This is the callback called by the LnTrafficController
     *
     * @param m Inbound LocoNet message to check.
     */
    @Override
    public void message(LocoNetMessage m) {

        // The message is 6 bytes long.
        if (m.getNumDataElements() != 6) {
            return;
        }

        int b1 = m.getOpCode();
        int b2 = m.getElement(1);
        int b3 = m.getElement(2);
        int b4 = m.getElement(3) & 0x07; // UR-92's set bit 4 for duplex

        // Response code is D7 {12, 17, 1F} 00 <value>
        if ((b1 == 0xD7)
                && ((b2 == 0x12) || (b2 == 0x17) || (b2 == 0x1F))
                && (b3 == 0x00)) {
            // We start with "-", so index + 1
            idBox.setSelectedIndex(b4 + 1);
        }
    }

    /**
     * Create a LocoNet packet to Query panels for the LocoNet ID
     *
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
     * Create a LocoNet packet to set the LocoNet ID.
     *
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

    /**
     * Tell the LocoNet controller we want to hear messages, which will
     * automatically call our "message"
     *
     * @param t LocoNet instance to connect to
     */
    public void connect(LnTrafficController t) {
        t.addLocoNetListener(~0, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        memo.getLnTrafficController().removeLocoNetListener(~0, this);
        super.dispose();
    }

}
