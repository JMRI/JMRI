// LocoFile.java

package jmri.jmrit.decoderdefn;

import com.sun.java.util.collections.List;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import org.jdom.Element;
import org.jdom.Namespace;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * Represents and manipulates a locomotive definition, both as a file and
 * in memory.  The interal storage is a JDOM tree. See locomotive-config.dtd
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		
 * @see jmri.jmrit.roster.RosterEntry
 * @see jmri.jmrit.roster.Roster
 */
public class LocoFile extends XmlFile {
	
	/**
	 * Define the namespace for reading/writing this to XML
	 */
	public Namespace getNamespace() {
		return Namespace.getNamespace("locomotive",
										"http://jmri.sourceforge.net/xml/locomotive");
	}
	
	/**
	 * Convert to a cannonical text form for ComboBoxes, etc
	 */
	public String titleString() { 
		return "no title form yet";
	}

	/**
	 * Load a CvTableModel from the locomotive element in the File
	 */
	public static void loadCvModel(Element loco, Namespace ns, CvTableModel cvModel){
			// get the CVs and load
			Element values = loco.getChild("values", ns);
			if (values != null) {
			// get the CV values and load
				List elementList = values.getChildren("CVvalue",ns);
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
	
	static public String fileLocation = "prefs";

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoFile.class.getName());
		
}
