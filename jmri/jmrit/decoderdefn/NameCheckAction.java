// NameCheckAction.java

package jmri.jmrit.decoderdefn;

import jmri.jmrit.XmlFile;

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
 * @version			$Revision: 1.2 $
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
				List varList = root.getChild("decoder").getChild("variables").getChildren("variable");
				if (log.isDebugEnabled()) log.debug("found "+varList.size()+" variables");
				jmri.jmrit.symbolicprog.NameFile nfile = jmri.jmrit.symbolicprog.NameFile.instance();

				String warnings = "";

				for (int i=0; i<varList.size(); i++) {
					Element varElement = (Element)(varList.get(i));
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
	Element readFile(File file) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract

		return xf.rootFromFile(file);

	}


	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NameCheckAction.class.getName());

}
