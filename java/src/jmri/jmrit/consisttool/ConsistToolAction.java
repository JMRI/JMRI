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
        setConsistManagerState();
    }

    public ConsistToolAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        setConsistManagerState();
    }

    public ConsistToolAction(String s) {
        super(s);
        setConsistManagerState();
    }

    private void setConsistManagerState () {
        // disable ourself if there is no consist manager available
        jmri.ConsistManager consistManager = jmri.InstanceManager.getNullableDefault(jmri.ConsistManager.class);
        if (consistManager == null) {
            setEnabled(false);
        } else if (consistManager.canBeDisabled()) {
            consistManager.registerEnableListener((value) -> {
                setEnabled(value);
            });
            setEnabled(consistManager.isEnabled());
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
