// NameCheckAction.java

package jmri.jmrit.decoderdefn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.util.*;
import org.jdom.*;
import org.jdom.filter.*;

/**
 * Check the names in an XML decoder file against the names.xml definitions
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2007
 * @version	$Revision$
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
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
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
            try {
                Element root = readFile(file);
                if (log.isDebugEnabled()) log.debug("parsing complete");
                
                // check to see if there's a decoder element
                if (root.getChild("decoder")==null) {
                    log.warn("Does not appear to be a decoder file");
                    return;
                }

                Iterator<Element> iter = root.getChild("decoder").getChild("variables")
                                            .getDescendants(new ElementFilter("variable"));

                jmri.jmrit.symbolicprog.NameFile nfile = jmri.jmrit.symbolicprog.NameFile.instance();
                
                String warnings = "";
                
                while (iter.hasNext()) {
                    Element varElement = iter.next();

                    // for each variable, see if can find in names file
                    Attribute labelAttr = varElement.getAttribute("label");
                    String label = null;
                    if (labelAttr!=null) label = labelAttr.getValue();
                    Attribute itemAttr = varElement.getAttribute("item");
                    String item = null;
                    if (itemAttr!=null) item = itemAttr.getValue();
                    if (log.isDebugEnabled()) log.debug("Variable called \""
                                                        +((label!=null)?label:"<none>")+"\" \""
                                                        +((item!=null)?item:"<none>"));
                    if (!(label==null ? false : nfile.checkName(label))
                        && !(item==null ? false : nfile.checkName(item))) {
                        log.warn("Variable not found: label=\""
                                 +((label!=null)?label:"<none>")+"\" item=\""
                                 +((item!=null)?label:"<none>")+"\"");
                        warnings += "Variable not found: label=\""
                            +((label!=null)?label:"<none>")+"\" item=\""
                            +((item!=null)?item:"<none>")+"\"\n";
                    }
                }
                
                if (!warnings.equals(""))
                    JOptionPane.showMessageDialog(_who,warnings);
                else
                    JOptionPane.showMessageDialog(_who,"No mismatched items found");
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(_who,"Error parsing decoder file: "+ex);
                return;
            }
            
        }
        else log.debug("XmlFileCheckAction cancelled in open dialog");
    }
    
    /**
     * Ask SAX to read and verify a file
     */
    Element readFile(File file) throws org.jdom.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
        
        return xf.rootFromFile(file);
        
    }
    
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(NameCheckAction.class.getName());
    
}
