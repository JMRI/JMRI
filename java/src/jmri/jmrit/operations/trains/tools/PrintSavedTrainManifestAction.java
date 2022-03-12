package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Action to print a train's manifest that has been saved.
 *
 * @author Daniel Boudreau Copyright (C) 2015
 */
public class PrintSavedTrainManifestAction extends AbstractAction implements java.beans.PropertyChangeListener {

    private final static Logger log = LoggerFactory.getLogger(PrintSavedTrainManifestAction.class);

    public PrintSavedTrainManifestAction(boolean isPreview, Train train) {
        super(isPreview ? Bundle.getMessage("MenuItemPreviewSavedManifest")
                : Bundle.getMessage("MenuItemPrintSavedManifest"));
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
        if (_isPreview && Setup.isManifestEditorEnabled()) {
            TrainUtilities.openDesktop(file);
            return;
        }
        String logoURL = Setup.NONE;
        if (_train != null && !_train.getManifestLogoPathName().equals(Train.NONE)) {
            logoURL = FileUtil.getExternalFilename(_train.getManifestLogoPathName());
        } else if (!Setup.getManifestLogoURL().equals(Setup.NONE)) {
            logoURL = FileUtil.getExternalFilename(Setup.getManifestLogoURL());
        }
        String printerName = Location.NONE;
        if (_train != null) {
            Location departs = InstanceManager.getDefault(LocationManager.class)
                    .getLocationByName(_train.getTrainDepartsName());
            if (departs != null) {
                printerName = departs.getDefaultPrinterName();
            }
        }
        TrainPrintUtilities.printReport(file, file.getName(), _isPreview, Setup.getFontName(), false, logoURL,
                printerName, Setup.getManifestOrientation(), Setup.getManifestFontSize(), Setup.isPrintPageHeaderEnabled());
        return;
    }

    // Get file to read from
    protected File getFile() {
        String pathName = InstanceManager.getDefault(TrainManagerXml.class).getBackupManifestDirectoryName();
        if (_train != null) {
            pathName = InstanceManager.getDefault(TrainManagerXml.class)
                    .getBackupManifestDirectoryName(_train.getName());
        }
        JFileChooser fc = new JFileChooser(pathName);
        fc.setFileFilter(new FileNameExtensionFilter(Bundle.getMessage("TextFiles"), "txt"));
        int retVal = fc.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null; // canceled
        }
        return fc.getSelectedFile();
    }
}
