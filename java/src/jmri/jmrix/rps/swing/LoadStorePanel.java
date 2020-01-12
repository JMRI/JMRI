package jmri.jmrix.rps.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.PositionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for load/store of RPS setup.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class LoadStorePanel extends javax.swing.JPanel {

    public LoadStorePanel() {
        super();

        // file load, store
        JButton b1;
        b1 = new JButton("Set Defaults");
        b1.setToolTipText("Store new default values");
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                storeDefault();
            }
        });
        add(b1);
        b1 = new JButton("Store...");
        b1.setToolTipText("Store in a user-selected file");
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                store();
            }
        });
        add(b1);
        b1 = new JButton("Load...");
        b1.setToolTipText("Load from a user-selected file");
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                load();
            }
        });
        add(b1);
    }

    JFileChooser fci = jmri.jmrit.XmlFile.userFileChooser();

    public void load() {
        try {
            // request the filename from an open dialog
            fci.rescanCurrentDirectory();
            int retVal = fci.showOpenDialog(this);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fci.getSelectedFile();
                if (log.isInfoEnabled()) {
                    log.info("located file " + file + " for load");
                }
                // handle the file
                Engine.instance().loadAlignment(file);
            } else {
                log.info("load cancelled in open dialog");
            }
        } catch (Exception e) {
            log.error("exception during load: " + e);
        }
    }

    public void store() {
        try {
            // request the filename from an open dialog
            fci.rescanCurrentDirectory();
            int retVal = fci.showSaveDialog(this);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fci.getSelectedFile();
                if (log.isInfoEnabled()) {
                    log.info("located file " + file + " for store");
                }
                // handle the file
                Engine.instance().storeAlignment(file);
            } else {
                log.info("load cancelled in open dialog");
            }
        } catch (Exception e) {
            log.error("exception during store: " + e);
        }
    }

    public void storeDefault() {
        try {
            File file = new File(PositionFile.defaultFilename());
            if (log.isInfoEnabled()) {
                log.info("located file " + file + " for store");
            }
            // handle the file
            Engine.instance().storeAlignment(file);
        } catch (Exception e) {
            log.error("exception during storeDefault: " + e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LoadStorePanel.class);

}
