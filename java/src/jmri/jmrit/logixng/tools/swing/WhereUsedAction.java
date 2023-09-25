package jmri.jmrit.logixng.tools.swing;

import java.awt.event.ActionEvent;

import jmri.util.swing.JmriAbstractAction;

/**
 * Swing action to create and register a WhereUsedFrame
 *
 * @author Dave Sand Copyright (C) 2020
 */
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
