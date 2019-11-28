package jmri.jmrit.simplelightctrl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.Light;
import jmri.swing.NamedBeanComboBox;

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
public class SimpleLightCtrlFrame extends jmri.util.JmriJFrame {

    DecimalFormat threeDigits = new DecimalFormat("000");
    DecimalFormat oneDigits = new DecimalFormat("0");
    DecimalFormat oneDotTwoDigits = new DecimalFormat("0.00");

    Light light = null;
    String newState = "";

    // GUI member declarations
    javax.swing.JButton onButton = new javax.swing.JButton();
    javax.swing.JButton offButton = new javax.swing.JButton();

    javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowStateTextField = new javax.swing.JLabel();
    javax.swing.JLabel nowControllersTextField = new javax.swing.JLabel();
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
    private final NamedBeanComboBox<Light> to1;
    private PropertyChangeListener _parentLightListener = null;

    public SimpleLightCtrlFrame() {
        super();

        to1 = new NamedBeanComboBox<>(InstanceManager.lightManagerInstance());
        to1.setAllowNull(true);
        to1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.debug("actionevent");
                resetLightToCombo();
            }
        });

        // configure items for GUI
        textStateLabel.setText(Bundle.getMessage("LightStatusLabel"));
        textStateLabel.setVisible(true);
        nowStateTextField.setText(Bundle.getMessage("BeanStateUnknown"));
        nowStateTextField.setVisible(true);
        nowControllersTextField.setText("");
        nowControllersTextField.setVisible(true);
        textIsEnabledLabel.setText(Bundle.getMessage("LightIsEnabledLabel"));
        textIsEnabledLabel.setToolTipText(Bundle.getMessage("LightIsEnabledLabelToolTip"));
        textIsEnabledLabel.setVisible(true);
        statusIsEnabledCheckBox.setVisible(true);
        statusIsEnabledCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enabledCheckboxActionPerformed(e);
            }
        });
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
        onButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onButtonActionPerformed(e);
            }
        });

        offButton.setText(Bundle.getMessage("StateOff"));
        offButton.setVisible(true);
        offButton.setToolTipText(Bundle.getMessage("LightOffButtonToolTip"));
        offButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        intensityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                intensityButtonActionPerformed(e);
            }
        });

        applyButton.setText(Bundle.getMessage("ButtonApply"));
        applyButton.setVisible(true);
        applyButton.setToolTipText(Bundle.getMessage("LightApplyButtonToolTip"));
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyButtonActionPerformed(e);
            }
        });

        // set buttons inactive as no Light yet selected
        setControlFrameActive(false);

        // general GUI config
        setTitle(Bundle.getMessage("LightBorder"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Light select
        JPanel pane2 = new JPanel();
        pane2.add(to1);
        getContentPane().add(pane2);

        // status text
        pane2 = new JPanel();
        pane2.add(textStateLabel);
        pane2.add(nowStateTextField);
        getContentPane().add(pane2);

        // on off buttons
        pane2 = new JPanel();
        pane2.add(onButton);
        pane2.add(offButton);
        getContentPane().add(pane2);
        getContentPane().add(new javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        // Controllers enabled checkbox
        pane2 = new JPanel();
        pane2.add(textIsEnabledLabel);
        pane2.add(statusIsEnabledCheckBox);
        getContentPane().add(pane2);

        // Controllers text
        pane2 = new JPanel();
        pane2.add(nowControllersTextField);
        getContentPane().add(pane2);
        getContentPane().add(new javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        // intensity field and button
        pane2 = new JPanel();
        pane2.add(intensityTextLabel1);
        pane2.add(nowIntensityLabel);
        pane2.add(intensityTextField);
        pane2.add(intensityTextLabel2);
        pane2.add(intensityButton);
        getContentPane().add(pane2);
        getContentPane().add(new javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        // min max textfields
        pane2 = new JPanel();
        pane2.add(intensityMinTextLabel);
        pane2.add(nowIntensityMinLabel);
        pane2.add(intensityMinTextField);
        pane2.add(intensityMaxTextLabel);
        pane2.add(nowIntensityMaxLabel);
        pane2.add(intensityMaxTextField);
        getContentPane().add(pane2);

        // time textfield, apply button
        pane2 = new JPanel();
        pane2.add(transitionTimeTextLabel);
        pane2.add(nowTransitionTimeLabel);
        pane2.add(transitionTimeTextField);
        pane2.add(applyButton);
        getContentPane().add(pane2);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.simplelightctrl.SimpleLightCtrl", true);

        pack();

    }

    private void setControlFrameActive(boolean showLight) {
        log.debug("selected light is {}", to1.getSelectedItem());
        onButton.setEnabled(showLight);
        offButton.setEnabled(showLight);
        statusIsEnabledCheckBox.setEnabled(showLight);

        if (showLight && light.isIntensityVariable()) {
            intensityButton.setEnabled(true);
            intensityMinTextField.setEnabled(true);
            intensityMaxTextField.setEnabled(true);
            intensityTextField.setEnabled(true);
            applyButton.setEnabled(true);
        } else {
            intensityButton.setEnabled(false);
            intensityMinTextField.setEnabled(false);
            intensityMaxTextField.setEnabled(false);
            intensityTextField.setEnabled(false);
            intensityButton.setEnabled(false);
            applyButton.setEnabled(false);
        }

        if (showLight && light.isTransitionAvailable()) {
            transitionTimeTextField.setEnabled(true);
        } else {
            transitionTimeTextField.setEnabled(false);
        }

    }

    public void offButtonActionPerformed(ActionEvent e) {
        if (to1.getSelectedItem() == null) {
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
            return;
        }
        try {
            // and set commanded state to ON
            light.setState(Light.OFF);
        } catch (Exception ex) {
            log.error(Bundle.getMessage("ErrorTitle") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    public void onButtonActionPerformed(ActionEvent e) {
        if (to1.getSelectedItem() == null) {
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
            return;
        }
        try {
            // and set commanded state to ON
            light.setState(Light.ON);
        } catch (Exception ex) {
            log.error(Bundle.getMessage("ErrorTitle") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    public void intensityButtonActionPerformed(ActionEvent e) {
        if (to1.getSelectedItem() == null) {
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
            return;
        }
        try {
            log.debug("about to command DIM"); // NOI18N
            // and set commanded state to DIM
            light.setTargetIntensity(Double.parseDouble(intensityTextField.getText().trim()) / 100);
        } catch (NumberFormatException ex) {
            log.error(Bundle.getMessage("LightErrorIntensityButtonException") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    private void enabledCheckboxActionPerformed(ActionEvent e) {
        if (statusIsEnabledCheckBox.isSelected()) {
            light.setEnabled(true);
        } else {
            light.setEnabled(false);
        }
    }

    /**
     * Handle changes for intensity, rate, etc.
     */
    public void applyButtonActionPerformed(ActionEvent e) {
        if (to1.getSelectedItem() == null) {
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
            resetLightToCombo();
            return;
        }
        // load address from switchAddrTextField
        try {
            double min = Double.parseDouble(intensityMinTextField.getText()) / 100.;
            double max = Double.parseDouble(intensityMaxTextField.getText()) / 100.;
            double time = Double.parseDouble(transitionTimeTextField.getText());
            log.debug("setting min: {} max: {} transition: {}", min, max, time); // NOI18N
            if (!light.isTransitionAvailable()) {
                time = 0.0d;
            }

            light.setMinIntensity(min);
            light.setMaxIntensity(max);
            light.setTransitionTime(time);
            updateLightStatusFields(false);

        } catch (NumberFormatException ex) {
            log.error(Bundle.getMessage("ErrorTitle") + ex.toString());
            nowStateTextField.setText(Bundle.getMessage("ErrorTitle"));
        }
    }

    private void resetLightToCombo() {
        if (light != null && light == to1.getSelectedItem()) {
            return;
        }
        log.debug("Light changed in combobox to {}", to1.getSelectedItem());
        // remove changelistener from previous Light
        if (light != null) {
            light.removePropertyChangeListener(_parentLightListener);
        }
        light = to1.getSelectedItem();
        if (light != null) {
            light.addPropertyChangeListener(
                    _parentLightListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    log.debug("recv propChange: {} {} -> {}", e.getPropertyName(), e.getOldValue(), e.getNewValue());
                    updateLightStatusFields(false);
                }
            });
            setControlFrameActive(true);
            updateLightStatusFields(true);

            StringBuilder name = new StringBuilder("<html>");
            light.getLightControlList().forEach((otherLc) -> {
                name.append(jmri.jmrit.beantable.LightTableAction.getDescriptionText(otherLc, otherLc.getControlType()));
                name.append("<br>");
            });

            if (light.getLightControlList().isEmpty()) {
                name.append("None");
            }
            name.append("</html>");
            nowControllersTextField.setText(name.toString());

            repaint();
            revalidate();
            pack();

        } else {
            setControlFrameActive(false);
            nowStateTextField.setText(Bundle.getMessage("BeanStateUnknown"));
            nowControllersTextField.setText("");
        }
    }

    // if flag true, sets intensity and time fields
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
