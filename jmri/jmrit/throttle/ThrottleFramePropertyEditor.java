package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A very specific dialog for editing the properties of a ThrottleFrame
 * object.
 */
public class ThrottleFramePropertyEditor extends JDialog
{
    private ThrottleFrame frame;
	
	private JTextField titleField;
	
    /**
     * Constructor. Create it and pack it.
     */
    public ThrottleFramePropertyEditor()
    {
        initGUI();
		//this.setModal(true);
        pack();
    }

    /**
     * Create, initilize, and place the GUI objects.
     */
    private void initGUI()
    {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setTitle("Edit Throttle Frame");
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

        titleField = new JTextField();
        titleField.setColumns(24);
        propertyPanel.add(new JLabel("Frame Title:"), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(titleField, constraints);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 4, 4));

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                saveProperties();
            }
        });


        JButton cancelButton = new JButton("Cancel");
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
     * Set the FunctionButton this dialog will edit. Method will
     * initialize GUI from button properties.
     * @param button The FunctionButton to edit.
     */
    public void setThrottleFrame(ThrottleFrame f)
    {
        this.frame = f;
        titleField.setText(frame.getTitle());
    }

    /**
     * Save the user-modified properties back to the ThrottleFrame.
     */
    private void saveProperties()
    {
        if (isDataValid())
        {
            frame.setTitle(titleField.getText());
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
     */
    private boolean isDataValid()
    {
        StringBuffer errors = new StringBuffer();
        int errorNumber = 0;


        if (errorNumber > 0)
        {
            JOptionPane.showMessageDialog(this, errors,
                    "Errors on page", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
	
}

