package jmri.jmrit.catalog.configurexml;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.tree.*;
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
 * <p>
 * Typically, a subclass will just implement the load(Element catalogTree)
 * class, relying on implementation here to load the individual CatalogTree
 * objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2009
 */
public class DefaultCatalogTreeManagerXml extends XmlFile {

    private final static String DEFAULT_FILE_NAME = FileUtil.getUserFilesPath() + "catalogTrees.xml";

    public DefaultCatalogTreeManagerXml() {
    }

    /**
     * Write out tree values to a file in the user's preferences directory.
     *
     * @throws IOException from any I/O issues during write; not handled locally
     */
    public void writeCatalogTrees() throws IOException {
        log.debug("entered writeCatalogTreeValues");
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        Set<CatalogTree> trees = manager.getNamedBeanSet();
        boolean found = false;
        for (CatalogTree tree : manager.getNamedBeanSet()) {
            String sname = tree.getSystemName();
            if (log.isDebugEnabled()) {
                log.debug("Tree: sysName= {}, userName= {}", sname, tree.getUserName());
                CatalogTreeNode root = tree.getRoot();
                log.debug("enumerateTree called for root= {}, has {} children", root, root.getChildCount());

                @SuppressWarnings("unchecked") // root.depthFirstEnumeration isn't fully typed in JDOM2
                Enumeration<TreeNode> e = root.depthFirstEnumeration();
                while (e.hasMoreElements()) {
                    CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
                    log.debug("nodeName= {} has {} leaves and {} subnodes.", n.getUserObject(), n.getLeaves().size(), n.getChildCount());
                }
            }
            if (sname.charAt(1) == CatalogTree.XML) {
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
            java.util.Map<String, String> m = new java.util.HashMap<>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation + "panelfile.xsl");
            org.jdom2.ProcessingInstruction p = new org.jdom2.ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            store(root, trees);

            try {
                if (!checkFile(DEFAULT_FILE_NAME)) {
                    // file does not exist, create it
                    File file = new File(DEFAULT_FILE_NAME);
                    if (!file.createNewFile()) {
                        log.error("createNewFile failed");
                    }
                }
                // write content to file
                writeXML(findFile(DEFAULT_FILE_NAME), doc);
                // memory consistent with file
                InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(false);
            } catch (IOException ioe) {
                log.error("IO Exception writing CatalogTrees", ioe);
                throw (ioe);
            }
        }
    }

    /**
     * Default implementation for storing the contents of a CatalogTreeManager.
     *
     * @param cat   Element to load with contents
     * @param trees List of contents
     */
    public void store(Element cat, Set<CatalogTree> trees) {
        cat.setAttribute("class", "jmri.jmrit.catalog.DefaultCatalogTreeManagerConfigXML");
        for (CatalogTree ct : trees) {
            String sname = ct.getSystemName();
            log.debug("system name is {}", sname);
            if (sname.charAt(1) != CatalogTree.XML) {
                continue;
            }
            Element elem = new Element("catalogTree");
            elem.setAttribute("systemName", sname);
            String uname = ct.getUserName();
            if (uname != null) {
                elem.setAttribute("userName", uname);
            }

            storeNode(elem, ct.getRoot());

            log.debug("store CatalogTree {}", sname);
            cat.addContent(elem);
        }
    }

    /**
     * Recursively store a CatalogTree.
     *
     * @param parent the element to store node in
     * @param node   the root node of the tree
     */
    public void storeNode(Element parent, CatalogTreeNode node) {
        log.debug("storeNode {}, has {} leaves.", node, node.getLeaves().size());
        Element element = new Element("node");
        element.setAttribute("nodeName", node.toString());
        List<CatalogTreeLeaf> leaves = node.getLeaves();
        for (CatalogTreeLeaf leaf : leaves) {
            Element el = new Element("leaf");
            el.setAttribute("name", leaf.getName()); // prefer to store non-localized name
            el.setAttribute("path", leaf.getPath());
            element.addContent(el);
        }
        parent.addContent(element);
        Enumeration<TreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = (CatalogTreeNode) e.nextElement();
            storeNode(element, n);
        }
    }

    /**
     * This is invoked as part of the "store all" mechanism, which is not used
     * for these objects. Hence this is implemented to do nothing.
     *
     * @param o the object to store
     * @return null
     */
    public Element store(Object o) {
        return null;
    }

    /*
     * Read CatalogTree values from a file in the user's preferences directory.
     */
    public void readCatalogTrees() {
        log.debug("entered readCatalogTrees");
        //CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        try {
            // check if file exists
            if (checkFile(DEFAULT_FILE_NAME)) {
                Element root = rootFromName(DEFAULT_FILE_NAME);
                if (root != null) {
                    load(root);
                }
            } else {
                log.debug("File: {} not Found", DEFAULT_FILE_NAME);
            }
        } catch (org.jdom2.JDOMException | IOException jde) {
            log.error("Exception reading CatalogTrees", jde);
        }
    }

    /**
     * Create a CatalogTreeManager object of the correct class, then register
     * and fill it.
     *
     * @param catalogTrees top level Element to unpack
     * @return true if successful
     */
    public boolean load(Element catalogTrees) {
        loadCatalogTrees(catalogTrees);
        return true;
    }

    /**
     * Utility method to load the individual CatalogTree objects.
     *
     * @param catalogTrees element containing trees
     */
    public void loadCatalogTrees(Element catalogTrees) {
        List<Element> catList = catalogTrees.getChildren("catalogTree");
        log.debug("loadCatalogTrees: found {} CatalogTree objects", catList.size());
        CatalogTreeManager mgr = InstanceManager.getDefault(jmri.CatalogTreeManager.class);

        for (Element elem : catList) {
            Attribute attr = elem.getAttribute("systemName");
            if (attr == null) {
                log.warn("unexpected null systemName. elem= {}, attrs= {}", elem, elem.getAttributes());
                continue;
            }
            String sysName = attr.getValue();
            String userName;
            attr = elem.getAttribute("userName");
            if (attr == null) {
                log.warn("unexpected null userName. attrs= {}", elem.getAttributes());
                continue;
            } else {
                userName = attr.getValue();
            }
            CatalogTree ct = mgr.getBySystemName(sysName);
            if (ct != null) {
                continue;   // tree already registered
            }
            ct = mgr.newCatalogTree(sysName, userName);
            if (ct instanceof DefaultTreeModel) {
                log.debug("CatalogTree: sysName= {}, userName= {}", sysName, userName);
                CatalogTreeNode root = ct.getRoot();
                elem = elem.getChild("node");
                loadNode(elem, root, (DefaultTreeModel) ct);
            }
        }
    }

    /**
     * Recursively load a CatalogTree.
     *
     * @param element element containing the node to load
     * @param parent  the parent node of the node in element
     * @param model   the tree model containing the tree to add the node to
     */
    public void loadNode(Element element, CatalogTreeNode parent, DefaultTreeModel model) {
        List<Element> nodeList = element.getChildren("node");
        log.debug("Found {} CatalogTreeNode objects", nodeList.size());
        for (int i = 0; i < nodeList.size(); i++) {
            Element elem = nodeList.get(i);
            Attribute attr = elem.getAttribute("nodeName");
            if (attr == null) {
                log.warn("unexpected null nodeName. elem= {}, attrs= {}", elem, elem.getAttributes());
                continue;
            }
            String nodeName = attr.getValue();
            CatalogTreeNode n = new CatalogTreeNode(nodeName);
            addLeaves(elem, n);
            model.insertNodeInto(n, parent, parent.getChildCount());
            loadNode(elem, n, model);
        }
    }

    private void addLeaves(Element element, CatalogTreeNode node) {
        List<Element> leafList = element.getChildren("leaf");
        for (Element elem : leafList) {
            Attribute attr = elem.getAttribute("name");
            if (attr == null) {
                log.error("unexpected null leaf name. elem= {}, attrs= {}", elem, elem.getAttributes());
                continue;
            }
            String name = attr.getValue();
            attr = elem.getAttribute("path");
            if (attr == null) {
                log.error("unexpected null leaf path. elem= {}, attrs= {}", elem, elem.getAttributes());
                continue;
            }
            String path = attr.getValue();
            // use the method that maintains the same order
            node.addLeaf(new CatalogTreeLeaf(name, path, 0));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultCatalogTreeManagerXml.class);

}
