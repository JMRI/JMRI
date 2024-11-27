package jmri.jmrix.openlcb.swing.lccpro;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.InstanceManager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

import org.openlcb.MimicNodeStore;

/**
 * AbstractAction that refeshes the underlying LCC node store
 *
 * @author Bob Jacobsen  Copyright (C) 2024
 */
public class RefreshAction extends JmriAbstractAction {

    public RefreshAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public RefreshAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * Default constructor used when instantiating via startup action or button
     * configured in user preferences
     */
    public RefreshAction() {
        super(Bundle.getMessage("RefreshAction")); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class)
            .get(MimicNodeStore.class).refresh();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
