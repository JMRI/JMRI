// XmlFileValidateAction.java

package jmri.jmrit;

import org.apache.log4j.Logger;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 * Make sure an XML file is readable, and validates OK
 *
 * @author	Bob Jacobsen   Copyright (C) 2005, 2007
 * @version	$Revision$
 * @see         jmri.jmrit.XmlFile
 * @see         jmri.jmrit.XmlFileCheckAction
 */
public class XmlFileValidateAction extends AbstractAction {

    public XmlFileValidateAction(String s, JPanel who) {
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
                XmlFile.verify = true;
                readFile(file);
            } catch (Exception ex) {
                // because of XInclude, we're doing this
                // again to validate the entire file
                // without losing the error message
                XmlFile.verify = false;
                Document doc;
                try {
                    InputStream stream = new BufferedInputStream(new FileInputStream(file));
                    SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser",false);
                    builder.setEntityResolver(new jmri.util.JmriLocalEntityResolver());
                    builder.setFeature("http://apache.org/xml/features/xinclude", true);
                    builder.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
                    builder.setFeature("http://apache.org/xml/features/validation/schema", false);
                    builder.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
                    builder.setFeature("http://xml.org/sax/features/namespaces", true);
                    doc = builder.build(new BufferedInputStream(stream));
                } catch (Exception ex2) {
                    JOptionPane.showMessageDialog(_who,"Err(1): "+ex2);
                    return;
                }
                XMLOutputter outputter = new XMLOutputter();
                StringWriter out = new StringWriter();
                try {
                    outputter.output(doc, out); 
                } catch (Exception ex2) {
                    JOptionPane.showMessageDialog(_who,"Err(4): "+ex2);
                    return;
                }
                StringReader input = new StringReader(new String(out.getBuffer()));
                SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser",true);
                builder.setEntityResolver(new jmri.util.JmriLocalEntityResolver());
                builder.setFeature("http://apache.org/xml/features/xinclude", true);
                builder.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
                builder.setFeature("http://apache.org/xml/features/validation/schema", true);
                builder.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
                builder.setFeature("http://xml.org/sax/features/namespaces", true);
                try {
                    XmlFile.verify = true;
                    builder.build(input).getRootElement();
                } catch (Exception ex2) {
                    JOptionPane.showMessageDialog(_who,"Err(2): "+ex2);
                    return;
                }
                
                JOptionPane.showMessageDialog(_who,"Err(3): "+ex);
                return;
            } finally {
                XmlFile.verify = original;
            }
            JOptionPane.showMessageDialog(_who,"OK");
            if (log.isDebugEnabled()) log.debug("parsing complete");

        }
        else log.debug("XmlFileValidateAction cancelled in open dialog");
    }

    /**
     * Ask SAX to read and verify a file
     */
    void readFile(File file) throws org.jdom.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract

        xf.rootFromFile(file);

    }

    // initialize logging
    static Logger log = Logger.getLogger(XmlFileValidateAction.class.getName());
}
