// LocoFile.java

package jmri.jmrit.roster;

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

/**
 * Represents and manipulates a locomotive definition, both as a file and
 * in memory.  The interal storage is a JDOM tree. See locomotive-config.dtd
 * <P>
 * This class is intended for use by RosterEntry only; you should not use it
 * directly. That's why it's not a public class.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version		 	$Revision: 1.5 $
 * @see jmri.jmrit.roster.RosterEntry
 * @see jmri.jmrit.roster.Roster
 */
class LocoFile extends XmlFile {

	/**
	 * Convert to a cannonical text form for ComboBoxes, etc
	 */
	public String titleString() {
		return "no title form yet";
	}

	/**
	 * Load a CvTableModel from the locomotive element in the File
     * @param loco A JDOM Element containing the locomotive definition
     * @param cvModel  An existing CvTableModel object which will have
     *                 the CVs from the loco Element appended.  It is
     *                 intended, but not required, that this be empty.
	 */
	public static void loadCvModel(Element loco, CvTableModel cvModel){
		CvValue cvObject;
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
				cvObject = (CvValue)(cvModel.allCvVector().elementAt(cv));
				if (cvObject == null) {
					log.warn("CV "+cv+" was in loco file, but not defined by the decoder definition");
					cvModel.addCV(name);
					cvObject = (CvValue)(cvModel.allCvVector().elementAt(cv));
				}
				cvObject.setValue(Integer.valueOf(value).intValue());
				cvObject.setState(CvValue.FROMFILE);
			}
		} else log.error("no values element found in config file; CVs not configured");

		// ugly hack - set CV17 back to fromFile if present
		// this is here because setting CV17, then CV18 seems to set
		// CV17 to Editted.  This needs to be understood & fixed.
		cvObject = (CvValue)(cvModel.allCvVector().elementAt(17));
		if (cvObject!=null) cvObject.setState(CvValue.FROMFILE);
	}

    /**
     * Write an XML version of this object, including also the RosterEntry
     * information.  Does not do an automatic backup of the file, so that
     * should be done elsewhere.
     *
     * @param file Destination file. This file is overwritten if it exists.
     * @param cvModel provides the CV numbers and contents
     * @param variableModel provides the variable names and contents
     * @param r  RosterEntry providing name, etc, information
     */
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
									.addAttribute("item", variableModel.getLabel(i))
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
     * Write an XML version of this object, including also the RosterEntry
     * information.  Does not do an automatic backup of the file, so that
     * should be done elsewhere. This is intended for copy and import
     * operations, where the tree has been read from an existing file.
     * Hence, only the "ID" information in the roster entry is updated.
     *
     * @param pFile Destination file. This file is overwritten if it exists.
     * @param pRootElement Root element of the JDOM tree to write.
     *                      This should be of type "locomotive-config", and
     *                      should not be in use elsewhere (clone it first!)
     * @param pEntry RosterEntry providing name, etc, information
     */
	public void writeFile(File pFile, Element pRootElement, RosterEntry pEntry) {
		try {
			// This is taken in large part from "Java and XML" page 368

			// create root element
			Document doc = new Document(pRootElement);
			doc.setDocType(new DocType("locomotive-config","locomotive-config.dtd"));

			// Update the locomotive.id element
            pRootElement.getChild("locomotive").getAttribute("id").setValue(pEntry.getId());

			// write the result to selected file
			java.io.FileOutputStream o = new java.io.FileOutputStream(pFile);
			XMLOutputter fmt = new XMLOutputter();
			fmt.setNewlines(true);   // pretty printing
			fmt.setIndent(true);
			fmt.output(doc, o);

        }
		catch (Exception ex) {
			log.error(ex);
		}
	}

    /**
     * Defines the preferences subdirectory in which LocoFiles are kept
     * by default.
     */
	static public String fileLocation = "roster"+File.separator;

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoFile.class.getName());

}
