package jmri.util.swing;

import javax.swing.tree.DefaultMutableTreeNode;
import org.jdom2.Element;

/**
 * Common utility methods for working with JTrees.
 * <p>
 * Chief among these is the loadTree method, for creating a tree from an XML
 * definition
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @since 2.9.4
 */
public class JTreeUtil extends GuiUtilBase {

    /**
     * @param name    XML file to be read and processed
     * @param wi      WindowInterface to be passed to the nodes in the tree
     * @param context Blind context Object passed to the nodes in the tree
     * @return a mutable tree node
     */
    static public DefaultMutableTreeNode loadTree(String name, WindowInterface wi, Object context) {
        Element root = rootFromName(name);

        return treeFromElement(root, wi, context);
    }

    /**
     * @param main    Element to be processed
     * @param wi      WindowInterface to be passed to the nodes in the tree
     * @param context Blind context Object passed to the nodes in the tree
     * @return a mutable tree node
     */
    static DefaultMutableTreeNode treeFromElement(Element main, WindowInterface wi, Object context) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) {
            name = e.getText();
        }

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        node.setUserObject(actionFromNode(main, wi, context));

        main.getChildren("node").stream().forEach((child) -> {
            node.add(treeFromElement(child, wi, context));
        });
        return node;
    }
}
