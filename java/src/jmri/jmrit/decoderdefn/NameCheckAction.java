package jmri.jmrit.decoderdefn;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.NameFile;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check the names in an XML decoder file against the names.xml definitions
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007
 * @see jmri.jmrit.XmlFile
 */
public class NameCheckAction extends AbstractAction {

    public NameCheckAction(String s, JPanel who) {
        super(s);
        _who = who;
    }

    JFileChooser fci;

    JPanel _who;

    @SuppressWarnings("unchecked")
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
            if (log.isDebugEnabled()) {
                log.debug("located file " + file + " for XML processing");
            }
            // handle the file (later should be outside this thread?)
            try {
                Element root = readFile(file);
                if (log.isDebugEnabled()) {
                    log.debug("parsing complete");
                }

                // check to see if there's a decoder element
                if (root.getChild("decoder") == null) {
                    log.warn("Does not appear to be a decoder file");
                    return;
                }

                Iterator<Element> iter = root.getChild("decoder").getChild("variables")
                        .getDescendants(new ElementFilter("variable"));

                NameFile nfile = InstanceManager.getDefault(NameFile.class);

                String warnings = "";

                while (iter.hasNext()) {
                    Element varElement = iter.next();

                    // for each variable, see if can find in names file
                    Attribute labelAttr = varElement.getAttribute("label");
                    String label = null;
                    if (labelAttr != null) {
                        label = labelAttr.getValue();
                    }
                    Attribute itemAttr = varElement.getAttribute("item");
                    String item = null;
                    if (itemAttr != null) {
                        item = itemAttr.getValue();
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Variable called \""
                                + ((label != null) ? label : "<none>") + "\" \""
                                + ((item != null) ? item : "<none>"));
                    }
                    if (!(label == null ? false : nfile.checkName(label))
                            && !(item == null ? false : nfile.checkName(item))) {
                        log.warn("Variable not found: label=\""
                                + ((label != null) ? label : "<none>") + "\" item=\""
                                + ((item != null) ? label : "<none>") + "\"");
                        warnings += "Variable not found: label=\""
                                + ((label != null) ? label : "<none>") + "\" item=\""
                                + ((item != null) ? item : "<none>") + "\"\n";
                    }
                }

                if (!warnings.equals("")) {
                    JOptionPane.showMessageDialog(_who, warnings);
                } else {
                    JOptionPane.showMessageDialog(_who, "No mismatched items found");
                }

            } catch (HeadlessException | IOException | JDOMException ex) {
                JOptionPane.showMessageDialog(_who, "Error parsing decoder file: " + ex);
            }

        } else {
            log.debug("XmlFileCheckAction cancelled in open dialog");
        }
    }

    /**
     * Read and verify an XML file.
     *
     * @param file the file to read
     * @return the root element in the file
     * @throws org.jdom2.JDOMException if the file cannot be parsed
     * @throws java.io.IOException     if the file cannot be read
     */
    Element readFile(File file) throws org.jdom2.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract

        return xf.rootFromFile(file);

    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(NameCheckAction.class);

}
