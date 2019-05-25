package jmri.jmrix.lenz.swing.systeminfo;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
 * Frame displaying Version information for XpressNet hardware.
 * <p>
 * This is a utility for reading the software version and type of the command
 * station, and, the Hardware and software versions of your XpressNet Computer
 * Interface.
 * <p>
 * Some of this code may be moved to facilitate automatic enabling of features
 * that are not available on all XpressNet Command Stations (as an example, the
 * fact that you can't program using the computer on a Commander or Compact)
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Giorgio Terdina Copyright (C) 2007
 */
public class SystemInfoFrame extends jmri.util.JmriJFrame implements XNetListener {

    protected XNetTrafficController tc = null;

    public SystemInfoFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(Bundle.getMessage("MenuItemXNetSystemInformation"));
        tc = memo.getXNetTrafficController();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        JPanel infoPane = new JPanel();
        infoPane.setBorder(BorderFactory.createEtchedBorder());
        infoPane.setLayout(new GridLayout(6, 2));

        infoPane.add(new JLabel(Bundle.getMessage("CommandStationLabel")));
        infoPane.add(CSType);

        infoPane.add(new JLabel(Bundle.getMessage("SoftwareVersionLabel")));
        infoPane.add(CSSoftwareVersion);

        infoPane.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("StatusCol"))));
        infoPane.add(CSStatus);

        infoPane.add(new JLabel(Bundle.getMessage("InterfaceLabel")));
        infoPane.add(LIType);

        infoPane.add(new JLabel(Bundle.getMessage("HardwareVersionLabel")));
        infoPane.add(LIHardwareVersion);

        infoPane.add(new JLabel(Bundle.getMessage("SoftwareVersionLabel")));
        infoPane.add(LISoftwareVersion);

        getContentPane().add(infoPane);
        getContentPane().add(Box.createVerticalGlue());

        JPanel buttonPane = new JPanel();
        buttonPane.add(getSystemInfoButton);
        buttonPane.add(closeButton);
        getContentPane().add(buttonPane);

        addHelpMenu("package.jmri.jmrix.lenz.systeminfo.SystemInfoFrame", true);

        // and prep for display
        pack();

        // initialize the display values with what the LenzCommandStation
        // class already knows.
        setCSVersionDisplay();

        // Add Get SystemInfo button handler
        getSystemInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                getSystemInfo();
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

        if (tc != null) {
            tc.addXNetListener(~0, this);
        } else {
            log.warn("No XpressNet connection, panel won't function");
        }

    }

    boolean read = false;

    JLabel CSType = new JLabel("                ");
    JLabel CSSoftwareVersion = new JLabel("");
    JLabel CSStatus = new JLabel(Bundle.getMessage("BeanStateUnknown"));
    JLabel LIType = new JLabel("       ");
    JLabel LIHardwareVersion = new JLabel("");
    JLabel LISoftwareVersion = new JLabel("");

    JToggleButton getSystemInfoButton = new JToggleButton(Bundle.getMessage("GetSystemInfoButtonLabel"));
    JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));

    /**
     * Send Information request to LI100/LI101.
     */
    void getSystemInfo() {
        /* First, we need to send a request for the Command Station
         hardware and software version */
        XNetMessage msg = XNetMessage.getCSVersionRequestMessage();
        //Then send to the controller
        tc.sendXNetMessage(msg, this);
        /* Next, get the message to request the Computer Interface 
         Hardware/Software Version */
        XNetMessage msg2 = XNetMessage.getLIVersionRequestMessage();
        // Then send it to the controller
        tc.sendXNetMessage(msg2, this);
        /* Finally, we send a request for the Command Station Status*/
        XNetMessage msg3 = XNetMessage.getCSStatusRequestMessage();
        //Then send to the controller
        tc.sendXNetMessage(msg3, this);
    }

    /**
     * Listen for responses from the LI101.
     */
    @Override
    public void message(XNetReply l) {

        // Check to see if this is a response for the LI version info
        // or the Command Station Version Info
        if (l.getElement(0) == XNetConstants.LI_VERSION_RESPONSE) {
            // This is an Interface response
            LIHardwareVersion.setText("" + (l.getElementBCD(1).floatValue() / 10));
            LISoftwareVersion.setText("" + (l.getElementBCD(2)));
        } else if (l.getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE) {
            // This is the Command Station Software Version Response
            if (l.getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
                tc.getCommandStation().setCommandStationSoftwareVersion(l);
                tc.getCommandStation().setCommandStationType(l);
                setCSVersionDisplay();
            }
        } else if (l.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE) {
            if (l.getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
                int statusByte = l.getElement(2);
                if ((statusByte & 0x01) == 0x01) {
                    // Command station is in Emergency Off Mode
                    CSStatus.setText(Bundle.getMessage("XNetCSStatusEmergencyOff"));
                } else if ((statusByte & 0x02) == 0x02) {
                    // Command station is in Emergency Stop Mode
                    CSStatus.setText(Bundle.getMessage("XNetCSStatusEmergencyStop"));
                } else if ((statusByte & 0x08) == 0x08) {
                    // Command station is in Service Mode
                    CSStatus.setText(Bundle.getMessage("XNetCSStatusServiceMode"));
                } else if ((statusByte & 0x40) == 0x40) {
                    // Command station is in Power Up Mode
                    if ((statusByte & 0x04) == 0x04) {
                        CSStatus.setText(Bundle.getMessage("XNetCSStatusPoweringUp") + ": "
                                + Bundle.getMessage("XNetCSStatusPowerModeAuto"));
                    } else {
                        CSStatus.setText(Bundle.getMessage("XNetCSStatusPoweringUp") + ": "
                                + Bundle.getMessage("XNetCSStatusPowerModeManual"));
                    }
                } else if ((statusByte & 0x80) == 0x80) {
                    // Command station has a experienced a ram check error
                    CSStatus.setText(Bundle.getMessage("XNetCSStatusRamCheck"));
                } else {
                    CSStatus.setText(Bundle.getMessage("XNetCSStatusRamNormal"));
                }
            }
        }
    }

    /**
     * Listen for the messages to the LI100/LI101.
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
     * Display the currently known version information from the
     * LenzCommandStation class.
     */
    private void setCSVersionDisplay() {
        CSSoftwareVersion.setText("" + tc.getCommandStation()
                .getCommandStationSoftwareVersion());
        int cs_type = tc.getCommandStation().getCommandStationType();
        if (cs_type == jmri.jmrix.lenz.XNetConstants.CS_TYPE_LZ100) {
            CSType.setText(Bundle.getMessage("CSTypeLZ100"));
        } else if (cs_type == jmri.jmrix.lenz.XNetConstants.CS_TYPE_LH200) {
            CSType.setText(Bundle.getMessage("CSTypeLH200"));
        } else if (cs_type == jmri.jmrix.lenz.XNetConstants.CS_TYPE_COMPACT) {
            CSType.setText(Bundle.getMessage("CSTypeCompact"));
        } else if (cs_type == jmri.jmrix.lenz.XNetConstants.CS_TYPE_MULTIMAUS) {
            CSType.setText(Bundle.getMessage("CSTypeMultiMaus"));
        } else if (cs_type == jmri.jmrix.lenz.XNetConstants.CS_TYPE_Z21) {
            CSType.setText(Bundle.getMessage("CSTypeZ21"));
        } else if (cs_type == jmri.jmrix.lenz.XNetConstants.CS_TYPE_LOKMAUSII) {
            CSType.setText(Bundle.getMessage("CSTypeLokMaus"));
        } else {
            CSType.setText(Bundle.getMessage("StateUnknown")); // use shared key
        }
    }

    @Override
    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SystemInfoFrame.class);

}
