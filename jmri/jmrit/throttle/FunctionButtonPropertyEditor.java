package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A very specific dialog for editing the properties of a FunctionButton
 * object.
 */
public class FunctionButtonPropertyEditor extends JDialog
{
    private FunctionButton button;

    private JTextField textField;
    private JCheckBox lockableCheckBox;
    private JTextField idField;
    private JTextField fontField;
    private JCheckBox visibleCheckBox;

    /**
     * Constructor. Create it and pack it.
     */
    public FunctionButtonPropertyEditor()
    {
        initGUI();
        pack();
    }

    /**
     * Create, initilize, and place the GUI objects.
     */
    private void initGUI()
    {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setTitle("Edit Function Button");
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

        idField = new JTextField();
        idField.setColumns(1);
        propertyPanel.add(new JLabel("Function Number:"), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(idField, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 1;
        textField = new JTextField();
        textField.setColumns(10);
        propertyPanel.add(new JLabel("Text:"), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(textField, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 2;
        fontField = new JTextField();
        fontField.setColumns(10);
        propertyPanel.add(new JLabel("Font Size:"), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(fontField, constraints);

        lockableCheckBox = new JCheckBox("Lockable");
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = 3;
        propertyPanel.add(lockableCheckBox, constraints);

        visibleCheckBox = new JCheckBox("Visible");
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = 4;
        propertyPanel.add(visibleCheckBox, constraints);

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
    public void setFunctionButton(FunctionButton button)
    {
        this.button = button;
        textField.setText(button.getText());
        lockableCheckBox.setSelected(button.getIsLockable());
        idField.setText(String.valueOf(button.getIdentity()));
        fontField.setText(String.valueOf(button.getFont().getSize()));
        visibleCheckBox.setSelected(button.isVisible());
    }

    /**
     * Save the user-modified properties back to the FunctionButton.
     */
    private void saveProperties()
    {
        if (isDataValid())
        {
            button.setText(textField.getText());
            button.setIsLockable(lockableCheckBox.isSelected());
            button.setIdentity(Integer.parseInt(idField.getText()));
            button.setFont(new Font(button.getFont().getFontName(),
                                    button.getFont().getStyle(),
                                    Integer.parseInt(fontField.getText())));
            button.setVisible(visibleCheckBox.isSelected());
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
        /* ID >=0 && ID <= 12 */
        try
        {
            int id = Integer.parseInt(idField.getText());
            if ((id < 0) || id > 12)
            {
                throw new NumberFormatException("");
            }
        }
        catch (NumberFormatException ex)
        {
            errors.append(String.valueOf(++errorNumber));
            errors.append(". Function number must integer between 0 and 12\n");
        }

        /* font > 0 */
        try
        {
            int size = Integer.parseInt(fontField.getText());
            if (size < 1)
            {
                throw new NumberFormatException("");
            }
        }
        catch (NumberFormatException ex)
        {
            errors.append(String.valueOf(++errorNumber));
            errors.append(". Font size must integer greater than 0");
        }



        if (errorNumber > 0)
        {
            JOptionPane.showMessageDialog(this, errors,
                    "Errors on page", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}