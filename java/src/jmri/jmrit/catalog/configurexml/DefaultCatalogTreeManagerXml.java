package jmri.jmrit.catalog.configurexml;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring the
 * CatalogTreeManager.
 * <P>
 * Typically, a subclass will just implement the load(Element catalogTree)
 * class, relying on implementation here to load the individual CatalogTree
 * objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2009
 *
 */
public class DefaultCatalogTreeManagerXml extends XmlFile /* extends jmri.configurexml.AbstractXmlAdapter */ {

    private static String defaultFileName = FileUtil.getUserFilesPath() + "catalogTrees.xml";

    public DefaultCatalogTreeManagerXml() {
    }

    /*
     *  Writes out tree values to a file in the user's preferences directory
     */
    public void writeCatalogTrees() throws java.io.IOException {
        if (log.isDebugEnabled()) {
            log.debug("entered writeCatalogTreeValues");
        }
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List<String> trees = manager.getSystemNameList();
        boolean found = false;
        Iterator<String> iter = manager.getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sname = iter.next();
            CatalogTree tree = manager.getBySystemName(sname);
            if (log.isDebugEnabled()) {
                log.debug("Tree: sysName= " + sname + ", userName= " + tree.getUserName());
                CatalogTreeNode root = (CatalogTreeNode) tree.getRoot();
                log.debug("enumerateTree called for root= " + root.toString()
                        + ", has " + root.getChildCount() + " children");

                @SuppressWarnings("unchecked") // root.depthFirstEnumeration isn't fully typed in JDOM2
                Enumeration<CatalogTreeNode> e = root.depthFirstEnumeration();
                while (e.hasMoreElements()) {
                    CatalogTreeNode n = e.nextElement();
                    log.debug("nodeName= " + n.getUserObject() + " has " + n.getLeaves().size()
                            + " leaves and " + n.getChildCount() + " subnodes.");
                }
            }
            if (sname != null && sname.charAt(1) == CatalogTree.XML) {
                found = true;
                break;
            }
        }
        if (found) {
            // there are trees defined, create root element
            Element root = new Element("catalogTrees");
            Document doc = newDocument(root, dtdLocation + "catalogTree.dtd");

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/tree-values.xsl"?>
            java.util.Map<String, String> m = new java.util.HashMap<String, String>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation + "panelfile.xsl");
            org.jdom2.ProcessingInstruction p = new org.jdom2.ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            store(root, trees);

            try {
                if (!checkFile(defaultFileName)) {
                    // file does not exist, create it
                    File file = new File(defaultFileName);
                    if (!file.createNewFile()) {
                        log.error("createNewFile failed");
                    }
                }
                // write content to file
                writeXML(findFile(defaultFileName), doc);
                // memory consistent with file
                jmri.jmrit.catalog.ImageIndexEditor.indexChanged(false);
            } catch (java.io.IOException ioe) {
                log.error("IO Exception " + ioe);
                throw (ioe);
            }
        }
    }

    /**
     * Default implementation for storing the contents of a CatalogTreeManager
     *
     * @param cat   Element to load with contents
     * @param trees List of contents
     */
    public void store(Element cat, List<String> trees) {
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        cat.setAttribute("class", "jmri.jmrit.catalog.DefaultCatalogTreeManagerConfigXML");
        Iterator<String> iter = trees.iterator();
        while (iter.hasNext()) {
            String sname = iter.next();
            if (sname == null) {
                log.error("System name null during store");
                continue;
            }
            if (log.isDebugEnabled()) {
                log.debug("system name is " + sname);
            }
            if (sname.charAt(1) != CatalogTree.XML) {
                continue;
            }
            CatalogTree ct = manager.getBySystemName(sname);
            Element elem = new Element("catalogTree");
            elem.setAttribute("systemName", sname);
            String uname = ct.getUserName();
            if (uname != null) {
                elem.setAttribute("userName", uname);
            }

            storeNode(elem, (CatalogTreeNode) ct.getRoot());

            if (log.isDebugEnabled()) {
                log.debug("store CatalogTree " + sname);
            }
            cat.addContent(elem);
        }
    }

    /**
     * Recursively store a CatalogTree
     */
    public void storeNode(Element parent, CatalogTreeNode node) {
        if (log.isDebugEnabled()) {
            log.debug("storeNode " + node.toString()
                    + ", has " + node.getLeaves().size() + " leaves.");
        }
        Element element = new Element("node");
        element.setAttribute("nodeName", node.toString());
        List<CatalogTreeLeaf> leaves = node.getLeaves();
        for (int i = 0; i < leaves.size(); i++) {
            Element el = new Element("leaf");
            CatalogTreeLeaf leaf = leaves.get(i);
            el.setAttribute("name", leaf.getName());
            el.setAttribute("path", leaf.getPath());
            element.addContent(el);
        }
        parent.addContent(element);
        @SuppressWarnings("unchecked") // is node.children actually of <Element> type?
        Enumeration<CatalogTreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            storeNode(element, n);
        }
    }

    /**
     * This is invoked as part of the "store all" mechanism, which is not used
     * for these objects. Hence this is implemented to do nothing.
     */
    public Element store(Object o) {
        return null;
    }

    /*
     *  Reads CatalogTree values from a file in the user's preferences directory
     */
    public void readCatalogTrees() {
        if (log.isDebugEnabled()) {
            log.debug("entered readCatalogTrees");
        }
        //CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        try {
            // check if file exists
            if (checkFile(defaultFileName)) {
                Element root = rootFromName(defaultFileName);
                if (root != null) {
                    load(root);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("File: " + defaultFileName + " not Found");
            }
        } catch (org.jdom2.JDOMException jde) {
            log.error("Exception reading CatalogTrees: " + jde);
        } catch (java.io.IOException ioe) {
            log.error("Exception reading CatalogTrees: " + ioe);
        }
    }

    public void load(Element element, Object o) throws Exception {
    }

    /**
     * Create a CatalogTreeManager object of the correct class, then register
     * and fill it.
     *
     * @param catalogTrees Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element catalogTrees) {
        loadCatalogTrees(catalogTrees);
        return true;
    }

    /**
     * Utility method to load the individual CatalogTree objects.
     */
    public void loadCatalogTrees(Element catalogTrees) {
        List<Element> catList = catalogTrees.getChildren("catalogTree");
        if (log.isDebugEnabled()) {
            log.debug("loadCatalogTrees: found " + catList.size() + " CatalogTree objects");
        }
        CatalogTreeManager mgr = InstanceManager.catalogTreeManagerInstance();

        for (int i = 0; i < catList.size(); i++) {
            Element elem = catList.get(i);
            Attribute attr = elem.getAttribute("systemName");
            if (attr == null) {
                log.warn("unexpected null systemName. elem= " + elem + ", attrs= " + elem.getAttributes());
                continue;
            }
            String sysName = attr.getValue();
            String userName = null;
            attr = elem.getAttribute("userName");
            if (attr == null) {
                log.warn("unexpected null userName. attrs= " + elem.getAttributes());
                continue;
            } else {
                userName = attr.getValue();
            }
            DefaultTreeModel ct = (DefaultTreeModel) mgr.getBySystemName(sysName);
            if (ct != null) {
                continue;   // tree already registered
            }
            ct = (DefaultTreeModel) mgr.newCatalogTree(sysName, userName);
            if (log.isDebugEnabled()) {
                log.debug("CatalogTree: sysName= " + sysName + ", userName= " + userName);
            }
            CatalogTreeNode root = (CatalogTreeNode) ct.getRoot();
            elem = elem.getChild("node");
            loadNode(elem, root, ct);
        }
    }

    private void addLeaves(Element element, CatalogTreeNode node) {
        List<Element> leafList = element.getChildren("leaf");
        for (int i = 0; i < leafList.size(); i++) {
            Element elem = leafList.get(i);
            Attribute attr = elem.getAttribute("name");
            if (attr == null) {
                log.error("unexpected null leaf name. elem= " + elem + ", attrs= " + elem.getAttributes());
                continue;
            }
            String name = attr.getValue();
            attr = elem.getAttribute("path");
            if (attr == null) {
                log.error("unexpected null leaf path. elem= " + elem + ", attrs= " + elem.getAttributes());
                continue;
            }
            String path = attr.getValue();
            // use the method that maintains the same order
            node.addLeaf(new CatalogTreeLeaf(name, path, 0));
        }
    }

    /**
     * Recursively load a CatalogTree
     */
    public void loadNode(Element element, CatalogTreeNode parent, DefaultTreeModel model) {
        List<Element> nodeList = element.getChildren("node");
        if (log.isDebugEnabled()) {
            log.debug("Found " + nodeList.size() + " CatalogTreeNode objects");
        }
        for (int i = 0; i < nodeList.size(); i++) {
            Element elem = nodeList.get(i);
            Attribute attr = elem.getAttribute("nodeName");
            if (attr == null) {
                log.warn("unexpected null nodeName. elem= " + elem + ", attrs= " + elem.getAttributes());
                continue;
            }
            String nodeName = attr.getValue();
            CatalogTreeNode n = new CatalogTreeNode(nodeName);
            addLeaves(elem, n);
            model.insertNodeInto(n, parent, parent.getChildCount());
            loadNode(elem, n, model);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultCatalogTreeManagerXml.class.getName());
}
