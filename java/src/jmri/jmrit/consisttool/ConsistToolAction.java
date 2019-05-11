package jmri.jmrit.consisttool;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a ConsistToolFrame object.
 *
 * @author Paul Bender Copyright (C) 2003
 */
public class ConsistToolAction extends JmriAbstractAction {

    public ConsistToolAction(String s, WindowInterface wi) {
        super(s, wi);
        // disable ourself if there is no consist manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.ConsistManager.class) == null) {
            setEnabled(false);
        }
    }

    public ConsistToolAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        // disable ourself if there is no consist manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.ConsistManager.class) == null) {
            setEnabled(false);
        }
    }

    public ConsistToolAction(String s) {
        super(s);

        // disable ourself if there is no consist manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.ConsistManager.class) == null) {
            setEnabled(false);
        }

    }

    public ConsistToolAction() {
        this(Bundle.getMessage("MenuItemConsistTool"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        ConsistToolFrame f = new ConsistToolFrame();
        f.setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
