package jmri.jmrit.simplelightctrl;

import java.awt.Dimension;
import java.text.DecimalFormat;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.Light;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame controlling a single light.
 * <p>
 * Built from a copy of simple turnout control.
 *
 * @author Ken Cameron Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SimpleLightCtrlFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {

    DecimalFormat threeDigits = new DecimalFormat("000");
    DecimalFormat oneDigits = new DecimalFormat("0");
    DecimalFormat oneDotTwoDigits = new DecimalFormat("0.00");

    Light light = null;
    String newState = "";

    // GUI member declarations
    javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
    javax.swing.JTextField adrTextField = new javax.swing.JTextField(5);
    javax.swing.JButton statusButton = new javax.swing.JButton();

    javax.swing.JButton onButton = new javax.swing.JButton();
    javax.swing.JButton offButton = new javax.swing.JButton();

    javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowStateTextField = new javax.swing.JLabel();
    javax.swing.JLabel textIsEnabledLabel = new javax.swing.JLabel();
    javax.swing.JCheckBox statusIsEnabledCheckBox = new javax.swing.JCheckBox();
    javax.swing.JLabel textIsVariableLabel = new javax.swing.JLabel();
    javax.swing.JCheckBox statusIsVariableCheckBox = new javax.swing.JCheckBox();
    javax.swing.JLabel textIsTransitionLabel = new javax.swing.JLabel();
    javax.swing.JCheckBox statusIsTransitionCheckBox = new javax.swing.JCheckBox();

    javax.swing.JLabel intensityTextLabel1 = new javax.swing.JLabel();
    javax.swing.JLabel nowIntensityLabel = new javax.swing.JLabel();
    javax.swing.JTextField intensityTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel intensityTextLabel2 = new javax.swing.JLabel();
    javax.swing.JButton intensityButton = new javax.swing.JButton();

    javax.swing.JLabel intensityMinTextLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowIntensityMinLabel = new javax.swing.JLabel();
    javax.swing.JTextField intensityMinTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel intensityMaxTextLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowIntensityMaxLabel = new javax.swing.JLabel();
    javax.swing.JTextField intensityMaxTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel transitionTimeTextLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowTransitionTimeLabel = new javax.swing.JLabel();
    javax.swing.JTextField transitionTimeTextField = new javax.swing.JTextField(4);

    javax.swing.JButton applyButton = new javax.swing.JButton();

    public SimpleLightCtrlFrame() {
        super();

        // configure items for GUI
        textAdrLabel.setText(Bundle.getMessage("LightAdrLabel"));
        textAdrLabel.setVisible(true);

        adrTextField.setText("");
        adrTextField.setVisible(true);
        adrTextField.setToolTipText(Bundle.getMessage("LightAdrTextToolTip"));

        statusButton.setText(Bundle.getMessage("LightGetStatusButton"));
        statusButton.setVisible(true);
        statusButton.setToolTipText(Bundle.getMessage("LightGetStatusToolTip"));
        statusButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                statusButtonActionPerformed(e);
            }
        });

        textStateLabel.setText(Bundle.getMessage("LightStatusLabel"));
        textStateLabel.setVisible(true);
        nowStateTextField.setText(Bundle.getMessage("BeanStateUnknown"));
        nowStateTextField.setVisible(true);
        textIsEnabledLabel.setText(Bundle.getMessage("LightIsEnabledLabel"));
        textIsEnabledLabel.setVisible(true);
        statusIsEnabledCheckBox.setVisible(true);
        statusIsEnabledCheckBox.setEnabled(false);
        textIsVariableLabel.setText(Bundle.getMessage("LightIsVariableLabel"));
        textIsVariableLabel.setVisible(true);
        statusIsVariableCheckBox.setVisible(true);
        statusIsVariableCheckBox.setEnabled(false);
        textIsTransitionLabel.setText(Bundle.getMessage("LightIsTransitionLabel"));
        textIsTransitionLabel.setVisible(true);
        statusIsTransitionCheckBox.setVisible(true);
        statusIsTransitionCheckBox.setEnabled(false);

        onButton.setText(Bundle.getMessage("StateOn"));
        onButton.setVisible(true);
        onButton.setToolTipText(Bundle.getMessage("LightOnButtonToolTip"));
        onButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                onButtonActionPerformed(e);
            }
        });

        offButton.setText(Bundle.getMessage("StateOff"));
        offButton.setVisible(true);
        offButton.setToolTipText(Bundle.getMessage("LightOffButtonToolTip"));
        offButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                offButtonActionPerformed(e);
            }
        });

        intensityTextLabel1.setText(Bundle.getMessage("LightIntensityTextLabel"));
        intensityTextLabel1.setVisible(true);
        nowIntensityLabel.setText("");
        nowIntensityLabel.setVisible(true);
        intensityTextField.setText(oneDigits.format(0));
        intensityTextField.setVisible(true);
        intensityTextLabel2.setText("%");
        intensityTextField.setToolTipText(Bundle.getMessage("LightIntensityTextToolTip"));

        intensityMinTextLabel.setText(Bundle.getMessage("LightMinIntensityLabel"));
        nowIntensityMinLabel.setText("");
        nowIntensityMinLabel.setVisible(true);
        intensityMinTextField.setText(oneDigits.format(0));
        intensityMinTextField.setVisible(true);
        intensityMinTextField.setToolTipText(Bundle.getMessage("LightMinIntensityToolTip"));
        intensityMaxTextLabel.setText(Bundle.getMessage("LightMaxIntensityLabel"));
        nowIntensityMaxLabel.setText("");
        nowIntensityMaxLabel.setVisible(true);
        intensityMaxTextField.setText(oneDigits.format(100));
        intensityMaxTextField.setVisible(true);
        intensityMaxTextField.setToolTipText(Bundle.getMessage("LightMinIntensityToolTip"));
        transitionTimeTextLabel.setText(Bundle.getMessage("LightTransitionTimeLabel"));
        nowTransitionTimeLabel.setText("");
        nowTransitionTimeLabel.setVisible(true);
        transitionTimeTextField.setText(oneDigits.format(0));
        transitionTimeTextField.setVisible(true);
        transitionTimeTextField.setEnabled(true);
        transitionTimeTextField.setToolTipText(Bundle.getMessage("LightTransitionTimeToolTip"));
        intensityButton.setText(Bundle.getMessage("LightSetButton"));
        intensityButton.setVisible(true);
        intensityButton.setToolTipText(Bundle.getMessage("LightSetButtonToolTip"));
        intensityButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                intensityButtonActionPerformed(e);
            }
        });

        applyButton.setText(Bundle.getMessage("ButtonApply"));
        applyButton.setVisible(true);
        applyButton.setToolTipText(Bundle.getMessage("LightApplyButtonToolTip"));
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                applyButtonActionPerformed(e);
            }
        });

        // general GUI config
        setTitle(Bundle.getMessage("LightBorder"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane2 = new JPanel();
        pane2.add(textAdrLabel);
        pane2.add(adrTextField);
        pane2.add(statusButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(textStateLabel);
        pane2.add(nowStateTextField);
        pane2.add(textIsEnabledLabel);
        pane2.add(statusIsEnabledCheckBox);
        pane2.add(textIsVariableLabel);
        pane2.add(statusIsVariableCheckBox);
        pane2.add(textIsTransitionLabel);
        pane2.add(statusIsTransitionCheckBox);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(onButton);
        pane2.add(offButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(intensityTextLabel1);
        pane2.add(nowIntensityLabel);
        pane2.add(intensityTextField);
        pane2.add(intensityTextLabel2);
        pane2.add(intensityButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(intensityMinTextLabel);
        pane2.add(nowIntensityMinLabel);
        pane2.add(intensityMinTextField);
        pane2.add(intensityMaxTextLabel);
        pane2.add(nowIntensityMaxLabel);
        pane2.add(intensityMaxTextField);
        pane2.add(transitionTimeTextLabel);
        pane2.add(nowTransitionTimeLabel);
        pane2.add(transitionTimeTextField);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(applyButton);
        getContentPane().add(pane2);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.simplelightctrl.SimpleLightCtrl", true);

        setMinimumSize(new Dimension(600, 200));
        setSize(700, 300);
        pack();

    }

    public void offButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        if (adrTextField.getText().length() < 1) {
            nowStateTextField.setText(Bundle.getMessage("NoAddressHint"));
            return;
        }
        try {
            if (light != null) {
                // we're changing the light we're watching
                light.removePropertyChangeListener(this);
            }
            try {
                light = InstanceManager.lightManagerInstance().provideLight(
                    adrTextField.getText());

            } catch (IllegalArgumentException ex) {
                log.error(Bundle.getMessage("LightErrorButtonNameBad") + adrTextField.getText());
            }
            light.addPropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                log.debug("about to command OFF"); // NOI18N
            }
            // and set commanded state to OFF (CLOSED)
            light.setState(Light.OFF);

        } catch (Exception ex) {
            log.error(Bundle.getMessage("LightErrorOffButtonException") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    public void onButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        if (adrTextField.getText().length() < 1) {
            nowStateTextField.setText(Bundle.getMessage("NoAddressHint"));
            return;
        }
        try {
            if (light != null) {
                // we're changing the light we're watching
                light.removePropertyChangeListener(this);
            }
            try {
                light = InstanceManager.lightManagerInstance().provideLight(
                    adrTextField.getText());

            } catch (IllegalArgumentException ex) {
                log.error(Bundle.getMessage("LightErrorButtonNameBad") + adrTextField.getText());
            } 
            light.addPropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                log.debug("about to command ON"); // NOI18N
            }
            // and set commanded state to ON
            light.setState(Light.ON);
        } catch (Exception ex) {
            log.error(Bundle.getMessage("LightErrorOnButtonException") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    public void intensityButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        try {
            if (light != null) {
                // we're changing the light we're watching
                light.removePropertyChangeListener(this);
            }
            try {
                light = InstanceManager.lightManagerInstance().provideLight(
                    adrTextField.getText());

            } catch (IllegalArgumentException ex) {
                log.error(Bundle.getMessage("LightErrorButtonNameBad") + adrTextField.getText());
            }
            light.addPropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                log.debug("about to command DIM"); // NOI18N
            }
            // and set commanded state to DIM
            light.setTargetIntensity(Double.parseDouble(intensityTextField.getText().trim()) / 100);

        } catch (Exception ex) {
            log.error(Bundle.getMessage("LightErrorIntensityButtonException") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    /**
     * Handle changes for intensity, rate, etc.
     */
    public void applyButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        try {
            if (light != null) {
                // we're changing the light we're watching
                light.removePropertyChangeListener(this);
            }
            try {
                light = InstanceManager.lightManagerInstance().provideLight(adrTextField.getText());

            } catch (IllegalArgumentException ex) {
                nowStateTextField.setText(Bundle.getMessage("LightErrorButtonNameBad") + adrTextField.getText());
            }
            
            double min = Double.parseDouble(intensityMinTextField.getText()) / 100.;
            double max = Double.parseDouble(intensityMaxTextField.getText()) / 100.;
            double time = Double.parseDouble(transitionTimeTextField.getText());
            if (log.isDebugEnabled()) {
                log.debug("setting min: " + min + " max: " + max + " transition: " + time); // NOI18N
            }
            light.setMinIntensity(min);
            light.setMaxIntensity(max);
            light.setTransitionTime(time);
            updateLightStatusFields(false);

        } catch (Exception ex) {
            log.error(Bundle.getMessage("LightErrorApplyButtonException") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    /**
     * Handle request to update status.
     */
    public void statusButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        try {
            if (light != null) {
                // we're changing the light we're watching
                light.removePropertyChangeListener(this);
            }
            try {
                light = InstanceManager.lightManagerInstance().provideLight(adrTextField.getText());

            } catch (IllegalArgumentException ex) {
                nowStateTextField.setText(Bundle.getMessage("LightErrorButtonNameBad") + adrTextField.getText());
            }
            updateLightStatusFields(true);

        } catch (Exception ex) {
            log.error(Bundle.getMessage("LightErrorStatusButtonException") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    /**
     * Update state field in GUI as state of light changes.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("recv propertyChange: " + e.getPropertyName() + " " + e.getOldValue() + " -> " + e.getNewValue());
        }
        updateLightStatusFields(false);
    }

    private void updateLightStatusFields(boolean flag) {
        int knownState = light.getState();
        switch (knownState) {
            case Light.ON:
                nowStateTextField.setText(Bundle.getMessage("StateOn"));
                break;
            case Light.INTERMEDIATE:
                nowStateTextField.setText(Bundle.getMessage("LightStateIntermediate"));
                break;
            case Light.OFF:
                nowStateTextField.setText(Bundle.getMessage("StateOff"));
                break;
            case Light.TRANSITIONINGTOFULLON:
                nowStateTextField.setText(Bundle.getMessage("LightStateTransitioningToFullOn"));
                break;
            case Light.TRANSITIONINGHIGHER:
                nowStateTextField.setText(Bundle.getMessage("LightStateTransitioningHigher"));
                break;
            case Light.TRANSITIONINGLOWER:
                nowStateTextField.setText(Bundle.getMessage("LightStateTransitioningLower"));
                break;
            case Light.TRANSITIONINGTOFULLOFF:
                nowStateTextField.setText(Bundle.getMessage("LightStateTransitioningToFullOff"));
                break;
            default:
                nowStateTextField.setText(Bundle.getMessage("UnexpectedValueLabel", knownState));
                break;
        }
        statusIsEnabledCheckBox.setSelected(light.getEnabled());
        statusIsVariableCheckBox.setSelected(light.isIntensityVariable());
        statusIsTransitionCheckBox.setSelected(light.isTransitionAvailable());
        nowIntensityLabel.setText(oneDigits.format(light.getCurrentIntensity() * 100));
        nowTransitionTimeLabel.setText(oneDotTwoDigits.format(light.getTransitionTime()));
        nowIntensityMinLabel.setText(oneDigits.format(light.getMinIntensity() * 100));
        nowIntensityMaxLabel.setText(oneDigits.format(light.getMaxIntensity() * 100));
        if (flag) {
            intensityTextField.setText(oneDigits.format(light.getTargetIntensity() * 100));
            transitionTimeTextField.setText(oneDotTwoDigits.format(light.getTransitionTime()));
            intensityMinTextField.setText(oneDigits.format(light.getMinIntensity() * 100));
            intensityMaxTextField.setText(oneDigits.format(light.getMaxIntensity() * 100));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleLightCtrlFrame.class);

}
