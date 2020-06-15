package jmri.util.swing;

/**
 * Facial subclass for {@link javax.swing.tree.DefaultMutableTreeNode}
 * to limit linkage for i.e. {@link jmri.CatalogTreeNode} use.
 *
 * @author Bob Jacobsen
 */
public class DefaultMutableTreeNode extends javax.swing.tree.DefaultMutableTreeNode {

    public DefaultMutableTreeNode(String name) {
        super(name);
    }

}
