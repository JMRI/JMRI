package jmri.jmrit.operations.trains;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Train file utilities
 *
 * @author Daniel Boudreau (C) 2010
 *
 *
 */
public class TrainUtilities {

    /**
     * This method uses Desktop which is supported in Java 1.6.
     * @param file The File to open.
     */
    public static void openDesktop(File file) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            log.warn("desktop not supported");
            return;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
            log.warn("desktop open not supported");
            return;
        }
        try {
            desktop.open(file);
        } catch (IOException e) {
            log.error("unable to open {} in desktop application", file, e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainUtilities.class);
}
