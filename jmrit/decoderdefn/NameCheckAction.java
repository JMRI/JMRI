// NameCheckAction.java

package jmri.jmrit;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.io.*;
import javax.swing.*;
import org.jdom.*;
import org.jdom.input.*;
import com.sun.java.util.collections.List;

/** 
 * Check the names in an XML decoder file against the names.xml definitions
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: NameCheckAction.java,v 1.1 2001-12-30 09:45:29 jacobsen Exp $
 * @see             jmri.jmrit.XmlFile
 */
public class NameCheckAction extends AbstractAction {
		
	public NameCheckAction(String s, JPanel who) { 
		super(s);
		_who = who;
	}
	
	JFileChooser fci = new JFileChooser(" ");

	JPanel _who;
	
    public void actionPerformed(ActionEvent e) {
		// request the filename from an open dialog
		int retVal = fci.showOpenDialog(_who);
		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fci.getSelectedFile();
			if (log.isInfoEnabled()) log.info("located file "+file+" for XML processing");
			// handle the file (later should be outside this thread?)
			try {
				Element root = readFile(file);
				if (log.isInfoEnabled()) log.info("parsing complete");

				// check to see if there's a decoder element
				if (root.getChild("decoder")==null) {
					log.warn("Does not appear to be a decoder file");
					return;
				}
				List varList = root.getChild("decoder").getChild("variables").getChildren("variable");
				if (log.isDebugEnabled()) log.debug("found "+varList.size()+" variables");
				jmri.jmrit.NameFile nfile = jmri.jmrit.NameFile.instance();
				
				String warnings = "";
				
				for (int i=0; i<varList.size(); i++) {
					Element varElement = (Element)(varList.get(i));
					// for each variable, see if can find in names file
					Attribute nameAttr = varElement.getAttribute("name");
					String name = null;
					if (nameAttr!=null) name = nameAttr.getValue();
					Attribute stdNameAttr = varElement.getAttribute("stdName");
					String stdName = null;
					if (stdNameAttr!=null) stdName = stdNameAttr.getValue();
					if (log.isDebugEnabled()) log.debug("Variable called \""
						+((name!=null)?name:"<none>")+"\" \""
						+((stdName!=null)?stdName:"<none>"));
					if (!(name==null ? false : nfile.checkName(name))
						 && !(stdName==null ? false : nfile.checkName(stdName))) {
						log.warn("Variable not found: name=\""
							+((name!=null)?name:"<none>")+"\" stdName=\""
							+((stdName!=null)?stdName:"<none>")+"\"");
						warnings += "Variable not found: name=\""
							+((name!=null)?name:"<none>")+"\" stdName=\""
							+((stdName!=null)?stdName:"<none>")+"\"\n";
					}
				}

				if (!warnings.equals(""))
					JOptionPane.showMessageDialog(_who,warnings);
				else
					JOptionPane.showMessageDialog(_who,"No mismatched names found");

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(_who,"Error parsing decoder file: "+ex);
				return;
			}
			
		}
		else log.info("XmlFileCheckAction cancelled in open dialog");
	}

	/**
	 * Ask SAX to read and verify a file
	 */
	Element readFile(File file) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
		
		return xf.rootFromFile(file);
		
	}
		
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NameCheckAction.class.getName());
		
}
