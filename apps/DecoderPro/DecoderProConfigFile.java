// DecoderProConfigFile.java

package jmri.apps;

import com.sun.java.util.collections.List;
import java.io.*;
import java.util.Date;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * Represents and manipulates the preferences information for the
 * DecoderPro application. Works with the DecoderProConfigFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		 	$Id: DecoderProConfigFile.java,v 1.1.1.1 2001-12-02 05:51:21 jacobsen Exp $
 * @see jmri.apps.DecodeProConfigFrame
 */
public class DecoderProConfigFile extends XmlFile {
	
	/**
	 * Convert to a cannonical text form for ComboBoxes, etc
	 */
	public String titleString() { 
		return "no title form yet";
	}

	public void writeFile(File file, CvTableModel cvModel, VariableTableModel variableModel, RosterEntry r) {
		try {
			// This is taken in large part from "Java and XML" page 368 

			// create root element
			Element root = new Element("locomotive-config");
			Document doc = new Document(root);
			doc.setDocType(new DocType("locomotive-config","locomotive-config.dtd"));
		
			// add top-level elements
			Element values;
			root.addContent(new Element("locomotive")		// locomotive values are first item
					.addAttribute("id", r.getId())
					.addAttribute("roadNumber",r.getRoadNumber())
					.addAttribute("roadName",r.getRoadName())
					.addAttribute("mfg",r.getMfg())
					.addAttribute("model",r.getModel())
					.addAttribute("dccAddress",r.getDccAddress())
					.addAttribute("comment",r.getComment())
					.addContent(new Element("decoder")
									.addAttribute("model",r.getDecoderModel())
									.addAttribute("family",r.getDecoderFamily())
									.addAttribute("comment",r.getDecoderComment())
								)
						  .addContent(values = new Element("values"))
					)
				;
					
			// Append a decoderDef element to values
			Element decoderDef;
			values.addContent(decoderDef = new Element("decoderDef"));
			// add the variable values to the decoderDef Element
			for (int i = 0; i < variableModel.getRowCount(); i++) {
				decoderDef.addContent(new Element("varValue")
									.addAttribute("name", variableModel.getName(i))
									.addAttribute("value", variableModel.getValString(i))
						);
			}
			// add the CV values to the values Element
			for (int i = 0; i < cvModel.getRowCount(); i++) {
				values.addContent(new Element("CVvalue")
									.addAttribute("name", cvModel.getName(i))
									.addAttribute("value", cvModel.getValString(i))
						);
			}
			
			// write the result to selected file
			java.io.FileOutputStream o = new java.io.FileOutputStream(file);
			XMLOutputter fmt = new XMLOutputter();
			fmt.setNewlines(true);   // pretty printing
			fmt.setIndent(true);
			fmt.output(doc, o);
			
			// mark file as OK
			variableModel.setFileDirty(false);
			}
		catch (Exception e) {
			log.error(e);
		}
	}

	/** 
	* Return a File reference to a new, unique backup file. This is here so it can 
	* be overridden during tests.
	*/
	static public File backupFileName(String name) {
		// File.createTempFile is not available in java 1, so use millisecond time as unique string
		File f =  new File(fileLocation+File.separator+name+"-"
							+((new Date()).getTime()));
		if (log.isDebugEnabled()) log.debug("backup file name is "+f.getAbsolutePath());
		return f;
	}
	
	static public String fileLocation = "prefs";

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProConfigFile.class.getName());
		
}
