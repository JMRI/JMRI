package jmri.jmrit.decoderdefn;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// try to limit the JDOM to this class, so that others can manipulate...
/**
 * DecoderIndex represents a decoderIndex.xml file in memory.
 * <p>
 * This allows a program to navigate to various decoder descriptions without
 * having to manipulate files.
 * <p>
 * This class doesn't provide tools for defining the index; that's done
 * manually, or at least not done here.
 * <p>
 * Multiple DecoderIndexFile objects don't make sense, so we use an "instance"
 * member to navigate to a single one.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class DecoderIndexFile extends XmlFile {

    // fill in abstract members
    protected List<DecoderFile> decoderList = new ArrayList<>();

    public int numDecoders() {
        return decoderList.size();
    }

    int fileVersion = -1;

    // map mfg ID numbers from & to mfg names
    protected HashMap<String, String> _mfgIdFromNameHash = new HashMap<>();
    protected HashMap<String, String> _mfgNameFromIdHash = new HashMap<>();

    protected ArrayList<String> mMfgNameList = new ArrayList<>();

    public List<String> getMfgNameList() {
        return mMfgNameList;
    }

    public String mfgIdFromName(String name) {
        return _mfgIdFromNameHash.get(name);
    }

    public String mfgNameFromId(String name) {
        return _mfgNameFromIdHash.get(name);
    }

    /**
     * Get a List of decoders matching some information.
     *
     * @param mfg              decoder manufacturer
     * @param family           decoder family
     * @param decoderMfgID     NMRA decoder manufacturer ID
     * @param decoderVersionID decoder version ID
     * @param decoderProductID decoder product ID
     * @param model            decoder model
     * @return a list, possibly empty, of matching decoders
     */
    @Nonnull
    public List<DecoderFile> matchingDecoderList(String mfg, String family,
            String decoderMfgID, String decoderVersionID, String decoderProductID,
            String model) {
        return (matchingDecoderList(mfg, family, decoderMfgID, decoderVersionID, decoderProductID, model, null));
    }

    /**
     * Get a List of decoders matching some information.
     *
     * @param mfg              decoder manufacturer
     * @param family           decoder family
     * @param decoderMfgID     NMRA decoder manufacturer ID
     * @param decoderVersionID decoder version ID
     * @param decoderProductID decoder product ID
     * @param model            decoder model
     * @param developerID      developer ID
     * @return a list, possibly empty, of matching decoders
     */
    @Nonnull
    public List<DecoderFile> matchingDecoderList(String mfg, String family,
            String decoderMfgID, String decoderVersionID,
            String decoderProductID, String model, String developerID) {
        List<DecoderFile> l = new ArrayList<>();
        for (int i = 0; i < numDecoders(); i++) {
            if (checkEntry(i, mfg, family, decoderMfgID, decoderVersionID, decoderProductID, model, developerID)) {
                l.add(decoderList.get(i));
            }
        }
        return l;
    }

    /**
     * Get a JComboBox representing the choices that match some information.
     *
     * @param mfg              decoder manufacturer
     * @param family           decoder family
     * @param decoderMfgID     NMRA decoder manufacturer ID
     * @param decoderVersionID decoder version ID
     * @param decoderProductID decoder product ID
     * @param model            decoder model
     * @return a combo box populated with matching decoders
     */
    public JComboBox<String> matchingComboBox(String mfg, String family, String decoderMfgID, String decoderVersionID, String decoderProductID, String model) {
        List<DecoderFile> l = matchingDecoderList(mfg, family, decoderMfgID, decoderVersionID, decoderProductID, model);
        return jComboBoxFromList(l);
    }

    /**
     * Get a JComboBox made with the titles from a list of DecoderFile entries.
     *
     * @param l list of decoders
     * @return a combo box populated with the list
     */
    static public JComboBox<String> jComboBoxFromList(List<DecoderFile> l) {
        return new JComboBox<>(jComboBoxModelFromList(l));
    }

    /**
     * Get a new ComboBoxModel made with the titles from a list of DecoderFile
     * entries.
     *
     * @param l list of decoders
     * @return a combo box model populated with the list
     */
    static public javax.swing.ComboBoxModel<String> jComboBoxModelFromList(List<DecoderFile> l) {
        javax.swing.DefaultComboBoxModel<String> b = new javax.swing.DefaultComboBoxModel<>();
        for (int i = 0; i < l.size(); i++) {
            DecoderFile r = l.get(i);
            b.addElement(r.titleString());
        }
        return b;
    }

    /**
     * Get a DecoderFile from a "title" string, typically a selection in a
     * matching ComboBox.
     *
     * @param title the decoder title
     * @return the decoder file
     */
    public DecoderFile fileFromTitle(String title) {
        for (int i = numDecoders() - 1; i >= 0; i--) {
            DecoderFile r = decoderList.get(i);
            if (r.titleString().equals(title)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Check if an entry consistent with specific properties. A null String
     * entry always matches. Strings are used for convenience in GUI building.
     * Don't bother asking about the model number...
     *
     * @param i                index of entry
     * @param mfgName          decoder manufacturer
     * @param family           decoder family
     * @param mfgID            NMRA decoder manufacturer ID
     * @param decoderVersionID decoder version ID
     * @param decoderProductID decoder product ID
     * @param model            decoder model
     * @param developerID      developer ID
     * @return true if entry at i matches the other parameters; false otherwise
     */
    public boolean checkEntry(int i, String mfgName, String family, String mfgID,
            String decoderVersionID, String decoderProductID, String model,
            String developerID) {
        DecoderFile r = decoderList.get(i);
        if (mfgName != null && !mfgName.equals(r.getMfg())) {
            return false;
        }
        if (family != null && !family.equals(r.getFamily())) {
            return false;
        }
        if (mfgID != null && !mfgID.equals(r.getMfgID())) {
            return false;
        }
        if (model != null && !model.equals(r.getModel())) {
            return false;
        }
        // check version ID - no match if a range specified and out of range
        if (decoderVersionID != null) {
            int versionID = Integer.parseInt(decoderVersionID);
            if (!r.isVersion(versionID)) {
                return false;
            }
        }
        if (decoderProductID != null && !("," + r.getProductID() + ",").contains("," + decoderProductID + ",")) {
            return false;
        }
        if (developerID != null && !developerID.equals(r.getDeveloperID())) {
            if (!("," + r.getModelElement().getAttribute("developerID").getValue() + ",").contains("," + developerID + ",")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replace the managed instance with a new instance.
     */
    public synchronized static void resetInstance() {
        InstanceManager.getDefault().clear(DecoderIndexFile.class);
    }

    /**
     * Check whether the user's version of the decoder index file needs to be
     * updated; if it does, then forces the update.
     *
     * @return true is the index should be reloaded because it was updated
     * @throws org.jdom2.JDOMException if unable to parse decoder index
     * @throws java.io.IOException     if unable to read decoder index
     */
    static boolean updateIndexIfNeeded() throws org.jdom2.JDOMException, java.io.IOException {
        switch (FileUtil.findFiles(defaultDecoderIndexFilename(), ".").size()) {
            case 0:
                log.debug("creating decoder index");
                forceCreationOfNewIndex();
                return true; // no index exists, so create one
            case 1:
                return false; // only one index, so nothing to compare
            default:
                // multiple indexes, so continue with more specific checks
                break;
        }

        // get version from master index; if not found, give up
        String masterVersion = null;
        DecoderIndexFile masterXmlFile = new DecoderIndexFile();
        URL masterFile = FileUtil.findURL("xml/" + defaultDecoderIndexFilename(), FileUtil.Location.INSTALLED);
        if (masterFile == null) {
            return false;
        }
        log.debug("checking for master file at {}", masterFile);
        Element masterRoot = masterXmlFile.rootFromURL(masterFile);
        if (masterRoot.getChild("decoderIndex") != null) {
            if (masterRoot.getChild("decoderIndex").getAttribute("version") != null) {
                masterVersion = masterRoot.getChild("decoderIndex").getAttribute("version").getValue();
            }
            log.debug("master version found, is {}", masterVersion);
        } else {
            return false;
        }

        // get from user index.  Unless they are equal, force an update.
        // note we find this file via the search path; if not exists, so that
        // the master is found, we still do the right thing (nothing).
        String userVersion = null;
        DecoderIndexFile userXmlFile = new DecoderIndexFile();
        log.debug("checking for user file at {}", defaultDecoderIndexFilename());
        Element userRoot = userXmlFile.rootFromName(defaultDecoderIndexFilename());
        if (userRoot.getChild("decoderIndex") != null) {
            if (userRoot.getChild("decoderIndex").getAttribute("version") != null) {
                userVersion = userRoot.getChild("decoderIndex").getAttribute("version").getValue();
            }
            log.debug("user version found, is {}", userVersion);
        }
        if (masterVersion != null && masterVersion.equals(userVersion)) {
            return false;
        }

        // force the update, with the version number located earlier is available
        log.debug("forcing update of decoder index due to {} and {}", masterVersion, userVersion);
        forceCreationOfNewIndex();
        // and force it to be used
        return true;

    }

    /**
     * Force creation of a new user index without incrementing version
     */
    static public void forceCreationOfNewIndex() {
        forceCreationOfNewIndex(false);
    }

    /**
     * Force creation of a new user index.
     *
     * @param increment true to increment the version of the decoder index
     */
    static public void forceCreationOfNewIndex(boolean increment) {
        log.info("update decoder index");
        // make sure we're using only the default manufacturer info
        // to keep from propagating wrong, old stuff
        File oldfile = new File(FileUtil.getUserFilesPath() + "decoderIndex.xml");
        if (oldfile.exists()) {
            log.debug("remove existing user decoderIndex.xml file");
            if (!oldfile.delete()) // delete file, check for success
            {
                log.error("Failed to delete old index file");
            }
            // force read from distributed file on next access
            resetInstance();
        }

        // create an array of file names from decoders dir in preferences, count entries
        ArrayList<String> al = new ArrayList<>();
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + DecoderFile.fileLocation);
        File fp = new File(FileUtil.getUserFilesPath() + DecoderFile.fileLocation);
    
        if (fp.exists()) {
            String[] list = fp.list();
            if (list !=null) {
                for (String sp : list) {
                    if (sp.endsWith(".xml") || sp.endsWith(".XML")) {
                        al.add(sp);
                    }
                }
            }
        } else {
            log.warn("{}decoders was missing, though tried to create it", FileUtil.getUserFilesPath());
        }
        // create an array of file names from xml/decoders, count entries
        String[] fileList = (new File(XmlFile.xmlDir() + DecoderFile.fileLocation)).list();
        if (fileList != null) {
            for (String sx : fileList ) {
                if (sx.endsWith(".xml") || sx.endsWith(".XML")) {
                    // Valid name.  Does it exist in preferences xml/decoders?
                    if (!al.contains(sx)) {
                        // no, include it!
                        al.add(sx);
                    }
                }
            }
        } else {
            log.error("Could not access decoder definition directory {}", XmlFile.xmlDir() + DecoderFile.fileLocation);
        }
        // copy the decoder entries to the final array
        String sbox[] = al.toArray(new String[al.size()]);

        //the resulting array is now sorted on file-name to make it easier
        // for humans to read
        Arrays.sort(sbox);

        // create a new decoderIndex
        DecoderIndexFile index = new DecoderIndexFile();

        // For user operations the existing version is used, so that a new master file
        // with a larger one will force an update
        if (increment) {
            index.fileVersion = InstanceManager.getDefault(DecoderIndexFile.class).fileVersion + 2;
        } else {
            index.fileVersion = InstanceManager.getDefault(DecoderIndexFile.class).fileVersion;
        }

        // write it out
        try {
            index.writeFile("decoderIndex.xml", InstanceManager.getDefault(DecoderIndexFile.class), sbox);
        } catch (java.io.IOException ex) {
            log.error("Error writing new decoder index file: {}", ex.getMessage());
        }
    }

    /**
     * Read the contents of a decoderIndex XML file into this object. Note that
     * this does not clear any existing entries; reset the instance to do that.
     *
     * @param name the name of the decoder index file
     * @throws org.jdom2.JDOMException if unable to parse to decoder index file
     * @throws java.io.IOException     if unable to read decoder index file
     */
    void readFile(String name) throws org.jdom2.JDOMException, java.io.IOException {
        if (log.isDebugEnabled()) {
            log.debug("readFile " + name);
        }

        // read file, find root
        Element root = rootFromName(name);

        // decode type, invoke proper processing routine if a decoder file
        if (root.getChild("decoderIndex") != null) {
            if (root.getChild("decoderIndex").getAttribute("version") != null) {
                fileVersion = Integer.parseInt(root.getChild("decoderIndex")
                        .getAttribute("version")
                        .getValue()
                );
            }
            log.debug("found fileVersion of {}", fileVersion);
            readMfgSection(root.getChild("decoderIndex"));
            readFamilySection(root.getChild("decoderIndex"));
        } else {
            log.error("Unrecognized decoderIndex file contents in file: {}", name);
        }
    }

    void readMfgSection(Element decoderIndex) {
        Element mfgList = decoderIndex.getChild("mfgList");
        if (mfgList != null) {

            Attribute a;
            a = mfgList.getAttribute("nmraListDate");
            if (a != null) {
                nmraListDate = a.getValue();
            }
            a = mfgList.getAttribute("updated");
            if (a != null) {
                updated = a.getValue();
            }
            a = mfgList.getAttribute("lastadd");
            if (a != null) {
                lastAdd = a.getValue();
            }

            List<Element> l = mfgList.getChildren("manufacturer");
            if (log.isDebugEnabled()) {
                log.debug("readMfgSection sees " + l.size() + " children");
            }
            for (int i = 0; i < l.size(); i++) {
                // handle each entry
                Element el = l.get(i);
                String mfg = el.getAttribute("mfg").getValue();
                mMfgNameList.add(mfg);
                Attribute attr = el.getAttribute("mfgID");
                if (attr != null) {
                    _mfgIdFromNameHash.put(mfg, attr.getValue());
                    _mfgNameFromIdHash.put(attr.getValue(), mfg);
                }
            }
        } else {
            log.warn("no mfgList found in decoderIndexFile");
        }
    }

    void readFamilySection(Element decoderIndex) {
        Element familyList = decoderIndex.getChild("familyList");
        if (familyList != null) {

            List<Element> l = familyList.getChildren("family");
            log.trace("readFamilySection sees {} children", l.size());
            for (int i = 0; i < l.size(); i++) {
                // handle each entry
                Element el = l.get(i);
                readFamily(el);
            }
        } else {
            log.warn("no familyList found in decoderIndexFile");
        }
    }

    void readFamily(Element family) {
        Attribute attr;
        String filename = family.getAttribute("file").getValue();
        String parentLowVersID = ((attr = family.getAttribute("lowVersionID")) != null ? attr.getValue() : null);
        String parentHighVersID = ((attr = family.getAttribute("highVersionID")) != null ? attr.getValue() : null);
        String ParentReplacementFamilyName = ((attr = family.getAttribute("replacementFamily")) != null ? attr.getValue() : null);
        String familyName = ((attr = family.getAttribute("name")) != null ? attr.getValue() : null);
        String mfg = ((attr = family.getAttribute("mfg")) != null ? attr.getValue() : null);
        String developer = ((attr = family.getAttribute("developerID")) != null ? attr.getValue() : null);
        String mfgID = null;
        if (mfg != null) {
            mfgID = mfgIdFromName(mfg);
        } else {
            log.error("Did not find required mfg attribute, may not find proper manufacturer");
        }

        List<Element> l = family.getChildren("model");
        log.trace("readFamily sees {} children", l.size());
        Element modelElement;
        if (l.size() <= 0) {
            log.error("Did not find at least one model in the {} family", familyName);
            modelElement = null;
        } else {
            modelElement = l.get(0);
        }

        // Record the family as a specific model, which allows you to select the
        // family as a possible thing to program
        DecoderFile vFamilyDecoderFile
                = new DecoderFile(mfg, mfgID, familyName,
                        parentLowVersID, parentHighVersID,
                        familyName,
                        filename,
                        (developer != null) ? developer : "-1",
                        -1, -1, modelElement,
                        ParentReplacementFamilyName, ParentReplacementFamilyName); // numFns, numOuts, XML element equal
        // to the first decoder
        decoderList.add(vFamilyDecoderFile);

        // record each of the decoders
        for (int i = 0; i < l.size(); i++) {
            // handle each entry by creating a DecoderFile object containing all it knows
            Element decoder = l.get(i);
            String loVersID = ((attr = decoder.getAttribute("lowVersionID")) != null ? attr.getValue() : parentLowVersID);
            String hiVersID = ((attr = decoder.getAttribute("highVersionID")) != null ? attr.getValue() : parentHighVersID);
            String replacementModelName = ((attr = decoder.getAttribute("replacementModel")) != null ? attr.getValue() : null);
            String replacementFamilyName = ((attr = decoder.getAttribute("replacementFamily")) != null ? attr.getValue() : ParentReplacementFamilyName);
            int numFns = ((attr = decoder.getAttribute("numFns")) != null ? Integer.parseInt(attr.getValue()) : -1);
            int numOuts = ((attr = decoder.getAttribute("numOuts")) != null ? Integer.parseInt(attr.getValue()) : -1);
            String devId = ((attr = decoder.getAttribute("developerId")) != null ? attr.getValue() : "-1");
            DecoderFile df = new DecoderFile(mfg, mfgID,
                    ((attr = decoder.getAttribute("model")) != null ? attr.getValue() : null),
                    loVersID, hiVersID, familyName, filename, devId, numFns, numOuts, decoder,
                    replacementModelName, replacementFamilyName);
            // and store it
            decoderList.add(df);
            // if there are additional version numbers defined, handle them too
            List<Element> vcodes = decoder.getChildren("versionCV");
            for (int j = 0; j < vcodes.size(); j++) {
                // for each versionCV element
                Element vcv = vcodes.get(j);
                String vLoVersID = ((attr = vcv.getAttribute("lowVersionID")) != null ? attr.getValue() : loVersID);
                String vHiVersID = ((attr = vcv.getAttribute("highVersionID")) != null ? attr.getValue() : hiVersID);
                df.setVersionRange(vLoVersID, vHiVersID);
            }
        }
    }

    public void writeFile(String name, DecoderIndexFile oldIndex, String files[]) throws java.io.IOException {
        if (log.isDebugEnabled()) {
            log.debug("writeFile " + name);
        }
        // This is taken in large part from "Java and XML" page 368
        File file = new File(FileUtil.getUserFilesPath() + name);

        // create root element and document
        Element root = new Element("decoderIndex-config");
        root.setAttribute("noNamespaceSchemaLocation",
                "http://jmri.org/xml/schema/decoder-4-15-2.xsd",
                org.jdom2.Namespace.getNamespace("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance"));

        Document doc = newDocument(root);

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/DecoderID.xsl"?>
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation + "DecoderID.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0, p);

        // add top-level elements
        Element index;
        root.addContent(index = new Element("decoderIndex"));
        index.setAttribute("version", Integer.toString(fileVersion));
        log.debug("version written to file as {}", fileVersion);

        // add mfg list from existing DecoderIndexFile item
        Element mfgList = new Element("mfgList");
        // copy dates from original mfgList element
        if (oldIndex.nmraListDate != null) {
            mfgList.setAttribute("nmraListDate", oldIndex.nmraListDate);
        }
        if (oldIndex.updated != null) {
            mfgList.setAttribute("updated", oldIndex.updated);
        }
        if (oldIndex.lastAdd != null) {
            mfgList.setAttribute("lastadd", oldIndex.lastAdd);
        }

        // We treat "NMRA" special...
        Element mfg = new Element("manufacturer");
        mfg.setAttribute("mfg", "NMRA");
        mfg.setAttribute("mfgID", "999");
        mfgList.addContent(mfg);
        // start working on the rest of the entries
        List<String> keys = new ArrayList<>(oldIndex._mfgIdFromNameHash.keySet());
        Collections.sort(keys);
        for (Object item : keys) {
            String mfgName = (String) item;
            if (!mfgName.equals("NMRA")) {
                mfg = new Element("manufacturer");
                mfg.setAttribute("mfg", mfgName);
                mfg.setAttribute("mfgID", oldIndex._mfgIdFromNameHash.get(mfgName));
                mfgList.addContent(mfg);
            }
        }

        // add family list by scanning files
        Element familyList = new Element("familyList");
        for (String fileName : files) {
            DecoderFile d = new DecoderFile();
            try {
                Element droot = d.rootFromName(DecoderFile.fileLocation + fileName);
                Element family = droot.getChild("decoder").getChild("family").clone();
                family.setAttribute("file", fileName);
                familyList.addContent(family);
            } catch (org.jdom2.JDOMException exj) {
                log.error("could not parse {}: {}", fileName, exj.getMessage());
            } catch (java.io.FileNotFoundException exj) {
                log.error("could not read {}: {}", fileName, exj.getMessage());
            } catch (IOException exj) {
                log.error("other exception while dealing with {}: {}", fileName, exj.getMessage());
            } catch (Exception exq) {
                log.error("exception reading {}", fileName, exq);
                throw exq;
            }
        }

        index.addContent(mfgList);
        index.addContent(familyList);

        writeXML(file, doc);

        // force a read of the new file next time
        resetInstance();
    }

    String nmraListDate = null;
    String updated = null;
    String lastAdd = null;

    /**
     * Get the filename for the default decoder index file, including location.
     * This is here to allow easy override in tests.
     *
     * @return the complete path to the decoder index
     */
    protected static String defaultDecoderIndexFilename() {
        return DECODER_INDEX_FILE_NAME;
    }

    static final protected String DECODER_INDEX_FILE_NAME = "decoderIndex.xml";
    private final static Logger log = LoggerFactory.getLogger(DecoderIndexFile.class);

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(DecoderIndexFile.class)) {
                // create and load
                DecoderIndexFile instance = new DecoderIndexFile();
                log.debug("DecoderIndexFile creating instance");
                try {
                    instance.readFile(defaultDecoderIndexFilename());
                } catch (IOException | JDOMException e) {
                    log.error("Exception during decoder index reading: ", e);
                }
                // see if needs to be updated
                try {
                    if (updateIndexIfNeeded()) {
                        try {
                            instance = new DecoderIndexFile();
                            instance.readFile(defaultDecoderIndexFilename());
                        } catch (IOException | JDOMException e) {
                            log.error("Exception during decoder index reload: ", e);
                        }
                    }
                } catch (IOException | JDOMException e) {
                    log.error("Exception during decoder index update: ", e);
                }
                log.debug("DecoderIndexFile returns instance {}", instance);
                return instance;
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(DecoderIndexFile.class);
            return set;
        }
    }
}
