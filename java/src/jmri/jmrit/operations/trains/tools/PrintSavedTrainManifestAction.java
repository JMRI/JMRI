// PrintSavedTrainManifestAction.java
package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.trains.TrainPrintUtilities;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a train's manifest that has been saved.
 *
 * @author Daniel Boudreau Copyright (C) 2015
 * @version $Revision$
 */
public class PrintSavedTrainManifestAction extends AbstractAction {

    public PrintSavedTrainManifestAction(String actionName, boolean preview) {
        super(actionName);
        isPreview = preview;
        setEnabled(Setup.isSaveTrainManifestsEnabled());
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;

    public void actionPerformed(ActionEvent e) {
        File file = getFile();
        if (file == null || !file.exists()) {
            log.debug("User didn't select a file");
            return;
        }
        if (isPreview && Setup.isManifestEditorEnabled()) {
            TrainPrintUtilities.openDesktopEditor(file);
            return;
        }
        String logoURL = Setup.NONE;
// TODO figure out which train manifest is being printed in case there's a custom logo for the train.
//        if (!train.getManifestLogoURL().equals(NONE)) {
//            logoURL = FileUtil.getExternalFilename(train.getManifestLogoURL());
//        } else 
        if (!Setup.getManifestLogoURL().equals(Setup.NONE)) {
            logoURL = FileUtil.getExternalFilename(Setup.getManifestLogoURL());
        }
        String printerName = Location.NONE;
// TODO not sure even if we know which train to send it to the departure printer
//      Location departs = LocationManager.instance().getLocationByName(train.getTrainDepartsName());
//        if (departs != null) {
//            printerName = departs.getDefaultPrinterName();
//        }
        TrainPrintUtilities.printReport(file, file.getName(), isPreview, Setup.getFontName(), false, logoURL,
                printerName, Setup.getManifestOrientation(), Setup.getManifestFontSize());
        return;
    }
    
    // Get file to read from
    protected File getFile() {
        JFileChooser fc = new JFileChooser(TrainManagerXml.instance().getBackupManifestDirectory());
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
    
    private final static Logger log = LoggerFactory.getLogger(PrintSavedTrainManifestAction.class.getName());
    
    private static class FileFilter extends javax.swing.filechooser.FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            if (name.matches(".*\\.txt")) // NOI18N
                return true;
            else
                return false;
        }
        
        public String getDescription() {
            return Bundle.getMessage("TextFiles");
        }
    }
}
        
        
