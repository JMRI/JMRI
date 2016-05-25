package jmri.jmrit.display.panelEditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Start a PanelEditor.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 * @see jmri.jmrit.display.panelEditor.PanelEditorAction
 */
public class PanelEditorAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 1057276169969379561L;

    public PanelEditorAction(String s) {
        super(s);
    }

    public PanelEditorAction() {
        this("New Panel");
    }

    public void actionPerformed(ActionEvent e) {
        String name = "Panel";
        for (int i = 2; i < 100; i++) {
            if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)) {
                name = "Panel " + i;
            }
        }
        PanelEditor frame = new PanelEditor(name);
        jmri.jmrit.display.PanelMenu.instance().addEditorPanel(frame);
        frame.setLocation(20, 20);

        frame.setTitle();

        frame.pack();
        frame.setVisible(true);
    }
}
