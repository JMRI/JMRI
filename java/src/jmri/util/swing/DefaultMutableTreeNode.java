package jmri.util.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Facial subclass for {@link javax.swing.tree.DefaultMutableTreeNode}
 * to limit linkage for i.e. {@link jmri.CatalogTreeNode} use.
 *
 * @author Bob Jacobsen
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Desired pattern is repeated class names to shift dependency")
@API(status = EXPERIMENTAL)
public class DefaultMutableTreeNode extends javax.swing.tree.DefaultMutableTreeNode {

    public DefaultMutableTreeNode(String name) {
        super(name);
    }

}
