package jmri.jmrit.operations.trains.excel;

import java.io.File;

import javax.swing.JLabel;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

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
        fileNameTextField.setText(InstanceManager.getDefault(TrainCustomSwitchList.class).getFileName());
        pDirectoryName.add(new JLabel(InstanceManager.getDefault(OperationsManager.class).getFile(InstanceManager.getDefault(TrainCustomSwitchList.class).getDirectoryName()).getPath()));
        setTitle(Bundle.getMessage("MenuItemSetupExcelProgramSwitchList"));

    }

    // Save and Test
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addButton) {
            File f = selectFile(InstanceManager.getDefault(TrainCustomSwitchList.class).getDirectoryName());
            if (f != null) {
                log.debug("User selected file: {}", f.getName());
                fileNameTextField.setText(f.getName());
            }
        }

        InstanceManager.getDefault(TrainCustomSwitchList.class).setFileName(fileNameTextField.getText());

        if (ae.getSource() == testButton) {
            if (InstanceManager.getDefault(TrainCustomSwitchList.class).excelFileExists()) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("DirectoryNameFileName",
                        InstanceManager.getDefault(TrainCustomSwitchList.class).getDirectoryName(), InstanceManager.getDefault(TrainCustomSwitchList.class).getFileName()),
                        Bundle.getMessage("ManifestCreatorFound"), JmriJOptionPane.INFORMATION_MESSAGE);
            } else {
                JmriJOptionPane.showMessageDialog(this, 
                        Bundle.getMessage("LoadDirectoryNameFileName",
                            InstanceManager.getDefault(TrainCustomSwitchList.class).getDirectoryName(), InstanceManager.getDefault(TrainCustomSwitchList.class).getFileName()), Bundle
                        .getMessage("ManifestCreatorNotFound"), JmriJOptionPane.ERROR_MESSAGE);
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupExcelProgramSwitchListFrame.class);
}
