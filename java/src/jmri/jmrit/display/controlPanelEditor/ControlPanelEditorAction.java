package jmri.jmrit.display.controlPanelEditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Start a ControlPanelEditor.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.display.controlPanelEditor.ControlPanelEditorAction
 */
public class ControlPanelEditorAction extends AbstractAction {

    public ControlPanelEditorAction(String s) {
        super(s);
    }

    public ControlPanelEditorAction() {
        this("New Panel");
    }

    public void actionPerformed(ActionEvent e) {
        String name = "Control Panel";
        for (int i = 2; i < 100; i++) {
            if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)) {
                name = "Panel " + i;
            }
        }
        ControlPanelEditor frame = new ControlPanelEditor(name);
        jmri.jmrit.display.PanelMenu.instance().addEditorPanel(frame);
        frame.setLocation(20, 20);

        frame.setTitle();
        frame.initView();

        frame.pack();
        frame.setVisible(true);
    }
}
