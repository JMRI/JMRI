package jmri.jmrit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make sure an XML file is readable, without doing a DTD or Schema validation.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2005, 2007
 * @see jmri.jmrit.XmlFile
 * @see jmri.jmrit.XmlFileValidateAction
 */
public class XmlFileCheckAction extends JmriAbstractAction {

    public XmlFileCheckAction(String s, Component who) {
        super(s);
        _who = who;
    }

    public XmlFileCheckAction(String s, WindowInterface wi) {
        this(s, wi != null ? wi.getFrame() : null);
    }

    JFileChooser fci;

    Component _who;
    
    XmlFile xmlfile = new XmlFile() {};   // odd syntax is due to XmlFile being abstract

    @Override
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
            log.debug("located file {} for XML processing", file);
            // handle the file (later should be outside this thread?)
            try {
                xmlfile.setValidate(XmlFile.Validate.None);
                readFile(file);
                JOptionPane.showMessageDialog(_who, "OK");
            } catch (IOException | JDOMException ex) {
                JOptionPane.showMessageDialog(_who, "Error: " + ex);
            }
            log.debug("parsing complete");

        } else {
            log.info("XmlFileCheckAction cancelled in open dialog");
        }
    }

    /**
     * Read and verify a file is XML.
     *
     * @param file the file to read
     * @throws org.jdom2.JDOMException if file is not XML
     * @throws java.io.IOException     if unable to read file
     */
    void readFile(File file) throws JDOMException, IOException {
        xmlfile.rootFromFile(file);

    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(XmlFileCheckAction.class);
}
