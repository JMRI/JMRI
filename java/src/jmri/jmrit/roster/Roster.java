// Roster.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.io.File;

import javax.swing.*;

import java.util.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Roster manages and manipulates a roster of locomotives.
 * <P>
 * It works
 * with the "roster-config" XML DTD to load and store its information.
 *<P>
 * This is an in-memory representation of the roster xml file (see below
 * for constants defining name and location).  As such, this class is
 * also responsible for the "dirty bit" handling to ensure it gets
 * written.  As a temporary reliability enhancement, all changes to
 * this structure are now being written to a backup file, and a copy
 * is made when the file is opened.
 *<P>
 * Multiple Roster objects don't make sense, so we use an "instance" member
 * to navigate to a single one.
 *<P>
 * The only bound property is the list of RosterEntrys; a PropertyChangedEvent
 * is fired every time that changes.
 * <P>
 * The entries are stored in an ArrayList, sorted alphabetically.  That
 * sort is done manually each time an entry is added.
 * <P>
 * The roster is stored in a "Roster Index", which can be read or written.
 * Each individual entry (once stored) contains a filename which can
 * be used to retrieve the locomotive information for that roster entry.
 * Note that the RosterEntry information is duplicated in both the Roster
 * (stored in the roster.xml file) and in the specific file for the entry.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2008, 2010
 * @author  Dennis Miller Copyright 2004
 * @version	$Revision$
 * @see         jmri.jmrit.roster.RosterEntry
 */
public class Roster extends XmlFile {

    /** record the single instance of Roster **/
    static transient Roster _instance = null;
    
    public synchronized static void resetInstance() { 
        _instance = null; 
    }

    /**
     * Locate the single instance of Roster, loading it if need be.
     * @return The valid Roster object
     */
    public static synchronized Roster instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) log.debug("Roster creating instance");
            // create and load
            _instance = new Roster();
            try {
                _instance.readFile(defaultRosterFilename());
            } catch (Exception e) {
                log.error("Exception during roster reading: "+e);
            }
            ALLENTRIES = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("ALLENTRIES");
        }
        if (log.isDebugEnabled()) log.debug("Roster returns instance "+_instance);
        return _instance;
    }

    /**
     * Provide a null (empty) roster instance
     * 
     */
    
    public static synchronized void installNullInstance() {
        _instance = new Roster();
    }
    
    /**
     * Add a RosterEntry object to the in-memory Roster.
     * @param e Entry to add
     */
    public void addEntry(RosterEntry e) {
        if (log.isDebugEnabled()) log.debug("Add entry "+e);
        int i = _list.size()-1;// Last valid index
        while (i>=0) {
            // compareToIgnoreCase not present in Java 1.1.8
            if (e.getId().toUpperCase().compareTo(_list.get(i).getId().toUpperCase()) > 0 )
                break; // I can never remember whether I want break or continue here
            i--;
        }
        _list.add(i+1, e);
        if(_rostergroup!=null)
            e.putAttribute(getRosterGroupWP(), "yes");
        setDirty(true);
        firePropertyChange("add", null, e);
    }

    /**
     * Remove a RosterEntry object from the in-memory Roster.  This
     * does not delete the file for the RosterEntry!
     * @param e Entry to remove
     */
    public void removeEntry(RosterEntry e) {
        if (log.isDebugEnabled()) log.debug("Remove entry "+e);
        /*If there is a current roster group set then we need to make sure that the entry is complete, 
        this can be check as there should be a file name assocated with the entry*/
        if((_rostergroup!=null) && (e.getFileName()!=null)){
            e.deleteAttribute(getRosterGroupWP());
            e.updateFile();
        }
        else
            _list.remove(_list.indexOf(e));
        setDirty(true);
        firePropertyChange("remove", null, e);
    }

    /**
     * @return Number of entries in the Roster.
     */
    public int numEntries() { return _list.size(); }
    
    /**
     * @return The Number of roster entries that are in the currently selected group.
     */
    public int numGroupEntries() {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null);
        int num = 0;
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = l.get(i);
            if(_rostergroup!=null){
                if(r.getAttribute(getRosterGroupWP())!=null){
                    if(r.getAttribute(getRosterGroupWP()).equals("yes"))
                        num++;
                }
            }
            else
                num++;
        }
        return num;
    }

    /**
     * Return a JComboBox containing the entire roster, if a roster group
     * has been selected, it will only return the entries relating to that 
     * group.
     * <P>
     * The JComboBox is based on a common model object, so it updates
     * when the roster changes.
     *
     */
    public JComboBox fullRosterComboBox() {
        return matchingComboBox(null, null, null, null, null, null, null);
    }
    
    /**
     * Return a JComboBox containing the entire roster, regardless of what
     * roster group has been selected.
     * <P>
     * The JComboBox is based on a common model object, so it updates
     * when the roster changes.
     *
     */
    
    public JComboBox fullRosterComboBoxGlobal() {
        return matchingComboBoxGlobal(null, null, null, null, null, null, null);
    }

    /**
     * Get a JComboBox representing the choices that match some information.
     * If a roster group has been selected then the results will be restricted
     * to those in the roster group.
     * <P>
     * Only entries that exactly match the given parameters will be
     * returned.  Null parameters mean to ignore that part of the match.
     * <P>
     * The JComboBox is based on a common model object, so it updates
     * when the roster changes.
     */
    public JComboBox matchingComboBox(String roadName, String roadNumber, String dccAddress,
                                      String mfg, String decoderMfgID, String decoderVersionID, String id ) {
        List<RosterEntry> l = matchingList(roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id );
        JComboBox b = new JComboBox();
        b.setRenderer(new RosterComboListRenderer());
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = l.get(i);
            if(_rostergroup!=null){
                if(r.getAttribute(getRosterGroupWP())!=null){
                    if(r.getAttribute(getRosterGroupWP()).equals("yes"))
                        b.addItem(r.titleString());
                }
            }
            else
                b.addItem(r.titleString());
        }
        return b;
    }
    /**
     * Get a JComboBox representing the choices that match some information
     * regardless of which roster group has been selected.
     * <P>
     * Only entries that exactly match the given parameters will be
     * returned.  Null parameters mean to ignore that part of the match.
     * <P>
     * The JComboBox is based on a common model object, so it updates
     * when the roster changes.
     */
    public JComboBox matchingComboBoxGlobal(String roadName, String roadNumber, String dccAddress,
                                      String mfg, String decoderMfgID, String decoderVersionID, String id ) {
        List<RosterEntry> l = matchingList(roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id );
        JComboBox b = new JComboBox();
        b.setRenderer(new RosterComboListRenderer());
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = l.get(i);
            b.addItem(r.titleString());
        }
        return b;
    }
    
    public void updateComboBox(JComboBox box) {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null );
        box.removeAllItems();
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = _list.get(i);
            if(_rostergroup!=null){
                if(r.getAttribute(getRosterGroupWP())!=null){
                    if(r.getAttribute(getRosterGroupWP()).equals("yes"))
                        box.addItem(r.titleString());
                }
            }
            else
                box.addItem(r.titleString());
        }
    }
    
    public void updateComboBoxGlobal(JComboBox box) {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null );
        box.removeAllItems();
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = _list.get(i);
            box.addItem(r.titleString());
        }
    }

    /**
     * Return RosterEntry from a "title" string, ala selection in matchingComboBox.
     */
    public RosterEntry entryFromTitle(String title ) {
        for (int i = 0; i < numEntries(); i++) {
            RosterEntry r = _list.get(i);
            if (r.titleString().equals(title)) return r;
        }
        return null;
    }
    
    /**
     * Return a specific entry by index
     */
    public RosterEntry getEntry(int i ) {
        return _list.get(i);
    }
    
    /**
     * @return The Number of roster entries that are in the currently selected group.
     */
    public RosterEntry getGroupEntry(int i) {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null);
        int num = 0;
        for (int j = 0; j < l.size(); j++) {
            RosterEntry r = l.get(j);
            if(_rostergroup!=null){
                if(r.getAttribute(getRosterGroupWP())!=null){
                    if(r.getAttribute(getRosterGroupWP()).equals("yes")){
                        if(num==i)
                            return r;
                        num++;
                    }
                }
            } else {
                if(num==i)
                    return r;
                num++;
            }
        }
        return null;
    }

    /**
     * Return filename from a "title" string, ala selection in matchingComboBox.
     * @return The filename matching this "title", or null if none exists
     */
    public String fileFromTitle(String title ) {
        RosterEntry r = entryFromTitle(title);
        if (r != null) return r.getFileName();
        return null;
    }

    public List<RosterEntry> getEntriesWithAttributeKey(String key) {
        // slow but effective algorithm
        ArrayList<RosterEntry> result = new ArrayList<RosterEntry>();
        java.util.Iterator<RosterEntry> i = _list.iterator();
        while (i.hasNext()) {
            RosterEntry r = i.next();
            if (r.getAttribute(key)!=null) result.add(r);
        }
        return result;
    }

   public List<RosterEntry> getEntriesWithAttributeKeyValue(String key, String value) {
        // slow but effective algorithm
        ArrayList<RosterEntry> result = new ArrayList<RosterEntry>();
        java.util.Iterator<RosterEntry> i = _list.iterator();
        while (i.hasNext()) {
            RosterEntry r = i.next();
            String v = r.getAttribute(key);
            if ( v!=null && v.equals(value)) 
                result.add(r);
        }
        return result;
   }
   
   public Set<String> getAllAttributeKeys() {
        // slow but effective algorithm
        Set<String> result = new TreeSet<String>();
        java.util.Iterator<RosterEntry> i = _list.iterator();
        while (i.hasNext()) {
            RosterEntry r = i.next();
            Set<String> s = r.getAttributes();
            if (s!=null) result.addAll(s);
        }
        return result;
   }
   
    /**
     * List of contained {@link RosterEntry} elements.
     */
    protected List<RosterEntry> _list = new ArrayList<RosterEntry>();

    /**
     *	Get a List of {@link RosterEntry} objects in Roster matching some information. 
     * The list may have
     *  null contents if there are no matches.
     */
    public List<RosterEntry> matchingList(String roadName, String roadNumber, String dccAddress,
                             String mfg, String decoderMfgID, String decoderVersionID, String id ) {
        List<RosterEntry> l = new ArrayList<RosterEntry>();
        for (int i = 0; i < numEntries(); i++) {
            if ( checkEntry(i, roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id ))
                l.add(_list.get(i));
        }
        return l;
    }

    /**
     * Check if an entry is consistent with specific properties. 
     *<P>
     * A null String entry
     * always matches. Strings are used for convenience in GUI building.
     *
     */
    public boolean checkEntry(int i, String roadName, String roadNumber, String dccAddress,
                              String mfg, String decoderModel, String decoderFamily,
                              String id ) {
        RosterEntry r = _list.get(i);
        if (id != null && !id.equals(r.getId())) return false;
        if (roadName != null && !roadName.equals(r.getRoadName())) return false;
        if (roadNumber != null && !roadNumber.equals(r.getRoadNumber())) return false;
        if (dccAddress != null && !dccAddress.equals(r.getDccAddress())) return false;
        if (mfg != null && !mfg.equals(r.getMfg())) return false;
        if (decoderModel != null && !decoderModel.equals(r.getDecoderModel())) return false;
        if (decoderFamily != null && !decoderFamily.equals(r.getDecoderFamily())) return false;
        return true;
    }

    /**
     * Write the entire roster to a file. 
	 *
	 * Creates a new file with the given name, and then calls writeFile (File) 
	 * to perform the actual work.
	 * 
     * @param name Filename for new file, including path info as needed.
     * @throws FileNotFoundException
     * @throws IOException
     */
    void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
        if (log.isDebugEnabled()) log.debug("writeFile "+name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }
        
        writeFile (file);
    }
 
    /**
     * Write the entire roster to a file object. This does not do backup; that has
     * to be done separately. See writeRosterFile() for a public function that
     * finds the default location, does a backup and then calls this.
     * @param file an op
     * @throws IOException
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    void writeFile (File file) throws java.io.IOException {
        // create root element
        Element root = new Element("roster-config");
        Document doc = newDocument(root, dtdLocation+"roster-config.dtd");

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/roster.xsl"?>
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation+"roster2array.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0,p);
        
        //Check the Comment and Decoder Comment fields for line breaks and
        //convert them to a processor directive for storage in XML
        //Note: this is also done in the LocoFile.java class to do
        //the same thing in the indidvidual locomotive roster files
        //Note: these changes have to be undone after writing the file
        //since the memory version of the roster is being changed to the
        //file version for writing
        for (int i=0; i<numEntries(); i++){

            //Extract the RosterEntry at this index and inspect the Comment and
            //Decoder Comment fields to change any \n characters to <?p?> processor
            //directives so they can be stored in the xml file and converted
            //back when the file is read.
            RosterEntry r = _list.get(i);
            String tempComment = r.getComment();
            String xmlComment = "";

            //transfer tempComment to xmlComment one character at a time, except
            //when \n is found.  In that case, insert <?p?>
            for (int k = 0; k < tempComment.length(); k++) {
                if (tempComment.startsWith("\n", k)) {
                    xmlComment = xmlComment + "<?p?>";
                }
                else {
                    xmlComment = xmlComment + tempComment.substring(k, k + 1);
                }
            }
            r.setComment(xmlComment);

            //Now do the same thing for the decoderComment field
            String tempDecoderComment = r.getDecoderComment();
            String xmlDecoderComment = "";

            for (int k = 0; k < tempDecoderComment.length(); k++) {
                if (tempDecoderComment.startsWith("\n", k)) {
                    xmlDecoderComment = xmlDecoderComment + "<?p?>";
                }
                else {
                    xmlDecoderComment = xmlDecoderComment +
                        tempDecoderComment.substring(k, k + 1);
                }
            }
            r.setDecoderComment(xmlDecoderComment);

        }
        //All Comments and Decoder Comment line feeds have been changed to processor directives


        // add top-level elements
        Element values;
        root.addContent(values = new Element("roster"));
        // add entries
        for (int i=0; i<numEntries(); i++) {
            values.addContent(_list.get(i).store());
        }
        
        if(_rosterGroupList.size()>=1){
            Element rosterGroup = new Element("rosterGroup");
            for (int i=0; i<_rosterGroupList.size(); i++){
                Element group = new Element("group");
                if(!_rosterGroupList.get(i).toString().equals(ALLENTRIES)){
                    group.addContent(_rosterGroupList.get(i).toString());
                    rosterGroup.addContent(group);
                }
            }
            root.addContent(rosterGroup);
        }

        writeXML(file, doc);

        //Now that the roster has been rewritten in file form we need to
        //restore the RosterEntry object to its normal \n state for the
        //Comment and Decoder comment fields, otherwise it can cause problems in
        //other parts of the program (e.g. in copying a roster)
        for (int i=0; i<numEntries(); i++){
            RosterEntry r = _list.get(i);
            String xmlComment = r.getComment();
            String tempComment = "";

            for (int k = 0; k < xmlComment.length(); k++) {
                if (xmlComment.startsWith("<?p?>", k)) {
                    tempComment = tempComment + "\n";
                    k = k + 4;
                }
                else {
                    tempComment = tempComment + xmlComment.substring(k, k + 1);
                }
            }
            r.setComment(tempComment);

            String xmlDecoderComment = r.getDecoderComment();
            String tempDecoderComment = "";

            for (int k = 0; k < xmlDecoderComment.length(); k++) {
                if (xmlDecoderComment.startsWith("<?p?>", k)) {
                    tempDecoderComment = tempDecoderComment + "\n";
                    k = k + 4;
                }
                else {
                    tempDecoderComment = tempDecoderComment +
                        xmlDecoderComment.substring(k, k + 1);
                }
            }
            r.setDecoderComment(tempDecoderComment);

        }

        // done - roster now stored, so can't be dirty
        setDirty(false);
        firePropertyChange("saved", false, true);
    }

    /**
     * Name a valid roster entry filename from an entry name.
     * <p>
     * <ul>
     * <li>Replaces all problematic characters with "_".
     * <li>Append .xml suffix
     * </ul>
     * Does not check for duplicates.
     * @throws IllegalArgumentException if called with null or empty entry name
     * @param entry the getId() entry name from the RosterEntry
     * @see RosterEntry#ensureFilenameExists()
     * @since 2.1.5
     */
    static public String makeValidFilename(String entry) {
        if (entry==null) throw new IllegalArgumentException("makeValidFilename requires non-null argument");
        if (entry.equals("")) throw new IllegalArgumentException("makeValidFilename requires non-empty argument");

        // name sure there are no bogus chars in name        
        String cleanName = entry.replaceAll("[\\W]","_");  // remove \W, all non-word (a-zA-Z0-9_) characters

        // ensure suffix
        return cleanName+".xml";
    }
    
    /**
     * Read the contents of a roster XML file into this object. 
     * <P>
     * Note that this does not
     * clear any existing entries.
     * @name filename of roster file
     */
    @SuppressWarnings("unchecked")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
	void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
        // roster exists?  
        if (!(new File(name)).exists()) {
            log.debug("no roster file found; this is normal if you haven't put decoders in your roster yet");
            return;
        }

        // find root
        Element root = rootFromName(name);
        if (root==null) {
            log.error("Roster file exists, but could not be read; roster not available");
            return;
        }
        //if (log.isDebugEnabled()) XmlFile.dumpElement(root);

        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("roster") != null) {
            List<Element> l = root.getChild("roster").getChildren("locomotive");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" children");
            for (int i=0; i<l.size(); i++) {
                addEntry(new RosterEntry(l.get(i)));
            }

            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < numEntries(); i++) {
                //Get a RosterEntry object for this index
                RosterEntry r = _list.get(i);

                //Extract the Comment field and create a new string for output
                String tempComment = r.getComment();
                String xmlComment = "";

                //transfer tempComment to xmlComment one character at a time, except
                //when <?p?> is found.  In that case, insert a \n and skip over those
                //characters in tempComment.
                for (int k = 0; k < tempComment.length(); k++) {
                    if (tempComment.startsWith("<?p?>", k)) {
                        xmlComment = xmlComment + "\n";
                        k = k + 4;
                    }
                    else {
                        xmlComment = xmlComment + tempComment.substring(k, k + 1);
                    }
                }
                r.setComment(xmlComment);

                //Now do the same thing for the decoderComment field
                String tempDecoderComment = r.getDecoderComment();
                String xmlDecoderComment = "";

                for (int k = 0; k < tempDecoderComment.length(); k++) {
                    if (tempDecoderComment.startsWith("<?p?>", k)) {
                        xmlDecoderComment = xmlDecoderComment + "\n";
                        k = k + 4;
                    }
                    else {
                        xmlDecoderComment = xmlDecoderComment +
                            tempDecoderComment.substring(k, k + 1);
                    }
                }

                r.setDecoderComment(xmlDecoderComment);
            }
        }
        
        else {
            log.error("Unrecognized roster file contents in file: "+name);
        }
        if (root.getChild("rosterGroup") != null) {
        	List<Element> g = root.getChild("rosterGroup").getChildren("group");
        	for (int i=0; i<g.size(); i++) {
        		addRosterGroupList(g.get(i).getText().toString());
        	}
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
     * Store the roster in the default place, including making a backup if needed.
     * <p>
     * Uses writeFile(String), a protected method that can write to
     * a specific location.
     */
    public static void writeRosterFile() {
        Roster.instance().makeBackupFile(defaultRosterFilename());
        try {
            Roster.instance().writeFile(defaultRosterFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new roster file, may not be complete: "+e);
        }
    }

    /**
     * Update the in-memory Roster to be consistent with
     * the current roster file.  This removes any existing roster entries!
     */
    public void reloadRosterFile() {
        // clear existing
        _list.clear();
        // and read new
        try {
            _instance.readFile(defaultRosterFilename());
        } catch (Exception e) {
            log.error("Exception during roster reading: "+e);
        }
    }


    /**
     * Return the filename String for the default roster file, including location.
     * This is here to allow easy override in tests.
     */
    public static String defaultRosterFilename() { return getFileLocation()+rosterFileName;}

    /**
     * Set the default location for the Roster file, and all
     * individual locomotive files.
     *
     * @param f Absolute pathname to use. A null or "" argument flags
     * a return to the original default in prefsdir.
     */
    public static void setFileLocation(String f) {
        if (f!=null && !f.equals("")) {
            fileLocation = f;
            if (f.endsWith(File.separator))
                LocoFile.setFileLocation(f+"roster");
            else
                LocoFile.setFileLocation(f+File.separator+"roster");
        } else {
            if (log.isDebugEnabled()) log.debug("Roster location reset to default");
            fileLocation = XmlFile.prefsDir();
            LocoFile.setFileLocation(XmlFile.prefsDir()+File.separator+"roster"+File.separator);
        }
        // and make sure next request gets the new one
        resetInstance();
    }

    /**
     * Absolute path to roster file location.
     * <P>
     * Default is in the prefsDir, but can be set to anything.
     * @see XmlFile#prefsDir()
     */
    public static String getFileLocation() { return fileLocation; }
    private static String fileLocation  = XmlFile.prefsDir();

    public static void setRosterFileName(String name) { rosterFileName = name; }
    private static String rosterFileName = "roster.xml";

    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it.
    // Note that dispose() doesn't act on these.  Its not clear whether it should...
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p,old,n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Notify that the ID of an entry has changed.  This doesn't actually change the
     * Roster per se, but triggers recreation.
     */
    public void entryIdChanged(RosterEntry r) {
        log.debug("EntryIdChanged");

        // order may be wrong! Sort
        RosterEntry[] rarray = new RosterEntry[_list.size()];
        for (int i=0; i<rarray.length; i++) rarray[i] =_list.get(i);
        jmri.util.StringUtil.sortUpperCase(rarray);
        for (int i=0; i<rarray.length; i++) _list.set(i,rarray[i]);

        firePropertyChange("change", null, r);
    }
    
        
    static String _rostergroup = null;
    
    public void setRosterGroup(String group){
        String oldGroup = _rostergroup;
        String newGroup = group;
        if (group==null) _rostergroup=null;
        else if (group.equals(ALLENTRIES))
            _rostergroup=null;
        else
            _rostergroup = group;
        if(oldGroup==null)
            oldGroup=ALLENTRIES;
        if(newGroup==null)
            newGroup=ALLENTRIES;
        firePropertyChange("ActiveRosterGroup", oldGroup, newGroup);
    }
    
    public static String getRosterGroup(){
        return _rostergroup;
    }
    
    public static String getRosterGroupName(){
        if(_rostergroup==null)
            return ALLENTRIES;
        return _rostergroup;
    }
    
    public static String getRosterGroupWP(){
        String group = _rosterGroupPrefix+_rostergroup;
        return group;
    }
    
    
    /**
     * This is here so that when a roster entry is added to a group via the table entry,
     * a propertyChangeEvent is fired off.  Not Ideal but it works.
     */ 
    public void rosterGroupEntryChanged(){
        firePropertyChange("ActiveRosterGroup", null, null);
    }

    
    protected ArrayList<String> _rosterGroupList = new ArrayList<String>();
    
    static String _rosterGroupPrefix = "RosterGroup:";
    
    public String getRosterGroupPrefix(){ return _rosterGroupPrefix; }

    /**
     * Add a roster group, notifying all listeners of the change
     * <p>
     * This method fires the property change notification "RosterGroupAdded"
     *
     * @param str The group to be added
     */
    public void addRosterGroupList(String str) {
        addRosterGroupList(str, true);
    }

    /**
     * Add a roster group and optionally notify all listeners
     *
     * renameRosterGroupList(old, new) calls this method with notify=false
     * The public version of this method calls this method with notify=true
     * This method fires the property change notification "RosterGroupAdded"
     *
     * @param str The group to be added
     * @param notify Flag to fire a property change
     */
    private void addRosterGroupList(String str, boolean notify) {
        if (_rosterGroupList.contains(str)) {
            return;
        }
        _rosterGroupList.add(str);
        Collections.sort(_rosterGroupList);
        if (notify) {
            firePropertyChange("RosterGroupAdded", null, str);
        }
    }

    /**
     * Delete a roster group, notifying all listeners of the change
     * <p>
     * This method fires the property change notification "RosterGroupDeleted"
     *
     * @param str The group to be deleted
     */
    public void delRosterGroupList(String str) {
        delRosterGroupList(str, true);
    }

    /**
     * Delete a roster group and optionally notify all listeners
     * <p>
     * renameRosterGroupList(old, new) calls this method with notify=false
     * The public version of this method calls this method with notify=true
     * This method fires the property change notification "RosterGroupDeleted"
     *
     * @param str The group to be deleted
     * @param notify Flag to fire a property change
     */
    private void delRosterGroupList(String str, boolean notify) {
        _rosterGroupList.remove(str);
        // if deleting active group, set active group to all entries
        if (str.equals(_rostergroup)) {
            setRosterGroup(null);
        }
        str = _rosterGroupPrefix + str;
        List<RosterEntry> groupentries = getEntriesWithAttributeKey(str);
        for (int i = 0; i < groupentries.size(); i++) {
            groupentries.get(i).deleteAttribute(str);
        }
        if (notify) {
            firePropertyChange("RosterGroupRemoved", str, null);
        }
    }

    /**
     * Copy a roster group, adding every entry in the roster group to the new
     * group.
     * <p>
     * If a roster group with the target name already exists, this method
     * silently fails to rename the roster group. The GUI method
     * CopyRosterGroupAction.performAction() catches this error and informs
     * the user. This method fires the property change "RosterGroupAdded".
     *
     * @param oldName Name of the roster group to be copied
     * @param newName Name of the new roster group
     * @see RenameRosterGroupAction
     */
    public void copyRosterGroupList(String oldName, String newName) {
        if (_rosterGroupList.contains(newName)) {
            return;
        }
        String oldGroup = _rosterGroupPrefix + oldName;
        String newGroup = _rosterGroupPrefix + newName;
        List<RosterEntry> groupEntries = getEntriesWithAttributeKey(oldGroup);
        for (RosterEntry re : groupEntries) {
            re.putAttribute(newGroup, "yes");
        }
        addRosterGroupList(newName, true);
    }

    /**
     * Rename a roster group, while keeping every entry in the roster group
     * <p>
     * If a roster group with the target name already exists, this method
     * silently fails to rename the roster group. The GUI method
     * RenameRosterGroupAction.performAction() catches this error and informs
     * the user. This method fires the property change "RosterGroupRenamed".
     *
     * @param oldName Name of the roster group to be renamed
     * @param newName New name for the roster group
     * @see RenameRosterGroupAction
     */
    public void renameRosterGroupList(String oldName, String newName) {
        if (_rosterGroupList.contains(newName)) {
            return;
        }
        String oldGroup = _rosterGroupPrefix + oldName;
        String newGroup = _rosterGroupPrefix + newName;
        List<RosterEntry> groupEntries = getEntriesWithAttributeKey(oldGroup);
        for (RosterEntry re : groupEntries) {
            re.putAttribute(newGroup, "yes");
            re.deleteAttribute(oldGroup);
        }
        addRosterGroupList(newName, false); // do not fire property change
        delRosterGroupList(oldName, false); // do not fire property change
        firePropertyChange("RosterGroupRenamed", oldName, newName);
    }

    // What does this do? Should this return the group at i?
    public void getRosterGroupList(int i) {
        _rosterGroupList.get(i);
    }

    /**
     * Get a list of the user defined roster groups.
     *
     * This list is a shallow copy of the system-wide list of roster groups.
     * Strings are immutable, so deleting an item from the copy should not
     * affect the system-wide list of roster groups.
     *
     * @return ArrayList<String>
     */
    public ArrayList<String> getRosterGroupList() {
        return new ArrayList<String>(_rosterGroupList);
    }

    public JComboBox rosterGroupBox() {
        JComboBox b = new JComboBox();
        updateGroupBox(b);
        /*b.insertItemAt(ALLENTRIES,0);
        
        String[] displayList = _rosterGroupList.toArray(new String[_rosterGroupList.size()]);
        java.util.Arrays.sort(displayList);
        for (int i = 0; i < displayList.length; i++) {
            b.addItem(displayList[i]);
        }
        if(_rostergroup==null)
            b.setSelectedIndex(0);
        else
            b.setSelectedItem(_rostergroup);*/
        return b;
    }
    
    public void updateGroupBox(JComboBox box){
        box.removeAllItems();
        
        String[] displayList = _rosterGroupList.toArray(new String[_rosterGroupList.size()]);
        Arrays.sort(displayList);
        
        for(String e : displayList){
            box.addItem(e);
        }
        
        box.insertItemAt(ALLENTRIES,0);
        if(_rostergroup==null)
            box.setSelectedIndex(0);
        else
            box.setSelectedItem(_rostergroup);
    }
    
    public JList rosterGroupList() {
        JList l = new JList();
        updateGroupList(l);
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return l;
    }

    public void updateGroupList(JList l) {
        l.removeAll();
        ArrayList<String> data = new ArrayList<String>();
        data.add(ALLENTRIES);
        
        String[] displayList = _rosterGroupList.toArray(new String[_rosterGroupList.size()]);
        Arrays.sort(displayList);
        
        data.addAll(Arrays.asList(displayList));
        l.setListData(data.toArray());
        
        if (_rostergroup == null) {
            l.setSelectedIndex(0);
        } else {
            l.setSelectedValue(_rostergroup, true);
        }
    }

    static String ALLENTRIES = "All Entries";
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Roster.class.getName());

}
