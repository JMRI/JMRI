package jmri.jmrix.roco.z21.swing.configtool;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jmri.jmrix.roco.z21.Z21Listener;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21Reply;
import jmri.jmrix.roco.z21.Z21TrafficController;
import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying Version information and broadcast flags for Z21 hardware.
 * <P>
 * This is a utility for reading the hardware and software versions of your 
 * Z21 command station and along with the flags and the serial number.
 * <P>
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21ConfigFrame extends jmri.util.JmriJFrame implements Z21Listener {

    /**
     *
     */
    private Z21TrafficController tc = null;
    private RocoZ21CommandStation cs = null;

    /* updatable fields and field labels */
    private JToggleButton getSystemInfoButton;
    private JToggleButton setSystemInfoButton;
    private JToggleButton closeButton;
    private JLabel hardwareVersionLabel;
    private JTextField hardwareVersionTextField;
    private JLabel softwareVersionLabel;
    private JTextField softwareVersionTextField ;
    private JLabel serialNumLabel;
    private JTextField serialNumTextField;

    // flag checkboxes
    private JCheckBox XPressNetMessagesCheckBox;
    private JCheckBox RMBusMessagesCheckBox;
    private JCheckBox SystemStatusMessagesCheckBox;
    private JCheckBox XPressNetLocomotiveMessagesCheckBox;
    private JCheckBox RailComMessagesCheckBox;
    private JCheckBox LocoNetMessagesCheckBox;
    private JCheckBox LocoNetLocomotiveMessagesCheckBox;
    private JCheckBox LocoNetTurnoutMessagesCheckBox;
    private JCheckBox LocoNetOccupancyMessagesCheckBox;


    public Z21ConfigFrame(jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        super(Bundle.getMessage("Z21ConfigToolMenuItem"));
        tc = memo.getTrafficController();
        cs = memo.getRocoZ21CommandStation();
        getContentPane().setLayout(new GridLayout(0, 1));

        // build sub panel for version and serial number
        getContentPane().add(getSystemInfoPanel());

        // build sub panel for the flag list.
        getContentPane().add(getBroadcastFlagsPanel());



        // build sub panel with the read and close buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 2));
        
        getSystemInfoButton = new JToggleButton(Bundle.getMessage("GetSystemInfoButtonLabel"));
        getSystemInfoButton.setToolTipText(Bundle.getMessage("GetSystemInfoButtonToolTip"));
        closeButton = new JToggleButton("CloseButtonLabel");
        closeButton.setToolTipText(Bundle.getMessage("CloseButtonToolTip"));
        buttonPanel.add(getSystemInfoButton);
        buttonPanel.add(closeButton);
        getContentPane().add(buttonPanel);

        addHelpMenu("package.jmri.jmrix.roco.z21.swing.configtool.ConfigToolFrame", true);

        // and prep for display
        pack();

        // Add Get SystemInfo button handler
        getSystemInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                getSystemInfo();
            }
        }
        );

        // install close button handler
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                setVisible(false);
                dispose();
            }
        }
        );

        if (tc != null) {
            tc.addz21Listener(this);
        } else {
            log.warn("No Z21 connection, panel won't function");
        }

    }

    private JPanel getSystemInfoPanel(){
         JPanel panel = new JPanel();
         panel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SystemInformationLabel")));
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
    
    private JPanel getBroadcastFlagsPanel(){
         JPanel panel = new JPanel();
         panel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BroadcastFlagsLabel")));
         panel.setLayout(new GridLayout(0, 1));
    
         XPressNetMessagesCheckBox = new JCheckBox(Bundle.getMessage("XPressNetMessagesFlagLabel"),cs.getXPressNetMessagesFlag());
         XPressNetMessagesCheckBox.setToolTipText(Bundle.getMessage("XPressNetMessagesFlagToolTip"));
         panel.add(XPressNetMessagesCheckBox);

         RMBusMessagesCheckBox = new JCheckBox(Bundle.getMessage("RMBusMessagesFlagLabel"),cs.getRMBusMessagesFlag());
         RMBusMessagesCheckBox.setToolTipText(Bundle.getMessage("RMBusMessagesFlagToolTip"));
         panel.add(RMBusMessagesCheckBox);

         SystemStatusMessagesCheckBox = new JCheckBox(Bundle.getMessage("SystemStatusMessagesFlagLabel"),cs.getSystemStatusMessagesFlag());
         SystemStatusMessagesCheckBox.setToolTipText(Bundle.getMessage("RMBusMessagesFlagToolTip"));
         panel.add(SystemStatusMessagesCheckBox);

         XPressNetLocomotiveMessagesCheckBox = new JCheckBox(Bundle.getMessage("XPressNetLocomotiveMessagesFlagLabel"),cs.getXPressNetLocomotiveMessagesFlag());
         XPressNetLocomotiveMessagesCheckBox.setToolTipText(Bundle.getMessage("XPressNetLocomotiveMessagesFlagToolTip"));
         panel.add(XPressNetLocomotiveMessagesCheckBox);

         RailComMessagesCheckBox = new JCheckBox(Bundle.getMessage("RailComMessagesFlagLabel"),cs.getRailComMessagesFlag());
         RailComMessagesCheckBox.setToolTipText(Bundle.getMessage("RailComMessagesFlagToolTip"));
         panel.add(RailComMessagesCheckBox);

         LocoNetMessagesCheckBox = new JCheckBox(Bundle.getMessage("LocoNetMessagesFlagLabel"),cs.getLocoNetMessagesFlag());
         LocoNetMessagesCheckBox.setToolTipText(Bundle.getMessage("LocoNetMessagesFlagToolTip"));
         panel.add(LocoNetMessagesCheckBox);

         LocoNetLocomotiveMessagesCheckBox = new JCheckBox(Bundle.getMessage("LocoNetLocomotiveMessagesFlagLabel"),cs.getLocoNetLocomotiveMessagesFlag());
         LocoNetLocomotiveMessagesCheckBox.setToolTipText(Bundle.getMessage("LocoNetLocomotiveMessagesFlagToolTip"));
         panel.add(LocoNetLocomotiveMessagesCheckBox);

         LocoNetTurnoutMessagesCheckBox = new JCheckBox(Bundle.getMessage("LocoNetTurnoutMessagesFlagLabel"),cs.getLocoNetTurnoutMessagesFlag());
         LocoNetTurnoutMessagesCheckBox.setToolTipText(Bundle.getMessage("LocoNetTurnoutMessagesFlagToolTip"));
         panel.add(LocoNetTurnoutMessagesCheckBox);

         LocoNetOccupancyMessagesCheckBox = new JCheckBox(Bundle.getMessage("LocoNetOccupancyMessagesFlagLabel"),cs.getLocoNetOccupancyMessagesFlag());
         LocoNetOccupancyMessagesCheckBox.setToolTipText(Bundle.getMessage("LocoNetOccupancyMessagesFlagToolTip"));
         panel.add(LocoNetOccupancyMessagesCheckBox);

         setSystemInfoButton = new JToggleButton("SetSystemInfoButtonLabel");
         setSystemInfoButton.setToolTipText(Bundle.getMessage("SetSystemInfoButtonToolTip"));
        
         // Add Get SystemInfo button handler
         setSystemInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                writeSystemInfo();
            }
        }
        );
         panel.add(setSystemInfoButton);

         return panel;
    }
    
    /**
     * request command station information
     */
    private void getSystemInfo() {
        // request the version information
        tc.sendz21Message(Z21Message.getLanGetHardwareInfoRequestMessage(),this);
        // request the serial number
        tc.sendz21Message(Z21Message.getSerialNumberRequestMessage(),this);
        // request the flags.
        tc.sendz21Message(Z21Message.getLanGetBroadcastFlagsRequestMessage(),this);
    }

    /**
     * request command station information
     */
    private void writeSystemInfo() {
        // set the flags in the command station representation based on the 
        // checkboxes.
        cs.setXPressNetMessagesFlag(XPressNetMessagesCheckBox.isSelected());
        cs.setRMBusMessagesFlag(RMBusMessagesCheckBox.isSelected());
        cs.setSystemStatusMessagesFlag(SystemStatusMessagesCheckBox.isSelected());
        cs.setRailComMessagesFlag(RailComMessagesCheckBox.isSelected());
        cs.setXPressNetLocomotiveMessagesFlag(XPressNetLocomotiveMessagesCheckBox.isSelected());
        cs.setLocoNetMessagesFlag(LocoNetMessagesCheckBox.isSelected());
        cs.setLocoNetLocomotiveMessagesFlag(LocoNetLocomotiveMessagesCheckBox.isSelected());
        cs.setLocoNetTurnoutMessagesFlag(LocoNetTurnoutMessagesCheckBox.isSelected());
        cs.setLocoNetOccupancyMessagesFlag(LocoNetOccupancyMessagesCheckBox.isSelected());

        // send the flags to the command station.
        tc.sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(cs.getZ21BroadcastFlags()),this);
    }

    // listen for responses from the Z21
    public void reply(Z21Reply zr) {
       switch(zr.getOpCode()){
          // handle replies with the serial number
          case 0x0010:
             // the serial number is a 32 bit integer stored in little 
             // endian format starting with the 1st databyte (element 4).
             int serialNo = zr.getElement(4) + (zr.getElement(5) << 8) +
	                 (zr.getElement(6) << 16) + (zr.getElement(7) << 24);
             cs.setSerialNumber(serialNo);
             updateSerialNumber();
             break;
          // handle repleis with the hardware and software version.
          case 0x001A:
             // the hardware version is a 32 bit integer stored in little 
             // endian format starting with the 1st databyte (element 4).
             int hwversion = zr.getElement(4) + (zr.getElement(5) << 8) +
                         (zr.getElement(6) << 16) + (zr.getElement(7) << 24);
             cs.setHardwareVersion(hwversion);
             // the software version is a 32 bit integer stored in little 
             // endian format and written in BCD, starting after the hardware
             // version (element 8).  The least significant byte is to be 
             // treated as a decimal. 
             float swversion = zr.getElementBCD(8)/100+
                               zr.getElementBCD(9)+
                               (zr.getElementBCD(10)*100)+
                               (zr.getElementBCD(11))*10000;
             cs.setSoftwareVersion(swversion);
             updateVersionInformation();
             break;
          // handle replies with the flags.
          case 0x0051:
             // the payload is a 32 bit integer stored in little endian format
             // starting with the 1st databyte (element 4).
             int flags = zr.getElement(4) + (zr.getElement(5) << 8) +
                         (zr.getElement(6) << 16) + (zr.getElement(7) << 24);
             cs.setZ21BroadcastFlags(flags);
             updateFlagInformation();
             break;
          default:
             // ignore all other message types.
             log.debug("unhandled op-code recieved.");
       }
    }

    // listen for the messages sent to the Z21
    public void message(Z21Message zm) {
    }

    public void dispose() {
        // remove the listener for this object.
        tc.removez21Listener(this);
        // take apart the JFrame
        super.dispose();
    }

    // read the versions displayed from the command station representation
    private void updateVersionInformation(){
         hardwareVersionTextField.setText("" + cs.getHardwareVersion());
         softwareVersionTextField.setText("" + cs.getHardwareVersion());
    }

    // read the serial number from the command station representation
    private void updateSerialNumber(){
         serialNumTextField.setText("" + cs.getHardwareVersion());
    }

    // read the flags displayed from the command station representation
    private void updateFlagInformation(){
         XPressNetMessagesCheckBox.setSelected(cs.getXPressNetMessagesFlag());
         RMBusMessagesCheckBox.setSelected(cs.getRMBusMessagesFlag());
         SystemStatusMessagesCheckBox.setSelected(cs.getSystemStatusMessagesFlag());
         XPressNetLocomotiveMessagesCheckBox.setSelected(cs.getXPressNetLocomotiveMessagesFlag());
         RailComMessagesCheckBox.setSelected(cs.getRailComMessagesFlag());
         LocoNetMessagesCheckBox.setSelected(cs.getLocoNetMessagesFlag());
         LocoNetLocomotiveMessagesCheckBox.setSelected(cs.getLocoNetLocomotiveMessagesFlag());
         LocoNetTurnoutMessagesCheckBox.setSelected(cs.getLocoNetTurnoutMessagesFlag());
         LocoNetOccupancyMessagesCheckBox.setSelected(cs.getLocoNetOccupancyMessagesFlag());
    }

    private final static Logger log = LoggerFactory.getLogger(Z21ConfigFrame.class.getName());

}
