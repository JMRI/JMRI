package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.util.FileUtil;
import jmri.util.swing.EditableResizableImagePanel;

/**
 * A very specific dialog for editing the properties of a FunctionButton object.
 */
public class FunctionButtonPropertyEditor extends JDialog {

    private FunctionButton button;

    private JTextField textField;
    private JCheckBox lockableCheckBox;
    private JTextField idField;
    private JTextField fontField;
    private JCheckBox visibleCheckBox;
    private EditableResizableImagePanel _imageFilePath;
    private EditableResizableImagePanel _imagePressedFilePath;
    final static int BUT_IMG_SIZE = 45;

    /**
     * Constructor. Create it and pack it.
     */
    public FunctionButtonPropertyEditor() {
        initGUI();
        pack();
    }

    /**
     * Create, initilize, and place the GUI objects.
     */
    private void initGUI() {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setTitle(Bundle.getMessage("ButtonEditFunction"));
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
        propertyPanel.add(new JLabel(Bundle.getMessage("LabelFunctionNumber")), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(idField, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 1;
        textField = new JTextField();
        textField.setColumns(10);
        propertyPanel.add(new JLabel(Bundle.getMessage("LabelText")), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(textField, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 2;
        fontField = new JTextField();
        fontField.setColumns(10);
        propertyPanel.add(new JLabel(Bundle.getMessage("LabelFontSize")), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(fontField, constraints);

        lockableCheckBox = new JCheckBox(Bundle.getMessage("CheckBoxLockable"));
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = 3;
        propertyPanel.add(lockableCheckBox, constraints);

        visibleCheckBox = new JCheckBox(Bundle.getMessage("CheckBoxVisible"));
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = 4;
        propertyPanel.add(visibleCheckBox, constraints);

        constraints.gridy = 5;
        constraints.gridx = 0;
        propertyPanel.add(new JLabel(Bundle.getMessage("OffIcon")), constraints);

        constraints.gridx = 1;
        propertyPanel.add(new JLabel(Bundle.getMessage("OnIcon")), constraints);

        constraints.gridy = 6;
        constraints.gridx = 0;
        _imageFilePath = new EditableResizableImagePanel("", BUT_IMG_SIZE, BUT_IMG_SIZE);
        _imageFilePath.setDropFolder(FileUtil.getUserResourcePath());
        _imageFilePath.setBackground(new Color(0, 0, 0, 0));
        _imageFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        propertyPanel.add(_imageFilePath, constraints);

        constraints.gridx = 1;
        _imagePressedFilePath = new EditableResizableImagePanel("", BUT_IMG_SIZE, BUT_IMG_SIZE);
        _imagePressedFilePath.setDropFolder(FileUtil.getUserResourcePath());
        _imagePressedFilePath.setBackground(new Color(0, 0, 0, 0));
        _imagePressedFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        propertyPanel.add(_imagePressedFilePath, constraints);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 4, 4));

        JButton saveButton = new JButton(Bundle.getMessage("ButtonOK"));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProperties();
            }
        });

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finishEdit();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(propertyPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    }

    /**
     * Set the FunctionButton this dialog will edit. Method will initialize GUI
     * from button properties.
     *
     * @param button The FunctionButton to edit.
     */
    public void setFunctionButton(FunctionButton button) {
        this.button = button;
        textField.setText(button.getButtonLabel());
        lockableCheckBox.setSelected(button.getIsLockable());
        idField.setText(String.valueOf(button.getIdentity()));
        fontField.setText(String.valueOf(button.getFont().getSize()));
        visibleCheckBox.setSelected(button.getDisplay());
        _imageFilePath.setImagePath(button.getIconPath());
        _imagePressedFilePath.setImagePath(button.getSelectedIconPath());
        textField.requestFocus();
    }

    /**
     * Save the user-modified properties back to the FunctionButton.
     */
    private void saveProperties() {
        if (isDataValid()) {
            button.setButtonLabel(textField.getText());
            button.setIsLockable(lockableCheckBox.isSelected());
            button.setIdentity(Integer.parseInt(idField.getText()));
            String name = button.getFont().getName();
            button.setFont(new Font(name,
                    button.getFont().getStyle(),
                    Integer.parseInt(fontField.getText())));
            button.setVisible(visibleCheckBox.isSelected());
            button.setDisplay(visibleCheckBox.isSelected());
            button.setIconPath(_imageFilePath.getImagePath());
            button.setSelectedIconPath(_imagePressedFilePath.getImagePath());
            button.setDirty(true);
            button.updateLnF();
            finishEdit();
        }
    }

    /**
     * Finish the editing process. Hide the dialog.
     */
    private void finishEdit() {
        this.setVisible(false);
    }

    /**
     * Verify the data on the dialog. If invalid, notify user of errors.
     */
    private boolean isDataValid() {
        StringBuffer errors = new StringBuffer();
        int errorNumber = 0;
        /* ID >=0 && ID <= 28 */
        try {
            int id = Integer.parseInt(idField.getText());
            if ((id < 0) || id > 28) {
                throw new NumberFormatException("");
            }
        } catch (NumberFormatException ex) {
            errors.append(String.valueOf(++errorNumber));
            errors.append(". " + Bundle.getMessage("ErrorFunctionKeyRange") + "\n");
        }

        /* font > 0 */
        try {
            int size = Integer.parseInt(fontField.getText());
            if (size < 1) {
                throw new NumberFormatException("");
            }
        } catch (NumberFormatException ex) {
            errors.append(String.valueOf(++errorNumber));
            errors.append(". " + Bundle.getMessage("ErrorFontSize"));
        }

        if (errorNumber > 0) {
            JOptionPane.showMessageDialog(this, errors,
                    "Errors on page", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
