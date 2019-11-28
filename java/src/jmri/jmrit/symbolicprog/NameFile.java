package jmri.jmrit.symbolicprog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.XmlFile;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// try to limit the JDOM to this class, so that others can manipulate...
/**
 * Represents a set of standard names and aliases in memory.
 * <p>
 * This class doesn't provide tools for defining the names and aliases; that's
 * done manually, or at least not done here, to create the file.
 * <p>
 * This automatically initializes from the default file if requested
 * from the InstanceManager.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class NameFile extends XmlFile {

    public Set<String> names() {
        return _nameHash.keySet();
    }

    // hold names in a HashMap
    protected HashMap<String, Element> _nameHash = new HashMap<>();

    public Element elementFromName(String name) {
        return _nameHash.get(name);
    }

    /**
     * Check to see if a name is present in the file.
     *
     * @param name the name to check
     * @return true if present; false otherwise
     */
    public boolean checkName(String name) {
        return (elementFromName(name) != null);
    }

    /**
     * Read the contents of a NameFile XML file into this object. Note that this
     * does not clear any existing entries.
     */
    void readFile(String name) throws org.jdom2.JDOMException, java.io.IOException {
        if (log.isDebugEnabled()) {
            log.debug("readFile " + name);
        }

        // read file, find root
        Element root = rootFromName(name);
        // decode type, invoke proper processing routine
        readNames(root);
    }

    void readNames(Element root) {

        List<Element> l = root.getChildren("definition");
        if (log.isDebugEnabled()) {
            log.debug("readNames sees " + l.size() + " direct children");
        }
        for (int i = 0; i < l.size(); i++) {
            // handle each entry
            Element el = l.get(i);
            storeDefinition(el);
        }
        // now recurse with "definitiongroup" children
        l = root.getChildren("definitiongroup");
        if (log.isDebugEnabled()) {
            log.debug("readNames sees " + l.size() + " groups");
        }
        for (int i = 0; i < l.size(); i++) {
            // handle each entry
            Element el = l.get(i);
            readNames(el);
        }

    }

    void storeDefinition(Element el) {
        String name = el.getAttribute("item").getValue();
        _nameHash.put(name, el);
    }

    /**
     * Get the filename for the default file, including location. This is here
     * to allow easy override in tests.
     *
     * @return the default filename
     */
    protected static String defaultNameFilename() {
        return fileLocation + nameFileName;
    }

    static String fileLocation = "";
    static String nameFileName = "names.xml";

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(NameFile.class)) {
                if (log.isDebugEnabled()) {
                    log.debug("NameFile creating instance");
                }
                // create and load
                NameFile instance = new NameFile();
                try {
                    instance.readFile(defaultNameFilename());
                } catch (IOException | JDOMException e) {
                    log.error("Exception during name file reading: {}", e.getMessage());
                }
                log.debug("NameFile returns instance {}", instance);
                return instance;
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(NameFile.class);
            return set;
        }

    }
    
    private final static Logger log = LoggerFactory.getLogger(NameFile.class);
}
