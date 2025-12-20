package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;

/**
 * Action to print a train's build report that has been saved.
 *
 * @author Daniel Boudreau Copyright (C) 2022
 */
public class PrintSavedBuildReportAction extends AbstractAction implements java.beans.PropertyChangeListener {

    private final static Logger log = LoggerFactory.getLogger(PrintSavedBuildReportAction.class);

    public PrintSavedBuildReportAction(boolean isPreview, Train train) {
        super(isPreview ? Bundle.getMessage("MenuItemPreviewSavedBuildReport")
                : Bundle.getMessage("MenuItemPrintSavedBuildReport"));
        _isPreview = isPreview;
        _train = train;
        setEnabled(Setup.isSaveTrainManifestsEnabled());
        Setup.getDefault().addPropertyChangeListener(this);
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean _isPreview;
    Train _train;

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(Setup.SAVE_TRAIN_MANIFEST_PROPERTY_CHANGE)) {
            setEnabled(Setup.isSaveTrainManifestsEnabled());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File file = getFile();
        if (file == null || !file.exists()) {
            log.debug("User didn't select a file");
            return;
        }
        if (_isPreview && Setup.isBuildReportEditorEnabled()) {
            TrainPrintBuildReport.editReport(file, _train.getName());
            return;
        }
        TrainPrintBuildReport.printReport(file,
                Bundle.getMessage("buildReport", _train.getDescription()), _isPreview);
        return;
    }

    // Get file to read from
    protected File getFile() {
        String pathName = InstanceManager.getDefault(TrainManagerXml.class).getBackupBuildStatusDirectoryName();
        if (_train != null) {
            pathName = InstanceManager.getDefault(TrainManagerXml.class)
                    .getBackupBuildStatusDirectoryName(_train.getName());
        }
        JFileChooser fc = new jmri.util.swing.JmriJFileChooser(pathName);
        fc.setFileFilter(new FileNameExtensionFilter(Bundle.getMessage("TextFiles"), "txt"));
        int retVal = fc.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null; // canceled
        }
        return fc.getSelectedFile();
    }
}
