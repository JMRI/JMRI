package jmri.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.Application;
import jmri.Disposable;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.implementation.DefaultIdTag;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation for the Internal {@link jmri.IdTagManager} interface.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class DefaultIdTagManager extends AbstractManager<IdTag> implements IdTagManager, Disposable {

    private boolean dirty = false;
    private boolean initialised = false;
    private boolean loading = false;
    private boolean storeState = false;
    private boolean useFastClock = false;
    private ShutDownTask shutDownTask = null;

    public DefaultIdTagManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.IDTAGS;
    }

    @Override
    public boolean isInitialised() {
        return initialised;
    }

    @Override
    public void init() {
        log.debug("init called");
        if (!initialised && !loading) {
            log.debug("Initialising");
            // Load when created
            loading = true;
            new IdTagManagerXml().load();
            loading = false;

            // Create shutdown task to save
            log.debug("Register ShutDown task");
            if (this.shutDownTask == null) {
                InstanceManager.getOptionalDefault(ShutDownManager.class).ifPresent((sdm) -> {
                    this.shutDownTask = new jmri.implementation.AbstractShutDownTask("Writing IdTags") { // NOI18N
                        @Override
                        public boolean execute() {
                            // Save IdTag details prior to exit, if necessary
                            log.debug("Start writing IdTag details...");
                            try {
                                ((DefaultIdTagManager) InstanceManager.getDefault(IdTagManager.class)).writeIdTagDetails();
                                //new jmri.managers.DefaultIdTagManager().writeIdTagDetails();
                            } catch (java.io.IOException ioe) {
                                log.error("Exception writing IdTags: {}", (Object) ioe);
                            }

                            // continue shutdown
                            return true;
                        }
                    };
                    sdm.register(this.shutDownTask);
                });
            }
            initialised = true;
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
    public IdTag provideIdTag(String name) throws IllegalArgumentException {
        if (!initialised && !loading) {
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
        if (!initialised && !loading) {
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
        if (!initialised && !loading) {
            init();
        }
        return _tsys.get(name);
    }

    @Override
    public IdTag getByUserName(String key) {
        if (!initialised && !loading) {
            init();
        }
        return _tuser.get(key);
    }

    @Override
    public IdTag getByTagID(String tagID) {
        if (!initialised && !loading) {
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
    public IdTag newIdTag(@Nonnull String systemName, @CheckForNull String userName) {
        if (!initialised && !loading) {
            init();
        }
        log.debug("new IdTag:{};{}", systemName, (userName == null) ? "null" : userName); // NOI18N
        Objects.requireNonNull(systemName, "SystemName cannot be null.");

        // return existing if there is one
        IdTag s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})", userName, systemName, s.getSystemName());
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found IdTag via system name ({}) with non-null user name ({})", systemName, userName); // NOI18N
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
    public void register(IdTag s) {
        super.register(s);
        this.setDirty(true);
    }

    @Override
    public void deregister(IdTag s) {
        super.deregister(s);
        this.setDirty(true);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        super.propertyChange(e);
        this.setDirty(true);
    }

    public void writeIdTagDetails() throws java.io.IOException {
        if (this.dirty) {
            new IdTagManagerXml().store();
            this.dirty = false;
            log.debug("...done writing IdTag details");
        }
    }

    @Override
    public void setStateStored(boolean state) {
        if (state != storeState) {
            this.setDirty(true);
        }
        storeState = state;
    }

    @Override
    public boolean isStateStored() {
        return storeState;
    }

    @Override
    public void setFastClockUsed(boolean fastClock) {
        if (fastClock != useFastClock) {
            this.setDirty(true);
        }
        useFastClock = fastClock;
    }

    @Override
    public boolean isFastClockUsed() {
        return useFastClock;
    }

    @Override
    public List<IdTag> getTagsForReporter(Reporter reporter, long threshold) {
        List<IdTag> out = new ArrayList<>();
        Date lastWhenLastSeen = new Date(0);

        // First create a list of all tags seen by specified reporter
        // and record the time most recently seen
        for (IdTag n : _tsys.values()) {
            IdTag t = n;
            if (t.getWhereLastSeen() == reporter) {
                out.add(t);
                Date tagLastSeen = t.getWhenLastSeen();
                if (tagLastSeen != null && tagLastSeen.after(lastWhenLastSeen)) {
                    lastWhenLastSeen = tagLastSeen;
                }
            }
        }

        // Calculate the threshold time based on the most recently seen tag
        Date thresholdTime = new Date(lastWhenLastSeen.getTime() - threshold);

        // Now remove from the list all tags seen prior to the threshold time
        out.removeIf((t) -> {
            Date tagLastSeen = t.getWhenLastSeen();
            return tagLastSeen == null || tagLastSeen.before(thresholdTime);
        });

        return out;
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(ShutDownManager.class).ifPresent((sdm) -> {
            sdm.deregister(this.shutDownTask);
        });
        super.dispose();
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameReporter");
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultIdTagManager.class);

    /**
     * Concrete implementation of abstract {@link jmri.jmrit.XmlFile} for
     * internal use
     */
    class IdTagManagerXml extends XmlFile {

        protected void load() {
            log.debug("Loading...");
            try {
                readFile(getDefaultIdTagFileName());
                setDirty(false);
            } catch (JDOMException | IOException ex) {
                log.error("Exception during IdTag file reading: {}", (Object) ex);
            }
        }

        protected void store() throws java.io.IOException {
            if (dirty) {
                log.debug("Storing...");
                log.debug("Using file: {}", getDefaultIdTagFileName());
                createFile(getDefaultIdTagFileName(), true);
                try {
                    writeFile(getDefaultIdTagFileName());
                } catch (FileNotFoundException ex) {
                    log.error("File not found while writing IdTag file, may not be complete: {}", (Object) ex);
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
                log.error("Exception while creating IdTag file, may not be complete: {}", (Object) ex);
            }
            return file;
        }

        private void writeFile(String fileName) throws FileNotFoundException, java.io.IOException {
            log.debug("writeFile {}", fileName);
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
            java.util.Map<String, String> m = new java.util.HashMap<>();
            m.put("type", "text/xsl");                                                // NOI18N
            m.put("href", xsltLocation + "idtags.xsl");                                 // NOI18N
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
            doc.addContent(0, p);

            IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);

            Element values;

            // Store configuration
            root.addContent(values = new Element("configuration"));                                              // NOI18N
            values.addContent(new Element("storeState").addContent(manager.isStateStored() ? "yes" : "no"));     // NOI18N
            values.addContent(new Element("useFastClock").addContent(manager.isFastClockUsed() ? "yes" : "no")); // NOI18N

            // Loop through RfidTags
            root.addContent(values = new Element("idtags")); // NOI18N
            List<String> idTagList = manager.getSystemNameList();
            for (int i = 0; i < idTagList.size(); i++) {
                IdTag t = manager.getBySystemName(idTagList.get(i));
                if (t != null) {
                    log.debug("Writing IdTag: {}", t.getSystemName());
                    values.addContent(t.store(manager.isStateStored()));
                }
            }
            writeXML(file, doc);
        }

        private void readFile(String fileName) throws org.jdom2.JDOMException, java.io.IOException, IllegalArgumentException {
            // Check file exists
            if (findFile(fileName) == null) {
                log.debug("{} file could not be found", fileName);
                return;
            }

            // Find root
            Element root = rootFromName(fileName);
            if (root == null) {
                log.debug("{} file could not be read", fileName);
                return;
            }

            IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);

            // First read configuration
            if (root.getChild("configuration") != null) { // NOI18N
                @SuppressWarnings("unchecked")
                List<Element> l = root.getChild("configuration").getChildren(); // NOI18N
                log.debug("readFile sees {} configurations", l.size());
                for (int i = 0; i < l.size(); i++) {
                    Element e = l.get(i);
                    log.debug("Configuration {} value {}", e.getName(), e.getValue());
                    if (e.getName().equals("storeState")) { // NOI18N
                        manager.setStateStored(e.getValue().equals("yes")); // NOI18N
                    }
                    if (e.getName().equals("useFastClock")) { // NOI18N
                        manager.setFastClockUsed(e.getValue().equals("yes")); // NOI18N
                    }
                }
            }

            // Now read tag information
            if (root.getChild("idtags") != null) { // NOI18N
                @SuppressWarnings("unchecked")
                List<Element> l = root.getChild("idtags").getChildren("idtag"); // NOI18N
                log.debug("readFile sees {} idtags", l.size());
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

        private static final String IDTAG_DIRECTORY_NAME = "idtags"; // NOI18N

        public String getIdTagDirectoryName() {
            return IDTAG_DIRECTORY_NAME;
        }

        private static final String IDTAG_BASE_FILENAME = "IdTags.xml"; // NOI18N

        public String getIdTagFileName() {
            return Application.getApplicationName() + IDTAG_BASE_FILENAME;
        }

        /**
         * Absolute path to location of IdTag files.
         *
         * @return path to location
         */
        public String getFileLocation() {
            return FILE_LOCATION;
        }

        private final String FILE_LOCATION = FileUtil.getUserFilesPath();

        private final Logger log = LoggerFactory.getLogger(DefaultIdTagManager.IdTagManagerXml.class);

    }

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(IdTagManager.class)) {
                return new DefaultIdTagManager();
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(IdTagManager.class);
            return set;
        }
    }

}
