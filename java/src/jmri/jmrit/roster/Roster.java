package jmri.jmrit.roster;

import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.beans.PropertyChangeProvider;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;

/**
 * Roster manages and manipulates a roster of locomotives.
 * <p>
 * It works with the "roster-config" XML schema to load and store its
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
 * The only bound property is the list of RosterEntrys; a PropertyChangedEvent
 * is fired every time that changes.
 * <p>
 * The entries are stored in an ArrayList, sorted alphabetically. That sort is
 * done manually each time an entry is added.
 * <p>
 * The roster is stored in a "Roster Index", which can be read or written. Each
 * individual entry (once stored) contains a filename which can be used to
 * retrieve the locomotive information for that roster entry. Note that the
 * RosterEntry information is duplicated in both the Roster (stored in the
 * roster.xml file) and in the specific file for the entry.
 * <p>
 * Originally, JMRI managed just one global roster, held in a global Roster
 * object. With the rise of more complicated layouts, code has been added to
 * address multiple rosters, with the primary one now held in Roster.default().
 * We're moving references to Roster.default() out to the using code, so that
 * eventually we can make those explicit references to other Roster objects
 * as/when needed.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 * @author Dennis Miller Copyright 2004
 * @see jmri.jmrit.roster.RosterEntry
 */
public class Roster extends XmlFile implements RosterGroupSelector, PropertyChangeProvider, PropertyChangeListener {

    /**
     * List of contained {@link RosterEntry} elements.
     */
    private final List<RosterEntry> _list = new ArrayList<>();
    private boolean dirty = false;
    /*
     * This should always be a real path, changes in the UserFiles location are
     * tracked by listening to FileUtilSupport for those changes and updating
     * this path as needed.
     */
    private String rosterLocation = FileUtil.getUserFilesPath();
    private String rosterIndexFileName = Roster.DEFAULT_ROSTER_INDEX;
    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it.
    // Note that dispose() doesn't act on these.  Its not clear whether it should...
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    static final public String schemaVersion = ""; // NOI18N
    private String defaultRosterGroup = null;
    private final HashMap<String, RosterGroup> rosterGroups = new HashMap<>();

    /**
     * Name of the default roster index file. {@value #DEFAULT_ROSTER_INDEX}
     */
    public static final String DEFAULT_ROSTER_INDEX = "roster.xml"; // NOI18N
    /**
     * Name for the property change fired when adding a roster entry.
     * {@value #ADD}
     */
    public static final String ADD = "add"; // NOI18N
    /**
     * Name for the property change fired when removing a roster entry.
     * {@value #REMOVE}
     */
    public static final String REMOVE = "remove"; // NOI18N
    /**
     * Name for the property change fired when changing the ID of a roster
     * entry. {@value #CHANGE}
     */
    public static final String CHANGE = "change"; // NOI18N
    /**
     * Property change event fired when saving the roster. {@value #SAVED}
     */
    public static final String SAVED = "saved"; // NOI18N
    /**
     * Property change fired when adding a roster group.
     * {@value #ROSTER_GROUP_ADDED}
     */
    public static final String ROSTER_GROUP_ADDED = "RosterGroupAdded"; // NOI18N
    /**
     * Property change fired when removing a roster group.
     * {@value #ROSTER_GROUP_REMOVED}
     */
    public static final String ROSTER_GROUP_REMOVED = "RosterGroupRemoved"; // NOI18N
    /**
     * Property change fired when renaming a roster group.
     * {@value  #ROSTER_GROUP_RENAMED}
     */
    public static final String ROSTER_GROUP_RENAMED = "RosterGroupRenamed"; // NOI18N
    /**
     * String prefixed to roster group names in the roster entry XML.
     * {@value #ROSTER_GROUP_PREFIX}
     */
    public static final String ROSTER_GROUP_PREFIX = "RosterGroup:"; // NOI18N
    /**
     * Title of the "All Entries" roster group. As this varies by locale, do not
     * rely on being able to store this value.
     */
    public static final String ALLENTRIES = Bundle.getMessage("ALLENTRIES"); // NOI18N

    /**
     * Create a roster with default contents.
     */
    public Roster() {
        super();
        FileUtil.getDefault().addPropertyChangeListener(FileUtil.PREFERENCES, (PropertyChangeEvent evt) -> {
            FileUtil.Property oldValue = (FileUtil.Property) evt.getOldValue();
            FileUtil.Property newValue = (FileUtil.Property) evt.getNewValue();
            Profile project = oldValue.getKey();
            if (this.equals(getRoster(project)) && getRosterLocation().equals(oldValue.getValue())) {
                setRosterLocation(newValue.getValue());
                reloadRosterFile();
            }
        });
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((upm) -> {
            // During JUnit testing, preferences is often null
            this.setDefaultRosterGroup((String) upm.getProperty(Roster.class.getCanonicalName(), "defaultRosterGroup")); // NOI18N
        });
    }

    // should be private except that JUnit testing creates multiple Roster objects
    public Roster(String rosterFilename) {
        this();
        try {
            // if the rosterFilename passed in is null, create a complete path
            // to the default roster index before attempting to read
            if (rosterFilename == null) {
                rosterFilename = this.getRosterIndexPath();
            }
            this.readFile(rosterFilename);
        } catch (IOException | JDOMException e) {
            log.error("Exception during reading while constructing roster", e);
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

    /**
     * Get the roster for the profile returned by
     * {@link ProfileManager#getActiveProfile()}.
     *
     * @return the roster for the active profile
     */
    public static synchronized Roster getDefault() {
        return getRoster(ProfileManager.getDefault().getActiveProfile());
    }

    /**
     * Get the roster for the specified profile.
     *
     * @param profile the Profile to get the roster for
     * @return the roster for the profile
     */
    public static synchronized @Nonnull
    Roster getRoster(@CheckForNull Profile profile) {
        return InstanceManager.getDefault(RosterConfigManager.class).getRoster(profile);
    }

    /**
     * Add a RosterEntry object to the in-memory Roster.
     *
     * @param e Entry to add
     */
    public void addEntry(RosterEntry e) {
        log.debug("Add entry {}", e);
        synchronized (_list) {
            int i = _list.size() - 1; // Last valid index
            while (i >= 0) {
                if (e.getId().compareToIgnoreCase(_list.get(i).getId()) > 0) {
                    break; // get out of the loop since the entry at I sorts
                    // before the new entry
                }
                i--;
            }
            _list.add(i + 1, e);
        }
        e.addPropertyChangeListener(this);
        this.addRosterGroups(e.getGroups(this));
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
        log.debug("Remove entry {}", e);
        synchronized (_list) {
            _list.remove(e);
        }
        e.removePropertyChangeListener(this);
        setDirty(true);
        firePropertyChange(REMOVE, e, null);
    }

    /**
     * @return number of entries in the roster
     */
    public int numEntries() {
        synchronized (_list) {
            return _list.size();
        }
    }

    /**
     * @param group The group being queried or null for all entries in the
     *              roster.
     * @return The Number of roster entries in the specified group or 0 if the
     *         group does not exist.
     */
    public int numGroupEntries(String group) {
        if (group != null
                && !group.equals(Roster.ALLENTRIES)
                && !group.equals(Roster.allEntries(Locale.getDefault()))) {
            return (this.rosterGroups.get(group) != null) ? this.rosterGroups.get(group).getEntries().size() : 0;
        } else {
            return this.numEntries();
        }
    }

    /**
     * Return RosterEntry from a "title" string, ala selection in
     * matchingComboBox.
     *
     * @param title The title for the RosterEntry.
     * @return The matching RosterEntry or null
     */
    public RosterEntry entryFromTitle(String title) {
        synchronized (_list) {
            for (RosterEntry re : _list) {
                if (re.titleString().equals(title)) {
                    return re;
                }
            }
        }
        return null;
    }

    /**
     * Return RosterEntry from a "id" string.
     *
     * @param id The id for the RosterEntry.
     * @return The matching RosterEntry or null
     */
    public RosterEntry getEntryForId(String id) {
        synchronized (_list) {
            for (RosterEntry re : _list) {
                if (re.getId().equals(id)) {
                    return re;
                }
            }
        }
        return null;
    }

    /**
     * Return a list of RosterEntry which have a particular DCC address.
     *
     * @param a The address.
     * @return a List of matching entries, empty if there are not matches.
     */
    @Nonnull
    public List<RosterEntry> getEntriesByDccAddress(String a) {
        return findMatchingEntries(
                (RosterEntry r) -> {
                    return r.getDccAddress().equals(a);
                }
        );
    }

    /**
     * Return a specific entry by index
     *
     * @param i The RosterEntry at position i in the roster.
     * @return The matching RosterEntry
     */
    @Nonnull
    public RosterEntry getEntry(int i) {
        synchronized (_list) {
            return _list.get(i);
        }
    }

    /**
     * Get all roster entries.
     *
     * @return a list of roster entries; the list is empty if the roster is
     *         empty
     */
    @Nonnull
    public List<RosterEntry> getAllEntries() {
        return this.getEntriesInGroup(null);
    }

    /**
     * Get the Nth RosterEntry in the group
     *
     * @param group The group being queried.
     * @param i     The index within the group of the requested entry.
     * @return The specified entry in the group or null if i is larger than the
     *         group, or the group does not exist.
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
     * @param title The title for the entry.
     * @return The filename for the RosterEntry matching title, or null if no
     *         such RosterEntry exists.
     */
    public String fileFromTitle(String title) {
        RosterEntry r = entryFromTitle(title);
        if (r != null) {
            return r.getFileName();
        }
        return null;
    }

    public List<RosterEntry> getEntriesWithAttributeKey(String key) {
        ArrayList<RosterEntry> result = new ArrayList<>();
        synchronized (_list) {
            _list.stream().filter((r) -> (r.getAttribute(key) != null)).forEachOrdered((r) -> {
                result.add(r);
            });
        }
        return result;
    }

    public List<RosterEntry> getEntriesWithAttributeKeyValue(String key, String value) {
        ArrayList<RosterEntry> result = new ArrayList<>();
        synchronized (_list) {
            _list.stream().forEach((r) -> {
                String v = r.getAttribute(key);
                if (v != null && v.equals(value)) {
                    result.add(r);
                }
            });
        }
        return result;
    }

    public Set<String> getAllAttributeKeys() {
        Set<String> result = new TreeSet<>();
        synchronized (_list) {
            _list.stream().forEach((r) -> {
                result.addAll(r.getAttributes());
            });
        }
        return result;
    }

    public List<RosterEntry> getEntriesInGroup(String group) {
        if (group == null || group.equals(Roster.ALLENTRIES) || group.isEmpty()) {
            return this.matchingList(null, null, null, null, null, null, null);
        } else {
            return this.getEntriesWithAttributeKeyValue(Roster.getRosterGroupProperty(group), "yes"); // NOI18N
        }
    }

    /**
     * Internal interface works with #findMatchingEntries to provide a common
     * search-match-return capability.
     */
    private interface RosterComparator {

        public boolean check(RosterEntry r);
    }

    /**
     * Internal method works with #RosterComparator to provide a common
     * search-match-return capability.
     */
    private List<RosterEntry> findMatchingEntries(RosterComparator c) {
        List<RosterEntry> l = new ArrayList<>();
        synchronized (_list) {
            _list.stream().filter((r) -> (c.check(r))).forEachOrdered((r) -> {
                l.add(r);
            });
        }
        return l;
    }

    /**
     * Get a List of {@link RosterEntry} objects in Roster matching some
     * information. The list will be empty if there are no matches.
     *
     * @param roadName      road name of entry or null for any road name
     * @param roadNumber    road number of entry of null for any number
     * @param dccAddress    address of entry or null for any address
     * @param mfg           manufacturer of entry or null for any manufacturer
     * @param decoderModel  decoder model of entry or null for any model
     * @param decoderFamily decoder family of entry or null for any family
     * @param id            id of entry or null for any id
     * @param group         group entry is member of or null for any group
     * @return List of matching RosterEntries or an empty List
     */
    @Nonnull
    public List<RosterEntry> getEntriesMatchingCriteria(String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderModel, String decoderFamily, String id, String group) {
        return findMatchingEntries(
                (RosterEntry r) -> {
                    return checkEntry(r, roadName, roadNumber, dccAddress,
                            mfg, decoderModel, decoderFamily,
                            id, group);
                }
        );
    }

    /**
     * Get a List of {@link RosterEntry} objects in Roster matching some
     * information. The list will be empty if there are no matches.
     * <p>
     * This method calls {@link #getEntriesMatchingCriteria(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     * }
     * with a null group.
     *
     * @param roadName      road name of entry or null for any road name
     * @param roadNumber    road number of entry of null for any number
     * @param dccAddress    address of entry or null for any address
     * @param mfg           manufacturer of entry or null for any manufacturer
     * @param decoderModel  decoder model of entry or null for any model
     * @param decoderFamily decoder family of entry or null for any family
     * @param id            id of entry or null for any id
     * @return List of matching RosterEntries or an empty List
     * @see #getEntriesMatchingCriteria(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Nonnull
    public List<RosterEntry> matchingList(String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderModel, String decoderFamily, String id) {
        return this.getEntriesMatchingCriteria(roadName, roadNumber, dccAddress, mfg, decoderModel, decoderFamily, id, null);
    }

    /**
     * Check if an entry is consistent with specific properties.
     * <p>
     * A null String argument always matches. Strings are used for convenience
     * in GUI building.
     *
     * @param i             index in the roster for the RosterEntry
     * @param roadName      road name of entry or null for any road name
     * @param roadNumber    road number of entry of null for any number
     * @param dccAddress    address of entry or null for any address
     * @param mfg           manufacturer of entry or null for any manufacturer
     * @param decoderModel  decoder model of entry or null for any model
     * @param decoderFamily decoder family of entry or null for any family
     * @param id            id of entry or null for any id
     * @param group         group entry is member of or null for any group
     * @return true if the entry matches
     */
    public boolean checkEntry(int i, String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderModel, String decoderFamily,
            String id, String group) {
        return this.checkEntry(_list, i, roadName, roadNumber, dccAddress, mfg, decoderModel, decoderFamily, id, group);
    }

    /**
     * Check if an entry is consistent with specific properties.
     * <p>
     * A null String argument always matches. Strings are used for convenience
     * in GUI building.
     *
     * @param list          the list of RosterEntrys being searched
     * @param i             the index of the roster entry in the list
     * @param roadName      road name of entry or null for any road name
     * @param roadNumber    road number of entry of null for any number
     * @param dccAddress    address of entry or null for any address
     * @param mfg           manufacturer of entry or null for any manufacturer
     * @param decoderModel  decoder model of entry or null for any model
     * @param decoderFamily decoder family of entry or null for any family
     * @param id            id of entry or null for any id
     * @param group         group entry is member of or null for any group
     * @return True if the entry matches
     */
    public boolean checkEntry(List<RosterEntry> list, int i, String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderModel, String decoderFamily,
            String id, String group) {
        RosterEntry r = list.get(i);
        return checkEntry(r, roadName, roadNumber, dccAddress,
                mfg, decoderModel, decoderFamily,
                id, group);
    }

    /**
     * Check if an entry is consistent with specific properties.
     * <p>
     * A null String argument always matches. Strings are used for convenience
     * in GUI building.
     *
     * @param r             the roster entry being checked
     * @param roadName      road name of entry or null for any road name
     * @param roadNumber    road number of entry of null for any number
     * @param dccAddress    address of entry or null for any address
     * @param mfg           manufacturer of entry or null for any manufacturer
     * @param decoderModel  decoder model of entry or null for any model
     * @param decoderFamily decoder family of entry or null for any family
     * @param id            id of entry or null for any id
     * @param group         group entry is member of or null for any group
     * @return True if the entry matches
     */
    public boolean checkEntry(RosterEntry r, String roadName, String roadNumber, String dccAddress,
            String mfg, String decoderModel, String decoderFamily,
            String id, String group) {

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
        return (group == null
                || Roster.ALLENTRIES.equals(group)
                || (r.getAttribute(Roster.getRosterGroupProperty(group)) != null
                && r.getAttribute(Roster.getRosterGroupProperty(group)).equals("yes")));
    }

    /**
     * Write the entire roster to a file.
     * <p>
     * Creates a new file with the given name, and then calls writeFile (File)
     * to perform the actual work.
     *
     * @param name Filename for new file, including path info as needed.
     * @throws java.io.FileNotFoundException if file does not exist
     * @throws java.io.IOException           if unable to write file
     */
    void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
        if (log.isDebugEnabled()) {
            log.debug("writeFile " + name);
        }
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }

        writeFile(file);
    }

    /**
     * Write the entire roster to a file object. This does not do backup; that
     * has to be done separately. See writeRosterFile() for a public function
     * that finds the default location, does a backup and then calls this.
     *
     * @param file the file to write to
     * @throws java.io.IOException if unable to write file
     */
    void writeFile(File file) throws java.io.IOException {
        // create root element
        Element root = new Element("roster-config"); // NOI18N
        root.setAttribute("noNamespaceSchemaLocation", // NOI18N
                "http://jmri.org/xml/schema/roster" + schemaVersion + ".xsd", // NOI18N
                org.jdom2.Namespace.getNamespace("xsi", // NOI18N
                        "http://www.w3.org/2001/XMLSchema-instance")); // NOI18N
        Document doc = newDocument(root);

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/roster.xsl"?>
        java.util.Map<String, String> m = new java.util.HashMap<>();
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
        synchronized (_list) {
            _list.forEach((entry) -> {
                //Extract the RosterEntry at this index and inspect the Comment and
                //Decoder Comment fields to change any \n characters to <?p?> processor
                //directives so they can be stored in the xml file and converted
                //back when the file is read.
                if (!entry.getId().equals(newLocoString)) {
                    String tempComment = entry.getComment();
                    StringBuilder xmlComment = new StringBuilder();

                    //transfer tempComment to xmlComment one character at a time, except
                    //when \n is found.  In that case, insert <?p?>
                    for (int k = 0; k < tempComment.length(); k++) {
                        if (tempComment.startsWith("\n", k)) { // NOI18N
                            xmlComment.append("<?p?>"); // NOI18N
                        } else {
                            xmlComment.append(tempComment.substring(k, k + 1));
                        }
                    }
                    entry.setComment(xmlComment.toString());

                    //Now do the same thing for the decoderComment field
                    String tempDecoderComment = entry.getDecoderComment();
                    StringBuilder xmlDecoderComment = new StringBuilder();

                    for (int k = 0; k < tempDecoderComment.length(); k++) {
                        if (tempDecoderComment.startsWith("\n", k)) { // NOI18N
                            xmlDecoderComment.append("<?p?>"); // NOI18N
                        } else {
                            xmlDecoderComment.append(tempDecoderComment.substring(k, k + 1));
                        }
                    }
                    entry.setDecoderComment(xmlDecoderComment.toString());
                } else {
                    log.debug("skip unsaved roster entry with default name " + entry.getId());
                }
            }); //All Comments and Decoder Comment line feeds have been changed to processor directives
        }
        // add top-level elements
        Element values = new Element("roster"); // NOI18N
        root.addContent(values);
        // add entries
        synchronized (_list) {
            _list.stream().forEach((entry) -> {
                if (!entry.getId().equals(newLocoString)) {
                    values.addContent(entry.store());
                } else {
                    log.debug("skip unsaved roster entry with default name " + entry.getId());
                }
            });
        }
        if (!this.rosterGroups.isEmpty()) {
            Element rosterGroup = new Element("rosterGroup"); // NOI18N
            rosterGroups.keySet().stream().forEach((name) -> {
                Element group = new Element("group"); // NOI18N
                if (!name.equals(Roster.ALLENTRIES)) {
                    group.addContent(name);
                    rosterGroup.addContent(group);
                }
            });
            root.addContent(rosterGroup);
        }

        writeXML(file, doc);

        //Now that the roster has been rewritten in file form we need to
        //restore the RosterEntry object to its normal \n state for the
        //Comment and Decoder comment fields, otherwise it can cause problems in
        //other parts of the program (e.g. in copying a roster)
        synchronized (_list) {
            _list.stream().forEach((entry) -> {
                if (!entry.getId().equals(newLocoString)) {
                    String xmlComment = entry.getComment();
                    StringBuilder tempComment = new StringBuilder();

                    for (int k = 0; k < xmlComment.length(); k++) {
                        if (xmlComment.startsWith("<?p?>", k)) { // NOI18N
                            tempComment.append("\n"); // NOI18N
                            k = k + 4;
                        } else {
                            tempComment.append(xmlComment.substring(k, k + 1));
                        }
                    }
                    entry.setComment(tempComment.toString());

                    String xmlDecoderComment = entry.getDecoderComment();
                    StringBuilder tempDecoderComment = new StringBuilder(); // NOI18N

                    for (int k = 0; k < xmlDecoderComment.length(); k++) {
                        if (xmlDecoderComment.startsWith("<?p?>", k)) { // NOI18N
                            tempDecoderComment.append("\n"); // NOI18N
                            k = k + 4;
                        } else {
                            tempDecoderComment.append(xmlDecoderComment.substring(k, k + 1));
                        }
                    }
                    entry.setDecoderComment(tempDecoderComment.toString());
                } else {
                    log.debug("skip unsaved roster entry with default name {}", entry.getId());
                }
            });
        }
        // done - roster now stored, so can't be dirty
        setDirty(false);
        firePropertyChange(SAVED, false, true);
    }

    /**
     * Name a valid roster entry filename from an entry name.
     * <ul>
     * <li>Replaces all problematic characters with "_". <li>Append .xml suffix
     * </ul> Does not check for duplicates.
     *
     * @return Filename for RosterEntry
     * @param entry the getId() entry name from the RosterEntry
     * @throws IllegalArgumentException if called with null or empty entry name
     * @see RosterEntry#ensureFilenameExists()
     * @since 2.1.5
     */
    static public String makeValidFilename(String entry) {
        if (entry == null) {
            throw new IllegalArgumentException("makeValidFilename requires non-null argument");
        }
        if (entry.isEmpty()) {
            throw new IllegalArgumentException("makeValidFilename requires non-empty argument");
        }

        // name sure there are no bogus chars in name
        String cleanName = entry.replaceAll("[\\W]", "_");  // remove \W, all non-word (a-zA-Z0-9_) characters // NOI18N

        // ensure suffix
        return cleanName + ".xml"; // NOI18N
    }

    /**
     * Read the contents of a roster XML file into this object.
     * <p>
     * Note that this does not clear any existing entries.
     *
     * @param name filename of roster file
     * @throws org.jdom2.JDOMException if file is invalid XML
     * @throws java.io.IOException     if unable to read file
     */
    void readFile(String name) throws org.jdom2.JDOMException, java.io.IOException {
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
            l.stream().forEach((e) -> {
                addEntry(new RosterEntry(e));
            });

            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            synchronized (_list) {
                _list.stream().map((entry) -> {
                    //Extract the Comment field and create a new string for output
                    String tempComment = entry.getComment();
                    StringBuilder xmlComment = new StringBuilder();
                    //transfer tempComment to xmlComment one character at a time, except
                    //when <?p?> is found.  In that case, insert a \n and skip over those
                    //characters in tempComment.
                    for (int k = 0; k < tempComment.length(); k++) {
                        if (tempComment.startsWith("<?p?>", k)) { // NOI18N
                            xmlComment.append("\n"); // NOI18N
                            k = k + 4;
                        } else {
                            xmlComment.append(tempComment.substring(k, k + 1));
                        }
                    }
                    entry.setComment(xmlComment.toString());
                    return entry;
                }).forEachOrdered((r) -> {
                    //Now do the same thing for the decoderComment field
                    String tempDecoderComment = r.getDecoderComment();
                    StringBuilder xmlDecoderComment = new StringBuilder();

                    for (int k = 0; k < tempDecoderComment.length(); k++) {
                        if (tempDecoderComment.startsWith("<?p?>", k)) { // NOI18N
                            xmlDecoderComment.append("\n"); // NOI18N
                            k = k + 4;
                        } else {
                            xmlDecoderComment.append(tempDecoderComment.substring(k, k + 1));
                        }
                    }

                    r.setDecoderComment(xmlDecoderComment.toString());
                });
            }
        } else {
            log.error("Unrecognized roster file contents in file: {}", name);
        }
        if (root.getChild("rosterGroup") != null) { // NOI18N
            List<Element> groups = root.getChild("rosterGroup").getChildren("group"); // NOI18N
            groups.stream().forEach((group) -> {
                addRosterGroup(group.getText());
            });
        }
    }

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
     * needed.
     * <p>
     * Uses writeFile(String), a protected method that can write to a specific
     * location.
     */
    public void writeRoster() {
        this.makeBackupFile(this.getRosterIndexPath());
        try {
            this.writeFile(this.getRosterIndexPath());
        } catch (IOException e) {
            log.error("Exception while writing the new roster file, may not be complete: {}", e);
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
     * Rebuild the Roster index and store it.
     */
    public void reindex() {
        Roster roster = new Roster();
        for (String fileName : Roster.getAllFileNames()) {
            // Read file
            try {
                Element loco = (new LocoFile()).rootFromName(getRosterFilesLocation() + fileName).getChild("locomotive");
                if (loco != null) {
                    RosterEntry re = new RosterEntry(loco);
                    re.setFileName(fileName);
                    roster.addEntry(re);
                }
            } catch (JDOMException | IOException ex) {
                log.error("Exception while loading loco XML file: {}", fileName, ex);
            }
        }

        this.makeBackupFile(this.getRosterIndexPath());
        try {
            roster.writeFile(this.getRosterIndexPath());
        } catch (IOException ex) {
            log.error("Exception while writing the new roster file, may not be complete: {}", ex);
        }
        this.reloadRosterFile();
        log.info("Roster rebuilt, stored in {}", this.getRosterIndexPath());
    }

    /**
     * Update the in-memory Roster to be consistent with the current roster
     * file. This removes any existing roster entries!
     */
    public void reloadRosterFile() {
        // clear existing
        synchronized (_list) {

            _list.clear();
        }
        this.rosterGroups.clear();
        // and read new
        try {
            this.readFile(this.getRosterIndexPath());
        } catch (IOException | JDOMException e) {
            log.error("Exception during reading while reloading roster", e);
        }
    }

    public void setRosterIndexFileName(String fileName) {
        this.rosterIndexFileName = fileName;
    }

    public String getRosterIndexFileName() {
        return this.rosterIndexFileName;
    }

    public String getRosterIndexPath() {
        return this.getRosterLocation() + this.getRosterIndexFileName();
    }

    /*
     * get the path to the file containing roster entry files.
     */
    public String getRosterFilesLocation() {
        return getDefault().getRosterLocation() + "roster" + File.separator;
    }

    /**
     * Set the default location for the Roster file, and all individual
     * locomotive files.
     *
     * @param f Absolute pathname to use. A null or "" argument flags a return
     *          to the original default in the user's files directory. This
     *          parameter must be a potentially valid path on the system.
     */
    public void setRosterLocation(String f) {
        String oldRosterLocation = this.rosterLocation;
        String p = f;
        if (p != null) {
            if (p.isEmpty()) {
                p = null;
            } else {
                p = FileUtil.getAbsoluteFilename(p);
                if (!p.endsWith(File.separator)) {
                    p = p + File.separator;
                }
            }
        }
        if (p == null) {
            p = FileUtil.getUserFilesPath();
        }
        this.rosterLocation = p;
        log.debug("Setting roster location from {} to {}", oldRosterLocation, this.rosterLocation);
        if (this.rosterLocation.equals(FileUtil.getUserFilesPath())) {
            log.debug("Roster location reset to default");
        }
        if (!this.rosterLocation.equals(oldRosterLocation)) {
            this.firePropertyChange(RosterConfigManager.DIRECTORY, oldRosterLocation, this.rosterLocation);
        }
        this.reloadRosterFile();
    }

    /**
     * Absolute path to roster file location.
     * <p>
     * Default is in the user's files directory, but can be set to anything.
     *
     * @return location of the Roster file
     * @see jmri.util.FileUtil#getUserFilesPath()
     */
    @Nonnull
    public String getRosterLocation() {
        return this.rosterLocation;
    }

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

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * Notify that the ID of an entry has changed. This doesn't actually change
     * the roster contents, but triggers a reordering of the roster contents.
     *
     * @param r the entry with a changed Id
     */
    public void entryIdChanged(RosterEntry r) {
        log.debug("EntryIdChanged");
        synchronized (_list) {
            Collections.sort(_list, (RosterEntry o1, RosterEntry o2) -> o1.getId().compareToIgnoreCase(o2.getId()));
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
        return ROSTER_GROUP_PREFIX + name;
    }

    /**
     * Add a roster group, notifying all listeners of the change.
     * <p>
     * This method fires the property change notification
     * {@value #ROSTER_GROUP_ADDED}.
     *
     * @param rg The group to be added
     */
    public void addRosterGroup(RosterGroup rg) {
        if (this.rosterGroups.containsKey(rg.getName())) {
            return;
        }
        this.rosterGroups.put(rg.getName(), rg);
        log.debug("firePropertyChange Roster Groups model: {}", rg.getName()); // test for panel redraw after duplication
        firePropertyChange(ROSTER_GROUP_ADDED, null, rg.getName());
    }

    /**
     * Add a roster group, notifying all listeners of the change.
     * <p>
     * This method creates a {@link jmri.jmrit.roster.rostergroup.RosterGroup}.
     * Use {@link #addRosterGroup(jmri.jmrit.roster.rostergroup.RosterGroup) }
     * if you need to add a subclass of RosterGroup. This method fires the
     * property change notification {@value #ROSTER_GROUP_ADDED}.
     *
     * @param rg The name of the group to be added
     */
    public void addRosterGroup(String rg) {
        // do a quick return without creating a new RosterGroup object
        // if the roster group aleady exists
        if (this.rosterGroups.containsKey(rg)) {
            return;
        }
        this.addRosterGroup(new RosterGroup(rg));
    }

    /**
     * Add a list of {@link jmri.jmrit.roster.rostergroup.RosterGroup}.
     * RosterGroups that are already known to the Roster are ignored.
     *
     * @param groups RosterGroups to add to the roster. RosterGroups already in
     *               the roster will not be added again.
     */
    public void addRosterGroups(List<RosterGroup> groups) {
        groups.stream().forEach((rg) -> {
            this.addRosterGroup(rg);
        });
    }

    public void removeRosterGroup(RosterGroup rg) {
        this.delRosterGroupList(rg.getName());
    }

    /**
     * Delete a roster group, notifying all listeners of the change.
     * <p>
     * This method fires the property change notification
     * "{@value #ROSTER_GROUP_REMOVED}".
     *
     * @param rg The group to be deleted
     */
    public void delRosterGroupList(String rg) {
        RosterGroup group = this.rosterGroups.remove(rg);
        String str = Roster.getRosterGroupProperty(rg);
        group.getEntries().stream().forEach((re) -> {
            re.deleteAttribute(str);
        });
        firePropertyChange(ROSTER_GROUP_REMOVED, rg, null);
    }

    /**
     * Copy a roster group, adding every entry in the roster group to the new
     * group.
     * <p>
     * If a roster group with the target name already exists, this method
     * silently fails to rename the roster group. The GUI method
     * CopyRosterGroupAction.performAction() catches this error and informs the
     * user. This method fires the property change
     * "{@value #ROSTER_GROUP_ADDED}".
     *
     * @param oldName Name of the roster group to be copied
     * @param newName Name of the new roster group
     * @see jmri.jmrit.roster.swing.RenameRosterGroupAction
     */
    public void copyRosterGroupList(String oldName, String newName) {
        if (this.rosterGroups.containsKey(newName)) {
            return;
        }
        this.rosterGroups.put(newName, new RosterGroup(newName));
        String newGroup = Roster.getRosterGroupProperty(newName);
        this.rosterGroups.get(oldName).getEntries().stream().forEach((re) -> {
            re.putAttribute(newGroup, "yes"); // NOI18N
        });
        this.addRosterGroup(new RosterGroup(newName));
        // the firePropertyChange event will be called by addRosterGroup()
    }

    public void rosterGroupRenamed(String oldName, String newName) {
        this.firePropertyChange(Roster.ROSTER_GROUP_RENAMED, oldName, newName);
    }

    /**
     * Rename a roster group, while keeping every entry in the roster group.
     * <p>
     * If a roster group with the target name already exists, this method
     * silently fails to rename the roster group. The GUI method
     * RenameRosterGroupAction.performAction() catches this error and informs
     * the user. This method fires the property change
     * "{@value #ROSTER_GROUP_RENAMED}".
     *
     * @param oldName Name of the roster group to be renamed
     * @param newName New name for the roster group
     * @see jmri.jmrit.roster.swing.RenameRosterGroupAction
     */
    public void renameRosterGroupList(String oldName, String newName) {
        if (this.rosterGroups.containsKey(newName)) {
            return;
        }
        this.rosterGroups.get(oldName).setName(newName);
    }

    /**
     * Get a list of the user defined roster group names.
     * <p>
     * Strings are immutable, so deleting an item from the copy should not
     * affect the system-wide list of roster groups.
     *
     * @return A list of the roster group names.
     */
    public ArrayList<String> getRosterGroupList() {
        ArrayList<String> list = new ArrayList<>(this.rosterGroups.keySet());
        Collections.sort(list);
        return list;
    }

    /**
     * Get the identifier for all entries in the roster.
     *
     * @param locale The desired locale
     * @return "All Entries" in the specified locale
     */
    public static String allEntries(Locale locale) {
        return Bundle.getMessage(locale, "ALLENTRIES"); // NOI18N
    }

    /**
     * Get the default roster group.
     * <p>
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
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((upm) -> {
            upm.setProperty(Roster.class.getCanonicalName(), "defaultRosterGroup", defaultRosterGroup); // NOI18N
        });
    }

    /**
     * Get an array of all the RosterEntry-containing files in the target
     * directory.
     *
     * @return the list of file names for entries in this roster
     */
    static String[] getAllFileNames() {
        // ensure preferences will be found for read
        FileUtil.createDirectory(getDefault().getRosterFilesLocation());

        // create an array of file names from roster dir in preferences, count entries
        int i;
        int np = 0;
        String[] sp = null;
        if (log.isDebugEnabled()) {
            log.debug("search directory " + getDefault().getRosterFilesLocation());
        }
        File fp = new File(getDefault().getRosterFilesLocation());
        if (fp.exists()) {
            sp = fp.list();
            if (sp != null) {
                for (i = 0; i < sp.length; i++) {
                    if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML")) {
                        np++;
                    }
                }
            } else {
                log.warn("expected directory, but {} was a file", getDefault().getRosterFilesLocation());
            }
        } else {
            log.warn(FileUtil.getUserFilesPath() + "roster directory was missing, though tried to create it");
        }

        // Copy the entries to the final array
        String sbox[] = new String[np];
        int n = 0;
        if (sp != null && np > 0) {
            for (i = 0; i < sp.length; i++) {
                if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML")) {
                    sbox[n++] = sp[i];
                }
            }
        }
        // The resulting array is now sorted on file-name to make it easier
        // for humans to read
        java.util.Arrays.sort(sbox);

        if (log.isDebugEnabled()) {
            log.debug("filename list:");
            for (i = 0; i < sbox.length; i++) {
                log.debug("      " + sbox[i]);
            }
        }
        return sbox;
    }

    /**
     * Get the groups known to the roster itself. Note that changes to the
     * returned Map will not be reflected in the Roster.
     *
     * @return the rosterGroups
     */
    public HashMap<String, RosterGroup> getRosterGroups() {
        return new HashMap<>(rosterGroups);
    }

    /**
     * Changes the key used to lookup a RosterGroup by name. This is a helper
     * method that does not fire a notification to any propertyChangeListeners.
     * <p>
     * To rename a RosterGroup, use
     * {@link jmri.jmrit.roster.rostergroup.RosterGroup#setName(java.lang.String)}.
     *
     * @param group  The group being associated with newKey and will be
     *               disassociated with the key matching
     *               {@link RosterGroup#getName()}.
     * @param newKey The new key by which group can be found in the map of
     *               RosterGroups. This should match the intended new name of
     *               group.
     */
    public void remapRosterGroup(RosterGroup group, String newKey) {
        this.rosterGroups.remove(group.getName());
        this.rosterGroups.put(newKey, group);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof RosterEntry) {
            if (evt.getPropertyName().equals(RosterEntry.ID)) {
                this.entryIdChanged((RosterEntry) evt.getSource());
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Roster.class);
}
