// DecoderFile.java

package jmri.jmrit.decoderdefn;

import com.sun.java.util.collections.List;
import jmri.jmrit.symbolicprog.VariableTableModel;
import org.jdom.Element;
import org.jdom.Namespace;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * Represents and manipulates a decoder definition, both as a file and
 * in memory.  The interal storage is a JDOM tree.
 *<P>
 * This object is created by DecoderIndexFile to represent the 
 * decoder identification info _before_ the actual decoder file is read.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		
 * @see jmri.jmrit.decoderdefn.DecoderIndexFile	
 */
public class DecoderFile {
	
	public static String getMfgName(Element decoderElement, Namespace ns) {
		return decoderElement.getChild("id",ns).getAttribute("mfg").getValue();
	}

	public static String getMfgID(Element decoderElement, Namespace ns) {
		return decoderElement.getChild("id",ns).getAttribute("mfg").getValue();
	}
		
	public static String getModelName(Element decoderElement, Namespace ns) {
		return decoderElement.getChild("id",ns).getAttribute("model").getValue();
	}

	public static String getFamilyName(Element decoderElement, Namespace ns) {
		return decoderElement.getChild("id",ns).getAttribute("model").getValue();
	}
		
	public static String getVersionID(Element decoderElement, Namespace ns) {
		return decoderElement.getChild("id",ns).getAttribute("model").getValue();
	}
		
	public static void loadVariableModel(Element decoderElement, Namespace ns, VariableTableModel variableModel) {
		// find decoder id, assuming first decoder is fine for now (e.g. one per file)
		Element decoderID = decoderElement.getChild("id",ns);
			
		// start loading variables to table
		List varList = decoderElement.getChild("variables",ns).getChildren("variable",ns);
		for (int i=0; i<varList.size(); i++) {
			// load each row
			variableModel.setRow(i, (Element)(varList.get(i)), ns);
		}
		variableModel.configDone();
	}


	// initialize logging	
    //static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderFile.class.getName());
		
}
