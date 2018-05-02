//SetupExcelProgramManifestFrame.java
package jmri.jmrit.operations.trains.excel;

import java.io.File;
import java.text.MessageFormat;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of the file name of an Excel program used to generate
 * custom manifests.
 *
 * @author Dan Boudreau Copyright (C) 2014
 * 
 */
public class SetupExcelProgramManifestFrame extends SetupExcelProgramFrame {

    @Override
    public void initComponents() {
        super.initComponents();

        generateCheckBox.setText(rb.getString("GenerateCsvManifest"));
        generateCheckBox.setSelected(Setup.isGenerateCsvManifestEnabled());
        fileNameTextField.setText(InstanceManager.getDefault(TrainCustomManifest.class).getFileName());
        pDirectoryName.add(new JLabel(OperationsManager.getInstance().getFile(InstanceManager.getDefault(TrainCustomManifest.class).getDirectoryName()).getPath()));
    }

    // Save and Test
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addButton) {
            File f = selectFile(InstanceManager.getDefault(TrainCustomManifest.class).getDirectoryName());
            if (f != null) {
                log.debug("User selected file: {}", f.getName());
                fileNameTextField.setText(f.getName());
            }
        }

        InstanceManager.getDefault(TrainCustomManifest.class).setFileName(fileNameTextField.getText());

        if (ae.getSource() == testButton) {
            if (InstanceManager.getDefault(TrainCustomManifest.class).excelFileExists()) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("DirectoryNameFileName"),
                        new Object[]{InstanceManager.getDefault(TrainCustomManifest.class).getDirectoryName(), InstanceManager.getDefault(TrainCustomManifest.class).getFileName()}),
                        Bundle.getMessage("ManifestCreatorFound"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, MessageFormat.format(
                        Bundle.getMessage("LoadDirectoryNameFileName"), new Object[]{
                            InstanceManager.getDefault(TrainCustomManifest.class).getDirectoryName(), InstanceManager.getDefault(TrainCustomManifest.class).getFileName()}), Bundle
                        .getMessage("ManifestCreatorNotFound"), JOptionPane.ERROR_MESSAGE);
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

    private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramManifestFrame.class);
}
