package jmri.jmrix.can.cbus.swing.console;

import javax.swing.*;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.ValidationNotifications;

/**
 * Frame for CBUS Console
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsolePacketPane extends JPanel {
    
    private JTextField[] lastRxDataFields;
    
    private JPanel rxPacketPane;
    private JPanel sendPacketPane;
    
    private JTextField lastDynPriField;
    private JTextField lastMinPriField;
    
    private JTextField dynPriField;
    private JTextField minPriField;
    private JTextField[] dataFields;
    private JButton sendPacketButton;
    private JButton dataClearButton;
    
    private JButton copyButton;
    private JCheckBox decimalCheckBox;
    private JCheckBox rcvdDecimalCheckBox;
    private final CbusConsolePane _mainPane;
    
    public CbusConsolePacketPane(CbusConsolePane mainPane){
        super();
        _mainPane = mainPane;
        
        initRxPacketPane();
        initSendPacketPane();
        initPanel();
    }
    
    private void initPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(rxPacketPane);
        add(sendPacketPane);
    
    }

    private void initRxPacketPane() {
    
        rcvdDecimalCheckBox = new JCheckBox();
        rcvdDecimalCheckBox.setText(Bundle.getMessage("ButtonDecimal"));
        rcvdDecimalCheckBox.setVisible(true);
        rcvdDecimalCheckBox.setToolTipText(Bundle.getMessage("TooltipDecimal"));
        rcvdDecimalCheckBox.setSelected(true);
        
        // Pane for most recently recived packet
        rxPacketPane = new JPanel();
        rxPacketPane.setLayout(new BoxLayout(rxPacketPane, BoxLayout.X_AXIS));
        rxPacketPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("MostRecentPacketTitle")));
    
        copyButton = new JButton();
        copyButton.setText(Bundle.getMessage("ButtonCopy"));
        copyButton.setVisible(true);
        copyButton.setToolTipText(Bundle.getMessage("TooltipCopyEvent"));
        
        copyButton.addActionListener(this::copyButtonActionPerformed);
        
        initRxPriorityFields();
        
        initRxTextFields();
    
    }
    
    private void initSendPacketPane() {
    
        decimalCheckBox = new JCheckBox();
        decimalCheckBox.setText(Bundle.getMessage("ButtonDecimal"));
        decimalCheckBox.setVisible(true);
        decimalCheckBox.setToolTipText(Bundle.getMessage("TooltipDecimal"));
        decimalCheckBox.setSelected(true);
        
     // Pane for constructing packet to send
        sendPacketPane = new JPanel();
        sendPacketPane.setLayout(new BoxLayout(sendPacketPane, BoxLayout.X_AXIS));
        sendPacketPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("SendPacketTitle")));

        // Construct data fields for Priority and up to 8 bytes
        dynPriField = new JTextField("2", 4);
        dynPriField.setToolTipText(Bundle.getMessage("TooltipDynPri"));
        dynPriField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("DynPriTitle")));
        sendPacketPane.add(dynPriField);
        minPriField = new JTextField("3", 4);
        minPriField.setToolTipText(Bundle.getMessage("TooltipDinPri03"));
        minPriField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("MinPriTitle")));
        sendPacketPane.add(minPriField);
        
        initSendFields();
    
    }
    
    private void initSendFields() {
        
        sendPacketButton = new JButton();
        dataClearButton = new JButton();
        
        setupTextFields(dataFields = new JTextField[8],sendPacketPane);
        
        sendPacketPane.add(sendPacketButton);
        sendPacketPane.add(dataClearButton);
        sendPacketPane.add(decimalCheckBox);
        
        initButtonBorderToolTips();
        
        sendPacketButton.addActionListener(this::sendPacketButtonActionPerformed);
        dataClearButton.addActionListener(this::dataClearButtonActionPerformed);
        
    }
    
    
    private void initRxPriorityFields() {
    
    // Construct data fields for Priority and up to 8 bytes
        lastDynPriField = new JTextField("", 4);
        lastDynPriField.setToolTipText(Bundle.getMessage("TooltipDynPri"));
        lastDynPriField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("DynPriTitle")));
        rxPacketPane.add(lastDynPriField);
        lastMinPriField = new JTextField("", 4);
        lastMinPriField.setToolTipText(Bundle.getMessage("TooltipMinPri"));
        lastMinPriField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("MinPriTitle")));
        rxPacketPane.add(lastMinPriField);
        
    }
    
    
    private void initRxTextFields() {
        
        setupTextFields(lastRxDataFields = new JTextField[8],rxPacketPane);
        rxPacketPane.add(copyButton);
        rxPacketPane.add(rcvdDecimalCheckBox);
    
    }
    
    private void setupTextFields(JTextField[] data, JPanel panelToAdd) {
        for (int i = 0; i < 8; i++) {
            data[i] = new JTextField("", 4);
            if (i == 0) {
                data[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d0 (OPC)"));
                data[i].setToolTipText(Bundle.getMessage("TooltipOpc"));
            } else {
                data[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d" + i));
                data[i].setToolTipText(Bundle.getMessage("TooltipDbX", i));
            }
            panelToAdd.add(data[i]);
        }
    }
    
    
    private void copyButtonActionPerformed(java.awt.event.ActionEvent e) {
        dynPriField.setText(lastDynPriField.getText());
        minPriField.setText(lastMinPriField.getText());
        for (int j = 0; j < 8; j++) {
            dataFields[j].setText(lastRxDataFields[j].getText());
        }
    }
    
    private void initButtonBorderToolTips(){
        
        sendPacketButton.setText(Bundle.getMessage("ButtonSend"));
        sendPacketButton.setVisible(true);
        sendPacketButton.setToolTipText(Bundle.getMessage("TooltipSendPacket"));

        dataClearButton.setText(Bundle.getMessage("ButtonClear"));
        dataClearButton.setVisible(true);
        dataClearButton.setToolTipText(Bundle.getMessage("TooltipClearFields"));
    
    }
    
    protected void setLastReceived(CanReply r) {
    
        if (rcvdDecimalCheckBox.isSelected()) {
            lastDynPriField.setText(Integer.toString(CbusMessage.getPri(r) / 4));
            lastMinPriField.setText(Integer.toString(CbusMessage.getPri(r) & 3));
        } else {
            lastDynPriField.setText(Integer.toHexString(CbusMessage.getPri(r) / 4));
            lastMinPriField.setText(Integer.toHexString(CbusMessage.getPri(r) & 3));
        }
        // Pay attention to data length in op-code
        for (int j = 0; j < (r.getElement(0) >> 5) + 1; j++) {
            if (rcvdDecimalCheckBox.isSelected()) {
                lastRxDataFields[j].setText(Integer.toString(r.getElement(j)));
            } else {
                lastRxDataFields[j].setText(Integer.toHexString(r.getElement(j)));
            }
        }
    
    }
    
    private void sendPacketButtonActionPerformed(java.awt.event.ActionEvent e) {
        CanMessage m = new CanMessage(_mainPane.tc.getCanid());
        int data = ValidationNotifications.parseBinDecHexByte(dynPriField.getText(), 2, decimalCheckBox.isSelected(), Bundle.getMessage("DynPriErrorDialog"),_mainPane);
        if (data == -1) {
            return;
        }
        int data2 = ValidationNotifications.parseBinDecHexByte(minPriField.getText(), 3, decimalCheckBox.isSelected(), Bundle.getMessage("MinPriErrorDialog"),_mainPane);
        if (data2 == -1) {
            return;
        }
        m.setHeader(data * 4 + data2);
        
        int j;
        if ((j = setPacketData(m))<1){
            return;
        }
        
        // Does the number of data match the opcode?
        // Subtract one as loop variable will have incremented
        if ((j - 1) != (opcToSend >> 5)) {
            JmriJOptionPane.showMessageDialog(_mainPane, Bundle.getMessage("OpcCountErrorDialog", (opcToSend >> 5)),
                    Bundle.getMessage("CbusConsoleTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        m.setNumDataElements(j);
        
        _mainPane.tc.sendCanMessage(m, null);
    }
    
    private int opcToSend;
    
    private int setPacketData(CanMessage m) {
    
        int j;
        for (j = 0; j < 8; j++) {
            if (!dataFields[j].getText().isEmpty()) {
                int data = ValidationNotifications.parseBinDecHexByte(dataFields[j].getText(), 255, decimalCheckBox.isSelected(),
                        Bundle.getMessage("DbxErrorDialog", j),_mainPane);
                if (data == -1) {
                    return -1;
                }
                m.setElement(j, data);
                if (j == 0) {
                    opcToSend = data; // save OPC(d0) for later
                }
            } else {
                if (j == 0) {
                    JmriJOptionPane.showMessageDialog(_mainPane, Bundle.getMessage("OpcErrorDialog"),
                        Bundle.getMessage("CbusConsoleTitle"), JmriJOptionPane.ERROR_MESSAGE);
                }
                return j;
            }
        }
        return j;
    }

    
    private void dataClearButtonActionPerformed(java.awt.event.ActionEvent e) {
        dynPriField.setText("2");
        minPriField.setText("3");
        for (int i = 0; i < 8; i++) {
            dataFields[i].setText("");
        }
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusConsolePacketPane.class);
}
