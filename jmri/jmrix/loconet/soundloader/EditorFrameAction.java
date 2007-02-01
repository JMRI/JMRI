// EditorFrameAction.java

package jmri.jmrix.loconet.soundloader;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * LoaderFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2005
 * @version         $Revision: 1.2 $
 */
public class EditorFrameAction extends AbstractAction {
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.soundloader.Editor");

    public EditorFrameAction(String s) {
	    super(s);
        
    }

    public EditorFrameAction() {
        this(res.getString("TitleLoader"));
    }

    public void actionPerformed(ActionEvent e) {
        // create a EditorFrame
        EditorFrame f = new EditorFrame();
        f.setVisible(true);
    }
}

/* @(#)LoaderPanelAction.java */
