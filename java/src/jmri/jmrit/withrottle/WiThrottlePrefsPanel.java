package jmri.jmrit.withrottle;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision$
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import java.io.File;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import jmri.util.FileUtil;

public class WiThrottlePrefsPanel extends JPanel{
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");
    
    JCheckBox eStopCB;
    JSpinner delaySpinner;
    
    JCheckBox momF2CB;

    JCheckBox portCB;
    JTextField port;

    JCheckBox powerCB;
    JCheckBox turnoutCB;
    JCheckBox routeCB;
    JCheckBox consistCB;
    JRadioButton wifiRB;
    JRadioButton dccRB;

    JButton saveB;
    JButton cancelB;

    WiThrottlePreferences localPrefs;
    JFrame parentFrame = null;
    boolean enableSave;

    public WiThrottlePrefsPanel(){
        if(jmri.InstanceManager.getDefault(jmri.jmrit.withrottle.WiThrottlePreferences.class)==null){
            jmri.InstanceManager.store(new jmri.jmrit.withrottle.WiThrottlePreferences(FileUtil.getUserFilesPath()+ "throttle" +File.separator+ "WiThrottlePreferences.xml"), jmri.jmrit.withrottle.WiThrottlePreferences.class);
        }
        localPrefs = jmri.InstanceManager.getDefault(jmri.jmrit.withrottle.WiThrottlePreferences.class);
        //  set local prefs to match instance prefs
        //localPrefs.apply(WiThrottleManager.withrottlePreferencesInstance());
        initGUI();
        setGUI();
    }

    public WiThrottlePrefsPanel(JFrame f){
        this();
        parentFrame = f;
    }

    public void initGUI(){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(eStopDelayPanel());
        add(functionsPanel());
        add(socketPortPanel());
        add(allowedControllers());
        add(cancelApplySave());

    }

    private void setGUI(){
        eStopCB.setSelected(localPrefs.isUseEStop());
        delaySpinner.setValue(localPrefs.getEStopDelay());
        
        momF2CB.setSelected(localPrefs.isUseMomF2());

        portCB.setSelected(localPrefs.isUseFixedPort());
        updatePortField();

        powerCB.setSelected(localPrefs.isAllowTrackPower());
        turnoutCB.setSelected(localPrefs.isAllowTurnout());
        routeCB.setSelected(localPrefs.isAllowRoute());
        consistCB.setSelected(localPrefs.isAllowConsist());
        wifiRB.setSelected(localPrefs.isUseWiFiConsist());
        dccRB.setSelected(!localPrefs.isUseWiFiConsist());
    }

/**
 * Show the save and cancel buttons if displayed in its own frame.
 */
    public void enableSave(){
        saveB.setVisible(true);
        cancelB.setVisible(true);
    }
/**
 * set the local prefs to match the GUI
 * Local prefs are independant from the singleton instance prefs.
 * @return true if set, false if values are unacceptable.
 */
    private boolean setValues(){
        boolean didSet = true;
        localPrefs.setUseEStop(eStopCB.isSelected());
        localPrefs.setEStopDelay((Integer)delaySpinner.getValue());
        
        localPrefs.setUseMomF2(momF2CB.isSelected());

        localPrefs.setUseFixedPort(portCB.isSelected());
        if (portCB.isSelected()){
            int portNum;
            try{
                portNum = Integer.parseInt(port.getText());
            }catch (NumberFormatException NFE){ //  Not a number
                portNum = 0;
            }
            if ((portNum < 1024) || (portNum > 65535)){ //  Invalid port value
                javax.swing.JOptionPane.showMessageDialog(this,
                    rb.getString("WarningInvalidPort"),
                    rb.getString("TitlePortWarningDialog"),
                    JOptionPane.WARNING_MESSAGE);
                didSet = false;
            }else {
                localPrefs.setPort(port.getText());
            }
        }
        

        localPrefs.setAllowTrackPower(powerCB.isSelected());
        localPrefs.setAllowTurnout(turnoutCB.isSelected());
        localPrefs.setAllowRoute(routeCB.isSelected());
        localPrefs.setAllowConsist(consistCB.isSelected());
        localPrefs.setUseWiFiConsist(wifiRB.isSelected());

        return didSet;
    }

    public void storeValues() {
        if (setValues()){
            WiThrottleManager.withrottlePreferencesInstance().apply(localPrefs);
            WiThrottleManager.withrottlePreferencesInstance().save();
            
            if (parentFrame != null){
                parentFrame.dispose();
            }
        }

        
    }
/**
 * Update the singleton instance of prefs,
 * then mark (isDirty) that the
 * values have changed and needs to save to xml file.
 */
    protected void applyValues(){
        if (setValues()){
            WiThrottleManager.withrottlePreferencesInstance().apply(localPrefs);
            WiThrottleManager.withrottlePreferencesInstance().setIsDirty(true); //  mark to save later
        }
    }

    protected void cancelValues(){
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }


    private JPanel eStopDelayPanel(){
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder(rb.getString("TitleDelayPanel")),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        eStopCB = new JCheckBox(rb.getString("LabelUseEStop"));
        eStopCB.setToolTipText(rb.getString("ToolTipUseEStop"));
        SpinnerNumberModel spinMod = new SpinnerNumberModel(10,4,60,2);
        delaySpinner = new JSpinner(spinMod);
        ((JSpinner.DefaultEditor)delaySpinner.getEditor()).getTextField().setEditable(false);
        panel.add(eStopCB);
        panel.add(delaySpinner);
        panel.add(new JLabel(rb.getString("LabelEStopDelay")));
        return panel;
    }
    
    private JPanel functionsPanel(){
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder(rb.getString("TitleFunctionsPanel")),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        momF2CB = new JCheckBox(rb.getString("LabelMomF2"));
        momF2CB.setToolTipText(rb.getString("ToolTipMomF2"));
        panel.add(momF2CB);
        return panel;
    }

    private JPanel socketPortPanel(){
        JPanel SPPanel = new JPanel();

        SPPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder(rb.getString("TitleNetworkPanel")),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        portCB = new JCheckBox(rb.getString("LabelUseFixedPortNumber"));
        portCB.setToolTipText(rb.getString("ToolTipUseFixedPortNumber"));
        portCB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                updatePortField();
            }
        });
        port = new JTextField();
        port.setText(rb.getString("LabelNotFixed"));
        port.setPreferredSize(port.getPreferredSize());
        SPPanel.add(portCB);
        SPPanel.add(port);
        return SPPanel;
    }

    private JPanel allowedControllers(){
        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder(rb.getString("TitleControllersPanel")),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        powerCB = new JCheckBox(rb.getString("LabelTrackPower"));
        powerCB.setToolTipText(rb.getString("ToolTipTrackPower"));
        panel.add(powerCB);

        turnoutCB = new JCheckBox(rb.getString("LabelTurnout"));
        turnoutCB.setToolTipText(rb.getString("ToolTipTurnout"));
        panel.add(turnoutCB);

        routeCB = new JCheckBox(rb.getString("LabelRoute"));
        routeCB.setToolTipText(rb.getString("ToolTipRoute"));
        panel.add(routeCB);

        consistCB = new JCheckBox(rb.getString("LabelConsist"));
        consistCB.setToolTipText(rb.getString("ToolTipConsist"));
        panel.add(consistCB);
        
        JPanel conPanel = new JPanel();
        conPanel.setLayout(new BoxLayout(conPanel, BoxLayout.Y_AXIS));
        wifiRB = new JRadioButton(rb.getString("LabelWiFiConsist"));
        wifiRB.setToolTipText(rb.getString("ToolTipWiFiConsist"));
        dccRB = new JRadioButton(rb.getString("LabelDCCConsist"));
        dccRB.setToolTipText(rb.getString("ToolTipDCCConsist"));
        
        ButtonGroup group = new ButtonGroup();
        group.add(wifiRB);
        group.add(dccRB);
        conPanel.add(wifiRB);
        conPanel.add(dccRB);
        panel.add(conPanel);

        return panel;
    }
    
    private JPanel cancelApplySave(){
        JPanel panel = new JPanel();
        cancelB = new JButton(rb.getString("ButtonCancel"));
        cancelB.setVisible(false);
        cancelB.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                cancelValues();
            }
        });
        JButton applyB = new JButton(rb.getString("ButtonApply"));
        applyB.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                applyValues();
            }
        });
        saveB = new JButton(rb.getString("ButtonSave"));
        saveB.setVisible(false);
        saveB.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent event){
                storeValues();
            }
        });
        panel.add(cancelB);
        panel.add(saveB);
        panel.add(new JLabel(rb.getString("LabelApplyWarning")));
        panel.add(applyB);
        return panel;
    }

    private void updatePortField(){
        if (portCB.isSelected()){
            port.setText(localPrefs.getPort());

        }else {
            port.setText(rb.getString("LabelNotFixed"));
        }

    }

    //private static Logger log = LoggerFactory.getLogger(WiThrottlePrefsPanel.class.getName());

}
