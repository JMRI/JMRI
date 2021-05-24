package jmri.jmrit.logixng.implementation.configurexml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultLogixNGManager;

import org.jdom2.Element;

import jmri.jmrit.logixng.implementation.ClipboardMany;
import jmri.jmrit.logixng.implementation.DefaultClipboard;
import jmri.jmrit.logixng.implementation.DefaultLogixNG;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.ThreadingUtil;

/**
 * Provides the functionality for configuring LogixNGManagers
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public class DefaultLogixNGManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultLogixNGManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a LogixNG_Manager
     *
     * @param o Object to store, of type LogixNG_Manager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element logixNGs = new Element("LogixNGs");
        setStoreElementClass(logixNGs);
        LogixNG_Manager tm = (LogixNG_Manager) o;
        if (tm != null) {
            for (LogixNG_Thread thread : LogixNG_Thread.getThreads()) {
                Element e = new Element("Thread");  // NOI18N
                e.addContent(new Element("id").addContent(Integer.toString(thread.getThreadId())));
                e.addContent(new Element("name").addContent(thread.getThreadName()));
                logixNGs.addContent(e);
            }

            for (LogixNG logixNG : tm.getNamedBeanSet()) {
                log.debug("logixng system name is " + logixNG.getSystemName());  // NOI18N
                boolean enabled = logixNG.isEnabled();
                Element elem = new Element("LogixNG");  // NOI18N
                elem.addContent(new Element("systemName").addContent(logixNG.getSystemName()));  // NOI18N

                // store common part
                storeCommon(logixNG, elem);

                Element e = new Element("ConditionalNGs");
                for (int i=0; i < logixNG.getNumConditionalNGs(); i++) {
                    e.addContent(new Element("systemName").addContent(logixNG.getConditionalNG(i).getSystemName()));
                }
                elem.addContent(e);

                elem.setAttribute("enabled", enabled ? "yes" : "no");  // NOI18N

                logixNGs.addContent(elem);
            }

            Element elemInitializationTable = new Element("InitializationTable");  // NOI18N
            for (LogixNG logixNG : InstanceManager.getDefault(LogixNG_InitializationManager.class).getList()) {
                Element e = new Element("LogixNG").addContent(logixNG.getSystemName());   // NOI18N
                elemInitializationTable.addContent(e);
            }
            logixNGs.addContent(elemInitializationTable);

            // Store items on the clipboard
            Element elemClipboard = new Element("Clipboard");  // NOI18N
            Clipboard clipboard = tm.getClipboard();
            if (clipboard.getFemaleSocket().isConnected()) {
                Base rootObject = clipboard.getFemaleSocket().getConnectedSocket().getObject();
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(rootObject);
                    if (e != null) {
                        elemClipboard.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing action: {}", e, e);
                }
            }
            logixNGs.addContent(elemClipboard);
        }
        return (logixNGs);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param logixngs The top-level element being created
     */
    public void setStoreElementClass(Element logixngs) {
        logixngs.setAttribute("class", this.getClass().getName());  // NOI18N
    }

    /**
     * Create a LogixNG_Manager object of the correct class, then register and
     * fill it.
     *
     * @param sharedLogixNG  Shared top level Element to unpack.
     * @param perNodeLogixNG Per-node top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedLogixNG, Element perNodeLogixNG) {
        // create the master object
        replaceLogixNGManager();
        // load individual sharedLogix
        loadThreads(sharedLogixNG);
        loadLogixNGs(sharedLogixNG);
        loadInitializationTable(sharedLogixNG);
        loadClipboard(sharedLogixNG);
        return true;
    }

    /**
     * Utility method to load the individual LogixNG objects. If there's no
     * additional info needed for a specific logixng type, invoke this with the
     * parent of the set of LogixNG elements.
     *
     * @param sharedLogixNG Element containing the LogixNG elements to load.
     */
    public void loadThreads(Element sharedLogixNG) {
        List<Element> threads = sharedLogixNG.getChildren("Thread");  // NOI18N
        log.debug("Found " + threads.size() + " threads");  // NOI18N

        for (int i = 0; i < threads.size(); i++) {

            Element threadElement = threads.get(i);

            int threadId = Integer.parseInt(threadElement.getChild("id").getTextTrim());
            String threadName = threadElement.getChild("name").getTextTrim();

            log.debug("create thread: " + Integer.toString(threadId) + ", " + threadName);  // NOI18N
            LogixNG_Thread.createNewThread(threadId, threadName);
        }
    }

    /**
     * Utility method to load the individual LogixNG objects. If there's no
     * additional info needed for a specific logixng type, invoke this with the
     * parent of the set of LogixNG elements.
     *
     * @param sharedLogixNG Element containing the LogixNG elements to load.
     */
    public void loadLogixNGs(Element sharedLogixNG) {
        List<Element> logixNGList = sharedLogixNG.getChildren("LogixNG");  // NOI18N
        log.debug("Found " + logixNGList.size() + " logixngs");  // NOI18N
        LogixNG_Manager tm = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class);

        for (int i = 0; i < logixNGList.size(); i++) {

            Element logixNG_Element = logixNGList.get(i);

            String sysName = getSystemName(logixNG_Element);
            if (sysName == null) {
                log.warn("unexpected null in systemName " + logixNG_Element);  // NOI18N
                break;
            }

            String userName = getUserName(logixNG_Element);

            String yesno = "";
            if (logixNGList.get(i).getAttribute("enabled") != null) {  // NOI18N
                yesno = logixNG_Element.getAttribute("enabled").getValue();  // NOI18N
            }
            log.debug("create logixng: (" + sysName + ")("  // NOI18N
                    + (userName == null ? "<null>" : userName) + ")");  // NOI18N

            // Create a new LogixNG but don't setup the initial tree.
            DefaultLogixNG logixNG = (DefaultLogixNG)tm.createLogixNG(sysName, userName);
            if (logixNG != null) {
                // load common part
                loadCommon(logixNG, logixNGList.get(i));

                // set enabled/disabled if attribute was present
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {  // NOI18N
                        logixNG.setEnabled(true);
                    } else if (yesno.equals("no")) {  // NOI18N
                        logixNG.setEnabled(false);
                    }
                }

                List<Element> conditionalNGList =
                        logixNG_Element.getChild("ConditionalNGs").getChildren();  // NOI18N

                for (int j = 0; j < conditionalNGList.size(); j++) {

                    Element systemNameElement = conditionalNGList.get(j);
                    String systemName = null;
                    if (systemNameElement != null) {
                        systemName = systemNameElement.getTextTrim();
                    }
                    logixNG.setConditionalNG_SystemName(j, systemName);
                }
            }
        }
    }

    public void loadInitializationTable(Element sharedLogixNG) {
        LogixNG_Manager tm =
                InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class);

        LogixNG_InitializationManager initializationManager =
                InstanceManager.getDefault(LogixNG_InitializationManager.class);

        List<Element> initTableList = sharedLogixNG.getChildren("InitializationTable");  // NOI18N
        if (initTableList.isEmpty()) return;
        List<Element> logixNGList = initTableList.get(0).getChildren();
        if (logixNGList.isEmpty()) return;
        for (Element e : logixNGList) {
            LogixNG logixNG = tm.getBySystemName(e.getTextTrim());
            if (logixNG != null) {
                initializationManager.add(logixNG);
            } else {
                log.warn("LogixNG '{}' cannot be found", e.getTextTrim());
            }
        }
    }

    public void loadClipboard(Element sharedLogixNG) {
        List<Element> clipboardList = sharedLogixNG.getChildren("Clipboard");  // NOI18N
        if (clipboardList.isEmpty()) return;
        List<Element> clipboardSubList = clipboardList.get(0).getChildren();
        if (clipboardSubList.isEmpty()) return;

        String className = clipboardSubList.get(0).getAttribute("class").getValue();
//        log.error("className: " + className);

        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            log.error("cannot load class " + className, ex);
            return;
        }

        Constructor<?> c;
        try {
            c = clazz.getConstructor();
        } catch (NoSuchMethodException | SecurityException ex) {
            log.error("cannot create constructor", ex);
            return;
        }

        try {
            Object o = c.newInstance();

            if (o == null) {
                log.error("class is null");
                return;
            }
            if (! (o instanceof ClipboardManyXml)) {
                log.error("class has wrong type: " + o.getClass().getName());
                return;
            }

            LogixNG_Manager tm = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class);
            ClipboardMany anyMany = ((ClipboardManyXml)o).loadItem(clipboardList.get(0));
            List<String> errors = new ArrayList<>();
            if (! ((DefaultClipboard)tm.getClipboard()).replaceClipboardItems(anyMany, errors)) {
                for (String s : errors) log.error(s);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.error("cannot create object", ex);
        }
    }

    /**
     * Replace the current LogixManager, if there is one, with one newly created
     * during a load operation. This is skipped if they are of the same absolute
     * type.
     */
    protected void replaceLogixNGManager() {
        if (InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).getClass().getName()
                .equals(DefaultLogixNGManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.LogixNG_Manager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class));
            }

        }

        ThreadingUtil.runOnGUI(() -> {
            // register new one with InstanceManager
            DefaultLogixNGManager pManager = DefaultLogixNGManager.instance();
            InstanceManager.store(pManager, LogixNG_Manager.class);
            // register new one for configuration
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.registerConfig(pManager, jmri.Manager.LOGIXNGS);
            }
        });
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).getXMLOrder();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultLogixNGManagerXml.class);
}
