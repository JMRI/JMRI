package jmri.managers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalSystem;
import jmri.SignalSystemManager;
import jmri.implementation.DefaultSignalSystem;
import jmri.jmrit.XmlFile;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a SignalSystemManager.
 * <p>
 * This loads automatically the first time used.
 * <p>
 *
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalSystemManager extends AbstractManager<SignalSystem>
        implements SignalSystemManager {

    public DefaultSignalSystemManager(InternalSystemConnectionMemo memo) {
        super(memo);

        // load when created, which will generally
        // be the first time referenced
        load();
    }

    @Override
    public int getXMLOrder() {
        return 65400;
    }

    /**
     * Don't want to store this information
     */
    @Override
    protected void registerSelf() {
    }

    @Override
    public char typeLetter() {
        return 'F';
    }

    @Override
    public SignalSystem getSystem(String name) {
        SignalSystem t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    @Override
    public SignalSystem getBySystemName(String key) {
        return _tsys.get(key);
    }

    @Override
    public SignalSystem getByUserName(String key) {
        return _tuser.get(key);
    }

    void load() {
        List<String> list = getListOfNames();
        for (int i = 0; i < list.size(); i++) {
            SignalSystem s = makeBean(list.get(i));
            register(s);
        }
    }

    List<String> getListOfNames() {
        List<String> retval = new ArrayList<>();
        // first locate the signal system directory
        // and get names of systems
        File signalDir = null;
        //First get the default pre-configured signalling systems
        try {
            signalDir = new File(FileUtil.findURL("xml/signals", FileUtil.Location.INSTALLED).toURI());
        } catch (URISyntaxException | NullPointerException ex) {
            log.error("Unable to get installed signals.", ex);
        }
        if (signalDir != null) {
            File[] files = signalDir.listFiles();
            if (files != null) { // null if not a directory
                for (File file : files) {
                    if (file.isDirectory()) {
                        // check that there's an aspects.xml file
                        File aspects = new File(file.getPath() + File.separator + "aspects.xml");
                        if (aspects.exists()) {
                            log.debug("found system: " + file.getName());
                            retval.add(file.getName());
                        }
                    }
                }
            }
        }
        //Now get the user defined systems.
        try {
            URL dir = FileUtil.findURL("signals", FileUtil.Location.USER, "resources", "xml");
            if (dir == null) {
                try {
                    if (!(new File(FileUtil.getUserFilesPath(), "xml/signals")).mkdirs()) {
                        log.error("Error while creating xml/signals directory");
                    }
                } catch (Exception ex) {
                    log.error("Unable to create user's signals directory.", ex);
                }
                dir = FileUtil.findURL("xml/signals", FileUtil.Location.USER);
            }
            signalDir = new File(dir.toURI());
        } catch (URISyntaxException ex) {
            log.error("Unable to get installed signals.", ex);
        }
        if (signalDir != null) {
            File[] files = signalDir.listFiles();
            if (files != null) { // null if not a directory
                for (File file : files) {
                    if (file.isDirectory()) {
                        // check that there's an aspects.xml file
                        File aspects = new File(file.getPath() + File.separator + "aspects.xml");
                        if ((aspects.exists()) && (!retval.contains(file.getName()))) {
                            log.debug("found system: " + file.getName());
                            retval.add(file.getName());
                        }
                    }
                }
            }
        }
        return retval;
    }

    SignalSystem makeBean(String name) {

        //First check to see if the bean is in the default system directory
        URL path = FileUtil.findURL("xml/signals/" + name + "/aspects.xml", FileUtil.Location.INSTALLED);
        log.debug("load from {}", path);
        XmlFile xf = new AspectFile();
        if (path != null) {
            try {
                Element root = xf.rootFromURL(path);
                DefaultSignalSystem s = new DefaultSignalSystem(name);
                loadBean(s, root);
                return s;
            } catch (IOException | JDOMException e) {
                log.error("Could not parse aspect file \"{}\" due to: {}", path, e);
            }
        }

        //if the file doesn't exist or fails the load from the default location then try the user directory
        path = FileUtil.findURL("signals/" + name + "/aspects.xml", FileUtil.Location.USER, "xml", "resources");
        log.debug("load from {}", path);
        if (path != null) {
            xf = new AspectFile();
            try {
                Element root = xf.rootFromURL(path);
                DefaultSignalSystem s = new DefaultSignalSystem(name);
                loadBean(s, root);
                return s;
            } catch (IOException | JDOMException e) {
                log.error("Could not parse aspect file \"{}\" due to: {}", path, e);
            }
        }

        return null;
    }

    void loadBean(DefaultSignalSystem s, Element root) {
        List<Element> l = root.getChild("aspects").getChildren("aspect");

        // set user name from system name element
        s.setUserName(root.getChild("name").getText());

        // find all aspects, include them by name, 
        // add all other sub-elements as key/value pairs
        for (int i = 0; i < l.size(); i++) {
            String name = l.get(i).getChild("name").getText();
            log.debug("aspect name {}", name);

            List<Element> c = l.get(i).getChildren();

            for (int j = 0; j < c.size(); j++) {
                // note: includes setting name; redundant, but needed
                s.setProperty(name, c.get(j).getName(), c.get(j).getText());
            }
        }

        if (root.getChild("imagetypes") != null) {
            List<Element> t = root.getChild("imagetypes").getChildren("imagetype");
            for (int i = 0; i < t.size(); i++) {
                String type = t.get(i).getAttribute("type").getValue();
                s.setImageType(type);
            }
        }
        //loadProperties(s, root);
        if (root.getChild("properties") != null) {
            for (Object next : root.getChild("properties").getChildren("property")) {
                Element e = (Element) next;

                try {
                    Class<?> cl;
                    Constructor<?> ctor;
                    
                    // create key string
                    String key = e.getChild("key").getText();
                    
                    // check for non-String key.  Warn&proceed if found.
                    // Pre-JMRI 4.3, keys in NamedBean parameters could be Objects
                    // constructed from Strings, similar to the value code below.
                    if (! (
                        e.getChild("key").getAttributeValue("class") == null
                        || e.getChild("key").getAttributeValue("class").equals("")
                        || e.getChild("key").getAttributeValue("class").equals("java.lang.String")
                        )) {
                        
                        log.warn("SignalSystem {} property key of invalid non-String type {} not supported", 
                            s.getSystemName(), e.getChild("key").getAttributeValue("class"));
                    }
                    
                    // create value object
                    Object value = null;
                    if (e.getChild("value") != null) {
                        cl = Class.forName(e.getChild("value").getAttributeValue("class"));
                        ctor = cl.getConstructor(new Class<?>[]{String.class});
                        value = ctor.newInstance(new Object[]{e.getChild("value").getText()});
                    }

                    // store
                    s.setProperty(key, value);
                } catch (ClassNotFoundException 
                            | NoSuchMethodException | InstantiationException
                            | IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
                    log.error("Error loading properties", ex);
                }
            }
        }
    }

    void loadProperties(NamedBean t, Element elem) {
        // do nothing
    }

    /**
     * XmlFile is abstract, so this extends for local use
     */
    static class AspectFile extends XmlFile {
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameSignalSystems" : "BeanNameSignalSystem");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<SignalSystem> getNamedBeanClass() {
        return SignalSystem.class;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalSystemManager.class);
}
