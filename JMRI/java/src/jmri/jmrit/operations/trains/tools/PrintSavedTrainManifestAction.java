package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.trains.TrainPrintUtilities;
import jmri.jmrit.operations.trains.TrainUtilities;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a train's manifest that has been saved.
 *
 * @author Daniel Boudreau Copyright (C) 2015
 */
public class PrintSavedTrainManifestAction extends AbstractAction implements java.beans.PropertyChangeListener {

    private final static Logger log = LoggerFactory.getLogger(PrintSavedTrainManifestAction.class);

    public PrintSavedTrainManifestAction(String actionName, boolean isPreview, Train train) {
        super(actionName);
        _isPreview = isPreview;
        _train = train;
        setEnabled(Setup.isSaveTrainManifestsEnabled());
        Setup.addPropertyChangeListener(this);
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean _isPreview;
    Train _train;

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
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
        if (_isPreview && Setup.isManifestEditorEnabled()) {
            TrainUtilities.openDesktop(file);
            return;
        }
        String logoURL = Setup.NONE;
        if (_train != null && !_train.getManifestLogoURL().equals(Train.NONE)) {
            logoURL = FileUtil.getExternalFilename(_train.getManifestLogoURL());
        } else if (!Setup.getManifestLogoURL().equals(Setup.NONE)) {
            logoURL = FileUtil.getExternalFilename(Setup.getManifestLogoURL());
        }
        String printerName = Location.NONE;
        if (_train != null) {
            Location departs = InstanceManager.getDefault(LocationManager.class).getLocationByName(_train.getTrainDepartsName());
            if (departs != null) {
                printerName = departs.getDefaultPrinterName();
            }
        }
        TrainPrintUtilities.printReport(file, file.getName(), _isPreview, Setup.getFontName(), false, logoURL,
                printerName, Setup.getManifestOrientation(), Setup.getManifestFontSize());
        return;
    }

    // Get file to read from
    protected File getFile() {
        String pathName = InstanceManager.getDefault(TrainManagerXml.class).getBackupManifestDirectoryName();
        if (_train != null) {
            pathName = InstanceManager.getDefault(TrainManagerXml.class).getBackupManifestDirectoryName(_train.getName());
        }
        JFileChooser fc = new JFileChooser(pathName);
        fc.addChoosableFileFilter(new FileFilter());
        int retVal = fc.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null; // canceled
        }
        if (fc.getSelectedFile() == null) {
            return null; // canceled
        }
        File file = fc.getSelectedFile();
        return file;
    }

    private static class FileFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            if (name.matches(".*\\.txt")) // NOI18N
                return true;
            else
                return false;
        }

        @Override
        public String getDescription() {
            return Bundle.getMessage("TextFiles");
        }
    }
}
