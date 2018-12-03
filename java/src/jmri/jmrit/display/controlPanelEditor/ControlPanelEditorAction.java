package jmri.jmrit.display.controlPanelEditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrit.display.PanelMenu;

/**
 * Start a ControlPanelEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.display.controlPanelEditor.ControlPanelEditorAction
 */
public class ControlPanelEditorAction extends AbstractAction {

    public ControlPanelEditorAction(String s) {
        super(s);
    }

    public ControlPanelEditorAction() {
        this("New Panel");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = "Control Panel";
        for (int i = 2; i < 100; i++) {
            if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(name)) {
                name = "Panel " + i;
            }
        }
        ControlPanelEditor frame = new ControlPanelEditor(name);
        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(frame);
        frame.setLocation(20, 20);

        frame.setTitle();
        frame.initView();

        frame.pack();
        frame.setVisible(true);
        frame.newPanelDefaults();
    }
}
