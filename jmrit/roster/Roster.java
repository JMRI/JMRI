// Roster.java

package jmri.jmrit.roster;

import java.io.*;
import javax.swing.*;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import java.util.Date;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/** 
 * Roster manages and manipulates a roster of locomotives.  It works 
 * with the "roster-config" XML DTD to load and store its information.
 *<P>
 * This is an in-memory representation of the roster xml file (see below
 * for constants defining name and location).  As such, this class is
 * also responsible for the "dirty bit" handling to ensure it gets
 * written.  As a temporary reliability enhancement, all changes to
 * this structure are now being written to a backup file, and a copy
 * is made when the file is opened. Both of these are placed in the
 * "xml/backups/" directory.
 *<P>
 * Multiple Roster objects don't make sense, so we use an "instance" member
 * to navigate to a single one.
 *<P>
 * This predates the "XmlFile" base class, so doesn't use it.  Not sure
 * whether it should...
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: Roster.java,v 1.4 2001-11-16 15:02:41 jacobsen Exp $
 * @see             jmri.jmrit.roster.RosterEntry
 */
public class Roster {
	
	private static Roster _instance = null;
	public static Roster instance() {
		if (_instance == null) {
			// create and load
			_instance = new Roster();
			try {
				_instance.readFile(defaultRosterFilename());
			} catch (Exception e) {
				log.error("Exception during roster reading: "+e);
			}
		}
		return _instance;
	}
	
	public void addEntry(RosterEntry e) {
		if (log.isDebugEnabled()) log.debug("Add entry "+e);
		_list.add(_list.size(), e);
		setDirty(true);
	}
	
	public void removeEntry(RosterEntry e) {
		if (log.isDebugEnabled()) log.debug("Remove entry "+e);
		_list.remove(_list.indexOf(e));
		setDirty(true);
	}
	
	public int numEntries() { return _list.size(); }
	
	/** 
	 * Get a JComboBox representing the choices that match 
	 * some information
	 */
	public JComboBox matchingComboBox(String roadName, String roadNumber, String dccAddress,
									String mfg, String decoderMfgID, String decoderVersionID, String id ) {
		List l = matchingList(roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id );
		JComboBox b = new JComboBox();
		for (int i = 0; i < l.size(); i++) {
			RosterEntry r = (RosterEntry)_list.get(i);
			b.addItem(r.titleString());
		}
		return b;
	}
	 
	/** 
	 * Return RosterEntry from a "title" string, ala selection in matchingComboBox
	 */
	public RosterEntry entryFromTitle(String title ) {
		for (int i = 0; i < numEntries(); i++) {
			RosterEntry r = (RosterEntry)_list.get(i);
			if (r.titleString().equals(title)) return r;
		}
		return null;
	}

	/** 
	 * Return filename from a "title" string, ala selection in matchingComboBox
	 */
	public String fileFromTitle(String title ) {
		RosterEntry r = entryFromTitle(title);
		if (r != null) return r.getFileName();
		return null;
	}

	protected List _list = new ArrayList();

	/**
	 *	Get a List of entries matching some information
	 */
	public List matchingList(String roadName, String roadNumber, String dccAddress,
									String mfg, String decoderMfgID, String decoderVersionID, String id ) {
		List l = new ArrayList();
		for (int i = 0; i < numEntries(); i++) {
			if ( checkEntry(i, roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id ))
				l.add(_list.get(i));
		}
		return l;
	}
	
	/** 
	* Check if an entry consistent with specific properties. A null String entry
	* always matches. Strings are used for convenience in GUI building.
	* 
	*/
	public boolean checkEntry(int i, String roadName, String roadNumber, String dccAddress,
									String mfg, String decoderModel, String decoderFamily,
									String id ) {
		RosterEntry r = (RosterEntry)_list.get(i);
		if (id != null && id.equals(r.getId())) return false;
		if (roadName != null && !roadName.equals(r.getRoadName())) return false;
		if (roadNumber != null && !roadNumber.equals(r.getRoadNumber())) return false;
		if (dccAddress != null && !dccAddress.equals(r.getDccAddress())) return false;
		if (mfg != null && !mfg.equals(r.getMfg())) return false;
		if (decoderModel != null && !decoderModel.equals(r.getDecoderModel())) return false;
		if (decoderFamily != null && !decoderFamily.equals(r.getDecoderFamily())) return false;
		return true;
	}
	
	void writeFile(String name) throws java.io.IOException {
		if (log.isInfoEnabled()) log.info("writeFile "+name);
		// This is taken in large part from "Java and XML" page 368 
		File file = new File(name);
		Namespace ns = Namespace.getNamespace("roster",
										"http://jmri.sourceforge.net/xml/roster");

		// create root element
		Element root = new Element("roster-config", ns);
		Document doc = new Document(root);
		doc.setDocType(new DocType("roster:roster-config","DTD/roster-config.dtd"));
		
		// add top-level elements
		Element values;
		root.addContent(values = new Element("roster",ns));
		// add entries
		for (int i=0; i<numEntries(); i++) {
			values.addContent(((RosterEntry)_list.get(i)).store(ns));
		}
		// write the result to selected file
		java.io.FileOutputStream o = new java.io.FileOutputStream(file);
		XMLOutputter fmt = new XMLOutputter();
		fmt.setNewlines(true);   // pretty printing
		fmt.setIndent(true);
		fmt.output(doc, o);

		// done - roster now stored, so can't be dirty
		setDirty(false);
	}
	
	/**
	 * Read the contents of a roster XML file into this object. Note that this does not
	 * clear any existing entries.
	 */
	void readFile(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException {
		if (log.isInfoEnabled()) log.info("readFile "+name);
		// This is taken in large part from "Java and XML" page 354
			
		// Open and parse file
		Namespace ns = Namespace.getNamespace("roster",
										"http://jmri.sourceforge.net/xml/roster");
		SAXBuilder builder = new SAXBuilder(true);  // argument controls validation, on for now
		Document doc = builder.build(new FileInputStream(new File(name)),"xml"+File.separator);
		// find root
		Element root = doc.getRootElement();
			
		// decode type, invoke proper processing routine if a decoder file
		if (root.getChild("roster", ns) != null) {
			List l = root.getChild("roster", ns).getChildren("locomotive",ns);
			if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" children");
			for (int i=0; i<l.size(); i++) {
				addEntry(new RosterEntry((Element)l.get(i), ns));
			}
		}
		else {
			log.error("Unrecognized roster file contents in file: "+name);
		}
	}
		
	private boolean dirty = false;
	void setDirty(boolean b) {dirty = b;}
	boolean isDirty() {return dirty;}
	
	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");
		if (dirty) log.error("Dispose invoked on dirty Roster");
	}
	
	/** 
	* Move original file to a backup. Use this before writing out a new version of the file
	*/
	protected void makeBackupFile() {
		File roster = new File(defaultRosterFilename());
		if (roster.exists()) {
			roster.renameTo(backupFileName(rosterFileName));
		}
		else log.warn("No roster file to backup");
	}
	
	/** 
	* Return a File reference to a new, unique backup file. This is here so it can 
	* be overridden during tests.
	*/
	protected File backupFileName(String name) {
		// File.createTempFile is not available in java 1, so use millisecond time as unique string
		File f =  new File(fileLocation+File.separator+"b-"
							+((new Date()).getTime())+"-"+name);
		if (log.isDebugEnabled()) log.debug("backup file name is "+f.getAbsolutePath());
		return f;
	}
	
	/** 
	* Return the filename String for the default roster file, including location.
	* This is here to allow easy override in tests.
	*/
	protected static String defaultRosterFilename() { return fileLocation+File.separator+rosterFileName;}

	static protected String fileLocation  = "prefs";
	static final protected String rosterFileName = "roster.xml";
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Roster.class.getName());
		
}
