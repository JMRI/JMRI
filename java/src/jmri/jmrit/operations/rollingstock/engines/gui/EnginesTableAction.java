package jmri.jmrit.operations.rollingstock.engines.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a EnginesTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class EnginesTableAction extends AbstractAction {

    public EnginesTableAction() {
        super(Bundle.getMessage("MenuEngines")); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a engine table frame
        new EnginesTableFrame();
    }
}


