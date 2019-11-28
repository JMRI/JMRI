package jmri.jmrix.roco.z21.swing.configtool;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21Listener;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21Reply;
import jmri.jmrix.roco.z21.Z21TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying Version information and broadcast flags for Z21 hardware.
 * <p>
 * This is a utility for reading the hardware and software versions of your Z21
 * command station and along with the flags and the serial number.
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21ConfigFrame extends jmri.util.JmriJFrame implements Z21Listener {

    /**
     *
     */
    private Z21TrafficController tc = null;
    private RocoZ21CommandStation cs = null;

    /* updatable fields and field labels */
    private final JToggleButton getSystemInfoButton;
    private JToggleButton setSystemInfoButton;
    private final JToggleButton closeButton;
    private JLabel hardwareVersionLabel;
    private JTextField hardwareVersionTextField;
    private JLabel softwareVersionLabel;
    private JTextField softwareVersionTextField;
    private JLabel serialNumLabel;
    private JTextField serialNumTextField;

    // flag checkboxes
    private JCheckBox xPressNetMessagesCheckBox;
    private JCheckBox rmBusMessagesCheckBox;
    private JCheckBox systemStatusMessagesCheckBox;
    private JCheckBox xPressNetLocomotiveMessagesCheckBox;
    private JCheckBox railComMessagesCheckBox;
    private JCheckBox locoNetMessagesCheckBox;
    private JCheckBox locoNetLocomotiveMessagesCheckBox;
    private JCheckBox locoNetTurnoutMessagesCheckBox;
    private JCheckBox locoNetOccupancyMessagesCheckBox;
    private JCheckBox railComAutomaticCheckBox;
    private JCheckBox canDetectorCheckBox;

    public Z21ConfigFrame(jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        super(Bundle.getMessage("Z21ConfigToolMenuItem"));
        tc = memo.getTrafficController();
        cs = memo.getRocoZ21CommandStation();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)); // prevents strange stretching of content

        // build sub panel for version and serial number
        getContentPane().add(getSystemInfoPanel());

        // build sub panel for the flag list.
        getContentPane().add(getBroadcastFlagsPanel());

        // build sub panel with the read and close buttons
        JPanel buttonPanel = new JPanel();
        //buttonPanel.setLayout(new GridLayout(1, 2));

        getSystemInfoButton = new JToggleButton(Bundle.getMessage("GetSystemInfoButtonLabel"));
        getSystemInfoButton.setToolTipText(Bundle.getMessage("GetSystemInfoButtonToolTip"));
        closeButton = new JToggleButton(Bundle.getMessage("ButtonClose"));
        closeButton.setToolTipText(Bundle.getMessage("CloseButtonToolTip"));
        buttonPanel.add(getSystemInfoButton);
        buttonPanel.add(closeButton);
        getContentPane().add(buttonPanel);

        addHelpMenu("package.jmri.jmrix.roco.z21.swing.configtool.ConfigToolFrame", true);

        // and prep for display
        pack();

        // Add Get SystemInfo button handler
        getSystemInfoButton.addActionListener((ActionEvent a) -> {
            getSystemInfo();
        });

        // install close button handler
        closeButton.addActionListener((ActionEvent a) -> {
            setVisible(false);
            dispose();
        });

        if (tc != null) {
            tc.addz21Listener(this);
        } else {
            log.warn("No Z21 connection, panel won't function");
        }
    }

    private JPanel getSystemInfoPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SystemInformationTitle")));
        panel.setLayout(new GridLayout(0, 2));

        hardwareVersionLabel = new JLabel(Bundle.getMessage("HardwareVersionLabel"));
        hardwareVersionTextField = new JTextField("" + cs.getHardwareVersion());
        hardwareVersionTextField.setEnabled(false);
        panel.add(hardwareVersionLabel);
        panel.add(hardwareVersionTextField);

        softwareVersionLabel = new JLabel(Bundle.getMessage("SoftwareVersionLabel"));
        softwareVersionTextField = new JTextField("" + cs.getSoftwareVersion());
        softwareVersionTextField.setEnabled(false);
        panel.add(softwareVersionLabel);
        panel.add(softwareVersionTextField);

        serialNumLabel = new JLabel(Bundle.getMessage("SerialNumberLabel"));
        serialNumTextField = new JTextField("" + cs.getSerialNumber());
        serialNumTextField.setEnabled(false);

        panel.add(serialNumLabel);
        panel.add(serialNumTextField);

        return panel;
    }

    private JPanel getBroadcastFlagsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BroadcastFlagsTitle")));
        panel.setLayout(new GridLayout(0, 1));

        xPressNetMessagesCheckBox = new JCheckBox(Bundle.getMessage("XpressNetMessagesFlagLabel"), cs.getXPressNetMessagesFlag());
        xPressNetMessagesCheckBox.setToolTipText(Bundle.getMessage("XpressNetMessagesFlagToolTip"));
        panel.add(xPressNetMessagesCheckBox);

        rmBusMessagesCheckBox = new JCheckBox(Bundle.getMessage("RMBusMessagesFlagLabel"), cs.getRMBusMessagesFlag());
        rmBusMessagesCheckBox.setToolTipText(Bundle.getMessage("RMBusMessagesFlagToolTip"));
        panel.add(rmBusMessagesCheckBox);

        systemStatusMessagesCheckBox = new JCheckBox(Bundle.getMessage("SystemStatusMessagesFlagLabel"), cs.getSystemStatusMessagesFlag());
        systemStatusMessagesCheckBox.setToolTipText(Bundle.getMessage("RMBusMessagesFlagToolTip"));
        panel.add(systemStatusMessagesCheckBox);

        xPressNetLocomotiveMessagesCheckBox = new JCheckBox(Bundle.getMessage("XpressNetLocomotiveMessagesFlagLabel"), cs.getXPressNetLocomotiveMessagesFlag());
        xPressNetLocomotiveMessagesCheckBox.setToolTipText(Bundle.getMessage("XpressNetLocomotiveMessagesFlagToolTip"));
        panel.add(xPressNetLocomotiveMessagesCheckBox);

        railComMessagesCheckBox = new JCheckBox(Bundle.getMessage("RailComMessagesFlagLabel"), cs.getRailComMessagesFlag());
        railComMessagesCheckBox.setToolTipText(Bundle.getMessage("RailComMessagesFlagToolTip"));
        panel.add(railComMessagesCheckBox);

        railComAutomaticCheckBox = new JCheckBox(Bundle.getMessage("RailComAutomaticFlagLabel"), cs.getRailComAutomaticFlag());
        railComMessagesCheckBox.setToolTipText(Bundle.getMessage("RailComAutomaticFlagToolTip"));
        panel.add(railComAutomaticCheckBox);

        locoNetMessagesCheckBox = new JCheckBox(Bundle.getMessage("LocoNetMessagesFlagLabel"), cs.getLocoNetMessagesFlag());
        locoNetMessagesCheckBox.setToolTipText(Bundle.getMessage("LocoNetMessagesFlagToolTip"));
        panel.add(locoNetMessagesCheckBox);

        locoNetLocomotiveMessagesCheckBox = new JCheckBox(Bundle.getMessage("LocoNetLocomotiveMessagesFlagLabel"), cs.getLocoNetLocomotiveMessagesFlag());
        locoNetLocomotiveMessagesCheckBox.setToolTipText(Bundle.getMessage("LocoNetLocomotiveMessagesFlagToolTip"));
        panel.add(locoNetLocomotiveMessagesCheckBox);

        locoNetTurnoutMessagesCheckBox = new JCheckBox(Bundle.getMessage("LocoNetTurnoutMessagesFlagLabel"), cs.getLocoNetTurnoutMessagesFlag());
        locoNetTurnoutMessagesCheckBox.setToolTipText(Bundle.getMessage("LocoNetTurnoutMessagesFlagToolTip"));
        panel.add(locoNetTurnoutMessagesCheckBox);

        locoNetOccupancyMessagesCheckBox = new JCheckBox(Bundle.getMessage("LocoNetOccupancyMessagesFlagLabel"), cs.getLocoNetOccupancyMessagesFlag());
        locoNetOccupancyMessagesCheckBox.setToolTipText(Bundle.getMessage("LocoNetOccupancyMessagesFlagToolTip"));
        panel.add(locoNetOccupancyMessagesCheckBox);

        canDetectorCheckBox = new JCheckBox(Bundle.getMessage("canDetectorFlagLabel"), cs.getCanDetectorFlag());
        canDetectorCheckBox.setToolTipText(Bundle.getMessage("canDetectorFlagToolTip"));
        panel.add(canDetectorCheckBox);

        setSystemInfoButton = new JToggleButton(Bundle.getMessage("SetSystemInfoButtonLabel"));
        setSystemInfoButton.setToolTipText(Bundle.getMessage("SetSystemInfoButtonToolTip"));

        // Add Get SystemInfo button handler
        setSystemInfoButton.addActionListener((ActionEvent a) -> {
            writeSystemInfo();
        });
        panel.add(setSystemInfoButton);

        return panel;
    }

    /**
     * Request command station information.
     */
    private void getSystemInfo() {
        // request the version information
        tc.sendz21Message(Z21Message.getLanGetHardwareInfoRequestMessage(), this);
        // request the serial number
        tc.sendz21Message(Z21Message.getSerialNumberRequestMessage(), this);
        // request the flags.
        tc.sendz21Message(Z21Message.getLanGetBroadcastFlagsRequestMessage(), this);
    }

    /**
     * Request command station information.
     */
    private void writeSystemInfo() {
        // set the flags in the command station representation based on the
        // checkboxes.
        cs.setXPressNetMessagesFlag(xPressNetMessagesCheckBox.isSelected());
        cs.setRMBusMessagesFlag(rmBusMessagesCheckBox.isSelected());
        cs.setSystemStatusMessagesFlag(systemStatusMessagesCheckBox.isSelected());
        cs.setRailComMessagesFlag(railComMessagesCheckBox.isSelected());
        cs.setRailComAutomaticFlag(railComAutomaticCheckBox.isSelected());
        cs.setXPressNetLocomotiveMessagesFlag(xPressNetLocomotiveMessagesCheckBox.isSelected());
        cs.setLocoNetMessagesFlag(locoNetMessagesCheckBox.isSelected());
        cs.setLocoNetLocomotiveMessagesFlag(locoNetLocomotiveMessagesCheckBox.isSelected());
        cs.setLocoNetTurnoutMessagesFlag(locoNetTurnoutMessagesCheckBox.isSelected());
        cs.setLocoNetOccupancyMessagesFlag(locoNetOccupancyMessagesCheckBox.isSelected());
        cs.setCanDetectorFlag(canDetectorCheckBox.isSelected());

        // send the flags to the command station.
        tc.sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(cs.getZ21BroadcastFlags()), this);
    }

    /**
     * Listen for responses from the Z21.
     *
     * @param zr the reply
     */
    @Override
    public void reply(Z21Reply zr) {
        switch (zr.getOpCode()) {
            // handle replies with the serial number
            case 0x0010:
                // the serial number is a 32 bit integer stored in little
                // endian format starting with the 1st databyte (element 4).
                int serialNo = (zr.getElement(4)&0xff) + ((zr.getElement(5)&0xff) << 8)
                        + ((zr.getElement(6)&0xff) << 16) + ((zr.getElement(7)&0xff) << 24);
                cs.setSerialNumber(serialNo);
                updateSerialNumber();
                break;
            // handle replies with the hardware and software version.
            case 0x001A:
                // the hardware version is a 32 bit integer stored in little
                // endian format starting with the 1st databyte (element 4).
                int hwversion = zr.getElement(4) + (zr.getElement(5) << 8)
                        + (zr.getElement(6) << 16) + (zr.getElement(7) << 24);
                cs.setHardwareVersion(hwversion);
                // the software version is a 32 bit integer stored in little
                // endian format and written in BCD, starting after the hardware
                // version (element 8).  The least significant byte is to be
                // treated as a decimal.
                float swversion = (zr.getElementBCD(8) / 100.0f)
                        + (zr.getElementBCD(9))
                        + (zr.getElementBCD(10) * 100)
                        + (zr.getElementBCD(11)) * 10000;
                cs.setSoftwareVersion(swversion);
                updateVersionInformation();
                break;
            // handle replies with the flags.
            case 0x0051:
                // the payload is a 32 bit integer stored in little endian format
                // starting with the 1st databyte (element 4).
                int flags = zr.getElement(4) + (zr.getElement(5) << 8)
                        + (zr.getElement(6) << 16) + (zr.getElement(7) << 24);
                cs.setZ21BroadcastFlags(flags);
                updateFlagInformation();
                break;
            default:
                // ignore all other message types.
                log.debug("unhandled op-code received.");
        }
    }

    /**
     * Listen for the messages sent to the Z21.
     *
     * @param zm the message to listen for
     */
    @Override
    public void message(Z21Message zm) {
    }

    @Override
    public void dispose() {
        // remove the listener for this object.
        tc.removez21Listener(this);
        // take apart the JFrame
        super.dispose();
    }

    /**
     * Read the versions displayed from the command station representation.
     */
    private void updateVersionInformation() {
        hardwareVersionTextField.setText("0x" + java.lang.Integer.toHexString(cs.getHardwareVersion()));
        softwareVersionTextField.setText("" + cs.getSoftwareVersion());
    }

    /**
     * Read the serial number from the command station representation.
     */
    private void updateSerialNumber() {
        serialNumTextField.setText("" + cs.getSerialNumber());
    }

    /**
     * Read the flags displayed from the command station representation.
     */
    private void updateFlagInformation() {
        xPressNetMessagesCheckBox.setSelected(cs.getXPressNetMessagesFlag());
        rmBusMessagesCheckBox.setSelected(cs.getRMBusMessagesFlag());
        systemStatusMessagesCheckBox.setSelected(cs.getSystemStatusMessagesFlag());
        xPressNetLocomotiveMessagesCheckBox.setSelected(cs.getXPressNetLocomotiveMessagesFlag());
        railComMessagesCheckBox.setSelected(cs.getRailComMessagesFlag());
        locoNetMessagesCheckBox.setSelected(cs.getLocoNetMessagesFlag());
        locoNetLocomotiveMessagesCheckBox.setSelected(cs.getLocoNetLocomotiveMessagesFlag());
        locoNetTurnoutMessagesCheckBox.setSelected(cs.getLocoNetTurnoutMessagesFlag());
        locoNetOccupancyMessagesCheckBox.setSelected(cs.getLocoNetOccupancyMessagesFlag());
    }

    private final static Logger log = LoggerFactory.getLogger(Z21ConfigFrame.class);

}
