package jmri.jmrit.operations.trains.excel;

import java.io.File;

import javax.swing.JLabel;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of the file name of an Excel program used to generate
 * custom manifests.
 *
 * @author Dan Boudreau Copyright (C) 2014, 2023
 */
public class SetupExcelProgramManifestFrame extends SetupExcelProgramFrame {

    TrainCustomManifest tcm = InstanceManager.getDefault(TrainCustomManifest.class);

    @Override
    public void initComponents() {
        super.initComponents();

        generateCheckBox.setText(rb.getString("GenerateCsvManifest"));
        generateCheckBox.setSelected(Setup.isGenerateCsvManifestEnabled());
        fileNameTextField.setText(tcm.getFileName());
        pDirectoryName.add(new JLabel(tcm.getDirectoryPathName()));
    }

    // Add, Test and Save buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addButton) {
            File f = selectFile(tcm.getDirectoryName());
            if (f != null) {
                log.debug("User selected file: {}", f.getName());
                fileNameTextField.setText(f.getName());
            }
        }

        tcm.setFileName(fileNameTextField.getText());

        if (ae.getSource() == testButton) {
            if (tcm.doesExcelFileExist()) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("DirectoryNameFileName",
                        tcm.getDirectoryName(), tcm.getFileName()),
                        Bundle.getMessage("ManifestCreatorFound"), JmriJOptionPane.INFORMATION_MESSAGE);
            } else {
                JmriJOptionPane.showMessageDialog(this, 
                        Bundle.getMessage("LoadDirectoryNameFileName",
                                tcm.getDirectoryPathName(),
                                tcm.getFileName()),
                        Bundle.getMessage("ManifestCreatorNotFound"), JmriJOptionPane.ERROR_MESSAGE);
            }
        }
        if (ae.getSource() == saveButton) {
            log.debug("Save button activated");
            Setup.setGenerateCsvManifestEnabled(generateCheckBox.isSelected());
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetupExcelProgramManifestFrame.class);
}
