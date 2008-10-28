package jmri.jmrit.display;

import jmri.util.JmriJFrame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Start a PanelEditor.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision: 1.17 $
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
			String name = "Panel";
			for (int i = 2; i < 100; i++){
				if(jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)){
					name = "Panel " +i;
				}
			}
			JmriJFrame targetFrame = panel.makeFrame(name);
			jmri.jmrit.display.PanelMenu.instance().addPanelEditorPanel(panel);
			targetFrame.setLocation(20,20);
        
			panel.setTitle();

			targetFrame.pack();
			targetFrame.setVisible(true);

			panel.pack();
			panel.setVisible(true);
	}
}
