// NameFile.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.XmlFile;
import java.io.File;
import javax.swing.JComboBox;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Hashtable;

import org.jdom.Attribute;
import org.jdom.Element;

// try to limit the JDOM to this class, so that others can manipulate...

/** 
 * Represents a set of standard names and aliases in memory.
 *<P>
 * This class doesn't provide tools for defining the names & aliases; that's done manually, or
 * at least not done here, to create the file.
 *<P>
 * Initially, we only need one of these, so we use an "instance" method to 
 * locate the one associated with the "xml/names.xml" file.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: NameFile.java,v 1.2 2001-12-09 17:59:44 jacobsen Exp $
 */
public class NameFile extends XmlFile {
	
	// fill in abstract members
	
	protected List nameElementList = new ArrayList();
	public int numNames() { return nameElementList.size(); }
	
	// map standard names to NMRA forms
	protected Hashtable _nmraFromStdHash = new Hashtable(); 
	protected Hashtable _StdFromNmraHash = new Hashtable(); 
	
	public String nmraFromStd(String name) {
		return (String)_nmraFromStdHash.get(name);
	}
	
	public String stdFromNmra(String name) {
		return (String)_StdFromNmraHash.get(name);
	}
	
	static NameFile _instance = null;
	public synchronized static NameFile instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("NameFile creating instance");
			// create and load
			_instance = new NameFile();
			try {
				_instance.readFile(defaultNameFilename());
			} catch (Exception e) {
				log.error("Exception during name file reading: "+e);
			}
		}
		if (log.isDebugEnabled()) log.debug("NameFile returns instance "+_instance);
		return _instance;
	}
	
	/**
	 * Read the contents of a NameFile XML file into this object. Note that this does not
	 * clear any existing entries.
	 */
	void readFile(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		if (log.isDebugEnabled()) log.debug("readFile "+name);

		// read file, find root
		Element root = rootFromFile(name);
		// decode type, invoke proper processing routine
		readNames(root);
	}

	void readNames(Element root) {
		
		List l = root.getChildren("variable");
		if (log.isDebugEnabled()) log.debug("readNames sees "+l.size()+" children");
		for (int i=0; i<l.size(); i++) {
			// handle each entry
			Element el = (Element)l.get(i);
			storeVariable(el);
		}
	}
	
	void storeVariable(Element el) {
		String name = el.getAttribute("name").getValue();
		Attribute attr = el.getAttribute("mfgID");
		if (attr != null) {
			// _mfgIdFromNameHash.put(mfg, attr.getValue());
			// _mfgNameFromIdHash.put(attr.getValue(), mfg);
		}
	}

	/** 
	* Return the filename String for the default file, including location.
	* This is here to allow easy override in tests.
	*/
	protected static String defaultNameFilename() { return fileLocation+File.separator+nameFileName;}

	static protected String fileLocation  = "xml";
	static final protected String nameFileName = "names.xml";
	// initialize logging	
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NameFile.class.getName());
		
}
