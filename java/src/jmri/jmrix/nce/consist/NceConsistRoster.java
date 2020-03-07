package jmri.jmrix.nce.consist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NCE Consist Roster manages and manipulates a roster of consists.
 * <p>
 * It works with the "consist-roster-config" XML DTD to load and store its
 * information.
 * <p>
 * This is an in-memory representation of the roster xml file (see below for
 * constants defining name and location). As such, this class is also
 * responsible for the "dirty bit" handling to ensure it gets written. As a
 * temporary reliability enhancement, all changes to this structure are now
 * being written to a backup file, and a copy is made when the file is opened.
 * <p>
 * Multiple Roster objects don't make sense, so we use an "instance" member to
 * navigate to a single one.
 * <p>
 * This predates the "XmlFile" base class, so doesn't use it. Not sure whether
 * it should...
 * <p>
 * The only bound property is the list of s; a PropertyChangedEvent is fired
 * every time that changes.
 * <p>
 * The entries are stored in an ArrayList, sorted alphabetically. That sort is
 * done manually each time an entry is added.
 *
 * @author Bob Jacobsen Copyright (C) 2001; Dennis Miller Copyright 2004
 * @author Daniel Boudreau (C) 2008
 * @see NceConsistRosterEntry
 */
public class NceConsistRoster extends XmlFile implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize {
    
    public NceConsistRoster() {
    }

    /**
     * @return The NCE consist roster object
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    public static synchronized NceConsistRoster instance() {
        return InstanceManager.getDefault(NceConsistRoster.class);
    }

    /**
     * Add a RosterEntry object to the in-memory Roster.
     *
     * @param e Entry to add
     */
    public void addEntry(NceConsistRosterEntry e) {
        if (log.isDebugEnabled()) {
            log.debug("Add entry " + e);
        }
        int i = _list.size() - 1;// Last valid index
        while (i >= 0) {
            if (e.getId().compareTo(_list.get(i).getId())> 0) {
                break; // I can never remember whether I want break or continue here
            }
            i--;
        }
        _list.add(i + 1, e);
        setDirty(true);
        firePropertyChange("add", null, e);
    }

    /**
     * Remove a RosterEntry object from the in-memory Roster. This does not
     * delete the file for the RosterEntry!
     *
     * @param e Entry to remove
     */
    public void removeEntry(NceConsistRosterEntry e) {
        if (log.isDebugEnabled()) {
            log.debug("Remove entry " + e);
        }
        _list.remove(_list.indexOf(e));
        setDirty(true);
        firePropertyChange("remove", null, e);
    }

    /**
     * @return Number of entries in the Roster
     */
    public int numEntries() {
        return _list.size();
    }

    /**
     * Return a combo box containing the entire ConsistRoster.
     * <p>
     * This is based on a single model, so it can be updated when the
     * ConsistRoster changes.
     * @return combo box of whole roster
     *
     */
    public JComboBox<String> fullRosterComboBox() {
        return matchingComboBox(null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Get a JComboBox representing the choices that match. There's 10 elements.
     * @param roadName value to match against roster roadname field
     * @param roadNumber value to match against roster roadnumber field
     * @param consistNumber value to match against roster consist number field
     * @param eng1Address value to match against roster 1st engine address field
     * @param eng2Address value to match against roster 2nd engine address field
     * @param eng3Address value to match against roster 3rd engine address field
     * @param eng4Address value to match against roster 4th engine address field
     * @param eng5Address value to match against roster 5th engine address field
     * @param eng6Address value to match against roster 6th engine address field
     * @param id value to match against roster id field
     * @return combo box of matching roster entries
     */
    public JComboBox<String> matchingComboBox(String roadName, String roadNumber,
            String consistNumber, String eng1Address, String eng2Address,
            String eng3Address, String eng4Address, String eng5Address,
            String eng6Address, String id) {
        List<NceConsistRosterEntry> l = matchingList(roadName, roadNumber, consistNumber, eng1Address,
                eng2Address, eng3Address, eng4Address, eng5Address,
                eng6Address, id);
        JComboBox<String> b = new JComboBox<>();
        for (int i = 0; i < l.size(); i++) {
            NceConsistRosterEntry r = _list.get(i);
            b.addItem(r.titleString());
        }
        return b;
    }

    public void updateComboBox(JComboBox<String> box) {
        List<NceConsistRosterEntry> l = matchingList(null, null, null, null, null, null, null, null, null, null);
        box.removeAllItems();
        for (int i = 0; i < l.size(); i++) {
            NceConsistRosterEntry r = _list.get(i);
            box.addItem(r.titleString());
        }
    }

    /**
     * Return RosterEntry from a "title" string, ala selection in
     * matchingComboBox
     * @param title title to search for in consist roster
     * @return matching consist roster entry
     */
    public NceConsistRosterEntry entryFromTitle(String title) {
        for (int i = 0; i < numEntries(); i++) {
            NceConsistRosterEntry r = _list.get(i);
            if (r.titleString().equals(title)) {
                return r;
            }
        }
        return null;
    }

    /**
     * List of contained RosterEntry elements.
     */
    protected List<NceConsistRosterEntry> _list = new ArrayList<>();

    /**
     * Get a List of entries matching some information. The list may have null
     * contents.
     * @param roadName value to match against roster roadname field
     * @param roadNumber value to match against roster roadnumber field
     * @param consistNumber value to match against roster consist number field
     * @param eng1Address value to match against roster 1st engine address field
     * @param eng2Address value to match against roster 2nd engine address field
     * @param eng3Address value to match against roster 3rd engine address field
     * @param eng4Address value to match against roster 4th engine address field
     * @param eng5Address value to match against roster 5th engine address field
     * @param eng6Address value to match against roster 6th engine address field
     * @param id value to match against roster id field
     * @return list of consist roster entries matching request
     */
    public List<NceConsistRosterEntry> matchingList(String roadName, String roadNumber,
            String consistNumber, String eng1Address, String eng2Address,
            String eng3Address, String eng4Address, String eng5Address,
            String eng6Address, String id) {
        List<NceConsistRosterEntry> l = new ArrayList<>();
        for (int i = 0; i < numEntries(); i++) {
            if (checkEntry(i, roadName, roadNumber, consistNumber, eng1Address,
                    eng2Address, eng3Address, eng4Address, eng5Address,
                    eng6Address, id)) {
                l.add(_list.get(i));
            }
        }
        return l;
    }

    /**
     * Check if an entry consistent with specific properties. A null String
     * entry always matches. Strings are used for convenience in GUI building.
     * @param i index to consist roster entry
     * @param roadName value to match against roster roadname field
     * @param roadNumber value to match against roster roadnumber field
     * @param consistNumber value to match against roster consist number field
     * @param loco1Address value to match against roster 1st engine address field
     * @param loco2Address value to match against roster 2nd engine address field
     * @param loco3Address value to match against roster 3rd engine address field
     * @param loco4Address value to match against roster 4th engine address field
     * @param loco5Address value to match against roster 5th engine address field
     * @param loco6Address value to match against roster 6th engine address field
     * @param id value to match against roster id field
     * @return true if values provided matches indexed entry
     */
    public boolean checkEntry(int i, String roadName, String roadNumber,
            String consistNumber, String loco1Address, String loco2Address,
            String loco3Address, String loco4Address, String loco5Address,
            String loco6Address, String id) {
        NceConsistRosterEntry r = _list.get(i);
        if (id != null && !id.equals(r.getId())) {
            return false;
        }
        if (roadName != null && !roadName.equals(r.getRoadName())) {
            return false;
        }
        if (roadNumber != null && !roadNumber.equals(r.getRoadNumber())) {
            return false;
        }
        if (consistNumber != null && !consistNumber.equals(r.getConsistNumber())) {
            return false;
        }
        if (loco1Address != null && !loco1Address.equals(r.getLoco1DccAddress())) {
            return false;
        }
        if (loco2Address != null && !loco2Address.equals(r.getLoco2DccAddress())) {
            return false;
        }
        if (loco3Address != null && !loco3Address.equals(r.getLoco3DccAddress())) {
            return false;
        }
        if (loco4Address != null && !loco4Address.equals(r.getLoco4DccAddress())) {
            return false;
        }
        if (loco5Address != null && !loco5Address.equals(r.getLoco5DccAddress())) {
            return false;
        }
        if (loco6Address != null && !loco6Address.equals(r.getLoco6DccAddress())) {
            return false;
        }
        return true;
    }

    /**
     * Write the entire roster to a file. This does not do backup; that has to
     * be done separately. See writeRosterFile() for a function that finds the
     * default location, does a backup and then calls this.
     *
     * @param name Filename for new file, including path info as needed.
     * @throws java.io.FileNotFoundException when file not found
     * @throws java.io.IOException when fault accessing file
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
        // create root element
        Element root = new Element("consist-roster-config");
        Document doc = newDocument(root, dtdLocation + "consist-roster-config.dtd");

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation + "consistRoster.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0, p);

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
            NceConsistRosterEntry r = _list.get(i);
            String tempComment = r.getComment();
            StringBuilder buf = new StringBuilder();

            //transfer tempComment to xmlComment one character at a time, except
            //when \n is found.  In that case, insert <?p?>
            for (int k = 0; k < tempComment.length(); k++) {
                if (tempComment.startsWith("\n", k)) {
                    buf.append("<?p?>");
                } else {
                    buf.append(tempComment.substring(k, k + 1));
                }
            }
            r.setComment(buf.toString());
        }
        //All Comments and Decoder Comment line feeds have been changed to processor directives

        // add top-level elements
        Element values;
        root.addContent(values = new Element("roster"));
        // add entries
        for (int i = 0; i < numEntries(); i++) {
            values.addContent(_list.get(i).store());
        }
        writeXML(file, doc);

        //Now that the roster has been rewritten in file form we need to
        //restore the RosterEntry object to its normal \n state for the
        //Comment and Decoder comment fields, otherwise it can cause problems in
        //other parts of the program (e.g. in copying a roster)
        for (int i = 0; i < numEntries(); i++) {
            NceConsistRosterEntry r = _list.get(i);
            String xmlComment = r.getComment();
            StringBuilder buf = new StringBuilder();

            for (int k = 0; k < xmlComment.length(); k++) {
                if (xmlComment.startsWith("<?p?>", k)) {
                    buf.append("\n");
                    k = k + 4;
                } else {
                    buf.append(xmlComment.substring(k, k + 1));
                }
            }
            r.setComment(buf.toString());

        }

        // done - roster now stored, so can't be dirty
        setDirty(false);
    }

    /**
     * Read the contents of a roster XML file into this object. Note that this
     * does not clear any existing entries.
     * @param name file name for consist roster
     * @throws org.jdom2.JDOMException other errors
     * @throws java.io.IOException error accessing file
     */
    void readFile(String name) throws org.jdom2.JDOMException, java.io.IOException {
        // find root
        Element root = rootFromName(name);
        if (root == null) {
            log.debug("ConsistRoster file could not be read");
            return;
        }
        //if (log.isDebugEnabled()) XmlFile.dumpElement(root);

        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("roster") != null) {
            List<Element> l = root.getChild("roster").getChildren("consist");
            if (log.isDebugEnabled()) {
                log.debug("readFile sees " + l.size() + " children");
            }
            for (int i = 0; i < l.size(); i++) {
                addEntry(new NceConsistRosterEntry(l.get(i)));
            }

            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < numEntries(); i++) {
                //Get a RosterEntry object for this index
                NceConsistRosterEntry r = _list.get(i);

                //Extract the Comment field and create a new string for output
                String tempComment = r.getComment();
                StringBuilder buf = new StringBuilder();

                //transfer tempComment to xmlComment one character at a time, except
                //when <?p?> is found.  In that case, insert a \n and skip over those
                //characters in tempComment.
                for (int k = 0; k < tempComment.length(); k++) {
                    if (tempComment.startsWith("<?p?>", k)) {
                        buf.append("\n");
                        k = k + 4;
                    } else {
                        buf.append(tempComment.substring(k, k + 1));
                    }
                }
                r.setComment(buf.toString());
            }

        } else {
            log.error("Unrecognized ConsistRoster file contents in file: {}", name);
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
            log.error("Dispose invoked on dirty ConsistRoster");
        }
    }

    /**
     * Store the roster in the default place, including making a backup if
     * needed
     */
    public void writeRosterFile() {
        makeBackupFile(defaultNceConsistRosterFilename());
        try {
            writeFile(defaultNceConsistRosterFilename());
        } catch (IOException e) {
            log.error("Exception while writing the new ConsistRoster file, may not be complete: {}", e.getMessage());
        }
    }

    /**
     * update the in-memory Roster to be consistent with the current roster
     * file. This removes the existing roster entries!
     */
    public void reloadRosterFile() {
        // clear existing
        _list.clear();
        // and read new
        try {
            readFile(defaultNceConsistRosterFilename());
        } catch (IOException | JDOMException e) {
            log.error("Exception during ConsistRoster reading: {}", e.getMessage());
        }
    }

    /**
     * Return the filename String for the default ConsistRoster file, including
     * location.
     * @return consist roster file name
     */
    public static String defaultNceConsistRosterFilename() {
        return Roster.getDefault().getRosterLocation() + nceConsistRosterFileName;
    }

    public static void setNceConsistRosterFileName(String name) {
        nceConsistRosterFileName = name;
    }
    private static String nceConsistRosterFileName = "ConsistRoster.xml";

    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it.
    // Note that dispose() doesn't act on these.  Its not clear whether it should...
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Notify that the ID of an entry has changed. This doesn't actually change
     * the ConsistRoster per se, but triggers recreation.
     * @param r consist roster to recreate due to changes
     */
    public void entryIdChanged(NceConsistRosterEntry r) {
        log.debug("EntryIdChanged");

        // order may be wrong! Sort
        NceConsistRosterEntry[] rarray = new NceConsistRosterEntry[_list.size()];
        for (int i = 0; i < rarray.length; i++) {
            rarray[i] = _list.get(i);
        }
        jmri.util.StringUtil.sortUpperCase(rarray);
        for (int i = 0; i < rarray.length; i++) {
            _list.set(i, rarray[i]);
        }

        firePropertyChange("change", null, r);
    }
    
    @Override
    public void initialize() {
        if (checkFile(defaultNceConsistRosterFilename())) {
            try {
                readFile(defaultNceConsistRosterFilename());
            } catch (IOException | JDOMException e) {
                log.error("Exception during ConsistRoster reading: {}", e.getMessage());
            }
        }
    }
    
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(NceConsistRoster.class);

}
