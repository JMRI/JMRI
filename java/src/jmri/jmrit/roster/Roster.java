// Roster.java
package jmri.jmrit.roster;

import java.awt.HeadlessException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.util.FileUtil;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Roster manages and manipulates a roster of locomotives. <P> It works with the
 * "roster-config" XML schema to load and store its information. <P> This is an
 * in-memory representation of the roster xml file (see below for constants
 * defining name and location). As such, this class is also responsible for the
 * "dirty bit" handling to ensure it gets written. As a temporary reliability
 * enhancement, all changes to this structure are now being written to a backup
 * file, and a copy is made when the file is opened. <P> Multiple Roster objects
 * don't make sense, so we use an "instance" member to navigate to a single one.
 * <P> The only bound property is the list of RosterEntrys; a
 * PropertyChangedEvent is fired every time that changes. <P> The entries are
 * stored in an ArrayList, sorted alphabetically. That sort is done manually
 * each time an entry is added. <P> The roster is stored in a "Roster Index",
 * which can be read or written. Each individual entry (once stored) contains a
 * filename which can be used to retrieve the locomotive information for that
 * roster entry. Note that the RosterEntry information is duplicated in both the
 * Roster (stored in the roster.xml file) and in the specific file for the
 * entry.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008, 2010
 * @author Dennis Miller Copyright 2004
 * @version	$Revision$
 * @see jmri.jmrit.roster.RosterEntry
 */
public class Roster extends XmlFile implements RosterGroupSelector {

    /*
     * record the single instance of Roster
     */
    static transient Roster _instance = null;
    /*
     * retain any property change listeners when reseting the roster, so listeners get roster changes across the reset
     */
    private static PropertyChangeListener[] listeners = null;
    private UserPreferencesManager preferences;
    private String defaultRosterGroup = null;

    /**
     * Name for the property change fired when adding a roster entry
     */
    public static final String ADD = "add"; // NOI18N
    /**
     * Name for the property change fired when removing a roster entry
     */
    public static final String REMOVE = "remove"; // NOI18N
    /**
     * Name for the property change fired when changing the ID of a roster entry
     */
    public static final String CHANGE = "change"; // NOI18N
    /**
     * Property change event fired when saving the roster
     */
    public static final String SAVED = "saved"; // NOI18N
    /**
     * Property change fired when adding a roster group
     */
    public static final String ROSTER_GROUP_ADDED = "RosterGroupAdded"; // NOI18N
    /**
     * Property change fired when removing a roster group
     */
    public static final String ROSTER_GROUP_REMOVED = "RosterGroupRemoved"; // NOI18N
    /**
     * Property change fired when renaming a roster group
     */
    public static final String ROSTER_GROUP_RENAMED = "RosterGroupRenamed"; // NOI18N
    
    // should be private except that JUnit testing creates multiple Roster objects
    public Roster() {
        super();
        synchronized (this) {
            if (listeners != null) {
                for (PropertyChangeListener l : listeners) {
                    this.addPropertyChangeListener(l);
                }
                listeners = null;
            }
        }
        this.preferences = InstanceManager.getDefault(UserPreferencesManager.class);
        if (this.preferences != null) {
            // for some reason, during JUnit testing, preferences is often null
            this.setDefaultRosterGroup((String) this.preferences.getProperty(Roster.class.getCanonicalName(), "defaultRosterGroup")); // NOI18N
        }
    }

    // should be private except that JUnit testing creates multiple Roster objects
    public Roster(String rosterFilename) {
        this();
        try {
            this.readFile(rosterFilename);
        } catch (Exception e) {
            log.error("Exception during roster reading: " + e);
            try {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ErrorReadingText") + "\n" + e.getMessage(),
                        Bundle.getMessage("ErrorReadingTitle"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException he) {
                // ignore inability to display dialog
            }
        }
    }

    public synchronized static void resetInstance() {
        if (_instance != null) {
            listeners = _instance.pcs.getPropertyChangeListeners();
        }
        _instance = null;
    }

    /**
     * Locate the single instance of Roster, loading it if need be.
     *
     * @return The valid Roster object
     */
    public static synchronized Roster instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("Roster creating instance");
            }
            // create and load
            _instance = new Roster(defaultRosterFilename());
        }
        if (log.isDebugEnabled()) {
            log.debug("Roster returns instance " + _instance);
        }
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
     *
     * @param e Entry to add
     */
    public void addEntry(RosterEntry e) {
        if (log.isDebugEnabled()) {
            log.debug("Add entry " + e);
        }
        int i = _list.size() - 1;// Last valid index
        while (i >= 0) {
            // compareToIgnoreCase not present in Java 1.1.8
            if (e.getId().toUpperCase().compareTo(_list.get(i).getId().toUpperCase()) > 0) {
                break; // I can never remember whether I want break or continue here
            }
            i--;
        }
        _list.add(i + 1, e);
        setDirty(true);
        firePropertyChange(ADD, null, e);
    }

    /**
     * Remove a RosterEntry object from the in-memory Roster. This does not
     * delete the file for the RosterEntry!
     *
     * @param e Entry to remove
     */
    public void removeEntry(RosterEntry e) {
        if (log.isDebugEnabled()) {
            log.debug("Remove entry " + e);
        }
        _list.remove(_list.indexOf(e));
        setDirty(true);
        firePropertyChange(REMOVE, null, e);
    }

    /**
     * @return Number of entries in the Roster.
     */
    public int numEntries() {
        return _list.size();
    }

    /**
     * @return The Number of roster entries that are in the specified group.
     */
    public int numGroupEntries(String group) {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null);
        int num = 0;
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = l.get(i);
            if (group != null) {
                if (r.getAttribute(getRosterGroupProperty(group)) != null) {
                    if (r.getAttribute(getRosterGroupProperty(group)).equals("yes")) { // NOI18N
                        num++;
                    }
                }
            } else {
                num++;
            }
        }
        return num;
    }

    /**
     * Return RosterEntry from a "title" string, ala selection in
     * matchingComboBox.
     */
    public RosterEntry entryFromTitle(String title) {
        for (int i = 0; i < numEntries(); i++) {
            RosterEntry r = _list.get(i);
            if (r.titleString().equals(title)) {
                return r;
            }
        }
        return null;
    }

    public RosterEntry getEntryForId(String id) {
        for (RosterEntry re : _list) {
            if (re.getId().equals(id)) {
                return re;
            }
        }
        return null;
    }

    /**
     * Return a specific entry by index
     */
    public RosterEntry getEntry(int i) {
        return _list.get(i);
    }

    /**
     * Get the Nth RosterEntry in the group
     *
     * @param group
     * @param i
     * @return The specified entry in the group
     */
    public RosterEntry getGroupEntry(String group, int i) {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null);
        int num = 0;
        for (RosterEntry r : l) {
            if (group != null) {
                if ((r.getAttribute(getRosterGroupProperty(group)) != null)
                        && r.getAttribute(getRosterGroupProperty(group)).equals("yes")) { // NOI18N
                    if (num == i) {
                        return r;
                    }
                    num++;
                }
            } else {
                if (num == i) {
                    return r;
                }
                num++;
            }
        }
        return null;
    }

    public int getGroupIndex(String group, RosterEntry re) {
        List<RosterEntry> l = matchingList(null, null, null, null, null, null, null);
        int num = 0;
        for (RosterEntry r : l) {
            if (group != null) {
                if ((r.getAttribute(getRosterGroupProperty(group)) != null)
                        && r.getAttribute(getRosterGroupProperty(group)).equals("yes")) { // NOI18N
                    if (r == re) {
                        return num;
                    }
                    num++;
                }
            } else {
                if (re == r) {
                    return num;
                }
                num++;
            }
        }
        return -1;
    }

    /**
     * Return filename from a "title" string, ala selection in matchingComboBox.
     *
     * @return The filename matching this "title", or null if none exists
     */
    public String fileFromTitle(String title) {
        RosterEntry r = entryFromTitle(title);
        if (r != null) {
            return r.getFileName();
        }
        return null;
    }

    public List<RosterEntry> getEntriesWithAttributeKey(String key) {
        // slow but effective algorithm
        ArrayList<RosterEntry> result = new ArrayList<RosterEntry>();
        java.util.Iterator<RosterEntry> i = _list.iterator();
        while (i.hasNext()) {
            RosterEntry r = i.next();
            if (r.getAttribute(key) != null) {
                result.add(r);
            }
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
            if (v != null && v.equals(value)) {
                result.add(r);
            }
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
            if (s != null) {
                result.addAll(s);
            }
        }
        return result;
    }

    public List<RosterEntry> getEntriesInGroup(String group) {
        if (group == null || group.equals(Roster.ALLENTRIES)) {
            return this.matchingList(null, null, null, null, null, null, null);
        } else {
            return this.getEntriesWithAttributeKeyValue(Roster.getRosterGroupProperty(group), "yes"); // NOI18N
        }
    }

    /**
     * Get a List of {@link RosterEntry} objects in Roster matching some
     * information. The list may have null contents if there are no matches.
     */
    public List<RosterEntry> getEntriesMatchingCriteria(String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderMfgID, String decoderVersionID, String id, String group) {
        List<RosterEntry> l = new ArrayList<RosterEntry>();
        for (int i = 0; i < this.numEntries(); i++) {
            if (this.checkEntry(i, roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderMfgID, id, group)) {
                l.add(this.getEntry(i));
            }
        }
        return l;
    }
    /**
     * List of contained {@link RosterEntry} elements.
     */
    protected List<RosterEntry> _list = new ArrayList<RosterEntry>();

    /**
     * Get a List of {@link RosterEntry} objects in Roster matching some
     * information. The list may have null contents if there are no matches.
     *
     * This method calls {@link #getEntriesMatchingCriteria(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     * }
     * with a null group.
     *
     * @see #getEntriesMatchingCriteria(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public List<RosterEntry> matchingList(String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderMfgID, String decoderVersionID, String id) {
        return this.getEntriesMatchingCriteria(roadName, roadNumber, dccAddress, mfg, decoderMfgID, decoderVersionID, id, null);
    }

    /**
     * Check if an entry is consistent with specific properties. <P> A null
     * String entry always matches. Strings are used for convenience in GUI
     * building.
     *
     */
    public boolean checkEntry(int i, String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderModel, String decoderFamily,
            String id, String group) {
        return this.checkEntry(_list, i, roadName, roadNumber, dccAddress, mfg, decoderModel, decoderFamily, id, group);
    }

    /**
     * Check if an entry is consistent with specific properties. <P> A null
     * String entry always matches. Strings are used for convenience in GUI
     * building.
     *
     */
    public boolean checkEntry(List<RosterEntry> list, int i, String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderModel, String decoderFamily,
            String id, String group) {
        RosterEntry r = list.get(i);
        if (id != null && !id.equals(r.getId())) {
            return false;
        }
        if (roadName != null && !roadName.equals(r.getRoadName())) {
            return false;
        }
        if (roadNumber != null && !roadNumber.equals(r.getRoadNumber())) {
            return false;
        }
        if (dccAddress != null && !dccAddress.equals(r.getDccAddress())) {
            return false;
        }
        if (mfg != null && !mfg.equals(r.getMfg())) {
            return false;
        }
        if (decoderModel != null && !decoderModel.equals(r.getDecoderModel())) {
            return false;
        }
        if (decoderFamily != null && !decoderFamily.equals(r.getDecoderFamily())) {
            return false;
        }
        if (group != null
                && !Roster.ALLENTRIES.equals(group)
                && (r.getAttribute(Roster.getRosterGroupProperty(group)) == null
                || !r.getAttribute(Roster.getRosterGroupProperty(group)).equals("yes"))) { // NOI18N
            return false;
        }
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
        if (log.isDebugEnabled()) {
            log.debug("writeFile " + name);
        }
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        writeFile(file);
    }
    static final public String schemaVersion = ""; // NOI18N

    /**
     * Write the entire roster to a file object. This does not do backup; that
     * has to be done separately. See writeRosterFile() for a public function
     * that finds the default location, does a backup and then calls this.
     *
     * @param file an op
     * @throws IOException
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    void writeFile(File file) throws java.io.IOException {
        // create root element
        Element root = new Element("roster-config"); // NOI18N
        root.setAttribute("noNamespaceSchemaLocation", // NOI18N
                "http://jmri.org/xml/schema/roster" + schemaVersion + ".xsd", // NOI18N
                org.jdom.Namespace.getNamespace("xsi", // NOI18N
                "http://www.w3.org/2001/XMLSchema-instance")); // NOI18N
        Document doc = newDocument(root);

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/roster.xsl"?>
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "roster2array.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        String newLocoString = SymbolicProgBundle.getMessage("LabelNewDecoder");

        //Check the Comment and Decoder Comment fields for line breaks and
        //convert them to a processor directive for storage in XML
        //Note: this is also done in the LocoFile.java class to do
        //the same thing in the indidvidual locomotive roster files
        //Note: these changes have to be undone after writing the file
        //since the memory version of the roster is being changed to the
        //file version for writing
        for (int i = 0; i < numEntries(); i++) {

            //Extract the RosterEntry at this index and inspect the Comment and
            //Decoder Comment fields to change any \n characters to <?p?> processor
            //directives so they can be stored in the xml file and converted
            //back when the file is read.
            RosterEntry r = _list.get(i);
            if (!r.getId().equals(newLocoString)) {
                String tempComment = r.getComment();
                String xmlComment = "";

                //transfer tempComment to xmlComment one character at a time, except
                //when \n is found.  In that case, insert <?p?>
                for (int k = 0; k < tempComment.length(); k++) {
                    if (tempComment.startsWith("\n", k)) { // NOI18N
                        xmlComment = xmlComment + "<?p?>"; // NOI18N
                    } else {
                        xmlComment = xmlComment + tempComment.substring(k, k + 1);
                    }
                }
                r.setComment(xmlComment);

                //Now do the same thing for the decoderComment field
                String tempDecoderComment = r.getDecoderComment();
                String xmlDecoderComment = "";

                for (int k = 0; k < tempDecoderComment.length(); k++) {
                    if (tempDecoderComment.startsWith("\n", k)) { // NOI18N
                        xmlDecoderComment = xmlDecoderComment + "<?p?>"; // NOI18N
                    } else {
                        xmlDecoderComment = xmlDecoderComment
                                + tempDecoderComment.substring(k, k + 1);
                    }
                }
                r.setDecoderComment(xmlDecoderComment);
            } else {
                log.info("skip unsaved roster entry with default name " + r.getId());
            }
        }
        //All Comments and Decoder Comment line feeds have been changed to processor directives


        // add top-level elements
        Element values;
        root.addContent(values = new Element("roster")); // NOI18N
        // add entries
        for (int i = 0; i < numEntries(); i++) {
            if (!_list.get(i).getId().equals(newLocoString)) {
                values.addContent(_list.get(i).store());
            } else {
                log.info("skip unsaved roster entry with default name " + _list.get(i).getId());
            }
        }

        if (_rosterGroupList.size() >= 1) {
            Element rosterGroup = new Element("rosterGroup"); // NOI18N
            for (int i = 0; i < _rosterGroupList.size(); i++) {
                Element group = new Element("group"); // NOI18N
                if (!_rosterGroupList.get(i).toString().equals(ALLENTRIES)) {
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
        for (int i = 0; i < numEntries(); i++) {
            RosterEntry r = _list.get(i);
            if (!r.getId().equals(newLocoString)) {
                String xmlComment = r.getComment();
                String tempComment = "";

                for (int k = 0; k < xmlComment.length(); k++) {
                    if (xmlComment.startsWith("<?p?>", k)) { // NOI18N
                        tempComment = tempComment + "\n"; // NOI18N
                        k = k + 4;
                    } else {
                        tempComment = tempComment + xmlComment.substring(k, k + 1);
                    }
                }
                r.setComment(tempComment);

                String xmlDecoderComment = r.getDecoderComment();
                String tempDecoderComment = ""; // NOI18N

                for (int k = 0; k < xmlDecoderComment.length(); k++) {
                    if (xmlDecoderComment.startsWith("<?p?>", k)) { // NOI18N
                        tempDecoderComment = tempDecoderComment + "\n"; // NOI18N
                        k = k + 4;
                    } else {
                        tempDecoderComment = tempDecoderComment
                                + xmlDecoderComment.substring(k, k + 1);
                    }
                }
                r.setDecoderComment(tempDecoderComment);
            } else {
                log.info("skip unsaved roster entry with default name " + r.getId());
            }
        }

        // done - roster now stored, so can't be dirty
        setDirty(false);
        firePropertyChange(SAVED, false, true);
    }

    /**
     * Name a valid roster entry filename from an entry name. <p> <ul>
     * <li>Replaces all problematic characters with "_". <li>Append .xml suffix
     * </ul> Does not check for duplicates.
     *
     * @throws IllegalArgumentException if called with null or empty entry name
     * @param entry the getId() entry name from the RosterEntry
     * @see RosterEntry#ensureFilenameExists()
     * @since 2.1.5
     */
    static public String makeValidFilename(String entry) {
        if (entry == null) {
            throw new IllegalArgumentException("makeValidFilename requires non-null argument");
        }
        if (entry.equals("")) {
            throw new IllegalArgumentException("makeValidFilename requires non-empty argument");
        }

        // name sure there are no bogus chars in name        
        String cleanName = entry.replaceAll("[\\W]", "_");  // remove \W, all non-word (a-zA-Z0-9_) characters // NOI18N

        // ensure suffix
        return cleanName + ".xml"; // NOI18N
    }

    /**
     * Read the contents of a roster XML file into this object. <P> Note that
     * this does not clear any existing entries.
     *
     * @param name filename of roster file
     */
    @SuppressWarnings("unchecked")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
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
        if (root == null) {
            log.error("Roster file exists, but could not be read; roster not available");
            return;
        }
        //if (log.isDebugEnabled()) XmlFile.dumpElement(root);

        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("roster") != null) { // NOI18N
            List<Element> l = root.getChild("roster").getChildren("locomotive"); // NOI18N
            if (log.isDebugEnabled()) {
                log.debug("readFile sees " + l.size() + " children");
            }
            for (int i = 0; i < l.size(); i++) {
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
                    if (tempComment.startsWith("<?p?>", k)) { // NOI18N
                        xmlComment = xmlComment + "\n"; // NOI18N
                        k = k + 4;
                    } else {
                        xmlComment = xmlComment + tempComment.substring(k, k + 1);
                    }
                }
                r.setComment(xmlComment);

                //Now do the same thing for the decoderComment field
                String tempDecoderComment = r.getDecoderComment();
                String xmlDecoderComment = "";

                for (int k = 0; k < tempDecoderComment.length(); k++) {
                    if (tempDecoderComment.startsWith("<?p?>", k)) { // NOI18N
                        xmlDecoderComment = xmlDecoderComment + "\n"; // NOI18N
                        k = k + 4;
                    } else {
                        xmlDecoderComment = xmlDecoderComment
                                + tempDecoderComment.substring(k, k + 1);
                    }
                }

                r.setDecoderComment(xmlDecoderComment);
            }
        } else {
            log.error("Unrecognized roster file contents in file: " + name);
        }
        if (root.getChild("rosterGroup") != null) { // NOI18N
            List<Element> g = root.getChild("rosterGroup").getChildren("group"); // NOI18N
            for (int i = 0; i < g.size(); i++) {
                addRosterGroupList(g.get(i).getText().toString());
            }
        }
    }
    private boolean dirty = false;

    void setDirty(boolean b) {
        dirty = b;
    }

    boolean isDirty() {
        return dirty;
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
        if (dirty) {
            log.error("Dispose invoked on dirty Roster");
        }
    }

    /**
     * Store the roster in the default place, including making a backup if
     * needed. <p> Uses writeFile(String), a protected method that can write to
     * a specific location.
     */
    public static void writeRosterFile() {
        Roster.instance().makeBackupFile(defaultRosterFilename());
        try {
            Roster.instance().writeFile(defaultRosterFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new roster file, may not be complete: " + e);
            try {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ErrorSavingText") + "\n" + e.getMessage(),
                        Bundle.getMessage("ErrorSavingTitle"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore failure to display dialog
            }
        }
    }

    /**
     * Update the in-memory Roster to be consistent with the current roster
     * file. This removes any existing roster entries!
     */
    public void reloadRosterFile() {
        // clear existing
        _list.clear();
        // and read new
        try {
            _instance.readFile(defaultRosterFilename());
        } catch (Exception e) {
            log.error("Exception during roster reading: " + e);
        }
    }

    /**
     * Return the filename String for the default roster file, including
     * location. This is here to allow easy override in tests.
     */
    public static String defaultRosterFilename() {
        return getFileLocation() + rosterFileName;
    }

    /**
     * Set the default location for the Roster file, and all individual
     * locomotive files.
     *
     * @param f Absolute pathname to use. A null or "" argument flags a return
     * to the original default in the user's files directory.
     */
    public static void setFileLocation(String f) {
        if (f != null && !f.equals("")) {
            fileLocation = f;
            if (f.endsWith(File.separator)) {
                LocoFile.setFileLocation(f + "roster"); // NOI18N
            } else {
                LocoFile.setFileLocation(f + File.separator + "roster"); // NOI18N
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Roster location reset to default");
            }
            fileLocation = FileUtil.getUserFilesPath();
            LocoFile.setFileLocation(FileUtil.getUserFilesPath() + "roster" + File.separator); // NOI18N
        }
        // and make sure next request gets the new one
        resetInstance();
    }

    /**
     * Absolute path to roster file location. <P> Default is in the user's files
     * directory, but can be set to anything.
     *
     * @see jmri.util.FileUtil#getUserFilesPath()
     */
    public static String getFileLocation() {
        return fileLocation;
    }
    private static String fileLocation = FileUtil.getUserFilesPath();

    public static void setRosterFileName(String name) {
        rosterFileName = name;
    }
    private static String rosterFileName = "roster.xml"; // NOI18N
    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it.
    // Note that dispose() doesn't act on these.  Its not clear whether it should...
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Notify that the ID of an entry has changed. This doesn't actually change
     * the Roster per se, but triggers recreation.
     */
    public void entryIdChanged(RosterEntry r) {
        log.debug("EntryIdChanged");

        // order may be wrong! Sort
        RosterEntry[] rarray = new RosterEntry[_list.size()];
        for (int i = 0; i < rarray.length; i++) {
            rarray[i] = _list.get(i);
        }
        StringUtil.sortUpperCase(rarray);
        for (int i = 0; i < rarray.length; i++) {
            _list.set(i, rarray[i]);
        }

        firePropertyChange(CHANGE, null, r);
    }

    public static String getRosterGroupName(String rosterGroup) {
        if (rosterGroup == null) {
            return ALLENTRIES;
        }
        return rosterGroup;
    }

    /**
     * Get the string for a RosterGroup property in a RosterEntry
     *
     * @param name The name of the rosterGroup
     * @return The full property string
     */
    public static String getRosterGroupProperty(String name) {
        return _rosterGroupPrefix + name;
    }
    protected ArrayList<String> _rosterGroupList = new ArrayList<String>();
    static String _rosterGroupPrefix = "RosterGroup:"; // NOI18N

    public String getRosterGroupPrefix() {
        return _rosterGroupPrefix;
    }

    /**
     * Add a roster group, notifying all listeners of the change <p> This method
     * fires the property change notification "RosterGroupAdded"
     *
     * @param str The group to be added
     */
    public void addRosterGroupList(String str) {
        addRosterGroupList(str, true);
    }

    /**
     * Add a roster group and optionally notify all listeners
     *
     * renameRosterGroupList(old, new) calls this method with notify=false The
     * public version of this method calls this method with notify=true This
     * method fires the property change notification "RosterGroupAdded"
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
            firePropertyChange(ROSTER_GROUP_ADDED, null, str);
        }
    }

    /**
     * Delete a roster group, notifying all listeners of the change <p> This
     * method fires the property change notification "RosterGroupDeleted"
     *
     * @param str The group to be deleted
     */
    public void delRosterGroupList(String str) {
        delRosterGroupList(str, true);
    }

    /**
     * Delete a roster group and optionally notify all listeners <p>
     * renameRosterGroupList(old, new) calls this method with notify=false The
     * public version of this method calls this method with notify=true This
     * method fires the property change notification "RosterGroupDeleted"
     *
     * @param str The group to be deleted
     * @param notify Flag to fire a property change
     */
    private void delRosterGroupList(String rg, boolean notify) {
        _rosterGroupList.remove(rg);
        String str = _rosterGroupPrefix + rg;
        List<RosterEntry> groupentries = getEntriesWithAttributeKey(str);
        for (int i = 0; i < groupentries.size(); i++) {
            groupentries.get(i).deleteAttribute(str);
        }
        if (notify) {
            firePropertyChange(ROSTER_GROUP_REMOVED, rg, null);
        }
    }

    /**
     * Copy a roster group, adding every entry in the roster group to the new
     * group. <p> If a roster group with the target name already exists, this
     * method silently fails to rename the roster group. The GUI method
     * CopyRosterGroupAction.performAction() catches this error and informs the
     * user. This method fires the property change "RosterGroupAdded".
     *
     * @param oldName Name of the roster group to be copied
     * @param newName Name of the new roster group
     * @see jmri.jmrit.roster.swing.RenameRosterGroupAction
     */
    public void copyRosterGroupList(String oldName, String newName) {
        if (_rosterGroupList.contains(newName)) {
            return;
        }
        String oldGroup = _rosterGroupPrefix + oldName;
        String newGroup = _rosterGroupPrefix + newName;
        List<RosterEntry> groupEntries = getEntriesWithAttributeKey(oldGroup);
        for (RosterEntry re : groupEntries) {
            re.putAttribute(newGroup, "yes"); // NOI18N
        }
        addRosterGroupList(newName, true);
    }

    /**
     * Rename a roster group, while keeping every entry in the roster group <p>
     * If a roster group with the target name already exists, this method
     * silently fails to rename the roster group. The GUI method
     * RenameRosterGroupAction.performAction() catches this error and informs
     * the user. This method fires the property change "RosterGroupRenamed".
     *
     * @param oldName Name of the roster group to be renamed
     * @param newName New name for the roster group
     * @see jmri.jmrit.roster.swing.RenameRosterGroupAction
     */
    public void renameRosterGroupList(String oldName, String newName) {
        if (_rosterGroupList.contains(newName)) {
            return;
        }
        String oldGroup = _rosterGroupPrefix + oldName;
        String newGroup = _rosterGroupPrefix + newName;
        List<RosterEntry> groupEntries = getEntriesWithAttributeKey(oldGroup);
        for (RosterEntry re : groupEntries) {
            re.putAttribute(newGroup, "yes"); // NOI18N
            re.deleteAttribute(oldGroup);
        }
        addRosterGroupList(newName, false); // do not fire property change
        delRosterGroupList(oldName, false); // do not fire property change
        firePropertyChange(ROSTER_GROUP_RENAMED, oldName, newName);
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
    public final static String ALLENTRIES = Bundle.getMessage("ALLENTRIES");
    // initialize logging
    static Logger log = LoggerFactory.getLogger(Roster.class.getName());

    /**
     * Get the default roster group.
     *
     * This method ensures adherence to the RosterGroupSelector protocol
     *
     * @return The entire roster
     */
    @Override
    public String getSelectedRosterGroup() {
        return getDefaultRosterGroup();
    }

    /**
     * @return the defaultRosterGroup
     */
    public String getDefaultRosterGroup() {
        return defaultRosterGroup;
    }

    /**
     * @param defaultRosterGroup the defaultRosterGroup to set
     */
    public void setDefaultRosterGroup(String defaultRosterGroup) {
        this.defaultRosterGroup = defaultRosterGroup;
        this.preferences.setProperty(Roster.class.getCanonicalName(), "defaultRosterGroup", defaultRosterGroup); // NOI18N
    }

    /**
     * Get an array of all the RosterEntry-containing files in the 
     * target directory
     */
    static String[] getAllFileNames() {
        // ensure preferences will be found for read
        FileUtil.createDirectory(LocoFile.getFileLocation());

        // create an array of file names from roster dir in preferences, count entries
        int i;
        int np = 0;
        String[] sp = null;
        if (log.isDebugEnabled()) log.debug("search directory "+LocoFile.getFileLocation());
        File fp = new File(LocoFile.getFileLocation());
        if (fp.exists()) {
            sp = fp.list();
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML")) {
                    np++;
                }
            }
        } else {
            log.warn(FileUtil.getUserFilesPath()+"roster directory was missing, though tried to create it");
        }

        // Copy the entries to the final array
        String sbox[] = new String[np];
        int n=0;
        if (sp != null && np> 0)
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML")) {
                    sbox[n++] = sp[i];
                }
            }
        // The resulting array is now sorted on file-name to make it easier
        // for humans to read
        jmri.util.StringUtil.sort(sbox);

        if (log.isDebugEnabled()) {
            log.debug("filename list:");
            for (i=0; i<sbox.length; i++)
                log.debug("      "+sbox[i]);
        }
        return sbox;
    }

}
