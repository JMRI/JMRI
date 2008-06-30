package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

/**
 * A very specific dialog for editing the properties of a FunctionButton
 * object.
 *
 * @author Paul Bender Copyright (C) 2005
 * @version $Revision: 1.6 $
 */
public class ControlPanelPropertyEditor extends JDialog
{
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.throttle.ThrottleBundle");
    private ControlPanel control;

    private JRadioButton displaySlider;
    private JRadioButton displaySteps;
    private JCheckBox trackBox;

    private int _displaySlider;

    /**
     * Constructor. Create it and pack it.
     */
    public ControlPanelPropertyEditor(ControlPanel panel)
    {
	control=panel;
        initGUI();
        pack();
    }

    /**
     * Create, initilize, and place the GUI objects.
     */
    private void initGUI()
    {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setTitle(rb.getString("TitleEditSpeedControlPanel"));
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

    	displaySlider=new JRadioButton(rb.getString("ButtonDisplaySpeedSlider"));
    	displaySteps=new JRadioButton(rb.getString("ButtonDisplaySpeedSteps"));

	modeSelectionButtons.add(displaySlider);
	modeSelectionButtons.add(displaySteps);

	_displaySlider = control.getDisplaySlider();

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridy = 1;
        propertyPanel.add(displaySlider, constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridy = 2;
        propertyPanel.add(displaySteps, constraints);

        
        trackBox = new JCheckBox(rb.getString("CheckBoxTrackSliderInRealTime"));
        constraints.gridy = 3;
        propertyPanel.add(trackBox, constraints);
	displaySlider.setSelected(_displaySlider==ControlPanel.SLIDERDISPLAY);
	displaySteps.setSelected(_displaySlider==ControlPanel.STEPDISPLAY);

	displaySlider.addActionListener(
        	new ActionListener()
                        {
                                public void actionPerformed(ActionEvent e)
                                {
                                        displaySlider.setSelected(true);
                                        displaySteps.setSelected(false);
					_displaySlider=ControlPanel.SLIDERDISPLAY;
                                }
                        });
                
	displaySteps.addActionListener(
        	new ActionListener()
                        {
                                public void actionPerformed(ActionEvent e)
                                {
                                        displaySlider.setSelected(false);
                                        displaySteps.setSelected(true);
					_displaySlider=ControlPanel.STEPDISPLAY;
                                }
                        });
 
	// disable the speed controls if the control panel says they 
	// aren't possible
	displaySlider.setEnabled(control.
				isSpeedControllerAvailable(ControlPanel.SLIDERDISPLAY));
	displaySteps.setEnabled(control.
				isSpeedControllerAvailable(ControlPanel.STEPDISPLAY));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 4, 4));

        JButton saveButton = new JButton(rb.getString("ButtonOk"));
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                saveProperties();
            }
        });

        JButton cancelButton = new JButton(rb.getString("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                finishEdit();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(propertyPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    }

    /**
     * Save the user-modified properties back to the FunctionButton.
     */
    private void saveProperties()
    {
    	if (isDataValid())
    	{
    		control.setSpeedController(_displaySlider);
    		control.setTrackSlider(trackBox.isSelected());
    		finishEdit();
    	}
    }

    /**
     * Finish the editing process. Hide the dialog.
     */
    private void finishEdit()
    {
        this.setVisible(false);
    }

    /**
     * Verify the data on the dialog. If invalid, notify user of errors.
     * This only needs to do something if we add something other than 
     * speed control selection to this panel.
     */
    private boolean isDataValid()
    {
        return true;
    }
}
