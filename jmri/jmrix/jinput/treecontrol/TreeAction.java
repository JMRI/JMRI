// TreeAction.java

package jmri.jmrix.jinput.treecontrol;

import java.awt.event.ActionEvent;

/**
 * Create a JInput control window.
 *
 * @author   Bob Jacobsen Copyright 2008
 * @version	$Revision: 1.1 $
 */
public class TreeAction extends jmri.util.JmriJFrameAction {

    public TreeAction(String s) { 
        super(s);
    }

    public TreeAction() {
        this("USB Controls");
    }

    public String getName() {
        return "jmri.jmrix.jinput.treecontrol.TreeFrame";
    }
}

/* @(#)TreeAction.java */
