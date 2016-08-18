package jmri.jmrit.display.layoutEditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Start a LayoutEditor.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @see jmri.jmrit.display.panelEditor.PanelEditorAction
 */
public class LayoutEditorAction extends AbstractAction {

    public LayoutEditorAction(String s) {
        super(s);
    }

    public LayoutEditorAction() {
        this("New Panel");
    }

    public void actionPerformed(ActionEvent e) {
        String name = "My Layout";
        for (int i = 2; i < 100; i++) {
            if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)) {
                name = "My Layout " + i;
            }
        }
        LayoutEditor panel = new LayoutEditor(name);
        panel.pack();
        panel.setVisible(true);
        panel.setAllEditable(true);
        panel.setCurrentPositionAndSize();
    }
}
