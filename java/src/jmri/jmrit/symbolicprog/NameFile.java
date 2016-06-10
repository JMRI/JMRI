package jmri.jmrit.symbolicprog;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import jmri.jmrit.XmlFile;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// try to limit the JDOM to this class, so that others can manipulate...
/**
 * Represents a set of standard names and aliases in memory.
 * <P>
 * This class doesn't provide tools for defining the names {@literal &} aliases;
 * that's done manually, or at least not done here, to create the file.
 * <P>
 * Initially, we only need one of these, so we use an "instance" method to
 * locate the one associated with the "xml/names.xml" file.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class NameFile extends XmlFile {

    // fill in abstract members
    //protected List<Element> nameElementList = new ArrayList<Element>();
    //public int numNames() { return nameElementList.size(); }
    public Set<String> names() {
        //List<String> list = new ArrayList<String>();
        //for (int i = 0; i<nameElementList.size(); i++) 
        //    list.add(nameElementList.get());
        return _nameHash.keySet();
    }

    // hold names in a Hashtable
    protected Hashtable<String, Element> _nameHash = new Hashtable<String, Element>();

    public Element elementFromName(String name) {
        return _nameHash.get(name);
    }

    static NameFile _instance = null;

    public synchronized static NameFile instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("NameFile creating instance");
            }
            // create and load
            _instance = new NameFile();
            try {
                _instance.readFile(defaultNameFilename());
            } catch (Exception e) {
                log.error("Exception during name file reading: " + e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("NameFile returns instance " + _instance);
        }
        return _instance;
    }

    /**
     * Check to see if a name is present in the file
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
     * Return the filename String for the default file, including location. This
     * is here to allow easy override in tests.
     */
    protected static String defaultNameFilename() {
        return fileLocation + nameFileName;
    }

    static String fileLocation = "";
    static String nameFileName = "names.xml";
    // initialize logging
    static private Logger log = LoggerFactory.getLogger(NameFile.class.getName());

}
