package jmri.jmrit.simpleprog;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a SimpleProgAction object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SimpleProgAction extends JmriAbstractAction {

    public SimpleProgAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public SimpleProgAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public SimpleProgAction(String s) {
        super(s);

        // disable ourself if programming is not possible
        boolean enabled = false;
        if (InstanceManager.getList(GlobalProgrammerManager.class).size() > 0) {
            enabled = true;
        }
        if (InstanceManager.getList(AddressedProgrammerManager.class).size() > 0) {
            enabled = true;
        }

        setEnabled(enabled);
    }

    public SimpleProgAction() {
        this(Bundle.getMessage("MenuItemSingleCVProgrammer"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // create a SimpleProgFrame
        SimpleProgFrame f = new SimpleProgFrame();
        f.setVisible(true);

    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
