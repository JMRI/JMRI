// DecoderIndexFile.java

package jmri.jmrit.decoderdefn;

import jmri.jmrit.XmlFile;
import java.io.File;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

import org.jdom.Element;
import org.jdom.Namespace;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * DecoderIndexFile.java
 *
 * Description:		Manipulates the index of decoder definitions.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: DecoderIndexFile.java,v 1.1 2001-11-10 21:41:04 jacobsen Exp $
 *
 * DecoderIndex represents a decoderIndex.xml file in memory, allowing a program
 * to navigate to various decoder descriptions without having to 
 * manipulate files.
 *<P>
 * This class doesn't provide tools for defining the index; that's done manually, or
 * at least not done here.
 *<P>
 * Multiple DecoderIndexFile objects don't make sense, so we use an "instance" member
 * to navigate to a single one.
 *
 */
public class DecoderIndexFile extends XmlFile {
	
	// fill in abstract members
	
	public Namespace getNamespace() {
		return Namespace.getNamespace("decoderIndex",
										"http://jmri.sourceforge.net/xml/decoderIndex");
	}
	
	protected List _list = new ArrayList();
	public int numEntries() { return _list.size(); }
	
	/**
	 *	Get a List of decoders matching some information
	 */
	public List matchingList(String mfg, String family, String decoderMfgID, String decoderVersionID ) {
		List l = new ArrayList();
		for (int i = 0; i < numEntries(); i++) {
			if ( checkEntry(i, mfg, family, decoderMfgID, decoderVersionID ))
				l.add(_list.get(i));
		}
		return l;
	}
	
	/** 
	* Check if an entry consistent with specific properties. A null String entry
	* always matches. Strings are used for convenience in GUI building.
	* Don't bother asking about the model number...
	* 
	*/
	public boolean checkEntry(int i, String mfgName, String family, String mfgID, String decoderVersionID) {
		DecoderFile r = (DecoderFile)_list.get(i);
		//if (mfgName != null && !mfgName.equals(r.getMfgName())) return false;
		//if (family != null && !family.equals(r.getFamily())) return false;
		//if (mfgID != null && !mfgID.equals(r.getMfgID())) return false;
		//if (decoderVersionID != null && !decoderVersionID.equals(r.getDecoderVersionID())) return false;
		return true;
	}

	private static DecoderIndexFile _instance = null;
	public static DecoderIndexFile instance() {
		if (_instance == null) {
			// create and load
			_instance = new DecoderIndexFile();
			try {
				_instance.readFile(defaultDecoderIndexFilename());
			} catch (Exception e) {
				log.error("Exception during decoder index reading: "+e);
			}
		}
		return _instance;
	}
	
	/**
	 * Read the contents of a decoderIndex XML file into this object. Note that this does not
	 * clear any existing entries.
	 */
	void readFile(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		if (log.isInfoEnabled()) log.info("readFile "+name);

		// read file, find root
		Namespace ns = getNamespace();
		Element root = rootFromFile(name, true);
			
		// decode type, invoke proper processing routine if a decoder file
		if (root.getChild("roster", ns) != null) {
			List l = root.getChild("roster", ns).getChildren("locomotive",ns);
			if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" children");
			for (int i=0; i<l.size(); i++) {
				// handle each entry
			}
		}
		else {
			log.error("Unrecognized roster file contents in file: "+name);
		}
	}

	/** 
	* Return the filename String for the default decoder index file, including location.
	* This is here to allow easy override in tests.
	*/
	protected static String defaultDecoderIndexFilename() { return fileLocation+File.separator+decoderIndexFileName;}

	static protected String fileLocation  = "xml";
	static final protected String decoderIndexFileName = "decoderIndex.xml";
	// initialize logging	
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderIndexFile.class.getName());
		
}
