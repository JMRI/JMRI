package jmri.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jmri.Application;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.implementation.DefaultIdTag;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation for the Internal {@link jmri.IdTagManager} interface.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class DefaultIdTagManager extends AbstractManager
        implements IdTagManager {

    private static boolean _initialised = false;
    private static boolean _loading = false;
    private static boolean _storeState = false;
    private static boolean _useFastClock = false;

    public DefaultIdTagManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.IDTAGS;
    }

    @Override
    public boolean isInitialised() {
        return _initialised;
    }

    @Override
    public void init() {
        log.debug("init called");
        if (!_initialised && !_loading) {
            log.debug("Initialising");
            // Load when created
            _loading = true;
            IdTagManagerXml.instance().load();
            _loading = false;

            // Create shutdown task to save
            log.debug("Register ShutDown task");
            InstanceManager.shutDownManagerInstance().
                    register(new jmri.implementation.AbstractShutDownTask("Writing IdTags") { // NOI18N
                        @Override
                        public boolean execute() {
                            // Save IdTag details prior to exit, if necessary
                            log.debug("Start writing IdTag details...");
                            try {
                                ((DefaultIdTagManager) InstanceManager.getDefault(IdTagManager.class)).writeIdTagDetails();
                                //new jmri.managers.DefaultIdTagManager().writeIdTagDetails();
                            } catch (java.io.IOException ioe) {
                                log.error("Exception writing IdTags: " + ioe);
                            }

                            // continue shutdown
                            return true;
                        }
                    });
            _initialised = true;
        }
    }

    /**
     * Don't want to store this information
     */
    @Override
    protected void registerSelf() {
    }

    @Override
    public char typeLetter() {
        return 'D';
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public IdTag provideIdTag(String name) {
        if (!_initialised && !_loading) {
            init();
        }
        IdTag t = getIdTag(name);
        if (t != null) {
            return t;
        }
        if (name.startsWith(getSystemPrefix() + typeLetter())) {
            return newIdTag(name, null);
        } else {
            return newIdTag(makeSystemName(name), null);
        }
    }

    @Override
    public IdTag getIdTag(String name) {
        if (!_initialised && !_loading) {
            init();
        }

        IdTag t = getBySystemName(makeSystemName(name));
        if (t != null) {
            return t;
        }

        t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    @Override
    public IdTag getBySystemName(String name) {
        if (!_initialised && !_loading) {
            init();
        }
        return (IdTag) _tsys.get(name);
    }

    @Override
    public IdTag getByUserName(String key) {
        if (!_initialised && !_loading) {
            init();
        }
        return (IdTag) _tuser.get(key);
    }

    @Override
    public IdTag getByTagID(String tagID) {
        if (!_initialised && !_loading) {
            init();
        }
        return getBySystemName(makeSystemName(tagID));
    }

    protected IdTag createNewIdTag(String systemName, String userName) {
        // we've decided to enforce that IdTag system
        // names start with ID by prepending if not present
        if (!systemName.startsWith("ID")) // NOI18N
        {
            systemName = "ID" + systemName; // NOI18N
        }
        return new DefaultIdTag(systemName, userName);
    }

    @Override
    public IdTag newIdTag(String systemName, String userName) {
        if (!_initialised && !_loading) {
            init();
        }
        if (log.isDebugEnabled()) {
            log.debug("new IdTag:"
                    + ((systemName == null) ? "null" : systemName) // NOI18N
                    + ";" + ((userName == null) ? "null" : userName)); // NOI18N
        }
        if (systemName == null) {
            log.error("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName)); // NOI18N
            throw new IllegalArgumentException("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName));
        }
        // return existing if there is one
        IdTag s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + s.getSystemName() + ")");
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found IdTag via system name (" + systemName
                        + ") with non-null user name (" + userName + ")"); // NOI18N
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewIdTag(systemName, userName);

        // save in the maps
        register(s);

        // if that failed, blame it on the input arguements
        if (s == null) {
            throw new IllegalArgumentException();
        }

        return s;
    }

    @Override
    public void register(NamedBean s) {
        super.register(s);
        IdTagManagerXml.instance().setDirty(true);
    }

    @Override
    public void deregister(NamedBean s) {
        super.deregister(s);
        IdTagManagerXml.instance().setDirty(true);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        super.propertyChange(e);
        IdTagManagerXml.instance().setDirty(true);
    }

    public void writeIdTagDetails() throws java.io.IOException {
        IdTagManagerXml.instance().store();
        log.debug("...done writing IdTag details");
    }

    @Override
    public void setStateStored(boolean state) {
        if (state != _storeState) {
            IdTagManagerXml.instance().setDirty(true);
        }
        _storeState = state;
    }

    @Override
    public boolean isStateStored() {
        return _storeState;
    }

    @Override
    public void setFastClockUsed(boolean fastClock) {
        if (fastClock != _useFastClock) {
            IdTagManagerXml.instance().setDirty(true);
        }
        _useFastClock = fastClock;
    }

    @Override
    public boolean isFastClockUsed() {
        return _useFastClock;
    }

    @Override
    public List<IdTag> getTagsForReporter(Reporter reporter, long threshold) {
        List<IdTag> out = new ArrayList<IdTag>();
        Date lastWhenLastSeen = new Date(0);

        // First create a list of all tags seen by specified reporter
        // and record the time most recently seen
        for (NamedBean n : _tsys.values()) {
            IdTag t = (IdTag) n;
            if (t.getWhereLastSeen() == reporter) {
                out.add(t);
                if (t.getWhenLastSeen().after(lastWhenLastSeen)) {
                    lastWhenLastSeen = t.getWhenLastSeen();
                }
            }
        }

        if (out.size() > 0) {
            // Calculate the threshold time based on the most recently seen tag
            Date thresholdTime = new Date(lastWhenLastSeen.getTime() - threshold);

            // Now remove from the list all tags seen prior to the threshold time
            for (IdTag t : out) {
                if (t.getWhenLastSeen().before(thresholdTime)) {
                    out.remove(t);
                }
            }
        }

        return out;
    }

    /**
     * Concrete implementation of abstract {@link jmri.jmrit.XmlFile} for
     * internal use
     */
    static class IdTagManagerXml extends XmlFile {

        /**
         * Record the single instance
         */
        private static IdTagManagerXml _instance = null;

        private static boolean _loaded = false;

        private boolean _dirty = false;

        public static synchronized IdTagManagerXml instance() {
            if (_instance == null) {
                if (log.isDebugEnabled()) {
                    log.debug("IdTagManagerXml creating instance");
                }

                // Create instance and load
                _instance = new IdTagManagerXml();
                _instance.load();
            }
            return _instance;
        }

        public synchronized void setDirty(boolean dirty) {
            _dirty = dirty;
            if (log.isDebugEnabled()) {
                log.debug("Set dirty to " + (_dirty ? "True" : "False"));
            }
        }

        protected void load() {
            if (!_loaded) {
                log.debug("Loading...");
                try {
                    readFile(getDefaultIdTagFileName());
                    _loaded = true;
                    setDirty(false);
                } catch (Exception ex) {
                    log.error("Exception during IdTag file reading: " + ex);
                }
            }
        }

        protected void store() throws java.io.IOException {
            if (_dirty) {
                log.debug("Storing...");
                log.debug("Using file: " + getDefaultIdTagFileName());
                createFile(getDefaultIdTagFileName(), true);
                try {
                    writeFile(getDefaultIdTagFileName());
                } catch (FileNotFoundException ex) {
                    log.error("File not found while writing IdTag file, may not be complete: " + ex);
                }
            } else {
                log.debug("Not dirty - no need to store");
            }
        }

        private File createFile(String fileName, boolean backup) {
            if (backup) {
                makeBackupFile(fileName);
            }

            File file = null;
            try {
                if (!checkFile(fileName)) {
                    // The file does not exist, create it before writing
                    file = new File(fileName);
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        if (!parentDir.mkdir()) {
                            log.error("Directory wasn't created");
                        }
                    }
                    if (file.createNewFile()) {
                        log.debug("New file created");
                    }
                } else {
                    file = new File(fileName);
                }
            } catch (java.io.IOException ex) {
                log.error("Exception while creating IdTag file, may not be complete: " + ex);
            }
            return file;
        }

        private void writeFile(String fileName) throws FileNotFoundException, java.io.IOException {
            if (log.isDebugEnabled()) {
                log.debug("writeFile " + fileName);
            }
            // This is taken in large part from "Java and XML" page 368
            File file = findFile(fileName);
            if (file == null) {
                file = new File(fileName);
            }
            // Create root element
            Element root = new Element("idtagtable");              // NOI18N
            root.setAttribute("noNamespaceSchemaLocation", // NOI18N
                    "http://jmri.org/xml/schema/idtags.xsd", // NOI18N
                    org.jdom2.Namespace.getNamespace("xsi", // NOI18N
                            "http://www.w3.org/2001/XMLSchema-instance")); // NOI18N
            Document doc = newDocument(root);

            // add XSLT processing instruction
            java.util.Map<String, String> m = new java.util.HashMap<String, String>();
            m.put("type", "text/xsl");                                                // NOI18N
            m.put("href", xsltLocation + "idtags.xsl");                                 // NOI18N
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
            doc.addContent(0, p);

            IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);

            Element values;

            // Store configuration
            root.addContent(values = new Element("configuration"));                                          // NOI18N
            values.addContent(new Element("storeState").addContent(manager.isStateStored() ? "yes" : "no"));     // NOI18N
            values.addContent(new Element("useFastClock").addContent(manager.isFastClockUsed() ? "yes" : "no")); // NOI18N

            // Loop through RfidTags
            root.addContent(values = new Element("idtags")); // NOI18N
            List<String> idTagList = manager.getSystemNameList();
            for (int i = 0; i < idTagList.size(); i++) {
                IdTag t = manager.getBySystemName(idTagList.get(i));
                if (log.isDebugEnabled()) {
                    log.debug("Writing IdTag: " + t.getSystemName());
                }
                values.addContent(t.store(manager.isStateStored()));
            }
            writeXML(file, doc);
        }

        private void readFile(String fileName) throws org.jdom2.JDOMException, java.io.IOException {
            // Check file exists
            if (findFile(fileName) == null) {
                log.debug(fileName + " file could not be found");
                return;
            }

            // Find root
            Element root = rootFromName(fileName);
            if (root == null) {
                log.debug(fileName + " file could not be read");
                return;
            }

            IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);

            // First read configuration
            if (root.getChild("configuration") != null) { // NOI18N
                @SuppressWarnings("unchecked")
                List<Element> l = root.getChild("configuration").getChildren(); // NOI18N
                if (log.isDebugEnabled()) {
                    log.debug("readFile sees " + l.size() + " configurations");
                }
                for (int i = 0; i < l.size(); i++) {
                    Element e = l.get(i);
                    if (log.isDebugEnabled()) {
                        log.debug("Configuration " + e.getName() + " value " + e.getValue());
                    }
                    if (e.getName().equals("storeState")) { // NOI18N
                        manager.setStateStored(e.getValue().equals("yes") ? true : false); // NOI18N
                    }
                    if (e.getName().equals("useFastClock")) { // NOI18N
                        manager.setFastClockUsed(e.getValue().equals("yes") ? true : false); // NOI18N
                    }
                }
            }

            // Now read tag information
            if (root.getChild("idtags") != null) { // NOI18N
                @SuppressWarnings("unchecked")
                List<Element> l = root.getChild("idtags").getChildren("idtag"); // NOI18N
                if (log.isDebugEnabled()) {
                    log.debug("readFile sees " + l.size() + " idtags");
                }
                for (int i = 0; i < l.size(); i++) {
                    Element e = l.get(i);
                    String systemName = e.getChild("systemName").getText(); // NOI18N
                    IdTag t = manager.provideIdTag(systemName);
                    t.load(e);
                }
            }
        }

        public String getDefaultIdTagFileName() {
            return getFileLocation() + getIdTagDirectoryName() + File.separator + getIdTagFileName();
        }

        private static String idTagDirectoryName = "idtags"; // NOI18N

        public String getIdTagDirectoryName() {
            return idTagDirectoryName;
        }

        private String idTagBaseFileName = "IdTags.xml"; // NOI18N

        public String getIdTagFileName() {
            return Application.getApplicationName() + idTagBaseFileName;
        }

        /**
         * Absolute path to location of IdTag files.
         *
         * @return path to location
         */
        public static String getFileLocation() {
            return fileLocation;
        }

        private static String fileLocation = FileUtil.getUserFilesPath();

        private static final Logger log = LoggerFactory.getLogger(DefaultIdTagManager.IdTagManagerXml.class.getName());

    }

    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameReporter");
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultIdTagManager.class.getName());

}
