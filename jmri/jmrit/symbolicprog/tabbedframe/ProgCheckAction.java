// ProgCheckAction.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.util.*;
import org.jdom.*;

/**
 * Check the names in an XML programmer file against the names.xml definitions
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2007
 * @version	$Revision: 1.8 $
 * @see         jmri.jmrit.XmlFile
 */
public class ProgCheckAction extends AbstractAction {

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
            if (log.isDebugEnabled()) log.debug("located file "+file+" for XML processing");
            
            checkMissingNames(file);
            
            // as ugly special case, do reverse check for Comprehensive programmer
            if (file.getName().toLowerCase().endsWith("comprehensive.xml"))
                checkIncompleteComprehensive(file);
        }
        else log.info("XmlFileCheckAction cancelled in open dialog");
    }
    
    /**
     * Find all of the display elements descending from this element
     */
    protected void expandElement(Element el, List<Element> list) {
        // get the leaves here
        list.addAll((java.util.Collection<Element>)el.getChildren("display"));
        
        List<Element> children = (List<Element>)el.getChildren();
        for (int i=0; i<children.size(); i++)
            expandElement(children.get(i), list);
    }
    
    /**
     * Check for names in programer that are not in names.xml
     */
    void checkMissingNames(File file) {
        // handle the file (later should be outside this thread?)
        try {
            Element root = readFile(file);
            if (log.isDebugEnabled()) log.debug("parsing complete");
            
            // check to see if there's a programmer element
            if (root.getChild("programmer")==null) {
                log.warn("Does not appear to be a programmer file");
                return;
            }
            
            // walk the entire tree of elements, saving a reference
            // to all of the "display" elements
            List<Element> varList = new ArrayList<Element>();
            expandElement(root.getChild("programmer"), varList);
            if (log.isDebugEnabled()) log.debug("found "+varList.size()+" display elements");
            jmri.jmrit.symbolicprog.NameFile nfile = jmri.jmrit.symbolicprog.NameFile.instance();
            
            String warnings = "";
            
            for (int i=0; i<varList.size(); i++) {
                Element varElement = (varList.get(i));
                // for each variable, see if can find in names file
                Attribute nameAttr = varElement.getAttribute("item");
                String name = null;
                if (nameAttr!=null) name = nameAttr.getValue();
                if (log.isDebugEnabled()) log.debug("Variable called \""
                                                    +((name!=null)?name:"<none>")+"\"");
                if (!(name==null ? false : nfile.checkName(name))) {
                    log.warn("Variable not found in name list: name=\""
                             +((name!=null)?name:"<none>")+"\"");
                    warnings += "Variable not found in name list: name=\""
                        +((name!=null)?name:"<none>")+"\"\n";
                }
            }
            
            if (!warnings.equals(""))
                JOptionPane.showMessageDialog(_who,"Names missing from Comprehensive.xml\n"+warnings);
            else
                JOptionPane.showMessageDialog(_who,"No missing names found");
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(_who,"Error parsing programmer file: "+ex);
            return;
        }
    }
    
    /**
     * Check for names in names.xml that are not in file
     */
    void checkIncompleteComprehensive(File file) {
        // handle the file (later should be outside this thread?)
        try {
            Element root = readFile(file);
            if (log.isDebugEnabled()) log.debug("parsing complete");
            
            // check to see if there's a programmer element
            if (root.getChild("programmer")==null) {
                log.warn("Does not appear to be a programmer file");
                return;
            }
            
            // walk the entire tree of elements, saving a reference
            // to all of the "display" elements
            List<Element> varList = new ArrayList<Element>();
            expandElement(root.getChild("programmer"), varList);
            if (log.isDebugEnabled()) log.debug("found "+varList.size()+" display elements");
            jmri.jmrit.symbolicprog.NameFile nfile = jmri.jmrit.symbolicprog.NameFile.instance();
            
            String warnings = "";
            
            // for each item in names, see if found in this file
            for (String s : nfile.names()) {
                boolean found = false;
                for (int i=0; i<varList.size(); i++) {
                    Element varElement = (Element)(varList.get(i));
                    // for each variable, see if can find in names file
                    Attribute nameAttr = varElement.getAttribute("item");
                    String name = null;
                    if (nameAttr!=null) name = nameAttr.getValue();
                    // now check
                    if (name.equals(s)){
                        found = true;
                    }                                  
                }
                if (!found) {
                    log.warn("Variable not in Comprehensive: name=\""
                             +s+"\"");
                    warnings += "Variable not in Comprehensive: name=\""
                        +s+"\"\n";
                }
            }
            
            
            if (!warnings.equals(""))
                JOptionPane.showMessageDialog(_who,warnings);
            else
                JOptionPane.showMessageDialog(_who,"No mismatched names found");
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(_who,"Error parsing programmer file: "+ex);
            return;
        }
    }
    
    /**
     * Ask SAX to read and verify a file
     */
    Element readFile(File file) throws org.jdom.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
        
        return xf.rootFromFile(file);
        
    }
    
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgCheckAction.class.getName());
    
}
