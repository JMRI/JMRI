// XmlFileCheckAction.java

package jmri.jmrit;

import org.apache.log4j.Logger;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/**
 * Make sure an XML file is readable, without doing a DTD validation.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2005, 2007
 * @version	$Revision$
 * @see         jmri.jmrit.XmlFile
 * @see         jmri.jmrit.XmlFileValidateAction
 */
public class XmlFileCheckAction extends AbstractAction {

    public XmlFileCheckAction(String s, JPanel who) {
        super(s);
        _who = who;
    }

    JFileChooser fci;

    JPanel _who;

    public void actionPerformed(ActionEvent e) {
        if (fci==null) {
            fci = jmri.jmrit.XmlFile.userFileChooser("XML files", "xml");
        }
        // request the filename from an open dialog
        fci.rescanCurrentDirectory();
        int retVal = fci.showOpenDialog(_who);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            if (log.isDebugEnabled()) log.debug("located file "+file+" for XML processing");
            // handle the file (later should be outside this thread?)
            boolean original = XmlFile.verify;
            try {
                XmlFile.verify = false;
                readFile(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(_who,"Error: "+ex);
                return;
            } finally {
                XmlFile.verify = original;
            }
            JOptionPane.showMessageDialog(_who,"OK");
            if (log.isDebugEnabled()) log.debug("parsing complete");

        }
        else log.info("XmlFileCheckAction cancelled in open dialog");
    }

    /**
     * Ask SAX to read and verify a file
     */
    void readFile(File file) throws org.jdom.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract

        xf.rootFromFile(file);

    }

    // initialize logging
    static Logger log = Logger.getLogger(XmlFileCheckAction.class.getName());
}
