// TreeAction.java

package jmri.jmrix.jinput.treecontrol;


/**
 * Create a JInput control window.
 *
 * @author   Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class TreeAction extends jmri.util.JmriJFrameAction {

    public TreeAction(String s) { 
        super(s);
    }

    public TreeAction() {
        this("USB Input Control");
    }

    public String getName() {
        return "jmri.jmrix.jinput.treecontrol.TreeFrame";
    }
}

/* @(#)TreeAction.java */
