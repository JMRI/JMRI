package jmri.util.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Facial subclass for {@link javax.swing.tree.DefaultMutableTreeNode}
 * to limit linkage for i.e. {@link jmri.CatalogTreeNode} use.
 *
 * @author Bob Jacobsen
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Desired pattern is repeated class names to shift dependency")
public class DefaultMutableTreeNode extends javax.swing.tree.DefaultMutableTreeNode {

    public DefaultMutableTreeNode(String name) {
        super(name);
    }

}
