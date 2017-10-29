package jmri.configurexml;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.revhistory.FileHistory;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the mechanisms for storing an entire layout configuration to XML.
 * "Layout" refers to the hardware: Specific communication systems, etc.
 *
 * @see <A HREF="package-summary.html">Package summary for details of the
 * overall structure</A>
 * @author Bob Jacobsen Copyright (c) 2002, 2008
 */
public class ConfigXmlManager extends jmri.jmrit.XmlFile
        implements jmri.ConfigureManager {

    /**
     * Define the current schema version string for the layout-config schema.
     * See the <A HREF="package-summary.html#schema">Schema versioning
     * discussion</a>. Also controls the stylesheet file version.
     */
    static final public String schemaVersion = "-2-9-6";

    public ConfigXmlManager() {
    }

    @Override
    public void registerConfig(Object o) {
        registerConfig(o, 50);
    }

    @Override
    public void registerPref(Object o) {
        // skip if already present, leaving in original order
        if (plist.contains(o)) {
            return;
        }
        if (log.isDebugEnabled()) {
            confirmAdapterAvailable(o);
        }

        // and add to list
        plist.add(o);
    }

    /**
     * Common check routine to confirm an adapter is available as part of
     * registration process.
     * <p>
     * Note: Should only be called for debugging purposes,, for example, when
     * Log4J DEBUG level is selected, to load fewer classes at startup.
     *
     * @param o object to confirm XML adapter exists for
     */
    void confirmAdapterAvailable(Object o) {
        String adapter = adapterName(o);
        log.debug("register {} adapter {}", o, adapter);
        if (adapter != null) {
            try {
                Class.forName(adapter);
            } catch (ClassNotFoundException | NoClassDefFoundError ex) {
                locateClassFailed(ex, adapter, o);
            }
        }
    }

    /**
     * Handles ConfigureXml classes that have moved to a new package or been
     * superceded.
     *
     * @param name name of the moved or superceded ConfigureXml class
     * @return name of the ConfigureXml class in newer package or of superseding
     *         class
     */
    static public String currentClassName(String name) {
        return InstanceManager.getDefault(ClassMigrationManager.class).getClassName(name);
    }

    /**
     * Remove the registered preference items. This is used, for example, when a
     * GUI wants to replace the preferences with new values.
     */
    @Override
    public void removePrefItems() {
        log.debug("removePrefItems dropped {}", plist.size());
        plist.clear();
    }

    @Override
    public Object findInstance(Class<?> c, int index) {
        List<Object> temp = new ArrayList<>(plist);
        temp.addAll(clist.keySet());
        temp.addAll(tlist);
        temp.addAll(ulist);
        temp.addAll(uplist);
        for (Object o : temp) {
            if (c.isInstance(o)) {
                if (index-- == 0) {
                    return o;
                }
            }
        }
        return null;
    }

    @Override
    public List<Object> getInstanceList(Class<?> c) {
        List<Object> result = new ArrayList<>();

        List<Object> temp = new ArrayList<>(plist);
        temp.addAll(clist.keySet());
        temp.addAll(tlist);
        temp.addAll(ulist);
        temp.addAll(uplist);
        for (Object o : temp) {
            if (c.isInstance(o)) {
                result.add(o);
            }
        }
        return result;
    }

    @Override
    public void registerConfig(Object o, int x) {
        // skip if already present, leaving in original order
        if (clist.containsKey(o)) {
            return;
        }

        if (log.isDebugEnabled()) {
            confirmAdapterAvailable(o);
        }

        // and add to list
        clist.put(o, x);
    }

    @Override
    public void registerTool(Object o) {
        // skip if already present, leaving in original order
        if (tlist.contains(o)) {
            return;
        }

        if (log.isDebugEnabled()) {
            confirmAdapterAvailable(o);
        }

        // and add to list
        tlist.add(o);
    }

    /**
     * Register an object whose state is to be tracked. It is not an error if
     * the original object was already registered.
     *
     * @param o The object, which must have an associated adapter class.
     */
    @Override
    public void registerUser(Object o) {
        // skip if already present, leaving in original order
        if (ulist.contains(o)) {
            return;
        }

        if (log.isDebugEnabled()) {
            confirmAdapterAvailable(o);
        }

        // and add to list
        ulist.add(o);
    }

    @Override
    public void registerUserPrefs(Object o) {
        // skip if already present, leaving in original order
        if (uplist.contains(o)) {
            return;
        }

        if (log.isDebugEnabled()) {
            confirmAdapterAvailable(o);
        }

        // and add to list
        uplist.add(o);
    }

    @Override
    public void deregister(Object o) {
        plist.remove(o);
        if (o != null) {
            clist.remove(o);
        }
        tlist.remove(o);
        ulist.remove(o);
        uplist.remove(o);
    }

    List<Object> plist = new ArrayList<>();
    Map<Object, Integer> clist = Collections.synchronizedMap(new LinkedHashMap<>());
    List<Object> tlist = new ArrayList<>();
    List<Object> ulist = new ArrayList<>();
    List<Object> uplist = new ArrayList<>();
    private final List<Element> loadDeferredList = new ArrayList<>();

    /**
     * Find the name of the adapter class for an object.
     *
     * @param o object of a configurable type
     * @return class name of adapter
     */
    public static String adapterName(Object o) {
        String className = o.getClass().getName();
        log.debug("handle object of class {}", className);
        int lastDot = className.lastIndexOf(".");
        if (lastDot > 0) {
            // found package-class boundary OK
            String result = className.substring(0, lastDot)
                    + ".configurexml."
                    + className.substring(lastDot + 1, className.length())
                    + "Xml";
            log.debug("adapter class name is {}", result);
            return result;
        } else {
            // no last dot found!
            log.error("No package name found, which is not yet handled!");
            return null;
        }
    }

    /**
     * Handle failure to load adapter class. Although only a one-liner in this
     * class, it is a separate member to facilitate testing.
     *
     * @param ex          the exception throw failing to load adapterName as o
     * @param adapterName name of the adapter class
     * @param o           adapter object
     */
    void locateClassFailed(Throwable ex, String adapterName, Object o) {
        log.error("{} could not load adapter class {}", ex, adapterName);
        log.debug("Stack trace is", ex);
    }

    protected Element initStore() {
        Element root = new Element("layout-config");
        root.setAttribute("noNamespaceSchemaLocation",
                "http://jmri.org/xml/schema/layout" + schemaVersion + ".xsd",
                org.jdom2.Namespace.getNamespace("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance"));
        return root;
    }

    protected void addPrefsStore(Element root) {
        for (int i = 0; i < plist.size(); i++) {
            Object o = plist.get(i);
            Element e = elementFromObject(o);
            if (e != null) {
                root.addContent(e);
            }
        }
    }

    protected boolean addConfigStore(Element root) {
        boolean result = true;
        List<Map.Entry<Object, Integer>> l = new ArrayList<>(clist.entrySet());
        Collections.sort(l, (Map.Entry<Object, Integer> o1, Map.Entry<Object, Integer> o2) -> o1.getValue().compareTo(o2.getValue()));
        for (int i = 0; i < l.size(); i++) {
            try {
                Object o = l.get(i).getKey();
                Element e = elementFromObject(o);
                if (e != null) {
                    root.addContent(e);
                }
            } catch (Exception e) {
                storingErrorEncountered(null, "storing to file in addConfigStore",
                        "Exception thrown", null, null, e);
                result = false;
            }
        }
        return result;
    }

    protected boolean addToolsStore(Element root) {
        boolean result = true;
        for (Object o : tlist) {
            try {
                Element e = elementFromObject(o);
                if (e != null) {
                    root.addContent(e);
                }
            } catch (Exception e) {
                result = false;
                storingErrorEncountered(null, "storing to file in addToolsStore",
                        "Exception thrown", null, null, e);
            }
        }
        return result;
    }

    protected boolean addUserStore(Element root) {
        boolean result = true;
        for (Object o : ulist) {
            try {
                Element e = elementFromObject(o);
                if (e != null) {
                    root.addContent(e);
                }
            } catch (Exception e) {
                result = false;
                storingErrorEncountered(null, "storing to file in addUserStore",
                        "Exception thrown", null, null, e);
            }
        }
        return result;
    }

    protected void addUserPrefsStore(Element root) {
        for (Object o : uplist) {
            Element e = elementFromObject(o);
            if (e != null) {
                root.addContent(e);
            }
        }
    }

    protected void includeHistory(Element root) {
        // add history to end of document
        if (InstanceManager.getNullableDefault(FileHistory.class) != null) {
            root.addContent(jmri.jmrit.revhistory.configurexml.FileHistoryXml.storeDirectly(InstanceManager.getDefault(FileHistory.class)));
        }
    }

    protected boolean finalStore(Element root, File file) {
        try {
            // Document doc = newDocument(root, dtdLocation+"layout-config-"+dtdVersion+".dtd");
            Document doc = newDocument(root);

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/panelfile"+schemaVersion+".xsl"?>
            java.util.Map<String, String> m = new java.util.HashMap<>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation + "panelfile" + schemaVersion + ".xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            // add version at front
            storeVersion(root);

            writeXML(file, doc);
        } catch (java.io.FileNotFoundException ex3) {
            storingErrorEncountered(null, "storing to file " + file.getName(),
                    "File not found " + file.getName(), null, null, ex3);
            log.error("FileNotFound error writing file: " + ex3.getLocalizedMessage());
            return false;
        } catch (java.io.IOException ex2) {
            storingErrorEncountered(null, "storing to file " + file.getName(),
                    "IO error writing file " + file.getName(), null, null, ex2);
            log.error("IO error writing file: " + ex2.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Writes config, tools and user to a file.
     *
     * @param file config file to write
     */
    @Override
    public boolean storeAll(File file) {
        boolean result = true;
        Element root = initStore();
        if (!addConfigStore(root)) {
            result = false;
        }
        if (!addToolsStore(root)) {
            result = false;
        }
        if (!addUserStore(root)) {
            result = false;
        }
        addConfigStore(root);
        addToolsStore(root);
        addUserStore(root);
        includeHistory(root);
        if (!finalStore(root, file)) {
            result = false;
        }
        return result;
    }

    /**
     * Writes prefs to a predefined File location.
     */
    @Override
    public void storePrefs() {
        storePrefs(prefsFile);
    }

    @Override
    public void storePrefs(File file) {
        synchronized (this) {
            Element root = initStore();
            addPrefsStore(root);
            finalStore(root, file);
        }
    }

    @Override
    public void storeUserPrefs(File file) {
        synchronized (this) {
            Element root = initStore();
            addUserPrefsStore(root);
            finalStore(root, file);
        }
    }

    /**
     * Set location for preferences file.
     * <p>
     * File need not exist, but location must be writable when storePrefs()
     * called.
     *
     * @param prefsFile new location for preferences file
     */
    public void setPrefsLocation(File prefsFile) {
        this.prefsFile = prefsFile;
    }
    File prefsFile;

    /**
     * Writes prefs to a file.
     *
     * @param file config file to write.
     */
    @Override
    public boolean storeConfig(File file) {
        boolean result = true;
        Element root = initStore();
        if (!addConfigStore(root)) {
            result = false;
        }
        includeHistory(root);
        if (!finalStore(root, file)) {
            result = false;
        }
        return result;
    }

    /**
     * Writes user and config info to a file.
     * <P>
     * Config is included here because it doesn't hurt to read it again, and the
     * user data (typically a panel) requires it to be present first.
     *
     * @param file config file to write
     */
    @Override
    public boolean storeUser(File file) {
        boolean result = true;
        Element root = initStore();
        if (!addConfigStore(root)) {
            result = false;
        }
        if (!addUserStore(root)) {
            result = false;
        }
        includeHistory(root);
        if (!finalStore(root, file)) {
            result = false;
        }
        return result;
    }

    @Override
    public boolean makeBackup(File file) {
        return makeBackupFile(defaultBackupDirectory, file);
    }

    String defaultBackupDirectory = FileUtil.getUserFilesPath() + "backupPanels";

    /**
     *
     * @param o The object to get an XML representation of
     * @return An XML element representing o
     */
    static public Element elementFromObject(Object o) {
        return ConfigXmlManager.elementFromObject(o, true);
    }

    /**
     *
     * @param object The object to get an XML representation of
     * @param shared true if the XML should be shared, false if the XML should
     *               be per-node
     * @return An XML element representing object
     */
    static public Element elementFromObject(Object object, boolean shared) {
        String aName = adapterName(object);
        log.debug("store using {}", aName);
        XmlAdapter adapter = null;
        try {
            adapter = (XmlAdapter) Class.forName(adapterName(object)).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            log.error("Cannot load configuration adapter for {}", object.getClass().getName(), ex);
        }
        if (adapter != null) {
            return adapter.store(object, shared);
        } else {
            log.error("Cannot store configuration for {}", object.getClass().getName());
            return null;
        }
    }

    private void storeVersion(Element root) {
        // add version at front
        root.addContent(0,
                new Element("jmriversion")
                        .addContent(new Element("major").addContent("" + jmri.Version.major))
                        .addContent(new Element("minor").addContent("" + jmri.Version.minor))
                        .addContent(new Element("test").addContent("" + jmri.Version.test))
                        .addContent(new Element("modifier").addContent(jmri.Version.getModifier()))
        );
    }

    /**
     * Load a file.
     * <p>
     * Handles problems locally to the extent that it can, by routing them to
     * the creationErrorEncountered method.
     *
     * @param fi file to load
     * @return true if no problems during the load
     * @throws jmri.configurexml.JmriConfigureXmlException if unable to load
     *                                                     file
     */
    @Override
    public boolean load(File fi) throws JmriConfigureXmlException {
        return load(fi, false);
    }

    @Override
    public boolean load(URL url) throws JmriConfigureXmlException {
        return load(url, false);
    }

    /**
     * Load a file.
     * <p>
     * Handles problems locally to the extent that it can, by routing them to
     * the creationErrorEncountered method.
     *
     * @param fi               file to load
     * @param registerDeferred true to register objects to defer
     * @return true if no problems during the load
     * @throws JmriConfigureXmlException if problem during load
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     * @since 2.11.2
     */
    @Override
    public boolean load(File fi, boolean registerDeferred) throws JmriConfigureXmlException {
        return this.load(FileUtil.fileToURL(fi), registerDeferred);
    }

    /**
     * Load a file.
     * <p>
     * Handles problems locally to the extent that it can, by routing them to
     * the creationErrorEncountered method.
     * <p>
     * Always processes on Swing thread
     *
     * @param url              URL of file to load
     * @param registerDeferred true to register objects to defer
     * @return true if no problems during the load
     * @throws JmriConfigureXmlException if problem during load
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     * @since 3.3.2
     */
    @Override
    public boolean load(URL url, boolean registerDeferred) throws JmriConfigureXmlException {
        // must not use invokeAndWait on Swing thread
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            // direct exec
            return loadOnSwingThread(url, registerDeferred);
        } else {
            // push to swing
            final java.util.concurrent.atomic.AtomicReference<Boolean> result = new java.util.concurrent.atomic.AtomicReference<>();

            try {
                javax.swing.SwingUtilities.invokeAndWait(() -> {
                    try {
                        boolean temp = loadOnSwingThread(url, registerDeferred);
                        result.set(temp);
                    } catch (JmriConfigureXmlException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new JmriConfigureXmlException(e);
            }

            return result.get();
        }
    }

    private XmlFile.Validate validate = XmlFile.Validate.CheckDtdThenSchema;

    /**
     * Default XML verification. Public to allow scripting.
     */
    public void setValidate(XmlFile.Validate v) {
        validate = v;
    }

    /**
     * Default XML verification. Public to allow scripting.
     */
    public XmlFile.Validate getValidate() {
        return validate;
    }

    private boolean loadOnSwingThread(URL url, boolean registerDeferred) throws JmriConfigureXmlException {
        boolean result = true;
        Element root = null;
        /* We will put all the elements into a load list, along with the load order
         As XML files prior to 2.13.1 had no order to the store, beans would be stored/loaded
         before beans that they were dependant upon had been stored/loaded
         */
        Map<Element, Integer> loadlist = Collections.synchronizedMap(new LinkedHashMap<>());

        try {
            setValidate(validate);
            root = super.rootFromURL(url);
            // get the objects to load
            List<Element> items = root.getChildren();
            for (int i = 0; i < items.size(); i++) {
                //Put things into an ordered list
                Element item = items.get(i);
                Attribute a = item.getAttribute("class");
                if (a == null) {
                    // this is an element that we're not meant to read
                    log.debug("skipping {}", item);
                    continue;
                }
                String adapterName = a.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("attempt to get adapter {} for {}", adapterName, item);
                }
                adapterName = currentClassName(adapterName);
                XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).newInstance();
                int order = adapter.loadOrder();
                log.debug("add {} to load list with order id of {}", item, order);
                loadlist.put(item, order);
            }

            List<Map.Entry<Element, Integer>> l = new ArrayList<>(loadlist.entrySet());
            Collections.sort(l, (Map.Entry<Element, Integer> o1, Map.Entry<Element, Integer> o2) -> o1.getValue().compareTo(o2.getValue()));
            for (int i = 0; i < l.size(); i++) {
                Element item = l.get(i).getKey();
                String adapterName = item.getAttribute("class").getValue();
                adapterName = currentClassName(adapterName);
                if (log.isDebugEnabled()) {
                    log.debug("load " + item + " via " + adapterName);
                }
                XmlAdapter adapter = null;
                try {
                    adapter = (XmlAdapter) Class.forName(adapterName).newInstance();

                    // get version info
                    // loadVersion(root, adapter);
                    // and do it
                    if (adapter.loadDeferred() && registerDeferred) {
                        // register in the list for deferred load
                        loadDeferredList.add(item);
                        if (log.isDebugEnabled()) {
                            log.debug("deferred load registered for " + item + " " + adapterName);
                        }
                    } else {
                        boolean loadStatus = adapter.load(item, item);
                        if (log.isDebugEnabled()) {
                            log.debug("load status for " + item + " " + adapterName + " is " + loadStatus);
                        }

                        // if any adaptor load fails, then the entire load has failed
                        if (!loadStatus) {
                            result = false;
                        }
                    }
                } catch (Exception e) {
                    creationErrorEncountered(adapter, "load(" + url.getFile() + ")",
                            "Unexpected error (Exception)", null, null, e);

                    result = false;  // keep going, but return false to signal problem
                } catch (Throwable et) {
                    creationErrorEncountered(adapter, "in load(" + url.getFile() + ")",
                            "Unexpected error (Throwable)", null, null, et);

                    result = false;  // keep going, but return false to signal problem
                }
            }

        } catch (java.io.FileNotFoundException e1) {
            // this returns false to indicate un-success, but not enough
            // of an error to require a message
            creationErrorEncountered(null, "opening file " + url.getFile(),
                    "File not found", null, null, e1);
            result = false;
        } catch (org.jdom2.JDOMException e) {
            creationErrorEncountered(null, "parsing file " + url.getFile(),
                    "Parse error", null, null, e);
            result = false;
        } catch (java.io.IOException e) {
            creationErrorEncountered(null, "loading from file " + url.getFile(),
                    "IOException", null, null, e);
            result = false;
        } catch (ClassNotFoundException e) {
            creationErrorEncountered(null, "loading from file " + url.getFile(),
                    "ClassNotFoundException", null, null, e);
            result = false;
        } catch (InstantiationException e) {
            creationErrorEncountered(null, "loading from file " + url.getFile(),
                    "InstantiationException", null, null, e);
            result = false;
        } catch (IllegalAccessException e) {
            creationErrorEncountered(null, "loading from file " + url.getFile(),
                    "IllegalAccessException", null, null, e);
            result = false;
        } finally {
            // no matter what, close error reporting
            handler.done();
        }

        // loading complete, as far as it got, make history entry
        FileHistory r = InstanceManager.getNullableDefault(FileHistory.class);
        if (r != null) {
            FileHistory included = null;
            if (root != null) {
                Element filehistory = root.getChild("filehistory");
                if (filehistory != null) {
                    included = jmri.jmrit.revhistory.configurexml.FileHistoryXml.loadFileHistory(filehistory);
                }
            }
            r.addOperation((result ? "Load OK" : "Load with errors"), url.getFile(), included);
        } else {
            log.info("Not recording file history");
        }
        return result;
    }

    @Override
    public boolean loadDeferred(File fi) {
        return this.loadDeferred(FileUtil.fileToURL(fi));
    }

    @Override
    public boolean loadDeferred(URL url) {
        boolean result = true;
        // Now process the load-later list
        log.debug("Start processing deferred load list (size): " + loadDeferredList.size());
        if (!loadDeferredList.isEmpty()) {
            for (Element item : loadDeferredList) {
                String adapterName = item.getAttribute("class").getValue();
                log.debug("deferred load via " + adapterName);
                XmlAdapter adapter = null;
                try {
                    adapter = (XmlAdapter) Class.forName(adapterName).newInstance();
                    boolean loadStatus = adapter.load(item, item);
                    log.debug("deferred load status for " + adapterName + " is " + loadStatus);

                    // if any adaptor load fails, then the entire load has failed
                    if (!loadStatus) {
                        result = false;
                    }
                } catch (Exception e) {
                    creationErrorEncountered(adapter, "deferred load(" + url.getFile() + ")",
                            "Unexpected error (Exception)", null, null, e);
                    result = false;  // keep going, but return false to signal problem
                } catch (Throwable et) {
                    creationErrorEncountered(adapter, "in deferred load(" + url.getFile() + ")",
                            "Unexpected error (Throwable)", null, null, et);
                    result = false;  // keep going, but return false to signal problem
                }
            }
        }
        log.debug("Done processing deferred load list with result: " + result);
        return result;
    }

    /**
     * Find a file by looking
     * <UL>
     * <LI> in xml/layout/ in the preferences directory, if that exists
     * <LI> in xml/layout/ in the application directory, if that exists
     * <LI> in xml/ in the preferences directory, if that exists
     * <LI> in xml/ in the application directory, if that exists
     * <LI> at top level in the application directory
     * <LI>
     * </ul>
     *
     * @param f Local filename, perhaps without path information
     * @return Corresponding File object
     */
    @Override
    public URL find(String f) {
        URL u = FileUtil.findURL(f, "xml/layout", "xml"); // NOI18N
        if (u == null) {
            this.locateFileFailed(f);
        }
        return u;
    }

    /**
     * Report a failure to find a file. This is a separate member to ease
     * testing.
     *
     * @param f Name of file not located.
     */
    void locateFileFailed(String f) {
        log.warn("Could not locate file " + f);
    }

    /**
     * Invoke common handling of errors that happen during the "load" process.
     * <p>
     * Exceptions passed into this are absorbed.
     *
     * @param adapter     Object that encountered the error (for reporting), may
     *                    be null
     * @param operation   description of the operation being attempted, may be
     *                    null
     * @param description description of error encountered
     * @param systemName  System name of bean being handled, may be null
     * @param userName    used name of the bean being handled, may be null
     * @param exception   Any exception being handled in the processing, may be
     *                    null
     */
    static public void creationErrorEncountered(
            XmlAdapter adapter,
            String operation,
            String description,
            String systemName,
            String userName,
            Throwable exception) {
        // format and log a message (note reordered from arguments)
        ErrorMemo e = new ErrorMemo(
                adapter, operation, description,
                systemName, userName, exception, "loading");
        if (adapter != null) {
            ErrorHandler aeh = adapter.getExceptionHandler();
            if (aeh != null) {
                aeh.handle(e);
            }
        } else {
            handler.handle(e);
        }
    }

    /**
     * Invoke common handling of errors that happen during the "store" process.
     * <p>
     * Exceptions passed into this are absorbed.
     *
     * @param adapter     Object that encountered the error (for reporting), may
     *                    be null
     * @param operation   description of the operation being attempted, may be
     *                    null
     * @param description description of error encountered
     * @param systemName  System name of bean being handled, may be null
     * @param userName    used name of the bean being handled, may be null
     * @param exception   Any exception being handled in the processing, may be
     *                    null
     */
    static public void storingErrorEncountered(
            XmlAdapter adapter,
            String operation,
            String description,
            String systemName,
            String userName,
            Throwable exception) {
        // format and log a message (note reordered from arguments)
        ErrorMemo e = new ErrorMemo(
                adapter, operation, description,
                systemName, userName, exception, "storing");
        if (adapter != null) {
            ErrorHandler aeh = adapter.getExceptionHandler();
            if (aeh != null) {
                aeh.handle(e);
            }
        } else {
            handler.handle(e);
        }
    }

    static ErrorHandler handler = new ErrorHandler();

    static public void setErrorHandler(ErrorHandler handler) {
        ConfigXmlManager.handler = handler;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConfigXmlManager.class);

    /**
     * @return the loadDeferredList
     */
    protected List<Element> getLoadDeferredList() {
        return loadDeferredList;
    }
}
