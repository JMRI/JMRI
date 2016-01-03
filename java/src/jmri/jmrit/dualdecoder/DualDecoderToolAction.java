// DualDecoderToolAction.java
package jmri.jmrit.dualdecoder;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a DualDecoderTool
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class DualDecoderToolAction extends JmriAbstractAction {

    public DualDecoderToolAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public DualDecoderToolAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public DualDecoderToolAction(String s) {
        super(s);

        // disable ourself if programming is not possible
        boolean enabled = false;
        if ((InstanceManager.getList(GlobalProgrammerManager.class) != null)
                && (InstanceManager.getList(GlobalProgrammerManager.class).size() > 0)) {
            enabled = true;
        }
        if ((InstanceManager.getList(AddressedProgrammerManager.class) != null)
                && (InstanceManager.getList(AddressedProgrammerManager.class).size() > 0)) {
            enabled = true;
        }

        setEnabled(enabled);
    }

    public DualDecoderToolAction() {
        this(Bundle.getMessage("MenuItemMultiDecoderControl"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new DualDecoderSelectFrame().setVisible(true);
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() { return null; } // not used by this classes actionPerformed, not migrated to new form yet

}

/* @(#)DualDecoderToolAction.java */
