// AutoSave.java
package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto Save. When enabled will automatically save operation files.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */
public class AutoSave {

    static Thread autoSave = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SC_START_IN_CTOR")
    public AutoSave() {
        synchronized (this) {
            if (Setup.isAutoSaveEnabled() && autoSave == null) {
                autoSave = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveFiles();
                    }
                });
                autoSave.setName("Operations Auto Save"); // NOI18N
                autoSave.start();
            }
        }
    }

    private synchronized void saveFiles() {
        while (true) {
            try {
                wait(60000);	// check every minute
            } catch (InterruptedException e) {
            }
            if (!Setup.isAutoSaveEnabled()) {
                break;
            }
            if (OperationsXml.areFilesDirty()) {
                log.debug("Detected dirty operation files");
                try {
                    wait(60000);	// wait another minute before saving
                } catch (InterruptedException e) {
                }
                if (TrainManager.instance().isAnyTrainBuilding()) {
                    log.debug("Detected trains being built");
                    continue;
                }
                if (OperationsXml.areFilesDirty()) {
                    OperationsXml.save();
                    log.info("Operation files automatically saved");
                }
            }
        }
        autoSave = null;	// done
    }

    private final static Logger log = LoggerFactory.getLogger(AutoSave.class.getName());
}
