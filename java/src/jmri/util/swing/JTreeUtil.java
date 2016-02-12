// JTreeUtil.java
package jmri.util.swing;

import javax.swing.tree.DefaultMutableTreeNode;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for working with JTrees.
 * <P>
 * Chief among these is the loadTree method, for creating a tree from an XML
 * definition
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @version $Revision$
 * @since 2.9.4
 */
public class JTreeUtil extends GuiUtilBase {

    /**
     * @param name    XML file to be read and processed
     * @param wi      WindowInterface to be passed to the nodes in the tree
     * @param context Blind context Object passed to the nodes in the tree
     */
    static public DefaultMutableTreeNode loadTree(String name, WindowInterface wi, Object context) {
        Element root = rootFromName(name);

        return treeFromElement(root, wi, context);
    }

    /**
     * @param main    Element to be processed
     * @param wi      WindowInterface to be passed to the nodes in the tree
     * @param context Blind context Object passed to the nodes in the tree
     */
    static DefaultMutableTreeNode treeFromElement(Element main, WindowInterface wi, Object context) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) {
            name = e.getText();
        }

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        node.setUserObject(actionFromNode(main, wi, context));

        for (Object child : main.getChildren("node")) {
            node.add(treeFromElement((Element) child, wi, context));
        }
        return node;
    }

    private final static Logger log = LoggerFactory.getLogger(JTreeUtil.class.getName());
}
