// DecoderIndexFile.java

package jmri.jmrit.decoderdefn;

import jmri.jmrit.XmlFile;
import java.io.File;
import javax.swing.JComboBox;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Hashtable;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * DecoderIndexFile.java
 *
 * Description:		Manipulates the index of decoder definitions.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: DecoderIndexFile.java,v 1.3 2001-11-12 21:53:27 jacobsen Exp $
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
	
	protected List decoderList = new ArrayList();
	public int numDecoders() { return decoderList.size(); }
	
	// map mfg ID numbers from & to mfg names	
	protected Hashtable _mfgIdFromNameHash = new Hashtable(); 
	protected Hashtable _mfgNameFromIdHash = new Hashtable(); 
	
	public String mfgIdFromName(String name) {
		return (String)_mfgIdFromNameHash.get(name);
	}
	
	public String mfgNameFromId(String name) {
		return (String)_mfgNameFromIdHash.get(name);
	}
	
	/**
	 *	Get a List of decoders matching some information
	 */
	public List matchingDecoderList(String mfg, String family, String decoderMfgID, String decoderVersionID, String model ) {
		List l = new ArrayList();
		for (int i = 0; i < numDecoders(); i++) {
			if ( checkEntry(i, mfg, family, decoderMfgID, decoderVersionID, model ))
				l.add(decoderList.get(i));
		}
		return l;
	}
	
	/** 
	 * Get a JComboBox representing the choices that match 
	 * some information
	 */
	public JComboBox matchingComboBox(String mfg, String family, String decoderMfgID, String decoderVersionID, String model ) {
		List l = matchingDecoderList(mfg, family, decoderMfgID, decoderVersionID, model );
		JComboBox b = new JComboBox();
		for (int i = 0; i < l.size(); i++) {
			DecoderFile r = (DecoderFile)decoderList.get(i);
			b.addItem(r.titleString());
		}
		return b;
	}

	/** 
	 * Return DecoderFile from a "title" string, ala selection in matchingComboBox
	 */
	public DecoderFile fileFromTitle(String title ) {
		for (int i = 0; i < numDecoders(); i++) {
			DecoderFile r = (DecoderFile)decoderList.get(i);
			if (r.titleString().equals(title)) return r;
		}
		return null;
	}

	/** 
	* Check if an entry consistent with specific properties. A null String entry
	* always matches. Strings are used for convenience in GUI building.
	* Don't bother asking about the model number...
	* 
	*/
	public boolean checkEntry(int i, String mfgName, String family, String mfgID, String decoderVersionID, String model) {
		DecoderFile r = (DecoderFile)decoderList.get(i);
		if (mfgName != null && !mfgName.equals(r.getMfg())) return false;
		if (family != null && !family.equals(r.getFamily())) return false;
		if (mfgID != null && !mfgID.equals(r.getMfgID())) return false;
		if (decoderVersionID != null && !decoderVersionID.equals(r.getVersionID())) return false;
		if (model != null && !model.equals(r.getModel())) return false;
		return true;
	}

	static DecoderIndexFile _instance = null;
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
		if (root.getChild("decoderIndex", ns) != null) {
			readMfgSection(root.getChild("decoderIndex", ns), ns);
			readFamilySection(root.getChild("decoderIndex", ns), ns);
		}
		else {
			log.error("Unrecognized decoderIndex file contents in file: "+name);
		}
	}

	void readMfgSection(Element decoderIndex, Namespace ns) {
		Element mfgList = decoderIndex.getChild("mfgList", ns);
		if (mfgList != null) {
		
			List l = mfgList.getChildren("manufacturer",ns);
			if (log.isDebugEnabled()) log.debug("readMfgSection sees "+l.size()+" children");
			for (int i=0; i<l.size(); i++) {
				// handle each entry
				Element el = (Element)l.get(i);
				String mfg = el.getAttribute("mfg").getValue();
				Attribute attr = el.getAttribute("mfgID");
				if (attr != null) {
					_mfgIdFromNameHash.put(mfg, attr.getValue());
					_mfgNameFromIdHash.put(attr.getValue(), mfg);
				}
			}
		} else log.warn("no mfgList found in decoderIndexFile");
	}
	
	void readFamilySection(Element decoderIndex, Namespace ns) {
		Element familyList = decoderIndex.getChild("familyList", ns);
		if (familyList != null) {
		
			List l = familyList.getChildren("family",ns);
			if (log.isDebugEnabled()) log.debug("readFamilySection sees "+l.size()+" children");
			for (int i=0; i<l.size(); i++) {
				// handle each entry
				Element el = (Element)l.get(i);
				readFamily(el, ns);
			}
		} else log.warn("no familyList found in decoderIndexFile");
	}

	void readFamily(Element family, Namespace ns) {
		Attribute attr;
		String filename = family.getAttribute("file").getValue();
		String parentVersID = ((attr = family.getAttribute("versionID"))     != null ? attr.getValue() : null );
		String familyName   = ((attr = family.getAttribute("name"))     != null ? attr.getValue() : null );
		String mfg   = ((attr = family.getAttribute("mfg"))     != null ? attr.getValue() : null );
		String mfgID   = mfgIdFromName(mfg);

		// record the decoders
		List l = family.getChildren("decoder",ns);
		if (log.isDebugEnabled()) log.debug("readFamily sees "+l.size()+" children");
		for (int i=0; i<l.size(); i++) {
			// handle each entry by creating a DecoderFile object containing all it knows
			Element decoder = (Element)l.get(i);
			String versID = ( (attr = decoder.getAttribute("versionID"))     != null ? attr.getValue() : parentVersID); 
			int numFns   = ((attr = decoder.getAttribute("numFns"))     != null ? Integer.valueOf(attr.getValue()).intValue() : -1 );
			DecoderFile df = new DecoderFile( mfg, mfgID,
									( (attr = decoder.getAttribute("model"))     != null ? attr.getValue() : null ), 
									versID, familyName, filename, numFns); 
			// and store it
			decoderList.add(df);
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
