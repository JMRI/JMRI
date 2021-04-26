package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import jmri.Throttle;
import jmri.util.FileUtil;
import jmri.util.swing.EditableResizableImagePanel;

/**
 * A very specific dialog for editing the properties of a FunctionButton object.
 */
public final class FunctionButtonPropertyEditor extends JDialog {

    private final FunctionButton button;

    private JTextField textField;
    private JCheckBox lockableCheckBox;
    private JTextField idField;
    private JTextField fontField;
    private JCheckBox visibleCheckBox;
    private EditableResizableImagePanel _imageFilePath;
    private EditableResizableImagePanel _imagePressedFilePath;
    private JTextField imageSize;
    final static int BUT_IMG_SIZE = 45;

    /**
     * Constructor. Create it and pack it.
     * @param btn the functionButton
     */
    public FunctionButtonPropertyEditor(FunctionButton btn) {
        button = btn;
        initGUI();
        resetProperties();
    }

    /**
     * Create, initialise, and place the GUI objects.
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
        constraints.gridy ++;
        textField = new JTextField();
        textField.setColumns(10);
        propertyPanel.add(new JLabel(Bundle.getMessage("LabelText")), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(textField, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy ++;
        fontField = new JTextField();
        fontField.setColumns(10);
        propertyPanel.add(new JLabel(Bundle.getMessage("LabelFontSize")), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(fontField, constraints);
        
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy ++;
        imageSize = new JTextField();
        imageSize.setColumns(10);
        propertyPanel.add(new JLabel(Bundle.getMessage("LabelFunctionImageSize")), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 1;
        propertyPanel.add(imageSize, constraints);

        lockableCheckBox = new JCheckBox(Bundle.getMessage("CheckBoxLockable"));
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy ++;
        propertyPanel.add(lockableCheckBox, constraints);

        visibleCheckBox = new JCheckBox(Bundle.getMessage("CheckBoxVisible"));
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy ++;
        propertyPanel.add(visibleCheckBox, constraints);

        constraints.gridy ++;
        constraints.gridx = 0;
        propertyPanel.add(new JLabel(Bundle.getMessage("OffIcon")), constraints);

        constraints.gridx = 1;
        propertyPanel.add(new JLabel(Bundle.getMessage("OnIcon")), constraints);

        constraints.gridy ++;
        constraints.gridx = 0;
        _imageFilePath = new EditableResizableImagePanel("", BUT_IMG_SIZE, BUT_IMG_SIZE);
        _imageFilePath.setDropFolder(FileUtil.getUserResourcePath());
        _imageFilePath.setBackground(new Color(0, 0, 0, 0));
        _imageFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        _imageFilePath.addMenuItemBrowseFolder(Bundle.getMessage("OpenSystemFileBrowserOnJMRIfnButtonsRessources"), FileUtil.getExternalFilename("resources/icons/functionicons/transparent_background"));
        propertyPanel.add(_imageFilePath, constraints);

        constraints.gridx = 1;
        _imagePressedFilePath = new EditableResizableImagePanel("", BUT_IMG_SIZE, BUT_IMG_SIZE);
        _imagePressedFilePath.setDropFolder(FileUtil.getUserResourcePath());
        _imagePressedFilePath.setBackground(new Color(0, 0, 0, 0));
        _imagePressedFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        _imagePressedFilePath.addMenuItemBrowseFolder(Bundle.getMessage("OpenSystemFileBrowserOnJMRIfnButtonsRessources"), FileUtil.getExternalFilename("resources/icons/functionicons/transparent_background"));
        propertyPanel.add(_imagePressedFilePath, constraints);

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
     * Initialize GUI from button properties.
     *
     */
    public void resetProperties() {
        textField.setText(button.getButtonLabel());
        lockableCheckBox.setSelected(button.getIsLockable());
        idField.setText(String.valueOf(button.getIdentity()));
        Throttle mThrottle = button.getThrottle();
        if (mThrottle!=null) {
            idField.setToolTipText(Bundle.getMessage("MaxFunction",mThrottle.getFunctions().length -1));
        }
        fontField.setText(String.valueOf(button.getFont().getSize()));
        imageSize.setText(String.valueOf(button.getButtonImageSize()));
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
            button.setButtonImageSize( Integer.parseInt(imageSize.getText()) );
            button.setVisible(visibleCheckBox.isSelected());
            button.setDisplay(visibleCheckBox.isSelected());
            button.setIconPath(_imageFilePath.getImagePath());
            button.setSelectedIconPath(_imagePressedFilePath.getImagePath());
            button.setDirty(true);
            button.updateLnF();
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
     * @return true if valid, else false.
     */
    private boolean isDataValid() {
        StringBuffer errors = new StringBuffer();
        int errorNumber = 0;
        /* ID >=0 && ID <= 28 */
        
        Throttle mThrottle = button.getThrottle();
        if (mThrottle==null) {
            return false;
        }
        
        try {
            int id = Integer.parseInt(idField.getText());
            if ((id < 0) || id >= mThrottle.getFunctions().length) {
                throw new NumberFormatException("");
            }
        } catch (NumberFormatException ex) {
            errors.append(String.valueOf(++errorNumber)).append(". ");
            errors.append(Bundle.getMessage("ErrorFunctionKeyRange",
                mThrottle.getFunctions().length-1)).append("\n");
        }

        /* font > 0 */
        try {
            int size = Integer.parseInt(fontField.getText());
            if (size < 1) {
                throw new NumberFormatException("");
            }
        } catch (NumberFormatException ex) {
            errors.append(String.valueOf(++errorNumber)).append(". ");
            errors.append( Bundle.getMessage("ErrorFontSize"));
        }

        /* image size > 0 */
        try {
            int size = Integer.parseInt(imageSize.getText());
            if (size < 1) {
                throw new NumberFormatException("");
            }
        } catch (NumberFormatException ex) {
            errors.append(String.valueOf(++errorNumber)).append(". ");
            errors.append( Bundle.getMessage("ErrorImageSize"));
        }        

        if (errorNumber > 0) {
            JOptionPane.showMessageDialog(this, errors,
                Bundle.getMessage("ErrorOnPage"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
