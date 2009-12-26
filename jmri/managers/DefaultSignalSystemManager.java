// DefaultSignalSystemManager.java

package jmri.managers;

import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.implementation.AbstractManager;
import jmri.implementation.DefaultSignalAspectTable;

import java.io.*;

import java.util.List;
import java.util.ArrayList;

import org.jdom.Element;

/**
 * Default implementation of a SignalSystemManager.
 * <P>
 * This loads automatically the first time used.
 * <p>
 *
 *
 * @author  Bob Jacobsen Copyright (C) 2009
 * @version	$Revision: 1.1 $
 */
public class DefaultSignalSystemManager extends AbstractManager
    implements SignalSystemManager, java.beans.PropertyChangeListener {

    public DefaultSignalSystemManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'F'; }
    
    void load() {
        List<String> list = getListOfNames();
        for (int i = 0; i < list.size(); i++) {
            SignalAspectTable s = makeBean(list.get(i));
            register(s);
        }
    }

    List<String> getListOfNames() {
        List<String> retval = new ArrayList<String>();
        // first locate the signal system directory
        // and get names of systems
        File signalDir = new File("xml"+File.separator+"signals");
        File[] files = signalDir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                // check that there's an aspects.xml file
                File aspects = new File(files[i].getPath()+File.separator+"aspects.xml");
                if (aspects.exists()) {
                    log.debug("found system: "+files[i].getName());
                    retval.add(files[i].getName());
                }
            }
        }
        return retval;
    }

    SignalAspectTable makeBean(String name) {
        String filename = "xml"+File.separator+"signals"
                            +File.separator+name
                            +File.separator+"aspects.xml";
        log.debug("load from "+filename);
        XmlFile xf = new AspectFile();
        try {
            Element root = xf.rootFromName(filename);
            DefaultSignalAspectTable s = new DefaultSignalAspectTable(name);
            loadBean(s, root);
            return s;
        } catch (Exception e) {
            log.error("Could not parse aspect file: "+e);
        }
        return null;
    }

    void loadBean(DefaultSignalAspectTable s, Element root) {
        List l = root.getChild("aspects").getChildren("aspect");
        // find all aspects, include them by name, 
        // add all other sub-elements as key/value pairs
        for (int i = 0; i < l.size(); i++) {
            String name = ((Element)l.get(i)).getChild("name").getText();
            if (log.isDebugEnabled()) log.debug("aspect name "+name);
            List c = ((Element)l.get(i)).getChildren();
            for (int j = 0; j < c.size(); j++) {
                // note: includes setting name; redundant, but needed
                s.setProperty(name, ((Element)c.get(j)).getName(), ((Element)c.get(j)).getText());
            }
        }
    }

    /** 
     * XmlFile is abstract, so this extends for local use
     */
    class AspectFile extends XmlFile {
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalSystemManager.class.getName());
}

/* @(#)DefaultSignalSystemManager.java */
