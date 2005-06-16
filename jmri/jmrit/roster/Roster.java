// Roster.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.io.File;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

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
 * This predates the "XmlFile" base class, so doesn't use it.  Not sure
 * whether it should...
 * <P>
 * The only bound property is the list of RosterEntrys; a PropertyChangedEvent
 * is fired every time that changes.
 * <P>
 * The entries are stored in an ArrayList, sorted alphabetically.  That
 * sort is done manually each time an entry is added.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001;  Dennis Miller Copyright 2004
 * @version	$Revision: 1.26 $
 * @see         jmri.jmrit.roster.RosterEntry
 */
public class Roster extends XmlFile {

    /** record the single instance of Roster **/
    private static Roster _instance = null;

    public synchronized static void resetInstance() { _instance = null; }

    /**
     * Locate the single instance of Roster, loading it if need be
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
        }
        if (log.isDebugEnabled()) log.debug("Roster returns instance "+_instance);
        return _instance;
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
            if (e.getId().toUpperCase().compareTo(((RosterEntry)_list.get(i)).getId().toUpperCase()) > 0 )
                break; // I can never remember whether I want break or continue here
            i--;
        }
        _list.add(i+1, e);
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
        _list.remove(_list.indexOf(e));
        setDirty(true);
        firePropertyChange("remove", null, e);
    }

    /**
     * @return Number of entries in the Roster
     */
    public int numEntries() { return _list.size(); }

    /**
     * Return a combo box containing the entire roster.
     * <P>
     * This is based on a single model, so it can be updated
     * when the roster changes.
     *
     */
    public JComboBox fullRosterComboBox() {
        return matchingComboBox(null, null, null, null, null, null, null);
    }

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

    public void updateComboBox(JComboBox box) {
        List l = matchingList(null, null, null, null, null, null, null );
        box.removeAllItems();
        for (int i = 0; i < l.size(); i++) {
            RosterEntry r = (RosterEntry)_list.get(i);
            box.addItem(r.titleString());
        }
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
     * @return The filename matching this "title", or null if none exists
     */
    public String fileFromTitle(String title ) {
        RosterEntry r = entryFromTitle(title);
        if (r != null) return r.getFileName();
        return null;
    }

    /**
     * List of contained RosterEntry elements.
     */
    protected List _list = new ArrayList();

    /**
     *	Get a List of entries matching some information. The list may have
     *  null contents.
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
     * Write the entire roster to a file. This does not do backup; that has
     * to be done separately. See writeRosterFile() for a function that
     * finds the default location, does a backup and then calls this.
     * @param name Filename for new file, including path info as needed.
     * @throws IOException
     */
    void writeFile(String name) throws java.io.IOException {
        if (log.isDebugEnabled()) log.debug("writeFile "+name);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(name);
        if (file == null) {
            file = new File(name);
        }
        // create root element
        Element root = new Element("roster-config");
        Document doc = newDocument(root, "roster-config.dtd");

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
          RosterEntry r = (RosterEntry) (RosterEntry)_list.get(i);
          String tempComment = r.getComment();
          String xmlComment = new String();

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
          String xmlDecoderComment = new String();

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
            values.addContent(((RosterEntry)_list.get(i)).store());
        }
        // write the result to selected file
        java.io.FileOutputStream o = new java.io.FileOutputStream(file);
        XMLOutputter fmt = new XMLOutputter();
        fmt.setNewlines(true);   // pretty printing
        fmt.setIndent(true);
        fmt.output(doc, o);
        o.close();

        //Now that the roster has been rewritten in file form we need to
        //restore the RosterEntry object to its normal \n state for the
        //Comment and Decoder comment fields, otherwise it can cause problems in
        //other parts of the program (e.g. in copying a roster)
        for (int i=0; i<numEntries(); i++){
          RosterEntry r = (RosterEntry) (RosterEntry)_list.get(i);
          String xmlComment = r.getComment();
          String tempComment = new String();

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
          String tempDecoderComment = new String();

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
    }

    /**
     * Read the contents of a roster XML file into this object. Note that this does not
     * clear any existing entries.
     */
    void readFile(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException {
        // find root
        Element root = rootFromName(name);
        if (root==null) {
            log.warn("roster file could not be read");
            return;
        }
        if (log.isDebugEnabled()) XmlFile.dumpElement(root);

        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("roster") != null) {
            List l = root.getChild("roster").getChildren("locomotive");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" children");
            for (int i=0; i<l.size(); i++) {
                addEntry(new RosterEntry((Element)l.get(i)));
            }

            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < numEntries(); i++) {
              //Get a RosterEntry object for this index
              RosterEntry r = (RosterEntry) (RosterEntry) _list.get(i);

              //Extract the Comment field and create a new string for output
              String tempComment = r.getComment();
              String xmlComment = new String();

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
              String xmlDecoderComment = new String();

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
    }

    private boolean dirty = false;
    void setDirty(boolean b) {dirty = b;}
    boolean isDirty() {return dirty;}

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        if (dirty) log.error("Dispose invoked on dirty Roster");
    }

    /**
     * Store the roster in the default place, including making a backup if needed
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
     * update the in-memory Roster to be consistent with
     * the current roster file.  This removes the existing roster entries!
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

    static String getFileLocation() { return fileLocation; }

    /**
     * Absolute path to roster file location.
     * <P>
     * Default is in the prefsDir, but can be set to anything.
     * @see XmlFile.prefsDir()
     */
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
        for (int i=0; i<rarray.length; i++) rarray[i] =(RosterEntry) (_list.get(i));
        jmri.util.StringUtil.sortUpperCase(rarray);
        for (int i=0; i<rarray.length; i++) _list.set(i,rarray[i]);
        
        firePropertyChange("change", null, r);
    }
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Roster.class.getName());

}
