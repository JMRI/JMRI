package jmri.jmrit.whereused;

import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a WhereUsedFrame
 *
 * @author Dave Sand Copyright (C) 2020
 */
@API(status = MAINTAINED)
public class WhereUsedAction extends JmriAbstractAction {

    public WhereUsedAction(String s) {
        super(s);
    }

    public WhereUsedAction() {
        this("WhereUsed");  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WhereUsedFrame f = new WhereUsedFrame();
        f.setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");  // NOI18N
    }
}
