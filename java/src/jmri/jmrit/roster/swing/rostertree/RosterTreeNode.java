package jmri.jmrit.roster.swing.rostertree;

import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Create a TreeNode representing the entire Roster.
 *
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version	$Revision$
 */
public class RosterTreeNode extends DefaultMutableTreeNode {

    /**
     *
     */
    private static final long serialVersionUID = -7965184515372170059L;

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
        List<RosterEntry> list = Roster.instance().matchingList(null, null, null, null, null, null, null);

        for (RosterEntry r : list) {
            add(new DefaultMutableTreeNode(r.getId()));
        }
    }
}
