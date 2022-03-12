package jmri.jmrix.can.cbus.swing;

import java.awt.event.ActionEvent;
import javax.swing.*;
import jmri.jmrix.can.swing.CanPanel;
import jmri.jmrix.can.cbus.CbusEvent;
import jmri.util.swing.ValidationNotifications;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Panel for CBUS Console to Send CBUS Events
 * 
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusSendEventPane extends JPanel {
    
    private final CanPanel _mainPane;
    
    private JRadioButton onButton;
    private JRadioButton offButton;
    private JRadioButton requestButton;
    private ButtonGroup onOffGroup;
    private ButtonGroup dataGroup;
    private JTextField nnField;
    private JTextField evField;
    private JButton sendEvButton;
    private JCheckBox showEventData;
    private JCheckBox decimalCheckBoxC;
    private JPanel eventDataPanel;
    private JTextField[] data;
    private JRadioButton select0Data;
    private JRadioButton select1Data;
    private JRadioButton select2Data;
    private JRadioButton select3Data;
    private int _selectedData = 0;
    
    public CbusSendEventPane(CanPanel mainPane){
        super();
        _mainPane = mainPane;
        init();
    }
    
    private void init() {
    
        onButton = new JRadioButton();
        offButton = new JRadioButton();
        requestButton = new JRadioButton();
        showEventData = new JCheckBox();
        onOffGroup = new ButtonGroup();
        dataGroup = new ButtonGroup();
        sendEvButton = new JButton();
        decimalCheckBoxC = new JCheckBox();
    
        initButtons();
        addToPanel();
        
    }

    private void addToPanel() {
        
        JPanel mainEventPanel = new JPanel();
        
        // mainEventPanel.setLayout(new BoxLayout(mainEventPanel, BoxLayout.X_AXIS));
    
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("ButtonSendEvent")));

        nnField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("CbusNode")));
        mainEventPanel.add(nnField);

        
        evField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("CbusEvent")));
        mainEventPanel.add(evField);
        
        mainEventPanel.add(decimalCheckBoxC);

        onOffGroup.add(onButton);
        onOffGroup.add(offButton);
        onOffGroup.add(requestButton);
        
        JPanel group = new JPanel();
        group.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
        
        group.add(onButton);
        group.add(offButton);
        group.add(requestButton);
        
        mainEventPanel.add(group);
        
        mainEventPanel.add(sendEvButton);
        
        mainEventPanel.add(showEventData);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(mainEventPanel);
        eventDataPanel = getNewDataPanel();
        add(eventDataPanel);
        
        dataSelectTypePerformed(null); // reset data panel
        
    }
    
    private void initButtons() {
    
        onButton.setText(Bundle.getMessage("InitialStateOn"));
        onButton.setToolTipText(Bundle.getMessage("TooltipSendOnEvent"));
        onButton.setSelected(true);

        offButton.setText(Bundle.getMessage("InitialStateOff"));
        offButton.setToolTipText(Bundle.getMessage("TooltipSendOffEvent"));
        
        requestButton.setText(Bundle.getMessage("CbusEventRequest"));
        requestButton.setToolTipText(Bundle.getMessage("TooltipSendRequestEvent"));

        sendEvButton.setText(Bundle.getMessage("ButtonSend"));
        sendEvButton.setToolTipText(Bundle.getMessage("TooltipSendEvent"));
    
        decimalCheckBoxC.setText(Bundle.getMessage("ButtonDecimal"));
        decimalCheckBoxC.setToolTipText(Bundle.getMessage("TooltipDecimal"));
        decimalCheckBoxC.setSelected(true);
        
        showEventData.setText("Event Data");
        showEventData.setToolTipText("Show / hide Event Data Input");
        
        addButtonListeners();
    
    }
    
    private JPanel getNewDataPanel(){
    
        initDataPanelInputs();
        
        JPanel dataPanel = new JPanel();
        
        JPanel group = new JPanel();
        group.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
        
        group.add(select0Data);
        group.add(select1Data);
        group.add(select2Data);
        group.add(select3Data);
        
        data = new JTextField[3];
        
        for (int i = 0; i < 3; i++) {
            data[i] = new JTextField("0", 10); // binary input
            data[i].setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("OPC_DA") + (i+1)));
            data[i].setToolTipText(Bundle.getMessage("TooltipDbX", i+1));
            dataPanel.add(data[i]);
        }
        
        dataPanel.add(group);
    
        dataPanel.setVisible(false);
        return dataPanel;
    }
    
    private void initDataPanelInputs(){
    
        select0Data = new JRadioButton();
        select1Data = new JRadioButton();
        select2Data = new JRadioButton();
        select3Data = new JRadioButton();
        
        select0Data.setText("No Data");
        select1Data.setText("1 byte");
        select2Data.setText("2 bytes");
        select3Data.setText("3 bytes");
        
        dataGroup.add(select0Data);
        dataGroup.add(select1Data);
        dataGroup.add(select2Data);
        dataGroup.add(select3Data);
        
        select0Data.setSelected(true);
        _selectedData = 0;
        
        select0Data.addActionListener(this::dataSelectTypePerformed);
        select1Data.addActionListener(this::dataSelectTypePerformed);
        select2Data.addActionListener(this::dataSelectTypePerformed);
        select3Data.addActionListener(this::dataSelectTypePerformed);
    
    }
    
    private void addButtonListeners() {
        
        nnField = new JTextField("0", 5);
        nnField.setToolTipText("<html>" + Bundle.getMessage("ToolTipNodeNumber") + "<br>" +
            Bundle.getMessage("ToolTipPrefix") + "</html>");
        
        evField = new JTextField("0", 5);
        evField.setToolTipText("<html>" + Bundle.getMessage("ToolTipEvent") + "<br>" +
            Bundle.getMessage("ToolTipPrefix") + "</html>");
        
        nnField.addActionListener(this::sendEvButtonActionPerformed);        
        evField.addActionListener(this::sendEvButtonActionPerformed);
        sendEvButton.addActionListener(this::sendEvButtonActionPerformed);
        
        showEventData.addActionListener((ActionEvent e) -> {
            eventDataPanel.setVisible(showEventData.isSelected());
        });
        
        onButton.addActionListener(this::dataSelectTypePerformed);
        offButton.addActionListener(this::dataSelectTypePerformed);
        requestButton.addActionListener(this::dataSelectTypePerformed);
    
    }
    
    private void dataSelectTypePerformed(java.awt.event.ActionEvent e) {
        
        select0Data.setEnabled(!requestButton.isSelected());
        select1Data.setEnabled(!requestButton.isSelected());
        select2Data.setEnabled(!requestButton.isSelected());
        select3Data.setEnabled(!requestButton.isSelected());
        
        if (requestButton.isSelected() || select0Data.isSelected()){
            _selectedData = 0;
        }
        else if (select1Data.isSelected()) {
            _selectedData = 1;
        }
        else if (select2Data.isSelected()) {
            _selectedData = 2;
        }
        else if (select3Data.isSelected()) {
            _selectedData = 3;
        }
        setWhichDataEnabled(_selectedData);
    }
    
    private void setWhichDataEnabled(int numData) {
        for (int i = 0; i < 3; i++) {
            data[i].setEnabled(numData>i);
        }
    }
    
    private void sendEvButtonActionPerformed(java.awt.event.ActionEvent e) {

        int nn = ValidationNotifications.parseBinDecHexByte(nnField.getText().trim(), 65535, decimalCheckBoxC.isSelected(), 
            Bundle.getMessage("SendEventNodeError"),_mainPane);
        if (nn == -1) { return; }
        int ev = ValidationNotifications.parseBinDecHexByte(evField.getText().trim(), 65535, decimalCheckBoxC.isSelected(), 
            Bundle.getMessage("SendEventInvalidError"),_mainPane);
        if (ev == -1) { return; }
        
        CbusEvent event = new CbusEvent(_mainPane.getMemo(),nn,ev);
        if (!checkData(event)){
            return;
        }
        
        if (onButton.isSelected()) {
            event.sendOn();
        }
        else if (offButton.isSelected()){
            event.sendOff();
        }
        else {
            event.sendRequest();
        }

    }
    
    private boolean checkData( CbusEvent event ) {
        event.setNumElements(_selectedData);
        for (int i = 0; i < _selectedData; i++) {
            int dat = ValidationNotifications.parseBinDecHexByte(data[i].getText().trim(), 256, 
                decimalCheckBoxC.isSelected(), Bundle.getMessage("DbxErrorDialog",i+1),_mainPane);
            if (dat == -1) { return false; }
            event.setData(i+1, dat);
        }
        return true;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusSendEventPane.class);
}
