package jmri.jmrit.roster.swing.rostertree;


import jmri.jmrit.roster.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * Create a TreeNode representing the entire Roster.
 *
 * <P>
 * @author	Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision$
 */

public class RosterTreeNode extends DefaultMutableTreeNode {
    
    public RosterTreeNode() { super(); }
    
    /**
     * Initialize the connection to the Roster.
     * <p>
     * Should be called before connecting the node to a 
     * JTree.
     */
    public void initComponents() {
    
        // title this node
        setUserObject("Roster");
        
        // add every roster entry
        List<RosterEntry> list = Roster.instance().matchingList(null, null, null, null, null, null, null );

        for (RosterEntry r : list) {
            add(new DefaultMutableTreeNode(r.getId()));
        }
    }
}