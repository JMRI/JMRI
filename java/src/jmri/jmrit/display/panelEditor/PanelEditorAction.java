package jmri.jmrit.display.panelEditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;

/**
 * Start a PanelEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.display.panelEditor.PanelEditorAction
 */
public class PanelEditorAction extends AbstractAction {

    public PanelEditorAction(String s) {
        super(s);
    }

    public PanelEditorAction() {
        this("New Panel");
    } // NOI18N

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = Bundle.getMessage("PanelDefaultName", ""); // "Panel"
        for (int i = 2; i < 100; i++) {
            if (InstanceManager.getDefault(EditorManager.class).contains(name)) {
                name = Bundle.getMessage("PanelDefaultName", i);
            }
        }
        PanelEditor frame = new PanelEditor(name);
        InstanceManager.getDefault(EditorManager.class).add(frame);
        InstanceManager.getDefault(EditorManager.class).setChanged(true);
        frame.setLocation(20, 20);

        frame.setTitle();

        frame.pack();
        frame.setVisible(true);
        frame.newPanelDefaults();
    }
}
