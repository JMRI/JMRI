package jmri.jmrit.display;

import jmri.util.JmriJFrame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLayeredPane;

/**
 * Start a PanelEditor.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision: 1.13 $
 * @see         jmri.jmrit.display.PanelEditorAction
 */
public class PanelEditorAction extends AbstractAction {

    public PanelEditorAction(String s) {
        super(s);
    }

    public PanelEditorAction() {
        this("New Panel");
    }

    public void actionPerformed(ActionEvent e) {
        PanelEditor panel = new PanelEditor();
        JmriJFrame targetFrame = panel.makeFrame("Panel");
        targetFrame.setLocation(20,20);

        panel.setTitle();

        targetFrame.pack();
        targetFrame.show();

        panel.pack();
        panel.show();

	}
}
