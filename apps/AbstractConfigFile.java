// AbstractConfigFile.java

package apps;

import com.sun.java.util.collections.List;
import java.io.*;
import jmri.jmrit.XmlFile;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * Abstract base class to represent and manipulate the preferences information for an
 * application. Works with the AbstractConfigFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		 	$Revision: 1.9 $
 * @see apps.AbstractConfigFrame
 */
abstract public class AbstractConfigFile extends XmlFile {

    public void readFile(String name) throws java.io.FileNotFoundException, org.jdom.JDOMException {
        Element root = rootFromName(name);
        readConnection(root);
        _gui = root.getChild("gui");
        _programmer = root.getChild("programmer");
        _perform = root.getChild("perform");
    }

    protected void readConnection(Element root) {
        _connection = root.getChild("connection");
    }

    // access to the four elements
    public Element getConnectionElement() {
        return _connection;
    }

    public Element getGuiElement() {
        return _gui;
    }

    public Element getProgrammerElement() {
        return _programmer;
    }

    public Element getPerformElement() {
        return _perform;
    }

    protected Element _connection;
    protected Element _gui;
    protected Element _programmer;
    protected Element _perform;

    public void writeFile(String name, AbstractConfigFrame f) {
        try {
            // This is taken in large part from "Java and XML" page 368

            // create file Object
            XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
            File file = new File(prefsDir()+name);

            // create root element
            Element root = new Element("preferences-config");
            Document doc = new Document(root);
            doc.setDocType(new DocType("preferences-config","preferences-config.dtd"));

            Element values;

            // add connection element
            writeConnection(root, f);

            // add gui element
            root.addContent(f.getGUI());

            // add programmer element
            root.addContent(f.getProgrammer());

            // write the result to selected file
            java.io.FileOutputStream o = new java.io.FileOutputStream(file);
            XMLOutputter fmt = new XMLOutputter();
            fmt.setNewlines(true);   // pretty printing
            fmt.setIndent(true);
            fmt.output(doc, o);
            o.close();
        }
        catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    protected void writeConnection(Element root, AbstractConfigFrame f) {
        // add connection element
        root.addContent(f.getCommPane().getConnection());
    }

    abstract protected String configFileName();

    public String defaultConfigFilename() { return configFileName();}

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractConfigFile.class.getName());

}
