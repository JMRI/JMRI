// ProgCheckAction.java

package jmri.jmrit.tabbedframe;

import jmri.jmrit.XmlFile;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;
import org.jdom.*;
import org.jdom.input.*;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

/** 
 * Check the names in an XML programmer file against the names.xml definitions
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: ProgCheckAction.java,v 1.2 2002-01-13 03:38:31 jacobsen Exp $
 * @see             jmri.jmrit.XmlFile
 */
public class ProgCheckAction extends AbstractAction {
		
	public ProgCheckAction(String s, JPanel who) { 
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

				// check to see if there's a programmer element
				if (root.getChild("programmer")==null) {
					log.warn("Does not appear to be a programmer file");
					return;
				}
				
				// walk the entire tree of elements, saving a reference
				// to all of the "display" elements
				List varList = new ArrayList();
				expandElement(root.getChild("programmer"), varList);
				if (log.isDebugEnabled()) log.debug("found "+varList.size()+" display elements");
				jmri.jmrit.NameFile nfile = jmri.jmrit.NameFile.instance();
				
				String warnings = "";
				
				for (int i=0; i<varList.size(); i++) {
					Element varElement = (Element)(varList.get(i));
					// for each variable, see if can find in names file
					Attribute nameAttr = varElement.getAttribute("item");
					String name = null;
					if (nameAttr!=null) name = nameAttr.getValue();
					if (log.isDebugEnabled()) log.debug("Variable called \""
						+((name!=null)?name:"<none>")+"\"");
					if (!(name==null ? false : nfile.checkName(name))) {
						log.warn("Variable not found: name=\""
							+((name!=null)?name:"<none>")+"\"");
						warnings += "Variable not found: name=\""
							+((name!=null)?name:"<none>")+"\"\n";
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
		else log.info("XmlFileCheckAction cancelled in open dialog");
	}

	/**
	 * Find all of the display elements descending from this element
	 */
	protected void expandElement(Element el, List list) {
		// get the leaves here 
		list.addAll(el.getChildren("display"));
		
		List children = el.getChildren();
		for (int i=0; i<children.size(); i++) 
			expandElement((Element)(children.get(i)), list);
	}
	
	/**
	 * Ask SAX to read and verify a file
	 */
	Element readFile(File file) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
		
		return xf.rootFromFile(file);
		
	}
		
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgCheckAction.class.getName());
		
}
