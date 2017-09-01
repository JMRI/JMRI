/**
 * Frame for configuring an XPA using a modem.
 *
 * @author	Paul Bender Copyright (C) 2004
 */
package jmri.jmrix.xpa.swing.xpaconfig;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrix.xpa.XpaMessage;
import jmri.jmrix.xpa.XpaSystemConnectionMemo;

public class XpaConfigureFrame extends jmri.util.JmriJFrame implements jmri.jmrix.xpa.XpaListener {

    // member declarations
    XpaSystemConnectionMemo memo = null;

    // Drop down box and button to set XpressNet address
    javax.swing.JComboBox<String> addrBox = new javax.swing.JComboBox<String>();
    javax.swing.JButton setAddr = new javax.swing.JButton();

    // Buttons to set the function of the phone's zero button (is it 
    // emergency stop or emenrgency off?
    javax.swing.JRadioButton zeroEmergencyOff = new javax.swing.JRadioButton();
    javax.swing.JRadioButton zeroEmergencyStop = new javax.swing.JRadioButton();
    javax.swing.JButton setZero = new javax.swing.JButton();

    // Drop down box and button to set duration of a momentary 
    // function
    javax.swing.JComboBox<String> functionBox = new javax.swing.JComboBox<String>();
    javax.swing.JButton setFunction = new javax.swing.JButton();

    protected String[] validTimes = new String[]{"0.2s", "0.4s", "0.6s", "0.8s", "1.0s", "1.2s", "1.4s", "1.6s", "1.8s", "2.0s"};
    protected int[] validTimeValues = new int[]{50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60};

    protected String[] validXNetAddresses = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};

    // Button to send a reset to the XPA
    javax.swing.JButton xpaReset = new javax.swing.JButton();

    public XpaConfigureFrame(XpaSystemConnectionMemo m) {
        super();
        setTitle(Bundle.getMessage("MenuItemXpaConfigTool"));
        memo = m;
    }

    @Override
    public void initComponents() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // First set up the pane for address selection.
        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel(Bundle.getMessage("XpAddressLabel")));
        pane0.add(addrBox);
        pane0.add(setAddr);
        pane0.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane0);

        // Next, set up the pane that determines what the zero 
        // button does
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(Bundle.getMessage("ZeroButtonLabel")));
        pane1.add(zeroEmergencyOff);
        pane1.add(zeroEmergencyStop);
        pane1.add(setZero);
        pane1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane1);

        // add the pane that sets the momentary function durration
        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(new JLabel(Bundle.getMessage("MomentaryDurLabel")));
        pane2.add(functionBox);
        pane2.add(setFunction);
        pane2.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        getContentPane().add(pane2);

        // add the reset button.
        getContentPane().add(xpaReset);

        // Initialzie the components
        setAddr.setText(Bundle.getMessage("ButtonSetAddress"));
        setAddr.setVisible(true);

        setAddr.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setAddrActionPerformed(e);
            }
        });

        // Initilize the Address Components
        addrBox.setVisible(true);
        addrBox.setToolTipText(Bundle.getMessage("SetAddressToolTip"));
        for (int i = 0; i < validXNetAddresses.length; i++) {
            addrBox.addItem(validXNetAddresses[i]);
        }
        addrBox.setSelectedIndex(0);

        // Initilize the function buttons for the zero key 
        // settings.
        zeroEmergencyOff.setText(Bundle.getMessage("XNetCSStatusEmergencyOff"));
        zeroEmergencyOff.setSelected(true);

        zeroEmergencyOff.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                zeroEmergencyOffActionPerformed(e);
            }
        });

        zeroEmergencyStop.setText(Bundle.getMessage("XNetCSStatusEmergencyStop"));
        zeroEmergencyStop.setSelected(false);

        zeroEmergencyStop.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                zeroEmergencyStopActionPerformed(e);
            }
        });

        setZero.setText(Bundle.getMessage("ButtonSetZero"));

        setZero.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setZeroActionPerformed(e);
            }
        });

        // Initilize the Function durration components
        setFunction.setText(Bundle.getMessage("ButtonSetDuration"));
        setFunction.setVisible(true);

        setFunction.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setFunctionActionPerformed(e);
            }
        });

        functionBox.setVisible(true);
        functionBox.setToolTipText(Bundle.getMessage("SetDurationToolTip"));
        for (int i = 0; i < validTimes.length; i++) {
            functionBox.addItem(validTimes[i]);
        }
        functionBox.setSelectedIndex(0);

        // Initilize the reset button
        xpaReset.setText(Bundle.getMessage("ButtonReset"));
        xpaReset.setVisible(true);

        xpaReset.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                xpaResetActionPerformed(e);
            }
        });

        // pack for display
        pack();
    }

    public void setAddrActionPerformed(java.awt.event.ActionEvent e) {
        // The first address available for the XPA is 1, so we 
        // have to add 1 to the selected index to get the correct value.
        XpaMessage m = XpaMessage.getDeviceSettingMsg(addrBox.getSelectedIndex() + 1);
        memo.getXpaTrafficController().sendXpaMessage(m, this);
    }

    public void setFunctionActionPerformed(java.awt.event.ActionEvent e) {
        XpaMessage m = XpaMessage.getDeviceSettingMsg(validTimeValues[functionBox.getSelectedIndex()]);
        memo.getXpaTrafficController().sendXpaMessage(m, this);
    }

    public void xpaResetActionPerformed(java.awt.event.ActionEvent e) {
        XpaMessage m = XpaMessage.getDeviceSettingMsg(99);
        memo.getXpaTrafficController().sendXpaMessage(m, this);
    }

    public void zeroEmergencyOffActionPerformed(java.awt.event.ActionEvent e) {
        // toggle from Emergency Stop to Emergency Off
        zeroEmergencyOff.setSelected(true);
        zeroEmergencyStop.setSelected(false);
    }

    public void zeroEmergencyStopActionPerformed(java.awt.event.ActionEvent e) {
        // toggle from Emergency Off to Emergency Stop
        zeroEmergencyStop.setSelected(true);
        zeroEmergencyOff.setSelected(false);
    }

    public void setZeroActionPerformed(java.awt.event.ActionEvent e) {
        XpaMessage m;
        if (zeroEmergencyOff.isSelected()) {
            m = XpaMessage.getDeviceSettingMsg(40);
        } else {
            m = XpaMessage.getDeviceSettingMsg(41);
        }
        memo.getXpaTrafficController().sendXpaMessage(m, this);
    }

    @Override
    public void message(XpaMessage m) {
    }  // ignore replies

    @Override
    public void reply(XpaMessage r) {
    } // ignore replies

}
