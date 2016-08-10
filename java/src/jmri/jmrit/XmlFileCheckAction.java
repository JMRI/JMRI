package jmri.jmrit;

import jmri.util.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make sure an XML file is readable, without doing a Schema validation.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2005, 2007
 * @see jmri.jmrit.XmlFile
 * @see jmri.jmrit.XmlFileValidateAction
 */
public class XmlFileCheckAction extends jmri.util.swing.JmriAbstractAction {

    public XmlFileCheckAction(String s, Component who) {
        super(s);
        _who = who;
    }

    public XmlFileCheckAction(String s, WindowInterface wi) {
        this(s, wi!=null ? wi.getFrame() : null);
    }

    JFileChooser fci;

    Component _who;

    public void actionPerformed(ActionEvent e) {
        if (fci == null) {
            fci = jmri.jmrit.XmlFile.userFileChooser("XML files", "xml");
        }
        // request the filename from an open dialog
        fci.rescanCurrentDirectory();
        int retVal = fci.showOpenDialog(_who);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("located file " + file + " for XML processing");
            }
            // handle the file (later should be outside this thread?)
            boolean original = XmlFile.verify;
            try {
                XmlFile.verify = false;
                readFile(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(_who, "Error: " + ex);
                return;
            } finally {
                XmlFile.verify = original;
            }
            JOptionPane.showMessageDialog(_who, "OK");
            if (log.isDebugEnabled()) {
                log.debug("parsing complete");
            }

        } else {
            log.info("XmlFileCheckAction cancelled in open dialog");
        }
    }

    /**
     * Ask SAX to read and verify a file
     */
    void readFile(File file) throws org.jdom2.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract

        xf.rootFromFile(file);

    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(XmlFileCheckAction.class.getName());
}
