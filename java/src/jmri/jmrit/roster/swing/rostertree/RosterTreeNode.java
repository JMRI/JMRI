package jmri.jmrit.roster.swing.rostertree;

import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Create a TreeNode representing the entire Roster.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class RosterTreeNode extends DefaultMutableTreeNode {

    public RosterTreeNode() {
        super();
    }

    /**
     * Initialize the connection to the Roster.
     * <p>
     * Should be called before connecting the node to a JTree.
     */
    public void initComponents() {

        // title this node
        setUserObject("Roster");

        // add every roster entry
        List<RosterEntry> list = Roster.getDefault().matchingList(null, null, null, null, null, null, null);

        for (RosterEntry r : list) {
            add(new DefaultMutableTreeNode(r.getId()));
        }
    }
}
