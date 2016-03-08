// ProgCheckAction.java
package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrit.XmlFile;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check the names in an XML programmer file against the names.xml definitions
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2007
 * @version	$Revision$
 * @see jmri.jmrit.XmlFile
 */
public class ProgCheckAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 2266366526574569234L;

    public ProgCheckAction(String s, JPanel who) {
        super(s);
        _who = who;
    }

    JFileChooser fci;

    JPanel _who;

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

            warnMissingNames(file);

            // as ugly special case, do reverse check for Comprehensive programmer
            if (file.getName().toLowerCase().endsWith("comprehensive.xml")) {
                warnIncompleteComprehensive(file);
            }
        } else {
            log.info("XmlFileCheckAction cancelled in open dialog");
        }
    }

    /**
     * Find all of the display elements descending from this element
     */
    static protected void expandElement(Element el, List<Element> list) {
        // get the leaves here
        list.addAll(el.getChildren("display"));

        List<Element> children = el.getChildren();
        for (int i = 0; i < children.size(); i++) {
            expandElement(children.get(i), list);
        }
    }

    /**
     * Check for names in programer that are not in names.xml
     */
    void warnMissingNames(File file) {
        String result = checkMissingNames(file);
        if (result.equals("")) {
            JOptionPane.showMessageDialog(_who, "OK, all variables in file are known");
        } else {
            JOptionPane.showMessageDialog(_who, result);
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    static String checkMissingNames(File file) {
        try {
            Element root = readFile(file);
            if (log.isDebugEnabled()) {
                log.debug("parsing complete");
            }

            // check to see if there's a programmer element
            if (root.getChild("programmer") == null) {
                log.warn("Does not appear to be a programmer file");
                return "Does not appear to be a programmer file";
            }

            // walk the entire tree of elements, saving a reference
            // to all of the "display" elements
            List<Element> varList = new ArrayList<Element>();
            expandElement(root.getChild("programmer"), varList);
            if (log.isDebugEnabled()) {
                log.debug("found " + varList.size() + " display elements");
            }
            jmri.jmrit.symbolicprog.NameFile nfile = jmri.jmrit.symbolicprog.NameFile.instance();

            String warnings = "";

            for (int i = 0; i < varList.size(); i++) {
                Element varElement = (varList.get(i));
                // for each variable, see if can find in names file
                Attribute nameAttr = varElement.getAttribute("item");
                String name = null;
                if (nameAttr != null) {
                    name = nameAttr.getValue();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Variable called \""
                            + ((name != null) ? name : "<none>") + "\"");
                }
                if (!(name == null ? false : nfile.checkName(name))) {
                    log.warn("Variable not found in name list: name=\""
                            + ((name != null) ? name : "<none>") + "\"");
                    warnings += "Variable not found in name list: name=\""
                            + ((name != null) ? name : "<none>") + "\"\n";
                }
            }

            if (!warnings.equals("")) {
                return "Names missing from Comprehensive.xml\n" + warnings;
            } else {
                return "";
            }

        } catch (Exception ex) {
            return "Error parsing programmer file: " + ex;
        }
    }

    /**
     * Check for names in names.xml that are not in file
     */
    void warnIncompleteComprehensive(File file) {
        String result = checkIncompleteComprehensive(file);
        if (result.equals("")) {
            JOptionPane.showMessageDialog(_who, "OK, Comprehensive.xml is complete");
        } else {
            JOptionPane.showMessageDialog(_who, result);
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    static String checkIncompleteComprehensive(File file) {
        // handle the file (later should be outside this thread?)
        try {
            Element root = readFile(file);
            if (log.isDebugEnabled()) {
                log.debug("parsing complete");
            }

            // check to see if there's a programmer element
            if (root.getChild("programmer") == null) {
                log.warn("Does not appear to be a programmer file");
                return "Does not appear to be a programmer file";
            }

            // walk the entire tree of elements, saving a reference
            // to all of the "display" elements
            List<Element> varList = new ArrayList<Element>();
            expandElement(root.getChild("programmer"), varList);
            if (log.isDebugEnabled()) {
                log.debug("found " + varList.size() + " display elements");
            }
            jmri.jmrit.symbolicprog.NameFile nfile = jmri.jmrit.symbolicprog.NameFile.instance();

            String warnings = "";

            // for each item in names, see if found in this file
            for (String s : nfile.names()) {
                if (functionMapName(s)) {
                    continue;
                }
                boolean found = false;
                for (int i = 0; i < varList.size(); i++) {
                    Element varElement = varList.get(i);
                    // for each variable, see if can find in names file
                    Attribute nameAttr = varElement.getAttribute("item");
                    String name = null;
                    if (nameAttr != null) {
                        name = nameAttr.getValue();
                    }
                    // now check
                    if (name != null && name.equals(s)) {
                        found = true;
                    }
                }
                if (!found) {
                    log.warn("Variable not in Comprehensive: name=\""
                            + s + "\"");
                    warnings += "Variable not in Comprehensive: name=\""
                            + s + "\"\n";
                }
            }

            return warnings;
        } catch (Exception ex) {
            return "Error parsing programmer file: " + ex;
        }
    }

    /**
     * Check if the name is a function name, e.g. "F5 controls output 8" or
     * "FL(f) controls output 14"
     */
    static boolean functionMapName(String name) {
        if (numericPattern == null) {
            numericPattern = Pattern.compile(numericRegex);
        }
        if (ffPattern == null) {
            ffPattern = Pattern.compile(ffRegex);
        }
        if (frPattern == null) {
            frPattern = Pattern.compile(frRegex);
        }

        Matcher matcher = numericPattern.matcher(name);
        if (matcher.matches()) {
            return true;
        }
        matcher = ffPattern.matcher(name);
        if (matcher.matches()) {
            return true;
        }
        matcher = frPattern.matcher(name);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }
    static final String numericRegex = "^F(\\d++) controls output (\\d++)$";
    static Pattern numericPattern;
    static final String ffRegex = "^FL\\(f\\) controls output (\\d++)$";
    static Pattern ffPattern;
    static final String frRegex = "^FL\\(r\\) controls output (\\d++)$";
    static Pattern frPattern;

    /**
     * Ask SAX to read and verify a file
     */
    static Element readFile(File file) throws org.jdom2.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract

        return xf.rootFromFile(file);

    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ProgCheckAction.class.getName());

}
