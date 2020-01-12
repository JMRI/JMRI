package jmri.jmrix.lenz.swing.li101;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying the LI101 configuration utility
 * <p>
 * This is a configuration utility for the LI101. It allows the user to set the
 * XpressNet Address and the port speed used to communicate with the LI101.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 */
public class LI101Frame extends jmri.util.JmriJFrame implements XNetListener {

    protected XNetTrafficController tc = null;

    public LI101Frame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(Bundle.getMessage("MenuItemLI101ConfigurationManager"));
        tc = memo.getXNetTrafficController();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel(Bundle.getMessage("XNetAddressLabel")));
        pane0.add(addrBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane1 = new JPanel();
        pane1.add(new JLabel(Bundle.getMessage("LI101SpeedSettingLabel")));
        pane1.add(speedBox);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.add(readSettingsButton);
        pane2.add(writeSettingsButton);
        pane2.add(resetButton);
        resetButton.setToolTipText(Bundle.getMessage("ResetDefaultsToolTip"));
        pane2.add(closeButton);
        getContentPane().add(pane2);

        // Initialize the comboBoxes
        addrBox.setVisible(true);
        addrBox.setToolTipText(Bundle.getMessage("XNetAddressToolTip"));
        for (int i = 0; i < validXNetAddresses.length; i++) {
            addrBox.addItem(validXNetAddresses[i]);
        }
        addrBox.setSelectedIndex(32);

        speedBox.setVisible(true);
        speedBox.setToolTipText(Bundle.getMessage("LI101SpeedSettingToolTip"));
        for (int i = 0; i < validSpeeds.length; i++) {
            speedBox.addItem(validSpeeds[i]);
        }
        speedBox.setSelectedIndex(4);

        // and prep for display
        pack();

        status.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(status);

        // install read settings, write settings button handlers
        readSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                readLI101Settings();
            }
        }
        );

        writeSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                writeLI101Settings();
            }
        }
        );

        // install close button handler
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                setVisible(false);
                dispose();
            }
        }
        );

        // install reset button handler
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                resetLI101Settings();

            }
        }
        );

        // add status
        getContentPane().add(status);

        if (tc != null) {
            tc.addXNetListener(~0, this);
        } else {
            log.warn("No XpressNet connection, so panel won't function");
        }

    }

    boolean read = false;

    JComboBox<String> addrBox = new javax.swing.JComboBox<String>();
    JComboBox<String> speedBox = new javax.swing.JComboBox<String>();

    JLabel status = new JLabel("");

    JToggleButton readSettingsButton = new JToggleButton(Bundle.getMessage("LI101ReadButton"));
    JToggleButton writeSettingsButton = new JToggleButton(Bundle.getMessage("LI101WriteButton"));
    JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));
    JButton resetButton = new JButton(Bundle.getMessage("ButtonResetDefaults"));

    protected String[] validXNetAddresses = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27",
            "28", "29", "30", "31", ""};

    protected String[] validSpeeds = new String[]{Bundle.getMessage("LIBaud19200"), Bundle.getMessage("Baud38400"),
            Bundle.getMessage("Baud57600"), Bundle.getMessage("Baud115200"), ""};
    protected int[] validSpeedValues = new int[]{19200, 38400, 57600, 115200};

    /**
     * Send new address/baud rate to LI101.
     */
    void writeLI101Settings() {
        if (!(((String) addrBox.getSelectedItem()).equals(""))
                && (String) addrBox.getSelectedItem() != null) {
            /* First, we take care of generating an address request */
            XNetMessage msg = XNetMessage.getLIAddressRequestMsg(
                    addrBox.getSelectedIndex());
            //Then send to the controller
            tc.sendXNetMessage(msg, this);
        }
        if (!(((String) speedBox.getSelectedItem()).equals(""))
                && (String) speedBox.getSelectedItem() != null) {
            /* Now, we can send a baud rate request */
            XNetMessage msg = XNetMessage.getLISpeedRequestMsg(speedBox.getSelectedIndex() + 1);
            //Then send to the controller
            tc.sendXNetMessage(msg, this);
        }
    }

    /**
     * Send Information request to LI101.
     */
    void readLI101Settings() {
        /* First, we request setting an out of range address
         to get the current value. */
        XNetMessage msg = XNetMessage.getLIAddressRequestMsg(32);
        //Then send to the controller
        tc.sendXNetMessage(msg, this);

        /* Next, we request setting an out of range speed request
         to get the current value. */
        XNetMessage msg2 = XNetMessage.getLISpeedRequestMsg(6);
        //Then send to the controller
        tc.sendXNetMessage(msg2, this);
    }

    /**
     * Listen for responces from the LI101.
     */
    @Override
    public void message(XNetReply l) {
        // Check to see if this is an LI101 info request messgage, if it
        //is, determine if it's the baud rate setting, or the address
        //setting
        if (l.getElement(0) == XNetConstants.LI101_REQUEST) {
            if (l.getElement(1) == XNetConstants.LI101_REQUEST_ADDRESS) {
                // The third element is the address
                addrBox.setSelectedIndex(l.getElement(2));
                status.setText("Address" + l.getElement(2) + "received from LI101");
            } else if (l.getElement(1) == XNetConstants.LI101_REQUEST_BAUD) {
                // The third element is the encoded Baud rate
                speedBox.setSelectedIndex(l.getElement(2) - 1);
                status.setText("Baud rate" + validSpeeds[l.getElement(2) - 1] + "received from LI101");
            }
        }
    }

    /**
     * Listen for the messages to the LI101.
     */
    @Override
    public void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /**
     * Reset this LI101.
     * <p>
     * For now, Reset just resets the screen to factory defaults, and does
     * not send any information to the LI101F.  To do a reset, we need to
     * send a BREAK signal for 200 milliseconds.
     * Default values are 30 for the address, and 19,200 baud for the
     * speed.
     */
    void resetLI101Settings() {
        addrBox.setSelectedIndex(30);
        speedBox.setSelectedIndex(0);
    }

    @Override
    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LI101Frame.class);

}
