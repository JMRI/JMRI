/** 
 * DecoderFile.java
 *
 * Description:		Manipulates a decoder XML file; can fill other structures
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog;

import com.sun.java.util.collections.List;

import org.jdom.Element;
import org.jdom.Namespace;

// try to limit the JDOM to this class, so that others can manipulate...

public class DecoderFile {
	
	public static String getMfgName(Element decoderElement, Namespace ns) {
		return decoderElement.getChild("id",ns).getAttribute("mfg").getValue();
	}
		
	public static String getModelName(Element decoderElement, Namespace ns) {
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
