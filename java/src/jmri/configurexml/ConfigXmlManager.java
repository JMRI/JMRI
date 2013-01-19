package jmri.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.revhistory.FileHistory;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Comparator;
import java.util.LinkedHashMap;
import jmri.util.FileUtil;

//import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import org.apache.log4j.Level;

/**
 * Provides the mechanisms for storing an entire layout configuration
 * to XML.  "Layout" refers to the hardware:  Specific communcation
 * systems, etc.
 * @see <A HREF="package-summary.html">Package summary for details of the overall structure</A>
 * @author Bob Jacobsen  Copyright (c) 2002, 2008
 * @version $Revision$
 */
public class ConfigXmlManager extends jmri.jmrit.XmlFile
    implements jmri.ConfigureManager {

    /**
     * Define the current DTD version string for the layout-config DTD.
     * See the <A HREF="package-summary.html#DTD">DTD versioning discussion</a>
     */
    static final public String schemaVersion = "-2-9-6";
    
    public ConfigXmlManager() {
    }

    public void registerConfig(Object o){
        registerConfig(o, 50);
    }
    
    public void registerPref(Object o) {
        // skip if already present, leaving in original order
        if (plist.contains(o)) return;
        confirmAdapterAvailable(o);
        
        // and add to list
        plist.add(o);
    }

    /**
     * Common check routine to confirm 
     * an adapter is available as part of
     * registration process. Only enabled when
     * Log4J DEBUG level is selected, to load fewer
     * classes at startup.
     */
    void confirmAdapterAvailable(Object o) {
        if (log.isDebugEnabled()) {
            String adapter = adapterName(o);
            if (log.isDebugEnabled()) log.debug("register "+o+" adapter "+adapter);
            if (adapter!=null)
                try {
                    Class.forName(adapter);
                } catch (java.lang.ClassNotFoundException ex) {
                    locateClassFailed(ex, adapter, o);
                } catch (java.lang.NoClassDefFoundError ex) {
                    locateClassFailed(ex, adapter, o);
                }
        }
    }
    
    /**
     * Remove the registered preference items.  This is used
     * e.g. when a GUI wants to replace the preferences with new
     * values.
     */
    public void removePrefItems(){
        if (log.isDebugEnabled()) log.debug("removePrefItems dropped "+plist.size());
        plist.clear();
    }

    public Object findInstance(Class<?> c, int index) {
        ArrayList<Object> temp = new ArrayList<Object>(plist);
        temp.addAll(clist.keySet());
        temp.addAll(tlist);
        temp.addAll(ulist);
        temp.addAll(uplist);
        for (int i=0; i<temp.size(); i++) {
            if (c.isInstance(temp.get(i))) { 
                if (index-- == 0) return temp.get(i);
            }
        }
        return null;
    }
    
    public ArrayList<Object> getInstanceList(Class<?> c){
        ArrayList<Object> temp = new ArrayList<Object>(plist);
        ArrayList<Object> returnlist = new ArrayList<Object>();
        temp.addAll(clist.keySet());
        temp.addAll(tlist);
        temp.addAll(ulist);
        temp.addAll(uplist);
        for (int i=0; i<temp.size(); i++) {
            if (c.isInstance(temp.get(i))) {
                returnlist.add(temp.get(i));
                //if (index-- == 0) return temp.get(i);
            }
        }
        if(returnlist.isEmpty())
            return null;
        return returnlist;
    
    }

    public void registerConfig(Object o, int x) {
        // skip if already present, leaving in original order
        if (clist.containsKey(o)) return;

        confirmAdapterAvailable(o);
        
        // and add to list
        //clist.add(o);
        clist.put(o, x);
    }
    public void registerTool(Object o) {
        // skip if already present, leaving in original order
        if (tlist.contains(o)) return;

        confirmAdapterAvailable(o);
        
        // and add to list
        tlist.add(o);
    }
    /**
     * Register an object whose state is to be tracked.
     * It is not an error if the original object was already registered.
     * @param o The object, which must have an
     *              associated adapter class.
     */
    public void registerUser(Object o) {
        // skip if already present, leaving in original order
        if (ulist.contains(o)) return;

        confirmAdapterAvailable(o);
        
        // and add to list
        ulist.add(o);
    }
    
    public void registerUserPrefs(Object o) {
        // skip if already present, leaving in original order
        if (uplist.contains(o)) return;

        confirmAdapterAvailable(o);
        
        // and add to list
        uplist.add(o);
    }

    public void deregister(Object o) {
        plist.remove(o);
        if(o!=null)
            clist.remove(o);
        tlist.remove(o);
        ulist.remove(o);
        uplist.remove(o);
    }

    ArrayList<Object> plist = new ArrayList<Object>();
    //Hashtable<Object, Integer> clist = new Hashtable<Object, Integer>();
    Map<Object, Integer> clist = Collections.synchronizedMap(new LinkedHashMap<Object, Integer>());
    ArrayList<Object> tlist = new ArrayList<Object>();
    ArrayList<Object> ulist = new ArrayList<Object>();
    ArrayList<Object> uplist = new ArrayList<Object>();
    ArrayList<Element> loadDeferredList = new ArrayList<Element>();

    /**
     * Find the name of the adapter class for an object.
     * @param o object of a configurable type
     * @return class name of adapter
     */
    public static String adapterName(Object o) {
        String className = o.getClass().getName();
        if (log.isDebugEnabled()) log.debug("handle object of class "+className);
        int lastDot = className.lastIndexOf(".");
        String result = null;

        if (lastDot>0) {
            // found package-class boundary OK
            result = className.substring(0,lastDot)
                +".configurexml."
                +className.substring(lastDot+1,className.length())
                +"Xml";
            if (log.isDebugEnabled()) log.debug("adapter class name is "+result);
            return result;
        } else {
            // no last dot found!
            log.error("No package name found, which is not yet handled!");
            return null;
        }
    }

    /**
     * Handle failure to load adapter class. Although only a
     * one-liner in this class, it is a separate member to facilitate testing.
     */
    void locateClassFailed(Throwable ex, String adapterName, Object o) {
        log.error(ex.getMessage()+" could not load adapter class "+adapterName);
        if (log.isDebugEnabled()) ex.printStackTrace();
    }

    protected Element initStore() {
        Element root = new Element("layout-config");
        root.setAttribute("noNamespaceSchemaLocation",
                    "http://jmri.org/xml/schema/layout"+schemaVersion+".xsd",
                    org.jdom.Namespace.getNamespace("xsi",
                      "http://www.w3.org/2001/XMLSchema-instance"));
        return root;
    }
    protected void addPrefsStore(Element root) {
        for (int i=0; i<plist.size(); i++) {
            Object o = plist.get(i);
            Element e = elementFromObject(o);
            if (e!=null) root.addContent(e);
        }
    }
    
    protected boolean addConfigStore(Element root) {
        boolean result = true;
        ArrayList<Map.Entry<Object, Integer>> l = new ArrayList<Map.Entry<Object, Integer>>(clist.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<Object, Integer>>(){

         public int compare(Map.Entry<Object, Integer> o1, Map.Entry<Object, Integer> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }});
        for (int i=0; i<l.size(); i++) {
            try {
                Object o = l.get(i).getKey();
                Element e = elementFromObject(o);
                if (e!=null) root.addContent(e);
            } catch (java.lang.Exception e) {
                storingErrorEncountered (null, "storing to file", Level.ERROR,
                                          "Unknown error (Exception)", null,null,e);
                result = false;
            }
        }
        return result;
    }
    
    protected boolean addToolsStore(Element root) {
        boolean result = true;
        for (int i=0; i<tlist.size(); i++) {
            Object o = tlist.get(i);
            try {
                Element e = elementFromObject(o);
                if (e!=null) root.addContent(e);
            } catch (java.lang.Exception e) {
                result = false;
                storingErrorEncountered (((XmlAdapter)o), "storing to file", Level.ERROR,
                                          "Unknown error (Exception)", null,null,e);
            }
        }
        return result;
    }
    
    protected boolean addUserStore(Element root) {
        boolean result = true;
        for (int i=0; i<ulist.size(); i++) {
            Object o = ulist.get(i);
            try{
                Element e = elementFromObject(o);
                if (e!=null) root.addContent(e);
            } catch (java.lang.Exception e) {
                result = false;
                storingErrorEncountered ((XmlAdapter)o, "storing to file", Level.ERROR, 
                                          "Unknown error (Exception)", null,null,e);
            }
        }
        return result;
    }
    
    protected void addUserPrefsStore(Element root) {
        for (int i=0; i<uplist.size(); i++) {
            Object o = uplist.get(i);
            Element e = elementFromObject(o);
            if (e!=null) root.addContent(e);
        }
    }
    
    protected void includeHistory(Element root) {
        // add history to end of document
        if (InstanceManager.getDefault(FileHistory.class) != null)
        root.addContent(jmri.jmrit.revhistory.configurexml.FileHistoryXml.storeDirectly(InstanceManager.getDefault(FileHistory.class)));
    }

    protected boolean finalStore(Element root, File file) {
        try {
            // Document doc = newDocument(root, dtdLocation+"layout-config-"+dtdVersion+".dtd");
            Document doc = newDocument(root);

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/panelfile"+schemaVersion+".xsl"?>
            java.util.Map<String,String> m = new java.util.HashMap<String,String>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation+"panelfile"+schemaVersion+".xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0,p);
            
            // add version at front
            storeVersion(root);

            writeXML(file, doc);
        } catch (java.io.FileNotFoundException ex3) {
            storingErrorEncountered (null, "storing to file "+file.getName(), Level.ERROR,
                                      "File not found " + file.getName(), null,null,ex3);
            log.error("FileNotFound error writing file: "+ex3.getLocalizedMessage());
            return false;
        } catch (java.io.IOException ex2) {
            storingErrorEncountered (null, "storing to file "+file.getName(), Level.ERROR,
                              "IO error writing file " + file.getName(), null,null,ex2);
            log.error("IO error writing file: "+ex2.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Writes config, tools and user to a file.
     * @param file
     */
    public boolean storeAll(File file) {
        boolean result = true;
        Element root = initStore();
        if(!addConfigStore(root)){
            result=false;
        }
        if(!addToolsStore(root)){
            result=false;
        }
        if(!addUserStore(root)){
            result=false;
        }
        addConfigStore(root);
        addToolsStore(root);
        addUserStore(root);
        includeHistory(root);
        if(!finalStore(root, file)){
            result=false;
        }
        return result;
    }
    /**
     * Writes prefs to a predefined File location.
     */
    public void storePrefs() {
        storePrefs(prefsFile);
    }
    
    public void storePrefs(File file) {
        Element root = initStore();
        addPrefsStore(root);
        finalStore(root, file);
    }
    
    public void storeUserPrefs(File file) {
        Element root = initStore();
        addUserPrefsStore(root);
        finalStore(root, file);
    }

    /**
     * Set location for preferences file. 
     * <p>
     * File need not exist,
     * but location must be writable when storePrefs() called.
     */
    public void setPrefsLocation(File prefsFile) { this.prefsFile = prefsFile; }
    File prefsFile;
    
    /**
     * Set location for user preferences file. 
     * <p>
     * File need not exist,
     * but location must be writable when storePrefs() called.
     */
    /*public void setUserPrefsLocation(File userPrefsFile) { this.userPrefsFile = userPrefsFile; }
    File userPrefsFile;*/
    
    /**
     * Writes prefs to a file.
     * @param file
     */
    public boolean storeConfig(File file) {
        boolean result = true;
        Element root = initStore();
        if(!addConfigStore(root)){
            result=false;
        }
        includeHistory(root);
        if(!finalStore(root, file)){
            result=false;
        }
        return result;
    }

     /**
     * Writes user and config info to a file.
     * <P>
     * Config is included here because it doesnt hurt to
     * read it again, and the user data (typically a panel)
     * requires it to be present first.
     * @param file
     */
    public boolean storeUser(File file) {
        boolean result = true;
        Element root = initStore();
        if(!addConfigStore(root)){
            result=false;
        }
        if(!addUserStore(root)){
            result=false;
        }
        includeHistory(root);
        if(!finalStore(root, file)){
            result=false;
        }
        return result;
    }
    
    public boolean makeBackup(File file){
    	return makeBackupFile(defaultBackupDirectory, file);
    }
    
    String defaultBackupDirectory = FileUtil.getUserFilesPath()+"backupPanels";

    static public Element elementFromObject(Object o) {
        String aName = adapterName(o);
        log.debug("store using "+aName);
        XmlAdapter adapter = null;
        try {
            adapter = (XmlAdapter)Class.forName(adapterName(o)).newInstance();
        } catch (java.lang.ClassNotFoundException ex1) {
            log.error("Cannot load configuration adapter for "+o.getClass().getName()+" due to "+ex1);
        } catch (java.lang.IllegalAccessException ex2) {
            log.error("Cannot load configuration adapter for "+o.getClass().getName()+" due to "+ex2);
        } catch (java.lang.InstantiationException ex3) {
            log.error("Cannot load configuration adapter for "+o.getClass().getName()+" due to "+ex3);
        }
        if (adapter!=null){
            return adapter.store(o);
        } else {
            log.error("Cannot store configuration for "+o.getClass().getName());
            return null;
        }
    }

    private void storeVersion(Element root) {
        // add version at front
        root.addContent(0,
            new Element("jmriversion")
                .addContent(new Element("major").addContent(""+jmri.Version.major))
                .addContent(new Element("minor").addContent(""+jmri.Version.minor))
                .addContent(new Element("test").addContent(""+jmri.Version.test))
		.addContent(new Element("modifier").addContent(jmri.Version.getModifier()))
        );
    }

//     private void loadVersion(Element root, XmlAdapter adapter) {
//         int majorRelease = 0;
//         int minorRelease = 0;
//         int testRelease = 0;
//         Element v = root.getChild("jmriversion");
//         if (v!=null) {
//             try {
//                 majorRelease = Integer.parseInt(v.getChild("major").getText());
//                 minorRelease = Integer.parseInt(v.getChild("minor").getText());
//                 testRelease = Integer.parseInt(v.getChild("test").getText());
//             } catch (NullPointerException npe) {
//             } catch ( NumberFormatException nfe) {
//             }
//         }
//         adapter.setConfigXmlManager(this);
//         adapter.setMajorRelease(majorRelease);
//         adapter.setMinorRelease(minorRelease);
//         adapter.setTestRelease(testRelease);
//     }
    
    /** 
     * Load a file.
     * <p>
     * Handles problems locally to the extent that it can,
     * by routing them to the creationErrorEncountered
     * method. 
     * @return true if no problems during the load
     */
    public boolean load(File fi) throws JmriConfigureXmlException {
        return load(fi, false);
    }

    /**
     * Load a file.
     * <p>
     * Handles problems locally to the extent that it can,
     * by routing them to the creationErrorEncountered
     * method.
     * @param fi file to load
     * @param registerDeferred true to register objects to defer
     * @return true if no problems during the load
     * @throws JmriConfigureXmlException
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     * @since 2.11.2
     */
    @SuppressWarnings("unchecked")
    public boolean load(File fi, boolean registerDeferred) throws JmriConfigureXmlException {
        boolean result = true;
        Element root = null;
        /* We will put all the elements into a load list, along with the load order
        As XML files prior to 2.13.1 had no order to the store, beans would be stored/loaded
        before beans that they were dependant upon had been stored/loaded
        */
        Map<Element, Integer> loadlist = Collections.synchronizedMap(new LinkedHashMap<Element, Integer>());
        
        try{
            root = super.rootFromFile(fi);
            // get the objects to load
            List<Element> items = root.getChildren();
            for (int i = 0; i<items.size(); i++) {
                //Put things into an ordered list
                Element item = items.get(i);
                if (item.getAttribute("class") == null) {
                    // this is an element that we're not meant to read
                	if (log.isDebugEnabled()) log.debug("skipping " + item);
                    continue;
                }
                String adapterName = item.getAttribute("class").getValue();
                if (log.isDebugEnabled()) log.debug("attempt to get adapter "+adapterName + " for " + item);
                XmlAdapter adapter = null;
                
                adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                int order = adapter.loadOrder();
                if (log.isDebugEnabled()) log.debug("add "+ item + " to load list with order id of " + order);
                loadlist.put(item, order);
            }
            
            ArrayList<Map.Entry<Element, Integer>> l = new ArrayList<Map.Entry<Element, Integer>>(loadlist.entrySet());
            Collections.sort(l, new Comparator<Map.Entry<Element, Integer>>(){

             public int compare(Map.Entry<Element, Integer> o1, Map.Entry<Element, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }});
            for (int i=0; i<l.size(); i++) {
                Element item = l.get(i).getKey();
                String adapterName = item.getAttribute("class").getValue();
                if (log.isDebugEnabled()) log.debug("load " + item + " via "+adapterName);
                XmlAdapter adapter = null;
                try {
                    adapter = (XmlAdapter)Class.forName(adapterName).newInstance();

                    // get version info
                    // loadVersion(root, adapter);
                    
                    // and do it
                    if (adapter.loadDeferred() && registerDeferred) {
                        // register in the list for deferred load
                        loadDeferredList.add(item);
                        if (log.isDebugEnabled()) log.debug("deferred load registered for " + item + " "  + adapterName);
                    } else {
                        boolean loadStatus = adapter.load(item);
                        if (log.isDebugEnabled()) log.debug("load status for " + item + " " +adapterName+" is "+loadStatus);
                    
                        // if any adaptor load fails, then the entire load has failed
                        if (!loadStatus)
                            result = false;
                    }
                } catch (Exception e) {
                    creationErrorEncountered (adapter, "load("+fi.getName()+")",Level.ERROR,
                                              "Unexpected error (Exception)",null,null,e);
                   
                    result = false;  // keep going, but return false to signal problem
                } catch (Throwable et) {
                    creationErrorEncountered (adapter, "in load("+fi.getName()+")", Level.ERROR,
                                              "Unexpected error (Throwable)",null,null,et);

                    result = false;  // keep going, but return false to signal problem
                }
            }

            
            
        } catch (java.io.FileNotFoundException e1) {
            // this returns false to indicate un-success, but not enough
            // of an error to require a message
            creationErrorEncountered (null, "opening file "+fi.getName(), Level.ERROR,
                                      "File not found", null,null,e1);
            result = false;
        } catch (org.jdom.JDOMException e) {
            creationErrorEncountered (null, "parsing file "+fi.getName(), Level.ERROR,
                                      "Parse error", null,null,e);
            result = false;
        } catch (java.lang.Exception e) {
            creationErrorEncountered (null, "loading from file "+fi.getName(), Level.ERROR,
                                      "Unknown error (Exception)", null,null,e);
            result = false;
        } finally {
            // no matter what, close error reporting
            handler.done();
        }
       
        
        /*try {
            root = super.rootFromFile(fi);
            // get the objects to load
            List<Element> items = root.getChildren();
            for (int i = 0; i<items.size(); i++) {
                // get the class, hence the adapter object to do loading
                Element item = items.get(i);
                if (item.getAttribute("class") == null) {
                    // this is an element that we're not meant to read
                    continue;
                }
                String adapterName = item.getAttribute("class").getValue();
                log.debug("load via "+adapterName);
                XmlAdapter adapter = null;
                
                try {
                    adapter = (XmlAdapter)Class.forName(adapterName).newInstance();

                    // get version info
                    // loadVersion(root, adapter);
                    
                    // and do it
                    if (adapter.loadDeferred() && registerDeferred) {
                        // register in the list for deferred load
                        loadDeferredList.add(item);
                        log.debug("deferred load registered for " + adapterName);
                    } else {
                        boolean loadStatus = adapter.load(item);
                        log.debug("load status for "+adapterName+" is "+loadStatus);
                    
                        // if any adaptor load fails, then the entire load has failed
                        if (!loadStatus)
                            result = false;
                    }
                } catch (Exception e) {
                    creationErrorEncountered (adapter, "load("+fi.getName()+")",Level.ERROR,
                                              "Unexpected error (Exception)",null,null,e);
                   
                    result = false;  // keep going, but return false to signal problem
                } catch (Throwable et) {
                    creationErrorEncountered (adapter, "in load("+fi.getName()+")", Level.ERROR,
                                              "Unexpected error (Throwable)",null,null,et);

                    result = false;  // keep going, but return false to signal problem
                }
            }


        } catch (java.io.FileNotFoundException e1) {
            // this returns false to indicate un-success, but not enough
            // of an error to require a message
            creationErrorEncountered (null, "opening file "+fi.getName(), Level.ERROR,
                                      "File not found", null,null,e1);
            result = false;
        } catch (org.jdom.JDOMException e) {
            creationErrorEncountered (null, "parsing file "+fi.getName(), Level.ERROR,
                                      "Parse error", null,null,e);
            result = false;
        } catch (java.lang.Exception e) {
            creationErrorEncountered (null, "loading from file "+fi.getName(), Level.ERROR,
                                      "Unknown error (Exception)", null,null,e);
            result = false;
        } finally {
            // no matter what, close error reporting
            handler.done();
        }*/

        // loading complete, as far as it got, make history entry
        FileHistory r = InstanceManager.getDefault(FileHistory.class);
        if (r!=null) {
            FileHistory included = null;
            if (root != null) {
                Element filehistory = root.getChild("filehistory");
                if (filehistory != null) {
                    included = jmri.jmrit.revhistory.configurexml.FileHistoryXml.loadFileHistory(filehistory);
                }
            }
            r.addOperation((result ? "Load OK":"Load with errors"), fi.getName(), included);
        } else {
            log.info("Not recording file history");
        }

        if (!result) return false;
        
		// all loaded, initialize objects as necessary
		InstanceManager.logixManagerInstance().activateAllLogixs();
		InstanceManager.layoutBlockManagerInstance().initializeLayoutBlockPaths();
        new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
        return result;
    }

    public boolean loadDeferred(File fi) {
        boolean result = true;
        // Now process the load-later list
        log.debug("Start processing deferred load list (size): "+loadDeferredList.size());
        if (!loadDeferredList.isEmpty()) {
            for(Element item: loadDeferredList) {
                String adapterName = item.getAttribute("class").getValue();
                log.debug("deferred load via "+adapterName);
                XmlAdapter adapter = null;
                try {
                    adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                    boolean loadStatus = adapter.load(item);
                    log.debug("deferred load status for "+adapterName+" is "+loadStatus);

                    // if any adaptor load fails, then the entire load has failed
                    if (!loadStatus)
                        result = false;
                } catch (Exception e) {
                    creationErrorEncountered (adapter, "deferred load("+fi.getName()+")",Level.ERROR,
                                              "Unexpected error (Exception)",null,null,e);
                    result = false;  // keep going, but return false to signal problem
                } catch (Throwable et) {
                    creationErrorEncountered (adapter, "in deferred load("+fi.getName()+")", Level.ERROR,
                                              "Unexpected error (Throwable)",null,null,et);
                    result = false;  // keep going, but return false to signal problem
                }
            }
        }
        log.debug("Done processing deferred load list with result: "+result);
        return result;
    }

    private static final String fileLocation = "layout"+File.separator;

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
     * @param f Local filename, perhaps without path information
     * @return Corresponding File object
     */
    public File find(String f) {
        File result;
        result = findFile(f);
        if (result != null) {
            log.debug("found at "+result.getAbsolutePath());
            return result;
        } else if ( null != (result = findFile(fileLocation+f) )) {
            log.debug("found at "+result.getAbsolutePath());
            return result;
        } else if ((result = new File(f)).exists() ) {
            log.debug("found at "+result.getAbsolutePath());
            return result;
        } else {
            locateFileFailed(f);
            return null;
        }
    }

    /**
     * Report a failure to find a file.  This is a separate member
     * to ease testing.
     * @param f Name of file not located.
     */
    void locateFileFailed(String f) {
        log.warn("Could not locate file "+f);
    }

    /**
     * Invoke common handling of errors that
     * happen during the "load" process.
     * <p>
     * Generally, this is invoked by {@link XmlAdapter}
     * implementations of their creationErrorEncountered()
     * method (note different arguemments, though). The
     * standard implemenation of that is in {@link AbstractXmlAdapter}.
     * <p>
     * Exceptions passed into this are absorbed.
     *
     * @param adapter Object that encountered the error (for reporting),
     *                  may be null
     * @param operation description of the operation being attempted,
     *                  may be null
     * @param description description of error encountered
     * @param systemName System name of bean being handled, may be null
     * @param userName used name of the bean being handled, may be null
     * @param exception Any exception being handled in the processing, may be null
     */
    static public void creationErrorEncountered (
                XmlAdapter adapter,
                String operation,
                org.apache.log4j.Level level,
                String description, 
                String systemName, 
                String userName, 
                Throwable exception)
    {
        // format and log a message (note reordered from arguments)
        ErrorMemo e = new ErrorMemo(level,
                            adapter, operation, description, 
                            systemName, userName, exception, "loading");

        handler.handle(e);
    }
    
    /**
     * Invoke common handling of errors that
     * happen during the "store" process.
     * <p>
     * Generally, this is invoked by {@link XmlAdapter}
     * implementations of their creationErrorEncountered()
     * method (note different arguemments, though). The
     * standard implemenation of that is in {@link AbstractXmlAdapter}.
     * <p>
     * Exceptions passed into this are absorbed.
     *
     * @param adapter Object that encountered the error (for reporting),
     *                  may be null
     * @param operation description of the operation being attempted,
     *                  may be null
     * @param description description of error encountered
     * @param systemName System name of bean being handled, may be null
     * @param userName used name of the bean being handled, may be null
     * @param exception Any exception being handled in the processing, may be null
     */
    
    static public void storingErrorEncountered (
                XmlAdapter adapter,
                String operation,
                org.apache.log4j.Level level,
                String description, 
                String systemName, 
                String userName, 
                Throwable exception)
    {
        // format and log a message (note reordered from arguments)
        ErrorMemo e = new ErrorMemo(level,
                            adapter, operation, description, 
                            systemName, userName, exception, "storing");

        handler.handle(e);
    }
    
    static ErrorHandler handler = new ErrorHandler();
    static public void setErrorHandler(ErrorHandler handler) { ConfigXmlManager.handler = handler; }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConfigXmlManager.class.getName());
}

