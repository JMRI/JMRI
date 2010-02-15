// JTreeUtil.java

package jmri.util.swing;

import javax.swing.tree.*;
import org.jdom.*;

/**
 * Common utility methods for working with JTrees.
 * <P>
 * Chief among these is the loadTree method, for
 * creating a tree from an XML definition
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision: 1.3 $
 */

public class JTreeUtil extends GuiUtilBase {

    static public TreeNode loadTree(String filename, WindowInterface wi) {
        Element root;
        
        try {
            root = new jmri.jmrit.XmlFile(){}.rootFromName(filename);
        } catch (Exception e) {
            log.error("Could not parse JTree file \""+filename+"\" due to: "+e);
            return null;
        }
        return treeFromElement(root, wi);
    }
    
    static DefaultMutableTreeNode treeFromElement(Element main, WindowInterface wi) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) name = e.getText();

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        node.setUserObject(actionFromNode(main, wi));

        for (Object child : main.getChildren("node")) {
            node.add(treeFromElement((Element)child, wi));
        }
        return node;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JTreeUtil.class.getName());
}