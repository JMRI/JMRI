package jmri.jmrit.display;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;

import jmri.InstanceManager;

/**
 * Start a PanelEditor.  Creates a JFrame to contain it, and another to
 * connect it to.
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.4 $
 * @see             jmri.jmrit.display.PanelEditorAction
 */
public class PanelEditorAction extends AbstractAction {

	public PanelEditorAction(String s) {
		super(s);
	}

    public void actionPerformed(ActionEvent e) {
        JFrame targetFrame = new JFrame("Panel");
        JLayeredPane targetPanel = new JLayeredPane();
        targetFrame.getContentPane().add(targetPanel);
        targetPanel.setLayout(null);

        targetFrame.show();

        PanelEditor panel = new PanelEditor();
        panel.setTarget(targetPanel);
        JFrame editFrame = new JFrame("PanelEditor");
        editFrame.getContentPane().add(panel);
        editFrame.pack();
        editFrame.show();

        // register the result for later configuration
        InstanceManager.configureManagerInstance().register(panel);

	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorAction.class.getName());

}
