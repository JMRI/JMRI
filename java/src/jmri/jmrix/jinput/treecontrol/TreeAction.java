package jmri.jmrix.jinput.treecontrol;

import jmri.util.JmriJFrameAction;

/**
 * Create a JInput control window.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class TreeAction extends JmriJFrameAction {

    public TreeAction(String s) {
        super(s);
    }

    public TreeAction() {
        this(Bundle.getMessage("USBInputControl"));
    }

    @Override
    public String getName() {
        return "jmri.jmrix.jinput.treecontrol.TreeFrame";
    }
}
