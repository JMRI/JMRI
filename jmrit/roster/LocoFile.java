// LocoFile.java

package jmri.jmrit.decoderdefn;

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
 * Represents and manipulates a locomotive definition, both as a file and
 * in memory.  The interal storage is a JDOM tree. See locomotive-config.dtd
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		 	$Id: LocoFile.java,v 1.6 2001-12-18 07:31:07 jacobsen Exp $
 * @see jmri.jmrit.roster.RosterEntry
 * @see jmri.jmrit.roster.Roster
 */
public class LocoFile extends XmlFile {
	
	/**
	 * Convert to a cannonical text form for ComboBoxes, etc
	 */
	public String titleString() { 
		return "no title form yet";
	}

	/**
	 * Load a CvTableModel from the locomotive element in the File
	 */
	public static void loadCvModel(Element loco, CvTableModel cvModel){
			// get the CVs and load
			Element values = loco.getChild("values");
			if (values != null) {
			// get the CV values and load
				List elementList = values.getChildren("CVvalue");
				if (log.isDebugEnabled()) log.debug("Found "+elementList.size()+" CVvalues");
				
				for (int i=0; i<elementList.size(); i++) {
					// locate the row 
					if ( ((Element)(elementList.get(i))).getAttribute("name") == null) {
						  if (log.isDebugEnabled()) log.debug("unexpected null in name "+((Element)(elementList.get(i)))+" "+((Element)(elementList.get(i))).getAttributes());
						  break;
					}
					if ( ((Element)(elementList.get(i))).getAttribute("value") == null) {
						  if (log.isDebugEnabled()) log.debug("unexpected null in value "+((Element)(elementList.get(i)))+" "+((Element)(elementList.get(i))).getAttributes());
						  break;
					}

					String name = ((Element)(elementList.get(i))).getAttribute("name").getValue();
					String value = ((Element)(elementList.get(i))).getAttribute("value").getValue();
					if (log.isDebugEnabled()) log.debug("CV: "+i+"th entry, CV number "+name+" has value: "+value);

					int cv = Integer.valueOf(name).intValue();
					CvValue cvObject = (CvValue)(cvModel.allCvVector().elementAt(cv));
					if (cvObject == null) {
						log.warn("CV "+cv+" was in loco file, but not defined by the decoder definition");
						cvModel.addCV(name);
						cvObject = (CvValue)(cvModel.allCvVector().elementAt(cv));
					}
					cvObject.setValue(Integer.valueOf(value).intValue());
					cvObject.setState(CvValue.FROMFILE);
				}
			} else log.error("no values element found in config file; CVs not configured");
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

	static public String fileLocation = "roster"+File.separator;

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoFile.class.getName());
		
}
