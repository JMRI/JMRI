// DecoderFile.java

package jmri.jmrit.decoderdefn;

import java.io.*;
import com.sun.java.util.collections.List;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.VariableTableModel;
import org.jdom.Element;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * Represents and manipulates a decoder definition, both as a file and
 * in memory.  The interal storage is a JDOM tree.
 *<P>
 * This object is created by DecoderIndexFile to represent the 
 * decoder identification info _before_ the actual decoder file is read.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: DecoderFile.java,v 1.6 2001-11-27 03:27:15 jacobsen Exp $
 * @see jmri.jmrit.decoderdefn.DecoderIndexFile	
 */
public class DecoderFile extends XmlFile {

	public DecoderFile() {}
	
	public DecoderFile(String mfg, String mfgID, String model, String versionID, 
						String family, String filename, int numFns) {
		_mfg = mfg;
		_mfgID = mfgID;
		_model = model;
		_versionID = versionID;
		_family = family;
		_filename = filename;
		_numFns = numFns;
	}
	
	// store indexing information
	String _mfg       = null;
	String _mfgID     = null;
	String _model     = null;
	String _versionID = null;
	String _family    = null;
	String _filename  = null;
	int _numFns  = -1;

	public String getMfg()       { return _mfg; }	
	public String getMfgID()     { return _mfgID; }	
	public String getModel()     { return _model; }	
	public String getVersionID() { return _versionID; }	
	public String getFamily()    { return _family; }	
	public String getFilename()  { return _filename; }	
	public int getNumFunctions()  { return _numFns; }	
	
	// static service methods - extract info from a given Element
	public static String getMfgName(Element decoderElement) {
		return decoderElement.getChild("id").getAttribute("mfg").getValue();
	}

	public static String getMfgID(Element decoderElement) {
		return decoderElement.getChild("id").getAttribute("mfgID").getValue();
	}
		
	public static String getFamilyName(Element decoderElement) {
		return decoderElement.getChild("id").getAttribute("family").getValue();
	}
		
	public static String getVersionID(Element decoderElement) {
		return decoderElement.getChild("id").getAttribute("versionID").getValue();
	}
			
	// use the decoder Element from the file to load a VariableTableModel for programming.
	public void loadVariableModel(Element decoderElement, 
											VariableTableModel variableModel) {
		// find decoder id, assuming first decoder is fine for now (e.g. one per file)
		Element decoderID = decoderElement.getChild("id");
			
		// start loading variables to table
		List varList = decoderElement.getChild("variables").getChildren("variable");
		for (int i=0; i<varList.size(); i++) {
			Element e = (Element)(varList.get(i));
			// if its associated with an inconsistent number of functions,
			// skip creating it
			if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
				&& getNumFunctions() < Integer.valueOf(e.getAttribute("minFn").getValue()).intValue() )
					continue;
			// load each row
			variableModel.setRow(i, e);
		}
		variableModel.configDone();
	}

	/**
	 * Convert to a cannonical text form for ComboBoxes, etc
	 */
	public String titleString() { 
		return getMfg()+" "+getModel();
	}

	static public String fileLocation = "xml"+File.separator+"decoders";

	// initialize logging	
    //static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderFile.class.getName());
		
}
