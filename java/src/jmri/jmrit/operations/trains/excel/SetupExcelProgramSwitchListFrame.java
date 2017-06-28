package jmri.jmrit.operations.trains.excel;

import java.io.File;
import java.text.MessageFormat;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of the file name of an Excel program used to generate
 * switch lists.
 *
 * @author Dan Boudreau Copyright (C) 2014
 * 
 */
public class SetupExcelProgramSwitchListFrame extends SetupExcelProgramFrame {

    @Override
    public void initComponents() {
        super.initComponents();

        generateCheckBox.setText(rb.getString("GenerateCsvSwitchList"));
        generateCheckBox.setSelected(Setup.isGenerateCsvSwitchListEnabled());
        fileNameTextField.setText(TrainCustomSwitchList.instance().getFileName());
        pDirectoryName.add(new JLabel(OperationsManager.getInstance().getFile(TrainCustomSwitchList.instance().getDirectoryName()).getPath()));
        setTitle(Bundle.getMessage("MenuItemSetupExcelProgramSwitchList"));

    }

    // Save and Test
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addButton) {
            File f = selectFile(TrainCustomSwitchList.instance().getDirectoryName());
            if (f != null) {
                log.debug("User selected file: {}", f.getName());
                fileNameTextField.setText(f.getName());
            }
        }

        TrainCustomSwitchList.instance().setFileName(fileNameTextField.getText());

        if (ae.getSource() == testButton) {
            if (TrainCustomSwitchList.instance().excelFileExists()) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("DirectoryNameFileName"),
                        new Object[]{TrainCustomSwitchList.instance().getDirectoryName(), TrainCustomSwitchList.instance().getFileName()}),
                        Bundle.getMessage("ManifestCreatorFound"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, MessageFormat.format(
                        Bundle.getMessage("LoadDirectoryNameFileName"), new Object[]{
                            TrainCustomSwitchList.instance().getDirectoryName(), TrainCustomSwitchList.instance().getFileName()}), Bundle
                        .getMessage("ManifestCreatorNotFound"), JOptionPane.ERROR_MESSAGE);
            }
        }
        if (ae.getSource() == saveButton) {
            log.debug("Save button activated");
            Setup.setGenerateCsvSwitchListEnabled(generateCheckBox.isSelected());
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramSwitchListFrame.class.getName());
}
