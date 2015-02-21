// TreeAction.java
package jmri.jmrix.jinput.treecontrol;

import jmri.util.JmriJFrameAction;

/**
 * Create a JInput control window.
 *
 * @author Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class TreeAction extends JmriJFrameAction {

    /**
     *
     */
    private static final long serialVersionUID = 6718366926805444393L;

    public TreeAction(String s) {
        super(s);
    }

    public TreeAction() {
        this("USB Input Control");
    }

    @Override
    public String getName() {
        return "jmri.jmrix.jinput.treecontrol.TreeFrame";
    }
}

/* @(#)TreeAction.java */
