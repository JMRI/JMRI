package jmri.jmrit.display;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;

/**
 * Start a PanelEditor.  Creates a JFrame to contain it, and another to
 * connect it to.
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.9 $
 * @see             jmri.jmrit.display.PanelEditorAction
 */
public class PanelEditorAction extends AbstractAction {

	public PanelEditorAction(String s) {
		super(s);
	}

    public PanelEditorAction() {
        this("New Panel");
    }

    public void actionPerformed(ActionEvent e) {
        JFrame targetFrame = new JFrame("Panel");
        targetFrame.setSize(200,200);
        JLayeredPane targetPanel = new JLayeredPane();
        targetFrame.getContentPane().add(targetPanel);
        targetPanel.setLayout(null);

        targetFrame.show();

        PanelEditor panel = new PanelEditor();
        panel.setFrame(targetFrame);
        panel.setTarget(targetPanel);
        panel.pack();
        panel.show();

	}

	// initialize logging
    //static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorAction.class.getName());

}
