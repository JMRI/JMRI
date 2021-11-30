package jmri.jmrix.rps.swing;

import java.io.File;
import javax.annotation.concurrent.GuardedBy;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.PositionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for load/store of RPS setup.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author i18n Egbert Broerse 2021
 */
public class LoadStorePanel extends javax.swing.JPanel {

    public LoadStorePanel() {
        super();

        // file load, store
        JButton b1;
        b1 = new JButton(Bundle.getMessage("ButtonSetDefaults"));
        b1.setToolTipText(Bundle.getMessage("HintSetDefaults"));
        b1.addActionListener(event -> storeDefault());
        add(b1);
        b1 = new JButton(Bundle.getMessage("ButtonStore_"));
        b1.setToolTipText(Bundle.getMessage("HintStore"));
        b1.addActionListener(event -> store());
        add(b1);
        b1 = new JButton(Bundle.getMessage("ButtonLoad_"));
        b1.setToolTipText(Bundle.getMessage("HintLoad"));
        b1.addActionListener(event -> load());
        add(b1);
    }

    @GuardedBy("this")
    JFileChooser fci = jmri.jmrit.XmlFile.userFileChooser();

    public synchronized void load() {
        try {
            // request the filename from an open dialog
            fci.rescanCurrentDirectory();
            int retVal = fci.showOpenDialog(this);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fci.getSelectedFile();
                log.debug("located file {} for load", file);
                // handle the file
                Engine.instance().loadAlignment(file);
            } else {
                log.debug("load cancelled in open dialog");
            }
        } catch (Exception e) {
            log.error("exception during load: ", e);
        }
    }

    public synchronized void store() {
        try {
            // request the filename from an open dialog
            fci.rescanCurrentDirectory();
            int retVal = fci.showSaveDialog(this);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fci.getSelectedFile();
                log.debug("located file {} for store", file);
                // handle the file
                Engine.instance().storeAlignment(file);
            } else {
                log.debug("load cancelled in open dialog");
            }
        } catch (Exception e) {
            log.error("exception during store: ", e);
        }
    }

    public void storeDefault() {
        try {
            File file = new File(PositionFile.defaultFilename());
            log.debug("located file {} for store", file);
            // handle the file
            Engine.instance().storeAlignment(file);
        } catch (Exception e) {
            log.error("exception during storeDefault: ", e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LoadStorePanel.class);

}
