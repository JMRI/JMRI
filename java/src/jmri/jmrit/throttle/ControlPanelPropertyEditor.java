package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very specific dialog for editing the properties of a ControlPanel object.
 *
 * @author Paul Bender Copyright (C) 2005
 */
public class ControlPanelPropertyEditor extends JDialog {

    private final ControlPanel control;

    private JRadioButton displaySlider; // display slider from 0 to 100
    private JRadioButton displaySliderContinuous; // display slider from -100 to 0 to 100
    private JRadioButton displaySteps;
    private JCheckBox trackBox;
    private JCheckBox speedStepBoxVisibleBox;
    private JTextField functionSwitchSlider;

    private int _displaySlider;

    /**
     * Constructor. Create it and pack it.
     * @param panel control panel.
     */
    public ControlPanelPropertyEditor(ControlPanel panel) {
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        control = panel;
        initGUI();
        resetProperties();        
    }

    /**
     * Create, and place the GUI objects.
     */
    private void initGUI() {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setTitle(Bundle.getMessage("TitleEditSpeedControlPanel"));
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());

        JPanel propertyPanel = new JPanel();
        propertyPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 2, 2, 2);
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        ButtonGroup modeSelectionButtons = new ButtonGroup();

        displaySlider = new JRadioButton(Bundle.getMessage("ButtonDisplaySpeedSlider"));
        displaySliderContinuous = new JRadioButton(Bundle.getMessage("ButtonDisplaySpeedSliderContinuous"));
        displaySteps = new JRadioButton(Bundle.getMessage("ButtonDisplaySpeedSteps"));

        modeSelectionButtons.add(displaySlider);
        modeSelectionButtons.add(displaySteps);
        modeSelectionButtons.add(displaySliderContinuous);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridy = 1;
        propertyPanel.add(displaySlider, constraints);

        constraints.gridy = 2;
        propertyPanel.add(displaySteps, constraints);

        constraints.gridy = 3;
        propertyPanel.add(displaySliderContinuous, constraints);

        trackBox = new JCheckBox(Bundle.getMessage("CheckBoxTrackSliderInRealTime"));
        constraints.gridy = 4;
        propertyPanel.add(trackBox, constraints);
        
        speedStepBoxVisibleBox = new JCheckBox(Bundle.getMessage("CheckBoxHideSpeedStepSelector"));
        constraints.gridy = 5;
        propertyPanel.add(speedStepBoxVisibleBox, constraints);                

        JLabel functionSwitchLabel = new JLabel(Bundle.getMessage("SwitchSliderOnFunction"));
        functionSwitchSlider = new JTextField(4);
        constraints.gridy = 6;
        constraints.gridx = 0;
        propertyPanel.add(functionSwitchLabel, constraints);
        constraints.gridx = 1;
        propertyPanel.add(functionSwitchSlider, constraints);

        displaySlider.addActionListener((ActionEvent e) -> {
            displaySlider.setSelected(true);
            displaySteps.setSelected(false);
            displaySliderContinuous.setSelected(false);
            _displaySlider = ControlPanel.SLIDERDISPLAY;
        });

        displaySteps.addActionListener((ActionEvent e) -> {
            displaySlider.setSelected(false);
            displaySteps.setSelected(true);
            displaySliderContinuous.setSelected(false);
            _displaySlider = ControlPanel.STEPDISPLAY;
        });

        displaySliderContinuous.addActionListener((ActionEvent e) -> {
            displaySlider.setSelected(false);
            displaySteps.setSelected(false);
            displaySliderContinuous.setSelected(true);
            _displaySlider = ControlPanel.SLIDERDISPLAYCONTINUOUS;
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 4, 4));

        JButton applyButton = new JButton(Bundle.getMessage("ButtonApply"));
        applyButton.addActionListener((ActionEvent e) -> {
            saveProperties();
        });
                
        JButton resetButton = new JButton(Bundle.getMessage("ButtonReset"));
        resetButton.addActionListener((ActionEvent e) -> {
            resetProperties();           
        });        
        
        JButton closeButton = new JButton(Bundle.getMessage("ButtonClose"));
        closeButton.addActionListener((ActionEvent e) -> {
            finishEdit();
        });

        buttonPanel.add(resetButton);
        buttonPanel.add(closeButton);
        buttonPanel.add(applyButton);
        
        mainPanel.add(propertyPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        pack();        
    }

    /**
     * Save the user-modified properties back to the FunctionButton.
     */
    private void saveProperties() {
        if (isDataValid()) {
            control.setTrackSlider(trackBox.isSelected());
            control.setSwitchSliderFunction(functionSwitchSlider.getText());
            control.setSpeedController(_displaySlider);
            control.setHideSpeedStep(speedStepBoxVisibleBox.isSelected());
        }
    }

    /**
     * Finish the editing process. Hide the dialog.
     */
    private void finishEdit() {
        this.setVisible(false);
    }

    /**
     * Update values from the controlPanel
     */
    public void resetProperties() {
        // type of slider
        _displaySlider = control.getDisplaySlider();        
        displaySlider.setSelected(_displaySlider == ControlPanel.SLIDERDISPLAY);
        displaySteps.setSelected(_displaySlider == ControlPanel.STEPDISPLAY);
        displaySliderContinuous.setSelected(_displaySlider == ControlPanel.SLIDERDISPLAYCONTINUOUS);
        // disable the speed controls if the control panel says they aren't possible
        displaySlider.setEnabled(control.isSpeedControllerAvailable(ControlPanel.SLIDERDISPLAY));
        displaySteps.setEnabled(control.isSpeedControllerAvailable(ControlPanel.STEPDISPLAY));
        displaySliderContinuous.setEnabled(control.isSpeedControllerAvailable(ControlPanel.SLIDERDISPLAYCONTINUOUS));
        // other propertiess
        trackBox.setSelected(control.getTrackSlider());
        speedStepBoxVisibleBox.setSelected(control.getHideSpeedStep() );
        functionSwitchSlider.setText(control.getSwitchSliderFunction());
    }
        
    /**
     * Verify the data on the dialog. If invalid, notify user of errors. This
     * only needs to do something if we add something other than speed control
     * selection to this panel.
     */
    private boolean isDataValid() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(ControlPanelPropertyEditor.class);    
}
