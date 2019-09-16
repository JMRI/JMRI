package jmri.jmrit.revhistory.configurexml;

import java.util.ArrayList;
import jmri.jmrit.revhistory.FileHistory;
import org.jdom2.Element;

/**
 * Load/Store FileHistory objects.
 * <p>
 * This interacts somewhat differently with the ConfigureXML system. FileHistory
 * objects are _not_ registed with the manager, but rather handled explicitly by
 * them. The "load()" method is therefore a null-op here.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 */
public class FileHistoryXml extends jmri.configurexml.AbstractXmlAdapter {

    /**
     * Usual configurexml method, this one doesn't do anything because the
     * content is explicitly loaded from the file
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        return true;
    }

    /**
     * Load RevHistory from an element.
     *
     * <p>
     * If no RevHistory already present in InstanceManager, creates one and adds
     * this.
     * <p>
     * Then adds, instead of replacing, the history information
     */
    public boolean loadDirectly(Element e) {
        if (!e.getName().equals("filehistory")) {
            log.error("Unexpected element name: {}", e.getName());
            return false;
        }

        FileHistory rmain = jmri.InstanceManager.getDefault(FileHistory.class);

        FileHistory r = loadFileHistory(e);
        rmain.addOperation("Load", "", r);

        return true;
    }

    static public FileHistory loadFileHistory(Element e) {
        FileHistory r = new FileHistory();

        java.util.List<Element> list = e.getChildren("operation");
        for (int i = 0; i < list.size(); i++) {
            loadOperation(r, list.get(i));
        }
        return r;
    }

    static public void loadOperation(FileHistory r, Element e) {
        Element s;

        String type = null;
        s = e.getChild("type");
        if (s != null) {
            type = s.getText();
        }

        String date = null;
        s = e.getChild("date");
        if (s != null) {
            date = s.getText();
        }

        String filename = null;
        s = e.getChild("filename");
        if (s != null) {
            filename = s.getText();
        }

        FileHistory filehistory = null;
        s = e.getChild("filehistory");
        if (s != null) {
            filehistory = loadFileHistory(s);
        }

        r.addOperation(type, date, filename, filehistory);
    }

    /**
     * Create a set of configured objects from their XML description, using an
     * auxiliary object.
     * <p>
     * For example, the auxilary object o might be a manager or GUI of some type
     * that needs to be informed as each object is created.
     *
     * @param e Top-level XML element containing the description
     * @param o Implementation-specific Object needed for the conversion
     */
    @Override
    public void load(Element e, Object o) {
        throw new UnsupportedOperationException("Method not coded");
    }

    /**
     * Store the
     *
     * @param o The object to be recorded. Specific XmlAdapter implementations
     *          will require this to be of a specific type; that binding is done
     *          in ConfigXmlManager.
     * @return The XML representation Element
     */
    @Override
    public Element store(Object o) {
        return storeDirectly(o);
    }

    static int defaultDepth = 5;

    static public Element storeDirectly(Object o) {
        final FileHistory r = (FileHistory) o;
        if (r == null) {
            return null;  // no file history object, not recording
        }
        Element e = historyElement(r, defaultDepth);

        // add one more element for this store
        FileHistory.OperationMemo rev
                = r.new OperationMemo() {
                    {
                        type = "Store";
                        date = (new java.util.Date()).toString();
                        filename = "";
                        history = null;
                    }
                };

        e.addContent(
                operationElement(rev, 10)
        );
        // and return
        return e;
    }

    static Element historyElement(FileHistory r, int depth) {
        ArrayList<FileHistory.OperationMemo> list = r.getList();

        Element e = new Element("filehistory");

        for (int i = 0; i < list.size(); i++) {
            Element operation = operationElement(list.get(i), depth);
            e.addContent(operation);
        }

        return e;
    }

    static Element operationElement(FileHistory.OperationMemo r, int depth) {
        Element rev = new Element("operation");

        Element revnumber = new Element("type");
        revnumber.addContent("" + r.type);
        rev.addContent(revnumber);

        Element date = new Element("date");
        date.addContent(r.date);
        rev.addContent(date);

        Element authorinitials = new Element("filename");
        authorinitials.addContent(r.filename);
        rev.addContent(authorinitials);

        if (r.history != null && depth >= 1) {
            rev.addContent(historyElement(r.history, depth - 1));
        }

        return rev;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileHistoryXml.class);
}
