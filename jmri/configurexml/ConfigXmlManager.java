package jmri.configurexml;

import java.io.File;
import org.jdom.*;
import org.jdom.output.*;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import jmri.*;

/**
 * Provides the mechanisms for storing an entire layout configuration
 * to XML.  "Layout" refers to the hardware:  Specific communcation
 * systems, etc.
 * @see <A HREF="package-summary.html">Package summary for details of the overall structure</A>
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public class ConfigXmlManager extends jmri.jmrit.XmlFile
                                implements jmri.ConfigureManager {

    public ConfigXmlManager() {
    }

    /**
     * Register an object whose state is to be tracked.
     * @param o The object, which must have an
     *              associated adapter class.
     */
    public void register(Object o) {
        // skip if already present
        if (list.contains(o)) return;
        // find the class name of the adapter
        String adapter = adapterName(o);
        if (adapter!=null)
            try {
                Class.forName(adapter);
            } catch (java.lang.ClassNotFoundException ex) {
                locateFailed(ex, adapter, o);
            }
        // and add to list
        list.add(o);
    }

    public void deregister(Object o) {
        list.remove(o);
    }

    ArrayList list = new ArrayList();

    /**
     * Find the name of the adapter class for an object.
     * @param o object of a configurable type
     * @return class name of adapter
     */
    static String adapterName(Object o) {
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
     * one-liner in this class, it's a separate member to facilitate testing.
     */
    void locateFailed(java.lang.ClassNotFoundException ex, String adapterName, Object o) {
        log.error("could not load adapter class "+adapterName);
    }

	public void store(File file) {
        // ensure that certain items from the InstanceManager are registered
        // to be stored.  (Others are registered as they are created)
        register(InstanceManager.turnoutManagerInstance());

        // do the write
		try {
			// This is taken in large part from "Java and XML" page 368

			// create root element
			Element root = new Element("layout-config");
			Document doc = new Document(root);
			doc.setDocType(new DocType("layout-config","layout-config.dtd"));

            // get the registered objects and store as top-level elements
            for (int i=0; i<list.size(); i++) {
                Object o = list.get(i);
                Element e = elementFromObject(o);
                if (e!=null) root.addContent(e);
            }

			// write the result to selected file
			java.io.FileOutputStream o = new java.io.FileOutputStream(file);
			XMLOutputter fmt = new XMLOutputter();
			fmt.setNewlines(true);   // pretty printing
			fmt.setIndent(true);
			fmt.output(doc, o);
        }
		catch (Exception e) {
			log.error("exception during config write "+e);
            e.printStackTrace();
		}
	}

    static public Element elementFromObject(Object o)
            throws java.lang.InstantiationException, java.lang.IllegalAccessException {
        String aName = adapterName(o);
        log.debug("store using "+aName);
        XmlAdapter adapter = null;
        try {
            adapter = (XmlAdapter)Class.forName(adapterName(o)).newInstance();
        } catch (java.lang.ClassNotFoundException ex) {
            log.fatal("Cannot load configuration adapter for "+o.getClass().getName());
        }
        if (adapter!=null){
            return adapter.store(o);
        } else {
            log.fatal("Cannot store configuration for "+o.getClass().getName());
            return null;
        }
    }

    public void load(File fi) {
        try {
            Element root = super.rootFromFile(fi);
            // get the objects to load
            List items = root.getChildren();
            for (int i = 0; i<items.size(); i++) {
                // get the class, hence the adapter object to do loading
                Element item = (Element)items.get(i);
                String adapterName = item.getAttribute("class").getValue();
                log.debug("load via "+adapterName);
                try {
                    XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                    // and do it
                    adapter.load(item);
                } catch (Exception e) {
                    log.error("Exception while loading "+item.getName()+":"+e);
                }
            }

        }
        catch (org.jdom.JDOMException e) { log.error("Exception reading: "+e); }
        catch (java.io.IOException e) { log.error("Exception reading: "+e); }
    }

	static public String fileLocation = "layout"+File.separator;

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConfigXmlManager.class.getName());
}