package jmri.jmrix.lenz.swing.liusb;

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
 * Frame displaying the LIUSB configuration utility.
 * <p>
 * This is a configuration utility for the LIUSB. It allows the user to set the
 * XpressNet Address and the port speed used to communicate with the LIUSB.
 *
 * @author Paul Bender Copyright (C) 2009-2010
 */
public class LIUSBConfigFrame extends jmri.util.JmriJFrame implements XNetListener {

    protected XNetTrafficController tc = null;

    public LIUSBConfigFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(Bundle.getMessage("MenuItemLIUSBConfigurationManager"));
        tc = memo.getXNetTrafficController();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel(Bundle.getMessage("XNetAddressLabel")));
        pane0.add(addrBox);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        JPanel pane2 = new JPanel();
        pane2.add(readSettingsButton);
        pane2.add(writeSettingsButton);
        pane2.add(resetButton);
        resetButton.setToolTipText(Bundle.getMessage("ResetDefaultsToolTip"));
        pane2.add(closeButton);
        getContentPane().add(pane2);

        // Initilize the Combo Boxes
        addrBox.setVisible(true);
        addrBox.setToolTipText(Bundle.getMessage("XNetAddressToolTip"));
        for (int i = 0; i < validXNetAddresses.length; i++) {
            addrBox.addItem(validXNetAddresses[i]);
        }
        addrBox.setSelectedIndex(32);

        status.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(status);

        // and prep for display
        pack();

        // install read settings, write settings button handlers
        readSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                readLIUSBSettings();
            }
        }
        );

        writeSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                writeLIUSBSettings();
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
                resetLIUSBSettings();
            }
        }
        );

        if (tc != null) {
            tc.addXNetListener(~0, this);
        } else {
            log.warn("No XpressNet connection, so panel won't function");
        }
    }

    boolean read = false;

    JComboBox<String> addrBox = new javax.swing.JComboBox<String>();

    JLabel status = new JLabel("    ");

    JToggleButton readSettingsButton = new JToggleButton(Bundle.getMessage("LIUSBReadButton"));
    JToggleButton writeSettingsButton = new JToggleButton(Bundle.getMessage("LIUSBWriteButton"));
    JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));
    JButton resetButton = new JButton(Bundle.getMessage("ButtonResetDefaults"));

    protected String[] validXNetAddresses = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8",
            "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23",
            "24", "25", "26", "27", "28", "29", "30", "31", ""};

    /**
     * Send new address to LIUSB.
     */
    void writeLIUSBSettings() {
        if (!(((String) addrBox.getSelectedItem()).equals(""))
                && (String) addrBox.getSelectedItem() != null) {
            /* we take care of generating an address request */
            XNetMessage msg = XNetMessage.getLIAddressRequestMsg(
                    addrBox.getSelectedIndex());
            //Then send to the controller
            tc.sendXNetMessage(msg, this);
        }
    }

    /**
     * Send Information request to LI-USB.
     */
    void readLIUSBSettings() {
        /* we request setting an out of range address 
         to get the current value. */
        XNetMessage msg = XNetMessage.getLIAddressRequestMsg(32);
        //Then send to the controller
        tc.sendXNetMessage(msg, this);
    }

    /**
     * Listen for responces from the LI-USB.
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
                status.setText(Bundle.getMessage("LIUSBReceivedStatus", l.getElement(2)));
            }
        }
    }

    /**
     * Listen for the messages to the LI-USB.
     */
    @Override
    public void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message {}", msg.toString());
    }

    /**
     * For now, reset just reset the screen to factory defaults.
     * Default values is 30 for the address
     */
    void resetLIUSBSettings() {
        addrBox.setSelectedIndex(30);
    }

    @Override
    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LIUSBConfigFrame.class);

}
