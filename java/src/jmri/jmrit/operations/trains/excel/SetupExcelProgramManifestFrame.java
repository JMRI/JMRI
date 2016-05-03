//SetupExcelProgramManifestFrame.java
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
 * custom manifests.
 *
 * @author Dan Boudreau Copyright (C) 2014
 * @version $Revision: 22249 $
 */
public class SetupExcelProgramManifestFrame extends SetupExcelProgramFrame {

    @Override
    public void initComponents() {
        super.initComponents();

        generateCheckBox.setText(rb.getString("GenerateCsvManifest"));
        generateCheckBox.setSelected(Setup.isGenerateCsvManifestEnabled());
        fileNameTextField.setText(TrainCustomManifest.getFileName());
        pDirectoryName.add(new JLabel(OperationsManager.getInstance().getFile(TrainCustomManifest.getDirectoryName()).getPath()));
    }

    // Save and Test
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addButton) {
            File f = selectFile(TrainCustomManifest.getDirectoryName());
            if (f != null) {
                log.debug("User selected file: {}", f.getName());
                fileNameTextField.setText(f.getName());
            }
        }

        TrainCustomManifest.setFileName(fileNameTextField.getText());

        if (ae.getSource() == testButton) {
            if (TrainCustomManifest.manifestCreatorFileExists()) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("DirectoryNameFileName"),
                        new Object[]{TrainCustomManifest.getDirectoryName(), TrainCustomManifest.getFileName()}),
                        Bundle.getMessage("ManifestCreatorFound"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, MessageFormat.format(
                        Bundle.getMessage("LoadDirectoryNameFileName"), new Object[]{
                            TrainCustomManifest.getDirectoryName(), TrainCustomManifest.getFileName()}), Bundle
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

    private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramManifestFrame.class.getName());
}
